package com.cresign.timer.quartz;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.client.file.FileClient;
//import com.cresign.timer.service.ScriptTimer;
//import com.cresign.timer.service.StatService;
//import com.cresign.timer.utils.ElasticsearchUtils;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Ut;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Init;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class ESQuartz implements Job {

    @Autowired
    //@Qualifier("getRestHighLevelClient")//指定Bean
    private RestHighLevelClient restHighLevelClient;

//    @Autowired
//    private ElasticsearchUtils elasticsearchUtils;

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private Ut ut;

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private FileClient fileClient;

//    @Autowired
//    private StatService statService;
//
//    @Autowired
//    private ScriptTimer scriptTimer;

    /**
     * 定时器删除任务方法
     *
     * ##Params: jobExecutionContext 异常信息
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        jobExecutionContext.getJobDetail().getJobDataMap();

    }
//
//    private final String din18 = "0 0/5 * * * ?";
//    @Scheduled(cron = din18)
//    @SuppressWarnings("unchecked")
//    public void test() throws IOException {
//
////        //构建查询库
////        SearchRequest searchRequest = new SearchRequest("assetflow");
////        //构建搜索条件
////        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
////        searchSourceBuilder.from(0);
////        searchSourceBuilder.size(10000);
////        //组合查询条件
////        //BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
////
////        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
////        Date nowBeforeFiveMinutes = new Date(new Date().getTime()-300000);
////        String trim = sdf.format(nowBeforeFiveMinutes).trim();
////        //2.当前时间  当前时间 - 5分钟
////        //boolQueryBuilder.must(QueryBuilders.rangeQuery("tmd").gte(trim));
////        //from（）是时间格式，.gte（）.lte（）  时间范围  比如 gte10:20   lte10:25
////        searchSourceBuilder.query(QueryBuilders.rangeQuery("tmd")
////                .from(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()).gte(trim)
////                .lte(dateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate())));
////        System.out.println(trim);
////        System.out.println(dateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
////        searchRequest.source(searchSourceBuilder);
////        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
////
////        for (SearchHit hit : search.getHits().getHits()) {
////            System.out.println(hit.getSourceAsMap());
////            Map<String,Object> data = (Map<String, Object>) hit.getSourceAsMap().get("data");
////
////            Query oStockQ = new Query(new Criteria("_id").is(hit.getSourceAsMap().get("id_O")));
////            oStockQ.fields().include("oStock");
////            Order order = mongoTemplate.findOne(oStockQ, Order.class);
////
////            JSONObject oStock = new JSONObject();
////            if (order.getOStock() == null){
////                oStock.put("sumQty",data.get("qty"));
////
////            }else{
////
////                oStock.put("sumQty",order.getOStock().getDouble("sumQty") + Double.parseDouble(data.get("qty").toString()));
////            }
////
////            mongoTemplate.updateFirst(oStockQ,new Update().set("oStock",oStock),Order.class);
////            System.out.println(order.getId());
////
////        }
//
//    }






    //0 * * * * ?
    //每月  月底删除数据,每个月月底23点启动.//0 00 23 L * ? java不支持L字符改成0 00 23 28-31 * ?（方法下面判断当月的最后日期）
    private final String din1 = "0 0 18 * * ?";
    @Scheduled(cron = din1)
    @SuppressWarnings("unchecked")
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
//                    if (prod.getFile00s() != null && prod.getInfo().containsKey("id_C")){
//
//                        HashMap<String, Object> reqJson = new HashMap<>();
//                        reqJson.put("id",prod.getId());
//                        reqJson.put("id_C",prod.getInfo().get("id_C"));
//                        reqJson.put("fileList",prod.getFile00s().get("objData"));
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
//                                        .and("info.ref").is("a-module")),update, Asset.class);
//
//                    }
                   // }



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
            //}
        //}

    }

    //每月  月底删除数据,每个月月底23点启动
    private final String din2 = "0 0 18 * * ?";
    @Scheduled(cron = din2)
    @SuppressWarnings("unchecked")
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
    //每月  月底删除数据,每个月月底23点启动
    private final String din3 = "0 0 18 * * ?";
    @Scheduled(cron = din3)
    @SuppressWarnings("unchecked")
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

    //每月  月底删除数据,每个月月底23点启动
    private final String din4 = "0 0 18 * * ?";
    @Scheduled(cron = din4)
    @SuppressWarnings("unchecked")
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

    //聚合公司申请的 APPKEY
    public static final String APPKEY ="59e03ed07942aef5ccd202ab23e134be";
    static Double[] eRate = new Double[14];

    // 此汇率仅供参考，更新时间2分钟，以中国银行各分行实际交易汇率为准，不构成投资建议。投资者据此买卖，风险自担
    //0 0 0 1 * ?  每月1号上午0点0分触发
    private final String din5 = "0 0 0 1 * ?";
    @Scheduled(cron = din5)
    @SuppressWarnings("unchecked")
    public void currencyRate(){
        String result =null;
        String url ="http://web.juhe.cn:8080/finance/exchange/frate";//请求接口地址   查看外汇
        Map params = new HashMap();//请求参数
        params.put("key",APPKEY);//APP Key
        params.put("type","");//两种格式(0或者1,默认为0)
        //查询条件
        Query query = new Query(new Criteria("_id").is("cn_java").and("exchangeRate").exists(true));

        try {
            result = ut.net(url, params, "POST");
            //将字符串转化成JSON对象
            JSONObject object = JSONObject.parseObject(result);
            //转化成JSON数组
            JSONArray resultList = object.getJSONArray("result");
            //取出JSON数组中的值
            for (int i=0; i<resultList.size();i++){
                Update update = new Update();
                // buyPic 买入价
                // closePri 最新价
                // 澳元美元
                JSONObject json = (JSONObject) resultList.get(i);
//                String AUD = json.getString("data1");
//                (AUD);
                JSONObject rJson1 = (JSONObject) json.get("data1");

                eRate[1] = rJson1.getDouble("buyPic");

                //new BigDecimal(Double.toString(eRate[1]))   超过16位有效位的数进行精确的运算
                update.set("exchangeRate.AUD",new BigDecimal(Double.toString(eRate[1])));
                //美元指数
//                String DINIW = json.getString("data2");
//                (DINIW);
                JSONObject rJson2 = (JSONObject) json.get("data2");

                eRate[2] = rJson2.getDouble("buyPic");

                update.set("exchangeRate.DINIW",new BigDecimal(Double.toString(eRate[2])));
                //欧元美元
//                String EUR = json.getString("data3");
//                (EUR);
                JSONObject rJson3 = (JSONObject) json.get("data3");

                eRate[3] = rJson3.getDouble("buyPic");

                update.set("exchangeRate.EUR",new BigDecimal(Double.toString(eRate[3])));
                //英镑美元
//                String GBP = json.getString("data4");
//                (GBP);
                JSONObject rJson4 = (JSONObject) json.get("data4");

                eRate[4] = rJson4.getDouble("buyPic");

                update.set("exchangeRate.GBP",new BigDecimal(Double.toString(eRate[4])));
                // 新西兰元美元
//                String NZD = json.getString("data5");
//                (NZD);
                JSONObject rJson5 = (JSONObject) json.get("data5");

                eRate[5] = rJson5.getDouble("buyPic");

                update.set("exchangeRate.NZD",new BigDecimal(Double.toString(eRate[5])));
                // 美元加元
//                String CAD = json.getString("data6");
//                (CAD);
                JSONObject rJson6 = (JSONObject) json.get("data6");

                eRate[6] = rJson6.getDouble("buyPic");

                update.set("exchangeRate.CAD",new BigDecimal(Double.toString(eRate[6])));
                // 美元瑞郎
//                String CHF = json.getString("data7");
//                (CHF);
                JSONObject rJson7 = (JSONObject) json.get("data7");

                eRate[7] = rJson7.getDouble("buyPic");

                update.set("exchangeRate.CHF",new BigDecimal(Double.toString(eRate[7])));
                //美元人民币
//                String CNY = json.getString("data8");
//                (CNY);
                JSONObject rJson8 = (JSONObject) json.get("data8");

                eRate[8] = rJson8.getDouble("buyPic");

                update.set("exchangeRate.CNY",new BigDecimal(Double.toString(eRate[8])));
                //美元港元
//                String HKD = json.getString("data9");
//                (HKD);
                JSONObject rJson9 = (JSONObject) json.get("data9");

                eRate[9] = rJson9.getDouble("buyPic");

                update.set("exchangeRate.HKD",new BigDecimal(Double.toString(eRate[9])));
                //美元日元
//                String JPY = json.getString("data10");
//                (JPY);
                JSONObject rJson10 = (JSONObject) json.get("data10");

                eRate[10] = rJson10.getDouble("buyPic");

                update.set("exchangeRate.JPY",new BigDecimal(Double.toString(eRate[10])));
                //美元马币
//                String MYR = json.getString("data11");
//                (MYR);
                JSONObject rJson11 = (JSONObject) json.get("data11");

                eRate[11] = rJson11.getDouble("buyPic");

                update.set("exchangeRate.MYR",new BigDecimal(Double.toString(eRate[11])));
                //美元新加坡元
//                String SGD = json.getString("data12");
//                (SGD);
                JSONObject rJson12 = (JSONObject) json.get("data12");

                eRate[12] = rJson12.getDouble("buyPic");

                update.set("exchangeRate.SGD",new BigDecimal(Double.toString(eRate[12])));
                //美元台币
//                String TWD = json.getString("data13");
//                (TWD);
                JSONObject rJson13 = (JSONObject) json.get("data13");

                eRate[13] = rJson13.getDouble("buyPic");

                update.set("exchangeRate.TWD",new BigDecimal(Double.toString(eRate[13])));

                if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                    mongoTemplate.updateFirst(query, update, Init.class);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //安全库存预警
    private final String din6 = "0 0 0 * * ?";
    @Scheduled(cron = din6)
    @SuppressWarnings("unchecked")
    public void qtySafe() throws IOException {
        //查询条件
        Query query = new Query(new Criteria("qtySafe").exists(true).and("info.lAT").is(2));
        query.fields().include("info").include("qtySafe");
        List<Asset> assets = mongoTemplate.find(query, Asset.class);
        for (int i = 0; i < assets.size(); i++) {
            //不等于空
            if (assets.get(i).getQtySafe() != null){

                //bisWarning == false  lAT = 0  wn2qty != null
                if (assets.get(i).getQtySafe().get("bisWarning").equals(false) &&
                        assets.get(i).getInfo().getLAT().equals(2) &&
                        assets.get(i).getInfo().getWn2qty() != null){
                    //条件满足，发ES   安全最低数量 大于 现在数量
                    if (Integer.parseInt(assets.get(i).getQtySafe().get("wn2qtyMin").toString()) > Integer.parseInt(assets.get(i).getInfo().getWn2qty().toString())){

                        Query queryID = new Query(new Criteria("_id").is(assets.get(i).getId()));
                        Update update = new Update();
                        update.set("qtySafe.bisWarning",true);

                        if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                            mongoTemplate.updateFirst(queryID, update, Asset.class);
                        }

                        JSONObject hashMap = new JSONObject();
                        hashMap.put("wn2qtychg",Double.parseDouble(assets.get(i).getQtySafe().get("wn2qtyBuy").toString()));
                        hashMap.put("subtype",6);hashMap.put("id_to",assets.get(i).getId());hashMap.put("id_from","");
                        hashMap.put("id_P",assets.get(i).getInfo().getId_P());
//                        hashMap.put("id_O",assets.get(i).getInfo().get("id_O"));
                        hashMap.put("id_C",assets.get(i).getInfo().getId_C());
                        hashMap.put("ref",assets.get(i).getInfo().getRef());
                        hashMap.put("wrdN",assets.get(i).getInfo().getWrdN());
                        hashMap.put("pic",assets.get(i).getInfo().getPic());
                        hashMap.put("wrddesc",assets.get(i).getInfo().getWrddesc());
//                        hashMap.put("wn4price",Double.parseDouble(assets.get(i).getInfo().get("wn4price").toString()));
//                        hashMap.put("wn2qc",assets.get(i).getInfo().get("wn2qc"));//hashMap.put("batchQtySafe", "");
//                        hashMap.put("grpU","");hashMap.put("grpUB","");
                        hashMap.put("tmk", dateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
                        hashMap.put("tmd",dateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));


                        dbUtils.addES(hashMap,"assetflow");

                    }

                }

            }

        }

    }

    //查询powerup卡片容量是否超出，超出则修改状态
    private final String din7 = "0 0 0 * * ?";
    @Scheduled(cron = din7)
    @SuppressWarnings("unchecked")
    public  void powerup() {
        //查询条件
        Query query = new Query(new Criteria("powerup").exists(true));
        query.fields().include("powerup");
        List<Asset> assets = mongoTemplate.find(query, Asset.class);
        for (int i = 0; i < assets.size(); i++) {
            //不等于空
            if (assets.get(i).getPowerup() != null) {
                //capacity
                if (assets.get(i).getPowerup().get("capacity") != null){
                   HashMap<String, Object> capacity = (HashMap<String, Object>) assets.get(i).getPowerup().get("capacity");

                   if (capacity.get("used") == null){
                        continue;
                   }
                   if (capacity.get("total") == null){
                       continue;
                   }

                   //已使用量大于总容量   修改状态
                   if (Long.parseLong(capacity.get("used").toString()) > Long.parseLong(capacity.get("total").toString())){
                       Query modeQ = new Query(
                               new Criteria("_id").is(assets.get(i).getId()));

                       Update update = new Update();
                       capacity.put("status",false);

                       update.set("powerup.capacity",capacity);


                       if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                           mongoTemplate.updateFirst(modeQ,update, Asset.class);
                       }

                   }

                }

            }
        }
    }

//    /**
//     * 每分钟统计任务完成情况
//     * @Author Rachel
//     * @Data 2021/08/12
//     * @Card
//     **/
//    private final String statAssetflow = "0 0/1 * * * ?";
//    @Scheduled(cron = statAssetflow)
//    public void statAssetflow() throws IOException, ScriptException {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.add(Calendar.MINUTE, - 1);
//        Date date = calendar.getTime();
//        String day = sdf.format(date);
//        System.out.println("过去一分钟：" + day);
//        String dateLog = day + ":00";
//        String dateLogs = day + ":59";
//        System.out.println(dateLog);
//        SearchSourceBuilder builderLog = new SearchSourceBuilder();
//        BoolQueryBuilder queryLog = QueryBuilders.boolQuery();
//        queryLog.must(QueryBuilders.rangeQuery("tmd").from(dateLog, true).to(dateLogs, true)).must(QueryBuilders.termQuery("subType.keyword", "qtyChg"));
//        builderLog.query(queryLog).from(0).size(10000);
//        SearchRequest requestLog = new SearchRequest("assetflow").source(builderLog);
//        SearchResponse responseLog = restHighLevelClient.search(requestLog, RequestOptions.DEFAULT);
//        SearchHit[] hitsLog = responseLog.getHits().getHits();
//        SearchHit[] hitsProd = hitsLog.clone();
//        System.out.println(hitsLog.length);
//        System.out.println(hitsLog);
//        if (hitsLog.length != 0) {
//            JSONArray jsonLog = new JSONArray();
//            JSONArray jsonProd = new JSONArray();
//            for (int i = 0; i < hitsLog.length; i++) {
//                JSONObject hitLog = (JSONObject) JSON.parse(hitsLog[i].getSourceAsString());
//                JSONObject jsonL = new JSONObject();
//                jsonL.put("id_O", hitLog.getString("id_O"));
//                jsonL.put("index", hitLog.getInteger("oIndex"));
//                jsonL.put("wn2qtynow", hitLog.getJSONObject("data").getDouble("wn2qtynow"));
//                jsonLog.add(jsonL);
//                JSONObject hitProd = (JSONObject) JSON.parse(hitsProd[i].getSourceAsString());
//                JSONObject jsonP = new JSONObject();
//                jsonP.put("id_O", hitProd.getString("id_O"));
//                jsonP.put("index", hitProd.getInteger("oIndex"));
//                jsonP.put("wn2qtynow", 0);
//                jsonProd.add(jsonP);
//                System.out.println(hitLog.getString("tmd") + ", " + hitLog.getString("subType"));
//            }
//            System.out.println(jsonLog);
//            System.out.println(jsonProd);
//            for (int i = 0; i < jsonProd.size(); i++) {
//                for (int j = i + 1; j < jsonProd.size(); j++) {
//                    if (jsonProd.getJSONObject(i).getString("id_O").equals(jsonProd.getJSONObject(j).getString("id_O")) &&
//                            jsonProd.getJSONObject(i).getInteger("index") == jsonProd.getJSONObject(j).getInteger("index")) {
//                        jsonProd.remove(j);
//                        j = j - 1;
//                    }
//                }
//            }
//            System.out.println(jsonProd);
//            for (int i = 0; i < jsonProd.size(); i++) {
//                for (int j = 0; j < hitsLog.length; j++) {
//                    JSONObject json = (JSONObject) JSON.parse(hitsLog[j].getSourceAsString());
//                    if (jsonProd.getJSONObject(i).getString("id_O").equals(jsonLog.getJSONObject(j).getString("id_O")) &&
//                            jsonProd.getJSONObject(i).getInteger("index") == jsonLog.getJSONObject(j).getInteger("index")) {
//                        Double wn2qtynow = jsonProd.getJSONObject(i).getDouble("wn2qtynow");
//                        wn2qtynow += jsonLog.getJSONObject(j).getDouble("wn2qtynow");
//                        jsonProd.getJSONObject(i).put("wn2qtynow", wn2qtynow);
//                    }
//                }
//            }
//            System.out.println("jsonProd=" + jsonProd);
//            UpdateResult result = null;
//
//
//
//
//            for (int i = 0; i < jsonProd.size(); i++) {
//
//                Query query = new Query(new Criteria("_id").is(jsonProd.getJSONObject(i).getString("id_O")));
//                Order order = mongoTemplate.findOne(query, Order.class);
//                String id_P = order.getOItem().getJSONArray("objItem").getJSONObject(jsonProd.getJSONObject(i).getInteger("index")).getString("id");
//                System.out.println("wn2qtynow=" + jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                JSONObject oStock = order.getOStock();
//
//
//                if (oStock == null) {
//                    //oStock为空，创建objData数组, 添加数组对象
//                    JSONArray array = new JSONArray();
//                    JSONObject object = new JSONObject();
//                    object.put("wn2qtynow", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    object.put("wn2qtymade", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    object.put("id_P", id_P);
//                    array.set(jsonProd.getJSONObject(i).getInteger("index"), object);
//                    Update update = new Update();
//                    update.set("oStock.objData", array);
//                    update.push("view", "oStock");
//                    UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Order.class);
//                    System.out.println("1:" + updateResult);
//                } else if (oStock.getJSONArray("objData") == null) {
//                    //oStock.objData为空，创建objData数组, 添加数组对象
//                    JSONArray array = new JSONArray();
//                    JSONObject object = new JSONObject();
//                    object.put("wn2qtynow", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    object.put("wn2qtymade", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    object.put("id_P", id_P);
//                    array.set(jsonProd.getJSONObject(i).getInteger("index"), object);
//                    Update update = new Update();
//                    update.set("oStock.objData", array);
//                    UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Order.class);
//                    System.out.println("2:" + updateResult);
//                } else if (oStock.getJSONArray("objData").size() - 1 < jsonProd.getJSONObject(i).getInteger("index")) {
//                    //oStock.objData.指定下标越界, 添加数组对象
//                    JSONObject object = new JSONObject();
//                    object.put("wn2qtynow", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    object.put("wn2qtymade", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    object.put("id_P", id_P);
//                    Update update = new Update();
//                    update.set("oStock.objData." + jsonProd.getJSONObject(i).getInteger("index"), object);
//                    UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Order.class);
//                    System.out.println("3:" + updateResult);
//                } else if (oStock.getJSONArray("objData").getJSONObject(jsonProd.getJSONObject(i).getInteger("index")) == null) {
//                    //oStock.objData.指定下标为空, 添加数组对象
//                    JSONObject object = new JSONObject();
//                    object.put("wn2qtynow", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    object.put("wn2qtymade", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    object.put("id_P", id_P);
//                    Update update = new Update();
//                    update.set("oStock.objData." + jsonProd.getJSONObject(i).getInteger("index"), object);
//                    UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Order.class);
//                    System.out.println("4:" + updateResult);
//                } else {
//                    //oStock.objData.指定下标不为空，增加数量
//                    Update update = new Update();
//                    update.inc("oStock.objData." + jsonProd.getJSONObject(i).getInteger("index") + ".wn2qtynow", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    update.inc("oStock.objData." + jsonProd.getJSONObject(i).getInteger("index") + ".wn2qtymade", jsonProd.getJSONObject(i).getDouble("wn2qtynow"));
//                    UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Order.class);
//                    System.out.println("5:" + updateResult);
//                }
//            }
//            System.out.println("========================================");
//        }
//    }

