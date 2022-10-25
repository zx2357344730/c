package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
//import com.cresign.login.service.AuthFilterService;
import com.cresign.login.service.RoleService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.InitJava;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ##description:
 * @author JackSon
 * @updated 2020-12-26 11:26
 * @ver 1.0
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private AuthCheck authCheck;

    @Autowired
    private RetResult retResult;

    @Autowired
    private Qt qt;

//    @Override
//    public ApiResponse getRole(String id_U, String id_C, String grpU) {
//        // 权限校验
////        JSONArray params = new JSONArray();
////        params.add("role");
////        JSONObject reqJson = new JSONObject();
////        reqJson.put("id_U", id_U);
////        reqJson.put("id_C", id_C);
////        reqJson.put("listType", "lSAsset");
////        reqJson.put("grp", "1003");
////        reqJson.put("authType", "card");
////        reqJson.put("params", params);
////
////        authFilterClient.getUserSelectAuth(reqJson);
//
//        //之前是调用login服务的权限API，现在是移到login服务了，直接调用
//        JSONArray canUpdate = authCheck.getUserSelectAuth(id_U,id_C, grpU,"lSAsset","1003","card");
//
//        if (canUpdate.contains("role")) {
//            Query menuQuery = new Query(
//                    new Criteria("info.id_C").is(id_C)
//                            .and("info.ref").is("a-auth"));
//            menuQuery.fields().include("role");
//            Asset asset = mongoTemplate.findOne(menuQuery, Asset.class);
//            //JSONObject roleJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(menuQuery, Asset.class));
//
//            return retResult.ok(CodeEnum.OK.getCode(), asset.getRole());
//        }
//
//        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode() , null);
//
//    }

//    @Override
//    public ApiResponse getRoleData(String id_U, String id_C, String listType, String grp, String grpU) {
//
////        JSONArray params = new JSONArray();
////        params.add("role");
////        JSONObject reqJson = new JSONObject();
////        reqJson.put("id_U", id_U);
////        reqJson.put("id_C", id_C);
////        reqJson.put("listType", listType);
////        reqJson.put("grp", grp);
////        reqJson.put("authType", "card");
////        reqJson.put("params", params);
////
////        authFilterClient.getUserSelectAuth(reqJson);
//
//        JSONArray canUpdate = authCheck.getUserSelectAuth(id_U,id_C, grpU,"lSAsset","1003","card");
//
//
//        if (canUpdate.contains("role")) {
//
//            // 查询该公司的权限 role卡片
//            Query query = new Query(
//                    new Criteria("info.id_C").is(id_C)
//                            .and("info.ref").is("a-auth"));
//            query.fields().include("role");
//            Asset asset = mongoTemplate.findOne(query, Asset.class);
//            //JSONObject resultJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(query, Asset.class));
//            //JSONObject grpJson = roleJson.getJSONObject("objAuth").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp);
//
//            JSONObject grpJson = asset.getRole().getJSONObject("objAuth").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp);
//
//
//            // 最终收集卡片的数组
//            JSONArray cardSArray = new JSONArray();
//
//            // mod的键
//            for (String modKey : grpJson.keySet()) {
//                JSONObject modObj = grpJson.getJSONObject(modKey);
//
//                // mod模块的版本
//                for (String modVer : modObj.keySet()) {
//                    JSONObject modVerObj = modObj.getJSONObject(modVer);
//
//
//                    for (String cardKey : modVerObj.getJSONObject("card").keySet()) {
//
//                        JSONObject cardJson = modVerObj.getJSONObject("card").getJSONObject(cardKey);
//
//                        // 最终返回的卡片
//                        JSONObject cardObj = new JSONObject();
//                        cardObj.put("module", modKey);
//                        cardObj.put("bcdLevel", modVer);
//                        cardObj.put("ref", cardKey);
//                        cardObj.put("auth", cardJson.getString("auth"));
//
//                        // 将fields放入
//                        JSONArray fieldsArray = new JSONArray();
//                        Set<Map.Entry<String, Object>> fields = cardJson.getJSONObject("fields").entrySet();
//                        for (Map.Entry<String, Object> field : fields) {
//                            JSONObject fieldJson = new JSONObject();
//                            fieldJson.put("key", field.getKey());
//                            fieldJson.put("auth", field.getValue());
//                            fieldsArray.add(fieldJson);
//                        }
//
//                        cardObj.put("data", fieldsArray);
//
//                        cardSArray.add(cardObj);
//
//                    }
//
//                }
//            }
//
//            return retResult.ok(CodeEnum.OK.getCode(), cardSArray);
//        }
//        throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode() , null);
//
//    }

    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
    @Override
    //ok
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

        return retResult.ok(CodeEnum.OK.getCode(), "");


    }

