package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.ModuleService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.es.lBProd;
import com.cresign.tools.pojo.es.lBUser;
import com.cresign.tools.pojo.es.lNComp;
import com.cresign.tools.pojo.es.lSBComp;
import com.cresign.tools.pojo.po.*;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateBatchRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateBatchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@Service
public class ModuleServicelmpl implements ModuleService {

    @Value("${secret.id}")
    private String secretId;

    @Value("${secret.key}")
    private String secretKey;

    @Autowired
    private Qt qt;

    @Autowired
    private RetResult retResult;


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
//    @Override
//    public ApiResponse addOrUpdateInitMod(JSONObject objLogMod) {
//        // 获取init信息
//        Init init = qt.getInitData("cn");
//        // 获取日志模块信息
//        JSONObject logInit = init.getLogInit();
//        // 遍历模块信息
//        objLogMod.keySet().forEach(k -> {
//            // 获取模块对象
//            JSONObject jsonObject = objLogMod.getJSONObject(k);
//            // 添加模块
//            logInit.put(k,jsonObject);
//        });
//        // 更新数据库
//        coupaUtil.updateInitLog(logInit);
//        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
//    }

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
                Init init = qt.getInitData("cn");
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
                        // 调用方法更新数据
                        setAndUpdateAuth(listTypeGrp,modListNew,authListType,authGrpU
                                ,objAuth,role,assetIdRole,grpU,listType,grp);

                    }
                    result.put("errRole",errRole);
                }

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
                , getStrIsNull(lsprod.getString("pic")),lsprod.getString("refDC")
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

        // objUser = {"id_U" :{a-core-0:"add"}}
        JSONObject result = new JSONObject();
//        JSONArray reAddArr = userT(can.getJSONObject("objUser"), id_C);
        // 调用核心方法，获取返回结果
        JSONArray reAddArr = new JSONArray();
        // 遍历用户集合
        objUser.keySet().forEach(id_U -> updateUserModAuth(id_U,reAddArr,id_C,objUser));


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
    public ApiResponse modSetControl(String authComp, String id_C,JSONObject objModQ) {

        if (!authComp.equals("61a5940b01902729e2576ead") && !authComp.equals("6141b6797e8ac90760913fd0"))
        {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.MODUL_NO_HAVE.getCode(), "无匹配的modRef");
        }
        // 调用方法获取公司模块信息
        JSONObject compAssetMod = getCompAssetByRef(id_C,"a-core","control");
        // 获取错误状态，为0是没有错误
//        Integer status = compAssetMod.getInteger("status");
            // 定义存储错误信息json集合
            JSONObject control = compAssetMod.getJSONObject("control");
            String assetId = compAssetMod.getString("assetId");

            qt.errPrint("ass", null, control, assetId);
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
                    InitJava init = qt.getInitData();
                    if (mod.getBoolean("setA"))
                    {
                        //a-mes
                        String modRef = mod.getJSONObject("val").getString("mod");
                        Info initComp = qt.getMDContent(qt.idJson.getString("newComp"), "jsonInfo", Info.class);

                        JSONObject authObject = initComp.getJsonInfo().getJSONObject("objData").getJSONObject(modRef);

                        authObject.getJSONObject("info").put("id_C", id_C);
                        authObject.getJSONObject("info").put("id_CP", id_C);
                        authObject.getJSONObject("info").put("tmk", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                        authObject.getJSONObject("info").put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                        this.createAsset(id_C, qt.GetObjectId() ,modRef,authObject);
                    }

                    objMod.put(k,mod.getJSONObject("val"));
                } else if ("del".equals(type)) {
                    // 删除模块信息
                    objMod.remove(k);
                } else if ("upSpec".equals(type)) {
                    String tfin = mod.getJSONObject("val").getString("tfin");
                    Integer buyUser = mod.getJSONObject("val").getInteger("wn0buyUser");

                    JSONObject modJson = objMod.getJSONObject(k);
                    modJson.put("tfin",tfin);
                    modJson.put("wn0buyUser", buyUser);
                    objMod.put(k,modJson);

                } else if ("upState".equals(type)) {
                    JSONArray userList = mod.getJSONObject("val").getJSONArray("id_U");
                    Integer bcdState = mod.getJSONObject("val").getInteger("bcdState");
                    if (null != userList) {
                        JSONArray resultArr = new JSONArray();
                        for (int i = 0; i < userList.size(); i++) {
                            String id_U = userList.getString(i);
//                            updateUserModAuth(string,resultArr,id_C,null,"setComp",k,bcdState);
                            setAuthStatus(id_U,id_C,k,bcdState);
                        }
                    }
                    JSONObject jsonObject = objMod.getJSONObject(k);
                    jsonObject.put("bcdState",bcdState);
                    objMod.put(k,jsonObject);

                } else {
                    JSONObject re = new JSONObject();
                    re.put("key",k);
                    re.put("err","修改状态为空");
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.MODUL_NO_HAVE.getCode(), "无匹配的modRef");
                }
            });

            qt.setMDContent(assetId,qt.setJson("control.objMod",objMod),Asset.class);
            return retResult.ok(CodeEnum.OK.getCode(), "");
        }
