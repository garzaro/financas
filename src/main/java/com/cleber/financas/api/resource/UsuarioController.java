package com.cleber.financas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;

import com.cleber.financas.api.converter.ConvertDtoToEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cleber.financas.api.dto.UsuarioAutenticacaoDTO;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios") /*para mapeamento de todas as requisições*/
//@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioController {
	@Autowired
    public UsuarioService usuarioService;
	@Autowired
	public LancamentoService lancamentoService;
    @Autowired
    public ConvertDtoToEntity convertDtoToEntity;
    
    public UsuarioController(UsuarioService usuarioService, LancamentoService lancamentoService) {
        this.usuarioService = usuarioService;
        this.lancamentoService = lancamentoService;
    }

    @PostMapping("/autenticar")
    public ResponseEntity<?> autenticar(@RequestBody UsuarioAutenticacaoDTO dto) {
        try {
            Usuario usuarioAutenticado = usuarioService.autenticar(dto.getEmail(), dto.getSenha());
            return ResponseEntity.ok(usuarioAutenticado);
        } catch (ErroDeAutenticacao e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /*Salvar - Este metodo é um endpoint que recebe uma requisição HTTP POST - vem la do front*/
    /*ResponseEntity representa o corpo da resposta*/
    @PostMapping
    public ResponseEntity salvarUsuario(@RequestBody UsuarioDTO dto) {
        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .usuario(dto.getUsuario())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .build();

        try {
            Usuario usuarioSalvo = usuarioService.salvarUsuario(usuario);
            return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
            /*ou usar url*/
            /*return ResponseEntity.created(URI.create("/api/usuarios/" + usuarioSalvo.getId())).build();*/
        } catch (RegraDeNegocioException mensagemDeErro) {
            return ResponseEntity.badRequest().body(mensagemDeErro.getMessage());
        }
    }

    @PutMapping("{id}")
    public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody UsuarioDTO dto){
        return usuarioService.obterUsuarioPorId(id).map(entity ->{
            try{
                Usuario usuario = convertDtoToEntity.converterDtoParaEntidade(dto);
                usuario.setId(id);
                usuarioService.atualizarUsuario(usuario);
                return ResponseEntity.ok(usuario);

            }catch (RegraDeNegocioException r){
                return ResponseEntity.badRequest().body(r.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity(
                "O usuario com o ID " + "["+ id +"] " + "não foi encontrado", HttpStatus.BAD_REQUEST));
    }
    
    @GetMapping("{id}/saldo")
    public ResponseEntity obterSaldo(@PathVariable("id") Long id) {
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
