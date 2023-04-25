package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.config.mq.MqToEs;
import com.cresign.chat.service.LogService;
import com.cresign.chat.utils.AesUtil;
import com.cresign.chat.utils.RsaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.User;
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
    /**
     * 注入redis数据库下标1模板
     */
    private static StringRedisTemplate redisTemplate0;
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
     * @param qt	DB工具类
     * @param logService	日志接口
     * @param redisTemplate0	redis下标为1的数据库模板
     * @param rocketMQTemplate	rocketMQ模板
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/22
     */
    @Autowired
    public void setWebSocketUserServer(Qt qt, LogService logService
            , StringRedisTemplate redisTemplate0, RocketMQTemplate rocketMQTemplate, MqToEs mqToEs) {
        WebSocketUserServer.logService = logService;
        WebSocketUserServer.qt = qt;
        WebSocketUserServer.redisTemplate0 = redisTemplate0;
        WebSocketUserServer.rocketMQTemplate = rocketMQTemplate;

        WebSocketUserServer.mqToEs = mqToEs;

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
        System.out.println("开启-在线人数:"+getOnlineCount());
        log.info("在线人数:"+getOnlineCount());
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
        System.out.println(WebSocketUserServer.bz+"-关闭ws:"+uId);
        log.info(WebSocketUserServer.bz+"-关闭ws:"+uId);
        closeWS(uId);
        System.out.println("在线人数:"+getOnlineCount());
        log.info("在线人数:"+getOnlineCount());
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
//        error.printStackTrace();
        System.out.println(error.getMessage());
        log.info(error.getMessage());
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

//        //每次响应之前随机获取AES的key，加密data数据
//        String key = AesUtil.getKey();
//
//        // 加密logContent数据
//        JSONObject stringMap = aes(logContent,key);
//        stringMap.put("en",true);

        if ("cusmsg".equals(logContent.getLogType())) {
            JSONObject data = logContent.getData();
            sendMsgOne(data.getString("id_UPointTo"),logContent);
//            String subType = logContent.getSubType();
//            JSONObject data = logContent.getData();
//            boolean isHost = data.getBoolean("isHost");
//            String id_uCus = data.getString("id_UCus");
//            String id_CCus = data.getString("id_CCus");
//            if (isHost) {
//                if ("score".equals(subType)) {
//                    String id_uPointTo = data.getString("id_UPointTo");
//                    String type = data.getString("type");
//                    Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
//                    JSONObject dataNew = new JSONObject();
//                    dataNew.put("id_UPointTo",logContent.getId_U());
//                    String resultDesc;
//                    if (null != asset) {
//                        JSONObject flowControl = asset.getFlowControl();
//                        if (null != flowControl) {
//                            JSONObject cusAsset = flowControl.getJSONObject("cus");
//                            if (null != cusAsset) {
//                                JSONArray cusArr = cusAsset.getJSONArray(type);
//                                if (null != cusArr && cusArr.size() > 0) {
//                                    for (int i = 0; i < cusArr.size(); i++) {
//                                        JSONObject cusArrI = cusArr.getJSONObject(i);
//                                        if (cusArrI.getString("id_U").equals(logContent.getId_U())) {
//                                            JSONObject userAll = cusArrI.getJSONObject("userAll");
//                                            JSONObject id_uPo = userAll.getJSONObject(id_uPointTo);
//                                            id_uPo.put("type",2);
//                                            userAll.put(id_uPointTo,id_uPo);
//                                            cusArrI.put("userAll",userAll);
//                                            cusArrI.put("isBusy",0);
//                                            qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+type+"."+i,cusArrI), Asset.class);
//                                            JSONObject foUp = new JSONObject();
//                                            foUp.put("img","111.jpg");
//                                            foUp.put("type",1);
//                                            qt.setMDContent(asset.getId(),qt.setJson("flowControl.cusFoUp."+id_uPointTo,foUp), Asset.class);
//                                            break;
//                                        }
//                                    }
//                                    data.put("result",true);
//                                    logContent.setData(data);
//                                    sendMsgOne(id_uPointTo,logContent);
//                                    sendMsgOne(logContent.getId_U(),logContent);
//                                    return;
//                                } else {
//                                    resultDesc = "资产客服列表信息为空";
//                                }
//                            } else {
//                                resultDesc = "资产客服信息为空";
//                            }
//                        } else {
//                            resultDesc = "资产消息信息为空";
//                        }
//                    } else {
//                        resultDesc = "资产信息为空";
//                    }
//                    sendMsgNotice(logContent.getId_U(),id_CCus,resultDesc, null,dataNew);
//                    return;
//                } else if ("foUp".equals(subType)) {
//                    String id_uPointTo = data.getString("id_UPointTo");
//                    User user = qt.getMDContent(id_uPointTo, "rolex", User.class);
//                    JSONObject dataNew = new JSONObject();
//                    dataNew.put("id_UPointTo",logContent.getId_U());
//                    String resultDesc;
//                    if (null != user) {
//                        JSONObject rolex = user.getRolex();
//                        if (null != rolex) {
//                            JSONObject cus = rolex.getJSONObject("cus");
//                            if (null != cus) {
//                                JSONObject cusUser = cus.getJSONObject(id_CCus);
//                                if (null == cusUser) {
////                                    sendMsgNotice(logContent.getId_U(),id_CCus,"无法回访该用户!", logContent.getId_U(),dataNew);
//                                    resultDesc = "无法回访该用户!";
//                                } else {
//                                    int cusFoUp = cusUser.getInteger("cusFoUp");
//                                    if (cusFoUp > 0) {
//                                        sendMsgOne(logContent.getId_U(),logContent);
//                                        sendMsgOne(id_uPointTo,logContent);
//                                        cusFoUp--;
//                                        cusUser.put("cusFoUp",cusFoUp);
//                                        qt.setMDContent(logContent.getId_U(),qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
//                                        return;
//                                    } else {
////                                        sendMsgNotice(logContent.getId_U(),id_CCus,"回访次数已达上限!", logContent.getId_U(),dataNew);
//                                        resultDesc = "回访次数已达上限!";
//                                    }
//                                }
//                            } else {
//                                resultDesc = "用户客服信息为空";
//                            }
//                        } else {
//                            resultDesc = "用户权限信息为空";
//                        }
//                    } else {
//                        resultDesc = "用户信息为空";
//                    }
//                    sendMsgNotice(logContent.getId_U(),id_CCus,resultDesc, null,dataNew);
//                    return;
//                }
//            } else {
//                if ("rejection".equals(subType)) {
//                    JSONObject cusUser = new JSONObject();
//                    cusUser.put("state",0);
//                    cusUser.put("id_UCus",null);
//                    cusUser.put("type",null);
//                    cusUser.put("cusFoUp",0);
//                    qt.setMDContent(logContent.getId_U(),qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
//                    data.put("result",true);
//                    logContent.setData(data);
//                    sendMsgOne(logContent.getId_U(),logContent);
//                    return;
//                } else if ("del".equals(subType)) {
//                    User user = qt.getMDContent(logContent.getId_U(), "rolex", User.class);
//                    JSONObject dataNew = new JSONObject();
//                    dataNew.put("id_UPointTo",logContent.getId_U());
//                    String resultDesc;
//                    if (null != user) {
//                        JSONObject rolex = user.getRolex();
//                        if (null != rolex) {
//                            JSONObject cus = rolex.getJSONObject("cus");
//                            if (null != cus) {
//                                cus.remove(id_CCus);
//                                qt.setMDContent(logContent.getId_U(),qt.setJson("rolex.cus",cus), User.class);
//                                data.put("result",true);
//                                logContent.setData(data);
//                                sendMsgOne(logContent.getId_U(),logContent);
//                                return;
//                            } else {
//                                resultDesc = "用户客服信息为空";
//                            }
//                        } else {
//                            resultDesc = "用户权限信息为空";
//                        }
//                    } else {
//                        resultDesc = "用户信息为空";
//                    }
//                    sendMsgNotice(logContent.getId_U(),id_CCus,resultDesc, null,dataNew);
//                    return;
//                } else if ("score".equals(subType)) {
//                    User user = qt.getMDContent(logContent.getId_U(), "rolex", User.class);
//                    JSONObject dataNew = new JSONObject();
//                    dataNew.put("id_UPointTo",logContent.getId_U());
//                    String resultDesc;
//                    String id_uCusNew;
//                    String type;
//                    String score;
//                    JSONObject cusUser;
//                    if (null != user) {
//                        JSONObject rolex = user.getRolex();
//                        if (null != rolex) {
//                            JSONObject cus = rolex.getJSONObject("cus");
//                            if (null != cus) {
//                                cusUser = cus.getJSONObject(id_CCus);
//                                id_uCusNew = cusUser.getString("id_UCus");
//                                type = cusUser.getString("type");
//                                score = data.getString("score");
//                            } else {
//                                sendMsgNotice(logContent.getId_U(),id_CCus,"用户客服信息为空", null,dataNew);
//                                return;
//                            }
//                        } else {
//                            sendMsgNotice(logContent.getId_U(),id_CCus,"用户权限信息为空", null,dataNew);
//                            return;
//                        }
//                    } else {
//                        sendMsgNotice(logContent.getId_U(),id_CCus,"用户信息为空", null,dataNew);
//                        return;
//                    }
//
//                    Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
//                    if (null != asset) {
//                        JSONObject flowControl = asset.getFlowControl();
//                        if (null != flowControl) {
//                            JSONObject cusAsset = flowControl.getJSONObject("cus");
//                            JSONArray cusArr = cusAsset.getJSONArray(type);
//                            for (int i = 0; i < cusArr.size(); i++) {
//                                JSONObject cusArrI = cusArr.getJSONObject(i);
//                                if (cusArrI.getString("id_U").equals(id_uCusNew)) {
//                                    int scoreCus = cusArrI.getInteger(score);
//                                    scoreCus++;
//                                    qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+type+"."+i+".score."+score,scoreCus), Asset.class);
//                                    break;
//                                }
//                            }
//                            cusUser.put("id_UCus","");
//                            cusUser.put("type","");
//                            cusUser.put("cusFoUp",cusUser.getInteger("cusFoUp"));
//                            cusUser.put("state",cusUser.getInteger("state"));
//                            qt.setMDContent(logContent.getId_U(),qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
//                            data.put("result",true);
//                            logContent.setData(data);
//                            sendMsgOne(logContent.getId_U(),logContent);
//                            sendMsgOne(id_uCusNew,logContent);
//                            return;
//                        } else {
//                            resultDesc = "资产消息信息为空";
//                        }
//                    } else {
//                        resultDesc = "资产信息为空";
//                    }
//                    sendMsgNotice(logContent.getId_U(),id_CCus,resultDesc, null,dataNew);
//                    return;
//                }
//            }
//            // normal
//            String resultDesc;
//            if (null != id_uCus && !"".equals(id_uCus)) {
//                String id_uPointTo = data.getString("id_UPointTo");
//                if (isHost) {
//                    User user = qt.getMDContent(id_uPointTo, "rolex", User.class);
//                    if (null != user && null != user.getRolex()) {
//                        JSONObject rolex = user.getRolex();
//                        JSONObject cus = rolex.getJSONObject("cus");
//                        if (null != cus) {
//                            JSONObject cusUser = cus.getJSONObject(id_CCus);
//                            if (null != cusUser) {
//                                int state = cusUser.getInteger("state");
//                                if (state == 0) {
//                                    resultDesc = "用户已拒收!";
//                                } else {
//                                    sendMsgOne(logContent.getId_U(),logContent);
//                                    sendMsgOne(id_uPointTo,logContent);
//                                    return;
//                                }
//                            } else {
//                                resultDesc = "该用户客服信息无本公司!";
//                            }
//                        } else {
//                            resultDesc = "该用户客服信息异常!";
//                        }
//                    } else {
//                        resultDesc = "该用户信息为空!";
//                    }
//                    JSONObject dataNew = new JSONObject();
//                    dataNew.put("id_UPointTo",logContent.getId_U());
//                    sendMsgNotice(logContent.getId_U(),id_CCus,resultDesc,null,dataNew);
//                } else {
//                    sendMsgOne(logContent.getId_U(),logContent);
//                    sendMsgOne(id_uPointTo,logContent);
//                }
//                return;
//            }
//            Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
//            if (null != asset && null != asset.getFlowControl()) {
//                JSONObject flowControl = asset.getFlowControl();
//                JSONObject cus = flowControl.getJSONObject("cus");
//                if (null != cus) {
//                    String typePointTo = data.getString("typePointTo");
//                    JSONArray cusArr = cus.getJSONArray(typePointTo);
//                    if (null == cusArr || cusArr.size() > 0) {
//                        cusArr = cus.getJSONArray("zhu");
//                        typePointTo = "zhu";
//                    }
//                    if (null != cusArr && cusArr.size() > 0) {
//                        for (int i = 0; i < cusArr.size(); i++) {
//                            JSONObject cusArrObj = cusArr.getJSONObject(i);
//                            int isBusy = cusArrObj.getInteger("isBusy");
//                            if (isBusy == 0) {
//                                cusArrObj.put("isBusy",1);
//                                JSONObject userAll = cusArrObj.getJSONObject("userAll");
//                                JSONObject userInfo = new JSONObject();
//                                userInfo.put("img",logContent.getPic());
//                                userInfo.put("type",1);
//                                userAll.put(logContent.getId_U(),userInfo);
//                                cusArrObj.put("userAll",userAll);
//                                qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+typePointTo+"."+i,cusArrObj), Asset.class);
//
//                                String id_u = cusArrObj.getString("id_U");
//                                JSONObject cusUser = new JSONObject();
//                                cusUser.put("state",1);
//                                cusUser.put("id_UCus",id_u);
//                                cusUser.put("type",typePointTo);
//                                cusUser.put("cusFoUp",3);
//                                qt.setMDContent(logContent.getId_U(),qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
//
//                                JSONObject dataNew = new JSONObject();
//                                dataNew.put("id_UPointTo",logContent.getId_U());
//                                dataNew.put("id_UCus",id_u);
//                                sendMsgNotice(logContent.getId_U(),id_CCus,"客服-"+id_u+"-为你服务!",id_u,dataNew);
//                                dataNew.put("id_UPointTo",id_u);
//                                sendMsgNotice(id_u,id_CCus,"顾客"+logContent.getId_U()+"需要服务!",logContent.getId_U(),dataNew);
//                                return;
//                            }
//                        }
//                        resultDesc = "该公司客服-繁忙中...";
//                    } else {
//                        resultDesc = "该公司客服-为空-!";
//                    }
//                } else {
//                    resultDesc = "该公司没有客服!";
//                }
//            } else {
//                resultDesc = "该公司权限为空!";
//            }
//            JSONObject dataNew = new JSONObject();
//            dataNew.put("id_UPointTo",logContent.getId_U());
//            sendMsgNotice(logContent.getId_U(),id_CCus,resultDesc,null,dataNew);
//            return;
        }
        if ("link".equals(logContent.getSubType())) {
            //每次响应之前随机获取AES的key，加密data数据
            String key = AesUtil.getKey();
            // 加密logContent数据
            JSONObject stringMap = aes(logContent,key);
            stringMap.put("en",true);
//            sendMessage(stringMap,key,true);
            String id_U = logContent.getId_U();
            if (WebSocketUserServer.webSocketSet.containsKey(id_U)) {
                Map<String, String> cliU = WebSocketUserServer.clients.get(id_U);
                Map<String, WebSocketUserServer> sw = WebSocketUserServer.webSocketSet.get(id_U);

                cliU.keySet().forEach(k -> {
//                    String s = cliU.get(k);
//                    if ("web".equals(s)) {
//
//                    }
                    sw.get(k).sendMessage(stringMap,key,true);
                });
//                mqToEs.sendLogByES(logContent.getLogType(), logContent);
            }
//            sendMsgToMQ(null,logContent);
        } else if ("only".equals(logContent.getSubType()))
        {
            JSONObject data = logContent.getData();
            JSONArray sendU = data.getJSONArray("sendU");
            System.out.println("sendU:");
            System.out.println(JSON.toJSONString(sendU));
            String client = data.getString("client");
            for (int i = 0; i < sendU.size(); i++) {
                String id_U = sendU.getString(i);
                if (WebSocketUserServer.webSocketSet.containsKey(id_U)) {
                    Map<String, WebSocketUserServer> sw = WebSocketUserServer.webSocketSet.get(id_U);
                    //每次响应之前随机获取AES的key，加密data数据
                    String key = AesUtil.getKey();
                    // 加密logContent数据
                    JSONObject stringMap = aes(logContent,key);
                    stringMap.put("en",true);
                    if ("all".equals(client)) {
                        sw.values().forEach(w -> w.sendMessage(stringMap,key,true));
                    } else {
                        Map<String, String> cliU = WebSocketUserServer.clients.get(id_U);
                        cliU.keySet().forEach(k -> {
                            String s = cliU.get(k);
                            if (client.equals(s)) {
                                sw.get(k).sendMessage(stringMap,key,true);
                            }
                        });
                    }
                }
            }
        } else {
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
//            // 127.0.0.1 local test switching
            localSending(id_Us, logContent);
            // 调用检测id_U在不在本服务并发送信息方法
            qt.sendMQ("chatTopic:chatTap",JSONObject.parseObject(JSON.toJSONString(logContent)));
//            sendMsgToMQ(id_Us,logContent);
////            //4. regular send to ES 1 time
//              sendMsgToEs(logContent);
////            //5. regular send to PUSH to everybody who registered id_APP - batch push
//            sendMsgToPush(cidArray,logContent);
        }


    }

    public static void sendMsgOne(String id_U,LogFlow logFlow){
//        JSONObject data = logFlow.getData();
//        data.put("id_UPointTo",id_U);
//        logFlow.setData(data);
        if (WebSocketUserServer.webSocketSet.containsKey(id_U)) {
            Map<String, String> cliU = WebSocketUserServer.clients.get(id_U);
            Map<String, WebSocketUserServer> sw = WebSocketUserServer.webSocketSet.get(id_U);
            cliU.keySet().forEach(k->
                    sw.get(k).sendMessage(JSONObject.parseObject(JSON.toJSONString(logFlow))
                            ,AesUtil.getKey(),true)
            );
        }
//        else {
//            qt.sendMQ("chatTopic:chatTap",JSONObject.parseObject(JSON.toJSONString(logFlow)));
//        }
    }
