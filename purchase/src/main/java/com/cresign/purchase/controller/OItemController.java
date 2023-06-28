package com.cresign.purchase.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.OItemService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("oItem")
public class OItemController {

    @Autowired
    private OItemService oItemService;

    @Autowired
    private GetUserIdByToken getUserToken;

    @SecurityParameter
    @PostMapping("/v1/mergeOrder")
    public ApiResponse mergeOrder(@RequestBody JSONObject json) {

        return oItemService.mergeOrders(
                json.getString("id_O"),
                json.getInteger("index"),
                json.getJSONArray("arrayMergeId_O"),
                json.getJSONArray("arrayMergeIndex")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/splitOrder")
    public ApiResponse splitOrder(@RequestBody JSONObject json) {
        return oItemService.splitOrder(
                json.getString("id_O"),
                json.getInteger("index"),
                json.getJSONArray("arrayWn2qty")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/moveOItem")
    public ApiResponse moveOItem(@RequestBody JSONObject reqJson) {
        try {
            return oItemService.moveOItems(
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    reqJson.getString("moveId_O"),
                    reqJson.getJSONArray("arrayMoveIndex")
            );
        } catch (Exception e){
            return getUserToken.err(reqJson, "moveOItem", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/delOItem")
    public ApiResponse delOItem(@RequestBody JSONObject json) throws IOException {
        return oItemService.delOItem(
                json.getString("id_O"),
                json.getInteger("index")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/movePosition")
    public ApiResponse movePosition(@RequestBody JSONObject json) {
        return oItemService.movePosition(
                json.getString("id_O"),
                json.getInteger("index"),
                json.getInteger("moveIndex")
        );
    }


    @SecurityParameter
    @PostMapping("/v1/replaceComp")
    public ApiResponse replaceComp(@RequestBody JSONObject json) throws CloneNotSupportedException, IOException {
        return oItemService.replaceComp(
                json.getString("id_O"),
                json.getJSONArray("arrayReplace")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/replaceProd")
    public ApiResponse replaceProd(@RequestBody JSONObject json) throws IOException {
        return oItemService.replaceProd(
                json.getJSONArray("arrayId_P"),
                json.getBoolean("isAdd")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/textToOItem")
    public ApiResponse textToOItem(@RequestBody JSONObject json) {
        return oItemService.textToOItem(
                json.getString("id_C"),
                json.getString("id_O"),
                json.getString("id"),
                json.getInteger("cardIndex"),
                json.getInteger("textIndex"),
                json.getString("table")

        );
    }

}