//        Query roleQ = new Query(
//                new Criteria("info.id_C").is(id_C)
//                        .and("info.ref").is("a-auth")
//                        .and("role.objAuth."
//                                + grpU + "."
//                                + listType + "."
//                                + grp + "."
//                                + authType).is(dataName));
//
//        Update roleUpdate = new Update();
//        System.out.println("data"+dataName);
//
//        // 拼接字符串
//        String updateKey = "role.objAuth."
//                + grpU + "."
//                + listType + "."
//                + grp + "."
//                + authType + ".$.auth";
//        roleUpdate.set(updateKey, auth);
//        System.out.println("what"+ updateKey);
//        System.out.println("what"+roleQ);
//        UpdateResult updateResult = mongoTemplate.updateFirst(roleQ, roleUpdate, Asset.class);
//
//        System.out.println("up"+updateResult);
//        // 没有找到该键值，无法修改
//        if (updateResult.getMatchedCount() == 0) {
//            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), null);
//        }
//        redisTemplate0.opsForHash().delete("login:get_read_authcompId-" + id_C, grpU + "_" + listType + "_" + grp + "_" + authType);
//        redisTemplate0.opsForHash().delete("login:get_readwrite_authcompId-" + id_C, grpU + "_" + listType + "_" + grp + "_" + authType);

//    @Override
//    public ApiResponse getListTypeGrp(String id_U, String id_C) {
//
//        authCheck.getUserUpdateAuth(id_U,id_C,"lSAsset","1003","card",new JSONArray().fluentAdd("role"));
//
//
//        String id_A = dbUtils.getId_A(id_C, "a-auth");
//        Query query = new Query(new Criteria("_id").is(id_A));
//        query.fields().include("def");
//        Asset asset = mongoTemplate.findOne(query, Asset.class);
//        //JSONObject assetJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(query, Asset.class));
//
//        // 最终返回数据
//        JSONArray resultArray = new JSONArray();
//
//        //JSONObject ocnlistType = assetJson.getJSONObject("def").getJSONObject("ocnlistType");
//        JSONObject ocnlistType = asset.getDef().getJSONObject("ocnlistType");
//        for (String listTypeKey : ocnlistType.keySet()) {
//
//            // 列表类型名称
//            String listTypeName = ocnlistType.getString(listTypeKey);
//
//            JSONObject listTypeGrpData = new JSONObject();
//            listTypeGrpData.put("name", listTypeName);
//            listTypeGrpData.put("key", listTypeKey);
//
//            // 动态获取每个列表类型的组别列表
//            listTypeGrpData.put("grpList", asset.getDef().getJSONObject("obj" + listTypeKey.substring(0,3)));
//            resultArray.add(listTypeGrpData);
//        }
//
//        return retResult.ok(CodeEnum.OK.getCode(), resultArray);
//
//    }

    @Override
    public ApiResponse getRoleDataByGrpUAndGrp(String id_U, String id_C, String listType, String grp, String grpU) {

//        JSONArray params = new JSONArray();
//        params.add("role");
//        JSONObject reqJson = new JSONObject();
//        reqJson.put("id_U", id_U);
//        reqJson.put("id_C", id_C);
//        reqJson.put("listType", "lSAsset");
//        reqJson.put("grp", "1003");
//        reqJson.put("authType", "card");
//        reqJson.put("params", params);
//
//        authFilterClient.getUserUpdateAuth(reqJson);


        //authFilterService.getUserUpdateAuth(id_U,id_C,"lSAsset","1003","card",new JSONArray().fluentAdd("role"));

        if (grpU.equals("1099") && !listType.equals("lSProd")) {
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, LoginEnum.GRP_NOT_AUTH.getCode(), null);

        }

