package com.cresign.timer.service;

import com.alibaba.fastjson.JSONObject;
import org.quartz.SchedulerException;

import java.io.IOException;

public interface QuartzService {

    Object getJobList(String id_C) throws IOException;

    Object getJob(String jobName) throws IOException;

    Object addJob(String id_C, String cron, JSONObject jsonData) throws SchedulerException, IOException;

    Object updateJob(String id_C, String jobName, String cron, JSONObject jsonData) throws SchedulerException, IOException;

    Object delJob(String jobName) throws SchedulerException, IOException;
}


