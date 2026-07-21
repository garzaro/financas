package com.cleber.financas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cleber.financas.api.converter.UsuarioConverter;
import com.cleber.financas.api.dto.AuthResponse;
import com.cleber.financas.api.dto.LoginRequest;
import com.cleber.financas.api.dto.RegisterRequest;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Controlador responsável pelos endpoints de autenticação e gestão de usuários.
 *
 * <p>Mapeamento base: {@code /auth}
 * <ul>
 *   <li>POST /auth/register — registro de novo usuário (criação de conta; sem token)</li>
 *   <li>POST /auth/login    — autenticação e emissão de JWT</li>
 * </ul>
 *
 * <p>Tratamento de erros é delegado ao {@link com.cleber.financas.api.resource.common.GlobalExceptionHandler}:
 * {@code BadCredentialsException} → 401, {@code EmailJaCadastradoException} → 409,
 * {@code MethodArgumentNotValidException} → 400.
 */
@RestController
/**
 * para mapeamento de todas as requisições
 * http://localhost:8081/v1/auth/usuario
 * 
 * */
@RequestMapping("/api/auth") //api/usuario
//@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UsuarioService usuarioService;
    private final LancamentoService lancamentoService;
    private final UsuarioConverter usuarioConverter;    

    public UsuarioController(
    		AuthenticationManager authenticationManager, 
    		JwtService jwtService,
			UserDetailsService userDetailsService, 
			UsuarioService usuarioService, 
			LancamentoService lancamentoService,
			UsuarioConverter usuarioConverter) {
		super();
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.usuarioService = usuarioService;
		this.lancamentoService = lancamentoService;
		this.usuarioConverter = usuarioConverter;
	}

	// -------------------------------------------------------------------------
    // POST /auth/register
    // -------------------------------------------------------------------------

    /**
     * Registra um novo usuário.
     *
     * Apenas cria a conta; o cliente deve chamar {@code /auth/login}
     * separadamente para obter o token. Isso preserva espaço para
     * confirmação de e-mail no futuro sem redesenho do endpoint.
     *
     * Regras:
     * 
     *   Valida os campos via {@code @Valid} (400 automático se inválido)
     *   Verifica duplicidade de email → 409 via handler global
     *   Encodifica a senha antes de persistir
     * 
     *
     * @return 201 Created (sem corpo)
     */
//    @PostMapping("/register")
//    @ResponseStatus(HttpStatus.CREATED)
//    public void register(@RequestBody @Valid  UsuarioDTO dto) {
//        usuarioService.salvarUsuario(request);
//        log.info("Conta criada com sucesso — email: {}", request.email());
//    }

    // -------------------------------------------------------------------------
    // POST /auth/login
    // -------------------------------------------------------------------------

    /**
     * Autentica o usuário e retorna um JWT.
     *
     * Caso as credenciais sejam inválidas, o {@code AuthenticationManager}
     * lança {@code BadCredentialsException}, que é capturada pelo handler global
     * e respondida com 401 — nunca revelando se foi o email ou a senha que errou.
     *
     * @return 200 OK com {@link AuthResponse} contendo o token e o tipo "Bearer"
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        // Delega a validação ao AuthenticationManager; falha lança BadCredentialsException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        // Autenticação bem-sucedida: carrega detalhes e gera token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.gerarToken(userDetails);

        log.info("Login bem-sucedido — email: {}", request.email());

        return ResponseEntity.ok(new AuthResponse(token));
    }

    // -------------------------------------------------------------------------
    // Endpoints de gestão de usuário (mantidos sem alteração de comportamento)
    // -------------------------------------------------------------------------

    /**
     * Cadastro completo de usuário (fluxo legado com CPF, nome de usuário etc.).
     * Mapeado em {@code /auth/join/sign-up/} para retrocompatibilidade.
     */
    @PostMapping("/join/sign-up/")
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
                    usuario.setId(id);
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
