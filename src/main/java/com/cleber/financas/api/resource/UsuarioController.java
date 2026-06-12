package com.cleber.financas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cleber.financas.api.converter.UsuarioConverter;
import com.cleber.financas.api.dto.TokenDTO;
import com.cleber.financas.api.dto.UsuarioAutenticacaoDTO;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.ErroValidacaoException;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;

@RestController
/**
 * para mapeamento de todas as requisições
 * http://localhost:8081/v1/auth/usuario
 * 
 * */
@RequestMapping("/api/join/sign-up/") //api/usuario
//@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioController {
    
    private final UsuarioService usuarioService;
    private final LancamentoService lancamentoService;
    private final UsuarioConverter usuarioConverter;
    private final JwtService jwtService;
    
    public UsuarioController(
            UsuarioService usuarioService,
            LancamentoService lancamentoService,
            UsuarioConverter usuarioConverter,
            JwtService jwtService
    ){
        this.usuarioService = usuarioService;
        this.lancamentoService = lancamentoService;
        this.usuarioConverter = usuarioConverter;
        this.jwtService = jwtService;
    }

    @PostMapping("/autenticar")
    public ResponseEntity<?> autenticar(@RequestBody UsuarioAutenticacaoDTO dto) {
        try {
            Usuario usuarioAutenticado = usuarioService.autenticar(dto.getEmail(), dto.getSenha());
            String token = jwtService.gerarToken(usuarioAutenticado);
            TokenDTO tokenDTO = TokenDTO.builder()
                    .nome(usuarioAutenticado.getNomeCompleto())
                    .email(usuarioAutenticado.getEmail())
                    .token(token)
                    .build();
            return ResponseEntity.ok(tokenDTO);
        } catch (ErroDeAutenticacao e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Salvar - Este metodo é um endpoint que recebe
     * uma requisição HTTP POST - vem la do front
     *
     * ResponseEntity representa o corpo da resposta
     **/
    @PostMapping
    public ResponseEntity<?> salvarUsuario( @RequestBody UsuarioDTO dto) { /**@Valid*/
        Usuario usuario = Usuario.builder()
                .nomeCompleto(dto.getNomeCompleto())
                .cpf(dto.getCpf())
                .nomeUsuario(dto.getNomeUsuario())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .build();

        try {
            Usuario usuarioSalvo = usuarioService.salvarUsuario(usuario);
            return new ResponseEntity<>(usuarioSalvo, HttpStatus.CREATED);
            /*ou usar url*/
            /*return ResponseEntity.created(URI.create("/api/usuarios/" + usuarioSalvo.getId())).build();*/
        } catch (ErroValidacaoException mensagemDeErro) {
            mensagemDeErro.printStackTrace();
            return ResponseEntity.badRequest().body(mensagemDeErro.getMessage());
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> atualizar(@PathVariable("id") UUID id, @RequestBody UsuarioDTO dto){
        return usuarioService.obterUsuarioPorId(id).map(entity ->{
            try{
                Usuario usuario = usuarioConverter.dtoToEntity(dto);
                usuario.setId(id);
                usuarioService.atualizarUsuario(usuario);
                return ResponseEntity.ok(usuario);

            }catch (RegraDeNegocioException r){
                return ResponseEntity.badRequest().body(r.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity<>(
                "O usuario informado não foi encontrado", HttpStatus.BAD_REQUEST));
    }
    
    @GetMapping("{id}/saldo")
    public ResponseEntity<?> obterSaldo(@PathVariable("id") UUID id) {
    	/*saldo por usuario*/
    	Optional<Usuario> usuario = usuarioService.obterUsuarioPorId(id);
    	
    	if(!usuario.isPresent()) {
    		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	}
    	BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);
    	return ResponseEntity.ok(saldo);
    }
    /*@GetMapping("/")public String helloWorld() {return "Fala dev";}*/
}
