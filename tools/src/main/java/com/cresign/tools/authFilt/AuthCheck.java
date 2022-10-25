package com.cresign.tools.authFilt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.token.GetUserIdByToken;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * redis工具类
 * @author Rachel
 * @Data 2021/09/15
 **/
@Component
public class AuthCheck {

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private Qt qt;

    @Autowired
    private RetResult retResult;
    

    public JSONArray getUserSelectAuth(String id_U, String id_C, String grpU, String listType, String grp, String authType) {


        // check Company Auth Controller @ a-auth.role
        // 可以更新的部件的数组
        JSONArray enabledArray = new JSONArray();
        enabledArray.add("view");
        JSONObject result = new JSONObject();


//        JSONObject rolex = MongoUtils.getRolex(id_U, id_C, mongoTemplate);
//        if (rolex == null){
//            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, "042000", "no such rolex");
//        }
//        String user_grpU = rolex.getString("grpU");
        String user_grpU = grpU;
        String redisKey = user_grpU + "_" + listType + "_" + grp + "_" + authType;


//        if (redisTemplate0.opsForHash().hasKey("login:get_read_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType)) {
//            String val = redisTemplate0.opsForHash().get("login:get_read_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType).toString();
//            result = JSONObject.parseObject(val);
//            enabledArray = result.getJSONArray("result");
////            System.out.println("result Array from Redis"+enabledArray.toString());
//
//        }
        if (qt.hasRDHashItem("login:get_read_auth","compId-" + id_C,redisKey))
        {
            result = qt.getRDHash("login:get_read_auth","compId-" + id_C,redisKey);
            enabledArray = result.getJSONArray("result");
        }
        else {
//            String id_A = dbUtils.getId_A(id_C, "a-auth");
//            Query roleQ = new Query(new Criteria("_id").is(id_A));
//            roleQ.fields().include("role.objAuth."+user_grpU+"."+listType+"."+grp);
//            Asset asset = mongoTemplate.findOne(roleQ, Asset.class);
            
            Asset asset = qt.getConfig(id_C, "a-auth", "role.objData."+user_grpU+"."+listType+"."+grp);

            JSONObject grpJson = asset.getRole().getJSONObject("objData").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);
            if (ObjectUtils.isEmpty(grpJson)) {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN,  "042000", "can't find a-auth.grpU");
            }
            //            System.out.println("asset"+asset);
            JSONObject objAuth = grpJson.getJSONObject(authType);
            
            for (String authItem : objAuth.keySet())
            {
                if (objAuth.getInteger(authItem) >= 1 ) {
                    enabledArray.add(authItem);
                }
            }

//            for (int i = 0; i < objAuth.size(); i++) {
//
//                JSONObject authJson = objAuth.getJSONObject(i);
//                if (authJson.getInteger("auth") >= 1 ) {
//                    enabledArray.add(authJson.getString("ref"));
//                }
//            }
            result.put("result", enabledArray);
            result.put("user_grpU", user_grpU);

            qt.putRDHash("login:get_read_auth", "compId-" + id_C,
                    user_grpU + "_" + listType + "_" + grp + "_" + authType,result.toJSONString());

//            redisTemplate0.opsForHash().put("login:get_read_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType, result.toJSONString());
        }

        return enabledArray;

    }

    public boolean getUpdateAuth(JSONObject tokData, String listType, String grp, String authType, JSONArray checkArray) {

        // check Company Auth Controller @ a-auth.role
        // 可以更新的部件的数组
        JSONArray enabledArray = new JSONArray();
        enabledArray.add("view");
        JSONObject result = new JSONObject();
        String id_C = tokData.getString("id_C");
        String user_grpU = tokData.getString("grpU");
        String redisKey = user_grpU + "_" + listType + "_" + grp + "_" + authType;

//
//        if (redisTemplate0.opsForHash().hasKey("login:get_readwrite_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType)) {
//            String val = redisTemplate0.opsForHash().get("login:get_readwrite_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType).toString();
//            result = JSONObject.parseObject(val);
//            enabledArray = result.getJSONArray("result");
//
//        }
        if (qt.hasRDHashItem("login:get_readwrite_auth","compId-" + id_C,redisKey))
        {
            result = qt.getRDHash("login:get_readwrite_auth","compId-" + id_C,redisKey);
            enabledArray = result.getJSONArray("result");
        }
        else {
            Asset asset = qt.getConfig(id_C, "a-auth", "role.objData."+user_grpU+"."+listType+"."+grp);

            JSONObject grpJson = asset.getRole().getJSONObject("objData").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);

            if (ObjectUtils.isEmpty(grpJson)) {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN,  "042000", "can't find a-auth.grpU");
            }
            //            System.out.println("asset"+asset);
            JSONObject objAuth = grpJson.getJSONObject(authType);

            for (String authItem : objAuth.keySet())
            {
                if (objAuth.getInteger(authItem).equals(2)) {
                    enabledArray.add(authItem);
                }
            }


            result.put("result", enabledArray);
