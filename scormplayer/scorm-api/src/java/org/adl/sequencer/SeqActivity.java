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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of one node of an activity tree.<br><br>
 * 
 * <strong>Filename:</strong> SeqActivity.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This implementation includes both the activity's status tracking
 * information and all sequencing information described in the IMS SS
 * Specification.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE. <br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * This implementation has not been optimized.<br><br>
 * 
 * This implementation is not intended to scale well or provide
 * the 'best' solution.  The goal of this implementation is to
 * provide a proof-of-concept implementation of the IMS SS
 * Specification.<br><br>
 * 
 * Future versions of this sequencing implementation may utilize
 * other data structures/sources and/or make performance and
 * scalablity optimizations.<br><br>
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS 1.0</li>
 *     <li>SCORM 2004 3rd Edition</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
@Slf4j
public class SeqActivity extends SeqActivityTrackingAccess implements SeqActivityStateAccess, Serializable, ISeqActivity {
	@Serial
    private static final long serialVersionUID = 1L;

	/**
	 * The PK of this object
	 */
	protected long id;

	/**
	 * Enumeration of the possible times for application of the Selection and
	 * Randomization Processes.
	 * <br>never
	 * <br><b>"_NEVER_"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String TIMING_NEVER = "never";

	/**
	 * Enumeration of the possible times for application of the Selection and
	 * Randomization Processes.
	 * <br>once
	 * <br><b>"_ONCE_"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String TIMING_ONCE = "once";

	/**
	 * Enumeration of the possible times for application of the Selection and
	 * Randomization Processes.
	 * <br>on each new attempt
	 * <br><b>"_EACHNEW_"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String TIMING_EACHNEW = "onEachNewAttempt";

	/**
	 * Enumeration of the possible termination requests -- described in section
	 * TB of the IMS SS Specification.
	 * <br>Exit All
	 * <br><b>"_EXITALL_"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	@SuppressWarnings("unused")
	private static final String TER_EXITALL = "_EXITALL_";

    /**
	 * This describes the sequencing definition model element 2
	 */
	ISeqRuleset mPreConditionRules = null;

	/**
	 * This describes the sequencing definition model element 2
	 */
	ISeqRuleset mPostConditionRules = null;

	/**
	 * This describes the sequencing definition model element 2
	 */
	ISeqRuleset mExitActionRules = null;

	/**
	 * This contains the XML fragment describing this activity's sequencing 
	 * definition information.
	 */
	private String mXML = null;

	/**
	 * This describes the count (order) of the activity in its activity tree
	 */
	private int mDepth = 0;

	/**
	* This describes the depth of the activity in its activity tree
	*/
	private int mCount = -1;

	/**
	 * This describes the ID associated with the root aggregation's user.
	 */
	private String mLearnerID = "_NULL_";

	/**
	 * This describes the ID associated with the activity's objective's scope
	 */
	private String mScopeID = null;

	/**
	 * This describes the activity's unique ID
	 */
	private String mActivityID = null;

	/**
	 * This describes the ID of the activity's associated resources
	 */
	private String mResourceID = null;

	/**
	 * This describes the ID of the activity's associated persistent state
	 */
	private String mStateID = null;

	/**
	 * This describes a human-readable description of the activity
	 */
	private String mTitle = null;

	/**
	 * This describes if this activity is visible in a TOC
	 */
	private boolean mIsVisible = true;

	/**
	 * This describes the relative order of this activity in context of all of
	 * its siblings.
	 */
	private int mOrder = -1;

	/**
	 * This describes the relative order of this activity in context of its
	 * selected siblings.
	 */
	private int mActiveOrder = -1;

	/**
	 * This describes if the activity is in the 'selected' set
	 */
	private boolean mSelected = true;

	/**
	 * This describes the parent activity of this activity
	 */
	private SeqActivity mParent = null;

	// Activity State information
	/**
	 * This describes if this activity is considered to be 'active'
	 */
	private boolean mIsActive = false;

	/**
	 * This describes if this activity is suspended
	 */
	private boolean mIsSuspended = false;

	/**
	 * This describes the set of children of this activity
	 */
	private List<SeqActivity> mChildren = null;

	/**
	 * This describes the set of 'active' children of this activity -- these
	 * are children that will be considered during sequencing.
	 */
	private List<SeqActivity> mActiveChildren = null;

	/**
	 * This describes the delivery mode of the activity
	 */
	private String mDeliveryMode = "normal";

	// Sequencing Definintions described in IMS SS SD 
	/**
	 * This describes the sequencing definition model element 1.1
	 */
	private boolean mControl_choice = true;

	/**
	 * This describes the sequencing definition model element 1.2
	 */
	private boolean mControl_choiceExit = true;

	/**
	 * This describes the sequencing definition model element 1.3
	 */
	private boolean mControl_flow = false;

	/**
	 * This describes the sequencing definition model element 1.4
	 */
	private boolean mControl_forwardOnly = false;

	/**
	 * This describes the Constrained Choice SCORM sequencing extension
	 */
	private boolean mConstrainChoice = false;

	/**
	 * This describes the Prevent Activation SCORM sequencing extension
	 */
	private boolean mPreventActivation = false;

	/**
	 * This describes the sequencing definition model element 1.5
	 */
	private boolean mUseCurObj = true;

	/**
	 * This describes the sequencing definition model element 1.6
	 */
	private boolean mUseCurPro = true;

	/**
	 * This describes the sequencing definition model element 3.1
	 */
	private boolean mMaxAttemptControl = false;

	/**
	 * This describes the sequencing definition model element 3.2
	 */
	private long mMaxAttempt = 0;

	/**
	 * This describes the sequencing definition model element 3.3
	 */
	private boolean mAttemptAbDurControl = false;

	/**
	 * This describes the sequencing definition model element 3.4
	 */
	private IDuration mAttemptAbDur = null;

	/**
	 * This describes the sequencing definition model element 3.5
	 */
	private boolean mAttemptExDurControl = false;

	/**
	 * This describes the sequencing definition model element 3.6
	 */
	private IDuration mAttemptExDur = null;

	/**
	 * This describes the sequencing definition model element 3.7
	 */
	private boolean mActivityAbDurControl = false;

	/**
	 * This describes the sequencing definition model element 3.8
	 */
	private IDuration mActivityAbDur = null;

	/**
	 * This describes the sequencing definition model element 3.9
	 */
	private boolean mActivityExDurControl = false;

	/**
	 * This describes the sequencing definition model element 3.10
	 */
	private IDuration mActivityExDur = null;

	/**
	 * This describes the sequencing definition model element 3.11
	 */
	private boolean mBeginTimeControl = false;

	/**
	 * This describes the sequencing definition model element 3.12
	 */
	private String mBeginTime = null;

	/**
	 * This describes the sequencing definition model element 3.13
	 */
	private boolean mEndTimeControl = false;

	/**
	 * This describes the sequencing definition model element 3.14
	 */
	private String mEndTime = null;

	/**
	 * This describes the sequencing definition model elements 4
	 */
	private List<ADLAuxiliaryResource> mAuxResources = null;

	/**
	 * This describes the sequencing definition model elements 5
	 */
	private ISeqRollupRuleset mRollupRules = null;

	/** 
	 * This indicates if satisfaction of the activity can be evaluated from
	 * measure when the activity is active.
	 */
	private boolean mActiveMeasure = true;

	/** 
	 * This describes when the activity contributes to its parent's rollup
	 * during a Satisfied rule evaluation
	 */
	private String mRequiredForSatisfied = SeqRollupRule.ROLLUP_CONSIDER_ALWAYS;

	/** 
	 * This describes when the activity contributes to its parent's rollup
	 * during a Not Satisfied rule evaluation
	 */
	private String mRequiredForNotSatisfied = SeqRollupRule.ROLLUP_CONSIDER_ALWAYS;

	/** 
	 * This describes when the activity contributes to its parent's rollup
	 * during a Completed rule evaluation
	 */
	private String mRequiredForCompleted = SeqRollupRule.ROLLUP_CONSIDER_ALWAYS;

	/** 
	 * This describes when the activity contributes to its parent's rollup
	 * during a Not Satisfied rule evaluation
	 */
	private String mRequiredForIncomplete = SeqRollupRule.ROLLUP_CONSIDER_ALWAYS;

	/**
	 * This describes the sequencing definition model elements 6 and 7
	 */
	private List<SeqObjective> mObjectives = null;

	/**
	 * This describes the set of objective mappings defined for the activity's
	 * objectives.
	 */
	private Map<String, List<SeqObjectiveMap>> mObjMaps = null;

	/**
	 * This describes the sequencing definition model element 8.1
	 */
	private boolean mIsObjectiveRolledUp = true;

	/**
	 * This describes the sequencing definition model element 8.2
	 */
	private double mObjMeasureWeight = 1.0;

	/**
	 * This describes the sequencing definition model element 8.3
	 */
	private boolean mIsProgressRolledUp = true;

	/**
	 * This describes the sequencing definition model element 9.1
	 */
	private String mSelectTiming = "never";

	/**
	 * This describes the sequencing definition model element 9.2
	 */
	private boolean mSelectStatus = false;

	/**
	 * This describes the sequencing definition model element 9.3
	 */
	private int mSelectCount = 0;

	/**
	 * This describes if the selection process has already been applied to
	 * the activity.
	 */
	private boolean mSelection = false;

	/**
	 * This describes the sequencing definition model element 10.1
	 */
	private String mRandomTiming = "never";

	/**
	 * This describes the sequencing definition model element 10.2
	 */
	private boolean mReorder = false;

	/**
	 * This describes if the randomized process has already been applied to
	 * the activity.
	 */
	private boolean mRandomized = false;

	/**
	 * This describes the sequencing definition model element 11.1
	 */
	private boolean mIsTracked = true;

	/**
	 * This describes the sequencing definition model element 11.2
	 */
	private boolean mContentSetsCompletion = false;

	/**
	 * This describes the sequencing definition model element 11.3
	 */
	private boolean mContentSetsObj = false;

	/**
	 * This describes the 'current' tracking information
	 */
	private ADLTracking mCurTracking = null;

	/**
	 * This records the tracking history
	 */
	private List<ADLTracking> mTracking = null;

	/**
	 * This describes the tracking status model element 1.2.1 Element 4
	 */
	private long mNumAttempt = 0;

	/**
	 * This describes the number of times a SCO attempted ended when its
	 * associated activity's atttempt did not
	 */
	private long mNumSCOAttempt = 0;

	/** 
	 * This describes the activity's absolute duration.<br>
	 * Tracking element 1.2.1 Element 2
	 */
	private ADLDuration mActivityAbDur_track = null;

	/** 
	 * This describes the activity's experienced duration.<br>
	 * Tracking element 1.2.1 Element 3
	 */
	private ADLDuration mActivityExDur_track = null;

	/**
	 * Default Constructor
	 */
	public SeqActivity() {
		// Default constructor
	}

	/**
	  * Appends one activity to the list of all existing child activities for thi
	  * activity.
	  * 
	  * @param ioChild The child activity to add.
	  */
	void addChild(SeqActivity ioChild) {
        log.debug("  :: SeqActivity     --> BEGIN - addChild");

		if (mChildren == null) {
			mChildren = new ArrayList<>();
		}

		// To maintain consistency, adding a child activity will set the active
		// children to the set of all children.
		mActiveChildren = mChildren;

		mChildren.add(ioChild);

		// Tell the child who its parent is and its order in relation to its
		// siblings.
		ioChild.setOrder(mChildren.size() - 1);
		ioChild.setActiveOrder(mChildren.size() - 1);

		ioChild.setParent(this);
        log.debug("  :: SeqActivity     --> END   - addChild");
	}

