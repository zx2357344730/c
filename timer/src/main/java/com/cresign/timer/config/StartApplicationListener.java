package com.cresign.timer.config;

import com.example.job.QuartzJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private Scheduler scheduler;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

//        try {
//            TriggerKey triggerKey1 = TriggerKey.triggerKey("trigger1","trigger-group1");
//            Trigger trigger1 = scheduler.getTrigger(triggerKey1);
//            if(trigger1 == null){
//                System.out.println("任务一注入");
//                trigger1 = TriggerBuilder.newTrigger()
//                        .withIdentity(triggerKey1)//给Trigger起个名字
//                        .withSchedule(CronScheduleBuilder.cronSchedule("0/20 * * * * ?"))
//                        .startNow()
//                        .build();
//                JobDetail jobDetail1 = JobBuilder.newJob(QuartzJob.class)
//                        .withIdentity("syncUserJobDetail", "job-group1")
//                        .usingJobData("job", "111") //设置参数（键值对）
//                        .usingJobData("api", "test")
//                        .storeDurably() //即使没有Trigger关联时，也不需要删除该JobDetail
//                        .build();
//                scheduler.scheduleJob(jobDetail1,trigger1);
//
//
//
//            }
//            TriggerKey triggerKey2 = TriggerKey.triggerKey("trigger2","trigger-group2");
//            Trigger trigger2 = scheduler.getTrigger(triggerKey2);
//            if(trigger2 == null) {
//                System.out.println("任务二注入");
//                trigger2 = TriggerBuilder.newTrigger()
//                        .withIdentity(triggerKey2)
//                        .withSchedule(CronScheduleBuilder.cronSchedule("0/20 * * * * ?"))
//                        .startNow()
//                        .build();
//                JobDetail jobDetail2 = JobBuilder.newJob(QuartzJob.class)
//                        .withIdentity("syncUserJobDetail", "job-group2")
//                        .usingJobData("job", "222") //设置参数（键值对）
//                        .usingJobData("api", "test2")
//                        .storeDurably() //即使没有Trigger关联时，也不需要删除该JobDetail
//                        .build();
//                scheduler.scheduleJob(jobDetail2, trigger2);
//            }
//            TriggerKey triggerKey3 = TriggerKey.triggerKey("trigger3","trigger-group3");
//            Trigger trigger3 = scheduler.getTrigger(triggerKey3);
//            if(trigger3 == null) {
//                System.out.println("任务三注入");
//                trigger3 = TriggerBuilder.newTrigger()
//                        .withIdentity(triggerKey3)
//                        .withSchedule(CronScheduleBuilder.cronSchedule("0/20 * * * * ?"))
//                        .startNow()
//                        .build();
//                JobDetail jobDetail3 = JobBuilder.newJob(QuartzJob.class)
//                        .withIdentity("syncUserJobDetail", "job-group3")
//                        .usingJobData("job", "333") //设置参数（键值对）
//                        .usingJobData("api", "test3")
//                        .storeDurably() //即使没有Trigger关联时，也不需要删除该JobDetail
//                        .build();
//                scheduler.scheduleJob(jobDetail3, trigger3);
//            }
//            TriggerKey triggerKey4 = TriggerKey.triggerKey("trigger4","trigger-group4");
//            Trigger trigger4 = scheduler.getTrigger(triggerKey4);
//            if(trigger4 == null) {
//                System.out.println("任务四注入");
//                trigger4 = TriggerBuilder.newTrigger()
//                        .withIdentity(triggerKey4)
//                        .withSchedule(CronScheduleBuilder.cronSchedule("0/20 * * * * ?"))
//                        .startNow()
//                        .build();
//                JobDetail jobDetail4 = JobBuilder.newJob(QuartzJob.class)
//                        .withIdentity("syncUserJobDetail", "job-group4")
//                        .usingJobData("job", "444") //设置参数（键值对）
//                        .usingJobData("api", "test4")
//                        .storeDurably() //即使没有Trigger关联时，也不需要删除该JobDetail
//                        .build();
//                scheduler.scheduleJob(jobDetail4, trigger4);
//            }
//            System.out.println("任务开始");
//                scheduler.start();
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//        }
    }
}