//            result.put("user_grpU", user_grpU);

            qt.putRDHash("login:get_readwrite_auth", "compId-" + id_C,
                    user_grpU + "_" + listType + "_" + grp + "_" + authType,result.toJSONString());

        }

        // 无权限
        checkArray.removeAll(enabledArray);
        if (checkArray.size() > 0)
            return false;
        else
            return true;
    }


    public ApiResponse getUserUpdateAuth(String id_U, String id_C, String listType, String grp, String authType, JSONArray checkArray) {

        // check Company Auth Controller @ a-auth.role
        // 可以更新的部件的数组
        JSONArray enabledArray = new JSONArray();
        enabledArray.add("view");
        JSONObject result = new JSONObject();

        User user = qt.getMDContent(id_U, "rolex.objComp."+id_C,User.class);
        JSONObject rolex = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);
        if (rolex == null){
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, "042000", null);
        }
        String user_grpU = rolex.getString("grpU");


        if (qt.hasRDHashItem("login:get_readwrite_auth","compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType)) {
//             String val = redisTemplate0.opsForHash().get("login:get_readwrite_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType).toString();
            result = qt.getRDHash("login:get_readwrite_auth","compId-" + id_C,user_grpU + "_" + listType + "_" + grp + "_" + authType);
            enabledArray = result.getJSONArray("result");

        } else {
//            String id_A = dbUtils.getId_A(id_C, "a-auth");
//            Query roleQ = new Query(new Criteria("_id").is(id_A));
//            roleQ.fields().include("role.objAuth."+user_grpU+"."+listType+"."+grp);
//            Asset asset = mongoTemplate.findOne(roleQ, Asset.class);
//            if (asset == null){
//                throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
//            }

            Asset asset = qt.getConfig(id_C, "a-auth", "role.objData."+user_grpU+"."+listType+"."+grp);


            JSONObject grpJson = asset.getRole().getJSONObject("objData").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);
//            System.out.println("asset"+asset);
//            JSONArray objAuth = grpJson.getJSONArray(authType);
//
//            for (int i = 0; i < objAuth.size(); i++) {
//
//                JSONObject authJson = objAuth.getJSONObject(i);
//                if (authJson.getInteger("auth").equals(2)) {
//                    enabledArray.add(authJson.getString("ref"));
//                }
//            }

            if (ObjectUtils.isEmpty(grpJson)) {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN,  "042000", "can't find a-auth.grpU");
            }
            //            System.out.println("asset"+asset);
            JSONObject objAuth = grpJson.getJSONObject(authType);

            for (String authItem : objAuth.keySet())
            {
                if (objAuth.getInteger(authItem).equals(2)) {
                    enabledArray.add(authItem);
                }
            }


            result.put("result", enabledArray);
            result.put("user_grpU", user_grpU);

            qt.putRDHash("login:get_readwrite_auth", "compId-" + id_C,
                    user_grpU + "_" + listType + "_" + grp + "_" + authType,result.toJSONString());


//            redisTemplate0.opsForHash().put("login:get_readwrite_auth:compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType, result.toJSONString());
        }
//        System.out.println("result Array finally"+ enabledArray.toString());

//        JSONArray checkArray2 = (JSONArray) enabledArray.clone();
        // 无权限
        checkArray.removeAll(enabledArray);
        if (checkArray.size() > 0)
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);

        // Here need to start to check objMod


        // Check Module Auth
//        JSONArray moduleArray = new JSONArray();

//        for (Object objMod : rolex.getJSONArray("objMod")) {
//
//            JSONObject objModJson = (JSONObject) JSON.toJSON(objMod);
//
//            String module2 = objModJson.getString("module");
//            Integer bcdLevel = objModJson.getInteger("bcdlevel");
//            // I know what module I can use now, so I go initData to get the list of card etc
//            //moduleArray.add( array from initData)
//        }
//
//        checkArray2.removeAll(moduleArray);
//        if (checkArray2.size() > 0)
//            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

//    public ApiResponse authCheckLogType(String id_C, String id_U, String grpU, String logType)
//    {
//        // return array of all the toolset he can do in this "logType"
//        // Array is filtered by Module Equip
//        // Use timeflow to set deadDate for Module, 7 days, 3 days, 1 days alert + stop,
//        // when stop, delete redis + delete rolex
//        return retResult.ok(CodeEnum.OK.getCode(), "done");
//    }

}
