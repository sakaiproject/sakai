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

package org.adl.sequencer.impl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.adl.sequencer.ADLAuxiliaryResource;
import org.adl.sequencer.ADLDuration;
import org.adl.sequencer.ADLTracking;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.ActivityNode;
import org.adl.sequencer.IDuration;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISeqRollupRuleset;
import org.adl.sequencer.ISeqRuleset;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqActivity;
import org.adl.sequencer.SeqActivityTree;
import org.adl.sequencer.SeqNavRequests;
import org.adl.sequencer.SeqRollupRuleset;
import org.adl.sequencer.SeqRule;
import org.adl.sequencer.SeqRuleset;
import org.adl.util.debug.DebugIndicator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reference sequencing implementation of the IMS Simple Sequencing
 * Specification.<br>
 * <br>
 * 
 * <strong>Filename:</strong> ADLSequencer.java<br>
 * <br>
 * 
 * <strong>Description:</strong><br>
 * The <code>ADLSequencer</code> encapsulates the functionality of all four
 * conceptual processes required for sequencing: navigation interpreter,
 * sequencing, rollup, and delivery.<br>
 * <br>
 * 
 * The approach taken with this implementation is to provide public interfaces
 * for the RTE, enabling navigation/delivery, status reporting, simple session
 * management, and TOC information.<br>
 * <br>
 * 
 * Internaly, the <code>ADLSequencer</code> acts on a
 * <code>SeqActivityTree</code>, which provides state management of the
 * activity tree and access to the internal structures of its activities.<br>
 * <br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM Sample RTE 2.0.<br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * This implementation encapsulates all conceptual sequencing behaviors and
 * their corresponding processes. This implementation processes navigation
 * requests serially.<br>
 * <br>
 * 
 * To ensure the activity tree remains in a consistent state, the success of
 * requests are tracked globally by the sequencer.<br>
 * <br>
 * 
 * This implementation has not been optimized.<br>
 * <br>
 * 
 * <strong>Known Problems:</strong><br>
 * <br>
 * 
 * <strong>Side Effects:</strong><br>
 * <br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 * <li>IMS Simple Sequencing 1.0
 * <li>SCORM 2004
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class ADLSequencer implements SeqNavigation, SeqReportActivityStatus,
		ISequencer {
	private static Log log = LogFactory.getLog(ADLSequencer.class);
	
	/**
	 * Private class Required by various traversal methods to provide an 'out'
	 * parameter
	 */
	private class Walk {
		/**
		 * A sequencing activity
		 */
		public SeqActivity at = null;

		/**
		 * Used to indcate the direction of the walk. The default value is no
		 * flow
		 */
		public int direction = ADLSequencer.FLOW_NONE;

		/**
		 * Used to describe if the flow traversal walked off the acivity tree
		 */
		public boolean endSession = false;

		/**
		 * Default constructor for the private class
		 */
		public Walk() {
			// Default constructor
		}
	}

	/**
	 * Enumeration of the traversal directions -- described in section SB.4 of
	 * the IMS SS Specification. <br>
	 * None <br>
	 * <b>0</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static final int FLOW_NONE = 0;

	/**
	 * Enumeration of the traversal directions -- described in section SB.4 of
	 * the IMS SS Specification. <br>
	 * Forward <br>
	 * <b>1</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static final int FLOW_FORWARD = 1;

	/**
	 * Enumeration of the traversal directions -- described in section SB.4 of
	 * the IMS SS Specification. <br>
	 * Backward <br>
	 * <b>1</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static final int FLOW_BACKWARD = 2;

	/**
	 * This controls display of log messages to the java console.
	 */
	private static boolean _Debug = DebugIndicator.ON;

	/**
	 * Enumeration of the possible termination requests -- described in section
	 * TB of the IMS SS Specification. <br>
	 * Exit <br>
	 * <b>"_EXIT_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String TER_EXIT = "_EXIT_";

	/**
	 * Enumeration of the possible termination requests -- described in section
	 * TB of the IMS SS Specification. <br>
	 * Exit All <br>
	 * <b>"_EXITALL_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String TER_EXITALL = "_EXITALL_";

	/**
	 * Enumeration of the possible termination requests -- described in section
	 * TB of the IMS SS Specification. <br>
	 * Supsend All <br>
	 * <b>"_SUSPENDALL_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String TER_SUSPENDALL = "_SUSPENDALL_";

	/**
	 * Enumeration of the possible termination requests -- described in section
	 * TB of the IMS SS Specification. <br>
	 * Abandon <br>
	 * <b>"_ABANDON_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String TER_ABANDON = "_ABANDON_";

	/**
	 * Enumeration of the possible termination requests -- described in section
	 * TB of the IMS SS Specification. <br>
	 * Abandon All <br>
	 * <b>"_ABANDONALL_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String TER_ABANDONALL = "_ABANDONALL_";

	/**
	 * Enumeration of the possible sequencing requests -- described in section
	 * SB of the IMS SS Specification. <br>
	 * Start <br>
	 * <b>"_START_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String SEQ_START = "_START_";

	/**
	 * Enumeration of the possible sequencing requests -- described in section
	 * SB of the IMS SS Specification. <br>
	 * Retry <br>
	 * <b>"_RETRY_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String SEQ_RETRY = "_RETRY_";

	/**
	 * Enumeration of the possible sequencing requests -- described in section
	 * SB of the IMS SS Specification. <br>
	 * Resume All <br>
	 * <b>"_RESUMEALL_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String SEQ_RESUMEALL = "_RESUMEALL_";

	/**
	 * Enumeration of the possible sequencing requests -- described in section
	 * SB of the IMS SS Specification. <br>
	 * Exit <br>
	 * <b>"_EXIT_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String SEQ_EXIT = "_EXIT_";

	/**
	 * Enumeration of the possible sequencing requests -- described in section
	 * SB of the IMS SS Specification. <br>
	 * Continue <br>
	 * <b>"_CONTINUE_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String SEQ_CONTINUE = "_CONTINUE_";

	/**
	 * Enumeration of the possible sequencing requests -- described in section
	 * SB of the IMS SS Specification. <br>
	 * Previous <br>
	 * <b>"_PREVIOUS_"</b> <br>
	 * [SEQUENCING SUBSYSTEM CONSTANT]
	 */
	private static String SEQ_PREVIOUS = "_PREVIOUS_";

	/**
	 * Internal activity tree this instance of the sequencer acts upon.
	 */
	private SeqActivityTree mSeqTree = null;

	/**
	 * Indicates if the sequencing session has ended.
	 */
	private boolean mEndSession = false;

	/**
	 * Indicates that an exit action rule was successfully evaluated at the root
	 * of the activity tree.
	 */
	private boolean mExitCourse = false;

	/**
	 * Indicates if the sequencer is currently processing a retry sequencing
	 * request.
	 */
	private boolean mRetry = false;

	/**
	 * Indicates if an exitAll was tentativly processed.
	 */
	private boolean mExitAll = false;

	// The following attributes track the global state of the overall sequencing
	// process.

	/**
	 * Indicates if the most recently processed termination request was valid.
	 */
	private boolean mValidTermination = true;

	/**
	 * Indicates if the most recently processed sequencing request was valid.
	 */
	private boolean mValidSequencing = true;
	
	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	 
	 Public Methods
	 
	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

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
	public Vector getObjStatusSet(String iActivityID) {

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - getObjStatusSet");
		}

		Vector objSet = null;
		SeqActivity act = getActivity(iActivityID);

		// Make sure the activity exists
		if (act != null) {
			// Ask the activity for its current set of objective status records
			objSet = act.getObjStatusSet();
		} else {
			if (_Debug) {
				System.out.println("  ::-->  Activity not found");
			}
		}

		if (_Debug) {
			if (objSet == null) {
				System.out.println("  ::-->  NULL");
			} else {
				System.out.println("  ::-->  [ " + objSet.size() + " ]");
			}

			System.out.println("  :: ADLSequencer --> END   - getObjStatusSet");
		}

		return objSet;
	}

	/**
	 * Retrieves the current set of valid navigation requests.
	 * 
	 * @param oValid
	 *            Upon return, contains the set of valid navigation reqeusts.
	 */
	public void getValidRequests(IValidRequests argValid) {
		ADLValidRequests oValid = (ADLValidRequests)argValid;
		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> BEGIN - getValidRequests");
		}

		ADLValidRequests valid = null;

		if (mSeqTree != null) {
			valid = mSeqTree.getValidRequests();

			if (valid != null) {
				validateRequests();

				valid = mSeqTree.getValidRequests();
			}
		}

		// Copy the set of valid requests to the return object
		if (valid != null) {
			oValid.mContinue = valid.mContinue;
			oValid.mContinueExit = valid.mContinueExit;
			oValid.mPrevious = valid.mPrevious;
			
			if (valid.mTreeModel != null) {
				// TODO: Remove this
				//oValid.mTOC = (Vector) (((Vector) (valid.mTOC)).clone());
				oValid.mTreeModel = valid.mTreeModel;	//convertTOC((Vector)oValid.mTOC);
			}

			if (valid.mChoice != null) {
				oValid.mChoice = (Hashtable) (((Hashtable) (valid.mChoice))
						.clone());
			}
		} else {
			if (_Debug) {
				System.out
						.println("  ::--> ERROR : Unable to validate requests");
			}

			// Make sure nothing is valid
			oValid.mContinue = false;
			oValid.mContinueExit = false;
			oValid.mPrevious = false;
			oValid.mChoice = null;
			// TODO: Remove this
			//oValid.mTOC = null;
			oValid.mTreeModel = null;
		}

		if (_Debug) {
			if (oValid == null) {
				System.out.println("  ::--> NULL");
			} else {
				if (oValid.mChoice == null) {
					System.out.println("  ::--> [NULL]");
				} else {
					System.out.println("  ::--> [" + oValid.mChoice.size()
							+ "]");
				}
			}

			System.out
					.println("  :: ADLSequencer --> END   - getValidRequests");
		}
	}

	/**
	 * Sets the active activity tree for this sequencer to act on.
	 * 
	 * @param iTree
	 *            The activty tree for this sequencer to act on.
	 */
	public void setActivityTree(ISeqActivityTree iTree) {

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - setActivityTree");
		}

		// Make sure the activity tree exists.
		if (iTree != null) {
			// Set the activity tree to be acted upon
			mSeqTree = (SeqActivityTree)iTree;

			if (_Debug) {
				System.out.println("  ::--> Activity tree set.");
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> NULL activity tree.");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - setActivityTree");
		}
	}

	/**
	 * Gets the current active activity tree.
	 * 
	 * @return The current activity tree (<code>SeqActivityTree</code>).
	 */
	public ISeqActivityTree getActivityTree() {
		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - getActivityTree");
			System.out.println("  :: ADLSequencer --> END   - getActivityTree");
		}

		return mSeqTree;
	}

	/**
	 * Gets the root of the current activity tree.
	 * 
	 * @return The root activity of the current activity tree (<code>SeqActivity</code>).
	 */
	public ISeqActivity getRoot() {
		ISeqActivity rootActivity = null;
		if (mSeqTree != null) {
			rootActivity = mSeqTree.getRoot();
		}
		return rootActivity;
	}

	/**
	 * Clear the current activity; this is done unconditionally.
	 */
	public void clearSeqState() {

		if (_Debug) {
			System.out.println("  ::--> Clear Session");
		}

		SeqActivity temp = null;

		mSeqTree.setCurrentActivity(temp);
		mSeqTree.setFirstCandidate(temp);

	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Implementation of SeqReportActivityStatus

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

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
	public void reportSuspension(String iID, boolean iSuspended) {

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> BEGIN - reportSuspension");
			System.out.println("  ::--> Target activity: " + iID);
			System.out.println("  ::-->  " + iSuspended);
		}

		SeqActivity target = getActivity(iID);

		// Make sure the target activity is valid
		if (target != null) {
			// Confirm the activity is still active
			if (target.getIsActive()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false)
						&& mSeqTree.getCurrentActivity() == target) {
					// Set the activity's suspended state
					target.setIsSuspended(iSuspended);
				} else {
					if (_Debug) {
						System.out.println("  ::--> ERROR : Invalid target");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> ERROR : Target not active");
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Activity does not exist");
			}
		}

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> END   - reportSuspension");
		}

	}

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
	public void setAttemptDuration(String iID, IDuration argDur) {
		ADLDuration iDur = (ADLDuration)argDur;
		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "setAttemptDuration");
			System.out.println("  ::--> " + iID);

			if (iDur != null) {
				System.out.println("  ::--> "
						+ iDur.format(IDuration.FORMAT_SCHEMA));
			} else {
				System.out.println("  ::--> NULL");
			}
		}

		SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {
			// Make sure the activity is a valid target for status changes
			// -- the tracked active leaf current activity
			if (target.getIsActive() && target.getIsTracked()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false)
						&& mSeqTree.getCurrentActivity() == target) {
					target.setCurAttemptExDur(iDur);

					// Revalidate the navigation requests
					validateRequests();
				} else {
					if (_Debug) {
						System.out.println("  ::--> ERROR : Invalid target");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> ERROR : Target not active");
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Activity does not exist");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - "
					+ "setAttemptDuration");
		}
	}

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
	public void clearAttemptObjMeasure(String iID, String iObjID) {
		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "clearAttemptObjMeasure");
			System.out.println("  ::--> Target activity: " + iID);
			System.out.println("  ::--> Objective:       " + iObjID);
		}

		// Find the target activty
		SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {

			// Make sure the activity is a valid target for status changes
			// -- the active leaf current activity
			if (target.getIsActive()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false)
						&& mSeqTree.getCurrentActivity() == target) {
					boolean statusChange = target.clearObjMeasure(iObjID);

					if (statusChange) {
						// If the activity's status changed, it may affect other
						// activities -- invoke rollup
						Vector writeObjIDs = target.getObjIDs(iObjID, false);
						// invokeRollup(target, writeObjIDs);

						// Revalidate the navigation requests
						validateRequests();
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> ERROR : Invalid target");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> ERROR : Target not active");
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Activity does not exist");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - "
					+ "clearAttemptObjMeasure");
		}
	}

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
	public void setAttemptObjMeasure(String iID, String iObjID, double iMeasure) {
		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "setAttemptObjMeasure");
			System.out.println("  ::--> Target activity: " + iID);
			System.out.println("  ::--> Objective:       " + iObjID);
			System.out.println("  ::--> Measure:         " + iMeasure);
		}

		// Find the target activty
		SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {

			// Make sure the activity is a valid target for status changes
			// -- the tracked active leaf current activity
			if (target.getIsActive() && target.getIsTracked()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false)
						&& mSeqTree.getCurrentActivity() == target) {
					/* boolean statusChange = */
					target.setObjMeasure(iObjID, iMeasure);

					if (true /* statusChange */) {
						// If the activity's status changed, it may affect other
						// activities -- invoke rollup

						Vector writeObjIDs = target.getObjIDs(iObjID, false);
						// invokeRollup(target, writeObjIDs);

						// Revalidate the navigation requests
						validateRequests();
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> ERROR : Invalid target");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> ERROR : Target not active");
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Activity does not exist");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - "
					+ "setAttemptObjMeasure");
		}
	}

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
	public void setAttemptObjSatisfied(String iID, String iObjID, String iStatus) {
		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "setAttemptObjSatisfied");
			System.out.println("  ::--> Target activity: " + iID);
			System.out.println("  ::--> Objective:       " + iObjID);
			System.out.println("  ::--> Status:          " + iStatus);
		}

		// Find the activty whose status is being set
		SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {

			// Make sure the activity is a valid target for status changes
			// -- the tracked active leaf current activity
			if (target.getIsActive() && target.getIsTracked()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false)
						&& mSeqTree.getCurrentActivity() == target) {
					boolean statusChange = target.setObjSatisfied(iObjID,
							iStatus);

					if (statusChange) {
						// If the activity's status changed, it may affect other
						// activities -- invoke rollup
						Vector writeObjIDs = target.getObjIDs(iObjID, false);
						// invokeRollup(target, writeObjIDs);

						// Revalidate the navigation requests
						validateRequests();
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> ERROR : Invalid target");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> ERROR : Target not active");
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Activity does not exist");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - "
					+ "setAttemptObjSatisfied");
		}
	}

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
	public void setAttemptProgressStatus(String iID, String iProgress) {
		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "setAttemptProgressStatus");
			System.out.println("  ::--> Target activity:    " + iID);
			System.out.println("  ::--> Completion status:  " + iProgress);
		}

		SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {
			// Make sure the activity is a valid target for status changes
			// -- the tracked active leaf current activity
			if (target.getIsActive() && target.getIsTracked()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false)
						&& mSeqTree.getCurrentActivity() == target) {
					boolean statusChange = target.setProgress(iProgress);

					if (statusChange) {

						// If the activity's status changed, it may affect other
						// activities -- invoke rollup
						// invokeRollup(target, null);

						// Revalidate the navigation requests
						validateRequests();
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> ERROR : Invalid target");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> ERROR : Target not active");
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Activity does not exist");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - "
					+ "setAttemptProgressStatus");
		}
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	 
	 Implementation of SeqNavigation 
	 
	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

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
	public ADLLaunch navigate(String iTarget) {

		// This method implements case 7 of the Navigation Request Process
		// (NB.2.1).
		//
		// It also applies the Overall Sequencing Process (OP) to the
		// indicated navigation request.

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> BEGIN - navigate[choice]");
			System.out.println("  :: [" + iTarget + "]");
		}

		ADLLaunch launch = new ADLLaunch();

		// Make sure an activity tree has been associated with this sequencer
		if (mSeqTree == null) {
			if (_Debug) {
				System.out.println("  ::--> ERROR : No activity tree defined.");
				System.out.println("  :: ADLSequencer --> END   - "
						+ "navigate[choice]");
			}

			// No activity tree, therefore nothing to do
			// -- inform the caller of the error.
			launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
			launch.mEndSession = true;

			return launch;
		}

		// Make sure the requested activity exists
		ISeqActivity target = getActivity(iTarget);

		if (target != null) {
			// If this is a new session, we start at the root.
			boolean newSession = false;

			SeqActivity cur = mSeqTree.getCurrentActivity();

			if (cur == null) {
				prepareClusters();
				newSession = true;
			}

			boolean process = true;

			validateRequests();

			// If the sequencing session has already begun, confirm the
			// navigation request is valid.
			if (!newSession) {
				ADLValidRequests valid = mSeqTree.getValidRequests();

				if (valid != null) {
					// Confirm the target activity is allowed
					if (valid.mChoice != null) {
						ActivityNode testNode = (ActivityNode)valid.mChoice.get(iTarget);
						//ADLTOC test = (ADLTOC) valid.mChoice.get(iTarget);

						if (testNode == null) {
							if (_Debug) {
								System.out
										.println("  ::--> Target not available");
							}

							launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;

							process = false;
						} else if (!testNode.isSelectable()) {
							if (_Debug) {
								System.out
										.println("  ::--> Target not selectable");
							}

							launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;

							process = false;
						}
					} else {
						if (_Debug) {
							System.out.println("  ::--> No 'choice' enabled");
						}

						launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;

						process = false;
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> ERROR : "
								+ "Cannot validate request");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
					launch.mEndSession = true;

					// Invalid request -- do not process
					process = false;
				}
			}

			// If the navigation request is valid process it
			if (process) {
				// This block implements the overall sequencing loop

				// Clear Global State
				mValidTermination = true;
				mValidSequencing = true;

				String seqReq = iTarget;
				String delReq = null;

				// Check if a termination is required
				if (!newSession) {
					if (cur.getIsActive()) {
						// Issue a termination request of 'exit'
						seqReq = doTerminationRequest(ADLSequencer.TER_EXIT,
								false);

						if (seqReq == null) {
							seqReq = iTarget;
						}
					}
				}

				if (mValidTermination) {
					// Issue the pending sequencing request
					delReq = doSequencingRequest(seqReq);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> Bad Request");
				}

				launch.mNavState = mSeqTree.getValidRequests();
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : The target activity is "
						+ "not in the tree");
			}

			launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
			launch.mEndSession = true;
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - "
					+ "navigate[choice]");
		}

		return launch;
	}

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
	public ADLLaunch navigate(int iRequest) {

		// This method implements all cases, except case #7 of the Navigation
		// Request Process (NB.2.1).
		//
		// It also applies the Overall Sequencing Process (OP) to the
		// indicated navigation request.

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - navigate");
			System.out.println("  ::--> " + iRequest);
		}

		// This function attempts to translate the navigation request into the
		// corresponding termination and sequencing requests, and invoke the
		// overall sequencing process.

		ADLLaunch launch = new ADLLaunch();

		// Make sure an activity tree has been associated with this sequencer
		if (mSeqTree == null) {
			if (_Debug) {
				System.out.println("  ::--> ERROR : No activity tree defined.");
				System.out.println("  :: ADLSequencer --> END   - "
						+ "navigate");
			}

			// No activity tree, therefore nothing to do
			// -- inform the caller of the error.
			launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
			launch.mEndSession = true;

			return launch;
		}

		// If this is a new session, we start at the root.
		boolean newSession = false;

		SeqActivity cur = mSeqTree.getCurrentActivity();

		if (cur == null) {
			if (_Debug) {
				System.out
						.println("  ::--> No current Activity -- New Session");
			}

			prepareClusters();
			newSession = true;

			validateRequests();
		}

		boolean process = true;

		ADLValidRequests valid = null;

		if (newSession && iRequest == SeqNavRequests.NAV_NONE) {
			if (_Debug) {
				System.out.println("  ::--> Processing a TOC request");
			}
		} else if (newSession
				&& (iRequest == SeqNavRequests.NAV_EXITALL || iRequest == SeqNavRequests.NAV_ABANDONALL)) {
			if (_Debug) {
				System.out
						.println("  ::--> Exiting a session that hasn't started");
			}

			launch.mSeqNonContent = ADLLaunch.LAUNCH_EXITSESSION;
			launch.mEndSession = true;

			process = false;
		} else if (iRequest == SeqNavRequests.NAV_CONTINUE
				|| iRequest == SeqNavRequests.NAV_PREVIOUS) {
			validateRequests();
			valid = mSeqTree.getValidRequests();

			// Can't validate requests -- Error
			if (valid == null) {
				if (_Debug) {
					System.out.println("  ::--> ERROR : "
							+ "Cannot validate request");
				}

				launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
				launch.mEndSession = true;

				// Invalid request -- do not process
				process = false;
			} else {
				if (iRequest == SeqNavRequests.NAV_CONTINUE) {
					if (!valid.mContinue) {
						if (_Debug) {
							System.out.println("  ::--> Continue not valid");
						}

						process = false;
						launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;
					}
				} else {
					if (!valid.mPrevious) {
						if (_Debug) {
							System.out.println("  ::--> Previous not valid");
						}

						process = false;
						launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;
					}
				}
			}
		} else {
			// Use the IMS Navigation Request Process to validate the request
			process = doIMSNavValidation(iRequest);

			if (!process) {
				launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;
			}
		}

		// Process any pending navigation request
		if (process) {
			// This block implements the overall sequencing loop

			// Clear Global State
			mValidTermination = true;
			mValidSequencing = true;

			String seqReq = null;
			String delReq = null;

			// Translate the navigation request into termination and/or
			// sequencing
			// request(s).
			switch (iRequest) {

			case SeqNavRequests.NAV_START:

				delReq = doSequencingRequest(ADLSequencer.SEQ_START);

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_RESUMEALL:

				delReq = doSequencingRequest(ADLSequencer.SEQ_RESUMEALL);

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_CONTINUE:

				if (cur.getIsActive()) {
					// Issue a termination request of 'exit'
					seqReq = doTerminationRequest(ADLSequencer.TER_EXIT, false);
				}

				if (mValidTermination) {
					// Issue the pending sequencing request
					if (seqReq == null) {
						delReq = doSequencingRequest(ADLSequencer.SEQ_CONTINUE);
					} else {
						delReq = doSequencingRequest(seqReq);
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_PREVIOUS:

				if (cur.getIsActive()) {
					// Issue a termination request of 'exit'
					seqReq = doTerminationRequest(ADLSequencer.TER_EXIT, false);
				}

				if (mValidTermination) {
					// Issue the pending sequencing request
					if (seqReq == null) {
						delReq = doSequencingRequest(ADLSequencer.SEQ_PREVIOUS);
					} else {
						delReq = doSequencingRequest(seqReq);
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_ABANDON:

				// Issue a termination request of 'abandon'
				seqReq = doTerminationRequest(ADLSequencer.TER_ABANDON, false);

				// The termination process cannot return a sequencing request
				// because post condition rules are not evaluated.
				if (mValidTermination) {
					if (seqReq != null) {
						if (_Debug) {
							System.out.println("  ::--> ERROR : "
									+ "Postconditions Processed");
						}
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);

					// If the session hasn't ended, re-validate nav requests
					if (!mEndSession && !mExitCourse) {
						if (_Debug) {
							System.out.println("  ::--> REVALIDATE");
						}

						validateRequests();
					}

				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_ABANDONALL:

				// Issue a termination request of 'abandonAll'
				seqReq = doTerminationRequest(ADLSequencer.TER_ABANDONALL, false);

				// The termination process cannot return a sequencing request
				// because post condition rules are not evaluated.
				if (mValidTermination) {
					if (seqReq != null) {
						if (_Debug) {
							System.out.println("  ::--> ERROR : "
									+ "Postconditions Processed");
						}
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_SUSPENDALL:

				// Issue a termination request of 'suspendAll'
				seqReq = doTerminationRequest(ADLSequencer.TER_SUSPENDALL,
						false);

				// The termination process cannot return a sequencing request
				// because post condition rules are not evaluated.
				if (mValidTermination) {
					if (seqReq != null) {
						if (_Debug) {
							System.out.println("  ::--> ERROR : "
									+ "Postconditions Processed");
						}
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);

				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_EXIT:

				// Issue a termination request of 'exit'
				seqReq = doTerminationRequest(ADLSequencer.TER_EXIT, false);

				if (mValidTermination) {
					if (seqReq == null) {
						delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);
					} else {
						delReq = doSequencingRequest(seqReq);
					}

					// If the session hasn't ended, re-validate nav requests
					if (!mEndSession && !mExitCourse) {
						if (_Debug) {
							System.out.println("  ::--> REVALIDATE");
						}

						validateRequests();
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_EXITALL:

				// Issue a termination request of 'exitAll'
				seqReq = doTerminationRequest(ADLSequencer.TER_EXITALL, false);

				// The termination process cannot return a sequencing request
				// because post condition rules are not evaluated.
				if (mValidTermination) {
					if (seqReq != null) {
						if (_Debug) {
							System.out.println("  ::--> ERROR : "
									+ "Postconditions Processed");
						}
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Termination");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
					if (_Debug) {
						System.out.println("  ::--> Invalid Sequencing");
					}

					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_NONE:

				// Don't invoke any termination or sequencing requests,
				// but display a TOC if available
				launch.mSeqNonContent = ADLLaunch.LAUNCH_TOC;

				launch.mNavState = mSeqTree.getValidRequests();

				// Make sure that a TOC is realy available
				// TODO: Moved from mTOC to mTreeModel
				if (launch.mNavState.mTreeModel == null) {
					launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;
				}

				break;

			default:

				if (_Debug) {
					System.out.println("  ::-->  ERROR : "
							+ "Invalid navigation request: " + iRequest);
				}

				launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> INVALID NAV REQUEST");
			}

			launch.mNavState = mSeqTree.getValidRequests();

			// If navigation requests haven't been validated, try to validate
			// now.
			if (launch.mNavState == null) {
				if (_Debug) {
					System.out
							.println("  ::--> Not Validated Yet -- DO IT NOW");
				}

				validateRequests();
				launch.mNavState = mSeqTree.getValidRequests();
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - navigate");
		}

		return launch;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Private Methods

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Retrieve the activity (<code>SeqActivity</code>) with the associated
	 * activity ID.
	 * 
	 * @param iActivityID
	 *            ID of the desired activity.
	 * 
	 * @return The activity (<code>SeqActivity</code>) with the associated
	 *         activity ID, or <code>null</code> if it does not exist.
	 */
	private SeqActivity getActivity(String iActivityID) {

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - getActivity");
			System.out.println("  ::-->  " + iActivityID);
		}

		SeqActivity thisActivity = null;

		if (mSeqTree != null) {
			// Get an activity node from the activity tree based on its ID
			thisActivity = mSeqTree.getActivity(iActivityID);

			if (_Debug) {
				System.out.println("  ::-->  FOUND");
				System.out.println("  :: ADLSequencer --> END   - getActivity");
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : No Activity Tree");
				System.out.println("  :: ADLSequencer --> END   - getActivity");
			}
		}

		return thisActivity;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Navigation Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Validates the indicated navigation request according to the IMS
	 * Navigation Request Process Behavior.
	 * 
	 * @param iRequest
	 *            The Request being valididated.
	 * 
	 * @return <code>true</code> if the indentified request is valid according
	 *         to the IMS Navigation Request Process.
	 */
	private boolean doIMSNavValidation(int iRequest) {

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> BEGIN - doIMSNavValidation");
		}

		boolean ok = true;

		// Assume the navigation request is valid
		boolean process = true;

		// If this is a new session, we start at the root.
		boolean newSession = false;

		SeqActivity cur = mSeqTree.getCurrentActivity();
		SeqActivity parent = null;

		if (cur == null) {
			newSession = true;
		} else {
			parent = cur.getParent();
		}

		// Validate the pending navigation request before processing it.
		// The following tests implement the validation logic of NB.2.1; it
		// covers all cases where the requst, itself, is invalid.
		switch (iRequest) {

		case SeqNavRequests.NAV_START:

			if (!newSession) {
				if (_Debug) {
					System.out.println("  ::--> 'Start' not valid");
				}

				process = false;
			}

			break;

		case SeqNavRequests.NAV_RESUMEALL:

			ok = true;

			if (!newSession) {
				ok = false;
			} else if (mSeqTree.getSuspendAll() == null) {
				ok = false;
			}

			// Request not valid
			if (!ok) {
				if (_Debug) {
					System.out.println("  ::--> 'ResumeAll' not valid");
				}

				process = false;
			}

			break;

		case SeqNavRequests.NAV_CONTINUE:

			// Request not valid
			if (newSession) {
				if (_Debug) {
					System.out.println("  ::--> 'Continue' not valid");
				}

				process = false;
			} else {
				if (parent == null || !parent.getControlModeFlow()) {
					if (_Debug) {
						System.out.println("  ::--> 'Continue' not valid");
					}

					process = false;

				}
			}

			break;

		case SeqNavRequests.NAV_PREVIOUS:

			if (newSession) {
				if (_Debug) {
					System.out.println("  ::--> 'Previous' not valid");
				}

				process = false;
			} else {
				if (parent != null) {
					if (!parent.getControlModeFlow()
							|| parent.getControlForwardOnly()) {
						if (_Debug) {
							System.out.println("  ::--> 'Previous' not valid "
									+ "-- Control Mode");
						}

						process = false;
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> 'Previous' not valid "
								+ "-- NULL Parent");
					}

					process = false;
				}
			}

			break;

		case SeqNavRequests.NAV_ABANDON:

			ok = true;

			if (newSession) {
				ok = false;
			} else if (!cur.getIsActive()) {
				ok = false;
			}

			// Request is not valid
			if (!ok) {
				if (_Debug) {
					System.out.println("  ::--> 'Abandon' not valid");
				}

				process = false;
			}

			break;

		case SeqNavRequests.NAV_ABANDONALL:

			if (newSession) {
				if (_Debug) {
					System.out.println("  ::--> 'AbandonAll' not valid");
				}

				process = false;
			}

			break;

		case SeqNavRequests.NAV_SUSPENDALL:

			if (newSession) {
				if (_Debug) {
					System.out.println("  ::--> 'Abandon' not valid");
				}

				process = false;
			}

			break;

		case SeqNavRequests.NAV_EXIT:

			if (newSession) {
				ok = false;
			} else if (!cur.getIsActive()) {
				ok = false;
			}

			// Request not valid
			if (!ok) {
				if (_Debug) {
					System.out.println("  ::--> 'Exit' not valid");
				}

				process = false;
			}

			break;

		case SeqNavRequests.NAV_EXITALL:

			if (newSession) {
				if (_Debug) {
					System.out.println("  ::--> 'ExitAll' not valid");
				}

				process = false;
			}

			break;

		default:

			if (_Debug) {
				System.out.println("  ::--> Invalid navigation request: "
						+ iRequest);
			}

			process = false;
		}

		if (_Debug) {
			System.out.println("  ::--> " + process);
			System.out
					.println("  :: ADLSequencer --> END   - doIMSNavValidation");
		}

		return process;
	}

	/**
	 * Performs 'what-if' evaluations of possible sequencing requests
	 * originating from the current activity, given the current state of the
	 * activity tree.
	 * 
	 * <br>
	 * <br>
	 * This method assumes that content has already reported status and rollup
	 * has been performed.
	 */
	private void validateRequests() {

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> BEGIN - validateRequests");
		}

		ADLValidRequests valid = mSeqTree.getValidRequests();

		// If there is no current activity or the current activity is inactive,
		// no state change could have occured since the last validation.
		SeqActivity cur = mSeqTree.getCurrentActivity();

		if (cur != null) {
			boolean test = false;
			valid = new ADLValidRequests();
			ADLLaunch tempLaunch = new ADLLaunch();

			// Clear global state
			mValidTermination = true;
			mValidSequencing = true;

			String seqReq = null;
			boolean seqReqSuccess = false;

			String delReq = null;

			// If there is a current activity, 'suspendAll' is valid
			valid.mSuspend = true;

			// If the current activity does not prevent choiceExit,
			// Test all 'Choice' requests
			if (cur.getControlModeChoiceExit() || !cur.getIsActive()) {
				// TODO: Remove this
				//valid.mTOC = getTOC(mSeqTree.getRoot());
				valid.mTreeModel = getTreeModel(mSeqTree.getRoot());		//convertTOC(valid.mTOC);
			}
			
			
			if (valid.mTreeModel != null) {
				valid.mChoice = getChoiceSet(valid.mTreeModel);
			}
			
			if (valid.mTreeModel != null && valid.mTreeModel.getRoot() != null && ((ActivityNode)valid.mTreeModel.getRoot()).getChildCount() == 0) {
				valid.mTreeModel = null;
			}
			
			// TODO: Remove this.
			/*if (valid.mTOC != null) {
				Vector newTOC = new Vector();

				valid.mChoice = getChoiceSet((Vector) valid.mTOC, newTOC);

				if (newTOC.size() > 0) {
					valid.mTOC = newTOC;
					//valid.mTreeModel = convertTOC(newTOC);
				} else {
					valid.mTOC = null;
					valid.mTreeModel = null;
				}
			}*/

			if (cur.getParent() != null) {
				if (_Debug) {
					System.out.println("  ::--> Validate 'Continue'");
				}

				// Always provide a Continue Button if the current activity
				// is in a 'Flow' cluster
				if (cur.getParent().getControlModeFlow()) {
					valid.mContinue = true;
				}

				if (_Debug) {
					System.out.println("  ::--> Validate 'Previous'");
				}

				test = doIMSNavValidation(SeqNavRequests.NAV_PREVIOUS);

				if (test) {
					// Test the 'Previous' request
					mValidSequencing = true;

					delReq = doSequencingRequest(ADLSequencer.SEQ_PREVIOUS);
					if (mValidSequencing) {
						valid.mPrevious = doDeliveryRequest(delReq, true,
								tempLaunch);
					}
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> No current activity");
			}

			valid = new ADLValidRequests();

			if (_Debug) {
				System.out.println("  ::--> Validate 'Start' and 'Resume'");
			}

			// Check to see if a resume All should be processed instead of a
			// start
			if (mSeqTree.getSuspendAll() != null) {
				valid.mResume = true;
			} else {
				// Test Start Navigation Request
				Walk walk = new Walk();
				walk.at = mSeqTree.getRoot();

				valid.mStart = processFlow(ADLSequencer.FLOW_FORWARD, true,
						walk, false);

				// Validate availablity of the identfied activity if one was
				// identified
				if (valid.mStart) {
					boolean ok = true;

					while (walk.at != null && ok) {
						if (_Debug) {
							System.out.println("  ::-->  Checking --> "
									+ walk.at.getID());
						}

						ok = !checkActivity(walk.at);

						if (ok) {
							walk.at = walk.at.getParent();
						} else {
							valid.mStart = false;
						}
					}
				}
			}

			if (_Debug) {
				if (valid.mStart) {
					System.out.println("  ::--> 'Start' Request is VALID");
				} else {
					System.out.println("  ::--> 'Start' Request is INVALID");
				}

				if (valid.mResume) {
					System.out.println("  ::--> 'Resume All' Request is VALID");
				} else {
					System.out
							.println("  ::--> 'Resume All' Request is INVALID");
				}
			}

			if (_Debug) {
				System.out.println("  ::--> Validate 'Choice' requests");
			}

			// Test all 'Choice' requests
			// TODO: Remove this
			//valid.mTOC = getTOC(mSeqTree.getRoot());
			valid.mTreeModel = getTreeModel(mSeqTree.getRoot()); //convertTOC(valid.mTOC);

			if (valid.mTreeModel != null) {
				valid.mChoice = getChoiceSet(valid.mTreeModel);
			}
			
			if (valid.mTreeModel != null && valid.mTreeModel.getRoot() != null && ((ActivityNode)valid.mTreeModel.getRoot()).getChildCount() == 0) {
				valid.mTreeModel = null;
			}
			
			// TODO: Remove this
			/*if (valid.mTOC != null) {
				Vector newTOC = new Vector();

				valid.mChoice = getChoiceSet((Vector) valid.mTOC, newTOC);

				if (newTOC.size() > 0) {
					valid.mTOC = newTOC;
					//valid.mTreeModel = convertTOC(newTOC);
				} else {
					valid.mTOC = null;
					valid.mTreeModel = null;
				}
			}*/
		}

		// If an updated set of valid requests has completed, associated it with
		// the activity tree
		if (valid != null) {
			mSeqTree.setValidRequests(valid);
		}

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> END   - validateRequests");
		}
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Termination Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * This method evaluates 'Exit' rules of all active clusters. <br>
	 * <br>
	 * This is an implementation of the Sequencing Exit Action Rules Subprocess
	 * (TB.2.1).
	 * 
	 * @param iTentative
	 *            Indicates if descendent activities should be terminated.
	 */
	private void evaluateExitRules(boolean iTentative) {

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> BEGIN - evaluateExitRules");
			System.out.println("  ::--> REAL? -- "
					+ ((iTentative) ? "NO" : "YES"));
		}

		// Clear global state
		mExitCourse = false;

		// Always begin processing at the current activity
		SeqActivity start = mSeqTree.getCurrentActivity();

		SeqActivity exitAt = null;
		String exited = null;

		Vector path = new Vector();

		if (start != null) {
			SeqActivity parent = start.getParent();

			while (parent != null) {
				if (_Debug) {
					System.out.println("  ::--> Adding :: " + parent.getID());
				}

				path.add(parent);

				parent = parent.getParent();
			}

			// Starting at the root, walk down the tree to the current activity'
			// parent.
			while (path.size() > 0 && (exited == null)) {
				parent = (SeqActivity) path.elementAt(path.size() - 1);
				path.remove(path.size() - 1);

				if (_Debug) {
					System.out.println("  ::--> Evaluating 'Exit' at -- "
							+ parent.getID());
				}

				// Attempt to get rule information from the activity node
				ISeqRuleset exitRules = parent.getExitSeqRules();

				if (exitRules != null) {
					exited = exitRules.evaluate(SeqRuleset.RULE_TYPE_EXIT,
							parent, false);
				}

				// If the rule evaluation did not return null, the activity must
				// have exited.
				if (exited != null) {
					exitAt = parent;
				}
			}

			if (exited != null) {
				if (exitAt == mSeqTree.getRoot()) {
					if (_Debug) {
						System.out.println("  ::--> ROOT   <<< Exited");
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> " + exitAt.getID()
								+ "  <<< Exited");
					}
				}

				// If this was a 'real' evaluation, end the appropriate
				// attempts.
				if (!iTentative) {
					// If an activity exited, end attempts at all remaining
					// cluster
					// on the 'active' branch.
					terminateDescendentAttempts(exitAt);

					// End the attempt on the 'exited' activity
					endAttempt(exitAt, false);

					// invokeRollup(cur, null);
				}

				// Sequencing requests begin at the 'exited' activity
				mSeqTree.setFirstCandidate(exitAt);
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : NULL Current Activity");
			}
		}

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> END   - evaluateExitRules");
		}
	}

	/**
	 * Processes a specified termination request on the current activity tree.
	 * <br>
	 * <b>Internal Sequencing Process</b><br>
	 * <br>
	 * 
	 * @param iRequest
	 *            Identifies the termination request being processed.
	 * 
	 * @param iTentative
	 *            Indicates if attempts should ended.
	 * 
	 * @return May return a sequencing request (<code>String</code>) that
	 *         over rides any existing sequencing request, or <code>null</code>.
	 */

	private String doTerminationRequest(String iRequest, boolean iTentative) {

		// This method implements the Termination Request Process (TB.2.3).
		//
		// The Termination Request Process ensures the activity tree is in the
		// most current state, to ensure that subsequent sequencing requests ar
		// processed on up todate data.
		//
		// The termination request is processed from the current activity and
		// determines what the first candidate for sequencing should is.
		// If this is a 'real' termination request, the current activity is mov
		// to the identified first candidate activity.

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "doTerminationRequest");
			System.out.println("  ::--> Request:  " + iRequest);
			System.out.println("  ::--> Real?     "
					+ ((iTentative) ? "NO" : "YES"));
		}

		// The Termination Request Process may return a sequencing request
		String seqReq = null;
		mExitAll = false;

		// Ensure the request exists
		if (iRequest == null) {

			mValidTermination = false;

			if (_Debug) {
				System.out.println("  ::--> NULL request");
				System.out.println("  :: ADLSequencer --> END   - "
						+ "doTerminationRequest");
			}

			return seqReq;
		}

		// The Sequencing Request Process will always begin processing at the
		// 'first candidate'.
		// Assume the first candidate for sequencing is the current activity.
		SeqActivity cur = mSeqTree.getCurrentActivity();

		if (cur != null) {
			mSeqTree.setFirstCandidate(cur);
		} else {

			mValidTermination = false;

			if (_Debug) {
				System.out.println("  ::--> No current activity");
				System.out.println("  :: ADLSequencer --> END   - "
						+ "doTerminationRequest");
			}

			return seqReq;
		}

		// Apply the termination request
		if (iRequest.equals(ADLSequencer.TER_EXIT)) {

			// Make sure the current activity is active.
			if (cur.getIsActive()) {

				// End the attempt on the current activity
				endAttempt(cur, iTentative);

				// Evaluate exit action rules
				evaluateExitRules(iTentative);

				// Evaluate post conditions
				boolean exited = false;

				do {
					exited = false;

					// Only process post conditions on the first candidate
					SeqActivity process = mSeqTree.getFirstCandidate();

					// Make sure we are not at the root
					if (!mExitCourse) {
						// This block implements the Sequencing Post Condition
						// Rule
						// Subprocess (SB.2.2)

						if (_Debug) {
							System.out
									.println("  ::--> Evaluating 'POST' at -- "
											+ process.getID());
						}

						// Attempt to get rule information from the activity
						ISeqRuleset postRules = process.getPostSeqRules();

						if (postRules != null) {
							String result = null;
							result = postRules.evaluate(
									SeqRuleset.RULE_TYPE_POST, process, false);

							if (result != null) {
								if (_Debug) {
									System.out.println("  ::--> " + result);
								}

								// This set of ifs implement TB.2.2
								if (result.equals(SeqRule.SEQ_ACTION_RETRY)) {
									// Override any existing sequencing request
									seqReq = ADLSequencer.SEQ_RETRY;

									// If we are processing the root activity,
									// behave
									// as if this where an exitAll
									if (process == mSeqTree.getRoot()) {
										// Break from the current loop and jump
										// to the
										// next case
										iRequest = ADLSequencer.TER_EXITALL;
									}
								} else if (result
										.equals(SeqRule.SEQ_ACTION_CONTINUE)) {
									// Override any existing sequencing request
									seqReq = ADLSequencer.SEQ_CONTINUE;
								} else if (result
										.equals(SeqRule.SEQ_ACTION_PREVIOUS)) {
									// Override any existing sequencing request
									seqReq = ADLSequencer.SEQ_PREVIOUS;
								} else if (result
										.equals(SeqRule.SEQ_ACTION_EXITALL)) {
									// Break from the current loop and jump to
									// the
									// next case
									iRequest = ADLSequencer.TER_EXITALL;
								} else if (result
										.equals(SeqRule.SEQ_ACTION_EXITPARENT)) {
									process = process.getParent();

									if (process == null) {
										if (_Debug) {
											System.out
													.println("  ::--> ERROR :: "
															+ " No parent to exit");
										}
									} else {

										mSeqTree.setFirstCandidate(process);
										endAttempt(process, iTentative);

										exited = true;
									}
								} else if (result
										.equals(SeqRule.SEQ_ACTION_RETRYALL)) {
									// Override any existing sequencing request
									seqReq = ADLSequencer.SEQ_RETRY;

									// Break from the current loop and jump to
									// the
									// next case
									iRequest = ADLSequencer.TER_EXITALL;
								} else if (process == mSeqTree.getRoot()) {
									// Exited Root with no postcondition rules
									// End the Course
									mExitCourse = true;
								}
							} else {
								if (_Debug) {
									System.out
											.println("  ::--> NULL Evaluation");
								}
							}
						} else if (process == mSeqTree.getRoot()) {
							// Exited Root with no postcondition rules
							// End the Course
							mExitCourse = true;
						}
					} else {
						if (_Debug) {
							System.out.println("  --> Exited Course");
							seqReq = ADLSequencer.SEQ_EXIT;
						}
					}
				} while (exited);
			} else {
				if (_Debug) {
					System.out.println("  ::--> INVALID :: "
							+ "activity inactive");
				}

				mValidTermination = false;
			}
		}

		// Double check for an EXIT request
		if (iRequest.equals(ADLSequencer.TER_EXIT)) {
			// Already handled
		} else if (iRequest.equals(ADLSequencer.TER_EXITALL)) {
			if (_Debug) {
				System.out.println("  ::--> Processing EXIT ALL");
			}

			// Don't modify the activity tree if this is only a tentative exit
			if (!iTentative) {
				SeqActivity process = mSeqTree.getFirstCandidate();

				if (process.getIsActive()) {
					endAttempt(process, false);
				}

				terminateDescendentAttempts(mSeqTree.getRoot());

				endAttempt(mSeqTree.getRoot(), false);

				// only exit if we're not retrying the root
				if (seqReq != ADLSequencer.SEQ_RETRY) {
					seqReq = ADLSequencer.SEQ_EXIT;
				}

				// Start any subsequent seqencing request from the root
				mSeqTree.setFirstCandidate(mSeqTree.getRoot());

			} else {
				// Although this was a tentative evaluation, remember that we
				// processed the exitAll so that a retry from the root can be
				// tested
				// mExitAll = true;
			}

			// Start any subsequent seqencing request from the root
			mSeqTree.setFirstCandidate(mSeqTree.getRoot());

		} else if (iRequest.equals(ADLSequencer.TER_SUSPENDALL)) {
			// Don't modify the activty tree if this is only a tentative exit
			if (!iTentative) {
				SeqActivity process = mSeqTree.getFirstCandidate();

				if (process.getIsActive()) {
					// Invoke rollup
					invokeRollup(process, null);

					mSeqTree.setSuspendAll(process);

					// Check to see if the SCO's learner attempt ended
					if (!process.getIsSuspended()) {
						process.incrementSCOAttempt();
					}
				} else {
					if (!process.getIsSuspended()) {
						mSeqTree.setSuspendAll(process.getParent());

						// Make sure there was a an activity to suspend
						if (mSeqTree.getSuspendAll() == null) {
							mValidTermination = false;
						}
					}
				}

				if (mValidTermination) {
					SeqActivity start = mSeqTree.getSuspendAll();

					// This process suspends all clusters up to the root
					while (start != null) {
						start.setIsActive(false);
						start.setIsSuspended(true);

						start = start.getParent();
					}
				}
			}

			// Start any subsequent seqencing request from the root
			mSeqTree.setFirstCandidate(mSeqTree.getRoot());

		} else if (iRequest.equals(ADLSequencer.TER_ABANDON)) {
			// Don't modify the activty tree if this is only a tentativen exit
			if (!iTentative) {
				SeqActivity process = mSeqTree.getFirstCandidate();

				if (_Debug) {
					System.out.println("  --> CLEARING STATE ABANDON");
				}

				// Ignore any status values reported by the content
				process.setProgress(ADLTracking.TRACK_UNKNOWN);
				process.setObjSatisfied(null, ADLTracking.TRACK_UNKNOWN);
				process.clearObjMeasure(null);

				process.setIsActive(false);
			}
		} else if (iRequest.equals(ADLSequencer.TER_ABANDONALL)) {
			// Don't modify the activty tree if this is only a tentative exit
			if (!iTentative) {
				SeqActivity process = mSeqTree.getFirstCandidate();

				if (_Debug) {
					System.out.println("  --> CLEARING STATE ABANDONALL");
				}

				// Ignore any status values reported by the content
				process.setProgress(ADLTracking.TRACK_UNKNOWN);
				process.setObjSatisfied(null, ADLTracking.TRACK_UNKNOWN);
				process.clearObjMeasure(null);

				while (process != null) {
					process.setIsActive(false);

					process = process.getParent();
				}

				seqReq = ADLSequencer.SEQ_EXIT;

				// Start any subsequent seqencing request from the root
				mSeqTree.setFirstCandidate(mSeqTree.getRoot());
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> INVALID :: " + "invalid request");
			}

			mValidTermination = false;
		}

		// If this was a 'real' termination request, move the current activity
		if (!iTentative) {
			mSeqTree.setCurrentActivity(mSeqTree.getFirstCandidate());
		}

		String tmpID = mSeqTree.getFirstCandidate().getID();

		if (_Debug) {
			System.out.println("  ::--> SEQ REQ      :: " + seqReq);
			System.out.println("  ::--> FIRST        :: " + tmpID);
			System.out.println("  ::--> EXIT COURSE  :: " + mExitCourse);
			System.out.println("  :: ADLSequencer --> END   - "
					+ "doTerminationRequest");
		}

		return seqReq;

	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Rollup Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Initiates deterministic rollup from the target activity and any other
	 * activities that may have been affected. <br>
	 * <b>Internal Sequencing Process</b><br>
	 * <br>
	 * 
	 * @param ioTarget
	 *            Identifies the activity where rollup is applied.
	 * 
	 * @param iWriteObjIDs
	 *            Identifies the set of objective IDs that are affected by this
	 *            invokation; or <code>null</code> if none.
	 */
	private void invokeRollup(SeqActivity ioTarget, Vector iWriteObjIDs) {

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - invokeRollup");
			System.out.println("  ::-->  Start: " + ioTarget.getID());
		}

		Hashtable rollupSet = new Hashtable();

		// Case #1 -- Rollup applies along the active path
		if (ioTarget == mSeqTree.getCurrentActivity()) {

			if (_Debug) {
				System.out.println("  ::--> CASE #1 Rollup");
			}

			SeqActivity walk = ioTarget;

			// Walk from the target to the root, apply rollup rules at each step
			while (walk != null) {

				if (_Debug) {
					System.out.println("  ::--> Adding :: " + walk.getID());
				}

				rollupSet.put(walk.getID(), new Integer(walk.getDepth()));

				Vector writeObjIDs = walk.getObjIDs(null, false);

				if (writeObjIDs != null) {
					for (int i = 0; i < writeObjIDs.size(); i++) {
						String objID = (String) writeObjIDs.elementAt(i);

						if (_Debug) {
							System.out.println("  ::--> Rolling up Obj -- "
									+ objID);
						}

						// Need to identify all activity's that 'read' this
						// objective
						// into their primary objective -- those activities need
						// to be
						// included in the rollup set
						Vector acts = mSeqTree.getObjMap(objID);

						if (_Debug) {
							System.out.println("  ACTS == " + acts);
						}

						if (acts != null) {
							for (int j = 0; j < acts.size(); j++) {
								SeqActivity act = getActivity(((String) acts
										.elementAt(j)));

								if (_Debug) {
									System.out.println("  *+> " + j
											+ " <+*  :: " + act.getID());
								}

								// Only rollup at the parent of the affected
								// activity
								act = act.getParent();

								if (act != null) {
									// Only add if the activity is selected
									if (act.getIsSelected()) {
										if (_Debug) {
											System.out
													.println("  ::--> Adding :: "
															+ act.getID());
										}

										rollupSet.put(act.getID(), new Integer(
												act.getDepth()));
									}
								}
							}
						}
					}
				}

				walk = walk.getParent();
			}

			// Remove the Current Activity from the rollup set
			rollupSet.remove(ioTarget.getID());

		}

		// Case #2 -- Rollup applies when the state of a global shared objective
		// is written to...
		if (iWriteObjIDs != null) {

			if (_Debug) {
				System.out.println("  ::--> CASE #2 Rollup");
			}

			for (int i = 0; i < iWriteObjIDs.size(); i++) {
				String objID = (String) iWriteObjIDs.elementAt(i);

				if (_Debug) {
					System.out.println("  ::--> Rolling up Obj -- " + objID);
				}

				// Need to identify all activity's that 'read' this objective
				// into their primary objective -- those activities need to be
				// included in the rollup set
				Vector acts = mSeqTree.getObjMap(objID);

				if (_Debug) {
					System.out.println("  ACTS == " + acts);
				}

				if (acts != null) {
					for (int j = 0; j < acts.size(); j++) {
						SeqActivity act = getActivity(((String) acts
								.elementAt(j)));

						if (_Debug) {
							System.out.println("  *+> " + j + " <+*  :: "
									+ act.getID());
						}

						// Only rollup at the parent of the affected activity
						act = act.getParent();

						if (act != null) {
							// Only add if the activity is selected
							if (act.getIsSelected()) {
								if (_Debug) {
									System.out.println("  ::--> Adding :: "
											+ act.getID());
								}

								rollupSet.put(act.getID(), new Integer(act
										.getDepth()));
							}
						}
					}
				}
			}
		}

		// Perform the deterministic rollup extension
		while (rollupSet.size() != 0) {
			if (_Debug) {
				System.out.println("  ::--> Rollup Set Size == "
						+ rollupSet.size());

				Enumeration theEnum = rollupSet.keys();
				while (theEnum.hasMoreElements()) {
					String key = (String) theEnum.nextElement();
					int thisDepth = ((Integer) rollupSet.get(key)).intValue();

					System.out
							.println("  ::-->  " + key + "  //  " + thisDepth);
				}
			}

			// Find the deepest activity
			SeqActivity deepest = null;
			int depth = -1;

			Enumeration theEnum = rollupSet.keys();
			while (theEnum.hasMoreElements()) {
				String key = (String) theEnum.nextElement();
				int thisDepth = ((Integer) rollupSet.get(key)).intValue();

				if (depth == -1) {
					depth = thisDepth;
					deepest = getActivity(key);
				} else if (thisDepth > depth) {
					depth = thisDepth;
					deepest = getActivity(key);
				}
			}

			if (deepest != null) {
				doOverallRollup(deepest, rollupSet);

				// If rollup was performed on the root, set the course's status
				if (deepest == mSeqTree.getRoot()) {

					String satisfied = "unknown";
					if (deepest.getObjStatus(false)) {
						satisfied = (deepest.getObjSatisfied(false)) ? "satisfied"
								: "notSatisfied";
					}

					String measure = "unknown";
					if (deepest.getObjMeasureStatus(false)) {
						measure = (new Double(deepest.getObjMeasure(false)))
								.toString();
					}

					String completed = "unknown";
					if (deepest.getProgressStatus(false)) {
						completed = (deepest.getAttemptCompleted(false)) ? "completed"
								: "incomplete";
					}

					//ADLSeqUtilities.setCourseStatus(mSeqTree.getCourseID(),
					//		mSeqTree.getLearnerID(), satisfied, measure,
					//		completed);
				}

			} else {
				if (_Debug) {
					System.out.println("  :: ERROR :: No activity found");
				}
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - invokeRollup");
		}
	}

	/**
	 * Applies the Overall Rollup Process to the target activity. <br>
	 * <b>Internal Sequencing Process</b><br>
	 * <br>
	 * 
	 * @param ioTarget
	 *            Identifies the activity where rollup is applied.
	 * 
	 * @param ioRollupSet
	 *            Identifies the set of activities remaining.
	 */
	private void doOverallRollup(SeqActivity ioTarget, Hashtable ioRollupSet) {

		// This method implements the loop of RB.1.5. The other rollup process
		// are encapsulated in the RollupRuleset object.

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - doOverallRollup");
			System.out.println("  ::-->  Target: " + ioTarget.getID());
		}

		// Attempt to get Rollup Rule information from the activity node
		ISeqRollupRuleset rollupRules = ioTarget.getRollupRules();

		if (rollupRules == null) {
			rollupRules = new SeqRollupRuleset();
		}

		// Apply the rollup processes to the activity
		rollupRules.evaluate(ioTarget);

		if (_Debug) {
			boolean objMeasureStatus = ioTarget.getObjMeasureStatus(false);
			double objMeasure = ioTarget.getObjMeasure(false);

			boolean objStatus = ioTarget.getObjStatus(false);
			boolean objSatisfied = ioTarget.getObjSatisfied(false);

			boolean proStatus = ioTarget.getProgressStatus(false);
			boolean proCompleted = ioTarget.getAttemptCompleted(false);

			System.out.println("  ::--> RESULTS");
			System.out.println("  :: OBJ Measure ::   " + objMeasureStatus
					+ "  //  " + objMeasure);
			System.out.println("  :: OBJ Status  ::   " + objStatus + "  //  "
					+ objSatisfied);
			System.out.println("  :: Progress    ::   " + proStatus + "  //  "
					+ proCompleted);
		}

		// Remove this activity from the rollup set
		ioRollupSet.remove(ioTarget.getID());

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - doOverallRollup");
		}
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Selection and Randomization Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Walk the activity tree for the first time preparing clusters by applying
	 * selection and randomization processes where appropriate.
	 */
	private void prepareClusters() {

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - prepareClusters");
		}

		SeqActivity walk = mSeqTree.getRoot();
		Vector lookAt = new Vector();

		if (walk != null) {
			while (walk != null) {
				// Only prepare clusters
				if (walk.hasChildren(true)) {
					if (!walk.getSelectionTiming().equals(
							SeqActivity.TIMING_NEVER)) {
						if (!walk.getSelection()) {
							doSelection(walk);
							walk.setSelection(true);
						}
					}

					if (!walk.getRandomTiming()
							.equals(SeqActivity.TIMING_NEVER)) {
						if (!walk.getRandomized()) {
							doRandomize(walk);
							walk.setRandomized(true);
						}
					}

					// Keep track of children we still need to look at
					if (walk.hasChildren(false)) {
						lookAt.add(walk);
					}
				}

				// Move to next activity
				walk = walk.getNextSibling(false);

				if (walk == null) {
					if (lookAt.size() != 0) {
						walk = (SeqActivity) lookAt.elementAt(0);
						walk = (SeqActivity) walk.getChildren(false).get(0);

						lookAt.remove(0);
					}
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ERROR :: NULL Activity Tree");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - prepareClusters");
		}
	}

	/**
	 * Prepare the children of a cluster to be considered for sequencing.
	 * 
	 * @param ioCluster
	 *            Cluster to be prepared.
	 */
	private void doSelection(SeqActivity ioCluster) {

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - doSelection");
			System.out.println("  ::-->  Target: " + ioCluster.getID());
		}

		// Make sure this is a cluster
		if (ioCluster.getChildren(true) != null) {

			Random gen = new Random();

			int count = ioCluster.getSelectCount();
			Vector all = (Vector) ioCluster.getChildren(true);

			if (_Debug) {
				System.out.println("  ::--> Cluster has '" + all.size() + "' "
						+ " children");
			}

			Vector children = null;
			Vector set = null;

			boolean ok = false;
			int rand = 0;
			int num = 0;
			int lookUp = 0;

			// First select the Select Count number of children
			if (count > 0) {
				// Check to see if the count exceeds the number of children
				if (count < all.size()) {

					if (_Debug) {
						System.out.println("  ::--> Selecting --> " + count);
					}

					// Select count activities from the set of children
					children = new Vector();
					set = new Vector();

					while (set.size() < count) {
						// Find an unselected child of the cluster
						ok = false;
						while (!ok) {
							rand = gen.nextInt();
							num = Math.abs(rand % all.size());

							lookUp = set.indexOf(new Integer(num));

							if (lookUp == -1) {
								set.add(new Integer(num));
								ok = true;

								if (_Debug) {
									System.out.println("  ::--> ADDED --> "
											+ num);
								}
							}
						}
					}

					// Create the selected child vector
					for (int i = 0; i < all.size(); i++) {
						lookUp = set.indexOf(new Integer(i));

						if (lookUp != -1) {
							children.add(all.elementAt(i));
						}
					}

					// Assign the selected set of children to the cluster
					ioCluster.setChildren(children, false);

				} else {
					if (_Debug) {
						System.out.println("  ::--> All Children Selected");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> No Children Selected");
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> Not A Cluster");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - doSelection");
		}
	}

	/**
	 * Reorder the children of a cluster to be considered for sequencing.
	 * 
	 * @param ioCluster
	 *            Cluster to be prepared.
	 */
	private void doRandomize(SeqActivity ioCluster) {

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - doRandomize");
			System.out.println("  ::-->  Target: " + ioCluster.getID());
		}

		// Make sure this is a cluster
		if (ioCluster.getChildren(true) != null) {

			Random gen = new Random();
			Vector all = (Vector) ioCluster.getChildren(false);

			if (_Debug) {
				System.out.println("  ::--> Cluster has '" + all.size() + "' "
						+ " children to randomize");
			}

			Vector set = null;

			boolean ok = false;
			int rand = 0;
			int num = 0;
			int lookUp = 0;

			// Reorder the 'selected' child set if neccessary
			if (ioCluster.getReorderChildren()) {
				Vector reorder = new Vector();
				set = new Vector();

				for (int i = 0; i < all.size(); i++) {
					// Pick an unselected child
					ok = false;
					while (!ok) {
						rand = gen.nextInt();
						num = Math.abs(rand % all.size());

						lookUp = set.indexOf(new Integer(num));

						if (lookUp == -1) {
							set.add(new Integer(num));
							reorder.add(all.get(num));

							if (_Debug) {
								System.out.println("  ::--> PLACED --> " + num);
							}

							ok = true;
						}
					}
				}

				// Assign the current set of active children to this cluster
				ioCluster.setChildren(reorder, false);

			} else {
				if (_Debug) {
					System.out.println("  ::--> Don't Reorder");
				}
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> Not A Cluster");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - doRandomize");
		}
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Sequencing Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * This method processes the indicated sequencing request on the activity
	 * tree, attempting to identify an activity for delivery. <br>
	 * <b>Internal Sequencing Process</b><br>
	 * <br>
	 * 
	 * @param iRequest
	 *            The sequencing request to be processed.
	 * 
	 * @return An activity identified for delivery (<code>String</code>) or
	 *         <code>null</code> if the sequencing request did not identify an
	 *         activity for delivery.
	 */
	private String doSequencingRequest(String iRequest) {

		// This method implements the Sequencing Request Process (SB.2.12)

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "doSequencingRequest");
			System.out.println("  ::-->  " + iRequest);
		}

		String delReq = null;

		// Clear global state
		mEndSession = false;

		// All sequencing requests are processed from the First Candidate
		SeqActivity from = mSeqTree.getFirstCandidate();

		if (iRequest.equals(ADLSequencer.SEQ_START)) {
			// This block implements the Start Sequencing Request Process (SB.2.

			if (_Debug) {
				System.out.println("  ::--> Process 'Start' request.");
			}

			// Make sure this request will begin a new session
			if (from == null) {
				// Begin traversing the activity tree from the root
				Walk walk = new Walk();
				walk.at = mSeqTree.getRoot();

				boolean success = processFlow(ADLSequencer.FLOW_FORWARD, true,
						walk, false);

				if (success) {
					// Delivery request is where flow stopped.
					delReq = walk.at.getID();
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> Session already begun");
				}
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_RESUMEALL)) {
			// This block implements the Resume All Sequencing Request Process
			// (SB.2.6)

			if (_Debug) {
				System.out.println("  ::--> Process 'Resume All' request.");
			}

			// Make sure this request will begin a new session
			if (from == null) {
				SeqActivity resume = mSeqTree.getSuspendAll();

				if (resume != null) {
					delReq = resume.getID();
				} else {
					if (_Debug) {
						System.out.println("  ::--> No suspended activity");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> Session already begun");
				}
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_CONTINUE)) {
			// This block implements the Continue Sequencing Request Process
			// (SB.2.7)

			if (_Debug) {
				System.out.println("  ::--> Process 'Continue' request.");
			}

			// Make sure the session has already started
			if (from != null) {
				// Confirm 'flow' is enabled
				SeqActivity parent = from.getParent();
				if (parent == null || parent.getControlModeFlow()) {

					// Begin traversing the activity tree from the root
					Walk walk = new Walk();
					walk.at = from;

					boolean success = processFlow(ADLSequencer.FLOW_FORWARD,
							false, walk, false);

					if (success) {
						// Delivery request is where flow stopped.
						delReq = walk.at.getID();
					} else {
						if (_Debug) {
							System.out
									.println("  :+: CONTINUE FAILED :+:  --> "
											+ walk.at.getID());
						}
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> Session hasn't begun");
				}
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_EXIT)) {
			// This block implements the Exit Sequencing Request Process
			// (SB.2.11)

			if (_Debug) {
				System.out.println("  ::--> Process 'Exit' request.");
			}

			// Make sure the session has already started
			if (from != null) {
				if (!from.getIsActive()) {
					ISeqActivity parent = from.getParent();

					if (parent == null) {
						// The sequencing session is over -- set global state
						mEndSession = true;
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> Activity is still active");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> Session hasn't begun");
				}
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_PREVIOUS)) {
			// This block implements the Previous Sequencing Request Process
			// (SB.2.8)

			if (_Debug) {
				System.out.println("  ::--> Process 'Previous' request.");
			}

			// Make sure the session has already started
			if (from != null) {
				// Confirm 'flow' is enabled
				SeqActivity parent = from.getParent();
				if (parent == null || parent.getControlModeFlow()) {
					// Begin traversing the activity tree from the root
					Walk walk = new Walk();
					walk.at = from;

					boolean success = processFlow(ADLSequencer.FLOW_BACKWARD,
							false, walk, false);

					if (success) {
						// Delivery request is where flow stopped.
						delReq = walk.at.getID();
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> Session hasn't begun");
				}
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_RETRY)) {
			// This block implements the Retry Sequencing Request Process
			// (SB.2.10)

			if (_Debug) {
				System.out.println("  ::--> Process 'Retry' request.");
			}

			// Make sure the session has already started
			if (from != null) {
				if (mExitAll
						|| (!(from.getIsActive() || from.getIsSuspended()))) {
					if (from.getChildren(false) != null) {
						Walk walk = new Walk();
						walk.at = from;

						// Set 'Retry' flag
						setRetry(true);

						boolean success = processFlow(
								ADLSequencer.FLOW_FORWARD, true, walk, false);

						// Reset 'Retry' flag
						setRetry(false);

						if (success) {
							delReq = walk.at.getID();
						}
					} else {
						delReq = from.getID();
					}
				} else {
					if (_Debug) {
						System.out
								.println("  ::--> Activity is active or suspended");
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> Session hasn't begun");
				}
			}
		} else {
			// This block implements the Choice Sequencing Request Process
			// (SB.2)

			if (_Debug) {
				System.out.println("  ::--> Process 'Choice' request.");
			}

			// The sequencing request identifies the target activity
			SeqActivity target = getActivity(iRequest);

			if (target != null) {
				boolean process = true;
				SeqActivity parent = target.getParent();

				// Check if the activity should be considered.
				if (!target.getIsSelected()) {
					// Exception SB.2.9-2
					process = false;

					if (_Debug) {
						System.out.println("  ::--> Activity not in parent's "
								+ "set of avaliable children");
					}
				}

				if (process) {
					SeqActivity walk = target.getParent();

					// Walk up the tree evaluating 'Hide from Choice' rules.
					while (walk != null) {
						// Attempt to get rule information from the activity
						ISeqRuleset hideRules = walk.getPreSeqRules();

						String result = null;

						if (hideRules != null) {
							result = hideRules.evaluate(
									SeqRuleset.RULE_TYPE_HIDDEN, walk, false);
						}

						// If the rule evaluation did not return null, the
						// activity
						// must be hidden.
						if (result != null) {
							// Exception SB.2.9-3
							walk = null;
							process = false;

							if (_Debug) {
								System.out.println("  ::--> Activity hidden");
							}
						} else {
							walk = walk.getParent();
						}
					}
				}

				// Confirm the control mode is valid
				if (process) {
					if (parent != null) {
						if (!parent.getControlModeChoice()) {
							// Exception SB.2.9-4
							process = false;

							if (_Debug) {
								System.out
										.println("  ::--> Invalid control mode");
							}
						}
					}
				}

				SeqActivity common = mSeqTree.getRoot();

				if (process) {
					if (from != null) {
						common = findCommonAncestor(from, target);

						if (common == null) {
							process = false;

							if (_Debug) {
								System.out
										.println("  ::-->  ERROR : Invalid ancestor");
							}
						}
					} else {
						// If the sequencing session has not begun, start at the
						// root
						from = common;
					}

					if (_Debug) {
						System.out.println("  :: CHOICE PROCESS ::");

						if (from != null) {
							System.out.println("  ::  F : " + from.getID());
						} else {
							System.out.println("  ::  F : NULL");
						}

						if (target != null) {
							System.out.println("  ::  T : " + target.getID());
						} else {
							System.out.println("  ::  T : NULL");
						}

						if (common != null) {
							System.out.println("  ::  C : " + common.getID());
						} else {
							System.out.println("  ::  C : NULL");
						}
					}

					// Choice Case #1 -- The current activity was selected
					if (from == target) {

						if (_Debug) {
							System.out.println("  ::-->  Choice Case #1");
						}

						// Nothing more to do...

					}

					// Choice Case #2 -- The current activity and target are in
					// the
					// same cluster
					else if (from.getParent() == target.getParent()) {

						if (_Debug) {
							System.out.println("  ::-->  Choice Case #2");
						}

						int dir = ADLSequencer.FLOW_FORWARD;

						if (target.getActiveOrder() < from.getActiveOrder()) {
							dir = ADLSequencer.FLOW_BACKWARD;
						}

						SeqActivity walk = from;

						// Make sure no control modes or rules prevent the
						// traversal
						while (walk != target && process) {
							process = evaluateChoiceTraversal(dir, walk);

							if (dir == ADLSequencer.FLOW_FORWARD) {
								walk = walk.getNextSibling(false);
							} else {
								walk = walk.getPrevSibling(false);
							}
						}
					}

					// Choice Case #3 -- Path to the target is forward in the
					// tree
					else if (from == common) {

						if (_Debug) {
							System.out.println("  ::-->  Choice Case #3");
						}

						SeqActivity walk = target.getParent();

						while (walk != from && process) {
							process = evaluateChoiceTraversal(
									ADLSequencer.FLOW_FORWARD, walk);

							// Test prevent Activation
							if (process) {
								if (!walk.getIsActive()
										&& walk.getPreventActivation()) {
									// Exception 2.9-6
									process = false;

									continue;
								}
							}

							walk = walk.getParent();
						}

						// Evaluate at the common ancestor
						if (process) {
							process = evaluateChoiceTraversal(
									ADLSequencer.FLOW_FORWARD, walk);
						}
					}

					// Choice Case #4 -- Path to target is backward in the tree
					else if (target == common) {

						if (_Debug) {
							System.out.println("  ::-->  Choice Case #4");
						}

						// Don't need to test choiceExit on the current activity
						// because the navigation request validated.
						SeqActivity walk = from.getParent();

						while (walk != target && process) {
							// Need to make sure that none of the 'exiting'
							// activities
							// prevents us from reaching the common ancestor.
							process = walk.getControlModeChoiceExit();

							walk = walk.getParent();
						}
					}

					// Choice Case #5 -- Target is a descendent of the ancestor
					else {

						if (_Debug) {
							System.out.println("  ::-->  Choice Case #5");
						}

						SeqActivity con = null;
						SeqActivity walk = from.getParent();

						// Walk up the tree to the common ancestor
						while (walk != common && process) {
							process = walk.getControlModeChoiceExit();

							if (process && con == null) {
								if (walk.getConstrainChoice()) {
									con = walk;
								}
							}

							walk = walk.getParent();
						}

						// Evaluate constrained choice set
						if (process && con != null) {
							Walk walkCon = new Walk();
							walkCon.at = con;

							if (target.getCount() > con.getCount()) {
								processFlow(FLOW_FORWARD, false, walkCon, true);
							} else {
								processFlow(FLOW_BACKWARD, false, walkCon, true);
							}

							if (_Debug) {
								System.out
										.println("  ::-->  Constrained Choice Eval");
								System.out.println("  ::-->  Stopped at --> "
										+ walkCon.at.getID());
							}

							if (target.getParent() != walkCon.at
									&& target != walkCon.at) {
								// Exception SB.2.9-8
								process = false;
							}
						}

						// Walk down the tree to the target
						walk = target.getParent();

						while (walk != common && process) {
							process = evaluateChoiceTraversal(
									ADLSequencer.FLOW_FORWARD, walk);

							// Test prevent Activation
							if (process) {
								if (!walk.getIsActive()
										&& walk.getPreventActivation()) {
									// Exception 2.9-6
									process = false;

									continue;
								}
							}

							walk = walk.getParent();
						}

						// Evaluate the common ancestor
						if (process) {
							process = evaluateChoiceTraversal(
									ADLSequencer.FLOW_FORWARD, walk);
						}
					}

					// Did we reach the target successfully?
					if (process) {

						// Is the target a cluster
						if (target.getChildren(false) != null) {
							Walk walk = new Walk();
							walk.at = target;

							boolean success = processFlow(
									ADLSequencer.FLOW_FORWARD, true, walk,
									false);

							if (success) {
								delReq = walk.at.getID();
							} else {
								if (_Debug) {
									System.out
											.println("  ::--> Failed to find leaf");
									System.out
											.println("  ::--> Moving Current Activity");

									if (common == null) {
										System.out.println("  ::--> NULL");
									} else {
										System.out.println("  ::--> "
												+ common.getID());
									}
								}

								if (mSeqTree.getCurrentActivity() != null
										&& common != null) {
									terminateDescendentAttempts(common);
									endAttempt(common, false);

									// Move the current activity
									mSeqTree.setCurrentActivity(target);
									mSeqTree.setFirstCandidate(target);
								}
							}
						} else {
							delReq = target.getID();
						}
					}
				}
			} else {
				// Exception SB.2.9-1
				if (_Debug) {
					System.out
							.println("  ::-->  Target does not exist in the tree");
				}
			}
		}

		if (_Debug) {
			System.out.println("  ::-->  " + delReq);
			System.out.println("  ::-->  " + (mEndSession || mExitCourse));
			System.out.println("  :: ADLSequencer --> END   - "
					+ "doSequencingRequest");

		}

		return delReq;
	}

	/**
	 * This method finds a common parent to two activities in the activity tree.
	 * 
	 * @param iFrom
	 *            The starting activity.
	 * 
	 * @param iTo
	 *            The destination activity.
	 * 
	 * @return The common ancestor of both activities (<code>SeqActivity</code>)
	 *         or <code>null</code> if the process failed.
	 */
	private SeqActivity findCommonAncestor(SeqActivity iFrom, SeqActivity iTo) {

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> BEGIN - findCommonAncestor");
		}

		SeqActivity ancestor = null;
		boolean done = false;

		SeqActivity stepFrom = null;

		// If either activity is 'null', no common parent
		if (iFrom == null || iTo == null) {
			done = true;
		} else {
			// Get the starting parents -- only look at clusters
			// This algorithm uses the exising 'selected' children.
			if (!iFrom.hasChildren(false)) {
				stepFrom = iFrom.getParent();
			} else {
				stepFrom = iFrom;
			}

			if (!iTo.hasChildren(false)) {
				iTo = iTo.getParent();
			}
		}

		while (!done) {
			// Test if the 'to' activity is a decendent of 'from' parent
			boolean success = isDescendent(stepFrom, iTo);

			// If we found the target activity, we are done
			if (success) {
				ancestor = stepFrom;
				done = true;

				continue;
			}

			// If this isn't the common parent, move up the tree
			if (!done) {
				stepFrom = stepFrom.getParent();
			}
		}

		if (_Debug) {
			if (ancestor != null) {
				System.out.println("  ::-->  " + ancestor.getID());
			} else {
				System.out.println("  ::-->  NULL");
			}

			System.out
					.println("  :: ADLSequencer --> END   - findCommonAncestor");
		}

		return ancestor;
	}

	/**
	 * This method determines if an activity is a descendent of another
	 * activity.
	 * 
	 * @param iRoot
	 *            Root of the subtree being looked at.
	 * 
	 * @param iTarget
	 *            Target activity being tested.
	 * 
	 * @return Returns <code>true</code> if the target is a descendent of the
	 *         root, otherwise <code>false</code>.
	 */
	private boolean isDescendent(SeqActivity iRoot, SeqActivity iTarget) {

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - isDescendent");

			if (iRoot != null) {
				System.out.println("  ::-->  Root --> " + iRoot.getID());
			} else {
				System.out.println("  ::-->  Root --> NULL");
			}

			if (iTarget != null) {
				System.out.println("  ::-->  Target  --> " + iTarget.getID());
			} else {
				System.out.println("  ::-->  Target  --> NULL");
			}
		}

		boolean found = false;

		if (iRoot == null) {
			if (_Debug) {
				System.out.println("  ::-->  ERROR : NULL Root");
			}
		} else if (iRoot == mSeqTree.getRoot()) {
			// All activities are descendents of the root
			found = true;
		} else if (iRoot != null && iTarget != null) {
			while (iTarget != null && !found) {
				if (iTarget == iRoot) {
					found = true;
				}

				iTarget = iTarget.getParent();
			}
		}

		if (_Debug) {
			System.out.println("  ::-->  " + found);
			System.out.println("  :: ADLSequencer --> END   - isDescendent");
		}

		return found;
	}

	/**
	 * Traverses the activity tree as defined by the Flow Tree Traversal
	 * Subprocess.
	 * 
	 * @param iDirection
	 *            Indicates the direction the tree is to be traversed.
	 * 
	 * @param iPrevDirection
	 *            Indicates the previous traversal direction.
	 * 
	 * @param iEnter
	 *            Indicates if the children of the acivity should b considered.
	 * 
	 * @param iFrom
	 *            Indicates where the traversal should start at.
	 * 
	 * @param iControl
	 *            Indicates if control modes should be enforced during this
	 *            traversal.
	 * 
	 * @return Indicates the 'next' activity in the traversal direction, or
	 *         <code>null</code> if no 'next' activity can be identified.
	 */
	private Walk walkTree(int iDirection, int iPrevDirection, boolean iEnter,
			SeqActivity iFrom, boolean iControl) {

		// This method implements Flow Subprocess SB.2.1

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - " + "walkTree");
			System.out.println("  Dir  ::-->  "
					+ (iDirection == ADLSequencer.FLOW_BACKWARD ? "Backward"
							: "Forward"));
			if (iPrevDirection == ADLSequencer.FLOW_NONE) {
				System.out.println("  Prev ::-->  None");
			} else {
				System.out
						.println("  Prev ::-->  "
								+ (iPrevDirection == ADLSequencer.FLOW_BACKWARD ? "Backward"
										: "Forward"));
			}

			System.out
					.println("  ::-->  " + (iEnter ? "Enter" : "Don't Enter"));
			System.out
					.println("  ::--> Control?  " + (iControl ? "Yes" : "No"));

			if (iFrom != null) {
				System.out.println("  ::-->  " + iFrom.getID());
			} else {
				System.out.println("  ::--> NULL");
			}
		}

		SeqActivity next = null;
		SeqActivity parent = null;

		int direction = iDirection;
		boolean reversed = false;

		boolean done = false;
		boolean endSession = false;

		if (iFrom == null) {

			if (_Debug) {
				System.out.println("  ::--> Walked off the Activity Tree");
			}

			// The sequencing session is over
			endSession = true;
			done = true;
		} else {
			parent = iFrom.getParent();
		}

		// Test if we have skipped all of the children in a 'forward-only'
		// cluster traversing backward
		if (!done && parent != null) {
			if (iPrevDirection == ADLSequencer.FLOW_BACKWARD) {
				if (iFrom.getNextSibling(false) == null) {
					// Switch traversal direction
					direction = ADLSequencer.FLOW_BACKWARD;

					// Move our starting point
					iFrom = (SeqActivity) (parent.getChildren(false).get(0));

					reversed = true;

					if (_Debug) {
						System.out.println("  ::  REVERSING DIRECTION ::   "
								+ iFrom.getID());
					}
				}
			}
		}

		if (!done && direction == ADLSequencer.FLOW_FORWARD) {
			if (iFrom.getID().equals(mSeqTree.getLastLeaf())) {

				// We are at the last leaf of the tree, the sequencing
				// session is over
				done = true;
				endSession = true;

				if (_Debug) {
					System.out.println("  ::-->  Last Leaf");
				}
			}

			if (!done) {
				// Is the activity a leaf or a cluster that should not be
				// entered
				if (!iFrom.hasChildren(false) || !iEnter) {

					// String result = null;
					//
					// if ( iControl )
					// {
					//
					// if ( _Debug )
					// {
					// System.out.println(" ::--> TESTING FORWARD BLOCK");
					// }
					//
					// // Attempt to get rule information from the activity node
					// SeqRuleset stopTrav = iFrom.getPreSeqRules();
					//
					// if ( stopTrav != null )
					// {
					// result =
					// stopTrav.evaluate(SeqRuleset.RULE_TYPE_FORWARDBLOCK,
					// iFrom);
					// }
					// }
					//
					// // If the rule evaluation did not returns null, move to
					// // the activity's sibling
					// if ( result == null )
					// {
					next = iFrom.getNextSibling(false);

					if (next == null) {
						if (_Debug) {
							System.out
									.println("  ::-->  FORWARD RECURSION  <--::");
						}

						Walk walk = walkTree(direction, ADLSequencer.FLOW_NONE,
								false, parent, iControl);

						next = walk.at;
						endSession = walk.endSession;
					}
				}
				// else
				// {
				// if ( _Debug )
				// {
				// System.out.println(" ::--> BLOCKED <--::");
				// }
				//
				// done = true;
				// }
				// }

				// Enter the Cluster
				else {
					// Return the first child activity
					next = (SeqActivity) (iFrom.getChildren(false).get(0));
				}
			}
		} else if (!done && direction == ADLSequencer.FLOW_BACKWARD) {
			// Can't walk off the root of the tree
			if (parent != null) {
				// Is the activity a leaf or a cluster that should not be
				// entered
				if (!iFrom.hasChildren(false) || !iEnter) {
					// Make sure we can move backward
					if (iControl && !reversed) {

						if (parent.getControlForwardOnly()) {
							if (_Debug) {
								System.out.println("  ::--> Forward Only "
										+ "Control Mode "
										+ "violation at -->  " + iFrom.getID());

								System.out
										.println("  :: ADLSequencer --> END   - "
												+ "walkTree");
							}

							done = true;
						}
					}

					if (!done) {
						next = iFrom.getPrevSibling(false);

						if (next == null) {
							if (_Debug) {
								System.out
										.println("  ::-->  BACKWARD RECURSION  "
												+ "<--::");
							}

							Walk walk = walkTree(direction,
									ADLSequencer.FLOW_NONE, false, parent,
									iControl);

							next = walk.at;
							endSession = walk.endSession;
						}
					}
				}

				// Enter the cluster backward
				else {
					if (iFrom.getControlForwardOnly()) {
						// Return the first child activity
						next = (SeqActivity) (iFrom.getChildren(false).get(0));

						// And switch direction
						direction = ADLSequencer.FLOW_FORWARD;
					} else {
						int size = iFrom.getChildren(false).size();

						// Return the last child activity
						next = (SeqActivity) (iFrom.getChildren(false)
								.get(size - 1));
					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> ERROR : Walked off the root");
				}
			}
		} else if (!done) {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Invalid direction");
			}
		}

		if (_Debug) {
			if (next != null) {
				System.out.println("  ::-->  " + next.getID());
			} else {
				System.out.println("  ::-->  NULL");
			}

			System.out.println("  ::-->  End Session?  "
					+ ((endSession) ? "YES" : "NO"));

			System.out.println("  ::--> MOVING ---> "
					+ ((direction == ADLSequencer.FLOW_FORWARD) ? "Forward"
							: "Backward"));

			System.out.println("  :: ADLSequencer --> END   - " + "walkTree");
		}

		Walk walk = new Walk();
		walk.at = next;
		walk.direction = direction;

		walk.endSession = endSession;

		return walk;
	}

	/**
	 * Traverses the activity tree as defined by the Flow Activity Traveral
	 * Subprocess.
	 * 
	 * @param iDirection
	 *            Indicates the direction the tree is to be traversed.
	 * 
	 * @param iPrevDirection
	 *            Indicates the previous traversal direction.
	 * 
	 * @param ioFrom
	 *            Indicates where the traversal should start at, an upon
	 *            completion, indicates where the traversal stopped.
	 * 
	 * @return Indicates if the traversal resulted in a deliveriable activity.
	 */
	private boolean walkActivity(int iDirection, int iPrevDirection, Walk ioFrom) {

		// This method implements Flow Subprocess SB.2.3

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "walkActivity");
			System.out.println("  Dir  ::-->  "
					+ (iDirection == ADLSequencer.FLOW_BACKWARD ? "Backward"
							: "Forward"));
			if (iPrevDirection == ADLSequencer.FLOW_NONE) {
				System.out.println("  Prev ::-->  None");
			} else {
				System.out
						.println("  Prev ::-->  "
								+ (iPrevDirection == ADLSequencer.FLOW_BACKWARD ? "Backward"
										: "Forward"));
			}

			if (ioFrom.at != null) {
				System.out.println("  ::-->  " + ioFrom.at.getID());
			} else {
				System.out.println("  ::--> ERROR : NULL starting point");
			}
		}

		boolean deliver = true;

		SeqActivity parent = ioFrom.at.getParent();

		if (parent != null) {
			// Confirm that 'flow' is enabled for the cluster
			if (!parent.getControlModeFlow()) {
				if (_Debug) {
					System.out.println("  ::--> Control Mode violated");
				}

				deliver = false;
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Cannot have null parent");
			}

			deliver = false;
		}

		if (deliver) {
			// Check if the activity should be 'skipped'.
			String result = null;

			ISeqRuleset skippedRules = ioFrom.at.getPreSeqRules();

			if (skippedRules != null) {
				result = skippedRules.evaluate(SeqRuleset.RULE_TYPE_SKIPPED,
						ioFrom.at, mRetry);
			}

			// If the rule evaluation did not return null, the activity is
			// skipped
			if (result != null) {
				Walk walk = walkTree(iDirection, iPrevDirection, false,
						ioFrom.at, true);

				if (walk.at == null) {
					deliver = false;
				} else {
					if (_Debug) {
						System.out.println("  ::-->  RECURSION  <--::");
					}

					ioFrom.at = walk.at;

					// Test if we've switched directions...
					if (iPrevDirection == ADLSequencer.FLOW_BACKWARD
							&& walk.direction == ADLSequencer.FLOW_BACKWARD) {
						return walkActivity(ADLSequencer.FLOW_BACKWARD,
								ADLSequencer.FLOW_NONE, ioFrom);
					} else {
						return walkActivity(iDirection, iPrevDirection, ioFrom);
					}
				}
			} else {
				// The activity was not skipped, make sure it is enabled
				if (!checkActivity(ioFrom.at)) {
					// Make sure the activity being considered is a leaf
					if (ioFrom.at.hasChildren(false)) {
						Walk walk = walkTree(iDirection,
								ADLSequencer.FLOW_NONE, true, ioFrom.at, true);

						if (walk.at != null) {

							ioFrom.at = walk.at;

							if (iDirection == ADLSequencer.FLOW_BACKWARD
									&& walk.direction == ADLSequencer.FLOW_FORWARD) {
								if (_Debug) {
									System.out.println("  ::--> REVERSING");
								}

								deliver = walkActivity(
										ADLSequencer.FLOW_FORWARD,
										ADLSequencer.FLOW_BACKWARD, ioFrom);
							} else {
								deliver = walkActivity(iDirection,
										ADLSequencer.FLOW_NONE, ioFrom);
							}
						} else {
							deliver = false;
						}
					} else {
						if (_Debug) {
							System.out.println("  ::--> Found a leaf");
						}
					}
				} else {
					deliver = false;
				}
			}
		}

		if (_Debug) {
			System.out.println("  ::-->  " + deliver);

			if (ioFrom.at != null) {
				System.out.println("  ::-->  " + ioFrom.at.getID());
			} else {
				System.out.println("  ::-->  NULL");
			}

			System.out.println("  :: ADLSequencer --> END   - "
					+ "walkActivity");
		}

		return deliver;
	}

	/**
	 * Traverses the activity tree as defined by the Flow Subprocess.
	 * 
	 * @param iDirection
	 *            Indicates the direction the tree is to be traversed.
	 * 
	 * @param iEnter
	 *            Indicates if the children of the starting activity should be
	 *            considered during the traversal.
	 * 
	 * @param ioFrom
	 *            Indicates where the traversal should start at, and upon
	 *            completion, indicates where the traversal stopped.
	 * 
	 * @param iConChoice
	 *            Indicates if only the 'next' activity should be reached.
	 * 
	 * @return Indicates if the traversal was successful.
	 */
	private boolean processFlow(int iDirection, boolean iEnter, Walk ioFrom,
			boolean iConChoice) {

		// This method implements Flow Subprocess SB.2.3

		if (_Debug) {
			System.out
					.println("  :: ADLSequencer --> BEGIN - " + "processFlow");
			System.out.println("  ::-->  " + iDirection);
			System.out.println("  ::-->  " + iConChoice);
			System.out.println("  ::--> Con Choice?  "
					+ (iConChoice ? "Yes" : "No"));

			if (ioFrom.at != null) {
				System.out.println("  ::-->  " + ioFrom.at.getID());
			} else {
				System.out.println("  ::-->  ERROR : NULL starting point");
			}
		}

		boolean success = true;

		SeqActivity candidate = ioFrom.at;

		// Make sure we have somewhere to start from
		if (candidate != null) {

			Walk walk = walkTree(iDirection, ADLSequencer.FLOW_NONE, iEnter,
					candidate, !iConChoice);

			if (!iConChoice && walk.at != null) {
				ioFrom.at = walk.at;
				success = walkActivity(iDirection, ADLSequencer.FLOW_NONE,
						ioFrom);
			} else {
				if (iConChoice) {

					ioFrom.at = walk.at;

					if (_Debug) {
						System.out.println("  ::--> Constrained Choice test");
					}
				} else {

					if (_Debug) {
						System.out.println("  ::--> No 'next' activity");
					}
				}

				success = false;
			}

			// Check to see if the sequencing session is ending due to
			// walking off the activity tree
			if (walk.at == null && walk.endSession) {

				if (_Debug) {
					System.out.println("  ::--> ENDING SESSION");
				}

				// End the attempt on the root of the activity tree
				terminateDescendentAttempts(mSeqTree.getRoot());

				// The sequencing session is over -- set global state
				mEndSession = true;

				success = false;
			}
		} else {
			success = false;
		}

		if (_Debug) {
			System.out.println("  ::-->  " + success);

			if (ioFrom.at != null) {
				System.out.println("  ::-->  " + ioFrom.at.getID());
			} else {
				System.out.println("  ::-->  NULL");
			}

			System.out
					.println("  :: ADLSequencer --> END   - " + "processFlow");
		}

		return success;

	}

	/**
	 * Evaluates if the identified activity can be skipped during a 'Choice'
	 * sequencing request.
	 * 
	 * @param iDirection
	 *            Indicates the direction the tree is to be traversed.
	 * 
	 * @param iAt
	 *            Indicates the activity being considered.
	 * 
	 * @return Indicates if the activity can be reached.
	 */
	private boolean evaluateChoiceTraversal(int iDirection, SeqActivity iAt) {

		// This method implements Choice Activity Traversal Subprocess SB.2.4

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "evaluateChoiceTraversal");
			System.out.println("  ::-->  " + iDirection);

			if (iAt != null) {
				System.out.println("  ::-->  " + iAt.getID());
			} else {
				System.out.println("  ::-->  ERROR : NULL starting point");
			}
		}

		boolean success = true;

		// Make sure we have somewhere to start from
		if (iAt != null) {
			if (true) {
				if (iDirection == ADLSequencer.FLOW_FORWARD) {
					// Attempt to get rule information from the activity node
					ISeqRuleset stopTrav = iAt.getPreSeqRules();
					String result = null;

					if (stopTrav != null) {
						result = stopTrav.evaluate(
								SeqRuleset.RULE_TYPE_FORWARDBLOCK, iAt, false);
					}

					// If the rule evaluation does not return null, can't move
					// to the
					// activity's sibling
					if (result != null) {
						success = false;
					}
				} else if (iDirection == ADLSequencer.FLOW_BACKWARD) {
					SeqActivity parent = iAt.getParent();

					if (parent != null) {
						success = !parent.getControlForwardOnly();
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> ERROR : Invalid direction");
					}

					success = false;
				}
			} else {
				success = false;
			}
		} else {
			success = false;
		}

		if (_Debug) {
			System.out.println("  ::-->  " + success);
			System.out.println("  :: ADLSequencer --> END   - "
					+ "evaluateChoiceTraversal");
		}

		return success;

	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Delivery Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * This method validates a delivery request by waling the activity tree from
	 * the root to the identifed activity. At each step, disabled sequencing
	 * rules and limit conditions are evaluated. If the identified activity can
	 * be delivered, an <code>ADLLaunch</code> object is initialized with the
	 * required information for the RTE. <br>
	 * <b>Internal Sequencing Process</b><br>
	 * <br>
	 * 
	 * @param iTarget
	 *            The activity identified for delivery.
	 * 
	 * @param iTentative
	 *            Should the launch (<code>ADLLaunch</code>) information be
	 *            provided?
	 * 
	 * @param oLaunch
	 *            The launch (<code>ADLLaunch</code>) information to be
	 *            provided to the RTE.
	 * 
	 * @return <code>true</code> if the identifed activity is valid for delive
	 *         otherwise <code>false</code>.
	 */
	private boolean doDeliveryRequest(String iTarget, boolean iTentative,
			ADLLaunch oLaunch) {
		// This method implements DB.1. Also, if the delivery request is not
		// tentative, it invokes the Content Delivery Environment Process.

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "doDeliveryRequest");
			System.out.println("  ::-->         " + iTarget);
			System.out.println("  ::-->  REAL?  "
					+ ((iTentative) ? "NO" : "YES"));
		}

		boolean deliveryOK = true;

		// Make sure the identified activity exists in the tree.
		SeqActivity act = getActivity(iTarget);

		if (act == null) {

			// If there is no activity identified for delivery, there is nothing
			// to delivery -- indentify non-Sequenced content
			deliveryOK = false;

			if (_Debug) {
				System.out.println("  ::--> No Delivery Request");
			}

			if (!iTentative) {
				if (mExitCourse) {
					oLaunch.mSeqNonContent = ADLLaunch.LAUNCH_COURSECOMPLETE;
				} else {
					if (mEndSession) {
						oLaunch.mSeqNonContent = ADLLaunch.LAUNCH_EXITSESSION;
					} else {
						oLaunch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
					}
				}
			}
		}

		// Confirm the target activity is a leaf
		if (deliveryOK && act.hasChildren(false)) {
			deliveryOK = false;

			oLaunch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
			oLaunch.mEndSession = mEndSession;

			if (_Debug) {
				System.out.println("  ::-->  Activity is not a leaf");
			}
		} else if (deliveryOK) {
			boolean ok = true;

			// Walk the path from the target activity to the root, checking each
			// activity.
			while (act != null && ok) {

				if (_Debug) {
					System.out
							.println("  ::-->  Validating --> " + act.getID());
				}

				ok = !checkActivity(act);

				if (ok) {
					act = act.getParent();
				}
			}

			if (!ok) {

				if (_Debug) {
					System.out
							.println("  ::-->  Some activity did not validate");
				}

				deliveryOK = false;

				oLaunch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
			}
		}

		// If the delivery request not a tentative request, prepare for deliver
		if (!iTentative) {
			// Did the request validate
			if (deliveryOK) {
				contentDelivery(iTarget, oLaunch);
				validateRequests();
			} else {
				oLaunch.mEndSession = mEndSession || mExitCourse;

				if (!oLaunch.mEndSession) {
					validateRequests();
					oLaunch.mNavState = mSeqTree.getValidRequests();
				}
			}
		}

		if (_Debug) {
			System.out.println("  ::-->  " + deliveryOK);
			System.out.println("  :: ADLSequencer --> END   - "
					+ "doDeliveryRequest");
		}

		return deliveryOK;
	}

	/**
	 * The method is the exit point of the overall sequencing loop. When it is
	 * invoked, it is assumed that the activity identified for delivery has been
	 * validated by the Delivery Request Process. This method performs necessery
	 * activity tree management and returns sufficient information to the RTE
	 * launching the resource(s) associated with the identified activity.
	 * 
	 * @param iTarget
	 *            The activity identified for delivery.
	 * 
	 * @param oLaunch
	 *            An 'out' parameter that provides information to the RTE for
	 *            launching the resources associated with the activity.
	 */
	private void contentDelivery(String iTarget, ADLLaunch oLaunch) {

		// This method implements the Content Delivery Environment Process
		// (DB.2)
		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "contentDelivery");
			System.out.println("  ::-->  " + iTarget);
		}

		SeqActivity target = getActivity(iTarget);
		boolean done = false;

		if (target == null) {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Invalid target");
			}

			oLaunch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
			oLaunch.mEndSession = mEndSession;

			done = true;
		}

		SeqActivity cur = mSeqTree.getFirstCandidate();

		if (cur != null && !done) {
			if (cur.getIsActive()) {
				if (_Debug) {
					System.out.println("  ::--> ERROR : "
							+ "Current activity still active.");
				}

				oLaunch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
				oLaunch.mEndSession = mEndSession;

				done = true;
			}
		}

		if (!done) {

			// Clear any 'suspended' activity
			clearSuspendedActivity(target);

			// End any active attempts
			terminateDescendentAttempts(target);

			// Begin all required new attempts
			Vector begin = new Vector();
			SeqActivity walk = target;

			while (walk != null) {
				begin.add(walk);

				walk = walk.getParent();
			}

			if (begin.size() > 0) {

				for (int i = begin.size() - 1; i >= 0; i--) {

					walk = (SeqActivity) begin.elementAt(i);

					if (_Debug) {
						System.out.println("  ::--> BEGIN >> " + walk.getID());
					}

					if (!walk.getIsActive()) {
						if (walk.getIsTracked()) {
							if (walk.getIsSuspended()) {
								walk.setIsSuspended(false);
							} else {
								// Initialize tracking information for the new
								// attempt
								walk.incrementAttempt();
							}
						}

						walk.setIsActive(true);

					}
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> ERROR : Empty begin vector");
				}
			}

			// Set the tree in the appropriate state
			mSeqTree.setCurrentActivity(target);
			mSeqTree.setFirstCandidate(target);

			// Fill in required launch information
			oLaunch.mEndSession = mEndSession;
			oLaunch.mActivityID = iTarget;
			oLaunch.mResourceID = target.getResourceID();

			oLaunch.mStateID = target.getStateID();
			if (oLaunch.mStateID == null) {
				oLaunch.mStateID = iTarget;
			}

			oLaunch.mNumAttempt = target.getNumAttempt()
					+ target.getNumSCOAttempt();
			oLaunch.mMaxTime = target.getAttemptAbDur();

			// Create auxilary services vector
			Hashtable services = new Hashtable();
			ADLAuxiliaryResource test = null;
			walk = target;

			// Starting at the target activity, walk up the tree adding services
			while (walk != null) {
				Vector curSet = (Vector) walk.getAuxResources();

				if (curSet != null) {

					for (int i = 0; i < curSet.size(); i++) {
						ADLAuxiliaryResource res = null;
						res = (ADLAuxiliaryResource) curSet.elementAt(i);

						// If the resource isn't already included in the set,
						// add it
						test = (ADLAuxiliaryResource) services.get(res.mType);

						if (test == null) {
							services.put(res.mType, res);
						}
					}
				}

				// Walk up the tree
				walk = walk.getParent();
			}

			if (services.size() > 0) {
				oLaunch.mServices = services;
			}
		}

		if (_Debug) {
			System.out.println("  ::--> Content Delivery Valididation");
		}

		validateRequests();
		oLaunch.mNavState = mSeqTree.getValidRequests();

		// Make sure Continue Exit is not enabled for non-content
		if (oLaunch.mSeqNonContent != null) {
			oLaunch.mNavState.mContinueExit = false;
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - "
					+ "contentDelivery");
		}
	}

	/**
	 * This is a utility method that clears the Suspended Activity upon launch
	 * of content.
	 * 
	 * @param iTarget
	 *            Identififies the target activity for delivery.
	 */
	private void clearSuspendedActivity(SeqActivity iTarget) {

		// This method implements the Clear Supsended Activity Subprocess (DB.2)

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "clearSuspendedActivity");
		}

		SeqActivity act = mSeqTree.getSuspendAll();

		if (iTarget == null) {
			if (_Debug) {
				System.out.println("  ::--> Nothing to deliver");
			}

			act = null;
		}

		if (act != null) {

			if (iTarget != act) {

				ISeqActivity common = findCommonAncestor(iTarget, act);

				while (act != common) {
					act.setIsSuspended(false);

					List children = act.getChildren(false);

					if (children != null) {
						boolean done = false;

						for (int i = 0; i < children.size() && !done; i++) {
							SeqActivity lookAt = (SeqActivity) children.get(i);

							if (lookAt.getIsSuspended()) {
								act.setIsSuspended(true);

								done = true;
							}
						}
					}

					act = act.getParent();
				}
			} else {
				if (_Debug) {
					System.out.println("  ::--> Target is the Suspended Act");
				}
			}

			// Clear the suspended activity
			SeqActivity temp = null;
			mSeqTree.setSuspendAll(temp);
		} else {
			if (_Debug) {
				System.out.println("  ::-->  Nothing to clear");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - "
					+ "clearSuspendedActivity");
		}
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Utility Processes

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Evaluate all limit conditions defined for the target activity.
	 * 
	 * @param iTarget
	 *            Target activity for limit condition evaluations.
	 * 
	 * @return <code>true</code> if the evaulation of limit condtions for the
	 *         target activity result in that activity becoming disabled,
	 *         otherwise <code>false</code>.
	 */
	private boolean evaluateLimitConditions(SeqActivity iTarget) {

		// This is an implementation of UP.1

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "evaluateLimitConditions");
			System.out.println("  ::-->  Target: " + iTarget.getID());
		}

		// For 2, we only test max attempt limits
		// Need to add all limit condition tests...

		boolean disabled = false;

		// Only test limitConditions if the activity is not active
		if (!iTarget.getIsActive() && !iTarget.getIsSuspended()) {
			if (iTarget.getAttemptLimitControl()) {
				disabled = iTarget.getNumAttempt() >= iTarget.getAttemptLimit();
			}
		}

		if (_Debug) {
			System.out.println("  ::-->  " + disabled);
			System.out.println("  :: ADLSequencer --> END   - "
					+ "evaluateLimitConditions");
		}

		return disabled;
	}

	/**
	 * End the attempt on active descendents of the target.
	 * 
	 * @param iTarget
	 *            Activity for which all descendent attempts will end.
	 */
	private void terminateDescendentAttempts(SeqActivity iTarget) {

		// This is an implementation of the Terminate Descendent Attempts
		// Process (UP.3)

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "terminateDescendentAttempts");

			if (iTarget != null) {
				System.out.println("  ::-->  Target: " + iTarget.getID());
			} else {
				System.out.println("  ::--> ERROR : NULL Activity");
			}
		}

		SeqActivity cur = mSeqTree.getFirstCandidate();

		if (cur != null) {
			ISeqActivity common = findCommonAncestor(cur, iTarget);
			SeqActivity walk = cur;

			while (walk != common) {
				endAttempt(walk, false);

				walk = walk.getParent();
			}
		} else {
			if (_Debug) {
				System.out.println("  ::--> No current activity");
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   -  "
					+ "terminateDescendentAttempts");
		}
	}

	/**
	 * End the attempt on the target activity and perform any required state
	 * maintenance on the activity tree.
	 * 
	 * @param iTarget
	 *            Activity for which an attempt will end.
	 * 
	 * @param iTentative
	 *            Indicates if the attempt should 'really' end.
	 */
	private void endAttempt(SeqActivity iTarget, boolean iTentative) {

		// This is an implementation of the End Attempt Process (UP.4)

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - endAttempt");

			if (iTarget != null) {
				System.out.println("  ::--> Target : " + iTarget.getID());
			} else {
				System.out.println("  ::--> ERROR : NULL Activity");
			}

			System.out.println("  ::--> REAL?    -- "
					+ ((iTentative) ? "NO" : "YES"));
		}

		if (iTarget != null) {
			List children = iTarget.getChildren(false);

			// Is the activity a tracked leaf
			if (children == null && iTarget.getIsTracked()) {

				// If the attempt was not suspended, perform attempt cleanup
				if (!iTarget.getIsSuspended()) {
					if (!iTarget.getSetCompletion()) {
						// If the content hasn't set this value, set it
						if (!iTarget.getProgressStatus(false)) {
							iTarget.setProgress(ADLTracking.TRACK_COMPLETED);
						}
					}

					if (!iTarget.getSetObjective()) {
						// If the content hasn't set this value, set it
						if (!iTarget.getObjStatus(false, true)) {
							iTarget
									.setObjSatisfied(ADLTracking.TRACK_SATISFIED);
						}
					}
				}
			} else if (children != null) {
				// The activity is a cluster, check if any of its children are
				// suspended.

				// Only set suspended state if this is a 'real' termiantion
				if (!iTentative) {

					iTarget.setIsSuspended(false);

					for (int i = 0; i < children.size(); i++) {
						SeqActivity act = (SeqActivity) children.get(i);

						if (act.getIsSuspended()) {
							iTarget.setIsSuspended(true);
							break;
						}
					}

					// If the cluster is not suspended check for selection and
					// randomization
					if (!iTarget.getIsSuspended()) {
						if (iTarget.getSelectionTiming().equals(
								SeqActivity.TIMING_EACHNEW)) {
							doSelection(iTarget);
							iTarget.setSelection(true);
						}

						if (iTarget.getRandomTiming().equals(
								SeqActivity.TIMING_EACHNEW)) {
							doRandomize(iTarget);
							iTarget.setRandomized(true);
						}
					}
				}
			}

			// The activity becomes inactive if this is a 'real' termination
			if (!iTentative) {
				iTarget.setIsActive(false);

				if (iTarget.getIsTracked()) {
					// Make sure satisfaction is updated according to measure
					iTarget.triggerObjMeasure();
				}

				// Invoke rollup
				invokeRollup(iTarget, null);
			}
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - endAttempt");
		}
	}

	/**
	 * Checks if the identified activity is allowed to be considered for
	 * delivery during a sequencing process.<br>
	 * 
	 * @param iTarget
	 *            Target activity for limit condition evaluations.
	 * 
	 * @return <code>true</code> if the activity should not be considered for
	 *         delivery, otherwise <code>false</code>.
	 */
	private boolean checkActivity(SeqActivity iTarget) {

		// This is an implementation of UP.5.

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - "
					+ "checkActivity");
			System.out.println("  ::-->  Target: " + iTarget.getID());
		}

		boolean disabled = false;
		String result = null;

		// Attempt to get rule information from the activity node
		ISeqRuleset disabledRules = iTarget.getPreSeqRules();

		if (disabledRules != null) {
			result = disabledRules.evaluate(SeqRuleset.RULE_TYPE_DISABLED,
					iTarget, mRetry);
		}

		// If the rule evaluation did not return null, the activity must
		// be disabled.
		if (result != null) {
			disabled = true;
		}

		if (!disabled) {
			// Evaluate other limit conditions associated with the activity.
			disabled = evaluateLimitConditions(iTarget);
		}

		if (_Debug) {
			System.out.println("  ::-->  " + disabled);
			System.out.println("  :: ADLSequencer --> END   - "
					+ "checkActivity");
		}

		return disabled;

	}

	/**
	 * Indicates if a retry sequencing request is being processed. If it is,
	 * this flag indicates to the sequencer to assume 'default' status
	 * information when evaluating sequencing information.
	 * 
	 * @param iRetry
	 *            Indicates if the sequencer is processing a retry request.
	 */
	private void setRetry(boolean iRetry) {
		mRetry = false; // iRetry;

		if (_Debug) {
			System.out.println("  ::--> RETRY == " + mRetry);
		}
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Methods to facilitate navigation and UI

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	
	/**
	 * Displays the values of the <code>ADLTOC</code> objects that constitute
	 * table of contents. This method is used for diagnostic purposes.
	 * 
	 * @param iOldTOC
	 *            A vector of <code>ADLTOC</code> objects describing the
	 *            'first pass' TOC.
	 * 
	 * @param oNewTOC
	 *            A vector of <code>ADLTOC</code> objects describing the
	 *            'final pass' TOC.
	 * 
	 * @return The set of valid activity IDs for 'Choice' navigation requests.
	 */
	private Hashtable getChoiceSet(TreeModel treeModel) {
		Hashtable set = null;
		String lastLeaf = null;

		
		if (treeModel != null) {
			ActivityNode tempNode = null;
			set = new Hashtable();

			ActivityNode rootNode = (ActivityNode)treeModel.getRoot();
		
			if (rootNode != null) {
				Enumeration<ActivityNode> breadthFirst = rootNode.breadthFirstEnumeration();
				
				List<ActivityNode> bfList = Collections.list(breadthFirst);
				
				// Traverse the breadth-first search backwards
				for (int i=bfList.size() - 1;i>0;i--) {
					tempNode = bfList.get(i);
										
					if (tempNode.getDepth() == -1) {
						if (tempNode.isSelectable()) {
							set.put(tempNode.getActivity().getID(), tempNode);
						}
					} else if (!tempNode.isHidden()) {
						set.put(tempNode.getActivity().getID(), tempNode);
					}
					
					if (lastLeaf == null) {
						if (tempNode.isLeaf() && tempNode.isEnabled())
							lastLeaf = tempNode.getActivity().getID();
					}
					
				}
			}
		}

		if (lastLeaf != null) {
			if (_Debug) {
				System.out.println("  ::--> Setting last leaf --> " + lastLeaf);
			}

			mSeqTree.setLastLeaf(lastLeaf);
		}

		// If there are no items in the set, there is no TOC.
		if (set.size() == 0) {
			set = null;
		}

		
		// TODO: JLR -- think we might be able to live without this... 9/10/2007
		
		// If there is only one item in the set, it must be the root -- remove
		// it
		// If there is only one item in the set, it is the parent of a
		// choiceExit == false cluster, it cannot be selected -- no TOC
		/*if (oNewTOC.size() == 1) {
			ADLTOC temp = (ADLTOC) oNewTOC.elementAt(0);

			if (!temp.mIsEnabled) {
				if (_Debug) {
					System.out.println("  ::--> Clearing single non-enabled "
							+ " activity");
				}

				oNewTOC.remove(0);
			} else if (!temp.mLeaf) {
				if (_Debug) {
					System.out.println("  ::--> Clearing root activity");
				}

				oNewTOC.remove(0);
			}
		}*/

		return set;
	}

	
	/*private Hashtable getChoiceSet(Vector iOldTOC, Vector oNewTOC) {
		if (_Debug) {
			System.out.println("  :: ADLSequencer  --> BEGIN - getChoiceSet");

			if (iOldTOC != null) {
				System.out.println("  ::--> " + iOldTOC.size());
			} else {
				System.out.println("  ::--> NULL");
			}
		}

		Hashtable set = null;
		String lastLeaf = null;

		if (iOldTOC != null) {
			ADLTOC temp = null;
			set = new Hashtable();

			// Walk backward along the vector looking for the last available
			// leaf
			for (int i = iOldTOC.size() - 1; i >= 0; i--) {
				temp = (ADLTOC) iOldTOC.elementAt(i);

				if (temp.mDepth == -1) {
					if (_Debug) {
						System.out.println("  ::--> Remove :: " + temp.mID);
					}

					if (temp.mIsSelectable) {

						if (_Debug) {
							System.out.println("  ::--> Invisible :: "
									+ temp.mID);
						}

						// Not in the TOC, but still a valid target
						set.put(temp.mID, temp);
					}
				} else if (temp.mIsVisible) {
					set.put(temp.mID, temp);
					oNewTOC.add(temp);
				}

				if (lastLeaf == null) {
					if (temp.mLeaf && temp.mIsEnabled) {
						lastLeaf = temp.mID;
					}
				}
			}
		}

		if (lastLeaf != null) {
			if (_Debug) {
				System.out.println("  ::--> Setting last leaf --> " + lastLeaf);
			}

			mSeqTree.setLastLeaf(lastLeaf);
		}

		// If there are no items in the set, there is no TOC.
		if (set.size() == 0) {
			set = null;
		}

		// If there is only one item in the set, it must be the root -- remove
		// it
		// If there is only one item in the set, it is the parent of a
		// choiceExit == false cluster, it cannot be selected -- no TOC
		if (oNewTOC.size() == 1) {
			ADLTOC temp = (ADLTOC) oNewTOC.elementAt(0);

			if (!temp.mIsEnabled) {
				if (_Debug) {
					System.out.println("  ::--> Clearing single non-enabled "
							+ " activity");
				}

				oNewTOC.remove(0);
			} else if (!temp.mLeaf) {
				if (_Debug) {
					System.out.println("  ::--> Clearing root activity");
				}

				oNewTOC.remove(0);
			}
		}

		if (_Debug) {
			if (set != null) {
				System.out.println("  ::--> " + set.size() + "  //  "
						+ oNewTOC.size());

				for (int i = 0; i < oNewTOC.size(); i++) {
					ADLTOC temp = (ADLTOC) oNewTOC.elementAt(i);

					temp.dumpState();
				}
			} else {
				System.out.println("  ::--> NULL");
			}

			System.out.println("  :: ADLSequencer  --> END   - getChoiceSet");
		}

		return set;
	}
	
	
	
	
	private MutableTreeNode addNode(Map<Integer, MutableTreeNode> nodeMap, SeqActivity activity) {
		Integer i = Integer.valueOf(activity.getCount());
		
		if (!nodeMap.containsKey(i)) {
			MutableTreeNode node = new DefaultMutableTreeNode(activity);
			nodeMap.put(i, node);
		}
		
		return nodeMap.get(i);
	}
	
	private MutableTreeNode addNode(Map<Integer, MutableTreeNode> nodeMap, MutableTreeNode node) {
		SeqActivity activity = (SeqActivity)((DefaultMutableTreeNode)node).getUserObject();
		Integer i = Integer.valueOf(activity.getCount());
		
		if (!nodeMap.containsKey(i)) {
			nodeMap.put(i, node);
		}
		
		return nodeMap.get(i);
	}
	
	private MutableTreeNode getNode(Map<Integer, MutableTreeNode> nodeMap, Integer key) {
		return nodeMap.get(key);
	}
	
	
	private MutableTreeNode nodify(ISeqActivity activity, boolean all) {
		MutableTreeNode node = new DefaultMutableTreeNode(activity);
		List<ISeqActivity> children = ((SeqActivity)activity).getChildren(all);
		
		if (null != children) {
			for (ISeqActivity child : children) {
				MutableTreeNode childNode = nodify(child, all);
				((DefaultMutableTreeNode)node).add(childNode);
			}
		}
		
		return node;
	}
	
	
	private int addChildren(DefaultMutableTreeNode node, List copy){
		SeqActivity activity = (SeqActivity)node.getUserObject();
		
		int count = activity.getCount();
		int found = 0;
		log.info("Node  (" + activity.getTitle() +")");
		log.info("Remaining items ("+ copy.size() +")");
		
		//
		// find any objects that have this node's number as parent (eg, tocObject.getParent()+1==this.count)
		// .... make a new node for it, add it to this node, then call this method with that new node
		//
		for(int i=copy.size()-1;i>=0;i-=(found+1)){ /// original TOC objects build with higher order items last
			ADLTOC t = (ADLTOC)copy.get(i);
			if(t.mParent+1 == count){
				log.info("Adding subnode (" + t.mID + ") to (" + activity.getTitle() +")");
				SeqActivity newActivity = mSeqTree.getActivity(t.mID);
				ActivityNode newNode = new ActivityNode(newActivity);
				node.add(newNode);
				found++;
				copy.remove(i--);
				found += addChildren(newNode, copy);
			}
		}
		
		///Now, do the same for all the children of this node
		for(Enumeration<ActivityNode> children = node.children();children.hasMoreElements();) {
			ActivityNode child = children.nextElement();
			found += addChildren(child, copy);
		}
		
		return found;
	}
	
		
	private TreeModel convertTOC(List<ADLTOC> tocList) {
		ActivityNode root = null;
		
		List copy = new Vector(tocList);
		ADLTOC temp;
		
		// Get the root
		for (int i=0;i<copy.size();++i){
			temp = (ADLTOC)copy.get(i);
			if(temp.mParent == -1){ ///presumes that there is only one 'root'
				SeqActivity activity = mSeqTree.getActivity(temp.mID);
				root = new ActivityNode(activity);
				copy.remove(i);
				break;
			}
		}

		// Traverse the tree while there are still outstanding objects && while there is still progress
		for(int lastrun=0, thisrun = -1;copy.size()>0 && lastrun!=thisrun;lastrun=thisrun,thisrun=-1){
			thisrun = addChildren(root, copy);
		}
		
		root.sortChildrenRecursively();
		

		//fixTreeNodes(root);
		
		TreeModel treeModel = new DefaultTreeModel(root);
		return treeModel;
	}*/
	
	/*
	 * This is a method inserted for the Sakai SCORM implementation
	 * 
	 * 
	 */
	/*public TreeModel getTreeModel(ISeqActivity iStart) {
		Map<Integer, MutableTreeNode> nodeMap = new HashMap<Integer, MutableTreeNode>();
		
		Vector lookAt = new Vector();
		boolean done = false;

		// Make sure we have an activity tree
		if (mSeqTree == null) {
			if (_Debug) {
				System.out.println("  ::-->  No Activity Tree");
			}

			done = true;
		}
		
		// Perform a breadth-first walk of the activity tree.
		SeqActivity walk = (SeqActivity)iStart;
		int depth = 0;

		// Tree traversal status indicators
		boolean next = false;
		boolean include = false;
		boolean collapse = false;

		// Make sure the activity has been associated with this sequencer
		// If not, build the TOC from the root
		if (walk == null) {
			walk = mSeqTree.getRoot();
		}
		
		MutableTreeNode startNode = nodify(walk, true);
		
		return new DefaultTreeModel(startNode);
	}*/
	
	/**
	 * This method returns the current (as of the function call) valid Table of
	 * Contents (TOC) for the activity tree. The format of the resulting vector
	 * is a result of a breadth-first walk of the activity tree.<br>
	 * <br>
	 * 
	 * @param iStart
	 *            The 'root' of the requested TOC.
	 * 
	 * @return A vector of <code>ADLTOC</code> objects describing the current.
	 */
	/*private Vector getTOC(SeqActivity iStart) {

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> BEGIN - getTOC");
		}

		Vector toc = new Vector();
		ADLTOC temp = null;
		boolean done = false;

		// Make sure we have an activity tree
		if (mSeqTree == null) {
			if (_Debug) {
				System.out.println("  ::-->  No Activity Tree");
			}

			done = true;
		}

		// Perform a breadth-first walk of the activity tree.
		SeqActivity walk = iStart;
		int depth = 0;
		int parentTOC = -1;
		Vector lookAt = new Vector();
		Vector flatTOC = new Vector();

		// Tree traversal status indicators
		boolean next = false;
		boolean include = false;
		boolean collapse = false;

		// Make sure the activity has been associated with this sequencer
		// If not, build the TOC from the root
		if (walk == null) {
			walk = mSeqTree.getRoot();
		}

		if (!done) {
			if (_Debug) {
				System.out.println("  ::--> Building TOC from:  "
						+ walk.getID());
			}
		}

		SeqActivity cur = mSeqTree.getFirstCandidate();
		int curIdx = -1;

		if (cur == null) {
			cur = mSeqTree.getCurrentActivity();
		}

		while (!done) {
			include = false;
			collapse = false;
			next = false;

			// If the activity is a valid target for a choice sequecing request,
			// include it in the TOC and determine its attributes
			if (walk.getParent() != null) {
				if (walk.getParent().getControlModeChoice()) {
					include = true;
				}
			} else {
				// Always include the root of the activity tree in the TOC
				include = true;
			}

			// Make sure the activity we are considering is not disabled or
			// hidden
			if (include) {
				// Attempt to get rule information from the activity
				ISeqRuleset hiddenRules = walk.getPreSeqRules();

				String result = null;

				if (hiddenRules != null) {
					result = hiddenRules.evaluate(SeqRuleset.RULE_TYPE_HIDDEN,
							walk, false);
				}

				// If the rule evaluation did not return null, the activity
				// must be hidden.
				if (result != null) {
					if (_Debug) {
						System.out.println("  ::--> HIDDEN");
					}

					include = false;
					collapse = true;
				} else {
					// Check if this activity is prevented from activation
					if (walk.getPreventActivation() && !walk.getIsActive()) {
						if (cur != null) {
							if (walk != cur && cur.getParent() != walk) {
								if (_Debug) {
									System.out.println("  ::--> PREVENTED !!");
									System.out.println(" " + walk.getID()
											+ " != " + cur.getParent().getID());
								}

								include = false;
							}
						} else {
							if (_Debug) {
								System.out
										.println("  ::--> PREVENTED -- no cur");
							}

							include = false;
						}
					}
				}
			}

			// The activity is included in the TOC, set its attributes
			if (include) {
				ISeqActivity parent = walk.getParent();

				temp = new ADLTOC();

				temp.mCount = walk.getCount();
				temp.mTitle = walk.getTitle();
				temp.mDepth = depth;
				temp.mIsVisible = walk.getIsVisible();
				temp.mIsEnabled = !checkActivity(walk);
				temp.mID = walk.getID();

				if (walk.getParent() != null) {
					temp.mInChoice = walk.getParent().getControlModeChoice();
				} else {
					temp.mInChoice = true;
				}

				// Check if we looking at the 'current' cluster
				if (cur != null) {
					if (temp.mID.equals(cur.getID())) {
						temp.mIsCurrent = true;
						curIdx = toc.size();
					}
				}

				temp.mLeaf = !walk.hasChildren(false);
				temp.mParent = parentTOC;

				if (_Debug) {
					System.out.println("  :: Added :: " + temp.mID + "  [[ "
							+ temp.mDepth + " ]] (" + temp.mParent + ")   //  "
							+ temp.mIsSelectable + "  [E] " + temp.mIsEnabled);
				}

				toc.add(temp);
			} else {
				temp = new ADLTOC();

				temp.mCount = walk.getCount();
				temp.mTitle = walk.getTitle();
				temp.mIsVisible = walk.getIsVisible();

				temp.mIsEnabled = !checkActivity(walk);
				temp.mDepth = -(depth);
				temp.mID = walk.getID();
				temp.mIsSelectable = false;

				temp.mLeaf = (walk.getChildren(false) == null);
				temp.mParent = parentTOC;

				if (collapse) {
					temp.mIsVisible = false;
				}

				if (_Debug) {
					System.out.println("  :: Added not-included :: " + temp.mID
							+ "  [[ " + temp.mDepth + " ]] (" + temp.mParent
							+ ")   //  " + temp.mIsSelectable + "  [E] "
							+ temp.mIsEnabled);
				}

				toc.add(temp);
			}

			// Add this activity to the "flat TOC"
			flatTOC.add(walk);

			// If this activity has children, look at them later...
			if (walk.hasChildren(false)) {
				// Remember where we are at and look at the children now,
				// unless we are at the root
				if (walk.getParent() != null) {
					lookAt.add(walk);
				}

				// Go to the first child
				walk = (SeqActivity) (walk.getChildren(false)).get(0);
				parentTOC = toc.size() - 1;
				depth++;

				next = true;
			}

			if (!next) {
				// Move to its sibling
				walk = walk.getNextSibling(false);
				temp = (ADLTOC) toc.elementAt(toc.size() - 1);
				parentTOC = temp.mParent;

				while (walk == null && !done) {
					if (lookAt.size() > 0) {
						// Walk back up the tree to the parent's next sibling
						walk = (SeqActivity) lookAt
								.elementAt(lookAt.size() - 1);
						lookAt.remove(lookAt.size() - 1);
						depth--;

						// Find the correct parent
						temp = (ADLTOC) toc.elementAt(parentTOC);

						while (!temp.mID.equals(walk.getID())) {
							parentTOC = temp.mParent;
							temp = (ADLTOC) toc.elementAt(parentTOC);
						}

						walk = walk.getNextSibling(false);
					} else {
						done = true;
					}
				}

				if (walk != null) {
					parentTOC = temp.mParent;
				}
			}
		}

		if (_Debug) {
			System.out.println("  ::--> Completed first pass");
		}

		// After the TOC has been created, mark activites unselectable
		// if the Prevent Activation prevents them being selected,
		// and mark them invisible if they are descendents of a hidden
		// from choice activity
		int hidden = -1;
		int prevented = -1;

		for (int i = 0; i < toc.size(); i++) {
			SeqActivity tempAct = (SeqActivity) flatTOC.elementAt(i);
			ADLTOC tempTOC = (ADLTOC) toc.elementAt(i);

			if (_Debug) {
				System.out.println("  ::--> Evaluating --> " + tempAct.getID());
				System.out.println("                   --> " + tempTOC.mTitle);
			}

			int checkDepth = ((tempTOC.mDepth >= 0) ? tempTOC.mDepth
					: (-tempTOC.mDepth));

			if (hidden != -1) {
				// Check to see if we are done hiding activities
				if (checkDepth <= hidden) {
					hidden = -1;
				} else {
					// This must be a descendent
					tempTOC.mDepth = -(depth);
					tempTOC.mIsSelectable = false;

					tempTOC.mIsVisible = false;
				}
			}

			// Evaluate hide from choice rules if we are not hidden
			if (hidden == -1) {
				// Attempt to get rule information from the activity
				ISeqRuleset hiddenRules = tempAct.getPreSeqRules();

				String result = null;

				if (hiddenRules != null) {
					result = hiddenRules.evaluate(SeqRuleset.RULE_TYPE_HIDDEN,
							tempAct, false);
				}

				// If the rule evaluation did not return null, the activity
				// must be hidden.
				if (result != null) {
					if (_Debug) {
						System.out.println("  ::--> Hidden --> "
								+ tempTOC.mDepth);
					}

					// The depth we are looking for should be positive
					hidden = -tempTOC.mDepth;
					prevented = -1;
				} else {
					if (_Debug) {
						System.out.println("  ::--> Prevented ??" + prevented);
					}

					if (prevented != -1) {
						// Check to see if we are done preventing activities
						if (checkDepth <= prevented) {
							// Reset the check until we find another prevented
							prevented = -1;
						} else {
							// This must be a prevented descendent
							tempTOC.mDepth = -1;
							tempTOC.mIsSelectable = false;
						}
					} else {
						// Check if this activity is prevented from activation
						if (tempAct.getPreventActivation()
								&& !tempAct.getIsActive()) {
							if (cur != null) {
								if (tempAct != cur
										&& cur.getParent() != tempAct) {
									if (_Debug) {
										System.out
												.println("  ::--> PREVENTED !!");
										System.out.println(" "
												+ tempAct.getID() + " != "
												+ cur.getParent().getID());
									}

									include = false;

									prevented = (tempTOC.mDepth > 0) ? tempTOC.mDepth
											: -tempTOC.mDepth;

									// The activity cannot be selected
									temp.mDepth = -1;
									temp.mIsSelectable = false;
								}
							}
						}
					}
				}
			}
		}

		if (_Debug) {
			System.out.println("  ::--> Completed post-1 pass");
		}

		// After the TOC has been created, mark activites unselectable
		// if the Choice Exit control prevents them being selected
		SeqActivity noExit = null;

		if (mSeqTree.getFirstCandidate() != null) {
			walk = mSeqTree.getFirstCandidate().getParent();
		} else {
			walk = null;
		}

		// Walk up the active path looking for a non-exiting cluster
		while (walk != null && noExit == null) {
			// We cannot choose any target that is outside of the activiy tree,
			// so choice exit does not apply to the root of the tree
			if (walk.getParent() != null) {
				if (!walk.getControlModeChoiceExit()) {
					noExit = walk;
				}
			}

			// Move up the tree
			walk = walk.getParent();
		}

		if (noExit != null) {
			depth = -1;

			if (_Debug) {
				System.out.println("  ::--> Found NoExit Cluster -- "
						+ noExit.getID());
			}

			// Only descendents of this activity can be selected.
			for (int i = 0; i < toc.size(); i++) {
				temp = (ADLTOC) toc.elementAt(i);

				// When we find the the 'non-exiting' activity, remember its
				// depth
				if (temp.mID.equals(noExit.getID())) {
					depth = (temp.mDepth > 0) ? temp.mDepth : -temp.mDepth;

					// The cluster activity cannot be selected
					temp.mDepth = -1;
					temp.mIsSelectable = false;
				}

				// If we haven't found the the 'non-exiting' activity yet, then
				// the
				// activity being considered cannot be selected.
				else if (depth == -1) {
					temp.mDepth = -1;
					temp.mIsSelectable = false;
				}

				// When we back out of the depth-first-walk and encounter a
				// sibling
				// or parent of the 'non-exiting' activity, start making
				// activity
				// unselectable
				else if (((temp.mDepth > 0) ? temp.mDepth : -temp.mDepth) <= depth) {
					depth = -1;

					temp.mDepth = -1;
					temp.mIsSelectable = false;
				}
			}
		}

		// Boundary Condition -- evaluate choice exit on root
		temp = (ADLTOC) toc.elementAt(0);
		SeqActivity root = mSeqTree.getRoot();

		if (!root.getControlModeChoiceExit()) {
			temp.mIsSelectable = false;
		}

		if (_Debug) {
			System.out.println("  ::--> Completed second pass");
		}

		// Look for constrained activities relative to the current activity and
		// mark activites unselectable if they are outside of the avaliable set
		SeqActivity con = null;

		if (mSeqTree.getFirstCandidate() != null) {
			walk = mSeqTree.getFirstCandidate().getParent();
		} else {
			walk = null;
		}

		// Walk up the tree to the root
		while (walk != null && con == null) {

			if (walk.getConstrainChoice()) {
				con = walk;
			}

			walk = walk.getParent();
		}

		// Evaluate constrained choice set
		if (con != null) {

			if (_Debug) {
				System.out
						.println("  ::-->  Constrained Choice Activity Found");
				System.out.println("  ::-->  Stopped at --> " + con.getID());
			}

			int forwardAct = -1;
			int backwardAct = -1;
			List list = null;

			Walk walkCon = new Walk();
			walkCon.at = con;

			// Find the next activity relative to the constrained activity.
			processFlow(FLOW_FORWARD, false, walkCon, true);

			if (walkCon.at == null) {
				if (_Debug) {
					System.out.println("  ::--> Walked forward off the tree");
				}

				walkCon.at = con;
			}

			String lookFor = "";
			list = walkCon.at.getChildren(false);
			if (list != null) {
				int size = list.size();
				lookFor = ((SeqActivity) list.get(size - 1)).getID();
			} else {
				lookFor = walkCon.at.getID();
			}

			for (int j = 0; j < toc.size(); j++) {
				temp = (ADLTOC) toc.elementAt(j);

				if (temp.mID.equals(lookFor)) {
					forwardAct = j;
					break;
				}
			}

			// Find the previous activity relative to the constrained activity.
			walkCon.at = con;
			processFlow(FLOW_BACKWARD, false, walkCon, true);

			if (walkCon.at == null) {
				if (_Debug) {
					System.out.println("  ::--> Walked backward off the tree");
				}

				walkCon.at = con;
			}

			lookFor = walkCon.at.getID();
			for (int j = 0; j < toc.size(); j++) {
				temp = (ADLTOC) toc.elementAt(j);

				if (temp.mID.equals(lookFor)) {
					backwardAct = j;
					break;
				}
			}

			// If the forward activity on either end of the range is a cluster,
			// we need to include its descendents
			temp = (ADLTOC) toc.elementAt(forwardAct);
			if (!temp.mLeaf) {
				int idx = forwardAct;
				boolean foundLeaf = false;

				while (!foundLeaf) {
					for (int i = toc.size() - 1; i > idx; i--) {
						temp = (ADLTOC) toc.elementAt(i);

						if (temp.mParent == idx) {
							idx = i;
							foundLeaf = temp.mLeaf;

							break;
						}
					}
				}

				if (idx != toc.size()) {
					forwardAct = idx;
				}
			}

			if (_Debug) {
				System.out.println("  ::--> Constrained Range == [ "
						+ backwardAct + " , " + forwardAct + " ]");
			}

			// Disable activities outside of the avaliable range
			for (int i = 0; i < toc.size(); i++) {
				temp = (ADLTOC) toc.elementAt(i);

				if (i < backwardAct || i > forwardAct) {
					temp.mIsSelectable = false;

					if (_Debug) {
						System.out.println("  ::--> Turn off -- " + temp.mID);
					}
				}
			}
		}

		if (_Debug) {
			System.out.println("  ::--> Completed third pass");
		}

		// Walk the TOC looking for disabled activities...
		if (toc != null) {
			depth = -1;

			for (int i = 0; i < toc.size(); i++) {
				temp = (ADLTOC) toc.elementAt(i);

				if (depth != -1) {
					if (depth >= ((temp.mDepth > 0) ? temp.mDepth
							: -temp.mDepth)) {
						depth = -1;
					} else {
						temp.mIsEnabled = false;
						temp.mIsSelectable = false;
					}
				}

				if (!temp.mIsEnabled && depth == -1) {
					// Remember where the disabled activity is
					depth = (temp.mDepth > 0) ? temp.mDepth : -temp.mDepth;

					if (_Debug) {
						System.out.println("  ::--> [" + i + "]  "
								+ "Found Disabled -->  " + temp.mID + "  <<"
								+ temp.mDepth + ">>");
					}
				}

			}
		}

		if (_Debug) {
			System.out.println("  ::--> Completed fourth pass");
		}

		// If there is a current activity, check availablity of its siblings
		// This pass corresponds to Case #2 of the Choice Sequencing Request
		if (toc != null && curIdx != -1) {
			if (_Debug) {
				System.out
						.println("  ::--> Checking Current Activity Siblings");
			}

			int par = ((ADLTOC) toc.elementAt(curIdx)).mParent;
			int idx;

			// Check if the current activity is in a forward only cluster
			if (cur.getParent() != null
					&& cur.getParent().getControlForwardOnly()) {
				idx = curIdx - 1;

				temp = (ADLTOC) toc.elementAt(idx);
				while (temp.mParent == par) {
					temp.mIsSelectable = false;

					idx--;
					temp = (ADLTOC) toc.elementAt(idx);
				}
			}

			// Check for Stop Forward Traversal Rules
			idx = curIdx;
			boolean blocked = false;

			while (idx < toc.size()) {
				temp = (ADLTOC) toc.elementAt(idx);
				if (temp.mParent == par) {
					if (!blocked) {
						ISeqRuleset stopTrav = getActivity(temp.mID)
								.getPreSeqRules();

						String result = null;
						if (stopTrav != null) {
							result = stopTrav.evaluate(
									SeqRuleset.RULE_TYPE_FORWARDBLOCK,
									getActivity(temp.mID), false);
						}

						// If the rule evaluation did not return null, the
						// activity is blocked
						blocked = (result != null);
					} else {
						temp.mIsSelectable = false;
					}
				}

				idx++;
			}
		}

		if (_Debug) {
			System.out.println("  ::--> Completed fifth pass");
		}

		// Evaluate Stop Forward Traversal Rules -- this pass cooresponds to
		// Case #3 and #5 of the Choice Sequencing Request Subprocess. In these
		// cases, we need to check if the target activity is forward in the
		// Activity Tree relative to the commen ancestor and cuurent activity
		if (toc != null && curIdx != -1) {
			if (_Debug) {
				System.out.println("  ::--> Checking Stop Forward Traversal");
			}

			int curParent = ((ADLTOC) toc.elementAt(curIdx)).mParent;

			int idx = toc.size() - 1;
			temp = (ADLTOC) toc.elementAt(idx);

			// Walk backward from last available activity,
			// checking each until we get to a sibling of the current activity
			while (temp.mParent != -1 && temp.mParent != curParent) {
				temp = (ADLTOC) toc.elementAt(temp.mParent);
				ISeqRuleset stopTrav = getActivity(temp.mID).getPreSeqRules();

				String result = null;
				if (stopTrav != null) {
					result = stopTrav.evaluate(
							SeqRuleset.RULE_TYPE_FORWARDBLOCK,
							getActivity(temp.mID), false);
				}

				// If the rule evaluation did not return null,
				// then all of its descendents are blocked
				if (result != null) {
					if (_Debug) {
						System.out.println("  ::--> BLOCKED SOURCE --> "
								+ temp.mID + " [" + temp.mDepth + "]");
					}

					// The depth of the blocked activity
					int blocked = temp.mDepth;

					for (int i = idx; i < toc.size(); i++) {
						ADLTOC tempTOC = (ADLTOC) toc.elementAt(i);

						int checkDepth = ((tempTOC.mDepth >= 0) ? tempTOC.mDepth
								: (-tempTOC.mDepth));

						// Check to see if we are done blocking activities
						if (checkDepth <= blocked) {
							break;
						}

						// This activity must be a descendent
						tempTOC.mIsSelectable = false;
					}
				}

				idx--;
				temp = (ADLTOC) toc.elementAt(idx);
			}
		}

		if (_Debug) {
			System.out.println("  ::--> Completed sixth pass");
		}

		// Boundary condition -- if there is a TOC make sure all "selectable"
		// clusters actually flow into content
		for (int i = 0; i < toc.size(); i++) {
			temp = (ADLTOC) toc.elementAt(i);

			if (!temp.mLeaf) {
				if (_Debug) {
					System.out
							.println("  ::--> Process 'Continue' request from "
									+ temp.mID);
				}

				SeqActivity from = getActivity(temp.mID);

				// Confirm 'flow' is enabled from this cluster
				if (from.getControlModeFlow()) {
					// Begin traversing the activity tree from the root
					Walk treeWalk = new Walk();
					treeWalk.at = from;

					boolean success = processFlow(ADLSequencer.FLOW_FORWARD,
							true, treeWalk, false);

					if (!success) {
						temp.mIsSelectable = false;

						if (_Debug) {
							System.out
									.println("  :+: CONTINUE FAILED :+:  --> "
											+ treeWalk.at.getID());
						}
					}
				} else {
					// Cluster does not have flow == true
					temp.mIsSelectable = false;
				}
			}
		}

		if (_Debug) {
			System.out.println("  ::--> Completed seventh pass");
		}

		for (int i = toc.size() - 1; i >= 0; i--) {
			temp = (ADLTOC) toc.elementAt(i);

			if (temp.mIsCurrent && temp.mInChoice) {
				if (temp.mDepth < 0) {
					temp.mDepth = -temp.mDepth;
				}
			}

			if (temp.mDepth >= 0) {
				while (temp.mParent != -1) {
					temp = (ADLTOC) toc.elementAt(temp.mParent);

					if (temp.mDepth < 0) {
						temp.mDepth = -temp.mDepth;
					}
				}
			} else if (temp.mIsVisible) {
				temp.mDepth = -1;
			}
		}

		for (int i = 0; i < toc.size(); i++) {
			temp = (ADLTOC) toc.elementAt(i);

			if (!temp.mIsVisible) {
				temp.mDepth = -1;
				Vector parents = new Vector();

				for (int j = i + 1; j < toc.size(); j++) {
					temp = (ADLTOC) toc.elementAt(j);

					if (temp.mParent == i && temp.mDepth > 0) {
						temp.mDepth--;
						parents.add(new Integer(j));
					} else {
						if (temp.mDepth != -1) {
							int idx = parents
									.indexOf(new Integer(temp.mParent));

							if (idx != -1) {
								temp.mDepth--;
								parents.add(new Integer(j));
							}
						}
					}
				}
			}
		}

		if (_Debug) {
			System.out.println("  ::--> Completed TOC walk up");
		}

		if (_Debug) {
			System.out.println("  :: ADLSequencer --> END   - getTOC");
		}

		return toc;
	}*/
	
	
	private boolean canBeIncluded(SeqActivity walk, SeqActivity cur, ActivityNode node) {
		
		// If we're _not_ looking at the root node, and its parent is _not_ a choice event, then quit now.
		if (walk.getParent() != null && !walk.getParent().getControlModeChoice())
			return false;

		// Attempt to get rule information from the activity
		ISeqRuleset hiddenRules = walk.getPreSeqRules();

		String result = null;

		if (hiddenRules != null) {
			result = hiddenRules.evaluate(SeqRuleset.RULE_TYPE_HIDDEN, walk, false);
		}

		// If the rule evaluation did not return null, the activity
		// must be hidden.
		if (result != null) {
			node.setHidden(true);
			return false;
		}

		// Check if this activity is prevented from activation		
		if (walk.getPreventActivation() && !walk.getIsActive()) {
			// Looks like we need a candidate
			if (cur == null)
				return false;
			
			// If we're not looking at the candidate or its parent, then we don't want to include
			if (walk != cur && walk != cur.getParent()) 
				return false;
		}
		
		return true;
	}
	
	
	
	/**
	 * Method added 8/24/2007 JLR
	 * 
	 * This is an attempt to more efficiently generate the TreeModel object that we pass around
	 * instead of the ADLTOC Vector that ADL uses in their reference implementation.
	 * 
	 */
	private TreeModel getTreeModel(SeqActivity iStart) {
		
		Vector<ActivityNode> nodes = new Vector<ActivityNode>();

		log.debug("Generating the table of contents tree model");
		
		boolean done = false;

		// Make sure we have an activity tree
		if (mSeqTree == null) {
			log.warn("No activity tree found");
			done = true;
		}

		// Perform a breadth-first walk of the activity tree.
		SeqActivity walk = iStart;
		int depth = 0;
		int parentTOC = -1;
		Vector lookAt = new Vector();
		Vector flatTOC = new Vector();

		// Tree traversal status indicators
		boolean next = false;

		// Make sure the activity has been associated with this sequencer
		// If not, build the TOC from the root
		if (walk == null) {
			walk = mSeqTree.getRoot();
		}

		if (!done) {
			if (log.isDebugEnabled()) {
				log.debug("  ::--> Building TOC from:  " + walk.getID());
			}
		}

		SeqActivity cur = mSeqTree.getFirstCandidate();
		int curIdx = -1;

		if (cur == null) {
			cur = mSeqTree.getCurrentActivity();
		}

		while (!done) {
			next = false;

			ActivityNode node = new ActivityNode(walk);
			node.setParentLocation(parentTOC);
			node.setEnabled(!checkActivity(walk));
			node.setHidden(!walk.getIsVisible());
			node.setLeaf(!walk.hasChildren(false));
			
			// Check to see if this activity can be included -- JLR
			if (canBeIncluded(walk, cur, node)) {
				ISeqActivity parent = walk.getParent();
				node.setIncluded(true);
				
				// Not sure why we're checking this twice... but I'm sticking with the ADL template here -- JLR
				if (parent != null)
					node.setInChoice(parent.getControlModeChoice());
				else
					node.setInChoice(true);
				
				node.setDepth(depth);
				
				// Check if we looking at the 'current' cluster
				if (cur != null) {
					if (walk.getID().equals(cur.getID())) {
						node.setCurrent(true);
						curIdx = nodes.size();
					}
				}
			} else {
				node.setDepth(-depth);
				node.setSelectable(false);
			}
			
			// Doesn't really matter if it's included or not, we still add it... strangely enough
			nodes.add(node);
			
			// Add this activity to the "flat TOC"
			flatTOC.add(walk);

			// If this activity has children, look at them later...
			if (walk.hasChildren(false)) {
				// Remember where we are at and look at the children now,
				// unless we are at the root
				if (walk.getParent() != null) {
					lookAt.add(walk);
				}

				// Go to the first child
				walk = (SeqActivity) (walk.getChildren(false)).get(0);
				parentTOC = nodes.size() - 1;
				depth++;

				next = true;
			}

			if (!next) {
				// Move to its sibling
				walk = walk.getNextSibling(false);
				
				ActivityNode tempNode = nodes.elementAt(nodes.size() - 1);
				parentTOC = tempNode.getParentLocation();

				while (walk == null && !done) {
					if (lookAt.size() > 0) {
						// Walk back up the tree to the parent's next sibling
						walk = (SeqActivity) lookAt.elementAt(lookAt.size() - 1);
						lookAt.remove(lookAt.size() - 1);
						depth--;

						// Find the correct parent
						tempNode = nodes.elementAt(parentTOC);

						String tempNodeId = tempNode.getActivity().getID();
						String walkId = walk.getID();
						
						while (!tempNodeId.equals(walk.getID())) {
							parentTOC = tempNode.getParentLocation();
							tempNode = nodes.elementAt(parentTOC);
							tempNodeId = tempNode.getActivity().getID();
						}

						walk = walk.getNextSibling(false);
					} else {
						done = true;
					}
				}

				if (walk != null) {
					parentTOC = tempNode.getParentLocation();
				}
			}
		}

		log.debug("  ::--> Completed first pass");
		
		

		// After the TOC has been created, mark activites unselectable
		// if the Prevent Activation prevents them being selected,
		// and mark them invisible if they are descendents of a hidden
		// from choice activity
		int hidden = -1;
		int prevented = -1;

		for (int i = 0; i < nodes.size(); i++) {
			SeqActivity tempAct = (SeqActivity) flatTOC.elementAt(i);
			ActivityNode tempNode = nodes.elementAt(i);

			if (log.isDebugEnabled()) {
				log.debug("  ::--> Evaluating --> " + tempAct.getID());
				log.debug("                   --> " + tempAct.getTitle());
			}

			int tempDepth = tempNode.getDepth();
			
			// Flipping the cardinality of the hidden depths, apparently -- JLR
			int checkDepth = ((tempDepth >= 0) ? tempDepth : (-tempDepth));

			if (hidden != -1) {
				// Check to see if we are done hiding activities
				if (checkDepth <= hidden) {
					hidden = -1;
				} else {
					// This must be a descendent
					tempNode.setDepth(-depth);
					tempNode.setSelectable(false);
					tempNode.setHidden(true);
				}
			}

			// Evaluate hide from choice rules if we are not hidden
			if (hidden == -1) {
				// Attempt to get rule information from the activity
				ISeqRuleset hiddenRules = tempAct.getPreSeqRules();

				String result = null;

				if (hiddenRules != null) {
					result = hiddenRules.evaluate(SeqRuleset.RULE_TYPE_HIDDEN, tempAct, false);
				}

				// If the rule evaluation did not return null, the activity
				// must be hidden.
				if (result != null) {
					// The depth we are looking for should be positive
					hidden = -tempNode.getDepth();
					prevented = -1;
				} else {
					if (log.isDebugEnabled()) 
						log.debug("  ::--> Prevented ??" + prevented);
					
					if (prevented != -1) {
						// Check to see if we are done preventing activities
						if (checkDepth <= prevented) {
							// Reset the check until we find another prevented
							prevented = -1;
						} else {
							// This must be a prevented descendent
							tempNode.setDepth(-1);
							tempNode.setSelectable(false);
						}
					} else {
						// Check if this activity is prevented from activation
						if (tempAct.getPreventActivation() && !tempAct.getIsActive()) {
							if (cur != null) {
								if (tempAct != cur && cur.getParent() != tempAct) {
									if (log.isDebugEnabled()) {
										log.debug("  ::--> PREVENTED !!");
										log.debug(" " + tempAct.getID() + " != " + cur.getParent().getID());
									}

									// Not sure why we need to check this again -- JLR
									tempNode.setIncluded(false);

									// Flipping the cardinality of the hidden depths, apparently -- JLR
									int td = tempNode.getDepth();
									prevented = (td > 0) ? td : -td;

									// The activity cannot be selected
									tempNode.setDepth(-1);
									tempNode.setSelectable(false);
								}
							}
						}
					}
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("  ::--> Completed post-1 pass");
		}

		
		// After the TOC has been created, mark activites unselectable
		// if the Choice Exit control prevents them being selected
		SeqActivity noExit = null;

		if (mSeqTree.getFirstCandidate() != null) {
			walk = mSeqTree.getFirstCandidate().getParent();
		} else {
			walk = null;
		}

		// Walk up the active path looking for a non-exiting cluster
		while (walk != null && noExit == null) {
			// We cannot choose any target that is outside of the activiy tree,
			// so choice exit does not apply to the root of the tree
			if (walk.getParent() != null) {
				if (!walk.getControlModeChoiceExit()) {
					noExit = walk;
				}
			}

			// Move up the tree
			walk = walk.getParent();
		}

		if (noExit != null) {
			depth = -1;

			if (log.isDebugEnabled()) {
				log.debug("  ::--> Found NoExit Cluster -- " + noExit.getID());
			}

			// Only descendents of this activity can be selected.
			for (int i = 0; i < nodes.size(); i++) {
				ActivityNode tempNode = nodes.elementAt(i);
				int td = tempNode.getDepth();
				
				// When we find the the 'non-exiting' activity, remember its
				// depth
				if (tempNode.getActivity().getID().equals(noExit.getID())) {
					depth = (td > 0) ? td : -td;

					// The cluster activity cannot be selected
					tempNode.setDepth(-1);
					tempNode.setSelectable(false);
				}
				// If we haven't found the the 'non-exiting' activity yet, then
				// the
				// activity being considered cannot be selected.
				else if (depth == -1) {
					tempNode.setDepth(-1);
					tempNode.setSelectable(false);
				}

				// When we back out of the depth-first-walk and encounter a
				// sibling
				// or parent of the 'non-exiting' activity, start making
				// activity
				// unselectable
				else if (((td > 0) ? td : -td) <= depth) {
					depth = -1;

					tempNode.setDepth(-1);
					tempNode.setSelectable(false);
				}
			}
		}

		// Boundary Condition -- evaluate choice exit on root
		ActivityNode tempNode = nodes.elementAt(0);
		SeqActivity root = mSeqTree.getRoot();

		if (!root.getControlModeChoiceExit()) {
			tempNode.setSelectable(false);
		}

		log.debug("  ::--> Completed second pass");
		

		// Look for constrained activities relative to the current activity and
		// mark activites unselectable if they are outside of the avaliable set
		SeqActivity con = null;

		if (mSeqTree.getFirstCandidate() != null) {
			walk = mSeqTree.getFirstCandidate().getParent();
		} else {
			walk = null;
		}

		// Walk up the tree to the root
		while (walk != null && con == null) {

			if (walk.getConstrainChoice()) {
				con = walk;
			}

			walk = walk.getParent();
		}

		// Evaluate constrained choice set
		if (con != null) {

			if (log.isDebugEnabled()) {
				log.debug("  ::-->  Constrained Choice Activity Found");
				log.debug("  ::-->  Stopped at --> " + con.getID());
			}

			int forwardAct = -1;
			int backwardAct = -1;
			Vector list = null;

			Walk walkCon = new Walk();
			walkCon.at = con;

			// Find the next activity relative to the constrained activity.
			processFlow(FLOW_FORWARD, false, walkCon, true);

			if (walkCon.at == null) {
				if (log.isDebugEnabled()) {
					log.debug("  ::--> Walked forward off the tree");
				}

				walkCon.at = con;
			}

			String lookFor = "";
			list = (Vector) walkCon.at.getChildren(false);
			if (list != null) {
				int size = list.size();
				lookFor = ((SeqActivity) list.elementAt(size - 1)).getID();
			} else {
				lookFor = walkCon.at.getID();
			}

			for (int j = 0; j < nodes.size(); j++) {
				tempNode = nodes.elementAt(j);

				if (tempNode.getActivity().getID().equals(lookFor)) {
					forwardAct = j;
					break;
				}
			}

			// Find the previous activity relative to the constrained activity.
			walkCon.at = con;
			processFlow(FLOW_BACKWARD, false, walkCon, true);

			if (walkCon.at == null) {
				log.debug("  ::--> Walked backward off the tree");
				walkCon.at = con;
			}

			lookFor = walkCon.at.getID();
			for (int j = 0; j < nodes.size(); j++) {
				tempNode = nodes.elementAt(j);

				if (tempNode.getActivity().getID().equals(lookFor)) {
					backwardAct = j;
					break;
				}
			}

			// If the forward activity on either end of the range is a cluster,
			// we need to include its descendents
			tempNode = nodes.elementAt(forwardAct);
			if (!tempNode.isLeaf()) {
				int idx = forwardAct;
				boolean foundLeaf = false;

				while (!foundLeaf) {
					for (int i = nodes.size() - 1; i > idx; i--) {
						tempNode = nodes.elementAt(i);

						if (tempNode.getParentLocation() == idx) {
							idx = i;
							foundLeaf = tempNode.isLeaf();
							break;
						}
					}
				}

				if (idx != nodes.size()) {
					forwardAct = idx;
				}
			}

			if (log.isDebugEnabled()) {
				log.debug("  ::--> Constrained Range == [ " + backwardAct + " , " + forwardAct + " ]");
			}

			// Disable activities outside of the avaliable range
			for (int i = 0; i < nodes.size(); i++) {
				tempNode = nodes.elementAt(i);

				if (i < backwardAct || i > forwardAct) {
					tempNode.setSelectable(false);

					if (log.isDebugEnabled()) {
						log.debug("  ::--> Turn off -- " + tempNode.getActivity().getID());
					}
				}
			}
		}

		log.debug("  ::--> Completed third pass");
		

		// Walk the TOC looking for disabled activities...
		if (nodes != null) {
			depth = -1;

			for (int i = 0; i < nodes.size(); i++) {
				tempNode = nodes.elementAt(i);

				if (depth != -1) {
					int td = tempNode.getDepth();
					if (depth >= ((td > 0) ? td	: -td)) {
						depth = -1;
					} else {
						tempNode.setEnabled(false);
						tempNode.setSelectable(false);
					}
				}

				if (!tempNode.isEnabled() && depth == -1) {
					int td = tempNode.getDepth();
					// Remember where the disabled activity is
					depth = (td > 0) ? td : -td;

					if (log.isDebugEnabled()) {
						log.debug("  ::--> [" + i + "]  " + "Found Disabled -->  " + tempNode.getActivity().getID() + "  <<" + tempNode.getDepth() + ">>");
					}
				}

			}
		}

		log.debug("  ::--> Completed fourth pass");

		// If there is a current activity, check availablity of its siblings
		// This pass corresponds to Case #2 of the Choice Sequencing Request
		if (nodes != null && curIdx != -1) {
			log.debug("  ::--> Checking Current Activity Siblings");
			
			int par = (nodes.elementAt(curIdx)).getParentLocation();
			int idx;

			// Check if the current activity is in a forward only cluster
			if (cur.getParent() != null
					&& cur.getParent().getControlForwardOnly()) {
				idx = curIdx - 1;

				tempNode = nodes.elementAt(idx);
				while (tempNode.getParentLocation() == par) {
					tempNode.setSelectable(false);

					idx--;
					tempNode = nodes.elementAt(idx);
				}
			}

			// Check for Stop Forward Traversal Rules
			idx = curIdx;
			boolean blocked = false;

			while (idx < nodes.size()) {
				tempNode = nodes.elementAt(idx);
				if (tempNode.getParentLocation() == par) {
					if (!blocked) {
						ISeqRuleset stopTrav = tempNode.getActivity().getPreSeqRules();

						String result = null;
						if (stopTrav != null) 
							result = stopTrav.evaluate(SeqRuleset.RULE_TYPE_FORWARDBLOCK, tempNode.getActivity(), false);

						// If the rule evaluation did not return null, the
						// activity is blocked
						blocked = (result != null);
					} else {
						tempNode.setSelectable(false);
					}
				}

				idx++;
			}
		}

		log.debug("  ::--> Completed fifth pass");

		
		// Evaluate Stop Forward Traversal Rules -- this pass cooresponds to
		// Case #3 and #5 of the Choice Sequencing Request Subprocess. In these
		// cases, we need to check if the target activity is forward in the
		// Activity Tree relative to the commen ancestor and cuurent activity
		if (nodes != null && curIdx != -1) {
			log.debug("  ::--> Checking Stop Forward Traversal");

			int curParent = (nodes.elementAt(curIdx)).getParentLocation();

			int idx = nodes.size() - 1;
			tempNode = nodes.elementAt(idx);

			// Walk backward from last available activity,
			// checking each until we get to a sibling of the current activity
			while (tempNode.getParentLocation() != -1 && tempNode.getParentLocation() != curParent) {
				tempNode = nodes.elementAt(tempNode.getParentLocation());
				ISeqRuleset stopTrav = tempNode.getActivity().getPreSeqRules();

				String result = null;
				if (stopTrav != null) {
					result = stopTrav.evaluate(SeqRuleset.RULE_TYPE_FORWARDBLOCK,
							tempNode.getActivity(), false);
				}

				// If the rule evaluation did not return null,
				// then all of its descendents are blocked
				if (result != null) {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> BLOCKED SOURCE --> "
								+ tempNode.getActivity().getID() + " [" + tempNode.getDepth() + "]");
					}

					// The depth of the blocked activity
					int blocked = tempNode.getDepth();

					for (int i = idx; i < nodes.size(); i++) {
						ActivityNode tempAN = nodes.elementAt(i);

						int td = tempAN.getDepth();
						int checkDepth = ((td >= 0) ? td : (-td));

						// Check to see if we are done blocking activities
						if (checkDepth <= blocked) {
							break;
						}

						// This activity must be a descendent
						tempAN.setSelectable(false);
					}
				}

				idx--;
				tempNode = nodes.elementAt(idx);
			}
		}
		
		log.debug("  ::--> Completed sixth pass");
		

		// Boundary condition -- if there is a TOC make sure all "selectable"
		// clusters actually flow into content
		for (int i = 0; i < nodes.size(); i++) {
			tempNode = nodes.elementAt(i);

			if (!tempNode.isLeaf()) {
				if (log.isDebugEnabled()) {
					log.debug("  ::--> Process 'Continue' request from " + tempNode.getActivity().getID());
				}

				SeqActivity from = tempNode.getActivity();

				// Confirm 'flow' is enabled from this cluster
				if (from.getControlModeFlow()) {
					// Begin traversing the activity tree from the root
					Walk treeWalk = new Walk();
					treeWalk.at = from;

					boolean success = processFlow(ADLSequencer.FLOW_FORWARD,
							true, treeWalk, false);

					if (!success) {
						tempNode.setSelectable(false);

						if (log.isDebugEnabled()) {
							log.debug("  :+: CONTINUE FAILED :+:  --> " + treeWalk.at.getID());
						}
					}
				} else {
					// Cluster does not have flow == true
					tempNode.setSelectable(false);
				}
			}
		}

		log.debug("  ::--> Completed seventh pass");

		for (int i = nodes.size() - 1; i >= 0; i--) {
			tempNode = nodes.elementAt(i);

			if (tempNode.isCurrent() && tempNode.isInChoice()) {
				if (tempNode.getDepth() < 0) {
					tempNode.setDepth(-tempNode.getDepth());
				}
			}

			if (tempNode.getDepth() >= 0) {
				while (tempNode.getParentLocation() != -1) {
					tempNode = nodes.elementAt(tempNode.getParentLocation());

					if (tempNode.getDepth() < 0) {
						tempNode.setDepth(-tempNode.getDepth());
					}
				}
			} else if (!tempNode.isHidden()) {
				tempNode.setDepth(-1);
			}
		}

		for (int i = 0; i < nodes.size(); i++) {
			tempNode = nodes.elementAt(i);

			if (tempNode.isHidden()) {
				tempNode.setDepth(-1);
				Vector parents = new Vector();

				for (int j = i + 1; j < nodes.size(); j++) {
					tempNode = nodes.elementAt(j);

					if (tempNode.getParentLocation() == i && tempNode.getDepth() > 0) {
						tempNode.setDepth(tempNode.getDepth() - 1);
						parents.add(new Integer(j));
					} else {
						if (tempNode.getDepth() != -1) {
							int idx = parents.indexOf(new Integer(tempNode.getParentLocation()));

							if (idx != -1) {
								tempNode.setDepth(tempNode.getDepth() - 1);
								parents.add(new Integer(j));
							}
						}
					}
				}
			}
		}

		log.debug("  ::--> Completed TOC walk up");

		log.debug("  :: ADLSequencer --> END   - getTOC");

		
		
		ActivityNode rootNode = null;
		
		for (int i=0;i<nodes.size();i++) {
			ActivityNode node = nodes.get(i);
			
			if (node.getParentLocation() == -1) {
				rootNode = node;
			} else if (node.getDepth() >= 0){
				ActivityNode parentNode = nodes.get(node.getParentLocation());
				parentNode.add(node);
			}
		}
		
		TreeModel treeModel = new DefaultTreeModel(rootNode);
		return treeModel;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

} // end ADLSequencer
