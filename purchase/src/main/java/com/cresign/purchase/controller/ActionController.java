package com.cresign.purchase.controller;


import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.enumeration.PurchaseEnum;
import com.cresign.purchase.service.ActionService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/action")
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

//    @PostMapping("/v1/delPi")
//    @SecurityParameter
//    public ApiResponse delPi(@RequestBody JSONObject reqJson){
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        reqJson.put("id_U",tokData.getString("id_U"));
//        return actionService.delPi(
//                reqJson.getString("rname")
//                ,reqJson.getString("id_C"));
//    }
//
//    @PostMapping("/v1/rpiCode")
//    @SecurityParameter
//    public ApiResponse rpiCode(@RequestBody JSONObject reqJson){
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        reqJson.put("id_U",tokData.getString("id_U"));
//        return actionService.rpiCode(
//                reqJson.getString("rname"),
//                reqJson.getString("id_C"));
//    }
//
//    @PostMapping("/v1/requestRpiStatus")
//    @SecurityParameter
//    public ApiResponse requestRpiStatus(@RequestBody JSONObject reqJson){
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        reqJson.put("id_U",tokData.getString("id_U"));
//        return actionService.requestRpiStatus(
//                reqJson.getString("token"),
//                reqJson.getString("id_C"),
//                reqJson.getString("id_U"));
//    }
//
//    @PostMapping("/v1/bindingRpi")
//    @SecurityParameter
//    public ApiResponse bindingRpi(@RequestBody JSONObject reqJson){
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        reqJson.put("id_U",tokData.getString("id_U"));
//        return actionService.bindingRpi(
//                reqJson.getString("token"),
//                reqJson.getString("id_C"),
//                reqJson.getString("id_U"),
//                reqJson.getString("grpU"),
//                reqJson.getInteger("oIndex"),
//                reqJson.getJSONObject("wrdNU"),
//                reqJson.getInteger("imp"),
//                reqJson.getString("id_O"),
//                reqJson.getInteger("tzone"),
//                reqJson.getString("lang"),
//                reqJson.getString("id_P"),
//                reqJson.getString("pic"),
//                reqJson.getInteger("wn2qtynow"),
//                reqJson.getString("grpB"),
//                reqJson.getJSONObject("fields"),
//                reqJson.getJSONObject("wrdNP"),
//                reqJson.getJSONObject("wrdN"),
//                reqJson.getString("dep"));
//    }
//
//    @PostMapping("/v1/relieveRpi")
//    @SecurityParameter
//    public ApiResponse relieveRpi(@RequestBody JSONObject reqJson){
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        reqJson.put("id_U",tokData.getString("id_U"));
//        return actionService.relieveRpi(
//                reqJson.getString("token"),
//                reqJson.getString("id_C"),
//                reqJson.getString("id_U"));
//    }

    @SecurityParameter
    @PostMapping("/v1/dgActivateAll")
    public ApiResponse dgActivateAll(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {

        return actionService.dgActivateAll(
                reqJson.getString("id_O"),
                reqJson.getString("id_C"),
                tokData.getString("id_U"),
                tokData.getString("grpU"),
                tokData.getString("dep"),
                tokData.getJSONObject("wrdNU"));

        } catch (Exception e)
        {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.ASSET_NOT_FOUND.getCode(), "产品需要更新");
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
    public ApiResponse rePush(@RequestBody JSONObject reqJson) throws IOException {
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
    }

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



}
