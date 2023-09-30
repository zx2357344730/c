package com.cresign.purchase.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.pojo.po.LogFlow;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName ZjTestService
 * @Date 2023/8/10
 * @ver 1.0.0
 */
public interface ZjTestService {
    ApiResponse getMdSetEs(String key,String esIndex,String condition,String val);
    ApiResponse sendLog(LogFlow logFlow);
    ApiResponse sendLogSp(String id_U, String id_C,String id,String logType
            ,String subType,String zcnDesc, JSONObject data);
    ApiResponse sendLogXj(String id_U, String id_C,String id,String logType
            ,String subType,String zcnDesc, JSONObject data);
    ApiResponse getLog(String id,String logType,String subType,String id_SP);
    ApiResponse getLogSp(String id,String id_SP);
    ApiResponse getLogXj(String id,String id_SP);
    ApiResponse shareSave(JSONObject data);
    ApiResponse shareOpen(String shareId);
    ApiResponse initFC(String id_C,String id_U);
    ApiResponse getFCAuth(String id_C,String id);
    ApiResponse setFCAuth(String id_C,String id,JSONObject users);
    ApiResponse getFCAuthByUser(String id_C,String id_U);
    ApiResponse getLSProdShareId(String id_P);
    ApiResponse getLSInfoShareId(String id_I);
    ApiResponse getLNUserShareId(String id_U);
    ApiResponse getLNCompShareId(String id_C);
    ApiResponse getLBProdShareId(String id_P);
    ApiResponse getLBInfoShareId(String id_I);
}