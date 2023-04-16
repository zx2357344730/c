package com.cresign.tools.pojo.po.orderCard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
public class OrderOItem {


//    public OrderOItem(String id_P, String id_C, String id_CP, String id_CB, JSONObject wrdN,
//                      JSONObject wrddesc, JSONObject wrdprep, String grp, String grpB, String ref,
//                      String refB, String pic, Integer lCR, Integer lUT, Double wn2qtyneed,
//                      Double wn4price, Integer wn0prior) {
//
//        JSONObject wrdEmpty = new JSONObject();
//        wrdEmpty.put("cn","");
//
//        this.id_P = id_P;
//        this.id_C = id_C;
//        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
//        this.id_CB = id_CB;
//        this.rKey = UUID19.uuid8();
//        this.wrdN = wrdN  == null? (JSONObject) wrdEmpty.clone(): wrdN;
//        this.wrddesc = wrddesc  == null? (JSONObject) wrdEmpty.clone(): wrddesc;
//        this.wrdprep = wrdprep == null? (JSONObject) wrdEmpty.clone(): wrdprep;
//        this.grp = grp == null? "1000": grp;
//        this.grpB = grpB == null? "1000": grpB;
//        this.ref = ref == null? "": ref;
//        this.refB = refB == null? "": refB;
//        this.pic = pic  == null? "": pic;
//        this.lUT = lUT  == null? 0: lUT;
//        this.lCR = lCR  == null? 0: lCR;
//        this.wn2qtyneed = wn2qtyneed == null? 1: wn2qtyneed;
//        this.wn4price = wn4price == null? 0: wn4price;
//        this.wn0prior = wn0prior;
//    } // need id_O, index, wrdNP, id_OP

    public OrderOItem(String id_P, String id_OP, String id_CP, String id_C, String id_CB, String id_O, Integer index,
                       String ref, String refB, String grp, String grpB, Integer wn0prior,
                       String pic, Integer lUT, Integer lCR, Double wn2qtyneed, Double wn4price,
                       JSONObject wrdNP, JSONObject wrdN, JSONObject wrddesc, JSONObject wrdprep) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_P = id_P == null ? "": id_P;
        this.id_OP = id_OP  == null ? "": id_OP;
        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
        this.id_C = id_C;
        this.id_CB = id_CB;
        this.id_O = id_O;
        this.index = index;
        this.rKey = UUID19.uuid8();
        this.ref = ref == null? "": ref;
        this.refB = refB == null? "": refB;
        this.grp = grp == null? "1000": grp;
        this.grpB = grpB == null? "1000": grpB;
        this.wn0prior = wn0prior;
        this.seq = index == 0 ? "0" : "2" ;
        this.priority = 1;
        this.wrdNP = wrdNP == null? (JSONObject) wrdEmpty.clone(): wrdNP;
        this.wrdN = wrdN  == null? (JSONObject) wrdEmpty.clone(): wrdN;
        this.pic = pic  == null? "": pic;
        this.lUT = lUT  == null? 0: lUT;
        this.lCR = lCR  == null? 0: lCR;
        this.wn2qtyneed = wn2qtyneed == null? 1: wn2qtyneed;
        this.wn4price = wn4price == null? 0: wn4price;
        this.wrddesc = wrddesc  == null? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.wrdprep = wrdprep == null? (JSONObject) wrdEmpty.clone(): wrdprep;
    }


    private static class Hod{
        private static final OrderOItem instance = new OrderOItem();
    }

    public static OrderOItem getInstance(){
        return OrderOItem.Hod.instance;
    }

    public void genRKey() {
        this.rKey =  UUID19.uuid8();
    }

    /**
     * 产品id
     */
    private String id_P = "";

    /**
     * 零件次序
     */
    private Integer wn0prior = 100;
    private Integer priority;

    /**
     * 零件父公司id
     */
    private String id_CP = "";

    private String id_OP = "";
    private String id_CB;
    private String id_O;
    private String id_C;
    private String rKey;

    /**
     * 零件编号
     */
    private String ref;
    private String refB;

    /**
     * 所有父产品名称拼接存放
     */
    private JSONObject wrdNP;
    private JSONObject wrdN;

    /**
     * 零件图片
     */
    private String pic  = "";

    private Integer objSub  = 0;


    /**
     * 零件用量单位，例：个，只，件
     */
    @JsonProperty("lUT")
    private Integer lUT = 0;

    /**
     * 零件的货币类型，例：人民币，美元
     */
    @JsonProperty("lCR")
    private Integer lCR = 0;

    /**
     * 零件用户组别
     */
//    private String grpU;

    /**
     * ### -零件制作用量,需要数量- 不用
     * 数量
     */
    private Double wn2qtyneed;


    /**
     * 零件单价
     */
    private Double wn4price;


    /**
     * 产品组别
     */
    private String grpB = "1000";
    private String grp = "1000";


    /**
     * 是否合并: 0 : 未合并，1 : 合并，2 : 被合并
     */
    private Integer merge = 0;
//    private Integer bmdpt = 0;

    private Integer index = 0;


    private JSONObject wrddesc;

    private JSONObject wrdprep;

    private JSONArray subTask;


    /**
     *
     */
    private String seq = "1";

    private String tmd;

    private Integer tvs;
}
