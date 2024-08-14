package com.cresign.tools.pojo.po.prodCard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Prod")
@Data
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProdPriceQty {

    public ProdPriceQty(Integer lCR, Integer wntLead, Integer wntShip, Double wn2qtyday, Double wn2qtymoq, Double wn4price,
                        JSONArray objGrpDis, JSONArray objQtyDis) {

        this.lCR = lCR == null? 0: lCR;
        this.wntLead = wntLead == null? 1: wntLead;
        this.wntShip = wntShip == null? 1: wntShip;
        this.wn2qtyday = wn2qtyday == null? 1: wn2qtyday;
        this.wn2qtymoq = wn2qtymoq == null? 1: wn2qtymoq;
        this.wn4price = wn4price == null? 0: wn4price;
        this.objGrpDis = objGrpDis == null? new JSONArray(): objGrpDis;
        this.objQtyDis = objQtyDis == null? new JSONArray(): objQtyDis;
    }

    @JsonProperty("lCR")
    private Integer lCR;
    private Integer wntLead;
    private Integer wntShip;

    private Double wn2qtyday;
    private Double wn2qtymoq;
    private Double wn4price;

    private JSONArray objGrpDis;
    private JSONArray objQtyDis;

}
