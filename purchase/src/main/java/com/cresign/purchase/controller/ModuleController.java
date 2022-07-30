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

    @PostMapping("/v1/testFy")
    @SecurityParameter
    public ApiResponse testFy(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.testFy(reqJson.getJSONObject("data"));
    }

    @PostMapping("/v1/testFy2")
    @SecurityParameter
    public ApiResponse testFy2(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.testFy2(reqJson.getJSONObject("data"));
    }

    @PostMapping("/v1/lSprod2lBprod")
    @SecurityParameter
    public ApiResponse lSprod2lBprod(@RequestBody JSONObject reqJson){
//        JSONObject tokData = getTokenOfUserId.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"t",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.lSprod2lBprod(
                reqJson.getString("id_P")
                ,reqJson.getString("id_C")
                ,reqJson.getBoolean("isMove"));
    }

    @PostMapping("/v1/modSetUser")
    @SecurityParameter
    public ApiResponse modSetUser(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.modSetUser(
                reqJson.getString("id_C")
                ,reqJson.getJSONObject("objUser"));
    }

    @PostMapping("/v1/modSetControl")
    @SecurityParameter
    public ApiResponse modSetControl(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.modSetControl(
                reqJson.getString("id_C")
                ,reqJson.getJSONObject("objMod"));
    }

    @PostMapping("/v1/modGetControl")
    @SecurityParameter
    public ApiResponse modGetControl(@RequestBody JSONObject reqJson){
        JSONObject tokData = getTokenOfUserId.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        reqJson.put("id_U",tokData.getString("id_U"));
        return moduleService.modGetControl(reqJson.getString("id_C"));
    }

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
