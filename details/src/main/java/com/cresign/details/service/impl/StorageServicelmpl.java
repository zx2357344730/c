package com.cresign.details.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.details.client.WSClient;
import com.cresign.details.enumeration.DetailsEnum;
import com.cresign.details.service.StorageService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.es.lSAsset;
import com.cresign.tools.pojo.es.lSBOrder;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.assetCard.AssetAStock;
import com.cresign.tools.pojo.po.assetCard.AssetInfo;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import com.cresign.tools.pojo.po.prodCard.ProdInfo;
import com.mongodb.client.result.UpdateResult;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static java.lang.Math.abs;

@Service
public class StorageServicelmpl implements StorageService {


    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Autowired
    private CoupaUtil coupaUtil;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RetResult retResult;

    @Autowired
    private WSClient wsClient;
//    @Autowired
//    private AuthCheck authCheck;

    DecimalFormat decimalFormat = new DecimalFormat("#.000");



//    @Override
//    public ApiResponse goodsAllocation(String id_C, String id_U, String grp, String listType, String id_O, JSONArray assetObject) throws IOException {
//
////        JSONObject reqJson = new JSONObject();
////        reqJson.put("id_U", id_U);
////        reqJson.put("id_C", id_C);
////        reqJson.put("listType", "lBOrder");
////        reqJson.put("grp", grp);
////        reqJson.put("authType", "card");//卡片/按钮  card/batch
////        reqJson.put("params", new JSONArray().fluentAdd("oStock"));//卡片名称/按钮名称
////        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
////        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
////        if ("200".equals(authModuleJson.getString("code"))){
//
//
//        for (int i = 0; i < assetObject.size(); i++) {
//
//            //HashMap<String,Object> idexObj = (HashMap<String, Object>) assetObject.get(i);
//            JSONObject idexObj = assetObject.getJSONObject(i);
//
//            //判断一个字符串是否是数字,并转换成double
//            Object idexObjWn2qty = detailsUtils.isNumber(idexObj.getString("wn2qty"));
//
//            if (idexObjWn2qty.equals(false)){
//
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);
//
//            }
//
//
//            SearchRequest searchRequest = new SearchRequest(listType);
//            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//
//            QueryBuilder queryBuilder1 = QueryBuilders.boolQuery()
//                    //条件1：
//                    .must(QueryBuilders.termQuery("id_A", idexObj.get("id_A")))
//                    //条件2
//                    .must(QueryBuilders.termQuery("id_C",id_C));
//            sourceBuilder.query(queryBuilder1);
//
//            searchRequest.source(sourceBuilder);
//
//            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//
//
//            //lSAsset仓库数量减少
//
//            SearchHit hit = search.getHits().getHits()[0];
//            UpdateRequest updateRequest = new UpdateRequest();
//
//            //判断一个字符串是否是数字,并转换成double
//            Object number = detailsUtils.isNumber(hit.getSourceAsMap().get("wn2qty").toString());
//
//            if (number.equals(false)){
//
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);
//
//            }
//
//
//
//            hit.getSourceAsMap().put("wn2qty",Double.parseDouble(number.toString()) - Double.parseDouble(idexObjWn2qty.toString()));
//            hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//            updateRequest.index(listType);
//            updateRequest.id( hit.getId());
//            updateRequest.doc(hit.getSourceAsMap());
//
//            //查找条件  lAT == 2(库存)
//            Query assetCondition = new Query(new Criteria("_id").is(idexObj.get("id_A"))
//                    .and("info").exists(true).and("info.lAT").is(2));
//            assetCondition.fields().include("info");
//            //Asset仓库数量减少
//            Asset asset = mongoTemplate.findOne(assetCondition, Asset.class);
//
//            if (asset == null){
//
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_NOT_FOUND.getCode(), null);
//
//            }
//
//            //Map<String, Object> info =  asset.getInfo();
//            AssetInfo info =  asset.getInfo();
//            //判断一个字符串是否是数字,并转换成double
//            Object infoWn2qty = detailsUtils.isNumber(info.get.toString());
//
//            if (infoWn2qty.equals(false)){
//
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);
//
//            }
//
//            info.put("wn2qty",Double.parseDouble(infoWn2qty.toString()) - Double.parseDouble(idexObjWn2qty.toString()));
//            info.put("tmd",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//            Update update = new Update();
//            update.set("info",info);
//            if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//                mongoTemplate.updateFirst(assetCondition,update, Asset.class);
//                //ES的修改放在判断info字段后面（怕info没有这个字段报错，ES改了，所以放在判断后面）
//                //放在mongdb后面，它成功了我才改，它失败了能回滚，也不会执行修改ES代码
//                restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
//
//                JSONObject assetflow = new JSONObject();
//
//                assetflow.put("wn2qtychg",Double.parseDouble(idexObjWn2qty.toString()));
//                assetflow.put("subtype",3);assetflow.put("id_to",asset.getId());assetflow.put("id_from","");
//                assetflow.put("id_P",info.get("id_P"));assetflow.put("id_O",id_O);
//                assetflow.put("id_C",id_C);assetflow.put("ref",info.get("ref"));
//                assetflow.put("wrdN",info.get("wrdN"));assetflow.put("pic",info.get("pic"));
//                assetflow.put("wrddesc",info.get("wrddesc"));
//                assetflow.put("wn4price",Double.parseDouble(info.get("wn4price").toString()));
//                assetflow.put("wn2qc",info.get("wn2qc"));//仓库位置（没具体弄 废弃）  hashMap.put("refSpace", info.get("refSpace"));
//                assetflow.put("grpU","");assetflow.put("grpUB",id_U);
//                assetflow.put("tmk",DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//                assetflow.put("tmd",DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//
//                dbUtils.addES(assetflow,"assetflow");
//
//            }
//
//
//            Query orderCondition = new Query(new Criteria("_id").is(id_O));
//
//            orderCondition.fields().include("oStock");
//
//            Order order = mongoTemplate.findOne(orderCondition, Order.class);
//
//            Update orderUpdate = new Update();
//
//            JSONObject oStock = new JSONObject();
//
//            JSONArray objAsset = new JSONArray();
//            JSONObject idexAsset = new JSONObject();
//
//            idexAsset.put("id_P",info.get("id_P"));
//            idexAsset.put("wn2qtynow",idexObjWn2qty);
//            idexAsset.put("wn4price",info.get("wn4price"));
//            idexAsset.put("wn2qc",info.get("wn2qc"));
//            //idexAsset.put("pic",info.get("pic"));
//            //idexAsset.put("ref",info.get("ref"));
//            //idexAsset.put("wrddesc",info.get("wrddesc"));
//            //idexAsset.put("wrdN",info.get("wrdN"));
//            objAsset.add(idexAsset);
//
//            boolean judge = true;
//            //1.没有这卡片，新增一个
//            if (order.getOStock() == null){
//                oStock.put("objAsset",objAsset);
//                orderUpdate.set("oStock",oStock);
//
//            }else{
//
//                JSONArray arrayArray = order.getOStock().getJSONArray("objAsset");
//                for (int j = 0; j < arrayArray.size(); j++) {
//                    JSONObject idexObjAsset =  arrayArray.getJSONObject(j);
//                    //2.有这张卡片，但是id_P一样，只修改数量
//                    if (idexObjAsset.get("id_P").equals(info.get("id_P"))){
//                        idexObjAsset.put("wn2qtynow",idexObjAsset.getDouble("wn2qtynow") + Double.parseDouble(idexObjWn2qty.toString()));
//                        orderUpdate.set("oStock",order.getOStock());
//                        judge = false;
//
//                    }
//
//                }
//                //3.有这张卡片，但是id_P不一样，
//                if (judge){
//                    orderUpdate.push("oStock.objAsset",idexAsset);
//                }
//
//            }
//
//            if (!ObjectUtils.isEmpty(orderUpdate.getUpdateObject())) {
//                mongoTemplate.updateFirst(orderCondition,orderUpdate,Order.class);
//            }
//
//        }
//
//        return retResult.ok(CodeEnum.OK.getCode(),null);
//    }
//
////        return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.FORBIDDEN.getCode(), null);
////
////    }
//
//    @Override
//    public ApiResponse goodsWarehousing(String id_C, String id_U,String grp, String listType, String id_O, String id_P, String id_A, String wn2qty) throws IOException {
////        JSONObject reqJson = new JSONObject();
////        reqJson.put("id_U", id_U);
////        reqJson.put("id_C", id_C);
////        reqJson.put("listType", listType);  //列表
////        reqJson.put("grp", grp);    //组别
////        reqJson.put("authType", "card");//卡片/按钮  card/batch
////        reqJson.put("params", new JSONArray().fluentAdd("oStock"));//卡片名称/按钮名称
////        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
////        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
////        if ("200".equals(authModuleJson.getString("code"))){
//
//
//        //判断一个字符串是否是数字,并转换成double(前端给的是String,要转换)
//        Object number = detailsUtils.isNumber(wn2qty);
//
//        if (number.equals(false)){
//
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);
//
//        }
//
//        Query orderCondition = new Query(new Criteria("_id").is(id_O));
//        orderCondition.fields().include("info").include("oStock");
//        Order order = mongoTemplate.findOne(orderCondition, Order.class);
//
//        if (order == null){
//
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_NOT_FOUND.getCode(), null);
//
//        }
//
//        JSONArray objAsset = order.getOStock().getJSONArray("objAsset");
//        for (int i = 0; i < objAsset.size(); i++) {
//
//            JSONObject idexObj =  objAsset.getJSONObject(i);
//
//            if (idexObj.get("id_P").equals(id_P)){
//                //先拿出来，要不然会覆盖
//                double  primaryWn2qty = idexObj.getDouble("wn2qtynow");
//                boolean judge = false;
//                //从oStock的物料放回仓库，如果仓库里没有这个物料，新增一个Asset
//                if (id_A.equals("")){
//                    //入库方为  id_CB的话  他没这个产品查询会为空，需要产品的图片名称描述，
//                    //拿订单的id_C,拿卖方的公司id
//                    Map<String, Object> map = detailsUtils.querylSProd(order.getInfo().getId_C(), id_P, "lSProd");
//
//                    if (map == null){
//
//                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.LIST_OBJECT_NO_HAVE.getCode(), null);
//
//                    }
//
//
//                    JSONObject data = new JSONObject();
//                    //给Asset.view
//                    JSONArray assetList = new JSONArray();
//                    assetList.add("info");
//
//
//                    JSONObject info = new JSONObject();
//
//                    info.put("pic",map.get("pic"));
//                    info.put("wrdN",map.get("wrdN"));
//                    info.put("wrddesc",map.get("wrddesc"));
//                    info.put("grp",grp);
//                    info.put("id_C",id_C);
//                    info.put("lAT",2);
//                    info.put("wn2qty", number);
//                    info.put("wn2qc", idexObj.get("wn2qc"));
//                    info.put("id_P", idexObj.get("id_P"));
//                    info.put("wn4price", idexObj.get("wn4price"));
//                    //info.put("id_O",id_O);
//
//                    //info.put("refSpace",new ArrayList<>());
//                    data.put("info",info);
//                    data.put("view", assetList);
//
//                    //生成objectid
//                    String id = MongoUtils.GetObjectId();
//
//                    String ref = singleService.refAuto(id_C, listType, grp);
//                    //调用
//                    JSONObject jsonObject1 =singleService.addlSAsset(id_C,id_U, id ,ref, JSON.toJSONString(data));
//                    if (jsonObject1.get("boolean").equals("true")){
//                        judge = true;
//                    }else if (jsonObject1.get("boolean").equals("false") && jsonObject1.get("reason").equals("comp对象为空")){
//                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
//
//                    }else{
//                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(), null);
//
//                    }
//
//
//                }else{
//                    //修改id_A的数量
//                    //1.修改ES列表
//                    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//                    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                            //条件1：
//                            .must(QueryBuilders.termQuery("id_A", id_A))
//                            //条件2
//                            .must(QueryBuilders.termQuery("id_C",id_C));
//                    searchSourceBuilder.query(queryBuilder);
//                    SearchRequest srb = new SearchRequest(listType);
//
//                    srb.source(searchSourceBuilder);
//
//                    SearchResponse search = restHighLevelClient.search(srb, RequestOptions.DEFAULT);
//                    SearchHit hit = search.getHits().getHits()[0];
//
//
//                    UpdateRequest updateRequest = new UpdateRequest();
//
//                    //判断一个字符串是否是数字,并转换成double
//                    Object numberES = detailsUtils.isNumber(hit.getSourceAsMap().get("wn2qty").toString());
//
//                    if (numberES.equals(false)){
//
//                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);
//
//                    }
//
//                    hit.getSourceAsMap().put("wn2qty", Double.parseDouble(numberES.toString()) +  Double.parseDouble(number.toString()));
//                    hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//                    updateRequest.index(listType);
//                    updateRequest.id( hit.getId());
//                    updateRequest.doc(hit.getSourceAsMap());
//
//
//
//
//                    //2.修改mongdb  查找条件  lAT == 2(库存)
//                    Query condition = new Query(new Criteria("_id").is(id_A)
//                            .and("info").exists(true).and("info.lAT").is(2));
//                    condition.fields().include("info");
//                    //Asset仓库数量减少
//                    Asset asset = mongoTemplate.findOne(condition, Asset.class);
//
//                    if (asset == null){
//
//                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_NOT_FOUND.getCode(), null);
//
//                    }
//
//                    JSONObject assetInfo =  asset.getInfo();
//                    Update update = new Update();
//
//                    //判断一个字符串是否是数字,并转换成double
//                    Object infoNumber = detailsUtils.isNumber(assetInfo.get("wn2qty").toString());
//
//                    if (infoNumber.equals(false)){
//
//                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);
//
//                    }
//
//
//
//                    //原始数量 + 入库数量
//                    assetInfo.put("wn2qty", Double.parseDouble(infoNumber.toString())  + Double.parseDouble(number.toString()));
//
//                    update.set("info", assetInfo);
//                    if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//                        mongoTemplate.updateFirst(condition, update, Asset.class);
//                        //ES的修改放在判断info字段后面（怕info没有这个字段报错，ES改了，所以放在判断后面）
//                        //放在mongdb后面，它成功了我才改，它失败了能回滚，也不会执行修改ES代码
//                        restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
//
////                        JSONObject assetflow = new JSONObject();
////                        assetflow.put("wn2qtychg",number);
////                        assetflow.put("subtype",5);assetflow.put("id_to",asset.getId());assetflow.put("id_from","");
////                        assetflow.put("id_P",assetInfo.get("id_P"));assetflow.put("id_O",assetInfo.get("id_O"));
////                        assetflow.put("id_C",id_C);assetflow.put("ref",assetInfo.get("ref"));
////                        assetflow.put("wrdN",assetInfo.get("wrdN"));assetflow.put("pic",assetInfo.get("pic"));
////                        assetflow.put("wrddesc",assetInfo.get("wrddesc"));
////                        assetflow.put("wn4price",Double.parseDouble(assetInfo.get("wn4price").toString()));
////                        assetflow.put("wn2qc",assetInfo.get("wn2qc"));//hashMap.put("refSpace", assetInfo.get("refSpace"));
////                        assetflow.put("grpU","");assetflow.put("grpUB",id_U);
////
////                        assetflow.put("tmd",DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//
//                        //commUtils.addES(hashMap,"assetflow");
//
//                        judge = true;
//                    }
//
//                }
//
//                if (judge){
//                    //id_CB和当前传入的公司id一致的话，那就是客户点击入库，客户点击入库相当于卖出产品了，这里是否发ES，产品销售数额
//                    //客户点击的入库会产生费用，费用放在oMoney卡
//                    if (id_C.equals(order.getInfo().getId_CB())){
//                        Update orderUpdate = new Update();
//
//                        JSONObject oMoney = new JSONObject();
//
//                        JSONArray objMoney = new JSONArray();
//
//                        JSONObject idexMoney = new JSONObject();
//                        idexMoney.put("id_P",idexObj.get("id_P"));
//                        idexMoney.put("wn2qtynow",number);
//                        idexMoney.put("wn4price",idexObj.get("wn4price"));
//                        //idexMoney.put("wrdN",idexObj.get("wrdN"));
//
//
//                        boolean oMoneyjudge = true;
//                        //1.没有oMoney这卡片，新增一个
//                        if (order.getOMoney() == null){
//
//                            objMoney.add(idexMoney);
//                            oMoney.put("objMoney",objMoney);
//                            //总价   商品数量*价格
//                            oMoney.put("wn2mnyOwe",Double.parseDouble(number.toString()) * Double.parseDouble(idexObj.get("wn4price").toString()));
//                            //wn2mnyReceive收款
//                            oMoney.put("wn2mnyReceive",0.0);
//                            //wn2mnyPaid支付
//                            oMoney.put("wn2mnyPaid",0.0);
//                            orderUpdate.set("oMoney",oMoney);
//
//                        }else{
//
//                            JSONArray arrayArray =  order.getOMoney().getJSONArray("objMoney");
//                            for (int j = 0; j < arrayArray.size(); j++) {
//                                JSONObject idexObjAsset = arrayArray.getJSONObject(j);
//                                //2.有这张卡片，但是id_P一样，只修改数量
//                                if (idexObjAsset.get("id_P").equals(id_P)){
//                                    //累加数量
//                                    idexObjAsset.put("wn2qtynow",idexObjAsset.getInteger("wn2qtynow") + wn2qty );
//
//                                    //idexObjAsset.put("wn2mnyOwe",Integer.parseInt(idexObjAsset.get("wn2qtynow").toString()) * Double.parseDouble(idexObj.get("wn4price").toString()));
//
//                                    //新入库数量 * 价钱  = 累加价钱
//                                    Double newwn2mnyOwe  =  Double.parseDouble(number.toString()) * Double.parseDouble(idexObj.get("wn4price").toString());
//                                    //应付价钱 + 累加价钱
//                                    order.getOMoney().put("wn2mnyOwe",order.getOMoney().getDouble("wn2mnyOwe") + newwn2mnyOwe);
//                                    orderUpdate.set("oMoney",order.getOMoney());
//                                    oMoneyjudge = false;
//
//                                }
//
//                            }
//                            //3.有这张卡片，但是id_P不一样，
//                            if (oMoneyjudge){
//                                order.getOMoney().put("wn2mnyOwe",Double.parseDouble(number.toString()) * Double.parseDouble(idexObj.get("wn4price").toString()));
//                                arrayArray.add(idexMoney);
//                                orderUpdate.set("oMoney",order.getOMoney());
//                            }
//
//                        }
//
//                        if (!ObjectUtils.isEmpty(orderUpdate.getUpdateObject())) {
//                            mongoTemplate.updateFirst(orderCondition,orderUpdate,Order.class);
//                        }
//
//                    }
//
//
//                    Update update = new Update();
//                    //等于，证明全部入库，订单里这个产品的数据全部删了
//                    if (primaryWn2qty == Double.parseDouble(number.toString())){
//                        objAsset.remove(i);
//                        update.set("oStock.objAsset",objAsset);
//                    }else{
//
//                        //原来数量  -  入库数量
//                        idexObj.put("wn2qtynow",primaryWn2qty - Double.parseDouble(number.toString()));
//                        update.set("oStock.objAsset",objAsset);
//                    }
//                    if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//                        mongoTemplate.updateFirst(orderCondition, update, Order.class);
//                    }
//                }
//
//            }
//
//
//        }
//
//        return retResult.ok(CodeEnum.OK.getCode(),null);
//
//    }