	/**
	 * Clears the value of the primary objective's measure.
	 * 
	 * @return <code>true</code> if the satisfaction of the objective changed,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean clearObjMeasure() {
        log.debug("  :: SeqActivity     --> BEGIN - clearObjMeasure");

		boolean statusChange = false;

		if (mCurTracking != null && mCurTracking.mObjectives != null) {
			SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(mCurTracking.mPrimaryObj));

			if (obj != null) {
				SeqObjective objD = obj.getObj();
				boolean affectSatisfaction = objD.mSatisfiedByMeasure;

				if (affectSatisfaction) {
					affectSatisfaction = !objD.mContributesToRollup || (mActiveMeasure || !mIsActive);
				}

				statusChange = obj.clearObjMeasure(affectSatisfaction);
			} else {
                log.debug("  ::-->  ERROR : No primary objective");
			}
		}
        log.debug("  ::--> {}", statusChange);
		log.debug("  :: SeqActivity     --> END   - clearObjMeasure");

		return statusChange;
	}

	/**
	 * Clears the value of the designated objective's measure.
	 * 
	 * @param iObjID ID of the objective whose measure has changed.
	 * 
	 * @return <code>true</code> if the satisfaction of the objective changed,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean clearObjMeasure(String iObjID) {
        log.debug("  :: SeqActivity     --> BEGIN - clearObjMeasure");
		log.debug("  ::--> {}", iObjID);

		boolean statusChange = false;

		// A null objective indicates the primary objective
		if (iObjID == null) {
			statusChange = clearObjMeasure();
		} else if (mCurTracking != null && mCurTracking.mObjectives != null) {
			SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(iObjID));

			if (obj != null) {
				SeqObjective objD = obj.getObj();
				boolean affectSatisfaction = objD.mSatisfiedByMeasure;

				if (affectSatisfaction) {
					affectSatisfaction = !objD.mContributesToRollup || (mActiveMeasure || !mIsActive);
				}

				statusChange = obj.clearObjMeasure(affectSatisfaction);
			} else {
                log.debug("  ::-->  Objective Undefined");
			}
		}
        log.debug("  ::--> {}", statusChange);
		log.debug("  :: SeqActivity     --> END   - clearObjMeasure");

		return statusChange;
	}

	/**
	 * This method provides the state this <code>SeqActivity</code> object for
	 * diagnostic purposes.
	 *
	 */
	@Override
	public void dumpState() {
        log.debug("""
                    :: SeqActivty   --> BEGIN - dumpState
                  \t  ::--> Depth:         {}
                  \t  ::--> Count:         {}
                  \t  ::--> Order:         {}
                  \t  ::--> Selected:      {}
                  \t  ::--> Active Order:  {}
                  \t  ::--> Learner:       {}
                  \t  ::--> Activity ID:   {}
                  \t  ::--> Resource ID:   {}
                  \t  ::--> State ID:      {}
                  \t  ::--> Title:         {}
                  \t  ::--> Delivery Mode: {}
                  
                  \t  ::--> XML:           {}
                  
                  \t  ::--> Num Attempt :  {}
                  """, 
                mDepth, mCount, mOrder, mSelected, mActiveOrder, mLearnerID, mActivityID, mResourceID,
                mStateID, mTitle, mDeliveryMode, mXML, mNumAttempt);

		if (mCurTracking != null) {
            mCurTracking.dumpState();
        } else {
            log.debug("\t  ::--> Cur Track   :  NULL");
        }

        log.debug("\t  ::--> Act Ab Dur  : {}", mActivityAbDur_track != null ? mActivityAbDur_track.format(IDuration.FORMAT_SECONDS) : "NULL");
        log.debug("\t  ::--> Act Ex Dur  : {}", mActivityExDur_track != null ? mActivityExDur_track.format(IDuration.FORMAT_SECONDS) : "NULL");

        if (mTracking != null) {
            for (ADLTracking adlTracking : mTracking) {
                adlTracking.dumpState();
            }
        }

        log.debug("""
                  \t  ::--> IsActive    :  {}
                  \t  ::--> IsSupended  :  {}
                  \t  ::--> IsVisible   :  {}
                  \t  ::--> Parent      :  {}
                  \t  ::--> Children    :  [{}]
                  \t  ::--> ActChildren :  [{}]
                  \t  ::--> Choice      :  {}
                  \t  ::--> Choice Exit :  {}
                  \t  ::--> Flow        :  {}
                  \t  ::--> ForwardOnly :  {}
                  \t  ::--> Constrain   :  {}
                  \t  ::--> Prevent Act :  {}
                  \t  ::--> Use Cur Obj :  {}
                  \t  ::--> Use Cur Pro :  {}
                  \t  ::--> PRE SeqRules : [{}]
                  \t  ::--> EXIT SeqRules: [{}]
                  \t  ::--> POST SeqRules: [{}]
                  """,
                mIsActive,
                mIsSuspended,
                mIsVisible,
                mParent != null ? mParent.getID() : "NULL",
                mChildren == null ? "NULL" : mChildren.size(),
                mActiveChildren != null ? mActiveChildren.size() : "NULL",
                mControl_choice,
                mControl_choiceExit,
                mControl_flow,
                mControl_forwardOnly,
                mConstrainChoice,
                mPreventActivation,
                mUseCurObj,
                mUseCurPro,
                mPreConditionRules == null ? "NULL" : mPreConditionRules.size(),
                mExitActionRules == null ? "NULL" : mExitActionRules.size(),
                mPostConditionRules != null ? mPostConditionRules.size() : "NULL");

        log.debug("\tCONTROL MaxAttempts :  {}", mMaxAttemptControl);
        if (mMaxAttemptControl) {
            log.debug("\t      ::-->         :  {}", mMaxAttempt);
        }

        log.debug("\tCONTROL Att Ab Dur  :  {}", mAttemptAbDurControl);
        if (mAttemptAbDurControl) {
            log.debug("\t      ::-->         :  {}", mAttemptAbDur.format(IDuration.FORMAT_SECONDS));
        }

        log.debug("\tCONTROL Att Ex Dur  :  {}", mAttemptExDurControl);
        if (mAttemptExDurControl) {
            log.debug("\t      ::-->         :  {}", mAttemptExDur.format(IDuration.FORMAT_SECONDS));
        }

        log.debug("\tCONTROL Act Ab Dur  :  {}", mActivityAbDurControl);
        if (mActivityAbDurControl) {
            log.debug("\t      ::-->         :  {}", mActivityAbDur.format(IDuration.FORMAT_SECONDS));

        }

        log.debug("\tCONTROL Act Ex Dur  :  {}", mActivityExDurControl);
        if (mActivityExDurControl) {
            log.debug("\t      ::-->         :  {}", mActivityExDur.format(IDuration.FORMAT_SECONDS));
        }

        log.debug("""
                  \tCONTROL Begin Time  :  {}
                  \t      ::-->         :  {}
                  \tCONTROL End Time    :  {}
                  \t      ::-->         :  {}
                  """, 
                mBeginTimeControl, mBeginTime, mEndTimeControl, mEndTime);

        
        if (mAuxResources != null) {
            log.debug("\t  ::--> Services    :  [{}]", mAuxResources.size());
            mAuxResources.forEach(ADLAuxiliaryResource::dumpState);
        } else {
            log.debug("\t  ::--> Services    :  NULL");
        }

        log.debug("""
                  \t  ::--> RollupRules :  [{}]
                  \t  ::--> Rollup Satisfied      :  {}
                  \t  ::--> Rollup Not Satisfied  :  {}
                  \t  ::--> Rollup Completed      :  {}
                  \t  ::--> Rollup Incomplete     :  {}
                  """,
                mRollupRules == null ? "NULL" : mRollupRules.size(),
                mRequiredForSatisfied,
                mRequiredForNotSatisfied,
                mRequiredForCompleted,
                mRequiredForIncomplete);

        if (mObjectives == null) {
            log.debug("\t  ::--> Objectives  :  NULL");
        } else {
            log.debug("\t  ::--> Objectives  :  [{}]", mObjectives.size());
            mObjectives.forEach(SeqObjective::dumpState);
        }

        log.debug("""
                  \t  ::--> Rollup Obj     :  {}
                  \t  ::--> Rollup Weight  :  {}
                  \t  ::--> Rollup Pro     :  {}
                  \t  ::--> Select Time    :  {}
                  \t CONTROL Select Count  :  {}
                  \t         ::-->         :  {}
                  \t  ::--> Random Time    :  {}
                  \t  ::--> Reorder        :  {}
                  \t  ::--> Is Tracked     :  {}
                  \t  ::--> Cont Sets Obj  :  {}
                  \t  ::--> Cont Sets Pro  :  {}
                    :: SeqActivity   --> END   - dumpState
                  """,
                mIsObjectiveRolledUp,
                mObjMeasureWeight,
                mIsProgressRolledUp,
                mSelectTiming,
                mSelectStatus, 
                mSelectCount,
                mRandomTiming,
                mReorder,
                mIsTracked,
                mContentSetsCompletion,
                mContentSetsObj);
	}

	/**
	 * Evaluate all limit conditions defined for the activity.
	 *
	 * @return <code>true</code> if the evaulation of limit condtions for the
	 *         target activity result in that activity becoming disabled,
	 *         otherwise <code>false</code>.
	 */
	boolean evaluateLimitConditions() {
		// This is an implementation of UP.1
        log.debug("  :: SeqActivity --> BEGIN - evaluateLimitConditions");

		boolean disabled = false;

		if (mCurTracking != null) {

			// Test max attempts
			if (mMaxAttemptControl) {
                log.debug("  ::--> Attempt Limit Check");

				if (mNumAttempt >= mMaxAttempt) {
					disabled = true;
				}
			}

			if (mActivityAbDurControl && !disabled) {
                log.debug("  ::--> Activity Ab Dur Check");

				if (mActivityAbDur.compare(mActivityAbDur_track) != IDuration.LT) {
					disabled = true;
				}
			}

			if (mActivityExDurControl && !disabled) {
                log.debug("  ::--> Activity Ex Dur Check");

				if (mActivityExDur.compare(mActivityExDur_track) != IDuration.LT) {
					disabled = true;
				}
			}

			if (mAttemptAbDurControl && !disabled) {
                log.debug("  ::--> Attempt Ab Dur Check");

				if (mActivityAbDur.compare(mCurTracking.mAttemptAbDur) != IDuration.LT) {
					disabled = true;
				}
			}

			if (mAttemptExDurControl && !disabled) {
                log.debug("  ::--> Attempt Ex Dur Check");

				if (mActivityExDur.compare(mCurTracking.mAttemptExDur) != IDuration.LT) {
					disabled = true;
				}
			}

			if (mBeginTimeControl && !disabled) {
                log.debug("  ::--> Begin Time Check");
			}

			if (mEndTimeControl && !disabled) {
                log.debug("  ::--> End Time Check");
			}
		} else {
            log.debug("  ::--> Nothing to check");
		}
        log.debug("  :: SeqActivity --> END   - evaluateLimitConditions");

		return disabled;

	}

	/**
	 * Retrieves the order of this activity in relation to its active siblings.
	 * 
	 * @return The order of this activity relative to its active siblings.
	 */
	public int getActiveOrder() {
        log.debug("  :: SeqActivity     --> BEGIN - getActiveOrder");

		log.debug("  ::-->  {}", mActiveOrder);
		log.debug("  :: SeqActivity     --> END   - getActiveOrder");

		return mActiveOrder;
	}

	/**
	 * Retrieves the value of the limitCondition.activiyAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.8</b>) for this
	 * activity.
	 * 
	 * @return The absolute duration limit for an activity.
	 * 
	 */
	@Override
	public String getActivityAbDur() {
        log.debug("  :: SeqActivity    --> BEGIN - getActivityAbDur");

		String dur = null;

		if (mActivityAbDur != null) {
			dur = mActivityAbDur.format(IDuration.FORMAT_SCHEMA);
		}
        log.debug("  ::-->  {}", dur);
		log.debug("  :: SeqActivity    --> END   - getActivityAbDur");

		return dur;
	}

	/**
	 * Retrieves the value of the activiyAbsoluteDurationLimitControl
	 * Sequencing Definition Model Element (<b>element 3.7</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.activiyAbsoluteDurationLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getActivityAbDurControl() {
        log.debug("  :: SeqActivity    --> BEGIN - getActivityAbDurControl");
		log.debug("  ::-->  {}", mActivityAbDurControl);
		log.debug("  :: SeqActivity    --> END   - getActivityAbDurControl");

		return mActivityAbDurControl;
	}

	/**
	 * Retrieves the attempt status of this activity.
	 * 
	 * @return <code>true</code> if the activity has been attempted, otherwise,
	 *         <code>false</code>.
	 * 
	 */
	@Override
	boolean getActivityAttempted() {
        log.debug("  :: SeqActivity     --> BEGIN - getActivityAttempted");
		log.debug("  ::-->  {}", mNumAttempt == 0 ? "NotAttempted" : "Attempted");
		log.debug("  :: SeqActivity     --> END   - getActivityAttempted");

		return (mNumAttempt != 0);
	}

	/**
	 * Retrieves the value of the limitCondition.activityExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.10</b>)
	 * for this activity.
	 * 
	 * @return The experienced duration limit for an activity.
	 * 
	 */
	@Override
	public String getActivityExDur() {
        log.debug("  :: SeqActivity    --> BEGIN - getActivityExDur");

		String dur = null;

		if (mActivityExDur != null) {
			dur = mActivityExDur.format(IDuration.FORMAT_SCHEMA);
		}
        log.debug("  ::-->  {}", dur);
		log.debug("  :: SeqActivity    --> END   - getActivityExDur");

		return dur;
	}

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
	@Override
	public boolean getActivityExDurControl()

	{
        log.debug("  :: SeqActivity    --> BEGIN - getActivityExDurControl");
		log.debug("  ::-->  {}", mActivityExDurControl);
		log.debug("  :: SeqActivity    --> END   - getActivityExDurControl");

		return mActivityExDurControl;
	}

	/**
	 * Retrieves the value of the limitCondition.attemptAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.4</b>)
	 * for this activity.
	 * 
	 * @return The absolute duration limit for an attempt on the activity.
	 * 
	 */
	@Override
	public String getAttemptAbDur() {
        log.debug("  :: SeqActivity    --> BEGIN - getAttemptAbDur");

		String dur = null;

		if (mAttemptAbDur != null) {
			dur = mAttemptAbDur.format(IDuration.FORMAT_SCHEMA);
		}
        log.debug("  ::-->  {}", dur);
		log.debug("  :: SeqActivity    --> END   - getAttemptAbDur");

		return dur;
	}

	/**
	 * Retrieves the value of the attemptAbsoluteDurationLimitControl
	 * Sequencing Definition Model Element (<b>element 3.3</b>) for this 
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.attemptAbsoluteDurationLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getAttemptAbDurControl() {
        log.debug("  :: SeqActivity    --> BEGIN - getAttemptAbDurControl");
		log.debug("  ::-->  {}", mAttemptAbDurControl);
		log.debug("  :: SeqActivity    --> END   - getAttemptAbDurControl");

		return mAttemptAbDurControl;
	}

	/**
	 * Retrieves the current attempt's progress status.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @return <code>true</code> if the current attempt on the activity is
	 *         completed, otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getAttemptCompleted(boolean iIsRetry) {
        log.debug("  :: SeqActivity     --> BEGIN - getAttemptCompleted");

		String progress = ADLTracking.TRACK_UNKNOWN;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

				track.mAttempt = mNumAttempt;

				mCurTracking = track;
			}

			// make sure the current state is valid
			if (!(mCurTracking.mDirtyPro && iIsRetry)) {
				progress = mCurTracking.mProgress;
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::--> {}", progress);
		log.debug("  :: SeqActivity     --> END   - getAttemptCompleted");

		return (progress.equals(ADLTracking.TRACK_COMPLETED));
	}

	/**
	 * Retrieves the value of the limitCondition.attemptExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.6</b>)
	 * for this activity.
	 * 
	 * @return The experienced duration limit for an attempt on the activity.
	 * 
	 */
	@Override
	public String getAttemptExDur() {
        log.debug("  :: SeqActivity    --> BEGIN - getAttemptExDur");

		String dur = null;

		if (mAttemptExDur != null) {
			dur = mAttemptExDur.format(IDuration.FORMAT_SCHEMA);
		}
        log.debug("  ::-->  {}", dur);
		log.debug("  :: SeqActivity    --> END   - getAttemptExDur");

		return dur;
	}

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
	@Override
	public boolean getAttemptExDurControl() {
        log.debug("  :: SeqActivity    --> BEGIN - getAttemptExDurControl");
		log.debug("  ::-->  {}", mAttemptExDurControl);
		log.debug("  :: SeqActivity    --> END   - getAttemptExDurControl");

		return mAttemptExDurControl;
	}

	/**
	 * Retrieves the value of the limitCondition.attemptLimit Sequencing
	 * Definition Model Element (<b>element 3.2</b>) for this activity.
	 * 
	 * @return The maximum attempts (<code>long</code>) that has been defined
	 *         for this activity, or <code>-1</code> if none have been
	 *         defined.
	 *
	 */
	@Override
	public long getAttemptLimit() {
        log.debug("  :: SeqActivity    --> BEGIN - getAttemptLimit");
		log.debug("  ::-->  {}", mMaxAttempt);
		log.debug("  :: SeqActivity    --> END   - getAttemptLimit");

		return mMaxAttempt;
	}

	/**
	 * Retrieves the value of the limitCondition.attemptLimitControl Sequencing
	 * Definition Model Element (<b>element 3.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if limitCondition.attemptLimit is defined for
	 *         this activity, otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getAttemptLimitControl() {
        log.debug("  :: SeqActivity    --> BEGIN - getAttemptLimitControl");
		log.debug("  ::-->  {}", mMaxAttemptControl);
		log.debug("  :: SeqActivity    --> END   - getAttemptLimitControl");

		return mMaxAttemptControl;
	}

	/**
	 * Retrieves the value of the set of Auxiliary Resource Sequencing
	 * Definition Model Elements (<b>element 4</b>) for this activity.
	 * 
	 * @return The set (<code>List</code> of <code>ADLAuxiliaryResource</code>
	 *         objects) of auxiliary resource assoiciated with the activity.
	 * 
	 */
	@Override
	public List<ADLAuxiliaryResource> getAuxResources() {
        log.debug("  :: SeqActivity     --> BEGIN - getAuxResources");

		List<ADLAuxiliaryResource> result = null;
		if (mAuxResources != null && !mAuxResources.isEmpty()) {
			result = new ArrayList<>(mAuxResources);
		}

		log.debug("  :: SeqActivity     --> END   - getAuxResources");
		return result;
	}

