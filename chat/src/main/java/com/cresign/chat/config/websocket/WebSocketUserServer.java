package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.config.mq.MqToEs;
import com.cresign.chat.service.LogService;
import com.cresign.chat.utils.AesUtil;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import io.netty.channel.ChannelHandler.Sharable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/23
 * ##version: 1.0.0
 * ##description: 群聊天websocket类
 */
@ServerEndpoint("/wsU/msg/{uId}/{publicKey}/{token}")
@Component
@Sharable
@RocketMQMessageListener(
        topic = "chatTopic",
        selectorExpression = "chatTap",
        messageModel = MessageModel.BROADCASTING,
        consumerGroup = "topicF-chat"
)
@Slf4j
public class WebSocketUserServer implements RocketMQListener<String> {
    @Override
    public boolean equals(Object obj){
        return super.equals(obj);
    }
    @Override
    public native int hashCode();
    /**
     * 当前websocket的标志
     */
    public static final String bz = UUID.randomUUID().toString().replace("-","");
    /**
     * 用来根据用户编号存储每个用户的WebSocket连接信息
     */
    private static final Map<String, Map<String, WebSocketUserServer>> webSocketSet = new HashMap<>(16);
    /**
     * 用来存储所有连接的在线人数
     */
    private static Integer totalOnlineCount = 0;
    /**
     * 用户的前端公钥Map集合
     */
    private static final Map<String, String> loginPublicKeyList = new HashMap<>(16);
    /**
     * 后端聊天室对应公钥私钥Map集合
     */
    private static final Map<String,JSONObject> keyJava = new HashMap<>(16);
    /**
     * 注入日志接口
     */
    private static LogService logService;
    /**
     * 注入redis工具类
     */
    private static DbUtils dbUtils;
    /**
     * 注入redis数据库下标1模板
     */
    private static StringRedisTemplate redisTemplate1;
    /**
     * 注入RocketMQ模板
     */
    private static RocketMQTemplate rocketMQTemplate;

    private static MqToEs mqToEs;

    /**
     * 用户自身编号
     */
//    private String userId;

    private String userId;

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 注入的时候，给类的 service 注入
     * @param dbUtils	redis工具类
     * @param logService	日志接口
     * @param redisTemplate1	redis下标为1的数据库模板
     * @param rocketMQTemplate	rocketMQ模板
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/22
     */
    @Autowired
    public void setWebSocketUserServer(DbUtils dbUtils,LogService logService
            , StringRedisTemplate redisTemplate1,RocketMQTemplate rocketMQTemplate, MqToEs mqToEs) {
        WebSocketUserServer.logService = logService;
        WebSocketUserServer.dbUtils = dbUtils;
        WebSocketUserServer.redisTemplate1 = redisTemplate1;
        WebSocketUserServer.rocketMQTemplate = rocketMQTemplate;

        WebSocketUserServer.mqToEs = mqToEs;

    }