//        String id_A = dbUtils.getId_A(id_C, "a-auth");
//        Query query = new Query(new Criteria("_id").is(id_A));
//        query.fields().include("role.objAuth." + grpU + "." + listType + "." + grp);
//        Asset asset = mongoTemplate.findOne(query, Asset.class);

        Asset asset = qt.getConfig(id_C, "a-auth", "role.objData");
        //JSONObject assetJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(query, Asset.class));


        //role.objData.(grpU.listType.grp.card/batch[authType])
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
            for (String key : grpJson.getJSONObject("card").keySet()) {
                //if init+, grp-, add to grp
                if (!initGrpJson.getJSONObject("card").keySet().contains(key)) {
                    resultJson.getJSONObject("card").remove(key);
                }
            }

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
            if (listType.equals("lSProd") || listType.equals("lBProd")) {
                if (grpJson.getJSONObject("log") == null)
                {
                    grpJson.put("log", new JSONObject());
                    resultJson.put("log", new JSONObject());
                }

                for (String key : initGrpJson.getJSONObject("log").keySet()) {
                    //if init+, grp-, add to grp
                    if (!grpJson.getJSONObject("log").keySet().contains(key)) {
                        resultJson.getJSONObject("log").put(key, initGrpJson.getJSONObject("log").getIntValue(key));
                    }
                }
                for (String key : grpJson.getJSONObject("log").keySet()) {
                    //if init+, grp-, add to grp
                    if (!initGrpJson.getJSONObject("log").keySet().contains(key)) {
                        resultJson.getJSONObject("log").remove(key);
                    }
                }
            }
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


