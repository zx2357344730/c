package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.client.WSFilterClient;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.AccountLoginService;
import com.cresign.login.utils.LoginResult;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.uuid.UUID19;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/7/25 10:14
 * ##version: 1.0
 */
@Service
@Slf4j
public class AccountLoginServiceImpl implements AccountLoginService {

    //降低 Autowired 检测的级别，将 Severity 的级别由之前的 error 改成 warning 或其它可以忽略的级别。
    @Autowired
    private WSFilterClient wsFilterClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LoginResult loginResult;

    @Autowired
    private RetResult retResult;

    @Autowired
    private StringRedisTemplate redisTemplate1;

    public static final String SCANCODE_LOGINCOMP = "scancode:logincomp-";

    public static final String HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_LOGIN_COMP_T = "https://www.cresign.cn/qrCodeTest?qrType=logincomp&t=";


    /**
     * 账号登录方法
     * ##Params: usn 用户名
     * ##Params: pwd 密码
     * ##Params: clientType 客户端
     * ##author: JackSon
     * ##updated: 2020/7/29 10:20
     * ##Return: java.lang.String
     */
    @Override
    public ApiResponse doNumberLogin(String clientType) {

        // 声明转换后结果存放
        JSONObject result = new JSONObject();
        //创建查询对象
//        Query query = new Query(Criteria.where("info.usn").is(usn));
        Query query = new Query(Criteria.where("_id").is("5f28bf314f65cc7dc2e60262"));
        // 创建Auth对象存放查询后的结果
        User user = mongoTemplate.findOne(query, User.class);

        // 初步判断用户名是否存在

                // 返回json数据给前端
                result = loginResult.allResult(user, clientType, "number");

                return retResult.ok(CodeEnum.OK.getCode(),result);
    }

    @Override
    public ApiResponse generateLoginCode(String id) {

        String token = UUID19.uuid();

        JSONObject qrObject = new JSONObject();

        qrObject.put("id", id);
        qrObject.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String keyName = SCANCODE_LOGINCOMP + token;

        qrObject.put("endTimeSec", "300");
        redisTemplate1.opsForHash().putAll(keyName, qrObject);
        redisTemplate1.expire(keyName, 300, TimeUnit.SECONDS);

        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_LOGIN_COMP_T + token;
        System.out.println(token);
        return retResult.ok(CodeEnum.OK.getCode(), url);

    }

    @Override
    public ApiResponse scanLoginCode( String token, String id_U) {

        String keyName = SCANCODE_LOGINCOMP + token;
        Boolean hasKey = redisTemplate1.hasKey(keyName);

        if (!hasKey) {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
LOGIN_CODE_OVERDUE.getCode(),null);
        }

        // 获取到整个hash
        Map<Object, Object> entries = redisTemplate1.opsForHash().entries(keyName);

        Query query = new Query(Criteria.where("_id").is(id_U));

        // 创建Auth对象存放查询后的结果
        User user = mongoTemplate.findOne(query, User.class);

        if (user == null)
        {
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
        }

        // 返回json数据给前端
        JSONObject result = loginResult.allResult(user, "web", "web");
        System.out.println("result = " + result);
        //判断redis里面有  webSocket_id键
        if (entries.containsKey("id")){
            //1.是否链接成功
            System.out.println(entries.get("id"));
                JSONObject reqJson = new JSONObject();
                reqJson.put("id",entries.get("id"));
                reqJson.put("infoData",JSON.toJSONString(result));
                //发送消息体
                wsFilterClient.sendLoginDesc(reqJson);

        }

        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


}