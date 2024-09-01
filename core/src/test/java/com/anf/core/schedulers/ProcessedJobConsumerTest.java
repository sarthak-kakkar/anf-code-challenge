package com.anf.core.schedulers;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import org.apache.sling.api.resource.*;
import org.apache.sling.event.jobs.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessedJobConsumerTest {

    @InjectMocks
    private ProcessedJobConsumer processedJobConsumer;

    @Mock
    private Job job;

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private Session session;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Query query;

    @Mock
    private Resource resource;

    @Mock
    private ModifiableValueMap modifiableValueMap;

    @BeforeEach
    void setUp() throws LoginException {
        List<Resource> mockResources = new ArrayList<>();
        mockResources.add(resource);

        when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenReturn(resourceResolver);
        lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        lenient().when(queryBuilder.createQuery(any(PredicateGroup.class), any(Session.class))).thenReturn(query);
        lenient().when(query.getResult().getResources()).thenReturn(mockResources.listIterator());
        lenient().when(resource.adaptTo(ModifiableValueMap.class)).thenReturn(modifiableValueMap);
    }

    @Test
    void processSuccess() throws PersistenceException {
        processedJobConsumer.process(job);
        verify(resourceResolver).commit();
    }

    @Test
    void processFailed() throws PersistenceException, LoginException {
        when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenThrow(LoginException.class);
        processedJobConsumer.process(job);
        verify(resourceResolver, never()).commit();
    }
}