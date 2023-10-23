//package com.cresign.action.utils;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.action.service.impl.FlowNewServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.AsyncResult;
//import org.springframework.stereotype.Component;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.concurrent.Future;
//
///**
// * @author tang
// * @Description 作者很懒什么也没写
// * @ClassName FlowAsyncUtil
// * @Date 2023/9/23
// * @ver 1.0.0
// */
//@Component
//public class FlowAsyncUtil {
//    @Autowired
//    private FlowNewServiceImpl flowNewService;
//
//    /**
//     * 开启多线程递归产品方法（JSONArray版本）
//     * @param id_Ps 递归所有id存储
//     * @param item  当前产品列表
//     * @param myCompId  公司编号
//     * @return  线程处理结果
//     */
//    @Async
//    public Future<String> testResult(HashSet<String> id_Ps, JSONArray item, String myCompId) {
//        Future<String> future;
//        try {
//            // 调用递归验证方法
//            flowNewService.checkUtil(id_Ps,item,myCompId);
//            future = new AsyncResult<>("success:");
//        } catch(IllegalArgumentException e){
//            future = new AsyncResult<>("error-IllegalArgumentException");
//        }
//        System.out.println("--- "+getThreadId()+" ---");
//        return future;
//    }
//    /**
//     * 开启多线程递归产品方法（List<JSONObject>版本）
//     * @param id_Ps 递归所有id存储
//     * @param item  当前产品列表
//     * @param myCompId  公司编号
//     * @return  线程处理结果
//     */
//    @Async
//    public Future<String> testResult(HashSet<String> id_Ps, List<JSONObject> item, String myCompId) {
//        Future<String> future;
//        System.out.println("--- "+getThreadId()+" ---");
//        try {
//            // 调用递归验证方法
//            flowNewService.checkUtil(id_Ps,item,myCompId);
//            future = new AsyncResult<>("success:");
//        } catch(IllegalArgumentException e){
//            future = new AsyncResult<>("error-IllegalArgumentException");
//        }
//        System.out.println("+++ "+getThreadId()+" +++");
//        return future;
//    }
//
//    /**
//     * 获取当前线程id
//     * @return  线程id
//     */
//    public long getThreadId(){
//        Thread curr = Thread.currentThread();
//        return curr.getId();
//    }
//}
