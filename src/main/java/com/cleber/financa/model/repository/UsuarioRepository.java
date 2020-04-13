package com.cleber.financa.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cleber.financa.model.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	
	/*findby... chama se query methods,  abaixo, forma didatica.
	 * 
	 * Optional<Usuario> findByEmail(String email);
	 *  */
		
	boolean existsByEmail(String email);
	
	boolean existsByNome(String nome);
	
	
	

}
