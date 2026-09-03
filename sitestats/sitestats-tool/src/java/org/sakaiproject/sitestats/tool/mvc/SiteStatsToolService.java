/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.mvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.JobRun;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.UserId;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.PagingParams;
import org.sakaiproject.sitestats.api.event.detailed.SortingParams;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportSummary;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.tool.transformers.ResolvedRefTransformer;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SiteStatsToolService {

    private static final int PAGE_SIZE = 50;
    private static final int USER_SEARCH_CANDIDATE_LIMIT = 50;
    private static final int USER_SEARCH_RESULT_LIMIT = 20;

    private final StatsManager statsManager;
    private final StatsUpdateManager statsUpdateManager;
    private final EventRegistryService eventRegistryService;
    private final SiteStatsToolEventsService siteStatsToolEventsService;
    private final DetailedEventsManager detailedEventsManager;
    private final ReportManager reportManager;
    private final SiteStatsViewService siteStatsViewService;
    private final SiteStatsReportAccessService reportAccessService;
    private final SiteStatsReportExportService reportExportService;
    private final SiteStatsReportPreviewService reportPreviewService;
    private final SiteStatsToolAuthorizationService authorizationService;
    private final SiteStatsReportFormValidator reportFormValidator;
    private final SiteService siteService;
    private final AuthzGroupService authzGroupService;
    private final UserDirectoryService userDirectoryService;
    private final LocaleService localeService;
    private final UserTimeService userTimeService;
    private final ResolvedRefTransformer resolvedRefTransformer;

    public SiteStatsToolService(StatsManager statsManager, StatsUpdateManager statsUpdateManager,
            EventRegistryService eventRegistryService, SiteStatsToolEventsService siteStatsToolEventsService,
            DetailedEventsManager detailedEventsManager, ReportManager reportManager,
            SiteStatsViewService siteStatsViewService, SiteStatsReportAccessService reportAccessService,
            SiteStatsReportExportService reportExportService, SiteStatsReportPreviewService reportPreviewService,
            SiteStatsToolAuthorizationService authorizationService, SiteStatsReportFormValidator reportFormValidator,
            SiteService siteService, AuthzGroupService authzGroupService, UserDirectoryService userDirectoryService,
            LocaleService localeService,
            @Qualifier("org.sakaiproject.time.api.UserTimeService") UserTimeService userTimeService,
            ResolvedRefTransformer resolvedRefTransformer) {
        this.statsManager = statsManager;
        this.statsUpdateManager = statsUpdateManager;
        this.eventRegistryService = eventRegistryService;
        this.siteStatsToolEventsService = siteStatsToolEventsService;
        this.detailedEventsManager = detailedEventsManager;
        this.reportManager = reportManager;
        this.siteStatsViewService = siteStatsViewService;
        this.reportAccessService = reportAccessService;
        this.reportExportService = reportExportService;
        this.reportPreviewService = reportPreviewService;
        this.authorizationService = authorizationService;
        this.reportFormValidator = reportFormValidator;
        this.siteService = siteService;
        this.authzGroupService = authzGroupService;
        this.userDirectoryService = userDirectoryService;
        this.localeService = localeService;
        this.userTimeService = userTimeService;
        this.resolvedRefTransformer = resolvedRefTransformer;
    }

    public String currentSiteId() {
        return authorizationService.currentSiteId();
    }

    public String currentUserId() {
        return authorizationService.currentUserId();
    }

    public boolean isAdminTool() {
        return authorizationService.isAdminTool();
    }

    private SiteStatsOverview overview(String requestedSiteId) {
        String siteId = viewSite(requestedSiteId);
        SiteStatsOverview overview = siteStatsViewService.getOverview(siteId);
        statsManager.logEvent(null, StatsManager.LOG_ACTION_VIEW, siteId, true);
        return overview;
    }

    public OverviewResult overviewWithEndpoints(String requestedSiteId) {
        SiteStatsOverview overview = overview(requestedSiteId);
        Map<String, String> widgetEndpoints = new LinkedHashMap<String, String>();
        SiteStatsReportRequest reportRequest = new SiteStatsReportRequest();
        reportRequest.setIncludeTable(true);
        reportRequest.setIncludeChart(true);
        for (SiteStatsWidget widget : overview.getWidgets()) {
            if (widget.isVisible()) {
                for (SiteStatsWidgetTab tab : widget.getTabs()) {
                    widgetEndpoints.put(widget.getId() + ":" + tab.getId(), SiteStatsApiUrls.widgetReport(
                            overview.getSiteId(), widget.getId(), tab.getId(), reportRequest));
                }
            }
        }
        return new OverviewResult(overview, widgetEndpoints);
    }

    public List<SiteStatsReportSummary> reports(String requestedSiteId) {
        return siteStatsViewService.getReports(reportSite(requestedSiteId));
    }

    public SiteStatsReportForm newReportForm() {
        return SiteStatsReportForm.create(Clock.system(userTimeService.getLocalTimeZone().toZoneId()));
    }

    public ReportDef reportDefinition(String requestedSiteId, long reportId) {
        String siteId = reportSite(requestedSiteId);
        ReportDef report = reportAccessService.persistedReportDefinition(siteId, reportId);
        return new ReportDef(report, siteId);
    }

    public ReportDef editableReportDefinition(String requestedSiteId, long reportId) {
        String siteId = reportSite(requestedSiteId);
        ReportDef report = reportAccessService.persistedSiteReportDefinition(siteId, reportId);
        return new ReportDef(report, siteId);
    }

    public SiteStatsReportForm editReportForm(String requestedSiteId, long reportId) {
        ReportDef report = editableReportDefinition(requestedSiteId, reportId);
        return SiteStatsReportForm.from(report, userTimeService.getLocalTimeZone().toZoneId());
    }

    public CopiedReport copyReport(String requestedSiteId, long reportId) {
        ReportDef report = reportDefinition(requestedSiteId, reportId);
        SiteStatsReportForm form = SiteStatsReportForm.from(report, userTimeService.getLocalTimeZone().toZoneId());
        form.setId(0);
        return new CopiedReport(saveReport(report.getSiteId(), form), report.getSiteId());
    }

    public boolean canViewPreview(String requestedSiteId, String previewId) {
        return reportExportService.canExportPreviewReport(reportSite(requestedSiteId), previewId);
    }

    public ReportDef buildReport(String requestedSiteId, SiteStatsReportForm form) {
        String siteId = reportSite(requestedSiteId);
        String validationCode = reportFormValidator.validateForSite(siteId, form);
        if (validationCode != null) {
            throw new InvalidReportConfigurationException(validationCode);
        }
        ReportDef report;
        if (form.getId() > 0) {
            report = editableReportDefinition(siteId, form.getId());
        } else {
            report = new ReportDef(null, siteId);
            report.setId(0);
        }
        report.setSiteId(siteId);
        report.setTitle(StringUtils.trim(form.getTitle()));
        report.setDescription(StringUtils.trimToEmpty(form.getDescription()));
        report.setHidden(false);
        applyReportParameters(report.getReportParams(), form);
        return report;
    }

    public ReportEditorOptions reportEditorOptions(String requestedSiteId) {
        return reportEditorOptions(requestedSiteId, Collections.emptyList());
    }

    public ReportEditorOptions reportEditorOptions(String requestedSiteId, Collection<String> selectedUserIds) {
        String siteId = reportSite(requestedSiteId);
        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Unknown site", e);
        }

        List<String> availableReportTypes = reportFormValidator.availableReportTypes(site);
        boolean visitsAvailable = availableReportTypes.contains(ReportManager.WHAT_VISITS);
        boolean activityAvailable = availableReportTypes.contains(ReportManager.WHAT_EVENTS);
        boolean resourcesAvailable = availableReportTypes.contains(ReportManager.WHAT_RESOURCES);
        boolean presencesAvailable = availableReportTypes.contains(ReportManager.WHAT_PRESENCES);

        PrefsData preferences = statsManager.getPreferences(siteId, true);
        List<NamedOption> tools = new ArrayList<NamedOption>();
        List<NamedOption> events = new ArrayList<NamedOption>();
        for (ToolInfo tool : eventRegistryService.getEventRegistry(
                siteId, preferences.isListToolEventsOnlyAvailableInSite())) {
            if (!siteStatsToolEventsService.isToolSupported(siteId, tool, preferences)) {
                continue;
            }
            tools.add(new NamedOption(tool.getToolId(), eventRegistryService.getToolName(tool.getToolId())));
            for (EventInfo event : tool.getEvents()) {
                events.add(new NamedOption(event.getEventId(), eventRegistryService.getEventName(event.getEventId())));
            }
        }
        Comparator<NamedOption> byLabel = Comparator.comparing(NamedOption::getLabel, String.CASE_INSENSITIVE_ORDER);
        tools.sort(byLabel);
        events.sort(byLabel);

        List<NamedOption> groups = site.getGroups().stream()
                .map(group -> new NamedOption(group.getId(), group.getTitle()))
                .sorted(byLabel).collect(Collectors.toList());
        List<NamedOption> users = userDirectoryService.getUsers(selectedUserIds).stream()
                .filter(user -> isActiveSiteMember(site, user.getId()))
                .map(user -> new NamedOption(user.getId(), displayName(user)))
                .sorted(byLabel).collect(Collectors.toList());
        Set<String> roleIds = new HashSet<String>();
        try {
            authzGroupService.getAuthzGroup(siteService.siteReference(siteId)).getRoles()
                    .forEach(role -> roleIds.add(role.getId()));
        } catch (GroupNotDefinedException e) {
            throw new IllegalArgumentException("The site authorization group is unavailable", e);
        }
        List<NamedOption> roles = roleIds.stream().map(role -> new NamedOption(role, role))
                .sorted(byLabel).collect(Collectors.toList());
        return new ReportEditorOptions(tools, events, roles, groups, users, availableReportTypes,
                new ReportEditorRulesView(),
                visitsAvailable, activityAvailable, resourcesAvailable, presencesAvailable);
    }

    public List<NamedOption> searchReportUsers(String requestedSiteId, String query) {
        String siteId = reportSite(requestedSiteId);
        String normalizedQuery = StringUtils.trimToEmpty(query);
        if (normalizedQuery.length() < 2) {
            return Collections.emptyList();
        }
        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Unknown site", e);
        }
        List<User> candidates = new ArrayList<User>();
        try {
            User exactMatch = userDirectoryService.getUserByEid(normalizedQuery);
            if (exactMatch != null) {
                candidates.add(exactMatch);
            }
        } catch (UserNotDefinedException e) {
            // Continue with the bounded directory search.
        }
        candidates.addAll(userDirectoryService.searchUsers(
                normalizedQuery, 1, USER_SEARCH_CANDIDATE_LIMIT));
        Set<String> seenUserIds = new HashSet<String>();
        return candidates.stream()
                .filter(user -> seenUserIds.add(user.getId()))
                .filter(user -> isActiveSiteMember(site, user.getId()))
                .map(user -> new NamedOption(user.getId(), displayName(user)))
                .sorted(Comparator.comparing(NamedOption::getLabel, String.CASE_INSENSITIVE_ORDER))
                .limit(USER_SEARCH_RESULT_LIMIT)
                .collect(Collectors.toList());
    }

    public void prepareReportForm(SiteStatsReportForm form, ReportEditorOptions options) {
        if (!options.getAvailableReportTypes().contains(form.getWhat())
                && !options.getAvailableReportTypes().isEmpty()) {
            form.setWhat(options.getAvailableReportTypes().get(0));
        }
    }

    public String validateReport(String requestedSiteId, SiteStatsReportForm form) {
        return reportFormValidator.validateForSite(reportSite(requestedSiteId), form);
    }

    private void applyReportParameters(ReportParams params, SiteStatsReportForm form) {
        params.setWhat(form.getWhat());
        params.setWhatEventSelType(form.getWhatEventSelType());
        params.setWhatToolIds(new ArrayList<String>(form.getWhatToolIds()));
        params.setWhatEventIds(new ArrayList<String>(form.getWhatEventIds()));
        params.setWhatLimitedAction(form.isWhatLimitedAction());
        params.setWhatLimitedResourceIds(form.isWhatLimitedResourceIds());
        params.setWhatResourceAction(form.getWhatResourceAction());
        params.setWhatResourceIds(Arrays.stream(StringUtils.defaultString(form.getWhatResourceIds()).split("[\\r\\n]+"))
                .map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        params.setWhen(form.getWhen());
        ZoneId zoneId = userTimeService.getLocalTimeZone().toZoneId();
        if (form.getWhenFrom() != null) {
            params.setWhenFrom(Date.from(form.getWhenFrom().atStartOfDay(zoneId).toInstant()));
        }
        if (form.getWhenTo() != null) {
            params.setWhenTo(Date.from(form.getWhenTo().plusDays(1).atStartOfDay(zoneId).minusNanos(1).toInstant()));
        }
        params.setWho(form.getWho());
        params.setWhoRoleId(form.getWhoRoleId());
        params.setWhoGroupId(form.getWhoGroupId());
        params.setWhoUserIds(new ArrayList<String>(form.getWhoUserIds()));
        params.setHowTotalsBy(new ArrayList<String>(form.getHowTotalsBy()));
        params.setHowSort(form.isHowSort());
        params.setHowSortBy(form.getHowSortBy());
        params.setHowSortAscending(form.isHowSortAscending());
        params.setHowLimitedMaxResults(form.isHowLimitedMaxResults());
        params.setHowMaxResults(form.getHowMaxResults());
        params.setHowPresentationMode(form.getHowPresentationMode());
        params.setHowChartType(form.getHowChartType());
        params.setHowChartSource(form.getHowChartSource());
        params.setHowChartCategorySource(form.getHowChartCategorySource());
        params.setHowChartSeriesSource(form.getHowChartSeriesSource());
        params.setHowChartSeriesPeriod(form.getHowChartSeriesPeriod());
    }

    public long saveReport(String requestedSiteId, SiteStatsReportForm form) {
        ReportDef report = buildReport(requestedSiteId, form);
        if (!reportManager.saveReportDefinition(report)) {
            throw new SiteStatsOperationException(
                    "sitestats_error_report_save",
                    "Report " + report.getId() + " could not be saved for site " + report.getSiteId());
        }
        return report.getId();
    }

    public String previewReport(String requestedSiteId, SiteStatsReportForm form) {
        ReportDef report = buildReport(requestedSiteId, form);
        return reportPreviewService.register(report.getSiteId(), currentUserId(), report);
    }

    public void deleteReport(String requestedSiteId, long reportId) {
        ReportDef report = editableReportDefinition(requestedSiteId, reportId);
        if (!reportManager.removeReportDefinition(report)) {
            throw new SiteStatsOperationException(
                    "sitestats_error_report_delete",
                    "Report " + reportId + " could not be deleted for site " + report.getSiteId());
        }
    }

    public PreferencesResult preferences(String requestedSiteId) {
        String siteId = viewSite(requestedSiteId);
        PrefsData preferences = statsManager.getPreferences(siteId, true);
        PreferencesForm form = new PreferencesForm();
        form.setListToolEventsOnlyAvailableInSite(preferences.isListToolEventsOnlyAvailableInSite());
        form.setShowOwnStatisticsToStudents(preferences.isShowOwnStatisticsToStudents());
        form.setUseAllTools(preferences.isUseAllTools());
        form.setItemLabelsVisible(preferences.isItemLabelsVisible());
        form.setChartTransparency(preferences.getChartTransparency());
        form.setSelectedEventIds(new ArrayList<String>(preferences.getToolEventsStringList()));
        return new PreferencesResult(siteId, form, activityDefinitionTools(preferences));
    }

    public List<ActivityDefinitionTool> activityDefinitionTools(PrefsData preferences) {
        return preferences.getToolEventsDef().stream()
                .map(tool -> new ActivityDefinitionTool(tool.getToolId(), toolName(tool.getToolId()),
                        tool.getEvents().stream()
                                .map(event -> new NamedOption(event.getEventId(), eventName(event.getEventId())))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public void savePreferences(String requestedSiteId, PreferencesForm form) {
        String siteId = reportSite(requestedSiteId);
        PrefsData preferences = statsManager.getPreferences(siteId, true);
        preferences.setListToolEventsOnlyAvailableInSite(form.isListToolEventsOnlyAvailableInSite());
        preferences.setShowOwnStatisticsToStudents(form.isShowOwnStatisticsToStudents());
        preferences.setUseAllTools(form.isUseAllTools());
        preferences.setItemLabelsVisible(form.isItemLabelsVisible());
        preferences.setChartTransparency(form.getChartTransparency());
        Set<String> selectedEvents = new HashSet<String>(form.getSelectedEventIds());
        for (ToolInfo tool : preferences.getToolEventsDef()) {
            boolean toolSelected = false;
            for (EventInfo event : tool.getEvents()) {
                boolean eventSelected = selectedEvents.contains(event.getEventId());
                event.setSelected(eventSelected);
                toolSelected = toolSelected || eventSelected;
            }
            tool.setSelected(toolSelected);
        }
        if (!statsManager.setPreferences(siteId, preferences)) {
            throw new SiteStatsOperationException(
                    "sitestats_error_preferences_save",
                    "Preferences could not be saved for site " + siteId);
        }
    }

    public UserActivityResult userActivity(String requestedSiteId, UserActivityForm form) {
        String siteId = viewSite(requestedSiteId);
        assertCanViewUserActivity(siteId);
        ZoneId zoneId = userTimeService.getLocalTimeZone().toZoneId();
        LocalDate today = LocalDate.now(zoneId);
        if (form.getStartDate() == null) {
            form.setStartDate(today);
        }
        if (form.getEndDate() == null) {
            form.setEndDate(today);
        }
        if (StringUtils.isBlank(form.getUserId())) {
            form.setUserId(ReportManager.WHO_NONE);
        }
        List<UserOption> users = detailedEventsManager.getUsersForTracking(siteId).stream()
                .map(userId -> new UserOption(userId, displayName(userId)))
                .sorted(Comparator.comparing(UserOption::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        List<NamedOption> tools = new ArrayList<NamedOption>();
        tools.add(new NamedOption(ReportManager.WHAT_EVENTS_ALLTOOLS,
                toolName(ReportManager.WHAT_EVENTS_ALLTOOLS)));
        PrefsData preferences = statsManager.getPreferences(siteId, false);
        siteStatsToolEventsService.getToolIds(siteId, preferences).stream()
                .map(toolId -> new NamedOption(toolId, toolName(toolId)))
                .forEach(tools::add);
        List<ActivityEvent> events = Collections.emptyList();
        long total = 0;
        if (StringUtils.isNotBlank(form.getUserId()) && !ReportManager.WHO_NONE.equals(form.getUserId())) {
            Instant start = form.getStartDate().atStartOfDay(zoneId).toInstant();
            Instant end = form.getEndDate().plusDays(1).atStartOfDay(zoneId).toInstant();
            TrackingParams tracking = new TrackingParams(siteId,
                    siteStatsToolEventsService.getEventsForToolFilter(
                            form.getToolId(), siteId, preferences, true),
                    Collections.singletonList(form.getUserId()), start, end);
            total = detailedEventsManager.getDetailedEventsCount(tracking);
            long offset = (long) Math.max(0, form.getPage() - 1) * PAGE_SIZE;
            events = detailedEventsManager.getDetailedEvents(tracking,
                    new PagingParams(offset, PAGE_SIZE), new SortingParams("eventDate", true)).stream()
                    .map(event -> new ActivityEvent(event.getId(), eventName(event.getEventId()), event.getEventRef(),
                            event.getEventDate().toInstant().toString(), formatTimestamp(event.getEventDate()),
                            detailedEventsManager.isResolvable(event.getEventId())))
                    .collect(Collectors.toList());
            statsManager.logEvent(new UserId(form.getUserId()), StatsManager.LOG_ACTION_TRACK, siteId, false);
        }
        return new UserActivityResult(siteId, users, tools, events, total, PAGE_SIZE);
    }

    public EventDetailsResult eventDetails(String requestedSiteId, long eventId) {
        String siteId = viewSite(requestedSiteId);
        assertCanViewUserActivity(siteId);
        DetailedEvent event = detailedEventsManager.getDetailedEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown event"));
        if (!siteId.equals(event.getSiteId())) {
            throw new SecurityException("Not authorized for the requested event");
        }
        List<EventDetail> details = resolvedRefTransformer.transform(
                detailedEventsManager.resolveEventReference(
                        event.getEventId(), event.getEventRef(), event.getSiteId()), siteId);
        return new EventDetailsResult(siteId, event, details);
    }

    public boolean canViewUserActivity(String siteId) {
        return authorizationService.canViewUserActivity(siteId);
    }

    public boolean canViewAllSiteStats(String siteId) {
        return authorizationService.canViewAllSiteStats(siteId);
    }

    private void assertCanViewUserActivity(String siteId) {
        if (!canViewUserActivity(siteId)) {
            throw new SecurityException("User Activity is not available");
        }
    }

    public List<Site> adminSites(String search, String type, int page) {
        adminSite(currentSiteId());
        Object siteType = StringUtils.isBlank(type) || "all".equals(type) ? null : type;
        int first = (Math.max(1, page) - 1) * PAGE_SIZE + 1;
        PagingPosition paging = new PagingPosition(first, first + PAGE_SIZE - 1);
        return siteService.getSites(SiteService.SelectionType.NON_USER, siteType,
                StringUtils.trimToNull(search), null, SiteService.SortType.TITLE_ASC, paging);
    }

    public List<String> siteTypes() {
        adminSite(currentSiteId());
        return siteService.getSiteTypes();
    }

    public LastJobRunResult latestJobRun() {
        adminSite(currentSiteId());
        try {
            JobRun jobRun = statsUpdateManager.getLatestJobRun();
            if (jobRun == null || jobRun.getJobEndDate() == null) {
                return null;
            }
            return new LastJobRunResult(jobRun.getJobEndDate().toInstant().toString(),
                    formatTimestamp(jobRun.getJobEndDate()));
        } catch (Exception e) {
            log.warn("Unable to retrieve the latest SiteStats job run", e);
            return null;
        }
    }

    public String viewSite(String requestedSiteId) {
        return authorizationService.viewSite(requestedSiteId);
    }

    public String reportSite(String requestedSiteId) {
        return authorizationService.reportSite(requestedSiteId);
    }

    public String adminSite(String requestedSiteId) {
        return authorizationService.adminSite(requestedSiteId);
    }

    private String displayName(String userId) {
        try {
            User user = userDirectoryService.getUser(userId);
            return displayName(user);
        } catch (UserNotDefinedException e) {
            log.debug("User {} was not found while resolving a SiteStats display name", userId);
            return userId;
        } catch (Exception e) {
            log.warn("Unable to resolve the SiteStats display name for user {}", userId, e);
            return userId;
        }
    }

    private String displayName(User user) {
        return StringUtils.defaultIfBlank(statsManager.getUserNameForDisplay(user),
                StringUtils.defaultIfBlank(user.getDisplayName(), user.getId()));
    }

    private boolean isActiveSiteMember(Site site, String userId) {
        Member member = site.getMember(userId);
        return member != null && member.isActive();
    }

    private String toolName(String toolId) {
        return StringUtils.defaultIfBlank(eventRegistryService.getToolName(toolId), toolId);
    }

    private String eventName(String eventId) {
        return StringUtils.defaultIfBlank(eventRegistryService.getEventName(eventId), eventId);
    }

    private String formatTimestamp(Date date) {
        return userTimeService.shortLocalizedTimestamp(date.toInstant(), userTimeService.getLocalTimeZone(),
                localeService.getLocaleForCurrentSiteAndUser());
    }

    @Getter
    @RequiredArgsConstructor
    public static class NamedOption {
        private final String id;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    public static class OverviewResult {
        private final SiteStatsOverview overview;
        private final Map<String, String> widgetEndpoints;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ActivityDefinitionTool {
        private final String id;
        private final String label;
        private final List<NamedOption> events;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ReportEditorOptions {
        private final List<NamedOption> tools;
        private final List<NamedOption> events;
        private final List<NamedOption> roles;
        private final List<NamedOption> groups;
        private final List<NamedOption> users;
        private final List<String> availableReportTypes;
        private final ReportEditorRulesView rules;
        private final boolean visitsAvailable;
        private final boolean activityAvailable;
        private final boolean resourcesAvailable;
        private final boolean presencesAvailable;
    }

    @Getter
    @RequiredArgsConstructor
    public static class CopiedReport {
        private final long reportId;
        private final String siteId;
    }

    @Getter
    @RequiredArgsConstructor
    public static class UserOption {
        private final String id;
        private final String displayName;
    }

    @Getter
    @RequiredArgsConstructor
    public static class UserActivityResult {
        private final String siteId;
        private final List<UserOption> users;
        private final List<NamedOption> tools;
        private final List<ActivityEvent> events;
        private final long total;
        private final int pageSize;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ActivityEvent {
        private final long id;
        private final String label;
        private final String reference;
        private final String timestamp;
        private final String displayTimestamp;
        private final boolean resolvable;
    }

    @Getter
    @RequiredArgsConstructor
    public static class LastJobRunResult {
        private final String timestamp;
        private final String displayTimestamp;
    }

    @Getter
    @RequiredArgsConstructor
    public static class EventDetailsResult {
        private final String siteId;
        private final DetailedEvent event;
        private final List<EventDetail> details;
    }

    @Getter
    @RequiredArgsConstructor
    public static class PreferencesResult {
        private final String siteId;
        private final PreferencesForm form;
        private final List<ActivityDefinitionTool> tools;
    }

    @Getter
    public static class PreferencesForm {
        private boolean listToolEventsOnlyAvailableInSite;
        private boolean showOwnStatisticsToStudents;
        private boolean useAllTools;
        private boolean itemLabelsVisible;
        private float chartTransparency = 1.0f;
        private List<String> selectedEventIds = new ArrayList<String>();

        public void setListToolEventsOnlyAvailableInSite(boolean value) { this.listToolEventsOnlyAvailableInSite = value; }
        public void setShowOwnStatisticsToStudents(boolean value) { this.showOwnStatisticsToStudents = value; }
        public void setUseAllTools(boolean value) { this.useAllTools = value; }
        public void setItemLabelsVisible(boolean value) { this.itemLabelsVisible = value; }
        public void setChartTransparency(float value) { this.chartTransparency = value; }
        public void setSelectedEventIds(List<String> value) {
            this.selectedEventIds = value == null ? new ArrayList<String>() : value;
        }
    }

    @Getter
    public static class UserActivityForm {
        private String userId = ReportManager.WHO_NONE;
        private String toolId = ReportManager.WHAT_EVENTS_ALLTOOLS;
        private LocalDate startDate;
        private LocalDate endDate;
        private int page = 1;

        public void setUserId(String userId) { this.userId = userId; }
        public void setToolId(String toolId) { this.toolId = toolId; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public void setPage(int page) { this.page = page; }
    }
}
