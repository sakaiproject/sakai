package org.sakaiproject.lessonbuildertool.tool.view;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class CommentsViewParameters extends SimpleViewParameters {
	public Long itemId = -1L;
	public Long pageItemId = -1L;
	public boolean showAllComments = false;
	public boolean showNewComments = false;
	public long postedComment = -1;
	public String deleteComment = null;
	public String siteId = null;
	public long pageId = -1;
	public String placementId = null;
	
	public String author = null;
	public boolean filter = false; // If this is set, only shows comments by this author.
	public boolean studentContentItem = false; // If set, this means that itemId refers to a Student Content item
	
	public CommentsViewParameters() { super(); }
	
	public CommentsViewParameters(String VIEW_ID) {
		super(VIEW_ID);
	}
}