//    public static void sendMsgNotice(String sendUser,String id_CCus,String desc,String logUser,JSONObject dataNew){
//        dataNew.put("id_CCus",id_CCus);
//        LogFlow logFlow = getNullLogFlow("cusmsg","notice"
//                ,desc,id_CCus,logUser,dataNew);
//        sendMsgOne(sendUser,logFlow);
//    }

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
            } else if ("4".equals(id)) {
                System.out.println(JSON.toJSONString(WebSocketUserServer.clients));
            } else {
                // 调用解密并且发送信息方法
                LogFlow logData = RsaUtil.encryptionSend(map, WebSocketUserServer.keyJava.get(this.session.getId())
                        .getString("privateKeyJava"));

                sendLog(logData);

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
        JSONObject json = JSONObject.parseObject(msg);
        String subType = json.getString("subType");
        if ("hd".equals(subType)) {
            System.out.println("接收后端直接发送消息:");
            System.out.println(JSON.toJSONString(json));
            JSONObject data = json.getJSONObject("data");
            JSONArray id_Us = data.getJSONArray("id_Us");
            data.remove("id_Us");
            json.put("data",data);
            for (int i = 0; i < id_Us.size(); i++) {
                if (WebSocketUserServer.webSocketSet.containsKey(id_Us.getString(i))) {
                    WebSocketUserServer.webSocketSet.get(id_Us.getString(i)).values()
                            .forEach(w -> w.sendMessage(json, AesUtil.getKey(),true));
                }
            }
            return;
        } else if ("cusmsg".equals(json.getString("logType"))) {
            System.out.println("收到客服信息:");
            System.out.println(JSON.toJSONString(json));
            JSONObject data = json.getJSONObject("data");
            String id_uPointTo = data.getString("id_UPointTo");
            if (WebSocketUserServer.webSocketSet.containsKey(id_uPointTo)) {
                System.out.println("--在本服务--");
                WebSocketUserServer.webSocketSet.get(id_uPointTo).values()
                        .forEach(w -> w.sendMessage(json, AesUtil.getKey(),true));
            }
        }
        if ("link".equals(subType)) {
            String id_U = json.getString("id_U");
//            JSONArray sessions = json.getJSONArray("sessions");
            if (WebSocketUserServer.webSocketSet.containsKey(id_U)) {
                System.out.println("在本服务:"+" - "+json.getString("bz")+" - "+"进入Chat_MQ:当前ws标志-"+ WebSocketUserServer.bz);
                Map<String, String> sm = WebSocketUserServer.clients.get(id_U);
                Map<String, WebSocketUserServer> sw = WebSocketUserServer.webSocketSet.get(id_U);
                sm.keySet().forEach(k -> {
                    String s = sm.get(k);
                    if ("web".equals(s)) {
                        sw.get(k).sendMessage(json.getJSONObject("stringMap")
                                ,json.getString("key"),true);
                    }
                });
            }
        } else if ("only".equals(subType))
        {
            String sendU = json.getString("sendU");
            if (WebSocketUserServer.webSocketSet.containsKey(sendU)) {
                String client = json.getString("client");
                Map<String, WebSocketUserServer> sw = WebSocketUserServer.webSocketSet.get(sendU);
                if ("all".equals(client)) {
                    sw.values().forEach(w -> w.sendMessage(json.getJSONObject("stringMap")
                            ,json.getString("key"),true));
                } else {
                    Map<String, String> cliU = WebSocketUserServer.clients.get(sendU);
                    cliU.keySet().forEach(k -> {
                        String s = cliU.get(k);
                        if (client.equals(s)) {
                            sw.get(k).sendMessage(json.getJSONObject("stringMap")
                                    ,json.getString("key"),true);
                        }
                    });
                }
            }
        } else {
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
//     * @ver 1.0.0
//     * @date 2022/6/23
//     */
//    public static void setWsU(String id_U,String state){
//        redisTemplate0.opsForValue().set(id_U+"_ws",state);
//    }
//
//    /**
//     * 获取id_U的在线状态
//     * @param id_U	用户编号
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * @date 2022/6/23
//     */
//    public static String getWsU(String id_U){
//        return redisTemplate0.opsForValue().get(id_U+"_ws");
//    }

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
//        setWsU(uId,"0");
        // 判断用户连接不为空
        if (null != WebSocketUserServer.webSocketSet.get(uId)) {
            // 删除用户自己的连接
//            WebSocketUserServerQ.webSocketSet.remove(uId);
            if (null != WebSocketUserServer.webSocketSet.get(uId).get(this.session.getId())) {
                WebSocketUserServer.webSocketSet.get(uId).remove(this.session.getId());
            }

//            if (WebSocketUserServer.webSocketSet.get(uId).isEmpty()) {
//                WebSocketUserServer.webSocketSet.remove(uId);
//            }
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

    //1. check a-auth to get objUser list @ id_C
    //2. if id_C!= id_CS, check a-auth get id_CS objUser if *** id_CS is real
    //3. put an array of id_U into mq

    private static void prepareMqUserInfo(String id_C, LogFlow logContent, JSONArray id_Us, JSONArray cidArray)
    {
//        String assetId = qt.getId_A(id_C, "a-auth");
//        if (null == assetId || "none".equals(assetId)) {
//            return;
//        }

        // 根据asset编号获取asset信息
//        Asset asset = dbUtils.getAssetById(assetId, Collections.singletonList("flowControl"));

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
//        String assetId = qt.getId_A(id_C, "a-auth");
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
//     * @ver 1.0.0
//     * @date 2022/6/23
//     */
//    private static void getPushList(JSONArray cidArray,String title,String body){
//        String token = logService.getToken();
//        logService.sendPushBatch(cidArray, title, body, token);
//    }

    /**
     * 检测id_U在不在本服务并发送信息方法，在：直接发送、不在：放入mq发送
     * @param id_Us	用户编号
     * @param logContent 消息体
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/28
     */
    private static void sendMsgToMQ(JSONArray id_Us,LogFlow logContent) {

        //每次响应之前随机获取AES的key，加密data数据
        String key = AesUtil.getKey();

        // 加密logContent数据
        JSONObject stringMap = aes(logContent,key);
        stringMap.put("en",true);
        JSONObject db = new JSONObject();
        String subType = logContent.getSubType();
        db.put("stringMap", stringMap);
        db.put("key", key);
        db.put("bz", WebSocketUserServer.bz);
        db.put("subType",logContent.getSubType());
        if ("link".equals(subType)) {
            db.put("id_U", logContent.getId_U());
//            db.put("sessions", id_Us);
        } else if ("only".equals(subType)) {
            db.put("sendU",logContent.getData().getString("sendU"));
            db.put("client",logContent.getData().getString("client"));
        } else {
            db.put("id_Us", id_Us);
        }
        // mq消息推送
        System.out.println("发送消息1");

        rocketMQTemplate.convertAndSend("chatTopic:chatTap", db);
        System.out.println("发送消息2");
    }


    private static void sendMsgToPush(JSONArray cidArray, LogFlow logContent)
    { //id_APP
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
     * @author tang
     * @ver 1.0.0
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
        try {
            String key = AesUtil.getKey();

            // 加密logContent数据
            JSONObject stringMap = aes(logContent, key);
            stringMap.put("en", true);
            JSONObject db = new JSONObject();

            db.put("stringMap", stringMap);
//        db.put("key", key);
            db.put("id_Us", id_Us);
            db.put("bz", WebSocketUserServer.bz);

            for (int i = 0; i < id_Us.size(); i++) {
                System.out.println("idU" + id_Us.getString(i));
                if (WebSocketUserServer.webSocketSet.containsKey(id_Us.getString(i))) {
                    System.out.println("在本服务:" + " - " + db.getString("bz") + " - " + "进入Chat_MQ:当前ws标志-" + WebSocketUserServer.bz);

                    WebSocketUserServer.webSocketSet.get(id_Us.getString(i)).values()
                            .forEach(w -> w.sendMessage(db.getJSONObject("stringMap")
                                    , key, true));
                }
            }

            mqToEs.sendLogByES(logContent.getLogType(), logContent);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    private static LogFlow getNullLogFlow(String logType,String subType,String desc,String id_C,String id_U,JSONObject data){
        LogFlow logFlow = LogFlow.getInstance();
        logFlow.setLogType(logType);
        logFlow.setSubType(subType);
        logFlow.setZcndesc(desc);
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        logFlow.setId_C(id_C);
        logFlow.setId_U(id_U);
        logFlow.setData(data);
        return logFlow;
    }
}