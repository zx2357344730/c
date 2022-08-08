package com.cresign.details.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.details.client.WSClient;
import com.cresign.details.enumeration.DetailsEnum;
import com.cresign.details.service.SingleService;
import com.cresign.details.utils.UpdateSingleUtil;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.logger.LogUtil;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.es.lBProd;
import com.cresign.tools.pojo.es.lSBComp;
import com.cresign.tools.pojo.po.*;
import com.cresign.tools.pojo.po.compCard.CompInfo;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.cresign.tools.pojo.po.prodCard.ProdInfo;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class SingleServiceImpl implements SingleService {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private UpdateSingleUtil updateSingleUtil;

    @Autowired
    private RetResult retResult;

    @Autowired
    private AuthCheck authCheck;

    @Autowired
    private WSClient wsClient;

    @Autowired
    private LogUtil logUtil;

    /**
     * 注入redis数据库下标1模板
     */
    @Resource
    private StringRedisTemplate redisTemplate1;

    private static final String QD_Key = "qdKey";

    @Override
    public ApiResponse getSingle(String id, String listType, String id_C, String reqJsonC, Integer tvs, String id_U, String grp, String grpU) {

//        // 权限校验
//        JSONObject reqJson = new JSONObject();

        //先查询这个公司有没有这个人，没有的话grp默认1099
//        JSONObject rolex = MongoUtils.getRolex(id_U, id_C, mongoTemplate);

//        if (rolex == null){
//            reqJson.put("grp", "1099");
//        } else {
//            reqJson.put("grp", grp);
//        }
//
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", listType);
//        reqJson.put("authType", "card");

//        // 查询条件
//        Query query = new Query(new Criteria("_id").is(id));

        // THIS IS WRONG
        // lNUser must only check id_U
        // lNComp must only check id_C
        // no X card now

        // very simple, if reqC != tokC
        // check if I am anybody in that comp

        if (!id_C.equals(reqJsonC))
        {
            grpU = this.getMyGrpU(id_U, reqJsonC);
        }

        JSONArray authModuleResult = new JSONArray();

        if (listType.equals("lNComp")) {
            Query query = new Query(new Criteria("_id").is(id_C));

            Comp comp = mongoTemplate.findOne(query, Comp.class);

            if (ObjectUtils.isEmpty(comp)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }

            return retResult.ok(CodeEnum.OK.getCode(), comp);

        }
        else if (listType.equals("lNUser")) {
            Query query = new Query(new Criteria("_id").is(id_U));
            //返回结果
            User user = mongoTemplate.findOne(query, User.class);
            if (ObjectUtils.isEmpty(user)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            return retResult.ok(CodeEnum.OK.getCode(), user);
        }
        else if ("lBProd".equals(listType)) {
            Query query = new Query(new Criteria("_id").is(id));
            query.fields().include("info");
            // 先获取prod
            JSONObject checkProdOwner = (JSONObject) JSON.toJSON(mongoTemplate.findOne(query, Prod.class));
            if (ObjectUtils.isEmpty(checkProdOwner) ) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            String grpS = checkProdOwner.getJSONObject("info").getString("grp");
            // 零件的id_C
            String other_id_C = checkProdOwner.getJSONObject("info").getString("id_C");

            // 知道对方公司是否是真公司
            int judgeComp = dbUtils.judgeComp(id_C,other_id_C);
            System.out.println("judgeComp"+judgeComp);
            // 初始化校验参数
            // 如果对方公司是真公司
            if (judgeComp == 1) {
                grpU = this.getMyGrpU(id_U, other_id_C);
                authModuleResult = authCheck.getUserSelectAuth(id_U,other_id_C, grpU, "lSProd",grpS,"card");
            } else {
                authModuleResult = authCheck.getUserSelectAuth(id_U,id_C,grpU, listType,grp,"card");
            }
            // 将权限的卡片放入查询语句，限制返回卡片字段
            for (Object cardRef : authModuleResult) {
                //x卡片只取当前公司的数据
                if (cardRef.toString().substring(cardRef.toString().length()-1).equals("x")){
                    query.fields().include(cardRef.toString()+"."+id_C);
                }else{
                    query.fields().include(cardRef.toString());
                }
            }
            query.fields().include("view");
            query.fields().include("tvs");

            // 将权限的卡片放入查询语句，限制返回卡片字段
            Prod prod = mongoTemplate.findOne(query, Prod.class);
            if (ObjectUtils.isEmpty(prod)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            return retResult.ok(CodeEnum.OK.getCode(), prod);
        }
        else if (listType.equals("lBComp") || listType.equals("lSComp")) {
            Query query = new Query(new Criteria("_id").is(id));

            // 知道对方公司是否是真公司
            int judgeComp = dbUtils.judgeComp( id_C, id);
            // 如果对方公司是真公司
            if (judgeComp == 1) {
//                grpU = this.getMyGrpU(id_U, id);
                authModuleResult = authCheck.getUserSelectAuth(id_U,id, "1099",listType,grp,"card");
            } else {
                authModuleResult = authCheck.getUserSelectAuth(id_U,id_C,grpU, listType,grp,"card");
            }

            // 将权限的卡片放入查询语句，限制返回卡片字段
            for (Object cardRef : authModuleResult) {
                //x卡片只取当前公司的数据
                if (cardRef.toString().substring(cardRef.toString().length()-1).equals("x")){
                    query.fields().include(cardRef.toString()+"."+id_C);
                }else{
                    query.fields().include(cardRef.toString());
                }
            }
            query.fields().include("view");
            query.fields().include("tvs");

            // 将权限的卡片放入查询语句，限制返回卡片字段
            Comp compOne = mongoTemplate.findOne(query, Comp.class);

            if (ObjectUtils.isEmpty(compOne)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }

            return retResult.ok(CodeEnum.OK.getCode(), compOne);
        }
        else if(listType.equals("lBOrder") || listType.equals("lSOrder")){
            Query query = new Query(new Criteria("_id").is(id));

            authModuleResult = authCheck.getUserSelectAuth(id_U,id_C, grpU, listType,grp,"card");

            // 将权限的卡片放入查询语句，限制返回卡片字段
            for (Object cardRef : authModuleResult) {
                //x卡片只取当前公司的数据
                if (cardRef.toString().substring(cardRef.toString().length()-1).equals("x")){
                    query.fields().include(cardRef.toString()+"."+id_C);
                }else{
                    query.fields().include(cardRef.toString());
                }
            }
            query.fields().include("view");
            query.fields().include("tvs");

            // 将权限的卡片放入查询语句，限制返回卡片字段
            Order orderOne = mongoTemplate.findOne(query, Order.class);
            if (ObjectUtils.isEmpty(orderOne)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            return retResult.ok(CodeEnum.OK.getCode(), orderOne);
        }
        else if(listType.equals("lBUser")) {
            Query query = new Query(new Criteria("_id").is(id));

            authModuleResult = authCheck.getUserSelectAuth(id_U,id_C, grpU, listType,grp,"card");

            // 将权限的卡片放入查询语句，限制返回卡片字段
            for (Object cardRef : authModuleResult) {
                //x卡片只取当前公司的数据
                if (cardRef.toString().substring(cardRef.toString().length()-1).equals("x")){
                    query.fields().include(cardRef.toString()+".objComp."+id_C);
                }else if (cardRef.toString().equals("role")){
                }else{
                    query.fields().include(cardRef.toString());
                }
            }
            query.fields().include("view");
            query.fields().include("tvs");

            //返回结果
            User user = mongoTemplate.findOne(query, User.class);
            if (ObjectUtils.isEmpty(user)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            return retResult.ok(CodeEnum.OK.getCode(), user);
        }
        else {  //lSProd lSAsset
            Query query = new Query(new Criteria("_id").is(id));

            authModuleResult = authCheck.getUserSelectAuth(id_U,id_C,grpU,listType,grp,"card");

            // 将权限的卡片放入查询语句，限制返回卡片字段
            for (Object cardRef : authModuleResult) {
                //x卡片只取当前公司的数据
                if (cardRef.toString().substring(cardRef.toString().length()-1).equals("x")){
                    query.fields().include(cardRef.toString()+"."+id_C);
                }else if (cardRef.toString().equals("role")){
                }else{
                    query.fields().include(cardRef.toString());
                }
            }
            query.fields().include("view");
            query.fields().include("tvs");

            if (listType.equals("lSProd")) {
                Prod prod = mongoTemplate.findOne(query, Prod.class);

                if (ObjectUtils.isEmpty(prod)) {
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
                }
                if (prod.getInfo() == null &&
                        prod.getInfo().getGrp() == null &&
                        !grp.equals(prod.getInfo().getGrp()) ){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.GRP_NOT_MATCH.getCode(), null);
                }
                return retResult.ok(CodeEnum.OK.getCode(), prod);

            } else if ("lSAsset".equals(listType)) {

                Asset asset = mongoTemplate.findOne(query, Asset.class);
                if (ObjectUtils.isEmpty(asset)) {
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
                }
                if (asset.getInfo() == null &&
                        asset.getInfo().getGrp() == null &&
                        !grp.equals(asset.getInfo().getGrp()) ){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.GRP_NOT_MATCH.getCode(), null);
                }
                return retResult.ok(CodeEnum.OK.getCode(), asset);
            }
        }

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }

    private String getMyGrpU(String id_U, String id_C)
    {
        JSONObject jsonRolex = MongoUtils.getRolex(id_U, id_C, mongoTemplate);
        System.out.println("jsonRolex=" + jsonRolex);
        if (jsonRolex != null) {
            return jsonRolex.getString("grpU");
        } else {
            return "1099";
        }
    }

// need to check Auth from redis
/*
 these above auth code need to use in updateSingle above to fix up impChgList
        JSONObject reqJson = new JSONObject();
        reqJson.put("id_U", id_U);
        reqJson.put("id_C", id_C);
        reqJson.put("listType", listType);
        reqJson.put("grp", grp);
        reqJson.put("authType", authType);//卡片/按钮
        reqJson.put("params", impChgList);//卡片名称/按钮名称
        String authModuleResult = authFilterClient.getUserUpdateAuth(reqJson);
        JSONObject authModuleJson = JSONObject.parseObject(authModuleResult);

        // 返回的结果
        JSONArray authJson = authModuleJson.getJSONArray("result");

        //JSONArray转List
        List<JSON> list = JSONObject.parseArray(authJson.toJSONString(), JSON.class);
        //List转HashSet
        Set<JSON> authSet = new HashSet<>(list);
        //List转HashSet
        Set<String> impChgSet = new HashSet<>(impChgList);

        //交集
        authSet.retainAll(impChgSet);

        if (authJson.size() <= 0) {
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
        } else {
            //
            return updateSingleUtil.UpdateSingle(ID_, id_U, id_C, data, authSet,listCol, listType,grp);
        }
*/

    @Override
    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
    public ApiResponse updateSingle(String id_C, JSONObject data, String listType, String impChg,JSONObject listCol, String id_U, String grp, String authType) throws IOException {

        // use Auth to retainAll impChgSet

        String ID_ = data.getString("id");
        String lang = request.getHeader("lang"); //get Header Lang
        List<String> impChgList = Arrays.asList(impChg.split(","));
        Set<String> cardList;

        if (StringUtils.isEmpty(impChg)) {
            return retResult.ok(CodeEnum.OK.getCode(), "");
        }

        if ("lSProd".equals(listType) || "lBProd".equals(listType)) {

            Query prodQ = new Query(new Criteria("_id").is(ID_));
            prodQ.fields().include("info");
            prodQ.fields().include("tvs");

            Prod prod = mongoTemplate.findOne(prodQ, Prod.class);
            //Check exist
            if (ObjectUtils.isEmpty(prod)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            //Check tvs not match means someone already updated
            if (!prod.getTvs().equals(data.getInteger("tvs"))) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ALREADY_UPDATED.getCode(), "");

            }
            if ("lSProd".equals(listType) && prod.getInfo() == null &&
                    prod.getInfo().getGrp() == null &&
                    !grp.equals(prod.getInfo().getGrp())){
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.GRP_NOT_MATCH.getCode(), null);
            }

            cardList = new HashSet<>(impChgList);

            //If lBProd check id_C bcdNet, if real, only can edit x cards
            //Check if id_C == id_CB == Myself

            if ("lBProd".equals(listType) && !prod.getInfo().getId_C().equals(id_C)) {
                Query compQ = new Query(new Criteria("_id").is(prod.getInfo().getId_C()));
                compQ.fields().include("bcdNet");

                Comp prodOwner = mongoTemplate.findOne(compQ, Comp.class);
                if (prodOwner.getBcdNet() == 1) {
                    // if Prod's owner Company is Real, you can only update X cards
                    System.out.println("REAL:::: Product owner is ");
                    cardList = new HashSet<>();
                    for (int i = 0; i < impChgList.size(); i++) {
                        if (impChgList.get(i).endsWith("x") || impChgList.get(i).equals("view")) {
                            cardList.add(impChgList.get(i));
                        }
                    }
                }
            }
            return updateSingleUtil.ProdUpdateSingle(ID_, id_C, data, cardList, listCol, listType, lang);
        }
        else if ("lBOrder".equals(listType) || "lSOrder".equals(listType)) {
            Query orderQ = new Query(new Criteria("_id").is(ID_));
            orderQ.fields().include("info");
            orderQ.fields().include("tvs");

            Order order = mongoTemplate.findOne(orderQ, Order.class);

            //Check if Order id_ actually exist, it may get deleted...
            if (ObjectUtils.isEmpty(order)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            //Check tvs not match means someone already updated
            if (!order.getTvs().equals(data.getInteger("tvs"))) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ALREADY_UPDATED.getCode(), "");

            }

            //check if it's already confirmed?
//            if (order.getInfo().getLST() >= 8){
//                throw new ErrorResponseException(HttpStatus.FORBIDDEN, DetailsEnum.CONFIRMED_CANNOT_BE_DEL.getCode(), null);
//            }
            //check grp matching
            if ("lBOrder".equals(listType) && order.getInfo() == null &&
                        order.getInfo().getGrpB() == null &&
                        !grp.equals(order.getInfo().getGrpB())){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.GRP_NOT_MATCH.getCode(), null);
                }
            else if ("lSOrder".equals(listType) && order.getInfo() == null &&
                        order.getInfo().getGrp() == null &&
                        !grp.equals(order.getInfo().getGrp())){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.GRP_NOT_MATCH.getCode(), null);
                }

            //All checking passed

            cardList = new HashSet<> (impChgList);

            return updateSingleUtil.OrderUpdateSingle(ID_, id_C, data, cardList, listCol, listType, lang);
        }
        else if ("lBComp".equals(listType) || "lSComp".equals(listType)) {
            Query compQ = new Query(new Criteria("_id").is(ID_));
            compQ.fields().include("bcdNet");
            compQ.fields().include("tvs");

            Comp comp = mongoTemplate.findOne(compQ, Comp.class);
            if (ObjectUtils.isEmpty(comp)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            if (!comp.getTvs().equals(data.getInteger("tvs"))) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ALREADY_UPDATED.getCode(), "");

            }
