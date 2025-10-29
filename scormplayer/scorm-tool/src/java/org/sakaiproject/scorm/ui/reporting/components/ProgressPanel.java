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

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import org.sakaiproject.scorm.model.api.Progress;
import org.sakaiproject.scorm.ui.reporting.util.ScormDurationFormatter;

public class ProgressPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	public ProgressPanel(String id, Progress progress)
	{
		super(id, new CompoundPropertyModel(progress));

		double mark = progress.getProgressMeasure();
		double scale = progress.getCompletionThreshold();

		Double percentage = null;
		if (mark >= 0 && scale > 0)
		{
			percentage = (mark / scale) * 100.0;
		}

		String percentageLabelText = percentage != null ? String.format(Locale.US, "%.1f%%", percentage) : "";
		Label percentCompleteLabel = new Label("percentComplete", Model.of(percentageLabelText));
		percentCompleteLabel.setVisible(percentage != null);
		add(percentCompleteLabel);

		WebMarkupContainer percentBar = new WebMarkupContainer("percentCompleteBar");
		percentBar.add(AttributeModifier.replace("role", "progressbar"));
		percentBar.add(AttributeModifier.replace("aria-valuemin", "0"));
		percentBar.add(AttributeModifier.replace("aria-valuemax", "100"));
		percentBar.setVisible(percentage != null);
		if (percentage != null)
		{
			double clamped = Math.max(0, Math.min(100, percentage));
			String width = String.format(Locale.US, "%.1f", clamped);
			percentBar.add(AttributeModifier.replace("style", "width: " + width + "%;"));
			percentBar.add(AttributeModifier.replace("aria-valuenow", width));
		}
		add(percentBar);

		Label successLabel = new Label("successStatus");
		Label completionLabel = new Label("completionStatus");
		successLabel.setVisible(progress.getSuccessStatus() != null && progress.getSuccessStatus().trim().length() != 0);
		completionLabel.setVisible(progress.getCompletionStatus() != null && progress.getCompletionStatus().trim().length() != 0);
		add(successLabel);
		add(completionLabel);

		String totalTime = progress.getTotalSessionSeconds();
		String formattedTotalTime = ScormDurationFormatter.formatOrNull(totalTime, getLocale());
		Label totalTimeLabel = new Label("totalTime", Model.of(formattedTotalTime != null ? formattedTotalTime : ""));
		totalTimeLabel.setVisible(formattedTotalTime != null);
		add(totalTimeLabel);
	}
}
