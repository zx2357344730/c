package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.client.LoginClient;
import com.cresign.chat.config.mq.MqToEs;
import com.cresign.chat.service.LogService;
import com.cresign.chat.utils.AesUtil;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
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
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author tangzejin
 * @updated 2019/8/23
 * @ver 1.0.0
 * ##description: 群聊天websocket类
 */
@ServerEndpoint("/wsU/msg/{uId}/{publicKey}/{token}/{client}")
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
     * 用来根据用户编号存储每个用户的客户端信息
     */
    private static final Map<String, Map<String, String>> clients = new HashMap<>(16);
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
    private static Qt qt;

    private static Ws ws;

    /**
     * 注入redis数据库下标1模板
     */
    private static StringRedisTemplate redisTemplate0;
    /**
     * 注入RocketMQ模板
     */
    private static RocketMQTemplate rocketMQTemplate;
    /**
     * 注入RocketMQ模板
     */
    private static LoginClient loginClient;

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
     * @param qt	DB工具类
     * @param logService	日志接口
     * @param redisTemplate0	redis下标为1的数据库模板
     * @param rocketMQTemplate	rocketMQ模板
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/22
     */
    @Autowired
    public void setWebSocketUserServer(Qt qt, Ws ws, LogService logService
            , StringRedisTemplate redisTemplate0, RocketMQTemplate rocketMQTemplate,LoginClient loginClient) {
        WebSocketUserServer.logService = logService;
        WebSocketUserServer.qt = qt;
        WebSocketUserServer.ws = ws;

        WebSocketUserServer.redisTemplate0 = redisTemplate0;
        WebSocketUserServer.rocketMQTemplate = rocketMQTemplate;
        WebSocketUserServer.loginClient = loginClient;
    }

    /**
     * 连接建立成功调用的方法
     * @param session   连接用户的session
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uId") String uId,@PathParam("publicKey") String publicKey
            ,@PathParam("token") String token,@PathParam("client")String client) {
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

            // 根据聊天室存储后端钥匙
            System.out.println("session start"+this.session.getId());
            WebSocketUserServer.keyJava.put(this.session.getId(),keyM);
        }

//        WebSocketUserServerQ.webSocketSet.put(uId,this);
        if (WebSocketUserServer.webSocketSet.containsKey(uId)) {
            WebSocketUserServer.webSocketSet.get(uId).put(this.session.getId(),this);
            WebSocketUserServer.clients.get(uId).put(this.session.getId(),client);
        } else {
            Map<String, WebSocketUserServer> map = new HashMap<>(16);
            map.put(this.session.getId(),this);
            WebSocketUserServer.webSocketSet.put(uId,map);
            Map<String,String> map1 = new HashMap<>(16);
            map1.put(this.session.getId(),client);
            WebSocketUserServer.clients.put(uId,map1);
        }
        System.out.println("Token:j:"+token);
        String s1 = redisTemplate0.opsForValue().get(token);
        System.out.println("token:"+s1);
        System.out.println("Token:u:"+uId);
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

        String msg = WebSocketUserServer.bz+"开启WS-:"+uId + ", 在线人数:"+getOnlineCount();
        ws.sendUsageFlow(qt.setJson("cn", "WS在线人数"), msg, "wsCount", "WS");

    }

    /**
     * 连接关闭调用的方法
     * @param uId    聊天室连接id
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    @OnClose
    public void onClose(@PathParam("uId") String uId) {
        String msg = WebSocketUserServer.bz+"-关闭ws:"+uId + ", 在线人数:"+getOnlineCount();
        ws.sendUsageFlow(qt.setJson("cn", "WS在线人数"), msg, "wsCount", "WS");
        closeWS(uId);
    }

    /**
     * websocket异常回调类
     * @param error 异常信息
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    @OnError
    public void onError(Throwable error) {
        // 输出错误信息
//        String msg = WebSocketUserServer.bz+"-Error: "+error.getMessage();
////        ws.sendUsageFlow(qt.setJson("cn", "Websocket Error"), msg, "wsError", "ALL");
    }

    /**
     * 实现服务器主动推送
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
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
     * @param logContent   发送消息
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    public synchronized static void sendLog(LogFlow logContent) {
        System.out.println("logContent:");
        System.out.println(JSON.toJSONString(logContent));
        // 设置日志时间
        logContent.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

        ws.sendWS(logContent);

    }
//        if ("link".equals(logContent.getSubType())) {
//            //每次响应之前随机获取AES的key，加密data数据
//            String key = AesUtil.getKey();
//            // 加密logContent数据
//            JSONObject stringMap = aes(logContent,key);
//            stringMap.put("en",true);
////            sendMessage(stringMap,key,true);
//            String id_U = logContent.getId_U();
//            if (WebSocketUserServer.webSocketSet.containsKey(id_U)) {
//                Map<String, String> cliU = WebSocketUserServer.clients.get(id_U);
//                Map<String, WebSocketUserServer> sw = WebSocketUserServer.webSocketSet.get(id_U);
//
//                cliU.keySet().forEach(k -> {
////                    String s = cliU.get(k);
////                    if ("web".equals(s)) {
////
////                    }
//                    sw.get(k).sendMessage(stringMap,key,true);
//                });
////                mqToEs.sendLogByES(logContent.getLogType(), logContent);
//            }
////            sendMsgToMQ(null,logContent);
//        }
//        else if ("only".equals(logContent.getSubType())) {
//            JSONObject data = logContent.getData();
//            JSONArray sendU = data.getJSONArray("sendU");
//            System.out.println("sendU:");
//            System.out.println(JSON.toJSONString(sendU));
//            String client = data.getString("client");
//            for (int i = 0; i < sendU.size(); i++) {
//                String id_U = sendU.getString(i);
//                if (WebSocketUserServer.webSocketSet.containsKey(id_U)) {
//                    Map<String, WebSocketUserServer> sw = WebSocketUserServer.webSocketSet.get(id_U);
//                    //每次响应之前随机获取AES的key，加密data数据
//                    String key = AesUtil.getKey();
//                    // 加密logContent数据
//                    JSONObject stringMap = aes(logContent,key);
//                    stringMap.put("en",true);
//                    if ("all".equals(client)) {
//                        sw.values().forEach(w -> w.sendMessage(stringMap,key,true));
//                    } else {
//                        Map<String, String> cliU = WebSocketUserServer.clients.get(id_U);
//                        cliU.keySet().forEach(k -> {
//                            String s = cliU.get(k);
//                            if (client.equals(s)) {
//                                sw.get(k).sendMessage(stringMap,key,true);
//                            }
//                        });
//                    }
//                }
//            }
//        } else {
//            JSONArray id_Us = new JSONArray();
//            JSONArray cidArray = new JSONArray();
//            // 获取公司编号
//            String id_C = logContent.getId_C();
//            // 获取供应商编号
//            String id_CS = logContent.getId_CS();
//
//            // fill up id_Us and cidArray (user array info)
//            prepareMqUserInfo(id_C, logContent, id_Us, cidArray);
//            if (id_CS != null && !id_C.equals(id_CS)) {
//                prepareMqUserInfo(id_CS, logContent, id_Us, cidArray);
//            }
////            // 127.0.0.1 local test switching
////            localSending(id_Us, logContent);
//            // 调用检测id_U在不在本服务并发送信息方法
//            ws.sendWS(logContent);
//
////
////            sendMsgToMQ(id_Us,logContent);
////////            //4. regular send to ES 1 time
////            sendMsgToEs(logContent);
////////            //5. regular send to PUSH to everybody who registered id_APP - batch push
////            sendMsgToPush(cidArray,logContent);
//        }

    /**
     * 根据key加密logContent数据
     * @param logContent	发送的日志数据
     * @param key	AES
     * @return java.util.Map<java.lang.String,java.lang.String>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
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
     * @param message   接收的消息
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
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
            else if ("4".equals(id)) {
                System.out.println(JSON.toJSONString(WebSocketUserServer.clients));
            } else {

                // 调用解密并且发送信息方法
                LogFlow logData = RsaUtil.encryptionSend(map, WebSocketUserServer.keyJava.get(this.session.getId())
                        .getString("privateKeyJava"));
                if (WebSocketUserServer.webSocketSet.containsKey(logData.getId_U())) {
                    System.out.println("在本服务:");
                    System.out.println(JSON.toJSONString(logData));
                    if ("refreshToken".equals(logData.getLogType())) {
                        JSONObject data = logData.getData();
                        System.out.println("请求api:");
                        String apiResponse = loginClient.refreshToken2(logData.getId_U()
                                , logData.getId_C(),data.getString("refreshTokenJiu"),data.getString("clientType"));
                        System.out.println("输出请求refreshToken:");
                        System.out.println(apiResponse);
                        data.put("refreshToken",apiResponse);
                        logData.setData(data);
                        JSONArray array = new JSONArray();
                        array.add(logData.getId_U());
                        logData.setId_Us(array);
                    }
                    logData.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                    ws.sendWS(logData);
                } else {
                    System.out.println("不在本服务:");
                }
                ws.sendWSOnly(logData);
            }
        }
    }

    /**
     * MQ消息接收方法
     * @param msg	接收的消息
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/22
     */
    @Override
    public void onMessage(String msg)
    {
        System.out.println("MQ收到消息:");
        System.out.println(msg);
        JSONObject json = JSONObject.parseObject(msg);

        LogFlow logContent = qt.jsonTo(json, LogFlow.class);

        //每次响应之前随机获取AES的key，加密data数据
        String key = AesUtil.getKey();

        // 加密logContent数据
        JSONObject stringMap = aes(logContent,key);
        stringMap.put("en",true);

        if (logContent.getId_Us().size() == 0) {
//            System.out.println("单消息:");
//            if (WebSocketUserServer.webSocketSet.containsKey(logContent.getId_U())) {
//                WebSocketUserServer.webSocketSet.get(logContent.getId_U()).values()
//                        .forEach(w -> w.sendMessage(stringMap
//                                ,key,true));
//            }
        } else {
            System.out.println("群消息:");
            JSONArray id_Us = json.getJSONArray("id_Us");
            for (int i = 0; i < id_Us.size(); i++)
            {
                System.out.println("idU"+id_Us.getString(i));
                if (WebSocketUserServer.webSocketSet.containsKey(id_Us.getString(i))) {
                    WebSocketUserServer.webSocketSet.get(id_Us.getString(i)).values()
                            .forEach(w -> w.sendMessage(stringMap
                                    ,key,true));
                }
            }
        }

    }

    /**
     * 当前总在线人数加1
     * 无参
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/23
     */
    private static synchronized void addOnlineCount(){
        WebSocketUserServer.totalOnlineCount++;
    }

    /**
     * 当前总在线人数减1
     * 无参
     * @author tang
     * @ver 1.0.0
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
     * @ver 1.0.0
     * @date 2022/6/23
     */
    private static synchronized int getOnlineCount(){
        return WebSocketUserServer.totalOnlineCount;
    }

    /**
     * ws关闭执行方法
     * @param uId	当前连接用户编号
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/23
     */
    private synchronized void closeWS(String uId){
        // 设置用户为离线
        // 判断用户连接不为空
        if (null != WebSocketUserServer.webSocketSet.get(uId)) {
            // 删除用户自己的连接
//            WebSocketUserServerQ.webSocketSet.remove(uId);
            if (null != WebSocketUserServer.webSocketSet.get(uId).get(this.session.getId())) {
                WebSocketUserServer.webSocketSet.get(uId).remove(this.session.getId());
            }

            if (null != WebSocketUserServer.clients.get(uId).get(this.session.getId())) {
                WebSocketUserServer.clients.get(uId).remove(this.session.getId());
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


}