package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.TimeZjService;
import com.cresign.action.utils.TaskObj;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.assetCard.AssetInfo;
import com.cresign.tools.pojo.po.chkin.Task;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Description 作者很懒什么也没写
 * @ClassName TimeServiceImpl
 * @author tang
 * @Date 2021/12/20 11:10
 * @ver 1.0.0
 */
@Service
public class TimeZjServiceImplX extends TimeZj implements TimeZjService {

    /**
     * 是否测试: = true 说明是测试、 = false 说明不是测试
     */
    public static boolean isTest = true;
    /**
     * 是否添加测试订单与任务: = true 说明是、 = false 说明不是
     */
    public static boolean isTestAddOrder = true;

    @Resource
    private RetResult retResult;

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
    @Override
    public ApiResponse moveAArrange(String id_C, String moveId_A, String coverMoveId_A) {
        // 根据asset编号获取asset的时间处理卡片信息
//        Asset asset = coupaUtil.getAssetById(coverMoveId_A, Arrays.asList(timeCard,"info"));
        Asset asset = qt.getMDContent(coverMoveId_A,qt.strList(timeCard,"info"), Asset.class);
        if (null == asset) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_NULL.getCode(), "被移动的资产为空");
        }
        AssetInfo info = asset.getInfo();
        if (null == info) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_INFO_NULL.getCode(), "资产内Info为空");
        }
        if (!info.getId_C().equals(id_C)) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ID_C_NO_MATCHING.getCode(), "公司编号不匹配");
        }
//        JSONObject aArrange = asset.getAArrange2();
        JSONObject aArrange = getAArrangeNew(asset);
        if (null == aArrange) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_ARRANGE_NULL.getCode(), "被移动的资产Arrange为空");
        }
        JSONObject objTask = aArrange.getJSONObject("objTask");
        if (null == objTask) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_ARRANGE_OBJ_TASK_NULL.getCode(), "被移动的资产Arrange内objTask为空");
        }
        // 根据asset编号获取asset的时间处理卡片信息
//        Asset assetMove = coupaUtil.getAssetById(moveId_A, Arrays.asList(timeCard,"info"));
        Asset assetMove = qt.getMDContent(moveId_A,qt.strList(timeCard,"info"), Asset.class);
        if (null == assetMove) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_NULL.getCode(), "移动的资产为空");
        }
        AssetInfo infoNew = assetMove.getInfo();
        if (null == infoNew) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_INFO_NULL.getCode(), "移动的资产内Info为空");
        }
        if (!infoNew.getId_C().equals(id_C)) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ID_C_NO_MATCHING.getCode(), "移动的公司编号不匹配");
        }
        // 获取时间处理卡片信息
//        JSONObject aArrangeMove = assetMove.getAArrange2();
        JSONObject aArrangeMove = getAArrangeNew(assetMove);
        if (null == aArrangeMove) {
            aArrangeMove = new JSONObject();
        } else {
            JSONObject objTaskNew = aArrangeMove.getJSONObject("objTask");
            if (null != objTaskNew) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_EXISTS_NULL.getCode(), "资产内已存在");
            }
        }
        // 添加信息
        aArrangeMove.put("objTask",objTask);
