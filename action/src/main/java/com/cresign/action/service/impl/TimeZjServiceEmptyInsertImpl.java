package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.TimeZjServiceEmptyInsert;
import com.cresign.action.utils.GsThisInfo;
import com.cresign.action.utils.TaskObj;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.chkin.Task;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Description 时间处理空插和分割代码类(空插处理方法、空插和空插冲突处理方法、时间处理删除方法、计算空插时间方法)
 * @ClassName TimeZjServiceEmptyInsertImpl
 * @Author tang
 * @Date 2022/10/11
 * @Version 1.0.0
 */
@Service
public class TimeZjServiceEmptyInsertImpl extends TimeZj implements TimeZjServiceEmptyInsert {

    @Resource
    private RetResult retResult;

    /**
     * 空插处理方法
     * @param tasks	任务集合
     * @param conflictInd	冲突任务下标
     * @param conflict	冲突任务集合
     * @param currentHandleTask	当前处理任务
     * @param zon	任务余剩时间
     * @param grpB	组别
     * @param dep	部门
     * @param random	当前唯一编号
     * @param isTimeStopState	isTimeStopState == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
     * @param teS	当前时间戳
     * @param emptyInsertHandlePattern	emptyInsertHandlePattern == 0 正常第一次调用空插处理方法、is == 1 空插处理方法调用空插处理方法
     * @param teDate	当前处理任务所在时间对象
     * @param oDates	当前任务基础信息集合
     * @param dgInfo	当前任务基础信息
     * @param taskTimeKeyFirstVal	任务所在时间键的第一个键的值（时间戳）
     * @param timeConflictCopy	当前任务所在日期
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param isProblemState	存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
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
     * @param id_OAndIndexTaskInfo 被清理的任务信息
     * @param isInside 是否写入数据库
     * @param isSetTasks 是否写入当前任务
     * @return int  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @Override
    @SuppressWarnings("unchecked")
    public int emptyInsertHandle(List<Task> tasks, int conflictInd, List<Task> conflict, Task currentHandleTask, Long zon
            , String grpB, String dep, String random, int isTimeStopState, Long teS, int emptyInsertHandlePattern
            , JSONObject teDate, JSONArray oDates, JSONObject dgInfo, Long taskTimeKeyFirstVal
            , JSONObject timeConflictCopy, Integer isGetTaskPattern, JSONObject sho
            , int isProblemState, int csSta, String randomAll, JSONObject xbAndSbAll, JSONObject actionIdO
            , JSONObject objTaskAll, JSONObject recordId_OIndexState, JSONObject storageTaskWhereTime
            , JSONObject allImageTotalTime, Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks
            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState
            , JSONObject recordNoOperation,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate
            ,JSONObject id_OAndIndexTaskInfo,boolean isInside,boolean isSetTasks,JSONObject depAllTime) {
        // isTimeStopState == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
        if (isTimeStopState == 1) {
            if (isInside) {
                System.out.println("跳天写入数据:"+teS+" - "+grpB+" - "+dep+" - "+zon+" - ");
                setTasksAndZon(tasks,grpB,dep,teS,zon,objTaskAll);
            }
            // 当前时间戳加一天
            teS += 86400L;
            // 任务所在时间键的第一个键的值（时间戳）加一天
            taskTimeKeyFirstVal += 86400L;
            System.out.println("--进入空插冲突跳天--");
            System.out.println(JSON.toJSONString(currentHandleTask));
            // 调用获取任务综合信息方法
            Map<String, Object> jumpDay = getJumpDay(random, grpB, dep,1,teS,isGetTaskPattern,currentHandleTask.getId_C()
                    ,xbAndSbAll,objTaskAll,allImageTotalTime,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,depAllTime);
            // 获取任务集合
            List<Task> tasksInside = (List<Task>) jumpDay.get("tasks");
            // 获取任务余剩时间
            long zonInside = (long) jumpDay.get("zon");
            // isTimeStopStateInside == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
            int isTimeStopStateInside = 0;
            // 存储问题状态参数: isProblemStateCurrent = 0 正常、isPd2 = 1 订单编号为空、isPd2 = 2 主生产部件
            int isProblemStateCurrent = 0;
            // 定义存储最后结束时间
            long tePFinish;
            // 定义存储当天时间
            long endTime;
            // 定义存储是否写入任务列表
            boolean isSetTasksNew = true;
            // 遍历任务集合
            for (int i = 0; i < tasksInside.size(); i++) {
                // 判断冲突任务下标大于等于冲突任务集合长度
                if (conflictInd >= conflict.size()) {
                    // 结束循环
                    break;
                }
                // 获取对比任务1 contrastTaskOne
                Task contrastTaskOne = tasksInside.get(i);
                // 判断任务下标加1小于任务集合长度
                if ((i + 1) < tasksInside.size()) {
                    // 获取对比任务2
                    Task contrastTaskTwo = tasksInside.get(i+1);
//                    System.out.println("task1X-2X:");
//                    System.out.println(JSON.toJSONString(task1X));
//                    System.out.println(JSON.toJSONString(task2X));
                    // 调用空插冲突处理方法
                    JSONObject emptyInsertAndEmptyInsertConflictHandleInfo = emptyInsertAndEmptyInsertConflictHandle(currentHandleTask, contrastTaskOne, contrastTaskTwo, tasksInside, i, conflictInd, zonInside, conflict
                            ,teDate,random,dep,grpB,oDates,dgInfo,taskTimeKeyFirstVal,timeConflictCopy,isGetTaskPattern,sho,csSta,randomAll,xbAndSbAll
                            ,actionIdO,objTaskAll,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                            ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                            ,thisInfo,allImageTeDate,id_OAndIndexTaskInfo,true,depAllTime);
                    JSONObject currentHandleTaskJSON = emptyInsertAndEmptyInsertConflictHandleInfo.getJSONObject("currentHandleTask");
                    if (null != currentHandleTaskJSON) {
                        currentHandleTask = JSONObject.parseObject(JSON.toJSONString(currentHandleTaskJSON),Task.class);
                    }
                    // 获取任务余剩时间
                    zonInside = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("zon");
                    // 更新冲突任务集合下标为冲突下标的冲突任务信息
                    conflict.set(conflictInd, TaskObj.getTaskX(currentHandleTask.getTePStart(),currentHandleTask.getTePFinish(),currentHandleTask.getWntDurTotal(),currentHandleTask));
                    // 获取冲突任务下标
                    conflictInd = emptyInsertAndEmptyInsertConflictHandleInfo.getInteger("conflictInd");
                    // 获取任务所在时间键的第一个键的值（时间戳）
                    taskTimeKeyFirstVal = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("taskTimeKeyFirstVal");
                    tePFinish = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("tePFinish");
                    endTime = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("endTime");
                    isSetTasksNew = emptyInsertAndEmptyInsertConflictHandleInfo.getBoolean("isSetTasks");
//                    System.out.println("出来后赋值-conflictInd-kc:"+conflictInd);
                    // 判断冲突任务下标小于冲突任务集合的长度
                    if (conflictInd < conflict.size()) {
                        // 获取当前任务订单编号
                        String id_O = currentHandleTask.getId_O();
                        // 获取当前任务订单下标
                        int index = currentHandleTask.getIndex();
                        // 根据冲突任务下标获取冲突任务对象
                        Task conflictTask = conflict.get(conflictInd);
                        // 深度复制冲突任务对象
                        Task conflictTaskCopy = TaskObj.getTaskX(conflictTask.getTePStart(),conflictTask.getTePFinish()
                                ,conflictTask.getWntDurTotal(),conflictTask);
                        // 判断当前任务订单编号不等于冲突任务订单编号，或者当前任务订单下标不等于冲突任务订单下标
                        if (!id_O.equals(conflictTaskCopy.getId_O()) || index != conflictTaskCopy.getIndex()) {
                            // 根据当前任务订单编号获取任务所在日期对象
                            JSONObject taskTime = timeConflictCopy.getJSONObject(currentHandleTask.getId_O());
                            // 根据当前任务订单下标添加任务所在日期
                            taskTime.put(currentHandleTask.getDateIndex()+"",teDate);
                            // 根据当前任务订单编号添加任务所在日期对象
                            timeConflictCopy.put(currentHandleTask.getId_O(),taskTime);
                            // 获取清理状态方法
                            int clearStatusThis = getClearStatus(currentHandleTask.getId_O()
                                    , (currentHandleTask.getDateIndex()+1), clearStatus);
                            // 判断状态为0
                            if (clearStatusThis == 0) {
                                // 写入清理状态方法
                                GsThisInfo.setClearStatus(currentHandleTask.getId_O()
                                        , (currentHandleTask.getDateIndex()+1),clearStatus,1);
                                System.out.println("进入清理-3");
                                int nextDateIndex = (currentHandleTask.getDateIndex() + 1);
                                if (nextDateIndex != oDates.size()) {
                                    // 获取零件信息
                                    JSONObject nextInfo = oDates.getJSONObject(nextDateIndex);
                                    System.out.println("timeComprehensiveHandle - 4 :清理状态:"+clearStatusThis);
                                    // 调用获取冲突处理方法，原方法
                                    JSONObject timeComprehensiveHandleInfo =
                                            timeZjServiceComprehensive.timeComprehensiveHandle(nextInfo.getString("id_O")
                                                    , nextInfo.getInteger("index"),nextDateIndex, timeConflictCopy, isGetTaskPattern
                                                    , 1, sho,currentHandleTask.getId_C(),csSta,randomAll,xbAndSbAll
                                                    ,actionIdO,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                                                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                                    ,onlyRefState,recordNoOperation,tePFinish,id_OAndIndexTaskInfo
                                                    ,clearStatus,thisInfo,allImageTeDate,endTime,depAllTime,random,false
                                                    ,nextInfo.getString("id_PF"),nextInfo.getString("layer"));
                                    // 获取存储问题状态参数
                                    isProblemStateCurrent = timeComprehensiveHandleInfo.getInteger("isProblemState");
                                    // 创建任务所在日期对象
                                    teDate = new JSONObject();
                                    // 判断当前任务订单编号不等于冲突任务订单编号
                                    if (!id_O.equals(conflictTaskCopy.getId_O())) {
//                                            // 获取进度卡片的所有递归信息
//                                            objAction = actionIdO.getJSONArray(conflictTaskCopy.getId_O());
//                                            if (null == objAction) {
//                                                // 根据冲突任务订单编号获取订单信息 - t
////                                                Order order = coupaUtil.getOrderByListKey(conflictTaskCopy.getId_O(), Collections.singletonList("action"));
//                                                Order order = qt.getMDContent(conflictTaskCopy.getId_O(),"action", Order.class);
//                                                // 获取进度卡片的所有递归信息
//                                                objAction = order.getAction().getJSONArray("objAction");
//                                                actionIdO.put(conflictTaskCopy.getId_O(),objAction);
//                                            }
                                        String conflictTaskId_OP = sonGetOrderFatherId(conflictTaskCopy.getId_O(), conflictTaskCopy.getId_C(), thisInfo, actionIdO, new JSONObject());
                                        oDates = actionIdO.getJSONObject(conflictTaskId_OP).getJSONObject(conflictTaskCopy
                                                .getLayer()+"").getJSONObject(conflictTaskCopy.getId_PF()).getJSONArray("oDates");
                                    }
                                    // 根据冲突任务订单下标获取递归信息
                                    dgInfo = oDates.getJSONObject(conflictTaskCopy.getDateIndex());
                                    // 获取递归信息的任务所在日期对象
                                    JSONObject teDateDg = dgInfo.getJSONObject("teDate");
                                    // 获取任务所在日期的所有键
                                    Set<String> teDateDgKey = teDateDg.keySet();
                                    // 将设置日期键转换成集合类型
                                    List<String> teDateDgKeySon = new ArrayList<>(teDateDgKey);
                                    // 获取任务所在时间键的第一个键的值（时间戳）
                                    taskTimeKeyFirstVal = Long.parseLong(teDateDgKeySon.get(0));
                                    // 调用写入当前时间戳方法
                                    setTeS(random , dgInfo.getString("grpB"), dgInfo.getString("dep"),taskTimeKeyFirstVal,newestLastCurrentTimestamp);
                                    // 根据冲突任务订单编号添加任务所在日期
                                    timeConflictCopy.put(conflictTaskCopy.getId_O()
                                            ,qt.setJson(conflictTaskCopy.getDateIndex()+"",teDate));
                                }
//                                // 获取下一个零件信息
//                                JSONArray prtNext = dgInfo.getJSONArray("prtNext");
//                                // 判断下一个不为空
//                                if (null != prtNext && prtNext.size() > 0) {
//                                    // 遍历下一个信息
//                                    for (int next = 0; next < prtNext.size(); next++) {
//                                        // 获取零件信息
//                                        JSONObject nextInfo = prtNext.getJSONObject(next);
//                                        System.out.println("timeComprehensiveHandle - 4 :清理状态:"+clearStatusThis);
//                                        // 调用获取冲突处理方法，原方法
//                                        JSONObject timeComprehensiveHandleInfo =
//                                                timeZjServiceComprehensive.timeComprehensiveHandle(nextInfo.getString("id_O")
//                                                        , nextInfo.getInteger("index"), timeConflictCopy, isGetTaskPattern
//                                                        , 1, sho,currentHandleTask.getId_C(),csSta,randomAll,xbAndSbAll
//                                                        ,actionIdO,objTaskAll,recordId_OIndexState,storageTaskWhereTime
//                                                        ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
//                                                        ,onlyRefState,recordNoOperation,tePFinish,id_OAndIndexTaskInfo
//                                                        ,clearStatus,thisInfo,allImageTeDate,endTime,depAllTime,random,false);
//                                        // 获取存储问题状态参数
//                                        isProblemStateCurrent = timeComprehensiveHandleInfo.getInteger("isProblemState");
//                                        // 创建任务所在日期对象
//                                        teDate = new JSONObject();
//                                        // 判断当前任务订单编号不等于冲突任务订单编号
//                                        if (!id_O.equals(conflictTaskCopy.getId_O())) {
////                                            // 获取进度卡片的所有递归信息
////                                            objAction = actionIdO.getJSONArray(conflictTaskCopy.getId_O());
////                                            if (null == objAction) {
////                                                // 根据冲突任务订单编号获取订单信息 - t
//////                                                Order order = coupaUtil.getOrderByListKey(conflictTaskCopy.getId_O(), Collections.singletonList("action"));
////                                                Order order = qt.getMDContent(conflictTaskCopy.getId_O(),"action", Order.class);
////                                                // 获取进度卡片的所有递归信息
////                                                objAction = order.getAction().getJSONArray("objAction");
////                                                actionIdO.put(conflictTaskCopy.getId_O(),objAction);
////                                            }
//                                            String conflictTaskId_OP = sonGetOrderFatherId(conflictTaskCopy.getId_O(), conflictTaskCopy.getId_C(), thisInfo, actionIdO, new JSONObject());
//                                            oDates = actionIdO.getJSONObject(conflictTaskId_OP).getJSONArray("oDates");
//                                        }
//                                        // 根据冲突任务订单下标获取递归信息
//                                        dgInfo = oDates.getJSONObject(conflictTaskCopy.getDateIndex());
//                                        // 获取递归信息的任务所在日期对象
//                                        JSONObject teDateDg = dgInfo.getJSONObject("teDate");
//                                        // 获取任务所在日期的所有键
//                                        Set<String> teDateDgKey = teDateDg.keySet();
//                                        // 将设置日期键转换成集合类型
//                                        List<String> teDateDgKeySon = new ArrayList<>(teDateDgKey);
//                                        // 获取任务所在时间键的第一个键的值（时间戳）
//                                        taskTimeKeyFirstVal = Long.parseLong(teDateDgKeySon.get(0));
//                                        // 调用写入当前时间戳方法
//                                        setTeS(random , dgInfo.getString("grpB"), dgInfo.getString("dep"),taskTimeKeyFirstVal,newestLastCurrentTimestamp);
//                                        // 创建任务所在日期存储对象
//                                        JSONObject teDaSon = new JSONObject();
//                                        // 根据冲突任务订单下标添加任务所在日期
//                                        teDaSon.put(conflictTaskCopy.getIndex()+"",teDate);
//                                        // 根据冲突任务订单编号添加任务所在日期
//                                        timeConflictCopy.put(conflictTaskCopy.getId_O(),teDaSon);
//                                    }
//                                }
                            }
                        }
                        // 根据冲突任务下标获取冲突任务信息
                        currentHandleTask = conflict.get(conflictInd);
//                        System.out.println("换taskX:"+zon2);
                    } else {
//                        System.out.println("tasks2-kc-2:");
//                        System.out.println(JSON.toJSONString(tasks2));
                        // 根据当前任务订单编号获取任务所在日期对象
                        JSONObject taskTime = timeConflictCopy.getJSONObject(currentHandleTask.getId_O());
                        if (null == taskTime) {
                            taskTime = new JSONObject();
                        }
                        // 根据当前任务订单下标添加任务所在日期
                        taskTime.put(currentHandleTask.getDateIndex()+"",teDate);
                        // 根据当前任务订单编号添加任务所在日期对象
                        timeConflictCopy.put(currentHandleTask.getId_O(),taskTime);
                        // isGetTaskPattern = 0 获取数据库任务信息、 = 1 获取镜像任务信息
                        if (isGetTaskPattern == 1 && isSetTasksNew) {
                            System.out.println("这里镜像-1");
                            // 调用写入镜像任务集合方法
                            setImageTasks(tasksInside,grpB,dep,teS,allImageTasks);
                            // 调用写入镜像任务余剩时间
                            setImageZon(zonInside,grpB,dep,teS,allImageTotalTime);
                        }
                        // 获取清理状态方法
                        int clearStatusThis = getClearStatus(currentHandleTask.getId_O()
                                , (currentHandleTask.getDateIndex()+1), clearStatus);
                        System.out.println("进入清理-4 | timeComprehensiveHandle - 5:清理状态:"+clearStatusThis);
                        // 判断状态为0
                        if (clearStatusThis == 0) {
                            // 写入清理状态方法
                            GsThisInfo.setClearStatus(currentHandleTask.getId_O()
                                    , (currentHandleTask.getDateIndex()+1),clearStatus,1);
                            int nextDateIndex = (currentHandleTask.getDateIndex() + 1);
                            if (nextDateIndex != oDates.size()) {
                                // 获取零件信息
                                JSONObject nextInfo = oDates.getJSONObject(nextDateIndex);
//                                    System.out.println("timeComprehensiveHandle - 5 :");
                                // 调用获取冲突处理方法，原方法
                                JSONObject timeComprehensiveHandleInfo =
                                        timeZjServiceComprehensive.timeComprehensiveHandle(nextInfo.getString("id_O")
                                                , nextInfo.getInteger("index"),nextDateIndex, timeConflictCopy, isGetTaskPattern
                                                , 1, sho,currentHandleTask.getId_C(),csSta,randomAll,xbAndSbAll
                                                ,actionIdO,objTaskAll,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                                ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState
                                                ,recordNoOperation, tePFinish,id_OAndIndexTaskInfo,clearStatus,thisInfo
                                                ,allImageTeDate,endTime,depAllTime,random,false
                                                ,nextInfo.getString("id_PF"),nextInfo.getString("layer"));
                                // 获取存储问题状态参数
                                isProblemStateCurrent = timeComprehensiveHandleInfo.getInteger("isProblemState");
                            }
//                            // 获取下一个零件信息
//                            JSONArray prtNext = dgInfo.getJSONArray("prtNext");
////                            System.out.println("进入清理-4");
//                            // 判断不为空
//                            if (null != prtNext && prtNext.size() > 0) {
//                                // 遍历下一个信息
//                                for (int next = 0; next < prtNext.size(); next++) {
//                                    // 获取零件信息
//                                    JSONObject nextInfo = prtNext.getJSONObject(next);
////                                    System.out.println("timeComprehensiveHandle - 5 :");
//                                    // 调用获取冲突处理方法，原方法
//                                    JSONObject timeComprehensiveHandleInfo =
//                                            timeZjServiceComprehensive.timeComprehensiveHandle(nextInfo.getString("id_O")
//                                                    , nextInfo.getInteger("index"), timeConflictCopy, isGetTaskPattern
//                                                    , 1, sho,currentHandleTask.getId_C(),csSta,randomAll,xbAndSbAll
//                                                    ,actionIdO,objTaskAll,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
//                                                    ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState
//                                                    ,recordNoOperation, tePFinish,id_OAndIndexTaskInfo,clearStatus,thisInfo
//                                                    ,allImageTeDate,endTime,depAllTime,random,false);
//                                    // 获取存储问题状态参数
//                                    isProblemStateCurrent = timeComprehensiveHandleInfo.getInteger("isProblemState");
//                                }
//                            }
                        }
                    }
//                    System.out.println("taskX-kc-2:");
//                    System.out.println(JSON.toJSONString(taskX));
                    // isTimeStopStateInside == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
                    isTimeStopStateInside = emptyInsertAndEmptyInsertConflictHandleInfo.getInteger("isTimeStopState");
                    // 空插冲突强制停止参数累加
                    leiW.put(randomAll,(leiW.getInteger(randomAll)+1));
                    // 判断空插冲突强制停止参数等于60
                    if (leiW.getInteger(randomAll) == 560) {
                        System.out.println("----进入强制停止空差冲突方法-2----");
                        // 赋值强制停止出现后的记录参数等于1
                        isQzTz.put(randomAll,1);
                        break;
                    }
                }
            }
            // 判断空插冲突强制停止参数小于61
            if (leiW.getInteger(randomAll) < 561) {
//                System.out.println("---这里问题---:"+isProblemStateCurrent);
                // 调用空插处理方法
                emptyInsertHandle(tasksInside,conflictInd,conflict,currentHandleTask,zonInside,grpB,dep,random,isTimeStopStateInside,teS,1,teDate
                        ,oDates,dgInfo,taskTimeKeyFirstVal,timeConflictCopy,isGetTaskPattern,sho,isProblemStateCurrent,csSta,randomAll
                        ,xbAndSbAll,actionIdO,objTaskAll
                        ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                        ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState
                        ,recordNoOperation,clearStatus,thisInfo,allImageTeDate,id_OAndIndexTaskInfo,true
                        ,isSetTasksNew,depAllTime);
                return isProblemStateCurrent;
            } else {
                System.out.println("----进入强制停止空差冲突方法-2-1----");
                // 赋值强制停止出现后的记录参数等于1
                isQzTz.put(randomAll,1);
            }
        }
        else {
            // emptyInsertHandlePattern == 0 正常第一次调用空插处理方法、is == 1 空插处理方法调用空插处理方法
            if (emptyInsertHandlePattern == 1) {
                // isGetTaskPattern = 0 获取数据库任务信息、 = 1 获取镜像任务信息
                if (isGetTaskPattern == 0) {
                    setTasksAndZon(tasks,grpB,dep,teS,zon,objTaskAll);
                } else {
//                    System.out.println("isSetTasks:"+isSetTasks);
                    // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                    if (isProblemState != 2 && isSetTasks) {
                        System.out.println("这里写入了镜像-2");
                        // 调用写入镜像任务集合方法
                        setImageTasks(tasks,grpB,dep,teS,allImageTasks);
                        // 调用写入镜像任务余剩时间方法
                        setImageZon(zon,grpB,dep,teS,allImageTotalTime);
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 空插和空插冲突处理方法
     * @param currentHandleTask	当前处理任务信息
     * @param contrastTaskOne	对比任务信息-1
     * @param contrastTaskTwo	对比任务信息-2
     * @param tasks	任务集合
     * @param i	任务下标
     * @param conflictInd	被冲突任务下标
     * @param zon	任务余剩时间
     * @param conflict	被冲突任务集合
     * @param teDate	当前处理的任务的所在日期对象
     * @param random	当前唯一编号
     * @param dep	部门
     * @param grpB	组别
     * @param oDates	当前任务基础信息集合
     * @param dgInfo	当前任务基础信息
     * @param taskTimeKeyFirstVal	任务所在时间键的第一个键的值（时间戳）
     * @param timeConflictCopy	当前任务所在日期对象
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
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
     * @param id_OAndIndexTaskInfo 被清理的任务信息
     * @param isSetTasks 是否写入当前任务
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @Override
    public JSONObject emptyInsertAndEmptyInsertConflictHandle(Task currentHandleTask, Task contrastTaskOne
            , Task contrastTaskTwo, List<Task> tasks
            , int i, int conflictInd, long zon, List<Task> conflict, JSONObject teDate, String random
            , String dep, String grpB, JSONArray oDates, JSONObject dgInfo, Long taskTimeKeyFirstVal
            , JSONObject timeConflictCopy, Integer isGetTaskPattern, JSONObject sho, int csSta
            , String randomAll, JSONObject xbAndSbAll, JSONObject actionIdO, JSONObject objTaskAll
            , JSONObject recordId_OIndexState, JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks, JSONObject onlyFirstTimeStamp
            , JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState, JSONObject recordNoOperation
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate
            ,JSONObject id_OAndIndexTaskInfo,boolean isSetTasks,JSONObject depAllTime) {
        // 创建返回结果
        JSONObject result = new JSONObject();
        // 添加正常时间够用停止状态
        result.put("isTimeStopState",0);
        // 定义存储最后结束时间
        long tePFinish = 0;
        // 定义存储当天时间
        long endTime = 0;
//        System.out.println("进入空插和空插冲突:");
//        System.out.println(JSON.toJSONString(currentHandleTask));
//        System.out.println(JSON.toJSONString(contrastTaskOne));
//        System.out.println(JSON.toJSONString(contrastTaskTwo));
        // 获取产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer prodState = sho.getJSONObject(currentHandleTask.getId_O()).getJSONObject(currentHandleTask.getIndex().toString()).getInteger("prodState");
        // currentOnlyNumberState 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
        int currentOnlyNumberState = onlyRefState.getInteger(random);
        // 判断对比任务1的优先级等于系统并且对比任务2的优先级不等于系统
        if (contrastTaskOne.getPriority() == -1 && contrastTaskTwo.getPriority() != -1) {
//            System.out.println("进入-1和!=-1:");
            // 获取开始时间（对比任务1的结束时间+当前任务的任务总时间）
            long startTime = contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal();
            // 判断开始时间大于对比任务2的结束时间
            if (startTime > contrastTaskTwo.getTePFinish()) {
                // 判断当前任务优先级小于对比任务2优先级
                if (currentHandleTask.getPriority() < contrastTaskTwo.getPriority()){
                    // 任务余剩时间累加（+对比任务2的总时间）
                    zon += contrastTaskTwo.getWntDurTotal();
                    // 任务余剩时间累减（-当前任务总时间）
                    zon -= currentHandleTask.getWntDurTotal();
                    // 更新任务集合对应的下标（i1+1）的任务信息为当前任务
                    tasks.set(i+1, TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal())
                            ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                    tePFinish = (contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal());
                    endTime = tasks.get(0).getTePStart();
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
                    System.out.println("进入这里++=33");
                    // 冲突任务集合添加对比任务2信息
                    conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getWntDurTotal(),contrastTaskTwo));
                    addThisConflictInfoStatus(1, currentHandleTask.getDateIndex(), thisInfo);
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
//                    addThisConflictLastODate(currentHandleTask.getId_O(), currentHandleTask.getId_C(), thisInfo,actionIdO);
                    // 调用添加或更新产品状态方法
                    addSho(sho, currentHandleTask.getId_O(),currentHandleTask.getIndex().toString()
                            ,contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(taskTimeKeyFirstVal,teDate, currentHandleTask.getWntDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal
                            ,sho.getJSONObject(currentHandleTask.getId_O()).getJSONObject(currentHandleTask.getIndex().toString()).getInteger("prodState")
                            ,storageTaskWhereTime);
                    // 调用更新镜像的所在时间方法
                    setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                            ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                    currentHandleTask.setWntDurTotal(0L);
                    result.put("currentHandleTask",currentHandleTask);
                }
//                System.out.println("进入前赋值-conflictInd-2-1:"+conflictInd);
                // 调用处理冲突核心方法2
                JSONObject emptyInsertAndEmptyInsertConflictHandleSonInfo = emptyInsertAndEmptyInsertConflictHandleSon(conflictInd, tasks, conflict, currentHandleTask, zon, teDate, random
                        , oDates, dgInfo, i,dep,grpB,taskTimeKeyFirstVal,timeConflictCopy,isGetTaskPattern,sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                        ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                        ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus,thisInfo
                        ,allImageTeDate,id_OAndIndexTaskInfo,depAllTime);
//                System.out.println("emptyInsertAndEmptyInsertConflictHandleSonInfo结束后输出:");
//                System.out.println(JSON.toJSONString(currentHandleTask));
//                System.out.println(JSON.toJSONString(emptyInsertAndEmptyInsertConflictHandleSonInfo.getJSONObject("currentHandleTask")));
                // 更新冲突集合指定的冲突下标的任务信息
                conflict.set(conflictInd, TaskObj.getTaskX(currentHandleTask.getTePStart(),currentHandleTask.getTePFinish(),currentHandleTask.getWntDurTotal(),currentHandleTask));
                // 获取冲突下标
                conflictInd = emptyInsertAndEmptyInsertConflictHandleSonInfo.getInteger("conflictInd");
                // 获取任务余剩时间
                zon = emptyInsertAndEmptyInsertConflictHandleSonInfo.getLong("zon");
                // 获取任务所在时间键的第一个键的值（时间戳）
                taskTimeKeyFirstVal = emptyInsertAndEmptyInsertConflictHandleSonInfo.getLong("taskTimeKeyFirstVal");
                isSetTasks = false;
                tePFinish = emptyInsertAndEmptyInsertConflictHandleSonInfo.getLong("tePFinish");
                endTime = emptyInsertAndEmptyInsertConflictHandleSonInfo.getLong("endTime");
                result.put("currentHandleTask",emptyInsertAndEmptyInsertConflictHandleSonInfo.getJSONObject("currentHandleTask"));
                if (null != emptyInsertAndEmptyInsertConflictHandleSonInfo.getBoolean("lastComplete")
                        && emptyInsertAndEmptyInsertConflictHandleSonInfo.getBoolean("lastComplete")) {
                    result.put("lastComplete",true);
                }
            } else
            {
                // 判断当前任务优先级小于对比任务2的优先级
                if (currentHandleTask.getPriority() < contrastTaskTwo.getPriority()) {
                    // 判断开始时间小于等于对比时间2的开始时间
                    if (startTime <= contrastTaskTwo.getTePStart()) {
                        // 任务集合添加任务下标加1（i1+1）添加任务信息
                        tasks.add((i+1), TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal())
                                ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                        tePFinish = (contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal());
                        endTime = tasks.get(0).getTePStart();
                        // 任务余剩时间累减
                        zon -= currentHandleTask.getWntDurTotal();
                        // 冲突任务下标累加
                        conflictInd++;
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(taskTimeKeyFirstVal,teDate, currentHandleTask.getWntDurTotal());
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal,prodState,storageTaskWhereTime);
                        setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                                ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                        System.out.println("进入空-1:");
                        currentHandleTask.setWntDurTotal(0L);
                        result.put("currentHandleTask",currentHandleTask);
                        GsThisInfo.setThisInfoTimeCount(thisInfo);
                    } else if (startTime > contrastTaskTwo.getTePStart() && startTime <= contrastTaskTwo.getTePFinish()) {
                        // 任务余剩时间累加（+对比任务2的总时间）
                        zon += contrastTaskTwo.getWntDurTotal();
                        // 任务余剩时间累减（-当前任务总时间）
                        zon -= currentHandleTask.getWntDurTotal();
                        // 更新任务集合对应的下标（i1+1）的任务信息为当前任务
                        tasks.set(i+1, TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal())
                                ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                        tePFinish = (contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal());
                        endTime = tasks.get(0).getTePStart();
                        // 冲突任务集合添加对比任务2信息
                        conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getWntDurTotal(),contrastTaskTwo));
                        addThisConflictInfoStatus(1, currentHandleTask.getDateIndex(), thisInfo);
                        GsThisInfo.setThisInfoTimeCount(thisInfo);
//                        addThisConflictLastODate(currentHandleTask.getId_O(), currentHandleTask.getId_C(), thisInfo,actionIdO);
                        // 调用添加或更新产品状态方法
                        addSho(sho, currentHandleTask.getId_O(),currentHandleTask.getIndex().toString(),contrastTaskTwo.getId_O(), contrastTaskTwo.getIndex().toString(),0);
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(taskTimeKeyFirstVal,teDate, currentHandleTask.getWntDurTotal());
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal
                                ,sho.getJSONObject(currentHandleTask.getId_O()).getJSONObject(currentHandleTask.getIndex()
                                        .toString()).getInteger("prodState"),storageTaskWhereTime);
                        setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                                ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                        System.out.println("进入空-2:");
                        currentHandleTask.setWntDurTotal(0L);
                        result.put("currentHandleTask",currentHandleTask);
                        GsThisInfo.setThisInfoTimeCount(thisInfo);
                    }
                }
            }
        }
        else if (contrastTaskOne.getPriority() == -1 && contrastTaskTwo.getPriority() == -1)
        {
            // 获取开始时间（对比任务1的结束时间+当前任务的任务总时间）
            long startTime = contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal();
            // 判断开始时间大于对比任务2的开始时间
            if (startTime > contrastTaskTwo.getTePStart()) {
                // 获取余剩时间（对比时间2的开始时间-对比时间1的结束时间）
                long remainingTime = contrastTaskTwo.getTePStart() - contrastTaskOne.getTePFinish();
                // 判断余剩时间大于0
                if (remainingTime > 0) {
                    // 获取时间差（当前任务总时间-余剩时间）
                    long timeDifference = currentHandleTask.getWntDurTotal() - remainingTime;
                    // 任务集合添加任务下标加1（i1+1）添加任务信息
                    tasks.add((i+1), TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+remainingTime),remainingTime,currentHandleTask));
                    tePFinish = (contrastTaskOne.getTePFinish()+remainingTime);
                    endTime = tasks.get(0).getTePStart();
                    // 任务余剩时间累减
                    zon -= remainingTime;
                    // 设置当前任务总共时间
                    currentHandleTask.setWntDurTotal(timeDifference);
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(taskTimeKeyFirstVal,teDate,remainingTime);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal,prodState,storageTaskWhereTime);
                    setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                            ,taskTimeKeyFirstVal, remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                    System.out.println("进入空-3:");
                    result.put("currentHandleTask",currentHandleTask);
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
                } else {
                    result.put("isTimeStopState",1);
//                    System.out.println("进入空-4-无需查看:");
                }
            } else {
                System.out.println(JSON.toJSONString(currentHandleTask));
                System.out.println(JSON.toJSONString(tasks));
                // 任务集合添加任务下标加1（i1+1）添加任务信息
                tasks.add((i+1), TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal())
                        ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                System.out.println(JSON.toJSONString(tasks));
                tePFinish = (contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal());
                endTime = tasks.get(0).getTePStart();
                // 任务余剩时间累减
                zon -= currentHandleTask.getWntDurTotal();
                // 冲突任务下标累加
                conflictInd++;
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(taskTimeKeyFirstVal,teDate,currentHandleTask.getWntDurTotal());
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal,prodState,storageTaskWhereTime);
                setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                        ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                System.out.println("进入空-5:");
                currentHandleTask.setWntDurTotal(0L);
                result.put("currentHandleTask",currentHandleTask);
                GsThisInfo.setThisInfoTimeCount(thisInfo);
            }
        } else if (contrastTaskOne.getPriority() != -1 && contrastTaskTwo.getPriority() == -1)
        {
            // 判断当前任务优先级小于对比任务1的优先级
            if (currentHandleTask.getPriority() < contrastTaskOne.getPriority()) {
                // 任务余剩时间累加（+对比任务1的总时间）
                zon += contrastTaskOne.getWntDurTotal();
                // 任务余剩时间累减（-当前任务总时间）
                zon -= currentHandleTask.getWntDurTotal();
                // 更新任务集合对应的下标（i1）的任务信息为当前任务
                tasks.set(i, TaskObj.getTaskX(contrastTaskOne.getTePStart(),(contrastTaskOne.getTePStart()+currentHandleTask.getWntDurTotal())
                        ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                tePFinish = (contrastTaskOne.getTePStart()+currentHandleTask.getWntDurTotal());
                endTime = tasks.get(0).getTePStart();
                // 冲突任务集合添加对比任务1信息
                conflict.add(TaskObj.getTaskX(contrastTaskOne.getTePStart(),contrastTaskOne.getTePFinish(),contrastTaskOne.getWntDurTotal(),contrastTaskOne));
                addThisConflictInfoStatus(1, currentHandleTask.getDateIndex(), thisInfo);
                GsThisInfo.setThisInfoTimeCount(thisInfo);
//                addThisConflictLastODate(currentHandleTask.getId_O(), currentHandleTask.getId_C(), thisInfo,actionIdO);
                // 调用添加或更新产品状态方法
                addSho(sho, currentHandleTask.getId_O(),currentHandleTask.getIndex().toString(), contrastTaskOne.getId_O(),contrastTaskOne.getIndex().toString(),0);
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(taskTimeKeyFirstVal,teDate,currentHandleTask.getWntDurTotal());
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal
                        ,sho.getJSONObject(currentHandleTask.getId_O()).getJSONObject(currentHandleTask.getIndex().toString()).getInteger("prodState")
                        ,storageTaskWhereTime);
                setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                        ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                System.out.println("进入空-6:");
                currentHandleTask.setWntDurTotal(0L);
                result.put("currentHandleTask",currentHandleTask);
                GsThisInfo.setThisInfoTimeCount(thisInfo);
            } else {
                // 获取开始时间（对比任务1的结束时间+当前任务的任务总时间）
                long startTime = contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal();
                // 判断开始时间大于对比任务2的开始时间
                if (startTime > contrastTaskTwo.getTePStart()) {
                    // 获取余剩时间（对比时间2的开始时间-对比时间1的结束时间）
                    long remainingTime = contrastTaskTwo.getTePStart() - contrastTaskOne.getTePFinish();
                    // 判断余剩时间大于0
                    if (remainingTime > 0) {
                        // 获取时间差（当前任务总时间-余剩时间）
                        long timeDifference = currentHandleTask.getWntDurTotal() - remainingTime;
                        System.out.println(JSON.toJSONString(tasks));
                        // 任务集合添加任务下标加1（i1+1）添加任务信息
                        tasks.add((i+1), TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                ,(contrastTaskOne.getTePFinish()+remainingTime),remainingTime,currentHandleTask));
                        System.out.println(JSON.toJSONString(tasks));
                        tePFinish = (contrastTaskOne.getTePFinish()+remainingTime);
                        endTime = tasks.get(0).getTePStart();
                        // 任务余剩时间累减
                        zon -= remainingTime;
                        // 设置当前任务总共时间
                        currentHandleTask.setWntDurTotal(timeDifference);
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(taskTimeKeyFirstVal,teDate,remainingTime);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal,prodState,storageTaskWhereTime);
                        setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                                ,taskTimeKeyFirstVal, remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                        System.out.println("进入空-7:");
                        result.put("currentHandleTask",currentHandleTask);
                        GsThisInfo.setThisInfoTimeCount(thisInfo);
                    } else {
                        result.put("isTimeStopState",1);
//                        System.out.println("进入空-8-无需查看:");
                    }
                } else {
                    System.out.println(JSON.toJSONString(tasks));
                    // 任务集合添加任务下标加1（i1+1）添加任务信息
                    tasks.add((i+1), TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal())
                            ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                    System.out.println(JSON.toJSONString(tasks));
                    tePFinish = (contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal());
                    endTime = tasks.get(0).getTePStart();
                    // 任务余剩时间累减
                    zon -= currentHandleTask.getWntDurTotal();
                    // 冲突任务下标累加
                    conflictInd++;
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(taskTimeKeyFirstVal,teDate, currentHandleTask.getWntDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal,prodState,storageTaskWhereTime);
                    setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                            ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                    System.out.println("进入空-9:"+tePFinish);
                    currentHandleTask.setWntDurTotal(0L);
                    result.put("currentHandleTask",currentHandleTask);
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
                }
            }
        } else
        {
            // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
            int conflictIndexIsAccumulation = 0;
            // 判断当前任务优先级小于对比任务1的优先级
            if (currentHandleTask.getPriority() < contrastTaskOne.getPriority()) {
                // 任务余剩时间累加
                zon += contrastTaskOne.getWntDurTotal();
                // 任务余剩时间累减
                zon -= currentHandleTask.getWntDurTotal();
                // 更新任务集合对应的下标（i1）的任务信息为当前任务
                tasks.set(i, TaskObj.getTaskX(contrastTaskOne.getTePStart(),(contrastTaskOne.getTePStart()+currentHandleTask.getWntDurTotal())
                        ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                tePFinish = (contrastTaskOne.getTePStart()+currentHandleTask.getWntDurTotal());
                endTime = tasks.get(0).getTePStart();
                // 冲突任务集合添加对比任务1信息
                conflict.add(TaskObj.getTaskX(contrastTaskOne.getTePStart(),contrastTaskOne.getTePFinish(),contrastTaskOne.getWntDurTotal(),contrastTaskOne));
                addThisConflictInfoStatus(1,currentHandleTask.getDateIndex(),thisInfo);
                GsThisInfo.setThisInfoTimeCount(thisInfo);
//                addThisConflictLastODate(currentHandleTask.getId_O(), currentHandleTask.getId_C(), thisInfo,actionIdO);
                // 调用添加或更新产品状态方法
                addSho(sho, currentHandleTask.getId_O(),currentHandleTask.getIndex().toString(),contrastTaskOne.getId_O(), contrastTaskOne.getIndex().toString(),0);
                // 冲突任务下标累加
                conflictInd++;
                // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
                conflictIndexIsAccumulation = 1;
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(taskTimeKeyFirstVal,teDate, currentHandleTask.getWntDurTotal());
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal
                        ,sho.getJSONObject(currentHandleTask.getId_O()).getJSONObject(currentHandleTask.getIndex().toString()).getInteger("prodState")
                        ,storageTaskWhereTime);
                setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                        ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                System.out.println("进入空-x-1:");
                currentHandleTask.setWntDurTotal(0L);
                result.put("currentHandleTask",currentHandleTask);
                GsThisInfo.setThisInfoTimeCount(thisInfo);
            }
            // 获取余剩时间（对比时间2的开始时间-对比时间1的结束时间）
            long remainingTime = contrastTaskTwo.getTePStart() - contrastTaskOne.getTePFinish();
            // 判断余剩时间大于0
            if (remainingTime > 0) {
                System.out.println();
                System.out.println(JSON.toJSONString(currentHandleTask));
                System.out.println(JSON.toJSONString(contrastTaskOne));
                System.out.println(JSON.toJSONString(contrastTaskTwo));
                // 获取时间差（当前任务总时间-余剩时间）
                long timeDifference = currentHandleTask.getWntDurTotal() - remainingTime;
                System.out.println(JSON.toJSONString(tasks));
                // 任务集合添加任务下标加1（i1+1）添加任务信息
                tasks.add((i+1), TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                        ,(contrastTaskOne.getTePFinish()+remainingTime),remainingTime
                        ,currentHandleTask));
                System.out.println(JSON.toJSONString(tasks));
                tePFinish = (contrastTaskOne.getTePFinish()+remainingTime);
                endTime = tasks.get(0).getTePStart();
                // 任务余剩时间累减
                zon -= remainingTime;
                // 判断时间差不为0
                if (timeDifference != 0) {
                    // 设置当前任务总共时间
                    currentHandleTask.setWntDurTotal(timeDifference);
                }
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(taskTimeKeyFirstVal,teDate,remainingTime);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal,prodState,storageTaskWhereTime);
                setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                        ,taskTimeKeyFirstVal, remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                System.out.println("进入空-x-2:");
                result.put("currentHandleTask",currentHandleTask);
                GsThisInfo.setThisInfoTimeCount(thisInfo);
            } else {
                // 存储时间差 - 判断对比任务2优先级等于系统，如果等于就赋值为0，否则xin等于（对比任务2的开始时间-对比任务1的结束时间）
                long timeDifference = contrastTaskTwo.getPriority()==-1?0:contrastTaskTwo.getTePStart() - contrastTaskOne.getTePFinish();
                // 获取开始时间（对比任务1+存储时间差）
                long startTime = contrastTaskOne.getTePFinish() + timeDifference;
                // 开始时间累加当前任务总时间
                startTime += currentHandleTask.getWntDurTotal();
                // 判断开始时间大于对比任务2的结束时间并且，当前任务优先级小于对比任务2的优先级
                if (startTime > contrastTaskTwo.getTePFinish() && currentHandleTask.getPriority() < contrastTaskTwo.getPriority()) {
                    // 任务余剩时间累加
                    zon += contrastTaskTwo.getWntDurTotal();
                    // 判断存储时间差大于0
                    if (timeDifference > 0) {
                        // 设置当前任务总时间（当前任务总时间-存储时间差）
                        currentHandleTask.setWntDurTotal(currentHandleTask.getWntDurTotal()-timeDifference);
                    }
                    // 任务余剩时间累减
                    zon -= currentHandleTask.getWntDurTotal();
                    long wntDurTotal = currentHandleTask.getWntDurTotal();
                    // 更新任务集合对应的下标（i1+1）的任务信息为当前任务
                    tasks.set(i+1, TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal())
                            ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
                    tePFinish = (contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal());
                    endTime = tasks.get(0).getTePStart();
                    // 冲突任务集合添加对比任务2信息
                    conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getWntDurTotal(),contrastTaskTwo));
                    addThisConflictInfoStatus(1,currentHandleTask.getDateIndex(),thisInfo);
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
//                    addThisConflictLastODate(currentHandleTask.getId_O(), currentHandleTask.getId_C(), thisInfo,actionIdO);
                    // 调用添加或更新产品状态方法
                    addSho(sho, currentHandleTask.getId_O(),currentHandleTask.getIndex().toString(), contrastTaskTwo.getId_O(),contrastTaskTwo.getIndex().toString(),0);
                    // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
                    if (conflictIndexIsAccumulation == 0) {
                        // 冲突任务下标累加
                        conflictInd++;
                    }
                    System.out.println("进入前赋值-conflictInd-2-2:"+conflictInd);
                    // 调用处理冲突核心方法2
                    JSONObject emptyInsertAndEmptyInsertConflictHandleSonInfo = emptyInsertAndEmptyInsertConflictHandleSon(conflictInd, tasks, conflict, currentHandleTask, zon, teDate, random
                            , oDates, dgInfo, i,dep,grpB,taskTimeKeyFirstVal,timeConflictCopy,isGetTaskPattern,sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                            ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus,thisInfo
                            ,allImageTeDate,id_OAndIndexTaskInfo,depAllTime);
                    // 更新冲突集合指定的冲突下标的任务信息
                    conflict.set(conflictInd, TaskObj.getTaskX(currentHandleTask.getTePStart(),currentHandleTask.getTePFinish(),currentHandleTask.getWntDurTotal(),currentHandleTask));
                    // 获取冲突任务下标
                    conflictInd = emptyInsertAndEmptyInsertConflictHandleSonInfo.getInteger("conflictInd");
                    // 获取任务余剩时间
                    zon = emptyInsertAndEmptyInsertConflictHandleSonInfo.getLong("zon");
                    // 获取任务所在时间键的第一个键的值（时间戳）
                    taskTimeKeyFirstVal = emptyInsertAndEmptyInsertConflictHandleSonInfo.getLong("taskTimeKeyFirstVal");
                    System.out.println("进入后赋值-conflictInd-2-2:");
                    System.out.println(emptyInsertAndEmptyInsertConflictHandleSonInfo.getLong("tePFinish"));
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(taskTimeKeyFirstVal,teDate,wntDurTotal);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal
                            ,sho.getJSONObject(currentHandleTask.getId_O()).getJSONObject(currentHandleTask.getIndex().toString()).getInteger("prodState")
                            ,storageTaskWhereTime);
                    setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                            ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                    result.put("currentHandleTask",currentHandleTask);
                } else if (startTime > contrastTaskTwo.getTePStart() && startTime <= contrastTaskTwo.getTePFinish() && currentHandleTask.getPriority() < contrastTaskTwo.getPriority()) {
                    // 任务余剩时间累加
                    zon += contrastTaskTwo.getWntDurTotal();
                    // 判断存储时间差大于0
                    if (timeDifference > 0) {
                        // 设置当前任务总时间（当前任务总时间-存储时间差）
                        currentHandleTask.setWntDurTotal(currentHandleTask.getWntDurTotal()-timeDifference);
                    }
                    // 任务余剩时间累减
                    zon -= currentHandleTask.getWntDurTotal();
                    // 更新任务集合对应的下标（i1+1）的任务信息为当前任务
                    tasks.set(i+1, TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal())
                            ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                    tePFinish = (contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal());
                    endTime = tasks.get(0).getTePStart();
                    // 冲突任务集合添加对比任务2信息
                    conflict.add(TaskObj.getTaskX(contrastTaskTwo.getTePStart(),contrastTaskTwo.getTePFinish(),contrastTaskTwo.getWntDurTotal(),contrastTaskTwo));
                    addThisConflictInfoStatus(1,currentHandleTask.getDateIndex(),thisInfo);
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
//                    addThisConflictLastODate(currentHandleTask.getId_O(), currentHandleTask.getId_C(), thisInfo,actionIdO);
                    // 调用添加或更新产品状态方法
                    addSho(sho, currentHandleTask.getId_O(),currentHandleTask.getIndex().toString(),contrastTaskTwo.getId_O(), contrastTaskTwo.getIndex().toString(),0);
                    // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
                    if (conflictIndexIsAccumulation == 0) {
                        // 冲突任务下标累加
                        conflictInd++;
                    }
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(taskTimeKeyFirstVal,teDate,currentHandleTask.getWntDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal
                            ,sho.getJSONObject(currentHandleTask.getId_O()).getJSONObject(currentHandleTask.getIndex().toString()).getInteger("prodState")
                            ,storageTaskWhereTime);
                    // 更新镜像的所在时间方法
                    setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                            ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                    System.out.println("进入空-x-3:");
                    currentHandleTask.setWntDurTotal(0L);
                    result.put("currentHandleTask",currentHandleTask);
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
                } else if (startTime <= contrastTaskTwo.getTePStart()) {
//                    System.out.println("进入这个奇怪的地方");
                    // 判断存储时间差大于0
                    if (timeDifference > 0) {
                        // 设置当前任务总时间（当前任务总时间-存储时间差）
                        currentHandleTask.setWntDurTotal(currentHandleTask.getWntDurTotal()-timeDifference);
                    }
                    // 任务集合添加任务下标加1（i1+1）添加任务信息
                    tasks.add((i+1), TaskObj.getTaskX(contrastTaskOne.getTePFinish(),(contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal())
                            ,currentHandleTask.getWntDurTotal(),currentHandleTask));
                    tePFinish = (contrastTaskOne.getTePFinish()+currentHandleTask.getWntDurTotal());
                    endTime = tasks.get(0).getTePStart();
                    // 任务余剩时间累减
                    zon -= currentHandleTask.getWntDurTotal();
                    // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
                    if (conflictIndexIsAccumulation == 0) {
                        // 冲突任务下标累加
                        conflictInd++;
                    }
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(taskTimeKeyFirstVal,teDate,currentHandleTask.getWntDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(currentHandleTask.getId_O(), currentHandleTask.getDateIndex(),taskTimeKeyFirstVal,prodState,storageTaskWhereTime);
                    setAllImageTeDateAndDate(currentHandleTask.getId_O(),currentHandleTask.getDateIndex()
                            ,taskTimeKeyFirstVal, currentHandleTask.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                    System.out.println("进入空-x-4:");
                    currentHandleTask.setWntDurTotal(0L);
                    result.put("currentHandleTask",currentHandleTask);
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
                }
            }
        }
//        System.out.println("进入空插和空插冲突-结束:"+tePFinish);
        if (isSetTasks) {
            if (isGetTaskPattern == 0) {
//                System.out.println("进入空插和空插冲突写入数据-数据库:");
//                System.out.println(JSON.toJSONString(tasks));
                setTasksAndZon(tasks,grpB,dep,tasks.get(0).getTePStart(),zon,objTaskAll);
            } else {
//                System.out.println("进入空插和空插冲突写入数据-镜像:");
                setImageTasks(tasks,grpB,dep,tasks.get(0).getTePStart(),allImageTasks);
                setImageZon(zon,grpB,dep,tasks.get(0).getTePStart(),allImageTotalTime);
            }
        }
        System.out.println("进入空-返回结束时间:"+tePFinish);
        // 添加返回结果信息
        result.put("zon",zon);
        result.put("conflictInd",conflictInd);
        result.put("taskTimeKeyFirstVal",taskTimeKeyFirstVal);
        result.put("tePFinish",tePFinish);
        result.put("endTime",endTime);
        result.put("isSetTasks",isSetTasks);
        return result;
    }

    /**
     * 空插和空插冲突处理子方法
     * @param conflictInd	被冲突任务下标
     * @param tasks	任务集合
     * @param conflict	被冲突任务集合
     * @param currentHandleTask	当前处理任务
     * @param zon	任务余剩时间
     * @param teDate	当前处理的任务的所在日期对象
     * @param random	当前唯一编号
     * @param oDates	当前任务基础信息集合
     * @param dgInfo	当前任务基础信息
     * @param i	任务下标
     * @param dep	部门
     * @param grpB	组别
     * @param taskTimeKeyFirstVal	任务所在时间键的第一个键的值（时间戳）
     * @param timeConflictCopy	当前任务所在日期对象
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
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
     * @param id_OAndIndexTaskInfo 被清理的任务信息
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public JSONObject emptyInsertAndEmptyInsertConflictHandleSon(int conflictInd, List<Task> tasks, List<Task> conflict
            , Task currentHandleTask, Long zon, JSONObject teDate, String random, JSONArray oDates
            , JSONObject dgInfo, int i, String dep, String grpB, Long taskTimeKeyFirstVal
            , JSONObject timeConflictCopy, Integer isGetTaskPattern, JSONObject sho
            , int csSta, String randomAll, JSONObject xbAndSbAll, JSONObject actionIdO
            , JSONObject objTaskAll
            , JSONObject recordId_OIndexState, JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks, JSONObject onlyFirstTimeStamp
            , JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState, JSONObject recordNoOperation
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,JSONObject id_OAndIndexTaskInfo
            ,JSONObject depAllTime) {
        // 创建返回结果对象
        JSONObject result = new JSONObject();
        long tePFinish = 0;
        long endTime = 0;
        // 判断冲突任务下标小于冲突任务集合总长度
        if (conflictInd < conflict.size()) {
            // 获取冲突任务信息
            currentHandleTask = conflict.get(conflictInd);
        }
        // 遍历任务集合
        for (int j = i+1; j < tasks.size(); j++) {
            // 判断冲突任务下标大于等于冲突任务集合总长度
            if (conflictInd >= conflict.size()) {
                break;
            }
            // 获取对比任务1
            Task contrastTaskOne = tasks.get(j);
            // 判断（i2（当前任务下标） + 1）小于任务集合总长度
            if ((j + 1) < tasks.size()) {
                // 获取对比任务2
                Task contrastTaskTwo = tasks.get(j + 1);
//                boolean isEndList = (j+1) == tasks.size()-1;
                // 调用空插冲突处理方法
                JSONObject emptyInsertAndEmptyInsertConflictHandleInfo = emptyInsertAndEmptyInsertConflictHandle(currentHandleTask, contrastTaskOne
                        , contrastTaskTwo, tasks, j, conflictInd, zon, conflict,teDate,random, dep,grpB
                        ,oDates,dgInfo,taskTimeKeyFirstVal,timeConflictCopy,isGetTaskPattern,sho
                        ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,recordId_OIndexState
                        ,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                        ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate,id_OAndIndexTaskInfo
                        ,true,depAllTime);
//                System.out.println("空插和空插冲突处理方法-5:");
//                System.out.println(JSON.toJSONString(emptyInsertAndEmptyInsertConflictHandleInfo));
//                System.out.println(JSON.toJSONString(currentHandleTask));
//                System.out.println(JSON.toJSONString(conflict));
                // 获取任务余剩时间
                zon = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("zon");
                // 更新冲突任务集合指定下标（冲突任务下标）的冲突任务信息
                conflict.set(conflictInd, TaskObj.getTaskX(currentHandleTask.getTePStart(),currentHandleTask.getTePFinish(),currentHandleTask.getWntDurTotal(),currentHandleTask));
                // 获取冲突任务下标
                conflictInd = emptyInsertAndEmptyInsertConflictHandleInfo.getInteger("conflictInd");
                // 获取任务所在时间键的第一个键的值（时间戳）
                taskTimeKeyFirstVal = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("taskTimeKeyFirstVal");
                tePFinish = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("tePFinish");
                endTime = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("endTime");
                // 判断冲突任务下标小于冲突任务集合的长度
                if (conflictInd < conflict.size()) {
                    // 获取当前任务订单编号
                    String id_O = currentHandleTask.getId_O();
                    // 获取当前任务订单下标
                    int index = currentHandleTask.getIndex();
                    // 根据冲突任务下标获取冲突任务对象
                    Task conflictTask = conflict.get(conflictInd);
                    // 深度复制冲突任务对象
                    Task conflictTaskCopy = TaskObj.getTaskX(conflictTask.getTePStart(),conflictTask.getTePFinish()
                            ,conflictTask.getWntDurTotal(),conflictTask);
                    // 判断当前任务订单编号不等于冲突任务订单编号，或者当前任务订单下标不等于冲突任务订单下标
                    if (!id_O.equals(conflictTaskCopy.getId_O()) || index != conflictTaskCopy.getIndex()) {
                        // 根据当前任务订单编号获取任务所在日期对象
                        JSONObject id_OTaskTime = timeConflictCopy.getJSONObject(currentHandleTask.getId_O());
                        // 根据当前任务订单下标添加任务所在日期
                        id_OTaskTime.put(currentHandleTask.getDateIndex()+"",teDate);
                        // 根据当前任务订单编号添加任务所在日期对象
                        timeConflictCopy.put(currentHandleTask.getId_O(),id_OTaskTime);
                        // 获取清理状态方法
                        int clearStatusThis = getClearStatus(currentHandleTask.getId_O()
                                , (currentHandleTask.getDateIndex()+1), clearStatus);
                        // 判断状态为0
                        if (clearStatusThis == 0) {
                            // 写入清理状态方法
                            GsThisInfo.setClearStatus(currentHandleTask.getId_O()
                                    , (currentHandleTask.getDateIndex()+1),clearStatus,1);
                            System.out.println("进入清理-5 | timeComprehensiveHandle - 6:清理状态:"+clearStatusThis);
                            int nextDateIndex = (currentHandleTask.getDateIndex() + 1);
                            if (nextDateIndex != oDates.size()){
                                // 获取零件信息
                                JSONObject nextInfo = oDates.getJSONObject(nextDateIndex);
//                                    System.out.println("timeComprehensiveHandle - 6 :清理状态:"+clearStatusThis);
                                // 调用获取冲突处理方法，原方法
                                timeZjServiceComprehensive.timeComprehensiveHandle(nextInfo.getString("id_O")
                                        ,nextInfo.getInteger("index"),nextDateIndex
                                        ,timeConflictCopy,isGetTaskPattern,0,sho,currentHandleTask.getId_C()
                                        ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                        ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                                        ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                        ,tePFinish,id_OAndIndexTaskInfo,clearStatus,thisInfo,allImageTeDate,endTime,depAllTime
                                        ,random,false,nextInfo.getString("id_PF"),nextInfo.getString("layer"));
                                // 创建任务所在日期对象
                                teDate = new JSONObject();
                                // 判断当前任务订单编号不等于冲突任务订单编号
                                if (!id_O.equals(conflictTaskCopy.getId_O())) {
//                                        // 获取进度卡片的所有递归信息
//                                        objAction = actionIdO.getJSONArray(conflictTaskCopy.getId_O());
//                                        if (null == objAction) {
////                                            System.out.println("为空-获取数据库-5:"+conflictTaskCopy.getId_O());
//                                            // 根据冲突任务订单编号获取订单信息 - t
////                                            Order order = coupaUtil.getOrderByListKey(conflictTaskCopy.getId_O(), Collections.singletonList("action"));
//                                            Order order = qt.getMDContent(conflictTaskCopy.getId_O(),"action", Order.class);
//                                            // 获取进度卡片的所有递归信息
//                                            objAction = order.getAction().getJSONArray("objAction");
//                                            actionIdO.put(conflictTaskCopy.getId_O(),objAction);
//                                        }
                                    String conflictTaskId_OP = sonGetOrderFatherId(conflictTaskCopy.getId_O(), conflictTaskCopy.getId_C(), thisInfo, actionIdO, new JSONObject());
                                    oDates = actionIdO.getJSONObject(conflictTaskId_OP).getJSONObject(conflictTaskCopy
                                            .getLayer()+"").getJSONObject(conflictTaskCopy.getId_PF()).getJSONArray("oDates");
                                }
                                // 根据冲突任务订单下标获取递归信息
                                dgInfo = oDates.getJSONObject(conflictTaskCopy.getDateIndex());
                                // 获取递归信息的任务所在日期对象
                                JSONObject teDateNew = dgInfo.getJSONObject("teDate");
                                // 获取任务所在日期的所有键
                                Set<String> teDateNewKey = teDateNew.keySet();
                                // 将设置日期键转换成集合类型
                                List<String> teDateNewKeyCopy = new ArrayList<>(teDateNewKey);
                                // 获取任务所在时间键的第一个键的值（时间戳）
                                taskTimeKeyFirstVal = Long.parseLong(teDateNewKeyCopy.get(0));
                                // 调用写入当前时间戳方法
                                setTeS(random , dgInfo.getString("grpB"), dgInfo.getString("dep"),taskTimeKeyFirstVal,newestLastCurrentTimestamp);
                                // 根据冲突任务订单编号添加任务所在日期
                                timeConflictCopy.put(conflictTaskCopy.getId_O()
                                        ,qt.setJson(conflictTaskCopy.getDateIndex()+"",teDate));
                            }
//                            // 获取下一个零件信息列表
//                            JSONArray prtNext = dgInfo.getJSONArray("prtNext");
//                            // 判断不为空
//                            if (null != prtNext && prtNext.size() > 0) {
//                                // 遍历零件信息列表
//                                for (int next = 0; next < prtNext.size(); next++) {
//                                    // 获取零件信息
//                                    JSONObject nextInfo = prtNext.getJSONObject(next);
////                                    System.out.println("timeComprehensiveHandle - 6 :清理状态:"+clearStatusThis);
//                                    // 调用获取冲突处理方法，原方法
//                                    timeZjServiceComprehensive.timeComprehensiveHandle(nextInfo.getString("id_O")
//                                            ,nextInfo.getInteger("index")
//                                            ,timeConflictCopy,isGetTaskPattern,0,sho,currentHandleTask.getId_C()
//                                            ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
//                                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
//                                            ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
//                                            ,tePFinish,id_OAndIndexTaskInfo,clearStatus,thisInfo,allImageTeDate,endTime,depAllTime,random,false);
//                                    // 创建任务所在日期对象
//                                    teDate = new JSONObject();
//                                    // 判断当前任务订单编号不等于冲突任务订单编号
//                                    if (!id_O.equals(conflictTaskCopy.getId_O())) {
////                                        // 获取进度卡片的所有递归信息
////                                        objAction = actionIdO.getJSONArray(conflictTaskCopy.getId_O());
////                                        if (null == objAction) {
//////                                            System.out.println("为空-获取数据库-5:"+conflictTaskCopy.getId_O());
////                                            // 根据冲突任务订单编号获取订单信息 - t
//////                                            Order order = coupaUtil.getOrderByListKey(conflictTaskCopy.getId_O(), Collections.singletonList("action"));
////                                            Order order = qt.getMDContent(conflictTaskCopy.getId_O(),"action", Order.class);
////                                            // 获取进度卡片的所有递归信息
////                                            objAction = order.getAction().getJSONArray("objAction");
////                                            actionIdO.put(conflictTaskCopy.getId_O(),objAction);
////                                        }
//                                        String conflictTaskId_OP = sonGetOrderFatherId(conflictTaskCopy.getId_O(), conflictTaskCopy.getId_C(), thisInfo, actionIdO, new JSONObject());
//                                        oDates = actionIdO.getJSONObject(conflictTaskId_OP).getJSONArray("oDates");
//                                    }
//                                    // 根据冲突任务订单下标获取递归信息
//                                    dgInfo = oDates.getJSONObject(conflictTaskCopy.getDateIndex());
//                                    // 获取递归信息的任务所在日期对象
//                                    JSONObject teDateNew = dgInfo.getJSONObject("teDate");
//                                    // 获取任务所在日期的所有键
//                                    Set<String> teDateNewKey = teDateNew.keySet();
//                                    // 将设置日期键转换成集合类型
//                                    List<String> teDateNewKeyCopy = new ArrayList<>(teDateNewKey);
//                                    // 获取任务所在时间键的第一个键的值（时间戳）
//                                    taskTimeKeyFirstVal = Long.parseLong(teDateNewKeyCopy.get(0));
//                                    // 调用写入当前时间戳方法
//                                    setTeS(random , dgInfo.getString("grpB"), dgInfo.getString("dep"),taskTimeKeyFirstVal,newestLastCurrentTimestamp);
//                                    // 创建任务所在日期存储对象
//                                    JSONObject teDaTaskTime = new JSONObject();
//                                    // 根据冲突任务订单下标添加任务所在日期
//                                    teDaTaskTime.put(conflictTaskCopy.getIndex()+"",teDate);
//                                    // 根据冲突任务订单编号添加任务所在日期
//                                    timeConflictCopy.put(conflictTaskCopy.getId_O(),teDaTaskTime);
//                                }
//                            }
                        }
                    }
                    // 根据冲突任务下标获取冲突任务信息
                    currentHandleTask = conflict.get(conflictInd);
//                    System.out.println(JSON.toJSONString(currentHandleTask));
//                    System.out.println("换taskX:"+zon);
                } else
                {
                    // 根据当前任务订单编号获取任务所在日期对象
                    JSONObject id_OTaskTime = timeConflictCopy.getJSONObject(currentHandleTask.getId_O());
                    // 根据当前任务订单下标添加任务所在日期
                    id_OTaskTime.put(currentHandleTask.getDateIndex()+"",teDate);
                    // 根据当前任务订单编号添加任务所在日期对象
                    timeConflictCopy.put(currentHandleTask.getId_O(),id_OTaskTime);
                    // 获取清理状态方法
                    int clearStatusThis = getClearStatus(currentHandleTask.getId_O()
                            , (currentHandleTask.getDateIndex()+1), clearStatus);
                    // 判断状态为0
                    if (clearStatusThis == 0) {
                        // 写入清理状态方法
                        GsThisInfo.setClearStatus(currentHandleTask.getId_O()
                                , (currentHandleTask.getDateIndex()+1),clearStatus,1);
                        System.out.println("进入清理-6 | timeComprehensiveHandle - 7 :清理状态:"+clearStatusThis);
                        int nextDateIndex = (currentHandleTask.getDateIndex() + 1);
                        if (nextDateIndex != oDates.size()){
                            // 获取零件信息
                            JSONObject nextInfo = oDates.getJSONObject(nextDateIndex);
//                                System.out.println("timeComprehensiveHandle - 7 :清理状态:"+clearStatusThis);
                            // 调用获取冲突处理方法，原方法
                            JSONObject timeComprehensiveHandleInfo = timeZjServiceComprehensive.timeComprehensiveHandle(nextInfo.getString("id_O")
                                    , nextInfo.getInteger("index"), nextDateIndex
                                    , timeConflictCopy, isGetTaskPattern, 0, sho, currentHandleTask.getId_C()
                                    , csSta, randomAll, xbAndSbAll, actionIdO, objTaskAll
                                    , recordId_OIndexState, storageTaskWhereTime, allImageTotalTime, allImageTasks
                                    , onlyFirstTimeStamp, newestLastCurrentTimestamp, onlyRefState, recordNoOperation
                                    , tePFinish, id_OAndIndexTaskInfo, clearStatus, thisInfo, allImageTeDate, endTime, depAllTime
                                    , random, false, nextInfo.getString("id_PF"), nextInfo.getString("layer"));
                            Boolean lastComplete = timeComprehensiveHandleInfo.getBoolean("lastComplete");
                            if (null != lastComplete && lastComplete) {
                                System.out.println("-最新-结束:");
                                result.put("lastComplete",true);
                                break;
                            }
                        }
//                        // 获取下一个零件列表
//                        JSONArray prtNext = dgInfo.getJSONArray("prtNext");
//                        // 判断不为空
//                        if (null != prtNext && prtNext.size() > 0) {
//                            // 遍历零件列表
//                            for (int next = 0; next < prtNext.size(); next++) {
//                                // 获取零件信息
//                                JSONObject nextInfo = prtNext.getJSONObject(next);
////                                System.out.println("timeComprehensiveHandle - 7 :清理状态:"+clearStatusThis);
//                                // 调用获取冲突处理方法，原方法
//                                timeZjServiceComprehensive.timeComprehensiveHandle(nextInfo.getString("id_O")
//                                        ,nextInfo.getInteger("index")
//                                        ,timeConflictCopy,isGetTaskPattern,0,sho,currentHandleTask.getId_C()
//                                        ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
//                                        ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
//                                        ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
//                                        ,tePFinish,id_OAndIndexTaskInfo,clearStatus,thisInfo,allImageTeDate,endTime,depAllTime,random,false);
//                            }
//                        }
                    }
                }
            }
        }
        result.put("conflictInd",conflictInd);
        result.put("zon",zon);
        result.put("taskTimeKeyFirstVal",taskTimeKeyFirstVal);
        result.put("tePFinish",tePFinish);
        result.put("endTime",endTime);
        result.put("currentHandleTask",currentHandleTask);
        return result;
    }

    /**
     * 时间处理删除方法: 根据主订单和对应公司编号，删除时间处理信息 ( TimeZjServiceImplX类的，分割到该类 )
     * @param id_O	主订单编号
     * @param id_C	公司编号
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public ApiResponse removeTime(String id_O, String id_C){
        // 调用方法获取订单信息
//        Order order = coupaUtil.getOrderByListKey(
//                id_O, Arrays.asList("casItemx","action"));
        Order order = qt.getMDContent(id_O,qt.strList("casItemx","action"), Order.class);
        // 判断订单是否为空
        if (null == order || null == order.getCasItemx() || null == order.getAction()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        // 获取递归订单列表
        JSONArray objOrder = order.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
        // 获取主订单的时间处理所在日期存储信息
        JSONObject timeRecord = order.getAction().getJSONObject("timeRecord");
//        Asset asset = qt.getConfig(id_C,"a-chkin",timeCard);
//        // 获取时间处理卡片信息
//        JSONObject aArrange = getAArrangeNew(asset);
//        // 获取时间处理信息
//        JSONObject objTask = aArrange.getJSONObject("objTask");
//        // 创建时间处理信息镜像
//        JSONObject objTaskImage = JSONObject.parseObject(objTask.toJSONString());
        // 创建时间处理删除后任务列表存储对象
        JSONObject delAfterObjTask = new JSONObject();
        Map<String,Asset> assetMap = new HashMap<>();
        // 遍历订单列表
        for (int j = 0; j < objOrder.size(); j++) {
            // 获取订单编号
            String id_OSon = objOrder.getJSONObject(j).getString("id_O");
            // 判断订单编号等于主订单编号
            if (id_OSon.equals(id_O)) {
                continue;
            }
            // 遍历时间处理所在日期存储部门列表
            timeRecord.keySet().forEach(dep -> {
                Asset asset;
                if (assetMap.containsKey(dep)) {
                    asset = assetMap.get(dep);
                } else {
                    asset = qt.getConfig(id_C,"d-"+dep,timeCard);
                    assetMap.put(dep,asset);
                }
                // 获取时间处理卡片信息
                JSONObject aArrange = getAArrangeNew(asset);
                // 获取时间处理信息
                JSONObject objTask = aArrange.getJSONObject("objTask");
                // 获取时间处理所在日期存储部门列表
                JSONObject grpBs = timeRecord.getJSONObject(dep);
                // 遍历时间处理所在日期存储组别列表
                grpBs.keySet().forEach(grpB -> {
                    // 获取时间处理所在日期存储组别列表
                    JSONObject teSs = grpBs.getJSONObject(grpB);
                    // 遍历时间处理所在日期存储日期列表
                    teSs.keySet().forEach(teS -> {
                        // 根据部门组别日期获取对应的任务信息
                        JSONObject taskInfo = objTask.getJSONObject(grpB).getJSONObject(teS);
                        // 获取任务信息列表
                        JSONArray tasks = taskInfo.getJSONArray("tasks");
                        // 获取删除后的部门任务信息
                        JSONObject depDelAfter = delAfterObjTask.getJSONObject(dep);
                        // 判断为空
                        if (null == depDelAfter) {
                            // 创建
                            depDelAfter = new JSONObject();
                        }
                        // 获取删除后的组别任务信息
                        JSONObject grpBDelAfter = depDelAfter.getJSONObject(grpB);
                        if (null == grpBDelAfter) {
                            grpBDelAfter = new JSONObject();
                        }
                        // 获取删除后的时间戳任务信息
                        JSONObject teSDelAfter = grpBDelAfter.getJSONObject(teS);
                        if (null == teSDelAfter) {
                            teSDelAfter = new JSONObject();
                        }
                        // 获取删除后余剩总时间
                        Long zon = teSDelAfter.getLong("zon");
                        // 判断为空
                        if (null == zon) {
                            // 获取余剩总时间
                            zon = taskInfo.getLong("zon");
//                            System.out.println("zon-null:"+zon);
                        }
                        // 定义存储任务列表需要删除的任务下标
                        List<Integer> removeIndex = new ArrayList<>();
                        // 遍历任务列表
                        for (int i = 0; i < tasks.size(); i++) {
                            // 获取任务信息
                            JSONObject task = tasks.getJSONObject(i);
                            // 判断订单编号等于要删除的订单编号
                            if (task.getString("id_O").equals(id_OSon)) {
                                // 添加删除的任务下标
                                removeIndex.add(i);
//                                System.out.println("zon-q:"+zon);
                                // 累加余剩总时间
                                zon = zon + task.getLong("wntDurTotal");
//                                System.out.println("wntDurTotal:"+task.getLong("wntDurTotal"));
//                                System.out.println("zon-h:"+zon);
                            }
                        }
                        // 判断任务列表需要删除的任务下标不为空
                        if (removeIndex.size() > 0) {
                            // 降序排序循环存储下标
                            removeIndex.sort(Comparator.reverseOrder());
                            // 遍历并删除列表对应下标的任务
                            for (int index : removeIndex) {
                                tasks.remove(index);
                            }
                        }
                        // 添加删除后信息
                        teSDelAfter.put("zon",zon);
                        teSDelAfter.put("tasks",tasks);
                        grpBDelAfter.put(teS,teSDelAfter);
                        depDelAfter.put(grpB,grpBDelAfter);
                        delAfterObjTask.put(dep,depDelAfter);
                    });
                });
            });
        }
        // 遍历删除后的部门信息
        delAfterObjTask.keySet().forEach(dep -> {
            Asset asset;
            if (assetMap.containsKey(dep)) {
                asset = assetMap.get(dep);
            } else {
                asset = qt.getConfig(id_C,"d-"+dep,timeCard);
                assetMap.put(dep,asset);
            }
            // 获取时间处理卡片信息
            JSONObject aArrange = getAArrangeNew(asset);
            // 获取时间处理信息
            JSONObject objTask = aArrange.getJSONObject("objTask");
            // 创建时间处理信息镜像
            JSONObject objTaskImage = JSONObject.parseObject(objTask.toJSONString());
            // 获取删除后的部门信息
            JSONObject grpBs = delAfterObjTask.getJSONObject(dep);
//            // 获取镜像的部门信息
//            JSONObject grpBsImage = objTaskImage.getJSONObject(dep);
            // 遍历删除后的组别信息
            grpBs.keySet().forEach(grpB -> {
                // 获取删除后的组别信息
                JSONObject teSs = grpBs.getJSONObject(grpB);
                // 获取镜像的组别信息
                JSONObject teSsImage = objTaskImage.getJSONObject(grpB);
                // 遍历删除后的时间戳信息
                teSs.keySet().forEach(teS -> {
                    // 直接添加删除后的任务信息到镜像任务信息
                    teSsImage.put(teS,teSs.getJSONObject(teS));
                    objTaskImage.put(grpB,teSsImage);
//                    objTaskImage.put(dep,grpBsImage);
                    aArrange.put("objTask",objTaskImage);
                    asset.setAArrange(aArrange);
                    assetMap.put(dep,asset);
                });
            });

        });
        for (String dep : assetMap.keySet()) {
            Asset asset = assetMap.get(dep);
            qt.setMDContent(asset.getId(),qt.setJson(timeCard,asset.getAArrange()), Asset.class);
        }
//        // 添加镜像任务信息
//        aArrange.put("objTask",objTaskImage);
////        // 创建请求参数存储字典
////        JSONObject mapKey = new JSONObject();
////        // 添加请求参数
////        mapKey.put(timeCard,aArrange);
////        // 请求修改卡片信息
////        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
//        qt.setMDContent(asset.getId(),qt.setJson(timeCard,aArrange), Asset.class);

//        // 创建请求更改参数
//        mapKey = new JSONObject();
//        // 添加请求更改参数信息
//        mapKey.put("action.timeRecord",new JSONObject());
//        // 调用接口发起数据库更改信息请求
//        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
        qt.setMDContent(id_O,qt.setJson("action.timeRecord",new JSONObject()), Order.class);

        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "时间删除处理成功!");
    }

    /**
     * 计算空插时间方法 ( TimeZjServiceImplX类的，分割到该类 )
     * @param task	当前任务信息
     * @param task1	对比任务信息1
     * @param task2	对比任务信息2
     * @param tasks	任务集合
     * @param i	任务下标
     * @param zon	任务余剩时间
     * @param isControlCalculationMode	控制计算模式参数，isKC == 0 需要重写任务的开始时间和结束时间、isKC == 1 不需要重写任务的开始时间和结束时间、isKC == 2 使用新模式进行计算时间
     * @param random	当前唯一编号
     * @param teDate	存储当前处理的任务的所在日期对象状态
     * @param dep	部门
     * @param grpB	组别
     * @param prodState	产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
     * @param storageTaskWhereTime 存储任务所在日期
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param isGetTaskPattern  = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param allImageTeDate 镜像任务所在日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public JSONObject calculationEmptyInsertTime(Task task, Task task1, Task task2, List<Task> tasks
            , Integer i, Long zon, Integer isControlCalculationMode,String random,JSONObject teDate,String dep
            ,String grpB,int prodState,JSONObject storageTaskWhereTime,JSONObject onlyFirstTimeStamp
            ,JSONObject newestLastCurrentTimestamp,Integer isGetTaskPattern,JSONObject allImageTeDate
            ,JSONObject onlyRefState,JSONObject thisInfo){
        // 创建返回结果
        JSONObject result = new JSONObject();
        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
        result.put("isJumpDay", 0);
        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
        result.put("isEndLoop",0);
        // 获取余剩时间（对比任务信息2的开始时间-对比任务信息1的结束时间）
        long remainingTime = task2.getTePStart() - task1.getTePFinish();
        // 判断余剩时间大于0
        if (remainingTime > 0) {
            // 控制计算模式参数，isKC == 0 需要重写任务的开始时间和结束时间、isKC == 1 不需要重写任务的开始时间和结束时间、isKC == 2 使用新模式进行计算时间
            if (isControlCalculationMode == 2) {
                // 添加控制是否结束跳天
                result.put("isJumpDay", 1);
                // 获取时间差（当前任务的开始时间-对比任务信息1的结束时间）
                long currentTimeDifference = task.getTePStart() - task1.getTePFinish();
                // 获取时间差2（余剩时间-时间差）
                long remainingTimeDifference = remainingTime - currentTimeDifference;
                // 设置任务总共时间（任务总共时间-时间差2）
                task.setWntDurTotal(task.getWntDurTotal()-remainingTimeDifference);
                // 设置任务开始时间
                task.setTePStart(task.getTePStart());
                // 设置任务结束时间（对比任务信息2的开始时间）
                task.setTePFinish(task2.getTePStart());
                System.out.println(JSON.toJSONString(tasks));
                // 任务集合指定添加任务下标+1位置添加任务信息
                tasks.add(i+1, TaskObj.getTaskX(task.getTePStart(),task2.getTePStart(),remainingTimeDifference,task));
                System.out.println(JSON.toJSONString(tasks));
                // 添加控制是否结束循环参数
                result.put("isEndLoop",4);
                result.put("tePFinish",task2.getTePStart());
                result.put("endTime",tasks.get(0).getTePStart());
                // 任务余剩时间累减
                zon -= remainingTimeDifference;
                // 调用获取当前时间戳方法
                Long teS = getTeS(random, grpB, dep,onlyFirstTimeStamp,newestLastCurrentTimestamp);
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(teS,teDate,remainingTimeDifference);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(task.getId_O(), task.getDateIndex(),teS,prodState,storageTaskWhereTime);
                setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex(),teS,remainingTimeDifference
                        ,allImageTeDate,isGetTaskPattern,tasks.get(0).getTePStart());
                if (task.getWntDurTotal() == 0) {
                    System.out.println("ji-1-进入等于0:");
                    result.put("isEndLoop",2);
                }
                System.out.println("ji-1:");
                onlyRefState.put(random,1);
                GsThisInfo.setThisInfoTimeCount(thisInfo);
            } else {
                // 添加控制是否跳天参数
                result.put("isJumpDay", 1);
                // 获取时间差（余剩时间-当前任务时间）
                long timeDifference = remainingTime - task.getWntDurTotal();
                long wntDurTotal;
                // 判断时间差大于等于0
                if (timeDifference >= 0) {
                    // 控制计算模式参数，isKC == 0 需要重写任务的开始时间和结束时间、isKC == 1 不需要重写任务的开始时间和结束时间、isKC == 2 使用新模式进行计算时间
                    if (isControlCalculationMode == 0) {
                        // 更新任务开始时间
                        task.setTePStart(task1.getTePFinish());
                        // 更新任务结束时间
                        task.setTePFinish(task1.getTePFinish()+task.getWntDurTotal());
                    }
                    System.out.println(JSON.toJSONString(tasks));
                    // 任务集合指定添加任务下标+1位置添加任务信息
                    tasks.add(i+1, TaskObj.getTaskX(task.getTePStart(),task.getTePFinish(),task.getWntDurTotal(),task));
                    System.out.println(JSON.toJSONString(tasks));
                    wntDurTotal = task.getWntDurTotal();
                    // 添加控制是否结束循环参数
                    result.put("isEndLoop",2);
                    result.put("tePFinish",task.getTePFinish());
                    result.put("endTime",tasks.get(0).getTePStart());
                    // 任务余剩时间累减
                    zon -= task.getWntDurTotal();
                    System.out.println("ji-2:");
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
                    onlyRefState.put(random,1);
                } else {
                    // 获取时间差2（当前任务时间-余剩时间）
                    long currentTimeDifference = task.getWntDurTotal() - remainingTime;
                    // 更新任务总时间
                    task.setWntDurTotal(currentTimeDifference);
                    // 更新任务开始时间
                    task.setTePStart(task1.getTePFinish());
                    // 更新任务结束时间
                    task.setTePFinish((task1.getTePFinish()+remainingTime));
                    System.out.println(JSON.toJSONString(tasks));
                    // 任务集合指定添加任务下标+1位置添加任务信息
                    tasks.add(i+1, TaskObj.getTaskX(task1.getTePFinish()
                            ,(task1.getTePFinish()+remainingTime),remainingTime,task));
                    System.out.println(JSON.toJSONString(tasks));
                    wntDurTotal = remainingTime;
                    // 添加控制是否结束循环参数
                    result.put("isEndLoop",4);
                    result.put("tePFinish",(task1.getTePFinish()+remainingTime));
                    result.put("endTime",tasks.get(0).getTePStart());
                    // 任务余剩时间累减
                    zon -= remainingTime;
                    System.out.println("ji-3:");
                    GsThisInfo.setThisInfoTimeCount(thisInfo);
                }
                // 调用获取当前时间戳方法
                Long teS = getTeS(random, grpB, dep,onlyFirstTimeStamp,newestLastCurrentTimestamp);
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(teS,teDate,wntDurTotal);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(task.getId_O(), task.getDateIndex(),teS,prodState,storageTaskWhereTime);
                setAllImageTeDateAndDate(task.getId_O(),task.getDateIndex(),teS
                        ,wntDurTotal,allImageTeDate,isGetTaskPattern,tasks.get(0).getTePStart());
            }
        } else {
            // 添加控制是否结束循环参数
            result.put("isEndLoop",3);
            result.put("endTime",tasks.get(0).getTePStart());
        }
        result.put("zon",zon);
//        System.out.println("kc-storageTaskWhereTime:");
//        System.out.println(JSON.toJSONString(storageTaskWhereTime));
        return result;
    }
}
