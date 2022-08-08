package com.cresign.details.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.cresign.details.enumeration.DetailsEnum;
import com.cresign.details.service.GroupService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.mongo.ObjectIdJsonSerializer;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.User;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
public class GroupServicelmpl implements GroupService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private StringRedisTemplate redisTemplate1;

    @Autowired
    private CoupaUtil coupaUtil;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private RetResult retResult;
    
    @Autowired
    private AuthCheck authCheck;

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse addFC(String id_U, String id_C, JSONObject requestJson) throws IOException {

        String id_A = dbUtils.getId_A(id_C, "a-auth");
        Query authQ = new Query(new Criteria("_id").is(id_A)
                .and("flowControl.objData.id").is(requestJson.getString("id")));

            authQ.fields().include("flowControl.objData.$");

            //默认有flowControl这张卡片
            Asset asset = mongoTemplate.findOne(authQ, Asset.class);

            //查询此编号，如果有，那证明重复了
            if (asset != null){

                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_NOT_FOUND.getCode(), null);
            }

            //redis添加群
            JSONArray objUser = this.setFlowControlUserList(id_C, requestJson.getString("dep"), requestJson.getJSONArray("grpU"),requestJson.getJSONArray("id_UM"));
            requestJson.put("objUser",objUser);

            //push添加  pull 删除
            Query query = new Query(new Criteria("_id").is(id_A));

            mongoTemplate.updateFirst(query, new Update().push("flowControl.objData", requestJson), Asset.class);

            return retResult.ok(CodeEnum.OK.getCode(),requestJson);
    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse deleteGroup(String id_U, String id_C, String id) {
//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        //flowControl在lSAsset
//        reqJson.put("listType", "lSAsset");
//        reqJson.put("grp", "1003");
//        reqJson.put("authType", "card");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("flowControl"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);

        authCheck.getUserUpdateAuth(id_U,id_C,"lSAsset","1003","card",new JSONArray().fluentAdd("flowControl"));

//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        //权限判断
//        if ("200".equals(authModuleJson.getString("code"))) {

            Document document = new Document();
            //指定对象中的某个键的值，相匹配
            document.put("id",id);
            //删除redis键
            redisTemplate1.delete("userPush_"+id_C+"_"+ id);
            //push添加  pull 删除
        String id_A = dbUtils.getId_A(id_C, "a-auth");
        Query query = new Query(new Criteria("_id").is(id_A).and("flowControl.objData.id").is(id));
            mongoTemplate.updateFirst(query, new Update().pull("flowControl.objData", document), Asset.class);


            return retResult.ok(CodeEnum.OK.getCode(),null);
//        }
//
//
//        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }


    @Override
    @Transactional(noRollbackFor = ResponseException.class,rollbackFor = Error.class)
    public ApiResponse updateGroup(String id_A, String id_U, String id_C, JSONObject requestJson) throws IOException {

            Query authQ = new Query(
                    new Criteria("_id").is(id_A)
                            .and("flowControl.objData.id").is(requestJson.getString("id")));
            authQ.fields().include("flowControl.objData.$");
            authQ.fields().include("def.objlBP");

            //默认有flowControl这张卡片
            Asset asset = mongoTemplate.findOne(authQ, Asset.class);

            if (asset == null) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_NOT_FOUND.getCode(), null);
            }

            JSONObject map = asset.getFlowControl().getJSONArray("objData").getJSONObject(0);
            JSONObject lBProd = asset.getDef().getJSONObject("objlBP");

            if (map.getString("id").equals(requestJson.getString("id"))) {

//                if (map.getJSONArray("id_UM") == null)
//                {
//                    map.put("id_UM", new JSONArray());
//                }

                //1.判断前端数组是否和数据库数组不一致
//                if (!requestJson.getJSONArray("grpU").containsAll(map.getJSONArray("grpU")) ||
//                        !requestJson.getJSONArray("id_UM").containsAll(map.getJSONArray("id_UM"))) {

                    //取出差集  以数据库传来为主，前端为次。这样就拿到要提出群的组别
                    map.getJSONArray("grpU").removeAll(requestJson.getJSONArray("grpU"));

                    //拿到要踢出群的组别
                    if (map.getJSONArray("grpU").size() > 0){
                        this.removeGrpUFromFC(id_C,map.getJSONArray("grpU"),map.getJSONArray("objUser"));
                    }
                        //添加群
                JSONArray userArray = this.setFlowControlUserList(id_C, requestJson.getString("dep"), requestJson.getJSONArray("grpU"), requestJson.getJSONArray("id_UM"));

                System.out.println("userArray"+ userArray);
               map.putAll(requestJson);
               map.put("objUser", userArray);

            }
            System.out.println("flowCon"+map);

            Update update = new Update();
            update.set("flowControl.objData.$", map);
            update.inc("tvs", 1);
            mongoTemplate.updateFirst(authQ, update, Asset.class);

            this.setFlowControlActionGrpB(requestJson.getJSONArray("grpB"),requestJson.getString("id_O"), lBProd);

            return retResult.ok(CodeEnum.OK.getCode(), map);

    }

    private void setFlowControlActionGrpB(JSONArray grpB, String id_O, JSONObject defObject)
    {

        JSONObject result = new JSONObject();
        for (int j = 0; j < grpB.size(); j++) {

            result.put(grpB.getString(j),defObject.getJSONObject(grpB.getString(j)));

//            for (int k = 0; k < grpB.size(); k++) {
//                if (defArray.getJSONObject(k).getString("ref").equals(grpB.getString(j)))
//                {
//                    result.put(grpB.getString(j), defArray.getJSONObject(k));
//                }
//            }
        }
        try {
            Query orderFC = new Query(
                    new Criteria("_id").is(id_O));
            orderFC.fields().include("action.grpBGroup");
            Update update = new Update();
            update.set("action.grpBGroup", result);
            mongoTemplate.updateFirst(orderFC, update, Order.class);
        } catch (RuntimeException e) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
        }
    }
    private JSONArray setFlowControlUserList(String id_C, String dep, JSONArray grpU, JSONArray id_UM) throws IOException {

        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lbuser");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        JSONArray result = new JSONArray();

        //根据职位和公司id查出所有人
        for (Object o : grpU) {

            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    //条件1：当前公司id
                    .must(QueryBuilders.termQuery("id_CB", id_C))
                    //条件2：grpU
                    .must(QueryBuilders.termQuery("grpU", o))
                    .must(QueryBuilders.matchPhraseQuery("dep", dep));
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(10000);
            //把构建对象放入，指定查那个对象，把查询条件放进去
            searchRequest.source(searchSourceBuilder);
            //执行请求
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : search.getHits().getHits()) {
                System.out.println("now is" + o + " " + dep + " " + hit.getSourceAsMap().get("id_U"));
//                if (hit.getSourceAsMap().get("dep") != null && hit.getSourceAsMap().get("dep").equals(dep)) {
                JSONObject objUser = new JSONObject();
                objUser.put("imp", 3);
                objUser.put("id_U", hit.getSourceAsMap().get("id_U"));
                //三元运算符
                objUser.put("id_APP", hit.getSourceAsMap().getOrDefault("id_APP", ""));
                result.add(objUser);
//                }
            }
        }

        //Add manager into objUser
        for (int k = 0; k < id_UM.size(); k++) {
            boolean inResult = false;
            for (int j=0; j< result.size(); j++)
            {
                //Check not repeated assigned in objUser by grpU 检查不会和职位重复。
                if (result.getJSONObject(j).getString("id_U").equals(id_UM.get(k)))
                {
                    inResult = true;
                }
            }
            if (!inResult) {
                QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                        //条件1：当前公司id
                        .must(QueryBuilders.termQuery("id_CB", id_C))
                        //条件2：grpU
                        .must(QueryBuilders.termQuery("id_U", id_UM.get(k)));
                searchSourceBuilder.query(queryBuilder);
                searchSourceBuilder.from(0);
                searchSourceBuilder.size(1);
                searchRequest.source(searchSourceBuilder);
                //执行请求
                SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                for (SearchHit hit : search.getHits().getHits()) {
                    JSONObject objUser = new JSONObject();
                    objUser.put("imp", 3);
                    objUser.put("id_U", hit.getSourceAsMap().get("id_U"));
                    objUser.put("id_APP", hit.getSourceAsMap().getOrDefault("id_APP", ""));
                    result.add(objUser);
                }
            }
        }

        System.out.println("result"+result);

        return result;

    }

    //批量删除群
    private void removeGrpUFromFC(String id_C, JSONArray grpU,JSONArray objUser ) throws IOException {

        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lbuser");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //根据职位和公司id查出所有人
        for (Object o : grpU) {

            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    //条件1：当前公司id
                    .must(QueryBuilders.termQuery("id_CB", id_C))
                    //条件2：grpU
                    .must(QueryBuilders.termQuery("grpU", o));
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(10000);
            //把构建对象放入，指定查那个对象，把查询条件放进去
            searchRequest.source(searchSourceBuilder);
            //执行请求
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : search.getHits().getHits()) {

                for (int i = 0; i < objUser.size(); i++) {

                    if (objUser.getJSONObject(i).containsValue(hit.getSourceAsMap().get("id_U"))) {
                        objUser.remove(i);
                        i--;
                    }
                }
            }
        }

    }

    @Override
    public ApiResponse getFlowControl(String id_C, String id_U,List<String> type) {


        JSONObject objRolex = MongoUtils.getRolex(id_U, id_C, mongoTemplate);
        if (objRolex == null) {

            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.USER_NOT_FOUND.getCode(), null);


        }

        String id_A = dbUtils.getId_A(id_C, "a-auth");
        Query auth = new Query(new Criteria("_id").is(id_A));
        auth.fields().include("flowControl");
        Asset asset = mongoTemplate.findOne(auth, Asset.class);
        if (asset != null &&asset.getFlowControl() == null) {

            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.CARD_NO_HAVE.getCode(), null);

        } else {

            //获取公司所有聊天室并遍历
            List<Object> arrFlowControl = new LinkedList<>();
            for (int i = 0; i < asset.getFlowControl().getJSONArray("objData").size(); i++) {
                //拿到每一个聊天室
                JSONObject objIndex = asset.getFlowControl().getJSONArray("objData").getJSONObject(i);


                if (type.size() > 0){
                    //判断type数组中是否包含了这个聊天室的type
//                    if (type.contains(objIndex.get("type"))) {
//
//                        //判断职位数组中是否包含了当前用户的职位
//                        if (objIndex.getJSONArray("grpU").contains(objRolex.get("grpU"))){
//                            objIndex.remove("grpU");
//                            arrFlowControl.add(objIndex);
//                        }
//                    }
                }else{
                    //拿聊天室里面的职位数组
                    //判断职位数组中是否包含了当前用户的职位
//                    if (objIndex.getJSONArray("grpU").contains(objRolex.get("grpU"))){
//                        objIndex.remove("grpU");
//                        arrFlowControl.add(objIndex);
//                    }

                    for (int j = 0; j< objIndex.getJSONArray("objUser").size(); j++)
                    {
                        if (objIndex.getJSONArray("objUser").getJSONObject(j).getString("id_U").equals(id_U))
                        {
                            arrFlowControl.add(objIndex);
                        }
                    }

                }

            }
            return retResult.ok(CodeEnum.OK.getCode(),arrFlowControl);

        }
    }

    @Override
    public ApiResponse changeUserGrp(String id_U, String id_C, String grpUTarget, String uid) throws IOException {
        String grp = "1000";
        authCheck.getUserUpdateAuth(id_U, id_C, "lBUser", grp, "card", new JSONArray().fluentAdd("rolex"));
            Query userQ = new Query(
                    new Criteria("_id").is(uid));
            userQ.fields().include("rolex.objComp." + id_C).include("info");
            User user = mongoTemplate.findOne(userQ, User.class);
            if (user == null){
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_NOT_FOUND.getCode(), null);
            }
            JSONObject objRolex = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);

            //提前拿旧组别出来(需要声明常量，要不然对象修改了  它也跟着修改)
            final  String usedGrp = objRolex.getString("grpU");
            //修改grpU之前  去拿旧grpU去flowControl先清除，
            //后添加（添加的是新组别从ES里面拿，但是ES还没改所以拿的还是旧组别）得ES改完组别
            String id_A = dbUtils.getId_A(id_C,"a-auth");
            Query authQ = new Query(new Criteria("_id").is(id_A));
            authQ.fields().include("flowControl.objData");
            //默认有flowControl这张卡片
            Asset asset = mongoTemplate.findOne(authQ, Asset.class);
            if (asset == null){
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_NOT_FOUND.getCode(), null);
            }

            // Update All flowcontrol's objUser so that I can get into them
            JSONArray objData = asset.getFlowControl().getJSONArray("objData");
            for (int i = 0; i < objData.size(); i++) {
                //群组 有旧组别 就删除
                if (objData.getJSONObject(i).getJSONArray("grpU").contains(usedGrp)){
                    singleDelGrpU(id_C,uid,objData.getJSONObject(i).getJSONArray("objUser"));
                }
                //群组 有新调职的新组别 就添加
                if (objData.getJSONObject(i).getJSONArray("grpU").contains(grpUTarget)){
                    JSONArray jsonArray = singleAddGrpU(id_C,  uid);
                    objData.getJSONObject(i).getJSONArray("objUser").addAll(jsonArray);
                }
            }
            //清空当前公司的objMod之前先拿里面的编号去删除control里面的id_U数组