//        // 创建请求参数存储字典
//        JSONObject mapKey = new JSONObject();
//        // 添加请求参数
//        mapKey.put(timeCard,aArrangeMove);
//        coupaUtil.updateAssetByKeyAndListKeyVal("id",moveId_A,mapKey);
        qt.setMDContent(moveId_A,qt.setJson(timeCard,aArrangeMove), Asset.class);
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "移动AArrange成功!");
    }

    /**
     * 获取物料预计开始时间
     * @param id_O	订单编号
     * @param id_C	公司编号
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/17
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse getEstimateStartTime(String id_O, String id_C, Long teStart) {
        // 调用方法获取订单信息
//        Order salesOrderData = coupaUtil.getOrderByListKey(
//                id_O, Arrays.asList("info", "action", "casItemx"));
        Order salesOrderData = qt.getMDContent(id_O,qt.strList("info", "action", "casItemx"), Order.class);
        System.out.println("--------");
        System.out.println(JSON.toJSONString(salesOrderData));
        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getInfo() || null == salesOrderData.getAction()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        if (null != salesOrderData.getInfo().getId_OP()) {
            id_O = salesOrderData.getInfo().getId_OP();
//            salesOrderData = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info", "action"));
            salesOrderData = qt.getMDContent(id_O,qt.strList("info", "action", "casItemx"), Order.class);
            // 判断订单是否为空
            if (null == salesOrderData || null == salesOrderData.getInfo()
                    || null == salesOrderData.getAction() || null == salesOrderData.getCasItemx()) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "父订单不存在");
            }
        }
        // 存储部门对应组别的职位总人数
        JSONObject grpUNumAll = new JSONObject();
        // 获取递归订单列表
        JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
//        Asset asset = qt.getConfig(id_C,"a-chkin","chkin00s");
//        // 获取打卡卡片信息
//        JSONObject chkin00s = asset.getChkin00s();
//        // 获取职位人数信息
//        JSONObject objDepInfo = chkin00s.getJSONObject("objZw");
//        // 存储判断职位人数信息是否为空
//        boolean isDepNull = null != objDepInfo;
//        // 获取打卡信息
//        JSONArray objData = chkin00s.getJSONArray("objData");
//        // 存储判断打卡信息是否为空
//        boolean isXbAndSbNull = true;
//        // 存储判断是否是否有时间处理打卡信息
//        boolean isTimeChKin = false;
//        // 定义存储时间处理打卡时间字典
//        JSONObject xbAndSb = null;
//        // 定义存储时间处理打卡信息下标
//        int chKinInfoIndex = -1;
//        // 判断打卡信息为空
//        if (null == objData) {
//            isXbAndSbNull = false;
//        } else {
//            // 遍历打卡信息
//            for (int j = 0; j < objData.size(); j++) {
//                // 根据j获取对应的打卡信息
//                JSONObject chKinInfo = objData.getJSONObject(j);
//                // 判断是否是时间处理打卡信息
//                if (null != chKinInfo.getInteger("timeP")) {
//                    // 设置为是
//                    isTimeChKin = true;
//                    // 获取下标位置
//                    chKinInfoIndex = j;
//                    // 创建时间处理打卡信息存储
//                    xbAndSb = new JSONObject();
//                    // 获取时间处理打卡的上班和下班信息
//                    JSONObject objDataZ = objData.getJSONObject(j);
//                    // 添加信息
//                    xbAndSb.put("xb",objDataZ.getJSONArray("objXb"));
//                    xbAndSb.put("sb",objDataZ.getJSONArray("objSb"));
//                    break;
//                }
//            }
//        }
//        // 获取部门组别对应时间处理打卡信息下标字典
//        JSONObject objWorkTime = chkin00s.getJSONObject("objWorkTime");
//        // 判断为空
//        if (null == objWorkTime) {
//            isXbAndSbNull = false;
//        }
//        // 存储部门对应组别的上班和下班时间
//        JSONObject xbAndSbAll = new JSONObject();
//        // 根据组别存储部门信息
//        JSONObject grpBGroupIdOJ = new JSONObject();
//        // 遍历订单列表
//        for (int i = 0; i < objOrder.size(); i++) {
//            // 获取订单列表的订单编号
//            String id_OInside = objOrder.getJSONObject(i).getString("id_O");
//            // 判断订单等于主订单，则通过循环
//            if (id_OInside.equals(id_O)) {
//                continue;
//            }
//            // 根据订单编号查询action卡片信息
////            Order insideAction = coupaUtil.getOrderByListKey(id_OInside, Collections.singletonList("action"));
//            Order insideAction = qt.getMDContent(id_OInside,"action", Order.class);
//            System.out.println();
//            System.out.println(JSON.toJSONString(insideAction));
//            // 获取组别对应部门信息
//            JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
//            // 遍历组别对应部门信息
//            for (String grpB : grpBGroup.keySet()) {
//                // 创建存储部门字典
//                JSONObject depMap = new JSONObject();
//                // 根据组别获取组别信息
//                JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpB);
//                // 获取组别的部门
//                String dep = grpBGroupInfo.getString("dep");
//                // 判断职位人数不为空
//                if (isDepNull) {
//                    // 根据部门获取职位人数部门信息
//                    JSONObject depInfo = objDepInfo.getJSONObject(dep);
//                    // 判断不为空
//                    if (null != depInfo) {
//                        // 根据组别，获取职位人数部门对应的组别信息
//                        Integer grpBInfo = objDepInfo.getInteger(grpB);
//                        if (null != grpBInfo) {
//                            // 根据部门，获取部门对应的全局职位人数信息
//                            JSONObject depAllInfo = grpUNumAll.getJSONObject(dep);
//                            // 判断部门全局职位人数信息为空
//                            if (null == depAllInfo) {
//                                // 创建部门全局职位人数信息
//                                depAllInfo = new JSONObject();
//                                // 根据组别添加职位人数
//                                depAllInfo.put(grpB,grpBInfo);
//                                grpUNumAll.put(dep,depAllInfo);
//                            } else {
//                                // 直接根据组别获取全局职位人数
//                                Integer grpBAllInfo = depAllInfo.getInteger(grpB);
//                                // 判断为空
//                                if (null == grpBAllInfo) {
//                                    // 添加全局职位人数信息
//                                    depAllInfo.put(grpB,grpBAllInfo);
//                                    grpUNumAll.put(dep,depAllInfo);
//                                }
//                            }
//                        }
//                    }
//                }
//                // 判断上班下班时间不为空，并且有时间处理打卡信息
//                if (isXbAndSbNull && isTimeChKin) {
//                    // 根据部门获取上班下班信息
//                    JSONObject depChKin = objWorkTime.getJSONObject(dep);
//                    if (null != depChKin) {
//                        // 根据组别获取上班下班信息
//                        Integer grpBChKin = depChKin.getInteger(grpB);
//                        // 判断上班下班信息不为空，并且，下标位置等于时间处理打卡信息的下标
//                        if (null != grpBChKin && grpBChKin == chKinInfoIndex) {
//                            // 根据部门获取全局上班下班信息
//                            JSONObject depAllChKin = xbAndSbAll.getJSONObject(dep);
//                            // 判断为空
//                            if (null == depAllChKin) {
//                                // 创建
//                                depAllChKin = new JSONObject();
//                                // 添加全局上班下班信息
//                                depAllChKin.put(grpB,xbAndSb);
//                                xbAndSbAll.put(dep,depAllChKin);
//                            } else {
//                                // 根据组别获取全局上班下班信息
//                                JSONObject grpBAllChKin = depAllChKin.getJSONObject(grpB);
//                                if (null == grpBAllChKin) {
//                                    // 添加全局上班下班信息
//                                    depAllChKin.put(grpB,xbAndSb);
//                                    xbAndSbAll.put(dep,depAllChKin);
//                                }
//                            }
//                        }
//                    }
//                }
//                // 添加部门信息
//                depMap.put("dep",dep);
//                depMap.put("id_O",id_OInside);
//                // 添加信息
//                grpBGroupIdOJ.put(grpB,depMap);
//            }
//        }
        System.out.println("--- 分割 ---");
//        Asset asset = qt.getConfig(id_C,"a-chkin","chkin00s");
//        // 获取打卡卡片信息
//        JSONObject chkin00s = asset.getChkin00s();
//        // 获取职位人数信息
//        JSONObject objDepInfo = chkin00s.getJSONObject("objZw");
//        // 存储判断职位人数信息是否为空
//        boolean isDepNull = null != objDepInfo;
//        // 获取打卡信息
//        JSONArray objData = chkin00s.getJSONArray("objData");
//        // 存储判断打卡信息是否为空
//        boolean isXbAndSbNull = true;
//        // 存储判断是否是否有时间处理打卡信息
//        boolean isTimeChKin = false;
        // 定义存储时间处理打卡时间字典
        JSONObject xbAndSb;
//        // 定义存储时间处理打卡信息下标
//        int chKinInfoIndex = -1;
//        // 判断打卡信息为空
//        if (null == objData) {
//            isXbAndSbNull = false;
//        } else {
//            // 遍历打卡信息
//            for (int j = 0; j < objData.size(); j++) {
//                // 根据j获取对应的打卡信息
//                JSONObject chKinInfo = objData.getJSONObject(j);
//                // 判断是否是时间处理打卡信息
//                if (null != chKinInfo.getInteger("timeP")) {
//                    // 设置为是
//                    isTimeChKin = true;
//                    // 获取下标位置
//                    chKinInfoIndex = j;
//                    // 创建时间处理打卡信息存储
//                    xbAndSb = new JSONObject();
//                    // 获取时间处理打卡的上班和下班信息
//                    JSONObject objDataZ = objData.getJSONObject(j);
//                    // 添加信息
//                    xbAndSb.put("xb",objDataZ.getJSONArray("objXb"));
//                    xbAndSb.put("sb",objDataZ.getJSONArray("objSb"));
//                    break;
//                }
//            }
//        }
//        // 获取部门组别对应时间处理打卡信息下标字典
//        JSONObject objWorkTime = chkin00s.getJSONObject("objWorkTime");
//        // 判断为空
//        if (null == objWorkTime) {
//            isXbAndSbNull = false;
//        }
        // 存储部门对应组别的上班和下班时间
        JSONObject xbAndSbAll = new JSONObject();
        // 根据组别存储部门信息
        JSONObject grpBGroupIdOJ = new JSONObject();
        Map<String,Asset> assetMap = new HashMap<>();
        // 遍历订单列表
        for (int i = 0; i < objOrder.size(); i++) {
            // 获取订单列表的订单编号
            String id_OInside = objOrder.getJSONObject(i).getString("id_O");
            // 判断订单等于主订单，则通过循环
            if (id_OInside.equals(id_O)) {
                continue;
            }
            // 根据订单编号查询action卡片信息
//            Order insideAction = coupaUtil.getOrderByListKey(id_OInside, Collections.singletonList("action"));
            Order insideAction = qt.getMDContent(id_OInside,"action", Order.class);
            System.out.println();
            System.out.println(JSON.toJSONString(insideAction));
            // 获取组别对应部门信息
            JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
            // 遍历组别对应部门信息
            for (String grpB : grpBGroup.keySet()) {
                // 创建存储部门字典
                JSONObject depMap = new JSONObject();
                // 根据组别获取组别信息
                JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpB);
                // 获取组别的部门
                String dep = grpBGroupInfo.getString("dep");
                Asset assetDep;
                if (assetMap.containsKey(dep)) {
                    assetDep = assetMap.get(dep);
                } else {
                    assetDep = qt.getConfig(id_C,"d-"+dep,"chkin");
                    assetMap.put(dep,assetDep);
                }
                JSONObject chkGrpB;
                if (null == assetDep || null == assetDep.getChkin() || null == assetDep.getChkin().getJSONObject("objChkin")) {
                    chkGrpB = TaskObj.getChkinJava();
                } else {
                    JSONObject chkin = assetDep.getChkin();
                    JSONObject objChkin = chkin.getJSONObject("objChkin");
//                    JSONObject chkDep = objChkin.getJSONObject(dep);
                    chkGrpB = objChkin.getJSONObject(grpB);
                }
                JSONArray arrTime = chkGrpB.getJSONArray("arrTime");
                JSONArray objSb = new JSONArray();
                JSONArray objXb = new JSONArray();
                // 用于保存上一个时间段的结束时间
//                long belowTimeData = 0;
//                int priority = 0;
                timeZjService.getArrTime(arrTime,objSb,objXb);
//                for (int j = 0; j < arrTime.size(); j+=2) {
//                    String timeUpper = arrTime.getString(j);
//                    String timeBelow = arrTime.getString(j+1);
//                    String[] splitUpper = timeUpper.split(":");
//                    String[] splitBelow = timeBelow.split(":");
//                    int upper = Integer.parseInt(splitUpper[0]);
//                    int upperDivide = Integer.parseInt(splitUpper[1]);
//                    int below = Integer.parseInt(splitBelow[0]);
//                    int belowDivide = Integer.parseInt(splitBelow[1]);
//                    long upperTime = (((long) upper * 60) * 60)+((long) upperDivide * 60);
//                    long belowTime = (((long) below * 60) * 60)+((long) belowDivide * 60);
//
//                    JSONObject objSbZ = new JSONObject();
//                    objSbZ.put("priority",priority);
//                    priority++;
//                    objSbZ.put("tePStart",upperTime);
//                    objSbZ.put("tePFinish",belowTime);
//                    objSbZ.put("zon",belowTime-upperTime);
//                    objSb.add(objSbZ);
//                    if (j == 0) {
//                        if (upperTime != 0) {
//                            JSONObject objXbZ = new JSONObject();
//                            objXbZ.put("priority",-1);
//                            objXbZ.put("tePStart",0);
//                            objXbZ.put("tePFinish",upperTime);
//                            objXbZ.put("zon",upperTime);
//                            objXb.add(objXbZ);
//                        }
//                    } else if ((j + 1) + 1 >= arrTime.size()) {
//                        JSONObject objXbZ = new JSONObject();
//                        objXbZ.put("priority",-1);
//                        objXbZ.put("tePStart",belowTimeData);
//                        objXbZ.put("tePFinish",upperTime);
//                        objXbZ.put("zon",upperTime-belowTimeData);
//                        objXb.add(objXbZ);
//                        if (belowTime != 86400) {
//                            objXbZ = new JSONObject();
//                            objXbZ.put("priority",-1);
//                            objXbZ.put("tePStart",belowTime);
//                            objXbZ.put("tePFinish",86400);
//                            objXbZ.put("zon",86400-belowTime);
//                            objXb.add(objXbZ);
//                        }
//                    } else {
//                        JSONObject objXbZ = new JSONObject();
//                        objXbZ.put("priority",-1);
//                        objXbZ.put("tePStart",belowTimeData);
//                        objXbZ.put("tePFinish",upperTime);
//                        objXbZ.put("zon",upperTime-belowTimeData);
//                        objXb.add(objXbZ);
//                    }
//                    belowTimeData = belowTime;
//                }
                // 创建时间处理打卡信息存储
                xbAndSb = new JSONObject();
                // 添加信息
                xbAndSb.put("xb",objXb);
                xbAndSb.put("sb",objSb);

                // 根据部门，获取部门对应的全局职位人数信息
                JSONObject depAllInfo = grpUNumAll.getJSONObject(dep);
                // 判断部门全局职位人数信息为空
                if (null == depAllInfo) {
                    // 创建部门全局职位人数信息
                    depAllInfo = new JSONObject();
                    // 根据组别添加职位人数
                    depAllInfo.put(grpB,1);
                    grpUNumAll.put(dep,depAllInfo);
                } else {
                    // 直接根据组别获取全局职位人数
                    Integer grpBAllInfo = depAllInfo.getInteger(grpB);
                    // 判断为空
                    if (null == grpBAllInfo) {
                        // 添加全局职位人数信息
                        depAllInfo.put(grpB,grpBAllInfo);
                        grpUNumAll.put(dep,depAllInfo);
                    }
                }

                // 根据部门获取全局上班下班信息
                JSONObject depAllChKin = xbAndSbAll.getJSONObject(dep);
                // 判断为空
                if (null == depAllChKin) {
                    // 创建
                    depAllChKin = new JSONObject();
                    // 添加全局上班下班信息
                    depAllChKin.put(grpB,xbAndSb);
                    xbAndSbAll.put(dep,depAllChKin);
                } else {
                    // 根据组别获取全局上班下班信息
                    JSONObject grpBAllChKin = depAllChKin.getJSONObject(grpB);
                    if (null == grpBAllChKin) {
                        // 添加全局上班下班信息
                        depAllChKin.put(grpB,xbAndSb);
                        xbAndSbAll.put(dep,depAllChKin);
                    }
                }

                // 添加部门信息
                depMap.put("dep",dep);
                depMap.put("id_O",id_OInside);
                // 添加信息
                grpBGroupIdOJ.put(grpB,depMap);
            }
        }
        // 获取进度卡片信息
//        JSONObject action = salesOrderData.getAction();
        JSONObject casItemx = salesOrderData.getCasItemx();
        // 获取时间处理数据集合
//        JSONArray oDates = action.getJSONArray("oDates");
        JSONArray oDates = casItemx.getJSONObject("java").getJSONArray("oDates");
        // 获取时间处理任务集合
//        JSONArray oTasks = action.getJSONArray("oTasks");
        System.out.println("开始开始输出:");
//        System.out.println(JSON.toJSONString(oDates));
//        System.out.println(JSON.toJSONString(oTasks));
        // 创建存储物料下标
        JSONArray materialIndex = new JSONArray();
        // 遍历数据集合
        for (int i = 0; i < oDates.size(); i++) {
            // 获取当前i数据
            JSONObject oDate = oDates.getJSONObject(i);
            // 获取类别
            Integer bmdpt = oDate.getInteger("bmdpt");
//            // 判断不为空
//            if (null == bmdpt) {
//                // 调用方法获取订单信息
////                Order orderData = coupaUtil.getOrderByListKey(
////                        oDate.getString("id_O"), Collections.singletonList("action"));
//                Order orderData = qt.getMDContent(oDate.getString("id_O"),"action", Order.class);
//                // 获取进度卡片数据
//                JSONObject actionNew = orderData.getAction();
//                // 获取进度信息
//                JSONArray objAction = actionNew.getJSONArray("objAction");
//                // 获取类别
//                bmdpt = objAction.getJSONObject(oDate.getInteger("index")).getInteger("bmdpt");
//            }
            // 判断是物料
            if (bmdpt == 3) {
                // 添加下标
                materialIndex.add(i);
            }
        }
        System.out.println(JSON.toJSONString(materialIndex));
        // 创建存储最大时间
        long maxTeDurTotal = 0;

        // 降序遍历物料下标集合
        for (int i = materialIndex.size()-1; i >= 0; i--) {
            // 获取下标
            int index = materialIndex.getInteger(i);
            // 获取时间处理数据
            JSONObject oDate = oDates.getJSONObject(index);


            // 获取时间处理的实际准备时间
            Long wntPrep = oDate.getLong("wntPrep");
            Long wntDur = oDate.getLong("wntDur");
            Double wn2qtyneed = oDate.getDouble("wn2qtyneed");
//            System.out.println(JSON.toJSONString(oDa));
            // 存储任务总时间
            long taskTotalTime = (long)(wntDur * wn2qtyneed);
            // 获取时间处理的组别
            String grpB = oDate.getString("grpB");
            // 根据组别获取部门
            String dep = grpBGroupIdOJ.getJSONObject(grpB).getString("dep");
            oDate.put("dep",dep);
            oDate.put("grpUNum",getObjGrpUNum(grpB,dep,oDate.getString("id_C"),grpUNumAll));
//            System.out.println("wntDur:"+wntDur+",wn2qty:"+wn2qtyneed+",l:"+l);
            long grpUNum;
            // 计算总时间
            if (taskTotalTime % oDate.getInteger("grpUNum") == 0) {
                grpUNum = taskTotalTime / oDate.getInteger("grpUNum");
            } else {
                grpUNum = (long) Math.ceil((double) (taskTotalTime / oDate.getInteger("grpUNum")));
            }
            // 获取时间处理的总任务时间
            long wntDurTotal = grpUNum+wntPrep;


            if (wntDurTotal > maxTeDurTotal) {
                maxTeDurTotal = wntDurTotal;
            }
//            oDates.remove(index);
//            oTasks.remove(index);
        }
        System.out.println("最后输出:"+(teStart+maxTeDurTotal)+" - "+maxTeDurTotal);
//        System.out.println(JSON.toJSONString(oDates));
//        System.out.println(JSON.toJSONString(oTasks));
        // (数据类型)(最小值+Math.random()*(最大值-最小值+1))
        //   (int)  ( 1 + Math.random()* (  6 - 1 + 1))
        int i = (int) (1 + Math.random() * (6 - 1 + 1));
        System.out.println("i:"+i+" - maxTeDurTotal:"+maxTeDurTotal);
        maxTeDurTotal += i * 86400L;
        System.out.println("maxTeDurTotal:"+maxTeDurTotal);

//        // 创建请求更改参数
//        JSONObject mapKey = new JSONObject();
//        // 添加请求更改参数信息
//        mapKey.put("action.oDates",oDates);
//        mapKey.put("action.oTasks",oTasks);
////        mapKey.put("info.test","测试1111");
//        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);

        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), (teStart+maxTeDurTotal));
    }

    /**
     * 时间处理方法
     * @param id_O  主订单编号
     * @param teStart   开始时间
     * @param id_C  公司编号
     * @param wn0TPrior 优先级
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 1:26
     */
    public ApiResponse getAtFirst(String id_O, Long teStart, String id_C, Integer wn0TPrior){
        if (isTest)
            // 调用根据公司编号清空所有任务信息方法
            setTaskAndZonKai(id_C);
//        if (isTestAddOrder) {
//            // 调用添加测试数据方法
//            TaskObj.addOrder(teStart,qt);
//            TaskObj.addOrder2(teStart,qt);
//            TaskObj.addOrder3(teStart,qt);
//            // 全部任务存储
//            JSONObject objTaskAll = new JSONObject();
//            // 调用添加测试数据方法
//            TaskObj.addTasks(teStart,"1001","1xx2",id_C,objTaskAll);
//            TaskObj.addTasksAndOrder(teStart,id_C,objTaskAll);
//            TaskObj.addTasksAndOrder3(teStart,id_C,objTaskAll);
//
//            // 调用任务最后处理方法
//            timeZjServiceComprehensive.taskLastHandle(new JSONObject(),id_C,new ObjectId().toString(),objTaskAll
//                    ,new JSONObject(),new JSONObject(),new HashMap<>(16),new JSONObject()
//                    ,id_O,new JSONArray(),new JSONObject(),new JSONObject(),null);
//        }
//        atFirst(wn0TPrior,teStart,id_O,id_C);
        System.out.println();
//        TimeZj.isZ = 6;
//        System.out.println(id_O);
//        // 调用方法获取订单信息
//        Order salesOrderData = qt.getMDContent(id_O,qt.strList("oItem", "info", "view", "action", "casItemx"), Order.class);
//        System.out.println("--------");
//        System.out.println(JSON.toJSONString(salesOrderData));
//        // 判断订单是否为空
//        if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
//                || null == salesOrderData.getCasItemx()) {
//            // 返回为空错误信息
//            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
//        }
//        // 定义，存储进入未操作到的地方记录
//        JSONObject recordNoOperation = new JSONObject();
//        // 存储任务所在日期
//        JSONObject storageTaskWhereTime = new JSONObject();
//        // 镜像任务存储
//        Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks = new HashMap<>(16);
//        // 镜像总时间存储
//        JSONObject allImageTotalTime = new JSONObject();
//        // 全部任务存储
//        JSONObject objTaskAll = new JSONObject();
//        // 判断是测试
//        if (isTest) {
//            // 调用根据公司编号清空所有任务信息方法
//            setTaskAndZonKai(id_C);
//            // 调用添加测试数据方法
//            TaskObj.addOrder(teStart,qt);
//            TaskObj.addOrder2(teStart,qt);
//            TaskObj.addOrder3(teStart,qt);
//            // 调用添加测试数据方法
//            TaskObj.addTasks(teStart,"1001","1xx2",id_C,objTaskAll);
//            TaskObj.addTasksAndOrder(teStart,id_C,objTaskAll);
//            TaskObj.addTasksAndOrder3(teStart,id_C,objTaskAll);
//
////            // 根据键（订单id）获取订单信息
////            Order order = coupaUtil.getOrderByListKey("t-1", Arrays.asList("action","info"));
////            System.out.println("输出订单:");
////            System.out.println(JSON.toJSONString(order));
//
////            // 获取唯一下标
//////        String random = MongoUtils.GetObjectId();
////            String random = new ObjectId().toString();
//            // 获取全局唯一下标
////        String randomAll = MongoUtils.GetObjectId();
//            String randomAll = new ObjectId().toString();
//
//            // 调用任务最后处理方法
//            timeZjServiceComprehensive.taskLastHandle(new JSONObject(),id_C,randomAll,objTaskAll
//                    ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,id_O,new JSONArray()
//                    ,new JSONObject(),new JSONObject(),null);
//
////            // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
////            onlyFirstTimeStamp.remove(random);
////            // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
////            newestLastCurrentTimestamp.remove(random);
////            // 根据当前唯一标识删除信息
////            onlyRefState.remove(random);
////            // 抛出操作成功异常
////            return retResult.ok(CodeEnum.OK.getCode(), "新增测试时间处理成功!");
//            objTaskAll = new JSONObject();
//            storageTaskWhereTime = new JSONObject();
//            allImageTasks = new HashMap<>(16);
//            allImageTotalTime = new JSONObject();
//        }
//        // 根据组别存储部门信息
//        JSONObject grpBGroupIdOJ = new JSONObject();
//        // 存储casItemx内订单列表的订单action数据
//        JSONObject actionIdO = new JSONObject();
//        // 存储部门对应组别的职位总人数
//        JSONObject grpUNumAll = new JSONObject();
//        // 存储部门对应组别的上班和下班时间
//        JSONObject xbAndSbAll = new JSONObject();
//        // 统一id_O和index存储记录状态信息
//        JSONObject recordId_OIndexState = new JSONObject();
//        // 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
//        JSONObject onlyRefState = new JSONObject();
//        // 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
//        JSONObject newestLastCurrentTimestamp = new JSONObject();
//        // 存储当前唯一编号的第一个当前时间戳
//        JSONObject onlyFirstTimeStamp = new JSONObject();
//        // 获取唯一下标
//        String random = new ObjectId().toString();
//        // 获取全局唯一下标
//        String randomAll = new ObjectId().toString();
//
//        // 设置问题记录的初始值
//        yiShu.put(randomAll,0);
//        leiW.put(randomAll,0);
//        xin.put(randomAll,0);
//        isQzTz.put(randomAll,0);
//        recordNoOperation.put(randomAll,new JSONArray());
//
//        // 设置存储当前唯一编号的第一个当前时间戳
//        onlyFirstTimeStamp.put(random,teStart);
//        // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
//        onlyRefState.put(random,0);
//        // 存储最初开始时间
//        long initialStartTime = 0L;
//        // 存储最后结束时间
//        long lastEndTime = 0L;
//        // 获取递归订单列表
//        JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
//        // 存储递归订单列表的订单编号集合
//        JSONArray objOrderList = new JSONArray();
//        JSONObject depAllTime = new JSONObject();
//
////        Asset asset = qt.getConfig(id_C,"a-chkin","chkin00s");
////        // 获取打卡卡片信息
////        JSONObject chkin00s = asset.getChkin00s();
////        // 获取职位人数信息
////        JSONObject objDepInfo = chkin00s.getJSONObject("objZw");
////        // 存储判断职位人数信息是否为空
////        boolean isDepNull = null != objDepInfo;
////        // 获取打卡信息
////        JSONArray objData = chkin00s.getJSONArray("objData");
////        // 存储判断打卡信息是否为空
////        boolean isXbAndSbNull = true;
////        // 存储判断是否是否有时间处理打卡信息
////        boolean isTimeChKin = false;
////        // 定义存储时间处理打卡时间字典
////        JSONObject xbAndSb = null;
////        // 定义存储时间处理打卡信息下标
////        int chKinInfoIndex = -1;
////        // 判断打卡信息为空
////        if (null == objData) {
////            isXbAndSbNull = false;
////        } else {
////            // 遍历打卡信息
////            for (int j = 0; j < objData.size(); j++) {
////                // 根据j获取对应的打卡信息
////                JSONObject chKinInfo = objData.getJSONObject(j);
////                // 判断是否是时间处理打卡信息
////                if (null != chKinInfo.getInteger("timeP")) {
////                    // 设置为是
////                    isTimeChKin = true;
////                    // 获取下标位置
////                    chKinInfoIndex = j;
////                    // 创建时间处理打卡信息存储
////                    xbAndSb = new JSONObject();
////                    // 获取时间处理打卡的上班和下班信息
////                    JSONObject objDataZ = objData.getJSONObject(j);
////                    // 添加信息
////                    xbAndSb.put("xb",objDataZ.getJSONArray("objXb"));
////                    xbAndSb.put("sb",objDataZ.getJSONArray("objSb"));
////                    break;
////                }
////            }
////        }
////        // 获取部门组别对应时间处理打卡信息下标字典
////        JSONObject objWorkTime = chkin00s.getJSONObject("objWorkTime");
////        // 判断为空
////        if (null == objWorkTime) {
////            isXbAndSbNull = false;
////        }
////        // 遍历订单列表
////        for (int i = 0; i < objOrder.size(); i++) {
////            // 获取订单列表的订单编号
////            String id_OInside = objOrder.getJSONObject(i).getString("id_O");
////            // 判断订单等于主订单，则通过循环
////            if (id_OInside.equals(id_O)) {
////                continue;
////            }
////            // 添加订单编号
////            objOrderList.add(id_OInside);
////            // 根据订单编号查询action卡片信息
//////            Order insideAction = coupaUtil.getOrderByListKey(id_OInside, Collections.singletonList("action"));
////            Order insideAction = qt.getMDContent(id_OInside,"action", Order.class);
////            // 获取递归信息
////            JSONArray objAction = insideAction.getAction().getJSONArray("objAction");
////            // 获取组别对应部门信息
////            JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
////            // 遍历组别对应部门信息
////            for (String grpB : grpBGroup.keySet()) {
////                // 创建存储部门字典
////                JSONObject depMap = new JSONObject();
////                // 根据组别获取组别信息
////                JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpB);
////                // 获取组别的部门
////                String dep = grpBGroupInfo.getString("dep");
////                // 判断职位人数不为空
////                if (isDepNull) {
////                    // 根据部门获取职位人数部门信息
////                    JSONObject depInfo = objDepInfo.getJSONObject(dep);
////                    // 判断不为空
////                    if (null != depInfo) {
////                        // 根据组别，获取职位人数部门对应的组别信息
////                        Integer grpBInfo = objDepInfo.getInteger(grpB);
////                        if (null != grpBInfo) {
////                            // 根据部门，获取部门对应的全局职位人数信息
////                            JSONObject depAllInfo = grpUNumAll.getJSONObject(dep);
////                            // 判断部门全局职位人数信息为空
////                            if (null == depAllInfo) {
////                                // 创建部门全局职位人数信息
////                                depAllInfo = new JSONObject();
////                                // 根据组别添加职位人数
////                                depAllInfo.put(grpB,grpBInfo);
////                                grpUNumAll.put(dep,depAllInfo);
////                            } else {
////                                // 直接根据组别获取全局职位人数
////                                Integer grpBAllInfo = depAllInfo.getInteger(grpB);
////                                // 判断为空
////                                if (null == grpBAllInfo) {
////                                    // 添加全局职位人数信息
////                                    depAllInfo.put(grpB,grpBAllInfo);
////                                    grpUNumAll.put(dep,depAllInfo);
////                                }
////                            }
////                        }
////                    }
////                }
////                // 判断上班下班时间不为空，并且有时间处理打卡信息
////                if (isXbAndSbNull && isTimeChKin) {
////                    // 根据部门获取上班下班信息
////                    JSONObject depChKin = objWorkTime.getJSONObject(dep);
////                    if (null != depChKin) {
////                        // 根据组别获取上班下班信息
////                        Integer grpBChKin = depChKin.getInteger(grpB);
////                        // 判断上班下班信息不为空，并且，下标位置等于时间处理打卡信息的下标
////                        if (null != grpBChKin && grpBChKin == chKinInfoIndex) {
////                            // 根据部门获取全局上班下班信息
////                            JSONObject depAllChKin = xbAndSbAll.getJSONObject(dep);
////                            // 判断为空
////                            if (null == depAllChKin) {
////                                // 创建
////                                depAllChKin = new JSONObject();
////                                // 添加全局上班下班信息
////                                depAllChKin.put(grpB,xbAndSb);
////                                xbAndSbAll.put(dep,depAllChKin);
////                            } else {
////                                // 根据组别获取全局上班下班信息
////                                JSONObject grpBAllChKin = depAllChKin.getJSONObject(grpB);
////                                if (null == grpBAllChKin) {
////                                    // 添加全局上班下班信息
////                                    depAllChKin.put(grpB,xbAndSb);
////                                    xbAndSbAll.put(dep,depAllChKin);
////                                }
////                            }
////                        }
////                    }
////                }
////                // 添加部门信息
////                depMap.put("dep",dep);
////                depMap.put("id_O",id_OInside);
////                // 添加信息
////                grpBGroupIdOJ.put(grpB,depMap);
////            }
////            // 根据订单编号添加订单信息存储
////            actionIdO.put(id_OInside,objAction);
////        }
//        System.out.println("--- 分割 ---");
////        Asset asset = qt.getConfig(id_C,"a-chkin","chkin");
////        // 获取打卡卡片信息
////        JSONObject chkin = asset.getChkin();
////        // 获取职位人数信息
////        JSONObject objChkin = chkin.getJSONObject("objChkin");
////        // 存储判断职位人数信息是否为空
////        boolean isDepNull = null != objChkin;
////        // 获取打卡信息
////        JSONArray objData = chkin00s.getJSONArray("objData");
////        // 存储判断打卡信息是否为空
////        boolean isXbAndSbNull = true;
////        // 存储判断是否是否有时间处理打卡信息
////        boolean isTimeChKin = false;
//        // 定义存储时间处理打卡时间字典
//        JSONObject xbAndSb;
////        // 定义存储时间处理打卡信息下标
////        int chKinInfoIndex = -1;
////        // 判断打卡信息为空
////        if (null == objChkin) {
////            isXbAndSbNull = false;
////        } else {
////            // 遍历打卡信息
////            for (int j = 0; j < objData.size(); j++) {
////                // 根据j获取对应的打卡信息
////                JSONObject chKinInfo = objData.getJSONObject(j);
////                // 判断是否是时间处理打卡信息
////                if (null != chKinInfo.getInteger("timeP")) {
////                    // 设置为是
////                    isTimeChKin = true;
////                    // 获取下标位置
////                    chKinInfoIndex = j;
////                    // 创建时间处理打卡信息存储
////                    xbAndSb = new JSONObject();
////                    // 获取时间处理打卡的上班和下班信息
////                    JSONObject objDataZ = objData.getJSONObject(j);
////                    // 添加信息
////                    xbAndSb.put("xb",objDataZ.getJSONArray("objXb"));
////                    xbAndSb.put("sb",objDataZ.getJSONArray("objSb"));
////                    break;
////                }
////            }
////        }
////        // 获取部门组别对应时间处理打卡信息下标字典
////        JSONObject objWorkTime = chkin00s.getJSONObject("objWorkTime");
////        // 判断为空
////        if (null == objWorkTime) {
////            isXbAndSbNull = false;
////        }
//        // 遍历订单列表
//        Map<String,Asset> assetMap = new HashMap<>();
//        for (int i = 0; i < objOrder.size(); i++) {
//            // 获取订单列表的订单编号
//            String id_OInside = objOrder.getJSONObject(i).getString("id_O");
//            // 判断订单等于主订单，则通过循环
//            if (id_OInside.equals(id_O)) {
//                continue;
//            }
//            // 添加订单编号
//            objOrderList.add(id_OInside);
//            // 根据订单编号查询action卡片信息
//            Order insideAction = qt.getMDContent(id_OInside,"action", Order.class);
//            // 获取递归信息
//            JSONArray objAction = insideAction.getAction().getJSONArray("objAction");
//            // 获取组别对应部门信息
//            JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
//            getGrpB(grpBGroup,assetMap,id_C,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,id_OInside);
////            // 遍历组别对应部门信息
////            for (String grpB : grpBGroup.keySet()) {
////                // 创建存储部门字典
////                JSONObject depMap = new JSONObject();
////                // 根据组别获取组别信息
////                JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpB);
////                // 获取组别的部门
////                String dep = grpBGroupInfo.getString("dep");
////                Asset assetDep;
////                if (assetMap.containsKey(dep)) {
////                    assetDep = assetMap.get(dep);
////                } else {
////                    assetDep = qt.getConfig(id_C,"d-"+dep,"chkin");
////                    assetMap.put(dep,assetDep);
////                }
////                JSONObject chkGrpB;
////                if (null == assetDep || null == assetDep.getChkin() || null == assetDep.getChkin().getJSONObject("objChkin")) {
////                    chkGrpB = TaskObj.getChkinJava();
////                } else {
////                    JSONObject chkin = assetDep.getChkin();
////                    JSONObject objChkin = chkin.getJSONObject("objChkin");
////                    chkGrpB = objChkin.getJSONObject(grpB);
////                }
////                JSONArray arrTime = chkGrpB.getJSONArray("arrTime");
////                Integer wn0UserCount = chkGrpB.getInteger("wn0UserCount");
////                JSONArray objSb = new JSONArray();
////                JSONArray objXb = new JSONArray();
////                // 用于保存上一个时间段的结束时间
////                long belowTimeData = 0;
////                int priority = 0;
////                long allTime = 0;
////                for (int j = 0; j < arrTime.size(); j+=2) {
////                    String timeUpper = arrTime.getString(j);
////                    String timeBelow = arrTime.getString(j+1);
////                    String[] splitUpper = timeUpper.split(":");
////                    String[] splitBelow = timeBelow.split(":");
////                    int upper = Integer.parseInt(splitUpper[0]);
////                    int upperDivide = Integer.parseInt(splitUpper[1]);
////                    int below = Integer.parseInt(splitBelow[0]);
////                    int belowDivide = Integer.parseInt(splitBelow[1]);
////                    long upperTime = (((long) upper * 60) * 60)+((long) upperDivide * 60);
////                    long belowTime = (((long) below * 60) * 60)+((long) belowDivide * 60);
////                    JSONObject objSbZ = new JSONObject();
////                    objSbZ.put("priority",priority);
////                    priority++;
////                    objSbZ.put("tePStart",upperTime);
////                    objSbZ.put("tePFinish",belowTime);
////                    objSbZ.put("zon",belowTime-upperTime);
////                    allTime+=(belowTime-upperTime);
////                    objSb.add(objSbZ);
////                    if (j == 0) {
////                        if (upperTime != 0) {
////                            JSONObject objXbZ = new JSONObject();
////                            objXbZ.put("priority",-1);
////                            objXbZ.put("tePStart",0);
////                            objXbZ.put("tePFinish",upperTime);
////                            objXbZ.put("zon",upperTime);
////                            objXb.add(objXbZ);
////                        }
////                    } else if ((j + 1) + 1 >= arrTime.size()) {
////                        JSONObject objXbZ = new JSONObject();
////                        objXbZ.put("priority",-1);
////                        objXbZ.put("tePStart",belowTimeData);
////                        objXbZ.put("tePFinish",upperTime);
////                        objXbZ.put("zon",upperTime-belowTimeData);
////                        objXb.add(objXbZ);
////                        if (belowTime != 86400) {
////                            objXbZ = new JSONObject();
////                            objXbZ.put("priority",-1);
////                            objXbZ.put("tePStart",belowTime);
////                            objXbZ.put("tePFinish",86400);
////                            objXbZ.put("zon",86400-belowTime);
////                            objXb.add(objXbZ);
////                        }
////                    } else {
////                        JSONObject objXbZ = new JSONObject();
////                        objXbZ.put("priority",-1);
////                        objXbZ.put("tePStart",belowTimeData);
////                        objXbZ.put("tePFinish",upperTime);
////                        objXbZ.put("zon",upperTime-belowTimeData);
////                        objXb.add(objXbZ);
////                    }
////                    belowTimeData = belowTime;
////                }
////                if (!depAllTime.containsKey(dep)) {
////                    depAllTime.put(dep,allTime);
////                }
////                // 创建时间处理打卡信息存储
////                xbAndSb = new JSONObject();
////                // 添加信息
////                xbAndSb.put("xb",objXb);
////                xbAndSb.put("sb",objSb);
////
////                // 根据部门，获取部门对应的全局职位人数信息
////                JSONObject depAllInfo = grpUNumAll.getJSONObject(dep);
////                // 判断部门全局职位人数信息为空
////                if (null == depAllInfo) {
////                    // 创建部门全局职位人数信息
////                    depAllInfo = new JSONObject();
////                    // 根据组别添加职位人数
////                    depAllInfo.put(grpB,wn0UserCount);
////                    grpUNumAll.put(dep,depAllInfo);
////                } else {
////                    // 直接根据组别获取全局职位人数
////                    Integer grpBAllInfo = depAllInfo.getInteger(grpB);
////                    // 判断为空
////                    if (null == grpBAllInfo) {
////                        // 添加全局职位人数信息
////                        depAllInfo.put(grpB,wn0UserCount);
////                        grpUNumAll.put(dep,depAllInfo);
////                    }
////                }
////
////                // 根据部门获取全局上班下班信息
////                JSONObject depAllChKin = xbAndSbAll.getJSONObject(dep);
////                // 判断为空
////                if (null == depAllChKin) {
////                    // 创建
////                    depAllChKin = new JSONObject();
////                    // 添加全局上班下班信息
////                    depAllChKin.put(grpB,xbAndSb);
////                    xbAndSbAll.put(dep,depAllChKin);
////                } else {
////                    // 根据组别获取全局上班下班信息
////                    JSONObject grpBAllChKin = depAllChKin.getJSONObject(grpB);
////                    if (null == grpBAllChKin) {
////                        // 添加全局上班下班信息
////                        depAllChKin.put(grpB,xbAndSb);
////                        xbAndSbAll.put(dep,depAllChKin);
////                    }
////                }
////                // 添加部门信息
////                depMap.put("dep",dep);
////                depMap.put("id_O",id_OInside);
////                // 添加信息
////                grpBGroupIdOJ.put(grpB,depMap);
////            }
//            // 根据订单编号添加订单信息存储
//            actionIdO.put(id_OInside,objAction);
//        }
//        System.out.println("xbAndSbAll:");
//        System.out.println(JSON.toJSONString(xbAndSbAll));
//
//        JSONObject casItemx = salesOrderData.getCasItemx();
//        // 获取递归存储的时间处理信息
//        JSONArray oDates = casItemx.getJSONObject("java").getJSONArray("oDates");
//        // 获取递归存储的时间任务信息
//        JSONArray oTasks = casItemx.getJSONObject("java").getJSONArray("oTasks");
//        // 用于存储时间冲突的副本
//        JSONObject timeConflictCopy = new JSONObject();
//        // 用于存储判断镜像是否是第一个被冲突的产品
//        JSONObject sho = new JSONObject();
//        // 用于存储控制只进入一次的判断，用于记录第一个数据处理的结束时间
//        boolean canOnlyEnterOnce = true;
//        // 定义用来存储最大结束时间
//        long maxSte = 0;
//        // 用于存储每一个时间任务的结束时间
//        JSONArray teFinList = new JSONArray();
//        // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
//        JSONObject serialOneFatherLastTime = new JSONObject();
//        // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的预计开始时间
//        JSONObject serialOneFatherStartTime = new JSONObject();
//        // 清理状态
//        JSONObject clearStatus = new JSONObject();
//        // 当前处理信息
//        JSONObject thisInfo = new JSONObject();
//        setThisInfoRef(thisInfo,"time");
//        // 镜像任务所在时间
//        JSONObject allImageTeDate = new JSONObject();
//
//        JSONObject resultTask = mergeTaskByPrior(oDates, oTasks, grpBGroupIdOJ, grpUNumAll);
//        oDates = resultTask.getJSONArray("oDates");
//        oTasks = resultTask.getJSONArray("oTasks");
//
////        // 抛出操作成功异常
////        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
//
//        // 遍历时间处理信息集合
//        for (int i = 0; i < oDates.size(); i++) {
//            // 获取i对应的时间处理信息
//            JSONObject oDate = oDates.getJSONObject(i);
//            // 获取订单编号
//            String id_OInside = oDate.getString("id_O");
//            // 获取订单下标
//            int indexInside = oDate.getInteger("index");
////            JSONArray objAction = actionIdO.getJSONArray(id_OInside);
////            JSONObject indexAction = objAction.getJSONObject(indexInside);
////            int bcdStatus = indexAction.getInteger("bcdStatus") == null?0:indexAction.getInteger("bcdStatus");
////            if (bcdStatus == 8 || bcdStatus == 2) {
////                continue;
////            }
////            int bcdStatus = oDate.getInteger("bcdStatus") == null?0:oDate.getInteger("bcdStatus");
////            if (bcdStatus == 5) {
////                continue;
////            }
//            // 获取时间处理的序号
//            Integer priorItem = oDate.getInteger("priorItem");
//            // 获取时间处理的父零件编号
//            String id_PF = oDate.getString("id_PF");
//            // 获取时间处理的序号是否为1层级 csSta - timeHandleSerialNoIsOne
//            Integer csSta = oDate.getInteger("csSta");
//            // 获取时间处理的判断是否是空时间信息
//            Boolean empty = oDate.getBoolean("empty");
//            // 判断当前时间处理为空时间信息
//            if (empty) {
//                // 获取时间处理的链接下标
//                Integer linkInd = oDate.getInteger("linkInd");
//                // 根据链接下标获取指定的结束时间
//                Long indexEndTime = teFinList.getLong(linkInd);
//                // 判断父id的预计开始时间为空，并且序号为第一个
//                if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1) {
//                    serialOneFatherStartTime.put(id_PF,indexEndTime);
//                }
//                // 根据父零件编号获取序号信息
//                JSONObject fatherSerialInfo = serialOneFatherLastTime.getJSONObject(id_PF);
//                // 判断序号信息为空
//                if (null == fatherSerialInfo) {
//                    // 创建序号信息
//                    fatherSerialInfo = new JSONObject();
//                    // 添加序号的结束时间，默认为0
//                    fatherSerialInfo.put(priorItem.toString(),0);
//                }
//                // 获取序号结束时间
//                Long serialEndTime = fatherSerialInfo.getLong(priorItem.toString());
//                // 添加链接结束时间到当前空时间处理结束时间列表内
//                teFinList.add(indexEndTime);
//                // 判断链接结束时间大于当前结束时间
//                if (indexEndTime > serialEndTime) {
//                    // 修改当前结束时间为链接结束时间
//                    fatherSerialInfo.put(priorItem.toString(),indexEndTime);
//                    // 根据父零件编号添加序号信息
//                    serialOneFatherLastTime.put(id_PF,fatherSerialInfo);
//                }
//                continue;
//            }
//
//            // 获取当前唯一ID存储时间处理的最初开始时间
//            Long hTeStart = initialStartTime;
//            // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
//            JSONObject firstConflictId_O = new JSONObject();
//            JSONObject firstConflictIndex = new JSONObject();
//            // 设置为-1代表的是递归的零件
//            firstConflictIndex.put("prodState",-1);
//            firstConflictIndex.put("z","-1");
//            firstConflictId_O.put(oDate.getString("index"),firstConflictIndex);
//            sho.put(oDate.getString("id_O"),firstConflictId_O);
//            // 获取时间处理的组别
//            String grpB = oDate.getString("grpB");
//            String dep = oDate.getString("dep");
//
//            // 获取时间处理的零件产品编号
//            String id_P = oDate.getString("id_P");
//            // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
//            Integer kaiJie = oDate.getInteger("kaiJie");
//            // 获取时间处理的实际准备时间
////            Long wntPrep = oDate.getLong("wntPrep");
//            Long wntPrep = oTasks.getJSONObject(i).getLong("prep");
//            Long wntDurTotal = oTasks.getJSONObject(i).getLong("wntDurTotal");
////            Long wntDur = oDate.getLong("wntDur");
////            Double wn2qtyneed = oDate.getDouble("wn2qtyneed");
//////            System.out.println(JSON.toJSONString(oDa));
////            // 存储任务总时间
////            long taskTotalTime = (long)(wntDur * wn2qtyneed);
//////            System.out.println("wntDur:"+wntDur+",wn2qty:"+wn2qtyneed+",l:"+l);
////            long grpUNum;
////            if (taskTotalTime % oDate.getInteger("grpUNum") == 0) {
////                grpUNum = taskTotalTime / oDate.getInteger("grpUNum");
////            } else {
////                grpUNum = (long) Math.ceil((double) (taskTotalTime / oDate.getInteger("grpUNum")));
////            }
//////            long grpUNum = taskTotalTime / oDate.getInteger("grpUNum");
////            // 获取时间处理的总任务时间
////            long wntDurTotal = grpUNum;
//////            if (bcdState == 5) {
//////                wntDurTotal = 0L;
//////                wntPrep = 0L;
//////            }
////            System.out.println("wntDurTotal:" +wntDurTotal+" - wntPrep:"
////                    +wntPrep+" - prior:"+priorItem);
////            System.out.println("csTeJTe:"+" - id_PF:"+id_PF);
////            if (wntDurTotal == 0 && wntPrep == 0) {
////                continue;
////            }
//            // 判断当前唯一ID存储时间处理的最初开始时间为0
//            if (hTeStart == 0) {
//                // 调用获取当前时间戳方法设置开始时间
//                oDate.put("teStart",getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp));
//            } else {
//                // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
//                if (csSta == 1 && kaiJie == 1) {
//                    // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
//                    hTeStart = lastEndTime;
//                }
//                oDate.put("teStart",hTeStart);
//                System.out.println("这里开始时间-1:"+hTeStart);
//            }
////            int isPriorItem = 1;
//            // 存储判断执行方法
////            boolean isExecutionMethod = (csSta == 0 && priorItem != 0) || (kaiJie != 1 && csSta == 1);
//            boolean isExecutionMethod = (csSta == 0 && priorItem != 1) || (kaiJie != 1 && csSta == 1);
//
//            // 序号是不为1层级
//            // 判断执行方法为true
//            if (isExecutionMethod) {
//                // 定义获取存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
//                JSONObject serialOneEndTime;
//                System.out.println("serialOneFatherLastTime:id_P:"+id_P+" - id_PF:"+id_PF);
//                System.out.println(JSON.toJSONString(serialOneFatherLastTime));
//                // 获取判断自己的id是否等于已存在的父id
//                boolean b = serialOneFatherLastTime.containsKey(id_P);
//                // 判断自己的id是已存在的父id
//                if (b) {
//                    // 根据自己的id获取按照父零件编号存储每个序号的最后结束时间
//                    serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_P);
//                    // 转换键信息
//                    List<String> list = new ArrayList<>(serialOneEndTime.keySet());
//                    // 获取最后一个时间信息
//                    String s = list.get(list.size() - 1);
//                    // 赋值为最后一个时间信息
//                    hTeStart = serialOneEndTime.getLong(s);
//                } else {
//                    // 根据父id获取按照父零件编号存储每个序号的最后结束时间
//                    serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_PF);
//                    if (null != serialOneEndTime) {
//                        // 获取上一个序号的时间信息并赋值
//                        hTeStart = serialOneEndTime.getLong(((priorItem - 1) + ""));
//                    }
//                }
//                // 设置开始时间
//                oDate.put("teStart",hTeStart);
//                System.out.println("这里开始时间-3:"+hTeStart);
//            }
//
//            // 获取任务的最初开始时间备份
//            Long teStartBackups = oDate.getLong("teStart");
//            // 设置最初结束时间
//            oDate.put("teFin",(teStartBackups+(wntDurTotal+wntPrep)));
//            // 获取最初结束时间
//            Long teFin = oDate.getLong("teFin");
//            // 获取任务信息，并且转换为任务类
//            Task task = JSON.parseObject(JSON.toJSONString(oTasks.get(i)),Task.class);
//            // 设置最初任务信息的时间信息
//            task.setWntDurTotal((teFin - teStartBackups));
//            task.setTePStart(teStartBackups);
//            task.setTePFinish(teFin);
//            task.setTeCsStart(teStartBackups);
//            task.setTeCsSonOneStart(0L);
//            task.setDateIndex(i);
////            task.setPrep(oDate.getLong("wntPrep"));
//            // 判断优先级不等于-1
//            if (wn0TPrior != -1) {
//                // 设置优先级为传参的优先级
//                task.setPriority(wn0TPrior);
//            }
//            // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
//            if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1
//                    && kaiJie != 5 && kaiJie != 3
////                    && !isGetIntoNull
//            ) {
//                // 根据父id添加开始时间
//                serialOneFatherStartTime.put(id_PF,task.getTeCsStart());
//            } else if (kaiJie == 3 || kaiJie == 5) {
//                // 添加子最初开始时间
//                task.setTeCsSonOneStart(serialOneFatherStartTime.getLong(id_P));
//            }
//
//            // 创建当前处理的任务的所在日期对象
//            JSONObject teDate = new JSONObject();
//            System.out.println("taskTe:");
//            System.out.println(JSON.toJSONString(task));
////            // 获取订单编号
////            String id_OInside = oDate.getString("id_O");
////            // 获取订单下标
////            Integer indexInside = oDate.getInteger("index");
////            System.out.println("外部sho:");
////            System.out.println(JSON.toJSONString(sho));
//            // 调用时间处理方法
//            JSONObject timeHandleInfo = timeHandle(task,hTeStart,grpB,dep,id_OInside,indexInside
//                    ,0,random,1,teDate,timeConflictCopy,0
//                    ,sho,0,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
//                    ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
//                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
//                    ,clearStatus,thisInfo,allImageTeDate,false,depAllTime);
//            // 更新任务最初始开始时间
//            hTeStart = timeHandleInfo.getLong("hTeStart");
//            System.out.println("最外层-2:"+hTeStart);
//            System.out.println(JSON.toJSONString(timeHandleInfo));
//            // 添加结束时间
//            teFinList.add(hTeStart);
//
//            // 根据订单编号获取递归集合
//            JSONArray dgList = actionIdO.getJSONArray(id_OInside);
//            // 根据订单下标获取递归信息并且转换为递归类
//            OrderAction orderAction = JSON.parseObject(
//                    JSON.toJSONString(dgList.getJSONObject(indexInside)),OrderAction.class);
//            // 更新递归信息
//            orderAction.setDep(dep);
//            orderAction.setGrpB(grpB);
//            orderAction.setTeDate(teDate);
//            // 将更新的递归信息写入回去
//            dgList.set(indexInside,orderAction);
//            actionIdO.put(id_OInside,dgList);
//
//            // 定义存储最后结束时间参数
//            long storageLastEndTime;
//            // 判断序号是为1层级
//            if (csSta == 1) {
//                // 获取实际结束时间
//                Long actualEndTime = timeHandleInfo.getLong("xFin");
////                System.out.println("xFinW:"+xFin);
//                // 定义存储判断实际结束时间是否为空
//                boolean isActualEndTime = false;
//                // 判断实际结束时间不等于空
//                if (null != actualEndTime) {
////                    System.out.println("xFin:"+xFin);
//                    // 赋值实际结束时间
//                    hTeStart = actualEndTime;
//                    // 判断当前实际结束时间大于最大结束时间
//                    if (actualEndTime > maxSte) {
//                        // 判断大于则更新最大结束时间为当前结束时间
//                        maxSte = actualEndTime;
//                    }
//                    // 设置不为空
//                    isActualEndTime = true;
//                } else {
//                    // 判断当前实际结束时间大于最大结束时间：注 ： xFin 和 task.getTePFinish() 有时候是不一样的，不能随便改
//                    if (task.getTePFinish() > maxSte) {
//                        // 判断大于则更新最大结束时间为当前结束时间
//                        maxSte = task.getTePFinish();
//                    }
//                }
////                System.out.println("maxSte:"+maxSte);
//                // 判断实际结束时间不为空
//                if (isActualEndTime) {
//                    // 赋值结束时间
//                    storageLastEndTime = actualEndTime;
//                } else {
//                    // 赋值结束时间
//                    storageLastEndTime = task.getTePFinish();
//                }
//                // 判断是第一次进入
//                if (canOnlyEnterOnce) {
//                    // 添加设置第一层的开始时间
//                    lastEndTime=task.getTePStart();
//                    // 设置只能进入一次
//                    canOnlyEnterOnce = false;
//                }
//            } else {
//                // 直接赋值最后结束时间
//                storageLastEndTime = hTeStart;
//            }
//            // 根据父id获取最后结束时间信息
//            JSONObject fatherGetEndTimeInfo = serialOneFatherLastTime.getJSONObject(id_PF);
//            // 判断最后结束时间信息为空
//            if (null == fatherGetEndTimeInfo) {
//                // 创建并且赋值最后结束时间
//                fatherGetEndTimeInfo = new JSONObject();
//                fatherGetEndTimeInfo.put(priorItem.toString(),0);
//            }
//            // 根据序号获取最后结束时间
//            Long aLong = fatherGetEndTimeInfo.getLong(priorItem.toString());
//            // 判断最后结束时间为空
//            if (null == aLong) {
//                // 为空，则直接添加最后结束时间信息
//                fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
//                serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
//            } else {
//                // 不为空，则判断当前最后结束时间大于已存在的最后结束时间
//                if (storageLastEndTime > aLong) {
//                    // 判断当前最后结束时间大于，则更新最后结束时间为当前结束时间
//                    fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
//                    serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
//                }
//            }
//            initialStartTime=hTeStart;
////            System.out.println();
//        }
//
//        // 调用任务最后处理方法
//        timeZjServiceComprehensive.taskLastHandle(timeConflictCopy,id_C,randomAll,objTaskAll
//                ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,id_O
//                ,objOrderList,actionIdO,allImageTeDate,depAllTime);
//
//        // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
//        onlyFirstTimeStamp.remove(random);
//        // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
//        newestLastCurrentTimestamp.remove(random);
//        // 根据当前唯一标识删除信息
//        onlyRefState.remove(random);

        // 抛出操作成功异常
        System.out.println();
//        atFirst(wn0TPrior,teStart,id_O,id_C);
        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
    }

    /**
     * 多订单时间处理方法
     * @param id_C  公司编号
     * @param orderList 订单信息
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     */
    @Override
    public ApiResponse getAtFirstList(String id_C, JSONArray orderList) {
        if (isTest)
            // 调用根据公司编号清空所有任务信息方法
            setTaskAndZonKai(id_C);
        for (int o = 0; o < orderList.size(); o++) {
            JSONObject orderInfo = orderList.getJSONObject(o);
            String id_O = orderInfo.getString("id_O");
            Integer wn0TPrior = orderInfo.getInteger("wn0TPrior");
            Long teStart = orderInfo.getLong("teStart");
            System.out.println(o+"- 订单开始:"+id_O);
//            atFirst(wn0TPrior,teStart,id_O,id_C);
            atFirstODateObj(wn0TPrior,teStart,id_O,id_C);
            System.out.println("--- 分割 ---");
//            TimeZj.isZ = 6;
//            System.out.println(id_O);
//            // 调用方法获取订单信息
//            Order salesOrderData = qt.getMDContent(id_O,qt.strList("oItem", "info", "view", "action", "casItemx"), Order.class);
//            System.out.println("--------");
//            System.out.println(JSON.toJSONString(salesOrderData));
//            // 判断订单是否为空
//            if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
//                    || null == salesOrderData.getCasItemx()) {
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
//            }
//            // 定义，存储进入未操作到的地方记录
//            JSONObject recordNoOperation = new JSONObject();
//            // 存储任务所在日期
//            JSONObject storageTaskWhereTime = new JSONObject();
//            // 镜像任务存储
//            Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks = new HashMap<>(16);
//            // 镜像总时间存储
//            JSONObject allImageTotalTime = new JSONObject();
//            // 全部任务存储
//            JSONObject objTaskAll = new JSONObject();
////            // 判断是测试
////            if (isTest) {
////                // 调用根据公司编号清空所有任务信息方法
////                setTaskAndZonKai(id_C);
////                // 调用添加测试数据方法
////                TaskObj.addOrder(teStart,qt);
////                TaskObj.addOrder2(teStart,qt);
////                TaskObj.addOrder3(teStart,qt);
////                // 调用添加测试数据方法
////                TaskObj.addTasks(teStart,"1001","1xx1",id_C,objTaskAll);
////                TaskObj.addTasksAndOrder(teStart,id_C,objTaskAll);
////                TaskObj.addTasksAndOrder3(teStart,id_C,objTaskAll);
////
////                String randomAll = new ObjectId().toString();
////
////                // 调用任务最后处理方法
////                timeZjServiceComprehensive.taskLastHandle(new JSONObject(),id_C,randomAll,objTaskAll
////                        ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,id_O,new JSONArray()
////                        ,new JSONObject(),new JSONObject(),null);
////                objTaskAll = new JSONObject();
////                storageTaskWhereTime = new JSONObject();
////                allImageTasks = new HashMap<>(16);
////                allImageTotalTime = new JSONObject();
////            }
//            // 根据组别存储部门信息
//            JSONObject grpBGroupIdOJ = new JSONObject();
//            // 存储casItemx内订单列表的订单action数据
//            JSONObject actionIdO = new JSONObject();
//            // 存储部门对应组别的职位总人数
//
//            JSONObject grpUNumAll = new JSONObject();
//            // 存储部门对应组别的上班和下班时间
//            JSONObject xbAndSbAll = new JSONObject();
//            // 统一id_O和index存储记录状态信息
//            JSONObject recordId_OIndexState = new JSONObject();
//            // 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
//            JSONObject onlyRefState = new JSONObject();
//            // 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
//            JSONObject newestLastCurrentTimestamp = new JSONObject();
//            // 存储当前唯一编号的第一个当前时间戳
//            JSONObject onlyFirstTimeStamp = new JSONObject();
//            // 获取唯一下标
//            String random = new ObjectId().toString();
//            // 获取全局唯一下标
//            String randomAll = new ObjectId().toString();
//
//            // 设置问题记录的初始值
//            yiShu.put(randomAll,0);
//            leiW.put(randomAll,0);
//            xin.put(randomAll,0);
//            isQzTz.put(randomAll,0);
//            recordNoOperation.put(randomAll,new JSONArray());
//
//            // 设置存储当前唯一编号的第一个当前时间戳
//            onlyFirstTimeStamp.put(random,teStart);
//            // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
//            onlyRefState.put(random,0);
//            // 存储最初开始时间
//            long initialStartTime = 0L;
//            // 存储最后结束时间
//            long lastEndTime = 0L;
//            // 获取递归订单列表
//            JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
//            // 存储递归订单列表的订单编号集合
//            JSONArray objOrderList = new JSONArray();
//            JSONObject depAllTime = new JSONObject();
//            System.out.println("--- 分割 ---");
//            // 遍历订单列表
//            Map<String,Asset> assetMap = new HashMap<>();
//            for (int i = 0; i < objOrder.size(); i++) {
//                // 获取订单列表的订单编号
//                String id_OInside = objOrder.getJSONObject(i).getString("id_O");
//                // 判断订单等于主订单，则通过循环
//                if (id_OInside.equals(id_O)) {
//                    continue;
//                }
//                // 添加订单编号
//                objOrderList.add(id_OInside);
//                // 根据订单编号查询action卡片信息
//                Order insideAction = qt.getMDContent(id_OInside,"action", Order.class);
//                // 获取递归信息
//                JSONArray objAction = insideAction.getAction().getJSONArray("objAction");
//                // 获取组别对应部门信息
//                JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
//                getGrpB(grpBGroup,assetMap,id_C,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,id_OInside);
//                // 根据订单编号添加订单信息存储
//                actionIdO.put(id_OInside,objAction);
//            }
//            System.out.println("xbAndSbAll:");
//            System.out.println(JSON.toJSONString(xbAndSbAll));
//
//            JSONObject casItemx = salesOrderData.getCasItemx();
//            // 获取递归存储的时间处理信息
//            JSONArray oDates = casItemx.getJSONObject("java").getJSONArray("oDates");
//            // 获取递归存储的时间任务信息
//            JSONArray oTasks = casItemx.getJSONObject("java").getJSONArray("oTasks");
//            // 用于存储时间冲突的副本
//            JSONObject timeConflictCopy = new JSONObject();
//            // 用于存储判断镜像是否是第一个被冲突的产品
//            JSONObject sho = new JSONObject();
//            // 用于存储控制只进入一次的判断，用于记录第一个数据处理的结束时间
//            boolean canOnlyEnterOnce = true;
//            // 定义用来存储最大结束时间
//            long maxSte = 0;
//            // 用于存储每一个时间任务的结束时间
//            JSONArray teFinList = new JSONArray();
//            // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
//            JSONObject serialOneFatherLastTime = new JSONObject();
//            // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的预计开始时间
//            JSONObject serialOneFatherStartTime = new JSONObject();
//            // 清理状态
//            JSONObject clearStatus = new JSONObject();
//            // 当前处理信息
//            JSONObject thisInfo = new JSONObject();
//            setThisInfoRef(thisInfo,"time");
//            // 镜像任务所在时间
//            JSONObject allImageTeDate = new JSONObject();
//
//            JSONObject resultTask = mergeTaskByPrior(oDates, oTasks, grpBGroupIdOJ, grpUNumAll);
//            oDates = resultTask.getJSONArray("oDates");
//            oTasks = resultTask.getJSONArray("oTasks");
//
////        // 抛出操作成功异常
////        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
//
//            // 遍历时间处理信息集合
//            for (int i = 0; i < oDates.size(); i++) {
//                // 获取i对应的时间处理信息
//                JSONObject oDate = oDates.getJSONObject(i);
////                Integer bmdpt = oDate.getInteger("bmdpt");
////                if (bmdpt == 3) {
////                    continue;
////                }
//                // 获取订单编号
//                String id_OInside = oDate.getString("id_O");
//                // 获取订单下标
//                int indexInside = oDate.getInteger("index");
////                JSONArray objAction = actionIdO.getJSONArray(id_OInside);
////                JSONObject indexAction = objAction.getJSONObject(indexInside);
////                int bcdStatus = indexAction.getInteger("bcdStatus") == null?0:indexAction.getInteger("bcdStatus");
////                if (bcdStatus == 8 || bcdStatus == 2) {
////                    continue;
////                }
//                // 获取时间处理的序号
//                Integer priorItem = oDate.getInteger("priorItem");
//                // 获取时间处理的父零件编号
//                String id_PF = oDate.getString("id_PF");
//                // 获取时间处理的序号是否为1层级 csSta - timeHandleSerialNoIsOne
//                Integer csSta = oDate.getInteger("csSta");
//                // 获取时间处理的判断是否是空时间信息
//                Boolean empty = oDate.getBoolean("empty");
//                // 判断当前时间处理为空时间信息
//                if (empty) {
//                    // 获取时间处理的链接下标
//                    Integer linkInd = oDate.getInteger("linkInd");
//                    // 根据链接下标获取指定的结束时间
//                    Long indexEndTime = teFinList.getLong(linkInd);
//                    // 判断父id的预计开始时间为空，并且序号为第一个
//                    if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1) {
//                        serialOneFatherStartTime.put(id_PF,indexEndTime);
//                    }
//                    // 根据父零件编号获取序号信息
//                    JSONObject fatherSerialInfo = serialOneFatherLastTime.getJSONObject(id_PF);
//                    // 判断序号信息为空
//                    if (null == fatherSerialInfo) {
//                        // 创建序号信息
//                        fatherSerialInfo = new JSONObject();
//                        // 添加序号的结束时间，默认为0
//                        fatherSerialInfo.put(priorItem.toString(),0);
//                    }
//                    // 获取序号结束时间
//                    Long serialEndTime = fatherSerialInfo.getLong(priorItem.toString());
//                    // 添加链接结束时间到当前空时间处理结束时间列表内
//                    teFinList.add(indexEndTime);
//                    // 判断链接结束时间大于当前结束时间
//                    if (indexEndTime > serialEndTime) {
//                        // 修改当前结束时间为链接结束时间
//                        fatherSerialInfo.put(priorItem.toString(),indexEndTime);
//                        // 根据父零件编号添加序号信息
//                        serialOneFatherLastTime.put(id_PF,fatherSerialInfo);
//                    }
//                    continue;
//                }
//
//                // 获取当前唯一ID存储时间处理的最初开始时间
//                Long hTeStart = initialStartTime;
//                // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
//                JSONObject firstConflictId_O = new JSONObject();
//                JSONObject firstConflictIndex = new JSONObject();
//                // 设置为-1代表的是递归的零件
//                firstConflictIndex.put("prodState",-1);
//                firstConflictIndex.put("z","-1");
//                firstConflictId_O.put(oDate.getString("index"),firstConflictIndex);
//                sho.put(oDate.getString("id_O"),firstConflictId_O);
//                // 获取时间处理的组别
//                String grpB = oDate.getString("grpB");
//                String dep = oDate.getString("dep");
//
//                // 获取时间处理的零件产品编号
//                String id_P = oDate.getString("id_P");
//                // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
//                Integer kaiJie = oDate.getInteger("kaiJie");
//                // 获取时间处理的实际准备时间
//                Long wntPrep = oTasks.getJSONObject(i).getLong("prep");
//                Long wntDurTotal = oTasks.getJSONObject(i).getLong("wntDurTotal");
//                // 判断当前唯一ID存储时间处理的最初开始时间为0
//                if (hTeStart == 0) {
//                    // 调用获取当前时间戳方法设置开始时间
//                    oDate.put("teStart",getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp));
//                } else {
//                    // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
//                    if (csSta == 1 && kaiJie == 1) {
//                        // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
//                        hTeStart = lastEndTime;
//                    }
//                    oDate.put("teStart",hTeStart);
//                    System.out.println("这里开始时间-1:"+hTeStart);
//                }
//                boolean isExecutionMethod = (csSta == 0 && priorItem != 1) || (kaiJie != 1 && csSta == 1);
//
//                // 序号是不为1层级
//                // 判断执行方法为true
//                if (isExecutionMethod) {
//                    // 定义获取存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
//                    JSONObject serialOneEndTime;
//                    System.out.println("serialOneFatherLastTime:id_P:"+id_P+" - id_PF:"+id_PF);
//                    System.out.println(JSON.toJSONString(serialOneFatherLastTime));
//                    // 获取判断自己的id是否等于已存在的父id
//                    boolean b = serialOneFatherLastTime.containsKey(id_P);
//                    // 判断自己的id是已存在的父id
//                    if (b) {
//                        // 根据自己的id获取按照父零件编号存储每个序号的最后结束时间
//                        serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_P);
//                        // 转换键信息
//                        List<String> list = new ArrayList<>(serialOneEndTime.keySet());
//                        // 获取最后一个时间信息
//                        String s = list.get(list.size() - 1);
//                        // 赋值为最后一个时间信息
//                        hTeStart = serialOneEndTime.getLong(s);
//                    } else {
//                        // 根据父id获取按照父零件编号存储每个序号的最后结束时间
//                        serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_PF);
//                        if (null != serialOneEndTime) {
//                            // 获取上一个序号的时间信息并赋值
//                            hTeStart = serialOneEndTime.getLong(((priorItem - 1) + ""));
//                        }
//                    }
//                    // 设置开始时间
//                    oDate.put("teStart",hTeStart);
//                    System.out.println("这里开始时间-3:"+hTeStart);
//                }
//
//                // 获取任务的最初开始时间备份
//                Long teStartBackups = oDate.getLong("teStart");
//                // 设置最初结束时间
//                oDate.put("teFin",(teStartBackups+(wntDurTotal+wntPrep)));
//                // 获取最初结束时间
//                Long teFin = oDate.getLong("teFin");
//                // 获取任务信息，并且转换为任务类
//                Task task = JSON.parseObject(JSON.toJSONString(oTasks.get(i)),Task.class);
//                // 设置最初任务信息的时间信息
//                task.setWntDurTotal((teFin - teStartBackups));
//                task.setTePStart(teStartBackups);
//                task.setTePFinish(teFin);
//                task.setTeCsStart(teStartBackups);
//                task.setTeCsSonOneStart(0L);
//                task.setDateIndex(i);
//                // 判断优先级不等于-1
//                if (wn0TPrior != -1) {
//                    // 设置优先级为传参的优先级
//                    task.setPriority(wn0TPrior);
//                }
//                // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
//                if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1
//                        && kaiJie != 5 && kaiJie != 3
////                    && !isGetIntoNull
//                ) {
//                    // 根据父id添加开始时间
//                    serialOneFatherStartTime.put(id_PF,task.getTeCsStart());
//                } else if (kaiJie == 3 || kaiJie == 5) {
//                    // 添加子最初开始时间
//                    task.setTeCsSonOneStart(serialOneFatherStartTime.getLong(id_P));
//                }
//
//                // 创建当前处理的任务的所在日期对象
//                JSONObject teDate = new JSONObject();
//                System.out.println("++ taskTe: ++ "+dep+" - "+grpB);
//                System.out.println(JSON.toJSONString(task));
//                System.out.println(JSON.toJSONString(objTaskAll));
//                System.out.println(JSON.toJSONString(allImageTasks));
//                // 调用时间处理方法
//                JSONObject timeHandleInfo = timeHandle(task,hTeStart,grpB,dep,id_OInside,indexInside
//                        ,0,random,1,teDate,timeConflictCopy,0
//                        ,sho,0,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
//                        ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
//                        ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
//                        ,clearStatus,thisInfo,allImageTeDate,false,depAllTime);
//                // 更新任务最初始开始时间
//                hTeStart = timeHandleInfo.getLong("hTeStart");
//                System.out.println("最外层-3:"+hTeStart);
//                System.out.println(JSON.toJSONString(timeHandleInfo));
//                // 添加结束时间
//                teFinList.add(hTeStart);
//
//                // 根据订单编号获取递归集合
//                JSONArray dgList = actionIdO.getJSONArray(id_OInside);
//                // 根据订单下标获取递归信息并且转换为递归类
//                OrderAction orderAction = JSON.parseObject(
//                        JSON.toJSONString(dgList.getJSONObject(indexInside)),OrderAction.class);
//                // 更新递归信息
//                orderAction.setDep(dep);
//                orderAction.setGrpB(grpB);
//                orderAction.setTeDate(teDate);
//                // 将更新的递归信息写入回去
//                dgList.set(indexInside,orderAction);
//                actionIdO.put(id_OInside,dgList);
//
//                // 定义存储最后结束时间参数
//                long storageLastEndTime;
//                // 判断序号是为1层级
//                if (csSta == 1) {
//                    // 获取实际结束时间
//                    Long actualEndTime = timeHandleInfo.getLong("xFin");
////                System.out.println("xFinW:"+xFin);
//                    // 定义存储判断实际结束时间是否为空
//                    boolean isActualEndTime = false;
//                    // 判断实际结束时间不等于空
//                    if (null != actualEndTime) {
////                    System.out.println("xFin:"+xFin);
//                        // 赋值实际结束时间
//                        hTeStart = actualEndTime;
//                        // 判断当前实际结束时间大于最大结束时间
//                        if (actualEndTime > maxSte) {
//                            // 判断大于则更新最大结束时间为当前结束时间
//                            maxSte = actualEndTime;
//                        }
//                        // 设置不为空
//                        isActualEndTime = true;
//                    } else {
//                        // 判断当前实际结束时间大于最大结束时间：注 ： xFin 和 task.getTePFinish() 有时候是不一样的，不能随便改
//                        if (task.getTePFinish() > maxSte) {
//                            // 判断大于则更新最大结束时间为当前结束时间
//                            maxSte = task.getTePFinish();
//                        }
//                    }
////                System.out.println("maxSte:"+maxSte);
//                    // 判断实际结束时间不为空
//                    if (isActualEndTime) {
//                        // 赋值结束时间
//                        storageLastEndTime = actualEndTime;
//                    } else {
//                        // 赋值结束时间
//                        storageLastEndTime = task.getTePFinish();
//                    }
//                    // 判断是第一次进入
//                    if (canOnlyEnterOnce) {
//                        // 添加设置第一层的开始时间
//                        lastEndTime=task.getTePStart();
//                        // 设置只能进入一次
//                        canOnlyEnterOnce = false;
//                    }
//                } else {
//                    // 直接赋值最后结束时间
//                    storageLastEndTime = hTeStart;
//                }
//                // 根据父id获取最后结束时间信息
//                JSONObject fatherGetEndTimeInfo = serialOneFatherLastTime.getJSONObject(id_PF);
//                // 判断最后结束时间信息为空
//                if (null == fatherGetEndTimeInfo) {
//                    // 创建并且赋值最后结束时间
//                    fatherGetEndTimeInfo = new JSONObject();
//                    fatherGetEndTimeInfo.put(priorItem.toString(),0);
//                }
//                // 根据序号获取最后结束时间
//                Long aLong = fatherGetEndTimeInfo.getLong(priorItem.toString());
//                // 判断最后结束时间为空
//                if (null == aLong) {
//                    // 为空，则直接添加最后结束时间信息
//                    fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
//                    serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
//                } else {
//                    // 不为空，则判断当前最后结束时间大于已存在的最后结束时间
//                    if (storageLastEndTime > aLong) {
//                        // 判断当前最后结束时间大于，则更新最后结束时间为当前结束时间
//                        fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
//                        serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
//                    }
//                }
//                initialStartTime=hTeStart;
////            System.out.println();
//            }
//
//            // 调用任务最后处理方法
//            timeZjServiceComprehensive.taskLastHandle(timeConflictCopy,id_C,randomAll,objTaskAll
//                    ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,id_O
//                    ,objOrderList,actionIdO,allImageTeDate,depAllTime);
//
//            // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
//            onlyFirstTimeStamp.remove(random);
//            // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
//            newestLastCurrentTimestamp.remove(random);
//            // 根据当前唯一标识删除信息
//            onlyRefState.remove(random);
        }
        return retResult.ok(CodeEnum.OK.getCode(), "多个时间处理成功!");
    }

    /**
     * 时间处理核心方法
     * @param id_O  主订单编号
     * @param teStart   开始时间
     * @param id_C  公司编号
     * @param wn0TPrior 优先级
     * @author tang
     * @ver 1.0.0
     */
