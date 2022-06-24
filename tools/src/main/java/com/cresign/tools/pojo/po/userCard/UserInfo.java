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
                    String def_C, String lNGdef, String lCRdef, String pic, String cnty, String cem,
                    String mbn, Integer phoneType) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_WX = id_WX == null ? "": id_WX;
        this.id_APP = id_APP == null ? "": id_APP;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrdNReal = wrdNReal == null ? (JSONObject) wrdEmpty.clone(): wrdNReal;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.def_C = def_C == null ? "5f2a2502425e1b07946f52e9": def_C;
        this.lNGdef = lNGdef == null ? "": lNGdef;
        this.lCRdef = lCRdef == null ? "": lCRdef;
        this.pic = pic == null ? "": pic;
        this.cnty = cnty == null ? "": cnty;
        this.cem = cem == null ? "": cem;
        this.mbn = mbn == null ? "": mbn;
        this.phoneType = phoneType == null ? 86: phoneType;
        this.tmd = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
        this.tmk = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
    }

    private String id_WX;

    private String id_APP;

    private String id_AUN;

    private JSONObject wrdN;

    private JSONObject wrdNReal;

    private JSONObject wrddesc;

    private String def_C;

//    private String pwd;
//    private String usn;

    private String lNGdef;

    private String lCRdef;

    private String pic;

    private String cnty;

    private String cem;

    private String mbn;

    private Integer phoneType;

    private String authCode;

    private String tmd;

    private String tmk;
}