//            Query moduleQ = new Query(
//                    new Criteria("info.id_C").is(id_C)
//                            .and("info.ref").is("a-module"));
//            moduleQ.fields().include("control.objData");
//            //默认有control这张卡片
//            Asset assetModule = mongoTemplate.findOne(moduleQ, Asset.class);
//            if (assetModule == null){
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_NOT_FOUND.getCode(), null);
//            }
//            JSONArray controlObjData = assetModule.getControl().getJSONArray("objData");
//
//            for (int j = 0; j < objRolex.getJSONArray("objMod").size(); j++) {
//                for (int i = 0; i < controlObjData.size(); i++) {
//                    //判断模块编号和模块等级一致，从id_U数组中删除当前用户
//                    if (controlObjData.getJSONObject(i).getString("ref").equals(objRolex.getJSONArray("objMod").getJSONObject(j).getString("ref")) &&
//                            controlObjData.getJSONObject(i).getString("bcdLevel").equals(objRolex.getJSONArray("objMod").getJSONObject(j).getString("bcdLevel"))){
//                        controlObjData.getJSONObject(i).getJSONArray("id_U").remove(uid);
//                    }
//                }
//            }
//            objRolex.remove("objMod");

            mongoTemplate.updateFirst(authQ, new Update().set("flowControl.objData",objData), Asset.class);
