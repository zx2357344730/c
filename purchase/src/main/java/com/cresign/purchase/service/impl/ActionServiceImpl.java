package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.client.WSClient;
import com.cresign.purchase.common.ChatEnum;
import com.cresign.purchase.service.ActionService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ut;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class ActionServiceImpl implements ActionService {

//    public static final String PI_GP_T = "pi:gp_";
//    public static final String PI = "pi:p_";

    /**
     * getActionData **** 这个很重要，拿了action / oItem 的内容都要转换
     * changeActionStatus -> updateNext -> updateParent
     *
     * 这几个没有改完 dgActivate / switchTask2Prod / genFlowControlOrder
     *
     * 这些是OK 的
     * createQuest / createTask / dgConfirmOrder
     * getFlowList / changeDepAndFlow / dgActivateAll
     */
        


        @Autowired
        private RetResult retResult;

        @Autowired
        private CoupaUtil coupaUtil;

        @Autowired
        private WSClient ws;

        @Autowired
        private Ut ut;

        @Autowired
        private Qt qt;

        @Autowired
        private DbUtils dbUtils;

        @Autowired
        private RestHighLevelClient restHighLevelClient;

//    /**
//     * 注入redis数据库下标1模板
//     */
//    @Resource
//    private StringRedisTemplate redisTemplate0;
//    public static final String HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_RPI_T = "https://www.cresign.cn/qrCodeTest?qrType=rpi&t=";

    /**
     * 根据oId修改grpBGroup字段
     * @param id_O	   订单编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @date 2021/9/10 17:32
     */
    @Override
    public ApiResponse changeDepAndFlow(String id_O, String grpB, JSONObject grpBOld,JSONObject grpBNew
            ,String id_C,String id_U,String grpU, JSONObject wrdNU) {

//        //1. Save the new
//        JSONObject mapKey = new JSONObject();
//        mapKey.put("action.grpBGroup."+grpB,grpBNew);
//        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
        qt.setMDContent(id_O, qt.setJson("action.grpBGroup."+grpB, grpBNew), Order.class);
        //2. get Order's oItem+action
//        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info","oItem", "action"));
        Order order = qt.getMDContent(id_O,qt.strList("action", "oItem","info"), Order.class);
        JSONArray objItem = order.getOItem().getJSONArray("objItem");
        JSONArray objAction = order.getAction().getJSONArray("objAction");
        //3. Loop check grpB==oItem(i).grpB and objAction(i).isPUshed == 1
        for (int i = 0; i < objItem.size(); i++) {
            if (grpB.equals(objItem.getJSONObject(i).getString("grpB")) &&
            objAction.getJSONObject(i).getInteger("bisPush") == 1 &&
            objAction.getJSONObject(i).getInteger("bcdStatus") != 2){

                OrderAction oAction = ut.jsonTo(objAction.getJSONObject(i),OrderAction.class);
                OrderOItem oItem = ut.jsonTo(objItem.getJSONObject(i), OrderOItem.class);

                //4. Send Log to stop old Flow
                LogFlow logStop = new LogFlow("action", // grpBOld.getString("logType"),
                        grpBOld.getString("id_FC"),"","stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),grpBOld.getString("grpB"), "",
                        objAction.getJSONObject(i).getString("id_OP"),
                        objItem.getJSONObject(i).getString("id_O"),
                        i,id_C,order.getInfo().getId_C(),objItem.getJSONObject(i).getString("pic"),grpBOld.getString("dep"),
                        "转换另一队伍执行",3,objItem.getJSONObject(i).getJSONObject("wrdN"), wrdNU);
                logStop.setLogData_action(oAction, oItem);
                logStop.getData().put("bcdStatus", 9);

                LogFlow logStart = new LogFlow("action", //grpBOld.getString("logType"),
                        grpBNew.getString("id_FC"),"","stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),grpBNew.getString("grpB"), "",
                        objAction.getJSONObject(i).getString("id_OP"),
                        objItem.getJSONObject(i).getString("id_O"),
                        i,id_C,order.getInfo().getId_C(),objItem.getJSONObject(i).getString("pic"),grpBNew.getString("dep"),
                        "转换队伍，开始执行",3,objItem.getJSONObject(i).getJSONObject("wrdN"), wrdNU);
                logStart.setLogData_action(oAction, oItem);
                logStart.getData().put("bcdStatus", 1);

                ws.sendWS(logStop);
                ws.sendWS(logStart);
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "换群成功");

    }

    @Override
    public ApiResponse changeDepAndFlowSL(String id_O, String grpB, JSONObject grpBOld,JSONObject grpBNew
            ,String id_C,String id_U,String grpU, JSONObject wrdNU) {

        //1. Save the new
        JSONObject mapKey = new JSONObject();
        mapKey.put("action.grpGroup."+grpB,grpBNew);
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

                OrderAction oAction = ut.jsonTo(objAction.getJSONObject(i),OrderAction.class);
                OrderOItem oItem = ut.jsonTo(objItem.getJSONObject(i), OrderOItem.class);

                //4. Send Log to stop old Flow
                LogFlow logStop = new LogFlow("action","",grpBOld.getString("id_FC"),"stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),"", grpBOld.getString("grpB"),
                        objAction.getJSONObject(i).getString("id_OP"),
                        objItem.getJSONObject(i).getString("id_O"),
                        i,order.getInfo().getId_CB(),id_C,objItem.getJSONObject(i).getString("pic"),grpBOld.getString("dep"),
                        "转换另一队伍执行",3,objItem.getJSONObject(i).getJSONObject("wrdN"), wrdNU);
                logStop.setLogData_action(oAction, oItem);
                logStop.getData().put("bcdStatus", 9);

                LogFlow logStart = new LogFlow("action","",grpBNew.getString("id_FC"),"stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),"", grpBNew.getString("grpB"),
                        objAction.getJSONObject(i).getString("id_OP"),
                        objItem.getJSONObject(i).getString("id_O"),
                        i,order.getInfo().getId_CB(),id_C,objItem.getJSONObject(i).getString("pic"),grpBNew.getString("dep"),
                        "转换另一队伍执行",3,objItem.getJSONObject(i).getJSONObject("wrdN"), wrdNU);

                logStart.setLogData_action(oAction, oItem);
                logStart.getData().put("bcdStatus", 1);

                ws.sendWS(logStop);
                ws.sendWS(logStart);
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "换群成功");

    }

    @Override
    public ApiResponse rePush(String id_O, Integer index, JSONObject tokData) throws IOException {

        JSONObject actData = this.getActionData(id_O, index);

        OrderOItem orderOItem = ut.jsonTo(actData.get("orderOItem"), OrderOItem.class);
        OrderAction orderAction = ut.jsonTo(actData.get("orderAction"), OrderAction.class);

        String taskName = orderOItem.getWrdN().getString("cn");

        if (orderAction.getBcdStatus() != 100 && orderAction.getBcdStatus() != 2 && orderAction.getBcdStatus() != 9)
        {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "这任务没有关闭");
        }

        // 备注
        String message = taskName + "[已恢复执行]";
        JSONObject res = new JSONObject();
        JSONObject mapKey = new JSONObject();
        JSONObject listCol = new JSONObject();

        mapKey.put("info.lST", 8);
        listCol.put("lST", 8);
        // 设置产品状态
        orderAction.setBcdStatus(3);

        // Start making log with data
        LogFlow logL = new LogFlow("action",actData.getString("id_FC"),
                actData.getString("id_FS"),"stateChg", tokData.getString("id_U"),tokData.getString("grpU"),orderOItem.getId_P(),orderOItem.getGrpB(),orderOItem.getGrp(),
                orderAction.getId_OP(), id_O,index,actData.getJSONObject("info").getString("id_CB"),orderOItem.getId_C(), "",tokData.getString("dep"),message,3,orderOItem.getWrdN(),tokData.getJSONObject("wrdNU"));

        logL.setLogData_action(orderAction,orderOItem);
        logL.getData().put("wn0prog", 0);

        try {
            ws.sendWS(logL);

            mapKey.put("action.objAction."+index,orderAction);
            coupaUtil.updateOrderByListKeyVal(id_O,mapKey);

            if(null != listCol.getInteger("lST")) {
                QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("id_O", id_O));
                dbUtils.updateListCol(queryBuilder, "lsborder", listCol);
            }
            return retResult.ok(CodeEnum.OK.getCode(), res);

        } catch (RuntimeException e) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_AN_ERROR_OCCURRED.getCode(), "不能开始,"+e);
        }
    }
    /**
         * 操作开始，暂停，恢复功能 - 注释完成
         * @return java.lang.String  返回结果: 结果
         * @author tang
         * @ver 1.0.0
         * @date 2020/8/6 9:22
         */


            @Override
            @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
            public ApiResponse changeActionStatus(String logType, Integer status, String msg,
                                                  Integer index, String id_O, Boolean isLink,
                                                  String id_FC, String id_FS, JSONObject tokData) throws IOException {

                JSONObject actData = this.getActionData(id_O, index);

                OrderOItem orderOItem = ut.jsonTo(actData.get("orderOItem"), OrderOItem.class);
                OrderAction orderAction = ut.jsonTo(actData.get("orderAction"),OrderAction.class);

                String taskName = orderOItem.getWrdN().getString("cn");

                // 根据下标获取递归信息

            System.out.println("bcdStatus"+orderOItem.getWrdN());

            // 判断新的状态和旧状态不一样
            if (orderAction.getBcdStatus().equals(status)){
                // 抛出操作成功异常
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "该操作已被处理");
            }

            // 备注
            String message = "";
            JSONObject res = new JSONObject();
            JSONObject mapKey = new JSONObject();
            JSONObject listCol = new JSONObject();

                /***
                 * logStatus = 2@推送 3@开始 1@停 -1@再开 -6@取消 5@完成
                 */
            Integer logStatus = 2;
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
                    message = taskName + "[开始运行]"+ msg;
                    logStatus = 3;
                    JSONArray id_Us = orderAction.getId_Us() == null? new JSONArray() : orderAction.getId_Us();

                    //Adding myself to the id_Us of action to indicate
                        id_Us.add(tokData.getString("id_U"));
                        orderAction.setId_Us(id_Us);
                    res.put("isJoin", 1);
                    res.put("id_Us", orderAction.getId_Us());
                    if (actData.getJSONObject("info").getInteger("lST") == 7)
                    {
                        mapKey.put("info.lST", 8);
                        listCol.put("lST", 8);
                    }
                    break;
                case 2:                    // 2 = finish, set qtyfin setNextDesc
                    if (orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3
                            && orderAction.getBcdStatus() != 7)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经处理了");
                    }
                    if (orderAction.getSumChild() > 0 && orderAction.getBmdpt() == 2 && isLink)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_RECURRED.getCode(), "还有子工序没完成，不能完成");
                    }
                    logStatus = 5;

                    Double progress = Double.valueOf((actData.getInteger("progress") + 1) / actData.getJSONArray("actionArray").size() * 100);
                    //update actions' total progress
                    if (progress == 100.0)
                    {
                        mapKey.put("info.lST", 13);
                        listCol.put("lST", 13);

                    }
                    mapKey.put("action.wn2progress", progress );

                    message = taskName + "[已完成]" + msg;
                    break;
                case 3: // resume OItem
                    if (orderAction.getBcdStatus() != 8)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");
                    }
                    message = taskName + "[已恢复执行]"  + msg;
                    logStatus = -1;

                    break;
                case 5:
                    if (orderAction.getId_Us() == null) {
                        orderAction.setId_Us(new JSONArray());
                    }
                    if ( !orderAction.getId_Us().contains(tokData.getString("id_U")))
                    {
                        orderAction.getId_Us().add(tokData.getString("id_U"));
                    }
                    message = tokData.getJSONObject("wrdNU").getString("cn") + "[加入成功]"  + msg;
