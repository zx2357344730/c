package com.cresign.login.service;

/**
 * ##description:    谷歌登录
 * @author JackSon
 * @updated 2020/9/21 16:30
 * @ver 1.0
 */
public interface GoogleLoginService {

    /**
     * 谷歌登录
     * @author JackSon
     * @param id_Token      获取用户在谷歌得到的id_Token
     * @param clientType    客户端类型
     * @ver 1.0
     * @updated 2020/9/21 16:35
     * @return java.lang.String
     */
    String googleLogin(String id_Token, String clientType);

}
