//package com.cresign.timer.jobfactory;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.timer.service.ScheduledJob;
//import com.cresign.tools.annotation.SecurityParameter;
//import com.cresign.tools.apires.ApiResponse;
//import org.quartz.SchedulerException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.text.ParseException;
//
//@RestController
//@RequestMapping("/quartz")
//public class QuartzController{
//
//    @Autowired
//    public SchedulerManager myScheduler;
//
//    @Resource
//    private ScheduledJob scheduledJob;
//
//    //服务器加载Servlet的时候运行，并且只会被服务器执行一次
//    @PostConstruct
//    public void setMyScheduler() throws SchedulerException, IOException, ParseException {
//        scheduledJob.setMyScheduler();
//    }
//
//    @SecurityParameter
//    @PostMapping("/v1/xxx")
//    public ApiResponse xxx(@RequestBody JSONObject reqJson) throws ParseException, SchedulerException {
//
//        return scheduledJob.xxx(
//                reqJson.getString("time"),
//                reqJson.getString("jobName"),
//                reqJson.getString("jobGroup"),
//                reqJson.getString("className"),
//                reqJson.getInteger("repeat"));
//
//    }
//
//    @RequestMapping(value = "/xxx_job",method = RequestMethod.GET)
//    public String scheduleJob2() {
//        try {
//
//            //1.执行时间    2.类名称+公司id    3.组别+公司id    4.任务体  5.是否重复  07/27 16:00:00
//            //A公司 2021/07/25 20:54:00    B公司 2021/07/25 20:58:00
//            myScheduler.startJob("2021/08/12 ","job-","group-", xxx.class,1);//每五秒执行一次
//            //0 0/5 14 * * ?在每天下午2点到下午2:55期间的每5分钟触发 0/5 * * * * ?
//            //0 50 14 * * ?在每天下午2点50分5秒执行一次
//            //myScheduler.startJob("5 50 14 * * ?","job2","group2", xxx.class);
//            return "启动定时器成功";
//        } catch (SchedulerException | ParseException e) {
//            e.printStackTrace();
//        }
//        return "启动定时器失败";
//    }
//
//
//    @RequestMapping(value = "/del_job2",method = RequestMethod.GET)
//    public String deleteScheduleJob2()
//    {
//        try {
//            myScheduler.deleteJob("job2","group2");
//            return "删除定时器成功";
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//        }
//        return "删除定时器失败";
//    }
//
//
//}