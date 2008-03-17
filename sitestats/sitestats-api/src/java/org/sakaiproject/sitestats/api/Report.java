package org.sakaiproject.sitestats.api;

import java.util.List;

import org.sakaiproject.time.api.Time;


public interface Report {

	public List<CommonStatGrpByDate> getReportData();

	public void setReportData(List<CommonStatGrpByDate> reportData);

	public ReportParams getReportParams();

	public void setReportParams(ReportParams reportParams);
	
	public Time getReportGenerationDate();
	
	public String getLocalizedReportGenerationDate();

	public void setReportGenerationDate(Time reportGenerationDate);
}