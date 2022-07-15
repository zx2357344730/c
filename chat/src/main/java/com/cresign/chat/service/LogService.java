package com.cresign.chat.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.pojo.po.LogFlow;

/**
 * ##author: tangzejin
 * ##updated: 2019/7/16
 * ##version: 1.0.0
 * ##description: 日志接口类
 */
public interface LogService {

    /**
     * 推送
     * ##param clientId	推送id
     * ##param title	推送标题
     * ##param body	推送内容
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/7/12 14:47
     */
    void sendPush(String clientId,String title,String body,String token);

    void sendPushBatch(JSONArray cidArray, String title, String body);

    ApiResponse rpiCode(JSONObject can);

    ApiResponse requestRpi(JSONObject can);

    ApiResponse requestRpiStatus(JSONObject can);

    ApiResponse bindingRpi(JSONObject can);

    ApiResponse relieveRpi(JSONObject can);

    /**
     * 根据can获取单个用户信息
     * Params: uid	用户编号
     * return: java.lang.String  返回结果:用户信息
     * Author: tang
     * version: 1.0.0
     * Updated: 2020/8/6 8:21
     */
    ApiResponse setGpio(JSONObject can);

    /**
     * 根据can获取单个用户信息
     * Params: uid	用户编号
     * return: java.lang.String  返回结果:用户信息
     * Author: tang
     * version: 1.0.0
     * Updated: 2020/8/6 8:21
     */
    ApiResponse unsetGpio(JSONObject can);

    String getToken();


//    /**
//     * 根据can获取单个用户信息
//     * Params: uid	用户编号
//     * return: java.lang.String  返回结果:用户信息
//     * Author: tang
//     * version: 1.0.0
//     * Updated: 2020/8/6 8:21
//     */
//    ApiResponse getIdCAndRpI();

//    JSONObject getOrderDataByLogType(String oid);


}
