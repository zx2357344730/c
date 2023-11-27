package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.common.ChatEnum;
import com.cresign.purchase.service.ActionService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
public class ActionServiceImpl implements ActionService {

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


    /**
     * getActionData **** 这个很重要，拿了action / oItem 的内容都要转换
     * changeActionStatus -> updateNext -> updateParent
     * createQuest / createTask / dgConfirmOrder
     * getFlowList / changeDepAndFlow / dgActivateAll
     */

        @Autowired
        private RetResult retResult;

        @Autowired
        private RestHighLevelClient restHighLevelClient;

        @Autowired
        private Ws ws;

        @Autowired
        private Qt qt;

        @Autowired
        private DbUtils dbu;

    /**
     * 根据oId修改grpBGroup字段
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @date 2021/9/10 17:32
     */
    @Override
    public ApiResponse changeDepAndFlow(String id_O, String grpB, JSONObject grpBOld,JSONObject grpBNew
            ,String id_C,String id_U,String grpU, JSONObject wrdNU) {

//        //1. Save the new
        qt.setMDContent(id_O, qt.setJson("action.grpBGroup."+grpB, grpBNew), Order.class);
        //2. get Order's oItem+action
        Order order = qt.getMDContent(id_O,qt.strList("action", "oItem","info"), Order.class);
        JSONArray objItem = order.getOItem().getJSONArray("objItem");
        JSONArray objAction = order.getAction().getJSONArray("objAction");
        //3. Loop check grpB==oItem(i).grpB and objAction(i).isPUshed == 1
        for (int i = 0; i < objItem.size(); i++) {
            if (grpB.equals(objItem.getJSONObject(i).getString("grpB")) &&
            objAction.getJSONObject(i).getInteger("bisPush") == 1 &&
            objAction.getJSONObject(i).getInteger("bcdStatus") != 2){
                //System.out.println("i am here in "+ grpB+ " "+ i);

                OrderAction oAction = qt.jsonTo(objAction.getJSONObject(i),OrderAction.class);
                OrderOItem oItem = qt.jsonTo(objItem.getJSONObject(i), OrderOItem.class);

                //4. Send Log to stop old Flow
                LogFlow logStop = new LogFlow("action", // grpBOld.getString("logType"),
                        grpBOld.getString("id_Flow"),"","stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),grpBOld.getString("grpB"), "",
                        objAction.getJSONObject(i).getString("id_OP"),
                        objItem.getJSONObject(i).getString("id_O"),
                        i,order.getInfo().getId_CB(),order.getInfo().getId_C(),objItem.getJSONObject(i).getString("pic"),grpBOld.getString("dep"),
                        "转换另一队伍执行",3,objItem.getJSONObject(i).getJSONObject("wrdN"), wrdNU);
                logStop.setLogData_action(oAction, oItem);
                logStop.getData().put("bcdStatus", 9);

                LogFlow logStart = new LogFlow("action", //grpBOld.getString("logType"),
                        grpBNew.getString("id_Flow"),"","stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),grpBNew.getString("grpB"), "",
                        objAction.getJSONObject(i).getString("id_OP"),
                        objItem.getJSONObject(i).getString("id_O"),
                        i,order.getInfo().getId_CB(),order.getInfo().getId_C(),objItem.getJSONObject(i).getString("pic"),grpBNew.getString("dep"),
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
        qt.setMDContent(id_O,qt.setJson("action.grpGroup."+grpB,grpBNew), Order.class);
        //2. get Order's oItem+action
        Order order = qt.getMDContent(id_O, Arrays.asList("info","oItem", "action"), Order.class);
        JSONArray objItem = order.getOItem().getJSONArray("objItem");
        JSONArray objAction = order.getAction().getJSONArray("objAction");
        //3. Loop check grpB==oItem(i).grpB and objAction(i).isPUshed == 1
        for (int i = 0; i < objItem.size(); i++) {
            if (grpB.equals(objItem.getJSONObject(i).getString("grpB")) &&
                    objAction.getJSONObject(i).getInteger("bisPush") == 1 &&
                    objAction.getJSONObject(i).getInteger("bcdStatus") != 2){

                OrderAction oAction = qt.jsonTo(objAction.getJSONObject(i),OrderAction.class);
                OrderOItem oItem = qt.jsonTo(objItem.getJSONObject(i), OrderOItem.class);

                //4. Send Log to stop old Flow
                LogFlow logStop = new LogFlow("action","",grpBOld.getString("id_Flow"),"stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),"", grpBOld.getString("grpB"),
                        objAction.getJSONObject(i).getString("id_OP"),
                        objItem.getJSONObject(i).getString("id_O"),
                        i,order.getInfo().getId_CB(),order.getInfo().getId_C(),objItem.getJSONObject(i).getString("pic"),grpBOld.getString("dep"),
                        "转换另一队伍执行",3,objItem.getJSONObject(i).getJSONObject("wrdN"), wrdNU);
                logStop.setLogData_action(oAction, oItem);
                logStop.getData().put("bcdStatus", 9);

                LogFlow logStart = new LogFlow("action","",grpBNew.getString("id_Flow"),"stateChg",
                        id_U, grpU, objItem.getJSONObject(i).getString("id_P"),"", grpBNew.getString("grpB"),
                        objAction.getJSONObject(i).getString("id_OP"),
                        objItem.getJSONObject(i).getString("id_O"),
                        i,order.getInfo().getId_CB(),order.getInfo().getId_C(),objItem.getJSONObject(i).getString("pic"),grpBNew.getString("dep"),
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
    public ApiResponse rePush(String id_O, Integer index, JSONObject tokData) {

        JSONObject actData = this.getActionData(id_O, index);

        OrderOItem orderOItem = qt.jsonTo(actData.get("orderOItem"), OrderOItem.class);
        OrderAction orderAction = qt.jsonTo(actData.get("orderAction"), OrderAction.class);

        String taskName = orderOItem.getWrdN().getString("cn");
        Integer bcdStatus = orderAction.getBcdStatus();

        if (bcdStatus != 100 && bcdStatus != 2 && bcdStatus != 9)
        {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "这任务没有关闭");
        }

        // 备注
        String message = taskName + "[已恢复执行]";
        JSONObject res = new JSONObject();
        JSONObject listCol = new JSONObject();

        listCol.put("lST", 8);
        // 设置产品状态
//        orderAction.setBcdStatus(bcdStatus);
        orderAction.setBcdStatus(0);

        // Start making log with data
        LogFlow logL = new LogFlow("action",actData.getString("id_FC"),
                actData.getString("id_FS"),"stateChg", tokData.getString("id_U"),tokData.getString("grpU"), orderOItem.getId_P(), orderOItem.getGrpB(),orderOItem.getGrp(),
                orderAction.getId_OP(), id_O,index,orderOItem.getId_CB(),orderOItem.getId_C(), "",tokData.getString("dep"),message,3,orderOItem.getWrdN(),tokData.getJSONObject("wrdNU"));

        logL.setLogData_action(orderAction,orderOItem);
        logL.getData().put("bcdStatus", -1 * bcdStatus);
        logL.getData().put("wn0prog", 0);

            ws.sendWS(logL);

            qt.setMDContent(id_O, qt.setJson("info.lST", 8, "action.objAction."+index,orderAction), Order.class);

            if(null != listCol.getInteger("lST")) {

                qt.setES("lSBOrder", qt.setESFilt("id_O", id_O), listCol);
            }
            return retResult.ok(CodeEnum.OK.getCode(), res);
    }

    /**
     * 客服向顾客申请评分api
     * @param id_O	订单编号
     * @param index	订单任务对应下标
     * @param id	内部群编号
     * @param id_Fs	对外群编号
     * @param tokData	当前登录信息
     * @param id_Us	指定发送用户列表
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse applyForScore(String id_O, Integer index
            ,String id,String id_Fs, JSONObject tokData,JSONArray id_Us) {
        // 定义存储返回结果
        JSONObject result = new JSONObject();
        // 获取用户ID
        String id_U = tokData.getString("id_U");
        // 获取公司ID
        String id_C = tokData.getString("id_C");
        try {
            // 获取进度信息
            JSONObject actData = this.getActionData(id_O, index);
            // 获取当前oItem信息
            OrderOItem orderOItem = qt.jsonTo(actData.get("orderOItem"), OrderOItem.class);
            // 获取当前action信息
            OrderAction orderAction = qt.jsonTo(actData.get("orderAction"), OrderAction.class);
            // 添加返回结果
            result.put("desc","申请评分成功!");
            result.put("type",1);
            // 创建日志信息
            LogFlow logFlow = new LogFlow("oQc", id,
                    id_Fs, "scoreAf", id_U, tokData.getString("grpU")
                    , orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
                    orderAction.getId_OP(), id_O, index, orderOItem.getId_CB(), orderOItem.getId_C(), ""
                    , tokData.getString("dep"), "申请评分成功!", 3, orderOItem.getWrdN()
                    , tokData.getJSONObject("wrdNU"));
            // 设置发送对象
            logFlow.setId_Us(id_Us);
            // 发送websocket
            ws.sendWS(logFlow);
            return retResult.ok(CodeEnum.OK.getCode(), result);
        } catch (Exception e) {
            result.put("desc","申请评分异常");
            result.put("type",0);
            sendMsgNotice(id_C,"申请评分异常!",id_U,id_O,qt.setArray(id_U),index,"scoreAf",id,id);
            return retResult.ok(CodeEnum.OK.getCode(), result);
        }
    }

    /**
     * 顾客评分api
     * @param id_O	订单编号
     * @param index	订单任务对应下标
     * @param score	评分分数
     * @param id	内部群编号
     * @param id_Fs	对外群编号
     * @param tokData	当前登录信息
     * @param id_Us	指定发送用户列表
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse haveScore(String id_O, Integer index, Integer score
            ,String id,String id_Fs, JSONObject tokData,JSONArray id_Us) {
        // 定义存储返回结果
        JSONObject result = new JSONObject();
        // 获取用户ID
        String id_U = tokData.getString("id_U");
        // 获取公司ID
        String id_C = tokData.getString("id_C");

        try {
            // 获取进度信息
            JSONObject actData = this.getActionData(id_O, index);
            // 获取当前oItem信息
            OrderOItem orderOItem = qt.jsonTo(actData.get("orderOItem"), OrderOItem.class);
            // 获取当前action信息
            OrderAction orderAction = qt.jsonTo(actData.get("orderAction"), OrderAction.class);
            // 更新oQc卡片分数
            qt.setMDContent(id_O, qt.setJson("oQc.objQc."+index+".score",score), Order.class);
            // 添加返回结果
            result.put("desc","评分成功!");
            result.put("type",1);
            // 创建日志信息
            LogFlow logFlow = new LogFlow("oQc", id,
                    id_Fs, "score", id_U, tokData.getString("grpU")
                    , orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
                    orderAction.getId_OP(), id_O, index, orderOItem.getId_CB(), orderOItem.getId_C(), ""
                    , tokData.getString("dep"), "评分成功!", 3, orderOItem.getWrdN()
                    , tokData.getJSONObject("wrdNU"));
            logFlow.getData().put("score",score);
            // 设置发送对象
            logFlow.setId_Us(id_Us);
            // 调用新方法发送websocket
            ws.sendWS(logFlow);
            return retResult.ok(CodeEnum.OK.getCode(), result);
        } catch (Exception e) {
            result.put("desc","评分异常");
            result.put("type",0);
            sendMsgNotice(id_C,"评分异常!",id_U,id_O,qt.setArray(id_U),index,"score",id,id);
            return retResult.ok(CodeEnum.OK.getCode(), result);
        }
    }

    /**
     * 操作群的默认回复api
     * @param id_C	公司编号
     * @param logId	群编号
     * @param defReply	默认回复信息
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse updateDefReply(String id_C, String logId, JSONArray defReply) {
        Asset asset = qt.getConfig(id_C,"a-auth","flowControl");
        if (null != asset && null != asset.getFlowControl()) {
            JSONArray objData = asset.getFlowControl().getJSONArray("objData");
            for (int i = 0; i < objData.size(); i++) {
                if (logId.equals(objData.getJSONObject(i).getString("id"))) {
                    qt.setMDContent(asset.getId(), qt.setJson("flowControl.objData."+i+".defReply", defReply), Asset.class);
                    return retResult.ok(CodeEnum.OK.getCode(), "1");
                }
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "0");
    }

    /**
     * 发送日志api
     * @param logType	日志类型
     * @param dataInfo	消息类型对应的数据体
     * @param index	订单任务对应下标
     * @param id_O	订单编号
     * @param id	内部群编号
     * @param id_FS	对外群编号
     * @param tokData	当前登录信息
     * @param type	消息类型
     * @param id_Us	指定发送用户列表
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse sendMsgByOnly(String logType, String dataInfo,
                                     Integer index, String id_O,
                                     String id, String id_FS, JSONObject tokData,int type,JSONArray id_Us) {
        // 获取进度信息
        JSONObject actData = this.getActionData(id_O, index);

//        // 设置产品状态
//        String compId;
//        if (logType.endsWith("SL")) {
//            compId = actData.getJSONObject("info").getString("id_CB");
//        } else {
//            compId = tokData.getString("id_C");
//        }
        // 获取当前oItem信息
        OrderOItem orderOItem = qt.jsonTo(actData.get("orderOItem"), OrderOItem.class);
        // 获取当前action信息
        OrderAction orderAction = qt.jsonTo(actData.get("orderAction"), OrderAction.class);
        // 定义存储描述
        String desc = "";
        // 定义存储日志data
        JSONObject data = new JSONObject();
        // 定义存储子日志类型
        String subType;
        // 添加数据
        data.put("type","sendMsg");
        if (type == 1) {
            // 判断为图片消息
            subType = "pic";
            data.put("pic",dataInfo);
            data.put("video","");
        } else if (type == 2) {
            // 判断为产品消息
            subType = "prod";
            data.put("id_P",dataInfo);
        } else {
            // 判断为普通消息
            subType = "msg";
            desc = dataInfo;
        }
        // 根据存储值创建日志
        LogFlow logL = new LogFlow("msg", id,
                id_FS, subType, tokData.getString("id_U"), tokData.getString("grpU")
                , orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
                orderAction.getId_OP(), id_O, index, orderOItem.getId_CB(), orderOItem.getId_C(), ""
                , tokData.getString("dep"), desc, 3, orderOItem.getWrdN(), tokData.getJSONObject("wrdNU"));
        // 设置日志信息
        logL.setData(data);
        logL.setId_Us(id_Us);
        // 发送日志信息
        ws.sendWS(logL);
        return retResult.ok(CodeEnum.OK.getCode(), "发送成功");
    }

    /**
     * 发送日志信息到指定的id_U方法
     * @param sendUser	指定的id_U（用户编号）
     * @param logFlow	日志信息
     * @author tang
     * @date 创建时间: 2023/5/30
     * @ver 版本号: 1.0.0
     */
//    public void sendMsgOneNew(String sendUser,LogFlow logFlow){
////        JSONObject data = logFlow.getData();
////        data.put("id_UPointTo",sendUser);
//        if (null != sendUser) {
//            logFlow.setId_Us(qt.setArray(sendUser, logFlow.getId_U()));
//        } else {
//            logFlow.setId_Us(qt.setArray(logFlow.getId_U()));
//        }
//        JSONObject data = logFlow.getData();
//        data.put("id_UPointTo",logFlow.getId_Us());
//        logFlow.setData(data);
////        logFlow.setData(data);
//        ws.sendWS(logFlow);
//    }

    /**
     * 客服回访顾客api
     * @param id_O	订单编号
     * @param index 订单任务对应下标
     * @param id    内部群编号
     * @param id_Fs 对外群编号
     * @param tokData   当前登录信息
     * @param type  回访类型
     * @param dataInfo  回访类型对应的数据体
     * @param id_Us 指定发送用户列表
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse foCount(String id_O, Integer index,String id,String id_Fs
            , JSONObject tokData,int type,String dataInfo,JSONArray id_Us) {
        // 获取订单的oQc卡片信息
        Order order = qt.getMDContent(id_O, Collections.singletonList("oQc"), Order.class);
        // 获取公司编号
        String id_C = tokData.getString("id_C");
        // 获取用户编号
        String id_U = tokData.getString("id_U");
        // 定义存储返回结果
        JSONObject result = new JSONObject();
        // 判断订单和oQc卡片不为空
        if (null == order || null == order.getOQc() || null == order.getOQc().getJSONArray("objQc")) {
            // 添加返回结果
            result.put("desc","订单异常");
            result.put("type",0);
            // 发送通知日志
            sendMsgNotice(id_C,"订单异常",id_U,id_O,qt.setArray(id_U),index,"foCount",id,id);
            return retResult.ok(CodeEnum.OK.getCode(), result);
        }
        // 获取oQc卡片信息
        JSONArray objQc = order.getOQc().getJSONArray("objQc");
        // 获取回访次数
        int foUp = objQc.getJSONObject(index).getInteger("foCount");
        // 回访次数减一
        foUp--;
        // 判断回访次数不足
        if (foUp < 0) {
            // 添加返回结果
            result.put("desc","回访次数上限！");
            result.put("type",2);
            sendMsgNotice(id_C,"回访次数上限！",id_U,id_O,qt.setArray(id_U),index,"foCount",id,id);
            return retResult.ok(CodeEnum.OK.getCode(), result);
        }
        // 获取进度信息
        JSONObject actData = this.getActionData(id_O, index);
        // 获取当前oItem信息
        OrderOItem orderOItem = qt.jsonTo(actData.get("orderOItem"), OrderOItem.class);
        // 获取当前action信息
        OrderAction orderAction = qt.jsonTo(actData.get("orderAction"), OrderAction.class);
        // 获取当前时间
        String dateNow = DateUtils.getDateNow(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        // 创建日志信息
        LogFlow logFlow = new LogFlow("oQc", id,
                id_Fs, "foCount", id_U, tokData.getString("grpU")
                , orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
                orderAction.getId_OP(), id_O, index, orderOItem.getId_CB(), orderOItem.getId_C(), ""
                , tokData.getString("dep"), "回访成功!", 3, orderOItem.getWrdN()
                , tokData.getJSONObject("wrdNU"));
        // 设置日志data信息
        logFlow.getData().put("type",type);
        logFlow.getData().put("dataInfo",dataInfo);
        // 设置日志接收用户列表
        logFlow.setId_Us(id_Us);
        logFlow.setTmd(dateNow);
        // 发送日志
        ws.sendWS(logFlow);
        // 更新日志回访次数
        qt.setMDContent(id_O, qt.setJson("oQc.objQc."+index+".foCount",foUp), Order.class);
        // 添加返回结果
        result.put("desc","回访成功!");
        result.put("type",1);
        // 定义存储子日志类型
        String subType;
        // 定义存储日志data信息
        JSONObject data = new JSONObject();
        // 定义存储日志描述
        String desc = "";
        // 添加日志data信息
        data.put("type","defReply");
        if (type == 1) {
            // 判断为图片
            subType = "pic";
            data.put("pic",dataInfo);
        } else if (type == 2) {
            // 判断为产品
            subType = "prod";
            data.put("id_P",dataInfo);
        } else {
            // 判断为普通日志
            subType = "msg";
            desc = dataInfo;
        }
        // 创建日志信息
        LogFlow logLNewMsg = new LogFlow("msg", id,
                id_Fs, subType, id_U, tokData.getString("grpU")
                , orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
                orderAction.getId_OP(), id_O, index, orderOItem.getId_CB(), orderOItem.getId_C(), ""
                , tokData.getString("dep"), desc, 3, orderOItem.getWrdN()
                , tokData.getJSONObject("wrdNU"));
        // 设置日志data
        logLNewMsg.setData(data);
        // 设置接收用户
        logLNewMsg.setId_Us(id_Us);
        // 设置时间并且把时间加一秒
        logLNewMsg.setTmd(DateUtils.getDateNowAddSecond(DateEnum.DATE_TIME_FULL.getDate(),dateNow,1));
        // 发送websocket
        ws.sendWS(logLNewMsg);
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    /**
     * 发送通知日志方法
     * @param id_CCus	公司编号
     * @param desc	消息内容
     * @param logUser	发送用户编号
     * @param id_O	日志订单编号
     * @author tang
     * @date 创建时间: 2023/5/29
     * @ver 版本号: 1.0.0
     */
    public void sendMsgNotice(String id_CCus,String desc,String logUser
            ,String id_O,JSONArray id_Us,Integer index,String type,String id,String id_Fs){
        LogFlow logFlow = getNullLogFlow(desc,id_CCus,logUser,new JSONObject(),id_O,index
                ,"action","notice",id,id_Fs);
        logFlow.setId_Us(id_Us);
        logFlow.getData().put("type",type);
        ws.sendWS(logFlow);
    }

    /**
     * 获取清空并重新赋值的日志信息
     *
     * @param desc 日志内容
     * @param id_C 公司编号
     * @param id_U 用户编号
     * @param data 日志详细信息
     * @param id_O 日志订单编号
     * @return 返回结果: {@link LogFlow}
     * @author tang
     * @date 创建时间: 2023/5/29
     * @ver 版本号: 1.0.0
     */
    private LogFlow getNullLogFlow(String desc, String id_C, String id_U
            , JSONObject data, String id_O, Integer index,String logType,String subType
            ,String id,String id_Fs){
        LogFlow logFlow = LogFlow.getInstance();
        logFlow.setId(id);
        logFlow.setId_FS(id_Fs);
        logFlow.setLogType(logType);
        logFlow.setSubType(subType);
        logFlow.setZcndesc(desc);
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        logFlow.setId_C(id_C);
        logFlow.setId_U(id_U);
        logFlow.setData(data);
        logFlow.setId_O(id_O);
        logFlow.setIndex(index);
        return logFlow;
    }

    private void setFavRecent(String id_C, String id_U, String id_O, Integer index, String id_FC, String id_FS, JSONObject wrdN, String pic)
    {
        JSONObject jsonFav = qt.setJson("id_C", id_C, "id_O", id_O, "index", index, "id", id_FC, "id_FS", id_FS,
                "wrdN", wrdN, "pic", pic, "type", 0);
        qt.pushMDContent(id_U, "fav.objFav", jsonFav, User.class);
        qt.pushMDContent(id_O, "action.objAction." + index + ".arrUA", id_U, Order.class);
    }

    private void removeFavRecent(JSONArray idRemove, String id_O, Integer index, String id_FC, String id_FS, JSONArray id_UA)
    {
        JSONObject jsonFav = qt.setJson("id_O", id_O, "index", index, "id", id_FC, "id_FS", id_FS);
        for (int i = 0; i < idRemove.size(); i++) {
            qt.pullMDContent(idRemove.getString(i), "fav.objFav", jsonFav, User.class);
            id_UA.remove(idRemove.getString(i));
        }
        qt.setMDContent(id_O, qt.setJson("action.objAction." + index + ".arrUA", id_UA), Order.class);
    }
    /**
     * 操作开始，暂停，恢复功能 - 注释完成
     *
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2020/8/6 9:22
     */


        @Override
        @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
        public JSONObject changeActionStatus(String logType, Integer status, String msg,
                                             Integer index, String id_O, Boolean isLink,
                                             String id_FC, String id_FS, JSONObject tokData) {

                JSONObject actData = this.getActionData(id_O, index);

                OrderOItem orderOItem = qt.jsonTo(actData.get("orderOItem"), OrderOItem.class);
                OrderAction orderAction = qt.jsonTo(actData.get("orderAction"), OrderAction.class);


                 // [E1323] xxxx产品 - 工序 " " 拼接zcndesc
                String wrdNP = orderOItem.getWrdNP() == null ? "" : (orderOItem.getWrdNP().getString("cn") +  " - " );
                String prefixTaskName = orderAction.getRefOP().equals("") ? "" :  "[" + orderAction.getRefOP() + "] ";
                String taskName = prefixTaskName + wrdNP + orderOItem.getWrdN().getString("cn") + " ";

                // 根据下标获取递归信息

                // 判断新的状态和旧状态如果一样, 该操作已被处理, 两人同时有用, 防止重复按
                if (orderAction.getBcdStatus().equals(status)) {
                    // 抛出操作成功异常
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "该操作已被处理");
                }

                // 备注
                String message;
                JSONObject res = new JSONObject();
                JSONObject mapKey = new JSONObject();
                JSONObject listCol = new JSONObject();
                boolean isStateChg = false;
                String duraType = "none";

                /***
                 * logStatus(wn0prog) = 2@推送 3@开始 1@停 -1@再开 -6@取消 5@完成
                 * bcdStatus = 0, 1 start, 2 end, -8 resume, 8 stop, 9 cancel
                 * 0 about, 1 process, 9 stop, -8(3) resume/process, 3 finished, 10 cancel, 8 process(with next started)
                 * rePush = > -2,
                 *
                 * 1. send logDura, 2. favRecent+, 3.
                 */
                // if state => 1 ^id_Us+ / ^state => 1 / ^logState 1 / ^ logdura+start / ^recent+ / ^lST = 8
                // if state => 5 ^(add me) id_Us+ / ^state no chg / ^logState no / ^logdura+start / ^recent+
                // if state => 6 ^(rem me) id+Us- / ^state no chg / ^logState no / ^logdura+end / ^recent keep

                // 8 pause (^if id_Us [1]) id_Us- / ^stateChg / ^logState / ^logdura+end / ^recent keep
                // 3/-8 resume (^id_Us+) / id_Us = []? ^stateChg / ^logState / ^logdura+start /

                // if state => 2 ^statechg/^logState /^id_Us = [] / ^logdura all user end /  ^FavRecent remove all / ^lST = 13

                // 7 start next => ^isactivated = 4 / push next / ^nothing else changed
                // 9 cancel => ^state = 9 / ^id_Us = [] ^ all user end / ^favRecent remove all

                // ???repush state = 0 log sent as 0, waiting for user to start?

            // activate = 4 means Skip = already pushed Next


            JSONArray id_Us = orderAction.getId_Us() == null ? new JSONArray() : orderAction.getId_Us();

                // 判断属于什么操作
                switch (status) {
                    case 0: // ready
                        message = taskName + "[准备开始] " + msg;
                        orderAction.setBcdStatus(status);
                        isStateChg = true;

                        break;
                    case 1:
                        // Start an OItem, DG just start, Task just start, Quest start + update Prob
                        if (orderAction.getBcdStatus() != 0) {
                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经开始过了");
                        }
                        // 设置备注信息
                        // can start now, send a msg for status into 1, and then can start doing other buttons, very simple
                        message = taskName + "[开始运行] " + msg;

                        //Adding myself to the id_Us of action to indicate
                        id_Us.add(tokData.getString("id_U"));
                        orderAction.setId_Us(id_Us);
                        res.put("isJoin", 1);
                        res.put("id_Us", orderAction.getId_Us());
                        if (actData.getJSONObject("info").getInteger("lST") == 3 || actData.getJSONObject("info").getInteger("lST") == 7) {
                            mapKey.put("info.lST", 8);
                            listCol.put("lST", 8);
                        }
                        orderAction.setBcdStatus(status);
                        isStateChg = true;
                        duraType = "start";

                        break;
                    case 2:                    // 2 = finish, set qtyfin setNextDesc
//                        if (orderAction.getBcdStatus() != 2 && orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3 && orderAction.getBcdStatus() != -8 && orderAction.getBcdStatus() != 7) {
//                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经处理了");
//                        }
                        if (orderAction.getSumChild() > 0 && orderAction.getBmdpt() == 2 && isLink) {
                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_RECURRED.getCode(), "还有子工序没完成，不能完成");
                        }

                        Double progress = Double.valueOf((actData.getInteger("progress") + 1) / actData.getJSONArray("actionArray").size() * 100);
                        //update actions' total progress
                        if (progress == 100.0) {
                            mapKey.put("info.lST", 13);
                            listCol.put("lST", 13);
                        }
                        mapKey.put("action.wn2progress", progress);
                        orderAction.setBcdStatus(status);

                        message = taskName + "[已完成] " + msg;
                        isStateChg = true;
                        duraType = "allEnd";

                        break;
                    case -2:                    // 2 = finish, set qtyfin setNextDesc
//                        if (orderAction.getBcdStatus() != 2 && orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3 && orderAction.getBcdStatus() != -8 && orderAction.getBcdStatus() != 7) {
//                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经处理了");
//                        }
                        if (orderAction.getSumChild() > 0 && orderAction.getBmdpt() == 2 && isLink) {
                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_RECURRED.getCode(), "还有子工序没完成，不能完成");
                        }

                        message = taskName + "[已完成] " + msg;
                        isStateChg = true;
                        duraType = "allEnd";

                        break;
                    case 3:
                    case -8: // resume OItem
                        if (orderAction.getBcdStatus() != 8) {
//                            return retResult.ok(ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");

                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");
                        }

                        message = taskName + "[已恢复执行] " + msg;
                        orderAction.setBcdStatus(-8);
                        res.put("isJoin", 1);
                        res.put("id_Us", orderAction.getId_Us());
                        isStateChg = true;
                        duraType = "resume";

                        break;
                    case 5: // 加入
                        id_Us.add(tokData.getString("id_U"));
                        orderAction.setId_Us(id_Us);
                        message = tokData.getJSONObject("wrdNU").getString("cn") + "[加入成功] " + taskName;
                        // set back status to original status
                        status = orderAction.getBcdStatus();

                        res.put("isJoin", 1);
                        res.put("id_Us", orderAction.getId_Us());
                        duraType = "start";
                        isStateChg = true;
                        break;
                    case 6:
                        id_Us.remove(tokData.getString("id_U"));
                        orderAction.setId_Us(id_Us);

                        message = tokData.getJSONObject("wrdNU").getString("cn") + "[退出成功] " + taskName;
                        status = orderAction.getBcdStatus();

                        res.put("isJoin", 0);
                        res.put("id_Us", orderAction.getId_Us());
                        duraType = "end";
                        isStateChg = true;
                        break;
                    case 7:
                        if ((orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != -8)
                                || orderAction.getBisactivate() == 4) {
                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能操作");
                        }
                        orderAction.setBisactivate(4);
                        message = taskName + "[继续下一个]" + msg;

                        break;
                    case 8: // pause
                        if (orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3
                                && orderAction.getBcdStatus() != -8) {
                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能");
                        }                    // 设置备注信息
                        message = taskName + "[已暂停]" + msg;
                        orderAction.setBcdStatus(status);
                        isStateChg = true;
                        duraType = "pause";

//                    logStatus = 1;
                        break;
                    case 9: // cancel

                        // pause but it's nothing special
                        if (orderAction.getBcdStatus() == 8) {
                            // need to unpause then cancel
                            LogFlow logL = new LogFlow("action", id_FC,
                                    id_FS, "stateChg", tokData.getString("id_U"), tokData.getString("grpU"), orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
                                    orderAction.getId_OP(), id_O, index, orderOItem.getId_CB(), orderOItem.getId_C(), "", tokData.getString("dep"), "[准备取消]", 3, orderOItem.getWrdN(), tokData.getJSONObject("wrdNU"));
                            logL.setLogData_action(orderAction, orderOItem);
                            logL.getData().put("bcdStatus", -8);
                            ws.sendWS(logL);
                        }
                        // 设置备注信息
                        orderAction.setBcdStatus(status);
                        message = taskName + "[已取消] " + msg;
                        isStateChg = true;
                        duraType = "allEnd";
                        break;
                    case 54:
                        // 设置备注信息
                        // can start now, send a msg for status into 1, and then can start doing other buttons, very simple
                        message = "[cusMsg-拒收]" + msg;

                        //Adding myself to the id_Us of action to indicate
//                        id_Us.add(tokData.getString("id_U"));
//                        orderAction.setId_Us(id_Us);
//                        res.put("isJoin", 1);
//                        res.put("id_Us", orderAction.getId_Us());
//                        if (actData.getJSONObject("info").getInteger("lST") == 3 || actData.getJSONObject("info").getInteger("lST") == 7) {
//                            mapKey.put("info.lST", 8);
//                            listCol.put("lST", 8);
//                        }
                        orderAction.setBcdStatus(status);
//                        duraType = "start";
                        res.put("isJoin", 1);
                        res.put("id_Us", orderAction.getId_Us());
                        isStateChg = true;
                        duraType = "resume";
                        break;
                    case 55:
                        message = "[cusMsg-删除]" + msg;

                        //Adding myself to the id_Us of action to indicate
//                        id_Us.add(tokData.getString("id_U"));
//                        orderAction.setId_Us(id_Us);
//                        res.put("isJoin", 1);
//                        res.put("id_Us", orderAction.getId_Us());
//                        if (actData.getJSONObject("info").getInteger("lST") == 3 || actData.getJSONObject("info").getInteger("lST") == 7) {
//                            mapKey.put("info.lST", 8);
//                            listCol.put("lST", 8);
//                        }
                        orderAction.setBcdStatus(status);
                        res.put("isJoin", 1);
                        res.put("id_Us", orderAction.getId_Us());
                        isStateChg = true;
                        duraType = "resume";
                        break;
                    case 53:
                        message = "[已恢复执行]" + msg;
                        orderAction.setBcdStatus(-8);
                        res.put("isJoin", 1);
                        res.put("id_Us", orderAction.getId_Us());
                        isStateChg = true;
                        duraType = "resume";
                        break;
                    default:
                        message = taskName + "[无法操作]";
                        break;
                }

                // 设置产品状态
                String compId;

                if (logType.endsWith("SL")) {
                    compId = actData.getJSONObject("info").getString("id_CB");
                } else {
                    compId = tokData.getString("id_C");
                }

                if (isStateChg) {

                    if (status.equals(-2) || status.equals(2) || status.equals(9))
                    {
                        //removing it from flowControl RefOP
                        this.updateRefOP(actData.getJSONObject("info").getString("id_CB"), actData.getJSONObject("info").getString("id_C"),
                                id_FC, id_FS, orderAction.getId_OP(), orderAction.getRefOP(), orderAction.getWrdNP(), orderAction.getIndex(), false );
                    }

                    // Start making log with data
                    LogFlow logL = new LogFlow("action", id_FC,
                            id_FS, "stateChg", tokData.getString("id_U"), tokData.getString("grpU"), orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
                            orderAction.getId_OP(), id_O, index, orderOItem.getId_CB(), orderOItem.getId_C(), "", tokData.getString("dep"), message, 3, orderOItem.getWrdN(), tokData.getJSONObject("wrdNU"));

                    // Here set time info into action's log
                    logL.setLogData_action(orderAction, orderOItem);
                    logL.getData().put("id_Us",actData.getJSONArray("id_Us"));

                    if (duraType.equals("start") || duraType.equals("resume")) {
                        logL.setActionTime(DateUtils.getTimeStamp(), 0L, duraType);
                    } else if (duraType.equals("end") || duraType.equals("pause") || duraType.equals("allEnd")) {
                        logL.setActionTime(0L, DateUtils.getTimeStamp(), duraType);
                    }

                    qt.errPrint("logL is", logL);
                    ws.sendWS(logL);
                }

                // setup User's Fav card
                if (duraType.equals("start"))
                    this.setFavRecent(tokData.getString("id_C"), tokData.getString("id_U"), id_O, index, id_FC, id_FS,
                            orderOItem.getWrdN(), orderOItem.getPic());
                else if (duraType.equals("end")) {
                    this.removeFavRecent(qt.setArray(tokData.getString("id_U")), id_O, index, id_FC, id_FS, orderAction.getArrUA());
                    orderAction.getId_Us().remove(tokData.getString("id_U"));
                }
                else if (duraType.equals("allEnd"))
                {
                    this.removeFavRecent(orderAction.getId_Us(), id_O, index, id_FC, id_FS, orderAction.getArrUA());
                    orderAction.setId_Us(new JSONArray());
                    LogFlow logDURA = new LogFlow("action", id_FC,
                        id_FS, "userStat", tokData.getString("id_U"), tokData.getString("grpU"), orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
                        orderAction.getId_OP(), id_O, index, compId, orderOItem.getId_C(), "", tokData.getString("dep"), "", 3, orderOItem.getWrdN(), tokData.getJSONObject("wrdNU"));

                    // I am just fixing this by putting the "finish" time into calculation and ignoring the last log
                    this.sumDura(id_O, index, logDURA);
                }


                mapKey.put("action.objAction." + index, orderAction);
                qt.setMDContent(id_O, mapKey, Order.class);

                if (null != listCol.getInteger("lST")) {
                    qt.setES("lsborder", qt.setESFilt("id_O", id_O), listCol);
                }

                // if Quest, send log + update OItem of myself = task = DG = above
                // get upPrnt data, and find the prob, set that status of Prob to status

                //*** Here we set oStock qty to 1 whenever noP task is completed
                if ((status == 2 || status == -2) && orderOItem.getId_P().equals(""))
                {
                    Order oStockCheck = qt.getMDContent(id_O, Arrays.asList("info","oStock", "action", "oItem", "view"), Order.class);
                    if (oStockCheck.getOStock() != null)
                    {
                        if (oStockCheck.getOStock().getJSONArray("objData") == null || oStockCheck.getOStock().getJSONArray("objData").size() < index + 1)
                        {
                            oStockCheck.getOStock().put ("objData", dbu.initOStock(qt.toJson(orderOItem), oStockCheck.getOStock().getJSONArray("objData"), index));
                        }

                        Double qty = oStockCheck.getOStock().getJSONArray("objData").getJSONObject(index).getDouble("wn2qtynow");
                        Double qtyAdding = 1 - qty;

                        oStockCheck.getOStock().getJSONArray("objData").getJSONObject(index).put("wn2qtynow", 1.0);
                        JSONObject listCol2 = new JSONObject();
                        dbu.summOrder(oStockCheck, listCol2, qt.setArray("oStock"));
//                        qt.saveMD(oStockCheck);
                        qt.setMDContent(id_O, qt.setJson("oStock", oStockCheck.getOStock()), Order.class);

                        qt.setES("lsborder", qt.setESFilt("id_O", id_O), listCol);

                        LogFlow log = new LogFlow(tokData, oStockCheck.getOItem(), oStockCheck.getAction(), "", id_O, index,
                                "assetflow", "qtyChg", orderAction.getRefOP() + "-" + orderOItem.getWrdN().getString("cn") +
                                " 完成了 " + qtyAdding, 3);

                        Double price = orderOItem.getWn4price() == null ? 0.0: orderOItem.getWn4price();
                        log.setLogData_assetflow(qtyAdding, price, "","");

                        ws.sendWS(log);

                    }
                    //getOStock (if not null)
                    //was 0.3 then set to 1
                    //send log that I finished 0.7
                }

                if (orderAction.getBisactivate() == 7)
                {
                    JSONObject upPrnt = orderAction.getUpPrnts().getJSONObject(0);

                    try {
                        // if any null exception, catch
                        JSONObject taskOwner = this.getActionData(upPrnt.getString("id_O"), upPrnt.getInteger("index"));
                        OrderAction objAction = JSONObject.parseObject(JSON.toJSONString(taskOwner.get("orderAction")), OrderAction.class);

                        JSONObject probData = objAction.getProb().getJSONObject(upPrnt.getInteger("probIndex"));
                        probData.put("bcdStatus", status);

                        qt.setMDContent(upPrnt.getString("id_O"),
                                qt.setJson("action.objAction." + upPrnt.getInteger("index"), objAction),
                                Order.class);
                    } catch (RuntimeException e) {
                        //System.out.println("shit A");
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_AN_ERROR_OCCURRED.getCode(), "不能开始," + e);
                    }
                } else if ((status == 2 && orderAction.getBisactivate() != 4) || status == 7) {
                    // activate = 4 means Skip = already pushed Next, status == -2 then skip upNext
                    if (orderAction.getUpPrnts().size() == 0 && orderOItem.getId_P().equals("")) {
                        //Here for noP, DO NOT check parent, it will blow up
                        this.updateNext(orderAction, tokData);
                    }
                    else {
                        //for regular DG, we will go check our parent first
                        //then push it together with myself because I am always the first Item
                        this.updateParent(orderAction, tokData);
                    }
                } else if (status == 1 && orderAction.getBmdpt() == 4)
                {
                    // here I must check all my subParts, and see if they are prtPrev.size == 0
                    // if so, push
                    this.updateSon(orderAction, tokData);
                }

            // 抛出操作成功异常
            return res;
//            return retResult.ok(CodeEnum.OK.getCode(), res);
        }

//    @Override
//    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
//    public ApiResponse changeActionStatusNew(String logType, Integer status, String msg,
//                                          Integer index, String id_O, Boolean isLink,
//                                          String id_FC, String id_FS, JSONObject tokData,JSONArray id_UsLog) {
//
//        JSONObject actData = this.getActionData(id_O, index);
//
//        OrderOItem orderOItem = qt.jsonTo(actData.get("orderOItem"), OrderOItem.class);
//        OrderAction orderAction = qt.jsonTo(actData.get("orderAction"), OrderAction.class);
//
//        String taskName = orderOItem.getWrdN().getString("cn");
//
//        // 根据下标获取递归信息
//
//        // 判断新的状态和旧状态不一样
////                if (orderAction.getBcdStatus().equals(status)) {
////                    // 抛出操作成功异常
//////                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "该操作已被处理");
////                    return retResult.error(ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "该操作已被处理");
////
////                }
//
//        // 备注
//        String message;
//        JSONObject res = new JSONObject();
//        JSONObject mapKey = new JSONObject();
//        JSONObject listCol = new JSONObject();
//        boolean isStateChg = false;
//        String duraType = "none";
//
//        /***
//         * logStatus(wn0prog) = 2@推送 3@开始 1@停 -1@再开 -6@取消 5@完成
//         * bcdStatus = 0, 1 start, 2 end, -8 resume, 8 stop, 9 cancel
//         * 0 about, 1 process, 9 stop, -8(3) resume/process, 3 finished, 10 cancel, 8 process(with next started)
//         * rePush = > -2,
//         *
//         * 1. send logDura, 2. favRecent+, 3.
//         */
//        // if state => 1 ^id_Us+ / ^state => 1 / ^logState 1 / ^ logdura+start / ^recent+ / ^lST = 8
//        // if state => 5 ^(add me) id_Us+ / ^state no chg / ^logState no / ^logdura+start / ^recent+
//        // if state => 6 ^(rem me) id+Us- / ^state no chg / ^logState no / ^logdura+end / ^recent keep
//
//        // 8 pause (^if id_Us [1]) id_Us- / ^stateChg / ^logState / ^logdura+end / ^recent keep
//        // 3/-8 resume (^id_Us+) / id_Us = []? ^stateChg / ^logState / ^logdura+start /
//
//        // if state => 2 ^statechg/^logState /^id_Us = [] / ^logdura all user end /  ^FavRecent remove all / ^lST = 13
//
//        // 7 start next => ^isactivated = 4 / push next / ^nothing else changed
//        // 9 cancel => ^state = 9 / ^id_Us = [] ^ all user end / ^favRecent remove all
//
//        // ???repush state = 0 log sent as 0, waiting for user to start?
//
//        // activate = 4 means Skip = already pushed Next
//
//
//        JSONArray id_Us = orderAction.getId_Us() == null ? new JSONArray() : orderAction.getId_Us();
//
//        // 判断属于什么操作
//        switch (status) {
//            case 1:
//                // Start an OItem, DG just start, Task just start, Quest start + update Prob
//                if (orderAction.getBcdStatus() != 0) {
//                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经开始过了");
//                }
//                // 设置备注信息
//                // can start now, send a msg for status into 1, and then can start doing other buttons, very simple
//                message = "[开始运行]" + msg;
//
//                //Adding myself to the id_Us of action to indicate
//                id_Us.add(tokData.getString("id_U"));
//                orderAction.setId_Us(id_Us);
//                res.put("isJoin", 1);
//                res.put("id_Us", orderAction.getId_Us());
//                if (actData.getJSONObject("info").getInteger("lST") == 3 || actData.getJSONObject("info").getInteger("lST") == 7) {
//                    mapKey.put("info.lST", 8);
//                    listCol.put("lST", 8);
//                }
//                orderAction.setBcdStatus(status);
//                isStateChg = true;
//                duraType = "start";
//
//                break;
//            case 2:                    // 2 = finish, set qtyfin setNextDesc
////                        if (orderAction.getBcdStatus() != 2 && orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3 && orderAction.getBcdStatus() != -8 && orderAction.getBcdStatus() != 7) {
////                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经处理了");
////                        }
//                if (orderAction.getSumChild() > 0 && orderAction.getBmdpt() == 2 && isLink) {
//                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_RECURRED.getCode(), "还有子工序没完成，不能完成");
//                }
//
//                Double progress = Double.valueOf((actData.getInteger("progress") + 1) / actData.getJSONArray("actionArray").size() * 100);
//                //update actions' total progress
//                if (progress == 100.0) {
//                    mapKey.put("info.lST", 13);
//                    listCol.put("lST", 13);
//                }
//                mapKey.put("action.wn2progress", progress);
//                orderAction.setBcdStatus(status);
//
//                message = "[已完成]" + msg;
//                isStateChg = true;
//                duraType = "allEnd";
//
//                break;
//            case 3:
//            case -8: // resume OItem
//                if (orderAction.getBcdStatus() != 8) {
//                    return retResult.ok(ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");
//
////                            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");
//                }
//
//                message = "[已恢复执行]" + msg;
//                orderAction.setBcdStatus(-8);
//                res.put("isJoin", 1);
//                res.put("id_Us", orderAction.getId_Us());
//                isStateChg = true;
//                duraType = "resume";
//
//                break;
//            case 5: // 加入
//                id_Us.add(tokData.getString("id_U"));
//                orderAction.setId_Us(id_Us);
//                message = tokData.getJSONObject("wrdNU").getString("cn") + "[加入成功]" + msg;
//                // set back status to original status
//                status = orderAction.getBcdStatus();
//
//                res.put("isJoin", 1);
//                res.put("id_Us", orderAction.getId_Us());
//                duraType = "start";
//                break;
//            case 6:
//                id_Us.remove(tokData.getString("id_U"));
//                orderAction.setId_Us(id_Us);
//
//                message = tokData.getJSONObject("wrdNU").getString("cn") + "[退出成功]" + msg;
//                status = orderAction.getBcdStatus();
//
//                res.put("isJoin", 0);
//                res.put("id_Us", orderAction.getId_Us());
//                duraType = "end";
//                break;
//            case 7:
//                if ((orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != -8)
//                        || orderAction.getBisactivate() == 4) {
//                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能操作");
//                }
//                orderAction.setBisactivate(4);
//                message = "[继续下一个]" + msg;
//
//                break;
//            case 8: // pause
//                if (orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3
//                        && orderAction.getBcdStatus() != -8) {
//                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能");
//                }                    // 设置备注信息
//                message = "[已暂停]" + msg;
//                orderAction.setBcdStatus(status);
//                isStateChg = true;
//                duraType = "pause";
//
////                    logStatus = 1;
//                break;
//            case 9: // cancel
//
//                // pause but it's nothing special
//                if (orderAction.getBcdStatus() == 8) {
//                    // need to unpause then cancel
//                    LogFlow logL = new LogFlow("action", id_FC,
//                            id_FS, "stateChg", tokData.getString("id_U"), tokData.getString("grpU"), orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
//                            orderAction.getId_OP(), id_O, index, tokData.getString("id_C"), orderOItem.getId_C(), "", tokData.getString("dep"), "[准备取消]", 3, orderOItem.getWrdN(), tokData.getJSONObject("wrdNU"));
//                    logL.setLogData_action(orderAction, orderOItem);
//                    logL.getData().put("bcdStatus", -8);
//                    ws.sendWS(logL);
//                }
//                // 设置备注信息
//                orderAction.setBcdStatus(status);
//                message = "[已取消]" + msg;
//                isStateChg = true;
//                duraType = "allEnd";
//                break;
//            case 54:
//                // 设置备注信息
//                // can start now, send a msg for status into 1, and then can start doing other buttons, very simple
//                message = "[cusMsg-拒收]" + msg;
//
//                //Adding myself to the id_Us of action to indicate
////                        id_Us.add(tokData.getString("id_U"));
////                        orderAction.setId_Us(id_Us);
////                        res.put("isJoin", 1);
////                        res.put("id_Us", orderAction.getId_Us());
////                        if (actData.getJSONObject("info").getInteger("lST") == 3 || actData.getJSONObject("info").getInteger("lST") == 7) {
////                            mapKey.put("info.lST", 8);
////                            listCol.put("lST", 8);
////                        }
//                orderAction.setBcdStatus(status);
////                        duraType = "start";
//                res.put("isJoin", 1);
//                res.put("id_Us", orderAction.getId_Us());
//                isStateChg = true;
////                        duraType = "resume";
//                break;
//            case 55:
//                message = "[cusMsg-删除]" + msg;
//
//                //Adding myself to the id_Us of action to indicate
////                        id_Us.add(tokData.getString("id_U"));
////                        orderAction.setId_Us(id_Us);
////                        res.put("isJoin", 1);
////                        res.put("id_Us", orderAction.getId_Us());
////                        if (actData.getJSONObject("info").getInteger("lST") == 3 || actData.getJSONObject("info").getInteger("lST") == 7) {
////                            mapKey.put("info.lST", 8);
////                            listCol.put("lST", 8);
////                        }
//                orderAction.setBcdStatus(status);
//                res.put("isJoin", 1);
//                res.put("id_Us", orderAction.getId_Us());
//                isStateChg = true;
////                        duraType = "resume";
//                break;
//            case 53:
//                message = "[已恢复执行]" + msg;
//                orderAction.setBcdStatus(-8);
//                res.put("isJoin", 1);
//                res.put("id_Us", orderAction.getId_Us());
//                isStateChg = true;
//                duraType = "resume";
//                break;
//            default:
//                message = taskName + "[无法操作]";
//                break;
//        }
//
//        // 设置产品状态
//        String compId;
//
//        if (logType.endsWith("SL")) {
//            compId = actData.getJSONObject("info").getString("id_CB");
//        } else {
//            compId = tokData.getString("id_C");
//        }
//
//        if (isStateChg) {
//
//            if (status.equals(2) || status.equals(9))
//            {
//                //removing it from flowControl RefOP
//                this.updateRefOP(actData.getJSONObject("info").getString("id_CB"), actData.getJSONObject("info").getString("id_C"),
//                        id_FC, id_FS, orderAction.getId_OP(), orderAction.getRefOP(), orderAction.getWrdNP(), orderAction.getIndex(), false );
//            }
//
//            // Start making log with data
//            LogFlow logL = new LogFlow("action", id_FC,
//                    id_FS, "stateChg", tokData.getString("id_U"), tokData.getString("grpU"), orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
//                    orderAction.getId_OP(), id_O, index, compId, orderOItem.getId_C(), "", tokData.getString("dep"), message, 3, orderOItem.getWrdN(), tokData.getJSONObject("wrdNU"));
//
//            // Here set time info into action's log
//            logL.setLogData_action(orderAction, orderOItem);
//            logL.getData().put("id_Us",actData.getJSONArray("id_Us"));
//
//            if (duraType.equals("start") || duraType.equals("resume")) {
//                logL.setActionTime(DateUtils.getTimeStamp(), 0L, duraType);
//            } else if (duraType.equals("end") || duraType.equals("pause") || duraType.equals("allEnd")) {
//                logL.setActionTime(0L, DateUtils.getTimeStamp(), duraType);
//            }
//
//            logL.setId_Us(id_UsLog);
//            ws.sendWS(logL);
//        }
//
//        // setup User's Fav card
//        if (duraType.equals("start"))
//            this.setFavRecent(tokData.getString("id_C"), tokData.getString("id_U"), id_O, index, id_FC, id_FS,
//                    orderOItem.getWrdN(), orderOItem.getPic());
//        else if (duraType.equals("end")) {
//            this.removeFavRecent(qt.setArray(tokData.getString("id_U")), id_O, index, id_FC, id_FS, orderAction.getArrUA());
//            orderAction.getId_Us().remove(tokData.getString("id_U"));
//        }
//        else if (duraType.equals("allEnd"))
//        {
//            this.removeFavRecent(orderAction.getId_Us(), id_O, index, id_FC, id_FS, orderAction.getArrUA());
//            orderAction.setId_Us(new JSONArray());
//            LogFlow logDURA = new LogFlow("action", id_FC,
//                    id_FS, "userStat", tokData.getString("id_U"), tokData.getString("grpU"), orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
//                    orderAction.getId_OP(), id_O, index, compId, orderOItem.getId_C(), "", tokData.getString("dep"), "", 3, orderOItem.getWrdN(), tokData.getJSONObject("wrdNU"));
//
//            // I am just fixing this by putting the "finish" time into calculation and ignoring the last log
//            this.sumDura(id_O, index, logDURA);
//        }
//
//
////                LogFlow logL = new LogFlow("duraflow", id_FC,
////                        id_FS, "userStat", tokData.getString("id_U"), tokData.getString("grpU"), orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
////                        orderAction.getId_OP(), id_O, index, compId, orderOItem.getId_C(), "", tokData.getString("dep"), "", 3, orderOItem.getWrdN(), tokData.getJSONObject("wrdNU"));
////
////                if (duraType.equals("start") || duraType.equals("resume")) { //type start
////                    // Start making log with data
////                    logL.setLogData_duraflow(DateUtils.getTimeStamp(), 0L, duraType);
////                    logL.setZcndesc("任务计时开始");
////                    ws.sendWS(logL);
//////casItemx, summ00s
////                } else if (duraType.equals("end") || duraType.equals("pause")) { //type end
////                    // End making log with data
////                    logL.setLogData_duraflow(0L, DateUtils.getTimeStamp(), duraType);
////                    logL.setZcndesc("任务计时停止");
////                    ws.sendWS(logL);
////
////                } else if (duraType.equals("allEnd")) { // type allEnd
////                    for (int i = 0; i < orderAction.getId_Us().size(); i++) {
////                        logL.setId_U(orderAction.getId_Us().getString(i));
////                        logL.setGrpU("1000");
////                        logL.setLogData_duraflow(0L, DateUtils.getTimeStamp(), duraType);
////                        logL.setZcndesc("任务结束");
////                        ws.sendWS(logL);
////                    }
////                }
////                try {
//        mapKey.put("action.objAction." + index, orderAction);
//        qt.setMDContent(id_O, mapKey, Order.class);
//
//        if (null != listCol.getInteger("lST")) {
//            qt.setES("lsborder", qt.setESFilt("id_O", id_O), listCol);
//        }
//
//        // if Quest, send log + update OItem of myself = task = DG = above
//        // get upPrnt data, and find the prob, set that status of Prob to status
//
//        //*** Here we set oStock qty to 1 whenever noP task is completed
//        if (status == 2 && orderOItem.getId_P().equals(""))
//        {
//            Order oStockCheck = qt.getMDContent(id_O, Arrays.asList("info","oStock", "action", "oItem", "view"), Order.class);
//            if (oStockCheck.getOStock() != null)
//            {
//                Double qty = oStockCheck.getOStock().getJSONArray("objData").getJSONObject(index).getDouble("wn2qtynow");
//                Double qtyAdding = 1 - qty;
//
//                oStockCheck.getOStock().getJSONArray("objData").getJSONObject(index).put("wn2qtynow", 1.0);
//                JSONObject listCol2 = new JSONObject();
//                dbu.summOrder(oStockCheck, listCol2, qt.setArray("oStock"));
////                qt.saveMD(oStockCheck);
//                qt.setMDContent(id_O, qt.setJson("oStock", oStockCheck.getOStock()), Order.class);
//
//                qt.setES("lsborder", qt.setESFilt("id_O", id_O), listCol);
//
//                LogFlow log = new LogFlow(tokData, oStockCheck.getOItem(), oStockCheck.getAction(), "", id_O, index,
//                        "assetflow", "qtyChg", orderAction.getRefOP() + "-" + orderOItem.getWrdN().getString("cn") +
//                        " 完成了 " + qtyAdding, 3);
//
//                Double price = orderOItem.getWn4price() == null ? 0.0: orderOItem.getWn4price();
//                log.setLogData_assetflow(qtyAdding, price, "","");
//
//                log.setId_Us(id_UsLog);
//                ws.sendWS(log);
//            }
//            //getOStock (if not null)
//            //was 0.3 then set to 1
//            //send log that I finished 0.7
//        }
//
//        if (orderAction.getBisactivate() == 7)
//        {
//            JSONObject upPrnt = orderAction.getUpPrnts().getJSONObject(0);
//
//            try {
//                // if any null exception, catch
//                JSONObject taskOwner = this.getActionData(upPrnt.getString("id_O"), upPrnt.getInteger("index"));
//                OrderAction objAction = JSONObject.parseObject(JSON.toJSONString(taskOwner.get("orderAction")), OrderAction.class);
//
//                JSONObject probData = objAction.getProb().getJSONObject(upPrnt.getInteger("probIndex"));
//                probData.put("bcdStatus", status);
//
//                qt.setMDContent(upPrnt.getString("id_O"),
//                        qt.setJson("action.objAction." + upPrnt.getInteger("index"), objAction),
//                        Order.class);
//            } catch (RuntimeException e) {
//                //System.out.println("shit A");
//                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_AN_ERROR_OCCURRED.getCode(), "不能开始," + e);
//            }
//        } else if ((status == 2 && orderAction.getBisactivate() != 4) || status == 7) {
//            // activate = 4 means Skip = already pushed Next
//            if (orderAction.getUpPrnts().size() == 0 && orderOItem.getId_P().equals("")) {
//                //Here for noP, DO NOT check parent, it will blow up
//                this.updateNext(orderAction, tokData);
//            }
//            else {
//                //for regular DG, we will go check our parent first
//                //then push it together with myself because I am always the first Item
//                this.updateParent(orderAction, tokData);
//            }
//        } else if (status == 1 && orderAction.getBmdpt() == 4)
//        {
//            // here I must check all my subParts, and see if they are prtPrev.size == 0
//            // if so, push
//            this.updateSon(orderAction, tokData);
//        }
//
//
//        // 抛出操作成功异常
//        return retResult.ok(CodeEnum.OK.getCode(), res);
//    }

    private void updateSon(OrderAction orderAction, JSONObject tokData)
    {

        for (Integer i = 0; i < orderAction.getSubParts().size(); i++ )
        {
            // 获取子产品的id + index
            String sonId = orderAction.getSubParts().getJSONObject(i).getString("id_O");
            Integer sonIndex = orderAction.getSubParts().getJSONObject(i).getInteger("index");

            JSONObject actData = this.getActionData(sonId, sonIndex);

            if (null != actData) {

                OrderOItem orderOItem1 = JSONObject.parseObject(JSON.toJSONString(actData.get("orderOItem")), OrderOItem.class);
                OrderAction orderAction1 = JSONObject.parseObject(JSON.toJSONString(actData.get("orderAction")), OrderAction.class);

                if (orderAction1.getBcdStatus() == 100 && orderAction1.getPrtPrev().size() == 0) {
                    // 设置该产品的上一个数量减一
                    orderAction1.setBcdStatus(0); //状态改为准备开始
                    orderAction1.setBisPush(1);

                    this.updateRefOP(orderOItem1.getId_CB(), orderOItem1.getId_C(),
                            actData.getString("id_FC"),
                            actData.getString("id_FS"), orderAction1.getId_OP(), orderAction1.getRefOP(),
                            orderAction1.getWrdNP(), sonIndex, true);


                    // Start making log with data
                    LogFlow logL = new LogFlow("action", actData.getString("id_FC"),
                            actData.getString("id_FS"), "stateChg",
                            tokData.getString("id_U"), tokData.getString("grpU"), orderOItem1.getId_P(), orderOItem1.getGrpB(), orderOItem1.getGrp(),
                            orderAction1.getId_OP(), sonId, sonIndex, orderOItem1.getId_CB(), orderOItem1.getId_C(),
                            "", tokData.getString("dep"), orderOItem1.getWrdN().get("cn") + "准备工序组开始", 3, orderOItem1.getWrdN(), tokData.getJSONObject("wrdNU"));
                    logL.setLogData_action(orderAction1, orderOItem1);
                    logL.setActionTime(DateUtils.getTimeStamp(), 0L, "push");

                    logL.getData().put("wn0prog", 2);

                    // 调用发送日志方法
                    ws.sendWS(logL);


                }
                qt.setMDContent(sonId, qt.setJson("action.objAction." + sonIndex, orderAction1), Order.class);

            }
        }
    }

    private void updateNext(OrderAction orderAction, JSONObject tokData)
    {

        for (Integer i = 0; i < orderAction.getPrtNext().size(); i++ )
        {
            // 获取下一个产品的id + index
            String nextId = orderAction.getPrtNext().getJSONObject(i).getString("id_O");
            Integer nextIndex = orderAction.getPrtNext().getJSONObject(i).getInteger("index");

                JSONObject actData = this.getActionData(nextId, nextIndex);
//            qt.errPrint("inUPNEXT", null, actData);

            if (null != actData) {
                    OrderOItem orderOItem1 = JSONObject.parseObject(JSON.toJSONString(actData.get("orderOItem")),OrderOItem.class);
                    OrderAction orderAction1 = JSONObject.parseObject(JSON.toJSONString(actData.get("orderAction")),OrderAction.class);

                    if (orderAction1.getBcdStatus() == 100) {
                        // 设置该产品的上一个数量减一
                        orderAction1.setSumPrev(orderAction1.getSumPrev() - 1);

                        // 判断下一个产品子产品是否为0, // both sumXXX are 0, send log
                        if (orderAction1.getSumPrev() <= 0 && (orderAction1.getSubParts().size() == 0 || orderAction1.getBmdpt() == 4)) {
//                            if (orderAction1.getBmdpt() != 4 && orderAction1.getSumChild() == 0) {
                                orderAction1.setBcdStatus(0); //状态改为准备开始
                                orderAction1.setBisPush(1);

                                this.updateRefOP(orderOItem1.getId_CB(), orderOItem1.getId_C(),
                                        actData.getString("id_FC"),
                                        actData.getString("id_FS"), orderAction1.getId_OP(), orderAction1.getRefOP(),
                                        orderAction1.getWrdNP(), nextIndex, true );

                                // Start making log with data
                                LogFlow logL = new LogFlow("action", actData.getString("id_FC"),
                                        actData.getString("id_FS"), "stateChg",
                                        tokData.getString("id_U"), tokData.getString("grpU"), orderOItem1.getId_P(),orderOItem1.getGrpB(), orderOItem1.getGrp(),
                                        orderAction1.getId_OP(), nextId, nextIndex, orderOItem1.getId_CB(), orderOItem1.getId_C(),
                                        "", tokData.getString("dep"), orderOItem1.getWrdN().get("cn") + "准备开始", 3, orderOItem1.getWrdN(), tokData.getJSONObject("wrdNU"));
                                logL.setLogData_action(orderAction1, orderOItem1);
                                logL.setActionTime(DateUtils.getTimeStamp(), 0L, "push");

                                logL.getData().put("wn0prog", 2);

                                // 调用发送日志方法
                                ws.sendWS(logL);
                        }
                        qt.setMDContent(nextId, qt.setJson("action.objAction." + nextIndex, orderAction1), Order.class);
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
        private void updateParent(OrderAction orderAction,JSONObject tokData) {


            for (Integer i = 0; i < orderAction.getUpPrnts().size(); i++) {

                Integer indexPrnt = orderAction.getUpPrnts().getJSONObject(i).getInteger("index");
                String idPrnt = orderAction.getUpPrnts().getJSONObject(i).getString("id_O");
                JSONObject actData = this.getActionData(idPrnt, indexPrnt);
                if (null != actData) {

                    OrderOItem unitOItemPrnt = JSONObject.parseObject(JSON.toJSONString(actData.get("orderOItem")),OrderOItem.class);
                    OrderAction unitActionPrnt = JSONObject.parseObject(JSON.toJSONString(actData.get("orderAction")),OrderAction.class);

                    // **** 把父的子产品数量减1，带表当前产品已完成
                    unitActionPrnt.setSumChild(unitActionPrnt.getSumChild() - 1);


                    // 只要是第一个开了，父就推出来, @4 工序组时也可能再推，检查已推isPush
                    if (unitActionPrnt.getSumChild().equals(unitActionPrnt.getSubParts().size() - 1)
                    && unitActionPrnt.getBisPush() != 1) {

                        unitActionPrnt.setBcdStatus(0);
                        unitActionPrnt.setBisPush(1);


                        this.updateRefOP(unitOItemPrnt.getId_CB(), unitOItemPrnt.getId_C(),
                                actData.getString("id_FC"),
                                actData.getString("id_FS"), unitActionPrnt.getId_OP(), unitActionPrnt.getRefOP(),
                                unitActionPrnt.getWrdNP(), indexPrnt, true );


                        // Start making log with data
                        LogFlow logL = new LogFlow("action", actData.getString("id_FC"),
                                actData.getString("id_FS"), "stateChg",
                                tokData.getString("id_U"), tokData.getString("grpU"), unitOItemPrnt.getId_P(), unitOItemPrnt.getGrpB(), unitOItemPrnt.getGrp(),
                                unitActionPrnt.getId_OP(), idPrnt, indexPrnt, unitOItemPrnt.getId_CB(), unitOItemPrnt.getId_C(),
                                "", tokData.getString("dep"), unitOItemPrnt.getWrdN().get("cn") + "准备开始", 3, unitOItemPrnt.getWrdN(), tokData.getJSONObject("wrdNU"));

                        //System.out.println(" sending log here"+ logL);

                        logL.setLogData_action(unitActionPrnt, unitOItemPrnt);
                        logL.setActionTime(DateUtils.getTimeStamp(), 0L, "push");



                        ws.sendWS(logL);
                    }

                    qt.setMDContent(idPrnt, qt.setJson("action.objAction."+indexPrnt,unitActionPrnt) , Order.class);

                    // sumChild = 0 时， 所有子零部件都已经推送了， 不用查Next
                    if (unitActionPrnt.getSumChild() != 0)
                    {
                        this.updateNext(orderAction, tokData);
                    }
                }
            }
        }


        //keep a record on all the orders that flow room currently managing, put into flowControl card
    //refOP{"refOP":{refOP, wrdN, index[], id_OP}}
    private void updateRefOP(String id_CB, String id_CS, String id_FC, String id_FS,
                             String id_OP, String refOP, JSONObject wrdN, Integer index, Boolean isStart)
    {
        if (id_CB == null)
            id_CB = "";
        if (id_CS == null)
            id_CS = "";
        if (id_FC == null)
            id_FC = "";
        if (id_FS == null)
            id_FS = "";

        Asset assetB = qt.getConfig(id_CB, "a-auth", "flowControl");
        Asset assetS = qt.getConfig(id_CS, "a-auth", "flowControl");

        JSONObject refOPInfo = qt.setJson("refOP", refOP, "wrdN", wrdN, "index", new JSONArray(), "id_OP", id_OP);

        // check and update id_FC
        if (!id_FC.equals("") && !assetB.getId().equals("none"))
        {

            for (int i = 0; i < assetB.getFlowControl().getJSONArray("objData").size(); i++)
            {

                JSONObject flowInfo = assetB.getFlowControl().getJSONArray("objData").getJSONObject(i);

                //adding 1 index
                if (flowInfo.getString("id").equals(id_FC) && isStart && refOP != null) {
                    // case 1: id_OP not exists in refOP, init
                    if (flowInfo.getJSONObject("refOP") == null)
                    {
                        flowInfo.put("refOP", new JSONObject());
                    }

                    if (id_OP.equals(""))
                    {
                        flowInfo.getJSONObject("refOP").put("id_OP", flowInfo.getString("id_O"));
                    }

                    // case 2: refOP not exists
                    if (flowInfo.getJSONObject("refOP").getJSONObject(refOP) == null)
                    {
                        flowInfo.getJSONObject("refOP").put(refOP, refOPInfo);
                    }

                    // any case, add this index to index[]
                    if (!flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").contains(index)) {
                        flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").add(index);
                        qt.setMDContent(assetB.getId(), qt.setJson("flowControl.objData." + i + ".refOP", flowInfo.getJSONObject("refOP")), Asset.class);

                    }

                    break;

                } // removing now here:
                else if (flowInfo.getString("id").equals(id_FC) && !isStart)
                {

                    // case 1: if index size == 1, remove the whole id_OP
                    try {
                        if (flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").size() == 1)
                        {
                            flowInfo.getJSONObject("refOP").remove(refOP);
                        } else
                        {
                            flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").remove(index);
                        }

                        qt.setMDContent(assetB.getId(), qt.setJson("flowControl.objData." + i + ".refOP", flowInfo.getJSONObject("refOP")), Asset.class);

                        break;

                    } catch (Exception e) {
                        // here it must have an error flowcontrol
                    }
                    break;
                }
            }
        }
        if (!id_FS.equals("") && !assetS.getId().equals("none"))
        {
            for (int i = 0; i < assetS.getFlowControl().getJSONArray("objData").size(); i++)
            {
                JSONObject flowInfo = assetS.getFlowControl().getJSONArray("objData").getJSONObject(i);
                //adding 1 index
                if (flowInfo.getString("id").equals(id_FS) && isStart && refOP != null) {
                    // case 1: id_OP not exists in refOP, init
                    if (flowInfo.getJSONObject("refOP") == null)
                    {
                        flowInfo.put("refOP", new JSONObject());
                    }
                    if (id_OP.equals(""))
                    {
                        flowInfo.getJSONObject("refOP").put("id_OP", flowInfo.getString("id_O"));
                    }
                    // case 2: refOP not exists
                    if (flowInfo.getJSONObject("refOP").getJSONObject(refOP) == null)
                    {
                        flowInfo.getJSONObject("refOP").put(refOP, refOPInfo);
                    }
                    // any case, add this index to index[]
                    if (!flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").contains(index)) {
                        flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").add(index);
                        qt.setMDContent(assetS.getId(), qt.setJson("flowControl.objData." + i + ".refOP", flowInfo.getJSONObject("refOP")), Asset.class);
                    }
                    break;

                } // removing now here:
                else if (flowInfo.getString("id").equals(id_FS) && !isStart) {
                    // case 1: if index size == 1, remove the whole id_OP
                    try {
                        if (flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").size() == 1)
                        {
                            flowInfo.getJSONObject("refOP").remove(refOP);
                        } else
                        {
                            flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").remove(index);
                        }
                        qt.setMDContent(assetS.getId(), qt.setJson("flowControl.objData." + i + ".refOP", flowInfo.getJSONObject("refOP")), Asset.class);

                    } catch (Exception e) {
                        // here it must have an error flowcontrol
                    }
                    break;
                }
            }
        }
    }



    private void sumDura(String id_O, Integer index, LogFlow log) {

        JSONObject filt1 = qt.setJson("filtKey", "id_O", "method", "eq", "filtVal", id_O);
        JSONObject filt2 = qt.setJson("filtKey", "index", "method", "exact", "filtVal", index);

        JSONArray filterArray = qt.setArray(filt1, filt2);

        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            qt.filterBuilder(filterArray, queryBuilder);
            //System.out.println(queryBuilder);

            sourceBuilder.query(queryBuilder).size(100)
                    .aggregation(AggregationBuilders.sum("taStart").field("data.taStart"))
                    .aggregation(AggregationBuilders.sum("taFin").field("data.taFin"));
            SearchRequest searchRequest = new SearchRequest("action").source(sourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            JSONObject jsonResponse = (JSONObject) JSON.parse(searchResponse.toString());
            //System.out.println(jsonResponse);
            JSONObject jsonAgg = jsonResponse.getJSONObject("aggregations");
            Long taStart = jsonAgg.getJSONObject("sum#taStart").getLong("value");
            Long taFin = jsonAgg.getJSONObject("sum#taFin").getLong("value");
            long ms = taFin - taStart;

            // in case the last log has not loaded into ES, I force add it
            if (ms < 0)
            {
                ms = ms + DateUtils.getTimeStamp();
            } else if (ms > 100000000)
            { // in case it is cancel without restart
                ms = ms - DateUtils.getTimeStamp();
            }
            //System.out.println(ms);
            String time = qt.formatMs(ms);
            //System.out.println(time);
            log.setData(qt.setJson("tDur", ms));
            log.setZcndesc("任务总用时" + time);
            ws.sendWS(log);
        } catch (Exception e)
        {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_ES_GET_DATA_IS_NULL.getCode(), "没有关联订单");
        }

//        return retResult.ok(CodeEnum.OK.getCode(), time);
    }


    @Override
        public ApiResponse getRefOPList(String id_Flow, Boolean isSL, String id_C) {

            // 1. if flowControl.refOP in front end not exist, will call this to get all refOP info and
            // 2. use updateRefOP to write into flowControl REF

                String idF = "id";
                String idC = "id_C";
                //set to SL's search checking keys if the chatroom is Sales side
                if (isSL)
                {
                    idF = "id_FS";
                    idC = "id_CS";
                }
                JSONObject refOPIndexArray = new JSONObject();

                // Type = 1: msgList regular with stateChg filter,
                // 2: progress with no filter but only those in id_O+index
                //构建搜索条件

                JSONArray filtArray1 = qt.setESFilt(idC, id_C, "data.bcdStatus", 0);
                qt.setESFilt(filtArray1, idF, "eq", id_Flow);

                JSONArray filtArray2 = new JSONArray();
                qt.setESFilt(filtArray2, idF, "eq", id_Flow);
                qt.setESFilt(filtArray2, idC, "exact", id_C);
                qt.setESFilt(filtArray2, "data.bcdStatus", "contain", Arrays.asList("2", "9"));

                JSONArray result = qt.getES("action", filtArray1, 4000);
                JSONArray result2 = qt.getES("action", filtArray2, 4000);

                JSONObject refOPList = new JSONObject();
                // contentMap方法意义：比如lSProd 的id_P替换id，这样的意义，前端可以拿这个id去查Prod表

                for (int i = 0; i < result.size(); i++) {

                    String refOP = result.getJSONObject(i).getJSONObject("data").getString("refOP");

                    if (refOP != null && ! refOP.equals("") && refOPList.getInteger(refOP) == null && result.getJSONObject(i).getInteger("index") != null)
                    {
                        refOPList.put(refOP, 1);
                        refOPIndexArray.put(refOP, qt.setJson("refOP", refOP, "index", new JSONArray()));
                        refOPIndexArray.getJSONObject(refOP).getJSONArray("index").add(result.getJSONObject(i).getInteger("index"));


                    } else if (refOP != null && !refOP.equals("") && result.getJSONObject(i).getInteger("index") != null)
                    {
                        refOPList.put(refOP, refOPList.getInteger(refOP) + 1);
                        refOPIndexArray.getJSONObject(refOP).getJSONArray("index").add(result.getJSONObject(i).getInteger("index"));

                    }

                }
                for (int i = 0; i < result2.size(); i++) {
                    //System.out.println("result222"+result2.getJSONObject(i).getJSONObject("data").getString("refOP"));

                    String refOP = result2.getJSONObject(i).getJSONObject("data").getString("refOP");

                    if (refOP != null && ! refOP.equals("") && refOPList.getInteger(refOP) != null && result2.getJSONObject(i).getInteger("index") != null)
                    {
                        refOPList.put(refOP, refOPList.getInteger(refOP) - 1);
                        refOPIndexArray.getJSONObject(refOP).getJSONArray("index").remove(result2.getJSONObject(i).getInteger("index"));

                        if (refOPList.getInteger(refOP).equals(0))
                        {
                            refOPList.remove(refOP);
                            refOPIndexArray.remove(refOP);
                        }

                    }
                }

                Asset asset = qt.getConfig(id_C, "a-auth", "flowControl");
        for (int i = 0; i < asset.getFlowControl().getJSONArray("objData").size(); i++) {
            JSONObject flowInfo = asset.getFlowControl().getJSONArray("objData").getJSONObject(i);
            //adding 1 index
            if (flowInfo.getString("id").equals(id_Flow)) {
                qt.setMDContent(asset.getId(), qt.setJson("flowControl.objData." + i + ".refOP", refOPIndexArray), Asset.class);
                break;
            }
        }





        //System.out.println(refOPList);

                return retResult.ok(CodeEnum.OK.getCode(), refOPList);

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

//                JSONObject mapKey = new JSONObject();
//                mapKey.put(updatingGrp, grpData);
//                coupaUtil.updateOrderByListKeyVal(id_O, mapKey);

                qt.setMDContent(id_O, qt.setJson(updatingGrp, grpData), Order.class);

//                JSONObject mapKey2 = new JSONObject();
//                mapKey2.put("flowControl.objData."+index+".grpB", grpB);
//                coupaUtil.updateOrderByListKeyVal(id_A, mapKey2);

            return retResult.ok(CodeEnum.OK.getCode(), grpData);

        }


    private JSONObject getActionData(String oid, Integer index) {
        // 创建卡片信息存储集合
        // 调用方法并且获取请求结果
//        Order order = coupaUtil.getOrderByListKey(oid, Arrays.asList("info", "oItem", "action"));
        Order order = qt.getMDContent(oid, Arrays.asList("info", "oItem", "action"), Order.class);

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
            order.getOItem().getJSONArray("objCard").add("action");
            dbu.initAction(oItemArray);
            //System.out.println("result  action array init");
        }

        int counter = 0;
        for (int i = 0; i < actionArray.size(); i++)
        {
            if (actionArray.getJSONObject(i).getInteger("bcdStatus") == 2)
            {
                counter++;
            }
        }

        String id_FC = "";
        String id_FS = "";

        try {
            result.put("orderAction",qt.cloneObj(actionArray.getJSONObject(index)));
            result.put("orderOItem",qt.cloneObj(oItemArray.getJSONObject(index)));
        }catch (Exception e)
        {}
        try {
            id_FC = grpBGroup.getJSONObject(oItemArray.getJSONObject(index).getString("grpB")).getString("id_Flow");
        }catch (Exception e)
            {}
        try {
            id_FS = grpGroup.getJSONObject(oItemArray.getJSONObject(index).getString("grp")).getString("id_Flow");
        } catch (Exception e)
        {}

        result.put("oItemArray", oItemArray);
        result.put("actionArray", actionArray);
        result.put("progress", counter);
        result.put("grpBGroup",grpBGroup);
        result.put("grpGroup", grpGroup);
        result.put("id_FC", id_FC);
        result.put("id_FS", id_FS);
        result.put("info",qt.toJson(order.getInfo()));
        result.put("size", oItemArray.size());

        //System.out.println(result);

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
                this.activateThis(actData, id_O, index, myCompId, id_U, grpU, dep, wrdNU);
            }

        return retResult.ok(CodeEnum.OK.getCode(), "done");
    }

    private Integer activateThis(JSONObject actData, String id_O, Integer index, String myCompId, String id_U, String grpU, String dep, JSONObject wrdNU)
    {
        OrderOItem unitOItem = JSONObject.parseObject(JSON.toJSONString(actData.getJSONArray("oItemArray").getJSONObject(index)), OrderOItem.class);
        OrderAction unitAction = JSONObject.parseObject(JSON.toJSONString(actData.getJSONArray("actionArray").getJSONObject(index)), OrderAction.class);

        // 根据零件递归信息获取零件信息，并且制作日志
        unitAction.setBcdStatus(0);
        unitAction.setBisPush(1);

        qt.setMDContent(id_O, qt.setJson("action.objAction." + index, unitAction), Order.class);

        this.updateRefOP(actData.getJSONObject("info").getString("id_CB"), actData.getJSONObject("info").getString("id_C"),
                actData.getString("id_FC"), actData.getString("id_FS"), unitAction.getId_OP(), unitAction.getRefOP(), unitAction.getWrdNP(), index, true );

//      String logType = actData.getJSONObject("grpBGroup").getJSONObject(unitOItem.getGrpB()).getString("logType");

        LogFlow logLP = new LogFlow("action", actData.getString("id_FC"),
                actData.getString("id_FS"), "stateChg",
                id_U, grpU, unitOItem.getId_P(), unitOItem.getGrpB(), unitOItem.getGrp(),
                unitAction.getId_OP(), id_O, index, unitOItem.getId_CB(), unitOItem.getId_C(),
                "", dep, unitOItem.getWrdN().get("cn") +" 准备开始", 3, unitOItem.getWrdN(), wrdNU);
        logLP.setLogData_action(unitAction, unitOItem);
        logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");

        ws.sendWS(logLP);

        Integer totalSubCount = 0;

        // check if objSub > 0, if so, activate also next, if so(activate next)
        if (unitOItem.getObjSub() > 0)
        {
            Integer subCountWithin = this.activateThis(actData, id_O, index + 1, myCompId, id_U, grpU, dep, wrdNU);
            totalSubCount = unitOItem.getObjSub() + subCountWithin;
        }

        // check if next oItem (index + 1 + objSub(0) > size && has seq == 1
        Integer seqCheckIndex = index + 1 + totalSubCount;
        if (seqCheckIndex < actData.getInteger("size") && unitOItem.getId_P().equals(""))
        {
            String seqNext = actData.getJSONArray("oItemArray").getJSONObject(seqCheckIndex).getString("seq");
            if (seqNext.equals("1"))
            {
                this.activateThis(actData, id_O, seqCheckIndex, myCompId, id_U, grpU, dep, wrdNU);
            }
        }
        return totalSubCount;
    }

    // this special Pushing API is for Start all orders from casItemx if they are "materials"
    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse dgActivateAll(String id_O, String myCompId, String id_U, String grpU, String dep, JSONObject wrdNU) {

        Order orderMainData = qt.getMDContent(id_O, "casItemx", Order.class);
        List <Order> orderDataList = new ArrayList<>();
        JSONArray orderList = orderMainData.getCasItemx().getJSONObject(myCompId).getJSONArray("objOrder");


        for (Integer n = 0; n < orderList.size(); n++) {

            orderDataList.add(
                    qt.getMDContent(orderList.getJSONObject(n).getString("id_O"), Arrays.asList( "info","oItem", "action"), Order.class)

            );
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
            //System.out.println("orderData"+orderData);

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

//                                if (subAction.getPriority() == 1) {
                                if (subOItem.getWn0prior() == 1) {
                                    subAction.setBcdStatus(0);
                                    subAction.setBisPush(1);

                                    JSONObject mapKey = new JSONObject();
                                    mapKey.put("action.objAction." + unitAction.getSubParts().getJSONObject(k).getInteger("index"), subAction);
                                    qt.setMDContent(unitAction.getSubParts().getJSONObject(k).getString("id_O"), mapKey, Order.class);
                                    //System.out.println("unit " + subOItem.getGrpB() + subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()));
                                    String logType = subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()).getString("logType");

                                    this.updateRefOP(subOrderData.getJSONObject("info").getString("id_CB"), subOrderData.getJSONObject("info").getString("id_C"),
                                            subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), subAction.getId_OP(), subAction.getRefOP(), subAction.getWrdNP(), subAction.getIndex(), true );

                                    LogFlow logLP = new LogFlow(logType, subOrderData.getString("id_FC"),
                                            subOrderData.getString("id_FS"), "stateChg",
                                            id_U, grpU, subOItem.getId_P(), subOItem.getGrpB(), subOItem.getGrp(),
                                            id_O, unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                            unitAction.getSubParts().getJSONObject(k).getInteger("index"),
                                            subOItem.getId_CB(), subOItem.getId_C(),
                                            "", dep, subOItem.getWrdN().get("cn") + " 准备开始", 3, subOItem.getWrdN(), wrdNU);
                                    logLP.setLogData_action(subAction, subOItem);
                                    logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");

                                    ws.sendWS(logLP);



                                } else {
                                    //System.out.println("break @ " + k);
                                    break;
                                }
                            }
                        } else if (unitAction.getBmdpt() == 3 && unitAction.getSumPrev() == 0) {
                            //                // 根据零件递归信息获取零件信息，并且制作日志
                            unitAction.setBcdStatus(0);
                            unitAction.setBisPush(1);

                            qt.setMDContent(orderList.getJSONObject(i).getString("id_O"), qt.setJson("action.objAction." + j, unitAction), Order.class);

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

                            this.updateRefOP(myCompId, unitOItem.getId_C(),
                                    id_FC, id_FS, unitAction.getId_OP(), unitAction.getRefOP(), unitAction.getWrdNP(), unitAction.getIndex(), true );

                            String msgText = "[" + unitAction.getRefOP()+"] " + unitOItem.getWrdN().get("cn") + " 准备开始";
                            LogFlow logLP = new LogFlow(fcCheck.getString("logType"),
                                    id_FC,
                                    id_FS, "stateChg", id_U, grpU,
                                    unitOItem.getId_P(), unitOItem.getGrpB(), unitOItem.getGrp(), id_O, unitAction.getId_O(), unitAction.getIndex(),
                                    unitOItem.getId_CB(), unitOItem.getId_C(), "", dep, msgText, 3, unitOItem.getWrdN(), wrdNU);
                            logLP.setLogData_action(unitAction, unitOItem);
                            logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");


                            //System.out.println(logLP);
                            ws.sendWS(logLP);
                            // 发送日志
                        }
                    }
                }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "doneAll");
    }

    // for dgActivate if C=CB=Me, lST must be less than 10
    // if C!= CB, must be above 7
    // and if lST != 8, set to 8
    // I can stop the order and set everything to pause / finish/ cancel?

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse dgActivateSingle(String id_O, Integer i, String myCompId, String id_U, String grpU, String dep, JSONObject wrdNU) {

        Order orderMainData = qt.getMDContent(id_O, "casItemx", Order.class);
        List <Order> orderDataList = new ArrayList<>();
        JSONArray orderList = orderMainData.getCasItemx().getJSONObject(myCompId).getJSONArray("objOrder");


        for (Integer n = 0; n < orderList.size(); n++) {

            orderDataList.add(
                    qt.getMDContent(orderList.getJSONObject(n).getString("id_O"), Arrays.asList( "info","oItem", "action"), Order.class)

            );
            OrderInfo unitInfo = JSONObject.parseObject(JSON.toJSONString(orderDataList.get(n).getInfo()), OrderInfo.class);

            if (unitInfo.getLST() != 7) {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_ORDER_NEED_FINAL.getCode(), "");
            }
        }
        // After checked all lST, they are all 7 and ready to push

            Order orderData = orderDataList.get(i);
            JSONArray objAction = orderData.getAction().getJSONArray("objAction");
            JSONArray objOItem = orderData.getOItem().getJSONArray("objItem");
            JSONObject grpBGroup = orderData.getAction().getJSONObject("grpBGroup");
            JSONObject grpGroup = orderData.getAction().getJSONObject("grpGroup");
            //System.out.println("orderData"+orderData);

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

