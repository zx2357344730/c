package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.common.Constants;
import com.cresign.tools.pojo.es.lBProd;
import com.cresign.tools.pojo.es.lSBComp;
import com.cresign.tools.pojo.es.lSBOrder;
import com.cresign.tools.pojo.es.lSProd;
import com.cresign.tools.pojo.po.*;
import com.mongodb.client.result.DeleteResult;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Random;

@Service
public class CoupaUtil {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private RestHighLevelClient client;

    @Autowired
    private DbUtils dbUtils;

    public Init getInit(){
        Query query = new Query(new Criteria("_id").is("cn_java"));
        Field fields = query.fields();
        fields.include("logInit");
        return mongoTemplate.findOne(query, Init.class);
    }

    public void updateInitLog(JSONObject logInit){
        // 创建查询条件，并且添加查询条件
        Query query = new Query(new Criteria("_id").is("cn_java"));

        // 创建修改对象
        Update update = new Update();

        // 循环添加修改的键和值
        update.set("logInit",logInit);

        // 调用数据库进行修改
        mongoTemplate.updateFirst(query,update, Init.class);
    }


    /////////////////////////////COMP//////////////////////////////////////////

    /**
     * 根据cId获取listKey需要的信息
     *
     * ##Params: cId     公司id
     * ##Params: listKey 需要的数据集合
     * ##return: com.cresign.tools.pojo.po.Comp  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 14:50
     */
    public Comp getCompByListKey(String cId, List<String> listKey) {
        Query query = new Query(new Criteria("_id").is(cId));
        Field fields = query.fields();
        listKey.forEach(fields::include);
        return mongoTemplate.findOne(query, Comp.class);
    }

    /////////////////////////////ASSET//////////////////////////////////////////

    public void updateAssetByKeyAndListKeyVal(String key, Object val, JSONObject keyVal) {
        // 创建查询条件，并且添加查询条件
        Query query = new Query(new Criteria(key).is(val));

        // 创建修改对象
        Update update = new Update();

        // 循环添加修改的键和值
        keyVal.keySet().forEach(k -> update.set(k,keyVal.get(k)));

        // 调用数据库进行修改
        mongoTemplate.updateFirst(query,update, Asset.class);
    }

