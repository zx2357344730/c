package com.example.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.client.file.FileClient;
import com.cresign.timer.service.ScriptTimer;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.reflectTools.ApplicationContextTools;
import com.mongodb.client.result.DeleteResult;
import lombok.SneakyThrows;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.ObjectUtils;

import javax.script.ScriptException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class QuartzJob extends QuartzJobBean {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private FileClient fileClient;

    @Autowired
    private ScriptTimer scriptTimer;

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context)
    {
        //获取当前时间
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("当前时间：" + dateFormat.format(date));

        //获取JobDetail中传递的参数
        JSONObject jsonParams = (JSONObject) JSON.toJSON(context.getJobDetail().getJobDataMap().get("params"));
        jsonParams.forEach((k, v) ->{
            System.out.println(k + ":" + v);
        });
        System.out.println("----------------------------------------");

        String methodName = jsonParams.getString("methodName");
        if (methodName != null) {
            switch (methodName) {
                case "cTrigger":
//                    String id_C = jsonParams.getString("id_C");
                    String id_A = jsonParams.getString("id_A");
                    String ref = jsonParams.getString("ref");
                    cTrigger(id_A, ref);
                    break;
                case "removelSProd":
                    removelSProd();
                    removelBProd();
                    removelSBOrder();
                    removelBAsset();
                    break;
            }
        }
    }

    public void test() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, - 7);
        Date date = calendar.getTime();
        String day = sdf.format(date);
        System.out.println("过去七天："+day);
        fileClient.delDownload(day);
    }

    public void cTrigger(String id_A, String ref) throws ScriptException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        JSONObject jsonTrigger = new JSONObject();
        System.out.println(id_A);
        System.out.println(ref);
        Asset asset = (Asset) dbUtils.getMongoOneField(id_A, "cTrigger", Asset.class);
        JSONObject jsonObjTrigger = asset.getCTrigger().getJSONObject("objData");

        JSONObject jsonIf = jsonObjTrigger.getJSONObject("objIf").getJSONObject(ref);
        jsonIf.put("ref", ref);

        jsonTrigger.put("id_A", id_A);
        jsonTrigger.put("objIf", jsonIf);
        jsonTrigger.put("objVar", jsonObjTrigger.getJSONObject("objVar"));
        jsonTrigger.put("objExec", jsonObjTrigger.getJSONObject("objExec"));
        System.out.println("jsonTrigger=" + jsonTrigger);
        Object o = scriptTimer.scriptEngine(jsonTrigger);
        System.out.println("o=" + o);
    }

    public void removelSProd() throws IOException, ParseException {
//        final Calendar calendar = Calendar.getInstance();
//        if (calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE)) {
        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lSProd");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        //查询组别为1020的
        searchSourceBuilder.query(QueryBuilders.matchPhraseQuery("grp", "1020"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        for (SearchHit hit : search.getHits().getHits()) {

            //判断月份是否相同
//                boolean isSameMonth = genericUtils.isSameMonth(hit.getSourceAsMap().get("tmd").toString());
//
//                if (isSameMonth) {
//
//                } else {

            //1.删除mongdb
            Query query = new Query(new Criteria("_id").is(hit.getSourceAsMap().get("id_P")));
            query.fields().include("file00s").include("info");

            Prod prod = mongoTemplate.findOne(query, Prod.class);

            //判断有没有file00s卡片
                    if (prod.getFile00s() != null){

                        HashMap<String, Object> reqJson = new HashMap<>();
                        reqJson.put("id",prod.getId());
                        reqJson.put("id_C",prod.getInfo().getId_C());
                        reqJson.put("fileList",prod.getFile00s().get("objData"));
                        //调用file服务API
                        long fileSize = fileClient.delCOSFile(reqJson);
//            容量卡可能会改，先注释
//                    Update update = new Update();
//                    //自减,powerup容量卡
//                    update.inc("powerup."+"capacity"+".used",- fileSize);
//
//                    if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//
//                        mongoTemplate.updateFirst(new Query(
//                                new Criteria("info.id_C").is(prod.getInfo().getId_C())
//                                        .and("info.ref").is("a-module")),update, Asset.class);
//
//                    }
             }



            //删除mdb
            DeleteResult remove = mongoTemplate.remove(query, Prod.class);

            //判断结果大于0才进行删除列表
            if (remove.getDeletedCount() > 0) {

                // 2.删除es列表
                DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("lSProd");
                deleteByQueryRequest.setQuery(QueryBuilders.termQuery("id_P", hit.getSourceAsMap().get("id_P")));
                restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);


                //3.修改id_P lBProd 的 grpB为1010   可能别人链接我这个产品，但是我这个产品删除了，所以修改别人的组别
                SearchRequest requestlBProd = new SearchRequest("lBProd");
                SearchSourceBuilder searchSourceBuilderlBProd = new SearchSourceBuilder();
                searchSourceBuilderlBProd.query(QueryBuilders.matchPhraseQuery("id_P", hit.getSourceAsMap().get("id_P")));
                requestlBProd.source(searchSourceBuilderlBProd);
                SearchResponse searchlBProd = restHighLevelClient.search(requestlBProd, RequestOptions.DEFAULT);

                for (SearchHit hitlBProd : searchlBProd.getHits().getHits()) {

                    UpdateRequest updateRequest = new UpdateRequest("lBProd", hitlBProd.getId());

                    hitlBProd.getSourceAsMap().put("grpB", "1010");

                    updateRequest.doc(hitlBProd.getSourceAsMap());
                    restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

                }
            }
        }
    }

    public void removelBProd() throws IOException, ParseException {
//        final Calendar calendar = Calendar.getInstance();
//        if (calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE)) {
        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lBProd");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        //查询组别为1020的
        searchSourceBuilder.query(QueryBuilders.matchPhraseQuery("grpB", "1020"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        for (SearchHit hit : search.getHits().getHits()) {

            //判断月份是否相同
//                boolean isSameMonth = genericUtils.isSameMonth(hit.getSourceAsMap().get("tmd").toString());
//
//                if (isSameMonth) {
//
//
//                }else{

            //对方公司id、我放公司id
            int bcdNet = dbUtils.judgeComp( hit.getSourceAsMap().get("id_CB").toString(),hit.getSourceAsMap().get("id_C").toString());

            if (bcdNet == 0 || bcdNet == 2){

                //1.删除mongdb
                Query query = new Query(new Criteria("_id").is(hit.getSourceAsMap().get("id_P")));
                query.fields().include("file00s").include("info");
                Prod prod = mongoTemplate.findOne(query, Prod.class);

                //删除mdb
                DeleteResult remove = mongoTemplate.remove(query, Prod.class);

                //判断有没有file00s卡片
//                        if (prod.getFile00s() != null && prod.getInfo().containsKey("id_C")) {
//
//                            HashMap<String, Object> reqJson = new HashMap<>();
//                            reqJson.put("id", prod.getId());
//                            reqJson.put("id_C", prod.getInfo().get("id_C"));
//                            reqJson.put("fileList", prod.getFile00s().get("objData"));
//                            //调用file服务API
//                            long fileSize = fileClient.delCOSFile(reqJson);

                //容量卡可能会改，先注释
//                    Update update = new Update();
//                    //自减,powerup容量卡
//                    update.inc("powerup."+"capacity"+".used",- fileSize);
//
//                    if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//
//                        mongoTemplate.updateFirst(new Query(
//                                new Criteria("info.id_C").is(prod.getInfo().get("id_C"))
//                                        .and("info.ref").is("a-module")),update, Asset.class);
//
//                    }
                //}

            }
            // 2.删除es列表
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("lBProd");
            deleteByQueryRequest.setQuery(QueryBuilders.termQuery("id_P", hit.getSourceAsMap().get("id_P")));
            restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);


        }

        //}

        //}

    }

    public void removelSBOrder() throws IOException, ParseException {
//        final Calendar calendar = Calendar.getInstance();
//        if (calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE)) {
        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lSBOrder");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        //查询组别为1020的
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 查询条件  两方组别都是1020&& lST小于8
        boolQueryBuilder.must(QueryBuilders.termQuery("grp", "1020"));
        boolQueryBuilder.must(QueryBuilders.termQuery("grpB", "1020"));
        boolQueryBuilder.must(QueryBuilders.rangeQuery("lST").lt(8));
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit hit : search.getHits().getHits()) {

            //判断月份是否相同
//                boolean isSameMonth = genericUtils.isSameMonth(hit.getSourceAsMap().get("tmd").toString());
//
//                if (isSameMonth) {
//
//
//                } else {

            //1.删除mongdb
            Query query = new Query(new Criteria("_id").is(hit.getSourceAsMap().get("id_O")));
            query.fields().include("file00s").include("info");
            Order order = mongoTemplate.findOne(query, Order.class);

            //删除mdb
            DeleteResult remove = mongoTemplate.remove(query, Order.class);
            //判断有没有file00s卡片
//                    if (order.getFile00s() != null && order.getInfo().containsKey("id_C")) {
//
//                        HashMap<String, Object> reqJson = new HashMap<>();
//                        reqJson.put("id", order.getId());
//                        reqJson.put("id_C", order.getInfo().get("id_C"));
//                        reqJson.put("fileList", order.getFile00s().get("objData"));
//                        //调用file服务API
//                        long fileSize = fileClient.delCOSFile(reqJson);
            //容量卡可能会改，先注释
//                    Update update = new Update();
//                    //自减,powerup容量卡
//                    update.inc("powerup."+"capacity"+".used",- fileSize);
//
//                    if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//
//                        mongoTemplate.updateFirst(new Query(
//                                new Criteria("info.id_C").is(prod.getInfo().get("id_C"))
//                                        .and("info.ref").is("a-module")),update, order.class);
//
//                    }
            //}


            //判断结果大于0才进行删除列表
            if (remove.getDeletedCount() > 0) {

                // 2.删除es列表
                DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("lSBOrder");
                deleteByQueryRequest.setQuery(QueryBuilders.termQuery("id_O", hit.getSourceAsMap().get("id_O")));
                restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);

            }
        }
        // }
        //}
    }

    public void removelBAsset() throws IOException, ParseException {
//        final Calendar calendar = Calendar.getInstance();
//        if (calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE)) {
        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lBAsset");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        //组合查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 查询条件  lAT=2 grp = 1020
        boolQueryBuilder.must(QueryBuilders.termQuery("lAT", 2));
        boolQueryBuilder.must(QueryBuilders.termQuery("grp", "1020"));
        searchSourceBuilder.query(boolQueryBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        for (SearchHit hit : search.getHits().getHits()) {

            //判断月份是否相同
//                boolean isSameMonth = genericUtils.isSameMonth(hit.getSourceAsMap().get("tmd").toString());
//
//                if (isSameMonth) {
//
//
//                } else {
            //1.删除mongdb
            Query query = new Query(new Criteria("_id").is(hit.getSourceAsMap().get("id_A")));
            query.fields().include("file00s").include("info");
            Asset asset = mongoTemplate.findOne(query, Asset.class);
            //删除mdb
            DeleteResult remove = mongoTemplate.remove(query, Asset.class);
//                    if (asset.getFile00s() != null && asset.getInfo().containsKey("id_C")) {
//
//                        HashMap<String, Object> reqJson = new HashMap<>();
//                        reqJson.put("id", asset.getId());
//                        reqJson.put("id_C", asset.getInfo().get("id_C"));
//                        reqJson.put("fileList", asset.getFile00s().get("objData"));
//                        //调用file服务API
//                        long fileSize = fileClient.delCOSFile(reqJson);
            //容量卡可能会改，先注释
//                    Update update = new Update();
//                    //自减,powerup容量卡
//                    update.inc("powerup."+"capacity"+".used",- fileSize);
//
//                    if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//
//                        mongoTemplate.updateFirst(new Query(
//                                new Criteria("info.id_C").is(prod.getInfo().get("id_C"))
//                                        .and("info.ref").is("a-module")),update, asset.class);
//
//                    }
            // }

            //判断结果大于0才进行删除列表
            if (remove.getDeletedCount() > 0) {

                // 2.删除es列表
                DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("lBAsset");
                // 查询条件
                deleteByQueryRequest.setQuery(QueryBuilders.termQuery("id_A", hit.getSourceAsMap().get("id_A")));
                restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);

            }
        }
        // }
        //}
    }
}
