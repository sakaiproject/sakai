/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.jsf;

import java.util.ArrayList;
import java.util.List;
import org.sakaiproject.signup.model.SignupAttachment;

public class SignupAttachmentWrapper {
	
	private List<SignupAttachment> attendeeAttachments = new ArrayList<SignupAttachment>();
	
	private List<SignupAttachment> eventMainAttachments = new ArrayList<SignupAttachment>();
	
	public SignupAttachmentWrapper(List<SignupAttachment> attachments){
		if(attachments != null){
			for (SignupAttachment attach: attachments) {
				if(attach.getTimeslotId() !=null && ! attach.getViewByAll())
					attendeeAttachments.add(attach);
				else if( attach.getViewByAll())
					eventMainAttachments.add(attach);
				
				//TODO other cases: such as attachment for a specific time slot only.
			}
		}
	}

	public List<SignupAttachment> getAttendeeAttachments() {
		return attendeeAttachments;
	}

	public void setAttendeeAttachments(List<SignupAttachment> attendeeAttachments) {
		this.attendeeAttachments = attendeeAttachments;
	}

	public List<SignupAttachment> getEventMainAttachments() {
		return eventMainAttachments;
	}

	public void setEventMainAttachments(List<SignupAttachment> eventMainAttachments) {
		this.eventMainAttachments = eventMainAttachments;
	}
	

}
