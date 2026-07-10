package org.sakaiproject.sitestats.tool.mvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.JobRun;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.UserId;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.PagingParams;
import org.sakaiproject.sitestats.api.event.detailed.SortingParams;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportSummary;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.transformers.ResolvedRefTransformer;
import org.sakaiproject.user.api.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteStatsToolService {

    private static final int PAGE_SIZE = 50;

    private final SakaiFacade facade;

    public String currentSiteId() {
        return facade.getToolManager().getCurrentPlacement().getContext();
    }

    public String currentUserId() {
        return facade.getSiteStatsReportAccessService().currentUserId();
    }

    public boolean isAdminTool() {
        return StatsManager.SITESTATS_ADMIN_TOOLID.equals(facade.getToolManager().getCurrentTool().getId());
    }

    public SiteStatsOverview overview(String requestedSiteId) {
        String siteId = viewSite(requestedSiteId);
        SiteStatsOverview overview = facade.getSiteStatsViewService().getOverview(siteId);
        facade.getStatsManager().logEvent(null, StatsManager.LOG_ACTION_VIEW, siteId, true);
        return overview;
    }

    public List<SiteStatsReportSummary> reports(String requestedSiteId) {
        return facade.getSiteStatsViewService().getReports(reportSite(requestedSiteId));
    }

    public ReportDef reportDefinition(String requestedSiteId, long reportId) {
        String siteId = reportSite(requestedSiteId);
        ReportDef report = facade.getSiteStatsReportAccessService().persistedReportDefinition(siteId, reportId);
        return new ReportDef(report, siteId);
    }

    public ReportDef editableReportDefinition(String requestedSiteId, long reportId) {
        String siteId = reportSite(requestedSiteId);
        ReportDef report = facade.getSiteStatsReportAccessService().persistedSiteReportDefinition(siteId, reportId);
        return new ReportDef(report, siteId);
    }

    public List<ReportDef> reportTemplates(String requestedSiteId) {
        reportSite(requestedSiteId);
        return facade.getReportManager().getReportDefinitions(null, true, false);
    }

    public ReportDef buildReport(String requestedSiteId, ReportForm form) {
        String siteId = reportSite(requestedSiteId);
        assertReportTypeAvailable(siteId, form);
        ReportDef report;
        if (form.getId() > 0) {
            report = editableReportDefinition(siteId, form.getId());
        } else if (form.getTemplateId() > 0) {
            report = reportDefinition(siteId, form.getTemplateId());
            report.setId(0);
            report.setCreatedBy(null);
            report.setCreatedOn(null);
        } else {
            List<ReportDef> templates = reportTemplates(siteId);
            report = templates.isEmpty() ? new ReportDef(null, siteId) : new ReportDef(templates.get(0), siteId);
            report.setId(0);
        }
        report.setSiteId(siteId);
        report.setTitle(StringUtils.trim(form.getTitle()));
        report.setDescription(StringUtils.trimToEmpty(form.getDescription()));
        report.setHidden(form.isHidden());
        applyReportParameters(report.getReportParams(), form);
        return report;
    }

    public ReportEditorOptions reportEditorOptions(String requestedSiteId) {
        String siteId = reportSite(requestedSiteId);
        Site site;
        try {
            site = facade.getSiteService().getSite(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Unknown site", e);
        }

        boolean visitsAvailable = facade.getStatsManager().getEnableSiteVisits()
                && facade.getStatsManager().getVisitsInfoAvailable();
        boolean activityAvailable = facade.getStatsManager().isEnableSiteActivity();
        boolean resourcesAvailable = facade.getStatsManager().isEnableResourceStats()
                && site.getToolForCommonId(StatsManager.RESOURCES_TOOLID) != null;
        boolean presencesAvailable = facade.getStatsManager().getEnableSitePresences();
        List<String> availableReportTypes = new ArrayList<String>();
        if (visitsAvailable) {
            availableReportTypes.add(ReportManager.WHAT_VISITS);
        }
        if (activityAvailable) {
            availableReportTypes.add(ReportManager.WHAT_EVENTS);
        }
        if (resourcesAvailable) {
            availableReportTypes.add(ReportManager.WHAT_RESOURCES);
        }
        if (presencesAvailable) {
            availableReportTypes.add(ReportManager.WHAT_PRESENCES);
        }

        PrefsData preferences = facade.getStatsManager().getPreferences(siteId, true);
        List<NamedOption> tools = new ArrayList<NamedOption>();
        List<NamedOption> events = new ArrayList<NamedOption>();
        for (ToolInfo tool : facade.getEventRegistryService().getEventRegistry(
                siteId, preferences.isListToolEventsOnlyAvailableInSite())) {
            if (!facade.getSiteStatsToolEventsService().isToolSupported(siteId, tool, preferences)) {
                continue;
            }
            tools.add(new NamedOption(tool.getToolId(), facade.getEventRegistryService().getToolName(tool.getToolId())));
            for (EventInfo event : tool.getEvents()) {
                events.add(new NamedOption(event.getEventId(), facade.getEventRegistryService().getEventName(event.getEventId())));
            }
        }
        Comparator<NamedOption> byLabel = Comparator.comparing(NamedOption::getLabel, String.CASE_INSENSITIVE_ORDER);
        tools.sort(byLabel);
        events.sort(byLabel);

        List<NamedOption> groups = site.getGroups().stream()
                .map(group -> new NamedOption(group.getId(), group.getTitle()))
                .sorted(byLabel).collect(Collectors.toList());
        List<NamedOption> users = site.getUsers().stream()
                .map(userId -> new NamedOption(userId, displayName(userId)))
                .sorted(byLabel).collect(Collectors.toList());
        Set<String> roleIds = new HashSet<String>();
        try {
            facade.getAuthzGroupService().getAuthzGroup(facade.getSiteService().siteReference(siteId)).getRoles()
                    .forEach(role -> roleIds.add(role.getId()));
        } catch (GroupNotDefinedException e) {
            throw new IllegalArgumentException("The site authorization group is unavailable", e);
        }
        List<NamedOption> roles = roleIds.stream().map(role -> new NamedOption(role, role))
                .sorted(byLabel).collect(Collectors.toList());
        return new ReportEditorOptions(tools, events, roles, groups, users, availableReportTypes,
                visitsAvailable, activityAvailable, resourcesAvailable, presencesAvailable);
    }

    public void prepareReportForm(ReportForm form, ReportEditorOptions options) {
        if (!options.getAvailableReportTypes().contains(form.getWhat())
                && !options.getAvailableReportTypes().isEmpty()) {
            form.setWhat(options.getAvailableReportTypes().get(0));
        }
    }

    public String validateReport(String requestedSiteId, ReportForm form) {
        String siteId = reportSite(requestedSiteId);
        if (!reportEditorOptions(siteId).getAvailableReportTypes().contains(form.getWhat())) {
            return "sitestats_report_type_unavailable";
        }
        Site site;
        try {
            site = facade.getSiteService().getSite(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Unknown site", e);
        }
        if (ReportManager.WHO_ROLE.equals(form.getWho())
                && (StringUtils.isBlank(form.getWhoRoleId())
                        || site.getUsersHasRole(form.getWhoRoleId()).isEmpty())) {
            return "report_err_emptyrole";
        }
        if (ReportManager.WHO_GROUPS.equals(form.getWho()) && StringUtils.isNotBlank(form.getWhoGroupId())) {
            Group group = site.getGroup(form.getWhoGroupId());
            if (group == null) {
                return "report_err_nogroup";
            }
            if (group.getUsers().isEmpty()) {
                return "report_err_emptygroup";
            }
        }
        return validateReport(form);
    }

    public String validateReport(ReportForm form) {
        if (StringUtils.isBlank(form.getTitle())) {
            return "sitestats_report_title_required";
        }
        if (ReportManager.WHAT_EVENTS.equals(form.getWhat())) {
            if (ReportManager.WHAT_EVENTS_BYTOOL.equals(form.getWhatEventSelType())
                    && form.getWhatToolIds().isEmpty()) {
                return "report_err_notools";
            }
            if (ReportManager.WHAT_EVENTS_BYEVENTS.equals(form.getWhatEventSelType())
                    && form.getWhatEventIds().isEmpty()) {
                return "report_err_noevents";
            }
        }
        if (ReportManager.WHAT_RESOURCES.equals(form.getWhat()) && form.isWhatLimitedResourceIds()
                && Arrays.stream(StringUtils.defaultString(form.getWhatResourceIds()).split("[\\r\\n]+"))
                        .noneMatch(StringUtils::isNotBlank)) {
            return "report_err_noresources";
        }
        if (ReportManager.WHEN_CUSTOM.equals(form.getWhen())
                && (form.getWhenFrom() == null || form.getWhenTo() == null || form.getWhenFrom().isAfter(form.getWhenTo()))) {
            return "report_err_nocustomdates";
        }
        if (ReportManager.WHO_GROUPS.equals(form.getWho()) && StringUtils.isBlank(form.getWhoGroupId())) {
            return "report_err_nogroup";
        }
        if (ReportManager.WHO_CUSTOM.equals(form.getWho()) && form.getWhoUserIds().isEmpty()) {
            return "report_err_nousers";
        }
        if (form.getHowTotalsBy().isEmpty()) {
            return "reportParams.howTotalsBy.Required";
        }
        if (ReportManager.WHAT_EVENTS.equals(form.getWhat())
                && (form.getHowTotalsBy().contains(StatsManager.T_RESOURCE)
                        || form.getHowTotalsBy().contains(StatsManager.T_RESOURCE_ACTION))) {
            return "report_err_totalsbyevent";
        }
        if (ReportManager.WHAT_RESOURCES.equals(form.getWhat())
                && form.getHowTotalsBy().contains(StatsManager.T_EVENT)) {
            return "report_err_totalsbyresource";
        }
        if (form.isHowLimitedMaxResults() && form.getHowMaxResults() <= 0) {
            return "reportParams.howMaxResults.IConverter.int";
        }
        return null;
    }

    private void applyReportParameters(ReportParams params, ReportForm form) {
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
        ZoneId zoneId = facade.getUserTimeService().getLocalTimeZone().toZoneId();
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
        boolean validSort = !form.isHowSort() || ReportManager.HOW_SORT_DEFAULT.equals(form.getHowSortBy())
                || StatsManager.T_TOTAL.equals(form.getHowSortBy())
                || form.getHowTotalsBy().contains(form.getHowSortBy());
        params.setHowSort(form.isHowSort() && validSort);
        params.setHowSortBy(validSort ? form.getHowSortBy() : ReportManager.HOW_SORT_DEFAULT);
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

    private void assertReportTypeAvailable(String siteId, ReportForm form) {
        if (!reportEditorOptions(siteId).getAvailableReportTypes().contains(form.getWhat())) {
            throw new IllegalArgumentException("The selected report type is unavailable for this site");
        }
    }

    public long saveReport(String requestedSiteId, ReportForm form) {
        ReportDef report = buildReport(requestedSiteId, form);
        if (!facade.getReportManager().saveReportDefinition(report)) {
            throw new IllegalStateException("The report could not be saved");
        }
        return report.getId();
    }

    public String previewReport(String requestedSiteId, ReportForm form) {
        ReportDef report = buildReport(requestedSiteId, form);
        return facade.getSiteStatsReportPreviewService().register(report.getSiteId(), currentUserId(), report);
    }

    public void deleteReport(String requestedSiteId, long reportId) {
        ReportDef report = editableReportDefinition(requestedSiteId, reportId);
        if (!facade.getReportManager().removeReportDefinition(report)) {
            throw new IllegalStateException("The report could not be deleted");
        }
    }

    public PrefsData preferences(String requestedSiteId) {
        return facade.getStatsManager().getPreferences(viewSite(requestedSiteId), true);
    }

    public void savePreferences(String requestedSiteId, PreferencesForm form) {
        String siteId = viewSite(requestedSiteId);
        PrefsData preferences = facade.getStatsManager().getPreferences(siteId, true);
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
        if (!facade.getStatsManager().setPreferences(siteId, preferences)) {
            throw new IllegalStateException("The preferences could not be saved");
        }
    }

    public UserActivityResult userActivity(String requestedSiteId, UserActivityForm form) {
        String siteId = viewSite(requestedSiteId);
        List<UserOption> users = facade.getDetailedEventsManager().getUsersForTracking(siteId).stream()
                .map(userId -> new UserOption(userId, displayName(userId)))
                .sorted(Comparator.comparing(UserOption::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        List<String> tools = new ArrayList<String>();
        tools.add(ReportManager.WHAT_EVENTS_ALLTOOLS);
        PrefsData preferences = facade.getStatsManager().getPreferences(siteId, false);
        tools.addAll(facade.getSiteStatsToolEventsService().getToolIds(siteId, preferences));
        List<DetailedEvent> events = Collections.emptyList();
        long total = 0;
        if (StringUtils.isNotBlank(form.getUserId()) && !ReportManager.WHO_NONE.equals(form.getUserId())) {
            ZoneId zoneId = facade.getUserTimeService().getLocalTimeZone().toZoneId();
            Instant start = form.getStartDate().atStartOfDay(zoneId).toInstant();
            Instant end = form.getEndDate().plusDays(1).atStartOfDay(zoneId).toInstant();
            TrackingParams tracking = new TrackingParams(siteId,
                    facade.getSiteStatsToolEventsService().getEventsForToolFilter(
                            form.getToolId(), siteId, preferences, true),
                    Collections.singletonList(form.getUserId()), start, end);
            total = facade.getDetailedEventsManager().getDetailedEventsCount(tracking);
            long offset = (long) Math.max(0, form.getPage() - 1) * PAGE_SIZE;
            events = facade.getDetailedEventsManager().getDetailedEvents(tracking,
                    new PagingParams(offset, PAGE_SIZE), new SortingParams("eventDate", true));
            facade.getStatsManager().logEvent(new UserId(form.getUserId()), StatsManager.LOG_ACTION_TRACK, siteId, false);
        }
        return new UserActivityResult(siteId, users, tools, events, total, PAGE_SIZE);
    }

    public EventDetailsResult eventDetails(String requestedSiteId, long eventId) {
        String siteId = viewSite(requestedSiteId);
        DetailedEvent event = facade.getDetailedEventsManager().getDetailedEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown event"));
        if (!siteId.equals(event.getSiteId())) {
            throw new SecurityException("Not authorized for the requested event");
        }
        List<EventDetail> details = ResolvedRefTransformer.transform(
                facade.getDetailedEventsManager().resolveEventReference(
                        event.getEventId(), event.getEventRef(), event.getSiteId()));
        return new EventDetailsResult(siteId, event, details);
    }

    public List<Site> adminSites(String search, String type, int page) {
        adminSite(currentSiteId());
        Object siteType = StringUtils.isBlank(type) || "all".equals(type) ? null : type;
        int first = (Math.max(1, page) - 1) * PAGE_SIZE + 1;
        PagingPosition paging = new PagingPosition(first, first + PAGE_SIZE - 1);
        return facade.getSiteService().getSites(SiteService.SelectionType.NON_USER, siteType,
                StringUtils.trimToNull(search), null, SiteService.SortType.TITLE_ASC, paging);
    }

    public List<String> siteTypes() {
        adminSite(currentSiteId());
        return facade.getSiteService().getSiteTypes();
    }

    public JobRun latestJobRun() {
        adminSite(currentSiteId());
        try {
            return facade.getStatsUpdateManager().getLatestJobRun();
        } catch (Exception e) {
            return null;
        }
    }

    public String viewSite(String requestedSiteId) {
        return authorizedSite(requestedSiteId, AccessLevel.VIEW);
    }

    public String reportSite(String requestedSiteId) {
        return authorizedSite(requestedSiteId, AccessLevel.ALL);
    }

    public String adminSite(String requestedSiteId) {
        return authorizedSite(requestedSiteId, AccessLevel.ADMIN);
    }

    private String authorizedSite(String requestedSiteId, AccessLevel accessLevel) {
        String siteId = StringUtils.defaultIfBlank(requestedSiteId, currentSiteId());
        String currentSiteId = currentSiteId();
        if (!siteId.equals(currentSiteId)) {
            facade.getSiteStatsReportAccessService().assertCanViewAdmin(currentSiteId);
        }
        if (AccessLevel.ADMIN == accessLevel) {
            facade.getSiteStatsReportAccessService().assertCanViewAdmin(siteId);
        } else if (AccessLevel.ALL == accessLevel) {
            facade.getSiteStatsReportAccessService().assertCanViewAll(siteId);
        } else {
            facade.getSiteStatsReportAccessService().assertCanView(siteId);
        }
        return siteId;
    }

    private enum AccessLevel {
        VIEW,
        ALL,
        ADMIN
    }

    private String displayName(String userId) {
        try {
            User user = facade.getUserDirectoryService().getUser(userId);
            return facade.getStatsManager().getUserNameForDisplay(user);
        } catch (Exception e) {
            return userId;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class NamedOption {
        private final String id;
        private final String label;
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
        private final boolean visitsAvailable;
        private final boolean activityAvailable;
        private final boolean resourcesAvailable;
        private final boolean presencesAvailable;
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
        private final List<String> tools;
        private final List<DetailedEvent> events;
        private final long total;
        private final int pageSize;
    }

    @Getter
    @RequiredArgsConstructor
    public static class EventDetailsResult {
        private final String siteId;
        private final DetailedEvent event;
        private final List<EventDetail> details;
    }

    @Getter
    @Setter
    public static class ReportForm {
        private long id;
        private long templateId;
        private String title;
        private String description;
        private boolean hidden;
        private String what = ReportManager.WHAT_VISITS;
        private String whatEventSelType = ReportManager.WHAT_EVENTS_BYTOOL;
        private List<String> whatToolIds = new ArrayList<String>(Collections.singletonList(ReportManager.WHAT_EVENTS_ALLTOOLS));
        private List<String> whatEventIds = new ArrayList<String>();
        private boolean whatLimitedAction;
        private boolean whatLimitedResourceIds;
        private String whatResourceAction = ReportManager.WHAT_RESOURCES_ACTION_NEW;
        private String whatResourceIds;
        private String when = ReportManager.WHEN_LAST7DAYS;
        private LocalDate whenFrom = LocalDate.now().minusDays(7);
        private LocalDate whenTo = LocalDate.now();
        private String who = ReportManager.WHO_ALL;
        private String whoRoleId;
        private String whoGroupId;
        private List<String> whoUserIds = new ArrayList<String>();
        private List<String> howTotalsBy = new ArrayList<String>(StatsManager.TOTALSBY_EVENT_DEFAULT);
        private boolean howSort;
        private String howSortBy = ReportManager.HOW_SORT_DEFAULT;
        private boolean howSortAscending = true;
        private boolean howLimitedMaxResults;
        private int howMaxResults;
        private String howPresentationMode = ReportManager.HOW_PRESENTATION_TABLE;
        private String howChartType = StatsManager.CHARTTYPE_BAR;
        private String howChartSource = StatsManager.T_EVENT;
        private String howChartCategorySource = StatsManager.T_NONE;
        private String howChartSeriesSource = StatsManager.T_TOTAL;
        private String howChartSeriesPeriod = StatsManager.CHARTTIMESERIES_DAY;

        public static ReportForm from(ReportDef report, ZoneId zoneId) {
            ReportForm form = new ReportForm();
            form.id = report.getId();
            form.title = report.getTitle();
            form.description = report.getDescription();
            form.hidden = report.isHidden();
            ReportParams params = report.getReportParams();
            form.what = params.getWhat();
            form.whatEventSelType = params.getWhatEventSelType();
            form.whatToolIds = new ArrayList<String>(params.getWhatToolIds());
            form.whatEventIds = new ArrayList<String>(params.getWhatEventIds());
            form.whatLimitedAction = params.isWhatLimitedAction();
            form.whatLimitedResourceIds = params.isWhatLimitedResourceIds();
            form.whatResourceAction = params.getWhatResourceAction();
            form.whatResourceIds = String.join("\n", params.getWhatResourceIds());
            form.when = params.getWhen();
            form.whenFrom = params.getWhenFrom() == null ? null
                    : params.getWhenFrom().toInstant().atZone(zoneId).toLocalDate();
            form.whenTo = params.getWhenTo() == null ? null
                    : params.getWhenTo().toInstant().atZone(zoneId).toLocalDate();
            form.who = params.getWho();
            form.whoRoleId = params.getWhoRoleId();
            form.whoGroupId = params.getWhoGroupId();
            form.whoUserIds = new ArrayList<String>(params.getWhoUserIds());
            form.howTotalsBy = new ArrayList<String>(params.getHowTotalsBy());
            form.howSort = params.isHowSort();
            form.howSortBy = params.getHowSortBy();
            form.howSortAscending = params.getHowSortAscending();
            form.howLimitedMaxResults = params.isHowLimitedMaxResults();
            form.howMaxResults = params.getHowMaxResults();
            form.howPresentationMode = params.getHowPresentationMode();
            form.howChartType = params.getHowChartType();
            form.howChartSource = params.getHowChartSource();
            form.howChartCategorySource = params.getHowChartCategorySource();
            form.howChartSeriesSource = params.getHowChartSeriesSource();
            form.howChartSeriesPeriod = params.getHowChartSeriesPeriod();
            return form;
        }
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
        private LocalDate startDate = LocalDate.now();
        private LocalDate endDate = LocalDate.now();
        private int page = 1;

        public void setUserId(String userId) { this.userId = userId; }
        public void setToolId(String toolId) { this.toolId = toolId; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public void setPage(int page) { this.page = page; }
    }
}
