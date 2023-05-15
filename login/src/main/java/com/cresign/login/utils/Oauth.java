package com.cresign.login.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.jwt.JwtUtil;
import com.cresign.tools.pojo.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
    private Qt qt;

    @Autowired
    private DateUtils dt;

    @Autowired
    private JwtUtil jwtUtil;


    /**
     *##description:      根据uuid 生成token
     *                      clientType : 客户端类型
     *@return           token and refreshToken
     *@author           Kevin
     *@updated             2020/5/15 13:28
     */
    public  String setToken(User user, String cid, String grpU, String dep,  String clientType){

            String token = "";

            JSONObject dataSet = new JSONObject();
            dataSet.put("id_U", user.getId());
            dataSet.put("wrdNU", user.getInfo().getWrdN());
            dataSet.put("pic", user.getInfo().getPic());
            dataSet.put("id_C", cid);
            dataSet.put("grpU", grpU);
            dataSet.put("dep", dep);

            JSONArray modArray = new JSONArray();
            JSONObject modAuth = user.getRolex().getJSONObject("objComp").getJSONObject(cid).getJSONObject("modAuth");

            if (modAuth == null) {
                modArray.add("a-core-0");
            } else {
                for (String refKey : modAuth.keySet()) {
                    String tfin = modAuth.getJSONObject(refKey).getString("tfin");
                    String tnow = dt.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
                    if (tfin.equals(-1) || tfin.equals("-1"))
                    {
//                        modArray.add(refKey);
                        tfin = "2099/12/31 23:59:59";
                    }
                    if (dt.differentDays(tnow, tfin) > 0) {
                        modArray.add(refKey);
                    }
                }
            }

            //TODO KEV
            //dataSet has modAuth, but if I want to check this grpU can do whatever?
            //get redis.login:readwrite_auth - key: 1001_lSComp_1000_(batch/card/log).result = list of workable things
            //params: lType, grp, tokData.grpU, tokData.modArray + initData compare then good

            dataSet.put("modAuth", modArray);
            String uid = user.getId();

            System.out.println("setting Token" + dataSet.toJSONString());

            token = jwtUtil.createJWT(uid, clientType);

            qt.setRDSet(clientType + "Token", token, dataSet, 1800L);

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

}
