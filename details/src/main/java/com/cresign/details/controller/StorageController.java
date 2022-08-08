package com.cresign.details.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.details.service.StorageService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.token.GetUserIdByToken;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DbUtils dbUtils;


//    @SecurityParameter
//    @PostMapping("/v1/goodsAllocation")
//    public ApiResponse goodsAllocation(@RequestBody JSONObject requestJson, HttpServletRequest request) throws IOException {
//        return storageService.goodsAllocation(
//                requestJson.getString("id_C"),
////                "5f28bf314f65cc7dc2e60386",
//                getUserToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                requestJson.getString("grp"),
//                requestJson.getString("listType"),
//                requestJson.getString("id_O"),
//                requestJson.getJSONArray("assetObject")
//        );
//    }
//
//    @SecurityParameter
//    @PostMapping("/v1/goodsWarehousing")
//    public ApiResponse goodsWarehousing(@RequestBody JSONObject requestJson, HttpServletRequest request) throws IOException {
//        return storageService.goodsWarehousing(
//                requestJson.getString("id_C"),
////                "5f28bf314f65cc7dc2e60386",
//                getUserToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                requestJson.getString("grp"),
//                requestJson.getString("listType"),
//                requestJson.getString("id_O"),
//                requestJson.getString("id_P"),
//                requestJson.getString("id_A"),
//                requestJson.getString("wn2qty")
//        );
//    }
//
//    @PostMapping("/v1/prodMerge")
//    @SecurityParameter
//    public ApiResponse prodMerge(@RequestBody JSONObject reqJson) throws IOException {
//
//        return storageService.prodMerge(
//                //"5f28bf314f65cc7dc2e60386",
//                getUserToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("grp"),
//                reqJson.getString("listType"),
//                reqJson.getString("id_to"),
//                (List<String>) reqJson.get("id_from"));
//
//    }
//
//    @PostMapping("/v1/prodSplit")
//    @SecurityParameter
//    public ApiResponse prodSplit(@RequestBody JSONObject reqJson) throws IOException {
//
//        Object wn2qty = DetailsUtils.isNumber(reqJson.get("wn2qty").toString());
//        if (wn2qty.equals(false)){
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);
//        }
//        return storageService.prodSplit(
//                getUserToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("grp"),
//                reqJson.getString("listType"),
//                reqJson.getString("id_to"),
//                Double.parseDouble(wn2qty.toString()));
//    }
//
//
//    @SecurityParameter
//    @PostMapping("/v1/batchQtySafe")
//    public ApiResponse batchQtySafe(@RequestBody Map<String, Object> requestJson) throws IOException {
//        return storageService.batchQtySafe(
//                requestJson.get("id_C").toString(),
//                //"5f28bf314f65cc7dc2e60386",
//                getUserToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                requestJson.get("listType").toString()
////                "5f2a2502425e1b07946f5404",
////                "5f28bf314f65cc7dc2e60386",
////                "lSAsset"
//
//        );
//    }
//
//    @SecurityParameter
//    @PostMapping("/v1/addOrder")
//    public ApiResponse addOrder(@RequestBody Map<String, Object> requestJson, HttpServletRequest request) throws IOException {
//
//        return storageService.addOrder(
//                //"5f28bf314f65cc7dc2e60386",
//                getUserToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                requestJson.get("id_C").toString(),
//                requestJson.get("grp").toString(),
//                (HashMap<String, Object>) requestJson.get("reqJson"),
//                requestJson.get("listType").toString(),
//                requestJson.get("data").toString());
//    }

    @SecurityParameter
    @PostMapping("/v1/getWarehouse")
    public ApiResponse getWarehouse(@RequestBody JSONObject json) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.getWarehouse(
                tokData.getString("id_C"));
    }

    @SecurityParameter
    @PostMapping("/v1/getArea")
    public ApiResponse getArea(@RequestBody JSONObject json) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.getArea(
                tokData.getString("id_C"),
                json.getString("ref"));
    }

