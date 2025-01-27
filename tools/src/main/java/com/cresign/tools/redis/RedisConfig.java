//package com.cresign.tools.redis;
//
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.PropertyAccessor;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisClusterConfiguration;
//import org.springframework.data.redis.connection.RedisNode;
//import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//import redis.clients.jedis.JedisPoolConfig;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * @ClassName ClusterRedisConfig
// * @Description 作者很懒什么也没写
// * @Author tang
// * @Date 2022/9/5
// * @Version 1.0.0
// */
//@Configuration
//public class RedisConfig {
//
//    private static final String clusterNodes = "42.194.194.96:6868,42.194.194.96:6869,42.194.194.96:6870,42.194.194.96:6871,42.193.193.122:6869,42.193.193.122:6870";
//
//    private static final int maxRedirects = 6;
//
////    private static final int timeout = 10000;
//
//    private static final int maxIdle = 300;
//
//    private static final int maxTotal = 1000;
//
//    private static final int maxWaitMillis = 1000;
//
//    private static final int minEvictableIdleTimeMillis = 300000;
//
//    private static final int numTestsPerEvictionRun = 1024;
//
//    private static final int timeBetweenEvictionRunsMillis = 30000;
//
//    private static final boolean testOnBorrow = true;
//
//    private static final boolean testWhileIdle = true;
//
//    /**
//     * Redis连接池的配置
//     *
//     * @return JedisPoolConfig
//     */
//    @Bean
//    public JedisPoolConfig getJedisPoolConfig() {
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        // 最大空闲数
//        jedisPoolConfig.setMaxIdle(maxIdle);
//        // 连接池的最大数据库连接数
//        jedisPoolConfig.setMaxTotal(maxTotal);
//        // 最大建立连接等待时间
//        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
//        // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
//        jedisPoolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
//        // 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
//        jedisPoolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
//        // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
//        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
//        // 是否在从池中取出连接前进行检验,如果检验失败,则从池中去除连接并尝试取出另一个
//        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
//        // 在空闲时检查有效性, 默认false
//        jedisPoolConfig.setTestWhileIdle(testWhileIdle);
//        return jedisPoolConfig;
//    }
//
//
//    /**
//     * Redis集群的配置
//     * @return RedisClusterConfiguration
//     */
//    @Bean
//    public RedisClusterConfiguration redisClusterConfiguration() {
//        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
//        // Set<RedisNode> clusterNodes
//        String[] serverArray = clusterNodes.split(",");
//        Set<RedisNode> nodes = new HashSet<>();
//        for (String ipPort : serverArray) {
//            String[] ipAndPort = ipPort.split(":");
//            nodes.add(new RedisNode(ipAndPort[0].trim(), Integer.parseInt(ipAndPort[1])));
//        }
//        redisClusterConfiguration.setClusterNodes(nodes);
//        redisClusterConfiguration.setMaxRedirects(maxRedirects);
//        return redisClusterConfiguration;
//    }
//
//    /**
//     * redis连接工厂类
//     * @return JedisConnectionFactory
//     */
//    @Bean
//    public JedisConnectionFactory jedisConnectionFactory() {
//        // 集群模式
//        return new JedisConnectionFactory(redisClusterConfiguration(), getJedisPoolConfig());
//    }
//
//
//    /**
//     * 实例化 RedisTemplate 对象
//     *
//     * @return RedisTemplate<String, Object>
//     */
//    @Bean
//    public RedisTemplate<String,Object> redisTemplate() {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        // Template初始化
//        initDomainRedisTemplate(redisTemplate);
//        return redisTemplate;
//    }
//
//    /**
//     * 设置数据存入 redis 的序列化方式 使用默认的序列化会导致key乱码
//     */
//    private void initDomainRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
//        // 开启redis数据库事务的支持
//        redisTemplate.setEnableTransactionSupport(true);
//        redisTemplate.setConnectionFactory(jedisConnectionFactory());
//
//        // 如果不配置Serializer，那么存储的时候缺省使用String，如果用User类型存储，那么会提示错误User can't cast to
//        // String！
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//        redisTemplate.setKeySerializer(stringRedisSerializer);
//        // hash的key也采用String的序列化方式
//        redisTemplate.setHashKeySerializer(stringRedisSerializer);
//
//        // jackson序列化对象设置
//        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
//                Object.class);
//        ObjectMapper om = new ObjectMapper();
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(om);
//
//        // value序列化方式采用jackson
//        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
//        // hash的value序列化方式采用jackson
//        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
//
//        redisTemplate.afterPropertiesSet();
//    }
//
//}
