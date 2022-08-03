package com.cresign.chat.client;

import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.service.fallback.DetailsFallbackFactory;
import com.cresign.chat.service.fallback.LoginFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @ClassName DetailsC
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/7/18
 * @Version 1.0.0
 */
@FeignClient(
        value = "cresign-login",
        fallbackFactory = LoginFallbackFactory.class,
        contextId = "LoginClient"
)
public interface LoginClient {

    @PostMapping("/account/v1/getHdKey")
    String getHdKey(JSONObject reqJson);

}
