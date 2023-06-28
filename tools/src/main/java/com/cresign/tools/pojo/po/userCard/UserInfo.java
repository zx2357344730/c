package com.cresign.tools.pojo.po.userCard;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "User")
@Data
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {

    public UserInfo(String id_WX, String id_APP, JSONObject wrdN, JSONObject wrdNReal, JSONObject wrddesc,
                    String def_C, String defNG, String defCR, String pic, String cnty, String cem,
                    String mbn, Integer phoneType,String id_AUN) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn", "");

        this.id_WX = id_WX == null ? "": id_WX;
        this.id_APP = id_APP == null ? "": id_APP;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrdNReal = wrdNReal == null ? (JSONObject) wrdEmpty.clone(): wrdNReal;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.def_C = def_C == null ? "5f2a2502425e1b07946f52e9": def_C;
        this.defNG = defNG == null ? "": defNG;
        this.defCR = defCR == null ? "": defCR;
        this.pic = pic == null ? "": pic;
        this.cnty = cnty == null ? "": cnty;
        this.cem = cem == null ? "": cem;
        this.mbn = mbn == null ? "": mbn;
        this.phoneType = phoneType == null ? 86: phoneType;
        this.id_AUN = id_AUN == null ? "" : id_AUN;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }

    // 小程序的 ID
    private String id_WX;

    // 推送专用ID
    private String id_APP;

    // App 端的微信登录 ID
    private String id_AUN;

    private JSONObject wrdN;

    private JSONObject wrdNReal;

    private JSONObject wrddesc;

    private JSONObject wrdTag; // not here move to tag

    private String def_C;

    private String defNG;

    private String defCR;

    private String pic;

    private String cnty;

    private String cem;

    private String mbn;

    private Integer phoneType;

    private String authCode; // for Mail service

    private String tmd;

    private String tmk;
}
