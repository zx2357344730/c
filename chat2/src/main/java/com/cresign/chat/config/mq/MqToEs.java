//package com.cresign.chat.config.mq;
//
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.tools.dbTools.Qt;
//import org.apache.rocketmq.spring.annotation.MessageModel;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * @ClassName MqToEs
// * @Description 作者很懒什么也没写
// * @authortang
// * @Date 2022/6/30
// * @ver 1.0.0
// */
//@Component
//@RocketMQMessageListener(
//        topic = "chatTopicEs",
//        selectorExpression = "chatTapEs",
//        messageModel = MessageModel.CLUSTERING,
//        consumerGroup = "topicF-chat-es"
//)
//public class MqToEs implements RocketMQListener<String> {
//
//
////    @Autowired
////    private RestHighLevelClient client;
////
//    @Autowired
//    private Qt qt;
//
//    @Override
//    public void onMessage(String s) {
//        // 转换为json信息
//            JSONObject json = JSONObject.parseObject(s);
//        System.out.println("esHere"+ json);
//
////        LogFlow logData = JSONObject.parseObject(json.getString("logF"), LogFlow.class);
////        LogFlow logData = qt.jsonTo(JSONObject.parseObject(s), LogFlow.class);
//        qt.addES(json.getString("logType"), json);
////        sendLogByES(logData.getLogType(),logData);
//        System.out.println("es:写入完成");
//    }
//
////    public void sendLogByES(String logType, LogFlow logFlow){
////
////        GetIndexRequest request = new GetIndexRequest(logType);
////        try {
////            // 获取结果
////            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
////            // 判断结果
////            if (!exists) {
////                // 1、创建索引请求
////                CreateIndexRequest requestC = new CreateIndexRequest(logType);
////                // 新增索引
////                client.indices().create(requestC, RequestOptions.DEFAULT);
////            }
////
////            // 创建插入数据请求
////            IndexRequest requestI = new IndexRequest(logType);
////
////            // 将我们的数据放入请求 json
////            requestI.source(JSON.toJSONString(logFlow), XContentType.JSON);
////            // 写入完成立即刷新
////            requestI.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
////            // 写入数据
////            client.index(requestI, RequestOptions.DEFAULT);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
//}
