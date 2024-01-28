package com.cresign.action.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.chkin.Task;

import java.util.List;
import java.util.Map;

/**
 * @author tang
 * @Description 作者很懒什么也没写...
 * @ClassName TimeZjServiceComprehensive
 * @Date 2022/10/25
 * @ver 1.0.0
 */
public interface TimeZjServiceComprehensive {

    /**
     * 时间处理综合方法（跳天操作、延迟时间记录操作、等等一系列操作...）
     * @param id_oNext	当前处理订单编号
     * @param indONext	当前处理订单编号对应的下标
     * @param timeConflictCopy	当前任务所在时间
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param isEmptyInsert	isR = 0 不是空插处理、isR = 1 是空插处理 isEmptyInsert
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param id_C	公司编号
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
     * @param tePFinish 任务最后结束时间
     * @param id_OAndIndexTaskInfo 被清理的任务信息
     * @param clearStatus 清理状态信息
     * @param thisInfo 当前处理通用信息存储
     * @param allImageTeDate 镜像任务所在日期
     * @param endTime 任务结束日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 conflictHandle
     */
    JSONObject timeComprehensiveHandle(String id_oNext, Integer indONext, JSONObject timeConflictCopy
            , Integer isGetTaskPattern, Integer isEmptyInsert, JSONObject sho, String id_C
            , int csSta, String randomAll, JSONObject xbAndSbAll, JSONObject actionIdO
            , JSONObject objTaskAll, JSONObject recordId_OIndexState, JSONObject storageTaskWhereTime
            , JSONObject allImageTotalTime, Map<String,Map<String, Map<Long, List<Task>>>> allImageTasks
            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState
            , JSONObject recordNoOperation,Long tePFinish,JSONObject id_OAndIndexTaskInfo
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,long endTime
            ,JSONObject depAllTime,String randomJiu);

//    JSONObject timeComprehensiveHandleCopy(String id_oNext, Integer indONext, JSONObject timeConflictCopy
//            , Integer isGetTaskPattern, Integer isEmptyInsert, JSONObject sho, String id_C
//            , int csSta, String randomAll, JSONObject xbAndSbAll, JSONObject actionIdO
//            , JSONObject objTaskAll, JSONObject recordId_OIndexState, JSONObject storageTaskWhereTime
//            , JSONObject allImageTotalTime, Map<String,Map<String, Map<Long, List<Task>>>> allImageTasks
//            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState
//            , JSONObject recordNoOperation,Long tePFinish,int dateIndex);

    /**
     * 任务最后处理方法 ( TimeZjServiceImplX.timeHandle()方法的，分割到该类 )
     * @param timeConflictCopy	时间冲突的副本
     * @param id_C  公司编号
     * @param randomAll	全局唯一编号
     * @param objTaskAll    全局任务信息
     * @param storageTaskWhereTime   存储任务所在日期
     * @param allImageTotalTime  全局任务余剩总时间信息
     * @param allImageTasks    全局任务列表镜像信息
     * @param recordNoOperation 定义，存储进入未操作到的地方记录
     * @param id_O  订单编号
     * @param objOrderList  订单编号列表
     * @param actionIdO 存储casItemx内订单列表的订单action数据
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 2:55
     */
    void taskLastHandle(JSONObject timeConflictCopy, String id_C, String randomAll, JSONObject objTaskAll, JSONObject storageTaskWhereTime
            , JSONObject allImageTotalTime, Map<String,Map<String, Map<Long, List<Task>>>> allImageTasks, JSONObject recordNoOperation
            , String id_O, JSONArray objOrderList,JSONObject actionIdO,JSONObject allImageTeDate,JSONObject depAllTime);

    /**
     * 时间处理收尾（结束）方法 ( TimeZjServiceImplX.timeHandle()方法的，分割到该类 )
     * @param isJumpDay	控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param tasks	任务集合
     * @param grpB	组别
     * @param dep	部门
     * @param random	当前唯一编号
     * @param zon	任务余剩时间
     * @param isCanOnlyOnceTimeEmptyInsert	控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
     * @param hTeStart	任务最初始开始时间
     * @param task	当前处理任务信息
     * @param id_O	订单编号
     * @param index	订单下标
     * @param teDate	当前处理的任务的所在日期对象
     * @param timeConflictCopy	当前任务所在日期对象
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param isProblemState	存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
     * @param isSaveAppearEmpty	保存是否出现任务为空异常: = 0 正常操作未出现异常、 = 1 出现拿出任务为空异常镜像|数据库
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
     * @param isSetEnd 是否是列表最后任务
     * @param endTime 任务结束日期
     * @return long  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 handleTimeEnd getIsT
     */
    JSONObject timeHandleEnd(int isJumpDay, int isGetTaskPattern, List<Task> tasks, String grpB, String dep
            , String random, Long zon, int isCanOnlyOnceTimeEmptyInsert, Long hTeStart, Task task
            , String id_O, int index, JSONObject teDate, JSONObject timeConflictCopy, JSONObject sho
            , int isProblemState, int isSaveAppearEmpty, int csSta, String randomAll, JSONObject xbAndSbAll
            , JSONObject actionIdO, JSONObject objTaskAll, JSONObject recordId_OIndexState
            , JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks
            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp
            , JSONObject onlyRefState, JSONObject recordNoOperation,JSONObject clearStatus,JSONObject thisInfo
            ,JSONObject allImageTeDate,boolean isSetEnd,long endTime,JSONObject depAllTime);

    /**
     * 处理时间冲突收尾（结束）方法 ( TimeZjServiceTimeConflictImpl.handleTimeConflict()方法的，分割到该类 )
     * @param i	当前任务对应循环下标
     * @param tasks	任务集合
     * @param conflict	用于存储被冲突的任务集合
     * @param zon	当前任务余剩时间
     * @param random	当前唯一编号
     * @param dep	部门
     * @param grpB	组别
     * @param timeConflictCopy	当前任务所在日期
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
     * @param tePFinish 任务最后结束时间
     * @param clearStatus 清理状态信息
     * @param thisInfo 当前处理通用信息存储
     * @param allImageTeDate 镜像任务所在日期
     * @param isSetImage 是否写入镜像状态信息
     * @param endTime 任务结束日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 conflictHandleCore
     */
    JSONObject handleTimeConflictEnd(int i, List<Task> tasks, List<Task> conflict, Long zon, String random
            , String dep, String grpB, JSONObject timeConflictCopy, Integer isGetTaskPattern
            , Integer getCurrentTimeStampPattern, JSONObject sho, int csSta, String randomAll, JSONObject xbAndSbAll
            , JSONObject actionIdO, JSONObject objTaskAll, JSONObject recordId_OIndexState
            , JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks
            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp
            , JSONObject onlyRefState, JSONObject recordNoOperation,Long tePFinish
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,JSONObject isSetImage,long endTime
            ,JSONObject depAllTime);
}
