package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import com.cresign.tools.pojo.po.orderCard.OrderStock;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class DbUtils {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private Qt qt;

    @Autowired
    private DoubleUtils du;

    /*
        ES
        lSBxxxUpdateFields
        getES-lSBxxx data
        getES-logFlow data
        sendLog

        Mdb
        getCard@COUPA, setCard[]@COUPA, addCard[]@COUPA
        getCard[]ById
        delCOUPA saveCOUPA

        redis
        setRedis0, setRedis1
        getRedis0, getRedis1

     */


    /**
     * 根据id查询mongo
     * @author Rachel
     * @Date 2022/01/14
     * @param id
     * @param field 返回字段
     * @param classType 表对应的实体类
     * @Return java.lang.Object
     * @Card
     **/

    //Fixed
    public Object getMongoOneField(String id, String field, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        if (field != null) {
            query.fields().include(field);
        }
        return mongoTemplate.findOne(query, classType);
    }

    /**
     * 根据id查询mongo
     * @author Rachel
     * @Date 2022/01/14
     * @param id
     * @param listField 多个返回字段
     * @param classType 表对应的实体类
     * @Return java.lang.Object
     * @Card
     **/

    //FIXED
    public Object getMongoOneFields(String id, List<String> listField, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        listField.forEach(query.fields()::include);
        return mongoTemplate.findOne(query, classType);
    }


    public Map<String, ?> getMongoMapField(HashSet setId, String field, Class<?> classType) {
        Query query = new Query(new Criteria("_id").in(setId));
        if (field != null) {
            query.fields().include(field);
        }
        List<?> list = mongoTemplate.find(query, classType);
        System.out.println("list=" + list);
        Map<String, Object> mapId = new HashMap<>();
        list.forEach(l ->{
            System.out.println("l= " + l);
            JSONObject json = (JSONObject) JSON.toJSON(l);
            mapId.put(json.getString("id"), l);
        });

        return mapId;
    }


    /**
     * set修改mongo
     * @author Rachel
     * @Date 2022/01/14
     * @param id
     * @param updateKey 修改字段key
     * @param updateValue 修改字段value
     * @param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    //FIXED

    public UpdateResult setMongoValue(String id, String updateKey, Object updateValue, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        update.set(updateKey, updateValue);
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * set修改mongo
     * @author Rachel
     * @Date 2022/01/14
     * @param jsonUpdate 多个修改字段
     * @param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    //FIXED
    public UpdateResult setMongoValues(String id, JSONObject jsonUpdate, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        jsonUpdate.forEach((k, v) ->{
            update.set(k, v);
        });
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * 修改mongo
     * @author Rachel
     * @Date 2022/01/14
     * @param id
     * @param update 修改语句
     * @param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    //FIXED
    public UpdateResult updateMongoValues(String id, Update update, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * 批量操作mongo
     * @author Rachel
     * @Date 2022/05/16
     * @Param listBulk [新增：{"type":"insert", "insert":Object} / 修改：{"type":"update", "id":"", update:Update} / 删除：{"type":"remove", "id":""}]
     * @Param classType 表对应的实体类
     * @Return com.mongodb.bulk.BulkWriteResult
     * @Card
     **/
    public BulkWriteResult bulkMongo(List<Map> listBulk, Class<?> classType) {
        System.out.println("listBulk=" + listBulk);
        BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, classType);
        listBulk.forEach(mapBulk -> {
            String type = mapBulk.get("type").toString();
            if (type.equals("insert")) {
                bulk.insert(mapBulk.get("insert"));
            } else if (type.equals("update")) {
                Query query = new Query(new Criteria("_id").is(mapBulk.get("id")));
                bulk.updateOne(query, (Update) mapBulk.get("update"));
            } else if (type.equals("delete")) {
                Query query = new Query(new Criteria("_id").is(mapBulk.get("id")));
                bulk.remove(query);
            }
        });
        BulkWriteResult execute = bulk.execute();
        return execute;
    }

    /**
     * 获取原有日志修改后发日志
     * @author Rachel
     * @Date 2022/05/18
     * @Param queryBuilder es查询语句
     * @Param sortKey 排序字段
     * @Param jsonLog 日志修改 / 新增字段
     * @Param jsonData 日志data修改 / 新增字段
     * @Param logType 日志类型
     * @Return org.elasticsearch.action.index.IndexResponse
     * @Card
     **/
    //FIXED
    public LogFlow getRecentLog(String id_O, Integer index, JSONObject tokData) throws IOException {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("id_C", tokData.getString("id_C")))
                .must(QueryBuilders.termQuery("id_O", id_O))
                .must(QueryBuilders.termQuery("index", index));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder).from(0).size(1).sort("tmd", SortOrder.DESC);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("action","assetflow");
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("result");
        System.out.println(id_O+" "+index+" "+tokData.getString("id_C"));

        System.out.println(searchResponse.getHits().getHits());
        SearchHit hit = searchResponse.getHits().getHits()[0];
