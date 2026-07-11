package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

public class SiteStatsToolAuthorizationServiceTest {

    private SiteStatsReportAccessService reportAccessService;
    private ToolManager toolManager;
    private SiteStatsToolAuthorizationService authorizationService;

    @Before
    public void setUp() {
        reportAccessService = mock(SiteStatsReportAccessService.class);
        toolManager = mock(ToolManager.class);
        Placement placement = mock(Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn("current-site");
        authorizationService = new SiteStatsToolAuthorizationService(reportAccessService, toolManager);
    }

    @Test
    public void reportSiteRequiresViewAllForTheRequestedSite() {
        assertEquals("current-site", authorizationService.reportSite(null));

        verify(reportAccessService).assertCanViewAll("current-site");
    }

    @Test
    public void crossSiteReportRequiresAdminForTheCurrentPlacement() {
        assertEquals("other-site", authorizationService.reportSite("other-site"));

        verify(reportAccessService).assertCanViewAdmin("current-site");
        verify(reportAccessService).assertCanViewAll("other-site");
    }

    @Test
    public void authorizationFailuresAreNotHidden() {
        when(toolManager.getCurrentPlacement().getContext()).thenReturn("current-site");
        org.mockito.Mockito.doThrow(new SecurityException("Not authorized"))
                .when(reportAccessService).assertCanView("current-site");

        assertThrows(SecurityException.class, () -> authorizationService.viewSite(null));
    }
}
