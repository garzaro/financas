package com.cleber.financas.service.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.cleber.financas.api.dto.RegisterRequest;
import com.cleber.financas.exception.EmailJaCadastradoException;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.ErroValidacaoException;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * TODO-List
 * [] - Não permitir usuario salvar senha com o proprio nome
 */
@Service
@Validated
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
//    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            Validator validator) {
        this.usuarioRepository = usuarioRepository;
//        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    /**
     * lista de emails permitidos
     */
    private static final java.util.List<String> dominiosEmailPermitidos = java.util.List.of(
            "gmail.com", "edu.br", "gov.br"
    );

    // -------------------------------------------------------------------------
    // Fluxo de autenticação legado (usado pelo endpoint /api/auth/autenticar)
    // -------------------------------------------------------------------------

    /**
     * login, validação e autenticação
     */
    @Override
    public Usuario autenticar(String email, String senha) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);

        if (!usuario.isPresent()) {
            throw new ErroDeAutenticacao("Verifique seu email e tente novamente.");
        }
        boolean senhaCorreta = passwordEncoder().matches(senha, usuario.get().getSenha());
        if (!senhaCorreta) {
            throw new ErroDeAutenticacao("Credenciais inválidas.");
        }
        return usuario.get();
    }

    // -------------------------------------------------------------------------
    // Fluxo de registro JWT — POST /auth/register
    // -------------------------------------------------------------------------

    /**
     * Registra um novo usuário a partir do DTO simplificado (nome + email + senha).
     * Valida duplicidade de email antes de persistir; encodifica a senha com o
     * PasswordEncoder exposto pelo SecurityConfig (Argon2).
     * Não loga a senha em nenhum nível.
     */
    @Override
    @Transactional
    public Usuario registrar(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException("E-mail já cadastrado: " + request.email());
        }

        Usuario novoUsuario = Usuario.builder()
                .nomeCompleto(request.nome())
                .email(request.email())
                .senha(passwordEncoder().encode(request.senha()))
                // cpf e nomeUsuario ficam nulos neste fluxo; podem ser completados depois
                .build();

        Usuario salvo = usuarioRepository.save(novoUsuario);
        log.info("Novo usuário registrado com sucesso — email: {}", salvo.getEmail());
        return salvo;
    }

    // -------------------------------------------------------------------------
    // Fluxo completo (com CPF) — mantido para retrocompatibilidade
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        /* deve validar o email e o cpf, verificar se existe*/
        validarEmailCpf(usuario.getEmail(), usuario.getCpf());
        validarUsuario(usuario);
        usuario.setSenha(passwordEncoder().encode(usuario.getSenha())); /* hash da senha */
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario atualizarUsuario(Usuario usuario) {
        Objects.requireNonNull(usuario.getId());
        validarUsuario(usuario);
        return usuarioRepository.save(usuario);
    }

    /**
     * VALIDAÇÃO para garantir a integridade dos dados — campos vazio
     */
    @Override
    public void validarUsuario(Usuario usuario) {
        Set<ConstraintViolation<Usuario>> violacao = validator.validate(usuario);
        if (!violacao.isEmpty()) {
            String mensagemErro = violacao.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ErroValidacaoException(mensagemErro);
        }
    }

    /**
     * validação de existencia (email + CPF)
     */
    @Override
    public void validarEmailCpf(String email, String cpf) {
        Optional<Usuario> usuarioEmailExiste = usuarioRepository.findByEmail(email);
        if (usuarioEmailExiste.isPresent()) {
            throw new RegraDeNegocioException("Esse email já está em uso");
        }
        Optional<Usuario> usuarioCpfExiste = usuarioRepository.findByCpf(cpf);
        if (usuarioCpfExiste.isPresent()) {
            throw new RegraDeNegocioException("Esse CPF já está em uso");
        }
    }

    @Override
    public Optional<Usuario> obterUsuarioPorId(UUID id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> obterUsuarioPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }
}

 /*

//@Override
    public Usuario autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ErroDeAutenticacao("Credenciais inválidas."));
        /**
         * System.out.println("Hash do banco: " + usuario.getSenha());
         *         System.out.println("Senha digitada: " + senha);
         * *

        boolean senhaCorreta = passwordEncoder.matches(senha, usuario.getSenha());
        System.out.println("Resultado da comparação: " + senhaCorreta);

        if (!senhaCorreta) {
            throw new ErroDeAutenticacao("Credenciais inválidas.");
        }
        return usuario;
    }*/