//    public void scrpitEngine(JSONObject jsonProd) throws ScriptException, IOException {
//        Query queryOrder = new Query(new Criteria("_id").is(jsonProd.getString("id_O")));
//        Order order = mongoTemplate.findOne(queryOrder, Order.class);
//        JSONArray jsonObjData = new JSONArray();
//        if (order.getOTrigger() != null) {
//            jsonObjData = order.getOTrigger().getJSONArray("objData");
//        }
//        JSONArray objItem = order.getOItem().getJSONArray("objItem");
//        JSONObject jsonOrder = (JSONObject) JSON.toJSON(order);
//        System.out.println(jsonObjData);
//        System.out.println(jsonOrder);
//        //遍历执行订单所有scriptEngine
//        for (int i = 0; i < jsonObjData.size(); i++) {
//            ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
//            Compilable compilable = (Compilable) engine;
//            Bindings bindings = engine.createBindings();
//            CompiledScript JSFunction = compilable.compile(jsonObjData.getJSONObject(i).getString("script"));
//            JSONArray jsonVarDesc = jsonObjData.getJSONObject(i).getJSONArray("varDesc");
//            System.out.println("varDesc=" + jsonVarDesc);
//            //遍历传参到script
//            for (int j = 0; j < jsonVarDesc.size(); j++) {
//                String varDesc = jsonVarDesc.getString(j);
//                //正则判断是否数字
//                Pattern pattern = Pattern.compile("-?[0-9]+(\\\\.[0-9]+)?");
//                Matcher matcher = pattern.matcher(varDesc);
//                //判断字符串是否是数字
//                if (matcher.matches()) {
//                    bindings.put("op" + j, Double.parseDouble(varDesc));
//                    System.out.println("op" + j + "=" + Double.parseDouble(varDesc));
//                } else {
//                    //根据.拆分,第一个类型是jsonObject，最后一个类型是double
//                    String[] varDescs = varDesc.split("\\.");
//                    System.out.println(varDescs.length);
//                    //拆分成两份
//                    if (varDescs.length == 2) {
//                        bindings.put("op" + j, jsonOrder.getJSONObject(varDescs[0]).getDouble(varDescs[1]));
//                        System.out.println("op" + j + "=" + jsonOrder.getJSONObject(varDescs[0]).getDouble(varDescs[1]));
//                    //拆分成多份
//                    } else {
//                        JSONObject cloneOrder = jsonOrder.getJSONObject(varDescs[0]);
//                        for (int k = 1; k < varDescs.length - 1; k++) {
//                            System.out.println("cloneOrder=" + cloneOrder);
//                            //根据[拆分
//                            String[] isArray = varDescs[k].split("\\[");
//                            System.out.println("length=" + isArray.length);
//                            //拆分成一份类型为jsonObject
//                            if (isArray.length == 1) {
//                                cloneOrder = cloneOrder.getJSONObject(isArray[0]);
//                            //拆分成两份类型为jsonArray
//                            } else if (isArray.length == 2) {
//                                String[] nums = isArray[1].split("]");
//                                System.out.println("numlength=" + nums.length);
//                                cloneOrder = cloneOrder.getJSONArray(isArray[0]).getJSONObject(Integer.parseInt(nums[0]));
//                            }
//                        }
//                        //传入参数
//                        bindings.put("op" + j, cloneOrder.getDouble(varDescs[varDescs.length - 1]));
//                        System.out.println("op" + j + "=" + cloneOrder.getDouble(varDescs[varDescs.length - 1]));
//                    }
//                }
//            }
//
//            Boolean result = (Boolean) JSFunction.eval(bindings);
//            System.out.println(result);
//            if (result && jsonObjData.getJSONObject(i).getBoolean("isActive")) {
//                //执行完修改状态isDone为false
//                Update updateOrder = new Update();
//                updateOrder.set("oTrigger.objData." + i + ".isActive", false);
//                UpdateResult updateResult = mongoTemplate.updateFirst(queryOrder, updateOrder, Order.class);
//                System.out.println(updateResult.getModifiedCount());
//                //构建日志内容
//                LogFlow logFlow = new LogFlow();
//                logFlow.setId_C(objItem.getJSONObject(jsonProd.getInteger("index")).getString("id_CP"));
//                logFlow.setId_O(order.getId());
//                logFlow.setIndex(jsonProd.getInteger("index"));
//                logFlow.setId_P(objItem.getJSONObject(jsonProd.getInteger("index")).getString("id_P"));
//                logFlow.setSubType("notify");
//                logFlow.setLogType("assetflow");
//                logFlow.setLang("cn");
//                logFlow.setTmd(dateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//                logFlow.setZcndesc(jsonObjData.getJSONObject(i).getString("print"));
//                //发送日志
////                LogUtil.assetflow(logFlow);
//                IndexRequest indexRequest = new IndexRequest(logFlow.getLogType());
//                indexRequest.source(JSON.toJSONString(logFlow), XContentType.JSON);
//                indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
//                IndexResponse index = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//                System.out.println(index);
//            }
//        }
//    }
/////////////////////////////////////////////Start Checking from here //////////////////////////////////////
    /**
     * 每天删除7天前上传的文件
     * @Author Rachel
     * @Date 2021/09/15
     * @Card
     **/
    private final String delFiles = "0 0 0 * * ?";
    @Scheduled(cron = delFiles)
    @SuppressWarnings("unchecked")
    public void delFiles() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, - 7);
        Date date = calendar.getTime();
        String day = sdf.format(date);
        System.out.println("过去七天："+day);
        fileClient.delDownload(day);
    }

    /**
     * 生产测试数据
     * @Author Rachel
     * @Data 2021/10/04
     * @Return
     * @Card
     **/
