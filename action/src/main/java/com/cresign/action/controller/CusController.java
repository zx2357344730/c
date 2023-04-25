package com.cresign.action.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.CusService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName CusController
 * @Date 2023/4/24
 * @ver 1.0.0
 */
@RestController
@RequestMapping("cus")
public class CusController {

    @Autowired
    private CusService cusService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @SecurityParameter
    @PostMapping("/v1/sendUserCusCustomer")
    public ApiResponse sendUserCusCustomer(@RequestBody JSONObject reqJson){
        return cusService.sendUserCusCustomer(JSONObject.parseObject(JSON.toJSONString(reqJson), LogFlow.class));
    }

    @SecurityParameter
    @PostMapping("/v1/sendUserCusService")
    public ApiResponse sendUserCusService(@RequestBody JSONObject reqJson){
        return cusService.sendUserCusService(JSONObject.parseObject(JSON.toJSONString(reqJson), LogFlow.class));
    }

    @SecurityParameter
    @PostMapping("/v1/cusOperate")
    public ApiResponse cusOperate(@RequestBody JSONObject reqJson){
        return cusService.cusOperate(reqJson.getString("id_C")
                ,getUserToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"))
                ,reqJson.getString("id_O"),reqJson.getInteger("index")
                ,reqJson.getInteger("bcdStatus"));
    }

}
