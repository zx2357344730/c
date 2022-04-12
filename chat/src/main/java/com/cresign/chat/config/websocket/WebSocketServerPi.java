package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.utils.AesUtil;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.LogFlow;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @ClassName WebSocketServer
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/3/1 11:28
 * @Version 1.0.0
 */
@ServerEndpoint("/pi/{id_C}/{name}/{publicKey}")
@Component
//@Sharable
public class WebSocketServerPi {

    private int z = 0;

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 用来存储每个产品的WebSocket单独连接
     */
    private static CopyOnWriteArraySet<WebSocketServerPi> webSocketSet;

    /**
     * 用来存储所有产品连接的在线人数
     */
    private static final JSONObject map2 = new JSONObject();

    /**
     * 用户的前端公钥Map集合
     */
    private static final Map<Session, String> loginPublicKeyList = new HashMap<>(16);

    /**
     * 后端聊天室对应公钥私钥Map集合
     */
    private static final Map<String,JSONObject> keyJava = new HashMap<>(16);

    private static final Map<String, JSONObject> piData = new HashMap<>(16);

    /**
     * 连接建立成功调用的方法
     * ##Params: session   连接用户的session
     * ##Params: id   聊天室id
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @OnOpen
    public void onOpen(Session session,@PathParam("name") String name,@PathParam("publicKey") String publicKey){
        System.out.println("进入ws打开:");
        // 判断是否有prodID的连接
        if (webSocketSet==null) {
            System.out.println("进入新建:");
            // 没有，则新建一个prodID的连接
            webSocketSet = new CopyOnWriteArraySet<>();
        }
        // 获取后端私钥
        String privateKeyJava = RsaUtil.getPrivateKey();
        // 获取后端公钥
        String publicKeyJava = RsaUtil.getPublicKey();

        // 创建存储后端公钥私钥
        JSONObject keyM = new JSONObject();
        keyM.put("privateKeyJava",privateKeyJava);
        keyM.put("publicKeyJava",publicKeyJava);
        // 根据聊天室存储后端钥匙
        keyJava.put(name,keyM);
        // 获取当前用户session
        this.session = session;
        webSocketSet.add(this);
        this.z = 1;
        // 在线数加1
        addOnlineCount(name);
        System.out.println("打卡-java-WebSocket!,连接用户:"+name);

        // 字符串转换
        String s = publicKey.replaceAll(",", "/");

        // 设置前端公钥
        loginPublicKeyList.put(this.session,s);

        // 创建回应前端日志
        LogFlow log1 = LogFlow.getInstance();
        log1.setId("uId");
        log1.setZcndesc(null);
        log1.setTmd(null);
        log1.setId_C(null);
        log1.setId_U(null);
        log1.setLogType("key");
        log1.setTzone(null);
        JSONObject data = new JSONObject();

        // 携带后端公钥
        data.put("publicKeyJava",keyJava.get(name).getString("publicKeyJava"));
        log1.setData(data);

        WebSocketServerPi.sendInfo(log1);
    }

    /**
     * 连接关闭调用的方法
     * ##Params: id    聊天室连接id
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @OnClose
    public void onClose(@PathParam("name") String name){
        System.out.println("关闭-java-WebSocket");
        if (this.z == 2) {
            return;
        }
        // 在线数减1
        subOnlineCount(name);
        webSocketSet.remove(this);
        loginPublicKeyList.remove(this.session);
        // 删除钥匙
        keyJava.remove(name);
    }

    /**
     * websocket异常回调类
     * ##Params: error 异常信息
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    @OnError
    public void onError(Throwable error,@PathParam("name") String name) {

        // 输出错误信息

//        error.printStackTrace();
        this.z = 2;
        System.out.println("输出异常信息:");
        System.out.println("name:"+name+",在线人数:"+getOnlineCount(name));
        // 在线数减1
        subOnlineCount(name);
        webSocketSet.remove(this);
        loginPublicKeyList.remove(this.session);
        // 删除钥匙
        keyJava.remove(name);
        System.out.println(error.getMessage());
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
        JSONObject jsonObject = JSON.parseObject(message);
        encryptionSend(jsonObject,jsonObject.getString("rname"));
    }

    /**
     * 群发自定义消息
     * ##Params: log1   发送消息
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    public static void sendInfo(LogFlow log) {
        // 设置日志时间
        log.setTmd(DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
        //每次响应之前随机获取AES的key，加密data数据
        String key = AesUtil.getKey();
        // 加密log1数据
        JSONObject stringMap = aes(key,log);
        // 判断连接不为空
        if (webSocketSet != null) {
            // 循环遍历产品连接，并向所有连接人发送信息
            webSocketSet.forEach(item -> item.sendMessage(stringMap,key));
        }
    }

    /**
     * 实现服务器主动推送
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    private void sendMessage(JSONObject stringMap,String key) {
        //用前端的公钥来解密AES的key，并转成Base64
        try {
            // 使用前端公钥加密key
            String aesKey2 = Base64.encodeBase64String(RsaUtil.
                    encryptByPublicKey(key.getBytes(), loginPublicKeyList.get(this.session)));
            // 添加加密数据到返回集合
            stringMap.put("aesKey",aesKey2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 向前端推送log消息
        try {
            // 发送返回数据
            this.session.getBasicRemote().sendText(JSON.toJSONString(stringMap));
        } catch (IOException e) {
            System.out.println("出现错误");
        }
    }

    /**
     * 根据key加密log1数据
     * ##Params: log1	发送的日志数据
     * ##Params: key	AES
     * ##return: java.util.Map<java.lang.String,java.lang.String>  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/11/28 16:12
     */
    private static JSONObject aes(String key,LogFlow log){
        // 创建返回存储map
        JSONObject stringMap = new JSONObject();
        try {
            // 根据key加密log1数据
            String data2 = AesUtil.encrypt(JSON.toJSONString(log), key);

            // 添加到返回map
            stringMap.put("data",data2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回结果
        return stringMap;
    }

    public static void encryptionSend(JSONObject map,String rname){
        // AES加密后的数据
        String data = map.getString("data");
        // 后端RSA公钥加密后的AES的key
        String aesKey = map.getString("aesKey");
        // 后端私钥解密的到AES的key
        try {
            byte[] plaintext = RsaUtil.decryptByPrivateKey(Base64.decodeBase64(aesKey)
                    , keyJava.get(rname).getString("privateKeyJava"));
            aesKey = new String(plaintext);
            //AES解密得到明文data数据
            String decrypt = AesUtil.decrypt(data, aesKey);

            LogFlow log = JSONObject.parseObject(decrypt,LogFlow.class);
            System.out.println("消息转换之后:");
            System.out.println(JSON.toJSONString(log));
            String type = log.getLogType();
            if ("piTimer".equals(type)) {
                System.out.println("pi_type: 2 = pi-数据集-发送pi按键数据");
                JSONObject jsonObject = piData.get(rname);
                JSONObject data_all = log.getData();
                data_all.keySet().forEach(k -> {
                    JSONArray jsonArray1 = data_all.getJSONArray(k);
                    JSONObject gpIoObj = jsonObject.getJSONObject(k);
                    JSONArray li = gpIoObj.getJSONArray("li");
                    if (null == li) {
                        li = new JSONArray();
                    }
                    li.addAll(jsonArray1);
                    gpIoObj.put("li",li);
                    jsonObject.put(k,gpIoObj);
                });
                piData.put(rname,jsonObject);
                System.out.println("输出piData:");
                System.out.println(JSON.toJSONString(piData));
            } else if ("piCallback".equals(type)) {
                System.out.println("pi_type: 0 = pi-打招呼-成功获取后端公钥");
            } else if ("pi_info_put".equals(type)) {
                System.out.println("添加gpIo-info信息:");
                JSONObject jsonObject;
                jsonObject = piData.get(rname) == null ? new JSONObject() : piData.get(rname);
                JSONObject data1 = log.getData();
                JSONObject data2 = data1.getJSONObject("data");
                String gpIo = data2.getString("gpIo");
                JSONObject gpIoObj = jsonObject.getJSONObject(gpIo);
                if (null == gpIoObj) {
                    gpIoObj = new JSONObject();
                }
                gpIoObj.put("info",log.getData());
                jsonObject.put(gpIo,gpIoObj);
                piData.put(rname,jsonObject);
                System.out.println("输出piData:");
                System.out.println(JSON.toJSONString(piData));
            } else if ("pi_info_del".equals(type)) {
                System.out.println("删除gpIo-info信息:");
                JSONObject jsonObject;
                jsonObject = piData.get(rname);
                if (null == jsonObject) {
                    return;
                }
                String gpIo = log.getData().getString("gpIo");
                JSONObject gpIoObj = jsonObject.getJSONObject(gpIo);
                gpIoObj.put("info",new JSONObject());
                jsonObject.put(gpIo,gpIoObj);
                piData.put(rname,jsonObject);
                System.out.println("输出piData:");
                System.out.println(JSON.toJSONString(piData));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        return map2.getInteger(key);
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
        Integer nol = map2.get(key)==null? 0:map2.getInteger(key);

        // 在线人数加一
        nol++;

        // 把加一后的在线人数设置回去
        map2.put(key,nol);
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
        Integer nol = map2.getInteger(key);

        // 在线人数减一
        nol--;

        // 把减一后的在线人数设置回去
        map2.put(key,nol);
    }
}
