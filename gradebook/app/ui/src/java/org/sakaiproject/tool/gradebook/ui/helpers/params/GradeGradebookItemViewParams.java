/**
 * Copyright (c) 2003-2008 The Apereo Foundation
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
package org.sakaiproject.tool.gradebook.ui.helpers.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class GradeGradebookItemViewParams extends HelperAwareViewParams {
	
	public Long assignmentId;
	public String contextId;
	public String userId;
	
	public GradeGradebookItemViewParams(){}
	
	public GradeGradebookItemViewParams(String viewId, String contextId, Long assignmentId, String userId){
		super(viewId);
		this.assignmentId = assignmentId;
		this.contextId = contextId;
		this.userId = userId;
	}
	
	public String getParseSpec() {
		return super.getParseSpec() + ",@1:contextId,@2:assignmentId,@3:userId,finishURL";
	}
}