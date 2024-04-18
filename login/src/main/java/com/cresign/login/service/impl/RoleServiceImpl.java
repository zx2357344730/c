package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.RoleService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.InitJava;
import com.cresign.tools.pojo.po.LogFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ##description:
 * @author JackSon
 * @updated 2020-12-26 11:26
 * @ver 1.0
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private AuthCheck authCheck;

    @Autowired
    private RetResult retResult;

    @Autowired
    private Qt qt;

    @Autowired
    private Ws ws;

    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
    @Override
    public ApiResponse updateRole(String id_U, String id_C, String listType, String grp, String dataName, String authType, Integer auth, String grpU) {

        authCheck.getUserUpdateAuth(id_U,id_C,"lSAsset","1003","card",new JSONArray().fluentAdd("role"));

        // 查询出该修改的键，并且修改
        String id_A = qt.getId_A(id_C, "a-auth");

        //role.objData.1001.lSProd.1000.card.info => 2
        qt.setMDContent(id_A, qt.setJson("role.objData." + grpU + "." + listType + "." + grp + "." + authType + "." + dataName,
                auth), Asset.class);
        String redisKey = grpU + "_" + listType + "_" + grp + "_" + authType;
        qt.delRDHashItem("login:get_read_auth", "compId-"+id_C, redisKey);
        qt.delRDHashItem("login:get_readwrite_auth", "compId-" + id_C, redisKey);

//        ws.sendWS_grpU(id_C,id_U,"updateRole");

        return retResult.ok(CodeEnum.OK.getCode(), "");


    }


    @Override
    public ApiResponse getRoleDataByGrpUAndGrp(String id_U, String id_C, String listType, String grp, String grpU) {

        //authFilterService.getUserUpdateAuth(id_U,id_C,"lSAsset","1003","card",new JSONArray().fluentAdd("role"));

        if (grpU.equals("1099") && !(listType.equals("lSProd")|| listType.equals("lSInfo"))) {
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, ErrEnum.GRP_NOT_AUTH.getCode(), null);

        }

        Asset asset = qt.getConfig(id_C, "a-auth", "role.objData");

        // 判断 权限下的职位、列表类型、组别是否为空，为空则初始化模块
        if (null == asset.getRole().getJSONObject("objData").getJSONObject(grpU)
                || null == asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType)
                || null == asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp)) {
            //去init_java 初始化模块
            JSONObject result = this.addModInitRole(id_C, listType);

            qt.setMDContent(asset.getId(), qt.setJson("role.objData." + grpU + "." + listType + "." + grp, result), Asset.class);

            qt.delRD("login:get_read_auth", "compId-"+id_C);
            qt.delRD("login:get_readwrite_auth", "compId-" + id_C);

            return retResult.ok(CodeEnum.OK.getCode(), result);
        } else {

            //Else the data was already in the role.objData
            JSONObject grpJson = asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp);
            return retResult.ok(CodeEnum.OK.getCode(), grpJson);

        }
    }
        // 最终结果返回
