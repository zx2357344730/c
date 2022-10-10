package com.cresign.login.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.jwt.JwtUtil;
import com.cresign.tools.pojo.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ##class: Oauth
 * ##description: 设置 token，以及oauth等
 * @author jackson
 * @updated 2019-07-02 13:54
 **/
@Component
@Service
public class Oauth {

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private Qt qt;

    @Autowired
    private JwtUtil jwtUtil;

//    /**
//     *##description:      设置该用户的基本信息存入到redis中
//     *@param            user : 用户对象
//     *@return
//     *@author           Kevin
//     *@updated             2020/5/15 13:29
//     */
//    public void setOauth(User user) {
//
//        // 截取uid 用户编号
//        //List<Map<String, Object>> objCompList = (List<Map<String, Object>>) user.getRolex().get("objComp");
//        JSONArray objCompList = user.getRolex().getJSONArray("objComp");
//        // 生成hashMap 存入redis
//        //Map<String, Object> map = new HashMap<>();
//        JSONObject map = new JSONObject();
//        for (int i = 0; i < objCompList.size(); i++) {
//            map.put(objCompList.getJSONObject(i).getString("id_C"), objCompList.getJSONObject(i).get("lrefRole"));
//        }
//
//
//        redisTemplate0.opsForHash().putAll("oauth-" + user.getId(), map);
////        redisTemplate1.expire("oauth-" + UIDSubstring, 7, TimeUnit.DAYS);
//
//    }

    /**
     *##description:      根据uuid 生成token
     *                      clientType : 客户端类型
     *@return           token and refreshToken
     *@author           JackSon
     *@updated             2020/5/15 13:28
     */
    public  String setToken(User user, String cid, String grpU, String dep,  String clientType) {

        String token = "";

        JSONObject dataSet = new JSONObject();
        dataSet.put("id_U", user.getId());
        dataSet.put("wrdNU", user.getInfo().getWrdN());
        dataSet.put("pic", user.getInfo().getPic());
        dataSet.put("id_C", cid);
        dataSet.put("grpU", grpU);
        dataSet.put("dep", dep);

//        JSONObject moduleData = new JSONObject();
//        JSONArray objMod = user.getRolex().getJSONObject("objComp").getJSONObject(cid).getJSONArray("objMod");
//        for (int i  = 0; i < objMod.size(); i++)
//        {
//            if (objMod.getJSONObject(i).getInteger("bcdState").equals(1))
//            {
//                moduleData.put(objMod.getJSONObject(i).getString("ref"), objMod.getJSONObject(i).getInteger("bcdLevel"));
//            }
//        }
//        dataSet.put("modAuth",moduleData);
        JSONObject moduleDataX = new JSONObject();
        JSONObject modAuth = user.getRolex().getJSONObject("objComp").getJSONObject(cid).getJSONObject("modAuth");
        JSONObject test = new JSONObject();
            test.put("bcdState",1);
            test.put("tfin","2022/8/1");
            test.put("bcdLevel",1);
            test.put("ref","测试-为空专属");
        if (null == modAuth) {
            moduleDataX.put("core",test);
        } else {
            boolean isCore = false;
            for (String k : modAuth.keySet()) {
                moduleDataX.put(k, modAuth.getJSONObject(k));
                if ("core".equals(k)) {
                    isCore = true;
                }
            }
            if (!isCore) {
                moduleDataX.put("core",test);
            }
//            modAuth.keySet().forEach(k -> {
//                JSONObject modZ = modAuth.getJSONObject(k);
//                if (modZ.getInteger("bcdState").equals(1)) {
//                    moduleDataX.put(k, modZ);
//                }
//            });
        }
        dataSet.put("modAuth",moduleDataX);

        String uid = user.getId();

        System.out.println("setting Token"+ dataSet.toJSONString());


        token = jwtUtil.createJWT(uid, clientType);

        qt.setRDSet(clientType+"Token", token, dataSet, 1800L);


//        }

        return token;
    }

    /**
     *##description:      获取 refreshToken
     *@param
     *@return
     *@author           JackSon
     *@updated             2020/5/15 15:45
     */
    public  String setRefreshToken(String uid, String clientType) {

        String refreshToken = jwtUtil.createJWT(UUID.randomUUID().toString(), clientType);
        qt.setRDSet(clientType + "RefreshToken", refreshToken,uid, 604800L);

        return refreshToken;
    }


//
//    /**
//     *##description:      根据公司id生成 menu 和 role 存入到 redis 中
//     *@param            cid : 公司id
//     *@return
//     *@author           JackSon
//     *@updated             2020/5/15 13:30
//     */
//    public void setCompMenuAndRole(String cid) {
//
//        if (StringUtils.isNotEmpty(cid)) {
//
//            if (!redisTemplate0.opsForHash().hasKey("compMenu", cid)) {
//
//                String id_A = dbUtils.getId_A(cid, "a-auth");
//                Query query = new Query(new Criteria("_id").is(id_A));
//                Asset asset = mongoTemplate.findOne(query, Asset.class);
//
//                Map<String, Object> assetResult = new HashMap<>();
//
//                if (null != asset) {
//
//                    assetResult.put("menu", asset.getMenu().get("objMenu"));
//                    assetResult.put("role", asset.getRole().get("objRole"));
//
//                } else {
//
//                    RetResult.jsonResultEncrypt(HttpStatus.OK, LoginEnum.COMP_NOT_FOUND.getCode(), null);
//
//                }
//
//                redisTemplate0.opsForHash().put("compMenu", cid, JSONObject.toJSONString(assetResult));
//
//
//            }
//
//        }
//
//
//    }
}
