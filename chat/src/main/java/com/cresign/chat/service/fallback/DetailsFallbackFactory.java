package com.cresign.chat.service.fallback;

import com.cresign.chat.client.DetailsClient;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Service;

/**
 * @ClassName DetailsFallbackFactory
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/7/18
 * @Version 1.0.0
 */
@Service
public class DetailsFallbackFactory implements FallbackFactory<DetailsClient> {


    @Override
    public DetailsClient create(Throwable cause) {
        return reqJson -> null;
    }
}
