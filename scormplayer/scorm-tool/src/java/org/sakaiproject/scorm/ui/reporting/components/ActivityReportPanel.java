/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.reporting.components;

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

import org.sakaiproject.scorm.model.api.ActivityReport;

public class ActivityReportPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	public ActivityReportPanel(String id, ActivityReport report)
	{
		super(id, new CompoundPropertyModel(report));

		add(new Label("title"));
		add(new ScorePanel("score", report.getScore()));
		add(new ProgressPanel("progress", report.getProgress()));
		add(new ObjectivesPanel("objectives", new ArrayList<>(report.getObjectives().values())));
		add(new CMIDataGraph("cmiDataGraph", report.getCmiData()));
	}
}
