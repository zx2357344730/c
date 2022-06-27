package com.cresign.timer.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.client.SearchClient;
import com.cresign.timer.service.QuartzService;
import com.cresign.tools.annotation.SecurityParameter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("quartz")
public class QuartzController {

    @Autowired
    private QuartzService quartzService;

    @Autowired
    private SearchClient searchClient;

    @SecurityParameter
    @PostMapping("/v1/getJob")
    public Object getJob(@RequestBody JSONObject json) throws IOException {
        return quartzService.getJobList(
                json.getString("id_C")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/addJob")
    public Object addJob(@RequestBody JSONObject json) throws SchedulerException, IOException {
        return quartzService.addJob(
                json.getString("id_C"),
                json.getString("cron"),
                json.getJSONObject("jsonData")
        );
    }

    @SecurityParameter
    @PostMapping("/v1/delJob")
    public Object delJob(@RequestBody JSONObject json) throws SchedulerException, IOException {
        return quartzService.delJob(
                json.getString("jobName")
        );
    }

    @PostMapping("/v1/testSE")
    public Object testSE(@RequestBody JSONObject json) {
        System.out.println("json=" + json);
        Object value = searchClient.getStatValueByEsSE(json);
        return value;
    }
}
