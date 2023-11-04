package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.enumeration.PurchaseEnum;
import com.cresign.purchase.service.ZjTestService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.es.lNUser;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.pojo.po.userCard.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName ZjTestServiceImpl
 * @Date 2023/8/10
 * @ver 1.0.0
 */
@Service
public class ZjTestServiceImpl implements ZjTestService {
    @Autowired
    private Qt qt;
    @Autowired
    private Ws ws;
    @Autowired
    private RetResult retResult;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HttpServletResponse response;
    private static final String sharePrefix = "share";

    @Override
    public ApiResponse getMdSetEs(String key, String esIndex,String condition,String val) {
        if (null == key || "".equals(key)) {
            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                    ERR_KEY_IS_NULL.getCode(),"");
        }
        Criteria where = Criteria.where(key);
        //创建查询对象
        Query query = new Query();
        if (condition.equals("1")) {
            query.addCriteria(where.is(val));
        } else {
            query.addCriteria(where.ne(null));
        }
        query.fields().include("info");
        List<User> users = mongoTemplate.find(query, User.class);

        System.out.println("长度:");
        System.out.println(users.size());
        int shu = 0;
        for (User user : users) {
            JSONArray id_u = qt.getES(esIndex, qt.setESFilt("id_U", user.getId()));
            if (null == id_u || id_u.size() == 0) {
                UserInfo info = user.getInfo();
                JSONObject wrdN = info.getWrdN();
                if (null == wrdN) {
                    wrdN = new JSONObject();
                    wrdN.put("cn","为空");
                } else {
                    String cn = wrdN.getString("cn");
                    if (null == cn || "".equals(cn)) {
                        wrdN.put("cn","为空");
                    }
                }
                JSONObject wrddesc = info.getWrddesc();
                if (null == wrddesc) {
                    wrddesc = new JSONObject();
                    wrddesc.put("cn","为空");
                } else {
                    String cn = wrddesc.getString("cn");
                    if (null == cn || "".equals(cn)) {
                        wrddesc.put("cn","为空");
                    }
                }
                JSONObject wrdNReal = info.getWrdNReal();
                if (null == wrdNReal) {
                    wrdNReal = new JSONObject();
                    wrdNReal.put("cn","为空");
                } else {
                    String cn = wrdNReal.getString("cn");
                    if (null == cn || "".equals(cn)) {
                        wrdNReal.put("cn","为空");
                    }
                }
                lNUser lnuser = new lNUser(user.getId(),wrdN,wrddesc,
                        wrdNReal,info.getWrdTag(), info.getPic(), info.getId_APP()
                        , info.getId_WX(), info.getCem(), info.getMbn(),info.getCnty()
                        , info.getDefNG(), 0);
                qt.addES("lNUser", lnuser);
//                shu++;
            }
        }
        System.out.println("shu:"+shu);
        return retResult.ok(CodeEnum.OK.getCode(), "1");
    }

    public static final String red = "wsl";
    @Override
    public ApiResponse sendLog(LogFlow logFlow) {
//        String id_u = logFlow.getId_U();
//        String rdSetStr = qt.getRDSetStr(red, id_u);
//        System.out.println("rdSetStr:"+rdSetStr);
//        if ((null != rdSetStr && !"".equals(rdSetStr))) {
////            ws.sendWSXin(rdSetStr,logFlow);
//        } else {
//            ws.sendWSEs(logFlow);
////            ws.push2(logFlow,id_u);
//        }
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        ws.sendWS(logFlow);
        return retResult.ok(CodeEnum.OK.getCode(), "发送成功");
    }

    @Override
    public ApiResponse sendLogSp(String id_U, String id_C, String id, String logType
            , String subType, String zcnDesc, JSONObject data) {
        LogFlow logFlow = new LogFlow();
        logFlow.setId_U(id_U);
        logFlow.setId_C(id_C);
        logFlow.setId(id);
        logFlow.setLogType(logType);
        logFlow.setSubType(subType);
        logFlow.setZcndesc(zcnDesc);
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        if (data.getInteger("res") == 0) {
            data.put("id_SP",UUID.randomUUID().toString().replace("-",""));
            data.put("id_UA",id_U);
        } else {
            data.put("id_UM",id_U);
        }
        logFlow.setData(data);
        JSONArray id_Us = getUsersById_Q(id_C, id, id_U);
        if (null != id_Us) {
            logFlow.setId_Us(id_Us);
            ws.sendWS(logFlow);
            return retResult.ok(CodeEnum.OK.getCode(), "发送-审批-成功");
        }
        return retResult.ok(CodeEnum.OK.getCode(), "发送-审批-失败");
    }

    @Override
    public ApiResponse sendLogXj(String id_U, String id_C, String id, String logType
            , String subType, String zcnDesc, JSONObject data) {
        LogFlow logFlow = new LogFlow();
        logFlow.setId_U(id_U);
        logFlow.setId_C(id_C);
        logFlow.setId(id);
        logFlow.setLogType(logType);
        logFlow.setSubType(subType);
        logFlow.setZcndesc(zcnDesc);
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        if (data.getInteger("res") == 0) {
            data.put("id_SP",UUID.randomUUID().toString().replace("-",""));
            data.put("id_UA",id_U);
            logFlow.setData(data);
            Asset asset = qt.getConfig(logFlow.getId_C(), "a-auth", "role");
            if (null == asset || null == asset.getRole() || null == asset.getRole().getJSONObject("objFC")) {
                return retResult.ok(CodeEnum.OK.getCode(), "操作失败!");
            }
            JSONObject objFC = asset.getRole().getJSONObject("objFC");
            JSONArray electData = data.getJSONArray("electData");
            boolean isSendThis = false;
            for (int i = 0; i < electData.size(); i++) {
                JSONObject electSon = electData.getJSONObject(i);
                String id_q = electSon.getString("id_Q");
                JSONArray grp = electSon.getJSONArray("grp");
                JSONArray sendIds = new JSONArray();
                JSONObject objFCSon = objFC.getJSONObject(id_q);
                if (grp.size() > 0) {
                    for (String id_UNew : objFCSon.keySet()) {
                        JSONObject id_UInfo = objFCSon.getJSONObject(id_UNew);
                        String position = id_UInfo.getString("position");
                        boolean isAdd = false;
                        for (int j = 0; j < grp.size(); j++) {
                            String string = grp.getString(j);
                            if (string.equals(position)) {
                                isAdd = true;
                                break;
                            }
                        }
                        if (!isAdd) {
                            sendIds.add(id_UNew);
                        }
                    }
                } else {
                    sendIds.addAll(objFCSon.keySet());
                }
                if (id_q.equals(id)) {
                    isSendThis = true;
                    if (!objFCSon.containsKey(id_U)) {
                        sendIds.add(id_U);
                    }
                }
                logFlow.setId_Us(sendIds);
                logFlow.setId(id_q);
                System.out.println("logFlow.getId:"+logFlow.getId());
                ws.sendWS(logFlow);
            }
            if (!isSendThis) {
                logFlow.setId(id);
                JSONObject objFCSon = objFC.getJSONObject(id);
                JSONArray id_Us = new JSONArray();
                id_Us.addAll(objFCSon.keySet());
                logFlow.setId_Us(id_Us);
                ws.sendWS(logFlow);
            }
            return retResult.ok(CodeEnum.OK.getCode(), "发送-选举-成功");
        } else {
            logFlow.setData(data);
            JSONArray id_Us = getUsersById_Q(id_C, id, id_U);
            if (null != id_Us) {
                logFlow.setId_Us(id_Us);
                ws.sendWS(logFlow);
                return retResult.ok(CodeEnum.OK.getCode(), "发送-选举-成功");
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "发送-选举-失败");
    }

    public JSONArray getUsersById_Q(String id_C,String id,String id_U){
        Asset asset = qt.getConfig(id_C, "a-auth", "role");
        if (null != asset && null != asset.getRole() && null != asset.getRole().getJSONObject("objFC")) {
            JSONObject role = asset.getRole();
            JSONObject objFC = role.getJSONObject("objFC");
            JSONObject users = objFC.getJSONObject(id);
            JSONArray id_Us = new JSONArray();
            id_Us.addAll(users.keySet());
            if (!users.containsKey(id_U)) {
                id_Us.add(id_U);
            }
            return id_Us;
        }
        return null;
    }

    @Override
    public ApiResponse getLog(String id, String logType, String subType, String id_SP) {
        boolean isAdd = false;
        JSONArray filterArray = new JSONArray();
        if (null != id) {
            JSONArray array = qt.setESFilt("id", id);
            filterArray.addAll(array);
            isAdd = true;
        }
        boolean isXj = false;
        if (null != subType) {
            if (subType.equals("elect")) {
                isXj = true;
            }
            if (null != logType) {
                JSONArray array = qt.setESFilt("logType", logType,"subType",subType);
                filterArray.addAll(array);
                isAdd = true;
            }
        }
        if (null != id_SP) {
            JSONArray array = qt.setESFilt("data.id_SP", id_SP);
            filterArray.addAll(array);
            isAdd = true;
        }
        if (!isAdd) {
            return retResult.ok(CodeEnum.OK.getCode(), new JSONArray());
        }
        JSONArray msg = qt.getES("msg", filterArray);
        if (null != msg && msg.size() > 0) {
            if (isXj) {
                JSONObject result = new JSONObject();
                result.put("msg",msg);
                int z = 0;
                int f = 0;
                int zon = 0;
                for (int i = 0; i < msg.size(); i++) {
                    JSONObject json = msg.getJSONObject(i);
                    JSONObject data = json.getJSONObject("data");
                    Integer res = data.getInteger("res");
                    if (res > 0) {
                        z+=res;
                    } else {
                        f+=res;
                    }
                    zon+=res;
                }
                result.put("z",z);
                result.put("f",f);
                result.put("zon",zon);
                return retResult.ok(CodeEnum.OK.getCode(), result);
            }
            return retResult.ok(CodeEnum.OK.getCode(), msg);
        }
        return retResult.ok(CodeEnum.OK.getCode(), new JSONArray());
    }

    @Override
    public ApiResponse getLogSp(String id, String id_SP) {
        JSONArray filterArray = new JSONArray();
        if (null != id) {
            JSONArray array = qt.setESFilt("id", id);
            filterArray.addAll(array);
        }
        if (null != id_SP) {
            JSONArray array = qt.setESFilt("data.id_SP", id_SP);
            filterArray.addAll(array);
        }
        JSONArray array = qt.setESFilt("logType", "msg","subType","confirm");
        filterArray.addAll(array);
        JSONArray msg = qt.getES("msg", filterArray);
        if (null != msg && msg.size() > 0) {
            return retResult.ok(CodeEnum.OK.getCode(), msg);
        }
        return retResult.ok(CodeEnum.OK.getCode(), new JSONArray());
    }

    @Override
    public ApiResponse getLogXj(String id, String id_SP) {
        JSONArray filterArray = new JSONArray();
        if (null != id) {
            JSONArray array = qt.setESFilt("id", id);
            filterArray.addAll(array);
        }
        if (null != id_SP) {
            JSONArray array = qt.setESFilt("data.id_SP", id_SP);
            filterArray.addAll(array);
        }
        JSONArray array = qt.setESFilt("logType", "msg","subType","elect");
        filterArray.addAll(array);
        JSONArray msg = qt.getES("msg", filterArray);
        if (null != msg && msg.size() > 0) {
            JSONObject result = new JSONObject();
            int z = 0;
            int f = 0;
            int zon = 0;
            JSONObject statistics = new JSONObject();
            for (int i = 0; i < msg.size(); i++) {
                JSONObject json = msg.getJSONObject(i);
                JSONObject data = json.getJSONObject("data");
                Integer res = data.getInteger("res");
                if (res > 0) {
                    z+=res;
                } else {
                    f+=res;
                }
                zon+=res;
                Integer integer = statistics.getInteger(res.toString());
                if (null == integer) {
                    statistics.put(res.toString(),1);
                } else {
                    integer++;
                    statistics.put(res.toString(),integer);
                }
            }
            result.put("statistics",statistics);
            result.put("z",z);
            result.put("f",f);
            result.put("zon",zon);
            return retResult.ok(CodeEnum.OK.getCode(), result);
        }
        return retResult.ok(CodeEnum.OK.getCode(), new JSONArray());
    }

    @Override
    public ApiResponse shareSave(JSONObject data) {
        /*
        data:
        id_U, tmk, tdur (86400L), wrdNU, wn0open, zcndesc描述, wrdN id_X, listType
         */
        Long tdur = data.getLong("tdur");
        String shareId = UUID.randomUUID().toString().replace("-","");
        boolean isSetRd = true;
        if (null == tdur) {
            tdur = (long)(86400*2);
        } else if (tdur == -1) {
            isSetRd = false;
        }
        if (isSetRd) {
            qt.setRDSet(sharePrefix,shareId,data,tdur);
        } else {
            shareId+="_ES";
            data.put("shareId",shareId);
            qt.addES("linkflow",data);
        }
        return retResult.ok(CodeEnum.OK.getCode(), shareId);
    }

    @Override
    public ApiResponse shareOpen(String shareId) {
        System.out.println("分享打开输出:"+shareId);
        boolean isEs = shareId.endsWith("_ES");
        if (!isEs) {
            JSONObject rdSet = qt.getRDSet(sharePrefix, shareId);
            System.out.println(rdSet);
            if (null != rdSet) {
                return retResult.ok(CodeEnum.OK.getCode(), rdSet);
            }
//            try {
//                response.sendRedirect("https://www.cresign.cn/share?shareId="+shareId);
//            } catch (IOException e) {
//                System.out.println("转发异常");
//            }
        } else {
            JSONArray es = qt.getES("linkflow", qt.setESFilt("shareId", shareId));
            if (null != es && es.size() > 0) {
                return retResult.ok(CodeEnum.OK.getCode(), es.getJSONObject(0));
            }
        }
        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                ERR_SHARE_NULL.getCode(),"");
    }

    @Override
    public ApiResponse initFC(String id_C,String id_U) {
        Asset asset = qt.getConfig(id_C, "a-auth", "flowControl");
        if (null == asset || null == asset.getFlowControl() || null == asset.getFlowControl().getJSONArray("objData")) {
            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                    ASSET_NOT_FOUND.getCode(),"");
        }
        JSONObject flowControl = asset.getFlowControl();
        JSONArray objData = flowControl.getJSONArray("objData");
        JSONObject objFC = new JSONObject();
        for (int i = 0; i < objData.size(); i++) {
            JSONObject data = objData.getJSONObject(i);
            String id = data.getString("id");
            JSONArray objUser = data.getJSONArray("objUser");
            JSONObject userFc = new JSONObject();
            for (int j = 0; j < objUser.size(); j++) {
                JSONObject userData = objUser.getJSONObject(j);
                String id_UNew = userData.getString("id_U");
                JSONObject userFcData = new JSONObject();
//                JSONArray array = new JSONArray();
//                array.add("test");
//                userFcData.put("role",array);
//                userFcData.put("position",j==0?"main":"ordinary");
//                userFcData.put("isProhibit",j==0?"false":"true");
                userFcData.put("role",new JSONArray());
                if (id_UNew.equals(id_U)) {
                    userFcData.put("position","main");
                } else {
                    userFcData.put("position","ordinary");
                }
                userFcData.put("isProhibit","false");
                userFcData.put("timeProhibit",0);
                userFcData.put("startTimeProhibit","");
                userFc.put(id_UNew,userFcData);
            }
            objFC.put(id,userFc);
        }
        qt.setMDContent(asset.getId(),qt.setJson("role.objFC",objFC), Asset.class);
        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
    }

    @Override
    public ApiResponse getFCAuth(String id_C,String id) {
        Asset asset = qt.getConfig(id_C, "a-auth", "role");
        if (null == asset || null == asset.getRole() || null == asset.getRole().getJSONObject("objFC")) {
            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                    ASSET_NOT_FOUND.getCode(),"");
        }
        JSONObject objFC = asset.getRole().getJSONObject("objFC");
        JSONObject result = objFC.getJSONObject(id);
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    @Override
    public ApiResponse setFCAuth(String id_C, String id, JSONObject users) {
        Asset asset = qt.getConfig(id_C, "a-auth", "role");
        if (null == asset || null == asset.getRole() || null == asset.getRole().getJSONObject("objFC")) {
            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                    ASSET_NOT_FOUND.getCode(),"");
        }
        JSONObject objFC = asset.getRole().getJSONObject("objFC");
        JSONObject objFCData = objFC.getJSONObject(id);
        for (String s : users.keySet()) {
            objFCData.put(s,users.getJSONObject(s));
        }
        qt.setMDContent(asset.getId(),qt.setJson("role.objFC."+id,objFCData), Asset.class);
        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
    }

    @Override
    public ApiResponse getFCAuthByUser(String id_C, String id_U) {
        Asset asset = qt.getConfig(id_C, "a-auth", "role");
        if (null == asset || null == asset.getRole() || null == asset.getRole().getJSONObject("objFC")) {
            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                    ASSET_NOT_FOUND.getCode(),"");
        }
        JSONObject objFC = asset.getRole().getJSONObject("objFC");
        JSONObject result = new JSONObject();
        for (String s : objFC.keySet()) {
            JSONObject objFCData = objFC.getJSONObject(s);
            if (objFCData.containsKey(id_U)) {
                result.put(s,objFCData.getJSONObject(id_U));
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    @Override
    public ApiResponse getLSProdShareId(String id_P) {
        JSONArray lSProd = qt.getES("lSProd", qt.setESFilt("id_P", id_P));
        if (null != lSProd && lSProd.size() > 0) {
            JSONObject object = lSProd.getJSONObject(0);
            String shareId = object.getString("qr");
            System.out.println("ES:"+object.getString("id_ES"));
            if (null == shareId || "".equals(shareId)) {
                shareId = UUID.randomUUID().toString().replace("-","");
                qt.setES("lSProd",qt.setESFilt("_id",object.getString("id_ES")),qt.setJson("qr",shareId));
            }
            return retResult.ok(CodeEnum.OK.getCode(), shareId);
        }
        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                LS_PROD_NOT_FOUND.getCode(),"");
    }

    @Override
    public ApiResponse getLSInfoShareId(String id_I) {
        JSONArray lSInfo = qt.getES("lSInfo", qt.setESFilt("id_I", id_I));
        if (null != lSInfo && lSInfo.size() > 0) {
            JSONObject object = lSInfo.getJSONObject(0);
            String shareId = object.getString("qr");
            if (null == shareId || "".equals(shareId)) {
                shareId = UUID.randomUUID().toString().replace("-","");
                qt.setES("lSInfo",qt.setESFilt("_id",object.getString("id_ES")),qt.setJson("qr",shareId));
            }
            return retResult.ok(CodeEnum.OK.getCode(), shareId);
        }
        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                LS_INFO_NOT_FOUND.getCode(),"");
    }

    @Override
    public ApiResponse getLNUserShareId(String id_U) {
        JSONArray lNUser = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
        if (null != lNUser && lNUser.size() > 0) {
            JSONObject object = lNUser.getJSONObject(0);
            String shareId = object.getString("qr");
            if (null == shareId || "".equals(shareId)) {
                shareId = UUID.randomUUID().toString().replace("-","");
                qt.setES("lNUser",qt.setESFilt("_id",object.getString("id_ES")),qt.setJson("qr",shareId));
            }
            return retResult.ok(CodeEnum.OK.getCode(), shareId);
        }
        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                LN_USER_NOT_FOUND.getCode(),"");
    }

    @Override
    public ApiResponse getLNCompShareId(String id_C) {
        JSONArray lNComp = qt.getES("lNComp", qt.setESFilt("id_C", id_C));
        if (null != lNComp && lNComp.size() > 0) {
            JSONObject object = lNComp.getJSONObject(0);
            String shareId = object.getString("qr");
            if (null == shareId || "".equals(shareId)) {
                shareId = UUID.randomUUID().toString().replace("-","");
                qt.setES("lNComp",qt.setESFilt("_id",object.getString("id_ES")),qt.setJson("qr",shareId));
            }
            return retResult.ok(CodeEnum.OK.getCode(), shareId);
        }
        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                LN_COMP_NOT_FOUND.getCode(),"");
    }

    @Override
    public ApiResponse getLBProdShareId(String id_P) {
        JSONArray lBProd = qt.getES("lBProd", qt.setESFilt("id_P", id_P));
        if (null != lBProd && lBProd.size() > 0) {
            JSONObject object = lBProd.getJSONObject(0);
            String shareId = object.getString("qr");
            if (null == shareId || "".equals(shareId)) {
                shareId = UUID.randomUUID().toString().replace("-","");
                qt.setES("lBProd",qt.setESFilt("_id",object.getString("id_ES")),qt.setJson("qr",shareId));
            }
            return retResult.ok(CodeEnum.OK.getCode(), shareId);
        }
        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                LB_PROD_NOT_FOUND.getCode(),"");
    }

    @Override
    public ApiResponse getLBInfoShareId(String id_I) {
        JSONArray lBInfo = qt.getES("lBInfo", qt.setESFilt("id_I", id_I));
        if (null != lBInfo && lBInfo.size() > 0) {
            JSONObject object = lBInfo.getJSONObject(0);
            String shareId = object.getString("qr");
            if (null == shareId || "".equals(shareId)) {
                shareId = UUID.randomUUID().toString().replace("-","");
                qt.setES("lBInfo",qt.setESFilt("_id",object.getString("id_ES")),qt.setJson("qr",shareId));
            }
            return retResult.ok(CodeEnum.OK.getCode(), shareId);
        }
        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                LB_INFO_NOT_FOUND.getCode(),"");
    }

    @Override
    public ApiResponse saveProdEncryption(JSONObject en) {
        Prod prod = new Prod();
        prod.setId(qt.GetObjectId());
        prod.setEn(en);
        prod.setView(new JSONArray());
//        qt.setMDContent(id_P,qt.setJson("en",en), Prod.class);
        qt.addMD(prod);
        return retResult.ok(CodeEnum.OK.getCode(), prod.getId());
    }

    @Override
    public ApiResponse getProdEncryption(String id_P) {
        Prod en = qt.getMDContent(id_P, "en", Prod.class);
        return retResult.ok(CodeEnum.OK.getCode(), en);
    }

    @Override
    public ApiResponse getShareId(String shareId, String type) {
        JSONArray typeArr = qt.getES(type, qt.setESFilt("qr", shareId));
        if (null != typeArr && typeArr.size() > 0) {
            JSONObject object = typeArr.getJSONObject(0);
            return retResult.ok(CodeEnum.OK.getCode(), object);
        }
        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                TYPE_NOT_FOUND.getCode(),"");
    }

    @Override
    public ApiResponse applyForView(String id_U, String id_C, String id, String logType
            , String subType, String zcnDesc, JSONObject data,int imp) {
        LogFlow logFlow = new LogFlow();
        logFlow.setId_U(id_U);
        logFlow.setId_C(id_C);
        logFlow.setId(id);
        logFlow.setLogType(logType);
        logFlow.setSubType(subType);
        logFlow.setZcndesc(zcnDesc);
        logFlow.setData(data);
        logFlow.setImp(imp);
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        JSONArray id_Us = getUsersById_Q(id_C, id, id_U);
        if (null != id_Us) {
            logFlow.setId_Us(id_Us);
            ws.sendWS(logFlow);
            return retResult.ok(CodeEnum.OK.getCode(), "发送-查看-成功");
        }
        return retResult.ok(CodeEnum.OK.getCode(), "发送-查看-失败");
    }

    @Override
    public ApiResponse applyForAgreeWith(String id_U, String id_C, String id, String logType
            , String subType, String zcnDesc, JSONObject data,int imp) {
        LogFlow logFlow = new LogFlow();
        logFlow.setId_U(id_U);
        logFlow.setId_C(id_C);
        logFlow.setId(id);
        logFlow.setLogType(logType);
        logFlow.setSubType(subType);
        logFlow.setZcndesc(zcnDesc);
        logFlow.setData(data);
        logFlow.setImp(imp);
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        JSONArray id_Us = getUsersById_Q(id_C, id, id_U);
        if (null != id_Us) {
            logFlow.setId_Us(id_Us);
            ws.sendWS(logFlow);
            return retResult.ok(CodeEnum.OK.getCode(), "发送-同意查看-成功");
        }
        return retResult.ok(CodeEnum.OK.getCode(), "发送-同意查看-失败");
    }

    /**
     *
     * @param id_C
     * @param sumDates
     * @param chkInMode 打卡模式
     * @param isAllSpecialTime 是否全部特殊时间
     * @param isAutoCardReplacement 是否自动系统补卡
     * @param isSumSpecialTime 是否统计特殊时间
     * @return
     */
    @Override
    public ApiResponse statisticsChKin(String id_C,JSONArray sumDates,int chkInMode
            ,boolean isAllSpecialTime,boolean isAutoCardReplacement,boolean isSumSpecialTime) {
        String date = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        Asset asset = qt.getConfig(id_C, "a-chkin", "chkin");
        if (null == asset || null == asset.getChkin()) {
            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                    ASSET_NOT_FOUND.getCode(),"");
        }
        JSONObject chkin = asset.getChkin();
        JSONObject objChkin = chkin.getJSONObject("objChkin");
        JSONObject bm1 = objChkin.getJSONObject("bm1");
        JSONObject zb1 = bm1.getJSONObject("zb1");
        String theSameDay = sumDates.getString(0);
