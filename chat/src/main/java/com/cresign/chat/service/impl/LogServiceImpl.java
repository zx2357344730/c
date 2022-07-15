package com.cresign.chat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.common.ChatEnum;
import com.cresign.chat.config.websocket.WebSocketServerPi;
import com.cresign.chat.service.LogService;
import com.cresign.chat.utils.HttpClientUtils;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.uuid.UUID19;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * ##author: tangzejin
 * ##updated: 2019/7/16
 * ##version: 1.0.0
 * ##description: 日志实现类
 */
@Service
@Slf4j
public class LogServiceImpl  implements LogService {


    @Autowired
    private RetResult retResult;

    @Autowired
    private CoupaUtil coupaUtil;

    /**
     * 注入redis数据库下标1模板
     */
    @Resource
    private StringRedisTemplate redisTemplate1;

    public static final String appId = "KVB0qQq0fRArupojoL4WM9";

    public static final String HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_RPI_T = "https://www.cresign.cn/qrCodeTest?qrType=rpi&t=";

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
    public void sendPush(String clientId,String title,String body,String token){
        String request_id = MongoUtils.GetObjectId();
        JSONObject settings = new JSONObject();
        int ttl = 3600000;
        settings.put("ttl",ttl);
        JSONObject strategy = new JSONObject();
        strategy.put("default",4);
        settings.put("strategy",strategy);
        JSONObject audience = new JSONObject();
        JSONArray cid = new JSONArray();
        cid.add(clientId);
        audience.put("cid",cid);
        JSONObject push_message = new JSONObject();
        JSONObject notification = new JSONObject();
        notification.put("title",title+"_toD");
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
        heads.put("token",token);
        String s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/single/cid", push, heads);
        log.info("单推推送结果:");
        log.info(s);
//        String s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/single/batch/cid", push, heads);
//        System.out.println(HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/single/cid", push, heads));
//        System.out.println(s);

//        getTestToListPush("聊天信息","...");
//        System.out.println("推送成功");
    }

    @Override
    public void sendPushBatch(JSONArray cidArray,String title,String body){
        String s;
        JSONObject heads = new JSONObject();
        String token = this.getToken();
        heads.put("token",token);
        JSONObject push;


        String group_name = "任务组名";
        String request_id = MongoUtils.GetObjectId();
        JSONObject settings = new JSONObject();
        int ttl = 3600000;
        settings.put("ttl",ttl);
        JSONObject strategy = new JSONObject();
        strategy.put("default",4);
        settings.put("strategy",strategy);
        JSONObject push_message = new JSONObject();
        JSONObject notification = new JSONObject();
        notification.put("title",title+"_toLP");
        notification.put("body",body);
        notification.put("click_type","startapp");
        notification.put("url","https://www.baidu.com");
        push_message.put("notification",notification);
        push = new JSONObject();
        push.put("request_id",request_id);
        push.put("group_name",group_name);
        push.put("settings",settings);
        push.put("push_message",push_message);
        s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/message", push, heads);
//        System.out.println("创建message返回结果:");
//        System.out.println(s);
//        log.info("创建message返回结果:");
//        log.info(s);


        JSONObject re = JSONObject.parseObject(s);
        String taskid = re.getJSONObject("data").getString("taskid");
        JSONObject audience = new JSONObject();
        audience.put("cid",cidArray);
        push = new JSONObject();
        push.put("audience",audience);
        push.put("taskid",taskid);
        push.put("is_async",true);
        s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/cid", push, heads);
//        System.out.println("批量推送创建的message返回结果:");
//        System.out.println(s);
//        log.info("批量推送创建的message返回结果:");
//        log.info(s);
    }

    @Override
    public ApiResponse requestRpi(JSONObject can) {
//        String token = can.getString("token");
//        String s = redisTemplate1.opsForValue().get(token);
//        JSONObject redJ = JSON.parseObject(s);
//        boolean isBinding = redJ.getBoolean("isBinding");
//        String id_U = can.getString("id_U");
//        if (!isBinding) {
//            updateRedJ(redJ,id_U, can.getString("id_C"), can.getString("grpU")
//                    , can.getInteger("oIndex"), can.getJSONObject("wrdNU"), can.getInteger("imp")
//                    , can.getString("id_O"), can.getInteger("tzone"), can.getString("lang")
//                    , can.getString("id_P"), can.getString("pic"), can.getInteger("wn2qtynow")
//                    , can.getString("grpB"), can.getJSONObject("fields"), can.getJSONObject("wrdNP")
//                    , can.getJSONObject("wrdN"), true);
//            redisTemplate1.opsForValue().set(token,JSON.toJSONString(redJ));
//            return retResult.ok(CodeEnum.OK.getCode(), "绑定gpIo成功");
//        } else {
//            if (redJ.getString("id_U").equals(id_U)) {
//                updateRedJ(redJ,"", "", "", 0, new JSONObject(), 0, ""
//                        , 0, "", "", "", 0, "", new JSONObject()
//                        , new JSONObject(), new JSONObject(), false);
//                return retResult.ok(CodeEnum.OK.getCode(), "解除绑定gpIo成功");
//            } else {
//                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_IO_ALREADY_BINDED.getCode(), "gpio已经被绑定");
//            }
//        }
        return null;
    }

