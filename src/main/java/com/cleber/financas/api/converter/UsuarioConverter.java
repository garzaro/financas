package com.cleber.financas.api.converter;


import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.model.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioConverter {

    /* Um metodo para converter o dto em uma entidade de usuario */
    public Usuario dtoToEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId()); /* caso precise atualizar, ele vem preenchido com o id */
        usuario.setNome(dto.getNome());
        usuario.setCpf(dto.getCpf());
        usuario.setNomeUsuario(dto.getUsuario());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setDataCadastro(dto.getDataCadastro());
        return usuario;
    }
}




