package org.sakaiproject.scorm.tool.components;

import org.adl.sequencer.SeqNavRequests;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.scorm.tool.pages.View;

public class ChoicePanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	/*
	 * <li><a href="#" wicket:id="navStart">Start</a></li>
	<li><a href="#" wicket:id="navAbandon">Abandon</a></li>
	<li><a href="#" wicket:id="navAbandonAll"/>Abandon All</a></li>
	<li><a href="#" wicket:id="navNone">None</a></li>
	<li><a href="#" wicket:id="navResumeAll">Resume All</a></li>
	<li><a href="#" wicket:id="navExit">Exit</a></li>
	<li><a href="#" wicket:id="navExitAll">Exit All</a></li>
	 */
	
	public ChoicePanel(String id, String contentPackageId, String error) {
		super(id);
		
		add(new Label("error", error));
		
		addChoice("navStart", SeqNavRequests.NAV_START, contentPackageId);
		addChoice("navAbandon", SeqNavRequests.NAV_ABANDON, contentPackageId);
		addChoice("navAbandonAll", SeqNavRequests.NAV_ABANDONALL, contentPackageId);
		addChoice("navNone", SeqNavRequests.NAV_NONE, contentPackageId);
		addChoice("navResumeAll", SeqNavRequests.NAV_RESUMEALL, contentPackageId);
		addChoice("navExit", SeqNavRequests.NAV_EXIT, contentPackageId);
		addChoice("navExitAll", SeqNavRequests.NAV_EXITALL, contentPackageId);
		
	}

	
	private void addChoice(String requestId, int request, String contentPackageId) {
		final PageParameters params = new PageParameters();
		params.add("contentPackage", contentPackageId);
		params.add("navRequest", "" + request);
		
		add(new BookmarkablePageLink(requestId, View.class, params));
	}
	
	

	
	
}