//
            if (comp.getBcdNet() == 0) {
                cardList = new HashSet<>(impChgList);
            } else {
        // if company is Real, you can only update X cards
                cardList = new HashSet<>();
                for (int i = 0; i < impChgList.size(); i++) {
                    if (impChgList.get(i).endsWith("x") || impChgList.get(i).equals("view")) {
                        cardList.add(impChgList.get(i));
                    }
                }
            }
            return updateSingleUtil.CompUpdateSingle(ID_, id_C, data, cardList,listCol, listType, lang);
        }
        else if ("lBUser".equals(listType)) {
            Query userQ = new Query(new Criteria("_id").is(ID_));
            userQ.fields().include("tvs");

            User user = mongoTemplate.findOne(userQ, User.class);
            if (ObjectUtils.isEmpty(user)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            if (!user.getTvs().equals(data.getInteger("tvs"))) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ALREADY_UPDATED.getCode(), "");

            }
            // lBUser can ONLY edit x cards, so filter all non-x
            cardList = new HashSet<>();
            for (int i = 0; i < impChgList.size(); i++) {
                if (impChgList.get(i).endsWith("x") || impChgList.get(i).equals("view")) {
                    cardList.add(impChgList.get(i));
                }
            }
            return updateSingleUtil.UserUpdateSingle(ID_, id_C, data, cardList, listCol, listType, lang);
        }
        else if ("lSAsset".equals(listType)) {
            Query userQ = new Query(new Criteria("_id").is(ID_));
            userQ.fields().include("tvs");

            Asset asset = mongoTemplate.findOne(userQ, Asset.class);
            if (ObjectUtils.isEmpty(asset)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.NOT_FOUND_SINGLE_MSG.getCode(), null);
            }
            if (!asset.getTvs().equals(data.getInteger("tvs"))) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ALREADY_UPDATED.getCode(), "");
            }
            // lSAsset = lSProd, can update anything as long as auth ok
            cardList = new HashSet<>(impChgList);
            return updateSingleUtil.AssetUpdateSingle(ID_, id_C, data, cardList, listCol, listType, lang);
        }
        // not possible to update Asset??? yes but very complicate

        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }


    // update ALL ES with lBUser = me + update mdb.user._id = me, ALL, cannot update X cards
    @Override
    public ApiResponse updateMyUserDetail(String id_U, JSONObject upJson, JSONObject listCol,String impChg) throws IOException {

        Update update = new Update();
        Query userQ = new Query(new Criteria("_id").is(id_U));
        User userUpdating = mongoTemplate.findOne(userQ, User.class);

        if (ObjectUtils.isEmpty(userUpdating)) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.USER_NOT_FOUND.getCode(), null);
        }

        // 2.判断是否版本号不一致
        if (userUpdating.getTvs() != upJson.get("tvs")) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ALREADY_UPDATED.getCode(), null);
        }

        String[] impChgList = impChg.split(",");

        // 3.修改卡片, 非 X card
        for (String cardPicked : impChgList) {

            //x卡片处理 skip it
            if (cardPicked.endsWith("x")){

            }else{
                // putting regular cards into regular JSON
                update.set(cardPicked, upJson.get(cardPicked));
            }
        }
        try {
            if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                update.inc("tvs", 1);
                mongoTemplate.updateFirst(userQ, update, User.class);
            }
        } catch (RuntimeException e) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.UPDATE_SINGLE_ERROR.getCode(), null);
        }

        // Start to update, first ES updating
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("id_U", id_U));
        dbUtils.updateListCol(queryBuilder, "lbuser", listCol);

        /////////////////////////////////////////

        return retResult.ok(CodeEnum.OK.getCode(), impChgList);
    }


    // update all lSBComp with me as id_C / id_CB, no ref&grp, only pic, desc, wrdN
    @Override
    public ApiResponse updateMyCompDetail(String id_C, String id_U, String grpU, JSONObject upJson, JSONObject listColC, JSONObject listColCB, String impChg) throws IOException
    {

            if (!"1001".equals(grpU)) {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
            }
////
            Update update = new Update();
            Query compQ = new Query(new Criteria("_id").is(id_C));
            Comp compUpdating = mongoTemplate.findOne(compQ, Comp.class);

            // 1.先查询该公司是否存在
            if (ObjectUtils.isEmpty(compUpdating)) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.COMP_NOT_FOUND.getCode(), null);
            }

            // 2.判断是否版本号不一致
            if (compUpdating.getTvs() != upJson.get("tvs")) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ALREADY_UPDATED.getCode(), null);
            }

            String[] impChgList = impChg.split(",");


            // 3.修改卡片, 非 X card
            // Then updating Mongo
            for (String cardPicked : impChgList) {

                //x卡片处理 skip it
                if (cardPicked.endsWith("x")){

                }else{
                    // putting regular cards into regular JSON
                    update.set(cardPicked, upJson.get(cardPicked));
//                    update.set(cardPicked, upJson.getJSONObject(cardPicked));
                }
            }
            try {
                if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                    update.inc("tvs", 1);
                    mongoTemplate.updateFirst(compQ, update, Comp.class);
                }
            } catch (RuntimeException e) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.UPDATE_SINGLE_ERROR.getCode(), null);
            }


            // Start to update, first ES updating
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchQuery("id_C", id_C));
            dbUtils.updateListCol(queryBuilder, "lsbcomp", listColC);

            QueryBuilder queryBuilder2 = QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchQuery("id_CB", id_C));
            dbUtils.updateListCol(queryBuilder2, "lsbcomp", listColCB);

            /////////////////////////////////////////

            return retResult.ok(CodeEnum.OK.getCode(), impChgList);

    }



    /**
     * 根据 id 选取哪个表的单个内容，根据 key 获取其单个键值
     *
     * ##return:
     * @throws IllegalAccessException
     */
//    @Override
//    public String getSingleMini(String id_U, Map<String, Object> reqJson) {
//
//
//        // 查询条件
//        Query query = new Query(new Criteria("_id").is(
//                reqJson.get("id")));
//
//
//            List<String> keyArray = (List<String>) reqJson.get("key");
//
//            // 根据 key 来限制只查看那个键值
//            for (int i = 0; i < keyArray.size(); i++) {
//
//                query.fields().include(keyArray.get(i));
//
//            }
//            // 判断传过来的类型
//            if (reqJson.get("listType").equals("lBOrder") || reqJson.get("listType").equals("lSOrder")) {
//
//                //返回结果
//                Order order = mongoTemplate.findOne(query, Order.class);
//
//                // 判断查询结果是否为空
//                if (order != null) {
//
//                    return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), JSON.toJSONString(order));
//
//                } else {
//
//                    return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.OK.getCode(), null);
//
//                }
//
//            } else if (reqJson.get("listType").equals("lBComp") || reqJson.get("listType").equals("lSComp")) {
//
//                //返回结果
//                Comp comp = mongoTemplate.findOne(query, Comp.class);
//
//                if (comp != null) {
//
//                    return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), JSON.toJSONString(comp));
//
//                } else {
//
//                    return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.OK.getCode(), null);
//
//                }
//            } else if (reqJson.get("listType").equals("lBUser")) {
//
//                //返回结果
//                User user = mongoTemplate.findOne(query, User.class);
//
//                if (user != null) {
//
//                    return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), JSON.toJSONString(user));
//
//                } else {
//
//                    return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.OK.getCode(), null);
//
//                }
//
//            } else if (reqJson.get("listType").equals("lBProd") || reqJson.get("listType").equals("lSProd")) {
//
//                //返回结果
//                Prod prod = mongoTemplate.findOne(query, Prod.class);
//
//                if (prod != null) {
//
//                    return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), JSON.toJSONString(prod));
//
//                } else {
//
//                    return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.OK.getCode(), null);
//
//                }
//
//            } else if (reqJson.get("listType").equals("lSAsset")) {
//
//                Query queryAsset = new Query();
//
//
//                //返回结果
//                Asset asset = mongoTemplate.findOne(query, Asset.class);
//
//                if (asset != null) {
//
//                    return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), JSON.toJSONString(asset));
//
//                } else {
//
//                    return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.OK.getCode(), null);
//
//                }
//
//            }
//        }
//
//        if (reqJson.get("listType").equals("lNComp")) {
//
//            //返回结果
//            Comp comp = mongoTemplate.findOne(query, Comp.class);
//
//            if (comp != null) {
//
//                return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), JSON.toJSONString(comp));
//
//            } else {
//
//                return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.OK.getCode(), null);
//
//            }
//
//        } else if (reqJson.get("listType").equals("lSUser")) {
//
//            //返回结果
//            User user = mongoTemplate.findOne(query, User.class);
//
//            if (user != null) {
//
//                return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), JSON.toJSONString(user));
//
//            } else {
//
//                return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.OK.getCode(), null);
//
//            }
//
//        }
//
//        return RetResult.errorJsonResult(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//
//    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse addEmptyCoup(String id_U,String grp ,String id_C, String listType, String data) throws IOException {

        authCheck.getUserUpdateAuth(id_U,id_C,listType,grp,"batch",new JSONArray().fluentAdd("add"));
            //生成objectid
            String id = MongoUtils.GetObjectId();

            if (listType.equals("lSAsset")) {
                //refAuto卡片方法
                String ref = refAuto(id_C, listType, grp);
                JSONObject resultJson = addlSAsset(id_C, id_U, id,ref, data);

                if (resultJson.get("boolean").equals("true")){
                    return retResult.ok(CodeEnum.OK.getCode(),resultJson.get("reason"));

                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("comp对象为空")){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

                }else{
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(), null);

                }


            }
            else if (listType.equals("lSProd")) {

                String ref = refAuto(id_C, listType, grp);

                JSONObject resultJson = addlSProd(id_C, id_U, id,ref, data);
                if (resultJson.get("boolean").equals("true")){
                    return retResult.ok(CodeEnum.OK.getCode(),resultJson.get("reason"));
                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("comp对象为空")){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

                }else{
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.LSPROD_ADD_ERROR.getCode(), null);

                }
            }
            else if (listType.equals("lBProd")) {
                //KEV bcdNet need fix
//                String ref = refAuto(id_C, listType, grp);
                String ref = "";
                JSONObject resultJson = addlBProd(id_C, id_U, id,ref, data);
                if (resultJson.get("boolean").equals("true")){
                    return retResult.ok(CodeEnum.OK.getCode(),resultJson.get("reason"));
                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("comp对象为空")){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("添加零件失败,bcdNet=1,不能帮真公司添加零件")){

                    throw new ErrorResponseException(HttpStatus.BAD_REQUEST, DetailsEnum.id_C_NOT_ERROR.getCode(), null);

                }else{
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.LBPROD_ADD_ERROR.getCode(), null);

                }

            }
            else if ( listType.equals("lSComp")) {
                String ref = refAuto(id_C, listType, grp);
                //lSComp，id_C是自己,帮id_CB建立公司
                JSONObject resultJson = addlSComp(id_C, id_U, id,ref, data);
                if (resultJson.get("boolean").equals("true")){
                    return retResult.ok(CodeEnum.OK.getCode(),resultJson.get("reason"));
                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("comp对象为空")){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

                }else{
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.LSCOMP_ADD_ERROR.getCode(), null);

                }



            }
            else if ( listType.equals("lBComp")) {
                String ref = refAuto(id_C, listType, grp);
                //lBComp，id_CB是自己，帮id_C建立公司
                JSONObject resultJson = addlBComp(id_C, id_U, id,ref, data);
                if (resultJson.get("boolean").equals("true")){
                    return retResult.ok(CodeEnum.OK.getCode(),resultJson.get("reason"));
                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("comp对象为空")){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

                }else{
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.LBCOMP_ADD_ERROR.getCode(), null);

                }


            }
            else if (listType.equals("lSOrder")) {
                //最后两边都确认final成功了再添加编号，现在就给空编号
                //String ref = refAuto(id_C, listType, grp);
                JSONObject resultJson = addlSOrder(id_C, id_U, id,"", data);
                if (resultJson.get("boolean").equals("true")){
                    return retResult.ok(CodeEnum.OK.getCode(),resultJson.get("reason"));
                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("lSComp对象为空")){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("lBComp对象为空")){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

                }else{
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_ADD_ERROR.getCode(), null);

                }

            }else if (listType.equals("lBOrder")) {
                //String ref = refAuto(id_C, listType, grp);
                JSONObject resultJson = addlBOrder(id_C, id_U, id,"", data);
                if (resultJson.get("boolean").equals("true")){
                    return retResult.ok(CodeEnum.OK.getCode(),resultJson.get("reason"));
                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("lSComp对象为空")){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

                }else if (resultJson.get("boolean").equals("false") && resultJson.get("reason").equals("lBComp对象为空")){
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.OBJECT_IS_NULL.getCode(), null);

                }else{
                    throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_ADD_ERROR.getCode(), null);
                }
            }
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
            //addSingleMethod(id_C, listType, data, id_U);
    }

    @Override
    public JSONObject addlSAsset(String id_C,String id_U, String id ,String ref,String data) throws IOException {
        //状态结果对象
        JSONObject resultJson = new JSONObject();

        try {


            JSONObject objData = JSONObject.parseObject(data);
            objData.getJSONObject("info").put("ref",ref);
            // 获取data里面的info数据,这个给ES
            JSONObject listObject = new  JSONObject();
            listObject.putAll(objData.getJSONObject("info"));


            // 获取data里面的info数据，这个给mongdb
            JSONObject infoObject =  objData.getJSONObject("info");

            //查找当前公司，获取公司信息
            //Query compCondition = new Query(new Criteria("_id").is(infoObject.get("id_CB")).and("info").exists(true));
            Query compCondition = new Query(new Criteria("_id").is(infoObject.get("id_C")).and("info").exists(true));

            compCondition.fields().include("info");
            Comp objComp = mongoTemplate.findOne(compCondition, Comp.class);
            if(objComp == null){

                resultJson.put("boolean","false");
                resultJson.put("reason","comp对象为空");

            }

            infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("id_CP", objComp.getInfo().getId_CP());
            Asset Asset = JSONObject.toJavaObject(objData, Asset.class);

            Asset.setId(id);

            mongoTemplate.insert(Asset);

            //指定ES索引
            IndexRequest request = new IndexRequest("lSAsset");
            //ES列表
            listObject.put("id_A", id);
            listObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            listObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            listObject.put("id_CP", objComp.getInfo().getId_CP());

            request.source(listObject, XContentType.JSON);
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
            if (infoObject.get("lAT").equals(2)){

//                //拿info位置数组
//                JSONArray refSpaceList = infoObject.getJSONArray("refSpace");
//                if(refSpaceList.size() > 0){
//                    //批量修改
//                    List<Pair<Query, Update>> updateList = new ArrayList<>();
//                    BulkOperations operations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Asset.class);
//                    for (int i = 0; i < refSpaceList.size(); i++) {
//                        Query storageQ = new Query(Criteria.where("info.ref").is("a-storage").and("info.id_C").is(id_C).and("locSetup.locData.refSpace").is(refSpaceList.get(i)));
//                        storageQ.fields().include("locSetup.locData.$");
//
//                        Update update = new Update();
//                        update.set("locSetup.locData.$.id_A",id);
//                        Pair<Query, Update> updatePair = Pair.of(storageQ, update);
//                        updateList.add(updatePair);
//                    }
//                    //批量修改
//                    operations.updateMulti(updateList);
//                    BulkWriteResult result = operations.execute();
//                    //插入计数 insertedCount=0,匹配计数 matchedCount=300,删除计数 removedCount=0,被改进的 modifiedCount=1
//
//                }

                //HashMap<String, Object> hashMap = new HashMap<>();
                JSONObject assetflow = new JSONObject();
                //前端传空会报错
                assetflow.put("wn2qtychg",infoObject.get("wn2qty"));
                assetflow.put("subtype",0);assetflow.put("id_to",id);assetflow.put("id_from","");
                assetflow.put("id_P",infoObject.get("id_P"));assetflow.put("id_O",infoObject.get("id_O"));
                assetflow.put("id_C",id_C);assetflow.put("ref",infoObject.get("ref"));
                assetflow.put("wrdN",infoObject.getJSONObject("wrdN"));assetflow.put("pic",infoObject.get("pic"));
                assetflow.put("wrddesc",infoObject.getJSONObject("wrddesc"));
                //前端传空会报错  应该这里double强转，没有值会报错
                assetflow.put("wn4price",infoObject.getDouble("wn4price"));
                assetflow.put("wn2qc",infoObject.get("wn2qc"));//hashMap.put("refSpace", infoObject.get("refSpace"));
                assetflow.put("grpU","");assetflow.put("grpUB",id_U);
                assetflow.put("tmk",DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
                assetflow.put("tmd",DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));


                dbUtils.addES(assetflow,"assetflow");


            }

            resultJson.put("boolean","true");
            resultJson.put("reason",id);

        } catch (Exception e) {

            resultJson.put("boolean","false");
            resultJson.put("reason","添加内部失败");
            //return RetResult.errorJsonResult(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(),null);


        }


        return resultJson;
    }

    @Override
    public JSONObject addlSProd(String id_C, String id_U, String id, String ref,String data) {

        JSONObject resultJson = new JSONObject();

        try {

            JSONObject objData = JSONObject.parseObject(data);
//            objData.getJSONObject("info").put("ref",ref);


            // 获取data里面的info数据,这个给ES
            JSONObject listObject = new  JSONObject();
            listObject.putAll( objData.getJSONObject("info"));


            // 获取data里面的info数据
            JSONObject infoObject =  objData.getJSONObject("info");

            //查找当前公司，获取公司信息
            Query compCondition = new Query(new Criteria("_id").is(infoObject.get("id_C")).and("info").exists(true));
            compCondition.fields().include("info");
            Comp objComp = mongoTemplate.findOne(compCondition, Comp.class);
            if (objComp == null) {
                resultJson.put("boolean", "false");
                resultJson.put("reason", "comp对象为空");
            }

            infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("id_CP", objComp.getInfo().getId_CP());

            //过滤info卡不要字段
            infoObject.remove("lCR");
            infoObject.remove("wn4price");
            Prod prod = JSONObject.toJavaObject(objData, Prod.class);
            prod.setId(id);

            mongoTemplate.insert(prod);


            //指定ES索引
            IndexRequest request = new IndexRequest("lsprod");
            //ES列表
            listObject.put("id_P", id);
            listObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            listObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            listObject.put("id_CP", objComp.getInfo().getId_CP());

//            listObject.put("wrdNC", objComp.getInfo().get("wrdN"));
//            listObject.put("picC", objComp.getInfo().get("pic"));


            if (objData.containsKey("tag")){
                infoObject.put("tag", objData.get("tag"));
            }




            request.source(listObject, XContentType.JSON);

            restHighLevelClient.index(request, RequestOptions.DEFAULT);
            resultJson.put("boolean","true");
            resultJson.put("reason",id);
        } catch (Exception e) {

            resultJson.put("boolean","false");
            resultJson.put("reason","添加产品失败");
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(),"添加产品失败");

        }


        return resultJson;

    }

    @Override
    public JSONObject addlBProd(String id_C, String id_U, String id, String ref, String data)  {
        JSONObject resultJson = new JSONObject();

        try {

            JSONObject objData = JSONObject.parseObject(data);
//            objData.getJSONObject("info").put("ref",ref);


            // 获取data里面的info数据,这个给ES
            JSONObject listObject = new  JSONObject();
            listObject.putAll( objData.getJSONObject("info"));


            // 获取data里面的info数据
            JSONObject infoObject =  objData.getJSONObject("info");

            //查找当前公司，获取公司信息
            Query compCondition = new Query(new Criteria("_id").is(infoObject.get("id_C")).and("info").exists(true));
            compCondition.fields().include("info");
            Comp objComp = mongoTemplate.findOne(compCondition, Comp.class);
            if (objComp == null) {
                resultJson.put("boolean", "false");
                resultJson.put("reason", "comp对象为空");
            }
            listObject.put("id_P", id);
            listObject.put("id_CB", id_C);
            listObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            listObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            listObject.put("id_CP", objComp.getInfo().getId_CP());

            listObject.put("grpB", objData.getJSONObject("info").getString("grpB"));
            listObject.put("refB", objData.getJSONObject("info").getString("refB"));


            infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("id_CP", objComp.getInfo().getId_CP());

            //过滤info卡不要字段

            infoObject.remove("refB");
            infoObject.remove("grpB");
            Prod prod = JSONObject.toJavaObject(objData, Prod.class);
            prod.setId(id);

            mongoTemplate.insert(prod);


            //指定ES索引
            IndexRequest request = new IndexRequest("lbprod");
            request.source(listObject, XContentType.JSON);
            restHighLevelClient.index(request, RequestOptions.DEFAULT);

            resultJson.put("boolean","true");
            resultJson.put("reason",id);
        } catch (Exception e) {

            resultJson.put("boolean","false");
            resultJson.put("reason","添加产品失败");
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(),"添加产品失败");

        }


        return resultJson;