//        Map<String, Object> newestLog = hit.getSourceAsMap();
        if (hit == null)
        {
            return null;
        }
        LogFlow newestLog = JSONObject.parseObject(hit.getSourceAsString(), LogFlow.class);
//        newestLog.putAll(jsonLog);
        //Setup who and when
        newestLog.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        newestLog.setId_U(tokData.getString("id_U"));
        newestLog.setDep(tokData.getString("dep"));
        newestLog.setGrpU(tokData.getString("grpU"));
        newestLog.setWrdNU(tokData.getJSONObject("wrdNU"));


//
//        newestLog.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//        newestLog.put("id_U", tokData.getString("id_U"));
//        newestLog.put("dep", tokData.getString("dep"));
//        newestLog.put("grpU", tokData.getString("grpU"));
//        newestLog.put("wrdNU", tokData.getJSONObject("wrdNU"));
//
//
//        if (jsonData != null) {
//            JSONObject jsonHitData = (JSONObject) JSON.toJSON(newestLog.get("data"));
//            jsonHitData.putAll(jsonData);
//            newestLog.put("data", jsonHitData);
//        }
//        LogFlow log = JSONObject.parseObject(JSON.toJSONString(newestLog),LogFlow.class);


//        //no sending, just create?
//
//        IndexRequest indexRequest = new IndexRequest(logType);
//        indexRequest.source(mapHit, XContentType.JSON);
//        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        return newestLog;
    }

    /**
     * 查询es
     * @author Rachel
     * @Date 2022/01/14
     * @param key 查询key
     * @param value 查询value
     * @param logType 索引名
     * @Return org.elasticsearch.action.search.SearchResponse
     * @Card
     **/
    //FIXED
    public JSONArray getEsKey(String key, Object value, String logType) throws IOException {

        JSONArray result = new JSONArray();

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery(key, value));
        sourceBuilder.query(queryBuilder).from(0).size(1000);

        System.out.println("VA 1 only"+value+"   ");

        try {
        SearchRequest request = new SearchRequest(logType).source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> mapHit = hit.getSourceAsMap();
//                mapHit.put("esId", hit.getId());
                result.add(mapHit);
            }

            return result;

        } catch (
                IOException e) {
            e.printStackTrace();
        }

        return null;

    }
    //FIXED

    public JSONArray getEsKey(String key, Object value, String key2, Object value2, String logType) {

        JSONArray result = new JSONArray();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery(key, value))
                .must(QueryBuilders.termQuery(key2, value2));
        sourceBuilder.query(queryBuilder).from(0).size(1000);

        System.out.println("value2"+value+"   "+ value2);

        try {
            SearchRequest request = new SearchRequest(logType).source(sourceBuilder);

            SearchResponse search = client.search(request, RequestOptions.DEFAULT);
            for (SearchHit hit : search.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
                result.add(hit.getSourceAsMap());
            }

            System.out.println("result"+result);
            return result;

        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询es
     * @author Rachel
     * @Date 2022/01/14
     * @param jsonQuery 多个查询对象
     * @param logType 索引名
     * @Return org.elasticsearch.action.search.SearchResponse
     * @Card
     **/
    //FIXED

    public SearchResponse getEsKeys(JSONObject jsonQuery, String logType) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        jsonQuery.forEach((k, v) ->{
            queryBuilder.must(QueryBuilders.termQuery(k, v));
        });
        sourceBuilder.query(queryBuilder).from(0).size(1000);
        SearchRequest request = new SearchRequest(logType).source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        return response;
    }

    /**
     * 查询es
     * @author Rachel
     * @Date 2022/02/27
     * @param queryBuilder 查询语句
     * @param logType 索引名
     * @Return org.elasticsearch.action.search.SearchResponse
     * @Card
     **/
    //FIXED

    public SearchResponse getEsQuery(BoolQueryBuilder queryBuilder, String logType) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder).from(0).size(5000);
        SearchRequest request = new SearchRequest(logType).source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        return response;
    }

    //FIXED
    public JSONArray getESListByQuery(BoolQueryBuilder queryBuilder, String logType) throws IOException {
        JSONArray result = new JSONArray();

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder).from(0).size(5000);
        SearchRequest request = new SearchRequest(logType).source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        for (SearchHit hit : response.getHits().getHits()) {
            result.add(hit.getSourceAsMap());
        }

        return result;
    }

    /**
     * 新增assetflow日志
     * @author Jevon
     * @param infoObject
     * @ver 1.0
     * @updated 2020/10/26 8:30
     * @return void
     */



    public void addES(JSONObject infoObject , String indexes ) throws IOException {

        //8-1 indexes = indexes + "-write";
        infoObject.put("tmk", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        //指定ES索引 "assetflow" / "assetflow-write / assetflow-read
        IndexRequest request = new IndexRequest(indexes);
        //ES列表
        request.source(infoObject, XContentType.JSON);

        client.index(request, RequestOptions.DEFAULT);

    }




    public UpdateResponse updateEs(String logType, String id, JSONObject logInfo) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(logType, id);
        logInfo.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        updateRequest.doc(logInfo, XContentType.JSON);
        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        return updateResponse;
    }


    public void updateListCol(QueryBuilder query, String listType, JSONObject listCol) throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(query).size(5000);
        SearchRequest srb = new SearchRequest(listType);

        srb.source(searchSourceBuilder);

        SearchResponse search = client.search(srb, RequestOptions.DEFAULT);

        for (SearchHit hit : search.getHits().getHits()) {
            UpdateRequest updateRequest = new UpdateRequest();

            hit.getSourceAsMap().put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            hit.getSourceAsMap().putAll(listCol);

            updateRequest.index(listType);
            updateRequest.id(hit.getId());
            updateRequest.doc(hit.getSourceAsMap());
            client.update(updateRequest, RequestOptions.DEFAULT);

        }

    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// init oItem, action, oStock if needed
