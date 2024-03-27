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

import org.sakaiproject.scorm.model.api.Score;

public class ScorePanel extends Panel
{
	private static final long serialVersionUID = 1L;

	public ScorePanel(String id, Score score)
	{
		super(id);

		addScoreLabel("scaledScore", score.getScaled(), true);
		addScoreLabel("rawScore", score.getRaw(), false);
		addScoreParenthetical("scoreParenthetical", score.getMin(), score.getMax());

		if (score.getScaled() == -1 && score.getRaw() == -1)
		{
			setVisible(false);
		}
	}

	private void addScoreLabel(String id, double score, boolean isPercentage)
	{
		String scoreValue = String.valueOf(score);
		if (isPercentage)
		{
			scoreValue = getPercentageString(score);
		}

		Label label = new Label(id, scoreValue);

		if (score < 0.0)
		{
			label.setVisible(false);
		}

		add(label);
	}

	private void addScoreParenthetical(String id, double min, double max)
	{
		String text = new StringBuilder(" [").append(min).append(" - ").append(max).append("]").toString();

		Label label = new Label(id, text);

		if (min == -1 || max == -1)
		{
			label.setVisible(false);
		}

		add(label);
	}

	private String getPercentageString(double d)
	{
		double p = d * 100.0;
		return "" + p + " %";
	}
}
