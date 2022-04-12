package com.cresign.timer.service.fallback;

import com.cresign.timer.client.CompClient;
import com.cresign.tools.pojo.po.Comp;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ##ClassName: CompClientTimerFallbackFactory
 * ##description: 公司feign异常处理
 * ##Author: tang
 * ##Updated: 2020/8/3 11:05
 * ##version: 1.0
 */
@Service
public class CompClientTimerFallbackFactory implements FallbackFactory<CompClient> {

    /**
     * 异常处理方法
     * ##Params: cause	异常信息
     * ##return: com.cresign.chat.service.CompClient  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 9:28
     */
    @Override
    public CompClient create(Throwable cause) {
        return new CompClient() {

            /**
             * 查询Comp的Chkin字段不为空的Comp信息
             * 无参
             * ##return: java.util.List<com.cresign.details.pojo.po.Comp>  返回结果: 所有Chkin字段不为空的Comp信息
             * ##Author: tang
             * ##version: 1.0.0
             * ##Updated: 2020/8/6 14:49
             */
            @Override
            public List<Comp> getCompByChkInIsNull() {

                return null;
            }

            /**
             * 用来获取所有公司的id
             * 无参
             * ##return: java.util.List<com.cresign.details.pojo.po.Comp>  返回结果: 所有公司的id
             * ##Author: tang
             * ##version: 1.0.0
             * ##Updated: 2020/8/6 14:50
             */
            @Override
            public List<Comp> getCompAllId() {

                return null;
            }
        };
    }
}
