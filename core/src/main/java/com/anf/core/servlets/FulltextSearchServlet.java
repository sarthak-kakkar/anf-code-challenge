package com.anf.core.servlets;

import com.anf.core.dto.FullTextSearchItem;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(service = Servlet.class, property = {
        SLING_SERVLET_PATHS + "=/bin/anfsearch",
        SLING_SERVLET_METHODS + "=GET"
    }
)
public class FulltextSearchServlet extends SlingSafeMethodsServlet {

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String fulltext = request.getParameter("fulltext");
        if (fulltext.trim().equals(StringUtils.EMPTY)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        SearchResult searchResult = executeQuery(request.getResourceResolver(), fulltext);

        if (searchResult.getTotalMatches() == 0) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        List<FullTextSearchItem> searchResponse = createResponse(searchResult);

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(searchResponse));
    }

    private static List<FullTextSearchItem> createResponse(SearchResult searchResult) {
        Iterator<Resource> iterator = searchResult.getResources();
        List<FullTextSearchItem> searchResponse = new ArrayList<>();

        while (iterator.hasNext()) {
            Resource resource = iterator.next();
            Resource image = resource.getChild("image");
            FullTextSearchItem fullTextSearchItem = FullTextSearchItem.builder()
                    .title(resource.getValueMap().get(JcrConstants.JCR_TITLE, StringUtils.EMPTY))
                    .description(resource.getValueMap().get(JcrConstants.JCR_DESCRIPTION, StringUtils.EMPTY))
                    .image(image != null ? image.getValueMap().get("fileReference", StringUtils.EMPTY) : StringUtils.EMPTY)
                    .lastModified(resource.getValueMap().get("cq:lastModified", StringUtils.EMPTY))
                    .build();
            searchResponse.add(fullTextSearchItem);
        }
        return searchResponse;
    }

    private SearchResult executeQuery(ResourceResolver resourceResolver, String fulltext) {
        Map<String, String> map = new HashMap<>();
        map.put("path", "/content/anf-code-challenge");
        map.put("type", "cq:PageContent");
        map.put("group.p.or", "true");
        map.put("group.1_fulltext", fulltext);
        map.put("group.1_fulltext.relPath", "@" + JcrConstants.JCR_TITLE);
        map.put("group.2_fulltext", fulltext);
        map.put("group.2_fulltext.relPath", "@" + JcrConstants.JCR_DESCRIPTION);
        map.put("p.limit", "-1");

        Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        return query.getResult();
    }
}