//                                if (subAction.getPriority() == 1) {
                            if (subOItem.getWn0prior() == 1) {
                                subAction.setBcdStatus(0);
                                subAction.setBisPush(1);

                                JSONObject mapKey = new JSONObject();
                                mapKey.put("action.objAction." + unitAction.getSubParts().getJSONObject(k).getInteger("index"), subAction);
                                qt.setMDContent(unitAction.getSubParts().getJSONObject(k).getString("id_O"), mapKey, Order.class);
                                //System.out.println("unit " + subOItem.getGrpB() + subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()));
                                String logType = subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()).getString("logType");
                                this.updateRefOP(myCompId, subOItem.getId_C(),
                                        subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), id_O, unitAction.getRefOP(), unitAction.getWrdNP(), unitAction.getSubParts().getJSONObject(k).getInteger("index"), true );

                                LogFlow logLP = new LogFlow(logType, subOrderData.getString("id_FC"),
                                        subOrderData.getString("id_FS"), "stateChg",
                                        id_U, grpU, subOItem.getId_P(), subOItem.getGrpB(), subOItem.getGrp(),
                                        id_O, unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                        unitAction.getSubParts().getJSONObject(k).getInteger("index"),
                                        subOItem.getId_CB(), subOItem.getId_C(),
                                        "", dep, subOItem.getWrdN().get("cn") + "准备开始", 3, subOItem.getWrdN(), wrdNU);
                                logLP.setLogData_action(subAction, subOItem);
                                logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");


                                ws.sendWS(logLP);

                            } else {
                                //System.out.println("break @ " + k);
                                break;
                            }
                        }
                    } else if (unitAction.getBmdpt() == 3 && unitAction.getSumPrev() == 0) {
                        //                // 根据零件递归信息获取零件信息，并且制作日志
                        unitAction.setBcdStatus(0);
                        unitAction.setBisPush(1);

                        qt.setMDContent(orderList.getJSONObject(i).getString("id_O"), qt.setJson("action.objAction." + j, unitAction), Order.class);

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

                        this.updateRefOP(myCompId, unitOItem.getId_C(),
                                id_FC, id_FS, id_O, unitAction.getRefOP(), unitAction.getWrdNP(), unitAction.getIndex(), true );

                        LogFlow logLP = new LogFlow(fcCheck.getString("logType"),
                                id_FC,
                                id_FS, "stateChg", id_U, grpU,
                                unitOItem.getId_P(), unitOItem.getGrpB(), unitOItem.getGrp(), id_O, unitAction.getId_O(), unitAction.getIndex(),
                                unitOItem.getId_CB(), unitOItem.getId_C(), "", dep, unitOItem.getWrdN().get("cn") +" 准备开始", 3, unitOItem.getWrdN(), wrdNU);
                        logLP.setLogData_action(unitAction, unitOItem);
                        logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");


                        //System.out.println(logLP);
                        ws.sendWS(logLP);
                        // 发送日志
                    }
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

        Integer index = 0;
        Integer prior = 1;
        JSONObject actData = this.getActionData(id_O, index - 1);
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
            prior = index > 0 ? actData.getJSONArray("oItemArray").getJSONObject(index - 1).getInteger("wn0prior") : 0;

        }

        // Adding oItem and Action
        OrderOItem unitOItem = new OrderOItem ("","",oItemData.getString("id_CP"),myCompId,myCompId,
                id_O,index,"","",
                oItemData.getString("grp"),oItemData.getString("grpB"),prior,oItemData.getString("pic"),
                oItemData.getInteger("lUT"),oItemData.getInteger("lCR"),oItemData.getDouble("wn2qtyneed"),
                oItemData.getDouble("wn4price"),oItemData.getJSONObject("wrdNP"),oItemData.getJSONObject("wrdN"),
                oItemData.getJSONObject("wrddesc"),oItemData.getJSONObject("wrdprep"));
        // All Task/Quest orders are AUTO
        unitOItem.setSeq("3");

        JSONArray prtPrev = new JSONArray();

        JSONArray allAction = actData.getJSONArray("actionArray"); // this is action.objAction []

        if (index - 1 >= 0) {
            prtPrev.add(qt.setJson("id_O", id_O, "index", index - 1));
            allAction.getJSONObject(index - 1).getJSONArray("prtNext").add(qt.setJson("id_O", id_O, "index", index));
        }

        OrderAction unitAction = new OrderAction(0,1,5,1,
                "","","",id_O, index, unitOItem.getRKey(),0, index == 0 ? 0 : 1,
                new JSONArray(),new JSONArray(),new JSONArray(),prtPrev,
                oItemData.getJSONObject("wrdNP"),oItemData.getJSONObject("wrdN"));

        allAction.add(unitAction);

        this.updateRefOP(myCompId, myCompId,
                id_FC, id_FS, id_O, "grpTask", oItemData.getJSONObject("wrdNP"), index, true );


        // Send a log
        LogFlow logLP = new LogFlow(logType,id_FC,
                id_FS,"stateChg", id_U,grpU,"",unitOItem.getGrpB(), "",id_O,id_O,index, myCompId,myCompId,
                oItemData.getString("pic"),dep,oItemData.getJSONObject("wrdN").getString("cn") +"准备开始",3,qt.cloneObj(oItemData.getJSONObject("wrdN")),wrdNU);
       logLP.setLogData_action(unitAction,unitOItem);
        logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");
        ws.sendWS(logLP);

