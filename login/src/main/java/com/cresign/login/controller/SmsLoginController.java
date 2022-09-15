package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.SmsLoginService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.manavalue.HeaderEnum;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/8/4 10:23
 * ##version: 1.0
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

    @PostMapping("/v1/getSmsLoginNum")
    @SecurityParameter
    public ApiResponse getSmsLoginNum(@RequestBody JSONObject reqJson) throws TencentCloudSDKException {
        return smsLoginService.getSmsLoginNum(
                reqJson.getString("phone")
        );
    }

    @PostMapping("/v1/smsLogin")
    @SecurityParameter
    public ApiResponse smsLogin(@RequestBody JSONObject reqJson) {

        return smsLoginService.smsLogin(
                reqJson.getString("phone"),
                reqJson.getString("smsCode"),
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName())
        );
    }

    @SecurityParameter
    @PostMapping("/v1/getSmsRegisterNum")
    public ApiResponse getSmsRegisterNum(@RequestBody JSONObject reqJson)  {

        return smsLoginService.getSmsRegisterNum(
                reqJson.getString("phone")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/smsRegister")
    public ApiResponse smsRegister(@RequestBody JSONObject reqJson) {
        System.out.println("进入注册接口:");

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
    }





}