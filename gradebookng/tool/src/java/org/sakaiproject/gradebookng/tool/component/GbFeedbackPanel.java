/*
 * Copyright (c) Orchestral Developments Ltd and the Orion Health group of companies (2001 - 2016).
 *
 * This document is copyright. Except for the purpose of fair reviewing, no part
 * of this publication may be reproduced or transmitted in any form or by any
 * means, electronic or mechanical, including photocopying, recording, or any
 * information storage and retrieval system, without permission in writing from
 * the publisher. Infringers of copyright render themselves liable for
 * prosecution.
 */
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
				message.getLevel() == FeedbackMessage.FATAL ||
				message.getLevel() == FeedbackMessage.WARNING) {
			add(AttributeModifier.replace("class", "messageError"));
			add(AttributeModifier.append("class", "feedback"));
		} else if (message.getLevel() == FeedbackMessage.INFO) {
			add(AttributeModifier.replace("class", "messageSuccess"));
			add(AttributeModifier.append("class", "feedback"));
		}

		return newMessageDisplayComponent;
	}
}
