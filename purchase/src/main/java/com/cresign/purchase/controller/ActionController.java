package com.cresign.purchase.controller;


import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.ActionService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/8/6 10:05
 * @ver 1.0
 */
@RestController
@RequestMapping("action")
public class ActionController {

        @Autowired
        private ActionService actionService;

        @Autowired
        private HttpServletRequest request;

        @Autowired
        private GetUserIdByToken getUserToken;


    /**
     * 递归发日志 改isPush
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 9:03
     */
    @SecurityParameter
    @PostMapping("/v1/dgActivate")
    public ApiResponse dgActivate(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return actionService.dgActivate(
                reqJson.getString("id_O"),
                reqJson.getInteger("index"),
                tokData.getString("id_C"),
                tokData.getString("id_U"),
                tokData.getString("grpU"),
                tokData.getString("dep"),
                tokData.getJSONObject("wrdNU"));
    }

    @SecurityParameter
    @PostMapping("/v1/dgActivateAll")
    public ApiResponse dgActivateAll(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {

        return actionService.dgActivateAll(
                reqJson.getString("id_O"),
                tokData.getString("id_C"),
                tokData.getString("id_U"),
                tokData.getString("grpU"),
                tokData.getString("dep"),
                tokData.getJSONObject("wrdNU"));

        } catch (Exception e)
        {

            return getUserToken.err(reqJson, "actionService.dgActivateAll", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/taskToProd")
    public ApiResponse taskToProd(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {

            return actionService.taskToProd(
                    tokData,
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    reqJson.getString("id_P"));

        } catch (Exception e)
        {
            return getUserToken.err(reqJson, "actionService.task2Prod", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/dgActivateSingle")
    public ApiResponse dgActivateSingle(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {

            return actionService.dgActivateSingle(
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    tokData.getString("id_C"),
                    tokData.getString("id_U"),
                    tokData.getString("grpU"),
                    tokData.getString("dep"),
                    tokData.getJSONObject("wrdNU"));

        } catch (Exception e)
        {
            return getUserToken.err(reqJson, "actionService.dgActivateAll", e);
        }
    }



    /**
     * 双方确认订单
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/10/27 9:03
     */
    @SecurityParameter
    @PostMapping("/v1/confirmOrder")
    public ApiResponse confirmOrder(@RequestBody JSONObject reqJson){
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        return actionService.confirmOrder(
                tokData.getString("id_C"),
                reqJson.getString("id_O"));
    }

    @SecurityParameter
    @PostMapping("/v1/cancelOrder")
    public ApiResponse cancelOrder(@RequestBody JSONObject reqJson){
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        return actionService.cancelOrder(
                tokData.getString("id_C"),
                reqJson.getString("id_O"));
    }


    @SecurityParameter
    @PostMapping("/v1/rePush")
    public ApiResponse rePush(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        return actionService.rePush(
                reqJson.getString("id_O"),
                reqJson.getInteger("index"),
                tokData);
    }
    /**
     * 通用日志方法(action,prob,msg)
     * @return java.lang.String  返回结果: 日志结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 9:08
     * 100 = cannot start
     * 0 = ready to go
     * 1 = processing
     * 2 = finish
     * 3 =
     * 4 =
     * 8 =
     * cancelled
     * bmdpt: 1= Process; 2 = part; 3 = Material; 4 = ProcessBatch; 5=SalesProduct,
     * no id_P oItem = ?
     *
     */
    @SecurityParameter
    @PostMapping("/v2/statusChange")
    public ApiResponse statusChange(@RequestBody JSONObject reqJson) throws IOException {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        try {
        return actionService.changeActionStatus(
                reqJson.getString("logType"),
                reqJson.getInteger("status"),
                reqJson.getString("msg"),
                reqJson.getInteger("index"),
                reqJson.getString("id_O"),
                reqJson.getBoolean("isLink"),
                reqJson.getString("id_FC"),
                reqJson.getString("id_FS"),
                tokData);
        } catch (Exception e)
        {
            return getUserToken.err(reqJson, "statusChg", e);
        }
    }


    /**
     * for a "component", batch change the status of all its subParts
     * isLink means whether this will control the next step
     * statusType 0 - all stop, 1 - all start, 2 - all finish
     * @return
     * @throws IOException
     */
    @SecurityParameter
    @PostMapping("/v1/subStatusChange")
    public ApiResponse subStatusChange(@RequestBody JSONObject reqJson) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return actionService.subStatusChange(
                reqJson.getString("id_O"),
                reqJson.getInteger("index"),
                reqJson.getBoolean("isLink"),
                reqJson.getInteger("statusType"),
                tokData);
    }

    @SecurityParameter
    @PostMapping("/v1/getRefOPList")
    public ApiResponse getRefOPList(@RequestBody JSONObject reqJson) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return actionService.getRefOPList(
                reqJson.getString("id_Flow"),
                reqJson.getBoolean("isSL"),
                tokData.getString("id_C"));
    }


    @SecurityParameter
    @PostMapping("/v1/createTask")
    public ApiResponse createTask(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        try {
            return actionService.createTask(
                    reqJson.getString("logType"),
                    reqJson.getString("id_FC"),
                    reqJson.getString("id_O"),
                    tokData.getString("id_C"),
                    tokData.getString("id_U"),
                    tokData.getString("grpU"),
                    tokData.getString("dep"),
                    reqJson.getJSONObject("oItemData"),
                    tokData.getJSONObject("wrdNU"));
        } catch (Exception e)
        {
            return getUserToken.err(reqJson, "createTask", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/createTaskNew")
    public ApiResponse createTaskNew(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        try {
            return actionService.createTaskNew(
                    reqJson.getString("logType"),
                    reqJson.getString("id"),
                    reqJson.getString("id_FS"),
                    reqJson.getString("id_O"),
                    tokData.getString("id_C"),
                    tokData.getString("id_U"),
                    tokData.getString("grpU"),
                    tokData.getString("dep"),
                    reqJson.getJSONObject("oItemData"),
                    tokData.getJSONObject("wrdNU"));
        } catch (Exception e)
        {
            return getUserToken.err(reqJson, "createTask", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/up_FC_action_grpB")
    public ApiResponse up_FC_action_grpB(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        return actionService.up_FC_action_grpB(
                tokData.getString("id_C"),
                reqJson.getString("id_O"),
                reqJson.getString("dep"),
                reqJson.getString("depMain"),
                reqJson.getString("logType"),
                reqJson.getString("id_Flow"),
                reqJson.getJSONObject("wrdFC"),
                reqJson.getJSONArray("grpB"),
                reqJson.getJSONArray("wrdGrpB"));

    }

    @SecurityParameter
    @PostMapping("/v1/createQuest")
    public ApiResponse createQuest(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        try {
            return actionService.createQuest(
                    tokData.getString("id_C"),
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    reqJson.getString("id_Prob"),
                    reqJson.getString("id_FC"),
                    reqJson.getString("id_FQ"),
                    tokData.getString("id_U"),
                    tokData.getString("grpU"),
                    tokData.getString("dep"),
                    tokData.getJSONObject("wrdNU"),
                    reqJson.getJSONObject("probData"));
        } catch (Exception e)
        {
            return getUserToken.err(reqJson, "createQuest", e);
        }
    }

        /**
         * 更新Order的grpBGroup字段
         * @return java.lang.String  返回结果: 结果
         * @author tang
         * @ver 1.0.0
         * @date 2021/1/19 10:05
         */
        @SecurityParameter
        @PostMapping("/v1/changeDepAndFlow")
        public ApiResponse changeDepAndFlow(@RequestBody JSONObject reqJson){
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

            if (reqJson.getBoolean("isSL"))
            {
                return actionService.changeDepAndFlowSL(
                        reqJson.getString("id_O"),
                        reqJson.getString("grpB"),
                        reqJson.getJSONObject("grpBOld"),
                        reqJson.getJSONObject("grpBNew"),
                        tokData.getString("id_C"),
                        tokData.getString("id_U"),
                        tokData.getString("grpU"),
                        tokData.getJSONObject("wrdNU"));
            } else {
                return actionService.changeDepAndFlow(
                        reqJson.getString("id_O"),
                        reqJson.getString("grpB"),
                        reqJson.getJSONObject("grpBOld"),
                        reqJson.getJSONObject("grpBNew"),
                        tokData.getString("id_C"),
                        tokData.getString("id_U"),
                        tokData.getString("grpU"),
                        tokData.getJSONObject("wrdNU"));
            }
        }

    @SecurityParameter
    @PostMapping("/v2/dgConfirmOrder")
    public ApiResponse dgConfirmOrder(@RequestBody JSONObject reqJson) throws IOException {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        return actionService.dgConfirmOrder(
                tokData.getString("id_C"),
                reqJson.getJSONArray("casList"));
    }


    @SecurityParameter
        @PostMapping("/v2/getFlowList")
        public ApiResponse getFlowList(@RequestBody JSONObject reqJson){
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
            return actionService.getFlowList(
                    tokData.getString("id_C"),
                    reqJson.getString("grpB"));
        }


    @SecurityParameter
    @PostMapping("/v1/actionChart")
    public ApiResponse actionChart(@RequestBody JSONObject json) {
        return actionService.actionChart(
                json.getString("id_O")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/applyForScore")
    public ApiResponse applyForScore(@RequestBody JSONObject reqJson){
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        return actionService.applyForScore(reqJson.getString("id_O"), reqJson.getInteger("index")
                , tokData.getString("id_C"), tokData.getString("id_U"),reqJson.getJSONArray("id_Us"));
    }

    @SecurityParameter
    @PostMapping("/v1/haveScore")
    public ApiResponse haveScore(@RequestBody JSONObject reqJson){
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        return actionService.haveScore(reqJson.getString("id_O"), reqJson.getInteger("index"), reqJson.getInteger("score")
                , tokData.getString("id_C"), tokData.getString("id_U"),reqJson.getJSONArray("id_Us"));
    }

    @SecurityParameter
    @PostMapping("/v1/foCount")
    public ApiResponse foCount(@RequestBody JSONObject reqJson){
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        return actionService.foCount(reqJson.getString("id_O"), reqJson.getInteger("index")
                , tokData.getString("id_C"), tokData.getString("id_U"),reqJson.getJSONArray("id_Us"));
    }

}
