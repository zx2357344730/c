package com.cresign.login.utils;

import com.alibaba.fastjson.JSON;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.es.lNUser;
import com.cresign.tools.pojo.po.InitJava;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.pojo.po.userCard.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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


//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @Autowired
//    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private Qt qt;

    @Transactional(noRollbackFor = ResponseException.class)
    public String registerUser(Map<String, Object> info) {


            InitJava initJava = qt.getInitData(); //qt.getMDContent("cn_java", "newUser", InitJava.class);

            // objectId
            String addID = qt.GetObjectId();

            User addUser = new User();

            addUser.setId(addID);
            addUser.setRolex(initJava.getNewUser().getJSONObject("rolex"));

            UserInfo infoJson =  qt.jsonTo(initJava.getNewUser().getJSONObject("info"), UserInfo.class);
            infoJson.setMbn(info.get("mbn").toString());
            if (null != info.get("id_APP")) {
                infoJson.setId_APP(info.get("id_APP").toString());
            }
            System.out.println(JSON.toJSONString(infoJson));
            addUser.setInfo(infoJson);
            addUser.setView(initJava.getNewUser().getJSONArray("view"));
            System.out.println("新增注册:");
            System.out.println(JSON.toJSONString(addUser));
//            mongoTemplate.insert(addUser);

            qt.addMD(addUser);

            lNUser lnuser = new lNUser(addID,infoJson.getWrdN(),infoJson.getWrddesc(),
                    infoJson.getWrdNReal(),null, infoJson.getPic(), ""
                    ,"", infoJson.getCem(), infoJson.getMbn(),infoJson.getCnty()
                    , infoJson.getDefNG(), 0);

            qt.addES("lnuser", lnuser);
            // 查询公司
//            Query compQuery = new Query(new Criteria("_id").is("5f2a2502425e1b07946f52e9"));
//            compQuery.fields().include("info");
//            Comp comp = mongoTemplate.findOne(compQuery, Comp.class);
            //JSONObject compOne = (JSONObject) JSON.toJSON(mongoTemplate.findOne(compQuery, Comp.class));

//            lBUser addLBUser = new lBUser();
//            addLBUser.setId_CB(comp.getId());
//            addLBUser.setId_U(addID);
//            addLBUser.setGrpU("1000");
//            addLBUser.setPic(info.get("pic").toString());
//            addLBUser.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//            addLBUser.setTmk(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//            addLBUser.setWrdN(infoJson.getWrdN());
//            addLBUser.setWrdNCB(comp.getInfo().getWrdN());
//
//            IndexRequest indexRequest = new IndexRequest("lbuser");
//            indexRequest.source(JSON.toJSONString(addLBUser), XContentType.JSON);
//            try {
//                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//
//            } catch (IOException e) {
//
//            }
            return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), null);


//        } catch (RuntimeException e) {
//
//            e.printStackTrace();
//
//        }

        //return "";

    }


}