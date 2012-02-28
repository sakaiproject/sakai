/*******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you 
** ("Licensee") a non-exclusive, royalty free, license to use, modify and 
** redistribute this software in source and binary code form, provided that 
** i) this copyright notice and license appear on all copies of the software; 
** and ii) Licensee does not utilize the software in a manner which is 
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL 
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING 
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS 
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO 
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, 
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE 
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH 
** DAMAGES.
**
*******************************************************************************/

package org.adl.sequencer;

import java.util.List;

/**
 * Provides a common interface to the access the sequencing information  
 * associated with one node of the conceptual activity tree.<br><br>
 * 
 * <strong>Filename:</strong> SeqActivityStateAccess.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This interface provides common accessor methods to the nodes of an
 * activity tree.  During the internal sequencing processes, the sequencer may
 * request information from activities in the current context (the currently
 * active branch of the activity tree) to perform the sequencing behaviors
 * defined by the IMS SS Specification.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE.<br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * It is the responsibility of the requester to utilize any information
 * provided to establish sequencing behaviors as described in the IMS SS
 * Specification.<br>
 * Sequencing and Rollup Rule evaluation behavior are embedded in the helper
 * objects, <code>SeqRuleset</code>, <code>SeqRule</code>, 
 * <code>SeqRollupRuleset</code>, <code>SeqRollupRule</code.  Implementations of
 * this interface must construct the cooresponding objects from internal state
 * and provide them to the requester.
 * <br>
 * 
 * <strong>Known Problems:</strong><br>
 * 
 * <strong>Side Effects:</strong><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS 1.0
 *     <li>SCORM 2004 3rd Edtion
 * </ul>
 * 
 * @author ADL Technical Team
 */
interface SeqActivityStateAccess {

	// The following accessors provide information included in the Sequencing
	// Definition Model
	// ----------------------------------------------------------------------

	/**
	 * Retrieves the value of the limitCondition.activiyAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.8</b>) for this
	 * activity.
	 * 
	 * @return The absolute duration limit for an activity.
	 */
	String getActivityAbDur();

	/**
	 * Retrieves the value of the activiyAbsoluteDurationLimitControl
	 * Sequencing Definition Model Element (<b>element 3.7</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.activiyAbsoluteDurationLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 */
	boolean getActivityAbDurControl();

	/**
	 * Retrieves the value of the limitCondition.activityExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.10</b>)
	 * for this activity.
	 * 
	 * @return The experienced duration limit for an activity.
	 */
	String getActivityExDur();

	/**
	 * Retrieves the value of the activityExperiencedDurationLimitControl
	 * Sequencing Definition Model Element (<b>element 3.9</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if
	 *         limitCondition.activityExperiencedDurationLimit is defined for
	 *         this activity, otherwise <code>false</code>.
	 */
	boolean getActivityExDurControl();

	/**
	 * Retrieves the value of the limitCondition.attemptAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.4</b>)
	 * for this activity.
	 * 
	 * @return The absolute duration limit for an attempt on the activity.
	 */
	String getAttemptAbDur();

	/**
	 * Retrieves the value of the attemptAbsoluteDurationLimitControl
	 * Sequencing Definition Model Element (<b>element 3.3</b>) for this 
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.attemptAbsoluteDurationLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 */
	boolean getAttemptAbDurControl();

	/**
	 * Retrieves the value of the limitCondition.attemptExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.6</b>)
	 * for this activity.
	 * 
	 * @return The experienced duration limit for an attempt on the activity.
	 */
	String getAttemptExDur();

	/**
	 * Retrieves the value of the attemptExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.5</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if
	 *         limitCondition.attemptExperiencedDurationLimit is defined for
	 *         this activity, otherwise <code>false</code>.
	 */
	boolean getAttemptExDurControl();

	/**
	 * Retrieves the value of the limitCondition.attemptLimit Sequencing
	 * Definition Model Element (<b>element 3.2</b>) for this activity.
	 * 
	 * @return The maximum attempts (<code>long</code>) that has been defined
	 *         for this activity, or <code>-1</code> if none have been
	 *         defined.
	 */
	long getAttemptLimit();

