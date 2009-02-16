package org.sakaiproject.sitestats.api.report;

import java.util.Date;
import java.util.List;

import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.Stat;


public interface Report {

	/** Get the reports data (List of {@link EventStat} or {@link ResourceStat}). */
	public List<Stat> getReportData();

	/** Set the reports data (List of {@link EventStat} or {@link ResourceStat}). */
	public void setReportData(List<Stat> reportData);

	/** Get the report definition (see {@link ReportDef}). */
	public ReportDef getReportDefinition();

	/** Set the report definition (see {@link ReportDef}). */
	public void setReportDefinition(ReportDef reportDef);
	
	/** Get the time the report was generated. */
	public Date getReportGenerationDate();
	
	/** Get the localized date the report was generated. */
	public String getLocalizedReportGenerationDate();

	/** Set the localized date the report was generated. */
	public void setReportGenerationDate(Date reportGenerationDate);
}