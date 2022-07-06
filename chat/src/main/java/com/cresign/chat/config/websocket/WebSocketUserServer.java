package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.common.ChatConstants;
import com.cresign.chat.service.LogService;
import com.cresign.chat.utils.AesUtil;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import io.netty.channel.ChannelHandler.Sharable;
import org.apache.commons.codec.binary.Base64;
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
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/23
 * ##version: 1.0.0
 * ##description: 群聊天websocket类
 */
@ServerEndpoint("/wsU/{uId}/{publicKey}/{token}")
@Component
@Sharable
public class WebSocketUserServer {

    @Override
    public boolean equals(Object obj){
        return super.equals(obj);
    }

    @Override
    public native int hashCode();

    /**
     * 用来存储每个产品的WebSocket单独连接
     */
    private static CopyOnWriteArraySet<WebSocketUserServer> webSocketSet;
    /**
     * 用来存储所有产品的连接
     */
    private static final Map<String,CopyOnWriteArraySet<WebSocketUserServer>> map = new HashMap<>(ChatConstants.HASH_MAP_DEFAULT_LENGTH);
    /**
     * 用来存储所有产品连接的在线人数
     */
    private static final JSONObject onlineCount = new JSONObject();
    /**
     * 用户的前端公钥Map集合
     */
    private static final Map<Session, String> loginPublicKeyList = new HashMap<>(ChatConstants.HASH_MAP_DEFAULT_LENGTH);
    /**
     * 后端聊天室对应公钥私钥Map集合
     */
    private static final Map<String,JSONObject> keyJava = new HashMap<>(ChatConstants.HASH_MAP_DEFAULT_LENGTH);
    private static LogService logService;
    private static DbUtils dbUtils;
    private static StringRedisTemplate redisTemplate1;
    // 注入的时候，给类的 service 注入
    @Autowired
    public void setWebSocketUserServer(DbUtils dbUtils,LogService logService, StringRedisTemplate redisTemplate1) {

        WebSocketUserServer.logService = logService;
        WebSocketUserServer.dbUtils = dbUtils;
        WebSocketUserServer.redisTemplate1 = redisTemplate1;
    }
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 用户自身编号
     */
    private String userId;

