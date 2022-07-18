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
