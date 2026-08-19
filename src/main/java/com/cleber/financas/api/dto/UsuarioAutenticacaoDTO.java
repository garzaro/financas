package com.cleber.financas.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAutenticacaoDTO {
	@NotBlank(message = "{usuario.email.obrigatorio}")
    @Email(message = "{usuario.email.invalido}")
    private String email;
	
	@NotBlank(message = "{usuario.senha.obrigatoria}")
    @Size(min = 6, message = "{usuario.senha.tamanho.minimo}")
    private String senha;
}
