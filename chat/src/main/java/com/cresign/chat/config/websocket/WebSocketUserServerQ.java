package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.common.ChatConstants;
import com.cresign.chat.service.LogService;
import com.cresign.chat.utils.AesUtil;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.RedisUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import io.netty.channel.ChannelHandler.Sharable;
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
@ServerEndpoint("/wsU/Q/{uId}/{publicKey}/{token}")
@Component
@Sharable
@RocketMQMessageListener(
        topic = "chatTopic",
        selectorExpression = "chatTap_1",
//        selectorExpression = "chatTap_2",
//        selectorExpression = "chatTap_3",
//        selectorExpression = "chatTap_4",
        messageModel = MessageModel.BROADCASTING,
        consumerGroup = "topicF-chat"
)
public class WebSocketUserServerQ implements RocketMQListener<String> {
    /**
     * 当前websocket的标志
     */
    private static final String bz = "1";
//    private static final String bz = "2";
//    private static final String bz = "3";
//    private static final String bz = "4";

    @Override
    public boolean equals(Object obj){
        return super.equals(obj);
    }
    @Override
    public native int hashCode();
//    private static final String bz = UUID.randomUUID().toString().replace("-","");
    /**
     * 用来根据用户编号存储每个用户的WebSocket连接信息
     */
    private static final Map<String,WebSocketUserServerQ> webSocketSet = new HashMap<>(ChatConstants.HASH_MAP_DEFAULT_LENGTH);
    /**
     * 用来存储所有连接的在线人数
     */
    private static Integer totalOnlineCount = 0;
    /**
     * 用户的前端公钥Map集合
     */
    private static final Map<Session, String> loginPublicKeyList = new HashMap<>(ChatConstants.HASH_MAP_DEFAULT_LENGTH);
    /**
     * 后端聊天室对应公钥私钥Map集合
     */
    private static final Map<String,JSONObject> keyJava = new HashMap<>(ChatConstants.HASH_MAP_DEFAULT_LENGTH);
    /**
     * 注入日志接口
     */
    private static LogService logService;
    /**
     * 注入redis工具类
     */
    private static RedisUtils redisUtils;
    /**
     * 注入redis数据库下标1模板
     */
    private static StringRedisTemplate redisTemplate1;
    /**
     * 注入RocketMQ模板
     */
    private static RocketMQTemplate rocketMQTemplate;
    /**
     * 用户自身编号
     */
    private String userId;
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 注入的时候，给类的 service 注入
     * @param redisUtils	redis工具类
     * @param logService	日志接口
     * @param redisTemplate1	redis下标为1的数据库模板
     * @param rocketMQTemplate	rocketMQ模板
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/22
     */
    @Autowired
    public void setWebSocketUserServer(RedisUtils redisUtils,LogService logService
            , StringRedisTemplate redisTemplate1,RocketMQTemplate rocketMQTemplate) {
        WebSocketUserServerQ.logService = logService;
        WebSocketUserServerQ.redisUtils = redisUtils;
        WebSocketUserServerQ.redisTemplate1 = redisTemplate1;
        WebSocketUserServerQ.rocketMQTemplate = rocketMQTemplate;
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
            ,@PathParam("token") String token
    ) {
        System.out.println(WebSocketUserServerQ.bz+"-ws打开:"+uId);
        setWsU(uId,bz);
        this.userId = uId;
        addZonO();
        // 获取当前用户session
        this.session = session;

        // 字符串转换
        String s = publicKey.replaceAll(",", "/");
        s = s.replaceAll("%20"," ");
        String replace = s.replace("-----BEGIN PUBLIC KEY-----", "");
        String replace2 = replace.replace("-----END PUBLIC KEY-----", "");

        // 设置前端公钥
        WebSocketUserServerQ.loginPublicKeyList.put(this.session,replace2);

        if (!WebSocketUserServerQ.webSocketSet.containsKey(uId)) {
            // 获取后端私钥
            String privateKeyJava = RsaUtil.getPrivateKey();
            // 获取后端公钥
            String publicKeyJava = RsaUtil.getPublicKey();

            // 创建存储后端公钥私钥
            JSONObject keyM = new JSONObject();
            keyM.put("privateKeyJava",privateKeyJava);
            keyM.put("publicKeyJava",publicKeyJava);

            // 根据聊天室存储后端钥匙
            WebSocketUserServerQ.keyJava.put(uId,keyM);
        }

        WebSocketUserServerQ.webSocketSet.put(uId,this);

//        System.out.println(token);
        String s1 = redisTemplate1.opsForValue().get(token);
//        System.out.println("查询结果:"+s1);
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
            stringMap.put("err","err");
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
        data.put("publicKeyJava",WebSocketUserServerQ.keyJava.get(uId).getString("publicKeyJava"));
        logContent.setData(data);
        //每次响应之前随机获取AES的key，加密data数据
        String keyAes = AesUtil.getKey();
        // 根据AES加密数据
        JSONObject stringMap = aes(logContent,keyAes);
        stringMap.put("en",true);
        stringMap.put("totalOnlineCount",getZonO());
        // 发送到前端
        this.sendMessage(stringMap,keyAes,true);
        System.out.println("在线人数:"+getZonO());
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
        System.out.println(WebSocketUserServerQ.bz+"-关闭ws:"+uId);
        eliminateWS(uId);
        System.out.println("在线人数:"+getZonO());
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
        System.out.println(WebSocketUserServerQ.bz+"-出现错误:"+this.userId);
//        eliminateWS(this.userId);
        // 输出错误信息
//        error.printStackTrace();
        System.out.println(error.getMessage());
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
                                , WebSocketUserServerQ.loginPublicKeyList.get(this.session)));

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
        System.out.println("logContent:"+JSON.toJSONString(logContent));
        // 设置日志时间
        logContent.setTmd(DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        // 根据角色获取要发送的连接
        // 获取当前聊天室对象
        WebSocketUserServerQ wsUQw = WebSocketUserServerQ.webSocketSet.get(logContent.getId_U());

        //每次响应之前随机获取AES的key，加密data数据
        String key = AesUtil.getKey();

        // 加密logContent数据
        JSONObject stringMap = aes(logContent,key);
        stringMap.put("en",true);

        // 判断连接不为空
        if (wsUQw != null) {
            System.out.println(wsUQw.userId + " 发送给自己 " + logContent.getId_U());
            // 发送消息给自己
            wsUQw.sendMessage(stringMap,key,true);
            // 获取公司编号
            String id_C = logContent.getId_C();
            // 获取供应商编号
            String id_CS = logContent.getId_CS();
            // 调用获取用户群列表并且判断推送方法
            getA(id_C,logContent,wsUQw.userId,stringMap,key);
            // IF two companies aren't the same, need to push to both companies
            if (id_CS != null && !id_C.equals(id_CS)) {
                // 调用获取用户群列表并且判断推送方法
                getA(id_CS,logContent,wsUQw.userId,stringMap,key);
            }
        }
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
            } else if ("3".equals(id)) {
//                System.out.println("测试在线输出:"+id+",回复:22222");

                // 加密logContent数据
                JSONObject stringMap = new JSONObject();
                stringMap.put("key","3");
                stringMap.put("en",false);

                this.sendMessage(stringMap,null,false);
            } else {
                // 调用解密并且发送信息方法
                RsaUtil.encryptionSend(map,WebSocketUserServerQ.keyJava.get(id)
                        .getString("privateKeyJava"),logService);
            }
        }
    }

    /**
     * MQ消息接收方法
     * @param s	接收的消息
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/22
     */
    @Override
    public void onMessage(String s) {
        // 转换为json信息
        JSONObject json = JSONObject.parseObject(s);
        System.out.println("进入Chat_MQ:当前ws标志-"+WebSocketUserServerQ.bz);
        System.out.println(JSON.toJSONString(json));
        // 获取用户编号
        String id_u = json.getString("id_U");
        // 判断当前连接存在用户编号ws连接
        if (WebSocketUserServerQ.webSocketSet.containsKey(id_u)) {
            // 发送消息
            WebSocketUserServerQ.webSocketSet.get(id_u)
                    .sendMessage(json.getJSONObject("stringMap")
                            ,json.getString("key"),true);
        }
    }

    /**
     * 更新id_U的在线状态
     * @param id_U	用户编号
     * @param state	状态
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    public static void setWsU(String id_U,String state){
        redisTemplate1.opsForValue().set(id_U+"_ws",state);
    }

    /**
     * 获取id_U的在线状态
     * @param id_U	用户编号
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    public static String getWsU(String id_U){
        return redisTemplate1.opsForValue().get(id_U+"_ws");
    }

    /**
     * 当前总在线人数加1
     * 无参
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    private static synchronized void addZonO(){
        WebSocketUserServerQ.totalOnlineCount++;
    }

    /**
     * 当前总在线人数减1
     * 无参
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    private static synchronized void subZonO(){
        WebSocketUserServerQ.totalOnlineCount--;
    }

    /**
     * 获取当前总在线人数
     * 无参
     * @return int  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    private static synchronized int getZonO(){
        return WebSocketUserServerQ.totalOnlineCount;
    }

    /**
     * ws关闭执行方法
     * @param uId	当前连接用户编号
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    private synchronized void eliminateWS(String uId){
        // 设置用户为离线
        setWsU(uId,"0");
        // 判断用户连接不为空
        if (WebSocketUserServerQ.webSocketSet.get(uId) != null) {
            // 删除用户自己的连接
            WebSocketUserServerQ.webSocketSet.remove(uId);
        }
        // 删除用户的加密信息
        WebSocketUserServerQ.loginPublicKeyList.remove(this.session);
        // 在线数减1
        subZonO();
        // 删除钥匙
        WebSocketUserServerQ.keyJava.remove(uId);
    }

    /**
     * 获取用户群列表并且判断推送方法
     * @param id_C  公司编号
     * @param logContent    日志信息
     * @param wsUQwId_U 当前ws连接用户编号
     * @param stringMap 加密数据信息
     * @param key   aes加密键
     */
    private static void getA(String id_C,LogFlow logContent,String wsUQwId_U
            ,JSONObject stringMap,String key){
        // 根据公司编号获取a-auth的asset信息的编号
        String assetId = redisUtils.getId_A(id_C, "a-auth");
        // 判断asset编号为空
        if (null == assetId || "none".equals(assetId)) {
            return;
        }
        // 根据asset编号获取asset信息
        Asset asset = redisUtils.getAssetById(assetId, Collections.singletonList("flowControl"));
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
                System.out.println("objUser:" + JSON.toJSONString(objUser));
                // 创建存储推送用户信息
                JSONArray pushUList = new JSONArray();
                // 遍历群用户id列表
                for (int j = 0; j < objUser.size(); j++) {
                    // 获取j对应的群用户信息
                    JSONObject roomSetting1 = objUser.getJSONObject(j);
                    // 获取群用户id
                    String id_U = roomSetting1.getString("id_U");
                    // 判断群用户id不等于当前ws连接用户id
                    if (!id_U.equals(wsUQwId_U)) {
                        // 获取群用户id的在线状态
                        String wsU = getWsU(id_U);
                        System.out.println("wsU:"+wsU+" - "+id_U);
                        // 判断在线
                        if (null != wsU && !"0".equals(wsU)) {
                            // 判断存在当前服务
                            if (WebSocketUserServerQ.webSocketSet.containsKey(id_U)) {
                                System.out.println("在本服务:"+wsU);
                                // 直接发送消息
                                WebSocketUserServerQ.webSocketSet.get(id_U)
                                        .sendMessage(stringMap,key,true);
                            } else {
                                System.out.println("不在本服务:"+wsU);
                                JSONObject db = new JSONObject();
                                db.put("stringMap",stringMap);
                                db.put("key",key);
                                db.put("id_U",id_U);
                                // mq消息推送
                                rocketMQTemplate.convertAndSend("chatTopic:chatTap_"+wsU, db);
                            }
                        } else {
                            // 推送
                            System.out.println("进入需要推送--");
                            if (roomSetting1.getInteger("imp") <= logContent.getImp()) {
                                String id_client = roomSetting1.getString("id_APP");
                                if (id_client != null) {
                                    pushUList.add(id_client);
//                                    logService.sendPush(id_client, wrdNU.getString("cn"), logContent.getZcndesc());
                                }
                            }
                        }
                    }
                }
                String wrdNUC = "用户名称为空";
                JSONObject wrdNU = logContent.getWrdNU();
                if (null != wrdNU && null != wrdNU.getString("cn")) {
                    wrdNUC = wrdNU.getString("cn");
                }
                // 调用推送集合消息方法
                getPushList(pushUList,wrdNUC,logContent.getZcndesc());
            }
        }
    }

    /**
     * 推送集合消息方法
     * @param pushUList	需要推送的信息
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/6/23
     */
    private static void getPushList(JSONArray pushUList,String title,String body){
        logService.sendTestToListPush(pushUList, title, body);
        logService.sendPushX(pushUList, title, body);
    }
}