	/**
	 * Retrieves the value of the limitCondition.attemptLimitControl Sequencing
	 * Definition Model Element (<b>element 3.1</b> for this activity.
	 * 
	 * @return <code>true</code> if limitCondition.attemptLimit is defined for
	 *         this activity, otherwise <code>false</code>.
	 */
	boolean getAttemptLimitControl();

	/**
	 * Retrieves the value of the set of Auxiliary Resource Sequencing
	 * Definition Model Elements (<b>element 4</b>) for this activity.
	 * 
	 * @return The set (<code>Vector</code> of <code>ADLAuxiliaryResource</code>
	 *         objects) of auxiliary resource assoiciated with the activity.
	 */
	List<ADLAuxiliaryResource> getAuxResources();

	/**
	 * Retrieves the value of the limitCondition.beginTimeLimit Sequencing
	 * Definition Model Element (<b>element 3.12</b>) for this activity.
	 * 
	 * @return The time limit when an activity may begin.
	 */
	String getBeginTimeLimit();

	/**
	 * Retrieves the value of the limitCondition.beginTimeLimitControl
	 * Sequencing Definition Model Element (<b>element 3.11</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.beginTimeLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 */
	boolean getBeginTimeLimitControl();

	/**
	 * Retrieves the value of the Choice Constraint Constrain Choice Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @return <code>true</code> if the 'constrainChoice' is defined for this
	 *         activity,  otherwise <code>false</code>.
	 */
	boolean getConstrainChoice();

	/**
	 * Retrieves the value of the ControlMode.ForwardOnly Sequencing Definition
	 * Model Element (<b>element 1.4</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'ForwardOnly' is defined for this
	 *         cluster,  otherwise <code>false</code>.
	 */
	boolean getControlForwardOnly();

	/**
	 * Retrieves the value of the ControlMode.Choice Sequencing Definition
	 * Model Element (<b>element 1.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'Choice' is enabled for this cluster,
	 *         otherwise <code>false</code>.
	 */
	boolean getControlModeChoice();

	/**
	 * Retrieves the value of the ControlMode.ChoiceExit Sequencing Definition
	 * Model Element (<b>element 1.2</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'ChoiceExit' is enabled for this cluster,
	 *         otherwise <code>false</code>.
	 */
	boolean getControlModeChoiceExit();

	/**
	 * Retrieves the value of the ControlMode.Flow Sequencing Definition Model
	 * Element (<b>element 1.3</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'Flow' is enabled for this cluster,
	 *         otherwise <code>false</code>.
	 */
	boolean getControlModeFlow();

	/**
	 * Retrieves the value of the Delivery Mode.
	 * 
	 * @return The DeliveryMode (<code>String</code>) for this activity.
	 */
	String getDeliveryMode();

	/**
	 * Retrieves the value of the limitCondition.endTimeLimit Sequencing
	 * Definition Model Element (<b>element 3.14</b>) for this activity.
	 * 
	 * @return The time limit by which an activity must end.
	 */
	String getEndTimeLimit();

	/**
	 * Retrieves the value of the limitCondition.endTimeLimitControl
	 * Sequencing Definition Model Element (<b>element 3.13</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.endTimeLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 */
	boolean getEndTimeLimitControl();

	/**
	 * Retrieves the Exit Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, exit action subset) defined for this activity.
	 * 
	 * @return The Exit Action Sequencing Rules (<code>SeqRuleset</code>)
	 *         defined for this activity, or <code>null</code> if no exit action
	 *         rules have been defined.
	 */
	ISeqRuleset getExitSeqRules();

	/**
	 * Retrieves the activity ID of this activity.
	 * 
	 * @return The unique ID (<code>String</code>) of this resource.<br>
	 *         NOTE: This will not be <code>null</code>.
	 */
	String getID();

	/**
	 * Retrieve this activity's 'IsActive' status.
	 * 
	 * @return <code>true</code> if this activity is assumed to be 'active',
	 *         otherwise <code>false</code>
	 */
	boolean getIsActive();

	/**
	 * Retrieves the value of the Rollup Objective Satisfied Sequencing
	 * Definition Model Element (<b>element 8.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if objective status for this activity should
	 *         be considered during rollup for its parent, or
	 *         <code>false</code> if it should not be considered.
	 */
	boolean getIsObjRolledUp();

