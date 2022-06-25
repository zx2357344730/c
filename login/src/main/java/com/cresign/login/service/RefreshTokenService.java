package com.cresign.login.service;

import com.cresign.tools.apires.ApiResponse;

/**
 * ##description:    注销账号
 * ##author: JackSon
 * ##updated: 2020/9/5 14:20
 * ##version: 1.0
 */
public interface RefreshTokenService {


    /**
     * 根据refreshToken来获取用户id进行登录
     * ##author: JackSon
     * ##Params: refreshToken      redis中存放的refreshToken
     * ##Params: clientType        客户端类型
     * ##version: 1.0
     * ##updated: 2020/9/5 15:24
     * ##Return: java.lang.String
     */
    ApiResponse refreshTokenLogin(String refreshToken, String clientType);


    /**
     * 注销用户账户
     * ##author: JackSon
     * ##Params: refreshToken      redis中存放的refreshToken
     * ##Params: clientType        客户端类型
     * ##version: 1.0
     * ##updated: 2020/9/5 15:23
     * ##Return: java.lang.String
     */
    ApiResponse loginOut(String refreshToken, String clientType);

    /**
     *##description:      获取 refreshToken 重新给前端生成 token
     *##Params:            refreshToken:刷新token, clientType: 客户端类型, id_U: 用户id
     *##Return:           返回token或者状态码信息
     *##author:           JackSon
     *##updated:             2020/5/15 19:00
     */
    ApiResponse refreshToken(String refreshToken, String id_C, String clientType, String id_U);

}