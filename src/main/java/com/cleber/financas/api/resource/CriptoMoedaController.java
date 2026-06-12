package com.cleber.financas.api.resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.service.CriptoMoedaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/criptomoeda")
public class CriptoMoedaController {
	
	private final CriptoMoedaService criptoMoedaService;
	
	@PostMapping
	public ResponseEntity<Object> salvarCriptomoeda(@RequestBody @Valid CriptoMoedaDTO dto){
		try {
			CriptoMoedaDTO salvarCriptomoeda = criptoMoedaService.salvarCriptomoeda(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(salvarCriptomoeda);
			
		} catch (RegraDeNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());		
		}
	}	
}
