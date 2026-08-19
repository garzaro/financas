package com.cleber.financas.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtualizarStatusDTO {
	@NotNull(message = "{status.obrigatorio}")
    private String statusLancamento;	
}
