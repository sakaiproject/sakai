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
