package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.client.WSFilterClient;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.AccountLoginService;
import com.cresign.login.utils.LoginResult;
import com.cresign.tools.advice.RetResult;
//import com.cresign.tools.advice.RsaUtilF;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.uuid.UUID19;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/7/25 10:14
 * ##version: 1.0
 */
@Service
@Slf4j
public class AccountLoginServiceImpl implements AccountLoginService {

    //降低 Autowired 检测的级别，将 Severity 的级别由之前的 error 改成 warning 或其它可以忽略的级别。
    @Autowired
    private WSFilterClient wsFilterClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LoginResult loginResult;

    @Autowired
    private RetResult retResult;

    @Autowired
    private StringRedisTemplate redisTemplate1;

//    @Resource
//    private CoupaUtil coupaUtil;

    public static final String SCANCODE_LOGINCOMP = "scancode:logincomp-";

    public static final String HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_LOGIN_COMP_T = "https://www.cresign.cn/qrCodeTest?qrType=logincomp&t=";


//    @Override
//    public ApiResponse setAppId(String appId,String id_U) {
//        User user = coupaUtil.getUserById(id_U, Collections.singletonList("rolex"));
//        if (null == user) {
//            throw new ErrorResponseException(HttpStatus.FORBIDDEN, "tang", "当前用户为空，不存在");
//        }
//        JSONObject rolex = user.getRolex();
//        JSONObject result = new JSONObject();
//        JSONArray err = new JSONArray();
//        rolex.getJSONObject("objComp").keySet().forEach(id_C -> {
//            if (id_C.equals("6076a1c7f3861e40c87fd294")) {
//                String aId = coupaUtil.getAssetId(id_C, "a-auth");
//                Asset asset = coupaUtil.getAssetById(aId, Collections.singletonList("flowControl"));
//                boolean isErr = true;
//                JSONObject flowControl = null;
//                JSONArray objData = null;
//                if (null == asset) {
//                    System.out.println("公司该资产为空");
//                    setErrJson(err,id_C,aId,"公司该资产为空");
//                    isErr = false;
//                } else {
//                    flowControl = asset.getFlowControl();
//                    if (null == flowControl) {
//                        setErrJson(err,id_C,aId,"公司该资产的flowControl卡片为空");
//                        isErr = false;
//                    } else {
//                        objData = flowControl.getJSONArray("objData");
//                        if (null == objData) {
//                            setErrJson(err,id_C,aId,"公司该资产的flowControl卡片内objData数据为空");
//                            isErr = false;
//                        }
//                    }
//                }
//                if (isErr) {
//                    boolean isErr2 = false;
//                    for (int i = 0; i < objData.size(); i++) {
//                        JSONObject dataZ = objData.getJSONObject(i);
//                        JSONArray objUser = dataZ.getJSONArray("objUser");
//                        if (null == objUser) {
//                            setErrJson(err,id_C,aId,"公司该资产的flowControl卡片内objData内的objUser数据为空:"+i);
//                        } else {
//                            for (int i1 = 0; i1 < objUser.size(); i1++) {
//                                JSONObject userZ = objUser.getJSONObject(i1);
//                                String id_UN = userZ.getString("id_U");
//                                if (null == id_UN) {
//                                    setErrJson(err,id_C,aId,"公司该资产的flowControl卡片内objData内的objUser数据内的id_U为空"+i);
//                                } else {
//                                    if (id_UN.equals(id_U)){
//                                        userZ.put("id_APP",appId);
//                                        objUser.set(i1,userZ);
//                                        isErr2 = true;
//                                        break;
//                                    }
//                                }
//                            }
//                            if (isErr2) {
//                                dataZ.put("objUser",objUser);
//                                objData.set(i,dataZ);
//                            }
//                        }
//                    }
//                    if (isErr2) {
//                        flowControl.put("objData",objData);
//                        // 定义存储flowControl字典
//                        JSONObject mapKey = new JSONObject();
//                        // 设置字段数据
//                        mapKey.put("flowControl",flowControl);
//                        coupaUtil.updateAssetByKeyAndListKeyVal("id",aId,mapKey);
//                    }
//                }
//            } else {
//                System.out.println("跳过公司:"+id_C);
//            }
//        });
//        if (err.size() > 0) {
//            result.put("type",1);
//            result.put("desc","内部出现错误");
//            result.put("errList",err);
//        } else {
//            result.put("type",0);
//            result.put("desc","操作成功!");
//        }
//        return retResult.ok(CodeEnum.OK.getCode(),result);
//    }
//
//    private void setErrJson(JSONArray err,String id_C,String id_A,String desc){
//        JSONObject reZ = new JSONObject();
//        reZ.put("id_C",id_C);
//        reZ.put("id_A",id_A);
//        reZ.put("desc",desc);
//        err.add(reZ);
//    }
//
//    @Override
//    public ApiResponse getKey(String qdKey) {
//        QdKey.setClient_Public_Key(qdKey);
////        return retResult.ok(CodeEnum.OK.getCode(), RsaUtilF.getPublicKey());
//        return retResult.ok(CodeEnum.OK.getCode(), "RsaUtilF.getPublicKey()");
//    }
//
//    @Override
//    public String getHdAndQdKey(String qdKey) {
//        QdKey.setClient_Public_Key(qdKey);
//        System.out.println("前端公钥:");
////        System.out.println(QdKey.getClient_Public_Key());
////        String publicKey = RsaUtilF.getPublicKey();
//        String publicKey = "RsaUtilF.getPublicKey()";
//        System.out.println("后端公钥:");
//        System.out.println(publicKey);
//        return publicKey;
//    }

