package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.utils.AesUtil;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.tools.pojo.po.LogFlow;
import io.netty.channel.ChannelHandler.Sharable;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tangzejin
 * @ver 1.0.0
 * ##description: 微信登录websocket
 */
@ServerEndpoint("/wsU/login/{id_U}/{publicKey}/{client}")
@Component
@Sharable
public class WebSocketLoginServer {

    @Override
    public boolean equals(Object obj){
        return super.equals(obj);
    }

    @Override
    public native int hashCode();

    /**
     * 用来存储所有产品的连接
     */
    private static final Map<String,WebSocketLoginServer> map = new HashMap<>(16);
    private static final Map<String,JSONObject> keyJava = new HashMap<>(16);
    private static final Map<String,String> keyWeb = new HashMap<>(16);

    /**
     * 用来存储所有产品连接的在线人数
     */
    private static final Map<String,Integer> onlineCount = new HashMap<>(16);

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    private String thisId;

    /**
     * 连接建立成功调用的方法
     * @param session	连接用户的session
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/5 10:49
     */
    @OnOpen
    public void onOpen(Session session
            ,@PathParam("id_U") String id_U,@PathParam("publicKey") String publicKey
            ,@PathParam("client")String client) {
        this.thisId = id_U+"_"+client;
        // 获取当前用户session
        this.session = session;
        // 字符串转换
        String s = publicKey.replaceAll(",", "/");
        s = s.replaceAll("%20"," ");
        String replace = s.replace("-----BEGIN PUBLIC KEY-----", "");
        String frontEndPublicKey = replace.replace("-----END PUBLIC KEY-----", "");
        keyWeb.put(this.thisId,frontEndPublicKey);
        // 获取后端私钥
        String privateKeyJava = RsaUtil.getPrivateKey();
        // 获取后端公钥
        String publicKeyJava = RsaUtil.getPublicKey();
        // 创建存储后端公钥私钥
        JSONObject javaKey = new JSONObject();
        javaKey.put("private",privateKeyJava);
        javaKey.put("public",publicKeyJava);
        keyJava.put(this.thisId,javaKey);
        // 加入set中
        map.put(this.thisId,this);
        // 在线数加1
        addOnlineCount(this.thisId);
        System.out.println("----- login-ws打开 -----:" + ",id_U:" + id_U+",端:"+client);
        // 创建回应前端日志
        LogFlow logContent = LogFlow.getInstance();
        logContent.setId(null);
        logContent.setZcndesc(null);
        logContent.setTmd(null);
        logContent.setId_C(null);
        logContent.setId_U(id_U);
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
//        // 加密logContent数据
//        JSONObject stringMap = new JSONObject();
//        stringMap.put("key",id);
//        stringMap.put("en",false);
//        this.sendMessage(stringMap);
    }

    /**
     * 连接关闭调用的方法
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/5 10:50
     */
    @OnClose
    public void onClose(@PathParam("id_U") String id_U) {
        // 在线数减1
        subOnlineCount(this.thisId);
        // 删除连接
        map.remove(this.thisId);
        keyWeb.remove(this.thisId);
        keyJava.remove(this.thisId);
    }

    /**
     * 连接异常回调方法
     * @param error	异常信息
     * @return void    返回结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/5 10:51
     */
    @OnError
    public void onError(Throwable error) {
        // 输出错误信息
        System.out.println("websocket-login出现错误:"+error.getMessage());
    }

    /**
     * 实现服务器主动推送
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/5 10:51
     */
    private synchronized void sendMessage(JSONObject stringMap) {
        // 向前端推送log消息
        try {
            this.session.getBasicRemote().sendText(JSON.toJSONString(stringMap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送自定义消息
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/5 10:52
     */
    public static void sendInfo(String id_U,String client,JSONObject infoData) {
        String thisId = id_U+"_"+client;
        // 判断连接不为空
        if (map.containsKey(thisId)) {
            // 创建回应前端日志
            LogFlow logContent = LogFlow.getInstance();
            logContent.setId(null);
            logContent.setZcndesc(null);
            logContent.setTmd(null);
            logContent.setId_C(null);
            logContent.setId_U(id_U);
            logContent.setLogType("successLogin");
            logContent.setTzone(null);
            ///////////////////////...
            JSONObject data = new JSONObject();
            data.put("client",client);
            // 携带后端公钥
            data.put("infoData", infoData.getJSONObject("infoData"));
            logContent.setData(data);
            // 循环遍历产品连接，并向所有连接人发送信息
//            map.get(thisId).sendMessage(infoData);
            map.get(thisId).encryptSendMsg(logContent,true,keyWeb.get(thisId));
        }
    }

    /**
     * 根据prodID获取该连接的总在线人数
     * @param key	键
     * @return int    返回结果总在线人数
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/5 10:53
     */
    private static synchronized int getOnlineCount(String key) {
        return onlineCount.get(key);
    }

    /**
     * 根据prodID，把当前人数加一
     * @param key	键
     * @return void  返回结果:总在线人数
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/5 10:54
     */
    private static synchronized void addOnlineCount(String key) {

        // 获取在线人数，如果为空则设置为0，不为空则直接拿出
        Integer nol = onlineCount.get(key)==null?  0 :onlineCount.get(key);

        // 在线人数加一
        nol++;

        // 把加一后的在线人数设置回去
        onlineCount.put(key,nol);
    }

    /**
     * 根据prodID，把当前人数减一
     * @param key	键
     * @return void  返回结果:
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/5 10:54
     */
    private static synchronized void subOnlineCount(String key) {

        // 获取在线人数
        Integer nol = onlineCount.get(key);

        // 在线人数减一
        nol--;

        // 把减一后的在线人数设置回去
        onlineCount.put(key,nol);
    }

    private synchronized void encryptSendMsg(LogFlow logData,boolean isEncrypt,String frontEndPublicKey) {
        try {
            JSONObject stringMap = new JSONObject();
            if (isEncrypt) {
                //每次响应之前随机获取AES的key，加密data数据
                String key = AesUtil.getKey();
                // 根据key加密logContent数据
                String data2 = AesUtil.encrypt(JSON.toJSONString(logData), key);
                // 添加到返回map
                stringMap.put("data", data2);
                stringMap.put("en", true);
                //用前端的公钥来解密AES的key，并转成Base64
                // 使用前端公钥加密key
                String aesKey = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(key.getBytes()
                        , frontEndPublicKey));
                // 添加加密数据到返回集合
                stringMap.put("aesKey", aesKey);
            } else {
                stringMap.put("key", "2");
                stringMap.put("en", false);
            }
            // 向前端推送log消息
            if (!this.session.isOpen()) {
                System.out.println("消息发送失败，session 处于关闭状态:" + session.getId());
                return;
            }
            // 发送返回数据 ******* send HERE
            this.session.getBasicRemote().sendText(JSON.toJSONString(stringMap));
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("encrypt failed");
        }
    }

}