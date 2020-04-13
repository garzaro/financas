package com.cleber.financa.model.repository;



import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import com.cleber.financa.model.entity.Lancamento;


@SpringBootTest
@ExtendWith(SpringExtension.class)
public class LancamentoRepositoryTest {
	
	@Autowired
	LancamentoRepository lancamentoRepository;
	
	@Test
	public void verificarMesDeLancamento(){
		//cenario, onde se cria
	Lancamento lance = Lancamento.builder().ano(2000).mes(4).build();
	lancamentoRepository.save(lance);	
		
	//ação / execucao, onde se chama os metodos
	boolean result = lancamentoRepository.existsByMes(4);
	boolean results = lancamentoRepository.existsByAno(2020);
	
	//verificação, onde se verifica, parte que mostra o comportamento	
	Assertions.assertThat(result).isTrue();
	Assertions.assertThat(results).isFalse();
	
	}
	

}