//            mongoTemplate.updateFirst(moduleQ, new Update().set("control.objData",controlObjData), Asset.class);
            //把Rolex 的所有module卸下，由人事部以後手動要裝上
            //改组别
            objRolex.put("grpU",grpUTarget);
            UpdateResult updateResult = mongoTemplate.updateFirst(userQ, new Update().set("rolex.objComp." + id_C,objRolex), User.class);
            //
            if (updateResult.getModifiedCount() > 0) {

                SearchRequest searchRequest = new SearchRequest("lBUser");
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                        //条件1：
                        .must(QueryBuilders.termQuery("id_U", uid))
                        //条件2
                        .must(QueryBuilders.termQuery("id_CB", id_C));
                sourceBuilder.query(queryBuilder);
                searchRequest.source(sourceBuilder);

                SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

                SearchHit hit = search.getHits().getHits()[0];
                if (search.getHits().getHits().length > 0) {
                    hit.getSourceAsMap().put("grpU",grpUTarget);
                    UpdateRequest updateRequest = new UpdateRequest("lBUser",hit.getId());
                    updateRequest.doc(hit.getSourceAsMap());
                    restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);


                }
            }

            return retResult.ok(CodeEnum.OK.getCode(), objRolex);
    }



    //单个添加进群
    public JSONArray singleAddGrpU(String id_C,  String uid) throws IOException {

        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lbuser");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        JSONArray result = new JSONArray();

        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                //条件1：当前公司id
                .must(QueryBuilders.termQuery("id_CB", id_C))
                //条件2：grpU
                .must(QueryBuilders.termQuery("id_U", uid));
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        //把构建对象放入，指定查那个对象，把查询条件放进去
        searchRequest.source(searchSourceBuilder);
        //执行请求
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit hit : search.getHits().getHits()) {
            JSONObject objUser = new JSONObject();
            objUser.put("imp",3);
            objUser.put("id_U",hit.getSourceAsMap().get("id_U"));
            //三元运算符
            objUser.put("id_APP", hit.getSourceAsMap().getOrDefault("id_APP", ""));
            result.add(objUser);
        }


        return result;

    }
    //单个删除群里面的某个人
    public void singleDelGrpU(String id_C, String uid,JSONArray objUser ) throws IOException {

        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lbuser");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                //条件1：当前公司id
                .must(QueryBuilders.termQuery("id_CB", id_C))
                //条件2：grpU
                .must(QueryBuilders.termQuery("id_U", uid));
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        //把构建对象放入，指定查那个对象，把查询条件放进去
        searchRequest.source(searchSourceBuilder);
        //执行请求
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit hit : search.getHits().getHits()) {

            for (int i = 0; i < objUser.size(); i++) {

                if (objUser.getJSONObject(i).containsValue(hit.getSourceAsMap().get("id_U"))){
                    objUser.remove(i);
                    i--;
                }
            }
        }
    }


    public ApiResponse getMyFavComp(String id_U) {

        // 查询哪个用户
        Criteria criteria = new Criteria();
        criteria.and("_id").is(id_U);
        AggregationOperation match = Aggregation.match(criteria);


        // 因为rolex的id_C是string，comp表的_id是objectId，所以要将id_C string转ObjectId才能关联  ($toObjectId)
        Map<String, Object> map1 = new HashMap<>();
        map1.put("$toObjectId", "$$r.id_C");

        Map<String, Object> map2 = new HashMap<>();
        map2.put("input", "$fav.objFav");
        map2.put("as", "r");
        map2.put("in", map1);

        AggregationOperation addFields = new AggregationOperation() {

            @Override
            public Document toDocument(AggregationOperationContext context) {
                return new Document("$addFields",
                        new Document("convertedId",
                                new Document("$map",
                                        new Document(map2))));
            }

        };


        AggregationOperation lookup = Aggregation.lookup(
                "Comp",
                "convertedId",
                "_id",
                "docs");

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                addFields,
                lookup,
                Aggregation.project().andInclude("docs").andExclude("_id")
        );

        Document compInfo = null;
        try {
            compInfo = mongoTemplate.aggregate(aggregation, "User", Document.class).getUniqueMappedResult();
        } catch (RuntimeException e) {

            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

        }

        SerializeConfig config = new SerializeConfig();
        //解决mongdb id序列化问题
        config.put(ObjectId.class, new ObjectIdJsonSerializer());
        JSONObject compInfoJson = (JSONObject) JSON.parse(JSON.toJSONString(compInfo, config));

        // 最终返回数据
        JSONArray result = new JSONArray();

        for (Object docs : compInfoJson.getJSONArray("docs")) {

            JSONObject docsJson = (JSONObject) docs;
            JSONObject newDocs = new JSONObject();
            newDocs.put("compId", docsJson.getString("_id"));
            newDocs.put("wrdN", docsJson.getJSONObject("info").getJSONObject("wrdN"));
            newDocs.put("pic", docsJson.getJSONObject("info").getString("pic"));

            result.add(newDocs);
        }
        return retResult.ok(CodeEnum.OK.getCode(),result);


    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse addFav(String id_U, String id_C)  {
//
//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        //flowControl在lSAsset
//        reqJson.put("listType", "lSAsset");
//        //默认组别（老K说的，错了找他）
//        reqJson.put("grp", "1003");
//        reqJson.put("authType", "card");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("flowControl"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        //权限判断
//        if ("200".equals(authModuleJson.getString("code"))) {


        JSONObject rolex = MongoUtils.getRolex(id_U, id_C, mongoTemplate);
        //查询此编号，如果有，那证明重复了
        if (rolex != null){
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, DetailsEnum.COMP_IS_FOUND.getCode(), null);

        }


        //push添加  pull 删除
        mongoTemplate.updateFirst(new Query(
                new Criteria("id").is(id_U)
        ), new Update().push("fav.objFav", new JSONObject().fluentPut("id_C",id_C)).addToSet("view","fav"), User.class);


        return retResult.ok(CodeEnum.OK.getCode(),null);
