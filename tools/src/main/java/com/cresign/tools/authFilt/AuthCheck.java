package com.cresign.tools.authFilt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.User;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Qt qt;

    @Autowired
    private RetResult retResult;
    

    public JSONArray getUserSelectAuth(String id_U, String id_C, String grpU, String listType, String grp, String authType) {


        // check Company Auth Controller @ a-auth.role
        // 可以更新的部件的数组
        JSONArray enabledArray = new JSONArray();
        enabledArray.add("view");
        JSONObject result = new JSONObject();


        String user_grpU = grpU;
        String redisKey = user_grpU + "_" + listType + "_" + grp + "_" + authType;

        if (qt.hasRDHashItem("login:get_read_auth","compId-" + id_C,redisKey))
        {
            result = qt.getRDHash("login:get_read_auth","compId-" + id_C,redisKey);
            enabledArray = result.getJSONArray("result");
        }
        else {
            
            Asset asset = qt.getConfig(id_C, "a-auth", "role.objData."+user_grpU+"."+ listType + "."+grp);

            JSONObject grpJson = new JSONObject();
            try {
                grpJson = asset.getRole().getJSONObject("objData").getJSONObject(user_grpU).getJSONObject(listType).getJSONObject(grp);
            } catch (Exception e)
            {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN,  "042000", "can't find a-auth.grpU");
            }

            if (ObjectUtils.isEmpty(grpJson)) {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN,  "042000", "can't find a-auth.grpU");
            }
            
            JSONObject objAuth = grpJson.getJSONObject(authType);
            
            for (String authItem : objAuth.keySet())
            {
                if (objAuth.getInteger(authItem) >= 1 ) {
                    enabledArray.add(authItem);
                }
            }

            result.put("result", enabledArray);
            result.put("user_grpU", user_grpU);

            qt.putRDHash("login:get_read_auth", "compId-" + id_C,
                    user_grpU + "_" + listType + "_" + grp + "_" + authType,result.toJSONString());

        }

        return enabledArray;

    }

    public void getUpdateAuth(JSONObject tokData, String listType, String grp, String authType, JSONArray checkArray) {

        // check Company Auth Controller @ a-auth.role
        // 可以更新的部件的数组
        JSONArray enabledArray = new JSONArray();
        enabledArray.add("view");
        JSONObject result = new JSONObject();
        String id_C = tokData.getString("id_C");
        String user_grpU = tokData.getString("grpU");
        String redisKey = user_grpU + "_" + listType + "_" + grp + "_" + authType;

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

            qt.putRDHash("login:get_readwrite_auth", "compId-" + id_C,
                    user_grpU + "_" + listType + "_" + grp + "_" + authType,result.toJSONString());

        }
        // 无权限
        checkArray.removeAll(enabledArray);

        if (checkArray.size() > 0)
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, "042000", "can't find a-auth.grpU");

//TODO KEV
//        // from initJava, get cards' mod and bcdLevel
//        InitJava init = qt.getInitData();
//        Boolean alright;
//        JSONArray addedMods = tokData.getJSONArray("modAuth");
//        for (int j = 0; j < checkArray.size(); j++) {
//            alright = false;
//            JSONObject mod = new JSONObject();
//            if (authType.equals("card"))
//            {
//                mod = init.getCardInit().getJSONObject(checkArray.getString(j));
//            } else if (authType.equals("batch")) {
//                init.getBatchInit().getJSONObject(checkArray.getString(j));
//            }
//            for (int i = 0; i < addedMods.size(); i++) {
//                String[] modName = addedMods.getString(i).split("-");
//                if (modName[1].equals(mod.getString("modRef"))) {
//                    if (Integer.parseInt(modName[2]) >= mod.getInteger("bcdLevel")) {
//                        alright = true;
//                    } else {
//                        throw new ErrorResponseException(HttpStatus.FORBIDDEN, "041111", "mod level not high enough");
//                    }
//                }
//            }
//            if (!alright)
//                throw new ErrorResponseException(HttpStatus.FORBIDDEN, "041111", "mod not installed");
//        }



        // rolex objMod split
        // check if split[1] == mod
        // check if split[2] >= bcdLevel
        // if NOT ok, remove that from result


    }


    public ApiResponse getUserUpdateAuth(String id_U, String id_C, String listType, String grp, String authType, JSONArray checkArray) {

        // check Company Auth Controller @ a-auth.role
        // 可以更新的部件的数组
        JSONArray enabledArray = new JSONArray();
        enabledArray.add("view");
        JSONObject result = new JSONObject();

        User user = qt.getMDContent(id_U, "rolex."+id_C,User.class);
        JSONObject rolex = user.getRolex().getJSONObject(id_C);
        if (rolex == null){
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, "042000", null);
        }
        String user_grpU = rolex.getString("grpU");


        if (qt.hasRDHashItem("login:get_readwrite_auth","compId-" + id_C, user_grpU + "_" + listType + "_" + grp + "_" + authType)) {
            result = qt.getRDHash("login:get_readwrite_auth","compId-" + id_C,user_grpU + "_" + listType + "_" + grp + "_" + authType);
            enabledArray = result.getJSONArray("result");

        } else {

            Asset asset = qt.getConfig(id_C, "a-auth", "role.objData." + user_grpU + "." + listType + "." + grp);


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
                throw new ErrorResponseException(HttpStatus.FORBIDDEN, "042000", "can't find a-auth.grpU");
            }
            JSONObject objAuth = grpJson.getJSONObject(authType);

            for (String authItem : objAuth.keySet()) {
                if (objAuth.getInteger(authItem).equals(2)) {
                    enabledArray.add(authItem);
                }
            }

            result.put("result", enabledArray);
            result.put("user_grpU", user_grpU);

            qt.putRDHash("login:get_readwrite_auth", "compId-" + id_C,
                    user_grpU + "_" + listType + "_" + grp + "_" + authType, result.toJSONString());

            // 无权限
            checkArray.removeAll(enabledArray);
            if (checkArray.size() > 0)
                throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);

        }
        // Here need to start to check objMod




//        if (checkArray.size() > 0)
//            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);


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
