package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.SetAuthService;
import com.cresign.login.utils.Oauth;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.es.lSUser;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Comp;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.pojo.po.userCard.UserInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class SetAuthServicelmpl implements SetAuthService {

//    @Autowired
//    private MongoTemplate mongoTemplate;

    @Autowired
    private RetResult retResult;

    @Autowired
    private AuthCheck authCheck;

    @Autowired
    private Oauth oauth;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private Qt qt;

//    @Autowired
//    private AuthFilterService authFilterService;


    @Override
    public ApiResponse getMyBatchList(String id_U, String id_C, String listType, JSONArray grp) {

        User user = qt.getMDContent(id_U, "rolex.objComp."+id_C,User.class);
        JSONObject rolex = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);

        if (rolex == null){
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.LOGIN_NOTFOUND_USER.getCode(), null);
        }
        // 用户的role权限
        String grpU = rolex.getString("grpU");

//        Query batchQ = new Query(
//                new Criteria("info.id_C").is(id_C)
//                        .and("info.ref").is("a-auth"));
//        //batchQ.fields().include("role.objAuth." + grpU + "." + listType + "." + grp + ".batch");
//        batchQ.fields().include("role.objData." + grpU + "." + listType);
//
//        Asset asset = mongoTemplate.findOne(batchQ, Asset.class);
//        Asset asset = qt.getConfig(id_C,"a-auth","role");
        Asset asset = qt.getConfig(id_C, "a-auth", "role.objData." + grpU + "." + listType);
        //JSONObject roleJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(batchQ, Asset.class));

        // 没有设置职位权限
        if (null == asset.getRole().getJSONObject("objData").getJSONObject(grpU))
//        if (!qt.isNotNull(asset.getRole(), qt.setArray("role.objData."+ grpU + "."+listType)))
        {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.ROLE_NOT_SET.getCode(), null);
        }
        JSONArray batchArray = new JSONArray();

        for (int i = 0; i<grp.size();i++) {

            try {
                JSONObject tempBatch = asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp.getString(i)).getJSONObject("batch");
                for (String batchItem : tempBatch.keySet()) {
                    if (!batchArray.contains(batchItem)) {
                        if (tempBatch.getInteger(batchItem).equals(2)) {
                            batchArray.add(batchItem);
                        }
                    }
                }
            } catch(Exception e) {

            }
        }
        System.out.println("batch "+batchArray.toJSONString());

//
//        if (ObjectUtils.isEmpty(batchArray)) {
//            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.COMP_NOT_FOUND.getCode(), null);
//        }

//        // 最终返回batch
//        JSONArray result = new JSONArray();
//        for (int i = 0; i < batchArray.size(); i++) {
//
//            JSONObject batchJson = batchArray.getJSONObject(i);
//
//            // 判断是可读，或者可写读才能拿到
//            if (batchJson.getInteger("auth") == 2) {
//                result.add(batchJson.getString("ref"));
//            }
//        }
//        System.out.println("batch "+ JSON.toJSONString(result));


        return retResult.ok(CodeEnum.OK.getCode(), JSON.toJSONString(batchArray));

    }

    @Override
    public ApiResponse getMyUpdateCardList(String id_U, String id_C, String listType, String grp) {

        User user = qt.getMDContent(id_U, "rolex.objComp."+id_C,User.class);
        JSONObject rolex = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);

        if (rolex == null){
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.LOGIN_NOTFOUND_USER.getCode(), null);
        }
        // 用户的role权限
        String grpU = rolex.getString("grpU");

        Asset asset = qt.getConfig(id_C, "a-auth", "role.objData." + grpU + "." + listType + "." + grp + ".card");

        //JSONObject roleJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(batchQ, Asset.class));

        // 没有设置职位权限
        if (null == asset.getRole().getJSONObject("objData").getJSONObject(grpU)) {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.ROLE_NOT_SET.getCode(), null);
        }

        // 返回的card列表数据
        JSONObject cardArray = asset.getRole().getJSONObject("objData").getJSONObject(grpU).getJSONObject(listType).getJSONObject(grp).getJSONObject("card");

        if (ObjectUtils.isEmpty(cardArray)) {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.COMP_NOT_FOUND.getCode(), null);
        }

        // 最终返回batch
        JSONArray result = new JSONArray();

        for (String cardItem : cardArray.keySet())
        {
            if (cardArray.getInteger(cardItem).equals(2))
            {
                if (listType == "lBUser" && cardItem.endsWith("x"))
                {
                    result.add(cardItem);
                } else if (listType == "lSProd" && !cardItem.endsWith("x")) {
                    result.add(cardItem);
                } else if (listType != "lSProd" && listType != "lBUser" ) {
                    result.add(cardItem);
                }
            }
        }

