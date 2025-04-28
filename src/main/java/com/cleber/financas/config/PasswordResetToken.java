package com.cleber.financas.config;

import com.cleber.financas.model.entity.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;


@Data
@Entity
@Table(name = "alterar_senha_token", indexes = @Index(name = "idx_token", columnList = "token"))
public class PasswordResetToken {
	
    private static final long EXPIRATION_MINUTES = 60 * 24; /*dura 24 horas*/
	
    @Id
    private Long id;
    private String token;
    private Usuario usuario;
    private LocalDateTime dataExpira;
    private boolean tokenUsado = false;
    
    // Construtor para JPA
    protected PasswordResetToken() {}

    public PasswordResetToken(Usuario usuario) {
        this.usuario = Objects.requireNonNull(usuario, "Usuario não pode ser nulo");
        this.token = UUID.randomUUID().toString();
        this.dataExpira = calculateDataExpira();
    }

	private LocalDateTime calculateDataExpira() {
		return LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
	}
	public boolean isTokenExpirado() {
		return LocalDateTime.now().isAfter(dataExpira);		
	}
	public boolean isTokenValido() {
		return !isTokenExpirado() && !tokenUsado;
	}
	
	/*getters and setters*/
	public void marcarComoUsado() {
        this.tokenUsado = true;
    }
}
