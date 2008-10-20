package org.sakaiproject.scorm.ui.reporting.components;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.ui.Icon;

public class InteractionPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private static ResourceReference BLANK_ICON = new ResourceReference(InteractionPanel.class, "res/brick.png");
	private static ResourceReference CORRECT_ICON = new ResourceReference(InteractionPanel.class, "res/tick.png");
	private static ResourceReference INCORRECT_ICON = new ResourceReference(InteractionPanel.class, "res/cross.png");
	private static ResourceReference UNANTICIPATED_ICON = new ResourceReference(InteractionPanel.class, "res/exclamation.png");
	private static ResourceReference NEUTRAL_ICON = new ResourceReference(InteractionPanel.class, "res/page_white_text.png");

	
	public InteractionPanel(String id, Interaction interaction) {
		super(id, new CompoundPropertyModel(interaction));
		
		add(new Label("interactionId"));
		add(new Label("type", new ResourceModel(new StringBuilder("type.").append(interaction.getType()).toString())));
		add(new Label("description"));
		add(new Label("learnerResponse"));
		
		StringBuilder correctResponse = new StringBuilder();
		for (String response : interaction.getCorrectResponses()) {
			if (response != null)
				correctResponse.append(response);
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
		if (result != null) {
			if (result.equalsIgnoreCase("correct"))
				resultIconReference = CORRECT_ICON;
			else if (result.equalsIgnoreCase("incorrect"))
				resultIconReference = INCORRECT_ICON;
			else if (result.equalsIgnoreCase("neutral")) {
				resultIconReference = NEUTRAL_ICON;
				isNeutral = true;
			} else if (result.equalsIgnoreCase("unanticipated"))
				resultIconReference = UNANTICIPATED_ICON;
			else
				showIcon = false;
		} else
			showIcon = false;
		
		Icon resultIcon = new Icon("resultIcon", resultIconReference);
		resultIcon.setVisible(showIcon);
		add(resultIcon);
		
		correctResponseLabel.setVisible(! isNeutral);
	}

}