//    @SecurityParameter
//    @PostMapping("/v1/getRack")
//    public ApiResponse getRack(@RequestBody JSONObject json) {
//        return storageService.getRack(json.getString("id_C"), json.getString("ref"), json.getString("refArea"));
//    }

    @SecurityParameter
    @PostMapping("/v1/getLocByRef")
    public ApiResponse getLocByRef(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.getLocByRef(
                tokData.getString("id_C"),
                json.getString("locAddr"));
    }

    @SecurityParameter
    @PostMapping("/v1/getLocByRefEmpty")
    public ApiResponse getLocByRefEmpty(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.getLocByRefEmpty(
                tokData.getString("id_C"),
                json.getString("locAddr"));
    }

    @SecurityParameter
    @PostMapping("/v1/getLocName")
    public ApiResponse getLocName(@RequestBody JSONObject json) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.getLocName(
                tokData.getString("id_C")
//                json.getString("id_C")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/moveAsset")
    public ApiResponse moveAsset(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.moveAsset(
                tokData.getString("id_C"),
                json.getString("fromLocAddr"),
                json.getString("toLocAddr"),
                json.getJSONArray("fromLocSpace"),
                json.getJSONArray("toLocSpace"),
                json.getJSONArray("fromWn2qty"),
                json.getJSONArray("toWn2qty"));
    }

    @SecurityParameter
    @PostMapping("/v1/pushAsset")
    public ApiResponse pushAsset(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.pushAsset(
                tokData,
                json.getString("id_O"),
                json.getInteger("index"),
                json.getString("locAddr"),
                json.getJSONArray("locSpace"),
                json.getJSONArray("wn2qty")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/popAssetByLocation")
    public ApiResponse popAssetByLocation(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.popAssetByLocation(
                tokData,
                json.getString("id_P"),
                json.getString("id_O"),
                json.getInteger("index"),
                json.getString("locAddr"),
                json.getJSONArray("locSpace"),
                json.getJSONArray("wn2qty")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/popAssetById_A")
    public ApiResponse popAssetById_A(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.popAssetById_A(
                tokData,
                json.getString("id_P"),
                json.getString("id_O"),
                json.getInteger("index"),
                json.getString("id_A"),
                json.getJSONArray("locSpace"),
                json.getJSONArray("wn2qty"),
                json.getBoolean("isResv")
        );
    }

//    @SecurityParameter
//    @PostMapping("/v1/up_locSetup")
//    public ApiResponse up_locSetup(@RequestBody JSONObject json) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
//        return storageService.up_locSetup(
//                tokData.getString("id_U"),
//                tokData.getString("id_C"),
//                json.getString("id_A"),
//                json.getJSONObject("locSetup"),
//                json.getInteger("tvs")
//        );
//    }


    @SecurityParameter
    @PostMapping("/v1/producedMax")
    public ApiResponse producedMax(@RequestBody JSONObject json) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.producedMax(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                json.getString("id_O"),
                json.getInteger("index")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/producedNow")
    public ApiResponse producedNow(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.producedNow(
               tokData,
                json.getString("id_O"),
                json.getInteger("index"),
                json.getDouble("wn2qtynow")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/updateOStock")
    public ApiResponse updateOStock(@RequestBody JSONObject json) {

        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        JSONArray arrTime = new JSONArray();
        return storageService.updateOStock(
                json.getString("id_O"),
                json.getInteger("index"),
                json.getDouble("wn2qtynow"),
                tokData,
                arrTime
        );
    }

    @PostMapping("/v1/updateOStockPi")
    public Integer updateOStockPi(@RequestBody JSONObject json) {

        return storageService.updateOStockPi(
                json.getString("id_C"),
                json.getString("id_O"),
                json.getInteger("index"),
                json.getDouble("wn2qtynow"),
                json.getString("dep"),
                json.getString("grpU"),
                json.getString("id_U"),
                json.getJSONObject("wrdNU"),
                json.getJSONArray("arrTime"));
    }



    @SecurityParameter
    @PostMapping("/v1/deductQty")
    public ApiResponse deductQty(@RequestBody JSONObject json) {

        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return storageService.deductQty(
                json.getString("id_O"),
                json.getInteger("index"),
                json.getDouble("wn2qtynow"),
                json.getString("id_UW"),
                tokData
        );
    }

    @SecurityParameter
    @PostMapping("/v1/shipNow")
    public ApiResponse shipNow(@RequestBody JSONObject json) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return storageService.shipNow(
                tokData,
                json.getString("id_O"),
                json.getInteger("index"),
                json.getBoolean("isLink"),
                json.getDouble("qtyShip"),
                json.getInteger("prntIndex")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/inventory")
    public Object inventory(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.inventory(
                tokData.getString("id_C"),
                json.getString("locAddr"),
                json.getJSONArray("arrayLoc")
        );
    }


    @SecurityParameter
    @PostMapping("/v1/getFromStock")
    public Object getFromStock(@RequestBody JSONObject json) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return storageService.getFromStock(
                tokData.getString("id_C"),
                json.getString("id_O")
        );
    }

//
//    @PostMapping("/v1/beantest")
//    public Object beantest(@RequestBody JSONObject json) throws IOException {
//        JSONObject jsonQuery = new JSONObject();
//        jsonQuery.put("id_C", json.getString("id_C"));
//        jsonQuery.put("locAddr.keyword", json.getString("locAddr"));
//        SearchResponse response = dbUtils.getEsKeys(jsonQuery, "lSAsset");
//        SearchHit[] hits = response.getHits().getHits();
//        for (SearchHit hit : hits) {
//            lSAsset lsasset = JSON.parseObject(hit.getSourceAsString(), lSAsset.class);
//            System.out.println("lSAsset=" + lSAsset);
//        }
//        return null;
//    }
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @PostMapping("/v1/batchtest")
//    public Object batchtest(@RequestBody JSONObject json) {
//        String locAddr = json.getString("locAddr");
//        Query query = new Query(new Criteria("aStock.locAddr").is(locAddr));
//        List<Asset> assets = mongoTemplate.find(query, Asset.class);
//        for (Asset asset : assets) {
//
//        }
//        return null;
//    }

//    @PostMapping("/v1/bulktest")
//    public Object bulktest(@RequestBody JSONObject json) {
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//
//    }
}