	/**
	 * Retrieves the value of the limitCondition.beginTimeLimit Sequencing
	 * Definition Model Element (<b>element 3.12</b>) for this activity.
	 * 
	 * @return The time limit when an activity may begin.
	 *
	 */
	@Override
	public String getBeginTimeLimit() {
        log.debug("  :: SeqActivity    --> BEGIN - getBeginTimeLimit");
		log.debug("  ::-->  {}", mBeginTime);
		log.debug("  :: SeqActivity    --> END   - getBeginTimeLimit");

		return mBeginTime;
	}

	/**
	 * Retrieves the value of the limitCondition.beginTimeLimitControl
	 * Sequencing Definition Model Element (<b>element 3.11</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.beginTimeLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 *
	 */
	@Override
	public boolean getBeginTimeLimitControl() {
        log.debug("  :: SeqActivity    --> BEGIN - getBeginTimeLimitControl");
		log.debug("  ::-->  {}", mBeginTimeControl);
		log.debug("  :: SeqActivity    --> END   - getBeginTimeLimitControl");

		return mBeginTimeControl;
	}

	/**
	 * Method added 8/24/2007 by JLR to facilitate SeqActivityTree implementing TreeModel
	 * 
	 */
	public Object getChild(int i) {
		Object child = null;

		if (mActiveChildren != null) {
			child = mActiveChildren.get(i);
		}

		return child;
	}

	/**
	 * Method added 8/24/2007 by JLR to facilitate SeqActivityTree implementing TreeModel
	 * 
	 */
	public int getChildCount() {
		int count = 0;

		if (mActiveChildren != null) {
			count = mActiveChildren.size();
		}

		return count;
	}

	/**
	 * Retrieves the set of child activites for this activity.
	 * 
	 * @param iAll  Indicates if the set of activies requested is intended to
	 *              be 'all' of this activity's children, or just the 'active'
	 *              set.
	 * 
	 * @return The set of child activites (<code>List</code> of <code>
	 *         SeqActivity</code>), or <code>null</code> if the activity has no
	 *         children.
	 */
	public List<SeqActivity> getChildren(boolean iAll) {
        log.debug("  :: SeqActivity     --> BEGIN - getChildren");
		log.debug("  ::-->  {}", iAll);

		List<SeqActivity> result = null;

		if (iAll) {
			if (null != mChildren && !mChildren.isEmpty()) {
				result = new ArrayList<>(mChildren);
			}
		} else {
			if (null != mActiveChildren && !mActiveChildren.isEmpty()) {
				result = new ArrayList<>(mActiveChildren);
			}
		}
        if (result != null) {
			log.debug("  ::-->  [{}]", result.size());
			log.debug("  :: SeqActivity     --> END   - getChildren");
		}

		return result;
	}

	/**
	  * Retrieves the value of the Choice Constraint Constrain Choice Sequencing
	  * Definition Model Element for this activity.
	  * 
	  * @return <code>true</code> if the 'constrainChoice' is defined for this
	  *         activity,  otherwise <code>false</code>.
	  * 
	  */
	@Override
	public boolean getConstrainChoice() {
        log.debug("  :: SeqActivity     --> BEGIN - getConstrainChoice");
		log.debug("  ::-->  {}", mConstrainChoice);
		log.debug("  :: SeqActivity     --> END   - getConstrainChoice");

		return mConstrainChoice;
	}

	/**
	 * Retrieves the value of the ControlMode.ForwardOnly Sequencing Definition
	 * Model Element (<b>element 1.4</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'ForwardOnly' is defined for this
	 *         cluster,  otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getControlForwardOnly() {
        log.debug("  :: SeqActivity     --> BEGIN - getForwardOnly");
		log.debug("  ::-->  {}", mControl_forwardOnly);
		log.debug("  :: SeqActivity     --> END   - getForwardOnly");

		return mControl_forwardOnly;
	}

	/**
	 * Retrieves the value of the ControlMode.Choice Sequencing Definition
	 * Model Element (<b>element 1.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'Choice' is enabled for this cluster,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getControlModeChoice() {
        log.debug("  :: SeqActivity     --> BEGIN - getChoice");
		log.debug("  ::-->  {}", mControl_choice);
		log.debug("  :: SeqActivity     --> END   - getChoice");
		return mControl_choice;
	}

	/**
	 * Retrieves the value of the ControlMode.ChoiceExit Sequencing Definition
	 * Model Element (<b>element 1.2</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'ChoiceExit' is enabled for this cluster,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getControlModeChoiceExit() {
        log.debug("  :: SeqActivity     --> BEGIN - getChoiceExit");
		log.debug("  ::-->  {}", mControl_choiceExit);
		log.debug("  :: SeqActivity     --> END   - getChoiceExit");

		return mControl_choiceExit;
	}

	/**
	 * Retrieves the value of the ControlMode.Flow Sequencing Definition Model
	 * Element (<b>element 1.3</b>) for this activity.
	 * 
	 * @return <code>true</code> if the 'Flow' is enabled for this cluster,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getControlModeFlow() {
        log.debug("  :: SeqActivity     --> BEGIN - getFlow");
		log.debug("  ::-->  {}", mControl_flow);
		log.debug("  :: SeqActivity     --> END   - getFlow");

		return mControl_flow;
	}

	/**
	 * Retrieves the count (order) of this activity in the activity tree
	 * 
	 * @return The count of this activity in the activity tree
	 */
	public int getCount() {
        log.debug("  :: SeqActivity     --> BEGIN - getCount");
		log.debug("  :: SeqActivity     --> END   - getCount");
		return mCount;
	}

	/**
	 * Retrieves the value of the Delivery Mode.
	 * 
	 * @return The DeliveryMode (<code>String</code>) for this activity.
	 * 
	 */
	@Override
	public String getDeliveryMode() {
        log.debug("  :: SeqActivity    --> BEGIN - getDeliveryMode");
		log.debug("  ::-->  {}", mDeliveryMode);
		log.debug("  :: SeqActivity    --> END   - getDeliveryMode");
		return mDeliveryMode;
	}

	/**
	 * Retrieves the depth of this activity in the activity tree
	 * 
	 * @return The depth of this activity in the activity tree
	 */
	public int getDepth() {
        log.debug("  :: SeqActivity     --> BEGIN - getDepth");
		log.debug("  :: SeqActivity     --> END   - getDepth");
		return mDepth;
	}

	/**
	 * Retrieves the value of the limitCondition.endTimeLimit Sequencing
	 * Definition Model Element (<b>element 3.14</b>) for this activity.
	 * 
	 * @return The time limit by which an activity must end.
	 * 
	 */
	@Override
	public String getEndTimeLimit() {
        log.debug("  :: SeqActivity    --> BEGIN - getEndTimeLimit");
		log.debug("  ::-->  {}", mEndTime);
		log.debug("  :: SeqActivity    --> END   - getEndTimeLimit");
		return mEndTime;
	}

	/**
	 * Retrieves the value of the limitCondition.endTimeLimitControl
	 * Sequencing Definition Model Element (<b>element 3.13</b>) for this
	 * activity.
	 * 
	 * @return <code>true</code> if limitCondition.endTimeLimit
	 *         is defined for this activity, otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getEndTimeLimitControl() {
        log.debug("  :: SeqActivity    --> BEGIN - getEndTimeLimitControl");
		log.debug("  ::-->  {}", mEndTimeControl);
		log.debug("  :: SeqActivity    --> END   - getEndTimeLimitControl");
		return mEndTimeControl;
	}

	/**
	 * Retrieves the Exit Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, exit action subset) defined for this activity.
	 * 
	 * @return The Exit Action Sequencing Rules (<code>SeqRuleset</code>)
	 *         defined for this activity, or <code>null</code> if no exit action
	 *         rules have been defined.
	 * 
	 */
	@Override
	public ISeqRuleset getExitSeqRules() {
        log.debug("  :: SeqActivity     --> BEGIN - getExitSeqRules");
		log.debug("  :: SeqActivity     --> END   - getExitSeqRules");
		return mExitActionRules;
	}

	/**
	 * Retrieves the activity ID of this activity.
	 * 
	 * @return The unique ID (<code>String</code>) of this resource.<br>
	 *         NOTE: This will not be <code>null</code>.
	 * 
	 */
	@Override
	public String getID() {
		return mActivityID;
	}

	/**
	 * Method added 8/24/2007 by JLR to facilitate SeqActivityTree implementing TreeModel
	 * 
	 */
	public int getIndexOfChild(SeqActivity child) {
        return mActiveChildren != null ? mActiveChildren.indexOf(child) : -1;
	}

	/**
	 * Retrieve this activity's 'IsActive' status.
	 * 
	 * @return <code>true</code> if this activity is assumed to be 'active',
	 *         otherwise <code>false</code>
	 * 
	 */
	@Override
	public boolean getIsActive() {
        log.debug("  :: SeqActivity     --> BEGIN - getIsActive");
		log.debug("  ::-->  {}", mIsActive);
		log.debug("  :: SeqActivity     --> END   - getIsActive");
		return mIsActive;
	}

	/**
	 * Retrieves the value of the Rollup Objective Satisfied Sequencing
	 * Definition Model Element (<b>element 8.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if objective status for this activity should
	 *         be considered during rollup for its parent, or
	 *         <code>false</code> if it should not be considered.
	 * 
	 */
	@Override
	public boolean getIsObjRolledUp() {
        log.debug("  :: SeqActivity     --> BEGIN - getIsObjRolledUp");
		log.debug("  ::-->  {}", mIsObjectiveRolledUp);
		log.debug("  :: SeqActivity     --> END   - getIsObjRolledUp");
		return mIsObjectiveRolledUp;
	}

	/**
	 * Retrieves the value of the Rollup Progress Completion Sequencing
	 * Definition Model Element (<b>element 8.3</b>) for this activity.
	 * 
	 * @return <code>true</code> if completion status for this activity should
	 *         be considered during rollup for its parent, or
	 *         <code>false</code> if it should not be considered.
	 * 
	 */
	@Override
	public boolean getIsProgressRolledUp() {
        log.debug("  :: SeqActivity     --> BEGIN - getIsProgressRolledUp");
		log.debug("  ::-->  {}", mIsProgressRolledUp);
		log.debug("  :: SeqActivity     --> END   - getIsProgressRolledUp");
		return mIsProgressRolledUp;
	}

	/**
	 * Retrieves the 'selected' state of the activity.  Activities become 
	 * 'selected' through the Selection and Randomization Process.
	 * 
	 * @return Indication if the activity is currently selected
	 */
	@Override
	public boolean getIsSelected() {
        log.debug("  :: SeqActivity     --> BEGIN - getSelected");
        log.debug("  ::-->  {}", mSelected);
		log.debug("  :: SeqActivity     --> END   - getSelected");
		return mSelected;
	}

	/**
	 * Retrieve this activity's 'IsSuspended' status.
	 * 
	 * @return <code>true</code> if this activity is assumed to be 'suspended',
	 *         otherwise <code>false</code>
	 * 
	 */
	@Override
	public boolean getIsSuspended() {
        log.debug("  :: SeqActivity     --> BEGIN - getIsSuspended");
		log.debug("  ::-->  {}", mIsSuspended);
		log.debug("  :: SeqActivity     --> END   - getIsSuspended");
		return mIsSuspended;
	}

	/**
	 * Retrieves the value of the DeliveryControl.isTracked Sequencing Definition
	 * Model Element (<b>element 11.1</b>) for this activity.
	 * 
	 * @return <code>true</code> if the activity is tracked, otherwise
	 *         <code>false</code>.
	 * 
	 */
	@Override
	public boolean getIsTracked() {
        log.debug("  :: SeqActivity    --> BEGIN - getIsTracked");
		log.debug("  ::-->  {}", mIsTracked);
		log.debug("  :: SeqActivity    --> END   - getIsTracked");

		return mIsTracked;
	}

	/**
	 * Retrieve this activity's IsVisible status.
	 * 
	 * @return <code>true</code> if this activity is 'visible', otherwise
	 *         <code>false</code>
	 * 
	 */
	@Override
	public boolean getIsVisible() {
        log.debug("  :: SeqActivity     --> BEGIN - getIsVisible");
		log.debug("  ::-->  {}", mIsVisible);
		log.debug("  :: SeqActivity     --> END   - getIsVisible");

		return mIsVisible;
	}

	/**
	 * Retrives the a learner ID associated with this activity
	 * 
	 * @return The ID (<code>String</code>> of the learner associated with
	 *         this activity.
	 * 
	 */
	@Override
	public String getLearnerID() {
        log.debug("  :: SeqActivity     --> BEGIN - getLearnerID");
		log.debug("  ::-->  {}", mLearnerID);
		log.debug("  :: SeqActivity     --> END   - getLearnerID");

		return mLearnerID;
	}

	/**
	 * Retreives the next sibling (forward traversal) to this activity.
	 * 
	 * @param iAll  Indicates if the set of activies requested is intended to
	 *              be 'all' of this activity's children, or just the 'active'
	 *              set.
	 *
	 * @return The next sibling (<code>SeqActivity</code>), or <code>null
	 *         </code> if none exists.
	 */
	public SeqActivity getNextSibling(boolean iAll) {
        log.debug("  :: SeqActivity     --> BEGIN - getNextSibling");

		SeqActivity next = null;
		int target = -1;

		// Make sure this activity has a parent
		if (mParent != null) {
			if (iAll) {
				target = mOrder + 1;
			} else {
				target = mActiveOrder + 1;
			}

			// Make sure there is a 'next' sibling
			if (target < mParent.getChildren(iAll).size()) {
				next = mParent.getChildren(iAll).get(target);
			}
		}
        log.debug("  ::-->  {}", next != null ? next.getID() : "NULL");
        log.debug("  :: SeqActivity     --> END   - getNextSibling");
		return next;
	}

