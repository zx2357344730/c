package com.cresign.timer.service.fallback;

import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.client.SearchClient;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Service;

@Service
public class SearchFallbackFactory implements FallbackFactory<SearchClient> {

    @Override
    public SearchClient create(Throwable throwable) {
        return new SearchClient() {
            @Override
            public Object getStatValueByEsSE(JSONObject json) {
                System.out.println("getStatValueByEsSE-fallback");
                return null;
            }
        };
    }
}
