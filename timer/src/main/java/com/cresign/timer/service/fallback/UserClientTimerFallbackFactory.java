package com.cresign.timer.service.fallback;

import com.cresign.timer.client.UserClient;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Service;

/**
 * ##ClassName: UserClientTimerFallbackFactory
 * ##description: 用户feign异常处理
 * ##Author: tang
 * ##Updated: 2020/8/3 11:01
 * ##version: 1.0
 */
@Service
public class UserClientTimerFallbackFactory implements FallbackFactory<UserClient> {

    /**
     * 异常处理方法
     * ##Params: cause	异常信息
     * ##return: com.cresign.chat.service.UserClient  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 9:28
     */
    @Override
    public UserClient create(Throwable cause) {
        return reqMap -> {

            return null;
        };
    }
}
