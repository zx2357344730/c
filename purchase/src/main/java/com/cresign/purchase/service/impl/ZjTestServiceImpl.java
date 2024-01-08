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
import com.cresign.tools.pojo.es.*;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

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


        return retResult.ok(CodeEnum.OK.getCode(),addCompSp(tokData.getString("id_U"),tokData.getString("id_C")
                ,wrdN,wrddesc,pic,ref));
    }

    private String addCompSp(String id_U,String id_C, JSONObject wrdN, JSONObject wrddesc, String pic, String ref){
        String new_id_C = qt.GetObjectId();
        if (null == id_C || "".equals(id_C)) {
            id_C = new_id_C;
        }

        InitJava init = qt.getInitData();
        JSONObject newSpace = init.getNewSpace();
        Comp comp = qt.jsonTo(newSpace.getJSONObject("comp"), Comp.class);
//        String uid = tokData.getString("id_U");

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

        qt.setMDContent(id_U,qt.setJson("rolex.objComp."+new_id_C,rolex), User.class);

        //a-core
        JSONObject coreObject = newSpace.getJSONObject("a-core");
        coreObject.getJSONObject("info").put("id_C",new_id_C);
        coreObject.getJSONObject("info").put("tmk", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        coreObject.getJSONObject("info").put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

        // add me into control's a-core-3 as the only User

        if (null == coreObject.getJSONObject("control")) {
            JSONObject control = new JSONObject();
            JSONObject objMod = new JSONObject();
            JSONObject a_core = new JSONObject();
            JSONArray id_UArr = new JSONArray();
            id_UArr.add(id_U);
            a_core.put("id_U",id_UArr);
            objMod.put("a-core-3",a_core);
            control.put("objMod",objMod);
            coreObject.put("control",control);
        } else {
            coreObject.getJSONObject("control").getJSONObject("objMod").getJSONObject("a-core-3").getJSONArray("id_U").add(id_U);
        }
        //调用
        this.createAsset(new_id_C, qt.GetObjectId() ,"a-core",coreObject);

        User user = qt.getMDContent(id_U, "info", User.class);

        lBUser lbuser = new lBUser(id_U,new_id_C,user.getInfo().getWrdN(),comp.getInfo().getWrdN(),
                user.getInfo().getWrdNReal(),user.getInfo().getWrddesc(),"1001",user.getInfo().getMbn(),
                "",user.getInfo().getId_WX(),user.getInfo().getPic(),"1000");

        qt.addES( "lbuser", lbuser);

        Comp cSeller = qt.getMDContent(id_C, "info", Comp.class);

        lNComp lncomp = new lNComp(new_id_C,new_id_C,comp.getInfo().getWrdN(),comp.getInfo().getWrddesc(),comp.getInfo().getRef(),comp.getInfo().getPic());

        lSBComp lsbcomp = new lSBComp(id_C,id_C,new_id_C,new_id_C, cSeller.getInfo().getWrdN(),cSeller.getInfo().getWrddesc(),
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
            userFlowData.put("id_U", id_U);
            userFlowData.put("id_APP", user.getInfo().getId_APP());
            userFlowData.put("imp", 3);
            flowList.getJSONObject(i).getJSONArray("objUser").add(userFlowData);
        }

        //调用
        JSONObject asset = this.createAsset(new_id_C, qt.GetObjectId(), "a-auth", authObject);
        System.out.println(JSON.toJSONString(asset));
        return new_id_C;
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
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LOG_FAILF.getCode(),e.getMessage());
        }
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                LOG_FAILF.getCode(),"");
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
        if (null == arrayEs || arrayEs.size() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LOG_GET_DATA_NULL.getCode(),"");
        }
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

    @Override
    public ApiResponse addCompSpace(String id_U,String id_C, JSONObject wrdN, JSONObject wrddesc, String pic, String ref) {
        JSONArray lNUser = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
        if (null == lNUser || lNUser.size() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LN_USER_NOT_FOUND.getCode(),"");
        }
//        String compId = qt.GetObjectId();
        JSONObject user = lNUser.getJSONObject(0);
