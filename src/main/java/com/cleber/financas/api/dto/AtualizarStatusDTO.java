package com.cleber.financas.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtualizarStatusDTO {
	@NotNull(message = "Status do lançamento não pode ser nulo manoooooooooo")
    private String statusLancamento;	
}
