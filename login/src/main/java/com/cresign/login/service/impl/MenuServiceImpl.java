package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
//import com.cresign.login.service.AuthFilterService;
import com.cresign.login.service.MenuService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
//import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.InitJava;
import com.cresign.tools.pojo.po.assetCard.MainMenuBO;
import com.cresign.tools.pojo.po.assetCard.SubMenuBO;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ##description:
 * @author JackSon
 * @updated 2020-12-26 11:37
 * @ver 1.0
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MongoTemplate mongoTemplate;

//    @Autowired
//    private AuthFilterService authFilterService;

    @Autowired
    private AuthCheck authCheck;

    @Autowired
    private RetResult retResult;

    @Autowired
    private Qt qt;


    @Override
    public ApiResponse getMenusAndSubMenus(String id_C) {
        //之前是调用login服务的权限API，现在是移到login服务了，直接调用

        Asset asset =  qt.getConfig(id_C, "a-auth", "menu");

        return retResult.ok(CodeEnum.OK.getCode(), asset.getMenu());
    }

    @Override
    public ApiResponse getGrpUForMenusInRole(String id_C, String grpU) {
//            Query menuQuery = new Query(
//                    new Criteria("info.id_C").is(id_C)
//                            .and("info.ref").is("a-auth"));
//            menuQuery.fields().include("menu");
//            Asset asset = mongoTemplate.findOne(menuQuery, Asset.class);
            //JSONObject menuJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(menuQuery, Asset.class));
            Asset asset =  qt.getConfig(id_C, "a-auth", "menu");

            // 当前职位的主菜单数组
            //JSONArray grpUMainMenus = menuJson.getJSONObject("menu").getJSONObject("mainMenus").getJSONArray(grpU);
            JSONArray grpUMainMenus = asset.getMenu().getJSONObject("mainMenus").getJSONArray(grpU);
            // 没有该职位主菜单数据
            if (ObjectUtils.isEmpty(grpUMainMenus)) {
                throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode() , "");
            }
            return retResult.ok(CodeEnum.OK.getCode(), grpUMainMenus);

    }


    @Override
    public ApiResponse getMenuGrp(String id_C,String ref, String grpType) {
        Query menuQuery = new Query(
                new Criteria("info.id_C").is(id_C)
                        .and("info.ref").is("a-auth")
                            .and("menu.subMenus.ref").is(ref));
        menuQuery.fields().include("menu.subMenus.$");
        Asset asset = mongoTemplate.findOne(menuQuery, Asset.class);

        if (asset == null){
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, LoginEnum.MENU_DEL_ERROR.getCode(), null);
        }

        // 当前菜单数组的限定组别
        //asset.getMenu().getJSONArray("subMenus").getJSONObject(0).getJSONArray(grpType);
        return retResult.ok(CodeEnum.OK.getCode(), asset.getMenu().getJSONArray("subMenus").getJSONObject(0).getJSONArray(grpType));
    }


    @Override
    public ApiResponse getMenuListByGrpU(String id_C, String grpU) {
        // 最终返回数据
        JSONArray result = new JSONArray();

        // redis cache 先查redis缓存有没有改公司菜单
        if (qt.hasRDHashItem("details:get_menus", "compId-"+ id_C, grpU)) {
            String val = qt.getRDHashStr("details:get_menus","compId-" + id_C, grpU);
                return retResult.ok(CodeEnum.OK.getCode(), JSONArray.parse(val));
        }
        // 查询该公司的菜单

        Asset asset = qt.getConfig(id_C, "a-auth","menu");

        // 当前职位的主菜单数组
        JSONArray grpUMainMenus = asset.getMenu().getJSONObject("mainMenus").getJSONArray(grpU);

        // 子菜单数组
        JSONArray subMenus = asset.getMenu().getJSONArray("subMenus");


        // 没有该职位主菜单数据
        if (ObjectUtils.isEmpty(grpUMainMenus)) {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), "");

        }

        for (Object mainMenu : grpUMainMenus) {

            JSONObject mainMenuJson = (JSONObject) JSONObject.toJSON(mainMenu);
            // 该主菜单下的子菜单
            JSONArray mainSubMenus = mainMenuJson.getJSONArray("subMenus");

            // 用来包含子菜单数组
            JSONArray subMenusArray = new JSONArray();

            for (Object mainSubMenu : mainSubMenus) {
                String subMenuRef = mainSubMenu.toString();
                // 通过子菜单ref来获取子菜单的数据
                for (Object subMenu : subMenus) {
                    JSONObject subMenuJson = (JSONObject) JSON.toJSON(subMenu);
                    if (subMenuRef.equals(subMenuJson.getString("ref"))) {
                        JSONObject json = new JSONObject(subMenuJson);
                        subMenusArray.add(json);
                    }
                }
            }

            mainMenuJson.put("subMenus", subMenusArray);
            result.add(mainMenuJson);
        }

        // 获取成功后并缓存到redis中，下次拿redis缓存
        qt.putRDHash("details:get_menus","compId-"+ id_C, grpU,result.toJSONString());
        return retResult.ok(CodeEnum.OK.getCode(), result);

    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
    public ApiResponse updateMenuData(String id_U, String id_C, String grpU, List<MainMenuBO> mainMenusData) {

        authCheck.getUserUpdateAuth(id_U, id_C, "lSAsset", "1003", "card", new JSONArray().fluentAdd("menu"));

        boolean judge = true;

        //先判断grpU == 1001,是的话循环mainMenusData
        if (grpU.equals("1001")){
            for (int i = 0; i < mainMenusData.size(); i++) {
                MainMenuBO moduleObj = mainMenusData.get(i);
                //拿对象里面的ref判断有没有ppp(控制台)
                if (moduleObj.getRef().equals("PPP")){
                    if (!moduleObj.getSubMenus().contains("listUser")){
                        moduleObj.getSubMenus().add("listUser");
                    }
                    if (!moduleObj.getSubMenus().contains("listControl")){
                        moduleObj.getSubMenus().add("listControl");
                    }

                    judge = false;
                }
            }
            //judge = true的话证明mainMenusData里面控制台被删除，去cn_java找回来（默认不给删除）
            if (judge){
                Query initQ = new Query(
                        new Criteria("_id").is("cn_java")
                            .and("newComp.a-auth.menu.mainMenus.1001.ref").is("PPP"));
                initQ.fields().include("newComp.a-auth.menu.mainMenus.1001.$");
                InitJava initJava = mongoTemplate.findOne(initQ, InitJava.class);
                //JSONObject转成MainMenuBO实体类添加进mainMenusData数组
                mainMenusData.add(JSONObject.parseObject(String.valueOf(initJava.getNewComp().getJSONObject("a-auth").getJSONObject("menu")
                        .getJSONObject("mainMenus").getJSONArray("1001").getJSONObject(0)),MainMenuBO.class));
            }
        }

        Asset asset = qt.getConfig(id_C, "a-auth","menu");

//        Update mainMenuUd = new Update();
//        mainMenuUd.set("menu.mainMenus." + grpU, mainMenusData);
//        UpdateResult updateResult = mongoTemplate.updateFirst(menuQuery, mainMenuUd, Asset.class);
        qt.setMDContent(asset.getId(),qt.setJson("menu.mainMenus." + grpU, mainMenusData), Asset.class);

        qt.delRD("details:get_menus", "compId-" + id_C);

        return retResult.ok(CodeEnum.OK.getCode(), null);

    }

    @Override
    public ApiResponse getSubMenusData(String id_C) {

        Asset asset =  qt.getConfig(id_C, "a-auth", "menu");

        return retResult.ok(CodeEnum.OK.getCode(), asset.getMenu().getJSONArray("subMenus"));
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
    public ApiResponse updateSubMenuData(String id_U, String id_C, List<SubMenuBO> subMenuBOS) {
        // 权限校验
//        JSONArray params = new JSONArray();
//        params.add("menu");
//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", "lSAsset");
//        reqJson.put("grp", "1003");
//        reqJson.put("authType", "card");
//        reqJson.put("params", params);
//
//        authFilterClient.getUserUpdateAuth(reqJson);

//        authFilterService.getUserSelectAuth(id_U,id_C,"lSAsset","1003","card");


//        String id_A = dbUtils.getId_A(id_C, "a-auth");
//        Query menuQuery = new Query(new Criteria("_id").is(id_A));
//        menuQuery.fields().include("menu");
        Asset asset =  qt.getConfig(id_C, "a-auth", "menu");

//        Update mainMenuUd = new Update();
//        mainMenuUd.set("menu.subMenus", subMenuBOS);
//        mongoTemplate.updateFirst(menuQuery, mainMenuUd, Asset.class);
        qt.setMDContent(asset.getId(),qt.setJson("menu.subMenus", subMenuBOS), Asset.class);



        // 删除key重新设置缓存
        qt.delRD("details:get_menus","compId-" + id_C);
        return retResult.ok(CodeEnum.OK.getCode(), null);

    }

    @Override
    public ApiResponse getDefListType(String id_C) {

        Asset asset = qt.getConfig(id_C, "a-auth", "def");

        return retResult.ok(CodeEnum.OK.getCode(), asset.getDef());


    }
//
//    @Override
//    public ApiResponse checkSubMenuUse(String id_U, String id_C, String ref) {
//
//        Query query = new Query(Criteria.where("info.id_C").is(id_C).and("info.ref").is("a-auth"));
//        query.fields().include("menu");
//        Asset asset = mongoTemplate.findOne(query, Asset.class);
//
//        //JSONObject mainMenusJson = (JSONObject) JSONObject.toJSON(asset.getMenu().get("mainMenus"));
//        JSONObject mainMenusJson = asset.getMenu().getJSONObject("mainMenus");
//        for (String grpU : mainMenusJson.keySet()) {
//
//            JSONArray grpUMainMenus = mainMenusJson.getJSONArray(grpU);
//
//            for (Object grpUMainMenu : grpUMainMenus) {
//
//
//                JSONObject grpUMain = (JSONObject) grpUMainMenu;
//                JSONArray subMenus1 = grpUMain.getJSONArray("subMenus");
//
//
//                for (int i = 0; i < subMenus1.size(); i++) {
//
//                    // 判断出主菜单有该子菜单
//                    if (subMenus1.get(i).equals(ref)) {
//
//                        throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
//MAINMENU_USE_SUBMENU.getCode(), null);
//
//                    }
//                }
//            }
//        }
//
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//
//    }
////
//    @Override
//    @Transactional
//    public ApiResponse delSubMenu(String id_U, String id_C, String ref) {
//
//        /**
//         * 1.  判断前端ref不可是listUser/listControl。这两个不可删除
//         * 2. 先循环删除每个职位的主菜单里面的子菜单
//         * 3. 再删除子菜单
//         */
//
//        if (ref.equals("listUser") || ref.equals("listControl")){
//
//            throw new ErrorResponseException(HttpStatus.REQUEST_TIMEOUT, LoginEnum.REF_DEL_ERROR.getCode(), null);
//
//        }
//
//
//        try {
//            Query query = new Query(Criteria.where("info.id_C").is(id_C).and("info.ref").is("a-auth"));
//            query.fields().include("menu");
//            Asset asset = mongoTemplate.findOne(query, Asset.class);
//            JSONObject mainMenusJson = asset.getMenu().getJSONObject("mainMenus");
//            //JSONObject mainMenusJson = (JSONObject) JSONObject.toJSON(asset.getMenu().get("mainMenus"));
//
//
//
//            for (String grpU : mainMenusJson.keySet()) {
//
//                JSONArray grpUMainMenus = mainMenusJson.getJSONArray(grpU);
//                JSONArray newGrpUMainMenus = new JSONArray();
//
//                for (Object grpUMainMenu : grpUMainMenus) {
//
//
//                    JSONObject grpUMain = (JSONObject) grpUMainMenu;
//                    JSONArray subMenus1 = grpUMain.getJSONArray("subMenus");
//
//                    JSONObject newSubMenuObj = new JSONObject(grpUMain);
//
//                    for (int i = 0; i < subMenus1.size(); i++) {
//                        if (subMenus1.get(i).equals(ref)) {
//                            subMenus1.remove(i);
//                        }
//                    }
//
//                    newSubMenuObj.put("subMenus", subMenus1);
//
//                    newGrpUMainMenus.add(newSubMenuObj);
//
//                    mainMenusJson.put(grpU, newGrpUMainMenus);
//
//                }
//
//            }
//
//
//            // 删除主菜单里面的子菜单
//            mongoTemplate.updateFirst(query, Update.update("menu.mainMenus", mainMenusJson), Asset.class);
//
//
//            // 删除子菜单
//            Query subMenuQ = new Query(Criteria.where("menu.subMenus.ref").is(ref).and("info.id_C").is(id_C).and("info.ref").is("a-auth"));
//            Update update = new Update();
//            Document doc = new Document();
//            doc.put("ref", ref);
//            update.pull("menu.subMenus", doc);
//
//            mongoTemplate.updateFirst(subMenuQ, update, Asset.class);
//        } catch (RuntimeException e) {
//
//            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.MENU_DEL_ERROR.getCode(), null);
//
//        }
//
//
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//
//    }
}