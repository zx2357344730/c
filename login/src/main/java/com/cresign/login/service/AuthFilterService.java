package com.cresign.login.service;

import com.cresign.tools.apires.ApiResponse;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/8/6 10:24
 * ##version: 1.0
 */
public interface AuthFilterService {


//    /**
//     * 获取用户能查询的权限
//     * ##author: JackSon
//     * ##Params: id_U          用户id
//     * ##Params: id_C          用户id
//     * ##Params: listType      列表类型
//     * ##Params: grp           组别
//     * ##Params: authType      card/batch
//     * ##version: 1.0
//     * ##updated: 2020-12-07 9:21
//     * ##Return: java.lang.String
//     */
//    ApiResponse getUserSelectAuth(String id_U, String id_C, String listType, String grp, String authType);
//
//    /**
//     * 获取用户能修改的权限
//     * ##author: JackSon
//     * ##Params: id_U          用户id
//     * ##Params: id_C          公司id
//     * ##Params: listType      列表类型
//     * ##Params: grp           组别
//     * ##Params: authType      card/batch
//     * ##Params: params        card的数据/batch的数据
//     * ##version: 1.0
//     * ##updated: 2021-02-25 11:30
//     * ##Return: java.lang.String
//     */
//    ApiResponse getUserUpdateAuth(String id_U, String id_C, String listType, String grp, String authType, JSONArray params);
//

    /**
     * 获取游客可看getSingle的权限
     * ##author: JackSon
     * ##Params: id_C          用户id
     * ##Params: listType      列表类型
     * ##Params: grp           组别
     * ##Params: authType      card/batch
     * ##version: 1.0
     * ##updated: 2020-12-07 9:21
     * ##Return: java.lang.String
     */
    ApiResponse getTouristAuth(String id_C, String listType, String grp, String authType);


}
