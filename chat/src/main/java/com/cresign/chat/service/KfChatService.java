package com.cresign.chat.service;

import com.cresign.tools.apires.ApiResponse;

/**
 * @ClassName KfChatService
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2021/6/17 14:34
 * @Version 1.0.0
 */
public interface KfChatService {

    /**
     * 根据公司id获取该公司的客服聊天室id
     * ##param id_C	公司编号
     * ##param id_U	用户编号
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 14:58
     */
    ApiResponse getCompByCompIdAndWsIdAndKf(String id_C, String id_U);

    /**
     * 恢复客服信息,Es数据库
     * ##param oId	订单编号
     * ##param type	类型
     * ##param id_U	用户编号
     * ##param kf	客服信息
     * ##param indexOnly	唯一标识
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 14:59
     */
    ApiResponse getRecoveryKf(String oId,String type,String id_U,String kf,Integer indexOnly);

    /**
     * 恢复客服信息,并且携带用户基础信息,Es数据库
     * ##param oId	订单编号
     * ##param type	类型
     * ##param indexOnly	唯一标识
     * ##param id_U	用户编号
     * ##param kf	客服信息
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 15:01
     */
    ApiResponse getRecoveryKfAndUserInfo(String oId,String type,Integer indexOnly,String id_U,String kf);


    /**
     * 用户评分
     * ##param id_U	用户编号
     * ##param id_C	公司编号
     * ##param id_O	订单编号
     * ##param uuId	唯一id
     * ##param score	分数
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 15:04
     */
    ApiResponse getScoreUser(String id_U,String id_C,String id_O,String uuId,Integer score);



}
