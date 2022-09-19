package com.cresign.login.utils;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.Comp;
import com.cresign.tools.pojo.po.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * ##description: 登录返回工具类
 * @author JackSon
 * @updated 2020/7/25 10:54
 * @ver 1.0
 */
@Component
public class LoginResult {


    @Autowired
    private Oauth oauth;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate1;


    /**
     * 返回allList 所有信息方法（账号登录，以及微信登录可用）
     *
     * @param user       用户对象
     * @param clientType 客户端类型
     * @param loginType  登录类型
     * @author JackSon
     * @updated 2020/7/25 11:00
     * @return java.lang.Object
     */
    public JSONObject allResult(User user, String clientType, String loginType) {

        //oauth.setOauth(user);
        String token = "";
        String newAssignRFToken = "";
        String def_C = user.getInfo().getDef_C().toString();


        /*
           infoData --------------
        */
        // new一个 infoData 用来存储基本信息
        JSONObject infoData = new JSONObject();

        if (StringUtils.isNotEmpty(user.getId()))
            infoData.put("id_U", user.getId());

        infoData.put("grpU",user.getRolex().getJSONObject("objComp").getJSONObject(def_C).getString("grpU"));
        infoData.put("dep",user.getRolex().getJSONObject("objComp").getJSONObject(def_C).getString("dep"));

        infoData.put("id_C", def_C);


        if (!"refreshToken".equals(loginType)) {
            token = oauth.setToken(
                    user,
                    def_C,
                    user.getRolex().getJSONObject("objComp").getJSONObject(def_C).getString("grpU"),
                    user.getRolex().getJSONObject("objComp").getJSONObject(def_C).getString("dep"),
                    clientType);

            newAssignRFToken = oauth.setRefreshToken(user.getId(), clientType);
        }

        if (null != user.getInfo().getLCRdef())
            infoData.put("defNG", user.getInfo().getLNGdef());

        if (null != user.getInfo().getId_WX())
            infoData.put("id_WX", user.getInfo().getId_WX());

        if (null != user.getInfo().getId_APP())
            infoData.put("id_APP", user.getInfo().getId_APP());

        if (null != user.getInfo().getMbn())
            infoData.put("mbn", user.getInfo().getMbn());

        if (null != user.getInfo().getId_AUN())
            infoData.put("id_AUN", user.getInfo().getId_AUN());

        if (null != user.getInfo().getPic())
            infoData.put("user_pic", user.getInfo().getPic());

        infoData.put("user_wrdN", user.getInfo().getWrdN()); // 用户中文名

        infoData.put("token", token);               // 存储token

        infoData.put("refreshToken", newAssignRFToken);  // 存储 refreshToken


        // 新建 data map 用来获取所有的数据信息
        JSONObject data = new JSONObject();

        Query queryComp = new Query(new Criteria("_id").is(def_C));
        queryComp.fields().include("info");
        Comp comp = mongoTemplate.findOne(queryComp, Comp.class);

        if (null != comp) {

            if (null != comp.getInfo()) {
                infoData.put("compName", comp.getInfo().getWrdN());
                infoData.put("compPic", comp.getInfo().getPic());
                infoData.put("compRef", comp.getInfo().getRef());
                infoData.put("compId_CP", comp.getInfo().getId_CP());
            } else
            {
                infoData.put("compName", "unable to get Comp");

            }

        }

        // infoData
        data.put("infoData", infoData);

        // 返回结果
        return data;

    }

}