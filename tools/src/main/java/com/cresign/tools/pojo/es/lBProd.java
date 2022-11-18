package com.cresign.tools.pojo.es;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ##class: lBProd
 * ##description:
 * @author jackson
 * @updated 2019-07-18 14:32
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor//全参构造
@NoArgsConstructor//无参构造
@Document(collection = "lBProd")
public class lBProd {

    public lBProd(String id_P, String id_C, String id_CP, String id_CB, JSONObject wrdN, JSONObject wrddesc,
                  String grp, String grpB, String ref, String refB, String pic, Integer lDC, Integer lUT) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_P = id_P;
        this.id_C = id_C;
        this.id_CP = id_CP == null || id_CP.equals("") ? id_C: id_CP;
        this.id_CB = id_CB;
        this.wrdN = wrdN  == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc  == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.grp = grp == null ? "1000": grp;
        this.grpB = grpB == null ? "1000": grpB;
        this.ref = ref == null ? "": ref;
        this.refB = refB == null ? "": refB;
        this.pic = pic  == null ? "": pic;
        this.lDC = lDC  == null ? 0: lDC;
        this.lUT = lUT  == null ? 0: lUT;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }

    private String id_P;

    private String id_C;

    private String id_CB;

    private String id_CP;

    private JSONObject wrdN;

    private JSONObject wrddesc;

    private JSONArray arrP;

    private String grp;

    private String grpB;

    private String ref;

    private String refB;

    private String pic;

    private Integer lDC;

    private Integer lUT;

    private Integer lCR;//

    private double wn4price;

    private String tmd;

    private String tmk;

}
