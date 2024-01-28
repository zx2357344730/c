package com.cresign.action.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.chkin.Task;

import java.util.List;
import java.util.Map;

/**
 * @ClassName TimeZjServiceTimeConflict
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/10/11
 * @Version 1.0.0
 */
public interface TimeZjServiceTimeConflict {

    /**
     * 处理时间冲突方法
     * @param task	当前处理任务信息
     * @param contrastTaskOne	对比任务1
     * @param contrastTaskTwo	对比任务2
     * @param zon	任务余剩时间
     * @param tasks	任务集合
     * @param i	任务下标
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param teDate	当前处理的任务的所在日期对象
     * @param timeConflictCopy	当前任务所在日期对象
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param getCurrentTimeStampPattern	ts = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param recordId_OIndexState	统一id_O和index存储记录状态信息
     * @param storageTaskWhereTime	存储任务所在日期
     * @param allImageTotalTime	全局镜像任务余剩总时间信息
     * @param allImageTasks	全局镜像任务列表信息
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyRefState	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param recordNoOperation	定义存储进入未操作到的地方记录
     * @param clearStatus 清理状态信息
     * @param thisInfo 当前处理通用信息存储
     * @param allImageTeDate 镜像任务所在日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    JSONObject handleTimeConflict(Task task,Task contrastTaskOne,Task contrastTaskTwo,Long zon,List<Task> tasks
            ,int i,String random,String grpB,String dep,JSONObject teDate,JSONObject timeConflictCopy
            ,Integer isGetTaskPattern,Integer getCurrentTimeStampPattern,JSONObject sho,int csSta
            ,String randomAll,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll
            ,JSONObject recordId_OIndexState,JSONObject storageTaskWhereTime
            ,JSONObject allImageTotalTime,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks
            ,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp,JSONObject onlyRefState
            ,JSONObject recordNoOperation,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate
            ,boolean isComprehensiveHandle,JSONObject depAllTime);
}
