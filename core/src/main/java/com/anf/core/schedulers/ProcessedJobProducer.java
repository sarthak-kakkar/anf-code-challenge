package com.anf.core.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.ScheduledJobInfo;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.Collection;

@Component
@Designate(ocd = ProcessedJobProducer.Config.class)
@Slf4j
public class ProcessedJobProducer {

    @ObjectClassDefinition
    public @interface Config {
        @AttributeDefinition(name = "Scheduled Job Cron", type = AttributeType.STRING)
        String cron() default "0 0/2 * * * ?";
    }

    private ProcessedJobProducer.Config config;

    @Reference
    private JobManager jobManager;

    @Reference
    private SlingSettingsService slingSettingsService;

    @Activate
    @Modified
    protected void activate(ProcessedJobProducer.Config config) {
        this.config = config;
        Collection<ScheduledJobInfo> topicJobQueue = jobManager.getScheduledJobs("job/topic/processed", 1, null);
        if (topicJobQueue.isEmpty() && slingSettingsService.getRunModes().contains("author")) {
            JobBuilder.ScheduleBuilder scheduleBuilder = jobManager.createJob("job/topic/processed").schedule();
            scheduleBuilder.cron(config.cron());
            ScheduledJobInfo scheduledJobInfo = scheduleBuilder.add();
            log.info("[ProcessedJobProducer] Job schedule info, next at {}", scheduledJobInfo.getNextScheduledExecution());
        }
    }
}
