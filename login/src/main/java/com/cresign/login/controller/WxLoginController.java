package com.cresign.login.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.WxLoginService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.manavalue.HeaderEnum;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/7/29 9:26
 * @ver 1.0
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

    @Autowired
    private GetUserIdByToken getUserToken;

    /**
     * 微信登录接口 (web端)
     * @param reqJson 前端传入参数
     * @author JackSon
     * @updated 2020/7/29 9:26
     * @return java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/wxWebLogin")
    public ApiResponse wxWebLogin(@RequestBody JSONObject reqJson) throws IOException {
        try {
            return wxLoginService.wxWebLogin(reqJson.getString("code"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "WxLoginController.wxWebLogin", e);
        }
    }

    /**
     * 微信小程序解密用户数据接口
     * @author JackSon
     * @param reqJson 前端传入参数
     * @ver 1.0
     * @updated 2020/8/8 10:38
     * @return java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/decodeUserInfo")
    public ApiResponse decodeUserInfo(@RequestBody JSONObject reqJson) {
        System.out.println("进入微信小程序解密用户数据接口：");
        System.out.println(JSON.toJSONString(reqJson));
        try {
            return wxLoginService.decodeUserInfo(reqJson);
        } catch (Exception e) {
            return getUserToken.err(reqJson, "WxLoginController.decodeUserInfo", e);
        }
    }


    /**
     * 微信小程序登录接口 (微信小程序)
     * @param reqJson 前端传入参数
     * @author JackSon
     * @updated 2020/7/29 9:26
     * @return java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/wxAsLogin")
    public ApiResponse wXLoginByIdWx(@RequestBody JSONObject reqJson) {
        try {
            System.out.println("进入wxAsLogin：");
            System.out.println(JSON.toJSONString(reqJson));
            String uuId = request.getHeader("uuId");
            System.out.println("uuId:");
            System.out.println(uuId);
            return wxLoginService.wXLoginByIdWx(
                    reqJson.getString("id_WX"),
                    request.getHeader("clientType"));
        } catch (Exception e){
            System.out.println("出现异常:");
//            e.printStackTrace();
            return getUserToken.err(reqJson, "WxLoginController.wXLoginByIdWx", e);
        }
//        try {
//
//        } catch (Exception e) {
//            return getUserToken.err(reqJson, "WxLoginController.wXLoginByIdWx", e);
//        }
    }

    /**
     * 微信小程序登录接口 (微信小程序)
     * @param reqJson 前端传入参数
     * @author JackSon
     * @updated 2020/7/29 9:26
     * @return java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/wxAppLogin")
    public ApiResponse appLoginByIdWx(@RequestBody JSONObject reqJson) {
        try {
            return wxLoginService.appLoginByIdWx(
                    reqJson.getString("id_AUN"),
                    request.getHeader("clientType"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "WxLoginController.appLoginByIdWx", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/wxRegisterUser")
    public ApiResponse wxRegisterUser(@RequestBody JSONObject reqJson) throws IOException {
        try {
            return wxLoginService.wxRegisterUser(reqJson);
        } catch (Exception e) {
            return getUserToken.err(reqJson, "WxLoginController.wxRegisterUser", e);
        }
    }

    /**
     * 验证appId，并且返回AUN—ID
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
//    @SecurityParameter
//    @PostMapping("/v1/verificationAUN")
//    public ApiResponse verificationAUN(@RequestBody JSONObject reqJson) {
//
//        return wxLoginService.verificationAUN(reqJson.getString("id_APP"));
//
//    }
//

    @SecurityParameter
    @PostMapping("/v1/wechatRegister")
    public ApiResponse wechatRegister(@RequestBody JSONObject reqJson) throws IOException {
        try {
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
        } catch (Exception e) {
            return getUserToken.err(reqJson, "WxLoginController.wechatRegister", e);
        }
    }


    @SecurityParameter
    @PostMapping("/v1/wxmp_register")
    public ApiResponse wxmpRegister(@RequestBody JSONObject reqJson) {
        try {
            System.out.println("wxmpRegister:");
            System.out.println(JSON.toJSONString(reqJson));
            return wxLoginService.wxmpRegister(
                    reqJson.getString("nickName"),
                    reqJson.getString("avatarUrl"),
                    reqJson.getString("unionId"),
                    reqJson.getInteger("countryCode"),
                    reqJson.getString("phoneNumber"),
                    reqJson.getString("realName")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "WxLoginController.wxmpRegister", e);
        }
    }

//    @SecurityParameter
//    @PostMapping("/v1/uploadWXHeadSculpture")
//    public ApiResponse uploadWXHeadSculpture(@RequestBody JSONObject reqJson) {
//        return wxLoginService.uploadWXHeadSculpture(reqJson.getString("path"));
//    }

    @SecurityParameter
    @PostMapping("/v1/getAUN")
    public ApiResponse getAUN(@RequestBody JSONObject reqJson) {
        try {
            return wxLoginService.getAUN(reqJson.getString("id_AUN"),reqJson.getString("id_C"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "WxLoginController.getAUN", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getPhone")
    public ApiResponse getPhone(@RequestBody JSONObject reqJson) {
        try {
            return wxLoginService.getPhone(reqJson.getString("phone")
                    ,reqJson.getString("id_WX"),reqJson.getString("countryCode"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "WxLoginController.getPhone", e);
        }
    }
}