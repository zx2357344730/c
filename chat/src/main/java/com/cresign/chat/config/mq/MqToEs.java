package com.cresign.chat.config.mq;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.Qt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName MqToEs
 * @Description 作者很懒什么也没写
 * @authortang
 * @Date 2022/6/30
 * @ver 1.0.0
 */
@Component
@RocketMQMessageListener(
        topic = "chatTopicEs",
        selectorExpression = "chatTapEs",
        messageModel = MessageModel.CLUSTERING,
        consumerGroup = "topicF-chat-es"
)
public class MqToEs implements RocketMQListener<String> {

    @Autowired
    private Qt qt;

    @Override
    public void onMessage(String s) {
        // 转换为json信息
            JSONObject json = JSONObject.parseObject(s);
        qt.addES(json.getString("logType"), json);
//        sendLogByES(logData.getLogType(),logData);
        System.out.println("es:从mq 拿数据写入完成");
    }

}
