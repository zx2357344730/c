package com.cresign.timer.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.enumeration.TimerEnum;
import com.cresign.timer.service.StatService;
import com.cresign.timer.utils.AsyncUtils;
import com.cresign.timer.utils.CosUpload;
import com.cresign.timer.utils.ExcelUtils;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
//import com.cresign.tools.date.DateUtils;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.RedisUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.logger.LogUtil;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.po.*;
import com.cresign.tools.pojo.po.assetCard.AssetInfo;
import com.mongodb.client.result.UpdateResult;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class StatServiceImpl implements StatService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private RetResult retResult;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private AsyncUtils asyncUtils;

    @Override
    public ApiResponse getStatistic(String id_C, String startDate, String endDate, String subType, Integer second, String id_A, JSONArray excelField, String outputType, String fileName, String logType, JSONArray statField) throws IOException, ParseException {
       //Must have excelField, must have at least 1 field
        //subType is kinda useless becase you already build in qtychg
        // Very complete implementation

        if (excelField.size() == 0) {
            throw new ErrorResponseException(HttpStatus.OK,TimerEnum.EXCELFIELD_IS_NULL.getCode(), "");
        }
        System.out.println("fileNameLength=" + fileName.length());
        if (outputType.equals("excel") && fileName.length() < 3) {
            throw new ErrorResponseException(HttpStatus.OK,TimerEnum.FILENAME_LENGTH_LT.getCode(), "");
        }
        //秒数不为0，根据日期分组
        if (second != 0) {
            excelField.add("tmd");
        }
        long getstart = System.currentTimeMillis();

        //Here I already Got the statistic from MongoDB
        JSONObject jsonMongo = this.getStatisticByMongo(id_C, startDate, endDate, second, excelField);
        long getend = System.currentTimeMillis();
        JSONArray arrayField = new JSONArray();
        for (int i = 0; i < excelField.size(); i++) {
            String eField = excelField.getJSONObject(i).getString("field");
            String eReplaceField = eField.replace(".", "");
            arrayField.add(eReplaceField);
        }
        for (int i = 0; i < statField.size(); i++) {
            String sField = statField.getJSONObject(i).getString("field");
            String sReplaceField = sField.replace(".", "");
            arrayField.add(sReplaceField);
        }
        //获取首行数据
        Query queryInit = new Query(new Criteria("_id").is("cn_java"));
        queryInit.fields().include("statInit");
        // Here I need to go to cn_java to use the statInit field
        InitJava initJava = mongoTemplate.findOne(queryInit, InitJava.class);
        JSONObject statInit = initJava.getStatInit();

        System.out.println("statInit=" + statInit);
        JSONArray arrayStatistic = new JSONArray();
        JSONArray arrayBuckets = new JSONArray();
        JSONArray arrayStatField = new JSONArray();
        int arrayStatisticSize = 1;
        //If nothing in it, make query for all dates
        if (jsonMongo == null) {
            System.out.println("==mongo null==");
            long querystart = System.currentTimeMillis();
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            queryBuilder.must(QueryBuilders.termQuery("id_C", id_C))
                    .must(QueryBuilders.rangeQuery("tmd").from(startDate, true).to(endDate, false))
                    .must(QueryBuilders.termQuery("subType", "qtyChg"));
            //fields倒序
            Collections.reverse(excelField);
            Boolean oBool = false;
            Boolean pBool = false;
            Object aggregationBuilders = null;
            //根据field构建分组查询语句
            for (int i = 0; i < excelField.size(); i++) {
                String field = excelField.getString(i);
                //KEV why?
                switch (field) {
                    case "id_O":
                        oBool = true;
                        break;
                    case "id_P":
                        pBool = true;
                        break;
                }
                if (i == 0) {
                    if (field.equals("tmd")) {
                        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(field).field(field)
                                .fixedInterval(DateHistogramInterval.seconds(second));
                        for (int j = 0; j < statField.size(); j++) {
                            String strField = statField.getString(j);
                            String replaceField = strField.replace(".", "");
                            arrayStatField.add(replaceField);
                            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.sum(replaceField).field(strField));
                        }
                        aggregationBuilders = dateHistogramAggregationBuilder;
                    } else {
                        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(field).field(field);
                        for (int j = 0; j < statField.size(); j++) {
                            String strField = statField.getString(j);
                            String replaceField = strField.replace(".", "");
                            arrayStatField.add(replaceField);
                            termsAggregationBuilder = termsAggregationBuilder.subAggregation(AggregationBuilders.sum(replaceField).field(strField));
                        }
                        aggregationBuilders = termsAggregationBuilder;
                    }
                } else {
                    TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(field).field(field);
                    aggregationBuilders = termsAggregationBuilder.subAggregation((AggregationBuilder) aggregationBuilders);
                }
            }
            arrayField.addAll(arrayStatField);
            sourceBuilder.query(queryBuilder).aggregation((AggregationBuilder) aggregationBuilders);
            SearchRequest searchRequest = new SearchRequest(logType).source(sourceBuilder);
//            long buildtime = System.currentTimeMillis();
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println("searchResponse=" + searchResponse);
//            long queryend = System.currentTimeMillis();
            JSONObject jsonResponse = (JSONObject) JSON.parse(searchResponse.toString());
//            JSONObject jsonResponse = getStatisticByEs(id_C, startDate, endDate, second, excelField, logType, statField);
            //field倒序
            Collections.reverse(excelField);
            arrayBuckets = jsonResponse.getJSONObject("aggregations").getJSONObject("sterms#" + excelField.getString(0)).getJSONArray("buckets");
            JSONObject jsonCell = new JSONObject();
//            if (oBool == true && pBool == true) {
//                arrayField.add("price");
////                arrayField.add("sum");
//            }
            //excel首行数据
            for (int i = 0; i < arrayField.size(); i++) {
                System.out.println("arrayField=" + arrayField.getString(i) + ", " + statInit.getJSONObject(arrayField.getString(i)));
                JSONObject cell = new JSONObject();
                cell.put("wrdN", statInit.getJSONObject(arrayField.getString(i)).getJSONObject("wrdN").getString("cn"));
                cell.put("type", statInit.getJSONObject(arrayField.getString(i)).getString("type"));
                jsonCell.put(arrayField.getString(i), cell);
            }
            arrayStatistic.add(jsonCell);
            System.out.println("arrayRow=" + arrayStatistic);

        }
        else {
            System.out.println("==mongo==");
            String type = jsonMongo.getString("type");
            SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
            String startTime;
            String endTime;
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
            //mongo的数据和需要的数据时间类型
            switch (type) {
                case "==":
                    //时间相等
                    arrayStatistic = jsonMongo.getJSONArray("array");
                    if (outputType.equals("card")) {
                        if (arrayStatistic.size() <= 1000) {
                            System.out.println();
                            Boolean bool = setStatisticByMongo(arrayStatistic, id_C, startDate, endDate, second, excelField, fileName);
                            return retResult.ok(CodeEnum.OK.getCode(), bool);
                        }
                        return null;
                    } else {
                        File file = ExcelUtils.statisticExcel(arrayStatistic, excelField, fileName);
                        String url = CosUpload.uploadFile(file, id_A);
                        return retResult.ok(CodeEnum.OK.getCode(), url);
                    }
                case ">":
                    //开始时间相同，结束时间超出
                    arrayStatistic = jsonMongo.getJSONArray("array");
                    endTime = sdf.format(new Date(jsonMongo.getLong("endTime")));
                    System.out.println("endTime=" + endTime);
                    queryBuilder.must(QueryBuilders.termQuery("id_C", id_C))
                            .must(QueryBuilders.rangeQuery("tmd").from(endTime, true).to(endDate, false))
                            .must(QueryBuilders.termQuery("subType", "qtyChg"));
                    break;
                case "<":
                    //开始时间超出，结束时间相同
                    arrayStatistic = jsonMongo.getJSONArray("array");
                    startTime = sdf.format(new Date(jsonMongo.getLong("startTime")));
                    System.out.println("startTime=" + startTime);
                    queryBuilder.must(QueryBuilders.termQuery("id_C", id_C))
                            .must(QueryBuilders.rangeQuery("tmd").from(startDate, true).to(startTime, false))
                            .must(QueryBuilders.termQuery("subType", "qtyChg"));
                    break;
                case "<>":
                    //开始时间和结束时间超出
                    arrayStatistic = jsonMongo.getJSONArray("array");
                    startTime = sdf.format(new Date(jsonMongo.getLong("startTime")));
                    endTime = sdf.format(new Date(jsonMongo.getLong("endTime")));
                    System.out.println("startTime=" + startTime);
                    System.out.println("endTime=" + endTime);
                    BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                    shouldQueryBuilder.should(QueryBuilders.rangeQuery("tmd").from(endTime, true).to(endDate, false))
                            .should(QueryBuilders.rangeQuery("tmd").from(startDate, true).to(startTime, false));
                    queryBuilder.must(QueryBuilders.termQuery("id_C", id_C)).must(shouldQueryBuilder)
                            .must(QueryBuilders.termQuery("subType", "qtyChg"));
                    break;
            }
            arrayStatisticSize = arrayStatistic.size();
            System.out.println("arrayStatisticSize=" + arrayStatisticSize);
            //fields倒序
            Collections.reverse(excelField);
            Boolean oBool = false;
            Boolean pBool = false;
            Object aggregationBuilders = null;
            //根据field构建分组查询语句
            for (int i = 0; i < excelField.size(); i++) {
                String field = excelField.getString(i);
                switch (field) {
                    case "id_O":
                        oBool = true;
                        break;
                    case "id_P":
                        pBool = true;
                        break;
                }
                if (i == 0) {
                    if (field.equals("tmd")) {
                        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(field).field(field)
                                .fixedInterval(DateHistogramInterval.seconds(second));
                        for (int j = 0; j < statField.size(); j++) {
                            String strField = statField.getString(j);
                            String replaceField = strField.replace(".", "");
                            arrayStatField.add(replaceField);
                            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.sum(replaceField).field(strField));
                        }
                        aggregationBuilders = dateHistogramAggregationBuilder;
                    } else {
                        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(field).field(field);
                        for (int j = 0; j < statField.size(); j++) {
                            String strField = statField.getString(j);
                            String replaceField = strField.replace(".", "");
                            arrayStatField.add(replaceField);
                            termsAggregationBuilder = termsAggregationBuilder.subAggregation(AggregationBuilders.sum(replaceField).field(strField));
                        }
                        aggregationBuilders = termsAggregationBuilder;
                    }
                } else {
                    TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(field).field(field);
                    aggregationBuilders = termsAggregationBuilder.subAggregation((AggregationBuilder) aggregationBuilders);
                }
            }
            arrayField.addAll(arrayStatField);
            sourceBuilder.query(queryBuilder).aggregation((AggregationBuilder) aggregationBuilders);
            SearchRequest searchRequest = new SearchRequest(logType).source(sourceBuilder);
            long buildtime = System.currentTimeMillis();
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println("searchResponse=" + searchResponse);
            long queryend = System.currentTimeMillis();
            JSONObject jsonResponse = (JSONObject) JSON.parse(searchResponse.toString());
            //field倒序
            Collections.reverse(excelField);
            arrayBuckets = jsonResponse.getJSONObject("aggregations").getJSONObject("sterms#" + excelField.getString(0)).getJSONArray("buckets");
        }
        System.out.println("arrayStatField=" + arrayStatField);
        if (second == 0) {
            arrayStatistic = statisticRecursionNoTmd(arrayBuckets, excelField, 0, "{", arrayStatistic, arrayStatField);
        } else {
            arrayStatistic = statisticRecursionHasTmd(arrayBuckets, excelField, 0, "{", arrayStatistic, arrayStatField);
        }
        long start = System.currentTimeMillis();
        JSONObject jsonQuery = new JSONObject();
        for (int i = 0; i < excelField.size(); i++) {
            HashSet setQuery = new HashSet();
            for (int j = 1; j < arrayStatistic.size(); j++) {
                setQuery.add(arrayStatistic.getJSONObject(j).getString(excelField.getString(i)));
            }
            JSONArray arrayQuery = (JSONArray) JSON.toJSON(setQuery);
            jsonQuery.put(excelField.getString(i), arrayQuery);
        }
