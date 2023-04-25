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
import com.cresign.tools.pojo.es.lBAsset;
import com.cresign.tools.pojo.es.lSAsset;
import com.cresign.tools.pojo.po.*;
import com.cresign.tools.pojo.po.assetCard.AssetAStock;
import com.cresign.tools.pojo.po.assetCard.AssetInfo;
import com.cresign.tools.reflectTools.ApplicationContextTools;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
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
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
    public class Qt {

        @Autowired
        private MongoTemplate mongoTemplate;

        @Autowired
        private RestHighLevelClient client;

        @Autowired
        private StringRedisTemplate redisTemplate0;

        public JSONObject initData = new JSONObject();

        /**
         * 注入RocketMQ模板
         */
        @Autowired
        private RocketMQTemplate rocketMQTemplate;

        //MDB - done: get, getMany, del, new, inc, set, push, pull
        //MDB - need: ops, opsExec
        //ESS - done: get, getFilt, del, add, set
        //ESS - need: ops, opsExec
        //RED - done: set/hash, hasKey, put/set, get expire
        //Other - done: toJson, jsonTo, list2Map, getConfig, filterBuilder
        //Other - need: getRecentLog, judgeComp, chkUnique,, checkOrder, updateSize

//        public static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
//            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
//            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
//            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
//            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
//            "W", "X", "Y", "Z" };
//
//
//        public String getRKey() {
//            StringBuffer rKey = new StringBuffer();
//            String uuid = UUID.randomUUID().toString().replace("-", "");
//            for (int i = 0; i < 8; i++) {
//                String str = uuid.substring(i * 4, i * 4 + 4);
//                int x = Integer.parseInt(str, 16);
//                rKey.append(chars[x % 0x3E]);
//            }
//            return rKey.toString();
//
//        }

        /**
         * 发送MQ信息
         * @param destination MQ标签
         * @param db	发送内容
         * @return 返回结果:
         * @author tang
         * @date 创建时间: 2023/4/15
         * @ver 版本号: 1.0.0
         */
        public void sendMQ(String destination,JSONObject db){
            rocketMQTemplate.convertAndSend(destination, db);
        }
        public void sendMQRearEnd(String id_C,String id_U,String noticeType){
//            Asset asset = getConfig(id_C,"a-auth","flowControl");
            JSONArray result = this.getES("lBUser", this.setESFilt("id_CB",id_C,"grpU","1001"));
            System.out.println(JSON.toJSONString(result));
            LogFlow logContent = LogFlow.getInstance();
            logContent.setId_C(id_C);
            logContent.setId_U(id_U);
            logContent.setLogType("notice");
            logContent.setSubType("hd");
            logContent.setId(null);
            logContent.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            JSONObject logData = new JSONObject();
            JSONArray id_Us = new JSONArray();
            for (int i = 0; i < result.size(); i++) {
                JSONObject jsonObject = result.getJSONObject(i);
                id_Us.add(jsonObject.getString("id_U"));
            }
//            if (!asset.getId().equals("none")) {
//                LogFlow logContent = LogFlow.getInstance();
//                logContent.setId_C(id_C);
//                logContent.setId_U(id_U);
//                logContent.setLogType("notice");
//                logContent.setSubType("hd");
//                logContent.setId(null);
//                logContent.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//                JSONObject logData = new JSONObject();
//                JSONArray id_Us = new JSONArray();
//                JSONArray cidArray = new JSONArray();
//                // 获取卡片信息
//                JSONObject flowControl = asset.getFlowControl();
//                // 获取卡片data信息
//                JSONArray flowData = flowControl.getJSONArray("objData");
//                // 遍历data
//                for (int i = 0; i < flowData.size(); i++) {
//                    // 获取i对应的data信息
//                    JSONObject roomSetting = flowData.getJSONObject(i);
//                    // 获取群id
//                    String roomId = roomSetting.getString("id");
//                    // 获取日志群id
//                    String logFlowId = logContent.getId();
//                    if (roomSetting.getString("type").endsWith("SL")) {
//                        logFlowId = logContent.getId_FS();
//                    }
//                    // 判断群id一样
//                    if (roomId.equals(logFlowId)) {
////                System.out.println("进入各FC 的loop: id=" + logContent.getId());
//                        // 获取群用户id列表
//                        JSONArray objUser = roomSetting.getJSONArray("objUser");
////                System.out.println("objUser:" + JSON.toJSONString(objUser));
//                        // 创建存储推送用户信息
////                    System.out.println("当前服务标志:" + WebSocketUserServer.bz);
//                        // 遍历群用户id列表
//                        for (int j = 0; j < objUser.size(); j++) {
//                            // 获取j对应的群用户信息
//                            JSONObject thisUser = objUser.getJSONObject(j);
//                            // 获取群用户id
//                            id_Us.add(thisUser.getString("id_U"));
//
//                            // 判断群用户id不等于当前ws连接用户id
//                            if (thisUser.getInteger("imp") <= logContent.getImp()) {
//                                String id_client = thisUser.getString("id_APP");
//                                if (null != id_client && !"".equals(id_client)) {
//                                    cidArray.add(id_client);
//                                }
//                            }
//                        }
//                    }
//                }
//                logData.put("id_Us",id_Us);
//                logData.put("cidArray",cidArray);
//                logData.put("noticeType",noticeType);
//                logContent.setData(logData);
//                sendMQ("chatTopic:chatTap",JSONObject.parseObject(JSON.toJSONString(logContent)));
//            }
            logData.put("id_Us",id_Us);
            logData.put("noticeType",noticeType);
            logContent.setData(logData);
            sendMQ("chatTopic:chatTap",JSONObject.parseObject(JSON.toJSONString(logContent)));
        }

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

        public InitJava getInitData()
        {
            if (initData.get("cn_java") == null)
            {
                InitJava result = this.getMDContent("cn_java", "", InitJava.class);
                initData.put("cn_java", result);
                return result;
            }
            return this.jsonTo(initData.get("java"), InitJava.class);
        }
        public Init getInitData(String lang)
        {
            if (initData.get(lang) == null)
            {
                //get init, then save to initData
                    Init result = this.getMDContent(lang, "", Init.class);
                    initData.put(lang, result);
                    return result;
            }
            return this.jsonTo(initData.get(lang), Init.class);
        }
        public void updateInitData(String lang, String key, Object val) {
            JSONObject jsonUpdate = this.setJson(key, val);
            if (lang.endsWith("java")) {
                this.setMDContent(lang, jsonUpdate, InitJava.class);
            } else {
                this.setMDContent(lang, jsonUpdate, Init.class);
            }
        }
        public Integer replaceInitData(String lang) {
            if (lang.endsWith("java")) {
                InitJava initJava = this.getMDContent(lang, "", InitJava.class);
                initData.put(lang, initJava);
                return initJava.getVer();
            } else {
                Init init = this.getMDContent(lang, "", Init.class);
                initData.put(lang, init);
                return init.getVer();
            }
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
            Query query = new Query(new Criteria("_id").in(setIds));
            if (fields != null) {
                for (Object field : fields)
                {
                    query.fields().include(field.toString());
                }
            }
            try {
                return mongoTemplate.find(query, classType);
            } catch (Exception e)
            {
                System.out.println("error"+e);
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }

        public List<?> getMDContentMany(HashSet setIds, String field, Class<?> classType) {

            Query query = new Query(new Criteria("_id").in(setIds));
            if (field != null && !field.equals("")) {
                query.fields().include(field);
            }
            try {
                return mongoTemplate.find(query, classType);
            } catch (Exception e)
            {
                System.out.println("error"+e);
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }

        public List<String> strList(String... str)
        {
            List<String> result = new ArrayList<>();
            for (String s : str) {
                result.add(s);
            }
            return result;
        }

        public HashSet str(String... str)
        {
            HashSet result = new HashSet();
            for (String s : str) {
                result.add(s);
            }
            return result;
        }

        public void errPrint(String title, Exception e, Object... vars)
        {
            System.out.println("****[" +title+"]****");

//            System.out.println("[");
            for (Object item : vars)
            {
                if (item.getClass().toString().startsWith("class java.util.Array"))
                {
                    System.out.println(this.toJArray(item));
                }
                else if (item.getClass().toString().startsWith("class com.cresign.tools.pojo") ||
                        item.getClass().toString().startsWith("class java.util"))
                {
                    System.out.println(this.toJson(item));
                }
                else if (item!= null) {
//                    System.out.println(item.getClass());
                    System.out.println(item);
                } else {
                    System.out.println("....null");
                }
            }
            System.out.println("*****[End]*****");

            if (e != null)
                e.printStackTrace();
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
            try {
                mongoTemplate.updateFirst(query, update, classType);
            } catch (Exception e)
            {
                System.out.println("error"+e);
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }

        }

        public void incMDContent(String id, String updateKey, Number updateValue, Class<?> classType) {
            Query query = new Query(new Criteria("_id").is(id));
            Update update = new Update();
            update.inc(updateKey, updateValue);
            update.inc("tvs", 1);
            try {
                mongoTemplate.updateFirst(query, update, classType);
            } catch (Exception e)
            {
                System.out.println("error"+e);
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
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
            try {
                Query query = new Query(new Criteria("_id").is(id));
                Update update = new Update();

//                keyVal.keySet().forEach(k -> update.set(k, keyVal.get(k)));
                for (String key : keyVal.keySet())
                {
                    update.set(key,keyVal.get(key));
                }
                update.inc("tvs", 1);
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
                System.out.println("inSetMD" + updateResult);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
//            if (updateResult.getModifiedCount() == 0) {
//                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), "");
//            }
        }
        public void setMDContentMany(HashSet setId, JSONObject keyVal, Class<?> classType) {
            try {
                Query query = new Query(new Criteria("_id").in(setId));
                Update update = new Update();

//                keyVal.keySet().forEach(k -> update.set(k, keyVal.get(k)));
                for (String key : keyVal.keySet())
                {
                    update.set(key,keyVal.get(key));
                }
                update.inc("tvs", 1);
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
                System.out.println("inSetMD" + updateResult);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        public void setMDContentMany(List<JSONObject> listBulk, Class<?> classType) {
            System.out.println("listBulk=" + listBulk);
            try {
                BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, classType);
                listBulk.forEach(jsonBulk -> {
                    String type = jsonBulk.getString("type");
                    if (type.equals("insert")) {
                        bulk.insert(JSON.parseObject(jsonBulk.getJSONObject("insert").toJSONString(), classType));
                    } else if (type.equals("update")) {
                        Query query = new Query(new Criteria("_id").is(jsonBulk.getString("id")));

                        Update update = new Update();
                        for (String key : jsonBulk.getJSONObject("update").keySet())
                        {
                            Object val = jsonBulk.getJSONObject("update").get(key);
                            if (val == null) {
                                update.unset(key);
                            } else {
                                update.set(key, val);
                            }
                        }
                        update.inc("tvs", 1);
                            bulk.updateOne(query, update);

                    } if (type.equals("delete")) {
                        Query query = new Query(new Criteria("_id").is(jsonBulk.getString("id")));
                        bulk.remove(query);
                    }
                });
                BulkWriteResult execute = bulk.execute();
            } catch (Exception e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }

        public void delMD(String id,  Class<?> classType) {
            // 创建查询，并且添加查询条件
            Query query = new Query(new Criteria("_id").is(id));
            // 根据查询删除信息
            try {
                mongoTemplate.remove(query,classType);
            } catch (Exception e)
            {
                System.out.println("error"+e);
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }

        public void delMD(HashSet<String> setId,  Class<?> classType) {
            // 创建查询，并且添加查询条件
            Query query = new Query(new Criteria("_id").in(setId));
            // 根据查询删除信息
            try {
                mongoTemplate.remove(query,classType);
            } catch (Exception e)
            {
                System.out.println("error"+e);
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }

        public void addMD( Object obj) {
            // 新增order信息
            try {
                mongoTemplate.insert(obj);
            } catch (Exception e)
            {
                System.out.println("error"+e);
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }

        }

        public void saveMD( Object obj) {
            // 新增order信息
            try {
            mongoTemplate.save(obj);
            } catch (Exception e)
            {
                System.out.println("error"+e);
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }

        }

        public JSONObject setJson(Object... val) {
            JSONObject json = new JSONObject();
            int length = val.length;
            for (int i = 0; i < length; i+=2) {
                json.put(val[i].toString(), val[i + 1]);
            }
            return json;
        }

        public void upJson(JSONObject upJson, Object... val)
        {
            int length = val.length;
            for (int i = 0; i < length; i+=2) {
                upJson.put(val[i].toString(), val[i + 1]);
            }

        }

        public Map setMap(Object... val) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < val.length; i+=2) {
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

//        public JSONObject setJson(String key, Object val)
//        {
//            JSONObject mapKey = new JSONObject();
//            mapKey.put(key, val);
//            return mapKey;
//        }
//
//        public JSONObject setJson(String key, Object val, String key2, Object val2)
//        {
//            JSONObject mapKey = new JSONObject();
//            mapKey.put(key, val);
//            mapKey.put(key2, val2);
//            return mapKey;
//        }
//        public JSONObject setJson(String key, Object val, String key2, Object val2, String key3, Object val3)
//        {
//            JSONObject mapKey = new JSONObject();
//            mapKey.put(key, val);
//            mapKey.put(key2, val2);
//            mapKey.put(key3, val3);
//            return mapKey;
//        }

        //Tools//////////////////////////////////////////////////------------------------------

        /**
         * 根据id_C和ref获取id_A
         * @author Rachel
         * @Date 2022/01/14
         * @param id_C 公司id
         * @Return java.lang.String
         * @Card
         **/

        public int judgeComp(String id_C,String compOther){

            JSONArray result = this.getES("lSAsset", this.setESFilt("id_C",compOther,"ref","a-auth"), 1);
            // 2 = same comp
            // 1 = real comp
            // 0 = fake comp
            if (id_C.equals(compOther) && result.size() == 1){
                return 2;
            }else{
                if (result.size() == 1) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }


    public Asset getConfig(String id_C, String ref, String listField) {

            return getConfig(id_C, ref, this.strList(listField));
        }

        public String getId_A(String id_C, String ref)
        {
            JSONArray result = this.getES("lSAsset", this.setESFilt("id_C",id_C,"ref",ref), 1);
            if (result.size() == 1) {
                return result.getJSONObject(0).getString("id_A");
            } else
                return "";
        }


        public Asset getConfig(String id_C, String ref, List <String> listField) {

            if(this.hasRDHashItem("login:module_id","compId-"+ id_C, ref))
            {
                String id_A = this.getRDHashStr("login:module_id","compId-" + id_C, ref);
                System.out.println("id_A from redis"+id_A);
                return this.getMDContent(id_A, listField, Asset.class);
            } else {
                System.out.println("getConfig from ES"+id_C+ "   "+ref);
                JSONArray result = this.getES("lSAsset", this.setESFilt("id_C",id_C,"ref",ref,"grp","1003"), 1);

                if (result.size() == 1) {
                    String id_A = result.getJSONObject(0).getString("id_A");
                    System.out.println("getConfig ES"+id_A);

                    Asset asset = this.getMDContent(id_A, listField, Asset.class);
                    this.putRDHash("login:module_id", "compId-" + id_C, ref, asset.getId());

                    return asset;
                }
                Asset nothing = new Asset();
                nothing.setId("none");

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

        public ArrayList <?> arr2List(JSONArray array)
        {
//            ArrayList<String> listCard = this.jsonTo(array, ArrayList.class);
            return this.jsonTo(array, ArrayList.class);
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

        public JSONArray toJArray(Object data){

            return  JSONArray.parseArray(JSON.toJSONString(data));
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

            JSONArray result = this.getES("action/assetflow", filt,1, 1, "tmd", "desc");

            return this.jsonTo(result.getJSONObject(0), LogFlow.class);
        }

        public JSONArray contentMap(String listType, JSONArray convertArray) {
            JSONArray result = new JSONArray();
            for (Object o : convertArray) {

                //  把id的数据换成id_P的数据
                JSONObject contentMap = (JSONObject) JSONObject.toJSON(o);

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

                String[] indices = index.split("/");
                request.indices(indices);
                request.source(sourceBuilder);
                SearchResponse response = client.search(request, RequestOptions.DEFAULT);
                System.out.println(response);

                return this.hit2Array(response);

            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }

        public JSONArray hit2Array(SearchResponse response)
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
                String[] indices = index.split("/");

                BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
                this.filterBuilder(filterArray, queryBuilder);
                CountRequest countRequest = new CountRequest(indices).query(queryBuilder);
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
                if (response.getHits().getHits().length > 0) {
                    client.bulk(bulk, RequestOptions.DEFAULT);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }

        }

        public void setES(String listType, String id_ES, JSONObject listCol) {
            UpdateRequest updateRequest = new UpdateRequest(listType, id_ES);
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



        public void setESMany(String logType, List<JSONObject> listBulk) {
            try {
                System.out.println("listBulk=" + listBulk);
                BulkRequest bulk = new BulkRequest();
                listBulk.forEach(jsonBulk ->{
                    String type = jsonBulk.getString("type");
                    String logT = jsonBulk.getString("logType") == null ? logType : jsonBulk.getString("logType");
                    if (type.equals("insert")) {
                        bulk.add(new IndexRequest(logT).source(jsonBulk.getJSONObject("insert")));
                    } else if (type.equals("update")) {
                        JSONObject jsonEs = jsonBulk.getJSONObject("update");
                        jsonEs.remove("id_ES");
                        bulk.add(new UpdateRequest(logT, jsonBulk.getString("id")).doc(jsonEs, XContentType.JSON));
                    } else if (type.equals("delete")) {
                        bulk.add(new DeleteRequest(logT, jsonBulk.getString("id")));
                    }
                });
                if (listBulk.size() > 0) {
                    client.bulk(bulk, RequestOptions.DEFAULT);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
            }
        }


        public void delES(String index,String id) //getES 有处理 id_ES
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

        public JSONArray setESFilt(Object... val) {
            JSONArray filterArray = new JSONArray();

            for (int i = 0; i < val.length; i+=2) {
                JSONObject json = new JSONObject();
                json.put("filtKey", val[i].toString());
                json.put("method", "exact");
                json.put("filtVal", val[i + 1]);
                filterArray.add(json);
            }
            return filterArray;
        }

        public Class getClassType(String table) {
            if (table.endsWith("Comp") || table.startsWith("id_C")) {
                return Comp.class;
            } else if (table.endsWith("Order") || table.startsWith("id_O")) {
                return Order.class;
            } else if (table.endsWith("User") || table.startsWith("id_U")) {
                return User.class;
            } else if (table.endsWith("Prod") || table.startsWith("id_P")) {
                return Prod.class;
            } else {
                return Asset.class;
            }
        }

        public String formatMs(Long ms) {
            Integer ss = 1000;
            Integer mm = ss * 60;
            Integer hh = mm * 60;
            Integer dd = hh * 24;
            Long day = ms / dd;
            Long hour = (ms - day * dd) / hh;
            Long minute = (ms - day * dd - hour * hh) / mm;
            Long second = (ms - day * dd - hour * hh - minute * mm) / ss;
            StringBuffer sb = new StringBuffer();
            if(day > 0) {
                sb.append(day+"天");
            }
            if(hour > 0) {
                sb.append(hour).append("时");
            }
            if(minute > 0) {
                sb.append(minute).append("分");
            }
            if(second > 0) {
                sb.append(second).append("秒");
            }
            return sb.toString();
        }

        public void checkCapacity(String id_C, Long fileSize) {
//            String id_A = getId_A(id_C, "a-core");
//            Asset asset = getMDContent(id_A, "powerup.capacity", Asset.class);
            Asset asset = getConfig(id_C, "a-core", Arrays.asList("powerup.capacity", "view"));

            if (asset.getPowerup() == null)
            {
                JSONObject power =  this.getInitData().getNewComp().getJSONObject("a-core").getJSONObject("powerup");
                JSONArray newView = asset.getView();
                newView.add("powerup");
                this.setMDContent(asset.getId(), this.setJson("powerup", power, "view", newView), Asset.class);
                asset.setPowerup(power);
            }
            JSONObject jsonCapacity = asset.getPowerup().getJSONObject("capacity");
            //超额
            if (fileSize > 0 && fileSize + jsonCapacity.getLong("used") > jsonCapacity.getLong("total")) {
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.POWER_NOT_ENOUGH.getCode(), null);
            }
            JSONObject jsonUpdate = setJson("powerup.capacity.used", fileSize);
            incMDContent(asset.getId(), jsonUpdate, Asset.class);
        }

        public void setESFilt (JSONArray filterArray, Object key, String method, Object val )
        {
            JSONObject newFilt = new JSONObject();
            newFilt.put("filtKey", key);
            newFilt.put("method", method);
            newFilt.put("filtVal", val);

            filterArray.add(newFilt);
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
                        case "":
                            //下一个
                            break;
                        case "exact":
                            //精确查询，速度快不分词，查keyword类型
                            queryBuilder.must(QueryBuilders.termQuery(conditionMap.getString("filtKey"), conditionMap.get("filtVal")));
                            break;
                        case "eq":
                            //模糊查询，必须匹配所有分词
                            queryBuilder.must(QueryBuilders.matchPhraseQuery(conditionMap.getString("filtKey"), conditionMap.get("filtVal")));
                            break;
                        case "null":
                            //模糊查询，必须匹配所有分词
                            queryBuilder.mustNot(QueryBuilders.existsQuery(conditionMap.getString("filtKey")));
                            break;
                        case "notnull":
                            //模糊查询，必须匹配所有分词
                            queryBuilder.must(QueryBuilders.existsQuery(conditionMap.getString("filtKey")));
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
                        case "shouldeq": {
                            //复杂查询，可一对一，一对多，多对一，多对多
                            JSONArray arrayKey = conditionMap.getJSONArray("filtKey");
                            JSONArray arrayValue = conditionMap.getJSONArray("filtVal");
                            String value = StringUtils.join(arrayValue, " OR ");
                            System.out.println("value=" + value);
                            QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(value);
                            for (int j = 0; j < arrayKey.size(); j++) {
                                queryStringQueryBuilder = queryStringQueryBuilder.field(arrayKey.getString(j));
//                                queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey")).gte(arrayValue.get(0)).lt(arrayValue.get(1)));

                            }
                            queryBuilder.should(queryStringQueryBuilder);
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
//                        case "timeRange":
//                            JSONArray filtList = conditionMap.getJSONArray("filtVal");
//                            //.from（）是时间格式，.gte（）.lte（）  时间范围
////                            queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey"))
////                                    .from(DateEnum.DATE_TIME_FULL.getDate()).gte(filtList.get(0))
////                                    .lte(filtList.get(1)));
//
//                            queryBuilder.must(QueryBuilders.rangeQuery(conditionMap.getString("filtKey"))
//                                    .from(filtList.get(0), true).to(filtList.get(1),false));
//                            break;
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

//            System.out.println("result:"+result);
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

        public JSONArray getRDHashMulti(String hash, List<Object> names)
        {
            JSONArray arrayService = new JSONArray();
//
            List<Object> nacosListener = redisTemplate0.opsForHash().multiGet(hash, names);
            for (Object ip : nacosListener) {
                JSONArray arrayIp = JSON.parseArray(ip.toString());
                arrayService.add(arrayIp);
            }

            return arrayService;
        }
        
        public void delRD(String collection, String hash)
        {
            redisTemplate0.delete(collection + ":"+ hash);
        }

        public void delRDHashItem(String collection, String hash, String key)
        {
            redisTemplate0.opsForHash().delete(collection + ":"+ hash, key);
        }

    public <T> T  cloneThis(Object json, Class<T> classType) {
        T object = JSONObject.parseObject(JSON.toJSONString(json), classType);
        return object;
    }

        public JSONObject cloneObj(JSONObject json) {
            String jsonString = json.toJSONString();
            JSONObject jsonObject = JSON.parseObject(jsonString);
            return jsonObject;
        }

        public JSONArray cloneArr(JSONArray json) {
            String jsonString = json.toJSONString();
            JSONArray jsonArray = JSON.parseArray(jsonString);
            return jsonArray;
        }


//    public void updateAsset(Order order, JSONArray arrayLsasset, JSONArray arrayLbasset) {
//
//        HashSet setId_P = new HashSet();
//        JSONArray arrayLsaQuery = new JSONArray();
//        JSONArray arrayLbaQuery = new JSONArray();
//        for (int i = 0; i < arrayLsasset.size(); i++) {
//            JSONObject jsonLsasset = arrayLsasset.getJSONObject(i);
//            String id_C = jsonLsasset.getJSONObject("tokData").getString("id_C");
//            String id_P = jsonLsasset.getString("id_P");
//            setId_P.add(id_P);
//            JSONObject jsonLsaQuery = this.setJson("id_C", id_C,
//                    "id_P", id_P);
//            String locAddr = jsonLsasset.getString("locAddr");
//            if (locAddr != null) {
//                jsonLsaQuery.put("locAddr", locAddr);
//            }
//            arrayLsaQuery.add(jsonLsaQuery);
//        }
//        System.out.println("arrayLsaQuery=" + arrayLsaQuery);
//
//        for (int i = 0; i < arrayLbasset.size(); i++) {
//            JSONObject jsonLbasset = arrayLbasset.getJSONObject(i);
//            String id_C = jsonLbasset.getJSONObject("tokData").getString("id_C");
//            String id_P = jsonLbasset.getString("id_P");
//            setId_P.add(id_P);
//            JSONObject jsonLbaQuery = this.setJson("id_C", id_C,
//                    "id_P", id_P);
//            arrayLbaQuery.add(jsonLbaQuery);
//        }
//        System.out.println("arrayLbaQuery=" + arrayLbaQuery);
//        List<?> prods = this.getMDContentMany(setId_P, "info", Prod.class);
//        JSONObject jsonProds = this.list2Obj(prods, "id");
//
//        HashSet setId_A = new HashSet();
//        JSONObject jsonLsas = this.getId_AById_CId_P(arrayLsaQuery, "lSAsset", setId_A);
//        JSONObject jsonLbas = this.getId_AById_CId_P(arrayLbaQuery, "lBAsset", setId_A);
//
//        List<?> assets = this.getMDContentMany(setId_A, Arrays.asList("info", "aStock"), Asset.class);
//        JSONObject jsonAssets = this.list2Obj(assets, "id");
//
//        List<JSONObject> listBulkAsset = new ArrayList<>();
//        List<JSONObject> listBulkLsasset = new ArrayList<>();
//        List<JSONObject> listBulkLbasset = new ArrayList<>();
//        assetType(order, jsonAssets, jsonLsas, arrayLsasset, jsonProds, listBulkAsset, listBulkLsasset, false, true);
//        assetType(order, jsonAssets, jsonLbas, arrayLbasset, jsonProds, listBulkAsset, listBulkLbasset, false, false);
////        qt.setMDContentMany(listBulkAsset, Asset.class);
////        qt.setESMany("lSAsset", listBulkLsasset);
////        qt.setESMany("lBAsset", listBulkLbasset);
//        this.errPrint("new", null, arrayLsasset, arrayLbasset, listBulkAsset, listBulkLsasset, listBulkLbasset);
//    }
//
//    public void assetType(Order order, JSONObject jsonAssets, JSONObject jsonLsas, JSONArray arrayLsasset,
//                          JSONObject jsonProds, List<JSONObject> listBulkAsset, List<JSONObject> listBulkLsasset,
//                          Boolean isResv, Boolean isLsa) {
//        String id_O = order.getId();
//        JSONArray arrayOItem = order.getOItem().getJSONArray("objItem");
//        for (int i = 0; i < arrayLsasset.size(); i++) {
//            JSONObject jsonLsasset = arrayLsasset.getJSONObject(i);
//            JSONObject tokData = jsonLsasset.getJSONObject("tokData");
//            String id_C = tokData.getString("id_C");
//            String id_CB = tokData.getString("id_CB");
//            String id_U = tokData.getString("id_U");
//            String grpU = tokData.getString("grpU");
//            JSONObject jsonLog = jsonLsasset.getJSONObject("log");
//            Integer index = jsonLsasset.getInteger("index");
//            String id_P = jsonLsasset.getString("id_P");
//            Double wn2qty = jsonLsasset.getDouble("wn2qty");
//            JSONObject jsonBulkAsset = null;
//            JSONObject jsonBulkLsasset = null;
//            String id_A = null;
//            String grpA = "";
//            //index不为空是产品，反之是金钱
//            if (index != null) {
//                JSONObject jsonOItem = arrayOItem.getJSONObject(index);
//                Double wn4price = jsonOItem.getDouble("wn4price");
//                Double wn4value = DoubleUtils.multiply(wn2qty, wn4price);
//                String locAddr = jsonLsasset.getString("locAddr");
//                JSONArray arrayUpdateLocSpace = jsonLsasset.getJSONArray("locSpace");
//                JSONArray arrayUpdateSpaceQty = jsonLsasset.getJSONArray("spaceQty");
//                //存在资产
//                if (jsonLsas.getJSONObject(id_C + "-" + id_P + "-" + locAddr) != null) {
//                    JSONObject jsonLsa = jsonLsas.getJSONObject(id_C + "-" + id_P + "-" + locAddr);
//                    id_A = jsonLsa.getString("id_A");
//                    grpA = jsonLsa.getString("grp");
//                    JSONObject jsonAsset = jsonAssets.getJSONObject(id_A);
//                    JSONObject aStock = jsonAsset.getJSONObject("aStock");
//                    JSONArray arrayLocSpace = aStock.getJSONArray("locSpace");
//                    JSONArray arraySpaceQty = aStock.getJSONArray("spaceQty");
//
//                    //货架的格子
//                    for (int j = 0; j < arrayLocSpace.size(); j++) {
//                        //要移动的格子
//                        for (int k = 0; k < arrayUpdateLocSpace.size(); k++) {
//                            //格子相等
//                            if (arrayLocSpace.getInteger(j) == arrayUpdateLocSpace.getInteger(k)) {
//                                Double spaceQty = arraySpaceQty.getDouble(j);
//                                //移动数量，移入正数，移出负数
//                                Double updateSpaceQty = arrayUpdateSpaceQty.getDouble(k);
//                                Double qty = DoubleUtils.add(spaceQty, updateSpaceQty);
//                                System.out.println("spaceQty=" + spaceQty);
//                                System.out.println("updateSpaceQty=" + updateSpaceQty);
//                                System.out.println("qty=" + qty);
//                                //货架格子小于移动格子
////                                if (DoubleUtils.compareTo(qty, 0) == -1) {
////                                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), null);
////                                }
//                                //大于，减去数量
//                                if (DoubleUtils.compareTo(qty, 0) == 1) {
//                                    arraySpaceQty.set(j, qty);
//                                }
//                                //等于，删除格子数组和数量数组对应的数组元素
//                                else {
//                                    arrayLocSpace.remove(j);
//                                    arraySpaceQty.remove(j);
//                                }
//                            }
//                        }
//                    }
//
//                    if (isResv && aStock.getJSONObject("resvQty") != null &&
//                            aStock.getJSONObject("resvQty").getDouble(id_O + "-" + index) != null) {
//                        ///////************SET - aStock resvAsset qty **************//////////
//                        Double remain = Qt.add(aStock.getDouble("wn2qtyResv"), wn2qty);
//
//                        if (aStock.getDouble("wn2qty") == 0 && remain == 0) {
//                            jsonBulkAsset = this.setJson("type", "delete",
//                                    "id", id_A);
//                            jsonBulkLsasset = this.setJson("type", "delete",
//                                    "id", jsonLsa.getString("id_ES"));
//                        } else {
//                            //check if fromSum == resvQty.wn2qty, if so remove that object, else deduct
//                            JSONObject jsonResvQty = aStock.getJSONObject("resvQty");
//                            if (Qt.compareTo(jsonResvQty.getDouble(id_O + "-" + index), wn2qty) == 0) {
//                                jsonResvQty.remove(id_O + "-" + index);
//                            } else {
//                                jsonResvQty.put(id_O + "-" + index, Qt.add(jsonResvQty.getDouble(id_O + "-" + index), wn2qty));
//                            }
//
//                            AssetAStock assetAStock = new AssetAStock(
//                                    wn4price, locAddr, arrayLocSpace, arraySpaceQty, remain, jsonResvQty);
//                            JSONObject jsonUpdate = this.setJson("aStock", assetAStock);
//                            jsonBulkAsset = this.setJson("type", "update",
//                                    "id", id_A,
//                                    "update", jsonUpdate);
//
//                            this.upJson(jsonLsa, "wn2qty", DoubleUtils.add(aStock.getDouble("wn2qty"), wn2qty),
//                                    "wn4value", DoubleUtils.add(aStock.getDouble("wn4value"), wn4value),
//                                    "locSpace", arrayLocSpace,
//                                    "spaceQty", arraySpaceQty,
//                                    "wn2qtyResv", remain);
//                            jsonBulkLsasset = this.setJson("type", "update",
//                                    "id", jsonLsa.getString("id_ES"),
//                                    "update", jsonLsa);
//                        }
//                    }
//                    else {
//                        if (Qt.compareTo(aStock.getDouble("wn2qty"), wn2qty) == 0) {
//                            jsonBulkAsset = this.setJson("type", "delete",
//                                    "id", id_A);
//                            jsonBulkLsasset = this.setJson("type", "delete",
//                                    "id", jsonLsa.getString("id_ES"));
//                        } else {
//                            AssetAStock assetAStock = new AssetAStock(
//                                    wn4price,
//                                    locAddr, arrayLocSpace, arraySpaceQty);
//                            JSONObject jsonUpdate = this.setJson("aStock", assetAStock);
//                            jsonBulkAsset = this.setJson("type", "update",
//                                    "id", id_A,
//                                    "update", jsonUpdate);
//
//                            this.upJson(jsonLsa, "wn2qty", DoubleUtils.add(aStock.getDouble("wn2qty"), wn2qty),
//                                    "wn4value", DoubleUtils.add(aStock.getDouble("wn4value"), wn4value),
//                                    "locSpace", arrayLocSpace,
//                                    "spaceQty", arraySpaceQty);
//                            jsonBulkLsasset = this.setJson("type", "update",
//                                    "id", jsonLsa.getString("id_ES"),
//                                    "update", jsonLsa);
//                        }
//                    }
//                }
//                //不存在资产，新增资产
//                else {
//                    Asset asset = new Asset();
//                    id_A = this.GetObjectId();
//                    asset.setId(id_A);
//                    AssetInfo assetInfo = new AssetInfo(id_C, id_C, id_P, jsonOItem.getJSONObject("wrdN"),
//                            jsonOItem.getJSONObject("wrddesc"), "1030", jsonOItem.getString("ref"),
//                            jsonOItem.getString("pic"), jsonLsasset.getInteger("lAT"));
//                    asset.setInfo(assetInfo);
//
//                    AssetAStock assetAStock = new AssetAStock(wn4price, locAddr, arrayUpdateLocSpace, arrayUpdateSpaceQty);
//                    asset.setAStock((JSONObject) JSON.toJSON(assetAStock));
//                    JSONArray view = this.setArray("info", "aStock");
//                    asset.setView(view);
//                    jsonBulkAsset = this.setJson("type", "insert",
//                            "insert", asset);
//
////                    lSAsset lsasset = new lSAsset(id_A, id_C, id_C, id_P, jsonOItem.getJSONObject("wrdN"),
////                            jsonOItem.getJSONObject("wrddesc"), "1030", jsonOItem.getString("ref"),
////                            jsonOItem.getString("pic"), 2, wn2qty, wn4price, wn4value);
////                    lsasset.setLocAddr(locAddr);
////                    lsasset.setLocSpace(arrayUpdateLocSpace);
////                    lsasset.setSpaceQty(arrayUpdateSpaceQty);
//
//                    if (isLsa) {
//                        lSAsset lsasset = new lSAsset(id_A, id_C, id_C, id_P, jsonOItem.getJSONObject("wrdN"),
//                                jsonOItem.getJSONObject("wrddesc"), "1030", jsonOItem.getString("ref"),
//                                jsonOItem.getString("pic"), jsonLsasset.getInteger("lAT"), wn2qty, wn4price);
//                        lsasset.setLocAddr(locAddr);
//                        lsasset.setLocSpace(arrayUpdateLocSpace);
//                        lsasset.setSpaceQty(arrayUpdateSpaceQty);
//
//                        jsonBulkLsasset = this.setJson("type", "insert",
//                                "insert", lsasset);
//                    } else {
//                        lBAsset lbasset = new lBAsset(id_A, id_C, id_C, id_CB, id_P, jsonOItem.getJSONObject("wrdN"),
//                                jsonOItem.getJSONObject("wrddesc"), "1030", jsonOItem.getString("ref"),
//                                jsonOItem.getString("pic"), jsonLsasset.getInteger("lAT"), wn2qty, wn4price);
//                        lbasset.setLocAddr(locAddr);
//                        lbasset.setLocSpace(arrayUpdateLocSpace);
//                        lbasset.setSpaceQty(arrayUpdateSpaceQty);
//
//                        jsonBulkLsasset = this.setJson("type", "insert",
//                                "insert", lbasset);
//                    }
//
//                }
//                listBulkAsset.add(jsonBulkAsset);
//                listBulkLsasset.add(jsonBulkLsasset);
//
//                LogFlow log = new LogFlow(tokData, jsonOItem, order.getAction(),
//                        order.getInfo().getId_CB(), id_O, index, "assetflow", "stoChg",
//                        jsonOItem.getJSONObject("wrdN").getString("cn") + jsonLog.getString("zcndesc"),
//                        jsonLog.getInteger("imp"));
//                log.setLogData_assetflow(wn2qty, wn4price, id_A, grpA);
//                System.out.println("assetflow=" + log);
////                ws.sendWS(log);
//            }
//            else {
//                JSONObject prodInfo = jsonProds.getJSONObject(id_P).getJSONObject("info");
//                //存在金钱
//                if (jsonLsas.getJSONObject(id_C + "-" + id_P) != null) {
//                    JSONObject jsonLsa = jsonLsas.getJSONObject(id_C + "-" + id_P);
//                    id_A = jsonLsa.getString("id_A");
//                    JSONObject jsonAsset = jsonAssets.getJSONObject(id_A);
//                    JSONObject assetInfo = jsonAsset.getJSONObject("info");
//                    JSONObject aStock = jsonAsset.getJSONObject("aStock");
//                    AssetAStock assetAStock = new AssetAStock( Qt.add(aStock.getDouble("wn4price"), wn2qty),
//                            "", new JSONArray(), new JSONArray());
//                    JSONObject jsonUpdate = this.setJson("aStock", assetAStock);
//                    jsonBulkAsset = this.setJson("type", "update",
//                            "id", id_A,
//                            "update", jsonUpdate);
//
//                    this.upJson(jsonLsa, "wn4price", Qt.add(aStock.getDouble("wn4price"), wn2qty),
//                            "wn4value", Qt.add(aStock.getDouble("wn4value"), wn2qty));
//                    jsonBulkLsasset = this.setJson("type", "update",
//                            "id", jsonLsa.getString("id_ES"),
//                            "update", jsonLsa);
//                }
//                //不存在金钱，新增
//                else {
//                    Asset asset = new Asset();
//                    id_A = this.GetObjectId();
//                    asset.setId(id_A);
////                    JSONObject prodInfo = jsonProds.getJSONObject(id_P).getJSONObject("info");
//                    AssetInfo assetInfo = new AssetInfo(id_C, id_C, id_P, prodInfo.getJSONObject("wrdN"),
//                            prodInfo.getJSONObject("wrddesc"), "1030", prodInfo.getString("ref"),
//                            prodInfo.getString("pic"), jsonLsasset.getInteger("lAT"));
//                    asset.setInfo(assetInfo);
//                    AssetAStock assetAStock = new AssetAStock(wn2qty, "", new JSONArray(), new JSONArray());
//                    asset.setAStock((JSONObject) JSON.toJSON(assetAStock));
//                    JSONArray view = this.setArray("info", "aStock");
//                    asset.setView(view);
//                    jsonBulkAsset = this.setJson("type", "insert",
//                            "insert", asset);
//
//                    if (isLsa) {
//                        lSAsset lsasset = new lSAsset(id_A, id_C, id_C, id_P, prodInfo.getJSONObject("wrdN"),
//                                prodInfo.getJSONObject("wrddesc"), "1030", prodInfo.getString("ref"),
//                                prodInfo.getString("pic"), jsonLsasset.getInteger("lAT"), 1.0, wn2qty);
//
//                        jsonBulkLsasset = this.setJson("type", "insert",
//                                "insert", lsasset);
//                    } else {
//                        lBAsset lbasset = new lBAsset(id_A, id_C, id_C, id_CB, id_P, prodInfo.getJSONObject("wrdN"),
//                                prodInfo.getJSONObject("wrddesc"), "1030", prodInfo.getString("ref"),
//                                prodInfo.getString("pic"), jsonLsasset.getInteger("lAT"), 1.0, wn2qty);
//
//                        jsonBulkLsasset = this.setJson("type", "insert",
//                                "insert", lbasset);
//                    }
//
//                }
//
//                listBulkAsset.add(jsonBulkAsset);
//                listBulkLsasset.add(jsonBulkLsasset);
//
//                LogFlow log = new LogFlow("moneyflow", jsonLog.getString("id"), jsonLog.getString("id_FS"),
//                        "stoChg", id_U, grpU, id_P, jsonLog.getString("grpB"), jsonLog.getString("grp"),
//                        jsonLog.getString("id_OP"), id_O, jsonLog.getInteger("index"), id_C,
//                        jsonLog.getString("id_CS"), prodInfo.getString("pic"), tokData.getString("dep"),
//                        prodInfo.getJSONObject("wrdN").getString("cn") + jsonLog.getString("zcndesc"),
//                        jsonLog.getInteger("imp"), prodInfo.getJSONObject("wrdN"), tokData.getJSONObject("wrdNU"));
//                log.setLogData_money(id_A, "", wn2qty);
//                System.out.println("moneyflow=" + log);
////                ws.sendWS(log);
//            }
//        }
//    }

//    public JSONObject getId_AById_CId_P(JSONArray arrayQuery, String logType, HashSet setId_A) {
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder shouldQuery = new BoolQueryBuilder();
//        for (int i = 0; i < arrayQuery.size(); i++) {
//            JSONObject jsonQuery = arrayQuery.getJSONObject(i);
//            BoolQueryBuilder mustQuery = new BoolQueryBuilder();
//            mustQuery.must(QueryBuilders.termQuery("id_C", jsonQuery.getString("id_C")))
//                    .must(QueryBuilders.termQuery("id_P", jsonQuery.getString("id_P")));
//            if (jsonQuery.getString("locAddr") != null) {
//                mustQuery.must(QueryBuilders.termQuery("locAddr", jsonQuery.getString("locAddr")));
//            }
//            shouldQuery.should(mustQuery);
//        }
//        System.out.println("shouldQuery=" + shouldQuery);
//        sourceBuilder.query(shouldQuery).size(1000);
//        try {
//            SearchRequest request = new SearchRequest(logType).source(sourceBuilder);
//            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//            JSONArray arrayEs = this.hit2Array(response);
//            System.out.println("arrayEs=" + arrayEs);
//            JSONObject jsonResult = new JSONObject();
//            for (int i = 0; i < arrayEs.size(); i++) {
//                JSONObject jsonEs = arrayEs.getJSONObject(i);
//                String locAddr = jsonEs.getString("locAddr");
//                if (locAddr == null || locAddr.equals("")) {
//                    jsonResult.put(jsonEs.getString("id_C") + "-" + jsonEs.getString("id_P"), jsonEs);
//                } else {
//                    jsonResult.put(jsonEs.getString("id_C") + "-" + jsonEs.getString("id_P") + "-" + locAddr, jsonEs);
//                }
//                setId_A.add(jsonEs.getString("id_A"));
//            }
//            System.out.println("jsonResult=" + jsonResult);
//            return jsonResult;
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
//        }
//    }



}
