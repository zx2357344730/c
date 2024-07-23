package com.cresign.tools.pojo.po.orderCard;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.uuid.UUID19;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ##ClassName: ProdOItem
 * ##description: prod的oitem类
 * @author tang
 * ##Updated: 2020/10/17 16:49
 * @ver 1.0.0
 */
@Document(collection = "Order")
@Data
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderInfo implements Cloneable{

    public OrderInfo(String id_C, String id_CB, String id_CP, String id_CBP, String id_OP,
                     String ref, String refB, String grp, String grpB, Integer priority,
                     String pic, Integer lST, Integer lCR, JSONObject wrdN, JSONObject wrddesc) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_OP = id_OP;
        this.id_CP = id_CP == null || id_CP.equals("") ? id_C: id_CP;
        this.id_CBP = id_CBP == null || id_CBP.equals("") ? id_CB: id_CBP;
        this.id_C = id_C;
        this.id_CB = id_CB;
        this.id_CBP = id_CBP == null || id_CBP.equals("") ? id_CB: id_CBP;
        this.wrdN = wrdN  == null? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc  == null? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.grp = grp == null? "1000": grp;
        this.grpB = grpB == null? "1000": grpB;
        this.ref = ref == null? "": ref;
        this.refB = refB == null? "": refB;
        this.priority = priority == null ? 5: priority;
        this.pic = pic  == null? "": pic;
        this.lST = lST  == null? 4: lST;
        this.lCR = lCR  == null? 0: lCR;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.qr = UUID19.uuid();

    }


    private static class Hod{
        private static final OrderInfo instance = new OrderInfo();
    }

    public static OrderInfo getInstance(){
        return OrderInfo.Hod.instance;
    }

    @Override
    public OrderInfo clone() throws CloneNotSupportedException {
        return (OrderInfo) super.clone();
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

    private String id_UC;
    private String id_UCB;

    /**
     * 订单编号
     */
    private String ref;
    private String refB;

    private String refDC;

    private String qr;

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

    private Integer priority;

    private String tmd;

    private String tmk;

    private JSONObject wrdTag;
}
