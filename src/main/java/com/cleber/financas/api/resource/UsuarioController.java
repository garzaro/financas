package com.cleber.financas.api.resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cleber.financas.api.dto.UsuarioAutenticacaoDTO;
import com.cleber.financas.api.dto.UsuarioCadastroDTO;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.UsuarioService;

@RestController
@RequestMapping(path = "/api/usuarios") /*para mapeamento de todas as requisições*/
public class UsuarioController {

    public UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/autenticar")
    public ResponseEntity autenticarUsuario(@RequestBody UsuarioAutenticacaoDTO dtoAuth) {
        try {
            Usuario usuarioAutenticado = usuarioService.autenticarUsuario(dtoAuth.getEmail(), dtoAuth.getSenha());
            return ResponseEntity.ok(usuarioAutenticado);
        } catch (ErroDeAutenticacao e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        }

    }

    /*Salvar - Este método é um endpoint que recebe uma requisição HTTP POST*/
    /*ResponseEntity representa o corpo da resposta*/
    @PostMapping
    public ResponseEntity salvar(@RequestBody UsuarioCadastroDTO dto) {
        Usuario usuario = Usuario.builder()
                .nomeCompleto(dto.getNomeCompleto())
                .cadastroPessoaFisica(dto.getCadastroPessoaFisica())
                .nomeUsuario(dto.getNomeUsuario())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .dataCadastro(dto.getDataCadastro())
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

    /*@GetMapping("/")public String helloWorld() {return "Fala dev";}*/
}
