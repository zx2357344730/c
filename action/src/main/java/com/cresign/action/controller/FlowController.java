package com.cresign.action.controller;


import com.alibaba.fastjson.JSONObject;
import com.cresign.action.common.ActionEnum;
import com.cresign.action.service.FlowService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.common.Constants;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
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
@RequestMapping("/flow")
public class FlowController {


        @Autowired
        private FlowService flowService;

        @Autowired
        private HttpServletRequest request;

        @Autowired
        private GetUserIdByToken getUserToken;

        @SecurityParameter
        @PostMapping("/v1/timeHandle")
        public ApiResponse timeHandle(@RequestBody JSONObject reqJson){
            return flowService.timeHandle(
                    reqJson.getString("id_O"),
                    getUserToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    reqJson.getString("id_C"),
                    reqJson.getLong("teStart"),
                    reqJson.getInteger("wn0TPrior"));
        }

        @SecurityParameter
        @PostMapping("/v1/removeTime")
        public ApiResponse removeTime(@RequestBody JSONObject reqJson){
            return flowService.removeTime(reqJson.getString("id_O"),
                    reqJson.getString("id_C"));
        }


        /**
         * 根据请求参数，获取更新后的订单oitem
         * @param map	请求参数
         * @return java.lang.String  返回结果: 日志结果
         * @author tang
         * @ver 1.0.0
         * ##Updated: 2020/8/6 9:08
         */
        @SecurityParameter
        @PostMapping("/v1/setDgAllBmdpt")
        public ApiResponse dgUpdatePartInfo(@RequestBody JSONObject map){
            String id_P = map.getString("id_P");
            String id_C = map.getString(Constants.GET_ID_C);
//            try {
                return flowService.dgUpdatePartInfo(id_P, id_C);
//            } catch (Exception e)
//            {
//                e.printStackTrace();
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_NO_RECURSION_PART.getCode(), "产品需要更新");
//            }
        }

        @SecurityParameter
        @PostMapping("/v1/prodPart")
        public ApiResponse prodPart(@RequestBody JSONObject json) {

            try {
                return flowService.prodPart(json.getString("id_P"));
            } catch (Exception e)
            {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_NO_RECURSION_PART.getCode(), "产品需要更新");
            }
        }

        /**
         * 根据prodID进行递归,Es数据库2
         * @return java.lang.String  返回结果: 递归结果
         * @author tang
         * @ver 1.0.0
         * ##Updated: 2020/8/6 9:03
         * getDg
         */
        @SecurityParameter
        @PostMapping("/v2/getDg")
        public ApiResponse getDgResult(@RequestBody JSONObject reqJson){

            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

            try {
            return flowService.getDgResult(
                    reqJson.getString("id_O"),
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    reqJson.getLong("teStart"));


            } catch (Exception e)
            {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_NO_RECURSION_PART.getCode(), "产品需要更新");
            }
        }


        /**
         * 递归验证
         * @param map	请求参数
         * @return java.lang.String  返回结果: 递归结果
         * @author tang
         * @ver 1.0.0
         * ##Updated: 2020/8/6 9:03
         */
        @SecurityParameter
        @PostMapping("/v1/dgCheck")
        public ApiResponse dgCheck(@RequestBody JSONObject map) {
            // 获取产品id
            String id_P = map.getString("id_P");
            String id_C = map.getString("id_C");
            return flowService.dgCheck(id_P, id_C);
        }

//    /**
//     * 递归发日志 改isPush
//     * @param map	请求参数
//     * @return java.lang.String  返回结果: 递归结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/6 9:03
//     */
//    @SecurityParameter
//    @PostMapping("/v1/dgActivate")
//    public ApiResponse dgActivate(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//
//        return actionService.dgActivate(
//                reqJson.getString("id_O"),
//                reqJson.getInteger("index"),
//                reqJson.getString("id_C"),
//                tokData.getString("id_U"),
//                tokData.getString("grpU"),
//                tokData.getString("dep"),
//                tokData.getJSONObject("wrdNU"));
//    }

//    @SecurityParameter
//    @PostMapping("/v1/dgActivateAll")
//    public ApiResponse dgActivateAll(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//
//        if (tokData.getJSONObject("modAuth").getInteger("a-core").equals(null) || tokData.getJSONObject("modAuth").getInteger("a-core") >= 2)
//        {
//            //throw new Exception Not yet purchase
//        }
//
//        return actionService.dgActivateAll(
//                reqJson.getString("id_O"),
//                reqJson.getString("id_C"),
//                tokData.getString("id_U"),
//                tokData.getString("grpU"),
//                tokData.getString("dep"),
//                tokData.getJSONObject("wrdNU"));
//    }

    @SecurityParameter
    @PostMapping("/v1/dgRemove")
    public ApiResponse dgRemove(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        return flowService.dgRemove(
                reqJson.getString("id_O"),
                tokData.getString("id_C"),
                tokData.getString("id_U"));
    }


    /**
     * 双方确认订单
     * @param map	请求参数
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/10/27 9:03
     */