//            return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.FORBIDDEN.getCode(), null);
//
//        }

//    @Override
//    public ApiResponse prodMerge(String id_U, String id_C, String grp, String listType, String id_to, List<String> id_from) throws IOException {

//        JSONObject authObject = new JSONObject();
//        authObject.put("id_U", id_U);
//        authObject.put("id_C", id_C);
//        authObject.put("listType", listType);  //列表
//        authObject.put("grp", grp);    //组别
//        authObject.put("authType", "batch");//卡片/按钮  card/batch
//        authObject.put("params", new JSONArray().fluentAdd("delete"));//卡片名称/按钮名称
//        String authModuleResult = authFilterClient.getUserUpdateAuth(authObject);
//        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);
//        if ("200".equals(authModuleJson.getString("code"))) {

//        Query id_toCondition = new Query(new Criteria("_id").is(id_to).and("info").exists(true));
//        id_toCondition.fields().include("info");
//        //查询id_to的对象
//        Asset id_toAsset = mongoTemplate.findOne(id_toCondition, Asset.class);
//        //判断不为空 && lAT必须是2,2:物品，只有物品才能合并,id_P不能为空
//        if (id_toAsset != null && id_toAsset.getInfo().getLAT().equals(2) && id_toAsset.getInfo().getId_P() != null) {
//
//            //id_toinfo转成对象
//            AssetInfo id_toInfo =  id_toAsset.getInfo();
//            //循环要合并的id
//            for (int i = 0; i < id_from.size(); i++) {
//
//                Query id_fromCondition = new Query(new Criteria("_id").is(id_from.get(i)).and("info").exists(true));
//                id_fromCondition.fields().include("info").include("file00s");
//                //查询id_from的对象
//                Asset id_fromAsset = mongoTemplate.findOne(id_fromCondition, Asset.class);
//
//                //id_fromAsset.getInfo().get("lAT")  必须是2,2代表是物品，只有物品才能合并 id_P不能为空  并且两个的id_P必须一致
//                if (id_fromAsset != null && id_fromAsset.getInfo().getLAT().equals(2) && id_toAsset.getInfo().getId_P() != null
//                        && id_toAsset.getInfo().getId_P().equals(id_fromAsset.getInfo().getId_P())) {
//
//                    //判断有没有file00s卡片
//                    if (id_fromAsset.getFile00s() != null){
//
//                        JSONObject reqJson = new JSONObject();
//                        reqJson.put("id",id_fromAsset.getId());
//                        reqJson.put("id_C",id_C);
//                        reqJson.put("fileList",id_fromAsset.getFile00s().get("objData"));
//                        //调用file服务API
//                        long fileSize = fileClient.delCOSFile(reqJson);
//                        Update update = new Update();
//
//                        //自减,powerup容量卡
//                        update.inc("powerup."+"capacity"+".used",- fileSize);
//
//                        if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//
//                            mongoTemplate.updateFirst(new Query(
//                                    new Criteria("info.id_C").is(id_C)
//                                            .and("info.ref").is("a-module")),update, Asset.class);
//
//                        }
//                    }
//
//
//
//                    //id_frominfo转成对象
//                    AssetInfo id_fromInfo =  id_fromAsset.getInfo();
//                    //重置id_toInfo的数量，拿id_toInfo的数量+id_fromInfo的数量 = 新的数量
//                    id_toInfo.setWn2qty(Double.parseDouble(id_toInfo.getWn2qty().toString())+ Double.parseDouble(id_fromInfo.getWn2qty().toString()));
//                    id_toInfo.setTmd(DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//
//                    //出仓（写ES日志）
//
//
//                    JSONObject id_fromObj = new JSONObject();
//                    id_fromObj.put("wn2qtychg", Double.parseDouble(id_fromInfo.get("wn2qty").toString()));
//                    id_fromObj.put("subtype", 5);
//                    id_fromObj.put("id_to", id_fromAsset.getId());
//                    id_fromObj.put("id_from", id_toAsset.getId());
//                    id_fromObj.put("id_P", id_fromInfo.get("id_P"));
//                    id_fromObj.put("id_O", id_fromInfo.get("id_O"));
//                    id_fromObj.put("id_C", id_C);
//                    id_fromObj.put("ref", id_fromInfo.get("ref"));
//                    id_fromObj.put("wrdN", id_fromInfo.get("wrdN"));
//                    id_fromObj.put("pic", id_fromInfo.get("pic"));
//                    id_fromObj.put("wrddesc", id_fromInfo.get("wrddesc"));
//                    id_fromObj.put("wn4price", Double.parseDouble(id_fromInfo.get("wn4price").toString()));
//                    id_fromObj.put("wn2qc", id_fromInfo.get("wn2qc"));
//                    //id_fromHashMap.put("refSpace", id_fromInfo.get("refSpace"));
//                    id_fromObj.put("grpU", "");
//                    id_fromObj.put("grpUB", id_U);
//                    id_fromObj.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//                    dbUtils.addES(id_fromObj, "assetflow");
//                    //清空已合并的仓库位置
////                            ArrayList refSpaceList = (ArrayList) id_fromHashMap.get("refSpace");
////                            if (refSpaceList.size() > 0) {
////                                commUtils.emptyRefSpace(id_C, refSpaceList);
////                            }
//
//
//                    //进仓（写ES日志）
//
////
//
//                    JSONObject id_toObject = new JSONObject();
//                    id_toObject.put("wn2qtychg", Double.parseDouble(id_fromInfo.get("wn2qty").toString()));
//                    id_toObject.put("subtype", 5);
//                    id_toObject.put("id_to", id_toAsset.getId());
//                    id_toObject.put("id_from", id_fromAsset.getId());
//                    id_toObject.put("id_P", id_toInfo.get("id_P"));
//                    id_toObject.put("id_O", id_toInfo.get("id_O"));
//                    id_toObject.put("id_C", id_C);
//                    id_toObject.put("ref", id_toInfo.get("ref"));
//                    id_toObject.put("wrdN", id_toInfo.get("wrdN"));
//                    id_toObject.put("pic", id_toInfo.get("pic"));
//                    id_toObject.put("wrddesc", id_toInfo.get("wrddesc"));
//                    id_toObject.put("wn4price", Double.parseDouble(id_toInfo.get("wn4price").toString()));
//                    id_toObject.put("wn2qc", id_toInfo.get("wn2qc"));
//                    //id_toHashMap.put("refSpace", id_toInfo.get("refSpace"));
//                    id_toObject.put("grpU", "");
//                    id_toObject.put("grpUB", id_U);
//                    id_toObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//
//                    dbUtils.addES(id_toObject, "assetflow");
//
//                    //删除合并的数据,先删ES，再mongdb
//
//                    // 删除es列表
//                    DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(listType);
//                    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//                    // 查询条件
//                    boolQueryBuilder.must(QueryBuilders.termQuery("id_A", id_fromAsset.getId()));
//                    deleteByQueryRequest.setQuery(boolQueryBuilder);
//
//                    try {
//
//                        restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
//                    } catch (IOException e) {
//                        throw new ErrorResponseException(HttpStatus.OK, CodeEnum.OK.getCode(), null);
//
//                    }
//
//
//                    // 创建查询，并且添加查询条件
//                    Query query = new Query(new Criteria(Constants.CRITERIA__ID).is(id_fromAsset.getId()));
//
//                    // 根据id删除,合并后删除mongdb
//                    mongoTemplate.remove(query, Asset.class);
//
//
//                } else {
//                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.TYPE_ERROR.getCode(), null);
//
//                }
//
//
//            }
//            //1.修改ES列表
//            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                    //条件1：
//                    .must(QueryBuilders.termQuery("id_A", id_to))
//                    //条件2
//                    .must(QueryBuilders.termQuery("id_C", id_C));
//            searchSourceBuilder.query(queryBuilder);
//            SearchRequest srb = new SearchRequest(listType);
//
//            srb.source(searchSourceBuilder);
//
//            SearchResponse search = restHighLevelClient.search(srb, RequestOptions.DEFAULT);
//            if (search.getHits().getHits().length > 0) {
//
//                SearchHit hit = search.getHits().getHits()[0];
//                UpdateRequest updateRequest = new UpdateRequest();
//
//                hit.getSourceAsMap().put("wn2qty", id_toInfo.get("wn2qty"));
//                hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//                updateRequest.index(listType);
//                updateRequest.id(hit.getId());
//                updateRequest.doc(hit.getSourceAsMap());
//
//
//
//                Update update = new Update();
//                update.set("info", id_toInfo);
//                if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//                    mongoTemplate.updateFirst(id_toCondition, update, Asset.class);
//
//                    restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
//
//                }
//
//                return retResult.ok(CodeEnum.OK.getCode(),null);
//
//            }
//
//
//        } else {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.TYPE_ERROR.getCode(), null);
//
//        }
//
//        return null;
//
////        }
////
////
////        return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.FORBIDDEN.getCode(), null);
//    }
//
//    @Override
//    public ApiResponse prodSplit(String id_U, String id_C,String grp, String listType, String id_to, Double wn2qty) throws IOException {
//
//        Query id_toCondition = new Query(new Criteria("_id").is(id_to).and("info").exists(true));
//        id_toCondition.fields().include("info");
//        //查询id_to的对象
//        Asset id_toAsset = mongoTemplate.findOne(id_toCondition, Asset.class);
//        //判断不为空
//        if (id_toAsset != null) {
//            //type必须是2,2=物品，只有物品才能拆分，经过讨论，不一定是物品才可以拆分
//            //if (id_toAsset.getInfo().get("lAT").equals(2)) {
//            //id_toinfo转成对象
//            JSONObject id_toInfo =  id_toAsset.getInfo();
//            //先拿出来，要不然会覆盖
//            double  primaryWn2qty =  Double.parseDouble(id_toInfo.get("wn2qty").toString());
//
//
//            JSONObject data = new JSONObject();
//            //给Asset.view
//
//            JSONArray assetList = new JSONArray();
//            assetList.add("info");
//            data.put("view", assetList);
//            //拆分数量不能大于原始数量
//            if (wn2qty >= Double.parseDouble(id_toInfo.get("wn2qty").toString())){
//
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.WN2QTY_ERROR.getCode(), null);
//
//            }
//            id_toInfo.put("wn2qty", wn2qty);
//            id_toInfo.put("tmk",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//            id_toInfo.put("tmd",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//            data.put("info",id_toInfo);
//
//            //生成objectid
//            String id = MongoUtils.GetObjectId();
//            System.out.println("id = " + id);
//            String ref = singleService.refAuto(id_C, listType, grp);
//
//            //调用
//            JSONObject jsonObject1 = singleService.addlSAsset(id_C,id_U, id ,ref,JSON.toJSONString(data));
//
//            if(jsonObject1.get("boolean").equals("true")){
//                //1.修改ES列表
//                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//                QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                        //条件1：
//                        .must(QueryBuilders.termQuery("id_A", id_to))
//                        //条件2
//                        .must(QueryBuilders.termQuery("id_C",id_C));
//                searchSourceBuilder.query(queryBuilder);
//                SearchRequest srb = new SearchRequest(listType);
//
//                srb.source(searchSourceBuilder);
//
//                SearchResponse search = restHighLevelClient.search(srb, RequestOptions.DEFAULT);
//
//                SearchHit hit = search.getHits().getHits()[0];
//                UpdateRequest updateRequest = new UpdateRequest();
//
//                hit.getSourceAsMap().put("wn2qty", primaryWn2qty  - wn2qty);
//                hit.getSourceAsMap().put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//                updateRequest.index(listType);
//                updateRequest.id( hit.getId());
//                updateRequest.doc(hit.getSourceAsMap());
//
//
//
//                //2.修改mongdb
//                Update update = new Update();
//                //原始数量 - 拆分数量
//                id_toInfo.put("wn2qty", primaryWn2qty  - wn2qty);
//                update.set("info", id_toInfo);
//                if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//                    mongoTemplate.updateFirst(id_toCondition, update, Asset.class);
//                    restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
//
//                    JSONObject assetflow = new JSONObject();
//                    assetflow.put("wn2qtychg",wn2qty);
//                    assetflow.put("subtype",5);assetflow.put("id_to",id_toAsset.getId());assetflow.put("id_from",id);
//                    assetflow.put("id_P",id_toInfo.get("id_P"));assetflow.put("id_O",id_toInfo.get("id_O"));
//                    assetflow.put("id_C",id_C);assetflow.put("ref",id_toInfo.get("ref"));
//                    assetflow.put("wrdN",id_toInfo.get("wrdN"));assetflow.put("pic",id_toInfo.get("pic"));
//                    assetflow.put("wrddesc",id_toInfo.get("wrddesc"));
//                    assetflow.put("wn4price",Double.parseDouble(id_toInfo.get("wn4price").toString()));
//                    assetflow.put("wn2qc",id_toInfo.get("wn2qc"));//hashMap.put("refSpace", id_toInfo.get("refSpace"));
//                    assetflow.put("grpU","");assetflow.put("grpUB",id_U);
//                    assetflow.put("tmd",DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//
//                    dbUtils.addES(assetflow,"assetflow");
//                    return retResult.ok(CodeEnum.OK.getCode(),null);
//
//                }
//
//            }else if (jsonObject1.get("boolean").equals("false") && jsonObject1.get("reason").equals("comp对象为空")){
//
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
//
//            }else{
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(), null);
//
//            }
//
//        }else{
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_NOT_FOUND.getCode(), null);
//
//        }
//
//        return  null;
//    }
//
//    @Override
//    @Transactional(noRollbackFor = ResponseException.class)
//    public ApiResponse batchQtySafe(String id_C, String id_U, String listType) throws IOException {
//
//        //查询条件
//        Query query = new Query(new Criteria("qtySafe").exists(true));
//        query.fields().include("info").include("qtySafe");
//        List<Prod> prods = mongoTemplate.find(query, Prod.class);
//        for (int i = 0; i < prods.size(); i++) {
//            //不等于空
//            if (prods.get(i).getQtySafe() != null){
//
//                //bisWarning == false  lAT = 2  wn2qty != null
//                if (prods.get(i).getQtySafe().get("bisWarning").equals(false) ){
//
//                    //构建查询库
//                    SearchRequest searchRequest = new SearchRequest("lSAsset");
//                    //构建搜索条件
//                    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//                    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                            //条件1：
//                            .must(QueryBuilders.termQuery("id_P", prods.get(i).getId()))
//                            //条件2
//                            .must(QueryBuilders.termQuery("id_C",id_C));
//                    searchSourceBuilder.query(queryBuilder);
//
//                    searchSourceBuilder.from(0);
//                    searchSourceBuilder.size(10000);
//
//                    searchRequest.source(searchSourceBuilder);
//                    SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//
//                    double wn2qty = 0.0;
//
//                    for (SearchHit hit : search.getHits().getHits()) {
//
//                        wn2qty += Double.parseDouble(hit.getSourceAsMap().get("wn2qty").toString());
//
//                    }
//
//
//                    //条件满足，发ES，预订数量大于现在数量
//                    if (Double.parseDouble(prods.get(i).getQtySafe().get("wn2qtyMin").toString()) > wn2qty){
//
//
//                        Update update = new Update();
//                        update.set("qtySafe.bisWarning",true);
//
//                        if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
//                            mongoTemplate.updateFirst(new Query(new Criteria("_id").is(prods.get(i).getId())), update, Prod.class);
//                        }
//
//
//                        JSONObject assetflow = new JSONObject();
//                        assetflow.put("wn2qtychg",Double.parseDouble(prods.get(i).getQtySafe().get("wn2qtyBuy").toString()));
//                        assetflow.put("subtype",6);assetflow.put("id_to","");assetflow.put("id_from","");
//                        assetflow.put("id_P",prods.get(i).getId());assetflow.put("id_O","");
//                        assetflow.put("id_C",prods.get(i).getInfo().get("id_C"));assetflow.put("ref",prods.get(i).getInfo().get("ref"));
//                        assetflow.put("wrdN",prods.get(i).getInfo().get("wrdN"));assetflow.put("pic",prods.get(i).getInfo().get("pic"));
//                        assetflow.put("wrddesc",prods.get(i).getInfo().get("wrddesc"));
//                        assetflow.put("wn4price","");//Double.parseDouble(prods.get(i).getInfo().get("wn4price").toString())
//                        assetflow.put("wn2qc",prods.get(i).getInfo().get("wn2qc"));//hashMap.put("batchQtySafe", "");
//                        assetflow.put("grpU","");assetflow.put("grpUB","");
//                        assetflow.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//                        assetflow.put("tmd",DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//
//                        dbUtils.addES(assetflow,"assetflow");
//
//                        return retResult.ok(CodeEnum.OK.getCode(),null);
//
//                    }
//
//                }
//
//            }
//
//        }
//
//        return null;
//
//    }
//
//
//
//    @Override
//    public ApiResponse addOrder(String id_U, String id_C,String grp,HashMap<String, Object> reqJson, String listType, String data) throws IOException {
//
//        authCheck.getUserUpdateAuth(id_U,id_C,listType,grp,"batch",new JSONArray().fluentAdd("add"));
//
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                //条件1：
//                .must(QueryBuilders.termQuery("id_P", reqJson.get("id_P")))
//                //条件2
//                .must(QueryBuilders.termQuery("id_CB", id_C));
//        searchSourceBuilder.query(queryBuilder);
//        SearchRequest srb = new SearchRequest(listType);
//
//        srb.source(searchSourceBuilder);
//
//        SearchResponse search = restHighLevelClient.search(srb, RequestOptions.DEFAULT);
//
//
//        for (SearchHit hit : search.getHits().getHits()) {
//            //数量与单价
//            hit.getSourceAsMap().put("wn2qtyneed", reqJson.get("wn2qtychg"));
//            hit.getSourceAsMap().put("wn2mnyprice", reqJson.get("wn4price"));
//            //HashMap<String,Object> hitObj = new HashMap<>();
//
//            JSONArray objItem = new JSONArray();
//            objItem.add(hit.getSourceAsMap());
//
//            JSONObject oItem = new JSONObject();
//            oItem.put("objItem", objItem);
//
//            JSONObject objData = JSONObject.parseObject(data);
//            objData.put("oItem", oItem);
//            //生成objectid
//            String id = MongoUtils.GetObjectId();
//            //最后两边都确认final成功了再添加编号，现在就给空编号
//            //String ref = singleService.refAuto(id_C, listType, grp);
//            //调用
//            JSONObject jsonObject1 = singleService.addlSOrder(id_C, id_U, id, "", JSON.toJSONString(objData));
//
//            if (jsonObject1.get("boolean").equals("true")) {
//                return retResult.ok(CodeEnum.OK.getCode(), jsonObject1.get("reason"));
//
//            } else if (jsonObject1.get("boolean").equals("false") && jsonObject1.get("reason").equals("comp对象为空")) {
//
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
//
//            } else {
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(), null);
//
//            }
//        }
//        return retResult.ok(CodeEnum.OK.getCode(),"");
//    }


    @Override
    public ApiResponse getWarehouse(String id_C) {
        String id_A = dbUtils.getId_A(id_C, "a-auth");
        Asset asset = (Asset) dbUtils.getMongoOneField(id_A, "locSetup.objWH", Asset.class);

        JSONArray arrayObjWH = asset.getLocSetup().getJSONArray("objWH");
        System.out.println("arrayObjWH=" + arrayObjWH);
        if (arrayObjWH != null) {
            return retResult.ok(CodeEnum.OK.getCode(), arrayObjWH);
        }
        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
    }

    @Override
    public ApiResponse getArea(String id_C, String ref) {
        String id_A = dbUtils.getId_A(id_C, "a-auth");
        Asset asset = (Asset) dbUtils.getMongoOneField(id_A, "locSetup.locWH", Asset.class);
        JSONObject jsonLocWH = asset.getLocSetup().getJSONObject("locWH").getJSONObject(ref);
        System.out.println("jsonLocWH=" + jsonLocWH);
        if (jsonLocWH != null) {
            return retResult.ok(CodeEnum.OK.getCode(), jsonLocWH);
        }
        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
    }

