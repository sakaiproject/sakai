package org.sakaiproject.announcement.tool;

import lombok.Getter;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementChannelEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.util.ToolPortalServlet;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.vm.VelocityServlet;
import org.springframework.mock.web.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("src/webapp")
// With the bullhorn handlers no longer being scanned/created in the kernel, we can load the announcement service in
// a separate context. For that to work, we must set the global context to ours, so the component manager can find our
// beans. Or we can customize the kernel context through the hierarchy. This gives us support for disentangling tools
// from the global state. Announcements is an interesting example because the tool uses its own cover, and the cover
// caches what it gets back from the component manager... So, because the cover is used in other places during init,
// we would have to have the new context in place during its own refresh. For now, it's easier to load it in the same
// context as the kernel, deal with the cover issues later.
@ContextHierarchy({
        @ContextConfiguration(name = "kernel", locations = "file:../../announcement-impl/impl/src/webapp/WEB-INF/components.xml"),
//        @ContextConfiguration(locations = "file:../../announcement-impl/impl/src/webapp/WEB-INF/components.xml"),
        @ContextConfiguration(classes = AnncTestBeanConfig.class)
})

public class WebContextSetupTest extends ModiWebTest {
    @Inject WebApplicationContext applicationContext;
    @Inject MockServletContext servletContext;
    @Inject ToolPortalServlet toolPortal;
    @Inject ActiveToolManager activeToolManager;
    @Inject UsageSessionService usageSessionService;
    @Inject SessionManager sessionManager;
    @Inject AnnouncementService announcementService;

    @Before
    public void setup() throws ServletException {
        AnnouncementAction action = new AnnouncementAction();
        MockServletConfig actionConfig = new MockServletConfig(servletContext);
        actionConfig.addInitParameter("template", "announcement/chef_announcements");
        action.init(actionConfig);

        VelocityServlet velocity = new VelocityServlet();
        MockServletConfig velocityConfig = new MockServletConfig(servletContext);
        velocityConfig.addInitParameter("properties", "velocity.properties");
        velocity.init(velocityConfig);

        activeToolManager.register(Path.of("src/webapp/WEB-INF/tools/sakai.announcements.xml").toFile(), servletContext);
        servletContext.registerNamedDispatcher("sakai.announcements", new ToolDispatcher(action));
        servletContext.registerNamedDispatcher("sakai.vm", new ToolDispatcher(velocity));
    }

    @Test
    public void givenAWorkingAnnouncementsTool_whenWeRequestWithMockMvc_thenTheToolResponds() throws Exception {
        Session session = sessionManager.startSession();
        session.setUserEid("admin");
        session.setUserId("admin");
        sessionManager.setCurrentSession(session);
        String cookieValue = session.getId() + "." + System.getProperty(RequestFilter.SAKAI_SERVERID, "sakai");
        Cookie sessionCookie = new Cookie("JSESSIONID", cookieValue);

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .addFilters(new RequestFilteredServlet(toolPortal))
                .build();

        mvc.perform(get("/mercury-310").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreMatchers.containsString("<title>Announcements</title>")));
    }

    @Test
    public void givenAWorkingAnnouncementsTool_whenWeRequestThroughFilterChain_thenTheToolResponds() throws ServletException, IOException, URISyntaxException {
        Session session = sessionManager.startSession();
        session.setUserEid("admin");
        session.setUserId("admin");
        sessionManager.setCurrentSession(session);
        String cookieValue = session.getId() + "." + System.getProperty(RequestFilter.SAKAI_SERVERID, "sakai");
        Cookie sessionCookie = new Cookie("JSESSIONID", cookieValue);

        MockHttpServletRequest request = MockMvcRequestBuilders
                .get(new URI("/mercury-310"))
                .cookie(sessionCookie)
                .buildRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();

        RequestFilter requestFilter = new RequestFilter();
        MockFilterChain filterChain = new MockFilterChain(toolPortal, requestFilter);
        filterChain.doFilter(request, response);

        assertThat(response.getContentAsString()).contains("<title>Announcements</title>");
    }

    @Test
    public void givenAWorkingAnnouncementsTool_whenWeAddAMessageViaService_thenTheToolListsIt() throws Exception {
        Session session = sessionManager.startSession();
        session.setUserEid("admin");
        session.setUserId("admin");
        sessionManager.setCurrentSession(session);
        String cookieValue = session.getId() + "." + System.getProperty(RequestFilter.SAKAI_SERVERID, "sakai");
        Cookie sessionCookie = new Cookie("JSESSIONID", cookieValue);

        AnnouncementChannel channel;
        try {
            channel = announcementService.getAnnouncementChannel("/announcement/channel/mercury/main");
        } catch (IdUnusedException e) {
            AnnouncementChannelEdit edit = announcementService.addAnnouncementChannel("/announcement/channel/mercury/main");
            announcementService.commitChannel(edit);
            channel = announcementService.getAnnouncementChannel("/announcement/channel/mercury/main");
        }
        channel.addAnnouncementMessage("This is a modi test", false, List.of(), "This is only a test.");

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .addFilters(new RequestFilteredServlet(toolPortal))
                .build();

        mvc.perform(get("/mercury-310").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreMatchers.containsString("Viewing 1 - 1 of 1 items")))
                .andExpect(content().string(CoreMatchers.containsString("This is a modi test")));
    }

    public static class ToolDispatcher implements RequestDispatcher {
        @Getter
        final HttpServlet tool;
        public ToolDispatcher(HttpServlet tool) {
            this.tool = tool;
        }

        @Override
        public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            tool.service(request, response);
        }

        @Override
        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            tool.service(request, response);
        }
    }

    public static class RequestFilteredServlet implements Filter {
        private final MockFilterChain requestFilterAndServlet;
        public RequestFilteredServlet(Servlet servlet) {
            this.requestFilterAndServlet = new MockFilterChain(servlet, new RequestFilter());
        }
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            requestFilterAndServlet.doFilter(request, response);
        }
    }
}