//    @Override
//    public ApiResponse updateNewestRole2(String id_U, String id_C, String listType, String grp, String grpU) {
//
//        //authFilterService.getUserUpdateAuth(id_U,id_C,"lSAsset","1003","card",new JSONArray().fluentAdd("role"));
//
//        //1. get a-auth
//        //2. get control
//        //3. get initJava
//        //4. use control's all modules to check include, then get a list of all the BCL
//
//        //5. check against what is in a-auth now, if con yes, list no, add, else con no list yes delete, con yes list yes do nothing
//
//
//
//            String id_A = dbUtils.getId_A(id_C, "a-auth");
//            Query query = new Query(new Criteria("_id").is(id_A));
////            query.fields().include("role.objAuth." + grpU + "." + listType + "." + grp);
////            Asset asset = mongoTemplate.findOne(query, Asset.class);
////            JSONObject grpJson = asset.getRole().getJSONObject("objAuth").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp);
//
//
//            Asset asset = qt.getConfig(id_C, "a-auth", "role.objData."+ grpU + "." + listType + "." + grp);
//            JSONObject grpJson = asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp);
//
//        // 获取模块的初始化数据
////            Query initQ = new Query(new Criteria("_id").is("cn_java"));
////            initQ.fields().include("listTypeInit").include("cardInit").include("batchInit");
////            JSONObject initJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(initQ, InitJava.class));
//
//            InitJava initJson = qt.getMDContent("cn_java", qt.strList("listTypeInit","cardInit","batchInit"), InitJava.class);
//
//        JSONObject listTypeInit = initJson.getListTypeInit();
//        JSONObject cardInit = initJson.getCardInit();
//        JSONObject batchInit = initJson.getBatchInit();
//            // 先获取该用户已拥有的模块
//
//            JSONObject controlJson = qt.getConfig(id_C, "a-core", "control").getControl();
//
//
//
////            String id_A2 = dbUtils.getId_A(id_C, "a-module");
////            Query myModQ = new Query(new Criteria("_id").is(id_A2));
////            myModQ.fields().include("control");
////            JSONObject controlJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(myModQ, Asset.class));
//
//            JSONArray resultModArray = controlJson.getJSONObject("control").getJSONArray("objData");
//            //克隆
//            // data is objData.[].{tfin/ref/tmk/wn0buyUser/id_P/bcdLevel/wcnN...
//        // data changed to objMod now with ref: {{
////            JSONArray myModArray = (JSONArray) resultModArray.clone();
//            JSONArray myModArray = qt.cloneArr(resultModArray);
//
//
//            //原理：1.拿主resultModArray和克隆myModArray 循环比较，
//            //     2.ref想同和主resultModArray的等级大于克隆myModArray等级，删除克隆myModArray的对象
//            //     3.最后得出myModArray里面都是等级大的
//            // 获取control等级去重后的模块数组
//            for (int i = 0; i < resultModArray.size(); i++) {
//                for (int j = 0; j < myModArray.size(); j++) {
//                    if (resultModArray.getJSONObject(i).getString("ref").equals(myModArray.getJSONObject(j).getString("ref"))){
//                        if (resultModArray.getJSONObject(i).getInteger("bcdLevel") > myModArray.getJSONObject(j).getInteger("bcdLevel")){
//                            myModArray.fluentRemove(j);
//                        }
//                    }
//                }
//            }
//
//            // 初始化该卡片对象数组
//            List<JSONObject> cardList = new ArrayList<>();
//
//            // 循环获取这个列表类型的卡片对象
//            for (Object cardKey : listTypeInit.getJSONObject(listType).getJSONArray("card")) {
//
//                String cardRef = cardKey.toString();
//
//                JSONObject cardJson = cardInit.getJSONObject(cardRef);
//                cardList.add(cardJson);
//
//            }
//
//            // 初始化该卡片对象数组
//            List<JSONObject> batchList = new ArrayList<>();
//
//            // 循环获取这个列表类型的按钮对象
//            for (Object batchKey : listTypeInit.getJSONObject(listType).getJSONArray("batch")) {
//
//                String batchRef = batchKey.toString();
//
//                JSONObject cardJson = batchInit.getJSONObject(batchRef);
//                batchList.add(cardJson);
//
//            }
//
//            // 卡片列表最终返回
//            JSONArray resultCardArray = new JSONArray();
////            System.out.println("cardList"+JSON.toJSONString(cardList));
////        System.out.println("modArray"+JSON.toJSONString(myModArray));
//
//        for (JSONObject cardJson : cardList) {
//
//                for (int i = 0; i < myModArray.size(); i++) {
//                    //TODO KEV change this array to object and put ref:auth
//                    String modArrayRef = myModArray.getJSONObject(i).getString("ref");
//                    Integer modArrayLevel = myModArray.getJSONObject(i).getInteger("bcdLevel");
//                    if (modArrayRef.equals(cardJson.getString("modRef"))
//                            && modArrayLevel >= cardJson.getInteger("bcdLevel")) {
//                        resultCardArray.add(new JSONObject().fluentPut("ref",cardJson.getString("ref"))
//                                .fluentPut("modRef",cardJson.getString("modRef"))
//                                .fluentPut("auth",cardJson.getJSONObject("defRole").getInteger("auth"))
//                                .fluentPut("bcdLevel",cardJson.getInteger("bcdLevel")));
//                    }
//                }
//            }
//
//            // Batch功能列表最终返回
//            JSONArray resultBatchArray = new JSONArray();
//
//            for (JSONObject batchJson : batchList) {
//
//                for (int i = 0; i < myModArray.size(); i++) {
//                    //TODO KEV change this array to object and put ref:auth
//
//                    String modArrayRef = myModArray.getJSONObject(i).getString("ref");
//                    Integer modArrayLevel = myModArray.getJSONObject(i).getInteger("bcdLevel");
//
//                    if (modArrayRef.equals(batchJson.getString("modRef")) && modArrayLevel >= batchJson.getInteger("bcdLevel")) {
//                        resultBatchArray.add(new JSONObject().fluentPut("ref",batchJson.getString("ref"))
//                                .fluentPut("modRef",batchJson.getString("modRef"))
//                                    .fluentPut("auth",batchJson.getJSONObject("defRole").getInteger("auth"))
//                                        .fluentPut("bcdLevel",batchJson.getInteger("bcdLevel")));
//                    }
//                }
//            }
//
//
////            //card去重
////            for (int i = 0; i < grpJson.getJSONArray("card").size(); i++) {
////                for (int j = 0; j < resultCardArray.size(); j++) {
////                    if (resultCardArray.getJSONObject(j).getString("ref").equals(grpJson.getJSONArray("card").getJSONObject(i).getString("ref"))){
////                        resultCardArray.remove(j);
////                        j--;
////                    }
////                }
////            }
//
//            //batch去重
////            for (int i = 0; i < grpJson.getJSONArray("batch").size(); i++) {
////                for (int j = 0; j < resultBatchArray.size(); j++) {
////                    if (resultBatchArray.getJSONObject(j).getString("ref").equals(grpJson.getJSONArray("batch").getJSONObject(i).getString("ref"))){
////                        resultBatchArray.remove(j);
////                        j--;
////                    }
////                }
////            }
//
//            //replace instead
//            grpJson.put("card", resultCardArray);
//            grpJson.put("batch", resultBatchArray);
//
//
//
//
//            //grpJson.getJSONArray("card").addAll(resultCardArray);
//            //grpJson.getJSONArray("batch").addAll(resultBatchArray);
//
//            System.out.println("grpJson"+grpJson);
//
//            Update roleUpdate = new Update();
//            roleUpdate.set("role.objAuth." + grpU + "." + listType + "." + grp, grpJson);
//
//            mongoTemplate.updateFirst(query, roleUpdate, Asset.class);
//
//        qt.delRD("login:get_read_auth", "compId-"+id_C);
//        qt.delRD("login:get_readwrite_auth:", "compId-" + id_C);
//
//        return retResult.ok(CodeEnum.OK.getCode(), grpJson);
//
//
//    }

    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
    @Override
    // Batch loop and set all Auth to upAuth in a grpU.lType.grp
    public ApiResponse upRoleAllAuth(String id_U, String id_C, String listType, String grp, Integer upAuth, String upType, String grpU) {

        authCheck.getUserUpdateAuth(id_U,id_C,"lSAsset","1003","card",new JSONArray().fluentAdd("role"));

//        String id_A = dbUtils.getId_A(id_C, "a-auth");
//
//        Query roleQ = new Query(
//                new Criteria("_id").is(id_A));
//        roleQ.fields().include("role.objAuth." + grpU + "." + listType + "." + grp + "." + upType);
//        Asset asset = mongoTemplate.findOne(roleQ, Asset.class);
        //JSONObject roleJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(roleQ, Asset.class));

        String authPath = "role.objData." + grpU + "." + listType + "." + grp + "." + upType;

        Asset asset = qt.getConfig(id_C, "a-auth", "role.objData." + grpU + "." + listType + "." + grp + "." + upType);

        // 没有设置职位权限
        if (null == asset.getRole().getJSONObject("objData").getJSONObject(grpU)) {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.ROLE_NOT_SET.getCode(), null);
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
            upTypeList.put(authType, upAuth);
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

        return retResult.ok(CodeEnum.OK.getCode(), "");


    }

    @Override
    public ApiResponse copyGrpU(String id_U, String id_C, String to_grpU, String grpU)
    {
//        Query batchQ = new Query(
//                new Criteria("info.id_C").is(id_C)
//                        .and("info.ref").is("a-auth"));
//
//        batchQ.fields().include("role.objAuth." + grpU);
//        batchQ.fields().include("menu.mainMenus." + grpU);
//
//        Asset asset = mongoTemplate.findOne(batchQ, Asset.class);

        Asset asset = qt.getConfig(id_C, "a-auth", qt.strList("role.objData." + grpU,"menu.mainMenus." + grpU));
        System.out.println("asset"+asset);
        // 没有设置职位权限
        if (null == asset.getRole().getJSONObject("objData").getJSONObject(grpU)) {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.ROLE_NOT_SET.getCode(), null);
        }

        // 复制的组别数据
        JSONObject copyRole = asset.getRole().getJSONObject("objData").getJSONObject(grpU);
        JSONArray copyMenu = asset.getMenu().getJSONObject("mainMenus").getJSONArray(grpU);


        System.out.println("role"+copyRole);
        System.out.println("menu"+copyMenu);

