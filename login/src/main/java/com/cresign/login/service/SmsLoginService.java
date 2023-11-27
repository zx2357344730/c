package com.cresign.login.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

import java.io.IOException;

/**
 * 短信登录实现接口
 * ##description: 短信登录
 * @author JackSon
 * @updated 2020/7/31 23:01
 * @ver 1.0
 */
public interface SmsLoginService {

    /**
     * 获取短信登录验证码
     * @param phone         手机号
     * @author JackSon
     * @updated 2020/8/1 10:28
     * @return java.lang.String
     */
    ApiResponse getSmsLoginNum(String phone) throws TencentCloudSDKException;


    /**
     * 短信验证登录
     * @param phone 手机号
     * @param smsNum 短信号
     * @param clientType 客户端
     * @author JackSon
     * @updated 2020/8/1 10:29
     * @return java.lang.String
     */
    ApiResponse smsLogin(String phone, String smsNum, String clientType);

    /**
     * 获取短信注册验证码
     * @param phone         手机号
     * @author JackSon
     * @updated 2020/8/1 10:28
     * @return java.lang.String
     */
    ApiResponse getSmsRegisterNum(String phone);

    /**
     * 通过短信验证码进行注册用户
     * @param phone      +86手机号码
     * @param phoneType  +86
     * @param smsNum     验证码
     * @param clientType  app/wx/web  那个服务端注册（默认是app端）
     * @param pic
     * @return
     */
    ApiResponse smsRegister(String phone, Integer phoneType, String smsNum, String clientType, String id_APP, String pic, JSONObject wrdN) throws IOException;


    ApiResponse regOffUser(String id_U);

    ApiResponse regBackUser(String mbn);

    ApiResponse setTestUser(String name,String type);

    ApiResponse delTestUser(String name);

    ApiResponse delUserAll(String id_U);

}
