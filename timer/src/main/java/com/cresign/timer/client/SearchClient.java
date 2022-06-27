package com.cresign.timer.client;


import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        value = "cresign-search"
)
public interface SearchClient {

    @PostMapping("stat/v1/getStatValueByEsSE")
    Object getStatValueByEsSE(JSONObject json);

}
