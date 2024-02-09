package com.cresign.purchase.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.pojo.po.LogFlow;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName ZjTestService
 * @Date 2023/8/10
 * @ver 1.0.0
 */
public interface ZjTestService {
    ApiResponse getMdSetEs(String key,String esIndex,String condition,String val);
    ApiResponse sendLog(LogFlow logFlow);
    ApiResponse sendLogSp(String id_U, String id_C,String id,String logType
            ,String subType,String zcnDesc, JSONObject data);
    ApiResponse sendLogXj(String id_U, String id_C,String id,String logType
            ,String subType,String zcnDesc, JSONObject data);
    ApiResponse getLog(String id,String logType,String subType,String id_SP);
    ApiResponse getLogSp(String id,String id_SP);
    ApiResponse getLogXj(String id,String id_SP);
    ApiResponse shareSave(JSONObject data);
    ApiResponse shareOpen(String shareId);
    ApiResponse initFC(String id_C,String id_U);
    ApiResponse getFCAuth(String id_C,String id);
    ApiResponse setFCAuth(String id_C,String id,JSONObject users);
    ApiResponse getFCAuthByUser(String id_C,String id_U);
    ApiResponse getLSProdShareId(String id_P);
    ApiResponse getLSInfoShareId(String id_I);
    ApiResponse getLNUserShareId(String id_U);
    ApiResponse getLNCompShareId(String id_C);
    ApiResponse getLBProdShareId(String id_P);
    ApiResponse getLBInfoShareId(String id_I);
    ApiResponse saveProdEncryption(JSONObject en);
    ApiResponse getProdEncryption(String id_P);

    ApiResponse getShareId(String shareId,String type);
    ApiResponse applyForView(String id_U, String id_C, String id, String logType
            , String subType, String zcnDesc, JSONObject data,int imp);
    ApiResponse applyForAgreeWith(String id_U, String id_C, String id, String logType
            , String subType, String zcnDesc, JSONObject data,int imp);

    ApiResponse addBlankCompNew(JSONObject tokData, JSONObject wrdN, JSONObject wrddesc,
                                String pic, String ref);

    ApiResponse removeUser(String id_U);

    ApiResponse genChkinCode(String id_C);

    ApiResponse scanChkinCode(String id_U,String token);

    ApiResponse getOnLine(String id_U);

    ApiResponse delLBUser(String id_U,String id_C);

    ApiResponse testEx(String id_C, String fileName, String id_U
            , int subTypeStatus, String year, String month, JSONArray arrField);

    /**
     * 添加用户个人空间
     * @param id_U  用户编号
     * @param id_C  公司编号
     * @param wrdN  公司名称
     * @param wrddesc   公司描述
     * @param pic   公司头像
     * @param ref   公司ref
     * @return  添加结果
     */
    ApiResponse addCompSpace(String id_U,String id_C, JSONObject wrdN, JSONObject wrddesc, String pic, String ref);

    /**
     * 添加劳动合同api
     * @param id_U  用户编号
     * @param id_CB 劳动合同合作公司
     * @param money 钱数量
     * @param year  年份
     * @param contJ 合同甲方详细信息
     * @param contY 合同乙方详细信息
     * @param grpB  组别
     * @param dep   部门
     * @return  添加结果
     */
    ApiResponse addWorkContract(String id_U,String id_CB,int money,int year
            ,JSONObject contJ,JSONObject contY,String grpB,String dep);

    /**
     * 根据 subTypeStatus 统计打卡数据
     * @param id_C  公司编号
     * @param id_U  用户编号
     * @param subTypeStatus 统计类型（ == 1 统计月打卡信息、 == 0 统计天打卡信息）
     * @param year  当前年份
     * @param monthArr  统计月数组
     * @return  统计结果
     */
    ApiResponse sumTimeChkIn(String id_C,String id_U,int subTypeStatus,int year,JSONArray monthArr);

    /**
     * 查询指定的es库的keyVal条件的所有内容，并且返回size条数
     * @param index     指定的es库
     * @param keyVal    查询条件
     * @param size  返回条数
     * @return  查询结果
     */
    ApiResponse getEsShow(String index,JSONObject keyVal,int size);

    /**
     * 删除指定es库的id_ES的内容
     * @param index 指定的es库
     * @param id_ES es编号
     * @return  删除结果
     */
    ApiResponse delEs(String index,String id_ES);

    /**
     * 添加订单记录信息api
     * @param id_O  订单编号
     * @param wrdN  记录名称
     * @param ref   记录编号
     * @param allow 记录次数
     * @param pr    记录单次钱
     * @param wn4pr 记录合计钱
     * @return  添加结果
     */
    ApiResponse addOItemAllow(String id_O,String wrdN,String ref,double allow,double pr,double wn4pr);

    /**
     * 统计订单记录信息
     * @param id_O  统计的订单编号
     * @return  统计结果
     */
    ApiResponse sumOItemAllow(String id_O);

    /**
     * 根据参数indexArr下标集合修改订单的oItem为参数keyVal对应信息
     * @param id_O  订单编号
     * @param isCover   是否覆盖旧数据
     * @param indexArr  修改的下标位置集合
     * @param keyVal    修改的键值对
     * @return  修改结果
     */
    ApiResponse setOItemExtraKey(String id_O,boolean isCover, JSONArray indexArr, JSONObject keyVal);

    /**
     * 下线指定端
     * @param id_U  下线用户
     * @param client    下线端
     * @return  下线结果
     */
    ApiResponse activeOffline(String id_U,String client);

    /**
     * app端同意登录后，设置能登录接口
     * @param id_U  请求用户
     * @param client    需要登录端
     * @return  请求结果
     */
    ApiResponse allowLogin(String id_U,String client);

    /**
     * app端同意登录后，设置能登录接口
     * @param id_U  请求用户
     * @param clientOld 请求的端
     * @return  请求结果
     */
    ApiResponse requestLogin(String id_U, String clientOld);

    /**
     * 修改指定产品的价格，单人单件用时，准备时间，并且修改所有用到的part
     * @param id_P  需要修改的产品
     * @param wn4pr 产品新的价格
     * @param teDur 产品单人单件用时
     * @param tePrep    准备时间
     * @return  处理结果
     */
    ApiResponse updatePartAll(String id_P,double wn4pr,long teDur,long tePrep);

    /**
     * 批量新增或修改mongodb的Prod内arrP，和es的lBProd的arrP字段
     * @return 处理结果
     */
    ApiResponse updateAllObjItemByArrP();

    /**
     * 批量新增或修改mongodb的Prod内part的objItem内的时间，准备时间，价格的默认值
     * @return  请求结果
     */
    ApiResponse updateAllObjItemByTime();

    /**
     * 根据参数indexArr下标集合修改产品的part为参数keyVal对应信息
     * @param id_P  产品编号
     * @param isCover   是否覆盖旧数据
     * @param indexArr  修改的下标位置集合
     * @param keyVal    修改的键值对
     * @return  修改结果
     */
    ApiResponse setPartExtraKey(String id_P,boolean isCover, JSONArray indexArr,JSONObject keyVal);

    ApiResponse updatePartTime(String id_P);

    /**
     * 添加测试Asset的lSAsset信息
     * @return  添加结果
     */
    ApiResponse addAsset();
}
