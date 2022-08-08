package com.cresign.details.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.details.service.GroupService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequestMapping("/group")
@RestController
public class GroupController {


    @Autowired
    private GroupService groupService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @SecurityParameter
    @PostMapping("/v1/addFC")
    public ApiResponse addFC(@RequestBody JSONObject requestJson) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.addFC(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                requestJson.getJSONObject("requestJson"));
    }

    @SecurityParameter
    @PostMapping("/v1/deleteGroup")
    public ApiResponse deleteGroup(@RequestBody JSONObject requestJson) {

        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.deleteGroup(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                requestJson.getString("id"));
    }

    @SecurityParameter
    @PostMapping("/v1/updateGroup")
    public ApiResponse updateGroup(@RequestBody JSONObject requestJson) throws IOException {

        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.updateGroup(
                requestJson.getString("id_A"),
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                requestJson.getJSONObject("requestJson"));
    }

    @SecurityParameter
    @PostMapping("/v1/orderRelease")
    public ApiResponse orderRelease(@RequestBody JSONObject requestJson) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        return groupService.orderRelease(
                requestJson.getString("id_O"),
                tokData);
    }

    @SecurityParameter
    @PostMapping("/v1/getFlowControl")
    public ApiResponse getFlowControl(@RequestBody Map<String, Object> reqMap) throws IOException {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.getFlowControl(
                tokData.getString("id_C"),
                tokData.getString("id_U"),
                (List<String>) reqMap.get("type"));
    }

    @SecurityParameter
    @PostMapping("/v1/changeUserGrp")
    public ApiResponse changeUserGrp(@RequestBody JSONObject reqJson) throws IOException {

        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.changeUserGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("grpUTarget"),
                reqJson.getString("uid"));
    }


    @SecurityParameter
    @PostMapping("/v1/getMyFavComp")
    public ApiResponse getMyFavComp() {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.getMyFavComp(

                tokData.getString("id_U"));
//                tokData.getString("id_C"),

    }

    @SecurityParameter
    @PostMapping("/v1/addFav")
    public ApiResponse addFav(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.addFav(
                tokData.getString("id_U"),
                tokData.getString("id_C")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/delFav")
    public ApiResponse delFav(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.delFav(
                tokData.getString("id_U"),
                tokData.getString("id_C")
        );
    }

    @PostMapping("/v1/changeAssetGrp")
    @SecurityParameter
    public ApiResponse changeAssetGrp(@RequestBody JSONObject reqJson) throws Exception {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.changeAssetGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("grp"),
                reqJson.getString("listType"),
                reqJson.getString("id")); }

    @PostMapping("/v1/changeProdGrp")
    @SecurityParameter
    public ApiResponse changeProdGrp(@RequestBody JSONObject reqJson) throws Exception {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.changeProdGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("grp"),
                reqJson.getString("listType"),
                reqJson.getString("id"));
    }

    @PostMapping("/v1/changeOrderGrp")
    @SecurityParameter
    public ApiResponse changeOrderGrp(@RequestBody JSONObject reqJson) throws Exception {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.changeOrderGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("grp"),
                reqJson.getString("listType"),
                reqJson.getString("id"));
    }

    @PostMapping("/v1/changeCompGrp")
    @SecurityParameter
    public ApiResponse changeCompGrp(@RequestBody JSONObject reqJson) throws Exception {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.changeCompGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("grp"),
                reqJson.getString("listType"),
                reqJson.getString("id")); }

//    @PostMapping("/v1/restoreUser")
//    @SecurityParameter
//    public ApiResponse restoreUser(@RequestBody JSONObject reqJson) throws Exception {
//        return groupService.restoreUser(
//                //"5f28bf314f65cc7dc2e60346",
//                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("uid"));
//    }

    @PostMapping("/v1/restoreAssetGrp")
    @SecurityParameter
    public ApiResponse restoreAssetGrp(@RequestBody JSONObject reqJson) throws Exception {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.restoreAssetGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("id"));
    }

    @PostMapping("/v1/restoreProdGrp")
    @SecurityParameter
    public ApiResponse restoreProdGrp(@RequestBody JSONObject reqJson) throws Exception {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.restoreProdGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("id"));
    }


    @PostMapping("/v1/restoreOrderGrp")
    @SecurityParameter
    public ApiResponse restoreOrderGrp(@RequestBody JSONObject reqJson) throws Exception {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.restoreOrderGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("id"));
    }

    @PostMapping("/v1/restoreCompGrp")
    @SecurityParameter
    public ApiResponse restoreCompGrp(@RequestBody JSONObject reqJson) throws Exception {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.restoreCompGrp(
                tokData.getString("id_U"),
                tokData.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("id"));
    }

    @PostMapping("/v1/updateCascadeInfo")
    @SecurityParameter
    public ApiResponse updateCascadeInfo(@RequestBody JSONObject reqJson) throws Exception {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        return groupService.updateCascadeInfo(
                reqJson.getString("id_O"),
                reqJson.getJSONObject("changes"));
    }



}
