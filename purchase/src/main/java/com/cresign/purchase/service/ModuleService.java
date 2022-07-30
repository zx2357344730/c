package com.cresign.purchase.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

/**
 * 添加模块
 * ##description:
 * ##author: Jevon
 * ##updated: 2021-05-20 14:30
 * ##version: 1.0
 */
public interface ModuleService {

    ApiResponse testFy(JSONObject data);

    ApiResponse testFy2(JSONObject data);

    ApiResponse lSprod2lBprod(String id_P,String id_C,Boolean isMove);

    ApiResponse modSetUser(String id_C,JSONObject objUser);

    ApiResponse modSetControl(String id_C,JSONObject objModQ);

    ApiResponse modGetControl(String id_C);

    ApiResponse modAddLSBComp(String id_C,String id_CP,String id_CB,String id_CBP
            ,JSONObject wrdNC,JSONObject wrddesc,JSONObject wrdNCB,JSONObject wrddescB
            ,String grp,String grpB,String refC,String refCB,String picC,String picCB);

    /**
     * 添加模块信息
     * ##author: Jevon
     * ##Params: id_U      用户id
     * ##Params: oid       redis订单号
     * ##Params: id_C      公司id
     * ##Params: ref       模块编号
     * ##Params: bcdLevel  模块等级
     * ##version: 1.0
     * ##updated: 2021/3/5 15:24
     * ##Return: java.lang.String
     */
    ApiResponse addModule (String id_U, String oid, String id_C, String ref, Integer bcdLevel) throws IOException;

    /**
     * 新建公司并添加默认模块
     * ##Params: uid   用户id
     * ##Params: reqJson   公司基本资料
     * ##author: Jevon
     * ##version: 1.0
     * ##updated: 2020/08/05 08:32:53
     * ##Return: String
     */
    ApiResponse addBlankComp(String uid, JSONObject reqJson) throws IOException;

}
