package com.cresign.timer.service.fallback;

import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.client.DetailsClient;
import com.cresign.tools.apires.ApiResponse;
import feign.hystrix.FallbackFactory;

public class DetailsClientTimerFallbackFactory implements FallbackFactory<DetailsClient> {

    /**
     * 异常处理方法
     * @Author Rachel
     * @Date 2021/12/09
     * ##param throwable 异常信息
     * @Return com.cresign.timer.client.DetailsClient
     **/
    @Override
    public DetailsClient create(Throwable throwable) {
        return new DetailsClient() {

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
            @Override
            public ApiResponse setOItemRelated(JSONObject json) {
                return null;
            }
        };
    }
}
