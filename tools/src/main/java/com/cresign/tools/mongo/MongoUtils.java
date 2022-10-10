package com.cresign.tools.mongo;

import org.bson.types.ObjectId;


/**
 * ##description: mongodb工具类
 * @author JackSon
 * @updated 2020/7/29 16:46
 * @ver 1.0
 */
public class MongoUtils {

    /**
     * 生成ObjectId
     * @param
     * @author JackSon
     * @updated 2020/7/29 16:51
     * @return java.lang.String
     */
    public static String GetObjectId() {
        return new ObjectId().toString();
    }

}