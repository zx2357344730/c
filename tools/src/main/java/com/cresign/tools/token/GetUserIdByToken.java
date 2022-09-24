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
 * @author JackSon
 * @updated 2020/7/29 15:13
 * @ver 1.0
 */
@Component
public class GetUserIdByToken {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate1;

    /**
     *##description:      获取token从redis中拿取id_U
     *@param            jwtStr : token
     *@return
     *@author           JackSon
     *@updated             2020/5/16 9:49
     */
    public String getTokenOfUserId(String jwtStr, String clientType) {


        boolean token_is = jwtUtil.validJWT(clientType, jwtStr);
        if(token_is) {

            JSONObject result = new JSONObject();
            result = JSONObject.parseObject(redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr));

            return result.getString("id_U");
        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

    public JSONObject getTokenData(String jwtStr, String clientType) {


        boolean token_is = jwtUtil.validJWT(clientType, jwtStr);
        if(token_is) {
            JSONObject result = JSONObject.parseObject(redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr));
            return result;

        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

    public JSONObject getTokenDataX(String jwtStr, String clientType,String mod,Integer lev) {


        boolean token_is = jwtUtil.validJWT(clientType, jwtStr);
        if(token_is) {
            //            System.out.println(redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr));
            JSONObject result = JSONObject.parseObject(redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr));
            if ("core".equals(mod) && lev == 1) {
                return result;
            } else {
                JSONObject modAuth = result.getJSONObject("modAuth");
                if (null == modAuth) {
                    throw new ErrorResponseException(HttpStatus.OK, "01119", "该公司没有modAuth");
                } else {
                    JSONObject modJ = modAuth.getJSONObject(mod);
                    if (null == modJ) {
                        throw new ErrorResponseException(HttpStatus.OK, "01117", "该公司没有这个模块功能");
                    } else {
                        Integer bcdLevel = modJ.getInteger("bcdLevel");
                        // 1 < 4 4=4
                        if (lev <= bcdLevel) {
                            return result;
                        } else {
                            throw new ErrorResponseException(HttpStatus.OK, "01118", "当前用户这个模块功能权限不够");
                        }
                    }
                }
            }
//            return redisTemplate1.opsForValue().get(clientType + "Token-" + jwtStr);
//            return result;
        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

}