//        }
//
//        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);

    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse delFav(String id_U, String id_C) {
//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        //flowControl在lSAsset
//        reqJson.put("listType", "lSAsset");
//        reqJson.put("grp", "1003");
//        reqJson.put("authType", "card");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("flowControl"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        //权限判断
//        if ("200".equals(authModuleJson.getString("code"))) {

        Document document = new Document();
        //指定对象中的某个键的值，相匹配
        document.put("id_C",id_C);


        //push添加  pull 删除
        mongoTemplate.updateFirst(new Query(
                new Criteria("id").is(id_U)
                        .and("fav.objFav.id_C").is(id_C)), new Update().pull("fav.objFav", document), User.class);


        return retResult.ok(CodeEnum.OK.getCode(),null);
//        }
//
//
//        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }


//    @Override
//    public ApiResponse changeUserGrp(String id_U, String id_C, String grp, String listType, String id) throws IOException {
//
////        JSONObject reqJson = new JSONObject();
////        reqJson.put("id_U", id_U);
////        reqJson.put("id_C", id_C);
////        reqJson.put("listType", listType);
////        reqJson.put("grp", grp);
////        reqJson.put("authType", "batch");//卡片/按钮  card/batch
////        reqJson.put("params", new JSONArray().fluentAdd("delete"));//卡片名称/按钮名称
////        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
////        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
////        if ("200".equals(authModuleJson.getString("code"))){
//
//
//        //构建查询库
//        SearchRequest searchRequest = new SearchRequest(listType);
//        //构建搜索条件
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        //组合查询条件
//        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//        //条件1 id
//        boolQueryBuilder.must(QueryBuilders.termQuery("id_U", id));
//        //条件2 公司id
//        boolQueryBuilder.must(QueryBuilders.termQuery("id_CB", id_C));
//
//        searchSourceBuilder.query(boolQueryBuilder);
//        searchRequest.source(searchSourceBuilder);
//        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//
//        if (search.getHits().getHits().length > 0){
//
//            Update updateMG = new Update();
//
//            SearchHit hit = search.getHits().getHits()[0];
//
//            UpdateRequest updateRequest = new UpdateRequest(listType,hit.getId());
//            hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//
//            hit.getSourceAsMap().put("usedGrp",hit.getSourceAsMap().get("grpU"));
//            //改新的grp
//            hit.getSourceAsMap().put("grpU",grp);
//            updateRequest.doc(hit.getSourceAsMap());
//            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
//
//            if (listType.equals("lBUser") ){
//                Query query = new Query(new Criteria().and("_id").is(id));
//                query.fields().include("rolex.objComp."+id_C);
//
//                User user = mongoTemplate.findOne(query, User.class);
//                JSONObject objRolex = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);
//
//                objRolex.put("grpU",grp);
//                updateMG.set("rolex.objComp."+id_C,objRolex);
//
//                if (!ObjectUtils.isEmpty(updateMG.getUpdateObject()) && update.status().getStatus() == 200 ) {
//
//                    mongoTemplate.updateFirst(query, updateMG, User.class);
//
//                    return retResult.ok(CodeEnum.OK.getCode(),null);
//                }
//            }
//        }
//
//
//        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");

////        }
////
////        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
////
//    }

    @Override
    public ApiResponse changeAssetGrp(String id_U, String id_C, String grp, String listType, String id) throws IOException {

//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);
//        reqJson.put("grp", grp);
//        reqJson.put("authType", "batch");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("delete"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){



        //构建查询库
        SearchRequest searchRequest = new SearchRequest(listType);
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //组合查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //条件1 id
        boolQueryBuilder.must(QueryBuilders.termQuery("id_A", id));
        //条件2 公司id
        boolQueryBuilder.must(QueryBuilders.termQuery("id_C", id_C));

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        if (search.getHits().getHits().length > 0) {

            Update updateMG = new Update();



            SearchHit hit = search.getHits().getHits()[0];

            //数量 = 0.0  lAT = 物品
//            if (hit.getSourceAsMap().get("wn2qty").equals(0.0) && hit.getSourceAsMap().get("lAT").equals(2)){

                UpdateRequest updateRequest = new UpdateRequest(listType,hit.getId());

                //放旧组别
                hit.getSourceAsMap().put("usedGrp", hit.getSourceAsMap().get("grp"));
                //改新的grp
                hit.getSourceAsMap().put("grp", grp);
                //定时器根据更新时间来删除
                hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

                updateRequest.doc(hit.getSourceAsMap());
                UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

                updateMG.set("info.grp", grp);

                if (!ObjectUtils.isEmpty(updateMG.getUpdateObject()) && update.status().getStatus() == 200) {

                    Query query = new Query(new Criteria().and("_id").is(id));

                    mongoTemplate.updateFirst(query, updateMG, Asset.class);

                }

                return retResult.ok(CodeEnum.OK.getCode(), null);

//            }else{
//
//                throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.TYPE_ERROR.getCode(), null);
//
//            }


        }

        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");

//        }
//
        //throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
    }

    @Override
    public ApiResponse changeProdGrp(String id_U, String id_C, String grp, String listType, String id) throws IOException {

//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);
//        reqJson.put("grp", grp);
//        reqJson.put("authType", "batch");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("delete"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){

        String compID = "";
        String group = "";
        String def = "";

        boolean judge = true;

        if (listType.equals("lSProd")) {
            group = "grp";
            compID = "id_C";
            def = "objlSP";
        }else{
            group = "grpB";
            compID = "id_CB";
            def = "objlBP";
            judge = false;

        }

        //构建查询库
        SearchRequest searchRequest = new SearchRequest(listType);
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //组合查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //条件1 id
        boolQueryBuilder.must(QueryBuilders.termQuery("id_P", id));
        //条件2 公司id
        boolQueryBuilder.must(QueryBuilders.termQuery(compID, id_C));

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        if (search.getHits().getHits().length > 0) {

            Update updateMG = new Update();

            SearchHit hit = search.getHits().getHits()[0];

            //如果是lBProd就进来，判断ES中的id_C公司是0或者2都改info中的组别
            if (listType.equals("lBProd")){
                //对方公司id、我放公司id
                int bcdNet = dbUtils.judgeComp(id_C,hit.getSourceAsMap().get("id_C").toString());

                if (bcdNet == 0 || bcdNet == 2){
                    judge = true;
                }

//                hit.getSourceAsMap().put("bcdNet", bcdNet);
                //放旧组别
                hit.getSourceAsMap().put("usedGrpB", hit.getSourceAsMap().get(group));
            }else{
                //放旧组别
                hit.getSourceAsMap().put("usedGrp", hit.getSourceAsMap().get(group));
            }


            //改新的grp
            hit.getSourceAsMap().put(group, grp);
            //定时器根据更新时间来删除
            hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

            UpdateRequest updateRequest = new UpdateRequest(listType,hit.getId());

            updateRequest.doc(hit.getSourceAsMap());
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

            updateMG.set("info."+group, grp);

            if (!ObjectUtils.isEmpty(updateMG.getUpdateObject()) && update.status().getStatus() == 200 && judge) {
                Query queryProd = new Query(new Criteria("_id").is(id));
                Prod prod = mongoTemplate.findOne(queryProd, Prod.class);
                String oldGrp = prod.getInfo().getGrp();
                String pic = prod.getInfo().getPic();
                JSONObject wrdN = prod.getInfo().getWrdN();
                JSONObject wrddesc = prod.getInfo().getWrddesc();
                UpdateResult updateResultProd = mongoTemplate.updateFirst(queryProd, updateMG, Prod.class);
                System.out.println("updateResultProd=" + updateResultProd);
                if (grp.equals("1019")) {
                    String id_A = dbUtils.getId_A(id_C, "a-auth");
                    JSONObject jsonTemp = new JSONObject();
                    jsonTemp.put("id_P", id);
                    jsonTemp.put("pic", pic);
                    jsonTemp.put("wrdN", wrdN);
                    jsonTemp.put("wrddesc", wrddesc);
                    Query queryAsset = new Query(new Criteria("_id").is(id_A));
                    Asset asset = mongoTemplate.findOne(queryAsset, Asset.class);
                    JSONArray arrayDef = asset.getDef().getJSONArray(def);
                    Integer index = null;
                    for (int i = 0; i < arrayDef.size(); i++) {
                        if (arrayDef.getJSONObject(i).getString("ref").equals(oldGrp)) {
                            index = i;
                        }
                    }
                    if (index == null) {
                        throw new ErrorResponseException(HttpStatus.FORBIDDEN, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
                    }
                    Update updateAsset = new Update();
                    updateAsset.push("def." + def + "." + index + ".id_temp", jsonTemp);
                    UpdateResult updateResultAsset = mongoTemplate.updateFirst(queryAsset, updateAsset, Asset.class);
                    System.out.println("updateResultAsset=" + updateResultAsset);
                    if (updateResultProd.getModifiedCount() > 0 && updateResultAsset.getModifiedCount() > 0) {
                        return retResult.ok(CodeEnum.OK.getCode(), true);
                    }
                }
                if (updateResultProd.getModifiedCount() > 0) {
                    return retResult.ok(CodeEnum.OK.getCode(), true);
                }
            }
        }
        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");

//        }
//
        //throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
    }

    @Override
    public ApiResponse changeOrderGrp(String id_U, String id_C, String grp, String listType, String id) throws IOException {
//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);
//        reqJson.put("grp", grp);
//        reqJson.put("authType", "batch");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("delete"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){

        String compID = "";
        String def = "";

        if (listType.equals("lSOrder")) {
            compID = "id_C";
            def = "objlSO";
        }else{
            compID = "id_CB";
            def = "objlBO";
        }

        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lSBOrder");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //组合查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //条件1 id
        boolQueryBuilder.must(QueryBuilders.termQuery("id_O", id));
        //条件2 公司id
        boolQueryBuilder.must(QueryBuilders.termQuery(compID, id_C));

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("search.length=" + search.getHits().getHits().length);
        if (search.getHits().getHits().length > 0) {

            Update updateMG = new Update();

            SearchHit hit = search.getHits().getHits()[0];

            //如果lST >= 8  订单合同已签  不可修改  要小于8
            if (Integer.parseInt(hit.getSourceAsMap().get("lST").toString())  >= 8){

                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.CONFIRMED_CANNOT_BE_DEL.getCode(), null);

            }

            //两边都一起改组别
            //放旧组别
            hit.getSourceAsMap().put("usedGrp", hit.getSourceAsMap().get("grp"));
            hit.getSourceAsMap().put("usedGrpB", hit.getSourceAsMap().get("grpB"));
            //改新的grp
            hit.getSourceAsMap().put("grp", grp);
            hit.getSourceAsMap().put("grpB", grp);
            //定时器根据更新时间来删除
            hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

            UpdateRequest updateRequest = new UpdateRequest("lSBOrder",hit.getId());
            updateRequest.doc(hit.getSourceAsMap());
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

            updateMG.set("info.grp", grp);
            updateMG.set("info.grpB", grp);

            if (!ObjectUtils.isEmpty(updateMG.getUpdateObject()) && update.status().getStatus() == 200 ) {
                Query queryOrder = new Query(new Criteria("_id").is(id));
                Order order = mongoTemplate.findOne(queryOrder, Order.class);
                String oldGrp = order.getInfo().getGrp();
                String pic = order.getInfo().getPic();
                JSONObject wrdN = order.getInfo().getWrdN();
                JSONObject wrddesc = order.getInfo().getWrddesc();
                UpdateResult updateResultOrder = mongoTemplate.updateFirst(queryOrder, updateMG, Order.class);
                System.out.println("updateResultOrder=" + updateResultOrder);
                if (grp.equals("1019")) {
                    String id_A = dbUtils.getId_A(id_C, "a-auth");
                    JSONObject jsonTemp = new JSONObject();
                    jsonTemp.put("id_P", id);
                    jsonTemp.put("pic", pic);
                    jsonTemp.put("wrdN", wrdN);
                    jsonTemp.put("wrddesc", wrddesc);
                    Query queryAsset = new Query(new Criteria("_id").is(id_A));
                    Asset asset = mongoTemplate.findOne(queryAsset, Asset.class);
//                    JSONArray arrayDef = asset.getDef().getJSONArray(def);
//                    Integer index = null;
//                    for (int i = 0; i < arrayDef.size(); i++) {
//                        if (arrayDef.getJSONObject(i).getString("ref").equals(oldGrp)) {
//                            index = i;
//                        }
//                    }
//                    if (index == null) {
//                        throw new ErrorResponseException(HttpStatus.FORBIDDEN, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
//                    }
                    Update updateAsset = new Update();

                    updateAsset.push("def." + def + "." + oldGrp + ".id_temp", jsonTemp);

                    UpdateResult updateResultAsset = mongoTemplate.updateFirst(queryAsset, updateAsset, Asset.class);
                    System.out.println("updateResultAsset=" + updateResultAsset);
                    if (updateResultOrder.getModifiedCount() > 0 && updateResultAsset.getModifiedCount() > 0) {
                        return retResult.ok(CodeEnum.OK.getCode(), "");
                    }
                }
                if (updateResultOrder.getModifiedCount() > 0) {
                    return retResult.ok(CodeEnum.OK.getCode(), "");
                }
            }
        }
        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");

//        }
//
        //throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
    }
    @Override
    public ApiResponse orderRelease(String id_O, JSONObject tokData) throws IOException {
        //1. get id_O
        //check lST ok
        //change opposite side to grp = 1001 "await handling"
        //send log to comp's handler
        //if not real comp, return code not-real no need release
        String myCompId = tokData.getString("id_C");

        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info"));

        if (!order.getInfo().getLST().equals(4))
        {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_ALL_ERROR.getCode(), "it's not planning");

        }
        if (order.getInfo().getId_CB().equals(myCompId))
        {
            // I am buyer, check if seller is real
            if (dbUtils.judgeComp(myCompId,order.getInfo().getId_C()) != 1)
            {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_ALL_ERROR.getCode(), "internal order");
            }
            //set grp => 1001
            JSONObject mapKey = new JSONObject();
            mapKey.put("info.grp","1001");
            coupaUtil.updateOrderByListKeyVal(id_O,mapKey);

            //update lsborder
            JSONObject listCol = new JSONObject();
            listCol.put("grp", "1001");
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_O", id_O));
            dbUtils.updateListCol(queryBuilder, "lsborder", listCol);
            //send log to cusmsg
        } else {
            //I am seller now, check if buyer is real
            if (dbUtils.judgeComp(myCompId,order.getInfo().getId_CB()) != 1)
            {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_ALL_ERROR.getCode(),"internal order");
            }
            JSONObject mapKey = new JSONObject();
            mapKey.put("info.grpB","1001");
            coupaUtil.updateOrderByListKeyVal(id_O,mapKey);

            //update lsborder
            JSONObject listCol = new JSONObject();
            listCol.put("grpB", "1001");
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_O", id_O));
            dbUtils.updateListCol(queryBuilder, "lsborder", listCol);
            //send log to cusmsg
        }

        return retResult.ok(CodeEnum.OK.getCode(), "");
    }

    @Override
    public ApiResponse changeCompGrp(String id_U, String id_C, String grp, String listType, String id) throws IOException {
        //        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);
//        reqJson.put("grp", grp);
//        reqJson.put("authType", "batch");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("delete"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){

        String compID = "";
        String listID = "";//列表ID
        String group = "";
        if (listType.equals("lBComp")) {
            compID = "id_CB";
            listID = "id_C";
            group = "grpB";
        }  else if (listType.equals("lSComp")) {
            compID = "id_C";
            listID = "id_CB";
            group = "grp";
        }


        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lSBComp");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //组合查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //条件1 对方公司id
        boolQueryBuilder.must(QueryBuilders.termQuery(listID, id));
        //条件2 自己公司id
        boolQueryBuilder.must(QueryBuilders.termQuery(compID, id_C));

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        if (search.getHits().getHits().length > 0) {

            SearchHit hit = search.getHits().getHits()[0];

            UpdateRequest updateRequest = new UpdateRequest("lSBComp",hit.getId());

            //保存旧组别
            hit.getSourceAsMap().put("usedGrp", hit.getSourceAsMap().get("grp"));

            //改新的grp
            hit.getSourceAsMap().put(group, grp);

            //定时器根据更新时间来删除
            hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

            updateRequest.doc(hit.getSourceAsMap());

            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

            return retResult.ok(CodeEnum.OK.getCode(), null);

        }

        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");

//        }
//
        //throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
    }


