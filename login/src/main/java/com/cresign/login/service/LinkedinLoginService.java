package com.cresign.login.service;

import com.cresign.tools.apires.ApiResponse;

/**
 * ##description: 领英登录接口类
 * ##author: JackSon
 * ##updated: 2020/7/29 11:11
 * ##version: 1.0
 */
public interface LinkedinLoginService {

    /**
     * 领英登录接口(web端)
     * ##author: JackSon
     * ##Params: code          用户的code
     * ##Params: clientType    客户端类型
     * ##version: 1.0
     * ##updated: 2020/9/24 11:33
     * ##Return: java.lang.String
     */
    ApiResponse linkedinWebLogin(String code, String clientType) ;

    ApiResponse registerLinked(String code, String phone, String phoneType, String smsNum);

}
