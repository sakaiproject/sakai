package org.adl.sequencer;

import java.io.Serializable;
import java.util.List;

public interface ISeqActivity extends Serializable {
	
	/**
	 * Retrieves the value of the ControlMode.Choice Sequencing Definition
	 * Model Element (<b>element 1.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'Choice' is enabled for this cluster,
	 *         otherwise <code>false</code>.
	 * 
	 */
	public boolean getControlModeChoice();

	/**
	 * Sets the value of the ControlMode.Choice Sequencing Definition Model
	 * Element (<b>element 1.1</b>) for this activity.
	 * 
	 * @param iChoice <code>true</code> to enable 'Choice' for this cluster or
	 *                <code>false</code> to disable.
	 */
	public void setControlModeChoice(boolean iChoice);

	/**
	 * Retrieves the value of the ControlMode.ChoiceExit Sequencing Definition
	 * Model Element (<b>element 1.2</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'ChoiceExit' is enabled for this cluster,
	 *         otherwise <code>false</code>.
	 * 
	 */
	public boolean getControlModeChoiceExit();

	/**
	 * Sets the value of the ControlMode.ChoiceExit Sequencing Definition Model
	 * Element (<b>element 1.2</b>) for this activity.
	 * 
	 * @param iChoiceExit <code>true</code> to enable 'ChoiceExit' for this
	 *                    or <code>false</code> to disable.
	 * 
	 */
	public void setControlModeChoiceExit(boolean iChoiceExit);

	/**
	 * Retrieves the value of the ControlMode.Flow Sequencing Definition Model
	 * Element (<b>element 1.3</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'Flow' is enabled for this cluster,
	 *         otherwise <code>false</code>.
	 * 
	 */
	public boolean getControlModeFlow();

	/**
	 * Sets the value of the ControlMode.Flow Sequencing Definition Model
	 * Element (<b>element 1.3</b>) for this activity.
	 * 
	 * @param iFlow  <code>true</code> to enable 'Flow' for this cluster or
	 *               <code>false</code> to disable.
	 * 
	 */
	public void setControlModeFlow(boolean iFlow);

	/**
	 * Retrieves the value of the ControlMode.ForwardOnly Sequencing Definition
	 * Model Element (<b>element 1.4</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'ForwardOnly' is defined for this
	 *         cluster,  otherwise <code>false</code>.
	 * 
	 */
	public boolean getControlForwardOnly();

	/**
	 * Sets the value of the ControlMode.ForwardOnly Sequencing Definition Model 
	 * Element (<b>element 1.4</b>) for this activity.
	 * 
	 * @param iForwardOnly <code>true</code> to enforce 'ForwardOnly' for this
	 *                     cluster or <code>false</code> to disable.
	 * 
	 */
	public void setControlForwardOnly(boolean iForwardOnly);

	/**
	 * Retrieves the value of the Choice Constraint Constrain Choice Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @return <code>true</code> if the 'constrainChoice' is defined for this
	 *         activity,  otherwise <code>false</code>.
	 * 
	 */
	public boolean getConstrainChoice();

	/**
	 * Sets the value of the Choice Constraint Constrain Choice Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @param iConstrainChoice <code>true</code> to enforce 'constrainChoice' for 
	 *                     this activity or <code>false</code> to prevent.
	 * 
	 */
	public void setConstrainChoice(boolean iConstrainChoice);

	/**
	 * Retrieves the value of the Choice Constraint Prevent Activation Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @return <code>true</code> if the 'preventActivation' is defined for this
	 *         activity,  otherwise <code>false</code>.
	 * 
	 */
	public boolean getPreventActivation();

	/**
	 * Sets the value of the Choice Constraint Prevent Activation Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @param iPreventActivation <code>true</code> to enforce 'preventActivation' for 
	 *                     this activity or <code>false</code> to allow.
	 * 
	 */
	public void setPreventActivation(boolean iPreventActivation);

	/**
	 * Retrieves the value of the ControlMode.useCurrentAttemptObjectiveInfo
	 * Sequencing Definition Model Element (<b>element 1.5</b>)
	 * for this activity.
	 * 
	 * @return <code>true</code> if the 'useCurrentAttemptObjectiveInfo'
	 *         is defined for this cluster, otherwise <code>false</code>.
	 *
	 */
	public boolean getUseCurObjective();

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
	 * 
	 */
	public void setUseCurObjective(boolean iUseCur);

