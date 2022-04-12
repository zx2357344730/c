package com.cresign.tools.pojo.po.prodCard;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
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
public class ProdInfo {

    public ProdInfo(String id_C, String id_CP, String id_CB, JSONObject wrdN, JSONObject wrddesc, String grp,
                  String ref, String pic, Integer lDC, Integer lUT) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_C = id_C;
        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
        this.id_CB = id_CB;
        this.wrdN = wrdN  == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc  == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.grp = grp == null ? "1000": grp;
        this.ref = ref == null ? "": ref;
        this.pic = pic  == null ? "": pic;
        this.lDC = lDC  == null ? 0: lDC;
        this.lUT = lUT  == null ? 0: lUT;
        this.tmd = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
        this.tmk = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
    }

    private String id_C;

    private String id_CP;

    private String id_CB;

    private JSONObject wrdN;

    private JSONObject wrddesc;

    private String grp;

    private String ref;

    private String pic;

    @JsonProperty("lDC")
    private Integer lDC;

    @JsonProperty("lUT")
    private Integer lUT;

    private String tmd;

    private String tmk;

}
