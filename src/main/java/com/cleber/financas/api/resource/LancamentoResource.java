package com.cleber.financas.api.resource;

import java.util.List;
import java.util.Optional;

//import java.util.List;
//import java.util.Optional;
//
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cleber.financas.api.converter.LancamentoConverter;
import com.cleber.financas.api.converter.UsuarioConverter;
import com.cleber.financas.api.dto.AtualizarStatusDTO;
import com.cleber.financas.api.dto.LancamentoDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.enums.StatusLancamento;
import com.cleber.financas.model.enums.TipoLancamento;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lancamento")
public class LancamentoResource {
    
    private final LancamentoService lancamentoService;
    private final UsuarioService usuarioService;
    private final UsuarioConverter usuarioConverter;
    private final LancamentoConverter lancamentoConverter;
    
    public LancamentoResource(
            LancamentoService lancamentoService,
            UsuarioService usuarioService,
            UsuarioConverter usuarioConverter,
            LancamentoConverter lancamentoConverter
    ){
        this.lancamentoService = lancamentoService;
        this.usuarioService = usuarioService;
        this.usuarioConverter = usuarioConverter;
        this.lancamentoConverter = lancamentoConverter;
    }
    
    final Logger logger = LoggerFactory.getLogger(LancamentoResource.class);

    @PostMapping
    public ResponseEntity salvarLancamento(@RequestBody LancamentoDTO dto) {
        try {
            Lancamento converteEntidade = lancamentoConverter.dtoToEntity(dto);
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
                Lancamento lancamento = lancamentoConverter.dtoToEntity(dto);
                lancamento.setId(entity.getId()); //id
                lancamentoService.atualizarLancamento(lancamento);
                return ResponseEntity.ok(lancamento);
            } catch (RegraDeNegocioException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet(() ->
//                new ResponseEntity("O lancamento com o ID " + "(" + id + ")" + " não foi encontrado",
                new ResponseEntity("O lancamento informado não foi encontrado",
                HttpStatus.BAD_REQUEST));
    }

    @GetMapping("{id}")
    public ResponseEntity obterLancamentoPorId(@PathVariable("id") Long id){
        return lancamentoService.obterLancamentoPorId(id)
                .map( lancamento -> new ResponseEntity(lancamentoConverter.entityToDto( lancamento), HttpStatus.OK) )
                .orElseGet( () -> new ResponseEntity(HttpStatus.NOT_FOUND) );
    }

    @PutMapping("{id}/atualizar-statusLancamento")
    public ResponseEntity<?> atualizarStatus(@PathVariable("id") Long id, @Valid @RequestBody AtualizarStatusDTO dto) {        
    	return lancamentoService.obterLancamentoPorId(id).map(entity -> {
    		
            logger.info("Se imprimir null, o problema é o mapeamento do JSON (ponto 1 e 2). " + dto.getStatusLancamento());
            StatusLancamento selecionarStatus = StatusLancamento.valueOf(dto.getStatusLancamento());
            try {
                entity.setStatusLancamento(selecionarStatus);
                lancamentoService.atualizarLancamento(entity);
                return ResponseEntity.ok(entity);
            } catch (RegraDeNegocioException exception) {
                return ResponseEntity.badRequest().body(exception.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Lancamento não encontrado! ", HttpStatus.BAD_REQUEST));
    }

    @GetMapping
    public ResponseEntity buscarLancamento(
                                           @RequestParam(value = "tipoLancamento") TipoLancamento tipoLancamento,
                                           @RequestParam(value = "mes", required = false) Integer mes,
                                           @RequestParam(value = "ano") Integer ano,
            /* Parametro obrigatorio para fazer o filtro */
                                           @RequestParam("usuario") Long idUsuario
    ) {
        try {
            /* Verifica se o ID do usuário foi passado */
            
            if (idUsuario == null) {
                return ResponseEntity.badRequest().body("O ID do usuário é obrigatório" + "[" + idUsuario + "].");
            }
            /* filtrando */
            Lancamento lancamentoFiltro = new Lancamento();
            lancamentoFiltro.setTipoLancamento(tipoLancamento);
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
                        "Lançamento informado não foi encontrado. Verifique se a informação está correta e tente novamente.",
                        HttpStatus.BAD_REQUEST));
    }
}

/**
 /* @RequiredArgsConstructor, substitui o construtor na injeção de deps, declara deps com final
 * @PostMapping, mapear requisição Http, criar recurso no servidor
 * @RequestParams, java.util.Map<String, String> params substitui os parametros, metodo buscar
 * return new ResponseEntity.ok(converteEntidade
 * return new ResponseEntity(lancamento, HttpStatus.CREATED)
 * Spring Boot não permite null em parâmetros obrigatórios de métodos REST, como em @RequestParam - value required
 * @PathVariable("id") Long id: Extrai o valor do parâmetro id da URL.
 * {id} é um parâmetro de caminho.
 * **/
