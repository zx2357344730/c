package com.cresign.timer.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.enumeration.TimerEnum;
import com.cresign.timer.service.QuartzService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.es.lSJob;
import com.cresign.tools.pojo.po.QuartzJobs;
import com.example.job.QuartzJob;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class QuartzServiceImpl implements QuartzService {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private RetResult retResult;

    @Override
    public Object getJobList(String id_C) throws IOException {
        JSONArray arrayHit = dbUtils.getEsKey("id_C", id_C, "lSJob");
        return arrayHit;

//        Query query = new Query(new Criteria("params.id_C").is(id_C));
//        query.fields().include("keyName");
//        query.fields().include("params");
//        List<QuartzJobs> quartzJobs = mongoTemplate.find(query, QuartzJobs.class);
//        return quartzJobs;
    }

    @Override
    public Object getJob(String jobName) throws IOException {
        Query query = new Query(new Criteria("keyName").is(jobName));
        List<QuartzJobs> quartzJobs = mongoTemplate.find(query, QuartzJobs.class);
        System.out.println("quartzJobs=" + quartzJobs);
        return retResult.ok(CodeEnum.OK.getCode(), quartzJobs);
    }

    @Override
    public Object addJob(String id_C, String cron, JSONObject jsonData) throws SchedulerException, IOException {
        String jobName = MongoUtils.GetObjectId();
        JobKey jobKey = new JobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            throw new ErrorResponseException(HttpStatus.OK, TimerEnum.JOBNAME_IS_EXIST.getCode(), null);
        }
        JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class)
                .withIdentity(jobName)
                .build();
        jsonData.put("id_C", id_C);
        if (jsonData != null) {
            jobDetail.getJobDataMap().put("params", jsonData);
        }
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName)
                .forJob(jobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing())
                .build();
        scheduler.scheduleJob(jobDetail, cronTrigger);
        scheduler.start();
        lSJob lsjob = new lSJob(jobName, id_C, cron, jsonData.getJSONObject("wrdN"), jsonData.getJSONObject("wrdesc"));
        dbUtils.addES((JSONObject) JSON.toJSON(lsjob), "lSJob");
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }

    @Override
    public Object updateJob(String id_C, String jobName, String cron, JSONObject jsonData) throws SchedulerException, IOException {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder().must(QueryBuilders.termQuery("jobName", jobName));
        SearchResponse searchResponse = dbUtils.getEsQuery(queryBuilder, "lSJob");
        JSONObject jsonLsjob = JSON.parseObject(searchResponse.getHits().getHits()[0].getSourceAsString());
        //jsonData没有键值对
        if (jsonData.isEmpty()) {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName);
            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing())
                    .build();
            scheduler.rescheduleJob(triggerKey, cronTrigger);
            jsonLsjob.put("cron", cron);
        } else {
            JobKey jobKey = JobKey.jobKey(jobName);
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName);
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);

            JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class)
                    .withIdentity(jobName)
                    .build();
            jsonData.put("id_C", id_C);
            if (jsonData != null) {
                jobDetail.getJobDataMap().put("params", jsonData);
            }
            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName)
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing())
                    .build();
            scheduler.scheduleJob(jobDetail, cronTrigger);
            scheduler.start();
            jsonLsjob.put("cron", cron);
            jsonLsjob.put("wrdN", jsonData.getJSONObject("wrdN"));
            jsonLsjob.put("wrddesc", jsonData.getJSONObject("wrddesc"));

        }

        return retResult.ok(CodeEnum.OK.getCode(), null);
    }


    @Override
    public Object delJob(String jobName) throws SchedulerException, IOException {
        JobKey jobKey = JobKey.jobKey(jobName);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName);
        scheduler.pauseTrigger(triggerKey);
        scheduler.unscheduleJob(triggerKey);
        scheduler.deleteJob(jobKey);
        dbUtils.delES("lSJob", "jobName", jobName);
        return retResult.ok(CodeEnum.OK.getCode(), null);
    }
}
