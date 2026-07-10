package org.sakaiproject.sitestats.tool.mvc;

import static org.mockito.Mockito.mock;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.tool.transformers.ResolvedRefTransformer;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

class SiteStatsToolServiceTestFixture {

    final StatsManager statsManager = mock(StatsManager.class);
    final SiteStatsReportAccessService reportAccessService = mock(SiteStatsReportAccessService.class);
    final ToolManager toolManager = mock(ToolManager.class);

    SiteStatsToolService createService() {
        UserTimeService userTimeService = mock(UserTimeService.class);
        UserDirectoryService userDirectoryService = mock(UserDirectoryService.class);
        ResolvedRefTransformer transformer = new ResolvedRefTransformer(
                statsManager, userTimeService, userDirectoryService);
        return new SiteStatsToolService(
                statsManager,
                mock(StatsUpdateManager.class),
                mock(EventRegistryService.class),
                mock(SiteStatsToolEventsService.class),
                mock(DetailedEventsManager.class),
                mock(ReportManager.class),
                mock(SiteStatsViewService.class),
                reportAccessService,
                mock(SiteStatsReportExportService.class),
                mock(SiteStatsReportPreviewService.class),
                mock(SiteService.class),
                mock(AuthzGroupService.class),
                userDirectoryService,
                userTimeService,
                toolManager,
                transformer);
    }
}
