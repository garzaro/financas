package com.cleber.financas.service.impl;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.cleber.financas.api.dto.AutenticacaoResultado;
import com.cleber.financas.api.dto.RequestLoginDTO;
import com.cleber.financas.api.dto.TokenResponseDTO;
import com.cleber.financas.service.token.RefreshTokenEmitido;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.ErroValidacaoException;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.exception.TooManyRequestsException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.security.ratelimit.LimiteTentativasService;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.UsuarioService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
@Validated
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final LimiteTentativasService limiteTentativasService;
    private final com.cleber.financas.service.token.RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long loginFalhasMax;
    private final long loginFalhasWindowSec;
    private final long loginBloqueioFalhasSec;

    @Autowired
    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            Validator validator,
            LimiteTentativasService limiteTentativasService,
            com.cleber.financas.service.token.RefreshTokenService refreshTokenService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${app.ratelimit.login.fail.max}") long loginFalhasMax,
            @Value("${app.ratelimit.login.fail.window.sec}") long loginFalhasWindowSec,
            @Value("${app.ratelimit.login.fail.block.sec}") long loginBloqueioFalhasSec
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.limiteTentativasService = limiteTentativasService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginFalhasMax = loginFalhasMax;
        this.loginFalhasWindowSec = loginFalhasWindowSec;
        this.loginBloqueioFalhasSec = loginBloqueioFalhasSec;
    }

    private static final List<String> dominiosEmailPermitidos = List.of(
            "gmail.com", "edu.br", "gov.br"
    );

    @Override
    public AutenticacaoResultado autenticar(RequestLoginDTO request, String ip, String agenteUsuario) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String chave = emailChave(email);

        if (limiteTentativasService.isBloqueado(chave)) {
            throw new TooManyRequestsException("Muitas tentativas",
                    limiteTentativasService.obterRetryAfter(chave));
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email, request.senha()
                    ));
        } catch (BadCredentialsException ex) {
            limiteTentativasService.registrar(chave, loginFalhasMax,
                    Duration.ofSeconds(loginFalhasWindowSec),
                    Duration.ofSeconds(loginBloqueioFalhasSec));
            throw ex;
        }
        limiteTentativasService.limpar(chave);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new
                        ErroDeAutenticacao("Credenciais inválidas!"));

        TokenResponseDTO tokens = new TokenResponseDTO(jwtService
                .gerarToken(usuario, List.of("USER")));
        RefreshTokenEmitido refresh = refreshTokenService
                .gerar(usuario.getUuid(), ip, agenteUsuario);

        return new AutenticacaoResultado(tokens, refresh);
    }

    private String emailChave(String email) {
        if (email == null) {
            return "rl:login:fail:email:unknown";
        }
        return "rl:login:fail:email:" + email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    @Override
    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().trim().toLowerCase(Locale.ROOT));
        }
        validarEmailCpf(usuario.getEmail(), usuario.getCpf());
        validarUsuario(usuario);
        usuario.setIsAtivo(true);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario atualizarUsuario(Usuario usuario) {
        Objects.requireNonNull(usuario.getUuid());
        validarUsuario(usuario);
        return usuarioRepository.save(usuario);
    }

    @Override
    public void validarUsuario(Usuario usuario) {
        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario);
        if (!violations.isEmpty()) {
            String mensagemErro = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new ErroValidacaoException(mensagemErro);
        }
    }

    @Override
    public void validarEmailCpf(String email, String cpf) {
        Optional<Usuario> usuarioEmailExiste = usuarioRepository.findByEmail(email);
        if (usuarioEmailExiste.isPresent()) {
            throw new RegraDeNegocioException("Verifique este email e tente novamente");
        }
        Optional<Usuario> usuarioCpfExiste = usuarioRepository.findByCpf(cpf);
        if (usuarioCpfExiste.isPresent()) {
            throw new RegraDeNegocioException("Verifique este CPF e tente novamente");
        }
    }

    @Override
    public Optional<Usuario> obterUsuarioPorId(UUID uuid) {
        return usuarioRepository.findById(uuid);
    }

    @Override
    public Optional<Usuario> obterUsuarioPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }

    @Override
    @Transactional
    public void trocarSenha(UUID usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ErroValidacaoException("Usuário não encontrado"));

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new ErroValidacaoException("A senha atual informada está incorreta");
        }

        alterarSenha(usuarioId, novaSenha);
    }

    @Override
    @Transactional
    public void alterarSenha(UUID usuarioId, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ErroValidacaoException("Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        refreshTokenService.revogarAllByUser(usuarioId);
    }
}