//        Update update = new Update();
//        // 循环将复制组别的数据拷贝到要复制的组别上
//        update.set("role.objAuth." + to_grpU, copyRole);
//        update.set("menu.mainMenus."+ to_grpU, copyMenu);
//        UpdateResult updateResult = mongoTemplate.updateFirst(batchQ, update, Asset.class);
        qt.setMDContent(asset.getId(), qt.setJson("role.objData." + to_grpU, copyRole, "menu.mainMenus."+ to_grpU, copyMenu),Asset.class);

        // 修改失败
//        if (updateResult.getModifiedCount() < 0) {
//            // 修改出现错误
//            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.ROLE_UP_ERROR.getCode(), null);
//        }

//        redisTemplate0.delete("login:get_read_authcompId-" + id_C);
//        redisTemplate0.delete("login:get_readwrite_authcompId-" + id_C);
        qt.delRD("login:get_read_auth", "compId-"+id_C);
        qt.delRD("login:get_readwrite_auth", "compId-" + id_C);


        return retResult.ok(CodeEnum.OK.getCode(), "");

    }




    //    @Transactional(rollbackFor = RuntimeException.class, noRollbackFor = ResponseException.class)
    @Override
    public ApiResponse copyGrpRoleToOtherGrp(String id_U, String id_C, String listType, String copy_grp, JSONArray
            to_grp, String grpU) {


//        Query batchQ = new Query(
//                new Criteria("info.id_C").is(id_C)
//                        .and("info.ref").is("a-auth"));
//
//        batchQ.fields().include("role.objAuth." + grpU + "." + listType + "." + copy_grp);
//
//        Asset asset = mongoTemplate.findOne(batchQ, Asset.class);

        Asset asset = qt.getConfig(id_C, "a-auth", "role.objData." + grpU + "." + listType + "." + copy_grp);
        //JSONObject roleJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(batchQ, Asset.class));

        System.out.println("asset"+asset);
        // 没有设置职位权限
        if (null == asset.getRole().getJSONObject("objData").getJSONObject(grpU)) {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.ROLE_NOT_SET.getCode(), null);
        }

        // 复制的组别数据
        JSONObject copy_grp_json = asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType).getJSONObject(copy_grp);
