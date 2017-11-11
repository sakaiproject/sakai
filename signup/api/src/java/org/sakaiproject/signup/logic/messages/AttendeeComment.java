/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
package org.sakaiproject.signup.logic.messages;

public class AttendeeComment {
	
	private String attendeeComment;
	
	private String attendeeId;
	
	private String commentModifierUserId;
	
	/**
	 * Constructor
	 * 
	 * @param attendeeComment
	 *            holds the comment of the attendee in the timeslot
	 * @param attendeeId
	 *         	  the attendee's Id
	 * @param commentModifierUserId
	 *            current user's Id / comment modifier's Id
	 */
	
	public AttendeeComment(String attendeeComment, String attendeeId, String commentModifierUserId){
		this.attendeeComment = attendeeComment;
		this.attendeeId = attendeeId;
		this.commentModifierUserId = commentModifierUserId;
	}

	public String getAttendeeComment() {
		return attendeeComment;
	}

	public void setAttendeeComment(String attendeeComment) {
		this.attendeeComment = attendeeComment;
	}

	public String getAttendeeId() {
		return attendeeId;
	}

	public void setAttendeeId(String attendeeId) {
		this.attendeeId = attendeeId;
	}

	public String getCommentModifierUserId() {
		return commentModifierUserId;
	}

	public void setCommentModifierUserId(String commentModifierUserId) {
		this.commentModifierUserId = commentModifierUserId;
	}
	
	
}
