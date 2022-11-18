package com.cresign.gateway.config.redis;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * ##description: redis切换库1
 * @author  JackSon
 * @updated  2020/8/5 9:04
 * @ver 1.0
 */
@Configuration
public class RedisTemplateConfiguration1 {

    private final RedisProperties1 redisProperties1;
    /**
     * 注入 redisProperties1
     * @param redisProperties1 redisProperties1配置类
     * @author JackSon
     * @ver 1.0
     * @updated 2020/8/5 9:04
     * @return RedisTemplateConfiguration1
     */
    @Autowired
    public RedisTemplateConfiguration1(RedisProperties1 redisProperties1) {
        this.redisProperties1 = redisProperties1;
    }

    /**
     * ##description: redis切换库1 Config
     * @author  JackSon
     * @updated  2020/8/5 9:04
     * @ver 1.0
     */
    @Configuration
    public class RedisConfig {
        /**
         * redisConnectionFactory1 工厂制造器，用来将 redisProperties1配置写入
         * @param
         * @author JackSon
         * @ver 1.0
         * @updated 2020/8/5 9:05
         * @return org.springframework.data.redis.connection.RedisConnectionFactory
         */
        @Bean(name = "redisConnectionFactory1")
        @Primary
        public RedisConnectionFactory redisConnectionFactory1() {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(redisProperties1.getLettuce().getPool().getMax_active());
            config.setMaxIdle(redisProperties1.getLettuce().getPool().getMax_idle());
            config.setMinIdle(redisProperties1.getLettuce().getPool().getMin_idle());
            config.setMaxWaitMillis(redisProperties1.getLettuce().getPool().getMax_wait());
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
            configuration.setDatabase(redisProperties1.getDatabase());
            configuration.setHostName(redisProperties1.getHost());
            configuration.setPassword(redisProperties1.getPassword());
            ClientResources clientResources = DefaultClientResources.builder().build();
            configuration.setPort(redisProperties1.getPort());
            LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder()
                    .poolConfig(config).commandTimeout(Duration.ofSeconds(10)).shutdownTimeout(Duration.ofMillis(100)).clientResources(clientResources).build();
            return new LettuceConnectionFactory(configuration, lettuceClientConfiguration);
        }
        /**
         * 重新注入 redisTemplate1
         * @param
         * @author JackSon
         * @ver 1.0
         * @updated 2020/8/5 9:06
         * @return org.springframework.data.redis.core.StringRedisTemplate
         */
        @Bean(name = "redisTemplate1")
        public StringRedisTemplate redisTemplate1() {
            StringRedisTemplate redisTemplateObject = new StringRedisTemplate();
            redisTemplateObject.setConnectionFactory(redisConnectionFactory1());
            return redisTemplateObject;
        }

    }
}