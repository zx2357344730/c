package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.AuthFilterService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/8/6 10:29
 * ##version: 1.0
 */
@Service
public class AuthFilterServiceImpl implements AuthFilterService {


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private RetResult retResult;


//    @Override
//    // 1. only findOne @ grpU.listType.grp ...
//    // 2. use new id_A for roleQ
//    // 3. change role datastruct into read[] rw[] batch[] logtype[]
//    // 4. need check rolex module, and then check redis module card/batch/logType List
//
//    public ApiResponse getUserSelectAuth(String id_U, String id_C, String listType, String grp, String authType) {
//        // 最终返回数组
//        JSONArray resultArray = new JSONArray();
//
//        JSONObject rolex = MongoUtils.getRolex(id_U, id_C, mongoTemplate);
//
//        if (rolex == null){
//            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.LOGIN_NOTFOUND_USER.getCode(), null);
//        }
//
//        // 用户的role下标
//        String user_grpU = rolex.getString("grpU");
//        // redis cache
//        if (redisTemplate0.hasKey("login:get_read_auth:compId-" + id_C)) {
//            if (redisTemplate0.opsForHash().hasKey("login:get_read_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType)) {
//                String val = redisTemplate0.opsForHash().get("login:get_read_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType).toString();
//                return retResult.okNoEncode(CodeEnum.OK.getCode(), JSONObject.parse(val));
//
//            }
//        }
//String id_A = dbUtils.getId_A(id_C, "a-auth");
//	Query menuQuery = new Query(new Criteria("_id").is(id_A));
//        Query roleQ = new Query(new Criteria("info.id_C").is(id_C).and("info.ref").is("a-auth"));
//        roleQ.fields().include("role");
//        Asset asset = mongoTemplate.findOne(roleQ, Asset.class);
//        //JSONObject roleJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(roleQ, Asset.class));
//
//
//        JSONObject grpJson = asset.getRole().getJSONObject("objAuth").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);
//        //JSONObject grpJson = roleJson.getJSONObject("role").getJSONObject("objAuth").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);
//
//
//        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//        LocalDateTime nowDate = LocalDateTime.now();
//        String localTime = df.format(nowDate);
//
//        if (ObjectUtils.isEmpty(grpJson)) {
//            throw new ErrorResponseException(HttpStatus.FORBIDDEN, LoginEnum.GRP_NOT_AUTH.getCode(), null);
//        }
//        // 权限的数组
//        JSONArray authTypeArray = grpJson.getJSONArray(authType);
//
//        for (int i = 0; i < authTypeArray.size(); i++) {
//            JSONObject authJson = authTypeArray.getJSONObject(i);
//            // 这里只判断了有读写权限，没有去判断 用户是否有用该模块
//            if (authJson.getInteger("auth").equals(1) || authJson.getInteger("auth").equals(2)) {
//                resultArray.add(authJson.getString("ref"));
//            }
//
//        }
//
////        for (Object authTypeObj : authTypeArray) {
////
////            JSONObject authJson = (JSONObject) authTypeObj;
////
////            // 这里只判断了有读写权限，没有去判断 用户是否有用该模块
////            if (authJson.getInteger("auth").equals(1) || authJson.getInteger("auth").equals(2)) {
////                resultArray.add(authJson.getString("ref"));
////            }
////
////            // 这里是判断了用户有没有该模块，并且判断是否有读写权限
//////            for (Object objMod : dataObj.getJSONArray("objMod")) {
//////
//////                JSONObject objModJson = (JSONObject) JSON.toJSON(objMod);
//////
//////                // 判断如果相同的话就拿出
//////                if (authJson.getString("modRef").equals(objModJson.getString("ref"))) {
//////
//////                    if (localTime.compareTo(objModJson.getString("tfin")) < 0 || objModJson.getString("tfin").equals("-1")) {
//////
//////
//////                        if (authJson.getInteger("auth").equals(1) || authJson.getInteger("auth").equals(2)) {
//////                            resultArray.add(authJson.getString("ref"));
//////                        }
//////
//////                    } else {
//////                        return RetResult.errorJsonResult(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(),null);
//////                    }
//////
//////                }
//////
//////            }
////
////        }
//
//        // 如果 selectResult 等于空则无权限
//        if (ObjectUtils.isEmpty(resultArray)) {
//            // 无权限
//            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//        }
//
//
//        // 硬编+view卡片的权限
//        resultArray.add("view");
//
//        JSONObject result = new JSONObject();
//        result.put("result", resultArray);
//        result.put("user_grpU", user_grpU);
//
//        redisTemplate0.opsForHash().put("login:get_read_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType, result.toJSONString());
//
//
//        return retResult.okNoEncode(CodeEnum.OK.getCode(), result);
//
//
//    }
//
//
//
//    @Override
//    public ApiResponse getUserUpdateAuth(String id_U, String id_C, String listType, String grp, String authType, JSONArray params) {
//
//        // 最终返回数组
//        JSONArray resultArray = new JSONArray();
//
//        JSONObject rolex = MongoUtils.getRolex(id_U, id_C, mongoTemplate);
//
//        if (rolex == null){
//            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, LoginEnum.LOGIN_NOTFOUND_USER.getCode(), null);
//        }
//        // 用户的role下标
//        String user_grpU = rolex.getString("grpU");
//
//        // redis cache
//        if (redisTemplate0.hasKey("login:get_readwrite_auth:compId-" + id_C)) {
//
//            if (redisTemplate0.opsForHash().hasKey("login:get_readwrite_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType)) {
//                String val = redisTemplate0.opsForHash().get("login:get_readwrite_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType).toString();
//                return retResult.okNoEncode(CodeEnum.OK.getCode(), JSONObject.parse(val));
//
//            }
//
//        }
//String id_A = dbUtils.getId_A(id_C, "a-auth");
//	Query menuQuery = new Query(new Criteria("_id").is(id_A));
//        Query roleQ = new Query(new Criteria("info.id_C").is(id_C).and("info.ref").is("a-auth"));
//        roleQ.fields().include("role");
//        Asset asset = mongoTemplate.findOne(roleQ, Asset.class);
//        //JSONObject roleJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(roleQ, Asset.class));
//
//        JSONObject grpJson = asset.getRole().getJSONObject("objAuth").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);
//        //JSONObject grpJson = roleJson.getJSONObject("role").getJSONObject("objAuth").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);
//
//
//        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//        LocalDateTime nowDate = LocalDateTime.now();
//        String localTime = df.format(nowDate);
//
//        // 权限的数组
//        JSONArray authTypeArray = grpJson.getJSONArray(authType);
//
//        for (int i = 0; i < authTypeArray.size(); i++) {
//
//            JSONObject authJson = authTypeArray.getJSONObject(i);
//
//            for (Object objMod : rolex.getJSONArray("objMod")) {
//
//                JSONObject objModJson = (JSONObject) JSON.toJSON(objMod);
//
//                // 判断如果相同的话就拿出
//                if (authJson.getString("modRef").equals(objModJson.getString("ref")))
//                    if (localTime.compareTo(objModJson.getString("tfin")) >= 0 && !objModJson.getString("tfin").equals("-1")) {
//                        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
//                    } else if (authJson.getInteger("auth").equals(2)) {
//                        resultArray.add(authJson.getString("ref"));
//                    }
//
//            }
//        }
//
////        for (Object authTypeObj : authTypeArray) {
////
////            JSONObject authJson = (JSONObject) authTypeObj;
////
////            for (Object objMod : rolex.getJSONArray("objMod")) {
////
////                JSONObject objModJson = (JSONObject) JSON.toJSON(objMod);
////
////                // 判断如果相同的话就拿出
////                if (authJson.getString("modRef").equals(objModJson.getString("ref")))
////                    if (localTime.compareTo(objModJson.getString("tfin")) >= 0 && !objModJson.getString("tfin").equals("-1")) {
////                        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
////
////                    } else if (authJson.getInteger("auth").equals(2)) {
////                        resultArray.add(authJson.getString("ref"));
////                    }
////
////            }
////
////
////        }
//
//        // 硬编+view卡片的权限
//        resultArray.add("view");
//
//        JSONArray cloneArray = (JSONArray) params.clone();
//        params.removeAll(resultArray);
//
//
//        if (params.size() > 0)
//            // 无权限
//            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
//
//        JSONObject result = new JSONObject();
//        result.put("result", resultArray);
//        result.put("user_grpU", user_grpU);
//
//        redisTemplate0.opsForHash().put("login:get_readwrite_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType, result.toJSONString());
//
//        // 返回params
//        return retResult.okNoEncode(CodeEnum.OK.getCode(), result);
//
//
//    }

