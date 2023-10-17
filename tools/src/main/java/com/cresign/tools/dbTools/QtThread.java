package com.cresign.tools.dbTools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
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
}
