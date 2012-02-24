/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
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

import org.sakaiproject.lessonbuildertool.tool.producers.LinkTrackerProducer;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class TrackerViewParameters extends SimpleViewParameters {
	private String URL;
	private long itemId;
	private boolean refresh = false;

	public TrackerViewParameters() {
		super();
	}

	public TrackerViewParameters(String VIEW_ID) {
		super(VIEW_ID);
	}

    	public TrackerViewParameters(String URL, long itemId, boolean refresh) {
		super(LinkTrackerProducer.VIEW_ID);
		this.URL = URL;
		this.itemId = itemId;
		this.refresh = refresh;
	}

	public void setURL(String URL) {
		this.URL = URL;
	}

	public String getURL() {
		return URL;
	}

	public void setItemId(long id) {
		itemId = id;
	}

	public long getItemId() {
		return itemId;
	}

    	public void setRefresh(boolean refresh) {
	    this.refresh = refresh;
	}

    	public boolean getRefresh() {
	    return refresh;
	}

}
