package com.cleber.financas.security.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LimiteTentativasService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateLimitScript;

    public LimiteTentativasService(
            StringRedisTemplate redisTemplate,
            RedisScript<Long> rateLimitScript
    ) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = rateLimitScript;
    }

    public boolean isBloqueado(String chave) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(chave + ":block"));
    }

    public long obterRetryAfter(String chave) {
        Long ttl = redisTemplate.getExpire(chave + ":block", TimeUnit.SECONDS);
        if (ttl == null || ttl <= 0) {
            return 1L;
        }
        return ttl;
    }

    public void registrar(String chave, long max, Duration janela, Duration duracaoBloqueio) {
        List<String> keys = List.of(chave + ":count", chave + ":block");
        redisTemplate.execute(
                rateLimitScript,
                keys,
                String.valueOf(max),
                String.valueOf(janela.toSeconds()),
                String.valueOf(duracaoBloqueio.toSeconds())
        );
    }

    public void limpar(String chave) {
        redisTemplate.delete(List.of(chave + ":count", chave + ":block"));
    }
}