	/**
	 * Retrieves the value of the Rollup Progress Completion Sequencing
	 * Definition Model Element (<b>element 8.3</b>) for this activity.
	 * 
	 * @return <code>true</code> if completion status for this activity should
	 *         be considered during rollup for its parent, or
	 *         <code>false</code> if it should not be considered.
	 */
	boolean getIsProgressRolledUp();

	/**
	 * Retrieve this activity's 'IsSuspended' status.
	 * 
	 * @return <code>true</code> if this activity is assumed to be 'suspended',
	 *         otherwise <code>false</code>
	 */
	boolean getIsSuspended();

	/**
	 * Retrieves the value of the DeliveryControl.isTracked Sequencing Definition
	 * Model Element (<b>element 11.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if the activity is tracked, otherwise
	 *         <code>false</code>.
	 */
	boolean getIsTracked();

	/**
	 * Retrieve this activity's IsVisible status.
	 * 
	 * @return <code>true</code> if this activity is 'visible', otherwise
	 *         <code>false</code>
	 */
	boolean getIsVisible();

	/**
	 * Retrives the a learner ID associated with this activity
	 * 
	 * @return The ID (<code>String</code>> of the learner associated with
	 *         this activity.
	 */
	String getLearnerID();

	/**
	 * Retrieves the value of the set of Objective Definition Sequencing
	 * Definition Model Elements (<b>elements 6 and 7</b>) for this activity.
	 * 
	 * @return The set of objectives assoiciated with the activity.  These
	 *         objectivees are returned as a <code>Vector</code> of
	 *         <code>SeqObjective</code> objects.
	 */
	List<SeqObjective> getObjectives();

	/**
	 * Retrieves the value of the Rollup Objective Measure Weight Sequencing
	 * Definition Model Element (<b>element 8.2</b>) for this activity.
	 * 
	 * @return A <code>double</code> value from 0.0 to 1.0, describing the
	 *         weight this activity's score will have during rollup.
	 */
	double getObjMeasureWeight();

	/**
	 * Retrieves the Post Condition Action Rules, Sequencing Definition Model
	 * Element (<b>element 2</b>, post condition action subset) defined for
	 * this activity.
	 * 
	 * @return The Post Condition Action Sequencing Rules 
	 *         (<code>SeqRuleset</code>) defined for this activity, or
	 *         <code>null</code> if no post condition rules have been defined.
	 */
	ISeqRuleset getPostSeqRules();

	/**
	 * Retrieves the Precondition Action Rules, Sequencing Definition Model
	 * Element (<b>element 2</b>, precondition action subset) defined for
	 * this activity.
	 * 
	 * @return The Precondition Action Sequencing Rules (<code>SeqRuleset</code>)
	 *         defined for this activity, or <code>null</code> if no precondition
	 *         rules have been defined.
	 */
	ISeqRuleset getPreSeqRules();

	/**
	 * Retrieves the value of the Choice Constraint Prevent Activation Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @return <code>true</code> if the 'preventActivation' is defined for this
	 *         activity,  otherwise <code>false</code>.
	 */
	boolean getPreventActivation();

	/**
	 * Retrieves the value of the RandomizationControl.RandomizationTiming
	 * Sequencing Definition Model Element (<b>element 10.1</b>) for
	 * this activity.
	 * 
	 * @return When the randomization process should be applied to this
	 *         activity.
	 */
	String getRandomTiming();

	/**
	 * Retrieves the value of the RandomizationControl.RandomizeChildren
	 * Sequencing Definition Model Element (<b>element 10.2</b>) for
	 * this activity.
	 * 
	 * @return If the children of this activty should be reordered when the
	 *         randomization process is applied (<code>boolean</code>).
	 */
	boolean getReorderChildren();

	/**
	 * Retrieves Complete Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 */
	String getRequiredForCompleted();

	/**
	 * Retrieves Incomplete Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 */
	String getRequiredForIncomplete();

	/**
	 * Retrieves Not Satisfied Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 */
	String getRequiredForNotSatisfied();

	/**
	 * Retrieves Satisfied Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 */
	String getRequiredForSatisfied();

	/**
	 * Retrieves the ID of the resource associated with this activity.
	 * 
	 * @return The ID (<code>String</code>) of the resource associated with this
	 *         activity, or <code>null</code> if the activity does not have a
	 *         resource.
	 */
	String getResourceID();