//        JSONObject resultJson = new JSONObject();
//        System.out.println("here 0");
//
//        try {
//            System.out.println("here 1");
//            JSONObject objData = JSONObject.parseObject(data);
////            objData.getJSONObject("info").put("ref",ref);
//            // 获取data里面的info数据,这个给ES
//            JSONObject listObject = new  JSONObject();
//            System.out.println("here 2");
//
//            listObject.putAll( objData.getJSONObject("info"));
//
//            int bcdNet = dbUtils.judgeComp(id_C, listObject.getString("id_C"));
//            System.out.println("now what"+ bcdNet+listObject);
//
//
//            if(bcdNet == 1){
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(),"添加零件失败");
//            }
//
//            // 获取data里面的info数据
//            JSONObject infoObject =  objData.getJSONObject("info");
//
//            //id_C公司
//            Query queryComp = new Query(new Criteria("_id").is(infoObject.get("id_C")).and("info").exists(true));
//            queryComp.fields().include("info");
//            Comp ownerComp = mongoTemplate.findOne(queryComp, Comp.class);
//            System.out.println("comp"+ownerComp);
//            if(ownerComp == null){
//                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(),"添加零件失败");
//            }
//
//
//            listObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//            listObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
//
//            //ls
//            infoObject.put("id_CP", ownerComp.getInfo().get("id_CP"));
//
//            //过滤info卡不要的字段
//
////            infoObject.remove("refB");
////            infoObject.remove("grpB");
//            System.out.println("objData"+objData);
//            Prod prod = JSONObject.toJavaObject(objData, Prod.class);
//            prod.setId(id);
//System.out.println("prod"+prod);
////            mongoTemplate.insert(prod);
//
//            System.out.println("prod"+prod);
//
////
////
////
////            //指定ES索引
////            IndexRequest request = new IndexRequest("lbprod");
////            //ES列表
////            listObject.put("id_P", id);
////            listObject.put("id_CB", id_C);
////            listObject.put("refB", objData.getJSONObject("info").getString("refB"));
//////            listObject.put("id_CP", lsComp.getInfo().get("id_CP"));
////            listObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
////            listObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
////
////            request.source(listObject, XContentType.JSON);
////
////            restHighLevelClient.index(request, RequestOptions.DEFAULT);
//
//            return retResult.ok(CodeEnum.OK.getCode(), "");
//        } catch (Exception e) {
//
//           throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ASSET_ADD_ERROR.getCode(),"添加零件失败");
//
//
//        }

    }

    @Override
    public JSONObject addlSOrder(String id_C, String id_U, String id, String ref,String data) throws IOException {
        //状态结果对象
        JSONObject resultJson = new JSONObject();

        try {
            JSONObject objData = JSONObject.parseObject(data);
//            objData.getJSONObject("info").put("ref",ref);
            // 获取data里面的info数据,这个给ES
            JSONObject listObject = new  JSONObject();
            listObject.putAll( objData.getJSONObject("info"));


            // 获取data里面的info数据
            JSONObject infoObject =  objData.getJSONObject("info");


            //id_C公司
            Query lsCompCondition = new Query(new Criteria("_id").is(infoObject.get("id_C")).and("info").exists(true));
            lsCompCondition.fields().include("info");
            Comp lsComp = mongoTemplate.findOne(lsCompCondition, Comp.class);
            if (lsComp == null) {

                resultJson.put("boolean", "false");
                resultJson.put("reason", "lSComp对象为空");

            }
            //id_B公司
            Query lbCompCondition = new Query(new Criteria("_id").is(infoObject.get("id_CB")).and("info").exists(true));
            lbCompCondition.fields().include("info");
            Comp lbComp = mongoTemplate.findOne(lbCompCondition, Comp.class);
            if (lbComp == null) {
                resultJson.put("boolean", "false");
                resultJson.put("reason", "lBComp对象为空");
            }

            infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));


            //ls
//            infoObject.put("wrdNC", lsComp.getInfo().get("wrdN"));
//            infoObject.put("picC", lsComp.getInfo().get("pic"));
            infoObject.put("id_CP", lsComp.getInfo().getId_CP());
            //lb
//            infoObject.put("wrdNCB", lbComp.getInfo().get("wrdN"));
//            infoObject.put("picCB", lbComp.getInfo().get("pic"));
            infoObject.put("id_CBP", lbComp.getInfo().getId_CP());


            Order order = JSONObject.toJavaObject(objData, Order.class);
            order.setId(id);

            mongoTemplate.insert(order);


            //指定ES索引
            IndexRequest request = new IndexRequest("lsborder");
            //ES列表
            listObject.put("id_O", id);
            listObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            listObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));


            //ls
//            listObject.put("wrdNC", lsComp.getInfo().get("wrdN"));
//            listObject.put("picC", lsComp.getInfo().get("pic"));
            listObject.put("id_CP", lsComp.getInfo().getId_CP());
            //lb
//            listObject.put("wrdNCB", lbComp.getInfo().get("wrdN"));
//            listObject.put("picCB", lbComp.getInfo().get("pic"));
            listObject.put("id_CBP", lbComp.getInfo().getId_CP());
            //查询对方公司是不是真的，当前公司肯定是真的
//            int bcdNet = detailsUtils.judgeComp(listObject.getString("id_CB"), listObject.getString("id_C"));
//            listObject.put("bcdNet", bcdNet);



            request.source(listObject, XContentType.JSON);

            restHighLevelClient.index(request, RequestOptions.DEFAULT);
            resultJson.put("boolean", "true");
            resultJson.put("reason", id);

        } catch (Exception e) {

            resultJson.put("boolean", "false");
            resultJson.put("reason", "添加销售订单失败");

        }

        return resultJson;
    }

    @Override
    public JSONObject addlBOrder(String id_C, String id_U, String id, String ref,String data) throws IOException {
        //状态结果对象
        JSONObject resultJson = new JSONObject();

        try {
            JSONObject objData = JSONObject.parseObject(data);
//            objData.getJSONObject("info").put("ref",ref);
            // 获取data里面的info数据,这个给ES
            JSONObject listObject = new  JSONObject();
            listObject.putAll(objData.getJSONObject("info"));


            // 获取data里面的info数据
            JSONObject infoObject =  objData.getJSONObject("info");


            //id_C公司
            Query lsCompCondition = new Query(new Criteria("_id").is(infoObject.get("id_C")).and("info").exists(true));
            lsCompCondition.fields().include("info");
            Comp lsComp = mongoTemplate.findOne(lsCompCondition, Comp.class);
            if (lsComp == null) {

                resultJson.put("boolean", "false");
                resultJson.put("reason", "lSComp对象为空");

            }
            //id_B公司
            Query lbCompCondition = new Query(new Criteria("_id").is(infoObject.get("id_CB")).and("info").exists(true));
            lbCompCondition.fields().include("info");
            Comp lbComp = mongoTemplate.findOne(lbCompCondition, Comp.class);
            if (lbComp == null) {
                resultJson.put("boolean", "false");
                resultJson.put("reason", "lBComp对象为空");
            }

            infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));


            //ls
//            infoObject.put("wrdNC", lsComp.getInfo().get("wrdN"));
//            infoObject.put("picC", lsComp.getInfo().get("pic"));
            infoObject.put("id_CP", lsComp.getInfo().getId_CP());
            //lb
//            infoObject.put("wrdNCB", lbComp.getInfo().get("wrdN"));
//            infoObject.put("picCB", lbComp.getInfo().get("pic"));
            infoObject.put("id_CBP", lbComp.getInfo().getId_CP());



            Order order = JSONObject.toJavaObject(objData, Order.class);
            order.setId(id);

            mongoTemplate.insert(order);


            //指定ES索引
            IndexRequest request = new IndexRequest("lsborder");
            //ES列表
            listObject.put("id_O", id);
            listObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            listObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));


            //ls
//            listObject.put("wrdNC", lsComp.getInfo().get("wrdN"));
//            listObject.put("picC", lsComp.getInfo().get("pic"));
            listObject.put("id_CP", lsComp.getInfo().getId_CP());
            //lb
//            listObject.put("wrdNCB", lbComp.getInfo().get("wrdN"));
//            listObject.put("picCB", lbComp.getInfo().get("pic"));
            listObject.put("id_CBP", lbComp.getInfo().getId_CP());

//            int bcdNet = detailsUtils.judgeComp(listObject.getString("id_C"), listObject.getString("id_CB"));
//            listObject.put("bcdNet", bcdNet);

            request.source(listObject, XContentType.JSON);

            restHighLevelClient.index(request, RequestOptions.DEFAULT);
            resultJson.put("boolean", "true");
            resultJson.put("reason", id);

        } catch (Exception e) {

            resultJson.put("boolean", "false");
            resultJson.put("reason", "添加订单失败");

        }

        return resultJson;
    }

    @Override
    public JSONObject addlSComp(String id_C, String id_U, String id, String ref,String data) throws IOException {
        //状态结果对象
        JSONObject resultJson = new JSONObject();

        try {
            JSONObject jsonObject = JSONObject.parseObject(data);
//            jsonObject.getJSONObject("info").put("ref",ref);
            // 获取data里面的info数据
            JSONObject infoObject =  jsonObject.getJSONObject("info");
            //指定ES索引
            IndexRequest request = new IndexRequest("lsbcomp");



            //id_C公司
            Query lsCompCondition = new Query(new Criteria("_id").is(id_C).and("info").exists(true));
            lsCompCondition.fields().include("info");
            Comp lsComp = mongoTemplate.findOne(lsCompCondition, Comp.class);
            if (lsComp == null) {

                resultJson.put("boolean", "false");
                resultJson.put("reason", "lSComp对象为空");

            }


            Comp comp = new Comp();

            JSONObject info = new JSONObject();

            info.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            info.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            //info.put("bcdNet", 0);
            info.put("id_C",id);
            info.put("id_CP",id_C);
            info.put("ref",infoObject.getString("ref"));

            info.put("pic",infoObject.getString("pic"));
            info.put("wrdN",infoObject.getJSONObject("wrdN"));
            info.put("wrddesc",infoObject.getJSONObject("wrddesc"));




            comp.setBcdNet(0);

            comp.setInfo(JSONObject.parseObject(JSON.toJSONString(info), CompInfo.class));
            comp.setView(jsonObject.getJSONArray("view"));

            comp.setId(id);

            mongoTemplate.insert(comp);



            //ES列表
            infoObject.put("id_CB",id);
            infoObject.put("id_CBP",id_C);
            infoObject.put("refCB",infoObject.get("ref"));
            infoObject.remove("ref");
            infoObject.put("picCB",infoObject.get("pic"));
            infoObject.remove("pic");
            infoObject.put("wrdNCB",infoObject.get("wrdN"));
            infoObject.remove("wrdN");
            infoObject.put("wrddescB",infoObject.get("wrddesc"));
            infoObject.remove("wrddesc");

            infoObject.put("id_C",id_C);
            infoObject.put("refC",lsComp.getInfo().getRef());
            infoObject.put("picC",lsComp.getInfo().getPic());
            infoObject.put("wrdNC", lsComp.getInfo().getWrdN());
            infoObject.put("wrddesc", lsComp.getInfo().getWrddesc());
            infoObject.put("id_CP",lsComp.getInfo().getId_CP());
//            infoObject.put("bcdNet", 0);
            infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));




            request.source(infoObject, XContentType.JSON);
            restHighLevelClient.index(request, RequestOptions.DEFAULT);

            resultJson.put("boolean","true");
            resultJson.put("reason",id);


        } catch (Exception e) {

            resultJson.put("boolean", "false");
            resultJson.put("reason", "添加客户失败");

        }

        return resultJson;
    }

    @Override
    public JSONObject addlBComp(String id_C, String id_U, String id, String ref,String data) throws IOException {
        //状态结果对象
        JSONObject resultJson = new JSONObject();

        try {

            JSONObject jsonObject = JSONObject.parseObject(data);
//            jsonObject.getJSONObject("info").put("ref",ref);
            // 获取data里面的info数据
            JSONObject infoObject =  jsonObject.getJSONObject("info");
            //指定ES索引
            IndexRequest request = new IndexRequest("lsbcomp");

            //id_B公司
            Query lbCompCondition = new Query(new Criteria("_id").is(id_C).and("info").exists(true));
            lbCompCondition.fields().include("info");
            Comp lbComp = mongoTemplate.findOne(lbCompCondition, Comp.class);
            if (lbComp == null) {

                resultJson.put("boolean", "false");
                resultJson.put("reason", "lBComp对象为空");

            }


            Comp comp = new Comp();

            JSONObject info = new JSONObject();
            info.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            info.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            //info.put("bcdNet", 0);
            info.put("id_C",id);
            info.put("id_CP",id_C);
            info.put("ref",infoObject.getString("ref"));

            info.put("pic",infoObject.getString("pic"));
            info.put("wrdN",infoObject.getJSONObject("wrdN"));
            info.put("wrddesc",infoObject.getJSONObject("wrddesc"));





            comp.setBcdNet(0);
            comp.setInfo(JSONObject.parseObject(JSON.toJSONString(info), CompInfo.class));
            comp.setView(jsonObject.getJSONArray("view"));

            comp.setId(id);

            mongoTemplate.insert(comp);


            //ES列表
            infoObject.put("id_C",id);
            infoObject.put("id_CP",id_C);
            infoObject.put("refC",infoObject.get("ref"));
            infoObject.remove("ref");
            infoObject.put("picC",infoObject.get("pic"));
            infoObject.remove("pic");
            infoObject.put("wrdNC",infoObject.get("wrdN"));
            infoObject.remove("wrdN");

            infoObject.put("id_CB",id_C);
            infoObject.put("refCB",lbComp.getInfo().getRef());
            infoObject.put("picCB",lbComp.getInfo().getPic());
            infoObject.put("wrdNCB", lbComp.getInfo().getWrdN());
            infoObject.put("wrddescB", lbComp.getInfo().getWrddesc());
            infoObject.put("id_CBP",lbComp.getInfo().getId_CP());
//            infoObject.put("bcdNet", 0);
            infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            infoObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));





            request.source(infoObject, XContentType.JSON);
            restHighLevelClient.index(request, RequestOptions.DEFAULT);

            resultJson.put("boolean", "true");
            resultJson.put("reason", id);

        } catch (Exception e) {

            resultJson.put("boolean", "false");
            resultJson.put("reason", "添加供应商失败");

        }

        return resultJson;
    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public String refAuto(String id_C, String listType, String grp) {

        //try {
        String id_A = dbUtils.getId_A(id_C, "a-auth");
        Query authQ = new Query(new Criteria("_id").is(id_A));

        authQ.fields().include("refAuto."+listType+"."+grp);

        Asset asset = mongoTemplate.findOne(authQ, Asset.class);


        //当查找出来的Asset为空，报错给前端，让它去refAuto卡片增加

        if (asset == null || asset.getRefAuto() == null ||
                asset.getRefAuto().getJSONObject(listType) == null ||
                asset.getRefAuto().getJSONObject(listType).size() == 0 ||
                asset.getRefAuto().getJSONObject(listType).getJSONArray(grp).size() ==  0){

            //return RetResult.errorJsonResult(HttpStatus.BAD_REQUEST, DetailsEnum.REFAUTO_ERROR.getCode(),null);
            return "";
        }

        JSONArray jsonArray = asset.getRefAuto().getJSONObject(listType).getJSONArray(grp);

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < jsonArray.size(); i++) {

            JSONObject objGrp = jsonArray.getJSONObject(i);


            if (objGrp.getString("key").equals("inc")){



                //幂次方  10^3-1   == 999
                Double digit = Math.pow(10, objGrp.getInteger("digit"));

                //如果自增数 与 位数的最大值一致，则从0开始
                if (objGrp.getInteger("nowCount") == (digit.intValue())){
                    objGrp.put("nowCount",1);

                    mongoTemplate.findAndModify(new Query(
                                    new Criteria("_id").is(asset.getId())),
                            new Update().set("refAuto." + listType + "." + grp +"."+ i, objGrp), Asset.class);
                }else{

                    mongoTemplate.findAndModify(new Query(
                                    new Criteria("_id").is(asset.getId())),
                            new Update().inc("refAuto." + listType + "." + grp +"."+i+ "." + "nowCount",  1), Asset.class);

                }

                String inc = String.format("%0"+objGrp.getInteger("digit")+"d", objGrp.getInteger("nowCount"));
                //result = String.format("%0"+objGrp.getInteger("digit")+"d", objGrp.getInteger("number"));
                result.append(inc);

            }

            if (objGrp.getString("key").equals("ref")){

                String ref  = objGrp.getString("val");
                result.append(ref);

            }

            if (objGrp.getString("key").equals("year")){

                String year  = new SimpleDateFormat(objGrp.getString("val"), Locale.CHINESE).format(Calendar.getInstance().getTime());
                //result  = new SimpleDateFormat(objGrp.getString("val"),Locale.CHINESE).format(Calendar.getInstance().getTime());
                result.append(year);

            }
            if (objGrp.getString("key").equals("month")){

                String month = new SimpleDateFormat(objGrp.getString("val"),Locale.CHINESE).format(Calendar.getInstance().getTime());
                //result = new SimpleDateFormat(objGrp.getString("val"),Locale.CHINESE).format(Calendar.getInstance().getTime());
                result.append(month);

            }
            if (objGrp.getString("key").equals("day")){

                String day = new SimpleDateFormat(objGrp.getString("val"),Locale.CHINESE).format(Calendar.getInstance().getTime());
                //result = new SimpleDateFormat(objGrp.getString("val"),Locale.CHINESE).format(Calendar.getInstance().getTime());
                result.append(day);


            }


        }




        return result.toString();