//    //根据公司id和货架名获取货架非空格子
//    private JSONObject getNotEmptyLocSpace(String id_C, String locAddr) throws IOException {
//        JSONObject jsonQuery = new JSONObject();
//        jsonQuery.put("id_C", id_C);
//        jsonQuery.put("locAddr.keyword", locAddr);
//        SearchResponse response = dbUtils.getEsQuery(jsonQuery, "lSAsset");
//        SearchHit[] hits = response.getHits().getHits();
//        JSONObject jsonLoc = new JSONObject();
//        JSONArray arrayLocSpace = new JSONArray();
//        JSONArray arraySpaceQty = new JSONArray();
//        //获取货架的非空格子
//        for (SearchHit hit : hits) {
//            lSAsset lsasset = JSON.parseObject(hit.getSourceAsString(), lSAsset.class);
//            arrayLocSpace.addAll(lSAsset.getLocSpace());
//            arraySpaceQty.addAll(lSAsset.getSpaceQty());
//        }
//        jsonLoc.put("locSpace", arrayLocSpace);
//        jsonLoc.put("spaceQty", arraySpaceQty);
//        return jsonLoc;
//    }

    @Override
    public ApiResponse getLocByRef(String id_C, String locAddr) throws IOException {
        JSONObject jsonQuery = new JSONObject();
        jsonQuery.put("id_C", id_C);
        jsonQuery.put("locAddr.keyword", locAddr);
        SearchResponse response = dbUtils.getEsKeys(jsonQuery, "lSAsset");
        SearchHit[] hits = response.getHits().getHits();
        if (hits.length == 0) {
            return retResult.ok(CodeEnum.OK.getCode(), new JSONArray());
        }
        JSONArray arrayLoc = new JSONArray();
        //获取货架的非空格子
        for (SearchHit hit : hits) {
            lSAsset lsasset = JSON.parseObject(hit.getSourceAsString(), lSAsset.class);
            JSONArray arrayLocSpace = lsasset.getLocSpace();
            JSONArray arraySpaceQty = lsasset.getSpaceQty();
            String id_P = lsasset.getId_P();
            for (int i = 0; i < arrayLocSpace.size(); i++) {
                JSONObject jsonLoc = new JSONObject();
                jsonLoc.put("locSpace", arrayLocSpace.getInteger(i));
                jsonLoc.put("spaceQty", arraySpaceQty.getDouble(i));
                jsonLoc.put("id_P", id_P);
                jsonLoc.put("wrdN", lsasset.getWrdN());
                arrayLoc.add(jsonLoc);
            }
        }
        System.out.println("arrayLoc=" + arrayLoc);
        //排序
        int min;
        JSONObject jsonLoc;
        for (int i = 0; i < arrayLoc.size() - 1; i++) {
            min = i;
            for (int j = i + 1; j < arrayLoc.size(); j++) {
                if (arrayLoc.getJSONObject(min).getInteger("locSpace") > arrayLoc.getJSONObject(j).getInteger("locSpace")) {
                    min = j;
                }
            }
            jsonLoc = arrayLoc.getJSONObject(i);
            arrayLoc.set(i, arrayLoc.getJSONObject(min));
            arrayLoc.set(min, jsonLoc);
        }
        System.out.println("arrayLoc=" + arrayLoc);
        if (arrayLoc.size() > 0) {
            return retResult.ok(CodeEnum.OK.getCode(), arrayLoc);
        }
        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
    }

    @Override
    public ApiResponse getLocByRefEmpty(String id_C, String locAddr) throws IOException {
        JSONObject jsonQuery = new JSONObject();
        jsonQuery.put("id_C", id_C);
        jsonQuery.put("locAddr.keyword", locAddr);
        SearchResponse response = dbUtils.getEsKeys(jsonQuery, "lSAsset");
        SearchHit[] hits = response.getHits().getHits();
        JSONArray arrayLocSpace = new JSONArray();

        //获取货架格子数
        String[] locSplit = locAddr.split("##");
        String field = "locSetup.locWH." + locSplit[0] + ".locArea." + locSplit[1] + ".locRack." + locSplit[2];
        String id_A = dbUtils.getId_A(id_C, "a-auth");
        Asset asset = (Asset) dbUtils.getMongoOneField(id_A, field, Asset.class);
        Integer spaceCount = asset.getLocSetup().getJSONObject("locWH").getJSONObject(locSplit[0]).getJSONObject("locArea")
                .getJSONObject(locSplit[1]).getJSONObject("locRack").getJSONObject(locSplit[2]).getInteger("spaceCount");
        System.out.println("spaceCount=" + spaceCount);
        System.out.println(arrayLocSpace);
        JSONObject emptyBox = new JSONObject();
        emptyBox.put("wn2qtyBox", 0.0);

        //init all boxes into 0.0
        for (int i = 0; i < spaceCount; i++) {
                arrayLocSpace.set(i, emptyBox);
        }

        //获取货架的非空格子
        for (SearchHit hit : hits) {
            lSAsset lsasset = JSON.parseObject(hit.getSourceAsString(), lSAsset.class);

            for (int j = 0; j < lsasset.getLocSpace().size(); j++)
            {
                //flatten the whole locSpace spaces into either null or have data
                Integer locIndex = lsasset.getLocSpace().getInteger(j);
                arrayLocSpace.set(locIndex, JSONObject.parseObject(JSON.toJSONString(lsasset)));
                arrayLocSpace.getJSONObject(locIndex).put("wn2qtyBox", lsasset.getSpaceQty().getDouble(j));
            }
        }


//        Boolean bool;
////        JSONArray arrayLocSpaceEmpty = new JSONArray();
////        for (int i = 0; i < spaceCount; i++) {
////
////            if (!arrayLocSpace.contains(i))
////            {
////                arrayLocSpaceEmpty.add(i);
////            }
////
////            bool = true;
////            for (int j = 0; j < arrayLocSpace.size(); j++) {
////                if (i == arrayLocSpace.getInteger(j)) {
////                    bool = false;
////                }
////            }
////            if (bool) {
////                arrayLocSpaceEmpty.add(i);
////            }
////        }
//        System.out.println("arrayLocSpaceEmpty=" + arrayLocSpaceEmpty);
        if (arrayLocSpace.size() > 0) {
            return retResult.ok(CodeEnum.OK.getCode(), arrayLocSpace);
        }
        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
    }

    @Override
    public ApiResponse getLocName(String id_C) {
        String field = "locSetup.locWH";
        String id_A = dbUtils.getId_A(id_C, "a-auth");
        Asset asset = (Asset) dbUtils.getMongoOneField(id_A, field, Asset.class);
        System.out.println("asset=" + asset);
        if (asset.getLocSetup() == null
                || asset.getLocSetup().getJSONObject("locWH") == null) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
        }

        JSONObject jsonLocWH = asset.getLocSetup().getJSONObject("locWH");
        jsonLocWH.forEach((k1, v1) -> {
            JSONObject jsonWH = jsonLocWH.getJSONObject(k1);
            jsonWH.remove("ref");
            JSONObject jsonLocArea = jsonWH.getJSONObject("locArea");
            jsonLocArea.forEach((k2, v2) -> {
                JSONObject jsonArea = jsonLocArea.getJSONObject(k2);
                jsonArea.remove("refArea");
                jsonArea.remove("locRack");
            });
        });
        System.out.println("jsonLocWH=" + jsonLocWH);
        return retResult.ok(CodeEnum.OK.getCode(), jsonLocWH);
    }

    //@Override
    private JSONObject getLocSpace(String id_C, String id_P, String locAddr, JSONArray locSpace) throws IOException {
        System.out.println("c=" + id_C + ",p=" + id_P + ",la=" + locAddr + ",ls=" + locSpace);
        BoolQueryBuilder shouldQuery = new BoolQueryBuilder();
        locSpace.forEach(s -> shouldQuery.should(QueryBuilders.termQuery("locSpace", s)));
        BoolQueryBuilder mustQuery = new BoolQueryBuilder();
        mustQuery.must(QueryBuilders.termQuery("id_C", id_C)).must(QueryBuilders.termQuery("locAddr.keyword", locAddr)).must(shouldQuery);
        SearchResponse fromResponse = dbUtils.getEsQuery(mustQuery, "lSAsset");

        SearchHit[] hits = fromResponse.getHits().getHits();
        System.out.println("hits.length=" + hits.length);
        System.out.println("response=" + fromResponse);
        JSONObject jsonHit = new JSONObject();
        //货架格子有多种产品
        if (hits.length > 1) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_OCCUPIED.getCode(), null);
        }
        //货架格子为空
        if (hits.length == 0) {
            JSONObject jsonQuery = new JSONObject();
            jsonQuery.put("id_C", id_C);
            jsonQuery.put("locAddr.keyword", locAddr);
            jsonQuery.put("id_P", id_P);
            SearchResponse response = dbUtils.getEsKeys(jsonQuery, "lSAsset");
            SearchHit[] hits1 = response.getHits().getHits();
            //货架有这种产品
            if (hits1.length > 0) {
                jsonHit = JSONObject.parseObject(hits1[0].getSourceAsString());
                jsonHit.put("_id", hits1[0].getId());
            }
            //货架没有这种产品
            else {
                jsonHit.put("_id", "isEmpty");
            }
        }
        //货架格子不为空且只有一种产品
        else {
            jsonHit = JSONObject.parseObject(hits[0].getSourceAsString());
            jsonHit.put("_id", hits[0].getId());
        }
        return jsonHit;
    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse moveAsset(String id_C, String fromLocAddr, String toLocAddr, JSONArray fromLocSpace,
                                 JSONArray toLocSpace, JSONArray fromWn2qty, JSONArray toWn2qty) throws IOException {
        Double fromSum = 0.0;
        Double toSum = 0.0;
        System.out.println("q0" + fromWn2qty + toWn2qty);
        // NO need check, FE checked already
//        fromSum += fromWn2qty.stream().mapToDouble(f -> (Double) f).sum();
//        toSum += toWn2qty.stream().mapToDouble(t -> (Double) t).sum();
//        //拿的产品和放的产品总量不同
//        if (Double.doubleToLongBits(fromSum) != Double.doubleToLongBits(toSum)) {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.MOVE_NUM_DIFFERENT.getCode(), null);
//        }
//        if (fromLocSpace.size() != fromWn2qty.size() || toLocSpace.size() != toWn2qty.size()) {
//            throw new ErrorResponseException(HttpStatus.FORBIDDEN, DetailsEnum.INDEXES_NO_HAVE.getCode(), null);
//        }
        //判断是否有货架和格子
        this.isLocCorrect(id_C, fromLocAddr, fromLocSpace);
        this.isLocCorrect(id_C, toLocAddr, toLocSpace);

        JSONObject jsonFromHit = this.getLocSpace(id_C, "", fromLocAddr, fromLocSpace);
        System.out.println("fromhit=" + jsonFromHit);
        String fromId_A = jsonFromHit.getString("id_A");
        //from货架要拿的格子为空
        if (jsonFromHit.getString("_id").equals("isEmpty")) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_NO_HAVE.getCode(), null);
        }
        String fromHitId = jsonFromHit.getString("_id");
        jsonFromHit.remove("_id");
        String id_P = jsonFromHit.getString("id_P");
        JSONObject jsonToHit = this.getLocSpace(id_C, id_P, toLocAddr, toLocSpace);
        System.out.println("tohit=" + jsonToHit);
        //to货架没有这种产品
        if (jsonToHit.getString("_id").equals("isEmpty")) {
            System.out.println("==========to货架没有这种产品==========");

            //获取from货架要拿的产品信息
            JSONArray arrayFromLocSpace = jsonFromHit.getJSONArray("locSpace");
            JSONArray arrayFromSpaceQty = jsonFromHit.getJSONArray("spaceQty");
            //from货架要拿的格子
            for (int i = 0; i < fromLocSpace.size(); i++) {
                //from货架这种产品的格子
                for (int j = 0; j < arrayFromLocSpace.size(); j++) {
                    //格子相等
                    if (fromLocSpace.getInteger(i).equals(arrayFromLocSpace.getInteger(j))) {
                        if (fromWn2qty.getDouble(i) - 0.00001 > arrayFromSpaceQty.getDouble(j)) {
                            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), null);
                        }
                        //小于，减去数量
                        else if (abs(fromWn2qty.getDouble(i) - arrayFromSpaceQty.getDouble(j)) > 0.00001) {
                            arrayFromSpaceQty.set(j, arrayFromSpaceQty.getDouble(j) - fromWn2qty.getDouble(i));
                        }
                        //等于，删除格子数组和数量数组对应的数组元素
                        else {
                            arrayFromLocSpace.remove(j);
                            arrayFromSpaceQty.remove(j);
                        }
                    }
                }

            }
            Double fromRemain = jsonFromHit.getDouble("wn2qty") - fromSum;
            jsonFromHit.put("wn2qty", fromRemain);

            JSONObject jsonUpdate = new JSONObject();
            jsonUpdate.put("info.wn2qty", fromRemain);
            jsonUpdate.put("aStock.locSpace", arrayFromLocSpace);
            jsonUpdate.put("aStock.spaceQty", arrayFromSpaceQty);
            UpdateResult fromUpdateResult = dbUtils.setMongoValues(fromId_A, jsonUpdate, Asset.class);
            if (fromUpdateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
            }
            //获取产品信息
            Prod prod = (Prod) dbUtils.getMongoOneField(id_P, "info", Prod.class);
            JSONObject toAssetInfo = new JSONObject();
            toAssetInfo.put("id_P", id_P);
            toAssetInfo.put("wrdN", prod.getInfo().getWrdN());
            toAssetInfo.put("wrddesc", prod.getInfo().getWrddesc());
            AssetAStock assetAStock = new AssetAStock(toLocAddr, toLocSpace, toWn2qty);

            this.createAsset(id_C, toAssetInfo, assetAStock, toSum);
            dbUtils.updateEs("lSAsset", fromHitId, jsonFromHit);
            return retResult.ok(CodeEnum.OK.getCode(), null);
        }
        System.out.println("==========2==========");
        String toHitId = jsonToHit.getString("_id");
        jsonToHit.remove("_id");
        //格子产品不同
        if (!id_P.equals(jsonToHit.getString("id_P"))) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_OCCUPIED.getCode(), null);
        }
        //相同
        String toId_A = jsonToHit.getString("id_A");
        //在同一个货架移动
        if (fromId_A.equals(toId_A)) {
            System.out.println("==========在同一个货架移动==========");

            JSONArray arrayLocSpace = jsonFromHit.getJSONArray("locSpace");
            JSONArray arraySpaceQty = jsonFromHit.getJSONArray("spaceQty");
            //from货架要拿的格子
            for (int i = 0; i < fromLocSpace.size(); i++) {
                //from货架这种产品的格子
                for (int j = 0; j < arrayLocSpace.size(); j++) {
                    //格子相等
                    if (fromLocSpace.getInteger(i).equals(arrayLocSpace.getInteger(j))) {
                        //移动数量大于from货架该格子的产品数量
                        if (fromWn2qty.getDouble(i) - 0.00001 > arraySpaceQty.getDouble(j)) {
                            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), null);
                        }
                        //小于，减去数量
                        else if (abs(fromWn2qty.getDouble(i) - arraySpaceQty.getDouble(j)) > 0.00001) {
                            arraySpaceQty.set(j, arraySpaceQty.getDouble(j) - fromWn2qty.getDouble(i));
                        }
                        //等于，删除格子数组和数量数组对应的数组元素
                        else {
                            arraySpaceQty.remove(j);
                            arraySpaceQty.remove(j);
                        }
                    }
                }

            }
            boolean bool;
            //to货架要放的格子
            for (int i = 0; i < toLocSpace.size(); i++) {
                bool = true;
                //to货架这种产品的格子
                for (int j = 0; j < arrayLocSpace.size(); j++) {
                    //找到to货架要放的格子，修改为false，默认为true
                    if (toLocSpace.getInteger(i).equals(arrayLocSpace.getInteger(j))) {
                        bool = false;
                        arraySpaceQty.set(j, arraySpaceQty.getDouble(j) + toWn2qty.getDouble(i));
                    }
                }
                //找不到to货架要放的格子，新增格子数组和数量数组的数组元素
                if (bool) {
                    arrayLocSpace.add(toLocSpace.getInteger(i));
                    arraySpaceQty.add(toWn2qty.getDouble(i));
                }
            }

            JSONObject jsonUpdate = new JSONObject();
            jsonUpdate.put("aStock.locSpace", arrayLocSpace);
            jsonUpdate.put("aStock.spaceQty", arraySpaceQty);
            UpdateResult fromUpdateResult = dbUtils.setMongoValues(fromId_A, jsonUpdate, Asset.class);
            if (fromUpdateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
            }
            dbUtils.updateEs("lSAsset", fromHitId, jsonFromHit);
        }
        //移动到另一个货架
        else {
            System.out.println("==========移动到另一个货架==========");

            JSONArray arrayFromLocSpace = jsonFromHit.getJSONArray("locSpace");
            JSONArray arrayFromSpaceQty = jsonFromHit.getJSONArray("spaceQty");
            JSONArray arrayToLocSpace = jsonToHit.getJSONArray("locSpace");
            JSONArray arrayToSpaceQty = jsonToHit.getJSONArray("spaceQty");
            //from货架要拿的格子
            for (int i = 0; i < fromLocSpace.size(); i++) {
                //from货架这种产品的格子
                for (int j = 0; j < arrayFromLocSpace.size(); j++) {
                    //格子相等
                    if (fromLocSpace.getInteger(i).equals(arrayFromLocSpace.getInteger(j))) {
                        if (fromWn2qty.getDouble(i) - 0.00001 > arrayFromSpaceQty.getDouble(j)) {
                            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), null);
                        }
                        //小于，减去数量
                        else if (abs(fromWn2qty.getDouble(i) - arrayFromSpaceQty.getDouble(j)) > 0.00001) {
                            arrayFromSpaceQty.set(j, arrayFromSpaceQty.getDouble(j) - fromWn2qty.getDouble(i));
                        }
                        //等于，删除格子数组和数量数组对应的数组元素
                        else {
                            arrayFromLocSpace.remove(j);
                            arrayFromSpaceQty.remove(j);
                        }
                    }
                }

            }

            Double fromRemain = jsonFromHit.getDouble("wn2qty") - fromSum;
            jsonFromHit.put("wn2qty", fromRemain);

            JSONObject jsonUpdate = new JSONObject();
            jsonUpdate.put("info.wn2qty", fromRemain);
            jsonUpdate.put("aStock.locSpace", arrayFromLocSpace);
            jsonUpdate.put("aStock.spaceQty", arrayFromSpaceQty);
            UpdateResult fromUpdateResult = dbUtils.setMongoValues(fromId_A, jsonUpdate, Asset.class);
            if (fromUpdateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
            }

            boolean bool;
            //to货架要放的格子
            for (int i = 0; i < toLocSpace.size(); i++) {
                bool = true;
                //to货架这种产品的格子
                //找到to货架要放的格子，修改为false，默认为true
                for (int j = 0; j < arrayToLocSpace.size(); j++)
                    if (toLocSpace.getInteger(i) == arrayToLocSpace.getInteger(j)) {
                        bool = false;
                        arrayToSpaceQty.set(j, arrayToSpaceQty.getDouble(j) + toWn2qty.getDouble(i));
                    }
                //找不到to货架要放的格子，新增格子数组和数量数组的数组元素
                if (bool) {
                    arrayToLocSpace.add(toLocSpace.getInteger(i));
                    arrayToSpaceQty.add(toWn2qty.getDouble(i));
                }
            }
            //KEV this wn2qty => 0, should be a value
            Double toRemain = jsonToHit.getDouble("wn2qty") + toSum;
            jsonToHit.put("wn2qty", toRemain);

            jsonUpdate = new JSONObject();
            jsonUpdate.put("info.wn2qty", toRemain);
            jsonUpdate.put("aStock.locSpace", arrayToLocSpace);
            jsonUpdate.put("aStock.spaceQty", arrayToSpaceQty);
            UpdateResult toUpdateResult = dbUtils.setMongoValues(toId_A, jsonUpdate, Asset.class);
            if (toUpdateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
            }

            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.add(new UpdateRequest("lSAsset", fromHitId).doc(jsonFromHit, XContentType.JSON));
            bulkRequest.add(new UpdateRequest("lSAsset", toHitId).doc(jsonToHit, XContentType.JSON));
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        }
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


    private void isLocCorrect(String id_C, String locAddr, JSONArray locSpace) {
        //分割 仓库编号##区域##前缀##编号
        String[] locSplit = locAddr.split("##");
        //获取仓库
        String id_A = dbUtils.getId_A(id_C, "a-auth");
        String field = "locSetup.locWH." + locSplit[0] + ".locArea." + locSplit[1] + ".locRack." + locSplit[2];
        System.out.println("id_A " + id_A + " " + id_C + " " + field);
        Asset asset = (Asset) dbUtils.getMongoOneField(id_A, "locSetup.locWH", Asset.class);


        //货架不存在
//        if (asset.getLocSetup() != null && asset.getLocSetup().getJSONObject("locWH") != null &&
//                asset.getLocSetup().getJSONObject("locWH").getJSONObject(locSplit[0]) != null &&
//                asset.getLocSetup().getJSONObject("locWH").getJSONObject(locSplit[0]).getJSONObject("locArea") != null ||
//                asset.getLocSetup().getJSONObject("locWH").getJSONObject(locSplit[0]).getJSONObject("locArea")
//                        .getJSONObject(locSplit[1]) == null ||
//                asset.getLocSetup().getJSONObject("locWH").getJSONObject(locSplit[0]).getJSONObject("locArea")
//                        .getJSONObject(locSplit[1]).getJSONObject("locRack").getJSONObject(locSplit[2]) == null) {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_NO_HAVE.getCode(), null);
//        }

        System.out.println("asset=" + asset);
        //获取货架的格子数
        Integer spaceCount = asset.getLocSetup().getJSONObject("locWH").getJSONObject(locSplit[0]).getJSONObject("locArea")
                .getJSONObject(locSplit[1]).getJSONObject("locRack").getJSONObject(locSplit[2]).getInteger("spaceCount");
        System.out.println("spaceCount=" + spaceCount);
        //判断是否格子越界
        locSpace.forEach(s -> {
            if ((Integer) s >= spaceCount) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_NO_HAVE.getCode(), null);
            }
        });
    }

    private String createAsset(String id_C, JSONObject oItemData, AssetAStock aStock, Double wn2qtynow) throws IOException {
        String assetId = MongoUtils.GetObjectId();
        System.out.println("assetId=" + assetId);

        System.out.println("grpP" + oItemData.getString("grpB"));
        String id_A = coupaUtil.getAssetId(id_C, "a-auth");
        String grpA = "1000";
        String objListType = "objlBP";
        String grpType = "grpB";
        Asset assetGrpChecker = coupaUtil.getAssetById(id_A, Collections.singletonList("def"));

        if (!oItemData.getString("id_CB").equals(id_C)) {
            // this buyer is not me, so it must be lSProd
            objListType = "objlSP";
            grpType = "grp";
        }

        grpA = assetGrpChecker.getDef().getJSONObject(objListType).getJSONObject(oItemData.getString(grpType)).getString("grpA");

//        for (int i = 0; i < assetGrpChecker.getDef().getJSONArray(objListType).size(); i++) {
//            String grpRef = assetGrpChecker.getDef().getJSONArray(objListType).getJSONObject(i).getString("ref");
//
//            if (grpRef.equals(oItemData.getString(grpType))) {
//                grpA = assetGrpChecker.getDef().getJSONArray(objListType).getJSONObject(i).getString("grpA");
//                break;
//            }
//        }


        AssetInfo assetInfo = new AssetInfo(id_C, id_C, oItemData.getString("id_P"), oItemData.getJSONObject("wrdN"),
                oItemData.getJSONObject("wrddesc"), grpA, oItemData.getString("ref"), oItemData.getString("pic"), 2);
        assetInfo.setWn2qty(wn2qtynow);
        JSONArray arrayView = new JSONArray();
        arrayView.add("info");
        arrayView.add("aStock");
        Asset addAsset = new Asset();
        addAsset.setId(assetId);
        addAsset.setInfo(assetInfo);
        addAsset.setView(arrayView);
        addAsset.setAStock((JSONObject) JSONObject.toJSON(aStock));
        mongoTemplate.insert(addAsset);
        //新增lSAsset
        lSAsset lsasset = new lSAsset(assetId, id_C, id_C, oItemData.getString("id_P"), wn2qtynow, oItemData.getJSONObject("wrdN"),
                oItemData.getJSONObject("wrddesc"), grpA, oItemData.getString("pic"), oItemData.getString("ref"), 2);
        lsasset.setLocAddr(aStock.getLocAddr());
        lsasset.setLocSpace(aStock.getLocSpace());
        lsasset.setSpaceQty(aStock.getSpaceQty());

        JSONObject lb = (JSONObject) JSON.toJSON(lsasset);
        System.out.println("lb" + lb);

        dbUtils.addES(lb, "lSAsset");

        return assetId;

    }

    @Override
    public ApiResponse pushAsset(JSONObject tokData, String id_O, Integer index,
                                 String locAddr, JSONArray locSpace, JSONArray wn2qty) throws IOException {

        this.isLocCorrect(tokData.getString("id_C"), locAddr, locSpace);

        String id_A;
        Double toSum = 0.0;

        //获取订单和产品id
//        Order order = (Order) dbUtils.getMongoOneFields(id_O, Arrays.asList("oStock", "oItem"), Order.class);
        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info", "oItem", "oStock", "action"));
        JSONObject oItem = order.getOItem().getJSONArray("objItem").getJSONObject(index);

        JSONObject jsonToHit = this.getLocSpace(tokData.getString("id_C"), oItem.getString("id_P"), locAddr, locSpace);
        System.out.println("jsonToHit=" + jsonToHit);
        String toHitId = jsonToHit.getString("_id");
        jsonToHit.remove("_id");
        //to货架没有这种产品
        if (toHitId.equals("isEmpty")) {
            Double wn2qtynow = 0.0;
            System.out.println("jsonToHit=" + jsonToHit);

            //移动产品
            for (int i = 0; i < wn2qty.size(); i++) {
                wn2qtynow += wn2qty.getDouble(i);
            }
            wn2qtynow = Double.parseDouble(decimalFormat.format(wn2qtynow));

            //减少订单产品数量
            UpdateResult updateResult = dbUtils.incMongoValue(id_O, "oStock.objData." + index + ".wn2qtynow",
                    -wn2qtynow, Order.class); //ok
            if (updateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
            }

            AssetAStock assetAStock = new AssetAStock(locAddr, locSpace, wn2qty);

            id_A = this.createAsset(tokData.getString("id_C"), oItem, assetAStock, wn2qtynow);
            toSum = wn2qtynow;

//            return retResult.ok(CodeEnum.OK.getCode(), null);
        }
        //货架要放的格子不为空且只有一种产品
        else {
            //格子产品不同
            if (!oItem.getString("id_P").equals(jsonToHit.getString("id_P"))) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_OCCUPIED.getCode(), null);
            }

            id_A = jsonToHit.getString("id_A");
            //相同
            JSONArray arrayToLocSpace = jsonToHit.getJSONArray("locSpace");
            JSONArray arrayToSpaceQty = jsonToHit.getJSONArray("spaceQty");
            Boolean bool;
            //to货架要放的格子
            for (int i = 0; i < locSpace.size(); i++) {
                bool = true;
                //to货架这种产品的格子
                for (int j = 0; j < arrayToLocSpace.size(); j++) {
                    //找到to货架要放的格子，修改为false，默认为true
                    if (locSpace.getInteger(i) == arrayToLocSpace.getInteger(j)) {
                        bool = false;
                        arrayToSpaceQty.set(j, arrayToSpaceQty.getDouble(j) + wn2qty.getDouble(i));
                    }
                }
                //找不到to货架要放的格子，新增格子数组和数量数组的数组元素
                if (bool) {
                    arrayToLocSpace.add(locSpace.getInteger(i));
                    arrayToSpaceQty.add(wn2qty.getDouble(i));
                }
                toSum += wn2qty.getDouble(i);
            }
            toSum = Double.parseDouble(decimalFormat.format(toSum));

            //减少订单产品数量
            UpdateResult updateResult = dbUtils.incMongoValue(id_O, "oStock.objData." + index + ".wn2qtynow", - toSum, Order.class);
            if (updateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
            }


            Double toRemain = Double.parseDouble(decimalFormat.format(jsonToHit.getDouble("wn2qty") + toSum));

            jsonToHit.put("wn2qty", toRemain);

            JSONObject jsonUpdate = new JSONObject();
            jsonUpdate.put("info.wn2qty", toRemain);
            jsonUpdate.put("aStock.locSpace", arrayToLocSpace);
            jsonUpdate.put("aStock.spaceQty", arrayToSpaceQty);
            UpdateResult toUpdateResult = dbUtils.setMongoValues(id_A, jsonUpdate, Asset.class);
            if (toUpdateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
            }

            UpdateResponse updateResponse = dbUtils.updateEs("lSAsset", toHitId, jsonToHit);
        }

        LogFlow log = new LogFlow(tokData, oItem, order.getAction(),
                order.getInfo().getId_CB(), id_O, index, "assetflow", "stoChg",
                oItem.getJSONObject("wrdN").getString("cn") +  "入仓了" + toSum, 3);

        Double logPrice = order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn4price");
