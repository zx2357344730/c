package com.cresign.login.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

/**
 * ##description:
 * @author JackSon
 * @updated 2021-05-03 9:52
 * @ver 1.0
 */
public interface RedirectService {

    /**
     * 获取发送日志二维码方法
     * @param id_C	公司编号
     * @param id_U	用户编号
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/5/29
     * @ver 版本号: 1.0.0
     */
    ApiResponse generateSaleChkinCode(String id_C, String id_U);

    /**
     * 扫码（扫描）发送日志二维码后请求的方法
     * @param token	token
     * @param longitude	经度
     * @param latitude	纬度
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/5/29
     * @ver 版本号: 1.0.0
     */
    ApiResponse scanSaleChkinCode(String token,String longitude,String latitude);

    /**
     * 生成产品二维码
     * @author JackSon
     * @param id_C
     * @param id_P
     * @param id_U
     * @param mode      模式: frequency, time
     * @param data      模式的数据:
     *      *                  mode为 frequency 的话 data里面则是
     *      *                  {
     *      *                      "count" : 1     // 次数
     *      *                  }
     *      *                  mode 为 time 的话 data里面则是
     *      *                  {
     *      *                      "endTimeMin" : 5, // 二维码无效时间单位是分钟,(redis的key时间)
     *      *                  }
     * @ver 1.0
     * @updated 2021-05-03 9:54
     * @return com.cresign.tools.apires.ApiResponse
     */
//    ApiResponse generateProdCode(String id_C, String id_P, String id_U, String mode, JSONObject data);




    /**
     * 重置产品分享二维码
     * @author JackSon
     * @param id_U
     * @param id_P
     * @param id_C
     * @ver 1.0
     * @updated 2021/5/7 11:38
     * @return com.cresign.tools.apires.ApiResponse
     */
//    ApiResponse resetProdCode(String id_U, String id_P, String id_C);

    /**
     * 用戶二維碼生成
     * @author Jevon
     * @param id_C
     * @param id_U
     * @param mode
     * @param data
     * @ver 1.0
     * @createDate: 2021/6/7 15:50
     * @return: com.cresign.tools.apires.ApiResponse
     */
//    ApiResponse generateUserCode(String id_C,String id_U, String mode, JSONObject data);

    /**
     * 公司二維碼生成
     * @author Jevon
     * @param id_C
     * @param id_U
     * @param mode
     * @param data
     * @ver 1.0
     * @createDate: 2021/6/7 15:50
     * @return: com.cresign.tools.apires.ApiResponse
     */
//    ApiResponse generateCompCode(String id_C,String id_U, String mode, JSONObject data);

    /**
     * 订单二維碼生成
     * @author Jevon
     * @param id_C
     * @param id_U
     * @param mode
     * @param data
     * @ver 1.0
     * @createDate: 2021/6/7 15:50
     * @return: com.cresign.tools.apires.ApiResponse
     */
//    ApiResponse generateOrderCode(String id_C,String id_U, String id_O,String listType,String mode, JSONObject data);
    /**
     * 所有列表二维码扫取
     * @author Jevon
     * @param token
     * @param id_U
     * @param listType
     * @ver 1.0
     * @updated 2021/6/7 11:36
     * @return com.cresign.tools.apires.ApiResponse
     */
    ApiResponse scanCode(String token, String listType ,String id_U);

    /**
     * 用户重置二维码
     * @author Jevon
     * @param id_U
     * @param id_C
     * @ver 1.0
     * @createDate: 2021/6/9 9:42
     * @return: com.cresign.tools.apires.ApiResponse
     */
//    ApiResponse resetUserCode(String id_U,  String id_C);

    /**
     * 公司重置二维码
     * @author Jevon
     * @param id_U
     * @param id_C
     * @ver 1.0
     * @createDate: 2021/6/9 9:42
     * @return: com.cresign.tools.apires.ApiResponse
     */
//    ApiResponse resetCompCode(String id_U,  String id_C);

    /**
     * 订单重置二维码
     * @author Jevon
     * @param id_U
     * @param id_C
     * @ver 1.0
     * @createDate: 2021/6/9 9:42
     * @return: com.cresign.tools.apires.ApiResponse
     */
//    ApiResponse resetOrderCode(String id_U, String id_O, String id_C);

    /**
     *
     * @author JackSon
     * @param id_U
     * @param id_C
     * @param mode      模式: frequency, time
     * @param data      模式的数据:
     *                  mode为 frequency 的话 data里面则是
     *                  {
     *                      "count" : 1     // 次数
     *                  }
     *                  mode 为 time 的话 data里面则是
     *                  {
     *                      "endTimeMin" : 5, // 二维码无效时间单位是分钟,(redis的key时间)
     *                  }
     * @ver 1.0
     * @updated 2021/5/5 15:56
     * @return com.cresign.tools.apires.ApiResponse
     */
    ApiResponse generateJoinCompCode(String id_U, String id_C, String grpU, String mode, JSONObject data);


    /**
     * 扫取加入公司二维码
     * @author JackSon
     * @param token
     * @param join_user
     * @ver 1.0
     * @updated 2021/5/7 12:00
     * @return com.cresign.tools.apires.ApiResponse
     */
    ApiResponse scanJoinCompCode(String token, String join_user);


    /**
     * 重置加入公司二维码
     * @author JackSon
     * @param id_U
     * @param id_C
     * @ver 1.0
     * @updated 2021/5/7 12:00
     * @return com.cresign.tools.apires.ApiResponse
     */
    ApiResponse resetJoinCompCode(String id_U, String id_C, String grpU);


}
