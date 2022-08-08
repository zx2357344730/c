package com.cresign.details.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.details.enumeration.DetailsEnum;
import com.cresign.details.service.OtherService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.pojo.po.assetCard.AssetInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.script.*;
import java.io.IOException;
import java.util.Map;


@Service
@Log4j2
public class OtherServicelmpl implements OtherService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DbUtils dbUtils;


    /**
     * 自动注入MongoTemplate类
     */
    @Resource
    private MongoTemplate mongoTemplate;


    @Autowired
    private RetResult retResult;

    @Override
    public ApiResponse paymentOrder(String id_C, String id_U, String grp , String listType, String id_O, String id_A, Double wn2mnyPaid) throws IOException {

//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);  //列表
//        reqJson.put("grp", grp);    //组别
//        reqJson.put("authType", "card");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("oMoney"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){

        Query orderCondition = new Query(new Criteria("_id").is(id_O));
        orderCondition.fields().include("info").include("oMoney");
        Order order = mongoTemplate.findOne(orderCondition, Order.class);

        if (order == null || order.getOMoney() == null){

            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.CARD_NO_HAVE.getCode(), null);

        }


        Query assetCondition = new Query(new Criteria("_id").is(id_A));
        assetCondition.fields().include("info");
        Asset moneyAsset = mongoTemplate.findOne(assetCondition, Asset.class);

        //lAT == 1 是资金   wn2qty(金钱)字段不能等于空
        if (moneyAsset != null && moneyAsset.getInfo().getLAT().equals(1) && moneyAsset.getInfo().getWn2qty() != null){
            //公司账户资金必须大于支付金额
            if (moneyAsset.getInfo().getWn2qty() >= wn2mnyPaid){
                boolean judge = false;

                //1.修改ES列表
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                        //条件1：
                        .must(QueryBuilders.termQuery("id_A", id_A))
                        //条件2
                        .must(QueryBuilders.termQuery("id_C",id_C));
                searchSourceBuilder.query(queryBuilder);
                SearchRequest srb = new SearchRequest("lSAsset");

                srb.source(searchSourceBuilder);

                SearchResponse search = restHighLevelClient.search(srb, RequestOptions.DEFAULT);

                SearchHit hit = search.getHits().getHits()[0];
                UpdateRequest updateRequest = new UpdateRequest();

                hit.getSourceAsMap().put("wn2qty", Double.parseDouble(hit.getSourceAsMap().get("wn2qty").toString()) -  wn2mnyPaid);
                hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
                updateRequest.index("lSAsset");
                updateRequest.id( hit.getId());
                updateRequest.doc(hit.getSourceAsMap());


                //2.修改mongdb
                Query Condition = new Query(new Criteria("_id").is(id_A).and("info").exists(true));
                Condition.fields().include("info");
                //查询id_A的对象
                Asset asset = mongoTemplate.findOne(Condition, Asset.class);
                AssetInfo assetInfo =  asset.getInfo();
                Update update = new Update();
                //原始金额 - 出库金额   可能只需要info.wn2qty来修改，不需要拿一整个info对象出来
                assetInfo.setWn2qty(assetInfo.getWn2qty()  - wn2mnyPaid);
                update.set("info", assetInfo);
                if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                    mongoTemplate.updateFirst(Condition, update, Asset.class);
                    //修改ES放在修改mongdb后面
                    restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);


                    //发送ES流水日志
                    judge = true;
                }

                if (judge){
                    Update orderUpdate = new Update();
                    //买家付了多少钱，卖家可以收款多少钱
                    order.getOMoney().put("wn2mnyPaid",order.getOMoney().getDouble("wn2mnyPaid")  + wn2mnyPaid);
                    order.getOMoney().put("wn2mnyReceive",order.getOMoney().getDouble("wn2mnyReceive")  + wn2mnyPaid);
                    orderUpdate.set("oMoney",order.getOMoney());
                    if (!ObjectUtils.isEmpty(orderUpdate.getUpdateObject())) {
                        mongoTemplate.updateFirst(orderCondition,orderUpdate,Order.class);

                        JSONObject moneyflow = new JSONObject();
                        moneyflow.put("subtype",1);moneyflow.put("grpA",3);
                        JSONObject wrddesc =  new JSONObject();
                        wrddesc.put("cn","支付订单余额");
                        moneyflow.put("bmdPay",1);moneyflow.put("wrddesc",wrddesc);
                        moneyflow.put("id_A",id_A);moneyflow.put("id_O",id_O);
                        moneyflow.put("id_C",id_C);moneyflow.put("id_CB",order.getInfo().getId_C());
                        moneyflow.put("grpU",id_U);moneyflow.put("wn2qtychg",wn2mnyPaid);
                        moneyflow.put("tmd",DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
                        dbUtils.addES(moneyflow,"moneyflow");

                        return retResult.ok(CodeEnum.OK.getCode(),null);

                    }
                }
            }else{
                //余额不足
                throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.MONEY_NOT_HAVE.getCode(), null);

            }


        }else{
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);

        }



        return null;
