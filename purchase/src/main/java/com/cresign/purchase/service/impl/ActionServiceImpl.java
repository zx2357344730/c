package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.ActionService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.*;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.*;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        JSONObject aCollect = new JSONObject();
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

                dbu.updateRefOP2(aCollect, order.getInfo().getId_CB(), order.getInfo().getId_C(),
                        grpBOld.getString("id_Flow"), "", objAction.getJSONObject(i).getString("id_OP"),
                        objAction.getJSONObject(i).getString("refOP"), objAction.getJSONObject(i).getJSONObject("wrdNP"),
                        id_O, i, false );

                dbu.updateRefOP2(aCollect, order.getInfo().getId_CB(), order.getInfo().getId_C(),
                        grpBNew.getString("id_Flow"), "", objAction.getJSONObject(i).getString("id_OP"),
                        objAction.getJSONObject(i).getString("refOP"), objAction.getJSONObject(i).getJSONObject("wrdNP"),
                        id_O, i, true );
            }
        }
        dbu.setMDRefOP(aCollect);

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
        JSONObject aCollect = new JSONObject();
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

                dbu.updateRefOP2(aCollect,order.getInfo().getId_CB(), order.getInfo().getId_C(),
                        "", grpBOld.getString("id_Flow"), objAction.getJSONObject(i).getString("id_OP"),
                        objAction.getJSONObject(i).getString("refOP"), objAction.getJSONObject(i).getJSONObject("wrdNP"),
                        id_O, i, false );

                dbu.updateRefOP2(aCollect,order.getInfo().getId_CB(), order.getInfo().getId_C(),
                        "", grpBNew.getString("id_Flow"), objAction.getJSONObject(i).getString("id_OP"),
                        objAction.getJSONObject(i).getString("refOP"), objAction.getJSONObject(i).getJSONObject("wrdNP"),
                        id_O, i, true );
            }
        }
        dbu.setMDRefOP(aCollect);

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
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "这任务没有关闭");
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
        logL.setActionTime(DateUtils.getTimeStamp(), 0L, "repush");

        logL.getData().put("bcdStatus", 0);
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
//        qt.pushMDContent(id_O, "action.objAction." + index + ".id_Us", id_U, Order.class);
    }

    private void removeFavRecent(JSONArray idRemove, String id_O, Integer index)
    {
        JSONObject jsonFav = qt.setJson("id_O", id_O, "index", index);
        for (int i = 0; i < idRemove.size(); i++) {
            qt.pullMDContent(idRemove.getString(i), "fav.objFav", jsonFav, User.class);
//            id_UA.remove(idRemove.getString(i));
        }
//        qt.setMDContent(id_O, qt.setJson("action.objAction." + index + ".arrUA", id_UA), Order.class);
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
        public JSONObject changeActionStatus(JSONObject assetCollection, String logType, Integer status, String msg,
                                             Integer index, String id_O, Boolean isLink,
                                             String id_FC, String id_FS, JSONObject tokData) {

                JSONObject actData = this.getActionData(id_O, index);
                Boolean isPublic = false;

                if (assetCollection == null)
                {
                    assetCollection = new JSONObject();
                    isPublic = true;
                }

                OrderOItem orderOItem = qt.jsonTo(actData.get("orderOItem"), OrderOItem.class);
                OrderAction orderAction = qt.jsonTo(actData.get("orderAction"), OrderAction.class);
                JSONObject orderInfo = actData.getJSONObject("info");


                 // [E1323] xxxx产品 - 工序 " " 拼接zcndesc
                String wrdNP = orderOItem.getWrdNP() == null ? "" : (orderOItem.getWrdNP().getString("cn") +  " - " );
                String prefixTaskName = orderAction.getRefOP().equals("") ? "" :  "[" + orderAction.getRefOP() + "] ";
                String taskName = prefixTaskName + wrdNP + orderOItem.getWrdN().getString("cn") + " ";

                // 根据下标获取递归信息

                // 判断新的状态和旧状态如果一样, 该操作已被处理, 两人同时有用, 防止重复按
                if (orderAction.getBcdStatus().equals(status)) {
                    // 抛出操作成功异常
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "该操作已被处理");
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
            Integer lDG = actData.getInteger("lDG");
            JSONObject objCount = actData.getJSONObject("objCount");
            JSONObject oDateOld = actData.getJSONObject("oDate");
            JSONObject setODate = null;
            // 判断属于什么操作
                switch (status) {
                    case 0: // ready
                        message = taskName + "[准备开始] " + msg;
                        orderAction.setBcdStatus(status);
                        isStateChg = true;
//                        if (lDG == 1 && orderAction.getBmdpt()==3) {
//                            lDG = 3;
//                            mapKey.put("action.lDG",lDG);
//                            qt.setMDContent(orderAction.getId_OP(),qt.setJson("action.lDG",3), Order.class);
//                        }
                        break;
                    case 1:
                        // Start an OItem, DG just start, Task just start, Quest start + update Prob
                        if (orderAction.getBcdStatus() != 0) {
                            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经开始过了");
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

//                        order = qt.getMDContent(orderAction.getId_OP(), "oDate", Order.class);
//                        if (null != order && null != order.getODate() && null != order.getODate().getJSONObject("objDate")) {
//                            JSONObject objDate = order.getODate().getJSONObject("objDate");
//                            JSONArray dateArray = objDate.getJSONArray(orderAction.getId_O());
//                            if (null != dateArray && dateArray.size() > 0) {
//                                boolean isTrue = false;
//                                for (int i = 0; i < dateArray.size(); i++) {
//                                    JSONObject date = dateArray.getJSONObject(i);
//                                    if (Objects.equals(date.getInteger("index"), orderAction.getIndex())) {
//                                        date.put("taPStart",(System.currentTimeMillis()/1000));
//                                        dateArray.set(i,date);
//                                        isTrue = true;
//                                        break;
//                                    }
//                                }
//                                if (isTrue) {
//                                    qt.setMDContent(orderAction.getId_OP(),qt.setJson("oDate.objDate."+orderAction.getId_O()
//                                            ,dateArray), Order.class);
//                                }
//                            }
//                        }

                        if (null != oDateOld && null != oDateOld.getJSONArray("objData")) {
                            setODate = new JSONObject();
                            JSONArray objData = oDateOld.getJSONArray("objData");
                            if (objData.size() > orderAction.getIndex()) {
                                JSONObject data = objData.getJSONObject(orderAction.getIndex());
                                if (null != data) {
                                    data.put("taStart",(System.currentTimeMillis()/1000));
                                    objData.set(orderAction.getIndex(),data);
                                    setODate.put("objData",objData);
                                }
                            }
                        } else {
                            setODate = new JSONObject();
                            JSONArray objData = new JSONArray();
                            for (int i = 0; i <= orderAction.getIndex(); i++) {
                                objData.add(qt.setJson("index",i));
                            }
                            if (objData.size() > orderAction.getIndex()) {
                                JSONObject data = objData.getJSONObject(orderAction.getIndex());
                                if (null != data) {
                                    data.put("taStart",(System.currentTimeMillis()/1000));
                                    objData.set(orderAction.getIndex(),data);
//                                data.put("taStart",(System.currentTimeMillis()/1000));
                                    setODate.put("objData",objData);
                                }
                            }
                        }
                        if (lDG == 1 && orderAction.getBmdpt()==3) {
                            lDG = 3;
                            mapKey.put("action.lDG",lDG);
                            String dateNow = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
                            qt.setMDContent(orderAction.getId_OP(),qt.setJson("action.lDG",3,"oDate.taStart",dateNow), Order.class);
                            qt.setES("lsborder", qt.setESFilt("id_O", orderAction.getId_OP()), qt.setJson("lDG",lDG,"taStart", dateNow));
                        }
                        break;
                    case 2:                    // 2 = finish, set qtyfin setNextDesc
//                        if (orderAction.getBcdStatus() != 2 && orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3 && orderAction.getBcdStatus() != -8 && orderAction.getBcdStatus() != 7) {
//                            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经处理了");
//                        }
                        if (orderAction.getSumChild() > 0 && orderAction.getBmdpt() == 2 && isLink) {
                            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_PROD_RECURRED.getCode(), "还有子工序没完成，不能完成");
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

//                        order = qt.getMDContent(orderAction.getId_OP(), "oDate", Order.class);
//                        if (null != order && null != order.getODate() && null != order.getODate().getJSONObject("objDate")) {
//                            JSONObject objDate = order.getODate().getJSONObject("objDate");
//                            JSONArray dateArray = objDate.getJSONArray(orderAction.getId_O());
//                            if (null != dateArray && dateArray.size() > 0) {
//                                boolean isTrue = false;
//                                for (int i = 0; i < dateArray.size(); i++) {
//                                    JSONObject date = dateArray.getJSONObject(i);
//                                    if (Objects.equals(date.getInteger("index"), orderAction.getIndex())) {
//                                        date.put("taPFinish",(System.currentTimeMillis()/1000));
//                                        dateArray.set(i,date);
//                                        isTrue = true;
//                                        break;
//                                    }
//                                }
//                                if (isTrue) {
//                                    qt.setMDContent(orderAction.getId_OP(),qt.setJson("oDate.objDate."+orderAction.getId_O()
//                                            ,dateArray), Order.class);
//                                }
//                            }
//                        }
                        if (null != oDateOld && null != oDateOld.getJSONArray("objData")) {
                            setODate = new JSONObject();
                            JSONArray objData = oDateOld.getJSONArray("objData");
                            if (objData.size() > orderAction.getIndex()) {
                                JSONObject data = objData.getJSONObject(orderAction.getIndex());
                                if (null != data && null != data.getLong("taStart")) {
                                    long taFin = System.currentTimeMillis() / 1000;
                                    long wntaDurtot = taFin - data.getLong("taStart");
                                    data.put("taFin",taFin);
                                    data.put("wntaDurtot",wntaDurtot);
                                    objData.set(orderAction.getIndex(),data);
                                    setODate.put("objData",objData);
                                }
                            }
                        }
                        lDG = setActCount(objCount, orderAction.getBmdpt(), lDG,mapKey,orderAction.getId_OP());
                        break;
                    case -2:                    // 2 = finish, set qtyfin setNextDesc
//                        if (orderAction.getBcdStatus() != 2 && orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3 && orderAction.getBcdStatus() != -8 && orderAction.getBcdStatus() != 7) {
//                            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经处理了");
//                        }
                        if (orderAction.getSumChild() > 0 && orderAction.getBmdpt() == 2 && isLink) {
                            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_PROD_RECURRED.getCode(), "还有子工序没完成，不能完成");
                        }

                        orderAction.setBcdStatus(2);

                        message = taskName + "[已完成] " + msg;
                        isStateChg = true;
                        duraType = "allEnd";

                        if (null != oDateOld && null != oDateOld.getJSONArray("objData")) {
                            setODate = new JSONObject();
                            JSONArray objData = oDateOld.getJSONArray("objData");
                            if (objData.size() > orderAction.getIndex()) {
                                JSONObject data = objData.getJSONObject(orderAction.getIndex());
                                if (null != data && null != data.getLong("taStart")) {
                                    long taFin = System.currentTimeMillis() / 1000;
                                    long wntaDurtot = taFin - data.getLong("taStart");
                                    data.put("taFin",taFin);
                                    data.put("wntaDurtot",wntaDurtot);
                                    objData.set(orderAction.getIndex(),data);
                                    setODate.put("objData",objData);
                                }
                            }
                        }
                        lDG = setActCount(objCount, orderAction.getBmdpt(), lDG,mapKey,orderAction.getId_OP());
                        break;
                    case 3:
                    case -8: // resume OItem
                        if (orderAction.getBcdStatus() != 8) {
//                            return retResult.ok(ErrEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");

                            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能开始");
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
                        message = tokData.getJSONObject("wrdNReal").getString("cn") + "[加入成功] " + orderOItem.getWrdN().getString("cn");
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

                        message = tokData.getJSONObject("wrdNReal").getString("cn") + "[退出成功] " + orderOItem.getWrdN().getString("cn");
                        status = orderAction.getBcdStatus();

                        res.put("isJoin", 0);
                        res.put("id_Us", orderAction.getId_Us());
                        duraType = "end";
                        isStateChg = true;
                        break;
                    case 7:
                        if ((orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != -8)
                                || orderAction.getBisactivate() == 4) {
                            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能操作");
                        }
                        orderAction.setBisactivate(4);
                        message = taskName + "[继续下一个]" + msg;

                        break;
                    case 8: // pause
                        if (orderAction.getBcdStatus() != 1 && orderAction.getBcdStatus() != 3
                                && orderAction.getBcdStatus() != -8) {
                            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "不能");
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

                        if (null != oDateOld && null != oDateOld.getJSONArray("objData")) {
                            setODate = new JSONObject();
                            JSONArray objData = oDateOld.getJSONArray("objData");
                            if (objData.size() > orderAction.getIndex()) {
                                JSONObject data = objData.getJSONObject(orderAction.getIndex());
                                if (null != data && null != data.getLong("taStart")) {
                                    long taFin = System.currentTimeMillis() / 1000;
                                    long wntaDurtot = taFin - data.getLong("taStart");
                                    data.put("taFin",taFin);
                                    data.put("wntaDurtot",wntaDurtot);
                                    objData.set(orderAction.getIndex(),data);
                                    setODate.put("objData",objData);
                                }
                            }
                        }
                        lDG = setActCount(objCount, orderAction.getBmdpt(), lDG,mapKey,orderAction.getId_OP());
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
                        dbu.updateRefOP2(assetCollection, actData.getJSONObject("info").getString("id_CB"), actData.getJSONObject("info").getString("id_C"),
                                id_FC, id_FS, orderAction.getId_OP(), orderAction.getRefOP(), orderAction.getWrdNP(), orderOItem.getId_O(), orderAction.getIndex(), false );
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
                        //if resume, check previous => wntWait / wntProb
                        //if start, check previous(0) pushed time => wntWait

                    } else if (duraType.equals("end") || duraType.equals("pause") || duraType.equals("allEnd")) {
                        logL.setActionTime(0L, DateUtils.getTimeStamp(), duraType);
                        //if pause, check previous => wntDur
                        //if allEnd, check previous => wntDur

                    }

                    qt.errPrint("logL is", logL);
                    ws.sendWS(logL);
                }

                long time = 0;

                // setup User's Fav card
                if (duraType.equals("start"))
                {
                    this.setFavRecent(tokData.getString("id_C"), tokData.getString("id_U"), id_O, index, id_FC, id_FS,
                            orderOItem.getWrdN(), orderOItem.getPic());


                }

                else if (duraType.equals("end")) {
                    this.removeFavRecent(qt.setArray(tokData.getString("id_U")), id_O, index);
                    orderAction.getId_Us().remove(tokData.getString("id_U"));
                }
                else if (duraType.equals("allEnd"))
                {
                    this.removeFavRecent(orderAction.getId_Us(), id_O, index);
                    orderAction.setId_Us(new JSONArray());

//                    LogFlow logSale = new LogFlow();
//                    logSale.setSaleLog(tokData, orderInfo.getString("id_CB"), "finish", "执行完毕", 4, id_O, id_O, "",
//                            orderInfo.getJSONObject("wrdN"), orderOItem.getGrpB());
//                    ws.sendWS(logSale);

                    LogFlow logDURA = new LogFlow("action", id_FC,
                        id_FS, "userStat", tokData.getString("id_U"), tokData.getString("grpU"), orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),
                        orderAction.getId_OP(), id_O, index, compId, orderOItem.getId_C(), "", tokData.getString("dep"), "", 3, orderOItem.getWrdN(), tokData.getJSONObject("wrdNU"));

                    // I am just fixing this by putting the "finish" time into calculation and ignoring the last log
                    time = this.sumDura(id_O, index, logDURA);
                }


                mapKey.put("action.objAction." + index, orderAction);

                if (null != listCol.getInteger("lST")) {
                    // set oDate if card exists

                    if (actData.getJSONObject("oDate") != null)
                    {
                        JSONObject oDate = actData.getJSONObject("oDate");
                        String dateNow = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
                        if ((lDG == 1 || lDG == 3) && status == 1 && "".equals(oDate.getString("taStart"))) {
                            oDate.put("taStart", dateNow);
                            listCol.put("taStart", dateNow);
                            LogFlow usageLog = new LogFlow();
                            usageLog.setUsageLog(tokData, "lSTchg", "订单开始执行", 3, id_O,
                                    orderOItem.getId_CB().equals(tokData.getString("id_C")) ? "lBOrder" : "lSOrder", actData.getJSONObject("info").getJSONObject("wrdN"),"1000", "");
                        }
                        if (lDG == 6 && "".equals(oDate.getString("taFin"))) {
                            long wntDur;
                            if (!"".equals(oDate.getString("taStart"))) {
                                String taStart = oDate.getString("taStart");
                                long taStartTimeStamp = getTimeStamp(taStart);
                                long taFinTimeStamp = getTimeStamp(dateNow);
                                wntDur = taFinTimeStamp - taStartTimeStamp;
                            } else {
                                wntDur = time;
                            }
                            oDate.put("wntDur", wntDur);
                            oDate.put("taFin", dateNow);
                            listCol.put("taFin", dateNow);
                            listCol.put("wntDur", wntDur);
                            LogFlow usageLog = new LogFlow();
                            usageLog.setUsageLog(tokData, "lSTchg", "订单全部完成", 3, id_O,
                                    orderOItem.getId_CB().equals(tokData.getString("id_C")) ? "lBOrder" : "lSOrder", actData.getJSONObject("info").getJSONObject("wrdN"),"1000", "");
                        }
//                        if (listCol.getInteger("lST") == 8 ) {
//                            oDate.put("taStart", dateNow);
//                            listCol.put("taStart", dateNow);
//                        }
//                            else {
//                            oDate.put("taFin", dateNow);
//                            oDate.put("wntDur", time);
//                            listCol.put("taFin", dateNow);
//                        }
                        if (null != setODate) {
                            for (String key : setODate.keySet()) {
                                oDate.put(key,setODate.getJSONArray(key));
                            }
                        }
                        mapKey.put("oDate",oDate);
                    } else {
                        if (null != setODate) {
                            for (String key : setODate.keySet()) {
                                mapKey.put("oDate."+key,setODate.getJSONArray(key));
                            }
                        }
                    }
                    qt.setES("lsborder", qt.setESFilt("id_O", id_O), listCol);
                    // send a log to record order status change
//                    LogFlow usageLog = new LogFlow();
//                    usageLog.setUsageLog(tokData, "lSTchg", listCol.getInteger("lST") == 8 ? "订单开始执行" : "订单全部完成", 3, id_O,
//                            orderOItem.getId_CB().equals(tokData.getString("id_C")) ? "lBOrder" : "lSOrder", actData.getJSONObject("info").getJSONObject("wrdN"),"1000", "");
//                    ws.sendWS(usageLog);
                } else {
                    if (null != setODate) {
                        for (String key : setODate.keySet()) {
                            mapKey.put("oDate."+key,setODate.getJSONArray(key));
                        }
                    }
                }

            //*** here we update id_O, then we update status of the order
                qt.setMDContent(id_O, mapKey, Order.class);


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
                        qt.setMDContent(id_O, qt.setJson("oStock", oStockCheck.getOStock()), Order.class);
                        qt.setES("lsborder", qt.setESFilt("id_O", id_O), listCol);

                        LogFlow log = new LogFlow(tokData, oStockCheck.getOItem(), oStockCheck.getAction(), "", id_O, index,
                                "assetflow", "qtyChg", orderAction.getRefOP() + "-" + orderOItem.getWrdN().getString("cn") +
                                " 完成了 " + qtyAdding, 3);

                        Double price = orderOItem.getWn4price() == null ? 0.0: orderOItem.getWn4price();
                        log.setLogData_assetflow(qtyAdding, price, "","");

                        ws.sendWS(log);

                    }
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
                        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_AN_ERROR_OCCURRED.getCode(), "不能开始," + e);
                    }
                } else if ((status == 2 && orderAction.getBisactivate() != 4) || status == 7) {
                    // activate = 4 means Skip = already pushed Next, status == -2 then skip upNext
                    if (orderAction.getUpPrnts().size() == 0 && orderOItem.getId_P().equals("")) {
                        //Here for noP, DO NOT check parent, it will blow up
                        this.updateNext(assetCollection, orderAction, tokData);
                    }
                    else {
                        //for regular DG, we will go check our parent first
                        //then push it together with myself because I am always the first Item
                        this.updateParent(assetCollection, orderAction, tokData);
                    }
                } else if (status == 1 && orderAction.getBmdpt() == 4)
                {
                    // here I must check all my subParts, and see if they are prtPrev.size == 0
                    // if so, push
                    this.updateSon(assetCollection, orderAction, tokData);
                }

                if (isPublic)
                {
                    dbu.setMDRefOP(assetCollection);
                }
            // 抛出操作成功异常
            return res;
