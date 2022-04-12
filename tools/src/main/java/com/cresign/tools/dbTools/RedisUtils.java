package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Comp;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * redis工具类
 * @Author Rachel
 * @Data 2021/09/15
 **/
@Component
public class RedisUtils {

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 根据id_C和ref获取id_A
     * @Author Rachel
     * @Date 2022/01/14
     * ##param id_C 公司id
     * ##param ref 编号
     * @Return java.lang.String
     * @Card
     **/
    public String getId_A(String id_C, String ref) {
        Boolean bool = redisTemplate0.opsForHash().hasKey("login:module_id:compId-" + id_C, ref);
        System.out.println("bool=" + bool);
        if (bool) {
            String id_A = (String) redisTemplate0.opsForHash().get("login:module_id:compId-" + id_C, ref);
            System.out.println("id_A=" + id_A);
            return id_A;
        } else {
            Query queryAsset = new Query(new Criteria("info.id_C").is(id_C).and("info.ref").is(ref));
            queryAsset.fields().include("id");
            Asset asset = mongoTemplate.findOne(queryAsset, Asset.class);
            System.out.println("what"+id_C+ref);
            if (asset == null) {
//                throw new ErrorResponseException(HttpStatus.FORBIDDEN, ToolEnum.ASSET_NOT_FOUND.getCode(), null);
                return "none";
            }
            redisTemplate0.opsForHash().put("login:module_id:compId-" + id_C, ref, asset.getId());
            System.out.println("id_A=" + asset.getId());
            return asset.getId();
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

    /**
     * 查询公司是真是假  1：真公司    0：假公司，2：都是自己
     * ##author: Jevon
     * ##Params: id_C      自己
     * ##Params: compOther     对方
     * ##version: 1.0
     * ##updated: 2021/1/12 9:33
     * ##Return: int
     */
    public int judgeComp(String id_C,String compOther){

        if (id_C.equals(compOther)){
            return 2;
        }else{
            Query compQ = new Query(
                    new Criteria("_id").is(compOther).and("bcdNet").is(1));
            compQ.fields().include("bcdNet");
            Comp comp = mongoTemplate.findOne(compQ, Comp.class);
            if (comp != null) {
                return 1;
            }else {
                return 0;
            }
        }
    }

    /**
     * 新增assetflow日志
     * ##author: Jevon
     * ##Params: infoObject
     * ##version: 1.0
     * ##updated: 2020/10/26 8:30
     * ##Return: void
     */
    public void addES(JSONObject infoObject , String indexes ) throws IOException {

        infoObject.put("tmk", DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        //指定ES索引
        IndexRequest request = new IndexRequest(indexes);
        //ES列表
        request.source(infoObject, XContentType.JSON);

        restHighLevelClient.index(request, RequestOptions.DEFAULT);


    }

}