//        InitJava init = qt.getInitData();
//        JSONObject newSpace = init.getNewSpace();
//        Comp comp = qt.jsonTo(newSpace.getJSONObject("comp"), Comp.class);
//        //如果reqJson为空，则添加默认公司，否则从reqJson里面取公司基本信息
//        JSONObject wrdN = new JSONObject();
//        wrdN.put("cn",id_U+"空间");
//        //用户填写公司信息
//        comp.getInfo().setWrdN(wrdN);
//        JSONObject wrddesc = new JSONObject();
//        wrddesc.put("cn","空间");
//        comp.getInfo().setWrddesc(wrddesc);
//        comp.getInfo().setPic("");
//        comp.getInfo().setRef(id_U+"Ref");
//
//
//        comp.getInfo().setId_CP(compId);
//        comp.getInfo().setId_CM(compId);
//        comp.getInfo().setId_C(compId);
//        comp.getInfo().setTmk(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//        comp.getInfo().setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//
//        //真公司标志
//        comp.setBcdNet(1);
//        comp.setId(compId);
//        JSONArray view = new JSONArray();
//        view.add("info");
//        view.add("contract");
//        view.add("view");
//        comp.setView(view);
//        comp.setContract(new JSONObject());
//        qt.addMD(comp);
        String compId = addCompSp(id_U,id_C, wrdN,wrddesc,pic,ref);
        qt.setES("lNUser",user.getString("id_ES"),qt.setJson("id_C",compId));