//        }
//
//            return RetResult.errorJsonResult(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.FORBIDDEN.getCode(), null);

    }

    @Override
    public ApiResponse collectionOrder(String id_C, String id_U,String grp , String listType, String id_O, String id_A, Double wn2mnyReceive) throws IOException {
//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);  //列表
//        reqJson.put("grp", grp);    //组别
//        reqJson.put("authType", "card");//卡片/按钮  card/batch
//        reqJson.put("params", new JSONArray().fluentAdd("oMoney"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))){

        Query orderCondition = new Query(new Criteria("_id").is(id_O));

        Order order = mongoTemplate.findOne(orderCondition, Order.class);


        //对象不能为空，卡片不能为空  入库金额不能大于订单金额
        if (order != null && order.getOMoney() != null &&
                wn2mnyReceive <= order.getOMoney().getDouble("wn2mnyReceive")) {

            JSONObject oMoney =  order.getOMoney();

            Query assetCondition = new Query(new Criteria("_id").is(id_A));//.and("info.lAT").is(1)
            assetCondition.fields().include("info");
            Asset moneyAsset = mongoTemplate.findOne(assetCondition, Asset.class);

            //Asset对象不能为空  type == 1 是资金   wn2qty字段不能等于空
            if (moneyAsset != null && moneyAsset.getInfo().getLAT().equals(1) && moneyAsset.getInfo().getWn2qty() != null) {

                boolean judge = false;

                //1.修改ES列表
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                        //条件1：
                        .must(QueryBuilders.termQuery("id_A", id_A))
                        //条件2
                        .must(QueryBuilders.termQuery("id_C", id_C));
                searchSourceBuilder.query(queryBuilder);

                SearchRequest srb = new SearchRequest("lSAsset");

                srb.source(searchSourceBuilder);

                SearchResponse search = restHighLevelClient.search(srb, RequestOptions.DEFAULT);

                SearchHit hit = search.getHits().getHits()[0];


                UpdateRequest updateRequest = new UpdateRequest();

                hit.getSourceAsMap().put("wn2qty", Double.parseDouble(hit.getSourceAsMap().get("wn2qty").toString()) + wn2mnyReceive);
                hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
                updateRequest.index("lSAsset");
                updateRequest.id(hit.getId());
                updateRequest.doc(hit.getSourceAsMap());

                //2.修改mongdb
//                        Query Condition = new Query(new Criteria("_id").is(id_A).and("info").exists(true));
//                        Condition.fields().include("info");
//                        //查询id_A的对象
//                        Asset asset = mongoTemplate.findOne(Condition, Asset.class);
//                        HashMap<String, Object> assetInfo = (HashMap<String, Object>) asset.getInfo();
                Update update = new Update();
                //原始金额 + 入库金额
                //moneyAsset.getInfo().put("wn2qty", Double.parseDouble(assetInfo.get("wn2qty").toString()) + wn2mnyReceive);
                update.set("info.wn2qty", moneyAsset.getInfo().getWn2qty() + wn2mnyReceive);
                if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                    mongoTemplate.updateFirst(assetCondition, update, Asset.class);
                    //修改ES放在 修改mongdb后面
                    restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

                    judge = true;
                }

                if (judge) {
                    Update orderUpdate = new Update();
                    oMoney.put("wn2mnyReceive", oMoney.getDouble("wn2mnyReceive") - wn2mnyReceive);

                    orderUpdate.set("oMoney", order.getOMoney());
                    if (!ObjectUtils.isEmpty(orderUpdate.getUpdateObject())) {
                        mongoTemplate.updateFirst(orderCondition, orderUpdate, Order.class);

                        //HashMap<String, Object> hashMap = new HashMap<>();
                        JSONObject moneyflow = new JSONObject();
                        moneyflow.put("subtype",2);moneyflow.put("grpA",1);
                        JSONObject wrddesc =  new JSONObject();
                        wrddesc.put("cn","收款订单余额");
                        moneyflow.put("bmdPay",1);moneyflow.put("wrddesc",wrddesc);
                        moneyflow.put("id_A",id_A);moneyflow.put("id_O",id_O);
                        moneyflow.put("id_C",id_C);moneyflow.put("id_CB",order.getInfo().getId_C());
                        moneyflow.put("grpU",id_U);moneyflow.put("wn2qtychg",wn2mnyReceive);
                        moneyflow.put("tmd",DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
                        dbUtils.addES(moneyflow,"moneyflow");
                        return retResult.ok(CodeEnum.OK.getCode(),null);

                    }
                }


            } else {
                throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.ASSET_NOT_FOUND.getCode(), null);

            }

        }else{
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.WN2CASH_ERROR.getCode(), null);

        }


        return  null;

