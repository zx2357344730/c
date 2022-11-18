package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.common.ActionEnum;
import com.cresign.action.service.UsageService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.es.lBProd;
import com.cresign.tools.pojo.es.lSBComp;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Comp;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.pojo.po.compCard.CompInfo;
import com.cresign.tools.pojo.po.prodCard.ProdInfo;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author kevin
 * @ClassName UsageServiceImpl
 * @Description
 * @updated 2022/11/16 10:48 PM
 * @return
 * @ver 1.0.0
 **/
public class UsageServiceImpl implements UsageService {

    @Autowired
    private StringRedisTemplate redisTemplate0;


    @Autowired
    private Qt qt;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private RetResult retResult;


    @Override
    public ApiResponse setFav(String id_U, String id_C, String id_O, Integer index, String id, String id_FS) {
        JSONObject jsonFav = qt.setJson("id_C", id_C, "id_O", id_O, "index", index, "id", id, "id_FS", id_FS);
        JSONObject jsonUpdate = qt.setJson("fav.objFav", jsonFav);
        qt.pushMDContent(id_U, jsonUpdate, User.class);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse setRecentTask(String id_U, String id_C, String id_O, Integer index, String id, String id_FS) {
        JSONObject jsonFav = qt.setJson("id_C", id_C, "id_O", id_O, "index", index, "id", id, "id_FS", id_FS);
        JSONObject jsonUpdate = qt.setJson("fav.objRecent", jsonFav);
        qt.pushMDContent(id_U, jsonUpdate, User.class);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    // if Recent > 10, discard thsoe old ones
    @Override
    public ApiResponse getFav(String id_U, String id_C) {
        User userData = qt.getMDContent(id_U, "fav", User.class);
        Integer recentListSize = userData.getFav().getJSONArray("objRecent").size();
        if (recentListSize > 10)
        {
            for (int i = 0; i < recentListSize - 10; i++)
            {
                userData.getFav().getJSONArray("objRecent").remove(0);
            }
            qt.setMDContent(id_U, qt.setJson("fav.objRecent", userData.getFav().getJSONArray("objRecent")), User.class);
        }
        return retResult.ok(CodeEnum.OK.getCode(), userData.getFav());
    }

    @Override
    public ApiResponse setFavAppoint(JSONArray arrayId_U, String id_C, String id_O, Integer index, String id, String id_FS) {
        JSONObject jsonFav = qt.setJson("id_C", id_C, "id_O", id_O, "index", index, "id", id, "id_FS", id_FS);
        for (int i = 0; i < arrayId_U.size(); i++) {
            String id_U = arrayId_U.getString(i);
            JSONObject jsonUpdate = qt.setJson("fav.objTask", jsonFav);
            qt.pushMDContent(id_U, jsonUpdate, User.class);
        }
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


    @Override
    public ApiResponse setRefAuto(String id_C, String type, JSONObject jsonRefAuto) {
//        String id_A = dbUtils.getId_A(id_C, "a-core");
//        Update update = new Update();
//        update.set("refAuto." + type, jsonRefAuto);
//        UpdateResult updateResult = dbUtils.updateMongoValues(id_A, update, Asset.class);
//        if (updateResult.getModifiedCount() == 0) {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.COOKIEX_ERROR.getCode(), null);
//        }
        String id_A = qt.getId_A(id_C, "a-core");
        qt.setMDContent(id_A, qt.setJson("refAuto." + type, jsonRefAuto), Asset.class);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse getRefAuto(String id_C, String type) {

        Asset asset = qt.getConfig(id_C, "a-core", "refAuto." + type);
        JSONObject jsonRefAuto = asset.getRefAuto().getJSONObject(type);
        if (jsonRefAuto == null) {
            return retResult.ok(CodeEnum.OK.getCode(), new JSONObject());
        }
        System.out.println("jsonRefAuto=" + jsonRefAuto);
        return retResult.ok(CodeEnum.OK.getCode(), jsonRefAuto);
    }

    @Override
    public ApiResponse setCookiex(String id_U, String id_C, String type, JSONArray arrayCookiex) {
//        Update update = new Update();
//        update.set("cookiex." + id_C + "." + type, arrayCookiex);
//        UpdateResult updateResult = dbUtils.updateMongoValues(id_U, update, User.class);
//        if (updateResult.getModifiedCount() == 0) {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.COOKIEX_ERROR.getCode(), null);
//        }
        qt.setMDContent(id_U, qt.setJson("cookiex." + id_C + "." + type, arrayCookiex), User.class);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse getCookiex(String id_U, String id_C, String type) {
        User user = qt.getMDContent(id_U, "cookiex." + id_C + "." + type, User.class);
        JSONArray arrayCookiex = user.getCookiex().getJSONObject(id_C).getJSONArray(type);
        if (arrayCookiex == null) {
            return retResult.ok(CodeEnum.OK.getCode(), new JSONArray());
        }
        return retResult.ok(CodeEnum.OK.getCode(), arrayCookiex);
    }

    @Override
    public ApiResponse getNacosStatus() {
        JSONArray arrayService = new JSONArray();
        List<Object> serviceNames = Arrays.asList("DEFAULT_GROUP@@api-gateway", "DEFAULT_GROUP@@cresign-login",
                "DEFAULT_GROUP@@cresign-details", "DEFAULT_GROUP@@cresign-timer", "DEFAULT_GROUP@@cresign-action","DEFAULT_GROUP@@cresign-file",
                "DEFAULT_GROUP@@cresign-search", "DEFAULT_GROUP@@cresign-chat", "DEFAULT_GROUP@@cresign-purchase",
                "DEFAULT_GROUP@@cresign-testCode", "DEFAULT_GROUP@@cresign-listener");
        List<Object> nacosListener = redisTemplate0.opsForHash().multiGet("nacosListener", serviceNames);
        for (Object ip : nacosListener) {
            JSONArray arrayIp = JSON.parseArray(ip.toString());
            arrayService.add(arrayIp);
        }
        return retResult.ok(CodeEnum.OK.getCode(), arrayService);
    }

    @Override
    public ApiResponse connectionComp(String id_C, String id_CB, Boolean isCB) throws IOException {

        HashSet setId_C = new HashSet();
        setId_C.add(id_C);
        setId_C.add(id_CB);
        Map<String, Comp> mapComp = (Map<String, Comp>) dbUtils.getMongoMapField(setId_C, "info", Comp.class);

        String id_CS = null;
        if (isCB) {
            id_CS = id_C;
        } else {
            id_CS = id_CB;
            id_CB = id_C;
        }
        Comp comp = mapComp.get(id_CS);
        Comp compB = mapComp.get(id_CB);
        //假公司不能连接
        if (comp.getBcdNet() == 0 || compB.getBcdNet() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), null);
        }
        CompInfo compInfo = comp.getInfo();
        CompInfo compInfoB = compB.getInfo();

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("id_C", id_CS))
                .must(QueryBuilders.termQuery("id_CB", id_CB));
        SearchResponse searchResponse = dbUtils.getEsQuery(queryBuilder, "lSBComp");
        //公司已连接
        if (searchResponse.getHits().getHits().length > 0) {
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), null);
        }

        lSBComp lsbcomp = new lSBComp(id_CS, compInfo.getId_CP(), id_CB, compInfoB.getId_CP(), compInfo.getWrdN(),
                compInfo.getWrddesc(), compInfoB.getWrdN(), compInfoB.getWrddesc(), "1000", "1000",
                compInfo.getRef(), compInfoB.getRef(), compInfo.getPic(), compInfoB.getPic());
        qt.addES("lsbcomp", lsbcomp);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse connectionProd(String id_C, String id_P) throws IOException {
        Prod prod = (Prod) dbUtils.getMongoOneField(id_P, "info", Prod.class);
        ProdInfo prodInfo = prod.getInfo();
        String id_CB = prodInfo.getId_C();
        Comp comp = (Comp) dbUtils.getMongoOneField(id_CB, "bcdNet", Comp.class);
        //假公司不能连接
        if (comp.getBcdNet() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), null);
        }

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.termQuery("id_P", id_P))
                .must(QueryBuilders.termQuery("id_C", id_CB))
                .must(QueryBuilders.termQuery("id_CB", id_C));
        SearchResponse searchResponse = dbUtils.getEsQuery(queryBuilder, "lBProd");
        //产品已连接
        if (searchResponse.getHits().getHits().length > 0) {
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), null);
        }

        lBProd lbprod = new lBProd(id_P, id_CB, prodInfo.getId_CP(), id_C, prodInfo.getWrdN(),
                prodInfo.getWrddesc(),"1000","1000","","", prodInfo.getPic(), prodInfo.getLDC(),prodInfo.getLUT());
        qt.addES("lBProd", lbprod);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

}
