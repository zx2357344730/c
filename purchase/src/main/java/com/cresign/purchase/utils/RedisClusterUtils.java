//package com.cresign.purchase.utils;
//
//import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
//import redis.clients.jedis.Connection;
//import redis.clients.jedis.HostAndPort;
//import redis.clients.jedis.JedisCluster;
//
//import java.util.HashSet;
////集群配置
//public class RedisClusterUtils {
//
//    //集群密码
//    private static final String AUTH = "jackson";
//
//    private static final JedisCluster jedisCluster;
//
//    static {
//        /*
//         * 2.9.0及以后的jar包中，没有setMaxActive和setMaxWait属性了
//         * maxActive----maxTotal
//         * maxWait---maxWaitMillis
//         */
//        //池配置
//        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
//        //最大连接数, 默认8个，赋值为-1 则表示不受限制
//        //如果连接数为设置的值，此时状态为exhausted耗尽
//        poolConfig.setMaxTotal(1000);
//        //最大空闲连接数, 默认8个，控制空闲状态数量，最多为设置的值
//        poolConfig.setMaxIdle(50);
//        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted)
//        // 如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1永不超时
//        poolConfig.setMaxWaitMillis(1000);
//        //在获取连接的时候检查有效性, 默认false8
//        poolConfig.setTestWhileIdle(true);
//        //在用一个redis实例时，是否提前进行验证操作
//        //如果为TRUE，则得到的实例都是可用的
//        poolConfig.setTestOnBorrow(true);
//        //是否进行有效性检查
//        poolConfig.setTestOnReturn(true);
//
//        //节点信息
//        HashSet<HostAndPort> set = new HashSet<>();
//        set.add(new HostAndPort("42.194.194.96", 6868));//1-m
//        set.add(new HostAndPort("42.194.194.96", 6869));//1-s
//        set.add(new HostAndPort("42.194.194.96",6870));//l-m
//        set.add(new HostAndPort("42.194.194.96",6871));//l-s
//        set.add(new HostAndPort("42.193.193.122",6869));//d-m
//        set.add(new HostAndPort("42.193.193.122",6870));//d-s
//        /*
//         * 参数1 redis节点信息
//         * 参数2 连接超时
//         * 参数3 读写超时
//         * 参数4 重试次数
//         * 参数5 集群密码
//         * 参数6 连接池参数
//         */
//        jedisCluster = new JedisCluster(set,5000,5000,5,AUTH,poolConfig);
//        /*
//         * 重试次数，JedisCluster在连接的时候，如果出现连接错误，则会尝试随机连接一个节点，
//         * 如果当期尝试的节点返回Moved重定向，jedis cluster会重新更新clots缓存。
//         * 如果重试依然返回连接错误，会接着再次重试，
//         * 当重试次数大于maxAttempts会报出
//         * Jedis ClusterMaxRedirectionsException(“to many Cluster redireciotns?”)异常
//         */
//    }
//
//    /**
//     *
//     * @return 连接信息
//     */
//    public static JedisCluster getJRedis(){
//        return jedisCluster;
//    }
//}