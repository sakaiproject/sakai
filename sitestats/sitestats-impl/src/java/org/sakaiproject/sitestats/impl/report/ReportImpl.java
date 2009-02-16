package org.sakaiproject.sitestats.impl.report;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.time.cover.TimeService;

/**
 * @author Nuno Fernandes
 *
 */
public class ReportImpl implements Report, Serializable {
	private static final long			serialVersionUID		= 1L;
	//private SimpleDateFormat			dateFormat				= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private ReportDef					reportDef;
	private Date						reportGenerationDate 	= null;
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
	public Date getReportGenerationDate() {
		return reportGenerationDate;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Report#getLocalizedReportGenerationDate()
	 */
	public String getLocalizedReportGenerationDate() {
		//return dateFormat.format(reportGenerationDate);
		return TimeService.newTime(reportGenerationDate.getTime()).toStringLocalFull();
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.Report#setReportGenerationDate(org.sakaiproject.time.api.Time)
	 */
	public void setReportGenerationDate(Date reportGenerationDate) {
		this.reportGenerationDate = reportGenerationDate;
	}
	
	
}
