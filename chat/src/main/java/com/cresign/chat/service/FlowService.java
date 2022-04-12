package com.cresign.chat.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

public interface FlowService {

    /**
     * 递归方法 - 注释完成
     * ##param id_OParent	订单编号
     * ##param id_U	用户编号
     * ##param id_C	公司编号
     * ##param wrdNU 用户名称
     * ##param teStart 订单开始时间
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 14:53
     */
    ApiResponse getDgResult(String id_OParent, String id_U, String id_C, JSONObject wrdNU, Long teStart);


        /**
         * 递归验证 - 注释完成
         * ##param id_P 产品编号
         * @return java.lang.String  返回结果: 递归结果
         * @author tang
         * @version 1.0.0
         * @date 2020/8/6 9:03
         */
    ApiResponse dgCheck(String id_P, String id_C);

    /**
     * 根据请求参数，获取更新后的订单oitem
     *
     * ##param id_P 产品编号
     * ##param id_C 公司编号
     * @return java.lang.String  返回结果: 日志结果
     * @author tang
     * @version 1.0.0
     * @date 2020/8/6 9:08
     */
    ApiResponse setDgAllBmdpt(String id_P, String id_C);


    /**
     * 检查part是否为空 - 注释完成
     * ##param id_P 产品编号
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/1/19 10:05
     */
    ApiResponse getPartIsNull(String id_P);


    ApiResponse dgRemove(String oId,String cId,String uId,JSONObject wrdNU);


}
