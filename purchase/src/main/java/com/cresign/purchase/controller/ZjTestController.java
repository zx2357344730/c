package com.cresign.purchase.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.ZjTestService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import com.cresign.tools.pojo.po.LogFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName ZjTestController
 * @Date 2023/8/10
 * @ver 1.0.0
 */
@RestController
@RequestMapping("zj")
public class ZjTestController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private ZjTestService zjService;

    @SecurityParameter
    @PostMapping("/v1/getMdSetEs")
    public ApiResponse getMdSetEs(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.getMdSetEs(
                    reqJson.getString("key"),
                    reqJson.getString("esIndex"),
                    reqJson.getString("condition"),
                    reqJson.getString("val"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ZjTestController.getMdSetEs", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/sendLog")
    public ApiResponse sendLog(@RequestBody LogFlow logData) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.sendLog(logData);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sendLog", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/shareSave")
    public ApiResponse shareSave(@RequestBody JSONObject data) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.shareSave(data);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.shareSave", e);
        }
    }

//    @GetMapping("/v1/shareOpen")
    @SecurityParameter
    @PostMapping("/v1/shareOpen")
    public ApiResponse shareOpen(@RequestParam("shareId") String shareId) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.shareOpen(shareId);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.shareOpen", e);
        }
    }
}
