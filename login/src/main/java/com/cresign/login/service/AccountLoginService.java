package com.cresign.login.service;

import com.cresign.tools.apires.ApiResponse;

/**
 * 
 * @author JackSon
 * @ver 1.0
 * ##Updated: 2020/7/25 9:33
 */
public interface AccountLoginService {

//    ApiResponse getKey(String qdKey);
//
//    String getHdAndQdKey(String qdKey);
    /**
     * 账户密码登录API

     * @param clientType 客户端类型
     * @author JackSon
     * @updated 2020/7/25 10:10
     * @return java.lang.String
     */
    ApiResponse unregLogin(String clientType);

    //1.前端给 WebSocket_id  后端生成二维码  带着唯一id  放redis 并  生成一个Token返回给前端
    //2.前端扫码带着Token来验证（或者是进来我验证它的WebSocket_id？）
    //3. 成功就拿到id_U，去查询他最近登录的公司，返回登录数据

    ApiResponse generateLoginCode(String id);

    ApiResponse scanLoginCode(String id, String id_U);

}