//        Integer logStatus = order.getAction().getJSONArray("objAction").getJSONObject(index).getInteger("bcdStatus");
        log.setLogData_assetflow(toSum * -1, logPrice, id_A);

        wsClient.sendWS(log);

        return retResult.ok(CodeEnum.OK.getCode(), null);

    }

    @Override
    public ApiResponse popAssetByLocation(JSONObject tokData, String id_P, String id_O, Integer index,
                                          String locAddr, JSONArray locSpace, JSONArray wn2qty) throws IOException {

        String id_C = tokData.getString("id_C");
        this.isLocCorrect(id_C, locAddr, locSpace);

        JSONObject jsonFromHit = this.getLocSpace(id_C, id_P, locAddr, locSpace);
        System.out.println("jsonFromHit" + jsonFromHit);
        String fromHitId = jsonFromHit.getString("_id");
        jsonFromHit.remove("_id");
        //from货架要拿的格子为空
        if (fromHitId.equals("isEmpty")) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_NO_HAVE.getCode(), null);
        }
        //格子产品不同
        if (!id_P.equals(jsonFromHit.getString("id_P"))) {
            System.out.println("Something in the space but it is not same id_P");
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_OCCUPIED.getCode(), null);
        }
        //相同
        JSONArray arrayFromLocSpace = jsonFromHit.getJSONArray("locSpace");
        JSONArray arrayFromSpaceQty = jsonFromHit.getJSONArray("spaceQty");
        Double fromSum = 0.0;
        //from货架要拿的格子
        for (int i = 0; i < locSpace.size(); i++) {
            //from货架这种产品的格子
            for (int j = 0; j < arrayFromLocSpace.size(); j++) {
                //格子相等
                if (locSpace.getInteger(i) == arrayFromLocSpace.getInteger(j)) {
                    //移动数量大于from货架该格子的产品数
                    if (wn2qty.getDouble(i) - 0.00001 > arrayFromSpaceQty.getDouble(j)) {
                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), null);
                    }
                    //小于，减去数量
                    else if (abs(wn2qty.getDouble(i) - arrayFromSpaceQty.getDouble(j)) > 0.00001) {
                        arrayFromSpaceQty.set(j, arrayFromSpaceQty.getDouble(j) - wn2qty.getDouble(i));
                    }
                    //等于，删除格子数组和数量数组对应的数组元素
                    else {
                        arrayFromLocSpace.remove(j);
                        arrayFromSpaceQty.remove(j);
                    }
                }
