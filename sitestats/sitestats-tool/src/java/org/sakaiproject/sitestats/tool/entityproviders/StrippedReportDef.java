package org.sakaiproject.sitestats.tool.entityproviders;

import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportParams;

import java.util.List;

import lombok.Getter;

/**
 * Wraps a ReportDef for the purpose of providing a less verbose JSON feed. Key
 * fields from the ReportParams member are provided at the top level, again to
 * make JSON parsing less complex.
 *
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
@Getter
public class StrippedReportDef {

	private long id;
	private String siteId;
	private String title;
	private String description;
    private long from = 0L;
    private long to = 0L;
    private List<String> toolIds;
    private List<String> eventIds;
    private List<String> resourceIds;
    private String resourceAction;
    private String who;
    private List<String> userIds;

    public StrippedReportDef() {
        super();
    }

    public StrippedReportDef(ReportDef reportDef) {

        super();

        this.id = reportDef.getId();
        this.siteId = reportDef.getSiteId();
        this.title = reportDef.getTitle();
        this.description = reportDef.getDescription();
        ReportParams params = reportDef.getReportParams();
        this.from = params.getWhenFrom().getTime();
        this.to = params.getWhenTo().getTime();
        this.toolIds = params.getWhatToolIds();
        this.eventIds = params.getWhatEventIds();
        this.resourceIds = params.getWhatResourceIds();
        this.resourceAction = params.getWhatResourceAction();
        this.who = params.getWho();
        this.userIds = params.getWhoUserIds();
    }
}
