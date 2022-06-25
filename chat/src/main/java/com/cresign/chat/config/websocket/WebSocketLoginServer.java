package com.cresign.chat.config.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.common.ChatConstants;
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
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/23
 * ##version: 1.0.0
 * ##description: 微信登录websocket
 */
@ServerEndpoint("/login/{id}")
@Component
public class WebSocketLoginServer {

    @Override
    public boolean equals(Object obj){
        return super.equals(obj);
    }

    @Override
    public native int hashCode();

    /**
     * 用来存储每个产品的WebSocket单独连接
     */
    private static CopyOnWriteArraySet<WebSocketLoginServer> webSocketSet;

    /**
     * 用来存储所有产品的连接
     */
    private static final Map<String,CopyOnWriteArraySet<WebSocketLoginServer>> map = new HashMap<>(ChatConstants.HASH_MAP_DEFAULT_LENGTH);

    /**
     * 用来存储所有产品连接的在线人数
     */
    private static final Map<String,Integer> onlineCount = new HashMap<>(ChatConstants.HASH_MAP_DEFAULT_LENGTH);

//    /**
//     * 用来存储支付状态
//     */
//    private static final Map<String,Integer> mapStatus = new HashMap<>(ChatConstants.HASH_MAP_DEFAULT_LENGTH);

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 连接建立成功调用的方法
     * ##Params: session	连接用户的session
     * ##Params: orderId	当前连接的订单id
     * ##Params: wxOrderId	微信订单id
     * ##Params: id_C  连接公司id
     * ##Params: id_U	连接用户id
     * ##return: void    返回结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/5 10:49
     */
    @OnOpen
    public void onOpen(Session session
            ,@PathParam("id") String id) {

        // 根据prodID获取产品连接
        CopyOnWriteArraySet<WebSocketLoginServer> writeArraySet = map.get(id);
        // 获取当前用户session
        this.session = session;

        // 判断是否有prodID的连接
        if (writeArraySet==null){
            // 没有，则新建一个prodID的连接
            writeArraySet = new CopyOnWriteArraySet<>();
        }
        // 加入set中
        writeArraySet.add(this);
        // 在线数加1
        addOnlineCount(id);

        // 添加一个prodID连接
        map.put(id,writeArraySet);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // 加密logContent数据
        JSONObject stringMap = new JSONObject();
        stringMap.put("key",id);
        stringMap.put("en",false);
        this.sendMessage(stringMap);
    }

    /**
     * 连接关闭调用的方法
     * ##Params: orderId	订单编号
     * ##Params: wxOrderId	微信订单编号
     * ##Params: id_C	公司编号
     * ##Params: id_U	用户编号
     * ##return: void    返回结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/5 10:50
     */
    @OnClose
    public void onClose(@PathParam("id") String id) {

        // 获取当前产品连接
        webSocketSet = map.get(id);

        // 删除用户自己的连接
        webSocketSet.remove(this);

        // 在线数减1
        subOnlineCount(id);

        // 获取当前在线人数
        int ren = getOnlineCount(id);

        // 如果在线人数为0，则删除产品连接
        if (ren==0){

            // 删除连接
            map.remove(id);
        }
    }

    /**
     * 连接异常回调方法
     * ##Params: error	异常信息
     * ##return: void    返回结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/5 10:51
     */
    @OnError
    public void onError(Throwable error) {

        // 输出错误信息
        System.out.println("websocket出现错误:"+error.getMessage());
    }

    /**
     * 实现服务器主动推送
     * ##Params: stats	推送状态
     * ##return: void    返回结果
     * ##Author: tang
     * ##version: 1.0.0
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
     * 群发自定义消息
     * ##Params: orderId	订单编号
     * ##Params: stats	订单状态
     * ##return: void    返回结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/5 10:52
     */
    public static void sendInfo(String id,JSONObject infoData) {
        // 根据角色获取要发送的连接
        webSocketSet = map.get(id);
        // 判断连接不为空
        if (webSocketSet != null) {
            // 循环遍历产品连接，并向所有连接人发送信息
            webSocketSet.forEach(item -> item.sendMessage(infoData));
        }
    }

//    /**
//     * 获取id是否连接成功
//     * ##param id    连接编号
//     * @return boolean  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2021/7/27 18:27
//     */
//    public static boolean isConnect(String id){
//        System.out.println("map.get(id)");
//        return false;
//    }

    /**
     * 根据prodID获取该连接的总在线人数
     * ##Params: key	键
     * ##return: int    返回结果总在线人数
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/5 10:53
     */
    private static synchronized int getOnlineCount(String key) {
        return onlineCount.get(key);
    }

    /**
     * 根据prodID，把当前人数加一
     * ##Params: key	键
     * ##return: void  返回结果:总在线人数
     * ##Author: tang
     * ##version: 1.0.0
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
     * ##Params: key	键
     * ##return: void  返回结果:
     * ##Author: tang
     * ##version: 1.0.0
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
}