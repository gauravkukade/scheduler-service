package com.coderolls.jobplatform.schedulerservice.config;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class QuartzConfig {

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory() {
            @Override
            protected Object createJobInstance(org.quartz.spi.TriggerFiredBundle bundle) throws Exception {
                Object job = super.createJobInstance(bundle);
                beanFactory.autowireBean(job);
                return job;
            }
        };
        return jobFactory;
    }
}