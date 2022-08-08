package com.cresign.details.controller;


import com.alibaba.fastjson.JSONObject;
import com.cresign.details.service.LinkService;
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

@RequestMapping("/link")
@RestController
public class LinkController {

    @Autowired
    private LinkService linkService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getTokenOfUserId;


    @SecurityParameter
    @PostMapping("/v1/setCompLink")
    public ApiResponse setCompLink(@RequestBody JSONObject reqMap) throws IOException {

        return linkService.setCompLink(
                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                //"5f28bf314f65cc7dc2e60386",
                reqMap.getString("id_C"),
                reqMap.getString("id_CB"),
                reqMap.getString("grp"),
                reqMap.getString("grpB"),
                reqMap.getString("listType"));
    }

    @SecurityParameter
    @PostMapping("/v1/setProdLink")
    public ApiResponse setProdLink(@RequestBody JSONObject reqMap) throws IOException {

        return linkService.setProdLink(
                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                //"5f28bf314f65cc7dc2e60386",
                reqMap.getString("id_C"),
                reqMap.getString("id_P"),
                reqMap.getString("grp"));
    }

    @SecurityParameter
    @PostMapping("/v1/updateProdListType")
    public ApiResponse updateProdListType(@RequestBody JSONObject reqJson) throws IOException {

        return linkService.updateProdListType(
                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                //"5f28bf314f65cc7dc2e60386",
                reqJson.getString("id_C"),
                reqJson.getString("grp"),
                reqJson.getString("listType"),
                reqJson.getString("id_P"));
    }


}