//        JSONObject result = new JSONObject();
//
//        result.put("card", grpJson.getJSONObject("card"));
//        result.put("batch", grpJson.getJSONObject("batch"));// mod的键
//            for (String modKey : grpJson.keySet()) {
//
//                JSONObject modObj = grpJson.getJSONObject(modKey);
//
//                JSONObject modJson = new JSONObject();
//                modJson.put("moduleRef", modKey);
//
//                JSONArray cardArray = modObj.getJSONArray("card");
//                // 最终返回卡片权限数组
//                JSONArray resultCardArray = new JSONArray();
//
//                for (Object cardObj : cardArray) {
//                    JSONObject cardJson = (JSONObject) cardObj;
//                    cardJson.put("rowId", grpU + "_" + listType + "_" + grp + "_" + modKey + "_" + cardJson.getString("ref"));
//                    resultCardArray.add(cardJson);
//                }
//
//                JSONArray batchArray = modObj.getJSONArray("batch");
//                // 最终返回按钮权限数组
//                JSONArray resultBatchArray = new JSONArray();
//
//                for (Object batchObj : batchArray) {
//                    JSONObject batchJson = (JSONObject) batchObj;
//                    batchJson.put("rowId", grpU + "_" + listType + "_" + grp + "_" + modKey + "_" + batchJson.getString("ref"));
//                    resultBatchArray.add(batchJson);
//                }
//
//
//                modJson.put("rowId", modKey);
//                modJson.put("cardChildren", resultCardArray);
//                modJson.put("batchChildren", resultBatchArray);
//
//                result.add(modJson);
//
//            }
    @Override
    public ApiResponse updateNewestRole(String id_C, String listType, String grp, String grpU) {

        Asset asset = qt.getConfig(id_C, "a-auth", "role.objData."+ grpU + "." + listType + "." + grp);
        JSONObject grpJson = asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp);

        JSONObject initGrpJson = this.addModInitRole(id_C, listType);
        JSONObject resultJson = qt.cloneObj(grpJson);

        qt.errPrint("newestRole init", null, initGrpJson);
        //Now, start to compare:
        //check Card first:
        try {

            if (grpJson.getJSONObject("card") == null)
            {
                grpJson.put("card", new JSONObject());
                resultJson.put("card", new JSONObject());

            }
            if (grpJson.getJSONObject("batch") == null)
            {
                grpJson.put("batch", new JSONObject());
                resultJson.put("batch", new JSONObject());

            }


            for (String key : initGrpJson.getJSONObject("card").keySet()) {
                //if init+, grp-, add to grp
                if (!grpJson.getJSONObject("card").keySet().contains(key)) {
                    resultJson.getJSONObject("card").put(key, initGrpJson.getJSONObject("card").getIntValue(key));

                }
            }
            qt.errPrint("newestRole init", null, resultJson.getJSONObject("card"));


            for (String key : grpJson.getJSONObject("card").keySet()) {
                //if init+, grp-, add to grp
                if (!initGrpJson.getJSONObject("card").keySet().contains(key)) {
                    resultJson.getJSONObject("card").remove(key);
                }

                //Double check tempa card - must only exist in grp 1019 @ P / O
                if (key.equals("tempa") && !grp.equals("1019"))
                {
                    resultJson.getJSONObject("card").remove("tempa");
                }
            }

            qt.errPrint("newestRole init", resultJson.getJSONObject("card"));

            //check Batch:
            for (String key : initGrpJson.getJSONObject("batch").keySet()) {
                //if init+, grp-, add to grp
                if (!grpJson.getJSONObject("batch").keySet().contains(key)) {
                    resultJson.getJSONObject("batch").put(key, initGrpJson.getJSONObject("batch").getIntValue(key));
                }
            }
            for (String key : grpJson.getJSONObject("batch").keySet()) {
                //if init+, grp-, add to grp
                if (!initGrpJson.getJSONObject("batch").keySet().contains(key)) {
                    resultJson.getJSONObject("batch").remove(key);
                }
            }

            //check Log:
//            if (listType.equals("lSProd") || listType.equals("lBProd")) {
//                if (grpJson.getJSONObject("log") == null)
//                {
//                    grpJson.put("log", new JSONObject());
//                    resultJson.put("log", new JSONObject());
//                }
//
////                for (String key : initGrpJson.getJSONObject("log").keySet()) {
////                    //if init+, grp-, add to grp
////                    if (!grpJson.getJSONObject("log").keySet().contains(key)) {
////                        resultJson.getJSONObject("log").put(key, initGrpJson.getJSONObject("log").getIntValue(key));
////                    }
////                }
////                for (String key : grpJson.getJSONObject("log").keySet()) {
////                    //if init+, grp-, add to grp
////                    if (!initGrpJson.getJSONObject("log").keySet().contains(key)) {
////                        resultJson.getJSONObject("log").remove(key);
////                    }
////                }
//            }
//            System.out.println(asset);

            qt.setMDContent(asset.getId(), qt.setJson("role.objData." + grpU + "." + listType + "." + grp, resultJson), Asset.class);

            qt.delRD("login:get_read_auth", "compId-" + id_C);
            qt.delRD("login:get_readwrite_auth", "compId-" + id_C);

        } catch (Exception e)
            {
                e.printStackTrace();
            }

        return retResult.ok(CodeEnum.OK.getCode(), grpJson);
    }


    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
    @Override
    // Batch loop and set all Auth to upAuth in a grpU.lType.grp
    public ApiResponse upRoleAllAuth(String id_U, String id_C, String listType, String grp, Integer upAuth, String upType, String grpU) {

        authCheck.getUserUpdateAuth(id_U,id_C,"lSAsset","1003","card",new JSONArray().fluentAdd("role"));

        String authPath = "role.objData." + grpU + "." + listType + "." + grp + "." + upType;

        Asset asset = qt.getConfig(id_C, "a-auth", "role.objData." + grpU + "." + listType + "." + grp + "." + upType);

        // 没有设置职位权限
        if (null == asset.getRole().getJSONObject("objData").getJSONObject(grpU)) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ROLE_NOT_SET.getCode(), null);
        }


        // 要修改的列表
        JSONObject upTypeList = asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp).getJSONObject(upType);

//        JSONArray resultSet = new JSONArray();

        // 循环修改 auth改成前端传入的upAuth
