package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.Ws;
import io.netty.channel.ChannelHandler.Sharable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName WebSocketTest
 * @Date 2023/4/15
 * @ver 1.0.0
 */
@ServerEndpoint("/wsU/test/{name}")
@Component
@Sharable
//@RocketMQMessageListener(
//        topic = "testTopic",
//        selectorExpression = "testTap",
//        messageModel = MessageModel.BROADCASTING,
//        consumerGroup = "topicF-chat-test"
//)
//@Slf4j
public class WebSocketTest
//        implements RocketMQListener<String>
{

//    /**
//     * 注入redis工具类
//     */
//    private static Qt qt;

    private static Ws ws;
    private String name;
    /**
     * 用来存储所有连接的在线人数
     */
    private static Integer totalOnlineCount = 0;
    /**
     * 用来根据用户编号存储每个用户的WebSocket连接信息
     */
    private static final Map<String, WebSocketTest> webSocketSet = new HashMap<>(16);
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 注入的时候，给类的 service 注入
//     * @param qt	DB工具类
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/22
     */
    @Autowired
    public void setWebSocketUserServer(
//            Qt qt,
                                       Ws ws) {
//        WebSocketTest.qt = qt;
        WebSocketTest.ws = ws;
    }

    /**
     * 连接建立成功调用的方法
     * @param session   连接用户的session
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name) {
        this.name = name;
        // 获取当前用户session
        this.session = session;
//        System.out.println("sessionId:"+this.session.getId());
        if (webSocketSet.containsKey(name)) {
            System.out.println("已存在连接");
        } else {
            webSocketSet.put(name,this);
            addOnlineCount();
        }
        JSONObject sendInfo = new JSONObject();
        sendInfo.put("num",getOnlineCount());
        sendInfo.put("desc","连接成功");
        sendInfo.put("name",name);
        sendMessage(sendInfo);
        System.out.println("ws打开:"+name+",在线人数:"+getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     * @param name    聊天室连接id
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    @OnClose
    public void onClose(@PathParam("name") String name) {
        subOnlineCount();
        webSocketSet.remove(name);
        System.out.println("ws关闭:"+name);
    }

    /**
     * 实现服务器主动推送
     * @author tangzejin
     * @ver 1.0.0
     * @updated 2020/8/5 9:14:20
     */
    private synchronized void sendMessage(JSONObject stringMap) {
        // 向前端推送log消息
        try {
            if (!this.session.isOpen()) {
                System.out.println("消息发送失败，session 处于关闭状态:" + this.name);
                return;
            }
            // 发送返回数据
            this.session.getBasicRemote().sendText(JSON.toJSONString(stringMap));
//            qt.sendMQ("testTopic:testTap",stringMap);
            ws.sendWSOnlyByObj(stringMap);
        } catch (IOException e) {
            System.out.println("sendMessage出现错误");
        }
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
        System.out.println("websocket-收到消息:"+message);
//        System.out.println(message);
    }

//    /**
//     * MQ消息接收方法
//     * @param msg	接收的消息
//     * @author tang
//     * @ver 1.0.0
//     * @date 2022/6/22
//     */
//    @Override
//    public void onMessage(String msg) {
//        System.out.println("mq-收到消息:"+msg);
////        System.out.println(msg);
//    }

    /**
     * 当前总在线人数加1
     * 无参
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/23
     */
    private static synchronized void addOnlineCount(){
        WebSocketTest.totalOnlineCount++;
    }

    /**
     * 当前总在线人数减1
     * 无参
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/23
     */
    private static synchronized void subOnlineCount(){
        WebSocketTest.totalOnlineCount--;
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
        return WebSocketTest.totalOnlineCount;
    }

}
