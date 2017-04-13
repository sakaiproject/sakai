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

import org.sakaiproject.rsf.helper.HelperViewParameters;

public class FilePickerViewParameters extends HelperViewParameters {

	private long sender = -1;
	private long pageItemId = -1;

	/**
	 * Simple boolean used for the resource picker. We go into the resource picker for two basic
	 * reasons, to add resources to the page or to add multimedia to the page. This will distinguish
	 * between them.
	 * 
	 * true = Add Multimedia false = Add Resource
	 */
	private boolean resourceType = false;
        private boolean website = false;
        private boolean caption = false;
	public String addBefore = ""; // itemid to add a new item before that item
	public String name = "";

	public FilePickerViewParameters() {
		super();
	}

	public FilePickerViewParameters(String VIEW_ID) {
		super(VIEW_ID);
	}

	public void setSender(long sender) {
		this.sender = sender;
	}

	public long getSender() {
		return sender;
	}

	public void setPageItemId(long pageItemId) {
	        this.pageItemId = pageItemId;
	}

	public long getPageItemId() {
		return pageItemId;
	}


	public void setResourceType(boolean b) {
		resourceType = b;
	}

	public boolean getResourceType() {
		return resourceType;
	}

	public void setWebsite(boolean b) {
		website = b;
	}

	public boolean isWebsite() {
		return website;
	}

	public void setCaption(boolean b) {
		caption = b;
	}

	public boolean getCaption() {
		return caption;
	}

	public void setAddBefore(String s) {
		this.addBefore = s;
	}

	public String getAddBefore() {
	    // should be impnossible, but I'd rather be defensive here than
	    // have all clients do it
		if (addBefore == null)
		    return "";
		return addBefore;
	}

	public void setName(String s) {
		this.name = s;
	}

	public String getName() {
		return name;
	}

}
