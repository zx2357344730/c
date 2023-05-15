package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.TimeZjServiceComprehensive;
import com.cresign.action.utils.TaskObj;
import com.cresign.tools.common.Constants;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.chkin.Task;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description 时间处理综合和分割代码类(综合方法、任务最后处理方法、时间处理收尾方法、处理时间冲突收尾方法)
 * @author tang
 * @ClassName TimeZjServiceComprehensiveImpl
 * @Date 2022/10/25
 * @ver 1.0.0
 */
@Service
public class TimeZjServiceComprehensiveImpl extends TimeZj implements TimeZjServiceComprehensive {

    int xz = 1;

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
    @Override
    public JSONObject timeComprehensiveHandle(String id_oNext, Integer indONext, JSONObject timeConflictCopy
            , Integer isGetTaskPattern, Integer isEmptyInsert, JSONObject sho, String id_C
            , int csSta, String randomAll, JSONObject xbAndSbAll, JSONObject actionIdO
            , JSONObject objTaskAll, JSONObject recordId_OIndexState, JSONObject storageTaskWhereTime
            , JSONObject allImageTotalTime, Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks
            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp
            , JSONObject onlyRefState, JSONObject recordNoOperation,Long tePFinish
            ,JSONObject id_OAndIndexTaskInfo,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,long endTime) {
        System.out.println("csSta:"+csSta+" - id_oNext:"+id_oNext+" - indONext:"+indONext);
        // 创建返回结果对象
        JSONObject result = new JSONObject();
        // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
        int isProblemState = 0;
        // 判断当前处理订单编号为空
        if (null == id_oNext) {
            // 设置问题为订单编号为空
            isProblemState = 1;
            // 添加返回信息
            result.put("lei",0L);
            result.put("isProblemState",isProblemState);
            return result;
        }
        // 根据订单编号获取当前产品冲突信息
        JSONObject id_OConflictInfo = sho.getJSONObject(id_oNext);
//        System.out.println(JSON.toJSONString(jsonObject2));
        // 根据订单编号对应的下标获取当前产品冲突信息
        JSONObject indexConflictInfo = id_OConflictInfo.getJSONObject(indONext.toString());
//        System.out.println(JSON.toJSONString(indexConflictInfo));
        // 判断产品状态不为当前递归产品
        if (indexConflictInfo.getInteger("prodState") != -1) {
            // 获取主产品信息
            String mainProd = indexConflictInfo.getString("z");
            // 将主产品信息进行拆分
            String[] split = mainProd.split("\\+");
            // 获取主产品下标并判断下标为0
            if (split[1].equals("0")) {
                // 设置下标为1
                split[1] = "1";
            }
            // 判断主产品订单编号等于当前订单编号，并且订单下标等于当前订单下标
            if (split[0].equals(id_oNext) && split[1].equals(indONext.toString())) {
                // 设置不是空插处理
                isEmptyInsert = 0;
//                System.out.println("重置所有镜像时间信息:");
                // 重置所有任务所在日期
                storageTaskWhereTime = new JSONObject();
            } else {
                // 设置是空插处理
                isEmptyInsert = 1;
            }
        }
        // 获取订单递归信息
        JSONArray objActionNext = actionIdO.getJSONArray(id_oNext);
        if (null == objActionNext) {
//            System.out.println("为空-获取数据库:"+id_oNext);
            // 根据当前订单编号获取订单信息
            Order orderNext = coupaUtil.getOrderByListKey(id_oNext, Collections.singletonList("action"));
            // 获取订单递归信息
            objActionNext = orderNext.getAction().getJSONArray("objAction");
            actionIdO.put(id_oNext,objActionNext);
        }
        // 根据当前订单下标获取订单递归信息
        JSONObject dgInfoNext = objActionNext.getJSONObject(indONext);
        // 判断订单递归状态等于5（主部件）
        if (dgInfoNext.getInteger(Constants.GET_BM_D_PT) == Constants.INT_FIVE
//                || xz == 0
//                || TimeZj.isZ <= 0
//                || true
        ) {
//            TimeZj.isZ--;
            System.out.println("-是-主生产部件停止-");
            // 设置问题为主生产部件
            isProblemState = 2;
            // 添加返回信息
            result.put("lei",0L);
            result.put("isProblemState",isProblemState);
            return result;
        }
//        xz--;
        // 获取当前订单部门
        String depNext = dgInfoNext.getString("dep");
        // 获取当前订单组别
        String grpBNext = dgInfoNext.getString("grpB");
        // 定义存储当前递归信息的下一个产品订单编号或父产品订单编号
        String id_ONextOrFather;
        // 定义存储当前递归信息的下一个产品订单下标或父产品订单下标
        int indexNextOrFather;
        // 调用获取任务所在日期
        List<String> taskTime = getTaskWhereDate(id_oNext,indONext,storageTaskWhereTime);
        // 判断任务所在日期为空
        if (taskTime.size() == 0) {
            // 获取当前递归信息的任务所在日期
            JSONObject teDateNext = dgInfoNext.getJSONObject("teDate");
//            System.out.println("teDateNext:");
//            System.out.println(JSON.toJSONString(teDateNext));
            // 获取任务所在日期键
            Set<String> strings = teDateNext.keySet();
            // 转换集合
            taskTime = new ArrayList<>(strings);
        }
        // 将任务所在日期键进行升序排序
        taskTime.sort(Comparator.naturalOrder());

        JSONObject id_OInfo = id_OAndIndexTaskInfo.getJSONObject(id_oNext);
        // 设置任务信息为空
        Task task = JSONObject.parseObject(JSON.toJSONString(id_OInfo.getJSONObject(indONext+"")),Task.class);
        // 保存是否出现任务为空异常:isTaskEmpty = 0 正常操作未出现异常、isYx = 1 出现拿出任务为空异常镜像|数据库
        int isTaskEmpty = 0;
        // 判断订单信息不为空
        if (null != task) {
            task.setTePStart(tePFinish);
            task.setTePFinish(tePFinish+task.getTeDurTotal());
            // 获取唯一下标
            String random = new ObjectId().toString();
            // 添加唯一编号状态
//            onlyRefState.put(random,1);
            onlyRefState.put(random,0);
            onlyRefState.put(random+"_new",0);
            // 根据键获取当前时间戳
            long teS = Long.parseLong(taskTime.get(0));
            // 添加唯一标识的当前时间戳
            onlyFirstTimeStamp.put(random,teS);
            // 调用写入当前时间戳
            setTeS(random,grpBNext,depNext,teS,newestLastCurrentTimestamp);
            // 创建当前递归任务所在日期对象
            JSONObject teDate = new JSONObject();
//            System.out.println("这里这里-sho:");
//            System.out.println(JSON.toJSONString(sho));
            System.out.println("nextTask: --进入前输出:"+teS +" - endTime:"+endTime);
            System.out.println(JSON.toJSONString(task));
            // 调用时间处理方法
            JSONObject timeHandleInfo = timeZjService.timeHandle(task, endTime, grpBNext, depNext, id_oNext, indONext, 0, random
                    , 1, teDate, timeConflictCopy, 1, sho, isTaskEmpty, csSta
//                    ,true
                    , randomAll, xbAndSbAll, actionIdO, objTaskAll, recordId_OIndexState, storageTaskWhereTime
                    , allImageTotalTime
                    , allImageTasks, onlyFirstTimeStamp, newestLastCurrentTimestamp, onlyRefState, recordNoOperation
                    , clearStatus, thisInfo, allImageTeDate,true);
            // 根据当前唯一标识删除信息
            onlyRefState.remove(random);
            newestLastCurrentTimestamp.remove(random);
            onlyFirstTimeStamp.remove(random);
//            System.out.println("timeComprehensiveHandle-结束:");
            // 定义存储当前递归信息的下一个产品订单编号或父产品订单编号
            JSONArray array = dgInfoNext.getJSONArray("prtNext");
            if (null == array || array.size() == 0) {
                array = dgInfoNext.getJSONArray("upPrnts");
            }
            for (int next = 0; next < array.size(); next++) {
                JSONObject obj = array.getJSONObject(next);
                id_ONextOrFather = obj.getString("id_O");
                indexNextOrFather = obj.getInteger("index");
                // 调用添加或更新产品状态方法
                addSho(sho,id_oNext,indONext.toString(),id_ONextOrFather
                        ,(indexNextOrFather+""),0);
                System.out.println("timeComprehensiveHandle - 1 :无需:"+task.getTePFinish() +" - "+timeHandleInfo.getLong("hTeStart"));
                System.out.println(JSON.toJSONString(task));
                long tePFin;
//                if (task.getTePFinish() == timeHandleInfo.getLong("hTeStart")) {
//                    tePFin = task.getTePFinish();
//                } else {
                    tePFin = timeHandleInfo.getLong("hTeStart");
//                }
                // 调用获取冲突处理方法，原方法
                JSONObject timeComprehensiveHandleInfo = timeComprehensiveHandle(id_ONextOrFather
                        , indexNextOrFather, timeConflictCopy, 0, 1
                        ,sho,task.getId_C(),csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,recordId_OIndexState
                        ,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
                        ,onlyRefState,recordNoOperation,tePFin
                        ,id_OAndIndexTaskInfo,clearStatus,thisInfo,allImageTeDate,timeHandleInfo.getLong("endTime"));

                // 赋值问题存储
                isProblemState = timeComprehensiveHandleInfo.getInteger("isProblemState");
            }
        }
        // 添加返回信息
        result.put("isProblemState",isProblemState);
        result.put("allImageTasks",allImageTasks);
        return result;
    }

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
    @Override
    public void taskLastHandle(JSONObject timeConflictCopy, String id_C, String randomAll
            , JSONObject objTaskAll, JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String,Map<String, Map<Long, List<Task>>>> allImageTasks
            , JSONObject recordNoOperation, String id_O, JSONArray objOrderList,JSONObject actionIdO,JSONObject allImageTeDate){
        System.out.println();
        System.out.println("！！！-最后输出这里-！！！:");
        System.out.println();
        System.out.println("输出--是否有未操作到的情况被处理--");
        JSONArray jsonArray = recordNoOperation.getJSONArray(randomAll);
        if (null != jsonArray) {
            System.out.println(jsonArray.size()==0?"无情况":"有这种情况");
            for (Object o : jsonArray) {
                System.out.println(o);
            }
        } else {
            System.out.println("无情况");
        }
        System.out.println("订单-开始");

//        if (true) {
//            // 遍历递归订单列表
//            for (int i = 0; i < objOrderList.size(); i++) {
//                // 获取递归订单编号
//                String id_OSon = objOrderList.getString(i);
////                System.out.println("id_OSon:");
////                System.out.println(id_OSon);
//                // 根据订单编号获取递归列表信息
//                JSONArray objAction = actionIdO.getJSONArray(id_OSon);
//                // 创建请求更改参数
//                JSONObject mapKey = new JSONObject();
//                // 添加请求更改参数信息
//                mapKey.put("action.objAction",objAction);
//                // 调用接口发起数据库更改信息请求
//                coupaUtil.updateOrderByListKeyVal(id_OSon,mapKey);
//            }
//            System.out.println("allImageTeDate-zui:");
//            System.out.println(JSON.toJSONString(allImageTeDate));
//            // 获取时间冲突副本的所有键（订单id）
//            Set<String> allImageTeDateAllKey = allImageTeDate.keySet();
//            // 遍历键（订单id）
//            allImageTeDateAllKey.forEach(id_OConflict -> {
//                // 获取键（订单id）对应的值
//                JSONObject id_OValInfo = allImageTeDate.getJSONObject(id_OConflict);
//                // 根据键获取订单信息
//                Order order = coupaUtil.getOrderByListKey(id_OConflict, Collections.singletonList("action"));
//                // 获取action卡片信息
//                JSONObject action = order.getAction();
//                // 获取订单的所有action递归信息
//                JSONArray objAction = action.getJSONArray("objAction");
//                // 获取对应键（订单id）的所有键（index）
//                Set<String> indexValInfo = id_OValInfo.keySet();
//                // 遍历键（index）
//                indexValInfo.forEach(indexConflict -> {
//                    // 根据键（index）获取递归所在时间
//                    JSONObject timeInfo = id_OValInfo.getJSONObject(indexConflict).getJSONObject("date");
//                    setOrder(id_OConflict,objAction,indexConflict,timeInfo,action,order);
//                });
//            });
//            System.out.println("timeConflictCopy-zui:");
//            System.out.println(JSON.toJSONString(timeConflictCopy));
//            // 获取时间冲突副本的所有键（订单id）
//            Set<String> timeConflictCopyAllKey = timeConflictCopy.keySet();
//            // 遍历键（订单id）
//            timeConflictCopyAllKey.forEach(id_OConflict -> {
//                // 获取键（订单id）对应的值
//                JSONObject id_OValInfo = timeConflictCopy.getJSONObject(id_OConflict);
//                // 根据键获取订单信息
//                Order order = coupaUtil.getOrderByListKey(id_OConflict, Collections.singletonList("action"));
//                // 获取action卡片信息
//                JSONObject action = order.getAction();
//                // 获取订单的所有action递归信息
//                JSONArray objAction = action.getJSONArray("objAction");
//                // 获取对应键（订单id）的所有键（index）
//                Set<String> indexValInfo = id_OValInfo.keySet();
//                // 遍历键（index）
//                indexValInfo.forEach(indexConflict -> {
//                    // 根据键（index）获取递归所在时间
//                    JSONObject timeInfo = id_OValInfo.getJSONObject(indexConflict);
//                    setOrder(id_OConflict,objAction,indexConflict,timeInfo,action,order);
//
////                // 根据键（index）获取递归信息
////                JSONObject actionInfo = objAction.getJSONObject(Integer.parseInt(indexConflict));
////                // 更新递归信息的所在时间
////                actionInfo.put("teDate",timeInfo);
////                // 更新递归信息
////                objAction.set(Integer.parseInt(indexConflict),actionInfo);
////                action.put("objAction",objAction);
//////                order.setAction(action);
//////                coupaUtil.saveOrder(order);
////
////                // 创建请求更改参数
////                JSONObject mapKey = new JSONObject();
////                // 添加请求更改参数信息
////                mapKey.put("action",action);
////                qt.setMDContent(id_OConflict,mapKey,Order.class);
//                });
//            });
//            System.out.println("storageTaskWhereTime-zui:");
//            System.out.println(JSON.toJSONString(storageTaskWhereTime));
////            // 获取所有键（订单id）
////            Set<String> storageTaskWhereTimeAllKey = storageTaskWhereTime.keySet();
////            // 遍历键（订单id）
////            for (String id_OConflict : storageTaskWhereTimeAllKey) {
//////                System.out.println("订单编号:");
//////                System.out.println(id_OConflict);
////                // 根据键（订单id）获取信息
////                JSONObject id_OValInfo = storageTaskWhereTime.getJSONObject(id_OConflict);
////                // 获取键（index）
////                Set<String> indexValInfo = id_OValInfo.keySet();
////                // 遍历键（index）
////                for (String indexConflict : indexValInfo) {
////                    // 根据键（index）获取任务所在时间对象
////                    JSONObject timeInfo = id_OValInfo.getJSONObject(indexConflict);
////                    // 根据键（订单id）获取订单信息
////                    Order order = coupaUtil.getOrderByListKey(id_OConflict, Collections.singletonList("action"));
//////                    System.out.println(id_OConflict);
//////                    System.out.println(JSON.toJSONString(order));
////                    // 获取订单action卡片信息
////                    JSONObject action = order.getAction();
////                    // 获取所有递归信息
////                    JSONArray objAction = action.getJSONArray("objAction");
////                    setOrder(id_OConflict,objAction,indexConflict,timeInfo,action,order);
////                }
////            }
//        }

        System.out.println("订单-结束");
        // 部门，组别，当天
        // 获取统一random对应的random唯一id，再根据唯一id获取任务信息镜像，再获取所有键（部门）
        Set<String> allImageTasksAllKey = allImageTasks.keySet();
        // 遍历键（部门）
        allImageTasksAllKey.forEach(dep -> {
            // 根据键（部门）获取组别对应的任务信息
            Map<String, Map<Long, List<Task>>> grpBTaskInfo = allImageTasks.get(dep);
            // 根据键（部门）获取组别任务当天余剩时间
            JSONObject sameDayRemainingTime = allImageTotalTime.getJSONObject(dep);
            // 遍历键（组别）
            grpBTaskInfo.keySet().forEach(grpB->{
                // 根据键（组别）获取当前任务信息
                Map<Long, List<Task>> sameDayTaskInfo = grpBTaskInfo.get(grpB);
                // 根据键（组别）获取任务当天余剩时间
                JSONObject taskSameDayRemainingTime = sameDayRemainingTime.getJSONObject(grpB);
                // 遍历键（当前）
                sameDayTaskInfo.keySet().forEach(date->{
                    // 根据键（当天）获取当天任务余剩时间
                    Long zon = taskSameDayRemainingTime.getLong(date+"");
                    // 获取当前任务信息
                    List<Task> tasks = sameDayTaskInfo.get(date);
                    // 调用写入任务到全局任务信息方法
                    setTasksAndZon(tasks,grpB,dep,date,zon,objTaskAll);
                });
            });
        });
        // 创建主订单的时间处理所在日期存储信息
        JSONObject timeRecord = new JSONObject();
        // 遍历全局任务信息的部门信息
        objTaskAll.keySet().forEach(dep -> {
            // 获取全局任务信息的部门信息
            JSONObject depOverallTask = objTaskAll.getJSONObject(dep);
            // 遍历全局任务信息的组别信息
            depOverallTask.keySet().forEach(grpB -> {
                // 获取全局任务信息的组别信息
                JSONObject grpBOverallTask = depOverallTask.getJSONObject(grpB);
                // 遍历全局任务信息的时间戳信息
                grpBOverallTask.keySet().forEach(teS -> {
                    // 获取全局任务信息的时间戳信息
                    JSONObject teSOverallTask = grpBOverallTask.getJSONObject(teS);
                    // 获取任务余剩总时间
                    long zon = teSOverallTask.getLong("zon");
                    // 创建存储任务列表
                    List<Task> tasks = new ArrayList<>();
                    // 创建存储要清理的任务下标信息
                    List<Integer> needClearTaskIndex = new ArrayList<>();
                    // 存储任务列表是否全是系统任务
                    boolean isTaskYesSystem = false;
                    // 存储任务是否属于当前主订单
                    boolean isBelongCurrentMainOrder = false;
                    // 获取全局任务信息的任务列表
                    JSONArray overallTasks = teSOverallTask.getJSONArray("tasks");
                    // 遍历全局任务列表
                    for (int i = 0; i < overallTasks.size(); i++) {
                        // 获取并且转换任务信息
                        Task task = JSONObject.parseObject(JSON.toJSONString(overallTasks.getJSONObject(i)), Task.class);
//                        // 判断任务为空任务
//                        if (task.getPriority() != -1 && task.getTeDurTotal() == 0 && task.getTeDelayDate() == 0) {
//                            // 添加删除下标
//                            needClearTaskIndex.add(i);
//                        }
                        // 判断属于系统任务
                        if (task.getPriority() != -1 && !isTaskYesSystem) {
                            // 遍历当前主订单的订单列表
                            for (int j = 0; j < objOrderList.size(); j++) {
                                // 判断当前任务属于当前主订单列表
                                if (task.getId_O().equals(objOrderList.getString(j))){
                                    // 等于说明属于
                                    isBelongCurrentMainOrder = true;
                                    break;
                                }
                            }
                            // 设置为不是全部都是系统任务
                            isTaskYesSystem = true;
                        }
                        // 添加任务信息
                        tasks.add(task);
                    }
//                    // 判断删除任务下标不为空
//                    if (needClearTaskIndex.size() > 0) {
//                        // 降序排序循环存储下标
//                        needClearTaskIndex.sort(Comparator.reverseOrder());
//                        // 遍历删除任务列表对应的任务
//                        for (int clearIndex : needClearTaskIndex) {
//                            tasks.remove(clearIndex);
//                        }
//                    }

                    Task taskEqually = null;
                    boolean isMerge = false;
                    boolean isChange = false;
                    String id_OExternal = "";
                    int indexExternal = -1;
                    int dateIndex = -1;
                    int oneIndex = -1;
                    boolean isChangeNew = false;
//                    System.out.println("最终结果合并:");
//                    System.out.println(JSON.toJSONString(tasks));
                    List<Integer> removeIndex = new ArrayList<>();
                    for (int i = 0; i < tasks.size(); i++) {
                        Task taskThis = tasks.get(i);
                        if (taskThis.getPriority() == -1) {
                            if (isMerge) {
                                tasks.set(oneIndex,taskEqually);
                                isMerge = false;
                            }
                            if (isChange) {
                                id_OExternal = "";
                                indexExternal = -1;
                                dateIndex = -1;
                                oneIndex = -1;
                                isChange = false;
                            }
                            continue;
                        }
                        String id_OThis = taskThis.getId_O();
                        int indexThis = taskThis.getIndex();
                        int dateIndexThis = taskThis.getDateIndex();
                        if (id_OExternal.equals(id_OThis) && indexExternal == indexThis
                                && dateIndex == dateIndexThis) {
                            if (!isMerge) {
                                isMerge = true;
                                taskEqually = tasks.get(oneIndex);
                            }
                            removeIndex.add(i);
                            taskEqually.setTeDurTotal(taskEqually.getTeDurTotal()+taskThis.getTeDurTotal());
                            taskEqually.setTePFinish(taskEqually.getTePFinish()+taskThis.getTeDurTotal());
//                            if (taskThis.getTaOver()!=0){
//                                System.out.println("taOver-h:");
//                                System.out.println(taskThis.getTaOver());
//                            }
//                            if (taskEqually.getTaOver() == 0) {
                                taskEqually.setTaOver(taskThis.getTaOver());
//                            }
                            if (!isChangeNew) {
                                isChangeNew = true;
                            }
                        } else {
                            if (isMerge) {
                                tasks.set(oneIndex,taskEqually);
                                isMerge = false;
                            }
                            isChange = true;
                            id_OExternal = id_OThis;
                            indexExternal = indexThis;
                            dateIndex = dateIndexThis;
                            oneIndex = i;
                        }
                    }
//                    for (int index : removeIndex) {
//                        tasks.remove(index);
//                    }
                    for (int i = removeIndex.size()-1; i >= 0; i--) {
                        int index = removeIndex.get(i);
                        tasks.remove(index);
                    }
//                    System.out.println(isChangeNew?"有改动":"无");
//                    System.out.println(JSON.toJSONString(tasks));

                    // 调用写入任务到全局任务信息方法
                    setTasksAndZon(tasks,grpB,dep,Long.parseLong(teS),zon,objTaskAll);
                    // 判断不是全部都是系统任务，并且属于当前主订单
                    if (isTaskYesSystem && isBelongCurrentMainOrder) {
                        // 获取主订单的时间处理所在日期存储信息的部门信息
                        JSONObject timeDep = timeRecord.getJSONObject(dep);
                        if (null == timeDep) {
                            timeDep = new JSONObject();
                        }
                        // 获取主订单的时间处理所在日期存储信息的组别信息
                        JSONObject timeGrpB = timeDep.getJSONObject(grpB);
                        if (null == timeGrpB) {
                            timeGrpB = new JSONObject();
                        }
                        // 获取主订单的时间处理所在日期存储信息的时间戳信息
                        Long timeTeS = timeGrpB.getLong(teS + "");
                        if (null == timeTeS) {
                            timeGrpB.put(teS+"",1);
                        }
                        // 添加当前时间戳
                        timeDep.put(grpB,timeGrpB);
                        timeRecord.put(dep,timeDep);
                    }
                });
            });
        });
        // 根据公司编号获取asset编号
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        // 根据asset编号获取asset的时间处理卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList(timeCard));
        // 获取时间处理卡片信息
//        JSONObject aArrange = asset.getAArrange2();
        JSONObject aArrange = getAArrangeNew(asset);
        System.out.println("处理的objTaskAll:");
        System.out.println(JSON.toJSONString(objTaskAll));
        JSONObject objTask;
        if (null == aArrange) {
            aArrange = new JSONObject();
            objTask = objTaskAll;
        } else {
            objTask = aArrange.getJSONObject("objTask");
            objTaskAll.keySet().forEach(dep -> {
                // 获取全局任务信息的部门信息
                JSONObject depOverallTask = objTaskAll.getJSONObject(dep);
                // 遍历全局任务信息的组别信息
                depOverallTask.keySet().forEach(grpB -> {
                    // 获取全局任务信息的组别信息
                    JSONObject grpBOverallTask = depOverallTask.getJSONObject(grpB);
                    // 遍历全局任务信息的时间戳信息
                    grpBOverallTask.keySet().forEach(teS -> {
                        // 获取全局任务信息的时间戳信息
                        JSONObject teSOverallTask = grpBOverallTask.getJSONObject(teS);
                        JSONObject depTime = objTask.getJSONObject(dep);
                        if (null == depTime) {
                            objTask.put(dep,depOverallTask);
                        } else {
                            JSONObject grpBTime = depTime.getJSONObject(grpB);
                            if (null == grpBTime) {
                                depTime.put(grpB,grpBOverallTask);
                                objTask.put(dep,depTime);
                            } else {
                                grpBTime.put(teS,teSOverallTask);
                                depTime.put(grpB,grpBTime);
                                objTask.put(dep,depTime);
                            }
                        }
                    });
                });
            });
        }
        System.out.println("旧objTask:");
        System.out.println(JSON.toJSONString(objTask));
        // 添加信息
        aArrange.put("objTask",objTask);
        aArrange.put("operationState",0);

        System.out.println("最终写入数据库-开始:");

//        if (true) {
//            // 创建请求参数存储字典
//            JSONObject mapKey = new JSONObject();
//            // 添加请求参数
//            mapKey.put(timeCard,aArrange);
////        mapKey.put("aArrange2",aArrange);
//            coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
//
//            System.out.println("timeRecord:");
//            System.out.println(JSON.toJSONString(timeRecord));
//            // 创建请求更改参数
//            mapKey = new JSONObject();
//            // 添加请求更改参数信息
//            mapKey.put("action.timeRecord",timeRecord);
////        if (!isTest) {
////            mapKey.put("action.oDates",new JSONArray());
////            mapKey.put("action.oTasks",new JSONArray());
////        }
//            // 调用接口发起数据库更改信息请求
//            coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
//
//            asset = coupaUtil.getAssetById(assetId, Collections.singletonList(timeCard));
////            aArrange = asset.getAArrange2();
//            aArrange = getAArrangeNew(asset);
//
//            System.out.println();
//            System.out.println("排序前-Tasks:");
//            System.out.println(JSON.toJSONString(aArrange.getJSONObject("objTask")));
//            System.out.println();
//        }

        System.out.println("最终写入数据库-结束");

        // 判断是否有出息强制停止
        if (null != isQzTz.getInteger(randomAll) && isQzTz.getInteger(randomAll) == 1) {
            System.out.println("-----出现强制停止-----");
            System.out.println();
        }
    }

