package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.InitService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Init;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class InitServiceImpl implements InitService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate1;

    @Autowired
    private RetResult retResult;

    @Override
    public ApiResponse getInitById(String lang, Integer ver) {

        Query query = new Query(new Criteria("_id").is(lang));

        query.fields().include("ver");
        //结果
        Init initVerCheck = mongoTemplate.findOne(query, Init.class);

        if (ver.equals(initVerCheck.getVer()))
        {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.ALREADY_LOCAL.getCode(), "");
        }

        // 判断 redis 中是否有这个键
        if (redisTemplate1.opsForHash().hasKey("initData", lang)) {

            String initData = (String)redisTemplate1.opsForHash().get("initData", lang);

            JSONObject init = JSONObject.parseObject(initData);

//            JSONObject langJson = (JSONObject) jsonObject.get("lang");

//            if (null != ver && !ver.equals(langJson.getInteger("ver"))) {
//
//                Init init = mongoTemplate.findOne(new Query(new Criteria("_id").is(lang)), Init.class);
//
//                redisTemplate1.opsForHash().put("initData", lang, JSONObject.toJSONString(init));

            return retResult.ok(CodeEnum.OK.getCode(), init);
//            }
//
//            return retResult.ok(CodeEnum.ALREADY_LOCAL.getCode(), null);

        } else {

            Init init = mongoTemplate.findOne(new Query(new Criteria("_id").is(lang)), Init.class);

            redisTemplate1.opsForHash().put("initData", lang, JSONObject.toJSONString(init));

            return retResult.ok(CodeEnum.OK.getCode(), init);
        }

    }

    @Override
    public ApiResponse getPhoneType(String lang) {

        Query query = new Query(new Criteria("_id").is(lang));
        query.fields().include("list.phoneType");

        Init init = mongoTemplate.findOne(query, Init.class);

        JSONArray phoneType = init.getList().getJSONArray("phoneType");

        return retResult.ok(CodeEnum.OK.getCode(), phoneType);
    }


    public ApiResponse getInitInclude(String lang,Integer ver, String include) {

        Query query = new Query(new Criteria("_id").is(lang));

        query.fields().include(include).include("ver");
        //结果
        Init init = mongoTemplate.findOne(query, Init.class);

        if (null != ver && !ver.equals(init.getVer())) {

            return retResult.ok(CodeEnum.OK.getCode(), init);
        }
        throw new ErrorResponseException(HttpStatus.OK, CodeEnum.ALREADY_LOCAL.getCode(), "");


    }

}
