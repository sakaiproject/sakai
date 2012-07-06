package org.sakaiproject.signup.logic.messages;

import java.util.List;

import org.sakaiproject.signup.model.SignupTimeslot;

/**
 * Email notification classes implement this to show that they hold information about timeslot changes for a user
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface SignupTimeslotChanges {

	/**
	 * List of timeslots that the user has been removed from
	 */
	List<SignupTimeslot> getRemoved();
	
	/**
	 * List of timeslots that the user has been added to
	 */
	List<SignupTimeslot> getAdded();

	
}
