//package com.cresign.login.controller;
//
//import com.cresign.tools.pojo.po.Result;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 服务器端实时推送技术之 SseEmitter 的用法测试
// * <p>
// * 测试步骤:
// * 1.请求http://localhost:8888/sse/start?clientId=111接口,浏览器会阻塞,等待服务器返回结果;
// * 2.请求http://localhost:8888/sse/send?clientId=111接口,可以请求多次,并观察第1步的浏览器返回结果;
// * 3.请求http://localhost:8888/sse/end?clientId=111接口结束某个请求,第1步的浏览器将结束阻塞;
// * 其中clientId代表请求的唯一标志;
// *
// */
//@RestController
//@RequestMapping("/sse")
//public class SseEmitterController {
//    private static final Logger logger = LoggerFactory.getLogger(SseEmitterController.class);
//
//    // 用于保存每个请求对应的 SseEmitter
//    private Map<String, Result> sseEmitterMap = new ConcurrentHashMap<>();
//
//
//
//
//    /**
//     * 返回SseEmitter对象
//     *
//     * @param clientId
//     * @return
//     */
//    @RequestMapping("/start")
//    public SseEmitter testSseEmitter(String clientId) throws IOException {
//
//        // 默认30秒超时,设置为0L则永不超时
//        SseEmitter sseEmitter = new SseEmitter(0L);
//        sseEmitterMap.put(clientId, new Result(clientId, System.currentTimeMillis(), sseEmitter));
//
//        return sseEmitter;
//    }
//
//    /**
//     * 向SseEmitter对象发送数据
//     *
//     * @param clientId
//     * @return
//     */
//    @RequestMapping("/send")
//    public String setSseEmitter(String clientId) {
//        try {
//            Result result = sseEmitterMap.get(clientId);
//            if (result != null && result.sseEmitter != null) {
//                long timestamp = System.currentTimeMillis();
//
//                String json = "{\"name\":\"jackson\",\n" +
//                        "\"age\":23}";
//
//                result.sseEmitter.send(json);
//            }
//        } catch (IOException e) {
//            logger.error("IOException!", e);
//            return "error";
//        }
//
//        return "Succeed!";
//    }
//
//    /**
//     * 将SseEmitter对象设置成完成
//     *
//     * @param clientId
//     * @return
//     */
//    @RequestMapping("/end")
//    public String completeSseEmitter(String clientId) {
//        Result result = sseEmitterMap.get(clientId);
//        if (result != null) {
//            sseEmitterMap.remove(clientId);
//            result.sseEmitter.complete();
//        }
//        return "Succeed!";
//    }
//}