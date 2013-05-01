/************************************************************************************
 *
 * Author: Stephen Kane, steve.kane@rutgers.edu
 *
 * Copyright (c) 2013 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.tool.view;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class PeerEvalStatsViewParameters extends SimpleViewParameters {
	private long itemId = -1L;
	private long sendingPage = -1;
	
	public PeerEvalStatsViewParameters() { 
		super(); 
	}
	
	public PeerEvalStatsViewParameters(String VIEW_ID) {
		super(VIEW_ID);
	}
	
	//getters and setters
	
	public void setSendingPage(long l) {
		sendingPage = l;
	}

	public long getSendingPage() {
		return sendingPage;
	}
	
	public void setItemId(long l) {
		itemId = l;
	}

	public long getItemId() {
		return itemId;
	}
}
