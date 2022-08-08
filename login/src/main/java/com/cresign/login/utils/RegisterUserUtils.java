package com.cresign.login.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.es.lBUser;
import com.cresign.tools.pojo.po.Comp;
import com.cresign.tools.pojo.po.InitJava;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.pojo.po.userCard.UserInfo;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * ##description: 注册用户工具类
 * ##author: JackSon
 * ##updated: 2020/9/15 15:03
 * ##version: 1.0
 */
@Component
public class RegisterUserUtils {


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 注入redis数据库下标1模板
     */
    @Resource
    private StringRedisTemplate redisTemplate1;

    private static final String QD_Key = "qdKey";

    @Transactional(noRollbackFor = ResponseException.class)
    public String registerUser(Map<String, Object> info)  {

        //try {

            Query initQuery = new Query(new Criteria("_id").is("cn_java"));
            initQuery.fields().include("newUser");
            InitJava initJava = mongoTemplate.findOne(initQuery, InitJava.class);

            // objectId
            String addID = MongoUtils.GetObjectId();

            User addUser = new User();

            addUser.setId(addID);
            addUser.setRolex(initJava.getNewUser().getJSONObject("rolex"));

            UserInfo infoJson =  JSONObject.parseObject(JSON.toJSONString(initJava.getNewUser().getJSONObject("info")), UserInfo.class);
            addUser.setInfo(infoJson);
            addUser.setView(initJava.getNewUser().getJSONArray("view"));
            mongoTemplate.insert(addUser);

            // 查询公司
            Query compQuery = new Query(new Criteria("_id").is("5f2a2502425e1b07946f52e9"));
            compQuery.fields().include("info");
            Comp comp = mongoTemplate.findOne(compQuery, Comp.class);
            //JSONObject compOne = (JSONObject) JSON.toJSON(mongoTemplate.findOne(compQuery, Comp.class));

            lBUser addLBUser = new lBUser();
            addLBUser.setId_CB(comp.getId());
            addLBUser.setId_U(addID);
            addLBUser.setGrpU("1000");
            addLBUser.setPic(info.get("pic").toString());
            addLBUser.setTmd(DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
            addLBUser.setTmk(DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
            addLBUser.setWrdN(infoJson.getWrdN());
            addLBUser.setWrdNCB(comp.getInfo().getWrdN());

            IndexRequest indexRequest = new IndexRequest("lbuser");
            indexRequest.source(JSON.toJSONString(addLBUser), XContentType.JSON);
            try {
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

            } catch (IOException e) {

            }
            return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), null);


//        } catch (RuntimeException e) {
//
//            e.printStackTrace();
//
//        }

        //return "";

    }



//    @Transactional(noRollbackFor = ResponseException.class)
//    public String registerUser(JSONObject info) {
//
//        try {
//            // objectId
//            String addID = MongoUtils.GetObjectId();
//
//            User addUser = new User();
//
//            info.put("def_C", "5f2a2502425e1b07946f52e9");
//
//            /*
//               添加User表
//             */
//
//            // Rolex
//            //Map<String, Object> rolex = new HashMap<>();
//            JSONObject rolex = new JSONObject(2);
//            // objMod 拥有的模块
//            JSONArray objMod = new JSONArray(2);
//            JSONObject mod = new JSONObject(4);
//            mod.put("ref", "a-core");
//            mod.put("bcdState", 1);
//            mod.put("bcdLevel", 1);
//            mod.put("tfin", "-1");
//            objMod.add(mod);
//
//            //Map<String, Object> rolexMap = new HashMap<>();
//            JSONObject rolexMap = new JSONObject(4);
//            rolexMap.put("id_C", "5f2a2502425e1b07946f52e9");
//            rolexMap.put("grpU", "1000");
//            rolexMap.put("objMod", objMod);
//
//            //List<Map<String, Object>> roleList = new ArrayList<>();
//            JSONArray roleList = new JSONArray();
//            roleList.add(rolexMap);
//
//            rolex.put("objComp", roleList);
//
//
//
////            List<String> view = new ArrayList<>();
////            view.add("Vinfo");
//
//            addUser.setId(addID);
//            addUser.setInfo(info);
//            addUser.setRolex(rolex);
//            addUser.setView(new JSONArray().fluentAdd("Vinfo"));
//
//            mongoTemplate.insert(addUser);
//
//            // 查询公司
//            Query compQuery = new Query(new Criteria("_id").is("5f2a2502425e1b07946f52e9"));
//            compQuery.fields().include("info");
//            Comp comp = mongoTemplate.findOne(compQuery, Comp.class);
//            //JSONObject compOne = (JSONObject) JSON.toJSON(mongoTemplate.findOne(compQuery, Comp.class));
//
//            lBUser addLBUser = new lBUser();
//            addLBUser.setId_CB(comp.getInfo().getString("_id"));
//            addLBUser.setId_U(addID);
//            addLBUser.setGrpU("1000");
//            addLBUser.setPic(info.get("pic").toString());
//            addLBUser.setRefC(comp.getInfo().getString("ref"));
//            addLBUser.setTmd(DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//            addLBUser.setTmk(DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
//            addLBUser.setWrdN(info.getJSONObject("wrdN"));
//            addLBUser.setWrdNC(comp.getInfo().getJSONObject("wrdN"));
//
//            IndexRequest indexRequest = new IndexRequest("lbuser");
//            indexRequest.source(JSON.toJSONString(addLBUser), XContentType.JSON);
//            try {
//                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//
//            } catch (IOException e) {
//
//            }
//
//
//
//            return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), null);
//
//
//        } catch (RuntimeException e) {
//
//            e.printStackTrace();
//
//        }
//
//        return "";
//
//    }

}