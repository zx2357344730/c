package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.TimeZjServiceTimeConflict;
import com.cresign.action.utils.TaskObj;
import com.cresign.tools.pojo.po.chkin.Task;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description 时间处理(处理时间冲突方法)类
 * @ClassName TimeZjServiceTimeConflictImpl
 * @Author tang
 * @Date 2022/10/11
 * @Version 1.0.0
 */
@Service
public class TimeZjServiceTimeConflictImpl extends TimeZj implements TimeZjServiceTimeConflict {

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
    @Override
    @SuppressWarnings("unchecked")
    public JSONObject handleTimeConflict(Task task, Task contrastTaskOne, Task contrastTaskTwo, Long zon, List<Task> tasks, int i
            , String random, String grpB, String dep, JSONObject teDate, JSONObject timeConflictCopy
            , Integer isGetTaskPattern, Integer getCurrentTimeStampPattern, JSONObject sho, int csSta, String randomAll, JSONObject xbAndSbAll
            , JSONObject actionIdO, JSONObject objTaskAll, JSONObject recordId_OIndexState, JSONObject storageTaskWhereTime
            , JSONObject allImageTotalTime, Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks
            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState
            , JSONObject recordNoOperation,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,boolean isComprehensiveHandle) {
        // 创建返回结果对象
        JSONObject result = new JSONObject();
//        System.out.println("处理时间冲突方法-q");
        // 创建冲突任务集合
        List<Task> conflict = new ArrayList<>();
        // 调用冲突处理核心方法
        JSONObject handleTimeConflictCoreInfo = handleTimeConflictCore(task, contrastTaskOne, contrastTaskTwo, zon
                , tasks, i, conflict,getTeS(random, grpB, dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                ,random,grpB,dep,teDate,isGetTaskPattern,getCurrentTimeStampPattern,sho,csSta,randomAll,xbAndSbAll
                ,objTaskAll,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                ,newestLastCurrentTimestamp,onlyRefState,allImageTeDate,isComprehensiveHandle);
//        System.out.println("处理时间冲突方法-2-q");
        // 获取任务余剩时间
        zon = handleTimeConflictCoreInfo.getLong("zon");
        long tePFinish = handleTimeConflictCoreInfo.getLong("tePFinish");
        // 获取当天时间
        long endTime = handleTimeConflictCoreInfo.getLong("endTime");
        // 获取存储任务是否被处理完状态参数：storageTaskIsProcessedComplete == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
        int storageTaskIsProcessedComplete = handleTimeConflictCoreInfo.getInteger("storageTaskIsProcessedComplete");
        System.out.println("storageTaskIsProcessedComplete-1:"+storageTaskIsProcessedComplete);
        // 获取当前时间戳
        long teSB = handleTimeConflictCoreInfo.getLong("teSB");
//        System.out.println("storageTaskIsProcessedComplete外:"+storageTaskIsProcessedComplete);
        // 获取存储任务是否被处理完状态参数：storageTaskIsProcessedComplete == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
        if (storageTaskIsProcessedComplete == 0) {
            // 开启循环
            do {
                // isJ强制停止参数累加
                xin.put(randomAll,(xin.getInteger(randomAll)+1));
                // 当前时间戳累加 = 2022-12-07 00:00:00
                teSB += 86400L;
                // 调用获取任务综合信息方法
                Map<String, Object> jumpDay = getJumpDay(random, grpB, dep, 1, teSB,isGetTaskPattern,task.getId_C()
                        ,xbAndSbAll,objTaskAll,allImageTotalTime,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp);
                // 获取任务集合
                List<Task> tasksInside = (List<Task>) jumpDay.get("tasks");
                // 获取任务余剩时间
                long zonInside = (long) jumpDay.get("zon");
                // 控制结束外层循环参数，controlLoopOuterLayerEnd == 0 不结束外层循环、zb == 1 结束外层循环 controlLoopOuterLayerEnd
                int controlLoopOuterLayerEnd = 0;
                // 遍历任务集合
                for (int j = 0; j < tasksInside.size(); j++) {
                    // 获取对比任务1
                    Task contrastTaskOneNew = tasksInside.get(j);
                    // 判断（i1（当前任务下标）+1）小于任务集合总长度
                    if ((j + 1) < tasksInside.size()) {
                        // 获取对比任务2
                        Task contrastTaskTwoNew = tasksInside.get(j + 1);
                        // 调用冲突处理核心方法
                        handleTimeConflictCoreInfo = handleTimeConflictCore(task, contrastTaskOneNew
                                , contrastTaskTwoNew, zonInside, tasksInside, j, conflict, teSB,random,grpB,dep
                                ,teDate,isGetTaskPattern,0,sho,csSta
                                ,randomAll,xbAndSbAll,objTaskAll,storageTaskWhereTime,allImageTotalTime
                                ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,allImageTeDate,isComprehensiveHandle);
                        // 获取任务余剩时间
                        zonInside = handleTimeConflictCoreInfo.getLong("zon");
                        // 获取存储任务是否被处理完状态参数：storageTaskIsProcessedComplete == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
                        storageTaskIsProcessedComplete = handleTimeConflictCoreInfo.getInteger("storageTaskIsProcessedComplete");
                        // 获取当前时间戳
                        teSB = handleTimeConflictCoreInfo.getLong("teSB");
                        tePFinish = handleTimeConflictCoreInfo.getLong("tePFinish");
                        endTime = handleTimeConflictCoreInfo.getLong("endTime");
//                        System.out.println("storageTaskIsProcessedComplete:" + storageTaskIsProcessedComplete);
                        // 获取存储任务是否被处理完状态参数：storageTaskIsProcessedComplete == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
                        if (storageTaskIsProcessedComplete == 1) {
                            controlLoopOuterLayerEnd = 1;
                            // isGetTaskPattern –  = 0 获取数据库任务信息、 = 1 获取镜像任务信息
                            if (isGetTaskPattern == 0) {
                                setTasksAndZon(tasksInside,grpB,dep,teSB,zonInside,objTaskAll);
                            } else {
                                System.out.println("进入-最开始-这里-1-写入镜像:");
                                // 调用写入镜像任务集合方法
                                setImageTasks(tasksInside,grpB,dep,teSB,allImageTasks);
                                // 调用写入镜像任务余剩时间方法
                                setImageZon(zonInside,grpB,dep,teSB,allImageTotalTime);
                            }
                            break;
                        }
                    }
                }
                // 控制结束外层循环参数，zb == 0 不结束外层循环、zb == 1 结束外层循环
                if (controlLoopOuterLayerEnd == 1) {
                    break;
                }
                // 判断isJ强制停止参数等于10
                if (xin.getInteger(randomAll) == 510) {
                    System.out.println("进入isJ强制结束!!!");
                    // 赋值强制停止出现后的记录参数
                    isQzTz.put(randomAll,1);
                    break;
                }
            } while (true);
        }
        System.out.println("处理时间冲突方法-2h");
        JSONObject isSetImage = null;
        if (isGetTaskPattern == 0) {
            long teS = tasks.get(0).getTePStart();
            if (teS != teSB) {
                teSB = teS;
            }
            setTasksAndZon(tasks,grpB,dep,teSB,zon,objTaskAll);
        } else {
            isSetImage = new JSONObject();
            JSONObject isSetImageGrpB = new JSONObject();
//            isSetImageGrpB.put(grpB,teSB);
            isSetImageGrpB.put(grpB,tasks.get(0).getTePStart());
            isSetImage.put(dep,isSetImageGrpB);
//            System.out.println(JSON.toJSONString(isSetImage));
//            setImageTasks(tasks,grpB,dep,teSB,allImageTasks);
//            setImageZon(zon,grpB,dep,teSB,allImageTotalTime);
            setImageTasks(tasks,grpB,dep,tasks.get(0).getTePStart(),allImageTasks);
            setImageZon(zon,grpB,dep,tasks.get(0).getTePStart(),allImageTotalTime);
        }
        // 调用处理冲突核心方法
        JSONObject handleTimeConflictEndInfo = timeZjServiceComprehensive.handleTimeConflictEnd(i
                ,tasks,conflict,zon,random,dep,grpB,timeConflictCopy,isGetTaskPattern
                ,getCurrentTimeStampPattern,sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState
                ,recordNoOperation,tePFinish,clearStatus,thisInfo,allImageTeDate,isSetImage,endTime);
        System.out.println("处理时间冲突方法-2h-H:"+tePFinish);
        result.put("zon",handleTimeConflictEndInfo.getLong("zon"));
        // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
        result.put("isProblemState",handleTimeConflictEndInfo.getInteger("isProblemState"));
        result.put("tePFinish",tePFinish);
        result.put("endTime",endTime);
        result.put("isSetEnd", handleTimeConflictEndInfo.getBoolean("isSetEnd") == null || handleTimeConflictEndInfo.getBoolean("isSetEnd"));
        return result;
    }

    /**
     * 处理时间冲突方法复刻方法（比起原方法，少了很多操作）
     * @param task	当前处理任务信息
     * @param contrastTaskOne	对比任务1
     * @param contrastTaskTwo	对比任务2
     * @param zon	任务余剩时间
     * @param tasks	任务集合
     * @param i	上一个任务下标
     * @param j	当前任务下标
     * @param conflict	被冲突任务集合
     * @param teSB	当前时间戳
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param teDate	当前处理的任务的所在日期对象
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param getCurrentTimeStampPattern	ts = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param objTaskAll	全局任务信息
     * @param storageTaskWhereTime	存储任务所在日期
     * @param allImageTotalTime	全局镜像任务余剩总时间信息
     * @param allImageTasks	全局镜像任务列表信息
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyRefState	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param allImageTeDate 镜像任务所在日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    public JSONObject handleTimeConflictEasy(Task task, Task contrastTaskOne, Task contrastTaskTwo, Long zon, List<Task> tasks
            , int i, int j, List<Task> conflict, Long teSB, String random, String grpB, String dep, JSONObject teDate
            , Integer isGetTaskPattern, Integer getCurrentTimeStampPattern, JSONObject sho
            , int csSta, String randomAll, JSONObject xbAndSbAll, JSONObject objTaskAll
            , JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks, JSONObject onlyFirstTimeStamp
            , JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState,JSONObject allImageTeDate,boolean isComprehensiveHandle) {
        // 调用冲突处理核心方法
        JSONObject handleTimeConflictCoreInfo = handleTimeConflictCore(task, contrastTaskOne, contrastTaskTwo
                , zon, tasks, j, conflict,teSB,random,grpB,dep,teDate,isGetTaskPattern,getCurrentTimeStampPattern
                ,sho,csSta,randomAll,xbAndSbAll,objTaskAll,storageTaskWhereTime
                ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,allImageTeDate,isComprehensiveHandle);
        // 获取任务余剩时间
        zon = handleTimeConflictCoreInfo.getLong("zon");
        // 获取存储任务是否被处理完状态参数：isJ2 == 0 任务没有被处理完、isJ2 == 1 任务已经被处理完了
        int storageTaskIsProcessedComplete = handleTimeConflictCoreInfo.getInteger("storageTaskIsProcessedComplete");
        // 获取当前时间戳
        long teSBNew = handleTimeConflictCoreInfo.getLong("teSB");
        // 获取最后结束时间
        long tePFinish = handleTimeConflictCoreInfo.getLong("tePFinish");
        // 获取当天时间
        long endTime = handleTimeConflictCoreInfo.getLong("endTime");
        // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
        int taskIsProcessedComplete = handleTimeConflictCoreInfo.getInteger("taskIsProcessedComplete");
        // 获取存储冲突处理模式
        int conflictHandlePattern = handleTimeConflictCoreInfo.getInteger("conflictHandlePattern");
        System.out.println("isJ2外:"+taskIsProcessedComplete
                +" - handleTimeConflictCoreInfo外:"+storageTaskIsProcessedComplete);
        // 获取存储任务是否被处理完状态参数：isJ2 == 0 任务没有被处理完、isJ2 == 1 任务已经被处理完了
        if (storageTaskIsProcessedComplete == 0) {
            // 开启循环
            do {
                // 当前时间戳累加
                teSBNew += 86400L;
                System.out.println((isGetTaskPattern==0?"获取数据库任务信息-t":"获取镜像任务信息-t")+" - "+isGetTaskPattern);
                // 调用获取任务综合信息方法
                Map<String, Object> jumpDay = getJumpDay(random, grpB, dep, 1, teSBNew,isGetTaskPattern
                        ,task.getId_C(),xbAndSbAll,objTaskAll,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                        ,newestLastCurrentTimestamp);
                // 获取任务集合
                List<Task> tasksInside = (List<Task>) jumpDay.get("tasks");
                // 获取任务余剩时间
                long zonInside = (long) jumpDay.get("zon");
                // 控制结束外层循环参数，zb == 0 不结束外层循环、zb == 1 结束外层循环
                int controlLoopOuterLayerEnd = 0;
                // 遍历任务集合
                for (int k = 0; k < tasksInside.size(); k++) {
                    // 获取对比任务1
                    Task task1Xx = tasksInside.get(k);
                    // 判断（i2（当前任务下标）+1）小于任务集合总长度
                    if ((k + 1) < tasksInside.size()) {
                        // 获取对比任务2
                        Task task2Xx = tasksInside.get(k + 1);
                        // 调用冲突处理核心方法
                        handleTimeConflictCoreInfo = handleTimeConflictCore(task, task1Xx, task2Xx, zonInside, tasksInside
                                , k, conflict, teSBNew,random,grpB,dep,teDate,isGetTaskPattern,0,sho
                                ,csSta,randomAll,xbAndSbAll,objTaskAll,storageTaskWhereTime,allImageTotalTime,allImageTasks
                                ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,allImageTeDate,isComprehensiveHandle);
                        // 获取任务余剩时间
                        zonInside = handleTimeConflictCoreInfo.getLong("zon");
                        // 获取存储任务是否被处理完状态参数：isJ2 == 0 任务没有被处理完、isJ2 == 1 任务已经被处理完了
                        storageTaskIsProcessedComplete = handleTimeConflictCoreInfo.getInteger("storageTaskIsProcessedComplete");
                        // 获取当前时间戳
                        teSBNew = handleTimeConflictCoreInfo.getLong("teSB");
                        tePFinish = handleTimeConflictCoreInfo.getLong("tePFinish");
                        endTime = handleTimeConflictCoreInfo.getLong("endTime");
                        taskIsProcessedComplete = handleTimeConflictCoreInfo.getInteger("taskIsProcessedComplete");
                        conflictHandlePattern = handleTimeConflictCoreInfo.getInteger("conflictHandlePattern");
                        System.out.println("storageTaskIsProcessedComplete-2:" + storageTaskIsProcessedComplete);
//                        isEnd = handleTimeConflictCoreInfo.getInteger("isEnd")==null?0:handleTimeConflictCoreInfo.getInteger("isEnd");
                        // 获取存储任务是否被处理完状态参数：isJ2 == 0 任务没有被处理完、isJ2 == 1 任务已经被处理完了
                        if (storageTaskIsProcessedComplete == 1) {
                            controlLoopOuterLayerEnd = 1;
                            // isGetTaskPattern = 0 获取数据库任务信息、 = 1 获取镜像任务信息
                            if (isGetTaskPattern == 0) {
                                setTasksAndZon(tasksInside,grpB,dep,teSBNew,zonInside,objTaskAll);
                            } else {
                                System.out.println("handleTimeConflictEasy:写入镜像");
                                // 调用写入镜像任务集合方法
                                setImageTasks(tasksInside,grpB,dep,teSBNew,allImageTasks);
                                // 调用写入镜像任务余剩时间方法
                                setImageZon(zonInside,grpB,dep,teSBNew,allImageTotalTime);
                            }
                            break;
                        } else if (k + 1 == tasksInside.size() - 1) {
                            System.out.println("最后一个任务判断写入:"+teSBNew);
                            // isGetTaskPattern = 0 获取数据库任务信息、 = 1 获取镜像任务信息
                            if (isGetTaskPattern == 0) {
                                setTasksAndZon(tasksInside,grpB,dep,teSBNew,zonInside,objTaskAll);
                            } else {
                                // 调用写入镜像任务集合方法
                                setImageTasks(tasksInside,grpB,dep,teSBNew,allImageTasks);
                                // 调用写入镜像任务余剩时间方法
                                setImageZon(zonInside,grpB,dep,teSBNew,allImageTotalTime);
                            }
                        }
                    }
                }
                // 控制结束外层循环参数，zb == 0 不结束外层循环、zb == 1 结束外层循环
                if (controlLoopOuterLayerEnd == 1) {
                    break;
                }
                // 判断isJ强制停止参数等于10
                if (xin.getInteger(randomAll) == 510) {
                    System.out.println("进入isJ强制结束!!!");
                    // 赋值强制停止出现后的记录参数
                    isQzTz.put(randomAll,1);
                    break;
                }
            } while (true);
        }
        int isEnd = handleTimeConflictCoreInfo.getInteger("isEnd")==null?0:handleTimeConflictCoreInfo.getInteger("isEnd");
//        System.out.println("isEnd:"+isEnd);
        if (isEnd == 1) {
            System.out.println("没有完全结束:");
            System.out.println(JSON.toJSONString(task));
        }
        // 创建返回结果对象
        JSONObject result = new JSONObject();
        result.put("zon",zon);
        // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
        result.put("taskIsProcessedComplete",taskIsProcessedComplete);
        result.put("tePFinish",tePFinish);
        result.put("endTime",endTime);
        result.put("conflictHandlePattern",conflictHandlePattern);
        return result;
    }

    /**
     * 处理时间冲突核心方法
     * @param task	当前任务信息
     * @param contrastTaskOne	对比任务信息1
     * @param contrastTaskTwo	对比任务信息2
     * @param zon	任务余剩时间
     * @param tasks	任务集合
     * @param i	任务下标
     * @param conflict	冲突任务集合
     * @param teSB 当前时间戳
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param teDate	当前处理的任务的所在日期对象
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param getCurrentTimeStampPattern    getCurrentTimeStampPattern = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
     * @param sho 用于存储判断镜像是否是第一个被冲突的产品
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param objTaskAll	全局任务信息
     * @param storageTaskWhereTime	存储任务所在日期
     * @param allImageTotalTime	全局镜像任务余剩总时间信息
     * @param allImageTasks	全局镜像任务列表信息
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyRefState	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param allImageTeDate 镜像任务所在日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public JSONObject handleTimeConflictCore(Task task, Task contrastTaskOne, Task contrastTaskTwo, Long zon
            , List<Task> tasks, int i, List<Task> conflict, long teSB, String random, String grpB
            , String dep, JSONObject teDate, Integer isGetTaskPattern, Integer getCurrentTimeStampPattern
            , JSONObject sho
            , int csSta, String randomAll, JSONObject xbAndSbAll, JSONObject objTaskAll
            , JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks, JSONObject onlyFirstTimeStamp
            , JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState,JSONObject allImageTeDate,boolean isComprehensiveHandle) {
//        System.out.println("处理时间冲突核心方法-q");
        // 定义存储最后结束时间
        long tePFinish = 0;
        // 定义存储当天时间
        long endTime = 0;
        // 创建创建返回结果对象
        JSONObject result = new JSONObject();
        // taskIsProcessedComplete ：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
        result.put("taskIsProcessedComplete",0);
        // currentOnlyNumberState 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
        int currentOnlyNumberState = onlyRefState.getInteger(random);
        // 存储任务是否被处理完状态参数：storageTaskIsProcessedComplete == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
        int storageTaskIsProcessedComplete = 0;
        // 获取余剩时间（对比任务2的开始时间-对比任务1的结束时间）
        long remainingTime = contrastTaskTwo.getTePStart() - contrastTaskOne.getTePFinish();
        long teDurTotal;
        /*
         * 存储冲突处理模式参数：conflictHandlePattern
         * 1、isP == 0 正常处理（使用所有冲突处理方法）不携带任务集合当前下标往后的循环处理
         * 2、isP == 1 携带任务集合当前下标往后的循环处理 -
         * 3、isP == 2 对比任务2开始时间加上当前任务总时间大于对比任务3的开始时间快速处理冲突模式
         * 4、isP == 3 对比任务3不属于系统任务快速处理冲突模式
         * 5、isP == 4 正常处理（使用所有冲突处理方法）携带任务集合当前下标往后的循环处理
         */
        int conflictHandlePattern = 0;
        // 控制任务下标累加数参数：taskIndexAccumulation iZ == 1 下标加1、iZ == 2 下标加2
        int taskIndexAccumulation = 1;
        // 产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer prodState = sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("prodState");
        // getCurrentTimeStampPattern = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
        if (getCurrentTimeStampPattern == 1) {
//            System.out.println("处理时间冲突核心方法-q--1");
            conflictHandlePattern = 4;
            // 获取对比任务3 contrastTaskThree
            Task contrastTaskThree = tasks.get(i+2);
//            System.out.println("contrastTaskThree:");
//            System.out.println(JSON.toJSONString(contrastTaskThree));
            // 判断对比任务是系统任务
            if (contrastTaskThree.getPriority() == -1) {
                // 获取开始时间（对比任务2的开始时间+当前任务的总时间）
                long startTime = contrastTaskTwo.getTePStart() + task.getTeDurTotal();
                // 判断开始时间大于对比任务3的开始时间
                if (startTime > contrastTaskThree.getTePStart()) {
                    long timeDiffer = contrastTaskTwo.getTePStart() - contrastTaskOne.getTePFinish();
                    int accumulation = 1;
                    if (timeDiffer > 0) {
                        long durTotal = task.getTeDurTotal() - timeDiffer;
                        boolean isResult = false;
                        if (durTotal > 0) {
                            // 任务余剩时间累减
                            zon -= timeDiffer;
                            System.out.println(JSON.toJSONString(tasks));
                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                            tasks.add(i+accumulation, TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                    , (contrastTaskOne.getTePFinish()+timeDiffer),timeDiffer,task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = (contrastTaskOne.getTePFinish()+timeDiffer);
                            endTime = tasks.get(0).getTePStart();
                            teDurTotal = timeDiffer;
                            // 更新当前任务的开始时间
                            task.setTePStart(contrastTaskOne.getTePFinish());
                            // 更新任务的结束时间
                            task.setTePFinish((contrastTaskOne.getTePFinish()+timeDiffer));
                            task.setTeDurTotal(durTotal);
                            System.out.println("处理时间冲突核心方法--xx-1");
                            accumulation++;
                        } else if (durTotal == 0) {
                            // 任务余剩时间累减
                            zon -= timeDiffer;
                            System.out.println(JSON.toJSONString(tasks));
                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                            tasks.add(i+1, TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                    , (contrastTaskOne.getTePFinish()+timeDiffer),timeDiffer,task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = (contrastTaskOne.getTePFinish()+timeDiffer);
                            endTime = tasks.get(0).getTePStart();
                            teDurTotal = timeDiffer;
                            // 更新当前任务的开始时间
                            task.setTePStart(contrastTaskOne.getTePFinish());
                            // 更新任务的结束时间
                            task.setTePFinish((contrastTaskOne.getTePFinish()+timeDiffer));
                            task.setTeDurTotal(0L);
                            System.out.println("处理时间冲突核心方法--xx-2");
                            conflictHandlePattern = 5;
                            result.put("taskIsProcessedComplete",2);
                            storageTaskIsProcessedComplete = 1;
                            isResult = true;
                        } else {
                            // 任务余剩时间累减
                            zon -= task.getTeDurTotal();
                            System.out.println(JSON.toJSONString(tasks));
                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                            tasks.add(i+1, TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                    , (contrastTaskOne.getTePFinish()+task.getTeDurTotal()),task.getTeDurTotal(),task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = (contrastTaskOne.getTePFinish()+task.getTeDurTotal());
                            endTime = tasks.get(0).getTePStart();
                            teDurTotal = 0L;
                            // 更新当前任务的开始时间
                            task.setTePStart(contrastTaskOne.getTePFinish());
                            // 更新任务的结束时间
                            task.setTePFinish((contrastTaskOne.getTePFinish()+task.getTeDurTotal()));
                            task.setTeDurTotal(0L);
                            System.out.println("处理时间冲突核心方法--xx-3");
                            conflictHandlePattern = 5;
                            result.put("taskIsProcessedComplete",2);
                            storageTaskIsProcessedComplete = 1;
                            isResult = true;
                        }
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,teDurTotal);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                        .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                        setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                ,teDurTotal,allImageTeDate,isGetTaskPattern,endTime);
                        if (isResult) {
                            result.put("zon",zon);
                            result.put("hTeStart",tePFinish);
                            result.put("teSB",teSB);
                            result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                            result.put("tePFinish",tePFinish);
                            result.put("endTime",endTime);
                            result.put("conflictHandlePattern",conflictHandlePattern);
                            System.out.println("赋值的--处理时间冲突核心方法--xx-2 或 处理时间冲突核心方法--xx-3");
                            return result;
                        }
                    }
                    if (task.getPriority() < contrastTaskOne.getPriority()) {
                        boolean isResult = false;
                        // 任务余剩时间累加
                        zon += contrastTaskOne.getTeDurTotal();
                        // 冲突任务集合添加对比任务2的任务信息
                        conflict.add(TaskObj.getTaskX(contrastTaskOne.getTePStart(),contrastTaskOne.getTePFinish(),contrastTaskOne.getTeDurTotal(),contrastTaskOne));
                        // 调用添加或更新产品状态方法
                        addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskOne.getId_O(),contrastTaskOne.getIndex().toString(),0);
                        long teDurTotalNew = task.getTeDurTotal()-contrastTaskOne.getTeDurTotal();
                        if (teDurTotalNew > 0) {
                            // 任务余剩时间累减
                            zon -= contrastTaskOne.getTeDurTotal();
                            System.out.println(JSON.toJSONString(tasks));
                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                            tasks.set((i+accumulation)-1, TaskObj.getTaskX(contrastTaskOne.getTePStart()
                                    , contrastTaskOne.getTePFinish(),contrastTaskOne.getTeDurTotal(),task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = contrastTaskThree.getTePStart();
                            endTime = tasks.get(0).getTePStart();
                            teDurTotal = contrastTaskOne.getTeDurTotal();
                            // 更新当前任务的开始时间
                            task.setTePStart(contrastTaskOne.getTePStart());
                            // 更新任务的结束时间
                            task.setTePFinish(contrastTaskOne.getTePFinish());
                            task.setTeDurTotal(teDurTotalNew);
                            System.out.println("处理时间冲突核心方法-one-1");

                        } else {
                            // 任务余剩时间累减
                            zon -= task.getTeDurTotal();
                            System.out.println(JSON.toJSONString(tasks));
                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                            tasks.set((i+accumulation)-1, TaskObj.getTaskX(contrastTaskOne.getTePStart()
                                    , (contrastTaskOne.getTePStart()+task.getTeDurTotal()),task.getTeDurTotal(),task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = contrastTaskThree.getTePStart();
                            endTime = tasks.get(0).getTePStart();
                            teDurTotal = task.getTeDurTotal();
                            // 更新当前任务的开始时间
                            task.setTePStart(contrastTaskOne.getTePStart());
                            // 更新任务的结束时间
                            task.setTePFinish(contrastTaskOne.getTePStart()+task.getTeDurTotal());
                            task.setTeDurTotal(0L);
                            System.out.println("处理时间冲突核心方法-one-2");
                            result.put("taskIsProcessedComplete",2);
                            storageTaskIsProcessedComplete = 1;
                            isResult = true;
                        }
                        conflictHandlePattern = 5;
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,teDurTotal);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                        .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                        setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                ,teDurTotal,allImageTeDate,isGetTaskPattern,endTime);
                        if (isResult) {
                            result.put("zon",zon);
                            result.put("hTeStart",tePFinish);
                            result.put("teSB",teSB);
                            result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                            result.put("tePFinish",tePFinish);
                            result.put("endTime",endTime);
                            result.put("conflictHandlePattern",conflictHandlePattern);
                            System.out.println("赋值的--处理时间冲突核心方法-one-2");
                            return result;
                        }
                    }
                    // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                    if (currentOnlyNumberState == 0) {
                        if (task.getPriority() < contrastTaskTwo.getPriority()) {
                            // 获取时间差（对比任务3的开始时间-当前任务的开始时间）
                            long timeDifference = contrastTaskThree.getTePStart() - contrastTaskTwo.getTePStart();
                            // 获取余剩总时间（当前任务总时间-时间差）
                            remainingTime = task.getTeDurTotal() - timeDifference;
                            // 任务余剩时间累加
                            zon += contrastTaskTwo.getTeDurTotal();
                            // 冲突任务集合添加对比任务2的任务信息
                            conflict.add(TaskObj.getTaskX(task.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                            // 调用添加或更新产品状态方法
                            addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                            // 任务余剩时间累减
                            zon -= timeDifference;
                            System.out.println(JSON.toJSONString(tasks));
                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                            tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart(), contrastTaskThree.getTePStart(),timeDifference,task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = contrastTaskThree.getTePStart();
                            endTime = tasks.get(0).getTePStart();
                            teDurTotal = timeDifference;
                            // 更新当前任务的开始时间
                            task.setTePStart(contrastTaskTwo.getTePStart());
                            // 更新任务的结束时间
                            task.setTePFinish(contrastTaskThree.getTePStart());
                            // 更新当前任务的总时间
                            task.setTeDurTotal(remainingTime);
                            System.out.println("处理时间冲突核心方法--1");
                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,teDurTotal);
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                    ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                            .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                            setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                    ,teDurTotal,allImageTeDate,isGetTaskPattern,endTime);
                            taskIndexAccumulation = 2;
                            conflictHandlePattern = 7;
                        }
                    } else {
                        // 存储是否反回结果
                        boolean isResult = false;
                        // 获取时间差（对比任务3的开始时间-对比任务2的开始时间）
                        long timeDifference = contrastTaskThree.getTePStart() - contrastTaskTwo.getTePStart();
                        // 获取余剩总时间（当前任务总时间-时间差）
                        remainingTime = task.getTeDurTotal() - timeDifference;
                        // 任务余剩时间累加
                        zon += contrastTaskTwo.getTeDurTotal();
                        // 冲突任务集合添加对比任务2的任务信息
                        conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                        // 调用添加或更新产品状态方法
                        addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                        if (remainingTime > 0) {
                            // 任务余剩时间累减
                            zon -= timeDifference;
                            System.out.println(JSON.toJSONString(tasks));
                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                            tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart()
                                    , contrastTaskThree.getTePStart(),timeDifference,task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = contrastTaskThree.getTePStart();
                            endTime = tasks.get(0).getTePStart();
                            teDurTotal = timeDifference;
                            // 更新当前任务的开始时间
                            task.setTePStart(contrastTaskTwo.getTePStart());
                            // 更新任务的结束时间
                            task.setTePFinish(contrastTaskThree.getTePStart());
                            task.setTeDurTotal(remainingTime);
                            System.out.println("处理时间冲突核心方法--2");
                            conflictHandlePattern = 5;
                        } else {
                            // 任务余剩时间累减
                            zon -= task.getTeDurTotal();
                            System.out.println(JSON.toJSONString(tasks));
                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                            tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart()
                                    , (contrastTaskTwo.getTePStart()+task.getTeDurTotal()),task.getTeDurTotal(),task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = (contrastTaskTwo.getTePStart()+task.getTeDurTotal());
                            endTime = tasks.get(0).getTePStart();
                            teDurTotal = task.getTeDurTotal();
                            // 更新当前任务的开始时间
                            task.setTePStart(contrastTaskTwo.getTePStart());
                            // 更新任务的结束时间
                            task.setTePFinish((contrastTaskTwo.getTePStart()+task.getTeDurTotal()));
                            task.setTeDurTotal(0L);
                            // 更新当前任务的开始时间
                            task.setTePStart(contrastTaskTwo.getTePStart());
                            System.out.println("处理时间冲突核心方法--3");
                            isResult = true;
                            storageTaskIsProcessedComplete = 1;
                        }
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,teDurTotal);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                        .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                        setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                ,teDurTotal,allImageTeDate,isGetTaskPattern,endTime);
                        taskIndexAccumulation += accumulation;
                        if (isResult) {
                            result.put("zon",zon);
                            result.put("hTeStart",tePFinish);
                            result.put("teSB",teSB);
                            result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                            result.put("tePFinish",tePFinish);
                            result.put("endTime",endTime);
                            result.put("conflictHandlePattern",conflictHandlePattern);
                            System.out.println("赋值的--处理时间冲突核心方法--3");
                            return result;
                        }
                    }
                } else {
                    conflictHandlePattern = 2;
                }
            } else {
                conflictHandlePattern = 3;
            }

            if (conflictHandlePattern == 2 || conflictHandlePattern == 3) {
//                System.out.println("conflictHandlePattern:"+conflictHandlePattern);
                long timeDiffer = contrastTaskTwo.getTePStart() - contrastTaskOne.getTePFinish();
                // 累加下标
                int accumulation = 1;
                if (timeDiffer > 0) {
                    long durTotal = task.getTeDurTotal() - timeDiffer;
                    // 存储是否返回结果
                    boolean isResult = false;
//                    System.out.println("timeDiffer:"+timeDiffer+" - durTotal:"+durTotal);
                    if (durTotal > 0) {
                        // 任务余剩时间累减
                        zon -= timeDiffer;
                        System.out.println(JSON.toJSONString(tasks));
                        // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                        tasks.add(i+accumulation, TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                , (contrastTaskOne.getTePFinish()+timeDiffer),timeDiffer,task));
                        System.out.println(JSON.toJSONString(tasks));
                        tePFinish = (contrastTaskOne.getTePFinish()+timeDiffer);
                        endTime = tasks.get(0).getTePStart();
                        teDurTotal = timeDiffer;
                        // 更新当前任务的开始时间
                        task.setTePStart(contrastTaskOne.getTePFinish());
                        // 更新任务的结束时间
                        task.setTePFinish((contrastTaskOne.getTePFinish()+timeDiffer));
                        task.setTeDurTotal(durTotal);
                        System.out.println("处理时间冲突核心方法--xx-4");
                        accumulation++;
                        taskIndexAccumulation += 1;
                    } else if (durTotal == 0) {
                        // 任务余剩时间累减
                        zon -= timeDiffer;
                        System.out.println(JSON.toJSONString(tasks));
                        // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                        tasks.add(i+1, TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                , (contrastTaskOne.getTePFinish()+timeDiffer),timeDiffer,task));
                        System.out.println(JSON.toJSONString(tasks));
                        tePFinish = (contrastTaskOne.getTePFinish()+timeDiffer);
                        endTime = tasks.get(0).getTePStart();
                        teDurTotal = timeDiffer;
                        // 更新当前任务的开始时间
                        task.setTePStart(contrastTaskOne.getTePFinish());
                        // 更新任务的结束时间
                        task.setTePFinish((contrastTaskOne.getTePFinish()+timeDiffer));
                        task.setTeDurTotal(0L);
                        System.out.println("处理时间冲突核心方法--xx-5");
                        conflictHandlePattern = 5;
                        result.put("taskIsProcessedComplete",2);
                        storageTaskIsProcessedComplete = 1;
                        isResult = true;
                    } else {
                        // 任务余剩时间累减
                        zon -= task.getTeDurTotal();
                        System.out.println(JSON.toJSONString(tasks));
                        // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                        tasks.add(i+1, TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                , (contrastTaskOne.getTePFinish()+task.getTeDurTotal()),task.getTeDurTotal(),task));
                        System.out.println(JSON.toJSONString(tasks));
                        tePFinish = (contrastTaskOne.getTePFinish()+task.getTeDurTotal());
                        endTime = tasks.get(0).getTePStart();
                        teDurTotal = 0L;
                        // 更新当前任务的开始时间
                        task.setTePStart(contrastTaskOne.getTePFinish());
                        // 更新任务的结束时间
                        task.setTePFinish((contrastTaskOne.getTePFinish()+task.getTeDurTotal()));
                        task.setTeDurTotal(0L);
                        System.out.println("处理时间冲突核心方法--xx-6");
                        conflictHandlePattern = 5;
                        result.put("taskIsProcessedComplete",2);
                        storageTaskIsProcessedComplete = 1;
                        isResult = true;
                    }
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,teDurTotal);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex()
                                    .toString()).getInteger("prodState"),storageTaskWhereTime);
                    setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                            ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                            ,teDurTotal,allImageTeDate,isGetTaskPattern,endTime);
                    if (isResult) {
                        result.put("zon",zon);
                        result.put("hTeStart",tePFinish);
                        result.put("teSB",teSB);
                        result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                        result.put("tePFinish",tePFinish);
                        result.put("endTime",endTime);
                        result.put("conflictHandlePattern",conflictHandlePattern);
                        System.out.println("赋值的--处理时间冲突核心方法--xx-5 或 处理时间冲突核心方法--xx-6");
                        return result;
                    }
                }
                // 获取开始时间（对比任务2的开始时间+当前任务的总时间）
                long startTime = contrastTaskTwo.getTePStart()+task.getTeDurTotal();
                // 存储是否返回结果
                boolean isResult = false;
                // 判断开始时间大于对比任务3的开始时间
                if (startTime > contrastTaskThree.getTePFinish()) {
                    // 获取时间差2（当前任务的总时间-对比任务2的总时间）
                    long timeDifference = task.getTeDurTotal() - contrastTaskTwo.getTeDurTotal();
                    // 更新当前任务的总时间
                    task.setTeDurTotal(timeDifference);
                    // 任务余剩时间累加
                    zon += contrastTaskTwo.getTeDurTotal();
                    // 冲突任务集合添加对比任务2的任务信息
                    conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                    // 调用添加或更新产品状态方法
                    addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                    // 任务余剩时间累减
                    zon -= contrastTaskTwo.getTeDurTotal();
                    System.out.println(JSON.toJSONString(tasks));
                    // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                    tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart(),(contrastTaskTwo.getTePStart()+contrastTaskTwo.getTeDurTotal())
                            ,contrastTaskTwo.getTeDurTotal(),task));
                    System.out.println(JSON.toJSONString(tasks));
                    tePFinish = (contrastTaskTwo.getTePStart()+contrastTaskTwo.getTeDurTotal());
                    endTime = tasks.get(0).getTePStart();
                    teDurTotal = contrastTaskTwo.getTeDurTotal();
                    conflictHandlePattern = 1;
                    System.out.println("处理时间冲突核心方法--4");
                } else {
                    long surplusTime = task.getTeDurTotal() - contrastTaskTwo.getTeDurTotal();
                    // 任务余剩时间累加
                    zon += contrastTaskTwo.getTeDurTotal();
                    // 冲突任务集合添加对比任务2的任务信息
                    conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart()
                            ,contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                    // 调用添加或更新产品状态方法
                    addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O()
                            ,contrastTaskTwo.getIndex().toString(),0);
                    if (surplusTime > 0) {
                        // 任务余剩时间累减
                        zon -= contrastTaskTwo.getTeDurTotal();
                        System.out.println(JSON.toJSONString(tasks));
                        // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                        tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart(),(contrastTaskTwo.getTePStart()+contrastTaskTwo.getTeDurTotal())
                                ,contrastTaskTwo.getTeDurTotal(),task));
                        System.out.println(JSON.toJSONString(tasks));
                        tePFinish = (contrastTaskTwo.getTePStart()+contrastTaskTwo.getTeDurTotal());
                        endTime = tasks.get(0).getTePStart();
                        teDurTotal = contrastTaskTwo.getTeDurTotal();
                        task.setTeDurTotal(surplusTime);
                        System.out.println("处理时间冲突核心方法--5-1");
                        conflictHandlePattern = 4;
                    } else {
                        // 任务余剩时间累减
                        zon -= task.getTeDurTotal();
                        System.out.println(JSON.toJSONString(tasks));
                        // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                        tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart(),(contrastTaskTwo.getTePStart()+task.getTeDurTotal())
                                ,task.getTeDurTotal(),task));
                        System.out.println(JSON.toJSONString(tasks));
                        tePFinish = (contrastTaskTwo.getTePStart()+task.getTeDurTotal());
                        endTime = tasks.get(0).getTePStart();
                        teDurTotal = task.getTeDurTotal();
                        task.setTeDurTotal(0L);
                        System.out.println("处理时间冲突核心方法--5-2");
                        result.put("taskIsProcessedComplete",2);
                        storageTaskIsProcessedComplete = 1;
                        isResult = true;
                        conflictHandlePattern = 3;
                    }
                }
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,teDurTotal);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                        ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex()
                                .toString()).getInteger("prodState"),storageTaskWhereTime);
                setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                        ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                        ,teDurTotal,allImageTeDate,isGetTaskPattern,endTime);
                if (isResult) {
                    result.put("zon",zon);
                    result.put("hTeStart",tePFinish);
                    result.put("teSB",teSB);
                    result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                    result.put("tePFinish",tePFinish);
                    result.put("endTime",endTime);
                    result.put("conflictHandlePattern",conflictHandlePattern);
                    System.out.println("赋值的--处理时间冲突核心方法--5");
                    return result;
                }
            }
        }
        try {
            long teS = getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp);
            if (conflictHandlePattern == 0) {
                // 存储是否继续
                boolean isContinue = true;
                // 累加下标
                int accumulation = 1;
                // 判断余剩总时间大于0
                if (remainingTime > 0) {
                    // 获取时间差（余剩总时间-当前任务的开始时间）
                    long timeDifference = remainingTime - task.getTeDurTotal();
                    // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                    if (currentOnlyNumberState == 0) {
                        if (task.getTePStart() < contrastTaskTwo.getTePStart() && task.getTePStart() > contrastTaskOne.getTePFinish()) {
                            long surplusTime = contrastTaskTwo.getTePStart() - task.getTePStart();
                            // 判断时间差大于等于0
                            if (timeDifference >= 0) {
                                System.out.println(JSON.toJSONString(tasks));
                                // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                                tasks.add(i+accumulation, TaskObj.getTaskX(task.getTePStart()
                                        ,(task.getTePStart()+task.getTeDurTotal())
                                        ,task.getTeDurTotal(),task));
                                System.out.println(JSON.toJSONString(tasks));
                                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                addOrUpdateTeDate(teS,teDate,task.getTeDurTotal());
                                // 调用判断产品状态再调用写入任务所在日期方法的方法
                                putTeDate(task.getId_O(), task.getIndex(),teS,prodState,storageTaskWhereTime);
                                setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                        ,teS,task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,tasks.get(0).getTePStart());
                                // 任务余剩时间累减
                                zon -= task.getTeDurTotal();
                                // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                result.put("taskIsProcessedComplete",2);
                                result.put("zon",zon);
                                result.put("hTeStart",(task.getTePStart()+task.getTeDurTotal()));
                                result.put("teSB",teSB);
                                storageTaskIsProcessedComplete = 1;
                                result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                                result.put("tePFinish",(task.getTePStart()+task.getTeDurTotal()));
                                result.put("endTime",tasks.get(0).getTePStart());
                                conflictHandlePattern = 3;
                                result.put("conflictHandlePattern",conflictHandlePattern);
                                System.out.println("进入这里--=1");
                                return result;
                            } else {
                                // 获取时间差2（当前任务的总时间-余剩总时间）
                                long timeDifferenceNew = task.getTeDurTotal() - surplusTime;
                                System.out.println(JSON.toJSONString(tasks));
                                if (timeDifferenceNew > 0) {
                                    // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                                    tasks.add(i+accumulation, TaskObj.getTaskX(task.getTePStart(),(task.getTePStart()+surplusTime),surplusTime,task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (task.getTePStart()+surplusTime);
                                    endTime = tasks.get(0).getTePStart();
                                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                    addOrUpdateTeDate(teS,teDate,surplusTime);
                                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                                    putTeDate(task.getId_O(), task.getIndex(),teS,prodState,storageTaskWhereTime);
                                    setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                            ,teS,surplusTime,allImageTeDate,isGetTaskPattern,endTime);
                                    // 更新当前任务的总时间
                                    task.setTeDurTotal(timeDifferenceNew);
                                    // 更新当前任务的开始时间
                                    task.setTePStart((task.getTePStart()+surplusTime));
                                    // 更新任务的结束时间
                                    task.setTePFinish((task.getTePStart()+surplusTime)+task.getTeDurTotal());
                                    System.out.println("进入这里++=1:");
                                    currentOnlyNumberState = 1;
                                    // 添加存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                                    onlyRefState.put(random,1);
                                    // 任务余剩时间累减
                                    zon -= surplusTime;
                                    accumulation++;
                                    conflictHandlePattern = 4;
                                } else {
                                    // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                                    tasks.add(i+accumulation, TaskObj.getTaskX(task.getTePStart(),(task.getTePStart()+task.getTeDurTotal()),task.getTeDurTotal(),task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (task.getTePStart()+task.getTeDurTotal());
                                    endTime = tasks.get(0).getTePStart();
                                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                    addOrUpdateTeDate(teS,teDate,task.getTeDurTotal());
                                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                                    putTeDate(task.getId_O(), task.getIndex(),teS,prodState,storageTaskWhereTime);
                                    setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                            ,teS,task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                    // 更新当前任务的总时间
                                    task.setTeDurTotal(0L);
                                    // 更新当前任务的开始时间
                                    task.setTePStart((task.getTePStart()+task.getTeDurTotal()));
                                    // 更新任务的结束时间
                                    task.setTePFinish((task.getTePStart()+task.getTeDurTotal()));
                                    System.out.println("进入这里++=1-New:");
                                    // 添加存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                                    onlyRefState.put(random,1);
                                    // 任务余剩时间累减
                                    zon -= task.getTeDurTotal();
                                    // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                    result.put("taskIsProcessedComplete",2);
                                    result.put("zon",zon);
                                    result.put("hTeStart",tePFinish);
                                    result.put("teSB",teSB);
                                    storageTaskIsProcessedComplete = 1;
                                    result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                                    result.put("tePFinish",tePFinish);
                                    result.put("endTime",endTime);
                                    conflictHandlePattern = 3;
                                    result.put("conflictHandlePattern",conflictHandlePattern);
                                    return result;
                                }
                            }
                        }
                        else if (task.getTePStart() >= contrastTaskOne.getTePFinish() && task.getTePFinish() > contrastTaskTwo.getTePStart() && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                            if (contrastTaskTwo.getPriority() != -1) {
                                // 任务余剩时间累加
                                zon += contrastTaskTwo.getTeDurTotal();
                                // 冲突任务集合添加对比任务2的任务信息
                                conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                                // 调用添加或更新产品状态方法
                                addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);

                                // 任务余剩时间累减
                                zon -= task.getTeDurTotal();
                                System.out.println(JSON.toJSONString(tasks));
                                // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                tasks.set((i+accumulation), TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                        , contrastTaskOne.getTePFinish()+task.getTeDurTotal(),task.getTeDurTotal(),task));
                                System.out.println(JSON.toJSONString(tasks));
                                tePFinish = contrastTaskOne.getTePFinish()+task.getTeDurTotal();
                                endTime = tasks.get(0).getTePStart();
                                teDurTotal = task.getTeDurTotal();
                                // 更新当前任务的开始时间
                                task.setTePStart(contrastTaskOne.getTePFinish());
                                // 更新任务的结束时间
                                task.setTePFinish(contrastTaskOne.getTePFinish()+task.getTeDurTotal());
                                task.setTeDurTotal(0L);
                                System.out.println("进入这里--=++=1-1=新的");
                                conflictHandlePattern = 3;
                                isContinue = false;
                            } else {
                                long time = contrastTaskTwo.getTePStart() - contrastTaskOne.getTePFinish();
                                // 任务余剩时间累减
                                zon -= time;
                                System.out.println(JSON.toJSONString(tasks));
                                // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                tasks.add((i+accumulation)-1, TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                        , contrastTaskOne.getTePFinish()+time,time,task));
                                System.out.println(JSON.toJSONString(tasks));
                                tePFinish = contrastTaskOne.getTePFinish()+time;
                                endTime = tasks.get(0).getTePStart();
                                teDurTotal = time;
                                // 更新当前任务的开始时间
                                task.setTePStart(contrastTaskOne.getTePFinish());
                                // 更新任务的结束时间
                                task.setTePFinish(contrastTaskOne.getTePFinish()+time);
                                task.setTeDurTotal(task.getTeDurTotal() - time);
                                System.out.println("进入这里--=++=1-2=新的");
                                conflictHandlePattern = 4;
                            }
                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,teDurTotal);
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                    ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                            .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                            setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                    ,teDurTotal,allImageTeDate,isGetTaskPattern,endTime);
                        }
                        else {
                            System.out.println("进入这里 -- ++ = 1-跳过-");
                        }
                    } else {
                        // 判断时间差大于等于0
                        if (timeDifference >= 0) {
                            System.out.println(JSON.toJSONString(tasks));
                            // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                            tasks.add(i+1, TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                    ,(contrastTaskOne.getTePFinish()+task.getTeDurTotal())
                                    ,task.getTeDurTotal(),task));
                            System.out.println(JSON.toJSONString(tasks));
                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                            addOrUpdateTeDate(teS,teDate,task.getTeDurTotal());
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(), task.getIndex(),teS,prodState,storageTaskWhereTime);
                            setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                    ,teS,task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,tasks.get(0).getTePStart());
                            // 任务余剩时间累减
                            zon -= task.getTeDurTotal();
                            // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                            result.put("taskIsProcessedComplete",2);
                            result.put("zon",zon);
                            result.put("hTeStart",(contrastTaskOne.getTePFinish()+task.getTeDurTotal()));
                            result.put("teSB",teSB);
                            storageTaskIsProcessedComplete = 1;
                            result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                            result.put("tePFinish",(contrastTaskOne.getTePFinish()+task.getTeDurTotal()));
                            result.put("endTime",tasks.get(0).getTePStart());
                            task.setTeDurTotal(0L);
                            conflictHandlePattern = 3;
                            result.put("conflictHandlePattern",conflictHandlePattern);
                            System.out.println("进入这里--=2");
                            return result;
                        } else {
                            // 获取时间差2（当前任务的总时间-余剩总时间）
                            long timeDifferenceNew = task.getTeDurTotal() - remainingTime;
                            System.out.println(JSON.toJSONString(tasks));
                            // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                            tasks.add(i+accumulation, TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+remainingTime)
                                    ,remainingTime,task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = (contrastTaskOne.getTePFinish()+remainingTime);
                            endTime = tasks.get(0).getTePStart();
                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                            addOrUpdateTeDate(teS,teDate,remainingTime);
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(), task.getIndex(),teS,prodState,storageTaskWhereTime);
                            setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex()
                                    ,teS,remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                            // 更新当前任务的总时间
                            task.setTeDurTotal(timeDifferenceNew);
                            // 更新当前任务的开始时间
                            task.setTePStart((contrastTaskOne.getTePFinish()+remainingTime));
                            // 更新任务的结束时间
                            task.setTePFinish((contrastTaskOne.getTePFinish()+remainingTime)+task.getTeDurTotal());
                            System.out.println("进入这里++=2");
                            // 任务余剩时间累减
                            zon -= remainingTime;
                            accumulation++;
                            conflictHandlePattern = 4;
                            isContinue = false;
                        }
                    }
                } else {
                    if (contrastTaskOne.getPriority() == -1 && contrastTaskTwo.getPriority() == -1) {
                        isContinue = false;
                    }
//                    else if (remainingTime == 0) {
//                        isContinue = false;
//                    }
                }
                if (accumulation == 2) {
                    System.out.println("这里等于2:");
                }
                if (isContinue) {
                    // 判断当前任务的结束时间小于等于对比任务2的开始时间
                    if (task.getTePFinish() <= contrastTaskTwo.getTePStart()) {
                        boolean isResult = true;
                        // 判断当前任务的优先级小于对比任务1的优先级
                        if (task.getPriority() < contrastTaskOne.getPriority()) {
                            // 任务余剩时间累加
                            zon += contrastTaskOne.getTeDurTotal();
                            // 冲突任务集合添加对比任务1的任务信息
                            conflict.add(TaskObj.getTaskX(contrastTaskOne.getTePStart(), contrastTaskOne.getTePFinish(),contrastTaskOne.getTeDurTotal(),contrastTaskOne));
                            // 调用添加或更新产品状态方法
                            addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskOne.getId_O(),contrastTaskOne.getIndex().toString(),0);
                            // 任务余剩时间累减
                            zon -= task.getTeDurTotal();
                            System.out.println(JSON.toJSONString(tasks));
                            // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                            tasks.set(i, TaskObj.getTaskX(contrastTaskOne.getTePStart(),(contrastTaskOne.getTePStart()+task.getTeDurTotal())
                                    ,task.getTeDurTotal(),task));
                            System.out.println(JSON.toJSONString(tasks));
                            tePFinish = (contrastTaskOne.getTePStart()+task.getTeDurTotal());
                            endTime = tasks.get(0).getTePStart();
                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                            addOrUpdateTeDate(teS,teDate,task.getTeDurTotal());
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(), task.getIndex(),teS,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                    .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                    ,teS,task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                            // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                            result.put("taskIsProcessedComplete",2);
                            System.out.println("进入这里--=3");
                            storageTaskIsProcessedComplete = 1;
                            conflictHandlePattern = 3;
                        } else {
                            // 判断当前任务的优先级小于对比任务2的优先级
                            if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                long surplusTime = task.getTeDurTotal() - contrastTaskTwo.getTeDurTotal();
                                // 任务余剩时间累加
                                zon += contrastTaskTwo.getTeDurTotal();
                                // 冲突任务集合添加对比任务2的任务信息
                                conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                                // 调用添加或更新产品状态方法
                                addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                                if (surplusTime > 0) {
                                    // 任务余剩时间累减
                                    zon -= contrastTaskTwo.getTeDurTotal();
                                    Task testTask = TaskObj.getTaskX(contrastTaskTwo.getTePStart(),(contrastTaskTwo.getTePStart()+contrastTaskTwo.getTeDurTotal())
                                            ,contrastTaskTwo.getTeDurTotal(),task);
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                    tasks.set(i+1, testTask);
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (contrastTaskTwo.getTePStart()+contrastTaskTwo.getTeDurTotal());
                                    endTime = tasks.get(0).getTePStart();
                                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                    addOrUpdateTeDate(teS,teDate,contrastTaskTwo.getTeDurTotal());
                                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                                    putTeDate(task.getId_O(), task.getIndex(),teS,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                            .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                                    setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                            ,teS,contrastTaskTwo.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                    System.out.println("进入这里--=4-New:");
                                    task.setTeDurTotal(surplusTime);
                                    isResult = false;
                                    conflictHandlePattern = 8;
                                } else {
                                    // 任务余剩时间累减
                                    zon -= task.getTeDurTotal();
                                    Task testTask = TaskObj.getTaskX(contrastTaskTwo.getTePStart(),(contrastTaskTwo.getTePStart()+task.getTeDurTotal())
                                            ,task.getTeDurTotal(),task);
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                    tasks.set(i+1, testTask);
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (contrastTaskTwo.getTePStart()+task.getTeDurTotal());
                                    endTime = tasks.get(0).getTePStart();
                                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                    addOrUpdateTeDate(teS,teDate,task.getTeDurTotal());
                                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                                    putTeDate(task.getId_O(), task.getIndex(),teS,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                            .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                                    setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                            ,teS,task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                    storageTaskIsProcessedComplete = 1;
                                    System.out.println("进入这里--=4:");
                                    task.setTeDurTotal(0L);
                                    result.put("taskIsProcessedComplete",2);
                                    conflictHandlePattern = 3;
                                }
                            }
//                            else {
////                                storageTaskIsProcessedComplete = 0;
//                                result.put("zon",zon);
//                                result.put("hTeStart",tePFinish);
//                                result.put("teSB",teSB);
//                                result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
//                                result.put("tePFinish",tePFinish);
//                                result.put("endTime",endTime);
////                                System.out.println("赋值的--1");
//                                return result;
//                            }
                        }
                        if (isResult) {
                            result.put("zon",zon);
                            result.put("hTeStart",tePFinish);
                            result.put("teSB",teSB);
                            result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                            result.put("tePFinish",tePFinish);
                            result.put("endTime",endTime);
                            result.put("conflictHandlePattern",conflictHandlePattern);
                            System.out.println("赋值的--1");
                            return result;
                        }
                    } else if (task.getTePFinish() <= contrastTaskTwo.getTePFinish()) {
                        // 存储是否结束 = 0：结束 = 1：继续
                        int isEnd = 0;
                        // 存储是否进入时间操作
                        boolean isGetInto = false;
                        // 判断当前任务的优先级小于对比任务1的优先级
                        if (task.getPriority() < contrastTaskOne.getPriority() && task.getTePStart() <= contrastTaskOne.getTePFinish()) {
                            // 任务余剩时间累加
                            zon += contrastTaskOne.getTeDurTotal();
                            // 冲突任务集合添加对比任务1的任务信息
                            conflict.add(TaskObj.getTaskX(contrastTaskOne.getTePStart(),contrastTaskOne.getTePFinish(),contrastTaskOne.getTeDurTotal(),contrastTaskOne));
                            // 调用添加或更新产品状态方法
                            addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskOne.getId_O(),contrastTaskOne.getIndex().toString(),0);
                            long record = contrastTaskOne.getTePStart() + task.getTeDurTotal();
                            if (record <= contrastTaskOne.getTePFinish()) {
                                // 任务余剩时间累减
                                zon -= task.getTeDurTotal();
                                System.out.println(JSON.toJSONString(tasks));
                                // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                tasks.set(i, TaskObj.getTaskX(task.getTePStart(),(task.getTePStart()+task.getTeDurTotal())
                                        ,task.getTeDurTotal(),task));
                                System.out.println(JSON.toJSONString(tasks));
                                tePFinish = (task.getTePStart()+task.getTeDurTotal());
                                endTime = tasks.get(0).getTePStart();
                                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                addOrUpdateTeDate(teS,teDate,task.getTeDurTotal());
                                // 调用判断产品状态再调用写入任务所在日期方法的方法
                                putTeDate(task.getId_O(), task.getIndex(),teS,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                        .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                                setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                        ,teS,task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                System.out.println("进入这里--=5-1");
                                conflictHandlePattern = 3;
                            } else {
                                long recordZon = task.getTeDurTotal() - contrastTaskOne.getTeDurTotal();
                                if (recordZon <= 0) {
                                    // 任务余剩时间累减
                                    zon -= task.getTeDurTotal();
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                    tasks.set(i, TaskObj.getTaskX(contrastTaskOne.getTePStart(),(contrastTaskOne.getTePStart()+task.getTeDurTotal())
                                            ,task.getTeDurTotal(),task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (contrastTaskOne.getTePStart()+task.getTeDurTotal());
                                    endTime = tasks.get(0).getTePStart();
                                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                    addOrUpdateTeDate(teS,teDate,task.getTeDurTotal());
                                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                                    putTeDate(task.getId_O(), task.getIndex(),teS
                                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                                    .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                                    setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                            ,teS, task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                    task.setTeDurTotal(0L);
                                    System.out.println("进入这里--=5-2");
                                    isEnd = 1;
//                                    conflictHandlePattern = 6;
                                    conflictHandlePattern = 3;
                                } else {
                                    // 任务余剩时间累减
                                    zon -= contrastTaskOne.getTeDurTotal();
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                    tasks.set(i, TaskObj.getTaskX(contrastTaskOne.getTePStart(),(contrastTaskOne.getTePStart()+contrastTaskOne.getTeDurTotal())
                                            ,contrastTaskOne.getTeDurTotal(),task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (contrastTaskOne.getTePStart()+contrastTaskOne.getTeDurTotal());
                                    endTime = tasks.get(0).getTePStart();
                                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                    addOrUpdateTeDate(teS,teDate,contrastTaskOne.getTeDurTotal());
                                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                                    putTeDate(task.getId_O(), task.getIndex(),teS
                                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                                    .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                                    setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                            ,teS, contrastTaskOne.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                    task.setTeDurTotal(recordZon);
                                    System.out.println("进入这里--=5-2-New");
//                                    isEnd = 1;
                                    conflictHandlePattern = 7;
                                }
                            }
                            isGetInto = true;
                            // 判断当前任务的优先级小于对比任务2的优先级
                            if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                // 判断当前任务的开始时间大于等于对比任务2的开始时间，并且当前任务的开始时间小于等于对比任务2的结束时间
                                if (task.getTePStart() >= contrastTaskTwo.getTePStart() && task.getTePStart() <= contrastTaskTwo.getTePFinish()) {
                                    System.out.println("进入-优先级小于对比任务2的优先级-");
                                    // 任务余剩时间累加
                                    zon += contrastTaskTwo.getTeDurTotal();
                                    // 冲突任务集合添加对比任务2的任务信息
                                    conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                                    // 调用添加或更新产品状态方法
                                    addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                                    // 任务集合删除指定下标(i+1)任务
                                    tasks.remove((i+1));
                                }
                            }
                        } else {
                            // 判断当前任务的优先级小于对比任务2的优先级
                            if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                // 判断当前任务的开始时间大于等于对比任务2的开始时间，并且当前任务的开始时间小于等于对比任务2的结束时间
                                if (task.getTePStart() >= contrastTaskTwo.getTePStart() && task.getTePStart() <= contrastTaskTwo.getTePFinish()) {
                                    // 任务余剩时间累加
                                    zon += contrastTaskTwo.getTeDurTotal();
                                    // 冲突任务集合添加对比任务2的任务信息
                                    conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
//                                    clearOldTask(contrastTaskTwo.getId_O(), contrastTaskTwo.getDateIndex(), contrastTaskTwo.getId_C());
                                    // 调用添加或更新产品状态方法
                                    addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                                    // 任务余剩时间累减
                                    zon -= task.getTeDurTotal();
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                    tasks.set(i+accumulation, TaskObj.getTaskX(task.getTePStart(),(task.getTePStart()+task.getTeDurTotal())
                                            ,task.getTeDurTotal(),task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (task.getTePStart()+task.getTeDurTotal());
                                    endTime = tasks.get(0).getTePStart();
                                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                    addOrUpdateTeDate(teS
                                            ,teDate,task.getTeDurTotal());
                                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                                    putTeDate(task.getId_O(), task.getIndex(),teS
                                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                                    .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                                    setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                            ,teS, task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                    System.out.println("进入这里--=6");
                                    conflictHandlePattern = 3;
//                                    isGetInto = true;
                                } else {
                                    long time = contrastTaskTwo.getTeDurTotal() - task.getTeDurTotal();
                                    if (time >= 0) {
                                        // 任务余剩时间累加
                                        zon += contrastTaskTwo.getTeDurTotal();
                                        // 冲突任务集合添加对比任务2的任务信息
                                        conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
//                                        clearOldTask(contrastTaskTwo.getId_O(), contrastTaskTwo.getDateIndex(), contrastTaskTwo.getId_C());
                                        // 调用添加或更新产品状态方法
                                        addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                                        // 任务余剩时间累减
                                        zon -= task.getTeDurTotal();
                                        System.out.println(JSON.toJSONString(tasks));
                                        // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                        tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart(),(contrastTaskTwo.getTePStart()+task.getTeDurTotal())
                                                ,task.getTeDurTotal(),task));
                                        System.out.println(JSON.toJSONString(tasks));
                                        tePFinish = (contrastTaskTwo.getTePStart()+task.getTeDurTotal());
                                        endTime = tasks.get(0).getTePStart();
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(teS
                                                ,teDate,task.getTeDurTotal());
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),teS
                                                ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                                        .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                                        setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                ,teS, task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                        task.setTeDurTotal(0L);
                                        System.out.println("进入这里--=6-new-1");
                                        conflictHandlePattern = 3;
                                    } else {
                                        time = task.getTeDurTotal() - contrastTaskTwo.getTeDurTotal();
                                        // 任务余剩时间累加
                                        zon += contrastTaskTwo.getTeDurTotal();
                                        // 冲突任务集合添加对比任务2的任务信息
                                        conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                                        // 调用添加或更新产品状态方法
                                        addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                                        // 任务余剩时间累减
                                        zon -= contrastTaskTwo.getTeDurTotal();
                                        System.out.println(JSON.toJSONString(tasks));
                                        // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                        tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart(),(contrastTaskTwo.getTePStart()+contrastTaskTwo.getTeDurTotal())
                                                ,contrastTaskTwo.getTeDurTotal(),task));
                                        System.out.println(JSON.toJSONString(tasks));
                                        tePFinish = (contrastTaskTwo.getTePStart()+contrastTaskTwo.getTeDurTotal());
                                        endTime = tasks.get(0).getTePStart();
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(teS
                                                ,teDate,contrastTaskTwo.getTeDurTotal());
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),teS
                                                ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                                        .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                                        setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                ,teS, contrastTaskTwo.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                        task.setTeDurTotal(time);
                                        System.out.println("进入这里--=6-new-2");
                                        isEnd = 1;
//                                        conflictHandlePattern = 6;
                                        conflictHandlePattern = 4;
                                    }
                                }
                                isGetInto = true;
                            }
                        }
                        if (isEnd != 1 && conflictHandlePattern != 7) {
                            if (!isGetInto) {
                                // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                result.put("taskIsProcessedComplete",0);
                                // 存储任务是否被处理完状态参数：storageTaskIsProcessedComplete == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
                                result.put("storageTaskIsProcessedComplete",1);
                                System.out.println("赋值的--2x");
                            } else {
                                // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                result.put("taskIsProcessedComplete",2);
                                // 存储任务是否被处理完状态参数：storageTaskIsProcessedComplete == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
                                result.put("storageTaskIsProcessedComplete",1);
                                System.out.println("赋值的--2j");
                            }
                            result.put("isEnd",isEnd);
                            result.put("zon",zon);
                            result.put("hTeStart",tePFinish);
                            result.put("teSB",teSB);
                            result.put("tePFinish",tePFinish);
                            result.put("endTime",endTime);
                            result.put("conflictHandlePattern",conflictHandlePattern);
                            return result;
                        }
                    } else {
//                        conflictHandlePattern = 1;
                        // 判断当前任务的优先级小于对比任务1的优先级
                        if (task.getPriority() < contrastTaskOne.getPriority()) {
                            // 任务余剩时间累加
                            zon += contrastTaskOne.getTeDurTotal();
                            // 冲突任务集合添加对比任务1的任务信息
                            conflict.add(TaskObj.getTaskX(contrastTaskOne.getTePStart(),contrastTaskOne.getTePFinish(),contrastTaskOne.getTeDurTotal(),contrastTaskOne));
                            // 调用添加或更新产品状态方法
                            addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskOne.getId_O(),contrastTaskOne.getIndex().toString(),0);
                            // 判断当前任务的优先级小于对比任务2的优先级
                            if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                // 判断对比任务2的优先级等于系统
                                if (contrastTaskTwo.getPriority() == -1) {
                                    // 获取余剩总时间（对比任务2的开始时间-当前任务的开始时间）
                                    remainingTime = contrastTaskTwo.getTePStart() - task.getTePStart();
                                    // 获取时间差（当前任务总时间-余剩总时间）
                                    long timeDifference = task.getTeDurTotal() - remainingTime;
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                    tasks.set(i, TaskObj.getTaskX(task.getTePStart(),(task.getTePStart()+remainingTime),remainingTime,task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (task.getTePStart()+remainingTime);
                                    endTime = tasks.get(0).getTePStart();
                                    teDurTotal = remainingTime;
                                    // 更新当前任务的总时间
                                    task.setTeDurTotal(timeDifference);
                                    // 更新当前任务的开始时间
                                    task.setTePStart(contrastTaskTwo.getTePFinish());
                                    // 更新任务的结束时间
                                    task.setTePFinish(contrastTaskTwo.getTePFinish());
                                    System.out.println("进入这里++=3");
                                    // 任务余剩时间累减
                                    zon -= remainingTime;
                                    conflictHandlePattern = 1;
                                } else {
                                    // 任务余剩时间累加
                                    zon += contrastTaskTwo.getTeDurTotal();
                                    // 冲突任务集合添加对比任务2的任务信息
                                    conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                                    // 调用添加或更新产品状态方法
                                    addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                                    // 任务集合删除指定下标(i+1)任务
                                    tasks.remove(i+1);
                                    // 任务余剩时间累减
                                    zon -= task.getTeDurTotal();
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                    tasks.set(i, TaskObj.getTaskX(contrastTaskTwo.getTePStart(),(contrastTaskTwo.getTePStart()+task.getTeDurTotal())
                                            ,task.getTeDurTotal(),task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (contrastTaskTwo.getTePStart()+task.getTeDurTotal());
                                    endTime = tasks.get(0).getTePStart();
                                    teDurTotal = task.getTeDurTotal();
                                    System.out.println("进入这里--=7");
                                    conflictHandlePattern = 3;
                                }
                            } else {
                                // 判断对比任务1的总时间大于当前任务的总时间
                                if (contrastTaskOne.getTeDurTotal() > task.getTeDurTotal()) {
                                    // 任务余剩时间累减
                                    zon -= task.getTeDurTotal();
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                    tasks.set(i, TaskObj.getTaskX(contrastTaskOne.getTePStart()
                                            ,(contrastTaskOne.getTePStart()+task.getTeDurTotal())
                                            ,task.getTeDurTotal(),task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (contrastTaskOne.getTePStart()+task.getTeDurTotal());
                                    endTime = tasks.get(0).getTePStart();
                                    teDurTotal = task.getTeDurTotal();
                                    System.out.println("进入新开辟的-1-1");
                                    conflictHandlePattern = 3;
                                } else {
                                    // 获取余剩总时间（当前任务的总时间-对比任务1的总时间）
                                    remainingTime = task.getTeDurTotal() - contrastTaskOne.getTeDurTotal();
                                    // 任务余剩时间累减
                                    zon -= contrastTaskOne.getTeDurTotal();
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                    tasks.set(i, TaskObj.getTaskX(contrastTaskOne.getTePStart()
                                            ,contrastTaskOne.getTePFinish(),contrastTaskOne.getTeDurTotal(),task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = contrastTaskOne.getTePFinish();
                                    endTime = tasks.get(0).getTePStart();
                                    teDurTotal = contrastTaskOne.getTeDurTotal();
                                    // 更新当前任务的总时间
                                    task.setTeDurTotal(remainingTime);
                                    System.out.println("进入新开辟的-1-2");
                                    conflictHandlePattern = 1;
                                }
                            }
                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                            addOrUpdateTeDate(teS,teDate,teDurTotal);
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(), task.getIndex(),teS
                                    ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                            .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                    ,teS, teDurTotal,allImageTeDate,isGetTaskPattern,endTime);
                        } else {
                            // 判断对比任务2的优先级等于系统
                            if (contrastTaskTwo.getPriority() == -1) {
                                if (remainingTime > 0) {
                                    // 获取余剩总时间（对比任务2的开始时间-当前任务的开始时间）
                                    remainingTime = contrastTaskTwo.getTePStart() - task.getTePStart();
                                    // 获取时间差（当前任务总时间-余剩总时间）
                                    long timeDifference = task.getTeDurTotal() - remainingTime;
                                    System.out.println(JSON.toJSONString(tasks));
                                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                                    tasks.set(i, TaskObj.getTaskX(task.getTePStart()
                                            ,(task.getTePStart()+remainingTime),remainingTime,task));
                                    System.out.println(JSON.toJSONString(tasks));
                                    tePFinish = (task.getTePStart()+remainingTime);
                                    endTime = tasks.get(0).getTePStart();
                                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                    addOrUpdateTeDate(teS,teDate,remainingTime);
                                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                                    putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp),prodState,storageTaskWhereTime);
                                    setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                            ,teS, remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                                    // 更新当前任务的总时间
                                    task.setTeDurTotal(timeDifference);
                                    // 更新当前任务的开始时间
                                    task.setTePStart(contrastTaskTwo.getTePFinish());
                                    // 更新任务的结束时间
                                    task.setTePFinish(contrastTaskTwo.getTePFinish());
                                    System.out.println("进入这里++=4");
                                    // 任务余剩时间累减
                                    zon -= remainingTime;
                                    conflictHandlePattern = 1;
                                } else {
                                    System.out.println("时间为零-跳过");
                                }
                            } else {
                                // 判断当前任务的优先级小于对比任务2的优先级
                                if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                    // 任务余剩时间累加
                                    zon += contrastTaskTwo.getTeDurTotal();
                                    // 冲突任务集合添加对比任务2的任务信息
                                    conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),contrastTaskTwo));
                                    // 调用添加或更新产品状态方法
                                    addSho(sho, task.getId_O(),task.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                                    // 获取对比任务3
                                    Task contrastTaskThree = tasks.get(i + 2);
                                    conflictHandlePattern = 1;
                                    // 判断对比任务3的优先级等于系统
                                    if (contrastTaskThree.getPriority() == -1) {
                                        // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                                        if (currentOnlyNumberState == 0) {
                                            // 获取时间差（对比任务3的开始时间-当前任务的开始时间）
                                            long timeDifference = contrastTaskThree.getTePStart() - task.getTePStart();
                                            // 获取余剩总时间（当前任务总时间-时间差）
                                            remainingTime = task.getTeDurTotal() - timeDifference;
                                            // 更新当前任务总时间
                                            task.setTeDurTotal(remainingTime);
                                            // 任务余剩时间累减
                                            zon -= timeDifference;
                                            System.out.println(JSON.toJSONString(tasks));
                                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                            tasks.set(i+accumulation, TaskObj.getTaskX(task.getTePStart()
                                                    ,contrastTaskThree.getTePStart(),timeDifference,task));
                                            System.out.println(JSON.toJSONString(tasks));
                                            tePFinish = contrastTaskThree.getTePStart();
                                            endTime = tasks.get(0).getTePStart();
                                            teDurTotal = timeDifference;
                                            // 更新任务的结束时间
                                            task.setTePFinish(contrastTaskThree.getTePStart());
                                            // 更新当前任务的开始时间
                                            task.setTePStart(task.getTePStart());
                                            System.out.println("进入这里--=8");
                                        } else {
                                            // 获取时间差（对比任务3的开始时间-对比任务2的开始时间）
                                            long timeDifference = contrastTaskThree.getTePStart() - contrastTaskTwo.getTePStart();
                                            // 获取余剩总时间（当前任务总时间-时间差）
                                            remainingTime = task.getTeDurTotal() - timeDifference;
                                            // 更新当前任务总时间
                                            task.setTeDurTotal(remainingTime);
                                            // 任务余剩时间累减
                                            zon -= timeDifference;
                                            System.out.println(JSON.toJSONString(tasks));
                                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                            tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart()
                                                    ,contrastTaskThree.getTePStart(),timeDifference,task));
                                            System.out.println(JSON.toJSONString(tasks));
                                            tePFinish = contrastTaskThree.getTePStart();
                                            endTime = tasks.get(0).getTePStart();
                                            teDurTotal = timeDifference;
                                            // 更新任务的结束时间
                                            task.setTePFinish(contrastTaskThree.getTePStart());
                                            // 更新当前任务的开始时间
                                            task.setTePStart(contrastTaskTwo.getTePStart());
                                            System.out.println("进入这里--=9");
                                        }
                                    } else {
                                        // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                                        Integer onlyRefStateNew = onlyRefState.getInteger((random+"_new"));
                                        if (null == onlyRefStateNew) {
                                            onlyRefStateNew = onlyRefState.getInteger((random));
                                        }
                                        if (currentOnlyNumberState == 0 || onlyRefStateNew == 0) {
                                            if (currentOnlyNumberState == 1) {
                                                System.out.println("等于一进入的:");
                                            }
                                            onlyRefState.put((random+"_new"),1);
                                            // 获取时间差（对比任务2的结束时间-当前任务的开始时间）
                                            long timeDifference = contrastTaskTwo.getTePFinish() - task.getTePStart();
                                            // 获取余剩总时间（当前任务总时间-时间差）
                                            remainingTime = task.getTeDurTotal() - timeDifference;
                                            // 任务余剩时间累减
                                            zon -= timeDifference;
                                            System.out.println(JSON.toJSONString(tasks));
                                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                            tasks.set(i+accumulation, TaskObj.getTaskX(task.getTePStart()
                                                    ,contrastTaskTwo.getTePFinish(),timeDifference,task));
                                            System.out.println(JSON.toJSONString(tasks));
                                            tePFinish = contrastTaskTwo.getTePFinish();
                                            endTime = tasks.get(0).getTePStart();
                                            teDurTotal = timeDifference;
                                            System.out.println("进入这里--=22");
                                            // 更新当前任务的开始时间
                                            task.setTePStart(task.getTePStart());
                                        } else {
                                            // 获取余剩总时间（当前任务总时间-对比任务2的总时间）
                                            remainingTime = task.getTeDurTotal() - contrastTaskTwo.getTeDurTotal();
                                            // 任务余剩时间累减
                                            zon -= contrastTaskTwo.getTeDurTotal();
                                            System.out.println(JSON.toJSONString(tasks));
                                            // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                            tasks.set(i+accumulation, TaskObj.getTaskX(contrastTaskTwo.getTePStart()
                                                    ,contrastTaskTwo.getTePFinish(),contrastTaskTwo.getTeDurTotal(),task));
                                            System.out.println(JSON.toJSONString(tasks));
                                            tePFinish = contrastTaskTwo.getTePFinish();
                                            endTime = tasks.get(0).getTePStart();
                                            teDurTotal = contrastTaskTwo.getTeDurTotal();
                                            System.out.println("进入这里--=23");
                                        }
                                        // 更新当前任务总时间
                                        task.setTeDurTotal(remainingTime);
                                    }
                                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                    addOrUpdateTeDate(teS,teDate,teDurTotal);
                                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                                    putTeDate(task.getId_O(), task.getIndex(),teS
                                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                                    .getIndex().toString()).getInteger("prodState"),storageTaskWhereTime);
                                    setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                            ,teS, teDurTotal,allImageTeDate,isGetTaskPattern,endTime);
                                    taskIndexAccumulation += accumulation;
                                    currentOnlyNumberState = 1;
                                    // 添加存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                                    onlyRefState.put(random,1);
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("都为系统-跳过");
                }
            }
        } catch (Exception ex){
            System.out.println("出现异常");
            ex.printStackTrace();
        }
