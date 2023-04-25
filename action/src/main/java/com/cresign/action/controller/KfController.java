//package com.cresign.action.controller;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.action.service.KfActionService;
//import com.cresign.tools.annotation.SecurityParameter;
//import com.cresign.tools.apires.ApiResponse;
//import com.cresign.tools.common.Constants;
//import com.cresign.tools.token.GetUserIdByToken;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//
///**
// * ##description:
// * @author JackSon
// * @updated 2020/8/6 10:05
// * @ver 1.0
// */
//@RestController
//@RequestMapping("cusmsg")
//public class KfController {
//    /**
//     * 自动注入HttpServletRequest类
//     */
//    @Autowired
//    private HttpServletRequest request;
//
//    @Autowired
//    private KfActionService kfActionService;
//
//    @Autowired
//    private GetUserIdByToken getUserIdByToken;
//
//
//
//
//    /**
//     * 恢复客服信息,Es数据库
//     * @param map	请求参数
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/10/27 9:03
//     */
//    @SecurityParameter
//    @PostMapping("/v1/getRecoveryKf")
//    public ApiResponse getRecoveryKf(@RequestBody JSONObject map){
//        // 获取订单id
//        String oId = map.getString("id_O");
//        String cId = map.getString("id_C");
////        String type = map.getString("type");
////        String id_U = getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"));
////        String kf = map.getString("kf");
////        Integer indexOnly = map.getInteger(Constants.GET_INDEX_ONLY);
////        return kfActionService.getRecoveryKf(oId,type,id_U,kf,indexOnly);
//
//        return kfActionService.getRecoveryKf(oId,cId);
//    }
//
//    /**
//     * 恢复客服信息,并且携带用户基础信息,Es数据库
//     * @param map	请求参数
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/10/27 9:03
//     */
//    @SecurityParameter
//    @PostMapping("/v1/getRecoveryKfAndUserInfo")
//    public ApiResponse getRecoveryKfAndUserInfo(@RequestBody JSONObject map){
//        // 获取订单id
//        String oId = map.getString(Constants.REQUEST_OID);
//        String type = map.getString("type");
//        Integer indexOnly = map.getInteger(Constants.GET_INDEX_ONLY);
//        String id_U = getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"));
//        String kf = map.getString("kf");
//        return kfActionService.getRecoveryKfAndUserInfo(oId,type,indexOnly,id_U,kf);
//    }
//
//    /**
//     * 用户评分
//     * @param map	请求参数
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2021/1/19 10:05
//     */
//    @SecurityParameter
//    @PostMapping("/v1/getScoreUser")
//    public ApiResponse getScoreUser(@RequestBody JSONObject map){
//        String id_U = map.getString("id_U");
//        String id_C = map.getString("id_C");
//        String id_O = map.getString("id_O");
//        String uuId = map.getString("uuId");
//        Integer score = map.getInteger("score");
//        return kfActionService.getScoreUser(id_U,id_C,id_O,uuId,score);
//    }
//
//
//
//}
