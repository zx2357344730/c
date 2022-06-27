package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.assetCard.AssetInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;



@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "Asset")
public class Asset {

    private String id;

    private AssetInfo info; // JSONObject

    private JSONObject file00s;

    private JSONArray  view;

    private JSONObject spec;

    private JSONObject text00s;

    private JSONObject picroll00s;

    private JSONObject link00s;

    private JSONObject chkin00s;

    private JSONObject sumChkin;

    private JSONObject menu;

    private JSONObject role;

    private JSONObject def;

    private JSONObject locSetup;   //库存位置

    private JSONObject setPdf;   //PDF 设置

    @JsonProperty("aArrange")
    private JSONObject aArrange;   //订单排产

    private JSONObject mkCoupon;  //制作优惠券卡片

    private JSONObject coupon;     //优惠券

    @JsonProperty("cTrigger")
    private JSONObject cTrigger;

    private JSONObject powerup;    //限量卡

    private JSONObject excelControl;//Excel

    private JSONObject statCheck;

    private JSONObject qtySafe;

    private JSONObject rpi;

    private JSONObject control;

    private JSONObject flowControl;

    private JSONObject qrShareCode;

    @JsonProperty("aStock")
    private JSONObject aStock;

    @JsonProperty("aMoney")
    private JSONObject aMoney;

    private JSONObject refAuto;

    private JSONObject summ00s;

    private JSONObject grpDetail;

    private JSONObject summx;

    private JSONObject chgSupp;

    private JSONObject tempa;




    private Integer tvs = 1;

}