//        Order order = qt.getMDContent(id_O, Collections.singletonList("oQc"), Order.class);
//        JSONObject oQc = order.getOQc();
//        int isNull = 0;
//        if (null == oQc) {
//            oQc = new JSONObject();
//            isNull = 1;
//        }
//        JSONArray objQc = oQc.getJSONArray("objQc");
        JSONObject objQcSon = new JSONObject();
        objQcSon.put("score",0);
        objQcSon.put("foCount",3);
//        if (null == objQc) {
//            isNull = 2;
//            objQc = new JSONArray();
//            for (int i = 0; i < index; i++) {
//                objQc.add(objQcSon);
//            }
//            objQc.add(objQcSon);
//            oQc.put("objQc",objQc);
//        }

//        if (isNull==0) {
            // append an OItem + ActionItem, save OItem and action
            qt.setMDContent(id_O, qt.setJson("action.objAction",allAction, "oItem.objItem."+index, unitOItem,"oQc.objQc."+index,objQcSon), Order.class);
            qt.setES("lSBOrder", qt.setESFilt("id_O", id_O), qt.setJson("tmd",DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate())));
//        } else if (isNull == 1) {
//            // append an OItem + ActionItem, save OItem and action
//            qt.setMDContent(id_O, qt.setJson("action.objAction",allAction, "oItem.objItem."+index, unitOItem,"oQc",oQc), Order.class);
//        } else {
//            qt.setMDContent(id_O, qt.setJson("action.objAction",allAction, "oItem.objItem."+index, unitOItem,"oQc.objQc",objQc), Order.class);
//        }


        return retResult.ok(CodeEnum.OK.getCode(), "done");
    }

    @Override
    public ApiResponse createTaskNew(String logType, String id, String id_FS, String id_O, String myCompId, String id_U, String grpU, String dep, JSONObject oItemData, JSONObject wrdNU) {
        Integer index = 0;
        Integer prior = 1;
        JSONObject actData = this.getActionData(id_O, index);
//        String id_FS = "";

        if (id_O.equals(""))
        {                // 返回操作失败结果
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_GET_ORDER_NULL.getCode(), "没有关联订单");
        }

        JSONArray id_Us;
        JSONArray defReply = null;
        if (logType.endsWith("SL"))
            logType = StringUtils.strip(logType, "SL");

        //System.out.println("logType:"+logType);
        if (!id.equals(id_FS)) {
            Asset asset = qt.getConfig(myCompId,"a-auth","flowControl");
            if (null == asset || null == asset.getFlowControl()) {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_GET_ORDER_NULL.getCode(), "asset异常");
            }
            JSONArray objData = asset.getFlowControl().getJSONArray("objData");
            id_Us = new JSONArray();
            if (null == id_FS || "".equals(id_FS)) {
                for (int i = 0; i < objData.size(); i++) {
                    JSONObject objDataSon = objData.getJSONObject(i);
                    String id_ONew = objDataSon.getString("id_O");
                    String type = objDataSon.getString("type");
                    if (logType.equals(type)&&id_O.equals(id_ONew)) {
                        JSONArray objUser = objDataSon.getJSONArray("objUser");
                        for (int j = 0; j < objUser.size(); j++) {
                            JSONObject objUserSon = objUser.getJSONObject(j);
                            id_Us.add(objUserSon.getString("id_U"));
                        }
                        if (null != objDataSon.getJSONArray("defReply")) {
                            defReply = objDataSon.getJSONArray("defReply");
                        }
                        break;
                    }
                }
            } else {
                for (int i = 0; i < objData.size(); i++) {
                    JSONObject objDataSon = objData.getJSONObject(i);
                    String idNew = objDataSon.getString("id");
                    if (id_FS.equals(idNew)) {
                        JSONArray objUser = objDataSon.getJSONArray("objUser");
                        for (int j = 0; j < objUser.size(); j++) {
                            JSONObject objUserSon = objUser.getJSONObject(j);
                            id_Us.add(objUserSon.getString("id_U"));
                        }
                        if (null != objDataSon.getJSONArray("defReply")) {
                            defReply = objDataSon.getJSONArray("defReply");
                        }
                        break;
                    }
                }
            }
        } else {
            id_Us = null;
        }
        //System.out.println("id_Us:");
        //System.out.println(JSON.toJSONString(id_Us));