//        for (int i = 0; i < cardArray.size(); i++) {
//
//            JSONObject cardJson = cardArray.getJSONObject(i);
//
//            // 判断是可写读才能拿到
//            // IF lBUser, only x (set by vue)
//            // IF lSProd, only non-x (set by vue)
//            if (cardJson.getInteger("auth") == 2) {
//                if (listType == "lBUser" && cardJson.getString("ref").endsWith("x"))
//                {
//                    result.add(cardJson.getString("ref"));
//                } else if (listType == "lSProd" && !cardJson.getString("ref").endsWith("x")) {
//                    result.add(cardJson.getString("ref"));
//                } else if (listType != "lSProd" && listType != "lBUser" ) {
//                    result.add(cardJson.getString("ref"));
//                }
//            }
//
//        }
        // need a id_Check
        // KEV: IF lBProd & bcdNet == 1, real comp, Only X
        // IF lBProd/lSBComp & bcdNet == 1, real comp, only X
        // IF lBProd/lSBComp & bcdNet == 0/2, real comp, only non-X

        // judgeComp id_C, id_CB


        return retResult.ok(CodeEnum.OK.getCode(), result);

    }



    @Override
//    @Transactional(rollbackFor = Exception.class, noRollbackFor = ResponseException.class)
    public ApiResponse switchComp(String id_U, String id_C, String clientType) {

//         /*
//            设置 用户下次登录的公司 def_C
//         */
//
//        // 通过id_U查询该用户
//
//        User user = qt.getMDContent(id_U, Arrays.asList("rolex.objComp."+ id_C, "info"), User.class);
//
//        //  here delete old Token,
//        JSONObject userRolex = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);
//        // if user actually exists in this company
//        if (userRolex != null) {
//            oauth.setToken(user, id_C, userRolex.getString("grpU"),
//                    userRolex.getString("dep"), clientType);
//
//            Comp comp = qt.getMDContent(id_C, "info", Comp.class);
//            JSONObject updateData = new JSONObject();
//
//            if (!comp.getInfo().getPic().equals(userRolex.getString("picC")) ||
//                    !comp.getInfo().getWrdN().equals(userRolex.getJSONObject("wrdNC")))
//            {
//                updateData.put("rolex.objComp."+id_C+".wrdNC", comp.getInfo().getWrdN());
//                updateData.put("rolex.objComp."+id_C+".picC", comp.getInfo().getPic());
//
//            }
//
//            // 4. update def_C
////            Query query2 = new Query(new Criteria("_id").is(id_U));
////            query2.fields().include("info");
//            updateData.put("info.def_C", id_C);
//
//
////            mongoTemplate.updateFirst(query2, updateQuery, User.class);
//
//            qt.setMDContent(id_U, updateData, User.class);
//
//            JSONObject result = new JSONObject();
//            result.put("grpU", userRolex.getString("grpU"));
//            result.put("dep", userRolex.getString("dep"));
//            result.put("picC", comp.getInfo().getPic());
////            result.put("pic", comp.getInfo().getString("pic"));
//            result.put("wrdNC", comp.getInfo().getWrdN());
////            result.put("wrdNU", comp.getInfo().getJSONObject("wrdN"));
//            result.put("id_C", id_C);
//
//            return retResult.ok(CodeEnum.OK.getCode(), result);
//        } else {
//            JSONArray es = qt.getES("lSUser", qt.setESFilt("id_U", id_U, "id_C", id_C));
//            UserInfo info = user.getInfo();
//            Comp compInfo = qt.getMDContent(id_C, "info", Comp.class);
//            if (null == compInfo || null == compInfo.getInfo()) {
//                throw new ErrorResponseException(HttpStatus.OK, LoginEnum.COMP_NOT_FOUND.getCode(), null);
//            }
//            String thisGrpU;
//            if (null == es || es.size() == 0) {
//                thisGrpU = "1099";
//                lSUser lsUser = new lSUser(id_U,id_C, info.getWrdN()
//                        ,info.getWrdNReal(),info.getWrddesc(),thisGrpU,info.getMbn(),"", info.getPic());
//                qt.addES("lSUser",lsUser);
//                addRoleCompInfo(id_C, id_U,thisGrpU,compInfo.getInfo().getPic(),compInfo.getInfo().getWrdN());
//            } else {
//                JSONObject jsonObject = es.getJSONObject(0);
//                thisGrpU = jsonObject.getString("grpU");
//                addRoleCompInfo(id_C, id_U,thisGrpU
//                        ,compInfo.getInfo().getPic(),compInfo.getInfo().getWrdN());
//            }
//
////            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.COMP_NOT_FOUND.getCode(), null);
//            JSONObject result = new JSONObject();
//            result.put("grpU", thisGrpU);
//            result.put("dep", "1000");
//            result.put("picC", compInfo.getInfo().getPic());
////            result.put("pic", comp.getInfo().getString("pic"));
//            result.put("wrdNC", compInfo.getInfo().getWrdN());
////            result.put("wrdNU", comp.getInfo().getJSONObject("wrdN"));
//            result.put("id_C", id_C);
//            return retResult.ok(CodeEnum.OK.getCode(), result);
//        }

        /*
            设置 用户下次登录的公司 def_C
         */
        // 通过id_U查询该用户
        User user = qt.getMDContent(id_U, Arrays.asList("rolex.objComp."+ id_C, "info"), User.class);
        //  here delete old Token,
        JSONObject userRolex = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);
        // if user actually exists in this company
        if (userRolex != null) {
            oauth.setToken(user, id_C, userRolex.getString("grpU"),
                    userRolex.getString("dep"), clientType);
            Comp comp = qt.getMDContent(id_C, "info", Comp.class);
            JSONObject updateData = new JSONObject();
            if (!comp.getInfo().getPic().equals(userRolex.getString("picC")) ||
                    !comp.getInfo().getWrdN().equals(userRolex.getJSONObject("wrdNC")))
            {
                updateData.put("rolex.objComp."+id_C+".wrdNC", comp.getInfo().getWrdN());
                updateData.put("rolex.objComp."+id_C+".picC", comp.getInfo().getPic());

            }

            // 4. update def_C
//            Query query2 = new Query(new Criteria("_id").is(id_U));
//            query2.fields().include("info");
            updateData.put("info.def_C", id_C);
//            mongoTemplate.updateFirst(query2, updateQuery, User.class);

            qt.setMDContent(id_U, updateData, User.class);

            JSONObject result = new JSONObject();
            result.put("grpU", userRolex.getString("grpU"));
            result.put("dep", userRolex.getString("dep"));
            result.put("picC", comp.getInfo().getPic());
//            result.put("pic", comp.getInfo().getString("pic"));
            result.put("wrdNC", comp.getInfo().getWrdN());
//            result.put("wrdNU", comp.getInfo().getJSONObject("wrdN"));
            result.put("id_C", id_C);

            return retResult.ok(CodeEnum.OK.getCode(), result);
        } else {
            JSONArray es = qt.getES("lSUser", qt.setESFilt("id_U", id_U, "id_C", id_C));
            UserInfo info = user.getInfo();
            Comp compInfo = qt.getMDContent(id_C, "info", Comp.class);
            Asset asset = qt.getConfig(id_C, "a-auth", "role");
            if (null == asset || null == asset.getRole()) {
                throw new ErrorResponseException(HttpStatus.OK, LoginEnum.ASSET_NOT_FOUND.getCode(), null);
            }
            String thisGrpU;
            if (null == compInfo || null == compInfo.getInfo()) {
                throw new ErrorResponseException(HttpStatus.OK, LoginEnum.COMP_NOT_FOUND.getCode(), null);
            }
            if (null == es || es.size() == 0) {
                thisGrpU = "1099";
                lSUser lsUser = new lSUser(id_U,id_C, info.getWrdN()
                        ,info.getWrdNReal(),info.getWrddesc(),thisGrpU,info.getMbn(),"", info.getPic());
                qt.addES("lSUser",lsUser);
                addRoleCompInfo(id_C, id_U,thisGrpU,compInfo.getInfo().getPic(),compInfo.getInfo().getWrdN());
            } else {
                JSONObject jsonObject = es.getJSONObject(0);
                thisGrpU = jsonObject.getString("grpU");
                addRoleCompInfo(id_C, id_U,thisGrpU
                        ,compInfo.getInfo().getPic(),compInfo.getInfo().getWrdN());
            }

//            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.COMP_NOT_FOUND.getCode(), null);
            JSONObject result = new JSONObject();
            result.put("grpU", thisGrpU);
            result.put("dep", "1000");
            result.put("picC", compInfo.getInfo().getPic());
//            result.put("pic", comp.getInfo().getString("pic"));
            result.put("wrdNC", compInfo.getInfo().getWrdN());
//            result.put("wrdNU", comp.getInfo().getJSONObject("wrdN"));
            result.put("id_C", id_C);
            return retResult.ok(CodeEnum.OK.getCode(), result);
        }

    }

    @Override
