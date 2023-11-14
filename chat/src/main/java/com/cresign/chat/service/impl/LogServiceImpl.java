package com.cresign.chat.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.client.DetailsClient;
import com.cresign.chat.common.ChatEnum;
import com.cresign.chat.config.websocket.WebSocketServerPi;
import com.cresign.chat.service.LogService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author tangzejin
 * @updated 2019/7/16
 * @ver 1.0.0
 * ##description: 日志实现类
 */
@Service
public class LogServiceImpl  implements LogService {


    @Autowired
    private RetResult retResult;

    @Autowired
    private Qt qt;

    @Autowired
    private DetailsClient detailsClient;

//    public static final String appId = "KVB0qQq0fRArupojoL4WM9";
    @Value("${thisConfig.appId}")
    private String appId;
    @Value("${thisConfig.appKey}")
    private String appKey;
    @Value("${thisConfig.masterSecret}")
    private String masterSecret;
    @Override
    public int getDet(JSONObject reqJson) {

        return detailsClient.updateOStockPi(reqJson);
    }

    /**
     * 推送
     * @param clientId	推送id
     * @param title	推送标题
     * @param body	推送内容
     * @author tang
     * @ver 1.0.0
     * @date 2021/7/12 14:47
     */
//    @Override
//    public void sendPush(String clientId,String title,String body,String token){
//        String request_id = qt.GetObjectId();
//        JSONObject settings = new JSONObject();
//        int ttl = 3600000;
//        settings.put("ttl",ttl);
//        JSONObject strategy = new JSONObject();
//        strategy.put("default",4);
//        settings.put("strategy",strategy);
//        JSONObject audience = new JSONObject();
//        JSONArray cid = new JSONArray();
//        cid.add(clientId);
//        audience.put("cid",cid);
//        JSONObject push_message = new JSONObject();
//        JSONObject notification = new JSONObject();
//        notification.put("title",title+"_toD");
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
//        heads.put("token",token);
//        String s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/single/cid", push, heads);
////        log.info("单推推送结果:");
////        log.info(s);
////        String s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/single/batch/cid", push, heads);
////        System.out.println(HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/single/cid", push, heads));
////        System.out.println(s);
//
////        getTestToListPush("聊天信息","...");
////        System.out.println("推送成功");
//    }

//    @Override
//    public void sendPushBatch(JSONArray cidArray,String title,String body){
//        String s;
//        JSONObject heads = new JSONObject();
//        String token = this.getToken();
//        heads.put("token",token);
//        JSONObject push;
//
//
//        String group_name = "任务组名";
//        String request_id = qt.GetObjectId();
//        JSONObject settings = new JSONObject();
//        int ttl = 3600000;
//        settings.put("ttl",ttl);
//        JSONObject strategy = new JSONObject();
//        strategy.put("default",4);
//        settings.put("strategy",strategy);
//        JSONObject push_message = new JSONObject();
//        JSONObject notification = new JSONObject();
//        notification.put("title",title+"_toLP");
//        notification.put("body",body);
//        notification.put("click_type","startapp");
//        notification.put("url","https://www.cresign.cn");
//        push_message.put("notification",notification);
//        push = new JSONObject();
//        push.put("request_id",request_id);
//        push.put("group_name",group_name);
//        push.put("settings",settings);
//        push.put("push_message",push_message);
//        s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/message", push, heads);
//
//
//        JSONObject re = JSONObject.parseObject(s);
//        String taskid = re.getJSONObject("data").getString("taskid");
//        JSONObject audience = new JSONObject();
//        audience.put("cid",cidArray);
//        push = new JSONObject();
//        push.put("audience",audience);
//        push.put("taskid",taskid);
//        push.put("is_async",true);
//        s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/cid", push, heads);
//
//    }

//    @Override
//    public String getToken(){
//
//        long timestamp = System.currentTimeMillis();
//
//        JSONObject tokenPost = new JSONObject();
//        tokenPost.put("sign", RsaUtil.getSHA256Str(appKey+timestamp+masterSecret));
//        tokenPost.put("timestamp",timestamp);
//        tokenPost.put("appkey",appKey);
//        String s = HttpClientUtils.httpPost("https://restapi.getui.com/v2/" + appId + "/auth", tokenPost);
//        JSONObject tokenResult = JSON.parseObject(s);
//        JSONObject data = tokenResult.getJSONObject("data");
//        return data.getString("token");
//    }
    
    
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

        Asset asset = qt.getConfig(cid,"a-core","rpi");


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
        qt.setMDContent(asset.getId(),mapKey,Asset.class);

//        coupaUtil.updateAssetByKeyAndListKeyVal("id",asset.getId(),mapKey);
        LogFlow logFlow = LogFlow.getInstance();
        logFlow.setId_C(cid);
        logFlow.setId_U(bindingInfo.getString("id_U"));
        logFlow.setId(rname);
        JSONObject data = new JSONObject();
        data.put("gpio",gpio);
        data.put("rname",rname);
        if (isBind) {
            //TODO ZJ 要用 new logFlow 来做， 在pojo 加个 setLogData_rpi
            logFlow.setLogType("binding");
            logFlow.setZcndesc("绑定gpIo成功");
            logFlow.setGrpU(bindingInfo.getString("grpU"));
            //TODO ZJ oIndex 不存在了， 改为 index
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
     * @ver 1.0.0
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
//        String request_id = qt.GetObjectId();
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
//        String request_id = qt.GetObjectId();
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
