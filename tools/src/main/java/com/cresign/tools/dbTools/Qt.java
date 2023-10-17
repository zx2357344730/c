package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.ToolEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.*;
import com.mongodb.bulk.BulkWriteResult;
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
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kevin
 * @ClassName Qt
 * @Description
 * @updated 2022/9/11 10:05 AM
 * @ver 1.0.0
 **/
@Service
public class Qt {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private QtAsNew qtAsNew;

    public JSONObject initData = new JSONObject();

    public static final String appId = "KVB0qQq0fRArupojoL4WM9";

    //MDB - done: get, getMany, del, new, inc, set, push, pull
    //MDB - need: ops, opsExec
    //ESS - done: get, getFilt, del, add, set
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
        }
        T result;
        try {
            result = mongoTemplate.findOne(query, classType);
        } catch (Exception e){
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK,ToolEnum.DB_ERROR.getCode(), e.toString());
        }
        return result;
    }

    public <T> T  getMDContentAP(String id,  JSONObject AP, List<String>  fields, Class<T> classType) {

        Query query = new Query(new Criteria("_id").is(id)
                .and(AP.getString("key")).is(AP.getString("val")));

        if (fields != null) {

            fields.forEach(query.fields()::include);
        }
        T result;
        try {
            result = mongoTemplate.findOne(query, classType);
        } catch (Exception e){
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK,ToolEnum.DB_ERROR.getCode(), e.toString());
        }
        return result;
    }