//        System.out.println("处理时间冲突核心方法-2");
        if (conflictHandlePattern == 1 || conflictHandlePattern == 4 || conflictHandlePattern == 5
                || conflictHandlePattern == 6 || conflictHandlePattern == 7 || conflictHandlePattern == 8) {
            int conflictHandlePatternCopy = conflictHandlePattern;
            // 判断（i（当前任务下标）+iZ（控制任务下标累加数参数））小于任务集合总长度
            if ((i + taskIndexAccumulation) < tasks.size()) {
                // 遍历任务集合
                for (int j = i+taskIndexAccumulation; j < tasks.size(); j++) {
                    // 获取对比任务1
                    Task contrastTaskOneNew = tasks.get(j);
                    // 判断当前任务下标+1小于任务集合总长度
                    if ((j+1) < tasks.size()) {
                        // 获取对比任务2
                        Task contrastTaskTwoNew = tasks.get(j+1);
                        // 判断对比任务1的优先级等于系统，并且对比任务2的优先级等于系统
                        if (contrastTaskOneNew.getPriority() == -1 && contrastTaskTwoNew.getPriority() == -1) {
                            // 获取余剩总时间（对比任务2的开始时间-对比任务1的结束时间）
                            remainingTime = contrastTaskTwoNew.getTePStart() - contrastTaskOneNew.getTePFinish();
                            // 判断余剩总时间大于0
                            if (remainingTime > 0) {
                                // 获取时间差（余剩总时间-当前任务总时间）
                                long timeDifference = remainingTime - task.getTeDurTotal();
                                // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                                if (currentOnlyNumberState == 0) {
                                    // 判断时间差大于等于0
                                    if (timeDifference >= 0) {
                                        System.out.println(JSON.toJSONString(tasks));
                                        // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                        tasks.add(j+1, TaskObj.getTaskX(task.getTePStart()
                                                ,(task.getTePStart()+task.getTeDurTotal())
                                                ,task.getTeDurTotal(),task));
                                        System.out.println(JSON.toJSONString(tasks));
                                        tePFinish = (task.getTePStart()+task.getTeDurTotal());
                                        endTime = tasks.get(0).getTePStart();
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                ,teDate,task.getTeDurTotal());
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp),prodState,storageTaskWhereTime);
                                        setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                , task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                        // 任务余剩时间累减
                                        zon -= task.getTeDurTotal();
                                        result.put("zon",zon);
                                        // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                        result.put("taskIsProcessedComplete",2);
                                        result.put("hTeStart",(task.getTePStart()+task.getTeDurTotal()));
                                        storageTaskIsProcessedComplete = 1;
                                        // 更新当前任务的开始时间
                                        task.setTePStart(task.getTePStart());
                                        // 更新当前任务的结束时间
                                        task.setTePFinish((task.getTePStart()+task.getTeDurTotal()));
                                        System.out.println("进入这里++=5-2");
                                        conflictHandlePattern = conflictHandlePatternCopy;
                                        break;
                                    } else {
                                        // 获取时间差2（当前任务总时间-余剩总时间）
                                        long timeDifferenceNew = task.getTeDurTotal() - remainingTime;
                                        System.out.println(JSON.toJSONString(tasks));
                                        // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                        tasks.add(j+1, TaskObj.getTaskX(task.getTePFinish()
                                                ,(task.getTePFinish()+remainingTime),remainingTime,task));
                                        System.out.println(JSON.toJSONString(tasks));
                                        tePFinish = (task.getTePFinish()+remainingTime);
                                        endTime = tasks.get(0).getTePStart();
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,remainingTime);
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp),prodState,storageTaskWhereTime);
                                        setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                , remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                                        // 更新当前任务的总时间
                                        task.setTeDurTotal(timeDifferenceNew);
                                        // 更新当前任务的开始时间
                                        task.setTePStart(contrastTaskOne.getTePStart());
                                        // 更新当前任务的结束时间
                                        task.setTePFinish(contrastTaskOne.getTePFinish());
                                        System.out.println("进入这里++=5");
                                        // 任务余剩时间累减
                                        zon -= remainingTime;
                                        conflictHandlePattern = 7;
                                    }
                                } else {
                                    // 判断时间差大于等于0
                                    if (timeDifference >= 0) {
                                        System.out.println(JSON.toJSONString(tasks));
                                        // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                        tasks.add(j+1, TaskObj.getTaskX(contrastTaskOneNew.getTePFinish()
                                                ,(contrastTaskOneNew.getTePFinish()+task.getTeDurTotal())
                                                ,task.getTeDurTotal(),task));
                                        System.out.println(JSON.toJSONString(tasks));
                                        tePFinish = (contrastTaskOneNew.getTePFinish()+task.getTeDurTotal());
                                        endTime = tasks.get(0).getTePStart();
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                ,teDate,task.getTeDurTotal());
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp),prodState,storageTaskWhereTime);
                                        setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                , task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                        // 任务余剩时间累减
                                        zon -= task.getTeDurTotal();
                                        result.put("zon",zon);
                                        // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                        result.put("taskIsProcessedComplete",2);
                                        result.put("hTeStart",(contrastTaskOneNew.getTePFinish()+task.getTeDurTotal()));
                                        storageTaskIsProcessedComplete = 1;
                                        // 更新当前任务的开始时间
                                        task.setTePStart(contrastTaskOneNew.getTePFinish());
                                        // 更新当前任务的结束时间
                                        task.setTePFinish((contrastTaskOneNew.getTePFinish()+task.getTeDurTotal()));
                                        System.out.println("进入这里--=10");
                                        conflictHandlePattern = conflictHandlePatternCopy;
                                        break;
                                    } else {
                                        // 获取时间差2（当前任务总时间-余剩总时间）
                                        long timeDifferenceNew = task.getTeDurTotal() - remainingTime;
                                        System.out.println(JSON.toJSONString(tasks));
                                        // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                        tasks.add(j+1, TaskObj.getTaskX(contrastTaskOneNew.getTePFinish()
                                                ,(contrastTaskOneNew.getTePFinish()+remainingTime),remainingTime,task));
                                        System.out.println(JSON.toJSONString(tasks));
                                        tePFinish = (contrastTaskOneNew.getTePFinish()+remainingTime);
                                        endTime = tasks.get(0).getTePStart();
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,remainingTime);
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp),prodState,storageTaskWhereTime);
                                        setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                , remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                                        // 更新当前任务的总时间
                                        task.setTeDurTotal(timeDifferenceNew);
                                        // 更新当前任务的开始时间
                                        task.setTePStart(contrastTaskOne.getTePStart());
                                        // 更新当前任务的结束时间
                                        task.setTePFinish(contrastTaskOne.getTePFinish());
                                        System.out.println("进入这里++=6");
                                        // 任务余剩时间累减
                                        zon -= remainingTime;
                                        conflictHandlePattern = 7;
                                    }
                                }
                            }
                        } else {
                            // 判断当前任务优先级小于对比任务3的优先级
                            if (task.getPriority() < contrastTaskTwoNew.getPriority()) {
//                                System.out.println("进入这里X-X-1");
                                // 判断当前任务的开始时间小于对比任务3的开始时间，并且当前任务的结束时间小于对比任务3的结束时间
                                if (task.getTePStart() < contrastTaskTwoNew.getTePStart()
                                        && task.getTePFinish() < contrastTaskTwoNew.getTePFinish()) {
                                    System.out.println("进入这里X-X-1-1-q:");
                                    // 调用处理时间冲突方法复刻方法
                                    JSONObject handleTimeConflictEasyInfo = handleTimeConflictEasy(task, contrastTaskOneNew, contrastTaskTwoNew, zon, tasks, i, j, conflict, teSB
                                            , random, grpB, dep, teDate,isGetTaskPattern,1,sho,csSta,randomAll,xbAndSbAll
                                            ,objTaskAll,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,onlyRefState,allImageTeDate,isComprehensiveHandle);
//                                    System.out.println("进入这里X-X-1-1-h:");
//                                    System.out.println(JSON.toJSONString(handleTimeConflictEasyInfo));
                                    // 获取任务余剩时间
                                    zon = handleTimeConflictEasyInfo.getLong("zon");
                                    tePFinish = handleTimeConflictEasyInfo.getLong("tePFinish");
                                    endTime = handleTimeConflictEasyInfo.getLong("endTime");
                                    conflictHandlePattern = handleTimeConflictEasyInfo.getInteger("conflictHandlePattern");
                                    // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                    if (handleTimeConflictEasyInfo.getInteger("taskIsProcessedComplete") == 2) {
                                        storageTaskIsProcessedComplete = 1;
                                        result.put("taskIsProcessedComplete",2);
                                        break;
                                    }
                                } else {
                                    // 获取余剩总时间（对比任务3的开始时间-对比任务1的结束时间）
                                    remainingTime = contrastTaskTwoNew.getTePStart() - contrastTaskOneNew.getTePFinish();
                                    // 判断余剩总时间大于0
                                    if (remainingTime > 0) {
                                        // 获取时间差（余剩总时间-当前任务总时间）
                                        long timeDifference = remainingTime - task.getTeDurTotal();
                                        // 判断时间差大于等于0
                                        if (timeDifference >= 0) {
                                            System.out.println(JSON.toJSONString(tasks));
                                            // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                            tasks.add(j + 1, TaskObj.getTaskX(task.getTePStart()
                                                    ,(task.getTePStart() + task.getTeDurTotal())
                                                    ,task.getTeDurTotal(), task));
                                            System.out.println(JSON.toJSONString(tasks));
                                            tePFinish = (task.getTePStart() + task.getTeDurTotal());
                                            endTime = tasks.get(0).getTePStart();
                                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    ,teDate,task.getTeDurTotal());
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp),prodState,storageTaskWhereTime);
                                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    , task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                            // 任务余剩时间累减
                                            zon -= task.getTeDurTotal();
                                            result.put("zon", zon);
                                            // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                            result.put("taskIsProcessedComplete", 2);
                                            result.put("hTeStart", (task.getTePStart() + task.getTeDurTotal()));
                                            System.out.println("进入这里再出-1");
                                            storageTaskIsProcessedComplete = 1;
                                            conflictHandlePattern = conflictHandlePatternCopy;
                                            break;
                                        } else {
                                            // 获取时间差2（当前任务总时间-余剩总时间）
                                            long timeDifferenceNew = task.getTeDurTotal() - remainingTime;
                                            System.out.println(JSON.toJSONString(tasks));
                                            // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                            tasks.add(j + 1, TaskObj.getTaskX(task.getTePFinish()
                                                    , (task.getTePFinish() + remainingTime), remainingTime, task));
                                            System.out.println(JSON.toJSONString(tasks));
                                            tePFinish = (task.getTePFinish() + remainingTime);
                                            endTime = tasks.get(0).getTePStart();
                                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,remainingTime);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp),prodState,storageTaskWhereTime);
                                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    , remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                                            // 更新当前任务的总时间
                                            task.setTeDurTotal(timeDifferenceNew);
                                            // 更新当前任务的开始时间
                                            task.setTePStart((task.getTePFinish() + remainingTime));
                                            // 更新当前任务的结束时间
                                            task.setTePFinish((task.getTePFinish() + remainingTime) + task.getTeDurTotal());
                                            System.out.println("进入这里++=7");
                                            // 任务余剩时间累减
                                            zon -= remainingTime;
                                            conflictHandlePattern = 7;
                                        }
                                    } else {
                                        System.out.println("进入新的啊-q:");
                                        // 调用处理时间冲突方法复刻方法
                                        JSONObject handleTimeConflictEasyInfo = handleTimeConflictEasy(task, contrastTaskOneNew
                                                , contrastTaskTwoNew, zon, tasks, i, j, conflict, teSB, random, grpB, dep, teDate
                                                ,isGetTaskPattern,1,sho,csSta,randomAll,xbAndSbAll
                                                ,objTaskAll,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp,onlyRefState,allImageTeDate,isComprehensiveHandle);
                                        System.out.println("进入新的啊-h:");
//                                        System.out.println(JSON.toJSONString(handleTimeConflictEasyInfo));
                                        tePFinish = handleTimeConflictEasyInfo.getLong("tePFinish");
                                        endTime = tasks.get(0).getTePStart();
                                        // 获取任务余剩时间
                                        zon = handleTimeConflictEasyInfo.getLong("zon");
                                        conflictHandlePattern = handleTimeConflictEasyInfo.getInteger("conflictHandlePattern");
                                        // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                        if (handleTimeConflictEasyInfo.getInteger("taskIsProcessedComplete") == 2) {
                                            storageTaskIsProcessedComplete = 1;
                                            result.put("taskIsProcessedComplete",2);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                // 判断当前任务的开始时间大于等于对比任务1的开始时间，并且当前任务的开始时间小于对比任务1的结束时间
                                if (task.getTePStart() >= contrastTaskOneNew.getTePStart()
                                        && task.getTePStart() < contrastTaskOneNew.getTePFinish()) {
                                    System.out.println("进入这里-X-1-q:"+taskIndexAccumulation);
                                    // 调用处理时间冲突方法复刻方法
                                    JSONObject handleTimeConflictEasyInfo = handleTimeConflictEasy(task, contrastTaskOneNew
                                            , contrastTaskTwoNew, zon, tasks, i, j, conflict, teSB, random, grpB, dep, teDate
                                            ,isGetTaskPattern,0,sho,csSta,randomAll,xbAndSbAll
                                            ,objTaskAll,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,onlyRefState,allImageTeDate,isComprehensiveHandle);
                                    System.out.println("进入这里-X-1-h:");
                                    tePFinish = handleTimeConflictEasyInfo.getLong("tePFinish");
                                    endTime = handleTimeConflictEasyInfo.getLong("endTime");
                                    // 获取任务余剩时间
                                    zon = handleTimeConflictEasyInfo.getLong("zon");
                                    conflictHandlePattern = handleTimeConflictEasyInfo.getInteger("conflictHandlePattern");
                                    // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                    if (handleTimeConflictEasyInfo.getInteger("taskIsProcessedComplete") == 2) {
                                        storageTaskIsProcessedComplete = 1;
                                        break;
                                    }
                                } else if (contrastTaskOneNew.getTePStart() >= task.getTePStart()
                                        && contrastTaskOneNew.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入这里-X-2-q:");
                                    // 调用处理时间冲突方法复刻方法
                                    JSONObject handleTimeConflictEasyInfo = handleTimeConflictEasy(task, contrastTaskOneNew
                                            , contrastTaskTwoNew, zon, tasks, i, j, conflict, teSB, random, grpB, dep, teDate
                                            ,isGetTaskPattern,0,sho,csSta,randomAll,xbAndSbAll
                                            ,objTaskAll,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,onlyRefState,allImageTeDate,isComprehensiveHandle);
                                    tePFinish = handleTimeConflictEasyInfo.getLong("tePFinish");
                                    endTime = handleTimeConflictEasyInfo.getLong("endTime");
                                    // 获取任务余剩时间
                                    zon = handleTimeConflictEasyInfo.getLong("zon");
                                    System.out.println("进入这里-X-2-h:");
//                                    System.out.println(JSON.toJSONString(handleTimeConflictEasyInfo));
                                    conflictHandlePattern = handleTimeConflictEasyInfo.getInteger("conflictHandlePattern");
                                    // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                    if (handleTimeConflictEasyInfo.getInteger("taskIsProcessedComplete") == 2) {
                                        storageTaskIsProcessedComplete = 1;
                                        result.put("taskIsProcessedComplete",2);
                                        break;
                                    }
                                } else if (task.getTePFinish() > contrastTaskOneNew.getTePStart()
                                        && task.getTePFinish() < contrastTaskOneNew.getTePFinish()) {
                                    System.out.println("进入这里-X-3-q:");
                                    // 调用处理时间冲突方法复刻方法
                                    JSONObject handleTimeConflictEasyInfo = handleTimeConflictEasy(task, contrastTaskOneNew
                                            , contrastTaskTwoNew, zon, tasks, i, j, conflict, teSB, random, grpB, dep, teDate
                                            ,isGetTaskPattern,0,sho,csSta,randomAll,xbAndSbAll
                                            ,objTaskAll,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,onlyRefState,allImageTeDate,isComprehensiveHandle);
                                    tePFinish = handleTimeConflictEasyInfo.getLong("tePFinish");
                                    endTime = handleTimeConflictEasyInfo.getLong("endTime");
                                    // 获取任务余剩时间
                                    zon = handleTimeConflictEasyInfo.getLong("zon");
                                    System.out.println("进入这里-X-3-h:");
                                    conflictHandlePattern = handleTimeConflictEasyInfo.getInteger("conflictHandlePattern");
                                    // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                    if (handleTimeConflictEasyInfo.getInteger("taskIsProcessedComplete") == 2) {
                                        storageTaskIsProcessedComplete = 1;
                                        result.put("taskIsProcessedComplete",2);
                                        break;
                                    }
                                } else {
                                    // 获取余剩总时间（对比任务3的开始时间-对比任务1的结束时间）
                                    remainingTime = contrastTaskTwoNew.getTePStart() - contrastTaskOneNew.getTePFinish();
                                    // 判断余剩总时间大于0
                                    if (remainingTime > 0) {
                                        // 获取时间差（余剩总时间-当前任务总时间）
                                        long timeDifference = remainingTime - task.getTeDurTotal();
                                        // 判断时间差大于等于0
                                        if (timeDifference >= 0) {
                                            System.out.println(JSON.toJSONString(tasks));
                                            // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                            tasks.add(j + 1, TaskObj.getTaskX(contrastTaskOneNew.getTePFinish()
                                                    , (contrastTaskOneNew.getTePFinish() + task.getTeDurTotal())
                                                    , task.getTeDurTotal(), task));
                                            System.out.println(JSON.toJSONString(tasks));
                                            tePFinish = (contrastTaskOneNew.getTePFinish() + task.getTeDurTotal());
                                            endTime = tasks.get(0).getTePStart();
                                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    ,teDate,task.getTeDurTotal());
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp),prodState,storageTaskWhereTime);
                                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    , task.getTeDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                            // 任务余剩时间累减
                                            zon -= task.getTeDurTotal();
                                            task.setTeDurTotal(0L);
                                            result.put("zon", zon);
                                            // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                            result.put("taskIsProcessedComplete", 2);
                                            result.put("hTeStart",tePFinish);
                                            System.out.println("进入这里再出-1-2");
                                            storageTaskIsProcessedComplete = 1;
                                            conflictHandlePattern = conflictHandlePatternCopy;
                                            break;
                                        } else {
                                            // 获取时间差2（当前任务总时间-余剩总时间）
                                            long timeDifferenceNew = task.getTeDurTotal() - remainingTime;
                                            System.out.println(JSON.toJSONString(tasks));
                                            // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                            tasks.add(j + 1, TaskObj.getTaskX(contrastTaskOneNew.getTePFinish()
                                                    , (contrastTaskOneNew.getTePFinish() + remainingTime), remainingTime, task));
                                            System.out.println(JSON.toJSONString(tasks));
                                            tePFinish = (contrastTaskOneNew.getTePFinish() + remainingTime);
                                            endTime = tasks.get(0).getTePStart();
                                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),teDate,remainingTime);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp),prodState,storageTaskWhereTime);
                                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    , remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                                            // 更新当前任务的总时间
                                            task.setTeDurTotal(timeDifferenceNew);
                                            // 更新当前任务的开始时间
                                            task.setTePStart((contrastTaskOneNew.getTePFinish() + remainingTime));
                                            // 更新当前任务的结束时间
                                            task.setTePFinish((contrastTaskOneNew.getTePFinish() + remainingTime) + task.getTeDurTotal());
                                            // 任务余剩时间累减
                                            zon -= remainingTime;
                                            System.out.println("进入这里再出-2-2");
                                            conflictHandlePattern = 7;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("处理时间冲突核心方法-3:"+conflictHandlePattern);
        result.put("tePFinish",tePFinish);
        result.put("endTime",endTime);
        result.put("zon",zon);
        if (conflictHandlePattern >= 2) {
            if (conflictHandlePattern == 5) {
                System.out.println("赋值的--3");
                result.put("hTeStart",tePFinish);
                result.put("teSB",teSB);
                result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                result.put("taskIsProcessedComplete",0);
            } else if (conflictHandlePattern == 6) {
                // taskIsProcessedComplete：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                result.put("taskIsProcessedComplete",2);
                result.put("zon",zon);
                result.put("hTeStart",tePFinish);
                result.put("teSB",teSB);
                storageTaskIsProcessedComplete = 1;
                result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                result.put("tePFinish",tePFinish);
                System.out.println("赋值的--2-new");
                result.put("conflictHandlePattern",conflictHandlePattern);
                return result;
            } else if (conflictHandlePattern == 7) {
                System.out.println("赋值的--7:"+conflictHandlePattern);
                result.put("hTeStart",tePFinish);
                result.put("teSB",teSB);
                result.put("storageTaskIsProcessedComplete",0);
                result.put("taskIsProcessedComplete",0);
            } else if (conflictHandlePattern == 8) {
                System.out.println("赋值的--8:"+conflictHandlePattern);
                result.put("zon",zon);
                result.put("hTeStart",tePFinish);
                result.put("teSB",teSB);
                result.put("taskIsProcessedComplete",2);
                storageTaskIsProcessedComplete = 1;
                result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                result.put("tePFinish",tePFinish);
                result.put("endTime",endTime);
                result.put("conflictHandlePattern",conflictHandlePattern);
                return result;
            } else {
                storageTaskIsProcessedComplete = 1;
                System.out.println("赋值的--4:"+conflictHandlePattern);
                result.put("hTeStart",tePFinish);
                result.put("teSB",teSB);
                result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
                result.put("taskIsProcessedComplete",2);
            }
            result.put("conflictHandlePattern",conflictHandlePattern);
            return result;
        }
        result.put("teSB",teSB);
        result.put("storageTaskIsProcessedComplete",storageTaskIsProcessedComplete);
        result.put("conflictHandlePattern",conflictHandlePattern);
        return result;
    }
}