    /**
     * 根据id_C和ref获取Asset的Id
     * ##Params: id_C	公司id
     * ##Params: ref	模块名称
     * ##return: java.lang.String  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/25 15:20
     */
    public String getAssetId(String id_C, String ref) {

        System.out.println("what?"+id_C+ref);

        Query getAsset = new Query(new Criteria("info.id_C").is(id_C).and("info.ref").is(ref));

//        String id_A = dbUtils.getId_A(id_C, ref);
//        System.out.println(id_A+"......");
//        Query getAsset = new Query(new Criteria("_id").is(id_A));
        getAsset.fields().include("_id");
        Asset one = mongoTemplate.findOne(getAsset, Asset.class);
        System.out.print("one"+one);
        if (null != one) {
            return one.getId();
        } else {
            return null;
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

//    /**
//     * 设置Asset的Auth数据
//     * ##Params: aId	AssetId
//     * ##Params: view	设置的值
//     * ##return: int  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/6 9:30
//     */
//    @Override
//    public int setAssetAuth(String aId, String view) {
//        // 创建查询条件
//        Query query = new Query(new Criteria("_id").is(aId));
//        Asset one = mongoTemplate.findOne(query, Asset.class);
//        if (null != one) {
//            JSONObject menu = one.getMenu();
//            if (null != menu.get("objMenu")) {
//                JSONArray objMenu = menu.getJSONArray("objMenu");
//                int i = 0;
//                for (int j = 0; j < objMenu.size(); j++) {
//                    JSONObject map = objMenu.getJSONObject(j);
//                    if (null != map.get("bmdlistType") && map.get("bmdlistType").equals("lSAsset")) {
//                        if (null != map.get("lrefcard")) {
//                            // 创建修改条件
//                            Update update = new Update();
//                            // 添加修改条件
//                            update.addToSet("menu.objMenu."+i+".lrefcard",view);
//                            mongoTemplate.updateFirst(query,update,Asset.class);
//                            return 1;
//                        }
//                        return 0;
//                    }
//                    i++;
//                }
//            }
//        }
//        return 0;
//    }

    /**
     * 删除Asset的BuyTemp数据
     * ##Params: aId	AssetID
     * ##Params: id_O	订单id
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 9:31
     */
    public void delAssetBuyTemp(String aId, String id_O) {
//        if (null == aId || null == id_O) {
//            return;
//        }
//        // 创建查询条件
//        Query query = new Query(new Criteria("_id").is(aId));
//        Asset one = mongoTemplate.findOne(query, Asset.class);
//        if (null != one) {
//            JSONObject modList = one.getModList();
//            Object data = modList.get("data");
//            if (null != data) {
//                JSONArray dataList = modList.getJSONArray("data");
//                int i = 0;
//                int is = 0;
//                for (int j = 0; j < dataList.size(); j++) {
//                    JSONObject map = dataList.getJSONObject(j);
//                    if (map.get("id_O").equals(id_O)){
//                        dataList.remove(i);
//                        is = 1;
//                        break;
//                    }
//                    i++;
//                }
//                if (is == 1) {
//                    modList.put("data",dataList);
//                    // 创建修改条件
//                    Update update = new Update();
//                    // 添加修改条件
//                    update.set("modList",modList);
//                    mongoTemplate.updateFirst(query,update,Asset.class);
//                }
//            }
//        }
    }

    /**
     * 删除Asset的Auth数据
     * ##Params: aId	AssetId
     * ##Params: view	删除的值
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 9:31
     */
    public void delAssetAuth(String aId, String view) {
        if (null == aId || null == view) {
            return;
        }
        // 创建查询条件
        Query query = new Query(new Criteria("_id").is(aId));
        Asset one = mongoTemplate.findOne(query, Asset.class);
        if (null != one) {
            JSONObject menu = one.getMenu();
            if (null != menu.get("objMenu")) {
                JSONArray objMenu = menu.getJSONArray("objMenu");
                int i = 0;
                for (int j = 0; j < objMenu.size(); j++) {
                    JSONObject map = objMenu.getJSONObject(j);
                    if (null != map.get("bmdlistType") && map.get("bmdlistType").equals("lSAsset")) {
                        if (null != map.get("lrefcard")) {
                            // 创建修改条件
                            Update update = new Update();
                            // 添加修改条件
                            update.pull("menu.objMenu."+i+".lrefcard",view);
                            mongoTemplate.updateFirst(query,update,Asset.class);
                        }
                    }
                    i++;
                }
            }
        }
    }

    /**
     * 获取Asset里面flowControl的grp对应的聊天室id
     * ##Params: cId   公司id
     * ##Params: ref   模块名称
     * ##Params: grp   grp
     * ##return:  聊天室id
     */
    public String getAssetByGrp(String cId, String ref, String grp) {

        String id_A = dbUtils.getId_A(cId, ref);
        Query getAsset = new Query(new Criteria("_id").is(id_A));
        getAsset.fields().include("_id").include("flowControl");
        Asset one = mongoTemplate.findOne(getAsset, Asset.class);
        if (null != one) {
            if (null != one.getFlowControl()&&null!=one.getFlowControl().getJSONObject("objProdActionGrpB")){
                JSONObject objProdActionGrpB = one.getFlowControl().getJSONObject("objProdActionGrpB");
                return objProdActionGrpB.getString(grp);
            }
        }
        return null;
    }

    /**
     * 根据cId获取客服聊天室id
     * ##Params: cId	公司id
     * ##return: java.lang.String  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2021/1/13 9:18
     */
    public JSONObject getAssetByKf(String cId,String oId) {
        String id_A = dbUtils.getId_A(cId, "a-auth");
        Query getAsset = new Query(new Criteria("_id").is(id_A));
        getAsset.fields().include("flowControl");
        Asset one = mongoTemplate.findOne(getAsset, Asset.class);
        if (null != one) {
            JSONObject flowControl = one.getFlowControl();
            JSONArray objData = flowControl.getJSONArray("objData");
            if (null == oId) {
                JSONArray result = new JSONArray();
                for (int i = 0; i < objData.size(); i++) {
                    JSONObject obj = objData.getJSONObject(i);
                    if (obj.getString(Constants.GET_TYPE).equals("cusmsg")){
                        JSONObject re = new JSONObject();
                        re.put(Constants.ADD_ID,obj.getString(Constants.GET_ID));
                        result.add(re);
                    }
                }
                Random rand = new Random();
                int r = rand.nextInt(result.size());

                return result.getJSONObject(r);
            } else {
                for (int i = 0; i < objData.size(); i++) {
                    JSONObject obj = objData.getJSONObject(i);
                    if (obj.getString(Constants.GET_TYPE).equals("cusmsg")&&obj.getString(Constants.GET_ID).equals(oId)){
                        JSONObject re = new JSONObject();
                        re.put(Constants.ADD_ID,obj.getString(Constants.GET_ID));
                        return re;
                    }
                }
            }
        }
        return null;
    }

    /////////////////////////////USER//////////////////////////////////////////

    /**
     * 根据idU获取用户的指定info信息
     *
     * ##Params: idU 用户id
     * ##return: java.util.Map<java.lang.String, java.lang.Object>  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:11
     */
    public User getUserByKeyAndVal(String key,String val, List<String> listKey) {
        // 创建查询条件
        Query query = new Query(new Criteria(key).is(val));
        // 添加排序条件
        Field fields = query.fields();
        listKey.forEach(fields::include);
        // 返回结果
        return mongoTemplate.findOne(query, User.class);
    }

    /**
     * 根据idU获取用户的指定info信息
     *
     * ##Params: idU 用户id
     * ##return: java.util.Map<java.lang.String, java.lang.Object>  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:11
     */
    public JSONObject getUserInfo(String idU) {
        System.out.println("进入这里:");

        // 创建查询条件
        Query query = new Query();

        // 添加查询条件
        query.addCriteria(new Criteria("id").is(idU));

        // 添加排序条件
        query.fields().include("info");

        // 根据查询条件获取用户信息
        User one = mongoTemplate.findOne(query, User.class);

//        System.out.println(JSON.toJSONString(one));

        // 判断用户信息不为空
        if (null != one) {

            // 返回结果
            return (JSONObject) JSON.toJSON(one.getInfo());
        }

        // 返回结果
        return null;
    }

    /**
     * 根据idU获取用户的指定info信息
     *
     * ##Params: idU 用户id
     * ##return: java.util.Map<java.lang.String, java.lang.Object>  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:11
     */
    public User getUserById(String idU, List<String> listKey) {
        // 创建查询条件
        Query query = new Query(new Criteria("id").is(idU));
        // 添加排序条件
        Field fields = query.fields();
        listKey.forEach(fields::include);
        // 返回结果
        return mongoTemplate.findOne(query, User.class);
    }

//    /**
//     * 根据uId获取listKey需要的信息
//     *
//     * ##Params: uId     用户id
//     * ##Params: listKey 需要的数据集合
//     * ##return: com.cresign.tools.pojo.po.User  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/6 15:11
//     */
//    @Override
//    public User getUserByListKey(String uId, List<String> listKey) {
//        Query query = new Query(new Criteria("_id").is(uId));
//        Field fields = query.fields();
//        listKey.forEach(fields::include);
//        return mongoTemplate.findOne(query, User.class);
//    }

//    /**
//     * 根据compID获取用户信息
//     *
//     * ##Params: compID 公司id
//     * ##return: java.util.List<com.cresign.tools.pojo.po.User>  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/6 15:11
//     */
//    @Override
//    public List<User> getUserByCompID(String compID) {
//        //创建查询对象
//        Query query = new Query();
//
//        //添加查询条件
//        query.addCriteria(new Criteria("info.id_C").is(compID));
//
//        //执行查询
//        return mongoTemplate.find(query, User.class);
//    }


    /**
     * 根据key和val修改keyVal的对应数据
     * ##Params: key	查询的键
     * ##Params: val	查询的值
     * ##Params: keyVal	需要修改的键和值
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/9/7 14:48
     */
    public void updateUserByKeyAndListKeyVal(String key, Object val, JSONObject keyVal) {
        // 创建查询条件，并且添加查询条件
        Query query = new Query(new Criteria(key).is(val));

        // 创建修改对象
        Update update = new Update();

        // 循环添加修改的键和值
        keyVal.keySet().forEach(k -> update.set(k, keyVal.get(k)));

        // 调用数据库进行修改
        mongoTemplate.updateFirst(query, update, User.class);
    }

    /////////////////////////////PROD//////////////////////////////////////////

    public JSONArray getEsQuery(String index,List<String> key,List<String> val) {
        SearchRequest request = new SearchRequest(index);

        JSONArray result = new JSONArray();

        // 构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(1000);
        QueryBuilder queryBuilder;
//        System.out.println(JSON.toJSONString(key));
//        System.out.println(JSON.toJSONString(val));

        BoolQueryBuilder bq = QueryBuilders.boolQuery();
        for (int i = 0; i < key.size(); i++) {
            String k = key.get(i);
            String v = val.get(i);
            bq.must(QueryBuilders.termQuery(k, v));
        }
        queryBuilder = bq;

        builder.query(queryBuilder);

        request.source(builder);
        try {
            SearchResponse search = client.search(request, RequestOptions.DEFAULT);
//            System.out.println(JSON.toJSONString(search));
            for (SearchHit hit : search.getHits().getHits()) {
                System.out.println(JSON.toJSONString(hit));
                JSONObject re = new JSONObject();
                re.put("esId",hit.getId());
                re.put("map",hit.getSourceAsMap());
                result.add(re);
            }
//            System.out.println("result"+result);
            return result;
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer delEsById(String index,String id){
        // 2、定义请求对象
        DeleteRequest request = new DeleteRequest(index);
        request.id(id);
        // 3、发送请求到ES
        DeleteResponse response;
        try {
            response = client.delete(request, RequestOptions.DEFAULT);
            // 4、处理响应结果
            System.out.println("删除是否成功:" + response.getResult());
            return 0;
        } catch (IOException e) {
            return 1;
        }
    }

    /**
     *
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2020/9/8 8:54
     */
    public void updateES_lBProd(lBProd lbprod){
        // 异常捕获
        try {

            // 获取指定es索引
            IndexRequest requestI = new IndexRequest("lbprod");

            // 将我们的数据放入请求 json
            requestI.source(JSON.toJSONString(lbprod), XContentType.JSON);
            // 写入完成立即刷新
            requestI.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            // 插入数据
            client.index(requestI, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2020/9/8 8:54
     */
    public void updateES_lSProd(lSProd lsprod){
        // 异常捕获
        try {

            // 获取指定es索引
            IndexRequest requestI = new IndexRequest("lsprod");

            // 将我们的数据放入请求 json
            requestI.source(JSON.toJSONString(lsprod), XContentType.JSON);
            // 写入完成立即刷新
            requestI.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            // 插入数据
            client.index(requestI, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据pId获取listKey需要的信息
     *
     * ##Params: pId     产品id
     * ##Params: listKey 需要的数据集合
     * ##return: com.cresign.tools.pojo.po.Prod  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:09
     */
    public Prod getProdByListKey(String id_P, List<String> listKey) {
        Query query = new Query(new Criteria("_id").is(id_P));
        Field fields = query.fields();
        listKey.forEach(fields::include);
        return mongoTemplate.findOne(query, Prod.class);
    }

    /**
     * 根据oId删除信息
     * ##Params: oId	订单id
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 14:56
     */
    public void removeOrderById(String oId) {

        // 创建查询，并且添加查询条件
        Query query = new Query(new Criteria("_id").is(oId));

        // 根据查询删除信息
        mongoTemplate.remove(query,Order.class);
    }

//    /**
//     * 根据pId获取listKey需要的信息
//     *
//     * ##Params: pId     产品id
//     * ##Params: listKey 需要的数据集合
//     * ##return: com.cresign.tools.pojo.po.ProdDg  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/6 15:09
//     */
//    public ProdDg getProdDgByListKey(String id_P, List<String> listKey) {
//        Query query = new Query(new Criteria("_id").is(id_P));
//        Field fields = query.fields();
//        listKey.forEach(fields::include);
//        return mongoTemplate.findOne(query, ProdDg.class);
//    }

    /**
     * 根据keyVal修改pId的对应数据
     *
     * ##Params: pId    产品id
     * ##Params: keyVal 需要修改的键和值
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:09
     */
    public void updateProdByListKeyVal(String pId, JSONObject keyVal) {
        // 查询条件
        Query query = new Query(new Criteria("id").is(pId));
        Update update = new Update();
        keyVal.keySet().forEach(k -> update.set(k, keyVal.get(k)));
        mongoTemplate.updateFirst(query, update, Prod.class);
    }


//    ///////////////////// ES get list ////////////////////
//    public JSONArray getListData(String listType, String Key1, String Val1, String Key2, String Val2) {
//        SearchRequest request = new SearchRequest(listType);
//
//        JSONArray result = new JSONArray();
//
//        // 构建搜索条件
//        SearchSourceBuilder builder = new SearchSourceBuilder();
//        builder.size(100);
//        QueryBuilder queryBuilder;
//        System.out.println("input"+Key1+"x"+Val1+"x"+Key2+"x"+Val2+"x");
//
////        QueryBuilder queryBuilder1 = QueryBuilders.termQuery(Key1, Val1);
//
//
//        if (!Key2.equals("") && Key2 != null) {
////            QueryBuilder queryBuilder2 = QueryBuilders.termQuery(Key2, Val2);
//            queryBuilder = QueryBuilders.boolQuery()
//                    .must(QueryBuilders.termQuery(Key1, Val1))
//                    .must(QueryBuilders.termQuery(Key2, Val2));
//        } else {
//            queryBuilder = QueryBuilders.boolQuery()
//                    .must(QueryBuilders.termQuery(Key1, Val1));
//        }
//
//        builder.query(queryBuilder);
//
//        request.source(builder);
//
//        try {
//            SearchResponse search = client.search(request, RequestOptions.DEFAULT);
//            for (SearchHit hit : search.getHits().getHits()) {
//                    result.add(hit.getSourceAsMap());
//                }
//
//            System.out.println("result"+result);
//            return result;
//        } catch (
//                IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 根据key和val修改keyVal的对应数据
     * ##Params: key	查询的键
     * ##Params: val	查询的值
     * ##Params: keyVal	需要修改的键和值
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/9/7 14:48
     */
    public void updateOrderByKeyAndListKeyVal(String key, Object val, JSONObject keyVal) {
        // 创建查询条件，并且添加查询条件
        Query query = new Query(new Criteria(key).is(val));

        // 创建修改对象
        Update update = new Update();

        // 循环添加修改的键和值
        keyVal.keySet().forEach(k -> update.set(k,keyVal.get(k)));

        // 调用数据库进行修改
        mongoTemplate.updateFirst(query,update,Order.class);
    }

    /**
     * 根据keyVal修改oId的对应数据
     * ##Params: oId	订单id
     * ##Params: keyVal	需要修改的键和值
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 14:55
     */
    public void updateOrderByListKeyVal(String oId, JSONObject keyVal) {
//        ("进入修改订单方法...");
//        ("修改值:");
//        (JSON.toJSONString(keyVal));
//        ("oId:"+oId);
        // 创建查询条件，并且添加查询条件
        Query query = new Query(new Criteria("id").is(oId));

        // 创建修改对象
        Update update = new Update();

        // 循环添加修改的键和值
        keyVal.keySet().forEach(k -> update.set(k,keyVal.get(k)));

        // 调用数据库进行修改
        mongoTemplate.updateFirst(query,update,Order.class);
    }


    /**
     * 新增lsborder日志到Es - 注释完成
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2020/9/8 8:54
     */
    public void updateES_lSBOrder(lSBOrder lsborder){
        // 异常捕获
        try {

            // 获取指定es索引
            IndexRequest requestI = new IndexRequest("lsborder");

            // 将我们的数据放入请求 json
            requestI.source(com.alibaba.fastjson.JSON.toJSONString(lsborder), XContentType.JSON);
            // 写入完成立即刷新
            requestI.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            // 插入数据
            client.index(requestI, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 新增lsbcomp日志到Es - 注释完成
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2020/9/8 8:54
     */
    public void updateES_lSBComp(lSBComp lsbcomp){
        // 异常捕获
        try {

            // 获取指定es索引
            IndexRequest requestI = new IndexRequest("lsbcomp");

            // 将我们的数据放入请求 json
            requestI.source(com.alibaba.fastjson.JSON.toJSONString(lsbcomp), XContentType.JSON);
            // 写入完成立即刷新
            requestI.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            // 插入数据
            client.index(requestI, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据key和val获取listKey需要的信息
     * ##Params: key	查询的键
     * ##Params: val	查询的值
     * ##Params: listKey	需要的数据集合
     * ##return: com.cresign.tools.pojo.po.Order  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/9/7 14:19
     */
    public Order getOrderByKeyAndListKey(String key,Object val, List<String> listKey) {
        // 创建查询对象，并且添加查询条件
        Query query = new Query(new Criteria(key).is(val));

        // 过滤对象存储
        Field fields = query.fields();

        // 添加过滤条件
        listKey.forEach(fields::include);

        // 返回查询结果
        return mongoTemplate.findOne(query, Order.class);
    }

    /**
     * 根据oId获取listKey需要的信息
     * ##Params: oId	订单id
     * ##Params: listKey	需要的数据集合
     * ##return: com.cresign.tools.pojo.po.Order  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 14:54
     */
    public Order getOrderByListKey(String oId, List<String> listKey) {
        // 创建查询对象，并且添加查询条件
        Query query = new Query(new Criteria("_id").is(oId));

        // 过滤对象存储
        Field fields = query.fields();

        // 添加过滤条件
        listKey.forEach(fields::include);

        // 返回查询结果
        return mongoTemplate.findOne(query, Order.class);
    }
    /**
     * 根据ref获取产品的信息
     *
     * ##Params: ref 产品编号
     * ##return: com.cresign.tools.pojo.po.Prod  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:09
     */
    public Prod getProdByRef(String ref) {
        // 创建查询条件
        Query query = new Query(new Criteria("info.ref").is(ref));

        // 创建查询排除
        Field fields = query.fields();

        // 添加需要的值
        fields.include("spec").include("view");

        // 返回需要的结果
        return mongoTemplate.findOne(query, Prod.class);
    }

    /**
     * 根据ref更新产品的信息
     *
     * ##Params: ref  产品编号
     * ##Params: spec 修改参数
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:09
     */
//    public void setProdByRefAndSpec(String ref, JSONObject spec) {
//        // 查询条件
//        Query query = new Query(new Criteria("info.ref").is(ref));
//
//        // 创建修改条件
//        Update update = new Update();
//
//        // 添加修改条件
//        update.set("spec", spec);
//
//        // 修改数据
//        mongoTemplate.updateFirst(query, update, Prod.class);
//    }

    public void saveOrder(Order order) {
        // 新增order信息
        mongoTemplate.save(order);
    }

    /**
     * 根据oId删除信息
     * ##Params: oId	订单id
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 14:56
     */
    public void delOrder(String id) {
        // 创建查询，并且添加查询条件
        Query query = new Query(new Criteria("_id").is(id));
        // 根据查询删除信息
        mongoTemplate.remove(query,Order.class);
    }
    public void delProd(String id) {
        // 创建查询，并且添加查询条件
        Query query = new Query(new Criteria("_id").is(id));
        // 根据查询删除信息
        mongoTemplate.remove(query,Prod.class);
    }
    public void delAsset(String id) {
        // 创建查询，并且添加查询条件
        Query query = new Query(new Criteria("_id").is(id));
        // 根据查询删除信息
        mongoTemplate.remove(query,Asset.class);
        DeleteResult result = mongoTemplate.remove(query,Asset.class);
//        if (result.getDeletedCount() == 0) {
//            throw new ErrorResponseException(HttpStatus.OK, ToolEnum.ASSET_NOT_FOUND, "");
//        }
    }
    public void delComp(String id) {
        // 创建查询，并且添加查询条件
        Query query = new Query(new Criteria("_id").is(id));
        // 根据查询删除信息
        mongoTemplate.remove(query,Comp.class);
    }

}
