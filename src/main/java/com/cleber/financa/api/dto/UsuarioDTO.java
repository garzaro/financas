package com.cleber.financa.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder //faciltar a criação de um DTO. caso seja necessario
public class UsuarioDTO {
	
	private String nome;
	private String email;
	private String senha;
	
	
//	Para visualização do @Allargs... e @Noargs...
//	public UsuarioDTO() {
//		
//	}
//	public UsuarioDTO(String nome, String email, String senha) {
//		super();
//		this.nome = nome;
//		this.email = email;
//		this.senha = senha;
//	}
	
	

}
