package com.cresign.tools.dbTools;

/**
 * @author kevin
 * @ClassName Qt
 * @Description
 * @updated 2022/9/11 10:05 AM
 * @ver 1.0.0
 **/

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.encrypt.HttpClientUtils;
import com.cresign.tools.encrypt.RSAUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.User;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Ws {



        @Autowired
        private Qt qt;

        public static final String appId = "KVB0qQq0fRArupojoL4WM9";

    /**
         * 注入RocketMQ模板
         */
        @Autowired
        private RocketMQTemplate rocketMQTemplate;

        /**
         * 发送MQ信息
         *
         * @return 返回结果:
         * @author tang
         * @date 创建时间: 2023/4/15
         * @ver 版本号: 1.0.0
         */
        // 1。 receive logFlow only
        // 2. use roket to send ES, Chat, push
        // 3. Push
        public void sendWSOnly(LogFlow log){
            rocketMQTemplate.convertAndSend("chatTopic:chatTap", JSON.toJSONString(log));
            System.out.println("发送WS完成");

        }

        public void sendESOnly(LogFlow log){
            rocketMQTemplate.convertAndSend("chatTopicEs:chatTapEs", JSON.toJSONString(log));
            System.out.println("发送ES完成");

        }

    /**
     * 发送MQ信息给 WS 来群发
     * 发给MQ ES 来 add ES to flow
     * 发Push to cidArray (get from flowControl)
     * @return 返回结果:
     * @author tang
     * @date 创建时间: 2023/4/15
     * @ver 版本号: 1.0.0
     *  1. id / id_FS || 2. id_Us[], getES(lBUser), id_APP[]
     *  logContrent.getJSONArray("id_Us") / id_APPs[]
     */
    public void sendWS(LogFlow logContent){
        System.out.println("进入发送websocket信息的组合:");
        System.out.println(JSON.toJSONString(logContent));
        // the log's id_Us may have users
        // if so, getES and get id_APP to push
        // else set id_Us + cidArray as usual
        JSONArray id_Us = new JSONArray();

        JSONArray cidArray = new JSONArray();
        // 获取公司编号
        String id_C = logContent.getId_C();
        // 获取供应商编号
        String id_CS = logContent.getId_CS();

        if (logContent.getId_Us() == null)
            logContent.setId_Us(new JSONArray());

        // fill up id_Us and cidArray (user array info)
        // by FlowControl... and you can do things like, you
        // my comp first then CS comp
        // if id_Us is listed, i should not change that
        if (logContent.getId_Us().size() > 0)
        {
            cidArray = logContent.getId_APPs();
        } else {
            // get from flowControl
            setUserListByFlowId(id_C, logContent, id_Us, cidArray);
            if (id_CS != null && !id_C.equals(id_CS)) {
                setUserListByFlowId(id_CS, logContent, id_Us, cidArray);
            }
            logContent.setId_Us(id_Us);
        }

        qt.errPrint("what is going", null, logContent, id_Us, cidArray);
        System.out.println("sendWS:");
        System.out.println(JSON.toJSONString(logContent));
        this.sendWSOnly(logContent);

        if (cidArray.size() > 0) {
            String wrdNUC = "小银【系统】";
            JSONObject wrdNU = logContent.getWrdNU();
            if (null != wrdNU && null != wrdNU.getString("cn")) {
                wrdNUC = wrdNU.getString("cn");
            }
            // 调用推送集合消息方法
            this.sendPushBatch(cidArray, wrdNUC, logContent.getZcndesc());
        }

        // remove id_Us and id_APPs
        logContent.setId_Us(null);
        logContent.setId_APPs(null);

        System.out.println("发送ES"+logContent);

        this.sendESOnly(logContent);

    }

    /**
     * 发送MQ信息，根据指定的id_Us发送，所以id_Us不能为空！
     * @param logContent	日志信息
     * @param type	是否获取id_App
     * @author tang
     * @date 创建时间: 2023/7/3
     * @ver 版本号: 1.0.0
     */
    public void sendWSNew(LogFlow logContent,int type){
        // 定义存储id_Apps
        JSONArray cidArray;
        // 获取发送用户列表
        JSONArray id_Us = logContent.getId_Us();
        // 判断类型
        if (type == 0) {
            // 创建对象
            cidArray = new JSONArray();
            // 遍历用户列表
            for (int i = 0; i < id_Us.size(); i++) {
                // 获取用户ID
                String id_U = id_Us.getString(i);
                // 查询数据库用户的id_App
                User user = qt.getMDContent(id_U, "info", User.class);
                // 判断不为空
                if (null != user&&null!=user.getInfo()&&null!=user.getInfo().getId_APP()) {
                    cidArray.add(user.getInfo().getId_APP());
                } else {
                    cidArray.add("");
                }
            }
        } else {
            // 直接获取日志的id_App
            cidArray = logContent.getId_APPs();
        }

        qt.errPrint("what is going", null, logContent, logContent.getId_Us(), cidArray);
        System.out.println("sendWS:");
        System.out.println(JSON.toJSONString(logContent));
        this.sendWSOnly(logContent);

        if (cidArray.size() > 0) {
            String wrdNUC = "小银【系统】";
            JSONObject wrdNU = logContent.getWrdNU();
            if (null != wrdNU && null != wrdNU.getString("cn")) {
                wrdNUC = wrdNU.getString("cn");
            }
            // 调用推送集合消息方法
            this.sendPushBatch(cidArray, wrdNUC, logContent.getZcndesc());
        }

        // remove id_Us and id_APPs
        logContent.setId_Us(null);
        logContent.setId_APPs(null);

        System.out.println("发送ES"+logContent);

        this.sendESOnly(logContent);

    }
    public void setUserListByGrpU(LogFlow log, String id_C, String grpU)
    {
        JSONArray userList = qt.getES("lBUser", qt.setESFilt("id_CB", id_C, "grpU", grpU));

        JSONArray userIds = new JSONArray();
        JSONArray userPushIds = new JSONArray();

        for (int i = 0; i < userList.size(); i++) {
            userIds.add(userList.getJSONObject(i).getString("id_U"));
            userPushIds.add(userList.getJSONObject(i).getString("id_APPs"));
        }
        log.setId_Us(userIds);
        log.setId_APPs(userPushIds);
    }

    private void setUserListByFlowId(String id_C, LogFlow logContent, JSONArray id_Us, JSONArray cidArray)
    { // id_Us[] and id_APPs use flowcontrol (prepareUserList)
        System.out.println("进入获取群信息:");
        System.out.println(JSON.toJSONString(logContent));
            Asset asset = qt.getConfig(id_C,"a-auth","flowControl");
            if (asset.getId().equals("none"))
                return;

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
                System.out.println("进入各FC 的loop: id=" + logContent.getId());
                    // 获取群用户id列表
                    JSONArray objUser = roomSetting.getJSONArray("objUser");
                System.out.println("objUser:" + JSON.toJSONString(objUser));
                    // 创建存储推送用户信息
//                System.out.println("当前服务标志:" + WebSocketUserServer.bz);
                    // 遍历群用户id列表
                    for (int j = 0; j < objUser.size(); j++) {
                        // 获取j对应的群用户信息
                        JSONObject thisUser = objUser.getJSONObject(j);
                        // 获取群用户id
                        id_Us.add(thisUser.getString("id_U"));
                        logContent.getId_Us().add(thisUser.getString("id_U"));

                        // 判断群用户id不等于当前ws连接用户id (push id_APP
                        if (thisUser.getInteger("imp") <= logContent.getImp()) {
                            String id_client = thisUser.getString("id_APP");
                            if (null != id_client && !"".equals(id_client)) {
                                cidArray.add(id_client);
                                logContent.getId_APPs().add(id_client);
                            } else {
                                cidArray.add("");
                                logContent.getId_APPs().add("");
                            }
                        }
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

    public void sendUsageFlow(JSONObject wrdN, String msg, String subType, String type)
    {
        // set sys log format:
        LogFlow log = new LogFlow();
        log.setSysLog("6141b6797e8ac90760913fd0", subType, msg, 3, wrdN);


        JSONArray id_Us = new JSONArray();
        JSONArray id_APPs = new JSONArray();

        // find all users in the "system usageflow group
        this.setUserListByFlowId("6141b6797e8ac90760913fd0", log, id_Us, id_APPs );
        if (type.equals("ALL"))
        {   //send WS, write ES, send push
            this.sendWS(log);
        } else if (type.equals("WSES"))
        {  // no Push
            this.sendESOnly(log);
            this.sendWSOnly(log);
        } else if (type.equals("ES"))
        {
            this.sendESOnly(log);
        } else if (type.equals("WS"))
        {
            this.sendWSOnly(log);
        }
    }

    public void sendPushBatch(JSONArray cidArray,String title,String body){
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

    public String getPushToken(){
        String appKey = "ShxgT3kg6s73NbuZeAe3I";
        String masterSecret = "0sLuGUOFPG6Hyq0IcN2JR";
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

}
