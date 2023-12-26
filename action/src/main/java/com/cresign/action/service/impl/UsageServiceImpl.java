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

    @Autowired
    private Qt qt;

    @Autowired
    private Ws ws;

    @Autowired
    private RetResult retResult;


    //TODO KEV:  save oItem into fav by: id_O, index to ALL arrUA, content is from FE, need to check and fix

    @Override
    public ApiResponse setFav(String id_U, String id_C, JSONObject content) {
        qt.pushMDContent(id_U, "fav.objFav", content, User.class);
        String id_O = content.getString("id_O");
        Integer index = content.getInteger("index");
        qt.pushMDContent(id_O, "action.objAction." + index + ".arrUA", id_U, Order.class);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


    // save id_X into fav by: id_C, id, listType, grp, + wrdN, pic
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
            // init if null
            JSONObject initFav = qt.setJson("objFav", new JSONArray(), "objInfo", new JSONArray());
            qt.setMDContent(id_U, qt.setJson("fav", initFav), User.class);
            user.setFav(initFav);
        }
        if (user.getFav().getJSONArray("objInfo") == null)
        {
            // init if null
            JSONObject initFav = qt.setJson("objInfo", new JSONArray());
            qt.setMDContent(id_U, qt.setJson("fav.objInfo", initFav), User.class);
            user.getFav().put("objInfo", initFav);
        }
        if (user.getFav().getJSONArray("objFav") == null)
        {  // init if null
            JSONObject initFav = qt.setJson("objFav", new JSONArray());
            qt.setMDContent(id_U, qt.setJson("fav.objFav", initFav), User.class);
            user.getFav().put("objFav", initFav);
        }
        // return all those I init or already in user.getFav
        return retResult.ok(CodeEnum.OK.getCode(), user.getFav());
    }

    // del Fav from oItem objFav
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

    // del Fav from id_X objFav

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

        Order order = qt.getMDContent(id_O, qt.strList("action.objAction." + index + ".arrUA", "oItem.objItem."+index), Order.class);
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


    //powerUp need to set "key value" and really need to check auth and check WHY it can change (cresign ONLY API)

    @Override
    public ApiResponse setPowerup(String id_C, JSONObject capacity) {
        String id_A = qt.getId_A(id_C, "a-core");
        JSONObject jsonUpdate = qt.setJson("powerup", capacity);
        qt.setMDContent(id_A, jsonUpdate, Asset.class);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    //powerUp need to set "key value" and really need to check auth and check WHY it can change (cresign ONLY API)

    @Override
    public ApiResponse getPowerup(String id_C, String ref) {
        Asset asset = qt.getConfig(id_C, "a-core", "powerup");
        JSONObject jsonPowerup = asset.getPowerup().getJSONObject("objSize").getJSONObject(ref);
        return retResult.ok(CodeEnum.OK.getCode(), jsonPowerup);
    }


    @Override
    public ApiResponse setRefAuto(String id_C, String type, JSONObject jsonRefAuto) {

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
            return retResult.ok(CodeEnum.OK.getCode(), new JSONArray());

        } else if (user.getCookiex().getJSONObject(id_C) == null){
            qt.setMDContent(id_U, qt.setJson("cookiex."+ id_C, objType), User.class);
            return retResult.ok(CodeEnum.OK.getCode(), new JSONArray());

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
                "DEFAULT_GROUP@@cresign-testCode");

        return retResult.ok(CodeEnum.OK.getCode(), qt.getRDHashMulti("nacosListener", serviceNames));
    }

    @Override
    public ApiResponse notifyLog(String id_U, String id_C, JSONObject wrdNU, String id, String id_I, JSONObject wrdN, JSONObject wrddesc) {
        Asset asset = qt.getConfig(id_C, "a-auth", "flowControl");
        JSONArray arrayFlow = asset.getFlowControl().getJSONArray("objData");
        JSONObject jsonNotify = qt.setJson("id_U", id_U,
                "wrdNU", wrdNU,
                "wrdN", wrdN,
                "wrddesc", wrddesc,
                "id_I", id_I,
                "tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        for (int i = 0; i < arrayFlow.size(); i++) {
            JSONObject jsonFlow = arrayFlow.getJSONObject(i);
            // find out the id_flow that wants to notify
            if (jsonFlow.getString("id").equals(id)) {
                JSONArray arrayNotify = jsonFlow.getJSONArray("notify");
                if (arrayNotify != null) {
                    if (arrayNotify.size() == 8) {
                        for (int j = 0; j < arrayNotify.size() - 1; j++) {
                            arrayNotify.set(j, arrayNotify.getJSONObject(j + 1));
                        }
                        arrayNotify.set(arrayNotify.size() - 1, jsonNotify);
                    } else {
                        arrayNotify.add(jsonNotify);
                    }
                } else {
                    // if null, init
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


}