//        // Converting id_FS/id_FC
//        if (logType.endsWith("SL"))
//        {
//            //logType = actionSL, FC = FS,
//            logType = StringUtils.strip(logType, "SL");
////            id_FS = id_FC;
////            id_FC = "";
//        }

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
                id_O,index,"","",
                oItemData.getString("grp"),oItemData.getString("grpB"),prior,oItemData.getString("pic"),
                oItemData.getInteger("lUT"),oItemData.getInteger("lCR"),oItemData.getDouble("wn2qtyneed"),
                oItemData.getDouble("wn4price"),oItemData.getJSONObject("wrdNP"),oItemData.getJSONObject("wrdN"),
                oItemData.getJSONObject("wrddesc"),oItemData.getJSONObject("wrdprep"));

        JSONArray prtPrev = new JSONArray();

        JSONArray allAction = actData.getJSONArray("actionArray"); // this is action.objAction []

        if (index - 1 >= 0) {
            prtPrev.add(qt.setJson("id_O", id_O, "index", index - 1));
            allAction.getJSONObject(index - 1).getJSONArray("prtNext").add(qt.setJson("id_O", id_O, "index", index));
        }

        OrderAction unitAction = new OrderAction(0,1,5,1,
                "","","",id_O, index, unitOItem.getRKey(),0, index == 0 ? 0 : 1,
                new JSONArray(),new JSONArray(),new JSONArray(),prtPrev,
                oItemData.getJSONObject("wrdNP"),oItemData.getJSONObject("wrdN"));
        unitAction.setId_Us(qt.setArray(id_U));
        allAction.add(unitAction);

        this.updateRefOP(myCompId, myCompId,
                id, id_FS, id_O, "grpTask", oItemData.getJSONObject("wrdNP"), index, true );

        boolean isId_Us = null != id_Us && id_Us.size() > 0;

        JSONObject objQcSon = new JSONObject();
        objQcSon.put("score",0);
        objQcSon.put("foCount",3);

        qt.setMDContent(id_O, qt.setJson("action.objAction",allAction, "oItem.objItem."+index, unitOItem,"oQc.objQc."+index,objQcSon), Order.class);

        String dateNow = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        if (isId_Us) {
            LogFlow logLPNew = new LogFlow(logType,id_FS,
                    id,"stateChg", id_U,grpU,"",unitOItem.getGrpB(), "",id_O,id_O,index, myCompId,myCompId,
                    oItemData.getString("pic"),dep,oItemData.getJSONObject("wrdN").getString("cn") + "准备开始",3,qt.cloneObj(oItemData.getJSONObject("wrdN")),wrdNU);
            logLPNew.setLogData_action(unitAction,unitOItem);
            logLPNew.setActionTime(DateUtils.getTimeStamp(), 0L, "push");
            id_Us.add(id_U);
            logLPNew.setId_Us(id_Us);
            logLPNew.setTmd(dateNow);
            ws.sendWS(logLPNew);
        }

        if (null != defReply) {
            JSONObject wr = new JSONObject();
            wr.put("cn","小银【系统】: 自动回复");
            JSONObject wrNew = new JSONObject();
            wrNew.put("cn","小银【系统】: 自动回复");
            String pic = "https://cresign-1253919880.cos.ap-guangzhou.myqcloud.com/pic_thumb/c439c86ed37f3cf9c183c1c6b84fa0a1.jpg";
            for (int i = 0; i < defReply.size(); i++) {
                JSONObject defReplySon = defReply.getJSONObject(i);
                if (defReplySon.getInteger("state") == 0) {
                    LogFlow logLPDef;
                    if (defReplySon.getInteger("type")==1) {
                        JSONArray img = defReplySon.getJSONArray("img");
                        for (int j = 0; j < img.size(); j++) {
                            logLPDef = new LogFlow("msg",id_FS,
                                    id,"pic", "",grpU,"",unitOItem.getGrpB(), ""
                                    ,id_O,id_O,index, myCompId,myCompId,
                                    pic,dep,"",3
                                    ,wr,wrNew);
                            logLPDef.getData().put("type","defReply");
                            logLPDef.getData().put("pic",img.getString(j));
                            logLPDef.getData().put("video","");
                            logLPDef.setId_Us(qt.setArray(id_U));
                            dateNow = DateUtils.getDateNowAddSecond(DateEnum.DATE_TIME_FULL.getDate(),dateNow,1);
                            logLPDef.setTmd(dateNow);
                            ws.sendWS(logLPDef);
                        }
                    } else if (defReplySon.getInteger("type") == 2) {
                        JSONArray id_P = defReplySon.getJSONArray("id_P");
                        for (int j = 0; j < id_P.size(); j++) {
                            logLPDef = new LogFlow("msg",id_FS,
                                    id,"prod", "",grpU,"",unitOItem.getGrpB(), ""
                                    ,id_O,id_O,index, myCompId,myCompId,
                                    pic,dep,"",3
                                    ,wr,wrNew);
                            logLPDef.getData().put("type","defReply");
                            logLPDef.getData().put("id_P",id_P.getString(j));
                            logLPDef.setId_Us(qt.setArray(id_U));
                            dateNow = DateUtils.getDateNowAddSecond(DateEnum.DATE_TIME_FULL.getDate(),dateNow,1);
                            logLPDef.setTmd(dateNow);
                            ws.sendWS(logLPDef);
                        }
                    } else {
                        logLPDef = new LogFlow("msg",id_FS,
                                id,"msg", "",grpU,"",unitOItem.getGrpB(), ""
                                ,id_O,id_O,index, myCompId,myCompId,
                                pic,dep,defReplySon.getString("msg"),3
                                ,wr,wrNew);
                        logLPDef.getData().put("type","defReply");
                        logLPDef.setId_Us(qt.setArray(id_U));
                        dateNow = DateUtils.getDateNowAddSecond(DateEnum.DATE_TIME_FULL.getDate(),dateNow,1);
                        logLPDef.setTmd(dateNow);
                        ws.sendWS(logLPDef);
                    }
                }
            }
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
            if (indexProb > 0) {
                priorProb = actData.getJSONArray("oItemArray").getJSONObject(indexProb - 1).getInteger("wn0prior");
            }
        }

        OrderAction orderAction = qt.cloneThis(probSet.get("orderAction"),OrderAction.class);
        OrderOItem orderOItem = qt.cloneThis(probSet.get("orderOItem"), OrderOItem.class);

        if (orderAction.getProb() == null)
        {
            orderAction.setProb(new JSONArray());
        }

        Integer sumProb = orderAction.getProb().size();

        String logTypeProb = probData.getString("logType");

        probData.put("index",indexProb);
        orderAction.getProb().add(probData);

        qt.setMDContent(id_O, qt.setJson("action.objAction."+index,orderAction), Order.class);