//        Comp comp = new Comp();
//        CompInfo infoComp = new CompInfo();
//        infoComp.setTmk(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//        infoComp.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//        infoComp.setId_C(compId);
//        infoComp.setRef(id_U+"Comp");
//        JSONObject wrdN = new JSONObject();
//        wrdN.put("cn",id_U+"-公司");
//        infoComp.setWrdN(wrdN);

        return retResult.ok(CodeEnum.OK.getCode(), compId);
    }

    @Override
    public ApiResponse addWorkContract(String id_U,String id_CB,int money,int year
            ,JSONObject contJ,JSONObject contY,String grpB,String dep) {
        JSONArray lNUser = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
        if (null == lNUser || lNUser.size() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LN_USER_NOT_FOUND.getCode(),"");
        }
        JSONObject user = lNUser.getJSONObject(0);
        String id_C = user.getString("id_C");
        Comp comp = qt.getMDContent(id_C, "", Comp.class);
        if (null == comp || null == comp.getContract()) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    COMP_NOT_FOUND.getCode(),"");
        }
        JSONObject contract = comp.getContract();
        JSONObject objOrder = contract.getJSONObject("objOrder");
        if (null == objOrder) {
            objOrder = new JSONObject();
        } else {
            if (null != objOrder.getString("id_O")) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                        LOG_FAILF.getCode(),"");
            }
        }
        String orderId = qt.GetObjectId();
        objOrder.put("id_O",orderId);
        objOrder.put("time",DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        objOrder.put("id_U",id_U);
        qt.setMDContent(id_C,qt.setJson("contract.objOrder",objOrder), Comp.class);
        Order order = new Order();
        order.setId(orderId);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId_C(id_C);
        orderInfo.setId_CB(id_CB);
        JSONObject orderNameCas = new JSONObject();
        orderNameCas.put("cn",id_U+":合同");
        orderInfo.setWrdN(orderNameCas);
        JSONObject actionOrder = new JSONObject();
        order.setAction(actionOrder);
        JSONObject oItemOrder = new JSONObject();
        order.setOItem(oItemOrder);
        JSONObject workOrder = new JSONObject();
        JSONObject objWork = new JSONObject();
        objWork.put("id_U",id_U);
        objWork.put("hourMoney",money);
        objWork.put("contractYear",year);
        objWork.put("time",DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        objWork.put("contractContent","甲方：" +
                "乙方：性别 ： 身份证号码：" +
                "因原因，乙方向甲方申请中止劳动合同，经双方协商，订立本协议，以便共同遵守。" +
                "一、劳动合同中止期限自 年月 日起至 年 月 日止，共计年 月。" +
                "二、劳动合同中止期间，乙方不在甲方的工作时间不计算本企业的工作年限，乙方的任何行为均与甲方无关，乙方对自己的行为负责。" +
                "三、劳动合同中止期间，乙方不享受甲方的工资、奖金、专业技术职务津贴等薪酬福利待遇。" +
                "四、劳动合同中止期间，乙方的社会保险关系停止缴纳。" +
                "五、因乙方离岗日期较长，甲方不再保留乙方的岗位。" +
                "六、劳动合同中止期满，乙方愿意回甲方工作，须在一个月前提出申请，甲方可根据实际岗位需要对乙方进行考评，合适的予以安排上岗。" +
                "七、劳动合同中止协议期满后一个月内，乙方未回原单位的，甲方可按自动离职予以处理。" +
                "八、劳动合同中止期满，乙方若提出继续签订劳动合同中止协议，须经甲方同意后重新签订协议。" +
                "九、本协议在履行期间，如遇政策调整等客观因素发生，变化时，可以依照相关政策执行。" +
                "十、本协议自劳动合同中止期限之日起生效，在协议执行期间，双方不得随意变更或解除协议，本协议未尽事宜，应由双方根据国家的有关政策规定进行协商，作出补充规定。补充规定与本协议具有同等效力。" +
                "十一、本协议一式两份，甲乙双方各执一份。");
        objWork.put("contractJ",contJ);
        objWork.put("contractY",contY);
        objWork.put("grpB",grpB);
        objWork.put("dep",dep);
        workOrder.put("objWork",objWork);
        order.setWork(workOrder);
        qt.addMD(order);
        // 创建lSBOrder订单
        lSBOrder lsbOrder = new lSBOrder(id_C,id_CB,"","","",orderId, new JSONArray(),
                "","",null,"1000","",4,0,orderNameCas,null,null);
        qt.addES("lSBOrder",lsbOrder);
        return retResult.ok(CodeEnum.OK.getCode(),"创建成功");
    }

    @Override
    public ApiResponse sumTimeChkIn(String id_C,String id_U,int subTypeStatus,int year,JSONArray monthArr) {
        JSONObject result = new JSONObject();
        String subType;
        if (subTypeStatus == 1) {
            subType = "monthChkin";
        } else {
            subType = "dayChkin";
        }
        Comp comp = qt.getMDContent(id_C, "contract", Comp.class);
        if (null == comp || null == comp.getContract()) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LOG_GET_DATA_NULL.getCode(),"");
        }
        JSONObject contract = comp.getContract();
        JSONObject objOrder = contract.getJSONObject("objOrder");
        String id_O = objOrder.getString("id_O");
        Order order = qt.getMDContent(id_O, "work", Order.class);
        if (null == order || null == order.getWork()) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LOG_GET_DATA_NULL.getCode(),"");
        }
        Asset asset = qt.getConfig(id_C, "a-chkin", "chkin");
        if (null == asset || null == asset.getChkin()) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LOG_GET_DATA_NULL.getCode(),"");
        }
        JSONObject chkin = asset.getChkin();
        JSONObject objChkin = chkin.getJSONObject("objChkin");
        JSONObject work = order.getWork();
        JSONObject objWork = work.getJSONObject("objWork");
        // 一小时钱
        double hourMoney = objWork.getDouble("hourMoney");
        String grpB = objWork.getString("grpB");
        String dep = objWork.getString("dep");
        JSONObject chkDep = objChkin.getJSONObject(dep);
        JSONObject chkGrpB = chkDep.getJSONObject(grpB);
        // 迟到扣钱
        Integer late = chkGrpB.getInteger("late");
        // 缺勤扣钱
        Integer miss = chkGrpB.getInteger("miss");
        // 特殊上班钱倍率
        double extra = chkGrpB.getDouble("extra");
        // 早退扣钱
        Integer pre = chkGrpB.getInteger("pre");
        // 加班倍率
        double overtime = chkGrpB.getDouble("overtime");
        for (int m = 0; m < monthArr.size(); m++) {
            JSONArray sumData = new JSONArray();
            int month = monthArr.getInteger(m);
            JSONArray filterArray = qt.setESFilt("id_C",id_C,"id_U",id_U
                    ,"subType",subType,"data.year",year,"data.month",month);
            JSONArray arrayEs = qt.getES("usageflow", filterArray);
            if (null == arrayEs || arrayEs.size() == 0) {
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
//                        LOG_GET_DATA_NULL.getCode(),"");
                result.put(m+"",sumData);
                continue;
            }

//        // 特殊加班倍率
//        double overtimeExtra = objWork.getDouble("overtimeExtra");

//        int taDurMoneyAll = 0;
//        double taOverMoneyAll = 0;
//        int taMissMoneyAll = 0;
//        int taPreMoneyAll = 0;
//        double taExtraMoneyAll = 0;
//        int taLateMoneyAll = 0;
            double taDurMoney;
            double taOverMoney;
            int taMissMoney;
            int taPreMoney;
            double taExtraMoney;
            int taLateMoney;
            for (int i = 0; i < arrayEs.size(); i++) {
                JSONObject obj = arrayEs.getJSONObject(i);
                JSONObject data = new JSONObject();
                // 获取当前月份
                data.put("month",obj.getJSONObject("data").getString("month"));
                // 获取当前年份
                data.put("year",obj.getJSONObject("data").getString("year"));
                if (subTypeStatus == 1) {
                    JSONObject userChkInMonthData = obj.getJSONObject("data")
                            .getJSONObject("chkInData").getJSONObject("userChkInMonthData");
                    // 获取月普通上班时间
                    Long taDurAll = userChkInMonthData.getLong("taDurAll");
                    taDurMoney = Integer.parseInt(taDurAll+"") * hourMoney;
                    // 获取月加班时间
                    Long taOverAll = userChkInMonthData.getLong("taOverAll");
                    taOverMoney = Integer.getInteger(taOverAll+"") * overtime;
                    // 获取月缺勤次数
                    Integer taMissSumAll = userChkInMonthData.getInteger("taMissSumAll");
                    taMissMoney = taMissSumAll * miss;
                    // 获取月早退次数
                    Integer earlySumAll = userChkInMonthData.getInteger("earlySumAll");
                    taPreMoney = earlySumAll * pre;
                    // 获取月特殊上班时间
                    Long taExtraAll = userChkInMonthData.getLong("taExtraAll");
                    taExtraMoney = taExtraAll * extra;
                    // 获取月迟到次数
                    Integer lateSumAll = userChkInMonthData.getInteger("lateSumAll");
                    taLateMoney = lateSumAll * late;
                    // 获取当月旷工总数
                    Integer aemSum = userChkInMonthData.getInteger("AEMSum");
                    data.put("aemSum",aemSum);
//                JSONObject monthData = new JSONObject();
//                monthData.put("taDurMoney",taDurMoney);
//                monthData.put("taOverMoney",taOverMoney);
//                monthData.put("taMissMoney",taMissMoney);
//                monthData.put("taPreMoney",taPreMoney);
//                monthData.put("taExtraMoney",taExtraMoney);
//                monthData.put("taLateMoney",taLateMoney);
//                monthData.put("theSameDay",obj.getJSONObject("data").getString("theSameDay"));
//                sumData.add(monthData);
                } else {
                    JSONObject chkInData = obj.getJSONObject("data").getJSONObject("chkInData");
                    // 普通上班时间
                    long taDur = chkInData.getLong("taDur");
                    taDurMoney = Integer.parseInt(taDur+"") * hourMoney;
                    // 加班时间
                    long taOver = chkInData.getLong("taOver");
                    taOverMoney = Integer.getInteger(taOver+"") * overtime;
//                boolean isOvertimeExtra = chkInData.getBoolean("isOvertimeExtra");
//                double taOverMoney;
//                if (isOvertimeExtra) {
//                    taOverMoney = Integer.getInteger(taOver+"") * overtimeExtra;
//                } else {
//                    taOverMoney = Integer.getInteger(taOver+"") * overtime;
//                }
                    // 缺勤次数
                    int taMissSum = chkInData.getInteger("taMissSum");
                    taMissMoney = taMissSum * miss;
                    // 早退次数
                    int taPreSum = chkInData.getInteger("taPreSum");
                    taPreMoney = taPreSum * pre;
                    // 特殊上班时间
                    long taExtra = chkInData.getLong("taExtra");
                    taExtraMoney = taExtra * extra;
                    // 迟到次数
                    int taLateSum = chkInData.getInteger("taLateSum");
                    taLateMoney = taLateSum * late;
                    // 是否旷工
                    boolean isAEM = chkInData.getBoolean("isAEM");
                    data.put("isAEM",isAEM);
                    // 获取当前日期
                    data.put("theSameDay",obj.getJSONObject("data").getString("theSameDay"));
//                taDurMoneyAll += taDurMoney;
//                taOverMoneyAll += taOverMoney;
//                taMissMoneyAll += taMissMoney;
//                taPreMoneyAll += taPreMoney;
//                taExtraMoneyAll += taExtraMoney;
//                taLateMoneyAll += taLateMoney;
//                JSONObject dayData = new JSONObject();
//                dayData.put("taDurMoney",taDurMoney);
//                dayData.put("taOverMoney",taOverMoney);
//                dayData.put("taMissMoney",taMissMoney);
//                dayData.put("taPreMoney",taPreMoney);
//                dayData.put("taExtraMoney",taExtraMoney);
//                dayData.put("taLateMoney",taLateMoney);
//                dayData.put("theSameDay",obj.getJSONObject("data").getString("theSameDay"));
//                sumData.add(dayData);
                }
                data.put("taDurMoney",taDurMoney);
                data.put("taOverMoney",taOverMoney);
                data.put("taMissMoney",taMissMoney);
                data.put("taPreMoney",taPreMoney);
                data.put("taExtraMoney",taExtraMoney);
                data.put("taLateMoney",taLateMoney);
                double assembleMoney = (taDurMoney + taOverMoney + taExtraMoney);
                data.put("assembleMoney",assembleMoney);
                double assembleMinusMoney = (taMissMoney+taPreMoney+taLateMoney);
                double money = assembleMoney - assembleMinusMoney;
                data.put("money",money);
                sumData.add(data);
            }
            result.put(m+"",sumData);
        }
        result.put("subTypeStatus",subTypeStatus);
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    /**
     * 查询指定的es库的keyVal条件的所有内容，并且返回size条数
     * @param index     指定的es库
     * @param keyVal    查询条件
     * @param size  返回条数
     * @return  查询结果
     */
    @Override
    public ApiResponse getEsShow(String index,JSONObject keyVal,int size) {
        JSONArray array = new JSONArray();
        for (String key : keyVal.keySet()) {
            JSONObject json = new JSONObject();
            json.put("filtKey", key);
            json.put("method", "exact");
            json.put("filtVal", keyVal.getString(key));
            array.add(json);
        }
        JSONArray es = qt.getES(index, array);
        if (null == es || es.size() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LOG_FAILF.getCode(),"");
        }
        System.out.println("str------ es ------str");
        for (int i = 0; i < es.size(); i++) {
            JSONObject jsonObject = es.getJSONObject(i);
            System.out.println(JSON.toJSONString(jsonObject));
            if (i == size) {
                break;
            }
        }
        System.out.println("end------ es ------end");
        return retResult.ok(CodeEnum.OK.getCode(),es);
    }

    /**
     * 删除指定es库的id_ES的内容
     * @param index 指定的es库
     * @param id_ES es编号
     * @return  删除结果
     */
    @Override
    public ApiResponse delEs(String index, String id_ES) {
        qt.delES(index,id_ES);
        return retResult.ok(CodeEnum.OK.getCode(),"操作成功");
    }

    @Override
    public ApiResponse addOItemAllow(String id_O, String wrdN, String ref, double allow,double pr, double wn4pr) {
        Order order = qt.getMDContent(id_O, qt.strList("oItem", "work"), Order.class);
        if (null == order || null == order.getOItem()) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LOG_FAILF.getCode(),"");
        }
        JSONObject oItem = order.getOItem();
        JSONArray objAllow = oItem.getJSONArray("objAllow");
        if (null == objAllow) {
            objAllow = new JSONArray();
        }
        JSONObject allowSon = new JSONObject();
        // 名称
        allowSon.put("wrdN",wrdN);
        // 类型
        allowSon.put("ref",ref);
        // 次数
        allowSon.put("allow",allow);
        // 单次钱
        allowSon.put("pr",pr);
        // 合计
        allowSon.put("wn4pr",wn4pr);
        objAllow.add(allowSon);
        qt.setMDContent(id_O,qt.setJson("oItem.objAllow",objAllow), Order.class);
        return retResult.ok(CodeEnum.OK.getCode(),"操作成功");
    }

    @Override
    public ApiResponse sumOItemAllow(String id_O) {
        Order order = qt.getMDContent(id_O, "oItem", Order.class);
        if (null == order || null == order.getOItem()) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LOG_FAILF.getCode(),"");
        }
        JSONObject oItem = order.getOItem();
        JSONArray objAllow = oItem.getJSONArray("objAllow");
        double zon = 0;
        for (int i = 0; i < objAllow.size(); i++) {
            JSONObject obj = objAllow.getJSONObject(i);
            String ref = obj.getString("ref");
            Double wn4pr = obj.getDouble("wn4pr");
            if (ref.equals("k")) {
                zon-=wn4pr;
            } else {
                zon+=wn4pr;
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(),zon);
    }

    @Override
    public ApiResponse setOItem(String id_O, int index, JSONObject keyVal) {
        Order order = qt.getMDContent(id_O, "oItem", Order.class);
        if (null == order || null == order.getOItem()) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LOG_FAILF.getCode(),"");
        }
        JSONObject oItem = order.getOItem();
        JSONArray objItem = oItem.getJSONArray("objItem");
        JSONObject item = objItem.getJSONObject(index);
        for (String s : keyVal.keySet()) {
            JSONObject obj = keyVal.getJSONObject(s);
            String type = obj.getString("type");
            if ("int".equals(type)) {
                item.put(s,obj.getInteger("val"));
            } else if ("double".equals(type)) {
                item.put(s,obj.getDouble("val"));
            } else if ("long".equals(type)) {
                item.put(s,obj.getLong("val"));
            } else if ("boolean".equals(type)) {
                item.put(s,obj.getBoolean("val"));
            } else {
                item.put(s,obj.getString("val"));
            }
        }
        qt.setMDContent(id_O,qt.setJson("oItem.objItem."+index,item), Order.class);
        return retResult.ok(CodeEnum.OK.getCode(),"操作成功");
    }

    /**
     * 下线指定端
     * @param id_U  下线用户
     * @param client    下线端
     * @return  下线结果
     */
    @Override
    public ApiResponse activeOffline(String id_U,String client) {
        // 获取redis信息
        JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, id_U); // appID? /mqKey
        // 判断redis信息为空
        if (null == rdInfo) {
            rdInfo = new JSONObject();
        }
        // 获取当前端信息
        JSONObject cliInfo = rdInfo.getJSONObject(client);
        // 判断端信息为空
        if (null != cliInfo) {
            JSONObject wsData = cliInfo.getJSONObject("wsData");
            if (null != wsData) {
                // 获取mq编号
                String mqKeyOld = wsData.getString("mqKey");
                // 判断不为空
                if (null != mqKeyOld) {
                    // 创建日志
                    LogFlow logData = new LogFlow();
                    logData.setId_U(id_U);
                    logData.setId_Us(qt.setArray(id_U));
                    logData.setLogType("msg");
                    logData.setSubType("Offline");
                    JSONObject data = new JSONObject();
                    data.put("client",client);
                    logData.setData(data);
                    // 直接发送信息
                    ws.sendMQ(mqKeyOld,logData);
                    // 设置为不能登录
                    cliInfo.put("offlineType",1);
                    rdInfo.put(client,cliInfo);
                    // 保存到redis
                    qt.setRDSet(Ws.ws_mq_prefix,id_U,JSON.toJSONString(rdInfo),6000L);
                    return retResult.ok(CodeEnum.OK.getCode(),"操作成功");
                }
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(),"已经是离线");
    }

    /**
     * app端同意登录后，设置能登录接口
     * @param id_U  请求用户
     * @param client    需要登录端
     * @return  请求结果
     */
    @Override
    public ApiResponse allowLogin(String id_U, String client) {
        // 获取redis信息
        JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, id_U); // appID? /mqKey
        // 判断redis信息为空
        if (null == rdInfo) {
            rdInfo = new JSONObject();
        }
        // 获取当前端信息
        JSONObject cliInfo = rdInfo.getJSONObject(client);
        // 判断端信息不为空
        if (null != cliInfo) {
            // 设置为可以登录
            cliInfo.put("offlineType",0);
            rdInfo.put(client,cliInfo);
            // 保存到redis
            qt.setRDSet(Ws.ws_mq_prefix,id_U,JSON.toJSONString(rdInfo),6000L);
        }
        return retResult.ok(CodeEnum.OK.getCode(),"操作成功");
    }

    /**
     * 请求app端登录接口
     * @param id_U  请求用户
     * @param clientOld 请求的端
     * @return  请求结果
     */
    @Override
    public ApiResponse requestLogin(String id_U, String clientOld) {
        // 获取redis在线信息
        JSONObject rdInfo = qt.getRDSet(Ws.ws_mq_prefix, id_U); // appID? /mqKey
        // 判断redis信息为空
        if (null == rdInfo) {
            rdInfo = new JSONObject();
        }
        // 设置接收请求端为app端
        String client = "app";
        // 获取端信息
        JSONObject cliInfo = rdInfo.getJSONObject(client);
        // 判断端信息不为空
        if (null != cliInfo) {
            JSONObject wsData = cliInfo.getJSONObject("wsData");
            if (null != wsData) {
                // 获取mq编号
                String mqKeyOld = wsData.getString("mqKey");
                // 判断不为空
                if (null != mqKeyOld) {
                    // 创建日志
                    LogFlow logData = new LogFlow();
                    logData.setId_U(id_U);
                    logData.setId_Us(qt.setArray(id_U));
                    logData.setLogType("msg");
                    logData.setSubType("requestLogin");
                    JSONObject data = new JSONObject();
                    data.put("client",client);
                    data.put("requestClient",clientOld);
                    logData.setData(data);
                    // 直接发送信息
                    ws.sendMQ(mqKeyOld,logData);
                    return retResult.ok(CodeEnum.OK.getCode(),"操作成功");
                }
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(),"app已经离线");
    }

    /**
     * 修改指定产品的价格，单人单件用时，准备时间，并且修改所有用到的part
     * @param id_P  需要修改的产品
     * @param wn4pr 产品新的价格
     * @param teDur 产品单人单件用时
     * @param tePrep    准备时间
     * @return  处理结果
     */
    @Override
    public ApiResponse updatePartAll(String id_P,double wn4pr,long teDur,long tePrep) {
        // 查询es包含当前产品的所有产品
        JSONArray esP = qt.getES("lBProd", qt.setESFilt("arrP","contain", qt.setArray(id_P)));
        if (null == esP || esP.size() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    LB_PROD_NOT_FOUND.getCode(),"");
        }
        System.out.println("esP:");
        System.out.println(JSON.toJSONString(esP));
        // 遍历产品
        for (int i = 0; i < esP.size(); i++) {
            // 获取产品信息
            JSONObject obj = esP.getJSONObject(i);
            // 判断arrP不为空
            if (obj.containsKey("arrP")) {
                // 获取父产品id
                String id_PF = obj.getString("id_P");
                // 获取父产品的所有子产品id
                JSONArray arrP = obj.getJSONArray("arrP");
                // 获取当前产品在当前父产品的位置，默认不在
                int index = -1;
                // 遍历父产品的所有子产品id
                for (int j = 0; j < arrP.size(); j++) {
                    String p = arrP.getString(j);
                    // 判断等于，
                    if (p.equals(id_P)) {
                        // 获取位置
                        index = j;
                    }
                }
                // 判断有位置
                if (index != -1) {
                    // 获取父产品信息
                    Prod prod = qt.getMDContent(id_PF, "part", Prod.class);
                    if (null == prod || null == prod.getPart() || null == prod.getPart().getJSONArray("objItem")) {
                        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                                LOG_FAILF.getCode(),"");
                    }
                    JSONObject part = prod.getPart();
                    JSONArray objItem = part.getJSONArray("objItem");
                    // 更新父产品指定位置的当前产品信息
                    JSONObject itemIndex = objItem.getJSONObject(index);
                    itemIndex.put("wn4price",wn4pr);
                    itemIndex.put("teDur",teDur);
                    itemIndex.put("tePrep",tePrep);
                    // 更新mongodb
                    qt.setMDContent(id_PF,qt.setJson("part.objItem."+index,itemIndex), Prod.class);
                }
                System.out.println("lBProd:");
                System.out.println(JSON.toJSONString(obj));
            }
        }
