/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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