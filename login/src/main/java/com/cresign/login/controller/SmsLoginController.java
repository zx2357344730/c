package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.SmsLoginService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import com.cresign.tools.enumeration.manavalue.HeaderEnum;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
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
 * @updated 2020/8/4 10:23
 * @ver 1.0
 */
@RestController
@RequestMapping("sms")
public class SmsLoginController {



    /**
     * 用户头像图片路径
     */

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SmsLoginService smsLoginService;

    @Autowired
    private GetUserIdByToken getUserToken;

    @PostMapping("/v1/getSmsLoginNum")
    @SecurityParameter
    public ApiResponse getSmsLoginNum(@RequestBody JSONObject reqJson) throws TencentCloudSDKException {
        try {
            return smsLoginService.getSmsLoginNum(
                    reqJson.getString("phone")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "SmsLoginController.getSmsLoginNum", e);
        }
    }

    @PostMapping("/v1/smsLogin")
    @SecurityParameter
    public ApiResponse smsLogin(@RequestBody JSONObject reqJson) {
        try {
            return smsLoginService.smsLogin(
                    reqJson.getString("phone"),
                    reqJson.getString("smsCode"),
                    request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName())
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "SmsLoginController.smsLogin", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getSmsRegisterNum")
    public ApiResponse getSmsRegisterNum(@RequestBody JSONObject reqJson)  {
        try {
            return smsLoginService.getSmsRegisterNum(
                    reqJson.getString("phone")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "SmsLoginController.getSmsRegisterNum", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/smsRegister")
    public ApiResponse smsRegister(@RequestBody JSONObject reqJson) throws IOException {
        try {
            String id_APP = "";
            if (reqJson.containsKey("id_APP")) {
                id_APP = reqJson.getString("id_APP");
            }

            String pic = "https://cresign-1253919880.cos.ap-guangzhou.myqcloud.com/pic_small/userRegister.jpg";
            if (reqJson.containsKey("pic")) {
                pic = reqJson.getString("pic");
            }

//        JSONObject wrdN = new JSONObject();
//        wrdN.put("cn","新用户");
//        if (reqJson.containsKey("wrdN")){
//            wrdN = reqJson.getJSONObject("wrdN");
//        }
            return smsLoginService.smsRegister(
                    reqJson.getString("phone"),
                    reqJson.getInteger("phoneType"),
                    reqJson.getString("smsNum"),
                    request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName()),
                    id_APP,
                    pic,
                    new JSONObject().fluentPut("cn","新用户")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "SmsLoginController.smsRegister", e);
        }
    }

//    @SecurityParameter
//    @PostMapping("/v1/regOffUser")
//    public ApiResponse regOffUser()  {
//        try {
//            return smsLoginService.regOffUser(
//                    getUserToken.getTokenOfUserId(request.getHeader("authorization")
//                            , request.getHeader("clientType"))
//            );
//        } catch (Exception e) {
//            return getUserToken.err(new JSONObject(), "SmsLoginController.regOffUser", e);
//        }
//    }

//    @SecurityParameter
//    @PostMapping("/v1/regBackUser")
//    public ApiResponse regBackUser(@RequestBody JSONObject reqJson)  {
//        try {
//            return smsLoginService.regBackUser(reqJson.getString("mbn"));
//        } catch (Exception e) {
//            return getUserToken.err(new JSONObject(), "SmsLoginController.regBackUser", e);
//        }
//    }

    @SecurityParameter
    @PostMapping("/v1/setTestUser")
    public ApiResponse setTestUser(@RequestBody JSONObject reqJson)  {
        try {
            return smsLoginService.setTestUser(reqJson.getString("name"),reqJson.getString("type"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "SmsLoginController.setTestUser", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/delTestUser")
    public ApiResponse delTestUser(@RequestBody JSONObject reqJson)  {
        try {
            return smsLoginService.delTestUser(reqJson.getString("name"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "SmsLoginController.delTestUser", e);
        }
    }

}