package com.cleber.financas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.token.RefreshTokenEmitido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cleber.financas.api.common.GlobalExceptionHandler;
import com.cleber.financas.api.common.RefreshTokenCookieFactory;
import com.cleber.financas.api.converter.UsuarioConverter;
import com.cleber.financas.api.dto.AuthLoginDTO;
import com.cleber.financas.api.dto.TokenResponseDTO;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;
import com.cleber.financas.service.token.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Controlador responsável pelos endpoints de autenticação e gestão de usuários.
 *
 * Mapeamento base: {@code /api/auth}
 *
 * POST /auth/register — registro de novo usuário (criação de conta; sem accesstoken)
 * POST /auth/login    — autenticação e emissão de JWT
 *
 * Tratamento de erros é delegado ao {@link GlobalExceptionHandler}:
 * {@code BadCredentialsException} → 401, {@code EmailJaCadastradoException} → 409,
 * {@code MethodArgumentNotValidException} → 400.
 */
@RestController
@RequestMapping("/api/auth") //api/usuario
//@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenCookieFactory cookieFactory;
    private final UsuarioService usuarioService;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final LancamentoService lancamentoService;
    private final UsuarioConverter usuarioConverter;

    public UsuarioController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            LancamentoService lancamentoService,
            UsuarioConverter usuarioConverter,
            RefreshTokenService refreshTokenService,
            RefreshTokenCookieFactory cookieFactory) {
        super();
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.lancamentoService = lancamentoService;
        this.usuarioConverter = usuarioConverter;
        this.refreshTokenService = refreshTokenService;
        this.cookieFactory = cookieFactory;
    }

    /**LoginRequest
     * Autentica o usuário e retorna um JWT.
     *
     * Caso as credenciais sejam inválidas, o {@code AuthenticationManager}
     * lança {@code BadCredentialsException}, que é capturada pelo handler global
     * e respondida com 401 — nunca revelando se foi o email ou a senha que errou.
     *
     * @return 200 OK com {@link TokenResponseDTO} contendo o accesstoken e o tipo "Bearer"
     */
    @PostMapping("/sign-in")
    public ResponseEntity<TokenResponseDTO> login(
            @RequestBody @Valid AuthLoginDTO authDto,
            HttpServletRequest request) {

        var authenticationToken =
                new UsernamePasswordAuthenticationToken(authDto.email(), authDto.senha());
        authenticationManager.authenticate(authenticationToken); //validação da senha

        Usuario usuario = usuarioRepository.findByEmail(authDto.email())
                .orElseThrow(() -> new ErroDeAutenticacao("Credenciais inválidas."));

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String tokenAcesso = jwtService.gerarToken(userDetails);

        RefreshTokenEmitido emitido = refreshTokenService.gerar(
                usuario.getUuid(), clientIp(request), request.getHeader("User-Agent"));
//    VERIFICAR O ERRO DO CLIENTIP
        ResponseCookie cookie = cookieFactory.buildSet(emitido.rawToken(), emitido.ttl());

        log.info("Login bem-sucedido — email: {}", authDto.email());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(tokenAcesso));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME) String oldRefreshToken,
            HttpServletRequest request) {
        String newRefreshToken = refreshTokenService.rotacionar(oldRefreshToken, request);
        Usuario usuario = refreshTokenService.validar(newRefreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String newAccessToken = jwtService.gerarToken(userDetails);

        ResponseCookie cookie = cookieFactory.buildSet(newRefreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME) String refreshToken) {
        Usuario usuario = refreshTokenService.validar(refreshToken);
        refreshTokenService.revogarAllByUser(usuario.getUuid());

        ResponseCookie cleared = cookieFactory.buildClear();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .build();
    }

    /**
     * Endpoints de gestão de usuário
     * **/
    @PostMapping("/join/sign-up")
    public ResponseEntity<Usuario> salvarUsuario(@RequestBody @Valid UsuarioDTO dto) {
        Usuario usuario = Usuario.builder()
                .nomeCompleto(dto.getNomeCompleto())
                .cpf(dto.getCpf())
                .nomeUsuario(dto.getNomeUsuario())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .build();

        Usuario usuarioSalvo = usuarioService.salvarUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSalvo);
    }

    @PutMapping("{id}")
    public ResponseEntity<Usuario> atualizar(
            @PathVariable("id") UUID id,
            @RequestBody @Valid UsuarioDTO dto) {
        return usuarioService.obterUsuarioPorId(id)
                .map(entity -> {
                    Usuario usuario = usuarioConverter.dtoToEntity(dto);
                    usuario.setUuid(id);
                    usuarioService.atualizarUsuario(usuario);
                    return ResponseEntity.ok(usuario);
                })
                .orElseThrow(() -> new RegraDeNegocioException("O usuario informado não foi encontrado"));
    }

    @GetMapping("{id}/saldo")
    public ResponseEntity<?> obterSaldo(@PathVariable("id") UUID id) {
        Optional<Usuario> usuario = usuarioService.obterUsuarioPorId(id);

        if (!usuario.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);
        return ResponseEntity.ok(saldo);
    }
}