public JSONObject checkCard(Order order)
{
    JSONObject jsonCard = new JSONObject();
    JSONArray arrayCard = null;
    JSONArray arrayOItem = null;
    JSONArray arrayArrP = null;
    JSONArray arrayData = null;
    System.out.println("checking Card");

        arrayOItem = order.getOItem().getJSONArray("objItem");
        if (order.getOItem().getString("bmdHeight") == null)
        {    order.getOItem().put("bmdHeight", "unset"); }
        if ( order.getOItem().getJSONArray("objCard") == null)
        {
            order.getOItem().put("objCard", new JSONArray());
            order.getOItem().getJSONArray("objCard").add("oItem");

            if (order.getOStock() != null)
            {
                order.getOItem().getJSONArray("objCard").add("oStock");
            }
            if (order.getAction() != null) {
                order.getOItem().getJSONArray("objCard").add("action");
            }
        }
        if (order.getOItem().getJSONArray("arrP") == null)
        {
            order.getOItem().put("arrP", new JSONArray());
            for (int i = 0; i < arrayOItem.size(); i++)
            {
                order.getOItem().getJSONArray("arrP").add(arrayOItem.getJSONObject(i).getString("id_P"));
            }
        }

        if (order.getOItem().getJSONArray("objItemCol") == null)
         {
             order.getOItem().put("objItemCol", qt.getInitData("cn").getList().getJSONArray("objItemCol"));
             System.out.println(qt.getInitData("cn").getList());
         }

        arrayCard = order.getOItem().getJSONArray("objCard");
        arrayArrP = order.getOItem().getJSONArray("arrP");

    if (arrayCard == null || arrayOItem == null || arrayArrP == null || arrayOItem.size() != arrayArrP.size()) {
        System.out.println(2);
        throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), null);
    }

    // for each "card" in objCard: check if "json data" actually has data
    for (int n = 0; n < arrayCard.size(); n++)
    {
            switch (arrayCard.getString(n)) {
                case "oStock":
                    if (order.getOStock() == null)
                    {
                        //whole card Init
                        order.setOStock(this.initOStock(order.getOItem().getJSONArray("objItem")));
                        order.getView().add("oStock");
                    }
                    // Check if all oItem has a oStock, if not, init it
                    for (int i = 0; i < arrayOItem.size(); i++)
                    {
                        if (order.getOStock().getJSONArray("objData").getJSONObject(i) == null)
                        {
                            // just init 1 oitem
                            this.initOStock(arrayOItem.getJSONObject(i), order.getOStock().getJSONArray("objData"),i);
                        }
                    }
                    arrayData = order.getOStock().getJSONArray("objData");

                    break;
                case "action":
                    if (order.getAction() == null)
                    {
                        order.setAction(this.initAction(order.getOItem().getJSONArray("objItem")));
                        order.getView().add("action");
                    }
                    // Check if all oItem has a action, if not, init it
                    for (int i = 0; i < arrayOItem.size(); i++)
                    {
                        this.initAction(arrayOItem.getJSONObject(i), order.getAction().getJSONArray("objAction"),i);
                    }
                    arrayData = order.getAction().getJSONArray("objAction");
                    break;
            }
            jsonCard.put(arrayCard.getString(n), arrayData);
    }
    // this will return an JSONObject with "oStock":{...}, "action":{...}
    return jsonCard;
}

