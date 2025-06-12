package com.cleber.financas.api.resource;

/*@RequiredArgsConstructor, substitui o construtor na injeção de deps, declara deps com final */
/*@PostMapping, mapear requisição Http, criar recurso no servidor*/
/*@RequestParams, java.util.Map<String, String> params substitui os parametros, metodo buscar */
/*return new ResponseEntity.ok(converteEntidade*/
/*return new ResponseEntity(lancamento, HttpStatus.CREATED)*/
/*Spring Boot não permite null em parâmetros obrigatórios de métodos REST, como em @RequestParam - value required*/
/*@PathVariable("id") Long id: Extrai o valor do parâmetro id da URL.*/
/*{id} é um parâmetro de caminho.*/

import com.cleber.financas.api.converter.ConvertDtoToEntity;
import com.cleber.financas.api.dto.AtualizarStatusDTO;
import com.cleber.financas.api.dto.LancamentoDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
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
    
    private final LancamentoService lancamentoService;
    private final UsuarioService usuarioService;
    private final ConvertDtoToEntity converter;
    
    public LancamentoResource(LancamentoService lancamentoService, UsuarioService usuarioService, ConvertDtoToEntity converter) {
        this.lancamentoService = lancamentoService;
        this.usuarioService = usuarioService;
        this.converter = converter;
    }
    
    @PostMapping
    public ResponseEntity salvarLancamento(@RequestBody LancamentoDTO dto) {
        try {
            Lancamento converteEntidade = converter.converterDtoParaEntidade(dto);
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
                Lancamento lancamento = converter.converterDtoParaEntidade(dto);
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
            StatusLancamento selecionarStatus = StatusLancamento.valueOf(dto.getStatus());
            
            if (selecionarStatus == null) {
                return ResponseEntity.badRequest().body("O status informado não existe " + "[" + dto + "]" + " informar um status válido");
            }
            try {
                entity.setStatusLancamento(selecionarStatus);
                lancamentoService.atualizarLancamento(entity);
                return ResponseEntity.ok(entity);
            } catch (RegraDeNegocioException status) {
                return ResponseEntity.badRequest().body(status.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Status não encontrado ", HttpStatus.BAD_REQUEST));
    }
    
    @GetMapping
    public ResponseEntity buscarLancamento(@RequestParam(value = "descricao", required = false) String descricao,
                                           @RequestParam(value = "tipoLancamento", required = false) String tipoLancamento,
                                           @RequestParam(value = "mes", required = false) Integer mes,
                                           @RequestParam(value = "ano") Integer ano,
            /* Parametro obrigatorio para fazer o filtro */
                                           @RequestParam(value = "usuario", required = false) Long idUsuario) {
        try {
            /* Verifica se o ID do usuário foi passado */
            
            if (idUsuario == null) {
                return ResponseEntity.badRequest().body("O ID do usuário é obrigatório, Jão " + "[" + idUsuario + "].");
            }
            /* filtrando */
            Lancamento lancamentoFiltro = new Lancamento();
            lancamentoFiltro.setDescricao(descricao);
            lancamentoFiltro.setMes(mes);
            lancamentoFiltro.setAno(ano);
            /* verifica se o usuario existe e define o filtro do lancamento */
            Optional<Usuario> usuario = usuarioService.obterUsuarioPorId(idUsuario);
            if (!usuario.isPresent()) {
                return ResponseEntity.badRequest()
                        .body("Consulta não realizada, usuario não encontrado com o ID " + "[" + idUsuario + "]");
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
            lancamentoService.deletarLancamento(entity); /* o map permite executar uma operação na entidade encontrada */
            return new ResponseEntity("lancamento excluido com sucesso.", HttpStatus.NO_CONTENT);/*OK*/ /* e devolve o resultado */
        }).orElseGet(() ->
                new ResponseEntity(
                        "Lançamento com ID " + "_" + id + "_" + " não encontrado na base de dados." +
                                " Verifique se o ID está correto e tente novamente.",
                        HttpStatus.BAD_REQUEST));
    }
}
