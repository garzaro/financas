package com.cleber.financas.api.resource;

import com.cleber.financas.api.dto.LancamentoDTO;
import com.cleber.financas.exception.ErroAcessoBancoDadosException;
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

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoResource {
    
    private LancamentoService lancamentoService;
    
    private UsuarioService usuarioService;
    
    public LancamentoResource(LancamentoService lancamentoService){
        this.lancamentoService = lancamentoService;
    }
    
    @PostMapping /*MAPEAR REQUISICAO HTTP, criar recurso no servidor*/
    public ResponseEntity salvarLancamento(@RequestBody LancamentoDTO dto){
        try {
            Lancamento converteEntidade = converterDtoParaEntidade(dto);
            converteEntidade = lancamentoService.salvarLancamento(converteEntidade);
            return new ResponseEntity(converteEntidade, HttpStatus.CREATED);
            
        }catch (RegraDeNegocioException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /*entity é o retorno do service quando é obtido por id*/
    @PutMapping("{id}") /*mapear requisicao HTTP*/
    public ResponseEntity atualizarLancamento(@PathVariable("id") Long id,  @RequestBody LancamentoDTO dto){
        /*entity é resultado da busca pelo id*/
        return lancamentoService.obterLancamentoPorId(id).map(entity ->{
            try {
                Lancamento lancamento = converterDtoParaEntidade(dto);
                lancamento.setId(entity.getId());
                lancamentoService.atualizarLancamento(lancamento);
                /*return new ResponseEntity(lancamento, HttpStatus.CREATED)*/
                return ResponseEntity.ok(lancamento);
            }catch (RegraDeNegocioException e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Lancamento não encontrado", HttpStatus.BAD_REQUEST));
    }
    
    /*substitui todos os parametros abaixo, campos da pequisa sao todos opcionais*/
	/*@RequestParam java.util.Map<String, String> params*/
    /*opcional na busca, required = false*/
    
    @GetMapping
    public ResponseEntity buscarLancamento(
    		@RequestParam(value = "descricao", required = false) String descricao,
    		@RequestParam(value= "mes", required = false) Integer mes,
    		@RequestParam(value= "ano", required = false) Integer ano,
            /*Parametro obrigatorio para fazer o filtro*/
    		@RequestParam("usuario") Long idusuario
    ){
        try{
            /*validar o id do usuario, evitar excessao antes de chegar no banco*/
            if (idusuario == null){
                return ResponseEntity.badRequest().body("O ID do usuário é obrigatório");
            }
            /*filtrando*/
            Lancamento lancamentoFiltro = new Lancamento();
            lancamentoFiltro.setDescricao(descricao);
            lancamentoFiltro.setMes(mes);
            lancamentoFiltro.setAno(ano);

            /*verifica se o usuario existe e define o filtro do lancamento*/
            Optional<Usuario> usuario = usuarioService.obterUsuarioPorId(idusuario);
            if (!usuario.isPresent()) {
                return ResponseEntity.badRequest()
                        .body("Consulta não realizada, usuario não encontrado com o ID: " + idusuario);
            }else {
                lancamentoFiltro.setUsuario(usuario.get());
            }
            /*busca os lancamentos com base no filtro*/
            List<Lancamento> lancamentos = lancamentoService.buscarLancamento(lancamentoFiltro);
            return ResponseEntity.ok(lancamentos);

        }catch (IllegalArgumentException argumento) {
            /*Tratamento de excessão para argumentos inválidos*/
            return ResponseEntity.badRequest().body(argumento.getMessage());

        } catch (DataAccessException bd) {
            /*Tratamento de erro ao acessar o banco de dados*/
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(bd.getMessage());

        } catch (Exception e) {
            /* Tratamento de outras exceções*/
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado: " + e.getMessage());
        }
    }
    
    @DeleteMapping("{id}")
    public ResponseEntity deletar(@PathVariable ("id") Long id){
        return lancamentoService.obterLancamentoPorId(id)/*se tiver o id, invoca o map*/.map(entity ->{
            lancamentoService.deletarLancamento(entity); /*o map permite executar uma operação na entidade encontrada*/
            return new ResponseEntity(HttpStatus.NO_CONTENT); /*e devolve o resultado*/
        }).orElseGet( ()->
                new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST) );
    }
    
    /*Um metodo para converter o dto em uma entidade de lancamento*/
    private Lancamento converterDtoParaEntidade(LancamentoDTO dto){
        Lancamento lancamento = new Lancamento();
        lancamento.setId(dto.getId()); /*caso precise atualizar, ele vem preenchido com o id */
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setAno(dto.getAno());
        lancamento.setMes(dto.getMes());
        lancamento.setValor(dto.getValor());
        /*inicio usuario*/
        /*receber o id do usuario, conforme dto*/
        Usuario buscarUsuario = usuarioService.obterUsuarioPorId(dto.getUsuario())
                /*buscarLancamento o usuario por id, ou lancar uma exception caso ele nao exista*/
        .orElseThrow(() -> new RegraDeNegocioException("Usuario não encontrado com o id informado"));
        lancamento.setUsuario(buscarUsuario);
        /*fim usuario*/
        lancamento.setTipoLancamento(TipoLancamento.valueOf(dto.getTipo()));
        lancamento.setStatusLancamento(StatusLancamento.valueOf(dto.getStatus()));
        return lancamento;
    }
}
