//
//package com.cresign.purchase.controller;
//
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.purchase.service.impl.PurchaseServiceImpl;
//import com.cresign.tools.annotation.SecurityParameter;
//import com.cresign.tools.apires.ApiResponse;
//import com.cresign.tools.token.GetUserIdByToken;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.text.ParseException;
//import java.util.Map;
//
///**
// * 购买模块控制层
// * @author JackSon
// * @ver 1.0
// * @updated 2020/8/24 19:32
// */
//@RequestMapping("purchase")
//@RestController
//public class PurchaseController {
//
//
//
//    @Autowired
//    private PurchaseServiceImpl purchaseService;
//
//    @Autowired
//    private HttpServletRequest request;
//
//    @Autowired
//    private GetUserIdByToken getUserIdByToken;
//
//    /**
//     * 新增stripe的产品和价格
//     * @param map   需要参数
//     * @return  价格id
//     * @author tang
//     */
//    @SecurityParameter
//    @PostMapping("/v1/addStripeProdAndPrice")
//    public ApiResponse addStripeProdAndPrice(@RequestBody JSONObject map){
//        return purchaseService.addStripeProductAndPrice(map.getString("orderId"));
//    }
//
//
//
//    /**
//     * 微信查询订单支付接口
//     * @param map	请求参数
//     * @return java.lang.String  返回结果: 支付结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/8 9:36
//     */
//    @SecurityParameter
//    @PostMapping("/v1/wxGetOrder")
//    public ApiResponse wxGetOrder(@RequestBody JSONObject map) {
//        return purchaseService.wxGetOrder(map.getString("oId"));
//    }
//
//    /**
//     * 微信获取支付二维码接口
//     * @param map	请求参数
//     * @return java.lang.String  返回结果: 二维码结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/8 9:36
//     */
//    @SecurityParameter
//    @PostMapping("/v1/wxCharge")
//    public ApiResponse wxCharge(@RequestBody JSONObject map) {
//        return purchaseService.wxCharge(map.getString("oId"),map.getString("id_C"));
//    }
//
//    /**
//     * stripe支付成功回调方法
//     * @param map	请求参数
//     * @return java.lang.String  返回结果: 添加结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/8 8:20
//     */
//    @SecurityParameter
//    @PostMapping("/v1/stripeChargeNew")
//    public ApiResponse stripeChargeNew(@RequestBody JSONObject map) {
////        String uid = getUserIdByToken.getTokenOfUserId(request.getHeader("authorization")
////                , request.getHeader("clientType"));
////        return purchaseService.stripeChargeNew(map.getString("orderId"),map.getString("id_C"),uid);
//        return null;
//    }
//
//    /**
//     * 计算续费算法方法
//     * @param map	需要参数
//     * @return java.lang.String  返回结果: 计算结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/8 9:37
//     */
////    @SecurityParameter
////    @PostMapping("/v1/getRenewalCalculation")
////    public String getRenewalCalculation(@RequestBody Map<String,Object> map){
////        return purchaseService.renewalCalculation(map);
////    }
//
//    /**
//     * 查看用户是这家公司的三大管理者之一吗
//     * @author Jevon
//     * @param reqMap
//     * @ver 1.0
//     * @updated 2020/8/6 8:54
//     * @return java.lang.String
//     */
//    @SecurityParameter
//    @PostMapping("/v1/inspectUserDef")
//    public ApiResponse inspectUserDef(@RequestBody Map<String, Object> reqMap) {
//        //String uid = "u-46f807dcbd944320be98f73bcdbceda0";
//        return purchaseService.inspectUserDef(
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                //uid,
//                reqMap.get("id_C").toString());
//    }
//
//
//    /**
//     * 确认订单，把订单数据写进redis,设置过期时间
//     * @author Jevon
//     * @param reqMap    订单数据
//     * @ver 1.0
//     * @updated 2020/8/6 8:49
//     * @return java.lang.String
//     */
//    @SecurityParameter
//    @PostMapping("/v1/confirmOrder")
//    public ApiResponse confirmOrder(@RequestBody JSONObject reqMap) throws ParseException {
//
//        return purchaseService.confirmOrder(
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                //"5f28bf314f65cc7dc2e60386",
//                reqMap.getInteger("optionType"),
//                reqMap.getJSONObject("data"));
//    }
//
//
//    /**
//     * 查询模块的价格
//     * @author JackSon
//     * @param reqJson       请求参数
//     * @ver 1.0
//     * @updated 2020/8/24 19:02
//     * @return java.lang.String
//     */
//    @SecurityParameter
//    @PostMapping("/v1/getPurchaseModuleMoney")
//    public ApiResponse getPurchaseModuleMoney(@RequestBody JSONObject reqJson) {
//        return purchaseService.getPurchaseModuleMoney(
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                //"5f28bf314f65cc7dc2e6034a",
//                reqJson);
//    }
//
//    /**
//     * 发送短信验证码
//     * @author JackSon
//     * @param reqJson       请求参数
//     * @ver 1.0
//     * @updated 2020/8/24 19:03
//     * @return java.lang.String
//     */
//    @SecurityParameter
//    @PostMapping("/v1/sendBuySms")
//    public String sendBuySms(@RequestBody JSONObject reqJson) {
//        return purchaseService.sendBuySms(
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("phoneType"),
//                reqJson.getString("phone"));
//
//    }
//
//
//    /**
//     * 校验支付验证码是否正确
//     * @author JackSon
//     * @param reqJson       请求参数
//     * @ver 1.0
//     * @updated 2020/8/24 19:02
//     * @return java.lang.String
//     */
//    @SecurityParameter
//    @PostMapping("/v1/checkBuySms")
//    public ApiResponse checkBuySms(@RequestBody JSONObject reqJson) {
//        return purchaseService.checkBuySms(
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("smsCode"),
//                reqJson.getString("phone"));
//    }
//
//    /**
//     * 检查该用户是否已经购买了该模块
//     * @author JackSon
//     * @param reqJson       请求参数
//     * @ver 1.0
//     * @updated 2020/8/24 19:02
//     * @return java.lang.String
//     */
//    @SecurityParameter
//    @PostMapping("/v1/inspectModuleIsBuy")
//    public ApiResponse inspectModuleIsBuy(@RequestBody JSONObject reqJson) {
//        return purchaseService.inspectModuleIsBuy(
//                //"5f28bf314f65cc7dc2e60346",
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("ref"),
//                reqJson.getInteger("bcdLevel"));
//    }
//
//
////    /**
////     * 模块升级计算价格
////     * @author JackSon
////     * @param reqJson   前端传入参数
////     * @ver 1.0
////     * @updated 2020/8/24 19:09
////     * @return java.lang.String
////     */
////
////    @PostMapping("/v1/upModuleMoney")
////    public String upModuleMoney(@RequestBody JSONObject reqJson) {
////        return purchaseService.upModuleMoney(
////                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
////                reqJson);
////    }
//
//    /**
//     * 设置模块人数  加 和 变更版本的删除（版本1里面有A，把A放到版本2中，那版本1中的A就删除了）
//     * @author Jevon
//     * @param reqJson
//     * @ver 1.0
//     * @updated 2020/11/21 14:02
//     * @return java.lang.String
//     */
//    @PostMapping("/v1/moduleUpdataPlusUser")
//    @SecurityParameter
//    public ApiResponse moduleUpdataPlusUser(@RequestBody JSONObject reqJson) throws IOException {
//        return purchaseService.moduleUpdataPlusUser(
//                reqJson.getString("id_A"),
//                //"5f28bf314f65cc7dc2e60346",
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("ref"),
//                reqJson.getInteger("bcdLevel"),
//                reqJson.getJSONArray("users"));
//    }
//
//    /**
//     * 设置模块人数  减
//     * @author Jevon
//     * @param reqJson
//     * @ver 1.0
//     * @updated 2020/11/21 14:02
//     * @return java.lang.String
//     */
//    @PostMapping("/v1/moduleUpdataReduceUser")
//    @SecurityParameter
//    public ApiResponse moduleUpdataReduceUser(@RequestBody JSONObject reqJson) throws IOException {
//        return purchaseService.moduleUpdataReduceUser(
//                reqJson.getString("id_A"),
//                //"5f28bf314f65cc7dc2e60346",
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("ref"),
//                reqJson.getInteger("bcdLevel"),
//                reqJson.getJSONArray("reduce"));
//    }
//
//    /**
//     * 设置模块管理人数  加 + 减  一起
//     * @author Jevon
//     * @param reqJson
//     * @ver 1.0
//     * @updated 2020/11/21 14:02
//     * @return java.lang.String
//     */
//    @PostMapping("/v1/moduleUpAdministrators")
//    @SecurityParameter
//    public ApiResponse moduleUpAdministrators(@RequestBody JSONObject reqJson) throws IOException {
//        return purchaseService.moduleUpAdministrators(
//                reqJson.getString("id_A"),
//                //"5f28bf314f65cc7dc2e60346",
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("ref"),
//                reqJson.getInteger("bcdLevel"),
//                reqJson.getJSONArray("plusManager"),
//                reqJson.getJSONArray("reduceManager"));
//    }
//
//    /**
//     * 修改模块状态
//     * @author Jevon
//     * @param reqJson
//     * @ver 1.0
//     * @updated 2020/11/21 14:03
//     * @return java.lang.String
//     */
//    @PostMapping("/v1/changeState")
//    @SecurityParameter
//    public ApiResponse changeState(@RequestBody JSONObject reqJson) throws IOException {
//        return purchaseService.changeState(
//                reqJson.getString("id_A"),
//                //"5f28bf314f65cc7dc2e60346",
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("ref"),
//                reqJson.getInteger("bcdLevel"),
//                reqJson.getInteger("bcdState"));
//    }
//
//
//
//
//
//    /**
//     * 续费（月份+人数）
//     * @author Jevon
//     * @param reqJson
//     * @ver 1.0
//     * @updated 2021/1/16 13:37
//     * @return java.lang.String
//     */
//    @SecurityParameter
//    @PostMapping("/v1/renewModuleMoney")
//    public ApiResponse renewModuleMoney(@RequestBody JSONObject reqJson) throws ParseException {
//
//        return purchaseService.renewModuleMoney(
//                //"5f28bf314f65cc7dc2e60346",
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson);
//    }
//
//
//
//
//    @Autowired
//    private StringRedisTemplate redisTemplate0;
//
//    @GetMapping("lll")
//    public void tttt() {
//        redisTemplate0.opsForValue().set("222", "222");
//    }
//
//
//
//}
//
