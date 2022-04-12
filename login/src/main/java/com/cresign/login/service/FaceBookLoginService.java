package com.cresign.login.service;

import com.cresign.tools.apires.ApiResponse;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/9/22 14:05
 * ##version: 1.0
 */
public interface FaceBookLoginService {


    ApiResponse faceBookLogin(String id_fb, String clientType);


    /**
     * facebook用户注册
     * ##author: JackSon
     * ##Params: id_fb
     * ##Params: wcnN
     * ##Params: email
     * ##Params: pic
     * ##Params: phone
     * ##Params: phoneType
     * ##Params: smsNum
     * ##Params: clientID
     * ##Params: clientType
     * ##version: 1.0
     * ##updated: 2020/9/22 14:10
     * ##Return: java.lang.String
     */
    ApiResponse faceBookRegister(String id_fb, String wcnN, String email, String pic, String phone, String phoneType, String smsNum, String clientID, String clientType);

}
