package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    @Value("${thisConfig.appId}")
    private String appId;

    @Value("${thisConfig.url}")
    private String url;
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

    @Autowired
    private QtThread qtThread;



    /**
     * 直接发送MQ信息方法
     * 1。 receive logFlow only
     * 2. use roket to send ES, Chat, push
     * 3. Push
     * @author tang
     * @date 创建时间: 2023/4/15
     * @ver 版本号: 1.0.0
     */
    public void sendMQ(String mqKey,LogFlow log){
        try {
            rocketMQTemplate.convertAndSend(mqKey, JSON.toJSONString(log));
        } catch (Exception e) {
            System.out.print("sendMQ Error");
        }
    }

    /**
     * 发送日志
     * @param log	日志信息
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
//    public void sendWSOnlyNew(LogFlow log){
//        // 调用发送日志核心方法
//        sendWSCore(log);
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
        try {
        rocketMQTemplate.convertAndSend("chatTopicEs:chatTapEs", JSON.toJSONString(log));
        } catch (Exception e) {
            System.out.print("sendMQ Error");
        }
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

    @Async
    public void sendWS(LogFlow logContent){
        this.setId_UsByFC(logContent);
        this.setAppIds(logContent);
        qt.errPrint("sending WS", logContent);
        this.sendWSCore(logContent);
        this.sendESOnly(logContent);
    }

    @Async
    public void sendWSDirect(LogFlow logContent){
        qt.errPrint("sending WS direct", logContent);

        this.setAppIds(logContent);
        this.sendWSCore(logContent);
        this.sendESOnly(logContent);
    }

//    /**
//     * app发送推送方法
//     * @param title	推送标题
//     * @param content	推送内容
//     * @param pushAppIds	推送用户列表
//     * @author tang
//     * @date 创建时间: 2023/9/4
//     * @ver 版本号: 1.0.0
//     */
//    public void push2(String title,String content,JSONArray pushAppIds){
//
//        JSONObject map = new JSONObject();
//        map.put("cids",pushAppIds);
//        map.put("title",title);
//        map.put("content",content);
//        JSONObject options = new JSONObject();
//        JSONObject HW = new JSONObject();
//        HW.put("/message/android/category","WORK");
//        options.put("HW",HW);
//        map.put("options",options);
//        JSONObject date = new JSONObject();
//        date.put("toPage","/user/info.js");
//        date.put("name","张三");
//        date.put("desc","这是一个推送data");
//        map.put("date",date);
//        map.put("request_id", UUID.randomUUID().toString().replace("-",""));
//        String s = HttpClientUtil.sendPost(map,url);
//    }

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
//        JSONArray userPushIds = new JSONArray();

        for (int i = 0; i < userList.size(); i++) {
            userIds.add(userList.getJSONObject(i).getString("id_U"));
//            userPushIds.add(userList.getJSONObject(i).getString("id_APPs"));
        }
        log.setId_Us(userIds);
        this.setId_UsByFC(log);
        this.setAppIds(log); //统一用这个拿APPID
