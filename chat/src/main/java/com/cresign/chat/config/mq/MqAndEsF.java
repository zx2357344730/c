package com.cresign.chat.config.mq;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.logger.LogUtil;
import com.cresign.tools.pojo.po.LogFlow;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName MqAndEs
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/6/30
 * @Version 1.0.0
 */
@Component
@RocketMQMessageListener(
        topic = "chatTopicEsF",
        selectorExpression = "chatTapEsF",
        messageModel = MessageModel.CLUSTERING,
        consumerGroup = "topicF-chat-esF"
)
@Slf4j
public class MqAndEsF implements RocketMQListener<String> {

    @Resource
    private LogUtil logUtil;

    @Override
    public void onMessage(String s) {
        // 转换为json信息
        JSONObject json = JSONObject.parseObject(s);
        LogFlow logData = JSONObject.parseObject(json.getString("logF"), LogFlow.class);
        logUtil.sendLog(logData.getLogType(),logData);
        System.out.println("esF:写入完成");
    }
}