//                    result = "[加入成功]"  + msg;
                    status = orderAction.getBcdStatus();
                    if (status == 0)
                    {
                        status = 1;
                        logStatus = 3;
                        message = tokData.getJSONObject("wrdNU").getString("cn") + "[加入成功，开始执行]"  + msg;
                    }
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
                    message = taskName + "[继续下一个]"+msg;
                    break;
                case 8: // pause
                    if (orderAction.getBcdStatus() != 1
                            && orderAction.getBcdStatus() != 3)
                    {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");
                    }                    // 设置备注信息
                    message = taskName + "[已暂停]" + msg;
                    logStatus = 1;
                    break;
                case 9: // cancel
                    // pause but it's nothing special
                    // 设置备注信息
                    message = taskName + "[已取消]" + msg;
                    logStatus = -6;
                    break;
                default:
                    message = taskName + "[无法操作]";
                    break;
            }

            // 设置产品状态
            orderAction.setBcdStatus(status);
            String compId;

            if (logType.endsWith("SL"))
            {
                compId = actData.getJSONObject("info").getString("id_CB");
            } else
            {
                compId = tokData.getString("id_C");
            }

            // Start making log with data
            LogFlow logL = new LogFlow("action",id_FC,
                    id_FS,"stateChg", tokData.getString("id_U"),tokData.getString("grpU"),orderOItem.getId_P(),orderOItem.getGrpB(),orderOItem.getGrp(),
                    orderAction.getId_OP(), id_O,index,compId,orderOItem.getId_C(), "",tokData.getString("dep"),message,3,orderOItem.getWrdN(),tokData.getJSONObject("wrdNU"));

