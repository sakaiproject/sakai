/**
 * Copyright (c) 2003-2012 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