//
//        System.out.println("asset"+copy_grp_json);
//
//        Update update = new Update();
//
        JSONObject setUpdate = new JSONObject();

        // 循环将复制组别的数据拷贝到要复制的组别上
        for (Object to_grp_key : to_grp) {
//            update.set("role.objAuth." + grpU + "." + listType + "." + to_grp_key, copy_grp_json);
            setUpdate.put("role.objData." + grpU + "." + listType + "." + to_grp_key, copy_grp_json);
        }

        qt.setMDContent(asset.getId(), setUpdate, Asset.class);
        qt.delRD("login:get_read_auth", "compId-"+id_C);
        qt.delRD("login:get_readwrite_auth", "compId-" + id_C);


//        UpdateResult updateResult = mongoTemplate.updateFirst(batchQ, update, Asset.class);

//        // 修改失败
//        if (updateResult.getModifiedCount() < 0) {
//            // 修改出现错误
//            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.ROLE_UP_ERROR.getCode(), null);
//        }

//        redisTemplate0.delete("login:get_read_authcompId-" + id_C);
//        redisTemplate0.delete("login:get_readwrite_authcompId-" + id_C);

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
        InitJava initJson = qt.getMDContent("cn_java", qt.strList("listTypeInit","cardInit","batchInit","logInit"), InitJava.class);

        JSONArray resultModArray = qt.getConfig(id_C, "a-core", "control").getControl().getJSONArray("objData");
        JSONArray myModArray = qt.cloneArr(resultModArray);


