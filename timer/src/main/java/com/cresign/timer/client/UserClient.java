package com.cresign.timer.client;

import com.cresign.timer.service.fallback.UserClientTimerFallbackFactory;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.pojo.po.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * ##author: tangzejin
 * ##updated: 2019/10/23
 * ##version: 1.0.0
 * ##description: 用户Service类
 */
@FeignClient(
        value = "cresign-details",
        fallbackFactory = UserClientTimerFallbackFactory.class,
        contextId = "UserClient"
)
public interface UserClient {

    /**
     * 根据uId获取listKey需要的信息
     * ##Params: reqMap	请求参数
     * ##return: com.cresign.details.pojo.po.User  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:11
     */
    @SecurityParameter
    @PostMapping("/user/v1/getUserByListKey")
    User getUserByListKey(@RequestBody Map<String, Object> reqMap);

}
