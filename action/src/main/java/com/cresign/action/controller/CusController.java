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

    /**
     * 顾客发送日志api
     * @param reqJson	请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/4/24
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/sendUserCusCustomer")
    public ApiResponse sendUserCusCustomer(@RequestBody JSONObject reqJson){
        return cusService.sendUserCusCustomer(JSONObject.parseObject(JSON.toJSONString(reqJson), LogFlow.class));
    }

    /**
     * 客服发送日志api
     * @param reqJson	请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/4/24
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/sendUserCusService")
    public ApiResponse sendUserCusService(@RequestBody JSONObject reqJson){
        return cusService.sendUserCusService(JSONObject.parseObject(JSON.toJSONString(reqJson), LogFlow.class));
    }

    /**
     * 客服操作api
     * @param reqJson 请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/4/24
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/cusOperate")
    public ApiResponse cusOperate(@RequestBody JSONObject reqJson){
        return cusService.cusOperate(reqJson.getString("id_C")
                ,getUserToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"))
                ,reqJson.getString("id_O"),reqJson.getInteger("index")
                ,reqJson.getInteger("bcdStatus"));
    }

    /**
     * 根据id_C获取公司的聊天群信息
     * @param reqJson	请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/5/29
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/getLogList")
    public ApiResponse getLogList(@RequestBody JSONObject reqJson){
        return cusService.getLogList(reqJson.getString("id_C"));
    }

    /**
     * 更新修改群关联信息
     * @param reqJson 请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/5/29
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/updateLogListGl")
    public ApiResponse updateLogListGl(@RequestBody JSONObject reqJson){
        System.out.println("进入方法:");
        System.out.println(JSON.toJSONString(reqJson));
        return cusService.updateLogListGl(reqJson.getString("id_C")
                , reqJson.getString("logId"), reqJson.getJSONArray("glId") );
    }

    /**
     * 获取公司的日志权限信息
     * @param reqJson 请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/5/29
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/getLogAuth")
    public ApiResponse getLogAuth(@RequestBody JSONObject reqJson){
        System.out.println("进入方法:");
        System.out.println(JSON.toJSONString(reqJson));
        return cusService.getLogAuth(reqJson.getString("id_C")
                , reqJson.getString("grpUW"), reqJson.getString("grpUN"),reqJson.getString("type"));
    }
}