//            return retResult.ok(CodeEnum.OK.getCode(), res);
        }

    private void updateSon(JSONObject assetCollection, OrderAction orderAction, JSONObject tokData)
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

                    dbu.updateRefOP2(assetCollection, orderOItem1.getId_CB(), orderOItem1.getId_C(),
                            actData.getString("id_FC"),
                            actData.getString("id_FS"), orderAction1.getId_OP(), orderAction1.getRefOP(),
                            orderAction1.getWrdNP(), sonId, sonIndex, true);


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

    public int setActCount(JSONObject objActionCount,int bmdpt,int lDG,JSONObject mapKey,String id_OP){
        Order order = qt.getMDContent(id_OP, qt.strList("action","oDate"), Order.class);
        if (null != order && null != order.getAction() && null != order.getAction().getJSONObject("objCount")) {
            JSONObject objCount = order.getAction().getJSONObject("objCount");
            addTaPt(bmdpt, objCount);
            Integer teBmdPt1 = objCount.getInteger("wnePT1");
            Integer teBmdPt2 = objCount.getInteger("wnePT2");
            Integer teBmdPt3 = objCount.getInteger("wnePT3");
            Integer taBmdPt1 = objCount.getInteger("wnaPT1");
            Integer taBmdPt2 = objCount.getInteger("wnaPT2");
            Integer taBmdPt3 = objCount.getInteger("wnaPT3");
            boolean isSetLDG = false;
            if (teBmdPt3 != 0) {
                if (teBmdPt3.equals(taBmdPt3)) {
                    lDG = 4;
                    isSetLDG = true;
                }
            } else {
                lDG = 4;
                isSetLDG = true;
            }
            if (teBmdPt2 != 0) {
                if (teBmdPt2.equals(taBmdPt2)) {
                    lDG = 5;
                    isSetLDG = true;
                }
            } else {
                lDG = 5;
                isSetLDG = true;
            }
            if (teBmdPt1 != 0) {
                if (teBmdPt1.equals(taBmdPt1)) {
                    lDG = 6;
                    isSetLDG = true;
                }
            } else {
                lDG = 6;
                isSetLDG = true;
            }
            JSONObject setJson = qt.setJson("action.objCount", objCount);
            if (isSetLDG) {
                setJson.put("action.lDG",lDG);
                JSONObject col = qt.setJson("lDG", lDG);
                if (lDG == 6) {
                    String dateNow = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
                    if (null != order.getODate()) {
                        JSONObject oDate = order.getODate();
                        if (oDate.containsKey("taStart") && !"".equals(oDate.getString("taStart"))) {
                            String taStart = oDate.getString("taStart");
                            long taStartTimeStamp = getTimeStamp(taStart);
                            long taFinTimeStamp = getTimeStamp(dateNow);
                            long wntDur = taFinTimeStamp - taStartTimeStamp;
                            setJson.put("oDate.wntDur",wntDur);
                            col.put("wntDur",wntDur);
                        } else {
                            setJson.put("oDate.wntDur",0);
                            col.put("wntDur", 0);
                        }
                    } else {
                        setJson.put("oDate.wntDur",0);
                        col.put("wntDur", 0);
                    }
                    col.put("taFin", dateNow);
                    setJson.put("oDate.taFin",dateNow);
                }
                qt.setES("lsborder", qt.setESFilt("id_O", id_OP), col);
            }
            qt.setMDContent(id_OP,setJson, Order.class);
        }
        if (null != objActionCount) {
            addTaPt(bmdpt, objActionCount);
            Integer teBmdPt1 = objActionCount.getInteger("wnePT1");
            Integer teBmdPt2 = objActionCount.getInteger("wnePT2");
            Integer teBmdPt3 = objActionCount.getInteger("wnePT3");
            Integer taBmdPt1 = objActionCount.getInteger("wnaPT1");
            Integer taBmdPt2 = objActionCount.getInteger("wnaPT2");
            Integer taBmdPt3 = objActionCount.getInteger("wnaPT3");
            boolean isSetLDG = false;
            if (teBmdPt3 != 0) {
                if (teBmdPt3.equals(taBmdPt3)) {
                    lDG = 4;
                    isSetLDG = true;
                }
            } else {
                lDG = 4;
                isSetLDG = true;
            }
            if (teBmdPt2 != 0) {
                if (teBmdPt2.equals(taBmdPt2)) {
                    lDG = 5;
                    isSetLDG = true;
                }
            } else {
                lDG = 5;
                isSetLDG = true;
            }
            if (teBmdPt1 != 0) {
                if (teBmdPt1.equals(taBmdPt1)) {
                    lDG = 6;
                    isSetLDG = true;
                }
            } else {
                lDG = 6;
                isSetLDG = true;
            }
            mapKey.put("action.objCount",objActionCount);
            if (isSetLDG) {
                mapKey.put("action.lDG",lDG);
            }
        }
        return lDG;
    }

    public long getTimeStamp(String dateString){
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate());
//        String dateString = "2024/08/14 00:00:00";
        try {
            Date date = sdf.parse(dateString);
            long timeStamp = date.getTime();
            return timeStamp / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public String getTimeDate(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate());
        Date date = new Date((timestamp*1000));
        return sdf.format(date);
    }

    public void addTaPt(int bmdpt,JSONObject objActionCount){
        switch (bmdpt){
            case 1:
                objActionCount.put("wnaPT1",objActionCount.getInteger("wnaPT1")+1);
                break;
            case 2:
            case 4:
                objActionCount.put("wnaPT2",objActionCount.getInteger("wnaPT2")+1);
                break;
            case 3:
                objActionCount.put("wnaPT3",objActionCount.getInteger("wnaPT3")+1);
                break;
        }
    }

    private void updateNext(JSONObject assetCollection, OrderAction orderAction, JSONObject tokData)
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
                    qt.errPrint("what is", orderAction1, nextId, nextIndex);
                    if (orderAction1.getBcdStatus() == 100) {
                        // 设置该产品的上一个数量减一
                        orderAction1.setSumPrev(orderAction1.getSumPrev() - 1);

                        // 判断下一个产品子产品是否为0, // both sumXXX are 0, send log
                        if (orderAction1.getSumPrev() <= 0 && (orderAction1.getSubParts().size() == 0 || orderAction1.getBmdpt() == 4)) {
//                            if (orderAction1.getBmdpt() != 4 && orderAction1.getSumChild() == 0) {
                                orderAction1.setBcdStatus(0); //状态改为准备开始
                                orderAction1.setBisPush(1);

                                dbu.updateRefOP2(assetCollection, orderOItem1.getId_CB(), orderOItem1.getId_C(),
                                        actData.getString("id_FC"),
                                        actData.getString("id_FS"), orderAction1.getId_OP(), orderAction1.getRefOP(),
                                        orderAction1.getWrdNP(), nextId, nextIndex, true );

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
        private void updateParent(JSONObject assetCollection, OrderAction orderAction,JSONObject tokData) {


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


                        dbu.updateRefOP2(assetCollection, unitOItemPrnt.getId_CB(), unitOItemPrnt.getId_C(),
                                actData.getString("id_FC"),
                                actData.getString("id_FS"), unitActionPrnt.getId_OP(), unitActionPrnt.getRefOP(),
                                unitActionPrnt.getWrdNP(), idPrnt, indexPrnt, true );


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
                        this.updateNext(assetCollection, orderAction, tokData);
                    }
                }
            }
        }

    private long sumDura(String id_O, Integer index, LogFlow log) {

        JSONObject filt1 = qt.setJson("filtKey", "id_O", "method", "eq", "filtVal", id_O);
        JSONObject filt2 = qt.setJson("filtKey", "index", "method", "exact", "filtVal", index);

        JSONArray filterArray = qt.setArray(filt1, filt2);

        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            qt.filterBuilder(filterArray, queryBuilder);
            //System.out.println(queryBuilder);

            sourceBuilder.query(queryBuilder).size(10000)
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
            log.setData(qt.setJson("wntDur", ms));
            log.setZcndesc("任务总用时" + time); // 任务等待用时 taStart - taPush
            ws.sendWS(log);
            return ms;
        } catch (Exception e)
        {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ES_GET_DATA_IS_NULL.getCode(), "没有关联订单");
        }

        }