    @Override
    public ApiResponse getTouristAuth(String id_C, String listType, String grp, String authType) {
        // 最终返回数组
        JSONArray resultArray = new JSONArray();


        // 用户的role下标
        String user_grpU = "1099";

        String id_A = dbUtils.getId_A(id_C, "a-auth");
        Query roleQ = new Query(new Criteria("_id").is(id_A));
        roleQ.fields().include("role");
        Asset asset = mongoTemplate.findOne(roleQ, Asset.class);
        //JSONObject roleJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(roleQ, Asset.class));

        JSONObject grpJson = null;
        try {
             grpJson = asset.getRole().getJSONObject("objAuth").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);

            //grpJson = roleJson.getJSONObject("role").getJSONObject("objAuth").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);
        } catch (RuntimeException e) {
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, LoginEnum.NOT_FOUND_AUTH.getCode(), null);

        }

        if (ObjectUtils.isEmpty(grpJson)){

            throw new ErrorResponseException(HttpStatus.FORBIDDEN, LoginEnum.GRP_NOT_AUTH.getCode(), null);

        }

        // 权限的数组
        JSONArray authTypeArray = grpJson.getJSONArray(authType);


        for (Object authTypeObj : authTypeArray) {
            JSONObject authJson = (JSONObject) authTypeObj;
            if (authJson.getInteger("auth").equals(1) || authJson.getInteger("auth").equals(2)) {
                resultArray.add(authJson.getString("ref"));
            }
        }

        // 如果 selectResult 等于空则无权限
        if (ObjectUtils.isEmpty(resultArray)){
            // 无权限
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
        }


        // 硬编+view卡片的权限
        resultArray.add("view");
        return retResult.okNoEncode(CodeEnum.OK.getCode(), resultArray);
    }


}