package com.cresign.timer.client;

import com.cresign.timer.service.fallback.CompClientTimerFallbackFactory;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.pojo.po.Comp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/23
 * ##version: 1.0.0
 * ##description: 公司Service类
 */
@FeignClient(
        value = "cresign-details",
        fallbackFactory = CompClientTimerFallbackFactory.class,
        contextId = "CompClient"
)
public interface CompClient {

    /**
     * 查询Comp的Chkin字段不为空的Comp信息
     * 无参
     * ##return: java.util.List<com.cresign.details.pojo.po.Comp>  返回结果: 所有Chkin字段不为空的Comp信息
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 14:49
     */
    @SecurityParameter
    @PostMapping("/comp/v1/getCompByChkInIsNull")
    List<Comp> getCompByChkInIsNull();

    /**
     * 用来获取所有公司的id
     * 无参
     * ##return: java.util.List<com.cresign.details.pojo.po.Comp>  返回结果: 所有公司的id
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 14:50
     */
    @SecurityParameter
    @PostMapping("/comp/v1/getCompAllId")
    List<Comp> getCompAllId();

}
