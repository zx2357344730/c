package com.cresign.login.service;


import com.cresign.tools.apires.ApiResponse;

/**
 * 初始化信息Service类
 */
public interface InitService {


    /**
     * 根据id(语言)获取init的数据
     * ##author: JackSon
     * ##Params: id            id
     * ##Params: ver           版本号
     * ##version: 1.0
     * ##updated: 2020/8/8 11:07
     * ##Return: java.lang.String
     */
    ApiResponse getInitById(String lang, Integer ver,String qdKey,String uuId);


    /**
     * 获取手机号类型
     * ##author: JackSon
     * ##Params: lang      语言
     * ##version: 1.0
     * ##updated: 2020/9/21 8:52
     * ##Return: java.lang.String
     */
    ApiResponse getPhoneType(String lang);

    ApiResponse getInitInclude(String lang, Integer ver,String include);

}
