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
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.StatsManager;


public class ServerWideModel extends Model {
	private static final long	serialVersionUID		= 1L;
	public static final String	MONTHLY_LOGIN_REPORT	= StatsManager.MONTHLY_LOGIN_REPORT;
	public static final String	WEEKLY_LOGIN_REPORT		= StatsManager.WEEKLY_LOGIN_REPORT;
	public static final String	DAILY_LOGIN_REPORT		= StatsManager.DAILY_LOGIN_REPORT;
	public static final String	REGULAR_USERS_REPORT	= StatsManager.REGULAR_USERS_REPORT;
	public static final String	HOURLY_USAGE_REPORT		= StatsManager.HOURLY_USAGE_REPORT;
	public static final String	TOP_ACTIVITIES_REPORT	= StatsManager.TOP_ACTIVITIES_REPORT;
	public static final String	TOOL_REPORT				= StatsManager.TOOL_REPORT;
	public static final String	NONE					= "none";

	private String				reportTitle;
	private String				reportDescription;
	private String				reportNotes;
	private String				selectedView			= NONE;

	
	public ServerWideModel() {
	}

	public final String getReportTitle() {
		return reportTitle;
	}

	public final void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

	public final String getReportDescription() {
		return reportDescription;
	}

	public final void setReportDescription(String reportDescription) {
		this.reportDescription = reportDescription;
	}

	public final String getReportNotes() {
		return reportNotes;
	}

	public final void setReportNotes(String reportNotes) {
		this.reportNotes = reportNotes;
	}

	public final String getSelectedView() {
		return selectedView;
	}

	public final void setSelectedView(String selectedView) {
		this.selectedView = selectedView;
		if(MONTHLY_LOGIN_REPORT.equals(selectedView)) {
			reportTitle = (String) new ResourceModel("title_monthly_login_report").getObject();
			reportDescription = (String) new ResourceModel("desc_monthly_login_report").getObject();
			reportNotes = (String) new ResourceModel("notes_monthly_login_report").getObject();
			
		}else if(WEEKLY_LOGIN_REPORT.equals(selectedView)) {
			reportTitle = (String) new ResourceModel("title_weekly_login_report").getObject();
			reportDescription = (String) new ResourceModel("desc_weekly_login_report").getObject();
			reportNotes = (String) new ResourceModel("notes_weekly_login_report").getObject();
			
		}else if(DAILY_LOGIN_REPORT.equals(selectedView)) {
			reportTitle = (String) new ResourceModel("title_daily_login_report").getObject();
			reportDescription = (String) new ResourceModel("desc_daily_login_report").getObject();
			reportNotes = (String) new ResourceModel("notes_daily_login_report").getObject();
			
		}else if(REGULAR_USERS_REPORT.equals(selectedView)) {
			reportTitle = (String) new ResourceModel("title_regular_users_report").getObject();
			reportDescription = (String) new ResourceModel("desc_regular_users_report").getObject();
			reportNotes = (String) new ResourceModel("notes_regular_users_report").getObject();
			
		}else if(HOURLY_USAGE_REPORT.equals(selectedView)) {
			reportTitle = (String) new ResourceModel("title_hourly_usage_report").getObject();
			reportDescription = (String) new ResourceModel("desc_hourly_usage_report").getObject();
			reportNotes = (String) new ResourceModel("notes_hourly_usage_report").getObject();
			
		}else if(TOP_ACTIVITIES_REPORT.equals(selectedView)) {
			reportTitle = (String) new ResourceModel("title_top_activities_report").getObject();
			reportDescription = (String) new ResourceModel("desc_top_activities_report").getObject();
			reportNotes = (String) new ResourceModel("notes_top_activities_report").getObject();
			
		}else if(TOOL_REPORT.equals(selectedView)) {
			reportTitle = (String) new ResourceModel("title_tool_report").getObject();
			reportDescription = (String) new ResourceModel("desc_tool_report").getObject();
			reportNotes = (String) new ResourceModel("notes_tool_report").getObject();
			
		}else{
			reportTitle = "";
			reportDescription = "";
			reportNotes = "";
		}
	}

}
