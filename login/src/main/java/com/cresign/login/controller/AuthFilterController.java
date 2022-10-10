//package com.cresign.login.controller;
//
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.login.service.AuthFilterService;
//import com.cresign.tools.apires.ApiResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * ##description:
// * @author JackSon
// * @updated 2020/8/6 10:05
// * @ver 1.0
// */
//@RestController
//@RequestMapping("/auth")
//public class AuthFilterController {
//
//    @Autowired
//    private AuthFilterService authFilterService;
//
////    @PostMapping("/v1/getUserSelectAuth")
////    public ApiResponse getUserSelectAuth(@RequestBody JSONObject reqJson) {
////        return authFilterService.getUserSelectAuth(
////                reqJson.getString("id_U"),
////                reqJson.getString("id_C"),
////                reqJson.getString("listType"),
////                reqJson.getString("grp"),
////                reqJson.getString("authType")
////        );
////
////    }
////
////    @PostMapping("/v1/getUserUpdateAuth")
////    public ApiResponse getUserUpdateAuth(@RequestBody JSONObject reqJson) {
////
////
////        return authFilterService.getUserUpdateAuth(
////                reqJson.getString("id_U"),
////                reqJson.getString("id_C"),
////                reqJson.getString("listType"),
////                reqJson.getString("grp"),
////                reqJson.getString("authType"),
////                reqJson.getJSONArray("params")
////        );
////
////    }
//
////    @PostMapping("/v1/getTouristAuth")
////    public ApiResponse getTouristAuth(@RequestBody JSONObject reqJson) {
////        return authFilterService.getTouristAuth(
////                reqJson.getString("id_C"),
////                reqJson.getString("listType"),
////                reqJson.getString("grp"),
////                reqJson.getString("authType")
////        );
////
////    }
//
//
//
//
//
//}