package com.cresign.action.controller;


import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.FlowService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.common.Constants;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/8/6 10:05
 * @ver 1.0
 */
@RestController
@RequestMapping("flow")
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
                return flowService.dgUpdatePartInfo(id_P, id_C);

        }

        @SecurityParameter
        @PostMapping("/v1/prodPart")
        public ApiResponse prodPart(@RequestBody JSONObject json) {

                return flowService.prodPart(json.getString("id_P"));

        }

        @SecurityParameter
        @PostMapping("/v1/dgTaskOrder")
        public ApiResponse dgTaskOrder(@RequestBody JSONObject reqJson){
            return flowService.dgTaskOrder(
                    reqJson.getString("id_O")
                    );
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
            } catch (Exception e) {
                return getUserToken.err("flowService.getDG", e);
            }
        }

        @SecurityParameter
        @PostMapping("/v1/getDgSingle")
        public ApiResponse getDgSingle(@RequestBody JSONObject reqJson){

            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            try {
            return flowService.getDgSingle(
                         reqJson.getString("id_O"),
                         reqJson.getInteger("index"),
                         tokData.getString("id_U"),
                         tokData.getString("id_C"),
                         reqJson.getLong("teStart"));
            } catch (Exception e)
            {
                return getUserToken.err("flowService.getDgSingle", e);
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
         * 检查part是否为空
         * @return java.lang.String  返回结果: 结果
         * @author tang
         * @ver 1.0.0
         * @date 2021/1/19 10:00
         */
        @SecurityParameter
        @PostMapping("/v1/getPartIsNull")
        public ApiResponse getPartIsNull(@RequestBody JSONObject reqJson) {
            return flowService.getPartIsNull(
                    reqJson.getString("id_P"));
        }


}
