package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.prodCard.ProdInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author kevin
 * @updated 2021/6/1
 * @ver 1.0.0
 * ##description: 产品实体类
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Prod")
@Data
public class Prod {

    private String id;

    private ProdInfo info;

    private JSONArray view;

    private JSONObject part;

    private JSONObject spec;

    private JSONObject file00s;

    private JSONObject pack;

    private JSONObject text00s;

    private JSONObject table00s;

    private JSONObject grid;

    private JSONObject summ00s;

    private JSONObject picroll00s;

    private JSONObject color;

    private JSONObject priceQty;

    private JSONObject quali;

    private JSONObject cost;

    private JSONObject link00s;   // 连接


    private JSONObject qtySafex;    // 产品安全库存

    private JSONObject ch00s;

    private JSONObject buyInit;

//    private JSONObject qrShareCode;

    private JSONObject summx;

    private JSONObject en;

    private JSONObject tempa;



    private Integer tvs = 1;

    private Long wn0fsize;



}
