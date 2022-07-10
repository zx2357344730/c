package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Comp;
import com.cresign.tools.pojo.po.LogFlow;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
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
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private StringRedisTemplate redisTemplate0;

    /**
        ES
        merge addESs, delESs, updateES
        updateES, updateESBulk
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

     **/


    /**
     * 根据id查询mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param field 返回字段
     * ##param classType 表对应的实体类
     * @Return java.lang.Object
     * @Card
     **/
    public Object getMongoOneField(String id, String field, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        if (field != null) {
            query.fields().include(field);
        }
        Object one = mongoTemplate.findOne(query, classType);
        return one;
    }

    /**
     * 根据id查询mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param listField 多个返回字段
     * ##param classType 表对应的实体类
     * @Return java.lang.Object
     * @Card
     **/
    public Object getMongoOneFields(String id, List<String> listField, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        listField.forEach(query.fields()::include);
        Object one = mongoTemplate.findOne(query, classType);
        return one;
    }

    /**
     * 根据多个id查询mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param field 返回字段
     * ##param classType 表对应的实体类
     * @Return java.util.List<?>
     * @Card
     **/
    public List<?> getMongoListField(HashSet setId, String field, Class<?> classType) {
        Query query = new Query(new Criteria("_id").in(setId));
        if (field != null) {
            query.fields().include(field);
        }
        List<?> list = mongoTemplate.find(query, classType);
        return list;
    }

    /**
     * 根据多个id查询mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param listField 多个返回字段
     * ##param classType 表对应的实体类
     * @Return java.util.List<?>
     * @Card
     **/
    public List<?> getMongoListFields(HashSet setId, List<String> listField, Class<?> classType) {
        Query query = new Query(new Criteria("_id").in(setId));
        listField.forEach(query.fields()::include);
        List<?> list = mongoTemplate.find(query, classType);
        return list;
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


    public Map<String, ?> getMongoMapFields(HashSet setId, List<String> listField, Class<?> classType) {
        Query query = new Query(new Criteria("_id").in(setId));
        listField.forEach(query.fields()::include);
        List<?> list = mongoTemplate.find(query, classType);
        System.out.println("list=" + list);
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
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param updateKey 修改字段key
     * ##param updateValue 修改字段value
     * ##param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
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
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param jsonUpdate 多个修改字段
     * ##param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
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
     * inc修改mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param updateKey 修改字段key
     * ##param updateValue 修改字段value
     * ##param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    public UpdateResult incMongoValue(String id, String updateKey, Number updateValue, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        update.inc(updateKey, updateValue);
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * inc修改mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param jsonUpdate 多个修改对象
     * ##param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    public UpdateResult incMongoValues(String id, JSONObject jsonUpdate, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        jsonUpdate.forEach((k, v) ->{
            update.inc(k, (Number) v);
        });
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * push修改mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param updateKey 修改字段key
     * ##param updateValue 修改字段value
     * ##param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    public UpdateResult pushMongoValue(String id, String updateKey, Object updateValue, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        update.push(updateKey, updateValue);
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * push修改mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param jsonUpdate 多个修改对象
     * ##param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    public UpdateResult pushMongoValues(String id, JSONObject jsonUpdate, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        jsonUpdate.forEach((k, v) ->{
            update.push(k, v);
        });
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * pull修改mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param updateKey 修改字段key
     * ##param updateValue 修改字段value
     * ##param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    public UpdateResult pullMongoValue(String id, String updateKey, Object updateValue, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        update.pull(updateKey, updateValue);
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * pull修改mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param jsonUpdate 多个修改对象
     * ##param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    public UpdateResult pullMongoValues(String id, JSONObject jsonUpdate, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        jsonUpdate.forEach((k, v) ->{
            update.pull(k, v);
        });
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * 修改mongo
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id
     * ##param update 修改语句
     * ##param classType 表对应的实体类
     * @Return com.mongodb.client.result.UpdateResult
     * @Card
     **/
    public UpdateResult updateMongoValues(String id, Update update, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        return updateResult;
    }

    /**
     * 批量操作mongo
     * @Author Rachel
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
     * 批量操作es
     * @Author Rachel
     * @Date 2022/05/17
     * @Param arrayBulk 新增：{"type":"insert", "logType":"", "insert":{}} / 修改：{"type":"update", "logType":"", "id":"", update:{}} / 删除：{"type":"remove", "logType":"", "id":""}
     * @Param logType
     * @Return java.lang.Object
     * @Card
     **/
    public BulkResponse bulkEs(List<JSONObject> listBulk) throws IOException {
        System.out.println("listBulk=" + listBulk);
        BulkRequest bulk = new BulkRequest();
        listBulk.forEach(jsonBulk ->{
            String type = jsonBulk.getString("type");
            String logType = jsonBulk.getString("logType");
            if (type.equals("insert")) {
                bulk.add(new IndexRequest(logType).source(jsonBulk.getJSONObject("insert")));
            } else if (type.equals("update")) {
                bulk.add(new UpdateRequest(logType, jsonBulk.getString("id")).doc(jsonBulk.getJSONObject("update"), XContentType.JSON));
            } else if (type.equals("delete")) {
                bulk.add(new DeleteRequest(logType, jsonBulk.getString("id")));
            }
        });
        BulkResponse bulkResponse = client.bulk(bulk, RequestOptions.DEFAULT);
        return bulkResponse;
    }
    public BulkResponse bulkEs(List<JSONObject> listBulk, String logType) throws IOException {
        System.out.println("listBulk=" + listBulk);
        BulkRequest bulk = new BulkRequest();
        listBulk.forEach(jsonBulk ->{
            String type = jsonBulk.getString("type");
            if (type.equals("insert")) {
                bulk.add(new IndexRequest(logType).source(jsonBulk.getJSONObject("insert")));
            } else if (type.equals("update")) {
                bulk.add(new UpdateRequest(logType, jsonBulk.getString("id")).doc(jsonBulk.getJSONObject("update"), XContentType.JSON));
            } else if (type.equals("delete")) {
                bulk.add(new DeleteRequest(logType, jsonBulk.getString("id")));
            }
        });
        BulkResponse bulkResponse = client.bulk(bulk, RequestOptions.DEFAULT);
        return bulkResponse;
    }

    /**
     * 获取原有日志修改后发日志
     * @Author Rachel
     * @Date 2022/05/18
     * @Param queryBuilder es查询语句
     * @Param sortKey 排序字段
     * @Param jsonLog 日志修改 / 新增字段
     * @Param jsonData 日志data修改 / 新增字段
     * @Param logType 日志类型
     * @Return org.elasticsearch.action.index.IndexResponse
     * @Card
     **/
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

        SearchHit hit = searchResponse.getHits().getHits()[0];
