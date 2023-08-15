package com.cresign.login.controller;


import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.InitService;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * @author JackSon
 * @ver 1.0
 * ##description: Init 信息
 * @updated 2020-03-23 11:43
 */
@RestController
@RequestMapping("init")
public class InitController {

    @Autowired
    private InitService initService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    /**
     * 根据id(语言)获取init的数据
     * @author JackSon
     * @param lang      语言
     * @param ver           版本号
     * @ver 1.0
     * @updated 2020/8/8 11:07
     * @return java.lang.String
     */
    @GetMapping("/v1/getInit")
    public ApiResponse getInit(
            @RequestParam("lang") String lang,
            @RequestParam(value = "ver", required = false) Integer ver
            ,@RequestParam("qdKey") String qdKey){

        try {
// 字符串转换
            String s = qdKey.replaceAll(",", "/");
            s = s.replaceAll("%0A","\n");
            s = s.replaceAll("%2C","/");
            s = s.replaceAll("%2B","+");
            s = s.replaceAll("%3D","=");
//        System.out.println("进入这里:"+lang+" - "+ver);
//        System.out.println(s);
//        System.out.println(uuId);
//        System.out.println(request.getHeader("uuId"));
//        RetResult.setClient_Public_Key(s);
            return initService.getInitById(lang, ver,s,request.getHeader("uuId"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "InitController.getInit", e);
        }
    }

    @GetMapping("/v1/getPhoneType")
    public ApiResponse getPhoneType(@RequestParam("lang") String lang){
        try {
            return initService.getPhoneType(lang);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "InitController.getPhoneType", e);
        }
    }


//    @GetMapping("/v2/getInitInclude")
//    public ApiResponse getInitInclude(
//            @RequestParam("lang") String lang,
//            @RequestParam(value = "ver", required = false) Integer ver,
//            @RequestParam("include") String include){
//        return initService.getInitInclude(lang, ver,include);
//    }

}