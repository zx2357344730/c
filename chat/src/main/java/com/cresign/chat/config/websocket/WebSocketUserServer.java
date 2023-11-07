package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.utils.AesUtil;
import com.cresign.chat.utils.Oauth;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.chat.utils.WsId;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.User;
import io.netty.channel.ChannelHandler.Sharable;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
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
@RocketMQMessageListener(
        topic = WsId.topic,
        selectorExpression = WsId.tap,
        messageModel = MessageModel.BROADCASTING,
        consumerGroup = WsId.group
)
public class WebSocketUserServer implements RocketMQListener<String> {

//    @Override
//    public boolean equals(Object obj){
//        return super.equals(obj);
//    }
//    @Override
//    public native int hashCode();
    /**
     * 用来根据用户编号存储每个用户的WebSocket连接信息
     */
    private static final Map<String, Map<String, WebSocketUserServer>> webSocketSet = new HashMap<>(16);
    /**
     * 用来根据用户编号存储每个用户的客户端信息 -
     */
    //TODO ZJ put redis
    private static final Map<String, Map<String, String>> clients = new HashMap<>(16);
    /**
     * 用来存储用户的appId
     */
    //TODO ZJ put redis

    private static final Map<String, Map<String, String>> appIds = new HashMap<>(16);

    /**
     * 用户的前端公钥Map集合 id_U.client端
     */
    //TODO ZJ put redis
    private static final Map<String, Map<String, String>> loginPublicKeyList = new HashMap<>(16);
    /**
     * 后端聊天室对应公钥私钥Map集合
     */
    //TODO ZJ put redis
    private static final Map<String,Map<String, JSONObject>> keyJava = new HashMap<>(16);
    /**
     * 注入qt工具类
     */
    private static Qt qt;
    /**
     * 注入ws工具类
     */
    private static Ws ws;
    private static Oauth oauth;
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
//    /**
//     * 当前唯一编号
//     */
//    private String onlyId;
    private String thisUser;
    private String thisClient;
    /**
     * 注入的时候，给类的 service 注入
     * @param qt	DB工具类
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/22
     */
    //TODO ZJ ???
    @Autowired
    public void setWebSocketUserServer(Qt qt, Ws ws,Oauth oauth) {
        WebSocketUserServer.qt = qt;
        WebSocketUserServer.ws = ws;
        WebSocketUserServer.oauth = oauth;
    }