// updateAsset , ... ?????, Asset
    /**
     *
     * @param order
     * @param listCol ES update list
     * @return JSONObject of update String
     */
    public JSONObject summOrder(Order order, JSONObject listCol)
    {

        if (order.getOItem() == null)
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), null);


        JSONArray oItem = order.getOItem().getJSONArray("objItem");
        JSONArray oStock = null;
        JSONArray action = null;

        Double wn2fin = 0.0;
        Double wn2made = 0.0;
        Double wn2progress = 0.0;
        Integer count = 0;
        Double wn2qty = 0.0;
        Double wn4price = 0.0;
        JSONArray arrP = new JSONArray();

        if (order.getOItem().getJSONArray("objCard").contains("oStock") && order.getOStock() != null)
        {
            oStock = order.getOStock().getJSONArray("objData");
        }
        if (order.getOItem().getJSONArray("objCard").contains("action") && order.getAction() != null)
        {
            action = order.getAction().getJSONArray("objAction");
        }

        for (int i = 0; i < oItem.size(); i++)
        {
            wn2qty = du.add(oItem.getJSONObject(i).getDouble("wn2qtyneed"), wn2qty);
            wn4price = du.add(wn4price, du.multiply(oItem.getJSONObject(i).getDouble("wn2qtyneed"),oItem.getJSONObject(i).getDouble("wn4price")));
            arrP.add(oItem.getJSONObject(i).getString("id_P"));
            oItem.getJSONObject(i).put("index", i);

            if (oStock != null)
            {
                Double madePercent = du.divide(oStock.getJSONObject(i).getDouble("wn2qtymade"),oItem.getJSONObject(i).getDouble("wn2qtyneed"));
                wn2made = du.add(wn2made, madePercent);
                oStock.getJSONObject(i).put("index", i);

                if (action != null)
                {
                    for (int j = 0; j < action.getJSONObject(i).getJSONArray("upPrnts").size(); j++)
                    {
                        if (oStock.getJSONObject(i).getJSONArray("objShip").getJSONObject(j) == null)
                        {
                            // init it if it is null
                            JSONObject newObjShip = qt.setJson("wn2qtynow", 0.0, "wn2qtymade", 0.0,
                                    "wn2qtyneed", oItem.getJSONObject(i).getDouble("wn2qtyneed"));
                            oStock.getJSONObject(i).getJSONArray("objShip").add(newObjShip);
                        }
                    }
                }
            }
            if (action != null)
            {
                count = action.getJSONObject(i).getInteger("bcdStatus") == 2 ? 1: 0 + count;
                action.getJSONObject(i).put("index", i);

                String grp = oItem.getJSONObject(i).getString("grp");
                String grpB = oItem.getJSONObject(i).getString("grpB");

                // if grp not exists, need to init grpGroup
                if (grp != null && !grp.equals("") && order.getAction().getJSONObject("grpGroup").getJSONObject(grp) == null)
                {
                Asset asset = qt.getConfig(oItem.getJSONObject(i).getString("id_C"),"a-auth","def.objlSP."+grp);
                    //sales side getlSProd, and set default values
                    System.out.println("getGrp"+asset.getId());

                    if (!asset.getId().equals("none")) {
                        JSONObject grpData = asset.getDef().getJSONObject("objlSP").getJSONObject(grp) == null? new JSONObject() : asset.getDef().getJSONObject("objlSP").getJSONObject(grp);
                        order.getAction().getJSONObject("grpGroup").put(grp, grpData);
                    }
                }
                if (grpB != null && !grpB.equals("") && order.getAction().getJSONObject("grpBGroup").getJSONObject(grpB) == null)
                {

                    Asset asset = qt.getConfig(order.getInfo().getId_CB(),"a-auth","def.objlBP."+grpB);
                    System.out.println("getGrpB"+asset.getId());

                    //sales side getlSProd, and set default values
                    if (!asset.getId().equals("none")) {
                        JSONObject grpData = asset.getDef().getJSONObject("objlBP").getJSONObject(grpB) == null ? new JSONObject() : asset.getDef().getJSONObject("objlBP").getJSONObject(grpB);
                        order.getAction().getJSONObject("grpBGroup").put(grpB, grpData);
                    }
                }
            }
        }
        wn2fin = du.divide(wn2made, oItem.size());
        wn2progress = du.divide(count, oItem.size());
        qt.upJson(listCol, "wn2fin", wn2fin, "wn2progress", wn2progress, "wn2qty", wn2qty, "wn4price", wn4price, "arrP", arrP);

        order.getOItem().put("wn2qty", wn2qty);
        order.getOItem().put("wn4price", wn4price);
        order.getOItem().put("arrP", arrP);
        order.getAction().put("wn2progress", wn2progress);
        order.getOStock().put("wn2fin", wn2fin);

        JSONObject result = new JSONObject();
        result.put("oItem", order.getOItem());
        result.put("view", order.getView());
        if (oStock != null)
            result.put("oStock", order.getOStock());
        if (action != null)
            result.put("action", order.getAction());
        return result;

    }

    public void initAction(JSONObject orderOItem, JSONArray action, Integer index)
    {

        if (action.getJSONObject(index) == null) {
            JSONObject actionData = qt.setJson(
                    "bcdStatus", 100, "bisPush", 0,
                    "bisactivate", 0,
                    "id_O", orderOItem.getString("id_O"),
                    "id_Us", new JSONArray(),
                    "id_OP", orderOItem.getString("id_OP"),
                    "id_P", orderOItem.getString("id_P"),
                    "index", orderOItem.getString("index"),
                    "refOP", "",
                    "prob", new JSONArray(),
                    "sumChild", 0, "sumPrev", 0,
                    "prtNext", new JSONArray(),
                    "prtPrev", new JSONArray(),
                    "subParts", new JSONArray(),
                    "upPrnts", new JSONArray(),
                    "sumChild", 0,
                    "rKey", orderOItem.getString("rKey"),
                    "wrdNP", orderOItem.getJSONObject("wrdNP"),
                    "wrdN", orderOItem.getJSONObject("wrdN"),
                    "bmdpt", 1, "priority", 2);


            action.set(index, actionData);
        }
        if (action.getJSONObject(index).getJSONArray("upPrnts") == null)
        {
            action.getJSONObject(index).put("upPrnts", new JSONArray());
        }
        if (action.getJSONObject(index).getJSONArray("subParts") == null)
        {
            action.getJSONObject(index).put("subParts", new JSONArray());
        }
        if (action.getJSONObject(index).getJSONArray("prtPrev") == null)
        {
            action.getJSONObject(index).put("prtPrev", new JSONArray());
        }
        if (action.getJSONObject(index).getJSONArray("prtNext") == null)
        {
            action.getJSONObject(index).put("prtNext", new JSONArray());
        }
    }

    /***
     * init entire oStock from nothing
     * @param oItem need to send the entire oItem.objItem for data to init
     * @return
     */
    public JSONObject initAction(JSONArray oItem)
    {
        JSONArray actionArray = new JSONArray();
        for (int i = 0; i < oItem.size(); i++)
        {
            this.initAction(oItem.getJSONObject(i), actionArray, i);
        }
        JSONObject action = new JSONObject();
        action.put("objAction", actionArray);
        action.put("wn2progress", 0.0);
        action.put("grpGroup", new JSONObject());
        action.put("grpBGroup", new JSONObject());
        return action;
    }

    public void initOStock(JSONObject orderOItem, JSONArray oStock, Integer index)
    {

            JSONObject oStockData = qt.setJson("wn2qtynow", 0.0, "wn2qtymade", 0.0,
                    "id_P", orderOItem.getString("id_P"),
                    "resvQty", new JSONObject(),
                    "rKey", orderOItem.getString("rKey"));

            oStockData.put("objShip", qt.setArray(
                    qt.setJson("wn2qtynow", 0.0, "wn2qtymade", 0.0,
                            "wn2qtyneed", orderOItem.getDouble("wn2qtyneed"))));

            oStock.set(index, oStockData);
    }

    /***
     * init entire oStock from nothing
     * @param oItem need to send the entire oItem.objItem for data to init
     * @return
     */
    public JSONObject initOStock(JSONArray oItem)
    {
        JSONArray stockArray = new JSONArray();
        for (int i = 0; i < oItem.size(); i++)
        {
            this.initOStock(oItem.getJSONObject(i), stockArray, i);
        }
        JSONObject oStock = new JSONObject();
        oStock.put("objData", stockArray);
        oStock.put("wn2fin", 0.0);
        return oStock;
    }

    /*  Order -> order, =>

    if action, oItem -> action, sumQty, wn2Qtynow
    if oStock
    */

    public void upOrderRelated(Order data, JSONObject listCol) {

        JSONArray cardList = data.getOItem().getJSONArray("objCard");

        JSONArray oItem = data.getOItem().getJSONArray("objItem");

        JSONArray view = data.getView();

        // up all card's summary values
        for (int i = 0; i < cardList.size(); i++)
        {
            String card = cardList.getString(i);

            switch (card) {
                case "oItem":
                    // Calculate Summarize keys here
                    // summarize all lsbKeys
                    Double qtyTotal = 0.0;
                    Double priceTotal = 0.0;
                    for (int j = 0; j < data.getOItem().getJSONArray("objItem").size(); j++)
                    {
                        JSONObject objItem = data.getOItem().getJSONArray("objItem").getJSONObject(j);
                        qtyTotal = qtyTotal + objItem.getDouble("wn2qtyneed");
                        priceTotal = priceTotal + objItem.getDouble("wn2qtyneed") * objItem.getDouble("wn4price");
                    }

                    // set mdb & listCol
                    data.getOItem().put("wn2qty", qtyTotal);
                    data.getOItem().put("wn4price", priceTotal);
                    listCol.put("wn2qty", qtyTotal);
                    listCol.put("wn4price", priceTotal);

                    System.out.print("oItem");

                    break;
                case "action":

                    //if card is NULL, init
                    if (data.getAction() == null)
                    {
                        JSONObject obj = new JSONObject();
                        obj.put("grpBGroup", new JSONObject());
                        obj.put("grpGroup", new JSONObject());
                        obj.put("objAction", new JSONArray());
                        data.setAction(obj);
                    }
//                    JSONArray savingData = new JSONArray();
                    JSONArray actionObj = data.getAction().getJSONArray("objAction");
                    JSONArray savingData = new JSONArray();

                    Integer counter = 0;

                    for (int j = 0; j < oItem.size(); j++)
                    {
                        String rKey = oItem.getJSONObject(j).getString("rKey");
                        Boolean isSet = false;
                        for (int k = 0; k < actionObj.size(); k++)
                        {
                            if (actionObj.getJSONObject(k).getString("rKey").equals(rKey))
                            {
                                //setup rearrange items by rKey
                                savingData.add(qt.cloneObj(actionObj.getJSONObject(k)));
                                isSet = true;
                                //set index as j (it's not arranged correctly)
                                savingData.getJSONObject(j).put("index", j);
                            }
                            break;
                        }

                        if (!isSet)
                        {
                            //need to init objAction here
                            OrderAction objAction = new OrderAction(100, 0, 0, 1, "", "", oItem.getJSONObject(j).getString("id_P"),
                                    oItem.getJSONObject(j).getString("id_O"),j,oItem.getJSONObject(j).getString("rKey"),
                                    0, 0, null, null, null, null, null, oItem.getJSONObject(j).getJSONObject("wrdN"));
                            savingData.add(qt.toJson(objAction));
                            //TODO KEV setup grpBGroup grpGroup
                        }

                        if (savingData.getJSONObject(j).getInteger("bcdStatus").equals(2))
                        {
                            counter++;
                        }
                    }

                    // Calculate Summarize keys here
                    // summarize all lsbKeys
                    data.getAction().put("wn2progress", counter / data.getAction().getJSONArray("objAction").size());

                    // set mdb & listCol
                    data.getAction().put("objAction", savingData);
                    listCol.put("wn2progress", data.getAction().getDouble("wn2progress"));

                    System.out.print("action");
                    break;
                case "oStock":
                    //if card is NULL, init
                    if (data.getOStock() == null)
                    {
                        JSONObject obj = new JSONObject();
                        obj.put("wn2fin", 0);
                        obj.put("objData", new JSONArray());
                        data.setOStock(obj);
                    }
                    JSONArray stockObj = data.getOStock().getJSONArray("objData");
                    Double finQty = 0.0;
                    JSONArray savingStock = new JSONArray();

                    for (int j = 0; j < oItem.size(); j++)
                    {
                        String rKey = oItem.getJSONObject(j).getString("rKey");
                        Boolean isSet = false;
                        for (int k = 0; k < stockObj.size(); k++)
                        {
                            if (stockObj.getJSONObject(k).getString("rKey").equals(rKey))
                            {
                                //setup rearrange items by rKey
                                savingStock.add(qt.cloneObj(stockObj.getJSONObject(k)));
                                isSet = true;
                                //set index as j (it's not arranged correctly)
                                savingStock.getJSONObject(j).put("index", j);
                            }
                            break;
                        }

                        if (!isSet)
                        {
                            //need to init objAction here
                            OrderStock objStock = new OrderStock(oItem.getJSONObject(j).getString("id_P"),
                                    oItem.getJSONObject(j).getString("rKey"),j, 0.0, 0.0);
                            savingStock.add(qt.toJson(objStock));
                            //TODO KEV setup grpBGroup grpGroup
                        }

                        finQty = finQty + savingStock.getJSONObject(j).getDouble("wn2qtymade");
                    }

                    // set mdb & listCol
                    data.getOStock().put("objData", savingStock);
                    data.getOStock().put("wn2fin", finQty);
                    listCol.put("wn2fin", finQty);

                    System.out.print("oStock");
                    break;
            }

            System.out.println("...2");

            if(!view.contains(card)) {
                view.add(card);
            }
        }



     //   qt.setMDContent(data.getId(), savingData, Order.class);
      //  qt.setES(listType,qt.setESFilt("id", data.getId()), listCol);

    }
}