//       JSONArray prtPrev = new JSONArray();
//        java.lang.NullPointerException at com.cresign.purchase.service.impl.ActionServiceImpl.createQuest(ActionServiceImpl.java:1412) at com.cresign.purchase.service.impl.ActionServiceImpl$$FastClassBySpringCGLIB$$71b9cf0f.invoke(<generated>) at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218) at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:685) at com.cresign.purchase.service.impl.ActionServiceImpl$$EnhancerBySpringCGLIB$$2f684ac3.createQuest(<generated>) at com.cresign.purchase.controller.ActionController.createQuest$original$vSjocHQQ(ActionController.java:259) at com.cresign.purchase.controller.ActionController.createQuest$original$vSjocHQQ$accessor$5fjkzAdo(ActionController.java) at com.cresign.purchase.controller.ActionController$auxiliary$6PMXB0hl.call(Unknown Source) at
        JSONArray allAction = actData.getJSONArray("actionArray"); // this is action.objAction []
//
//        if (indexProb - 1 >= 0) {
//            prtPrev.add(qt.setJson("id_O", id_O, "index", indexProb - 1));
//            allAction.getJSONObject(indexProb - 1).getJSONArray("prtNext").add(qt.setJson("id_O", id_O, "index", indexProb));
//        }

        OrderOItem unitOItem = new OrderOItem ("",id_O,"",myCompId,myCompId,
                id_Prob,indexProb,"","",
                "1009",probData.getString("grpB"),priorProb, orderOItem.getPic(),
                1,1,1.0,
                0.0,probData.getJSONObject("wrdNP"),probData.getJSONObject("wrdN"),
                probData.getJSONObject("wrddesc"),probData.getJSONObject("wrdprep"));
        // this is also auto task
        unitOItem.setSeq("3");

        // Need to store id_O and Index into action Card so we will able to set prob finish
        JSONArray upPrnt = new JSONArray();
        JSONObject upPrntsData = new JSONObject();
        upPrntsData.put("id_O", id_O);
        upPrntsData.put("index", index);
        upPrntsData.put("probIndex",sumProb);
        upPrnt.add(upPrntsData);
        //System.out.println("probData"+probData);

        OrderAction unitAction = new OrderAction(0,1,7,1,probData.getString("id_OP"),probData.getString("refOP"),"",
//                id_O,index,unitOItem.getRKey(),0,indexProb, new JSONArray(),upPrnt,new JSONArray(),prtPrev,
                id_O,index,unitOItem.getRKey(),0,indexProb, new JSONArray(),upPrnt,new JSONArray(),new JSONArray(),
                probData.getJSONObject("wrdNP"),probData.getJSONObject("wrdN"));

        allAction.add(unitAction);

        this.updateRefOP(myCompId,myCompId,
                id_FC, id_FQ, probData.getString("id_OP"), "grpask",
                unitAction.getWrdNP(), index, true );


        LogFlow logProb = new LogFlow(logTypeProb,id_FC,
                id_FQ,"stateChg", id_U,grpU,orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),id_O, id_Prob,indexProb, myCompId,myCompId,
                probData.getString("pic"),dep,"准备解决",3,probData.getJSONObject("wrdN"),wrdNU);

        logProb.setLogData_action(unitAction,unitOItem);
        logProb.setActionTime(DateUtils.getTimeStamp(), 0L, "push");