//                for (int k = 0; k < remover.size(); k++)
//                {
//                    arrayFromLocSpace.remove(remover.getInteger(k));
//                    arrayFromSpaceQty.remove(remover.getInteger(k));
//                }

            }
            fromSum += wn2qty.getDouble(i);
        }

        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info", "action", "oItem", "oStock"));

        if (order == null || order.getOStock() == null || order.getOItem() == null) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_NOT_FOUND.getCode(), "");
        }

        //增加订单产品数量
        UpdateResult updateResult = dbUtils.incMongoValue(id_O, "oStock.objData." + index + ".wn2qtynow", fromSum, Order.class);


        if (updateResult.getModifiedCount() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
        }
        Double fromRemain = jsonFromHit.getDouble("wn2qty") - fromSum;


        if (fromRemain == 0) {
            // qty(fromRemain) = 0, delete Asset
            coupaUtil.delAsset(jsonFromHit.getString("id_A"));
            dbUtils.delES("lSAsset", "id_A", jsonFromHit.getString("id_A"));

        } else {
            // 创建查询，并且添加查询条件

            jsonFromHit.put("wn2qty", fromRemain);

            JSONObject jsonUpdate = new JSONObject();
            jsonUpdate.put("info.wn2qty", fromRemain);
            jsonUpdate.put("aStock.locSpace", arrayFromLocSpace);
            jsonUpdate.put("aStock.spaceQty", arrayFromSpaceQty);
            UpdateResult fromUpdateResult = dbUtils.setMongoValues(jsonFromHit.getString("id_A"), jsonUpdate, Asset.class);
            if (fromUpdateResult.getModifiedCount() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
            }
        }
        UpdateResponse updateResponse = dbUtils.updateEs("lSAsset", fromHitId, jsonFromHit);

        JSONObject oItem = order.getOItem().getJSONArray("objItem").getJSONObject(index);

        LogFlow log = new LogFlow(tokData, oItem, order.getAction(),
                order.getInfo().getId_CB(), id_O, index, "assetflow", "stoChg",
                oItem.getJSONObject("wrdN").getString("cn")+"出仓了" + fromRemain, 3);

        Double logPrice = order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn4price");
