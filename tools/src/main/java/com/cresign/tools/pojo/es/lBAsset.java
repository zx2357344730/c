package com.cresign.tools.pojo.es;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DoubleUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor//全参构造
@NoArgsConstructor//无参构造
@Document(collection = "lBAsset")
public class lBAsset {

    public lBAsset(String id_A, String id_C, String id_CP, String id_CB, String id_P, JSONObject wrdN, JSONObject wrddesc, String grp,
                   String pic, String ref, Integer lAT, Double wn2qty, Double wn4price) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_A = id_A;
        this.id_C = id_C;
        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
        this.id_CB = id_CB;
        this.id_P = id_P == null ? "": id_P;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone() : wrdN;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.grp = grp == null ? "1000" : grp;
        this.ref = ref == null ? "" : ref;
        this.pic = pic == null ? "" : pic;
        this.lAT = lAT == null ? 0 : lAT;
        this.wn2qty = wn2qty == 0 ? 0 : wn2qty;
        this.wn4price = wn4price == 0 ? 0 : wn4price;
        this.wn4value = DoubleUtils.multiply(wn2qty, wn4price);
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }


    private String id_A;

    private String id_C;

    private String id_CP;

    private String id_CB;

    private String id_P;

    private JSONObject wrdN;

    private JSONObject wrddesc;

    private String grp;

    private String grpP;

    private String ref;

    private String pic;

    private Double wn2qty; // 现在能出仓的数量

    private Double wn4price;

    private Double wn4value;

    private Double wn2qtyResv; //被预约了的数量

    private Integer lAT; //Asset Type

    private Integer lUT;

    private Integer lCR;

    private String locAddr;//

    private JSONArray locSpace;//

    private JSONArray spaceQty;//

    private String tmd;

    private String tmk;

    private JSONObject wrdTagB;

    //private Integer lType; //delete
    //private String wcnN;//delete
    // private String wenN;//delete
}
