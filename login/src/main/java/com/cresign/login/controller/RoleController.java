package com.cresign.login.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.RoleService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ##description:
 * @author JackSon
 * @updated 2020-12-26 11:28
 * @ver 1.0
 */
@RequestMapping("role")
@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private AuthCheck authCheck;

    @Autowired
    private GetUserIdByToken getTokenOfUserId;

    @SecurityParameter
    @PostMapping("/v1/get_role")
    public ApiResponse getRole(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        return roleService.getRole(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("grpU")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/getRoleData")
    public ApiResponse getRoleData(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);


        return roleService.getRoleData(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("grp"),
                reqJson.getString("grpU"));
    }

    @SecurityParameter
    @PostMapping("/v1/updateRole")
    public ApiResponse updateRole(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        return roleService.updateRole(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("grp"),
                reqJson.getString("dataName"),
                reqJson.getString("authType"),
                reqJson.getInteger("auth"),
                reqJson.getString("grpU"));
    }

//    @SecurityParameter
//    @PostMapping("/v1/get-type-grp")
//    public ApiResponse getListTypeGrp(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
//
//        return roleService.getListTypeGrp(
//                tokData.getString("id_U"),
//                tokData.getString("id_C"));
//    }

    @SecurityParameter
    @PostMapping("/v1/getRoleDataByGrp")
    public ApiResponse getRoleDataByGrpUAndGrp(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        return roleService.getRoleDataByGrpUAndGrp(
                tokData.getString("id_U"),
//                tokData.getString("id_C"),
                null==reqJson.getString("id_C")?tokData.getString("id_C"):reqJson.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("grp"),
                reqJson.getString("grpU"));
    }

    @SecurityParameter
    @PostMapping("/v1/updateNewestRole")
    public ApiResponse updateNewestRole(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        return roleService.updateNewestRole(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("grp"),
                reqJson.getString("grpU"));
    }

    @SecurityParameter
    @PostMapping("/v1/up_grp_all_auth")
    public ApiResponse upRoleOfAuth(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

        return roleService.upRoleOfAuth(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("grp"),
                reqJson.getInteger("upAuth"),
                reqJson.getString("upType"),
                reqJson.getString("grpU"));
    }

    @SecurityParameter
    @PostMapping("/v1/copy_grp_to_othergrp")
    public ApiResponse copyGrpRoleToOtherGrp(@RequestBody JSONObject reqJson) {

        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        authCheck.getUserUpdateAuth(tokData.getString("id_U"),tokData.getString("id_C"),"lSAsset","1003","card",new JSONArray().fluentAdd("role"));

        return roleService.copyGrpRoleToOtherGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("copy_grp"),
                reqJson.getJSONArray("to_grp"),
                reqJson.getString("grpU"));
    }

    @SecurityParameter
    @PostMapping("/v1/copyGrpU")
    public ApiResponse copyGrpU(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        authCheck.getUserUpdateAuth(tokData.getString("id_U"),tokData.getString("id_C"),"lSAsset","1003","card",new JSONArray().fluentAdd("role"));

        return roleService.copyGrpU(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("to_grpU"),
                reqJson.getString("grpU"));

    }

    }