package com.cresign.purchase.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

public interface ActionService {

    /**
     * 通用日志方法(action,prob,msg) - 注释完成
     *
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2021/6/16 14:35
     */
    JSONObject changeActionStatus(String logType, Integer status, String msg, Integer index, String id_O, Boolean isLink,
                                  String id_FC, String id_FS, JSONObject tokData);

//    ApiResponse changeActionStatusNew(String logType, Integer status, String msg, Integer index, String id_O, Boolean isLink,
//                                   String id_FC, String id_FS, JSONObject tokData,JSONArray id_Us) throws IOException;

    /**
     * 根据oId修改grpBGroup字段
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2021/9/10 17:32
     */
    ApiResponse changeDepAndFlow(String id_O,String grpB, JSONObject grpBOld,JSONObject grpBNew,String id_C,String id_U, String grpU, JSONObject wrdNU);

    ApiResponse changeDepAndFlowSL(String id_O,String grpB, JSONObject grpBOld,JSONObject grpBNew,String id_C,String id_U, String grpU, JSONObject wrdNU);

    ApiResponse taskToProd(JSONObject tokData, String id_O, Integer index, String id_P);

    ApiResponse getRefOPList(String id_Flow, Boolean isSL, String id_C);

    ApiResponse up_FC_action_grpB(String id_C, String id_O, String dep, String depMain, String logType,
                                  String id_Flow, JSONObject wrdFC, JSONArray grpB, JSONArray wrdGrpB);

    /**
     * 递归验证 - 注释完成
     * @param id_O 订单编号
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @ver 1.0.0
     * @date 2020/8/6 9:03
     */
    ApiResponse dgActivate(String id_O, Integer index, String id_C, String id_U, String grpU, String dep,JSONObject wrdNU);

    ApiResponse dgActivateAll(String id_O, JSONObject tokData);
    ApiResponse dgActivateStorage(JSONObject tokData, String id_O);
    String dgActivateStoSingle(JSONObject tokData, String id_O, Integer index);

    ApiResponse dgActivateSingle(String id_O, Integer index, String myCompId, String id_U, String grpU, String dep,JSONObject wrdNU);


    String createTask(JSONObject tokData, String logType, String id_FC, String id_O,JSONObject oItemData);

    ApiResponse createTaskNew(String logType, String id, String id_FS, String id_O, String myCompId, String id_U, String grpU,
                           String dep, JSONObject oItemData, JSONObject wrdNU);

    String createQuest(JSONObject tokData, String id_O, Integer index, String id_Prob, String id_FC, String id_FQ, JSONObject probData);

    ApiResponse getFlowList(String id_C, String grpB);

    ApiResponse subStatusChange(String id_O, Integer index, Boolean isLink, Integer statusType,  JSONObject tokData);

    ApiResponse dgConfirmOrder(JSONObject tokData, JSONArray casList);

    Integer confirmOrder(JSONObject tokData, String id_O);
    ApiResponse cancelOrder(String id_C, String id_O, Integer lST);

    ApiResponse actionChart(String id_O);

    ApiResponse rePush(String id_O, Integer index, JSONObject tokData);

    /**
     * 客服回访顾客api
     * @param id_O	订单编号
     * @param index 订单任务对应下标
     * @param id    内部群编号
     * @param id_Fs 对外群编号
     * @param tokData   当前登录信息
     * @param type  回访类型
     * @param dataInfo  回访类型对应的数据体
     * @param id_Us 指定发送用户列表
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    ApiResponse foCount(String id_O, Integer index,String id,String id_Fs
            , JSONObject tokData,int type,String dataInfo,JSONArray id_Us);

    /**
     * 客服向顾客申请评分api
     * @param id_O	订单编号
     * @param index	订单任务对应下标
     * @param id	内部群编号
     * @param id_Fs	对外群编号
     * @param tokData	当前登录信息
     * @param id_Us	指定发送用户列表
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    ApiResponse applyForScore(String id_O, Integer index,String id,String id_Fs, JSONObject tokData,JSONArray id_Us);

    /**
     * 顾客评分api
     * @param id_O	订单编号
     * @param index	订单任务对应下标
     * @param score	评分分数
     * @param id	内部群编号
     * @param id_Fs	对外群编号
     * @param tokData	当前登录信息
     * @param id_Us	指定发送用户列表
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    ApiResponse haveScore(String id_O, Integer index,Integer score,String id,String id_Fs, JSONObject tokData,JSONArray id_Us);

    /**
     * 操作群的默认回复api
     * @param id_C	公司编号
     * @param logId	群编号
     * @param defReply	默认回复信息
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    ApiResponse updateDefReply(String id_C,String logId,JSONArray defReply);

    /**
     * 发送日志api
     * @param logType	日志类型
     * @param dataInfo	消息类型对应的数据体
     * @param index	订单任务对应下标
     * @param id_O	订单编号
     * @param id	内部群编号
     * @param id_FS	对外群编号
     * @param tokData	当前登录信息
     * @param type	消息类型
     * @param id_Us	指定发送用户列表
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    ApiResponse sendMsgByOnly(String logType, String dataInfo,
                              Integer index, String id_O,
                              String id, String id_FS, JSONObject tokData,int type,JSONArray id_Us);
}