	/**
	 * Retrieve this activity's attempt count.
	 * 
	 * @return A <code>long</code> value indicating the number attempts on this
	 *         activity.
	 * 
	 */
	@Override
	public long getNumAttempt() {
        log.debug("  :: SeqActivity     --> BEGIN - getNumAttempt");

		long attempt = 0;

		if (mIsTracked) {
			attempt = mNumAttempt;
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::--> {}", attempt);
		log.debug("  :: SeqActivity     --> END   - getNumAttempt");

		return attempt;
	}

	/**
	 * Retrieve this activity's SCO attempt count.
	 * 
	 * @return A <code>long</code> value indicating the number times this
	 *         activity's cooresponding SCO had its learner attempt end but
	 *         the activity's attempt did not.
	 */
	public long getNumSCOAttempt() {
		return mNumSCOAttempt;
	}

	/**
	 * Retrieves the value of the set of Objective Definition Sequencing
	 * Definition Model Elements (<b>elements 6 and 7</b>) for this activity.
	 * 
	 * @return The set of objectives assoiciated with the activity.  These
	 *         objectivees are returned as a <code>List</code> of
	 *         <code>SeqObjective</code> objects.
	 * 
	 */
	@Override
	public List<SeqObjective> getObjectives() {
        log.debug("  :: SeqActivity     --> BEGIN - getObjectives");

		List<SeqObjective> result = null;
		if (mObjectives != null && !mObjectives.isEmpty()) {
			result = new ArrayList<>(mObjectives);
		}

		log.debug("  :: SeqActivity     --> END   - getObjectives");
		return result;
	}

	/**
	 * Retrieve the set of read or write objective maps for a specifed objective
	 * associated with this activity.
	 * 
	 * @param iObjID Specifies a local objective ID
	 * 
	 * @param iRead  Specifies if the request is for read or write maps
	 * 
	 * @return A set of global shared objective IDs that are written to by
	 *         the specified objective associated with this activity,
	 *         or <code>null</code> if none exisit.
	 */
	public List<String> getObjIDs(String iObjID, boolean iRead) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjIDs");
		if (iObjID != null) {
            log.debug("  ::--> {}", iObjID);
        } else {
            log.debug("  ::--> NULL");
		}

		// Attempt to find the ID associated with the rolledup objective
		if (iObjID == null) {

			if (mCurTracking != null) {
				iObjID = mCurTracking.mPrimaryObj;
			} else {
                log.debug("  :: ERROR :: Unknown Tracking");
			}
		}

		List<String> objSet = null;
		List<SeqObjectiveMap> mapSet = null;

		if (mIsTracked) {
			if (mObjMaps != null) {

				mapSet = mObjMaps.get(iObjID);
				if (mapSet != null) {

                    for (SeqObjectiveMap map : mapSet) {
                        if (!iRead && (map.mWriteStatus || map.mWriteMeasure)) {
                            if (objSet == null) {
                                objSet = new ArrayList<>();
                            }

                            objSet.add(map.mGlobalObjID);
                        } else if (iRead && (map.mReadStatus || map.mReadMeasure)) {
                            if (objSet == null) {
                                objSet = new ArrayList<>();
                            }

                            objSet.add(map.mGlobalObjID);
                        }
                    }
				} else {
                    log.debug("  ::--> No Maps defined for objective");
				}
			} else {
                log.debug("  ::--> No Maps defined for activity");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  :: SeqActivity     --> END   - getObjIDs");

		return objSet;
	}

	/**
	 * Retreives the primary objective's measure value.<br><br>
	 * <b>NOTE:</b> the value returned has no signifigance unless the
	 * objective's measure status is <code>true</code>.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 *
	 * @return The measure of the primary objective.
	 * 
	 */
	@Override
	public double getObjMeasure(boolean iIsRetry) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjMeasure");

		double measure = 0.0;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);
				track.mAttempt = mNumAttempt;
				mCurTracking = track;
			}

			if (mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(mCurTracking.mPrimaryObj));

