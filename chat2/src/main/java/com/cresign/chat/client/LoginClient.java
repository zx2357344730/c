package com.cresign.chat.client;

import com.cresign.chat.service.fallback.LoginFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName LoginClient
 * @Date 2023/5/17
 * @ver 1.0.0
 */
@FeignClient(
        value = "cresign-login",
        fallbackFactory = LoginFallbackFactory.class,
        contextId = "LoginClient"
)
public interface LoginClient {

    @GetMapping("/refreshToken/v1/refreshToken2")
    String refreshToken2(@RequestParam("id_U") String id_U, @RequestParam("id_C") String id_C
            ,@RequestParam("ton") String ton,@RequestParam("web") String web);

}
