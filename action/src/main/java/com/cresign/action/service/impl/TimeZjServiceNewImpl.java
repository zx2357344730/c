package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.TimeZjServiceNew;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName TimeZjServiceNewImpl
 * @Date 2022/11/2
 * @ver 1.0.0
 */
@Service
public class TimeZjServiceNewImpl extends TimeZj implements TimeZjServiceNew {

    @Resource
    private RetResult retResult;

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
    @Override
    public ApiResponse
    timeSortFromNew(String dep, String grpB, long currentTime
            , int index, String id_C,long taPFinish) {
        TimeZj.isZ = 6;
        Asset asset = qt.getConfig(id_C,"d-"+dep,timeCard);
        // 判断asset为空
        if (null == asset) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_NULL.getCode(), "资产为空");
        }
        // 获取aArrange2卡片信息
        JSONObject aArrange = getAArrangeNew(asset);
        // 判断卡片为空
        if (null == aArrange) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_ARRANGE_NULL.getCode(), "资产Arrange为空");
        }
        // 获取任务处理状态
        Integer operationState = aArrange.getInteger("operationState");
        // 判断处理状态为空或者为0
        if (null == operationState || 0 == operationState) {
            // 调用方法更新aArrange卡片操作状态接口
//            updateArrangeState(1,assetId);
            updateArrangeState(1,asset.getId());
        } else {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_TASK_PROCESSING.getCode(), "资产时间正在处理中");
        }
        try {

//            // 获取所有任务信息
//            JSONObject objTask = aArrange.getJSONObject("objTask");
////            System.out.println("objTask:");
////            System.out.println(JSON.toJSONString(objTask));
////            // 获取指定部门任务信息
////            JSONObject depTime = objTask.getJSONObject(dep);
//            // 获取指定组别任务信息
//            JSONObject grpBTime = objTask.getJSONObject(grpB);
//            // 获取当前处理日期的任务信息
//            JSONObject objTime = grpBTime.getJSONObject(currentTime + "");
//            // 获取任务列表
//            JSONArray tasksJson = objTime.getJSONArray("tasks");
//            // 创建集合任务列表
//            List<Task> tasks = new ArrayList<>();
//            // 遍历任务列表并且转换为集合
//            tasksJson.forEach(taskJson -> tasks.add(JSONObject.parseObject(JSON.toJSONString(taskJson),Task.class)));
//            // 根据任务下标获取任务信息
//            Task taskUsed = tasks.get(index);
//            // 获取任务的预计结束时间
//            Long tePFinish = taskUsed.getTePFinish();
//            // 判断预计结束时间等于实际结束时间
//            if (tePFinish == taPFinish) {
//                // 调用方法更新aArrange卡片操作状态接口
////                updateArrangeState(0,assetId);
//                updateArrangeState(0,asset.getId());
//                // 抛出操作成功异常
//                return retResult.ok(CodeEnum.OK.getCode(), "无需处理!");
//            } else if (tePFinish > taPFinish) {
//                // 调用方法更新aArrange卡片操作状态接口
////                updateArrangeState(0,assetId);
//                updateArrangeState(0,asset.getId());
//                // 抛出操作成功异常
//                return retResult.ok(CodeEnum.OK.getCode(), "时间小于无需处理!");
//            } else {
//                if ((taPFinish-tePFinish)>50000) {
//                    // 调用方法更新aArrange卡片操作状态接口
////                    updateArrangeState(0,assetId);
//                    updateArrangeState(0,asset.getId());
//                    // 抛出操作成功异常
//                    return retResult.ok(CodeEnum.OK.getCode(), "当前超时时间超过50000不支持处理!");
//                }
//                // 调用方法获取订单信息
////                Order orderNew = coupaUtil.getOrderByListKey(
////                        taskUsed.getId_O(), Collections.singletonList("info"));
//                Order orderNew = qt.getMDContent(taskUsed.getId_O(),"info", Order.class);
//                // 判断订单为空或者订单info卡片信息为空或者订单info卡片内的id_OP字段为空
//                if (null == orderNew || null == orderNew.getInfo() || null == orderNew.getInfo().getId_OP()) {
//                    // 调用方法更新aArrange卡片操作状态接口
////                    updateArrangeState(0,assetId);
//                    updateArrangeState(0,asset.getId());
//                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
//                }
//                // 调用方法获取订单信息
////                Order salesOrderData = coupaUtil.getOrderByListKey(
////                        orderNew.getInfo().getId_OP(), Arrays.asList("oItem", "info", "view", "action", "casItemx"));
//                Order salesOrderData = qt.getMDContent(orderNew.getInfo().getId_OP(),qt.strList("oItem", "info", "view", "action", "casItemx"), Order.class);
////            System.out.println("--------");
////            System.out.println(JSON.toJSONString(salesOrderData));
//                // 判断订单是否为空
//                if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
//                        || null == salesOrderData.getCasItemx()) {
////                    updateArrangeState(0,assetId);
//                    updateArrangeState(0,asset.getId());
//                    // 返回为空错误信息
//                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
//                }
//                // 获取递归订单列表
//                JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
//                // 根据组别存储部门信息
//                JSONObject grpBGroupIdOJ = new JSONObject();
//                OrderInfo info = salesOrderData.getInfo();
////                // 获取职位人数信息
////                JSONObject objDepInfo = chkin00s.getJSONObject("objZw");
////                // 存储判断职位人数信息是否为空
////                boolean isDepNull = null != objDepInfo;
//                // 存储递归订单列表的订单编号集合
//                JSONArray objOrderList = new JSONArray();
//                // 存储部门对应组别的职位总人数
//                JSONObject grpUNumAll = new JSONObject();
////                // 获取打卡信息
////                JSONArray objData = chkin00s.getJSONArray("objData");
////                // 存储判断打卡信息是否为空
////                boolean isXbAndSbNull = true;
////                // 存储判断是否是否有时间处理打卡信息
////                boolean isTimeChKin = false;
//                // 定义存储时间处理打卡时间字典
//                JSONObject xbAndSb;
////                // 定义存储时间处理打卡信息下标
////                int chKinInfoIndex = -1;
////                // 判断打卡信息为空
////                if (null == objData) {
////                    isXbAndSbNull = false;
////                } else {
////                    // 遍历打卡信息
////                    for (int j = 0; j < objData.size(); j++) {
////                        // 根据j获取对应的打卡信息
////                        JSONObject chKinInfo = objData.getJSONObject(j);
////                        // 判断是否是时间处理打卡信息
////                        if (null != chKinInfo.getInteger("timeP")) {
////                            // 设置为是
////                            isTimeChKin = true;
////                            // 获取下标位置
////                            chKinInfoIndex = j;
////                            // 创建时间处理打卡信息存储
////                            xbAndSb = new JSONObject();
////                            // 获取时间处理打卡的上班和下班信息
////                            JSONObject objDataZ = objData.getJSONObject(j);
////                            // 添加信息
////                            xbAndSb.put("xb",objDataZ.getJSONArray("objXb"));
////                            xbAndSb.put("sb",objDataZ.getJSONArray("objSb"));
////                            break;
////                        }
////                    }
////                }
////                // 获取部门组别对应时间处理打卡信息下标字典
////                JSONObject objWorkTime = chkin00s.getJSONObject("objWorkTime");
////                // 判断为空
////                if (null == objWorkTime) {
////                    isXbAndSbNull = false;
////                }
//                // 存储部门对应组别的上班和下班时间
//                JSONObject xbAndSbAll = new JSONObject();
//                // 存储casItemx内订单列表的订单action数据
//                JSONObject actionIdO = new JSONObject();
//                // 清理状态
//                JSONObject clearStatus = new JSONObject();
////                // 遍历订单列表
////                for (int i = 0; i < objOrder.size(); i++) {
////                    // 获取订单列表的订单编号
////                    String id_OInside = objOrder.getJSONObject(i).getString("id_O");
////                    // 判断订单等于主订单，则通过循环
////                    if (id_OInside.equals(orderNew.getInfo().getId_OP())) {
////                        continue;
////                    }
////                    // 添加订单编号
////                    objOrderList.add(id_OInside);
////                    // 根据订单编号查询action卡片信息
//////                    Order insideAction = coupaUtil.getOrderByListKey(id_OInside, Collections.singletonList("action"));
////                    Order insideAction = qt.getMDContent(id_OInside,"action", Order.class);
////                    // 获取递归信息
////                    JSONArray objAction = insideAction.getAction().getJSONArray("objAction");
////                    // 获取组别对应部门信息
////                    JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
////                    // 遍历组别对应部门信息
////                    for (String grpBNew : grpBGroup.keySet()) {
////                        // 创建存储部门字典
////                        JSONObject depMap = new JSONObject();
////                        // 根据组别获取组别信息
////                        JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpBNew);
////                        // 获取组别的部门
////                        String depNew = grpBGroupInfo.getString("dep");
////                        // 判断职位人数不为空
////                        if (isDepNull) {
////                            // 根据部门获取职位人数部门信息
////                            JSONObject depInfo = objDepInfo.getJSONObject(depNew);
////                            // 判断不为空
////                            if (null != depInfo) {
////                                // 根据组别，获取职位人数部门对应的组别信息
////                                Integer grpBInfo = objDepInfo.getInteger(grpBNew);
////                                if (null != grpBInfo) {
////                                    // 根据部门，获取部门对应的全局职位人数信息
////                                    JSONObject depAllInfo = grpUNumAll.getJSONObject(depNew);
////                                    // 判断部门全局职位人数信息为空
////                                    if (null == depAllInfo) {
////                                        // 创建部门全局职位人数信息
////                                        depAllInfo = new JSONObject();
////                                        // 根据组别添加职位人数
////                                        depAllInfo.put(grpBNew,grpBInfo);
////                                        grpUNumAll.put(depNew,depAllInfo);
////                                    } else {
////                                        // 直接根据组别获取全局职位人数
////                                        Integer grpBAllInfo = depAllInfo.getInteger(grpBNew);
////                                        // 判断为空
////                                        if (null == grpBAllInfo) {
////                                            // 添加全局职位人数信息
////                                            depAllInfo.put(grpBNew,grpBAllInfo);
////                                            grpUNumAll.put(depNew,depAllInfo);
////                                        }
////                                    }
////                                }
////                            }
////                        }
////                        // 判断上班下班时间不为空，并且有时间处理打卡信息
////                        if (isXbAndSbNull && isTimeChKin) {
////                            // 根据部门获取上班下班信息
////                            JSONObject depChKin = objWorkTime.getJSONObject(depNew);
////                            if (null != depChKin) {
////                                // 根据组别获取上班下班信息
////                                Integer grpBChKin = depChKin.getInteger(grpBNew);
////                                // 判断上班下班信息不为空，并且，下标位置等于时间处理打卡信息的下标
////                                if (null != grpBChKin && grpBChKin == chKinInfoIndex) {
////                                    // 根据部门获取全局上班下班信息
////                                    JSONObject depAllChKin = xbAndSbAll.getJSONObject(depNew);
////                                    // 判断为空
////                                    if (null == depAllChKin) {
////                                        // 创建
////                                        depAllChKin = new JSONObject();
////                                        // 添加全局上班下班信息
////                                        depAllChKin.put(grpBNew,xbAndSb);
////                                        xbAndSbAll.put(depNew,depAllChKin);
////                                    } else {
////                                        // 根据组别获取全局上班下班信息
////                                        JSONObject grpBAllChKin = depAllChKin.getJSONObject(grpBNew);
////                                        if (null == grpBAllChKin) {
////                                            // 添加全局上班下班信息
////                                            depAllChKin.put(grpBNew,xbAndSb);
////                                            xbAndSbAll.put(depNew,depAllChKin);
////                                        }
////                                    }
////                                }
////                            }
////                        }
////                        // 添加部门信息
////                        depMap.put("dep",depNew);
////                        depMap.put("id_O",id_OInside);
////                        // 添加信息
////                        grpBGroupIdOJ.put(grpBNew,depMap);
////                    }
////                    // 根据订单编号添加订单信息存储
////                    actionIdO.put(id_OInside,objAction);
////                }
//                System.out.println();
//                Map<String,Asset> assetMap = new HashMap<>();
//                JSONObject depAllTime = new JSONObject();
//                // 当前处理信息
//                JSONObject thisInfo = new JSONObject();
//                // 遍历订单列表
//                for (int i = 0; i < objOrder.size(); i++) {
//                    // 获取订单列表的订单编号
//                    String id_OInside = objOrder.getJSONObject(i).getString("id_O");
//                    // 判断订单等于主订单，则通过循环
//                    if (id_OInside.equals(orderNew.getInfo().getId_OP())) {
//                        continue;
//                    }
//                    // 添加订单编号
//                    objOrderList.add(id_OInside);
//                    // 根据订单编号查询action卡片信息
////                    Order insideAction = coupaUtil.getOrderByListKey(id_OInside, Collections.singletonList("action"));
//                    Order insideAction = qt.getMDContent(id_OInside,"action", Order.class);
////                    // 获取递归信息
////                    JSONArray objAction = insideAction.getAction().getJSONArray("objAction");
//                    // 获取组别对应部门信息
//                    JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
//                    // 遍历组别对应部门信息
//                    for (String grpBNew : grpBGroup.keySet()) {
//                        // 创建存储部门字典
//                        JSONObject depMap = new JSONObject();
//                        // 根据组别获取组别信息
//                        JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpBNew);
//                        // 获取组别的部门
//                        String depNew = grpBGroupInfo.getString("dep");
//                        Asset assetDep;
//                        if (assetMap.containsKey(depNew)) {
//                            assetDep = assetMap.get(depNew);
//                        } else {
//                            assetDep = qt.getConfig(id_C,"d-"+depNew,"chkin");
//                            assetMap.put(depNew,assetDep);
//                        }
//                        JSONObject chkGrpB;
//                        if (null == assetDep || null == assetDep.getChkin() || null == assetDep.getChkin().getJSONObject("objChkin")) {
//                            chkGrpB = TaskObj.getChkinJava();
//                        } else {
//                            JSONObject chkin = assetDep.getChkin();
//                            JSONObject objChkin = chkin.getJSONObject("objChkin");
////                            JSONObject chkDep = objChkin.getJSONObject(dep);
//                            chkGrpB = objChkin.getJSONObject(grpB);
//                        }
//                        JSONArray arrTime = chkGrpB.getJSONArray("arrTime");
//                        JSONArray objSb = new JSONArray();
//                        JSONArray objXb = new JSONArray();
//                        // 用于保存上一个时间段的结束时间
////                        long belowTimeData = 0;
////                        int priority = 0;
////                        long allTime = 0;
//                        long allTime = timeZjService.getArrTime(arrTime,objSb,objXb);
////                        for (int j = 0; j < arrTime.size(); j+=2) {
////                            String timeUpper = arrTime.getString(j);
////                            String timeBelow = arrTime.getString(j+1);
////                            String[] splitUpper = timeUpper.split(":");
////                            String[] splitBelow = timeBelow.split(":");
////                            int upper = Integer.parseInt(splitUpper[0]);
////                            int upperDivide = Integer.parseInt(splitUpper[1]);
////                            int below = Integer.parseInt(splitBelow[0]);
////                            int belowDivide = Integer.parseInt(splitBelow[1]);
////                            long upperTime = (((long) upper * 60) * 60)+((long) upperDivide * 60);
////                            long belowTime = (((long) below * 60) * 60)+((long) belowDivide * 60);
////                            JSONObject objSbZ = new JSONObject();
////                            objSbZ.put("priority",priority);
////                            priority++;
////                            objSbZ.put("tePStart",upperTime);
////                            objSbZ.put("tePFinish",belowTime);
////                            objSbZ.put("zon",belowTime-upperTime);
////                            allTime+=(belowTime-upperTime);
////                            objSb.add(objSbZ);
////                            if (j == 0) {
////                                if (upperTime != 0) {
////                                    JSONObject objXbZ = new JSONObject();
////                                    objXbZ.put("priority",-1);
////                                    objXbZ.put("tePStart",0);
////                                    objXbZ.put("tePFinish",upperTime);
////                                    objXbZ.put("zon",upperTime);
////                                    objXb.add(objXbZ);
////                                }
////                            } else if ((j + 1) + 1 >= arrTime.size()) {
////                                JSONObject objXbZ = new JSONObject();
////                                objXbZ.put("priority",-1);
////                                objXbZ.put("tePStart",belowTimeData);
////                                objXbZ.put("tePFinish",upperTime);
////                                objXbZ.put("zon",upperTime-belowTimeData);
////                                objXb.add(objXbZ);
////                                if (belowTime != 86400) {
////                                    objXbZ = new JSONObject();
////                                    objXbZ.put("priority",-1);
////                                    objXbZ.put("tePStart",belowTime);
////                                    objXbZ.put("tePFinish",86400);
////                                    objXbZ.put("zon",86400-belowTime);
////                                    objXb.add(objXbZ);
////                                }
////                            } else {
////                                JSONObject objXbZ = new JSONObject();
////                                objXbZ.put("priority",-1);
////                                objXbZ.put("tePStart",belowTimeData);
////                                objXbZ.put("tePFinish",upperTime);
////                                objXbZ.put("zon",upperTime-belowTimeData);
////                                objXb.add(objXbZ);
////                            }
////                            belowTimeData = belowTime;
////                        }
//                        if (!depAllTime.containsKey(dep)) {
//                            depAllTime.put(dep,allTime);
//                        }
//                        // 创建时间处理打卡信息存储
//                        xbAndSb = new JSONObject();
//                        // 添加信息
//                        xbAndSb.put("xb",objXb);
//                        xbAndSb.put("sb",objSb);
//
//                        // 根据部门，获取部门对应的全局职位人数信息
//                        JSONObject depAllInfo = grpUNumAll.getJSONObject(depNew);
//                        // 判断部门全局职位人数信息为空
//                        if (null == depAllInfo) {
//                            // 创建部门全局职位人数信息
//                            depAllInfo = new JSONObject();
//                            // 根据组别添加职位人数
//                            depAllInfo.put(grpBNew,1);
//                            grpUNumAll.put(depNew,depAllInfo);
//                        } else {
//                            // 直接根据组别获取全局职位人数
//                            Integer grpBAllInfo = depAllInfo.getInteger(grpBNew);
//                            // 判断为空
//                            if (null == grpBAllInfo) {
//                                // 添加全局职位人数信息
//                                depAllInfo.put(grpBNew,grpBAllInfo);
//                                grpUNumAll.put(depNew,depAllInfo);
//                            }
//                        }
//
//                        // 根据部门获取全局上班下班信息
//                        JSONObject depAllChKin = xbAndSbAll.getJSONObject(depNew);
//                        // 判断为空
//                        if (null == depAllChKin) {
//                            // 创建
//                            depAllChKin = new JSONObject();
//                            // 添加全局上班下班信息
//                            depAllChKin.put(grpBNew,xbAndSb);
//                            xbAndSbAll.put(depNew,depAllChKin);
//                        } else {
//                            // 根据组别获取全局上班下班信息
//                            JSONObject grpBAllChKin = depAllChKin.getJSONObject(grpBNew);
//                            if (null == grpBAllChKin) {
//                                // 添加全局上班下班信息
//                                depAllChKin.put(grpBNew,xbAndSb);
//                                xbAndSbAll.put(depNew,depAllChKin);
//                            }
//                        }
//
//                        // 添加部门信息
//                        depMap.put("dep",depNew);
//                        depMap.put("id_O",id_OInside);
//                        // 添加信息
//                        grpBGroupIdOJ.put(grpBNew,depMap);
//                    }
//                    // 根据订单编号添加订单信息存储
////                    actionIdO.put(id_OInside,objAction);
//                }
//                // 创建新的当前任务
//                Task taskNew = TaskObj.getTaskX(taskUsed.getTePFinish(), taPFinish
//                        , (taPFinish - taskUsed.getTePFinish()), taskUsed);
//                // 计算超时时间
//                long taOver = (taPFinish - taskUsed.getTePFinish());
//                // 更新超时时间
//                taskNew.setTaOver(taOver);
////                System.out.println("taOver-q:");
////                System.out.println(taskNew.getTaOver());
//
//                // 全部任务存储
//                JSONObject objTaskAll = new JSONObject();
//                JSONObject casItemx = salesOrderData.getCasItemx();
//                // 获取递归存储的时间处理信息
//                JSONArray oDates = casItemx.getJSONObject("java").getJSONArray("oDates");
////                // 获取递归存储的时间任务信息
////                JSONArray oTasks = casItemx.getJSONObject("java").getJSONArray("oTasks");
////                JSONObject resultTask = mergeTaskByPrior(oDates, oTasks, grpUNumAll);
////                oDates = resultTask.getJSONArray("oDates");
////                oTasks = resultTask.getJSONArray("oTasks");
//
//                oDates = mergeTaskByPrior(oDates,grpUNumAll);
//                qt.setMDContent(orderNew.getInfo().getId_OP(),qt.setJson("casItemx.java.oDates",oDates
////                        ,"casItemx.java.oTasks",oTasks
//                ), Order.class);
//
//                // 定义记录是否是当前新任务处理
//                boolean isOneDel = true;
////                System.out.println(JSON.toJSONString(objTask));
////                System.out.println("objTaskAll-q:");
////                System.out.println(JSON.toJSONString(objTaskAll));
////                Map<String,Asset> assetMap = new HashMap<>();
//                // 遍历时间处理信息集合
//                for (int i = taskNew.getDateIndex(); i < oDates.size(); i++) {
//                    // 获取i对应的时间处理信息
//                    JSONObject oDate = oDates.getJSONObject(i);
//                    // 获取时间处理的组别
//                    String grpBNew = oDate.getString("grpB");
//                    // 根据组别获取部门
//                    String depNew = grpBGroupIdOJ.getJSONObject(grpBNew).getString("dep");
//                    Asset assetNew;
//                    if (assetMap.containsKey(depNew)) {
//                        assetNew = assetMap.get(depNew);
//                    } else {
//                        assetNew = qt.getConfig(id_C,"d-"+depNew,timeCard);
//                        assetMap.put(depNew,assetNew);
//                    }
//                    // 判断asset为空
//                    if (null == assetNew) {
//                        continue;
//                    }
//                    // 获取aArrange2卡片信息
//                    JSONObject aArrangeNew = getAArrangeNew(assetNew);
//                    // 判断卡片为空
//                    if (null == aArrangeNew) {
//                        continue;
//                    }
//                    // 获取所有任务信息
//                    JSONObject objTaskNew = aArrangeNew.getJSONObject("objTask");
//                    oDate.put("dep",depNew);
//                    oDate.put("grpUNum",getObjGrpUNum(grpBNew,depNew,oDate.getString("id_C"),grpUNumAll));
//                    oDates.set(i,oDate);
//                    // 判断不是当前新任务处理
//                    if (!isOneDel) {
//                        // 获取任务订单编号
//                        String id_OThis = oDate.getString("id_O");
//                        // 获取任务订单编号对应下标
//                        Integer indexThis = oDate.getInteger("index");
////                        // 根据订单编号获取进度信息
////                        JSONArray actions = actionIdO.getJSONArray(id_OThis);
////                        // 根据下标获取进度信息
////                        JSONObject actionIndex = actions.getJSONObject(indexThis);
//                        // 获取任务的所在日期对象
//                        JSONObject teDateNext = oDate.getJSONObject("teDate");
////                        // 获取指定部门任务信息
////                        JSONObject depTask = objTaskNew.getJSONObject(depNew);
//                        // 获取指定组别任务信息
//                        JSONObject grpBTask = objTaskNew.getJSONObject(grpBNew);
////                        System.out.println("w - id_O:"+id_OThis+" - index:"+indexThis);
//                        // 遍历所在日期
//                        teDateNext.keySet().forEach(time -> {
////                            System.out.println("dep:"+depNew+" - grpB:"+grpBNew+" - time:"+time);
////                            System.out.println(JSON.toJSONString(depTask));
//                            // 根据所在日期获取任务信息
//                            JSONObject timeTask = grpBTask.getJSONObject(time);
//                            // 获取任务列表
//                            JSONArray tasksNew = timeTask.getJSONArray("tasks");
////                            System.out.println("tasksNew:");
////                            System.out.println(JSON.toJSONString(tasksNew));
//                            // 获取任务余剩时间
//                            Long zon = timeTask.getLong("zon");
//                            // 定义存储删除信息
//                            JSONArray removeIndex = new JSONArray();
////                            System.out.println("id_OThis:"+id_OThis+" - indexThis:"+indexThis);
//                            // 遍历任务列表
//                            for (int t = 1; t < tasksNew.size(); t++) {
//                                // 获取任务信息
//                                JSONObject taskInside = tasksNew.getJSONObject(t);
////                                System.out.println("n - id_O:"+taskInside.getString("id_O")+" - index:"+taskInside.getInteger("index"));
//                                // 判断循环任务订单编号等于当前任务编号并且循环任务下标等于当前任务下标
//                                if (taskInside.getString("id_O").equals(id_OThis)
//                                        && Objects.equals(taskInside.getInteger("index"), indexThis)) {
//                                    // 添加到删除
//                                    JSONObject removeInfo = new JSONObject();
//                                    removeInfo.put("index",t);
//                                    removeInfo.put("wntDurTotal",taskInside.getLong("wntDurTotal"));
//                                    removeIndex.add(removeInfo);
//                                }
//                            }
////                            System.out.println("removeIndex:");
////                            System.out.println(JSON.toJSONString(removeIndex));
//                            // 遍历删除信息
//                            for (int r = removeIndex.size()-1; r >= 0; r--) {
//                                // 获取删除信息
//                                JSONObject indexJson = removeIndex.getJSONObject(r);
////                                System.out.println("indexNew-1:"+indexJson.getInteger("index"));
//                                // 获取删除下标
//                                int indexNewThis = Integer.parseInt(indexJson.getString("index"));
//                                // 删除任务
//                                tasksNew.remove(indexNewThis);
//                                // 添加总时间
//                                zon+=indexJson.getLong("wntDurTotal");
//                            }
//
////                            tasksNew.remove(2);
////                            System.out.println(JSON.toJSONString(tasksNew));
//                            // 创建存储新任务列表
//                            List<Task> tasksNewTwo = new ArrayList<>();
//                            // 遍历删除后的任务列表并且添加到新任务列表
//                            for (int newI = 0; newI < tasksNew.size(); newI++) {
//                                tasksNewTwo.add(JSONObject.parseObject(JSON.toJSONString(tasksNew.getJSONObject(newI)),Task.class));
//                            }
//                            // 调用写入任务到全局任务信息方法
//                            setTasksAndZon(tasksNewTwo,grpBNew,depNew, Long.valueOf(time)
//                                    ,zon,objTaskAll);
//                            timeTask.put("tasks",tasksNew);
//                            timeTask.put("zon",zon);
//                            grpBTask.put(time,timeTask);
//                            objTaskNew.put(grpBNew,grpBTask);
//                        });
//                    } else {
//                        isOneDel = false;
//                    }
//                }
////                System.out.println("objTaskAll-h:");
////                System.out.println(JSON.toJSONString(objTaskAll));
//
////                // 创建请求参数存储字典
////                JSONObject mapKey = new JSONObject();
////                // 添加请求参数
////                mapKey.put("aArrange.objTask",objTask);
////                coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
//
//                // 定义存储是否是当前新任务
//                boolean isOne = true;
//                System.out.println("task-New:");
//                System.out.println(JSON.toJSONString(taskNew));
//                // 定义优先级
//                int wn0TPrior = 0;
//                // 获取唯一下标
//                String random = new ObjectId().toString();
//                // 获取全局唯一下标
//                String randomAll = new ObjectId().toString();
//
//                // 定义，存储进入未操作到的地方记录
//                JSONObject recordNoOperation = new JSONObject();
//                // 存储当前唯一编号的第一个当前时间戳
//                JSONObject onlyFirstTimeStamp = new JSONObject();
//                // 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
//                JSONObject newestLastCurrentTimestamp = new JSONObject();
//                // 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
//                JSONObject onlyRefState = new JSONObject();
//                // 统一id_O和index存储记录状态信息
//                JSONObject recordId_OIndexState = new JSONObject();
//                // 存储任务所在日期
//                JSONObject storageTaskWhereTime = new JSONObject();
//                // 镜像任务存储
//                Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks = new HashMap<>(16);
//                // 镜像总时间存储
//                JSONObject allImageTotalTime = new JSONObject();
//
//                // 设置问题记录的初始值
//                yiShu.put(randomAll,0);
//                leiW.put(randomAll,0);
//                xin.put(randomAll,0);
//                isQzTz.put(randomAll,0);
//                recordNoOperation.put(randomAll,new JSONArray());
//
//                // 设置存储当前唯一编号的第一个当前时间戳
//                onlyFirstTimeStamp.put(random, currentTime);
//                // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
//                onlyRefState.put(random,0);
//                // 存储最初开始时间
//                long initialStartTime = taskNew.getTePStart();
//                // 存储最后结束时间
//                long lastEndTime = 0L;
//
//                // 用于存储时间冲突的副本
//                JSONObject timeConflictCopy = new JSONObject();
//                // 用于存储判断镜像是否是第一个被冲突的产品
//                JSONObject sho = new JSONObject();
//                // 用于存储控制只进入一次的判断，用于记录第一个数据处理的结束时间
//                boolean canOnlyEnterOnce = true;
//                // 定义用来存储最大结束时间
//                long maxSte = 0;
//                // 用于存储每一个时间任务的结束时间
//                JSONArray teFinList = new JSONArray();
//                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
//                JSONObject serialOneFatherLastTime = new JSONObject();
//                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的预计开始时间
//                JSONObject serialOneFatherStartTime = new JSONObject();
////                // 当前处理通用信息存储
////                JSONObject thisInfo = new JSONObject();
//                // 添加信息
//                setThisInfoRef(thisInfo,"againTime");
//                // 镜像任务所在日期
//                JSONObject allImageTeDate = new JSONObject();
//                JSONObject objActions = new JSONObject();
//                // 遍历时间处理信息集合
//                for (int i = taskNew.getDateIndex(); i < oDates.size(); i++) {
//                    // 获取i对应的时间处理信息
//                    JSONObject oDate = oDates.getJSONObject(i);
//                    // 获取时间处理的父零件编号
//                    String id_PF = oDate.getString("id_PF");
//                    // 获取时间处理的序号
//                    Integer priorItem = oDate.getInteger("priorItem");
//                    // 获取时间处理的序号是否为1层级 csSta - timeHandleSerialNoIsOne
//                    Integer csSta = oDate.getInteger("csSta");
//                    // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
//                    Integer kaiJie = oDate.getInteger("kaiJie");
//                    // 获取当前唯一ID存储时间处理的最初开始时间
//                    Long hTeStart = initialStartTime;
//                    // 调用时间处理方法
//                    JSONObject timeHandleInfo;
//                    // 定义任务变量
//                    Task task;
//                    // 定义组别变量
//                    String grpBNew;
//                    // 定义部门变量
//                    String depNew;
//                    // 定义当前订单编号变量
//                    String id_OInside;
//                    // 定义当前下班变量
//                    int indexInside;
//                    // 创建当前处理的任务的所在日期对象
//                    JSONObject teDate;
//                    // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
//                    JSONObject firstConflictId_O = new JSONObject();
//                    JSONObject firstConflictIndex = new JSONObject();
//                    // 设置为-1代表的是递归的零件
//                    firstConflictIndex.put("prodState",-1);
//                    firstConflictIndex.put("z","-1");
//                    // 判断是当前新任务
//                    if (isOne) {
//                        // 更新状态
//                        isOne = false;
//                        // 创建当前处理的任务的所在日期对象
//                        teDate = new JSONObject();
//                        // 赋值信息
//                        task = taskNew;
//                        grpBNew = grpB;
//                        depNew = dep;
//                        id_OInside = task.getId_O();
//                        indexInside = task.getIndex();
//
//                        firstConflictId_O.put(indexInside+"",firstConflictIndex);
//                        sho.put(id_OInside,firstConflictId_O);
//
//                        // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
//                        if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1
//                                && kaiJie != 5 && kaiJie != 3) {
//                            // 根据父id添加开始时间
//                            serialOneFatherStartTime.put(id_PF,task.getTeCsStart());
//                        }
//
//                        // 调用时间处理方法
//                        timeHandleInfo = timeZjService.timeHandle(taskNew, hTeStart,grpB,dep, id_OInside, indexInside
//                                ,0,random,1,teDate,timeConflictCopy,0
//                                ,sho,0,0,randomAll,xbAndSbAll,actionIdO,objTaskAll
//                                ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
//                                ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
//                                ,clearStatus,thisInfo,allImageTeDate,false,depAllTime);
//                        // 更新任务最初始开始时间
////                        hTeStart = timeHandleInfo.getLong("hTeStart");
//                        System.out.println("超时的结束:"+timeHandleInfo.getLong("hTeStart"));
//                    } else {
//                        // 获取订单编号
//                        id_OInside = oDate.getString("id_O");
//                        // 获取订单下标
//                        indexInside = oDate.getInteger("index");
////                        JSONArray objAction = actionIdO.getJSONArray(id_OInside);
//                        JSONArray objAction = objActions.getJSONArray(id_OInside);
//                        if (null == objAction) {
//                            Order order = qt.getMDContent(id_OInside, "action", Order.class);
//                            if (null == order || null == order.getAction()) {
//                                continue;
//                            }
//                            objAction = order.getAction().getJSONArray("objAction");
//                        }
//                        JSONObject indexAction = objAction.getJSONObject(indexInside);
//                        int bcdStatus = indexAction.getInteger("bcdStatus") == null?0:indexAction.getInteger("bcdStatus");
////                        if (bcdStatus != 100 && bcdStatus != 0) {
////                            continue;
////                        }
//                        if (bcdStatus == 8 || bcdStatus == 2) {
//                            continue;
//                        }
//                        // 获取时间处理的判断是否是空时间信息
//                        Boolean empty = oDate.getBoolean("empty");
//                        // 判断当前时间处理为空时间信息
//                        if (empty) {
//                            // 获取时间处理的链接下标
//                            Integer linkInd = oDate.getInteger("linkInd");
//                            // 根据链接下标获取指定的结束时间
//                            Long indexEndTime = teFinList.getLong(linkInd);
//                            // 判断父id的预计开始时间为空，并且序号为第一个
//                            if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1) {
//                                serialOneFatherStartTime.put(id_PF,indexEndTime);
//                            }
//                            // 根据父零件编号获取序号信息
//                            JSONObject fatherSerialInfo = serialOneFatherLastTime.getJSONObject(id_PF);
//                            // 判断序号信息为空
//                            if (null == fatherSerialInfo) {
//                                // 创建序号信息
//                                fatherSerialInfo = new JSONObject();
//                                // 添加序号的结束时间，默认为0
//                                fatherSerialInfo.put(priorItem.toString(),0);
//                            }
//                            // 获取序号结束时间
//                            Long serialEndTime = fatherSerialInfo.getLong(priorItem.toString());
//                            // 添加链接结束时间到当前空时间处理结束时间列表内
//                            teFinList.add(indexEndTime);
//                            // 判断链接结束时间大于当前结束时间
//                            if (indexEndTime > serialEndTime) {
//                                // 修改当前结束时间为链接结束时间
//                                fatherSerialInfo.put(priorItem.toString(),indexEndTime);
//                                // 根据父零件编号添加序号信息
//                                serialOneFatherLastTime.put(id_PF,fatherSerialInfo);
//                            }
//                            continue;
//                        }
//                        firstConflictId_O.put(oDate.getString("index"),firstConflictIndex);
//                        sho.put(oDate.getString("id_O"),firstConflictId_O);
//                        // 获取时间处理的组别
//                        grpBNew = oDate.getString("grpB");
//                        depNew = oDate.getString("dep");
//                        // 获取时间处理的零件产品编号
//                        String id_P = oDate.getString("id_P");
////                        // 获取时间处理的实际准备时间
////                        Long wntPrep = oTasks.getJSONObject(i).getLong("wntPrep");
////                        long wntDurTotal = oTasks.getJSONObject(i).getLong("wntDurTotal");
//                        // 获取时间处理的实际准备时间
//                        Long wntPrep = oDate.getLong("wntPrep");
//                        long wntDurTotal = oDate.getLong("wntDurTotal");
//
//                        // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
//                        if (csSta == 1 && kaiJie == 1) {
//                            // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
//                            hTeStart = lastEndTime;
////                            System.out.println("开始时间-1:"+hTeStart);
//                        }
//                        long initialTeStart = hTeStart;
////                        oDate.put("teStart",hTeStart);
//
//                        // 存储判断执行方法
////                        boolean isExecutionMethod = (csSta == 0 && priorItem != 0) || (kaiJie != 1 && csSta == 1);
//                        boolean isExecutionMethod = (csSta == 0 && priorItem != 1) || (kaiJie != 1 && csSta == 1);
//                        // 序号是不为1层级
//                        // 判断执行方法为true
//                        if (isExecutionMethod) {
//                            // 定义获取存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
//                            JSONObject serialOneEndTime;
//                            // 获取判断自己的id是否等于已存在的父id
//                            boolean b = serialOneFatherLastTime.containsKey(id_P);
//                            // 判断自己的id是已存在的父id
//                            if (b) {
//                                // 根据自己的id获取按照父零件编号存储每个序号的最后结束时间
//                                serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_P);
//                                // 转换键信息
//                                List<String> list = new ArrayList<>(serialOneEndTime.keySet());
//                                // 获取最后一个时间信息
//                                String s = list.get(list.size() - 1);
//                                // 赋值为最后一个时间信息
//                                hTeStart = serialOneEndTime.getLong(s);
//                            } else {
//                                // 根据父id获取按照父零件编号存储每个序号的最后结束时间
//                                serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_PF);
//                                // 获取上一个序号的时间信息并赋值
//                                hTeStart = serialOneEndTime.getLong(((priorItem - 1) + ""));
//                            }
////                            System.out.println("开始时间-2:"+hTeStart+" - id_P:"+id_P+" - id_PF:"+id_PF);
////                            System.out.println(JSON.toJSONString(serialOneFatherLastTime));
//                            // 设置开始时间
////                            oDate.put("teStart",hTeStart);
//                            initialTeStart = hTeStart;
//                        }
//
//                        // 获取任务的最初开始时间备份
////                        Long teStartBackups = oDate.getLong("teStart");
//                        Long teStartBackups = initialTeStart;
////                        // 设置最初结束时间
////                        oDate.put("teFin",(teStartBackups+(wntDurTotal+wntPrep)));
////                        // 获取最初结束时间
////                        Long teFin = oDate.getLong("teFin");
//                        // 获取最初结束时间
//                        Long teFin = (teStartBackups+(wntDurTotal+wntPrep));
////                        // 获取任务信息，并且转换为任务类
////                        task = JSON.parseObject(JSON.toJSONString(oTasks.get(i)),Task.class);
//                        task = new Task(wn0TPrior,id_OInside,indexInside,wntPrep
//                                ,wntDurTotal+wntPrep,oDate.getString("id_C"),i,oDate.getJSONObject("wrdN"));
//                        // 设置最初任务信息的时间信息
////                        task.setWntDurTotal((teFin - teStartBackups));
//                        task.setTePStart(teStartBackups);
//                        task.setTePFinish(teFin);
//                        task.setTeCsStart(teStartBackups);
//                        task.setTeCsSonOneStart(0L);
////                        task.setDateIndex(i);
//                        task.setTaOver(taOver);
//                        task.setWrdNO(info.getWrdN());
//                        task.setRefOP(orderNew.getInfo().getId_OP());
//                        task.setWn2qtyneed(oDate.getDouble("wn2qtyneed"));
//                        // 设置优先级为传参的优先级
////                        task.setPriority(wn0TPrior);
//                        // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
//                        if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1
//                                && kaiJie != 5 && kaiJie != 3) {
//                            // 根据父id添加开始时间
//                            serialOneFatherStartTime.put(id_PF,task.getTeCsStart());
//                        } else if (kaiJie == 3 || kaiJie == 5) {
//                            // 添加子最初开始时间
//                            task.setTeCsSonOneStart(serialOneFatherStartTime.getLong(id_P));
//                        }
//
//                        // 创建当前处理的任务的所在日期对象
//                        teDate = new JSONObject();
//                        System.out.println("taskTe-TSF:");
//                        System.out.println(JSON.toJSONString(task));
//                        // 调用时间处理方法
//                        timeHandleInfo = timeZjService.timeHandle(task,hTeStart,grpBNew,depNew
//                                ,id_OInside,indexInside
//                                ,0,random,1,teDate,timeConflictCopy,0
//                                ,sho,0,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
//                                ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
//                                ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
//                                ,clearStatus,thisInfo,allImageTeDate,false,depAllTime);
//                    }
//
//                    // 更新任务最初始开始时间
//                    hTeStart = timeHandleInfo.getLong("hTeStart");
//                    System.out.println("最外层-1:"+hTeStart);
////                    System.out.println(JSON.toJSONString(timeHandleInfo));
//                    // 添加结束时间
//                    teFinList.add(hTeStart);
////                    // 根据订单编号获取递归集合
////                    JSONArray dgList = actionIdO.getJSONArray(id_OInside);
////                    // 根据订单下标获取递归信息并且转换为递归类
////                    OrderAction orderAction = JSON.parseObject(
////                            JSON.toJSONString(dgList.getJSONObject(indexInside)),OrderAction.class);
////                    // 更新递归信息
////                    orderAction.setDep(depNew);
////                    orderAction.setGrpB(grpBNew);
////                    orderAction.setTeDate(teDate);
////                    // 将更新的递归信息写入回去
////                    dgList.set(indexInside,orderAction);
////                    actionIdO.put(id_OInside,dgList);
//
////                    String id_OP = sonGetOrderFatherId(id_OInside, id_C, thisInfo, actionIdO, grpUNumAll);
////                    JSONObject oPDateInfo = actionIdO.getJSONObject(id_OP);
////                    JSONArray oDatesNew = oPDateInfo.getJSONArray("oDates");
////                    JSONObject oDateThis = oDatesNew.getJSONObject(i);
////                    oDateThis.put("teDate",teDate);
////                    oDatesNew.set(i,oDateThis);
////                    oPDateInfo.put("oDates",oDatesNew);
////                    actionIdO.put(id_OP,oPDateInfo);
//
//                    String id_OP = sonGetOrderFatherId(id_OInside, id_C, thisInfo, actionIdO, grpUNumAll);
//                    JSONObject oPDateInfo = actionIdO.getJSONObject(id_OP);
//                    JSONArray oDatesNew = oPDateInfo.getJSONArray("oDates");
//                    JSONObject oDateThis = oDatesNew.getJSONObject(i);
//                    oDateThis.put("teDate",teDate);
//                    oDatesNew.set(i,oDateThis);
//                    oPDateInfo.put("oDates",oDatesNew);
//                    actionIdO.put(id_OP,oPDateInfo);
//
//                    // 定义存储最后结束时间参数
//                    long storageLastEndTime;
//                    // 判断序号是为1层级
//                    if (csSta == 1) {
//                        // 获取实际结束时间
//                        Long actualEndTime = timeHandleInfo.getLong("xFin");
//                        // 定义存储判断实际结束时间是否为空
//                        boolean isActualEndTime = false;
//                        // 判断实际结束时间不等于空
//                        if (null != actualEndTime) {
//                            // 赋值实际结束时间
//                            hTeStart = actualEndTime;
//                            // 判断当前实际结束时间大于最大结束时间
//                            if (actualEndTime > maxSte) {
//                                // 判断大于则更新最大结束时间为当前结束时间
//                                maxSte = actualEndTime;
//                            }
//                            // 设置不为空
//                            isActualEndTime = true;
//                        } else {
//                            // 判断当前实际结束时间大于最大结束时间：注 ： xFin 和 task.getTePFinish() 有时候是不一样的，不能随便改
//                            if (task.getTePFinish() > maxSte) {
//                                // 判断大于则更新最大结束时间为当前结束时间
//                                maxSte = task.getTePFinish();
//                            }
//                        }
//                        // 判断实际结束时间不为空
//                        if (isActualEndTime) {
//                            // 赋值结束时间
//                            storageLastEndTime = actualEndTime;
//                        } else {
//                            // 赋值结束时间
//                            storageLastEndTime = task.getTePFinish();
//                        }
//                        // 判断是第一次进入
//                        if (canOnlyEnterOnce) {
//                            // 添加设置第一层的开始时间
//                            lastEndTime=task.getTePStart();
//                            // 设置只能进入一次
//                            canOnlyEnterOnce = false;
//                        }
//                    } else {
//                        // 直接赋值最后结束时间
//                        storageLastEndTime = hTeStart;
//                    }
//                    // 根据父id获取最后结束时间信息
//                    JSONObject fatherGetEndTimeInfo = serialOneFatherLastTime.getJSONObject(id_PF);
//                    // 判断最后结束时间信息为空
//                    if (null == fatherGetEndTimeInfo) {
//                        // 创建并且赋值最后结束时间
//                        fatherGetEndTimeInfo = new JSONObject();
//                        fatherGetEndTimeInfo.put(priorItem.toString(),0);
//                    }
//                    // 根据序号获取最后结束时间
//                    Long aLong = fatherGetEndTimeInfo.getLong(priorItem.toString());
//                    // 判断最后结束时间为空
//                    if (null == aLong) {
//                        // 为空，则直接添加最后结束时间信息
//                        fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
//                        serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
//                    } else {
//                        // 不为空，则判断当前最后结束时间大于已存在的最后结束时间
//                        if (storageLastEndTime > aLong) {
//                            // 判断当前最后结束时间大于，则更新最后结束时间为当前结束时间
//                            fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
//                            serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
//                        }
//                    }
//                    initialStartTime=hTeStart;
////                    System.out.println();
//                }
//
//                // 调用任务最后处理方法
//                timeZjServiceComprehensive.taskLastHandle(timeConflictCopy,id_C,randomAll,objTaskAll
//                        ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation
//                        ,orderNew.getInfo().getId_OP(),objOrderList,actionIdO,allImageTeDate,depAllTime,thisInfo);
//
//                // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
//                onlyFirstTimeStamp.remove(random);
//                // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
//                newestLastCurrentTimestamp.remove(random);
//                // 根据当前唯一标识删除信息
//                onlyRefState.remove(random);
//            }
////            updateArrangeState(0,assetId);
//            updateArrangeState(0,asset.getId());
            // 抛出操作成功异常
            return retResult.ok(CodeEnum.OK.getCode(), "处理成功!");
        } catch (Exception ex) {
            updateArrangeState(0,asset.getId());
            System.out.println("出现异常");
            ex.printStackTrace();
            // 抛出操作成功异常
            return retResult.ok(CodeEnum.OK.getCode(), "出现异常!");
        }
    }

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
    @Override
    public ApiResponse timeCalculation(String id_O, int index,int number) {
        Order order = qt.getMDContent(id_O, Arrays.asList("oItem","oStock"), Order.class);
        if (null == order) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ORDER_NULL.getCode(), "订单为空");
        }
        JSONObject oItem = order.getOItem();
        if (null == oItem) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ORDER_O_ITEM_NULL.getCode(), "订单卡片oItem为空");
        }
        JSONArray objItem = oItem.getJSONArray("objItem");
        if (null == objItem) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ORDER_O_ITEM_OBJ_ITEM_NULL.getCode(), "订单卡片oItem内objItem为空");
        }
        JSONObject oStock = order.getOStock();
        if (null == oStock) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ORDER_O_STOCK_NULL.getCode(), "订单卡片oStock为空");
        }
        JSONArray objData = oStock.getJSONArray("objData");
        if (null == objData) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ORDER_O_STOCK_OBJ_DATA_NULL.getCode(), "订单卡片oStock内objData为空");
        }
        try {
            JSONObject objDataSon = objData.getJSONObject(index);
            int wn2qtymade = objDataSon.getInteger("wn2qtymade");
            JSONObject objItemInd = objItem.getJSONObject(index);
            double wn2qtyneed = objItemInd.getDouble("wn2qtyneed");
            long wntDur = objItemInd.getLong("wntDur")==null?120:objItemInd.getLong("wntDur");
            // 存储任务总时间
            long taskTotalTime = (long) (wntDur * (wn2qtyneed-wn2qtymade));
            long grpUNum;
            if (taskTotalTime % number == 0) {
                grpUNum = taskTotalTime / number;
            } else {
                grpUNum = (long) Math.ceil((double) (taskTotalTime / number));
            }
            // 抛出操作成功异常
            return retResult.ok(CodeEnum.OK.getCode(), grpUNum);
        } catch (Exception ex) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_UNKNOWN.getCode(), "未知异常");
        }
    }

    /**
     * 删除或者新增aArrange卡片信息
     * @param id_C	公司编号
     * @param object	操作信息
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse delOrAddAArrange(String id_C,String dep,JSONObject object) {
        Asset asset = qt.getConfig(id_C,"d-"+dep,timeCard);
        // 判断asset为空
        if (null == asset) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_NULL.getCode(), "资产为空");
        }
        // 获取aArrange2卡片信息
        JSONObject aArrange = getAArrangeNew(asset);
        // 判断卡片为空
        if (null == aArrange || null == aArrange.getJSONObject("objTask")) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_ARRANGE_NULL.getCode(), "资产Arrange为空");
        }
        // 获取卡片的任务信息
        JSONObject objTask = aArrange.getJSONObject("objTask");
        // 获取删除操作信息状态
        int delState = object.getInteger("delState");
        // 判断要删除
        if (delState != 0) {
            // 获取删除操作信息
            JSONObject del = object.getJSONObject("del");
            // 判断删除状态为1
            if (delState == 1) {
                // 删除任务的部门信息
                objTask = new JSONObject();
            } else {
                // 获取删除操作的部门信息
                JSONArray delGrpBList = del.getJSONArray(dep);
                // 遍历删除操作部门信息
                for (int i = 0; i < delGrpBList.size(); i++) {
                    // 根据删除操作的部门信息删除任务的对应信息
                    objTask.remove(delGrpBList.getString(i));
                }
            }
        }
        // 获取新增操作信息状态
        int addState = object.getInteger("addState");
        // 定义存储新增错误信息
        JSONArray addList = new JSONArray();
        // 判断状态为新增
        if (addState != 0) {
            // 获取新增操作信息
            JSONObject add = object.getJSONObject("add");
            // 定义存储是否新增判断值
            boolean isAdd = false;
            // 获取新增操作部门信息
            JSONArray addDep = add.getJSONArray(dep);
            // 遍历新增操作部门信息
            for (int i = 0; i < addDep.size(); i++) {
                // 获取任务对应的组别信息
                JSONObject grpBInfo = objTask.getJSONObject(addDep.getString(i));
                // 判断任务组别信息为空
                if (null == grpBInfo) {
                    // 添加新增操作信息
                    objTask.put(addDep.getString(i),new JSONObject());
                    // 判断新增
                    if (!isAdd) {
                        // 赋值
                        isAdd = true;
                    }
                } else {
                    // 添加错误信息
                    JSONObject addInfo = new JSONObject();
                    addInfo.put("grpB",addDep.getString(i));
                    addInfo.put("desc","组别已存在，新增失败");
                    addList.add(addInfo);
                }
            }
        }
        // 创建返回结果
        JSONObject result = new JSONObject();
        // 添加返回信息
        result.put("addList",addList);
        result.put("desc","操作成功!");
        // 判断错误信息大于0
        if (addList.size() > 0) {
            // 添加返回状态
            result.put("state",1);
        } else {
            // 添加返回状态
            result.put("state",0);
        }
        qt.setMDContent(asset.getId(),qt.setJson(timeCard+".objTask",objTask),Asset.class);
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    /**
     * 更新aArrange卡片操作状态接口
     * @param operationState	更新后状态
     * @param assetId	资产编号
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    private void updateArrangeState(int operationState,String assetId){
        qt.setMDContent(assetId,qt.setJson(timeCard+".operationState",operationState), Asset.class);
    }
}
