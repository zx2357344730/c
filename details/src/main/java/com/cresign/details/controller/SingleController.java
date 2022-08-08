package com.cresign.details.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.details.service.SingleService;
import com.cresign.details.service.StorageService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.token.GetUserIdByToken;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * ##description:    detail 详细信息的增删改查控制层
 * ##author: JackSon
 * ##updated: 2020/8/10 14:11
 * ##version: 1.0
 */
@RestController
@RequestMapping("single")
public class SingleController {

    @Autowired
    private SingleService singleService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getTokenOfUserId;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @SecurityParameter
    @PostMapping("/v1/getSingle")
    public ApiResponse getSingle(@RequestBody JSONObject reqJson){
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        System.out.println("输出uuId:");
        System.out.println(request.getHeader("uuId"));
        // send also reqJson.id_C
        return singleService.getSingle(
                reqJson.getString("id"),
                reqJson.getString("listType"),
                tokData.getString("id_C"),
                reqJson.getString("id_C"),
                Integer.parseInt(reqJson.getString("tvs")),
                tokData.getString("id_U"),
                reqJson.getString("grp"),
                tokData.getString("grpU"));
    }

//    @SecurityParameter
//    @PostMapping("/v2/updateSingle")
//    public ApiResponse updateSingle2(@RequestBody JSONObject reqJson) throws IOException {
//
//        return singleService.updateSingle2(
//                reqJson.getString("id_C"),
//                reqJson.getJSONObject("data"),
//                reqJson.getString("listType"),
//                reqJson.getString("impChg"),
//                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                //"5f28bf314f65cc7dc2e60386",
//                reqJson.getString("grp"),
//                reqJson.getString("authType")
//        );
//
//    }


    @SecurityParameter
    @PostMapping("/v1/updateSingle")
    public ApiResponse updateSingle(@RequestBody JSONObject reqJson) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.updateSingle(
                tokData.getString("id_C"),
                reqJson.getJSONObject("data"),
                reqJson.getString("listType"),
                reqJson.getString("impChg"),
                reqJson.getJSONObject("listCol"),
                tokData.getString("id_U"),
                reqJson.getString("grp"),
                reqJson.getString("authType")
        );

    }


    @SecurityParameter
    @PostMapping("/v1/up_me")
    public ApiResponse updateMyUserDetail(@RequestBody JSONObject reqJson) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.updateMyUserDetail(
                tokData.getString("id_U"),
                reqJson.getJSONObject("data"),
                reqJson.getJSONObject("listCol"),
                reqJson.getString("impChg"));
    }

    @SecurityParameter
    @PostMapping("/v1/up_my_comp")
    public ApiResponse updateMyCompDetail(@RequestBody JSONObject reqJson) throws IOException {

        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.updateMyCompDetail(
                tokData.getString("id_C"),
                tokData.getString("id_U"),
                tokData.getString("grpU"),
                reqJson.getJSONObject("data"),
                reqJson.getJSONObject("listColC"),
                reqJson.getJSONObject("listColCB"),
                reqJson.getString("impChg"));
    }


