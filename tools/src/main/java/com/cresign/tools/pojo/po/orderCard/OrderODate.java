package com.cresign.tools.pojo.po.orderCard;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @ClassName OrderODate
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2021/9/16 11:15
 * @Version 1.0.0
 */
@Document(collection = "Order")
@Data
public class OrderODate {

    /**
     * 部门
     */
    private String dep;

    /**
     * 职位
     */
    private String grpU;

    /**
     * 职位人数
     */
    private Integer grpUNum;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 预计一人一件用时
     */
    private Long teDur;

    /**
     * 实际一人一件用时
     */
    private Long taDur;

    /**
     * 预计总用时
     */
    private Long teDurTotal;

    /**
     * 实际总用时
     */
    private Long taDurTotal;

    /**
     * 预计开始时间
     */
    private Long teStart;

    /**
     * 实际开始时间
     */
    private Long taStart;

    /**
     * 预计完成时间
     */
    private Long teFin;

    /**
     * 实际完成时间
     */
    private Long taFin;

    /**
     * 预计等待总时间
     */
    private Long teWait;

    /**
     * 实际等待总时间
     */
    private Long taWait;

    /**
     * 实际暂停总时间
     */
    private Long taPause;

    /**
     * 准备时间
     */
    private Long tePrep;

    /**
     * 实际准备时间
     */
    private Long taPrep;

    /**
     * 对应的订单编号
     */
    private String id_O;

    /**
     * 对应的零件下标
     */
    private Integer index;

    /**
     * 延迟总时间
     */
    private Long teDelayDate = 0L;

}
