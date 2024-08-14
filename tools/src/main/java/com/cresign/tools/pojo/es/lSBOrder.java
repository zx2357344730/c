package com.cresign.tools.pojo.es;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.cresign.tools.uuid.UUID19;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ##class: lSBComp
 * ##description: 卖买家公司类
 * @author jackson
 * @updated 2019-08-19 16:41
 **/

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "lSBOrder")
//生成全参数构造函数
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
public class lSBOrder {

    public lSBOrder(String id_C, String id_CB, String id_CP, String id_CBP,
                    String id_OP, String id_O, JSONArray id_P, String ref, String refB, String grp, String grpB,
                    String pic, Integer lST, Integer lCR,
                    JSONObject wrdN, JSONObject wrddesc, JSONObject wrddescB) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_O = id_O;
        this.id_OP = id_OP;
        this.id_C = id_C;
        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
        this.id_CB = id_CB;
        this.id_CBP = id_CBP == null || id_CBP == "" ? id_CB: id_CBP;
        this.arrP = id_P.size() == 0 ? new JSONArray(): id_P;
        this.wrdN = wrdN  == null? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc  == null? (JSONObject) wrdEmpty.clone() : wrddesc;
        this.wrddescB = wrddescB == null? (JSONObject) wrdEmpty.clone(): wrddescB;
        this.grp = grp == null? "1000": grp;
        this.grpB = grpB == null? "1000": grpB;
        this.ref = ref == null? "": ref;
        this.refB = refB == null? "": refB;
        this.pic = pic  == null? "": pic;
        this.lST = lST  == null? 0: lST;
        this.lCR = lCR  == null? 0: lCR;
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.qr = UUID19.uuid();

    }
    public lSBOrder(OrderInfo info, String id_O) {
        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_O = id_O;
        this.id_OP = info.getId_OP();
        this.id_C = info.getId_C();
        this.id_CP = info.getId_CP() == null || id_CP == "" ? id_C: info.getId_CP();
        this.id_CB = info.getId_CB();
        this.id_CBP = info.getId_CBP() == null || id_CBP == "" ? id_CB: info.getId_CBP();
        this.arrP = new JSONArray();
        this.wrdN = info.getWrdN()  == null? (JSONObject) wrdEmpty.clone(): info.getWrdN();
        this.wrddesc = wrddesc  == null? (JSONObject) wrdEmpty.clone() : info.getWrddesc();
//        this.wrddescB = wrddescB == null? (JSONObject) wrdEmpty.clone(): info.getWrddescB();
        this.grp = info.getGrp() == null? "1000": info.getGrp();
        this.grpB = info.getGrpB() == null? "1000": info.getGrpB();
        this.ref = info.getRef() == null? "": info.getRef();
        this.refB = info.getRefB() == null? "": info.getRefB();
        this.pic = info.getPic()  == null? "": info.getPic();
        this.lST = info.getLST()  == null? 0: info.getLST();
        this.lCR = info.getLCR()  == null? 0: info.getLCR();
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.qr = UUID19.uuid();

    }

    public static lSBOrder getInstance(){
        return lSBOrder.Hod.instance;
    }

    private static class Hod{
        private static final lSBOrder instance = new lSBOrder();
    }


    private String id_O;
    private String id_OP;

    private String id_CB;
    private String id_CBP;

    private String id_C;
    private String id_CP;
    private JSONArray arrP;

    private String id_UC;
    private String id_UCB;

    private Integer lDG = 0;

    private JSONArray grpT = new JSONArray();

    private String grp;

    private String grpB;

    private String usedGrp;
    private String usedGrpB;

    private JSONObject wrdN;
    private JSONObject wrddesc;

    // xxxxx no need
    private JSONObject wrddescB;

    private Integer lST;

    private Integer lCR;

    private Integer priority;

    private String pic;

    private String ref;

    private String refB;

    private String refOP;

    private String refDC;

    private double wn4price;
    private double wn2qty;

    private double wn2qtyship;

    private double wn2qtybreak;

    private double wn2qtyfixed;

    private double wn2fin;

    private double wn2progress;

    private double wn4mnymade;


    private double wn4mnynow;

    private double wn4mnypaid;

    private double wn2cas;


    private String tmd;

    private String tmk;

    private String teStart;
    private String teFin;
    private String taStart;
    private String taFin;

    private String teShip;
    private String teGot;
    private String tePay;
    private String taShip;

    private String taGot;
    private String taPay;

    private long wntDur;

    private long wntShip;

    private long wntBuy;

    private long wntWait;

    private long wntPrep;
    private String qr;

    private JSONObject wrdTag;

    private JSONObject wrdTagB;

    private Long wn0fsize;


}