    /**
     * 账号登录方法
     * ##Params: usn 用户名
     * ##Params: pwd 密码
     * ##Params: clientType 客户端
     * ##author: JackSon
     * ##updated: 2020/7/29 10:20
     * ##Return: java.lang.String
     */
    @Override
    public ApiResponse doNumberLogin(String clientType) {

        // 声明转换后结果存放
        JSONObject result = new JSONObject();
        //创建查询对象
//        Query query = new Query(Criteria.where("info.usn").is(usn));
        Query query = new Query(Criteria.where("_id").is("5f28bf314f65cc7dc2e60262"));
        // 创建Auth对象存放查询后的结果
        User user = mongoTemplate.findOne(query, User.class);

        // 初步判断用户名是否存在

                // 返回json数据给前端
                result = loginResult.allResult(user, clientType, "number");

                return retResult.ok(CodeEnum.OK.getCode(),result);
    }

    @Override
    public ApiResponse generateLoginCode(String id) {

        String token = UUID19.uuid();

        JSONObject qrObject = new JSONObject();

        qrObject.put("id", id);
        qrObject.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String keyName = SCANCODE_LOGINCOMP + token;

        qrObject.put("endTimeSec", "300");
        redisTemplate1.opsForHash().putAll(keyName, qrObject);
        redisTemplate1.expire(keyName, 300, TimeUnit.SECONDS);

        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_LOGIN_COMP_T + token;
        System.out.println(token);
        return retResult.ok(CodeEnum.OK.getCode(), url);

    }

    @Override
    public ApiResponse scanLoginCode( String token, String id_U) {

        String keyName = SCANCODE_LOGINCOMP + token;
        Boolean hasKey = redisTemplate1.hasKey(keyName);

        if (!hasKey) {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
LOGIN_CODE_OVERDUE.getCode(),null);
        }

        // 获取到整个hash
        Map<Object, Object> entries = redisTemplate1.opsForHash().entries(keyName);

        Query query = new Query(Criteria.where("_id").is(id_U));

        // 创建Auth对象存放查询后的结果
        User user = mongoTemplate.findOne(query, User.class);

        if (user == null)
        {
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
        }

        // 返回json数据给前端
        JSONObject result = loginResult.allResult(user, "web", "web");
        System.out.println("result = " + result);
        //判断redis里面有  webSocket_id键
        if (entries.containsKey("id")){
            //1.是否链接成功
            System.out.println(entries.get("id"));
                JSONObject reqJson = new JSONObject();
                reqJson.put("id",entries.get("id"));
                reqJson.put("infoData",JSON.toJSONString(result));
                //发送消息体
                wsFilterClient.sendLoginDesc(reqJson);

        }

        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


}