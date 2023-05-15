package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.MenuService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.InitJava;
import com.cresign.tools.pojo.po.assetCard.MainMenuBO;
import com.cresign.tools.pojo.po.assetCard.SubMenuBO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    @Autowired
    private AuthCheck authCheck;

    @Autowired
    private RetResult retResult;

    @Autowired
    private Qt qt;

    @Autowired
    private Ws ws;


    @Override
    public ApiResponse getMenusAndSubMenus(String id_C) {
        //之前是调用login服务的权限API，现在是移到login服务了，直接调用

        Asset asset =  qt.getConfig(id_C, "a-auth","menu");

        return retResult.ok(CodeEnum.OK.getCode(), asset.getMenu());
    }

    @Override
    public ApiResponse getGrpUForMenusInRole(String id_C, String grpU) {

            Asset asset =  qt.getConfig(id_C, "a-auth", "menu");

            // 当前职位的主菜单数组
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
//        Asset asset = qt.getConfig(id_C, "a-auth", "menu.subMenus");

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
//                InitJava initJava = mongoTemplate.findOne(initQ, InitJava.class);
                InitJava initJava = qt.getInitData();
                //JSONObject转成MainMenuBO实体类添加进mainMenusData数组
                mainMenusData.add(JSONObject.parseObject(String.valueOf(initJava.getNewComp().getJSONObject("a-auth").getJSONObject("menu")
                        .getJSONObject("mainMenus").getJSONArray("1001").getJSONObject(0)),MainMenuBO.class));
            }
        }

        qt.delRD("details:get_menus", "compId-" + id_C);

        ws.sendWS_grpU(id_C, id_U,"ud_grpU_mainMenu");

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

        Asset asset =  qt.getConfig(id_C, "a-auth", "menu");

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

}