package com.cresign.tools.pojo.es;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
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
@AllArgsConstructor//全参构造
@NoArgsConstructor//无参构造
@Document(collection = "lSBComp")
public class lSBComp {

    public lSBComp(String id_C, String id_CP, String id_CB, String id_CBP, JSONObject wrdNC,
                   JSONObject wrddesc, JSONObject wrdNCB, JSONObject wrddescB, String grp, String grpB,
                   String refC, String refCB, String picC, String picCB) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_C = id_C;
        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
        this.id_CB = id_CB;
        this.id_CBP = id_CBP == null || id_CBP == "" ? id_CB: id_CBP;
        this.wrdNC = wrdNC == null ? (JSONObject) wrdEmpty.clone(): wrdNC;
        this.wrdNCB = wrdNCB == null ? (JSONObject) wrdEmpty.clone(): wrdNCB;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.wrddescB = wrddescB == null ? (JSONObject) wrdEmpty.clone(): wrddescB;
        this.grp = grp == null ? "1000": grp;
        this.grpB = grpB == null ? "1000": grpB;
        this.refC = refC == null ? "": refC;
        this.refCB = refCB == null ? "": refCB;
        this.picC = picC == null ? "": picC;
        this.picCB = picCB == null ? "": picCB;
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());

    }

    private String id_C;

    private String id_CP;

    private String id_CB;

    private String id_CBP;

    private JSONObject wrdNC;

    private JSONObject wrddesc;

    private JSONObject wrdNCB;

    private JSONObject wrddescB;

    private String grp;

    private String grpB;

    private String refC;

    private String refCB;

    private String picC;

    private String picCB;

    private String tmd;

    private String tmk;
}
