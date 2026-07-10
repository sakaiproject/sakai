package org.sakaiproject.sitestats.tool.mvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
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
import org.sakaiproject.sitestats.tool.config.SiteStatsWebMvcConfiguration;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.ViewResolverComposite;

@Configuration
@Import(SiteStatsWebMvcConfiguration.class)
@PropertySource("classpath:/hibernate.properties")
public class SiteStatsToolTestConfiguration extends SakaiTestConfiguration {

    @Bean
    public ViewResolver testViewResolver() {
        ViewResolverComposite resolver = new ViewResolverComposite();
        resolver.setOrder(0);
        resolver.setViewResolvers(Collections.singletonList((viewName, locale) -> viewName.startsWith("redirect:")
                ? null : new View() {
            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
                response.setContentType(getContentType());
            }
        }));
        return resolver;
    }

    @Override
    @Bean(name = "org.sakaiproject.component.api.ServerConfigurationService")
    public ServerConfigurationService serverConfigurationService() {
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        when(serverConfigurationService.getString("version.service", "0")).thenReturn("test");
        when(serverConfigurationService.getString("portal.cdn.version", "test")).thenReturn("test");
        return serverConfigurationService;
    }

    @Bean(name = "org.sakaiproject.sitestats.api.StatsManager")
    public StatsManager statsManager() {
        return mock(StatsManager.class);
    }

    @Bean(name = "org.sakaiproject.sitestats.api.StatsUpdateManager")
    public StatsUpdateManager statsUpdateManager() {
        return mock(StatsUpdateManager.class);
    }

    @Bean(name = "org.sakaiproject.sitestats.api.event.EventRegistryService")
    public EventRegistryService eventRegistryService() {
        return mock(EventRegistryService.class);
    }

    @Bean(name = "org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService")
    public SiteStatsToolEventsService siteStatsToolEventsService() {
        return mock(SiteStatsToolEventsService.class);
    }

    @Bean(name = "org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager")
    public DetailedEventsManager detailedEventsManager() {
        return mock(DetailedEventsManager.class);
    }

    @Bean(name = "org.sakaiproject.sitestats.api.report.ReportManager")
    public ReportManager reportManager() {
        return mock(ReportManager.class);
    }

    @Bean
    public SiteStatsViewService siteStatsViewService() {
        return mock(SiteStatsViewService.class);
    }

    @Bean
    public SiteStatsReportAccessService siteStatsReportAccessService() {
        return mock(SiteStatsReportAccessService.class);
    }

    @Bean
    public SiteStatsReportExportService siteStatsReportExportService() {
        return mock(SiteStatsReportExportService.class);
    }

    @Bean
    public SiteStatsReportPreviewService siteStatsReportPreviewService() {
        return mock(SiteStatsReportPreviewService.class);
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        UserTimeService userTimeService = mock(UserTimeService.class);
        when(userTimeService.getLocalTimeZone()).thenReturn(TimeZone.getTimeZone("America/New_York"));
        return userTimeService;
    }

    @Bean(name = "org.sakaiproject.util.api.LocaleService")
    public LocaleService localeService() {
        LocaleService localeService = mock(LocaleService.class);
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.US);
        return localeService;
    }
}