//    public <T> T  getMDContentByKv(String key,String val,  List<String>  fields, Class<T> classType) {
//
//        Query query = new Query(new Criteria(key).is(val));
//        if (fields != null) {
//            fields.forEach(query.fields()::include);
//        }
//        T result;
//        try {
//            result = mongoTemplate.findOne(query, classType);
//        } catch (Exception e){
//            throw new ErrorResponseException(HttpStatus.OK,ToolEnum.DB_ERROR.getCode(), e.toString());
//        }
//        return result;
//    }
//    public <T> T  getMDContentByKv(String key,String val, String field, Class<T> classType) {
//
//        Query query = new Query(new Criteria(key).is(val));
//        if (field != null && !field.equals("")) {
//            query.fields().include(field);
//        }
//        T result;
//        try {
//            result = mongoTemplate.findOne(query, classType);
//        } catch (Exception e){
//            throw new ErrorResponseException(HttpStatus.OK,ToolEnum.DB_ERROR.getCode(), e.toString());
//        }
//        return result;
//    }

    public <T> T  getMDContent(String id,  String field, Class<T> classType) {

        Query query = new Query(new Criteria("_id").is(id));
        if (field != null && !field.equals("")) {
            query.fields().include(field);
        }
        T result;
        try {
            result = mongoTemplate.findOne(query, classType);
        } catch (Exception e){
            e.printStackTrace();
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
        return this.jsonTo(initData.get("cn_java"), InitJava.class);
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
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
        }
    }

    public <T> List<T> getMDContentMany2(Collection<?> queryIds, List<String> fields, Class<T> classType) {
        Query query = new Query(new Criteria("_id").in(queryIds));
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
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.DB_ERROR.getCode(), e.toString());
        }
    }

    public <T> List<T> getMDContentFast(JSONArray id_Ps,List<String> strList, Class<T> classType){
        int forProd = id_Ps.size() / 6;
        List<T> list = new ArrayList<>();
        JSONArray item5 = new JSONArray();
        JSONArray item6 = new JSONArray();
        if (forProd > 5) {
            JSONArray item1 = new JSONArray();
            JSONArray item2 = new JSONArray();
            JSONArray item3 = new JSONArray();
            JSONArray item4 = new JSONArray();
            int lei = 0;
            for (int i = 0; i < forProd; i++) {
                item1.add(id_Ps.getString(lei));
                item2.add(id_Ps.getString(lei+1));
                item3.add(id_Ps.getString(lei+2));
                item4.add(id_Ps.getString(lei+3));
                item5.add(id_Ps.getString(lei+4));
                item6.add(id_Ps.getString(lei+5));
                lei+=6;
            }
            int jie = id_Ps.size()-(forProd*6);
            if (jie > 0) {
                if (jie == 1) {
                    item1.add(id_Ps.getString((forProd*6)));
                } else if (jie == 2) {
                    item1.add(id_Ps.getString((forProd*6)));
                    item2.add(id_Ps.getString((forProd*6)+1));
                } else if (jie == 3) {
                    item1.add(id_Ps.getString((forProd*6)));
                    item2.add(id_Ps.getString((forProd*6)+1));
                    item3.add(id_Ps.getString((forProd*6)+2));
                } else if (jie == 4) {
                    item1.add(id_Ps.getString((forProd*6)));
                    item2.add(id_Ps.getString((forProd*6)+1));
                    item3.add(id_Ps.getString((forProd*6)+2));
                    item4.add(id_Ps.getString((forProd*6)+3));
                } else {
                    item1.add(id_Ps.getString((forProd*6)));
                    item2.add(id_Ps.getString((forProd*6)+1));
                    item3.add(id_Ps.getString((forProd*6)+2));
                    item4.add(id_Ps.getString((forProd*6)+3));
                    item5.add(id_Ps.getString((forProd*6)+4));
                }
            }
            Future<String> future1 = qtAsNew.testMdMany(item1, list,strList,classType);
            Future<String> future2 = qtAsNew.testMdMany(item2, list,strList,classType);
            Future<String> future3 = qtAsNew.testMdMany(item3, list,strList,classType);
            Future<String> future4 = qtAsNew.testMdMany(item4, list,strList,classType);
            Future<String> future5 = qtAsNew.testMdMany(item5, list,strList,classType);
            System.out.println("?");
            System.out.println("start_thread-id:"+qtAsNew.getThreadId());
            qtAsNew.mdManyUtilQuery(item6, list,strList,classType);
            while (true) {
                if (future1.isDone() && future2.isDone() &&
                        future3.isDone() && future4.isDone() && future5.isDone()
                ) {
                    break;
                }
            }
            System.out.println("大小:");
            System.out.println(list.size());
        } else {
            if (id_Ps.size() / 2 > 7) {
                boolean isAdd = true;
                for (int i = 0; i < id_Ps.size(); i++) {
                    if (isAdd) {
                        isAdd = false;
                        item5.add(id_Ps.getJSONObject(i));
                    } else {
                        isAdd = true;
                        item6.add(id_Ps.getJSONObject(i));
                    }
                }
                Future<String> future5 = qtAsNew.testMdMany(item5, list,strList,classType);
                qtAsNew.mdManyUtilQuery(item6, list,strList,classType);
                while (true) {
                    if (future5.isDone()) {
                        break;
                    }
                }
                System.out.println("大小:");
                System.out.println(list.size());
            } else {
                qtAsNew.mdManyUtilQuery(id_Ps, list,strList,classType);
            }
        }
        return list;
    }

    /**
     * 拆分JSONArray数组方法
     * @param splitNum  拆分数量
     * @param array 需要拆分的JSONArray数组
     * @param isReversalOne 是否调换数组0和数组最后一个
     * @param classType 拆分后集合类型
     * @return  拆分结果
     * @param <T>   集合类型
     */
    public <T> List<List<T>> getSubList(int splitNum,JSONArray array,boolean isReversalOne, Class<T> classType){
        // 记录累加数
        int count = 0;
        // 拆分结果存储
        List<List<T>> lists = new ArrayList<>();
        // 获取拆分后集合长度
        int splitLength = (array.size() / splitNum)+1;
        // 遍历拆分数量
        for (int i = 0; i < splitNum; i++) {
            // 定义存储当前循环拆分结果
            List<Object> objects;
            // 判断为循环的最后一次拆分
            if (i == splitNum - 1) {
                // 直接拆分累加数到最后长度
                objects = array.subList(count, array.size());
            // 否则为循环的平均拆分
            } else {
                // 拆分累加数到拆分后集合长度
                objects = array.subList(count, count+splitLength);
                // 数量累加到下次循环使用
                count += splitLength;
            }
            // 创建集合存储需要的返回类型集合
            List<T> strings = new ArrayList<>();
            // 遍历拆分结果
            for (Object object : objects) {
                // 将拆分结果转换成需要的返回类型，并且添加到集合
                strings.add(JSON.parseObject(JSON.toJSONString(object),classType));
            }
            // 将需要的返回类型集合添加到返回最终结果集合
            lists.add(strings);
        }
        // 判断需要调换数组第0位集合和最后一位集合
        if (isReversalOne) {
            lists.add(0,lists.get(lists.size() - 1));
            lists.remove(lists.size()-1);
        }
        // 返回结果
        return lists;
    }
    /**
     * 拆分List数组方法
     * @param splitNum  拆分数量
     * @param list 需要拆分的List数组
     * @param isReversalOne 是否调换数组0和数组最后一个
     * @return  拆分结果
     * @param <T>   集合类型
     */
    public <T> List<List<T>> getSubList(int splitNum,List<T> list,boolean isReversalOne){
        // 记录累加数
        int count = 0;
        // 拆分结果存储
        List<List<T>> lists = new ArrayList<>();
        // 获取拆分后集合长度
        int splitLength = (list.size() / splitNum)+1;
        // 遍历拆分数量
        for (int i = 0; i < splitNum; i++) {
            // 定义存储当前循环拆分结果
            List<T> objects;
            // 判断为循环的最后一次拆分
            if (i == splitNum - 1) {
                // 直接拆分累加数到最后长度
                objects = list.subList(count, list.size());
            // 否则为循环的平均拆分
            } else {
                // 拆分累加数到拆分后集合长度
                objects = list.subList(count, count+splitLength);
                // 数量累加到下次循环使用
                count += splitLength;
            }
            // 将拆分结果添加到最终结果集合
            lists.add(objects);
        }
        // 判断需要调换数组第0位集合和最后一位集合
        if (isReversalOne) {
            lists.add(0,lists.get(lists.size() - 1));
            lists.remove(lists.size()-1);
        }
        return lists;
    }

    /**
     * 根据JSONArray集合id列表查询多个对象方法
     * @param queryIds  需要查询的id，JSONArray集合
     * @param strList   查询后需要的字段
     * @param classType 查询的对象信息
     * @return  查询后对象信息集合
     * @param <T>   查询的对象信息
     */
    public <T> List<T> getMDContentFast2(JSONArray queryIds,List<String> strList, Class<T> classType){
        // 获取查询id集合长度
        int queryIdSize = queryIds.size();
        // 创建存储返回结果集合
        List<T> list = new ArrayList<>();
        // 判断id集合长度小于等于10
        if (queryIdSize <= 10) {
            // 直接主线程查询
            qtAsNew.mdManyUtilQuery(queryIds, list,strList,classType);
            // 返回结果
            return list;
        }
        // 定义存储id集合需要拆分的数量
        int splitNum;
        // 判断id集合长度小于等于20
        if (queryIdSize <= 20) {
            // 拆分两次，也就是分配两个线程
            splitNum = 2;
        // 判断id集合长度小于等于30
        } else if (queryIdSize <= 30) {
            // 拆分三次，也就是分配三个线程
            splitNum = 3;
        // 判断id集合长度小于等于40
        } else if (queryIdSize <= 40) {
            // 拆分四次，也就是分配四个线程
            splitNum = 4;
        // 判断id集合长度小于等于50
        } else if (queryIdSize <= 50) {
            // 拆分五次，也就是分配五个线程
            splitNum = 5;
        // 否则id集合长度大于50
        } else {
            // 拆分六次，也就是分配六个线程
            splitNum = 6;
        }
        // 调用拆分JSONArray数组方法，并且获取拆分结果
        List<List<String>> subList = getSubList(splitNum, queryIds, true, String.class);
        // 输出主线程编号
//        System.out.println("start_thread-id:"+qtAsNew.getThreadId());
        errPrint("start_thread-id:",null,qtAsNew.getThreadId());
        // 开启一线程
        Future<String> future1 = qtAsNew.testMdMany(subList.get(1), list,strList,classType);
        // 判断拆分数量为2
        if (splitNum == 2) {
            // 获取线程处理结果
            getReturn(splitNum,future1,null,null,null,null,list,subList.get(0),strList,classType);
            // 返回结果集合
            return list;
        }
        // 开启二线程
        Future<String> future2 = qtAsNew.testMdMany(subList.get(2), list,strList,classType);
        // 判断拆分数量为3
        if (splitNum == 3) {
            // 获取线程处理结果
            getReturn(splitNum,future1,future2,null,null,null,list,subList.get(0),strList,classType);
            return list;
        }
        // 开启三线程
        Future<String> future3 = qtAsNew.testMdMany(subList.get(3), list,strList,classType);
        // 判断拆分数量为4
        if (splitNum == 4) {
            // 获取线程处理结果
            getReturn(splitNum,future1,future2,future3,null,null,list,subList.get(0),strList,classType);
            return list;
        }
        // 开启四线程
        Future<String> future4 = qtAsNew.testMdMany(subList.get(4), list,strList,classType);
        // 判断拆分数量为5
        if (splitNum == 5) {
            // 获取线程处理结果
            getReturn(splitNum,future1,future2,future3,future4,null,list,subList.get(0),strList,classType);
            return list;
        }
        // 开启五线程
        Future<String> future5 = qtAsNew.testMdMany(subList.get(5), list,strList,classType);
        // 获取线程处理结果
        getReturn(splitNum,future1,future2,future3,future4,future5,list,subList.get(0),strList,classType);
        return list;
    }
    /**
     * 根据List集合id列表查询多个对象方法
     * @param queryIds  需要查询的id，List集合
     * @param strList   查询后需要的字段
     * @param classType 查询的对象信息
     * @return  查询后对象信息集合
     * @param <T>   查询的对象信息
     */
    public <T> List<T> getMDContentFast2(List<String> queryIds,List<String> strList, Class<T> classType){
        // 获取查询id集合长度
        int queryIdSize = queryIds.size();
        // 创建存储返回结果集合
        List<T> list = new ArrayList<>();
        // 判断id集合长度小于等于10
        if (queryIdSize <= 10) {
            // 直接主线程查询
            qtAsNew.mdManyUtilQuery(queryIds, list,strList,classType);
            // 返回结果
            return list;
        }
        // 定义存储id集合需要拆分的数量
        int splitNum;
        // 判断id集合长度小于等于20
        if (queryIdSize <= 20) {
            // 拆分2次，也就是分配2个线程
            splitNum = 2;
        // 判断id集合长度小于等于30
        } else if (queryIdSize <= 30) {
            // 拆分3次，也就是分配3个线程
            splitNum = 3;
        // 判断id集合长度小于等于40
        } else if (queryIdSize <= 40) {
            // 拆分4次，也就是分配4个线程
            splitNum = 4;
        // 判断id集合长度小于等于50
        } else if (queryIdSize <= 50) {
            // 拆分5次，也就是分配5个线程
            splitNum = 5;
        // 否则id集合长度大于50
        } else {
            // 拆分6次，也就是分配6个线程
            splitNum = 6;
        }
        // 调用拆分JSONArray数组方法，并且获取拆分结果
        List<List<String>> subList = getSubList(splitNum,queryIds,true);
//        System.out.println("start_thread-id:"+qtAsNew.getThreadId());
        errPrint("start_thread-id:",null,qtAsNew.getThreadId());
        // 开启1线程
        Future<String> future1 = qtAsNew.testMdMany(subList.get(1), list,strList,classType);
        // 判断拆分数量为2
        if (splitNum == 2) {
            // 获取线程处理结果
            getReturn(splitNum,future1,null,null,null,null,list,subList.get(0),strList,classType);
            // 返回结果集合
            return list;
        }
        // 开启2线程
        Future<String> future2 = qtAsNew.testMdMany(subList.get(2), list,strList,classType);
        // 判断拆分数量为3
        if (splitNum == 3) {
            // 获取线程处理结果
            getReturn(splitNum,future1,future2,null,null,null,list,subList.get(0),strList,classType);
            return list;
        }
        // 开启3线程
        Future<String> future3 = qtAsNew.testMdMany(subList.get(3), list,strList,classType);
        // 判断拆分数量为4
        if (splitNum == 4) {
            // 获取线程处理结果
            getReturn(splitNum,future1,future2,future3,null,null,list,subList.get(0),strList,classType);
            return list;
        }
        // 开启4线程
        Future<String> future4 = qtAsNew.testMdMany(subList.get(4), list,strList,classType);
        // 判断拆分数量为5
        if (splitNum == 5) {
            // 获取线程处理结果
            getReturn(splitNum,future1,future2,future3,future4,null,list,subList.get(0),strList,classType);
            return list;
        }
        // 开启5线程
        Future<String> future5 = qtAsNew.testMdMany(subList.get(5), list,strList,classType);
        // 获取线程处理结果
        getReturn(splitNum,future1,future2,future3,future4,future5,list,subList.get(0),strList,classType);
        return list;
    }

    /**
     * 获取多线程结果方法
     * @param splitNum  拆分数量
     * @param future1   线程1结果
     * @param future2   线程2结果
     * @param future3   线程3结果
     * @param future4   线程4结果
     * @param future5   线程5结果
     * @param list  查询结果集合
     * @param subListSon    查询的id集合
     * @param strList   查询后需要的字段
     * @param classType 需要的类型
     * @param <T>   需要的类型
     */
    public <T> void getReturn(int splitNum,Future<String> future1
            ,Future<String> future2,Future<String> future3
            ,Future<String> future4,Future<String> future5,List<T> list
            ,List<String> subListSon,List<String> strList, Class<T> classType){
//        System.out.println("?");
        errPrint("?",null,null);
        // 最后主线程查询
        qtAsNew.mdManyUtilQuery(subListSon, list,strList,classType);
//        System.out.println("- ! -");
        errPrint("- ! -",null,null);
        // 死循环获取线程结果
        while (true) {
            // 判断拆分数量为2，并且线程1完成
            if (splitNum == 2 && future1.isDone()) {
                // 结束死循环
                break;
            // 判断拆分数量为3，并且线程1，线程2完成
            } else if (splitNum == 3 && future1.isDone() && future2.isDone()) {
                // 结束死循环
                break;
            // 判断拆分数量为4，并且线程1，线程2，线程3完成
            } else if (splitNum == 4 && future1.isDone() && future2.isDone() && future3.isDone()) {
                // 结束死循环
                break;
            // 判断拆分数量为5，并且线程1，线程2，线程3，线程4完成
            } else if (splitNum == 5 && future1.isDone() && future2.isDone()
                    && future3.isDone() && future4.isDone()) {
                // 结束死循环
                break;
            // 否则拆分数量为6，
            } else {
                // 判断线程1，线程2，线程3，线程4，线程5完成
                if (future1.isDone() && future2.isDone() && future5.isDone()
                        && future3.isDone() && future4.isDone()) {
                    // 结束死循环
                    break;
                }
            }
        }
//        System.out.println("大小:");
//        System.out.println(list.size());
        errPrint("结果集合大小:",null,list.size());
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
            e.printStackTrace();
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
            if (item == null)
            {
                System.out.println("....null");
            }
            else if (item.getClass().toString().startsWith("class java.util.Array"))
            {
                System.out.println(this.toJArray(item));
            }
            else if (item.getClass().toString().startsWith("class com.cresign.tools.pojo") ||
                    item.getClass().toString().startsWith("class java.util"))
            {
                System.out.println(this.toJson(item));
            }
            else {
                System.out.println(item);
            }
        }
        System.out.println("*****[End]*****");

        if (e != null)
            e.printStackTrace();
    }

    public void errPrint(String title, Object... vars)
    {
        this.errPrint(title, null, vars);
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
        update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        update.inc("tvs", 1);
        try {
            mongoTemplate.updateFirst(query, update, classType);
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
        }

    }

    public void incMDContent(String id, String updateKey, Number updateValue, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        update.inc(updateKey, updateValue);
        update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        update.inc("tvs", 1);
        try {
            mongoTemplate.updateFirst(query, update, classType);
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
        }
    }


    public void pushMDContent(String id, JSONObject jsonUpdate, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        jsonUpdate.forEach(update::push);
        update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        if (updateResult.getModifiedCount() == 0) {

            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), "");
        }
    }

    public void pushMDContent(String id, String updateKey, Object updateValue, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        update.push(updateKey, updateValue);
        update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        if (updateResult.getModifiedCount() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), "");
        }
    }


    //Pull multiple Key Values from a single id
    public void pullMDContent(String id, JSONObject jsonUpdate, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        jsonUpdate.forEach(update::pull);
        update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        if (updateResult.getModifiedCount() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), "");
        }
    }

    public void pullMDContent(String id, String updateKey, Object updateValue, Class<?> classType) {
        Query query = new Query(new Criteria("_id").is(id));
        Update update = new Update();
        update.pull(updateKey, updateValue);
        update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        update.inc("tvs", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
        if (updateResult.getModifiedCount() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), "");
        }
    }


    public void setMDContentAP(String id,  JSONObject AP, JSONObject keyVal, Class<?> classType) {
        try {
        Query query = new Query(new Criteria("_id").is(id)
                .and(AP.getString("key")).is(AP.getString("val")));

            Update update = new Update();
            for (String key : keyVal.keySet())
            {
                Object val = keyVal.get(key);
                if (val == null) {
                    update.unset(key);
                } else {
                    if (key.equals("info"))
                    {
                        JSONObject valJson = this.toJson(val);
                        valJson.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                    }
                    update.set(key, val);
                }
            }
            if (!keyVal.keySet().contains("info")) {
                update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            }
            update.inc("tvs", 1);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
            System.out.println("inSetMD" + updateResult);
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
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
                Object val = keyVal.get(key);
                if (val == null) {
                    update.unset(key);
                } else {
                    if (key.equals("info"))
                    {
                        JSONObject valJson = this.toJson(val);
                        valJson.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                    }
                    update.set(key, val);
                }
            }
            if (!keyVal.keySet().contains("info")) {
                update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            }
            update.inc("tvs", 1);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
            System.out.println("inSetMD" + updateResult);
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
        }
    }
    public void setMDContentMany(HashSet setId, JSONObject keyVal, Class<?> classType) {
        try {
            Query query = new Query(new Criteria("_id").in(setId));
            Update update = new Update();

//                keyVal.keySet().forEach(k -> update.set(k, keyVal.get(k)));
            for (String key : keyVal.keySet())
            {
                Object val = keyVal.get(key);
                if (val == null) {
                    update.unset(key);
                } else {
                    update.set(key, val);
                }
            }
            update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            update.inc("tvs", 1);
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, classType);
            System.out.println("inSetMD" + updateResult);
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
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
                    update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
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
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
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
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
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
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
        }
    }

    public void addMD( Object obj) {
        // 新增order信息
        try {
            mongoTemplate.insert(obj);
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
        }

    }

    public void addAllMD(Collection<?> collection) {
        // 新增order信息
        try {
            mongoTemplate.insertAll(collection);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
        }
    }

    public void saveMD( Object obj) {
        // 新增order信息
        try {
            mongoTemplate.save(obj);
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.SAVE_DB_ERROR.getCode(), e.toString());
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

    public String getId_U(String phone)
    {
        JSONArray result = this.getES("lNUser", this.setESFilt("mbn","exact",phone));

        if (result.size() == 1) {
            return result.getJSONObject(0).getString("id_U");
        } else
            return "";

    }


    public Asset getConfig(String id_C, String ref, List <String> listField) {

        if (id_C != null && !id_C.equals("")) {
            if (this.hasRDHashItem("login:module_id", "compId-" + id_C, ref)) {
                String id_A = this.getRDHashStr("login:module_id", "compId-" + id_C, ref);
                System.out.println("id_A from redis" + id_A);
                return this.getMDContent(id_A, listField, Asset.class);
            } else {
                JSONArray result = this.getES("lSAsset", this.setESFilt("id_C", id_C, "ref", ref, "grp", "1003"), 1);

                if (result.size() == 1) {
                    String id_A = result.getJSONObject(0).getString("id_A");
                    System.out.println("getConfig ES" + id_A);

                    Asset asset = this.getMDContent(id_A, listField, Asset.class);
                    this.putRDHash("login:module_id", "compId-" + id_C, ref, asset.getId());

                    return asset;
                }
            }
        }

        Asset nothing = new Asset();
        nothing.setId("none");

        return nothing;

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
                case "lBInfo":
                case "lSInfo":
                    contentMap.put("id", contentMap.get("id_I"));
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
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ES_DB_ERROR.getCode(), e.toString());
        }
    }

    public JSONArray getES(String index, JSONArray filterArray) {
        return this.getES(index, filterArray, 1, 10000, "tmd", "desc");
    }

    public JSONArray getES(String index, JSONArray filterArray, Integer size)  {
        return this.getES(index, filterArray, 1, size, "tmd", "desc");
    }

    public JSONArray getES(String index, JSONArray filterArray, Integer page, Integer size, String sortKey, String sortOrder, JSONArray fetchCol) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();

        this.filterBuilder(filterArray, queryBuilder);
        sourceBuilder.query(queryBuilder).from((page - 1) * size).size(size);
        if (sortOrder.equals("desc"))
            sourceBuilder.sort(sortKey, SortOrder.DESC);
        else
            sourceBuilder.sort(sortKey, SortOrder.ASC);
        try {

            this.errPrint("getES", null, index, page, size, sortKey, sortOrder, filterArray);
            SearchRequest request = new SearchRequest();

            String[] indices = index.split("/");
            request.indices(indices);
            request.source(sourceBuilder);
            String[] array = fetchCol.toArray(new String[fetchCol.size()]);

            //参数一  指定键，参数二  过滤键
            sourceBuilder.fetchSource(array,null);

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            System.out.println(response);

            return this.hit2Array(response);

        } catch (IOException e) {
            e.printStackTrace();
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ES_DB_ERROR.getCode(), e.toString());
        }
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
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ES_DB_ERROR.getCode(), e.toString());
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
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ES_DB_ERROR.getCode(), e.toString());
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
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ES_DB_ERROR.getCode(), e.toString());
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
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ES_DB_ERROR.getCode(), e.toString());
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
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ES_DB_ERROR.getCode(), e.toString());
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
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ES_DB_ERROR.getCode(), e.toString());
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
            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ES_DB_ERROR.getCode(), e.toString());
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
        System.out.println("id_C=" + id_C);
        System.out.println("asset=" + asset);
        if (!asset.getId().equals("none")) {
            if (asset.getPowerup() == null) {
                JSONObject power = this.getInitData().getNewComp().getJSONObject("a-core").getJSONObject("powerup");
                JSONArray newView = asset.getView();
                newView.add("powerup");
                this.setMDContent(asset.getId(), this.setJson("powerup", power, "view", newView), Asset.class);
                asset.setPowerup(power);
            }
            JSONObject jsonCapacity = asset.getPowerup().getJSONObject("capacity");
            //超额
            if (fileSize > 0 && fileSize + jsonCapacity.getLong("used") > jsonCapacity.getLong("total")) {
                throw new ErrorResponseException(HttpStatus.OK, ToolEnum.POWER_NOT_ENOUGH.getCode(), "");
            }
            JSONObject jsonUpdate = setJson("powerup.capacity.used", fileSize);
            incMDContent(asset.getId(), jsonUpdate, Asset.class);
        }

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
            JSONObject jsonWrd = new JSONObject();
            for (int i = 0; i < filterArray.size(); i++) {
                JSONObject filter = filterArray.getJSONObject(i);
                String key = filter.getString("filtKey");
                String val = filter.getString("filtVal");
                if ("eq".equals(filter.getString("method")) && key.startsWith("wrd")) {
                    if (jsonWrd.getJSONArray(key) != null) {
                        jsonWrd.getJSONArray(key).add(val);
                    } else {
                        jsonWrd.put(key, this.setArray(val));
                    }
                    filterArray.remove(i);
                    i--;
                }
            }
            jsonWrd.forEach((k, v) ->{
                JSONArray arrayWrd = jsonWrd.getJSONArray(k);
                if (arrayWrd.size() > 1) {
                    this.setESFilt(filterArray, k, "contain", arrayWrd);
                } else {
                    this.setESFilt(filterArray, k, "eq", arrayWrd.getString(0));
                }
            });

            int i = 0;
            while (i < filterArray.size()) {
                //拿到每一组筛选条件

                JSONObject conditionMap =  filterArray.getJSONObject(i);
                String method = conditionMap.getString("method");

                // 每一条都要对 wrdxxx 处理 增加 语言.cn处理
                boolean isArray = conditionMap.get("filtKey") instanceof ArrayList;
                if (isArray) {
                    for (int fk = 0; fk < conditionMap.getJSONArray("filtKey").size(); fk++)
                    {
                        String filtKey = conditionMap.getJSONArray("filtKey").getString(fk);
                        if (filtKey.startsWith("wrd") && !filtKey.endsWith(".cn"))
                        {
                            conditionMap.getJSONArray("filtKey").set(fk, filtKey + ".cn");
                        }
                    }
                } else {
                    String filtKey = conditionMap.getString("filtKey");

                    if (filtKey.startsWith("wrd") && !filtKey.endsWith(".cn"))
                    {
                        conditionMap.put("filtKey", filtKey + ".cn");
                    }
                    if (filtKey.startsWith("ref"))
                    {
                        conditionMap.put("method", "prefix");
                    }

                }

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
                    case "prefix":
                        //前缀查询
                        queryBuilder.must(QueryBuilders.prefixQuery(conditionMap.getString("filtKey"), conditionMap.getString("filtVal")));
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
                        JSONArray arrayValue = conditionMap.getJSONArray("filtVal");
                        for (int j = 0; j < arrayValue.size(); j++) {
                            JSONObject jsonValue = arrayValue.getJSONObject(j);
                            BoolQueryBuilder mustQuery = new BoolQueryBuilder();
                            jsonValue.forEach((k, v) ->{
                                mustQuery.must(QueryBuilders.termQuery(k, v));
                            });
                            queryBuilder.should(mustQuery);
                        }
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

    public void incRD(String collection, String keyName,String key,int count)
    {
        redisTemplate0.opsForHash().increment(collection + ":" + keyName, key, count);

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
        return redisTemplate0.opsForValue().get(collection + ":" + key);
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
    public void delRDByCollection(Collection<String> keys)
    {
        redisTemplate0.delete(keys);
    }

    public void delRDHashItem(String collection, String hash, String key)
    {
        redisTemplate0.opsForHash().delete(collection + ":"+ hash, key);
    }

    public <T> T  cloneThis(Object json, Class<T> classType) {
        T object = JSONObject.parseObject(JSON.toJSONString(json, SerializerFeature.DisableCircularReferenceDetect), classType);
        return object;
    }

    public JSONObject cloneObj(Object json) {
        //json.toJSONString();
        String jsonString = JSON.toJSONString(json, SerializerFeature.DisableCircularReferenceDetect);
        JSONObject jsonObject = JSON.parseObject(jsonString);
        return jsonObject;
    }

    public JSONArray cloneArr(JSONArray json) {
        String jsonString = JSON.toJSONString(json, SerializerFeature.DisableCircularReferenceDetect);
        JSONArray jsonArray = JSON.parseArray(jsonString);
        return jsonArray;
    }

    public boolean isNotNull(Object jsonDb, JSONArray arrayField) {
        // 1. "info.wrdN.cn"
        // 2. "order.oItem.objItem[4].lST"
        // 2. XXXXX "order.oItem.objItem[].lST"
        // 3. ["info.abc", "order.oItem.objItem"]
        
        JSONArray arrayResult = new JSONArray();
        for (int i = 0; i < arrayField.size(); i++) {
            String field = arrayField.getString(i);
            String[] splitField = field.split("\\.");
            JSONObject jsonClone = this.cloneObj(jsonDb);
            StringBuffer key = new StringBuffer();
            for (int j = 0; j < splitField.length; j++) {
                String split = splitField[j];
                String[] isArray = split.split("\\[");
                System.out.println("jsonClone=" + jsonClone);
                if (j == splitField.length - 1) {
                    if (jsonClone.get(split) == null) {
                        arrayResult.add(key);
                        break;
                    } else {
                        System.out.println("result=" + jsonClone.get(split));
                    }
                } else {
                    if (isArray.length == 2) {
                        System.out.println("array");
                        Integer index = Integer.valueOf(isArray[1].substring(0, isArray[1].length() - 1));
                        System.out.println("index=" + index);
                        if (jsonClone.getJSONArray(isArray[0]) == null) {
                            key.append(isArray[0]);
                            arrayResult.add(key);
                            break;
                        } else if (jsonClone.getJSONArray(isArray[0]).getJSONObject(index) == null) {
                            key.append(split);
                            arrayResult.add(key);
                            break;
                        } else {
                            key.append(split);
                            jsonClone = jsonClone.getJSONArray(isArray[0]).getJSONObject(index);
                        }
                    } else {
                        System.out.println("json");
                        if (jsonClone.getJSONObject(split) == null) {
                            key.append(split);
                            arrayResult.add(key);
                            break;
                        } else {
                            key.append(split);
                            jsonClone = jsonClone.getJSONObject(split);
                        }

                    }
                }

            }
        }
        if (arrayResult.size() > 0) {
            return false;
//            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.VALUE_IS_NULL.getCode(), arrayResult.toJSONString());
        } else {
            return true;
        }
    }
}
