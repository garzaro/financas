package com.cleber.financas.api.resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.cleber.financas.api.common.GlobalExceptionHandler;
import com.cleber.financas.api.cookie.RefreshTokenCookieFactory;
import com.cleber.financas.api.converter.UsuarioConverter;
import com.cleber.financas.api.dto.AlterarSenhaDTO;
import com.cleber.financas.api.dto.AutenticacaoResultado;
import com.cleber.financas.api.dto.RequestLoginDTO;
import com.cleber.financas.api.dto.TokenResponseDTO;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.security.UsuarioAutenticado;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;
import com.cleber.financas.service.token.RefreshTokenEmitido;
import com.cleber.financas.service.token.RefreshTokenService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * POST /join/sign-up — cadastro
 * POST /sign-in      — autentica e emite access token (body) + refresh token (cookie)
 * POST /refresh      — rotaciona o refresh token e emite novo access token
 * POST /logout       — revoga as sessões do usuário e limpa o cookie
 *
 * Erros são delegados ao {@link GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UsuarioService usuarioService;
    private final LancamentoService lancamentoService;
    private final UsuarioConverter usuarioConverter;
    private final RefreshTokenCookieFactory cookieFactory;

    public UsuarioController(
            JwtService jwtService,
            UsuarioService usuarioService,
            LancamentoService lancamentoService,
            UsuarioConverter usuarioConverter,
            RefreshTokenService refreshTokenService,
            RefreshTokenCookieFactory cookieFactory
    ) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.lancamentoService = lancamentoService;
        this.usuarioConverter = usuarioConverter;
        this.refreshTokenService = refreshTokenService;
        this.cookieFactory = cookieFactory;
    }

    /** Autentica o usuário e emite o par de tokens. Credenciais inválidas → 401. */
    @PostMapping("/sign-in")
    public ResponseEntity<TokenResponseDTO> login(
            @RequestBody @Valid RequestLoginDTO request,
            HttpServletRequest httpRequest) {

        AutenticacaoResultado resultado = usuarioService.autenticar(
                request,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );

        ResponseCookie cookie = cookieFactory
                .buildSet(resultado.refresh().rawToken(), resultado.refresh().ttl());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(resultado.tokens());
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
                refreshTokenAntigo, request.getRemoteAddr(), request.getHeader("User-Agent"));

        Usuario usuario = refreshTokenService.validar(emitido.rawToken());
        String accessTokenNovo = jwtService.gerarToken(usuario, java.util.List.of("USER"));

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

    /** Altera a senha do usuário autenticado e revoga todas as suas sessões ativas. */
    @PostMapping("/trocar-senha")
    public ResponseEntity<Void> trocarSenha(
            @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado,
            @RequestBody @Valid AlterarSenhaDTO dto) {

        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        usuarioService.trocarSenha(usuarioAutenticado.getUsuarioUuid(), dto.senhaAtual(), dto.novaSenha());
        return ResponseEntity.noContent().build();
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

    @PutMapping("{uuid}")
    public ResponseEntity<Usuario> atualizar(
            @PathVariable("uuid") UUID id,
            @RequestBody @Valid UsuarioDTO dto) {
        return usuarioService.obterUsuarioPorId(id)
                .map(entity -> {
                    Usuario usuario = usuarioConverter.dtoToEntity(dto);
                    usuario.setUuid(id);
                    usuarioService.atualizarUsuario(usuario);
                    return ResponseEntity.ok(usuario);
                })
                .orElseThrow(() ->
                        new RegraDeNegocioException("O usuario informado não foi encontrado"));
    }

    @GetMapping("{uuid}/saldo")
    public ResponseEntity<?> obterSaldo(@PathVariable("uuid") UUID id) {
        Optional<Usuario> usuario = usuarioService.obterUsuarioPorId(id);

        if (!usuario.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);
        return ResponseEntity.ok(saldo);
    }
}
