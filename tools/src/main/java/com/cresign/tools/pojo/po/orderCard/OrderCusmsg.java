package com.cresign.tools.pojo.po.orderCard;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ##ClassName: OrderProb
 * ##description: 作者很懒什么也没写
 * @author tang
 * ##Updated: 2020/10/20 14:33
 * @ver 1.0.0
 */
@Document(collection = "Order")
@Data
public class OrderCusmsg {

    /**
     * 唯一下标
     */
    private Integer indexOnly;

    /**
     * 零件自己的状态
     */
    private Integer bcdStatus;

    /**
     * 当前订单id
     */
    private String id_O;

    /**
     * 自己的下标
     */
    private Integer index = 0;

    /**
     * 负责人id
     */
    private String id_U;

    /**
     * 提出人id
     */
    private String id_UPropose;

    /**
     * 类型
     */
    private String type;

    /**
     * 唯一编号
     */
    private String uuId;

//    /**
//     * 评分
//     */
//    private Integer score = 0;

}
