//package com.cresign.timer.utils;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.tools.common.Constants;
//import com.cresign.tools.enumeration.DateEnum;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.support.master.AcknowledgedResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.indices.CreateIndexRequest;
//import org.elasticsearch.client.indices.CreateIndexResponse;
//import org.elasticsearch.client.indices.DeleteAliasRequest;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.index.query.MatchQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * ##author: Jevon
// * ##version: 1.0
// * ##updated: 2020/8/18 15:12
// * ##description: ES通用工具类
// */
//@Service
//@Slf4j
//public class ElasticsearchUtils {
//
//    @Autowired
////    @Qualifier("getRestHighLevelClient")//指定Bean
//    private RestHighLevelClient restHighLevelClient;
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    /**
//     * 获取ES数据进行分组（根据id_C分组）
//     * ##author: Jevon
//     * ##Params:
//     * ##version: 1.0
//     * ##updated: 2020/8/18 15:53
//     * ##Return: java.util.List<java.util.List < java.util.Map < java.lang.String, java.lang.Object>>>
//     */
//    public  List<List<Map<String, Object>>> getCompGrouping(String logType) throws IOException, ParseException {
//        //构建搜索条件
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        // 获取昨天的日期
//        String pastDate = GenericUtils.getPastDate(Constants.INT_ONE,DateEnum.DATE_ONE.getDate());
//        //获取今天的日期   字段时间
//        //String pastDate = GenericUtils.getDateByT(DateEnum.DATE_ONE.getDate());
//        //获取这个月的时间   索引时间
//        String IndexeDate = GenericUtils.strOndToDateFormat(pastDate);
//
//        //只查时间为今天的数据    精确查询，根据时间字段
//        MatchQueryBuilder logTime = QueryBuilders.matchQuery("logTime", pastDate);
//
//        searchSourceBuilder.query(logTime);
//        //查那个索引（查那个表）  日志类型+日期
//        SearchRequest srb = new SearchRequest(logType+"-"+IndexeDate);
//        //SearchRequest srb = new SearchRequest(logType);
//        srb.source(searchSourceBuilder);
//        //查询
//        SearchResponse search = restHighLevelClient.search(srb, RequestOptions.DEFAULT);
//        List<Map<String,Object>>newList=new ArrayList<>();
//        //查出来的数据放list<Map<String,Object>>中，以便给jdk8特性分组
//        for (SearchHit hit : search.getHits().getHits()) {
//            newList.add(hit.getSourceAsMap());
//        }
//        //jdk8特性
//        Map<Object, List<Map<String, Object>>> mapList = newList.stream().collect(Collectors.groupingBy(m -> m.get("id_C")));
//
//        ArrayList<List<Map<String, Object>>> arrayList = new ArrayList<>();
//        //分组后的数据遍历，
//        for(Map.Entry<Object, List<Map<String, Object>>> keyvalue: mapList.entrySet()){
//            //只要value
//            arrayList.add(keyvalue.getValue());
//        }
//        return arrayList;
//    }
//
//
//    public CreateIndexResponse addIndex(String indexName, JSONObject mapping) throws IOException {
//        Calendar calendar = Calendar.getInstance();
//        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName + "-" + calendar.get(Calendar.YEAR));
//        createIndexRequest.settings(Settings.builder().put("index.number_of_shards", 5)
//                .put("index.number_of_replicas", 1));
//        JSONObject aliases = new JSONObject();
//        aliases.put(indexName, new JSONObject());
//        aliases.put(indexName + "-read", new JSONObject());
//        aliases.put(indexName + "-write", new JSONObject());
//        createIndexRequest.aliases(aliases);
//        createIndexRequest.mapping(mapping);
////        JSONObject mapping = new JSONObject();
////        mapping.put("dynamic", "false");
////        JSONObject properties = new JSONObject();
////        JSONObject id = new JSONObject();
////        id.put("type", "keyword");
////        JSONObject id_FS = new JSONObject();
////        id_FS.put("type", "keyword");
////        JSONObject id_C = new JSONObject();
////        id_C.put("type", "keyword");
////        JSONObject id_CS = new JSONObject();
////        id_CS.put("type", "keyword");
////        JSONObject id_U = new JSONObject();
////        id_U.put("type", "keyword");
////        JSONObject dep = new JSONObject();
////        dep.put("type", "keyword");
////        JSONObject grpU = new JSONObject();
////        grpU.put("type", "keyword");
////        JSONObject pic = new JSONObject();
////        pic.put("type", "text");
////        JSONObject id_O = new JSONObject();
////        id_O.put("type", "keyword");
////        JSONObject index = new JSONObject();
////        index.put("type", "long");
////        JSONObject id_P = new JSONObject();
////        id_P.put("type", "keyword");
////        JSONObject subType = new JSONObject();
////        subType.put("type", "keyword");
////        JSONObject lang = new JSONObject();
////        lang.put("type", "keyword");
////        JSONObject tmd = new JSONObject();
////        tmd.put("type", "date");
////        tmd.put("format", "yyyy/MM/dd HH:mm:ss");
////        JSONObject tmk = new JSONObject();
////        tmk.put("type", "date");
////        tmk.put("format", "yyyy/MM/dd HH:mm:ss");
////        JSONObject zcndesc = new JSONObject();
////        zcndesc.put("type", "text");
////        JSONObject tzone = new JSONObject();
////        tzone.put("type", "long");
////        JSONObject wrdN = new JSONObject();
////        wrdN.put("type", "text");
////        JSONObject wrdNU = new JSONObject();
////        wrdNU.put("type", "text");
////        JSONObject imp = new JSONObject();
////        imp.put("type", "long");
////        JSONObject grpO = new JSONObject();
////        grpO.put("type", "keyword");
////        JSONObject grpOB = new JSONObject();
////        grpOB.put("type", "keyword");
////        JSONObject grpP = new JSONObject();
////        grpP.put("type", "keyword");
////        JSONObject grpPB = new JSONObject();
////        grpPB.put("type", "keyword");
////        JSONObject data = new JSONObject();
////        JSONObject dataProperties = new JSONObject();
////        data.put("properties", dataProperties);
////        properties.put("id", id);
////        properties.put("id_FS", id_FS);
////        properties.put("id_C", id_C);
////        properties.put("id_CS", id_CS);
////        properties.put("id_U", id_U);
////        properties.put("dep", dep);
////        properties.put("grpU", grpU);
////        properties.put("pic", pic);
////        properties.put("id_O", id_O);
////        properties.put("index", index);
////        properties.put("id_P", id_P);
////        properties.put("subType", subType);
////        properties.put("lang", lang);
////        properties.put("tmd", tmd);
////        properties.put("tmk", tmk);
////        properties.put("zcndesc", zcndesc);
////        properties.put("tzone", tzone);
////        properties.put("wrdN", wrdN);
////        properties.put("wrdNU", wrdNU);
////        properties.put("imp", imp);
////        properties.put("grpO", grpO);
////        properties.put("grpOB", grpOB);
////        properties.put("grpP", grpP);
////        properties.put("grpPB", grpPB);
////        properties.put("data", data);
////        mapping.put("properties", properties);
////        createIndexRequest.mapping(mapping);
//        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
//        return createIndexResponse;
//    }
//
//
//    public AcknowledgedResponse addAlias(String indexName, String aliasName) throws IOException {
//        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
//        IndicesAliasesRequest.AliasActions aliasAction = new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD).index(indexName).alias(aliasName);
//        indicesAliasesRequest.addAliasAction(aliasAction);
//        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
//        return acknowledgedResponse;
//    }
//
//    public org.elasticsearch.client.core.AcknowledgedResponse deleteAlias(String indexName, String aliasName) throws IOException {
//        DeleteAliasRequest deleteAliasRequest = new DeleteAliasRequest(indexName, aliasName);
//        org.elasticsearch.client.core.AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().deleteAlias(deleteAliasRequest, RequestOptions.DEFAULT);
//        return acknowledgedResponse;
//    }
//
//}
