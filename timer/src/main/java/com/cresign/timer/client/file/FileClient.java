package com.cresign.timer.client.file;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        value = "cresign-file"
)
public interface FileClient {


    @PostMapping("encrypt/v1/delCOSFile")
    long delCOSFile(Map<String, Object> reqJson);

    @PostMapping("encrypt/v1/delDownload")
    void delDownload(@RequestParam(name = "date")String  date);


}
