package com.anf.core.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.resourceresolver.MockResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulltextSearchServletTest {

    @InjectMocks
    private FulltextSearchServlet fulltextSearchServlet;

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingHttpServletResponse response;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private Session session;

    @Mock
    private SearchResult searchResult;

    @Mock
    private Query query;

    @Mock
    private PrintWriter printWriter;

    @BeforeEach
    void setup() throws IOException {
        Map<String, Object> values = new HashMap<>();
        values.put("jcr:title", "Title");
        values.put("jcr:description", "Description");
        values.put("cq:lastModified", LocalDateTime.now());

        MockResource mockResource = new MockResource("/content/page", values, resourceResolver);

        List<Resource> resources = Collections.singletonList(mockResource);

        when(request.getParameter("fulltext")).thenReturn("fulltext");
        when(request.getResourceResolver()).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        when(queryBuilder.createQuery(any(PredicateGroup.class), any(Session.class))).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getTotalMatches()).thenReturn(1L);
        when(searchResult.getResources()).thenReturn(resources.iterator());
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void doGetSuccess() throws IOException {
        fulltextSearchServlet.doGet(request, response);
        verify(printWriter, atMost(1)).write(any(String.class));
    }
}