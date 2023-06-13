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
 * @author JackSon
 * @updated 2020/9/5 14:37
 * @ver 1.0
 */
@RestController
@RequestMapping("refreshToken")
public class RefreshTokenController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RefreshTokenService refreshTokenService;


    //real login
    @SecurityParameter
    @PostMapping("/v1/login")
    public ApiResponse refreshTokenLogin(@RequestBody JSONObject reqJson) {

        return refreshTokenService.refreshTokenLogin(
                reqJson.getString("refreshToken"),
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName()));
    }


    @SecurityParameter
    @PostMapping("/v1/logout")
    public ApiResponse logout(@RequestBody JSONObject reqJson) {
        return refreshTokenService.logout(
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

    @GetMapping("/v1/refreshToken2")
    public String refreshToken2(@RequestParam("id_U") String id_U, @RequestParam("id_C") String id_C
            ,@RequestParam("token") String token,@RequestParam("web") String web) {
        System.out.println("进入-refreshToken2-方法:");
        return refreshTokenService.refreshToken2(token, id_C, web,id_U);

    }

}