//    @Transactional(rollbackFor = Exception.class, noRollbackFor = ResponseException.class)
    public ApiResponse setAUN(String id_U, String id_AUN) {

         /*
            设置 用户下次登录的公司 def_C
         */
//        Query userQuery = new Query(new Criteria("id").is(id_U));
//        mongoTemplate.updateFirst(userQuery, new Update().set("info.id_AUN", id_AUN), User.class);
//        qt.setES("lBUser", qt.setESFilt("id_U", id_U), qt.setJson("id_AUN", id_AUN));
        qt.setMDContent(id_U, qt.setJson("info.id_AUN", id_AUN), User.class);
        qt.setES("lNUser", qt.setESFilt("id_U", id_U), qt.setJson("id_AUN", id_AUN));
        return retResult.ok(CodeEnum.OK.getCode(), null);

    }

    /**
     * 更新User的Info卡片的pic字段
     * @param id_U	用户编号
     * @param pic	用户头像
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/21
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse setUserPic(String id_U, String pic) {
        qt.setMDContent(id_U, qt.setJson("info.pic", pic), User.class);
        return retResult.ok(CodeEnum.OK.getCode(), "修改成功");
    }

    /**
     * 更新用户cid，推送id方法
     * @param id_U 用户id
     * @param cid	推送id
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/31
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse setUserCid(String id_U,String cid) {
        qt.setES("lNUser",qt.setESFilt("id_U",id_U),qt.setJson("id_APP",cid));
        qt.setMDContent(id_U, qt.setJson("info.id_APP", cid), User.class);
        return retResult.ok(CodeEnum.OK.getCode(), "修改成功");
    }

    @Override
    public ApiResponse getUserCid(String id_U) {
        JSONArray es = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
        if (null != es && es.size() > 0) {
            JSONObject esInfo = es.getJSONObject(0);
            return retResult.ok(CodeEnum.OK.getCode(), esInfo.getString("id_APP"));
        }
        throw new ErrorResponseException(HttpStatus.OK, LoginEnum.LOGIN_NOTFOUND_USER.getCode(), null);
    }


    @Override
    public ApiResponse getMySwitchComp(String id_U, String lang) {

        User one = qt.getMDContent(id_U, "rolex.objComp", User.class);

        if (one == null)
        {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.LOGIN_NOTFOUND_USER.getCode(), null);
        }
        // 最终返回数据
        JSONArray result = new JSONArray();
        JSONObject compList = one.getRolex().getJSONObject("objComp");

        for (String cid :compList.keySet()) {
            System.out.println("For ."+ cid);

            if (compList.getJSONObject(cid).getString("picC") == null) {
//                Query compQ = new Query(
//                        new Criteria("_id").is(cid));
//                compQ.fields().include("info");
//                Comp comp = mongoTemplate.findOne(compQ, Comp.class);

                Comp comp = qt.getMDContent(cid, "info", Comp.class);
                JSONObject newDocs = new JSONObject();
                newDocs.put("compId", comp.getId());
                newDocs.put("wrdN", comp.getInfo().getWrdN());
                newDocs.put("pic", comp.getInfo().getPic());
                result.add(newDocs);
//                Update updateQuery = new Update();
//                updateQuery.set("rolex.objComp."+cid+".wrdNC", comp.getInfo().getWrdN());
//                updateQuery.set("rolex.objComp."+cid+".picC", comp.getInfo().getPic());
//                Query query2 = new Query(new Criteria("_id").is(id_U));
//                query2.fields().include("rolex.objComp."+cid);
//                mongoTemplate.updateFirst(query2, qt.setJson("rolex.objComp."+cid+".wrdNC", comp.getInfo().getWrdN(),
//                        "rolex.objComp."+cid+".picC", comp.getInfo().getPic()), User.class);
                qt.setMDContent(id_U, qt.setJson("rolex.objComp."+cid+".wrdNC", comp.getInfo().getWrdN(),
                        "rolex.objComp."+cid+".picC", comp.getInfo().getPic()), User.class);

            } else {
                System.out.println("Adding List");

                JSONObject newDocs = new JSONObject();
                newDocs.put("compId", cid);
                newDocs.put("wrdN", compList.getJSONObject(cid).getJSONObject("wrdNC"));
                newDocs.put("pic", compList.getJSONObject(cid).getString("picC"));
                result.add(newDocs);
            }
        }

        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    public void addRoleCompInfo(String id_C, String id_U,String grpU,String picC,JSONObject wrdN){
        JSONObject objCompInfo = new JSONObject();
        objCompInfo.put("grpU",grpU);
        objCompInfo.put("id_C",id_C);
        objCompInfo.put("dep","1000");
        objCompInfo.put("picC",picC);
        objCompInfo.put("wrdNC",wrdN);
        JSONObject modAuth = new JSONObject();
        JSONObject core = new JSONObject();
        core.put("tfin","-1");
        core.put("ref","a-core-0");
        core.put("mod","a-core");
        core.put("bcdState","1");
        core.put("bcdLevel","0");
        modAuth.put("a-core-0",core);
        objCompInfo.put("modAuth",modAuth);
        qt.setMDContent(id_U,qt.setJson("rolex.objComp."+id_C,objCompInfo), User.class);
    }

//    @Override
//    public ApiResponse getMySwitchComp1(String id_U, String lang) {
//        Query userQ = new Query(
//                new Criteria("_id").is(id_U));
//
//        userQ.fields().include("rolex.objComp");
//        User one = mongoTemplate.findOne(userQ, User.class);
//
//        return retResult.ok(CodeEnum.OK.getCode(), one.getRolex().getJSONObject("objComp").keySet());
//    }

//
//    @Override
//    public ApiResponse updateDefCard(String id_U, String id_C, JSONObject defData) {
//
//
//        authCheck.getUserUpdateAuth(id_U, id_C, "lSAsset", "1003", "card", new JSONArray().fluentAdd("def"));
//
//            Query menuQuery = new Query(
//                    new Criteria("info.id_C").is(id_C)
//                            .and("info.ref").is("a-auth"));
//            menuQuery.fields().include("def");
//            Update mainMenuUd = new Update();
//            mainMenuUd.set("def", defData);
//            mongoTemplate.updateFirst(menuQuery, mainMenuUd, Asset.class);
//
//        return retResult.ok(CodeEnum.OK.getCode(), "");
//
//    }


}
