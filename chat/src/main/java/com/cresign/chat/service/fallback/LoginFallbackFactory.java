package com.cresign.chat.service.fallback;

import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.client.DetailsClient;
import com.cresign.chat.client.LoginClient;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ClassName DetailsFallbackFactory
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/7/18
 * @Version 1.0.0
 */
@Service
public class LoginFallbackFactory implements FallbackFactory<LoginClient> {

    @Override
    public LoginClient create(Throwable cause)
    {
        return new LoginClient()
        {
            @Override
            public String getHdKey(@RequestBody JSONObject reqJson) { return ""; }

        };
    }
}
