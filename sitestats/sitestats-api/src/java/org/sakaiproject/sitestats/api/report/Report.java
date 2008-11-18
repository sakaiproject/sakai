package org.sakaiproject.sitestats.api.report;

import java.util.List;

import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.time.api.Time;


public interface Report {

	/** Get the reports data (List of {@link EventStat} or {@link ResourceStat}). */
	public List<Stat> getReportData();

	/** Set the reports data (List of {@link EventStat} or {@link ResourceStat}). */
	public void setReportData(List<Stat> reportData);

	/** Get the report parameters (see {@link ReportParams}). */
	public ReportParams getReportParams();

	/** Set the report parameters (see {@link ReportParams}). */
	public void setReportParams(ReportParams reportParams);
	
	/** Get the time the report was generated. */
	public Time getReportGenerationDate();
	
	/** Get the localized date the report was generated. */
	public String getLocalizedReportGenerationDate();

	/** Set the localized date the report was generated. */
	public void setReportGenerationDate(Time reportGenerationDate);
}