//    public void atFirst(int wn0TPrior,long teStart,String id_O,String id_C){
//        TimeZj.isZ = 6;
////        System.out.println(id_O);
//        // 调用方法获取订单信息
//        Order salesOrderData = qt.getMDContent(id_O,qt.strList("oItem", "info", "view", "action", "casItemx"), Order.class);
////        System.out.println("--------");
////        System.out.println(JSON.toJSONString(salesOrderData));
//        // 判断订单是否为空
//        if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
//                || null == salesOrderData.getCasItemx() || null == salesOrderData.getInfo()) {
//            // 返回为空错误信息
//            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
//        }
//        // 定义，存储进入未操作到的地方记录
//        JSONObject recordNoOperation = new JSONObject();
//        // 存储任务所在日期
//        JSONObject storageTaskWhereTime = new JSONObject();
//        // 镜像任务存储
//        Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks = new HashMap<>(16);
//        // 镜像总时间存储
//        JSONObject allImageTotalTime = new JSONObject();
//        // 全部任务存储
//        JSONObject objTaskAll = new JSONObject();
////            // 判断是测试
////            if (isTest) {
////                // 调用根据公司编号清空所有任务信息方法
////                setTaskAndZonKai(id_C);
////                // 调用添加测试数据方法
////                TaskObj.addOrder(teStart,qt);
////                TaskObj.addOrder2(teStart,qt);
////                TaskObj.addOrder3(teStart,qt);
////                // 调用添加测试数据方法
////                TaskObj.addTasks(teStart,"1001","1xx1",id_C,objTaskAll);
////                TaskObj.addTasksAndOrder(teStart,id_C,objTaskAll);
////                TaskObj.addTasksAndOrder3(teStart,id_C,objTaskAll);
////
////                String randomAll = new ObjectId().toString();
////
////                // 调用任务最后处理方法
////                timeZjServiceComprehensive.taskLastHandle(new JSONObject(),id_C,randomAll,objTaskAll
////                        ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,id_O,new JSONArray()
////                        ,new JSONObject(),new JSONObject(),null);
////                objTaskAll = new JSONObject();
////                storageTaskWhereTime = new JSONObject();
////                allImageTasks = new HashMap<>(16);
////                allImageTotalTime = new JSONObject();
////            }
//        // 根据组别存储部门信息
//        JSONObject grpBGroupIdOJ = new JSONObject();
//        // 存储casItemx内订单列表的订单oDates数据
//        JSONObject actionIdO = new JSONObject();
//        // 存储部门对应组别的职位总人数
//        JSONObject grpUNumAll = new JSONObject();
//        // 存储部门对应组别的上班和下班时间
//        JSONObject xbAndSbAll = new JSONObject();
//        // 统一id_O和index存储记录状态信息
//        JSONObject recordId_OIndexState = new JSONObject();
//        // 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
//        JSONObject onlyRefState = new JSONObject();
//        // 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
//        JSONObject newestLastCurrentTimestamp = new JSONObject();
//        // 存储当前唯一编号的第一个当前时间戳
//        JSONObject onlyFirstTimeStamp = new JSONObject();
//        // 获取唯一下标
//        String random = new ObjectId().toString();
//        // 获取全局唯一下标
//        String randomAll = new ObjectId().toString();
//
//        // 设置问题记录的初始值
//        yiShu.put(randomAll,0);
//        leiW.put(randomAll,0);
//        xin.put(randomAll,0);
//        isQzTz.put(randomAll,0);
//        recordNoOperation.put(randomAll,new JSONArray());
//
//        // 设置存储当前唯一编号的第一个当前时间戳
//        onlyFirstTimeStamp.put(random,teStart);
//        // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
//        onlyRefState.put(random,0);
//        // 存储最初开始时间
//        long initialStartTime = 0L;
//        // 存储最后结束时间
//        long lastEndTime = 0L;
//        // 获取递归订单列表
//        JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
//        // 存储递归订单列表的订单编号集合
//        JSONArray objOrderList = new JSONArray();
//        for (int i = 0; i < objOrder.size() - 1; i++) {
//            // 添加订单编号
//            objOrderList.add(objOrder.getJSONObject(i).getString("id_O"));
//        }
//        JSONObject depAllTime = new JSONObject();
//        System.out.println("--- 分割 ---");
//        // 遍历订单列表
//        Map<String,Asset> assetMap = new HashMap<>();
//        // 当前处理信息
//        JSONObject thisInfo = new JSONObject();
////        JSONObject id_OPConflictObj = new JSONObject();
////        for (int i = 0; i < objOrder.size(); i++) {
////            // 获取订单列表的订单编号
////            String id_OInside = objOrder.getJSONObject(i).getString("id_O");
//////            id_OPConflictObj.put(id_OInside,0);
////            // 判断订单等于主订单，则通过循环
////            if (id_OInside.equals(id_O)) {
////                continue;
////            }
////            // 添加订单编号
////            objOrderList.add(id_OInside);
////            // 根据订单编号查询action卡片信息
////            Order insideAction = qt.getMDContent(id_OInside,"action", Order.class);
//////            // 获取递归信息
//////            JSONArray objAction = insideAction.getAction().getJSONArray("objAction");
////            // 获取组别对应部门信息
////            JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
//////            if ("65a8eb50d295bb70738c506c".equals(id_OInside)) {
//////                System.out.println("65a8eb50d295bb70738c506c-订单:");
//////                System.out.println(JSON.toJSONString(insideAction));
//////            }
////            getGrpB(grpBGroup,assetMap,id_C,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,id_OInside);
////            // 根据订单编号添加订单信息存储
//////            actionIdO.put(id_OInside,objAction);
//////            if ("65a8eb50d295bb70738c506c".equals(id_OInside)) {
//////                System.out.println("65a8eb50d295bb70738c506c-订单:");
//////                System.out.println(JSON.toJSONString(grpBGroupIdOJ));
//////            }
////        }
////        // 设置当前冲突订单的父订单编号
////        setThisInfoId_OPConflictObj(thisInfo,id_OPConflictObj);
//        System.out.println("xbAndSbAll:");
//        System.out.println(JSON.toJSONString(xbAndSbAll));
//
//        JSONObject casItemx = salesOrderData.getCasItemx();
//        OrderInfo info = salesOrderData.getInfo();
//        // 获取递归存储的时间处理信息
//        JSONArray oDates = casItemx.getJSONObject("java").getJSONArray("oDates");
//        JSONArray oDateObj = casItemx.getJSONObject("java").getJSONArray("oDateObj");
////        // 获取递归存储的时间任务信息
////        JSONArray oTasks = casItemx.getJSONObject("java").getJSONArray("oTasks");
//        // 用于存储时间冲突的副本
//        JSONObject timeConflictCopy = new JSONObject();
//        // 用于存储判断镜像是否是第一个被冲突的产品
//        JSONObject sho = new JSONObject();
//        // 用于存储控制只进入一次的判断，用于记录第一个数据处理的结束时间
//        boolean canOnlyEnterOnce = true;
//        // 定义用来存储最大结束时间
//        long maxSte = 0;
//
//        // 用于存储每一个时间任务的结束时间
//        JSONArray teFinList = new JSONArray();
//        // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
//        JSONObject serialOneFatherLastTime = new JSONObject();
//        // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的预计开始时间
//        JSONObject serialOneFatherStartTime = new JSONObject();
//        // 清理状态
//        JSONObject clearStatus = new JSONObject();
//
//        setThisInfoRef(thisInfo,"time");
//        // 镜像任务所在时间
//        JSONObject allImageTeDate = new JSONObject();
//
////        JSONObject resultTask = mergeTaskByPrior(oDates
//////                , oTasks
////                , grpUNumAll);
////        oDates = resultTask.getJSONArray("oDates");
//        oDates = mergeTaskByPrior(oDates,grpUNumAll);
////        oTasks = resultTask.getJSONArray("oTasks");
//        for (int i = 0; i < oDates.size(); i++) {
//            JSONObject oDate = oDates.getJSONObject(i);
//            getGrpB(oDate.getString("grpB"),oDate.getString("dep"), assetMap,id_C
//                    ,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,oDate.getString("id_O"));
//        }
////        for (int i = 0; i < objOrder.size(); i++) {
////            // 获取订单列表的订单编号
////            String id_OInside = objOrder.getJSONObject(i).getString("id_O");
//////            id_OPConflictObj.put(id_OInside,0);
////            // 判断订单等于主订单，则通过循环
////            if (id_OInside.equals(id_O)) {
////                continue;
////            }
////            // 添加订单编号
////            objOrderList.add(id_OInside);
////            // 根据订单编号查询action卡片信息
////            Order insideAction = qt.getMDContent(id_OInside,"action", Order.class);
//////            // 获取递归信息
//////            JSONArray objAction = insideAction.getAction().getJSONArray("objAction");
////            // 获取组别对应部门信息
////            JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
//////            if ("65a8eb50d295bb70738c506c".equals(id_OInside)) {
//////                System.out.println("65a8eb50d295bb70738c506c-订单:");
//////                System.out.println(JSON.toJSONString(insideAction));
//////            }
////            getGrpB(grpBGroup,assetMap,id_C,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,id_OInside);
////            // 根据订单编号添加订单信息存储
//////            actionIdO.put(id_OInside,objAction);
//////            if ("65a8eb50d295bb70738c506c".equals(id_OInside)) {
//////                System.out.println("65a8eb50d295bb70738c506c-订单:");
//////                System.out.println(JSON.toJSONString(grpBGroupIdOJ));
//////            }
////        }
//        qt.setMDContent(id_O,qt.setJson("casItemx.java.oDates",oDates
////                ,"casItemx.java.oTasks",oTasks
//        ), Order.class);
//        setOrderFatherId(id_O,thisInfo,actionIdO,objOrder,oDates);
//
////        // 抛出操作成功异常
////        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
//
//        boolean isOne = false;
//        setThisInfoFinalPartDateIndex(thisInfo,(oDates.size()-1));
//        setThisInfoFinalPartDateId_O(thisInfo,oDates.getJSONObject((oDates.size()-1)).getString("id_O"));
//        setThisInfoIsConflict(thisInfo,false);
//        // 遍历时间处理信息集合
//        for (int i = 0; i < oDates.size(); i++) {
//            setThisInfoIsFinalPart(thisInfo, i >= oDates.size() - 1);
//            // 获取i对应的时间处理信息
//            JSONObject oDate = oDates.getJSONObject(i);
////                Integer bmdpt = oDate.getInteger("bmdpt");
////                if (bmdpt == 3) {
////                    continue;
////                }
//            // 获取订单编号
//            String id_OInside = oDate.getString("id_O");
//            // 获取订单下标
//            int indexInside = oDate.getInteger("index");
////                JSONArray objAction = actionIdO.getJSONArray(id_OInside);
////                JSONObject indexAction = objAction.getJSONObject(indexInside);
////                int bcdStatus = indexAction.getInteger("bcdStatus") == null?0:indexAction.getInteger("bcdStatus");
////                if (bcdStatus == 8 || bcdStatus == 2) {
////                    continue;
////                }
//            // 获取时间处理的序号
//            Integer priorItem = oDate.getInteger("priorItem");
//            // 获取时间处理的父零件编号
//            String id_PF = oDate.getString("id_PF");
//            // 获取时间处理的序号是否为1层级 csSta - timeHandleSerialNoIsOne
//            Integer csSta = oDate.getInteger("csSta");
//            // 获取时间处理的判断是否是空时间信息
//            Boolean empty = oDate.getBoolean("empty");
//            // 判断当前时间处理为空时间信息
//            if (empty) {
//                // 获取时间处理的链接下标
//                Integer linkInd = oDate.getInteger("linkInd");
//                // 根据链接下标获取指定的结束时间
//                Long indexEndTime = teFinList.getLong(linkInd);
//                // 判断父id的预计开始时间为空，并且序号为第一个
//                if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1) {
//                    serialOneFatherStartTime.put(id_PF,indexEndTime);
//                }
//                // 根据父零件编号获取序号信息
//                JSONObject fatherSerialInfo = serialOneFatherLastTime.getJSONObject(id_PF);
//                // 判断序号信息为空
//                if (null == fatherSerialInfo) {
//                    // 创建序号信息
//                    fatherSerialInfo = new JSONObject();
//                    // 添加序号的结束时间，默认为0
//                    fatherSerialInfo.put(priorItem.toString(),0);
//                }
//                // 获取序号结束时间
//                Long serialEndTime = fatherSerialInfo.getLong(priorItem.toString());
//                // 添加链接结束时间到当前空时间处理结束时间列表内
//                teFinList.add(indexEndTime);
//                // 判断链接结束时间大于当前结束时间
//                if (indexEndTime > serialEndTime) {
//                    // 修改当前结束时间为链接结束时间
//                    fatherSerialInfo.put(priorItem.toString(),indexEndTime);
//                    // 根据父零件编号添加序号信息
//                    serialOneFatherLastTime.put(id_PF,fatherSerialInfo);
//                }
//                continue;
//            }
//
//            // 获取当前唯一ID存储时间处理的最初开始时间
//            Long hTeStart = initialStartTime;
//            // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
//            JSONObject firstConflictId_O = new JSONObject();
//            JSONObject firstConflictIndex = new JSONObject();
//            // 设置为-1代表的是递归的零件
//            firstConflictIndex.put("prodState",-1);
//            firstConflictIndex.put("z","-1");
//            firstConflictId_O.put(oDate.getString("index"),firstConflictIndex);
//            sho.put(oDate.getString("id_O"),firstConflictId_O);
//            // 获取时间处理的组别
//            String grpB = oDate.getString("grpB");
//            String dep = oDate.getString("dep");
//
//            // 获取时间处理的零件产品编号
//            String id_P = oDate.getString("id_P");
//            // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
//            Integer kaiJie = oDate.getInteger("kaiJie");
////            // 获取时间处理的实际准备时间
////            Long wntPrep = oTasks.getJSONObject(i).getLong("prep");
////            Long wntDurTotal = oTasks.getJSONObject(i).getLong("wntDurTotal");
//            // 获取时间处理的实际准备时间
//            Long wntPrep = oDate.getLong("wntPrep");
//            Long wntDurTotal = oDate.getLong("wntDurTotal");
//            long initialTeStart;
//            // 判断当前唯一ID存储时间处理的最初开始时间为0
//            if (hTeStart == 0) {
//                // 调用获取当前时间戳方法设置开始时间
////                oDate.put("teStart",getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp));
//                initialTeStart = getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp);
//            } else {
//                // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
//                if (csSta == 1 && kaiJie == 1) {
//                    // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
//                    hTeStart = lastEndTime;
//                }
////                oDate.put("teStart",hTeStart);
//                initialTeStart = hTeStart;
//                System.out.println("这里开始时间-1:"+hTeStart);
//            }
//            boolean isExecutionMethod = (csSta == 0 && priorItem != 1) || (kaiJie != 1 && csSta == 1);
//
//            // 序号是不为1层级
//            // 判断执行方法为true
//            if (isExecutionMethod) {
//                // 定义获取存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
//                JSONObject serialOneEndTime;
//                System.out.println("serialOneFatherLastTime:id_P:"+id_P+" - id_PF:"+id_PF);
//                System.out.println(JSON.toJSONString(serialOneFatherLastTime));
//                // 获取判断自己的id是否等于已存在的父id
//                boolean b = serialOneFatherLastTime.containsKey(id_P);
//                // 判断自己的id是已存在的父id
//                if (b) {
//                    // 根据自己的id获取按照父零件编号存储每个序号的最后结束时间
//                    serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_P);
//                    // 转换键信息
//                    List<String> list = new ArrayList<>(serialOneEndTime.keySet());
//                    // 获取最后一个时间信息
//                    String s = list.get(list.size() - 1);
//                    // 赋值为最后一个时间信息
//                    hTeStart = serialOneEndTime.getLong(s);
//                } else {
//                    // 根据父id获取按照父零件编号存储每个序号的最后结束时间
//                    serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_PF);
//                    if (null != serialOneEndTime) {
//                        // 获取上一个序号的时间信息并赋值
//                        hTeStart = serialOneEndTime.getLong(((priorItem - 1) + ""));
//                    }
//                }
//                // 设置开始时间
////                oDate.put("teStart",hTeStart);
//                initialTeStart = hTeStart;
//                System.out.println("这里开始时间-3:"+hTeStart);
//            }
//
//            // 获取任务的最初开始时间备份
////            Long teStartBackups = oDate.getLong("teStart");
//            Long teStartBackups = initialTeStart;
////            // 设置最初结束时间
////            oDate.put("teFin",(teStartBackups+(wntDurTotal+wntPrep)));
////            // 获取最初结束时间
////            Long teFin = oDate.getLong("teFin");
//            // 获取最初结束时间
//            Long teFin = (teStartBackups+(wntDurTotal+wntPrep));
////            // 获取任务信息，并且转换为任务类
////            Task task = JSON.parseObject(JSON.toJSONString(oTasks.get(i)),Task.class);
//            // 获取任务信息，并且转换为任务类
//            Task task = new Task(oDate.getInteger("priority"),id_OInside,indexInside,wntPrep
//                    ,wntDurTotal+wntPrep,id_C,i,oDate.getJSONObject("wrdN"));
//            // 设置最初任务信息的时间信息
//            task.setTePStart(teStartBackups);
//            task.setTePFinish(teFin);
//            task.setTeCsStart(teStartBackups);
//            task.setTeCsSonOneStart(0L);
//            task.setWrdNO(info.getWrdN());
//            task.setRefOP(id_O);
//            task.setWn2qtyneed(oDate.getDouble("wn2qtyneed"));
//            // 判断优先级不等于-1
//            if (wn0TPrior != -1) {
//                // 设置优先级为传参的优先级
//                task.setPriority(wn0TPrior);
//            }
//            // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
//            if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1
//                    && kaiJie != 5 && kaiJie != 3
////                    && !isGetIntoNull
//            ) {
//                // 根据父id添加开始时间
//                serialOneFatherStartTime.put(id_PF,task.getTeCsStart());
//            } else if (kaiJie == 3 || kaiJie == 5) {
//                // 添加子最初开始时间
//                task.setTeCsSonOneStart(serialOneFatherStartTime.getLong(id_P));
//            }
//
//            // 创建当前处理的任务的所在日期对象
//            JSONObject teDate = new JSONObject();
//            System.out.println();
//            System.out.println("++ taskTe: ++ "+dep+" - "+grpB);
//            System.out.println();
//            System.out.println(JSON.toJSONString(task));
//            System.out.println(JSON.toJSONString(objTaskAll));
//            System.out.println(JSON.toJSONString(allImageTasks));
//
////            JSONObject conflictInfo = new JSONObject();
//            JSONObject conflictInfo = getThisInfoConflictInfo(thisInfo);
//            if (null == conflictInfo) {
//                conflictInfo = new JSONObject();
//            }
//            JSONObject thisConfInfo = new JSONObject();
//            thisConfInfo.put("dep",dep);
//            thisConfInfo.put("grpB",grpB);
//            thisConfInfo.put("status",0);
//            conflictInfo.put(task.getDateIndex()+"",thisConfInfo);
//            setThisInfoConflictInfo(thisInfo,conflictInfo);
////            System.out.println("getThisInfoConflictInfo:");
////            System.out.println(JSON.toJSONString(getThisInfoConflictInfo(thisInfo)));
//            if (isOne) {
//                JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
//                JSONObject upConfInfo = thisInfoConflictInfo.getJSONObject((task.getDateIndex() - 1) + "");
//                for (int upI = (task.getDateIndex()-1); upI >= 0; upI--) {
//                    upConfInfo = thisInfoConflictInfo.getJSONObject((upI) + "");
//                    if (null != upConfInfo) {
//                        break;
//                    }
//                }
//                if (null != upConfInfo) {
//                    Integer status = upConfInfo.getInteger("status");
//                    if (status == 1) {
//                        String upDep = upConfInfo.getString("dep");
//                        String upGrpB = upConfInfo.getString("grpB");
//                        if (!upDep.equals(dep) || !upGrpB.equals(grpB)) {
//                            System.out.println("--+ 进入前置修改全局任务信息 +-- :");
//                            updateObjTaskAllSetAllImageTasks(objTaskAll,allImageTasks,allImageTotalTime);
//                            System.out.println("修改后:");
//                            System.out.println(JSON.toJSONString(objTaskAll));
//                            System.out.println(JSON.toJSONString(allImageTasks));
//                        }
//                    }
//                }
//            } else {
//                isOne = true;
//            }
//
//            // 调用时间处理方法
//            JSONObject timeHandleInfo = timeHandle(task,hTeStart,grpB,dep,id_OInside,indexInside
//                    ,0,random,1,teDate,timeConflictCopy,0
//                    ,sho,0,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
//                    ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
//                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
//                    ,clearStatus,thisInfo,allImageTeDate,false,depAllTime);
//            // 更新任务最初始开始时间
//            hTeStart = timeHandleInfo.getLong("hTeStart");
//            System.out.println("最外层-3-1:"+hTeStart);
//            System.out.println(JSON.toJSONString(timeHandleInfo));
//            // 添加结束时间
//            teFinList.add(hTeStart);
//
//            String id_OP = sonGetOrderFatherId(id_OInside, id_C, thisInfo, actionIdO, grpUNumAll);
//            JSONObject oPDateInfo = actionIdO.getJSONObject(id_OP);
//            JSONArray oDatesNew = oPDateInfo.getJSONArray("oDates");
//            JSONObject thisODate = oDatesNew.getJSONObject(i);
//            thisODate.put("teDate",teDate);
//            oDatesNew.set(i,thisODate);
//            oPDateInfo.put("oDates",oDatesNew);
//            actionIdO.put(id_OP,oPDateInfo);
//
//            // 定义存储最后结束时间参数
//            long storageLastEndTime;
//            // 判断序号是为1层级
//            if (csSta == 1) {
//                // 获取实际结束时间
//                Long actualEndTime = timeHandleInfo.getLong("xFin");
////                System.out.println("xFinW:"+xFin);
//                // 定义存储判断实际结束时间是否为空
//                boolean isActualEndTime = false;
//                // 判断实际结束时间不等于空
//                if (null != actualEndTime) {
////                    System.out.println("xFin:"+xFin);
//                    // 赋值实际结束时间
//                    hTeStart = actualEndTime;
//                    // 判断当前实际结束时间大于最大结束时间
//                    if (actualEndTime > maxSte) {
//                        // 判断大于则更新最大结束时间为当前结束时间
//                        maxSte = actualEndTime;
//                    }
//                    // 设置不为空
//                    isActualEndTime = true;
//                } else {
//                    // 判断当前实际结束时间大于最大结束时间：注 ： xFin 和 task.getTePFinish() 有时候是不一样的，不能随便改
//                    if (task.getTePFinish() > maxSte) {
//                        // 判断大于则更新最大结束时间为当前结束时间
//                        maxSte = task.getTePFinish();
//                    }
//                }
////                System.out.println("maxSte:"+maxSte);
//                // 判断实际结束时间不为空
//                if (isActualEndTime) {
//                    // 赋值结束时间
//                    storageLastEndTime = actualEndTime;
//                } else {
//                    // 赋值结束时间
//                    storageLastEndTime = task.getTePFinish();
//                }
//                // 判断是第一次进入
//                if (canOnlyEnterOnce) {
//                    // 添加设置第一层的开始时间
//                    lastEndTime=task.getTePStart();
//                    // 设置只能进入一次
//                    canOnlyEnterOnce = false;
//                }
//            } else {
//                // 直接赋值最后结束时间
//                storageLastEndTime = hTeStart;
//            }
//            // 根据父id获取最后结束时间信息
//            JSONObject fatherGetEndTimeInfo = serialOneFatherLastTime.getJSONObject(id_PF);
//            // 判断最后结束时间信息为空
//            if (null == fatherGetEndTimeInfo) {
//                // 创建并且赋值最后结束时间
//                fatherGetEndTimeInfo = new JSONObject();
//                fatherGetEndTimeInfo.put(priorItem.toString(),0);
//            }
//            // 根据序号获取最后结束时间
//            Long aLong = fatherGetEndTimeInfo.getLong(priorItem.toString());
//            // 判断最后结束时间为空
//            if (null == aLong) {
//                // 为空，则直接添加最后结束时间信息
//                fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
//                serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
//            } else {
//                // 不为空，则判断当前最后结束时间大于已存在的最后结束时间
//                if (storageLastEndTime > aLong) {
//                    // 判断当前最后结束时间大于，则更新最后结束时间为当前结束时间
//                    fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
//                    serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
//                }
//            }
//            initialStartTime=hTeStart;
//            if (getThisInfoIsFinalPart(thisInfo) && getThisInfoIsConflict(thisInfo)) {
//                System.out.println("开始处理冲突任务:");
//                JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
////                System.out.println(JSON.toJSONString(thisInfoConflictInfo));
//                JSONObject thisInfoConflictInfoSon = thisInfoConflictInfo.getJSONObject( i+ "");
//                thisInfoConflictInfoSon.put("status",1);
//                thisInfoConflictInfo.put(i+"",thisInfoConflictInfoSon);
//                setThisInfoConflictInfo(thisInfo,thisInfoConflictInfo);
//
//                JSONObject thisInfoQuiltConflictInfo = getThisInfoQuiltConflictInfo(thisInfo);
////                System.out.println(JSON.toJSONString(thisInfoQuiltConflictInfo));
//                List<Task> conflict = new ArrayList<>();
//                JSONArray conflictArr = thisInfoQuiltConflictInfo.getJSONArray("conflict");
//                for (int c = 0; c < conflictArr.size(); c++) {
//                    conflict.add(JSONObject.parseObject(JSON.toJSONString(conflictArr.getJSONObject(c)), Task.class));
//                }
////                System.out.println("排序前:");
////                System.out.println(JSON.toJSONString(conflict));
//                conflict.sort(Comparator.comparing(Task::getPriority));
////                System.out.println("排序后:");
////                System.out.println(JSON.toJSONString(conflict));
//                JSONObject indexInfo = thisInfoQuiltConflictInfo.getJSONObject("indexInfo");
//                JSONObject indexInfoNew = new JSONObject();
//                for (int conf = 0; conf < conflict.size(); conf++) {
//                    Task taskConflict = conflict.get(conf);
//                    String id_OConf = taskConflict.getId_O();
//                    Integer dateIndexConf = taskConflict.getDateIndex();
//                    for (String indInfoKey : indexInfo.keySet()) {
//                        JSONObject object = indexInfo.getJSONObject(indInfoKey);
//                        if (object.getString("id_O").equals(id_OConf)
//                                && Objects.equals(object.getInteger("dateIndex"), dateIndexConf)) {
////                            System.out.println("id_O:"+id_OConf+",dateIndex:"+dateIndexConf);
////                            System.out.println(JSON.toJSONString(object));
//                            indexInfoNew.put(conf+"", object);
//                        }
//                    }
//                }
//                thisInfoQuiltConflictInfo.put("indexInfo",indexInfoNew);
//                thisInfoQuiltConflictInfo.put("conflict",conflict);
//                System.out.println(JSON.toJSONString(thisInfoQuiltConflictInfo));
//                setThisInfoQuiltConflictInfo(thisInfo,thisInfoQuiltConflictInfo);
//
//                updateAllImageTasksSetObjTaskAll(objTaskAll,allImageTasks,allImageTotalTime);
//
//                dgHandleFollowUpConflict(thisInfo,grpB,dep,id_C,objTaskAll,depAllTime,random,timeConflictCopy,sho,csSta
//                        ,randomAll,xbAndSbAll,actionIdO,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
//                        ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
//                        ,clearStatus,allImageTeDate,conflict);
//            }
////            System.out.println();
//        }
//
//        // 调用任务最后处理方法
//        timeZjServiceComprehensive.taskLastHandle(timeConflictCopy,id_C,randomAll,objTaskAll
//                ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,id_O
//                ,objOrderList,actionIdO,allImageTeDate,depAllTime,thisInfo);
//
//        // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
//        onlyFirstTimeStamp.remove(random);
//        // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
//        newestLastCurrentTimestamp.remove(random);
//        // 根据当前唯一标识删除信息
//        onlyRefState.remove(random);
//    }
    public void atFirstODateObj(int wn0TPrior,long teStart,String id_O,String id_C){
        TimeZj.isZ = 6;
        // 调用方法获取订单信息
        Order salesOrderData = qt.getMDContent(id_O,qt.strList("oItem", "info", "view", "action", "casItemx"), Order.class);
        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
                || null == salesOrderData.getCasItemx() || null == salesOrderData.getInfo()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONObject casItemx = salesOrderData.getCasItemx();
        // 获取递归存储的时间处理信息
        JSONObject oDateObj = casItemx.getJSONObject("java").getJSONObject("oDateObj");
        // 存储部门对应组别的职位总人数
        JSONObject grpUNumAll = new JSONObject();
        oDateObj = mergeTaskByPriorODateObj(oDateObj,grpUNumAll);

//        // 抛出操作成功异常
//        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
        System.out.println("开始oDateObj:");
        System.out.println(JSON.toJSONString(oDateObj));
        qt.setMDContent(id_O,qt.setJson("casItemx.java.oDateObj",oDateObj), Order.class);
        List<Integer> layerList = new ArrayList<>();
        for (String layer : oDateObj.keySet()) {
            layerList.add(Integer.parseInt(layer));

            JSONObject layerInfo = oDateObj.getJSONObject(layer);
            for (String id_PF : layerInfo.keySet()) {
                JSONObject pfInfo = layerInfo.getJSONObject(id_PF);
                pfInfo.put("arrPStart",new JSONArray());
                layerInfo.put(id_PF,pfInfo);
                oDateObj.put(layer,layerInfo);
            }
        }
        layerList.sort(Comparator.reverseOrder());
        dgTime(layerList.get(0),oDateObj,salesOrderData,id_C,grpUNumAll,wn0TPrior,true,teStart);
        qt.setMDContent(id_O,qt.setJson("casItemx.java.oDateObj",oDateObj), Order.class);
    }

    private void dgTime(int layer,JSONObject oDateObj,Order salesOrderData
            ,String id_C,JSONObject grpUNumAll,int wn0TPrior,boolean isFirst,long teStart){
        if (layer > 0) {
            System.out.println("layer:"+layer);
            JSONObject prodInfo = oDateObj.getJSONObject(layer + "");
            for (String prodId : prodInfo.keySet()) {
                JSONObject partInfo = prodInfo.getJSONObject(prodId);
                long maxStaTime = teStart;
                if (!isFirst) {
                    JSONArray arrPStartThis = partInfo.getJSONArray("arrPStart");
                    maxStaTime = arrPStartThis.getJSONObject(0).getLong("lastTePFin");
                    teStart = arrPStartThis.getJSONObject(0).getLong("time");
                    if (arrPStartThis.size() > 0) {
                        for (int i = 1; i < arrPStartThis.size(); i++) {
                            Long time = arrPStartThis.getJSONObject(i).getLong("lastTePFin");
                            if (time > maxStaTime) {
                                maxStaTime = time;
                                teStart = arrPStartThis.getJSONObject(i).getLong("time");
                            }
                        }
                    }
                }
                partInfo.put("tePStart",maxStaTime);
                // 定义，存储进入未操作到的地方记录
                JSONObject recordNoOperation = new JSONObject();
                // 存储任务所在日期
                JSONObject storageTaskWhereTime = new JSONObject();
                // 镜像任务存储
                Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks = new HashMap<>(16);
                // 镜像总时间存储
                JSONObject allImageTotalTime = new JSONObject();
                // 全部任务存储
                JSONObject objTaskAll = new JSONObject();
                JSONObject grpBGroupIdOJ = new JSONObject();
                // 存储casItemx内订单列表的订单oDates数据
                JSONObject actionIdO = new JSONObject();
                // 存储部门对应组别的上班和下班时间
                JSONObject xbAndSbAll = new JSONObject();
                // 统一id_O和index存储记录状态信息
                JSONObject recordId_OIndexState = new JSONObject();
                // 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                JSONObject onlyRefState = new JSONObject();
                // 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
                JSONObject newestLastCurrentTimestamp = new JSONObject();
                // 存储当前唯一编号的第一个当前时间戳
                JSONObject onlyFirstTimeStamp = new JSONObject();
                // 获取唯一下标
                String random = new ObjectId().toString();
                // 获取全局唯一下标
                String randomAll = new ObjectId().toString();

                // 设置问题记录的初始值
                yiShu.put(randomAll,0);
                leiW.put(randomAll,0);
                xin.put(randomAll,0);
                isQzTz.put(randomAll,0);
                recordNoOperation.put(randomAll,new JSONArray());

                // 设置存储当前唯一编号的第一个当前时间戳
                onlyFirstTimeStamp.put(random,teStart);
                // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                onlyRefState.put(random,0);
                // 存储最初开始时间
                long initialStartTime = 0L;
                // 存储最后结束时间
                long lastEndTime = 0L;
                // 获取递归订单列表
                JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
                // 存储递归订单列表的订单编号集合
                JSONArray objOrderList = new JSONArray();
                for (int i = 0; i < objOrder.size() - 1; i++) {
                    // 添加订单编号
                    objOrderList.add(objOrder.getJSONObject(i).getString("id_O"));
                }
                JSONObject depAllTime = new JSONObject();
                System.out.println("--- 分割 ---");
                // 遍历订单列表
                Map<String,Asset> assetMap = new HashMap<>();
                // 当前处理信息
                JSONObject thisInfo = new JSONObject();
                setThisInfoLayer(thisInfo,layer);
                setThisInfoLayerProdId(thisInfo,prodId);
//                System.out.println("xbAndSbAll:");
//                System.out.println(JSON.toJSONString(xbAndSbAll));

                OrderInfo info = salesOrderData.getInfo();
                // 用于存储时间冲突的副本
                JSONObject timeConflictCopy = new JSONObject();
                // 用于存储判断镜像是否是第一个被冲突的产品
                JSONObject sho = new JSONObject();
                // 用于存储控制只进入一次的判断，用于记录第一个数据处理的结束时间
                boolean canOnlyEnterOnce = true;
                // 定义用来存储最大结束时间
                long maxLastTime = 0;

                // 用于存储每一个时间任务的结束时间
                JSONArray teFinList = new JSONArray();
                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
                JSONObject serialOneFatherLastTime = new JSONObject();
                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的预计开始时间
                JSONObject serialOneFatherStartTime = new JSONObject();
                // 清理状态
                JSONObject clearStatus = new JSONObject();

                setThisInfoRef(thisInfo,"time");
                // 镜像任务所在时间
                JSONObject allImageTeDate = new JSONObject();
                JSONArray oDates = partInfo.getJSONArray("oDates");
                for (int i = 0; i < oDates.size(); i++) {
                    JSONObject oDate = oDates.getJSONObject(i);
                    getGrpB(oDate.getString("grpB"),oDate.getString("dep"), assetMap,id_C
                            ,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,oDate.getString("id_O"));
                }
                setOrderFatherId(salesOrderData.getId(),thisInfo,actionIdO,objOrder,oDateObj);

                boolean isOne = false;
                setThisInfoFinalPartDateIndex(thisInfo,(oDates.size()-1));
                setThisInfoFinalPartDate(thisInfo,oDates.getJSONObject((oDates.size()-1)).getString("id_O")
                        ,oDates.getJSONObject((oDates.size()-1)).getString("layer")
                        ,oDates.getJSONObject((oDates.size()-1)).getString("id_PF"));
                setThisInfoIsConflict(thisInfo,false);
                // 遍历时间处理信息集合
                for (int i = 0; i < oDates.size(); i++) {
                    setThisInfoIsFinalPart(thisInfo, i >= oDates.size() - 1);
                    // 获取i对应的时间处理信息
                    JSONObject oDate = oDates.getJSONObject(i);
                    // 获取订单编号
                    String id_OInside = oDate.getString("id_O");
                    // 获取订单下标
                    int indexInside = oDate.getInteger("index");
                    // 获取时间处理的序号
                    Integer priorItem = oDate.getInteger("priorItem");
                    // 获取时间处理的父零件编号
                    String id_PF = oDate.getString("id_PF");
                    // 获取时间处理的序号是否为1层级 csSta - timeHandleSerialNoIsOne
                    Integer csSta = oDate.getInteger("csSta");
                    // 获取时间处理的判断是否是空时间信息
                    Boolean empty = oDate.getBoolean("empty");
                    // 判断当前时间处理为空时间信息
                    if (empty) {
                        // 获取时间处理的链接下标
                        Integer linkInd = oDate.getInteger("linkInd");
                        // 根据链接下标获取指定的结束时间
                        Long indexEndTime = teFinList.getLong(linkInd);
                        // 判断父id的预计开始时间为空，并且序号为第一个
                        if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1) {
                            serialOneFatherStartTime.put(id_PF,indexEndTime);
                        }
                        // 根据父零件编号获取序号信息
                        JSONObject fatherSerialInfo = serialOneFatherLastTime.getJSONObject(id_PF);
                        // 判断序号信息为空
                        if (null == fatherSerialInfo) {
                            // 创建序号信息
                            fatherSerialInfo = new JSONObject();
                            // 添加序号的结束时间，默认为0
                            fatherSerialInfo.put(priorItem.toString(),0);
                        }
                        // 获取序号结束时间
                        Long serialEndTime = fatherSerialInfo.getLong(priorItem.toString());
                        // 添加链接结束时间到当前空时间处理结束时间列表内
                        teFinList.add(indexEndTime);
                        // 判断链接结束时间大于当前结束时间
                        if (indexEndTime > serialEndTime) {
                            // 修改当前结束时间为链接结束时间
                            fatherSerialInfo.put(priorItem.toString(),indexEndTime);
                            // 根据父零件编号添加序号信息
                            serialOneFatherLastTime.put(id_PF,fatherSerialInfo);
                        }
                        continue;
                    }
                    // 获取当前唯一ID存储时间处理的最初开始时间
                    Long hTeStart = initialStartTime;
                    // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
                    JSONObject firstConflictId_O = new JSONObject();
                    JSONObject firstConflictIndex = new JSONObject();
                    // 设置为-1代表的是递归的零件
                    firstConflictIndex.put("prodState",-1);
                    firstConflictIndex.put("z","-1");
                    firstConflictId_O.put(oDate.getString("index"),firstConflictIndex);
                    sho.put(oDate.getString("id_O"),firstConflictId_O);
                    // 获取时间处理的组别
                    String grpB = oDate.getString("grpB");
                    String dep = oDate.getString("dep");
                    // 获取时间处理的零件产品编号
                    String id_P = oDate.getString("id_P");
                    // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
                    Integer kaiJie = oDate.getInteger("kaiJie");
                    // 获取时间处理的实际准备时间
                    Long wntPrep = oDate.getLong("wntPrep");
                    Long wntDurTotal = oDate.getLong("wntDurTotal");
                    long initialTeStart;
                    // 判断当前唯一ID存储时间处理的最初开始时间为0
                    if (hTeStart == 0) {
                        // 调用获取当前时间戳方法设置开始时间
                        initialTeStart = getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp);
                    } else {
                        // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
                        if (csSta == 1 && kaiJie == 1) {
                            // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
                            hTeStart = lastEndTime;
                        }
                        initialTeStart = hTeStart;
                        System.out.println("这里开始时间-1:"+hTeStart);
                    }
                    boolean isExecutionMethod = (csSta == 0 && priorItem != 1) || (kaiJie != 1 && csSta == 1);

                    // 序号是不为1层级
                    // 判断执行方法为true
                    if (isExecutionMethod) {
                        // 定义获取存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
                        JSONObject serialOneEndTime;
                        System.out.println("serialOneFatherLastTime:id_P:"+id_P+" - id_PF:"+id_PF);
                        System.out.println(JSON.toJSONString(serialOneFatherLastTime));
                        // 获取判断自己的id是否等于已存在的父id
                        boolean b = serialOneFatherLastTime.containsKey(id_P);
                        // 判断自己的id是已存在的父id
                        if (b) {
                            // 根据自己的id获取按照父零件编号存储每个序号的最后结束时间
                            serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_P);
                            // 转换键信息
                            List<String> list = new ArrayList<>(serialOneEndTime.keySet());
                            // 获取最后一个时间信息
                            String s = list.get(list.size() - 1);
                            // 赋值为最后一个时间信息
                            hTeStart = serialOneEndTime.getLong(s);
                        } else {
                            // 根据父id获取按照父零件编号存储每个序号的最后结束时间
                            serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_PF);
                            if (null != serialOneEndTime) {
                                // 获取上一个序号的时间信息并赋值
                                hTeStart = serialOneEndTime.getLong(((priorItem - 1) + ""));
                            }
                        }
                        // 设置开始时间
                        initialTeStart = hTeStart;
                        System.out.println("这里开始时间-3:"+hTeStart);
                    }

                    // 获取任务的最初开始时间备份
                    long teStartBackups = initialTeStart;
                    if (!isFirst && !isOne) {
                        teStartBackups = maxStaTime;
                    }
                    // 获取最初结束时间
                    Long teFin = (teStartBackups+(wntDurTotal+wntPrep));
                    // 获取任务信息，并且转换为任务类
                    Task task = new Task(oDate.getInteger("priority"),id_OInside,indexInside,wntPrep
                            ,wntDurTotal+wntPrep,id_C,i,oDate.getJSONObject("wrdN"));
                    // 设置最初任务信息的时间信息
                    task.setTePStart(teStartBackups);
                    task.setTePFinish(teFin);
                    task.setTeCsStart(teStartBackups);
                    task.setTeCsSonOneStart(0L);
                    task.setWrdNO(info.getWrdN());
                    task.setRefOP(salesOrderData.getId());
                    task.setWn2qtyneed(oDate.getDouble("wn2qtyneed"));
                    task.setId_PF(getThisInfoLayerProdId(thisInfo));
                    task.setLayer(getThisInfoLayer(thisInfo));
                    // 判断优先级不等于-1
                    if (wn0TPrior != -1) {
                        // 设置优先级为传参的优先级
                        task.setPriority(wn0TPrior);
                    }
                    // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
                    if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1
                            && kaiJie != 5 && kaiJie != 3
                    ) {
                        // 根据父id添加开始时间
                        serialOneFatherStartTime.put(id_PF,task.getTeCsStart());
                    } else if (kaiJie == 3 || kaiJie == 5) {
                        // 添加子最初开始时间
                        task.setTeCsSonOneStart(serialOneFatherStartTime.getLong(id_P));
                    }

                    // 创建当前处理的任务的所在日期对象
                    JSONObject teDate = new JSONObject();
                    System.out.println();
                    System.out.println("++ taskTe: ++ "+dep+" - "+grpB);
                    System.out.println();
                    System.out.println(JSON.toJSONString(task));
                    System.out.println(JSON.toJSONString(objTaskAll));
                    System.out.println(JSON.toJSONString(allImageTasks));

                    JSONObject conflictInfo = getThisInfoConflictInfo(thisInfo);
                    if (null == conflictInfo) {
                        conflictInfo = new JSONObject();
                    }
                    JSONObject thisConfInfo = new JSONObject();
                    thisConfInfo.put("dep",dep);
                    thisConfInfo.put("grpB",grpB);
                    thisConfInfo.put("status",0);
                    conflictInfo.put(task.getDateIndex()+"",thisConfInfo);
                    setThisInfoConflictInfo(thisInfo,conflictInfo);
                    int isCanOnly = 1;
                    if (isOne) {
                        JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
                        JSONObject upConfInfo = thisInfoConflictInfo.getJSONObject((task.getDateIndex() - 1) + "");
                        for (int upI = (task.getDateIndex()-1); upI >= 0; upI--) {
                            upConfInfo = thisInfoConflictInfo.getJSONObject((upI) + "");
                            if (null != upConfInfo) {
                                break;
                            }
                        }
                        if (null != upConfInfo) {
                            Integer status = upConfInfo.getInteger("status");
                            if (status == 1) {
                                String upDep = upConfInfo.getString("dep");
                                String upGrpB = upConfInfo.getString("grpB");
                                if (!upDep.equals(dep) || !upGrpB.equals(grpB)) {
                                    System.out.println("--+ 进入前置修改全局任务信息 +-- :");
                                    updateObjTaskAllSetAllImageTasks(objTaskAll,allImageTasks,allImageTotalTime);
                                    System.out.println("修改后:");
                                    System.out.println(JSON.toJSONString(objTaskAll));
                                    System.out.println(JSON.toJSONString(allImageTasks));
                                }
                            }
                        }
                    } else {
                        isOne = true;
                        if (!isFirst) {
                            isCanOnly = 0;
                            onlyRefState.put(random,1);
                            hTeStart = maxStaTime;
                        }
                    }
                    // 调用时间处理方法
                    JSONObject timeHandleInfo = timeHandle(task,hTeStart,grpB,dep,id_OInside,indexInside
                            ,0,random,isCanOnly,teDate,timeConflictCopy,0
                            ,sho,0,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                            ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                            ,clearStatus,thisInfo,allImageTeDate,false,depAllTime);
                    // 更新任务最初始开始时间
                    hTeStart = timeHandleInfo.getLong("hTeStart");
