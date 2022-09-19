package com.cresign.chat.service.fallback;

import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.client.DetailsClient;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ClassName DetailsFallbackFactory
 * @Description 作者很懒什么也没写
 * @authortang
 * @Date 2022/7/18
 * @ver 1.0.0
 */
@Service
public class DetailsFallbackFactory implements FallbackFactory<DetailsClient> {

    @Override
    public DetailsClient create(Throwable cause)
    {
        return new DetailsClient()
        {
            @Override
            public Integer updateOStockPi(@RequestBody JSONObject reqJson) { return 400; }

        };
    }
}