    @Override
    public ApiResponse rpiCode(JSONObject can) {
        JSONObject redisJ = new JSONObject();
        String rname = can.getString("rname");
        String gpio = can.getString("gpio");
        String id_C = can.getString("id_C");
//        System.out.println("id_C:"+id_C);
        redisJ.put("rname",rname);
        redisJ.put("gpio",gpio);
        updateRedJ(redisJ,"", "", "", 0, new JSONObject(), 0, ""
                , 0, "", "", "", 0, "", new JSONObject()
                , new JSONObject(), new JSONObject(), false);
//        redisJ.put("id_U",can.getString("id_U"));
//        redisJ.put("id_C",can.getString("id_C"));
        String token = "rpi_"+UUID19.uuid();
        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_RPI_T + token;
        String assetId = coupaUtil.getAssetId(id_C, "a-core");
//        System.out.println("assetId:"+assetId);
        if (null == assetId) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_ASSET_ID.getCode(), "该公司没有assetId");
        }
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("rpi"));
        if (null == asset) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_ASSET.getCode(), "该公司没有asset");
        }
//        System.out.println(JSON.toJSONString(asset));
        JSONObject rpi = asset.getRpi();
        if (null == rpi) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_K.getCode(), "该公司rpi卡片异常");
        }
        JSONObject rnames = rpi.getJSONObject("rnames");
        if (null == rnames) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_K.getCode(), "该公司rpi卡片异常");
        }
        JSONObject r = rnames.getJSONObject(rname);
        if (null == r) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_K.getCode(), "该公司rpi卡片异常");
        }
        r.put(gpio,token);
        rnames.put(rname,r);
        rpi.put("rnames",rnames);
        // 定义存储flowControl字典
        JSONObject mapKey = new JSONObject();
        // 设置字段数据
        mapKey.put("rpi",rpi);
        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
        redisTemplate1.opsForValue().set(token,JSON.toJSONString(redisJ));
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }

    @Override
    public ApiResponse requestRpiStatus(JSONObject can) {
        String token = can.getString("token");
        String s = redisTemplate1.opsForValue().get(token);
        if (null == s) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_T_DATA_NO.getCode(), "rpi的token数据不存在");
        }
        JSONObject redJ = JSON.parseObject(s);
        boolean isBinding = redJ.getBoolean("isBinding");
        String id_U = can.getString("id_U");
        if (!isBinding) {
            return retResult.ok(CodeEnum.OK.getCode(), "1");
        } else {
            if (redJ.getString("id_U").equals(id_U)) {
                return retResult.ok(CodeEnum.OK.getCode(), "2");
            } else {
                return retResult.ok(CodeEnum.OK.getCode(), "3");
            }
        }
    }

    @Override
    public ApiResponse bindingRpi(JSONObject can) {
        String token = can.getString("token");
        String s = redisTemplate1.opsForValue().get(token);
        if (null == s) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_T_DATA_NO.getCode(), "rpi的token数据不存在");
        }
        JSONObject redJ = JSON.parseObject(s);
        String id_U = can.getString("id_U");
        String rname = redJ.getString("rname");
        String gpio = redJ.getString("gpio");

        String id_C = can.getString("id_C");
        String grpU = can.getString("grpU");
        Integer oIndex = can.getInteger("oIndex");
        JSONObject wrdNU = can.getJSONObject("wrdNU");
        Integer imp = can.getInteger("imp");
        String id_O = can.getString("id_O");
        Integer tzone = can.getInteger("tzone");
        String lang = can.getString("lang");
        String id_P = can.getString("id_P");
        String pic = can.getString("pic");
        Integer wn2qtynow = can.getInteger("wn2qtynow");
        String grpB = can.getString("grpB");
        JSONObject fields = can.getJSONObject("fields");
        JSONObject wrdNP = can.getJSONObject("wrdNP");
        JSONObject wrdN = can.getJSONObject("wrdN");
        updateRedJ(redJ,id_U, id_C, grpU, oIndex, wrdNU, imp, id_O, tzone, lang
                , id_P, pic, wn2qtynow, grpB, fields, wrdNP, wrdN, true);
        LogFlow logFlow = getLogF(id_C,id_U,rname);
        logFlow.setLogType("binding");
        logFlow.setZcndesc("绑定gpIo成功");
        logFlow.setGrpU(grpU);
        logFlow.setIndex(oIndex);
        logFlow.setWrdNU(wrdNU);
        logFlow.setImp(imp);
        logFlow.setSubType("binding");
        logFlow.setId_O(id_O);
        logFlow.setTzone(tzone);
        logFlow.setLang(lang);
        logFlow.setId_P(id_P);
        JSONObject data = new JSONObject();
        data.put("gpio",gpio);
        data.put("rname",rname);
        data.put("pic",pic);
        data.put("wn2qtynow",wn2qtynow);
        data.put("grpB",grpB);
        data.put("fields",fields);
        data.put("wrdNP",wrdNP);
        data.put("wrdN",wrdN);
        logFlow.setData(data);
        WebSocketServerPi.sendInfo(logFlow);
        System.out.println("发送消息:");
        System.out.println(JSON.toJSONString(logFlow));
        redisTemplate1.opsForValue().set(token,JSON.toJSONString(redJ));
        return retResult.ok(CodeEnum.OK.getCode(), "绑定gpIo成功");
    }

    @Override
    public ApiResponse relieveRpi(JSONObject can) {
        String token = can.getString("token");
        String id_C = can.getString("id_C");
        String s = redisTemplate1.opsForValue().get(token);
        if (null == s) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_T_DATA_NO.getCode(), "rpi的token数据不存在");
        }
        JSONObject redJ = JSON.parseObject(s);
        String rname = redJ.getString("rname");
        String gpio = redJ.getString("gpio");
        updateRedJ(redJ,"", "", "", 0, new JSONObject(), 0, ""
                , 0, "", "", "", 0, "", new JSONObject()
                , new JSONObject(), new JSONObject(), false);
        LogFlow logFlow = getLogF(id_C, can.getString("id_U"),rname);
        JSONObject data = new JSONObject();
        data.put("gpio",gpio);
        data.put("rname",rname);
        logFlow.setLogType("unbound");
        logFlow.setZcndesc("解绑gpIo成功");
        logFlow.setData(data);
        WebSocketServerPi.sendInfo(logFlow);
        System.out.println("发送消息:");
        System.out.println(JSON.toJSONString(logFlow));
        redisTemplate1.opsForValue().set(token,JSON.toJSONString(redJ));
        return retResult.ok(CodeEnum.OK.getCode(), "解除绑定gpIo成功");
    }

    private void updateRedJ(JSONObject redJ,String id_U,String id_C,String grpU,Integer oIndex,JSONObject wrdNU
            ,Integer imp,String id_O,Integer tzone,String lang,String id_P,String pic,Integer wn2qtynow
            ,String grpB,JSONObject fields,JSONObject wrdNP,JSONObject wrdN,Boolean isBinding){
        redJ.put("id_U",id_U);
        redJ.put("id_C",id_C);
        redJ.put("grpU",grpU);
        redJ.put("oIndex",oIndex);
        redJ.put("wrdNU",wrdNU);
        redJ.put("imp",imp);
        redJ.put("id_O",id_O);
        redJ.put("tzone",tzone);
        redJ.put("lang", lang);
        redJ.put("id_P", id_P);
        redJ.put("pic", pic);
        redJ.put("wn2qtynow", wn2qtynow);
        redJ.put("grpB", grpB);
        redJ.put("fields", fields);
        redJ.put("wrdNP", wrdNP);
        redJ.put("wrdN", wrdN);
        redJ.put("isBinding",isBinding);
    }

    private LogFlow getLogF(String id_C,String id_U,String rname){
        LogFlow logFlow = LogFlow.getInstance();
        logFlow.setId_C(id_C);
        logFlow.setId_U(id_U);
        logFlow.setId(rname);
        return logFlow;
    }

    @Override
    public String getToken(){
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
        System.out.println("输出:");
        System.out.println(cid);
        System.out.println(assetId);
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("rpi"));
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