	/**
	 * Retrieves the value of the ControlMode.useCurrentAttemptProgressInfo
	 * Sequencing Definition Model Element (<b>element 1.6</b>)
	 * for this activity.
	 * 
	 * @return <code>true</code> if the 'useCurrentAttemptProgressInfo'
	 *         is defined for this cluster, otherwise <code>false</code>.
	 *
	 */
	public boolean getUseCurProgress();

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
	 *
	 */
	public void setUseCurProgress(boolean iUseCur);

	/**
	 * Retrieves the Precondition Action Rules, Sequencing Definition Model
	 * Element (<b>element 2</b>, precondition action subset) defined for
	 * this activity.
	 * 
	 * @return The Precondition Action Sequencing Rules (<code>SeqRuleset</code>)
	 *         defined for this activity, or <code>null</code> if no precondition
	 *         rules have been defined.
	 *
	 */
	public ISeqRuleset getPreSeqRules();

	/**
	 * Sets the Precondition Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, precondition action subset) defined for this
	 *  activity.
	 * 
	 * @param iRuleSet The set of Precondition Action Sequencing Rules
	 *                 (<code>SeqRuleset</code>) defined for this activity.
	 * 
	 */
	public void setPreSeqRules(ISeqRuleset iRuleSet);

	/**
	 * Retrieves the Exit Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, exit action subset) defined for this activity.
	 * 
	 * @return The Exit Action Sequencing Rules (<code>SeqRuleset</code>)
	 *         defined for this activity, or <code>null</code> if no exit action
	 *         rules have been defined.
	 * 
	 */
	public ISeqRuleset getExitSeqRules();

	/**
	 * Sets the Exit Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, exit action subset) defined for this activity.
	 * 
	 * @param iRuleSet The set of Exit Action Sequencing Rules
	 *                 (<code>SeqRuleset</code>) defined for this activity.
	 * 
	 */
	public void setExitSeqRules(ISeqRuleset iRuleSet);

	/**
	 * Retrieves the Post Condition Action Rules, Sequencing Definition Model
	 * Element (<b>element 2</b>, post condition action subset) defined for
	 * this activity.
	 * 
	 * @return The Post Condition Action Sequencing Rules 
	 *         (<code>SeqRuleset</code>) defined for this activity, or
	 *         <code>null</code> if no post condition rules have been defined.
	 * 
	 */
	public ISeqRuleset getPostSeqRules();

	/**
	 * Sets the Post Condition Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, post condition action subset) defined for this
	 * activity.
	 * 
	 * @param iRuleSet The set of Post Condition Action Sequencing Rules
	 *                 (<code>SeqRuleset</code>) defined for this activity.
	 *
	 */
	public void setPostSeqRules(ISeqRuleset iRuleSet);

	/**
	 * Retrieves the value of the limitCondition.attemptLimitControl Sequencing
	 * Definition Model Element (<b>element 3.1</b> for this activity.
	 * 
	 * @return <code>true</code> if limitCondition.attemptLimit is defined for
	 *         this activity, otherwise <code>false</code>.
	 * 
	 */
	public boolean getAttemptLimitControl();

	/**
	 * Retrieves the value of the limitCondition.attemptLimit Sequencing
	 * Definition Model Element (<b>element 3.2</b> for this activity.
	 * 
	 * @return The maximum attempts (<code>long</code>) that has been defined
	 *         for this activity, or <code>-1</code> if none have been
	 *         defined.
	 *
	 */
	public long getAttemptLimit();

	/**
	 * Sets the value of the LimitCondtitions.attemptLimit Sequencing
	 * Definition Model Element (<b>element 3.2</b>) for this activity.
	 * 
	 * @param iMaxAttempt The maximum number attempts allowed for this activity.
	 *
	 */
	public void setAttemptLimit(Long iMaxAttempt);

