package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author tangzejin
 * @updated 2019/6/12
 * @ver 1.0.0
 * ##description: 订单实体类
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Order")
@Data
public class Order {

    private String id;

    private OrderInfo info;

    @JsonProperty("oItem")
    private JSONObject oItem;


    private JSONArray view;

    private JSONObject casItemx;

    private JSONObject file00s;

    private JSONObject spec;

    private JSONArray term00s;

    private JSONObject text00s;

    private JSONObject table00s;

    private JSONObject grid;

    private JSONObject picroll00s;

    private JSONObject link00s;

    @JsonProperty("oPdf")
    private JSONObject oPdf;

    private JSONObject shipping;

    private JSONObject action;

    private JSONObject cusmsg;

    private JSONObject oQc;

    @JsonProperty("oStock")
    private JSONObject oStock;

    @JsonProperty("oTrigger")
    private JSONObject oTrigger;

    @JsonProperty("oDate")
    private JSONObject oDate;

    @JsonProperty("oMoney")
    private JSONObject oMoney;

    private JSONObject qrShareCode;

    private JSONObject summ00s;

    private JSONObject summx;

    private JSONObject tax;


    private JSONObject tempa;

    private Integer tvs = 1;

}
