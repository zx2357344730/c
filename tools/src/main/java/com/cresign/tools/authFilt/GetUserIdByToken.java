package com.cresign.tools.authFilt;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

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

    @Autowired
    private Ws ws;

    /**
     *##description:      获取token从redis中拿取id_U
     *@param            jwtStr : token
     *@return           either id_U or 401 (Forbidden) and stop the API
     *@author           JackSon
     *@updated             2020/5/16 9:49
     */
    public String getTokenOfUserId(String jwtStr, String clientType) {

        if(qt.hasRDKey(clientType+"Token", jwtStr)) {
            return  qt.getRDSet(clientType+"Token",jwtStr).getString("id_U");
       }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

    /**
     *##description:      获取token从redis中拿取 整个Redis 内容
     *@param            jwtStr : token
     *@return           either whole User current login content or 401 (Forbidden) and stop the API
     *@author           JackSon
     *@updated             2020/5/16 9:49
     */
    public JSONObject getTokenData(String jwtStr, String clientType) {

        if(qt.hasRDKey(clientType+"Token", jwtStr)) {
            return qt.getRDSet(clientType+"Token",jwtStr);
        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

    public ApiResponse err(JSONObject params, String apiName, Exception e)
    {
        if (e.getClass().equals(ErrorResponseException.class) &&
                (((ErrorResponseException) e).getStatus().equals(HttpStatus.OK) ||
                        ((ErrorResponseException) e).getStatus().equals(HttpStatus.FORBIDDEN)))
        {
            System.out.println(e.getClass());
            System.out.println(((ErrorResponseException) e).getCode());
            System.out.println(((ErrorResponseException) e).getDes());
            throw new ErrorResponseException(((ErrorResponseException) e).getStatus(),
                    ((ErrorResponseException) e).getCode(), ((ErrorResponseException) e).getDes());
        }
        e.printStackTrace();
        StringWriter writer = new StringWriter();
        PrintWriter printWriter= new PrintWriter(writer);
        e.printStackTrace(printWriter);
        String msg = params.toJSONString();
        String msg2 = writer.toString().substring(0, 450);
        ws.sendErrorToUsageflow(qt.setJson("cn", apiName), apiName + "/n" + msg + "/n/n" + msg2, "error", "ALL");

        throw new ErrorResponseException(HttpStatus.OK, ((ErrorResponseException) e).getCode(), writer.toString());
    }


    public JSONObject getTokenDataX(String jwtStr, String clientType,String mod,Integer lev) {

        //TODO KEV check if equipped or not
        //dataSet has modAuth, but if I want to check this grpU can do whatever?
        //get redis.login:readwrite_auth - key: 1001_lSComp_1000_(batch/card/log).result = list of workable things
        //params: lType, grp, tokData.grpU, tokData.modArray + initData compare then good, authType, card/batch/logName
        //getfrom Redis if ok, then ok. else 401 @ no compAuth or no modAuth


        // get lType, check authType (card/batch/log/none),  and grp
        if(qt.hasRDKey(clientType+"Token", jwtStr)) {
            //            System.out.println(redisTemplate0.opsForValue().get(clientType + "Token:" + jwtStr));
            JSONObject result;
            result = qt.getRDSet(clientType+"Token",jwtStr);

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
        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), "");
    }

}