	/**
	 * Retrieves the value of the attemptAbsoluteDurationLimitControl
	 * Sequencing Definition Model Element (<b>element 3.3</b>) for this 
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.attemptAbsoluteDurationLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 * 
	 */
	public boolean getAttemptAbDurControl();

	/**
	 * Retrieves the value of the limitCondition.attemptAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.4</b>)
	 * for this activity.
	 * 
	 * @return The absolute duration limit for an attempt on the activity.
	 * 
	 */
	public String getAttemptAbDur();

	/**
	 * Sets the value of the LimitCondition.attemptAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.4</b>) for this
	 * activity.
	 * 
	 * @param iDur   The absolute duration (<code>String</code>) for an attempt
	 *               on the activity.
	 * 
	 */
	public void setAttemptAbDur(String iDur);

	/**
	 * Retrieves the value of the attemptExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.5</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if 
	 *         limitCondition.attemptExperiencedDurationLimit is defined for
	 *         this activity, otherwise <code>false</code>.
	 * 
	 */
	public boolean getAttemptExDurControl();

	/**
	 * Retrieves the value of the limitCondition.attemptExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.6</b>)
	 * for this activity.
	 * 
	 * @return The experienced duration limit for an attempt on the activity.
	 * 
	 */
	public String getAttemptExDur();

	/**
	 * Sets the value of the LimitCondition.attemptExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.6</b>) for this
	 * activity.
	 * 
	 * @param iDur   The experienced duration (<code>String</code>) for an 
	 *               attempt on the activity.
	 * 
	 */
	public void setAttemptExDur(String iDur);

	/**
	 * Retrieves the value of the activiyAbsoluteDurationLimitControl
	 * Sequencing Definition Model Element (<b>element 3.7</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.activiyAbsoluteDurationLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 * 
	 */
	public boolean getActivityAbDurControl();

	/**
	 * Retrieves the value of the limitCondition.activiyAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.8</b>) for this
	 * activity.
	 * 
	 * @return The absolute duration limit for an activity.
	 * 
	 */
	public String getActivityAbDur();

	/**
	 * Sets the value of the LimitCondition.activityAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.8</b>) for this
	 * activity.
	 * 
	 * @param iDur   The absolute duration (<code>String</code>) for an activity.
	 * 
	 */
	public void setActivityAbDur(String iDur);

	/**
	 * Retrieves the value of the activityExperiencedDurationLimitControl
	 * Sequencing Definition Model Element (<b>element 3.9</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if
	 *         limitCondition.activityExperiencedDurationLimit is defined for
	 *         this activity, otherwise <code>false</code>.
	 *
	 */
	public boolean getActivityExDurControl();

	/**
	 * Retrieves the value of the limitCondition.activityExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.10</b>)
	 * for this activity.
	 * 
	 * @return The experienced duration limit for an activity.
	 * 
	 */
	public String getActivityExDur();

	/**
	 * Sets the value of the LimitCondition.activityExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.10</b>) for this
	 * activity.
	 * 
	 * @param iDur   The experienced duration (<code>String</code>) for an 
	 *               activity.
	 * 
	 */
	public void setActivityExDur(String iDur);

	/**
	 * Retrieves the value of the limitCondition.beginTimeLimitControl
	 * Sequencing Definition Model Element (<b>element 3.11</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.beginTimeLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 *
	 */
	public boolean getBeginTimeLimitControl();

	/**
	 * Retrieves the value of the limitCondition.beginTimeLimit Sequencing
	 * Definition Model Element (<b>element 3.12</b>) for this activity.
	 * 
	 * @return The time limit when an activity may begin.
	 *
	 */
	public String getBeginTimeLimit();

	/**
	 * Sets the value of the LimitCondition.beginTimeLimit Sequencing Definition
	 * Model Element (<b>element 3.12</b>) for this activity.
	 * 
	 * @param iTime  The time (<code>String</code>), before which, an attempt
	 *               on the activity cannot begin.
	 * 
	 */
	public void setBeginTimeLimit(String iTime);

	/**
	 * Retrieves the value of the limitCondition.endTimeLimitControl
	 * Sequencing Definition Model Element (<b>element 3.13</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.endTimeLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 * 
	 */
	public boolean getEndTimeLimitControl();

