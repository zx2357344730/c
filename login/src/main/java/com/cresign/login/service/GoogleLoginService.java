package com.cresign.login.service;

/**
 * ##description:    谷歌登录
 * ##author: JackSon
 * ##updated: 2020/9/21 16:30
 * ##version: 1.0
 */
public interface GoogleLoginService {

    /**
     * 谷歌登录
     * ##author: JackSon
     * ##Params: id_Token      获取用户在谷歌得到的id_Token
     * ##Params: clientType    客户端类型
     * ##version: 1.0
     * ##updated: 2020/9/21 16:35
     * ##Return: java.lang.String
     */
    String googleLogin(String id_Token, String clientType);

}
