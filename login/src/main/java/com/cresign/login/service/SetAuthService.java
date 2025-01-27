package com.cresign.login.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

public interface SetAuthService {


    /**
     * 获取我能用的按钮
     * @author JackSon
     * @param id_U      用户id
     * @param id_C      公司id
     * @param listType  列表类型
     * @param grp       组别
     * @ver 1.0
     * @updated 2021-02-05 9:31
     * @return java.lang.String
     * @return
     */
    ApiResponse getMyBatchList(String id_U, String id_C, String listType, JSONArray grp);

    /**
     * 获取我能修改的卡片列表
     * @author JackSon
     * @param id_U
     * @param id_C
     * @param listType
     * @param grp
     * @ver 1.0
     * @updated 2021-02-05 13:23
     * @return java.lang.String
     */
    ApiResponse getMyUpdateCardList(String id_U, String id_C, String listType, String grp);


    /**
     * 切换公司接口
     * @author JackSon
     * @param id_U          用户id
     * @param id_C          公司id
     * @param clientType    客户端类型
     * @ver 1.0
     * @createDate: 2020/8/10 17:00
     * @return java.lang.String
     */
    ApiResponse switchComp(String id_U, String id_C,String clientType);


    /**
     * 获取自己能切换公司的列表数据
     * @author JackSon
     * @param id_U          用户id
     * @ver 1.0
     * @createDate: 2021-01-28 15:33
     * @return java.lang.String
     */
    ApiResponse getMySwitchComp(String id_U, String lang);

//    ApiResponse getMySwitchComp1(String id_U, String lang);


    /**
     * 更新def卡数据
     * @author JackSon
     * @param id_U      用户id
     * @param id_C      公司id
     * @param defData   前端传入的def数据
     * @ver 1.0
     * @updated 2021-03-18 12:08
     * @return java.lang.String
     * @return
     */
//    ApiResponse updateDefCard(String id_U, String id_C, JSONObject defData);

    /**
     * 更新id_AUN卡数据
     * @author Kevin
     * @param id_AUN      用户id
     * @ver 1.0
     * @updated 2021-11-18 12:08
     * @return java.lang.String
     * @return
     */
    ApiResponse setAUN(String id_U, String id_AUN);
}