//        JSONObject listTypeInit = initJson.getListTypeInit();
        JSONObject cardInit = initJson.getCardInit();
        JSONObject batchInit = initJson.getBatchInit();
        JSONObject logInit = initJson.getLogInit();

        JSONObject resultCardObj = new JSONObject();
        JSONObject resultBatchObj = new JSONObject();
        JSONObject resultLogObj = new JSONObject();

        JSONObject result = new JSONObject();

        // 先获取该用户已拥有的模块

        //克隆
        // data is objData.[].{tfin/ref/tmk/wn0buyUser/id_P/bcdLevel/wcnN...
        // data changed to objMod now with ref: {{
//            JSONArray myModArray = (JSONArray) resultModArray.clone();

            // 初始化该卡片对象数组

//            // 循环获取这个列表类型的卡片对象
//            for (Object cardKey : listTypeInit.getJSONObject(listType).getJSONArray("card")) {
//                cardList.add(cardInit.getJSONObject(cardKey.toString()));
//            }
            for (String key : cardInit.keySet()) {

                JSONObject cardJson = cardInit.getJSONObject(key);
                JSONArray cardListType = cardInit.getJSONObject(key).getJSONArray("listType");
                for (int i = 0; i < myModArray.size(); i++) {
                    String modArrayRef = myModArray.getJSONObject(i).getString("ref");
                    Integer modArrayLevel = myModArray.getJSONObject(i).getInteger("bcdLevel");

                    if (modArrayRef.equals(cardJson.getString("modRef"))
                            && (cardListType.contains(listType.substring(2)) || cardListType.contains("all"))
                            && modArrayLevel <= (cardJson.getInteger("bcdLevel"))) {
                        resultCardObj.put(cardJson.getString("ref"), 0);
                    }
                }
            }


            // 初始化该卡片对象数组
            List<JSONObject> batchList = new ArrayList<>();

            try {
                for (String key : batchInit.keySet()) {
                    JSONObject batchJson = batchInit.getJSONObject(key);
                    JSONArray batchListType = batchInit.getJSONObject(key).getJSONArray("listType");

                    for (int i = 0; i < myModArray.size(); i++) {
                        String modArrayRef = myModArray.getJSONObject(i).getString("ref");
                        Integer modArrayLevel = myModArray.getJSONObject(i).getInteger("bcdLevel");

                        if (modArrayRef.equals(batchJson.getString("modRef"))
                                && (batchListType.contains(listType.substring(2)) || batchListType.contains("all"))
                                && modArrayLevel <= (batchJson.getInteger("bcdLevel"))) {
                            resultBatchObj.put(batchJson.getString("ref"), 0);
                        }
                    }
                }

                result.put("card", resultCardObj);
                result.put("batch", resultBatchObj);

                //Special treatment for lSProd/lBProd to add the logAuth into it
                if (listType.equals("lSProd") || listType.equals("lBProd")) {
                    for (String key : logInit.keySet()) {
                        JSONObject logJson = logInit.getJSONObject(key);
                        for (int i = 0; i < myModArray.size(); i++) {
                            String modArrayRef = myModArray.getJSONObject(i).getString("ref");
                            Integer modArrayLevel = myModArray.getJSONObject(i).getInteger("bcdLevel");

                            if (modArrayRef.equals(logJson.getString("modRef")) && modArrayLevel <= (logJson.getInteger("bcdLevel"))) {
                                resultLogObj.put(logJson.getString("ref"), 0);
                            }
                        }
                    }

                    result.put("log", resultLogObj);
                }

                System.out.println("result"+result.getJSONObject("log"));

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        return result;
    }
}