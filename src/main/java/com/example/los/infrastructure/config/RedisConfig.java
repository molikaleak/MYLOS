package com.example.los.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis configuration for JWT token blacklisting and caching.
 * Configures Redis connection and template for token management.
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * Creates Redis connection factory using Lettuce client.
     * Connection details are loaded from application.properties.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
        // Connection properties are automatically configured from
        // spring.data.redis.* properties in application.properties
        log.info("Redis connection factory configured");
        return connectionFactory;
    }

    /**
     * Creates a specialized RedisTemplate for token blacklisting operations.
     * Uses simple String serialization for token keys and expiration values.
     * This is sufficient for JWT token blacklisting where we only need to store
     * token signatures with expiration timestamps.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Simple String serialization for token blacklisting
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        template.afterPropertiesSet();
        log.info("RedisTemplate configured for token blacklisting");
        return template;
    }
}