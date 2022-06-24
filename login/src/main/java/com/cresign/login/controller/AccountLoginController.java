package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.AccountLoginService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/7/25 11:37
 * ##version: 1.0
 */
@RestController
@RequestMapping("/account")
public class AccountLoginController {

    @Autowired
    private GetUserIdByToken getUserIdByToken;

    @Autowired
    private AccountLoginService accountLoginService;

    @Autowired
    private HttpServletRequest request;

    @SecurityParameter
    @PostMapping("/v1/login")
    public ApiResponse doNumberLogin(@RequestBody JSONObject reqJson) {

        return accountLoginService.doNumberLogin(
//                reqJson.getString("usn"),
//                reqJson.getString("pwd"),
                request.getHeader("clientType"));

    }

    @SecurityParameter
    @PostMapping("/v1/generateLoginCode")
    public ApiResponse generateLoginCode(@RequestBody JSONObject reqJson) {

        return accountLoginService.generateLoginCode(
                reqJson.getString("id")
               );
    }

    @SecurityParameter
    @PostMapping("/v1/scanLoginCode")
    public ApiResponse scanLoginCode(@RequestBody JSONObject reqJson) {

        return accountLoginService.scanLoginCode(
                reqJson.getString("token"),
                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"))
//                reqJson.getString("clientType")
        );
    }


}