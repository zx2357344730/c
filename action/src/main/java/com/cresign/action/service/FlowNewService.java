package com.cresign.action.service;

import com.cresign.tools.apires.ApiResponse;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName FlowNewService
 * @Date 2023/9/15
 * @ver 1.0.0
 */
public interface FlowNewService {
    /**
     * 递归方法 - 注释完成
     * @param id_OParent	订单编号
     * @param id_U	用户编号
     * @param id_C	公司编号
     * @param teStart 订单开始时间
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2021/6/16 14:53
     */
    ApiResponse getDgResult(String id_OParent, String id_U, String id_C, Long teStart,String divideOrder);
    /**
     * 递归验证 - 注释完成
     * @param myCompId 公司编号
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @ver 1.0.0
     * @date 2020/8/6 9:03
     */
    ApiResponse dgCheckOrder(String myCompId,String id_O);
}
