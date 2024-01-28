package com.cresign.action.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName TimeZjServiceNew
 * @Date 2022/11/2
 * @ver 1.0.0
 */
public interface TimeZjServiceNew {

    /**
     * 任务实际结束时间处理接口
     * @param dep	部门
     * @param grpB	组别
     * @param currentTime	处理时间
     * @param index	任务列表对应下标
     * @param id_C	公司编号
     * @param taPFinish	任务实际结束时间
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    ApiResponse timeSortFromNew(String dep,String grpB,long currentTime,int index,String id_C, long taPFinish);

    /**
     * 根据订单编号与下标获取剩余数量的预计完成时间
     * @param id_O	订单编号
     * @param index	下标
     * @param number	人数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2022/11/6
     * @ver 版本号: 1.0.0
     */
    ApiResponse timeCalculation(String id_O,int index,int number);

    /**
     * 删除或者新增aArrange卡片信息
     * @param id_C	公司编号
     * @param object	操作信息
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    ApiResponse delOrAddAArrange(String id_C,String dep,JSONObject object);

}
