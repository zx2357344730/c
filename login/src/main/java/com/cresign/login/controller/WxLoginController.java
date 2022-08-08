package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.WxLoginService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.manavalue.HeaderEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/7/29 9:26
 * ##version: 1.0
 */
@RestController
@RequestMapping("wx")
public class WxLoginController {

    /**
     * app端的手机安装应用的id，用于push
     */
    public static final String CLIENT_ID = "clientID";

    /**
     * 用户头像图片路径
     */
    public static final String USER_PIC = "pic";

//    /**
//     * 前端传入的微信web端登录的code
//     */
//    private static final String CODE = "code";

    @Autowired
    private WxLoginService wxLoginService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 微信登录接口 (web端)
     * ##Params: reqJson 前端传入参数
     * ##author: JackSon
     * ##updated: 2020/7/29 9:26
     * ##Return: java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/wxWebLogin")
    public ApiResponse wxWebLogin(@RequestBody JSONObject reqJson) throws IOException {

        return wxLoginService.wxWebLogin(reqJson.getString("code"));

    }

    /**
     * 微信小程序解密用户数据接口
     * ##author: JackSon
     * ##Params: reqJson 前端传入参数
     * ##version: 1.0
     * ##updated: 2020/8/8 10:38
     * ##Return: java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/decodeUserInfo")
    public ApiResponse decodeUserInfo(@RequestBody JSONObject reqJson) {
        return wxLoginService.decodeUserInfo(reqJson);
    }


    /**
     * 微信小程序登录接口 (微信小程序)
     * ##Params: reqJson 前端传入参数
     * ##author: JackSon
     * ##updated: 2020/7/29 9:26
     * ##Return: java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/wxAsLogin")
    public ApiResponse wXLoginByIdWx(@RequestBody JSONObject reqJson) {
        String uuId = request.getHeader("uuId");
        System.out.println("uuId:");
        System.out.println(uuId);
        return wxLoginService.wXLoginByIdWx(
                reqJson.getString("id_WX"),
                request.getHeader("clientType"));

    }

    /**
     * 微信小程序登录接口 (微信小程序)
     * ##Params: reqJson 前端传入参数
     * ##author: JackSon
     * ##updated: 2020/7/29 9:26
     * ##Return: java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/wxAppLogin")
    public ApiResponse appLoginByIdWx(@RequestBody JSONObject reqJson) {

        return wxLoginService.appLoginByIdWx(
                reqJson.getString("id_AUN"),
                request.getHeader("clientType"));

    }

    @SecurityParameter
    @PostMapping("/v1/wxRegisterUser")
    public ApiResponse wxRegisterUser(@RequestBody JSONObject reqJson) {

        return wxLoginService.wxRegisterUser(reqJson);

    }


    @SecurityParameter
    @PostMapping("/v1/wechatRegister")
    public ApiResponse wechatRegister(@RequestBody JSONObject reqJson) {
        String clientID = "";
        if (reqJson.containsKey(CLIENT_ID)) {
            clientID = reqJson.getString(CLIENT_ID);
        }

        String pic = "https://cresign-1253919880.cos.ap-guangzhou.myqcloud.com/pic_small/userRegister.jpg";
        if (reqJson.containsKey(USER_PIC)) {
            pic = reqJson.getString(USER_PIC);
        }

        return wxLoginService.wechatRegister(
                reqJson.getString("phone"),
                reqJson.getInteger("phoneType"),
                reqJson.getString("smsNum"),
                reqJson.getString("wcnN"),
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName()),
                clientID,
                pic,
                reqJson.getString("id_WX")
        );
    }


    @SecurityParameter
    @PostMapping("/v1/wxmp_register")
    public ApiResponse wxmpRegister(@RequestBody JSONObject reqJson) {

        return wxLoginService.wxmpRegister(
                reqJson.getString("nickName"),
                reqJson.getString("avatarUrl"),
                reqJson.getString("unionId"),
                reqJson.getInteger("countryCode"),
                reqJson.getString("phoneNumber")
        );

    }

}