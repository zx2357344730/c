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
 * ##class: lNComp
 * ##description:
 * @author Kevin
 * @updated 2019-08-19 16:41
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor//全参构造
@NoArgsConstructor//无参构造
@Document(collection = "lNComp")
public class lNComp {

    public lNComp(String id_C, String id_CP,  JSONObject wrdN, JSONObject wrddesc, JSONObject wrdTag, String ref, String pic) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn", "");

        JSONObject jsonTag = new JSONObject();
        jsonTag.put("cn", new JSONArray());

        this.id_C = id_C;
        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.wrdTag = wrdTag == null ? jsonTag: wrdTag;
        this.ref = ref == null ? "": ref;
        this.pic = pic == null ? "": pic;
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());

    }

    private String id_C;

    private String id_CP;

    private JSONObject wrdN;

    private JSONObject wrddesc;

    private JSONObject wrdTag;

    private String ref;

    private String pic;

    private String tmd;

    private String tmk;
}