//            LogFlow logL = dbUtils.getRecentLog(id_O, index, tokData);
            logL.setLogData_action(orderAction,orderOItem);
            logL.getData().put("wn0prog", logStatus);


            try {
                ws.sendWS(logL);

                mapKey.put("action.objAction."+index,orderAction);
//                coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
                qt.setMDContent(id_O, mapKey, Order.class);

                if(null != listCol.getInteger("lST")) {
//                    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                            .must(QueryBuilders.termQuery("id_O", id_O));
//                    dbUtils.updateListCol(queryBuilder, "lsborder", listCol);
                    qt.setES("lsborder", qt.setESFilt("id_O", id_O), listCol);
                }
            } catch (RuntimeException e) {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_AN_ERROR_OCCURRED.getCode(), "不能开始,"+e);
            }

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

//                    JSONObject probKey = new JSONObject();
//                    probKey.put("action.objAction."+upPrnt.getInteger("index"),objAction);
//                    coupaUtil.updateOrderByListKeyVal(upPrnt.getString("id_O"),probKey);
                    qt.setMDContent(upPrnt.getString("id_O"),
                            qt.setJson("action.objAction."+upPrnt.getInteger("index"),objAction),
                            Order.class);
                } catch (RuntimeException e) {
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_AN_ERROR_OCCURRED.getCode(), "不能开始,"+e);
                }
            } else if ((status == 2 && orderAction.getBisactivate() != 4) || status == 7) {
                // activate = 4 means Skip = already pushed Next

//                Boolean childrenAllDone =
                this.updateParent(orderAction, tokData, logType);
//                // 判断父产品不为空
//                if (!childrenAllDone) {
//                    this.updateNext(orderAction, id_C, id_U,grpU, dep, wrdNU, logType);
//                }
            }


                // 抛出操作成功异常
            return retResult.ok(CodeEnum.OK.getCode(), res);
        }

    private void updateNext(OrderAction orderAction, JSONObject tokData, String logType)
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
                                LogFlow logL = new LogFlow("action", actData.getString("id_FC"),
                                        actData.getString("id_FS"), "stateChg",
                                        tokData.getString("id_U"), tokData.getString("grpU"), orderOItem1.getId_P(),orderOItem1.getGrpB(), orderOItem1.getGrp(),
                                        orderAction1.getId_OP(), nextId, nextIndex, tokData.getString("id_C"), orderOItem1.getId_C(),
                                        "", tokData.getString("dep"), orderOItem1.getWrdN().get("cn") + "准备开始", 3, orderOItem1.getWrdN(), tokData.getJSONObject("wrdNU"));
                                logL.setLogData_action(orderAction1, orderOItem1);
                                logL.getData().put("wn0prog", 2);

                                // 调用发送日志方法
                                ws.sendWS(logL);
                                // 把修改好的信息设置回去

