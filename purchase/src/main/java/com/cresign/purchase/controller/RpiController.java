package com.cresign.purchase.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.RpiService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tang
 * @ClassName RpiController
 * @Description 作者很懒什么也没写
 * @Date 2022/8/18
 * @ver 1.0.0
 */
@RestController
@RequestMapping("rpi")
public class RpiController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private RpiService rpiService;

    @PostMapping("/v1/delPi")
    @SecurityParameter
    public ApiResponse delPi(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            reqJson.put("id_U", tokData.getString("id_U"));
            return rpiService.delPi(
                    reqJson.getString("rname")
                    , reqJson.getString("id_C"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "RpiController.delPi", e);
        }
    }

    @PostMapping("/v1/rpiCode")
    @SecurityParameter
    public ApiResponse rpiCode(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            reqJson.put("id_U", tokData.getString("id_U"));
            return rpiService.rpiCode(
                    reqJson.getString("rname"),
                    reqJson.getString("id_C"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "RpiController.rpiCode", e);
        }
    }

    @PostMapping("/v1/requestRpiStatus")
    @SecurityParameter
    public ApiResponse requestRpiStatus(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            reqJson.put("id_U", tokData.getString("id_U"));
            return rpiService.requestRpiStatus(
                    reqJson.getString("token"),
                    reqJson.getString("id_C"),
                    reqJson.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "RpiController.requestRpiStatus", e);
        }
    }

    @PostMapping("/v1/bindingRpi")
    @SecurityParameter
    public ApiResponse bindingRpi(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            reqJson.put("id_U", tokData.getString("id_U"));
            return rpiService.bindingRpi(
                    reqJson.getString("token"),
                    reqJson.getString("id_C"),
                    reqJson.getString("id_U"),
                    reqJson.getString("grpU"),
                    reqJson.getInteger("oIndex"),
                    reqJson.getJSONObject("wrdNU"),
                    reqJson.getInteger("imp"),
                    reqJson.getString("id_O"),
                    reqJson.getInteger("tzone"),
                    reqJson.getString("lang"),
                    reqJson.getString("id_P"),
                    reqJson.getString("pic"),
                    reqJson.getInteger("wn2qtynow"),
                    reqJson.getString("grpB"),
                    reqJson.getJSONObject("fields"),
                    reqJson.getJSONObject("wrdNP"),
                    reqJson.getJSONObject("wrdN"),
                    reqJson.getString("dep"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "RpiController.bindingRpi", e);
        }
    }

    @PostMapping("/v1/relieveRpi")
    @SecurityParameter
    public ApiResponse relieveRpi(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            reqJson.put("id_U", tokData.getString("id_U"));
            return rpiService.relieveRpi(
                    reqJson.getString("token"),
                    reqJson.getString("id_C"),
                    reqJson.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "RpiController.relieveRpi", e);
        }
    }

}
