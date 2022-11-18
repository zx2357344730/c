//package com.cresign.action.service.impl;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.action.common.ActionEnum;
//import com.cresign.action.service.KfActionService;
//import com.cresign.tools.advice.RetResult;
//import com.cresign.tools.apires.ApiResponse;
//import com.cresign.tools.common.Constants;
//import com.cresign.tools.dbTools.CoupaUtil;
//import com.cresign.tools.dbTools.DateUtils;
//import com.cresign.tools.dbTools.DbUtils;
//import com.cresign.tools.dbTools.Ut;
//import com.cresign.tools.enumeration.CodeEnum;
//import com.cresign.tools.enumeration.DateEnum;
//import com.cresign.tools.exception.ErrorResponseException;
//import com.cresign.tools.pojo.po.Asset;
//import com.cresign.tools.pojo.po.LogFlow;
//import com.cresign.tools.pojo.po.Order;
//import com.cresign.tools.pojo.po.User;
//import org.apache.lucene.search.TotalHits;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.support.WriteRequest;
//import org.elasticsearch.action.update.UpdateRequest;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.indices.GetIndexRequest;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.TermQueryBuilder;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.*;
//
////这个文件还没开始修改
//
//
///**
// * @ClassName KfAction
// * @Description 作者很懒什么也没写
// * @authortang
// * @Date 2021/5/25 17:18
// * @ver 1.0.0
// */
//@Service
//public class KfActionServiceImpl implements KfActionService {
//
//    @Autowired
//    private CoupaUtil coupaUtil;
//
//    @Autowired
//    private Ut ut;
//
//    @Autowired
//    private DateUtils dateUtils;
//
//    @Autowired
//    private DbUtils dbUtils;
//
//    @Autowired
//    private RestHighLevelClient client;
//
//    @Autowired
//    private RetResult retResult;
//
//
//
//    /**
//     * 恢复客服信息,Es数据库
//     * @param oId	订单编号
//     * @param type	类型
//     * @param id_U	用户编号
//     * @param kf	客服信息
//     * @param indexOnly	唯一标识
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * @date 2021/6/16 14:59
//     */
//    @Override
//    public ApiResponse getRecoveryKf(String oId, String type, String id_U, String kf, Integer indexOnly) {
//
//
//        if (!"notice".equals(type)&&"false".equals(kf)) {
//            Order errOrder = coupaUtil.getOrderByKeyAndListKey(
//                    "info.cusmsgId",oId, Collections.singletonList(Constants.GET_CUSMSG));
//            if (null == errOrder) {
//                // 返回操作失败结果
////                return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_GET_DATA_NULL.getCode(),"获取订单数据为空");
//
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_GET_ORDER_NULL.getCode(), "获取订单数据为空");
//            }
//            JSONObject cusmsg = errOrder.getCusmsg();
//            if (null == cusmsg) {
//                // 返回操作失败结果
////                return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_GET_DATA_NULL.getCode(),"获取订单数据为空");
//
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_GET_ORDER_NULL.getCode(), "获取订单数据为空");
//            }
//            JSONObject cusUser = cusmsg.getJSONObject(Constants.GET_OBJ_CUS_USER);
//            indexOnly = cusUser.getInteger(id_U);
//        }
//
//        // 创建返回结果存储map
//        JSONObject objectMap = new JSONObject();
//
//        // 添加返回数据
//        objectMap.put("objData",this.getCusmsgLogListByEs(oId, type,indexOnly));
////        return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_SUCCESS.getCode()
////                ,objectMap);
//
//        return retResult.ok(CodeEnum.OK.getCode(), objectMap);
//    }
//
//
//
//    /**
//     * 获取oId获取Behavior数据库的数据的Log信息组成列表
//     * @param oId    订单编号
//     * @return 返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * @date 2020/9/1 20:42
//     */
//    private List<LogFlow> getCusmsgLogListByEs(String oId, String type
//            , Integer indexOnly){
//        int count = this.getCusmsgLogByCountAndEs(oId, type,indexOnly);
//        if (count == 0) {
//            return null;
//        } else if (count == -2){
//            return null;
//        } else {
//            SearchRequest request = new SearchRequest("cusmsg");
//
//            // 构建搜索条件
//            SearchSourceBuilder builder = new SearchSourceBuilder();
//
//            // 分页
//            builder.from(0);
//            builder.size(count);
//
//            TermQueryBuilder queryBuilder1 = QueryBuilders.termQuery("id.keyword", oId);
//            QueryBuilder queryBuilder;
//            if ("msg".equals(type)) {
//                TermQueryBuilder queryBuilder2 = QueryBuilders.termQuery("data.type.keyword", type);
//                TermQueryBuilder queryBuilder3 = QueryBuilders.termQuery("data.indexOnly", indexOnly == null?-1:indexOnly);
//                queryBuilder = QueryBuilders.boolQuery()
//                        .must(queryBuilder1)
//                        .must(queryBuilder2)
//                        .must(queryBuilder3);
//            } else if ("notice".equals(type)) {
//                TermQueryBuilder queryBuilder2 = QueryBuilders.termQuery("data.type.keyword", type);
//                queryBuilder = QueryBuilders.boolQuery()
//                        .must(queryBuilder1)
//                        .must(queryBuilder2);
//            } else {
//                TermQueryBuilder queryBuilder3 = QueryBuilders.termQuery("data.indexOnly", indexOnly == null?-1:indexOnly);
//                queryBuilder = QueryBuilders.boolQuery()
//                        .must(queryBuilder1)
//                        .must(queryBuilder3);
//            }
//            builder.query(queryBuilder);
//
//            request.source(builder);
//
//            try {
//                SearchResponse search = client.search(request, RequestOptions.DEFAULT);
//                List<LogFlow> result = new ArrayList<>();
//                for (SearchHit hit : search.getHits().getHits()) {
//                    // 获取log并添加到结果集合中
//                    result.add(JSONObject.parseObject(JSON.toJSONString(hit.getSourceAsMap()),LogFlow.class));
//                }
//                return getListLog1ByGroupAndSort(result);
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//    }
//
//
//    /**
//     * 根据公司id获取该公司的客服聊天室id
//     * @param id_C	公司编号
//     * @param id_U	用户编号
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * @date 2021/6/16 14:58
//     */
//    @Override
//    public ApiResponse getCompByCompIdAndWsIdAndKf(String id_C,String id_U) {
//
//        User user = coupaUtil.getUserByKeyAndVal("id_U", id_U, Arrays.asList("info"));
//
//        JSONObject userInfo = Ut.jsonTo(user.getInfo(), JSONObject.class);
//        if (null != userInfo) {
//            JSONObject assetByKf;
//            if (userInfo.get("cusList") != null) {
//                JSONObject cusList = userInfo.getJSONObject("cusList");
//                if (cusList.get(id_C) != null) {
//                    JSONObject objectMap = cusList.getJSONObject(id_C);
//                    String id_O = objectMap.get(Constants.GET_ID_O).toString();
//                    assetByKf = coupaUtil.getAssetByKf(id_C,id_O);
//                    if (null == assetByKf) {
////                        return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_FAILF.getCode()
////                                ,"该公司没有客服!");
//
//                        throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), "该公司没有客服");
//                    }
//                    // 抛出操作成功异常
////                    return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_SUCCESS.getCode(),assetByKf);
//                    return retResult.ok(CodeEnum.OK.getCode(), assetByKf);
//                }
//            }
//            assetByKf = coupaUtil.getAssetByKf(id_C,null);
//            if (null == assetByKf) {
////                return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_FAILF.getCode()
////                        ,"该公司没有客服!");
//
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), "该公司没有客服");
//            }
//            // 抛出操作成功异常
////            return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_SUCCESS.getCode(),assetByKf);
//
//            return retResult.ok(CodeEnum.OK.getCode(), assetByKf);
//        }
////        return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_FAILF.getCode()
////                ,"您的个人信息有误!");
//
//        throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_INCORRECT_PERSONAL_INFORMATION.getCode(), "您的个人信息有误");
//    }
//
//
//
//    /**
//     * 根据is获取条件，对result进行分组并且排序
//     * @param result	日志集合
//     * @return 返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * @date 2020/9/8 13:49
//     */
//    private List<LogFlow> getListLog1ByGroupAndSort(List<LogFlow> result){
//        if (null == result || result.size() == 0) {
//            return null;
//        }
//        List<LogFlow> resultZon = new ArrayList<>();
//        Map<Integer,List<LogFlow>> map = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
//        result.forEach(log1 -> {
//            Integer indexOnly = ut.objToInteger(log1.getData().get("indexOnly"));
//            JSONObject data = log1.getData();
//            data.put("isOk",2);
//            log1.setData(data);
//            if (map.get(indexOnly) != null) {
//                map.get(indexOnly).add(log1);
//            } else {
//                List<LogFlow> list = new ArrayList<>();
//                list.add(log1);
//                map.put(indexOnly,list);
//            }
//        });
//        map.keySet().forEach(k -> {
//            List<LogFlow> list = map.get(k);
//            ut.sortIs(2,list, DateEnum.DATE_TIME_FULL.getDate());
//            resultZon.addAll(list);
//        });
//        ut.sortIs(2,resultZon,DateEnum.DATE_TIME_FULL.getDate());
//        return resultZon;
//    }
//
//    /**
//     * 根据oId获取Behavior索引库总的大小
//     * @param oId    提问编号
//     * @return int  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * @date 2020/9/1 20:35
//     */
//    private int getCusmsgLogByCountAndEs(String oId, String type, Integer indexOnly){
//        GetIndexRequest requestG = new GetIndexRequest("cusmsg");
//        boolean exists;
//        try {
//            exists = client.indices().exists(requestG, RequestOptions.DEFAULT);
//            if (exists) {
//                SearchRequest request = new SearchRequest("cusmsg");
//
//                // 构建搜索条件
//                SearchSourceBuilder builder = new SearchSourceBuilder();
//
//                // 分页
//                builder.from(0);
//                builder.size(1);
//
//                // 查询条件，我们可以使用 QueryBuilders 工具来实现
////                    TermQueryBuilder queryBuilder1 = QueryBuilders.termQuery("data.id_O.keyword", oId);
//                TermQueryBuilder queryBuilder1 = QueryBuilders.termQuery("id.keyword", oId);
//                QueryBuilder queryBuilder;
//                if ("msg".equals(type)) {
//                    TermQueryBuilder queryBuilder2 = QueryBuilders.termQuery("data.type.keyword", type);
//                    TermQueryBuilder queryBuilder3 = QueryBuilders.termQuery("data.indexOnly", indexOnly == null?-1:indexOnly);
//                    queryBuilder = QueryBuilders.boolQuery()
//                            .must(queryBuilder1)
//                            .must(queryBuilder2)
//                            .must(queryBuilder3);
//                } else if ("notice".equals(type)) {
//                    TermQueryBuilder queryBuilder2 = QueryBuilders.termQuery("data.type.keyword", type);
//                    queryBuilder = QueryBuilders.boolQuery()
//                            .must(queryBuilder1)
//                            .must(queryBuilder2);
//                } else {
//                    TermQueryBuilder queryBuilder3 = QueryBuilders.termQuery("data.indexOnly", indexOnly == null?-1:indexOnly);
//                    queryBuilder = QueryBuilders.boolQuery()
//                            .must(queryBuilder1)
//                            .must(queryBuilder3);
//                }
//                builder.query(queryBuilder);
//                request.source(builder);
//
//                SearchResponse search = client.search(request, RequestOptions.DEFAULT);
//                TotalHits totalHits = search.getHits().getTotalHits();
//                return (int) totalHits.value;
//            } else {
//                return 0;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return -2;
//        }
//    }
//
//    /**
//     * 恢复客服信息,并且携带用户基础信息,Es数据库
//     * @param oId	订单编号
//     * @param type	类型
//     * @param indexOnly	唯一标识
//     * @param id_U	用户编号
//     * @param kf	客服信息
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * @date 2021/6/16 15:01
//     */
//    @Override
//    public ApiResponse getRecoveryKfAndUserInfo(String oId,String type,Integer indexOnly,String id_U,String kf) {
//
//
//        if (!"notice".equals(type)&&"false".equals(kf)) {
//            Order errOrder = coupaUtil.getOrderByKeyAndListKey(
//                    "info.cusmsgId",oId, Collections.singletonList(Constants.GET_CUSMSG));
//            if (null == errOrder) {
//                // 返回操作失败结果
//
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_GET_ORDER_NULL.getCode(), "获取订单数据为空");
//            }
//            JSONObject cusmsg = errOrder.getCusmsg();
//            if (null == cusmsg) {
//                // 返回操作失败结果
////                return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_GET_DATA_NULL.getCode(),"获取订单数据为空");
//
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_GET_ORDER_NULL.getCode(), "获取订单数据为空");
//            }
//            JSONObject cusUser = cusmsg.getJSONObject(Constants.GET_OBJ_CUS_USER);
//            indexOnly = cusUser.getInteger(id_U);
//        }
//        // 调用方法将日志集合放入，获取日志内所有用户的信息
////        return logService.getLogByTypeAndUserInfoF(
////                this.getCusmsgLogListByEs(oId, type,indexOnly));
//        return null;
//    }
//
//    /**
//     * 用户评分
//     * @param id_U	用户编号
//     * @param id_C	公司编号
//     * @param id_O	订单编号
//     * @param uuId	唯一id
//     * @param score	分数
//     * @return java.lang.String  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * @date 2021/6/16 15:04
//     */
//    @Override
//    public ApiResponse getScoreUser(String id_U,String id_C,String id_O,String uuId,Integer score) {
//
//        String assetId = dbUtils.getId_A(id_C, "a-auth");
//
//        Asset one = coupaUtil.getAssetById(assetId, Collections.singletonList("flowControl"));
//        if (null == one) {
//            // 返回操作失败结果
////            return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_GET_DATA_NULL.getCode(),"获取数据为空");
//
//            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ActionEnum.ERR_GET_DATA_NULL.getCode(), "获取数据为空");
//        }
//        JSONObject flowControl = one.getFlowControl();
//        JSONArray objData = flowControl.getJSONArray("objData");
//        for (int i = 0; i < objData.size(); i++) {
//            JSONObject obj = objData.getJSONObject(i);
////            if (obj.getString("type").equals("cusmsg")&&obj.getString(Constants.GET_ID).equals(id_O)){
//            if (obj.getString(Constants.GET_ID).equals(id_O)){
//                JSONObject re = obj.getJSONObject("objUser2");
////                System.out.println("到这里输出:");
////                System.out.println(id_U);
////                System.out.println(re.getJSONObject(id_U));
//                if (re == null) {
//                    re = new JSONObject();
//                }
//                JSONObject user;
//                if (re.getJSONObject(id_U) == null) {
//                    user = new JSONObject();
//                    user.put("1",0);
//                    user.put("2",0);
//                    user.put("3",0);
//                    user.put("4",0);
//                    user.put("5",0);
//                    re.put(id_U,user);
//                } else {
//                    user = re.getJSONObject(id_U);
//                }
//                Integer integer = user.getInteger(score.toString());
//                integer++;
//                user.put(score.toString(),integer);
//                JSONObject mapKey = new JSONObject();
//                mapKey.put("flowControl.objData."+i+".objUser2."+id_U,user);
//                coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
//                try {
//                    SearchRequest request = new SearchRequest("cusmsg");
//
//                    // 构建搜索条件
//                    SearchSourceBuilder builder = new SearchSourceBuilder();
//
//                    TermQueryBuilder queryBuilder1 = QueryBuilders.termQuery("data.uuId.keyword", uuId);
//                    QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(queryBuilder1);
//                    builder.query(queryBuilder);
//
//                    request.source(builder);
//
//                    String esId = null;
//                    SearchResponse search = client.search(request, RequestOptions.DEFAULT);
//                    LogFlow LogFlow = null;
//                    for (SearchHit hit : search.getHits().getHits()) {
//                        esId = hit.getId();
//                        LogFlow = JSONObject.parseObject(JSON.toJSONString(hit.getSourceAsMap()),LogFlow.class);
//                        break;
//                    }
//                    if (null == esId || null == LogFlow) {
//                        // 返回操作失败结果
////                        return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_GET_DATA_NULL.getCode(),"es获取数据为空");
//
//                        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ActionEnum.ERR_ES_GET_DATA_IS_NULL.getCode(), "es获取数据为空");
//                    }
//
//                    UpdateRequest updateRequest = new UpdateRequest("cusmsg",esId);
//                    JSONObject u = new JSONObject();
//                    JSONObject data = new JSONObject();
//                    data.put("score",score);
//                    u.put("data",data);
//                    updateRequest.doc(u);
//                    //写入完成立即刷新
//                    updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
//                    client.update(updateRequest,RequestOptions.DEFAULT);
//
//                    LogFlow.setTmd(dateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//                    JSONObject data1 = LogFlow.getData();
//                    data1.put("score",score);
//                    data1.put("bcdStatus", Constants.INT_SIX);
//                    LogFlow.setData(data1);
//                    this.sendLog(LogFlow);
////                    return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_SUCCESS.getCode(),score);
//
//                    return retResult.ok(CodeEnum.OK.getCode(), score);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        // 返回操作失败结果
////        return RetResult.jsonResultEncrypt(HttpStatus.OK, LogEnum.LOG_GET_DATA_NULL.getCode(),"出现异常");
//
//        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, ActionEnum.ERR_AN_ERROR_OCCURRED.getCode(), "出现错误");
//    }
//
//
//    /**
//     * 发送日志方法
//     * @param logL	需要发送的日志
//     * @return void  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * @date 2020/8/6 9:26
//     */
//    public void sendLog(LogFlow logL){
//        JSONObject data = logL.getData();
//        data.put("isOK",0);
//        logL.setData(data);
//
//        // 发送日志
//    }
//
//
//}
