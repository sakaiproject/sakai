package org.adl.sequencer;

import java.io.Serializable;
import java.util.Vector;

public interface ISequencer extends Serializable {

	/**
	 * Retrieves the set of objective status records associated with an
	 * activity.
	 * 
	 * @param iActivityID
	 *            The ID of the activity whose objectives are requested.
	 * 
	 * @return A <code>Vector</code> of <code>ADLObjStatus</code> objects
	 *         for the requested activity or <code>null</code> if none are
	 *         defined.
	 */
	public Vector getObjStatusSet(String iActivityID);

	/**
	 * Retrieves the current set of valid navigation requests.
	 * 
	 * @param oValid
	 *            Upon return, contains the set of valid navigation reqeusts.
	 */
	public void getValidRequests(IValidRequests oValid);

	/**
	 * Sets the active activity tree for this sequencer to act on.
	 * 
	 * @param iTree
	 *            The activty tree for this sequencer to act on.
	 */
	public void setActivityTree(ISeqActivityTree iTree);

	/**
	 * Gets the current active activity tree.
	 * 
	 * @return The current activity tree (<code>SeqActivityTree</code>).
	 */
	public ISeqActivityTree getActivityTree();

	/**
	 * Gets the root of the current activity tree.
	 * 
	 * @return The root activity of the current activity tree (<code>SeqActivity</code>).
	 */
	public ISeqActivity getRoot();

	/**
	 * Clear the current activity; this is done unconditionally.
	 */
	public void clearSeqState();

	/**
	 * This method is used to inform the sequencer of the suspended state for
	 * the current activity. This state will take affect when the activity
	 * terminates.
	 * 
	 * @param iID
	 *            ID of the activity whose suspended state is being set.
	 * 
	 * @param iSuspended
	 *            Indicates if the activity is suspended (<code>true
	 *                   </code>)
	 *            or not (<code>false</code>).
	 */
	public void reportSuspension(String iID, boolean iSuspended);

	/**
	 * This method is used to inform the sequencer of a change to an activity's
	 * current attempt experienced duration.
	 * 
	 * @param iID
	 *            ID of the activity being affected.
	 * 
	 * @param iDur
	 *            Indicates the experienced duration of the current attempt.
	 * 
	 */
	public void setAttemptDuration(String iID, IDuration iDur);

	/**
	 * This method is used to inform the sequencer to clear one of the
	 * activity's objective's measures -- set it to 'unknown'.
	 * 
	 * @param iID
	 *            ID of the activity whose measure has changed.
	 * 
	 * @param iObjID
	 *            ID of the objective whose measure has changed.
	 * 
	 */
	public void clearAttemptObjMeasure(String iID, String iObjID);

	/**
	 * This method is used to inform the sequencer of a change to one of the
	 * activity's objective's measures.
	 * 
	 * @param iID
	 *            ID of the activity whose measure has changed.
	 * 
	 * @param iObjID
	 *            ID of the objective whose measure has changed.
	 * 
	 * @param iMeasure
	 *            New value for the objective's measure.
	 * 
	 */
	public void setAttemptObjMeasure(String iID, String iObjID, double iMeasure);

	/**
	 * This method is used to inform the sequencer of a change to one of the
	 * activity's objective's satisfaction status.
	 * 
	 * @param iID
	 *            ID of the activity whose status has changed.
	 * 
	 * @param iObjID
	 *            ID of the objective whose satisfaction has changed.
	 * 
	 * @param iStatus
	 *            New value for the objective's satisfaction status. Valid
	 *            values are 'unknown', 'satisfied, 'notsatisfied'.
	 * 
	 */
	public void setAttemptObjSatisfied(String iID, String iObjID, String iStatus);

	/**
	 * This method is used to inform the sequencer of a change to the activity's
	 * progress status.
	 * 
	 * @param iID
	 *            ID of the activity whose progress status has changed.
	 * 
	 * @param iProgress
	 *            New value for the activity's progress status. Valid values
	 *            are: 'unknown', 'completed', 'incomplete'.
	 * 
	 */
	public void setAttemptProgressStatus(String iID, String iProgress);

	/**
	 * This method is used to inform the sequencer that a 'Choice' navigation
	 * request has occured.
	 * 
	 * @param iTarget
	 *            ID (<code>String</code>) of the target activity.
	 * 
	 * @return Information about the 'Next' activity to delivery or a processing
	 *         error.
	 */
	public ILaunch navigate(String iTarget);

	/**
	 * This method is used to inform the sequencer that a navigation request,
	 * other than 'Choice' has occured.
	 * 
	 * @param iRequest
	 *            Indicates which navigation request should be processed.
	 * 
	 * @return Information about the 'Next' activity to delivery or a processing
	 *         error.
	 */
	public ILaunch navigate(int iRequest);

	/*
	 * This is a method inserted for the Sakai SCORM implementation
	 * 
	 * 
	 */
	//public TreeModel getTreeModel(ISeqActivity iStart);

	
	
}