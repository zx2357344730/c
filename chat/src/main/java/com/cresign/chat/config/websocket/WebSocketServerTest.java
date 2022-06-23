package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName WebSocketServer
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/3/1 11:28
 * @Version 1.0.0
 */
@ServerEndpoint("/wsU/{name}")
@Component
@RocketMQMessageListener(
        topic = "TopicTest_Test",
        selectorExpression = "TapA_Test",
        messageModel = MessageModel.BROADCASTING,
        consumerGroup = "topicF-group-test"
)
public class WebSocketServerTest implements RocketMQListener<String> {
    public static String wsName = "ws:";

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 用来存储每个产品的WebSocket单独连接
     */
    private static final Map<String, WebSocketServerTest> webSocketSetMap = new HashMap<>();
    /**
     * 用来存储所有产品连接的在线人数
     */
    private static Integer map2 = 0;

    // 注入RocketMQ模板
    private static RocketMQTemplate rocketMQTemplate;

    // 注入的时候，给类的 service 注入
    @Autowired
    public void setWebSocketServerTest(RocketMQTemplate rocketMQTemplate){
        WebSocketServerTest.rocketMQTemplate = rocketMQTemplate;
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
    public void onOpen(Session session, @PathParam("name") String name){
        System.out.println("进入ws打开:"+wsName);
        // 获取当前用户session
        this.session = session;
        webSocketSetMap.put(name,this);
        // 在线数加1
        addOnlineCount();
        System.out.println("打卡-java-WebSocket!,连接用户:"+name);
        System.out.println("在线人数:"+getOnlineCount());
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
        System.out.println("关闭-java-WebSocket:"+wsName);
        // 在线数减1
        subOnlineCount();
        System.out.println("在线人数:"+getOnlineCount());
        webSocketSetMap.remove(name);
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
        System.out.println("输出异常信息:"+wsName);
        System.out.println(error.getMessage());
        map2 = 0;
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
        System.out.println("收到消息-"+wsName+":"+message);
        JSONObject jsonObject = JSON.parseObject(message);
        sendInfo(jsonObject);
    }

    /**
     * 群发自定义消息
     * ##Params: log1   发送消息
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    public static void sendInfo(JSONObject re) {
        WebSocketServerTest name = webSocketSetMap.get(re.getString("name"));
        name.sendMessage(re);
        // 判断连接不为空
        if (webSocketSetMap.get(re.getString("nameSend")) != null) {
            System.out.println("在本服务:"+ wsName);
            // 循环遍历产品连接，并向所有连接人发送信息
            webSocketSetMap.get(re.getString("nameSend")).sendMessage(re);
        } else {
            System.out.println("不在本服务:"+ wsName);
            rocketMQTemplate.convertAndSend("TopicTest_Test:TapA_Test", re);
        }
    }

    /**
     * 实现服务器主动推送
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    private void sendMessage(JSONObject stringMap) {
        // 向前端推送log消息
        try {
            // 发送返回数据
            this.session.getBasicRemote().sendText(JSON.toJSONString(stringMap));
        } catch (IOException e) {
            System.out.println("出现错误");
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
    private static synchronized int getOnlineCount() {
        return map2;
    }

    /**
     * 根据prodID，把当前人数加一
     * ##Params: key    公司id拼接角色
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    private static synchronized void addOnlineCount() {
        // 在线人数加一
        map2++;
    }

    /**
     * 根据prodID，把当前人数减一
     * ##Params: key    产品id
     * ##author: tangzejin
     * ##version: 1.0.0
     * ##updated: 2020/8/5 9:14:20
     */
    private static synchronized void subOnlineCount() {
        // 在线人数减一
        map2--;
    }

    @Override
    public void onMessage(String msg) {
        JSONObject json = JSONObject.parseObject(msg);
        System.out.println("进入MQ-"+wsName+"-:");
        // 判断连接不为空
        if (webSocketSetMap.get(json.getString("nameSend")) != null) {
            System.out.println(wsName+" - 存在");
            // 循环遍历产品连接，并向所有连接人发送信息
            webSocketSetMap.get(json.getString("nameSend")).sendMessage(json);
        } else {
            System.out.println(wsName+" - 不存在");
        }
    }

}