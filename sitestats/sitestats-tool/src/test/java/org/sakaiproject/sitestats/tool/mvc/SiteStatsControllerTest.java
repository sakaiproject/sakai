package org.sakaiproject.sitestats.tool.mvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class SiteStatsControllerTest {

    private SakaiFacade facade;
    private StatsAuthz statsAuthz;
    private Tool tool;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        facade = mock(SakaiFacade.class);
        ToolManager toolManager = mock(ToolManager.class);
        tool = mock(Tool.class);
        Placement placement = mock(Placement.class);
        statsAuthz = mock(StatsAuthz.class);
        when(facade.getToolManager()).thenReturn(toolManager);
        when(toolManager.getCurrentTool()).thenReturn(tool);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site1");
        when(facade.getStatsAuthz()).thenReturn(statsAuthz);
        SiteStatsToolService toolService = new SiteStatsToolService(facade);
        SiteStatsController controller = new SiteStatsController(toolService, facade, new StaticMessageSource());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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
        when(statsAuthz.isUserAbleToViewSiteStats("site1")).thenReturn(false);
        mockMvc.perform(get("/reports").param("siteId", "site1"))
                .andExpect(status().isForbidden());
    }
}
