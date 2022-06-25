package com.cresign.chat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.common.ChatEnum;
import com.cresign.chat.config.websocket.WebSocketUserServer;
import com.cresign.chat.service.ActionService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.RedisUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.logger.LogUtil;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ActionServiceImpl implements ActionService {

    /**
     * getActionData **** 这个很重要，拿了action / oItem 的内容都要转换
     * changeActionStatus -> updateNext -> updateParent
     *
     * 这几个没有改完 dgActivate / switchTask2Prod / genFlowControlOrder
     *
     * 这些是OK 的
     * createQuest / createTask / dgConfirmOrder
     * getFlowList / changeDepAndFlow / dgActivateAll
     * sendLogWSU 用 logUtil 和 wsu
     */



        
        @Autowired
        private LogUtil logUtil;

        @Autowired
        private RetResult retResult;

        @Autowired
        private CoupaUtil coupaUtil;

        @Autowired
        private RedisUtils redisUtils;

        @Autowired
        private DbUtils dbUtils;


    /**
     * 根据oId修改grpBGroup字段
     * ##param id_O	   订单编号
     * ##param grpBGroup	旧grpB分组信息
     * ##param grpBGroupX 新的grpB分组信息
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @date 2021/9/10 17:32
     */
    @Override
    public ApiResponse changeDepAndFlow(String id_O, String grpB, JSONObject grpBOld,JSONObject grpBNew
            ,String id_C,String id_U,String grpU, JSONObject wrdNU) {

        //1. Save the new
        JSONObject mapKey = new JSONObject();
        mapKey.put("action.grpBGroup."+grpB,grpBNew);
        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
        //2. get Order's oItem+action
        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info","oItem", "action"));
        JSONArray objItem = order.getOItem().getJSONArray("objItem");
        JSONArray objAction = order.getAction().getJSONArray("objAction");
        //3. Loop check grpB==oItem(i).grpB and objAction(i).isPUshed == 1
        for (int i = 0; i < objItem.size(); i++) {
            if (grpB.equals(objItem.getJSONObject(i).getString("grpB")) &&
            objAction.getJSONObject(i).getInteger("bisPush") == 1 &&
            objAction.getJSONObject(i).getInteger("bcdStatus") != 2){

                //4. Send Log to stop old Flow
                LogFlow logStop = new LogFlow(grpBOld.getString("logType"),grpBOld.getString("id_FC"),"","stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),grpBOld.getString("grpB"), "",
                        objItem.getJSONObject(i).getString("id_O"),
                        i,id_C,order.getInfo().getId_C(),objItem.getJSONObject(i).getString("pic"),grpBOld.getString("dep"),
                        "转换另一队伍执行",3,objItem.getJSONObject(i).getJSONObject("wrdN"), wrdNU);
                logStop.setActionData(objAction.getJSONObject(i).getInteger("bisactivate"),9,
                        objAction.getJSONObject(i).getJSONArray("id_Us"), objAction.getJSONObject(i).getDouble("priority"),
                        objItem.getJSONObject(i).getString("id_P"),
                        objItem.getJSONObject(i).getString("id_O"),i,1,objAction.getJSONObject(i).getJSONObject("wrdNP"),
                        objItem.getJSONObject(i).getJSONObject("wrdN"));

                LogFlow logStart = new LogFlow(grpBOld.getString("logType"),grpBNew.getString("id_FC"),"","stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),grpBNew.getString("grpB"), "",
                        objItem.getJSONObject(i).getString("id_O"),
                        i,id_C,order.getInfo().getId_C(),objItem.getJSONObject(i).getString("pic"),grpBNew.getString("dep"),
                        "转换队伍，开始执行",3,objItem.getJSONObject(i).getJSONObject("wrdN"), wrdNU);
                logStart.setActionData(objAction.getJSONObject(i).getInteger("bisactivate"),1,
                        objAction.getJSONObject(i).getJSONArray("id_Us"), objAction.getJSONObject(i).getDouble("priority"),
                        objItem.getJSONObject(i).getString("id_P"),
                        objItem.getJSONObject(i).getString("id_O"),i,1,objAction.getJSONObject(i).getJSONObject("wrdNP"),
                        objItem.getJSONObject(i).getJSONObject("wrdN"));

                this.sendLogWSU(logStop);
                this.sendLogWSU(logStart);
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "换群成功");

    }


        /**
         * 操作开始，暂停，恢复功能 - 注释完成
         * ##param id_C	当前公司id
         * ##param id_U	用户id
         * @return java.lang.String  返回结果: 结果
         * @author tang
         * @version 1.0.0
         * @date 2020/8/6 9:22
         */

        // make sure the original bcdStatus is correct to set to next Status
        // add Cancel status, if prob = 0, mine =9, next = 0
        // StartNext = do not change this, just push next status = 0
        // start noP oItem -> +OItem +action +log @ bcdStatus = 0 / all other oItem has 100@Status (notYetPush)
        // every oItem can push, what id_FC?
        //
        // start noP questOItem-> +OItem +action +log @ bcdStatus = 0 +setProbCard ? or set Prob[] @ action card
        // finish need all Prob = null
        // finish noP oItem = same logic as regular,
        // finish questOItem = same logic + prob - 1 set prob Status
        // cancel/StartNext @ noP next Status = 0, -1 all next, unchange my status or to 9
        // See what happen to noP effects, has P/O id can tap and modal open
        // with that cancel, you can use it in dgRemove
        // grpGroup = 


            @Override
            @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
            public ApiResponse changeActionStatus(String logType, Integer status, String msg,
                                                  Integer index, String id_O,
                                                  String id_FC, String id_FS, String id_C, String id_U,
                                                  String grpU, String dep, JSONObject wrdNU) {

                JSONObject actData = this.getActionData(id_O, index);

                OrderOItem orderOItem = JSONObject.parseObject(JSON.toJSONString(actData.get("orderOItem")),OrderOItem.class);
                OrderAction orderAction = JSONObject.parseObject(JSON.toJSONString(actData.get("orderAction")),OrderAction.class);

                // 根据下标获取递归信息

            System.out.println("bcdStatus"+orderOItem.getWrdN());

            // 判断新的状态和旧状态不一样
            if (orderAction.getBcdStatus().equals(status)){
                // 抛出操作成功异常
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "该操作已被处理");
            }

            // 备注
            String message = "";
//            String result = "";
            JSONObject res = new JSONObject();
            JSONObject mapKey = new JSONObject();

            // 判断属于什么操作
            switch (status){
                case 1:
                    // Start an OItem, DG just start, Task just start, Quest start + update Prob
                    if (orderAction.getBcdStatus() != 0)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");
                    }
                    // 设置备注信息
                    // can start now, send a msg for status into 1, and then can start doing other buttons, very simple
                    message = "[开始运行]"+ msg;
                    JSONArray id_Us = orderAction.getId_Us() == null? new JSONArray() : orderAction.getId_Us();

                    //Adding myself to the id_Us of action to indicate
                        id_Us.add(id_U);
                        orderAction.setId_Us(id_Us);
                    res.put("isJoin", 1);
                    res.put("id_Us", orderAction.getId_Us());
                    break;
                case 2:                    // 2 = finish, set qtyfin setNextDesc
                    if (orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3
                            && orderAction.getBcdStatus() != 7)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经处理了");
                    }
                    if (orderAction.getSumChild() > 0 && orderAction.getBmdpt() == 2)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "还有子工序没完成，不能完成");
                    }

                    Double progress = Double.valueOf((actData.getInteger("progress") + 1) / actData.getJSONArray("actionArray").size() * 100);
                    //update actions' total progress
                    mapKey.put("action.wn2progress", progress );

                    message = "[已完成]" + msg;
                    break;
                case 3: // resume OItem
                    if (orderAction.getBcdStatus() != 8)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");
                    }
                    message = "[已恢复执行]"  + msg;
                    break;
                case 5:
                    if (orderAction.getId_Us() == null) {
                        orderAction.setId_Us(new JSONArray());
                    }
                    orderAction.getId_Us().add(id_U);
                    message = "[加入成功]"  + msg;
