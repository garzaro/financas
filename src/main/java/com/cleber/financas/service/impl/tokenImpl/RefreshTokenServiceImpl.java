package com.cleber.financas.service.impl.tokenImpl;

import com.cleber.financas.exception.InvalidRefreshTokenException;
import com.cleber.financas.api.dto.RefreshDadosToken;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.token.RefreshTokenEmitido;
import com.cleber.financas.service.token.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

/**
 * Store de refresh tokens em Redis. Chaves:
 *   refresh:token:{hash}    -> RefreshTokenData (TTL = tempo de vida do token)
 *   refresh:family:{fid}    -> Set<String hash>  (TTL renovado a cada rotação)
 *   refresh:user:{userId}   -> Set<String familyId> (sem TTL — vive enquanto houver sessão)
 *
 * O valor bruto do token NUNCA é armazenado — só o hash SHA-256.
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final String TOKEN_PREFIX = "refresh:token:";
    private static final String FAMILIA_PREFIX = "refresh:family:";
    private static final String USUARIO_PREFIX = "refresh:user:";

    private final RedisTemplate<String, RefreshDadosToken> tokenTemplate;
    private final StringRedisTemplate setTemplate;
    private final UsuarioRepository usuarioRepository;
    private final Duration tokenTtl;
    /**periodo de tolerancia**/
    private final Duration graceRotacionado;

    public RefreshTokenServiceImpl(
            RedisTemplate<String, RefreshDadosToken> refreshTokenRedisTemplate,
            StringRedisTemplate stringRedisTemplate,
            UsuarioRepository usuarioRepository,
            @Value("${spring.app.jwtRefreshExpirationMs}") long tokenTtlMs,
            @Value("${spring.app.refreshRotatedGraceMs}") long graceRotacionadoMs
    ) {
        this.tokenTemplate = refreshTokenRedisTemplate;
        this.setTemplate = stringRedisTemplate;
        this.usuarioRepository = usuarioRepository;
        this.tokenTtl = Duration.ofMillis(tokenTtlMs);
        this.graceRotacionado = Duration.ofMillis(graceRotacionadoMs);
    }

    @Override
    public RefreshTokenEmitido gerar(UUID usuarioId, String clientIp, String agenteUsuario) {
        UUID familiaId = UUID.randomUUID();
        return emissao(usuarioId, familiaId, clientIp, agenteUsuario);
    }

    @Override
    public RefreshTokenEmitido rotacionar(String tokenAntigo, String ip, String agenteUsuario) {
        String hash = hash(tokenAntigo);
        String chave = TOKEN_PREFIX + hash;
        RefreshDadosToken dadosToken = tokenTemplate.opsForValue().get(chave);
        if (dadosToken == null) {
            throw new InvalidRefreshTokenException("Refresh token não encontrado ou expirado");
        }
        if (dadosToken.status() == RefreshDadosToken.Status.ROTACIONADO){
            /** Reuse detectado: token já rotacionado sendo reapresentado -> sessão comprometida**/
            revogarFamiliaById(dadosToken.familiaId(), dadosToken.usuarioId());
            throw new InvalidRefreshTokenException("Reuse de refresh token detectado - familia inteira revogada");
        }
        /** Marca o token atual como ROTATED com TTL curto (grace period), não deleta:
         * precisa continuar existindo pra detectar reuse se for reapresentado.
         * **/
        tokenTemplate.opsForValue()
                .set(chave, dadosToken.comStatus(
                        RefreshDadosToken.Status.ROTACIONADO), graceRotacionado
                );
        /**criar um token**/
        return emissao(dadosToken.usuarioId(), dadosToken.familiaId(), ip, agenteUsuario);
    }

