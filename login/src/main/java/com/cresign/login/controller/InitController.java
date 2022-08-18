package com.cresign.login.controller;


import com.cresign.login.service.InitService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * ##Author: JackSon
 * ##version: 1.0
 * ##description: Init 信息
 * ##updated: 2020-03-23 11:43
 */
@RestController
@RequestMapping("init")
public class InitController {

    @Autowired
    private InitService initService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 根据id(语言)获取init的数据
     * ##author: JackSon
     * ##Params: language      语言
     * ##Params: ver           版本号
     * ##version: 1.0
     * ##updated: 2020/8/8 11:07
     * ##Return: java.lang.String
     */
    @GetMapping("/v1/getInit")
    public ApiResponse getInit(
            @RequestParam("lang") String lang,
            @RequestParam(value = "ver", required = false) Integer ver
            ,@RequestParam("qdKey") String qdKey
            ,@RequestParam("uuId") String uuId){
        // 字符串转换
        String s = qdKey.replaceAll(",", "/");
        s = s.replaceAll("%0A","\n");
        s = s.replaceAll("%2C","/");
        s = s.replaceAll("%2B","+");
        s = s.replaceAll("%3D","=");
        System.out.println("进入这里:"+lang+" - "+ver);
        System.out.println(s);
        System.out.println(uuId);
        System.out.println(request.getHeader("uuId"));
//        RetResult.setClient_Public_Key(s);
        return initService.getInitById(lang, ver,s,request.getHeader("uuId"),request.getHeader("isDecrypt"));
    }

    @GetMapping("/v1/getPhoneType")
    public ApiResponse getPhoneType(@RequestParam("lang") String lang){
        return initService.getPhoneType(lang);
    }


    @GetMapping("/v2/getInitInclude")
    public ApiResponse getInitInclude(
            @RequestParam("lang") String lang,
            @RequestParam(value = "ver", required = false) Integer ver,
            @RequestParam("include") String include){
        return initService.getInitInclude(lang, ver,include);
    }

}