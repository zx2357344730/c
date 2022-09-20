package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.GoogleLoginService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.enumeration.manavalue.HeaderEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/9/21 16:36
 * @ver 1.0
 */
@RestController
@RequestMapping("google")
public class GoogleLoginController {

    @Autowired
    private GoogleLoginService googleLoginService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 谷歌登录
     * @author JackSon
     * @param reqJson      前端传入参数
     * @ver 1.0
     * @updated 2020/9/21 16:35
     * @return java.lang.String
     */
    @PostMapping("/v1/googleLogin")
    @SecurityParameter
    public String googleLogin(@RequestBody JSONObject reqJson) {

        return googleLoginService.googleLogin(
                reqJson.getString("id_Token"),
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName()));

    }

}