package com.cresign.tools.pojo.po.assetCard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DoubleUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "Asset")
public class AssetAStock {

    public AssetAStock(Double wn4price, String locAddr, JSONArray locSpace, JSONArray spaceQty) {
        JSONArray array = new JSONArray();
//        JSONObject json = new JSONObject();
        this.wn2qty = 0.0;
        for (int i = 0; i< spaceQty.size(); i++)
        {
            this.wn2qty = DoubleUtils.add(this.wn2qty,spaceQty.getDouble(i));
        }
//        this.wn2qty = wn2qty == null ? 0.0 : wn2qty;
        this.wn4price = wn4price == null ? 0.0 : wn4price;
        this.wn4value = DoubleUtils.multiply(wn2qty,wn4price);
        this.locAddr = locAddr == null ? "" : locAddr;
        this.locSpace = locSpace == null ? array : locSpace;
        this.spaceQty = spaceQty == null ? array : spaceQty;

    }

    public AssetAStock(Double wn4price) {
        JSONArray array = new JSONArray();
        this.wn2qty = 1.0;
        this.wn4price = wn4price == null ? 0.0 : wn4price;
        this.wn4value = DoubleUtils.multiply(wn2qty,wn4price);
        this.locAddr = "";
        this.locSpace = array;
        this.spaceQty = array;

    }

    public AssetAStock(Double wn4price, String locAddr, JSONArray locSpace, JSONArray spaceQty, Double wn2qtyResv, JSONObject resvQty) {
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();
        this.wn2qty = 0.0;
        for (int i = 0; i< spaceQty.size(); i++)
        {
            this.wn2qty = DoubleUtils.add(this.wn2qty,spaceQty.getDouble(i));
        }
//        this.wn2qty = wn2qty == null ? 0.0 : wn2qty;
        this.wn4price = wn4price == null ? 0.0 : wn4price;
        this.wn4value = DoubleUtils.multiply(wn2qty,wn4price);
        this.locAddr = locAddr == null ? "" : locAddr;
        this.locSpace = locSpace == null ? array : locSpace;
        this.spaceQty = spaceQty == null ? array : spaceQty;
        this.wn2qtyResv = wn2qtyResv == null ? 0.0 : wn2qtyResv;
        this.resvQty = resvQty == null ? json : resvQty;

    }

    private Double wn2qty;
    private Double wn4price;
    private Double wn4value;
    private String locAddr;
    private JSONArray locSpace;
    private JSONArray spaceQty;
    private Double wn2qtyResv = 0.0;
    private JSONObject resvQty;

}
