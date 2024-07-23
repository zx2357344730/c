package com.cresign.purchase.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.ZjTestService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import com.cresign.tools.pojo.po.LogFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.io.IOException;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName ZjTestController
 * @Date 2023/8/10
 * @ver 1.0.0
 */
@RestController
@RequestMapping("zj")
public class ZjTestController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private ZjTestService zjService;

    @SecurityParameter
    @PostMapping("/v1/getMdSetEs")
    public ApiResponse getMdSetEs(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.getMdSetEs(
                    reqJson.getString("key"),
                    reqJson.getString("esIndex"),
                    reqJson.getString("condition"),
                    reqJson.getString("val"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ZjTestController.getMdSetEs", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/sendLog")
    public ApiResponse getMdSetEs(@RequestBody LogFlow logData) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.sendLog(logData);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sendLog", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/sendLogSp")
    public ApiResponse sendLogSp(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.sendLogSp(tokData.getString("id_U"), tokData.getString("id_C"),reqJson.getString("id")
                    , reqJson.getString("logType"), reqJson.getString("subType")
                    , reqJson.getString("zcnDesc"), reqJson.getJSONObject("data"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sendLogSp", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/sendLogXj")
    public ApiResponse sendLogXj(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.sendLogXj(tokData.getString("id_U"), tokData.getString("id_C"),reqJson.getString("id")
                    , reqJson.getString("logType"), reqJson.getString("subType")
                    , reqJson.getString("zcnDesc"), reqJson.getJSONObject("data"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sendLogXj", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLog")
    public ApiResponse getLog(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.getLog(reqJson.getString("id").equals("")?null:reqJson.getString("id")
                    , reqJson.getString("logType").equals("")?null:reqJson.getString("logType")
                    , reqJson.getString("subType").equals("")?null:reqJson.getString("subType")
                    , reqJson.getString("id_SP").equals("")?null:reqJson.getString("id_SP"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sendLog", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/shareSave")
    public ApiResponse shareSave(@RequestBody JSONObject data) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.shareSave(data);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.shareSave", e);
        }
    }

//    @GetMapping("/v1/shareOpen")
    @SecurityParameter
    @PostMapping("/v1/shareOpen")
    public ApiResponse shareOpen(@RequestBody JSONObject data) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.shareOpen(data.getString("shareId"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.shareOpen", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/shareOpen/{shareId}")
    public ApiResponse shareOpenPath(@PathParam("shareId") String shareId) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.shareOpen(shareId);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.shareOpenPath", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/initFC")
    public ApiResponse initFC(@RequestBody JSONObject data) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.initFC(data.getString("id_C"),tokData.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.initFC", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getFCAuth")
    public ApiResponse getFCAuth(@RequestBody JSONObject data) {
        try {
            return zjService.getFCAuth(data.getString("id_C"),data.getString("id"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getFCAuth", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setFCAuth")
    public ApiResponse setFCAuth(@RequestBody JSONObject data) {
        try {
            return zjService.setFCAuth(data.getString("id_C"),data.getString("id"),data.getJSONObject("users"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.setFCAuth", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getFCAuthByUser")
    public ApiResponse getFCAuthByUser(@RequestBody JSONObject data) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.getFCAuthByUser(data.getString("id_C"),tokData.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getFCAuthByUser", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLSProdShareId")
    public ApiResponse getLSProdShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLSProdShareId(data.getString("id_P"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLSProdShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLSInfoShareId")
    public ApiResponse getLSInfoShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLSInfoShareId(data.getString("id_I"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLSInfoShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLNUserShareId")
    public ApiResponse getLNUserShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLNUserShareId(data.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLNUserShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLNCompShareId")
    public ApiResponse getLNCompShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLNCompShareId(data.getString("id_C"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLNCompShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLBProdShareId")
    public ApiResponse getLBProdShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLBProdShareId(data.getString("id_P"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLBProdShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLBInfoShareId")
    public ApiResponse getLBInfoShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLBInfoShareId(data.getString("id_I"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLBInfoShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/saveProdEncryption")
    public ApiResponse saveProdEncryption(@RequestBody JSONObject data) {
        try {
            return zjService.saveProdEncryption(data.getJSONObject("en"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.saveProdEncryption", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getProdEncryption")
    public ApiResponse getProdEncryption(@RequestBody JSONObject data) {
        try {
            return zjService.getProdEncryption(data.getString("id_P"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getProdEncryption", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getShareId")
    public ApiResponse getShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getShareId(data.getString("shareId"), data.getString("type"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/applyForView")
    public ApiResponse applyForView(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.applyForView(tokData.getString("id_U"), tokData.getString("id_C"),reqJson.getString("id")
                    , reqJson.getString("logType"), reqJson.getString("subType")
                    , reqJson.getString("zcnDesc"), reqJson.getJSONObject("data"), reqJson.getInteger("imp"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.applyForView", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/applyForAgreeWith")
    public ApiResponse applyForAgreeWith(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.applyForAgreeWith(tokData.getString("id_U"), tokData.getString("id_C"),reqJson.getString("id")
                    , reqJson.getString("logType"), reqJson.getString("subType")
                    , reqJson.getString("zcnDesc"), reqJson.getJSONObject("data"), reqJson.getInteger("imp"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.applyForAgreeWith", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/addBlankComp")
    public ApiResponse addBlankComp(@RequestBody JSONObject reqJson) throws IOException {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

            return zjService.addBlankCompNew(
                    tokData,
                    reqJson.getJSONObject("wrdN"),
                    reqJson.getJSONObject("wrddesc"),
                    reqJson.getString("pic"),
                    reqJson.getString("ref")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ModuleController.addBlankComp", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/genChkinCode")
    public ApiResponse genChkinCode() {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.genChkinCode( tokData.getString("id_C"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.genChkinCode", e);
        }
    }
    @SecurityParameter
    @PostMapping("/v1/scanChkinCode")
    public ApiResponse scanChkinCode(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.scanChkinCode(tokData.getString("id_U"), reqJson.getString("token"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.scanChkinCode", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/removeUser")
    public ApiResponse removeUser(@RequestBody JSONObject resJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.removeUser(resJson.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.removeUser", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getOnLine")
    public ApiResponse getOnLine() {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.getOnLine(tokData.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getOnLine", e);
        }
    }



    @SecurityParameter
    @PostMapping("/v1/testEx")
    public ApiResponse testEx(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.testEx(tokData.getString("id_C"), resJson.getString("fileName")
                    , tokData.getString("id_U"), resJson.getInteger("subTypeStatus"), resJson.getString("year")
                    , resJson.getString("month"),resJson.getJSONArray("arrField") );
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.testEx", e);
        }
    }

    /**
     * 添加用户个人空间
     * @param resJson 请求数据体
     * @return  添加结果
     */
    @SecurityParameter
    @PostMapping("/v1/addCompSpace")
    public ApiResponse addCompSpace(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.addCompSpace(resJson.getString("id_U"),resJson.getString("id_C")
                    ,resJson.getJSONObject("wrdN"), resJson.getJSONObject("wrddesc")
                    , resJson.getString("pic"), resJson.getString("ref") );
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.addCompSpace", e);
        }
    }

    /**
     * 添加劳动合同api
     * @param resJson 请求参数
     * @return  添加结果
     */
    @SecurityParameter
    @PostMapping("/v1/addWorkContract")
    public ApiResponse addWorkContract(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.addWorkContract(resJson.getString("id_U"), resJson.getString("id_CB")
                    , resJson.getInteger("money"), resJson.getInteger("year")
                    , resJson.getJSONObject("contJ"),resJson.getJSONObject("contY")
                    ,resJson.getString("grpB"),resJson.getString("dep"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.addWorkContract", e);
        }
    }

    /**
     * 根据 subTypeStatus 统计打卡数据
     * @param resJson 请求参数
     * @return  统计结果
     */
    @SecurityParameter
    @PostMapping("/v1/sumTimeChkIn")
    public ApiResponse sumTimeChkIn(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.sumTimeChkIn(resJson.getString("id_C"), resJson.getString("id_U")
                    , resJson.getInteger("subTypeStatus"), resJson.getInteger("year")
                    , resJson.getJSONArray("monthArr"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sumTimeChkIn", e);
        }
    }

    /**
     * 查询指定的es库的keyVal条件的所有内容，并且返回size条数
     * @param resJson 请求参数
     * @return  查询结果
     */
    @SecurityParameter
    @PostMapping("/v1/getEsShow")
    public ApiResponse getEsShow(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
//        try {
            return zjService.getEsShow(resJson.getString("index"), resJson.getJSONObject("keyVal"),resJson.getInteger("size"));
//        }
//        catch (Exception e) {
//            return getUserToken.err(new JSONObject(), "ZjTestController.getEsShow", e);
//        }
    }

    /**
     * 删除指定es库的id_ES的内容
     * @param resJson 请求参数
     * @return  删除结果
     */
    @SecurityParameter
    @PostMapping("/v1/delEs")
    public ApiResponse delEs(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.delEs(resJson.getString("index"),resJson.getString("id_ES"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.delEs", e);
        }
    }

    /**
     * 添加订单记录信息api
     * @param resJson 请求参数
     * @return  添加结果
     */
    @SecurityParameter
    @PostMapping("/v1/addOItemAllow")
    public ApiResponse addOItemAllow(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.addOItemAllow(resJson.getString("id_O"), resJson.getString("wrdN")
                    ,resJson.getString("ref"),resJson.getDouble("allow"),resJson.getDouble("pr")
                    ,resJson.getDouble("wn4pr"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.addOItemAllow", e);
        }
    }

    /**
     * 统计订单记录信息
     * @param resJson 请求参数
     * @return  统计结果
     */
    @SecurityParameter
    @PostMapping("/v1/sumOItemAllow")
    public ApiResponse sumOItemAllow(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.sumOItemAllow(resJson.getString("id_O"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sumOItemAllow", e);
        }
    }

    /**
     * 根据参数indexArr下标集合修改订单的oItem为参数keyVal对应信息
     * @param resJson 请求参数
     * @return  修改结果
     */
    @SecurityParameter
    @PostMapping("/v1/setOItemExtraKey")
    public ApiResponse setOItemExtraKey(@RequestBody JSONObject resJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.setOItemExtraKey(resJson.getString("id_O"),resJson.getBoolean("isCover"),resJson.getJSONArray("indexArr")
                    ,resJson.getJSONObject("keyVal"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.setOItemExtraKey", e);
        }
    }

    /**
     * 下线指定端
     * @param resJson 请求参数
     * @return  下线结果
     */
    @SecurityParameter
    @PostMapping("/v1/activeOffline")
    public ApiResponse activeOffline(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.activeOffline(tokData.getString("id_U"),resJson.getString("client"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.activeOffline", e);
        }
    }

    /**
     * app端同意登录后，设置能登录接口
     * @param resJson 请求参数
     * @return  请求结果
     */
    @SecurityParameter
    @PostMapping("/v1/allowLogin")
    public ApiResponse allowLogin(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.allowLogin(tokData.getString("id_U"),resJson.getString("client"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.allowLogin", e);
        }
    }

    /**
     * 请求app端登录接口
     * @param resJson 请求参数
     * @return  请求结果
     */
    @SecurityParameter
    @PostMapping("/v1/requestLogin")
    public ApiResponse requestLogin(@RequestBody JSONObject resJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.requestLogin(resJson.getString("id_U"),resJson.getString("client"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.requestLogin", e);
        }
    }

    /**
     * 修改指定产品的价格，单人单件用时，准备时间，并且修改所有用到的part
     * @param resJson 请求参数
     * @return  处理结果
     */
    @SecurityParameter
    @PostMapping("/v1/updatePartAll")
    public ApiResponse updatePartAll(@RequestBody JSONObject resJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.updatePartAll(resJson.getString("id_P"),resJson.getDouble("wn4pr")
                    ,resJson.getLong("wntDur"), resJson.getLong("wntPrep"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.updatePartAll", e);
        }
    }

    /**
     * 批量新增或修改mongodb的Prod内arrP，和es的lBProd的arrP字段
     * @return 处理结果
     */
    @SecurityParameter
    @PostMapping("/v1/updateAllObjItemByArrP")
    public ApiResponse updateAllObjItemByArrP() {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.updateAllObjItemByArrP();
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.updateAllObjItemByArrP", e);
        }
    }

    /**
     * 批量新增或修改mongodb的Prod内part的objItem内的时间，准备时间，价格的默认值
     * @return  请求结果
     */
    @SecurityParameter
    @PostMapping("/v1/updateAllObjItemByTime")
    public ApiResponse updateAllObjItemByTime() {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.updateAllObjItemByTime();
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.updateAllObjItemByTime", e);
        }
    }

    /**
     * 根据参数indexArr下标集合修改产品的part为参数keyVal对应信息
     * @param resJson 请求参数
     * @return  修改结果
     */
    @SecurityParameter
    @PostMapping("/v1/setPartExtraKey")
    public ApiResponse setPartExtraKey(@RequestBody JSONObject resJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.setPartExtraKey(resJson.getString("id_P"),resJson.getBoolean("isCover"),resJson.getJSONArray("indexArr")
                    ,resJson.getJSONObject("keyVal"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.setPartExtraKey", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/updatePartTime")
    public ApiResponse updatePartTime(@RequestBody JSONObject resJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.updatePartTime(resJson.getString("id_P"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.updatePartTime", e);
        }
    }

    /**
     * 添加测试Asset的lSAsset信息
     * @return  添加结果
     */
    @SecurityParameter
    @PostMapping("/v1/addAsset")
    public ApiResponse addAsset() {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.addAsset();
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.addAsset", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/aiQuesting")
    public ApiResponse aiQuesting(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.aiQuesting(tokData, resJson.getString("user"),resJson.getString("desc"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.aiQuesting", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/aiQuestingDeepSeek")
    public ApiResponse aiQuestingDeepSeek(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.aiQuestingDeepSeek(tokData,resJson.getString("desc"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.aiQuestingDeepSeek", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/aiQuestingDeepSeekByObj")
    public ApiResponse aiQuestingDeepSeekByObj(@RequestBody JSONObject resJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.aiQuestingDeepSeekByObj(tokData,resJson.getJSONObject("descObj"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.aiQuestingDeepSeekByObj", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setCosFileByAt")
    public ApiResponse setCosFileByAt(@RequestBody JSONObject resJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.setCosFileByAt(resJson.getString("id_A"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.setCosFileByAt", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setStFt")
    public ApiResponse setStFt(@RequestBody JSONObject resJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.setStFt(resJson.getString("id_O"),resJson.getJSONObject("setObj"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.setStFt", e);
        }
    }
}
