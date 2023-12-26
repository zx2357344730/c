package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.ZjTestService;
import com.cresign.purchase.utils.ExcelUtils;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CosUpload;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.es.lBUser;
import com.cresign.tools.pojo.es.lNComp;
import com.cresign.tools.pojo.es.lNUser;
import com.cresign.tools.pojo.es.lSBComp;
import com.cresign.tools.pojo.po.*;
import com.cresign.tools.pojo.po.compCard.CompInfo;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.cresign.tools.pojo.po.userCard.UserInfo;
import com.cresign.tools.uuid.UUID19;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    @Autowired
    private CosUpload cos;
    private static final String sharePrefix = "share";

    @Override
    public ApiResponse getMdSetEs(String key, String esIndex,String condition,String val) {
        if (null == key || "".equals(key)) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
        UUID uuid = UUID.randomUUID();
        System.out.println("输出:");
        System.out.println(uuid);
//        String shareId = (uuid+"").replace("-","");
        String shareId = (uuid+"").replace("-","");
        System.out.println(shareId);
//        System.out.println(JSON.toJSONString(jsonObject));
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
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                ERR_SHARE_NULL.getCode(),"");
    }

    @Override
    public ApiResponse initFC(String id_C,String id_U) {
        Asset asset = qt.getConfig(id_C, "a-auth", "flowControl");
        if (null == asset || null == asset.getFlowControl() || null == asset.getFlowControl().getJSONArray("objData")) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
//            JSONObject result = new JSONObject();
//            result.put("shareId",shareId);
            return retResult.ok(CodeEnum.OK.getCode(), shareId);
        }
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
//            JSONObject result = new JSONObject();
//            result.put("shareId",shareId);
            return retResult.ok(CodeEnum.OK.getCode(), shareId);
        }
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
//            JSONObject result = new JSONObject();
//            result.put("shareId",shareId);
            return retResult.ok(CodeEnum.OK.getCode(), shareId);
        }
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
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

    @Override
    public ApiResponse addBlankCompNew(JSONObject tokData, JSONObject wrdN, JSONObject wrddesc, String pic, String ref) {
        String new_id_C = qt.GetObjectId();

        InitJava init = qt.getInitData();
        JSONObject newSpace = init.getNewSpace();
        Comp comp = qt.jsonTo(newSpace.getJSONObject("comp"), Comp.class);
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
        JSONObject coreObject = newSpace.getJSONObject("a-core");
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
        JSONObject authObject = newSpace.getJSONObject("a-auth");

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
        JSONObject asset = this.createAsset(new_id_C, qt.GetObjectId(), "a-auth", authObject);
        System.out.println(JSON.toJSONString(asset));

        return retResult.ok(CodeEnum.OK.getCode(),new_id_C);
    }

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

    @Override
    public ApiResponse removeUser(String id_U) {
        qt.delMD(id_U, User.class);
        qt.delES("lBUser",qt.setESFilt("id_U","exact",id_U));
        JSONArray lNUser = qt.getES("lNUser", qt.setESFilt("id_U", "exact",id_U));
        if (null != lNUser && lNUser.size() > 0) {
            String id_C = lNUser.getJSONObject(0).getString("id_C");
            if (null != id_C && !"".equals(id_C)) {
                qt.delES("lsbComp",qt.setESFilt("id_C","exact",id_C));
            }
        }
        qt.delES("lNUser",qt.setESFilt("id_U","exact",id_U));
        return retResult.ok(CodeEnum.OK.getCode(), "删除成功");
    }

    /**
     * 获取二维码内容URL
     * @param id_C  公司编号
     * @return  二维码内容
     */
    @Override
    public ApiResponse genChkinCode(String id_C) {

//        return retResult.ok(CodeEnum.OK.getCode(), "创建二维码成功");

        String QR_URL = "https://www.cresign.cn/qrCodeTest?qrType=qrChkIn&t=";
//        String QR_URL = "http://127.0.0.1:8080/qrCodeTest?qrType=qrChkIn&t=";
        String token = UUID19.uuid();

        qt.setRDSet("chk_in",token,id_C,60L);
        String url = QR_URL + token;
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }

    /**
     * 扫码后打卡处理
     * @param id_U  用户编号
     * @param token 二维码标识
     * @return  扫码结果
     */
    @Override
    public ApiResponse scanChkinCode(String id_U, String token) {
        String chk_in = qt.getRDSetStr("chk_in", token);
        if (null == chk_in) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ADDINDEX_ERROR.getCode(),null);
        }
        User user = qt.getMDContent(id_U,qt.strList("info"), User.class);
        if (user == null) {
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
        }
        UserInfo userInfo = user.getInfo();
        // 创建日志
        LogFlow logFlow = new LogFlow();
        // 设置日志基础属性
        logFlow.setImp(3);
        logFlow.setLogType("chkin");
        logFlow.setSubType("normal");
        logFlow.setZcndesc("打卡");
        logFlow.setId_U(id_U);
        logFlow.setId_Us(qt.setArray(id_U));
        logFlow.setId_C(chk_in);
        logFlow.setWrdN(userInfo.getWrdN());
        logFlow.setWrdNU(userInfo.getWrdNReal());
        logFlow.setPic(userInfo.getPic());
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        // 创建日志详细数据
        JSONObject data = new JSONObject();
        // 设置日志详细数据
        data.put("date",DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        data.put("type","normal");
        data.put("chkType","normal");
        data.put("locLat","0");
        data.put("locLong","0");
        // 获取当前时间日期
        LocalDate date = getDate(data.getString("date"));
        // 添加当前时间日期
        data.put("theSameDay",date.getYear()+"/"+ date.getMonthValue()+"/"+ date.getDayOfMonth());
        logFlow.setData(data);
        ws.sendWS(logFlow);
        return retResult.ok(CodeEnum.OK.getCode(), "打卡成功");
    }

    @Override
    public ApiResponse getOnLine(String id_U) {
        JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, id_U); // appID? /mqKey
        // 判断redis信息为空
        if (null == rdInfo) {
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, ErrEnum.ERR_GET_DATA_NULL.getCode(), null);
        }
        JSONObject result = new JSONObject();
        for (String cli : rdInfo.keySet()) {
            JSONObject cliInfo = rdInfo.getJSONObject(cli);
            if (null != cliInfo && cliInfo.containsKey("wsData")) {
                result.put(cli,"true");
            } else {
                result.put(cli,"false");
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    @Override
    public ApiResponse delLBUser(String id_U,String id_C) {
        qt.delES("lBUser",qt.setESFilt("id_U",id_U,"id_C",id_C));
        User user = qt.getMDContent(id_U, "rolex", User.class);
        if (user == null || null == user.getRolex()) {
            throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode(), null);
        }
        JSONObject rolex = user.getRolex();
        JSONObject objComp = rolex.getJSONObject("objComp");
        objComp.remove(id_C);
        qt.setMDContent(id_U,qt.setJson("rolex"), User.class);
        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
    }

    @Override
    public ApiResponse testEx(String id_C,String fileName,String id_U
            ,int subTypeStatus,String year,String month,JSONArray arrField) {
        /*
         {
            "filtKey": "id_C",
            "method": "eq",
            "filtVal": ""
         }
         */

        /*
    [
        {
            "isEx": true,
            "field": "wrdN",
            "txt": "名称",
            "valType": "lang",
            "isWarp": true
        },
        {
            "isEx": true,
            "field": "wrddesc",
            "txt": "描述",
            "valType": "lang",
            "isWarp": true
        },
        {
            "isEx": true,
            "field": "ref",
            "txt": "编号",
            "valType": "String",
            "isWarp": true
        },
        {
            "isEx": true,
            "field": "tmd",
            "txt": "更新日期",
            "valType": "String",
            "isWarp": true
        }
    ]
         */
//        String id_C = "6076a1c7f3861e40c87fd294";
//        String fileName = "chkin统计表";
        JSONObject exData = getExData(id_C, id_U, subTypeStatus, year, month, arrField);
        try {
            File excel = ExcelUtils.createExcel2(exData.getJSONArray("arrayField"), exData.getJSONArray("arrayExcel"), new JSONArray(), new JSONArray());
            // 上传到cfiles桶
//            cos.uploadCFiles()
            // 上传到cresign桶
            JSONObject jsonUpload = cos.uploadCresignStat(excel,  "Chkin/" + DateUtils.getDateNow(DateEnum.DATE_FOLDER.getDate()) + "/", fileName + DateUtils.getDateNow(DateEnum.DATE_FOLDER_FULL.getDate()));
            qt.checkCapacity(id_C, jsonUpload.getLong("size"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public JSONObject getExData(String id_C,String id_U,int subTypeStatus,String year,String month,JSONArray arrField) {
        String subType;
        if (subTypeStatus == 1) {
            subType = "monthChkin";
        } else {
            subType = "dayChkin";
        }
        JSONArray filterArray = qt.setESFilt("id_C",id_C,"id_U",id_U
                ,"subType",subType,"data.year",year,"data.month",month);
        JSONArray arrayEs = qt.getES("usageflow", filterArray);
        JSONArray arrayField = new JSONArray();
        JSONObject fieldObj;
        for (int i = 0; i < arrField.size(); i++) {
            JSONObject arrObj = arrField.getJSONObject(i);
            fieldObj = new JSONObject();
            fieldObj.put("isEx",true);
            fieldObj.put("field",arrObj.getString("field"));
            fieldObj.put("txt",arrObj.getString("txt"));
            fieldObj.put("valType",arrObj.getString("valType"));
            fieldObj.put("isWarp",true);
            fieldObj.put("maxWidth",20000);
            fieldObj.put("align","center");
            arrayField.add(fieldObj);
        }
        System.out.println("arrayEs:");
        System.out.println(JSON.toJSONString(arrayEs));
        JSONArray arrayResult = new JSONArray();
        for (int i = 0; i < arrayEs.size(); i++) {
            JSONObject jsonEs = arrayEs.getJSONObject(i);
            JSONArray arrayRow = new JSONArray();
            for (int j = 0; j < arrayField.size(); j++) {
                JSONObject jsonField = arrayField.getJSONObject(j);
                String field = jsonField.getString("field");
                String valType = jsonField.getString("valType");

                switch (valType) {
                    case "String":
                        if (jsonEs == null || jsonEs.getString(field) == null) {
                            arrayRow.add("");
                        } else {
                            arrayRow.add(jsonEs.getString(field));
                        }
                        break;
                    case "Integer":
                        if (jsonEs == null || jsonEs.getInteger(field) == null) {
                            arrayRow.add(0);
                        } else {
                            arrayRow.add(jsonEs.getInteger(field));
                        }
                        break;
                    case "Double":
                        if (jsonEs == null || jsonEs.getDouble(field) == null) {
                            arrayRow.add(0.0);
                        } else {
                            arrayRow.add(jsonEs.getDouble(field));
                        }
                        break;
                    case "Long":
                        if (jsonEs == null || jsonEs.getLong(field) == null) {
                            arrayRow.add(0L);
                        } else {
                            arrayRow.add(jsonEs.getLong(field));
                        }
                        break;
                    case "lang":
                        if (jsonEs == null || jsonEs.getJSONObject(field) == null || jsonEs.getJSONObject(field).getString("cn") == null) {
                            arrayRow.add("");
                        } else {
                            arrayRow.add(jsonEs.getJSONObject(field).getString("cn"));
                        }
                        break;
                    case "logData":
                        if (jsonEs == null || jsonEs.getJSONObject("data") == null || jsonEs.getJSONObject("data").getString(field) == null) {
                            arrayRow.add("");
                        } else {
                            arrayRow.add(""+jsonEs.getJSONObject("data").getString(field)+"  ");
                        }
                        break;
                    case "chkInData":
                        if (jsonEs == null || jsonEs.getJSONObject("data") == null
                                || jsonEs.getJSONObject("data").getJSONObject("chkInData") == null
                                || jsonEs.getJSONObject("data").getJSONObject("chkInData").getString(field) == null) {
                            arrayRow.add("");
                        } else {
                            switch (field) {
                                // 总要求上班时间
                                case "teDur":
                                // 总上班时间
                                case "taAll":
                                // 普通上班时间
                                case "taDur":
                                // 加班时间
                                case "taOver": {
                                    Long fieldVal = jsonEs.getJSONObject("data").getJSONObject("chkInData").getLong(field);
                                    arrayRow.add("" + (fieldVal / 60 / 60) + "(小时)");
                                    break;
                                }
                                // 缺勤时间
                                case "taMiss":
                                // 早退时间
                                case "taPre":
                                // 特殊上班时间
                                case "taExtra":
                                // 迟到时间
                                case "taLate": {
                                    Long fieldVal = jsonEs.getJSONObject("data").getJSONObject("chkInData").getLong(field);
                                    arrayRow.add("" + (fieldVal / 60) + "(分钟)");
                                    break;
                                }
                                // 特殊上班开始时间集合
                                case "taPex":
                                // 特殊上班结束时间集合
                                case "taNex":
                                // 正常上班时间
                                case "arrTime": {
                                    JSONArray fieldArr = jsonEs.getJSONObject("data").getJSONObject("chkInData").getJSONArray(field);
                                    if (null != fieldArr && fieldArr.size() > 0) {
                                        JSONArray timeToStr = getTimeToStr(fieldArr);
                                        StringBuilder timeStr = new StringBuilder();
                                        for (int v = 0; v < timeToStr.size(); v++) {
                                            String val = timeToStr.getString(v);
                                            String[] sp = val.split(" ");
                                            if (v >= timeToStr.size() - 1) {
                                                timeStr.append(sp[1]);
                                            } else {
                                                timeStr.append(sp[1]).append(",");
                                            }
                                        }
                                        arrayRow.add("" + timeStr + "");
                                    } else {
                                        arrayRow.add("    无    ");
                                    }
                                    break;
                                }
                                // 是否旷工
                                case "isAEM": {
                                    boolean fieldVal = jsonEs.getJSONObject("data").getJSONObject("chkInData").getBoolean(field);
                                    arrayRow.add("" + (fieldVal ? "是" : "否") + "    ");
                                    break;
                                }
                                default:
                                    arrayRow.add("" + jsonEs.getJSONObject("data").getJSONObject("chkInData").getString(field) + "    ");
                                    break;
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + valType);
                }
            }
            arrayResult.add(arrayRow);
        }
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("arrayExcel", arrayResult);
        jsonResult.put("arrayField", arrayField);
        return jsonResult;
    }
    /**
     * 将long时间转换成字符串时间
     * @param timeList  long时间集合
     * @return  转换后的字符串时间集合
     */
    public JSONArray getTimeToStr(JSONArray timeList){
        JSONArray correctWorkDateStr = new JSONArray();
        for (int i = 0; i < timeList.size(); i++) {
            correctWorkDateStr.add(getDeDate(timeList.getLong(i)));
        }
        return correctWorkDateStr;
    }
    /**
     * 将date转换成字符串时间
     * @param date  long时间
     * @return  字符串时间
     */
    public String getDeDate(long date){
        if (date == 0) {
            return "0";
        }
        Date dateNew = new Date(date*1000);
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate());
        return sdf.format(dateNew);
    }

    public LocalDate getDate(String dateStr){
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DateEnum.DATE_TIME_FULL.getDate()));
    }
}
