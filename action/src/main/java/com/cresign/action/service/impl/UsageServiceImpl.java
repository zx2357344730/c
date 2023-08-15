package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.UsageService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author kevin
 * @ClassName UsageServiceImpl
 * @Description
 * @updated 2022/11/16 10:48 PM
 * @return
 * @ver 1.0.0
 **/
@Service
public class UsageServiceImpl implements UsageService {

    // Fav, Cookiex, refAuto, powerUp, nacos

//    @Autowired
//    private StringRedisTemplate redisTemplate0;


    @Autowired
    private Qt qt;

    @Autowired
    private Ws ws;


    @Autowired
    private RetResult retResult;



    //    @Override
//    public ApiResponse setFav(String id_U, String id_C, String id_O, Integer index, String id, String id_FS,
//                              JSONObject wrdN, String pic, Integer type) {
//        JSONObject jsonFav = qt.setJson("id_C", id_C, "id_O", id_O, "index", index, "id", id, "id_FS", id_FS,
//                "wrdN", wrdN, "pic", pic, "type", type);
//        qt.pushMDContent(id_U, "fav.objFav", jsonFav, User.class);
//        qt.pushMDContent(id_O, "action.arrUA", id_U, Order.class);
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
    @Override
    public ApiResponse setFav(String id_U, String id_C, JSONObject content) {
        qt.pushMDContent(id_U, "fav.objFav", content, User.class);
        String id_O = content.getString("id_O");
        Integer index = content.getInteger("index");
        qt.pushMDContent(id_O, "action.objAction." + index + ".arrUA", id_U, Order.class);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse setFavInfo(String id_U, String id_C, String id, String listType, String grp, String pic, JSONObject wrdN) {
        JSONObject content = qt.setJson("id", id, "id_C", id_C, "listType", listType,
                "grp", grp, "pic", pic, "wrdN", wrdN);
        qt.pushMDContent(id_U, "fav.objInfo", content, User.class);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


    @Override
    public ApiResponse getFav(String id_U) {
        User user = qt.getMDContent(id_U, "fav", User.class);
        //All new user must have fav and cookiex card, but it may not exists for old users
        if (user.getFav() == null)
        {
            JSONObject initFav = qt.setJson("objFav", new JSONArray(), "objInfo", new JSONArray());
            qt.setMDContent(id_U, qt.setJson("fav", initFav), User.class);
            user.setFav(initFav);
        }
        if (user.getFav().getJSONArray("objInfo") == null)
        {
            JSONObject initFav = qt.setJson("objInfo", new JSONArray());
            qt.setMDContent(id_U, qt.setJson("fav.objInfo", initFav), User.class);
            user.getFav().put("objInfo", initFav);
        }
        if (user.getFav().getJSONArray("objFav") == null)
        {
            JSONObject initFav = qt.setJson("objFav", new JSONArray());
            qt.setMDContent(id_U, qt.setJson("fav.objFav", initFav), User.class);
            user.getFav().put("objFav", initFav);
        }
        return retResult.ok(CodeEnum.OK.getCode(), user.getFav());
    }

    @Override
    public ApiResponse delFav(String id_U, String id_O, Integer index, String id, String id_FS) {
        JSONObject jsonFav = qt.setJson("id_O", id_O, "index", index, "id", id, "id_FS", id_FS);
        qt.pullMDContent(id_U, "fav.objFav", jsonFav, User.class);
        try {
            qt.pullMDContent(id_O, "action.objAction." + index + ".arrUA", id_U, Order.class);
        } catch (Exception e) {
            return retResult.ok(CodeEnum.OK.getCode(), "任务单已删除");
        }
        return retResult.ok(CodeEnum.OK.getCode(), "");
    }

    @Override
    public ApiResponse delFavInfo(String id_U, String id) {
        JSONObject jsonFav = qt.setJson("id", id);
        qt.pullMDContent(id_U, "fav.objInfo", jsonFav, User.class);
        return retResult.ok(CodeEnum.OK.getCode(), "");
    }


    @Override
    public ApiResponse appointTask(JSONArray arrayId_U, String id_UManager, String id_C, JSONObject content) {
        content.put("id_UM", id_UManager);
        String id_O = content.getString("id_O");
        Integer index = content.getInteger("index");

        Order order = qt.getMDContent(id_O, "action.objAction." + index + ".arrUA", Order.class);
        JSONArray arrUA = order.getAction().getJSONArray("objAction").getJSONObject(index).getJSONArray("arrUA");
        JSONObject oItem = order.getOItem().getJSONArray("objItem").getJSONObject(index);
        if (arrUA == null)
        {
            arrUA = new JSONArray();
        }
        for (int i = 0; i < arrayId_U.size(); i++) {
            String id_U = arrayId_U.getString(i);
            qt.pushMDContent(id_U, "fav.objFav", content, User.class);
            if (!arrUA.contains(id_U)) {
                arrUA.add(id_U);
                LogFlow appointLog = new LogFlow("action", "","","appoint",id_U,"", oItem.getString("id_P"), oItem.getString("grpB"),
                        oItem.getString("grp"), "", id_O, index, id_C, "", "", "", "请处理该任务", 5, qt.setJson("cn", "请处理该任务"), null);
                appointLog.setData(qt.setJson("id_UM", id_UManager));
                ws.sendWS(appointLog);
            }
        }
        qt.setMDContent(id_O, qt.setJson("action.objAction." + index + ".arrUA", arrUA), Order.class);



        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


    @Override
    public ApiResponse setPowerup(String id_C, JSONObject capacity) {
        String id_A = qt.getId_A(id_C, "a-core");
        JSONObject jsonUpdate = qt.setJson("powerup", capacity);
        qt.setMDContent(id_A, jsonUpdate, Asset.class);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public ApiResponse getPowerup(String id_C, String ref) {
        Asset asset = qt.getConfig(id_C, "a-core", "powerup");
        JSONObject jsonPowerup = asset.getPowerup().getJSONObject("objSize").getJSONObject(ref);
        return retResult.ok(CodeEnum.OK.getCode(), jsonPowerup);
    }


    @Override
    public ApiResponse setRefAuto(String id_C, String type, JSONObject jsonRefAuto) {
//        String id_A = qt.getId_A(id_C, "a-core");
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

        //init cookie 3 cases
        JSONObject initCookie = new JSONObject();
        JSONObject objType = new JSONObject();
        objType.put(type, new JSONArray());
        initCookie.put(id_C, objType);


        if (user.getCookiex() == null)
        {
            qt.setMDContent(id_U, qt.setJson("cookiex", initCookie), User.class);
        } else if (user.getCookiex().getJSONObject(id_C) == null){
            qt.setMDContent(id_U, qt.setJson("cookiex."+ id_C, objType), User.class);
        } else if (user.getCookiex().getJSONObject(id_C).getJSONArray(type) == null)
        {
            qt.setMDContent(id_U, qt.setJson("cookiex."+ id_C + "." + type, new JSONArray()), User.class);
            return retResult.ok(CodeEnum.OK.getCode(), new JSONArray());
        }

        JSONArray arrayCookiex = user.getCookiex().getJSONObject(id_C).getJSONArray(type);

        return retResult.ok(CodeEnum.OK.getCode(), arrayCookiex);
    }

    @Override
    public ApiResponse getNacosStatus() {
        List<Object> serviceNames = Arrays.asList("DEFAULT_GROUP@@api-gateway", "DEFAULT_GROUP@@cresign-login",
                "DEFAULT_GROUP@@cresign-details", "DEFAULT_GROUP@@cresign-timer", "DEFAULT_GROUP@@cresign-action","DEFAULT_GROUP@@cresign-file",
                "DEFAULT_GROUP@@cresign-search", "DEFAULT_GROUP@@cresign-chat", "DEFAULT_GROUP@@cresign-purchase",
                "DEFAULT_GROUP@@cresign-testCode", "DEFAULT_GROUP@@cresign-listener");

        return retResult.ok(CodeEnum.OK.getCode(), qt.getRDHashMulti("nacosListener", serviceNames));
    }

    @Override
    public ApiResponse notify(String id_U, String id_C, JSONObject wrdNU, String id, JSONObject wrdN, JSONObject wrddesc) {
        Asset asset = qt.getConfig(id_C, "a-auth", "flowControl");
        JSONArray arrayFlow = asset.getFlowControl().getJSONArray("objData");
        JSONObject jsonNotify = qt.setJson("id_U", id_U,
                "wrdNU", wrdNU,
                "wrdN", wrdN,
                "wrddesc", wrddesc,
                "tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        for (int i = 0; i < arrayFlow.size(); i++) {
            JSONObject jsonFlow = arrayFlow.getJSONObject(i);
            if (jsonFlow.getString("id").equals(id)) {
                JSONArray arrayNotify = jsonFlow.getJSONArray("notify");
                if (arrayNotify != null) {
                    if (arrayNotify.size() == 5) {
                        for (int j = 0; j < arrayNotify.size() - 1; j++) {
                            arrayNotify.set(j, arrayNotify.getJSONObject(j + 1));
                        }
                        arrayNotify.set(arrayNotify.size() - 1, jsonNotify);
                    } else {
                        arrayNotify.add(jsonNotify);
                    }
                } else {
                    arrayNotify = new JSONArray();
                    arrayNotify.add(jsonNotify);
                    jsonFlow.put("notify", arrayNotify);
                }
                JSONObject jsonUpdate = qt.setJson("flowControl.objData." + i, jsonFlow);
                qt.setMDContent(asset.getId(), jsonUpdate, Asset.class);
                break;
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

//    @Override
//    public ApiResponse connectionComp(String id_C, String id_CB, Boolean isCB) throws IOException {
//
//        HashSet setId_C = new HashSet();
//        setId_C.add(id_C);
//        setId_C.add(id_CB);
//        Map<String, Comp> mapComp = (Map<String, Comp>) dbUtils.getMongoMapField(setId_C, "info", Comp.class);
//
//        String id_CS = null;
//        if (isCB) {
//            id_CS = id_C;
//        } else {
//            id_CS = id_CB;
//            id_CB = id_C;
//        }
//        Comp comp = mapComp.get(id_CS);
//        Comp compB = mapComp.get(id_CB);
//        //假公司不能连接
//        if (comp.getBcdNet() == 0 || compB.getBcdNet() == 0) {
//            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), null);
//        }
//        CompInfo compInfo = comp.getInfo();
//        CompInfo compInfoB = compB.getInfo();
//
//        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//        queryBuilder.must(QueryBuilders.termQuery("id_C", id_CS))
//                .must(QueryBuilders.termQuery("id_CB", id_CB));
//        SearchResponse searchResponse = dbUtils.getEsQuery(queryBuilder, "lSBComp");
//        //公司已连接
//        if (searchResponse.getHits().getHits().length > 0) {
//            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), null);
//        }
//
//        lSBComp lsbcomp = new lSBComp(id_CS, compInfo.getId_CP(), id_CB, compInfoB.getId_CP(), compInfo.getWrdN(),
//                compInfo.getWrddesc(), compInfoB.getWrdN(), compInfoB.getWrddesc(), "1000", "1000",
//                compInfo.getRef(), compInfoB.getRef(), compInfo.getPic(), compInfoB.getPic());
//        qt.addES("lsbcomp", lsbcomp);
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
//    @Override
//    public ApiResponse connectionProd(String id_C, String id_P) throws IOException {
////        Prod prod = (Prod) dbUtils.getMongoOneField(id_P, "info", Prod.class);
//        Prod prod = qt.getMDContent(id_P, "info", Prod.class);
//        ProdInfo prodInfo = prod.getInfo();
//        String id_CB = prodInfo.getId_C();
//        Comp comp = (Comp) dbUtils.getMongoOneField(id_CB, "bcdNet", Comp.class);
//        Comp comp = qt.getMDContent()
//        //假公司不能连接
//        if (comp.getBcdNet() == 0) {
//            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), null);
//        }
//
//        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
//        queryBuilder.must(QueryBuilders.termQuery("id_P", id_P))
//                .must(QueryBuilders.termQuery("id_C", id_CB))
//                .must(QueryBuilders.termQuery("id_CB", id_C));
//        SearchResponse searchResponse = dbUtils.getEsQuery(queryBuilder, "lBProd");
//        //产品已连接
//        if (searchResponse.getHits().getHits().length > 0) {
//            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_COMP_NO_CUSTOMER_SERVICE.getCode(), null);
//        }
//
//        lBProd lbprod = new lBProd(id_P, id_CB, prodInfo.getId_CP(), id_C, prodInfo.getWrdN(),
//                prodInfo.getWrddesc(),"1000","1000","","", prodInfo.getPic(), prodInfo.getLDC(),prodInfo.getLUT());
//        qt.addES("lBProd", lbprod);
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }

}
