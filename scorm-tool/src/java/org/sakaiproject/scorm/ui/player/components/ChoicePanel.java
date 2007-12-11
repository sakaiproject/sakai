package org.sakaiproject.scorm.ui.player.components;

import org.adl.sequencer.SeqNavRequests;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;

public class ChoicePanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	
	public ChoicePanel(String id, PageParameters pageParams, String error) {
		super(id);
		
		String courseId = pageParams.getString("courseId");
		
		
		add(new Label("error", error));
		
		addChoice("navStart", SeqNavRequests.NAV_START, courseId);
		addChoice("navAbandon", SeqNavRequests.NAV_ABANDON, courseId);
		addChoice("navAbandonAll", SeqNavRequests.NAV_ABANDONALL, courseId);
		addChoice("navNone", SeqNavRequests.NAV_NONE, courseId);
		addChoice("navResumeAll", SeqNavRequests.NAV_RESUMEALL, courseId);
		addChoice("navExit", SeqNavRequests.NAV_EXIT, courseId);
		addChoice("navExitAll", SeqNavRequests.NAV_EXITALL, courseId);
		
	}

	
	private void addChoice(String requestId, int request, String courseId) {
		final PageParameters params = new PageParameters();
		params.add("courseId", courseId);
		params.add("navRequest", "" + request);
		
		add(new BookmarkablePageLink(requestId, PlayerPage.class, params));
	}
		
	
}
