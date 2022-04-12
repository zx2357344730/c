package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.enumeration.DateEnum;
import com.mongodb.client.result.UpdateResult;
import org.elasticsearch.action.delete.DeleteResponse;
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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Service
public class DbUtils {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private StringRedisTemplate redisTemplate0;

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
    public List<?> getMongoListFields(HashSet arrayId, List<String> listField, Class<?> classType) {
        Query query = new Query(new Criteria("_id").in(arrayId));
        listField.forEach(query.fields()::include);
        List<?> list = mongoTemplate.find(query, classType);
        return list;
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


//    public Object addMongo(Object obj) {
//
//        Object insert = mongoTemplate.insert(obj);
//    }

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
    public SearchResponse getEsKey(String key, Object value, String logType) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery(key, value));
        sourceBuilder.query(queryBuilder).from(0).size(1000);
        SearchRequest request = new SearchRequest(logType).source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        return response;
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
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        return response;
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
        sourceBuilder.query(queryBuilder).from(0).size(1000);
        SearchRequest request = new SearchRequest(logType).source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        return response;
    }




    public IndexResponse addEs(String logType, Object obj) throws IOException {
//        IndexRequest indexRequest = new IndexRequest(logType);
//        indexRequest.source(obj, XContentType.JSON);
//        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//        System.out.println("ES result"+indexResponse);
//        return indexResponse;
        return null;
    }

    public BulkByScrollResponse delES(String listType, String key, String id) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(listType);
        deleteByQueryRequest.setQuery(QueryBuilders.termQuery(key, id));
        return restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
    }
    public BulkByScrollResponse delES(String listType, String key, String id, String key2, String id2) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(listType);
        deleteByQueryRequest.setQuery(QueryBuilders.termQuery(key, id));
        deleteByQueryRequest.setQuery(QueryBuilders.termQuery(key2, id2));

        return restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
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

        infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        //指定ES索引
        IndexRequest request = new IndexRequest(indexes);
        //ES列表
        request.source(infoObject, XContentType.JSON);

        restHighLevelClient.index(request, RequestOptions.DEFAULT);

    }

    public UpdateResponse updateEs(String logType, String id, JSONObject jsonHit) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(logType, id);
        jsonHit.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        updateRequest.doc(jsonHit, XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        return updateResponse;
    }


    public void updateES(QueryBuilder query, String searchIndex, JSONObject listCol) throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(query).size(5000);
        SearchRequest srb = new SearchRequest(searchIndex);

        srb.source(searchSourceBuilder);

        SearchResponse search = restHighLevelClient.search(srb, RequestOptions.DEFAULT);

        for (SearchHit hit : search.getHits().getHits()) {
            UpdateRequest updateRequest = new UpdateRequest();

            hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
            hit.getSourceAsMap().putAll(listCol);

            updateRequest.index(searchIndex);
            updateRequest.id(hit.getId());
            updateRequest.doc(hit.getSourceAsMap());
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

        }

    }


}
