package com.cresign.action.controller;


import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.FlowNewService;
import com.cresign.action.service.FlowService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
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
        private FlowNewService flowNewService;

        @Autowired
        private HttpServletRequest request;

        @Autowired
        private GetUserIdByToken getUserToken;

        /**
         * 时间处理
         * @param reqJson 请求参数
         * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
         * @author tang
         * @ver 1.0.0
         * @date 2022/9/12
         */
        @SecurityParameter
        @PostMapping("/v1/timeHandle")
        public ApiResponse timeHandle(@RequestBody JSONObject reqJson){
            try {
                JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
                return flowService.timeHandle(
                        reqJson.getString("id_O"),
                        tokData.getString("id_U"),
                        tokData.getString("id_C"),
                        reqJson.getLong("teStart"),
                        reqJson.getInteger("wn0TPrior"));
            } catch (Exception e) {
                return getUserToken.err(reqJson, "FlowController.timeHandle", e);
            }
        }

        /**
         * 删除时间处理信息
         * @param reqJson	请求参数
         * @return 返回结果: {@link ApiResponse}
         * @author tang
         * @date 创建时间: 2023/9/12
         * @ver 版本号: 1.0.0
         */
        @SecurityParameter
        @PostMapping("/v1/removeTime")
        public ApiResponse removeTime(@RequestBody JSONObject reqJson){
            try {
                JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
                return flowService.removeTime(reqJson.getString("id_O"),
                        tokData.getString("id_C"));
            } catch (Exception e) {
                return getUserToken.err(reqJson, "FlowController.removeTime", e);
            }
        }


        /**
         * 根据请求参数，获取更新后的订单oitem
         * @param reqJson	请求参数
         * @return java.lang.String  返回结果: 日志结果
         * @author tang
         * @ver 1.0.0
         * ##Updated: 2020/8/6 9:08
         */
        @SecurityParameter
        @PostMapping("/v1/setDgAllBmdpt")
        public ApiResponse dgUpdatePartInfo(@RequestBody JSONObject reqJson){
            try {
                JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
                String id_P = reqJson.getString("id_P");
                String id_C = tokData.getString("id_C");
                return flowService.dgUpdatePartInfo(id_P, id_C);
            } catch (Exception e) {
                return getUserToken.err(reqJson, "FlowController.dgUpdatePartInfo", e);
            }
        }

    /**
     * 根据请求参数，获取更新后的订单oitem
     * @param reqJson	请求参数
     * @return java.lang.String  返回结果: 日志结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 9:08
     */
    @SecurityParameter
    @PostMapping("/v1/setDgSubInfo")
    public ApiResponse setDgSubInfo(@RequestBody JSONObject reqJson){
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            String id_I = reqJson.getString("id_I");
            String id_C = tokData.getString("id_C");
            return flowService.dgUpdateSubInfo(id_I, id_C);
        } catch (Exception e) {
            return getUserToken.err(reqJson, "flow.setDgSubInfo", e);
        }
    }

        @SecurityParameter
        @PostMapping("/v1/prodPart")
        public ApiResponse prodPart(@RequestBody JSONObject json) {
            try {
                return flowService.prodPart(json.getString("id_P"));
            } catch (Exception e) {
                return getUserToken.err(json, "FlowController.prodPart", e);
            }
        }

        @SecurityParameter
        @PostMapping("/v1/infoPart")
        public ApiResponse infoPart(@RequestBody JSONObject json) {
            try {
                return flowService.infoPart(json.getString("id_I"));
            } catch (Exception e) {
                return getUserToken.err(json, "FlowController.infoPart", e);
            }
        }

        @SecurityParameter
        @PostMapping("/v1/dgTaskOrder")
        public ApiResponse dgTaskOrder(@RequestBody JSONObject reqJson){
            try {
                return flowService.dgTaskOrder(
                        reqJson.getString("id_O")
                );
            } catch (Exception e) {
                return getUserToken.err(reqJson, "FlowController.dgTaskOrder", e);
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
            } catch (Exception e) {
                return getUserToken.err(reqJson, "flowService.getDgResult", e);
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
                return getUserToken.err(reqJson, "flowService.getDgSingle", e);
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
            try {
                JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
                // 获取产品id
                String id_P = map.getString("id_P");
                String id_C = tokData.getString("id_C");
                return flowService.dgCheck(id_P, id_C);
            } catch (Exception e) {
                return getUserToken.err(map, "FlowController.dgCheck", e);
            }
        }


    @SecurityParameter
    @PostMapping("/v1/dgRemove")
    public ApiResponse dgRemove(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
            return flowService.dgRemove(
                    reqJson.getString("id_O"),
                    tokData.getString("id_C"),
                    tokData.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "FlowController.dgRemove", e);
        }
    }

//    /**
//         * 检查part是否为空
//         * @return java.lang.String  返回结果: 结果
//         * @author tang
//         * @ver 1.0.0
//         * @date 2021/1/19 10:00
//         */
//        @SecurityParameter
//        @PostMapping("/v1/getPartIsNull")
//        public ApiResponse getPartIsNull(@RequestBody JSONObject reqJson) {
//            try {
//                return flowService.getPartIsNull(
//                        reqJson.getString("id_P"));
//            } catch (Exception e) {
//                return getUserToken.err(reqJson, "FlowController.getPartIsNull", e);
//            }
//        }

    /**
     * 根据prodID进行递归,Es数据库2
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 9:03
     * getDg
     */
    @SecurityParameter
    @PostMapping("/v3/getDg")
    public ApiResponse getDgResultNew(@RequestBody JSONObject reqJson){

        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {
            return flowNewService.getDgResult(
                    reqJson.getString("id_O"),
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
//                    reqJson.getString("id_C"),
                    reqJson.getLong("teStart"),
                    reqJson.getString("isSplit"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "flowService.getDgResult", e);
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
    @PostMapping("/v2/dgCheck")
    public ApiResponse dgCheckNew(@RequestBody JSONObject map) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            // 获取产品id
            String id_O = map.getString("id_O");
            String id_C = tokData.getString("id_C");
//            String id_C = map.getString("id_C");
            return flowNewService.dgCheckOrder(id_C,id_O);
        } catch (Exception e) {
            return getUserToken.err(map, "FlowController.dgCheck", e);
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
    @PostMapping("/v1/dgCheckInfo")
    public ApiResponse dgCheckInfo(@RequestBody JSONObject map) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            // 获取产品id
            String id_I = map.getString("id_I");
            String id_C = tokData.getString("id_C");
            return flowService.dgCheckInfo(id_I, id_C);
        } catch (Exception e) {
            return getUserToken.err(map, "flow/dgCheckInfo", e);
        }
    }
}