//                    result = "[加入成功]"  + msg;
                    status = orderAction.getBcdStatus();
                    res.put("isJoin", 1);
                    res.put("id_Us", orderAction.getId_Us());
                    break;
                case 7:
                    if ((orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3)
                            || orderAction.getBisactivate() == 4)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能操作");
                    }
                    orderAction.setBisactivate(4);
                    message = "[继续下一个]"+msg;
                    break;
                case 8: // pause
                    if (orderAction.getBcdStatus() != 1
                            && orderAction.getBcdStatus() != 3)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");
                    }                    // 设置备注信息
                    message = "[已暂停]" + msg;
                    break;
                case 9: // cancel
                    // pause but it's nothing special
                    // 设置备注信息
                    message = "[已取消]" + msg;
                    break;
                default:
                    message = "[无法操作]";
                    break;
            }

            // 设置产品状态
            orderAction.setBcdStatus(status);

            mapKey.put("action.objAction."+index,orderAction);
            coupaUtil.updateOrderByListKeyVal(id_O,mapKey);

            if (logType.endsWith("SL"))
            {
                id_C = actData.getJSONObject("info").getString("id_CB");
            }

            // Start making log with data
            LogFlow logL = new LogFlow(logType,id_FC,
                    id_FS,"stateChg", id_U,grpU,orderOItem.getId_P(),orderOItem.getGrpB(),orderOItem.getGrp(),
                    id_O,index,id_C,orderOItem.getId_C(), "",dep,message,3,orderOItem.getWrdN(),wrdNU);
            logL.setLogData_action(orderAction,orderOItem);

            this.sendLogWSU(logL);

            // if Quest, send log + update OItem of myself = task = DG = above
            // get upPrnt data, and find the prob, set that status of Prob to status

            if (orderAction.getBisactivate() == 7)
            {
                JSONObject upPrnt = orderAction.getUpPrnts().getJSONObject(0);

                try {
                    // if any null exception, catch
                    JSONObject taskOwner = this.getActionData(upPrnt.getString("id_O"),upPrnt.getInteger("index"));
                    OrderAction objAction = JSONObject.parseObject(JSON.toJSONString(taskOwner.get("orderAction")),OrderAction.class);

                    JSONObject probData = objAction.getProb().getJSONObject(upPrnt.getInteger("probIndex"));
                    probData.put("bcdStatus", status);

                    JSONObject probKey = new JSONObject();
                    probKey.put("action.objAction."+upPrnt.getInteger("index"),objAction);
                    coupaUtil.updateOrderByListKeyVal(upPrnt.getString("id_O"),probKey);
                } catch (RuntimeException e) {
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_AN_ERROR_OCCURRED.getCode(), "不能开始,"+e);
                }
            } else if ((status == 2 && orderAction.getBisactivate() != 4) || status == 7) {
                // activate = 4 means Skip = already pushed Next

//                Boolean childrenAllDone =
                        this.updateParent(orderAction, id_C, id_U,grpU,dep,wrdNU, logType);
//                // 判断父产品不为空
//                if (!childrenAllDone) {
//                    this.updateNext(orderAction, id_C, id_U,grpU, dep, wrdNU, logType);
//                }
            }
            // 抛出操作成功异常
            return retResult.ok(CodeEnum.OK.getCode(), res);
        }

    private void updateNext(OrderAction orderAction, String id_C, String id_U, String grpU, String dep, JSONObject wrdNU, String logType)
    {
        for (Integer i = 0; i < orderAction.getPrtNext().size(); i++ )
        {
            // 获取下一个产品的id + index
            String nextId = orderAction.getPrtNext().getJSONObject(i).getString("id_O");
            Integer nextIndex = orderAction.getPrtNext().getJSONObject(i).getInteger("index");

                JSONObject actData = this.getActionData(nextId, nextIndex);
                if (null != actData) {

                    OrderOItem orderOItem1 = JSONObject.parseObject(JSON.toJSONString(actData.get("orderOItem")),OrderOItem.class);
                    OrderAction orderAction1 = JSONObject.parseObject(JSON.toJSONString(actData.get("orderAction")),OrderAction.class);

                    if (orderAction1.getBcdStatus() == 100) {
                        // 设置该产品的上一个数量减一
                        orderAction1.setSumPrev(orderAction1.getSumPrev() - 1);

                        // 判断下一个产品子产品是否为0, // both sumXXX are 0, send log
                        if (orderAction1.getSumPrev() <= 0) {

                            if (orderAction1.getBmdpt() != 4 && orderAction1.getSumChild() == 0) {
                                orderAction1.setBcdStatus(0); //状态改为准备开始
                                orderAction1.setBisPush(1);

                                // Start making log with data
                                LogFlow logL = new LogFlow(logType, actData.getString("id_FC"),
                                        actData.getString("id_FS"), "stateChg",
                                        id_U, grpU, orderOItem1.getId_P(),orderOItem1.getGrpB(), orderOItem1.getGrp(),  nextId, nextIndex, id_C, orderOItem1.getId_C(),
                                        "", dep, orderOItem1.getWrdN().get("cn") + "准备开始", 3, orderOItem1.getWrdN(), wrdNU);
                                logL.setLogData_action(orderAction1, orderOItem1);

                                // 调用发送日志方法
                                this.sendLogWSU(logL);
                                // 把修改好的信息设置回去

//                                JSONObject mapKey = new JSONObject();
//                                mapKey.put("action.objAction." + nextIndex, orderAction1);
//                                coupaUtil.updateOrderByListKeyVal(nextId, mapKey);


                            } else if (orderAction1.getBmdpt() == 4) {
                                // bmdpt == 4 start subParts
                                for (int k = 0; k < orderAction1.getSubParts().size(); k++) {
                                    JSONObject subOrderData = this.getActionData(orderAction1.getSubParts().getJSONObject(k).getString("id_O"),
                                            orderAction1.getSubParts().getJSONObject(k).getInteger("index"));

                                    OrderOItem subOItem = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderOItem")), OrderOItem.class);
                                    OrderAction subAction = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderAction")), OrderAction.class);

                                    if (subAction.getPriority() == 0) {
                                        subAction.setBcdStatus(0);
                                        subAction.setBisPush(1);

                                        JSONObject mapKey = new JSONObject();
                                        mapKey.put("action.objAction." + subAction.getSubParts().getJSONObject(k).getInteger("index"), orderAction);
                                        coupaUtil.updateOrderByListKeyVal(subAction.getSubParts().getJSONObject(k).getString("id_O"), mapKey);

                                        LogFlow logLP = new LogFlow(logType, subOrderData.getString("id_FC"),
                                                subOrderData.getString("id_FS"), "stateChg",
                                                id_U, grpU, subOItem.getId_P(),subOItem.getGrpB(),subOItem.getGrp(),
                                                subAction.getSubParts().getJSONObject(k).getString("id_O"),
                                                subAction.getSubParts().getJSONObject(k).getInteger("index"),
                                                id_C, subOItem.getId_C(),
                                                "", dep, "准备开始", 3, subOItem.getWrdN(), wrdNU);
                                        logLP.setLogData_action(subAction, subOItem);

                                        this.sendLogWSU(logLP);
                                    }
                                }
                            }
                        }
                        JSONObject mapKey = new JSONObject();
                        mapKey.put("action.objAction." + nextIndex, orderAction1);
                        coupaUtil.updateOrderByListKeyVal(nextId, mapKey);
                    }
                }
            }
        }


        /**
         * 操作父产品 - 注释完成
         * ##param orderAction	子产品递归信息
         * ##param id_C	公司id
         * ##param id_U	用户id
         * @return java.lang.String  返回结果: 结果
         * @author tang
         * @version 1.0.0
         * @date 2020/8/6 9:21
         */
        private void updateParent(OrderAction orderAction,String id_C,String id_U, String grpU, String dep, JSONObject wrdNU,String logType) {

            for (Integer i = 0; i < orderAction.getUpPrnts().size(); i++) {

                Integer indexPrnt = orderAction.getUpPrnts().getJSONObject(i).getInteger("index");
                String idPrnt = orderAction.getUpPrnts().getJSONObject(i).getString("id_O");
                JSONObject actData = this.getActionData(idPrnt, indexPrnt);
                if (null != actData) {

                    OrderOItem unitOItemPrnt = JSONObject.parseObject(JSON.toJSONString(actData.get("orderOItem")),OrderOItem.class);
                    OrderAction unitActionPrnt = JSONObject.parseObject(JSON.toJSONString(actData.get("orderAction")),OrderAction.class);

                    // **** 把父的子产品数量减1，带表当前产品已完成
                    unitActionPrnt.setSumChild(unitActionPrnt.getSumChild() - 1);


                    // 判断父的子产品是否为0，如果为0则把父产品推送得到WebSocket
                    if (unitActionPrnt.getSumChild().equals(unitActionPrnt.getSubParts().size() - 1)) {
//                        if (unitActionPrnt.getSumChild() == 0)
//                        {

                        unitActionPrnt.setBcdStatus(0);
                        unitActionPrnt.setBisPush(1);

                        // Start making log with data
                        LogFlow logL = new LogFlow(logType, actData.getString("id_FC"),
                                actData.getString("id_FS"), "stateChg",
                                id_U, grpU, unitOItemPrnt.getId_P(), unitOItemPrnt.getGrpB(), unitOItemPrnt.getGrp(),
                                idPrnt, indexPrnt, id_C, unitOItemPrnt.getId_C(),
                                "", dep, "准备开始", 3, unitOItemPrnt.getWrdN(), wrdNU);

                        logL.setLogData_action(unitActionPrnt, unitOItemPrnt);

                        this.sendLogWSU(logL);
                    }

                        JSONObject mapKey = new JSONObject();
                        mapKey.put("action.objAction."+indexPrnt,unitActionPrnt);
                        coupaUtil.updateOrderByListKeyVal(idPrnt,mapKey);

//                            return true;
//                        } else {
//
//                            JSONObject mapKey = new JSONObject();
//                            mapKey.put("action.objAction."+indexPrnt,unitActionPrnt);
//                            coupaUtil.updateOrderByListKeyVal(idPrnt,mapKey);
//                        }

                    if (unitActionPrnt.getSumChild() != 0)
                    {
                        this.updateNext(orderAction, id_C, id_U,grpU, dep, wrdNU, logType);
                    }
                }
            }
        }

        // 用在 flowControl 卡片 改 grpB 的
        @Override
        public ApiResponse up_FC_action_grpB(String id_C, String id_O, String dep, String depMain, String logType,
                                             String id_Flow, JSONObject wrdFC, JSONArray grpB, JSONArray wrdGrpB)
        {
            JSONObject grpData = new JSONObject();
            String updatingGrp = "action.grpBGroup";

            if (id_O.equals(""))
            {                // 返回操作失败结果
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_GET_ORDER_NULL.getCode(), "没有关联订单");
            }


            // Converting id_FS/id_FC
            if (logType.endsWith("SL"))
            {
                logType = StringUtils.strip(logType, "SL");
                updatingGrp = "action.grpGroup";

            }

            for (int i = 0; i < grpB.size(); i++)
            {
                grpData.put(grpB.getString(i), new JSONObject());
                grpData.getJSONObject(grpB.getString(i)).put("dep",dep);
                grpData.getJSONObject(grpB.getString(i)).put("depMain",depMain);
                grpData.getJSONObject(grpB.getString(i)).put("logType",logType);
                grpData.getJSONObject(grpB.getString(i)).put("id_Flow",id_Flow);
                grpData.getJSONObject(grpB.getString(i)).put("id_O",id_O);
                grpData.getJSONObject(grpB.getString(i)).put("ref",grpB.getString(i));
                grpData.getJSONObject(grpB.getString(i)).put("wrdFC",wrdFC);
                grpData.getJSONObject(grpB.getString(i)).put("wrdN", wrdGrpB.getJSONObject(i));
            }

            try {
                JSONObject mapKey = new JSONObject();
                mapKey.put(updatingGrp, grpData);
                coupaUtil.updateOrderByListKeyVal(id_O, mapKey);
            } catch (RuntimeException e) {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_AN_ERROR_OCCURRED.getCode(), "不能开始,"+e);
            }


            return retResult.ok(CodeEnum.OK.getCode(), "done");

        }