//        System.out.println("jsonQuery=" + jsonQuery);
        JSONObject jsonResult = new JSONObject();
        //根据id和grp获取名称
        for (int i = 0; i < excelField.size(); i++) {
            JSONObject json = new JSONObject();
            if (excelField.getString(i).startsWith("id")) {
                Query query = new Query(new Criteria("_id").in(jsonQuery.getJSONArray(excelField.getString(i))));
                query.fields().include("info");
                String table = statInit.getJSONObject(excelField.getString(i)).getString("table");
                List list = new ArrayList();
                switch (table) {
                    case "User":
                        list = mongoTemplate.find(query, User.class);
                        System.out.println("user");
                        break;
                    case "Order":
                        list = mongoTemplate.find(query, Order.class);
                        System.out.println("order");
                        break;
                    case "Prod":
                        list = mongoTemplate.find(query, Prod.class);
                        System.out.println("prod");
                        break;
                    case "Comp":
                        list = mongoTemplate.find(query, Comp.class);
                        System.out.println("comp");
                        break;
                    case "Asset":
                        list = mongoTemplate.find(query, Asset.class);
                        System.out.println("asset");
                        break;
                }
                JSONArray array = (JSONArray) JSON.toJSON(list);
                System.out.println("array=" + array);
                for (int j = 0; j < array.size(); j++) {
                    json.put(array.getJSONObject(j).getString("id"), array.getJSONObject(j).getJSONObject("info").getJSONObject("wrdN").getString("cn"));
                }
                jsonResult.put(excelField.getString(i), json);
            } else if (excelField.getString(i).startsWith("grp")) {
                String aId = redisUtils.getId_A(id_C, "a-auth");
                Query queryAsset = new Query(new Criteria("_id").is(aId));
                queryAsset.fields().include("def");
                Asset asset = mongoTemplate.findOne(queryAsset, Asset.class);
                JSONArray arrayGrp = jsonQuery.getJSONArray(excelField.getString(i));
                JSONArray arrayGrpList = asset.getDef().getJSONArray(statInit.getJSONObject(excelField.getString(i)).getString("table"));
                for (int j = 0; j < arrayGrp.size(); j++) {
                    for (int k = 0; k < arrayGrpList.size(); k++) {
                        if (arrayGrp.getString(j).equals(arrayGrpList.getJSONObject(k).getString("ref"))) {
                            json.put(arrayGrp.getString(j), arrayGrpList.getJSONObject(k).getJSONObject("wrdN").getString("cn"));
                        }
                    }
                }
                jsonResult.put(excelField.getString(i), json);
            }
        }
        //根据id_O和id_P获取单价
