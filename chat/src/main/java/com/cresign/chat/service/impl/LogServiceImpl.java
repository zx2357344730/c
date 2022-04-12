package com.cresign.chat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.common.ChatEnum;
import com.cresign.chat.config.websocket.WebSocketServerPi;
import com.cresign.chat.config.websocket.WebSocketUserServer;
import com.cresign.chat.service.LogService;
import com.cresign.chat.utils.HttpClientUtils;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.logger.LogUtil;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;


/**
 * ##author: tangzejin
 * ##updated: 2019/7/16
 * ##version: 1.0.0
 * ##description: 日志实现类
 */
@Service
public class LogServiceImpl  implements LogService {

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private RetResult retResult;

    @Autowired
    private CoupaUtil coupaUtil;

    public static final String appId = "KVB0qQq0fRArupojoL4WM9";


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

    @Override
    public void sendPush(String clientId,String title,String body){
        String request_id = MongoUtils.GetObjectId();
        JSONObject settings = new JSONObject();
        int ttl = 3600000;
        settings.put("ttl",ttl);
        JSONObject audience = new JSONObject();
        JSONArray cid = new JSONArray();
        cid.add(clientId);
        audience.put("cid",cid);
        JSONObject push_message = new JSONObject();
        JSONObject notification = new JSONObject();
        notification.put("title",title);
        notification.put("body",body);
        notification.put("click_type","startapp");
        notification.put("url","https://www.baidu.com");
        push_message.put("notification",notification);

        JSONObject push = new JSONObject();
        push.put("request_id",request_id);
        push.put("settings",settings);
        push.put("audience",audience);
        push.put("push_message",push_message);
        JSONObject heads = new JSONObject();
        heads.put("token",getToken());
        HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/single/cid", push, heads);
        System.out.println(HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/single/cid"
                , push, heads));
        System.out.println("推送成功");
    }

    /**
     * 发送logL信息到User-websocket
     * ##param logData	日志信息
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/8/4 15:39
     */
    @Override
    public void sendLogWSU(LogFlow logData) {

        WebSocketUserServer.sendLog(logData);

        //KEV  COW @ close but send message error
        //KEV actually this sending bindingInfo move to WSU
        //https://blog.csdn.net/canot/article/details/52495333

        logUtil.sendLog(logData.getLogType(),logData);

    }

    private String getToken(){
        String appKey = "ShxgT3kg6s73NbuZeAe3I";
        String masterSecret = "0sLuGUOFPG6Hyq0IcN2JR";
        long timestamp = System.currentTimeMillis();

        JSONObject tokenPost = new JSONObject();
        tokenPost.put("sign", RsaUtil.getSHA256Str(appKey+timestamp+masterSecret));
        tokenPost.put("timestamp",timestamp);
        tokenPost.put("appkey",appKey);
        String s = HttpClientUtils.httpPost("https://restapi.getui.com/v2/" + appId + "/auth", tokenPost);
        JSONObject tokenResult = JSON.parseObject(s);
        JSONObject data = tokenResult.getJSONObject("data");
        return data.getString("token");
    }
    @Override
    public ApiResponse setGpio(JSONObject bindingInfo) {
        
        if (bindingGpio(bindingInfo, true)) {
            return retResult.ok(CodeEnum.OK.getCode(), "绑定gpio-请求成功!");
        }
        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_IO_ALREADY_BINDED.getCode(), "gpio已经被绑定");
    }

    @Override
    public ApiResponse unsetGpio(JSONObject bindingInfo) {

        if (bindingGpio(bindingInfo, false)) {
            return retResult.ok(CodeEnum.OK.getCode(), "解绑gpIo-请求成功!");
        }
        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_IO_ALREADY_UNBIND.getCode(), "gpIo已经被解绑");
    }

    private boolean bindingGpio(JSONObject bindingInfo,boolean isBind){
        String cid = bindingInfo.getString("id_C");
        String gpio = bindingInfo.getString("gpio");
        String rname = bindingInfo.getString("rname");

        String assetId = coupaUtil.getAssetId(cid, "a-core");
        Asset asset = coupaUtil.getAssetById(assetId, Arrays.asList("rpi"));
        JSONObject rpi = asset.getRpi();
        JSONObject rnames = rpi.getJSONObject("rnames");
        JSONObject r = rnames.getJSONObject(rname);
        int type = r.getInteger(gpio);
        if (isBind) {
            if (type == 1) {
                return false;
            }
            r.put(gpio,1);
        } else {
            if (type == 0) {
                return false;
            }
            r.put(gpio,0);
        }
        rnames.put(rname,r);
        rpi.put("rnames",rnames);
        // 定义存储flowControl字典
        JSONObject mapKey = new JSONObject();
        // 设置字段数据
        mapKey.put("rpi",rpi);
        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
        LogFlow logFlow = LogFlow.getInstance();
        logFlow.setId_C(cid);
        logFlow.setId_U(bindingInfo.getString("id_U"));
        logFlow.setId(rname);
        JSONObject data = new JSONObject();
        data.put("gpio",gpio);
        data.put("rname",rname);
        if (isBind) {
            logFlow.setLogType("binding");
            logFlow.setZcndesc("绑定gpIo成功");
            logFlow.setGrpU(bindingInfo.getString("grpU"));
            logFlow.setIndex(bindingInfo.getInteger("oIndex"));
            logFlow.setWrdNU(bindingInfo.getJSONObject("wrdNU"));
            logFlow.setImp(bindingInfo.getInteger("imp"));
            logFlow.setSubType("binding");
            logFlow.setId_O(bindingInfo.getString("id_O"));
            logFlow.setTzone(bindingInfo.getInteger("tzone"));
            logFlow.setLang(bindingInfo.getString("lang"));
            logFlow.setId_P(bindingInfo.getString("id_P"));
            data.put("pic",bindingInfo.getString("pic"));
            data.put("wn2qtynow",bindingInfo.getInteger("wn2qtynow"));
            data.put("grpB",bindingInfo.getString("grpB"));
            data.put("fields",bindingInfo.getJSONObject("fields"));
            data.put("wrdNP",bindingInfo.getJSONObject("wrdNP"));
            data.put("wrdN",bindingInfo.getJSONObject("wrdN"));
        } else {
            logFlow.setLogType("unbound");
            logFlow.setZcndesc("解绑gpIo成功");
        }
        logFlow.setData(data);
        WebSocketServerPi.sendInfo(logFlow);
        return true;
    }