//    @Override
//    public ApiResponse restoreUser(String id_U,String id_C,String uid) throws IOException {
//
////        JSONObject reqJson = new JSONObject();
////        reqJson.put("id_U", id_U);
////        reqJson.put("id_C", id_C);
////        reqJson.put("listType", listType);
////        reqJson.put("grp", grp);
////        reqJson.put("authType", "batch");//卡片/按钮  card/batch
////        reqJson.put("params", new JSONArray().fluentAdd("restore"));//卡片名称/按钮名称
////        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
////        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
////        if ("200".equals(authModuleJson.getString("code"))){
//
//        //构建查询库
//        SearchRequest searchRequest = new SearchRequest("lBUser");
//        //构建搜索条件
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        //组合查询条件
//        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//        //条件1 id
//        boolQueryBuilder.must(QueryBuilders.termQuery("id_U", uid));
//        //条件2 公司id
//        boolQueryBuilder.must(QueryBuilders.termQuery("id_CB", id_C));
//
//        searchSourceBuilder.query(boolQueryBuilder);
//        searchRequest.source(searchSourceBuilder);
//        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//
//        if (search.getHits().getHits().length > 0){
//            Update updateMG = new Update();
//            SearchHit hit = search.getHits().getHits()[0];
//            UpdateRequest updateRequest = new UpdateRequest("lBUser",hit.getId());
//            hit.getSourceAsMap().put("grpU","1020");
//            updateRequest.doc(hit.getSourceAsMap());
//            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
//
//            Query query = new Query(new Criteria().and("_id").is(uid));
//            query.fields().include("rolex.objComp."+ id_C);
//
//            User user = mongoTemplate.findOne(query, User.class);
//            JSONObject objRolex = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);
//
//            //清空当前公司的objMod之前先拿里面的编号去删除control里面的id_U
//            Query authQ = new Query(
//                    new Criteria("info.id_C").is(id_C)
//                            .and("info.ref").is("a-module"));
//            authQ.fields().include("control.objData");
//            //默认有control这张卡片
//            Asset asset = mongoTemplate.findOne(authQ, Asset.class);
//            JSONArray objData = asset.getControl().getJSONArray("objData");
//
//            for (int j = 0; j < objRolex.getJSONArray("objMod").size(); j++) {
//
//                for (int i = 0; i < objData.size(); i++) {
//                    //判断模块编号和模块等级一致，从id_U数组中删除当前用户
//                    if (objData.getJSONObject(i).getString("ref").equals(objRolex.getJSONArray("objMod").getJSONObject(j).getString("ref"))&&
//                            objData.getJSONObject(i).getString("bcdLevel").equals(objRolex.getJSONArray("objMod").getJSONObject(j).getString("bcdLevel"))){
//
//                        objData.getJSONObject(i).getJSONArray("id_U").remove(uid);
//
//                    }
//
//                }
//            }
//
//
//
//
//            //清空当前公司的个人模块权限
//            objRolex.remove("objMod");
//            objRolex.put("grpU","1020");
//            updateMG.set("rolex.objComp."+ id_C,objRolex);
//
//            if (!ObjectUtils.isEmpty(updateMG.getUpdateObject())  && update.status().getStatus() == 200) {
//
//                mongoTemplate.updateFirst(authQ, new Update().set("control.objData",objData), Asset.class);
//
//                mongoTemplate.updateFirst(query, updateMG, User.class);
//            }
//
//
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//
//        }
//
//        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");

