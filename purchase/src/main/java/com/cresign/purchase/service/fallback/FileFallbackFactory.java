package com.cresign.purchase.service.fallback;

import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.client.FileClient;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ##ClassName: AuthFilterClientFileFallbackFactory
 * ##description: 权限feign异常处理
 * ##Author: tang
 * ##Updated: 2020/8/7 11:42
 * ##version: 1.0.0
 */
@Service
public class FileFallbackFactory implements FallbackFactory<FileClient> {

    /**
     * 异常处理方法
     * ##Params: cause	异常信息
     * ##return: com.cresign.chat.service.AuthFilterClient  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 9:28
     *

     */
    @Override
    public FileClient create(Throwable cause)
    {
        return new FileClient()
        {
            @Override
            public long delCOSFile(@RequestBody JSONObject reqJson){ return 0;}

//            @Override
//            public Object cs(JSONObject json) {
//                System.out.println("fallback");
//                return null;
//            }
        };
    }
}
