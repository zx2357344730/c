package com.cresign.timer.config;

import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.text.ParseException;

@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws ParseException {
        SchedulerFactoryBean sfBean = new SchedulerFactoryBean();
        ClassPathResource cpr = new ClassPathResource("quartz.properties");
        sfBean.setConfigLocation(cpr);
        sfBean.setOverwriteExistingJobs(true);
        sfBean.setStartupDelay(10);
        // 解决 Job类中无法使用 Autowired 依赖注入问题
        sfBean.setJobFactory(autoWiringSpringBeanJobFactory());
        sfBean.setApplicationContextSchedulerContextKey("application");
        return sfBean;
    }

    @Bean
    public AutoWiringSpringBeanJobFactory autoWiringSpringBeanJobFactory() {
        return new AutoWiringSpringBeanJobFactory();
    }

    @Bean
    public Scheduler scheduler() throws ParseException {
        return schedulerFactoryBean().getScheduler();
    }
}
