package com.cresign.tools.pojo.po.orderCard;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @ClassName OrderODate
 * @Description 作者很懒什么也没写
 * @author tang
 * @Date 2021/9/16 11:15
 * @ver 1.0.0
 */
@Document(collection = "Order")
@Data
public class OrderODate {

    /**
     * 存储零件数量
     */
    private Double wn2qtyneed;
    /**
     * 存储公司编号
     */
    private String id_C;
    /**
     * 记录，存储是递归第一层的，序号为1和序号为最后一个状态,
     * 1 : 是递归第一层的第一层，2 : 正常操作，3 : 是递归的最后一个-直系为1并且是部件，4 : 是递归不是第一层的第一层，5 : 是部件-直系不为1并且是部件
     */
    private Integer kaiJie;

    /**
     * --直系存储状态--序号是否为1层级: 记录是不是属于递归产品的第一层,0 不属于，1 属于
     */
    private Integer csSta;

    private Integer bmdpt;

    /**
     * 部门
     */
    private String dep;

    /**
     * 职位
     */
    private String grpB;


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
    private Long wntDur;

    /**
     * 实际一人一件用时
     */
    private Long taDur;

    /**
     * 预计总用时
     */
    private Long wntDurTotal;

    /**
     * 实际总用时
     */
    private Long taDurTotal;

//    /**
//     * 预计开始时间
//     */
//    private Long teStart;
//
//    /**
//     * 实际开始时间
//     */
//    private Long taStart;
//
//    /**
//     * 预计完成时间
//     */
//    private Long teFin;
//
//    /**
//     * 实际完成时间
//     */
//    private Long taFin;

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
    private Long wntPrep;

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

    /**
     * 父产品id
     */
    private String id_PF;

    /**
     * 产品id
     */
    private String id_P;

    /**
     * item里的序号
     */
    private Integer priorItem;

    /**
     * 存储判断是否是空任务
     */
    private Boolean empty = false;

    /**
     * 链接下标
     */
    private Integer linkInd;

    /**
     * 是否使用库存 == true说明用库存 = 跳过
     */
    private Boolean isSto;

    /**
     * 当前任务的所在日期时间戳
     */
    private JSONObject teDate;

}
