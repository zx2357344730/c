package com.cresign.action.service;

import com.alibaba.fastjson.JSONArray;
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
     * 多订单时间处理方法
     * @param id_C  公司编号
     * @param orderList 订单信息
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     */
    ApiResponse getAtFirstList(String id_C, JSONArray orderList);

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
    ApiResponse delOrAddAArrange(String id_C,String dep,JSONObject object);

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
     * 获取计算物料时间后的开始时间
     * @param id_O  订单编号
     * @param teStart   开始时间
     * @return  开始时间
     */
    ApiResponse getTeStart(String id_O, Long teStart);

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
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,boolean isComprehensiveHandle
            ,JSONObject depAllTime);

    /**
     * 获取上班时间戳与休息时间戳
     * @param arrTime   上班时间段
     * @param objSb 存储上班信息
     * @param objXb 存储休息信息
     * @return  上班时间戳与休息时间戳
     */
    long getArrTime(JSONArray arrTime,JSONArray objSb,JSONArray objXb);

    /**
     * 获取多订单一起计算物料时间后的开始时间
     * @param id_OArr  订单编号集合
     * @param teStart   开始时间
     * @return  开始时间
     */
    ApiResponse getTeStartTotal(JSONArray id_OArr, Long teStart);

    /**
     * 获取多订单分开计算物料时间后的开始时间
     * @param orderInfo  订单信息集合
     * @return  开始时间
     */
    ApiResponse getTeStartList(JSONArray orderInfo);

    ApiResponse updateODate();

    ApiResponse delExcessiveODateField();

    /**
     * 清理任务api
     * @param id_O  订单编号
     * @param dateIndex 要清理的起始下标
     * @param id_C  公司编号
     * @param layer 层级
     * @param id_PF 父编号
     * @return  清理结果
     */
    ApiResponse getClearOldTask(String id_O,int dateIndex,String id_C,String layer,String id_PF);

    ApiResponse setChKinUserCountByOrder(String id_O,String id_C);

//    ApiResponse delMaterial();

    ApiResponse getAtFirstEasy(String id_O, Long teStart, String id_C,boolean setNew);

    ApiResponse setAtFirstEasy(String id_O, Long teStart, String id_C
            ,int dateIndex,String layer,String id_PF);

    ApiResponse getAtFirstEasyList(String id_C, JSONArray getList);

    ApiResponse setAtFirstEasyList(String id_C, JSONArray setList);

    ApiResponse setOrderUserCount(String id_O, String id_C, Long teStart);

    ApiResponse clearOrderAllTaskAndSave(String id_C,String id_O);

    ApiResponse clearThisDayTaskAndSave(String id_C,String dep,String grpB,long thisDay);
    ApiResponse clearThisDayTaskAndSaveClearFollowUp(String id_C,String dep,String grpB,long thisDay);

    ApiResponse clearThisDayEasyTaskAndSave(String id_C,String dep,long thisDay);
    ApiResponse clearThisDayEasyTaskAndSaveNew(String id_C,String dep,long thisDay);
    ApiResponse clearThisDayEasyTaskAndSaveClearFollowUp(String id_C,String dep,long thisDay);

    ApiResponse setAtFirst(String id_O, Long teStart, String id_C, Integer wn0TPrior
            ,int dateIndex,String layer,String id_PF);

    ApiResponse setAtFirstList(String id_C, JSONArray setList);
}
