package com.cresign.login.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

/**
 * 短信登录实现接口
 * ##description: 短信登录
 * ##author: JackSon
 * ##updated: 2020/7/31 23:01
 * ##version: 1.0
 */
public interface SmsLoginService {

    /**
     * 获取短信登录验证码
     * ##Params: phone         手机号
     * ##author: JackSon
     * ##updated: 2020/8/1 10:28
     * ##Return: java.lang.String
     */
    ApiResponse getSmsLoginNum(String phone) throws TencentCloudSDKException;


    /**
     * 短信验证登录
     * ##Params: phone 手机号
     * ##Params: smsNum 短信号
     * ##Params: clientType 客户端
     * ##author: JackSon
     * ##updated: 2020/8/1 10:29
     * ##Return: java.lang.String
     */
    ApiResponse smsLogin(String phone, String smsNum, String clientType);

    /**
     * 获取短信注册验证码
     * ##Params: phone         手机号
     * ##author: JackSon
     * ##updated: 2020/8/1 10:28
     * ##Return: java.lang.String
     */
    ApiResponse getSmsRegisterNum(String phone);

    /**
     * 通过短信验证码进行注册用户
     * ##Params: phone      +86手机号码
     * ##Params: phoneType  +86
     * ##Params: smsNum     验证码
     * ##Params: clientType  app/wx/web  那个服务端注册（默认是app端）
     * ##Params: pic
     * ##return:
     */
    ApiResponse smsRegister(String phone, Integer phoneType, String smsNum, String clientType, String id_APP, String pic, JSONObject wrdN);




}
