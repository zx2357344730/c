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
@Document(collection = "lNUser")
public class lNUser implements Serializable {

    public lNUser(String id_U, JSONObject wrdN, JSONObject wrddesc, JSONObject wrdNReal, JSONObject wrdTag, String pic,
                  String id_APP, String id_WX, String cem, String mbn, String cnty, String defNG) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn", "");

        JSONObject jsonTag = new JSONObject();
        jsonTag.put("cn", new JSONArray());

        this.id_U = id_U;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.wrdNReal = wrdNReal == null ? (JSONObject) wrdEmpty.clone(): wrdNReal;
        this.wrdTag = wrdTag == null ? jsonTag: wrdTag;
        this.pic = pic == null ? "" : pic;
        this.id_APP = id_APP == null ? "" : id_APP;
        this.id_WX = id_WX == null ? "" : id_WX;
        this.cem = cem == null ? "" : cem;
        this.mbn = mbn == null ? "" : mbn;
        this.cnty = cnty == null ? "" : cnty;
        this.defNG = defNG == null ? "" : defNG;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
    }

    private String id_U;

    private JSONObject wrdN;

    private JSONObject wrddesc;

    private JSONObject wrdNReal;

    private JSONObject wrdTag;

    private String pic;

    private String id_APP;

    private String id_WX;

    private String cem;

    private String mbn;

    private String cnty;

    private String defNG;

    private String defCR;

    private String tmd;

    private String tmk;

}