//        Integer logStatus = order.getAction().getJSONArray("objAction").getJSONObject(index).getInteger("bcdStatus");
        log.setLogData_assetflow(fromRemain, logPrice, jsonFromHit.getString("id_A"));

        wsClient.sendWS(log);

        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse popAssetById_A(JSONObject tokData, String id_P, String id_O, Integer index,
                                      String id_A, JSONArray locSpace, JSONArray wn2qty, Boolean isResv) throws IOException {
        //获取货架
        Asset fromAsset = (Asset) dbUtils.getMongoOneFields(id_A, Arrays.asList("info", "aStock"), Asset.class);
        //格子产品不同
        if (!id_P.equals(fromAsset.getInfo().getId_P())) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_OCCUPIED.getCode(), null);
        }



        JSONObject jsonFromAStock = fromAsset.getAStock();
        JSONArray arrayFromLocSpace = jsonFromAStock.getJSONArray("locSpace");
        JSONArray arrayFromSpaceQty = jsonFromAStock.getJSONArray("spaceQty");
        Double fromSum = 0.0;


        //from货架要拿的格子
        for (int i = 0; i < locSpace.size(); i++) {
//            JSONArray remover = new JSONArray();
            //from货架这种产品的格子
            for (int j = 0; j < arrayFromLocSpace.size(); j++) {
                //格子相等
                if (locSpace.getInteger(i) == arrayFromLocSpace.getInteger(j)) {
                    //移动数量大于from货架该格子的产品数量
                    if (wn2qty.getDouble(i) - 0.00001 > arrayFromSpaceQty.getDouble(j)) {
                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), null);
                    }
                    //小于，减去数量
                    else if (abs(wn2qty.getDouble(i) - arrayFromSpaceQty.getDouble(j)) > 0.00001) {
                        arrayFromSpaceQty.set(j, arrayFromSpaceQty.getDouble(j) - wn2qty.getDouble(i));
                    }
                    //等于，删除格子数组和数量数组对应的数组元素
                    else {
                        arrayFromLocSpace.remove(j);
                        arrayFromSpaceQty.remove(j);
                    }
                }
            }

            fromSum += wn2qty.getDouble(i);
        }
        //增加订单产品数量
        fromSum = Double.parseDouble(decimalFormat.format(fromSum));

        ///////************GET lSAsset - id_A ES **************//////////

        JSONObject jsonQuery = new JSONObject();
        jsonQuery.put("id_A", id_A);
        SearchResponse searchResponse = dbUtils.getEsKeys(jsonQuery, "lSAsset");
        SearchHit hit = searchResponse.getHits().getHits()[0];
        String fromHitId = hit.getId();
        JSONObject jsonFromHit = JSONObject.parseObject(hit.getSourceAsString());
        ///////************GET lSAsset - id_A ES **************//////////
        System.out.println("hit"+jsonFromHit);

        // if total qty needed is more than the qty available, error
        if ((isResv && Double.parseDouble(decimalFormat.format(jsonFromHit.getDouble("wn2qtyResv") - fromSum)) < 0)||
                (!isResv && Double.parseDouble(decimalFormat.format(jsonFromHit.getDouble("wn2qty") - fromSum)) < 0))
        {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), null);
        }


        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info", "action", "oItem", "oStock"));

        if (order == null || order.getOStock() == null || order.getOItem() == null) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_NOT_FOUND.getCode(), "");
        }

        JSONObject mapKey = new JSONObject();
        JSONObject resvOrderQty = new JSONObject();


        ///////************ oStock - resvQty **************//////////
        if (isResv && order.getOStock().getJSONArray("objData").getJSONObject(index).getJSONObject("resvQty") != null &&
                order.getOStock().getJSONArray("objData").getJSONObject(index).getJSONObject("resvQty").getJSONObject(id_A)!= null)
        {
            //start to delete id_A qty, if == 0, remove object, else delete wn2qty
            resvOrderQty = order.getOStock().getJSONArray("objData").getJSONObject(index).getJSONObject("resvQty");
            System.out.println(resvOrderQty);
            if (resvOrderQty.getJSONObject(id_A).getDouble("wn2qty") - fromSum <= 0)
            {
                resvOrderQty.remove(id_A);
            } else {
                resvOrderQty.getJSONObject(id_A).put("wn2qty", resvOrderQty.getJSONObject(id_A).getDouble("wn2qty") - fromSum);
            }

            mapKey.put("oStock.objData."+index+".resvQty",resvOrderQty);
            System.out.println("mapKey"+mapKey);
        }

        Double orderQty = order.getOStock().getJSONArray("objData").getJSONObject(index).getDouble("wn2qtynow") + fromSum;
        mapKey.put("oStock.objData."+index+".wn2qtynow",orderQty);

        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
        ///////************ oStock - resvQty **************//////////


            JSONObject jsonUpdate = new JSONObject();

        if (isResv && fromAsset.getAStock().getJSONObject("resvQty") != null &&
                    fromAsset.getAStock().getJSONObject("resvQty").getDouble(id_O + "-" + index) != null)
            {
                ///////************SET - aStock resvAsset qty **************//////////
                Double fromRemain = Double.parseDouble(decimalFormat.format(jsonFromHit.getDouble("wn2qtyResv") - fromSum));
                System.out.println("what is after parse"+ fromRemain);

                if (jsonFromHit.getDouble("wn2qty") == 0
                        && fromRemain == 0) {
                    // 创建查询，并且添加查询条件
                    coupaUtil.delAsset(id_A);
                    dbUtils.delES("lSAsset", "id_A", id_A);
                } else {
                    //check if fromSum == resvQty.wn2qty, if so remove that object, else deduct
                    JSONObject unitResv = fromAsset.getAStock().getJSONObject("resvQty");
                    if (fromSum - unitResv.getDouble(id_O + "-" + index) == 0) {
                        unitResv.remove(id_O + "-" + index);
                    } else {
                        unitResv.put(id_O + "-" + index, Double.parseDouble(decimalFormat.format(unitResv.getDouble(id_O + "-" + index) - fromSum)));
                    }
                    jsonUpdate.put("aStock.resvQty", unitResv);
                    jsonUpdate.put("aStock.wn2qtyResv", fromRemain);
                    jsonUpdate.put("aStock.locSpace", arrayFromLocSpace);
                    jsonUpdate.put("aStock.spaceQty", arrayFromSpaceQty);

                    jsonFromHit.put("wn2qtyResv", fromRemain);
                    jsonFromHit.put("locSpace", arrayFromLocSpace);
                    jsonFromHit.put("spaceQty", arrayFromSpaceQty);
                    dbUtils.updateEs("lSAsset", fromHitId, jsonFromHit);
                    UpdateResult fromUpdateResult = dbUtils.setMongoValues(id_A, jsonUpdate, Asset.class);
                    if (fromUpdateResult.getModifiedCount() == 0) {
                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
                    }
                }
                ///////************SET - aStock resvAsset qty **************//////////

            } else {
            ///////************SET - aStock regular qty **************//////////

            Double fromRemain = Double.parseDouble(decimalFormat.format(jsonFromHit.getDouble("wn2qty") - fromSum));
                System.out.println("from"+ fromRemain);
                if (fromRemain <= 0  && (jsonFromHit.getDouble("wn2qtyResv") == null || jsonFromHit.getDouble("wn2qtyResv") == 0)) {
                    // 创建查询，并且添加查询条件
                    coupaUtil.delAsset(id_A);
                    dbUtils.delES("lSAsset", "id_A", id_A);
                } else {
                    jsonUpdate.put("info.wn2qty", fromRemain);
                    jsonUpdate.put("aStock.locSpace", arrayFromLocSpace);
                    jsonUpdate.put("aStock.spaceQty", arrayFromSpaceQty);

                    jsonFromHit.put("wn2qty", fromRemain);
                    jsonFromHit.put("locSpace", arrayFromLocSpace);
                    jsonFromHit.put("spaceQty", arrayFromSpaceQty);
                    dbUtils.updateEs("lSAsset", fromHitId, jsonFromHit);
                    UpdateResult fromUpdateResult = dbUtils.setMongoValues(id_A, jsonUpdate, Asset.class);
                    if (fromUpdateResult.getModifiedCount() == 0) {
                        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
                    }
                }
            ///////************SET - aStock regular qty **************//////////

        }

        String prodName = order.getOItem().getJSONArray("objItem").getJSONObject(index).getJSONObject("wrdN").getString("cn");

        LogFlow log = new LogFlow(tokData, order.getOItem().getJSONArray("objItem").getJSONObject(index), order.getAction(),
                order.getInfo().getId_CB(), id_O, index, "assetflow", "stoChg", prodName + "出仓了 " + fromSum, 3);

        ///////************Send LOG by recentLog **************//////////

//        LogFlow log = dbUtils.getRecentLog(id_O, index, tokData);
        Double logPrice = order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn4price");
//        Integer logStatus = order.getAction().getJSONArray("objAction").getJSONObject(index).getInteger("bcdStatus");
//        log.setZcndesc(prodName + "出仓了 " + fromSum);
        log.setLogData_assetflow(fromSum, logPrice, id_A);

        wsClient.sendWS(log);

        return retResult.ok(CodeEnum.OK.getCode(), "");
    }


    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse producedMax(String id_U, String id_C, String id_O, Integer index) {

//        DecimalFormat decimalFormat = new DecimalFormat("#.000");

        Order order = (Order) dbUtils.getMongoOneFields(id_O, Arrays.asList("action", "oItem"), Order.class);
        JSONArray arraySubParts = order.getAction().getJSONArray("objAction").getJSONObject(index).getJSONArray("subParts");
        Double wn2qtyneed = order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn2qtyneed");

        System.out.println("arraySubParts=" + arraySubParts);
        if (arraySubParts != null) {
            Double ratio = 10.000000;
            JSONArray arrayProdNum = new JSONArray();
            //遍历获取本产品需要的零件数量和比例
            for (int i = 0; i < arraySubParts.size(); i++) {
                JSONObject jsonProdNum = new JSONObject();
                String subId_O = arraySubParts.getJSONObject(i).getString("id_O");
                Integer subIndex = arraySubParts.getJSONObject(i).getInteger("index");

                Order subOrder = (Order) dbUtils.getMongoOneFields(subId_O, Arrays.asList("oItem", "action", "oStock"), Order.class);

//                Double subWn2qtyneed = subOrder.getOItem().getJSONArray("objItem").getJSONObject(subIndex).getDouble("wn2qtyneed");
                Double subWn2qtyneed = arraySubParts.getJSONObject(i).getDouble("qtyEach") * wn2qtyneed;
                subWn2qtyneed = Double.parseDouble(decimalFormat.format(subWn2qtyneed));
                Double subWn2qtynow = 0.0;
                System.out.println("subWn2qtyneed=" + subWn2qtyneed);
                jsonProdNum.put("wn2qtyneed", subWn2qtyneed);
                System.out.println("subOrder" + subOrder.getOStock());
                JSONObject actionData = subOrder.getAction().getJSONArray("objAction").getJSONObject(subIndex);

                jsonProdNum.put("bcdStatus", actionData.getInteger("bcdStatus"));
                jsonProdNum.put("bmdpt", actionData.getInteger("bmdpt"));


                if (subOrder.getOStock() != null && subOrder.getOStock().getJSONArray("objData") != null &&
                        subOrder.getOStock().getJSONArray("objData").size() > subIndex &&
                        subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex) != null) {

                    if ( !actionData.getInteger("bmdpt").equals(1) && subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex).getJSONArray("objShip") != null) {

                        // objShip is array with the order you chose to stock them, but if it is a process.... then it can only be objShip.[0]
                        JSONObject stockNow = subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex).getJSONArray("objShip")
                                .getJSONObject(arraySubParts.getJSONObject(i).getInteger("upIndex"));
                        System.out.println("stockNOW" + stockNow);
                        subWn2qtynow = stockNow.getDouble("wn2qtynow");

                    } else if (actionData.getInteger("bmdpt").equals(1) && subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex).getJSONArray("objShip") != null){
                        //if this is a process, you don't have many objShip only [0]
                        JSONObject stockNow = subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex).getJSONArray("objShip")
                                .getJSONObject(0);
                        System.out.println("stockNOW" + stockNow);
                        subWn2qtynow = stockNow.getDouble("wn2qtynow");
                    }
                    System.out.println("subWn2qtynow=" + subWn2qtynow);
                }

                jsonProdNum.put("wn2qtynow", subWn2qtynow);


                String id_P = subOrder.getOItem().getJSONArray("objItem").getJSONObject(subIndex).getString("id_P");
                Prod prod = (Prod) dbUtils.getMongoOneField(id_P, "info.wrdN", Prod.class);
                jsonProdNum.put("wrdN", prod.getInfo().getWrdN());
                arrayProdNum.add(jsonProdNum);
                //获取零件数量的最小比例
                if ((subWn2qtynow / subWn2qtyneed) < ratio) {
//                    ratio = subWn2qtynow / subWn2qtyneed;
                    System.out.println("ratio=" + ratio);
                    ratio = Double.parseDouble(decimalFormat.format(subWn2qtynow / subWn2qtyneed));
                }
            }
            System.out.println("ratio=" + ratio);
            //根据比例向下取整获取最大增加产品数量
//                Double count = order.getOStock().getJSONArray("objData").getJSONObject(index).getDouble("count");
            Double wn2qtynow = wn2qtyneed * ratio;
            System.out.println("wn2qtynow=" + wn2qtynow);

            JSONObject jsonProduced = new JSONObject();
            jsonProduced.put("producedMax", wn2qtynow);
            jsonProduced.put("arrayProdNum", arrayProdNum);
            System.out.println("jsonProduced=" + jsonProduced);
            return retResult.ok(CodeEnum.OK.getCode(), jsonProduced);
        }
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, DetailsEnum.ORDER_ALL_ERROR.getCode(), null);
    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse producedNow(JSONObject tokData, String id_O, Integer index, Double wn2qtynow) throws IOException {

//        DecimalFormat decimalFormat = new DecimalFormat("#.000");

        Query queryOrder = new Query(new Criteria("_id").is(id_O));
        Order order = mongoTemplate.findOne(queryOrder, Order.class);

        JSONArray arraySubParts = order.getAction().getJSONArray("objAction").getJSONObject(index).getJSONArray("subParts");
        Double wn2qtyneed = order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn2qtyneed");

        System.out.println("arraySubParts=" + arraySubParts);
        if (arraySubParts != null) {
            Double ratio = 10.00000000;
            //遍历获取本产品需要的零件数量和比例
            for (int i = 0; i < arraySubParts.size(); i++) {
                String subId_O = arraySubParts.getJSONObject(i).getString("id_O");
                Integer subIndex = arraySubParts.getJSONObject(i).getInteger("index");
//                Query subQueryOrder = new Query(new Criteria("_id").is(subId_O));
//                Order subOrder = mongoTemplate.findOne(subQueryOrder, Order.class);

                Order subOrder = (Order) dbUtils.getMongoOneFields(subId_O, Arrays.asList("oItem", "action", "oStock"), Order.class);
                JSONObject actionData = subOrder.getAction().getJSONArray("objAction").getJSONObject(subIndex);

//                Double subWn2qtyneed = subOrder.getOItem().getJSONArray("objItem").getJSONObject(subIndex).getDouble("wn2qtyneed");
                Double subWn2qtyneed = arraySubParts.getJSONObject(i).getDouble("qtyEach") * wn2qtyneed;
                subWn2qtyneed = Double.parseDouble(decimalFormat.format(subWn2qtyneed));

                System.out.println("subWn2qtyneed=" + subWn2qtyneed);

                Double subWn2qtynow = 0.0;
                arraySubParts.getJSONObject(i).put("bmdpt", actionData.getInteger("bmdpt"));

                if (subOrder.getOStock() != null && subOrder.getOStock().getJSONArray("objData") != null &&
                        subOrder.getOStock().getJSONArray("objData").size() > subIndex &&
                        subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex) != null) {

                    if ( !actionData.getInteger("bmdpt").equals(1) && subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex).getJSONArray("objShip") != null) {

                        // objShip is array with the order you chose to stock them, but if it is a process.... then it can only be objShip.[0]
                        JSONObject stockNow = subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex).getJSONArray("objShip")
                                .getJSONObject(arraySubParts.getJSONObject(i).getInteger("upIndex"));
                        System.out.println("stockNOW" + stockNow);
                        subWn2qtynow = stockNow.getDouble("wn2qtynow");

                    } else if (actionData.getInteger("bmdpt").equals(1) && subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex).getJSONArray("objShip") != null){
                        //if this is a process, you don't have many objShip only [0]
                        JSONObject stockNow = subOrder.getOStock().getJSONArray("objData").getJSONObject(subIndex).getJSONArray("objShip")
                                .getJSONObject(0);
                        System.out.println("stockNOW" + stockNow);
                        subWn2qtynow = stockNow.getDouble("wn2qtynow");
                    }
                    System.out.println("subWn2qtynow=" + subWn2qtynow);
                }

                //获取零件数量的最小比例
                Double subRatio = subWn2qtynow / subWn2qtyneed;
                if (subRatio < ratio) {
                    ratio = Double.parseDouble(decimalFormat.format(subRatio));
                    System.out.println("ratio=" + ratio);
                }
            }