	/**
	 * Retrieves the value of the limitCondition.endTimeLimit Sequencing
	 * Definition Model Element (<b>element 3.14</b>) for this activity.
	 * 
	 * @return The time limit by which an activity must end.
	 * 
	 */
	public String getEndTimeLimit();

	/**
	 * Sets the value of the LimitCondition.endTimeLimit Sequencing Definition
	 * Model Element (<b>element 3.14</b>) for this activity.
	 * 
	 * @param iTime  The time (<code>String</code>), after which, an attempt
	 *               on the activity is invalid.
	 * 
	 */
	public void setEndTimeLimit(String iTime);

	/**
	 * Retrieves the value of the set of Auxiliary Resource Sequencing
	 * Definition Model Elements (<b>element 4</b>) for this activity.
	 * 
	 * @return The set (<code>Vector</code> of <code>ADLAuxiliaryResource</code>
	 *         objects) of auxiliary resource assoiciated with the activity.
	 * 
	 */
	public List getAuxResources();

	/**
	 * Sets the value of the Auxiliary Resource Sequencing Definition
	 * Model Element (<b>element 4</b>) for this activity.
	 * 
	 * @param iRes   The set (<code>Vector</code>) of auxiliary resources
	 *               (<code>ADLAuxiliaryResource</code>) associated with this
	 *               activity.
	 * 
	 */
	public void setAuxResources(List iRes);

	/**
	 * Retrieves the Rollup Rules, Sequencing Definiition Model Element
	 * (<b>element 5</b>) defined for this activity.
	 * 
	 * @return The Rollup Rules (<code>SeqRollupRuleset</code>) defined for this
	 *         activity, or <code>null</code> if no rollup rules have been
	 *         defined.
	 * 
	 */
	public ISeqRollupRuleset getRollupRules();

	/**
	 * Sets the Rollup Rules Sequencing Definition Model Elements
	 * (<b>element 5</b>) defined for this activity.
	 * 
	 * @param iRuleSet The set (<code>SeqRollupRuleset</code>) of Rollup Rules
	 *                 defined for this activity.
	 * 
	 */
	public void setRollupRules(ISeqRollupRuleset iRuleSet);

	/**
	 * Describes if measure should be used to evaluate satisfaction if the
	 * activity is active.
	 * 
	 * @return Indicates if measure should be used to evaluate satisfaction if 
	 *         the activity is active.
	 * 
	 */
	public boolean getSatisfactionIfActive();

	/**
	 * Indicates if measure should be used to evaluate satisfaction if the
	 * activity is active.
	 * 
	 * @param iActiveMeasure Indicates if measure should be used to evaluate 
	 *                       satisfaction if the activity is active.
	 * 
	 */
	public void setSatisfactionIfActive(boolean iActiveMeasure);

	/**
	 * Retrieves Satisfied Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 * 
	 */
	public String getRequiredForSatisfied();

	/**
	 * Sets the Satisfied Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 * 
	 */
	public void setRequiredForSatisfied(String iConsider);

	/**
	 * Retrieves Not Satisfied Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 * 
	 */
	public String getRequiredForNotSatisfied();

	/**
	 * Sets the Not Satisfied Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 * 
	 */
	public void setRequiredForNotSatisfied(String iConsider);

	/**
	 * Retrieves Complete Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 * 
	 */
	public String getRequiredForCompleted();

	/**
	 * Sets the Complete Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 * 
	 */
	public void setRequiredForCompleted(String iConsider);

	/**
	 * Retrieves Incomplete Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 * 
	 */
	public String getRequiredForIncomplete();

	/**
	 * Sets the Incomplete Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 *
	 */
	public void setRequiredForIncomplete(String iConsider);

	/**
	 * Retrieves the value of the set of Objective Definition Sequencing
	 * Definition Model Elements (<b>elements 6 and 7</b>) for this activity.
	 * 
	 * @return The set of objectives assoiciated with the activity.  These
	 *         objectivees are returned as a <code>Vector</code> of
	 *         <code>SeqObjective</code> objects.
	 * 
	 */
	public List getObjectives();

