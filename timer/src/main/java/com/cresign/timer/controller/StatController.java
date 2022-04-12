package com.cresign.timer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.service.ScriptTimer;
import com.cresign.timer.service.StatService;
import com.cresign.timer.utils.ElasticsearchUtils;
import com.cresign.timer.utils.GenericUtils;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.InitJava;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.token.GetUserIdByToken;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mongodb.client.result.UpdateResult;
import jdk.nashorn.internal.scripts.JO;
import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.AcknowledgedResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.DeleteAliasRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.script.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/stat")
public class StatController {

    @Autowired
    private StatService statService;

    @Autowired
    private ScriptTimer scriptTimer;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private RetResult retResult;

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
//    @Autowired
//    private GenericUtils genericUtils;
//
//    @Autowired
//    private ElasticsearchUtils elasticsearchUtils;

    @SecurityParameter
    @PostMapping("/v1/getStatistic")
    public ApiResponse getStatistic(@RequestBody JSONObject json) throws IOException, ParseException {
        return statService.getStatistic(
                json.getString("id_C"),
                json.getString("startDate"),
                json.getString("endDate"),
                json.getString("subType"),
                json.getInteger("second"),
                json.getString("id_A"),
                json.getJSONArray("excelField"),
                json.getString("outputType"),
                json.getString("fileName"),
                json.getString("logType"),
                json.getJSONArray("statField")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/getStatArrayByEs")
    public JSONArray getStatArrayByEs(@RequestBody JSONObject json) throws IOException, ParseException {
        return statService.getStatArrayByEs(
                json.getJSONObject("termField"),
                json.getJSONObject("rangeField"),
                json.getJSONObject("second"),
                json.getJSONArray("excelField"),
                json.getString("logType"),
                json.getJSONArray("statField")
        );
    }

//    @SecurityParameter
//    @PostMapping("/v1/getStatArrayByEsApi")
//    public ApiResponse getStatArrayByEsApi(@RequestBody JSONObject json) throws IOException, ParseException {
//        JSONArray statArray = statService.getStatArrayByEs(
//                json.getJSONObject("termField"),
//                json.getJSONObject("rangeField"),
//                json.getJSONObject("second"),
//                json.getJSONArray("excelField"),
//                json.getString("logType"),
//                json.getJSONArray("statField"));
//        return retResult.ok(CodeEnum.OK.getCode(), statArray);
//    }

    @SecurityParameter
    @PostMapping("/v1/getStatValueByEs")
    public Object getStatValueByEs(@RequestBody JSONObject json) throws IOException {
        return statService.getStatValueByEs(
                json.getJSONObject("termField"),
                json.getJSONObject("rangeField"),
                json.getString("logType"),
                json.getJSONObject("statField")
        );
    }

    @PostMapping("/v1/getSumm00s")
    public Object getSumm00s(@RequestBody JSONObject json) throws IOException, ParseException {
        System.out.println("controller");
        return statService.getSumm00s(
                json.getString("id_C"),
                json.getInteger("index")
        );
    }

//    @SecurityParameter
    @PostMapping("/v1/cTrigTest")
    public Object cTrigTest(@RequestBody JSONObject json) throws ScriptException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return scriptTimer.cTrigTest(
                json.getString("time")
        );
    }

}