//    let params2 ={
//            "dep": row.dep,
//            "depMain": row.depMain,
//            "logType": row.logType,
//            "id_Flow": row.id_Flow,
//            "id_O": row.id_O,
//            "wrdNList": grpB_wrdNList,
//            "grpB": row.grpB,
//            "wrdFC": row.wrdFC,
//}
//            this._http({
//                    method: "post",
//                    url: "/chat/flow/v1/up_FC_action_grpB",

    private void sendLogWSU(LogFlow logData) {

        WebSocketUserServer.sendLog(logData);

        //KEV  COW @ close but send message error
        //https://blog.csdn.net/canot/article/details/52495333

        logUtil.sendLog(logData.getLogType(),logData);

    }


    private JSONObject getActionData(String oid, Integer index) {
        // 创建卡片信息存储集合
        // 调用方法并且获取请求结果
        Order order = coupaUtil.getOrderByListKey(oid, Arrays.asList("info", "oItem", "action"));

        System.out.println("result ");
        // 判断订单为空
        if (null == order || order.getOItem() == null || order.getAction() == null) {
           
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        // 创建放回结果存储字典
        JSONObject result = new JSONObject();
        JSONArray actionArray = order.getAction().getJSONArray("objAction");
        JSONArray oItemArray = order.getOItem().getJSONArray("objItem");
        JSONObject grpBGroup = order.getAction().getJSONObject("grpBGroup");
        JSONObject grpGroup = order.getAction().getJSONObject("grpGroup");



        // 判断信息为空
        if (null == actionArray || null == oItemArray
                || actionArray.size() == 0 || oItemArray.size() == 0) {
            System.out.println("result  action array null");
            return null;
        }

        if (null == order.getAction()) {
            order.setAction(new JSONObject());
            order.getAction().put("objAction", new JSONArray());
            order.getAction().put("grpBGroup", new JSONObject());
            order.getAction().put("grpGroup", new JSONObject());
            //if null actionData, should just fail the API
        }

        int counter = 0;
        for (int i = 0; i < actionArray.size(); i++)
        {
            if (actionArray.getJSONObject(i).getInteger("bcdStatus") == 2)
            {
                counter++;
            }
        }



        JSONObject id_FC = grpBGroup.getJSONObject(oItemArray.getJSONObject(index).getString("grpB"));
        JSONObject id_FS = grpGroup.getJSONObject(oItemArray.getJSONObject(index).getString("grp"));

        result.put("orderAction",actionArray.getJSONObject(index));
        result.put("orderOItem",oItemArray.getJSONObject(index));
        result.put("oItemArray", oItemArray);
        result.put("actionArray", actionArray);
        result.put("progress", counter);
        result.put("grpBGroup",grpBGroup);
        result.put("grpGroup", grpGroup);
        result.put("id_FC", id_FC==null?"":id_FC.getString("id_Flow"));
        result.put("id_FS", id_FS==null?"":id_FS.getString("id_Flow"));
        result.put("info",order.getInfo());
        result.put("size", oItemArray.size());

        return result;

    }

    /**
     * 递归验证 - 注释完成
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @version 1.0.0
     * @date 2020/8/6 9:03
     */
    @Override
    public ApiResponse dgActivate(String id_O, Integer index, String myCompId, String id_U, String grpU, String dep, JSONObject wrdNU) {

            JSONObject actData = this.getActionData(id_O, index);

            if (null != actData) {
                OrderInfo unitInfo = JSONObject.parseObject(JSON.toJSONString(actData.get("info")),OrderInfo.class);
                if (unitInfo.getLST() != 7)
                 //All Push, not final cannot push, after final cannot move/delete oItem 全部推送，非最终不能推送，最终后不能移动／删除oItem
                {
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_ORDER_NEED_FINAL.getCode(), "还未确认");
                }
                OrderOItem unitOItem = JSONObject.parseObject(JSON.toJSONString(actData.get("orderOItem")),OrderOItem.class);
                OrderAction unitAction = JSONObject.parseObject(JSON.toJSONString(actData.get("orderAction")),OrderAction.class);

                if (unitAction.getBisPush() != 1)
                {
                    // 根据零件递归信息获取零件信息，并且制作日志
                    unitAction.setBcdStatus(0);
                    unitAction.setBisPush(1);

                    JSONObject mapKey = new JSONObject();
                    mapKey.put("action.objAction."+index,unitAction);
                    coupaUtil.updateOrderByListKeyVal(id_O,mapKey);


                    String logType = actData.getJSONObject("grpBGroup").getJSONObject(unitOItem.getGrpB()).getString("logType");

                    LogFlow logLP = new LogFlow(logType,actData.getString("id_FC"),
                        actData.getString("id_FS"),"stateChg",
                        id_U,grpU, unitOItem.getId_P(),unitOItem.getGrpB(),unitOItem.getGrp(),
                            id_O,unitAction.getIndex(), myCompId,unitOItem.getId_C(),
                        "",dep,"准备开始",3,unitOItem.getWrdN(),wrdNU);
                logLP.setLogData_action(unitAction,unitOItem);

                this.sendLogWSU(logLP);

            } else {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经推送过了");
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "done");
    }

    // this special Pushing API is for Start all orders from casItemx if they are "materials"
    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse dgActivateAll(String id_O, String myCompId, String id_U, String grpU, String dep, JSONObject wrdNU) {

        Order orderMainData = coupaUtil.getOrderByListKey(
                id_O, Arrays.asList("casItemx"));
        List <Order> orderDataList = new ArrayList<>();
        JSONArray orderList = orderMainData.getCasItemx().getJSONObject(myCompId).getJSONArray("objOrder");

//        JSONObject oParent = new JSONObject();
//        oParent.put("id_O", id_O);
//        orderList.add(oParent);

        for (Integer n = 0; n < orderList.size(); n++) {

            orderDataList.add(coupaUtil.getOrderByListKey(
                    orderList.getJSONObject(n).getString("id_O"), Arrays.asList( "info","oItem", "action")));
            OrderInfo unitInfo = JSONObject.parseObject(JSON.toJSONString(orderDataList.get(n).getInfo()), OrderInfo.class);

            if (unitInfo.getLST() != 7) {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_ORDER_NEED_FINAL.getCode(), "");
            }
        }
        // After checked all lST, they are all 7 and ready to push

        for (Integer i = 0; i < orderList.size(); i++) {
//            Order orderData = coupaUtil.getOrderByListKey(
//                    orderList.getJSONObject(i).getString("id_O"), Arrays.asList("oItem", "info", "action"));

            Order orderData = orderDataList.get(i);
            JSONArray objAction = orderData.getAction().getJSONArray("objAction");
            JSONArray objOItem = orderData.getOItem().getJSONArray("objItem");
            JSONObject grpBGroup = orderData.getAction().getJSONObject("grpBGroup");
            JSONObject grpGroup = orderData.getAction().getJSONObject("grpGroup");
            System.out.println("orderData"+orderData);

            for (Integer j = 0; j < objAction.size(); j++) {
                OrderAction unitAction = JSONObject.parseObject(JSON.toJSONString(objAction.getJSONObject(j)), OrderAction.class);
                OrderOItem unitOItem = JSONObject.parseObject(JSON.toJSONString(objOItem.getJSONObject(j)), OrderOItem.class);
                if (unitAction.getBisPush() != 1) {

                    // if bmdpt == 4, && sumPrev == 0, my subParts [prior == 0] starts
                    // for each subParts
                    // check if wn0prior == 0, if so dgActivate
                    if (unitAction.getBmdpt() == 4 && unitAction.getSumPrev() == 0)
                    {
                        for (int k = 0; k < unitAction.getSubParts().size(); k++)
                        {
                            JSONObject subOrderData = this.getActionData(unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                    unitAction.getSubParts().getJSONObject(k).getInteger("index"));

                            OrderOItem subOItem = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderOItem")),OrderOItem.class);
                            OrderAction subAction = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderAction")),OrderAction.class);

                            if (subAction.getPriority() == 1)
                            {
                                subAction.setBcdStatus(0);
                                subAction.setBisPush(1);

                                JSONObject mapKey = new JSONObject();
                                mapKey.put("action.objAction."+unitAction.getSubParts().getJSONObject(k).getInteger("index"),subAction);
                                coupaUtil.updateOrderByListKeyVal(unitAction.getSubParts().getJSONObject(k).getString("id_O"),mapKey);

                                System.out.println("unit "+subOItem.getGrpB()+subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()));
                                String logType = subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()).getString("logType");

                                LogFlow logLP = new LogFlow(logType,subOrderData.getString("id_FC"),
                                        subOrderData.getString("id_FS"),"stateChg",
                                        id_U,grpU, subOItem.getId_P(),subOItem.getGrpB(),subOItem.getGrp(),
                                        unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                        unitAction.getSubParts().getJSONObject(k).getInteger("index"),
                                        myCompId,subOItem.getId_C(),
                                        "",dep,"准备开始",3,subOItem.getWrdN(),wrdNU);
                                logLP.setLogData_action(subAction,subOItem);

                                this.sendLogWSU(logLP);
                            } else {
                                System.out.println("break @ "+k);
                                break;
                            }
                        }
                    }
                    else if (unitAction.getBmdpt() == 3 && unitAction.getSumPrev() == 0) {
                        //                // 根据零件递归信息获取零件信息，并且制作日志
                        unitAction.setBcdStatus(0);
                        unitAction.setBisPush(1);

                        JSONObject mapKey = new JSONObject();
                        mapKey.put("action.objAction."+j,unitAction);
                        coupaUtil.updateOrderByListKeyVal(orderList.getJSONObject(i).getString("id_O"),mapKey);

                        JSONObject fcCheck = grpBGroup.getJSONObject(unitOItem.getGrpB());
                        JSONObject fsCheck = grpGroup.getJSONObject(unitOItem.getGrp());
                        String id_FS = "";
                        String id_FC = "";

                        if (fcCheck != null)
                        {
                            id_FC = fcCheck.getString("id_Flow") != null
                                    && !unitOItem.getId_C().equals(myCompId) ?
                                    fcCheck.getString("id_Flow") : "";
                        }

                        if (fsCheck != null)
                        {
                            id_FS = fsCheck.getString("id_Flow") != null
                                    && !unitOItem.getId_C().equals(myCompId) ?
                                    fsCheck.getString("id_Flow") : "";
                        }

                        LogFlow logLP = new LogFlow(fcCheck.getString("logType"),
                                id_FC,
                                id_FS, "stateChg", id_U, grpU,
                                unitOItem.getId_P(), unitOItem.getGrpB(),unitOItem.getGrp(),unitAction.getId_O(), unitAction.getIndex(),
                                myCompId, unitOItem.getId_C(), "", dep, "准备开始", 3, unitOItem.getWrdN(), wrdNU);
                        logLP.setLogData_action(unitAction, unitOItem);
                        this.sendLogWSU(logLP);
                        // 发送日志
                    }
                }
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "doneAll");
    }



    /**
     * Create task OItem + Action + Log
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @version 1.0.0
     * @date 2020/8/6 9:03
     */
    @Override
    public ApiResponse createTask(String logType, String id_FC, String id_O, String myCompId, String id_U,
                                  String grpU, String dep, JSONObject oItemData, JSONObject wrdNU) {

        // Simply: set Order OItem.add[], Action.add[], send Log
        // Get order, check oItem size

        //if more than 5 Concurrent and not finished, it turns into Next task and send log Notify
        //so once all 5 are done, next 5 will show
        //can also set urgent (do it now, do it after, stop everything and do it now)
        //
        Integer index = 0;
        Integer prior = 1;
        JSONObject actData = this.getActionData(id_O, index);
        String id_FS = "";

        if (id_O.equals(""))
        {                // 返回操作失败结果
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_GET_ORDER_NULL.getCode(), "没有关联订单");
        }


        // Converting id_FS/id_FC
        if (logType.endsWith("SL"))
        {
            //logType = actionSL, FC = FS,
            logType = StringUtils.strip(logType, "SL");
            id_FS = id_FC;
            id_FC = "";
        }

        // Make sure index = 0 works by init the oItem[]
        if (actData != null)
        {
            // get the size of oItem, so I can append new "task" to it
            // 获取oItem的大小，这样我就可以向它附加新的“任务”
            index = actData.getInteger("size");
            prior = actData.getJSONArray("oItemArray").getJSONObject(index - 1).getInteger("wn0prior");

        }

        // Adding oItem and Action
        OrderOItem unitOItem = new OrderOItem ("","",oItemData.getString("id_CP"),myCompId,myCompId,
                id_O,index,1,"","",
                oItemData.getString("grp"),oItemData.getString("grpB"),prior,oItemData.getString("pic"),
                oItemData.getInteger("lUT"),oItemData.getInteger("lCR"),oItemData.getDouble("wn2qtyneed"),
                oItemData.getDouble("wn4price"),oItemData.getJSONObject("wrdNP"),oItemData.getJSONObject("wrdN"),
                oItemData.getJSONObject("wrddesc"),oItemData.getJSONObject("wrdprep"));

        OrderAction unitAction = new OrderAction(0,1,5,1,
                "","","",id_O,index, prior,0,0,
                null,null,null,null,
                oItemData.getJSONObject("wrdNP"),oItemData.getJSONObject("wrdN"));

        // Send a log
        LogFlow logLP = new LogFlow(logType,id_FC,
                id_FS,"stateChg", id_U,grpU,"",unitOItem.getGrpB(), "",id_O,index, myCompId,myCompId,
                oItemData.getString("pic"),dep,"准备开始",3,oItemData.getJSONObject("wrdN"),wrdNU);

        logLP.setLogData_action(unitAction,unitOItem);
        // append an OItem + ActionItem, save OItem and action
        JSONObject mapKey = new JSONObject();
        mapKey.put("action.objAction."+index,unitAction);
        mapKey.put("oItem.objItem."+index,unitOItem);
        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);

        this.sendLogWSU(logLP);

        return retResult.ok(CodeEnum.OK.getCode(), "done");
    }

    /**
     * *** 新增提问任务 ***
     * Get the id_ProbFC, get the id_O of prob, get the last item
     * Create oItem, action, with id_OP/index correctly defined
     * Append a new task to the last Item save MDB
     * Send Log
     * Return the probList, to able to show on my screen
     * 获取id_Probfc，获取Prob的id_o，获取最后一项
     * 创建oItem，action，并正确定义id_op／index
     * 将新任务追加到保存MDB的最后一项
     * 发送日志
     * 返回probList，以便能够显示在我的屏幕上
     */

    @Override
    public ApiResponse createQuest(String myCompId, String id_O, Integer index, String id_Prob, String id_FC,
                                   String id_FQ,
                                   String id_U, String grpU, String dep, JSONObject wrdNU,
                                  JSONObject probData) {

        JSONObject probSet = this.getActionData(id_O, index);

        // go to prob fixing group, make a new OItem, send log
        JSONObject actData = this.getActionData(id_Prob, 0);

        Integer indexProb = 0;
        Integer priorProb = 1;

        if (id_Prob.equals(""))
        {                // 返回操作失败结果
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_GET_ORDER_NULL.getCode(), "没有关联订单");
        }

        if (actData != null)
        {
            // get the size of oItem, so I can append new "task" to it
            // 获取oItem的大小，这样我就可以向它附加新的“任务”
            indexProb = actData.getInteger("size");
            priorProb = actData.getJSONArray("oItemArray").getJSONObject(indexProb - 1).getInteger("wn0prior");
        }

        OrderAction orderAction = JSONObject.parseObject(JSON.toJSONString(probSet.get("orderAction")),OrderAction.class);
        OrderOItem orderOItem = JSONObject.parseObject(JSON.toJSONString(probSet.get("orderOItem")),OrderOItem.class);

        if (orderAction.getProb() == null)
        {
            orderAction.setProb(new JSONArray());
        }

        Integer sumProb = orderAction.getProb().size();

        String logTypeProb = probData.getString("logType");

        probData.put("index",indexProb);
        orderAction.getProb().add(probData);

        JSONObject mapKey = new JSONObject();
        mapKey.put("action.objAction."+index,orderAction);
        // adding prob[] into id_O[index]
        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);

        //KEV possibly set cross company Questing? no, order must be a new order with CB != C and have id_P
        // ...

        OrderOItem unitOItem = new OrderOItem ("",id_O,"",myCompId,myCompId,
                id_Prob,indexProb,1,"","",
                "1009","1009",priorProb, orderOItem.getPic(),
                1,1,1.0,
                0.0,probData.getJSONObject("wrdNP"),probData.getJSONObject("wrdN"),
                probData.getJSONObject("wrddesc"),probData.getJSONObject("wrdprep"));

        // Need to store id_O and Index into action Card so we will able to set prob finish
        JSONArray upPrnt = new JSONArray();
        JSONObject upPrntsData = new JSONObject();
        upPrntsData.put("id_O", id_O);
        upPrntsData.put("index", index);
        upPrntsData.put("probIndex",sumProb);
        upPrnt.add(upPrntsData);
        System.out.println("probData"+probData);
        OrderAction unitAction = new OrderAction(0,1,7,1,probData.getString("id_OP"),probData.getString("refOP"),"",
                id_O,index,priorProb,0,0, null,upPrnt,null,null,
                probData.getJSONObject("wrdNP"),probData.getJSONObject("wrdN"));

        LogFlow logProb = new LogFlow(logTypeProb,id_FC,
                id_FQ,"stateChg", id_U,grpU,orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(), id_Prob,indexProb, myCompId,myCompId,
                probData.getString("pic"),dep,"准备解决",3,probData.getJSONObject("wrdN"),wrdNU);

               logProb.setLogData_action(unitAction,unitOItem);
