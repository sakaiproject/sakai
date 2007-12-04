package org.sakaiproject.scorm.model.api;

import org.adl.sequencer.SeqActivity;

public class SeqActivityTreeSnapshot {

	private long id;
	
	/**
	 * This is the course identifier for the current activity tree
	 */
	private String mCourseID = null; 

	/**
	 * This is the student identifier for the student utilizing this activity
	 * tree
	 */
	private String mLearnerID = null;

	/**
	 * This is last activity experienced by the student prior to a SuspendAll
	 * sequencing request.<br><br>
	 * Defined in IMS SS AM
	 */
	private SeqActivity mSuspendAll = null;
	
	/**
	 * Retrieves the activity (<code>SeqActivity</code>) associated with the
	 * last attempted activity before a 'SuspendAll' sequencing request.
	 * 
	 * @return The activity (<code>SeqActivity</code>) associated with the last
	 *         attempted activity or <code>null</code> if none exists.
	 */
	public SeqActivity getSuspendAll() {
		return mSuspendAll;
	}	
	
	public String getCourseId() {
		return mCourseID;
	}
	
}
