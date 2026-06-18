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
import org.sakaiproject.sitestats.api.view.SiteStatsServerWideReportIds;


public class ServerWideModel extends Model {
	private static final long serialVersionUID = 1L;
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
		if (!SiteStatsServerWideReportIds.isSupported(selectedView)) {
			reportTitle = "";
			reportDescription = "";
			reportNotes = "";
			return;
		}

		String suffix = SiteStatsServerWideReportIds.keySuffix(selectedView);
		reportTitle = (String) new ResourceModel("title_" + suffix).getObject();
		reportDescription = (String) new ResourceModel("desc_" + suffix).getObject();
		reportNotes = (String) new ResourceModel("notes_" + suffix).getObject();
	}

}
