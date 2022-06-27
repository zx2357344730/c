package com.cresign.tools.token;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * ##description: 通过token获取uid
 * ##author: JackSon
 * ##updated: 2020/7/29 15:13
 * ##version: 1.0
 */
@Component
public class GetUserIdByToken {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate1;

    /**
     *##description:      获取token从redis中拿取id_U
     *##Params:            jwtStr : token
     *##Return:
     *##author:           JackSon
     *##updated:             2020/5/16 9:49
     */
    public String getTokenOfUserId(String jwtStr, String clientType) {


        boolean token_is = jwtUtil.validJWT(clientType, jwtStr);
        if(token_is) {

            JSONObject result = new JSONObject();
//            System.out.println(redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr));
            result = JSONObject.parseObject(redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr));
//            System.out.println(result.getString("id_C"));
//            System.out.println(result.getString("id_U"));
//            System.out.println(result.getString("grpU"));
//            System.out.println(result.getString("dep"));


//            return redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr);
            return result.getString("id_U");
        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

    public JSONObject getTokenData(String jwtStr, String clientType) {


        boolean token_is = jwtUtil.validJWT(clientType, jwtStr);
        if(token_is) {
            //            System.out.println(redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr));
            JSONObject result = JSONObject.parseObject(redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr));
//            return redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr);
            return result;
        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

}