package org.sakaiproject.sitestats.tool.mvc;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ReportForm;
import org.springframework.stereotype.Service;

@Service
public class SiteStatsReportFormValidator {

    public String validate(ReportForm form) {
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
}
