package org.sakaiproject.sitestats.impl.report;

import java.util.List;

import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.time.api.Time;

/**
 * @author mgrilo
 *
 */
public class ReportImpl implements Report {
	private ReportParams				reportParams;
	private Time						reportGenerationDate = null;
	private List<Stat>					reportData;
	
	public ReportImpl(){
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.Report#getReportData()
	 */
	public List<Stat> getReportData() {
		return reportData;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.Report#setReportData(java.util.List)
	 */
	public void setReportData(List<Stat> reportData) {
		this.reportData = reportData;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.Report#getReportParams()
	 */
	public ReportParams getReportParams() {
		return reportParams;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.Report#setReportParams(org.sakaiproject.sitestats.api.ReportParams)
	 */
	public void setReportParams(ReportParams reportParams) {
		this.reportParams = reportParams;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Report#getReportGenerationDate()
	 */
	public Time getReportGenerationDate() {
		return reportGenerationDate;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Report#getLocalizedReportGenerationDate()
	 */
	public String getLocalizedReportGenerationDate() {
		return reportGenerationDate.toStringLocalFull();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Report#setReportGenerationDate(org.sakaiproject.time.api.Time)
	 */
	public void setReportGenerationDate(Time reportGenerationDate) {
		this.reportGenerationDate = reportGenerationDate;
	}
	
	
}