//        int chkInMode = 0;
        // 获取上下班时间
        JSONArray arrTime = zb1.getJSONArray("arrTime");
        // 获取上班前打卡时间范围
        long tPre = zb1.getInteger("tPre") * 60;
        // 获取下班后打卡时间范围
        long tPost = zb1.getInteger("tPost") * 60;
        // 获取严重迟到时间
        long tLate = zb1.getInteger("tLate") * 60;
        // 获取矿工迟到时间
        long tAbsent = zb1.getInteger("tAbsent") * 60;
        // 获取正常上班总时间
        long teDur = zb1.getInteger("teDur") * 60 *60;
        // 获取考勤类型（0 = 固定班、1 = 自由时间）
        String chkType = zb1.getString("chkType");
        // 获取默认班日( 0（1到5），1(1到6)，2（大小周），3（按月放假天）)
        String dayType = zb1.getString("dayType");
        // 记录加班时间，自己加一获取
        JSONArray ovt = zb1.getJSONArray("ovt");
        // 按月放假天数
        int dayOff = zb1.getInteger("dayOff");
        // 必须打卡日期
        JSONArray dayMust = zb1.getJSONArray("dayMust");
        // 无须打卡日期
        JSONArray dayMiss = zb1.getJSONArray("dayMiss");
        teDur+=7200;
        if (isAllSpecialTime) {
            JSONArray chkInEs = qt.getES("chkin", qt.setESFilt("id_U", "6256789ae1908c03460f906f"));
            if (null == chkInEs || chkInEs.size() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                        CHK_IN_NOT_FOUND.getCode(),"");
            }
            List<Long> userDates1 = getUserDates(chkInEs);
            testSpecialTime(userDates1,teDur);

            JSONArray chkInEs2 = qt.getES("chkin", qt.setESFilt("id_U", "test"));
            if (null == chkInEs2 || chkInEs2.size() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                        CHK_IN_NOT_FOUND.getCode(),"");
            }
            List<Long> userDates2 = getUserDates(chkInEs2);
            testSpecialTime(userDates2,teDur);
            return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
        }
        List<Long> arrTimeLong = getArrTime(arrTime, theSameDay);
        if (chkInMode == 0) {
            JSONArray chkInEs = qt.getES("chkin", qt.setESFilt("id_U", "6256789ae1908c03460f906f"));
            if (null == chkInEs || chkInEs.size() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                        CHK_IN_NOT_FOUND.getCode(),"");
            }
            List<Long> userDates1 = getUserDates(chkInEs);
            testChkInSum(arrTimeLong,userDates1,"6256789ae1908c03460f906f",teDur
                    ,isAutoCardReplacement,isSumSpecialTime,ovt);

            JSONArray chkInEs2 = qt.getES("chkin", qt.setESFilt("id_U", "test"));
            if (null == chkInEs2 || chkInEs2.size() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                        CHK_IN_NOT_FOUND.getCode(),"");
            }
            List<Long> userDates2 = getUserDates(chkInEs2);
            testChkInSum(arrTimeLong,userDates2,"test",teDur
                    ,isAutoCardReplacement,isSumSpecialTime,ovt);
            return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
        } else if (chkInMode == 1) {
            JSONArray chkInEs = qt.getES("chkin", qt.setESFilt("id_U", "6256789ae1908c03460f906f"));
            if (null == chkInEs || chkInEs.size() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                        CHK_IN_NOT_FOUND.getCode(),"");
            }
            List<Long> userDates1 = getUserDates(chkInEs);
            testChkInSumHeadTail(arrTimeLong,userDates1,"6256789ae1908c03460f906f",teDur
                    ,isAutoCardReplacement,isSumSpecialTime,ovt);

            JSONArray chkInEs2 = qt.getES("chkin", qt.setESFilt("id_U", "test"));
            if (null == chkInEs2 || chkInEs2.size() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.
                        CHK_IN_NOT_FOUND.getCode(),"");
            }
            List<Long> userDates2 = getUserDates(chkInEs2);
            testChkInSumHeadTail(arrTimeLong,userDates2,"test",teDur
                    ,isAutoCardReplacement,isSumSpecialTime,ovt);
            return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
        }
        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
    }

    public void testChkInSum(List<Long> arrTimeLong,List<Long> userDates,String id_U
            ,long teDur,boolean isAutoCardReplacement,boolean isSumSpecialTime,JSONArray ovt){
        List<List<Long>> originTimeList = new ArrayList<>();
        List<List<Long>> arrTimeList = new ArrayList<>();
        List<List<Long>> centreTimeList = new ArrayList<>();
        List<Long> originListNew = new ArrayList<>();
        List<Long> centreListNew = new ArrayList<>();
        long chi = 0;
        long zao = 0;
        for (int i = 0; i < arrTimeLong.size(); i+=2) {
            Long upper = arrTimeLong.get(i);
            Long below = arrTimeLong.get(i + 1);
            List<Long> centreList = new ArrayList<>();
            centreList.add(upper);
            centreList.add(below);
            arrTimeList.add(centreList);
            List<Long> centreTimeSon = new ArrayList<>();

            List<Long> originList = new ArrayList<>();
            List<Long> upperBelowBetween = getUpperBelowBetween(upper, below, userDates, 30, 30);
            if (upperBelowBetween.size() == 0) {
                originTimeList.add(originList);
                centreTimeList.add(centreTimeSon);
                continue;
            }
            if (upperBelowBetween.size() > 4) {
                int reduce = upperBelowBetween.size() - 4;
                for (int j = 0; j < reduce; j++) {
                    upperBelowBetween.remove(upperBelowBetween.size()-1);
                }
            }
            if (upperBelowBetween.size() % 2 == 0) {
                originListNew.addAll(upperBelowBetween);
                originList.addAll(upperBelowBetween);
                if (upperBelowBetween.get(0) <= upper) {
                    centreListNew.add(upper);
                    centreTimeSon.add(upper);
                } else {
                    chi += upperBelowBetween.get(0) - upper;
                    centreListNew.add(upperBelowBetween.get(0));
                    centreTimeSon.add(upperBelowBetween.get(0));
                }
                if (upperBelowBetween.size() == 4) {
                    centreListNew.add(upperBelowBetween.get(1));
                    centreListNew.add(upperBelowBetween.get(2));
                    centreTimeSon.add(upperBelowBetween.get(1));
                    centreTimeSon.add(upperBelowBetween.get(2));
                }
                if (upperBelowBetween.get(upperBelowBetween.size() - 1) >= below) {
                    centreListNew.add(below);
                    centreTimeSon.add(below);
                } else {
                    zao += below - upperBelowBetween.get(upperBelowBetween.size() - 1);
                    centreListNew.add(upperBelowBetween.get(upperBelowBetween.size() - 1));
                    centreTimeSon.add(upperBelowBetween.get(upperBelowBetween.size() - 1));
                }
            } else {
                long original;
                boolean isNormalUpper = false;
                boolean isNormalBelow = false;
                if (upperBelowBetween.size() == 3) {
                    if (upperBelowBetween.get(0) <= upper) {
                        centreListNew.add(upper);
                        centreTimeSon.add(upper);
                        originListNew.add(upperBelowBetween.get(0));
                        originList.add(upperBelowBetween.get(0));
                    } else {
                        if (isAutoCardReplacement) {
                            centreListNew.add(upper);
                            centreTimeSon.add(upper);
                        } else {
                            chi += upperBelowBetween.get(0) - upper;
                            centreListNew.add(0L);
                            centreTimeSon.add(0L);
                        }
                        originListNew.add(0L);
                        originList.add(0L);
                        for (Long betweenTime : upperBelowBetween) {
                            originListNew.add(betweenTime);
                            originList.add(betweenTime);
                            centreListNew.add(betweenTime);
                            centreTimeSon.add(betweenTime);
                        }
                        Long centreTime1 = upperBelowBetween.get(0);
                        Long centreTime2 = upperBelowBetween.get(1);
                        zao += centreTime2 - centreTime1;
                        originTimeList.add(originList);
                        centreTimeList.add(centreTimeSon);
                        continue;
                    }
                    if (upperBelowBetween.get(upperBelowBetween.size() - 1) >= below) {
                        centreListNew.add(below);
                        centreTimeSon.add(below);
                        originListNew.add(upperBelowBetween.get(upperBelowBetween.size() - 1));
                        originList.add(upperBelowBetween.get(upperBelowBetween.size() - 1));
                    } else {
                        for (int j = 1; j < upperBelowBetween.size(); j++) {
                            originListNew.add(upperBelowBetween.get(j));
                            originList.add(upperBelowBetween.get(j));
                            centreListNew.add(upperBelowBetween.get(j));
                            centreTimeSon.add(upperBelowBetween.get(j));
                        }
                        if (isAutoCardReplacement) {
                            centreListNew.add(below);
                            centreTimeSon.add(below);
                        } else {
                            zao += below - upperBelowBetween.get(upperBelowBetween.size() - 1);
                            centreListNew.add(0L);
                            centreTimeSon.add(0L);
                        }
                        originListNew.add(0L);
                        originList.add(0L);
                        Long centreTime1 = upperBelowBetween.get(1);
                        Long centreTime2 = upperBelowBetween.get(2);
                        zao += centreTime2 - centreTime1;
                    }
                    originTimeList.add(originList);
                    centreTimeList.add(centreTimeSon);
                    continue;
                } else {
                    original = upperBelowBetween.get(0);
                    if (original <= upper) {
                        isNormalUpper = true;
                    } else {
                        if (original >= below) {
                            isNormalBelow = true;
                        }
                    }
                }
                int originIndex = getContrastChkInTimeNew(original, centreList);
                int newIndex;
                // isUpper是对应newIndex的上下班时间
                boolean isUpper = true;
                if(originIndex % 2 == 0){
                    newIndex = originIndex+1;
                    isUpper = false;
                } else {
                    newIndex = originIndex-1;
                }
                Long originTime = centreList.get(originIndex);
                if (isAutoCardReplacement) {
                    Long newTime = centreList.get(newIndex);
//                    Long origTime = centreList.get(originIndex);
                    if (isNormalUpper) {
                        originListNew.add(original);
                        originListNew.add(0L);
                        centreListNew.add(originTime);
                        centreListNew.add(newTime);
                        centreTimeSon.add(originTime);
                        centreTimeSon.add(newTime);
                        originList.add(original);
                        originList.add(0L);
                    } else {
                        if (isNormalBelow) {
                            originListNew.add(0L);
                            originListNew.add(original);
                            centreListNew.add(newTime);
                            centreListNew.add(originTime);
                            centreTimeSon.add(newTime);
                            centreTimeSon.add(originTime);
                            originList.add(0L);
                            originList.add(original);
                        } else {
                            if (original < originTime) {
                                originListNew.add(original);
                                originListNew.add(0L);
                                centreListNew.add(original);
                                centreListNew.add(originTime);
                                centreTimeSon.add(original);
                                centreTimeSon.add(originTime);
                                originList.add(original);
                                originList.add(0L);
                            } else {
                                originListNew.add(0L);
                                originListNew.add(original);
                                centreListNew.add(originTime);
                                centreListNew.add(original);
                                centreTimeSon.add(originTime);
                                centreTimeSon.add(original);
                                originList.add(0L);
                                originList.add(original);
                            }
                            if (isUpper) {
                                chi += (original - newTime);
                            } else {
                                zao += (newTime - original);
                            }
                        }
                    }
//                    Long newTime = centreList.get(newIndex);
                } else {
                    if (original < originTime) {
                        originListNew.add(original);
                        originListNew.add(0L);
                        centreListNew.add(original);
                        centreListNew.add(0L);
                        centreTimeSon.add(original);
                        centreTimeSon.add(0L);
                        originList.add(original);
                        originList.add(0L);
                    } else {
                        originListNew.add(0L);
                        originListNew.add(original);
                        centreListNew.add(0L);
                        centreListNew.add(original);
                        centreTimeSon.add(0L);
                        centreTimeSon.add(original);
                        originList.add(0L);
                        originList.add(original);
                    }
                }
            }
            originTimeList.add(originList);
            centreTimeList.add(centreTimeSon);
        }
        System.out.println();
        System.out.println("-------------------- "+id_U+" --------------------");
        System.out.println("正常时间纠正New - centreListNew:");
        System.out.println(JSON.toJSONString(getTimeToStr(centreListNew)));
        System.out.println("正常时间 - originListNew:");
        System.out.println(JSON.toJSONString(getTimeToStr(originListNew)));
//        System.out.println("特殊时间 - specialTimeList:");
//        System.out.println(JSON.toJSONString(getTimeToStr(specialTimeList)));
        System.out.println("++++++++++++++++++++++++++++++++");
        System.out.println("正常每段时间纠正 - centreTimeList:");
        getTimeToListStr(centreTimeList);
        System.out.println("++++++++++++++++++++++++++++++++");
        System.out.println("正常每段时间 - originTimeList:");
        getTimeToListStr(originTimeList);
//        System.out.println("++++++++++++++++++++++++++++++++");
//        System.out.println("正常每段时间 - arrTimeList:");
//        getTimeToListStr(arrTimeList);
        long zon = 0;
        long jia = 0;
        long que = 0;
        long work;
        for (int i = 0; i < centreListNew.size(); i+=2) {
            long ownUpper = centreListNew.get(i);
            long ownBelow = centreListNew.get(i+1);
            if (ownUpper == 0 || ownBelow == 0) {
                continue;
            }
            zon += ownBelow - ownUpper;
        }
        for (int i = 0; i < arrTimeList.size(); i++) {
            List<Long> arrList = arrTimeList.get(i);
            List<Long> centreList = centreTimeList.get(i);
            boolean isQue = false;
            if (centreList.size() == 0) {
                isQue = true;
            } else if (centreList.size() == 2) {
                Long upper = centreList.get(0);
                Long below = centreList.get(1);
                if (upper == 0 || below == 0) {
                    isQue = true;
                }
            }
            if (isQue) {
                Long ownUpper = arrList.get(0);
                Long ownBelow = arrList.get(1);
                que += ownBelow - ownUpper;
            }
        }
        for (int i = 0; i < ovt.size(); i++) {
            Integer ovtIndex = ovt.getInteger(i);
            List<Long> centreList = centreTimeList.get(ovtIndex/2);
            if (centreList.size() > 0) {
                for (int j = 0; j < centreList.size(); j+=2) {
                    Long upper = centreList.get(j);
                    Long below = centreList.get(j + 1);
                    if (upper == 0 || below == 0) {
                        continue;
                    }
                    jia += below - upper;
                }
            }
        }
        work = zon - jia;
//        if (zon > teDur) {
//            zon = teDur;
//        }
        System.out.println("总上班时间 - zon:"+zon);
        System.out.println("普通上班时间: - work:"+work);
        System.out.println("迟到时间 - chi:"+chi);
        System.out.println("早退时间 - zao:"+zao);
        System.out.println("加班时间 - jia:"+jia);
        System.out.println("缺勤时间 - que:"+que);
        System.out.println("总要求上班时间 - teDur:"+teDur);
        if (isSumSpecialTime) {
            long te = 0;
            long start = arrTimeLong.get(0);
            long end = arrTimeLong.get(arrTimeLong.size()-1);
            // 开始或小于开始时间
            List<Long> startSpecialList = new ArrayList<>();
            // 结束或大于结束时间
            List<Long> endSpecialList = new ArrayList<>();
            for (Long da : userDates) {
                if (da <= start) {
                    startSpecialList.add(da);
                } else if (da >= end) {
                    endSpecialList.add(da);
                }
            }
            if (startSpecialList.size() > 0 && startSpecialList.size() % 2 != 0) {
                startSpecialList.remove(startSpecialList.size()-1);
            }
            if (endSpecialList.size() > 0 && endSpecialList.size() % 2 != 0) {
                endSpecialList.remove(0);
            }
            for (int i = 0; i < startSpecialList.size(); i+=2) {
                Long ownUpper = startSpecialList.get(i);
                Long ownBelow = startSpecialList.get(i + 1);
                te += ownBelow - ownUpper;
            }
            for (int i = 0; i < endSpecialList.size(); i+=2) {
                Long ownUpper = endSpecialList.get(i);
                Long ownBelow = endSpecialList.get(i + 1);
                te += ownBelow - ownUpper;
            }
            System.out.println("特殊- 开始 -时间 - startSpecialList:");
            System.out.println(JSON.toJSONString(getTimeToStr(startSpecialList)));
            System.out.println("特殊= 结束 =时间 - endSpecialList:");
            System.out.println(JSON.toJSONString(getTimeToStr(endSpecialList)));
            System.out.println("特殊上班时间 - te:"+te);
        }
    }
    public void testChkInSumHeadTail(List<Long> arrTimeLong,List<Long> userDates,String id_U
            ,long teDur,boolean isAutoCardReplacement,boolean isSumSpecialTime,JSONArray ovt){
        List<List<Long>> originTimeList = new ArrayList<>();
        List<List<Long>> arrTimeList = new ArrayList<>();
        List<List<Long>> centreTimeList = new ArrayList<>();
        List<Long> originListNew = new ArrayList<>();
        List<Long> centreListNew = new ArrayList<>();
        long chi = 0;
        long zao = 0;
        for (int i = 0; i < arrTimeLong.size(); i+=2) {
            Long upper = arrTimeLong.get(i);
            Long below = arrTimeLong.get(i + 1);
            List<Long> centreList = new ArrayList<>();
            centreList.add(upper);
            centreList.add(below);
            arrTimeList.add(centreList);

            List<Long> originList = new ArrayList<>();
            List<Long> centreTimeSon = new ArrayList<>();
            List<Long> upperBelowBetween = getUpperBelowBetween(upper, below, userDates, 30, 30);
            if (upperBelowBetween.size() == 0) {
                originTimeList.add(originList);
                centreTimeList.add(centreTimeSon);
                continue;
            }
            List<Long> upperBelowBetweenNew = new ArrayList<>();
            if (upperBelowBetween.size() > 2) {
                upperBelowBetweenNew.add(upperBelowBetween.get(0));
                upperBelowBetweenNew.add(upperBelowBetween.get(upperBelowBetween.size()-1));
            } else {
                upperBelowBetweenNew.addAll(upperBelowBetween);
            }
            if (upperBelowBetweenNew.size() % 2 == 0) {
                originListNew.addAll(upperBelowBetweenNew);
                originList.addAll(upperBelowBetweenNew);
                if (upperBelowBetweenNew.get(0) <= upper) {
                    centreListNew.add(upper);
                    centreTimeSon.add(upper);
                } else {
                    chi += upperBelowBetweenNew.get(0) - upper;
                    centreListNew.add(upperBelowBetweenNew.get(0));
                    centreTimeSon.add(upperBelowBetweenNew.get(0));
                }
                if (upperBelowBetweenNew.get(upperBelowBetweenNew.size() - 1) >= below) {
                    centreListNew.add(below);
                    centreTimeSon.add(below);
                } else {
                    zao += below - upperBelowBetweenNew.get(upperBelowBetweenNew.size() - 1);
                    centreListNew.add(upperBelowBetweenNew.get(upperBelowBetweenNew.size() - 1));
                    centreTimeSon.add(upperBelowBetweenNew.get(upperBelowBetweenNew.size() - 1));
                }
            } else {
                long original = upperBelowBetweenNew.get(0);
                boolean isNormalUpper = false;
                boolean isNormalBelow = false;
                if (original <= upper) {
                    isNormalUpper = true;
                } else {
                    if (original >= below) {
                        isNormalBelow = true;
                    }
                }
                int originIndex = getContrastChkInTimeNew(original, centreList);
                int newIndex;
                boolean isUpper = true;
                if(originIndex % 2 == 0){
                    newIndex = originIndex+1;
                    isUpper = false;
                } else {
                    newIndex = originIndex-1;
                }
                Long originTime = centreList.get(originIndex);
                if (isAutoCardReplacement) {
                    Long newTime = centreList.get(newIndex);
//                    Long origTime = centreList.get(originIndex);
                    if (isNormalUpper) {
                        originListNew.add(original);
                        originListNew.add(0L);
                        centreListNew.add(originTime);
                        centreListNew.add(newTime);
                        centreTimeSon.add(originTime);
                        centreTimeSon.add(newTime);
                        originList.add(original);
                        originList.add(0L);
                    } else {
                        if (isNormalBelow) {
                            originListNew.add(0L);
                            originListNew.add(original);
                            centreListNew.add(newTime);
                            centreListNew.add(originTime);
                            centreTimeSon.add(newTime);
                            centreTimeSon.add(originTime);
                            originList.add(0L);
                            originList.add(original);
                        } else {
                            if (original < originTime) {
                                originListNew.add(original);
                                originListNew.add(0L);
                                centreListNew.add(original);
                                centreListNew.add(originTime);
                                centreTimeSon.add(original);
                                centreTimeSon.add(originTime);
                                originList.add(original);
                                originList.add(originTime);
                            } else {
                                originListNew.add(0L);
                                originListNew.add(original);
                                centreListNew.add(originTime);
                                centreListNew.add(original);
                                centreTimeSon.add(originTime);
                                centreTimeSon.add(original);
                                originList.add(originTime);
                                originList.add(original);
                            }
                            if (isUpper) {
                                chi += (original - newTime);
                            } else {
                                zao += (newTime - original);
                            }
                        }
                    }
                } else {
                    if (original < originTime) {
                        originListNew.add(original);
                        originListNew.add(0L);
                        centreListNew.add(original);
                        centreListNew.add(0L);
                        centreTimeSon.add(original);
                        centreTimeSon.add(0L);
                        originList.add(original);
                        originList.add(0L);
                    } else {
                        originListNew.add(0L);
                        originListNew.add(original);
                        centreListNew.add(0L);
                        centreListNew.add(original);
                        centreTimeSon.add(0L);
                        centreTimeSon.add(original);
                        originList.add(0L);
                        originList.add(original);
                    }
                }
            }
            originTimeList.add(originList);
            centreTimeList.add(centreTimeSon);
        }
        System.out.println();
        System.out.println("-------------------- "+id_U+" --------------------");
        System.out.println("正常时间纠正New - centreListNew:");
        System.out.println(JSON.toJSONString(getTimeToStr(centreListNew)));
        System.out.println("正常时间 - originListNew:");
        System.out.println(JSON.toJSONString(getTimeToStr(originListNew)));
//        System.out.println("特殊时间 - specialTimeList:");
//        System.out.println(JSON.toJSONString(getTimeToStr(specialTimeList)));
        System.out.println("++++++++++++++++++++++++++++++++");
        System.out.println("正常每段时间纠正 - centreTimeList:");
        getTimeToListStr(centreTimeList);
        System.out.println("++++++++++++++++++++++++++++++++");
        System.out.println("正常每段时间 - originTimeList:");
        getTimeToListStr(originTimeList);
        long zon = 0;
        long jia = 0;
        long que = 0;
        long work;
        for (int i = 0; i < centreListNew.size(); i+=2) {
            long ownUpper = centreListNew.get(i);
            long ownBelow = centreListNew.get(i+1);
            if (ownUpper == 0 || ownBelow == 0) {
                continue;
            }
            zon += ownBelow - ownUpper;
        }
        for (int i = 0; i < arrTimeList.size(); i++) {
            List<Long> arrList = arrTimeList.get(i);
            List<Long> centreList = centreTimeList.get(i);
            boolean isQue = false;
            if (centreList.size() == 0) {
                isQue = true;
            } else if (centreList.size() == 2) {
                Long upper = centreList.get(0);
                Long below = centreList.get(1);
                if (upper == 0 || below == 0) {
                    isQue = true;
                }
            }
            if (isQue) {
                Long ownUpper = arrList.get(0);
                Long ownBelow = arrList.get(1);
                que += ownBelow - ownUpper;
            }
        }
        for (int i = 0; i < ovt.size(); i++) {
            Integer ovtIndex = ovt.getInteger(i);
            List<Long> centreList = centreTimeList.get(ovtIndex/2);
            if (centreList.size() > 0) {
                for (int j = 0; j < centreList.size(); j+=2) {
                    Long upper = centreList.get(j);
                    Long below = centreList.get(j + 1);
                    if (upper == 0 || below == 0) {
                        continue;
                    }
                    jia += below - upper;
                }
            }
        }
        work = zon - jia;
//        if (zon > teDur) {
//            zon = teDur;
//        }
        System.out.println("总上班时间 - zon:"+zon);
        System.out.println("普通上班时间: - work:"+work);
        System.out.println("迟到时间 - chi:"+chi);
        System.out.println("早退时间 - zao:"+zao);
        System.out.println("加班时间 - jia:"+jia);
        System.out.println("缺勤时间 - que:"+que);
        System.out.println("总要求上班时间 - teDur:"+teDur);
        if (isSumSpecialTime) {
            long te = 0;
            long start = arrTimeLong.get(0);
            long end = arrTimeLong.get(arrTimeLong.size()-1);
            // 开始或小于开始时间
            List<Long> startSpecialList = new ArrayList<>();
            // 结束或大于结束时间
            List<Long> endSpecialList = new ArrayList<>();
            for (Long da : userDates) {
                if (da <= start) {
                    startSpecialList.add(da);
                } else if (da >= end) {
                    endSpecialList.add(da);
                }
            }
            if (startSpecialList.size() > 0 && startSpecialList.size() % 2 != 0) {
                startSpecialList.remove(startSpecialList.size()-1);
            }
            if (endSpecialList.size() > 0 && endSpecialList.size() % 2 != 0) {
                endSpecialList.remove(0);
            }
            for (int i = 0; i < startSpecialList.size(); i+=2) {
                Long ownUpper = startSpecialList.get(i);
                Long ownBelow = startSpecialList.get(i + 1);
                te += ownBelow - ownUpper;
            }
            for (int i = 0; i < endSpecialList.size(); i+=2) {
                Long ownUpper = endSpecialList.get(i);
                Long ownBelow = endSpecialList.get(i + 1);
                te += ownBelow - ownUpper;
            }
            System.out.println("特殊- 开始 -时间 - startSpecialList:");
            System.out.println(JSON.toJSONString(getTimeToStr(startSpecialList)));
            System.out.println("特殊= 结束 =时间 - endSpecialList:");
            System.out.println(JSON.toJSONString(getTimeToStr(endSpecialList)));
            System.out.println("特殊上班时间 - te:"+te);
        }
    }

    public void testSpecialTime(List<Long> userDates,long teDur){
        if (null == userDates || userDates.size() == 0) {
            return;
        }
        if (userDates.size() % 2 != 0) {
            userDates.remove(userDates.size()-1);
        }
        long te = 0;
        long que = 0;
        for (int i = 0; i < userDates.size(); i+=2) {
            Long upper = userDates.get(i);
            Long below = userDates.get(i + 1);
            te += below - upper;
        }
        if (te > teDur) {
            te = teDur;
        } else {
            que = teDur - te;
        }
        System.out.println();
        System.out.println("用户上班时间 - userDates:");
        System.out.println(JSON.toJSONString(getTimeToStr(userDates)));
        System.out.println();
        System.out.println("特殊上班时间 - te:"+te);
        System.out.println("缺勤时间 - que:"+que);
        System.out.println("总要求上班时间 - teDur:"+teDur);
        System.out.println();
    }

