//package com.cresign.action.service.impl;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.cresign.tools.dbTools.Qt;
//import com.cresign.tools.pojo.es.lNUser;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
// * @author tang
// * @Description 作者很懒什么也没写
// * @ClassName EsRImpl
// * @Date 2023/10/9
// * @ver 1.0.0
// */
//@Service
//public class EsRImpl{
//
//    @Autowired
//    private EsTangRep esTangRep;
//
//    @Autowired
//    private Qt qt;
//
//    public void addAllES(List<lNUser> list){
//        esTangRep.saveAll(list);
//    }
//
//    public void getES(String index, JSONArray filterArray, Integer page, Integer size){
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//
//        qt.filterBuilder(filterArray, queryBuilder);
//        sourceBuilder.query(queryBuilder).from((page - 1) * size).size(size);
//        SearchRequest request = new SearchRequest();
//        String[] indices = index.split("/");
//        request.indices(indices);
//        request.source(sourceBuilder);
//        Iterable<lNUser> search = esTangRep.search(queryBuilder);
//        System.out.println(JSON.toJSONString(search));
//    }
//}