//    @Override
//        public ApiResponse getRefOPList(String id_Flow, Boolean isSL, String id_C) {
//
//            // 1. if flowControl.refOP in front end not exist, will call this to get all refOP info and
//            // 2. use updateRefOP to write into flowControl REF
//
//                String idF = "id";
//                String idC = "id_C";
//                //set to SL's search checking keys if the chatroom is Sales side
//                if (isSL)
//                {
//                    idF = "id_FS";
//                    idC = "id_CS";
//                }
//                JSONObject refOPIndexArray = new JSONObject();
//
//                // Type = 1: msgList regular with stateChg filter,
//                // 2: progress with no filter but only those in id_O+index
//                //构建搜索条件
//
//                JSONArray filtArray1 = qt.setESFilt(idC, id_C, "data.bcdStatus", 0);
//                qt.setESFilt(filtArray1, idF, "eq", id_Flow);
//
//                JSONArray filtArray2 = new JSONArray();
//                qt.setESFilt(filtArray2, idF, "eq", id_Flow);
//                qt.setESFilt(filtArray2, idC, "exact", id_C);
//                qt.setESFilt(filtArray2, "data.bcdStatus", "contain", Arrays.asList("2", "9"));
//
//                JSONArray result = qt.getES("action", filtArray1, 4000);
//                JSONArray result2 = qt.getES("action", filtArray2, 4000);
//
//                JSONObject refOPList = new JSONObject();
//                // contentMap方法意义：比如lSProd 的id_P替换id，这样的意义，前端可以拿这个id去查Prod表
//
//                for (int i = 0; i < result.size(); i++) {
//
//                    String refOP = result.getJSONObject(i).getJSONObject("data").getString("refOP");
//
//                    if (refOP != null && ! refOP.equals("") && refOPList.getInteger(refOP) == null && result.getJSONObject(i).getInteger("index") != null)
//                    {
//                        refOPList.put(refOP, 1);
//                        refOPIndexArray.put(refOP, qt.setJson("refOP", refOP, "index", new JSONArray()));
//                        refOPIndexArray.getJSONObject(refOP).getJSONArray("index").add(result.getJSONObject(i).getInteger("index"));
//
//
//                    } else if (refOP != null && !refOP.equals("") && result.getJSONObject(i).getInteger("index") != null)
//                    {
//                        refOPList.put(refOP, refOPList.getInteger(refOP) + 1);
//                        refOPIndexArray.getJSONObject(refOP).getJSONArray("index").add(result.getJSONObject(i).getInteger("index"));
//
//                    }
//
//                }
//                for (int i = 0; i < result2.size(); i++) {
//                    //System.out.println("result222"+result2.getJSONObject(i).getJSONObject("data").getString("refOP"));
//
//                    String refOP = result2.getJSONObject(i).getJSONObject("data").getString("refOP");
//
//                    if (refOP != null && ! refOP.equals("") && refOPList.getInteger(refOP) != null && result2.getJSONObject(i).getInteger("index") != null)
//                    {
//                        refOPList.put(refOP, refOPList.getInteger(refOP) - 1);
//                        refOPIndexArray.getJSONObject(refOP).getJSONArray("index").remove(result2.getJSONObject(i).getInteger("index"));
//
//                        if (refOPList.getInteger(refOP).equals(0))
//                        {
//                            refOPList.remove(refOP);
//                            refOPIndexArray.remove(refOP);
//                        }
//
//                    }
//                }
//
//                Asset asset = qt.getConfig(id_C, "a-auth", "flowControl");
//        for (int i = 0; i < asset.getFlowControl().getJSONArray("objData").size(); i++) {
//            JSONObject flowInfo = asset.getFlowControl().getJSONArray("objData").getJSONObject(i);
//            //adding 1 index
//            if (flowInfo.getString("id").equals(id_Flow)) {
//                qt.setMDContent(asset.getId(), qt.setJson("flowControl.objData." + i + ".refOP", refOPIndexArray), Asset.class);
//                break;
//            }
//        }
//




        //System.out.println(refOPList);