//    public long[] getRangeTime(List<Long> arrTimeLong,List<List<Long>> arrTimeList,List<Long> dates
//            ,List<List<Long>> originTimeList,List<Long> originListNew,List<Long> centreListNew
//            ,boolean isAutoCardReplacement){
//        long[] result = new long[2];
//        long chi = 0L;
//        long zao = 0L;
//        for (int i = 0; i < arrTimeLong.size(); i+=2) {
//            Long upper = arrTimeLong.get(i);
//            Long below = arrTimeLong.get(i + 1);
//            List<Long> centreList = new ArrayList<>();
//            centreList.add(upper);
//            centreList.add(below);
//            arrTimeList.add(centreList);
//
//            List<Long> originList = new ArrayList<>();
//            List<Long> upperBelowBetween = getUpperBelowBetween(upper, below, dates, 60, 60);
//            if (upperBelowBetween.size() == 0) {
//                originTimeList.add(originList);
//                continue;
//            }
//            if (upperBelowBetween.size() > 4) {
//                int reduce = upperBelowBetween.size() - 4;
//                for (int j = 0; j < reduce; j++) {
//                    upperBelowBetween.remove(upperBelowBetween.size()-1);
//                }
//            }
//            if (upperBelowBetween.size() % 2 == 0) {
//                originListNew.addAll(upperBelowBetween);
//                originList.addAll(upperBelowBetween);
//                if (upperBelowBetween.get(0) <= upper) {
//                    centreListNew.add(upper);
//                } else {
//                    chi += upperBelowBetween.get(0) - upper;
//                    centreListNew.add(upperBelowBetween.get(0));
//                }
//                if (upperBelowBetween.size() == 4) {
//                    centreListNew.add(upperBelowBetween.get(1));
//                    centreListNew.add(upperBelowBetween.get(2));
//                }
//                if (upperBelowBetween.get(upperBelowBetween.size() - 1) >= below) {
//                    centreListNew.add(below);
//                } else {
//                    zao += below - upperBelowBetween.get(upperBelowBetween.size() - 1);
//                    centreListNew.add(upperBelowBetween.get(upperBelowBetween.size() - 1));
//                }
//            } else {
//                long original;
//                if (upperBelowBetween.size() == 3) {
//                    original = upperBelowBetween.get(upperBelowBetween.size() - 1);
//                } else {
//                    original = upperBelowBetween.get(0);
//                }
//                int originIndex = getContrastChkInTimeNew(original, arrTimeLong);
//                int newIndex;
//                boolean isUpper = true;
//                if(originIndex % 2 == 0){
//                    newIndex = originIndex+1;
//                    isUpper = false;
//                } else {
//                    newIndex = originIndex-1;
//                }
//                Long originTime = arrTimeLong.get(originIndex);
//                if (isAutoCardReplacement) {
//                    if (original < originTime) {
//                        originListNew.add(original);
//                        originListNew.add(0L);
//                        centreListNew.add(original);
//                        centreListNew.add(originTime);
//                        originList.add(original);
//                        originList.add(originTime);
//                    } else {
//                        originListNew.add(0L);
//                        originListNew.add(original);
//                        centreListNew.add(originTime);
//                        centreListNew.add(original);
//                        originList.add(originTime);
//                        originList.add(original);
//                    }
//                    Long newTime = arrTimeLong.get(newIndex);
//                    if (isUpper) {
//                        chi += (original - newTime);
//                    } else {
//                        zao += (newTime - original);
//                    }
//                } else {
//                    if (original < originTime) {
//                        originListNew.add(original);
//                        originListNew.add(0L);
//                        centreListNew.add(original);
//                        centreListNew.add(0L);
//                        originList.add(original);
//                        originList.add(0L);
//                    } else {
//                        originListNew.add(0L);
//                        originListNew.add(original);
//                        centreListNew.add(0L);
//                        centreListNew.add(original);
//                        originList.add(0L);
//                        originList.add(original);
//                    }
//                }
//            }
//            originTimeList.add(originList);
//        }
//        result[0] = chi;
//        result[1] = zao;
//        return result;
//    }

    public List<Long> getUserDates(JSONArray chkInEs){
        List<LogFlow> logFlows = new ArrayList<>();
        for (int i = 0; i < chkInEs.size(); i++) {
            logFlows.add(JSONObject.parseObject(JSON.toJSONString(chkInEs.getJSONObject(i)), LogFlow.class));
        }
        //lambda表达式实现List接口sort方法排序
        logFlows.sort(Comparator.comparing(num -> num.getData().getString("date")));
        List<Long> dates = new ArrayList<>();
        for (LogFlow logFlow : logFlows) {
            JSONObject data = logFlow.getData();
            String date1 = data.getString("date");
            dates.add(getDeLong(date1));
        }
        return dates;
    }
    public List<Long> getArrTime(JSONArray arrTime,String theSameDay){
        // 正常规定上班时间
        List<Long> arrTimeLong = new ArrayList<>();
        for (int i = 0; i < arrTime.size(); i++) {
            arrTimeLong.add(getDeLong(theSameDay+" "+arrTime.getString(i)));
        }
        return arrTimeLong;
    }
    public List<String> getTimeToStr(List<Long> timeList){
        List<String> correctWorkDateStr = new ArrayList<>();
        for (Long aLong : timeList) {
            correctWorkDateStr.add(getDeDate(aLong));
        }
        return correctWorkDateStr;
    }
    public void getTimeToListStr(List<List<Long>> timeList){
        for (List<Long> longs : timeList) {
            List<String> correctWorkDateStr = new ArrayList<>();
            for (Long aLong : longs) {
                correctWorkDateStr.add(getDeDate(aLong));
            }
            System.out.println(JSON.toJSONString(correctWorkDateStr));
        }
        System.out.println("---------------------");
    }

    public long getDeLong(String date){
//        String timeString = "2021-01-01 08:33:00";
//        String pattern = "yyyy-MM-dd HH:mm:ss";

        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate());
        try {
            Date dateNew = sdf.parse(date);
            long timestamp = dateNew.getTime();
//            System.out.println("时间戳：" + (timestamp/1000));
            return (timestamp/1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getDeDate(long date){
        if (date == 0) {
            return "0";
        }
        Date dateNew = new Date(date*1000);
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate());
        return sdf.format(dateNew);
    }

//    public List<Long> getContrastChkInTime(long original,List<Long> arrTimeLong){
//        System.out.println("duo:");
//        System.out.println(original);
//        List<Long> countList = new ArrayList<>();
//        List<Long> resultList = new ArrayList<>();
////        for (int i = 1; i < arrTimeLong.size()-1; i++) {
////            Long ti = arrTimeLong.get(i);
////            long re = original - ti;
////            countList.add((re < 0) ? -re : re);
////        }
//        for (Long ti : arrTimeLong) {
//            long re = original - ti;
//            countList.add((re < 0) ? -re : re);
//        }
//        int index = 0;
//        long mix = countList.get(0);
//        for (int i = 1; i < countList.size(); i++) {
//            if (countList.get(i) < mix) {
//                mix = countList.get(i);
//                index = i;
//            }
//        }
////        index+=1;
//        System.out.println("resultList:");
//        System.out.println(JSON.toJSONString(countList));
//        System.out.println("re:"+arrTimeLong.get(index)+" , index:"+(index));
//        if (mix < 3600) {
//            resultList.add(arrTimeLong.get(index));
////            return arrTimeLong.get(index);
//            return resultList;
//        }
//        resultList.add(0L);
//        resultList.add(arrTimeLong.get(index));
////        return 0;
//        return resultList;
//    }
    public int getContrastChkInTimeNew(long original,List<Long> arrTimeLong){
        List<Long> countList = new ArrayList<>();
        for (Long ti : arrTimeLong) {
            long re = original - ti;
            countList.add((re < 0) ? -re : re);
        }
        int index = 0;
        long mix = countList.get(0);
        for (int i = 1; i < countList.size(); i++) {
            if (countList.get(i) < mix) {
                mix = countList.get(i);
                index = i;
            }
        }
//        System.out.println("resultList:");
//        System.out.println(JSON.toJSONString(countList));
//        System.out.println("re:"+arrTimeLong.get(index)+" , index:"+(index));
        return index;
    }

//    public long getContrastChkInTime(long original,long timeLong){
////        System.out.println("dang:"+original+" , timeLong:"+timeLong);
//        long l = original - timeLong;
////        System.out.println("l:"+l);
//        long re = (l < 0) ? -l : l;
////        System.out.println("re:"+re);
//        if (re < 3600) {
//            return timeLong;
//        }
//        return 0;
//    }
//    public List<Long> getContrastChkInTimeNew(long original,long timeLong){
//        List<Long> result = new ArrayList<>();
////        System.out.println("dang:"+original+" , timeLong:"+timeLong);
//        long l = original - timeLong;
////        System.out.println("l:"+l);
//        long re = (l < 0) ? -l : l;
////        System.out.println("re:"+re);
//        if (re < 3600) {
//            result.add(timeLong);
//            return result;
//        }
//        result.add(0L);
//        result.add(timeLong);
//        return result;
//    }
    public List<Long> getUpperBelowBetween(long upper,long below,List<Long> originList,long upperRange,long belowRange){
        List<Long> resultList = new ArrayList<>();
        upperRange *= 60;
        belowRange *= 60;
        upper -= upperRange;
        below += belowRange;
        for (Long origin : originList) {
            if (origin >= upper && origin <= below) {
                resultList.add(origin);
            }
        }
        return resultList;
    }
}
