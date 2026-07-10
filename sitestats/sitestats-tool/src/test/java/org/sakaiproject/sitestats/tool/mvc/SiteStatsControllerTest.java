package org.sakaiproject.sitestats.tool.mvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class SiteStatsControllerTest {

    private SiteStatsToolServiceTestFixture fixture;
    private Tool tool;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        fixture = new SiteStatsToolServiceTestFixture();
        tool = mock(Tool.class);
        Placement placement = mock(Placement.class);
        when(fixture.toolManager.getCurrentTool()).thenReturn(tool);
        when(fixture.toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("site1");
        SiteStatsToolService toolService = fixture.createService();
        SiteStatsController controller = new SiteStatsController(
                toolService, mock(SiteStatsToolExportService.class), new StaticMessageSource());
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
        doThrow(new SecurityException("Not authorized"))
                .when(fixture.reportAccessService).assertCanViewAll("site1");
        mockMvc.perform(get("/reports").param("siteId", "site1"))
                .andExpect(status().isForbidden());
    }
}