//    private final String productionData = "0/10 * * * * ?";
//    @Scheduled(cron = productionData)
//    @SuppressWarnings("unchecked")
//    public void productionData() throws IOException {
//        Random random = new Random();
//        int i = random.nextInt(4);
//        Query query = new Query(new Criteria("info.id_C").is("6141b6797e8ac90760913fd0"));
//        List<Order> orders = mongoTemplate.find(query, Order.class);
//        Order order = orders.get(i);
//        String id_O = order.getId();
//        String id_P = order.getOItem().getJSONArray("objItem").getJSONObject(0).getString("id");
//        String grpU = order.getInfo().getString("grpU");
//        String id_U = "6110c5bb9ddc3d5bfcdc3355";
//        String id_C = "6141b6797e8ac90760913fd0";
//        int wn2qtynow = random.nextInt(10) + 1;
//        System.out.println("wn2qtynow=" + wn2qtynow);
//        JSONObject data = new JSONObject();
//        data.put("wn2qtynow", wn2qtynow);
//        LogFlow logFlow = new LogFlow();
//        logFlow.setId_O(id_O);
//        logFlow.setOIndex(0);
//        logFlow.setId_P(id_P);
//        logFlow.setGrpU(grpU);
//        logFlow.setId_U(id_U);
//        logFlow.setId_C(id_C);
//        logFlow.setSubType("qtyChg");
//        logFlow.setLogType("assetflow");
//        logFlow.setLang("cn");
//        logFlow.setTmd(dateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//        logFlow.setData(data);
////        LogUtil.assetflow(logFlow);
//        GetIndexRequest request = new GetIndexRequest(logFlow.getLogType());
//        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
//        if (!exists) {
//            CreateIndexRequest requestC = new CreateIndexRequest(logFlow.getLogType());
//            restHighLevelClient.indices().create(requestC, RequestOptions.DEFAULT);
//        }
//        IndexRequest requestI = new IndexRequest(logFlow.getLogType());
//        requestI.source(JSON.toJSONString(logFlow), XContentType.JSON);
//        requestI.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
//        IndexResponse index = restHighLevelClient.index(requestI, RequestOptions.DEFAULT);
//        System.out.println(index);
//    }

    /**
     * 每分钟检查oTrigger任务
     * @Author Rachel
     * @Date 2021/11/13
     * @Return
     * @Card
     **/