	/**
	 * Sets the value of the Objectives Resource Sequencing Definition
	 * Model Elements (<b>elements 6 and 7</b>) for this activity.
	 * 
	 * @param iObjs  The set (<code>Vector</code> of <code>SeqObjective</code>)
	 *               of objectives(s) associated with this activity.
	 * 
	 */
	public void setObjectives(List iObjs);

	/**
	 * Retrieves the value of the Rollup Objective Satisfied Sequencing
	 * Definition Model Element (<b>element 8.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if objective status for this activity should
	 *         be considered during rollup for its parent, or
	 *         <code>false</code> if it should not be considered.
	 * 
	 */
	public boolean getIsObjRolledUp();

	/**
	 * Sets the value of the Rollup Objective Satisfied Sequencing
	 * Definition Model Element (<b>element 8.1</b>) for this activity.
	 * 
	 * @param iRolledup <code>true</code> if this activity should contribute
	 *                  mastery status to its parent during rollup; otherwise
	 *                  <code>false</code>.
	 * 
	 */
	public void setIsObjRolledUp(boolean iRolledup);

	/**
	 * Retrieves the value of the Rollup Objective Measure Weight Sequencing
	 * Definition Model Element (<b>element 8.2</b>) for this activity.
	 * 
	 * @return A <code>double</code> value from 0.0 to 1.0, describing the
	 *         weight this activity's score will have during rollup.
	 * 
	 */
	public double getObjMeasureWeight();

	/**
	 * Sets the value of the Rollup Objective Measure Weight Sequencing
	 * Definition Model Element (<b>element 8.2</b>) for this activity.
	 * 
	 * @param iWeight A value from 0.0 to 1.0 describing the
	 *                weight this activity's score will have during rollup.
	 * 
	 */
	public void setObjMeasureWeight(double iWeight);

	/**
	 * Retrieves the value of the Rollup Progress Completion Sequencing
	 * Definition Model Element (<b>element 8.3</b>) for this activity.
	 * 
	 * @return <code>true</code> if completion status for this activity should
	 *         be considered during rollup for its parent, or
	 *         <code>false</code> if it should not be considered.
	 * 
	 */
	public boolean getIsProgressRolledUp();

	/**
	 * Sets the value of the Rollup Progress Completion Sequencing
	 * Definition Model Element (<b>element 8.3</b>) for this activity.
	 * 
	 * @param iRolledup <code>true</code> if this activity should contribute
	 *                  completion status to its parent during rollup; otherwise
	 *                  <code>false</code>.
	 * 
	 */
	public void setIsProgressRolledUp(boolean iRolledup);

	/**
	 * Sets the value of the SelectionControl.SelectionTiming Sequencing
	 * Definition Model element (<b>element 9.1</b>) for this activity.
	 * 
	 * @param iTiming Indicates when the selection process should be applied
	 *                to this activity.
	 * 
	 */
	public void setSelectionTiming(String iTiming);

	/**
	 * Retrieves the value of the SelectionControl.SelectionTiming
	 * Sequencing Definition Model Element (<b>element 9.1</b>) for
	 * this activity.
	 * 
	 * @return When the selectiion process should be applied to this activity.
	 * 
	 */
	public String getSelectionTiming();

	/**
	 * Retrieves the value of the SelectCountStatus Sequencing Definition Model
	 * Element (<b>element 9.2</b>) for this activity.
	 * 
	 * @return <code>true</code> if the value of Selection Count is valid, 
	 * otherwise <code>false</code>
	 * 
	 */
	public boolean getSelectStatus();

	/**
	 * Retrieves the value of the SelectCount Sequencing Definition Model
	 * Element (<b>element 9.3</b>) for this activity.
	 * 
	 * @return The size of the random set of children to be selected from
	 *         from this activity.
	 * 
	 */
	public int getSelectCount();

	/**
	 * Sets the value of the SelectCount Sequencing Definition Model
	 * Element (<b>element 9.3</b>) for this activity.
	 * 
	 * @param iCount Indicates the number of children to be selected when
	 *               the selection process is applied to this activity.
	 * 
	 */
	public void setSelectCount(int iCount);