	/**
	 * Retrieves the Rollup Rules, Sequencing Definiition Model Element
	 * (<b>element 5</b>) defined for this activity.
	 * 
	 * @return The Rollup Rules (<code>SeqRollupRuleset</code>) defined for this
	 *         activity, or <code>null</code> if no rollup rules have been
	 *         defined.
	 */
	ISeqRollupRuleset getRollupRules();

	/**
	* Describes if measure should be used to evaluate satisfaction if the
	* activity is active.
	* 
	* @return Indicates if measure should be used to evaluate satisfaction if 
	*         the activity is active.
	*/
	boolean getSatisfactionIfActive();

	/**
	 * Retrives the a scope ID associated with this activity.
	 * 
	 * @return The ID (<code>String</code>> of the scope associated with
	 *         this activity's objectives.
	 */
	String getScopeID();

	/**
	 * Retrieves the value of the SelectCount Sequencing Definition Model
	 * Element (<b>element 9.3</b>) for this activity.
	 * 
	 * @return The size of the random set of children to be selected from
	 *         from this activity
	 */
	int getSelectCount();

	/**
	 * Retrieves the value of the SelectionControl.SelectionTiming
	 * Sequencing Definition Model Element (<b>element 9.1</b>) for
	 * this activity.
	 * 
	 * @return When the selectiion process should be applied to this activity.
	 */
	String getSelectionTiming();

	/**
	 * Retrieves the value of the SelectCountStatus Sequencing Definition Model
	 * Element (<b>element 9.2</b>) for this activity.
	 * 
	 * @return <code>true</code> if the value of Selection Count is valid, 
	 * otherwise <code>false</code>
	 */
	boolean getSelectStatus();

	/**
	 * Retrieves the value of the DeliveryControl.CompletionSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.2</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if the activity communicates its progress
	 *         status information; <code>false</code> if the LMS should set the
	 *         activity's progress status.
	 */
	boolean getSetCompletion();

	/**
	 * Retrieves the value of the DeliveryControl.ObjectiveSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.3</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if the activity communicates its objective
	 *         status information; <code>false</code> if the LMS should set the
	 *         activity's objective status.
	 */
	boolean getSetObjective();

	/**
	 * Retrieves the ID of the activity's associated persistent state.
	 * 
	 * @return The ID (<code>String</code>) of the persistent state
	 *         associated with this activity.
	 */
	String getStateID();

	/**
	 * Retrieves the user-readable title of this activity.
	 * 
	 * @return The user-readable title (<code>String</code>) of this activity, or
	 *         <code>null</code> if the activity does not have a title.
	 */
	String getTitle();

	/**
	 * Retrieves the value of the ControlMode.useCurrentAttemptObjectiveInfo
	 * Sequencing Definition Model Element (<b>element 1.5</b>)
	 * for this activity.
	 * 
	 * @return <code>true</code> if the 'useCurrentAttemptObjectiveInfo'
	 *         is defined for this cluster, otherwise <code>false</code>.
	 */
	boolean getUseCurObjective();

	/**
	 * Retrieves the value of the ControlMode.useCurrentAttemptProgressInfo
	 * Sequencing Definition Model Element (<b>element 1.6</b>)
	 * for this activity.
	 * 
	 * @return <code>true</code> if the 'useCurrentAttemptProgressInfo'
	 *         is defined for this cluster, otherwise <code>false</code>.
	 */
	boolean getUseCurProgress();

	/**
	 * Retrieves this activity's XML fragment of sequencing information.
	 * 
	 * @return The XML fragment.
	 */
	String getXMLFragment();

	/**
	 * Sets the value of the LimitCondition.activityAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.8</b>) for this
	 * activity.
	 * 
	 * @param iDur   The absolute duration (<code>String</code>) for an activity.
	 */
	void setActivityAbDur(String iDur);

	/**
	 * Sets the value of the LimitCondition.activityExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.10</b>) for this
	 * activity.
	 * 
	 * @param iDur   The experienced duration (<code>String</code>) for an 
	 *               activity.
	 */
	void setActivityExDur(String iDur);

	/**
	 * Sets the value of the LimitCondition.attemptAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.4</b>) for this
	 * activity.
	 * 
	 * @param iDur   The absolute duration (<code>String</code>) for an attempt
	 *               on the activity.
	 */
	void setAttemptAbDur(String iDur);

