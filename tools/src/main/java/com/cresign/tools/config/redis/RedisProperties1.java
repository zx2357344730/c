//package com.cresign.tools.config.redis;
//
//import lombok.Data;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//
///**
// * ##description: redis切换库1
// * @author  JackSon
// * @updated  2020/8/5 9:04
// * @ver 1.0
// */
//@Component
//@Data
//@ConfigurationProperties(prefix = "spring.redis1")
//public class RedisProperties1 {
//    private String host;
//    private int database;
//    private int port;
//    private String password;
//    private int timeout;
//    private Lettuce lettuce;
//    private Cache cache;
//
//
//    @Data
//    public static class Lettuce {
//        private Pool pool;
//    }
//
//    @Data
//    public static class Pool {
//        private int max_active;
//        private int max_wait;
//        private int max_idle;
//        private int min_idle;
//
//    }
//
//    @Data
//    public static class Cache{
//        private int livetime;
//    }
//
//}