				if (obj != null) {
					String result = obj.getObjMeasure(iIsRetry);

					if (!result.equals(ADLTracking.TRACK_UNKNOWN)) {
						measure = Double.parseDouble(result);
					}
				} else {
                    log.debug("  ::-->  ERROR : No primary objective");
				}
			} else {
                log.debug("  ::-->  ERROR : Bad Tracking");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", measure);

		log.debug("  :: SeqActivity     --> END   - getObjMeasure");
		return measure;

	}

	/**
	 * Retreives the designated objective's measure value.<br><br>
	 * <b>NOTE:</b> the value returned has no signifigance unless the
	 * objective's measure status is <code>true</code>.
	 * 
	 * @param iObjID   ID of the objective whose measure is desired.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @return The measure of the designated objective.
	 */
	@Override
	double getObjMeasure(String iObjID, boolean iIsRetry) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjMeasure");
		log.debug("  ::--> {}", iObjID);

		double measure = 0.0;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

				track.mAttempt = mNumAttempt;

				mCurTracking = track;
			}

			// A null objective indicates the primary objective
			if (iObjID == null) {
				measure = getObjMeasure(iIsRetry);
			} else if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(iObjID));

				if (obj != null) {
					String result = obj.getObjMeasure(iIsRetry);

					if (!result.equals(ADLTracking.TRACK_UNKNOWN)) {
						measure = Double.parseDouble(result);
					}
				} else {
                    log.debug("  ::-->  Objective undefined");
				}
			} else {
                log.debug("  ::-->  ERROR : Bad Tracking");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", measure);

		log.debug("  :: SeqActivity     --> END   - getObjMeasure");
		return measure;
	}

	/**
	 * Indicates if the primary objective's measure value is valid.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @return <code>true</code> if the designated objective's measure is valid,
	 *         otherwise <code>false</code>.
	 *
	 */
	@Override
	public boolean getObjMeasureStatus(boolean iIsRetry) {
		return getObjMeasureStatus(iIsRetry, false);
	}

	/**
	 * Indicates if the primary objective's measure value is valid.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @param iUseLocal Indicates if only the local status information should
	 *                  be considered.
	 * 
	 * @return <code>true</code> if the designated objective's measure is valid,
	 *         otherwise <code>false</code>.
	 *
	 */
	boolean getObjMeasureStatus(boolean iIsRetry, boolean iUseLocal) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjMeasureStatus");

		boolean status = false;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

				track.mAttempt = mNumAttempt;

				mCurTracking = track;
			}

			if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(mCurTracking.mPrimaryObj));

				if (obj != null) {
					String result = null;

					result = obj.getObjMeasure(iIsRetry, iUseLocal);

					if (!result.equals(ADLTracking.TRACK_UNKNOWN)) {
						status = true;
					}
				} else {
                    log.debug("  ::-->  ERROR : No primary objective");
				}
			} else {
                log.debug("  ::-->  ERROR : Bad Tracking");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", status);

		log.debug("  :: SeqActivity     --> END   - getObjMeasureStatus");
		return status;

	}

	/**
	 * Indicates if the designated objective's measure value is valid.
	 * 
	 * @param iObjID   ID of the objective whose measure is desired.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @return <code>true</code> if the designated objective's measure is valid,
	 *         otherwise <code>false</code>.
	 */
	@Override
	boolean getObjMeasureStatus(String iObjID, boolean iIsRetry) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjMeasureStatus");
		log.debug("  ::-->  {}", iObjID);

		boolean status = false;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

				track.mAttempt = mNumAttempt;

				mCurTracking = track;
			}

			// A null objective indicates the primary objective
			if (iObjID == null) {
				status = getObjMeasureStatus(iIsRetry);
			} else if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(iObjID));

				if (obj != null) {
					String result = null;

					result = obj.getObjMeasure(iIsRetry);

					if (!result.equals(ADLTracking.TRACK_UNKNOWN)) {
						status = true;
					}
				} else {
                    log.debug("  ::-->  Objective undefined");
				}
			} else {
                log.debug("  ::-->  ERROR : Bad Tracking");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", status);

		log.debug("  :: SeqActivity     --> END   - getObjMeasureStatus");
		return status;
	}

	/**
	 * Retrieves the value of the Rollup Objective Measure Weight Sequencing
	 * Definition Model Element (<b>element 8.2</b>) for this activity.
	 * 
	 * @return A <code>double</code> value from 0.0 to 1.0, describing the
	 *         weight this activity's score will have during rollup.
	 * 
	 */
	@Override
	public double getObjMeasureWeight() {
        log.debug("  :: SeqActivity     --> BEGIN - getObjMeasureWeight");
		log.debug("  ::-->  {}", mObjMeasureWeight);
		log.debug("  :: SeqActivity     --> END   - getObjMeasureWeight");

		return mObjMeasureWeight;
	}

	/**
	 * Retreives the primary objective's minimum measure value.<br><br>
	 * 
	 * @return The measure of the designated objective, or <code>-1.0</code>
	 *         if no minimum measure is defined.
	 * 
	 */
	@Override
	double getObjMinMeasure() {

		double returnValue = 0.0;

		if (mCurTracking != null) {
			returnValue = getObjMinMeasure(mCurTracking.mPrimaryObj);
		} else {
			returnValue = -1.0;
		}

		return returnValue;
	}

	/**
	 * Retreives the designated objective's minimum measure value.<br><br>
	 * 
	 * @param iObjID ID of the objective whose minimum measure is desired.
	 * 
	 * @return The measure of the designated objective, or <code>-1.0</code>
	 *         if no minimum measure is defined.
	 *
	 */
	@Override
	double getObjMinMeasure(String iObjID) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjMinMeasure");
		log.debug("  ::--> {}", iObjID);

		double minMeasure = -1.0;

		if (mObjectives != null) {
            for (SeqObjective obj : mObjectives) {
                if (iObjID.equals(obj.mObjID)) {
                    minMeasure = obj.mMinMeasure;
                }
            }
		}
        log.debug("  ::-->  {}", minMeasure);

		log.debug("  :: SeqActivity     --> END   - getObjMinMeasure");
		return minMeasure;

	}

	/**
	 * Retrieves the primary objective's status.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @return <code>true</code> if the primary objective is satisfied
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getObjSatisfied(boolean iIsRetry) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjSatisfied");

		boolean status = false;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

				track.mAttempt = mNumAttempt;

				mCurTracking = track;
			}

			if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(mCurTracking.mPrimaryObj));

				if (obj != null) {
					SeqObjective objData = obj.getObj();

					if (!objData.mSatisfiedByMeasure || mActiveMeasure || !mIsActive) {
						String result = null;

						result = obj.getObjStatus(iIsRetry);

						if (result.equals(ADLTracking.TRACK_SATISFIED)) {
							status = true;
						}
					}
				} else {
                    log.debug("  ::-->  ERROR : No primary objective");
				}
			} else {
                log.debug("  ::-->  ERROR : Bad Tracking");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", status);

		log.debug("  :: SeqActivity     --> END   - getObjSatisfied");
		return status;
	}

	/**
	 * Retrieves the designated objective's status.
	 * 
	 * @param iObjID ID of the objective.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @return <code>true</code> if the designated objective is satisfied
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getObjSatisfied(String iObjID, boolean iIsRetry) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjSatisfied");
		log.debug("  ::--> {}", iObjID);

		boolean status = false;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

				track.mAttempt = mNumAttempt;

				mCurTracking = track;
			}

			// A null objective indicates the primary objective
			if (iObjID == null) {
				status = getObjSatisfied(iIsRetry);
			} else if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(iObjID));

				if (obj != null) {
					SeqObjective objData = obj.getObj();

					if (!objData.mSatisfiedByMeasure || mActiveMeasure || !mIsActive) {
						String result = null;

						result = obj.getObjStatus(iIsRetry);

						if (result.equals(ADLTracking.TRACK_SATISFIED)) {
							status = true;
						}
					}
				} else {
                    log.debug("  ::-->  Objective not defined");
				}
			} else {
                log.debug("  ::-->  ERROR : Bad Tracking");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", status);

		log.debug("  :: SeqActivity     --> END   - getObjSatisfied");
		return status;
	}

	/**
	 * Determines if the activity's primary objective is satisfied by measure.
	 * 
	 * @return <code>true</code> if the primary objective is satisfied by
	 *         measure, otherwise <code>false</code>
	 *
	 */
	@Override
	boolean getObjSatisfiedByMeasure() {
        log.debug("  :: SeqActivity     --> BEGIN - getObjSatisfiedByMeasure");

		boolean byMeasure = false;

		if (mCurTracking != null && mCurTracking.mObjectives != null) {
			SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(mCurTracking.mPrimaryObj));

			if (obj != null) {
				byMeasure = obj.getByMeasure();
			} else {
                log.debug("  ::-->  ERROR : No primary objective");
			}
		}
        log.debug("  ::-->  {}", byMeasure);

		log.debug("  :: SeqActivity     --> END   - getObjSatisfiedByMeasure");
		return byMeasure;
	}

	/**
	 * Determines if the primary objective's progress status is valid.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @return <code>true</code> if the primary objective's progress status is
	 *         valid, otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getObjStatus(boolean iIsRetry) {
		return getObjStatus(iIsRetry, false);
	}

	/**
	 * Determines if the primary objective's progress status is valid.
	 * 
	 * @param iIsRetry  Indicates if this evaluation is occuring during the
	 *                  processing of a 'retry' sequencing request.
	 * 
	 * @param iUseLocal Indicates if only the local status information should
	 *                  be considered.
	 * 
	 * @return <code>true</code> if the primary objective's progress status is
	 *         valid, otherwise <code>false</code>.
	 * 
	 */
	public boolean getObjStatus(boolean iIsRetry, boolean iUseLocal) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjStatus");

		boolean status = false;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

				track.mAttempt = mNumAttempt;

				mCurTracking = track;
			}

			if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(mCurTracking.mPrimaryObj));

				if (obj != null) {
					SeqObjective objData = obj.getObj();

					if (!objData.mSatisfiedByMeasure || mActiveMeasure || !mIsActive) {
						String result = null;

						result = obj.getObjStatus(iIsRetry, iUseLocal);

						if (!result.equals(ADLTracking.TRACK_UNKNOWN)) {
							status = true;
						}
					}
				} else {
                    log.debug("  ::-->  ERROR : No primary objective");
				}
			} else {
                log.debug("  ::-->  ERROR : Bad Tracking");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", status);

		log.debug("  :: SeqActivity     --> END   - getObjStatus");
		return status;
	}

	/**
	 * Determines if the designated objective's progress status is valid.
	 * 
	 * @param iObjID   ID of the objective.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @return <code>true</code> if the designated objective's progress status is
	 *         valid, otherwise <code>false</code>.
	 */
	@Override
	boolean getObjStatus(String iObjID, boolean iIsRetry) {
        log.debug("  :: SeqActivity     --> BEGIN - getObjStatus");
		log.debug("  ::--> {}", iObjID);

		boolean status = false;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

				track.mAttempt = mNumAttempt;

				mCurTracking = track;
			}

			// A null objective indicates the primary objective
			if (iObjID == null) {
				status = getObjStatus(iIsRetry);
			} else if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(iObjID));

				if (obj != null) {
					SeqObjective objData = obj.getObj();

					if (!objData.mSatisfiedByMeasure || mActiveMeasure || !mIsActive) {
						String result = null;

						result = obj.getObjStatus(iIsRetry);

						if (!result.equals(ADLTracking.TRACK_UNKNOWN)) {
							status = true;
						}
					}
				} else {
                    log.debug("  ::-->  Objective not defined");
				}
			} else {
                log.debug("  ::-->  ERROR : Bad Tracking");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", status);
		log.debug("  :: SeqActivity     --> END   - getObjStatus");
		return status;
	}

	/**
	 * Retrieves the objective status records for the activity.
	 * 
	 * @return A list (<code>List</code>) of <code>ADLObjStatus</code> records.
	 */
	public List<ADLObjStatus> getObjStatusSet() {
        log.debug("  :: SeqActivity     --> BEGIN - getObjStatusSet");

		List<ADLObjStatus> objSet = null;

		if (mCurTracking == null) {
			ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

			track.mAttempt = mNumAttempt;

			mCurTracking = track;
		}

		if (mCurTracking.mObjectives != null) {
			objSet = new ArrayList<>();

			Iterator<String> it = mCurTracking.mObjectives.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();

				// Only include objectives with IDs
				if (!key.equals("_primary_")) {
                    log.debug("  ::--> Getting  -> {}", key);

					SeqObjectiveTracking obj = mCurTracking.mObjectives.get(key);

					ADLObjStatus objStatus = new ADLObjStatus();

					objStatus.mObjID = obj.getObjID();
					String measure = obj.getObjMeasure(false);

					objStatus.mHasMeasure = !measure.equals(ADLTracking.TRACK_UNKNOWN);

					if (objStatus.mHasMeasure) {
						objStatus.mMeasure = Double.parseDouble(measure);
					}

					objStatus.mStatus = obj.getObjStatus(false);

					objSet.add(objStatus);
				}
			}
		}

		if (objSet != null) {
			if (objSet.isEmpty()) {
				objSet = null;
			}
		}
        log.debug("  :: SeqActivity     --> BEGIN - getObjStatusSet");
		return objSet;
	}

	/**
	 * Retreives the parent activity of this activity.
	 * 
	 * @return The parent of this activity (<code>SeqActivity</code>),
	 *         or <code>null</code> if it is the 'Root'.
	 */
	public SeqActivity getParent() {
        log.debug("""
                  :: SeqActivity     --> BEGIN - getParent
                  ::--> {}
                  :: SeqActivity     --> END   - getParent
                """, mParent != null ? mParent.getID() : "NULL");
        return mParent;
	}

	/**
	 * Retreives the ID of the parent activity of this activity.
	 * 
	 * @return The ID (<code>String</code>) parent activity of this activity,
	 *         or <code>null</code> if it is the 'Root'.
	 */
	String getParentID() {
        log.debug("  :: SeqActivity     --> BEGIN - getParentID");
        String parentID = mParent != null ? mParent.getID() : null;
        log.debug("  :: SeqActivity     --> END   - getParentID");
		return parentID;

	}

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
	@Override
	public ISeqRuleset getPostSeqRules() {
        log.debug("  :: SeqActivity     --> BEGIN - getPostSeqRules");
		log.debug("  :: SeqActivity     --> END   - getPostSeqRules");
		return mPostConditionRules;
	}

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
	@Override
	public ISeqRuleset getPreSeqRules() {
        log.debug("  :: SeqActivity     --> BEGIN - getPreSeqRules");
		log.debug("  :: SeqActivity     --> END   - getPreSeqRules");
		return mPreConditionRules;
	}

	/**
	 * Retrieves the value of the Choice Constraint Prevent Activation Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @return <code>true</code> if the 'preventActivation' is defined for this
	 *         activity,  otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getPreventActivation() {
        log.debug("  :: SeqActivity     --> BEGIN - getPreventActivation");
		log.debug("  ::-->  {}", mPreventActivation);
		log.debug("  :: SeqActivity     --> END   - getPreventActivation");
		return mPreventActivation;
	}

	/**
	 * Retreives the previous sibling (reverse traversal) to this activity.
	 * 
	 * @param iAll  Indicates if the set of activies requested is intended to
	 *              be 'all' of this activity's children, or just the 'active'
	 *              set.
	 *
	 * @return The next sibling (<code>SeqActivity</code>), or <code>null
	 *         </code> if none exists.
	 */
	public SeqActivity getPrevSibling(boolean iAll) {
        log.debug("  :: SeqActivity     --> BEGIN - getPrevSibling");

        log.debug("  ::-->  {}", iAll ? mOrder : mActiveOrder);

		SeqActivity prev = null;
		int target = -1;

		// Make sure this activity has a parent
		if (mParent != null) {
			if (iAll) {
				target = mOrder - 1;
			} else {
				target = mActiveOrder - 1;
			}

			// Make sure there is a 'next' sibling
			if (target >= 0) {
				prev = mParent.getChildren(iAll).get(target);
			}
		}

        log.debug("  ::-->  {}", prev != null ? prev.getID() : "NULL");
        log.debug("  :: SeqActivity     --> END   - getPrevSibling");

		return prev;
	}

	/**
	 * Determines if the current attempt's progress status is valid.
	 * 
	 * @param iIsRetry Indicates if this evaluation is occuring during the
	 *                 processing of a 'retry' sequencing request.
	 * 
	 * @return <code>true</code> if the progress status of the current attempt
	 *         is valid, otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean getProgressStatus(boolean iIsRetry) {
        log.debug("  :: SeqActivity     --> BEGIN - getProgressStatus");

		boolean status = false;

		if (mIsTracked) {
			if (mCurTracking != null) {

				if (!(mCurTracking.mDirtyPro && iIsRetry)) {
					status = !mCurTracking.mProgress.equals(ADLTracking.TRACK_UNKNOWN);
				}
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::--> {}", status);
		log.debug("  :: SeqActivity     --> END   - getProgressStatus");

		return status;
	}

	/**
	 * Retreives if the activity has already had the Randomization Process 
	 * applied.
	 * 
	 * @return Has the activity already had the Randomization Process applied?
	 */
	public boolean getRandomized() {
        log.debug("  :: SeqActivity     --> BEGIN - getRandomized");
		log.debug("  :: SeqActivity     --> END   - getRandomized");

		return mRandomized;
	}

	/**
	 * Retrieves the value of the RandomizationControl.RandomizationTiming
	 * Sequencing Definition Model Element (<b>element 10.1</b>) for
	 * this activity.
	 * 
	 * @return When the randomization process should be applied to this
	 *         activity.
	 * 
	 */
	@Override
	public String getRandomTiming() {
        log.debug("  :: SeqActivity    --> BEGIN - getRandomTiming");
		log.debug("  ::-->  {}", mRandomTiming);
		log.debug("  :: SeqActivity    --> END   - getRandomTiming");

		return mRandomTiming;
	}

	/**
	 * Retrieves the value of the RandomizationControl.RandomizeChildren
	 * Sequencing Definition Model Element (<b>element 10.2</b>) for
	 * this activity.
	 * 
	 * @return If the children of this activty should be reordered when the
	 *         randomization process is applied (<code>boolean</code>).
	 * 
	 */
	@Override
	public boolean getReorderChildren() {
        log.debug("  :: SeqActivity    --> BEGIN - getReorderChildren");
		log.debug("  ::-->  {}", mReorder);
		log.debug("  :: SeqActivity    --> END   - getReorderChildren");

		return mReorder;
	}

	/**
	 * Retrieves Complete Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 * 
	 */
	@Override
	public String getRequiredForCompleted() {
        log.debug("  :: SeqActivity     --> BEGIN - getRequiredForCompleted");
		log.debug("  :: SeqActivity     --> END   - getRequiredForCompleted");

		return mRequiredForCompleted;
	}

	/**
	 * Retrieves Incomplete Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 * 
	 */
	@Override
	public String getRequiredForIncomplete() {
        log.debug("  :: SeqActivity     --> BEGIN - getRequiredForIncomplete");
		log.debug("  :: SeqActivity     --> END   - getRequiredForIncomplete");

		return mRequiredForIncomplete;
	}

	/**
	 * Retrieves Not Satisfied Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 * 
	 */
	@Override
	public String getRequiredForNotSatisfied() {
        log.debug("  :: SeqActivity     --> BEGIN - getRequiredForNotSatisfied");
		log.debug("  :: SeqActivity     --> END   - getRequiredForNotSatisfied");

		return mRequiredForNotSatisfied;
	}

	/**
	 * Retrieves Satisfied Rollup Rule Consideration Sequencing Model Element
	 * defined for this activity.
	 * 
	 * @return Indication of when the activity should be included in its
	 *         parents Satisfaction rollup rule evaluation.
	 * 
	 */
	@Override
	public String getRequiredForSatisfied() {
        log.debug("  :: SeqActivity     --> BEGIN - getRequiredForSatisfied");
		log.debug("  :: SeqActivity     --> END   - getRequiredForSatisfied");

		return mRequiredForSatisfied;
	}

	/**
	 * Retrieves the ID of the resource associated with this activity.
	 * 
	 * @return The ID (<code>String</code>) of the resource associated with this
	 *         activity, or <code>null</code> if the activity does not have a
	 *         resource.
	 * 
	 */
	@Override
	public String getResourceID() {
        log.debug("  :: SeqActivity     --> BEGIN - getResourceID");
		log.debug("  ::-->  {}", mResourceID);
		log.debug("  :: SeqActivity     --> END   - getResourceID");

		return mResourceID;
	}

	// The following accessors provide additional activity state information 
	// that is not included in the Sequencing Definition Model
	// ----------------------------------------------------------------------

	/**
	 * Retrieves the Rollup Rules, Sequencing Definiition Model Element
	 * (<b>element 5</b>) defined for this activity.
	 * 
	 * @return The Rollup Rules (<code>SeqRollupRuleset</code>) defined for this
	 *         activity, or <code>null</code> if no rollup rules have been
	 *         defined.
	 * 
	 */
	@Override
	public ISeqRollupRuleset getRollupRules() {
        log.debug("  :: SeqActivity     --> BEGIN - getRollupRules");
		log.debug("  :: SeqActivity     --> END   - getRollupRules");

		return mRollupRules;
	}

	/**
	 * Describes if measure should be used to evaluate satisfaction if the
	 * activity is active.
	 * 
	 * @return Indicates if measure should be used to evaluate satisfaction if 
	 *         the activity is active.
	 * 
	 */
	@Override
	public boolean getSatisfactionIfActive() {
        log.debug("  :: SeqActivity     --> BEGIN - getSatisfactionIfActive");
		log.debug("  :: SeqActivity     --> END   - getSatisfactionIfActive");

		return mActiveMeasure;
	}

	/**
	 * Retrives the a scope ID associated with this activity.
	 * 
	 * @return The ID (<code>String</code>> of the scope associated with
	 *         this activity's objectives.
	 * 
	 */
	@Override
	public String getScopeID() {
        log.debug("  :: SeqActivity     --> BEGIN - getScopeID");
		log.debug("  ::-->  {}", mScopeID);
		log.debug("  :: SeqActivity     --> END   - getScopeID");

		return mScopeID;
	}

	/**
	 * Retrieves the value of the SelectCount Sequencing Definition Model
	 * Element (<b>element 9.3</b>) for this activity.
	 * 
	 * @return The size of the random set of children to be selected from
	 *         from this activity.
	 * 
	 */
	@Override
	public int getSelectCount() {
        log.debug("  :: SeqActivity     --> BEGIN - getSelectCount");

		// If the number to be randomized is greater than the number of children
		// available, no  selection is required
		if (mChildren != null) {
			if (mSelectCount >= mChildren.size()) {
				mSelectTiming = "never";
				mSelectCount = mChildren.size();
			}
		} else {
			// No children to select from; can't select
			mSelectStatus = false;
			mSelectCount = 0;
		}
        log.debug("  ::-->  {}", mSelectCount);
		log.debug("  :: SeqActivity     --> END   - getSelectCount");

		return mSelectCount;
	}

	/**
	 * Retreives if the activity has already had the Selection Process applied.
	 * 
	 * @return Has the activity already had the Selection Process applied?
	 */
	public boolean getSelection() {
        log.debug("  :: SeqActivity     --> BEGIN - getSelection");
		log.debug("  :: SeqActivity     --> END   - getSelection");

		return mSelection;
	}

	/**
	 * Retrieves the value of the SelectionControl.SelectionTiming
	 * Sequencing Definition Model Element (<b>element 9.1</b>) for
	 * this activity.
	 * 
	 * @return When the selectiion process should be applied to this activity.
	 * 
	 */
	@Override
	public String getSelectionTiming() {
        log.debug("  :: SeqActivity    --> BEGIN - getSelectionTiming");
		log.debug("  ::-->  {}", mSelectTiming);
		log.debug("  :: SeqActivity    --> END   - getSelectionTiming");

		return mSelectTiming;
	}

	/**
	 * Retrieves the value of the SelectCountStatus Sequencing Definition Model
	 * Element (<b>element 9.2</b>) for this activity.
	 * 
	 * @return <code>true</code> if the value of Selection Count is valid, 
	 * otherwise <code>false</code>
	 * 
	 */
	@Override
	public boolean getSelectStatus() {
        log.debug("  :: SeqActivity     --> BEGIN - getSelectStatus");
		log.debug("  ::-->  {}", mSelectStatus);
		log.debug("  :: SeqActivity     --> END   - getSelectStatus");

		return mSelectStatus;
	}

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
	@Override
	public boolean getSetCompletion() {
        log.debug("  :: SeqActivity    --> BEGIN - getSetCompletion");
		log.debug("  ::-->  {}", mContentSetsCompletion);
		log.debug("  :: SeqActivity    --> END   - getSetCompletion");

		return mContentSetsCompletion;
	}

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
	@Override
	public boolean getSetObjective() {
        log.debug("  :: SeqActivity    --> BEGIN - getSetObjective");
		log.debug("  ::-->  {}", mContentSetsObj);
		log.debug("  :: SeqActivity    --> END   - getSetObjective");

		return mContentSetsObj;
	}

	/**
	 * Retrieves the ID of the activity's associated persistent state.
	 * 
	 * @return The ID (<code>String</code>) of the persistent state
	 *         associated with this activity.
	 * 
	 */
	@Override
	public String getStateID() {
        log.debug("  :: SeqActivity     --> BEGIN - getStateID");
        log.debug("  ::-->  {}", mStateID);
		log.debug("  :: SeqActivity     --> END   - getStateID");

		return mStateID;
	}

	/**
	 * Retrieves the user-readable title of this activity.
	 * 
	 * @return The user-readable title (<code>String</code>) of this activity, or
	 *         <code>null</code> if the activity does not have a title.
	 * 
	 */
	@Override
	public String getTitle() {
        log.debug("  :: SeqActivity     --> BEGIN - getTitle");
		log.debug("  ::-->  {}", mTitle);
		log.debug("  :: SeqActivity     --> END   - getTitle");

		return mTitle;
	}

	/**
	 * Retrieves the value of the ControlMode.useCurrentAttemptObjectiveInfo
	 * Sequencing Definition Model Element (<b>element 1.5</b>)
	 * for this activity.
	 * 
	 * @return <code>true</code> if the 'useCurrentAttemptObjectiveInfo'
	 *         is defined for this cluster, otherwise <code>false</code>.
	 *
	 */
	@Override
	public boolean getUseCurObjective() {
        log.debug("  :: SeqActivity     --> BEGIN - getUseCurObjective");
		log.debug("  ::-->  {}", mUseCurObj);
		log.debug("  :: SeqActivity     --> END   - getUseCurObjective");

		return mUseCurObj;
	}

	/**
	 * Retrieves the value of the ControlMode.useCurrentAttemptProgressInfo
	 * Sequencing Definition Model Element (<b>element 1.6</b>)
	 * for this activity.
	 * 
	 * @return <code>true</code> if the 'useCurrentAttemptProgressInfo'
	 *         is defined for this cluster, otherwise <code>false</code>.
	 *
	 */
	@Override
	public boolean getUseCurProgress() {
        log.debug("  :: SeqActivity     --> BEGIN - getUseCurProgress");
		log.debug("  ::-->  {}", mUseCurPro);
		log.debug("  :: SeqActivity     --> END   - getUseCurProgress");

		return mUseCurPro;
	}

	/**
	 * Retrieves this activity's XML fragment of sequencing information.
	 * 
	 * @return The XML fragment.
	 * 
	 */
	@Override
	public String getXMLFragment() {
        log.debug("  :: SeqActivity     --> BEGIN - getXMLFragment");
		log.debug("  ::-->  {}", mXML);
		log.debug("  :: SeqActivity     --> END   - getXMLFragment");

		return mXML;
	}

	/**
	 * Indicates if this activity has children.
	 * 
	 * @param iAll  Indicates if the set of activies requested is intended to
	 *              be 'all' of this activity's children, or just the 'active'
	 *              set.
	 * 
	 * @return <code>true</code> if this activity has children, otherwise
	 *         <code>false</code>.
	 */
	public boolean hasChildren(boolean iAll) {
        log.debug("  :: SeqActivity     --> BEGIN - hasChildren");
		log.debug("  ::-->  {}", iAll);

		boolean result = false;

		if (iAll) {
			result = mChildren != null && !mChildren.isEmpty();
		} else {
			result = mActiveChildren != null && !mActiveChildren.isEmpty();
		}
        log.debug("  ::-->  {}", result);
		log.debug("  :: SeqActivity     --> END   - hasChildren");

		return result;
	}

	/**
	 * Increment the attempt count for this activity by one.
	 * 
	 */
	@Override
	public void incrementAttempt() {
        log.debug("  :: SeqActivity     --> BEGIN - incrementAttempt");
		log.debug("  ::-->  {}", mActivityID);

		// Store existing tracking information for historical purposes
		if (mCurTracking != null) {
			if (mTracking == null) {
				mTracking = new ArrayList<>();
			}

			mTracking.add(mCurTracking);
		}

		// Create a set of tracking information for the new attempt
		ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

		mNumAttempt++;
		track.mAttempt = mNumAttempt;

		mCurTracking = track;

		// If this is a cluster, check useCurrent flags
		if (mActiveChildren != null) {

            for (SeqActivity temp : mActiveChildren) {
                // Flag 'dirty' data if we are supposed to only use 'current attempt
                // status -- Set existing data to 'dirty'.  When a new attempt on a
                // a child activity begins, the new tracking information will be
                // 'clean'.
                if (mUseCurObj) {
                    temp.setDirtyObj();
                }

                if (mUseCurPro) {
                    temp.setDirtyPro();
                }
            }
		}
        log.debug("  :: SeqActivity     --> END   - incrementAttempt");
	}

	/**
	 * Increment the count on the number of times a learner attempt on a SCO
	 * ended without the attempt on the SCO's cooresponding activity also
	 * ending.
	 * 
	 */
	public void incrementSCOAttempt() {
		mNumSCOAttempt++;
	}

	/**
	 * Method added 8/24/2007 by JLR to facilitate SeqActivityTree implementing TreeModel
	 * 
	 */
	public void replaceChild(SeqActivity oldChild, SeqActivity newChild) {
		int i = getIndexOfChild(oldChild);

		if (i != -1) {
			mActiveChildren.set(i, newChild);
		}
	}

	// The following package accessors provide acess to the activity's state.   
	// These methods are only called by the sequencing subprocesses.
	// ----------------------------------------------------------------------

	/**
	 * Reset this activity's attempt count.
	 * 
	 */
	@Override
	void resetNumAttempt() {
        log.debug("  :: SeqActivity     --> BEGIN - resetNumAttempt");

		// Clear all current and historical tracking information.
		mNumAttempt = 0;
		mCurTracking = null;
		mTracking = null;
        log.debug("  :: SeqActivity     --> END   - resetNumAttempt");
	}

	/**
	 * Sets the order of this activity in relation to its active siblings.
	 * 
	 * @param iOrder The order of this activity relative to its active siblings.
	 */
	private void setActiveOrder(int iOrder) {
        log.debug("  :: SeqActivity     --> BEGIN - setActiveOrder");
		log.debug("  ::-->  {}", iOrder);

		mActiveOrder = iOrder;
        log.debug("  :: SeqActivity     --> END   - setActiveOrder");
	}

	/**
	 * Sets the value of the LimitCondition.activityAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.8</b>) for this
	 * activity.
	 * 
	 * @param iDur   The absolute duration (<code>String</code>) for an activity.
	 * 
	 */
	@Override
	public void setActivityAbDur(String iDur) {
        log.debug("  :: SeqActivity     --> BEGIN - setActivityAbDur");
		log.debug("  ::-->  {}", iDur);

		if (iDur != null) {
			mActivityAbDurControl = true;
			mActivityAbDur = new ADLDuration(IDuration.FORMAT_SCHEMA, iDur);
		} else {
			mActivityAbDurControl = false;
		
        }
        log.debug("  :: SeqActivity     --> END   - setActivityAbDur");
	}

	/**
	 * Sets the value of the LimitCondition.activityExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.10</b>) for this
	 * activity.
	 * 
	 * @param iDur   The experienced duration (<code>String</code>) for an 
	 *               activity.
	 * 
	 */
	@Override
	public void setActivityExDur(String iDur) {
        log.debug("  :: SeqActivity     --> BEGIN - setActivityExDur");
		log.debug("  ::-->  {}", iDur);

		if (iDur != null) {
			mActivityExDurControl = true;
			mActivityExDur = new ADLDuration(IDuration.FORMAT_SCHEMA, iDur);
		} else {
			mActivityExDurControl = false;
		}
        log.debug("  :: SeqActivity     --> END   - setActivityExDur");
	}

	/**
	 * Sets the value of the LimitCondition.attemptAbsoluteDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.4</b>) for this
	 * activity.
	 * 
	 * @param iDur   The absolute duration (<code>String</code>) for an attempt
	 *               on the activity.
	 * 
	 */
	@Override
	public void setAttemptAbDur(String iDur) {
        log.debug("  :: SeqActivity     --> BEGIN - setAttemptAbDur");
		log.debug("  ::-->  {}", iDur);

		if (iDur != null) {
			mAttemptAbDurControl = true;
			mAttemptAbDur = new ADLDuration(IDuration.FORMAT_SCHEMA, iDur);
		} else {
			mAttemptAbDurControl = false;
			mAttemptAbDur = null;
		}
        log.debug("  :: SeqActivity     --> END   - setAttemptAbDur");
	}

	/**
	 * Sets the value of the LimitCondition.attemptExperiencedDurationLimit
	 * Sequencing Definition Model Element (<b>element 3.6</b>) for this
	 * activity.
	 * 
	 * @param iDur   The experienced duration (<code>String</code>) for an 
	 *               attempt on the activity.
	 * 
	 */
	@Override
	public void setAttemptExDur(String iDur) {
        log.debug("  :: SeqActivity     --> BEGIN - setAttemptExDur");
		log.debug("  ::-->  {}", iDur);

		if (iDur != null) {
			mAttemptExDurControl = true;
			mAttemptExDur = new ADLDuration(IDuration.FORMAT_SCHEMA, iDur);
		} else {
			mAttemptExDurControl = false;
		}
        log.debug("  :: SeqActivity     --> END   - setAttemptExDur");
	}

	/**
	 * Sets the value of the LimitCondtitions.attemptLimit Sequencing
	 * Definition Model Element (<b>element 3.2</b>) for this activity.
	 * 
	 * @param iMaxAttempt The maximum number attempts allowed for this activity.
	 *
	 */
	@Override
	public void setAttemptLimit(Long iMaxAttempt) {
        log.debug("  :: SeqActivity    --> BEGIN - setAttemptLimit");
        log.debug("  ::-->  {}", iMaxAttempt == null ? "NULL" : iMaxAttempt);

		if (iMaxAttempt != null) {
			long value = iMaxAttempt;

			if (value > 0) {
				mMaxAttemptControl = true;
				mMaxAttempt = value;
			} else {
				mMaxAttemptControl = false;
				mMaxAttempt = -1;
			}
		}
        log.debug("  :: SeqActivity     --> END  - setAttemptLimit");
	}

	/**
	 * Sets the value of the Auxiliary Resource Sequencing Definition
	 * Model Element (<b>element 4</b>) for this activity.
	 * 
	 * @param iRes   The set (<code>List</code>) of auxiliary resources
	 *               (<code>ADLAuxiliaryResource</code>) associated with this
	 *               activity.
	 * 
	 */
	@Override
	public void setAuxResources(List<ADLAuxiliaryResource> iRes) {
        log.debug("  :: SeqActivity     --> BEGIN - setAuxResources");
        log.debug("  ::-->  {}", iRes != null ? iRes.size() : "NULL");
		mAuxResources = iRes;
        log.debug("  :: SeqActivity     --> END   - setAuxResources");
	}

	/**
	 * Sets the value of the LimitCondition.beginTimeLimit Sequencing Definition
	 * Model Element (<b>element 3.12</b>) for this activity.
	 * 
	 * @param iTime  The time (<code>String</code>), before which, an attempt
	 *               on the activity cannot begin.
	 * 
	 */
	@Override
	public void setBeginTimeLimit(String iTime) {
        log.debug("  :: SeqActivity     --> BEGIN - setBeginTimeLimit");
		log.debug("  ::-->  {}", iTime);

		if (iTime != null) {
			mBeginTimeControl = true;
			mBeginTime = iTime;
		} else {
			mBeginTimeControl = false;
		}
        log.debug("  :: SeqActivity     --> END   - setBeginTimeLimit");
	}

	/**
	 * Assigns a set of activites (<code>List</code>) to become the child
	 * activities for this activity.
	 * 
	 * @param ioChildren The set of activites to become the children.
	 * 
	 * @param iAll      Indicates if the set of activies is intended to be 'all'
	 *                  of this activity's children, or just the 'active' set.
	 */
	public void setChildren(List<SeqActivity> ioChildren, boolean iAll) {
        log.debug("  :: SeqActivity     --> BEGIN - setChildren");
		log.debug("  ::-->  {}", iAll);

		SeqActivity walk = null;

		if (iAll) {
			mChildren = ioChildren;
			mActiveChildren = ioChildren;

			for (int i = 0; i < ioChildren.size(); i++) {
				walk = ioChildren.get(i);

				walk.setOrder(i);
				walk.setActiveOrder(i);
				walk.setParent(this);
				walk.setIsSelected(true);
			}
		} else {
            for (SeqActivity mChild : mChildren) {
                walk = mChild;
                walk.setIsSelected(false);
            }

			mActiveChildren = ioChildren;

			for (int i = 0; i < ioChildren.size(); i++) {
				walk = ioChildren.get(i);

				walk.setActiveOrder(i);
				walk.setIsSelected(true);
				walk.setParent(this);
			}
		}
        log.debug("  :: SeqActivity     --> END   - setChildren");
	}

	/**
	 * Sets the value of the Choice Constraint Constrain Choice Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @param iConstrainChoice <code>true</code> to enforce 'constrainChoice' for 
	 *                     this activity or <code>false</code> to prevent.
	 * 
	 */
	@Override
	public void setConstrainChoice(boolean iConstrainChoice) {
        log.debug("  :: SeqActivity     --> BEGIN - setConstrainChoice");
		log.debug("  ::-->  {}", iConstrainChoice);
		mConstrainChoice = iConstrainChoice;
        log.debug("  :: SeqActivity     --> END   -  setConstrainChoice");
	}

	/**
	 * Sets the value of the ControlMode.ForwardOnly Sequencing Definition Model 
	 * Element (<b>element 1.4</b>) for this activity.
	 * 
	 * @param iForwardOnly <code>true</code> to enforce 'ForwardOnly' for this
	 *                     cluster or <code>false</code> to disable.
	 * 
	 */
	@Override
	public void setControlForwardOnly(boolean iForwardOnly) {
        log.debug("  :: SeqActivity     --> BEGIN - setForwardOnly");
		log.debug("  ::-->  {}", iForwardOnly);
		mControl_forwardOnly = iForwardOnly;
        log.debug("  :: SeqActivity     --> END   - setForwardOnly");
	}

	/**
	 * Sets the value of the ControlMode.Choice Sequencing Definition Model
	 * Element (<b>element 1.1</b>) for this activity.
	 * 
	 * @param iChoice <code>true</code> to enable 'Choice' for this cluster or
	 *                <code>false</code> to disable.
	 */
	@Override
	public void setControlModeChoice(boolean iChoice) {
        log.debug("  :: SeqActivity     --> BEGIN - setChoice");
		log.debug("  ::-->  {}", iChoice);
		mControl_choice = iChoice;
        log.debug("  :: SeqActivity     --> END   - setChoice");
	}

	/**
	 * Sets the value of the ControlMode.ChoiceExit Sequencing Definition Model
	 * Element (<b>element 1.2</b>) for this activity.
	 * 
	 * @param iChoiceExit <code>true</code> to enable 'ChoiceExit' for this
	 *                    or <code>false</code> to disable.
	 * 
	 */
	@Override
	public void setControlModeChoiceExit(boolean iChoiceExit) {
        log.debug("  :: SeqActivity     --> BEGIN - setChoiceExit");
		log.debug("  ::-->  {}", iChoiceExit);
		mControl_choiceExit = iChoiceExit;
        log.debug("  :: SeqActivity     --> END   - setChoiceExit");
	}

	/**
	 * Sets the value of the ControlMode.Flow Sequencing Definition Model
	 * Element (<b>element 1.3</b>) for this activity.
	 * 
	 * @param iFlow  <code>true</code> to enable 'Flow' for this cluster or
	 *               <code>false</code> to disable.
	 * 
	 */
	@Override
	public void setControlModeFlow(boolean iFlow) {
        log.debug("  :: SeqActivity     --> BEGIN - setFlow");
		log.debug("  ::-->  {}", iFlow);
		mControl_flow = iFlow;
        log.debug("  :: SeqActivity     --> END   - setFlow");
	}

	/**
	 * Sets the count (order) of this activity in the activity tree.
	 * 
	 * @param iCount The depth of the activity.
	 */
	void setCount(int iCount) {
        log.debug("  :: SeqActivity     --> BEGIN - setCount");
		log.debug("  ::-->  {}", iCount);
		mCount = iCount;
        log.debug("  :: SeqActivity     --> END   - setCount");
	}

	/**
	 * Sets the designated objective's experienced duration for the current
	 * attempt.
	 * 
	 * @param iDur   The experienced duration of the current attempt on the
	 *               activity.
	 */
	@Override
	public void setCurAttemptExDur(ADLDuration iDur) {
        log.debug("  :: SeqActivity --> BEGIN - setCurAttemptExDur");
        log.debug("  ::--> {}", iDur != null ? iDur.format(IDuration.FORMAT_SCHEMA) : "NULL");
		if (mCurTracking != null) {
			mCurTracking.mAttemptAbDur = iDur;
		}
        log.debug("  :: SeqActivity --> END   - setCurAttemptExDur");
	}

	/**
	 * Sets the value of the Delivery Mode for this activity.
	 * 
	 * @param iDeliveryMode The Delivery Mode for this activity.
	 * 
	 */
	@Override
	public void setDeliveryMode(String iDeliveryMode) {
        log.debug("  :: SeqActivity     --> BEGIN - setDeliveryMode");
		log.debug("  ::-->  {}", iDeliveryMode);

		// Test vocabulary
		if (iDeliveryMode.equals("browse") || iDeliveryMode.equals("review") || iDeliveryMode.equals("normal")) {
			mDeliveryMode = iDeliveryMode;
		} else {
			mDeliveryMode = "normal";
		}
        log.debug("  :: SeqActivity     --> END   - setDeliveryMode");
	}

	/**
	 * Sets the depth this activity in the activity tree.
	 * 
	 * @param iDepth The depth of the activity.
	 */
	void setDepth(int iDepth) {
        log.debug("  :: SeqActivity     --> BEGIN - setDepth");
		log.debug("  ::-->  {}", iDepth);
		mDepth = iDepth;
        log.debug("  :: SeqActivity     --> END   - setDepth");
	}

	/**
	 * Indicates that the current Objective state is invalid due to a new
	 * attempt on the activity's parent.
	 */
	void setDirtyObj() {

		if (mCurTracking != null) {
			mCurTracking.setDirtyObj();
		}

		// If this is a cluster, check useCurrent flags
		if (mActiveChildren != null) {
            for (SeqActivity temp : mActiveChildren) {
                if (mUseCurObj) {
                    temp.setDirtyObj();
                }
            }
		}
	}

	/**
	 * Indicates that the current Progress state is invalid due to a new
	 * attempt on the activity's parent.
	 */
	void setDirtyPro() {
		if (mCurTracking != null) {
			mCurTracking.mDirtyPro = true;
		}

		// If this is a cluster, check useCurrent flags
		if (mActiveChildren != null) {
            for (SeqActivity temp : mActiveChildren) {
                if (mUseCurPro) {
                    temp.setDirtyPro();
                }
            }
		}
	}

	/**
	  * Sets the value of the LimitCondition.endTimeLimit Sequencing Definition
	  * Model Element (<b>element 3.14</b>) for this activity.
	  * 
	  * @param iTime  The time (<code>String</code>), after which, an attempt
	  *               on the activity is invalid.
	  * 
	 */
	@Override
	public void setEndTimeLimit(String iTime) {
        log.debug("  :: SeqActivity     --> BEGIN - setEndTimeLimit");
		log.debug("  ::-->  {}", iTime);

		if (iTime != null) {
			mEndTimeControl = true;
			mEndTime = iTime;
		} else {
			mEndTimeControl = false;
		}
        log.debug("  :: SeqActivity     --> END   - setEndTimeLimit");
	}

	/**
	 * Sets the Exit Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, exit action subset) defined for this activity.
	 * 
	 * @param iRuleSet The set of Exit Action Sequencing Rules
	 *                 (<code>SeqRuleset</code>) defined for this activity.
	 * 
	 */
	@Override
	public void setExitSeqRules(ISeqRuleset iRuleSet) {
        log.debug("  :: SeqActivity     --> BEGIN - setExitSeqRules");
        log.debug("  ::-->  {}", iRuleSet != null ? iRuleSet.size() : "NULL");
		mExitActionRules = iRuleSet;
        log.debug("  :: SeqActivity     --> END   - setExitSeqRules");
	}

	/**
	 * Sets the ID of this activity.
	 * 
	 * @param iID   The activity's ID.
	 * 
	 */
	@Override
	public void setID(String iID) {
        log.debug("  :: SeqActivity     --> BEGIN - setID");
		log.debug("  ::-->  {}", iID);
		mActivityID = iID;
        log.debug("  :: SeqActivity     --> END   - setID");
	}

	/**
	 * Set this activity's 'IsActive' status.
	 * 
	 * @param iActive Indicates that the activity is assumed to be 'Active'.
	 * 
	 */
	@Override
	public void setIsActive(boolean iActive) {
        log.debug("  :: SeqActivity     --> BEGIN - setIsActive");
		log.debug("  ::-->  {}", iActive);
		mIsActive = iActive;
        log.debug("  :: SeqActivity     --> END   - setIsActive");
	}

	/**
	 * Sets the value of the Rollup Objective Satisfied Sequencing
	 * Definition Model Element (<b>element 8.1</b>) for this activity.
	 * 
	 * @param iRolledup <code>true</code> if this activity should contribute
	 *                  mastery status to its parent during rollup; otherwise
	 *                  <code>false</code>.
	 * 
	 */
	@Override
	public void setIsObjRolledUp(boolean iRolledup) {
        log.debug("  :: SeqActivity     --> BEGIN - setIsObjRolledUp");
		log.debug("  ::-->  {}", iRolledup);
		mIsObjectiveRolledUp = iRolledup;
        log.debug("  :: SeqActivity     --> END   - setIsObjRolledUp");
	}

	/**
	 * Sets the value of the Rollup Progress Completion Sequencing
	 * Definition Model Element (<b>element 8.3</b>) for this activity.
	 * 
	 * @param iRolledup <code>true</code> if this activity should contribute
	 *                  completion status to its parent during rollup; otherwise
	 *                  <code>false</code>.
	 * 
	 */
	@Override
	public void setIsProgressRolledUp(boolean iRolledup) {
        log.debug("  :: SeqActivity     --> BEGIN - setIsProgressRolledUp");
		log.debug("  ::-->  {}", iRolledup);
		mIsProgressRolledUp = iRolledup;
        log.debug("  :: SeqActivity     --> END   - setIsProgressRolledUp");
	}

	/**
	 * Sets the 'selected' state of the activity
	 * 
	 * @param iSelected Indicates if the activity is 'selected.
	 */
	@Override
	public void setIsSelected(boolean iSelected) {
        log.debug("  :: SeqActivity     --> BEGIN - setSelected");
		log.debug("  ::--> State ID     : {}", iSelected);
		mSelected = iSelected;
        log.debug("  :: SeqActivity     --> END   - setSelected");
	}

	/**
	 * Set this activity's 'IsSuspended' status.
	 * 
	 * @param iSuspended Is the activity assumed to be 'suspended'?
	 * 
	 */
	@Override
	public void setIsSuspended(boolean iSuspended) {
        log.debug("  :: SeqActivity     --> BEGIN - setIsSuspended");
		log.debug("  ::-->  {}", iSuspended);
		mIsSuspended = iSuspended;
        log.debug("  :: SeqActivity     --> END   - setIsSuspended");
	}

	/**
	 * Sets the value of the DeliveryControl.isTracked Sequencing Definition
	 * Model Element (<b>element 11.1</b>)  for this activity.
	 * 
	 * @param iTracked <code>true</code> if Tracking Status informatin should be
	 *                 maintained for this activity.
	 * 
	 */
	@Override
	public void setIsTracked(boolean iTracked) {
        log.debug("  :: SeqActivity     --> BEGIN - setIsTracked");
		log.debug("  ::-->  {}", iTracked);
		mIsTracked = iTracked;
        log.debug("  :: SeqActivity     --> END   - setIsTracked");
	}

	/**
	 * Set this activity's 'IsVisible' status.
	 * 
	 * @param iIsVisible TODO:  Add description
	 * 
	 */
	@Override
	public void setIsVisible(boolean iIsVisible) {
        log.debug("  :: SeqActivity     --> BEGIN - setIsVisible");
		log.debug("  ::-->  {}", iIsVisible);
		mIsVisible = iIsVisible;
        log.debug("  :: SeqActivity     --> END   - setIsVisible");
	}

	/**
	 * Associates an ID with learner of this activity
	 * 
	 * @param iLearnerID The ID of the learner associated with this activity.
	 * 
	 */
	@Override
	public void setLearnerID(String iLearnerID) {
        log.debug("  :: SeqActivity     --> BEGIN - setLearnerID");
		log.debug("  ::-->  {}", iLearnerID);
		mLearnerID = iLearnerID;
        log.debug("  :: SeqActivity     --> END   - setLearnerID");
	}

	/**
	 * Sets the value of the Objectives Resource Sequencing Definition
	 * Model Elements (<b>elements 6 and 7</b>) for this activity.
	 * 
	 * @param iObjs  The set (<code>List</code> of <code>SeqObjective</code>)
	 *               of objectives(s) associated with this activity.
	 * 
	 */
	@Override
	public void setObjectives(List<SeqObjective> iObjs) {
        log.debug("  :: SeqActivity     --> BEGIN - setObjectives");
        log.debug("  ::-->  {}", iObjs != null ? iObjs.size() : "NULL");

		mObjectives = iObjs;

		if (iObjs != null) {
            for (SeqObjective obj : iObjs) {
                if (obj.mMaps != null) {
                    if (mObjMaps == null) {
                        mObjMaps = new Hashtable<>();
                    }
                    mObjMaps.put(obj.mObjID, obj.mMaps);
                }
            }
		}		
        log.debug("  :: SeqActivity     --> END   - setObjectives");
	}

	/**
	 * Sets the primary objective's measure to the desired value.
	 * 
	 * @param iMeasure The value of the objective's measure.
	 * 
	 * @return <code>true</code> if the satisfaction of the objective changed,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean setObjMeasure(double iMeasure) {
        log.debug("  :: SeqActivity     --> BEGIN - setObjMeasure");
		log.debug("  ::--> {}", iMeasure);

		boolean statusChange = false;

		if (mIsTracked) {
			if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(mCurTracking.mPrimaryObj));

				if (obj != null) {
					String prev = obj.getObjStatus(false);

					SeqObjective objD = obj.getObj();
					boolean affectSatisfaction = objD.mSatisfiedByMeasure;

					if (affectSatisfaction) {
						affectSatisfaction = !objD.mContributesToRollup || (mActiveMeasure || !mIsActive);
					}

					obj.setObjMeasure(iMeasure, affectSatisfaction);

					statusChange = !prev.equals(obj.getObjStatus(false));
				} else {
                    log.debug("  ::-->  ERROR : No primary objective");
				}
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", statusChange);
		log.debug("  :: SeqActivity     --> END   - setObjMeasure");

		return statusChange;

	}

	/**
	 * Sets the designated objective's measure to the desired value.
	 * 
	 * @param iObjID   ID of the objective whose measure has changed.
	 * 
	 * @param iMeasure The value of the objective's measure.
	 * 
	 * @return <code>true</code> if the satisfaction of the objective changed,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean setObjMeasure(String iObjID, double iMeasure) {
        log.debug("""
                    :: SeqActivity     --> BEGIN - setObjMeasure
                    ::--> {}
                    ::--> {}
                  """, iObjID, iMeasure);

		boolean statusChange = false;

		if (mIsTracked) {
			// A null objective indicates the primary objective
			if (iObjID == null) {
				statusChange = setObjMeasure(iMeasure);
			} else if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(iObjID));

				if (obj != null) {
					String prev = obj.getObjStatus(false);

					SeqObjective objD = obj.getObj();
					boolean affectSatisfaction = objD.mSatisfiedByMeasure;

					if (affectSatisfaction) {
						affectSatisfaction = !objD.mContributesToRollup || (mActiveMeasure || !mIsActive);
					}

					obj.setObjMeasure(iMeasure, affectSatisfaction);

					statusChange = !prev.equals(obj.getObjStatus(false));
				} else {
                    log.debug("  ::-->  ERROR : No primary objective");
				}
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", statusChange);
		log.debug("  :: SeqActivity     --> END   - setObjMeasure");

		return statusChange;
	}

	/**
	 * Sets the value of the Rollup Objective Measure Weight Sequencing
	 * Definition Model Element (<b>element 8.2</b>) for this activity.
	 * 
	 * @param iWeight A value from 0.0 to 1.0 describing the
	 *                weight this activity's score will have during rollup.
	 * 
	 */
	@Override
	public void setObjMeasureWeight(double iWeight) {
        log.debug("  :: SeqActivity     --> BEGIN - setObjMeasureWeight");
		log.debug("  ::-->  {}", iWeight);
		mObjMeasureWeight = iWeight;
        log.debug("  :: SeqActivity     --> END   - setObjMeasureWeight");
	}

	/**
	 * Set the primary objective's status to the desired value.
	 * 
	 * @param iStatus New value for the objective's satisfaction status.<br><br>
	 *                Valid values are: <code>unknown</code>,
	 *                <code>satisfied</code>, or <code>notsatisfied</code>.
	 * 
	 * @return <code>true</code> if the satisfaction of the objective changed,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean setObjSatisfied(String iStatus) {
        log.debug("  :: SeqActivity     --> BEGIN - setObjSatisfied");
		log.debug("  ::--> {}", iStatus);

		boolean statusChange = false;

		if (mIsTracked) {
			if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(mCurTracking.mPrimaryObj));

				if (obj != null) {
					// Validate desired value
					if (iStatus.equals(ADLTracking.TRACK_UNKNOWN) || iStatus.equals(ADLTracking.TRACK_SATISFIED)
					        || iStatus.equals(ADLTracking.TRACK_NOTSATISFIED)) {

						String result = obj.getObjStatus(false);

						obj.setObjStatus(iStatus);

						statusChange = !result.equals(iStatus);
					} else {
                        log.debug("  ::--> Invalid status value");
					}
				} else {
                    log.debug("  ::-->  ERROR : No primary objective");
				}
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", statusChange);
		log.debug("  :: SeqActivity     --> END   - setObjSatisfied");

		return statusChange;
	}

	/**
	 * Set the designated objective's status to the desired value.
	 * 
	 * @param iObjID  ID of the objective whose satisfaction has changed.
	 * 
	 * @param iStatus New value for the objective's satisfaction status.<br><br>
	 *                Valid values are: <code>unknown</code>,
	 *                <code>satisfied</code>, or <code>notsatisfied</code>.
	 * 
	 * @return <code>true</code> if the satisfaction of the objective changed,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean setObjSatisfied(String iObjID, String iStatus) {
        log.debug("""
                    :: SeqActivity     --> BEGIN - setObjSatisfied
                    ::--> {}
                    ::--> {}
                  """, iObjID, iStatus);

		boolean statusChange = false;

		if (mIsTracked) {
			// A null objective indicates the primary objective
			if (iObjID == null) {
				statusChange = setObjSatisfied(iStatus);
			} else if (mCurTracking != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(iObjID));

				if (obj != null) {
					// Validate desired value
					if (iStatus.equals(ADLTracking.TRACK_UNKNOWN) || iStatus.equals(ADLTracking.TRACK_SATISFIED)
					        || iStatus.equals(ADLTracking.TRACK_NOTSATISFIED)) {

						String result = obj.getObjStatus(false);

						obj.setObjStatus(iStatus);

						statusChange = !result.equals(iStatus);
					} else {
                        log.debug("  ::--> Invalid status value");
					}
				} else {
                    log.debug("  ::-->  ERROR : No primary objective");
				}
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", statusChange);
		log.debug("  :: SeqActivity     --> END   - setObjSatisfied");

		return statusChange;
	}

	/**
	 * Sets the order of this activity in relation to all of its siblings.
	 * 
	 * @param iOrder The order of this activity relative to its siblings.
	 */
	private void setOrder(int iOrder) {
        log.debug("  :: SeqActivity     --> BEGIN - setOrder");
		log.debug("  ::-->  {}", iOrder);
		mOrder = iOrder;
        log.debug("  :: SeqActivity     --> END   - setOrder");
	}

	/**
	 * Sets the designated activity as the parent of this activity
	 * 
	 * @param iParent The parent activity of this activity.
	 */
	private void setParent(SeqActivity iParent) {
        log.debug("  :: SeqActivity     --> BEGIN - setParent");
		mParent = iParent;
        log.debug("  :: SeqActivity     --> END   - setParent");
	}

	/**
	 * Sets the Post Condition Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, post condition action subset) defined for this
	 * activity.
	 * 
	 * @param iRuleSet The set of Post Condition Action Sequencing Rules
	 *                 (<code>SeqRuleset</code>) defined for this activity.
	 *
	 */
	@Override
	public void setPostSeqRules(ISeqRuleset iRuleSet) {
        log.debug("  :: SeqActivity     --> BEGIN - setPostSeqRules");
        log.debug("  ::-->  {}", iRuleSet != null ? iRuleSet.size() : "NULL");
		mPostConditionRules = iRuleSet;
        log.debug("  :: SeqActivity     --> END   - setPostSeqRules");
	}

	/**
	 * Sets the Precondition Action Rules, Sequencing Definition Model Element
	 * (<b>element 2</b>, precondition action subset) defined for this
	 *  activity.
	 * 
	 * @param iRuleSet The set of Precondition Action Sequencing Rules
	 *                 (<code>SeqRuleset</code>) defined for this activity.
	 * 
	 */
	@Override
	public void setPreSeqRules(ISeqRuleset iRuleSet) {
        log.debug("  :: SeqActivity     --> BEGIN - setPreSeqRules");
        log.debug("  ::-->  {}", iRuleSet != null ? iRuleSet.size() : "NULL");
		mPreConditionRules = iRuleSet;
        log.debug("  :: SeqActivity     --> END   - setPreSeqRules");
	}

	/**
	 * Sets the value of the Choice Constraint Prevent Activation Sequencing
	 * Definition Model Element for this activity.
	 * 
	 * @param iPreventActivation <code>true</code> to enforce 'preventActivation' for 
	 *                     this activity or <code>false</code> to allow.
	 * 
	 */
	@Override
	public void setPreventActivation(boolean iPreventActivation) {
        log.debug("  :: SeqActivity     --> BEGIN - setPreventActivation");
		log.debug("  ::-->  {}", iPreventActivation);
		mPreventActivation = iPreventActivation;
        log.debug("  :: SeqActivity     --> END   - setPreventActivation");
	}

	/**
	 * Set the current attempt's progress status to the desired value.<br><br>
	 * Valid values are: <code>unknown</code>, <code>completed</code>, and
	 * <code>incomplete</code>.
	 * 
	 * @param iProgress New value for the attempt's progress status.
	 * 
	 * @return <code>true</code> if the progress status of the activty changed,
	 *         otherwise <code>false</code>.
	 * 
	 */
	@Override
	public boolean setProgress(String iProgress) {
        log.debug("  :: SeqActivity     --> BEGIN - setProgress");
		log.debug("  ::-->  {}", iProgress);

		boolean statusChange = false;

		if (mIsTracked) {
			// Validate state data
			if (iProgress.equals(ADLTracking.TRACK_UNKNOWN) || iProgress.equals(ADLTracking.TRACK_COMPLETED) || iProgress.equals(ADLTracking.TRACK_INCOMPLETE)) {
				if (mCurTracking == null) {
					mCurTracking = new ADLTracking(mObjectives, mLearnerID, mScopeID);
				}

				String prev = mCurTracking.mProgress;

				mCurTracking.mProgress = iProgress;
				statusChange = !prev.equals(iProgress);
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  ::-->  {}", statusChange);
		log.debug("  :: SeqActivity     --> END   - setProgress");

		return statusChange;
	}

	/**
	 * Indictes if the activity has already had the Randomization Process
	 * applied.
	 * 
	 * @param iRandomized Has the activity already had the Randomized
	 *                    Process applied?
	 */
	public void setRandomized(boolean iRandomized) {
        log.debug("  :: SeqActivity     --> BEGIN - setRandomized");
		log.debug("  ::-->  {}", iRandomized);
		mRandomized = iRandomized;
        log.debug("  :: SeqActivity     --> END   - setRandomized");
	}

	/**
	 * Sets the value of the RandomizationControls.RandomizationTiming Sequencing
	 * Definition Model element (<b>element 10.1</b>) for this activity.
	 * 
	 * @param iTiming Indicates when the randomization process should be applied
	 *                to this activity.
	 * 
	 */
	@Override
	public void setRandomTiming(String iTiming) {
        log.debug("  :: SeqActivity    --> BEGIN - setRandomTiming");
		log.debug("  ::-->  {}", iTiming);

		// Validate vocabulary
		if (!(iTiming.equals(SeqActivity.TIMING_NEVER) || iTiming.equals(SeqActivity.TIMING_ONCE) || iTiming.equals(SeqActivity.TIMING_EACHNEW))) {
			mSelectTiming = SeqActivity.TIMING_NEVER;
		} else {
			mRandomTiming = iTiming;
		}
        log.debug("  :: SeqActivity     --> END  - setRandomTiming");
	}

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
	@Override
	public void setReorderChildren(boolean iReorder) {
        log.debug("  :: SeqActivity    --> BEGIN - setReorderChildren");
		log.debug("  ::-->  {}", iReorder);
		mReorder = iReorder;
        log.debug("  :: SeqActivity    --> END   - setReorderChildren");
	}

	/**
	 * Sets the Complete Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 * 
	 */
	@Override
	public void setRequiredForCompleted(String iConsider) {
        log.debug("  :: SeqActivity     --> BEGIN - setRequiredForCompleted");
		log.debug("  ::-->  {}", iConsider);
		// Assume the token is OK due to previous validation.
		mRequiredForCompleted = iConsider;
        log.debug("  :: SeqActivity     --> END   - setRequiredForCompleted");
	}

	/**
	 * Sets the Incomplete Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 *
	 */
	@Override
	public void setRequiredForIncomplete(String iConsider) {
        log.debug("  :: SeqActivity     --> BEGIN - setRequiredForIncomplete");
		log.debug("  ::-->  {}", iConsider);
		// Assume the token is OK due to previous validation.
		mRequiredForIncomplete = iConsider;
        log.debug("  :: SeqActivity     --> END   - setRequiredForIncomplete");
	}

	/**
	 * Sets the Not Satisfied Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 * 
	 */
	@Override
	public void setRequiredForNotSatisfied(String iConsider) {
        log.debug("  :: SeqActivity     --> BEGIN - setRequiredForNotSatisfied");
		log.debug("  ::-->  {}", iConsider);
		// Assume the token is OK due to previous validation.
		mRequiredForNotSatisfied = iConsider;
        log.debug("  :: SeqActivity     --> END   - setRequiredForNotSatisfied");
	}

	/**
	 * Sets the Satisfied Rollup Rule Consideration Sequencing Definition
	 * Model Element defined for this activity.
	 * 
	 * @param iConsider Indication of when the activity should be included in
	 *                  its parent's Satisfaction rollup rule evaluation.
	 * 
	 */
	@Override
	public void setRequiredForSatisfied(String iConsider) {
        log.debug("  :: SeqActivity     --> BEGIN - setRequiredForSatisfied");
		log.debug("  ::-->  {}", iConsider);

		// Assume the token is OK due to previous validation.
		mRequiredForSatisfied = iConsider;
        log.debug("  :: SeqActivity     --> END   - setRequiredForSatisfied");
	}

	/**
	 * Sets the ID of the resource associated with this activity.
	 * 
	 * @param iResourceID The ID (<code>String</code>) of the resource.
	 * 
	 */
	@Override
	public void setResourceID(String iResourceID) {
        log.debug("  :: SeqActivity     --> BEGIN - setResourceID");
		log.debug("  ::--> Resource ID     : {}", iResourceID);
		mResourceID = iResourceID;
        log.debug("  :: SeqActivity     --> END   - setResourceID");
	}

	/**
	 * Sets the Rollup Rules Sequencing Definition Model Elements
	 * (<b>element 5</b>) defined for this activity.
	 * 
	 * @param iRuleSet The set (<code>SeqRollupRuleset</code>) of Rollup Rules
	 *                 defined for this activity.
	 * 
	 */
	@Override
	public void setRollupRules(ISeqRollupRuleset iRuleSet) {
        log.debug("  :: SeqActivity     --> BEGIN - setRollupRules");
        log.debug("  ::-->  {}", iRuleSet != null ? iRuleSet.size() : "NULL");
		mRollupRules = iRuleSet;
        log.debug("  :: SeqActivity     --> END   - setRollupRules");
	}

	/**
	 * Indicates if measure should be used to evaluate satisfaction if the
	 * activity is active.
	 * 
	 * @param iActiveMeasure Indicates if measure should be used to evaluate 
	 *                       satisfaction if the activity is active.
	 * 
	 */
	@Override
	public void setSatisfactionIfActive(boolean iActiveMeasure) {
        log.debug("  :: SeqActivity     --> BEGIN - setSatisfactionIfActive");
		log.debug("  ::-->  {}", iActiveMeasure);
		// Assume the token is OK due to previous validation.
		mActiveMeasure = iActiveMeasure;
        log.debug("  :: SeqActivity     --> END   - setSatisfactionIfActive");
	}

	/**
	 * Associates an ID with scope of this activity's objectives
	 * 
	 * @param iScopeID The ID of the scope associated with the objectives
	 * 
	 */
	@Override
	public void setScopeID(String iScopeID) {
        log.debug("  :: SeqActivity     --> BEGIN - setScopeID");
		log.debug("  ::-->  {}", iScopeID);
		mScopeID = iScopeID;
        log.debug("  :: SeqActivity     --> END   - setScopeID");
	}

	/**
	 * Sets the value of the SelectCount Sequencing Definition Model
	 * Element (<b>element 9.3</b>) for this activity.
	 * 
	 * @param iCount Indicates the number of children to be selected when
	 *               the selection process is applied to this activity.
	 * 
	 */
	@Override
	public void setSelectCount(int iCount) {
        log.debug("  :: SeqActivity    --> BEGIN - setSelectCount");
		log.debug("  ::-->  {}", iCount);

		if (iCount >= 0) {
			mSelectStatus = true;
			mSelectCount = iCount;
		} else {
			mSelectStatus = false;
		}
        log.debug("  :: SeqActivity     --> END  - setSelectCount");
	}

	/**
	 * Indictes if the activity has already had the Selection Process applied.
	 * 
	 * @param iSelection Has the activity already had the Selection Process
	 *                   applied?
	 */
	public void setSelection(boolean iSelection) {
        log.debug("  :: SeqActivity     --> BEGIN - setSelection");
		log.debug("  ::-->  {}", iSelection);
		mSelection = iSelection;
        log.debug("  :: SeqActivity     --> END   - setSelection");
	}

	/**
	 * Sets the value of the SelectionControl.SelectionTiming Sequencing
	 * Definition Model element (<b>element 9.1</b>) for this activity.
	 * 
	 * @param iTiming Indicates when the selection process should be applied
	 *                to this activity.
	 * 
	 */
	@Override
	public void setSelectionTiming(String iTiming) {
        log.debug("  :: SeqActivity    --> BEGIN - setSelectionTiming");
		log.debug("  ::-->  {}", iTiming);

		// Validate vocabulary
		if (!(iTiming.equals(SeqActivity.TIMING_NEVER) || iTiming.equals(SeqActivity.TIMING_ONCE) || iTiming.equals(SeqActivity.TIMING_EACHNEW))) {
			mSelectTiming = SeqActivity.TIMING_NEVER;
		} else {
			mSelectTiming = iTiming;
		}
        log.debug("  :: SeqActivity     --> END  - setSelectionTiming");
	}

	/**
	 * Sets the value of the DeliveryControl.CompletionSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.2</b>) for this
	 * activity.
	 * 
	 * @param iSet   <code>true</code> if the activity communicates its progress
	 *               status information.
	 * 
	 */
	@Override
	public void setSetCompletion(boolean iSet) {
        log.debug("  :: SeqActivity     --> BEGIN - setSetCompletion");
		log.debug("  ::-->  {}", iSet);
		mContentSetsCompletion = iSet;
        log.debug("  :: SeqActivity     --> END   - setSetCompletion");
	}

	/**
	 * Sets the value of the DeliveryControl.ObjectiveSetbyContent
	 * Sequencing Definition Model Element (<b>element 11.3</b>) for this
	 * activity.
	 * 
	 * @param iSet   <code>true</code> if the activity communicates its objective
	 *               status information.
	 * 
	 */
	@Override
	public void setSetObjective(boolean iSet) {
        log.debug("  :: SeqActivity     --> BEGIN - setSetObjective");
		log.debug("  ::-->  {}", iSet);
		mContentSetsObj = iSet;
        log.debug("  :: SeqActivity     --> END   - setSetObjective");
	}

	/**
	 * Sets the ID of the activity's associated persisent state.
	 * 
	 * @param iStateID The ID (<code>String</code>) of the activity's persistent
	 *                 state information.
	 * 
	 */
	@Override
	public void setStateID(String iStateID) {
        log.debug("  :: SeqActivity     --> BEGIN - setStateID");
		log.debug("  ::--> State ID     : {}", iStateID);
		mStateID = iStateID;
        log.debug("  :: SeqActivity     --> END   - setStateID");
	}

	/**
	 * Sets the user-readable title for this activity.
	 * 
	 * @param iTitle The user-readable title for this activity.
	 * 
	 */
	@Override
	public void setTitle(String iTitle) {
        log.debug("  :: SeqActivity     --> BEGIN - setTitle");
		log.debug("  ::-->  {}", iTitle);
		mTitle = iTitle;
        log.debug("  :: SeqActivity     --> END   - setTitle");
	}

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
	@Override
	public void setUseCurObjective(boolean iUseCur) {
        log.debug("  :: SeqActivity     --> BEGIN - setUseCurObjective");
		log.debug("  ::-->  {}", iUseCur);
		mUseCurObj = iUseCur;
        log.debug("  :: SeqActivity     --> END - setUseCurObjective");
	}

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
	@Override
	public void setUseCurProgress(boolean iUseCur) {
        log.debug("  :: SeqActivity     --> BEGIN - setUseCurProgress");
		log.debug("  ::-->  {}", iUseCur);
		mUseCurPro = iUseCur;
        log.debug("  :: SeqActivity     --> END - setUseCurProgress");
	}

	/**
	 * Set this activity's XML fragment of sequencing information.
	 * 
	 * @param iXML   Contains the XML fragment.
	 * 
	 */
	@Override
	public void setXMLFragment(String iXML) {
        log.debug("  :: SeqActivity     --> BEGIN - setXMLFragment");
		log.debug("  ::-->  {}", iXML);
		mXML = iXML;
        log.debug("  :: SeqActivity     --> END   - setXMLFragment");
	}

	/**
	 * Triggers the deterministic rollup of measure.
	 * 
	 */
	public void triggerObjMeasure() {
        log.debug("  :: SeqActivity     --> BEGIN - triggerObjMeasure");

		double measure = 0.0;

		if (mIsTracked) {
			if (mCurTracking == null) {
				ADLTracking track = new ADLTracking(mObjectives, mLearnerID, mScopeID);

				track.mAttempt = mNumAttempt;

				mCurTracking = track;
			}

			if (mCurTracking != null && mCurTracking.mObjectives != null) {
				SeqObjectiveTracking obj = (mCurTracking.mObjectives.get(mCurTracking.mPrimaryObj));

				if (obj != null) {

					if (obj.getObj().mSatisfiedByMeasure) {

						String result = null;

						result = obj.getObjMeasure(false);

						if (!result.equals(ADLTracking.TRACK_UNKNOWN)) {
							measure = Double.parseDouble(result);
							obj.setObjMeasure(measure, true);
						} else {
							obj.clearObjMeasure(true);
						}
					} else {
                        log.debug("  ::--> Satisfaction not affected by measure");
					}
				} else {
                    log.debug("  ::-->  ERROR : No primary objective");
				}
			} else {
                log.debug("  ::-->  ERROR : Bad Tracking");
			}
		} else {
            log.debug("  ::--> NOT TRACKED");
		}
        log.debug("  :: SeqActivity     --> END   - triggerObjMeasure");
	}

	@Override
	public String toString() {
		return "SeqActivity [mActivityID=" + mActivityID + ", mResourceID=" + mResourceID + ", mLearnerID=" + mLearnerID + "]";
	}

} // end SeqActivity
