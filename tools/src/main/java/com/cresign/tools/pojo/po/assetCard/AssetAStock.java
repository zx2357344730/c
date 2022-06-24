package com.cresign.tools.pojo.po.assetCard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "lBAsset")
public class AssetAStock {

    public AssetAStock(String locAddr, JSONArray locSpace, JSONArray spaceQty) {
        JSONArray array = new JSONArray();
        this.locAddr = locAddr == null ? "" : locAddr;
        this.locSpace = locSpace == null ? array : locSpace;
        this.spaceQty = spaceQty == null ? array : spaceQty;
    }

    private String locAddr;
    private JSONArray locSpace;
    private JSONArray spaceQty;
    private Double wn2qtyResv = 0.0;
    private JSONObject resvQty;

}
