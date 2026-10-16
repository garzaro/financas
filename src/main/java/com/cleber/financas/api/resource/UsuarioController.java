package com.cleber.financas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.cleber.financas.api.common.GlobalExceptionHandler;
import com.cleber.financas.api.common.RefreshTokenCookieFactory;
import com.cleber.financas.api.converter.UsuarioConverter;
import com.cleber.financas.api.dto.AuthLoginDTO;
import com.cleber.financas.api.dto.TokenResponseDTO;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;
import com.cleber.financas.service.token.RefreshTokenEmitido;
import com.cleber.financas.service.token.RefreshTokenService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Controlador dos endpoints de autenticação e gestão de usuários.
 *
 * Base: {@code /api/auth}
 * POST /sign-in      — autentica e emite access token (body) + refresh token (cookie)
 * POST /refresh      — rotaciona o refresh token e emite novo access token
 * POST /logout       — revoga as sessões do usuário e limpa o cookie
 * POST /join/sign-up — cadastro
 *
 * Erros são delegados ao {@link GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenCookieFactory cookieFactory;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final LancamentoService lancamentoService;
    private final UsuarioConverter usuarioConverter;

    public UsuarioController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            LancamentoService lancamentoService,
            UsuarioConverter usuarioConverter,
            RefreshTokenService refreshTokenService,
            RefreshTokenCookieFactory cookieFactory) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.lancamentoService = lancamentoService;
        this.usuarioConverter = usuarioConverter;
        this.refreshTokenService = refreshTokenService;
        this.cookieFactory = cookieFactory;
    }

    /** Autentica o usuário e emite o par de tokens. Credenciais inválidas → 401. */
    @PostMapping("/sign-in")
    public ResponseEntity<TokenResponseDTO> login(
            @RequestBody @Valid AuthLoginDTO authDto,
            HttpServletRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authDto.email(), authDto.senha()));

        Usuario usuario = usuarioRepository.findByEmail(authDto.email())
                .orElseThrow(() -> new ErroDeAutenticacao("Credenciais inválidas."));

        String tokenAcesso = jwtService.gerarToken(usuario);

        RefreshTokenEmitido refreshEmitido = refreshTokenService.gerar(
                usuario.getUuid(), clienteIp(request), request.getHeader("User-Agent"));

        ResponseCookie cookie = cookieFactory
                .buildSet(refreshEmitido.rawToken(), refreshEmitido.ttl());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString()) //refresh vem aqui
                .body(new TokenResponseDTO(tokenAcesso)); //corpo somente com token de acesso
    }

    /** Rotaciona o refresh token (cookie) e emite novo access token. */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false)
            String refreshTokenAntigo,
            HttpServletRequest request) {

        if (refreshTokenAntigo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshTokenEmitido emitido = refreshTokenService.rotacionar(
                refreshTokenAntigo, clienteIp(request), request.getHeader("User-Agent"));

        Usuario usuario = refreshTokenService.validar(emitido.rawToken());
        String accessTokenNovo = jwtService.gerarToken(usuario);

        ResponseCookie cookie = cookieFactory
                .buildSet(emitido.rawToken(), emitido.ttl());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new TokenResponseDTO(accessTokenNovo));
    }

    /** Revoga todas as sessões do usuário e limpa o cookie. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME)
            String refreshToken) {

        Usuario usuario = refreshTokenService.validar(refreshToken);
        refreshTokenService.revogarAllByUser(usuario.getUuid());

        ResponseCookie liberado = cookieFactory.buildClear();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, liberado.toString())
                .build();
    }

    /** Endpoints de gestão de usuário. */
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

    private String clienteIp(HttpServletRequest request) {
        String encaminhado = request.getHeader("X-Forwarded-For");
        return (encaminhado != null && !encaminhado.isBlank())
                ? encaminhado.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
