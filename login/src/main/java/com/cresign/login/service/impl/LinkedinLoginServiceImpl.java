package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.LinkedinLoginService;
import com.cresign.login.utils.LoginResult;
import com.cresign.login.utils.RegisterUserUtils;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.SMSTypeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.request.HttpRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/7/29 11:10
 * ##version: 1.0
 */
@RefreshScope
@Service
public class LinkedinLoginServiceImpl implements LinkedinLoginService {

    @Value("${linked.web.clientId}")
    private String client_id;

    @Value("${linked.web.clientSecret}")
    private String client_secret;

    @Value("${linked.web.redirectUri}")
    private String redirect_uri;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate1;

    @Autowired
    private RegisterUserUtils registerUserUtils;

    @Autowired
    private LoginResult loginResult;

    @Autowired
    private RetResult retResult;

    @Autowired
    private HttpServletRequest request;

    @Override
    public ApiResponse linkedinWebLogin(String code, String clientType) {


        String url = "https://www.linkedin.com/oauth/v2/accessToken";
        String param = "grant_type=authorization_code&" +
                "code=" + code +
                "&redirect_uri=" + "http://localhost:8080/linklogin" +
                "&client_id=" + client_id +
                "&client_secret=" + client_secret;

        //发送POST请求：使用授权代码交换访问令牌
        String result = HttpRequest.sendPost(url, param);


        JSONObject resultJson = JSON.parseObject(result);
        String access_token = resultJson.getString("access_token");

        JSONObject userInfoJson = getLinkedinUserInfo(access_token);

//        // 用户id
//        String linkedin_id = userInfoJson.getString("id");
//
//        // 用户名称
//        String userName = userInfoJson.getString("localizedLastName") + userInfoJson.getString("localizedFirstName");
//


        String id_lk = userInfoJson.getString("id");

        Query query = new Query(new Criteria("info.id_lk").is(id_lk));

        User user = mongoTemplate.findOne(query, User.class);

        // 判断用户是否存在
        if (user!=null){

            //判断公司id是否为空
//            if (user.getLSComp() != null){
//
//                // 获取用户登录的数据
//                JSONObject userResult = loginResult.allResult(user, clientType, "facebook");
//
//                // 重新设值用户的默认登录的公司信息到redis中
////                oauth.setCompMenuAndRole(user.getInfo().get("def_C").toString());
//
//                return retResult.ok(CodeEnum.OK.getCode(), userResult);
//
//            }


        }

        throw new ErrorResponseException(HttpStatus.OK, LoginEnum.LINKED_NOT_BIND.getCode(), null);

    }

    @Override
    public ApiResponse registerLinked(String code, String phone, String phoneType, String smsNum) {

        // 判断是否存在这个 key
        if (redisTemplate1.hasKey(SMSTypeEnum.REGISTER.getSmsType() + phone)) {

            // 判断redis中的 smsSum 是否与前端传来的 smsNum 相同
            if (smsNum.equals(redisTemplate1.opsForValue().get(SMSTypeEnum.REGISTER.getSmsType() + phone))) {


                String url = "https://www.linkedin.com/oauth/v2/accessToken";
                String param = "grant_type=authorization_code&" +
                        "code=" + code +
                        "&redirect_uri=" + "http://localhost:8080/linklogin" +
                        "&client_id=" + client_id +
                        "&client_secret=" + client_secret;

                //发送POST请求：使用授权代码交换访问令牌
                String result = HttpRequest.sendPost(url, param);


                JSONObject resultJson = JSON.parseObject(result);
                String access_token = resultJson.getString("access_token");

                JSONObject userInfoJson = getLinkedinUserInfo(access_token);



                String id_lk = userInfoJson.getString("id");

                String wcnN = userInfoJson.getString("localizedLastName") + userInfoJson.getString("localizedFirstName");

                Query id_WXQue = new Query(new Criteria("info.id_lk").is(id_lk));

                User userFb = mongoTemplate.findOne(id_WXQue, User.class);

                if (ObjectUtils.isNotEmpty(userFb)) {

                    throw new ErrorResponseException(HttpStatus.OK, LoginEnum.REGISTER_USER_IS_HAVE.getCode(), null);

                }

                Query mbnQue = new Query(new Criteria("info.mbn").is(phone));

                User user = mongoTemplate.findOne(mbnQue, User.class);

                if (ObjectUtils.isNotEmpty(user)) {

                    Update update = new Update();
                    update.set("info.id_lk", id_lk);
                    update.set("info.tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));

                    mongoTemplate.updateFirst(mbnQue, update, User.class);

                    redisTemplate1.opsForValue().set(SMSTypeEnum.LOGIN.getSmsType() + phone, smsNum, 3, TimeUnit.MINUTES);

                    return retResult.ok(CodeEnum.OK.getCode(), null);
                }



                Map<String, String> wrdN = new HashMap<>(1);
                wrdN.put(request.getHeader("lang"), wcnN);
                // 设置info信息
                JSONObject infoJson = new JSONObject();
                infoJson.put("wrdN", wrdN);
                infoJson.put("id_lk", id_lk);
                infoJson.put("mbn", phone);
                infoJson.put("pic", "https://cresign-1253919880.cos.ap-guangzhou.myqcloud.com/pic_small/userRegister.jpg");
                infoJson.put("phoneType", phoneType);
                infoJson.put("tmk", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
                infoJson.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));


                // 调用注册用户方法
                registerUserUtils.registerUser(infoJson);

                redisTemplate1.opsForValue().set(SMSTypeEnum.LOGIN.getSmsType() + phone, smsNum, 3, TimeUnit.MINUTES);

                return retResult.ok(CodeEnum.OK.getCode(), null);

            } else {

                throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
SMS_CODE_NOT_CORRECT.getCode(), null);
            }

        } else {

            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
SMS_CODE_NOT_FOUND.getCode(), null);

        }
    }


    public JSONObject getLinkedinUserInfo(String access_token) {

        String url = "https://api.linkedin.com/v2/me";

        com.github.kevinsawicki.http.HttpRequest httpRequest = com.github.kevinsawicki.http.HttpRequest.get(url);
        // 设置请求超时时间
        httpRequest.connectTimeout(60000);
        // 设置读取超时时间
        httpRequest.readTimeout(60000);
        // 设置头部信息
        httpRequest.header("Authorization","Bearer " + access_token);

        return JSONObject.parseObject(httpRequest.body());

    }



}