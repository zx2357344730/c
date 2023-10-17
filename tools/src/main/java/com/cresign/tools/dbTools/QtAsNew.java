package com.cresign.tools.dbTools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Future;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName QtAs
 * @Date 2023/9/23
 * @ver 1.0.0
 */
@Component
public class QtAsNew {
    @Autowired
    private Qt qt;

    /**
     * 根据《queryIds》Id列表获取《strList》指定的字段的数据库信息，并且添加到《list》
     * @param queryIds  查询的id列表
     * @param list  所有查询结果存储集合
     * @param strList   需要的字段
     * @param classType 需要转换的类型
     * @param <T> 需要的类型
     */
    public <T> void mdManyUtilQuery(Collection<?> queryIds,List<T> list,List<String> strList, Class<T> classType){
        List<T> mdContentMany = qt.getMDContentMany2(queryIds , strList, classType);
        // 添加查询结果到list
        list.addAll(mdContentMany);
    }

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
    public <T> Future<String> testMdMany(Collection<?> queryIds,List<T> list,List<String> strList
            , Class<T> classType){
        System.out.println("开始:"+getThreadId());
        Future<String> future;
        try {
            // 调用查询方法
            mdManyUtilQuery(queryIds, list,strList,classType);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- "+getThreadId()+" ---");
        return future;
    }
}
