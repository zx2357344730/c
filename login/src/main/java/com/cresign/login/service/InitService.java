package com.cresign.login.service;


import com.cresign.tools.apires.ApiResponse;

/**
 * 初始化信息Service类
 */
public interface InitService {


    /**
     * 根据id(语言)获取init的数据
     * @author JackSon
     * @param id            id
     * @param ver           版本号
     * @ver 1.0
     * @updated 2020/8/8 11:07
     * @return java.lang.String
     */
    ApiResponse getInitById(String lang, Integer ver,String qdKey,String uuId);


    /**
     * 获取手机号类型
     * @author JackSon
     * @param lang      语言
     * @ver 1.0
     * @updated 2020/9/21 8:52
     * @return java.lang.String
     */
    ApiResponse getPhoneType(String lang);

    ApiResponse isDeveloper(String id_U);

//    ApiResponse getInitInclude(String lang, Integer ver,String include);

}
