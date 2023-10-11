package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.encrypt.HttpClientUtils;
import com.cresign.tools.encrypt.RSAUtils;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.request.HttpClientUtil;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author kevin
 * @ClassName Qt
 * @Description
 * @updated 2022/9/11 10:05 AM
 * @ver 1.0.0
 */
@Service
public class Ws {

    @Autowired
    private Qt qt;
//    public static final String appId = "KVB0qQq0fRArupojoL4WM9";
    @Value("${thisConfig.appId}")
    private String appId;
//    static String url = "https://fc-mp-21012483-e888-468f-852d-4c00bdde7107.next.bspapp.com/push";
    @Value("${thisConfig.url}")
    private String url;
//    private String appKey = "ShxgT3kg6s73NbuZeAe3I";
//    private String masterSecret = "0sLuGUOFPG6Hyq0IcN2JR";
    @Value("${thisConfig.appKey}")
    private String appKey;
    @Value("${thisConfig.masterSecret}")
    private String masterSecret;
    public static final String ws_mq_prefix = "wsl";

    /**
     * 注入RocketMQ模板
     */
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

//    public void testConfig(){
//        System.out.println("输出测试:");
//        System.out.println(appId);
//        System.out.println(url);
//        System.out.println(appKey);
//        System.out.println(masterSecret);
//    }

    /**
     * 直接发送MQ信息方法
     * 1。 receive logFlow only
     * 2. use roket to send ES, Chat, push
     * 3. Push
     * @author tang
     * @date 创建时间: 2023/4/15
     * @ver 版本号: 1.0.0
     */
    public void sendWSOnly(String mqKey,LogFlow log){
        rocketMQTemplate.convertAndSend(mqKey, JSON.toJSONString(log));
        System.out.println("发送WS完成:"+mqKey);
    }

    /**
     * 发送日志
     * @param log	日志信息
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    public void sendWSOnlyNew(LogFlow log){
        // 调用发送日志核心方法
        sendWSCore(log);
    }
//    public void sendWSOnlyXin(String mqKey,LogFlow log){
//        rocketMQTemplate.convertAndSend(mqKey, JSON.toJSONString(log));
//        System.out.println("发送WS-Xin完成:"+mqKey);
//    }

    /**
     * 写入es方法，写入并且清空发送用户列表和appId列表
     * @param log	日志信息
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    public void sendESOnly(LogFlow log){
        log.setId_Us(null);
        log.setId_APPs(null);
        rocketMQTemplate.convertAndSend("chatTopicEs:chatTapEs", JSON.toJSONString(log));
        System.out.println("发送ES完成");
    }

    /**
     * 发送MQ信息给 WS 来群发
     * 发给MQ ES 来 add ES to flow
     * 发Push to cidArray (get from flowControl)
     * @author tang
     * @date 创建时间: 2023/4/15
     * @ver 版本号: 1.0.0
     *  1. id / id_FS || 2. id_Us[], getES(lBUser), id_APP[]
     *  logContrent.getJSONArray("id_Us") / id_APPs[]
     */
    public void sendWS(LogFlow logContent){
        System.out.println("logContent:");
        System.out.println(JSON.toJSONString(logContent));

        // the log's id_Us may have users
        // if so, getES and get id_APP to push
        // else set id_Us + cidArray as usual
        getUserIdsOrAppIds(logContent);
        qt.errPrint("what is going", null, logContent);
        System.out.println("sendWS:");
        System.out.println(JSON.toJSONString(logContent));
        sendWSCore(logContent);
        this.sendESOnly(logContent);
    }