    /**
     * 连接建立成功调用的方法
     * @param session   连接用户的session
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    //TODO ZJ token no need
    //rdInfo
    @OnOpen
    public void onOpen(Session session, @PathParam("uId") String uId,@PathParam("publicKey") String publicKey
            ,@PathParam("token") String token,@PathParam("client")String client,@PathParam("appId")String appId) {
        try {
            // 获取当前标志
            String mqKey = WsId.topic + ":" + WsId.tap;
            // 设置当前用户session
            this.session = session;
            //set thisUser thisClient
            this.thisUser = uId;
            this.thisClient = client; //wx/app/web
            // 获取当前唯一编号
//            this.onlyId = UUID.randomUUID().toString().replace("-","");
            // 获取redis信息
            JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, uId); // appID? /mqKey
            // 判断redis信息为空
            if (null == rdInfo) {
                rdInfo = new JSONObject();
            }
            // 获取当前端信息
            JSONObject cliInfo = rdInfo.getJSONObject(client);
            // 判断端信息为空
            if (null == cliInfo) {
                cliInfo = new JSONObject();
            } else {
                // 获取mq编号
                String mqKeyOld = cliInfo.getString("mqKey");
                // 判断不为空
                if (null != mqKeyOld) {
                    // 获取appId
                    String appIdOld = cliInfo.getString("appId");
                    // 创建日志
                    // TODO ZJ offline - 连新要关旧
                    LogFlow logData = new LogFlow();
                    logData.setId_U(uId);
                    logData.setId_Us(qt.setArray(uId));
                    logData.setLogType("msg");
                    logData.setSubType("Offline");
                    JSONObject data = new JSONObject();
                    data.put("client",client);
                    logData.setData(data);
                    // 判断mq编号等于当前mq编号
                    if (mqKey.equals(mqKeyOld)) {
                        // 判断appId为新的 cid Push?
                        if (!appIdOld.equals(appId)) {
                            // 判断用户存在 真正发了WebSocket
                            if (WebSocketUserServer.webSocketSet.containsKey(uId)) {
                                if (null != WebSocketUserServer.webSocketSet.get(uId) && null!=getOnlyId(uId,client)
                                        &&null!=WebSocketUserServer.webSocketSet.get(uId).get(client)) {
                                    //每次响应之前随机获取AES的key，加密data数据
                                    String key = AesUtil.getKey();
                                    // 加密logContent数据
                                    JSONObject stringMap = aes(logData,key);
                                    stringMap.put("en",true);
                                    // 发送通知
                                    WebSocketUserServer.webSocketSet.get(uId).get(client)
                                            .sendMessage(stringMap,key,true);
                                } else {
                                    // 调用清理ws方法
                                    closeWS(uId,client,appId);
                                }
                            } else {
                                // 调用清理ws方法
                                closeWS(uId,client,appId);
                            }
                        }
                    } else {
                        // 直接发送信息
                        ws.sendMQ(mqKeyOld,logData);
                    }
                }
            }
            cliInfo.put("mqKey",mqKey);
            cliInfo.put("appId",appId); // mqKey appId ==
            rdInfo.put(client,cliInfo);
            // 更新redis当前端信息
            qt.setRDSet(Ws.ws_mq_prefix,uId,JSON.toJSONString(rdInfo),6000L);
            // 字符串转换
            String s = publicKey.replaceAll(",", "/");
            s = s.replaceAll("%20"," ");
            String replace = s.replace("-----BEGIN PUBLIC KEY-----", "");
            String replace2 = replace.replace("-----END PUBLIC KEY-----", "");
//            // 设置前端公钥
//            WebSocketUserServer.loginPublicKeyList.put(this.onlyId,replace2);
            Map<String, String> mapWebKey;
            if (WebSocketUserServer.loginPublicKeyList.containsKey(uId)) {
                mapWebKey = WebSocketUserServer.loginPublicKeyList.get(uId);
            } else {
                mapWebKey = new HashMap<>(16);
            }
            mapWebKey.put(client,replace2);
            WebSocketUserServer.loginPublicKeyList.put(uId,mapWebKey);
            // 获取后端私钥
            String privateKeyJava = RsaUtil.getPrivateKey();
            // 获取后端公钥
            String publicKeyJava = RsaUtil.getPublicKey();
            // 创建存储后端公钥私钥
            JSONObject keyM = new JSONObject();
            keyM.put("privateKeyJava",privateKeyJava);
            keyM.put("publicKeyJava",publicKeyJava);
//            // 存储后端钥匙
//            WebSocketUserServer.keyJava.put(this.onlyId,keyM);
            Map<String, JSONObject> mapKeyJava;
            if (WebSocketUserServer.keyJava.containsKey(uId)) {
                mapKeyJava = WebSocketUserServer.keyJava.get(uId);
            } else {
                mapKeyJava = new HashMap<>();
            }
            mapKeyJava.put(client,keyM);
            WebSocketUserServer.keyJava.put(uId,mapKeyJava);
//            if (WebSocketUserServer.webSocketSet.containsKey(uId)) {
//                // 添加新端信息
//                WebSocketUserServer.webSocketSet.get(uId).put(this.onlyId,this);
//                WebSocketUserServer.clients.get(uId).put(client,this.onlyId);
//                WebSocketUserServer.appIds.get(uId).put(this.onlyId,appId);
//            } else {
//                // 创建端
//                Map<String, WebSocketUserServer> map = new HashMap<>(16);
//                map.put(this.onlyId,this);
//                WebSocketUserServer.webSocketSet.put(uId,map);
//                Map<String,String> map1 = new HashMap<>(16);
//                map1.put(client,this.onlyId);
//                WebSocketUserServer.clients.put(uId,map1);
//                Map<String,String> map2 = new HashMap<>(16);
//                map2.put(this.onlyId,appId);
//                WebSocketUserServer.appIds.put(uId,map2);
//            }
            if (WebSocketUserServer.webSocketSet.containsKey(uId)) {
                // 添加新端信息
                WebSocketUserServer.webSocketSet.get(uId).put(client,this);
                WebSocketUserServer.clients.get(uId).put(client,"");
                WebSocketUserServer.appIds.get(uId).put(client,appId);
            } else {
                // 创建端
                Map<String, WebSocketUserServer> map = new HashMap<>(16);
                map.put(client,this);
                WebSocketUserServer.webSocketSet.put(uId,map);
                Map<String,String> map1 = new HashMap<>(16);
                map1.put(client,"");
                WebSocketUserServer.clients.put(uId,map1);
                Map<String,String> map2 = new HashMap<>(16);
                map2.put(client,appId);
                WebSocketUserServer.appIds.put(uId,map2);
            }
            System.out.println("----- ws打开 -----:" + ",uId:" + uId + ", 服务:"+mqKey+", 端:"+client);
            System.out.println("在线人数:"+getOnlineCount());
            System.out.println("当前用户在线-端-:"+JSON.toJSONString(WebSocketUserServer.clients.get(uId).keySet()));
//            String collection = client + "RefreshToken";
            // 获取redisToken
//            String getRefreshToken = qt.getRDSetStr(collection,token);
//            if (null == getRefreshToken) {
//                System.out.println("不等于-getToken:");
//                // 创建回应前端日志
//                LogFlow logContent = LogFlow.getInstance();
//                logContent.setId(null);
//                logContent.setZcndesc(null);
//                logContent.setTmd(null);
//                logContent.setId_C(null);
//                logContent.setId_U(uId);
//                logContent.setLogType("usageflow");
//                logContent.setSubType("tokenExpire");
//                logContent.setTzone(null);
//                JSONObject data = new JSONObject();
//                // 携带后端公钥
//                data.put("publicKeyJava", WebSocketUserServer.keyJava.get(this.onlyId).getString("publicKeyJava"));
//                data.put("client",client);
//                logContent.setData(data);
//                //每次响应之前随机获取AES的key，加密data数据
//                String keyAes = AesUtil.getKey();
//                // 根据AES加密数据
//                JSONObject stringMap = aes(logContent,keyAes);
//                stringMap.put("en",true);
//                // 发送到前端
//                this.sendMessage(stringMap,keyAes,true);
//                this.onClose(uId,client,appId);
//            } else {
                // 创建回应前端日志
                LogFlow logContent = LogFlow.getInstance();
                logContent.setId(null);
                logContent.setZcndesc(null);
                logContent.setTmd(null);
                logContent.setId_C(null);
                logContent.setId_U(uId);
                logContent.setLogType("key");
                logContent.setTzone(null);
                ///////////////////////...
                JSONObject data = new JSONObject();
                data.put("client",client);
                // 携带后端公钥
                data.put("publicKeyJava", WebSocketUserServer.keyJava.get(uId).get(client).getString("publicKeyJava"));
                logContent.setData(data);
                //每次响应之前随机获取AES的key，加密data数据
                String keyAes = AesUtil.getKey();
                // 根据AES加密数据
                JSONObject stringMap = aes(logContent,keyAes);
                stringMap.put("en",true);
                stringMap.put("totalOnlineCount",getOnlineCount());
                // 发送到前端
                this.sendMessage(stringMap,keyAes,true);
//            if (uId.equals("6256789ae1908c03460f906f")) {
//                qt.setRDSet(Ws.ws_mq_prefix,uId+":clients",JSON.toJSONString(WebSocketUserServer.clients.get(uId)),600L);
//                qt.setRDSet(Ws.ws_mq_prefix,uId+":appIds",JSON.toJSONString(WebSocketUserServer.appIds.get(uId)),600L);
//                qt.setRDSet(Ws.ws_mq_prefix,uId+":loginPublicKeyList",JSON.toJSONString(WebSocketUserServer.loginPublicKeyList.get(uId)),600L);
//                qt.setRDSet(Ws.ws_mq_prefix,uId+":webSocketSet",JSON.toJSONString(WebSocketUserServer.webSocketSet.get(uId).keySet()),600L);
////                qt.setRDSet(Ws.ws_mq_prefix,uId+":keyJava",JSON.toJSONString(WebSocketUserServer.keyJava),600L);
//            }
            //////////
//            }
        } catch (Exception e){
            System.out.println("出现异常:"+e.getMessage());
//            e.printStackTrace();
//            if (uId.equals("6256789ae1908c03460f906f")) {
//                qt.setRDSet(Ws.ws_mq_prefix,uId+":err",e.getMessage(),600L);
//            }
            closeWS(uId,client,appId);
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
//        System.out.println("websocket:关闭, 用户:"+uId+", 端:"+client+", 服务:"+WsId.topic + ":" + WsId.tap+", appId:"+appId);
        // 调用清理ws方法
        closeWS(uId,client,appId);
//        if (uId.equals("6256789ae1908c03460f906f")) {
//            qt.setRDSet(Ws.ws_mq_prefix,uId+":onClose","websocket:关闭, 用户:"+uId+", 端:"+client+", 服务:"+WsId.topic + ":" + WsId.tap+", appId:"+appId,600L);
//        }
    }

    /**
     * websocket异常回调类
     * @param error 异常信息
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    @OnError
    public void onError(Throwable error,@PathParam("uId") String uId) {
        // 输出错误信息
        StringWriter writer = new StringWriter();
        PrintWriter printWriter= new PrintWriter(writer);
        error.printStackTrace(printWriter);
        String msg = "-Error: "+error;
        String msg2 = writer.toString().substring(0, 650);
        ws.sendUsageFlow(qt.setJson("cn", msg), msg2, "wsError", "WS");
//        if (uId.equals("6256789ae1908c03460f906f")) {
//            qt.setRDSet(Ws.ws_mq_prefix,uId+":onError",error.getMessage(),600L);
//        }
    }
    /**
     * 实现服务器主动推送 XXX only did aes encrypt on stringMap
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    //TODO ZJ 改名为encryptSendMsg
    private synchronized void sendMessage(JSONObject stringMap, String key, boolean isEncrypt) {
        if (isEncrypt) {
            //用前端的公钥来解密AES的key，并转成Base64
            try {
                // 使用前端公钥加密key
                String aesKey = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(key.getBytes()
                                , WebSocketUserServer.loginPublicKeyList.get(this.thisUser).get(this.thisClient)));
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
            // 发送返回数据 ******* send HERE
            this.session.getBasicRemote().sendText(JSON.toJSONString(stringMap));
        } catch (IOException e) {
            System.out.println("sendMessage出现错误");
        }
    }

    /**
     * 根据key加密logContent数据
     * @param logContent	发送的日志数据
     * @param key	AES
     * @return java.util.Map<java.lang.String,java.lang.String>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/11/28 16:12
     */
    //TODO ZJ 合并进prepareMsg
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
        try {
            //TODO ZJ 1. id_Us -> 在我这个服务 - sendMessage / 不在就只要 ws.sendWS(log)
            JSONObject map = JSONObject.parseObject(message);
            System.out.println("收到ws消息:");
            System.out.println(map);
            if (null != map) {
                String id = map.getString("id");
                if ("2".equals(id)) {
                    // 加密logContent数据
                    JSONObject stringMap = new JSONObject();
                    stringMap.put("key","2");
                    stringMap.put("en",false);
                    this.sendMessage(stringMap,null,false); //TODO ZJ 不加密干嘛用sendMessage
                } else {
                    // 调用解密并且发送信息方法
                    LogFlow logData = RsaUtil.encryptionSend(map, WebSocketUserServer.keyJava.get(this.thisUser).get(this.thisClient)
                            .getString("privateKeyJava"));
                    System.out.println(JSON.toJSONString(logData));

                    // 这个是特殊条件: subType == token
                    if (null!=logData.getSubType() && null!=logData.getLogType()
                            && "token".equals(logData.getSubType())
                            && "usageflow".equals(logData.getLogType())) {
                        try {
                            JSONObject data = logData.getData();
                            System.out.println("请求 RT2 api:");
                            String newToken = refreshToken2(logData.getId_U(), logData.getId_C(),data.getString("token")
                                    ,data.getString("refreshTokenJiu"),data.getString("clientType"));

                            String client = data.getString("clientType");
                            if (null == client) {
                                return;
                            }
                            if (null != newToken) {

                                data.put("tokenThis",newToken);
                                logData.setData(data);
                                logData.getData().remove("refreshTokenJiu");
                                logData.setId_Us(qt.setArray(logData.getId_U()));
                                logData.setSubType("mut_token");
                                logData.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

                            } else {
                                logData = new LogFlow();
                                logData.setId_U(logData.getId_U());
                                logData.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                                logData.setId_Us(qt.setArray(logData.getId_U()));
                                logData.setLogType("msg");
                                logData.setSubType("refreshTokenErr");
                                JSONObject dataNew = new JSONObject();
                                dataNew.put("client",client);
                                logData.setData(dataNew);
                                logData.setZcndesc("refreshToken为空!");
                            }
                            if (WebSocketUserServer.webSocketSet.containsKey(logData.getId_U())
                                    && null!=WebSocketUserServer.clients.get(logData.getId_U()).get(client)
                            ) {
                                //TODO ZJ put this into sendMessage?
                                //每次响应之前随机获取AES的key，加密data数据
                                String key = AesUtil.getKey();
                                // 加密logContent数据
                                JSONObject stringMap = aes(logData,key);
                                stringMap.put("en",true);
//                                String onlyId = WebSocketUserServer.clients.get(logData.getId_U()).get(client);
                                WebSocketUserServer.webSocketSet.get(logData.getId_U()).get(client).sendMessage(stringMap,key,true);
                                ////////////////////////////
                            }
                        } catch (Exception e){
                            System.out.println("这里出现异常:"+e.getMessage());
                            e.printStackTrace();
                        }
                    } else
                    {
                        logData.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                        // 调用主websocket发送日志核心方法
                        sendLogCore(logData,false);
                    }
                }
            }
        } catch (Exception ex){
            System.out.println("接收消息:出现异常:"+ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * MQ消息接收方法
     * @param msg	接收的消息
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/22
     * ****MQ*****
     */
    @Override
    public void onMessage(String msg) {
        LogFlow logContent = qt.jsonTo(JSONObject.parseObject(msg), LogFlow.class);
//        System.out.println("收到MQ消息:");
//        System.out.println(JSON.toJSONString(logContent));
        sendLogCore(logContent,true);
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
     * ws关闭执行方法,清理当前端
     * @param uId	当前连接用户编号
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/23
     */
    //TODO ZJ 太多closeWS了, 这样对于前端就是突然断线, 字段放redis
    private static synchronized void closeWS(String uId,String client,String appId){
        if (null != WebSocketUserServer.webSocketSet.get(uId)) {
//            String onlyIdThis = getOnlyId(uId, client);
            if (null!=WebSocketUserServer.appIds.get(uId)
                    && null!=WebSocketUserServer.appIds.get(uId).get(client)
                    && WebSocketUserServer.appIds.get(uId).get(client).equals(appId)) {
                JSONObject mqKey = ws.getRDInfo(uId);
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
                //TODO ZJ remove redis 就好, 可以看到我在哪个ws 微服务吗?
                if (null != WebSocketUserServer.webSocketSet.get(uId).get(client)) {
                    try {
                        WebSocketUserServer.webSocketSet.get(uId).get(client).session.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    WebSocketUserServer.webSocketSet.get(uId).remove(client);
                }
                if (null != WebSocketUserServer.clients.get(uId).get(client)) {
                    WebSocketUserServer.clients.get(uId).remove(client);
                }
                if (null != WebSocketUserServer.appIds.get(uId).get(client)) {
                    WebSocketUserServer.appIds.get(uId).remove(client);
                }
                if (null != WebSocketUserServer.loginPublicKeyList.get(uId).get(client)) {
                    WebSocketUserServer.loginPublicKeyList.get(uId).remove(client);
                }
                if (null != WebSocketUserServer.keyJava.get(uId).get(client)) {
                    // 删除钥匙
                    WebSocketUserServer.keyJava.get(uId).remove(client);
                }
                if (WebSocketUserServer.webSocketSet.get(uId).size() == 0) {
                    WebSocketUserServer.webSocketSet.remove(uId);
                    WebSocketUserServer.clients.remove(uId);
                    WebSocketUserServer.appIds.remove(uId);
                }
                JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, uId);  //TODO ZJ qt.delRDHashItem();
                if (null != rdInfo && rdInfo.size() > 0 && null != rdInfo.getJSONObject(client)) {
                    rdInfo.getJSONObject(client).remove("mqKey");
                    qt.setRDSet(Ws.ws_mq_prefix,uId,JSON.toJSONString(rdInfo),6000L);
                }
            } else {
                System.out.println("旧appId客户端关闭:"+uId+", client:"+client);
            }
        } else {
            System.out.println("旧appId客户端关闭:"+uId+", client:"+client);
        }
    }

    /**
     * 主websocket发送日志核心方法
     * @param logContent	日志信息
     * @param isMQ	是否是mq
     * @author tang
     * @date 创建时间: 2023/9/2
     * @ver 版本号: 1.0.0
     * 1. from WS 自己, 2. from MQ, 3. from ws.sendWS(java)
     */ // 分开 为 sendLogInThis, sendLogMQ,
    //TODO ZJ *****其实 MQ 是精准, WS内发也是精准, 你只要检查
    // 1. if 在+ isMQ, 发WS 2.不在+!isMQ, qt.sendWS
    private static void sendLogCore(LogFlow logContent,boolean isMQ){
//        JSONObject pushUserOld = null;
        // 判断不是mq
        if (!isMQ) {
            // 获取发送用户列表
            ws.setAppIds(logContent);
        }
//        else {
////            JSONObject data = logContent.getData();
////            if (null != data && null != data.getJSONObject("pushUsers")) {
////                pushUserOld = data.getJSONObject("pushUsers");
////            }
//        }
        // 判断是下线信息
        boolean isOffline = "msg".equals(logContent.getLogType()) && "Offline".equals(logContent.getSubType());
        // 判断发送用户列表不为空
        if (logContent.getId_Us().size() > 0) {
            JSONArray id_Us = logContent.getId_Us();
            System.out.println("群消息:"+JSON.toJSONString(id_Us));
            // 存储发送mq信息
            JSONObject mqGroupId = new JSONObject();
            // 存储需要推送的用户列表
//            JSONObject pushUserObj = new JSONObject();
            JSONArray pushUserObj = new JSONArray();
            // IF rdInfo then it is online, send WS, else send Push
            for (int i = 0; i < id_Us.size(); i++) {
                String id_UNew = id_Us.getString(i);
                // 获取redis信息
                JSONObject rdInfo = ws.getRDInfo(id_UNew);
                // 判断redis信息为空
                if (null == rdInfo) {
                    // 添加推送
                    if (!pushUserObj.contains(id_UNew)) {
                        pushUserObj.add(id_UNew);
                    }
                    continue;
                }
                // 判断用户存在 TODO ZJ 改为 rdInfo.server == this server ID, 
                // TODO ZJ, 在这里删了已经WS 的idUs 再用 ws.sendLogCore 发其他(mq/push)
                if (WebSocketUserServer.webSocketSet.containsKey(id_UNew)) {

                    for (String client : WebSocketUserServer.clients.get(id_UNew).keySet()) {
//                        String onlyId = WebSocketUserServer.clients.get(id_UNew).get(client);
                        //每次响应之前随机获取AES的key，加密data数据
                        //TODO ZJ 全改为 -encryptMsg
                        String key = AesUtil.getKey();
                        // 加密logContent数据
                        JSONObject stringMap = aes(logContent, key);
                        stringMap.put("en", true);
                        // 发送信息
                        WebSocketUserServer.webSocketSet.get(id_UNew).get(client).sendMessage(stringMap, key, true);
                        // 删除当前信息
                        rdInfo.remove(client); //TODO ZJ 1.MQ - ws.sendWS 2. WS - getRD - app.ws1, web.ws2, wx.ws1
                        ws.sendESOnly(logContent);
                    }

                }else
                    {

                        ws.sendWS(logContent);  // for (rdInfo ...
                    }

                    ws.sendWS();
                    for (String client : rdInfo.keySet()) {
                        // 获取端信息
                        JSONObject rdInfoData = rdInfo.getJSONObject(client);
                        // 判断当前端为app端
                        boolean isApp = client.equals("app");
                        // 判断是mq  //TODO ZJ ????? 超3个WS 服务, push 了好多次?
                        if (isMQ) {
                            // 判断为app端
                            if (isApp) {
                                // 添加到推送列表
//                                pushUserObj.put(id_UNew,0);
//                                addPushUser(pushUserObj,pushUserOld,id_UNew);
                                if (!pushUserObj.contains(id_UNew)) {
                                    pushUserObj.add(id_UNew);
                                }

                            }
                            // 调用清理ws方法 //TODO ZJ ??? 为什么关
                            closeWS(id_UNew,client,rdInfoData.getString("appId"));
                        } else {
                            // 判断端信息不为空
                            if (null != rdInfoData) {
                                // 获取mq标志
                                String mqKey = rdInfoData.getString("mqKey");
                                // 判断mq标志不为空，并且等于当前mq标志
                                if (null != mqKey && !"".equals(mqKey) && !mqKey.equals(WsId.topic + ":" + WsId.tap)) {
                                    if (null == mqGroupId.getJSONObject(mqKey)) {
                                        mqGroupId.put(mqKey,new JSONObject());
                                    }
                                    // 添加mq推送信息
                                    JSONObject mqIdAndAppId = mqGroupId.getJSONObject(mqKey);
                                    mqIdAndAppId.put(id_UNew,"");
                                    mqGroupId.put(mqKey,mqIdAndAppId);
                                } else {
                                    // 判断为app端
                                    if (isApp) {
                                        // 添加到推送列表
//                                        pushUserObj.put(id_UNew,0);
                                        pushUserObj.add(id_UNew);

                                    }
                                    // 调用清理ws方法  //TODO ZJ ??? 为什么关
                                    closeWS(id_UNew,client,rdInfoData.getString("appId"));
                                }
                            }
                        }
                    }
                } else {
                    // 判断是mq
                    if (isMQ) {
                        // 判断是下线
                        if (isOffline) {
                            continue;
                        }
                        for (String cli : rdInfo.keySet()) {
                            // 获取端信息
                            JSONObject cliInfo = rdInfo.getJSONObject(cli);
                            // 判断端信息不为空
                            if (null != cliInfo) {
                                String mqKey = cliInfo.getString("mqKey");
                                String thisWs = WsId.topic + ":" + WsId.tap;
                                if (null != mqKey && !"".equals(mqKey)) {
                                    if (thisWs.equals(mqKey)) {
                                        String appId = cliInfo.getString("appId");
                                        // 调用清理ws方法
                                        closeWS(id_UNew,cli,appId);
                                    }
                                }
                            }
                        }
                        // 添加到推送列表
//                        pushUserObj.put(id_UNew,0);
//                        addPushUser(pushUserObj,pushUserOld,id_UNew);
                        if (!pushUserObj.contains(id_UNew)) {
                            pushUserObj.add(id_UNew);
                        }
                    } else
                    {
                        // 存储判断不为当前mq，默认为当前mq
                        boolean isSendMq = false;
                        // 存储是否是app端
                        boolean isApp = false;
                        for (String cli : rdInfo.keySet()) {
                            // 获取端信息
                            JSONObject cliInfo = rdInfo.getJSONObject(cli);
                            if (null != cliInfo) {
                                String mqKey = cliInfo.getString("mqKey");
                                if (null != mqKey && !"".equals(mqKey)) {
                                    if (!mqKey.equals(WsId.topic + ":" + WsId.tap)) {
                                        // 设置不为当前mq
                                        isSendMq = true;
                                        if (null == mqGroupId.getJSONObject(mqKey)) {
                                            mqGroupId.put(mqKey,new JSONObject());
                                        }
                                        // 添加mq推送信息
                                        JSONObject mqIdAndAppId = mqGroupId.getJSONObject(mqKey);
                                        mqIdAndAppId.put(id_UNew,"");
                                        mqGroupId.put(mqKey,mqIdAndAppId);
                                    }
                                } else {
                                    // 判断是app
                                    if (cli.equals("app")) {
                                        isApp = true;
                                    }
                                }
                            }
                        }
                        // 判断不为当前mq，或者是app
                        if (!isSendMq || isApp) {
                            // 添加到推送列表
//                            pushUserObj.put(id_UNew,0);
                            pushUserObj.add(id_UNew);

                        }
                    }
                }
            }
            // 判断不是下线消息
            if (!isOffline) {
                // 判断不是mq
                if (!isMQ) {
                    // 发送日志
                    ws.sendESOnly(logContent);
                }
                // 调用发送推送和mq消息方法
                ws.sendMqOrPush(mqGroupId,pushUserObj,logContent);
            }
        }
    }

    /**
     * 获取对应用户对应端的唯一编号
     * @param id_U	用户编号
     * @param client	端
     * @return 返回结果: {@link String}
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    // TODO ZJ write to redis not here
    private static String getOnlyId(String id_U,String client){
        if (null == WebSocketUserServer.clients.get(id_U) || null == WebSocketUserServer.clients.get(id_U).get(client)) {
            return null;
        }
        return WebSocketUserServer.clients.get(id_U).get(client);
    }

    /**
     * 获取新token方法
     * @param id_U	用户编号
     * @param id_C	公司编号
     * @param token	编号
     * @param refreshToken	验证编号？
     * @param clientType	端
     * @return 返回结果: {@link String}
     * @author tang
     * @date 创建时间: 2023/9/4
     * @ver 版本号: 1.0.0
     */
    private static String refreshToken2(String id_U, String id_C, String token,String refreshToken, String clientType){
        // 判断 传过来的参数是否为空
        if (StringUtils.isNotEmpty(refreshToken)) {
            // 从redis 中查询出该用户的 refreshToken
            String refreshTokenResult = qt.getRDSetStr(clientType+"RefreshToken", refreshToken);
            // 判断 refreshToken 是否为空
            if (StringUtils.isNotEmpty(refreshTokenResult)) {
                // 不为空则判断 传过来的 refreshToken 是否与 redis中的 refreshToken一致
                if (refreshTokenResult.equals(id_U)) {
                    JSONObject rdSet = qt.getRDSet(clientType + "Token", token);
                    qt.errPrint("rdSet", null, rdSet, clientType, token);
                    if (rdSet == null) {
                        // 通过id_U查询该用户
                        User user = qt.getMDContent(id_U, qt.strList("info", "rolex.objComp."+ id_C), User.class);
                        token = oauth.setToken(user, id_C,
                                user.getRolex().getJSONObject("objComp").getJSONObject(id_C).getString("grpU"),
                                user.getRolex().getJSONObject("objComp").getJSONObject(id_C).getString("dep"),
                                clientType);
                        qt.setRDExpire(clientType+"RefreshToken",refreshToken,
                                clientType.equals("web")? 604800L : 3888000L);
                    } else {
                        qt.setRDSet(clientType + "Token", token, rdSet, 500L);
                    }
                    return token;
                }
            }
        }
        return null;
    }

//    /**
//     * 发送推送和mq消息方法
//     * @param mqGroupId	mq信息
//     * @param pushUserObj	推送用户列表
//     * @param logContent	日志信息
//     * @author tang
//     * @date 创建时间: 2023/9/4
//     * @ver 版本号: 1.0.0
//     */
//    public static void sendMqOrPush(JSONObject mqGroupId,JSONObject pushUserObj,LogFlow logContent){
//        if (logContent.getImp() >= 3 && pushUserObj.size() > 0) {
//            System.out.println("推送用户列表:");
//            System.out.println(JSON.toJSONString(pushUserObj.keySet()));
//            // 创建存储appId列表
//            JSONArray pushApps = new JSONArray();
//            // 创建存储padId列表
//            JSONArray pushPads = new JSONArray();
//            for (String id_U : pushUserObj.keySet()) {
//                // 获取redis信息
//                JSONObject mqKey = ws.getRDInfo(id_U);
//                // 存储判断redis有appId
//                boolean isApp = false;
//                // 存储判断redis有padId
//                boolean isPad = false;
//                if (null != mqKey) {
//                    JSONObject app = mqKey.getJSONObject("app");
//                    if (null != app) {
//                        pushApps.add(app.getString("appId"));
//                        // 设置有
//                        isApp = true;
//                    }
//                    JSONObject pad = mqKey.getJSONObject("pad");
//                    if (null != pad) {
//                        pushPads.add(pad.getString("appId"));
//                        // 设置有
//                        isPad = true;
//                    }
//                    if (isApp && isPad) {
//                        continue;
//                    }
//                }
//                JSONArray es = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
//                if (null != es && es.size() > 0 && null != es.getJSONObject(0)) {
//                    JSONObject esObj = es.getJSONObject(0);
//                    if (!isApp) {
//                        if (null != esObj.getString("id_APP") && !"".equals(esObj.getString("id_APP"))) {
//                            pushApps.add(esObj.getString("id_APP"));
//                        }
//                    }
//                    if (!isPad) {
//                        if (null != esObj.getString("id_Pad") && !"".equals(esObj.getString("id_Pad"))) {
//                            pushPads.add(esObj.getString("id_Pad"));
//                        }
//                    }
//                }
//            }
//            System.out.println("推送app:");
//            System.out.println(JSON.toJSONString(pushApps));
//            System.out.println("推送pad:");
//            System.out.println(JSON.toJSONString(pushPads));
//            if (pushApps.size() > 0) {
//                String wrdNUC = "小银【系统】";
//                JSONObject wrdNU = logContent.getWrdNU();
//                if (null != wrdNU && null != wrdNU.getString("cn")) {
//                    wrdNUC = wrdNU.getString("cn");
//                }
//                // 调用app发送推送方法
//                ws.push2(wrdNUC,logContent.getZcndesc(),pushApps);
//            }
//        }
//        if (mqGroupId.size() > 0) {
//            for (String mqKey : mqGroupId.keySet()) {
//                JSONObject mqIdArr = mqGroupId.getJSONObject(mqKey);
//                // 获取用户列表
//                logContent.setId_Us(JSONArray.parseArray(JSON.toJSONString(mqIdArr.keySet())));
//                JSONObject data = logContent.getData();
//                if (null == data) {
//                    data = new JSONObject();
//                }
//                data.put("pushUsers",pushUserObj);
//                logContent.setData(data);
//                // 发送mq信息
//                ws.sendMQ(mqKey,logContent);
//            }
//        }
//    }

//    public static void addPushUser(JSONObject pushUserObj,JSONObject pushUserOld,String id_U){
//
//
//        if (null != pushUserOld) {
//            boolean b = pushUserOld.containsKey(id_U);
//            if (!b) {
//                pushUserObj.put(id_U,0);
//            }
//        } else {
//            pushUserObj.put(id_U,0);
//        }
//    }
}