//        for (int i = 0; i < upTypeList.size(); i++) {
//            JSONObject upTypeJson = upTypeList.getJSONObject(i);
//            upTypeJson.put("auth", upAuth);
//            resultSet.add(upTypeJson);
//        }
        for (String authType : upTypeList.keySet())
        {
            if (grp.equals("1019") || !authType.equals("tempa")) {
                upTypeList.put(authType, upAuth);
            }
        }


//        Update update = new Update();
//        update.set("role.objAuth." + grpU + "." + listType + "." + grp + "." + upType, resultSet);
//        UpdateResult updateResult = mongoTemplate.updateFirst(roleQ, update, Asset.class);
//
        qt.setMDContent(asset.getId(),qt.setJson(authPath, upTypeList), Asset.class);

//        // 修改失败
//        if (updateResult.getModifiedCount() < 0) {
//            // 修改出现错误
//            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.ROLE_UP_ERROR.getCode(), null);
//        }

//        redisTemplate0.opsForHash().delete("login:get_read_auth:compId-" + id_C, grpU + "_" + listType + "_" + grp + "_" + upType);
//        redisTemplate0.opsForHash().delete("login:get_readwrite_auth:compId-" + id_C, grpU + "_" + listType + "_" + grp + "_" + upType);
        String redisKey = grpU + "_" + listType + "_" + grp + "_" + upType;
        qt.delRDHashItem("login:get_read_auth", "compId-"+id_C, redisKey);
        qt.delRDHashItem("login:get_readwrite_auth", "compId-" + id_C, redisKey);

//        ws.sendWS_grpU(id_C,id_U,"up_grp_all_auth");

        return retResult.ok(CodeEnum.OK.getCode(), "");


    }

    @Override
    public ApiResponse copyGrpU(String id_U, String id_C, String to_grpU, String grpU)
    {

        Asset asset = qt.getConfig(id_C, "a-auth", qt.strList("role.objData." + grpU,"menu.mainMenus." + grpU, "menu.subMenus"));
        System.out.println("asset"+asset);
        // 没有设置职位权限
        if (null == asset.getRole().getJSONObject("objData").getJSONObject(grpU)) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ROLE_NOT_SET.getCode(), null);
        }

        // 复制的组别数据
        JSONObject copyRole = asset.getRole().getJSONObject("objData").getJSONObject(grpU);
        JSONArray copyMenu = asset.getMenu().getJSONObject("mainMenus").getJSONArray(grpU);


//        System.out.println("role"+copyRole);
//        System.out.println("menu"+copyMenu);

        qt.setMDContent(asset.getId(), qt.setJson("role.objData." + to_grpU, copyRole, "menu.mainMenus."+ to_grpU, copyMenu),Asset.class);

        qt.delRD("login:get_read_auth", "compId-"+id_C);
        qt.delRD("login:get_readwrite_auth", "compId-" + id_C);

