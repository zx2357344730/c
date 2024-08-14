package com.cresign.tools.pojo.po.prodCard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DoubleUtils;
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
public class ProdCost {

    public ProdCost(Integer wntDur, Integer wntPrep, Double wn2qtymore, JSONArray objData) {

        this.wntDur = wntDur == null? 1: wntDur;
        this.wntPrep = wntPrep == null? 1: wntPrep;
        this.wn2qtymore = wn2qtymore == null? 1: wn2qtymore;
        this.objData = objData == null? new JSONArray(): objData;
        this.wn4cost = 0.0;
        for (int i = 0; i < objData.size(); i++) {
            this.wn4cost = DoubleUtils.add(this.wn4cost, objData.getJSONObject(i).getDouble("wn4costT"));
        }
    }

    private Integer wntDur;
    private Integer wntPrep;

    private Double wn2qtymore;
    private Double wn4cost;

    private JSONArray objData;

}