//                                JSONObject mapKey = new JSONObject();
//                                mapKey.put("action.objAction." + nextIndex, orderAction1);
//                                coupaUtil.updateOrderByListKeyVal(nextId, mapKey);


                            } else if (orderAction1.getBmdpt() == 4) {

                                // Start myself, because I am a Process group and I don't need sumChild == 0 to start
                                orderAction1.setBcdStatus(0); //状态改为准备开始
                                orderAction1.setBisPush(1);

                                // Start making log with data
                                LogFlow logL = new LogFlow("action", actData.getString("id_FC"),
                                        actData.getString("id_FS"), "stateChg",
                                        tokData.getString("id_U"), tokData.getString("grpU"), orderOItem1.getId_P(),orderOItem1.getGrpB(), orderOItem1.getGrp(),
                                        orderAction1.getId_OP(), nextId, nextIndex, tokData.getString("id_C"), orderOItem1.getId_C(),
                                        "", tokData.getString("dep"), orderOItem1.getWrdN().get("cn") + "准备开始", 3, orderOItem1.getWrdN(), tokData.getJSONObject("wrdNU"));
                                logL.setLogData_action(orderAction1, orderOItem1);
                                logL.getData().put("wn0prog", 2);

                                // 调用发送日志方法
                                ws.sendWS(logL);


                                // bmdpt == 4 start subParts
                                for (int k = 0; k < orderAction1.getSubParts().size(); k++) {

                                    String subId = orderAction1.getSubParts().getJSONObject(k).getString("id_O");
                                    Integer subIndex = orderAction1.getSubParts().getJSONObject(k).getInteger("index");


                                    JSONObject subOrderData = this.getActionData(subId, subIndex);

                                    OrderOItem subOItem = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderOItem")), OrderOItem.class);
                                    OrderAction subAction = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderAction")), OrderAction.class);

                                    if (subAction.getPrtPrev().size() == 0) //it's 0 means he is the first one
                                    {
                                        subAction.setBcdStatus(0);
                                        subAction.setBisPush(1);

                                        JSONObject mapKey = new JSONObject();
                                        mapKey.put("action.objAction." + subIndex, subAction);
                                        coupaUtil.updateOrderByListKeyVal(subId, mapKey);

                                        LogFlow logLP = new LogFlow("action", subOrderData.getString("id_FC"),
                                                subOrderData.getString("id_FS"), "stateChg",
                                                tokData.getString("id_U"), tokData.getString("grpU"), subOItem.getId_P(), subOItem.getGrpB(), subOItem.getGrp(),
                                                subOItem.getId_OP(),
                                                subId,
                                                subIndex,
                                                tokData.getString("id_C"), subOItem.getId_C(),
                                                "", tokData.getString("dep"), "准备开始", 3, subOItem.getWrdN(), tokData.getJSONObject("wrdNU"));
                                        logLP.setLogData_action(subAction, subOItem);
                                        logLP.getData().put("wn0prog", 2);


                                        ws.sendWS(logLP);
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
         * @param orderAction	子产品递归信息
         * @return java.lang.String  返回结果: 结果
         * @author tang
         * @ver 1.0.0
         * @date 2020/8/6 9:21
         */
        private void updateParent(OrderAction orderAction,JSONObject tokData,String logType) {

            for (Integer i = 0; i < orderAction.getUpPrnts().size(); i++) {

                Integer indexPrnt = orderAction.getUpPrnts().getJSONObject(i).getInteger("index");
                String idPrnt = orderAction.getUpPrnts().getJSONObject(i).getString("id_O");
                JSONObject actData = this.getActionData(idPrnt, indexPrnt);
                System.out.println("prnt is "+ idPrnt + " " + indexPrnt);
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
                        LogFlow logL = new LogFlow("action", actData.getString("id_FC"),
                                actData.getString("id_FS"), "stateChg",
                                tokData.getString("id_U"), tokData.getString("grpU"), unitOItemPrnt.getId_P(), unitOItemPrnt.getGrpB(), unitOItemPrnt.getGrp(),
                                unitActionPrnt.getId_OP(), idPrnt, indexPrnt, tokData.getString("id_C"), unitOItemPrnt.getId_C(),
                                "", tokData.getString("dep"), "准备开始", 3, unitOItemPrnt.getWrdN(), tokData.getJSONObject("wrdNU"));

                        System.out.println(" sending log here"+ logL);

                        logL.setLogData_action(unitActionPrnt, unitOItemPrnt);

                        ws.sendWS(logL);
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
                        this.updateNext(orderAction, tokData, logType);
                    }
                }
            }
        }


        @Override
        public ApiResponse getRefOPList(String id_Flow, Boolean isSL, String id_C) throws IOException {

                Integer rows = 5000;
                String idF = "id";
                String idC = "id_C";
                //set to SL's search checking keys if the chatroom is for Sales
                if (isSL)
                {
                    idF = "id_FS";
                    idC = "id_CS";
                }
                // Type = 1: msgList regular with stateChg filter,
                // 2: progress with no filter but only those in id_O+index
                //构建搜索条件
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();

            //组合查询条件
                BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

                    boolQueryBuilder.must(QueryBuilders.matchQuery(idF , id_Flow));
                    boolQueryBuilder.must(QueryBuilders.termQuery(idC, id_C));
                    boolQueryBuilder.must(QueryBuilders.termQuery("data.bcdStatus", 0));

                BoolQueryBuilder boolQueryBuilder2 = new BoolQueryBuilder();

                boolQueryBuilder2.must(QueryBuilders.matchQuery(idF, id_Flow));
                boolQueryBuilder2.must(QueryBuilders.termQuery(idC, id_C));

                boolQueryBuilder2.must(QueryBuilders.queryStringQuery("2 OR 9").field("data.bcdStatus"));
                //构建查询库
                SearchRequest searchRequest = new SearchRequest();

                searchRequest.indices("action");

                //把查询条件放入构建搜索对象中
                searchSourceBuilder.query(boolQueryBuilder);
                //分页
                searchSourceBuilder.size(rows);
                // 获取排序的类型DESC还是ASC

                searchSourceBuilder.sort("tmd", SortOrder.DESC);

                //把构建对象放入，指定查那个对象，把查询条件放进去
                searchRequest.source(searchSourceBuilder);
                //执行请求
                SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                JSONArray result = new JSONArray();
                for (SearchHit hit : search.getHits().getHits()) {

                    result.add(hit.getSourceAsMap());
                }

                //------------
            //构建查询库
            SearchRequest searchRequest2 = new SearchRequest();
            searchRequest2.indices("action");

            //把查询条件放入构建搜索对象中
            searchSourceBuilder2.query(boolQueryBuilder2);
            //分页
            searchSourceBuilder2.size(rows);
            // 获取排序的类型DESC还是ASC
            searchSourceBuilder2.sort("tmd", SortOrder.DESC);
            //把构建对象放入，指定查那个对象，把查询条件放进去
            searchRequest2.source(searchSourceBuilder2);
            //执行请求
            SearchResponse search2 = restHighLevelClient.search(searchRequest2, RequestOptions.DEFAULT);
            JSONArray result2 = new JSONArray();
            for (SearchHit hit : search2.getHits().getHits()) {
                result2.add(hit.getSourceAsMap());
            }
            JSONObject refOPList = new JSONObject();
                // contentMap方法意义：比如lSProd 的id_P替换id，这样的意义，前端可以拿这个id去查Prod表

                for (int i = 0; i < result.size(); i++) {

                    String refOP = result.getJSONObject(i).getJSONObject("data").getString("refOP");
                    if (refOP != null && ! refOP.equals("") && refOPList.getInteger(refOP) == null)
                    {
                        refOPList.put(refOP, 1);
                    } else if (refOP != null && ! refOP.equals(""))
                    {
                        refOPList.put(refOP, refOPList.getInteger(refOP) + 1);
                    }
                }
                for (int i = 0; i < result2.size(); i++) {
                    System.out.println("result222"+result2.getJSONObject(i).getJSONObject("data").getString("refOP"));

                    String refOP = result2.getJSONObject(i).getJSONObject("data").getString("refOP");
                    if (refOP != null && ! refOP.equals("") && refOPList.getInteger(refOP) != null)
                    {
                        refOPList.put(refOP, refOPList.getInteger(refOP) - 1);
                        if (refOPList.getInteger(refOP).equals(0))
                        {
                            refOPList.remove(refOP);
                        }
                    }
                }
                System.out.println(refOPList);

                return retResult.ok(CodeEnum.OK.getCode(), refOPList);

            }
            //遍历获取最新数据
//            for (SearchHit hit : hits) {
////            for (SearchHit hit : searchStart.getHits().getHits()) {
//                JSONObject jsonHit = JSON.parseObject(hit.getSourceAsString());
//                System.out.println(jsonHit);
//
//                orderList.add( JSONObject.parseObject(JSON.toJSONString(hit.getSourceAsMap())));
//            }
//
//            JSONObject refOPList = new JSONObject();
//            System.out.println("what is in"+ orderList);
//
//            for (int i = 0; i < orderList.size(); i++)
//            {
//                String refOP = orderList.getJSONObject(i).getJSONObject("data").getString("refOP");
//                if (refOPList.getInteger(refOP).equals(null))
//                {
//                    refOPList.put(refOP, 1);
//                } else
//                {
//                    refOPList.put(refOP, refOPList.getInteger(refOP) + 1);
//                }
//            }
//
//            System.out.println("what is in"+ refOPList);

            // getfrom lBOrder, filter lST = making + id_C = myself
            // getfrom assetflow where mixeq [refOP] contains [refOP above]

            // return refOPList

//            return retResult.ok(CodeEnum.OK.getCode(), refOPList);

//        }

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

//                JSONObject mapKey2 = new JSONObject();
//                mapKey2.put("flowControl.objData."+index+".grpB", grpB);
//                coupaUtil.updateOrderByListKeyVal(id_A, mapKey2);
            } catch (RuntimeException e) {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_AN_ERROR_OCCURRED.getCode(), "不能开始,"+e);
            }


            return retResult.ok(CodeEnum.OK.getCode(), grpData);

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
     * @ver 1.0.0
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
                            "",id_O,unitAction.getIndex(), myCompId,unitOItem.getId_C(),
                        "",dep,"准备开始",3,unitOItem.getWrdN(),wrdNU);
                logLP.setLogData_action(unitAction,unitOItem);

                ws.sendWS(logLP);

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

            try {
                for (Integer j = 0; j < objAction.size(); j++) {
                    OrderAction unitAction = JSONObject.parseObject(JSON.toJSONString(objAction.getJSONObject(j)), OrderAction.class);
                    OrderOItem unitOItem = JSONObject.parseObject(JSON.toJSONString(objOItem.getJSONObject(j)), OrderOItem.class);
                    if (unitAction.getBisPush() != 1) {

                        // if bmdpt == 4, && sumPrev == 0, my subParts [prior == 0] starts
                        // for each subParts
                        // check if wn0prior == 0, if so dgActivate
                        if (unitAction.getBmdpt() == 4 && unitAction.getSumPrev() == 0) {
                            for (int k = 0; k < unitAction.getSubParts().size(); k++) {
                                JSONObject subOrderData = this.getActionData(unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                        unitAction.getSubParts().getJSONObject(k).getInteger("index"));

                                OrderOItem subOItem = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderOItem")), OrderOItem.class);
                                OrderAction subAction = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderAction")), OrderAction.class);

                                if (subAction.getPriority() == 1) {
                                    subAction.setBcdStatus(0);
                                    subAction.setBisPush(1);

                                    JSONObject mapKey = new JSONObject();
                                    mapKey.put("action.objAction." + unitAction.getSubParts().getJSONObject(k).getInteger("index"), subAction);
                                    coupaUtil.updateOrderByListKeyVal(unitAction.getSubParts().getJSONObject(k).getString("id_O"), mapKey);

                                    System.out.println("unit " + subOItem.getGrpB() + subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()));
                                    String logType = subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()).getString("logType");

                                    LogFlow logLP = new LogFlow(logType, subOrderData.getString("id_FC"),
                                            subOrderData.getString("id_FS"), "stateChg",
                                            id_U, grpU, subOItem.getId_P(), subOItem.getGrpB(), subOItem.getGrp(),
                                            id_O, unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                            unitAction.getSubParts().getJSONObject(k).getInteger("index"),
                                            myCompId, subOItem.getId_C(),
                                            "", dep, "准备开始", 3, subOItem.getWrdN(), wrdNU);
                                    logLP.setLogData_action(subAction, subOItem);

                                    ws.sendWS(logLP);
                                } else {
                                    System.out.println("break @ " + k);
                                    break;
                                }
                            }
                        } else if (unitAction.getBmdpt() == 3 && unitAction.getSumPrev() == 0) {
                            //                // 根据零件递归信息获取零件信息，并且制作日志
                            unitAction.setBcdStatus(0);
                            unitAction.setBisPush(1);

                            JSONObject mapKey = new JSONObject();
                            mapKey.put("action.objAction." + j, unitAction);
                            coupaUtil.updateOrderByListKeyVal(orderList.getJSONObject(i).getString("id_O"), mapKey);

                            JSONObject fcCheck = grpBGroup.getJSONObject(unitOItem.getGrpB());
                            JSONObject fsCheck = grpGroup.getJSONObject(unitOItem.getGrp());
                            String id_FS = "";
                            String id_FC = "";

                            if (fcCheck != null) {
                                id_FC = fcCheck.getString("id_Flow") != null
                                        && !unitOItem.getId_C().equals(myCompId) ?
                                        fcCheck.getString("id_Flow") : "";
                            }

                            if (fsCheck != null) {
                                id_FS = fsCheck.getString("id_Flow") != null
                                        && !unitOItem.getId_C().equals(myCompId) ?
                                        fsCheck.getString("id_Flow") : "";
                            }

                            LogFlow logLP = new LogFlow(fcCheck.getString("logType"),
                                    id_FC,
                                    id_FS, "stateChg", id_U, grpU,
                                    unitOItem.getId_P(), unitOItem.getGrpB(), unitOItem.getGrp(), id_O, unitAction.getId_O(), unitAction.getIndex(),
                                    myCompId, unitOItem.getId_C(), "", dep, "准备开始", 3, unitOItem.getWrdN(), wrdNU);
                            logLP.setLogData_action(unitAction, unitOItem);

                            System.out.println(logLP);
                            ws.sendWS(logLP);
                            // 发送日志
                        }
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "doneAll");
    }



    /**
     * Create task OItem + Action + Log
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @ver 1.0.0
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
                id_FS,"stateChg", id_U,grpU,"",unitOItem.getGrpB(), "","",id_O,index, myCompId,myCompId,
                oItemData.getString("pic"),dep,"准备开始",3,oItemData.getJSONObject("wrdN"),wrdNU);

        logLP.setLogData_action(unitAction,unitOItem);
        // append an OItem + ActionItem, save OItem and action
        JSONObject mapKey = new JSONObject();
        mapKey.put("action.objAction."+index,unitAction);
        mapKey.put("oItem.objItem."+index,unitOItem);
        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);

        try {
            ws.sendWS(logLP);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
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
                id_FQ,"stateChg", id_U,grpU,orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),id_O, id_Prob,indexProb, myCompId,myCompId,
                probData.getString("pic"),dep,"准备解决",3,probData.getJSONObject("wrdN"),wrdNU);

               logProb.setLogData_action(unitAction,unitOItem);
//        logProb.setActionData(unitAction.getBisactivate(),unitAction.getBcdStatus(),unitAction.getId_Us(),1.0,
//                orderOItem.getId_P(), id_O,index,1,unitAction.getWrdNP(),unitOItem.getWrdN());

        JSONObject probResult = new JSONObject();
        probResult.put("action.objAction."+indexProb,unitAction);
        probResult.put("oItem.objItem."+indexProb,unitOItem);

        coupaUtil.updateOrderByListKeyVal(id_Prob,probResult);

        ws.sendWS(logProb);

        return retResult.ok(CodeEnum.OK.getCode(), "done");

    }

    @Override
    public ApiResponse subStatusChange(String id_O, Integer index, Boolean isLink, Integer statusType, JSONObject tokData) throws IOException {

//        //1. Save the new
//        JSONObject mapKey = new JSONObject();
//        mapKey.put("action.grpBGroup."+grpB,grpBNew);
//        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
        //2. get Order's oItem+action
        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("action"));
//        JSONArray objItem = order.getOItem().getJSONArray("objItem");
        JSONObject objAction = order.getAction().getJSONArray("objAction").getJSONObject(index);
        //3. Loop check grpB==oItem(i).grpB and objAction(i).isPUshed == 1
        for (int i = 0; i < objAction.getJSONArray("subParts").size(); i++) {

            String subPartId_O = objAction.getJSONArray("subParts").getJSONObject(i).getString("id_O");
            Integer subPartIndex = objAction.getJSONArray("subParts").getJSONObject(i).getInteger("index");

            JSONObject subOrderData = this.getActionData(subPartId_O, subPartIndex);

            OrderOItem subOItem = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderOItem")), OrderOItem.class);
            OrderAction subAction = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderAction")), OrderAction.class);

            Integer subStatus = subAction.getBcdStatus();
            Integer newStatus = 0;
            String newMsg = "";


            if (statusType.equals(0))
            {
                if (subStatus.equals(0) || subStatus.equals(1) || subStatus.equals(3) || subStatus.equals(7))
                {
                    newStatus = 8;
                    newMsg = "全单暂停";
                }
            } else if (statusType.equals(1))
            {
                if (subStatus.equals(0))
                {
                    newStatus = 1;
                    newMsg = "全单开始";
                } else if (subStatus.equals(8))
                {
                    newStatus = 3;
                    newMsg = "全单再开";
                }
            } else if (statusType.equals(2))
            {
                if (subStatus.equals(1) || subStatus.equals(3) || subStatus.equals(7) || subStatus.equals(8))
                {
                    this.changeActionStatus("action", 2, "全单完成", subPartIndex, subPartId_O, isLink,
                            subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), tokData);
                }
            }

            if (!newStatus.equals(0))
            {
                //newStatus need to update mdb and send a log

                    JSONObject mapKey = new JSONObject();
                    subAction.setBcdStatus(newStatus);

                    mapKey.put("action.objAction." + subPartIndex +".bcdStatus", newStatus);
                    coupaUtil.updateOrderByListKeyVal(subPartId_O, mapKey);

                    LogFlow logLP = new LogFlow("action", subOrderData.getString("id_FC"),
                            subOrderData.getString("id_FS"), "stateChg",
                            tokData.getString("id_U"), tokData.getString("grpU"), subOItem.getId_P(),subOItem.getGrpB(),subOItem.getGrp(),
                            subAction.getId_OP(),subPartId_O, subPartIndex,
                            tokData.getString("id_C"), subOItem.getId_C(),
                            "", tokData.getString("dep"), newMsg, 3, subOItem.getWrdN(), tokData.getJSONObject("wrdNU"));
                    logLP.setLogData_action(subAction, subOItem);

                    ws.sendWS(logLP);

            }
        }

        return retResult.ok(CodeEnum.OK.getCode(), "Status batch changed");

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
     * @param id_C	公司编号
     * @param id_O	订单编号
     * @return java.lang.String  返回结果: 结果
     * @author Kevin
     * @ver 1.0.0
     * @date 2021/6/16 14:49
     */
    @Override
    public ApiResponse confirmOrder(String id_C,String id_O) {

        Order order = qt.getMDContent(id_O, "info",Order.class);
//        QueryBuilder queryBuilder;

        // if I am both buyer and seller, internal order, whatever side I am, set it to both final
        if (order.getInfo().getId_C().equals(id_C) && order.getInfo().getId_C().equals(order.getInfo().getId_CB())) { //Check C== CB
            order.getInfo().setLST(7);
//            queryBuilder = QueryBuilders.boolQuery()
//                    .must(QueryBuilders.termQuery("id_O", id_O))
//                    .must(QueryBuilders.termQuery("id_CB", id_C));
        } // I am id_CB Buyer
        else if (id_C.equals(order.getInfo().getId_CB())) { //if Seller is REAL
            if (dbUtils.judgeComp(id_C, order.getInfo().getId_C()) == 1) {
                if (order.getInfo().getLST() == 6) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(7);
                } else {
                    order.getInfo().setLST(5);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(7);
            }
//            queryBuilder = QueryBuilders.boolQuery()
//                    .must(QueryBuilders.termQuery("id_O", id_O))
//                    .must(QueryBuilders.termQuery("id_CB", id_C));
        } else // I am Seller
        { //if Buyer is REAL
            if (dbUtils.judgeComp(id_C, order.getInfo().getId_CB()) == 1) {
                if (order.getInfo().getLST() == 5) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(7);
                } else {
                    order.getInfo().setLST(6);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(7);
            }
//            queryBuilder = QueryBuilders.boolQuery()
//                    .must(QueryBuilders.termQuery("id_O", id_O))
//                    .must(QueryBuilders.termQuery("id_C", id_C));
        }
//        JSONObject mapKey = new JSONObject();
//        mapKey.put("info.lST", order.getInfo().getLST());
//        coupaUtil.updateOrderByListKeyVal(id_O, mapKey);

        qt.setMDContent(id_O, qt.setJson("info.lST", order.getInfo().getLST()), Order.class);

        //Save MDB above, then update ES here
//        JSONObject listCol = new JSONObject();
//        listCol.put("lST", order.getInfo().getLST());
//        dbUtils.updateListCol(queryBuilder, "lsborder", listCol);

        qt.setES("lsborder", qt.setESFilt("id_O", id_O), qt.setJson("lST", order.getInfo().getLST()));

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
            if (dbUtils.judgeComp(id_C, order.getInfo().getId_C()) == 1) {
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
            if (dbUtils.judgeComp(id_C, order.getInfo().getId_CB()) == 1) {
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

        // loop casItemx orders
        JSONObject result = new JSONObject();

        for (int i = 0; i < casList.size(); i++) {
            // check if orders are final, if need request Cancel, send request
            // if ok, open order
            result.put("final",new JSONArray());
            result.put("await", new JSONArray());
            String subOrderId = casList.getJSONObject(i).getString("id_O");
            //confirm it by confirmOrder function
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
        String assetId = dbUtils.getId_A(id_C, "a-auth");
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


    @Override
    public ApiResponse actionChart(String id_O) {
        HashSet setId_O = new HashSet();
        JSONObject jsonId_O = new JSONObject();
        JSONObject jsonUpId_OIndex = new JSONObject();
        JSONObject jsonAction = new JSONObject();
        JSONObject jsonExcludeId_OIndex = new JSONObject();
        JSONObject jsonObjStock = new JSONObject();


        Order order = (Order) dbUtils.getMongoOneFields(id_O, Arrays.asList("action","oItem","oStock"), Order.class);
        JSONArray arrayObjAction = order.getAction().getJSONArray("objAction");
        Integer num = 0;
        for (int a = 0; a < arrayObjAction.size(); a++) {
            JSONObject jsonObjAction = order.getAction().getJSONArray("objAction").getJSONObject(a);
            JSONObject jsonObjItem = order.getOItem().getJSONArray("objItem").getJSONObject(a);

            if (order.getOStock().getJSONArray("objData").size() <= a)
            {
                jsonObjStock.put("wn2qtynow", 0.0);
                jsonObjStock.put("wn2qtymade", 0.0);
            } else {
                jsonObjStock = order.getOStock().getJSONArray("objData").getJSONObject(a);
            }

            JSONArray arrayUpPrnt = jsonObjAction.getJSONArray("upPrnts");
            JSONArray arraySubPart = jsonObjAction.getJSONArray("subParts");
            for (int i = 0; i < arrayUpPrnt.size(); i++) {
                JSONObject jsonUpPrnt = arrayUpPrnt.getJSONObject(i);
                String upPrntId_O = jsonUpPrnt.getString("id_O");
                Integer upPrntIndex = jsonUpPrnt.getInteger("index");
                setId_O.add(upPrntId_O);
                if (jsonId_O.getJSONArray(upPrntId_O) != null) {
                    JSONArray arrayIndex = jsonId_O.getJSONArray(upPrntId_O);
                    arrayIndex.add(upPrntIndex);
                } else {
                    JSONArray arrayIndex = new JSONArray();
                    arrayIndex.add(upPrntIndex);
                    jsonId_O.put(upPrntId_O, arrayIndex);
                }

                if (jsonUpId_OIndex.getJSONArray(upPrntId_O + "." + upPrntIndex) != null) {
                    JSONArray arraySubId_OIndex = jsonUpId_OIndex.getJSONArray(upPrntId_O + "." + upPrntIndex);
                    arraySubId_OIndex.add(id_O + "." + a);
                } else {
                    JSONArray arraySubId_OIndex = new JSONArray();
                    arraySubId_OIndex.add(id_O + "." + a);
                    jsonUpId_OIndex.put(upPrntId_O + "." + upPrntIndex, arraySubId_OIndex);
                }
            }
            for (int i = 0; i < arraySubPart.size(); i++) {
                JSONObject jsonSubPart = arraySubPart.getJSONObject(i);
                String subPartId_O = jsonSubPart.getString("id_O");
                Integer subPartIndex = jsonSubPart.getInteger("index");
                setId_O.add(subPartId_O);
                if (jsonId_O.getJSONArray(subPartId_O) != null) {
                    JSONArray arrayIndex = jsonId_O.getJSONArray(subPartId_O);
                    arrayIndex.add(subPartIndex);
                } else {
                    JSONArray arrayIndex = new JSONArray();
                    arrayIndex.add(subPartIndex);
                    jsonId_O.put(subPartId_O, arrayIndex);
                }

                if (jsonUpId_OIndex.getJSONArray(id_O + "." + a) != null) {
                    JSONArray arraySubId_OIndex = jsonUpId_OIndex.getJSONArray(id_O + "." + a);
                    arraySubId_OIndex.add(subPartId_O + "." + subPartIndex);
                } else {
                    JSONArray arraySubId_OIndex = new JSONArray();
                    arraySubId_OIndex.add(subPartId_O + "." + subPartIndex);
                    jsonUpId_OIndex.put(id_O + "." + a, arraySubId_OIndex);
                }
            }
            jsonObjAction.put("name", "[" + num + "] " + jsonObjAction.getJSONObject("wrdN").getString("cn"));
            jsonObjAction.put("wn2qtyneed", jsonObjItem.getDouble("wn2qtyneed"));
            jsonObjAction.put("wn4price", jsonObjItem.getDouble("wn4price"));
            jsonObjAction.put("wn2qtynow", jsonObjStock.getDouble("wn2qtynow"));
            jsonObjAction.put("wn2qtymade", jsonObjStock.getDouble("wn2qtymade"));
            num++;
            jsonAction.put(id_O + "." + a, jsonObjAction);
            jsonExcludeId_OIndex.put(id_O + "." + a, "");
//            System.out.println("setId_O=" + setId_O);
//            System.out.println("jsonId_O=" + jsonId_O);
//            System.out.println("jsonAction=" + jsonAction);
//            System.out.println("jsonUpId_OIndex=" + jsonUpId_OIndex);
//            System.out.println("jsonExcludeId_OIndex=" + jsonExcludeId_OIndex);
        }
        recursionActionChart(setId_O, jsonId_O, jsonAction, jsonUpId_OIndex, jsonExcludeId_OIndex, num);
        System.out.println("setId_O=" + setId_O);
        System.out.println("jsonId_O=" + jsonId_O);
        System.out.println("jsonAction=" + jsonAction);
        System.out.println("jsonUpId_OIndex=" + jsonUpId_OIndex);
        System.out.println("jsonExcludeId_O=" + jsonExcludeId_OIndex);
        JSONArray arrayRelation = new JSONArray();
        jsonUpId_OIndex.forEach((upId_OIndex, v) ->{
            HashSet<String> setSubId_OIndex = JSON.parseObject(JSON.toJSONString(jsonUpId_OIndex.getJSONArray(upId_OIndex)), HashSet.class);
            for (String subId_OIndex : setSubId_OIndex) {
                JSONObject jsonRelation = new JSONObject();
                JSONObject jsonUp = jsonAction.getJSONObject(upId_OIndex);
                JSONObject jsonSub = jsonAction.getJSONObject(subId_OIndex);
                jsonRelation.put("source", jsonUp.getString("name"));
                jsonRelation.put("target", jsonSub.getString("name"));
                jsonRelation.put("value", jsonSub.getDouble("wn2qtyneed"));

                jsonRelation.put("upBcdStatus", jsonUp.getInteger("bcdStatus"));
                jsonRelation.put("upWn2qtyneed", jsonUp.getDouble("wn2qtyneed"));
                jsonRelation.put("upWn4price", jsonUp.getDouble("wn4price"));
                jsonRelation.put("upWn2qtynow", jsonUp.getDouble("wn2qtynow"));
                jsonRelation.put("upWn2qtymade", jsonUp.getDouble("wn2qtymade"));

                jsonRelation.put("subBcdStatus", jsonSub.getInteger("bcdStatus"));
                jsonRelation.put("subWn2qtyneed", jsonSub.getDouble("wn2qtyneed"));
                jsonRelation.put("subWn4price", jsonSub.getDouble("wn4price"));
                jsonRelation.put("subWn2qtynow", jsonSub.getDouble("wn2qtynow"));
                jsonRelation.put("subWn2qtymade", jsonSub.getDouble("wn2qtymade"));

                arrayRelation.add(jsonRelation);
            }
        });

        JSONArray arrayAction = new JSONArray();
        jsonAction.forEach((id_OIndex, v) ->{
            arrayAction.add(jsonAction.getJSONObject(id_OIndex));
//            JSONObject jsonName = new JSONObject();
//            jsonName.put("name", jsonAction.getJSONObject(id_OIndex).getString("name"));
//            arrayAction.add(jsonName);
        });
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("action", arrayAction);
        jsonResult.put("relation", arrayRelation);
        System.out.println("jsonResult=" + jsonResult);
        return retResult.ok(CodeEnum.OK.getCode(), jsonResult);

    }

    private void recursionActionChart(HashSet setId_O, JSONObject jsonUpId_O, JSONObject jsonAction, JSONObject jsonUpId_OIndex, JSONObject jsonExcludeId_OIndex, Integer num) {

        List<Order> orders = (List<Order>) dbUtils.getMongoListFields(setId_O, Arrays.asList("action","oItem","oStock"), Order.class);
        setId_O = new HashSet();
        JSONObject jsonId_O = new JSONObject();
        for (Order order : orders) {
            String id_O = order.getId();
            JSONArray arrayObjAction = order.getAction().getJSONArray("objAction");
            JSONArray arrayObjItem = order.getOItem().getJSONArray("objItem");
            JSONArray arrayObjStock = order.getOStock().getJSONArray("objData");
            HashSet<Integer> setIndex = JSON.parseObject(JSON.toJSONString(jsonUpId_O.getJSONArray(id_O)), HashSet.class);
            for (Integer index : setIndex) {
                if (jsonExcludeId_OIndex.getString(id_O + "." + index) == null) {
                    jsonExcludeId_OIndex.put(id_O + "." + index, "");
                    JSONObject jsonObjAction = arrayObjAction.getJSONObject(index);
                    JSONObject jsonObjItem = arrayObjItem.getJSONObject(index);
                    JSONObject jsonObjStock = arrayObjStock.getJSONObject(index);

                    JSONArray arrayUpPrnt = jsonObjAction.getJSONArray("upPrnts");
                    JSONArray arraySubPart = jsonObjAction.getJSONArray("subParts");
                    for (int i = 0; i < arrayUpPrnt.size(); i++) {
                        JSONObject jsonUpPrnt = arrayUpPrnt.getJSONObject(i);
                        String upPrntId_O = jsonUpPrnt.getString("id_O");
                        Integer upPrntIndex = jsonUpPrnt.getInteger("index");
                        setId_O.add(upPrntId_O);
                        if (jsonId_O.getJSONArray(upPrntId_O) != null) {
                            JSONArray arrayIndex = jsonId_O.getJSONArray(upPrntId_O);
                            arrayIndex.add(upPrntIndex);
                        } else {
                            JSONArray arrayIndex = new JSONArray();
                            arrayIndex.add(upPrntIndex);
                            jsonId_O.put(upPrntId_O, arrayIndex);
                        }

                        if (jsonUpId_OIndex.getJSONArray(upPrntId_O + "." + upPrntIndex) != null) {
                            JSONArray arraySubId_OIndex = jsonUpId_OIndex.getJSONArray(upPrntId_O + "." + upPrntIndex);
                            arraySubId_OIndex.add(id_O + "." + index);
                        } else {
                            JSONArray arraySubId_OIndex = new JSONArray();
                            arraySubId_OIndex.add(id_O + "." + index);
                            jsonUpId_OIndex.put(upPrntId_O + "." + upPrntIndex, arraySubId_OIndex);
                        }
                    }
                    for (int i = 0; i < arraySubPart.size(); i++) {
                        JSONObject jsonSubPart = arraySubPart.getJSONObject(i);
                        String subPartId_O = jsonSubPart.getString("id_O");
                        Integer subPartIndex = jsonSubPart.getInteger("index");
                        setId_O.add(subPartId_O);
                        if (jsonId_O.getJSONArray(subPartId_O) != null) {
                            JSONArray arrayIndex = jsonId_O.getJSONArray(subPartId_O);
                            arrayIndex.add(subPartIndex);
                        } else {
                            JSONArray arrayIndex = new JSONArray();
                            arrayIndex.add(subPartIndex);
                            jsonId_O.put(subPartId_O, arrayIndex);
                        }

                        if (jsonUpId_OIndex.getJSONArray(id_O + "." + index) != null) {
                            JSONArray arraySubId_OIndex = jsonUpId_OIndex.getJSONArray(id_O + "." + index);
                            arraySubId_OIndex.add(subPartId_O + "." + subPartIndex);
                        } else {
                            JSONArray arraySubId_OIndex = new JSONArray();
                            arraySubId_OIndex.add(subPartId_O + "." + subPartIndex);
                            jsonUpId_OIndex.put(id_O + "." + index, arraySubId_OIndex);
                        }
                    }
                    jsonObjAction.put("name", "[" + num + "] " + jsonObjAction.getJSONObject("wrdN").getString("cn"));
                    jsonObjAction.put("wn2qtyneed", jsonObjItem.getDouble("wn2qtyneed"));
                    jsonObjAction.put("wn4price", jsonObjItem.getDouble("wn4price"));
                    jsonObjAction.put("wn2qtynow", jsonObjStock.getDouble("wn2qtynow"));
                    jsonObjAction.put("wn2qtymade", jsonObjStock.getDouble("wn2qtymade"));
                    num++;
                    jsonAction.put(id_O + "." + index, jsonObjAction);
                }
            }
        }
        if (setId_O.size() > 0) {
            recursionActionChart(setId_O, jsonId_O, jsonAction, jsonUpId_OIndex, jsonExcludeId_OIndex, num);
        }
    }


}
