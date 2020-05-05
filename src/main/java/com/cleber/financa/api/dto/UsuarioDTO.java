package com.cleber.financa.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder //faciltar a criação de um DTO. caso seja necessario
public class UsuarioDTO {
	
	private String nome;
	private String email;
	private String senha;

}
