package org.sakaiproject.scorm.ui.tool.components;

import org.adl.sequencer.SeqNavRequests;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.sakaiproject.scorm.ui.tool.pages.BaseToolPage;
import org.sakaiproject.scorm.ui.tool.pages.View;

public class ChoicePanel extends Panel {
	private static final long serialVersionUID = 1L;

	
	public ChoicePanel(String id, PageParameters pageParams, String error) {
		super(id);
		
		String contentPackageId = pageParams.getString("contentPackage");
		
		if (contentPackageId != null) 
			contentPackageId = contentPackageId.replace(':', '/');
		
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
