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
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName lSUser
 * @Date 2023/8/15
 * @ver 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor//全参构造
@NoArgsConstructor//无参构造
@Document(collection = "lSUser")
public class lSUser {
    public lSUser(String id_U, String id_C, JSONObject wrdN, JSONObject wrdNReal, JSONObject wrddesc, String grpU,
                  String mbn, String refU, String pic) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.id_U = id_U;
        this.id_C = id_C;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone(): wrdN;
        this.wrdNReal = wrdNReal == null ? (JSONObject) wrdEmpty.clone(): wrdNReal;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone(): wrddesc;
        this.mbn = mbn == null ? "": mbn;
        this.grpU = grpU == null ? "1000" : grpU;
        this.refU = refU == null ? "" : refU;
        this.pic = pic == null ? "" : pic;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        this.tmk = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }
    private String id_U;

    private String id_C;

    private JSONObject wrdN;

    private String grpU;

    private String refU;

    private String pic;

    private String tmd;

    private String tmk;

    private JSONObject wrddesc;

    private JSONObject wrdNReal;

    private String mbn;

    private String cnty;
}
