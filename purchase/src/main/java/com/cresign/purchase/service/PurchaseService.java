//package com.cresign.purchase.service;
//
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.tools.apires.ApiResponse;
//
//import java.io.IOException;
//import java.text.ParseException;
//import java.util.List;
//
//public interface PurchaseService {
//
//    /**
//     * 新增stripe的产品和价格
//     * @param orderId	订单id
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/25 13:58
//     */
//    ApiResponse addStripeProductAndPrice(String orderId);
//
//    /**
//     * 根据orderId查询微信支付订单结果
//     * @param orderId	订单编号
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/8 8:19
//     */
//    ApiResponse wxGetOrder(String orderId);
//
//    /**
//     * 根据orderId和id_C生成微信支付二维码
//     * @param orderId	订单编号
//     * @param id_C	公司编号
//     * @return java.lang.String  返回结果: 支付二维码
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/8 8:19
//     */
//    ApiResponse wxCharge(String orderId,String id_C);
//
//    /**
//     * stripe支付成功回调方法
//     * @param orderId	订单编号
//     * @param id_C	公司编号
//     * @param uId	用户编号
//     * @return java.lang.String  返回结果: 添加结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/8 8:20
//     */
//    ApiResponse stripeChargeNew(String orderId,String id_C,String uId);
//
//    /**
//     * 计算续费算法方法
//     * @param map	需要参数
//     * @return java.lang.String  返回结果: 计算结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/8 8:21
//     */
//    //String renewalCalculation(Map<String,Object> map);
//
//    /**
//     * 查看用户是这家公司的三大管理者之一吗
//     * @author Jevon
//     * @param id_U 用户id
//     * @param id_C 公司id
//     * @ver 1.0
//     * @updated 2020/8/6 8:53
//     * @return java.lang.String
//     */
//    ApiResponse inspectUserDef(String id_U, String id_C);
//
//
//    /**
//     * 确认订单，把订单数据写进redis,设置过期时间
//     * @author Jevon
//     * @param id_U          用户id
//     * @param optionType    选择（0购买/1续费/2升级）
//     * @param data          订单数据
//     * @ver 1.0
//     * @updated 2020/8/6 8:50
//     * @return java.lang.String
//     */
//    ApiResponse confirmOrder(String id_U,Integer optionType, JSONObject data) throws ParseException;
//
//    /**
//     * 查询模块的价格
//     * @author JackSon
//     * @param id_U          用户id
//     * @param reqJson       传入参数
//     * @ver 1.0
//     * @updated 2020/8/24 16:38
//     * @return java.lang.String
//     */
//    ApiResponse getPurchaseModuleMoney(String id_U, JSONObject reqJson);
//
//    /**
//     * 发送短信验证码
//     * @author JackSon
//     * @param id_U          用户id
//     * @param id_C          公司id
//     * @param phoneType     手机号类型
//     * @param phone         手机号
//     * @ver 1.0
//     * @updated 2020/8/24 17:11
//     * @return java.lang.String
//     */
//    String sendBuySms(String id_U, String id_C, String phoneType, String phone);
//
//
//    /**
//     * 校验支付验证码是否正确
//     * @author JackSon
//     * @param id_U      用户编号
//     * @param id_C      公司编号
//     * @param smsCode   验证码
//     * @param phone     用户手机号
//     * @ver 1.0
//     * @updated 2020/8/24 18:45
//     * @return java.lang.String
//     */
//    ApiResponse checkBuySms(String id_U, String id_C, String smsCode, String phone);
//
//
//    /**
//     * 检查该用户是否已经购买了该模块
//     * @author JackSon
//     * @param id_U          用户id
//     * @param id_C          公司id
//     * @param ref           模块名称
//     * @param bcdLevel      模块等级
//     * @ver 1.0
//     * @updated 2020/8/24 18:54
//     * @return java.lang.String
//     */
//    ApiResponse inspectModuleIsBuy(String id_U, String id_C, String ref, Integer bcdLevel);
//
//    /**
//     * 模块升级计算价格
//     * @author JackSon
//     * @param id_U      用户编号
//     * @param reqJson   前端传入的参数
//     * @ver 1.0
//     * @updated 2020/8/24 19:04
//     * @return java.lang.String
//     */
//    //String upModuleMoney(String id_U, JSONObject reqJson);
//
//    /**
//     * 设置模块人数  加
//     * @author Jevon
//     * @param id_A
//     * @param id_U
//     * @param id_C
//     * @param ref       模块编号
//     * @param bcdLevel   模块版本
//     * @param users     用户数组
//     * @ver 1.0
//     * @updated 2020/11/16 13:53
//     * @return java.lang.String
//     */
//    ApiResponse moduleUpdataPlusUser(String id_A,String id_U, String id_C, String ref, Integer bcdLevel, List users) throws IOException;
//
//
//    /**
//     * 设置模块人数  减
//     * @author Jevon
//     * @param id_A
//     * @param id_U
//     * @param id_C
//     * @param ref
//     * @param bcdLevel
//     * @param reduce
//     * @ver 1.0
//     * @updated 2020/11/16 13:55
//     * @return java.lang.String
//     */
//    ApiResponse moduleUpdataReduceUser(String id_A,String id_U, String id_C, String ref, Integer bcdLevel, List reduce) throws IOException;
//
//
//    /**
//     * 设置模块管理人数  加 + 减  一起
//     * @author Jevon
//     * @param id_A
//     * @param id_U
//     * @param id_C
//     * @param ref
//     * @param bcdLevel
//     * @param plusManager
//     * @param reduceManager
//     * @ver 1.0
//     * @updated 2020/11/16 13:55
//     * @return java.lang.String
//     */
//    ApiResponse moduleUpAdministrators(String id_A, String id_U, String id_C, String ref, Integer bcdLevel,  List plusManager,List reduceManager);
//
//
//    /**
//     * 修改模块状态
//     * @author Jevon
//     * @param id_A
//     * @param id_U
//     * @param id_C
//     * @param ref
//     * @param bcdLevel
//     * @param bcdState
//     * @ver 1.0
//     * @updated 2020/11/16 13:55
//     * @return java.lang.String
//     */
//    ApiResponse changeState(String id_A, String id_U, String id_C, String ref, Integer bcdLevel,Integer bcdState);
//
//
//
//    /**
//     * 续费（月份+人数）
//     * @author Jevon
//     * @param id_U
//     * @param reqJson
//     * @ver 1.0
//     * @updated 2020/12/19 15:27
//     * @return java.lang.String
//     */
//    ApiResponse  renewModuleMoney(String id_U, JSONObject reqJson) throws ParseException;
//
//
//}