//        log.setId_APPs(userPushIds);
    }

    /**
     * 获取日志发送列表
     * @param logContent	日志信息
     * @author tang
     * @date 创建时间: 2023/9/2
     * @ver 版本号: 1.0.0
     */
    public void setAppIds(LogFlow logContent){

        if (null == logContent.getId_Us()) {
            logContent.setId_Us(new JSONArray());
        }
        if (null == logContent.getId_APPs()) {
            logContent.setId_APPs(new JSONArray());
        }

        if (logContent.getId_Us().size() > 0) {
            if (logContent.getId_APPs().size() == 0) {
                JSONArray lnUser = qt.getES("lNUser", qt.setESFilt("id_U", "contain", logContent.getId_Us()));
                JSONObject esAll = qt.arr2Obj(lnUser,"id_U");
                // 遍历用户列表
                for (int i = 0; i < logContent.getId_Us().size(); i++) {
                    // 获取用户ID
                    String id_U = logContent.getId_Us().getString(i);
//                    JSONArray es = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
//                    if (null != es && es.size() > 0) {
                    JSONObject esInfo = esAll.getJSONObject(id_U); //es.getJSONObject(0);
                    // 判断不为空
                    if (null != esInfo && null != esInfo.getString("id_APP")) {
                        logContent.getId_APPs().add(esInfo.getString("id_APP"));
                    } else {
                        logContent.getId_APPs().add("");
                    }
//                    }
                }
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
    public void setId_UsByFC(LogFlow logContent){
        // 获取公司编号
        String id_C = logContent.getId_C();
        // 获取供应商编号
        String id_CS = logContent.getId_CS();

        if (null == logContent.getId_Us()) {
            logContent.setId_Us(new JSONArray());
        }
        if (null == logContent.getId_APPs()) {
            logContent.setId_APPs(new JSONArray());
        }

        // fill up id_Us and cidArray (user array info)
        // by FlowControl... and you can do things like, you
        // my comp first then CS comp
        // if id_Us is listed, i should not change that
        // get from flowControl
        this.setUserListByFlowControl(id_C, logContent);
        if (id_CS != null && !id_C.equals(id_CS)) {
            setUserListByFlowControl(id_CS, logContent);
        }

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
    public void setUserListByFlowControl(String id_C, LogFlow logContent) {
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
                // 获取群用户id列表
                JSONArray objUser = roomSetting.getJSONArray("objUser");
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

    public void sendErrorToUsageflow(JSONObject wrdN, String msg, String subType, String type) {
        // set sys log format:
        LogFlow log = new LogFlow();
        log.setSysLog("6141b6797e8ac90760913fd0", subType, msg, 3, wrdN);

        // find all users in the "system usageflow group
        this.setUserListByFlowControl("6141b6797e8ac90760913fd0", log);
        if (type.equals("ALL"))
        {   //send WS, write ES, send push
            this.sendWS(log);
        }
        else if (type.equals("ES"))
        {
            this.sendESOnly(log);
        } else if (type.equals("WS"))
        {
            this.setId_UsByFC(log);
            this.setAppIds(log);
            this.sendWSCore(log);
        } else if (type.equals("WSXP")) // noPush
        {
            this.sendWSCore(log); //TODO ZJ 如果我不想PUSH? 只要不拿AppId 就可以了?
        }
    }

//    private void sendPushBatch(JSONArray cidArray,String title,String body){
//        String s;
//        JSONObject heads = new JSONObject();
//        String token = this.getPushToken();
//        heads.put("token",token);
//        JSONObject push;
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
//
//        push = new JSONObject();
//        push.put("request_id",request_id);
//        push.put("group_name",group_name);
//        push.put("settings",settings);
//        push.put("push_message",push_message);
//
//        s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/message", push, heads);
//
//        JSONObject re = JSONObject.parseObject(s);
//
//        if (re != null) {
//            String taskid = re.getJSONObject("data").getString("taskid");
//            JSONObject audience = new JSONObject();
//            audience.put("cid", cidArray);
//            push = new JSONObject();
//            push.put("audience", audience);
//            push.put("taskid", taskid);
//            push.put("is_async", true);
//            s = HttpClientUtils.httpPostAndHead("https://restapi.getui.com/v2/" + appId + "/push/list/cid", push, heads);
//        }
//
//    }

    /**
     * 发送推送和mq消息方法
     * @param mqGroupId	mq信息
     * @param id_Us	推送用户列表
     * @param logContent	日志信息
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     * MQ, WS, JAVA - app web wx - push ES WS
     * JAVA 70  - getRD (30MQ /40 PUSH)+ (ES)
     * WS -100 getRD (WS)+(ES) :::::70其他 -> JAVA
     * MQ - 30 - 30 WS + ES
     */
    public void sendMqOrPush(JSONObject mqGroupId,JSONArray id_Us,LogFlow logContent){
        if (logContent.getImp() > 3 && id_Us.size() > 0)
        {

            // 创建存储appId列表
            JSONArray pushApps = new JSONArray();
            // 创建存储padId列表
            JSONArray pushPads = new JSONArray();

            JSONArray arraylnuser = qt.getES("lNUser", qt.setESFilt("id_U", "contain", id_Us));

            JSONObject esAll = qt.arr2Obj(arraylnuser,"id_U");
//            for (int i = 0; i < arraylnuser.size(); i++) {
//                esAll.put(arraylnuser.getJSONObject(i).getString("id_U"), arraylnuser.getJSONObject(i));
//            }

            for (int i = 0; i < id_Us.size(); i++) {
                String id_U = id_Us.getString(i);
                // 获取redis信息
                JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, id_U);
                // 存储判断redis有appId
                boolean isApp = false;
                // 存储判断redis有padId
                boolean isPad = false;
                if (null != rdInfo) {
                    JSONObject app = rdInfo.getJSONObject("app");
                    if (null != app) {
                        pushApps.add(app.getString("appId"));
                        // 设置有
                        isApp = true;
                    }
                    JSONObject pad = rdInfo.getJSONObject("pad");
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
                JSONObject esObj = esAll.getJSONObject(id_U);
//                JSONArray es = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
                if (null != esObj) {
//                    JSONObject esObj = es.getJSONObject(0);
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

            if (pushApps.size() > 0) {
                String wrdNUC = "小银【系统】";
                JSONObject wrdNU = logContent.getWrdNU();
                if (null != wrdNU && null != wrdNU.getString("cn")) {
                    wrdNUC = wrdNU.getString("cn");
                }
                // 调用app发送推送方法
                qtThread.push2(wrdNUC,logContent.getZcndesc(),pushApps);
            }
            if (pushPads.size() > 0) {
                qt.errPrint("pushPads:",pushPads);
            }
        }
        if (null != mqGroupId && mqGroupId.size() > 0) {
            for (String mqKey : mqGroupId.keySet()) {
                JSONObject mqIdArr = mqGroupId.getJSONObject(mqKey);
                // 获取用户列表
                logContent.setId_Us(JSONArray.parseArray(JSON.toJSONString(mqIdArr.keySet())));
                JSONObject data = logContent.getData();
                if (null == data) {
                    data = new JSONObject();
                }
                data.put("pushUsers",id_Us);
                logContent.setData(data);
                // 发送mq信息
                sendMQ(mqKey,logContent);
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
    //TODO ZJ: 只可能是Java 发的: 1.rdInfo => MQ 2.rdInfo! == push
    public void sendWSCore(LogFlow logContent){
        JSONArray id_Us = logContent.getId_Us();
        // 存储发送mq信息
        JSONObject mqGroupId = new JSONObject();
        // 存储需要推送的用户列表
        JSONArray pushUserObj = new JSONArray();
        for (int i = 0; i < id_Us.size(); i++) {
            String id_U = id_Us.getString(i);
            // 获取redis信息
            JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, id_U);
            // 判断redis信息为空
            if (null == rdInfo) {
                // 添加推送
                pushUserObj.add(id_U);
                continue;
            }
            for (String cli : rdInfo.keySet()) {
                // 获取端信息
                JSONObject cliInfo = rdInfo.getJSONObject(cli);
                if (null == cliInfo || null == cliInfo.getJSONObject("wsData")) {
                    if (cli.equals("app")) {
                        // 添加推送
                        pushUserObj.add(id_U);
                    }
                } else {
                    JSONObject wsData = cliInfo.getJSONObject("wsData");
                    if (null!=wsData.getString("mqKey")) {
                        String mqKey = wsData.getString("mqKey");
                        if (null == mqGroupId.getJSONObject(mqKey)) {
                            mqGroupId.put(mqKey,new JSONObject());
                        }
                        // 添加mq推送信息
                        JSONObject mqIdAndAppId = mqGroupId.getJSONObject(mqKey);
                        mqIdAndAppId.put(id_U,"");
                        mqGroupId.put(mqKey,mqIdAndAppId);
                    } else {
                        if (cli.equals("app")) {
                            // 添加推送
                            pushUserObj.add(id_U);
                        }
                    }
                }
            }
        }
        // 调用发送推送和mq消息方法
        sendMqOrPush(mqGroupId,pushUserObj,logContent);
    }

    /**
     * 发送日志核心方法
     * @param logContent	日志信息
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    //TODO ZJ: 只可能是Java 发的: 1.rdInfo => MQ 2.rdInfo! == push
    public void sendWSCore(LogFlow logContent,JSONObject rdInfoMap,JSONArray id_UsWei,String mqKeyOld){
        for (int i = 0; i < id_UsWei.size(); i++) {
            String id_U = id_UsWei.getString(i);
            JSONObject rdInfoUser = qt.getRDSet(Ws.ws_mq_prefix, id_U);
            rdInfoMap.put(id_U, rdInfoUser);
        }
        JSONArray id_Us = logContent.getId_Us();
        // 存储发送mq信息
        JSONObject mqGroupId = new JSONObject();
        // 存储需要推送的用户列表
        JSONArray pushUserObj = new JSONArray();
        for (int i = 0; i < id_Us.size(); i++) {
            String id_U = id_Us.getString(i);
            // 获取redis信息
            JSONObject rdInfo = rdInfoMap.getJSONObject(id_U);
            // 判断redis信息为空
            if (null == rdInfo) {
                // 添加推送
                pushUserObj.add(id_U);
                continue;
            }
            for (String cli : rdInfo.keySet()) {
                // 获取端信息
                JSONObject cliInfo = rdInfo.getJSONObject(cli);
                if (null == cliInfo || null == cliInfo.getJSONObject("wsData")) {
                    if (cli.equals("app")) {
                        // 添加推送
                        pushUserObj.add(id_U);
                    }
                } else {
                    JSONObject wsData = cliInfo.getJSONObject("wsData");
                    if (null!=wsData.getString("mqKey") && !wsData.getString("mqKey").equals(mqKeyOld)) {
                        String mqKey = wsData.getString("mqKey");
                        if (null == mqGroupId.getJSONObject(mqKey)) {
                            mqGroupId.put(mqKey,new JSONObject());
                        }
                        // 添加mq推送信息
                        JSONObject mqIdAndAppId = mqGroupId.getJSONObject(mqKey);
                        mqIdAndAppId.put(id_U,"");
                        mqGroupId.put(mqKey,mqIdAndAppId);
                    } else {
                        if (cli.equals("app")) {
                            // 添加推送
                            pushUserObj.add(id_U);
                        }
                    }
                }
            }
        }
        // 调用发送推送和mq消息方法
        sendMqOrPush(mqGroupId,pushUserObj,logContent);
    }

}