//        if (jsonQuery.getJSONArray("id_O") != null && jsonQuery.getJSONArray("id_P") != null) {
//            JSONObject jsonPriceOrder = new JSONObject();
//            Query query = new Query(new Criteria("_id").in(jsonQuery.getZJSONArray("id_O")));
//            query.fields().include("info");
//            query.fields().include("oItem");
//            List<Order> orders = mongoTemplate.find(query, Order.class);
////                System.out.println("orders=" + orders);
//            for (int i = 0; i < orders.size(); i++) {
//                JSONArray arrayObjItem = orders.get(i).getOItem().getJSONArray("objItem");
//                JSONObject jsonPriceProd = new JSONObject();
//                for (int j = 0; j < arrayObjItem.size(); j++) {
//                    jsonPriceProd.put(arrayObjItem.getJSONObject(j).getString("id"), arrayObjItem.getJSONObject(j).getDouble("wn4price"));
//                }
//                jsonPriceOrder.put(orders.get(i).getId(), jsonPriceProd);
//            }
//            jsonResult.put("price", jsonPriceOrder);
//        }
//        System.out.println("arrayStatistic=" + arrayStatistic);
        //将id和grp修改为名称
        //KEV WHY?
        if (jsonResult.getJSONObject("price") != null) {
            System.out.println("jsonResult.getJSONObject(price)=" + jsonResult.getJSONObject("price"));
            for (int i = arrayStatisticSize; i < arrayStatistic.size(); i++) {
                String id_O = arrayStatistic.getJSONObject(i).getString("id_O");
                String id_P = arrayStatistic.getJSONObject(i).getString("id_P");
                System.out.println("id_O=" + id_O);
                System.out.println("id_P=" + id_P);
                arrayStatistic.getJSONObject(i).put("price", jsonResult.getJSONObject("price").getJSONObject(id_O).getDouble(id_P));
//                    double sum = arrayStatistic.getJSONObject(i).getDouble("count") * arrayStatistic.getJSONObject(i).getDouble("price");
//                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
//                    sum = Double.parseDouble(decimalFormat.format(sum));
//                    arrayStatistic.getJSONObject(i).put("sum", sum);
                for (int j = 0; j < excelField.size(); j++) {
                    if (!excelField.getString(j).equals("tmd")) {
                        String cell = arrayStatistic.getJSONObject(i).getString(excelField.getString(j));
                        arrayStatistic.getJSONObject(i).put(excelField.getString(j), jsonResult.getJSONObject(excelField.getString(j)).getString(cell));
                    }
                }
            }
        } else {
            for (int i = 1; i < arrayStatistic.size(); i++) {
                for (int j = 0; j < excelField.size(); j++) {
                    if (!excelField.getString(j).equals("tmd")) {
                        String cell = arrayStatistic.getJSONObject(i).getString(excelField.getString(j));
                        arrayStatistic.getJSONObject(i).put(excelField.getString(j), jsonResult.getJSONObject(excelField.getString(j)).getString(cell));
                    }
                }
            }
        }
