package com.cleber.financas.model.repository;

import com.cleber.financas.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /*existe um usuario com um email*/
    boolean existsByEmail(String email);
    /*existe um usuario por cpf*/
    boolean existsByCpf(String cpf);
    /*busca um usuario por email*/
    Optional<Usuario> findByEmail(String email);
    /*busca um usuario por cpf*/
    Optional<Usuario> findByCpf(String cpf);
}