//        logProb.setActionData(unitAction.getBisactivate(),unitAction.getBcdStatus(),unitAction.getId_Us(),1.0,
//                orderOItem.getId_P(), id_O,index,1,unitAction.getWrdNP(),unitOItem.getWrdN());

        JSONObject probResult = new JSONObject();
        probResult.put("action.objAction", allAction);
        probResult.put("oItem.objItem."+indexProb,unitOItem);

        qt.setMDContent(id_Prob, probResult, Order.class);

        ws.sendWS(logProb);

        return retResult.ok(CodeEnum.OK.getCode(), "done");

    }

    @Override
    public ApiResponse subStatusChange(String id_O, Integer index, Boolean isLink, Integer statusType, JSONObject tokData) {

        //2. get Order's oItem+action
            Order order = qt.getMDContent(id_O, "action", Order.class);
            JSONObject objAction = order.getAction().getJSONArray("objAction").getJSONObject(index);

            //-8. Loop check grpB==oItem(i).grpB and objAction(i).isPUshed == 1
            for (int i = 0; i < objAction.getJSONArray("subParts").size(); i++) {

                String subPartId_O = objAction.getJSONArray("subParts").getJSONObject(i).getString("id_O");
                Integer subPartIndex = objAction.getJSONArray("subParts").getJSONObject(i).getInteger("index");

                JSONObject subOrderData = this.getActionData(subPartId_O, subPartIndex);

                OrderOItem subOItem = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderOItem")), OrderOItem.class);
                OrderAction subAction = JSONObject.parseObject(JSON.toJSONString(subOrderData.get("orderAction")), OrderAction.class);

                Integer subStatus = subAction.getBcdStatus();
                Integer newStatus = 0;
                String newMsg = "";
//
//
                if (statusType.equals(0)) {
                    if (subStatus.equals(1) || subStatus.equals(-8)) {
//                        newStatus = 8;
                        newMsg = "全单暂停";
                        this.changeActionStatus("action", 8, "全单暂停", subPartIndex, subPartId_O, isLink,
                                subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), tokData);
                    }
                }
                else if (statusType.equals(1)) {
                    if (subStatus.equals(2) || subStatus.equals(9)) {
                        newStatus = 0;
                        newMsg = "全单准备";
                        this.changeActionStatus("action", 0, "全单准备", subPartIndex, subPartId_O, isLink,
                                subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), tokData);
                    }
                }
                else if (statusType.equals(2)) {
                    if (subStatus.equals(0) || subStatus.equals(1) || subStatus.equals(-8) || subStatus.equals(7) || subStatus.equals(8)) {
                        newMsg = "全单完成";
                        newStatus = 2;

                        this.changeActionStatus("action", 2, "全单完成", subPartIndex, subPartId_O, isLink,
                                subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), tokData);
                    }
                }
