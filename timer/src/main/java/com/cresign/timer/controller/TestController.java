//package com.cresign.timer.controller;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.timer.service.ScriptTimer;
////import com.cresign.timer.utils.ElasticsearchUtils;
////import com.cresign.timer.utils.GenericUtils;
//import com.cresign.tools.annotation.SecurityParameter;
//import com.cresign.tools.pojo.po.Asset;
//import com.cresign.tools.pojo.po.InitJava;
//import com.cresign.tools.token.GetUserIdByToken;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.core.AcknowledgedResponse;
//import org.elasticsearch.client.indices.CreateIndexResponse;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.reindex.BulkByScrollResponse;
//import org.elasticsearch.index.reindex.DeleteByQueryRequest;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.script.ScriptException;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.util.*;
//
//@RestController
//@RequestMapping("/test")
//public class TestController {
//
//    @Autowired
//    private ScriptTimer scriptTimer;
//
//    @Autowired
//    private GetUserIdByToken getUserIdByToken;
//
//    @Autowired
//    private HttpServletRequest request;
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @Autowired
//    private RestHighLevelClient restHighLevelClient;
//
////    @Autowired
////    private GenericUtils genericUtils;
////
////    @Autowired
////    private ElasticsearchUtils elasticsearchUtils;
//
////    @Autowired
////    private DetailsClient detailsClient;
//
//    @PostMapping("/rachelOne")
//    public Object rachelOne(@RequestBody JSONObject json) {
//        String ref = json.getString("ref");
//        String grp = json.getString("grp");
//        JSONObject jsonWrdN = json.getJSONObject("wrdN");
//        System.out.println("jsonWrdN=" + jsonWrdN);
//        JSONObject jsonResult = new JSONObject();
//        jsonResult.put(ref + grp, jsonWrdN.getString("cn"));
//        return jsonResult;
//    }
//
//    @SecurityParameter
//    @PostMapping("/rachelTwo")
//    public Object rachelTwo(@RequestBody JSONObject json) {
//        String ref = json.getString("ref");
//        String grp = json.getString("grp");
//        JSONObject jsonWrdN = json.getJSONObject("wrdN");
//        System.out.println("jsonWrdN=" + jsonWrdN);
//        JSONObject jsonResult = new JSONObject();
//        jsonResult.put(grp + ref, jsonWrdN.getString("cn"));
//        return jsonResult;
//    }
//
//
////    @SecurityParameter
////    @PostMapping("/v1/getStatistic")
////    public ApiResponse getStatistic(@RequestBody JSONObject json) throws IOException, ParseException {
////        return statService.getStatistic(
////                json.getString("id_C"),
////                json.getString("startDate"),
////                json.getString("endDate"),
////                json.getString("subType"),
////                json.getInteger("second"),
////                json.getString("id_A"),
////                json.getJSONArray("excelField"),
////                json.getString("outputType"),
////                json.getString("fileName"),
////                json.getString("logType"),
////                json.getJSONArray("statField")
////        );
////    }
////
////    @SecurityParameter
////    @PostMapping("/v1/getStatArrayByEs")
////    public JSONArray getStatArrayByEs(@RequestBody JSONObject json) throws IOException, ParseException {
////        return statService.getStatArrayByEs(
////                json.getJSONObject("termField"),
////                json.getJSONObject("rangeField"),
////                json.getJSONObject("second"),
////                json.getJSONArray("excelField"),
////                json.getJSONArray("statField"),
////                json.getString("logType")
////        );
////    }
////
////    @SecurityParameter
////    @PostMapping("/v1/getStatValueByEs")
////    public Object getStatValueByEs(@RequestBody JSONObject json) throws IOException {
////        return statService.getStatValueByEs(
////                json.getJSONObject("termField"),
////                json.getJSONObject("rangeField"),
////                json.getJSONObject("statField"),
////                json.getString("logType")
////        );
////    }
//
//    @PostMapping("/cTrigger")
//    public Object cTrigger(@RequestBody JSONObject json) throws IOException, ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        String time = json.getString("time");
////        SimpleDateFormat sdf = new SimpleDateFormat("Z");
////        String format = sdf.format(new Date());
////        String timeZone = null;
////        if (format.startsWith("+")) {
////            timeZone = "-" + format.substring(1, 3);
////        } else {
////            timeZone = "+" + format.substring(1, 3);
////        }
////        System.out.println("format=" + format + "," + timeZone);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date(time));
////        calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(timeZone));
//        System.out.println("日期=" + calendar.getTime());
//        //月要加1
//        System.out.println("月=" + (calendar.get(Calendar.MONTH) + 1));
//        //周从周日开始，周日1，周六7
//        System.out.println("周=" + calendar.get(Calendar.DAY_OF_WEEK));
//        System.out.println("日=" + calendar.get(Calendar.DAY_OF_MONTH));
//        System.out.println("时=" + calendar.get(Calendar.HOUR_OF_DAY));
//        System.out.println("分=" + calendar.get(Calendar.MINUTE));
//
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder monthQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder weekQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder dayQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder hourQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder minuteQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder monthExistsQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder weekExistsQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder dayExistsQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder hourExistsQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder minuteExistsQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder timeExistsQueryBuilder = new BoolQueryBuilder();
//        monthExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.month"));
//        weekExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.week"));
//        dayExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.day"));
//        hourExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.hour"));
//        minuteExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.minute"));
//        timeExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.time"));
//        monthQueryBuilder.should(QueryBuilders.termQuery("data.month", calendar.get(Calendar.MONTH) + 1)).should(monthExistsQueryBuilder);
//        weekQueryBuilder.should(QueryBuilders.termQuery("data.week", calendar.get(Calendar.DAY_OF_WEEK))).should(weekExistsQueryBuilder);
//        dayQueryBuilder.should(QueryBuilders.termQuery("data.day", calendar.get(Calendar.DAY_OF_MONTH))).should(dayExistsQueryBuilder);
//        hourQueryBuilder.should(QueryBuilders.termQuery("data.hour", calendar.get(Calendar.HOUR_OF_DAY))).should(hourExistsQueryBuilder);
//        minuteQueryBuilder.should(QueryBuilders.termQuery("data.minute", calendar.get(Calendar.MINUTE))).should(minuteExistsQueryBuilder);
//        queryBuilder.must(monthQueryBuilder).must(weekQueryBuilder).must(dayQueryBuilder).must(hourQueryBuilder).must(minuteQueryBuilder)
//                .must(timeExistsQueryBuilder).must(QueryBuilders.termQuery("subType", "timer"));
//        sourceBuilder.query(queryBuilder).from(0).size(10000);
//        SearchRequest searchRequest = new SearchRequest("timeflow").source(sourceBuilder);
//        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        System.out.println("searchResponse=" + searchResponse);
//        SearchHit[] hits = searchResponse.getHits().getHits();
//        System.out.println("hits=" + hits);
//        long start = System.currentTimeMillis();
//        HashSet setId_A = new HashSet();
//        JSONObject jsonId_A = new JSONObject();
//        System.out.println("length=" + hits.length);
//        for (int i = 0; i < hits.length; i++) {
//            JSONObject jsonHit = (JSONObject) JSON.parse(hits[i].getSourceAsString());
//            String id_A = jsonHit.getString("id_A");
//            String ref = jsonHit.getJSONObject("data").getString("ref");
//            System.out.println("ref=" + ref);
//            if (jsonId_A.getJSONArray(id_A) == null) {
//                JSONArray arrayRef = new JSONArray();
//                arrayRef.add(ref);
//                jsonId_A.put(id_A, arrayRef);
//            } else {
//                JSONArray arrayRef = jsonId_A.getJSONArray(id_A);
//                arrayRef.add(ref);
//                jsonId_A.put(id_A, arrayRef);
//            }
//            setId_A.add(id_A);
//        }
//        System.out.println("setId_A=" + setId_A);
//        System.out.println("jsonId_A=" + jsonId_A);
//
//        Query queryAsset = new Query(new Criteria("_id").in(setId_A));
//        List<Asset> assets = mongoTemplate.find(queryAsset, Asset.class);
//        JSONArray arrayTrigger = new JSONArray();
//        for (int i = 0; i < assets.size(); i++) {
//            JSONObject jsonTrigger = new JSONObject();
//            Asset asset = assets.get(i);
//            jsonTrigger.put("id_A", asset.getId());
//            JSONObject jsonObjData = asset.getCTrigger().getJSONObject("objData");
//            jsonTrigger.putAll(jsonObjData);
//            jsonTrigger.remove("objInfo");
//            JSONObject jsonObjIf = jsonObjData.getJSONObject("objIf");
//            JSONArray arrayIf = jsonId_A.getJSONArray(asset.getId());
//            JSONArray arrayObjIf = new JSONArray();
//            for (int j = 0; j < arrayIf.size(); j++) {
//                JSONObject jsonIf = jsonObjIf.getJSONObject(arrayIf.getString(j));
//                jsonIf.put("ref", arrayIf.getString(j));
//                arrayObjIf.add(jsonIf);
//            }
//            jsonTrigger.put("objIf", arrayObjIf);
//            arrayTrigger.add(jsonTrigger);
//        }
//        System.out.println("arrayTrigger=" + arrayTrigger);
////        for (int i = 0; i < assets.size(); i++) {
////            JSONArray arrayRef = jsonId_A.getJSONArray(assets.get(i).getId());
////            for (int j = 0; j < arrayRef.size(); j++) {
////                String ref = arrayRef.getString(j);
////                JSONObject jsonTrigger = assets.get(i).getCTrigger().getJSONObject("objData").getJSONObject(ref);
////                if (jsonTrigger.getBoolean("isActive") != null) {
////                    if (jsonTrigger.getBoolean("isActive")) {
////                        jsonTrigger.put("id", assets.get(i).getId());
////                        jsonTrigger.put("table", "Asset");
////                        jsonTrigger.put("key", "cTrigger.objData." + ref);
////                        arrayTrigger.add(jsonTrigger);
////                    }
////                } else {
////                    arrayTrigger.add(jsonTrigger);
////                }
////            }
////        }
//        long end = System.currentTimeMillis();
//        System.out.println("time=" + (end - start) + "ms");
////        Object o = scriptTimer.scriptEngine(arrayTrigger);
////        System.out.println("o=" + o);
//        return arrayTrigger;
//    }
//
////    @PostMapping("log")
////    public Object log(@RequestBody JSONObject json) throws IOException, ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
////        String time = json.getString("time");
////        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
////        Calendar calendar = Calendar.getInstance();
////        Date date = new Date(time);
////        String endTime = sdf.format(date);
////        calendar.setTime(date);
////        calendar.add(Calendar.SECOND, - 10);
////        Date dateTime = calendar.getTime();
////        String startTime = sdf.format(dateTime);
////        System.out.println("过去十秒：" + startTime + "~" + endTime);
////        SearchSourceBuilder builderLog = new SearchSourceBuilder();
////        BoolQueryBuilder queryLog = QueryBuilders.boolQuery();
////        queryLog.must(QueryBuilders.rangeQuery("tmd").from(startTime, true).to(endTime, false)).must(QueryBuilders.termQuery("subType", "qtyChg"));
////        builderLog.query(queryLog).from(0).size(10000);
////        SearchRequest assetflowRequest = new SearchRequest("assetflow").source(builderLog);
////        SearchResponse assetflowResponse = restHighLevelClient.search(assetflowRequest, RequestOptions.DEFAULT);
////        System.out.println("assetflowResponse=" + assetflowResponse);
////        SearchHit[] assetflowHits = assetflowResponse.getHits().getHits();
////        System.out.println("length=" + assetflowHits.length);
////        if (assetflowHits.length > 0) {
////
////            HashSet setId_O = new HashSet();
////            JSONObject jsonId_O = new JSONObject();
////            for (int i = 0; i < assetflowHits.length; i++) {
////                JSONObject jsonHit = (JSONObject) JSON.parse(assetflowHits[i].getSourceAsString());
////                String id_O = jsonHit.getString("id_O");
////                Integer index = jsonHit.getInteger("index");
////                String id_P = jsonHit.getString("id_P");
////                Double wn2qtynow = jsonHit.getJSONObject("data").getDouble("wn2qtynow");
////                setId_O.add(id_O);
////                System.out.println("");
////                System.out.println("jsonId_O=" + jsonId_O);
////                if (jsonId_O.getJSONArray(id_O) == null) {
////                    System.out.println("index=" + index);
////                    System.out.println("==1==");
////                    JSONArray arrayOStock = new JSONArray();
////                    JSONObject jsonOStock = new JSONObject();
////                    jsonOStock.put("id_P", id_P);
////                    jsonOStock.put("wn2qtynow", wn2qtynow);
////                    jsonOStock.put("wn2qtymade", wn2qtynow);
////                    arrayOStock.set(index, jsonOStock);
////                    System.out.println("arrayOStock=" + arrayOStock);
////                    jsonId_O.put(id_O, arrayOStock);
////                } else {
////                    JSONArray arrayOStock = jsonId_O.getJSONArray(id_O);
////                    System.out.println("size=" + arrayOStock.size() + ",index=" + index);
////                    if (arrayOStock.size() > index && arrayOStock.getJSONObject(index) != null) {
////                        System.out.println("==2==");
////                        JSONObject jsonOStock = arrayOStock.getJSONObject(index);
////                        jsonOStock.put("wn2qtynow", jsonOStock.getDouble("wn2qtynow") + wn2qtynow);
////                        jsonOStock.put("wn2qtymade", jsonOStock.getDouble("wn2qtymade") + wn2qtynow);
////                        arrayOStock.set(index, jsonOStock);
////                        System.out.println("arrayOStock=" + arrayOStock);
////                    } else {
////                        System.out.println("==3==");
////                        JSONObject jsonOStock = new JSONObject();
////                        jsonOStock.put("id_P", id_P);
////                        jsonOStock.put("wn2qtynow", wn2qtynow);
////                        jsonOStock.put("wn2qtymade", wn2qtynow);
////                        arrayOStock.set(index, jsonOStock);
////                        System.out.println("arrayOStock=" + arrayOStock);
////                    }
////                    jsonId_O.put(id_O, arrayOStock);
////                }
////            }
////            System.out.println("setId_O=" + setId_O);
////            System.out.println("jsonId_O=" + jsonId_O);
////
////            JSONArray arrayTrigger = new JSONArray();
////            Query queryOrder = new Query(new Criteria("_id").in(setId_O));
////            List<Order> orders = mongoTemplate.find(queryOrder, Order.class);
////            for (int i = 0; i < orders.size(); i++) {
////                Order order = orders.get(i);
////                if (order.getOTrigger() != null) {
////                    JSONArray arrayObjAssetflow = order.getOTrigger().getJSONArray("objAssetflow");
////                    if (arrayObjAssetflow != null) {
////                        for (int j = 0; j < arrayObjAssetflow.size(); j++) {
////                            JSONObject jsonObjAsset = arrayObjAssetflow.getJSONObject(j);
////                            if (jsonObjAsset.getBoolean("isActive") != null) {
////                                if (jsonObjAsset.getBoolean("isActive")) {
////                                    jsonObjAsset.put("id", order.getId());
////                                    jsonObjAsset.put("table", "Order");
////                                    jsonObjAsset.put("key", "oTrigger.objAssetflow." + j);
////                                    arrayTrigger.add(jsonObjAsset);
////                                }
////                            } else {
////                                arrayTrigger.add(jsonObjAsset);
////                            }
////                        }
////                    }
////                }
////                JSONArray arrayOStock = jsonId_O.getJSONArray(order.getId());
////                System.out.println("arrayOStock=" + arrayOStock);
////            }
////            System.out.println("arrayTrigger=" + arrayTrigger);
//////            Object result = statService.scriptEngine(arrayTrigger, jsonId_O);
//////            System.out.println("result=" + result);
////            return jsonId_O;
////        }
////        return null;
////    }
//
////    @PostMapping("oTrigger")
////    public Object oTrigger(@RequestBody JSONObject json) throws IOException, ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
////        String time = json.getString("time");
////        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
////        Calendar calendar = Calendar.getInstance();
////        Date date = new Date(time);
////        String endTime = sdf.format(date);
////        calendar.setTime(date);
////        calendar.add(Calendar.SECOND, - 10);
////        Date dateTime = calendar.getTime();
////        String startTime = sdf.format(dateTime);
////        System.out.println("过去十秒：" + startTime + "~" + endTime);
////        SearchSourceBuilder builderLog = new SearchSourceBuilder();
////        BoolQueryBuilder queryLog = QueryBuilders.boolQuery();
////        queryLog.must(QueryBuilders.rangeQuery("tmd").from(startTime, true).to(endTime, false)).must(QueryBuilders.termQuery("subType", "qtyChg"));
////        builderLog.query(queryLog).from(0).size(10000);
////        SearchRequest searchRequest = new SearchRequest("assetflow").source(builderLog);
////        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
////        System.out.println("searchResponse=" + searchResponse);
////        SearchHit[] hits = searchResponse.getHits().getHits();
////        System.out.println("length=" + hits.length);
////        if (hits.length > 0) {
////            HashSet setId_O = new HashSet();
////            JSONObject jsonId_O = new JSONObject();
////            for (int i = 0; i < hits.length; i++) {
////                JSONObject jsonHit = (JSONObject) JSON.parse(hits[i].getSourceAsString());
////                String id_O = jsonHit.getString("id_O");
////                Integer index = jsonHit.getInteger("index");
////                String id_P = jsonHit.getString("id_P");
////                Double wn2qtynow = jsonHit.getJSONObject("data").getDouble("wn2qtynow");
////                setId_O.add(id_O);
////                System.out.println("");
////                System.out.println("jsonId_O=" + jsonId_O);
////                if (jsonId_O.getJSONArray(id_O) == null) {
////                    System.out.println("index=" + index);
////                    System.out.println("==1==");
////                    JSONArray arrayOStock = new JSONArray();
////                    arrayOStock.set(index, wn2qtynow);
////                    jsonId_O.put(id_O, arrayOStock);
////                } else {
////                    JSONArray arrayOStock = jsonId_O.getJSONArray(id_O);
////                    System.out.println("size=" + arrayOStock.size() + ",index=" + index);
////                    if (arrayOStock.size() > index && arrayOStock.getDouble(index) != null) {
////                        System.out.println("==2==");
////                        arrayOStock.set(index, arrayOStock.getDouble(index) + wn2qtynow);
////                        System.out.println("arrayOStock=" + arrayOStock);
////                    } else {
////                        System.out.println("==3==");
////                        arrayOStock.set(index, wn2qtynow);
////                        System.out.println("arrayOStock=" + arrayOStock);
////                    }
////                    jsonId_O.put(id_O, arrayOStock);
////                }
////            }
////            System.out.println("setId_O=" + setId_O);
////            System.out.println("jsonId_O=" + jsonId_O);
////            Query queryOrder = new Query(new Criteria("_id").in(setId_O));
////            List<Order> orders = mongoTemplate.find(queryOrder, Order.class);
////            JSONArray arrayTrigger = new JSONArray();
////            for (int i = 0; i < orders.size(); i++) {
////                Order order = orders.get(i);
////                JSONObject jsonTrigger = new JSONObject();
////                jsonTrigger.put("id_O", order.getId());
////                jsonTrigger.putAll(order.getOTrigger().getJSONObject("objAssetflow"));
////                jsonTrigger.put("objGlobal", order.getOTrigger().getJSONObject("objGlobal"));
////                jsonTrigger.put("log", "objAssetflow");
////                arrayTrigger.add(jsonTrigger);
////            }
////            System.out.println("arrayTrigger=" + arrayTrigger);
////            Object result = statService.scriptEngine(arrayTrigger);
////            System.out.println("result=" + result);
////            JSONArray array = new JSONArray();
////            array.add(arrayTrigger);
////            array.add(jsonId_O);
////            return array;
////        }
////        return null;
////    }
//
//
//    @PostMapping("chkin")
//    public Object chkin(@RequestBody JSONObject json) throws IOException {
//        genericUtils.addES(json, "chkin");
//        return true;
//    }
//
////    @RequestMapping("clockIn")
////    public Object clockIn(@RequestBody JSONObject json) throws IOException, ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
////        String params = json.getString("date");
////        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
////        Calendar calendar = Calendar.getInstance();
////        Date date = new Date(params);
////        System.out.println("date=" + date);
////        calendar.setTime(date);
////        calendar.add(Calendar.HOUR, - 1);
////        System.out.println("date=" + date);
////        System.out.println("calendar=" + calendar.getTime());
////        String time = sdf.format(calendar.getTime());
////        String startTime = time + " 00:00:00";
////        String endTime = time + " 23:59:59";
////        System.out.println("时间：" +startTime +"~" + endTime);
////        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
////        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
////        queryBuilder.must(QueryBuilders.rangeQuery("tmd").from(startTime, true).to(endTime, true))
////                .must(QueryBuilders.termQuery("subType.keyword", "statusChg"));
////        sourceBuilder.query(queryBuilder).size(0).aggregation(AggregationBuilders.terms("id_C").field("id_C")
////                .subAggregation(AggregationBuilders.terms("id_UC").field("id_UC")
////                .subAggregation(AggregationBuilders.terms("tmd").field("tmd"))));
////        SearchRequest searchRequest = new SearchRequest("chkin").source(sourceBuilder);
////        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
////        System.out.println("searchResponse=" + searchResponse);
////        JSONObject jsonResponse = (JSONObject) JSON.parse(searchResponse.toString());
////        JSONArray arrayId_C = jsonResponse.getJSONObject("aggregations").getJSONObject("sterms#id_C").getJSONArray("buckets");
////        JSONObject jsonChkin = new JSONObject();
////        JSONArray arrayTrigger = new JSONArray();
////        for (int i = 0; i < arrayId_C.size(); i++) {
////            JSONObject jsonId_C = arrayId_C.getJSONObject(i);
////            String id_C = jsonId_C.getString("key");
////            JSONArray arrayId_UC = jsonId_C.getJSONObject("sterms#id_UC").getJSONArray("buckets");
////            for (int j = 0; j < arrayId_UC.size(); j++) {
////                JSONObject jsonId_UC = arrayId_UC.getJSONObject(j);
////                String id_UC = jsonId_UC.getString("key");
////                Query queryOrder = new Query(new Criteria("info.id_C").is(id_UC).and("info.id_CB").is(id_C));
////                Order order = mongoTemplate.findOne(queryOrder, Order.class);
////                System.out.println("order=" + order);
////                JSONObject jsonTrigger = new JSONObject();
////                jsonTrigger.put("id_O", order.getId());
////                jsonTrigger.putAll(order.getOTrigger().getJSONObject("objChkin"));
////                jsonTrigger.put("objGlobal", order.getOTrigger().getJSONObject("objGlobal"));
////                jsonTrigger.put("log", "objChkin");
////                arrayTrigger.add(jsonTrigger);
////                JSONArray arrayTmd = jsonId_UC.getJSONObject("lterms#tmd").getJSONArray("buckets");
////                JSONArray arrayChkin = new JSONArray();
////                for (int k = 0; k < arrayTmd.size(); k++) {
////                    arrayChkin.add(arrayTmd.getJSONObject(k).getString("key_as_string"));
////                }
////                jsonChkin.put(order.getId(), arrayChkin);
////            }
////        }
////        Object o = statService.scriptEngine(arrayTrigger);
////        JSONArray array = new JSONArray();
////        array.add(arrayTrigger);
////        array.add(jsonChkin);
////        return array;
////    }
//
////    @PostMapping("timeflow")
////    public Object timeflow(@RequestBody JSONObject json) throws IOException {
////        String time = json.getString("time");
////        SimpleDateFormat sdf = new SimpleDateFormat("Z");
////        String format = sdf.format(new Date());
////        String timeZone = null;
////        if (format.startsWith("+")) {
////            timeZone = "-" + format.substring(1, 3);
////        } else {
////            timeZone = "+" + format.substring(1, 3);
////        }
////        System.out.println("format=" + format + "," + timeZone);
////        Calendar calendar = Calendar.getInstance();
////        calendar.setTime(new Date(time));
////        calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(timeZone));
////        System.out.println("日期=" + calendar.getTime());
////        //月要加1
////        System.out.println("月=" + (calendar.get(Calendar.MONTH) + 1));
////        //周从周日开始，周日1，周六7
////        System.out.println("周=" + calendar.get(Calendar.DAY_OF_WEEK));
////        System.out.println("日=" + calendar.get(Calendar.DAY_OF_MONTH));
////        System.out.println("时=" + calendar.get(Calendar.HOUR_OF_DAY));
////        System.out.println("分=" + calendar.get(Calendar.MINUTE));
////
////        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
////        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder monthQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder weekQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder dayQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder hourQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder minuteQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder monthExistsQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder weekExistsQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder dayExistsQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder hourExistsQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder minuteExistsQueryBuilder = new BoolQueryBuilder();
////        BoolQueryBuilder timeExistsQueryBuilder = new BoolQueryBuilder();
////        monthExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.month"));
////        weekExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.week"));
////        dayExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.day"));
////        hourExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.hour"));
////        minuteExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.minute"));
////        timeExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.time"));
////        monthQueryBuilder.should(QueryBuilders.termQuery("data.month", calendar.get(Calendar.MONTH) + 1)).should(monthExistsQueryBuilder);
////        weekQueryBuilder.should(QueryBuilders.termQuery("data.week", calendar.get(Calendar.DAY_OF_WEEK))).should(weekExistsQueryBuilder);
////        dayQueryBuilder.should(QueryBuilders.termQuery("data.day", calendar.get(Calendar.DAY_OF_MONTH))).should(dayExistsQueryBuilder);
////        hourQueryBuilder.should(QueryBuilders.termQuery("data.hour", calendar.get(Calendar.HOUR_OF_DAY))).should(hourExistsQueryBuilder);
////        minuteQueryBuilder.should(QueryBuilders.termQuery("data.minute", calendar.get(Calendar.MINUTE))).should(minuteExistsQueryBuilder);
////        queryBuilder.must(monthQueryBuilder).must(weekQueryBuilder).must(dayQueryBuilder).must(hourQueryBuilder).must(minuteQueryBuilder)
////                .must(timeExistsQueryBuilder).must(QueryBuilders.termQuery("subType", "timer"));
////        sourceBuilder.query(queryBuilder).from(0).size(10000);
////        SearchRequest searchRequest = new SearchRequest("timeflow").source(sourceBuilder);
////        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
////        System.out.println("searchResponse=" + searchResponse);
////        SearchHit[] hits = searchResponse.getHits().getHits();
////        System.out.println("hits=" + hits);
////        return searchResponse;
////    }
//
//    @PostMapping("deltime")
//    public Object deltime(@RequestBody JSONObject json) throws IOException {
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//        queryBuilder.mustNot(QueryBuilders.existsQuery("data.month"));
//        sourceBuilder.query(queryBuilder).from(0).size(1000);
////        SearchRequest searchRequest = new SearchRequest("timeflow").source(sourceBuilder);
////        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("timeflow").setQuery(queryBuilder);
//        BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
//        return bulkByScrollResponse;
//    }
//
//    @PostMapping("testFun")
//    public Object testFun(@RequestBody JSONObject json) {
//        String id_O = json.getString("id_O");
//        Integer index = json.getInteger("index");
//        Double wn2qtynow = json.getDouble("wn2qtynow");
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("id_O", id_O);
//        jsonObject.put("index", index);
//        jsonObject.put("wn2qtynow", wn2qtynow * 2);
//        return jsonObject;
//    }
//
//    @PostMapping("mapping")
//    public Object mapping(@RequestBody JSONObject json) throws IOException {
//        String logType = json.getString("logType");
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//        sourceBuilder.query(queryBuilder);
//        SearchRequest searchRequest = new SearchRequest(logType).source(sourceBuilder);
//        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        JSONObject jsonResponse = (JSONObject) JSON.parse(searchResponse.toString());
//        return jsonResponse;
//    }
//
//    @PostMapping("addIndexByYear")
//    public Object addIndexByYear(@RequestBody JSONObject json) throws IOException {
//        Query queryInit = new Query(new Criteria("_id").is("cn_java"));
//        queryInit.fields().include("flowInit");
//        InitJava initJava = mongoTemplate.findOne(queryInit, InitJava.class);
//        JSONObject jsonMapping = initJava.getFlowInit();
//        for (Map.Entry<String, Object> entry : jsonMapping.entrySet()) {
//            String indexName = entry.getKey();
//            JSONObject mapping = (JSONObject) entry.getValue();
//            CreateIndexResponse createIndexResponse = dateUtil.addIndex(indexName, mapping);
//            if (createIndexResponse.isAcknowledged() && createIndexResponse.isShardsAcknowledged()) {
//                Calendar calendar = Calendar.getInstance();
//                calendar.add(Calendar.YEAR, -1);
//                AcknowledgedResponse acknowledgedResponse = elasticsearchUtils.deleteAlias(indexName + "-" + calendar.get(Calendar.YEAR), indexName + "-write");
//                if (acknowledgedResponse.isAcknowledged()) {
//                    calendar.add(Calendar.YEAR, -1);
//                    acknowledgedResponse = elasticsearchUtils.deleteAlias(indexName + "-" + calendar.get(Calendar.YEAR), indexName + "-read");
//                }
//            }
//        }
//        return true;
//    }
//
////    @PostMapping("chkintest")
////    public Object chkintest(@RequestBody JSONObject json) {
////        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
////        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
////        sourceBuilder.query(queryBuilder).aggregation(AggregationBuilders.terms("id_C").field("id_C")
////                .subAggregation());
////    }
//}
