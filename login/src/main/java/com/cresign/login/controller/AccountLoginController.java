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
 * @author JackSon
 * @updated 2020/7/25 11:37
 * @ver 1.0
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

//    /**
//     * 根据id_U修改appId
//     * @param reqJson 请求参数
//     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/9/19
//     */
//    @SecurityParameter
//    @PostMapping("/v1/setAppId")
//    public ApiResponse setAppId(@RequestBody JSONObject reqJson) {
////        System.out.println("进入接口:");
//        String id_U = reqJson.getString("id_U");
//        if (null == id_U) {
//            id_U = getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"));
//        }
//        return accountLoginService.setAppId(reqJson.getString("appId")
//                ,id_U);
//    }

//    @SecurityParameter
//    @PostMapping("/v1/key")
//    public ApiResponse getKey(@RequestBody JSONObject reqJson) {
//        return accountLoginService.getKey(reqJson.getString("qdKey"));
//    }
//
//    @PostMapping("/v1/getHdKey")
//    public String getHdKey(@RequestBody JSONObject reqJson) {
//        return accountLoginService.getHdAndQdKey(reqJson.getString("qdKey"));
//    }
//
//    @PostMapping("/v1/setQdKey")
//    public ApiResponse setQdKey(@RequestBody JSONObject reqJson) {
//        return accountLoginService.getKey(reqJson.getString("qdKey"));
//    }

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