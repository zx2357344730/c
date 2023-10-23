package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.request.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName QtAs
 * @Date 2023/9/23
 * @ver 1.0.0
 */
@Component
public class QtThread {
    @Autowired
    private Qt qt;

    @Value("${thisConfig.url}")
    private String url;

    /**
     * 获取当前线程id
     * @return  线程id
     */
    public long getThreadId(){
        Thread curr = Thread.currentThread();
        return curr.getId();
    }

    /**
     * 开启多线程查询方法
     * @param queryIds  查询id集合
     * @param list  查询结果存储集合
     * @param strList   查询需要的字段
     * @param classType 查询的类型
     * @return  线程处理结果
     * @param <T>   查询类型
     */
    @Async
    public <T> Future<String> threadMD(Collection<?> queryIds, List<T> list, List<String> strList
            , Class<T> classType){
        System.out.println("开始:"+getThreadId());
        Future<String> future;
        try {
            // 调用查询方法
//            mdManyUtilQuery(queryIds, list,strList,classType);
            List<T> thisList =  qt.getMDContentMany(queryIds , strList, classType);
            // 添加查询结果到list
            list.addAll(thisList);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- "+getThreadId()+" ---");
        return future;
    }

    @Async
    public void push2(String title, String content, JSONArray pushAppIds){
        System.out.println("开始:"+getThreadId());

        JSONObject map = new JSONObject();
        map.put("cids",pushAppIds);
        map.put("title",title);
        map.put("content",content);
        JSONObject options = new JSONObject();
        JSONObject HW = new JSONObject();
        HW.put("/message/android/category","WORK");
        options.put("HW",HW);
        map.put("options",options);
        JSONObject date = new JSONObject();
        date.put("toPage","/user/info.js");
        date.put("name","张三");
        date.put("desc","这是一个推送data");
        map.put("date",date);
        map.put("request_id", UUID.randomUUID().toString().replace("-",""));
        String s = HttpClientUtil.sendPost(map,url);
    }
}
