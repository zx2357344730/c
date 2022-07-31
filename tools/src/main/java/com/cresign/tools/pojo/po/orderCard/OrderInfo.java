package com.cresign.tools.pojo.po.orderCard;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ##ClassName: ProdOItem
 * ##description: prod的oitem类
 * ##Author: tang
 * ##Updated: 2020/10/17 16:49
 * ##version: 1.0.0
 */
@Document(collection = "Order")
@Data
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderInfo {

    public OrderInfo(String id_C, String id_CB, String id_CP, String id_CBP, String id_OP,
                     String ref, String refB, String grp, String grpB, Double priority,
                     String pic, Integer lST, Integer lCR, JSONObject wrdN, JSONObject wrddesc) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_OP = id_OP;
        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
        this.id_CBP = id_CBP == null || id_CBP == "" ? id_CB: id_CBP;
        this.id_C = id_C;
        this.id_CB = id_CB;
        this.id_CBP = id_CBP == null || id_CBP == "" ? id_CB: id_CBP;
        this.wrdN = wrdN  == null? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc  == null? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.grp = grp == null? "1000": grp;
        this.grpB = grpB == null? "1000": grpB;
        this.ref = ref == null? "": ref;
        this.refB = refB == null? "": refB;
        this.pic = pic  == null? "": pic;
        this.lST = lST  == null? 4: lST;
        this.lCR = lCR  == null? 0: lCR;
        this.tmd = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
        this.tmk = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
    }


    private static class Hod{
        private static final OrderInfo instance = new OrderInfo();
    }

    public static OrderInfo getInstance(){
        return OrderInfo.Hod.instance;
    }

//    private String id;

    /**
     * id
     */
    private String id_C;
    private String id_CP;
    private String id_CB;
    private String id_CBP;

    private String id_OP;

    /**
     * 订单编号
     */
    private String ref;
    private String refB;

    /**
     * 名称
     */
    private JSONObject wrdN;

    /**
     * 图片
     */
    private String pic;
    /**
     * 状态
     */
    @JsonProperty("lST")
    private Integer lST;

    /**
     * 零件的货币类型，例：人民币，美元
     */
    @JsonProperty("lCR")
    private Integer lCR;

    /**
     * 订单组别
     */
    private String grpB;
    private String grp;

    private JSONObject wrddesc;

    private Double priority;

    private String tmd;

    private String tmk;
}
