/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charlie Groll <charlieg@rutgers.edu>
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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

import org.sakaiproject.lessonbuildertool.tool.producers.ClosePageProducer;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class CloseViewParameters extends SimpleViewParameters {
	private boolean refresh = false;

	public CloseViewParameters() {
		super();
	}

	public CloseViewParameters(String VIEW_ID) {
		super(VIEW_ID);
	}

   	public CloseViewParameters(boolean refresh) {
		super(ClosePageProducer.VIEW_ID);
		this.refresh = refresh;
	}

   	public CloseViewParameters(String VIEW_ID, boolean refresh) {
		super(VIEW_ID);
		this.refresh = refresh;
	}

   	public void setRefresh(boolean refresh) {
	    this.refresh = refresh;
	}

   	public boolean getRefresh() {
	    return refresh;
	}

}