//            System.out.println("ratio=" + ratio);
            //根据比例向下取整获取最大增加产品数量
//                Double count = order.getOStock().getJSONArray("objData").getJSONObject(index).getDouble("count");
            Double wn2qtymax = wn2qtyneed * ratio;
            System.out.println("wn2qtymax=" + wn2qtymax);
            if (wn2qtynow > wn2qtymax) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), "");

            }

            Update updateOrder = new Update();
            JSONObject oStock = order.getOStock();
            String id_P = order.getOItem().getJSONArray("objItem").getJSONObject(index).getString("id");
            JSONObject jsonObject = new JSONObject();

            wn2qtynow = Double.parseDouble(decimalFormat.format(wn2qtynow));
            jsonObject.put("wn2qtynow", wn2qtynow);
            jsonObject.put("wn2qtymade", wn2qtynow);
            jsonObject.put("id_P", id_P);

            if (oStock == null) {
                //oStock为空，创建objData数组, 添加数组对象
                JSONArray jsonArray = new JSONArray();
                jsonArray.set(index, jsonObject);
                updateOrder.set("oStock.objData", jsonArray);
                updateOrder.push("view", "oStock");
            } else if (oStock.getJSONArray("objData") == null) {
                //oStock.objData为空，创建objData数组, 添加数组对象
                JSONArray jsonArray = new JSONArray();
                jsonArray.set(index, jsonObject);
                updateOrder.set("oStock.objData", jsonArray);
            } else if (oStock.getJSONArray("objData").size() - 1 < index ||
                    oStock.getJSONArray("objData").getJSONObject(index) == null) {
                updateOrder.set("oStock.objData." + index, jsonObject);
            } else {
                //oStock.objData.指定下标不为空，增加数量
                updateOrder.inc("oStock.objData." + index + ".wn2qtynow", wn2qtynow);
                updateOrder.inc("oStock.objData." + index + ".wn2qtymade", wn2qtynow);
            }

            //根据增加的产品数量回传比例
//            System.out.println("ratio=" + ratio);
            UpdateResult updateOrderResult = mongoTemplate.updateFirst(queryOrder, updateOrder, Order.class);
            System.out.println("updateOrderResult=" + updateOrderResult);
            boolean bool = true;


            // if my qty added, now I need to deduct all subPart qty
            if (updateOrderResult.getModifiedCount() > 0) {
                //遍历根据比例减少每种零件数量
                for (int i = 0; i < arraySubParts.size(); i++) {
                    String subId_O = arraySubParts.getJSONObject(i).getString("id_O");
                    Integer subIndex = arraySubParts.getJSONObject(i).getInteger("index");


                    Query querySubOrder = new Query(new Criteria("_id").is(subId_O));
//                    Order subOrder = mongoTemplate.findOne(querySubOrder, Order.class);
//                    Double subWn2qtyneed = subOrder.getOItem().getJSONArray("objItem").getJSONObject(subIndex).getDouble("wn2qtyneed");
                    Double subWn2qtyneed = arraySubParts.getJSONObject(i).getDouble("qtyEach") * wn2qtyneed;
                    subWn2qtyneed = Double.parseDouble(decimalFormat.format(subWn2qtyneed));

                    Double deductRatio = subWn2qtyneed / wn2qtyneed;
                    deductRatio = Double.parseDouble(decimalFormat.format(deductRatio));
                    System.out.println("subWn2qtyneed=" + subWn2qtyneed);
                    Double subQtyUsed = wn2qtynow * deductRatio;
                    subQtyUsed = Double.parseDouble(decimalFormat.format(subQtyUsed));

//                    Double subWn2qtynow = subWn2qtyneed * ratio; // this is wrong! because different component has different ratio
                    System.out.println("subWn2qtynow=" + subQtyUsed);
                    Update updateSubOrder = new Update();
                    Integer upIndex = arraySubParts.getJSONObject(i).getInteger("upIndex");
                    if (arraySubParts.getJSONObject(i).getInteger("bmdpt").equals(1))
                    {
                        upIndex = 0;
                    }

                    updateSubOrder.inc("oStock.objData." + subIndex + ".objShip." + upIndex + ".wn2qtynow", -subQtyUsed);
                    UpdateResult updateSubOrderResult = mongoTemplate.updateFirst(querySubOrder, updateSubOrder, Order.class);
                    System.out.println("updateSubOrderResult=" + updateSubOrderResult);
                    if (updateSubOrderResult.getModifiedCount() == 0) {
                        bool = false;
                    }

                    LogFlow logSub = dbUtils.getRecentLog(subId_O,subIndex,tokData);
                    logSub.setZcndesc("已使用" + subQtyUsed);
                    logSub.setSubType("qtyChg");
                    logSub.setLogData_assetflow(subQtyUsed * -1, 0.0, "");

                }
            }

            JSONObject oItem = order.getOItem().getJSONArray("objItem").getJSONObject(index);

            LogFlow log = new LogFlow(tokData, oItem, order.getAction(),
                    order.getInfo().getId_CB(), id_O, index, "assetflow",
                    "qtyChg", oItem.getJSONObject("wrdN").getString("cn") + "已生产" + wn2qtynow, 3);

            Double logPrice = order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn4price");
//            Integer logStatus = order.getAction().getJSONArray("objAction").getJSONObject(index).getInteger("bcdStatus");
            log.setLogData_assetflow(wn2qtynow, logPrice, "");

            wsClient.sendWS(log);

            if (updateOrderResult.getModifiedCount() > 0 && bool) {
                return retResult.ok(CodeEnum.OK.getCode(), true);
            }
        }
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, DetailsEnum.ORDER_ALL_ERROR.getCode(), null);
    }

    @Override
    public ApiResponse shipNow(JSONObject tokData, String id_O, Integer index, Boolean isLink, Double qtyShip, Integer prntIndex) {
        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info", "oItem", "action", "oStock"));

        if (order.getOStock() == null) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_ALL_ERROR.getCode(), "订单不存在");
        }

        // No Qty or qty not enough
        System.out.println("isLink" + isLink);
        if (isLink &&
                (order.getOStock().getJSONArray("objData").getJSONObject(index) == null ||
                        order.getOStock().getJSONArray("objData").getJSONObject(index).getDouble("wn2qtynow") - qtyShip < 0)) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), "");
        }

//        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        qtyShip = Double.parseDouble(decimalFormat.format(qtyShip));

        JSONArray prntData = order.getAction().getJSONArray("objAction").getJSONObject(index).getJSONArray("upPrnts");
        JSONObject stockData = order.getOStock().getJSONArray("objData").getJSONObject(index);
        if (stockData.getJSONArray("objShip") == null) {
            JSONArray qtyInit = new JSONArray();
            // if no such array, create one with {wn2qtynow, need, made}
            for (int i = 0; i < prntData.size(); i++) {
                JSONObject qtyContent = new JSONObject();
                qtyContent.put("wn2qtyneed", prntData.getJSONObject(prntIndex).getDouble("wn2qtyneed"));
                qtyContent.put("wn2qtynow", 0.0);
                qtyContent.put("wn2qtymade", 0.0);
                qtyInit.add(qtyContent);
                stockData.put("objShip", qtyInit);
            }
        }

        // get current deliver status
        JSONArray objShip = stockData.getJSONArray("objShip");


        //update deliver and set it back to OStock and save
        order.getOStock().getJSONArray("objData").getJSONObject(index).put("wn2qtynow",
                order.getOStock().getJSONArray("objData").getJSONObject(index).getDouble("wn2qtynow") - qtyShip);

        objShip.getJSONObject(prntIndex).put("wn2qtynow", objShip.getJSONObject(prntIndex).getDouble("wn2qtynow") + qtyShip);
        objShip.getJSONObject(prntIndex).put("wn2qtymade", objShip.getJSONObject(prntIndex).getDouble("wn2qtymade") + qtyShip);

        JSONObject updateStock = new JSONObject();
        updateStock.put("oStock", order.getOStock());

        coupaUtil.updateOrderByListKeyVal(id_O, updateStock);

        JSONObject oItem = order.getOItem().getJSONArray("objItem").getJSONObject(index);

        LogFlow log = new LogFlow(tokData, oItem, order.getAction(),
                order.getInfo().getId_CB(), id_O, index, "assetflow",
                "qtyLoad", oItem.getJSONObject("wrdN").getString("cn")+"已领用" + qtyShip, 3);
        Double logPrice = order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn4price");
//        Integer logStatus = order.getAction().getJSONArray("objAction").getJSONObject(index).getInteger("bcdStatus");
        log.setLogData_assetflow(qtyShip, logPrice, "");

        wsClient.sendWS(log);

        return retResult.ok(CodeEnum.OK.getCode(), "");
    }

    @Override
    public Integer updateOStockPi(String id_C, String id_O, Integer index, Double wn2qtynow, String dep, String grpU, String id_U, JSONObject wrdNU, JSONArray arrTime)
    {
        JSONObject tokData = new JSONObject();
        tokData.put("dep", dep);
        tokData.put("grpU", grpU);
        tokData.put("id_U", id_U);
        tokData.put("wrdNU", wrdNU);
        tokData.put("id_C", id_C);



        this.updateOStock(id_O, index, wn2qtynow, tokData, arrTime);

        return 200;
    }


    @Override
    public ApiResponse updateOStock(String id_O, Integer index, Double wn2qtynow, JSONObject tokData, JSONArray arrTime) {

        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info", "oItem", "action", "oStock", "view"));
        JSONObject oStock = order.getOStock();
        wn2qtynow = Double.parseDouble(decimalFormat.format(wn2qtynow));

        String id_P = order.getOItem().getJSONArray("objItem").getJSONObject(index).getString("id_P");
        JSONObject object = new JSONObject();
        object.put("resvQty", new JSONObject());
        object.put("id_P", id_P == null ? "" : id_P);
        object.put("wn2qtynow", wn2qtynow);
        object.put("wn2qtymade", wn2qtynow);

        //rendering objShip here check if bmdpt == 1, if and so...
        // if this is just a process, then you will need? to get objShip ready for produceNow and Max
        if (order.getAction().getJSONArray("objAction").getJSONObject(index).getInteger("bmdpt").equals(1)) {
//            JSONObject objShip = order.getOStock().getJSONArray("objData").getJSONObject(index);
//            JSONArray objShip = order.getOStock().getJSONArray("objData").getJSONObject(index).getJSONArray("objShip");
            System.out.print("in Obj");
            if (order.getOStock() == null || order.getOStock().getJSONArray("objData") == null ||
                    order.getOStock().getJSONArray("objData").size() - 1 < index ||
                    order.getOStock().getJSONArray("objData").getJSONObject(index) == null ||
                    order.getOStock().getJSONArray("objData").getJSONObject(index).getJSONArray("objShip") == null) {
                JSONArray qtyInit = new JSONArray();
                JSONObject qtyContent = new JSONObject();
                qtyContent.put("wn2qtyneed", order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn2qtyneed"));
                qtyContent.put("wn2qtynow", wn2qtynow);
                qtyContent.put("wn2qtymade", wn2qtynow);
                qtyInit.add(qtyContent);
                object.put("objShip", qtyInit);
            } else {
                JSONObject objShip = order.getOStock().getJSONArray("objData").getJSONObject(index).getJSONArray("objShip").getJSONObject(0);
                objShip.put("wn2qtynow", wn2qtynow + objShip.getDouble("wn2qtynow"));
                objShip.put("wn2qtymade", wn2qtynow + objShip.getDouble("wn2qtymade"));
                JSONArray objShipArray = order.getOStock().getJSONArray("objData").getJSONObject(index).getJSONArray("objShip");
                objShipArray.set(0, objShip);
                object.put("objShip", objShipArray);
            }
        }

        JSONObject oStockItem = new JSONObject();

        if (oStock == null) {
            //oStock为空，创建objData数组, 添加数组对象
            JSONArray array = new JSONArray();
            array.set(index, object);

            oStockItem.put("oStock.objData", array);
            oStockItem.put("view", order.getView().add("oStock"));
        } else if (oStock.getJSONArray("objData") == null) {
            //oStock.objData为空，创建objData数组, 添加数组对象
            System.out.print("in objData=Null");

            JSONArray array = new JSONArray();
            array.set(index, object);
            oStockItem.put("oStock.objData", array);

        } else if (oStock.getJSONArray("objData").size() - 1 < index) {
            System.out.print("in objData=size - 1");

            oStockItem.put("oStock.objData." + index, object);

        } else if (oStock.getJSONArray("objData").getJSONObject(index) == null) {
            System.out.print("in objData.index == null");

            oStockItem.put("oStock.objData." + index, object);

        } else {
            if (order.getOStock().getJSONArray("objData").getJSONObject(index).getJSONArray("objShip") != null)
            {
                object.put("objShip", order.getOStock().getJSONArray("objData").getJSONObject(index).getJSONArray("objShip"));
            }

            object.put("wn2qtynow", wn2qtynow + order.getOStock().getJSONArray("objData").getJSONObject(index).getDouble("wn2qtynow"));
            object.put("wn2qtymade", wn2qtynow + order.getOStock().getJSONArray("objData").getJSONObject(index).getDouble("wn2qtynow"));

            oStockItem.put("oStock.objData." + index, object);
        }

        coupaUtil.updateOrderByListKeyVal(id_O, oStockItem);


        // if bmdpt = 3 it's a process, then directly set oStock.objShip == wn2qtynow, 1:1
        System.out.println("bmdpt" + order.getAction().getJSONArray("objAction").getJSONObject(index));

        JSONObject oItem = order.getOItem().getJSONArray("objItem").getJSONObject(index);
        JSONObject objAction = order.getAction().getJSONArray("objAction").getJSONObject(index);


        LogFlow log = new LogFlow(tokData, oItem, order.getAction(), "", id_O, index,
                "assetflow", "qtyChg", objAction.getString("refOP") + "-" + oItem.getJSONObject("wrdN").getString("cn") +
                " 生产了 " + wn2qtynow, 3);


        Double logPrice = oItem.getDouble("wn4price");
//        Integer logStatus = objAction.getInteger("bcdStatus");
        log.setLogData_assetflow(wn2qtynow, logPrice, "");

        if (arrTime.size() > 0)
        {
            log.getData().put("arrTime", arrTime);
        }

        wsClient.sendWS(log);

        return retResult.ok(CodeEnum.OK.getCode(), "");
    }

    @Override
    public ApiResponse deductQty(String id_O, Integer index, Double wn2qtynow, String id_UW, JSONObject tokData) {

        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info", "oItem", "action", "oStock", "view"));

        //All qty input turns into negative and add like updateOStock
        wn2qtynow = Double.parseDouble(decimalFormat.format(wn2qtynow * -1));

        String id_P = order.getOItem().getJSONArray("objItem").getJSONObject(index).getString("id_P");
        JSONObject object = new JSONObject();

        if (!order.getAction().getJSONArray("objAction").getJSONObject(index).getInteger("bmdpt").equals(1) ||
                id_P.equals("")) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_ALL_ERROR.getCode(), "");
        }

        JSONObject itemOStock = order.getOStock().getJSONArray("objData").getJSONObject(index);

        System.out.print("in Obj");
            if (order.getOStock() == null || order.getOStock().getJSONArray("objData") == null ||
                    order.getOStock().getJSONArray("objData").size() - 1 < index ||
                    itemOStock == null ||
                    itemOStock.getJSONArray("objShip") == null) {
                JSONArray qtyInit = new JSONArray();
                JSONObject qtyContent = new JSONObject();
                qtyContent.put("wn2qtyneed", order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn2qtyneed"));
                qtyContent.put("wn2qtynow", wn2qtynow);
                qtyContent.put("wn2qtymade", wn2qtynow);
                qtyInit.add(qtyContent);
                object.put("objShip", qtyInit);
            } else {
                JSONObject objShip = itemOStock.getJSONArray("objShip").getJSONObject(0);
                objShip.put("wn2qtynow", wn2qtynow + objShip.getDouble("wn2qtynow"));
                objShip.put("wn2qtymade", wn2qtynow + objShip.getDouble("wn2qtymade"));
                JSONArray objShipArray = itemOStock.getJSONArray("objShip");
                objShipArray.set(0, objShip);
                object.put("objShip", objShipArray);

            }

            object.put("wn2qtynow", wn2qtynow + itemOStock.getDouble("wn2qtynow"));
            object.put("wn2qtymade", wn2qtynow + itemOStock.getDouble("wn2qtynow"));

            JSONObject mapKey = new JSONObject();
            mapKey.put("oStock.objData." + index, object);
            coupaUtil.updateOrderByListKeyVal(id_O, mapKey);


        // if bmdpt = 3 it's a process, then directly set oStock.objShip == wn2qtynow, 1:1
        System.out.println("bmdpt" + order.getAction().getJSONArray("objAction").getJSONObject(index));

        JSONObject oItem = order.getOItem().getJSONArray("objItem").getJSONObject(index);
        JSONObject objAction = order.getAction().getJSONArray("objAction").getJSONObject(index);


        LogFlow log = new LogFlow(tokData, oItem, order.getAction(), "", id_O, index,
                "assetflow", "qtyChg", objAction.getString("refOP") + " - " + oItem.getJSONObject("wrdN").getString("cn") +
                " 管理员减去 " + wn2qtynow, 3);

        log.setId_U(id_UW);
        log.getData().put("id_UM", tokData.getString("id_U"));
        Double logPrice = oItem.getDouble("wn4price");
//        Integer logStatus = objAction.getInteger("bcdStatus");
        log.setLogData_assetflow(wn2qtynow, logPrice, "");

        wsClient.sendWS(log);


        return retResult.ok(CodeEnum.OK.getCode(), "");
    }

    // Manager updateOStock API
    // 1. id_O + index + tokData + id_U + qtynow
    // Vue -> get id_Us from oItemData, pick the id_U(wrdNU) that need to adjust qtynow
    // updateOStock, must be bmdpt = 1
    // when send log, send id_U = picked adjuster, data.id_UM = tokData.id_U