//        logProb.setActionData(unitAction.getBisactivate(),unitAction.getBcdStatus(),unitAction.getId_Us(),1.0,
//                orderOItem.getId_P(), id_O,index,1,unitAction.getWrdNP(),unitOItem.getWrdN());

        JSONObject probResult = new JSONObject();
        probResult.put("action.objAction."+indexProb,unitAction);
        probResult.put("oItem.objItem."+indexProb,unitOItem);

        coupaUtil.updateOrderByListKeyVal(id_Prob,probResult);

        this.sendLogWSU(logProb);

        return retResult.ok(CodeEnum.OK.getCode(), "done");

    }



//    public ApiResponse oItemUpdatedRePush(String id_C, String id_O, String grpP, String grpU)
//    {
//        //KEV OItem MUST have grpP, and must have grpBGroup @ questing and task OK
//        // NOT important API, becaue you should not change the oItem ...
//
//        // after oItem changed, you can save it in oItem, must push to task
//        // bisPush = 3, then after push become 1
//        // then this button show instead of the Push button,
//        // then send a log to update detail of the "QTP" (Quest Task DGProcess)
//
//        //KEV OItem必须有grpP，并且必须有grpBGroup @ questing and task
//        //oItem更改后，可以将其保存在oItem中，
//        //bisPush变成3？
//        //然后显示这个按钮而不是推送按钮，
//        //然后发送日志来更新“QTP”（Quest Task DGProcess）的详细信息
//
//
//        return retResult.ok(CodeEnum.OK.getCode(), "done");
//
//    }

    /***
     * Final an order
     * Final dgOrder
     * Create subTasks
     * dgTask order, push TaskOrder
     * dg 1 oItem, push 1 oItem
     * add oItem to internal Order
     * change Dep
     * addOItem will need to fix action
      *dgTask need to fix action
     * make UI for pick dep / id_FC
     */

    public ApiResponse switchTask2Prod() //

    {
        //KEV
        //setOItem to OrderItem
        // id_P wn2qtyNow
        // getDgOItem API for igura, and this switch
        // Special bmdpt for dg-able product
        //if part, can dg -> final -> Push
        //

        return retResult.ok(CodeEnum.OK.getCode(), "done");

    }


    /**
     * 双方确认订单
     * ##param id_C	公司编号
     * ##param id_O	订单编号
     * @return java.lang.String  返回结果: 结果
     * @author Kevin
     * @version 1.0.0
     * @date 2021/6/16 14:49
     */
    @Override
    public ApiResponse confirmOrder(String id_C,String id_O) throws IOException {
        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info"));
        QueryBuilder queryBuilder;

        // if I am both buyer and seller, internal order, whatever side I am, set it to both final
        if (order.getInfo().getId_C().equals(id_C) && order.getInfo().getId_C().equals(order.getInfo().getId_CB())) { //Check C== CB
            order.getInfo().setLST(7);
            queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchPhraseQuery("id_O", id_O))
                    .must(QueryBuilders.matchPhraseQuery("id_CB", id_C));
        } // I am id_CB Buyer
        else if (id_C.equals(order.getInfo().getId_CB())) { //if Seller is REAL
            if (redisUtils.judgeComp(id_C, order.getInfo().getId_C()) == 1) {
                if (order.getInfo().getLST() == 6) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(7);
                } else {
                    order.getInfo().setLST(5);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(7);
            }
            queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchPhraseQuery("id_O", id_O))
                    .must(QueryBuilders.matchPhraseQuery("id_CB", id_C));
        } else // I am Seller
        { //if Buyer is REAL
            if (redisUtils.judgeComp(id_C, order.getInfo().getId_CB()) == 1) {
                if (order.getInfo().getLST() == 5) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(7);
                } else {
                    order.getInfo().setLST(6);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(7);
            }
            queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchPhraseQuery("id_O", id_O))
                    .must(QueryBuilders.matchPhraseQuery("id_C", id_C));
        }
        JSONObject mapKey = new JSONObject();
        mapKey.put("info.lST", order.getInfo().getLST());
        coupaUtil.updateOrderByListKeyVal(id_O, mapKey);

        //Save MDB above, then update ES here
        JSONObject listCol = new JSONObject();
        listCol.put("lST", order.getInfo().getLST());
        dbUtils.updateES(queryBuilder, "lsborder", listCol);

        return retResult.ok(CodeEnum.OK.getCode(), order.getInfo().getLST());
    }

    @Override
    public ApiResponse cancelOrder(String id_C,String id_O) {
        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info"));
        //Seller = 14, Buyer = 15, Both = 16

        // if I am both buyer and seller, internal order, whatever side I am, set it to both final
        if (order.getInfo().getId_C().equals(id_C) && order.getInfo().getId_C().equals(order.getInfo().getId_CB())) { //Check C== CB
            order.getInfo().setLST(16);
        } // I am id_CB Buyer
        else if (id_C.equals(order.getInfo().getId_CB())) { //if Seller is REAL
            if (redisUtils.judgeComp(id_C, order.getInfo().getId_C()) == 1) {
                if (order.getInfo().getLST() == 14) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(16);
                } else {
                    order.getInfo().setLST(15);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(16);
            }
        } else // I am Seller
        { //if Buyer is REAL
            if (redisUtils.judgeComp(id_C, order.getInfo().getId_CB()) == 1) {
                if (order.getInfo().getLST() == 15) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(16);
                } else {
                    order.getInfo().setLST(14);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(16);
            }
        }
        JSONObject mapKey = new JSONObject();
        mapKey.put("info.lST", order.getInfo().getLST());
        coupaUtil.updateOrderByListKeyVal(id_O, mapKey);
        return retResult.ok(CodeEnum.OK.getCode(), order.getInfo());
    }




    @Override
    public ApiResponse dgConfirmOrder(String id_C, JSONArray casList) throws IOException {
        //KEV
        //get casItemx
        //loop each order, pick the id_O
        //orderService.confirmOrder(id_C, id_O)
        //return if any order that's not BOTH confirmed

        // loop casItemx orders
        JSONObject result = new JSONObject();

        for (int i = 0; i < casList.size(); i++) {
            // check if orders are final, if need request Cancel, send request
            // if ok, open order
            result.put("final",new JSONArray());
            result.put("await", new JSONArray());
            String subOrderId = casList.getJSONObject(i).getString("id_O");

            this.confirmOrder(id_C, subOrderId);
        }

            return retResult.ok(CodeEnum.OK.getCode(), result);

    }

    //For change Dep/Grp @ grpBGroup,
    //Loop thru all Flowcontrol items
    //Pick those that match grpB[], return Object of FlowControl item
    @Override
    public ApiResponse getFlowList(String id_C, String grpB)
    {
        String assetId = redisUtils.getId_A(id_C, "a-auth");
        Asset assetData = coupaUtil.getAssetById(assetId, Collections.singletonList("flowControl"));
        if (null == assetData) {
            // 返回操作失败结果
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ChatEnum.ERR_GET_DATA_NULL.getCode(), "获取数据为空");
        }
        JSONArray result = new JSONArray();

        JSONArray objData = assetData.getFlowControl().getJSONArray("objData");
        for (int i = 0; i < objData.size(); i++) {
            JSONArray grpList = objData.getJSONObject(i).getJSONArray("grpB");
            System.out.println("grp"+objData.getJSONObject(i));

            if (grpList != null && grpList.contains(grpB)) {
                System.out.println("inHere "+grpB);
                result.add(objData.getJSONObject(i));
            }
        }

        return retResult.ok(CodeEnum.OK.getCode(), result);
    }


