package org.sakaiproject.sitestats.api.report;

public interface ReportFormattedParams {

	public abstract String getReportSite(Report report);
	
	public abstract String getReportTitle(Report report);
	
	public abstract String getReportDescription(Report report);

	public abstract String getReportGenerationDate(Report report);

	public abstract String getReportActivityBasedOn(Report report);

	public abstract String getReportActivitySelectionTitle(Report report);

	public abstract String getReportActivitySelection(Report report);

	public abstract String getReportResourceActionTitle(Report report);

	public abstract String getReportResourceAction(Report report);

	public abstract String getReportTimePeriod(Report report);

	public abstract String getReportUserSelectionType(Report report);

	public abstract String getReportUserSelectionTitle(Report report);

	public abstract String getReportUserSelection(Report report);

	public abstract boolean isStringLocalized(String string);
}