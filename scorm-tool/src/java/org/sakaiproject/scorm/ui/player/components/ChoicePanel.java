package org.sakaiproject.scorm.ui.player.components;

import org.adl.sequencer.SeqNavRequests;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;

public class ChoicePanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	
	public ChoicePanel(String id, long contentPackageId, String resourceId, String error) {
		super(id);
		
	
		add(new Label("error", error));
		
		addChoice("navStart", SeqNavRequests.NAV_START, contentPackageId, resourceId);
		addChoice("navAbandon", SeqNavRequests.NAV_ABANDON, contentPackageId, resourceId);
		addChoice("navAbandonAll", SeqNavRequests.NAV_ABANDONALL, contentPackageId, resourceId);
		addChoice("navNone", SeqNavRequests.NAV_NONE, contentPackageId, resourceId);
		addChoice("navResumeAll", SeqNavRequests.NAV_RESUMEALL, contentPackageId, resourceId);
		addChoice("navExit", SeqNavRequests.NAV_EXIT, contentPackageId, resourceId);
		addChoice("navExitAll", SeqNavRequests.NAV_EXITALL, contentPackageId, resourceId);
		
	}

	
	private void addChoice(String requestId, int request, long id, String resourceId) {
		final PageParameters params = new PageParameters();
		params.add("contentPackageId", "" + id);
		params.add("resourceId", resourceId);
		params.add("navRequest", "" + request);
		
		add(new BookmarkablePageLink(requestId, PlayerPage.class, params));
	}
	
	
}
