package com.cleber.financas.api.converter;

import com.cleber.financas.api.dto.LancamentoDTO;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.entity.TipoLancamento;
import com.cleber.financas.api.dto.UsuarioAutenticacaoDTO;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConvertDtoToEntity {
    
    @Autowired
    private UsuarioService usuarioService;
    
    /* Um metodo para converter o dto em uma entidade de lancamento */
    public Lancamento converterDtoParaEntidade(LancamentoDTO dto) {
        Lancamento lancamento = new Lancamento();
        lancamento.setId(dto.getId()); /* caso precise atualizar, ele vem preenchido com o id */
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setAno(dto.getAno());
        lancamento.setMes(dto.getMes());
        lancamento.setDataCadastro(dto.getDataCadastro());
        lancamento.setValor(dto.getValor());
        lancamento.setDataCadastro(dto.getDataCadastro());
        /* inicio usuario */
        /* receber o id do usuario, conforme dto */
        Usuario receberUsuario = usuarioService.obterUsuarioPorId(dto.getUsuario())
                /*
                 * buscarLancamento do usuario por id, ou lancar uma exception caso ele nao
                 * exista
                 */
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Usuario não encontrado com o id " + "(" + dto.getUsuario() + ")"));
        lancamento.setUsuario(receberUsuario);
        /* fim usuario */
        if (dto.getTipo() != null) {
            lancamento.setTipoLancamento(TipoLancamento.valueOf(dto.getTipo()));
        }
        if (dto.getStatus() != null) {
            lancamento.setStatusLancamento(StatusLancamento.valueOf(dto.getStatus()));
        }
        return lancamento;
    }

    /* Um metodo para converter o dto em uma entidade de usuario */
    public Usuario converterDtoParaEntidade(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId()); /* caso precise atualizar, ele vem preenchido com o id */
        usuario.setNome(dto.getNome());
        usuario.setCpf(dto.getCpf());
        usuario.setUsuario(dto.getUsuario());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setDataCadastro(dto.getDataCadastro());
        return usuario;
    }
    
}