    /**
     * app发送推送方法
     * @param title	推送标题
     * @param content	推送内容
     * @param pushAppIds	推送用户列表
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    public void push2(String title,String content,JSONArray pushAppIds){
        System.out.println("推送用户:");
        System.out.println(JSON.toJSONString(pushAppIds));
        JSONObject map = new JSONObject();
        map.put("cids",pushAppIds);
        map.put("title",title);
        map.put("content",content);
        JSONObject options = new JSONObject();
        JSONObject HW = new JSONObject();
        HW.put("/message/android/category","WORK");
        options.put("HW",HW);
        map.put("options",options);
        JSONObject date = new JSONObject();
        date.put("toPage","/user/info.js");
        date.put("name","张三");
        date.put("desc","这是一个推送data");
        map.put("date",date);
        map.put("request_id", UUID.randomUUID().toString().replace("-",""));
        String s = HttpClientUtil.sendPost(map,url);
        System.out.println("请求结果:");
        System.out.println(s);
    }

    /**
     * 获取es用户id列表和appId列表
     * @param log	日志信息
     * @param id_C	公司编号
     * @param grpU	组别
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    public void setUserListByGrpU(LogFlow log, String id_C, String grpU) {
        JSONArray userList = new JSONArray();
        if (grpU.equals("all"))
        {
            userList = qt.getES("lBUser", qt.setESFilt("id_CB", id_C));
        } else {
            userList = qt.getES("lBUser", qt.setESFilt("id_CB", id_C, "grpU", grpU));
        }
        JSONArray userIds = new JSONArray();
        JSONArray userPushIds = new JSONArray();

        for (int i = 0; i < userList.size(); i++) {
            userIds.add(userList.getJSONObject(i).getString("id_U"));
            userPushIds.add(userList.getJSONObject(i).getString("id_APPs"));
        }
        log.setId_Us(userIds);
        log.setId_APPs(userPushIds);
    }

    /**
     * 获取群用户列表
     * id_Us[] and id_APPs use flowcontrol (prepareUserList)
     * @param id_C	公司编号
     * @param logContent	日志信息
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    public void setUserListByFlowId(String id_C, LogFlow logContent) {
        Asset asset = qt.getConfig(id_C,"a-auth","flowControl");
        if (null == asset || asset.getId().equals("none") || null == asset.getFlowControl()) {
            return;
        }
        if (null == logContent.getId_Us()) {
            logContent.setId_Us(new JSONArray());
        }
        // 获取卡片信息
        JSONObject flowControl = asset.getFlowControl();
        // 获取卡片data信息
        JSONArray flowData = flowControl.getJSONArray("objData");
        // 遍历data
        for (int i = 0; i < flowData.size(); i++) {
            // 获取i对应的data信息
            JSONObject roomSetting = flowData.getJSONObject(i);
            // 获取群id
            String roomId = roomSetting.getString("id");
            // 获取日志群id
            String logFlowId = logContent.getId();
            if (roomSetting.getString("type").endsWith("SL")) {
                logFlowId = logContent.getId_FS();
            }
            // 判断群id一样
            if (roomId.equals(logFlowId)) {
//                System.out.println("进入各FC 的loop: id=" + logContent.getId());
                // 获取群用户id列表
                JSONArray objUser = roomSetting.getJSONArray("objUser");
//                System.out.println("objUser:" + JSON.toJSONString(objUser));
                // 创建存储推送用户信息
                // 遍历群用户id列表
                for (int j = 0; j < objUser.size(); j++) {
                    // 获取j对应的群用户信息
                    JSONObject thisUser = objUser.getJSONObject(j);
                    String id_U = thisUser.getString("id_U");
                    // 获取群用户id
                    logContent.getId_Us().add(id_U);
                }
            }
        }
    }

//    public void prepareUserByGrpU(String id_C,String grpU,LogFlow logContent){
////
//        JSONArray userList = qt.getES("lBUser", qt.setESFilt("id_CB", id_C, "grpU", grpU));
//
//        JSONArray id_Us = new JSONArray();
//        JSONArray id_APPs = new JSONArray();
//
//        for (int i = 0; i < userList.size(); i++) {
//            id_Us.add(userList.getJSONObject(i).getString("id_U"));
//            id_APPs.add(userList.getJSONObject(i).getString("id_APPs"));
//        }
//        logContent.setId_Us(id_Us);
//        logContent.setId_APPs(id_APPs);
//    }
//
//    public void sendWS_grpU(String id_C,String id_U,String noticeType){
//
//            LogFlow log = new LogFlow("usageflow", "BNyYCj2P4j3zBCzSafJz6aei", "", "setAuth",id_U,"1001","", "1000", "1000",
//                    "","",0,id_C,"","https://cresign-1253919880.cos.ap-guangzhou.myqcloud.com/avatar/cresignbot.jpg","", "更新菜单", 3, qt.setJson("cn", "小银【系统】"), qt.setJson("cn", "小银【系统】"));
//            log.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//            JSONArray id_Us = new JSONArray();
//            JSONArray id_APPs = new JSONArray();
////            this.setUserListByFlowId(id_C, log, id_Us, id_APPs );
//            // this is wrong, need to getES lBUser
//            log.getData().put("type", noticeType);
//            log.setId_Us(id_Us);
//            log.setId_APPs(id_APPs);
//            sendWS(log);
//        }
//
//    public void sendWS_SetAuth(String id_C,String id_U,JSONObject data,JSONArray id_Us,JSONArray id_APPs){
//
//        LogFlow log = new LogFlow("usageflow", "BNyYCj2P4j3zBCzSafJz6aei", "", "setMenuAuth",id_U,"1001","", "1000", "1000",
//                "","",0,id_C,"","https://cresign-1253919880.cos.ap-guangzhou.myqcloud.com/avatar/cresignbot.jpg","", "更新菜单", 3, qt.setJson("cn", "小银【系统】"), qt.setJson("cn", "小银【系统】"));
//        log.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
////        JSONArray id_Us = new JSONArray();
////        JSONArray id_APPs = new JSONArray();
//        log.setId_Us(id_Us);
//        log.setId_APPs(id_APPs);
//        log.setData(data);
//        try {
//            rocketMQTemplate.convertAndSend("chatTopic:chatTap", JSON.toJSONString(log));
//            System.out.println("发送WS完成");
//
//            System.out.println("sendWS:");
//            System.out.println(JSON.toJSONString(log));
//
//            log.setId_Us(null);
//            log.setId_APPs(null);
//            System.out.println("发送ES"+log);
//            rocketMQTemplate.convertAndSend("chatTopicEs:chatTapEs", JSON.toJSONString(log));
//            System.out.println("发送ES完成");
//        } catch (Exception e){
//            System.out.println("出现错误:"+e.getMessage());
//            e.printStackTrace();
//        }
////        sendWSNew(log);
//        this.sendWSOnly(log);
//    }
//
//

    public void sendUsageFlow(JSONObject wrdN, String msg, String subType, String type) {
        // set sys log format:
        LogFlow log = new LogFlow();
        log.setSysLog("6141b6797e8ac90760913fd0", subType, msg, 3, wrdN);

        // find all users in the "system usageflow group
        this.setUserListByFlowId("6141b6797e8ac90760913fd0", log);
        if (type.equals("ALL"))
        {   //send WS, write ES, send push
            this.sendWS(log);
        } else if (type.equals("WSES"))
        {  // no Push
            this.sendESOnly(log);
            this.sendWSOnlyNew(log);
        } else if (type.equals("ES"))
        {
            this.sendESOnly(log);
        } else if (type.equals("WS"))
        {
            this.sendWSOnlyNew(log);
        }
    }

    private void sendPushBatch(JSONArray cidArray,String title,String body){
        String s;
        JSONObject heads = new JSONObject();
        String token = this.getPushToken();
        heads.put("token",token);
        JSONObject push;

        String group_name = "任务组名";
        String request_id = qt.GetObjectId();
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
        notification.put("url","https://www.cresign.cn");
        push_message.put("notification",notification);

        push = new JSONObject();
        push.put("request_id",request_id);
        push.put("group_name",group_name);
        push.put("settings",settings);
        push.put("push_message",push_message);

        s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/message", push, heads);

        JSONObject re = JSONObject.parseObject(s);

        if (re != null) {
            String taskid = re.getJSONObject("data").getString("taskid");
            JSONObject audience = new JSONObject();
            audience.put("cid", cidArray);
            push = new JSONObject();
            push.put("audience", audience);
            push.put("taskid", taskid);
            push.put("is_async", true);
            s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/cid", push, heads);
        }

    }

    private String getPushToken(){
        long timestamp = System.currentTimeMillis();

        JSONObject tokenPost = new JSONObject();
        tokenPost.put("sign", RSAUtils.getSHA256Str(appKey+timestamp+masterSecret));
        tokenPost.put("timestamp",timestamp);
        tokenPost.put("appkey",appKey);
        String s = HttpClientUtils.httpPost("https://restapi.getui.com/v2/" + appId + "/auth", tokenPost);
        JSONObject tokenResult = JSON.parseObject(s);
        JSONObject data = tokenResult.getJSONObject("data");
        return data.getString("token");
    }

    /**
     * 获取用户的redis信息
     * @param id_U	用户编号
     * @return 返回结果: {@link JSONObject}
     * @author tang
     * @date 创建时间: 2023/9/2
     * @ver 版本号: 1.0.0
     */
    public JSONObject getMqKey(String id_U){
        JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, id_U);
        if (null != rdInfo && rdInfo.size() > 0) {
            return rdInfo;
        }
        return null;
    }

    /**
     * 发送推送和mq消息方法
     * @param mqGroupId	mq信息
     * @param pushUserObj	推送用户列表
     * @param logContent	日志信息
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    public void sendMqOrPush(JSONObject mqGroupId,JSONObject pushUserObj,LogFlow logContent){
        if (logContent.getImp() >= 3 && pushUserObj.size() > 0) {
            System.out.println("推送用户列表:");
            System.out.println(JSON.toJSONString(pushUserObj.keySet()));
            // 创建存储appId列表
            JSONArray pushApps = new JSONArray();
            // 创建存储padId列表
            JSONArray pushPads = new JSONArray();
            for (String id_U : pushUserObj.keySet()) {
                // 获取redis信息
                JSONObject mqKey = getMqKey(id_U);
                // 存储判断redis有appId
                boolean isApp = false;
                // 存储判断redis有padId
                boolean isPad = false;
                if (null != mqKey) {
                    JSONObject app = mqKey.getJSONObject("app");
                    if (null != app) {
                        pushApps.add(app.getString("appId"));
                        // 设置有
                        isApp = true;
                    }
                    JSONObject pad = mqKey.getJSONObject("pad");
                    if (null != pad) {
                        pushPads.add(pad.getString("appId"));
                        // 设置有
                        isPad = true;
                    }
                    // 判断都有，直接开启下一次循环
                    if (isApp && isPad) {
                        continue;
                    }
                }
                JSONArray es = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
                if (null != es && es.size() > 0 && null != es.getJSONObject(0)) {
                    JSONObject esObj = es.getJSONObject(0);
                    if (!isApp) {
                        if (null != esObj.getString("id_APP") && !"".equals(esObj.getString("id_APP"))) {
                            pushApps.add(esObj.getString("id_APP"));
                        }
                    }
                    if (!isPad) {
                        if (null != esObj.getString("id_Pad") && !"".equals(esObj.getString("id_Pad"))) {
                            pushPads.add(esObj.getString("id_Pad"));
                        }
                    }
                }
            }
            System.out.println("推送app:");
            System.out.println(JSON.toJSONString(pushApps));
            System.out.println("推送pad:");
            System.out.println(JSON.toJSONString(pushPads));
            if (pushApps.size() > 0) {
                String wrdNUC = "小银【系统】";
                JSONObject wrdNU = logContent.getWrdNU();
                if (null != wrdNU && null != wrdNU.getString("cn")) {
                    wrdNUC = wrdNU.getString("cn");
                }
                // 调用app发送推送方法
                push2(wrdNUC,logContent.getZcndesc(),pushApps);
            }
        }
        if (mqGroupId.size() > 0) {
            for (String mqKey : mqGroupId.keySet()) {
                JSONObject mqIdArr = mqGroupId.getJSONObject(mqKey);
                // 获取用户列表
                logContent.setId_Us(JSONArray.parseArray(JSON.toJSONString(mqIdArr.keySet())));
                JSONObject data = logContent.getData();
                if (null == data) {
                    data = new JSONObject();
                }
                data.put("pushUsers",pushUserObj);
                logContent.setData(data);
                // 发送mq信息
                sendWSOnly(mqKey,logContent);
            }
        }
    }

    /**
     * 获取日志发送列表
     * @param logContent	日志信息
     * @author tang
     * @date 创建时间: 2023/9/2
     * @ver 版本号: 1.0.0
     */
    public void getUserIdsOrAppIds(LogFlow logContent){
        // 获取公司编号
        String id_C = logContent.getId_C();
        // 获取供应商编号
        String id_CS = logContent.getId_CS();

        if (logContent.getId_Us() == null) {
            logContent.setId_Us(new JSONArray());
        }
        if (null == logContent.getId_APPs()) {
            logContent.setId_APPs(new JSONArray());
        }
        // fill up id_Us and cidArray (user array info)
        // by FlowControl... and you can do things like, you
        // my comp first then CS comp
        // if id_Us is listed, i should not change that
        if (logContent.getId_Us().size() > 0) {
            if (logContent.getId_APPs().size() == 0) {

                // 遍历用户列表
                for (int i = 0; i < logContent.getId_Us().size(); i++) {
                    // 获取用户ID
                    String id_U = logContent.getId_Us().getString(i);
                    JSONArray es = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
                    if (null != es && es.size() > 0) {
                        JSONObject esInfo = es.getJSONObject(0);
                        // 判断不为空
                        if (null != esInfo && null != esInfo.getString("id_APP")) {
                            logContent.getId_APPs().add(esInfo.getString("id_APP"));
                        } else {
                            logContent.getId_APPs().add("");
                        }
                    }
                }
            }
        } else {
            // get from flowControl
            setUserListByFlowId(id_C, logContent);
            if (id_CS != null && !id_C.equals(id_CS)) {
                setUserListByFlowId(id_CS, logContent);
            }
        }
    }

    /**
     * 发送日志核心方法
     * @param logContent	日志信息
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    private void sendWSCore(LogFlow logContent){
        JSONArray id_Us = logContent.getId_Us();
        // 存储发送mq信息
        JSONObject mqGroupId = new JSONObject();
        // 存储需要推送的用户列表
        JSONObject pushUserObj = new JSONObject();
        for (int i = 0; i < id_Us.size(); i++) {
            String id_U = id_Us.getString(i);
            // 获取redis信息
            JSONObject rdInfo = getMqKey(id_U);
            // 判断redis信息为空
            if (null == rdInfo) {
                // 添加推送
                pushUserObj.put(id_U,0);
                continue;
            }
            // 存储判断不为当前mq，默认为当前mq
            boolean isSendMq = false;
            // 存储是否是app端
            boolean isApp = false;
            for (String cli : rdInfo.keySet()) {
                // 获取端信息
                JSONObject cliInfo = rdInfo.getJSONObject(cli);
                if (null == cliInfo) {
                    // 添加推送
                    pushUserObj.put(id_U,0);
                } else {
                    String mqKey = cliInfo.getString("mqKey");
                    if (null!=mqKey) {
                        // 设置不为当前mq
                        isSendMq = true;
                        if (null == mqGroupId.getJSONObject(mqKey)) {
                            mqGroupId.put(mqKey,new JSONObject());
                        }
                        // 添加mq推送信息
                        JSONObject mqIdAndAppId = mqGroupId.getJSONObject(mqKey);
                        mqIdAndAppId.put(id_U,"");
                        mqGroupId.put(mqKey,mqIdAndAppId);
                    } else {
                        if (cli.equals("app")) {
                            isApp = true;
                        }
                    }
                }
            }
            // 判断不为当前mq，或者是app
            if (!isSendMq || isApp) {
                // 添加推送
                pushUserObj.put(id_U,0);
            }
        }
        // 调用发送推送和mq消息方法
        sendMqOrPush(mqGroupId,pushUserObj,logContent);
    }

}
