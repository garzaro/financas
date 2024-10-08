package com.cleber.financas.api.resource;

/*@RequiredArgsConstructor, substitui o construtor na injeção de deps, declara deps com final */
/*@PostMapping, mapear requisição Http, criar recurso no servidor*/
/*@RequestParams, java.util.Map<String, String> params substitui os parametros, metodo buscar */
/*return new ResponseEntity.ok(converteEntidade*/
/*return new ResponseEntity(lancamento, HttpStatus.CREATED)*/
/*Spring Boot não permite null em parâmetros obrigatórios de métodos REST, como em @RequestParam - value required*/
/*@PathVariable("id") Long id: Extrai o valor do parâmetro id da URL.*/
/*{id} é um parâmetro de caminho.*/

import com.cleber.financas.api.dto.AtualizarStatusDTO;
import com.cleber.financas.api.dto.LancamentoDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.entity.TipoLancamento;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoResource {

    private LancamentoService lancamentoService;
    private UsuarioService usuarioService;

    public LancamentoResource(LancamentoService lancamentoService, UsuarioService usuarioService) {
        this.lancamentoService = lancamentoService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity salvarLancamento(@RequestBody LancamentoDTO dto) {
        try {
            Lancamento converteEntidade = converterDtoParaEntidade(dto);
            converteEntidade = lancamentoService.salvarLancamento(converteEntidade);
            return new ResponseEntity(converteEntidade, HttpStatus.CREATED);

        } catch (RegraDeNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* entity é o retorno do service quando é obtido por id */
    @PutMapping("{id}")
    public ResponseEntity atualizarLancamento(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
        /* entity é resultado da busca pelo id */
        return lancamentoService.obterLancamentoPorId(id).map(entity -> {
            try {
                Lancamento lancamento = converterDtoParaEntidade(dto);
                lancamento.setId(id);
                lancamentoService.atualizarLancamento(lancamento);
                return ResponseEntity.ok(lancamento);

            } catch (RegraDeNegocioException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("O lancamento com o ID " + "(" + id + ")" + " não foi encontrado",
                HttpStatus.BAD_REQUEST));
    }

    @PutMapping("{id}/atualizar-status")
    public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizarStatusDTO dto) {
        return lancamentoService.obterLancamentoPorId(id).map(entity -> {
            try {
                /*Chama o método de serviço para atualizar o status*/
                lancamentoService.atualizarStatus(entity, dto.getStatus());
                return ResponseEntity.ok(entity);
            } catch (RegraDeNegocioException status) {
                /*Retorna a mensagem de erro caso uma exceção seja lançada*/
                return ResponseEntity.badRequest().body(status.getMessage());
            }
        }).orElseGet(() ->
                new ResponseEntity(
                        "Lançamento " + "[" + id + "]" + "não foi encontrado na base de dados",
                        HttpStatus.BAD_REQUEST));
    }

    @GetMapping
    public ResponseEntity buscarLancamento(@RequestParam(value = "descricao", required = false) String descricao,
                                           @RequestParam(value = "tipoLancamento", required = false) String tipoLancamento,
                                           @RequestParam(value = "mes", required = false) Integer mes,
                                           @RequestParam(value = "ano", required = false) Integer ano,
            /* Parametro obrigatorio para fazer o filtro */
                                           @RequestParam(value = "usuario", required = false) Long idusuario) {
        try {
            /* Verifica se o ID do usuário foi passado */

            if (idusuario == null) {
                return ResponseEntity.badRequest().body("O ID do usuário é obrigatório, Jão " + "[" + idusuario + "].");
            }
            /* filtrando */
            Lancamento lancamentoFiltro = new Lancamento();
            lancamentoFiltro.setDescricao(descricao);
            lancamentoFiltro.setMes(mes);
            lancamentoFiltro.setAno(ano);
            /* verifica se o usuario existe e define o filtro do lancamento */
            Optional<Usuario> usuario = usuarioService.obterUsuarioPorId(idusuario);
            if (!usuario.isPresent()) {
                return ResponseEntity.badRequest()
                        .body("Consulta não realizada, usuario não encontrado com o ID " + "[" + idusuario + "]");
            } else {
                lancamentoFiltro.setUsuario(usuario.get());
            }
            /* busca os lancamentos com base no filtro */
            List<Lancamento> lancamentos = lancamentoService.buscarLancamento(lancamentoFiltro);
            return ResponseEntity.ok(lancamentos);

        } catch (DataAccessException bd) {
            /* Tratamento de erro ao acessar o banco de dados */
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(bd.getMessage());
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity deletar(@PathVariable("id") Long id) {
        return lancamentoService.obterLancamentoPorId(id)/* se tiver o id, invoca o map */.map(entity -> {
            lancamentoService
                    .deletarLancamento(entity); /* o map permite executar uma operação na entidade encontrada */
            return new ResponseEntity(HttpStatus.NO_CONTENT); /* e devolve o resultado */
        }).orElseGet(() ->
        new ResponseEntity("Lançamento " + "[" + id + "]" + " não encontrado na base de dados.",
        		HttpStatus.BAD_REQUEST));
    }

    /* Um metodo para converter o dto em uma entidade de lancamento */
    private Lancamento converterDtoParaEntidade(LancamentoDTO dto) {
        Lancamento lancamento = new Lancamento();
        lancamento.setId(dto.getId()); /* caso precise atualizar, ele vem preenchido com o id */
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setAno(dto.getAno());
        lancamento.setMes(dto.getMes());
        lancamento.setDataCadastro(dto.getData());
        lancamento.setValor(dto.getValor());
        /* lancamento.setDataCadastro(dto.getDataCadastro()); */
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
}
