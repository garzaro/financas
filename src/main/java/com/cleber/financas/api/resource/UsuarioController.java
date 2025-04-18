package com.cleber.financas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cleber.financas.api.dto.UsuarioAutenticacaoDTO;
import com.cleber.financas.api.dto.UsuarioCadastroDTO;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios") /*para mapeamento de todas as requisições*/
@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioController {
	@Autowired
    public UsuarioService usuarioService;
	@Autowired
	public LancamentoService lancamentoService;
    
    public UsuarioController(UsuarioService usuarioService, LancamentoService lancamentoService) {
        this.usuarioService = usuarioService;
        this.lancamentoService = lancamentoService;
    }

    @PostMapping("/autenticar")
    public ResponseEntity<?> autenticarUsuario(@RequestBody UsuarioAutenticacaoDTO dtoAuth) {
        try {
            Usuario usuarioAutenticado = usuarioService.autenticarUsuario(dtoAuth.getEmail(), dtoAuth.getSenha());
            return ResponseEntity.ok(usuarioAutenticado);
        } catch (ErroDeAutenticacao e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /*Salvar - Este metodo é um endpoint que recebe uma requisição HTTP POST*/
    /*ResponseEntity representa o corpo da resposta*/
    @PostMapping
    public ResponseEntity salvar(@RequestBody UsuarioCadastroDTO dto) {
        Usuario usuario = Usuario.builder()
                .nomeCompleto(dto.getNomeCompleto())
                .cadastroPessoaFisica(dto.getCadastroPessoaFisica())
                .nomeUsuario(dto.getNomeUsuario())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .build();

        try {
            Usuario usuarioSalvo = usuarioService.persistirUsuarioNabaseDeDados(usuario);
            return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
            /*ou usar url*/
            /*return ResponseEntity.created(URI.create("/api/usuarios/" + usuarioSalvo.getId())).build();*/
        } catch (RegraDeNegocioException mensagemDeErro) {
            return ResponseEntity.badRequest().body(mensagemDeErro.getMessage());
        }
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
