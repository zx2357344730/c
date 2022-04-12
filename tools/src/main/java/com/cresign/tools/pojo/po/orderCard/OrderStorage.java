//package com.cresign.tools.pojo.po.orderCard;
//
//import lombok.Data;
//import org.springframework.data.mongodb.core.mapping.Document;
//
///**
// * ##ClassName: OrderProb
// * ##description: 作者很懒什么也没写
// * ##Author: tang
// * ##Updated: 2020/10/20 14:33
// * ##version: 1.0.0
// */
//@Document(collection = "Order")
//@Data
//public class OrderStorage {
//
//    /**
//     * 零件次序
//     */
//    private Integer wn0prior;
//
//    /**
//     * 唯一下标
//     */
//    private Integer indexOnly;
//
//    /**
//     * 零件自己的状态
//     */
//    private Integer bcdStatus;
//
//    /**
//     * 是否推送，0：未推送，1：已推送
//     */
//    private Integer bisPush = 0;
//
//    /**
//     * 是否激活，0：已激活、1：未激活
//     */
//    private Integer bisactivate = 0;
//
//    /**
//     * 当前订单id
//     */
//    private String id_O;
//
//    /**
//     * 总订单id，最大的订单id
//     */
//    private String id_OP;
//
//    /**
//     * 自己的下标
//     */
//    private Integer index = 0;
//
//    /**
//     *   类型，1：工序、2：部件、3：物料
//     */
//    private Integer bmdpt;
//
//    /**
//     * 下一个要处理的产品id
//     */
//    private String id_ONext;
//
//    /**
//     * 下一个要处理的产品下标
//     */
//    private Integer indONext;
//
//    /**
//     * 父产品id
//     */
//    private String id_OUpper;
//
//    /**
//     * 父产品下标
//     */
//    private Integer indOUpper;
//
//    /**
//     * 下一个兄弟产品id
//     */
//    private String id_OWith;
//
//    /**
//     * 下一个兄弟产品下标
//     */
//    private Integer indOWith;
//
//    /**
//     * 上一个提问备注
//     */
//    private String upperDesc;
//
//    /**
//     * 中文零件名称
//     */
//    private String wcnNp;
//
//    /**
//     * 英文零件名称
//     */
//    private String wenNp;
//
//    /**
//     * 负责人id
//     */
//    private String id_U;
//
//    /**
//     * 提出人id
//     */
//    private String id_UPropose;
//
//    /**
//     * 当前备注
//     */
//    private String desc;
//
//    /**
//     * 下一个问题备注
//     */
//    private String nextDesc;
//
//    /**
//     * 多任务数量
//     */
//    private Integer sumTaskCount;
//
//    /**
//     * 优先级
//     */
//    private Integer priority;
//
//}