//    private final String oTrigger = "0/30 * * * * ?";
//    @Scheduled(cron = "0/30 * * * * ?")
    //KEV how to set action / money / quality flow checker @ 35s, 40s, 45s??
//    @SuppressWarnings("unchecked")
//    public void oTrigger() throws IOException, ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        Calendar calendar = Calendar.getInstance();
//        Date date = new Date();
//        // endTime = now Time
//        String endTime = sdf.format(date);
//        calendar.setTime(date);
//        calendar.add(Calendar.SECOND, - 30);
//        // Setup startTime which is 30sec earlier
//        String startTime = sdf.format(calendar.getTime());
//        System.out.println("过去30秒：" + startTime + "~" + endTime);
//
//        //get log from assetflow
//        BoolQueryBuilder queryLog = QueryBuilders.boolQuery();
//        queryLog.must(QueryBuilders.rangeQuery("tmd").from(startTime, true).to(endTime, false)).must(QueryBuilders.termQuery("subType", "qtyChg"));
//
//        //need a Util to send query and return SearchHit[]
//        SearchSourceBuilder builderLog = new SearchSourceBuilder();
//        builderLog.query(queryLog).from(0).size(10000);
//        SearchRequest assetflowRequest = new SearchRequest("assetflow").source(builderLog);
//        SearchResponse assetflowResponse = restHighLevelClient.search(assetflowRequest, RequestOptions.DEFAULT);
//        SearchHit[] assetflowHits = assetflowResponse.getHits().getHits();
//
//
//        System.out.println("assetflowResponse=" + assetflowResponse);
//        System.out.println("length=" + assetflowHits.length);
//
//
//        // For each hit in assetflow, updateOStock + scriptEngine is mixed here
//        if (assetflowHits.length > 0) {
//            //Why HashSet not JSONArray
//            JSONArray setId_O = new JSONArray();
//            JSONObject jsonId_O = new JSONObject();
//            JSONArray arrayTrigger = new JSONArray();
//
//            //KEV 1st Loop is to get all the id_O [], need index?
//            for (int i = 0; i < assetflowHits.length; i++) {
//                JSONObject jsonHit = (JSONObject) JSON.parse(assetflowHits[i].getSourceAsString());
//                String id_O = jsonHit.getString("id_O");
//                Integer index = jsonHit.getInteger("index");
//                String id_P = jsonHit.getString("id_P");
//                Double wn2qtynow = jsonHit.getJSONObject("data").getDouble("wn2qtynow");
//                setId_O.add(id_O);
//                // If this is a new id_O item,
//                if (jsonId_O.getJSONArray(id_O) == null) {
//                    JSONArray arrayOStock = new JSONArray();
//                    JSONObject jsonOStock = new JSONObject();
//                    jsonOStock.put("id_P", id_P);
//                    jsonOStock.put("wn2qtynow", wn2qtynow);
//                    jsonOStock.put("wn2qtymade", wn2qtynow);
//                    arrayOStock.set(index, jsonOStock);
//                    jsonId_O.put(id_O, arrayOStock);
//                } else {
//                    // it is already a match, but still need to check index
//                    JSONArray arrayOStock = jsonId_O.getJSONArray(id_O);
//                    System.out.println("size=" + arrayOStock.size() + ",index=" + index);
//                    if (arrayOStock.size() - 1 > index && arrayOStock.getJSONObject(index) != null) {
//                        JSONObject jsonOStock = arrayOStock.getJSONObject(index);
//                        jsonOStock.put("wn2qtynow", jsonOStock.getDouble("wn2qtynow") + wn2qtynow);
//                        jsonOStock.put("wn2qtymade", jsonOStock.getDouble("wn2qtynow") + wn2qtynow);
//                        arrayOStock.set(index, jsonOStock);
//                    } else {
//                        JSONObject jsonOStock = new JSONObject();
//                        jsonOStock.put("id_P", id_P);
//                        jsonOStock.put("wn2qtynow", wn2qtynow);
//                        jsonOStock.put("wn2qtymade", wn2qtynow);
//                        arrayOStock.set(index, jsonOStock);
//                    }
//                    jsonId_O.put(id_O, arrayOStock);
//                }
//            }
//
//            //till now, arrayOStock, jsonId_O, jsonOStock are created and all stored into jsonId_O
//            //Pre-process of wn2qtynow
//
//            System.out.println("setId_O=" + setId_O);
//            System.out.println("jsonId_O=" + jsonId_O);
//
//            // Get ALL orders [] by a mongo Query
//            Query queryOrder = new Query(new Criteria("_id").in(setId_O));
//            //KEV NEED TO put in include oTrigger, oStock
//            queryOrder.fields().include("oTrigger").include("oStock").include("id");
//            List<Order> orders = mongoTemplate.find(queryOrder, Order.class);
//
//            //loop thru all order and retreive oTrigger
//            for (int i = 0; i < orders.size(); i++) {
//                Order order = orders.get(i);
//                if (order.getOTrigger() != null) {
//                    JSONArray arrayObjAssetflow = order.getOTrigger().getJSONArray("objAssetflow");
//                    if (arrayObjAssetflow != null) {
//                        for (int j = 0; j < arrayObjAssetflow.size(); j++) {
//                            JSONObject jsonObjAsset = arrayObjAssetflow.getJSONObject(j);
//                            if (jsonObjAsset.getBoolean("isActive") != null) {
//                                if (jsonObjAsset.getBoolean("isActive")) {
//                                    jsonObjAsset.put("id", order.getId());
//                                    // KEV added ! to isActive
//                                    // need to add id_P? id_C? id_CB? to make sure arrayTrigger runs well
//                                    // need to add index_O
//                                    jsonObjAsset.put("table", "Order");
//                                    jsonObjAsset.put("key", "oTrigger.objAssetflow." + j);
//                                    //add a trigger from oTrigger
//                                    arrayTrigger.add(jsonObjAsset);
//                                } // isActive change name
//                            }  else {
//                                // Doesn't make sense, well, this means it is a repeatable trigger
//                                arrayTrigger.add(jsonObjAsset);
//                            }
//                        }
//                    }
//                }
//                // This is for updateOStock
//                // I don't think it should happen here, should use oTrigger
//                // SHOULD move these lines to a updateOStock API in scriptEngine
//                JSONArray arrayOStock = jsonId_O.getJSONArray(order.getId());
//                System.out.println("arrayOStock=" + arrayOStock);
//                Query query = new Query(new Criteria("_id").is(order.getId()));
//                Update update = new Update();
//                JSONObject oStock = order.getOStock();
//                if (oStock == null) {
//                    //oStock为空，创建objData数组, 添加数组对象
//                    System.out.println("===0===");
//                    JSONObject jsonOStock = new JSONObject();
//                    jsonOStock.put("objData", arrayOStock);
//                    update.set("oStock", jsonOStock);
//                } else if (oStock.getJSONArray("objData") == null) {
//                    System.out.println("===1===");
//                    update.set("oStock.objData", arrayOStock);
//                } else {
//                    for (int j = 0; j < arrayOStock.size(); j++) {
//                        System.out.println(j + ":" + arrayOStock.getJSONObject(j));
//                        if (arrayOStock.getJSONObject(j) != null) {
//                            System.out.println(oStock.getJSONArray("objData").size() > j);
//                            if (oStock.getJSONArray("objData").size() > j) {
//                                System.out.println(oStock.getJSONArray("objData").getJSONObject(j) != null);
//                            }
//                            if (oStock.getJSONArray("objData").size() > j && oStock.getJSONArray("objData").getJSONObject(j) != null) {
//                                System.out.println("===2===");
//                                update.inc("oStock.objData." + j + ".wn2qtynow", arrayOStock.getJSONObject(j).getDouble("wn2qtynow"));
//                                update.inc("oStock.objData." + j + ".wn2qtymade", arrayOStock.getJSONObject(j).getDouble("wn2qtynow"));
//                            } else {
//                                System.out.println("===3===");
//                                update.set("oStock.objData." + j, arrayOStock.getJSONObject(j));
//                            }
//                        }
//                    }
//                }
//                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Order.class);
//                System.out.println("updateResult=" + updateResult);
//            }
//            System.out.println("arrayTrigger=" + arrayTrigger);
//            // Here we got a List of all the Triggers that need to use script Engine
//        Object result = scriptTimer.scriptEngine(arrayTrigger);
//        }
//    }

    /**
     * 每分钟检查cTrigger预约任务
     * @Author Rachel
     * @Date 2021/10/26
     * @Return
     * @Card
     **/
