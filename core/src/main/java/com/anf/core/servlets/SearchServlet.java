package com.anf.core.servlets;

import com.anf.core.dto.SearchResponse;
import com.day.cq.wcm.api.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.sling.api.servlets.ServletResolverConstants.*;

@Component(service = Servlet.class, property = {
        SLING_SERVLET_RESOURCE_TYPES + "=cq:Page",
        SLING_SERVLET_METHODS + "=GET",
        SLING_SERVLET_EXTENSIONS + "=json",
        SLING_SERVLET_EXTENSIONS + "=xml",
        SLING_SERVLET_SELECTORS + "=anf"
    }
)
@Slf4j
public class SearchServlet extends SlingSafeMethodsServlet {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, "anf"))){
            UserManager userManager = resourceResolver.adaptTo(UserManager.class);
            Page requestPage = request.getResource().adaptTo(Page.class);
            if (requestPage != null) {
                SearchResponse searchResponse = createResponse(requestPage, userManager);
                if (searchResponse == null) {
                    response.setStatus(404);
                    return;
                }
                writeResponse(request, response, searchResponse);
            }
        } catch (RepositoryException | JAXBException | LoginException e) {
            log.error("[SearchServlet] Failed due to ", e);
        }
    }

    private SearchResponse createResponse(Page requestPage, UserManager userManager) throws RepositoryException {
        String username = requestPage.getContentResource().getValueMap().get("cq:lastModifiedBy", StringUtils.EMPTY);
        Authorizable user = userManager != null ? userManager.getAuthorizable(username) : null;
        if (user == null) {
            return null;
        }
        String firstName = user.hasProperty("./profile/givenName") ? Arrays.stream(user.getProperty("./profile/givenName")).map(String::valueOf).findFirst().orElse(null) : null;
        String lastName = user.hasProperty("./profile/familyName") ? Arrays.stream(user.getProperty("./profile/familyName")).map(String::valueOf).findFirst().orElse(null) : null;

        List<String> childPages = new ArrayList<>();
        requestPage.listChildren(page -> page.getContentResource().getValueMap().get("cq:lastModifiedBy", StringUtils.EMPTY).equals(username),
                true).forEachRemaining(child -> childPages.add(child.getPath()));

        return new SearchResponse(firstName, lastName, childPages);
    }

    private void writeResponse(SlingHttpServletRequest request, SlingHttpServletResponse response, SearchResponse searchResponse) throws IOException, JAXBException {
        String pathInfo = request.getPathInfo();
        if (pathInfo.endsWith(".json")) {
            response.setContentType(String.valueOf(ContentType.APPLICATION_JSON));
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(searchResponse));
        } else if (pathInfo.endsWith(".xml")) {
            response.setContentType(String.valueOf(ContentType.APPLICATION_XML));
            JAXBContext context = JAXBContext.newInstance(SearchResponse.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(searchResponse, response.getWriter());
        }
    }
}