//    /**
//     * getSingleMini版，根据key 获取其部分内容
//     * ##return:
//     * @throws IllegalAccessException
//     */
//    @SecurityParameter
//    @PostMapping("/v1/getSingleMini")
//    public String getSingleMini(@RequestBody Map<String, Object> reqJson) throws IllegalAccessException {
//
//        return singleService.getSingleMini(
//                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                //"5f28bf314f65cc7dc2e60386",
//                reqJson);
//
//    }

    @SecurityParameter
    @PostMapping("/v1/addEmptyCoup")
    public ApiResponse addEmptyCoup(@RequestBody JSONObject requestJson, HttpServletRequest request) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.addEmptyCoup(
                tokData.getString("id_U"),
                requestJson.getString("grp"),
                tokData.getString("id_C"),
                requestJson.getString("listType"),
                requestJson.getString("data")
        );
    }


    @PostMapping("/v1/delUseless")
    @SecurityParameter
    public ApiResponse delUseless(@RequestBody JSONObject reqJson) throws Exception {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.delUseless(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("id"),
                reqJson.getString("listType")
        );

    }

    @SecurityParameter
    @PostMapping("/v1/get_part")
    public String getPartCardData(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.getPartCardData(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("id_P")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/setOItem")
    public ApiResponse setOItem(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.setOItem(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("id_O"),
                reqJson.getJSONObject("oItemData"),
                reqJson.getString("listType"),
                reqJson.getString("grp")
        );
    }

//    @SecurityParameter
//    @PostMapping("/v1/updateModuleId")
//    public ApiResponse updateModuleId(@RequestBody JSONObject json) {
//        return singleService.updateModuleId(json.getString("id_C"));
//    }

    @SecurityParameter
    @PostMapping("/v1/getOItemDetail")
    public ApiResponse getOItemDetail(@RequestBody JSONObject json) {
        return singleService.getOItemDetail(
                json.getString("id_O"),
                json.getInteger("index"),
                json.getJSONArray("cardList"));
    }


    @SecurityParameter
    @PostMapping("/v1/delComp")
    public ApiResponse delComp(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.delComp(
                tokData.getString("id_U"),
                tokData.getString("id_C"));
    }

    @SecurityParameter
    @PostMapping("/v1/setActionStatus")
    public ApiResponse setActionStatus(@RequestBody JSONObject json) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.setActionStatus(
                tokData.getString("id_C"),
                json.getString("id_O"),
                json.getInteger("index"),
                json.getInteger("bcdStatus")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/setOItemRelated")
    public ApiResponse setOItemRelated(@RequestBody JSONObject json) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.setOItemRelated(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                json.getString("listType"),
                json.getString("grp"),
                json.getString("id_O"),
                json.getString("card"),
                json.getString("objName"),
                json.getInteger("index"),
                json.getJSONObject("content")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/getOItemRelated")
    public ApiResponse getOItemRelated(@RequestBody JSONObject json) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return singleService.getOItemRelated(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                json.getString("listType"),
                json.getString("grp"),
                json.getString("id_O"),
                json.getString("card"),
                json.getString("objName"),
                json.getInteger("index")
        );
    }


    @SecurityParameter
    @PostMapping("/v1/checkBcdNet")
    public ApiResponse checkBcdNet(@RequestBody JSONObject json) {
        return singleService.checkBcdNet(json.getString("id_C"));
    }

    @SecurityParameter
    @PostMapping("/v1/moveMoney")
    public ApiResponse moveMoney(@RequestBody JSONObject json) {
        return singleService.moveMoney(
                json.getString("id_U"),
                json.getString("id_C"),
                json.getString("listType"),
                json.getString("grp"),
                json.getString("fromId_A"),
                json.getString("toId_A"),
                json.getDouble("money")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/pushMoney")
    public ApiResponse pushMoney(@RequestBody JSONObject json) {
        return singleService.pushMoney(
                json.getString("id_U"),
                json.getString("id_C"),
                json.getString("listType"),
                json.getString("grp"),
                json.getString("id_A"),
                json.getString("id_O"),
                json.getDouble("money")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/popMoney")
    public ApiResponse popMoney(@RequestBody JSONObject json) {
        return singleService.popMoney(
                json.getString("id_U"),
                json.getString("id_C"),
                json.getString("listType"),
                json.getString("grp"),
                json.getString("id_A"),
                json.getString("id_O"),
                json.getDouble("money")
        );
    }

//    @SecurityParameter
//    @PostMapping("/v1/addAsset")
//    public ApiResponse addAsset(@RequestBody JSONObject json) throws IOException {
//        return singleService.addAsset(
//                json.getString("id_U"),
//                json.getString("id_C"),
//                json.getString("listType"),
//                json.getString("grp"),
//                json.getString("ref"),
//                json.getString("id_A"),
//                json.getString("id_P"),
//                json.getInteger("seq"),
//                json.getInteger("lCR"),
//                json.getInteger("lUT"),
//                json.getDouble("wn2qtyneed"),
//                json.getDouble("wn4price"),
//                json.getInteger("wn0prior"),
//                json.getString("locAddr"),
//                json.getJSONArray("locSpace"),
//                json.getJSONArray("spaceQty")
//        );
//    }

    @SecurityParameter
    @PostMapping("/v1/addMySpace")
    public ApiResponse addMySpace(@RequestBody JSONObject json) throws IOException {
        return singleService.addMySpace(
                json.getString("id_U"),
                json.getJSONObject("reqJson")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/cTriggerToTimeflow")
    public ApiResponse cTriggerToTimeflow(@RequestBody JSONObject json) throws IOException {
        return singleService.cTriggerToTimeflow(
                json.getString("id_U"),
                json.getString("id_C"),
                json.getString("listType"),
                json.getString("grp"),
                json.getInteger("index"),
                json.getBoolean("activate")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/prodPart")
    public ApiResponse prodPart(@RequestBody JSONObject json) {
        return singleService.prodPart(json.getString("id_P"));
    }

    @SecurityParameter
    @PostMapping("/v1/connectionComp")
    public ApiResponse connectionComp(@RequestBody JSONObject json) throws IOException {
        return singleService.connectionComp(
                json.getString("id_C"),
                json.getString("id_CB")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/connectionProd")
    public ApiResponse connectionProd(@RequestBody JSONObject json) throws IOException {
        return singleService.connectionProd(
                json.getString("id_C"),
                json.getString("id_P")
        );
    }

//    @SecurityParameter
//    @PostMapping("/v1/copyCard")
//    public ApiResponse copyCard(@RequestBody JSONObject json) {
//        return singleService.copyCard(
//                json.getString("fromId"),
//                json.getJSONArray("toId"),
//                json.getJSONArray("card"),
//                json.getString("table")
//        );
//    }

//    @SecurityParameter
//    @PostMapping("/v1/moveOStock")
//    public ApiResponse moveOStock(@RequestBody JSONObject json) {
//        return singleService.moveOStock(
//                json.getString("id_C"),
//                json.getString("fromId_O"),
//                json.getString("toId_O"),
//                json.getInteger("fromIndex"),
//                json.getInteger("toIndex"),
//                json.getDouble("wn2qtynow")
//        );
//    }

//    @SecurityParameter
//    @PostMapping("/v1/mergeOrder")
//    public ApiResponse mergeOrder(@RequestBody JSONObject json) {
//        return singleService.mergeOrder(
////                json.getString("id_C"),
//                json.getString("id_O"),
//                json.getJSONArray("mergeId_O"),
//                json.getInteger("index"),
//                json.getJSONArray("mergeIndex")
//        );
//    }
////
//    @SecurityParameter
//    @PostMapping("/v1/text00sToOItem")
//    public ApiResponse text00sToOItem(@RequestBody JSONObject json) {
//        return singleService.text00sToOItem(
//                json.getString("id_C"),
//                json.getString("id_O"),
//                json.getString("id"),
//                json.getInteger("cardIndex"),
//                json.getInteger("textIndex"),
//                json.getString("table")
//
//        );
//    }


    @SecurityParameter
    @PostMapping("/v1/splitOrder")
    public ApiResponse splitOrder(@RequestBody JSONObject json) {
        return singleService.splitOrder(
                json.getString("id_O"),
                json.getJSONArray("arrayIndex")
        );
    }




}