//        long end = System.currentTimeMillis();
//        System.out.println("time=" + (end - start) + "ms");
//        System.out.println("getmongo=" + (getend - getstart) + "ms");
//        System.out.println("size=" + arrayStatistic.size());
        if (outputType.equals("card")) {
            if (arrayStatistic.size() <= 1000) {
                System.out.println("arrayStatistic=" + arrayStatistic);
                Boolean bool = setStatisticByMongo(arrayStatistic, id_C, startDate, endDate, second, arrayField, fileName);
                return retResult.ok(CodeEnum.OK.getCode(), bool);
            }
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, TimerEnum.STAT_LENGTH_GT.getCode(), null);
        } else {
            File file = ExcelUtils.statisticExcel(arrayStatistic, arrayField, fileName);
            String url = CosUpload.uploadFile(file, id_A);
            System.out.println("url=" + url);
            return retResult.ok(CodeEnum.OK.getCode(), url);
        }
    }

    //This is the core that return the histogram
    @Override
    public JSONArray getStatArrayByEs(JSONObject termField, JSONObject rangeField, JSONObject second, JSONArray excelField, String logType, JSONArray statField) throws IOException, ParseException {
        //秒数不为0，根据日期分组
        if (second.getInteger("second") != null) {
            System.out.println("second notnull");
            second.put("type", "date_histogram#");
            //KEV why
            if (excelField.size() != 0) {
                excelField.add(second);
            }
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        for (Map.Entry<String, Object> entry : termField.entrySet()) {
            queryBuilder.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
        }
        queryBuilder.must(QueryBuilders.rangeQuery(rangeField.getString("field"))
                .from(rangeField.getString("startDate"), true).to(rangeField.getString("endDate"), false));
        System.out.println("queryBuilder=" + queryBuilder);
        //fields倒序
        Collections.reverse(excelField);
        Object aggregationBuilders = null;
//        String sField = statField.getString("field");
//        String sType = statField.getString("type");
//        String sFieldReplace = sField.replace(".", "");
//        statField.put("field", sFieldReplace);
        //判断是否有分组条件
        if (excelField.size() != 0) {
            //根据field构建分组查询语句
            for (int i = 0; i < excelField.size(); i++) {
                JSONObject jsonExcelField = excelField.getJSONObject(i);
                String eField = jsonExcelField.getString("field");
                String eType = jsonExcelField.getString("type");
                String eReplace = eField.replace(".", "");
                jsonExcelField.put("field", eReplace);
                if (i == 0) {
                    switch (eType) {
                        case "date_histogram#":
                            System.out.println("===11===");
                            DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(eReplace).field(eField)
                                    .fixedInterval(DateHistogramInterval.seconds(jsonExcelField.getInteger("second")));
                            for (int j = 0; j < statField.size(); j++) {
                                JSONObject jsonStatField = statField.getJSONObject(j);
                                String sField = jsonStatField.getString("field");
                                String sType = jsonStatField.getString("type");
                                String sReplace = sField.replace(".", "");
                                jsonStatField.put("field", sReplace);
                                System.out.println("sReplace=" + sReplace);
                                System.out.println("sField=" + sField);
                                switch (sType) {
                                    case "sum#":
                                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.sum(sType + sReplace).field(sField));
                                        break;
                                    case "avg#":
                                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg(sType + sReplace).field(sField));
                                        break;
                                    case "max#":
                                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.max(sType + sReplace).field(sField));
                                        break;
                                    case "min#":
                                        dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.min(sType + sReplace).field(sField));
                                        break;
                                }
                            }
                            aggregationBuilders = dateHistogramAggregationBuilder;
                            break;
                        case "sterms#":
                            TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(eReplace).field(eField);
                            for (int j = 0; j < statField.size(); j++) {
                                JSONObject jsonStatField = statField.getJSONObject(j);
                                String sField = jsonStatField.getString("field");
                                String sType = jsonStatField.getString("type");
                                String sReplace = sField.replace(".", "");
                                jsonStatField.put("field", sReplace);
                                System.out.println("sReplace=" + sReplace);
                                System.out.println("sField=" + sField);
                                switch (sType) {
                                    case "sum#":
                                        termsAggregationBuilder = termsAggregationBuilder.subAggregation(AggregationBuilders.sum(sType + sReplace).field(sField));
                                        break;
                                    case "avg#":
                                        termsAggregationBuilder = termsAggregationBuilder.subAggregation(AggregationBuilders.avg(sType + sReplace).field(sField));
                                        break;
                                    case "max#":
                                        termsAggregationBuilder = termsAggregationBuilder.subAggregation(AggregationBuilders.max(sType + sReplace).field(sField));
                                        break;
                                    case "min#":
                                        termsAggregationBuilder = termsAggregationBuilder.subAggregation(AggregationBuilders.min(sType + sReplace).field(sField));
                                        break;
                                }
                            }
                            aggregationBuilders = termsAggregationBuilder;
                            break;
                    }
                } else {
                    TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(eReplace).field(eField);
                    aggregationBuilders = termsAggregationBuilder.subAggregation((AggregationBuilder) aggregationBuilders);
                }
            }
            sourceBuilder.query(queryBuilder).size(0).aggregation((AggregationBuilder) aggregationBuilders);
        } else {
            if (second.getInteger("second") != null) {
                System.out.println("===33===");
                String eField = second.getString("field");
                String eReplace = eField.replace(".", "");
                second.put("field", eReplace);
                DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(eReplace).field(eField)
                        .fixedInterval(DateHistogramInterval.seconds(second.getInteger("second")));
                for (int j = 0; j < statField.size(); j++) {
                    JSONObject jsonStatField = statField.getJSONObject(j);
                    String sField = jsonStatField.getString("field");
                    String sType = jsonStatField.getString("type");
                    String sReplace = sField.replace(".", "");
                    jsonStatField.put("field", sReplace);
                    System.out.println("sReplace=" + sReplace);
                    System.out.println("sField=" + sField);
                    switch (sType) {
                        case "sum#":
                            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.sum(sType + sReplace).field(sField));
                            break;
                        case "avg#":
                            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg(sType + sReplace).field(sField));
                            break;
                        case "max#":
                            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.max(sType + sReplace).field(sField));
                            break;
                        case "min#":
                            dateHistogramAggregationBuilder = dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.min(sType + sReplace).field(sField));
                            break;
                    }
                }
                sourceBuilder.query(queryBuilder).size(0).aggregation(dateHistogramAggregationBuilder);
            } else {
                System.out.println("===44===");
                sourceBuilder.query(queryBuilder).size(0);
                for (int j = 0; j < statField.size(); j++) {
                    JSONObject jsonStatField = statField.getJSONObject(j);
                    String sField = jsonStatField.getString("field");
                    String sType = jsonStatField.getString("type");
                    String sReplace = sField.replace(".", "");
                    jsonStatField.put("field", sReplace);
                    System.out.println("sReplace=" + sReplace);
                    System.out.println("sField=" + sField);
                    switch (sType) {
                        case "sum#":
                            sourceBuilder.aggregation(AggregationBuilders.sum(sType + sReplace).field(sField));
                            break;
                        case "avg#":
                            sourceBuilder.aggregation(AggregationBuilders.avg(sType + sReplace).field(sField));
                            break;
                        case "max#":
                            sourceBuilder.aggregation(AggregationBuilders.max(sType + sReplace).field(sField));
                            break;
                        case "min#":
                            sourceBuilder.aggregation(AggregationBuilders.min(sType + sReplace).field(sField));
                            break;
                    }
                }
            }
        }
        System.out.println("excelField=" + excelField);
        System.out.println("statField=" + statField);
        SearchRequest searchRequest = new SearchRequest(logType).source(sourceBuilder);
        long buildtime = System.currentTimeMillis();
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("searchResponse=" + searchResponse);
        long queryend = System.currentTimeMillis();
        JSONObject jsonResponse = (JSONObject) JSON.parse(searchResponse.toString());
        //field倒序
        Collections.reverse(excelField);
        JSONArray arrayStatistic = new JSONArray();
