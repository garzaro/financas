package com.cleber.financas.config;

import com.cleber.financas.api.dto.RefreshDadosToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    /**
     * Template dedicado a refresh:token:{hash} -> RefreshDadosToken. Tipado
     * diretamente no serializer — sem activateDefaultTyping, sem "@class" no
     * payload, porque esse template SÓ guarda um tipo.
     *
     * Os Sets (refresh:familia:*, refresh:user:*) guardam apenas Strings
     * (hash, familiaId) — para eles use o StringRedisTemplate autoconfigurado
     * pelo Spring Boot (nao preciso declarar o bean, ja existe). NAO reuse este
     * template pros Sets: o valueSerializer aqui é específico pra
     * RefreshDadosToken e quebraria ao tentar serializar uma String.
     */
    @Bean
    public RedisTemplate<String, RefreshDadosToken> refreshTokenRedisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, RefreshDadosToken> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        /**configuração - altamente configuravel**/
        StringRedisSerializer chaveSerializr = new StringRedisSerializer(); //para chaves
        Jackson2JsonRedisSerializer<RefreshDadosToken> valorSerializr =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper(), RefreshDadosToken.class); //para valores

        template.setKeySerializer(chaveSerializr);
        template.setValueSerializer(valorSerializr);

        template.setHashKeySerializer(chaveSerializr);
        template.setHashValueSerializer(valorSerializr);

        template.afterPropertiesSet();
        return template;
    }

    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        return mapper;
    }
}
