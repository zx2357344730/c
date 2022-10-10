package com.cresign.tools.token;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Qt qt;

    /**
     *##description:      获取token从redis中拿取id_U
     *@param            jwtStr : token
     *@return
     *@author           JackSon
     *@updated             2020/5/16 9:49
     */
    public String getTokenOfUserId(String jwtStr, String clientType) {

        if(qt.hasRDKey(clientType+"Token", jwtStr)) {
            return  qt.getRDSet(clientType+"Token",jwtStr).getString("id_U");
       }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

    public JSONObject getTokenData(String jwtStr, String clientType) {

        if(qt.hasRDKey(clientType+"Token", jwtStr)) {
            return qt.getRDSet(clientType+"Token",jwtStr);
        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

    public JSONObject getTokenDataX(String jwtStr, String clientType,String mod,Integer lev) {

        if(qt.hasRDKey(clientType+"Token", jwtStr)) {
            //            System.out.println(redisTemplate0.opsForValue().get(clientType + "Token:" + jwtStr));
            JSONObject result = new JSONObject();
            result = qt.getRDSet(clientType+"Token",jwtStr);

            if ("core".equals(mod) && lev == 1) {
                    return result;
            } else {
                JSONObject modAuth = result.getJSONObject("modAuth");
                JSONObject modJ = modAuth.getJSONObject(mod);
                if (null == modAuth) {
                    throw new ErrorResponseException(HttpStatus.OK, "01119", "该公司没有modAuth");
                } else {
                    modJ = modAuth.getJSONObject(mod);
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
        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

}