//
//
//    }

    @Override
    public ApiResponse restoreAssetGrp(String id_U, String id_C, String listType, String id) throws IOException {

//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);
//        reqJson.put("grp", grp);
//        reqJson.put("authType", "batch");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("restore"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){


        //构建查询库
        SearchRequest searchRequest = new SearchRequest(listType);
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //组合查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //条件1 id
        boolQueryBuilder.must(QueryBuilders.termQuery("id_A", id));
        //条件2 公司id
        boolQueryBuilder.must(QueryBuilders.termQuery("id_C", id_C));

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        if (search.getHits().getHits().length > 0){

            //旧组别
            String usedGrp = "";

            Update updateMG = new Update();

            SearchHit hit = search.getHits().getHits()[0];

            usedGrp = hit.getSourceAsMap().get("usedGrp").toString();

            UpdateRequest updateRequest = new UpdateRequest(listType,hit.getId());

            //因 无法同时提供脚本和文档，所以只能选择脚本
            StringBuffer script = new StringBuffer();
            script.append("ctx._source.grp = '" + usedGrp + "'");  //更新字段
            script.append(";ctx._source.remove(\"usedGrp\")");          //删除字段
//            script.append(";ctx._source.param1 = '" + value3 + "'");
            updateRequest.script(new Script(script.toString()));

            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

            updateMG.set("info.grp",usedGrp);

            if (!ObjectUtils.isEmpty(updateMG.getUpdateObject()) && update.status().getStatus() == 200) {

                Query query = new Query(new Criteria().and("_id").is(id));

                mongoTemplate.updateFirst(query, updateMG, Asset.class);

            }
            return retResult.ok(CodeEnum.OK.getCode(), null);


        }

        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");


//        }
//
//        return RetResult.errorJsonResult(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
    }

    @Override
    public ApiResponse restoreProdGrp(String id_U, String id_C, String listType, String id) throws IOException {

//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);
//        reqJson.put("grp", grp);
//        reqJson.put("authType", "batch");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("delete"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){


        String compID = "";

        String group = "";

        boolean judge = true;

        if (listType.equals("lSProd")) {
            group = "grp";
            compID = "id_C";

        }else{
            group = "grpB";
            compID = "id_CB";
            judge = false;

        }


        //构建查询库
        SearchRequest searchRequest = new SearchRequest(listType);
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //组合查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //条件1 id
        boolQueryBuilder.must(QueryBuilders.termQuery("id_P", id));
        //条件2 公司id
        boolQueryBuilder.must(QueryBuilders.termQuery(compID, id_C));

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        if (search.getHits().getHits().length > 0) {

            //旧组别
            String usedGrp = "";

            //删除字段
            String removeGrp = "";

            Update updateMG = new Update();
            SearchHit hit = search.getHits().getHits()[0];

            //如果是lBProd就进来，判断ES中的id_C公司是0或者2都改info中的组别
            if (listType.equals("lBProd")){
                //对方公司id、我放公司id
                int bcdNet = dbUtils.judgeComp(id_C, hit.getSourceAsMap().get("id_C").toString());

                if (bcdNet == 0 || bcdNet == 2){
                    judge = true;
                }

                usedGrp = hit.getSourceAsMap().get("usedGrpB").toString();
                removeGrp = "usedGrpB";
            }else{
                usedGrp = hit.getSourceAsMap().get("usedGrp").toString();
                removeGrp = "usedGrp";
            }



            UpdateRequest updateRequest = new UpdateRequest(listType,hit.getId());

            //因 无法同时提供脚本和文档，所以只能选择脚本
            StringBuffer script = new StringBuffer();
            script.append("ctx._source."+group +"= '" + usedGrp + "'");  //更新字段
            script.append(";ctx._source.remove('" + removeGrp + "')");          //删除字段
//            script.append(";ctx._source.remove(\"usedGrp\")");          //删除字段
//            script.append(";ctx._source.param1 = '" + value3 + "'");
            updateRequest.script(new Script(script.toString()));

            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

            if (update.status().getStatus() == 200 && judge) {

                updateMG.set("info."+group, usedGrp);

                mongoTemplate.updateFirst(new Query(new Criteria("_id").is(id)), updateMG, Prod.class);

            }

            return retResult.ok(CodeEnum.OK.getCode(), null);

        }

        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");

//        }
//
        //throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
    }

    @Override
    public ApiResponse restoreOrderGrp(String id_U, String id_C, String listType, String id) throws IOException {
        //        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);
//        reqJson.put("grp", grp);
//        reqJson.put("authType", "batch");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("delete"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){

        String compID = "";

        if (listType.equals("lSOrder")) {

            compID = "id_C";

        }else{
            compID = "id_CB";

        }


        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lSBOrder");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //组合查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //条件1 id
        boolQueryBuilder.must(QueryBuilders.termQuery("id_O", id));
        //条件2 公司id
        boolQueryBuilder.must(QueryBuilders.termQuery(compID, id_C));

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        if (search.getHits().getHits().length > 0) {


            //旧组别
            String usedGrp = "";
            //旧组别
            String usedGrpB = "";

            Update updateMG = new Update();


            SearchHit hit = search.getHits().getHits()[0];


            usedGrp = hit.getSourceAsMap().get("usedGrp").toString();

            usedGrpB = hit.getSourceAsMap().get("usedGrpB").toString();

            UpdateRequest updateRequest = new UpdateRequest("lSBOrder",hit.getId());

            //因 无法同时提供脚本和文档，所以只能选择脚本
            StringBuffer script = new StringBuffer();
            script.append("ctx._source.grp = '" + usedGrp + "'");  //更新字段
            script.append(";ctx._source.grpB = '" + usedGrpB + "'");  //更新字段
            script.append(";ctx._source.remove(\"usedGrp\")");          //删除字段
            script.append(";ctx._source.remove(\"usedGrpB\")");          //删除字段
//            script.append(";ctx._source.param1 = '" + value3 + "'");
            updateRequest.script(new Script(script.toString()));

            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

            updateMG.set("info.grp", usedGrp);
            updateMG.set("info.grpB", usedGrpB);

            if (!ObjectUtils.isEmpty(updateMG.getUpdateObject()) && update.status().getStatus() == 200 ) {

                mongoTemplate.updateFirst(new Query(new Criteria("_id").is(id)), updateMG, Order.class);

            }

            return retResult.ok(CodeEnum.OK.getCode(), null);

        }

        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");

//        }
//
        //throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
    }

    @Override
    public ApiResponse restoreCompGrp(String id_U, String id_C, String listType, String id) throws IOException {

//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);
//        reqJson.put("grp", grp);
//        reqJson.put("authType", "batch");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("restore"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){
        String group = "";

        String compID = "";
        String listID = "";//列表ID

        if (listType.equals("lBComp")) {

            compID = "id_CB";
            listID = "id_C";
            group = "grpB";

        }else if (listType.equals("lSComp")) {

            compID = "id_C";
            listID = "id_CB";
            group = "grp";

        }


        //构建查询库
        SearchRequest searchRequest = new SearchRequest("lSBComp");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //组合查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //条件1 对方公司id
        boolQueryBuilder.must(QueryBuilders.termQuery(listID, id));
        //条件2 自己公司id
        boolQueryBuilder.must(QueryBuilders.termQuery(compID, id_C));

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        if (search.getHits().getHits().length > 0){

            //旧组别
            String usedGrp = "";


            SearchHit hit = search.getHits().getHits()[0];

            usedGrp = hit.getSourceAsMap().get("usedGrp").toString();

            UpdateRequest updateRequest = new UpdateRequest("lSBComp",hit.getId());

            //因 无法同时提供脚本和文档，所以只能选择脚本
            StringBuffer script = new StringBuffer();
            script.append("ctx._source."+ group +"= '" + usedGrp + "'");  //更新字段
            script.append(";ctx._source.remove(\"usedGrp\")");          //删除字段
//            script.append(";ctx._source.param1 = '" + value3 + "'");
            updateRequest.script(new Script(script.toString()));

            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);


            return retResult.ok(CodeEnum.OK.getCode(), null);

        }

        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), "");


//        }
//
//        return RetResult.errorJsonResult(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
    }

    @Override
    public ApiResponse updateCascadeInfo(String id_O, JSONObject changes) throws IOException  {

        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("id_O", id_O));
        JSONObject mapKey = new JSONObject();
        JSONObject listCol = new JSONObject();
        if (changes.getJSONObject("wrdN") != null)
        {
            mapKey.put("info.wrdN", changes.getJSONObject("wrdN"));
            listCol.put("wrdN", changes.getJSONObject("wrdN"));
        }
        if (changes.getString("grpB")!= null)
        {
            mapKey.put("info.grpB", changes.getString("grpB"));
            listCol.put("grpB", changes.getString("grpB"));
        }
        if (changes.getString("refB")!= null)
        {
            mapKey.put("info.refB", changes.getString("refB"));
            listCol.put("refB", changes.getString("refB"));
        }
        if (changes.getString("lST") != null)
        {
            mapKey.put("info.lST", changes.getString("lST"));
            listCol.put("lST", changes.getString("lST"));
        }
        try {
            coupaUtil.updateOrderByListKeyVal(id_O, mapKey);
            dbUtils.updateListCol(queryBuilder, "lsborder", listCol);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("出现错误:" + e.getMessage());
        }
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


}
