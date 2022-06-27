package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.RefreshTokenService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.manavalue.HeaderEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/9/5 14:37
 * ##version: 1.0
 */
@RestController
@RequestMapping("refreshToken")
public class RefreshTokenController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RefreshTokenService refreshTokenService;


    @SecurityParameter
    @PostMapping("/v1/login")
    public ApiResponse refreshTokenLogin(@RequestBody JSONObject reqJson) {

        return refreshTokenService.refreshTokenLogin(
                reqJson.getString("refreshToken"),
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName()));
    }


    @SecurityParameter
    @PostMapping("/v1/logout")
    public ApiResponse loginOut(@RequestBody JSONObject reqJson) {
        return refreshTokenService.loginOut(
                reqJson.getString("refreshToken"),
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName()));
    }

    @GetMapping("/v1/refreshToken")
    public ApiResponse refreshToken(@RequestParam("id_U") String id_U, @RequestParam("id_C") String id_C) {



        return refreshTokenService.refreshToken(
                request.getHeader("refreshToken"),
                id_C,
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName()),
                id_U);

    }

}