//    } catch (Exception e) {
//
//            return RetResult.jsonResultEncrypt(HttpStatus.OK, DetailsEnum.TRY_AGAIN_LATER.getCode(),null);
//
//
//        }
    }




    @Override
    public ApiResponse delUseless(String id_U, String id_C, String id, String listType) throws IOException {

        String compID = "";
        StringBuffer indexType = new StringBuffer();//索引名称
        String listID = "";//列表ID
        Object typeClass = null;
        if (!StringUtils.isEmpty(listType)){

            if (listType.equals("lBProd")) {
                indexType.append("lBProd");
                compID = "id_CB";
                listID = "id_P";
                typeClass = Prod.class;
            } else if (listType.equals("lBUser")) {
                indexType.append("lBUser");
                compID = "id_CB";
                listID = "id_U";
                typeClass = User.class;
            } else if (listType.equals("lBComp")) {
                indexType.append("lSBComp");
                compID = "id_CB";
                listID = "id_CB";
                typeClass = Comp.class;
            } else if (listType.equals("lBOrder")) {
                indexType.append("lSBOrder");
                compID = "id_CB";
                listID = "id_O";
                typeClass = Order.class;
            } else if (listType.equals("lSOrder")) {
                indexType.append("lSBOrder");
                compID = "id_C";
                listID = "id_O";
                typeClass = Order.class;
            } else if (listType.equals("lSComp")) {
                indexType.append("lSBComp");
                compID = "id_C";
                listID = "id_C";
                typeClass = Comp.class;
            } else if(listType.equals("lSProd")) {
                indexType.append("lSProd");
                compID = "id_C";
                listID = "id_P";
                typeClass = Prod.class;
            }else if(listType.equals("lSAsset")) {
                indexType.append("lSAsset");
                compID = "id_C";
                listID = "id_A";
                typeClass = Asset.class;
            }


            Object one  = mongoTemplate.findOne(new Query(new Criteria("_id").is(id)), (Class<?>) typeClass);
            //当查询为空，证明没有数据，就删除ES
            if (one == null) {
                // 删除es列表
                DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(String.valueOf(indexType));
                BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
                // 查询条件
                boolQueryBuilder.must(QueryBuilders.termQuery(listID, id));
                boolQueryBuilder.must(QueryBuilders.termQuery(compID, id_C));
                //deleteByQueryRequest.setSize(1);
                deleteByQueryRequest.setQuery(boolQueryBuilder);

                restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);

                return retResult.ok(CodeEnum.OK.getCode(),null);


            }
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.id_C_NOT_ERROR.getCode(), null);

        }

        return null;
    }

    @Override
    public String getPartCardData(String id_U, String id_C, String id_P) {

        Query query =  new Query(new Criteria("_id").is(id_P));
        query.fields().include("info").include("part");
        Prod prod  = mongoTemplate.findOne(query, Prod.class);

        if (ObjectUtils.isEmpty(prod)) {
            return RetResult.jsonResult(HttpStatus.OK, DetailsEnum.PROD_NOT_FOUND.getCode(), null);
        }

        int judgeComp = dbUtils.judgeComp(id_C,prod.getInfo().getId_C());

        if (judgeComp == 1) {
            return RetResult.errorJsonResult(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
        }
        return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), prod.getPart());

    }

    @Override
    public ApiResponse setOItem(String id_U, String id_C, String id_O, JSONObject oItemData,String listType,String grp) {

            Query query = new Query();
            query.addCriteria(
                    new Criteria().andOperator(
                            Criteria.where("_id").is(id_O),
                            new Criteria().orOperator(
                                    new Criteria().andOperator(
                                            Criteria.where("info.id_C").is(id_C)),
                                    new Criteria().andOperator(
                                            Criteria.where("info.id_CB").is(id_C))
                            )
                    )
            );
            UpdateResult updateResult = mongoTemplate.updateFirst(query, new Update().push("oItem.objItem", oItemData), Order.class);
            System.out.println(updateResult);
            query.fields().include("oItem");
            Order order = mongoTemplate.findOne(query, Order.class);
            JSONArray objItem = order.getOItem().getJSONArray("objItem");
            System.out.println(objItem.size() - 1);
            if (updateResult.getModifiedCount() > 0){
                return retResult.ok(CodeEnum.OK.getCode(),objItem.size() - 1);
            }
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }

//    @Override
//    public ApiResponse updateModuleId(String id_C) {
//        String id_A = dbUtils.getId_A(id_C, "a-module");
//        Query query = new Query(new Criteria("_id").is(id_A));
//        query.fields().include("control");
//        Asset asset = mongoTemplate.findOne(query, Asset.class);
//        JSONArray objData = asset.getControl().getJSONArray("objData");
//        JSONArray objData1 = (JSONArray) objData.clone();
//        //去重：ref相同，保留bcdLevel最大的数据
//        for (int i = 0; i < objData.size(); i++) {
//            for (int j = 0; j < objData1.size(); j++) {
//                if (objData.getJSONObject(i) != null && objData1.getJSONObject(j) != null) {
//                    if (objData.getJSONObject(i).getString("ref").equals(objData1.getJSONObject(j).getString("ref"))) {
//                        if (objData.getJSONObject(i).getInteger("bcdLevel") > objData1.getJSONObject(j).getInteger("bcdLevel")) {
//                            objData1.remove(j);
//                        }
//                    }
//                }
//            }
//        }
//        JSONObject json = new JSONObject();
//        //遍历ref查询id_A
//        for (int i = 0; i < objData1.size(); i++) {
//            if (objData1.getJSONObject(i) != null) {
//String id_A = dbUtils.getId_A(id_C, "a-auth");
//    Query menuQuery = new Query(new Criteria("_id").is(id_A));
//                Query query1 = new Query(new Criteria("info.id_C").is(id_C).and("info.ref").is(objData1.getJSONObject(i).getString("ref")));
//                asset = mongoTemplate.findOne(query1, Asset.class);
//                if (asset != null) {
//                    json.put(objData1.getJSONObject(i).getString("ref"), asset.getId());
//                }
//            }
//        }
//        redisTemplate0.opsForHash().put("login:get_read_ids:compId-" + id_C, "id_A", json.toString());
//        Boolean bool = redisTemplate0.opsForHash().hasKey("login:get_read_ids:compId-" + id_C, "id_A");
//        System.out.println(bool);
//        if (bool) {
//            return retResult.ok(CodeEnum.OK.getCode(), json);
//        }
//        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//    }

    @Override
    public ApiResponse getOItemDetail(String id_O, Integer index,JSONArray cardList) {
        Query query = new Query(new Criteria("_id").is(id_O));
//        Field fields = query.fields();
//        return mongoTemplate.findOne(query, Comp.class);
        for (int i = 0; i < cardList.size(); i++)
        {
            query.fields().include(cardList.getString(i));
        }
        Order order = mongoTemplate.findOne(query, Order.class);
        JSONObject jsonOrder = (JSONObject) JSON.toJSON(order);
        System.out.println("jsonHere"+jsonOrder);

        if (order == null)
        {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_NOT_FOUND.getCode(), "");
        }
//        JSONArray objItem = order.getOItem().getJSONArray("objItem");
//        if (objItem.getJSONObject(index) != null) {
            JSONObject result = new JSONObject();
//              result.put("oItem", objItem.getJSONObject(index));
          for (int i = 0; i < cardList.size(); i++)
          {
              String[] cardSplit = cardList.getString(i).split("\\.");

              if(jsonOrder.getJSONObject(cardSplit[0]) != null &&
                      jsonOrder.getJSONObject(cardSplit[0]).getJSONArray(cardSplit[1]) != null &&
                      jsonOrder.getJSONObject(cardSplit[0]).getJSONArray(cardSplit[1]).size() > index) {

                  result.put(cardSplit[0], jsonOrder.getJSONObject(cardSplit[0]).getJSONArray(cardSplit[1]).getJSONObject(index));
              }
          }

            System.out.println(result);
            return retResult.ok(CodeEnum.OK.getCode(), result);
