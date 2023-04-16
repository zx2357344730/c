package com.cresign.action.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.pojo.po.chkin.Task;

import java.util.List;
import java.util.Map;

/**
 * @ClassName TimeService
 * @Description 作者很懒什么也没写
 * @author tang
 * @Date 2021/12/20 11:09
 * @ver 1.0.0
 */
public interface TimeZjService {

    /**
     * 移动资产内aArrange卡片
     * @param id_C	当前公司编号
     * @param moveId_A	移动的资产编号
     * @param coverMoveId_A	被移动的资产编号
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2022/10/30
     * @ver 版本号: 1.0.0
     */
    ApiResponse moveAArrange(String id_C,String moveId_A,String coverMoveId_A);

    /**
     * 获取物料预计开始时间
     * @param id_O	订单编号
     * @param id_C	公司编号
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/17
     * @ver 版本号: 1.0.0
     */
    ApiResponse getEstimateStartTime(String id_O, String id_C, Long teStart);

    /**
     * 初始化时间处理方法
     * @param id_O  主订单编号
     * @param teStart   开始时间
     * @param id_C  公司编号
     * @param wn0TPrior 优先级
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 1:26
     */
    ApiResponse getAtFirst(String id_O, Long teStart, String id_C, Integer wn0TPrior);

    /**
     * 根据主订单和对应公司编号，删除时间处理信息
     * @param id_O	主订单编号
     * @param id_C	公司编号
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    ApiResponse removeTime(String id_O,String id_C);

    /**
     * 任务实际结束时间处理接口
     * @param dep	部门
     * @param grpB	组别
     * @param currentTime	处理时间
     * @param index	任务列表对应下标
     * @param id_C	公司编号
     * @param taPFinish	任务实际结束时间
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    ApiResponse timeSortFromNew(String dep, String grpB, long currentTime, int index, String id_C,long taPFinish);

    /**
     * 删除或者新增aArrange卡片信息
     * @param id_C	公司编号
     * @param object	操作信息
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    ApiResponse delOrAddAArrange(String id_C,JSONObject object);

    /**
     * 根据订单编号与下标获取剩余数量的预计完成时间
     * @param id_O	订单编号
     * @param index	下标
     * @param number	人数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2022/11/6
     * @ver 版本号: 1.0.0
     */
    ApiResponse timeCalculation(String id_O, int index,int number);

    /**
     * 时间处理方法
     * @param task	当前处理任务信息
     * @param hTeStart	任务最初始开始时间
     * @param grpB	组别
     * @param dep	部门
     * @param id_O	订单编号
     * @param index	订单下标
     * @param isJumpDays	保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
     * @param random	当前唯一编号
     * @param isCanOnlyOnceTimeEmptyInsert	控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
     * @param teDate	当前处理的任务的所在日期对象
     * @param timeConflictCopy	当前任务所在日期对象
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
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
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    JSONObject timeHandle(Task task, Long hTeStart, String grpB, String dep, String id_O, Integer index
            , Integer isJumpDays, String random, Integer isCanOnlyOnceTimeEmptyInsert, JSONObject teDate
            , JSONObject timeConflictCopy
            , Integer isGetTaskPattern, JSONObject sho, int isSaveAppearEmpty, int csSta
//            ,boolean isD
            , String randomAll, JSONObject xbAndSbAll, JSONObject actionIdO, JSONObject objTaskAll
            , JSONObject recordId_OIndexState, JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String,Map<String, Map<Long, List<Task>>>> allImageTasks, JSONObject onlyFirstTimeStamp
            , JSONObject newestLastCurrentTimestamp, JSONObject onlyRefState, JSONObject recordNoOperation
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,boolean isComprehensiveHandle);

}