//    @SecurityParameter
//    @PostMapping("/v1/confirmOrder")
//    public ApiResponse confirmOrder(@RequestBody JSONObject reqJson) throws IOException {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        return actionService.confirmOrder(
//                tokData.getString("id_C"),
//                reqJson.getString("id_O"));
//    }
//
//    @SecurityParameter
//    @PostMapping("/v1/cancelOrder")
//    public ApiResponse cancelOrder(@RequestBody JSONObject reqJson){
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        return actionService.cancelOrder(
//                tokData.getString("id_C"),
//                reqJson.getString("id_O"));
//    }

    /**
     * 通用日志方法(action,prob,msg)
     * @param map	请求参数
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
//    @SecurityParameter
//    @PostMapping("/v2/statusChange")
//    public ApiResponse statusChange(@RequestBody JSONObject reqJson){
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        return actionService.changeActionStatus(
//                reqJson.getString("logType"),
//                reqJson.getInteger("status"),
//                reqJson.getString("msg"),
//                reqJson.getInteger("index"),
//                reqJson.getString("id_O"),
//                reqJson.getString("id_FC"),
//                reqJson.getString("id_FS"),
//                tokData.getString("id_C"),
//                tokData.getString("id_U"),
//                tokData.getString("grpU"),
//                tokData.getString("dep"),
//                tokData.getJSONObject("wrdNU"));
//    }


//    @SecurityParameter
//    @PostMapping("/v1/createTask")
//    public ApiResponse createTask(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//
//        return actionService.createTask(
//                reqJson.getString("logType"),
//                reqJson.getString("id_FC"),
//                reqJson.getString("id_O"),
//                tokData.getString("id_C"),
//                tokData.getString("id_U"),
//                tokData.getString("grpU"),
//                tokData.getString("dep"),
//                reqJson.getJSONObject("oItemData"),
//                tokData.getJSONObject("wrdNU"));
//    }
//
//    @SecurityParameter
//    @PostMapping("/v1/up_FC_action_grpB")
//    public ApiResponse up_FC_action_grpB(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        return actionService.up_FC_action_grpB(
//                tokData.getString("id_C"),
//                reqJson.getString("id_O"),
//                reqJson.getString("dep"),
//                reqJson.getString("depMain"),
//                reqJson.getString("logType"),
//                reqJson.getString("id_Flow"),
//                reqJson.getJSONObject("wrdFC"),
//                reqJson.getJSONArray("grpB"),
//                reqJson.getJSONArray("wrdGrpB"));
//
//    }
//
//    @SecurityParameter
//    @PostMapping("/v1/createQuest")
//    public ApiResponse createQuest(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//
//        return actionService.createQuest(
//                tokData.getString("id_C"),
//                reqJson.getString("id_O"),
//                reqJson.getInteger("index"),
//                reqJson.getString("id_Prob"),
//                reqJson.getString("id_FC"),
//                reqJson.getString("id_FQ"),
//                tokData.getString("id_U"),
//                tokData.getString("grpU"),
//                tokData.getString("dep"),
//                tokData.getJSONObject("wrdNU"),
//                reqJson.getJSONObject("probData"));
//    }

//    @SecurityParameter
//    @PostMapping("/v1/genAction")
//    public ApiResponse genAction(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//
//        return actionService.genAction(
//                tokData.getString("id_C"),
//                reqJson.getString("id_O"),
//                tokData.getString("id_U"),
//                tokData.getString("grpU"),
//                tokData.getString("dep"),
//                tokData.getJSONObject("wrdNU"),
//                reqJson.getString("id_FC")
//                );
//    }
    /**
         * 检查part是否为空
         * @return java.lang.String  返回结果: 结果
         * @author tang
         * @ver 1.0.0
         * @date 2021/1/19 10:00
         */
        @SecurityParameter
        @PostMapping("/v1/getPartIsNull")
        public ApiResponse getPartIsNull(@RequestBody JSONObject reqJson) throws IOException {
            return flowService.getPartIsNull(
                    reqJson.getString("id_P"));
        }

        /**
         * 更新Order的grpBGroup字段
         * @param map	请求参数
         * @return java.lang.String  返回结果: 结果
         * @author tang
         * @ver 1.0.0
         * @date 2021/1/19 10:05
         */
//        @SecurityParameter
//        @PostMapping("/v1/changeDepAndFlow")
//        public ApiResponse changeDepAndFlow(@RequestBody JSONObject reqJson){
//            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//
//            return actionService.changeDepAndFlow(
//                    reqJson.getString("id_O"),
//                    reqJson.getString("grpB"),
//                    reqJson.getJSONObject("grpBOld"),
//                    reqJson.getJSONObject("grpBNew"),
//                    tokData.getString("id_C"),
//                    tokData.getString("id_U"),
//                    tokData.getString("grpU"),
//                    tokData.getJSONObject("wrdNU"));
//        }

//    @SecurityParameter
//    @PostMapping("/v2/dgConfirmOrder")
//    public ApiResponse dgConfirmOrder(@RequestBody JSONObject reqJson) throws IOException {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//        return actionService.dgConfirmOrder(
//                tokData.getString("id_C"),
//                reqJson.getJSONArray("casList"));
//    }
//
//
//    @SecurityParameter
//        @PostMapping("/v2/getFlowList")
//        public ApiResponse getFlowList(@RequestBody JSONObject reqJson){
//            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//            return actionService.getFlowList(
//                    tokData.getString("id_C"),
//                    reqJson.getString("grpB"));
//        }


}
