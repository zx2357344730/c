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

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class InitServiceImpl implements InitService {

    public static final String RED_KEY = "key:k_";

    private static final String HD_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVF409Bdp1KRLBP/6UPjePTFEd8bFayzfRo6hHrxURlkLSvT2MMVeOD8J9DMbct/Dpju4uWIUBZC75mwERRD+q8G9r4umRUPokDfL29WSGxDZnr13i8NoI7mJl/D+5XeeHauW9+lYhM98ATtLJOEZ4hKFuVBQm5rHNON3L9dPz7wIDAQAB";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private RetResult retResult;

    @Override
    public ApiResponse getInitById(String lang, Integer ver,String qdKey,String uuId) {
        System.out.println("uuId:"+uuId);
        Query query = new Query(new Criteria("_id").is(lang));

        query.fields().include("ver");
        //结果
        Init initVerCheck = mongoTemplate.findOne(query, Init.class);
        System.out.println(ver);




        JSONObject re = new JSONObject();
        re.put("qdKey",qdKey);
        Map<String, String> stringMap = RsaUtilF.genKeyPairX();
        System.out.println("输出加密:");
        System.out.println(JSON.toJSONString(stringMap));
        assert stringMap != null;
        re.put("privateKey",stringMap.get("privateKey"));
        re.put("publicKey",stringMap.get("publicKey"));
        redisTemplate0.opsForValue().set((RED_KEY + uuId),JSON.toJSONString(re),3, TimeUnit.DAYS);//过期时间3天

        if (ver.equals(initVerCheck.getVer()))
        {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.ALREADY_LOCAL.getCode(), stringMap.get("publicKey"));
        }

        // 判断 redis 中是否有这个键
        if (redisTemplate0.opsForHash().hasKey("initData", lang)) {

            String initData = (String)redisTemplate0.opsForHash().get("initData", lang);

            JSONObject init = JSONObject.parseObject(initData);

//            JSONObject langJson = (JSONObject) jsonObject.get("lang");

//            if (null != ver && !ver.equals(langJson.getInteger("ver"))) {
//
//                Init init = mongoTemplate.findOne(new Query(new Criteria("_id").is(lang)), Init.class);
//
//                redisTemplate0.opsForHash().put("initData", lang, JSONObject.toJSONString(init));

//            if (isDecrypt.equals("false")) {
//                init.put("hdKey", HD_KEY);
//            } else {
                init.put("hdKey", re.getString("publicKey"));
//            }
            System.out.println("这里返回-1:"+init.getString("hdKey"));
            System.out.println(JSON.toJSONString(init));
            return retResult.ok(CodeEnum.OK.getCode(), init);
//            }
//
//            return retResult.ok(CodeEnum.ALREADY_LOCAL.getCode(), null);

        } else {

            Init init = mongoTemplate.findOne(new Query(new Criteria("_id").is(lang)), Init.class);

            redisTemplate0.opsForHash().put("initData", lang, JSONObject.toJSONString(init));

            assert init != null;
//            if (isDecrypt.equals("false")) {
//                init.setHdKey(HD_KEY);
//            } else {
                init.setHdKey(re.getString("publicKey"));
//            }
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

//
//    public ApiResponse getInitInclude(String lang,Integer ver, String include) {
//
//        Query query = new Query(new Criteria("_id").is(lang));
//
//        query.fields().include(include).include("ver");
//        //结果
//        Init init = mongoTemplate.findOne(query, Init.class);
//
//        if (null != ver && !ver.equals(init.getVer())) {
//
//            return retResult.ok(CodeEnum.OK.getCode(), init);
//        }
//        throw new ErrorResponseException(HttpStatus.OK, CodeEnum.ALREADY_LOCAL.getCode(), "");
//
//
//    }

}
