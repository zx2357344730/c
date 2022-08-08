package com.cresign.details.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;
import java.util.Map;

public interface OtherService {

    /**
     * 供应商支付
     * ##Author:: Jevon
     * ##Params: id_C      公司id
     * ##Params: id_U       用户id
     * ##Params: listType  列表类型
     * ##Params: id_O       订单id
     * ##Params: id_A      公司资金账户id
     * ##Params: wn2mnyPaid 支付多少钱
     * ##version:: 1.0
     * ##updated: 2020/10/31 10:56
     * ##Return: java.lang.String
     */
    ApiResponse paymentOrder (String id_C, String id_U, String grp , String listType, String id_O, String id_A, Double wn2mnyPaid) throws IOException;
    /**
     * 卖家收款
     * ##Author:: Jevon
     * ##Params: id_C          公司id
     * ##Params: id_U          用户id
     * ##Params: listType      列表类型
     * ##Params: id_O          订单id
     * ##Params: id_A          公司资金账户id
     * ##Params: wn2mnyReceive 收款多少钱
     * ##version:: 1.0
     * ##updated: 2020/10/31 10:57
     * ##Return: java.lang.String
     */
    ApiResponse collectionOrder (String id_C,String id_U,String grp ,String listType,String id_O,String id_A,Double wn2mnyReceive)throws IOException;



    /**
     * 临时用来发 ES
     * ##author: Jevon
     * ##Params: id_C  公司id
     * ##Params: id_U  用户id
     * ##Params: logType   日志类型
     * ##Params: data      日志数据
     * ##version: 1.0
     * ##updated: 2021/4/21 11:13
     * ##Return: java.lang.String
     */
    ApiResponse setLog (String id_C, String id_U, String logType, JSONObject data) throws IOException;

    /**
     * 赠送公司
     * @author: Jevon
     * ##Params: id_U      赠送人
     * ##Params: uid       受赠人
     * ##Params: id_C      公司id
     * ##Params: ref       公司编号
     * @version: 1.0
     * @createDate: 2021/3/1 15:21
     * ##Return: java.lang.String
     */
    ApiResponse rootToPrntC(String id_U,String uid,  String id_C,String ref);


    ApiResponse scriptEngine(String script, Map<String,Object> map);


}
