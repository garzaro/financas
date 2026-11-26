package com.cleber.financas.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cleber.financas.model.enums.TokenStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Um refresh token emitido para um usuário. Apenas o hash SHA-256 do token é armazenado;
 * portanto, um vazamento no banco de dados não expõe tokens que possam ser utilizados.
 * Os tokens são de uso único: a operação de renovação revoga o token apresentado e emite um novo.
 * **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "refresh_token",
        schema = "financeiro",
        indexes = {@Index(
                name = "idx_refresh_token_usuario", columnList = "id_usuario")}
        )
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "criado_em", nullable = false)
    private Instant createdAt;

    @Column(name = "expira_em", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "novoStatus", nullable = false, length = 10)
    private TokenStatus status;

    @Column(name = "ip", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;
}
