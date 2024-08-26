package com.anf.core.schedulers;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component(property = {
        JobConsumer.PROPERTY_TOPICS + "=job/topic/processed"
    }
)
@Slf4j
public class ProcessedJobConsumer implements JobConsumer {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public JobResult process(Job job) {
        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, "anf"))) {
            SearchResult result = executeQuery(resourceResolver);
            Iterator<Resource> iterator = result.getResources();

            while (iterator.hasNext()) {
                Resource resource = iterator.next();
                ModifiableValueMap modifiableValueMap = resource.adaptTo(ModifiableValueMap.class);
                if (modifiableValueMap != null) {
                    modifiableValueMap.put("processedDate", String.valueOf(OffsetDateTime.now()));
                }
            }
            resourceResolver.commit();
        } catch (LoginException | PersistenceException e) {
            log.error("[ProcessedJobConsumer] Failed due to ", e);
            // return JobResult.FAILED; Can be added for auto-retries(due to PersistenceException).
        }
        return JobResult.OK;
    }

    private SearchResult executeQuery(ResourceResolver resourceResolver) {
        Map<String, String> map = new HashMap<>();
        map.put("path", "/content/anf-code-challenge");
        map.put("type", "cq:PageContent");
        map.put("1_property", "cq:lastReplicated");
        map.put("1_property.operation", "exists");
        map.put("2_property", "processedDate");
        map.put("2_property.operation", "exists");
        map.put("2_property.value", "false");
        map.put("property.and", "true");
        map.put("p.limit", "-1");

        Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        return query.getResult();
    }
}
