package org.sakaiproject.signup.tool.downloadEvents;

import lombok.extern.apachecommons.CommonsLog;

import org.sakaiproject.signup.tool.jsf.SignupMeetingsBean;

/**
 * Bean to handle the download of ICS files from the UI
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@CommonsLog
public class DownloadICSBean extends SignupMeetingsBean {

	
	/**
	 * Creates and returns the ICS for a timeslot
	 */
	public void downloadICSForTimeslot() {
		
		//get the wrapper
		
		/*
		 
		 SignupMeetingWrapper wrapper = null;
		String path = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestServletPath();

		if (path != null && path.indexOf(FROM_ORGANIZER_EVENT_PAGE) > 0) {
			wrapper = getOrganizerSignupMBean().getMeetingWrapper();
		} else if (path != null && path.indexOf(FROM_ATTENDEE_EVENT_PAGE) > 0)
			wrapper = getAttendeeSignupMBean().getMeetingWrapper();
		 
		 
		 
		 */
		
		
	}
	
	
	/**
	 * Creates and returns the ICS for an overall meeting
	 */
	public void downloadICSForMeeting() {
		
	}
	
}
