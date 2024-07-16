package com.cresign.tools.pojo.po.infoCard;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.uuid.UUID19;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Info")
@Data
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoInfo {

    public InfoInfo(String id_C, String id_CP, String id_CB, JSONObject wrdN, JSONObject wrddesc, String grp,
                    String ref, String pic) {

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
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.qr = UUID19.uuid();

    }

    private String id_C;

    private String id_CP;

    private String id_CB;

    private String id_PP;

    private JSONObject wrdN;

    private JSONObject wrddesc;

    private String grp;
    private String grpB;

    private String ref;

    private String pic;


    private String tmd;

    private String tmk;

    private String qr;

    private JSONObject wrdTag;

    private String refDC;

}
