package com.cresign.tools.pojo.po.chkin;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @ClassName Task
 * @Description 作者很懒什么也没写
 * @authortang
 * @Date 2021/9/23 10:54
 * @ver 1.0.0
 */
@Data
public class Task {

    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 订单编号
     */
    private String id_O;
    /**
     * 零件位置，配合订单编号使用
     */
    private Integer index;
    /**
     * 预计开始时间
     */
    private Long tePStart;
    /**
     * 预计完成时间
     */
    private Long tePFinish;
    /**
     * 等待时间
     */
    private Long tePWait;
    /**
     * 准备时间
     */
    private Long prep;
//    /**
//     * 当天时间
//     */
//    private Long dang;
    /**
     * 用户名称
     */
    private JSONObject wrdN;
    /**
     * 总共时间
     */
    private Long teDurTotal;
    /**
     * 延迟总时间
     */
    private Long teDelayDate = 0L;
    /**
     * 存储公司编号
     */
    private String id_C;
    /**
     * 预计操作处理的开始时间
     */
    private Long teCsStart;
    /**
     * 预计操作处理的第一个子零件的开始时间
     */
    private Long teCsSonOneStart;

}
