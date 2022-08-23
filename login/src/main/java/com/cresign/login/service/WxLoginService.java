package com.cresign.login.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

/**
 * ##description: 微信登录接口类
 * ##author: JackSon
 * ##updated: 2020/7/29 9:27
 * ##version: 1.0
 */
public interface WxLoginService {

    /**
     * 验证appId，并且返回AUN—ID
     * @param id_APP	应用编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    ApiResponse verificationAUN(String id_APP);

    /**
     * 微信登录接口 (web端)
     * ##Params: code 前端传入的code
     * ##author: JackSon
     * ##updated: 2020/7/29 9:27
     * ##Return: java.lang.String
     * ##param code
     */
    ApiResponse wxWebLogin(String code) throws IOException;


    /**
     * 微信登录解密用户信息接口(微信小程序)
     * ##Params: reqJson 前端传入的code
     * ##author: JackSon
     * ##updated: 2020/8/8 9:43
     * ##Return: java.lang.String
     */
    ApiResponse decodeUserInfo(JSONObject reqJson);



    /**
     * 通过用户的微信id来进行登录
     * ##author: JackSon
     * ##Params: id_WX         用户的应用的唯一微信id
     * ##Params: clientType    客户端类型
     * ##version: 1.0
     * ##updated: 2020/8/10 16:15
     * ##Return: java.lang.String
     */
    ApiResponse wXLoginByIdWx(String id_WX, String clientType);

    /**
     * 通过用户的微信id来进行登录
     * ##author: JackSon
     * ##Params: id_WX         用户的应用的唯一微信id
     * ##Params: clientType    客户端类型
     * ##version: 1.0
     * ##updated: 2020/8/10 16:15
     * ##Return: java.lang.String
     */
    ApiResponse appLoginByIdWx(String id_AUN, String clientType);


    /**
     * 微信小程序点击头像注册
     * ##author: JackSon
     * ##Params: reqJson
     * ##version: 1.0
     * ##updated: 2020/8/27 15:25
     * ##Return: java.lang.String
     */
    ApiResponse wxRegisterUser(JSONObject reqJson);


    /**
     * 通过短信验证码进行注册微信用户
     * ##Params: phone         手机号
     * ##Params: phoneType     手机注册类型
     * ##Params: smsNum        验证码
     * ##Params: wcnN          用户名
     * ##Params: clientType    客户端类型
     * ##Params: pic           用户头像
     * ##Params: id_WX         用户
     * ##return:    String
     */
    ApiResponse wechatRegister(String phone, Integer phoneType, String smsNum, String wcnN, String clientType, String clientID, String pic, String id_WX);


    /**
     * 微信小程序注册用户
     * ##author: JackSon
     * ##Params: nickName
     * ##Params: avatarUrl
     * ##Params: unionId
     * ##Params: countryCode
     * ##Params: phoneNumber
     * ##version: 1.0
     * ##updated: 2021-03-09 18:57
     * ##Return: java.lang.String
     */
    ApiResponse wxmpRegister(String nickName, String avatarUrl, String unionId, Integer countryCode, String phoneNumber);

}
