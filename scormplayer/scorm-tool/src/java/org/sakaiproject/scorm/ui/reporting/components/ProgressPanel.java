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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import org.sakaiproject.scorm.model.api.Progress;

public class ProgressPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	public ProgressPanel(String id, Progress progress)
	{
		super(id, new CompoundPropertyModel(progress));

		double mark = progress.getProgressMeasure();
		double scale = progress.getCompletionThreshold();
		double percentage = 100.0 * mark / scale;

		Label percentCompleteLabel = new Label("percentComplete", new Model("" + percentage));		
		percentCompleteLabel.setVisible(mark != -1 && scale != -1);
		add(percentCompleteLabel);

		Label successLabel = new Label("successStatus");
		Label completionLabel = new Label("completionStatus");
		successLabel.setVisible(progress.getSuccessStatus() != null && progress.getSuccessStatus().trim().length() != 0);
		completionLabel.setVisible(progress.getCompletionStatus() != null && progress.getCompletionStatus().trim().length() != 0);
		add(successLabel);
		add(completionLabel);
	}
}
