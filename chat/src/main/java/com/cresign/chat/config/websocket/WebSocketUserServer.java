package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.client.LoginClient;
import com.cresign.chat.utils.AesUtil;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.chat.utils.WsId;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.LogFlow;
import io.netty.channel.ChannelHandler.Sharable;
import org.apache.commons.codec.binary.Base64;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author tangzejin
 * @updated 2019/8/23
 * @ver 1.0.0
 * ##description: 群聊天websocket类
 */
@ServerEndpoint("/wsU/msg/{uId}/{publicKey}/{token}/{appId}/{client}")
@Component
@Sharable
//@RocketMQMessageListener(
//        topic = "chatTopic",
//        selectorExpression = "chatTap",
//        messageModel = MessageModel.BROADCASTING,
////        messageModel = MessageModel.CLUSTERING,
//        consumerGroup = "topicF-chat"
//)
@RocketMQMessageListener(
        topic = WsId.topic,
        selectorExpression = WsId.tap,
        messageModel = MessageModel.BROADCASTING,
        consumerGroup = WsId.group
)
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
//    public static final String bz = UUID.randomUUID().toString().replace("-","");

    /**
     * 用来根据用户编号存储每个用户的WebSocket连接信息
     */
    private static final Map<String, Map<String, WebSocketUserServer>> webSocketSet = new HashMap<>(16);
    /**
     * 用来根据用户编号存储每个用户的客户端信息
     */
    private static final Map<String, Map<String, String>> clients = new HashMap<>(16);
    private static final Map<String, Map<String, String>> appIds = new HashMap<>(16);
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
//    /**
//     * 用户的前端公钥Map集合
//     */
//    private static final Map<String,Map<String, String>> loginPublicKeyList = new HashMap<>(16);
//    /**
//     * 后端聊天室对应公钥私钥Map集合
//     */
//    private static final Map<String,Map<String,JSONObject>> keyJava = new HashMap<>(16);
    /**
     * 注入日志接口
     */
//    private static LogService logService;
    /**
     * 注入redis工具类
     */
    private static Qt qt;

    private static Ws ws;

//    /**
//     * 注入redis数据库下标1模板
//     */
//    private static StringRedisTemplate redisTemplate0;
//    /**
//     * 注入RocketMQ模板
//     */
//    private static RocketMQTemplate rocketMQTemplate;
    /**
     * 注入RocketMQ模板
     */
    private static LoginClient loginClient;

    /**
     * 用户自身编号
     */
