package org.sakaiproject.scorm.ui.reporting.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.scorm.model.api.Interaction;

public class InteractionPanel extends Panel {

	private static final long serialVersionUID = 1L;

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
		add(new Label("correctResponse", correctResponse.toString()));
		add(new Label("weighting", String.valueOf(interaction.getWeighting())));
		add(new Label("timestamp"));
		add(new Label("latency"));
		add(new Label("result"));
	}

}
