package com.cleber.financas.api.controller;


import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    public UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public ResponseEntity salvar(@RequestBody UsuarioDTO usuarioDTO){
        Usuario usuario = Usuario.builder()
                .nomeCompleto(usuarioDTO.getNomeCompleto())
                .cadastroPessoaFisica(usuarioDTO.getCadastroPessoaFisica())
                .nomeUsuario(usuarioDTO.getNomeUsuario())
                .email(usuarioDTO.getEmail())
                .senha(usuarioDTO.getSenha())
                .build();

        try {
            Usuario usuarioSalvo = usuarioService.persistirUsuarioNabaseDeDados(usuario);
            return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
        }catch (RegraDeNegocioException mensagemDeErro){
            return ResponseEntity.badRequest().body(mensagemDeErro.getMessage());
        }

    }

}
