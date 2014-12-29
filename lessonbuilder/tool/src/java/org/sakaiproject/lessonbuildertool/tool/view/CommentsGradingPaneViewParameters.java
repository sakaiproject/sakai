package org.sakaiproject.lessonbuildertool.tool.view;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class CommentsGradingPaneViewParameters extends SimpleViewParameters {
	public boolean studentContentItem = false;
	public long commentsItemId;
	
	// So that we can make a back link.
	public long pageId;
	public long pageItemId;
	public String siteId = null;
	public String placementId = null;
	
	public CommentsGradingPaneViewParameters() {
		super();
	}
	
	public CommentsGradingPaneViewParameters(String VIEW_ID) {
		super(VIEW_ID);
	}
}