	/**
	 * Sets the value of the LimitCondition.attemptExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.6</b>) for this
	 * activity.
	 * 
	 * @param iDur   The experienced duration (<code>String</code>) for an 
	 *               attempt on the activity.
	 */
	void setAttemptExDur(String iDur);

	/**
	 * Sets the value of the LimitCondtitions.attemptLimit Sequencing
	 * Definition Model Element (<b>element 3.2</b>) for this activity.
	 * 
	 * @param iMaxAttempt The maximum number attempts allowed for this activity.
	 */
	void setAttemptLimit(Long iMaxAttempt);

	/**
	 * Sets the value of the Auxiliary Resource Sequencing Definition
	 * Model Element (<b>element 4</b>) for this activity.
	 * 
	 * @param iRes   The set (<code>Vector</code>) of auxiliary resources
	 *               (<code>ADLAuxiliaryResource</code>) associated with this
	 *               activity.
	 */
	void setAuxResources(List<ADLAuxiliaryResource> iRes);

	/**
	 * Sets the value of the LimitCondition.beginTimeLimit Sequencing Definition
	 * Model Element (<b>element 3.12</b>) for this activity.
	 * 
	 * @param iTime  The time (<code>String</code>), before which, an attempt
	 *               on the activity cannot begin.
	 */
	void setBeginTimeLimit(String iTime);

	/**
	 * Sets the value of the Choice Constraint Constrain Choice Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @param iConstrainChoice <code>true</code> to enforce 'constrainChoice' for 
	 *                     this activity or <code>false</code> to prevent.
	 */
	void setConstrainChoice(boolean iConstrainChoice);

	/**
	 * Sets the value of the ControlMode.ForwardOnly Sequencing Definition Model 
	 * Element (<b>element 1.4</b>) for this activity.
	 * 
	 * @param iForwardOnly <code>true</code> to enforce 'ForwardOnly' for this
	 *                     cluster or <code>false</code> to disable.
	 */
	void setControlForwardOnly(boolean iForwardOnly);

	/**
	 * Sets the value of the ControlMode.Choice Sequencing Definition Model
	 * Element (<b>element 1.1</b>) for this activity.
	 * 
	 * @param iChoice <code>true</code> to enable 'Choice' for this cluster or
	 *                <code>false</code> to disable.
	 */
	void setControlModeChoice(boolean iChoice);

	/**
	 * Sets the value of the ControlMode.ChoiceExit Sequencing Definition Model
	 * Element (<b>element 1.2</b>) for this activity.
	 * 
	 * @param iChoiceExit <code>true</code> to enable 'ChoiceExit' for this
	 *                    or <code>false</code> to disable.
	 */
	void setControlModeChoiceExit(boolean iChoiceExit);

	/**
	 * Sets the value of the ControlMode.Flow Sequencing Definition Model
	 * Element (<b>element 1.3</b>) for this activity.
	 * 
	 * @param iFlow  <code>true</code> to enable 'Flow' for this cluster or
	 *               <code>false</code> to disable.
	 */
	void setControlModeFlow(boolean iFlow);

	/**
	 * Sets the value of the Delivery Mode for this activity.
	 * 
	 * @param iDeliveryMode The Delivery Mode for this activity.
	 */
	void setDeliveryMode(String iDeliveryMode);

	/**
	  * Sets the value of the LimitCondition.endTimeLimit Sequencing Definition
	  * Model Element (<b>element 3.14</b>) for this activity.
	  * 
	  * @param iTime  The time (<code>String</code>), after which, an attempt
	  *               on the activity is invalid.
	  */
	void setEndTimeLimit(String iTime);

	/**
	 * Sets the Exit Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, exit action subset) defined for this activity.
	 * 
	 * @param iRuleSet The set of Exit Action Sequencing Rules
	 *                 (<code>SeqRuleset</code>) defined for this activity.
	 */
	void setExitSeqRules(ISeqRuleset iRuleSet);

	/**
	 * Sets the ID of this activity.
	 * 
	 * @param iID   The activity's ID.
	 */
	void setID(String iID);

	/**
	 * Set this activity's 'IsActive' status.
	 * 
	 * @param iActive Indicates that the activity is assumed to be 'Active'.
	 */
	void setIsActive(boolean iActive);

