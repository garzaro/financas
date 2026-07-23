package com.cleber.financas.api.converter;


import org.springframework.stereotype.Component;

import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.model.entity.Usuario;

@Component
public class UsuarioConverter {

    /* Um metodo para converter o dto em uma entidade de usuario */
    public Usuario dtoToEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setUuid(dto.getId()); /* caso precise atualizar, ele vem preenchido com o id */
        usuario.setNomeCompleto(dto.getNomeCompleto());
        usuario.setCpf(dto.getCpf());
        usuario.setNomeUsuario(dto.getNomeUsuario());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setDataCadastro(dto.getDataCadastro());
        return usuario;
    }
}




