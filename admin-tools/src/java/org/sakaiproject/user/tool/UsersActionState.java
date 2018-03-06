/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.user.tool;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.cheftool.ControllerState;

/**
 * Maintains user interface state for the Users action class.
 * This approach is borrowed from the AnnouncementActionState class
 * This may be able to be replaced with just using attributes in the SessionState...
 */
public class UsersActionState extends ControllerState {

	//store attachments
	private List attachments = new ArrayList();
	public List getAttachments(){
		return attachments;
	}	
	public void setAttachments(List attachments){
		if (attachments != null){
			this.attachments = attachments;
		}
		else{
			attachments.clear();
		}
	}
	
	//store status so we know where to go to when done with the helper
	private String m_status = null;
	public String getStatus(){
		return m_status;

	}
	public void setStatus(String status) {
		if (status!=null) {
			// if there's a change
			if (!status.equals(m_status)) {
				// remember the new
				m_status = status;
			}
		}
	}

}