	/**
	 * Sets the value of the Rollup Objective Satisfied Sequencing
	 * Definition Model Element (<b>element 8.1</b>) for this activity.
	 * 
	 * @param iRolledup <code>true</code> if this activity should contribute
	 *                  mastery status to its parent during rollup; otherwise
	 *                  <code>false</code>.
	 */
	void setIsObjRolledUp(boolean iRolledup);

	/**
	 * Sets the value of the Rollup Progress Completion Sequencing
	 * Definition Model Element (<b>element 8.3</b>) for this activity.
	 * 
	 * @param iRolledup <code>true</code> if this activity should contribute
	 *                  completion status to its parent during rollup; otherwise
	 *                  <code>false</code>.
	 */
	void setIsProgressRolledUp(boolean iRolledup);

	/**
	 * Set this activity's 'IsSuspended' status.
	 * 
	 * @param iSuspended Is the activity assumed to be 'suspended'?
	 */
	void setIsSuspended(boolean iSuspended);

	/**
	 * Sets the value of the DeliveryControl.isTracked Sequencing Definition
	 * Model Element (<b>element 11.1</b>)  for this activity.
	 * 
	 * @param iTracked <code>true</code> if Tracking Status informatin should be
	 *                 maintained for this activity.
	 */
	void setIsTracked(boolean iTracked);

	/**
	 * Set this activity's 'IsVisible' status.
	 * 
	 * @param iIsVisible the new status for IsVisible
	 */
	void setIsVisible(boolean iIsVisible);

	/**
	 * Associates an ID with learner of this activity
	 * 
	 * @param iLearnerID The ID of the learner associated with this activity.
	 */
	void setLearnerID(String iLearnerID);

	/**
	 * Sets the value of the Objectives Resource Sequencing Definition
	 * Model Elements (<b>elements 6 and 7</b>) for this activity.
	 * 
	 * @param iObjs  The set (<code>Vector</code> of <code>SeqObjective</code>)
	 *               of objectives(s) associated with this activity.
	 */
	void setObjectives(List<SeqObjective> iObjs);

	/**
	 * Sets the value of the Rollup Objective Measure Weight Sequencing
	 * Definition Model Element (<b>element 8.2</b>) for this activity.
	 * 
	 * @param iWeight A value from 0.0 to 1.0 describing the
	 *                weight this activity's score will have during rollup.
	 */
	void setObjMeasureWeight(double iWeight);

	// The following accessors provide additional activity state information 
	// that is not included in the Sequencing Definition Model
	// ----------------------------------------------------------------------

	/**
	 * Sets the Post Condition Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, post condition action subset) defined for this
	 * activity.
	 * 
	 * @param iRuleSet The set of Post Condition Action Sequencing Rules
	 *                 (<code>SeqRuleset</code>) defined for this activity.
	 */
	void setPostSeqRules(ISeqRuleset iRuleSet);

	/**
	 * Sets the Precondition Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, precondition action subset) defined for this
	 *  activity.
	 * 
	 * @param iRuleSet The set of Precondition Action Sequencing Rules
	 *                 (<code>SeqRuleset</code>) defined for this activity.
	 */
	void setPreSeqRules(ISeqRuleset iRuleSet);

	/**
	 * Sets the value of the Choice Constraint Prevent Activation Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @param iPreventActivation <code>true</code> to enforce 'preventActivation' for 
	 *                     this activity or <code>false</code> to allow.
	 */
	void setPreventActivation(boolean iPreventActivation);

	/**
	 * Sets the value of the RandomizationControls.RandomizationTiming Sequencing
	 * Definition Model element (<b>element 10.1</b>) for this activity.
	 * 
	 * @param iTiming Indicates when the randomization process should be applied
	 *                to this activity.
	 */
	void setRandomTiming(String iTiming);

	/**
	 * Sets the value of the RandomizationControl.RandomizeChildren 
	 * Sequencing Definition Model Element (<b>element 10.2</b>) for
	 * this activity.
	 * 
	 * @param iReorder Indicates if children of this activity should be
	 *                 reordered when the randomization process is applied to
	 *                 this activity.
	 */
	void setReorderChildren(boolean iReorder);

	/**
	 * Sets the Complete Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 */
	void setRequiredForCompleted(String iConsider);