//        }
//        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }

    @Override
    public ApiResponse delComp(String id_U, String id_C) throws IOException {
        Query queryUser = new Query(new Criteria("_id").is(id_U));
        queryUser.fields().include("rolex");
        User user = mongoTemplate.findOne(queryUser, User.class);
        String grpU = user.getRolex().getJSONObject("objComp").getJSONObject(id_C).getString("grpU");
        System.out.println(grpU);
        if ("1001".equals(grpU)) {
            //删除Prod：产品和自产零件
            Query queryProd = new Query(new Criteria("info.id_C").is(id_C));
            mongoTemplate.remove(queryProd, Prod.class);
            //删除lSProd：产品
            SearchSourceBuilder builderProd = new SearchSourceBuilder();
            BoolQueryBuilder mustQuery = new BoolQueryBuilder();
            mustQuery.must(QueryBuilders.termQuery("id_C", id_C));
            DeleteByQueryRequest lSProdDeleteRequest = new DeleteByQueryRequest("lSProd").setQuery(mustQuery);
            BulkByScrollResponse lSProdDeleteResponse = restHighLevelClient.deleteByQuery(lSProdDeleteRequest, RequestOptions.DEFAULT);
            System.out.println("lSProdDeleteResponse=" + lSProdDeleteResponse);
            //查询lBProd：获取要删除的id_P
            mustQuery = new BoolQueryBuilder();
            mustQuery.must(QueryBuilders.termQuery("id_CB", id_C)).must(QueryBuilders.termQuery("bcdNet", 0));
            builderProd.query(mustQuery).from(0).size(1000);
            SearchRequest lBProdSearchRequest = new SearchRequest("lBProd").source(builderProd);
            SearchResponse lBProdSearchResponse = restHighLevelClient.search(lBProdSearchRequest, RequestOptions.DEFAULT);
            System.out.println("lBProdSearchResponse=" + lBProdSearchResponse);
            SearchHit[] lBProdHits = lBProdSearchResponse.getHits().getHits();
            //删除Prod：逐条删除bcdNet=0的购买零件
            for (int i = 0; i < lBProdHits.length; i++) {
                JSONObject lBProdHit = (JSONObject) JSON.parse(lBProdHits[i].getSourceAsString());
                System.out.println("hits[" + i + "]=" + lBProdHit);
                String id_P = lBProdHit.getString("id_P");
                System.out.println("id_P=" + id_P);
                queryProd = new Query(new Criteria("_id").is(id_P));
                DeleteResult result = mongoTemplate.remove(queryProd, Prod.class);
                System.out.println("deleteCount=" + result.getDeletedCount());
            }
            //删除lBProd：自产零件和bcdNet=0的购买零件
            mustQuery = new BoolQueryBuilder();
            BoolQueryBuilder mustQuery1 = new BoolQueryBuilder();
            BoolQueryBuilder shouldQuery = new BoolQueryBuilder();
            mustQuery.must(QueryBuilders.termQuery("id_C", id_C));
            mustQuery1.must(QueryBuilders.termQuery("id_CB", id_C)).must(QueryBuilders.termQuery("bcdNet", 0));
            shouldQuery.should(mustQuery).should(mustQuery1);
            DeleteByQueryRequest lBProdDeleteRequest = new DeleteByQueryRequest("lBProd").setQuery(shouldQuery);
            BulkByScrollResponse lBProdDeleteResponse = restHighLevelClient.deleteByQuery(lBProdDeleteRequest, RequestOptions.DEFAULT);
            System.out.println("lBProdDeleteResponse=" + lBProdDeleteResponse);
            //查询lSBOrder：获取要删除的id_O
            SearchSourceBuilder builderOrder = new SearchSourceBuilder();
            mustQuery = new BoolQueryBuilder();
            shouldQuery = new BoolQueryBuilder();
            shouldQuery.should(QueryBuilders.termQuery("id_C", id_C)).should(QueryBuilders.termQuery("id_CB", id_C));
            mustQuery.must(QueryBuilders.termQuery("bcdNet", 0)).must(shouldQuery);
            builderOrder.query(mustQuery).from(0).size(1000);
            SearchRequest lSBOrderSearchRequest = new SearchRequest("lSBOrder").source(builderOrder);
            SearchResponse lSBOrderSearchResponse = restHighLevelClient.search(lSBOrderSearchRequest, RequestOptions.DEFAULT);
            SearchHit[] lSBOrderHits = lSBOrderSearchResponse.getHits().getHits();
            Query queryOrder;
            //删除Order：逐条删除bcdNet=0的订单
            for (int i = 0; i < lSBOrderHits.length; i++) {
                JSONObject lSBOrderHit = (JSONObject) JSON.parse(lSBOrderHits[i].getSourceAsString());
                System.out.println("hit=" + lSBOrderHit);
                String id_O = lSBOrderHit.getString("id_O");
                System.out.println(id_O);
                queryOrder = new Query(new Criteria("_id").is(id_O));
                DeleteResult result = mongoTemplate.remove(queryOrder, Order.class);
                System.out.println(result);
            }
            //删除lSBOrder：bcdNet=0的订单
            DeleteByQueryRequest lSBOrderDeleteRequest = new DeleteByQueryRequest("lSBOrder").setQuery(mustQuery);
            BulkByScrollResponse lSBOrderDeleteResponse = restHighLevelClient.deleteByQuery(lSBOrderDeleteRequest, RequestOptions.DEFAULT);
            System.out.println("lSBOrderDeleteResponse=" + lSBOrderDeleteResponse);
            //删除Asset
            Query queryAsset = new Query(new Criteria("info.id_C").is(id_C));
            DeleteResult removeAsset = mongoTemplate.remove(queryAsset, Asset.class);
            System.out.println("removeAsset=" + removeAsset);
            //删除lSAsset
            mustQuery = new BoolQueryBuilder();
            mustQuery.must(QueryBuilders.termQuery("id_C", id_C));
            DeleteByQueryRequest lSAssetDeleteRequest = new DeleteByQueryRequest("lSAsset").setQuery(mustQuery);
            BulkByScrollResponse lSAssetDeleteResponse = restHighLevelClient.deleteByQuery(lSAssetDeleteRequest, RequestOptions.DEFAULT);
            System.out.println("lSAssetDeleteResponse=" + lSAssetDeleteResponse);
            //查询lBUser：获取要修改的id_CU
            SearchSourceBuilder builderUser = new SearchSourceBuilder();
            mustQuery = new BoolQueryBuilder();
            mustQuery.must(QueryBuilders.termQuery("id_CB", id_C));
            builderUser.query(mustQuery).from(0).size(1000);
            SearchRequest lBUserSearchRequest = new SearchRequest("lBUser").source(builderUser);
            SearchResponse lBUserSearchResponse = restHighLevelClient.search(lBUserSearchRequest, RequestOptions.DEFAULT);
            System.out.println("lBUserSearchResponse=" + lBUserSearchResponse);
            SearchHit[] lBUserHits = lBUserSearchResponse.getHits().getHits();
            //修改User：逐条删除和本公司的关联
            for (int i = 0; i < lBUserHits.length; i++) {
                JSONObject lBUserHit = (JSONObject) JSON.parse(lBUserHits[i].getSourceAsString());
                System.out.println("hits[" + i + "]=" + lBUserHit);
                String id_CU = lBUserHit.getString("id_U");
                queryUser = new Query(new Criteria("_id").is(id_CU));
                Update update = new Update();
                update.unset("rolex.objComp." + id_C);
                update.set("info.def_C", "5f2a2502425e1b07946f52e9");
                UpdateResult updateUser = mongoTemplate.updateFirst(queryUser, update, User.class);
                System.out.println("updateUser=" + updateUser);
            }
            //删除lBUser
            DeleteByQueryRequest lBUserDeleteRequest = new DeleteByQueryRequest("lBUser").setQuery(mustQuery);
            BulkByScrollResponse lBUserDeleteResponse = restHighLevelClient.deleteByQuery(lBUserDeleteRequest, RequestOptions.DEFAULT);
            System.out.println("lBUserDeleteResponse=" + lBUserDeleteResponse);
            //查询lSBComp：获取要删除的id_CB
            SearchSourceBuilder builderComp = new SearchSourceBuilder();
            shouldQuery = new BoolQueryBuilder();
            mustQuery = new BoolQueryBuilder();
            shouldQuery.should(QueryBuilders.termQuery("id_C", id_C)).should(QueryBuilders.termQuery("id_CB", id_C));
            mustQuery.must(QueryBuilders.termQuery("bcdNet", 0)).must(shouldQuery);
            builderComp.query(mustQuery).from(0).size(1000);
            SearchRequest lSBCompSearchRequest = new SearchRequest("lSBComp").source(builderComp);
            SearchResponse lSBCompSearchResponse = restHighLevelClient.search(lSBCompSearchRequest, RequestOptions.DEFAULT);
            System.out.println("lSBCompSearchResponse=" + lSBCompSearchResponse);
            SearchHit[] lSBCompHits = lSBCompSearchResponse.getHits().getHits();
            Query queryComp;
            //删除Comp：逐条删除bcdNet=0的公司
            for (int i = 0; i < lSBCompHits.length; i++) {
                JSONObject lSBCompHit = (JSONObject) JSON.parse(lSBCompHits[i].getSourceAsString());
                System.out.println("hits[" + i + "]=" + lSBCompHit);
                String id_CB = null;
                if (lSBCompHit.getString("id_C").equals(id_C)) {
                    id_CB = lSBCompHit.getString("id_CB");
                } else {
                    id_CB = lSBCompHit.getString("id_C");
                }
                System.out.println("id_CB=" + id_CB);
                queryComp = new Query(new Criteria("_id").is(id_CB));
                DeleteResult removeComp = mongoTemplate.remove(queryComp, Comp.class);
                System.out.println("deleteCount=" + removeComp.getDeletedCount());
            }
            //删除lSBComp：bcdNet=0的公司
            DeleteByQueryRequest lSBCompDeleteRequest = new DeleteByQueryRequest("lSBComp").setQuery(mustQuery);
            BulkByScrollResponse lSBCompDeleteResponse = restHighLevelClient.deleteByQuery(lSBCompDeleteRequest, RequestOptions.DEFAULT);
            System.out.println("lSBCompDeleteResponse=" + lSBCompDeleteResponse);
            //修改lSBComp：bcdNet=1对应的grp修改为1099
            mustQuery = new BoolQueryBuilder();
            mustQuery.must(QueryBuilders.termQuery("bcdNet", 1)).must(QueryBuilders.termQuery("id_C", id_C));
            UpdateByQueryRequest lSBCompUpdateRequest = new UpdateByQueryRequest("lSBComp").setQuery(mustQuery)
                    .setScript(new Script("ctx._source.grp='1099'"));
            BulkByScrollResponse lSBCompUpdateResponse = restHighLevelClient.updateByQuery(lSBCompUpdateRequest, RequestOptions.DEFAULT);
            System.out.println("lSBCompUpdateResponse=" + lSBCompUpdateResponse);
            //修改lSBComp：bcdNet=1对应的grpB修改为1099
            mustQuery = new BoolQueryBuilder();
            mustQuery.must(QueryBuilders.termQuery("bcdNet", 1)).must(QueryBuilders.termQuery("id_CB", id_C));
            lSBCompUpdateRequest = new UpdateByQueryRequest("lSBComp").setQuery(mustQuery)
                    .setScript(new Script("ctx._source.grpB='1099'"));
            lSBCompUpdateResponse = restHighLevelClient.updateByQuery(lSBCompUpdateRequest, RequestOptions.DEFAULT);
            System.out.println("lSBCompUpdateResponse=" + lSBCompUpdateResponse);
            //删除Comp: 删除本公司
            queryComp = new Query(new Criteria("_id").is(id_C));
            DeleteResult removeComp = mongoTemplate.remove(queryComp, Comp.class);
            System.out.println("removeComp=" + removeComp);
            return retResult.ok(CodeEnum.OK.getCode(), true);
        }
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }

    @Override
    public ApiResponse setActionStatus(String id_C, String id_O, Integer oIndex, Integer bcdStatus) {
        Query queryOrder = new Query(new Criteria("_id").is(id_O).and("info.id_C").is(id_C));
        Update updateOrder = new Update();
        updateOrder.set("action.objAction." + oIndex + ".bcdStatus", bcdStatus);
        UpdateResult updateResult = mongoTemplate.updateFirst(queryOrder, updateOrder, Order.class);
        System.out.println(updateResult);
        if (updateResult.getModifiedCount() > 0) {
            return retResult.ok(CodeEnum.OK.getCode(), true);
        }
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, DetailsEnum.OBJECT_IS_NULL.getCode(), null);
    }

    @Override
    public ApiResponse setOItemRelated(String id_U, String id_C, String listType, String grp, String id_O, String card, String objName, Integer index, JSONObject content)
    {

            Query queryOrder = new Query(new Criteria("_id").is(id_O));
            UpdateResult updateResult = null;
            if (index == -1) {
                Update updateOrder = new Update();
                updateOrder.push(card + "." + objName, content);
                updateResult = mongoTemplate.updateFirst(queryOrder, updateOrder, Order.class);
                System.out.println(updateResult);
            } else {
                Update updateOrder = new Update();
                updateOrder.set(card + "." + objName + "." + index, content);
                updateResult = mongoTemplate.updateFirst(queryOrder, updateOrder, Order.class);
                System.out.println(updateResult);
            }
            if (updateResult.getModifiedCount() > 0) {
                return retResult.ok(CodeEnum.OK.getCode(), true);
            }
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, DetailsEnum.ORDER_ALL_ERROR.getCode(), null);

    }

    @Override
    public ApiResponse getOItemRelated(String id_U, String id_C, String listType, String grp, String id_O, String card, String objName, Integer index) {

            Query queryOrder = new Query(new Criteria("_id").is(id_O));
            Order order = mongoTemplate.findOne(queryOrder, Order.class);
            JSONObject jsonOrder = (JSONObject) JSON.toJSON(order);
            JSONObject arrayObject = jsonOrder.getJSONObject(card).getJSONArray(objName).getJSONObject(index);
            System.out.println(arrayObject);
            if (arrayObject != null) {
                return retResult.ok(CodeEnum.OK.getCode(), arrayObject);
            }
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_NOT_FOUND.getCode(), "");

    }

    @Override
    public ApiResponse checkBcdNet(String id_C) {
        Query queryComp = new Query(new Criteria("_id").is(id_C));
        queryComp.fields().include("bcdNet");
        Comp comp = mongoTemplate.findOne(queryComp, Comp.class);
        Integer bcdNet = comp.getBcdNet();
        return retResult.ok(CodeEnum.OK.getCode(), bcdNet);
    }