//
////                if (!newStatus.equals(0)) {
//                    //newStatus need to update mdb and send a log
//
//                    subAction.setBcdStatus(newStatus);
//
//                    qt.setMDContent(subPartId_O, qt.setJson("action.objAction." + subPartIndex + ".bcdStatus", newStatus), Order.class);
//
//                    LogFlow logLP = new LogFlow("action", subOrderData.getString("id_FC"),
//                            subOrderData.getString("id_FS"), "stateChg",
//                            tokData.getString("id_U"), tokData.getString("grpU"), subOItem.getId_P(), subOItem.getGrpB(), subOItem.getGrp(),
//                            subAction.getId_OP(), subPartId_O, subPartIndex,
//                            tokData.getString("id_C"), subOItem.getId_C(),
//                            "", tokData.getString("dep"), newMsg, 3, subOItem.getWrdN(), tokData.getJSONObject("wrdNU"));
//                    logLP.setLogData_action(subAction, subOItem);
//
//                    ws.sendWS(logLP);

//                }
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



    public ApiResponse taskToProd(JSONObject tokData, String id_O, Integer index, String id_P)

    {
        // send WS

        // get id_P data,
        JSONObject prodInfo = qt.getES("lBProd", qt.setESFilt("id_P", id_P)).getJSONObject(0);
        Order order = qt.getMDContent(id_O, Arrays.asList("oItem", "info", "action", "oStock","view"), Order.class);
        // update oItem
        JSONObject oItem = order.getOItem().getJSONArray("objItem").getJSONObject(index);
        qt.upJson(oItem, "id_P", id_P, "wrdN", prodInfo.getJSONObject("wrdN"), "wrddesc", prodInfo.getJSONObject("wrddesc"),
                "grpB", prodInfo.getJSONObject("grpB"), "grp", prodInfo.getJSONObject("grp"),
                "ref", prodInfo.getJSONObject("ref"), "refB", prodInfo.getJSONObject("refB"), "pic", prodInfo.getJSONObject("pic"),
                "lUT", prodInfo.getJSONObject("lUT"), "lCR", prodInfo.getJSONObject("lCR"), "wn4price", prodInfo.getJSONObject("wn4price"),
                "tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

        JSONObject listCol = new JSONObject();
        dbu.summOrder(order, listCol);
        // save oItem

        qt.setMDContent(id_O, qt.setJson("oItem", oItem), Order.class);
        qt.setES("lSBOrder", qt.setESFilt("id_O", id_O), listCol);

        JSONObject action = order.getAction().getJSONArray("objAction").getJSONObject(index);
        LogFlow log = new LogFlow(tokData, oItem, action, order.getInfo().getId_CB(), id_O, index, "action", "protocol", "改为任务流程: "+ prodInfo.getJSONObject("wrdN").getString("cn"), 3);
        ws.sendWS(log);

        return retResult.ok(CodeEnum.OK.getCode(), "");

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
    public Integer confirmOrder(JSONObject tokData,String id_O) {

        Order order = qt.getMDContent(id_O, "info",Order.class);
        String id_C = tokData.getString("id_C");

        // if I am both buyer and seller, internal order, whatever side I am, set it to both final
        if (order.getInfo().getId_C().equals(id_C) && order.getInfo().getId_C().equals(order.getInfo().getId_CB())) { //Check C== CB
            order.getInfo().setLST(7);
        } // I am id_CB Buyer
        else if (id_C.equals(order.getInfo().getId_CB())) { //if Seller is REAL
            if (qt.judgeComp(id_C, order.getInfo().getId_C()) == 1) {
                if (order.getInfo().getLST() == 6) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(7);
                } else {
                    order.getInfo().setLST(5);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(7);
            }
        } else // I am Seller
        { //if Buyer is REAL
            if (qt.judgeComp(id_C, order.getInfo().getId_CB()) == 1) {
                if (order.getInfo().getLST() == 5) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(7);
                } else {
                    order.getInfo().setLST(6);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(7);
            }
        }

        qt.setMDContent(id_O, qt.setJson("info.lST", order.getInfo().getLST()), Order.class);


        //Save MDB above, then update ES here

        qt.setES("lsborder", qt.setESFilt("id_O", id_O), qt.setJson("lST", order.getInfo().getLST()));

        String desc = tokData.getJSONObject("wrdNU").getString("cn") + "确认订单" ;

        String listType = order.getInfo().getId_CB() == tokData.getString("id_C") ? "lBOrder" : "lSOrder";

        //Writing update history
        LogFlow logUsage = new LogFlow();
        logUsage.setUsageLog(tokData, "confirm", desc, 2, id_O, listType,
                qt.setJson("cn", desc), listType == "lBOrder" ? order.getInfo().getGrpB(): order.getInfo().getGrp());
        ws.sendESOnly(logUsage);

        return order.getInfo().getLST();
//        return retResult.ok(CodeEnum.OK.getCode(), order.getInfo().getLST());
    }

    @Override
    public ApiResponse cancelOrder(String id_C,String id_O) {
//        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info"));
        Order order = qt.getMDContent(id_O, "info", Order.class);
        //Seller = 14, Buyer = 15, Both = 16

        // if I am both buyer and seller, internal order, whatever side I am, set it to both final
        if (order.getInfo().getId_C().equals(id_C) && order.getInfo().getId_C().equals(order.getInfo().getId_CB())) { //Check C== CB
            order.getInfo().setLST(16);
        } // I am id_CB Buyer
        else if (id_C.equals(order.getInfo().getId_CB())) { //if Seller is REAL
            if (qt.judgeComp(id_C, order.getInfo().getId_C()) == 1) {
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
            if (qt.judgeComp(id_C, order.getInfo().getId_CB()) == 1) {
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
//        JSONObject mapKey = new JSONObject();
//        mapKey.put("info.lST", order.getInfo().getLST());
//        coupaUtil.updateOrderByListKeyVal(id_O, mapKey);
        qt.setMDContent(id_O, qt.setJson("info.lST", order.getInfo().getLST()), Order.class);
        return retResult.ok(CodeEnum.OK.getCode(), order.getInfo());
    }


    @Override
    public ApiResponse dgConfirmOrder(JSONObject tokData, JSONArray casList) {

        // loop casItemx orders
        JSONArray result = new JSONArray();

        for (int i = 0; i < casList.size(); i++) {
            // check if orders are final, if need request Cancel, send request
            // if ok, open order
//            result.put("final",new JSONArray());
//            result.put("await", new JSONArray());
            String subOrderId = casList.getJSONObject(i).getString("id_O");
            //confirm it by confirmOrder function
            result.add(this.confirmOrder(tokData, subOrderId));
        }

        return retResult.ok(CodeEnum.OK.getCode(), result);

    }


    //setRefOP use in statusChg
    //params: id_C, id_FC/id_FS, flowControl's refOP{"id_OP":{refOP, wrdN, index{}}}
    //when start + index into fc.refOP
    //when stop, -index, if index == 0, - refOP.id_OP
    //no API is needed for vue



    //For change Dep/Grp @ grpBGroup,
    //工作任务换群专用
    //Loop thru all Flowcontrol items
    //Pick those that match grpB[], return Object of FlowControl item
    @Override
    public ApiResponse getFlowList(String id_C, String grpB)
    {
//        String assetId = qt.getId_A(id_C, "a-auth");
//        Asset assetData = coupaUtil.getAssetById(assetId, Collections.singletonList("flowControl"));
        Asset assetData = qt.getConfig(id_C, "a-auth", "flowControl");
        if (null == assetData) {
            // 返回操作失败结果
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ChatEnum.ERR_GET_DATA_NULL.getCode(), "获取数据为空");
        }
        JSONArray result = new JSONArray();

        JSONArray objData = assetData.getFlowControl().getJSONArray("objData");
        for (int i = 0; i < objData.size(); i++) {
            JSONArray grpList = objData.getJSONObject(i).getJSONArray("grpB");

            if (grpList != null && grpList.contains(grpB)) {
                //System.out.println("inHere "+grpB);
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


//        Order order = (Order) dbUtils.getMongoOneFields(id_O, Arrays.asList("action","oItem","oStock"), Order.class);
        Order order = qt.getMDContent(id_O, Arrays.asList("action","oItem","oStock"), Order.class);
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
//            //System.out.println("setId_O=" + setId_O);
//            //System.out.println("jsonId_O=" + jsonId_O);
//            //System.out.println("jsonAction=" + jsonAction);
//            //System.out.println("jsonUpId_OIndex=" + jsonUpId_OIndex);
//            //System.out.println("jsonExcludeId_OIndex=" + jsonExcludeId_OIndex);
        }
        recursionActionChart(setId_O, jsonId_O, jsonAction, jsonUpId_OIndex, jsonExcludeId_OIndex, num);
        //System.out.println("setId_O=" + setId_O);
        //System.out.println("jsonId_O=" + jsonId_O);
        //System.out.println("jsonAction=" + jsonAction);
        //System.out.println("jsonUpId_OIndex=" + jsonUpId_OIndex);
        //System.out.println("jsonExcludeId_O=" + jsonExcludeId_OIndex);
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
        //System.out.println("jsonResult=" + jsonResult);
        return retResult.ok(CodeEnum.OK.getCode(), jsonResult);

    }

    private void recursionActionChart(HashSet setId_O, JSONObject jsonUpId_O, JSONObject jsonAction, JSONObject jsonUpId_OIndex, JSONObject jsonExcludeId_OIndex, Integer num) {

//        List<Order> orders = (List<Order>) dbUtils.getMongoListFields(setId_O, Arrays.asList("action","oItem","oStock"), Order.class);

        List<Order> orders = (List<Order>) qt.getMDContentMany(setId_O, Arrays.asList("action","oItem","oStock"), Order.class);
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
                    qt.errPrint("arrayObj", null, arrayObjAction, index, id_O);
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
