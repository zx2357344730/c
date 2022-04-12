package com.cresign.timer.jobfactory;

import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;



/**
 * 自定义定时任务
 */
public class setJob implements Job {

    private static final Logger logger= Logger.getLogger(setJob.class);



    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println(jobExecutionContext.getTrigger());

        //执行任务逻辑....
        logger.info("下午好：执行自定义定时任务"+DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));


    }


}