package com.cresign.tools.mongo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

/**
 * @author JackSon
 * @ver 1.0
 * ##description: mongodb事务配置
 * @updated 2020-04-13 15:05
 */
@Configuration
public class MongodbTransaction {

    @Bean
    MongoTransactionManager transactionManager(MongoDbFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }


}