    /**
     * 连接建立成功调用的方法
     * ##Params: session   连接用户的session
     * ##Params: id   聊天室id
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uId") String uId
            ,@PathParam("publicKey") String publicKey,@PathParam("token") String token
    ) {
        // 根据prodID获取产品连接
        CopyOnWriteArraySet<WebSocketUserServer> writeArraySet = map.get(uId);
        // 获取当前用户session
        this.session = session;
        this.userId = uId;

        // 字符串转换
        String s = publicKey.replaceAll(",", "/");
        s = s.replaceAll("%20"," ");
        String replace = s.replace("-----BEGIN PUBLIC KEY-----", "");
        String replace2 = replace.replace("-----END PUBLIC KEY-----", "");

        // 设置前端公钥
        loginPublicKeyList.put(this.session,replace2);

        // 判断是否有prodID的连接
        if (writeArraySet==null){
            // 没有，则新建一个prodID的连接
            writeArraySet = new CopyOnWriteArraySet<>();

            // 获取后端私钥
            String privateKeyJava = RsaUtil.getPrivateKey();
            // 获取后端公钥
            String publicKeyJava = RsaUtil.getPublicKey();

            // 创建存储后端公钥私钥
            JSONObject keyM = new JSONObject();
            keyM.put("privateKeyJava",privateKeyJava);
            keyM.put("publicKeyJava",publicKeyJava);

            // 根据聊天室存储后端钥匙
            keyJava.put(uId,keyM);
        }
        // 加入set中
        writeArraySet.add(this);
        // 在线数加1
        addOnlineCount(uId);
        // 添加一个prodID连接
        map.put(uId,writeArraySet);

        System.out.println(token);
        String s1 = redisTemplate1.opsForValue().get(token);
        System.out.println("查询结果:"+s1);
        if (!uId.equals(s1)) {
            // 创建回应前端日志
            LogFlow logContent = LogFlow.getInstance();
            logContent.setId(uId);
            logContent.setZcndesc(null);
            logContent.setTmd(null);
            logContent.setId_C(null);
            logContent.setId_U(null);
            logContent.setLogType("keyErr");
//        logContent.setIpAddress(null);
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
        logContent.setId(uId);
        logContent.setZcndesc(null);
        logContent.setTmd(null);
        logContent.setId_C(null);
        logContent.setId_U(null);
        logContent.setLogType("key");
//        logContent.setIpAddress(null);
        logContent.setTzone(null);
        JSONObject data = new JSONObject();

        // 携带后端公钥
        data.put("publicKeyJava",keyJava.get(uId).getString("publicKeyJava"));
        logContent.setData(data);

        //每次响应之前随机获取AES的key，加密data数据
        String keyAes = AesUtil.getKey();

        // 根据AES加密数据
        JSONObject stringMap = aes(logContent,keyAes);
        stringMap.put("en",true);

        // 发送到前端
        this.sendMessage(stringMap,keyAes,true);

//        logService.addRedisOnlineH(uId);
//        System.out.println("ws开启输出:");

    }

    /**
     * 连接关闭调用的方法
     * ##Params: id    聊天室连接id
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @OnClose
    public synchronized void onClose(@PathParam("uId") String uId) {

        // 获取当前产品连接
        webSocketSet = map.get(uId);

        if (webSocketSet != null)
        {
            loginPublicKeyList.remove(this.session);

            // 删除用户自己的连接
            webSocketSet.remove(this);

            // 在线数减1
            subOnlineCount(uId);

            // 获取当前在线人数
            int onlineCount = getOnlineCount(uId);

            // 如果在线人数为0，则删除产品连接
            if (onlineCount==0){

                // 删除连接
                map.remove(uId);
                // 删除钥匙
                keyJava.remove(uId);
            }
        }

//        logService.delRedisOfflineH(uId);
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

        error.printStackTrace();
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
                        encryptByPublicKey(key.getBytes(), loginPublicKeyList.get(this.session)));

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

//    private static Allocator allT = Allocator.getAllocator();
//    static class Allocator{
//        private static Allocator allocator = new Allocator();
//
//        private Allocator(){}
//
//        public static Allocator getAllocator(){return allocator;}
//
//        private List<Object> als = new ArrayList<>();
//
//        // 一次性申请所有资源
//        synchronized boolean apply(Object from, Object to){
//            if (als.contains(from) || als.contains(to)) {
//                return false;
//            } else {
//                als.add(from);
//                als.add(to);
//            }
//            return true;
//        }
//
//        // 归还资源
//        synchronized void free(Object from, Object to){
//            als.remove(from);
//            als.remove(to);
//        }
//    }

    /**
     * 群发自定义消息
     * ##Params: logContent   发送消息
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    public synchronized static void sendLog(LogFlow logContent) {
//        while (!allT.apply(this, socket)) {;}
//        System.out.println("进入这里---sendLog");
        System.out.println("logContent:"+JSON.toJSONString(logContent));

        // 设置日志时间
        logContent.setTmd(DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        // 根据角色获取要发送的连接
        // 获取当前聊天室对象
        webSocketSet = map.get(logContent.getId_U());

        //每次响应之前随机获取AES的key，加密data数据
        String key = AesUtil.getKey();

        // 加密logContent数据
        JSONObject stringMap = aes(logContent,key);
        stringMap.put("en",true);

//        System.out.println(JSON.toJSONString(logContent));

        // 判断连接不为空
        if (webSocketSet != null) {
            // 循环遍历flowControl的用户连接，并向所有连接人发送信息
            // Case 1 C == CS, id_FC exist, FS ""
            // Case 2 C == CS, id_FC "", FS exist
            // Case 3 C != CS, id_FC + id_FS exist

            webSocketSet.forEach(socket -> {
//                System.out.println("进入这里:");
                String id_C = logContent.getId_C();
                String id_CS = logContent.getId_CS();

                String assetId = dbUtils.getId_A(id_C, "a-auth"); // assetServiceEdited
                System.out.println("id_c:"+id_C);
                if (!assetId.equals("none")) // possibly it's unreal comp
                {
                    Asset asset = dbUtils.getAssetById(assetId, Collections.singletonList("flowControl")); // assetServiceEdited
                    JSONObject flowControl = asset.getFlowControl();
                    JSONArray flowData = flowControl.getJSONArray("objData");

                    for (int i = 0; i < flowData.size(); i++) {
                        JSONObject roomSetting = flowData.getJSONObject(i);
                        String roomId = roomSetting.getString("id");
                        String logFlowId = logContent.getId();
                        if (roomSetting.getString("type").endsWith("SL")) {
                            logFlowId = logContent.getId_FS();
                        }
                        if (roomId.equals(logFlowId)) {
                            System.out.println("进入各FC 的loop: id=" + logContent.getId());
                            JSONArray objUser = roomSetting.getJSONArray("objUser");
                            System.out.println("objUser:" + JSON.toJSONString(objUser));
                            for (int j = 0; j < objUser.size(); j++) {
                                JSONObject roomSetting1 = objUser.getJSONObject(j);
                                String id_U = roomSetting1.getString("id_U");
                                if (!id_U.equals(socket.userId)) { // 一个是自己
                                    System.out.println(socket.userId + " 发送给其他人 " + id_U);
//                                WebSocketUserServer.sendLogCheckPush(roomSetting1, logContent,id_U,stringMap, key);
                                    CopyOnWriteArraySet<WebSocketUserServer> webSocketUserServers = map.get(id_U);

                                    // 判断连接不为空
                                    if (webSocketUserServers != null) {
                                        System.out.println(" 循环遍历产品连接，并向所有连接人发送信息");
                                        // 循环遍历产品连接，并向所有连接人发送信息
                                        webSocketUserServers.forEach(socketConnected -> sendWSLogUserOnline(socketConnected, stringMap, key));
                                    } else {
//                                        if (roomSetting1.getInteger("imp") <= logContent.getImp()) {
//                                            String id_client = roomSetting1.getString("id_APP");
//                                            JSONObject wrdNU = logContent.getWrdNU();
//                                            if (id_client != null && wrdNU != null) {
//                                                logService.sendPush(id_client, wrdNU.getString("cn"), logContent.getZcndesc());
//                                            }
//                                        }
                                    }
                                } else {
                                    System.out.println(socket.userId + " 发送给自己 " + id_U);
                                    WebSocketUserServer.sendWSLogUserOnline(socket, stringMap, key);
                                }
                            }
                        }
                    }
                }
                // IF two companies aren't the same, need to push to both companies
                if (id_CS != null && !id_C.equals(id_CS))
                {
                    String assetIdCS = dbUtils.getId_A(id_CS, "a-auth"); // assetServiceEdited
                    System.out.println("id_cs:"+id_CS+assetIdCS);
                    if (!assetIdCS.equals("none")) // possibly it's unreal comp
                    {
                    Asset assetCS = dbUtils.getAssetById(assetIdCS, Collections.singletonList("flowControl")); // assetServiceEdited
                    JSONObject flowControlCS = assetCS.getFlowControl();
                    JSONArray flowDataCS = flowControlCS.getJSONArray("objData");

                    for (int i = 0; i < flowDataCS.size(); i++) {
                        JSONObject roomSetting = flowDataCS.getJSONObject(i);
                        String roomId = roomSetting.getString("id");
                        String logFlowId = logContent.getId_FS();

                        if (roomId.equals(logFlowId)) {
                            System.out.println("进入各FC 的loop: id=" + logContent.getId());
                            JSONArray objUser = roomSetting.getJSONArray("objUser");
                            System.out.println("objUser:" + JSON.toJSONString(objUser));
                            for (int j = 0; j < objUser.size(); j++) {
                                JSONObject roomSetting1 = objUser.getJSONObject(j);
                                String id_U = roomSetting1.getString("id_U");
                                if (!id_U.equals(socket.userId)) { // 一个是自己
                                    System.out.println(socket.userId + " 发送给其他人 " + id_U);
//                                WebSocketUserServer.sendLogCheckPush(roomSetting1, logContent,id_U,stringMap, key);
                                    CopyOnWriteArraySet<WebSocketUserServer> webSocketUserServers = map.get(id_U);

                                    // 判断连接不为空
                                    if (webSocketUserServers != null) {
                                        System.out.println(" 循环遍历产品连接，并向所有连接人发送信息");
                                        // 循环遍历产品连接，并向所有连接人发送信息
                                        webSocketUserServers.forEach(socketConnected -> sendWSLogUserOnline(socketConnected, stringMap, key));
                                    } else {
//                                        if (roomSetting1.getInteger("imp") <= logContent.getImp()) {
//                                            String id_client = roomSetting1.getString("id_APP");
//                                            JSONObject wrdNU = logContent.getWrdNU();
//                                            if (id_client != null && wrdNU != null) {
//                                                logService.sendPush(id_client, wrdNU.getString("cn"), logContent.getZcndesc());
//                                            }
//                                        }
                                    }
                                } else {
                                    System.out.println(socket.userId + " 发送给自己 " + id_U);
                                    WebSocketUserServer.sendWSLogUserOnline(socket, stringMap, key);
                                }
                            }
                        }
                    }

                    }
                }
            });
        }
    }

    private synchronized static void sendWSLogUserOnline(WebSocketUserServer socket, JSONObject stringMap, String key){
            socket.sendMessage(stringMap,key,true);
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

//            if (!logContent.getLogType().getString(0).equals("msg")) {
//                logContent.setData(getLogDataJ(data));
//            }
//            stringMap.put("dataJ",JSON.toJSONString(logContent));
//            if (null != data.get(Constants.GET_COUNT)) {
//                data.remove("count");
//            }
//            logContent.setData(data);
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
                RsaUtil.encryptionSend(map,keyJava.get(id).getString("privateKeyJava"),logService);
            }
        }
    }

    /**
     * 根据prodID获取该连接的总在线人数
     * ##Params: key    公司id拼接角色
     * ##return:  总在线人数
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    private static synchronized int getOnlineCount(String key) {
        return onlineCount.getInteger(key);
    }

    /**
     * 根据prodID，把当前人数加一
     * ##Params: key    公司id拼接角色
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    private static synchronized void addOnlineCount(String key) {

        // 获取在线人数，如果为空则设置为0，不为空则直接拿出
        Integer nol = onlineCount.get(key)==null? 0 : onlineCount.getInteger(key);

        // 在线人数加一
        nol++;

        // 把加一后的在线人数设置回去
        onlineCount.put(key,nol);
    }

    /**
     * 根据prodID，把当前人数减一
     * ##Params: key    产品id
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    private static synchronized void subOnlineCount(String key) {

        // 获取在线人数
        Integer nol = onlineCount.getInteger(key);

        // 在线人数减一
        nol--;

        // 把减一后的在线人数设置回去
        onlineCount.put(key,nol);
    }
}