//                    System.out.println("最外层-3-2:"+hTeStart);
//                    System.out.println(JSON.toJSONString(timeHandleInfo));
                    // 添加结束时间
                    teFinList.add(hTeStart);

                    String id_OP = sonGetOrderFatherId(id_OInside, id_C, thisInfo, actionIdO, grpUNumAll);
                    JSONObject actByOPInfo = actionIdO.getJSONObject(id_OP);
                    JSONObject layerInfo = actByOPInfo.getJSONObject(oDate.getString("layer"));
                    JSONObject oPDateInfo = layerInfo.getJSONObject(oDate.getString("id_PF"));
//                    JSONObject oPDateInfo = actionIdO.getJSONObject(id_OP).getJSONObject(oDate.getString("layer")).getJSONObject(oDate.getString("id_PF"));
                    JSONArray oDatesNew = oPDateInfo.getJSONArray("oDates");
                    JSONObject thisODate = oDatesNew.getJSONObject(i);
                    thisODate.put("teDate",teDate);
                    oDatesNew.set(i,thisODate);
                    oPDateInfo.put("oDates",oDatesNew);
//                    actionIdO.put(id_OP,oPDateInfo);
                    layerInfo.put(oDate.getString("id_PF"),oPDateInfo);
                    actByOPInfo.put(oDate.getString("layer"),layerInfo);
                    actionIdO.put(id_OP,actByOPInfo);

                    // 定义存储最后结束时间参数
                    long storageLastEndTime;
                    // 判断序号是为1层级
                    if (csSta == 1) {
                        // 获取实际结束时间
                        Long actualEndTime = timeHandleInfo.getLong("xFin");
//                System.out.println("xFinW:"+xFin);
                        // 定义存储判断实际结束时间是否为空
                        boolean isActualEndTime = false;
                        // 判断实际结束时间不等于空
                        if (null != actualEndTime) {
//                    System.out.println("xFin:"+xFin);
                            // 赋值实际结束时间
                            hTeStart = actualEndTime;
                            // 判断当前实际结束时间大于最大结束时间
                            if (actualEndTime > maxLastTime) {
                                // 判断大于则更新最大结束时间为当前结束时间
                                maxLastTime = actualEndTime;
                            }
                            // 设置不为空
                            isActualEndTime = true;
                        } else {
                            // 判断当前实际结束时间大于最大结束时间：注 ： xFin 和 task.getTePFinish() 有时候是不一样的，不能随便改
                            if (task.getTePFinish() > maxLastTime) {
                                // 判断大于则更新最大结束时间为当前结束时间
                                maxLastTime = task.getTePFinish();
                            }
                        }
//                System.out.println("maxSte:"+maxSte);
                        // 判断实际结束时间不为空
                        if (isActualEndTime) {
                            // 赋值结束时间
                            storageLastEndTime = actualEndTime;
                        } else {
                            // 赋值结束时间
                            storageLastEndTime = task.getTePFinish();
                        }
                        // 判断是第一次进入
                        if (canOnlyEnterOnce) {
                            // 添加设置第一层的开始时间
                            lastEndTime=task.getTePStart();
                            // 设置只能进入一次
                            canOnlyEnterOnce = false;
                        }
                    } else {
                        // 直接赋值最后结束时间
                        storageLastEndTime = hTeStart;
                    }
                    // 根据父id获取最后结束时间信息
                    JSONObject fatherGetEndTimeInfo = serialOneFatherLastTime.getJSONObject(id_PF);
                    // 判断最后结束时间信息为空
                    if (null == fatherGetEndTimeInfo) {
                        // 创建并且赋值最后结束时间
                        fatherGetEndTimeInfo = new JSONObject();
                        fatherGetEndTimeInfo.put(priorItem.toString(),0);
                    }
                    // 根据序号获取最后结束时间
                    Long aLong = fatherGetEndTimeInfo.getLong(priorItem.toString());
                    // 判断最后结束时间为空
                    if (null == aLong) {
                        // 为空，则直接添加最后结束时间信息
                        fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
                        serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
                    } else {
                        // 不为空，则判断当前最后结束时间大于已存在的最后结束时间
                        if (storageLastEndTime > aLong) {
                            // 判断当前最后结束时间大于，则更新最后结束时间为当前结束时间
                            fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
                            serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
                        }
                    }
                    initialStartTime=hTeStart;
                    if (getThisInfoIsFinalPart(thisInfo) && getThisInfoIsConflict(thisInfo)) {
                        System.out.println("开始处理冲突任务:");
                        JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
                        JSONObject thisInfoConflictInfoSon = thisInfoConflictInfo.getJSONObject( i+ "");
                        thisInfoConflictInfoSon.put("status",1);
                        thisInfoConflictInfo.put(i+"",thisInfoConflictInfoSon);
                        setThisInfoConflictInfo(thisInfo,thisInfoConflictInfo);

                        JSONObject thisInfoQuiltConflictInfo = getThisInfoQuiltConflictInfo(thisInfo);
                        List<Task> conflict = new ArrayList<>();
                        JSONArray conflictArr = thisInfoQuiltConflictInfo.getJSONArray("conflict");
                        for (int c = 0; c < conflictArr.size(); c++) {
                            conflict.add(JSONObject.parseObject(JSON.toJSONString(conflictArr.getJSONObject(c)), Task.class));
                        }
                        conflict.sort(Comparator.comparing(Task::getPriority));
                        JSONObject indexInfo = thisInfoQuiltConflictInfo.getJSONObject("indexInfo");
                        JSONObject indexInfoNew = new JSONObject();
                        for (int conf = 0; conf < conflict.size(); conf++) {
                            Task taskConflict = conflict.get(conf);
                            String id_OConf = taskConflict.getId_O();
                            Integer dateIndexConf = taskConflict.getDateIndex();
                            for (String indInfoKey : indexInfo.keySet()) {
                                JSONObject object = indexInfo.getJSONObject(indInfoKey);
                                if (object.getString("id_O").equals(id_OConf)
                                        && Objects.equals(object.getInteger("dateIndex"), dateIndexConf)) {
                                    indexInfoNew.put(conf+"", object);
                                }
                            }
                        }
                        thisInfoQuiltConflictInfo.put("indexInfo",indexInfoNew);
                        thisInfoQuiltConflictInfo.put("conflict",conflict);
                        System.out.println(JSON.toJSONString(thisInfoQuiltConflictInfo));
                        setThisInfoQuiltConflictInfo(thisInfo,thisInfoQuiltConflictInfo);

                        updateAllImageTasksSetObjTaskAll(objTaskAll,allImageTasks,allImageTotalTime);

                        dgHandleFollowUpConflict(thisInfo,grpB,dep,id_C,objTaskAll,depAllTime,random,timeConflictCopy,sho,csSta
                                ,randomAll,xbAndSbAll,actionIdO,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                ,clearStatus,allImageTeDate,conflict);
                    }
                }

                // 调用任务最后处理方法
                timeZjServiceComprehensive.taskLastHandle(timeConflictCopy,id_C,randomAll,objTaskAll
                        ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,salesOrderData.getId()
                        ,objOrderList,actionIdO,allImageTeDate,depAllTime,thisInfo);

                // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
                onlyFirstTimeStamp.remove(random);
                // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
                newestLastCurrentTimestamp.remove(random);
                // 根据当前唯一标识删除信息
                onlyRefState.remove(random);
                JSONObject thisInfoLastTeInfo = getThisInfoLastTeInfo(thisInfo);
                Long lastTePFin = thisInfoLastTeInfo.getLong("lastTePFin");
                partInfo.put("tePFinish",lastTePFin);
                prodInfo.put(prodId,partInfo);
                oDateObj.put(layer + "",prodInfo);
                int layerUp = layer-1;
                if (layerUp > 0) {
                    String id_PF = partInfo.getString("id_PF");
                    JSONObject prodInfoUp = oDateObj.getJSONObject(layerUp + "");
                    JSONObject partInfoUp = prodInfoUp.getJSONObject(id_PF);
                    JSONArray arrPStart = partInfoUp.getJSONArray("arrPStart");
                    arrPStart.add(qt.setJson("lastTePFin",lastTePFin,"time",thisInfoLastTeInfo.getLong("time")));
                    partInfoUp.put("arrPStart",arrPStart);
                    prodInfoUp.put(id_PF,partInfoUp);
                    oDateObj.put(layerUp + "",prodInfoUp);
                }
            }
            dgTime(layer-1,oDateObj,salesOrderData,id_C,grpUNumAll,wn0TPrior,false,teStart);
        }
    }

    /**
     * 递归处理后续冲突
     */
    @SuppressWarnings("unchecked")
    private void dgHandleFollowUpConflict(JSONObject thisInfo,String grpB,String dep,String id_C,JSONObject objTaskAll
            ,JSONObject depAllTime,String random,JSONObject timeConflictCopy,JSONObject sho,int csSta,String randomAll
            ,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject recordId_OIndexState,JSONObject storageTaskWhereTime
            ,JSONObject allImageTotalTime,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks
            ,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp,JSONObject onlyRefState
            ,JSONObject recordNoOperation,JSONObject clearStatus,JSONObject allImageTeDate,List<Task> conflict){
        JSONObject thisInfoQuiltConflictInfo = getThisInfoQuiltConflictInfo(thisInfo);
        Integer operateIndex = getQuiltConflictInfoOperateIndex(thisInfo);
        JSONObject indexInfo = thisInfoQuiltConflictInfo.getJSONObject("indexInfo");
        JSONObject indexInfoSon = indexInfo.getJSONObject(operateIndex + "");
        if (null != indexInfoSon) {
            Integer thisI = indexInfoSon.getInteger("thisI");
            Long thisTime = indexInfoSon.getLong("thisTime");
            JSONObject id_oAndIndexTaskInfo = indexInfoSon.getJSONObject("id_OAndIndexTaskInfo");
//            List<Task> conflict = new ArrayList<>();
//            JSONArray conflictArr = thisInfoQuiltConflictInfo.getJSONArray("conflict");
//            for (int c = 0; c < conflictArr.size(); c++) {
//                conflict.add(JSONObject.parseObject(JSON.toJSONString(conflictArr.getJSONObject(c)), Task.class));
//            }
//            System.out.println("排序前:");
//            System.out.println(JSON.toJSONString(conflict));
//            conflict.sort(Comparator.comparing(Task::getPriority));
//            System.out.println("排序后:");
//            System.out.println(JSON.toJSONString(conflict));
            // 获取指定的全局任务信息方法
            JSONObject tasksAndZon = getTasksAndZon(thisTime, grpB, dep, id_C,objTaskAll,depAllTime);
            // 获取任务信息
            Object[] tasksIs = isTasksNull(tasksAndZon,depAllTime.getLong(dep));
            List<Task> tasks = (List<Task>) tasksIs[0];
            long zon = (long) tasksIs[1];
            System.out.println(JSON.toJSONString(objTaskAll));
            System.out.println(JSON.toJSONString(allImageTasks));
            System.out.println("最后循序的输出: zon:"+zon+" - thisI:"+thisI+" - thisTime:"+thisTime);
            System.out.println(JSON.toJSONString(id_oAndIndexTaskInfo));
            System.out.println(JSON.toJSONString(conflict));
            System.out.println(JSON.toJSONString(tasks));
            timeZjServiceComprehensive.handleTimeConflictEndNew(thisI,tasks,conflict,zon,random,dep,grpB
                    ,timeConflictCopy,0,1,sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                    ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate
                    ,depAllTime,id_oAndIndexTaskInfo,operateIndex);
            updateObjTaskAllSetAllImageTasks(objTaskAll,allImageTasks,allImageTotalTime);
            dgHandleFollowUpConflict(thisInfo,grpB,dep,id_C,objTaskAll,depAllTime,random,timeConflictCopy,sho,csSta
                    ,randomAll,xbAndSbAll,actionIdO,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                    ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                    ,clearStatus,allImageTeDate,conflict);
        }
    }

    /**
     * 根据主订单和对应公司编号，删除时间处理信息
     * @param id_O	主订单编号
     * @param id_C	公司编号
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @Override
    public ApiResponse removeTime(String id_O, String id_C) {
        return timeZjServiceEmptyInsert.removeTime(id_O,id_C);
    }

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
    public ApiResponse timeSortFromNew(String dep, String grpB, long currentTime, int index, String id_C, long taPFinish) {
        return timeZjServiceNew.timeSortFromNew(dep,grpB,currentTime,index,id_C,taPFinish);
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
    public ApiResponse delOrAddAArrange(String id_C,String dep, JSONObject object) {
        return timeZjServiceNew.delOrAddAArrange(id_C,dep,object);
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
    public ApiResponse timeCalculation(String id_O, int index, int number) {
        return timeZjServiceNew.timeCalculation(id_O,index,number);
    }

    /**
     * 获取计算物料时间后的开始时间
     * @param id_O  订单编号
     * @param teStart   开始时间
     * @return  开始时间
     */
    @Override
    public ApiResponse getTeStart(String id_O, Long teStart) {
//        long maxTime = teStart(id_O);
//        // qtySafex
//        Order order = qt.getMDContent(id_O, "casItemx", Order.class);
//        if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
//            // 返回为空错误信息
//            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
//        }
//        JSONObject java = order.getCasItemx().getJSONObject("java");
//        JSONArray oDates = java.getJSONArray("oDates");
//        Map<String, HashSet<String>> compMap = new HashMap<>();
//        for (int i = 0; i < oDates.size(); i++) {
//            JSONObject oDate = oDates.getJSONObject(i);
//            int bmdpt = oDate.getInteger("bmdpt");
//            if (bmdpt == 3) {
//                String id_P = oDate.getString("id_P");
//                String id_C = oDate.getString("id_C");
//                HashSet<String> prodSet;
//                if (compMap.containsKey(id_C)) {
//                    prodSet = compMap.get(id_C);
//                } else {
//                    prodSet = new HashSet<>();
//                }
//                prodSet.add(id_P);
//                compMap.put(id_C,prodSet);
//            } else {
//                Long wntDur = oDate.getLong("wntDur");
//                if (bmdpt != 2 && wntDur <= 0) {
//                    // 返回为空错误信息
//                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_TIME_NULL.getCode(), "时间为空");
//                }
//            }
//        }
//        long maxTime = 0;
//        Map<String,Prod> prodMap = new HashMap<>();
//        for (String id_C : compMap.keySet()) {
//            HashSet<String> prodArr = compMap.get(id_C);
//            for (String id_P : prodArr) {
//                Prod prod;
//                if (prodMap.containsKey(id_P)) {
//                    prod = prodMap.get(id_P);
//                } else {
//                    prod = qt.getMDContent(id_P, "qtySafex", Prod.class);
//                    if (null != prod && null != prod.getQtySafex() && null != prod.getQtySafex().getDouble(id_C)) {
//                        prodMap.put(id_P,prod);
//                    } else {
////                        prod = null;
//                        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_TIME_NULL.getCode(), "时间为空");
//                    }
//                }
//                if (null != prod) {
//                    double hour = prod.getQtySafex().getDouble(id_C);
//                    long time = (long) ((hour*60)*60);
//                    if (time > maxTime) {
//                        maxTime = time;
//                    }
//                }
//            }
//        }
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), teStart+teStart(id_O));
    }

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
     * @param clearStatus 清理状态信息
     * @param thisInfo 当前处理通用信息存储
     * @param allImageTeDate 镜像任务所在日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    public JSONObject timeHandle(Task task,Long hTeStart,String grpB,String dep,String id_O,Integer index
            ,Integer isJumpDays,String random,Integer isCanOnlyOnceTimeEmptyInsert,JSONObject teDate
            ,JSONObject timeConflictCopy,Integer isGetTaskPattern,JSONObject sho,int isSaveAppearEmpty,int csSta
            ,String randomAll,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll
            ,JSONObject recordId_OIndexState,JSONObject storageTaskWhereTime,JSONObject allImageTotalTime
            ,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks,JSONObject onlyFirstTimeStamp
            ,JSONObject newestLastCurrentTimestamp,JSONObject onlyRefState,JSONObject recordNoOperation
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,boolean isComprehensiveHandle
            ,JSONObject depAllTime){
        // 创建返回结果对象
        JSONObject result = new JSONObject();
        // 跳天强制停止参数累加
        yiShu.put(randomAll,(yiShu.getInteger(randomAll)+1));
//        System.out.println("newest-外:");
//        System.out.println(JSON.toJSONString(newestLastCurrentTimestamp));

//        System.out.println("获取前:");
//        System.out.println(JSON.toJSONString(allImageTotalTime));
//        System.out.println();
//        System.out.println(JSON.toJSONString(allImageTasks));
//        System.out.println();
//        System.out.println(JSON.toJSONString(objTaskAll));
        // 调用获取任务综合信息方法
        Map<String, Object> jumpDay = getJumpDay(random, grpB, dep,0,0L,isGetTaskPattern
                ,task.getId_C(),xbAndSbAll,objTaskAll,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                ,newestLastCurrentTimestamp,depAllTime);

//        System.out.println(JSON.toJSONString(jumpDay));

        // 获取任务集合
        List<Task> tasks = (List<Task>) jumpDay.get("tasks");
        // 获取任务余剩时间
        long zon = (long) jumpDay.get("zon");
//        System.out.println("查询后的zon:"+zon);

//        System.out.println("输出外sho-q:");
//        System.out.println(sho);
        // 产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        JSONObject shoObj = sho.getJSONObject(task.getId_O());
        if (null != shoObj) {
            JSONObject shoObjIndex = shoObj.getJSONObject(task.getIndex().toString());
            if (null != shoObjIndex) {
                Integer prodState = shoObjIndex.getInteger("prodState");
//        System.out.println("输出外sho-h:");
//        System.out.println(sho);
                if (null!=prodState && prodState != -1) {
                    // 调用获取获取统一id_O和index存储记录状态信息方法
                    Integer storage = getStorage(task.getId_O(), task.getIndex(),recordId_OIndexState);
                    // storage == 0 正常状态存储、storage == 1 冲突状态存储、storage == 2 调用时间处理状态存储
                    if (storage == 0) {
                        // 调用写入存储记录状态方法
                        setStorage(2, task.getId_O(), task.getIndex(),recordId_OIndexState);
                    }
                }
            }
//        System.out.println("输出外sho-h2:");
//        System.out.println(sho);
        }

        // 调用时间处理方法2
        JSONObject jsonObject = timeHandleCore(zon, hTeStart, tasks, grpB, dep, id_O, index, task
                , isJumpDays, random, isCanOnlyOnceTimeEmptyInsert, teDate, timeConflictCopy
                , isGetTaskPattern, sho,isSaveAppearEmpty,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                ,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
        // 获取任务最初始开始时间
        hTeStart = jsonObject.getLong("hTeStart");
        result.put("hTeStart",hTeStart);
        result.put("endTime",jsonObject.getLong("endTime"));
        result.put("xFin",jsonObject.getLong("xFin"));
//        System.out.println(JSON.toJSONString(tasks));
        JSONArray tasksArr = new JSONArray();
        for (Task task1 : tasks) {
            tasksArr.add(JSONObject.parseObject(JSON.toJSONString(task1)));
        }
        result.put("tasks",tasksArr);
        return result;
    }

    /**
     * 时间处理核心方法
     * @param zon	任务余剩时间
     * @param hTeStart	任务最初始开始时间
     * @param tasks	任务集合
     * @param grpB	组别
     * @param dep	部门
     * @param id_O	订单编号
     * @param index	订单下标
     * @param task	当前处理任务
     * @param isJumpDays	保存是否是时间处理方法调用的跳天操作：isJumpDays == 0 不是、isJumpDays == 1 是
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
     * @param clearStatus 清理状态信息
     * @param thisInfo 当前处理通用信息存储
     * @param allImageTeDate 镜像任务所在日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private JSONObject timeHandleCore(Long zon,Long hTeStart,List<Task> tasks,String grpB,String dep
            ,String id_O,Integer index,Task task,Integer isJumpDays,String random
            ,Integer isCanOnlyOnceTimeEmptyInsert,JSONObject teDate,JSONObject timeConflictCopy
            ,Integer isGetTaskPattern,JSONObject sho,int isSaveAppearEmpty,int csSta,String randomAll
            ,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll
            ,JSONObject recordId_OIndexState,JSONObject storageTaskWhereTime,JSONObject allImageTotalTime
            ,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks
            ,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp
            ,JSONObject onlyRefState,JSONObject recordNoOperation,JSONObject clearStatus,JSONObject thisInfo
            ,JSONObject allImageTeDate,boolean isComprehensiveHandle,JSONObject depAllTime){
//        System.out.println("时间处理核心方法-1:");
//        System.out.println(JSON.toJSONString(tasks));
        // 创建返回结果对象
        JSONObject result = new JSONObject();
//        System.out.println("sho:");
//        System.out.println(JSON.toJSONString(sho));
        // 产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer prodState = sho.getJSONObject(task.getId_O())
                .getJSONObject(task.getIndex().toString()).getInteger("prodState");
        long endTime = 0;
        // 判断任务余剩时间不等于0
        if (zon != 0) {
            // 控制是否跳天参数：isJumpDay == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
            int isJumpDay = 0;
            // 控制是否结束循环参数：isEndLoop == 2 结束循环、isP == 0 | 3 | 4 继续循环
            int isEndLoop;
            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
            int isProblemState = 0;
            boolean isSetEnd = true;
            boolean isSetTimeHandleEnd = true;
            // 判断任务最初始开始时间为0
            if (hTeStart == 0) {
//                System.out.println("进入hTeStart == 0---");
//                System.out.println(JSON.toJSONString(task));
//                System.out.println(JSON.toJSONString(tasks));
                // 遍历任务集合
                for (int i = 0; i < tasks.size(); i++) {
                    // 根据下标获取对比任务信息1
                    Task contrastTaskOne = tasks.get(i);
                    // 判断下标加1小于任务集合长度
                    if ((i + 1) < tasks.size()) {
                        // 根据下标获取对比任务信息2
                        Task contrastTaskTwo = tasks.get(i + 1);
//                        System.out.println("taskzongk:");
//                        System.out.println(JSON.toJSONString(task1));
//                        System.out.println(JSON.toJSONString(task2));
                        System.out.println("emptyInsertTimeInfo-q:");
                        System.out.println(JSON.toJSONString(tasks));
                        // 调用计算空插时间方法
                        JSONObject emptyInsertTimeInfo =
                                timeZjServiceEmptyInsert.calculationEmptyInsertTime( task, contrastTaskOne
                                , contrastTaskTwo, tasks, i, zon,0,random,teDate,dep,grpB
                                ,prodState,storageTaskWhereTime,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                        ,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
                        System.out.println("emptyInsertTimeInfo-h:");
                        System.out.println(JSON.toJSONString(tasks));
                        // 获取存储问题状态参数
                        isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                        if (isEndLoop == 2) {
                            // 获取控制是否跳天参数
                            isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                            // 获取任务余剩时间
                            zon = emptyInsertTimeInfo.getLong("zon");
                            hTeStart = emptyInsertTimeInfo.getLong("tePFinish");
                            endTime = emptyInsertTimeInfo.getLong("endTime");
                            break;
                        } else if (isEndLoop == 4) {
                            // 获取任务余剩时间
                            zon = emptyInsertTimeInfo.getLong("zon");
                        }
                    }
                }
            } else {
                // 存储任务冲突信息参数：isTaskConflictInfo == 0 控制只进入一次时间空插全流程处理-可以进入、isL > 0 记录冲突信息，不可进入-控制只进入一次时间空插全流程处理
                int isTaskConflictInfo = 0;
//                System.out.println("进入hTeStart != 0--- isTaskConflictInfo:"+isTaskConflictInfo);
                // 遍历任务集合
                for (int i = 0; i < tasks.size(); i++) {
                    // 根据下标获取对比任务信息1
                    Task contrastTaskOne = tasks.get(i);
                    // 判断下标加1小于任务集合长度
                    if ((i + 1) < tasks.size()) {
                        // 根据下标获取对比任务信息2
                        Task contrastTaskTwo = tasks.get(i + 1);
//                        System.out.println("task-zon:");
//                        System.out.println(JSON.toJSONString(task1));
//                        System.out.println(JSON.toJSONString(task2));
                        // 判断进入条件
                        // isJumpDays : 保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
                        // isTaskConflictInfo : 存储任务冲突信息参数：isTaskConflictInfo == 0 控制只进入一次时间空插全流程处理-可以进入、isL > 0 记录冲突信息，不可进入-控制只进入一次时间空插全流程处理
                        // isCanOnlyOnceTimeEmptyInsert : 控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
                        if (isJumpDays == 0 && isTaskConflictInfo == 0 || isCanOnlyOnceTimeEmptyInsert == 1) {
//                            System.out.println("进入if");
                            isCanOnlyOnceTimeEmptyInsert = 0;
                            // 判断当前任务的开始时间大于等于对比任务信息1的结束时间
                            if (task.getTePStart() >= contrastTaskOne.getTePFinish()) {
                                // 定义存储调用[计算空插时间方法]的结果
                                JSONObject emptyInsertTimeInfo;
                                // 判断当前任务的结束时间小于对比任务信息2的开始时间
                                if (task.getTePFinish() < contrastTaskTwo.getTePStart()) {
                                    System.out.println(JSON.toJSONString(task));
                                    System.out.println(JSON.toJSONString(contrastTaskOne));
                                    System.out.println(JSON.toJSONString(contrastTaskTwo));
                                    System.out.println("进入这里-1-1-q:");
                                    // 调用计算空插时间方法并赋值
                                    emptyInsertTimeInfo = timeZjServiceEmptyInsert.calculationEmptyInsertTime(
                                            task, contrastTaskOne, contrastTaskTwo, tasks, i, zon
                                            , 1,random,teDate
                                            ,dep,grpB,prodState,storageTaskWhereTime,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
                                    hTeStart = emptyInsertTimeInfo.getLong("tePFinish");
                                    endTime = emptyInsertTimeInfo.getLong("endTime");
                                    System.out.println("进入这里-1-1-h:"+hTeStart);
                                    System.out.println(JSON.toJSONString(emptyInsertTimeInfo));
                                } else if (task.getTePStart() <= contrastTaskTwo.getTePStart()
                                        && task.getPriority() >= contrastTaskTwo.getPriority()) {
                                    System.out.println("进入这里-1-2-q:");
//                                    System.out.println(JSON.toJSONString(tasks));
                                    // 调用计算空插时间方法并赋值
                                    emptyInsertTimeInfo = timeZjServiceEmptyInsert.calculationEmptyInsertTime(
                                            task, contrastTaskOne, contrastTaskTwo, tasks, i, zon
                                            , 2,random,teDate
                                            ,dep,grpB,prodState,storageTaskWhereTime,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
                                    endTime = emptyInsertTimeInfo.getLong("endTime");
//                                    System.out.println("进入这里-1-2-h:");
//                                    System.out.println(JSON.toJSONString(tasks));
//                                    System.out.println(JSON.toJSONString(emptyInsertTimeInfo));
                                } else if (task.getTePStart() < contrastTaskOne.getTePFinish()
                                        && contrastTaskOne.getPriority() != -1) {
//                                    System.out.println("进入这里-1-3");
                                    isTaskConflictInfo = 3;
                                    break;
                                } else {
                                    // 判断当前任务的优先级小于对比任务信息2的优先级
                                    if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                        // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                        if (task.getTePStart() >= contrastTaskTwo.getTePStart()
                                                && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                            isTaskConflictInfo = 3;
                                            System.out.println("进入-冲突-最开始-这里-1");
//                                            System.out.println("进入-冲突-最开始-sho");
//                                            System.out.println(JSON.toJSONString(sho));
                                            // 调用处理时间冲突方法
                                            JSONObject timeConflictInfo
                                                    = timeZjServiceTimeConflict.handleTimeConflict(task
                                                    , contrastTaskOne, contrastTaskTwo, zon, tasks, i
                                                    , random, grpB, dep, teDate, timeConflictCopy
                                                    , isGetTaskPattern, 0
                                                    , sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                                    ,recordId_OIndexState,storageTaskWhereTime
                                                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                    ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                            // 获取任务余剩时间
                                            zon = timeConflictInfo.getLong("zon");
                                            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isProblemState = timeConflictInfo.getInteger("isProblemState");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 2;
//                                            System.out.println("处理完成:");
                                            isSetEnd = timeConflictInfo.getBoolean("isSetEnd");
//                                            isSetEnd = true;
//                                            System.out.println(JSON.toJSONString(allImageTasks));
                                            hTeStart = timeConflictInfo.getLong("tePFinish");
                                            System.out.println("进入-冲突-最开始-这里-1-h:"+isSetEnd+" - hTeStart:"+hTeStart);
                                            endTime = timeConflictInfo.getLong("endTime");
//                                            System.out.println("进入-冲突-最开始-这里-1");
                                            break;
                                        } else
                                        if (contrastTaskTwo.getTePStart() >= task.getTePStart()
                                                && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-2");
//                                            System.out.println(JSON.toJSONString(task));
//                                            System.out.println(JSON.toJSONString(contrastTaskTwo));
//                                            addRecordGetIntoOperation(randomAll,"进入-冲突-最开始-这里-2",recordNoOperation);
                                            isTaskConflictInfo = 4;
                                            // 调用处理时间冲突方法
                                            JSONObject timeConflictInfo
                                                    = timeZjServiceTimeConflict.handleTimeConflict(task
                                                    , contrastTaskOne, contrastTaskTwo, zon, tasks, i
                                                    , random, grpB, dep, teDate, timeConflictCopy
                                                    , isGetTaskPattern, 0
                                                    , sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                                    ,recordId_OIndexState,storageTaskWhereTime
                                                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                    ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                            // 获取任务余剩时间
                                            zon = timeConflictInfo.getLong("zon");
                                            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isProblemState = timeConflictInfo.getInteger("isProblemState");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 2;
//                                            System.out.println("处理完成:");
                                            isSetEnd = timeConflictInfo.getBoolean("isSetEnd");
//                                            isSetEnd = true;
//                                            System.out.println(JSON.toJSONString(allImageTasks));
                                            hTeStart = timeConflictInfo.getLong("tePFinish");
                                            System.out.println("进入-冲突-最开始-这里-2-h:"+isSetEnd+" - hTeStart:"+hTeStart);
                                            endTime = timeConflictInfo.getLong("endTime");
                                            break;
                                        } else if (task.getTePFinish() > contrastTaskTwo.getTePStart()
                                                && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
//                                            System.out.println(JSON.toJSONString(task));
//                                            System.out.println(JSON.toJSONString(contrastTaskOne));
//                                            System.out.println(JSON.toJSONString(contrastTaskTwo));
                                            System.out.println("进入-冲突-最开始-这里-3");
//                                            addRecordGetIntoOperation(randomAll,"进入-冲突-最开始-这里-3",recordNoOperation);
                                            isTaskConflictInfo = 5;
                                            // 调用处理时间冲突方法
                                            JSONObject timeConflictInfo
                                                    = timeZjServiceTimeConflict.handleTimeConflict(task
                                                    , contrastTaskOne, contrastTaskTwo, zon, tasks, i
                                                    , random, grpB, dep, teDate, timeConflictCopy
                                                    , isGetTaskPattern, 0
                                                    , sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                                    ,recordId_OIndexState,storageTaskWhereTime
                                                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                    ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle
                                                    ,depAllTime);
                                            // 获取任务余剩时间
                                            zon = timeConflictInfo.getLong("zon");
                                            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isProblemState = timeConflictInfo.getInteger("isProblemState");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 2;
//                                            System.out.println("处理完成:");
                                            isSetEnd = timeConflictInfo.getBoolean("isSetEnd");
//                                            isSetEnd = true;
//                                            System.out.println(JSON.toJSONString(allImageTasks));
                                            hTeStart = timeConflictInfo.getLong("tePFinish");
                                            System.out.println("进入-冲突-最开始-这里-2-h:"+isSetEnd+" - hTeStart:"+hTeStart);
                                            endTime = timeConflictInfo.getLong("endTime");
                                            break;
                                        }
                                    } else if (task.getPriority() < contrastTaskOne.getPriority()){
                                        // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                        if (task.getTePStart() >= contrastTaskTwo.getTePStart()
                                                && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-1--1");
                                            isTaskConflictInfo = 33;
                                            // 调用处理时间冲突方法
                                            JSONObject timeConflictInfo
                                                    = timeZjServiceTimeConflict.handleTimeConflict(task
                                                    ,contrastTaskOne,contrastTaskTwo,zon,tasks,i,random
                                                    ,grpB,dep,teDate,timeConflictCopy,isGetTaskPattern
                                                    ,0,sho,csSta,randomAll,xbAndSbAll
                                                    ,actionIdO
                                                    ,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                                                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                    ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle
                                                    ,depAllTime);
                                            // 获取任务余剩时间
                                            zon = timeConflictInfo.getLong("zon");
                                            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isProblemState = timeConflictInfo.getInteger("isProblemState");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 2;
                                            endTime = timeConflictInfo.getLong("endTime");
                                            System.out.println("进入-冲突-最开始-这里-1--1-h:");
                                            break;
                                        } else
                                        if (contrastTaskTwo.getTePStart() >= task.getTePStart()
                                                && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-2--1");
                                            addRecordGetIntoOperation(randomAll,"进入-冲突-最开始-这里-2--1",recordNoOperation);
                                            isTaskConflictInfo = 44;
                                            break;
                                        } else if (task.getTePFinish() > contrastTaskTwo.getTePStart()
                                                && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-3--1");
                                            addRecordGetIntoOperation(randomAll,"进入-冲突-最开始-这里-3--1",recordNoOperation);
                                            isTaskConflictInfo = 55;
                                            break;
                                        }
                                    }
//                                    System.out.println("弹回---");
                                    isCanOnlyOnceTimeEmptyInsert = 1;
                                    continue;
                                }
                                // 获取控制是否结束循环参数
                                isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                if (isEndLoop == 2) {
                                    // 获取控制是否跳天参数
                                    isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                    // 获取任务余剩时间
                                    zon = emptyInsertTimeInfo.getLong("zon");
                                    break;
                                } else if (isEndLoop == 4) {
                                    // 获取任务余剩时间
                                    zon = emptyInsertTimeInfo.getLong("zon");
                                    isTaskConflictInfo = 1;
                                }
                                else {
                                    isTaskConflictInfo = 1;
                                }
                            } else {
//                                System.out.println("进入这里-2-1");
                                // 判断当前任务优先级小于对比任务信息1的优先级
                                if (task.getPriority() < contrastTaskOne.getPriority()) {
                                    // 判断当前任务的开始时间大于等于对比任务信息1的开始时间，并且当前任务的开始时间小于对比任务信息1的结束时间
                                    if (task.getTePStart() >= contrastTaskOne.getTePStart()
                                            && task.getTePStart() < contrastTaskOne.getTePFinish()) {
                                        isTaskConflictInfo = 3;
                                        System.out.println("进入-冲突-中间-这里-1");
                                        addRecordGetIntoOperation(randomAll,"进入-冲突-中间-这里-1",recordNoOperation);
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isJumpDay = 2;
                                        break;
                                    } else
                                    if (contrastTaskOne.getTePStart() >= task.getTePStart()
                                            && contrastTaskOne.getTePFinish() <= task.getTePFinish()) {
                                        System.out.println("进入-冲突-中间-这里-2");
                                        addRecordGetIntoOperation(randomAll,"进入-冲突-中间-这里-2",recordNoOperation);
                                        isTaskConflictInfo = 4;
                                        break;
                                    } else if (task.getTePFinish() > contrastTaskOne.getTePStart()
                                            && task.getTePFinish() < contrastTaskOne.getTePFinish()) {
                                        System.out.println("进入-冲突-中间-这里-3");
                                        addRecordGetIntoOperation(randomAll,"进入-冲突-中间-这里-3",recordNoOperation);
                                        isTaskConflictInfo = 5;
                                        break;
                                    } else {
                                        System.out.println("进入输出ji---1-q:");

                                        // 调用计算空插时间方法
                                        JSONObject emptyInsertTimeInfo
                                                = timeZjServiceEmptyInsert.calculationEmptyInsertTime( task
                                                , contrastTaskOne, contrastTaskTwo
                                                , tasks, i, zon,0,random,teDate,dep,grpB
                                                ,prodState,storageTaskWhereTime,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
                                        System.out.println("进入输出ji---1-h:");
                                        // 获取控制是否结束循环参数
                                        isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                        if (isEndLoop == 2) {
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                            // 获取任务余剩时间
                                            zon = emptyInsertTimeInfo.getLong("zon");
                                            endTime = emptyInsertTimeInfo.getLong("endTime");
                                            break;
                                        } else if (isEndLoop == 4) {
                                            // 获取任务余剩时间
                                            zon = emptyInsertTimeInfo.getLong("zon");
                                        }
                                    }
                                } else if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                    // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                    if (task.getTePStart() >= contrastTaskTwo.getTePStart()
                                            && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                        isTaskConflictInfo = 3;
                                        System.out.println("进入-冲突-最后面-这里-1");
                                        addRecordGetIntoOperation(randomAll,"进入-冲突-最后面-这里-1",recordNoOperation);
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isJumpDay = 2;
                                        break;
                                    } else
                                    if (contrastTaskTwo.getTePStart() >= task.getTePStart()
                                            && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                        isTaskConflictInfo = 4;
                                        System.out.println("进入-冲突-最后面-这里-2-q:");
                                        // 调用处理时间冲突方法
                                        JSONObject timeConflictInfo
                                                = timeZjServiceTimeConflict.handleTimeConflict(task
                                                , contrastTaskOne
                                                , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                                , timeConflictCopy, isGetTaskPattern, 0
                                                , sho,csSta,randomAll,xbAndSbAll,actionIdO
                                                ,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                                                ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle
                                                ,depAllTime);
                                        // 获取任务余剩时间
                                        zon = timeConflictInfo.getLong("zon");
                                        // 获取存储问题状态参数
                                        isProblemState = timeConflictInfo.getInteger("isProblemState");
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isJumpDay = 2;
                                        endTime = timeConflictInfo.getLong("endTime");
                                        System.out.println("进入-冲突-最后面-这里-2-h:");
                                        break;
                                    } else
                                    if (task.getTePFinish() > contrastTaskTwo.getTePStart()
                                            && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                        System.out.println("进入-冲突-最后面-这里-3-q:");
//                                        addRecordGetIntoOperation(randomAll,"进入-冲突-最后面-这里-3",recordNoOperation);
                                        isTaskConflictInfo = 5;
                                        // 调用处理时间冲突方法
                                        JSONObject timeConflictInfo
                                                = timeZjServiceTimeConflict.handleTimeConflict(task
                                                , contrastTaskOne
                                                , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                                , timeConflictCopy, isGetTaskPattern, 0
                                                , sho,csSta,randomAll,xbAndSbAll,actionIdO
                                                ,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                                                ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle
                                                ,depAllTime);
                                        // 获取任务余剩时间
                                        zon = timeConflictInfo.getLong("zon");
                                        // 获取存储问题状态参数
                                        isProblemState = timeConflictInfo.getInteger("isProblemState");
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isJumpDay = 2;
                                        endTime = timeConflictInfo.getLong("endTime");
                                        System.out.println("进入-冲突-最后面-这里-3-h:");
                                        break;
                                    } else {
                                        System.out.println(JSON.toJSONString(task));
                                        System.out.println(JSON.toJSONString(contrastTaskOne));
                                        System.out.println(JSON.toJSONString(contrastTaskTwo));
                                        System.out.println("进入输出ji---1-2-q:");

                                        // 调用计算空插时间方法
                                        JSONObject emptyInsertTimeInfo
                                                = timeZjServiceEmptyInsert.calculationEmptyInsertTime(task
                                                , contrastTaskOne
                                                , contrastTaskTwo, tasks, i, zon,0
                                                ,random,teDate,dep,grpB,prodState,storageTaskWhereTime
                                                ,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                                ,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);

                                        System.out.println("进入输出ji---1-2-h:");
                                        // 获取控制是否结束循环参数
                                        isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                        if (isEndLoop == 2) {
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                            // 获取任务余剩时间
                                            zon = emptyInsertTimeInfo.getLong("zon");
                                            endTime = emptyInsertTimeInfo.getLong("endTime");
                                            break;
                                        } else if (isEndLoop == 4) {
                                            // 获取任务余剩时间
                                            zon = emptyInsertTimeInfo.getLong("zon");
                                        }
                                    }
                                } else {
//                                    System.out.println("--优先级跳过--");
                                    // 获取余剩时间（对比任务2的开始时间-对比任务1的结束时间）
                                    long remainingTime = contrastTaskTwo.getTePStart()
                                            - contrastTaskOne.getTePFinish();
                                    // 判断余剩时间大于0
                                    if (remainingTime > 0) {
                                        // 获取时间差（余剩时间-当前任务总时间）
                                        long timeDifference = remainingTime - task.getWntDurTotal();
//                                        System.out.println("优先级:");
//                                        System.out.println(JSON.toJSONString(task));
//                                        System.out.println(JSON.toJSONString(contrastTaskOne));
//                                        System.out.println(JSON.toJSONString(contrastTaskTwo));
                                        // 判断时间差大于等于0
                                        if (timeDifference >= 0) {
                                            System.out.println("--优先级跳过--1");
                                            Task contrastTaskThree = TaskObj.getTaskX(
                                                    contrastTaskOne.getTePFinish()
                                                    ,(contrastTaskOne.getTePFinish()+task.getWntDurTotal())
                                                    ,task.getWntDurTotal(),task);
//                                            System.out.println(JSON.toJSONString(contrastTaskThree));
                                            // 指定任务集合下标为任务下标+1添加任务信息
                                            tasks.add(i+1,contrastTaskThree);
                                            endTime = tasks.get(0).getTePStart();
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp)+"",0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getDateIndex(),getTeS(random,grpB,dep
                                                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    ,prodState,storageTaskWhereTime);
                                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    , task.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                            result.put("xFin",contrastTaskThree.getTePFinish());
                                            // 任务余剩时间累减
                                            zon -= task.getWntDurTotal();
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 1;
                                            hTeStart = contrastTaskThree.getTePFinish();
                                            break;
                                        } else {
                                            System.out.println("--优先级跳过--2");
                                            // 获取时间差2（当前任务总时间-余剩时间）
                                            long timeDifferenceNew = task.getWntDurTotal() - remainingTime;
                                            Task contrastTaskThree = TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                                    ,(contrastTaskOne.getTePFinish()+remainingTime),remainingTime,task);
//                                            System.out.println(JSON.toJSONString(contrastTaskThree));
                                            // 指定任务集合下标为任务下标+1添加任务信息
                                            tasks.add(i+1,contrastTaskThree);
                                            endTime = tasks.get(0).getTePStart();
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp)+"",0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getDateIndex(),getTeS(random,grpB,dep
                                                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    ,prodState,storageTaskWhereTime);
                                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    , remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                                            // 更新任务总时间
                                            task.setWntDurTotal(timeDifferenceNew);
                                            // 更新任务开始时间
                                            task.setTePStart(contrastTaskOne.getTePStart());
                                            // 更新任务结束时间
                                            task.setTePFinish(contrastTaskOne.getTePFinish());
                                            // 任务余剩时间累减
                                            zon -= remainingTime;
                                            hTeStart = contrastTaskThree.getTePFinish();
                                        }
                                    }
                                }
                            }
                        } else {
//                            System.out.println("进入else");
                            // 判断当前任务优先级小于对比任务2的优先级
                            if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                // 判断当前任务开始时间小于对比任务2的开始时间，并且当前任务的开始时间小于对比任务2的结束时间
                                if (task.getTePStart() < contrastTaskTwo.getTePStart()
                                        && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                    isTaskConflictInfo = 3;
//                                    System.out.println("进入-冲突-新最开始-这里-1");
                                    // 调用处理时间冲突方法
                                    JSONObject timeConflictInfo
                                            = timeZjServiceTimeConflict.handleTimeConflict(task, contrastTaskOne
                                            , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                            , timeConflictCopy, isGetTaskPattern, 1, sho
                                            ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                            ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                            ,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate
                                            ,isComprehensiveHandle,depAllTime);
                                    // 获取任务余剩时间
                                    zon = timeConflictInfo.getLong("zon");
                                    // 获取存储问题状态参数
                                    isProblemState = timeConflictInfo.getInteger("isProblemState");
                                    hTeStart = timeConflictInfo.getLong("tePFinish");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    endTime = timeConflictInfo.getLong("endTime");
//                                    System.out.println("进入-冲突-新最开始-这里-1-h:"+hTeStart);
                                    isSetTimeHandleEnd = false;
                                    break;
                                } else {
                                    System.out.println("进入else-1-q:");

                                    // 调用计算空插时间方法
                                    JSONObject emptyInsertTimeInfo
                                            = timeZjServiceEmptyInsert.calculationEmptyInsertTime( task, contrastTaskOne
                                            , contrastTaskTwo, tasks, i, zon,0,random,teDate,dep,grpB
                                            ,prodState,storageTaskWhereTime,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                            ,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
//                                    System.out.println("进入else-1-h:");
                                    // 获取控制是否结束循环参数
                                    isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                    // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                    if (isEndLoop == 2) {
                                        // 获取控制是否跳天参数
                                        isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                        // 获取任务余剩时间
                                        zon = emptyInsertTimeInfo.getLong("zon");
                                        endTime = emptyInsertTimeInfo.getLong("endTime");
                                        break;
                                    } else if (isEndLoop == 4) {
                                        // 获取任务余剩时间
                                        zon = emptyInsertTimeInfo.getLong("zon");
                                    }
                                }
                            } else if (task.getPriority() < contrastTaskOne.getPriority()){
//                                System.out.println(JSON.toJSONString(task));
//                                System.out.println(JSON.toJSONString(contrastTaskOne));
//                                System.out.println(JSON.toJSONString(contrastTaskTwo));
                                addRecordGetIntoOperation(randomAll,"进入-冲突-新最开始-这里-1--1",recordNoOperation);
                                System.out.println("进入-冲突-新最开始-这里-1--1");
                                isTaskConflictInfo = 4;
//                                    System.out.println("进入-冲突-新最开始-这里-1");
                                // 调用处理时间冲突方法
                                JSONObject timeConflictInfo
                                        = timeZjServiceTimeConflict.handleTimeConflict(task, contrastTaskOne
                                        , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                        , timeConflictCopy, isGetTaskPattern, 1, sho
                                        ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                        ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                        ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                        ,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate
                                        ,isComprehensiveHandle,depAllTime);
                                // 获取任务余剩时间
                                zon = timeConflictInfo.getLong("zon");
                                // 获取存储问题状态参数
                                isProblemState = timeConflictInfo.getInteger("isProblemState");
                                hTeStart = timeConflictInfo.getLong("tePFinish");
                                // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                isJumpDay = 2;
                                endTime = timeConflictInfo.getLong("endTime");
//                                    System.out.println("进入-冲突-新最开始-这里-1-h:"+hTeStart);
                                isSetTimeHandleEnd = false;
                                break;
                            } else {
//                                System.out.println("进入else-2-q:");
//                                System.out.println(JSON.toJSONString(tasks));
                                // 调用计算空插时间方法
                                JSONObject emptyInsertTimeInfo
                                        = timeZjServiceEmptyInsert.calculationEmptyInsertTime( task, contrastTaskOne, contrastTaskTwo
                                        , tasks, i, zon,0,random,teDate,dep,grpB,prodState
                                        ,storageTaskWhereTime,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                        ,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
//                                System.out.println("进入else-2-h:");
//                                System.out.println(JSON.toJSONString(tasks));
                                // 获取控制是否结束循环参数
                                isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                if (isEndLoop == 2) {
                                    // 获取控制是否跳天参数
                                    isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                    // 获取任务余剩时间
                                    zon = emptyInsertTimeInfo.getLong("zon");
                                    hTeStart = emptyInsertTimeInfo.getLong("tePFinish");
                                    endTime = emptyInsertTimeInfo.getLong("endTime");
                                    break;
                                } else if (isEndLoop == 4) {
                                    // 获取任务余剩时间
                                    zon = emptyInsertTimeInfo.getLong("zon");
                                }
                            }
                        }
                    }
                }
                // 判断冲突信息
                if (isTaskConflictInfo >= 3) {
                    System.out.println("出现时间冲突!isTaskConflictInfo:"+isTaskConflictInfo);
                    System.out.println(JSON.toJSONString(allImageTasks));
                }
            }
//            System.out.println("进入收尾:endTime-q:"+endTime);
//            System.out.println();
            if (isSetTimeHandleEnd) {
                // 调用处理[时间处理方法2]结束操作的方法
                JSONObject timeHandleEndInfo = timeZjServiceComprehensive.timeHandleEnd(isJumpDay, isGetTaskPattern, tasks, grpB, dep, random, zon
                        , isCanOnlyOnceTimeEmptyInsert, hTeStart, task, id_O, index, teDate, timeConflictCopy
                        ,sho,isProblemState,isSaveAppearEmpty,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                        ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                        ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus,thisInfo
                        ,allImageTeDate,isSetEnd,endTime,depAllTime);
                hTeStart = timeHandleEndInfo.getLong("hTeStart");
                endTime = timeHandleEndInfo.getLong("endTime");
            } else {
                System.out.println("新的-最后操作跳过-:");
            }
//            System.out.println("返回后:"+hTeStart);
//            System.out.println("进入收尾:endTime-h:"+endTime);
        } else {
            // 存储任务冲突信息参数：isTaskConflictInfo == 0 控制只进入一次时间空插全流程处理-可以进入、isL > 0 记录冲突信息，不可进入-控制只进入一次时间空插全流程处理
            int isTaskConflictInfo = 0;
            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
            int isJumpDay = 0;
            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
            int isProblemState = 0;
            boolean isSetEnd = true;
            // 遍历任务集合
            for (int i = 0; i < tasks.size(); i++) {
                // 根据下标获取对比任务信息1
                Task contrastTaskOne = tasks.get(i);
                // 判断下标加1小于任务集合长度
                if ((i + 1) < tasks.size()) {
                    // 根据下标获取对比任务信息2
                    Task contrastTaskTwo = tasks.get(i + 1);
//                    System.out.println("task-x-zon:");
//                    System.out.println(JSON.toJSONString(task));
//                    System.out.println(JSON.toJSONString(task1));
//                    System.out.println(JSON.toJSONString(task2));
                    // 判断进入条件
                    // isT : 保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
                    // isK : 控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
                    if (isJumpDays == 0 || isCanOnlyOnceTimeEmptyInsert == 1) {
                        isCanOnlyOnceTimeEmptyInsert = 0;
                        // 判断当前任务的开始时间大于等于对比任务1的结束时间
                        if (task.getTePStart() >= contrastTaskOne.getTePFinish()) {
                            // 判断当前任务的优先级小于对比任务2的优先级
                            if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                if (task.getTePStart() >= contrastTaskTwo.getTePStart()
                                        && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                    isTaskConflictInfo = 3;
                                    System.out.println("进入-x-冲突-最开始-这里-1-q");
//                                    System.out.println(JSON.toJSONString(onlyRefState));
                                    // 调用处理时间冲突方法
                                    JSONObject timeConflictInfo
                                            = timeZjServiceTimeConflict.handleTimeConflict(task, contrastTaskOne
                                            , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                            , timeConflictCopy, isGetTaskPattern, 0, sho
                                            ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                            ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                            ,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate
                                            ,isComprehensiveHandle,depAllTime);
                                    // 获取任务余剩时间
                                    zon = timeConflictInfo.getLong("zon");
                                    // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isProblemState = timeConflictInfo.getInteger("isProblemState");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    endTime = timeConflictInfo.getLong("endTime");
                                    long hTeStartNew = timeConflictInfo.getLong("tePFinish");
                                    System.out.println("进入-x-冲突-最开始-这里-1-h:旧:"+hTeStart+" - 新:"+hTeStartNew);
                                    hTeStart = hTeStartNew;
//                                    System.out.println(JSON.toJSONString(timeConflictInfo));
                                    break;
                                } else
                                if (contrastTaskTwo.getTePStart() >= task.getTePStart()
                                        && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-2");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最开始-这里-2",recordNoOperation);
                                    isTaskConflictInfo = 4;
                                    break;
                                } else if (task.getTePFinish() > contrastTaskTwo.getTePStart()
                                        && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-3");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最开始-这里-3",recordNoOperation);
                                    isTaskConflictInfo = 5;
                                    break;
                                }
                            } else if (task.getPriority() < contrastTaskOne.getPriority()){
                                // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                if (task.getTePStart() >= contrastTaskTwo.getTePStart() && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-1--1");
                                    isTaskConflictInfo = 33;
                                    // 调用处理时间冲突方法
                                    JSONObject timeConflictInfo = timeZjServiceTimeConflict.handleTimeConflict(task
                                            , contrastTaskOne, contrastTaskTwo, zon, tasks, i, random, grpB
                                            , dep, teDate, timeConflictCopy, isGetTaskPattern, 0
                                            , sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,recordId_OIndexState
                                            ,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                                            ,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                    // 获取任务余剩时间
                                    zon = timeConflictInfo.getLong("zon");
                                    // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isProblemState = timeConflictInfo.getInteger("isProblemState");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    endTime = timeConflictInfo.getLong("endTime");
                                    System.out.println("进入-x-冲突-最开始-这里-1--1-1");
                                    break;
                                } else
                                if (contrastTaskTwo.getTePStart() >= task.getTePStart() && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-2--1");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最开始-这里-2--1",recordNoOperation);
                                    isTaskConflictInfo = 44;
                                    break;
                                } else if (task.getTePFinish() > contrastTaskTwo.getTePStart() && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-3--1");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最开始-这里-3--1",recordNoOperation);
                                    isTaskConflictInfo = 55;
                                    break;
                                }
                            }
//                            System.out.println("弹回---");
                            isCanOnlyOnceTimeEmptyInsert = 1;
                        } else {
//                            System.out.println("进入这里-2-2");
                            // 判断当前任务优先级小于对比任务1的优先级
                            if (task.getPriority() < contrastTaskOne.getPriority()) {
                                // 判断当前任务的开始时间大于等于对比任务信息1的开始时间，并且当前任务的开始时间小于对比任务信息1的结束时间
                                if (task.getTePStart() >= contrastTaskOne.getTePStart() && task.getTePStart() < contrastTaskOne.getTePFinish()) {
                                    isTaskConflictInfo = 3;
                                    System.out.println("进入-x-冲突-中间-这里-1");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-中间-这里-1",recordNoOperation);
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    break;
                                } else
                                if (contrastTaskOne.getTePStart() >= task.getTePStart() && contrastTaskOne.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-中间-这里-2");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-中间-这里-2",recordNoOperation);
                                    isTaskConflictInfo = 4;
                                    break;
                                } else if (task.getTePFinish() > contrastTaskOne.getTePStart() && task.getTePFinish() < contrastTaskOne.getTePFinish()) {
                                    System.out.println("进入-x-冲突-中间-这里-3");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-中间-这里-3",recordNoOperation);
                                    isTaskConflictInfo = 5;
                                    break;
                                }
                            } else if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                if (task.getTePStart() >= contrastTaskTwo.getTePStart() && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                    isTaskConflictInfo = 3;
                                    System.out.println("进入-x-冲突-最后面-这里-1");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最后面-这里-1",recordNoOperation);
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    break;
                                } else
                                if (contrastTaskTwo.getTePStart() >= task.getTePStart() && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                    isTaskConflictInfo = 4;
                                    System.out.println("进入-x-冲突-最后面-这里-2");
                                    // 调用处理时间冲突方法
                                    JSONObject timeConflictInfo = timeZjServiceTimeConflict.handleTimeConflict(task
                                            , contrastTaskOne, contrastTaskTwo, zon, tasks, i, random, grpB
                                            , dep, teDate, timeConflictCopy, isGetTaskPattern, 0
                                            , sho,csSta,randomAll,xbAndSbAll,actionIdO
                                            ,objTaskAll,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                            ,allImageTasks,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                                            ,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                    // 获取任务余剩时间
                                    zon = timeConflictInfo.getLong("zon");
                                    // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isProblemState = timeConflictInfo.getInteger("isProblemState");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    endTime = timeConflictInfo.getLong("endTime");
                                    System.out.println("进入-x-冲突-最后面-这里-2");
                                    break;
                                } else if (task.getTePFinish() > contrastTaskTwo.getTePStart() && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最后面-这里-3");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最后面-这里-3",recordNoOperation);
                                    isTaskConflictInfo = 5;
                                    break;
                                }
                            } else {
                                System.out.println("--x-优先级跳过--");
                            }
                        }
                    } else {
                        // 判断当前任务优先级小于对比任务2的优先级
                        if (task.getPriority() < contrastTaskTwo.getPriority()) {
                            // 判断当前任务开始时间小于对比任务2的开始时间，并且当前任务的开始时间小于对比任务2的结束时间
                            if (task.getTePStart() < contrastTaskTwo.getTePStart() && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                isTaskConflictInfo = 3;
                                System.out.println("进入-x-冲突-新最开始-这里-1");
                                // 调用处理时间冲突方法
                                JSONObject timeConflictInfo = timeZjServiceTimeConflict.handleTimeConflict(task, contrastTaskOne, contrastTaskTwo, zon, tasks, i, random, grpB
                                        , dep, teDate, timeConflictCopy, isGetTaskPattern, 1, sho,csSta,randomAll,xbAndSbAll,actionIdO
                                        ,objTaskAll,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                                        ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                        ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                // 获取任务余剩时间
                                zon = timeConflictInfo.getLong("zon");
                                // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                isProblemState = timeConflictInfo.getInteger("isProblemState");
                                // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                isJumpDay = 2;
                                isSetEnd = timeConflictInfo.getBoolean("isSetEnd");
                                endTime = timeConflictInfo.getLong("endTime");
                                System.out.println("进入-x-冲突-新最开始-这里-1");
                                break;
                            }
                        } else if (task.getPriority() < contrastTaskOne.getPriority()){
                            System.out.println("进入-x-冲突-新最开始-这里-1--1");
                            addRecordGetIntoOperation(randomAll,"进入-x-冲突-新最开始-这里-1--1",recordNoOperation);
                        }
                    }
                }
            }
//            if (isTaskConflictInfo >= 3) {
//                System.out.println("-x-出现时间冲突!isTaskConflictInfo:"+isTaskConflictInfo);
//            }
            // 调用处理[时间处理方法2]结束操作的方法
            JSONObject timeHandleEndInfo = timeZjServiceComprehensive.timeHandleEnd(isJumpDay, isGetTaskPattern
                    , tasks, grpB, dep, random, zon
                    , isCanOnlyOnceTimeEmptyInsert, hTeStart, task, id_O, index, teDate, timeConflictCopy
                    ,sho,isProblemState,isSaveAppearEmpty,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                    ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                    ,thisInfo,allImageTeDate,isSetEnd,endTime,depAllTime);
            hTeStart = timeHandleEndInfo.getLong("hTeStart");
            endTime = timeHandleEndInfo.getLong("endTime");
        }

        result.put("hTeStart",hTeStart);
        result.put("endTime",endTime);
        return result;
    }

    public void getGrpB(String grpB,String dep, Map<String,Asset> assetMap, String id_C, JSONObject depAllTime
            , JSONObject grpUNumAll, JSONObject xbAndSbAll, JSONObject grpBGroupIdOJ, String id_OInside){
        // 创建存储部门字典
        JSONObject depMap = new JSONObject();
//        // 根据组别获取组别信息
//        JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpB);
//        // 获取组别的部门
//        String dep = grpBGroupInfo.getString("dep");
        Asset assetDep;
        if (assetMap.containsKey(dep)) {
            assetDep = assetMap.get(dep);
        } else {
            assetDep = qt.getConfig(id_C,"d-"+dep,"chkin");
            assetMap.put(dep,assetDep);
        }
        JSONObject chkGrpB;
        if (null == assetDep || null == assetDep.getChkin() || null == assetDep.getChkin().getJSONObject("objChkin") || null == assetDep.getChkin().getJSONObject("objChkin").getJSONObject(grpB)) {
            chkGrpB = TaskObj.getChkinJava();
        } else {
            JSONObject chkin = assetDep.getChkin();
            JSONObject objChkin = chkin.getJSONObject("objChkin");
            chkGrpB = objChkin.getJSONObject(grpB);
        }
        JSONArray arrTime = chkGrpB.getJSONArray("arrTime");
        Integer wn0UserCount = chkGrpB.getInteger("wn0UserCount");
        JSONArray objSb = new JSONArray();
        JSONArray objXb = new JSONArray();
        // 用于保存上一个时间段的结束时间
//            long belowTimeData = 0;
//            int priority = 0;
//            long allTime = 0;
        long allTime = getArrTime(arrTime,objSb,objXb);
//            for (int j = 0; j < arrTime.size(); j+=2) {
//                String timeUpper = arrTime.getString(j);
//                String timeBelow = arrTime.getString(j+1);
//                String[] splitUpper = timeUpper.split(":");
//                String[] splitBelow = timeBelow.split(":");
//                int upper = Integer.parseInt(splitUpper[0]);
//                int upperDivide = Integer.parseInt(splitUpper[1]);
//                int below = Integer.parseInt(splitBelow[0]);
//                int belowDivide = Integer.parseInt(splitBelow[1]);
//                long upperTime = (((long) upper * 60) * 60)+((long) upperDivide * 60);
//                long belowTime = (((long) below * 60) * 60)+((long) belowDivide * 60);
//                JSONObject objSbZ = new JSONObject();
//                objSbZ.put("priority",priority);
//                priority++;
//                objSbZ.put("tePStart",upperTime);
//                objSbZ.put("tePFinish",belowTime);
//                objSbZ.put("zon",belowTime-upperTime);
//                allTime+=(belowTime-upperTime);
//                objSb.add(objSbZ);
//                if (j == 0) {
//                    if (upperTime != 0) {
//                        JSONObject objXbZ = new JSONObject();
//                        objXbZ.put("priority",-1);
//                        objXbZ.put("tePStart",0);
//                        objXbZ.put("tePFinish",upperTime);
//                        objXbZ.put("zon",upperTime);
//                        objXb.add(objXbZ);
//                    }
//                } else if ((j + 1) + 1 >= arrTime.size()) {
//                    JSONObject objXbZ = new JSONObject();
//                    objXbZ.put("priority",-1);
//                    objXbZ.put("tePStart",belowTimeData);
//                    objXbZ.put("tePFinish",upperTime);
//                    objXbZ.put("zon",upperTime-belowTimeData);
//                    objXb.add(objXbZ);
//                    if (belowTime != 86400) {
//                        objXbZ = new JSONObject();
//                        objXbZ.put("priority",-1);
//                        objXbZ.put("tePStart",belowTime);
//                        objXbZ.put("tePFinish",86400);
//                        objXbZ.put("zon",86400-belowTime);
//                        objXb.add(objXbZ);
//                    }
//                } else {
//                    JSONObject objXbZ = new JSONObject();
//                    objXbZ.put("priority",-1);
//                    objXbZ.put("tePStart",belowTimeData);
//                    objXbZ.put("tePFinish",upperTime);
//                    objXbZ.put("zon",upperTime-belowTimeData);
//                    objXb.add(objXbZ);
//                }
//                belowTimeData = belowTime;
//            }
        if (!depAllTime.containsKey(dep)) {
            depAllTime.put(dep,allTime);
        }
        // 创建时间处理打卡信息存储
        JSONObject xbAndSb = new JSONObject();
        // 添加信息
        xbAndSb.put("xb",objXb);
        xbAndSb.put("sb",objSb);

        // 根据部门，获取部门对应的全局职位人数信息
        JSONObject depAllInfo = grpUNumAll.getJSONObject(dep);
        // 判断部门全局职位人数信息为空
        if (null == depAllInfo) {
            // 创建部门全局职位人数信息
            depAllInfo = new JSONObject();
            // 根据组别添加职位人数
            depAllInfo.put(grpB,wn0UserCount);
            grpUNumAll.put(dep,depAllInfo);
        } else {
            // 直接根据组别获取全局职位人数
            Integer grpBAllInfo = depAllInfo.getInteger(grpB);
            // 判断为空
            if (null == grpBAllInfo) {
                // 添加全局职位人数信息
                depAllInfo.put(grpB,wn0UserCount);
                grpUNumAll.put(dep,depAllInfo);
            }
        }

        // 根据部门获取全局上班下班信息
        JSONObject depAllChKin = xbAndSbAll.getJSONObject(dep);
        // 判断为空
        if (null == depAllChKin) {
            // 创建
            depAllChKin = new JSONObject();
            // 添加全局上班下班信息
            depAllChKin.put(grpB, xbAndSb);
            xbAndSbAll.put(dep,depAllChKin);
        } else {
            // 根据组别获取全局上班下班信息
            JSONObject grpBAllChKin = depAllChKin.getJSONObject(grpB);
            if (null == grpBAllChKin) {
                // 添加全局上班下班信息
                depAllChKin.put(grpB, xbAndSb);
                xbAndSbAll.put(dep,depAllChKin);
            }
        }
        // 添加部门信息
        depMap.put("dep",dep);
        depMap.put("id_O",id_OInside);
        // 添加信息
        grpBGroupIdOJ.put(grpB,depMap);
    }
//    public void getGrpB(JSONObject grpBGroup, Map<String,Asset> assetMap, String id_C, JSONObject depAllTime
//            , JSONObject grpUNumAll, JSONObject xbAndSbAll, JSONObject grpBGroupIdOJ, String id_OInside){
//        // 遍历组别对应部门信息
//        for (String grpB : grpBGroup.keySet()) {
//            // 创建存储部门字典
//            JSONObject depMap = new JSONObject();
//            // 根据组别获取组别信息
//            JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpB);
//            // 获取组别的部门
//            String dep = grpBGroupInfo.getString("dep");
//            Asset assetDep;
//            if (assetMap.containsKey(dep)) {
//                assetDep = assetMap.get(dep);
//            } else {
//                assetDep = qt.getConfig(id_C,"d-"+dep,"chkin");
//                assetMap.put(dep,assetDep);
//            }
//            JSONObject chkGrpB;
//            if (null == assetDep || null == assetDep.getChkin() || null == assetDep.getChkin().getJSONObject("objChkin")) {
//                chkGrpB = TaskObj.getChkinJava();
//            } else {
//                JSONObject chkin = assetDep.getChkin();
//                JSONObject objChkin = chkin.getJSONObject("objChkin");
//                chkGrpB = objChkin.getJSONObject(grpB);
//            }
//            JSONArray arrTime = chkGrpB.getJSONArray("arrTime");
//            Integer wn0UserCount = chkGrpB.getInteger("wn0UserCount");
//            JSONArray objSb = new JSONArray();
//            JSONArray objXb = new JSONArray();
//            // 用于保存上一个时间段的结束时间
////            long belowTimeData = 0;
////            int priority = 0;
////            long allTime = 0;
//            long allTime = getArrTime(arrTime,objSb,objXb);
////            for (int j = 0; j < arrTime.size(); j+=2) {
////                String timeUpper = arrTime.getString(j);
////                String timeBelow = arrTime.getString(j+1);
////                String[] splitUpper = timeUpper.split(":");
////                String[] splitBelow = timeBelow.split(":");
////                int upper = Integer.parseInt(splitUpper[0]);
////                int upperDivide = Integer.parseInt(splitUpper[1]);
////                int below = Integer.parseInt(splitBelow[0]);
////                int belowDivide = Integer.parseInt(splitBelow[1]);
////                long upperTime = (((long) upper * 60) * 60)+((long) upperDivide * 60);
////                long belowTime = (((long) below * 60) * 60)+((long) belowDivide * 60);
////                JSONObject objSbZ = new JSONObject();
////                objSbZ.put("priority",priority);
////                priority++;
////                objSbZ.put("tePStart",upperTime);
////                objSbZ.put("tePFinish",belowTime);
////                objSbZ.put("zon",belowTime-upperTime);
////                allTime+=(belowTime-upperTime);
////                objSb.add(objSbZ);
////                if (j == 0) {
////                    if (upperTime != 0) {
////                        JSONObject objXbZ = new JSONObject();
////                        objXbZ.put("priority",-1);
////                        objXbZ.put("tePStart",0);
////                        objXbZ.put("tePFinish",upperTime);
////                        objXbZ.put("zon",upperTime);
////                        objXb.add(objXbZ);
////                    }
////                } else if ((j + 1) + 1 >= arrTime.size()) {
////                    JSONObject objXbZ = new JSONObject();
////                    objXbZ.put("priority",-1);
////                    objXbZ.put("tePStart",belowTimeData);
////                    objXbZ.put("tePFinish",upperTime);
////                    objXbZ.put("zon",upperTime-belowTimeData);
////                    objXb.add(objXbZ);
////                    if (belowTime != 86400) {
////                        objXbZ = new JSONObject();
////                        objXbZ.put("priority",-1);
////                        objXbZ.put("tePStart",belowTime);
////                        objXbZ.put("tePFinish",86400);
////                        objXbZ.put("zon",86400-belowTime);
////                        objXb.add(objXbZ);
////                    }
////                } else {
////                    JSONObject objXbZ = new JSONObject();
////                    objXbZ.put("priority",-1);
////                    objXbZ.put("tePStart",belowTimeData);
////                    objXbZ.put("tePFinish",upperTime);
////                    objXbZ.put("zon",upperTime-belowTimeData);
////                    objXb.add(objXbZ);
////                }
////                belowTimeData = belowTime;
////            }
//            if (!depAllTime.containsKey(dep)) {
//                depAllTime.put(dep,allTime);
//            }
//            // 创建时间处理打卡信息存储
//            JSONObject xbAndSb = new JSONObject();
//            // 添加信息
//            xbAndSb.put("xb",objXb);
//            xbAndSb.put("sb",objSb);
//
//            // 根据部门，获取部门对应的全局职位人数信息
//            JSONObject depAllInfo = grpUNumAll.getJSONObject(dep);
//            // 判断部门全局职位人数信息为空
//            if (null == depAllInfo) {
//                // 创建部门全局职位人数信息
//                depAllInfo = new JSONObject();
//                // 根据组别添加职位人数
//                depAllInfo.put(grpB,wn0UserCount);
//                grpUNumAll.put(dep,depAllInfo);
//            } else {
//                // 直接根据组别获取全局职位人数
//                Integer grpBAllInfo = depAllInfo.getInteger(grpB);
//                // 判断为空
//                if (null == grpBAllInfo) {
//                    // 添加全局职位人数信息
//                    depAllInfo.put(grpB,wn0UserCount);
//                    grpUNumAll.put(dep,depAllInfo);
//                }
//            }
//
//            // 根据部门获取全局上班下班信息
//            JSONObject depAllChKin = xbAndSbAll.getJSONObject(dep);
//            // 判断为空
//            if (null == depAllChKin) {
//                // 创建
//                depAllChKin = new JSONObject();
//                // 添加全局上班下班信息
//                depAllChKin.put(grpB, xbAndSb);
//                xbAndSbAll.put(dep,depAllChKin);
//            } else {
//                // 根据组别获取全局上班下班信息
//                JSONObject grpBAllChKin = depAllChKin.getJSONObject(grpB);
//                if (null == grpBAllChKin) {
//                    // 添加全局上班下班信息
//                    depAllChKin.put(grpB, xbAndSb);
//                    xbAndSbAll.put(dep,depAllChKin);
//                }
//            }
//            // 添加部门信息
//            depMap.put("dep",dep);
//            depMap.put("id_O",id_OInside);
//            // 添加信息
//            grpBGroupIdOJ.put(grpB,depMap);
//        }
//    }

    /**
     * 获取上班时间戳与休息时间戳
     * @param arrTime   上班时间段
     * @param objSb 存储上班信息
     * @param objXb 存储休息信息
     * @return  上班时间戳与休息时间戳
     */
    public long getArrTime(JSONArray arrTime,JSONArray objSb,JSONArray objXb){
        long belowTimeData = 0;
        int priority = 0;
        long allTime = 0;
        for (int j = 0; j < arrTime.size(); j+=2) {
            String timeUpper = arrTime.getString(j);
            String timeBelow = arrTime.getString(j+1);
            String[] splitUpper = timeUpper.split(":");
            String[] splitBelow = timeBelow.split(":");
            int upper = Integer.parseInt(splitUpper[0]);
            int upperDivide = Integer.parseInt(splitUpper[1]);
            int below = Integer.parseInt(splitBelow[0]);
            int belowDivide = Integer.parseInt(splitBelow[1]);
            long upperTime = (((long) upper * 60) * 60)+((long) upperDivide * 60);
            long belowTime = (((long) below * 60) * 60)+((long) belowDivide * 60);
            JSONObject objSbZ = new JSONObject();
            objSbZ.put("priority",priority);
            priority++;
            objSbZ.put("tePStart",upperTime);
            objSbZ.put("tePFinish",belowTime);
            objSbZ.put("zon",belowTime-upperTime);
            allTime+=(belowTime-upperTime);
            objSb.add(objSbZ);
            if (j == 0) {
                if (upperTime != 0) {
                    JSONObject objXbZ = new JSONObject();
                    objXbZ.put("priority",-1);
                    objXbZ.put("tePStart",0);
                    objXbZ.put("tePFinish",upperTime);
                    objXbZ.put("zon",upperTime);
                    objXb.add(objXbZ);
                }
            } else if ((j + 1) + 1 >= arrTime.size()) {
                JSONObject objXbZ = new JSONObject();
                objXbZ.put("priority",-1);
                objXbZ.put("tePStart",belowTimeData);
                objXbZ.put("tePFinish",upperTime);
                objXbZ.put("zon",upperTime-belowTimeData);
                objXb.add(objXbZ);
                if (belowTime != 86400) {
                    objXbZ = new JSONObject();
                    objXbZ.put("priority",-1);
                    objXbZ.put("tePStart",belowTime);
                    objXbZ.put("tePFinish",86400);
                    objXbZ.put("zon",86400-belowTime);
                    objXb.add(objXbZ);
                }
            } else {
                JSONObject objXbZ = new JSONObject();
                objXbZ.put("priority",-1);
                objXbZ.put("tePStart",belowTimeData);
                objXbZ.put("tePFinish",upperTime);
                objXbZ.put("zon",upperTime-belowTimeData);
                objXb.add(objXbZ);
            }
            belowTimeData = belowTime;
        }
        return allTime;
    }

    /**
     * 获取多订单一起计算物料时间后的开始时间
     * @param id_OArr  订单编号集合
     * @param teStart   开始时间
     * @return  开始时间
     */
    @Override
    public ApiResponse getTeStartTotal(JSONArray id_OArr, Long teStart) {
        long maxTime = 0;
        for (int o = 0; o < id_OArr.size(); o++) {
            String id_O = id_OArr.getString(o);
            long maxTimeNew = teStart(id_O);
            if (maxTimeNew > maxTime) {
                maxTime = maxTimeNew;
            }
//            Order order = qt.getMDContent(id_O, "casItemx", Order.class);
//            if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
//            }
//            JSONObject java = order.getCasItemx().getJSONObject("java");
//            JSONArray oDates = java.getJSONArray("oDates");
//            Map<String, HashSet<String>> compMap = new HashMap<>();
//            for (int i = 0; i < oDates.size(); i++) {
//                JSONObject oDate = oDates.getJSONObject(i);
//                int bmdpt = oDate.getInteger("bmdpt");
//                if (bmdpt == 3) {
//                    String id_P = oDate.getString("id_P");
//                    String id_C = oDate.getString("id_C");
//                    HashSet<String> prodSet;
//                    if (compMap.containsKey(id_C)) {
//                        prodSet = compMap.get(id_C);
//                    } else {
//                        prodSet = new HashSet<>();
//                    }
//                    prodSet.add(id_P);
//                    compMap.put(id_C,prodSet);
//                } else {
//                    Long wntDur = oDate.getLong("wntDur");
//                    if (bmdpt != 2 && wntDur <= 0) {
//                        // 返回为空错误信息
//                        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_TIME_NULL.getCode(), "时间为空");
//                    }
//                }
//            }
//            Map<String,Prod> prodMap = new HashMap<>();
//            for (String id_C : compMap.keySet()) {
//                HashSet<String> prodArr = compMap.get(id_C);
//                for (String id_P : prodArr) {
//                    Prod prod;
//                    if (prodMap.containsKey(id_P)) {
//                        prod = prodMap.get(id_P);
//                    } else {
//                        prod = qt.getMDContent(id_P, "qtySafex", Prod.class);
//                        if (null != prod && null != prod.getQtySafex() && null != prod.getQtySafex().getDouble(id_C)) {
//                            prodMap.put(id_P,prod);
//                        } else {
//                            prod = null;
//                        }
//                    }
//                    if (null != prod) {
//                        double hour = prod.getQtySafex().getDouble(id_C);
//                        long time = (long) ((hour*60)*60);
//                        if (time > maxTime) {
//                            maxTime = time;
//                        }
//                    }
//                }
//            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), teStart+maxTime);
    }

    /**
     * 获取多订单分开计算物料时间后的开始时间
     * @param orderInfo  订单信息集合
     * @return  开始时间
     */
    @Override
    public ApiResponse getTeStartList(JSONArray orderInfo) {
        JSONArray resultArr = new JSONArray();
        for (int o = 0; o < orderInfo.size(); o++) {
            JSONObject info = orderInfo.getJSONObject(o);
            resultArr.add(info.getLong("teStart")+teStart(info.getString("id_O")));
        }
        return retResult.ok(CodeEnum.OK.getCode(), resultArr);
    }

    /**
     * 获取订单的最长物料时间
     * @param id_O  订单编号
     * @return  最长物料时间
     */
    public long teStart(String id_O){
        long maxTime = 0;
        Order order = qt.getMDContent(id_O, "casItemx", Order.class);
        if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONObject java = order.getCasItemx().getJSONObject("java");
        JSONArray oDates = java.getJSONArray("oDates");
        Map<String, HashSet<String>> compMap = new HashMap<>();
        for (int i = 0; i < oDates.size(); i++) {
            JSONObject oDate = oDates.getJSONObject(i);
            int bmdpt = oDate.getInteger("bmdpt");
            if (bmdpt == 3) {
                String id_P = oDate.getString("id_P");
                String id_C = oDate.getString("id_C");
                HashSet<String> prodSet;
                if (compMap.containsKey(id_C)) {
                    prodSet = compMap.get(id_C);
                } else {
                    prodSet = new HashSet<>();
                }
                prodSet.add(id_P);
                compMap.put(id_C,prodSet);
            } else {
                Long wntDur = oDate.getLong("wntDur");
                if (bmdpt != 2 && wntDur <= 0) {
                    // 返回为空错误信息
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_TIME_NULL.getCode(), "时间为空");
                }
            }
        }
        Map<String,Prod> prodMap = new HashMap<>();
        for (String id_C : compMap.keySet()) {
            HashSet<String> prodArr = compMap.get(id_C);
            for (String id_P : prodArr) {
                Prod prod;
                if (prodMap.containsKey(id_P)) {
                    prod = prodMap.get(id_P);
                } else {
                    prod = qt.getMDContent(id_P, "qtySafex", Prod.class);
                    if (null != prod && null != prod.getQtySafex() && null != prod.getQtySafex().getDouble(id_C)) {
                        prodMap.put(id_P,prod);
                    } else {
                        prod = null;
                    }
                }
                if (null != prod) {
                    double hour = prod.getQtySafex().getDouble(id_C);
                    long time = (long) ((hour*60)*60);
                    if (time > maxTime) {
                        maxTime = time;
                    }
                }
            }
        }
        return maxTime;
    }

    @Override
    public ApiResponse updateODate() {
        JSONArray ids = qt.setArray("65a8d523ec65d36e9095034b","65b338bcec65d36e909503bb","65b33b86ec65d36e909503bc");
        List<Order> orders = qt.getMDContentFast(ids, qt.strList("casItemx"), Order.class);
        List<JSONObject> list = new ArrayList<>();
        for (Order order : orders) {
            JSONObject java = order.getCasItemx().getJSONObject("java");
            JSONArray oDates = java.getJSONArray("oDates");
//            JSONArray jsonArray = java.getJSONArray("");
            for (int i = 0; i < oDates.size(); i++) {
                JSONObject date = oDates.getJSONObject(i);
                date.put("wntDur",date.getLong("teDur"));
                date.put("wntDurTotal",date.getLong("teDurTotal"));
                date.put("wntPrep",date.getLong("tePrep"));
//                date.remove("teDur");
//                date.remove("teDurTotal");
//                date.remove("tePrep");
                oDates.set(i,date);
            }
//            qt.setMDContent(order.getId(),qt.setJson("casItemx.java.oDates",oDates), Order.class);
            list.add(qt.setJson("id",order.getId(),"updateData",qt.setJson("casItemx.java.oDates",oDates)));
        }
        qt.setMDContentFast(list, Order.class);
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    @Override
    public ApiResponse delExcessiveODateField() {
        JSONArray ids = qt.setArray("65a8d523ec65d36e9095034b","65b338bcec65d36e909503bb","65b33b86ec65d36e909503bc");
        List<Order> orders = qt.getMDContentFast(ids, qt.strList("casItemx"), Order.class);
        List<JSONObject> list = new ArrayList<>();
        for (Order order : orders) {
            JSONObject java = order.getCasItemx().getJSONObject("java");
            JSONArray oDates = java.getJSONArray("oDates");
            JSONArray oTasks = java.getJSONArray("oTasks");
            for (int i = 0; i < oDates.size(); i++) {
//                JSONObject date = oDates.getJSONObject(i);
//                date.remove("teDur");
//                date.remove("tePrep");
//                date.remove("teDurTotal");
//                date.remove("teStart");
//                date.remove("taStart");
//                date.remove("teFin");
//                date.remove("taFin");
//                oDates.set(i,date);

                JSONObject date = oDates.getJSONObject(i);
                JSONObject task = oTasks.getJSONObject(i);
                date.put("wrdN",task.getJSONObject("wrdN"));
                oDates.set(i,date);
            }
            list.add(qt.setJson("id",order.getId(),"updateData",qt.setJson("casItemx.java.oDates",oDates)));
        }
        qt.setMDContentFast(list, Order.class);
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    @Override
    public ApiResponse getClearOldTask(String id_O, int dateIndex, String id_C) {
        clearOldTaskNew(id_O,dateIndex,id_C);
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }
}
