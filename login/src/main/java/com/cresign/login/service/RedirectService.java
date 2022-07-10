package com.cresign.login.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2021-05-03 9:52
 * ##version: 1.0
 */
public interface RedirectService {

    /**
     * 生成产品二维码
     * ##author: JackSon
     * ##Params: id_C
     * ##Params: id_P
     * ##Params: id_U
     * ##Params: mode      模式: frequency, time
     * ##Params: data      模式的数据:
     *      *                  mode为 frequency 的话 data里面则是
     *      *                  {
     *      *                      "count" : 1     // 次数
     *      *                  }
     *      *                  mode 为 time 的话 data里面则是
     *      *                  {
     *      *                      "endTimeMin" : 5, // 二维码无效时间单位是分钟,(redis的key时间)
     *      *                  }
     * ##version: 1.0
     * ##updated: 2021-05-03 9:54
     * ##Return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse generateProdCode(String id_C, String id_P, String id_U, String mode, JSONObject data);




    /**
     * 重置产品分享二维码
     * ##author: JackSon
     * ##Params: id_U
     * ##Params: id_P
     * ##Params: id_C
     * ##version: 1.0
     * ##updated: 2021/5/7 11:38
     * ##Return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse resetProdCode(String id_U, String id_P, String id_C);

    /**
     * 用戶二維碼生成
     * @author: Jevon
     * ##param id_C
     * ##param id_U
     * ##param mode
     * ##param data
     * @version: 1.0
     * @createDate: 2021/6/7 15:50
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse generateUserCode(String id_C,String id_U, String mode, JSONObject data);

    /**
     * 公司二維碼生成
     * @author: Jevon
     * ##param id_C
     * ##param id_U
     * ##param mode
     * ##param data
     * @version: 1.0
     * @createDate: 2021/6/7 15:50
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse generateCompCode(String id_C,String id_U, String mode, JSONObject data);

    /**
     * 订单二維碼生成
     * @author: Jevon
     * ##param id_C
     * ##param id_U
     * ##param mode
     * ##param data
     * @version: 1.0
     * @createDate: 2021/6/7 15:50
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse generateOrderCode(String id_C,String id_U, String id_O,String listType,String mode, JSONObject data);
    /**
     * 所有列表二维码扫取
     * ##author: Jevon
     * ##Params: token
     * ##Params: id_U
     * ##Params: listType
     * ##version: 1.0
     * ##updated: 2021/6/7 11:36
     * ##Return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse scanCode(String token, String listType ,String id_U);

    /**
     * 用户重置二维码
     * @author: Jevon
     * ##param id_U
     * ##param id_C
     * @version: 1.0
     * @createDate: 2021/6/9 9:42
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse resetUserCode(String id_U,  String id_C);

    /**
     * 公司重置二维码
     * @author: Jevon
     * ##param id_U
     * ##param id_C
     * @version: 1.0
     * @createDate: 2021/6/9 9:42
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse resetCompCode(String id_U,  String id_C);

    /**
     * 订单重置二维码
     * @author: Jevon
     * ##param id_U
     * ##param id_C
     * @version: 1.0
     * @createDate: 2021/6/9 9:42
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse resetOrderCode(String id_U, String id_O, String id_C);

    /**
     *
     * ##author: JackSon
     * ##Params: id_U
     * ##Params: id_C
     * ##Params: mode      模式: frequency, time
     * ##Params: data      模式的数据:
     *                  mode为 frequency 的话 data里面则是
     *                  {
     *                      "count" : 1     // 次数
     *                  }
     *                  mode 为 time 的话 data里面则是
     *                  {
     *                      "endTimeMin" : 5, // 二维码无效时间单位是分钟,(redis的key时间)
     *                  }
     * ##version: 1.0
     * ##updated: 2021/5/5 15:56
     * ##Return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse generateJoinCompCode(String id_U, String id_C, String mode, JSONObject data);


    /**
     * 扫取加入公司二维码
     * ##author: JackSon
     * ##Params: token
     * ##Params: join_user
     * ##version: 1.0
     * ##updated: 2021/5/7 12:00
     * ##Return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse scanJoinCompCode(String token, String join_user) throws IOException;


    /**
     * 重置加入公司二维码
     * ##author: JackSon
     * ##Params: id_U
     * ##Params: id_C
     * ##version: 1.0
     * ##updated: 2021/5/7 12:00
     * ##Return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse resetJoinCompCode(String id_U, String id_C);


}