//
//                return retResult.ok(CodeEnum.OK.getCode(), refOPList);
//
//            }


        // 用在 flowControl 卡片 改 grpB 的
        @Override
        public ApiResponse up_FC_action_grpB(String id_C, String id_O, String dep, String depMain, String logType,
                                             String id_Flow, JSONObject wrdFC, JSONArray grpB, JSONArray wrdGrpB)
        {
            JSONObject grpData = new JSONObject();
            String updatingGrp = "action.grpBGroup";

            if (id_O.equals(""))
            {                // 返回操作失败结果
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_GET_ORDER_NULL.getCode(), "没有关联订单");
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
        Order order = qt.getMDContent(oid, Arrays.asList("info", "oItem", "action", "oDate"), Order.class);

        // 判断订单为空
        if (null == order || order.getOItem() == null || order.getAction() == null) {


            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在"+oid);
        }
        // 创建放回结果存储字典
        JSONObject result = new JSONObject();
        JSONArray actionArray = order.getAction().getJSONArray("objAction");
        JSONArray oItemArray = order.getOItem().getJSONArray("objItem");
        JSONObject grpBGroup = order.getAction().getJSONObject("grpBGroup");
        JSONObject grpGroup = order.getAction().getJSONObject("grpGroup");
        JSONObject objCount = order.getAction().getJSONObject("objCount");
        int lDG = order.getAction().getInteger("lDG")==null?1:order.getAction().getInteger("lDG");

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
        {
        }

        result.put("oItemArray", oItemArray);
        result.put("actionArray", actionArray);
        result.put("progress", counter);
        result.put("grpBGroup",grpBGroup);
        result.put("grpGroup", grpGroup);
        result.put("id_FC", id_FC);
        result.put("id_FS", id_FS);
        result.put("info",qt.toJson(order.getInfo()));
        result.put("oDate", order.getODate());
        result.put("size", oItemArray.size());
        result.put("objCount", objCount);
        result.put("lDG", lDG);

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
            JSONObject aCollect = new JSONObject();

            if (null != actData) {
                this.activateThis(aCollect, actData, id_O, index, myCompId, id_U, grpU, dep, wrdNU);
            }

            dbu.setMDRefOP(aCollect);

        return retResult.ok(CodeEnum.OK.getCode(), "done");
    }

    private Integer activateThis(JSONObject aCollect, JSONObject actData, String id_O, Integer index, String myCompId, String id_U, String grpU, String dep, JSONObject wrdNU)
    {
        OrderOItem unitOItem = JSONObject.parseObject(JSON.toJSONString(actData.getJSONArray("oItemArray").getJSONObject(index)), OrderOItem.class);
        OrderAction unitAction = JSONObject.parseObject(JSON.toJSONString(actData.getJSONArray("actionArray").getJSONObject(index)), OrderAction.class);

        // 根据零件递归信息获取零件信息，并且制作日志
        unitAction.setBcdStatus(0);
        unitAction.setBisPush(1);

        qt.setMDContent(id_O, qt.setJson("action.objAction." + index, unitAction), Order.class);

        dbu.updateRefOP2(aCollect, actData.getJSONObject("info").getString("id_CB"), actData.getJSONObject("info").getString("id_C"),
                actData.getString("id_FC"), actData.getString("id_FS"), unitAction.getId_OP(), unitAction.getRefOP(), unitAction.getWrdNP(), id_O, index, true );

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
            Integer subCountWithin = this.activateThis(aCollect, actData, id_O, index + 1, myCompId, id_U, grpU, dep, wrdNU);
            totalSubCount = unitOItem.getObjSub() + subCountWithin;
        }

        // check if next oItem (index + 1 + objSub(0) > size && has seq == 1
        Integer seqCheckIndex = index + 1 + totalSubCount;
        if (seqCheckIndex < actData.getInteger("size") && unitOItem.getId_P().equals(""))
        {
            String seqNext = actData.getJSONArray("oItemArray").getJSONObject(seqCheckIndex).getString("seq");
            if (seqNext.equals("1"))
            {
                this.activateThis(aCollect, actData, id_O, seqCheckIndex, myCompId, id_U, grpU, dep, wrdNU);
            }
        }
        return totalSubCount;
    }

    // this special Pushing API is for Start all orders from casItemx if they are "materials"
    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse dgActivateAll(String id_O, JSONObject tokData) {

//        String myCompId, String id_U, String grpU, String dep, JSONObject wrdNU/

        String myCompId = tokData.getString("id_C");

        Order orderMainData = qt.getMDContent(id_O, "casItemx", Order.class);
        List <Order> orderDataList = new ArrayList<>();
        JSONArray orderList = orderMainData.getCasItemx().getJSONObject(myCompId).getJSONArray("objOrder");
        JSONObject assetCollection = new JSONObject();

        for (Integer n = 0; n < orderList.size(); n++) {

            Order thisOrder = qt.getMDContent(orderList.getJSONObject(n).getString("id_O"), Arrays.asList( "info","oItem", "action"), Order.class);
            if (thisOrder != null) {
                orderDataList.add(thisOrder);
                if (thisOrder.getInfo().getLST() != 7) {
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ORDER_NEED_FINAL.getCode(), "");
                }
            }
        }
        // After checked all lST, they are all 7 and ready to push

        for (Integer i = 0; i < orderDataList.size(); i++) {

            Order orderData = orderDataList.get(i);
            JSONArray objAction = orderData.getAction().getJSONArray("objAction");
            JSONArray objOItem = orderData.getOItem().getJSONArray("objItem");
            JSONObject grpBGroup = orderData.getAction().getJSONObject("grpBGroup");
            JSONObject grpGroup = orderData.getAction().getJSONObject("grpGroup");

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

                                    dbu.updateRefOP2(assetCollection, subOrderData.getJSONObject("info").getString("id_CB"), subOrderData.getJSONObject("info").getString("id_C"),
                                            subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), subAction.getId_OP(), subAction.getRefOP(), subAction.getWrdNP(), subAction.getId_O(), subAction.getIndex(), true );

                                    LogFlow logLP = new LogFlow(logType, subOrderData.getString("id_FC"),
                                            subOrderData.getString("id_FS"), "stateChg",
                                            tokData.getString("id_U"), tokData.getString("grpU"), subOItem.getId_P(), subOItem.getGrpB(), subOItem.getGrp(),
                                            id_O, unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                            unitAction.getSubParts().getJSONObject(k).getInteger("index"),
                                            subOItem.getId_CB(), subOItem.getId_C(),
                                            "", tokData.getString("dep"), subOItem.getWrdN().get("cn") + " 准备开始", 3, subOItem.getWrdN(), tokData.getJSONObject("wrdNReal"));
                                    logLP.setLogData_action(subAction, subOItem);
                                    logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");

                                    ws.sendWS(logLP);



                                } else {
                                    //System.out.println("break @ " + k);
                                    break;
                                }
                            }
                        }
                        else if (unitAction.getBmdpt() == 3 && unitAction.getSumPrev() == 0) {
                            //                // 根据零件递归信息获取零件信息，并且制作日志
                            unitAction.setBcdStatus(0);
                            unitAction.setBisPush(1);

                            qt.setMDContent(orderData.getId(), qt.setJson("action.objAction." + j, unitAction), Order.class);

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

                            dbu.updateRefOP2(assetCollection, myCompId, unitOItem.getId_C(),
                                    id_FC, id_FS, unitAction.getId_OP(), unitAction.getRefOP(), unitAction.getWrdNP(), unitAction.getId_O(), unitAction.getIndex(), true );

                            String msgText = "[" + unitAction.getRefOP()+"] " + unitOItem.getWrdN().get("cn") + " 准备开始";
                            LogFlow logLP = new LogFlow(fcCheck.getString("logType"),
                                    id_FC,
                                    id_FS, "stateChg", tokData.getString("id_U"), tokData.getString("grpU"),
                                    unitOItem.getId_P(), unitOItem.getGrpB(), unitOItem.getGrp(), id_O, unitAction.getId_O(), unitAction.getIndex(),
                                    unitOItem.getId_CB(), unitOItem.getId_C(), "", tokData.getString("dep"), msgText, 3, unitOItem.getWrdN(), tokData.getJSONObject("wrdNReal"));
                            logLP.setLogData_action(unitAction, unitOItem);
                            logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");


                            //System.out.println(logLP);
                            ws.sendWS(logLP);
                            // 发送日志
                        }
                    }
                }
        }
        dbu.setMDRefOP(assetCollection);
        return retResult.ok(CodeEnum.OK.getCode(), "doneAll");
    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse itemActivateStorage(JSONObject tokData, String id_O)
    {

//        String myCompId = tokData.getString("id_C");
        JSONObject assetCollection = new JSONObject();

        Order order = qt.getMDContent(id_O, Arrays.asList( "id", "info","oItem", "action"), Order.class);
            JSONArray objAction = order.getAction().getJSONArray("objAction");

            qt.errPrint("1", order, objAction);

            for (Integer j = 0; j < objAction.size(); j++)
            {
                OrderAction unitAction = qt.cloneThis(objAction.getJSONObject(j), OrderAction.class);
                if ((unitAction.getBmdpt() == 3 || unitAction.getBmdpt() == 2) && unitAction.getSumPrev() == 0) {

                    if (!order.getInfo().getId_CB().equals(tokData.getString("id_C"))) // I am not the buyer
                    {
                        this.createStoQuestSL(assetCollection, tokData, order, order.getId(), j);
                    } else {
                        this.createStoQuest(assetCollection, tokData, order, order.getId(), j);
                    }
                }
            }

        dbu.setMDRefOP(assetCollection);

        return retResult.ok(CodeEnum.OK.getCode(), "doneAll");
    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse dgActivateStorage(JSONObject tokData, String id_O)
    {

        String myCompId = tokData.getString("id_C");
        JSONObject assetCollection = new JSONObject();

        Order orderMainData = qt.getMDContent(id_O, "casItemx", Order.class);

        List <Order> orderDataList = new ArrayList<>();
        JSONArray orderList = orderMainData.getCasItemx().getJSONObject(myCompId).getJSONArray("objOrder");

        for (Integer n = 0; n < orderList.size(); n++) {

            Order listOrder = qt.getMDContent(orderList.getJSONObject(n).getString("id_O"), Arrays.asList( "id", "info","oItem", "action"), Order.class);
            if (listOrder != null) {
                Integer unitLST = listOrder.getInfo().getLST();

                if (unitLST != 7) {
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ORDER_NEED_FINAL.getCode(), "");
                }
                orderDataList.add(listOrder);
            }

        }
        // After checked all lST, they are all 7 and ready to push

        for (Integer i = 0; i < orderDataList.size(); i++) {
            Order orderData = orderDataList.get(i);
            JSONArray objAction = orderData.getAction().getJSONArray("objAction");

            for (Integer j = 0; j < objAction.size(); j++)
            {
                OrderAction unitAction = qt.cloneThis(objAction.getJSONObject(j), OrderAction.class);
//                OrderOItem unitOItem = qt.cloneThis(objOItem.getJSONObject(j), OrderOItem.class);
                if (unitAction.getBisPush() != 1) {

                    // if bmdpt == 4, && sumPrev == 0, my subParts [prior == 0] starts
                    // for each subParts
                    // check if wn0prior == 0, if so dgActivate
                    if (unitAction.getBmdpt() == 4 && unitAction.getSumPrev() == 0)
                    {
                        for (int k = 0; k < unitAction.getSubParts().size(); k++) {
                            JSONObject subOrderData = this.getActionData(unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                    unitAction.getSubParts().getJSONObject(k).getInteger("index"));

                            OrderOItem subOItem = qt.cloneThis(subOrderData.get("orderOItem"), OrderOItem.class);
                            OrderAction subAction = qt.cloneThis(subOrderData.get("orderAction"), OrderAction.class);

//                                if (subAction.getPriority() == 1) {
                            if (subOItem.getWn0prior() == 1) {
                                subAction.setBcdStatus(0);
                                subAction.setBisPush(1);

                                JSONObject mapKey = new JSONObject();
                                mapKey.put("action.objAction." + unitAction.getSubParts().getJSONObject(k).getInteger("index"), subAction);
                                qt.setMDContent(unitAction.getSubParts().getJSONObject(k).getString("id_O"), mapKey, Order.class);
                                //System.out.println("unit " + subOItem.getGrpB() + subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()));
                                String logType = subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()).getString("logType");

                                dbu.updateRefOP2(assetCollection, subOrderData.getJSONObject("info").getString("id_CB"), subOrderData.getJSONObject("info").getString("id_C"),
                                        subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), subAction.getId_OP(), subAction.getRefOP(), subAction.getWrdNP(), subAction.getId_O(), subAction.getIndex(), true );

                                LogFlow logLP = new LogFlow(logType, subOrderData.getString("id_FC"),
                                        subOrderData.getString("id_FS"), "stateChg",
                                        tokData.getString("id_U"), tokData.getString("grpU"), subOItem.getId_P(), subOItem.getGrpB(), subOItem.getGrp(),
                                        id_O, unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                        unitAction.getSubParts().getJSONObject(k).getInteger("index"),
                                        subOItem.getId_CB(), subOItem.getId_C(),
                                        "", tokData.getString("dep"), subOItem.getWrdN().get("cn") + " 准备开始", 3, subOItem.getWrdN(), tokData.getJSONObject("wrdNReal"));
                                logLP.setLogData_action(subAction, subOItem);
                                logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");

                                ws.sendWS(logLP);
                           }
                        }
                    }
                    else if (unitAction.getBmdpt() == 3 && unitAction.getSumPrev() == 0 && unitAction.getProb() == null)
                    {
                        this.createStoQuest(assetCollection, tokData, orderData, orderData.getId(), j);
                    }
                }
            }
        }

        dbu.setMDRefOP(assetCollection);

        return retResult.ok(CodeEnum.OK.getCode(), "doneAll");
    }

    public ApiResponse mergeAllAndStorage(String id_O, JSONObject tokData){
        try {
            String myCompId = tokData.getString("id_C");
            Asset asset = qt.getConfig(myCompId, "a-auth", "def");
            JSONObject objlBP = asset.getDef().getJSONObject("objlBP");
            Order orderMainData = qt.getMDContent(id_O, "casItemx", Order.class);
            List <Order> orderDataList = new ArrayList<>();
            JSONArray orderList = orderMainData.getCasItemx().getJSONObject(myCompId).getJSONArray("objOrder");
            JSONObject assetCollection = new JSONObject();
            for (int n = 0; n < orderList.size(); n++) {
                Order listOrder = qt.getMDContent(orderList.getJSONObject(n).getString("id_O"), Arrays.asList( "id", "info","oItem", "action"), Order.class);
                if (listOrder != null) {
                    if (listOrder.getInfo().getLST() != 7) {
                        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ORDER_NEED_FINAL.getCode(), "");
                    }
                    orderDataList.add(listOrder);
                }
            }

            for (Order orderData : orderDataList) {

                JSONArray objAction = orderData.getAction().getJSONArray("objAction");
                JSONArray objOItem = orderData.getOItem().getJSONArray("objItem");
                JSONObject grpBGroup = orderData.getAction().getJSONObject("grpBGroup");
                JSONObject grpGroup = orderData.getAction().getJSONObject("grpGroup");

                for (int j = 0; j < objAction.size(); j++) {
                    OrderAction unitAction = qt.cloneThis(objAction.getJSONObject(j), OrderAction.class);
                    OrderOItem unitOItem = qt.cloneThis(objOItem.getJSONObject(j), OrderOItem.class);
                    if (unitAction.getBmdpt() == 3) {
                        System.out.println("action+oItem:");
                        System.out.println(JSON.toJSONString(unitAction));
                        System.out.println(JSON.toJSONString(unitOItem));
                    }
                    if (unitAction.getBisPush() != 1) {
                        boolean isSto;
                        JSONObject lbP = objlBP.getJSONObject(unitOItem.getGrpB());
                        if (null == lbP) {
                            isSto = false;
                        } else {
                            isSto = lbP.getBoolean("isSto") != null && lbP.getBoolean("isSto");
                        }
                        // if bmdpt == 4, && sumPrev == 0, my subParts [prior == 0] starts
                        // for each subParts
                        // check if wn0prior == 0, if so dgActivate
                        if (isSto) {
                            this.createStoQuest(assetCollection, tokData, orderData, orderData.getId(), j);
                        } else {
                            if (unitAction.getBmdpt() == 4 && unitAction.getSumPrev() == 0) {
                                for (int k = 0; k < unitAction.getSubParts().size(); k++) {
                                    JSONObject subOrderData = this.getActionData(unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                            unitAction.getSubParts().getJSONObject(k).getInteger("index"));
                                    OrderOItem subOItem = qt.cloneThis(subOrderData.get("orderOItem"), OrderOItem.class);
                                    OrderAction subAction = qt.cloneThis(subOrderData.get("orderAction"), OrderAction.class);

                                    if (subOItem.getWn0prior() == 1) {
                                        subAction.setBcdStatus(0);
                                        subAction.setBisPush(1);

                                        JSONObject mapKey = new JSONObject();
                                        mapKey.put("action.objAction." + unitAction.getSubParts().getJSONObject(k).getInteger("index"), subAction);
                                        qt.setMDContent(unitAction.getSubParts().getJSONObject(k).getString("id_O"), mapKey, Order.class);
                                        //System.out.println("unit " + subOItem.getGrpB() + subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()));
                                        String logType = subOrderData.getJSONObject("grpBGroup").getJSONObject(subOItem.getGrpB()).getString("logType");

                                        dbu.updateRefOP2(assetCollection, subOrderData.getJSONObject("info").getString("id_CB"), subOrderData.getJSONObject("info").getString("id_C"),
                                                subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), subAction.getId_OP(), subAction.getRefOP(), subAction.getWrdNP(), subAction.getId_O(), subAction.getIndex(), true);

                                        LogFlow logLP = new LogFlow(logType, subOrderData.getString("id_FC"),
                                                subOrderData.getString("id_FS"), "stateChg",
                                                tokData.getString("id_U"), tokData.getString("grpU"), subOItem.getId_P(), subOItem.getGrpB(), subOItem.getGrp(),
                                                id_O, unitAction.getSubParts().getJSONObject(k).getString("id_O"),
                                                unitAction.getSubParts().getJSONObject(k).getInteger("index"),
                                                subOItem.getId_CB(), subOItem.getId_C(),
                                                "", tokData.getString("dep"), subOItem.getWrdN().get("cn") + " 准备开始", 3, subOItem.getWrdN(), tokData.getJSONObject("wrdNReal"));
                                        logLP.setLogData_action(subAction, subOItem);
                                        logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");
                                        ws.sendWS(logLP);
                                    }
//                            else {
//                                //System.out.println("break @ " + k);
//                                break;
//                            }
                                }
                            }
                            else if (unitAction.getBmdpt() == 3 && unitAction.getSumPrev() == 0) {
                                System.out.println("进入物料:");
                                //                // 根据零件递归信息获取零件信息，并且制作日志
                                unitAction.setBcdStatus(0);
                                unitAction.setBisPush(1);

                                qt.setMDContent(orderData.getId(), qt.setJson("action.objAction." + j, unitAction), Order.class);

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

                                dbu.updateRefOP2(assetCollection, myCompId, unitOItem.getId_C(),
                                        id_FC, id_FS, unitAction.getId_OP(), unitAction.getRefOP(), unitAction.getWrdNP(), unitAction.getId_O(), unitAction.getIndex(), true);

                                String msgText = "[" + unitAction.getRefOP() + "] " + unitOItem.getWrdN().get("cn") + " 准备开始";
                                LogFlow logLP = new LogFlow(fcCheck.getString("logType"),
                                        id_FC,
                                        id_FS, "stateChg", tokData.getString("id_U"), tokData.getString("grpU"),
                                        unitOItem.getId_P(), unitOItem.getGrpB(), unitOItem.getGrp(), id_O, unitAction.getId_O(), unitAction.getIndex(),
                                        unitOItem.getId_CB(), unitOItem.getId_C(), "", tokData.getString("dep"), msgText, 3, unitOItem.getWrdN(), tokData.getJSONObject("wrdNReal"));
                                logLP.setLogData_action(unitAction, unitOItem);
                                logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");


                                //System.out.println(logLP);
                                ws.sendWS(logLP);
                                // 发送日志
                            } else {
                                System.out.println("进入else");
                            }
                        }
                    }
                }
            }

            dbu.setMDRefOP(assetCollection);
        } catch (Exception e) {
            System.out.println("出现异常:"+e.getMessage());
            e.printStackTrace();
        }
        return retResult.ok(CodeEnum.OK.getCode(), "AllAndStorage");
    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public String dgActivateStoSingle(JSONObject tokData, String id_O, Integer index) {

        JSONObject assetCollection = new JSONObject();
        Order order = qt.getMDContent(id_O, Arrays.asList( "info","oItem", "action"), Order.class);
        this.createStoQuest(assetCollection, tokData, order, id_O, index);
        return "done";

    }


    private void createStoQuest(JSONObject assetCollection, JSONObject tokData, Order order,String id_O, Integer index)
    {
        OrderAction unitAction = qt.cloneThis(order.getAction().getJSONArray("objAction").getJSONObject(index), OrderAction.class);
        OrderOItem unitOItem = qt.cloneThis(order.getOItem().getJSONArray("objItem").getJSONObject(index), OrderOItem.class);

        String myCompId = tokData.getString("id_C");

        JSONObject fcCheck = order.getAction().getJSONObject("grpBGroup").getJSONObject(unitOItem.getGrpB());
        String id_FQ = "";

        if (fcCheck != null) {
            id_FQ = fcCheck.getString("id_Flow") != null
                    && !unitOItem.getId_C().equals(myCompId) ?
                    fcCheck.getString("id_Flow") : "";
        }
        Asset assetgrpA = qt.getConfig(myCompId,"a-auth", "def.objlBP." + unitOItem.getGrpB());

        String grpA = assetgrpA.getDef().getJSONObject("objlBP").getJSONObject(unitOItem.getGrpB()).getString("grpA");

        if (grpA == null)
        {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ErrEnum.ASSET_OBJECT_NO_HAVE.getCode(), "获取数据为空 grpA");
        }

        Asset assetflow = qt.getMDContent(assetgrpA.getId(),"def.objlSA." + grpA, Asset.class);

        String fcOrderId = assetflow.getDef().getJSONObject("objlSA").getJSONObject(grpA).getString("id_O");
        String id_FC = assetflow.getDef().getJSONObject("objlSA").getJSONObject(grpA).getString("id_Flow");

        if (fcOrderId == null || id_FC == null)
        {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ErrEnum.ASSET_OBJECT_NO_HAVE.getCode(), "获取数据为空 FC id+O");
        }

        String desc = "[" + unitAction.getRefOP() + "] " + unitOItem.getWrdN().getString("cn") + "收货: " + unitOItem.getWn2qtyneed();

        JSONObject probData = qt.setJson("logType", "assetflow", "pic", unitOItem.getPic(), "wrdNP", unitOItem.getWrdNP(), "wrdN", unitOItem.getWrdN(),
                "ref", id_FC , "id_O", fcOrderId, "grpB", "1000", "id_OP", unitOItem.getId_OP(), "refOP", unitAction.getRefOP(), "wrddesc", qt.setJson("cn", desc),
                "wrdprep", qt.setJson("cn",""));



        this.createQuest(assetCollection, tokData,id_O, index, fcOrderId, id_FC, id_FQ, probData);
        // then, set qtySafex
        if (!order.getInfo().getId_C().equals(myCompId))
        {
            // I am not the seller, someone else must be, so there must be qtySafex
            // add this qtyNeed into the qtyBuy, "buying more and not yet receive"
            Prod prod = qt.getMDContent(unitOItem.getId_P(), Arrays.asList("qtySafex."+myCompId,"view"), Prod.class);

            JSONObject qtySafex = qt.cloneObj(prod.getQtySafex());
            JSONObject jsonSafex = qt.setJson("id_O", id_O, "index", index, "refOP",
                    unitAction.getRefOP(),
                    "wrdNO", unitOItem.getWrdN(), "wn2qty", unitOItem.getWn2qtyneed());
            if (qtySafex == null) {
                JSONArray arraySafex = qt.setArray(jsonSafex);
                prod.getView().add("qtySafex");
                JSONObject jsonId_C = qt.setJson("id_C", myCompId, "objSafex", arraySafex,"wn2buy", unitOItem.getWn2qtyneed());
                qtySafex = qt.setJson(myCompId, jsonId_C);
            } else if (qtySafex.getJSONObject(myCompId) == null) {
                JSONArray arraySafex = qt.setArray(jsonSafex);
                JSONObject jsonId_C = qt.setJson("id_C", myCompId, "objSafex", arraySafex, "wn2buy", unitOItem.getWn2qtyneed());
                qtySafex.put(myCompId, jsonId_C);
            } else {
                qtySafex.getJSONObject(myCompId).getJSONArray("objSafex").add(jsonSafex);
                qtySafex.getJSONObject(myCompId).put("wn2buy", DoubleUtils.add(qtySafex.getJSONObject(myCompId).getDouble("wn2buy"), unitOItem.getWn2qtyneed()));
            }
            // qtySafex.id_C. objSafex:{id_O, index, wn2qty} ok
            // wn2sum, wn2buy
            qt.setMDContent(unitOItem.getId_P(), qt.setJson("qtySafex."+myCompId, qtySafex.getJSONObject(myCompId),
                    "view", prod.getView()), Prod.class);

            qt.setES("lBProd", qt.setESFilt("id_P", unitOItem.getId_P(), "id_CB", myCompId),
                    qt.setJson("wn2buy", qtySafex.getJSONObject(myCompId).getDouble("wn2buy")));
        }
    }

    private void createStoQuestSL(JSONObject assetCollection, JSONObject tokData, Order order,String id_O, Integer index)
    {
        OrderAction unitAction = qt.cloneThis(order.getAction().getJSONArray("objAction").getJSONObject(index), OrderAction.class);
        OrderOItem unitOItem = qt.cloneThis(order.getOItem().getJSONArray("objItem").getJSONObject(index), OrderOItem.class);

        String myCompId = tokData.getString("id_C");

        JSONObject fcCheck = order.getAction().getJSONObject("grpGroup").getJSONObject(unitOItem.getGrp());
        String id_FQ = "";

        if (fcCheck != null) {
            id_FQ = fcCheck.getString("id_Flow") != null
                    && !unitOItem.getId_C().equals(myCompId) ?
                    fcCheck.getString("id_Flow") : "";
        }
        Asset assetgrpA = qt.getConfig(myCompId,"a-auth", "def.objlSP." + unitOItem.getGrp());

        String grpA = assetgrpA.getDef().getJSONObject("objlSP").getJSONObject(unitOItem.getGrp()).getString("grpA");

        if (grpA == null)
        {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ErrEnum.ASSET_OBJECT_NO_HAVE.getCode(), "获取数据为空 grpA");
        }

        Asset assetflow = qt.getMDContent(assetgrpA.getId(),"def.objlSA." + grpA, Asset.class);

        String fcOrderId = assetflow.getDef().getJSONObject("objlSA").getJSONObject(grpA).getString("id_O");
        String id_FC = assetflow.getDef().getJSONObject("objlSA").getJSONObject(grpA).getString("id_Flow");

        if (fcOrderId == null || id_FC == null)
        {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ErrEnum.ASSET_OBJECT_NO_HAVE.getCode(), "获取数据为空 FC id+O");
        }

        String desc = "[" + unitAction.getRefOP() + "] " + unitOItem.getWrdN().getString("cn") + "发货: " + unitOItem.getWn2qtyneed();

        JSONObject probData = qt.setJson("logType", "assetflow", "pic", unitOItem.getPic(), "wrdNP", unitOItem.getWrdNP(), "wrdN", unitOItem.getWrdN(),
                "ref", id_FC , "id_O", fcOrderId, "grpB", "1000", "id_OP", unitOItem.getId_OP(), "refOP", unitAction.getRefOP(), "wrddesc", qt.setJson("cn", desc),
                "wrdprep", qt.setJson("cn",""));



        this.createQuest(assetCollection, tokData,id_O, index, fcOrderId, id_FC, id_FQ, probData);
        // then, set qtySafex