//    @Override
//    //KEV  Delete this
//    // sending Log to different id_FC? grpP / id / dep setup
//    //1. need to check seq and see if they are going to push later
//    //2. need to make Next/Prev in action
//    //3. need to check ALL grpP and push to the right id_FC
//    //4. id_FC cannot have, instead, grpP must have
//    //5. no log push, use the dgActivateAllTask
//    // NO use for this one
//    public ApiResponse genAction(String id_C, String id_O, String id_U, String grpU, String dep, JSONObject wrdNU,
//                                   String id_FC) {
//
//        Order orderData = coupaUtil.getOrderByListKey(
//                id_O, Arrays.asList("oItem", "info"));
//
//        orderData.setAction(new JSONObject());
//        orderData.getAction().put("objAction", new JSONArray());
//        //KEV gen grpBGroup!!
//        orderData.getAction().put("grpBGroup", new JSONObject());
//        orderData.getAction().put("grpGroup", new JSONObject());
//
//        JSONArray objAction = orderData.getAction().getJSONArray("objAction");
//        JSONArray objOItem = orderData.getOItem().getJSONArray("objItem");
//        JSONObject grpBGroup = orderData.getAction().getJSONObject("grpBGroup");
//        JSONObject grpGroup = orderData.getAction().getJSONObject("grpGroup");
//       // for each current oItem,
//        // create unitAction data
//
//        for (Integer i = 0; i < objOItem.size(); i++) {
//
//            // create unitAction data
//            OrderOItem unitOItem = JSONObject.parseObject(JSON.toJSONString(objOItem.getJSONObject(i)),OrderOItem.class);
//
//            OrderAction unitAction = new OrderAction(0, 1, 5, 1,
//                    "","", "", id_O, i, 100, 0, 0,
//                    null, null, null, null,
//                    unitOItem.getWrdNP(), unitOItem.getWrdN());
//
//            objAction.add(unitAction);
////KEV gen GrpBGroup, just new object not good, need to check def to get init Values, if push, will push to that id_FC
//
//            if (null == grpBGroup.getJSONObject(unitOItem.getGrpB())) {
//                grpBGroup.put(unitOItem.getGrpB(), new JSONObject());
//            }
//
//            if (unitOItem.getId_C() != id_C) {
//                if (null == grpGroup.getJSONObject(unitOItem.getGrp())) {
//                    grpGroup.put(unitOItem.getGrp(), new JSONObject());
//                }
//            }
//
//            // send Log of Action
//            String logType = grpBGroup.getJSONObject(unitOItem.getGrpB()).getString("logType");
//
//            LogFlow logLP = new LogFlow(logType,id_FC,
//                    "","stateChg", id_U,grpU, "",unitOItem.getGrpB(),"",
//                    id_O, i, id_C,id_C, "",dep,"准备开始",3,unitOItem.getWrdN(),wrdNU);
//            logLP.setLogData_action(unitAction,unitOItem);
//
//            this.sendLogWSU(logLP);
//
//        }
//        // Save all uniAction[]
//            JSONObject mapKey = new JSONObject();
//            mapKey.put("action.objAction",objAction);
//            mapKey.put("action.grpBGroup", grpBGroup);
//            mapKey.put("action.grpGroup", grpGroup);
//            coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
//
//        return retResult.ok(CodeEnum.OK.getCode(), "done");
//    }

    //@Override
    // ??  sending Log to different id_FC? grpP / id / dep setup
    public ApiResponse genFlowControlOrder(String id_C, String id_U, String grpU, String dep, JSONObject wrdN,
                                 String id_FC) {

        String newId = MongoUtils.GetObjectId();
        Order order = new Order();
        order.setId(newId);
        //Set info card
        OrderInfo info = new OrderInfo(id_C,id_C,id_C,id_C,"","","","1009","1009",1.0,"",7,0,wrdN,null);
        //Set view card
        JSONArray view = new JSONArray();
        view.add("info"); view.add("oItem"); view.add("action");
        order.setInfo(info);
        order.setView(view);
        order.setOItem(new JSONObject());

        //Set oItem card
        //Set action card

        //Update order data
        //Update ES
        coupaUtil.saveOrder(order);


//        let data = {
//                view: ["info", "oItem", "action"],
//        oItem: {
//            oItemType : 2,
//                    objItem : []
//        },
//        action: {
//            objAction: [],
//            grpBGroup: {"1000":{}},
//            grpGroup: {"1000":{}}
//        },
//        info: {
//            id_C: this.mainData.compId,
//                    id_CB: this.mainData.compId,
//                    wrdN: {
//                "cn": row.wrdN.cn + "进度管理"
//            },
//            wrddesc: {
//                "cn": row.wrdN.cn + "进度管理"
//            },
//            grpU: "1001",
//                    refB: '',
//                    ref: '',
//                    dep: this.newEntryDep,
//                    lST: 4,
//                    grp: '1009',
//                    grpB: '1009'
//        }
//      };
//
//
//        orderData.setAction(new JSONObject());
//        orderData.getAction().put("objAction", new JSONArray());
//        orderData.getAction().put("grpBGroup", new JSONObject());
//        orderData.getAction().put("grpGroup", new JSONObject());
//
//        JSONArray objAction = orderData.getAction().getJSONArray("objAction");
//        JSONArray objOItem = orderData.getOItem().getJSONArray("objItem");
//        JSONObject grpBGroup = orderData.getAction().getJSONObject("grpBGroup");
//        JSONObject grpGroup = orderData.getAction().getJSONObject("grpGroup");
//        // for each current oItem,
//        // create unitAction data
//
//        for (Integer i = 0; i < objOItem.size(); i++) {
//
//            // create unitAction data
//            OrderOItem unitOItem = JSONObject.parseObject(JSON.toJSONString(objOItem.getJSONObject(i)),OrderOItem.class);
//
//            OrderAction unitAction = new OrderAction(0, 1, 5, 1,
//                    "", "", id_O, i, 100, 0, 0,
//                    null, null, null, null,
//                    unitOItem.getWrdNP(), unitOItem.getWrdN());
//
//            objAction.add(unitAction);
//
//            if (null == grpBGroup.getJSONObject(unitOItem.getGrpB())) {
//                grpBGroup.put(unitOItem.getGrpB(), new JSONObject());
//            }
//
//            if (unitOItem.getId_C() != id_C) {
//                if (null == grpGroup.getJSONObject(unitOItem.getGrp())) {
//                    grpGroup.put(unitOItem.getGrp(), new JSONObject());
//                }
//            }
//
//            // send Log of Action
//            LogFlow logLP = new LogFlow("action",id_FC,
//                    "","stateChg", id_U,grpU, "",
//                    id_O, i, id_C,id_C, "",dep,"准备开始",3,unitOItem.getWrdN(),wrdNU);
//            logLP.setLogData_action(unitAction,unitOItem);
//
//            this.sendLogWSU(logLP);
//
//        }
//        // Save all uniAction[]
//        JSONObject mapKey = new JSONObject();
//        mapKey.put("action.objAction",objAction);
//        mapKey.put("action.grpBGroup", grpBGroup);
//        mapKey.put("action.grpGroup", grpGroup);
//        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
//
        return retResult.ok(CodeEnum.OK.getCode(), "done");
    }
}
