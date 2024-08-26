package com.anf.core.servlets;

import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.value.StringValue;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServletTest {

    @InjectMocks
    private SearchServlet searchServlet;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SlingHttpServletRequest request;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SlingHttpServletResponse response;

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Page page;

    @Mock
    private UserManager userManager;

    @Mock
    private Authorizable authorizable;

    @BeforeEach
    void setUp() throws LoginException, RepositoryException {
        Value firstName = new StringValue("Test");
        Value lastName = new StringValue("User");

        when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenReturn(resourceResolver);
        when(request.getResource().adaptTo(Page.class)).thenReturn(page);
        when(page.getContentResource().getValueMap().get("cq:lastModifiedBy", StringUtils.EMPTY)).thenReturn("testuser");
        when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable(any(String.class))).thenReturn(authorizable);
        lenient().when(authorizable.hasProperty("./profile/givenName")).thenReturn(true);
        lenient().when(authorizable.hasProperty("./profile/familyName")).thenReturn(true);
        lenient().when(authorizable.getProperty("./profile/givenName")).thenReturn(new Value[]{firstName});
        lenient().when(authorizable.getProperty("./profile/familyName")).thenReturn(new Value[]{lastName});
    }

    @Test
    void doGetSuccessSelectorJson() throws IOException {
        when(request.getPathInfo()).thenReturn("/content/page.anf.json");
        searchServlet.doGet(request, response);
        verify(response.getWriter(), atMost(1)).write(anyString());

    }

    @Test
    void doGetSuccessSelectorXml() throws IOException {
        when(request.getPathInfo()).thenReturn("/content/page.anf.xml");
        searchServlet.doGet(request, response);
        verify(response, atMost(1)).getWriter();

    }
}