//        if (!order.getInfo().getId_C().equals(myCompId))
//        {
//            // I am not the seller, someone else must be, so there must be qtySafex
//            // add this qtyNeed into the qtyBuy, "buying more and not yet receive"
//            Prod prod = qt.getMDContent(unitOItem.getId_P(), Arrays.asList("qtySafex."+myCompId,"view"), Prod.class);
//
//            JSONObject qtySafex = qt.cloneObj(prod.getQtySafex());
//            JSONObject jsonSafex = qt.setJson("id_O", id_O, "index", index, "refOP",
//                    unitAction.getRefOP(),
//                    "wrdNO", unitOItem.getWrdN(), "wn2qty", unitOItem.getWn2qtyneed());
//            if (qtySafex == null) {
//                JSONArray arraySafex = qt.setArray(jsonSafex);
//                prod.getView().add("qtySafex");
//                JSONObject jsonId_C = qt.setJson("id_C", myCompId, "objSafex", arraySafex,"wn2buy", unitOItem.getWn2qtyneed());
//                qtySafex = qt.setJson(myCompId, jsonId_C);
//            } else if (qtySafex.getJSONObject(myCompId) == null) {
//                JSONArray arraySafex = qt.setArray(jsonSafex);
//                JSONObject jsonId_C = qt.setJson("id_C", myCompId, "objSafex", arraySafex, "wn2buy", unitOItem.getWn2qtyneed());
//                qtySafex.put(myCompId, jsonId_C);
//            } else {
//                qtySafex.getJSONObject(myCompId).getJSONArray("objSafex").add(jsonSafex);
//                qtySafex.getJSONObject(myCompId).put("wn2buy", DoubleUtils.add(qtySafex.getJSONObject(myCompId).getDouble("wn2buy"), unitOItem.getWn2qtyneed()));
//            }
//            // qtySafex.id_C. objSafex:{id_O, index, wn2qty} ok
//            // wn2sum, wn2buy
//            qt.setMDContent(unitOItem.getId_P(), qt.setJson("qtySafex."+myCompId, qtySafex.getJSONObject(myCompId),
//                    "view", prod.getView()), Prod.class);
//
//            qt.setES("lBProd", qt.setESFilt("id_P", unitOItem.getId_P(), "id_CB", myCompId),
//                    qt.setJson("wn2buy", qtySafex.getJSONObject(myCompId).getDouble("wn2buy")));
//        }
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

        JSONObject assetCollection = new JSONObject();

        for (Integer n = 0; n < orderList.size(); n++) {

            orderDataList.add(
                    qt.getMDContent(orderList.getJSONObject(n).getString("id_O"), Arrays.asList( "info","oItem", "action"), Order.class)

            );
            OrderInfo unitInfo = JSONObject.parseObject(JSON.toJSONString(orderDataList.get(n).getInfo()), OrderInfo.class);

            if (unitInfo.getLST() != 7) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ORDER_NEED_FINAL.getCode(), "");
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
                                dbu.updateRefOP2(assetCollection, myCompId, subOItem.getId_C(),
                                        subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), id_O, unitAction.getRefOP(), unitAction.getWrdNP(),
                                        unitAction.getSubParts().getJSONObject(k).getString("id_O"), unitAction.getSubParts().getJSONObject(k).getInteger("index"), true );

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

                        dbu.updateRefOP2(assetCollection, myCompId, unitOItem.getId_C(),
                                id_FC, id_FS, id_O, unitAction.getRefOP(), unitAction.getWrdNP(), unitAction.getId_O(), unitAction.getIndex(), true );

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
            dbu.setMDRefOP(assetCollection);

        return retResult.ok(CodeEnum.OK.getCode(), "doneAll");
    }



    /**
     * Create task OItem + Action + Log
     *
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @ver 1.0.0
     * @date 2020/8/6 9:03
     */
    @Override
    public Integer createTask(JSONObject tokData, String logType, String id_FC, String id_O, JSONObject oItemData) {

        Integer index = 0;
        Integer prior = 1;

        String id_FS = "";
        String myCompId = tokData.getString("id_C");

        if (id_O.equals(""))
        {                // 返回操作失败结果
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMyy");
            JSONArray idArray = qt.getES("lSBOrder", qt.setESFilt("refB", "s-"+id_FC+ "-"+ dateFormat.format(new Date())), 1);
            qt.errPrint("er", idArray,dateFormat.format(new Date()));

            if (idArray.size() == 0)
            {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_GET_ORDER_NULL.getCode(), "没有关联订单");
            }
            id_O = idArray.getJSONObject(0).getString("id_O");
        }
        JSONObject actData = this.getActionData(id_O, index - 1);

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
        OrderOItem unitOItem = new OrderOItem ("",oItemData.getString("id_OP"),oItemData.getString("id_CP"),myCompId, myCompId,
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


        JSONObject oStockData = qt.setJson("wn2qtynow", 0.0, "wn2qtymade", 0.0,
                "id_P", unitOItem.getId_P(),
                "resvQty", new JSONObject(), "index", index,
                "rKey", unitOItem.getRKey());

        oStockData.put("objShip", qt.setArray(
                qt.setJson("wn2qtyship", 0.0, "wn2qtyshipnow", 0.0,
                        "wn2qtynowS", 0.0, "wn2qtynow", 0.0, "wn2qtymade", 0.0,
                        "wn2qtyneed", unitOItem.getWn2qtyneed())));


        JSONObject aCollect = new JSONObject();
        dbu.updateRefOP2(aCollect, myCompId, myCompId,
                id_FC, id_FS, id_O, "grpTask", oItemData.getJSONObject("wrdNP"), id_O, index, true );
        dbu.setMDRefOP(aCollect);

        // Send a log
        LogFlow logLP = new LogFlow(logType,id_FC,
                id_FS,"stateChg", tokData.getString("id_U"),tokData.getString("grpU"),"",unitOItem.getGrpB(), "",id_O,id_O,index, myCompId,myCompId,
                oItemData.getString("pic"),tokData.getString("dep"),oItemData.getJSONObject("wrdN").getString("cn") +"准备开始",3,
                qt.cloneObj(oItemData.getJSONObject("wrdN")),tokData.getJSONObject("wrdNReal"));
       logLP.setLogData_action(unitAction,unitOItem);
       if (oItemData.getJSONArray("arrTask") != null)
       {
           logLP.getData().put("arrTask", oItemData.getJSONArray("arrTask"));
           unitAction.setArrTask(oItemData.getJSONArray("arrTask"));

       }
        logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");
        ws.sendWS(logLP);
        allAction.add(unitAction);

        qt.setMDContent(id_O, qt.setJson("action.objAction",allAction, "oItem.objItem."+index, unitOItem,
               "oStock.objData."+index, oStockData ), Order.class);
        qt.setES("lSBOrder", qt.setESFilt("id_O", id_O), qt.setJson("tmd",DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate())));

        return index;
    }


    // for TaskConfirm... pretty useless for now
    @Override
    public ApiResponse createTaskNew(String logType, String id, String id_FS, String id_O, String myCompId, String id_U, String grpU, String dep, JSONObject oItemData, JSONObject wrdNU) {
        Integer index = 0;
        Integer prior = 1;
        JSONObject actData = this.getActionData(id_O, index);
//        String id_FS = "";

        if (id_O.equals(""))
        {                // 返回操作失败结果
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_GET_ORDER_NULL.getCode(), "没有关联订单");
        }

        JSONArray id_Us;
        JSONArray defReply = null;
        if (logType.endsWith("SL"))
            logType = StringUtils.strip(logType, "SL");

        //System.out.println("logType:"+logType);
        if (!id.equals(id_FS)) {
            Asset asset = qt.getConfig(myCompId,"a-auth","flowControl");
            if (null == asset || null == asset.getFlowControl()) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_GET_ORDER_NULL.getCode(), "asset异常");
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

        dbu.updateRefOP(myCompId, myCompId,
                id, id_FS, id_O, "grpTask", oItemData.getJSONObject("wrdNP"), id_O, index, true );

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

    @Override
    public String createTaskAndQuest(JSONObject tokData, String id_FC, String id_FQ, boolean isSL, String grp, String inputMsg, JSONArray arrTask) {


        //1. get all id_O, id_OProb ok
        //2. send logs ok, once get
        //3. set order.index
        //4. make sure the log has "id_O, index, wrdN, id_P, wn2qtyneed,  refOP"
        //
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMyy");
        JSONArray idArray = qt.getES("lSBOrder", qt.setESFilt("refB", "s-"+id_FC+ "-"+ dateFormat.format(new Date())), 1);

        if (idArray.size() == 0)
        {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_GET_ORDER_NULL.getCode(), "没有关联订单");
        }
        String id_O = idArray.getJSONObject(0).getString("id_O");

        JSONObject oItemData =
                qt.setJson("grp", isSL? grp : "", "grpB", isSL? "": grp,
                "grpU", tokData.getString("grpU"), "grpUB", tokData.getString("grpU"),
                "id_C", tokData.getString("id_C"), "id_CB", tokData.getString("id_C"),
                "id_O", id_O, "id_CP", tokData.getString("id_C"), "id_P", "", "id", id_FC,
                "lCR", 0, "lUT", 1, "pic", "", "ref", "", "seq", 1, "wn0prior", 1, "wn2qtyneed", 1,
                "wrdN", qt.setJson("cn", inputMsg), "wrddesc", qt.setJson("cn", inputMsg),
                "wrdprep", qt.setJson("cn", ""), "wn4price", 0);

        oItemData.put("arrTask", arrTask);

        Integer indexO = this.createTask(tokData, "action", id_FC, id_O, oItemData);

        JSONObject probData = qt.setJson("logType", "action",
                "pic", oItemData.getString("pic"),
                "wrdNP", oItemData.getJSONObject("wrdN"),
                "wrdN", qt.setJson("cn", "需要处理"),
                "ref", "",
                "id_OP", id_O,
                "refOP", "",
                "wrddesc", oItemData.getJSONObject("wrddesc"),
                "wrdprep",qt.setJson("cn", ""),
                "arrTask", arrTask);


        this.createQuest(new JSONObject(), tokData, id_O, indexO, "", id_FC, id_FQ, probData);

        return "done";
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
     * id_FC = prob 的FC, Id_FQ 是 本来的
     */

    @Override
    public String createQuest(JSONObject assetCollection, JSONObject tokData, String id_O, Integer index, String id_Prob, String id_FC,
                                   String id_FQ, JSONObject probData) {

//        JSONObject probSet = this.getActionData(id_O, index);
        Order thisOrder = qt.getMDContent(id_O, Arrays.asList("info", "oItem", "action"), Order.class);

        Boolean isPublic = false;

        if (assetCollection == null)
        {
            assetCollection = new JSONObject();
            isPublic = true;
        }


        String myCompId = tokData.getString("id_C");

        if (id_Prob.equals(""))
        {                // 返回操作失败结果
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMyy");
            JSONArray idArray = qt.getES("lSBOrder", qt.setESFilt("refB", "s-"+id_FQ+ "-"+ dateFormat.format(new Date())), 1);

            if (idArray.size() == 0)
            {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_GET_ORDER_NULL.getCode(), "没有关联订单");
            }
            id_Prob = idArray.getJSONObject(0).getString("id_O");
        }

        // go to prob fixing group, make a new OItem, send log
        Order probOrder = qt.getMDContent(id_Prob, Arrays.asList("info", "oItem", "action", "oStock"), Order.class);