//        System.out.println();
//        JSONArray esP = qt.getES("lBProd", qt.setESFilt("id_P","exact", id_P));
////        JSONArray esP = qt.getES("lBProd", qt.setESFilt("id_P", id_P));
//        if (null == esP || esP.size() == 0) {
//            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
//                    LB_PROD_NOT_FOUND.getCode(),"");
//        }
////        qt.delES("lBProd","LbSub4sBaLRqu6aFP3nE");
//        System.out.println("esP:");
//        System.out.println(JSON.toJSONString(esP));

//        JSONArray es = qt.getES("lSBProd", qt.setESFilt("id_C", id_C));
////        JSONArray es = qt.getES("lSBProd", qt.setESFilt("id_P", id_P));
//        if (null == es || es.size() == 0) {
//            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
//                    LB_PROD_NOT_FOUND.getCode(),"");
//        }
//        for (int i = 0; i < es.size(); i++) {
//            JSONObject obj = es.getJSONObject(i);
//            if (obj.containsKey("arrP")) {
//                System.out.println("lSBProd:");
//                System.out.println(JSON.toJSONString(obj));
//            }
//        }
//        JSONObject lSBProd = es.getJSONObject(0);
//        System.out.println("lSBProd:");
//        System.out.println(JSON.toJSONString(lSBProd));

        return retResult.ok(CodeEnum.OK.getCode(),"成功");
    }

    /**
     * 批量新增或修改mongodb的Prod内arrP，和es的lBProd的arrP字段
     * @return 处理结果
     */
    @Override
    public ApiResponse updateAllObjItemByArrP() {
        Criteria where = Criteria.where("part");
        //创建查询对象
        Query query = new Query();
        query.addCriteria(where.ne(null));
        query.fields().include("part");
        List<Prod> prodList = mongoTemplate.find(query, Prod.class);
        for (Prod prod : prodList) {
            if (null != prod && null != prod.getPart()) {
                JSONObject part = prod.getPart();
                JSONArray objItem = part.getJSONArray("objItem");
                JSONArray arrP = new JSONArray();
                for (int j = 0; j < objItem.size(); j++) {
                    JSONObject item = objItem.getJSONObject(j);
                    arrP.add(item.getString("id_P"));
                }
                qt.setMDContent(prod.getId(), qt.setJson("part.arrP", arrP), Prod.class);
                qt.setES("lBProd",qt.setESFilt("id_P", "exact",prod.getId()),qt.setJson("arrP",arrP));
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(),"操作成功");
    }

    /**
     * 批量新增或修改mongodb的Prod内part的objItem内的时间，准备时间，价格的默认值
     * @return  请求结果
     */
    @Override
    public ApiResponse updateAllObjItemByTime() {
        Criteria where = Criteria.where("part");
        //创建查询对象
        Query query = new Query();
        query.addCriteria(where.ne(null));
        query.fields().include("part");
        List<Prod> prodList = mongoTemplate.find(query, Prod.class);
        for (Prod prod : prodList) {
            if (null != prod && null != prod.getPart()) {
                JSONObject part = prod.getPart();
                JSONArray objItem = part.getJSONArray("objItem");
                for (int j = 0; j < objItem.size(); j++) {
                    JSONObject item = objItem.getJSONObject(j);
                    if (!item.containsKey("teDur")) {
                        item.put("teDur",0);
                    }
                    if (!item.containsKey("tePrep")) {
                        item.put("tePrep",0);
                    }
                    if (!item.containsKey("wn4price")) {
                        item.put("wn4price",0);
                    }
                    objItem.set(j,item);
                }
                qt.setMDContent(prod.getId(), qt.setJson("part.objItem", objItem), Prod.class);
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(),"操作成功");
    }
}
