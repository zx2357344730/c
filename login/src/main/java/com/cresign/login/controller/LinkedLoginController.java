package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.LinkedinLoginService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.manavalue.HeaderEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/8/1 10:18
 * ##version: 1.0
 */
@RestController
@RequestMapping("linked")
public class LinkedLoginController {

    /**
     * 前端传入的 Linked web端登录的code
     */
    private static final String CODE = "code";

    @Autowired
    private LinkedinLoginService linkedinLoginService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/v1/LinkedWebLogin")
    @SecurityParameter
    public ApiResponse LinkedWebLogin(@RequestBody JSONObject jsonObject) {

        return linkedinLoginService.linkedinWebLogin(
                jsonObject.getString(CODE),
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName())
                );

    }

    @PostMapping("/v1/linkedRegister")
    @SecurityParameter
    public ApiResponse linkedRegister(@RequestBody JSONObject jsonObject) {

        return linkedinLoginService.registerLinked(
                jsonObject.getString(CODE),
                jsonObject.getString("phone"),
                jsonObject.getString("phoneType"),
                jsonObject.getString("smsNum")
        );

    }



}