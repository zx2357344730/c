package com.cresign.tools.dbTools;

/**
 * @author kevin
 * @ClassName Qt
 * @Description
 * @updated 2022/9/11 10:05 AM
 * @ver 1.0.0
 **/

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.ToolEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.User;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

    @Service
    public class Qt {

        /**
         * 1。 MDB
         * 2。 ES
         * 3. RD
         * get = read, add = + , set / inc/ push/pull = update, del = delete
         * Many (批量修改/ 批量Get）
         *
         *
         * 1。 MD - id 来查， 修改 =》 JSONObject
         * 2。 ES - get (filterArray - filter 条件)
         * queryBuilder XXX - 只能传JSONOvbject
         * 结果 = 》 只能是 JSONArray
         *
         *
         */

        @Autowired
        private MongoTemplate mongoTemplate;

        @Autowired
        private RestHighLevelClient client;

        @Autowired
        private Ut ut;

        @Autowired
        private StringRedisTemplate redisTemplate0;
        
        //MDB - done: get, getMany, del, new, inc, set, push, pull
        //MDB - need: ops, opsExec 
        //ESS - done: get, getFilt, del, add, set
        //ESS - need: ops, opsExec
        //RED - done: set/hash, hasKey, put/set, get expire
        //Other - done: toJson, jsonTo, list2Map, getConfig, filterBuilder
        //Other - need: getRecentLog, judgeComp, chkUnique,, checkOrder, updateSize

        public static String GetObjectId() {
            return new ObjectId().toString();
        }

        /**
         * 根据id查询mongo
         * @author Rachel
         * @Date 2022/01/14
         * @param id mongoDB ID
         * @param classType 表对应的实体类
         * @Return java.lang.Object
         * @Card
         **/
        public <T> T  getMDContent(String id,  List<String>  fields, Class<T> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            if (fields != null) {

                fields.forEach(query.fields()::include);
                System.out.println("query "+query);
            }
            T result;
            try {
                result = mongoTemplate.findOne(query, classType);
            } catch (Exception e){
                throw new ErrorResponseException(HttpStatus.OK,ToolEnum.DB_ERROR.getCode(), e.toString());
            }
            return result;
        }

        public <T> T  getMDContent(String id,  String field, Class<T> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            if (field != null && !field.equals("")) {
                query.fields().include(field);
            }
            T result;
            try {
                result = mongoTemplate.findOne(query, classType);
            } catch (Exception e){
                throw new ErrorResponseException(HttpStatus.OK,ToolEnum.DB_ERROR.getCode(), e.toString());
            }
            return result;
        }


        /**
         * 根据多个id查询mongo
         * @author Rachel
         * @Date 2022/01/14

         * @param classType 表对应的实体类
         * @Return java.util.List<?>
         * @Card
         **/

        public List<?> getMDContentMany(HashSet setIds, List<String> fields, Class<?> classType) {
//            String [] setIds = setId.split(",");
            Query query = new Query(new Criteria("_id").in(setIds));
            if (fields != null && !fields.equals("")) {
                fields.forEach(query.fields()::include);
            }
            
            return mongoTemplate.find(query, classType);
        }

        public List<?> getMDContentMany(HashSet setId, String field, Class<?> classType) {
            Query query = new Query(new Criteria("_id").in(setId));
            if (field != null && !field.equals("")) {
                query.fields().include(field);
            }
            return mongoTemplate.find(query, classType);
        }

//        public void opsMDSet(JSONObject map, String id, String type, String key, Object value)
//        {
//            //1. group by id
//            //2. type = set/inc/push etc all ok
//            //3. create the json needed for bulk
//            JSONObject mapBulk = new JSONObject();
//            mapBulk.put("type", type);
//            mapBulk.put("id", id);
//            mapBulk.put("key", key);
//            mapBulk.put("value", value);
//
//            if (map.getJSONArray(id).equals(null))
//            {
//                JSONArray newListForId = new JSONArray();
//                newListForId.add(mapBulk);
//                map.put(id, newListForId);
//            } else {
//                map.getJSONArray(id).add(mapBulk);
//            }
//        }
//
//        public void opsMDExec(JSONObject opsList)
//        {
//
//        }
//
//        public void opsESExec(JSONObject opsList)
//        {
//
//        }
//        public void opsESSet(JSONObject opsList)
//        {
//
//        }

        /**
         * inc修改mongo
         * @author Rachel
         * @Date 2022/01/14
         * @param id MongoDB ID
         * @param jsonUpdate 多个修改对象
         * @param classType 表对应的实体类
         * @Return com.mongodb.client.result.UpdateResult
         * @Card
         **/
        public void incMDContent(String id, JSONObject jsonUpdate, Class<?> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            Update update = new Update();
            jsonUpdate.forEach((k, v) -> update.inc(k, (Number) v));

            update.inc("tvs", 1);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
            if (updateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), "");
            }
        }
        
        public void incMDContent(String id, String updateKey, Number updateValue, Class<?> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            Update update = new Update();
            update.inc(updateKey, updateValue);
            update.inc("tvs", 1);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
            if (updateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), "");
            }
        }


        public void pushMDContent(String id, JSONObject jsonUpdate, Class<?> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            Update update = new Update();
            jsonUpdate.forEach(update::push);

            update.inc("tvs", 1);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
            if (updateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), "");
            }
        }

        public void pushMDContent(String id, String updateKey, Object updateValue, Class<?> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            Update update = new Update();
            update.push(updateKey, updateValue);
            update.inc("tvs", 1);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
            if (updateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), "");
            }
        }


        public void pullMDContent(String id, JSONObject jsonUpdate, Class<?> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            Update update = new Update();
            jsonUpdate.forEach(update::pull);

            update.inc("tvs", 1);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
            if (updateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), "");
            }
        }

        public void pullMDContent(String id, String updateKey, Object updateValue, Class<?> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            Update update = new Update();
            update.pull(updateKey, updateValue);
            update.inc("tvs", 1);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
            if (updateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), "");
            }
        }


        /**
         * set修改mongo
         * @author Kevin
         * @Date 2022/01/14
         * @param id
         * @param classType 表对应的实体类
         * @Return com.mongodb.client.result.UpdateResult
         * @Card
         **/
        public void setMDContent(String id, JSONObject keyVal, Class<?> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            Update update = new Update();
//            keyVal.forEach(update::set);
            System.out.println("refixed setMDContent");
            keyVal.keySet().forEach(k -> update.set(k,keyVal.get(k)));
            update.inc("tvs", 1);
            UpdateResult updateResult =  mongoTemplate.updateFirst(query, update, classType);
            if (updateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), "");
            }
        }

        public void delMD(String id,  Class<?> classType) {
            // 创建查询，并且添加查询条件
            Query query = new Query(new Criteria("_id").is(id));
            // 根据查询删除信息
            mongoTemplate.remove(query,classType);
        }

        public void delMD(HashSet<String> setId,  Class<?> classType) {
            // 创建查询，并且添加查询条件
            Query query = new Query(new Criteria("_id").in(setId));
            // 根据查询删除信息
            mongoTemplate.remove(query,classType);
        }

        public void addMD( Object obj) {
            // 新增order信息

            mongoTemplate.insert(obj);
            System.out.println("got all ok Sales");

        }

        public void saveMD( Object obj) {
            // 新增order信息

            mongoTemplate.save(obj);

        }

        public JSONObject setJson(Object... val) {
            JSONObject json = new JSONObject();
            int length = val.length;
            System.out.println("length=" + length);
            for (int i = 0; i < length; i+=2) {
                json.put(val[i].toString(), val[i + 1]);
            }
            return json;
        }

        public Map setMap(Object... val) {
            Map<String, Object> map = new HashMap<>();
            int length = val.length;
            System.out.println("length=" + length);
            for (int i = 0; i < length; i+=2) {
                map.put(val[i].toString(), val[i + 1]);
            }
            return map;
        }

        public JSONArray setArray(Object... val) {
            JSONArray array = new JSONArray();
            for (Object o : val) {
                array.add(o);
            }
            return array;
        }

        public JSONObject setJson(String key, Object val)
        {
            JSONObject mapKey = new JSONObject();
            mapKey.put(key, val);
            return mapKey;
        }

        public JSONObject setJson(String key, Object val, String key2, Object val2)
        {
            JSONObject mapKey = new JSONObject();
            mapKey.put(key, val);
            mapKey.put(key2, val2);
            return mapKey;
        }
        public JSONObject setJson(String key, Object val, String key2, Object val2, String key3, Object val3)
        {
            JSONObject mapKey = new JSONObject();
            mapKey.put(key, val);
            mapKey.put(key2, val2);
            mapKey.put(key3, val3);
            return mapKey;
        }

        //Tools//////////////////////////////////////////////////------------------------------

        /**
         * 根据id_C和ref获取id_A
         * @author Rachel
         * @Date 2022/01/14
         * @param id_C 公司id
         * @param ref 编号
         * @Return java.lang.String
         * @Card
         **/

        public Asset getConfig(String id_C, String ref, String listField) {

            if(this.hasRDHashItem("login:module_id","compId-"+ id_C, ref))
            {
                String id_A = this.getRDHashStr("login:module_id","compId-" + id_C, ref);
                System.out.println("id_A"+id_A);
                return this.getMDContent(id_A, listField, Asset.class);

            } else {
                System.out.println("getConfig1"+id_C+ "   "+ref);
                JSONArray result = this.getES("lSAsset", this.setESFilt("id_C",id_C,"ref",ref), 1);

                if (result.size() == 1) {
                    String id_A = result.getJSONObject(0).getString("id_A");
                    System.out.println("getConfig1"+id_A);

                    Asset asset = this.getMDContent(id_A, listField, Asset.class);
                    this.putRDHash("login:module_id", "compId-" + id_C, ref, asset.getId());

                    return asset;
                }
                System.out.println("Result here"+result.size());
                Asset nothing = new Asset();
                nothing.setId_A("none");

                return nothing;
            }

        }

        //////////////-----------------------------------------------
        public  JSONObject list2Obj(List<?> list, String key) // key = "id_O"
            {
            JSONObject mapResult = new JSONObject();
            list.forEach(l ->{
                JSONObject json = (JSONObject) JSON.toJSON(l);
                mapResult.put(json.getString(key), json);
            });
            return mapResult;
        }

        public JSONArray list2Arr(List <?> list)
        {
            JSONArray result = new JSONArray();
            result.addAll(list);
            return result;
        }

        public  JSONObject arr2Obj(JSONArray list, String idKey) {
            JSONObject mapResult = new JSONObject();
            list.forEach(l ->{
                JSONObject json = (JSONObject) JSON.toJSON(l);
                if (json.getString(idKey) != null)
                {
                    mapResult.put(json.getString(idKey), l);
                }
            });
            return mapResult;
        }

        public <T> T jsonTo(Object data, Class<T> classType){
            return JSONObject.parseObject(JSON.toJSONString(data), classType);
        }


        public JSONObject toJson(Object data){

            return JSONObject.parseObject(JSON.toJSONString(data));
        }

        /////////////////-------------------------------------------
        public LogFlow getLogRecent(String id_O, Integer index, String id_C, boolean isSL) {

            JSONArray filt = new JSONArray();
            this.setESFilt(filt, "id_O","eq",id_O);
            this.setESFilt(filt, "index","eq",index);

            if (isSL)
                this.setESFilt(filt, "id_CS","eq",id_C);
            else
                this.setESFilt(filt, "id_C","eq",id_C);

            JSONArray result = this.getES("action, assetflow", filt,1, 1, "tmd", "desc");

            return this.jsonTo(result.getJSONObject(0), LogFlow.class);
        }

        public JSONArray contentMap(String listType, JSONArray convertArray) {
            JSONArray result = new JSONArray();
            for (Object o : convertArray) {

                //  把id的数据换成id_P的数据
                JSONObject contentMap = (JSONObject) JSONObject.toJSON(o);
//
//            if (contentMap.getJSONObject("wrdN") != null){
//                contentMap.put("wrdN", contentMap.getJSONObject("wrdN").getString(request.getHeader("lang")));
//            } if (contentMap.getJSONObject("wrddesc") != null){
//                contentMap.put("wrddesc", contentMap.getJSONObject("wrddesc").getString(request.getHeader("lang")));
//            } if (null != contentMap.getJSONObject("wrdNC")) {
//                contentMap.put("wrdNC", contentMap.getJSONObject("wrdNC").getString(request.getHeader("lang")));
//            }  if (null != contentMap.getJSONObject("wrdNCB")){
//                contentMap.put("wrdNCB", contentMap.getJSONObject("wrdNCB").getString(request.getHeader("lang")));
//            }

                switch (listType) {
                    case "lBProd":
                    case "lSProd":
                        contentMap.put("id", contentMap.get("id_P"));
                        break;
                    case "lBOrder":
                    case "lSOrder":
                        contentMap.put("id", contentMap.get("id_O"));
                        break;
                    case "lBComp":
                        contentMap.put("id", contentMap.get("id_C"));
                        break;
                    case "lBUser":
                        contentMap.put("id", contentMap.get("id_U"));
                        break;
                    case "lSComp":
                        contentMap.put("id", contentMap.get("id_CB"));
                        break;
                    case "lSAsset":
                        contentMap.put("id", contentMap.get("id_A"));
                        break;
                }

                result.add(contentMap);
            }

            return result;
        }

        //ES////////////////////////////////////////////-------------------------------

        /**
         * 新增ES data
         * @author Jevon
         * @param infoObject  object of ES data
         * @ver 1.0
         * @updated 2020/10/26 8:30
         */
        public  void addES(String index, Object infoObject) {

            //8-1 indexes = indexes + "-write";
            //指定ES索引 "assetflow" / "assetflow-write / assetflow-read
            try {
                IndexRequest request = new IndexRequest(index);
                JSONObject data = this.toJson(infoObject);
                data.put("tmk", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

                request.source(data, XContentType.JSON);

                client.index(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }

        public JSONArray getES(String index, JSONArray filterArray) {
            return this.getES(index, filterArray, 1, 10000, "tmd", "desc");
        }

        public JSONArray getES(String index, JSONArray filterArray, Integer size)  {
            return this.getES(index, filterArray, 1, size, "tmd", "desc");
        }

        public JSONArray getES(String index, JSONArray filterArray, Integer page, Integer size, String sortKey, String sortOrder) {

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder queryBuilder = new BoolQueryBuilder();

            this.filterBuilder(filterArray, queryBuilder);
            sourceBuilder.query(queryBuilder).from((page - 1) * size).size(size);
            if (sortOrder.equals("desc"))
                sourceBuilder.sort(sortKey, SortOrder.DESC);
            else
                sourceBuilder.sort(sortKey, SortOrder.ASC);
            try {
                SearchRequest request = new SearchRequest();

                String[] indices = index.split(",");
                request.indices(indices);
                request.source(sourceBuilder);
                SearchResponse response = client.search(request, RequestOptions.DEFAULT);
                System.out.println(response);

                JSONArray abc = this.hit2Array(response);

                return abc;

            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }

        private JSONArray hit2Array(SearchResponse response)
        {
            JSONArray result = new JSONArray();
            if (response.getHits().getHits().length > 0)
            {
                for (SearchHit hit : response.getHits().getHits()) {
                    JSONObject mapHit = this.toJson(hit.getSourceAsMap());
                    mapHit.put("id_ES", hit.getId());
                    result.add(mapHit);
                }
            }
            return result;
        }


        public void delES(String index, JSONArray filterArray)
        {
            try {
                DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(index);
                BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
                this.filterBuilder(filterArray, queryBuilder);
                deleteByQueryRequest.setQuery(queryBuilder);
                client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);

            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }

        public long countES(String index, JSONArray filterArray) {
            CountResponse countResponse;
            try {
                BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
                this.filterBuilder(filterArray, queryBuilder);
                CountRequest countRequest = new CountRequest(index).query(queryBuilder);
                countResponse = client.count(countRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
            return countResponse.getCount();

        }
        public void setES(String listType, JSONArray filterArray, JSONObject listCol) {

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            try {
                BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
                this.filterBuilder(filterArray, queryBuilder);

                searchSourceBuilder.query(queryBuilder).size(5000);
                SearchRequest srb = new SearchRequest(listType);

                srb.source(searchSourceBuilder);

                SearchResponse response = client.search(srb, RequestOptions.DEFAULT);
                BulkRequest bulk = new BulkRequest();

                for (SearchHit hit : response.getHits().getHits()) {

                    hit.getSourceAsMap().put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                    hit.getSourceAsMap().putAll(listCol);

                    bulk.add(new UpdateRequest(listType, hit.getId()).doc(hit.getSourceAsMap()));

                }
                client.bulk(bulk, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }

        }

        public void setES(String listType, String id, JSONObject listCol) {
            UpdateRequest updateRequest = new UpdateRequest(listType, id);
            listCol.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            listCol.remove("id_ES");

            updateRequest.doc(listCol, XContentType.JSON);
            try {
                client.update(updateRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }

        }


        public void delES(String index,String id)
        {
            // 2、定义请求对象
            DeleteRequest request = new DeleteRequest(index);
            try {

                request.id(id);
                DeleteResponse response;
                response = client.delete(request, RequestOptions.DEFAULT);
                // 4、处理响应结果
                System.out.println("删除是否成功:" + response.getResult());
            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }


        public JSONArray setESFilt (Object key, String method, Object val )
        {
            JSONArray filterArray = new JSONArray();
            JSONObject newFilt = new JSONObject();
            newFilt.put("filtKey", key);
            newFilt.put("method", method);
            newFilt.put("filtVal", val);

            filterArray.add(newFilt);
            return filterArray;
        }

        public JSONArray setESFilt (Object key, Object val )
        {
            JSONArray filterArray = new JSONArray();
            JSONObject newFilt = new JSONObject();
            newFilt.put("filtKey", key);
            newFilt.put("method", "eq");
            newFilt.put("filtVal", val);

            filterArray.add(newFilt);
            return filterArray;
        }

        public JSONArray setESFilt (Object key, Object val, Object key2, Object val2 )
        {
            JSONArray filterArray = new JSONArray();

            JSONObject newFilt = new JSONObject();
            newFilt.put("filtKey", key);
            newFilt.put("method", "eq");
            newFilt.put("filtVal", val);

            JSONObject newFilt2 = new JSONObject();

            newFilt2.put("filtKey", key2);
            newFilt2.put("method", "eq");
            newFilt2.put("filtVal", val2);
            filterArray.add(newFilt);
            filterArray.add(newFilt2);

            return filterArray;
        }

        public JSONArray setESFilt (Object key, Object val, Object key2, Object val2, Object key3, Object val3)
        {
            JSONArray filterArray = new JSONArray();

            JSONObject newFilt = new JSONObject();
            newFilt.put("filtKey", key);
            newFilt.put("method", "eq");
            newFilt.put("filtVal", val);

            JSONObject newFilt2 = new JSONObject();

            newFilt2.put("filtKey", key2);
            newFilt2.put("method", "eq");
            newFilt2.put("filtVal", val2);
            JSONObject newFilt3 = new JSONObject();

            newFilt3.put("filtKey", key3);
            newFilt3.put("method", "eq");
            newFilt3.put("filtVal", val3);

            filterArray.add(newFilt);
            filterArray.add(newFilt2);
            filterArray.add(newFilt3);

            return filterArray;
        }

        public void setESFilt (JSONArray filterArray, Object key, String method, Object val )
        {
            JSONObject newFilt = new JSONObject();
            newFilt.put("filtKey", key);
            newFilt.put("method", method);
            newFilt.put("filtVal", val);

            filterArray.add(newFilt);
        }


        public void setRefAuto(String id_C, String type, JSONObject jsonRefAuto)
        {
            Asset asset = this.getConfig(id_C, "a-core", "refAuto");
            this.setMDContent(asset.getId(), this.setJson("refAuto."+type,jsonRefAuto), Asset.class);
        }

        public JSONObject getRefAuto(String id_C, String type) {
            Asset asset = this.getConfig(id_C, "a-core", "refAuto."+type);
            if (asset.getRefAuto().getJSONObject(type)  == null )
            {
                this.setMDContent(asset.getId(),this.setJson("refAuto."+type, new JSONObject()), Asset.class);
                return new JSONObject();
            } else {
                return asset.getRefAuto().getJSONObject(type);
            }
        }

        public void setCookiex(String id_U, String id_C, String type, Object cookieData) {

            this.setMDContent(id_U, this.setJson("cookiex." + id_C + "." + type,  cookieData), User.class);

        }

        public Object getCookiex(String id_U, String id_C, String type) {
            User user = this.getMDContent(id_U,  "cookiex." + id_C + "." + type, User.class);

            return  user.getCookiex().getJSONObject(id_C).get(type);
        }

        public void filterBuilder (JSONArray filterArray, BoolQueryBuilder queryBuilder)
        {
            //条件数组不为空
            if (filterArray.size() > 0) {
                int i = 0;
                while (i < filterArray.size()) {
                    //拿到每一组筛选条件
                    JSONObject conditionMap =  filterArray.getJSONObject(i);
                    String method = conditionMap.getString("method");
                    switch (method) {
                        case "exact":
                            //精确查询，速度快不分词，查keyword类型
                            queryBuilder.must(QueryBuilders.termQuery(conditionMap.getString("filtKey"), conditionMap.get("filtVal")));
                            break;
                        case "eq":
                            //模糊查询，必须匹配所有分词
                            queryBuilder.must(QueryBuilders.matchPhraseQuery(conditionMap.getString("filtKey"), conditionMap.get("filtVal")));
                            break;
                        case "ma":
                            //模糊查询，匹配任意一个分词
                            queryBuilder.must(QueryBuilders.matchQuery(conditionMap.getString("filtKey"), conditionMap.get("filtVal")));
                            break;
                        case "gte":
                            //大于等于
//                    if (pattern.matcher((CharSequence) conditionMap.get("filtVal")).matches())
                            queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey")).gte(conditionMap.get("filtVal")));
                            break;
                        case "gt":
                            //大于
                            queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey")).gt(conditionMap.get("filtVal")));
                            break;
                        case "lte":
                            //小于等于
                            queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey")).lte(conditionMap.get("filtVal")));
                            break;
                        case "lt":
                            //小于
                            queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey")).lt(conditionMap.get("filtVal")));
                            break;
                        case "range": {
                            //范围查询, 大于等于～小于
                            JSONArray arrayValue = conditionMap.getJSONArray("filtVal");
                            queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey")).gte(arrayValue.get(0)).lt(arrayValue.get(1)));
                            break;
                        }
                        case "mixeq": {
                            //复杂查询，可一对一，一对多，多对一，多对多
                            JSONArray arrayKey = conditionMap.getJSONArray("filtKey");
                            JSONArray arrayValue = conditionMap.getJSONArray("filtVal");
                            String value = StringUtils.join(arrayValue, " OR ");
                            System.out.println("value=" + value);
                            QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(value);
                            for (int j = 0; j < arrayKey.size(); j++) {
                                queryStringQueryBuilder = queryStringQueryBuilder.field(arrayKey.getString(j));
                            }
                            queryBuilder.must(queryStringQueryBuilder);
                            break;
                        }
                        case "nexact":
                            //精确查询，不分词
                            queryBuilder.mustNot(QueryBuilders.termQuery(conditionMap.getString("filtKey"), conditionMap.get("filtVal")));
                            break;
                        case "ne":
                            //模糊查询，匹配任意一个分词
                            queryBuilder.mustNot(QueryBuilders.matchQuery(conditionMap.getString("filtKey"), conditionMap.get("filtVal")));
                            break;
                        case "mixne": {
                            //复杂查询，可一对一，一对多，多对一，多对多
                            JSONArray arrayKey = conditionMap.getJSONArray("filtKey");
                            JSONArray arrayValue = conditionMap.getJSONArray("filtVal");
                            String value = StringUtils.join(arrayValue, " OR ");
                            System.out.println("value=" + value);
                            QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(value);
                            for (int j = 0; j < arrayKey.size(); j++) {
                                queryStringQueryBuilder = queryStringQueryBuilder.field(arrayKey.getString(j));
                            }
                            queryBuilder.mustNot(queryStringQueryBuilder);
                            break;
                        }
                        case "contain":
                            //设定条件  OR    contain：包含
                            String joinStr = StringUtils.join((List<String>) conditionMap.get("filtVal"), " OR ");
                            queryBuilder.must(QueryBuilders.queryStringQuery(joinStr).field(conditionMap.getString("filtKey")));
                            break;
                        case "timeRange":
                            JSONArray filtList = conditionMap.getJSONArray("filtVal");
                            //.from（）是时间格式，.gte（）.lte（）  时间范围
//                            queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey"))
//                                    .from(DateEnum.DATE_TIME_FULL.getDate()).gte(filtList.get(0))
//                                    .lte(filtList.get(1)));

                            queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey"))
                                    .from(filtList.get(0), true).to(filtList.get(1),false));
                            break;
                        case "sheq": {
                            BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                            JSONArray arrayFiltKey = conditionMap.getJSONArray("filtKey");
                            for (int j = 0; j < arrayFiltKey.size(); j++) {
                                shouldQueryBuilder.should(QueryBuilders.matchPhraseQuery(arrayFiltKey.getString(j), conditionMap.get("filtVal")));
                            }
                            queryBuilder.must(shouldQueryBuilder);
                            break;
                        }
//                        case "shex": {
//                            BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
//                            JSONArray arrayFiltKey = conditionMap.getJSONArray("filtKey");
//                            for (int j = 0; j < arrayFiltKey.size(); j++) {
//                                shouldQueryBuilder.should(QueryBuilders.termQuery(arrayFiltKey.getString(j), conditionMap.get("filtVal")));
//                            }
//                            queryBuilder.must(shouldQueryBuilder);
//                            break;
//                        }
                        case "shne": {
                            BoolQueryBuilder shouldQueryBuilder = new BoolQueryBuilder();
                            JSONArray arrayFiltKey = conditionMap.getJSONArray("filtKey");
                            for (int j = 0; j < arrayFiltKey.size(); j++) {
                                shouldQueryBuilder.should(QueryBuilders.termQuery(arrayFiltKey.getString(j), conditionMap.get("filtVal")));
                            }
                            queryBuilder.mustNot(shouldQueryBuilder);
                            break;
                        }
                        default:
                            throw new IllegalStateException("Unexpected value: " + method);
                    }
                    i++;
                }
            }
        }


        //Redis//////////////////////////////////////////////////------------------------------

        public boolean hasRDKey(String collection, String key)
        {
            return Boolean.TRUE.equals(redisTemplate0.hasKey(collection + ":" + key));
        }

        public boolean hasRDHashItem(String collection, String hash, String key)
        {
            if (redisTemplate0.hasKey(collection + ":" + hash))
                return redisTemplate0.opsForHash().hasKey(collection + ":" + hash, key);
            else
                return false;
        }


        // 1. put -Hash, set - Set, get
        // Collection : Hash
        // Set Collection:Key

        public void putRDHash(String collection, String hash, String key, Object val)
        {
            redisTemplate0.opsForHash().put(collection + ":" + hash, key, val.toString());
            redisTemplate0.expire(collection + ":" + hash, 1000, TimeUnit.HOURS);
        }

        public void putRDHashMany(String collection, String hash, JSONObject data,  Long second)
        {
            redisTemplate0.opsForHash().putAll(collection + ":" + hash, data);
            redisTemplate0.expire(collection + ":" + hash, second, TimeUnit.SECONDS);
        }
        public void putRDHash(String collection, String hash, String key, Object val, Long second)
        {
            redisTemplate0.opsForHash().put(collection + ":" + hash, key, val.toString());
            redisTemplate0.expire(collection + ":" + hash, second, TimeUnit.SECONDS);
        }

        public void setRDSet(String collection, String key, Object val)
        {
            redisTemplate0.opsForValue().set(collection + ":" + key, val.toString());
            redisTemplate0.expire(collection + ":" + key, 1000, TimeUnit.HOURS);
        }
        public void setRDSet(String collection, String key, Object val, Long second)
        {
            redisTemplate0.opsForValue().set(collection + ":" + key, val.toString(), second, TimeUnit.SECONDS);
        }

        public void setRDExpire(String collection, String key, Long second)
        {
            redisTemplate0.expire(collection + ":" + key, second, TimeUnit.SECONDS);
        }

        public JSONObject getRDHash(String collection, String hash, String key)
        {
            String result = (String) redisTemplate0.opsForHash().get(collection + ":" + hash, key);
            return JSONObject.parseObject(result);
        }

        public Map <Object, Object> getRDHashAll(String collection, String hash)
        {
            Map<Object,Object> result = redisTemplate0.opsForHash().entries(collection + ":" + hash);
            return result;
        }

        public JSONObject getRDSet(String collection, String key)
        {
            String result = redisTemplate0.opsForValue().get(collection + ":" + key);

            System.out.println("result:"+result);
            return JSONObject.parseObject(result);
        }

        public String getRDSetStr(String collection, String key)
        {
            return (String) redisTemplate0.opsForValue().get(collection + ":" + key);
        }

        public String getRDHashStr(String collection, String hash, String key)
        {
            return (String) redisTemplate0.opsForHash().get(collection + ":" + hash, key);
        }
        
        public void delRD(String collection, String hash)
        {
            redisTemplate0.delete(collection + ":"+ hash);
        }

        public void delRDHashItem(String collection, String hash, String key)
        {
            redisTemplate0.opsForHash().delete(collection + ":"+ hash, key);
        }



        public JSONObject cloneJSONObject(JSONObject json) {
            String jsonString = json.toJSONString();
            JSONObject jsonObject = JSON.parseObject(jsonString);
            return jsonObject;
        }

        public JSONArray cloneJSONArray(JSONArray json) {
            String jsonString = json.toJSONString();
            JSONArray jsonArray = JSON.parseArray(jsonString);
            return jsonArray;
        }







    }
