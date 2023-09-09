package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.GoogleLoginService;
import com.cresign.login.utils.googlelogin.GoogleCheckTokenUtils;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.pojo.po.User;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/9/21 16:32
 * @ver 1.0
 */
@Service
public class GoogleLoginServiceImpl implements GoogleLoginService {

//    @Autowired
//    private MongoTemplate mongoTemplate;

    @Autowired
    private RetResult retResult;

    @Autowired
    private Qt qt;

    @Override
    public String googleLogin(String id_Token, String clientType) {

        // 判断有没有aud的key, 没有则参数错误
        if (StringUtils.isNoneEmpty(id_Token)) {

            // 获取返回用户登录信息
            JSONObject resultJson = GoogleCheckTokenUtils.checkGoogleToken(id_Token);

            // 获取是否成功获取
            String resultCode = resultJson.getString("code");


            // 判断如果是 200 了就是正确返回
            if ("200".equals(resultCode)) {

//                Query selectOneUser = new Query(new Criteria("email").is(resultJson.getString("email")));
//                User userOne = mongoTemplate.findOne(selectOneUser, User.class);
//                if (ObjectUtils.isNotEmpty(userOne)) {
//
//                    return "";
//
//                }
                JSONArray es = qt.getES("lNUser", qt.setESFilt("email", resultJson.getString("email")));
                if (null==es||es.size()==0) {

                    return "";

                }

            }

        }
        return "";
    }
}