//        }
//
//        return RetResult.errorJsonResult(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.FORBIDDEN.getCode(), null);

    }



    @Override
    public ApiResponse setLog(String id_C, String id_U, String logType, JSONObject data) throws IOException {
        GetIndexRequest request = new GetIndexRequest(logType);

        //判断索引是否存在
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);

        if (exists){

            dbUtils.addES(data,logType);

            return retResult.ok(CodeEnum.OK.getCode(),null);

        }

        throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.INDEXES_NO_HAVE.getCode(), null);
    }


    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse rootToPrntC(String id_U, String uid, String id_C,String ref) {

        Query query = new Query(
                new Criteria("info.id_C").is(id_C)
                        .and("info.ref").is("a-auth"));
        query.fields().include("def.id_UM");
        Asset one = mongoTemplate.findOne(query, Asset.class);
        if (one != null && one.getDef().get("id_UM").equals(id_U)) {
            //1.改id_UM
            mongoTemplate.updateFirst(query, new Update().set("def.id_UM", uid), Asset.class);

            //2.把uid添加完所有模块  rolex
            Query authQ = new Query(
                    new Criteria("info.id_C").is(id_C)
                            .and("info.ref").is("a-module"));
            authQ.fields().include("control");
            Asset asset = mongoTemplate.findOne(authQ, Asset.class);

            JSONArray objDataArray =  asset.getControl().getJSONArray("objData");

            JSONArray objMod = new JSONArray();

            for (int i = 0; i < objDataArray.size(); i++) {

                JSONObject indexObj =  objDataArray.getJSONObject(i);

                JSONArray usersArray = indexObj.getJSONArray("id_U");
                //添加受赠人
                usersArray.add(uid);
                //删除赠送人
                usersArray.remove(id_U);

                //rolex.objComp.objMod.module
                JSONObject module = new JSONObject(4);
                module.put("bcdState", indexObj.get("bcdState"));
                module.put("tfin", indexObj.get("tfin"));
                module.put("bcdLevel", indexObj.get("bcdLevel"));
                module.put("ref", indexObj.get("ref"));
                objMod.add(module);
            }

            mongoTemplate.updateFirst(authQ, new Update().set("control.objData", objDataArray), Asset.class);

            //设置受赠人成管理员
            addRolex(objMod,id_C,ref,uid);
            //清除赠送人管理员身份
            mongoTemplate.updateFirst(new Query(
                    new Criteria("_id").is(id_U)), new Update().unset("rolex.objComp."+id_C), User.class);

            return retResult.ok(CodeEnum.OK.getCode(),null);

        }

        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PR_NO_CHARGE_USER.getCode(), null);


    }

    @Override
    public ApiResponse scriptEngine(String script, Map<String, Object> map) {
        System.out.println("script=" + script);
        System.out.println("map=" + map);
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
            Compilable compilable = (Compilable) engine;
            Bindings bindings = engine.createBindings(); //Local级别的Binding
            //定义函数并调用
            //String script = "function add(op1,op2){return op1+op2} add(a, b)";
            //解析编译脚本函数  //通过Bindings加入参数
            CompiledScript JSFunction = compilable.compile(script);

            for(Map.Entry<String,Object> entry:map.entrySet()){
                bindings.put(entry.getKey(),entry.getValue());
            }
            //调用缓存着的脚本函数对象，Bindings作为参数容器传入
            Object result = JSFunction.eval(bindings);
            System.out.println(result);
            return retResult.ok(CodeEnum.OK.getCode(),result);
        }catch (ScriptException e) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.TYPE_ERROR.getCode(), null);
        }


    }


    private void addRolex(JSONArray objMod,String id_C,String ref,String id_U){



        JSONObject rolex = new JSONObject(4);
        rolex.put("objMod",objMod);
        rolex.put("id_C",id_C);
        rolex.put("grpU","1000");
        rolex.put("ref",ref);

        mongoTemplate.updateFirst(new Query(new Criteria("_id").is(id_U)), new Update().set("rolex.objComp."+id_C, rolex), User.class);

    }





}
