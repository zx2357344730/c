package com.cresign.tools.mongo;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


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

//    /**
//     * 返回rolex中的一个公司数据
//     * @author Jevon
//     * @param id_U          用户id
//     * @param id_C          公司id
//     * @param mongoTemplate
//     * @ver 1.0
//     * @createDate: 2021/6/28 16:41
//     * @return: com.alibaba.fastjson.JSONObject
//     */
//    public static JSONObject getRolex(String id_U, String id_C, MongoTemplate mongoTemplate){
//        Query userQ = new Query(
//                new Criteria("_id").is(id_U));
//        userQ.fields().include("rolex.objComp." + id_C);
//        User user;
//        try {
//            user = mongoTemplate.findOne(userQ, User.class);
//        } catch (RuntimeException e) {
//            return null;
//        }
//        return user.getRolex().getJSONObject("objComp").getJSONObject(id_C);
//
//    }




}