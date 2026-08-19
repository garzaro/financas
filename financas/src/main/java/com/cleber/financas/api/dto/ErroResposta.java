package com.cleber.financas.api.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Record para estruturar mensagens de erro de integridade e validação
 * Captura erros de campos e fornece métodos auxiliares para construção
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErroResposta<CampoErro>(
		String mensagem,
		Integer status,		
		List<CampoErro> erros
		) {

	public static ErroResposta respostaPadrao(String mensagem) {
		return new ErroResposta(mensagem, HttpStatus.BAD_REQUEST.value(), List.of());
	}
	
	public static ErroResposta conflitoPadrao(String mensagem) {
		return new ErroResposta(mensagem, HttpStatus.CONFLICT.value(), List.of());
	}	
}
