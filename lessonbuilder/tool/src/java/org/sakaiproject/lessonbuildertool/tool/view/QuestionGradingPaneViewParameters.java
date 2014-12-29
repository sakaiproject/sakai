package org.sakaiproject.lessonbuildertool.tool.view;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class QuestionGradingPaneViewParameters extends SimpleViewParameters {
	public long questionItemId;
	
	// So that we can make a back link.
	public long pageId;
	public long pageItemId;
	public String siteId = null;
	public String placementId = null;
	
	public QuestionGradingPaneViewParameters() {
		super();
	}
	
	public QuestionGradingPaneViewParameters(String VIEW_ID) {
		super(VIEW_ID);
	}
}
