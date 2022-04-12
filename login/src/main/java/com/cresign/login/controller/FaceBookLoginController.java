package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.FaceBookLoginService;
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
 * ##updated: 2020/9/22 14:17
 * ##version: 1.0
 */
@RestController
@RequestMapping("facebook")
public class FaceBookLoginController {

    /**
     * app端的手机安装应用的id，用于push
     */
    public static final String CLIENT_ID = "clientID";

    /**
     * 用户头像图片路径
     */
    public static final String USER_PIC = "pic";

    @Autowired
    private FaceBookLoginService faceBookLoginService;

    @Autowired
    private HttpServletRequest request;

    @SecurityParameter
    @PostMapping("/v1/faceBookLogin")
    public ApiResponse faceBookLogin(@RequestBody JSONObject reqJson) {

        return faceBookLoginService.faceBookLogin(
                reqJson.getString("id_fb"),
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName())
        );

    }


    @SecurityParameter
    @PostMapping("/v1/faceBookRegister")
    public ApiResponse faceBookRegister(@RequestBody JSONObject reqJson) {
        String clientID = "";
        if (reqJson.containsKey(CLIENT_ID)) {
            clientID = reqJson.getString(CLIENT_ID);
        }

        String pic = "https://cresign-1253919880.cos.ap-guangzhou.myqcloud.com/pic_small/userRegister.jpg";
        if (reqJson.containsKey(USER_PIC)) {
            pic = reqJson.getString(USER_PIC);
        }

        return faceBookLoginService.faceBookRegister(
                reqJson.getString("id_fb"),
                reqJson.getString("wcnN"),
                reqJson.getString("email"),
                pic,
                reqJson.getString("phone"),
                reqJson.getString("phoneType"),
                reqJson.getString("smsNum"),
                clientID,
                request.getHeader(HeaderEnum.CLIENTTYPE.getHeaderName())
                );

    }

}