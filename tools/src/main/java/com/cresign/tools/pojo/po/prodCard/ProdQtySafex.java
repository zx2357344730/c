package com.cresign.tools.pojo.po.prodCard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Prod")
@Data
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProdQtySafex {

    public ProdQtySafex(String id_C, Integer wntSafe, Double wn2qty, Double wn2qtymin, Double wn2qtymob, JSONArray objSafex) {

        this.id_C = id_C;
        this.wntSafe = wntSafe == null? 1: wntSafe;
        this.wn2qty = wn2qty == null? 0: wn2qty;
        this.wn2qtymin = wn2qtymin == null? 1: wn2qtymin;
        this.wn2qtymob = wn2qtymob == null? 1: wn2qtymob;
        this.objSafex = objSafex == null? new JSONArray(): objSafex;
    }

    private String id_C;

    private Integer wntSafe;

    private Double wn2qty;
    private Double wn2qtymin;
    private Double wn2qtymob;

    private JSONArray objSafex;

}
