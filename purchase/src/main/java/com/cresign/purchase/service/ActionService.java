package com.cresign.purchase.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

public interface ActionService {

    ApiResponse delPi(String rname,String id_C);

    ApiResponse rpiCode(String rname,String id_C);

    ApiResponse relieveRpi(String token,String id_C,String id_U);

    ApiResponse requestRpiStatus(String token,String id_C,String id_U);

    ApiResponse bindingRpi(String token,String id_C,String id_U,String grpU,Integer oIndex
            ,JSONObject wrdNU,Integer imp,String id_O,Integer tzone,String lang,String id_P
            ,String pic,Integer wn2qtynow,String grpB,JSONObject fields,JSONObject wrdNP
            ,JSONObject wrdN,String dep);
    /**
     * 通用日志方法(action,prob,msg) - 注释完成
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 14:35
     */
    ApiResponse changeActionStatus(String logType, Integer status, String msg, Integer index, String id_O, Boolean isLink,
            String id_FC, String id_FS, JSONObject tokData) throws IOException;

    /**
     * 根据oId修改grpBGroup字段
     * ##param oId	   订单编号
     * ##param grpBGroup	旧grpB分组信息
     * ##param grpBGroupX 新的grpB分组信息
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/9/10 17:32
     */
    ApiResponse changeDepAndFlow(String id_O,String grpB, JSONObject grpBOld,JSONObject grpBNew,String id_C,String id_U, String grpU, JSONObject wrdNU);

    ApiResponse changeDepAndFlowSL(String id_O,String grpB, JSONObject grpBOld,JSONObject grpBNew,String id_C,String id_U, String grpU, JSONObject wrdNU);


    ApiResponse getRefOPList(String id_Flow, Boolean isSL, String id_C) throws IOException;

    ApiResponse up_FC_action_grpB(String id_C, String id_O, String dep, String depMain, String logType,
                                  String id_Flow, JSONObject wrdFC, JSONArray grpB, JSONArray wrdGrpB);

    /**
     * 递归验证 - 注释完成
     * ##param id_O 订单编号
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @version 1.0.0
     * @date 2020/8/6 9:03
     */
    ApiResponse dgActivate(String id_O, Integer index, String id_C, String id_U, String grpU, String dep,JSONObject wrdNU);

    ApiResponse dgActivateAll(String id_O, String myCompId, String id_U, String grpU, String dep,JSONObject wrdNU);

    ApiResponse createTask(String logType, String id_FC, String id_O, String myCompId, String id_U, String grpU,
                           String dep, JSONObject oItemData, JSONObject wrdNU);

    ApiResponse createQuest(String myCompId, String id_O, Integer index, String id_Prob, String id_FC, String id_FQ,
                            String id_U, String grpU, String dep, JSONObject wrdNU, JSONObject probData);

    ApiResponse getFlowList(String id_C, String grpB);

    ApiResponse subStatusChange(String id_O, Integer index, Boolean isLink, Integer statusType,  JSONObject tokData) throws IOException;

    ApiResponse dgConfirmOrder(String id_C, JSONArray casList) throws IOException;

    ApiResponse confirmOrder(String cId, String id_O) throws IOException;
    ApiResponse cancelOrder(String cId, String id_O);

    ApiResponse actionChart(String id_O);


}
