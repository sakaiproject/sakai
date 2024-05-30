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
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.ui.Icon;

public class InteractionPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private static ResourceReference BLANK_ICON = new PackageResourceReference(InteractionPanel.class, "res/brick.png");
	private static ResourceReference CORRECT_ICON = new PackageResourceReference(InteractionPanel.class, "res/tick.png");
	private static ResourceReference INCORRECT_ICON = new PackageResourceReference(InteractionPanel.class, "res/cross.png");
	private static ResourceReference UNANTICIPATED_ICON = new PackageResourceReference(InteractionPanel.class, "res/exclamation.png");
	private static ResourceReference NEUTRAL_ICON = new PackageResourceReference(InteractionPanel.class, "res/page_white_text.png");

	public InteractionPanel(String id, Interaction interaction)
	{
		super(id, new CompoundPropertyModel(interaction));

		add(new Label("interactionId"));
		add(new Label("type", new ResourceModel(new StringBuilder("type.").append(interaction.getType()).toString())));
		add(new Label("description"));
		add(new Label("learnerResponse"));

		StringBuilder correctResponse = new StringBuilder();
		for (String response : interaction.getCorrectResponses())
		{
			if (response != null)
			{
				correctResponse.append(response);
			}
		}

		Label correctResponseLabel = new Label("correctResponse", correctResponse.toString());
		add(correctResponseLabel);
		add(new Label("weighting", String.valueOf(interaction.getWeighting())));
		add(new Label("timestamp"));
		add(new Label("latency"));
		add(new Label("result"));

		boolean showIcon = true;
		boolean isNeutral = false;
		ResourceReference resultIconReference = BLANK_ICON;
		String result = interaction.getResult();
		if (result != null)
		{
			if (result.equalsIgnoreCase("correct"))
			{
				resultIconReference = CORRECT_ICON;
			}
			else if (result.equalsIgnoreCase("incorrect"))
			{
				resultIconReference = INCORRECT_ICON;
			}
			else if (result.equalsIgnoreCase("neutral"))
			{
				resultIconReference = NEUTRAL_ICON;
				isNeutral = true;
			}
			else if (result.equalsIgnoreCase("unanticipated"))
			{
				resultIconReference = UNANTICIPATED_ICON;
			}
			else
			{
				showIcon = false;
			}
		}
		else
		{
			showIcon = false;
		}

		Icon resultIcon = new Icon("resultIcon", resultIconReference);
		resultIcon.setVisible(showIcon);
		add(resultIcon);

		correctResponseLabel.setVisible(! isNeutral);
	}
}
