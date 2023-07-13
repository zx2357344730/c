//package com.cresign.action.service;
//
//import com.alibaba.fastjson.JSONArray;
//import com.cresign.tools.apires.ApiResponse;
//import com.cresign.tools.pojo.po.LogFlow;
//
///**
// * @author tang
// * @Description 作者很懒什么也没写
// * @ClassName CusService
// * @Date 2023/4/22
// * @ver 1.0.0
// */
//public interface CusService {
//
////    /**
////     * 顾客请求客服api
////     * @param id_CCus
////     * @param id_U
////     * @param id_O
////     * @return 返回结果: {@link ApiResponse}
////     * @author tang
////     * @date 创建时间: 2023/4/24
////     * @ver 版本号: 1.0.0
////     */
////    ApiResponse getCreateCus(String id_CCus,String id_U,String id_O);
//    ApiResponse renewCusUser(String id_C, JSONArray indexS, JSONArray ids, Integer type);
//    ApiResponse createCus(String id_CCus,String id_U,String id_O);
//    ApiResponse getCusListByUser(String id_U,JSONArray types);
//    ApiResponse getCusListByCusUser(String id_U,String id_O,JSONArray types);
//
//    /**
//     * 顾客发送日志api
//     * @param logFlow	日志信息
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/4/24
//     * @ver 版本号: 1.0.0
//     */
//    ApiResponse sendUserCusCustomer(LogFlow logFlow);
//
//    /**
//     * 客服发送日志api
//     * @param logFlow	日志信息
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/4/24
//     * @ver 版本号: 1.0.0
//     */
//    ApiResponse sendUserCusService(LogFlow logFlow);
//
////    /**
////     * 客服接受顾客api
////     * @param id_CCus	公司编号
////     * @param id_U	客服、负责人
////     * @param id_O	订单编号
////     * @param index	订单下标
////     * @return 返回结果: {@link ApiResponse}
////     * @author tang
////     * @date 创建时间: 2023/4/24
////     * @ver 版本号: 1.0.0
////     */
////    ApiResponse acceptCus(String id_CCus,String id_U,String id_O,int index);
//
//    /**
//     * 客服操作api
//     * @param id_CCus	公司编号
//     * @param id_U	客服、负责人编号
//     * @param id_O	订单编号
//     * @param index	订单下标
//     * @param bcdStatus	操作状态
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/4/24
//     * @ver 版本号: 1.0.0
//     */
//    ApiResponse cusOperate(String id_CCus,String id_U,String id_O,int index,int bcdStatus);
//
//    ApiResponse restoreCusLog(String id_O,String id_CCus,Integer index);
//
//    /**
//     * 根据id_C获取公司的聊天群信息
//     * @param id_C	公司编号
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/5/29
//     * @ver 版本号: 1.0.0
//     */
//    ApiResponse getLogList(String id_C);
//
//    /**
//     * 更新修改群关联信息
//     * @param id_C	公司编号
//     * @param logId	群编号
//     * @param glId	关联信息
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/5/29
//     * @ver 版本号: 1.0.0
//     */
//    ApiResponse updateLogListGl(String id_C, String logId, JSONArray glId);
//
//    /**
//     * 获取公司的日志权限信息
//     * @param id_C	公司编号
//     * @param grpUW	外层组别
//     * @param grpUN	内层组别
//     * @param type	获取类型
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/5/29
//     * @ver 版本号: 1.0.0
//     */
//    ApiResponse getLogAuth(String id_C,String grpUW,String grpUN,String type);
//}
