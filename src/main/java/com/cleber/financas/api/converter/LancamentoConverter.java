package com.cleber.financas.api.converter;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cleber.financas.api.dto.LancamentoDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.enums.StatusLancamento;
import com.cleber.financas.model.enums.TipoLancamento;
import com.cleber.financas.service.UsuarioService;

@Component
public class LancamentoConverter {

    @Autowired
    private UsuarioService usuarioService;

    /* converter o dto em uma entidade de lancamento */
    /* o dono do lancamento vem do access token (usuarioAutenticado), nao do corpo da requisicao */
    public Lancamento dtoToEntity(LancamentoDTO dto, UUID usuarioAutenticado) {
        Lancamento lancamento = new Lancamento();
        lancamento.setUuid(dto.getId()); /* caso precise atualizar, ele vem preenchido com o id */
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setMes(dto.getMes());
        lancamento.setAno(dto.getAno());
        lancamento.setValor(dto.getValor());
        /* inicio usuario */
        /* o id do usuario e resolvido a partir do token autenticado, ignorando o valor do dto */
        Usuario receberUsuario = usuarioService.obterUsuarioPorId(usuarioAutenticado)
                /** buscarLancamento do usuario por id, ou lancar uma exception caso ele nao exista*/
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Usuário autenticado não encontrado!")); //Usuario nao encontrado
        lancamento.setUsuario(receberUsuario);
        /* fim usuario */
        if (dto.getTipoLancamento() != null) {
            lancamento.setTipoLancamento(TipoLancamento.valueOf(dto.getTipoLancamento()));
        }
        if (dto.getStatusLancamento() != null) {
            lancamento.setStatusLancamento(StatusLancamento.valueOf(dto.getStatusLancamento().toUpperCase()));
        }
        return lancamento;
    }

    /* converter uma entidade de lancamento para dto - PARA ATUALIZAÇÃO */
    public LancamentoDTO entityToDto(Lancamento lancamento) {
        return LancamentoDTO.builder()
                .id(lancamento.getUuid())
                .descricao(lancamento.getDescricao())
                .valor(lancamento.getValor())
                .mes(lancamento.getMes())
                .ano(lancamento.getAno())
                .statusLancamento((lancamento.getStatusLancamento().name())) //tranforma em string - name() - ENUM é string *
                .tipoLancamento(String.valueOf(lancamento.getTipoLancamento())) // ou fazerum wrap com valueOf*
//                .usuario(lancamento.getUsuario().getUuid())
                .build();
    }
}
