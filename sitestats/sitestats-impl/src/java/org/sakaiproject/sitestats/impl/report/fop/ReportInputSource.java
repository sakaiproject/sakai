package org.sakaiproject.sitestats.impl.report.fop;

import org.sakaiproject.sitestats.api.report.Report;
import org.xml.sax.InputSource;

public class ReportInputSource extends InputSource {
	private Report report;

	public ReportInputSource(Report report) {
		super();
		this.report = report;
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}
	
}
