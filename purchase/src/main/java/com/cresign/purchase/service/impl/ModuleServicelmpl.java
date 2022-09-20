package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.common.ChatEnum;
import com.cresign.purchase.enumeration.PurchaseEnum;
import com.cresign.purchase.service.ModuleService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.es.lBProd;
import com.cresign.tools.pojo.es.lSBComp;
import com.cresign.tools.pojo.po.*;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateBatchRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateBatchResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


@Service
public class ModuleServicelmpl implements ModuleService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private Qt qt;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private RetResult retResult;

    @Resource
    private CoupaUtil coupaUtil;

    private static final String secretId = "AKIDwjMl15uUt53mFUVGk39zaw4ydAWfaS8a";
    private static final String secretKey = "HLEsHSRChx1sTtELCpFXfZGk14tVw97w";

    /**
     * 更新a-auth内日志权限信息，更新全部
     * @param id_C	公司编号
     * @param grpU	用户组别
     * @param listType	集合类型
     * @param grp	组别
     * @param auth	更新的状态
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    @Override
    public ApiResponse modifyLogAuthAll(String id_C, String grpU, String listType, String grp, Integer auth) {
//        JSONObject compAssetRole = getCompAssetRole(id_C);
//        JSONObject result = new JSONObject();
//        // 获取错误状态，为0是没有错误
//        Integer statusRole = compAssetRole.getInteger("status");
//        if (0 == statusRole) {
//            JSONObject role = compAssetRole.getJSONObject("role");
//            String assetIdRole = compAssetRole.getString("assetId");
//
//            JSONArray errRole = new JSONArray();
//            JSONObject objAuth = role.getJSONObject("objAuth");
//            boolean isOk = getLogAuth(objAuth,errRole,grpU,listType,grp);
//            result.put("errRole",errRole);
//            result.put("type",0);
//            if (isOk) {
//                JSONObject authGrpU = objAuth.getJSONObject(grpU);
//                JSONObject authListType = authGrpU.getJSONObject(listType);
//                JSONObject listTypeGrp = authListType.getJSONObject(grp);
//                JSONArray logAuthList = listTypeGrp.getJSONArray("log");
//
//                for (int i = 0; i < logAuthList.size(); i++) {
//                    JSONObject jsonObject = logAuthList.getJSONObject(i);
//                    jsonObject.put("auth",auth);
//                    logAuthList.set(i,jsonObject);
//                }
//
//                listTypeGrp.put("log",logAuthList);
//                authListType.put(grp,listTypeGrp);
//                authGrpU.put(listType,authListType);
//                objAuth.put(grpU,authGrpU);
//
//                role.put("objAuth",objAuth);
//                // 定义存储flowControl字典
//                JSONObject mapKey = new JSONObject();
//                // 设置字段数据
//                mapKey.put("role",role);
//                coupaUtil.updateAssetByKeyAndListKeyVal("id",assetIdRole,mapKey);
//                result.put("type",1);
//            }
//            return retResult.ok(CodeEnum.OK.getCode(), result);
//        } else {
//            return errResult(statusRole,1);
//        }
        return getObjAuth(id_C,grpU,listType,grp,auth,null,true);
    }

    /**
     * 更新a-auth内日志权限信息，指定更新
     * @param id_C	公司编号
     * @param grpU	用户组别
     * @param listType	集合类型
     * @param grp	组别
     * @param auth	更新的状态
     * @param modRef	指定的模块权限
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    @Override
    public ApiResponse modifyLogAuth(String id_C, String grpU, String listType, String grp, Integer auth, String modRef) {
//        JSONObject compAssetRole = getCompAssetRole(id_C);
//        JSONObject result = new JSONObject();
//        // 获取错误状态，为0是没有错误
//        Integer statusRole = compAssetRole.getInteger("status");
//        if (0 == statusRole) {
//            JSONObject role = compAssetRole.getJSONObject("role");
//            String assetIdRole = compAssetRole.getString("assetId");
//
//            JSONArray errRole = new JSONArray();
//            JSONObject objAuth = role.getJSONObject("objAuth");
//            boolean isOk = getLogAuth(objAuth,errRole,grpU,listType,grp);
//            result.put("errRole",errRole);
//            result.put("type",0);
//            if (isOk) {
//                JSONObject authGrpU = objAuth.getJSONObject(grpU);
//                JSONObject authListType = authGrpU.getJSONObject(listType);
//                JSONObject listTypeGrp = authListType.getJSONObject(grp);
//                JSONArray logAuthList = listTypeGrp.getJSONArray("log");
//                int updateInd = -1;
//                for (int i = 0; i < logAuthList.size(); i++) {
//                    JSONObject jsonObject = logAuthList.getJSONObject(i);
//                    String modRefLog = jsonObject.getString("modRef");
//                    if (modRefLog.equals(modRef)) {
//                        updateInd = i;
//                    }
//                }
//                if (updateInd != -1) {
//                    JSONObject jsonObject = logAuthList.getJSONObject(updateInd);
//                    jsonObject.put("auth",auth);
//                    logAuthList.set(updateInd,jsonObject);
//                    listTypeGrp.put("log",logAuthList);
//                    authListType.put(grp,listTypeGrp);
//                    authGrpU.put(listType,authListType);
//                    objAuth.put(grpU,authGrpU);
//
//                    role.put("objAuth",objAuth);
//                    // 定义存储flowControl字典
//                    JSONObject mapKey = new JSONObject();
//                    // 设置字段数据
//                    mapKey.put("role",role);
//                    coupaUtil.updateAssetByKeyAndListKeyVal("id",assetIdRole,mapKey);
//                    result.put("type",1);
//                } else {
//                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_MATCHING_MOD_REF.getCode(), "无匹配的modRef");
//                }
//            }
//            return retResult.ok(CodeEnum.OK.getCode(), result);
//        } else {
//            return errResult(statusRole,1);
//        }
        return getObjAuth(id_C,grpU,listType,grp,auth,modRef,false);
    }

    /**
     * 新增或修改init内日志模块
     * @param objLogMod	新增或修改信息
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    @Override
    public ApiResponse addOrUpdateInitMod(JSONObject objLogMod) {
        // 获取init信息
        Init init = coupaUtil.getInit();
        // 获取日志模块信息
        JSONObject logInit = init.getLogInit();
        // 遍历模块信息
        objLogMod.keySet().forEach(k -> {
            // 获取模块对象
            JSONObject jsonObject = objLogMod.getJSONObject(k);
            // 添加模块
            logInit.put(k,jsonObject);
        });
        // 更新数据库
        coupaUtil.updateInitLog(logInit);
        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
    }

    /**
     * 根据id_C修改asset的a-auth内role对应的grpU+listType+grp
     * @param id_C  公司编号
     * @param grpU  用户组别
     * @param listType  集合类型
     * @param grp   组别
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    @Override
    public ApiResponse updateLogAuth(String id_C,String grpU,String listType,String grp) {
        // 调用方法获取公司模块信息
//        JSONObject compAssetMod = getCompAssetMod(id_C);
        // 调用方法获取asset的a-core信息
        JSONObject compAssetMod = getCompAssetByRef(id_C,"a-core","control");
        // 获取错误状态，为0是没有错误
        Integer status = compAssetMod.getInteger("status");
        // 创建返回结果对象
        JSONObject result = new JSONObject();
        if (0 == status) {
//            JSONObject compAssetRole = getCompAssetRole(id_C);
            // 调用方法获取asset的a-auth信息
            JSONObject compAssetRole = getCompAssetByRef(id_C,"a-auth","role");
            // 获取错误状态，为0是没有错误
            Integer statusRole = compAssetRole.getInteger("status");
            if (0 == statusRole) {
                // 获取role卡片信息
                JSONObject role = compAssetRole.getJSONObject("role");
                // 获取asset编号
                String assetIdRole = compAssetRole.getString("assetId");

                // 获取control卡片信息
                JSONObject control = compAssetMod.getJSONObject("control");
                // 获取卡片内模块信息
                JSONObject objMod = control.getJSONObject("objMod");
                // 获取init对象
                Init init = coupaUtil.getInit();
                // 获取日志init信息
                JSONObject logInit = init.getLogInit();
//                System.out.println("init:");
//                System.out.println(JSON.toJSONString(init));
                // 创建存储a-core模组对象
                JSONObject modList = new JSONObject();
//                JSONObject modListNew = new JSONObject();
                // 创建存储a-auth模组集合
                JSONArray modListNew = new JSONArray();
                // 创建存储a-core异常信息集合
                JSONArray errMod = new JSONArray();
                // 遍历模组信息
                objMod.keySet().forEach(k -> {
                    // 根据键获取对应的模组信息
                    JSONObject jsonObject = logInit.getJSONObject(k);
                    if (null != jsonObject) {
//                        Integer auth = jsonObject.getInteger("auth");
//                        if (auth == 0) {
//                            JSONObject errSon = new JSONObject();
//                            errSon.put("modKey",k);
//                            errSon.put("desc","init内该mod已被禁用");
//                            errMod.add(errSon);
//                        } else {
//                            modList.put(k,objMod.getJSONObject(k));
//                            modListNew.put(k,jsonObject);
//                        }
                        // 添加模组信息
                        modList.put(k,objMod.getJSONObject(k));
                        modListNew.add(jsonObject);
                    } else {
                        // 添加错误信息
                        JSONObject errSon = new JSONObject();
                        errSon.put("modKey",k);
                        errSon.put("desc","init内不存在");
                        errMod.add(errSon);
                    }
                });
//                System.out.println("modList:");
//                System.out.println(JSON.toJSONString(modList));
//                System.out.println(JSON.toJSONString(modListNew));
                // 判断有模组
                if (modList.size() > 0) {
                    // 创建存储a-auth异常信息集合
                    JSONArray errRole = new JSONArray();
//                    System.out.println("前:");
//                    System.out.println(JSON.toJSONString(role));
                    // 获取auth信息
                    JSONObject objAuth = role.getJSONObject("objAuth");
                    // 获取判断结果
                    boolean isOk = getLogAuth(objAuth,errRole,grpU,listType,grp);
                    if (isOk) {
                        // 添加信息
                        JSONObject authGrpU = objAuth.getJSONObject(grpU);
                        JSONObject authListType = authGrpU.getJSONObject(listType);
                        JSONObject listTypeGrp = authListType.getJSONObject(grp);
//                        listTypeGrp.put("log",modListNew);
//                        authListType.put(grp,listTypeGrp);
//                        authGrpU.put(listType,authListType);
//                        objAuth.put(grpU,authGrpU);
//
////                        System.out.println("后:");
//                        role.put("objAuth",objAuth);
////                        System.out.println(JSON.toJSONString(role));
//                        // 定义存储flowControl字典
//                        JSONObject mapKey = new JSONObject();
//                        // 设置字段数据
//                        mapKey.put("role",role);
//                        // 更新数据库
//                        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetIdRole,mapKey);
                        // 调用方法更新数据
                        setAndUpdateAuth(listTypeGrp,modListNew,authListType,authGrpU
                                ,objAuth,role,assetIdRole,grpU,listType,grp);
                    }

//                    objAuth.keySet().forEach(k -> {
//                        JSONObject objAuthSon = objAuth.getJSONObject(k);
//                        JSONObject lSProd = objAuthSon.getJSONObject("lSProd");
//                        if (null == lSProd) {
//                            JSONObject errRoleSon = new JSONObject();
//                            errRoleSon.put("key",k);
//                            errRoleSon.put("desc","该键的lSProd为空");
//                            errRole.add(errRoleSon);
//                        } else {
////                            System.out.println("lSProd:");
////                            System.out.println(JSON.toJSONString(lSProd));
//                            lSProd.keySet().forEach(sonKey -> {
//                                JSONObject lSProdSon = lSProd.getJSONObject(sonKey);
////                                lSProdSon.put("log",modList);
//                                lSProdSon.put("log",modListNew);
//                                lSProd.put(sonKey,lSProdSon);
//                            });
//                            objAuthSon.put("lSProd",lSProd);
//                            objAuth.put(k,objAuthSon);
//                        }
//                    });

//                    System.out.println("role异常:");
//                    System.out.println(JSON.toJSONString(errRole));
                    result.put("errRole",errRole);
                }
//                System.out.println("modInit异常:");
//                System.out.println(JSON.toJSONString(errMod));
                result.put("errMod",errMod);
            } else {
                return errResult(statusRole,1);
            }
        } else {
            return errResult(status,0);
        }
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

//    /**
//     * 单翻译 - 只能翻译一个字段
//     * @param data	需要翻译的数据
//     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/8/19
//     */
//    @Override
//    public ApiResponse singleTranslate(JSONObject data){
//        try{
////            JSONObject data = can.getJSONObject("data");
//            String cn = data.getString("cn");
//            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
//            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
//            Credential cred = new Credential(secretId, secretKey);
//            // 实例化一个http选项，可选的，没有特殊需求可以跳过
//            HttpProfile httpProfile = new HttpProfile();
//            httpProfile.setEndpoint("tmt.tencentcloudapi.com");
//            // 实例化一个client选项，可选的，没有特殊需求可以跳过
//            ClientProfile clientProfile = new ClientProfile();
//            clientProfile.setHttpProfile(httpProfile);
//            // 实例化要请求产品的client对象,clientProfile是可选的
//            TmtClient client = new TmtClient(cred, "ap-guangzhou", clientProfile);
//            // 实例化一个请求对象,每个接口都会对应一个request对象
//            TextTranslateRequest req = new TextTranslateRequest();
//            req.setSourceText(cn);
//            req.setSource("zh");
//            req.setTarget("en");
//            req.setProjectId(1270102L);
//            // 返回的resp是一个TextTranslateResponse的实例，与请求对象对应
//            TextTranslateResponse resp = client.TextTranslate(req);
//            // 输出json格式的字符串回包
//            System.out.println(TextTranslateResponse.toJsonString(resp));
//            data.put("en",resp.getTargetText());
//            return retResult.ok(CodeEnum.OK.getCode(), data);
//        } catch (TencentCloudSDKException e) {
//            System.out.println(e.toString());
//        }
//        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
//    }

    /**
     * 多翻译 - 按照指定格式请求，可以翻译所有的字段
     * @param data	需要翻译的数据
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @Override
    public ApiResponse manyTranslate(JSONObject data) {
        try{
//            JSONObject data = can.getJSONObject("data");
            List<String> key = new ArrayList<>(data.keySet());
            String[] val = new String[key.size()];
//            key.forEach(k -> val.add(data.getJSONObject(k).getString("cn")));
            for (int i = 0; i < key.size(); i++) {
                val[i] = data.getJSONObject(key.get(i)).getString("cn");
            }
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
            Credential cred = new Credential(secretId, secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("tmt.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            TmtClient client = new TmtClient(cred, "ap-guangzhou", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            TextTranslateBatchRequest req = new TextTranslateBatchRequest();
            req.setSource("zh");
            req.setTarget("en");
            req.setProjectId(1270102L);

//            String[] sourceTextList1 = {"你好", "早上好"};
//            String[] sourceTextList1 = val;
            System.out.println(JSON.toJSONString(val));
            req.setSourceTextList(val);

            // 返回的resp是一个TextTranslateBatchResponse的实例，与请求对象对应
            TextTranslateBatchResponse resp = client.TextTranslateBatch(req);
            // 输出json格式的字符串回包
//            System.out.println(TextTranslateBatchResponse.toJsonString(resp));
//            System.out.println(JSON.toJSONString(resp));
            String[] targetTextList = resp.getTargetTextList();
            for (int i = 0; i < key.size(); i++) {
                String k = key.get(i);
                JSONObject daZ = data.getJSONObject(k);
                daZ.put("en",targetTextList[i]);
                data.put(k,daZ);
            }
            return retResult.ok(CodeEnum.OK.getCode(), data);
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
    }

    /**
     * es的lsprod转lbprod
     * @param id_P	产品编号
     * @param id_C	公司编号
     * @param isMove	是否删除lsprod
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @Override
    public ApiResponse lSProdTurnLBProd(String id_P,String id_C,Boolean isMove) {
//        String id_P = can.getString("id_P");
//        String id_C = can.getString("id_C");
//        Boolean isMove = can.getBoolean("isMove");

        // 获取es的lsprod信息
//        JSONArray esQuery = coupaUtil.getEsQuery("lsprod", Collections.singletonList("id_P")
//                , Collections.singletonList(id_P));
        JSONArray esQuery = qt.getES("lsprod", qt.setESFilt("id_P", id_P),1);
        // 获取查询es的第一个信息
        JSONObject prodRe = esQuery.getJSONObject(0);
        // 获取lsprod具体信息
        JSONObject lsprod = prodRe.getJSONObject("map");
        // 获取lsprod对应es的编号
        String esId = prodRe.getString("esId");
        System.out.println(JSON.toJSONString(lsprod));
        System.out.println(esId);
        // 判断是否删除lsprod
        if (isMove) {
//            Integer reI = coupaUtil.delEsById("lsprod", esId);
            qt.delES("lsprod",esId);
//            if (reI != 0) {
//                System.out.println("删除lsprod出现异常");
//                return retResult.ok(CodeEnum.OK.getCode(), "错误码：删除lsprod出现异常");
//            }
//            else {
//                lsprod.put("id_P","test_id_P");
//                coupaUtil.updateES_lSProd(JSONObject.parseObject(JSON.toJSONString(lsprod), lSProd.class));
//            }
        }
        // 根据lsprod创建lbprod
        lBProd lbprod = new lBProd(
                lsprod.getString("id_P"), id_C
                , lsprod.getString("id_CP"), id_C
                , lsprod.getJSONObject("wrdN")==null?new JSONObject():lsprod.getJSONObject("wrdN")
                , lsprod.getJSONObject("wrddesc")==null?new JSONObject():lsprod.getJSONObject("wrddesc")
                , getStrIsNull(lsprod.getString("grp")), getStrIsNull(lsprod.getString("grpB"))
                , getStrIsNull(lsprod.getString("ref")), getStrIsNull(lsprod.getString("refB"))
                , getStrIsNull(lsprod.getString("pic")),lsprod.getInteger("lDC")
                , lsprod.getInteger("lUT")==null?0:lsprod.getInteger("lUT"));
        // 写入lbprod信息
//        coupaUtil.updateES_lBProd(lbprod);
        qt.addES("lbprod", lbprod);

        return retResult.ok(CodeEnum.OK.getCode(), "请求成功");
    }

    /**
     * 判断str为null方法
     * @param str	字符串
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    private String getStrIsNull(String str){
        if (null == str) {
            return "";
        } else {
            return str;
        }
    }

    /**
     * 新增或删除用户的模块使用权
     * @param id_C	公司编号
     * @param objUser	用户信息集合
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @Override
    public ApiResponse modSetUser(String id_C,JSONObject objUser) {
//        String id_C = can.getString("id_C");
        JSONObject result = new JSONObject();
//        JSONArray reAddArr = userT(can.getJSONObject("objUser"), id_C);
        // 调用核心方法，获取返回结果
        JSONArray reAddArr = addOrDelUserModCore(objUser, id_C);
        if (reAddArr.size() > 0) {
            result.put("type",1);
            result.put("resultAddArr",reAddArr);
        } else {
            result.put("type",0);
        }
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    /**
     * 根据公司编号操作公司资产的模块信息
     * @param id_C  公司编号
     * @param objModQ   操作的模块信息
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @Override
    public ApiResponse modSetControl(String id_C,JSONObject objModQ) {
//        String id_C = can.getString("id_C");
        // 调用方法获取公司模块信息
//        JSONObject compAssetMod = getCompAssetMod(id_C);
        JSONObject compAssetMod = getCompAssetByRef(id_C,"a-core","control");
        // 获取错误状态，为0是没有错误
        Integer status = compAssetMod.getInteger("status");
        if (0 == status) {
//            boolean isC = false;
            // 定义存储错误信息json集合
            JSONArray errList = new JSONArray();
            JSONObject control = compAssetMod.getJSONObject("control");
            String assetId = compAssetMod.getString("assetId");
//            JSONObject objModQ = can.getJSONObject("objMod");
            JSONObject objMod = control.getJSONObject("objMod");
            // 遍历公司模块信息
            objModQ.keySet().forEach(k -> {
                // 根据模块键获取模块信息
                JSONObject mod = objModQ.getJSONObject(k);
                // 获取操作状态
                String type = mod.getString("type");
//                String key = js.getString("key");
                if ("add".equals(type)) {
                    // 新增模块信息
//                    JSONObject val = mod.getJSONObject("val");
//                    Integer upState = mod.getInteger("upState");
//                    if (upState == 1) {
//
//                    }
                    objMod.put(k,mod.getJSONObject("val"));
                } else if ("del".equals(type)) {
                    // 删除模块信息
                    objMod.remove(k);
                } else if ("upState".equals(type)) {
                    JSONObject val = mod.getJSONObject("val");
                    JSONArray id_u = val.getJSONArray("id_U");
                    Integer bcdState = val.getInteger("bcdState");
                    if (null != id_u) {
                        JSONArray resultArr = new JSONArray();
                        for (int i = 0; i < id_u.size(); i++) {
                            String string = id_u.getString(i);
                            getUserAndUp(string,resultArr,id_C,null,1,k,bcdState);
                        }
                    }
                    JSONObject jsonObject = objMod.getJSONObject(k);
                    jsonObject.put("bcdState",bcdState);
                    objMod.put(k,jsonObject);
//                        else {
////                            if (null != id_u) {
////                                JSONObject re = new JSONObject();
////                                re.put("key",k);
////                                re.put("err","用户列表为空或不存在");
////                                errList.add(re);
////                            }
//                        }
                } else {
                    JSONObject re = new JSONObject();
                    re.put("key",k);
                    re.put("err","修改状态为空");
                    errList.add(re);
                }
            });
            control.put("objMod",objMod);
            // 定义存储flowControl字典
            JSONObject mapKey = new JSONObject();
            // 设置字段数据
            mapKey.put("control",control);
            coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
            JSONObject result = new JSONObject();
            if (errList.size() > 0) {
                result.put("type",1);
                result.put("errList",errList);
            } else {
                result.put("type",0);
            }
            return retResult.ok(CodeEnum.OK.getCode(), result);
        } else {
            return errResult(status,0);
        }
    }

    /**
     * 根据id_C获取模块信息
     * @param id_C	公司编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @Override
    public ApiResponse modGetControl(String id_C) {
//        String id_C = can.getString("id_C");
        // 调用方法获取公司模块信息
//        JSONObject compAssetMod = getCompAssetMod(id_C);
        JSONObject compAssetMod = getCompAssetByRef(id_C,"a-core","control");
        // 获取错误状态，为0是没有错误
        Integer status = compAssetMod.getInteger("status");
        if (0 == status) {
            JSONObject control = compAssetMod.getJSONObject("control");
            return retResult.ok(CodeEnum.OK.getCode(), control);
        } else {
            return errResult(status,0);
        }
    }

    /**
     * 建立连接关系
     * @param id_C  公司编号
     * @param id_CP 公司编号？
     * @param id_CB 供应商编号
     * @param id_CBP 供应商编号？
     * @param wrdNC 公司名称
     * @param wrddesc 公司描述
     * @param wrdNCB 供应商名称
     * @param wrddescB 供应商描述
     * @param grp 公司组别
     * @param grpB 供应商组别
     * @param refC 公司编号
     * @param refCB 供应商编号
     * @param picC 公司图片
     * @param picCB 供应商图片
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @Override
    public ApiResponse modAddLSBComp(String id_C,String id_CP,String id_CB,String id_CBP
            ,JSONObject wrdNC,JSONObject wrddesc,JSONObject wrdNCB,JSONObject wrddescB
            ,String grp,String grpB,String refC,String refCB,String picC,String picCB) {
//        lSBComp comp = new lSBComp(
//                can.getString("id_C")
//                , can.getString("id_CP")
//                , can.getString("id_CB")
//                , can.getString("id_CBP")
//                , can.getJSONObject("wrdNC")
//                , can.getJSONObject("wrddesc")
//                , can.getJSONObject("wrdNCB")
//                , can.getJSONObject("wrddescB")
//                , can.getString("grp")
//                , can.getString("grpB")
//                , can.getString("refC")
//                , can.getString("refCB")
//                , can.getString("picC")
//                , can.getString("picCB")
//                , DateUtils.getDateNow(DateEnum.DATE_TWO.getDate())
//                , DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
        lSBComp lsbcomp = new lSBComp(
                id_C
                , id_CP
                , id_CB
                , id_CBP
                , wrdNC
                , wrddesc
                , wrdNCB
                , wrddescB
                , grp
                , grpB
                , refC
                , refCB
                , picC
                , picCB
                , DateUtils.getDateNow(DateEnum.DATE_TWO.getDate())
                , DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
//        coupaUtil.updateES_lSBComp(comp);
        qt.addES("lsbcomp", lsbcomp);
        return retResult.ok(CodeEnum.OK.getCode(), "连接关系成功");
    }

    /**
     * 更新日志模块权限核心方法
     * @param id_C	公司编号
     * @param grpU	用户组别
     * @param listType	集合类型
     * @param grp	组别
     * @param auth	更新的状态
     * @param modRef	指定的模块权限
     * @param isAll	是否是更新所有
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    private ApiResponse getObjAuth(String id_C, String grpU, String listType, String grp, Integer auth, String modRef,boolean isAll){
//        JSONObject compAssetRole = getCompAssetRole(id_C);
        // 调用方法获取asset
        JSONObject compAssetRole = getCompAssetByRef(id_C,"a-auth","role");
        // 创建返回信息对象
        JSONObject result = new JSONObject();
        // 获取错误状态，为0是没有错误
        Integer statusRole = compAssetRole.getInteger("status");
        if (0 == statusRole) {
            // 获取role卡片信息
            JSONObject role = compAssetRole.getJSONObject("role");
            // 获取asset编号
            String assetIdRole = compAssetRole.getString("assetId");
            // 创建存储异常集合
            JSONArray errRole = new JSONArray();
            // 获取权限信息
            JSONObject objAuth = role.getJSONObject("objAuth");
            // 获取权限信息判断结果
            boolean isOk = getLogAuth(objAuth,errRole,grpU,listType,grp);
            // 添加返回信息
            result.put("errRole",errRole);
            result.put("type",0);
            if (isOk) {
                // 获取内部字段
                JSONObject authGrpU = objAuth.getJSONObject(grpU);
                JSONObject authListType = authGrpU.getJSONObject(listType);
                JSONObject listTypeGrp = authListType.getJSONObject(grp);
                JSONArray logAuthList = listTypeGrp.getJSONArray("log");
                // 判断是更新所有
                if (isAll) {
                    // 遍历权限集合
                    for (int i = 0; i < logAuthList.size(); i++) {
                        JSONObject jsonObject = logAuthList.getJSONObject(i);
                        // 更新权限
                        jsonObject.put("auth",auth);
                        logAuthList.set(i,jsonObject);
                    }
                } else {
                    // 定义存储下标，默认-1
                    int updateInd = -1;
                    // 遍历权限集合
                    for (int i = 0; i < logAuthList.size(); i++) {
                        JSONObject jsonObject = logAuthList.getJSONObject(i);
                        String modRefLog = jsonObject.getString("modRef");
                        // 判断指定的模块
                        if (modRefLog.equals(modRef)) {
                            // 获取下标
                            updateInd = i;
                        }
                    }
                    if (updateInd != -1) {
                        JSONObject jsonObject = logAuthList.getJSONObject(updateInd);
                        // 更新状态
                        jsonObject.put("auth",auth);
                        logAuthList.set(updateInd,jsonObject);
                    } else {
                        throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_MATCHING_MOD_REF.getCode(), "无匹配的modRef");
                    }
                }
//                listTypeGrp.put("log",logAuthList);
//                authListType.put(grp,listTypeGrp);
//                authGrpU.put(listType,authListType);
//                objAuth.put(grpU,authGrpU);
//
//                role.put("objAuth",objAuth);
//                // 定义存储flowControl字典
//                JSONObject mapKey = new JSONObject();
//                // 设置字段数据
//                mapKey.put("role",role);
//                coupaUtil.updateAssetByKeyAndListKeyVal("id",assetIdRole,mapKey);
                // 调用更新字段并且更新数据库方法
                setAndUpdateAuth(listTypeGrp,logAuthList,authListType,authGrpU
                        ,objAuth,role,assetIdRole,grpU,listType,grp);
                // 返回正常状态
                result.put("type",1);
            }
            return retResult.ok(CodeEnum.OK.getCode(), result);
        } else {
            return errResult(statusRole,1);
        }
    }

    /**
     * 更新字段并且更新数据库方法
     * @param listTypeGrp	字段
     * @param logAuthList	字段
     * @param authListType	字段
     * @param authGrpU	字段
     * @param objAuth	字段
     * @param role	卡片信息
     * @param assetIdRole	asset编号
     * @param grpU	用户组别
     * @param listType	集合类型
     * @param grp	组别
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    private void setAndUpdateAuth(JSONObject listTypeGrp,JSONArray logAuthList,JSONObject authListType
            ,JSONObject authGrpU,JSONObject objAuth,JSONObject role,String assetIdRole,String grpU
            ,String listType,String grp){
        listTypeGrp.put("log",logAuthList);
        authListType.put(grp,listTypeGrp);
        authGrpU.put(listType,authListType);
        objAuth.put(grpU,authGrpU);

        role.put("objAuth",objAuth);
        // 定义存储flowControl字典
        JSONObject mapKey = new JSONObject();
        // 设置字段数据
        mapKey.put("role",role);
        // 更新数据库
        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetIdRole,mapKey);
    }

    /**
     * 判断objAuth为空，并且加上错误信息方法
     * @param objAuth	权限信息
     * @param errRole	错误信息集合
     * @param grpU	用户组别
     * @param listType	集合类型
     * @param grp	组别
     * @return boolean  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    private boolean getLogAuth(JSONObject objAuth,JSONArray errRole,String grpU,String listType,String grp){
        if (null == objAuth) {
            JSONObject errRoleSon = new JSONObject();
            errRoleSon.put("key","objAuth");
            errRoleSon.put("desc","公司objAuth为空");
            errRole.add(errRoleSon);
        } else {
            JSONObject authGrpU = objAuth.getJSONObject(grpU);
            if (null == authGrpU) {
                JSONObject errRoleSon = new JSONObject();
                errRoleSon.put("key",grpU);
                errRoleSon.put("desc","公司objAuth内当前key的grpU为空");
                errRole.add(errRoleSon);
            } else {
                JSONObject authListType = authGrpU.getJSONObject(listType);
                if (null == authListType) {
                    JSONObject errRoleSon = new JSONObject();
                    errRoleSon.put("key",listType);
                    errRoleSon.put("desc","公司objAuth内当前key的listType为空");
                    errRole.add(errRoleSon);
                } else {
                    JSONObject listTypeGrp = authListType.getJSONObject(grp);
                    if (null == listTypeGrp) {
                        JSONObject errRoleSon = new JSONObject();
                        errRoleSon.put("key",grp);
                        errRoleSon.put("desc","公司objAuth内当前key的grp为空");
                        errRole.add(errRoleSon);
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *  错误返回方法
     * @param status	错误状态
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/23
     */
    private ApiResponse errResult(int status,int isOperation){
        switch (status) {
            case 1:
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_ASSET_ID.getCode(), "该公司没有assetId");
            case 2:
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_ASSET.getCode(), "该公司没有asset");
            case 3:
                if (isOperation == 0) {
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_CONTROL_K.getCode(), "该公司没有control卡片");
                } else {
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_ROLE_K.getCode(), "该公司没有role卡片");
                }
            case 4:
                if (isOperation == 0) {
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_CONTROL_K.getCode(), "该公司control卡片异常");
                } else {
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_ROLE_K.getCode(), "该公司role卡片异常");
                }
            default:
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_WZ.getCode(), "接口未知异常");
        }
    }