	/**
	 * Sets the value of the RandomizationControls.RandomizationTiming Sequencing
	 * Definition Model element (<b>element 10.1</b>) for this activity.
	 * 
	 * @param iTiming Indicates when the randomization process should be applied
	 *                to this activity.
	 * 
	 */
	public void setRandomTiming(String iTiming);

	/**
	 * Retrieves the value of the RandomizationControl.RandomizationTiming
	 * Sequencing Definition Model Element (<b>element 10.1</b>) for
	 * this activity.
	 * 
	 * @return When the randomization process should be applied to this
	 *         activity.
	 * 
	 */
	public String getRandomTiming();

	/**
	 * Retrieves the value of the RandomizationControl.RandomizeChildren
	 * Sequencing Definition Model Element (<b>element 10.2</b>) for
	 * this activity.
	 * 
	 * @return If the children of this activty should be reordered when the
	 *         randomization process is applied (<code>boolean</code>).
	 * 
	 */
	public boolean getReorderChildren();

	/**
	 * Sets the value of the RandomizationControl.RandomizeChildren 
	 * Sequencing Definition Model Element (<b>element 10.2</b>) for
	 * this activity.
	 * 
	 * @param iReorder Indicates if children of this activity should be
	 *                 reordered when the randomization process is applied to
	 *                 this activity.
	 * 
	 */
	public void setReorderChildren(boolean iReorder);

	/**
	 * Retrieves the value of the DeliveryControl.isTracked Sequencing Definition
	 * Model Element (<b>element 11.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if the activity is tracked, otherwise
	 *         <code>false</code>.
	 * 
	 */
	public boolean getIsTracked();

	/**
	 * Sets the value of the DeliveryControl.isTracked Sequencing Definition
	 * Model Element (<b>element 11.1</b>)  for this activity.
	 * 
	 * @param iTracked <code>true</code> if Tracking Status informatin should be
	 *                 maintained for this activity.
	 * 
	 */
	public void setIsTracked(boolean iTracked);

	/**
	 * Retrieves the value of the DeliveryControl.CompletionSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.2</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if the activity communicates its progress
	 *         status information; <code>false</code> if the LMS should set the
	 *         activity's progress status.
	 * 
	 */
	public boolean getSetCompletion();

	/**
	 * Sets the value of the DeliveryControl.CompletionSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.2</b>) for this
	 * activity.
	 * 
	 * @param iSet   <code>true</code> if the activity communicates its progress
	 *               status information.
	 * 
	 */
	public void setSetCompletion(boolean iSet);

	/**
	 * Retrieves the value of the DeliveryControl.ObjectiveSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.3</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if the activity communicates its objective
	 *         status information; <code>false</code> if the LMS should set the
	 *         activity's objective status.
	 * 
	 */
	public boolean getSetObjective();

	/**
	 * Sets the value of the DeliveryControl.ObjectiveSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.3</b>) for this
	 * activity.
	 * 
	 * @param iSet   <code>true</code> if the activity communicates its objective
	 *               status information.
	 * 
	 */
	public void setSetObjective(boolean iSet);

	/**
	 * Retrieves the value of the Delivery Mode.
	 * 
	 * @return The DeliveryMode (<code>String</code>) for this activity.
	 * 
	 */
	public String getDeliveryMode();

	/**
	 * Sets the value of the Delivery Mode for this activity.
	 * 
	 * @param iDeliveryMode The Delivery Mode for this activity.
	 * 
	 */
	public void setDeliveryMode(String iDeliveryMode);

	/**
	 * Retrieves the ID of the resource associated with this activity.
	 * 
	 * @return The ID (<code>String</code>) of the resource associated with this
	 *         activity, or <code>null</code> if the activity does not have a
	 *         resource.
	 * 
	 */
	public String getResourceID();

	/**
	 * Sets the ID of the resource associated with this activity.
	 * 
	 * @param iResourceID The ID (<code>String</code>) of the resource.
	 * 
	 */
	public void setResourceID(String iResourceID);

	/**
	 * Retrieves the ID of the activity's associated persistent state.
	 * 
	 * @return The ID (<code>String</code>) of the persistent state
	 *         associated with this activity.
	 * 
	 */
	public String getStateID();

