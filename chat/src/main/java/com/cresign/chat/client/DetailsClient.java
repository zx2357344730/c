package com.cresign.chat.client;

import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.service.fallback.DetailsFallbackFactory;
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
        value = "cresign-details",
        fallbackFactory = DetailsFallbackFactory.class,
        contextId = "DetailsClient"
)
public interface DetailsClient {

    @PostMapping("/storage/v1/updateOStockPi")
    Integer updateOStockPi(JSONObject reqJson);

}