//        JSONObject jsonStatistic = new JSONObject();
        if (excelField.size() != 0) {
            String eType = excelField.getJSONObject(0).getString("type");
            String eField = excelField.getJSONObject(0).getString("field");
            JSONArray arrayBuckets = jsonResponse.getJSONObject("aggregations").getJSONObject(eType + eField).getJSONArray("buckets");
            if (second.getInteger("second") != null) {
                System.out.println("===1===");
                arrayStatistic = statisticRecursionHasTmd(arrayBuckets, excelField, 0, "{", statField, arrayStatistic);
            } else {
                System.out.println("===2===");
                arrayStatistic = statisticRecursionNoTmd(arrayBuckets, excelField, 0, "{", statField, arrayStatistic);
            }
        } else {
            if (second.getInteger("second") != null) {
                System.out.println("===3===");
                String eType = second.getString("type");
                String eField = second.getString("field");
                JSONArray arrayBuckets = jsonResponse.getJSONObject("aggregations").getJSONObject(eType + eField).getJSONArray("buckets");
                excelField.add(second);
                arrayStatistic = statisticRecursionHasTmd(arrayBuckets, excelField, 0, "{", statField, arrayStatistic);
            } else {
                System.out.println("===4===");
                System.out.println("jsonResponse=" + jsonResponse);
                JSONObject jsonStatistic = new JSONObject();
                for (int i = 0; i < statField.size(); i++) {
                    String sType = statField.getJSONObject(i).getString("type");
                    String sField = statField.getJSONObject(i).getString("field");
                    if (sField.equals("tmd") || sField.equals("tmk")) {
                        String value = jsonResponse.getJSONObject("aggregations").getJSONObject(sType + sType + sField).getString("value_as_string");
                        jsonStatistic.put(sType + sField, value);
                    } else {
                        Double value = jsonResponse.getJSONObject("aggregations").getJSONObject(sType + sType + sField).getDouble("value");
                        jsonStatistic.put(sType + sField, value);
                    }
                }
                arrayStatistic.add(jsonStatistic);
            }
        }
        System.out.println("arrayStatistic=" + arrayStatistic);
        return arrayStatistic;
    }

    //This one return 1 single result from summarize
    @Override
    public Object getStatValueByEs(JSONObject termField, JSONObject rangeField, String logType, JSONObject statField) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        for (Map.Entry<String, Object> entry : termField.entrySet()) {
            queryBuilder.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
        }
        queryBuilder.must(QueryBuilders.rangeQuery(rangeField.getString("field"))
                .from(rangeField.getString("startDate"), true).to(rangeField.getString("endDate"), false));
        sourceBuilder.query(queryBuilder).size(0);
        String type = statField.getString("type");
        String field = statField.getString("field");
        switch (type) {
            case "sum#":
                sourceBuilder.aggregation(AggregationBuilders.sum(field).field(field));
                break;
            case "avg#":
                sourceBuilder.aggregation(AggregationBuilders.avg(field).field(field));
                break;
            case "max#":
                sourceBuilder.aggregation(AggregationBuilders.max(field).field(field));
                break;
            case "min#":
                sourceBuilder.aggregation(AggregationBuilders.min(field).field(field));
                break;
        }
        SearchRequest searchRequest = new SearchRequest(logType).source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("searchResponse=" + searchResponse);
        JSONObject jsonResponse = (JSONObject) JSON.parse(searchResponse.toString());
        if (field.equals("tmd") || field.equals("tmk")) {
            String valueString = jsonResponse.getJSONObject("aggregations").getJSONObject(type + field).getString("value_as_string");
            return valueString;
        }
        Double value = jsonResponse.getJSONObject("aggregations").getJSONObject(type + field).getDouble("value");
        return value;
    }

    @Override
    public JSONObject statFilter(String id_C, JSONArray excelField, String logType, Integer titleType, JSONArray arrayStat) {
        Query queryInit = new Query(new Criteria("_id").is("cn_java"));
        queryInit.fields().include("statInit");
        InitJava initJava = mongoTemplate.findOne(queryInit, InitJava.class);
        System.out.println("initJava=" + initJava);
        JSONObject jsonStatInit = initJava.getStatInit().getJSONObject(logType);
        System.out.println("jsonStatInit=" + jsonStatInit);
        JSONObject jsonStatTitle = new JSONObject();
        JSONObject jsonQuery = new JSONObject();
        switch (titleType) {
            case 0:
                JSONObject jsonStat = arrayStat.getJSONObject(0);
                jsonStat.forEach((k, v) ->{
                    System.out.println(jsonStatInit.getJSONObject(k));
                    System.out.println(jsonStatInit.getJSONObject(k).getJSONObject("wrdN"));
                    jsonStatTitle.put(k, jsonStatInit.getJSONObject(k).getJSONObject("wrdN").getString("cn"));
                });
                System.out.println("jsonStatTitle=" + jsonStatTitle);
                for (int i = 0; i < excelField.size(); i++) {
                    String field = excelField.getJSONObject(i).getString("field");
                    System.out.println("field=" + field);
                    HashSet setQuery = new HashSet();
                    for (int j = 0; j < arrayStat.size(); j++) {
                        setQuery.add(arrayStat.getJSONObject(j).getString(field));
                    }
                    JSONArray arrayQuery = (JSONArray) JSON.toJSON(setQuery);
                    jsonQuery.put(field, arrayQuery);
                }
                break;
        }
        System.out.println("jsonQuery=" + jsonQuery);
        JSONObject jsonResult = new JSONObject();
        //根据id和grp获取名称
        for (int i = 0; i < excelField.size(); i++) {
            String field = excelField.getJSONObject(i).getString("field");
            JSONObject json = new JSONObject();
            if (field.startsWith("id")) {
                Query query = new Query(new Criteria("_id").in(jsonQuery.getJSONArray(field)));
                query.fields().include("info");
                String table = jsonStatInit.getJSONObject(field).getString("table");
                List list = new ArrayList();
                switch (table) {
                    case "User":
                        list = mongoTemplate.find(query, User.class);
                        System.out.println("user");
                        break;
                    case "Order":
                        list = mongoTemplate.find(query, Order.class);
                        System.out.println("order");
                        break;
                    case "Prod":
                        list = mongoTemplate.find(query, Prod.class);
                        System.out.println("prod");
                        break;
                    case "Comp":
                        list = mongoTemplate.find(query, Comp.class);
                        System.out.println("comp");
                        break;
                    case "Asset":
                        list = mongoTemplate.find(query, Asset.class);
                        System.out.println("asset");
                        break;
                }
                JSONArray array = (JSONArray) JSON.toJSON(list);
                System.out.println("array=" + array);
                for (int j = 0; j < array.size(); j++) {
                    json.put(array.getJSONObject(j).getString("id"), array.getJSONObject(j).getJSONObject("info").getJSONObject("wrdN").getString("cn"));
                }
                jsonResult.put(field, json);
            } else if (field.startsWith("grp")) {

                String aId = redisUtils.getId_A(id_C, "a-auth");

                Query queryAsset = new Query(new Criteria("_id").is(aId));
                queryAsset.fields().include("def");
                Asset asset = mongoTemplate.findOne(queryAsset, Asset.class);
                JSONArray arrayGrp = jsonQuery.getJSONArray(field);
                JSONArray arrayGrpList = asset.getDef().getJSONArray(jsonStatInit.getJSONObject(field).getString("table"));
                for (int j = 0; j < arrayGrp.size(); j++) {
                    for (int k = 0; k < arrayGrpList.size(); k++) {
                        if (arrayGrp.getString(j).equals(arrayGrpList.getJSONObject(k).getString("ref"))) {
                            json.put(arrayGrp.getString(j), arrayGrpList.getJSONObject(k).getJSONObject("wrdN").getString("cn"));
                        }
                    }
                }
                jsonResult.put(field, json);
            }
        }
        System.out.println("jsonResult=" + jsonResult);
        for (int i = 0; i < arrayStat.size(); i++) {
            for (int j = 0; j < excelField.size(); j++) {
                String field = excelField.getJSONObject(j).getString("field");
                if (!field.equals("tmd")) {
                    String cell = arrayStat.getJSONObject(i).getString(field);
                    arrayStat.getJSONObject(i).put(field, jsonResult.getJSONObject(field).getString(cell));
                }
            }
        }
        System.out.println("arrayStat=" + arrayStat);
        JSONObject jsonStatResult = new JSONObject();
        jsonStatResult.put("jsonStatTitle", jsonStatTitle);
        jsonStatResult.put("arrayStat", arrayStat);
        return jsonStatResult;
    }

    @Override
    public JSONArray statisticRecursionHasTmd(JSONArray arrayBuckets, JSONArray excelField, Integer index, String cell, JSONArray statField, JSONArray arrayStatistic) {
        System.out.println("index=" + index);
        if (index < excelField.size() - 1) {
            for (int i = 0; i < arrayBuckets.size(); i++) {
                String cell1 = cell + "\"" + excelField.getJSONObject(index).getString("field") + "\":\"" + arrayBuckets.getJSONObject(i).getString("key") + "\",";
                String eType = excelField.getJSONObject(index + 1).getString("type");
                String eField = excelField.getJSONObject(index + 1).getString("field");
                JSONArray arrayBuckets1 = arrayBuckets.getJSONObject(i).getJSONObject(eType + eField).getJSONArray("buckets");
                statisticRecursionHasTmd(arrayBuckets1, excelField, index + 1, cell1, statField, arrayStatistic);
            }
        } else {
            for (int i = 0; i < arrayBuckets.size(); i++) {
                System.out.println("arrayBuckets=" + arrayBuckets);
                System.out.println(excelField.getJSONObject(index).getString("field"));
                System.out.println(arrayBuckets.getJSONObject(i).getString("key_as_string"));
                String cell1 = cell + "\"" + excelField.getJSONObject(index).getString("field") + "\":\"" + arrayBuckets.getJSONObject(i).getString("key_as_string") + "\"";
                System.out.println("cell1=" + cell1);
                for (int j = 0; j < statField.size(); j++) {
                    String sType = statField.getJSONObject(j).getString("type");
                    String sField = statField.getJSONObject(j).getString("field");
                    String sTypeSubString = sType.substring(0, sType.length() - 1);
                    String cell2;
                    if (sField.equals("tmd") || sField.equals("tmk")) {
                        cell2 = cell1 + ",\"" + sTypeSubString + sField + "\":\"" + arrayBuckets.getJSONObject(i).getJSONObject(sType + sType + sField).getString("value_as_string") + "\"";
                    } else {
                        cell2 = cell1 + ",\"" + sTypeSubString + sField + "\":\"" + arrayBuckets.getJSONObject(i).getJSONObject(sType + sType + sField).getDouble("value") + "\"";
                    }
                    cell2 += "}";
                    System.out.println("cell2=" + cell2);
                    JSONObject jsonCell = JSONObject.parseObject(cell2);
                    arrayStatistic.add(jsonCell);
                }
            }
        }
        return arrayStatistic;
    }

    @Override
    public JSONArray statisticRecursionNoTmd(JSONArray arrayBuckets, JSONArray excelField, Integer index, String cell, JSONArray statField, JSONArray arrayStatistic) {
        System.out.println("index=" + index);
        if (index < excelField.size() - 1) {
            for (int i = 0; i < arrayBuckets.size(); i++) {
                cell += "\"" + excelField.getJSONObject(index).getString("field") + "\":\"" + arrayBuckets.getJSONObject(i).getString("key") + "\",";
                String eType = excelField.getJSONObject(index + 1).getString("type");
                String eField = excelField.getJSONObject(index + 1).getString("field");
                JSONArray arrayBuckets1 = arrayBuckets.getJSONObject(i).getJSONObject(eType + eField).getJSONArray("buckets");
                statisticRecursionNoTmd(arrayBuckets1, excelField, index + 1, cell, statField, arrayStatistic);
            }
        } else {
            for (int i = 0; i < arrayBuckets.size(); i++) {
                String cell1 = cell + "\"" + excelField.getJSONObject(index).getString("field") + "\":\"" + arrayBuckets.getJSONObject(i).getString("key") + "\"";
                for (int j = 0; j < statField.size(); j++) {
                    String sType = statField.getJSONObject(j).getString("type");
                    String sField = statField.getJSONObject(j).getString("field");
                    String sTypeSubString = sType.substring(0, sType.length() - 1);
                    if (sField.equals("tmd") || sField.equals("tmk")) {
                        cell1 += ",\"" + sTypeSubString + sField + "\":\"" + arrayBuckets.getJSONObject(i).getJSONObject(sType + sType + sField).getString("value_as_string") + "\"";
                    } else {
                        cell1 += ",\"" + sTypeSubString + sField + "\":\"" + arrayBuckets.getJSONObject(i).getJSONObject(sType + sType + sField).getDouble("value") + "\"";
                    }
                }
                cell1 += "}";
                JSONObject jsonCell = JSONObject.parseObject(cell1);
                arrayStatistic.add(jsonCell);
            }
        }
        return arrayStatistic;
    }

    @Override
    public JSONObject getStatisticByMongo(String id_C, String startDate, String endDate, Integer second, JSONArray excelField) throws ParseException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
        long startTime = sdf.parse(startDate).getTime();
        long endTime = sdf.parse(endDate).getTime();
        String strFields = excelField.toString();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("id_C", id_C)).must(QueryBuilders.termQuery("second", second)).must(QueryBuilders.termQuery("field", strFields));
        sourceBuilder.query(queryBuilder).from(0).size(1);
        //KEV something wrong: lBAsset, you don't need to query here...


        SearchRequest searchRequest = new SearchRequest("lBAsset").source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //判断是否有数据，没有则返回null
        //
        if (searchResponse.getHits().getHits().length != 0) {
            SearchHit hit = searchResponse.getHits().getHits()[0];
            JSONObject jsonHit = (JSONObject) JSON.parse(hit.getSourceAsString());
            System.out.println("jsonHit=" + jsonHit);
            long esStartTime = jsonHit.getLong("startTime");
            long esEndTime = jsonHit.getLong("endTime");
            Query query = new Query(new Criteria("_id").is(jsonHit.getString("id_A")));
            Asset asset = mongoTemplate.findOne(query, Asset.class);
            JSONObject jsonResult = new JSONObject();
            //判断时间范围是否有包含mongo数据时间范围，没有则返回null，有则返回相应数据
            if (startTime == esStartTime && endTime == esEndTime) {
                JSONArray arrayObjData = asset.getSumm00s().getJSONArray("objData");
                jsonResult.put("array", arrayObjData);
                jsonResult.put("type", "==");
                System.out.println("==");
                return jsonResult;
            } else if (startTime == esStartTime && endTime > esEndTime) {
                JSONArray arrayObjData = asset.getSumm00s().getJSONArray("objData");
                jsonResult.put("array", arrayObjData);
                jsonResult.put("endTime", esEndTime);
                jsonResult.put("type", ">");
                System.out.println(">");
                return jsonResult;
            } else if (startTime < esStartTime && endTime == esEndTime) {
                JSONArray arrayObjData = asset.getSumm00s().getJSONArray("objData");
                jsonResult.put("array", arrayObjData);
                jsonResult.put("startTime", esStartTime);
                jsonResult.put("type", "<");
                System.out.println("<");
                return jsonResult;
            } else if (startTime < esStartTime && endTime > esEndTime) {
                JSONArray arrayObjData = asset.getSumm00s().getJSONArray("objData");
                jsonResult.put("array", arrayObjData);
                jsonResult.put("startTime", esStartTime);
                jsonResult.put("endTime", esEndTime);
                jsonResult.put("type", "<>");
                System.out.println("<>");
                return jsonResult;
            }
        }
        return null;
    }


    //This one is incorrect, you set data into a summ00s card but not making the entire id_A, no need write info card
    @Override
    public Boolean setStatisticByMongo(JSONArray arrayExcel, String id_C, String startDate, String endDate, Integer second, JSONArray excelField, String fileName) throws IOException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());

        long startTime = sdf.parse(startDate).getTime();
        long endTime = sdf.parse(endDate).getTime();
        String strFields = excelField.toString();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("id_C", id_C)).must(QueryBuilders.termQuery("second", second)).must(QueryBuilders.termQuery("field", strFields));
        sourceBuilder.query(queryBuilder).from(0).size(1);
        SearchRequest searchRequest = new SearchRequest("lBAsset").source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        System.out.println("searchResponse=" + searchResponse);
        //判断是否有数据，有就覆盖，没有就新增
        if (searchResponse.getHits().getHits().length == 0) {
            String id_A = MongoUtils.GetObjectId();
            System.out.println("id_A=" + id_A);
            JSONObject jsonInfo = new JSONObject();
            JSONObject jsonWrdN = new JSONObject();
            JSONObject jsonSumm00s = new JSONObject();
            jsonWrdN.put("cn", fileName);
            jsonInfo.put("wrdN", jsonWrdN);
            jsonInfo.put("id_C", id_C);
            jsonInfo.put("startTime", startTime);
            jsonInfo.put("endTime", endTime);
            jsonInfo.put("second", second);
            jsonInfo.put("field", strFields);
            jsonInfo.put("tmd", dateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
            jsonSumm00s.put("objData", arrayExcel);
            Asset asset = new Asset();
            asset.setId(id_A);

            AssetInfo ainfo = new AssetInfo(id_C, id_C, "", jsonWrdN, jsonWrdN, "1000", "", "", 3);
            asset.setInfo(ainfo);
            asset.setSumm00s(jsonSumm00s);
            mongoTemplate.insert(asset);
            jsonInfo.put("id_A", id_A);
            IndexRequest indexRequest = new IndexRequest("lBAsset").source(jsonInfo, XContentType.JSON);
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            System.out.println("indexResponse=" + indexResponse);
            if (indexResponse != null) {
                return true;
            }
        } else {
            SearchHit hit = searchResponse.getHits().getHits()[0];
            JSONObject jsonHit = (JSONObject) JSON.parse(hit.getSourceAsString());
            String id_A = jsonHit.getString("id_A");
            Query queryAsset = new Query(new Criteria("_id").is(id_A));
            Update updateAsset = new Update();
            updateAsset.set("info.startTime", startTime);
            updateAsset.set("info.endTime", endTime);
            updateAsset.set("info.wrdN.cn", fileName);
            updateAsset.set("info.tmd", dateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
            updateAsset.set("summ00s.objData", arrayExcel);
            UpdateResult updateResult = mongoTemplate.updateFirst(queryAsset, updateAsset, Asset.class);
            System.out.println("updateResult=" + updateResult);
            if (updateResult.getModifiedCount() > 0) {
                jsonHit.put("startTime", startTime);
                jsonHit.put("endTime", endTime);
                UpdateRequest updateRequest = new UpdateRequest().index("lBAsset");
                updateRequest.id(hit.getId());
                updateRequest.doc(jsonHit, XContentType.JSON);
                UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
                System.out.println("updateResponse=" + updateResponse);
                if (updateResponse != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
//    @Async
    public Object getSumm00s(String id_C, Integer index) throws IOException, ParseException {
        String id_A = redisUtils.getId_A(id_C, "a-stat");
        Asset asset = (Asset) dbUtils.getMongoOneField(id_A, "summ00s", Asset.class);
        JSONObject jsonObjData = asset.getSumm00s().getJSONArray("objData").getJSONObject(index);
        //统计字段，统计间隔有变化，重新统计
        if (jsonObjData.getBoolean("isUpdate")) {
            asyncUtils.setSumm00s(id_C, id_A, index, jsonObjData);
            System.out.println("==true==");
            return retResult.ok(CodeEnum.OK.getCode(), null);
        }
        JSONObject jsonStatTitle = jsonObjData.getJSONObject("statTitle");
        JSONArray arrayStatContent = jsonObjData.getJSONArray("statContent");
        JSONObject jsonStatResult = new JSONObject();
        jsonStatResult.put("jsonStatTitle", jsonStatTitle);
        jsonStatResult.put("arrayStatContent", arrayStatContent);
        System.out.println("==false==");
        return jsonStatResult;
    }

//    @Override
//    @Async
//    public void setSumm00s(String id_C, JSONObject jsonObjData) throws IOException, ParseException {
//        JSONObject jsonTermField = jsonObjData.getJSONObject("termField");
//        JSONObject jsonRangeField = jsonObjData.getJSONObject("rangeField");
//        JSONObject jsonSecond = jsonObjData.getJSONObject("second");
//        JSONArray arrayExcelField = jsonObjData.getJSONArray("excelField");
//        String logType = jsonObjData.getString("logType");
//        JSONArray arrayStatField = jsonObjData.getJSONArray("statField");
//        JSONArray arrayStat = getStatArrayByEs(jsonTermField, jsonRangeField, jsonSecond, arrayExcelField, logType, arrayStatField);
//        if (arrayStat.size() > 1000) {
//            throw new ErrorResponseException(HttpStatus.FORBIDDEN, TimerEnum.STAT_LENGTH_GT.getCode(), null);
//        }
//        JSONObject jsonStatResult = statFilter(id_C, arrayExcelField, logType, 0, arrayStat);
//        System.out.println(jsonStatResult.getJSONObject("jsonTitle"));
//        System.out.println(jsonStatResult.getJSONArray("arrayStat"));
//
//    }


//    //I better redefine my needs before I ask him to redo these
//
//


}