	/**
	 * Sets the ID of the activity's associated persisent state.
	 * 
	 * @param iStateID The ID (<code>String</code>) of the activity's persistent
	 *                 state information.
	 * 
	 */
	public void setStateID(String iStateID);

	/**
	 * Retrieves the activity ID of this activity.
	 * 
	 * @return The unique ID (<code>String</code>) of this resource.<br>
	 *         NOTE: This will not be <code>null</code>.
	 * 
	 */
	public String getID();

	/**
	 * Sets the ID of this activity.
	 * 
	 * @param iID   The activity's ID.
	 * 
	 */
	public void setID(String iID);

	/**
	 * Retrieves the user-readable title of this activity.
	 * 
	 * @return The user-readable title (<code>String</code>) of this activity, or
	 *         <code>null</code> if the activity does not have a title.
	 * 
	 */
	public String getTitle();

	/**
	 * Sets the user-readable title for this activity.
	 * 
	 * @param iTitle The user-readable title for this activity.
	 * 
	 */
	public void setTitle(String iTitle);

	/**
	 * Set this activity's XML fragment of sequencing information.
	 * 
	 * @param iXML   Contains the XML fragment.
	 * 
	 */
	public void setXMLFragment(String iXML);

	/**
	 * Retrieves this activity's XML fragment of sequencing information.
	 * 
	 * @return The XML fragment.
	 * 
	 */
	public String getXMLFragment();

	/**
	 * Associates an ID with learner of this activity
	 * 
	 * @param iLearnerID The ID of the learner associated with this activity.
	 * 
	 */
	public void setLearnerID(String iLearnerID);

	/**
	 * Retrives the a learner ID associated with this activity
	 * 
	 * @return The ID (<code>String</code>> of the learner associated with
	 *         this activity.
	 * 
	 */
	public String getLearnerID();

	/**
	 * Retrieves the 'selected' state of the activity.  Activities become 
	 * 'selected' through the Selection and Randomization Process.
	 * 
	 * @return Indication if the activity is currently selected
	 */
	public boolean getIsSelected();

	/**
	 * Sets the 'selected' state of the activity
	 * 
	 * @param iSelected Indicates if the activity is 'selected.
	 */
	public void setIsSelected(boolean iSelected);

	/**
	 * Associates an ID with scope of this activity's objectives
	 * 
	 * @param iScopeID The ID of the scope associated with the objectives
	 * 
	 */
	public void setScopeID(String iScopeID);

	/**
	 * Retrives the a scope ID associated with this activity.
	 * 
	 * @return The ID (<code>String</code>> of the scope associated with
	 *         this activity's objectives.
	 * 
	 */
	public String getScopeID();

	/**
	 * Retrieve this activity's IsVisible status.
	 * 
	 * @return <code>true</code> if this activity is 'visible', otherwise
	 *         <code>false</code>
	 * 
	 */
	public boolean getIsVisible();

	/**
	 * Set this activity's 'IsVisible' status.
	 * 
	 * @param iIsVisible TODO:  Add description
	 * 
	 */
	public void setIsVisible(boolean iIsVisible);

	/**
	 * Retrieve this activity's 'IsActive' status.
	 * 
	 * @return <code>true</code> if this activity is assumed to be 'active',
	 *         otherwise <code>false</code>
	 * 
	 */
	public boolean getIsActive();

	/**
	 * Set this activity's 'IsActive' status.
	 * 
	 * @param iActive Indicates that the activity is assumed to be 'Active'.
	 * 
	 */
	public void setIsActive(boolean iActive);

	/**
	 * Retrieve this activity's 'IsSuspended' status.
	 * 
	 * @return <code>true</code> if this activity is assumed to be 'suspended',
	 *         otherwise <code>false</code>
	 * 
	 */
	public boolean getIsSuspended();

	/**
	 * Set this activity's 'IsSuspended' status.
	 * 
	 * @param iSuspended Is the activity assumed to be 'suspended'?
	 * 
	 */
	public void setIsSuspended(boolean iSuspended);

	/**
	 * This method provides the state this <code>SeqActivity</code> object for
	 * diagnostic purposes.
	 *
	 */
	public void dumpState();

}