//        JSONObject actData = this.getActionData(id_Prob, 0);

        Integer indexProb = 0;
        Integer priorProb = 1;



        if (probOrder != null)
        {
            // get the size of oItem, so I can append new "task" to it
            // 获取oItem的大小，这样我就可以向它附加新的“任务”
            indexProb = probOrder.getOItem().getJSONArray("objItem").size();
            if (indexProb > 0) {
                priorProb = probOrder.getOItem().getJSONArray("objItem").getJSONObject(indexProb - 1).getInteger("wn0prior");
            }
        }

        OrderAction orderAction = qt.cloneThis(thisOrder.getAction().getJSONArray("objAction").getJSONObject(index), OrderAction.class);
        OrderOItem orderOItem = qt.cloneThis(thisOrder.getOItem().getJSONArray("objItem").getJSONObject(index), OrderOItem.class);

        if (orderAction.getProb() == null)
        {
            orderAction.setProb(new JSONArray());
        }

        Integer sumProb = orderAction.getProb().size();

        String logTypeProb = probData.getString("logType");

        probData.put("index",indexProb);
        orderAction.getProb().add(probData);

        qt.errPrint("probData", probData, id_O, index);
//        qt.setMDContent(id_O, qt.setJson("action.objAction."+index,orderAction), Order.class);
        qt.pushMDContent(id_O,"action.objAction." + index + ".prob", probData, Order.class);
