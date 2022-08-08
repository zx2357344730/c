package com.cresign.details.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cresign.details.enumeration.DetailsEnum;
import com.cresign.details.service.LinkService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Comp;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LinkServicelmpl implements LinkService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RetResult retResult;

    @Autowired
    private DbUtils dbUtils;

//
//    @Autowired
//    private AuthFilterClient authFilterClient;


    @Override
    public ApiResponse setCompLink(String id_U, String id_C, String id_CB, String grp, String grpB, String listType) throws IOException {

        //指定ES索引
        IndexRequest request = new IndexRequest("lsbcomp");

        //id_C公司
        Query lsCompCondition = new Query(new Criteria("_id").is(id_C).and("info").exists(true));
        lsCompCondition.fields().include("info");
        Comp lsComp = mongoTemplate.findOne(lsCompCondition, Comp.class);
        if (lsComp == null) {
            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.COMP_NOT_FOUND.getCode(), "");

        }

        //id_B公司
        Query lbCompCondition = new Query(new Criteria("_id").is(id_CB).and("info").exists(true));
        lbCompCondition.fields().include("info");
        Comp lbComp = mongoTemplate.findOne(lbCompCondition, Comp.class);
        if (lbComp == null) {

            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.COMP_NOT_FOUND.getCode(), "");


        }
        JSONObject listObject = new JSONObject();

        listObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
        listObject.put("tmd", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
        //ls
        listObject.put("id_C", id_C);
        listObject.put("wrdNC", lsComp.getInfo().getWrdN());
        listObject.put("wrddesc", lsComp.getInfo().getWrddesc());
        listObject.put("refC", lsComp.getInfo().getRef());
        listObject.put("picC", lsComp.getInfo().getPic());
        listObject.put("id_CP", lsComp.getInfo().getId_CP());
        listObject.put("grp", grp);
        //lb
        listObject.put("id_CB", id_CB);
        listObject.put("wrdNCB", lbComp.getInfo().getWrdN());
        listObject.put("wrddescB", lbComp.getInfo().getWrddesc());
        listObject.put("refCB", lbComp.getInfo().getRef());
        listObject.put("picCB", lbComp.getInfo().getPic());
        listObject.put("id_CBP", lbComp.getInfo().getId_CP());
        listObject.put("grpB", grpB);

        //真假公司标志
        int bcdNet = 0;
        String compID = "";
        String listID = "";
        if (listType.equals("lSComp")) {
            listID = "id_CB";
            compID = "id_C";
            bcdNet = dbUtils.judgeComp(id_C, id_CB);
        } else {
            listID = "id_C";
            compID = "id_CB";
            bcdNet = dbUtils.judgeComp(id_CB, id_C);
        }
        SearchRequest searchRequest = new SearchRequest("lSBComp");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                //条件1：自己公司id
                .must(QueryBuilders.termQuery(compID, id_C))
                // 条件2：别人公司id
                .must(QueryBuilders.termQuery(listID,id_CB));

        sourceBuilder.query(queryBuilder);

        searchRequest.source(sourceBuilder);

        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        if (search.getHits().getHits().length > 0) {

            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.LIST_OBJECT_NO_HAVE.getCode(), null);

        }



//        listObject.put("bcdNet", bcdNet);

        request.source(listObject, XContentType.JSON);
        restHighLevelClient.index(request, RequestOptions.DEFAULT);

        return retResult.ok(CodeEnum.OK.getCode(),null);

    }

    @Override
    public ApiResponse setProdLink(String id_U, String id_C, String id_P, String grp) throws IOException {

        /*
               如果已经链接过这个产品了，还能链接吗，现在是可以（还没限制）
         */


            SearchRequest searchRequest = new SearchRequest("lSProd");

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    //条件1：
                    .must(QueryBuilders.termQuery("id_P", id_P))////条件2.must(QueryBuilders.termQuery("id_C",id_C))

                    ;

            sourceBuilder.query(queryBuilder);

            searchRequest.source(sourceBuilder);

            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


            if (search.getHits().getHits().length > 0) {

                SearchHit hit = search.getHits().getHits()[0];

                hit.getSourceAsMap().put("tmd",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
                hit.getSourceAsMap().put("tmk",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));

                hit.getSourceAsMap().put("id_CB",id_C);
                hit.getSourceAsMap().put("grpB",grp);
                hit.getSourceAsMap().put("refB","");
                IndexRequest request = new IndexRequest("lBProd").source(hit.getSourceAsMap(), XContentType.JSON);
                //request.source(hit.getSourceAsMap(), XContentType.JSON);
                restHighLevelClient.index(request, RequestOptions.DEFAULT);

                return retResult.ok(CodeEnum.OK.getCode(),null);
            }

            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.LIST_OBJECT_NO_HAVE.getCode(), null);

    }



    @Override
    public ApiResponse updateProdListType(String id_U, String id_C, String grp, String listType,String id_P) throws IOException {

            String cid = "id_C";

            if (listType.equals("lBProd")){
                cid = "id_CB";
            }


            //获取ES
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    //条件1：
                    .must(QueryBuilders.termQuery("id_P",id_P))//
                    //条件2
                    .must(QueryBuilders.termQuery(cid, id_C));


            SearchRequest srb = new SearchRequest(listType);
            searchSourceBuilder.query(queryBuilder);
            srb.source(searchSourceBuilder);

            SearchResponse search = restHighLevelClient.search(srb, RequestOptions.DEFAULT);

            if (search.getHits().getHits().length > 0) {

                SearchHit hit = search.getHits().getHits()[0];
                DeleteRequest deleteRequest = new DeleteRequest();


                if (listType.equals("lSProd")){
                    //转换成零件，组别、编号、怎么弄？
                    hit.getSourceAsMap().put("id_CB",id_C);
                    hit.getSourceAsMap().put("grpB","1001");
                    hit.getSourceAsMap().put("lDC",5);
//                    hit.getSourceAsMap().put("bcdNet",2);
                    hit.getSourceAsMap().put("tmd",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
                    dbUtils.addES((JSONObject) hit.getSourceAsMap(),"lBProd");

                    deleteRequest.id(hit.getId());
                    deleteRequest.index(listType);
                    restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
                }else{
//                    hit.getSourceAsMap().remove("bcdNet");
                    hit.getSourceAsMap().remove("id_CB");
                    hit.getSourceAsMap().remove("grpB");
                    hit.getSourceAsMap().put("id_C",id_C);
                    hit.getSourceAsMap().put("tmd",DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));

                    dbUtils.addES((JSONObject) hit.getSourceAsMap(),"lSProd");


                    deleteRequest.id(hit.getId());
                    deleteRequest.index(listType);
                    restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);


                }


                return retResult.ok(CodeEnum.OK.getCode(),null);
            }

            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.LIST_OBJECT_NO_HAVE.getCode(), null);

    }



}
