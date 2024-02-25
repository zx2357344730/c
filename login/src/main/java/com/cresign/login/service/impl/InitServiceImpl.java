package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.InitService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.advice.RsaUtilF;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Init;
import com.cresign.tools.pojo.po.InitJava;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InitServiceImpl implements InitService {

//    public static final String RED_KEY = "key:k_";
//
//    private static final String HD_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVF409Bdp1KRLBP/6UPjePTFEd8bFayzfRo6hHrxURlkLSvT2MMVeOD8J9DMbct/Dpju4uWIUBZC75mwERRD+q8G9r4umRUPokDfL29WSGxDZnr13i8NoI7mJl/D+5XeeHauW9+lYhM98ATtLJOEZ4hKFuVBQm5rHNON3L9dPz7wIDAQAB";

    @Autowired
    private Qt qt;

    @Autowired
    private RetResult retResult;

    @Override
    public ApiResponse getInitById(String lang, Integer ver,String qdKey,String uuId) {

        Init init = qt.getInitData(lang);
        JSONObject re = new JSONObject();
        re.put("qdKey",qdKey);
        Map<String, String> stringMap = RsaUtilF.genKeyPairX();
        assert stringMap != null;
        re.put("privateKey",stringMap.get("privateKey"));
        re.put("publicKey",stringMap.get("publicKey"));

        qt.setRDSet("key", "k_"+ uuId, JSON.toJSONString(re),2592000L);

        if (ver.equals(init.getVer()))
        {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.ALREADY_LOCAL.getCode(), stringMap.get("publicKey"));
//            return retResult.ok(CodeEnum.ALREADY_LOCAL.getCode(), stringMap.get("publicKey"));
        } else {
            JSONObject init2 = qt.toJson(init);
            init2.put("hdKey", re.getString("publicKey"));
            return retResult.ok(CodeEnum.OK.getCode(), init2);
        }

    }

    @Override
    public ApiResponse isDeveloper(String id_U) {

        InitJava init = qt.getInitData();
        Boolean isDev = false;
        String key = id_U + "-1";
        System.out.println("init"+init);

        if (init.getIpList().getLong(key) != null)
        {
            isDev = true;
        }

        return retResult.ok(CodeEnum.OK.getCode(), isDev);
    }


    @Override
    public ApiResponse getPhoneType(String lang) {

        Init init = qt.getInitData(lang);

        JSONArray phoneType = init.getList().getJSONArray("phoneType");

        return retResult.ok(CodeEnum.OK.getCode(), phoneType);
    }


}