//       JSONArray allAction = actData.getJSONArray("actionArray"); // this is action.objAction []

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


        OrderAction unitAction = new OrderAction(0,1,7,1,probData.getString("id_OP"),probData.getString("refOP"),"",
                id_O,index,unitOItem.getRKey(),0,indexProb, new JSONArray(),upPrnt,new JSONArray(),new JSONArray(),
                probData.getJSONObject("wrdNP"),probData.getJSONObject("wrdN"));



        JSONObject oStockData = qt.setJson("wn2qtynow", 0.0, "wn2qtymade", 0.0,
                "id_P", unitOItem.getId_P(),
                "resvQty", new JSONObject(), "index", indexProb,
                "rKey", unitOItem.getRKey());

        oStockData.put("objShip", qt.setArray(
                qt.setJson("wn2qtyship", 0.0, "wn2qtyshipnow", 0.0,
                        "wn2qtynowS", 0.0, "wn2qtynow", 0.0, "wn2qtymade", 0.0,
                        "wn2qtyneed", unitOItem.getWn2qtyneed())));


        //        allAction.add(unitAction);

        dbu.updateRefOP2(assetCollection, myCompId,myCompId,
                id_FC, "", orderAction.getId_OP(), orderAction.getRefOP(),
                orderAction.getWrdNP(), id_O, index, true );


        LogFlow logProb = new LogFlow(logTypeProb,id_FC,
                id_FQ,"stateChg", tokData.getString("id_U"),tokData.getString("grpU"),orderOItem.getId_P(), orderOItem.getGrpB(), orderOItem.getGrp(),id_O, id_Prob,indexProb, myCompId,myCompId,
                probData.getString("pic"),tokData.getString("dep"),probData.getJSONObject("wrdN").getString("cn"),3,probData.getJSONObject("wrdN"),tokData.getJSONObject("wrdNReal"));

        logProb.setLogData_action(unitAction,unitOItem);
        if (probData.getJSONArray("arrTask") != null)
        {
            logProb.getData().put("arrTask", probData.getJSONArray("arrTask"));
            unitAction.setArrTask(probData.getJSONArray("arrTask"));

        }
        logProb.setActionTime(DateUtils.getTimeStamp(), 0L, "push");
        ws.sendWS(logProb);

        qt.pushMDContent(id_Prob, "action.objAction", unitAction, Order.class);
        qt.pushMDContent(id_Prob, "oItem.objItem", unitOItem, Order.class);
        qt.pushMDContent(id_Prob, "oStock.objData", oStockData, Order.class);

        if (isPublic)
        {
            dbu.setMDRefOP(assetCollection);
        }

        return "done";
    }

    @Override
    public ApiResponse subStatusChange(String id_O, Integer index, Boolean isLink, Integer statusType, JSONObject tokData) {

        //2. get Order's oItem+action
            Order order = qt.getMDContent(id_O, "action", Order.class);
            JSONObject objAction = order.getAction().getJSONArray("objAction").getJSONObject(index);
            JSONObject assetCollection = new JSONObject();

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
                        this.changeActionStatus(assetCollection,"action", 8, "全单暂停", subPartIndex, subPartId_O, isLink,
                                subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), tokData);
                    }
                }
                else if (statusType.equals(1)) {
                    if (subStatus.equals(2) || subStatus.equals(9)) {
                        newStatus = 0;
                        newMsg = "全单准备";
                        this.changeActionStatus(assetCollection,"action", 0, "全单准备", subPartIndex, subPartId_O, isLink,
                                subOrderData.getString("id_FC"), subOrderData.getString("id_FS"), tokData);
                    }
                }
                else if (statusType.equals(2)) {
                    if (subStatus.equals(0) || subStatus.equals(1) || subStatus.equals(-8) || subStatus.equals(7) || subStatus.equals(8)) {
                        newMsg = "全单完成";
                        newStatus = 2;

                        this.changeActionStatus(assetCollection, "action", 2, "全单完成", subPartIndex, subPartId_O, isLink,
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
            dbu.setMDRefOP(assetCollection);

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
        JSONObject mdJson = new JSONObject();

        if (order == null)
        {
            qt.delES("lSBOrder", qt.setESFilt("id_O", id_O));
            return 0;
        }

        if (order.getInfo().getLST() >= 7)
        {
            //already running / confirmed, just skip the confirm
            return order.getInfo().getLST();
        }
        String id_OP = order.getInfo().getId_OP();
        if (null != id_OP) {
            Order orderOP = qt.getMDContent(id_OP, "casItemx."+id_C, Order.class);
            if (null != orderOP && null != orderOP.getCasItemx() && null != orderOP.getCasItemx().getJSONObject(id_C)) {
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_FOUND.getCode(),"父订单不存在");
                JSONObject casInfo = orderOP.getCasItemx().getJSONObject(id_C);
                if (null != casInfo.getInteger("teOrderCount")) {
                    Integer teOrderCount = casInfo.getInteger("teOrderCount");
                    Integer taOrderCount = casInfo.getInteger("taOrderCount");
                    taOrderCount++;
                    if (Objects.equals(teOrderCount, taOrderCount)) {
                        qt.setMDContent(id_OP,qt.setJson("action.lDG",2
                                ,"casItemx."+id_C+".taOrderCount",taOrderCount), Order.class);
                    } else {
                        qt.setMDContent(id_OP,qt.setJson("casItemx."+id_C+".taOrderCount",taOrderCount), Order.class);
                    }
                }
            }
        }
        // if I am both buyer and seller, internal order, whatever side I am, set it to both final
        if (order.getInfo().getId_C().equals(id_C) && order.getInfo().getId_C().equals(order.getInfo().getId_CB())) { //Check C== CB
            order.getInfo().setLST(7);
            mdJson = qt.setJson("info.lST", order.getInfo().getLST(), "info.id_UCB", tokData.getString("id_U"));

        }// I am id_CB Buyer
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
            mdJson = qt.setJson("info.lST", order.getInfo().getLST(), "info.id_UCB", tokData.getString("id_U"));
        }
        else // I am Seller
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
            mdJson = qt.setJson("info.lST", order.getInfo().getLST(), "info.id_UC", tokData.getString("id_U"));
        }

        qt.setMDContent(id_O, mdJson, Order.class);
        //Save MDB above, then update ES here

        qt.setES("lsborder", qt.setESFilt("id_O", id_O), qt.setJson("lST", order.getInfo().getLST(),
                order.getInfo().getId_CB().equals(id_C) ? "id_UCB": "id_UC", tokData.getString("id_U")));

        String desc = tokData.getJSONObject("wrdNReal").getString("cn") + "确认订单" ;

        String listType = order.getInfo().getId_CB().equals(id_C) ? "lBOrder" : "lSOrder";

        //Writing update history
        LogFlow logUsage = new LogFlow();
        logUsage.setUsageLog(tokData, "confirm", desc, 2, id_O, listType,
                qt.setJson("cn", desc), listType == "lBOrder" ? order.getInfo().getGrpB(): order.getInfo().getGrp(), "");
        ws.sendESOnly(logUsage);

        Asset asset = qt.getConfig(tokData.getString("id_C"), "a-auth","def");