//    /**
//     * 根据公司编号获取公司资产的模块信息
//     * @param id_C	公司编号
//     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/8/19
//     */
//    private JSONObject getCompAssetMod(String id_C){
//        JSONObject result = new JSONObject();
//        String assetId = coupaUtil.getAssetId(id_C, "a-core");
////        System.out.println("assetId:"+assetId);
//        if (null == assetId) {
//            result.put("status",1);
//            return result;
//        }
//        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("control"));
//        if (null == asset) {
//            result.put("status",2);
//            return result;
//        }
////        System.out.println(JSON.toJSONString(asset));
//        JSONObject control = asset.getControl();
//        if (null == control) {
//            result.put("status",3);
//            return result;
//        }
//        JSONObject objData = control.getJSONObject("objMod");
//        if (null == objData) {
//            result.put("status",4);
//            return result;
//        }
//        result.put("status",0);
//        result.put("control",control);
//        result.put("assetId",assetId);
//        return result;
//    }

//    /**
//     * 根据id_C获取asset的a-auth信息
//     * @param id_C	公司编号
//     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/9/19
//     */
//    private JSONObject getCompAssetRole(String id_C){
//        JSONObject result = new JSONObject();
//        String assetId = coupaUtil.getAssetId(id_C, "a-auth");
////        System.out.println("assetId:"+assetId);
//        if (null == assetId) {
//            result.put("status",1);
//            return result;
//        }
//        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("role"));
//        if (null == asset) {
//            result.put("status",2);
//            return result;
//        }
////        System.out.println(JSON.toJSONString(asset));
//        JSONObject role = asset.getRole();
//        if (null == role) {
//            result.put("status",3);
//            return result;
//        }
//        JSONObject objAuth = role.getJSONObject("objAuth");
//        if (null == objAuth) {
//            result.put("status",4);
//            return result;
//        }
//        result.put("status",0);
//        result.put("role",role);
//        result.put("assetId",assetId);
//        return result;
//    }

    /**
     * 根据id_C获取assetRef指定的asset的card信息
     * @param id_C	公司编号
     * @param assetRef	asset编号
     * @param card	卡片
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    private JSONObject getCompAssetByRef(String id_C,String assetRef,String card){
        JSONObject result = new JSONObject();
        String assetId = coupaUtil.getAssetId(id_C, assetRef);
        if (null == assetId) {
            result.put("status",1);
            return result;
        }
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList(card));
        if (null == asset) {
            result.put("status",2);
            return result;
        }
        if ("a-core".equals(assetRef)) {
            JSONObject control = asset.getControl();
            if (null == control) {
                result.put("status",3);
                return result;
            }
            JSONObject objData = control.getJSONObject("objMod");
            if (null == objData) {
                result.put("status",4);
                return result;
            }
            result.put("control",control);
        } else {
            JSONObject role = asset.getRole();
            if (null == role) {
                result.put("status",3);
                return result;
            }
            JSONObject objAuth = role.getJSONObject("objAuth");
            if (null == objAuth) {
                result.put("status",4);
                return result;
            }
            result.put("role",role);
        }
        result.put("status",0);
        result.put("assetId",assetId);
        return result;
    }

    /**
     * 新增或删除用户的模块使用权-转接方法
     * @param users	用户集合数据
     * @param id_C	公司编号
     * @return com.alibaba.fastjson.JSONArray  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    private JSONArray addOrDelUserModCore(JSONObject users,String id_C){
        // 创建返回结果集合
        JSONArray resultArr = new JSONArray();
//        JSONObject addUser = can.getJSONObject("addUser");
        // 遍历用户集合
        users.keySet().forEach(id_U -> getUserAndUp(id_U,resultArr,id_C,users,0,null,0));
        return resultArr;
    }

    /**
     * 新增或删除用户的模块使用权-核心方法
     * @param id_U	用户编号
     * @param resultArr	返回结果集合
     * @param id_C	公司编号
     * @param users	用户集合
     * @param operation 操作状态
     * @param modName   模块名称
     * @param bcdStatus 状态
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    private void getUserAndUp(String id_U,JSONArray resultArr,String id_C
            ,JSONObject users,Integer operation,String modName,Integer bcdStatus){
        // 根据用户编号获取用户的rolex卡片信息
        User user = coupaUtil.getUserById(id_U, Collections.singletonList("rolex"));
        // 创建返回结果对象
//        User user = coupaUtil.getUserById(id_U, Collections.singletonList("rolex"));
        User user = qt.getMDContent(id_U, "rolex", User.class);
        JSONObject result = new JSONObject();
        if (null == user) {
            result.put("id_U",id_U);
            result.put("desc","用户信息为空");
            resultArr.add(result);
        } else {
            JSONObject rolex = user.getRolex();
            if (null == rolex) {
                result.put("id_U",id_U);
                result.put("desc","用户权限卡片为空");
                resultArr.add(result);
            } else {
                // 获取公司权限信息集合
                JSONObject objComp = rolex.getJSONObject("objComp");
                // 获取当前处理公司权限信息
                JSONObject c_role = objComp.getJSONObject(id_C);
                if (null == c_role) {
                    result.put("id_U",id_U);
                    result.put("desc","用户权限卡片内当前公司信息为空");
                    resultArr.add(result);
                } else {
                    // 判断操作状态为0
                    if (operation == 0) {
                        // 调用方法获取公司模块信息
//                        JSONObject compAssetMod = getCompAssetMod(id_C);
                        JSONObject compAssetMod = getCompAssetByRef(id_C,"a-core","control");
                        // 获取错误状态，为0是没有错误
                        Integer status = compAssetMod.getInteger("status");
                        if (0 == status) {
                            // 获取卡片信息
                            JSONObject control = compAssetMod.getJSONObject("control");
                            // 获取资产编号
                            String assetId = compAssetMod.getString("assetId");
                            // 获取模块信息
                            JSONObject objMod = control.getJSONObject("objMod");

                            // 获取修改当前用户的模块信息
                            JSONObject u_role = users.getJSONObject(id_U);
                            // 获取当前公司的模块信息
                            JSONObject modAuth = getModAuth(c_role.getJSONObject("modAuth"));
                            // 遍历修改当前用户的模块信息
                            u_role.keySet().forEach(k -> {
                                // 根据键获取修改的模块信息
                                JSONObject mod = u_role.getJSONObject(k);
                                // 获取操作类型
                                String type = mod.getString("type");
                                // 定义存储是否为空，默认为空
                                boolean isNull = false;
                                // 获取卡片的模块信息
                                JSONObject jsonObject = objMod.getJSONObject(k);
                                // 定义存储模块用户信息
                                JSONArray jsonArray = null;
                                if (null != jsonObject) {
                                    isNull = true;
                                    jsonArray = jsonObject.getJSONArray("id_U");
                                }
                                // 判断为删除状态
                                if ("del".equals(type) && modAuth.containsKey(k)) {
                                    // 删除当前模块
                                    modAuth.remove(k);
                                    if (isNull) {
//                                        JSONArray jsonArray = jsonObject.getJSONArray("id_U");
                                        // 定义存储要删除的下标，默认-1
                                        int currentUser = -1;
                                        // 遍历用户集合
                                        for (int i = 0; i < jsonArray.size(); i++) {
                                            // 根据下标获取用户id
                                            String id_UN = jsonArray.getString(i);
                                            // 判断等于当前用户
                                            if (id_UN.equals(id_U)) {
                                                // 赋值下标
                                                currentUser = i;
                                            }
                                        }
                                        if (currentUser != -1) {
                                            // 根据下标删除信息
                                            jsonArray.remove(currentUser);
                                            jsonObject.put("id_U",jsonArray);
                                            objMod.put(k,jsonObject);
                                        }
                                    }
                                } else {
                                    // 获取当前模块键是否存在
                                    boolean isModAuth = modAuth.containsKey(k);
                                    // 修改当前模块
                                    modAuth.put(k,mod.getJSONObject("val"));
//                                    JSONObject jsonObject = objMod.getJSONObject(k);
                                    // 判断模块键不存在
                                    if (!isModAuth && isNull) {
//                                        JSONArray jsonArray = jsonObject.getJSONArray("id_U");
                                        // 定义存储，当前用户是否存在，默认不存在
                                        boolean isExistence = false;
                                        // 遍历用户集合
                                        for (int i = 0; i < jsonArray.size(); i++) {
                                            // 根据下标获取用户id
                                            String id_UN = jsonArray.getString(i);
                                            // 判断等于当前用户id
                                            if (id_UN.equals(id_U)) {
                                                isExistence = true;
                                                break;
                                            }
                                        }
                                        // 判断不存在
                                        if (!isExistence) {
                                            // 添加用户id
                                            jsonArray.add(id_U);
                                            jsonObject.put("id_U",jsonArray);
                                            objMod.put(k,jsonObject);
                                        }
                                    }
                                }
                            });
                            control.put("objMod",objMod);
                            // 定义存储flowControl字典
                            JSONObject mapKey = new JSONObject();
                            // 设置字段数据
                            mapKey.put("control",control);
                            // 更新数据库
                            coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);

                            c_role.put("modAuth",modAuth);
                            objComp.put(id_C,c_role);
                            rolex.put("objComp",objComp);
                            // 定义存储flowControl字典
                            mapKey = new JSONObject();
                            // 设置字段数据
                            mapKey.put("rolex",rolex);
                            // 更新数据库
                            qt.setMDContent(id_U, mapKey, User.class);
//                            coupaUtil.updateUserByKeyAndListKeyVal("id",id_U,mapKey);
                        } else {
                            result.put("id_U",id_U);
                            result.put("desc","获取公司信息异常");
                            resultArr.add(result);
                        }
                    } else {
                        // 获取当前公司的模块信息
                        JSONObject modAuth = c_role.getJSONObject("modAuth");
                        JSONObject mod = modAuth.getJSONObject(modName);
                        mod.put("bcdStatus",bcdStatus);
                        // 修改当前模块
                        modAuth.put(modName,mod);
                        c_role.put("modAuth",modAuth);
                        objComp.put(id_C,c_role);
                        rolex.put("objComp",objComp);
                        // 定义存储flowControl字典
                        JSONObject mapKey = new JSONObject();
                        // 设置字段数据
                        mapKey.put("rolex",rolex);
                        qt.setMDContent(id_U,mapKey,User.class);
//                        coupaUtil.updateUserByKeyAndListKeyVal("id",id_U,mapKey);
                    }
                }
            }
        }
    }

    /**
     * 获取当前公司的模块信息方法,为空则创建一个
     * @param modAuth	模块信息
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    private JSONObject getModAuth(JSONObject modAuth){
        if (null == modAuth) {
            modAuth = new JSONObject();
        }
        return modAuth;
    }

    @Override
    public ApiResponse addModule(String id_U, String oid, String id_C, String ref, Integer bcdLevel) throws IOException {
        //判断公司负责人
        Query query = new Query(
                new Criteria("info.id_C").is(id_C)
                        .and("info.ref").is("a-auth"));
        query.fields().include("def.id_UM");
        Asset asset = mongoTemplate.findOne(query, Asset.class);
        if (asset != null || asset.getDef().get("id_UM").equals(id_U)) {

            //查询redis订单信息
            String order = redisTemplate0.opsForValue().get(oid);

            JSONObject redisMap = (JSONObject) JSON.parse(order);

            if (redisMap == null){

                throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, PurchaseEnum.REDIS_ORDER_NO_HAVE.getCode(), null);
            }



            //Map<String,Object> control = new HashMap<>();
            JSONObject control = new JSONObject();
            control.put("ref",ref);control.put("wcnN",redisMap.getString("wcnN"));
            control.put("bcdLevel",bcdLevel);control.put("wn0buyUser",redisMap.getString("wn0buyUser"));
            control.put("id_P",redisMap.getString("id_P"));control.put("wn2PaidPrice",redisMap.getDouble("wn2PaidPrice"));
            control.put("wn2EstPrice",redisMap.getDouble("wn2EstPrice"));control.put("lCR",redisMap.getString("lCR"));
            control.put("amk",redisMap.getString("amk"));control.put("tmk",redisMap.getString("tmk"));
            control.put("tfin",redisMap.getString("tfin"));control.put("id_U",new JSONArray().fluentAdd(id_U));
            //control.put("bcdState",1);control.put("pcState",0);

            //添加control
            mongoTemplate.updateFirst(new Query(
                    new Criteria("info.id_C").is(id_C)
                            .and("info.ref").is("a-module")), new Update().push("control.objData", control), Asset.class);

            //查询用户  rolex
            Query rolexQ =  new Query(
                    new Criteria("_id").is(id_U));
            rolexQ.fields().include("rolex.objComp."+id_C);

            User user = mongoTemplate.findOne(rolexQ, User.class);

            JSONObject indexMap = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);


            JSONArray objMod = indexMap.getJSONArray("objMod");


            JSONObject module = new JSONObject(4);
            module.put("bcdState", 1);
            module.put("tfin", redisMap.getString("tfin"));
            module.put("bcdLevel", bcdLevel);
            module.put("ref", ref);

            objMod.add(module);

            //添加rolex
            mongoTemplate.updateFirst(rolexQ, new Update().set("rolex.objComp."+id_C,indexMap), User.class);


            //添加role.objAuth
            this.obtainObjAuth(ref, bcdLevel,id_C);

            //获取init模块信息
            Query Qcn_java = new Query(new Criteria("_id").is("cn_java"));
            query.fields().include("newComp");
            InitJava init = mongoTemplate.findOne(Qcn_java, InitJava.class);


            //a-xxx
            JSONObject object = init.getNewComp().getJSONObject(ref);
            object.getJSONObject("info").put("id_C",id_C);

            //调用
            this.createAsset(id_C, MongoUtils.GetObjectId(), ref, JSON.toJSONString(object));


            //生成order订单  未做

            return retResult.ok(CodeEnum.OK.getCode(),null);


        }

        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.NO_CHARGE_USER.getCode(), null);

    }



    private void obtainObjAuth(String ref ,Integer bcdLevel,String id_C) {

        List<String> listType = new LinkedList<>();
        listType.add("lBUser");listType.add("lSOrder");listType.add("lSAsset");
        listType.add("lBProd");listType.add("lBOrder");listType.add("lSComp");
        listType.add("lSProd");listType.add("lBComp");

        //默认职位
        String grpU = "1001";


        // 获取模块的初始化数据
        Query initQ = new Query(new Criteria("_id").is("cn_java"));
        initQ.fields().include("listTypeInit").include("cardInit").include("batchInit");
        JSONObject initJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(initQ, InitJava.class));

        //列表可用卡片
        JSONObject listTypeInit = initJson.getJSONObject("listTypeInit");
        //模块可用卡片和等级
        JSONObject cardInit = initJson.getJSONObject("cardInit");
        //模块可用按钮和等级
        JSONObject batchInit = initJson.getJSONObject("batchInit");



        Query authQ = new Query(
                new Criteria("info.id_C").is(id_C)
                        .and("info.ref").is("a-auth"));
        authQ.fields().include("role");
        authQ.fields().include("def");
        Asset asset = mongoTemplate.findOne(authQ, Asset.class);

        JSONObject objGrpU = asset.getRole().getJSONObject("objAuth").getJSONObject(grpU);


        for (int i = 0; i < listType.size(); i++) {

            // 符合模块等级和编号的 卡片列表最终返回
            JSONArray resultCardArray = new JSONArray();
            // 符合模块等级和编号的 按钮列表最终返回
            JSONArray resultBatchArray = new JSONArray();
            // 循环获取这个列表类型的卡片对象
            for (Object cardKey : listTypeInit.getJSONObject(listType.get(i)).getJSONArray("card")) {

                String cardRef = cardKey.toString();

                //从listTypeInit里拿所有列表的card去对应cardInit里面的card名字，有符合的就添加到新数组
                JSONObject cardJson = cardInit.getJSONObject(cardRef);


                //卡片列表最终返回   购买模块的编号与等级 和 数据库模块编号与等级一致  取出来，
                if (cardJson != null && ref.equals(cardJson.getString("modRef")) && bcdLevel.equals(cardJson.getInteger("bcdLevel"))) {


                    JSONObject cardMap = new JSONObject();

                    cardMap.put("ref",cardJson.getString("ref"));
                    cardMap.put("modRef",cardJson.getString("modRef"));
                    cardMap.put("bcdLevel",cardJson.getInteger("bcdLevel"));
                    cardMap.put("auth", cardJson.getJSONObject("defRole").getInteger("auth"));
                    resultCardArray.add(cardMap);

                }
            }
            // 循环获取这个列表类型的按钮对象
            for (Object batchKey : listTypeInit.getJSONObject(listType.get(i)).getJSONArray("batch")) {



                String batchRef = batchKey.toString();

                //从listTypeInit里拿所有列表的batch去对应cardInit里面的batch名字，有符合的就添加到新数组
                JSONObject batchJson = batchInit.getJSONObject(batchRef);


                if (batchJson != null && ref.equals(batchJson.getString("modRef")) && bcdLevel.equals(batchJson.getInteger("bcdLevel"))) {

                    JSONObject batchMap = new JSONObject();

                    batchMap.put("ref",batchJson.getString("ref"));
                    batchMap.put("modRef",batchJson.getString("modRef"));
                    batchMap.put("bcdLevel",batchJson.getInteger("bcdLevel"));
                    batchMap.put("auth", batchJson.getJSONObject("defRole").getInteger("auth"));
                    resultBatchArray.add(batchMap);

                }
            }
            //def某个listType数组
            JSONArray listTypeArray = asset.getDef().getJSONArray("obj" + listType.get(i));
            //循环获取def的编号，与role对应
            for (int j = 0; j < listTypeArray.size(); j++) {

                //获取def里面的组别对象
                JSONObject indexDefMap = listTypeArray.getJSONObject(j);
                //拿def里的ref  点role.objAuth.1001.listType.def的编号（role.objAuth.1001.listType.1001）
                JSONObject grpObj = objGrpU.getJSONObject(listType.get(i)).getJSONObject(indexDefMap.getString("ref"));

                //如果点对象是空，则增加
                if(grpObj == null){

                    JSONObject newGpr = new JSONObject();

                    newGpr.put("card", resultCardArray);
                    newGpr.put("batch", resultBatchArray);
                    objGrpU.getJSONObject(listType.get(i)).put(indexDefMap.get("ref").toString(),newGpr);
                    continue;

                }

                JSONArray cardArray  = grpObj.getJSONArray("card");

                JSONArray batchArray  = grpObj.getJSONArray("batch");


                cardArray.addAll(resultCardArray);
                batchArray.addAll(resultBatchArray);


            }

        }



        mongoTemplate.updateFirst(authQ,new Update().set("role.objAuth."+grpU,objGrpU),Asset.class);



    }


    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse addBlankComp(String uid, JSONObject reqJson) throws IOException {

        String new_id_C = MongoUtils.GetObjectId();

        //获取模块信息
        Query query = new Query(new Criteria("_id").is("cn_java"));
        query.fields().include("newComp");
        InitJava init = mongoTemplate.findOne(query, InitJava.class);
        JSONObject newComp = init.getNewComp();

        Comp comp = JSONObject.parseObject(JSON.toJSONString(newComp.getJSONObject("comp")),Comp.class);


        //如果reqJson为空，则添加默认公司，否则从reqJson里面取公司基本信息
        if (!reqJson.isEmpty()){
            //用户填写公司信息
            comp.getInfo().setWrdN(reqJson.getJSONObject("wrdN"));
            comp.getInfo().setWrddesc(reqJson.getJSONObject("wrddesc"));
            comp.getInfo().setPic(reqJson.getString("pic"));
            comp.getInfo().setRef(reqJson.getString("ref"));
        }

        comp.getInfo().setId_CP(new_id_C);
        comp.getInfo().setId_C(new_id_C);
        comp.getInfo().setTmk(DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        comp.getInfo().setTmd(DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));

        //真公司标志
        comp.setBcdNet(1);
//        comp.setInfo(info);
        comp.setId(new_id_C);
//        comp.setView( compJSON.getJSONArray("view"));
        mongoTemplate.insert(comp);

        //真公司，增ES，lNComp
        comp.getInfo().setId_C(new_id_C);
        comp.getInfo().setId_CP(new_id_C);
//        dbUtils.addES(comp.getInfo(), "lncomp");


        //a-module
        JSONObject moduleObject = newComp.getJSONObject("a-module");
        moduleObject.getJSONObject("info").put("id_C",new_id_C);
        moduleObject.getJSONObject("info").put("tmk", DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
        moduleObject.getJSONObject("info").put("tmd", DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
        //control.id_U
        JSONArray objDataList = moduleObject.getJSONObject("control").getJSONArray("objData");

        //Map<String, Object> rolex = new HashMap<>();
        JSONObject rolex = new JSONObject(4);
        //List objMod = new ArrayList<>();
        JSONArray objMod = new JSONArray();
        for (int i = 0; i < objDataList.size(); i++) {

            JSONObject indexObj =  objDataList.getJSONObject(i);

            JSONArray usersArray =  indexObj.getJSONArray("id_U");

            usersArray.add(uid);

            //rolex.objComp.objMod.module
            //Map<String, Object> module = new HashMap<>();
            JSONObject module = new JSONObject(4);
            module.put("bcdState", indexObj.get("bcdState"));
            module.put("tfin", indexObj.get("tfin"));
            module.put("bcdLevel", indexObj.get("bcdLevel"));
            module.put("ref", indexObj.get("ref"));
            objMod.add(module);
        }

        String id_A_aModule = MongoUtils.GetObjectId();
        System.out.println("amod_ID  :"+id_A_aModule);

        //调用.createAsset
        this.createAsset(new_id_C, id_A_aModule ,"a-module",JSON.toJSONString(moduleObject));
        //rolex   bug:lrefRole不知道在哪里拿
        rolex.put("objMod",objMod);
        rolex.put("id_C",new_id_C);
        rolex.put("grpU","1001");
        //rolex.put("ref",comp.getInfo().get("ref"));
//        Update updateUser = new Update();
//        updateUser.push("rolex.objComp", rolex);
//        mongoTemplate.updateFirst(new Query(new Criteria("_id").is(uid)), updateUser, User.class);
        mongoTemplate.updateFirst(new Query(new Criteria("_id").is(uid)), new Update().set("rolex.objComp."+new_id_C, rolex), User.class);

//        //增加lBUser
        Query infoQ = new Query(new Criteria("_id").is(uid));
        infoQ.fields().include("info");

        User user = mongoTemplate.findOne(infoQ, User.class);

        //Map<String, Object> infoData = new HashMap<>();
        JSONObject infoData = new JSONObject();
        infoData.put("id_U",uid);
        infoData.put("wrdN",user.getInfo().getWrdN());
        infoData.put("pic",user.getInfo().getPic());
        infoData.put("tmd",DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        infoData.put("tmk",DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
        infoData.put("id_CB",new_id_C);
        infoData.put("wrdNReal",user.getInfo().getWrdNReal());
        infoData.put("refU","");
        infoData.put("grpU","1001");
        infoData.put("refC",comp.getInfo().getRef());
        infoData.put("wrdNC",comp.getInfo().getWrdN());


        dbUtils.addES( infoData, "lbuser");


        //a-auth
        JSONObject authObject = newComp.getJSONObject("a-auth");
        authObject.getJSONObject("role").getJSONObject("objAuth").putAll(setRole(new_id_C));
//        authObject.getJSONObject("def").put("id_UM",uid);//添加root id_U
        authObject.getJSONObject("info").put("id_C",new_id_C);
        authObject.getJSONObject("info").put("tmk", DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
        authObject.getJSONObject("info").put("tmd", DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));



        JSONArray flowList = authObject.getJSONObject("flowControl").getJSONArray("objData");
        for (Integer i = 0; i < flowList.size(); i++)
        {
            JSONObject userFlowData = new JSONObject();
            userFlowData.put("id_U", uid);
            userFlowData.put("id_APP", user.getInfo().getId_APP());
            userFlowData.put("imp", 3);
            flowList.getJSONObject(i).getJSONArray("objUser").add(userFlowData);
        }




        //authObject.getJSONObject("info").put("wrdNC",comp.getInfo().get("wrdN"));

        //调用
        this.createAsset(new_id_C, MongoUtils.GetObjectId() ,"a-auth",JSON.toJSONString(authObject));

//        //a-core
//        JSONObject coreObject = newComp.getJSONObject("a-core");
//        coreObject.getJSONObject("info").put("id_C",new_id_C);
//        coreObject.getJSONObject("info").put("tmk", DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
//        coreObject.getJSONObject("info").put("tmd", DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
//
//        //coreObject.getJSONObject("info").put("wrdNC",comp.getInfo().get("wrdN"));
//
//        //调用
//        this.createAsset(new_id_C, MongoUtils.GetObjectId() ,"a-core",JSON.toJSONString(coreObject));
//


        return retResult.ok(CodeEnum.OK.getCode(),new_id_C);

    }


    private JSONObject setRole (String id_C) {

        String listType = "lSAsset";

        String grp = "1009";

        String grpU = "1000";

        // 获取模块的初始化数据
        Query initQ = new Query(new Criteria("_id").is("cn_java"));
        initQ.fields().include("listTypeInit").include("cardInit").include("batchInit");
        InitJava initJava = mongoTemplate.findOne(initQ, InitJava.class);
        //JSONObject initJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(initQ, InitJava.class));



        // 先获取该用户已拥有的模块
        String id_A = dbUtils.getId_A(id_C, "a-module");
        Query myModQ = new Query(new Criteria("_id").is(id_A));
        myModQ.fields().include("control");
        Asset asset = mongoTemplate.findOne(myModQ, Asset.class);

        System.out.println("asset = "+ asset);
        //JSONObject controlJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(myModQ, Asset.class));

        JSONArray myModArray = asset.getControl().getJSONArray("objData");



        JSONObject listTypeInit = initJava.getListTypeInit();
        JSONObject cardInit = initJava.getCardInit();
        JSONObject batchInit = initJava.getBatchInit();


        // 初始化该卡片对象数组
        List<JSONObject> cardList = new ArrayList<>();

        // 循环获取这个列表类型的卡片对象
        for (Object cardKey : listTypeInit.getJSONObject(listType).getJSONArray("card")) {

            String cardRef = cardKey.toString();

            JSONObject cardJson = cardInit.getJSONObject(cardRef);
            cardList.add(cardJson);

        }



        // 初始化该卡片对象数组
        List<JSONObject> batchList = new ArrayList<>();

        // 循环获取这个列表类型的卡片对象
        for (Object batchKey : listTypeInit.getJSONObject(listType).getJSONArray("batch")) {

            String batchRef = batchKey.toString();

            JSONObject batchJson = batchInit.getJSONObject(batchRef);
            batchList.add(batchJson);

        }




        // 卡片列表最终返回
        JSONArray resultCardArray = new JSONArray();

        for (JSONObject cardJson : cardList) {

            for (Object modObj : myModArray) {

                JSONObject modJson = (JSONObject) modObj;


                if (modJson.getString("ref").equals(cardJson.getString("modRef")) && modJson.getInteger("bcdLevel").equals(cardJson.getInteger("bcdLevel"))) {

//                    cardJson.put("auth", cardJson.getJSONObject("defRole").getInteger("auth"));
//                    cardJson.remove("defRole");

                    resultCardArray.add(new JSONObject().fluentPut("ref",cardJson.getString("ref"))
                            .fluentPut("modRef",cardJson.getString("modRef"))
                            .fluentPut("auth",cardJson.getJSONObject("defRole").getInteger("auth"))
                            .fluentPut("bcdLevel",cardJson.getInteger("bcdLevel")));

                }

            }

        }

        // 卡片列表最终返回
        JSONArray resultBatchArray = new JSONArray();

        for (JSONObject batchJson : batchList) {

            for (Object modObj : myModArray) {

                JSONObject modJson = (JSONObject) modObj;

                if (modJson.getString("ref").equals(batchJson.getString("modRef")) && modJson.getInteger("bcdLevel").equals(batchJson.getInteger("bcdLevel"))) {

//                    batchJson.put("auth", batchJson.getJSONObject("defRole").getInteger("auth"));
//                    batchJson.remove("defRole");

                    resultBatchArray.add(new JSONObject().fluentPut("ref",batchJson.getString("ref"))
                            .fluentPut("modRef",batchJson.getString("modRef"))
                            .fluentPut("auth",batchJson.getJSONObject("defRole").getInteger("auth"))
                            .fluentPut("bcdLevel",batchJson.getInteger("bcdLevel")));

                }

            }

        }






        JSONObject result = new JSONObject();
        result.put("card", resultCardArray);
        result.put("batch", resultBatchArray);

        JSONObject grpJson = new JSONObject();
        grpJson.put(grp, result);

        JSONObject listTypeJson = new JSONObject();
        listTypeJson.put(listType, grpJson);

        JSONObject grpUJson = new JSONObject();
        grpUJson.put(grpU, listTypeJson);

        return grpUJson;

    }


    private JSONObject createAsset(String id_C,String id ,String ref,String data) throws IOException {

        //状态结果对象
        JSONObject resultJson = new JSONObject();

        try {
//            JSONObject.parseObject(JSON.toJSONString(objAction
            JSONObject objData = JSONObject.parseObject(data);
//            objData.getJSONObject("info").put("ref",ref);
            // 获取data里面的info数据,这个给ES



            // 获取data里面的info数据，这个给mongdb
           // AssetInfo infoObject = JSONObject.parseObject(JSON.toJSONString(objData.getJSONObject("info"));

            //查找当前公司，获取公司信息
            //Query compCondition = new Query(new Criteria("_id").is(infoObject.get("id_CB")).and("info").exists(true));
            Query compCondition = new Query(new Criteria("_id").is(id_C).and("info").exists(true));

            compCondition.fields().include("info");
            Comp objComp = mongoTemplate.findOne(compCondition, Comp.class);
            if(objComp == null){
                System.out.println("no comp");
                resultJson.put("boolean","false");
                resultJson.put("reason","comp对象为空");

            }
            Asset asset = JSONObject.parseObject(JSON.toJSONString(objData), Asset.class);

            asset.setId(id);
            System.out.println("start making Asset");


            mongoTemplate.insert(asset);
            System.out.println("ok inserted  "+ id);

            //指定ES索引
            IndexRequest request = new IndexRequest("lSAsset");

            //ES列表
            JSONObject listObject = new  JSONObject();
            listObject.putAll( objData.getJSONObject("info"));
            listObject.put("id_A", id);
            listObject.put("tmk", DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
            listObject.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
            listObject.put("id_CP", objComp.getInfo().getId_CP());

            request.source(listObject, XContentType.JSON);
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
//            if (infoObject.get("lAT").equals(2)){

//                //拿info位置数组
//                JSONArray refSpaceList = infoObject.getJSONArray("refSpace");
//                if(refSpaceList.size() > 0){
//                    //批量修改
//                    List<Pair<Query, Update>> updateList = new ArrayList<>();
//                    BulkOperations operations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Asset.class);
//                    for (int i = 0; i < refSpaceList.size(); i++) {
//                        Query storageQ = new Query(Criteria.where("info.ref").is("a-storage").and("info.id_C").is(id_C).and("locSetup.locData.refSpace").is(refSpaceList.get(i)));
//                        storageQ.fields().include("locSetup.locData.$");
//
//                        Update update = new Update();
//                        update.set("locSetup.locData.$.id_A",id);
//                        Pair<Query, Update> updatePair = Pair.of(storageQ, update);
//                        updateList.add(updatePair);
//                    }
//                    //批量修改
//                    operations.updateMulti(updateList);
//                    BulkWriteResult result = operations.execute();
//                    //插入计数 insertedCount=0,匹配计数 matchedCount=300,删除计数 removedCount=0,被改进的 modifiedCount=1
//
//                }

//                JSONObject assetflow = new JSONObject();
//                //前端传空会报错
//                assetflow.put("wn2qtychg",infoObject.get("wn2qty"));
//                assetflow.put("subtype",0);assetflow.put("id_to",id);assetflow.put("id_from","");
//                assetflow.put("id_P",infoObject.get("id_P"));assetflow.put("id_O",infoObject.get("id_O"));
//                assetflow.put("id_C",id_C);assetflow.put("ref",infoObject.get("ref"));
//                assetflow.put("wrdN",infoObject.getJSONObject("wrdN"));assetflow.put("pic",infoObject.get("pic"));
//                assetflow.put("wrddesc",infoObject.getJSONObject("wrddesc"));
//                //前端传空会报错  应该这里double强转，没有值会报错
//                assetflow.put("wn4price",infoObject.getDouble("wn4price"));
//                assetflow.put("wn2qc",infoObject.get("wn2qc"));//hashMap.put("refSpace", infoObject.get("refSpace"));
//                assetflow.put("grpU","");assetflow.put("grpUB",id_U);
//                assetflow.put("tmk",DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
//                assetflow.put("tmd",DateUtils.getDateNow(DateEnum.DATE_TWO.getDate()));
//
//
//                dbUtils.addES(assetflow,"assetflow");



//            }
            System.out.println("All good");

            resultJson.put("boolean","true");
            resultJson.put("reason",id);

        } catch (Exception e) {
            System.out.println("caught Ex");


            resultJson.put("boolean","false");
            resultJson.put("reason","添加内部失败");
            //return RetResult.errorJsonResult(HttpStatus.INTERNAL_SERVER_ERROR, SingleEnum.ASSET_ADD_ERROR.getCode(),null);


        }


        return resultJson;
    }



}
