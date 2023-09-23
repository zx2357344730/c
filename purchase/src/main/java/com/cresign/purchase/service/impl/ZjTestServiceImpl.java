package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.enumeration.PurchaseEnum;
import com.cresign.purchase.service.ZjTestService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.es.lNUser;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
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
        ws.sendWS(logFlow);
        return retResult.ok(CodeEnum.OK.getCode(), "发送成功");
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
}