//    @Override
//    public ApiResponse statisticsSum(String id_C, String startDate, String endDate, String dateGroup, String groupKey, JSONArray groupValue) throws IOException {
//        SearchSourceBuilder builder = new SearchSourceBuilder();
//        BoolQueryBuilder query = new BoolQueryBuilder();
//        query.must(QueryBuilders.termQuery("id_C", id_C))
//                .must(QueryBuilders.rangeQuery("tmd").from(startDate, true).to(endDate, true));
//        switch (dateGroup) {
//            case "SECOND":
//                //根据传入的键进行分组
//                builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                        //根据时间进行分组
//                        .subAggregation(AggregationBuilders.dateHistogram("tmd").field("tmd").calendarInterval(DateHistogramInterval.SECOND)
//                                //设置最小值和最大值范围，默认值为0
//                                .extendedBounds(new ExtendedBounds(startDate, endDate)).minDocCount(0L)
//                                //统计该分组总数
//                                .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow"))));
//                break;
//            case "MINUTE":
//                builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                        .subAggregation(AggregationBuilders.dateHistogram("tmd").field("tmd").calendarInterval(DateHistogramInterval.MINUTE)
//                                .extendedBounds(new ExtendedBounds(startDate, endDate)).minDocCount(0L)
//                                .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow"))));
//                break;
//            case "HOUR":
//                builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                        .subAggregation(AggregationBuilders.dateHistogram("tmd").field("tmd").calendarInterval(DateHistogramInterval.HOUR)
//                                .extendedBounds(new ExtendedBounds(startDate, endDate)).minDocCount(0L)
//                                .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow"))));
//                break;
//            case "DAY":
//                builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                        .subAggregation(AggregationBuilders.dateHistogram("tmd").field("tmd").calendarInterval(DateHistogramInterval.DAY)
//                                .extendedBounds(new ExtendedBounds(startDate, endDate)).minDocCount(0L)
//                                .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow"))));
//                break;
//            case "WEEK":
//                builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                        .subAggregation(AggregationBuilders.dateHistogram("tmd").field("tmd").calendarInterval(DateHistogramInterval.WEEK)
//                                .extendedBounds(new ExtendedBounds(startDate, endDate)).minDocCount(0L)
//                                .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow"))));
//                break;
//            case "MONTH":
//                builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                        .subAggregation(AggregationBuilders.dateHistogram("tmd").field("tmd").calendarInterval(DateHistogramInterval.MONTH)
//                                .extendedBounds(new ExtendedBounds(startDate, endDate)).minDocCount(0L)
//                                .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow"))));
//                break;
//            case "QUARTER":
//                builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                        .subAggregation(AggregationBuilders.dateHistogram("tmd").field("tmd").calendarInterval(DateHistogramInterval.QUARTER)
//                                .extendedBounds(new ExtendedBounds(startDate, endDate)).minDocCount(0L)
//                                .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow"))));
//                break;
//            case "YEAR":
//                builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                        .subAggregation(AggregationBuilders.dateHistogram("tmd").field("tmd").calendarInterval(DateHistogramInterval.YEAR)
//                                .extendedBounds(new ExtendedBounds(startDate, endDate)).minDocCount(0L)
//                                .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow"))));
//                break;
//            case "":
//                builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                        .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow")));
//                break;
//        }
//        SearchRequest searchRequest = new SearchRequest("assetflow").source(builder);
//        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        JSONObject jsonResponse = (JSONObject) JSON.parse(searchResponse.toString());
//        System.out.println("jsonResponse=" + jsonResponse);
//        JSONArray arraySterms = jsonResponse.getJSONObject("aggregations").getJSONObject("sterms#" + groupKey).getJSONArray("buckets");
//        System.out.println("aggregations=" + jsonResponse.getJSONObject("aggregations"));
//        System.out.println("arraySterms=" + arraySterms);
//        JSONArray arrayId_O = new JSONArray();
//        //判断是否有日期分组
//        if (dateGroup.equals("")) {
//            for (int i = 0; i < groupValue.size(); i++) {
//                //判断类型分组值是否为""：代表不类型分组
//                if (groupValue.getString(i).equals("")) {
//                    Double sum = 0.0;
//                    //遍历获取总数
//                    for (int j = 0; j < arraySterms.size(); j++) {
//                        Double value = arraySterms.getJSONObject(j).getJSONObject("sum#wn2qtynow").getDouble("value");
//                        sum += value;
//                    }
//                    arrayId_O.add(sum);
//                } else {
//                    //遍历获取该分组总数
//                    for (int j = 0; j < arraySterms.size(); j++) {
//                        if (groupValue.getString(i).equals(arraySterms.getJSONObject(j).getString("key"))) {
//                            Double value = arraySterms.getJSONObject(j).getJSONObject("sum#wn2qtynow").getDouble("value");
//                            arrayId_O.add(value);
//                        }
//                    }
//                }
//            }
//        } else {
//            for (int i = 0; i < groupValue.size(); i++) {
//                //判断类型分组值是否为""：代表不类型分组
//                if (groupValue.getString(i).equals("")) {
//                    JSONArray arraySum = new JSONArray();
//                    //遍历获取该分组总数
//                    for (int j = 0; j < arraySterms.size(); j++) {
//                        JSONArray arrayHistogram = arraySterms.getJSONObject(j).getJSONObject("date_histogram#tmd").getJSONArray("buckets");
//                        for (int k = 0; k < arrayHistogram.size(); k++) {
//                            Double value = arrayHistogram.getJSONObject(k).getJSONObject("sum#wn2qtynow").getDouble("value");
//                            if (arraySum.size() - 1 < k) {
//                                arraySum.add(value);
//                            } else {
//                                arraySum.set(k, arraySum.getDouble(k) + value);
//                            }
//                            System.out.println(arraySum);
//                        }
//                    }
//                    arrayId_O.add(arraySum);
//                } else {
//                    //遍历获取该分组总数
//                    for (int j = 0; j < arraySterms.size(); j++) {
//                        if (groupValue.getString(i).equals(arraySterms.getJSONObject(j).getString("key"))) {
//                            JSONArray arraySum = new JSONArray();
//                            JSONArray arrayHistogram = arraySterms.getJSONObject(j).getJSONObject("date_histogram#tmd").getJSONArray("buckets");
//                            for (int k = 0; k < arrayHistogram.size(); k++) {
//                                Double value = arrayHistogram.getJSONObject(k).getJSONObject("sum#wn2qtynow").getDouble("value");
//                                arraySum.add(value);
//                            }
//                            arrayId_O.add(arraySum);
//                        }
//                    }
//                }
//            }
//        }
//        System.out.println("arrayId_O=" + arrayId_O);
//        return retResult.ok(CodeEnum.OK.getCode(), arrayId_O);
//    }
//
//    @Override
//    public ApiResponse statisticsAvg(String id_C, String startDate, String endDate, Integer second, String groupKey, JSONArray groupValue) throws IOException {
//        SearchSourceBuilder builder = new SearchSourceBuilder();
//        BoolQueryBuilder query = new BoolQueryBuilder();
//        query.must(QueryBuilders.termQuery("id_C", id_C))
//                .must(QueryBuilders.rangeQuery("tmd").from(startDate, true).to(endDate, true));
//        builder.query(query).aggregation(AggregationBuilders.terms(groupKey).field(groupKey)
//                .subAggregation(AggregationBuilders.dateHistogram("tmd").field("tmd").fixedInterval(DateHistogramInterval.seconds(second))
//                .extendedBounds(new ExtendedBounds(startDate, endDate)).minDocCount(0L)
//                .subAggregation(AggregationBuilders.sum("wn2qtynow").field("data.wn2qtynow"))));
//        SearchRequest searchRequest = new SearchRequest("assetflow").source(builder);
//        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        JSONObject jsonResponse = (JSONObject) JSON.parse(searchResponse.toString());
//        System.out.println("jsonResponse=" + jsonResponse);
//        JSONArray arraySterms = jsonResponse.getJSONObject("aggregations").getJSONObject("sterms#" + groupKey).getJSONArray("buckets");
//        System.out.println("aggregations=" + jsonResponse.getJSONObject("aggregations"));
//        System.out.println("arraySterms=" + arraySterms);
//        System.out.println("arraySterms.size=" + arraySterms.size());
//        JSONArray arrayId_O = new JSONArray();
//        for (int i = 0; i < groupValue.size(); i++) {
//            if (groupValue.getString(i).equals("")) {
//                Double sum = 0.0;
//                JSONArray arrayHistogram = new JSONArray();
//                for (int j = 0; j < arraySterms.size(); j++) {
//                    arrayHistogram = arraySterms.getJSONObject(j).getJSONObject("date_histogram#tmd").getJSONArray("buckets");
//                    for (int k = 0; k < arrayHistogram.size(); k++) {
//                        Double value = arrayHistogram.getJSONObject(k).getJSONObject("sum#wn2qtynow").getDouble("value");
//                        sum += value;
//                    }
//                    System.out.println("arrayHistogram.size=" + arrayHistogram.size());
//                }
//                Double avg = sum / arrayHistogram.size();
//                DecimalFormat decimalFormat = new DecimalFormat("#.00");
//                avg = Double.parseDouble(decimalFormat.format(avg));
//                arrayId_O.add(avg);
//            } else {
//                for (int j = 0; j < arraySterms.size(); j++) {
//                    if (groupValue.getString(i).equals(arraySterms.getJSONObject(j).getString("key"))) {
//                        Double sum = 0.0;
//                        JSONArray arrayHistogram = arraySterms.getJSONObject(j).getJSONObject("date_histogram#tmd").getJSONArray("buckets");
//                        for (int k = 0; k < arrayHistogram.size(); k++) {
//                            Double value = arrayHistogram.getJSONObject(k).getJSONObject("sum#wn2qtynow").getDouble("value");
//                            sum += value;
//                        }
//                        System.out.println("arrayHistogram.size=" + arrayHistogram.size());
//                        Double avg = sum / arrayHistogram.size();
//                        DecimalFormat decimalFormat = new DecimalFormat("#.00");
//                        avg = Double.parseDouble(decimalFormat.format(avg));
//                        arrayId_O.add(avg);
//                    }
//                }
//            }
//        }
//        System.out.println("arrayId_O=" + arrayId_O);
//        return retResult.ok(CodeEnum.OK.getCode(), arrayId_O);
//    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse moveMoney(String id_U, String id_C, String listType, String grp, String fromId_A, String toId_A, Double money) {
//        authCheck.getUserUpdateAuth(id_U, id_C, listType, grp, "batch", new JSONArray().fluentAdd(""));
        Query queryFrom = new Query(new Criteria("_id").is(fromId_A));
        queryFrom.fields().include("aMoney");
        Asset asset = mongoTemplate.findOne(queryFrom, Asset.class);
        Double moneyNow = asset.getAMoney().getDouble("moneyNow");
        if (Double.doubleToLongBits(moneyNow) < Double.doubleToLongBits(money)) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.MONEY_NOT_HAVE.getCode(), "");

        }
        Update updateFrom = new Update();
        updateFrom.inc("aMoney.moneyNow", - money);
        UpdateResult updateResultFrom = mongoTemplate.updateFirst(queryFrom, updateFrom, Asset.class);
        System.out.println("updateResultFrom=" + updateResultFrom);
        if (updateResultFrom.getModifiedCount() > 0) {
            Query queryTo = new Query(new Criteria("_id").is(toId_A));
            Update updateTo = new Update();
            updateTo.inc("aMoney.moneyNow", money);
            UpdateResult updateResultTo = mongoTemplate.updateFirst(queryTo, updateTo, Asset.class);
            System.out.println("updateResultTo=" + updateResultTo);
            if (updateResultTo.getModifiedCount() > 0) {
//                JSONObject jsonMoneyflow = new JSONObject();
//                jsonMoneyflow.put("id_U", id_U);
//                jsonMoneyflow.put("id_C", id_C);
//                jsonMoneyflow.put("id_A", fromId_A);
//                jsonMoneyflow.put("id_AB", toId_A);
//                jsonMoneyflow.put("moneyNow", money);
//                jsonMoneyflow.put("zcndesc", "转账" + moneyNow + "元");
//                jsonMoneyflow.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));

                LogFlow logMoney = new LogFlow("moneyflow","","","mnyChg",id_U,"1000","","1000","1000",
                        "","",0,id_C,"","","", "转账" + moneyNow + "元", 5,null, null);
                logMoney.setLogData_money(toId_A,fromId_A,money);
                wsClient.sendWS(logMoney);
//                logUtil.sendLog("moneyflow", logMoney);
                return retResult.ok(CodeEnum.OK.getCode(), true);
            }
        }
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse pushMoney(String id_U, String id_C, String listType, String grp, String id_A, String id_O, Double money) {
//        authCheck.getUserUpdateAuth(id_U, id_C, listType, grp, "batch", new JSONArray().fluentAdd(""));
        Query queryFrom = new Query(new Criteria("_id").is(id_O));
        queryFrom.fields().include("oMoney");
        Order order = mongoTemplate.findOne(queryFrom, Order.class);
        Double moneyNow = order.getOMoney().getDouble("moneyNow");
        if (Double.doubleToLongBits(moneyNow) < Double.doubleToLongBits(money)) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.MONEY_NOT_HAVE.getCode(), "");

        }
        Update updateFrom = new Update();
        updateFrom.inc("oMoney.moneyNow", - money);
        UpdateResult updateResultFrom = mongoTemplate.updateFirst(queryFrom, updateFrom, Asset.class);
        System.out.println("updateResultFrom=" + updateResultFrom);
        if (updateResultFrom.getModifiedCount() > 0) {
            Query queryTo = new Query(new Criteria("_id").is(id_A));
            Update updateTo = new Update();
            updateTo.inc("aMoney.moneyNow", money);
            UpdateResult updateResultTo = mongoTemplate.updateFirst(queryTo, updateTo, Asset.class);
            System.out.println("updateResultTo=" + updateResultTo);
            if (updateResultTo.getModifiedCount() > 0) {
                JSONObject jsonMoneyflow = new JSONObject();
                jsonMoneyflow.put("id_U", id_U);
                jsonMoneyflow.put("id_C", id_C);
                jsonMoneyflow.put("id_AB", id_A);
                jsonMoneyflow.put("id_O", id_O);
                jsonMoneyflow.put("moneyNow", money);
                jsonMoneyflow.put("zcndesc", "从订单取钱" + moneyNow + "元");
                jsonMoneyflow.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//                logUtil.sendLog("moneyflow", jsonMoneyflow);
//                wsClient.sendWS(jsonMoneyflow);
                return retResult.ok(CodeEnum.OK.getCode(), true);
            }
        }
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse popMoney(String id_U, String id_C, String listType, String grp, String id_A, String id_O, Double money) {
//        authCheck.getUserUpdateAuth(id_U, id_C, listType, grp, "batch", new JSONArray().fluentAdd(""));
        Query queryFrom = new Query(new Criteria("_id").is(id_A));
        queryFrom.fields().include("aMoney");
        Asset asset = mongoTemplate.findOne(queryFrom, Asset.class);
        Double moneyNow = asset.getAMoney().getDouble("moneyNow");
        if (Double.doubleToLongBits(moneyNow) < Double.doubleToLongBits(money)) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.MONEY_NOT_HAVE.getCode(), "");

        }
        Update updateFrom = new Update();
        updateFrom.inc("aMoney.moneyNow", - money);
        UpdateResult updateResultFrom = mongoTemplate.updateFirst(queryFrom, updateFrom, Asset.class);
        System.out.println("updateResultFrom=" + updateResultFrom);
        if (updateResultFrom.getModifiedCount() > 0) {
            Query queryTo = new Query(new Criteria("_id").is(id_O));
            Update updateTo = new Update();
            updateTo.inc("oMoney.moneyNow", money);
            UpdateResult updateResultTo = mongoTemplate.updateFirst(queryTo, updateTo, Order.class);
            System.out.println("updateResultTo=" + updateResultTo);
            if (updateResultTo.getModifiedCount() > 0) {
                JSONObject jsonMoneyflow = new JSONObject();
                jsonMoneyflow.put("id_U", id_U);
                jsonMoneyflow.put("id_C", id_C);
                jsonMoneyflow.put("id_A", id_A);
                jsonMoneyflow.put("id_O", id_O);
                jsonMoneyflow.put("moneyNow", money);
                jsonMoneyflow.put("zcndesc", "往订单打钱" + moneyNow + "元");
                jsonMoneyflow.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//                logUtil.sendLog("moneyflow", jsonMoneyflow);
                return retResult.ok(CodeEnum.OK.getCode(), true);
            }
        }
        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
    }