//        Map<String, Object> newestLog = hit.getSourceAsMap();
        if (hit == null)
        {
            return null;
        }
        LogFlow newestLog = JSONObject.parseObject(hit.getSourceAsString(), LogFlow.class);
//        newestLog.putAll(jsonLog);
        //Setup who and when
        newestLog.setTmd(DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        newestLog.setId_U(tokData.getString("id_U"));
        newestLog.setDep(tokData.getString("dep"));
        newestLog.setGrpU(tokData.getString("grpU"));
        newestLog.setWrdNU(tokData.getJSONObject("wrdNU"));
//        
//        newestLog.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
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
     * @Author Rachel
     * @Date 2022/01/14
     * ##param key 查询key
     * ##param value 查询value
     * ##param logType 索引名
     * @Return org.elasticsearch.action.search.SearchResponse
     * @Card
     **/
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
     * @Author Rachel
     * @Date 2022/01/14
     * ##param jsonQuery 多个查询对象
     * ##param logType 索引名
     * @Return org.elasticsearch.action.search.SearchResponse
     * @Card
     **/
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

    public JSONArray getEsKeyMany(JSONObject jsonQuery, String logType) throws IOException {
        JSONArray result = new JSONArray();

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        jsonQuery.forEach((k, v) ->{
            queryBuilder.must(QueryBuilders.termQuery(k, v));
        });
        sourceBuilder.query(queryBuilder).from(0).size(1000);

        try {
            SearchRequest request = new SearchRequest(logType).source(sourceBuilder);

            SearchResponse search = client.search(request, RequestOptions.DEFAULT);
            for (SearchHit hit : search.getHits().getHits()) {
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
     * @Author Rachel
     * @Date 2022/02/27
     * ##param queryBuilder 查询语句
     * ##param logType 索引名
     * @Return org.elasticsearch.action.search.SearchResponse
     * @Card
     **/
    public SearchResponse getEsQuery(BoolQueryBuilder queryBuilder, String logType) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder).from(0).size(5000);
        SearchRequest request = new SearchRequest(logType).source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        return response;
    }

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


    public BulkByScrollResponse delES(String listType, String key, String id) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(listType);
        deleteByQueryRequest.setQuery(QueryBuilders.termQuery(key, id));
        return client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
    }
    public BulkByScrollResponse delES(String listType, String key, String id, String key2, String id2) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(listType);
        deleteByQueryRequest.setQuery(QueryBuilders.termQuery(key, id));
        deleteByQueryRequest.setQuery(QueryBuilders.termQuery(key2, id2));

        return client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
    }

    /**
     * 新增assetflow日志
     * ##author: Jevon
     * ##Params: infoObject
     * ##version: 1.0
     * ##updated: 2020/10/26 8:30
     * ##Return: void
     */
    public  void addES(JSONObject infoObject , String indexes ) throws IOException {

        //8-1 indexes = indexes + "-write";
        infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        //指定ES索引 "assetflow" / "assetflow-write / assetflow-read
        IndexRequest request = new IndexRequest(indexes);
        //ES列表
        request.source(infoObject, XContentType.JSON);

        client.index(request, RequestOptions.DEFAULT);

    }


    /**
     * 根据id_C和ref获取id_A
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id_C 公司id
     * ##param ref 编号
     * @Return java.lang.String
     * @Card
     **/
    public String getId_A(String id_C, String ref) {
        Boolean bool = redisTemplate0.opsForHash().hasKey("login:module_id:compId-" + id_C, ref);
        System.out.println("bool=" + bool);
        if (bool) {
            String id_A = (String) redisTemplate0.opsForHash().get("login:module_id:compId-" + id_C, ref);
            System.out.println("id_A=" + id_A);
            return id_A;
        } else {
            Query queryAsset = new Query(new Criteria("info.id_C").is(id_C).and("info.ref").is(ref));
            queryAsset.fields().include("id");
            Asset asset = mongoTemplate.findOne(queryAsset, Asset.class);
            System.out.println("what"+id_C+ref);
            if (asset == null) {
//                throw new ErrorResponseException(HttpStatus.FORBIDDEN, ToolEnum.ASSET_NOT_FOUND.getCode(), null);
                return "none";
            }
            redisTemplate0.opsForHash().put("login:module_id:compId-" + id_C, ref, asset.getId());
            System.out.println("id_A=" + asset.getId());
            return asset.getId();
        }
    }

    /**
     * 根据aId获取listKey需要的信息
     * ##Params: aId	aid
     * ##Params: listKey	需要的数据集合
     * ##return: com.cresign.chat.pojo.po.Asset  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 9:29
     */
    public Asset getAssetById(String aId, List<String> listKey) {
        Query query = new Query(new Criteria("_id").is(aId));
        Field fields = query.fields();
        listKey.forEach(fields::include);
        return mongoTemplate.findOne(query, Asset.class);
    }

    /**
     * 查询公司是真是假  1：真公司    0：假公司，2：都是自己
     * ##author: Jevon
     * ##Params: id_C      自己
     * ##Params: compOther     对方
     * ##version: 1.0
     * ##updated: 2021/1/12 9:33
     * ##Return: int
     */
    public int judgeComp(String id_C,String compOther){

        if (id_C.equals(compOther)){
            return 2;
        }else{
            Query compQ = new Query(
                    new Criteria("_id").is(compOther).and("bcdNet").is(1));
            compQ.fields().include("bcdNet");
            Comp comp = mongoTemplate.findOne(compQ, Comp.class);
            if (comp != null) {
                return 1;
            }else {
                return 0;
            }
        }
    }


    public UpdateResponse updateEs(String logType, String id, JSONObject logInfo) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(logType, id);
        logInfo.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
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

            hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
            hit.getSourceAsMap().putAll(listCol);

            updateRequest.index(listType);
            updateRequest.id(hit.getId());
            updateRequest.doc(hit.getSourceAsMap());
            client.update(updateRequest, RequestOptions.DEFAULT);

        }

    }


}
