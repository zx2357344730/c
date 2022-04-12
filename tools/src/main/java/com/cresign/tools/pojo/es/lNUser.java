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
 * ##author: jackson
 * ##updated: 2019-07-17 09:00
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor//全参构造
@NoArgsConstructor//无参构造
@Document(collection = "lNUser")
public class lNUser implements Serializable {

    public lNUser(String id_U, JSONObject wrdN, JSONObject wrddesc,  String pic) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_U = id_U;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.pic = pic == null ? "" : pic;
        this.tmd = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
        this.tmk = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
    }

    private String id_U;

    private JSONObject wrdN;

    private String pic;

    private String tmd;

    private String tmk;

    private JSONObject wrddesc;

    private String id_APP;
    private String id_WX;

    private String cem;

    private String defNG;
    
    private JSONObject wrdNReal;

    private String mbn;

    private String cnty;


}