//    private String userId;

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    private String onlyId;
    /**
     * 注入的时候，给类的 service 注入
     * @param qt	DB工具类
//     * @param logService	日志接口
//     * @param redisTemplate0	redis下标为1的数据库模板
//     * @param rocketMQTemplate	rocketMQ模板
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/22
     */
    @Autowired
    public void setWebSocketUserServer(Qt qt, Ws ws
//            , LogService logService
//            , StringRedisTemplate redisTemplate0
//            , RocketMQTemplate rocketMQTemplate
            ,LoginClient loginClient) {
//        WebSocketUserServer.logService = logService;
        WebSocketUserServer.qt = qt;
        WebSocketUserServer.ws = ws;

//        WebSocketUserServer.redisTemplate0 = redisTemplate0;
//        WebSocketUserServer.rocketMQTemplate = rocketMQTemplate;
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
            ,@PathParam("token") String token,@PathParam("client")String client,@PathParam("appId")String appId) {
//        this.userId = uId;
        // 获取当前用户session
        try {
            String mqKey = WsId.topic + ":" + WsId.tap;
            this.session = session;
            this.onlyId = UUID.randomUUID().toString().replace("-","");
            JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, uId);
//            System.out.println(JSON.toJSONString(rdInfo));
            if (null == rdInfo) {
                rdInfo = new JSONObject();
            }
            JSONObject cliInfo = rdInfo.getJSONObject(client);
            if (null == cliInfo) {
                cliInfo = new JSONObject();
            } else {
                String mqKeyOld = cliInfo.getString("mqKey");
                if (null != mqKeyOld) {
                    String appIdOld = cliInfo.getString("appId");
                    LogFlow logData = new LogFlow();
                    logData.setId_U(uId);
                    logData.setId_Us(qt.setArray(uId));
//                    logData.setId_APPs(qt.setArray(appIdOld));
                    logData.setLogType("msg");
                    logData.setSubType("Offline");
                    JSONObject data = new JSONObject();
                    data.put("client",client);
                    logData.setData(data);
                    if (mqKey.equals(mqKeyOld)) {
//                    WebSocketUserServer.webSocketSet.get(id_UNew).values()
//                            .forEach(w -> w.sendMessage(stringMap,key,true));
                        if (!appIdOld.equals(appId)) {
                            if (WebSocketUserServer.webSocketSet.containsKey(uId)) {
                                //每次响应之前随机获取AES的key，加密data数据
                                String key = AesUtil.getKey();
                                // 加密logContent数据
                                JSONObject stringMap = aes(logData,key);
                                stringMap.put("en",true);
                                WebSocketUserServer.webSocketSet.get(uId).get(getOnlyId(uId,client))
                                        .sendMessage(stringMap,key,true);
                            } else {
                                closeWS(uId,client,appId);
                            }
                        }
//                    sendLogCore(logData,false);
                    } else {
                        ws.sendWSOnly(mqKeyOld,logData);
                    }
                }
            }
            cliInfo.put("mqKey",mqKey);
            cliInfo.put("appId",appId);
            rdInfo.put(client,cliInfo);
            qt.setRDSet(Ws.ws_mq_prefix,uId,JSON.toJSONString(rdInfo),6000L);

//            System.out.println("sessionId:"+this.session.getId());
//            System.out.println("onlyId:"+this.onlyId);

            // 字符串转换
            String s = publicKey.replaceAll(",", "/");
            s = s.replaceAll("%20"," ");

            String replace = s.replace("-----BEGIN PUBLIC KEY-----", "");
            String replace2 = replace.replace("-----END PUBLIC KEY-----", "");
//            System.out.println("前端公钥:");
//            System.out.println(replace2);
            // 设置前端公钥
            WebSocketUserServer.loginPublicKeyList.put(this.onlyId,replace2);
//            if (!WebSocketUserServer.webSocketSet.containsKey(uId) ||
//                    !WebSocketUserServer.webSocketSet.get(uId).containsKey(this.onlyId) ) {
//
//
//            }
            // 获取后端私钥
            String privateKeyJava = RsaUtil.getPrivateKey();
            // 获取后端公钥
            String publicKeyJava = RsaUtil.getPublicKey();
            // 创建存储后端公钥私钥
            JSONObject keyM = new JSONObject();
            keyM.put("privateKeyJava",privateKeyJava);
            keyM.put("publicKeyJava",publicKeyJava);

            // 根据聊天室存储后端钥匙
//                System.out.println("session start:"+this.session.getId());
//                System.out.println("onlyId start:"+this.onlyId);
            WebSocketUserServer.keyJava.put(this.onlyId,keyM);

//        WebSocketUserServerQ.webSocketSet.put(uId,this);
            if (WebSocketUserServer.webSocketSet.containsKey(uId)) {
                WebSocketUserServer.webSocketSet.get(uId).put(this.onlyId,this);
                WebSocketUserServer.clients.get(uId).put(client,this.onlyId);
                WebSocketUserServer.appIds.get(uId).put(this.onlyId,appId);
            } else {
                Map<String, WebSocketUserServer> map = new HashMap<>(16);
                map.put(this.onlyId,this);
                WebSocketUserServer.webSocketSet.put(uId,map);
                Map<String,String> map1 = new HashMap<>(16);
                map1.put(client,this.onlyId);
                WebSocketUserServer.clients.put(uId,map1);
                Map<String,String> map2 = new HashMap<>(16);
                map2.put(this.onlyId,appId);
                WebSocketUserServer.appIds.put(uId,map2);
            }
            System.out.println("----- ws打开 -----:"+this.onlyId + ",uId:" + uId + ", 服务:"+mqKey+", 端:"+client);
            System.out.println("在线人数:"+getOnlineCount());
            System.out.println("当前用户在线-端-:"+JSON.toJSONString(WebSocketUserServer.clients.get(uId).keySet()));
//            System.out.println("Token:"+token);
//            String s1 = redisTemplate0.opsForValue().get(token);
//            qt.getRDSet(token);
//            System.out.println("client:"+client);
//            String collection;
//            if ("web".equals(client)) {
//                collection = "webToken";
//            } else if ("wx".equals(client)) {
//                collection = "wxToken";
//            } else if ("pad".equals(client)) {
//                collection = "padToken";
//            } else {
//                collection = "appToken";
//            }
//            String collection = client + "Token";
            String collection = client + "RefreshToken";
            String getRefreshToken = qt.getRDSetStr(collection,token);
//            System.out.println("getToken:");
//            System.out.println(JSON.toJSONString(getToken));
//            System.out.println("Token:u:"+uId);
            if (null == getRefreshToken) {
                System.out.println("不等于-getToken:");
//                System.out.println("Token:u:"+uId);
                // 创建回应前端日志
                LogFlow logContent = LogFlow.getInstance();
                logContent.setId(null);
                logContent.setZcndesc(null);
                logContent.setTmd(null);
                logContent.setId_C(null);
                logContent.setId_U(uId);
                logContent.setLogType("msg");
                logContent.setSubType("tokenExpire");
                logContent.setTzone(null);
                JSONObject data = new JSONObject();
                // 携带后端公钥
                data.put("xxx","xxx");
                data.put("client",client);
                logContent.setData(data);
                //每次响应之前随机获取AES的key，加密data数据
                String keyAes = AesUtil.getKey();
                // 根据AES加密数据
                JSONObject stringMap = aes(logContent,keyAes);
                stringMap.put("en",false);
//            stringMap.put("err","err");
                // 发送到前端
                this.sendMessage(stringMap,keyAes,false);
                this.onClose(uId,client,appId);
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
            data.put("client",client);
            // 携带后端公钥
            data.put("publicKeyJava", WebSocketUserServer.keyJava.get(this.onlyId).getString("publicKeyJava"));
            logContent.setData(data);
            //每次响应之前随机获取AES的key，加密data数据
            String keyAes = AesUtil.getKey();
            // 根据AES加密数据
            JSONObject stringMap = aes(logContent,keyAes);
            stringMap.put("en",true);
            stringMap.put("totalOnlineCount",getOnlineCount());
            // 发送到前端
            this.sendMessage(stringMap,keyAes,true);

//            String msg = this.onlyId+"开启WS-:"+ uId + ", 在线人数:"+getOnlineCount();
//            ws.sendUsageFlowNew(qt.setJson("cn", "WS在线人数"), msg, "wsCount", "WS",mqKey);
        } catch (Exception e){
            System.out.println("出现异常:"+e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 连接关闭调用的方法
     * @param uId    聊天室连接id
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    @OnClose
    public void onClose(@PathParam("uId") String uId,@PathParam("client")String client,@PathParam("appId")String appId) {
        System.out.println("websocket:关闭, 用户:"+uId+", 端:"+client+", 服务:"+WsId.topic + ":" + WsId.tap+", appId:"+appId);
        closeWS(uId,client,appId);
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
        StringWriter writer = new StringWriter();
        PrintWriter printWriter= new PrintWriter(writer);
        error.printStackTrace(printWriter);
        String msg = this.onlyId+"-Error: "+error;
        String msg2 = writer.toString().substring(0, 650);
        ws.sendUsageFlow(qt.setJson("cn", msg), msg2, "wsError", "WS");
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
//                System.out.println("前端公钥:");
//                System.out.println(WebSocketUserServer.loginPublicKeyList.get(this.onlyId));
                String aesKey = Base64.encodeBase64String(RsaUtil.
                        encryptByPublicKey(key.getBytes()
                                , WebSocketUserServer.loginPublicKeyList.get(this.onlyId)));

                // 添加加密数据到返回集合
                stringMap.put("aesKey",aesKey);
            } catch (Exception e) {
                System.out.println(key);
                System.out.println(this.onlyId);
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

        sendLogCore(logContent,false);

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
     * websocket从前端直接消息接收
     * @param message   接收的消息
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    @OnMessage
    public void onMessageWeb(String message){
        JSONObject map = JSONObject.parseObject(message);
        System.out.println("收到ws消息:");
        System.out.println(map);
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
//            else if ("3".equals(id)) {
////                System.out.println("测试在线输出:"+id+",回复:22222");
//
//                // 加密logContent数据
//                JSONObject stringMap = new JSONObject();
//                stringMap.put("key","3");
//                stringMap.put("en",false);
//
//                this.sendMessage(stringMap,null,false);
//            }
//            else if ("4".equals(id)) {
//                System.out.println(JSON.toJSONString(WebSocketUserServer.clients));
//            }
//            else if ("2".equals(id)) {
////                System.out.println("心跳输出:"+id+",回复:22222");
//
//                // 加密logContent数据
//                JSONObject stringMap = new JSONObject();
//                stringMap.put("key","2");
//                stringMap.put("en",false);
//
//                this.sendMessage(stringMap,null,false);
//            }
            else {
                // 调用解密并且发送信息方法
                LogFlow logData = RsaUtil.encryptionSend(map, WebSocketUserServer.keyJava.get(this.onlyId)
                        .getString("privateKeyJava"));
                if (WebSocketUserServer.webSocketSet.containsKey(logData.getId_U())) {
                    System.out.println("在本服务:");

                    if ("token".equals(logData.getSubType()) && "usageflow".equals(logData.getLogType())) {
                        JSONObject data = logData.getData();
                        System.out.println("请求 RT2 api:");
                        String newToken = loginClient.refreshToken2(logData.getId_U(), logData.getId_C(),data.getString("refreshTokenJiu")
                                ,data.getString("clientType"),data.getString("token"));

                        data.put("token",newToken);
                        String client = data.getString("client");
                        logData.setData(data);
                        logData.getData().remove("refreshTokenJiu");
                        logData.setId_Us(qt.setArray(logData.getId_U()));
                        logData.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                        JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, logData.getId_U());
                        if (null != rdInfo && rdInfo.size()>0 && null != rdInfo.getJSONObject(client)) {
//                            for (String cli : rdInfo.keySet()) {
//                                JSONObject cliInfo = rdInfo.getJSONObject(cli);
//
//                            }
//                            // 放到mq
//                            ws.sendWSOnly(rdInfo.getJSONObject(client).getString("mqKey"),logData);
                            //每次响应之前随机获取AES的key，加密data数据
                            String key = AesUtil.getKey();
                            // 加密logContent数据
                            JSONObject stringMap = aes(logData,key);
                            stringMap.put("en",true);
                            if (WebSocketUserServer.webSocketSet.containsKey(logData.getId_U())) {
                                String onlyId = WebSocketUserServer.clients.get(logData.getId_U()).get(client);
                                WebSocketUserServer.webSocketSet.get(logData.getId_U()).get(onlyId).sendMessage(stringMap,key,true);
                            }
                        }
                    } else {
                        logData.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                        // 放到mq
//                        ws.sendWS(logData);
                        sendLogCore(logData,false);
                    }
                } else {
                    System.out.println("不在本服务:");
                    sendLogCore(logData,false);
                }
//                ws.sendWSOnly(logData);
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
        LogFlow logContent = qt.jsonTo(JSONObject.parseObject(msg), LogFlow.class);
        System.out.println("收到MQ消息:");
        System.out.println(JSON.toJSONString(logContent));
//        //每次响应之前随机获取AES的key，加密data数据
//        String key = AesUtil.getKey();
//
//        // 加密logContent数据
//        JSONObject stringMap = aes(logContent,key);
//        stringMap.put("en",true);
//
//        if (logContent.getId_Us().size() > 0) {
//            System.out.println("群消息:");
//            JSONArray id_Us = logContent.getId_Us();
//            for (int i = 0; i < id_Us.size(); i++)
//            {
//                System.out.println("idU:"+id_Us.getString(i));
//                if (WebSocketUserServer.webSocketSet.containsKey(id_Us.getString(i))) {
//                    WebSocketUserServer.webSocketSet.get(id_Us.getString(i)).values()
//                            .forEach(w -> w.sendMessage(stringMap,key,true));
//                }
//            }
//        }
        sendLogCore(logContent,true);

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
        return WebSocketUserServer.webSocketSet.size();
    }

    /**
     * ws关闭执行方法
     * @param uId	当前连接用户编号
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/23
     */
    private static synchronized void closeWS(String uId,String client,String appId){
        if (null != WebSocketUserServer.webSocketSet.get(uId)) {
            String onlyIdThis = getOnlyId(uId, client);
            if (WebSocketUserServer.appIds.get(uId).get(onlyIdThis).equals(appId)) {
                JSONObject mqKey = ws.getMqKey(uId);
                if (null != mqKey) {
                    JSONObject cliInfo = mqKey.getJSONObject(client);
                    if (null != cliInfo && null != cliInfo.getString("mqKey")) {
                        if (!cliInfo.getString("mqKey").equals(WsId.topic + ":" + WsId.tap)) {
                            System.out.println("旧appId客户端关闭-别的客户端在线:"+uId+", client:"+client);
                            return;
                        }
                    }
                }
                System.out.println("进入清理ws:-"+uId+", client:"+client);
                if (null != WebSocketUserServer.webSocketSet.get(uId).get(onlyIdThis)) {
                    WebSocketUserServer.webSocketSet.get(uId).remove(onlyIdThis);
                }
                if (null != WebSocketUserServer.clients.get(uId).get(client)) {
                    WebSocketUserServer.clients.get(uId).remove(client);
                }
                if (null != WebSocketUserServer.appIds.get(uId).get(onlyIdThis)) {
                    WebSocketUserServer.appIds.get(uId).remove(onlyIdThis);
                }
                if (null != WebSocketUserServer.loginPublicKeyList.get(onlyIdThis)) {
                    WebSocketUserServer.loginPublicKeyList.remove(onlyIdThis);
                }
                if (null != WebSocketUserServer.keyJava.get(onlyIdThis)) {
                    // 删除钥匙
                    WebSocketUserServer.keyJava.remove(onlyIdThis);
                }
                if (WebSocketUserServer.webSocketSet.get(uId).size() == 0) {
                    WebSocketUserServer.webSocketSet.remove(uId);
                    WebSocketUserServer.clients.remove(uId);
                    WebSocketUserServer.appIds.remove(uId);
                }
//            // 设置用户为离线
//            // 判断用户连接不为空
//            if (null != WebSocketUserServer.webSocketSet.get(uId)) {
//                // 删除用户自己的连接
////            WebSocketUserServerQ.webSocketSet.remove(uId);
//
////            System.out.println(JSON.toJSONString(WebSocketUserServer.webSocketSet.get(uId)));
////            // 在线数减1
////            if(WebSocketUserServer.totalOnlineCount > 0 && WebSocketUserServer.webSocketSet.get(uId).size()==0) {
////                subOnlineCount();
////            }
//            }

                JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, uId);
                if (null != rdInfo && rdInfo.size() > 0 && null != rdInfo.getJSONObject(client)) {
                    rdInfo.getJSONObject(client).remove("mqKey");
                    qt.setRDSet(Ws.ws_mq_prefix,uId,JSON.toJSONString(rdInfo),6000L);
//                rdInfo.remove(client);
//                if (rdInfo.size() > 0) {
//                    qt.setRDSet(Ws.ws_mq_prefix,uId,JSON.toJSONString(rdInfo),6000L);
//                } else {
//                    qt.delRD(Ws.ws_mq_prefix, uId);
//                }
                }
            } else {
                System.out.println("旧appId客户端关闭:"+uId+", client:"+client);
            }
        } else {
            System.out.println("旧appId客户端关闭:"+uId+", client:"+client);
        }
    }

    private static void sendLogCore(LogFlow logContent,boolean isMQ){
        //每次响应之前随机获取AES的key，加密data数据
        String key = AesUtil.getKey();

        // 加密logContent数据
        JSONObject stringMap = aes(logContent,key);
        stringMap.put("en",true);

        if (!isMQ) {
            ws.getUserIdsOrAppIds(logContent);
        }
        boolean isOffline = "msg".equals(logContent.getLogType()) && "Offline".equals(logContent.getSubType());
        if (logContent.getId_Us().size() > 0) {
            System.out.println("群消息:");
            JSONArray id_Us = logContent.getId_Us();
//            JSONArray id_APPs = logContent.getId_APPs();
            JSONObject mqGroupId = new JSONObject();
            JSONArray pushUsers = new JSONArray();
//            JSONObject mqGroupAppId = new JSONObject();
//            JSONArray pushAppIds = new JSONArray();
//            JSONArray pushPadAppIds = new JSONArray();
            for (int i = 0; i < id_Us.size(); i++) {
                String id_UNew = id_Us.getString(i);
                System.out.println("idU:"+id_UNew);
                JSONObject rdInfo = ws.getMqKey(id_UNew);
                if (null == rdInfo) {
//                    pushPadAppIds.add(id_UNew);
//                    if ("".equals(id_APPs.getString(i))) continue;
//                    pushAppIds.add(id_APPs.getString(i));
                    pushUsers.add(id_UNew);
                    continue;
                }
                if (WebSocketUserServer.webSocketSet.containsKey(id_UNew)) {
                    System.out.println(WsId.topic + ":" + WsId.tap+":本服务发送:");
//                    JSONArray sendClient = new JSONArray();
//                    WebSocketUserServer.webSocketSet.get(id_UNew).values()
//                            .forEach(w -> w.sendMessage(stringMap,key,true));
                    for (String client : WebSocketUserServer.clients.get(id_UNew).keySet()) {
                        String onlyId = WebSocketUserServer.clients.get(id_UNew).get(client);
                        WebSocketUserServer.webSocketSet.get(id_UNew).get(onlyId).sendMessage(stringMap,key,true);
                        rdInfo.remove(client);
                    }
                    for (String client : rdInfo.keySet()) {
                        closeWS(id_UNew,client,rdInfo.getJSONObject(client).getString("appId"));
                    }
                    ws.sendESOnlyNew(logContent);
                } else {
//                    String client = ws.getClient(logContent);
                    if (isMQ) {
                        if (isOffline) {
                            continue;
                        }
//                        JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, id_UNew);
                        for (String cli : rdInfo.keySet()) {
                            JSONObject cliInfo = rdInfo.getJSONObject(cli);
                            if (null != cliInfo) {
                                String mqKey = cliInfo.getString("mqKey");
                                String thisWs = WsId.topic + ":" + WsId.tap;
                                if ((null != mqKey && !"".equals(mqKey))) {
                                    if (thisWs.equals(mqKey)) {
//                                            rdInfo.remove(cli);
//                                            qt.setRDSet(Ws.ws_mq_prefix,id_UNew,JSON.toJSONString(rdInfo),6000L);
                                        String appId = cliInfo.getString("appId");
//                                        if (cli.equals("app")) {
//                                            if (!"".equals(appId)) {
//                                                pushAppIds.add(appId);
//                                            } else {
//                                                pushAppIds.add(id_APPs.getString(i));
//                                            }
//                                        }
//                                        pushPadAppIds.add(id_UNew);
                                        pushUsers.add(id_UNew);
                                        closeWS(id_UNew,cli,appId);
                                    }
                                }
                            }
                        }
//                        String cli = ws.getClient(logContent);
//                        JSONObject cliInfo = rdInfo.getJSONObject(cli);
//                        if (null != cliInfo) {
//                            String mqKey = cliInfo.getString("mqKey");
//                            String thisWs = WsId.topic + ":" + WsId.tap;
//                            if ((null != mqKey && !"".equals(mqKey))) {
//                                if (thisWs.equals(mqKey)) {
//                                    rdInfo.remove(cli);
//                                    qt.setRDSet(Ws.ws_mq_prefix,id_UNew,JSON.toJSONString(rdInfo),6000L);
//                                }
//                            }
//                        }
//                        if (null==appId||"".equals(appId)) continue;
//                        pushAppIds.add(appId);
//                        qt.delRD(Ws.ws_mq_prefix,id_UNew);
                    } else {
//                        String mqKey = WsId.topic + ":" + WsId.tap;
//                        String appId = null;
//                        String userAppId = id_APPs.getString(i);
//                        JSONObject rdInfo = ws.getMqKey(id_UNew);
                        for (String cli : rdInfo.keySet()) {
                            JSONObject cliInfo = rdInfo.getJSONObject(cli);
                            if (null != cliInfo) {
                                String mqKey = cliInfo.getString("mqKey");
//                                    String thisWs = WsId.topic + ":" + WsId.tap;
                                if ((null != mqKey && !"".equals(mqKey)) && !mqKey.equals(WsId.topic + ":" + WsId.tap)) {
//                                    userAppId = "";
//                                    id_APPs.set(i,userAppId);
                                    if (null == mqGroupId.getJSONObject(mqKey)) {
                                        mqGroupId.put(mqKey,new JSONObject());
//                                            mqGroupAppId.put(mqKey,new JSONObject());
                                    }
//                                        JSONArray mqIdArr = mqGroupId.getJSONArray(mqKey);
//                                        mqIdArr.add(id_UNew);
//                                        mqGroupId.put(mqKey,mqIdArr);
//                                        JSONArray mqAppIdArr = mqGroupAppId.getJSONArray(mqKey);
//                                        mqAppIdArr.add(userAppId);
//                                        mqGroupAppId.put(mqKey,mqAppIdArr);
                                    JSONObject mqIdAndAppId = mqGroupId.getJSONObject(mqKey);
                                    mqIdAndAppId.put(id_UNew,"");
//                                    String appId = mqIdAndAppId.getString(id_UNew);
//                                    if (null == appId) {
//                                        if (cli.equals("app")) {
//                                            mqIdAndAppId.put(id_UNew,userAppId);
//                                        } else {
//                                            mqIdAndAppId.put(id_UNew,"");
//                                        }
//                                    } else {
//                                        if ("".equals(appId) && cli.equals("app")) {
//                                            mqIdAndAppId.put(id_UNew,userAppId);
//                                        }
//                                    }
                                    mqGroupId.put(mqKey,mqIdAndAppId);
//                                        if (thisWs.equals(mqKey)) {
//                                            rdInfo.remove(cli);
//                                            qt.setRDSet(Ws.ws_mq_prefix,id_UNew,JSON.toJSONString(rdInfo),6000L);
//                                            if (cli.equals("app")) {
//                                                appId = cliInfo.getString("appId");
//                                            }
//                                        }
                                } else {
//                                    pushPadAppIds.add(id_UNew);
//                                    if ("".equals(userAppId)) continue;
//                                    pushAppIds.add(userAppId);
                                    pushUsers.add(id_UNew);
                                }
                            }
                        }
//                        String cli = ws.getClient(logContent);
//                        JSONObject cliInfo = rdInfo.getJSONObject(cli);
//                        if (null != cliInfo) {
//                            String mqKey = cliInfo.getString("mqKey");
//                            String thisWs = WsId.topic + ":" + WsId.tap;
//                            if ((null != mqKey && !"".equals(mqKey))) {
//                                if (thisWs.equals(mqKey)) {
//                                    rdInfo.remove(cli);
//                                    qt.setRDSet(Ws.ws_mq_prefix,id_UNew,JSON.toJSONString(rdInfo),6000L);
//                                }
//                            }
//                        }
//                        else {
//                            if (null==userAppId||"".equals(userAppId)) continue;
//                            pushAppIds.add(userAppId);
//                        }
//                        String mqKey = rdInfo.getString("mqKey");
//                        if (null!=mqKey) {
//                            userAppId = rdInfo.getString("appId");
//                            id_APPs.set(i,userAppId);
//                            if (null == mqGroupId.getJSONArray(mqKey)) {
//                                mqGroupId.put(mqKey,new JSONArray());
//                                mqGroupAppId.put(mqKey,new JSONArray());
//                            }
//                            JSONArray mqIdArr = mqGroupId.getJSONArray(mqKey);
//                            mqIdArr.add(id_UNew);
//                            mqGroupId.put(mqKey,mqIdArr);
//                            JSONArray mqAppIdArr = mqGroupAppId.getJSONArray(mqKey);
//                            mqAppIdArr.add(userAppId);
//                            mqGroupAppId.put(mqKey,mqAppIdArr);
//                        } else {
//                            if ("".equals(userAppId)||!client.equals("app")) continue;
//                            pushAppIds.add(userAppId);
//                        }
                    }
                }
            }
            if (!isOffline) {
                ws.sendMqOrPush(mqGroupId,pushUsers,logContent);
            }
        }
    }

    private static String getOnlyId(String id_U,String client){
//        if (null == WebSocketUserServer.clients.get(id_U) || null == WebSocketUserServer.clients.get(id_U).get(client)) {
//            return null;
//        }
        return WebSocketUserServer.clients.get(id_U).get(client);
    }
}