//        JSONArray subMenus = asset.getMenu().getJSONArray("subMenus");
//
//        // 存储修改的返回信息
//        JSONArray result = new JSONArray();
//        // 遍历当前修改的菜单
//        for (Object mainMenusDatum : copyMenu) {
//            JSONObject mainMenuJson = qt.cloneObj(qt.toJson(mainMenusDatum));
//            // 该主菜单下的子菜单
//            JSONArray mainSubMenus = mainMenuJson.getJSONArray("subMenus");
//            // 用来包含子菜单数组
//            JSONArray subMenusArray = new JSONArray();
//            for (int j = 0; j < mainSubMenus.size(); j++) {
//                String subMenuRef = mainSubMenus.getString(j);
//                for (int z = 0; z < subMenus.size(); z++) {
//                    JSONObject subMenuJson = subMenus.getJSONObject(z);
//                    // 判断ref一样，并添加对应的ref信息
//                    if (subMenuRef.equals(subMenuJson.getString("ref"))) {
//                        subMenusArray.add(subMenuJson);
//                    }
//                }
//            }
//            mainMenuJson.put("subMenus", subMenusArray);
//            result.add(mainMenuJson);
//        }
//        JSONObject data = qt.setJson("type", "chgUserGrp");
        LogFlow log = new LogFlow();
        log.setSysLog(id_C, "mut_menu", "更新菜单", 3, qt.setJson("cn", "系统权限更新"));
        ws.setUserListByGrpU(log, id_C, grpU);
        log.setData(qt.setJson("type", "chgUserGrp"));
        ws.sendWSDirect(log);

        return retResult.ok(CodeEnum.OK.getCode(), "");

    }




    //    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
    @Override
    public ApiResponse copyGrpRoleToOtherGrp(String id_U, String id_C, String listType, String copy_grp, JSONArray
            to_grp, String grpU) {


        Asset asset = qt.getConfig(id_C, "a-auth", "role.objData." + grpU + "." + listType + "." + copy_grp);

        System.out.println("asset"+asset);
        // 没有设置职位权限
        if (null == asset.getRole().getJSONObject("objData").getJSONObject(grpU)) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ROLE_NOT_SET.getCode(), null);
        }

        // 复制的组别数据
        JSONObject copy_grp_json = asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType).getJSONObject(copy_grp);

        JSONObject setUpdate = new JSONObject();

        // 循环将复制组别的数据拷贝到要复制的组别上
        for (Object to_grp_key : to_grp) {
            setUpdate.put("role.objData." + grpU + "." + listType + "." + to_grp_key, copy_grp_json);
        }

        qt.setMDContent(asset.getId(), setUpdate, Asset.class);
        qt.delRD("login:get_read_auth", "compId-"+id_C);
        qt.delRD("login:get_readwrite_auth", "compId-" + id_C);

        return retResult.ok(CodeEnum.OK.getCode(), null);


    }

    /**
     * 初始化权限模块的数据
     *
     * @param id_C
     * @param listType
     * @author JackSon
     * @ver 1.0
     * @updated 2021-01-22 13:29
     * @return void
     */
    private JSONObject addModInitRole(String id_C, String listType) {

        //1. check control has that mod?
        //2. get a list of all CardBatchLog we have now
        InitJava initJson = qt.getInitData();

        JSONObject resultModObj = qt.getConfig(id_C, "a-core", "control").getControl().getJSONObject("objMod");

        JSONObject myModObject = qt.cloneObj(resultModObj);

        JSONObject cardInit = initJson.getCardInit();
        JSONObject batchInit = initJson.getBatchInit();
        JSONObject logInit = initJson.getLogInit();

        JSONObject resultCardObj = new JSONObject();
        JSONObject resultBatchObj = new JSONObject();
        JSONObject resultLogObj = new JSONObject();

        JSONObject result = new JSONObject();

        // 先获取该用户已拥有的模块
            for (String key : cardInit.keySet()) {

                JSONObject cardJson = cardInit.getJSONObject(key);
                JSONArray cardListType = cardInit.getJSONObject(key).getJSONArray("listType");
                    for (String modKey : myModObject.keySet()) {
                        String modArrayRef = myModObject.getJSONObject(modKey).getString("mod");
                        Integer modArrayLevel = myModObject.getJSONObject(modKey).getInteger("bcdLevel");

                        System.out.println(cardJson.getString("ref"));
                        if (modArrayRef.equals("a-"+cardJson.getString("modRef"))
                            && (cardListType.contains(listType.substring(2)) || cardListType.contains("all"))
                            && modArrayLevel >= cardJson.getInteger("bcdLevel")) {

                            System.out.println("it is included");
                        resultCardObj.put(cardJson.getString("ref"), 0);
                    }
                }
            }


            // 初始化该卡片对象数组
            try {
                for (String key : batchInit.keySet()) {
                    JSONObject batchJson = batchInit.getJSONObject(key);
                    JSONArray batchListType = batchInit.getJSONObject(key).getJSONArray("listType");

                    for (String modKey : myModObject.keySet()) {
                        String modArrayRef = myModObject.getJSONObject(modKey).getString("mod");
                        Integer modArrayLevel = myModObject.getJSONObject(modKey).getInteger("bcdLevel");

                        if (modArrayRef.equals(batchJson.getString("modRef"))
                                && (batchListType.contains(listType.substring(2)) || batchListType.contains("all"))
                                && modArrayLevel >= (batchJson.getInteger("bcdLevel"))) {
                            resultBatchObj.put(batchJson.getString("ref"), 0);
                        }
                    }
                }

                result.put("card", resultCardObj);
                result.put("batch", resultBatchObj);

                //Special treatment for lSProd/lBProd to add the logAuth into it
                // well, maybe not working this way now
                if (listType.equals("lSProd") || listType.equals("lBProd")) {
                    for (String key : logInit.keySet()) {
                        JSONObject logJson = logInit.getJSONObject(key);

                        for (String modKey : myModObject.keySet()) {
                            String modArrayRef = myModObject.getJSONObject(modKey).getString("mod");
                            Integer modArrayLevel = myModObject.getJSONObject(modKey).getInteger("bcdLevel");

                            if (modArrayRef.equals(logJson.getString("modRef")) && modArrayLevel >= (logJson.getInteger("bcdLevel"))) {
                                resultLogObj.put(logJson.getString("ref"), 0);
                            }
                        }
                    }

                    result.put("log", resultLogObj);
                }

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        return result;
    }
}