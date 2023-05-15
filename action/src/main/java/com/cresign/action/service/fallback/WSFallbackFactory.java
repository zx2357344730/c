package com.cresign.action.service.fallback;

import com.alibaba.fastjson.JSONObject;
import com.cresign.action.client.WSClient;
import com.cresign.tools.pojo.po.LogFlow;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ##ClassName: AuthFilterClientFileFallbackFactory
 * ##description: 权限feign异常处理
 * @author tang
 * ##Updated: 2020/8/7 11:42
 * @ver 1.0.0
 */
@Service
public class WSFallbackFactory implements FallbackFactory<WSClient> {

    /**
     * 异常处理方法
     * @param cause	异常信息
     * @return com.cresign.chat.service.AuthFilterClient  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 9:28
     *

     */
    @Override
    public WSClient create(Throwable cause)
    {
        return new WSClient()
        {
            @Override
            public void sendWS(@RequestBody LogFlow logData){ }

            @Override
            public JSONObject testFill(JSONObject object) {
                System.out.println("testFill:出现异常");
                return null;
            }

        };
    }
}
