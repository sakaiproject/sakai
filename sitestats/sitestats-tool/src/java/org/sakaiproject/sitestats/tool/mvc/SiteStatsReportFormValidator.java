package org.sakaiproject.sitestats.tool.mvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportConfigurationRules;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteStatsReportFormValidator {

    private final StatsManager statsManager;
    private final SiteService siteService;

    public String validateForSite(String siteId, SiteStatsReportForm form) {
        Site site = site(siteId);
        if (!availableReportTypes(site).contains(form.getWhat())) {
            return "sitestats_report_type_unavailable";
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
        return validateForm(form);
    }

    public List<String> availableReportTypes(Site site) {
        List<String> availableReportTypes = new ArrayList<String>();
        if (statsManager.getEnableSiteVisits() && statsManager.getVisitsInfoAvailable()) {
            availableReportTypes.add(ReportManager.WHAT_VISITS);
        }
        if (statsManager.isEnableSiteActivity()) {
            availableReportTypes.add(ReportManager.WHAT_EVENTS);
        }
        if (statsManager.isEnableResourceStats()
                && site.getToolForCommonId(StatsManager.RESOURCES_TOOLID) != null) {
            availableReportTypes.add(ReportManager.WHAT_RESOURCES);
        }
        if (statsManager.getEnableSitePresences()) {
            availableReportTypes.add(ReportManager.WHAT_PRESENCES);
        }
        return availableReportTypes;
    }

    public static String validateForm(SiteStatsReportForm form) {
        if (StringUtils.isBlank(form.getTitle())) {
            return "sitestats_report_title_required";
        }
        if (ReportManager.WHAT_EVENTS.equals(form.getWhat())) {
            if (!ReportConfigurationRules.isEventSelectionTypeAllowed(form.getWhatEventSelType())) {
                return "sitestats_report_configuration_invalid";
            }
            if (ReportManager.WHAT_EVENTS_BYTOOL.equals(form.getWhatEventSelType())
                    && form.getWhatToolIds().isEmpty()) {
                return "report_err_notools";
            }
            if (ReportManager.WHAT_EVENTS_BYEVENTS.equals(form.getWhatEventSelType())
                    && form.getWhatEventIds().isEmpty()) {
                return "report_err_noevents";
            }
        }
        if (ReportManager.WHAT_RESOURCES.equals(form.getWhat())
                && form.isWhatLimitedAction()
                && !ReportConfigurationRules.isResourceActionAllowed(form.getWhatResourceAction())) {
            return "sitestats_report_configuration_invalid";
        }
        if (ReportManager.WHAT_RESOURCES.equals(form.getWhat()) && form.isWhatLimitedResourceIds()
                && Arrays.stream(StringUtils.defaultString(form.getWhatResourceIds()).split("[\\r\\n]+"))
                        .noneMatch(StringUtils::isNotBlank)) {
            return "report_err_noresources";
        }
        if (!ReportConfigurationRules.isWhenTypeAllowed(form.getWhen())) {
            return "sitestats_report_configuration_invalid";
        }
        if (ReportManager.WHEN_CUSTOM.equals(form.getWhen())
                && (form.getWhenFrom() == null || form.getWhenTo() == null || form.getWhenFrom().isAfter(form.getWhenTo()))) {
            return "report_err_nocustomdates";
        }
        if (!ReportConfigurationRules.isWhoTypeAllowed(form.getWho())) {
            return "sitestats_report_configuration_invalid";
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
        if (form.getHowTotalsBy().stream()
                .anyMatch(total -> !ReportConfigurationRules.isTotalAllowed(form.getWhat(), total))) {
            return "sitestats_report_totals_unavailable";
        }
        if (form.isHowSort()
                && !ReportConfigurationRules.isSortSourceAllowed(
                        form.getHowTotalsBy(), form.getHowSortBy())) {
            return "sitestats_report_configuration_invalid";
        }
        if (form.isHowLimitedMaxResults() && form.getHowMaxResults() <= 0) {
            return "reportParams.howMaxResults.IConverter.int";
        }
        if (!ReportConfigurationRules.isPresentationModeAllowed(form.getHowPresentationMode())) {
            return "sitestats_report_configuration_invalid";
        }
        if (hasChart(form) && !isValidChart(form)) {
            return "sitestats_report_configuration_invalid";
        }
        return null;
    }

    private static boolean hasChart(SiteStatsReportForm form) {
        return ReportManager.HOW_PRESENTATION_CHART.equals(form.getHowPresentationMode())
                || ReportManager.HOW_PRESENTATION_BOTH.equals(form.getHowPresentationMode());
    }

    private static boolean isValidChart(SiteStatsReportForm form) {
        return ReportConfigurationRules.isChartConfigurationAllowed(
                form.getHowChartType(), form.getHowChartSource(),
                form.getHowChartCategorySource(), form.getHowChartSeriesSource(),
                form.getHowChartSeriesPeriod(), form.getHowTotalsBy());
    }

    private Site site(String siteId) {
        try {
            return siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Unknown site", e);
        }
    }
}
