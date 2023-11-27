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

    /**
     * 用来根据用户编号存储每个用户的WebSocket连接信息
     */
    private static final Map<String, Map<String, WebSocketUserServer>> webSocketSet = new HashMap<>(16);
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
            this.thisUser = uId;
            this.thisClient = client; //wx/app/web
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
                JSONObject wsData = cliInfo.getJSONObject("wsData");
                if (null != wsData) {
                    // 获取mq编号
                    String mqKeyOld = wsData.getString("mqKey");
                    // 判断不为空
                    if (null != mqKeyOld) {
                        String frontEndPublicKey = wsData.getString("frontEndPublicKey");
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
                                    if (null != WebSocketUserServer.webSocketSet.get(uId)
                                            && null!=WebSocketUserServer.webSocketSet.get(uId).get(client)) {
                                        // 发送通知
                                        WebSocketUserServer.webSocketSet.get(uId).get(client)
                                                .encryptSendMsg(logData,true,frontEndPublicKey);
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
            }
            // 字符串转换
            String s = publicKey.replaceAll(",", "/");
            s = s.replaceAll("%20"," ");
            String replace = s.replace("-----BEGIN PUBLIC KEY-----", "");
            String frontEndPublicKey = replace.replace("-----END PUBLIC KEY-----", "");
            JSONObject wsData = new JSONObject();
            wsData.put("mqKey",mqKey);
            cliInfo.put("appId",appId); // mqKey appId ==
            wsData.put("frontEndPublicKey",frontEndPublicKey);
            // 获取后端私钥
            String privateKeyJava = RsaUtil.getPrivateKey();
            // 获取后端公钥
            String publicKeyJava = RsaUtil.getPublicKey();
            // 创建存储后端公钥私钥
            JSONObject javaKey = new JSONObject();
            javaKey.put("private",privateKeyJava);
            javaKey.put("public",publicKeyJava);
            wsData.put("javaKey",javaKey);
            if (WebSocketUserServer.webSocketSet.containsKey(uId)) {
                // 添加新端信息
                WebSocketUserServer.webSocketSet.get(uId).put(client,this);
            } else {
                // 创建端
                Map<String, WebSocketUserServer> map = new HashMap<>(16);
                map.put(client,this);
                WebSocketUserServer.webSocketSet.put(uId,map);
            }
            cliInfo.put("wsData",wsData);
            rdInfo.put(client,cliInfo);
            // 更新redis当前端信息
            qt.setRDSet(Ws.ws_mq_prefix,uId,JSON.toJSONString(rdInfo),6000L);
            System.out.println("----- ws打开 -----:" + ",uId:" + uId + ", 服务:"+mqKey+", 端:"+client);
            System.out.println("在线人数:"+getOnlineCount());
            System.out.println("当前用户在线-端-:"+JSON.toJSONString(getThisCli(rdInfo)));
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
            data.put("publicKeyJava", javaKey.getString("public"));
            logContent.setData(data);
            // 发送到前端
            this.encryptSendMsg(logContent,true,frontEndPublicKey);
        } catch (Exception e){
            System.out.println("出现异常:"+e.getMessage());
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
    }
    /**
     * 实现服务器主动推送 XXX only did aes encrypt on stringMap
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    //TODO ZJ 改名为encryptSendMsg
    private synchronized void encryptSendMsg(LogFlow logData,boolean isEncrypt,String frontEndPublicKey) {
        JSONObject stringMap = new JSONObject();
        if (isEncrypt) {
            //每次响应之前随机获取AES的key，加密data数据
            String key = AesUtil.getKey();
            // 加密logContent数据
            try {
                // 根据key加密logContent数据
                String data2 = AesUtil.encrypt(JSON.toJSONString(logData), key);
                // 添加到返回map
                stringMap.put("data",data2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            stringMap.put("en",true);
            //用前端的公钥来解密AES的key，并转成Base64
            try {
                // 使用前端公钥加密key
                String aesKey = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(key.getBytes()
                                , frontEndPublicKey));
                // 添加加密数据到返回集合
                stringMap.put("aesKey",aesKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            stringMap.put("key","2");
            stringMap.put("en",false);
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
                    this.encryptSendMsg(null,false,null); //TODO ZJ 不加密干嘛用sendMessage
                } else {
                    JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, this.thisUser);
                    if (null == rdInfo || null == rdInfo.getJSONObject(this.thisClient)
                            || null == rdInfo.getJSONObject(this.thisClient).getJSONObject("wsData")) {
                        return;
                    }
                    JSONObject cliInfo = rdInfo.getJSONObject(this.thisClient);
                    JSONObject wsData = cliInfo.getJSONObject("wsData");
                    JSONObject javaKey = wsData.getJSONObject("javaKey");
                    String frontEndPublicKey = wsData.getString("frontEndPublicKey");
                    // 调用解密并且发送信息方法
                    LogFlow logData = RsaUtil.encryptionSend(map, javaKey.getString("private"));
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
                            if (WebSocketUserServer.webSocketSet.containsKey(logData.getId_U())) {
                                WebSocketUserServer.webSocketSet.get(logData.getId_U())
                                        .get(client).encryptSendMsg(logData,true,frontEndPublicKey);
                            }
                        } catch (Exception e){
                            System.out.println("这里出现异常:"+e.getMessage());
                            e.printStackTrace();
                        }
                    } else
                    {
                        logData.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//                        JSONObject rdInfoUser = qt.getRDSet(Ws.ws_mq_prefix, this.thisUser);
//                        if (null != rdInfoUser && null != rdInfoUser.getJSONObject(this.thisClient)) {
//                            if (null != rdInfoUser.getJSONObject(this.thisClient).getJSONObject("wsData")) {
//                                JSONObject wsDataUser = rdInfoUser.getJSONObject(this.thisClient).getJSONObject("wsData");
//                                this.encryptSendMsg(logData,true,wsDataUser.getString("frontEndPublicKey"));
//                            }
//                        }
                        // 获取发送用户列表
                        ws.setId_UsByFC(logData);
                        ws.setAppIds(logData);
                        JSONArray id_Us = logData.getId_Us();
                        JSONObject rdInfoMap = new JSONObject();
                        JSONArray id_UsWei = new JSONArray();
                        for (int i = 0; i < id_Us.size(); i++) {
                            String id_U = id_Us.getString(i);
                            if (WebSocketUserServer.webSocketSet.containsKey(id_U)) {
                                Map<String, WebSocketUserServer> userServerMap = WebSocketUserServer.webSocketSet.get(id_U);
                                boolean isChaRd = false;
                                JSONObject rdInfoUser = null;
                                for (String cli : userServerMap.keySet()) {
                                    WebSocketUserServer thisWebSocket = userServerMap.get(cli);
                                    if (!isChaRd) {
                                        rdInfoUser = qt.getRDSet(Ws.ws_mq_prefix, thisWebSocket.thisUser);
                                        if (null != rdInfoUser){
                                            isChaRd = true;
                                        }
                                    }
                                    if (isChaRd && null != rdInfoUser.getJSONObject(thisWebSocket.thisClient)) {
                                        if (null != rdInfoUser.getJSONObject(thisWebSocket.thisClient).getJSONObject("wsData")) {
                                            JSONObject wsDataUser = rdInfoUser.getJSONObject(thisWebSocket.thisClient).getJSONObject("wsData");
                                            thisWebSocket.encryptSendMsg(logData,true,wsDataUser.getString("frontEndPublicKey"));
                                        }
                                    }
                                }
                                if (isChaRd) {
                                    rdInfoMap.put(id_U,rdInfoUser);
                                } else {
                                    rdInfoMap.put(id_U,null);
                                }
                            } else {
                                id_UsWei.add(id_U);
                            }
                        }

                        // 调用主websocket发送日志核心方法
                        ws.sendWSCore(logData,rdInfoMap,id_UsWei,WsId.topic + ":" + WsId.tap);
                        ws.sendESOnly(logData);
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
//        sendLogCore(logContent,true);

//        // 判断是下线信息
//        boolean isOffline = "msg".equals(logContent.getLogType()) && "Offline".equals(logContent.getSubType());
        // 判断发送用户列表不为空
        if (logContent.getId_Us().size() > 0) {
            JSONArray id_Us = logContent.getId_Us();
            System.out.println("群mq消息:"+JSON.toJSONString(id_Us));
            // IF rdInfo then it is online, send WS, else send Push
            for (int i = 0; i < id_Us.size(); i++) {
                String id_UNew = id_Us.getString(i);
                // 获取redis信息
                JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, id_UNew);

                qt.errPrint("all data", rdInfo,id_Us, msg);

                // 判断redis信息为空
                if (null == rdInfo) {
                    continue;
                }
                // 判断用户存在 TODO ZJ 改为 rdInfo.server == this server ID,
                // TODO ZJ, 在这里删了已经WS 的idUs 再用 ws.sendLogCore 发其他(mq/push)
                if (WebSocketUserServer.webSocketSet.containsKey(id_UNew)) {
                    for (String client : WebSocketUserServer.webSocketSet.get(id_UNew).keySet()) {
                        JSONObject cliInfo = rdInfo.getJSONObject(client);
                        if (null == cliInfo || null == cliInfo.getJSONObject("wsData")) {
                            continue;
                        }
                        JSONObject wsData = cliInfo.getJSONObject("wsData");
                        String frontEndPublicKey = wsData.getString("frontEndPublicKey");
                        //每次响应之前随机获取AES的key，加密data数据
                        WebSocketUserServer.webSocketSet.get(id_UNew)
                                .get(client).encryptSendMsg(logContent, true,frontEndPublicKey);
                    }
                }
            }
        }
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
            JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, uId);
            if (null != rdInfo && null != rdInfo.getJSONObject(client)) {
                JSONObject cliInfo = rdInfo.getJSONObject(client);
                if (null != cliInfo.getString("wsData")) {
                    JSONObject wsData = cliInfo.getJSONObject("wsData");
                    if (!wsData.getString("mqKey").equals(WsId.topic + ":" + WsId.tap)) {
                        System.out.println("旧appId客户端关闭-别的客户端在线:"+uId+", client:"+client);
                        return;
                    }
                }
                String appIdRd = cliInfo.getString("appId");
                if (null!=appIdRd && appIdRd.equals(appId)) {
//                JSONObject mqKey = ws.getRDInfo(uId);

                    System.out.println("进入清理ws:-"+uId+", client:"+client);
                    //TODO ZJ remove redis 就好, 可以看到我在哪个ws 微服务吗?
                    if (null != WebSocketUserServer.webSocketSet.get(uId).get(client)) {
                        WebSocketUserServer.webSocketSet.get(uId).remove(client);
                    }
                    if (WebSocketUserServer.webSocketSet.get(uId).size() == 0) {
                        WebSocketUserServer.webSocketSet.remove(uId);
                    }
                    rdInfo.getJSONObject(client).remove("wsData");
                    qt.setRDSet(Ws.ws_mq_prefix,uId,JSON.toJSONString(rdInfo),6000L);
                    System.out.println("清理后-在线端:"+JSON.toJSONString(getThisCli(rdInfo)));
                } else {
                    System.out.println("旧appId客户端关闭:"+uId+", client:"+client);
                }
            } else {
                System.out.println("旧appId客户端关闭:"+uId+", client:"+client);
            }
        } else {
            System.out.println("旧appId客户端关闭:"+uId+", client:"+client);
        }
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

    public static JSONArray getThisCli(JSONObject rdInfo){
        JSONArray onLineClient = new JSONArray();
        for (String thisCli : rdInfo.keySet()) {
            JSONObject thisCliInfo = rdInfo.getJSONObject(thisCli);
            if (null != thisCliInfo.getJSONObject("wsData")) {
                onLineClient.add(thisCli);
            }
        }
        return onLineClient;
    }
}