//    private final String cTriggerAppointment = "0 * * * * ?";
//    @Scheduled(cron = cTriggerAppointment)
//    @SuppressWarnings("unchecked")
//    public void cTriggerAppointment() throws IOException, ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//        //SubType should be "reserve", not 1min, use a Util
//        queryBuilder.must(QueryBuilders.termQuery("subType", "1minutes"));
//        sourceBuilder.query(queryBuilder).from(0).size(10000);
//        SearchRequest searchRequest = new SearchRequest("timeflow").source(sourceBuilder);
//        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        SearchHit[] hits = searchResponse.getHits().getHits();
//
//        //The result is multi-company, need to check what is in timeflow
//        //Split timeflow and timerepeat
//
//
//        // list of id_A (id_C's a-auth)
//        HashSet setId_A = new HashSet();
//        // only store all the ref (token) of tasks by each comp
//        JSONObject allTask = new JSONObject();
//        System.out.println("cTrigger - length=" + hits.length);
//        for (int i = 0; i < hits.length; i++) {
//            JSONObject jsonHit = (JSONObject) JSON.parse(hits[i].getSourceAsString());
//            String id_A = jsonHit.getString("id_A");
//            String ref = jsonHit.getJSONObject("data").getString("ref");
//            System.out.println("ref=" + ref);
//            //Check if id_A is a repeated
//            if (allTask.getJSONArray(id_A) == null) {
//                JSONArray arrayRefByID_A = new JSONArray();
//                //put ref into allTask
//                arrayRefByID_A.add(ref);
//                allTask.put(id_A, arrayRefByID_A);
//            } else {
//                // If same id_A, means 1 company has multi-task in this same min
//                JSONArray arrayRefByID_A = allTask.getJSONArray(id_A);
//                arrayRefByID_A.add(ref);
//                allTask.put(id_A, arrayRefByID_A);
//            }
//            setId_A.add(id_A);
//        }
//
//        //loop thru all id_A to get the very important cTrigger
//        Query queryAsset = new Query(new Criteria("_id").in(setId_A));
//        //KEV add includes cTrigger.objData
//        List<Asset> assets = mongoTemplate.find(queryAsset, Asset.class);
//        JSONArray arrayTrigger = new JSONArray();
//        for (int i = 0; i < assets.size(); i++) {
//            JSONArray arrayRef = allTask.getJSONArray(assets.get(i).getId());
//            for (int j = 0; j < arrayRef.size(); j++) {
//                String ref = arrayRef.getString(j);
//                JSONObject jsonTrigger = assets.get(i).getCTrigger().getJSONObject("objData").getJSONObject(ref);
//                if (jsonTrigger.getBoolean("isActive") != null) {
//                    if (jsonTrigger.getBoolean("isActive")) {
//                        //Here if it isActive(KEV added ! to indicate not Done)
//                        // add additional data
//                        jsonTrigger.put("id", assets.get(i).getId());
//                        jsonTrigger.put("table", "Asset");
//                        jsonTrigger.put("key", "cTrigger.objData." + ref);
//                        arrayTrigger.add(jsonTrigger);
//                    }
//                } else {
//                    // isActive cannot be null, throw error
//                    //arrayTrigger.add(jsonTrigger);
//                }
//            }
//        }
//
//        Object trigger = scriptTimer.scriptEngine(arrayTrigger);
//    }

    /**
     * 每5分钟检查cTrigger定时任务
     * @Author Rachel
     * @Date 2021/11/13
     * @Card
     **/
