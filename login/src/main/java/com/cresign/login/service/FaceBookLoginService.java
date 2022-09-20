package com.cresign.login.service;

import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/9/22 14:05
 * @ver 1.0
 */
public interface FaceBookLoginService {


    ApiResponse faceBookLogin(String id_fb, String clientType);


    /**
     * facebook用户注册
     * @author JackSon
     * @param id_fb
     * @param wcnN
     * @param email
     * @param pic
     * @param phone
     * @param phoneType
     * @param smsNum
     * @param clientID
     * @param clientType
     * @ver 1.0
     * @updated 2020/9/22 14:10
     * @return java.lang.String
     */
    ApiResponse faceBookRegister(String id_fb, String wcnN, String email, String pic, String phone, String phoneType, String smsNum, String clientID, String clientType) throws IOException;

}
