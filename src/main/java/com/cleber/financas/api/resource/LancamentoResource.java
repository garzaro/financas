package com.cleber.financas.api.resource;

import com.cleber.financas.api.dto.LancamentoDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.entity.TipoLancamento;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoResource {
    
    private LancamentoService lancamentoService;
    
    private UsuarioService usuarioService;
    
    public LancamentoResource(LancamentoService lancamentoService){
        this.lancamentoService = lancamentoService;
    }
    
    @PostMapping
    public ResponseEntity salvarLancamento(@RequestBody LancamentoDTO dto){
        try {
            Lancamento converterEntidade = converterDtoParaEntidade(dto);
            lancamentoService.salvarLancamento(converterEntidade);
            return new ResponseEntity(converterEntidade, HttpStatus.CREATED);
            
        }catch (RegraDeNegocioException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /*entity é o retorno do service quando é obtido por id*/
    
    @PutMapping("{id}")
    public ResponseEntity atualizarLancamento(@PathVariable("id") Long id,  @RequestBody LancamentoDTO dto){
        /*entity é resultado da busca pelo id*/
        return lancamentoService.obterLancamentoPorId(id).map(entity ->{
            Lancamento lancamento = converterDtoParaEntidade(dto);
            lancamento.setId(entity.getId());
            lancamentoService.atualizarLancamento(lancamento);
            return ResponseEntity.ok(lancamento);
        }).orElseGet(() -> new ResponseEntity("Lancamento não encontrado", HttpStatus.BAD_REQUEST));
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
        .orElseThrow(() -> new RegraDeNegocioException("Usuario não encontrado com o id informado"));
        lancamento.setUsuario(buscarUsuario);
        /*fim usuario*/
        lancamento.setTipoLancamento(TipoLancamento.valueOf(dto.getTipo()));
        lancamento.setStatusLancamento(StatusLancamento.valueOf(dto.getStatus()));
        return lancamento;
    }
}