//    @Override
//    public ApiResponse getIdCAndRpI() {
//
//        return retResult.ok(CodeEnum.OK.getCode(), "请求成功!");
//    }

    /**
     * 推送
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 15:01
     */
//    @Override
//    public ApiResponse sendPush() {
//
////        String pushSingle = sendPushSingle("46e9effd76c96e2dafc72bcf3ad72a03","123","gggg");
//        String pushSingle = sendPushSingle("46e9effd76c96e2dafc72bcf3ad72a03","123","gggg");
//        System.out.println(pushSingle);
//
////        String taskId = getTaskId();
////        System.out.println("taskId:"+taskId);
//
////        String pushList = sendPushList();
////        System.out.println(pushList);
//
//        return retResult.ok(CodeEnum.OK.getCode(), "推送成功");
//    }

//    private String sendPushList(){
//        JSONArray cid = new JSONArray();
//        cid.add("46e9effd76c96e2dafc72bcf3ad72a03");
//        cid.add("f7bcf244c0b94c97b4d233b6a1996f46");
//        JSONObject audience = new JSONObject();
//        audience.put("cid",cid);
//        JSONObject push = new JSONObject();
//        push.put("audience",audience);
//        push.put("is_async","false");
//        push.put("taskid","RASL_0628_cdcee845f9c34c678127152863a21f0a");
//        JSONObject heads = new JSONObject();
//        heads.put("token",getToken());
//        return HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/cid"
//                ,push,heads);
//    }

//    private String sendPushSingle(String clientId,String title,String body){
//        String request_id = MongoUtils.GetObjectId();
//        JSONObject settings = new JSONObject();
//        int ttl = 3600000;
//        settings.put("ttl",ttl);
//        JSONObject audience = new JSONObject();
//        JSONArray cid = new JSONArray();
////        cid.add("46e9effd76c96e2dafc72bcf3ad72a03");
////        cid.add("f7bcf244c0b94c97b4d233b6a1996f46");
//        cid.add(clientId);
//        audience.put("cid",cid);
//        JSONObject push_message = new JSONObject();
//        JSONObject notification = new JSONObject();
//        notification.put("title",title);
//        notification.put("body",body);
//        notification.put("click_type","startapp");
//        notification.put("url","https://www.baidu.com");
//        push_message.put("notification",notification);
//
//        JSONObject push = new JSONObject();
//        push.put("request_id",request_id);
//        push.put("settings",settings);
//        push.put("audience",audience);
//        push.put("push_message",push_message);
//        JSONObject heads = new JSONObject();
//        heads.put("token",getToken());
//        return HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/single/cid"
//                , push, heads);
//    }



//    private String getTaskId(){
//        String request_id = MongoUtils.GetObjectId();
//        String group_name = "任务名称";
//        JSONObject settings = new JSONObject();
//        int ttl = 3600000;
//        settings.put("ttl",ttl);
//        JSONObject push_message = new JSONObject();
//        JSONObject notification = new JSONObject();
//        notification.put("title","唐先生");
//        notification.put("body","天晴了雨停了");
//        notification.put("click_type","url");
//        notification.put("url","https://www.baidu.com");
//        push_message.put("notification",notification);
//
//        JSONObject push = new JSONObject();
//        push.put("request_id",request_id);
//        push.put("group_name",group_name);
//        push.put("settings",settings);
//        push.put("push_message",push_message);
//        JSONObject heads = new JSONObject();
//        heads.put("token",getToken());
//        return HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/message"
//                , push, heads);
//    }

}
