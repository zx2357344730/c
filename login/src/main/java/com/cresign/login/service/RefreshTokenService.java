package com.cresign.login.service;

import com.cresign.tools.apires.ApiResponse;

/**
 * ##description:    注销账号
 * @author JackSon
 * @updated 2020/9/5 14:20
 * @ver 1.0
 */
public interface RefreshTokenService {


    /**
     * 根据refreshToken来获取用户id进行登录
     * @author JackSon
     * @param refreshToken      redis中存放的refreshToken
     * @param clientType        客户端类型
     * @ver 1.0
     * @updated 2020/9/5 15:24
     * @return java.lang.String
     */
    ApiResponse refreshTokenLogin(String refreshToken, String clientType);


    /**
     * 注销用户账户
     * @author JackSon
     * @param refreshToken      redis中存放的refreshToken
     * @param clientType        客户端类型
     * @ver 1.0
     * @updated 2020/9/5 15:23
     * @return java.lang.String
     */
    ApiResponse logout(String refreshToken, String clientType);

    /**
     *##description:      获取 refreshToken 重新给前端生成 token
     *@param            refreshToken:刷新token, clientType: 客户端类型, id_U: 用户id
     *@return           返回token或者状态码信息
     *@author           JackSon
     *@updated             2020/5/15 19:00
     */
    ApiResponse refreshToken(String refreshToken, String id_C, String clientType, String id_U);

    /**
     *##description:      获取 refreshToken 重新给前端生成 token
     *@param            refreshToken:刷新token, clientType: 客户端类型, id_U: 用户id
     *@return           返回token或者状态码信息
     *@author           JackSon
     *@updated             2020/5/15 19:00
     */
    String refreshToken2(String refreshToken, String id_C, String clientType, String id_U,String token);

}