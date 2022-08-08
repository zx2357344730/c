package com.cresign.details.utils;

import com.alibaba.fastjson.JSONObject;
import com.cresign.details.enumeration.DetailsEnum;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.*;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * ##Author: KEVIN
 * ##version: 1.0
 * ##description: 用于 updateSingleUtil api 的处理
 * ##updated: 2021-10-12
 */
@Component
public class UpdateSingleUtil {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RetResult retResult;

    @Autowired
    private StringRedisTemplate redisTemplate0;


    @Autowired
    private DbUtils dbUtils;

//    @Autowired
//    private RestHighLevelClient restHighLevelClient;
//
//    @Resource
//    private HttpServletRequest request;

    public ApiResponse ProdUpdateSingle(String ID_, String id_C, JSONObject data, Set <String> cardList, JSONObject listCol, String listType, String lang) throws IOException {
        Update update = new Update();
        Query query = new Query(new Criteria("_id").is(ID_));

        // Start to update, first ES updating
        if (listType.equals("lBProd")) {
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_P", ID_))
                    .must(QueryBuilders.termQuery("id_CB", id_C));
            dbUtils.updateListCol(queryBuilder, "lbprod", listCol);
        }
        else if (listType.equals("lSProd")) {
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_P", ID_))
                    .must(QueryBuilders.termQuery("id_C", id_C));
            dbUtils.updateListCol(queryBuilder, "lsprod", listCol); //{"lST":7, "wrdN": {"cn":"ffff"}}

            QueryBuilder queryBuilder1 = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_P", ID_))
                    .must(QueryBuilders.termQuery("id_C", id_C));
            dbUtils.updateListCol(queryBuilder1, "lbprod", listCol);
        }

        // Then updating Mongo
        for (String cardPicked : cardList) {

            //x卡片处理 putting xCards into objComp
            if (cardPicked.endsWith("x")){
                update.set(cardPicked+"."+id_C,
                        data.getJSONObject(cardPicked).getJSONObject("objComp").getJSONObject(id_C));
            }else{
                // putting regular cards into regular JSON
                update.set(cardPicked, data.get(cardPicked));
            }
        }
        try {
            System.out.println("updating MDB"+update);
            if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                update.inc("tvs", 1);
                mongoTemplate.updateFirst(query, update, Prod.class);
            }
        } catch (RuntimeException e) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.UPDATE_SINGLE_ERROR.getCode(), null);
        }
        return retResult.ok(CodeEnum.OK.getCode(), cardList);

    }

    public ApiResponse AssetUpdateSingle(String ID_, String id_C, JSONObject data, Set <String> cardList, JSONObject listCol, String listType, String lang) throws IOException {
        Update update = new Update();
        Query query = new Query(new Criteria("_id").is(ID_));

        // Start to update, first ES updating

            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_A", ID_))
                    .must(QueryBuilders.termQuery("id_C", id_C));
            dbUtils.updateListCol(queryBuilder, "lSAsset", listCol);

        // Then updating Mongo
        for (String cardPicked : cardList) {

            //x卡片处理 putting xCards into objComp
            if (cardPicked.endsWith("x")){
                update.set(cardPicked+"."+id_C,
                        data.getJSONObject(cardPicked).getJSONObject("objComp").getJSONObject(id_C));
            } else if (!cardPicked.equals("role"))
                //role and ?? cards will be special and cannot use upSingle to update
            {
                // putting regular cards into regular JSON
                update.set(cardPicked, data.get(cardPicked));
            }

            if (cardPicked.equals("menu"))
            {
                redisTemplate0.delete("details:get_menus:compId-" + id_C);
            }
        }
        try {
            System.out.println("updating MDB"+update);
            if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                update.inc("tvs", 1);
                mongoTemplate.updateFirst(query, update, Asset.class);
            }
        } catch (RuntimeException e) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.UPDATE_SINGLE_ERROR.getCode(), null);
        }

        return retResult.ok(CodeEnum.OK.getCode(), cardList);

    }

    public ApiResponse UserUpdateSingle(String ID_, String id_C, JSONObject data, Set <String> cardList, JSONObject listCol, String listType, String lang) throws IOException {

        Update update = new Update();
        Query query = new Query(new Criteria("_id").is(ID_));

        // Start to update, first ES updating
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    //条件1：
                    .must(QueryBuilders.termQuery("id_U", ID_))
                    .must(QueryBuilders.termQuery("id_CB", id_C));
            dbUtils.updateListCol(queryBuilder, "lbuser", listCol);

        // Then updating Mongo
        for (String cardPicked : cardList) {
            //x卡片处理 putting xCards into objComp
//                update.set(cardPicked+".objComp."+id_C,
//                      data.getJSONObject(cardPicked).getJSONObject("objComp").getJSONObject(id_C));
            if (cardPicked.endsWith("x")){
                update.set(cardPicked+"."+id_C,
                        data.getJSONObject(cardPicked).getJSONObject("objComp").getJSONObject(id_C));
            } else if (cardPicked.equals("view"))
            {
                update.set(cardPicked, data.get(cardPicked));
            }
        }

        try {
            if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                update.inc("tvs", 1);
                mongoTemplate.updateFirst(query, update, User.class);
            }
        } catch (RuntimeException e) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.UPDATE_SINGLE_ERROR.getCode(), null);
        }
        return retResult.ok(CodeEnum.OK.getCode(), cardList);

    }

    public ApiResponse CompUpdateSingle(String ID_, String id_C, JSONObject data, Set <String> cardList, JSONObject listCol, String listType, String lang) throws IOException {

        Update update = new Update();
        Query query = new Query(new Criteria("_id").is(ID_));


        // Start to update, first ES updating
        if (listType.equals("lBComp")) {
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_CB", id_C))
                    .must(QueryBuilders.termQuery("id_C", ID_));
            dbUtils.updateListCol(queryBuilder, "lsbcomp", listCol);
        }
        else if (listType.equals("lSComp")) {
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_C", id_C))
                    .must(QueryBuilders.termQuery("id_CB", ID_));
            dbUtils.updateListCol(queryBuilder, "lsbcomp", listCol);
        }

        // Then updating Mongo
        for (String cardPicked : cardList) {

            //x卡片处理 putting xCards into objComp
            if (cardPicked.endsWith("x")){
                update.set(cardPicked+".objComp."+id_C,
                data.getJSONObject(cardPicked).getJSONObject("objComp").getJSONObject(id_C));
            }else{
            // putting regular cards into regular JSON
                update.set(cardPicked, data.get(cardPicked));
            }
        }
        try {
            if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                update.inc("tvs", 1);
                mongoTemplate.updateFirst(query, update, Comp.class);
            }
        } catch (RuntimeException e) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.UPDATE_SINGLE_ERROR.getCode(), null);
        }
        return retResult.ok(CodeEnum.OK.getCode(), cardList);
    }

    public ApiResponse OrderUpdateSingle(String ID_, String id_C, JSONObject data, Set <String> cardList, JSONObject listCol, String listType, String lang) throws IOException {

        Update update = new Update();
        Query query = new Query(new Criteria("_id").is(ID_));

        // Start to update, first ES updating
        if (listType.equals("lBOrder")) {
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_O", ID_))
                    .must(QueryBuilders.termQuery("id_CB", id_C));
            dbUtils.updateListCol(queryBuilder, "lsborder", listCol);
        }
        else if (listType.equals("lSOrder")) {
            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("id_O", ID_))
                    .must(QueryBuilders.termQuery("id_C", id_C));
            dbUtils.updateListCol(queryBuilder, "lsborder", listCol);
        }

        // Then updating Mongo
        for (String cardPicked : cardList) {
            //x卡片处理 putting xCards into objComp
            if (cardPicked.endsWith("x")){
                update.set(cardPicked+".objComp."+id_C,
                        data.getJSONObject(cardPicked).getJSONObject("objComp").getJSONObject(id_C));
            }else{
                // putting regular cards into regular JSON
                update.set(cardPicked, data.get(cardPicked));
            }
        }
        try {
            if (!ObjectUtils.isEmpty(update.getUpdateObject())) {
                update.inc("tvs", 1);
                mongoTemplate.updateFirst(query, update, Order.class);
            }
        } catch (RuntimeException e) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, DetailsEnum.UPDATE_SINGLE_ERROR.getCode(), null);
        }
        return retResult.ok(CodeEnum.OK.getCode(), cardList);
    }


}