package com.cresign.purchase.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.ModuleService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;

@RequestMapping("/module")
@RestController
public class ModuleController {

    @Resource
    private ModuleService moduleService;

    @Autowired
    private GetUserIdByToken getTokenOfUserId;

    @Resource
    private HttpServletRequest request;

    @PostMapping("/v1/modifyLogAuthAll")
    @SecurityParameter
    public ApiResponse modifyLogAuthAll(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.modifyLogAuthAll(reqJson.getString("id_C"),reqJson.getString("grpU")
                ,reqJson.getString("listType"),reqJson.getString("grp"),reqJson.getInteger("auth"));
    }

    @PostMapping("/v1/modifyLogAuth")
    @SecurityParameter
    public ApiResponse modifyLogAuth(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.modifyLogAuth(reqJson.getString("id_C"),reqJson.getString("grpU")
                ,reqJson.getString("listType"),reqJson.getString("grp"),reqJson.getInteger("auth")
                ,reqJson.getString("modRef"));
    }

    @PostMapping("/v1/addOrUpdateInitMod")
    @SecurityParameter
    public ApiResponse addOrUpdateInitMod(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.addOrUpdateInitMod(reqJson.getJSONObject("objLogMod"));
    }

    @PostMapping("/v1/updateLogAuth")
    @SecurityParameter
    public ApiResponse updateLogAuth(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.updateLogAuth(reqJson.getString("id_C")
                ,reqJson.getString("grpU"),reqJson.getString("listType")
                ,reqJson.getString("grp"));
    }

    /**
     * 单翻译 - 只能翻译一个字段
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @PostMapping("/v1/singleTranslate")
    @SecurityParameter
    public ApiResponse singleTranslate(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.singleTranslate(reqJson.getJSONObject("data"));
    }

    /**
     * 多翻译 - 按照指定格式请求，可以翻译所有的字段
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @PostMapping("/v1/manyTranslate")
    @SecurityParameter
    public ApiResponse manyTranslate(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.manyTranslate(reqJson.getJSONObject("data"));
    }

    /**
     * es的lsprod转lbprod
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @PostMapping("/v1/lSProdTurnLBProd")
    @SecurityParameter
    public ApiResponse lSProdTurnLBProd(@RequestBody JSONObject reqJson){
//        JSONObject tokData = getTokenOfUserId.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"t",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.lSProdTurnLBProd(
                reqJson.getString("id_P")
                ,reqJson.getString("id_C")
                ,reqJson.getBoolean("isMove"));
    }

    /**
     * 新增或删除用户的模块使用权
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @PostMapping("/v1/modSetUser")
    @SecurityParameter
    public ApiResponse modSetUser(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.modSetUser(
                reqJson.getString("id_C")
                ,reqJson.getJSONObject("objUser"));
    }

    /**
     * 根据公司编号操作公司资产的模块信息
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @PostMapping("/v1/modSetControl")
    @SecurityParameter
    public ApiResponse modSetControl(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.modSetControl(
                reqJson.getString("id_C")
                ,reqJson.getJSONObject("objMod"));
    }

    /**
     * 根据id_C获取模块信息
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @PostMapping("/v1/modGetControl")
    @SecurityParameter
    public ApiResponse modGetControl(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.modGetControl(reqJson.getString("id_C"));
    }

    /**
     * 建立连接关系
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @PostMapping("/v1/modAddLSBComp")
    @SecurityParameter
    public ApiResponse modAddLSBComp(@RequestBody JSONObject can){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        can.put("id_U",tokData.getString("id_U"));
        return moduleService.modAddLSBComp(
                can.getString("id_C")
                , can.getString("id_CP")
                , can.getString("id_CB")
                , can.getString("id_CBP")
                , can.getJSONObject("wrdNC")
                , can.getJSONObject("wrddesc")
                , can.getJSONObject("wrdNCB")
                , can.getJSONObject("wrddescB")
                , can.getString("grp")
                , can.getString("grpB")
                , can.getString("refC")
                , can.getString("refCB")
                , can.getString("picC")
                , can.getString("picCB")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/addModule")
    public ApiResponse addModule(@RequestBody JSONObject reqJson) throws ParseException, IOException {

        return moduleService.addModule(
                //"5f28bf314f65cc7dc2e60346",
                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                reqJson.getString("oid"),
                reqJson.getString("id_C"),
                reqJson.getString("ref"),
                reqJson.getInteger("bcdLevel"));
    }

    @SecurityParameter
    @PostMapping("/v1/addBlankComp")
    public ApiResponse addBlankComp(@RequestBody JSONObject reqJson) throws IOException {

        return moduleService.addBlankComp(
                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                //"5f28bf314f65cc7dc2e60387",
                reqJson);
    }
}
