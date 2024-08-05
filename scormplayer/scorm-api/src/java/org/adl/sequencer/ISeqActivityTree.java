package org.adl.sequencer;

import java.util.List;

public interface ISeqActivityTree {

	/**
	 * Clears the current state of the activity tree, except for its SuspsendAll
	 * state.
	 */
	public void clearSessionState();

	/**
	 * This method provides the state this <code>SeqActivityTree</code> object
	 * for diagnostic purposes.
	 */
	public void dumpState();

	/**
	 * Retrieves the activity (<code>SeqActivity</code>) associated with the
	 * ID requested
	 * 
	 * @param iActivityID The activity id
	 * 
	 * @return The activity (<code>SeqActivity</code>) associated with the ID
	 *         request or <code>null</code> if no activity with that ID exists in
	 *         current activity tree.
	 */
	public ISeqActivity getActivity(String iActivityID);

	public long getContentPackageId();

	/**
	 * Retrieves ID of the course associated with this activity tree.
	 * 
	 * @return The ID (<code>String</code>) of the course associated with this
	 *         activity tree or <code>null</code> if none has been assigned.
	 */
	public String getCourseID();

	/**
	 * Retrieves the set of global objective IDs used in this activity tree.
	 * 
	 * @return The set of global objective IDs <code>Vector</code> or <code>
	 *         null</code> if none exist.
	 */
	public List<String> getGlobalObjectives();

	/**
	 * Retrieves ID of the learner associated with this activity tree.
	 * 
	 * @return The ID (<code>String</code>) of the learner associated with this
	 *         activity tree or <code>null</code> if none has been assigned.
	 */
	public String getLearnerID();

	/**
	 * Retrieves the set of activities in the activity tree that read from a
	 * specified global shared objective
	 * 
	 * @param iObjID objective id
	 * 
	 * @return The set of activity IDs <code>Vector</code> or <code>null</code>
	 *         if none exist.
	 */
	public List<String> getObjMap(String iObjID);

	/**
	 * Retrieves ID of the course associated with the scope of this activity
	 * tree's objectives.
	 * 
	 * @return The ID (<code>String</code>) of associated with this activity
	 *         tree's objectives, or <code>null</code> if the objectives are
	 *         global to the system.
	 */
	public String getScopeID();

	/**
	 * Retrieves the activity (<code>SeqActivity</code>) associated with the
	 * last attempted activity before a 'SuspendAll' sequencing request.
	 * 
	 * @return The activity (<code>SeqActivity</code>) associated with the last
	 *         attempted activity or <code>null</code> if none exists.
	 */
	public ISeqActivity getSuspendAll();

	public void setContentPackageId(long contentPackageId);

	/**
	 * Sets the ID of the course associated with this activity tree.
	 * 
	 * @param iCourseID Describes the course ID
	 */
	public void setCourseID(String iCourseID);

	/**
	 * Sets 'current' activity for this activity tree.  The determination of
	 * which activity is 'current' is performed by the sequencer.  This
	 * information is maintained by the activity tree to allow for persistence.
	 * 
	 * @param iCurrent The 'current' activity (<code>SeqActivity</code>).
	 */
	public void setCurrentActivity(ISeqActivity iCurrent);

	/**
	 * Sets first candidate activity for processing sequencing requests.  The   
	 * determination of this activity is performed by the sequencer.  This
	 * information is maintained by the activity tree to allow for persistence.
	 * 
	 * @param iFirst The first candidate activity (<code>SeqActivity</code>).
	 */
	public void setFirstCandidate(ISeqActivity iFirst);

	/**
	 * Sets the ID of the learner associated with this activity tree.
	 * 
	 * @param iLearnerID The ID of the student associated with this tree
	 */
	public void setLearnerID(String iLearnerID);

	/**
	 * Sets the scope of the global objectives managed by this activity tree.
	 * 
	 * @param iScopeID Indicates the ID of the scope associated with this
	 *                 activity tree's objectives, or <code>null</code> of the
	 *                 objectives are global to the system.
	 */
	public void setScopeID(String iScopeID);

	/**
	 * Set the count of all activities in the activity tree.
	 */
	//public void setDepths();

	/**
	 * Set the count of all activities in the activity tree.
	 */
	//public void setTreeCount();

}