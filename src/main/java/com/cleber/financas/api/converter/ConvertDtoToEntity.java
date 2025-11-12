package com.cleber.financas.api.converter;

import com.cleber.financas.api.dto.LancamentoDTO;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.entity.TipoLancamento;
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
        lancamento.setValor(dto.getValor());
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
        if (dto.getTipoLancamento() != null) {
            lancamento.setTipoLancamento(TipoLancamento.valueOf(dto.getTipoLancamento()));
        }
        if (dto.getStatusLancamento() != null) {
            lancamento.setStatusLancamento(StatusLancamento.valueOf(dto.getStatusLancamento()));
        }
        return lancamento;
    }

    /* Um metodo para converter uma entidade de lancamento para dto */
    public LancamentoDTO converterEntidadeParaDto(Lancamento lancamento) {
        return LancamentoDTO.builder()
                .id(lancamento.getId())
                .descricao(lancamento.getDescricao())
                .valor(lancamento.getValor())
                .mes(lancamento.getMes())
                .ano(lancamento.getAno())
                .statusLancamento(lancamento.getStatusLancamento().name()) //tranforma em string - name() - ENUM é string *
                .tipoLancamento(String.valueOf(lancamento.getTipoLancamento())) // ou fazerum wrap com valueOf*
                .usuario(lancamento.getUsuario().getId())
                .build();
    }

    /* Um metodo para converter o dto em uma entidade de usuario */
    public Usuario converterDtoParaEntidade(UsuarioDTO dto) {
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
