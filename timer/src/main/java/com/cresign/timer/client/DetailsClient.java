package com.cresign.timer.client;

import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.service.fallback.DetailsClientTimerFallbackFactory;
import com.cresign.timer.service.fallback.UserClientTimerFallbackFactory;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @Author Rachel
 * @Date 2021/12/09
 **/
@FeignClient(
        value = "cresign-details",
        fallbackFactory = DetailsClientTimerFallbackFactory.class,
        contextId = "DetailsClient"
)
public interface DetailsClient {

    /**
     * 新增/修改指定的卡片数组对象
     * @Author Rachel
     * @Data 2021/09/02
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param id_O 订单id
     * ##param card 新增/修改哪张卡片
     * ##param objName 卡片下的数组
     * ##param index 数组下标
     * ##param content 新增/修改的内容
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    @SecurityParameter
    @PostMapping("/single/v1/setOItemRelated")
    ApiResponse setOItemRelated(@RequestBody JSONObject json);
}
