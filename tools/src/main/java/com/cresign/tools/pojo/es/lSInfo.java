package com.cresign.tools.pojo.es;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.uuid.UUID19;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;


/**
 * ##class: lSProd
 * ##description: 产品类
 * @author jackson
 * @updated 2019-07-16 15:25
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor//全参构造
@NoArgsConstructor//无参构造
@Document("lSInfo")
public class lSInfo implements Serializable {

    public lSInfo(String id_I, String id_C, String id_CP, JSONObject wrdN, JSONObject wrddesc, String grp,
                  String ref, String pic, String refDC) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_I = id_I;
        this.id_C = id_C;
        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
        this.wrdN = wrdN  == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc  == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.grp = grp == null ? "1000": grp;
        this.ref = ref == null ? "": ref;
        this.pic = pic  == null ? "": pic;
        this.refDC = refDC  == null ? "": refDC;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.qr = UUID19.uuid();

    }

    private String id_I;

    private String id_C;
//    private String id_CB;

    private String id_CP;

    private String id_IP;

    private JSONObject wrdN;

    private JSONObject wrddesc;

    private JSONArray arrI;

    private String grp;

    private JSONArray arrGrp = new JSONArray();

    private String qr = "";

    private String ref;

    private String pic;

    private String refDC;

    private String tmd;

    private String tmk;

    private JSONObject wrdTag;

    private Long wn0fsize;





}