//    private final String cTriggerRepeat = "0 0/5 * * * ?";
//    @Scheduled(cron = "0 0/5 * * * ?")
//    @SuppressWarnings("unchecked")
//    public void cTriggerRepeat() throws IOException, ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        //获取时区
//        SimpleDateFormat sdf = new SimpleDateFormat("Z");
//        String format = sdf.format(new Date());
//        String timeZone = null;
//        //把时间转成UTC
//        if (format.startsWith("+")) {
//            timeZone = "-" + format.substring(1, 3);
//        } else {
//            timeZone = "+" + format.substring(1, 3);
//        }
//        System.out.println("format=" + format + "," + timeZone);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        //KEV our timezone in server is +8, if you adjust, it will be wrong
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
//        monthQueryBuilder.should(QueryBuilders.termQuery("data.month", calendar.get(Calendar.MONTH) + 1)).should(QueryBuilders.termQuery("data.month", "all"));
//        weekQueryBuilder.should(QueryBuilders.termQuery("data.week", calendar.get(Calendar.DAY_OF_WEEK))).should(QueryBuilders.termQuery("data.week", "all"));
//        dayQueryBuilder.should(QueryBuilders.termQuery("data.day", calendar.get(Calendar.DAY_OF_MONTH))).should(QueryBuilders.termQuery("data.day", "all"));
//        hourQueryBuilder.should(QueryBuilders.termQuery("data.hour", calendar.get(Calendar.HOUR_OF_DAY))).should(QueryBuilders.termQuery("data.hour", "all"));
//        minuteQueryBuilder.should(QueryBuilders.termQuery("data.minute", calendar.get(Calendar.MINUTE))).should(QueryBuilders.termQuery("data.minute", "all"));
//
//        //getES by query here
//        queryBuilder.must(monthQueryBuilder).must(weekQueryBuilder).must(dayQueryBuilder).must(hourQueryBuilder).must(minuteQueryBuilder);
//        sourceBuilder.query(queryBuilder).from(0).size(10000);
//        SearchRequest searchRequest = new SearchRequest("timeflow").source(sourceBuilder);
//        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        SearchHit[] hits = searchResponse.getHits().getHits();
//
//        HashSet setId_A = new HashSet();
//        JSONObject jsonId_A = new JSONObject();
//        System.out.println("length=" + hits.length);
//
//        // Get list of id_A and ref
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
//        Query queryAsset = new Query(new Criteria("_id").in(setId_A));
//        List<Asset> assets = mongoTemplate.find(queryAsset, Asset.class);
//        JSONArray arrayTrigger = new JSONArray();
//        for (int i = 0; i < assets.size(); i++) {
//            JSONArray arrayRef = jsonId_A.getJSONArray(assets.get(i).getId());
//            for (int j = 0; j < arrayRef.size(); j++) {
//                String ref = arrayRef.getString(j);
//                JSONObject jsonTrigger = assets.get(i).getCTrigger().getJSONObject("objData").getJSONObject(ref);
//                //Same thing, but isActive should change to isActive
//                // so that I can disable a trigger instead of delete it from ctrig
//                if (jsonTrigger.getBoolean("isActive")) {
//                    if (jsonTrigger.getBoolean("isActive")) {
//                        jsonTrigger.put("id", assets.get(i).getId());
//                        jsonTrigger.put("table", "Asset");
//                        jsonTrigger.put("key", "cTrigger.objData." + ref);
//                        arrayTrigger.add(jsonTrigger);
//                    }
//                } else {
//                    //do nothing
//                   // arrayTrigger.add(jsonTrigger);
//                }
//            }
//        }
//
//        Object trigger = scriptTimer.scriptEngine(arrayTrigger);
//    }


//    private final String clockIn = "0 0 0 * * ?";
//    @Scheduled(cron = clockIn)
//    @SuppressWarnings("unchecked")
//    public void clockIn() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Calendar calendar = Calendar.getInstance();
//        Date date = new Date();
//        calendar.setTime(date);
//        calendar.add(Calendar.HOUR, - 10);
//        String time = sdf.format(new Date());
//        String startTime = date + " 00:00:00";
//        String endTime = date + "23:59:59";
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//        queryBuilder.must(QueryBuilders.rangeQuery("tmd").from(startTime, true).to(endTime, true));
//        sourceBuilder.query(queryBuilder);
//    }
}



