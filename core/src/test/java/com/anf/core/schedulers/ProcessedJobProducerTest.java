package com.anf.core.schedulers;

import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.settings.SlingSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessedJobProducerTest {

    @InjectMocks
    private ProcessedJobProducer processedJobProducer;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JobManager jobManager;

    @Mock
    private SlingSettingsService slingSettingsService;

    @Mock
    private ProcessedJobProducer.Config config;

    @BeforeEach
    void setUp() {
        Set<String> runmodes = Collections.singleton("author");

        when(jobManager.getScheduledJobs(anyString(), anyLong(), any()).isEmpty()).thenReturn(true);
        when(slingSettingsService.getRunModes()).thenReturn(runmodes);
    }

    @Test
    void testJobCreation() {
        processedJobProducer.activate(config);

        verify(jobManager, atMost(1)).createJob(anyString());
    }
}