    /**
     * 写入数据库订单方法
     * @param id_OConflict	订单编号
     * @param objAction	所有递归信息
     * @param indexConflict	下标
     * @param timeInfo	当天时间戳
     * @param action	订单action卡片信息
     * @param order	订单信息
     * @return 返回结果:
     * @author tang
     * @date 创建时间: 2023/2/9
     * @ver 版本号: 1.0.0
     */
    public void setOrder(String id_OConflict,JSONArray objAction,String indexConflict,JSONObject timeInfo
            ,JSONObject action,Order order){
//        System.out.println("进入写入订单方法-zui:");
//        System.out.println(id);
//        System.out.println(id_OConflict);
//        System.out.println(JSON.toJSONString(objAction));
//        System.out.println(JSON.toJSONString(indexConflict));
//        System.out.println(JSON.toJSONString(timeInfo));
//        System.out.println(timeInfo.keySet().size());
//        System.out.println(JSON.toJSONString(action));
//        System.out.println(JSON.toJSONString(order));
        int index = Integer.parseInt(indexConflict);
        JSONObject objActionSon = objAction.getJSONObject(index);
        objActionSon.put("teDate",timeInfo);
//        qt.setMDContent(id_OConflict,);
        JSONObject orderKey = new JSONObject();
        orderKey.put("action.objAction."+index, objActionSon);
        qt.setMDContent(id_OConflict,orderKey,Order.class);
    }

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
    public JSONObject timeHandleEnd(int isJumpDay, int isGetTaskPattern, List<Task> tasks, String grpB, String dep
            , String random, Long zon, int isCanOnlyOnceTimeEmptyInsert, Long hTeStart, Task task
            , String id_O, int index, JSONObject teDate, JSONObject timeConflictCopy, JSONObject sho
            , int isProblemState, int isSaveAppearEmpty, int csSta, String randomAll, JSONObject xbAndSbAll
            , JSONObject actionIdO, JSONObject objTaskAll, JSONObject recordId_OIndexState
            , JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks
            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp
            , JSONObject onlyRefState, JSONObject recordNoOperation,JSONObject clearStatus,JSONObject thisInfo
            ,JSONObject allImageTeDate,boolean isSetEnd,long endTime) {
//        System.out.println("进入收尾:");
        JSONObject result = new JSONObject();
        // 控制是否跳天参数：isJumpDay == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
        if (isJumpDay == 0) {
//            System.out.println("进入收尾跳天:");
            // isGetTaskPattern = 0 获取数据库任务信息、 = 1 获取镜像任务信息
            if (isGetTaskPattern == 0) {
//                System.out.println("进入收尾跳天-1:");
                setTasksAndZon(tasks,grpB,dep,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),zon,objTaskAll);
            } else {
//                System.out.println("进入收尾跳天-2:");
                // 调用写入镜像任务集合方法
                setImageTasks(tasks,grpB,dep,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),allImageTasks);
                // 调用写入镜像任务余剩时间方法
                setImageZon(zon,grpB,dep,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),allImageTotalTime);
            }
            // 根据当前唯一编号获取最新的（最后一个）当前时间戳
            JSONObject rand = newestLastCurrentTimestamp.getJSONObject(random);
            // 根据部门获取最新的（最后一个）当前时间戳
            JSONObject onlyDep = rand.getJSONObject(dep);
            // 根据组别获取最新的（最后一个）当前时间戳
            Long aLong = onlyDep.getLong(grpB);
            // 根据部门添加最新的（最后一个）当前时间戳
            onlyDep.put(grpB,(aLong+86400L));
            // 根据部门添加最新的（最后一个）当前时间戳
            rand.put(dep,onlyDep);
            // 根据当前唯一编号添加最新的（最后一个）当前时间戳
            newestLastCurrentTimestamp.put(random,rand);
            // 判断跳天强制停止参数等于55
            if (yiShu.getInteger(randomAll) == 555) {
                System.out.println("进入强制停止-----1");
                // 赋值强制停止出现后的记录参数
                isQzTz.put(randomAll,1);
                result.put("hTeStart",hTeStart);
                result.put("endTime",0);
                return result;
            }
            // 调用时间处理方法
            JSONObject timeHandleInfo = timeZjService.timeHandle(task, hTeStart, grpB, dep, id_O, index, 1
                    , random, isCanOnlyOnceTimeEmptyInsert, teDate, timeConflictCopy
                    , isGetTaskPattern, sho,isSaveAppearEmpty,csSta
//                    ,false
                    ,randomAll,xbAndSbAll,actionIdO,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
                    ,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate,false);
            // 获取任务最初始开始时间
            hTeStart = timeHandleInfo.getLong("hTeStart");
            result.put("hTeStart",hTeStart);
            result.put("endTime",timeHandleInfo.getLong("endTime"));
        } else {
//            System.out.println("进入收尾停止跳天:");
            // isGetTaskPattern –  = 0 获取数据库任务信息、 = 1 获取镜像任务信息
            if (isGetTaskPattern == 0) {
                setTasksAndZon(tasks,grpB,dep,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),zon,objTaskAll);
            } else {
                // isProblemState – 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                if (isProblemState != 2) {
//                    System.out.println("进入写入镜像这个地方了:"+isSetEnd+" - "+isGetTaskPattern);
                    if (isSetEnd) {
                        // 调用写入镜像任务集合方法
                        setImageTasks(tasks,grpB,dep,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),allImageTasks);
                        // 调用写入镜像任务余剩时间方法
                        setImageZon(zon,grpB,dep,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),allImageTotalTime);
                    }
                }
            }
            result.put("endTime",endTime);
        }
        result.put("hTeStart",hTeStart);
        return result;
    }

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
    public JSONObject handleTimeConflictEnd(int i, List<Task> tasks, List<Task> conflict, Long zon, String random
            , String dep, String grpB, JSONObject timeConflictCopy, Integer isGetTaskPattern
            , Integer getCurrentTimeStampPattern, JSONObject sho, int csSta, String randomAll, JSONObject xbAndSbAll
            , JSONObject actionIdO, JSONObject objTaskAll, JSONObject recordId_OIndexState
            , JSONObject storageTaskWhereTime, JSONObject allImageTotalTime
            , Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks
            , JSONObject onlyFirstTimeStamp, JSONObject newestLastCurrentTimestamp
            , JSONObject onlyRefState, JSONObject recordNoOperation,Long tePFinish
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,JSONObject isSetImage,long endTime) {
        System.out.println("进入-handleTimeConflictEnd:"+zon);
        // 相同任务存储字典
        Map<String,Map<String,Task>> confTask = new HashMap<>();
        // 相同任务存储下标字典
        Map<String,Map<String,Integer>> confIndex = new HashMap<>();
        // 需要更新存储字典
        Map<String,String> updateConf = new HashMap<>();
        // 需要删除集合
        List<Integer> removeConf = new ArrayList<>();
        // 遍历冲突的任务集合
        for (int n = 0; n < conflict.size(); n++) {
            // 获取循环当前任务
            Task task = conflict.get(n);
            // 根据订单编号获取相同任务信息
            Map<String,Task> taskGetId_O = confTask.get(task.getId_O());
            // 判断为空
            if (null == taskGetId_O) {
                // 创建相同任务信息
                taskGetId_O = new HashMap<>();
                taskGetId_O.put(task.getIndex()+"",task);
                confTask.put(task.getId_O(),taskGetId_O);
                // 创建记录信息
                Map<String, Integer> indexGetId_O = new HashMap<>();
                indexGetId_O.put(task.getIndex()+"",n);
                confIndex.put(task.getId_O(),indexGetId_O);
            } else {
                // 不为空，获取下标任务信息
                Task taskNew = taskGetId_O.get(task.getIndex() + "");
                // 获取记录下标
                Map<String, Integer> indexGetId_O = confIndex.get(task.getId_O());
                // 判断下标任务信息为空
                if (null == taskNew) {
                    // 创建记录信息
                    taskGetId_O.put(task.getIndex() + "",task);
                    confTask.put(task.getId_O(),taskGetId_O);

                    indexGetId_O.put(task.getIndex() + "",n);
                    confIndex.put(task.getId_O(),indexGetId_O);
                } else {
                    // 不为空，添加信息
                    taskNew.setTeDurTotal(taskNew.getTeDurTotal()+task.getTeDurTotal());
                    String updateId_O = updateConf.get(task.getId_O());
                    taskGetId_O.put(taskNew.getIndex() + "",taskNew);
                    confTask.put(taskNew.getId_O(),taskGetId_O);
                    removeConf.add(n);
                    // 判断更新状态为空
                    if (null == updateId_O) {
                        // 添加信息
                        updateConf.put(task.getId_O(),task.getIndex()+"");
                    }
                }
            }
        }
        // 遍历更新的下标
        for (String o : updateConf.keySet()) {
            // 根据订单编号获取下标
            String ind = updateConf.get(o);
            // 获取冲突下标信息
            Map<String, Integer> indexGetId_O = confIndex.get(o);
            // 获取冲突下标
            int index = indexGetId_O.get(ind);
            // 获取冲突任务信息
            Map<String, Task> taskGetId_O = confTask.get(o);
            // 获取任务信息
            Task task = taskGetId_O.get(ind);
            // 更新任务
            conflict.set(index,task);
        }
        // 遍历需要删除集合
        for (int r = removeConf.size()-1; r >= 0; r--) {
            // 获取删除下标
            int indexNewThis = removeConf.get(r);
            // 删除
            conflict.remove(indexNewThis);
        }
        // 创建删除集合
        removeConf = new ArrayList<>();
        // 创建重复记录字典
        Map<String,Map<String,Integer>> record = new HashMap<>();
        // 遍历冲突任务集合
        for (int n = 0; n < conflict.size(); n++) {
            // 获取任务信息
            Task task = conflict.get(n);
            // 获取订单编号记录信息
            Map<String,Integer> id_OInfo = record.get(task.getId_O());
            // 判断为空
            if (null == id_OInfo) {
                // 创建并添加记录信息
                Map<String,Integer> id_OInfoNew = new HashMap<>();
                id_OInfoNew.put(task.getIndex()+"",n);
                record.put(task.getId_O(),id_OInfoNew);
            } else {
                // 不为空，遍历记录信息所有键
                for (String indexStr : id_OInfo.keySet()) {
                    // 根据键获取下标
                    int id_OInfoInt = Integer.parseInt(indexStr);
                    // 判断下标小于当前
                    if (id_OInfoInt < task.getIndex()) {
                        // 添加外层任务到删除集合
                        removeConf.add(n);
                    } else {
                        // 添加当前任务到删除集合
                        removeConf.add(id_OInfo.get(indexStr));
                        Map<String,Integer> id_OInfoNew = new HashMap<>();
                        id_OInfoNew.put(task.getIndex()+"",n);
                        record.put(task.getId_O(),id_OInfoNew);
                    }
                    break;
                }
            }
        }
        for (int r = removeConf.size()-1; r >= 0; r--) {
            int indexNewThis = removeConf.get(r);
            conflict.remove(indexNewThis);
        }
        System.out.println("被冲突的任务-最后清理后:");
        System.out.println(JSON.toJSONString(conflict));

        // 创建返回结果对象
        JSONObject result = new JSONObject();
        // 创建存储被冲突的任务当前处理下标
        int conflictInd = 0;
        // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
        int isProblemState = 0;
        boolean isEndList = false;
        // 判断当前任务下标加1小于总任务集合数量
        if ((i + 1) < tasks.size()) {
            // 判断被冲突的任务当前处理下标等于最后一个或大于最后一个
            if (conflictInd >= conflict.size()) {
//                System.out.println("这里退出-");
                // 添加返回结果
                result.put("zon",zon);
                result.put("isProblemState",isProblemState);
                return result;
            }
            // 根据被冲突的任务当前处理下标获取被冲突任务信息
            Task conflictTask = conflict.get(conflictInd);
            // 深度复制被冲突任务信息
            Task conflictTaskCopy = TaskObj.getTaskY(conflictTask);
            System.out.println("conflictTaskCopy:"+zon);
            System.out.println(JSON.toJSONString(conflictTaskCopy));
//            System.out.println("当前任务清理-前:"+zon);
//            System.out.println(JSON.toJSONString(tasks));
            // 获取被清理的任务信息
            JSONObject id_OAndIndexTaskInfo = clearOldTask(conflictTaskCopy.getId_O()
                    , conflictTaskCopy.getDateIndex(), conflictTaskCopy.getId_C(),objTaskAll
                    ,clearStatus,allImageTasks,allImageTotalTime
                    ,allImageTeDate,tasks,isSetImage);
            // 重置一天时间
            zon = 86400L;
            // 计算余剩时间
            for (Task task : tasks) {
                zon -= task.getTeDurTotal();
            }
            System.out.println("当前任务清理-后:"+zon);
            System.out.println(JSON.toJSONString(tasks));

            // 调用获取被冲突任务所在日期
            List<String> taskWhereDateList = getTaskWhereDate(conflictTaskCopy.getId_O()
                    , conflictTaskCopy.getIndex(),storageTaskWhereTime);
            // 获取递归信息
            JSONArray objAction = actionIdO.getJSONArray(conflictTaskCopy.getId_O());
            if (null == objAction) {
//                System.out.println("为空-获取数据库-2:"+taskX.getId_O());
                // 根据被冲突任务订单编号获取订单信息
                Order order = coupaUtil.getOrderByListKey(conflictTaskCopy.getId_O(), Collections.singletonList("action"));
                // 获取递归信息
                objAction = order.getAction().getJSONArray("objAction");
                actionIdO.put(conflictTaskCopy.getId_O(),objAction);
            }
            // 根据被冲突的任务订单下标获取递归信息
            JSONObject dgInfo = objAction.getJSONObject(conflictTaskCopy.getIndex());
            JSONArray prtNext = dgInfo.getJSONArray("prtNext");
            if (null != prtNext && prtNext.size() > 0) {
                JSONObject nextInfo = prtNext.getJSONObject(0);
                // 调用添加或更新产品状态方法
                addSho(sho,conflictTaskCopy.getId_O(),conflictTaskCopy.getIndex().toString()
                        ,nextInfo.getString("id_O"), nextInfo.getInteger("index").toString(),1);
            }
            // 创建任务所在时间对象
            JSONObject teDateTask = new JSONObject();
            // 被冲突任务所在日期为空
            if (taskWhereDateList.size() == 0) {
                // 获取递归信息里的任务所在时间
                JSONObject teDateTaskNew = dgInfo.getJSONObject("teDate");
                // 获取任务所在时间键
                Set<String> teDateTaskKey = teDateTaskNew.keySet();
                // 转换键为集合类型存储
                taskWhereDateList = new ArrayList<>(teDateTaskKey);
            }
//            System.out.println("排序前:");
//            System.out.println(JSON.toJSONString(teDateYKeyZ));
            // 判断任务所在时间键大于1
            if (taskWhereDateList.size() > 1) {
                // 对任务所在时间键进行排序
                Collections.sort(taskWhereDateList);
//                System.out.println("排序后:");
//                System.out.println(JSON.toJSONString(teDateYKeyZ));
            }
            // 获取任务所在时间键的第一个键的值（时间戳）
            long taskTimeKeyFirstVal = Long.parseLong(taskWhereDateList.get(0));
            // 调用写入刚获取的时间戳
            setTeS(random , dgInfo.getString("grpB"), dgInfo.getString("dep"),taskTimeKeyFirstVal,newestLastCurrentTimestamp);
            // 创建订单下标存储任务所在时间对象
            JSONObject teDaTaskNew = new JSONObject();
            // 添加任务所在时间对象
            teDaTaskNew.put(conflictTaskCopy.getIndex()+"",teDateTask);
            // 添加订单下标存储任务所在时间对象
            timeConflictCopy.put(conflictTaskCopy.getId_O(),teDaTaskNew);
            // 获取产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
            Integer prodState = sho.getJSONObject(conflictTaskCopy.getId_O()).getJSONObject(conflictTaskCopy.getIndex().toString()).getInteger("prodState");
            if (prodState != -1) {
                // 调用获取获取统一id_O和index存储记录状态信息方法
                Integer storage = getStorage(conflictTaskCopy.getId_O(), conflictTaskCopy.getIndex(),recordId_OIndexState);
                // storage == 0 正常状态存储、storage == 1 冲突状态存储、storage == 2 调用时间处理状态存储
                if (storage == 0) {
                    // 调用写入存储记录状态方法
                    setStorage(1, conflictTaskCopy.getId_O(), conflictTaskCopy.getIndex(),recordId_OIndexState);
                }
            }
            // isTimeStopState == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
            int isTimeStopState = 0;
            // 遍历任务下标+1以后的任务集合
            for (int j = (i+1); j < tasks.size(); j++) {
                // 判断被冲突任务到了最后一个
                if (conflictInd >= conflict.size()) {
                    // 结束循环
                    break;
                }
                // 获取第一个任务信息
                Task taskOne = tasks.get(j);
                // 判断当前任务下标加1小于总任务集合
                if ((j + 1) < tasks.size()) {
                    // 获取第二个任务信息
                    Task taskTwo = tasks.get(j+1);
                    isEndList = (j+1) == tasks.size()-1;
//                    System.out.println("task1X-2X:");
//                    System.out.println(JSON.toJSONString(task1X));
//                    System.out.println(JSON.toJSONString(task2X));
                    // 调用空插冲突处理方法
                    JSONObject emptyInsertAndEmptyInsertConflictHandleInfo
                            = timeZjServiceEmptyInsert.emptyInsertAndEmptyInsertConflictHandle(conflictTaskCopy, taskOne
                            , taskTwo, tasks, j, conflictInd, zon, conflict,teDateTask,random,dep,grpB,objAction
                            ,dgInfo,taskTimeKeyFirstVal,timeConflictCopy
                            ,isGetTaskPattern
//                            ,1
                            ,sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                            ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                            ,clearStatus,thisInfo,allImageTeDate,id_OAndIndexTaskInfo,false);
//                    System.out.println("进入这里调用----:-h");
                    JSONObject currentHandleTask = emptyInsertAndEmptyInsertConflictHandleInfo.getJSONObject("currentHandleTask");
                    if (null != currentHandleTask) {
                        conflictTaskCopy = JSONObject.parseObject(JSON.toJSONString(currentHandleTask),Task.class);
                    }
                    // 获取任务余剩时间
                    zon = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("zon");
                    // 更新冲突任务集合下标为冲突下标（conflictInd）指定的冲突任务
                    conflict.set(conflictInd, TaskObj.getTaskWr(conflictTaskCopy));
                    // 获取冲突下标
                    conflictInd = emptyInsertAndEmptyInsertConflictHandleInfo.getInteger("conflictInd");
                    // 获取任务所在时间键的第一个键的值（时间戳）
                    taskTimeKeyFirstVal = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("taskTimeKeyFirstVal");
                    tePFinish = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("tePFinish");
                    endTime = emptyInsertAndEmptyInsertConflictHandleInfo.getLong("endTime");
                    // 判断冲突下标小于冲突集合长度
                    if (conflictInd < conflict.size()) {
                        // 获取当前任务订单编号
                        String id_O = conflictTaskCopy.getId_O();
                        // 获取当前任务订单下标
                        int index = conflictTaskCopy.getIndex();
                        // 根据冲突下标获取冲突任务信息
                        Task conflictTaskData = conflict.get(conflictInd);
                        // 深度复制冲突任务信息
                        Task conflictTaskDataCopy = TaskObj.getTaskY(conflictTaskData);
                        // 判断当前任务订单编号不等于冲突任务订单编号，或者当前任务订单下标不等于冲突任务订单下标
                        if (!id_O.equals(conflictTaskDataCopy.getId_O()) || index != conflictTaskDataCopy.getIndex()) {
                            // 根据当前任务订单编号获取任务所在日期
                            JSONObject id_OTaskTime = timeConflictCopy.getJSONObject(conflictTaskCopy.getId_O());
                            // 根据当前任务订单下标添加任务所在日期
                            id_OTaskTime.put(conflictTaskCopy.getIndex()+"",teDateTask);
                            // 根据当前任务订单编号添加任务所在日期
                            timeConflictCopy.put(conflictTaskCopy.getId_O(),id_OTaskTime);
                            // 获取清理状态方法
                            int clearStatusThis = getClearStatus(conflictTaskCopy.getId_O()
                                    , (conflictTaskCopy.getDateIndex()+1), clearStatus);
                            // 判断清理状态为0
                            if (clearStatusThis == 0) {
                                // 写入清理状态方法
                                setClearStatus(conflictTaskCopy.getId_O()
                                        , (conflictTaskCopy.getDateIndex()+1),clearStatus,1);
                                // 获取下一个零件列表
                                JSONArray prtNextNew = dgInfo.getJSONArray("prtNext");
                                System.out.println("进入清理-1");
                                // 判断下一个零件列表不为空
                                if (null != prtNextNew && prtNextNew.size() > 0) {
                                    // 遍历零件列表
                                    for (int next = 0; next < prtNextNew.size(); next++) {
                                        // 获取零件信息
                                        JSONObject nextInfoNew = prtNextNew.getJSONObject(next);
                                        System.out.println("timeComprehensiveHandle - 2 :清理状态:"+clearStatusThis);
                                        // 定义时间戳
                                        long teS = tasks.get(0).getTePStart();
                                        // 写入镜像任务集合方法
                                        setImageTasks(tasks,grpB,dep,teS,allImageTasks);
                                        setImageZon(zon,grpB,dep,teS,allImageTotalTime);
                                        // 调用获取冲突处理方法，原方法
                                        JSONObject timeComprehensiveHandleInfo
                                                = timeComprehensiveHandle(nextInfoNew.getString("id_O")
                                                , nextInfoNew.getInteger("index"), timeConflictCopy, isGetTaskPattern
                                                , 0, sho,conflictTaskCopy.getId_C(),csSta,randomAll,xbAndSbAll
                                                ,actionIdO,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                                                ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                ,tePFinish,id_OAndIndexTaskInfo,clearStatus,thisInfo,allImageTeDate,endTime);

                                        // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                        isProblemState = timeComprehensiveHandleInfo.getInteger("isProblemState");
                                        // 创建任务所在时间对象
                                        teDateTask = new JSONObject();
                                        // 判断当前任务订单编号不等于冲突任务订单编号
                                        if (!id_O.equals(conflictTaskDataCopy.getId_O())) {
                                            // 获取所有递归信息
                                            objAction = actionIdO.getJSONArray(conflictTaskDataCopy.getId_O());
                                            if (null == objAction) {
//                                                System.out.println("为空-获取数据库-3:"+conflictTaskDataCopy.getId_O());
                                                // 根据冲突任务订单编号获取订单信息
                                                Order order = coupaUtil.getOrderByListKey(conflictTaskDataCopy.getId_O(), Collections.singletonList("action"));
                                                // 获取所有递归信息
                                                objAction = order.getAction().getJSONArray("objAction");
                                                actionIdO.put(conflictTaskDataCopy.getId_O(),objAction);
                                            }
                                        }
                                        // 根据冲突任务订单下标获取递归信息
                                        dgInfo = objAction.getJSONObject(conflictTaskDataCopy.getIndex());
                                        // 获取任务所在日期对象
                                        JSONObject teDateTaskNew = dgInfo.getJSONObject("teDate");
                                        // 获取任务所在日期键集合
                                        Set<String> teDateTaskKey = teDateTaskNew.keySet();
                                        // 转换任务所在日期键集合
                                        List<String> teDateTaskKeyNew = new ArrayList<>(teDateTaskKey);
                                        // 获取任务所在时间键的第一个键的值（时间戳）
                                        taskTimeKeyFirstVal = Long.parseLong(teDateTaskKeyNew.get(0));
                                        // 调用写入当前时间戳方法
                                        setTeS(random , dgInfo.getString("grpB"), dgInfo.getString("dep"),taskTimeKeyFirstVal,newestLastCurrentTimestamp);
                                        // 创建订单下标存储任务所在时间对象
                                        teDaTaskNew = new JSONObject();
                                        // 添加订单下标存储任务所在时间对象
                                        teDaTaskNew.put(conflictTaskDataCopy.getIndex()+"",teDateTask);
                                        // 添加订单编号存储任务所在时间对象
                                        timeConflictCopy.put(conflictTaskDataCopy.getId_O(),teDaTaskNew);
                                    }
                                }
                            }
                        }
                        // 根据冲突任务下标获取冲突任务信息
                        conflictTaskCopy = conflict.get(conflictInd);
//                        System.out.println("换taskX:"+zon);
                    } else {
                        // 根据冲突任务订单编号获取任务所在日期对象
                        JSONObject jsonObject = timeConflictCopy.getJSONObject(conflictTaskCopy.getId_O());
                        // 根据冲突任务订单下标添加任务所在日期对象
                        jsonObject.put(conflictTaskCopy.getIndex()+"",teDateTask);
                        // 根据冲突任务订单编号添加任务所在日期对象
                        timeConflictCopy.put(conflictTaskCopy.getId_O(),jsonObject);
                        System.out.println("conflictTaskCopy:");
                        System.out.println(JSON.toJSONString(conflictTaskCopy));
                        // 更新当前信息ref方法
                        String thisInfoRef = getThisInfoRef(thisInfo);
                        // 获取清理状态方法
                        int clearStatusThis = getClearStatus(conflictTaskCopy.getId_O()
                                , (conflictTaskCopy.getDateIndex()+1), clearStatus);
//                        System.out.println("获取的状态:"+clearStatusThis +" -- "+thisInfoRef);
                        if ("time".equals(thisInfoRef)
                                || clearStatusThis == 0
                        ) {
                            // 写入清理状态方法
                            setClearStatus(conflictTaskCopy.getId_O()
                                    , (conflictTaskCopy.getDateIndex()+1),clearStatus,1);
                            // 获取下一个零件信息列表
                            JSONArray prtNextNew = dgInfo.getJSONArray("prtNext");
                            System.out.println("进入清理-2 | timeComprehensiveHandle - 3 :"+clearStatusThis);
                            // 判断不为空
                            if (null != prtNextNew && prtNextNew.size() > 0) {
                                // 遍历零件列表
                                for (int next = 0; next < prtNextNew.size(); next++) {
//                                    System.out.println("timeComprehensiveHandle - 3 :");
                                    JSONObject nextInfoNew = prtNextNew.getJSONObject(next);
                                    // 定义时间戳
                                    long teS = tasks.get(0).getTePStart();
                                    setImageTasks(tasks,grpB,dep,teS,allImageTasks);
                                    setImageZon(zon,grpB,dep,teS,allImageTotalTime);
                                    // 调用获取冲突处理方法，原方法
                                    JSONObject timeComprehensiveHandleInfo
                                            = timeComprehensiveHandle(nextInfoNew.getString("id_O")
                                            , nextInfoNew.getInteger("index"), timeConflictCopy, isGetTaskPattern
                                            , 0, sho,conflictTaskCopy.getId_C(),csSta,randomAll,xbAndSbAll
                                            ,actionIdO,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                                            ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,tePFinish
                                            ,id_OAndIndexTaskInfo,clearStatus,thisInfo,allImageTeDate,endTime);

                                    result.put("isSetEnd",false);
//                                    System.out.println("timeComprehensiveHandle-h:");
                                    // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isProblemState = timeComprehensiveHandleInfo.getInteger("isProblemState");
                                }
                            }
                        }
                    }
                    // isTimeStopState == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
                    isTimeStopState = emptyInsertAndEmptyInsertConflictHandleInfo.getInteger("isTimeStopState");
                    // 空插冲突强制停止参数累加
                    leiW.put(randomAll,(leiW.getInteger(randomAll)+1));
                    // 判断空插强制停止参数等于60
                    if (leiW.getInteger(randomAll) == 560) {
//                        System.out.println("----进入强制停止空差冲突方法-1----");
                        // 强制停止出现后的记录参数赋值等于1
                        isQzTz.put(randomAll,1);
                        // 停止方法
                        break;
                    }
                }
            }
            // 定义时间戳
            long teS;
            // getCurrentTimeStampPattern = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
            if (getCurrentTimeStampPattern == 1) {
                // 根据当前唯一编号获取最新的（最后一个）当前时间戳
                JSONObject rand = newestLastCurrentTimestamp.getJSONObject(random);
                // 根据部门获取获取最新的（最后一个）当前时间戳
                JSONObject onlyDep = rand.getJSONObject(dep);
                // 根据组别获取获取最新的（最后一个）当前时间戳
                teS = onlyDep.getLong(grpB);
//                System.out.println("获取最新的:");
            } else {
                // 根据当前唯一编号获取存储当前唯一编号的第一个当前时间戳
                teS = onlyFirstTimeStamp.getLong(random);
//                System.out.println("获取当前第一次初始时间戳:");
            }
            System.out.println("空插方法-q: - isSetEnd:"+result.getBoolean("isSetEnd")+" - isGetTaskPattern:"+isGetTaskPattern+" - isEndList:"+isEndList);
            // isGetTaskPattern = 0 获取数据库任务信息、 = 1 获取镜像任务信息
            if (isGetTaskPattern == 1 && (null == result.getBoolean("isSetEnd"))
                    || (null != result.getBoolean("isSetEnd") && result.getBoolean("isSetEnd"))
            || isEndList && isGetTaskPattern != 1) {
                Task taskNew = tasks.get(0);
                Long tePStart = taskNew.getTePStart();
                if (tePStart != teS) {
                    teS = tePStart;
                }
                System.out.println("进入这里写入镜像任务的列表:");
                // 调用写入镜像任务集合方法
                setImageTasks(tasks,grpB,dep,teS,allImageTasks);
                // 调用写入镜像任务余剩时间方法
                setImageZon(zon,grpB,dep,teS,allImageTotalTime);
            }
//            System.out.println("空插方法-h:");
            // 调用空插方法，获取存储问题状态
            isProblemState = timeZjServiceEmptyInsert.emptyInsertHandle(tasks, conflictInd, conflict, conflictTaskCopy, zon, grpB
                    , dep, random, isTimeStopState, teS, 0, teDateTask, objAction, dgInfo, taskTimeKeyFirstVal
                    , timeConflictCopy, isGetTaskPattern, sho, isProblemState,csSta,randomAll,xbAndSbAll,actionIdO
                    ,objTaskAll,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus,thisInfo
                    ,allImageTeDate,id_OAndIndexTaskInfo,false,true);
        }
        // 添加返回结果
        result.put("zon",zon);
        result.put("isProblemState",isProblemState);
        return result;
    }
}
