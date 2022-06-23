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
     * 根据can获取日志集合
     * ##param id	日志编号
     * ##param page	第几页
     * ##param pageSize	页大小
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/19 9:51
     */
//    ApiResponse getLogListByEsMsg(String id,int page,int pageSize);

    /**
     * 根据can获取日志集合，并携带用户信息
     * ##param id	日志编号
     * ##param page	第几页
     * ##param pageSize	页大小
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 15:08
     */
//    ApiResponse getLogListByEsMsgAndUserInfo(String id,int page,int pageSize);

    /**
     * 获取用户日志集合
     * ##param cid	公司编号
     * ##param status	状态
     * ##param uId	用户编号
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 14:22
     */
//    ApiResponse getUserLogList(String cid, String status, String uId);


    /**
     * 根据cId和uId获取用户私人聊天日志列表
     * ##Params: uId	用户id
     * ##Params: cId	公司id
     * ##return: java.util.List<com.cresign.chat.pojo.po.LogFlow>  返回结果: 私人聊天日志列表
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 10:59
     */
//    List<LogFlow> getListLogByU(String uId, String cId);

    /**
     * 推送
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 15:01
     */
//    ApiResponse sendPush();

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
    void sendPush(String clientId,String title,String body);

    void sendPushX(JSONArray pushUList,String title,String body);

    void sendTestToListPush(JSONArray pushUList, String title, String body);

    /**
     * 发送logL信息到User-websocket
     * ##param logL	日志信息
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/8/4 15:39
     */
    void sendLogWSU(LogFlow logL);

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

//    /**
//     * 根据can获取单个用户信息
//     * Params: uid	用户编号
//     * return: java.lang.String  返回结果:用户信息
//     * Author: tang
//     * version: 1.0.0
//     * Updated: 2020/8/6 8:21
//     */
//    ApiResponse getIdCAndRpI();

//    ApiResponse sendWSU(LogFlow logL);

//    JSONObject getOrderDataByLogType(String oid);


}