    /**
     * 连接建立成功调用的方法
     * ##Params: session   连接用户的session
     * ##Params: id   聊天室id
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uId") String uId,@PathParam("publicKey") String publicKey
            ,@PathParam("token") String token) {
        log.info(WebSocketUserServer.bz+"-ws打开:"+uId);
        System.out.println(WebSocketUserServer.bz+"-ws打开:"+uId);
        this.userId = uId;
        // 获取当前用户session
        this.session = session;
        System.out.println("sessionId:"+this.session.getId());

        // 字符串转换
        String s = publicKey.replaceAll(",", "/");
        s = s.replaceAll("%20"," ");

        String replace = s.replace("-----BEGIN PUBLIC KEY-----", "");
        String replace2 = replace.replace("-----END PUBLIC KEY-----", "");

        // 设置前端公钥
        WebSocketUserServer.loginPublicKeyList.put(this.session.getId(),replace2);

        if (!WebSocketUserServer.webSocketSet.containsKey(uId) ||
                !WebSocketUserServer.webSocketSet.get(uId).containsKey(this.session.getId()) ) {

            // 获取后端私钥
            String privateKeyJava = RsaUtil.getPrivateKey();
            // 获取后端公钥
            String publicKeyJava = RsaUtil.getPublicKey();

            // 创建存储后端公钥私钥
            JSONObject keyM = new JSONObject();
            keyM.put("privateKeyJava",privateKeyJava);
            keyM.put("publicKeyJava",publicKeyJava);
            System.out.println("....dfdsfdsafsd.");

            // 根据聊天室存储后端钥匙
            System.out.println("session start"+this.session.getId());
            WebSocketUserServer.keyJava.put(this.session.getId(),keyM);
        }
        System.out.println("....dfdsfdsafsd.");

//        WebSocketUserServerQ.webSocketSet.put(uId,this);
        if (WebSocketUserServer.webSocketSet.containsKey(uId)) {
            WebSocketUserServer.webSocketSet.get(uId).put(this.session.getId(),this);
        } else {
            Map<String, WebSocketUserServer> map = new HashMap<>(16);
            map.put(this.session.getId(),this);
            WebSocketUserServer.webSocketSet.put(uId,map);
        }

        String s1 = redisTemplate1.opsForValue().get(token);
        if (!uId.equals(s1)) {
            // 创建回应前端日志
            LogFlow logContent = LogFlow.getInstance();
            logContent.setId(null);
            logContent.setZcndesc(null);
            logContent.setTmd(null);
            logContent.setId_C(null);
            logContent.setId_U(uId);
            logContent.setLogType("keyErr");
            logContent.setTzone(null);
            JSONObject data = new JSONObject();
            // 携带后端公钥
            data.put("xxx","xxx");
            logContent.setData(data);
            //每次响应之前随机获取AES的key，加密data数据
            String keyAes = AesUtil.getKey();
            // 根据AES加密数据
            JSONObject stringMap = aes(logContent,keyAes);
            stringMap.put("en",false);
//            stringMap.put("err","err");
            // 发送到前端
            this.sendMessage(stringMap,keyAes,false);
            this.onClose(uId);
            return;
        }
        // 创建回应前端日志
        LogFlow logContent = LogFlow.getInstance();
        logContent.setId(null);
        logContent.setZcndesc(null);
        logContent.setTmd(null);
        logContent.setId_C(null);
        logContent.setId_U(uId);
        logContent.setLogType("key");
        logContent.setTzone(null);
        JSONObject data = new JSONObject();
        // 携带后端公钥
        data.put("publicKeyJava", WebSocketUserServer.keyJava.get(this.session.getId()).getString("publicKeyJava"));
        logContent.setData(data);
        addOnlineCount();
        //每次响应之前随机获取AES的key，加密data数据
        String keyAes = AesUtil.getKey();
        // 根据AES加密数据
        JSONObject stringMap = aes(logContent,keyAes);
        stringMap.put("en",true);
        stringMap.put("totalOnlineCount",getOnlineCount());
        // 发送到前端
        this.sendMessage(stringMap,keyAes,true);
        System.out.println("在线人数:"+getOnlineCount());
        log.info("在线人数:"+getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     * ##Params: id    聊天室连接id
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @OnClose
    public void onClose(@PathParam("uId") String uId) {
        System.out.println(WebSocketUserServer.bz+"-关闭ws:"+uId);
        log.info(WebSocketUserServer.bz+"-关闭ws:"+uId);
        closeWS(uId);
        System.out.println("在线人数:"+getOnlineCount());
        log.info("在线人数:"+getOnlineCount());
    }

    /**
     * websocket异常回调类
     * ##Params: error 异常信息
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @OnError
    public void onError(Throwable error) {
        // 输出错误信息
//        error.printStackTrace();
        System.out.println(error.getMessage());
        log.info(error.getMessage());
    }

    /**
     * 实现服务器主动推送
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    private synchronized void sendMessage(JSONObject stringMap, String key, boolean isEncrypt) {
        if (isEncrypt) {
            //用前端的公钥来解密AES的key，并转成Base64
            try {
                // 使用前端公钥加密key
                String aesKey = Base64.encodeBase64String(RsaUtil.
                        encryptByPublicKey(key.getBytes()
                                , WebSocketUserServer.loginPublicKeyList.get(this.session.getId())));

                // 添加加密数据到返回集合
                stringMap.put("aesKey",aesKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 向前端推送log消息
        try {
            if (!this.session.isOpen()) {
                System.out.println("消息发送失败，session 处于关闭状态:" + session.getId());
                return;
            }
            // 发送返回数据
            this.session.getBasicRemote().sendText(JSON.toJSONString(stringMap));
        } catch (IOException e) {
            System.out.println("sendMessage出现错误");
        }
    }

    /**
     * 群发自定义消息
     * ##Params: logContent   发送消息
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    public synchronized static void sendLog(LogFlow logContent) {
//        System.out.println("logContent:"+JSON.toJSONString(logContent));
        // 设置日志时间
        logContent.setTmd(DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        //每次响应之前随机获取AES的key，加密data数据
        String key = AesUtil.getKey();

        // 加密logContent数据
        JSONObject stringMap = aes(logContent,key);
        stringMap.put("en",true);

        JSONArray id_Us = new JSONArray();
        JSONArray cidArray = new JSONArray();
        // 获取公司编号
        String id_C = logContent.getId_C();
        // 获取供应商编号
        String id_CS = logContent.getId_CS();

        // fill up id_Us and cidArray (user array info)
        prepareMqUserInfo(id_C, logContent, id_Us, cidArray);
        if (id_CS != null && !id_C.equals(id_CS)) {
            prepareMqUserInfo(id_CS, logContent, id_Us, cidArray);
        }

        // 127.0.0.1 local test switching
        localSending(id_Us, logContent);

//        // 调用检测id_U在不在本服务并发送信息方法
//        sendMsgToMQ(id_Us,logContent);
//
//        //4. regular send to ES 1 time
//        sendMsgToEs(logContent);
//
//        //5. regular send to PUSH to everybody who registered id_APP - batch push
//        sendMsgToPush(cidArray,logContent);

    }

    /**
     * 根据key加密logContent数据
     * ##Params: logContent	发送的日志数据
     * ##Params: key	AES
     * ##return: java.util.Map<java.lang.String,java.lang.String>  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/11/28 16:12
     */
    private static JSONObject aes(LogFlow logContent,String key){

        // 创建返回存储map
        JSONObject stringMap = new JSONObject();
        try {

            // 根据key加密logContent数据
            String data2 = AesUtil.encrypt(JSON.toJSONString(logContent), key);

            // 添加到返回map
            stringMap.put("data",data2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 返回结果
        return stringMap;
    }

    /**
     * websocket消息接收
     * ##Params: message   接收的消息
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @OnMessage
    public void onMessageWeb(String message){
        JSONObject map = JSONObject.parseObject(message);
        if (null != map) {
            String id = map.getString("id");
//            System.out.println("进入这里-1");
            if ("2".equals(id)) {
//                System.out.println("心跳输出:"+id+",回复:22222");

                // 加密logContent数据
                JSONObject stringMap = new JSONObject();
                stringMap.put("key","2");
                stringMap.put("en",false);

                this.sendMessage(stringMap,null,false);
            }
            else if ("3".equals(id)) {
//                System.out.println("测试在线输出:"+id+",回复:22222");

                // 加密logContent数据
                JSONObject stringMap = new JSONObject();
                stringMap.put("key","3");
                stringMap.put("en",false);

                this.sendMessage(stringMap,null,false);
            }
            else {
                // 调用解密并且发送信息方法
                LogFlow logData =  RsaUtil.encryptionSend(map, WebSocketUserServer.keyJava.get(this.session.getId())
                        .getString("privateKeyJava"));

                sendLog(logData);

            }
        }
    }

    /**
     * MQ消息接收方法
     * @param msg	接收的消息
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/22
     */

    @Override
    public void onMessage(String msg)
    {
        JSONObject json = JSONObject.parseObject(msg);
        JSONArray id_Us = json.getJSONArray("id_Us");

        for (int i = 0; i < id_Us.size(); i++)
        {
            System.out.println("idU"+id_Us.getString(i));
            if (WebSocketUserServer.webSocketSet.containsKey(id_Us.getString(i))) {
                System.out.println("在本服务:"+" - "+json.getString("bz")+" - "+"进入Chat_MQ:当前ws标志-"+ WebSocketUserServer.bz);

                WebSocketUserServer.webSocketSet.get(id_Us.getString(i)).values()
                        .forEach(w -> w.sendMessage(json.getJSONObject("stringMap")
                                ,json.getString("key"),true));
            }
        }

    }

//    @Override
//    public void onMessage(String s) {
//        // 转换为json信息
//        JSONObject json = JSONObject.parseObject(s);
////        System.out.println("进入Chat_MQ:当前ws标志-"+WebSocketUserServerQ.bz+" - "+json.getString("bz"));
////        System.out.println(JSON.toJSONString(json));
//
//
//        // 获取用户编号**数组 []
//        String id_U = json.getString("id_U");
////        if (!WebSocketUserServerQ.bz.equals(json.getString("bz"))) {
////            System.out.println("在本服务:");
//            // 判断当前连接存在用户编号ws连接
//            if (WebSocketUserServerQ.webSocketSet.containsKey(id_U)) {
//                log.info("在本服务:"+" - "+json.getString("bz")+" - "+"进入Chat_MQ:当前ws标志-"+ WebSocketUserServerQ.bz);
//                System.out.println("在本服务:"+" - "+json.getString("bz")+" - "+"进入Chat_MQ:当前ws标志-"+ WebSocketUserServerQ.bz);
//                // 发送消息
////                WebSocketUserServerQ.webSocketSet.get(id_U).sendMessage(json.getJSONObject("stringMap")
////                                ,json.getString("key"),true);
//                WebSocketUserServerQ.webSocketSet.get(id_U).values()
//                        .forEach(w -> w.sendMessage(json.getJSONObject("stringMap")
//                                ,json.getString("key"),true));
//            }
////        }
////        else {
////            log.info("在本服务:"+" - "+json.getString("bz")+" - "+"进入Chat_MQ:当前ws标志-"+WebSocketUserServerQ.bz);
////            System.out.println("在本服务:"+" - "+json.getString("bz")+" - "+"进入Chat_MQ:当前ws标志-"+WebSocketUserServerQ.bz);
////            // 发送消息
////            WebSocketUserServerQ.webSocketSet.get(id_U)
////                    .sendMessage(json.getJSONObject("stringMap")
////                            ,json.getString("key"),true);
////        }
//    }
//
//    /**
//     * 更新id_U的在线状态
//     * @param id_U	用户编号
//     * @param state	状态
//     * @return void  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/6/23
//     */
//    public static void setWsU(String id_U,String state){
//        redisTemplate1.opsForValue().set(id_U+"_ws",state);
//    }
//
//    /**
//     * 获取id_U的在线状态
//     * @param id_U	用户编号
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/6/23
//     */
//    public static String getWsU(String id_U){
//        return redisTemplate1.opsForValue().get(id_U+"_ws");
//    }

    /**
     * 当前总在线人数加1
     * 无参
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    private static synchronized void addOnlineCount(){
        WebSocketUserServer.totalOnlineCount++;
    }

    /**
     * 当前总在线人数减1
     * 无参
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    private static synchronized void subOnlineCount(){
        WebSocketUserServer.totalOnlineCount--;
    }

    /**
     * 获取当前总在线人数
     * 无参
     * @return int  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    private static synchronized int getOnlineCount(){
        return WebSocketUserServer.totalOnlineCount;
    }

    /**
     * ws关闭执行方法
     * @param uId	当前连接用户编号
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    private synchronized void closeWS(String uId){
        // 设置用户为离线
//        setWsU(uId,"0");
        // 判断用户连接不为空
        if (null != WebSocketUserServer.webSocketSet.get(uId)) {
            // 删除用户自己的连接
//            WebSocketUserServerQ.webSocketSet.remove(uId);
            if (null != WebSocketUserServer.webSocketSet.get(uId).get(this.session.getId())) {
                WebSocketUserServer.webSocketSet.get(uId).remove(this.session.getId());
            }

            if (WebSocketUserServer.webSocketSet.get(uId).isEmpty()) {
                WebSocketUserServer.webSocketSet.remove(uId);
            }
        }

        // 在线数减1
        if(WebSocketUserServer.totalOnlineCount > 0)
        {
            subOnlineCount();
        }
        // 删除钥匙
        WebSocketUserServer.keyJava.remove(this.session.getId());
    }


    //1. check a-auth to get objUser list @ id_C
    //2. if id_C!= id_CS, check a-auth get id_CS objUser if *** id_CS is real
    //3. put an array of id_U into mq

    private static void prepareMqUserInfo(String id_C, LogFlow logContent, JSONArray id_Us, JSONArray cidArray)
    {
        String assetId = dbUtils.getId_A(id_C, "a-auth");
        if (null == assetId || "none".equals(assetId)) {
            return;
        }

        // 根据asset编号获取asset信息
        Asset asset = dbUtils.getAssetById(assetId, Collections.singletonList("flowControl"));
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
                System.out.println("当前服务标志:" + WebSocketUserServer.bz);
                // 遍历群用户id列表
                for (int j = 0; j < objUser.size(); j++) {
                    // 获取j对应的群用户信息
                    JSONObject thisUser = objUser.getJSONObject(j);
                    // 获取群用户id
                    id_Us.add(thisUser.getString("id_U"));

                    // 判断群用户id不等于当前ws连接用户id
                    if (thisUser.getInteger("imp") <= logContent.getImp()) {
                        String id_client = thisUser.getString("id_APP");
                        if (null != id_client && !"".equals(id_client)) {
                            cidArray.add(id_client);
                        }
                    }
                }
            }
        }
    }





//    /**
//     * 获取用户群列表并且判断推送方法
//     * @param id_C  公司编号
//     * @param logContent    日志信息
//     * @param id_UThisWS 当前ws连接用户编号
//     * @param stringMap 加密数据信息
//     * @param key   aes加密键
//     */
//    private static void startSendingMq(String id_C,LogFlow logContent,String id_UThisWS
//            ,JSONObject stringMap,String key){
//        // 根据公司编号获取a-auth的asset信息的编号
//        String assetId = dbUtils.getId_A(id_C, "a-auth");
//        // 判断asset编号为空
//        if (null == assetId || "none".equals(assetId)) {
//            return;
//        }
//        // 根据asset编号获取asset信息
//        Asset asset = dbUtils.getAssetById(assetId, Collections.singletonList("flowControl"));
//        // 获取卡片信息
//        JSONObject flowControl = asset.getFlowControl();
//        // 获取卡片data信息
//        JSONArray flowData = flowControl.getJSONArray("objData");
//        // 遍历data
//        for (int i = 0; i < flowData.size(); i++) {
//            // 获取i对应的data信息
//            JSONObject roomSetting = flowData.getJSONObject(i);
//            // 获取群id
//            String roomId = roomSetting.getString("id");
//            // 获取日志群id
//            String logFlowId = logContent.getId();
//            if (roomSetting.getString("type").endsWith("SL")) {
//                logFlowId = logContent.getId_FS();
//            }
//            // 判断群id一样
//            if (roomId.equals(logFlowId)) {
////                System.out.println("进入各FC 的loop: id=" + logContent.getId());
//                // 获取群用户id列表
//                JSONArray objUser = roomSetting.getJSONArray("objUser");
////                System.out.println("objUser:" + JSON.toJSONString(objUser));
//                // 创建存储推送用户信息
//                JSONArray cidArray = new JSONArray();
//                System.out.println("当前服务标志:"+ WebSocketUserServerQ.bz);
//                log.info("当前服务标志:"+ WebSocketUserServerQ.bz);
//                // 遍历群用户id列表
//                for (int j = 0; j < objUser.size(); j++) {
//                    // 获取j对应的群用户信息
//                    JSONObject roomSetting1 = objUser.getJSONObject(j);
//                    // 获取群用户id
//                    String id_U = roomSetting1.getString("id_U");
//                    // 判断群用户id不等于当前ws连接用户id
//                    if (!id_U.equals(id_UThisWS)) {
//                        // 获取群用户id的在线状态
////                        String wsU = getWsU(id_U);
////                        boolean isPU = false;
////                        if (id_U.equals("6256789ae1908c03460f906f")||id_U.equals("5f28bf314f65cc7dc2e60386")||id_U.equals("62318c9a890df37b8079952d"))
////                            isPU = true;
//////                        System.out.println("wsU:"+wsU+" - "+id_U);
////                        // 判断在线
////                        if ("1".equals(wsU)
////                                &&isPU
////                        ) {
////                            // 调用检测id_U在不在本服务并发送信息方法
////                            sendMsgToMQ(id_U,stringMap,key);
////                        } else continue;
////                        else {
//                            // 推送
////                            System.out.println("进入需要推送--");
//                            if (roomSetting1.getInteger("imp") <= logContent.getImp()) {
//                                String id_client = roomSetting1.getString("id_APP");
//                                if (null != id_client && !"".equals(id_client)) {
//                                    cidArray.add(id_client);
////                                    logService.sendPush(id_client, wrdNU.getString("cn"), logContent.getZcndesc());
//                                }
//                            }
////                        }
//                    }
//                }
//                if (cidArray.size() > 0) {
//                    String wrdNUC = "用户名称为空";
//                    JSONObject wrdNU = logContent.getWrdNU();
//                    if (null != wrdNU && null != wrdNU.getString("cn")) {
//                        wrdNUC = wrdNU.getString("cn");
//                    }
//                    // 调用推送集合消息方法
//                    logService.sendPushBatch(cidArray, wrdNUC, logContent.getZcndesc());
//                }
//            }
//        }
//    }

//    /**
//     * 推送集合消息方法
//     * @param cidArray	需要推送的信息
//     * @return void  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/6/23
//     */
//    private static void getPushList(JSONArray cidArray,String title,String body){
//        String token = logService.getToken();
//        logService.sendPushBatch(cidArray, title, body, token);
//    }

    /**
     * 检测id_U在不在本服务并发送信息方法，在：直接发送、不在：放入mq发送
     * @param id_Us	用户编号
     * @param logContent
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/28
     */
    private static void sendMsgToMQ(JSONArray id_Us,LogFlow logContent) {

        //每次响应之前随机获取AES的key，加密data数据
        String key = AesUtil.getKey();

        // 加密logContent数据
        JSONObject stringMap = aes(logContent,key);
        stringMap.put("en",true);
        JSONObject db = new JSONObject();

        db.put("stringMap", stringMap);
        db.put("key", key);
        db.put("id_Us", id_Us);
        db.put("bz", WebSocketUserServer.bz);
        // mq消息推送
        System.out.println("发送消息1");

        rocketMQTemplate.convertAndSend("chatTopic:chatTap", db);
        System.out.println("发送消息2");

    }


    private static void sendMsgToPush(JSONArray cidArray, LogFlow logContent)
    {
        if (cidArray.size() > 0) {
            String wrdNUC = "用户名称为空";
            JSONObject wrdNU = logContent.getWrdNU();
            if (null != wrdNU && null != wrdNU.getString("cn")) {
                wrdNUC = wrdNU.getString("cn");
            }
            // 调用推送集合消息方法
            logService.sendPushBatch(cidArray, wrdNUC, logContent.getZcndesc());
        }
    }

    /**
     * 发送消息到es监听mq
     * @param logContent	发送的消息
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/7/2
     */
    private static void sendMsgToEs(LogFlow logContent){
        JSONObject db2 = new JSONObject();
        db2.put("logF",JSON.toJSONString(logContent));
        db2.put("bz", WebSocketUserServer.bz);
        rocketMQTemplate.convertAndSend("chatTopicEs:chatTapEs", db2);
        System.out.println("发送消息完成");
    }

    private static void localSending(JSONArray id_Us, LogFlow logContent)
    {
        //每次响应之前随机获取AES的key，加密data数据
        String key = AesUtil.getKey();

        // 加密logContent数据
        JSONObject stringMap = aes(logContent,key);
        stringMap.put("en",true);
        JSONObject db = new JSONObject();

        db.put("stringMap", stringMap);
        db.put("key", key);
        db.put("id_Us", id_Us);
        db.put("bz", WebSocketUserServer.bz);

        for (int i = 0; i < id_Us.size(); i++)
        {
            System.out.println("idU"+id_Us.getString(i));
            if (WebSocketUserServer.webSocketSet.containsKey(id_Us.getString(i))) {
                System.out.println("在本服务:"+" - "+db.getString("bz")+" - "+"进入Chat_MQ:当前ws标志-"+ WebSocketUserServer.bz);

                WebSocketUserServer.webSocketSet.get(id_Us.getString(i)).values()
                        .forEach(w -> w.sendMessage(db.getJSONObject("stringMap")
                                ,db.getString("key"),true));
            }
        }

        mqToEs.sendLogByES(logContent.getLogType(), logContent);

    }
}