//        JSONObject slog = new JSONObject();
//
//        if (listType.equals("lBOrder"))
//        {
//            slog.put("id_FC", asset.getDef().getJSONObject("objlBO").getJSONObject(order.getInfo().getGrpB()).getString("id_Flow"));
//            qt.upJson(slog, "id_FS", "", "id_C", order.getInfo().getId_C(), "id_CB", tokData.getString("id_C"));
//        } else {
//            slog.put("id_FS", asset.getDef().getJSONObject("objlSO").getJSONObject(order.getInfo().getGrp()).getString("id_Flow"));
//            qt.upJson(slog, "id_FC", "", "id_CB", order.getInfo().getId_C(), "id_C", tokData.getString("id_C"));
//        }

        //    public String createTask(JSONObject tokData, String logType +SL, String id_FC, String id_O, JSONObject oItemData) {
//        LogFlow logSale = new LogFlow();
//        logSale.setSaleLog(tokData, slog.getString("id_C"), slog.getString("id_CB"), slog.getString("id_FC"), slog.getString("id_FS"), "confirm", desc, 4, id_O, id_O,  listType,
//                order.getInfo().getWrdN(), listType == "lBOrder" ? order.getInfo().getGrpB(): order.getInfo().getGrp());
//        ws.sendWS(logSale);

       if (!order.getInfo().getId_C().equals(order.getInfo().getId_CB())) {
           String taskName = listType.equals("lBOrder") ? order.getInfo().getRefB() : order.getInfo().getRef() + "单开始执行";
           String id_Flow = listType.equals("lBOrder") ? asset.getDef().getJSONObject("objlBO").getJSONObject(order.getInfo().getGrpB()).getString("id_Flow") :
                   asset.getDef().getJSONObject("objlSO").getJSONObject(order.getInfo().getGrp()).getString("id_Flow");
           JSONObject taskLog = qt.setJson("grpB", "1000", "grp", "1000", "id_CP", tokData.getString("id_C"),
                   "pic", order.getInfo().getPic(), "lUT", 0, "lCR", order.getInfo().getLCR(),
                   "wn2qtyneed", 1, "wn4price", 0, "id_OP", id_O,
                   "wrdN", qt.setJson("cn", taskName), "wrdNP", order.getInfo().getWrdN(), "wrdprep", order.getInfo().getWrddesc());
           this.createTask(tokData, listType.equals("lBOrder") ? "saleflow" : "saleflowSL", id_Flow, "", taskLog);
       }
        return order.getInfo().getLST();
    }

    @Override
    public ApiResponse cancelOrder(String id_C,String id_O, Integer bothLST) {
        Order order = qt.getMDContent(id_O, "info", Order.class);
        //Seller = 14, Buyer = 15, Both = 16


        // if I am both buyer and seller, internal order, whatever side I am, set it to both final
        if (order.getInfo().getId_C().equals(id_C) && order.getInfo().getId_C().equals(order.getInfo().getId_CB())) { //Check C== CB
            order.getInfo().setLST(bothLST);
        } // I am id_CB Buyer
        else if (id_C.equals(order.getInfo().getId_CB())) { //if Seller is REAL
            if (qt.judgeComp(id_C, order.getInfo().getId_C()) == 1) {
                if (order.getInfo().getLST() == 14) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(bothLST);
                } else {
                    order.getInfo().setLST(15);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(bothLST);
            }
        } else // I am Seller
        { //if Buyer is REAL
            if (qt.judgeComp(id_C, order.getInfo().getId_CB()) == 1) {
                if (order.getInfo().getLST() == 15) { // if they confirmed, set to both confirm
                    order.getInfo().setLST(bothLST);
                } else {
                    order.getInfo().setLST(14);
                }
            } else {
                // if otherComp is fake, set to both confirm
                order.getInfo().setLST(bothLST);
            }
        }

        qt.setMDContent(id_O, qt.setJson("info.lST", order.getInfo().getLST()), Order.class);
        qt.setES("lSBOrder", qt.setESFilt("id_O", id_O), qt.setJson("lST", order.getInfo().getLST()));
////////////////////////////////

        if (!order.getInfo().getId_C().equals(order.getInfo().getId_CB())) {
            //  if lS, getFlowId, then find id_O by getES,

//            String taskName = "订单取消执行";
//            String id_Flow = listType.equals("lBOrder") ? asset.getDef().getJSONObject("objlBO").getJSONObject(order.getInfo().getGrpB()).getString("id_Flow") :
//                    asset.getDef().getJSONObject("objlSO").getJSONObject(order.getInfo().getGrp()).getString("id_Flow");
//
//            LogFlow logLP = new LogFlow(logType,id_FC,
//                    id_FS,"stateChg", tokData.getString("id_U"),tokData.getString("grpU"),"",unitOItem.getGrpB(), "",id_O,id_O,index, myCompId,myCompId,
//                    oItemData.getString("pic"),tokData.getString("dep"),oItemData.getJSONObject("wrdN").getString("cn") +"准备开始",3,
//                    qt.cloneObj(oItemData.getJSONObject("wrdN")),tokData.getJSONObject("wrdNReal"));
//            logLP.setLogData_action(unitAction,unitOItem);
//            if (oItemData.getJSONArray("arrTask") != null)
//            {
//                logLP.getData().put("arrTask", oItemData.getJSONArray("arrTask"));
//            }
//            logLP.setActionTime(DateUtils.getTimeStamp(), 0L, "push");
//            ws.sendWS(logLP);
//
//
//            JSONObject taskLog = qt.setJson("grpB", "1000", "grp", "1000", "id_CP", tokData.getString("id_C"),
//                    "pic", order.getInfo().getPic(), "lUT", 0, "lCR", order.getInfo().getLCR(),
//                    "wn2qtyneed", 1, "wn4price", 0, "id_OP", id_O,
//                    "wrdN", qt.setJson("cn", taskName), "wrdNP", order.getInfo().getWrdN(), "wrdprep", order.getInfo().getWrddesc());
        }
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
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ErrEnum.ERR_GET_DATA_NULL.getCode(), "获取数据为空");
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
