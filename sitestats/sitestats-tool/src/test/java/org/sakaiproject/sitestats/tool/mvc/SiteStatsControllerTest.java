package org.sakaiproject.sitestats.tool.mvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.ZoneId;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SiteStatsToolTestConfiguration.class)
@WebAppConfiguration("src/webapp")
public class SiteStatsControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private SiteStatsReportAccessService reportAccessService;
    @Autowired private SiteStatsViewService siteStatsViewService;
    @Autowired private ToolManager toolManager;

    private Tool tool;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        reset(reportAccessService, siteStatsViewService, toolManager);
        tool = mock(Tool.class);
        Placement placement = mock(Placement.class);
        when(toolManager.getCurrentTool()).thenReturn(tool);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site1");
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void regularToolEntryRedirectsToOverview() throws Exception {
        when(tool.getId()).thenReturn(StatsManager.SITESTATS_TOOLID);
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void adminToolEntryRedirectsToAdmin() throws Exception {
        when(tool.getId()).thenReturn(StatsManager.SITESTATS_ADMIN_TOOLID);
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    public void unauthorizedDirectRouteReturnsForbidden() throws Exception {
        when(tool.getId()).thenReturn(StatsManager.SITESTATS_TOOLID);
        doThrow(new SecurityException("Not authorized"))
                .when(reportAccessService).assertCanViewAll("site1");
        mockMvc.perform(get("/reports").param("siteId", "site1"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void overviewUsesWiredControllerAdviceContext() throws Exception {
        when(tool.getId()).thenReturn(StatsManager.SITESTATS_TOOLID);
        SiteStatsOverview overview = new SiteStatsOverview();
        overview.setSiteId("site1");
        when(siteStatsViewService.getOverview("site1")).thenReturn(overview);

        mockMvc.perform(get("/home").param("siteId", "site1"))
                .andExpect(status().isOk())
                .andExpect(view().name("overview"))
                .andExpect(model().attribute("locale", Locale.US))
                .andExpect(model().attribute("timeZone", ZoneId.of("America/New_York")))
                .andExpect(model().attribute("cdnQuery", "?version=test"))
                .andExpect(model().attribute("siteId", "site1"));
    }

    @Test
    public void reportUserSearchUsesJsonRoute() throws Exception {
        mockMvc.perform(get("/reports/users")
                    .param("siteId", "site1")
                    .param("q", "a")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void reportUserSearchRequiresReportAuthorization() throws Exception {
        doThrow(new SecurityException("Not authorized"))
                .when(reportAccessService).assertCanViewAll("site1");

        mockMvc.perform(get("/reports/users")
                    .param("siteId", "site1")
                    .param("q", "alice")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