//    @Override
//    public ApiResponse producedClear(String id_U, String id_C, String listType, String grp, String id_O, Integer index) {
////        authCheck.getUserUpdateAuth(id_U, id_C, listType, grp, "batch", new JSONArray().fluentAdd(""));
//        Query queryOrder = new Query(new Criteria("_id").is(id_O));
//        queryOrder.fields().include("action");
//        Order order = mongoTemplate.findOne(queryOrder, Order.class);
//        JSONArray arraySubPart = order.getAction().getJSONArray("objAction").getJSONObject(index).getJSONArray("subParts");
//        JSONArray arrayClear = new JSONArray();
//        for (int i = 0; i < arraySubPart.size(); i++) {
//            JSONObject jsonSubPart = arraySubPart.getJSONObject(i);
//            Query query = new Query(new Criteria("_id").is(jsonSubPart.getString("id_O")));
//            Update update = new Update();
//            update.set("oStock.objData." + jsonSubPart.getInteger("index") + ".wn2qtynow", 0);
//            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Order.class);
//        }
//        JSONObject jsonLog = new JSONObject();
//        jsonLog.put("id_U", id_U);
//        jsonLog.put("id_C", id_C);
////        jsonLog.put("id_O", id_O);
////        jsonLog.put("index", index);
//        jsonLog.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//        jsonLog.put("clear", arraySubPart);
////        HashSet setId_O = new HashSet();
////        for (int i = 0; i < arraySubPart.size(); i++) {
////            JSONObject jsonSubPart = arraySubPart.getJSONObject(i);
////            setId_O.add(jsonSubPart.getString("id_O"));
////        }
////        Query query = new Query(new Criteria("_id").in(setId_O));
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }

    @Override
    @Transactional
    public Object inventory(String id_C, String locAddr, JSONArray arrayLoc) throws IOException {
        //arrayLoc
        //[{"id_P":"", "locSpace":[0,1], "spaceQty":[5,5]}]
        JSONArray arrayLocSpaceTotal = new JSONArray();
        HashSet<Integer> setLocSpaceTotal = new HashSet();
        JSONArray arrayId_P = new JSONArray();
        for (int i = 0; i < arrayLoc.size(); i++) {

            JSONObject jsonLoc = arrayLoc.getJSONObject(i);
            JSONArray arrayLocSpace = jsonLoc.getJSONArray("locSpace");
            HashSet setLocSpace = JSON.parseObject(JSON.toJSONString(arrayLocSpace), HashSet.class);
            arrayLocSpaceTotal.addAll(arrayLocSpace);
            setLocSpaceTotal.addAll(setLocSpace);
            arrayId_P.add(jsonLoc.getString("id_P"));

        }
        System.out.println("arrayLocSpaceTotal=" + arrayLocSpaceTotal);
        System.out.println("setLocSpaceTotal=" + setLocSpaceTotal);
        //同一个格子放入不同产品
        if (arrayLocSpaceTotal.size() != setLocSpaceTotal.size()) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_OCCUPIED.getCode(), null);
        }
        isLocCorrect(id_C, locAddr, arrayLocSpaceTotal);

        String id_O = MongoUtils.GetObjectId();
        JSONObject jsonWrdN = new JSONObject();
        jsonWrdN.put("cn", "盘点");
        OrderInfo orderInfo = new OrderInfo(id_C, id_C, id_C, id_C, "", "", "", "1000", "1000", 1.0, "", 7, 0, jsonWrdN, null);

        JSONObject jsonOItem = new JSONObject();
        jsonOItem.put("noPrice", true);
        JSONArray arrayObjItem = new JSONArray();
        JSONObject jsonOStock = new JSONObject();
        jsonOStock.put("sumQty", arrayLoc.size());
        JSONArray arrayObjStock = new JSONArray();

        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < arrayLoc.size(); i++) {
            JSONObject jsonLoc = arrayLoc.getJSONObject(i);
            String id_P = jsonLoc.getString("id_P");
            JSONArray arrayLocSpace = jsonLoc.getJSONArray("locSpace");
            JSONArray arraySpaceQty = jsonLoc.getJSONArray("spaceQty");
            //获取产品信息
            Prod prod = (Prod) dbUtils.getMongoOneField(id_P, "info", Prod.class);
            ProdInfo prodInfo = prod.getInfo();
            System.out.println("prodInfo=" + prodInfo);
            //获取to货架该产品信息
            JSONObject jsonToHit = getLocSpace(id_C, id_P, locAddr, arrayLocSpace);
            System.out.println("jsonToHit=" + jsonToHit);
            String toHitId = jsonToHit.getString("_id");
            jsonToHit.remove("_id");
            System.out.println("jsonToHit=" + jsonToHit);

            Double toSum = 0.0;
            //to货架没有这种产品
            if (toHitId.equals("isEmpty")) {
                for (int j = 0; j < arraySpaceQty.size(); j++) {
                    toSum += arraySpaceQty.getDouble(j);
                }
                AssetAStock assetAStock = new AssetAStock(locAddr, arrayLocSpace, arraySpaceQty);

                JSONObject jsonInfo = new JSONObject();
                jsonInfo.put("id_P", id_P);
                jsonInfo.put("wrdN", prodInfo.getWrdN());
                jsonInfo.put("wrddesc", prodInfo.getWrddesc());
                System.out.println("jsonInfo=" + jsonInfo);

                String id_A = MongoUtils.GetObjectId();
                System.out.println("assetId=" + id_A);
                AssetInfo assetInfo = new AssetInfo(id_C, id_C, id_P, prodInfo.getWrdN(),
                        prodInfo.getWrddesc(), "1000", "", "", 2);
                assetInfo.setWn2qty(toSum);
                JSONArray arrayView = new JSONArray();
                arrayView.add("info");
                arrayView.add("aStock");
                Asset asset = new Asset();
                asset.setId(id_A);
                asset.setInfo(assetInfo);
                asset.setView(arrayView);
                asset.setAStock((JSONObject) JSON.toJSON(assetAStock));
                mongoTemplate.insert(asset);

                lSAsset lsasset = new lSAsset(id_A, id_C, id_C, id_P, toSum, prodInfo.getWrdN(),
                        prodInfo.getWrddesc(), "1000", prodInfo.getPic(), prodInfo.getRef(), 2);
                lsasset.setLocAddr(locAddr);
                lsasset.setLocSpace(arrayLocSpace);
                lsasset.setSpaceQty(arraySpaceQty);
                bulkRequest.add(new IndexRequest("lSAsset").source(lsasset));
                createAsset(id_C, jsonInfo, assetAStock, toSum);
            }
            //货架要放的格子不为空且只有一种产品
            else {
                //格子产品不同
                if (!id_P.equals(jsonToHit.getString("id_P"))) {
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.SPACE_OCCUPIED.getCode(), null);
                }
                //相同
                JSONArray arrayToLocSpace = jsonToHit.getJSONArray("locSpace");
                JSONArray arrayToSpaceQty = jsonToHit.getJSONArray("spaceQty");
                System.out.println("arrayToSpaceQty=" + arrayToSpaceQty);
                for (int j = 0; j < arrayToSpaceQty.size(); j++) {
                    System.out.println("value" + arrayToSpaceQty.getDouble(j) + "type=" + arrayToSpaceQty.get(j).getClass().getSimpleName());
                }
                Boolean bool;
                //to货架要放的格子
                for (int j = 0; j < arrayLocSpace.size(); j++) {
                    bool = true;
                    //to货架这种产品的格子
                    for (int k = 0; k < arrayToLocSpace.size(); k++) {
                        //找到to货架要放的格子，修改为false，默认为true
                        if (arrayLocSpace.getInteger(j) == arrayToLocSpace.getInteger(k)) {
                            bool = false;
                            arrayToSpaceQty.set(k, arrayToSpaceQty.getDouble(k) + arraySpaceQty.getDouble(j));
                        } else if (j == 0) {
                            arrayToSpaceQty.set(k, arrayToSpaceQty.getDouble(k));
                        }
                    }
                    //找不到to货架要放的格子，新增格子数组和数量数组的数组元素
                    if (bool) {
                        arrayToLocSpace.add(arrayLocSpace.getInteger(j));
                        arrayToSpaceQty.add(arraySpaceQty.getDouble(j));
                    }
                    toSum += arraySpaceQty.getDouble(j);
                }
                System.out.println("arrayToSpaceQty=" + arrayToSpaceQty);
                for (int j = 0; j < arrayToSpaceQty.size(); j++) {
                    System.out.println("value" + arrayToSpaceQty.getDouble(j) + "type=" + arrayToSpaceQty.get(j).getClass().getSimpleName());
                }
                Double toRemain = jsonToHit.getDouble("wn2qty") + toSum;
                jsonToHit.put("wn2qty", toRemain);

                JSONObject jsonUpdate = new JSONObject();
                jsonUpdate.put("info.wn2qty", toRemain);
                jsonUpdate.put("aStock.locSpace", arrayToLocSpace);
                jsonUpdate.put("aStock.spaceQty", arrayToSpaceQty);
                UpdateResult toUpdateResult = dbUtils.setMongoValues(jsonToHit.getString("id_A"), jsonUpdate, Asset.class);
                if (toUpdateResult.getModifiedCount() == 0) {
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ALL_ERROR.getCode(), null);
                }
                bulkRequest.add(new UpdateRequest("lSAsset", toHitId).doc(jsonToHit, XContentType.JSON));
            }

            OrderOItem orderOItem = new OrderOItem(id_P, id_C, id_P, id_C, prodInfo.getWrdN(), prodInfo.getWrddesc(),
                    null, "1000", "1000", "", "", "", 0, 0,
                    toSum, 0.0, 1);
            if (i == 0) {
                orderOItem.setSeq("0");
            }
            arrayObjItem.add(orderOItem);

            JSONObject jsonObjStock = new JSONObject();
            jsonObjStock.put("wn2qtynow", 0);
            jsonObjStock.put("wn2qtymade", toSum);
            jsonObjStock.put("id_P", id_P);
            arrayObjStock.add(jsonObjStock);

        }
        System.out.println("arrayObjItem=" + arrayObjItem);
        jsonOItem.put("objItem", arrayObjItem);
        jsonOStock.put("objData", arrayObjStock);

        JSONArray arrayView = new JSONArray();
        arrayView.add("info");
        arrayView.add("oItem");
        arrayView.add("oStock");

        Order order = new Order();
        order.setId(id_O);
        order.setInfo(orderInfo);
        order.setOItem(jsonOItem);
        order.setOStock(jsonOStock);
        order.setView(arrayView);

        mongoTemplate.insert(order);
        System.out.println("id_O=" + id_O);
        lSBOrder lsborder = new lSBOrder(id_C, id_C, id_C, id_C, "", id_O, arrayId_P, "", "",
                "1000", "1000", "", 4, 0,
                orderInfo.getWrdN(), orderInfo.getWrddesc(), null);

        bulkRequest.add(new IndexRequest("lsborder").source((JSONObject) JSON.toJSON(lsborder)));
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse getFromStock(String myId_C, String id_O) throws IOException {

        //1. make sure this order is id_C != myself, and id_CB == myself
        //2. wrdN + [use stock] and id_C change to myself
        //3. update info Card and lsborder

        Order order = coupaUtil.getOrderByListKey(id_O, Collections.singletonList("info"));

        OrderInfo orderInfo = order.getInfo();

        if (orderInfo.getId_C().equals(myId_C) || !orderInfo.getId_CB().equals(myId_C))
        {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_ALL_ERROR.getCode(), null);
        }


        JSONObject wrdN = new JSONObject();
        wrdN.put("cn", orderInfo.getWrdN().getString("cn").replace("采购", "领料")+" [用库存]");

        JSONObject listCol = new JSONObject();
        listCol.put("id_C", myId_C);
        listCol.put("wrdN", wrdN);
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("id_O", id_O));
        dbUtils.updateListCol(queryBuilder, "lsborder", listCol);


        orderInfo.setId_C(myId_C);
        orderInfo.setWrdN(wrdN);
        JSONObject mapKey = new JSONObject();
        mapKey.put("info", orderInfo);
        coupaUtil.updateOrderByListKeyVal(id_O, mapKey);


        return retResult.ok(CodeEnum.OK.getCode(), null);

    }
}