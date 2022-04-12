package com.cresign.tools.pojo.po.assetCard;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Asset")
@Data
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetInfo {

    public AssetInfo(String id_C, String id_CP, String id_P, JSONObject wrdN, JSONObject wrddesc,
                     String grp, String ref, String pic, Integer lAT) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_C = id_C;
        this.id_CP = id_CP == null ? id_C: id_CP;
        this.id_P = id_P == null ? "": id_P;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.grp = grp == null ? "": grp;
        this.ref = ref == null ? "": ref;
        this.pic = pic == null ? "": pic;
        this.lAT = lAT == null ? 0: lAT;
        this.tmd = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
        this.tmk = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
    }

    private String id_C;

    private String id_CP;

    private String id_P;

    private JSONObject wrdN;

    private JSONObject wrddesc;

    private String grp;

    private String ref;

    private String pic;

    private Double wn2qty = 0.0;//

    @JsonProperty("lAT")
    private Integer lAT; //Asset Type

    private String tmd;

    private String tmk;
}
