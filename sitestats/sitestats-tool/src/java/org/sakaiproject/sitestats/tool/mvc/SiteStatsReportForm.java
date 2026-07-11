package org.sakaiproject.sitestats.tool.mvc;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;

@Getter
@Setter
public class SiteStatsReportForm {

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

    public static SiteStatsReportForm from(ReportDef report, ZoneId zoneId) {
        SiteStatsReportForm form = new SiteStatsReportForm();
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
