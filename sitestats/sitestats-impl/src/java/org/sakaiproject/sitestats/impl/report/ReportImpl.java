package org.sakaiproject.sitestats.impl.report;

import java.io.Serializable;
import java.util.List;

import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.time.api.Time;

/**
 * @author Nuno Fernandes
 *
 */
public class ReportImpl implements Report, Serializable {
	private static final long	serialVersionUID	= 1L;
	private ReportDef					reportDef;
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
	 * @see org.sakaiproject.sitestats.api.report.Report#getReportDefinition()
	 */
	public ReportDef getReportDefinition() {
		return reportDef;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.Report#setReportDefinition(org.sakaiproject.sitestats.api.report.ReportDef)
	 */
	public void setReportDefinition(ReportDef reportDef) {
		this.reportDef = reportDef;
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
