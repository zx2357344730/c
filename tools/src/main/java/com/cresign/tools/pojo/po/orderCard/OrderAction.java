package com.cresign.tools.pojo.po.orderCard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ##ClassName: ProdAction
 * ##description: prod的action类
 * ##Author: tang
 * ##Updated: 2020/10/17 16:49
 * ##version: 1.0.0
 */
@Data
@Document(collection = "Order")
//生成全参数构造函数
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)

public class OrderAction {

    public OrderAction(Integer bcdStatus, Integer bisPush, Integer bisactivate, Integer bmdpt,
                       String id_OP, String refOP, String id_P, String id_O, Integer index, Integer priority,
                       Integer sumChild, Integer sumPrev,
                   JSONArray subParts, JSONArray upPrnts, JSONArray prtNext, JSONArray prtPrev,
                   JSONObject wrdNP, JSONObject wrdN) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.bcdStatus = bcdStatus;
        this.bisPush = bisPush;
        this.bisactivate = bisactivate;
        this.id_O = id_O;
        this.id_OP = id_OP;
        this.refOP = refOP == null || refOP == "" ? "": refOP;
        this.id_P = id_P;
        this.index = index;
        this.bmdpt = bmdpt;
        this.sumChild = sumChild;
        this.sumPrev = sumPrev;
        this.wrdNP = wrdNP  == null? (JSONObject) wrdEmpty.clone(): wrdNP;

        this.wrdN = wrdN  == null? (JSONObject) wrdEmpty.clone(): wrdN;
        this.priority = priority;

        this.subParts = subParts == null? new JSONArray( ): subParts;
        this.upPrnts = upPrnts == null? new JSONArray( ): upPrnts;
        this.prtNext = prtNext == null? new JSONArray( ): prtNext;
        this.prtPrev = prtPrev == null? new JSONArray( ): prtPrev;
    }


    private static class Hod{
        private static final OrderAction instance = new OrderAction();
    }

    public static OrderAction getInstance(){
        return OrderAction.Hod.instance;
    }

    /**
     * 零件自己的状态 100 0 1 2 3 7 8
     */
    private Integer bcdStatus = 100;

    /**
     * 是否推送，0：未推送，1：已推送
     */
    private Integer bisPush = 0;

    /**
     * A special Type defining
     * 0 = DG, 4 = Being Skipped ,7 = Tasks/Quest?, 5 = ?
     */
    private Integer bisactivate = 0;

    /**
     * 当前订单id
     */
    private String id_O;

    /**
     * 总订单id，最大的订单id
     */
    private String id_OP;
    private String refOP;
    private String id_P;

    /**
     * 自己的下标
     */
    private Integer index = 0;

    /**
     *   类型，1：工序、2：部件、3：物料
     */
    private Integer bmdpt;

//    /**
//     * 下一个要处理的产品id
//     */
//    private String id_ONext;
//
//    /**
//     * 下一个要处理的产品下标
//     */
//    private Integer indONext;

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

    /**
     * 子产品数量
     */
    private Integer sumChild = 0;

    /**
     * 上一个序号的产品数量
     */
    private Integer sumPrev = 0;

    /**
     * 唯一下标
     */
//    private Integer indexOnly;

    /**
     * 现在完成数量
     */
//    private Integer wn2qtynow = 0;

    /**
     * 最後完成數量
     */
//    private Integer wn2qtyfin = 0;

    /**
     * 所有父产品名称拼接存放
     */
    private JSONObject wrdNP;
    private JSONObject wrdN;

    /**
     * 提问后回复
     */
//    private JSONObject nextDesc;

    /**
     * 负责人备注
     */
//    private JSONObject desc;

    /**
     * 前端传递数量
     */
//    private Double wn2qtyneed;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 是否是子任务的bmdpt-4 的父零件
     */
//    private Integer isZp = 0;

    /**
     * 加入共同工作人信息
     */
    private JSONArray id_Us;

    /**
     * subPart = parts, upPrnts = myParent[], Next[] next process, prev Process
     */
    private JSONArray subParts = new JSONArray();;
    private JSONArray upPrnts = new JSONArray();;

    private JSONArray prtNext = new JSONArray();
    private JSONArray prtPrev = new JSONArray();

    /**
     * 任务备注
     */
//    private JSONObject wrdPrep;

    /**
     * 是否合并: 0 : 未合并，1 : 合并，2 : 被合并
     */
//    private Integer merge = 0;

    /**
     * prob
     */
    private JSONArray prob;

    /**
     * 递归时间任务存储的部门
     */
    private String dep;

    /**
     * 递归时间任务存储的部门下的组别
     */
    private String grpB;

    /**
     * 递归时间任务存储的部门下的组别下的日期
     */
    private JSONObject teDate;

}