//        else {
//            return errResult(status,0);
//        }


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
        // 调用方法获取公司模块信息
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
//                , DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate())
//                , DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
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
                , picCB);
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
                        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.MODUL_NO_HAVE.getCode(), "无匹配的modRef");
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
//        listTypeGrp.put("log",logAuthList);
//        authListType.put(grp,listTypeGrp);
//        authGrpU.put(listType,authListType);
//        objAuth.put(grpU,authGrpU);
//
//        role.put("objAuth",objAuth);
//        // 定义存储flowControl字典
//        JSONObject mapKey = new JSONObject();
//        // 设置字段数据
//        mapKey.put("role",role);
        // 更新数据库
//        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetIdRole,mapKey);
        qt.setMDContent(assetIdRole,qt.setJson("role.objAuth."+grpU+"."+listType+"."+grp+".log",logAuthList),Asset.class);
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
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_NO_ASSET_ID.getCode(), "该公司没有assetId");
            case 2:
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_NO_ASSET.getCode(), "该公司没有asset");
            case 3:
                if (isOperation == 0) {
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.CARD_NO_HAVE.getCode(), "该公司没有control卡片");
                } else {
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.CARD_NO_HAVE.getCode(), "该公司没有role卡片");
                }
            case 4:
                if (isOperation == 0) {
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.CARD_NO_HAVE.getCode(), "该公司control卡片异常");
                } else {
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ROLE_UP_ERROR.getCode(), "该公司role卡片异常");
                }
            default:
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_UNKNOWN.getCode(), "接口未知异常");
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
        Asset asset = qt.getConfig(id_C, assetRef, card);
        if ("a-core".equals(assetRef)) {
            JSONObject control = asset.getControl();

            if (null == control || null == control.getJSONObject("objMod")) {
//                result.put("status",4);
                Info initMod = qt.getMDContent(qt.idJson.getString("newComp"), "jsonInfo", Info.class);
//                InitJava initMod = qt.getMDContent("cn_java", "newComp.a-core.control.objMod", InitJava.class);
                control = initMod.getJsonInfo().getJSONObject("objData").getJSONObject("a-core").getJSONObject("control");
                qt.setMDContent(asset.getId(), qt.setJson("control", control), Asset.class);
            }
            result.put("control", control);
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
        result.put("assetId",asset.getId());
        return result;
    }
   

    /**
     * 新增或删除用户的模块使用权-核心方法
     * @param id_U	用户编号
     * @param resultArr	返回结果集合
     * @param id_C	公司编号
     * @param objUser	用户集合
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    private void updateUserModAuth(String id_U,JSONArray resultArr,String id_C,JSONObject objUser){

        User user = qt.getMDContent(id_U, "rolex", User.class);
        JSONObject result;
        if (null == user || user.getRolex() == null ||
                user.getRolex().getJSONObject("objComp").getJSONObject(id_C) == null) {
            result = new JSONObject();
            result.put("id_U",id_U);
            result.put("desc","用户信息为空");
            resultArr.add(result);
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.REDIS_ORDER_NO_HAVE.getCode(), "");
        }
        JSONObject rolexData = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);

//        else {
//            JSONObject rolex = user.getRolex();
//            if (null == rolex) {
//                result = new JSONObject();
//                result.put("id_U",id_U);
//                result.put("desc","用户权限卡片为空");
//                resultArr.add(result);
//            } else {
//                // 获取公司权限信息集合
//                JSONObject rolexData = rolex.getJSONObject("objComp").getJSONObject(id_C);
//                // 获取当前处理公司权限信息
////                JSONObject rolexData = rolexData.getJSONObject(id_C);
//                if (null == rolexData) {
//                    result = new JSONObject();
//                    result.put("id_U",id_U);
//                    result.put("desc","用户不在当前公司");
//                    resultArr.add(result);
//                } else {
                    // 判断操作状态为0
//        if (operation.equals("setUser")) {
            // 调用方法获取公司模块信息
            JSONObject compAssetMod = getCompAssetByRef(id_C,"a-core","control");
            // 获取错误状态，为0是没有错误
            if (0 == compAssetMod.getInteger("status")) {
                // 获取卡片信息
                JSONObject control = compAssetMod.getJSONObject("control");
                // 获取资产编号
                String assetId = compAssetMod.getString("assetId");
                // 获取模块信息
                JSONObject objMod = control.getJSONObject("objMod");

                // 获取修改当前用户的模块信息
                JSONObject modToUpdate = objUser.getJSONObject(id_U);


                // 获取当前公司的模块信息
                JSONObject modAuth = rolexData.getJSONObject("modAuth") == null ? new JSONObject() :
                        rolexData.getJSONObject("modAuth");
                // 遍历修改当前用户的模块信息
                modToUpdate.keySet().forEach(mod_level -> {
                    // 获取卡片的模块信息
                    JSONObject pickedMod = objMod.getJSONObject(mod_level);
//                                // 定义存储模块用户信息
                    if (null != pickedMod) {
                        // 获取操作类型

                        String type = modToUpdate.getString(mod_level);
                        JSONArray userList = pickedMod.getJSONArray("id_U");
                        JSONObject val = new JSONObject();
                        val.put("tfin",pickedMod.getString("tfin"));
                        val.put("bcdStatus",pickedMod.getInteger("bcdState"));
                        val.put("bcdLevel",pickedMod.getInteger("bcdLevel"));
                        val.put("ref",pickedMod.getString("ref"));
                        val.put("mod",pickedMod.getString("mod"));

                        // 判断为删除状态
                        if ("del".equals(type) && modAuth.containsKey(mod_level)) {
                            // 删除当前模块
                            modAuth.remove(mod_level);
                            // 定义存储要删除的下标，默认-1
//                            int currentUser = -1;
                            userList.remove(id_U);
                            pickedMod.put("id_U",userList);
                            objMod.put(mod_level,pickedMod);


//                            // 遍历用户集合
//                            for (int i = 0; i < userList.size(); i++) {
//                                // 根据下标获取用户id
//                                String id_UN = userList.getString(i);
//                                // 判断等于当前用户
//                                if (id_UN.equals(id_U)) {
//                                    // 赋值下标
//                                    currentUser = i;
//                                }
//                            }
//                            if (currentUser != -1) {
//                                // 根据下标删除信息
//                                userList.remove(currentUser);
//                                pickedMod.put("id_U",userList);
//                                objMod.put(mod_level,pickedMod);
//                            }
//                                        if (isNull) {
////                                        JSONArray userList = pickedMod.getJSONArray("id_U");
//
//                                        }
                        }
                        else // Adding now
                        {
                            // 获取当前模块键是否存在
                            boolean isModAuth = modAuth.containsKey(mod_level);
                            // 修改当前模块
                            modAuth.put(mod_level,val);

                            // 判断模块键不存在
                            if (!isModAuth && !userList.contains(id_U)) {
                                // 定义存储，当前用户是否存在，默认不存在
//                                boolean isExistence = false;
//
//                                if (!userList.contains(id_U))
//                                {
                                String [] compare1 = mod_level.split("-");
                                for (String modKey : objMod.keySet())
                                {
                                    if (objMod.getJSONObject(modKey).getInteger("wn0buyUser").equals(objMod.getJSONObject(modKey).getJSONArray("id_U").size()))
                                    {
                                        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.NO_CHARGE_USER.getCode(), "");
                                    }
                                    String [] compare2 = modKey.split("-"); // a-core-2 splited[1] = core
                                    // looped thru control and then I found same "core" and id_U is also in it
                                    if (compare1[1].equals(compare2[1]) && objMod.getJSONObject(modKey).getJSONArray("id_U").contains(id_U))
                                    {
                                        //I found out that I am in another modKey!!
                                        //1. delete myself from that modKey
                                        objMod.getJSONObject(modKey).getJSONArray("id_U").remove(id_U);
                                        //2. delete my own mod from my modAuth
                                        modAuth.remove(modKey);
                                    }
                                }
                                    userList.add(id_U);
                                    pickedMod.put("id_U",userList);
                                    objMod.put(mod_level,pickedMod);


//                                }

//                                // 遍历用户集合
//                                for (int i = 0; i < userList.size(); i++) {
//                                    // 根据下标获取用户id
//                                    String id_UN = userList.getString(i);
//                                    // 判断等于当前用户id
//                                    if (id_UN.equals(id_U)) {
//                                        isExistence = true;
//                                        break;
//                                    }
//                                }
//                                // 判断不存在
//                                if (!isExistence) {
//                                    // 添加用户id
//                                    userList.add(id_U);
//                                    pickedMod.put("id_U",userList);
//                                    objMod.put(mod_level,pickedMod);
//                                }
                            }
                        }
                    } else {
                        JSONObject resultErr = new JSONObject();
                        resultErr.put("id_U",id_U);
                        resultErr.put("desc","该公司当前模块为空，模块名称:"+mod_level);
                        resultArr.add(resultErr);
                    }
                });

                //update Control, and Rolex
                qt.setMDContent(assetId,qt.setJson("control.objMod",objMod),Asset.class);

                qt.setMDContent(id_U, qt.setJson("rolex.objComp."+id_C+".modAuth",modAuth), User.class);


            } else {
                result = new JSONObject();
                result.put("id_U",id_U);
                result.put("desc","获取公司信息异常");
                resultArr.add(result);
            }
//
//        }
//
//
//        else if (operation.equals("setComp")){
//            // this ONLY reset the status
//            // 获取当前公司的模块信息
//            JSONObject modAuth = rolexData.getJSONObject("modAuth");
//            JSONObject mod = modAuth.getJSONObject(modName);
//            mod.put("bcdStatus",typeData);
//            // 修改当前模块
//            modAuth.put(modName,mod);
//            qt.setMDContent(id_U, qt.setJson("rolex.objComp."+id_C+".modAuth",modAuth),User.class);
//        }
////                }
////            }
////        }
    }

    private void setAuthStatus(String id_U,String id_C, String modName,Integer bcdStatus) {

        User user = qt.getMDContent(id_U, "rolex", User.class);
        if (null == user || user.getRolex() == null ||
                user.getRolex().getJSONObject("objComp").getJSONObject(id_C) == null) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.REDIS_ORDER_NO_HAVE.getCode(), "");
        }
        JSONObject rolexData = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);
        // this ONLY reset the status
        // 获取当前公司的模块信息
        JSONObject modAuth = rolexData.getJSONObject("modAuth");
        JSONObject mod = modAuth.getJSONObject(modName);
        mod.put("bcdStatus", bcdStatus);
        // 修改当前模块
        modAuth.put(modName, mod);
        qt.setMDContent(id_U, qt.setJson("rolex.objComp." + id_C + ".modAuth", modAuth), User.class);
    }


//        @Override
//    public ApiResponse addModule(String id_U, String oid, String id_C, String ref, Integer bcdLevel) throws IOException {
//        //判断公司负责人
////        Query query = new Query(
////                new Criteria("info.id_C").is(id_C)
////                        .and("info.ref").is("a-auth"));
////        query.fields().include("def.id_UM");
////        Asset asset = mongoTemplate.findOne(query, Asset.class);
//        Asset asset = qt.getConfig(id_C,"a-auth","def");
//        if (asset != null || asset.getDef().get("id_UM").equals(id_U)) {
//
//            //查询redis订单信息
////            String order = redisTemplate0.opsForValue().get(oid);
//            String order = qt.getRDSetStr(oid);
//
//            JSONObject redisMap = (JSONObject) JSON.parse(order);
//
//            if (redisMap == null){
//
//                throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, PurchaseEnum.REDIS_ORDER_NO_HAVE.getCode(), null);
//            }
//
//
//
//            //Map<String,Object> control = new HashMap<>();
//            JSONObject control = new JSONObject();
//            control.put("ref",ref);control.put("wcnN",redisMap.getString("wcnN"));
//            control.put("bcdLevel",bcdLevel);control.put("wn0buyUser",redisMap.getString("wn0buyUser"));
//            control.put("id_P",redisMap.getString("id_P"));control.put("wn2PaidPrice",redisMap.getDouble("wn2PaidPrice"));
//            control.put("wn2EstPrice",redisMap.getDouble("wn2EstPrice"));control.put("lCR",redisMap.getString("lCR"));
//            control.put("amk",redisMap.getString("amk"));control.put("tmk",redisMap.getString("tmk"));
//            control.put("tfin",redisMap.getString("tfin"));control.put("id_U",new JSONArray().fluentAdd(id_U));
//            //control.put("bcdState",1);control.put("pcState",0);
//
//            //添加control
////            mongoTemplate.updateFirst(new Query(
////                    new Criteria("info.id_C").is(id_C)
////                            .and("info.ref").is("a-core")), new Update().push("control.objMod", control), Asset.class);
//            Asset assetCore = qt.getConfig(id_C,"a-core","info");
//            if (null != assetCore) {
//                qt.setMDContent(assetCore.getId(),qt.setJson("control.objMod", control), Asset.class);
//            }
//
//            //查询用户  rolex
////            Query rolexQ =  new Query(
////                    new Criteria("_id").is(id_U));
////            rolexQ.fields().include("rolex.objComp."+id_C);
////
////            User user = mongoTemplate.findOne(rolexQ, User.class);
//            User user = qt.getMDContent(id_U,"rolex", User.class);
//
//            JSONObject indexMap = user.getRolex().getJSONObject("objComp").getJSONObject(id_C);
//
//
//            JSONArray objMod = indexMap.getJSONArray("objMod");
//
//
//            JSONObject module = new JSONObject(4);
//            module.put("bcdState", 1);
//            module.put("tfin", redisMap.getString("tfin"));
//            module.put("bcdLevel", bcdLevel);
//            module.put("ref", ref);
//
//            objMod.add(module);
//
//            //添加rolex
////            mongoTemplate.updateFirst(rolexQ, new Update().set("rolex.objComp."+id_C,indexMap), User.class);
//            qt.setMDContent(id_U,qt.setJson("rolex.objComp."+id_C,indexMap), User.class);
//
//
//            //添加role.objAuth
//            this.obtainObjAuth(ref, bcdLevel,id_C);
//
//            //获取init模块信息
////            Query Qcn_java = new Query(new Criteria("_id").is("cn_java"));
////            query.fields().include("newComp");
////            InitJava init = mongoTemplate.findOne(Qcn_java, InitJava.class);
//            InitJava init = qt.getMDContent("cn_java","newComp", InitJava.class);
//
//
//            //a-xxx
//            JSONObject object = init.getNewComp().getJSONObject(ref);
//            object.getJSONObject("info").put("id_C",id_C);
//
//            //调用
//            this.createAsset(id_C, qt.GetObjectId(), ref, object);
//
//
//            //生成order订单  未做
//
//            return retResult.ok(CodeEnum.OK.getCode(),null);
//
//
//        }
//
//        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.NO_CHARGE_USER.getCode(), null);
//
//    }



    private void obtainObjAuth(String ref ,Integer bcdLevel,String id_C) {

        List<String> listType = new LinkedList<>();
        listType.add("lBUser");listType.add("lSOrder");listType.add("lSAsset");
        listType.add("lBProd");listType.add("lBOrder");listType.add("lSComp");
        listType.add("lSProd");listType.add("lBComp");

        //默认职位
        String grpU = "1001";


        // 获取模块的初始化数据
//        Query initQ = new Query(new Criteria("_id").is("cn_java"));
//        initQ.fields().include("listTypeInit").include("cardInit").include("batchInit");
//        JSONObject initJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(initQ, InitJava.class));
        JSONObject initJson = (JSONObject) JSON.toJSON(qt.getMDContent("cn_java", qt.strList("listTypeInit", "cardInit", "batchInit"), InitJava.class));

        //列表可用卡片
        JSONObject listTypeInit = initJson.getJSONObject("listTypeInit");
        //模块可用卡片和等级
        JSONObject cardInit = initJson.getJSONObject("cardInit");
        //模块可用按钮和等级
        JSONObject batchInit = initJson.getJSONObject("batchInit");



//        Query authQ = new Query(
//                new Criteria("info.id_C").is(id_C)
//                        .and("info.ref").is("a-auth"));
//        authQ.fields().include("role");
//        authQ.fields().include("def");
//        Asset asset = mongoTemplate.findOne(authQ, Asset.class);
        Asset asset = qt.getConfig(id_C,"a-auth",qt.strList("role","def"));

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



//        mongoTemplate.updateFirst(authQ,new Update().set("role.objAuth."+grpU,objGrpU),Asset.class);
        qt.setMDContent(asset.getId(),qt.setJson("role.objAuth."+grpU,objGrpU), Asset.class);
    }


    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse addBlankComp(JSONObject tokData, JSONObject wrdN, JSONObject wrddesc,
                                    String pic, String ref) {

        String new_id_C = qt.GetObjectId();

//        InitJava init = qt.getInitData();

        Info init = qt.getMDContent(qt.idJson.getString("newComp"), "jsonInfo", Info.class);
        JSONObject newComp = init.getJsonInfo().getJSONObject("objData");
        Comp comp = qt.jsonTo(newComp.getJSONObject("comp"), Comp.class);
        String uid = tokData.getString("id_U");

        //如果reqJson为空，则添加默认公司，否则从reqJson里面取公司基本信息

        //用户填写公司信息
        comp.getInfo().setWrdN(wrdN);
        comp.getInfo().setWrddesc(wrddesc);
        comp.getInfo().setPic(pic);
        comp.getInfo().setRef(ref);


        comp.getInfo().setId_CP(new_id_C);
        comp.getInfo().setId_CM(new_id_C);
        comp.getInfo().setId_C(new_id_C);
        comp.getInfo().setTmk(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        comp.getInfo().setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

        //真公司标志
        comp.setBcdNet(1);
        comp.setId(new_id_C);
//        mongoTemplate.insert(comp);
        qt.addMD(comp);

        JSONObject rolex = new JSONObject(4);

        rolex.put("id_C",new_id_C);
        rolex.put("grpU","1001");
        rolex.put("dep","1000");

        //setting modAuth@rolex
        JSONObject val = new JSONObject();
        val.put("tfin",-1);
        val.put("bcdStatus",1);
        val.put("bcdLevel",3);
        val.put("ref","a-core-3");
        val.put("mod","a-core");
        JSONObject mod1 = new JSONObject();
        mod1.put("a-core-3", val);
        rolex.put("modAuth", mod1);

        qt.setMDContent(uid,qt.setJson("rolex.objComp."+new_id_C,rolex), User.class);

        //a-core
        JSONObject coreObject = newComp.getJSONObject("a-core");
        coreObject.getJSONObject("info").put("id_C",new_id_C);
        coreObject.getJSONObject("info").put("tmk", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        coreObject.getJSONObject("info").put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

        // add me into control's a-core-3 as the only User
        coreObject.getJSONObject("control").getJSONObject("objMod").getJSONObject("a-core-3").getJSONArray("id_U").add(uid);
        //调用
        this.createAsset(new_id_C, qt.GetObjectId() ,"a-core",coreObject);

        User user = qt.getMDContent(uid, "info", User.class);

        lBUser lbuser = new lBUser(uid,new_id_C,user.getInfo().getWrdN(),comp.getInfo().getWrdN(),
                user.getInfo().getWrdNReal(),user.getInfo().getWrddesc(),"1001",user.getInfo().getMbn(),
                "",user.getInfo().getId_WX(),user.getInfo().getPic(),"1000");

        qt.addES( "lbuser", lbuser);

        Comp cSeller = qt.getMDContent(tokData.getString("id_C"), "info", Comp.class);

        lNComp lncomp = new lNComp(new_id_C,new_id_C,comp.getInfo().getWrdN(),comp.getInfo().getWrddesc(),comp.getInfo().getRef(),comp.getInfo().getPic());

        lSBComp lsbcomp = new lSBComp(tokData.getString("id_C"),tokData.getString("id_C"),new_id_C,new_id_C, cSeller.getInfo().getWrdN(),cSeller.getInfo().getWrddesc(),
                comp.getInfo().getWrdN(),comp.getInfo().getWrddesc(),"1000","1000",comp.getInfo().getRef(),ref,cSeller.getInfo().getPic(),comp.getInfo().getPic());
        qt.addES("lncomp", lncomp);
        qt.addES("lsbcomp", lsbcomp);


        //a-auth
        JSONObject authObject = newComp.getJSONObject("a-auth");

        authObject.getJSONObject("info").put("id_C",new_id_C);
        authObject.getJSONObject("info").put("tmk", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        authObject.getJSONObject("info").put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

        JSONArray flowList = authObject.getJSONObject("flowControl").getJSONArray("objData");
        for (Integer i = 0; i < flowList.size(); i++)
        {
            JSONObject userFlowData = new JSONObject();
            userFlowData.put("id_U", uid);
            userFlowData.put("id_APP", user.getInfo().getId_APP());
            userFlowData.put("imp", 3);
            flowList.getJSONObject(i).getJSONArray("objUser").add(userFlowData);
        }

        //调用
        this.createAsset(new_id_C, qt.GetObjectId() ,"a-auth",authObject);

        return retResult.ok(CodeEnum.OK.getCode(),new_id_C);

    }


//    private JSONObject setRole (String id_C) {
//
//        String listType = "lSAsset";
//
//        String grp = "1009";
//
//        String grpU = "1000";
//
//        // 获取模块的初始化数据
//        Query initQ = new Query(new Criteria("_id").is("cn_java"));
//        initQ.fields().include("listTypeInit").include("cardInit").include("batchInit");
//        InitJava initJava = mongoTemplate.findOne(initQ, InitJava.class);
//        //JSONObject initJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(initQ, InitJava.class));
//
//
//
//        // 先获取该用户已拥有的模块
//        String id_A = qt.getId_A(id_C, "a-core");
//        Query myModQ = new Query(new Criteria("_id").is(id_A));
//        myModQ.fields().include("control");
//        Asset asset = mongoTemplate.findOne(myModQ, Asset.class);
//
//        System.out.println("asset = "+ asset);
//        //JSONObject controlJson = (JSONObject) JSON.toJSON(mongoTemplate.findOne(myModQ, Asset.class));
//
//        JSONArray myModArray = asset.getControl().getJSONArray("objMod");
//
//
//
//        JSONObject listTypeInit = initJava.getListTypeInit();
//        JSONObject cardInit = initJava.getCardInit();
//        JSONObject batchInit = initJava.getBatchInit();
//
//
//        // 初始化该卡片对象数组
//        List<JSONObject> cardList = new ArrayList<>();
//
//        // 循环获取这个列表类型的卡片对象
//        for (Object cardKey : listTypeInit.getJSONObject(listType).getJSONArray("card")) {
//
//            String cardRef = cardKey.toString();
//
//            JSONObject cardJson = cardInit.getJSONObject(cardRef);
//            cardList.add(cardJson);
//
//        }
//
//
//
//        // 初始化该卡片对象数组
//        List<JSONObject> batchList = new ArrayList<>();
//
//        // 循环获取这个列表类型的卡片对象
//        for (Object batchKey : listTypeInit.getJSONObject(listType).getJSONArray("batch")) {
//
//            String batchRef = batchKey.toString();
//
//            JSONObject batchJson = batchInit.getJSONObject(batchRef);
//            batchList.add(batchJson);
//
//        }
//
//
//
//
//        // 卡片列表最终返回
//        JSONArray resultCardArray = new JSONArray();
//
//        for (JSONObject cardJson : cardList) {
//
//            for (Object modObj : myModArray) {
//
//                JSONObject modJson = (JSONObject) modObj;
//
//
//                if (modJson.getString("ref").equals(cardJson.getString("modRef")) && modJson.getInteger("bcdLevel").equals(cardJson.getInteger("bcdLevel"))) {
//
////                    cardJson.put("auth", cardJson.getJSONObject("defRole").getInteger("auth"));
////                    cardJson.remove("defRole");
//
//                    resultCardArray.add(new JSONObject().fluentPut("ref",cardJson.getString("ref"))
//                            .fluentPut("modRef",cardJson.getString("modRef"))
//                            .fluentPut("auth",cardJson.getJSONObject("defRole").getInteger("auth"))
//                            .fluentPut("bcdLevel",cardJson.getInteger("bcdLevel")));
//
//                }
//
//            }
//
//        }
//
//        // 卡片列表最终返回
//        JSONArray resultBatchArray = new JSONArray();
//
//        for (JSONObject batchJson : batchList) {
//
//            for (Object modObj : myModArray) {
//
//                JSONObject modJson = (JSONObject) modObj;
//
//                if (modJson.getString("ref").equals(batchJson.getString("modRef")) && modJson.getInteger("bcdLevel").equals(batchJson.getInteger("bcdLevel"))) {
//
////                    batchJson.put("auth", batchJson.getJSONObject("defRole").getInteger("auth"));
////                    batchJson.remove("defRole");
//
//                    resultBatchArray.add(new JSONObject().fluentPut("ref",batchJson.getString("ref"))
//                            .fluentPut("modRef",batchJson.getString("modRef"))
//                            .fluentPut("auth",batchJson.getJSONObject("defRole").getInteger("auth"))
//                            .fluentPut("bcdLevel",batchJson.getInteger("bcdLevel")));
//
//                }
//
//            }
//
//        }
//
//
//
//
//
//
//        JSONObject result = new JSONObject();
//        result.put("card", resultCardArray);
//        result.put("batch", resultBatchArray);
//
//        JSONObject grpJson = new JSONObject();
//        grpJson.put(grp, result);
//
//        JSONObject listTypeJson = new JSONObject();
//        listTypeJson.put(listType, grpJson);
//
//        JSONObject grpUJson = new JSONObject();
//        grpUJson.put(grpU, listTypeJson);
//
//        return grpUJson;
//
//    }


    private JSONObject createAsset(String id_C,String id ,String ref,JSONObject data) {

        //状态结果对象
        JSONObject resultJson = new JSONObject();

        try {
//            JSONObject.parseObject(JSON.toJSONString(objAction
//            JSONObject objData = JSONObject.parseObject(data);
//            objData.getJSONObject("info").put("ref",ref);
            // 获取data里面的info数据,这个给ES



            // 获取data里面的info数据，这个给mongdb
           // AssetInfo infoObject = JSONObject.parseObject(JSON.toJSONString(objData.getJSONObject("info"));

            //查找当前公司，获取公司信息
            //Query compCondition = new Query(new Criteria("_id").is(infoObject.get("id_CB")).and("info").exists(true));
//            Query compCondition = new Query(new Criteria("_id").is(id_C).and("info").exists(true));
//
//            compCondition.fields().include("info");
//            Comp objComp = mongoTemplate.findOne(compCondition, Comp.class);

            Comp objComp = qt.getMDContent(id_C, "info", Comp.class);
            if(objComp == null){
                System.out.println("no comp");
                resultJson.put("boolean","false");
                resultJson.put("reason","comp对象为空");

            }
            Asset asset = qt.jsonTo(data, Asset.class);

            asset.setId(id);
            asset.getInfo().setRef(ref);
            System.out.println("start making Asset");


//            mongoTemplate.insert(asset);
            qt.addMD(asset);
            System.out.println("ok inserted  "+ id);

            //指定ES索引
//            IndexRequest request = new IndexRequest("lSAsset");

            //ES列表
            JSONObject listObject = new  JSONObject();
            listObject.putAll( data.getJSONObject("info"));
            listObject.put("id_A", id);
            listObject.put("tmk", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            listObject.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            listObject.put("id_CP", objComp.getInfo().getId_CP());

//            request.source(listObject, XContentType.JSON);
//            restHighLevelClient.index(request, RequestOptions.DEFAULT);
//
            qt.addES("lSAsset", listObject);
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
//                assetflow.put("tmk",DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//                assetflow.put("tmd",DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
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
