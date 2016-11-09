
package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/**
 * Feedback panel used and reused by GradebookNG so that the messages are styled consistently. Markup ID is automatically output.
 */
public class GbFeedbackPanel extends FeedbackPanel {

	private static final long serialVersionUID = 1L;

	public GbFeedbackPanel(final String id) {
		super(id);

		setOutputMarkupId(true);
	}

	@Override
	protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
		final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

		if (message.getLevel() == FeedbackMessage.ERROR ||
				message.getLevel() == FeedbackMessage.DEBUG ||
				message.getLevel() == FeedbackMessage.FATAL) {
			add(AttributeModifier.replace("class", "messageError"));
			add(AttributeModifier.append("class", "feedback"));
		} else if (message.getLevel() == FeedbackMessage.WARNING) {
			add(AttributeModifier.replace("class", "messageWarning"));
			add(AttributeModifier.append("class", "feedback"));
		} else if (message.getLevel() == FeedbackMessage.INFO) {
			add(AttributeModifier.replace("class", "messageInformation"));
			add(AttributeModifier.append("class", "feedback"));
		} else if (message.getLevel() == FeedbackMessage.SUCCESS) {
			add(AttributeModifier.replace("class", "messageSuccess"));
			add(AttributeModifier.append("class", "feedback"));
		}

		return newMessageDisplayComponent;
	}

	@Override
	public void onBeforeRender() {
		if (getFeedbackMessages().isEmpty()) {
			// ensure class is removed from feedback panel
			// when there are no messages to avoid empty
			// colored rectangle
			clear();
		}
		super.onBeforeRender();
	}

	/**
	 * Clear all messages from the feedback panel
	 */
	public void clear() {
		getFeedbackMessages().clear();
		this.add(AttributeModifier.remove("class"));
	}
}
