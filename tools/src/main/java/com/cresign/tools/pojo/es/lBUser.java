package com.cresign.tools.pojo.es;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * ##class: lBUser
 * ##description: 买家用户
 * @author jackson
 * @updated 2019-07-17 09:00
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor//全参构造
@NoArgsConstructor//无参构造
@Document(collection = "lBUser")
public class lBUser implements Serializable {

    public lBUser(String id_U, String id_CB, JSONObject wrdN, JSONObject wrdNCB, JSONObject wrdNReal, JSONObject wrddesc, String grpU,
                  String mbn, String refU,
                  String id_WX, String pic, String dep) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_U = id_U;
        this.id_CB = id_CB;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrdNCB = wrdNCB == null ? (JSONObject) wrdEmpty.clone(): wrdNCB;
        this.wrdNReal = wrdNReal == null ? (JSONObject) wrdEmpty.clone(): wrdNReal;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.mbn = mbn == null ? "": mbn;
        this.grpU = grpU == null ? "1000" : grpU;
        this.refU = refU == null ? "" : refU;
        this.id_WX = id_WX == null ? "" : id_WX;
        this.pic = pic == null ? "" : pic;
        this.dep = dep == null ? "1000" : dep;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }

    private String id_U;

    private String id_C;

    private String id_CB;

    private JSONObject wrdN;

    private JSONObject wrdNCB; // need delete

    private String grpU;

    private String refU;

    private String pic;

    private String tmd;

    private String tmk;

    private JSONObject wrddesc;

    private String id_APP;

    private String id_WX;

    private String cem;

    private String defNG;
    private String defCR;

    private JSONObject wrdNReal;

    private String mbn;

    private String cnty;

    private String dep;
}