	/**
	 * Sets the Incomplete Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 */
	void setRequiredForIncomplete(String iConsider);

	/**
	 * Sets the Not Satisfied Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 */
	void setRequiredForNotSatisfied(String iConsider);

	/**
	 * Sets the Satisfied Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 */
	void setRequiredForSatisfied(String iConsider);

	/**
	 * Sets the ID of the resource associated with this activity.
	 * 
	 * @param iResourceID The ID (<code>String</code>) of the resource.
	 */
	void setResourceID(String iResourceID);

	/**
	 * Sets the Rollup Rules Sequencing Definition Model Elements
	 * (<b>element 5</b>) defined for this activity.
	 * 
	 * @param iRuleSet The set (<code>SeqRollupRuleset</code>) of Rollup Rules
	 *                 defined for this activity.
	 */
	void setRollupRules(ISeqRollupRuleset iRuleSet);

	/**
	 * Indicates if measure should be used to evaluate satisfaction if the
	 * activity is active.
	 * 
	 * @param iActiveMeasure Indicates if measure should be used to evaluate 
	 *                       satisfaction if the activity is active.
	 */
	void setSatisfactionIfActive(boolean iActiveMeasure);

	/**
	 * Associates an ID with scope of this activity's objectives
	 * 
	 * @param iScopeID The ID of the scope associated with the objectives
	 */
	void setScopeID(String iScopeID);

	/**
	 * Sets the value of the SelectCount Sequencing Definition Model
	 * Element (<b>element 9.3</b>) for this activity.
	 * 
	 * @param iCount Indicates the number of children to be selected when
	 *               the selection process is applied to this activity.
	 */
	void setSelectCount(int iCount);

	/**
	 * Sets the value of the SelectionControl.SelectionTiming Sequencing
	 * Definition Model element (<b>element 9.1</b>) for this activity.
	 * 
	 * @param iTiming Indicates when the selection process should be applied
	 *                to this activity.
	 */
	void setSelectionTiming(String iTiming);

	/**
	 * Sets the value of the DeliveryControl.CompletionSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.2</b>) for this
	 * activity.
	 * 
	 * @param iSet   <code>true</code> if the activity communicates its progress
	 *               status information.
	 */
	void setSetCompletion(boolean iSet);

	// The following package accessors provide acess to the activity's state.   
	// These methods are only called by the sequencing subprocesses.
	// ----------------------------------------------------------------------

	/**
	 * Sets the value of the DeliveryControl.ObjectiveSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.3</b>) for this
	 * activity.
	 * 
	 * @param iSet   <code>true</code> if the activity communicates its objective
	 *               status information.
	 */
	void setSetObjective(boolean iSet);

	/**
	 * Sets the ID of the activity's associated persisent state.
	 * 
	 * @param iStateID The ID (<code>String</code>) of the activity's persistent
	 *                 state information.
	 */
	void setStateID(String iStateID);

	/**
	 * Sets the user-readable title for this activity.
	 * 
	 * @param iTitle The user-readable title for this activity.
	 */
	void setTitle(String iTitle);

	/**
	 * Sets the value of the ControlMode.useCurrentAttemptObjectiveInfo
	 * Sequencing Definition Model Element (<b>element 1.5</b>) for this
	 * activity.
	 * 
	 * @param iUseCur <code>true</code> to enforce that objective
	 *                information for children will only be used during
	 *                sequencing if that information was obtained withn the
	 *                context of the current attempt on the activity, or
	 *                <code>false</code> to disable.
	 */
	void setUseCurObjective(boolean iUseCur);

	/**
	 * Sets the value of the ControlMode.useCurrentAttemptProgressInfo
	 * Sequencing Definition Model Element (<b>element 1.6</b>) for this
	 * activity.
	 * 
	 * @param iUseCur <code>true</code> to enforce that progress
	 *                information for children will only be used during
	 *                sequencing if that information was obtained withn the
	 *                context of the current attempt on the activity, or
	 *                <code>false</code> to disable.
	 */
	void setUseCurProgress(boolean iUseCur);

	/**
	  * Set this activity's XML fragment of sequencing information.
	  * 
	  * @param iXML Contains the XML fragment.
	  */
	void setXMLFragment(String iXML);

} // end SeqActivityStateAccess
