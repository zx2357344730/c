//package com.cresign.tools.elastic;
//
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//
//import java.io.IOException;
//import java.util.Map;
//
//
//public class esListOperate {
//
////    @Autowired
////    private RestHighLevelClient restHighLevelClient;
//
//    /**
//     * 新增列表
//     *
//     * @param infoObject 列表数据
//     * @param indexes    列表索引
//     * @author Jevon
//     * @ver 1.0
//     * @createDate: 2021/6/25 17:16
//     * @return: void
//     */
////    public static void addES(JSONObject infoObject, String indexes,RestHighLevelClient restHighLevelClient) throws IOException {
////
////        infoObject.put("tmk", DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
////        //指定ES索引
////        IndexRequest request = new IndexRequest(indexes);
////        //ES列表
////        request.source(infoObject, XContentType.JSON);
////
////        restHighLevelClient.index(request, RequestOptions.DEFAULT);
////
////    }
//
//    /**
//     * 查找列表某一条数据
//     * @author Jevon
//     * @param indexes   索引
//     * @param name      字段名（比如id_A）
//     * @param text      查询文本(a-123)
//     * @param name1     字段名1(id_C)
//     * @param text1     查询文本2(c-123)
//     * @ver 1.0
//     * @createDate: 2021/6/25 17:22
//     * @return: java.util.Map<java.lang.String, java.lang.Object>
//     */
//    public static Map<String, Object> queryList(String indexes, String name, Object text, String name1, Object text1,RestHighLevelClient restHighLevelClient) throws IOException {
//
//        SearchRequest searchRequest = new SearchRequest(indexes);
//
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//
//        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                //条件1：
//                .must(QueryBuilders.matchPhraseQuery(name, text))
//                //条件2
//                .must(QueryBuilders.matchPhraseQuery(name1, text1));
//        sourceBuilder.query(queryBuilder);
//        searchRequest.source(sourceBuilder);
//
//        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//
//        SearchHit hit = search.getHits().getHits()[0];
//        if (search.getHits().getHits().length > 0) {
//            return hit.getSourceAsMap();
//        }
//
//        return null;
//
//    }
//
//    /**
//     * 根据id_U和id_C去ES查询得到用户的grpU
//     * @author Jevon
//     * @param id_CB
//     * @param id_U
//     * @param restHighLevelClient
//     * @ver 1.0
//     * @createDate: 2021/7/22 8:58
//     * @return: java.lang.String
//     */
//    public static String queryGrpU(String id_CB,  String id_U, RestHighLevelClient restHighLevelClient) throws IOException {
//
//        SearchRequest searchRequest = new SearchRequest("lBUser");
//
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//
//        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                //条件1：
//                .must(QueryBuilders.termQuery("id_CB", id_CB))
//                //条件2
//                .must(QueryBuilders.termQuery("id_U", id_U));
//        sourceBuilder.query(queryBuilder);
//        searchRequest.source(sourceBuilder);
//
//        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//
//        SearchHit hit = search.getHits().getHits()[0];
//        if (search.getHits().getHits().length > 0) {
//            return hit.getSourceAsMap().get("grpU").toString();
//        }else{
//            return "1099";
//        }
//
//
//    }
//
//
//
//}
