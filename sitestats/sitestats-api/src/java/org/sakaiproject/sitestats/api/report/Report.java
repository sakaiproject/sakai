package org.sakaiproject.sitestats.api.report;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.time.cover.TimeService;


public class Report implements Serializable {	
	private static final long			serialVersionUID		= 1L;
	private ReportDef					reportDef;
	private Date						reportGenerationDate 	= null;
	private List<Stat>					reportData;
	
	public Report(){
	}
	
	/** Get the reports data (List of {@link EventStat} or {@link ResourceStat}). */
	public List<Stat> getReportData() {
		return reportData;
	}
	/** Set the reports data (List of {@link EventStat} or {@link ResourceStat}). */
	public void setReportData(List<Stat> reportData) {
		this.reportData = reportData;
	}
	
	/** Get the report definition (see {@link ReportDef}). */
	public ReportDef getReportDefinition() {
		return reportDef;
	}
	/** Set the report definition (see {@link ReportDef}). */
	public void setReportDefinition(ReportDef reportDef) {
		this.reportDef = reportDef;
	}

	/** Get the time the report was generated. */
	public Date getReportGenerationDate() {
		return reportGenerationDate;
	}	
	/** Get the localized date the report was generated. */
	public String getLocalizedReportGenerationDate() {
		return TimeService.newTime(reportGenerationDate.getTime()).toStringLocalFull();
	}	
	/** Set the localized date the report was generated. */
	public void setReportGenerationDate(Date reportGenerationDate) {
		this.reportGenerationDate = reportGenerationDate;
	}
}