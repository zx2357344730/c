package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.InitService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.advice.RsaUtilF;
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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class InitServiceImpl implements InitService {

    public static final String RED_KEY = "key:k_";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate1;

    @Autowired
    private RetResult retResult;

    @Override
    public ApiResponse getInitById(String lang, Integer ver,String qdKey,String uuId) {
        System.out.println("uuId:"+uuId);
        Query query = new Query(new Criteria("_id").is(lang));

        query.fields().include("ver");
        //结果
        Init initVerCheck = mongoTemplate.findOne(query, Init.class);

        if (ver.equals(initVerCheck.getVer()))
        {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.ALREADY_LOCAL.getCode(), "");
        }

        JSONObject re = new JSONObject();
        re.put("qdKey",qdKey);
        Map<String, String> stringMap = RsaUtilF.genKeyPairX();
        assert stringMap != null;
        re.put("privateKey",stringMap.get("privateKey"));
        re.put("publicKey",stringMap.get("publicKey"));
        /*
        TimeUnit.SECONDS:秒
        TimeUnit.MINUTES：分
        TimeUnit.HOURS：时
        TimeUnit.DAYS：日
        TimeUnit.MILLISECONDS：毫秒
        TimeUnit.MILLISECONDS：微秒
        TimeUnit.NANOSECONDS：纳秒
        */
        redisTemplate1.opsForValue().set((RED_KEY + uuId),JSON.toJSONString(re),3, TimeUnit.DAYS);//过期时间2天
//        redisTemplate1.opsForValue().set(RED_KEY + uuId,JSON.toJSONString(re));
//        redisTemplate1.expire((RED_KEY + uuId),1000 , TimeUnit.MILLISECONDS);//设置过期时间
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

            init.put("hdKey", re.getString("publicKey"));
            System.out.println("这里返回-1:");
            System.out.println(JSON.toJSONString(init));
            return retResult.ok(CodeEnum.OK.getCode(), init);
//            }
//
//            return retResult.ok(CodeEnum.ALREADY_LOCAL.getCode(), null);

        } else {

            Init init = mongoTemplate.findOne(new Query(new Criteria("_id").is(lang)), Init.class);

            redisTemplate1.opsForHash().put("initData", lang, JSONObject.toJSONString(init));

            assert init != null;
            init.setHdKey(re.getString("publicKey"));
            System.out.println("这里返回-2:");
            System.out.println(JSON.toJSONString(init));
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
