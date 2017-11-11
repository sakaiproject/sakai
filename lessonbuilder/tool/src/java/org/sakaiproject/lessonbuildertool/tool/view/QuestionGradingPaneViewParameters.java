/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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