//    @Override
//    public RefreshDadosToken validar(String rawToken) {
//        String chave = TOKEN_PREFIX + hash(rawToken);
//        RefreshDadosToken dadosToken = tokenTemplate.opsForValue().get(chave);
//        if (dadosToken == null || dadosToken.status() != RefreshDadosToken.Status. ATIVO) {
//            throw new InvalidRefreshTokenException("Refresh token inválido");
//        }
//
//        return dadosToken;
//    }
    @Override
    public Usuario validar(String rawToken) {
        String chave = TOKEN_PREFIX + hash(rawToken); //token bruto
        RefreshDadosToken dados = tokenTemplate.opsForValue().get(chave);
        if (dados == null || dados.status() != RefreshDadosToken.Status.ATIVO) {
            throw new InvalidRefreshTokenException("Refresh token inválido");
        }
        return usuarioRepository.findById(dados.usuarioId())
                .orElseThrow(() -> new InvalidRefreshTokenException("Usuário do token não encontrado"));
    }

    @Override
    public void revogarFamilia(String rawToken) {
        String chave = TOKEN_PREFIX + hash(rawToken);
        RefreshDadosToken dadosToken = tokenTemplate.opsForValue().get(chave);
        if (dadosToken != null) {
            revogarFamiliaById(dadosToken.familiaId(), dadosToken.usuarioId());
        }
    }

    @Override
    public void revogarAllByUser(UUID usuarioId) {
        String chaveUsuario = USUARIO_PREFIX + usuarioId;
        Set<String> familias = setTemplate.opsForSet().members(chaveUsuario);
        if (familias != null){
            for (String familiaId : familias){
                revogarSomenteTokenDaFamilia(familiaId);
            }
        }
        setTemplate.delete(chaveUsuario);
    }

    /**metodos auxiliares internos**/

    private RefreshTokenEmitido emissao(UUID usuarioId, UUID familiaId, String ip, String agenteUsuario ){
        String rawToken = tokenAleatorio();
        String hash = hash(rawToken);

        RefreshDadosToken dadosToken = new RefreshDadosToken(
                usuarioId, familiaId, RefreshDadosToken.Status.ATIVO, ip, agenteUsuario, Instant.now());
        tokenTemplate.opsForValue().set(TOKEN_PREFIX + hash, dadosToken, tokenTtl);

        String chaveFamilia = FAMILIA_PREFIX + familiaId;
        setTemplate.opsForSet().add(chaveFamilia, hash);
        setTemplate.expire(chaveFamilia, tokenTtl);

        String chaveUsuario = USUARIO_PREFIX + usuarioId;
        setTemplate.opsForSet().add(chaveUsuario, familiaId.toString());

        return new RefreshTokenEmitido(rawToken, tokenTtl);
    }

    private void revogarFamiliaById(UUID familiaId, UUID usuarioId){
        revogarSomenteTokenDaFamilia(familiaId.toString());
        setTemplate.opsForSet().remove(USUARIO_PREFIX, usuarioId, familiaId.toString());

    }

    private void revogarSomenteTokenDaFamilia(String familiaId){
        String chaveFamilia = FAMILIA_PREFIX + familiaId;
        Set<String> hashes = setTemplate.opsForSet().members(chaveFamilia);
        if (hashes != null){
            for (String hash : hashes){
                tokenTemplate.delete(TOKEN_PREFIX + hash);
            }
        }
        setTemplate.delete(chaveFamilia);
    }

    private String tokenAleatorio() {
        /**256 bits de entropia via SecureRandom do próprio Spring Security — nada reinventado**/
        byte[] bytes = KeyGenerators.secureRandom(32).generateKey();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo de Resumo indisponível na JVM", ex);
        }
    }
}


/**
 * @RequiredArgsConstructor
 * ...
 * private final RedisTemplate<String, RefreshTokenData> tokenTemplate;
 *     private final StringRedisTemplate setTemplate;
 *
 *     @Value("${spring.app.jwtRefreshExpirationMs}")
 *     private Duration tokenTtl;
 *
 *     @Value("${spring.app.refreshRotatedGraceMs:30000ms}")
 *     private Duration rotatedGrace;
 * **/
