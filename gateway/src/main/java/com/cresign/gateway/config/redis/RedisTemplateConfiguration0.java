package com.cresign.gateway.config.redis;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Configuration
public class RedisTemplateConfiguration0 {

    private final RedisProperties0 redisProperties0;

    @Autowired
    public RedisTemplateConfiguration0(RedisProperties0 redisProperties0) {
        this.redisProperties0 = redisProperties0;
    }

    @Configuration
    public class RedisConfig {
        @Bean(name = "redisConnectionFactory0")
        public RedisConnectionFactory redisConnectionFactory0() {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(redisProperties0.getLettuce().getPool().getMax_active());
            config.setMaxIdle(redisProperties0.getLettuce().getPool().getMax_idle());
            config.setMinIdle(redisProperties0.getLettuce().getPool().getMin_idle());
            config.setMaxWaitMillis(redisProperties0.getLettuce().getPool().getMax_wait());
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
            configuration.setDatabase(redisProperties0.getDatabase());
            configuration.setHostName(redisProperties0.getHost());
            configuration.setPassword(redisProperties0.getPassword());
            ClientResources clientResources = DefaultClientResources.builder().build();
            configuration.setPort(redisProperties0.getPort());
            LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder()
                    .poolConfig(config).commandTimeout(Duration.ofSeconds(10)).shutdownTimeout(Duration.ofMillis(100)).clientResources(clientResources).build();
            return new LettuceConnectionFactory(configuration, lettuceClientConfiguration);
        }

        @Bean(name = "redisTemplate0")
        public StringRedisTemplate redisTemplate0() {
            StringRedisTemplate redisTemplateObject = new StringRedisTemplate();
            redisTemplateObject.setConnectionFactory(redisConnectionFactory0());
            return redisTemplateObject;
        }

    }
}