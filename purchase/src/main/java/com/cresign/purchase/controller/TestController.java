package com.cresign.purchase.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.TestService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName TestController
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/8/17
 * @Version 1.0.0
 */
@RequestMapping("pTest")
@RestController
public class TestController {

    @Resource
    private TestService testService;

    /**
     * 验证appId，并且返回AUN—ID
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @SecurityParameter
    @PostMapping("/v1/verificationAUN")
    public ApiResponse verificationAUN(@RequestBody JSONObject reqJson) {

        return testService.verificationAUN(reqJson.getString("id_APP"));

    }

}
