package com.cresign.action.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.pojo.po.chkin.Task;

import java.util.List;
import java.util.Map;

/**
 * @ClassName TimeZjServiceEmptyInsert
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/10/11
 * @Version 1.0.0
 */
public interface TimeZjServiceEmptyInsert {

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
     * @param objAction	所有递归信息
     * @param dgInfo	递归信息
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
    int emptyInsertHandle(List<Task> tasks, int conflictInd, List<Task> conflict, Task currentHandleTask, Long zon, String grpB
            , String dep, String random, int isTimeStopState, Long teS, int emptyInsertHandlePattern, JSONObject teDate, JSONArray objAction
            , JSONObject dgInfo, Long taskTimeKeyFirstVal, JSONObject timeConflictCopy, Integer isGetTaskPattern, JSONObject sho
            , int isProblemState, int csSta, String randomAll, JSONObject xbAndSbAll, JSONObject actionIdO
            , JSONObject objTaskAll, JSONObject recordId_OIndexState, JSONObject storageTaskWhereTime
            , JSONObject allImageTotalTime, Map<String,Map<String, Map<Long,List<Task>>>> allImageTasks
            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState
            , JSONObject recordNoOperation,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate
            ,JSONObject id_OAndIndexTaskInfo,boolean isInside,boolean isSetTasks,JSONObject depAllTime);

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
     * @param objAction	所有递归信息
     * @param actZ	递归信息
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
    JSONObject emptyInsertAndEmptyInsertConflictHandle(Task currentHandleTask,Task contrastTaskOne,Task contrastTaskTwo,List<Task> tasks,int i
            ,int conflictInd,long zon,List<Task> conflict,JSONObject teDate,String random,String dep,String grpB
            ,JSONArray objAction,JSONObject actZ,Long taskTimeKeyFirstVal,JSONObject timeConflictCopy
            ,Integer isGetTaskPattern,JSONObject sho,int csSta,String randomAll
            ,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll
            ,JSONObject recordId_OIndexState,JSONObject storageTaskWhereTime,JSONObject allImageTotalTime
            ,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks,JSONObject onlyFirstTimeStamp
            ,JSONObject newestLastCurrentTimestamp,JSONObject onlyRefState,JSONObject recordNoOperation
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate
            ,JSONObject id_OAndIndexTaskInfo,boolean isSetTasks,JSONObject depAllTime);

    /**
     * 根据主订单和对应公司编号，删除时间处理信息
     * @param id_O	主订单编号
     * @param id_C	公司编号
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    ApiResponse removeTime(String id_O, String id_C);

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
    JSONObject calculationEmptyInsertTime(Task task, Task task1, Task task2, List<Task> tasks
            , Integer i, Long zon, Integer isControlCalculationMode,String random,JSONObject teDate,String dep
            ,String grpB,int prodState,JSONObject storageTaskWhereTime,JSONObject onlyFirstTimeStamp
            ,JSONObject newestLastCurrentTimestamp,Integer isGetTaskPattern,JSONObject allImageTeDate,JSONObject onlyRefState);
}