//    @Override
//    @Transactional(noRollbackFor = ResponseException.class)
//    public ApiResponse addAsset(String id_U, String id_C, String listType, String grp, String ref, String id_A, String id_P, Integer seq, Integer lCR, Integer lUT,
//                           Double wn2qtyneed, Double wn4price, Integer wn0prior, String locAddr, JSONArray locSpace, JSONArray spaceQty) throws IOException {
////        authCheck.getUserUpdateAuth(id_U, id_C, listType, grp, "batch", new JSONArray().fluentAdd(""));
//        String id_Auth = dbUtils.getId_A(id_C, "a-auth");
//        Query queryAuth = new Query(new Criteria("_id").is(id_Auth));
//        queryAuth.fields().include("def.objlSAsset");
//        Asset asset = mongoTemplate.findOne(queryAuth, Asset.class);
//        JSONArray arrayObjlSAsset = asset.getDef().getJSONArray("objlSAsset");
//        //遍历objlSAsset找到对应的id_O
//        for (int i = 0; i < arrayObjlSAsset.size(); i++) {
//            JSONObject jsonObjlSAsset = arrayObjlSAsset.getJSONObject(i);
//            if (jsonObjlSAsset.getString("ref").equals(ref)) {
//                String id_O = jsonObjlSAsset.getString("id_O");
//                Query queryOrder = new Query(new Criteria("_id").is(id_O));
//                queryOrder.fields().include("oItem");
//                Order order = mongoTemplate.findOne(queryOrder, Order.class);
//                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//                BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//                queryBuilder.must(QueryBuilders.termQuery("id_P", id_P));
//                sourceBuilder.query(queryBuilder).from(0).size(1);
//                SearchRequest searchRequest = new SearchRequest("lbprod").source(sourceBuilder);
//                SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//                System.out.println("searchResponse=" + searchResponse);
//                JSONObject jsonHit = (JSONObject) JSON.parse(searchResponse.getHits().getHits()[0].getSourceAsString());
//                System.out.println("jsonHit=" + jsonHit);
//                Update updateOrder = new Update();
//                Boolean bool = true;
//                Integer oIndex = 0;
//                if (order.getOItem() != null && order.getOItem().getJSONArray("objItem") != null) {
//                    JSONArray arrayObjItem = order.getOItem().getJSONArray("objItem");
//                    oIndex = arrayObjItem.size() - 1;
//                    for (int j = 0; j < arrayObjItem.size(); j++) {
//                        JSONObject jsonObjItem = arrayObjItem.getJSONObject(j);
//                        if (jsonObjItem.getString("id_P").equals(id_P)) {
//                            bool = false;
//                            if (Double.doubleToLongBits(jsonObjItem.getDouble("wn4price")) == Double.doubleToLongBits(wn4price)) {
//                                updateOrder.inc("oItem.objItem." + j + ".wn2qtyneed", wn2qtyneed);
//                            } else {
//                                Double mongoWn2qtyneed = jsonObjItem.getDouble("wn2qtyneed");
//                                Double mongoWn4price = jsonObjItem.getDouble("wn4price");
//                                Double sum = wn2qtyneed * wn4price;
//                                Double mongoSum = mongoWn2qtyneed * mongoWn4price;
//                                Double avgWn4price = (sum + mongoSum) / (wn2qtyneed + mongoWn2qtyneed);
//                                updateOrder.set("oItem.objItem." + j + ".wn2qtyneed", wn2qtyneed + mongoWn2qtyneed);
//                                updateOrder.set("oItem.objItem." + j + ".wn4price", avgWn4price);
//                            }
//
//                        }
//                    }
//                }
//                if (bool) {
//                    JSONObject jsonOItem = (JSONObject) jsonHit.clone();
//                    JSONObject jsonWrdprep = new JSONObject();
//                    jsonWrdprep.put("cn", "");
//                    jsonOItem.remove("id_CB");
//                    jsonOItem.put("grpB", "1000");
//                    jsonOItem.put("seq", seq);
//                    jsonOItem.put("lCR", lCR);
//                    jsonOItem.put("lUT", lUT);
//                    jsonOItem.put("wn2qtyneed", wn2qtyneed);
//                    jsonOItem.put("wn4price", wn4price);
//                    jsonOItem.put("wrdprep", jsonWrdprep);
//                    jsonOItem.put("wn0prior", wn0prior);
//                    updateOrder.push("oItem.objItem", jsonOItem);
//                }
//                UpdateResult updateOrderResult = mongoTemplate.updateFirst(queryOrder, updateOrder, Order.class);
//                System.out.println("updateOrderResult=" + updateOrderResult);
//                if (updateOrderResult.getModifiedCount() > 0) {
//                    if (jsonObjlSAsset.getInteger("lAT") == 1) {
//                        JSONObject jsonInfo = (JSONObject) jsonHit.clone();
//                        jsonInfo.remove("lUT");
//                        jsonInfo.remove("lDC");
//                        jsonInfo.remove("id_CB");
//                        jsonInfo.remove("grpB");
//                        jsonInfo.remove("refB");
//                        jsonInfo.put("grp", "1030");
//                        jsonInfo.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//                        jsonInfo.put("tmk", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//                        //没有账户新建账户，有账户改变金额
//                        if (id_A.equals("")) {
//                            id_A = MongoUtils.GetObjectId();
//                            System.out.println("id_A=" + id_A);
//                            jsonInfo.put("id_A", id_A);
//                            JSONArray arrayView = new JSONArray();
//                            arrayView.add("info");
//                            arrayView.add("aMoney");
//                            JSONObject jsonMoney = new JSONObject();
//                            jsonMoney.put("moneyNow", wn2qtyneed * wn4price);
//                            Asset insertAsset = new Asset();
//                            insertAsset.setInfo(jsonInfo);
//                            AssetInfo jsonI = new AssetInfo()
//                            insertAsset.setInfo()
//                            insertAsset.setView(arrayView);
//                            insertAsset.setAMoney(jsonMoney);
//                            mongoTemplate.insert(insertAsset);
//                            jsonInfo.put("moneyNow", wn2qtyneed * wn4price);
//                            dbUtils.addES(jsonInfo, "lSAsset");
//                        } else {
//                            Query queryAsset = new Query(new Criteria("_id").is(id_A));
//                            Update updateAsset = new Update();
//                            updateAsset.inc("aMoney.moneyNow", wn2qtyneed * wn4price);
//                            UpdateResult updateResult = mongoTemplate.updateFirst(queryAsset, updateAsset, Asset.class);
//                            System.out.println("updateResult=" + updateResult);
//                        }
//                        //发日志
//                        jsonInfo.put("id_A", id_A);
//                        jsonInfo.put("wn2qtyneed", wn2qtyneed);
//                        jsonInfo.put("wn4price", wn4price);
//                        logUtil.sendLog("moneyflow", jsonInfo);
//                    }
//                    if (jsonObjlSAsset.getInteger("lAT") == 2) {
//                        storageService.pushAsset(id_U, id_C, id_O, oIndex, locAddr, locSpace, spaceQty);
//                    }
//                }
//
//
//            }
//        }
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse addMySpace(String id_U, JSONObject reqJson) throws IOException {
//        Query queryUser = new Query(new Criteria("_id").is(id_U));
//        queryUser.fields().include("info");
//        User user = mongoTemplate.findOne(queryUser, User.class);
//        String id_C = MongoUtils.GetObjectId();
//        JSONObject jsonInfo = new JSONObject();
//        jsonInfo.put("id_C", id_C);
//        jsonInfo.put("id_CP", id_C);
//        jsonInfo.put("wrdN", user.getInfo().getJSONObject("wrdN"));
//        jsonInfo.put("wrddesc", user.getInfo().getJSONObject("wrddesc"));
//        jsonInfo.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//        jsonInfo.put("tmk", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//        JSONArray arrayView = new JSONArray();
//        arrayView.add("info");
//        Comp comp = new Comp();
//        comp.setId(id_C);
//        comp.setInfo(jsonInfo);
//        comp.setView(arrayView);
//        comp.setBcdNet(5);
//        mongoTemplate.insert(comp);
//        dbUtils.addES(jsonInfo, "lncomp");
//        JSONObject jsonRolex = new JSONObject();
//        jsonRolex.put("grpU", "1001");
//        jsonRolex.put("id_C", id_C);
//        Update updateUser = new Update();
//        updateUser.set("rolex.objComp." + id_C, jsonRolex);
//        UpdateResult updateResult = mongoTemplate.updateFirst(queryUser, updateUser, User.class);
//        if (updateResult.getModifiedCount() > 0) {
//            return true;
//        }
//        return false;
        String new_id_C = MongoUtils.GetObjectId();

        //获取模块信息
        Query query = new Query(new Criteria("_id").is("cn_java"));
        query.fields().include("newComp");
        InitJava init = mongoTemplate.findOne(query, InitJava.class);
        JSONObject newComp = init.getNewComp();

        //Comp
        Comp comp = new Comp();
        JSONObject compJSON = newComp.getJSONObject("comp");

        JSONObject info = compJSON.getJSONObject("info");



        //如果reqJson为空，则添加默认公司，否则从reqJson里面取公司基本信息
        if (reqJson.isEmpty()){

            //默认公司信息
            info.put("id_CP",new_id_C);
            info.put("id_C",new_id_C);
            info.put("tmk", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
            info.put("tmd",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));

        }else{
            //用户填写公司信息
            info.put("wrdN",reqJson.getJSONObject("wrdN"));
            info.put("wrddesc",reqJson.getJSONObject("wrddesc"));
            //前端上传还是后端上传   还没定
            info.put("pic",reqJson.getString("pic"));
            info.put("ref",reqJson.getString("ref"));
            info.put("id_CP",new_id_C);
            info.put("id_C",new_id_C);
            info.put("tmk",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
            info.put("tmd",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));


        }
        //真公司标志
        comp.setBcdNet(5);
        comp.setInfo(JSONObject.parseObject(JSON.toJSONString(info), CompInfo.class));
        comp.setId(new_id_C);
        comp.setView( compJSON.getJSONArray("view"));
        mongoTemplate.insert(comp);

        //真公司，增ES，lNComp
        comp.getInfo().setId_C(new_id_C);
        dbUtils.addES((JSONObject) JSON.toJSON(comp.getInfo()), "lncomp");



        //a-module
        JSONObject moduleObject = newComp.getJSONObject("a-module");
        moduleObject.getJSONObject("info").put("id_C",new_id_C);
        //moduleObject.getJSONObject("info").put("wrdNC",comp.getInfo().get("wrdN"));
        //control.id_U
        JSONArray objDataList = moduleObject.getJSONObject("control").getJSONArray("objData");

        //Map<String, Object> rolex = new HashMap<>();
        JSONObject rolex = new JSONObject(4);
        //List objMod = new ArrayList<>();
        JSONArray objMod = new JSONArray();
        for (int i = 0; i < objDataList.size(); i++) {

            JSONObject indexObj =  objDataList.getJSONObject(i);

            JSONArray usersArray =  indexObj.getJSONArray("id_U");

            usersArray.add(id_U);

            //rolex.objComp.objMod.module
            //Map<String, Object> module = new HashMap<>();
            JSONObject module = new JSONObject(4);
            module.put("bcdState", indexObj.get("bcdState"));
            module.put("tfin", indexObj.get("tfin"));
            module.put("bcdLevel", indexObj.get("bcdLevel"));
            module.put("ref", indexObj.get("ref"));
            objMod.add(module);
        }


        //调用.addlSAsset
        addlSAsset(new_id_C,id_U, MongoUtils.GetObjectId() ,"a-module",JSON.toJSONString(moduleObject));
        //rolex   bug:lrefRole不知道在哪里拿
        rolex.put("objMod",objMod);
        rolex.put("id_C",new_id_C);
        rolex.put("grpU","1001");
        //rolex.put("ref",comp.getInfo().get("ref"));
//        Update updateUser = new Update();
//        updateUser.push("rolex.objComp", rolex);
//        mongoTemplate.updateFirst(new Query(new Criteria("_id").is(id_U)), updateUser, User.class);
        mongoTemplate.updateFirst(new Query(new Criteria("_id").is(id_U)), new Update().set("rolex.objComp."+new_id_C, rolex), User.class);




        //a-auth
        JSONObject authObject = newComp.getJSONObject("a-auth");
//        authObject.getJSONObject("role").getJSONObject("objAuth").putAll(setRole(new_id_C));
        authObject.getJSONObject("def").put("id_UM",id_U);//添加root id_U
        authObject.getJSONObject("info").put("id_C",new_id_C);
        //authObject.getJSONObject("info").put("wrdNC",comp.getInfo().get("wrdN"));

        //调用
        addlSAsset(new_id_C,id_U, MongoUtils.GetObjectId() ,"a-auth",JSON.toJSONString(authObject));

        //a-core
        JSONObject coreObject = newComp.getJSONObject("a-core");
        coreObject.getJSONObject("info").put("id_C",new_id_C);
        //coreObject.getJSONObject("info").put("wrdNC",comp.getInfo().get("wrdN"));

        //调用
        addlSAsset(new_id_C,id_U, MongoUtils.GetObjectId() ,"a-core",JSON.toJSONString(coreObject));



//        //增加lBUser
        Query infoQ = new Query(
                new Criteria("_id").is(id_U));
        infoQ.fields()
                .include("info");
        User user = mongoTemplate.findOne(infoQ, User.class);

        //Map<String, Object> infoData = new HashMap<>();
        JSONObject infoData = new JSONObject();
        infoData.put("id_U",id_U);
        infoData.put("wrdN",user.getInfo().getWrdN());
        infoData.put("pic",user.getInfo().getPic());
        infoData.put("tmd",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        infoData.put("tmk",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        infoData.put("id_CB",new_id_C);
        //infoData.put("wrdNReal","");
        infoData.put("refU","0");
        infoData.put("grpU","1001");
        infoData.put("refC",comp.getInfo().getRef());
        infoData.put("wrdNC",comp.getInfo().getWrdN());


        dbUtils.addES( infoData, "lbuser");
        return retResult.ok(CodeEnum.OK.getCode(), new_id_C);
    }


    @Override
    public ApiResponse cTriggerToTimeflow(String id_U, String id_C, String listType, String grp, Integer index, Boolean activate) throws IOException {
//        authCheck.getUserUpdateAuth(id_U, id_C, listType, grp, "batch", new JSONArray().fluentAdd(""));
        String id_A = dbUtils.getId_A(id_C, "a-trigger");
        Asset asset = (Asset) dbUtils.getMongoOneField(id_A, "cTrigger", Asset.class);
        JSONObject jsonObjInfo = asset.getCTrigger().getJSONObject("objData").getJSONArray("objInfo").getJSONObject(index);
        Boolean mongoActivate = jsonObjInfo.getBoolean("activate");
        Update update = new Update();
        if (mongoActivate != activate) {
            update.set("cTrigger.objData.objInfo." + index + ".activate", activate);
            UpdateResult updateResult = dbUtils.setMongoValue(id_A, "cTrigger.objData.objInfo." + index + ".activate", activate, Asset.class);
        }
        jsonObjInfo.remove("title");
        jsonObjInfo.remove("activate");
        JSONObject timeflow = new JSONObject();
        timeflow.put("data", jsonObjInfo);
        timeflow.put("id_U", id_U);
        timeflow.put("id_C", id_C);
        timeflow.put("id_A", id_A);
        timeflow.put("subType", "timer");
        timeflow.put("tmd", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("id_C", id_C)).must(QueryBuilders.termQuery("subType", "timer")).must(QueryBuilders.matchQuery("data.ref", jsonObjInfo.getString("ref")));
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest("timeflow").setQuery(queryBuilder);
        BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        System.out.println("bulkByScrollResponse.deleted=" + bulkByScrollResponse.getDeleted());
        if (activate) {
            dbUtils.addES(timeflow, "timeflow");
        }
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse prodPart(String id_P) {
        JSONObject jsonPart = new JSONObject();
        JSONObject recurCheckList = new JSONObject();
        recurCheckList.put(id_P, "");
        Prod prod = (Prod) dbUtils.getMongoOneFields(id_P, Arrays.asList("info", "part"), Prod.class);
        JSONArray arrayObjItem = prod.getPart().getJSONArray("objItem");
        jsonPart.put("name", prod.getInfo().getWrdN().getString("cn"));
        jsonPart.putAll(JSON.parseObject(JSON.toJSONString(prod.getInfo())));
        jsonPart.put("children", recursionProdPart(arrayObjItem, recurCheckList));
//        System.out.println("jsonPart=" + jsonPart);
        return retResult.ok(CodeEnum.OK.getCode(), jsonPart);
    }

    public Object recursionProdPart(JSONArray arrayObjItem, JSONObject recurCheckList) {
        JSONArray arrayChildren = new JSONArray();
        HashSet<String> setId_P = new HashSet();
        for (int i = 0; i < arrayObjItem.size(); i++) {
            String id_P = arrayObjItem.getJSONObject(i).getString("id_P");
            if (recurCheckList.getString(id_P) != null) {
                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PART_LEVEL_ERROR.getCode(), null);
            }
            if (arrayObjItem.getJSONObject(i).getString("bmdpt").equals(2)) {
                recurCheckList.put(id_P, "");
            }
                setId_P.add(id_P);
        }
        Query query = new Query(new Criteria("_id").in(setId_P).and("part").exists(true));
        query.fields().include("part");
        List<Prod> prods = mongoTemplate.find(query, Prod.class);
        JSONObject jsonProdPart = new JSONObject();
        for (Prod prod : prods) {
            jsonProdPart.put(prod.getId(), prod.getPart());
        }
        for (int i = 0; i < arrayObjItem.size(); i++) {
            JSONObject jsonObjItem = arrayObjItem.getJSONObject(i);
            JSONObject jsonChildren = new JSONObject();
            jsonChildren.put("name", jsonObjItem.getJSONObject("wrdN").getString("cn"));
            jsonChildren.putAll(jsonObjItem);
            //有下一层
            if (jsonProdPart.getJSONObject(jsonObjItem.getString("id_P")) != null) {
                JSONArray arrayPartObjItem = jsonProdPart.getJSONObject(jsonObjItem.getString("id_P")).getJSONArray("objItem");
                jsonChildren.put("children", recursionProdPart(arrayPartObjItem, recurCheckList));
            }
            arrayChildren.add(jsonChildren);
        }
        return arrayChildren;
    }

//    @Override
//    public ApiResponse prodPartChart(String id_P) {
//        JSONObject jsonPart = new JSONObject();
//        Prod prod = (Prod) dbUtils.getMongoOneFields(id_P, Arrays.asList("info", "part"), Prod.class);
//        JSONArray arrayObjItem = prod.getPart().getJSONArray("objItem");
//        jsonPart.put("name", prod.getInfo().getWrdN().getString("cn"));
//        jsonPart.putAll(JSON.parseObject(JSON.toJSONString(prod.getInfo())));
//        jsonPart.put("children", recursionProdPartChart(arrayObjItem));
//        System.out.println("jsonPart=" + jsonPart);
////        return jsonPart;
//        return retResult.ok(CodeEnum.OK.getCode(), jsonPart);
//    }
//
//    public Object recursionProdPartChart(JSONArray arrayObjItem) {
//        JSONArray arrayChildren = new JSONArray();
//        HashSet<String> setId_P = new HashSet();
//        for (int i = 0; i < arrayObjItem.size(); i++) {
//            JSONObject jsonObjItem = arrayObjItem.getJSONObject(i);
//            setId_P.add(jsonObjItem.getString("id_P"));
//        }
//        Query query = new Query(new Criteria("_id").in(setId_P).and("part").exists(true));
//        query.fields().include("part");
//        List<Prod> prods = mongoTemplate.find(query, Prod.class);
//        JSONObject jsonProdPart = new JSONObject();
//        for (Prod prod : prods) {
//            jsonProdPart.put(prod.getId(), prod.getPart());
//        }
//        for (int i = 0; i < arrayObjItem.size(); i++) {
//            JSONObject jsonObjItem = arrayObjItem.getJSONObject(i);
//            JSONObject jsonChildren = new JSONObject();
//            jsonChildren.put("name", jsonObjItem.getJSONObject("wrdN").getString("cn"));
//            jsonChildren.putAll(jsonObjItem);
//            if (jsonProdPart.getJSONObject(jsonObjItem.getString("id_P")) != null) {
//                JSONArray arrayPartObjItem = jsonProdPart.getJSONObject(jsonObjItem.getString("id_P")).getJSONArray("objItem");
//                jsonChildren.put("children", recursionProdPartChart(arrayPartObjItem));
//            }
//            arrayChildren.add(jsonChildren);
//        }
//        return arrayChildren;
//    }

    @Override
    public ApiResponse connectionComp(String id_C, String id_CB) throws IOException {

        HashSet setId_C = new HashSet();
        setId_C.add(id_C);
        setId_C.add(id_CB);
        List<Comp> comps = (List<Comp>) dbUtils.getMongoListField(setId_C, "info", Comp.class);
        Comp comp = new Comp();
        Comp compB = new Comp();
        if (id_C.equals(comps.get(0).getId())) {
            comp = comps.get(0);
            compB = comps.get(1);
        } else {
            comp = comps.get(1);
            compB = comps.get(0);
        }
        CompInfo compInfo = comp.getInfo();
        CompInfo compInfoB = compB.getInfo();
        lSBComp lsbcomp = new lSBComp(id_C, compInfo.getId_CP(), id_CB, compInfoB.getId_CP(), compInfo.getWrdN(),
                compInfo.getWrddesc(), compInfoB.getWrdN(), compInfoB.getWrddesc(), "1000", "1000",
                compInfo.getRef(), compInfoB.getRef(), compInfo.getPic(), compInfoB.getPic());
        dbUtils.addES(JSON.parseObject(JSON.toJSONString(lsbcomp)), "lsbcomp");
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse connectionProd(String id_C, String id_P) throws IOException {
        Prod prod = (Prod) dbUtils.getMongoOneField(id_P, "info", Prod.class);
        ProdInfo prodInfo = prod.getInfo();
        lBProd lbprod = new lBProd(id_P, prodInfo.getId_C(), prodInfo.getId_CP(), id_C, prodInfo.getWrdN(),
                prodInfo.getWrddesc(), "1000", "1000", "", "", "", "", "", 0);
        dbUtils.addES(JSON.parseObject(JSON.toJSONString(lbprod)), "lbprod");
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


    @Override
    @Transactional
    public ApiResponse splitOrder(String id_O, JSONArray arrayIndex) {
        Order order = (Order) dbUtils.getMongoOneFields(id_O, Arrays.asList("info", "oItem", "oStock", "action"), Order.class);
        OrderInfo orderInfo = order.getInfo();
        if (orderInfo.getLST() > 6) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.CONFIRMED_CANNOT_BE_DEL.getCode(), null);
        }
        String newId_O = MongoUtils.GetObjectId();
        System.out.println("newId_O=" + newId_O);
        JSONArray arrayOItem = order.getOItem().getJSONArray("objItem");
        JSONArray arrayAction = order.getAction().getJSONArray("objAction");
        JSONArray arrayNewOItem = new JSONArray();
        JSONArray arrayNewAction = new JSONArray();
        HashSet setId_O = new HashSet();
        JSONObject jsonId_O = new JSONObject();
        BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, Order.class);
        Query query = new Query(new Criteria("_id").is(id_O));
        Update update = new Update();
        Double wn2qtySum = 0.0;
        for (int i = 0; i < arrayIndex.size(); i++) {
            JSONObject jsonIndex = arrayIndex.getJSONObject(i);
            Integer index = jsonIndex.getInteger("index");
            JSONArray arrayWn2qty = jsonIndex.getJSONArray("wn2qty");
            Double wn2qty = arrayWn2qty.stream().mapToDouble(w -> Double.parseDouble(w.toString())).sum();
            wn2qtySum += wn2qty;
            JSONObject jsonOItem = arrayOItem.getJSONObject(index);
            JSONObject jsonAction = arrayAction.getJSONObject(index);

            jsonOItem.put("id_O", newId_O);
            jsonOItem.put("wn2qtyneed", wn2qty);
            jsonAction.put("id_O", newId_O);

            update.inc("oItem.objItem." + index + ".wn2qtyneed", -wn2qty);

            JSONArray arrayUpPrnt = jsonAction.getJSONArray("upPrnts");
            JSONArray arraySubPart = jsonAction.getJSONArray("subParts");
            JSONArray arrayPrtNext = jsonAction.getJSONArray("prtNext");
            JSONArray arrayPrtPrev = jsonAction.getJSONArray("prtPrev");
            if (arrayUpPrnt.size() > 0) {
                for (int j = 0; j < arrayUpPrnt.size(); j++) {
                    update.inc("action.objAction." + j + "upPrnts.wn2qtyneed", -arrayWn2qty.getDouble(j));
                    JSONObject jsonUpPrnt = arrayUpPrnt.getJSONObject(j);
                    String upPrntId_O = jsonUpPrnt.getString("id_O");
                    String upPrntIndex = jsonUpPrnt.getString("index");
                    setId_O.add(upPrntId_O);
                    JSONObject jsonType = new JSONObject();
                    JSONObject jsonUpIndex = new JSONObject();
                    JSONObject jsonUp = new JSONObject();
                    if (jsonId_O.getJSONObject(upPrntId_O) != null) {
                        jsonType = jsonId_O.getJSONObject(upPrntId_O);
                        if (jsonType.getJSONObject("up") != null) {
                            jsonUpIndex = jsonType.getJSONObject("up");
                            if (jsonUpIndex.getJSONObject(upPrntIndex) != null) {
                                jsonUp = jsonUpIndex.getJSONObject(upPrntIndex);
                            }
                        }
                    }
                    JSONObject jsonUpdate = new JSONObject();
                    jsonUpdate.put("id_O", newId_O);
                    jsonUpdate.put("index", arrayNewAction.size());
                    jsonUp.put(id_O + "." + index, jsonUpdate);
                    jsonUpIndex.put(upPrntIndex, jsonUp);
                    jsonType.put("up", jsonUpIndex);
                    jsonId_O.put(upPrntId_O, jsonType);
                }
                System.out.println("jsonId_O=" + jsonId_O);
            }
            if (arraySubPart.size() > 0) {
                for (int j = 0; j < arraySubPart.size(); j++) {
                    JSONObject jsonSubPart = arraySubPart.getJSONObject(j);
                    String subPartId_O = jsonSubPart.getString("id_O");
                    String subPartIndex = jsonSubPart.getString("index");
                    setId_O.add(subPartId_O);
                    JSONObject jsonType = new JSONObject();
                    JSONObject jsonSubIndex = new JSONObject();
                    JSONObject jsonSub = new JSONObject();
                    if (jsonId_O.getJSONObject(subPartId_O) != null) {
                        jsonType = jsonId_O.getJSONObject(subPartId_O);
                        if (jsonType.getJSONObject("sub") != null) {
                            jsonSubIndex = jsonType.getJSONObject("sub");
                            if (jsonSubIndex.getJSONObject(subPartIndex) != null) {
                                jsonSub = jsonSubIndex.getJSONObject(subPartIndex);
                            }
                        }
                    }
                    JSONObject jsonUpdate = new JSONObject();
                    jsonUpdate.put("id_O", newId_O);
                    jsonUpdate.put("index", arrayNewAction.size());
                    jsonUpdate.put("wn2qty", arrayWn2qty.getDouble(j) * jsonSubPart.getDouble("qtyEach"));
                    jsonSub.put(id_O + "." + index, jsonUpdate);
                    jsonSubIndex.put(subPartIndex, jsonSub);
                    jsonType.put("sub", jsonSubIndex);
                    jsonId_O.put(subPartId_O, jsonType);
                }
                System.out.println("jsonId_O=" + jsonId_O);
            }
            if (arrayPrtNext.size() > 0) {
                for (int j = 0; j < arrayPrtNext.size(); j++) {
                    JSONObject jsonPrtNext = arrayPrtNext.getJSONObject(j);
                    String prtNextId_O = jsonPrtNext.getString("id_O");
                    String prtNextIndex = jsonPrtNext.getString("index");
                    setId_O.add(prtNextId_O);
                    JSONObject jsonType = new JSONObject();
                    JSONObject jsonNextIndex = new JSONObject();
                    JSONObject jsonNext = new JSONObject();
                    if (jsonId_O.getJSONObject(prtNextId_O) != null) {
                        jsonType = jsonId_O.getJSONObject(prtNextId_O);
                        if (jsonType.getJSONObject("next") != null) {
                            jsonNextIndex = jsonType.getJSONObject("next");
                            if (jsonNextIndex.getJSONObject(prtNextIndex) != null) {
                                jsonNext = jsonNextIndex.getJSONObject(prtNextIndex);
                            }
                        }
                    }
                    JSONObject jsonUpdate = new JSONObject();
                    jsonUpdate.put("id_O", newId_O);
                    jsonUpdate.put("index", arrayNewAction.size());
                    System.out.println("jsonUpdate=" + jsonUpdate);
                    jsonNext.put(id_O + "." + index, jsonUpdate);
                    jsonNextIndex.put(prtNextIndex, jsonNext);
                    jsonType.put("next", jsonNextIndex);
                    jsonId_O.put(prtNextId_O, jsonType);
                }
                System.out.println("jsonId_O=" + jsonId_O);
            }
            if (arrayPrtPrev.size() > 0) {
                for (int j = 0; j < arrayPrtPrev.size(); j++) {
                    JSONObject jsonPrtPrev = arrayPrtPrev.getJSONObject(j);
                    String prtPrevId_O = jsonPrtPrev.getString("id_O");
                    String prtPrevIndex = jsonPrtPrev.getString("index");
                    setId_O.add(prtPrevId_O);
                    JSONObject jsonType = new JSONObject();
                    JSONObject jsonPrevIndex = new JSONObject();
                    JSONObject jsonPrev = new JSONObject();
                    if (jsonId_O.getJSONObject(prtPrevId_O) != null) {
                        jsonType = jsonId_O.getJSONObject(prtPrevId_O);
                        if (jsonType.getJSONObject("prev") != null) {
                            jsonPrevIndex = jsonType.getJSONObject("prev");
                            if (jsonPrevIndex.getJSONObject(prtPrevIndex) != null) {
                                jsonPrev = jsonPrevIndex.getJSONObject(prtPrevIndex);
                            }
                        }
                    }
                    JSONObject jsonUpdate = new JSONObject();
                    jsonUpdate.put("id_O", newId_O);
                    jsonUpdate.put("index", arrayNewAction.size());
                    jsonPrev.put(id_O + "." + index, jsonUpdate);
                    jsonPrevIndex.put(prtPrevIndex, jsonPrev);
                    jsonType.put("prev", jsonPrevIndex);
                    jsonId_O.put(prtPrevId_O, jsonType);
                }
                System.out.println("jsonId_O=" + jsonId_O);
            }
            arrayNewOItem.add(jsonOItem);
            arrayNewAction.add(jsonAction);
        }
        bulk.updateOne(query, update);
        System.out.println("setId_O=" + setId_O);
        System.out.println("jsonId_O=" + jsonId_O);
        //新订单
        orderInfo.setTmd(DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        orderInfo.setTmk(DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        JSONObject jsonNewOItem = new JSONObject();
        jsonNewOItem.put("wn4price", order.getOItem().getDouble("wn4price"));
        jsonNewOItem.put("wn2qty", order.getOItem().getDouble("wn2qty") - wn2qtySum);
        jsonNewOItem.put("objItem", arrayNewOItem);
        JSONObject jsonNewAction = new JSONObject();
        jsonNewAction.putAll(order.getAction());
        jsonNewAction.put("objAction", arrayNewAction);

        Order newOrder = new Order();
        newOrder.setId(newId_O);
        newOrder.setInfo(orderInfo);
        newOrder.setOItem(jsonNewOItem);
        newOrder.setAction(jsonNewAction);
        bulk.insert(newOrder);

        List<Order> relationOrders = (List<Order>) dbUtils.getMongoListField(setId_O, "action", Order.class);
        for (Order relationOrder : relationOrders) {
            String relationId_O = relationOrder.getId();
            Query queryRelation = new Query(new Criteria("_id").is(relationId_O));
            Update updateRelation = new Update();
            JSONArray arrayRelationAction = relationOrder.getAction().getJSONArray("objAction");
            JSONObject jsonType = jsonId_O.getJSONObject(relationId_O);
            if (jsonType.getJSONObject("up") != null) {
                JSONObject jsonUpIndex = jsonType.getJSONObject("up");
                jsonUpIndex.forEach((upIndex, v) ->{
                    JSONObject jsonUp = jsonUpIndex.getJSONObject(upIndex);
                    JSONObject jsonUpAction = arrayRelationAction.getJSONObject(Integer.parseInt(upIndex));
                    System.out.println("jsonUp=" + jsonUp);
                    System.out.println("jsonUpAction=" + jsonUpAction);
                    JSONArray arrayUpSubPart = jsonUpAction.getJSONArray("subParts");
                    for (int i = 0; i < arrayUpSubPart.size(); i++) {
                        JSONObject jsonUpSubPart = arrayUpSubPart.getJSONObject(i);
                        String key = jsonUpSubPart.getString("id_O") + "." + jsonUpSubPart.getInteger("index");
                        if (jsonUp.getJSONObject(key) != null) {
                            JSONObject jsonUpdate = jsonUp.getJSONObject(key);
                            jsonUpSubPart.put("id_O", jsonUpdate.getString("id_O"));
                            jsonUpSubPart.put("index", jsonUpdate.getInteger("index"));
                            updateRelation.push("action.objAction." + upIndex + ".subParts", jsonUpSubPart);
                            break;
                        }
                    }
                });
            }
            if (jsonType.getJSONObject("sub") != null) {
                JSONObject jsonSubIndex = jsonType.getJSONObject("sub");
                jsonSubIndex.forEach((subIndex, v) ->{
                    JSONObject jsonSub = jsonSubIndex.getJSONObject(subIndex);
                    JSONObject jsonSubAction = arrayRelationAction.getJSONObject(Integer.parseInt(subIndex));
                    System.out.println("jsonSub=" + jsonSub);
                    System.out.println("jsonSubAction=" + jsonSubAction);
                    JSONArray arraySubUpPrnt = jsonSubAction.getJSONArray("upPrnts");
                    for (int i = 0; i < arraySubUpPrnt.size(); i++) {
                        JSONObject jsonSubUpPrnt = arraySubUpPrnt.getJSONObject(i);
                        String key = jsonSubUpPrnt.getString("id_O") + "." + jsonSubUpPrnt.getInteger("index");
                        if (jsonSub.getJSONObject(key) != null) {
                            JSONObject jsonUpdate = jsonSub.getJSONObject(key);
                            Double wn2qty = jsonUpdate.getDouble("wn2qty");
                            jsonSubUpPrnt.put("id_O", jsonUpdate.getString("id_O"));
                            jsonSubUpPrnt.put("index", jsonUpdate.getInteger("index"));
                            jsonSubUpPrnt.put("wn2qtyneed", wn2qty);
                            updateRelation.push("action.objAction." + subIndex + ".upPrnts", jsonSubUpPrnt);
                            updateRelation.inc("action.objAction." + subIndex + ".upPrnts." + i + ".wn2qtyneed", -wn2qty);
                            break;
                        }
                    }
                });
            }
            if (jsonType.getJSONObject("next") != null) {
                JSONObject jsonNextIndex = jsonType.getJSONObject("next");
                jsonNextIndex.forEach((nextIndex, v) ->{
                    JSONObject jsonNext = jsonNextIndex.getJSONObject(nextIndex);
                    JSONObject jsonNextAction = arrayRelationAction.getJSONObject(Integer.parseInt(nextIndex));
                    System.out.println("jsonNext=" + jsonNext);
                    System.out.println("jsonNextAction=" + jsonNextAction);
                    JSONArray arrayNextPrtPrev = jsonNextAction.getJSONArray("prtPrev");
                    for (int i = 0; i < arrayNextPrtPrev.size(); i++) {
                        JSONObject jsonNextPrtPrev = arrayNextPrtPrev.getJSONObject(i);
                        String key = jsonNextPrtPrev.getString("id_O") + "." + jsonNextPrtPrev.getInteger("index");
                        if (jsonNext.getJSONObject(key) != null) {
                            JSONObject jsonUpdate = jsonNext.getJSONObject(key);
                            jsonNextPrtPrev.put("id_O", jsonUpdate.getString("id_O"));
                            jsonNextPrtPrev.put("index", jsonUpdate.getInteger("index"));
                            updateRelation.push("action.objAction." + nextIndex + ".prtPrev", jsonNextPrtPrev);
                            break;
                        }
                    }
                });
            }
            if (jsonType.getJSONObject("prev") != null) {
                JSONObject jsonPrevIndex = jsonType.getJSONObject("prev");
                jsonPrevIndex.forEach((prevIndex, v) ->{
                    JSONObject jsonPrev = jsonPrevIndex.getJSONObject(prevIndex);
                    JSONObject jsonPrevAction = arrayRelationAction.getJSONObject(Integer.parseInt(prevIndex));
                    System.out.println("jsonPrev=" + jsonPrev);
                    System.out.println("jsonPrevAction=" + jsonPrevAction);
                    JSONArray arrayPrevPrtNext = jsonPrevAction.getJSONArray("prtNext");
                    for (int i = 0; i < arrayPrevPrtNext.size(); i++) {
                        JSONObject jsonPrevPrtNext = arrayPrevPrtNext.getJSONObject(i);
                        String key = jsonPrevPrtNext.getString("id_O") + "." + jsonPrevPrtNext.getInteger("index");
                        if (jsonPrev.getJSONObject(key) != null) {
                            JSONObject jsonUpdate = jsonPrev.getJSONObject(key);
                            jsonPrevPrtNext.put("id_O", jsonUpdate.getString("id_O"));
                            jsonPrevPrtNext.put("index", jsonUpdate.getInteger("index"));
                            updateRelation.push("action.objAction." + prevIndex + ".prePrev", jsonPrevPrtNext);
                            break;
                        }
                    }
                });
            }
            bulk.updateOne(queryRelation, updateRelation);
        }
        BulkWriteResult bulkResult = bulk.execute();
        if (bulkResult.getModifiedCount() > 0) {
            return retResult.ok(CodeEnum.OK.getCode(), null);
        }
        throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.ORDER_ALL_ERROR.getCode(), null);
    }
}
