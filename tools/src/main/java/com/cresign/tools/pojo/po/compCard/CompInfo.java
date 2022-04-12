package com.cresign.tools.pojo.po.compCard;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Comp")
@Data
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompInfo {

    public CompInfo(String id_C, String id_CP, JSONObject wrdN, JSONObject wrddesc, String ref, String pic) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_C = id_C;
        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.ref = ref == null ? "": ref;
        this.pic = pic == null ? "": pic;
        this.tmk = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
        this.tmd = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
    }

    private String id_C;

    private String id_CP;

    private JSONObject wrdN;

    private JSONObject wrddesc;

    private String ref;

    private String pic;

    private String tmd;

    private String tmk;

}
