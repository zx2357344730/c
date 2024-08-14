package com.cresign.tools.pojo.po.prodCard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import com.cresign.tools.uuid.UUID19;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Prod")
@Data
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProdPart {

    public ProdPart(String id_P, String id_C, String id_CP, Integer index, String grp, String grpB, String ref, String refB,
                    String pic, Integer lUT, Integer lCR, Integer bmdpt, Integer wn0prior, Integer wntPrep, Integer wntDur, Integer wntSafe,
                    Double wn2qty, Double wn4qtyneed, Double wn4price, Double wn2qtymore, Double wn2port, JSONObject wrdN,
                    JSONObject wrddesc, JSONObject wrdprep) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_P = id_P;
        this.id_C = id_C;
        this.id_CP = id_CP == null || id_CP.equals("") ? id_C: id_CP;
        this.index = index;
        this.grp = grp == null? "1000": grp;
        this.grpB = grpB == null? "1000": grpB;
        this.ref = ref == null? "": ref;
        this.refB = refB == null? "": refB;
        this.pic = pic == null? "": pic;
        this.seq = index == 0 ? "0" : "2";
        this.lUT = lUT == null? 0: lUT;
        this.lCR = lCR == null? 0: lCR;
        this.bmdpt = bmdpt;
        this.wn0prior = wn0prior;
        this.wntPrep = wntPrep;
        this.wntDur = wntDur;
        this.wntSafe = wntSafe;
        this.wn2qty = wn2qty == null? 1: wn2qty;
        this.wn4qtyneed = wn4qtyneed == null? 1: wn4qtyneed;
        this.wn4price = wn4price == null? 0: wn4price;
        this.wn2qtymore = wn2qtymore == null? 0: wn2qtymore;
        this.wn2port = wn2port == null? 0: wn2port;
        this.wrdN = wrdN  == null? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc  == null? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.wrdprep = wrdprep == null? (JSONObject) wrdEmpty.clone(): wrdprep;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }


    private static class Hod{
        private static final ProdPart instance = new ProdPart();
    }

    public static ProdPart getInstance(){
        return ProdPart.Hod.instance;
    }

    private String id_P;
    private String id_C;
    private String id_CP = "";

    private Integer index = 0;

    private String grp = "1000";
    private String grpB = "1000";

    private String ref;
    private String refB;

    private String pic  = "";

    private String seq = "1";

    @JsonProperty("lUT")
    private Integer lUT = 0;

    @JsonProperty("lCR")
    private Integer lCR = 0;

    private Integer bmdpt;

    private Integer wn0prior = 100;

    private Integer wntPrep;
    private Integer wntDur;
    private Integer wntSafe;

    private Double wn2qty;
    private Double wn4qtyneed;
    private Double wn4price;
    private Double wn2qtymore;
    private Double wn2port;

    private JSONObject wrdN;
    private JSONObject wrddesc;
    private JSONObject wrdprep;

    private String tmd;
}
