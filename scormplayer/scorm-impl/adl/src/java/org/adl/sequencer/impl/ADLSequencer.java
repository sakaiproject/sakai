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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import lombok.extern.slf4j.Slf4j;
import org.adl.sequencer.ADLAuxiliaryResource;
import org.adl.sequencer.ADLDuration;
import org.adl.sequencer.ADLObjStatus;
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
import org.apache.commons.lang3.StringUtils;

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
@Slf4j
public class ADLSequencer implements SeqNavigation, SeqReportActivityStatus, ISequencer {
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
	 * ADLObjStatus
	 */
	private static final long serialVersionUID = 2693585400168148602L;

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
	 * This method returns the current (as of the function call) valid Table of
	 * Contents (TOC) for the activity tree. The format of the resulting List
	 * is a result of a breadth-first walk of the activity tree.<br>
	 * <br>
	 * 
	 * @param iStart
	 *            The 'root' of the requested TOC.
	 * 
	 * @return A List of <code>ADLTOC</code> objects describing the current.
	 */
	/*private List getTOC(SeqActivity iStart) {
		log.debug("  :: ADLSequencer --> BEGIN - getTOC");

		List toc = new ArrayList();
		ADLTOC temp = null;
		boolean done = false;

		// Make sure we have an activity tree
		if (mSeqTree == null) {
			log.debug("  ::-->  No Activity Tree");

			done = true;
		}

		// Perform a breadth-first walk of the activity tree.
		SeqActivity walk = iStart;
		int depth = 0;
		int parentTOC = -1;
		List lookAt = new ArrayList();
		List flatTOC = new ArrayList();

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
			log.debug("  ::--> Building TOC from:  {}", walk.getID());
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
					log.debug("  ::--> HIDDEN");

					include = false;
					collapse = true;
				} else {
					// Check if this activity is prevented from activation
					if (walk.getPreventActivation() && !walk.getIsActive()) {
						if (cur != null) {
							if (walk != cur && cur.getParent() != walk) {								log.debug("  ::--> PREVENTED !!");
								log.debug(" {} != {}", walk.getID(), cur.getParent().getID());
								include = false;
							}
						} else {
							log.debug("  ::--> PREVENTED -- no cur");
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
					log.debug("  :: Added :: {}  [[ {} ]] ({})   //  {}  [E] {}",
                        temp.mID, temp.mDepth, temp.mParent, temp.mIsSelectable, temp.mIsEnabled);
                        
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
				log.debug("  :: Added not-included :: {} [[ {} ]] ({})   //  {}  [E] {}",
                    temp.mID, temp.mDepth, temp.mParent, temp.mIsSelectable, temp.mIsEnabled);

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
				temp = (ADLTOC) toc.get(toc.size() - 1);
				parentTOC = temp.mParent;

				while (walk == null && !done) {
					if (lookAt.size() > 0) {
						// Walk back up the tree to the parent's next sibling
						walk = (SeqActivity) lookAt
								.get(lookAt.size() - 1);
						lookAt.remove(lookAt.size() - 1);
						depth--;

						// Find the correct parent
						temp = (ADLTOC) toc.get(parentTOC);

						while (!temp.mID.equals(walk.getID())) {
							parentTOC = temp.mParent;
							temp = (ADLTOC) toc.get(parentTOC);
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
		log.debug("  ::--> Completed first pass");

		// After the TOC has been created, mark activites unselectable
		// if the Prevent Activation prevents them being selected,
		// and mark them invisible if they are descendents of a hidden
		// from choice activity
		int hidden = -1;
		int prevented = -1;

		for (int i = 0; i < toc.size(); i++) {
			SeqActivity tempAct = (SeqActivity) flatTOC.get(i);
			ADLTOC tempTOC = (ADLTOC) toc.get(i);
			log.debug("  ::--> Evaluating --> {}", tempAct.getID());
			log.debug("                   --> {}", tempTOC.mTitle);

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
					log.debug("  ::--> Hidden --> {}", tempTOC.mDepth);

					// The depth we are looking for should be positive
					hidden = -tempTOC.mDepth;
					prevented = -1;
				} else {
					log.debug("  ::--> Prevented ??{}", prevented);

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
										&& cur.getParent() != tempAct) {									System.out
											.println("  ::--> PREVENTED !!");
									log.debug(" {} != {}", tempAct.getID(), cur.getParent().getID());

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
		log.debug("  ::--> Completed post-1 pass");

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
				log.debug("  ::--> Found NoExit Cluster -- {}", noExit.getID());

			// Only descendents of this activity can be selected.
			for (int i = 0; i < toc.size(); i++) {
				temp = (ADLTOC) toc.get(i);

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
		temp = (ADLTOC) toc.get(0);
		SeqActivity root = mSeqTree.getRoot();

		if (!root.getControlModeChoiceExit()) {
			temp.mIsSelectable = false;
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
			log.debug("  ::-->  Constrained Choice Activity Found");
			log.debug("  ::-->  Stopped at --> {}", con.getID());

			int forwardAct = -1;
			int backwardAct = -1;
			List list = null;

			Walk walkCon = new Walk();
			walkCon.at = con;

			// Find the next activity relative to the constrained activity.
			processFlow(FLOW_FORWARD, false, walkCon, true);

			if (walkCon.at == null) {				
			    log.debug("  ::--> Walked forward off the tree");
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
				temp = (ADLTOC) toc.get(j);

				if (temp.mID.equals(lookFor)) {
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
			for (int j = 0; j < toc.size(); j++) {
				temp = (ADLTOC) toc.get(j);

				if (temp.mID.equals(lookFor)) {
					backwardAct = j;
					break;
				}
			}

			// If the forward activity on either end of the range is a cluster,
			// we need to include its descendents
			temp = (ADLTOC) toc.get(forwardAct);
			if (!temp.mLeaf) {
				int idx = forwardAct;
				boolean foundLeaf = false;

				while (!foundLeaf) {
					for (int i = toc.size() - 1; i > idx; i--) {
						temp = (ADLTOC) toc.get(i);

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
			log.debug("  ::--> Constrained Range == [ {} , {} ]", backwardAct, forwardAct);

			// Disable activities outside of the avaliable range
			for (int i = 0; i < toc.size(); i++) {
				temp = (ADLTOC) toc.get(i);

				if (i < backwardAct || i > forwardAct) {
					temp.mIsSelectable = false;					
					log.debug("  ::--> Turn off -- {}", temp.mID);
				}
			}
		}
		log.debug("  ::--> Completed third pass");

		// Walk the TOC looking for disabled activities...
		if (toc != null) {
			depth = -1;

			for (int i = 0; i < toc.size(); i++) {
				temp = (ADLTOC) toc.get(i);

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
					log.debug("  ::--> [{}]  Found Disabled -->  {}  <<{}>>",
						i, temp.mID, temp.mDepth);
				}
			}
		}
		log.debug("  ::--> Completed fourth pass");

		// If there is a current activity, check availablity of its siblings
		// This pass corresponds to Case #2 of the Choice Sequencing Request
		if (toc != null && curIdx != -1) {			System.out
					.println("  ::--> Checking Current Activity Siblings");

			int par = ((ADLTOC) toc.get(curIdx)).mParent;
			int idx;

			// Check if the current activity is in a forward only cluster
			if (cur.getParent() != null
					&& cur.getParent().getControlForwardOnly()) {
				idx = curIdx - 1;

				temp = (ADLTOC) toc.get(idx);
				while (temp.mParent == par) {
					temp.mIsSelectable = false;

					idx--;
					temp = (ADLTOC) toc.get(idx);
				}
			}

			// Check for Stop Forward Traversal Rules
			idx = curIdx;
			boolean blocked = false;

			while (idx < toc.size()) {
				temp = (ADLTOC) toc.get(idx);
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
		log.debug("  ::--> Completed fifth pass");

		// Evaluate Stop Forward Traversal Rules -- this pass cooresponds to
		// Case #3 and #5 of the Choice Sequencing Request Subprocess. In these
		// cases, we need to check if the target activity is forward in the
		// Activity Tree relative to the commen ancestor and cuurent activity
		if (toc != null && curIdx != -1) {
			log.debug("  ::--> Checking Stop Forward Traversal");

			int curParent = ((ADLTOC) toc.get(curIdx)).mParent;

			int idx = toc.size() - 1;
			temp = (ADLTOC) toc.get(idx);

			// Walk backward from last available activity,
			// checking each until we get to a sibling of the current activity
			while (temp.mParent != -1 && temp.mParent != curParent) {
				temp = (ADLTOC) toc.get(temp.mParent);
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
					log.debug("  ::--> BLOCKED SOURCE --> {} [{}]", temp.mID, temp.mDepth);

					// The depth of the blocked activity
					int blocked = temp.mDepth;

					for (int i = idx; i < toc.size(); i++) {
						ADLTOC tempTOC = (ADLTOC) toc.get(i);

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
				temp = (ADLTOC) toc.get(idx);
			}
		}
		log.debug("  ::--> Completed sixth pass");

		// Boundary condition -- if there is a TOC make sure all "selectable"
		// clusters actually flow into content
		for (int i = 0; i < toc.size(); i++) {
			temp = (ADLTOC) toc.get(i);

			if (!temp.mLeaf) {				System.out
						.println("  ::--> Process 'Continue' request from "
								+ temp.mID);

				SeqActivity from = getActivity(temp.mID);

				// Confirm 'flow' is enabled from this cluster
				if (from.getControlModeFlow()) {
					// Begin traversing the activity tree from the root
					Walk treeWalk = new Walk();
					treeWalk.at = from;

					boolean success = processFlow(ADLSequencer.FLOW_FORWARD,
							true, treeWalk, false);

					if (!success) {
						temp.mIsSelectable = false;						System.out
								.println("  :+: CONTINUE FAILED :+:  --> "
										+ treeWalk.at.getID());
					}
				} else {
					// Cluster does not have flow == true
					temp.mIsSelectable = false;
				}
			}
		}
		log.debug("  ::--> Completed seventh pass");

		for (int i = toc.size() - 1; i >= 0; i--) {
			temp = (ADLTOC) toc.get(i);

			if (temp.mIsCurrent && temp.mInChoice) {
				if (temp.mDepth < 0) {
					temp.mDepth = -temp.mDepth;
				}
			}

			if (temp.mDepth >= 0) {
				while (temp.mParent != -1) {
					temp = (ADLTOC) toc.get(temp.mParent);

					if (temp.mDepth < 0) {
						temp.mDepth = -temp.mDepth;
					}
				}
			} else if (temp.mIsVisible) {
				temp.mDepth = -1;
			}
		}

		for (int i = 0; i < toc.size(); i++) {
			temp = (ADLTOC) toc.get(i);

			if (!temp.mIsVisible) {
				temp.mDepth = -1;
				List parents = new ArrayList();

				for (int j = i + 1; j < toc.size(); j++) {
					temp = (ADLTOC) toc.get(j);

					if (temp.mParent == i && temp.mDepth > 0) {
						temp.mDepth--;
						parents.add(Integer.valueOf(j));
					} else {
						if (temp.mDepth != -1) {
							int idx = parents
									.indexOf(Integer.valueOf(temp.mParent));

							if (idx != -1) {
								temp.mDepth--;
								parents.add(Integer.valueOf(j));
							}
						}
					}
				}
			}
		}
		log.debug("  ::--> Completed TOC walk up");
		log.debug("  :: ADLSequencer --> END   - getTOC");

		return toc;
	}*/

	private boolean canBeIncluded(SeqActivity walk, SeqActivity cur, ActivityNode node) {

		// If we're _not_ looking at the root node, and its parent is _not_ a choice event, then quit now.
		if (walk.getParent() != null && !walk.getParent().getControlModeChoice()){
			return false;
		}

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
			if (cur == null){
				return false;
			}

			// If we're not looking at the candidate or its parent, then we don't want to include
            return walk == cur || walk == cur.getParent();
		}

		return true;
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
        log.debug("  :: ADLSequencer --> BEGIN - checkActivity");
		log.debug("  ::-->  Target: {}", iTarget.getID());

		boolean disabled = false;
		String result = null;

		// Attempt to get rule information from the activity node
		ISeqRuleset disabledRules = iTarget.getPreSeqRules();

		if (disabledRules != null) {
			result = disabledRules.evaluate(SeqRuleset.RULE_TYPE_DISABLED, iTarget, mRetry);
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
        log.debug("""
                  ::--> {}
                  :: ADLSequencer --> END   - checkActivity
                """, disabled);

        return disabled;

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
	@Override
	public void clearAttemptObjMeasure(String iID, String iObjID) {
        log.debug("""
                        :: ADLSequencer --> BEGIN - clearAttemptObjMeasure
                        ::--> Target activity: {}
                        ::--> Objective:       {}
                      """,
                iID, iObjID);

        // Find the target activty
		SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {

			// Make sure the activity is a valid target for status changes
			// -- the active leaf current activity
			if (target.getIsActive()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false) && mSeqTree.getCurrentActivity() == target) {
					boolean statusChange = target.clearObjMeasure(iObjID);

					if (statusChange) {
						target.getObjIDs(iObjID, false);

						// Revalidate the navigation requests
						validateRequests();
					}
				} else {
                    log.debug("  ::--> ERROR : Invalid target");
				}
			} else {
                log.debug("  ::--> ERROR : Target not active");
			}
		} else {
            log.debug("  ::--> ERROR : Activity does not exist");
		}
        log.debug("  :: ADLSequencer --> END   - clearAttemptObjMeasure");
	}

	/**
	 * Clear the current activity; this is done unconditionally.
	 */
	@Override
	public void clearSeqState() {
        log.debug("  ::--> Clear Session");

		SeqActivity temp = null;

		mSeqTree.setCurrentActivity(temp);
		mSeqTree.setFirstCandidate(temp);

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
        log.debug("  :: ADLSequencer --> BEGIN - clearSuspendedActivity");

		SeqActivity act = mSeqTree.getSuspendAll();

		if (iTarget == null) {
            log.debug("  ::--> Nothing to deliver");
			act = null;
		}

		if (act != null) {

			if (iTarget != act) {

				ISeqActivity common = findCommonAncestor(iTarget, act);

				while (act != common) {
					act.setIsSuspended(false);

					List<SeqActivity> children = act.getChildren(false);

					if (children != null) {
						boolean done = false;

						for (int i = 0; i < children.size() && !done; i++) {
							SeqActivity lookAt = children.get(i);

							if (lookAt.getIsSuspended()) {
								act.setIsSuspended(true);

								done = true;
							}
						}
					}

					act = act.getParent();
				}
			} else {				
                log.debug("  ::--> Target is the Suspended Act");
			}

			// Clear the suspended activity
			SeqActivity temp = null;
			mSeqTree.setSuspendAll(temp);
		} else {
            log.debug("  ::-->  Nothing to clear");
		}
        log.debug("  :: ADLSequencer --> END   - clearSuspendedActivity");
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
        log.debug("  :: ADLSequencer --> BEGIN - contentDelivery");
		log.debug("  ::-->  {}", iTarget);

		SeqActivity target = getActivity(iTarget);
		boolean done = false;

		if (target == null) {
            log.debug("  ::--> ERROR : Invalid target");

			oLaunch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
			oLaunch.mEndSession = mEndSession;

			done = true;
		}

		SeqActivity cur = mSeqTree.getFirstCandidate();

		if (cur != null && !done) {
			if (cur.getIsActive()) {
                log.debug("  ::--> ERROR : Current activity still active.");

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
			List<SeqActivity> begin = new ArrayList<>();
			SeqActivity walk = target;

			while (walk != null) {
				begin.add(walk);

				walk = walk.getParent();
			}

			if (begin.size() > 0) {

				for (int i = begin.size() - 1; i >= 0; i--) {

					walk = begin.get(i);
                    log.debug("  ::--> BEGIN >> {}", walk.getID());

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
                log.debug("  ::--> ERROR : Empty begin List");
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

			oLaunch.mNumAttempt = target.getNumAttempt() + target.getNumSCOAttempt();
			oLaunch.mMaxTime = target.getAttemptAbDur();

			// Create auxilary services List
			Hashtable<String, ADLAuxiliaryResource> services = new Hashtable<>();
			ADLAuxiliaryResource test = null;
			walk = target;

			// Starting at the target activity, walk up the tree adding services
			while (walk != null) {
				List<ADLAuxiliaryResource> curSet = walk.getAuxResources();

				if (curSet != null) {

					for (int i = 0; i < curSet.size(); i++) {
						ADLAuxiliaryResource res = null;
						res = curSet.get(i);

						// If the resource isn't already included in the set,
						// add it
						test = services.get(res.mType);

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
        log.debug("  ::--> Content Delivery Valididation");

		validateRequests();
		oLaunch.mNavState = mSeqTree.getValidRequests();

		// Make sure Continue Exit is not enabled for non-content
		if (oLaunch.mSeqNonContent != null) {
			oLaunch.mNavState.mContinueExit = false;
		}
        log.debug("  :: ADLSequencer --> END   - contentDelivery");
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Implementation of SeqReportActivityStatus

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
	private boolean doDeliveryRequest(String iTarget, boolean iTentative, ADLLaunch oLaunch) {
		// This method implements DB.1. Also, if the delivery request is not
		// tentative, it invokes the Content Delivery Environment Process.
        log.debug("""
                    :: ADLSequencer --> BEGIN - doDeliveryRequest
                    ::-->         {}
                    ::-->  REAL?  {}
                  """, iTarget, iTentative ? "NO" : "YES");

        boolean deliveryOK = true;

		// Make sure the identified activity exists in the tree.
		SeqActivity act = getActivity(iTarget);

		if (act == null) {

			// If there is no activity identified for delivery, there is nothing
			// to delivery -- indentify non-Sequenced content
			deliveryOK = false;
            log.debug("  ::--> No Delivery Request");

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
            log.debug("  ::-->  Activity is not a leaf");
		} else if (deliveryOK) {
			boolean ok = true;

			// Walk the path from the target activity to the root, checking each
			// activity.
			while (act != null && ok) {
                log.debug("  ::-->  Validating --> {}", act.getID());

				ok = !checkActivity(act);

				if (ok) {
					act = act.getParent();
				}
			}

			if (!ok) {
                log.debug("  ::-->  Some activity did not validate");

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
        log.debug("  ::-->  {}", deliveryOK);
		log.debug("  :: ADLSequencer --> END   - doDeliveryRequest");

		return deliveryOK;
	}

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
        log.debug("  :: ADLSequencer --> BEGIN - doIMSNavValidation");

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
                log.debug("  ::--> 'Start' not valid");

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
                log.debug("  ::--> 'ResumeAll' not valid");
				process = false;
			}

			break;

		case SeqNavRequests.NAV_CONTINUE:

			// Request not valid
			if (newSession) {
                log.debug("  ::--> 'Continue' not valid");
				process = false;
			} else {
				if (parent == null || !parent.getControlModeFlow()) {
                    log.debug("  ::--> 'Continue' not valid");
					process = false;

				}
			}

			break;

		case SeqNavRequests.NAV_PREVIOUS:

			if (newSession) {
                log.debug("  ::--> 'Previous' not valid");
				process = false;
			} else {
				if (parent != null) {
					if (!parent.getControlModeFlow() || parent.getControlForwardOnly()) {
                        log.debug("  ::--> 'Previous' not valid -- Control Mode");
						process = false;
					}
				} else {
                    log.debug("  ::--> 'Previous' not valid -- NULL Parent");
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
                log.debug("  ::--> 'Abandon' not valid");
				process = false;
			}
			break;

		case SeqNavRequests.NAV_ABANDONALL:

			if (newSession) {
                log.debug("  ::--> 'AbandonAll' not valid");
				process = false;
			}
			break;

		case SeqNavRequests.NAV_SUSPENDALL:

			if (newSession) {
                log.debug("  ::--> 'Abandon' not valid");
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
                log.debug("  ::--> 'Exit' not valid");
				process = false;
			}
			break;

		case SeqNavRequests.NAV_EXITALL:

			if (newSession) {
                log.debug("  ::--> 'ExitAll' not valid");
				process = false;
			}
			break;

		default:
            log.debug("  ::--> Invalid navigation request: {}", iRequest);
			process = false;
		}
        log.debug("  ::--> {}", process);
		log.debug("  :: ADLSequencer --> END   - doIMSNavValidation");

		return process;
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
	private void doOverallRollup(SeqActivity ioTarget, Hashtable<String, Integer> ioRollupSet) {

		// This method implements the loop of RB.1.5. The other rollup process
		// are encapsulated in the RollupRuleset object.
        log.debug("  :: ADLSequencer --> BEGIN - doOverallRollup");
		log.debug("  ::-->  Target: {}", ioTarget.getID());

		// Attempt to get Rollup Rule information from the activity node
		ISeqRollupRuleset rollupRules = ioTarget.getRollupRules();

		if (rollupRules == null) {
			rollupRules = new SeqRollupRuleset();
		}

		// Apply the rollup processes to the activity
		rollupRules.evaluate(ioTarget);		boolean objMeasureStatus = ioTarget.getObjMeasureStatus(false);
		double objMeasure = ioTarget.getObjMeasure(false);

		boolean objStatus = ioTarget.getObjStatus(false);
		boolean objSatisfied = ioTarget.getObjSatisfied(false);

		boolean proStatus = ioTarget.getProgressStatus(false);
		boolean proCompleted = ioTarget.getAttemptCompleted(false);

        log.debug("""
                    ::--> RESULTS
                    :: OBJ Measure ::   {} // {}
                    :: OBJ Status  ::   {} // {}
                    :: Progress    ::   {} // {}
                  """,
                objMeasureStatus, objMeasure, objStatus, objSatisfied, proStatus, proCompleted);

        // Remove this activity from the rollup set
		ioRollupSet.remove(ioTarget.getID());
        log.debug("  :: ADLSequencer --> END   - doOverallRollup");
	}

	/**
	 * Reorder the children of a cluster to be considered for sequencing.
	 * 
	 * @param ioCluster
	 *            Cluster to be prepared.
	 */
	private void doRandomize(SeqActivity ioCluster) {
        log.debug("  :: ADLSequencer --> BEGIN - doRandomize");
		log.debug("  ::-->  Target: {}", ioCluster.getID());

		// Make sure this is a cluster
		if (ioCluster.getChildren(true) != null) {

			Random gen = new Random();
			List<SeqActivity> all = ioCluster.getChildren(false);
            log.debug("  ::--> Cluster has '{}' children to randomize", all.size());

			List<Integer> set = null;

			boolean ok = false;
			int rand = 0;
			int num = 0;
			int lookUp = 0;

			// Reorder the 'selected' child set if neccessary
			if (ioCluster.getReorderChildren()) {
				List<SeqActivity> reorder = new ArrayList<>();
				set = new ArrayList<>();

				for( SeqActivity all1 : all )
				{
					// Pick an unselected child
					ok = false;
					while (!ok) {
						rand = gen.nextInt();
						num = Math.abs(rand % all.size());

						lookUp = set.indexOf(num);

						if (lookUp == -1) {
							set.add(num);
							reorder.add(all.get(num));
                            log.debug("  ::--> PLACED --> {}", num);

							ok = true;
						}
					}
				}

				// Assign the current set of active children to this cluster
				ioCluster.setChildren(reorder, false);

			} else {
                log.debug("  ::--> Don't Reorder");
			}
		} else {
            log.debug("  ::--> Not A Cluster");
		}
        log.debug("  :: ADLSequencer --> END   - doRandomize");
	}

	/**
	 * Prepare the children of a cluster to be considered for sequencing.
	 * 
	 * @param ioCluster
	 *            Cluster to be prepared.
	 */
	private void doSelection(SeqActivity ioCluster) {
        log.debug("  :: ADLSequencer --> BEGIN - doSelection");
		log.debug("  ::-->  Target: {}", ioCluster.getID());

		// Make sure this is a cluster
		if (ioCluster.getChildren(true) != null) {

			Random gen = new Random();

			int count = ioCluster.getSelectCount();
			List<SeqActivity> all = ioCluster.getChildren(true);
            log.debug("  ::--> Cluster has '{}' children", all.size());

			List<SeqActivity> children = null;
			List<Integer> set = null;

			boolean ok = false;
			int rand = 0;
			int num = 0;
			int lookUp = 0;

			// First select the Select Count number of children
			if (count > 0) {
				// Check to see if the count exceeds the number of children
				if (count < all.size()) {
                    log.debug("  ::--> Selecting --> {}", count);

					// Select count activities from the set of children
					children = new ArrayList<>();
					set = new ArrayList<>();

					while (set.size() < count) {
						// Find an unselected child of the cluster
						ok = false;
						while (!ok) {
							rand = gen.nextInt();
							num = Math.abs(rand % all.size());

							lookUp = set.indexOf(num);

							if (lookUp == -1) {
								set.add(num);
								ok = true;
                                log.debug("  ::--> ADDED --> {}", num);
							}
						}
					}

					// Create the selected child List
					for (int i = 0; i < all.size(); i++) {
						lookUp = set.indexOf(i);

						if (lookUp != -1) {
							children.add(all.get(i));
						}
					}

					// Assign the selected set of children to the cluster
					ioCluster.setChildren(children, false);

				} else {
                    log.debug("  ::--> All Children Selected");
				}
			} else {
                log.debug("  ::--> No Children Selected");
			}
		} else {
            log.debug("  ::--> Not A Cluster");
		}
        log.debug("  :: ADLSequencer --> END   - doSelection");
	}

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
        log.debug("  :: ADLSequencer --> BEGIN - doSequencingRequest");
		log.debug("  ::-->  {}",  iRequest);

		String delReq = null;

		// Clear global state
		mEndSession = false;

		// All sequencing requests are processed from the First Candidate
		SeqActivity from = mSeqTree.getFirstCandidate();

		if (iRequest.equals(ADLSequencer.SEQ_START)) {
			// This block implements the Start Sequencing Request Process (SB.2.
            log.debug("  ::--> Process 'Start' request.");

			// Make sure this request will begin a new session
			if (from == null) {
				// Begin traversing the activity tree from the root
				Walk walk = new Walk();
				walk.at = mSeqTree.getRoot();

				boolean success = processFlow(ADLSequencer.FLOW_FORWARD, true, walk, false);

				if (success) {
					// Delivery request is where flow stopped.
					delReq = walk.at.getID();
				}
			} else {
                log.debug("  ::--> Session already begun");
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_RESUMEALL)) {
			// This block implements the Resume All Sequencing Request Process
			// (SB.2.6)
            log.debug("  ::--> Process 'Resume All' request.");

			// Make sure this request will begin a new session
			if (from == null) {
				SeqActivity resume = mSeqTree.getSuspendAll();

				if (resume != null) {
					delReq = resume.getID();
				} else {
                    log.debug("  ::--> No suspended activity");
				}
			} else {
                log.debug("  ::--> Session already begun");
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_CONTINUE)) {
			// This block implements the Continue Sequencing Request Process
			// (SB.2.7)
            log.debug("  ::--> Process 'Continue' request.");

			// Make sure the session has already started
			if (from != null) {
				// Confirm 'flow' is enabled
				SeqActivity parent = from.getParent();
				if (parent == null || parent.getControlModeFlow()) {

					// Begin traversing the activity tree from the root
					Walk walk = new Walk();
					walk.at = from;

					boolean success = processFlow(ADLSequencer.FLOW_FORWARD, false, walk, false);

					if (success) {
						// Delivery request is where flow stopped.
						delReq = walk.at.getID();
					} else {
                        log.debug("  :+: CONTINUE FAILED :+:  --> {}", walk.at.getID());
					}
				}
			} else {
                log.debug("  ::--> Session hasn't begun");
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_EXIT)) {
			// This block implements the Exit Sequencing Request Process
			// (SB.2.11)
            log.debug("  ::--> Process 'Exit' request.");

			// Make sure the session has already started
			if (from != null) {
				if (!from.getIsActive()) {
					ISeqActivity parent = from.getParent();

					if (parent == null) {
						// The sequencing session is over -- set global state
						mEndSession = true;
					}
				} else {
                    log.debug("  ::--> Activity is still active");
				}
			} else {
                log.debug("  ::--> Session hasn't begun");
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_PREVIOUS)) {
			// This block implements the Previous Sequencing Request Process
			// (SB.2.8)
            log.debug("  ::--> Process 'Previous' request.");

			// Make sure the session has already started
			if (from != null) {
				// Confirm 'flow' is enabled
				SeqActivity parent = from.getParent();
				if (parent == null || parent.getControlModeFlow()) {
					// Begin traversing the activity tree from the root
					Walk walk = new Walk();
					walk.at = from;

					boolean success = processFlow(ADLSequencer.FLOW_BACKWARD, false, walk, false);

					if (success) {
						// Delivery request is where flow stopped.
						delReq = walk.at.getID();
					}
				}
			} else {
                log.debug("  ::--> Session hasn't begun");
			}
		} else if (iRequest.equals(ADLSequencer.SEQ_RETRY)) {
			// This block implements the Retry Sequencing Request Process
			// (SB.2.10)
            log.debug("  ::--> Process 'Retry' request.");

			// Make sure the session has already started
			if (from != null) {
				if (mExitAll || (!(from.getIsActive() || from.getIsSuspended()))) {
					if (from.getChildren(false) != null) {
						Walk walk = new Walk();
						walk.at = from;

						// Set 'Retry' flag
						setRetry(true);

						boolean success = processFlow(ADLSequencer.FLOW_FORWARD, true, walk, false);

						// Reset 'Retry' flag
						setRetry(false);

						if (success) {
							delReq = walk.at.getID();
						}
					} else {
						delReq = from.getID();
					}
				} else {
                    log.debug("  ::--> Activity is active or suspended");
				}
			} else {
                log.debug("  ::--> Session hasn't begun");
			}
		} else {
			// This block implements the Choice Sequencing Request Process
			// (SB.2)
            log.debug("  ::--> Process 'Choice' request.");

			// The sequencing request identifies the target activity
			SeqActivity target = getActivity(iRequest);

			if (target != null) {
				boolean process = true;
				SeqActivity parent = target.getParent();

				// Check if the activity should be considered.
				if (!target.getIsSelected()) {
					// Exception SB.2.9-2
					process = false;
                    log.debug("  ::--> Activity not in parent's set of avaliable children");
				}

				if (process) {
					SeqActivity walk = target.getParent();

					// Walk up the tree evaluating 'Hide from Choice' rules.
					while (walk != null) {
						// Attempt to get rule information from the activity
						ISeqRuleset hideRules = walk.getPreSeqRules();

						String result = null;

						if (hideRules != null) {
							result = hideRules.evaluate(SeqRuleset.RULE_TYPE_HIDDEN, walk, false);
						}

						// If the rule evaluation did not return null, the
						// activity
						// must be hidden.
						if (result != null) {
							// Exception SB.2.9-3
							walk = null;
							process = false;							
                            log.debug("  ::--> Activity hidden");
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
                            log.debug("  ::--> Invalid control mode");
						}
					}
				}

				SeqActivity common = mSeqTree.getRoot();

				if (process) {
                    if (from != null) {
                        common = findCommonAncestor(from, target);

                        if (common == null) {
                            process = false;
                            log.debug("  ::-->  ERROR : Invalid ancestor");
                        }
                    } else {
                        // If the sequencing session has not begun, start at the
                        // root
                        from = common;
                    }
                    log.debug("""
                                      :: CHOICE PROCESS ::
                                      ::  F : {}
                                      ::  T : {}
                                      ::  C : {}
                                    """,
                            from != null ? from.getID() : "NULL",
                            target.getID(),
                            common != null ? common.getID() : "NULL");
                }

                // Choice Case #1 -- The current activity was selected
                if (from == target) {
                    log.debug("  ::-->  Choice Case #1");
                    // Nothing more to do...
                } else if (from != null && from.getParent() == target.getParent()) {
                    // Choice Case #2 -- The current activity and target are in
                    // the
                    // same cluster
                    log.debug("  ::-->  Choice Case #2");
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
                } else if (from == common) {
                    // Choice Case #3 -- Path to the target is forward in the tree
                    log.debug("  ::-->  Choice Case #3");
                    SeqActivity walk = target.getParent();

                    while (walk != from && process) {
                        process = evaluateChoiceTraversal(ADLSequencer.FLOW_FORWARD, walk);

                        // Test prevent Activation
                        if (process) {
                            if (!walk.getIsActive() && walk.getPreventActivation()) {
                                // Exception 2.9-6
                                process = false;
                                continue;
                            }
                        }

                        walk = walk.getParent();
                    }

                    // Evaluate at the common ancestor
                    if (process) {
                        process = evaluateChoiceTraversal(ADLSequencer.FLOW_FORWARD, walk);
                    }
                } else if (target == common) {
                    // Choice Case #4 -- Path to target is backward in the tree
                    log.debug("  ::-->  Choice Case #4");

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
                } else {
                    // Choice Case #5 -- Target is a descendent of the ancestor
                    log.debug("  ::-->  Choice Case #5");

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

                        log.debug("  ::-->  Constrained Choice Eval");
                        log.debug("  ::-->  Stopped at --> {}", walkCon.at.getID());

                        if (target.getParent() != walkCon.at && target != walkCon.at) {
                            // Exception SB.2.9-8
                            process = false;
                        }
                    }

                    // Walk down the tree to the target
                    walk = target.getParent();

                    while (walk != common && process) {
                        process = evaluateChoiceTraversal(ADLSequencer.FLOW_FORWARD, walk);

                        // Test prevent Activation
                        if (process) {
                            if (!walk.getIsActive() && walk.getPreventActivation()) {
                                // Exception 2.9-6
                                process = false;

                                continue;
                            }
                        }

                        walk = walk.getParent();
                    }

                    // Evaluate the common ancestor
                    if (process) {
                        process = evaluateChoiceTraversal(ADLSequencer.FLOW_FORWARD, walk);
                    }
                }

                // Did we reach the target successfully?
                if (process) {
                    // Is the target a cluster
                    if (target.getChildren(false) != null) {
                        Walk walk = new Walk();
                        walk.at = target;

                        boolean success = processFlow(ADLSequencer.FLOW_FORWARD, true, walk, false);

                        if (success) {
                            delReq = walk.at.getID();
                        } else {
                            log.debug("""
                                        ::--> Failed to find leaf
                                        ::--> Moving Current Activity
                                        ::--> {}
                                      """, common == null ? "NULL" : common.getID());

                            if (mSeqTree.getCurrentActivity() != null && common != null) {
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
            } else {
				// Exception SB.2.9-1
                log.debug("  ::-->  Target does not exist in the tree");
			}
		}
        log.debug("""
                    ::-->  {}
                    ::-->  {}
                    :: ADLSequencer --> END   - doSequencingRequest
                  """, delReq, mEndSession || mExitCourse);
        return delReq;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	 
	 Implementation of SeqNavigation 
	 
	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

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
            log.debug("""
                      :: ADLSequencer --> BEGIN - doTerminationRequest
                      ::--> Request:  {}
                      ::--> Real?     {}
                    """, iRequest, iTentative ? "NO" : "YES");
        // The Termination Request Process may return a sequencing request
		String seqReq = null;
		mExitAll = false;

		// Ensure the request exists
		if (iRequest == null) {

			mValidTermination = false;
            log.debug("""
                        ::--> NULL request
                        :: ADLSequencer --> END   - doTerminationRequest
                      """);

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
            log.debug("""
                        ::--> No current activity
                        :: ADLSequencer --> END   - doTerminationRequest
                      """);
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
                        log.debug("  ::--> Evaluating 'POST' at -- {}", process.getID());

						// Attempt to get rule information from the activity
						ISeqRuleset postRules = process.getPostSeqRules();

						if (postRules != null) {
							String result = null;
							result = postRules.evaluate(SeqRuleset.RULE_TYPE_POST, process, false);

							if (result != null) {
                                log.debug("  ::--> {}", result);

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
								} else if (result.equals(SeqRule.SEQ_ACTION_CONTINUE)) {
									// Override any existing sequencing request
									seqReq = ADLSequencer.SEQ_CONTINUE;
								} else if (result.equals(SeqRule.SEQ_ACTION_PREVIOUS)) {
									// Override any existing sequencing request
									seqReq = ADLSequencer.SEQ_PREVIOUS;
								} else if (result.equals(SeqRule.SEQ_ACTION_EXITALL)) {
									// Break from the current loop and jump to
									// the
									// next case
									iRequest = ADLSequencer.TER_EXITALL;
								} else if (result.equals(SeqRule.SEQ_ACTION_EXITPARENT)) {
									process = process.getParent();

									if (process == null) {
                                        log.debug("  ::--> ERROR ::  No parent to exit");
									} else {

										mSeqTree.setFirstCandidate(process);
										endAttempt(process, iTentative);

										exited = true;
									}
								} else if (result.equals(SeqRule.SEQ_ACTION_RETRYALL)) {
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
                                log.debug("  ::--> NULL Evaluation");
							}
						} else if (process == mSeqTree.getRoot()) {
							// Exited Root with no postcondition rules
							// End the Course
							mExitCourse = true;
						}
					} else {
                        log.debug("  --> Exited Course");
						seqReq = ADLSequencer.SEQ_EXIT;
					}
				} while (exited);
			} else {
                log.debug("  ::--> INVALID :: activity inactive");
				mValidTermination = false;
			}
		}

		// Double check for an EXIT request
		if (iRequest.equals(ADLSequencer.TER_EXIT)) {
			// Already handled
		} else if (iRequest.equals(ADLSequencer.TER_EXITALL)) {
            log.debug("  ::--> Processing EXIT ALL");

			// Don't modify the activity tree if this is only a tentative exit
			if (!iTentative) {
				SeqActivity process = mSeqTree.getFirstCandidate();

				if (process.getIsActive()) {
					endAttempt(process, false);
				}

				terminateDescendentAttempts(mSeqTree.getRoot());

				endAttempt(mSeqTree.getRoot(), false);

				// only exit if we're not retrying the root
				if (!StringUtils.equals(seqReq, ADLSequencer.SEQ_RETRY)) {
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
                log.debug("  --> CLEARING STATE ABANDON");

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
                log.debug("  --> CLEARING STATE ABANDONALL");

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
            log.debug("  ::--> INVALID :: invalid request");

			mValidTermination = false;
		}

		// If this was a 'real' termination request, move the current activity
		if (!iTentative) {
			mSeqTree.setCurrentActivity(mSeqTree.getFirstCandidate());
		}

		String tmpID = mSeqTree.getFirstCandidate().getID();
        log.debug("""
                    ::--> SEQ REQ      :: {}
                    ::--> FIRST        :: {}
                    ::--> EXIT COURSE  :: {}
                    :: ADLSequencer --> END   - doTerminationRequest
                  """, seqReq, tmpID, mExitCourse);
		return seqReq;
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
        log.debug("  :: ADLSequencer --> BEGIN - endAttempt");

        if (iTarget != null) {
            log.debug("  ::--> Target : {}", iTarget.getID());
        } else {
            log.debug("  ::--> ERROR : NULL Activity");
        }

        log.debug("  ::--> REAL?    -- {}", ((iTentative) ? "NO" : "YES"));

		if (iTarget != null) {
			List<SeqActivity> children = iTarget.getChildren(false);

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
							iTarget.setObjSatisfied(ADLTracking.TRACK_SATISFIED);
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
						SeqActivity act = children.get(i);

						if (act.getIsSuspended()) {
							iTarget.setIsSuspended(true);
							break;
						}
					}

					// If the cluster is not suspended check for selection and
					// randomization
					if (!iTarget.getIsSuspended()) {
						if (iTarget.getSelectionTiming().equals(SeqActivity.TIMING_EACHNEW)) {
							doSelection(iTarget);
							iTarget.setSelection(true);
						}

						if (iTarget.getRandomTiming().equals(SeqActivity.TIMING_EACHNEW)) {
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
        log.debug("  :: ADLSequencer --> END   - endAttempt");
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Private Methods

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

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
        log.debug("  :: ADLSequencer --> BEGIN - evaluateChoiceTraversal");
		log.debug("  ::-->  {}", iDirection);

        if (iAt != null) {
            log.debug("  ::-->  {}", iAt.getID());
        } else {
            log.debug("  ::-->  ERROR : NULL starting point");
        }

        boolean success = true;

		// Make sure we have somewhere to start from
		if (iAt != null) {
			if (iDirection == ADLSequencer.FLOW_FORWARD) {
				// Attempt to get rule information from the activity node
				ISeqRuleset stopTrav = iAt.getPreSeqRules();
				String result = null;

				if (stopTrav != null) {
					result = stopTrav.evaluate(SeqRuleset.RULE_TYPE_FORWARDBLOCK, iAt, false);
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
                log.debug("  ::--> ERROR : Invalid direction");
				success = false;
			}
		} else {
			success = false;
		}
        log.debug("  ::-->  {}", success);
		log.debug("  :: ADLSequencer --> END   - " + "evaluateChoiceTraversal");

		return success;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Navigation Behavior

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
        log.debug("  :: ADLSequencer --> BEGIN - evaluateExitRules");
		log.debug("  ::--> REAL? -- " + ((iTentative) ? "NO" : "YES"));

		// Clear global state
		mExitCourse = false;

		// Always begin processing at the current activity
		SeqActivity start = mSeqTree.getCurrentActivity();

		SeqActivity exitAt = null;
		String exited = null;

		List<SeqActivity> path = new ArrayList<>();

		if (start != null) {
			SeqActivity parent = start.getParent();

			while (parent != null) {
                log.debug("  ::--> Adding :: {}", parent.getID());

				path.add(parent);

				parent = parent.getParent();
			}

			// Starting at the root, walk down the tree to the current activity'
			// parent.
			while (path.size() > 0 && (exited == null)) {
				parent = path.get(path.size() - 1);
				path.remove(path.size() - 1);
                log.debug("  ::--> Evaluating 'Exit' at -- {}", parent.getID());

				// Attempt to get rule information from the activity node
				ISeqRuleset exitRules = parent.getExitSeqRules();

				if (exitRules != null) {
					exited = exitRules.evaluate(SeqRuleset.RULE_TYPE_EXIT, parent, false);
				}

				// If the rule evaluation did not return null, the activity must
				// have exited.
				if (exited != null) {
					exitAt = parent;
				}
			}

			if (exited != null) {
				if (exitAt == mSeqTree.getRoot()) {
                    log.debug("  ::--> ROOT   <<< Exited");
				} else {
                    log.debug("  ::--> {}  <<< Exited", exitAt.getID());
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
            log.debug("  ::--> ERROR : NULL Current Activity");
		}
        log.debug("  :: ADLSequencer --> END   - evaluateExitRules");
	}

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
        log.debug("  :: ADLSequencer --> BEGIN - evaluateLimitConditions");
		log.debug("  ::-->  Target: {}", iTarget.getID());

		// For 2, we only test max attempt limits
		// Need to add all limit condition tests...

		boolean disabled = false;

		// Only test limitConditions if the activity is not active
		if (!iTarget.getIsActive() && !iTarget.getIsSuspended()) {
			if (iTarget.getAttemptLimitControl()) {
				disabled = iTarget.getNumAttempt() >= iTarget.getAttemptLimit();
			}
		}
        log.debug("  ::-->  {}", disabled);
		log.debug("  :: ADLSequencer --> END   - evaluateLimitConditions");

		return disabled;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Termination Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

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
        log.debug("  :: ADLSequencer --> BEGIN - findCommonAncestor");

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
        log.debug("""
                    ::-->  {}
                    :: ADLSequencer --> END   - findCommonAncestor
                  """, ancestor != null ? ancestor.getID() : "NULL");
		return ancestor;
	}

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
        log.debug("""
                    :: ADLSequencer --> BEGIN - getActivity
                    ::-->  {}
                  """, iActivityID);

        SeqActivity thisActivity = null;

        // Get an activity node from the activity tree based on its ID
		if (mSeqTree != null) {
			thisActivity = mSeqTree.getActivity(iActivityID);
            log.debug("  ::-->  FOUND");
		} else {
            log.debug("  ::--> ERROR : No Activity Tree");
		}
        
        log.debug("  :: ADLSequencer --> END   - getActivity");

		return thisActivity;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Rollup Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Gets the current active activity tree.
	 * 
	 * @return The current activity tree (<code>SeqActivityTree</code>).
	 */
	@Override
	public ISeqActivityTree getActivityTree() {
        log.debug("  :: ADLSequencer --> BEGIN - getActivityTree");
		log.debug("  :: ADLSequencer --> END   - getActivityTree");

		return mSeqTree;
	}

	/**
	 * Displays the values of the <code>ADLTOC</code> objects that constitute
	 * table of contents. This method is used for diagnostic purposes.
	 * 
	 * @param iOldTOC
	 *            A List of <code>ADLTOC</code> objects describing the
	 *            'first pass' TOC.
	 * 
	 * @param oNewTOC
	 *            A List of <code>ADLTOC</code> objects describing the
	 *            'final pass' TOC.
	 * 
	 * @return The set of valid activity IDs for 'Choice' navigation requests.
	 */
	private Hashtable<String, ActivityNode> getChoiceSet(TreeModel treeModel) {
		Hashtable<String, ActivityNode> set = null;
		String lastLeaf = null;

		if (treeModel != null) {
			ActivityNode tempNode = null;
			set = new Hashtable<>();

			ActivityNode rootNode = (ActivityNode) treeModel.getRoot();

			if (rootNode != null) {
				@SuppressWarnings("unchecked")
				Enumeration<TreeNode> breadthFirst = rootNode.breadthFirstEnumeration();

				List<TreeNode> bfList = Collections.list(breadthFirst);

				// Traverse the breadth-first search backwards
				for (int i = bfList.size() - 1; i > 0; i--) {
					tempNode = (ActivityNode) bfList.get(i);

					if (tempNode.getDepth() == -1) {
						if (tempNode.isSelectable()) {
							set.put(tempNode.getActivity().getID(), tempNode);
						}
					} else if (!tempNode.isHidden()) {
						set.put(tempNode.getActivity().getID(), tempNode);
					}

					if (lastLeaf == null) {
						if (tempNode.isLeaf() && tempNode.isEnabled()) {
							lastLeaf = tempNode.getActivity().getID();
						}
					}

				}
			}
		}

		if (lastLeaf != null) {
            log.debug("  ::--> Setting last leaf --> {}", lastLeaf);
			mSeqTree.setLastLeaf(lastLeaf);
		}

		// If there are no items in the set, there is no TOC.
		if (set != null && set.isEmpty()) {
			set = null;
		}

		// TODO: JLR -- think we might be able to live without this... 9/10/2007

		// If there is only one item in the set, it must be the root -- remove
		// it
		// If there is only one item in the set, it is the parent of a
		// choiceExit == false cluster, it cannot be selected -- no TOC
		/*if (oNewTOC.size() == 1) {
			ADLTOC temp = (ADLTOC) oNewTOC.get(0);

			if (!temp.mIsEnabled) {
				log.debug("  ::--> Clearing single non-enabled activity");
				oNewTOC.remove(0);
			} else if (!temp.mLeaf) {
				log.debug("  ::--> Clearing root activity");
				oNewTOC.remove(0);
			}
		}*/

		return set;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Selection and Randomization Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Retrieves the set of objective status records associated with an
	 * activity.
	 * 
	 * @param iActivityID
	 *            The ID of the activity whose objectives are requested.
	 * 
	 * @return A <code>List</code> of <code>ADLObjStatus</code> objects
	 *         for the requested activity or <code>null</code> if none are
	 *         defined.
	 */
	@Override
	public List<ADLObjStatus> getObjStatusSet(String iActivityID) {
        log.debug("  :: ADLSequencer --> BEGIN - getObjStatusSet");

		List<ADLObjStatus> objSet = null;
		SeqActivity act = getActivity(iActivityID);

		// Make sure the activity exists
		if (act != null) {
			// Ask the activity for its current set of objective status records
			objSet = act.getObjStatusSet();
		} else {
            log.debug("  ::-->  Activity not found");
		}
        if (objSet == null) {
			log.debug("  ::-->  NULL");
        } else {
            log.debug("  ::-->  [ {} ]", objSet.size());
        }
        log.debug("  :: ADLSequencer --> END   - getObjStatusSet");

		return objSet;
	}

	/**
	 * Gets the root of the current activity tree.
	 * 
	 * @return The root activity of the current activity tree (<code>SeqActivity</code>).
	 */
	@Override
	public ISeqActivity getRoot() {
		ISeqActivity rootActivity = null;
		if (mSeqTree != null) {
			rootActivity = mSeqTree.getRoot();
		}
		return rootActivity;
	}

	/**
	 * Method added 8/24/2007 JLR
	 * 
	 * This is an attempt to more efficiently generate the TreeModel object that we pass around
	 * instead of the ADLTOC List that ADL uses in their reference implementation.
	 * 
	 */
	private DefaultTreeModel getTreeModel(SeqActivity iStart) {

		List<ActivityNode> nodes = new ArrayList<>();

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
		List<SeqActivity> lookAt = new ArrayList<>();
		List<SeqActivity> flatTOC = new ArrayList<>();

		// Tree traversal status indicators
		boolean next = false;

		// Make sure the activity has been associated with this sequencer
		// If not, build the TOC from the root
		if (walk == null) {
			walk = mSeqTree.getRoot();
		}

		if (!done) {
            log.debug("  ::--> Building TOC from:  {}", walk.getID());
		}

		SeqActivity cur = (mSeqTree != null ? mSeqTree.getFirstCandidate() : null);
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
				if (parent != null) {
					node.setInChoice(parent.getControlModeChoice());
				} else {
					node.setInChoice(true);
				}

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
				walk = (walk.getChildren(false)).get(0);
				parentTOC = nodes.size() - 1;
				depth++;

				next = true;
			}

			if (!next) {
				// Move to its sibling
				walk = walk.getNextSibling(false);

				ActivityNode tempNode = nodes.get(nodes.size() - 1);
				parentTOC = tempNode.getParentLocation();

				while (walk == null && !done) {
					if (lookAt.size() > 0) {
						// Walk back up the tree to the parent's next sibling
						walk = lookAt.get(lookAt.size() - 1);
						lookAt.remove(lookAt.size() - 1);
						depth--;

						// Find the correct parent
						tempNode = nodes.get(parentTOC);

						String tempNodeId = tempNode.getActivity().getID();
						walk.getID();

						while (!tempNodeId.equals(walk.getID())) {
							parentTOC = tempNode.getParentLocation();
							tempNode = nodes.get(parentTOC);
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
			SeqActivity tempAct = flatTOC.get(i);
			ActivityNode tempNode = nodes.get(i);

            log.debug("""
                        ::--> Evaluating --> {}
                                         --> {}
                      """, tempAct.getID(), tempAct.getTitle());

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
					if (log.isDebugEnabled()) {
						log.debug("  ::--> Prevented ??{}", prevented);
					}

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
										log.debug(" {} != {}", tempAct.getID(), cur.getParent().getID());
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

        log.debug("  ::--> Completed post-1 pass");

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

            log.debug("  ::--> Found NoExit Cluster -- {}", noExit.getID());

			// Only descendents of this activity can be selected.
			for (int i = 0; i < nodes.size(); i++) {
				ActivityNode tempNode = nodes.get(i);
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
		ActivityNode tempNode = nodes.get(0);
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

            log.debug("""
                        ::-->  Constrained Choice Activity Found
                        ::-->  Stopped at --> {}
                      """, con.getID());

			int forwardAct = -1;
			int backwardAct = -1;
			List<SeqActivity> list = null;

			Walk walkCon = new Walk();
			walkCon.at = con;

			// Find the next activity relative to the constrained activity.
			processFlow(FLOW_FORWARD, false, walkCon, true);

			if (walkCon.at == null) {
                log.debug("  ::--> Walked forward off the tree");
				walkCon.at = con;
			}

			String lookFor = "";
			list = walkCon.at.getChildren(false);
			if (list != null) {
				int size = list.size();
				lookFor = list.get(size - 1).getID();
			} else {
				lookFor = walkCon.at.getID();
			}

			for (int j = 0; j < nodes.size(); j++) {
				tempNode = nodes.get(j);

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
				tempNode = nodes.get(j);

				if (tempNode.getActivity().getID().equals(lookFor)) {
					backwardAct = j;
					break;
				}
			}

			// If the forward activity on either end of the range is a cluster,
			// we need to include its descendents
			tempNode = nodes.get(forwardAct);
			if (!tempNode.isLeaf()) {
				int idx = forwardAct;
				boolean foundLeaf = false;

				while (!foundLeaf) {
					for (int i = nodes.size() - 1; i > idx; i--) {
						tempNode = nodes.get(i);

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

            log.debug("  ::--> Constrained Range == [ {} , {} ]", backwardAct, forwardAct);

			// Disable activities outside of the avaliable range
			for (int i = 0; i < nodes.size(); i++) {
				tempNode = nodes.get(i);

				if (i < backwardAct || i > forwardAct) {
					tempNode.setSelectable(false);
                    log.debug("  ::--> Turn off -- {}", tempNode.getActivity().getID());
				}
			}
		}

		log.debug("  ::--> Completed third pass");

		// Walk the TOC looking for disabled activities...
		if (nodes != null) {
			depth = -1;

			for (int i = 0; i < nodes.size(); i++) {
				tempNode = nodes.get(i);

				if (depth != -1) {
					int td = tempNode.getDepth();
					if (depth >= ((td > 0) ? td : -td)) {
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
						log.debug("  ::--> [{}]  Found Disabled -->  {}  <<{}>>", i, tempNode.getActivity().getID(), tempNode.getDepth());
					}
				}

			}
		}

		log.debug("  ::--> Completed fourth pass");

		// If there is a current activity, check availablity of its siblings
		// This pass corresponds to Case #2 of the Choice Sequencing Request
		if (nodes != null && curIdx != -1) {
			log.debug("  ::--> Checking Current Activity Siblings");

			int par = (nodes.get(curIdx)).getParentLocation();
			int idx;

			// Check if the current activity is in a forward only cluster
			if (cur.getParent() != null && cur.getParent().getControlForwardOnly()) {
				idx = curIdx - 1;

				tempNode = nodes.get(idx);
				while (tempNode.getParentLocation() == par) {
					tempNode.setSelectable(false);

					idx--;
					tempNode = nodes.get(idx);
				}
			}

			// Check for Stop Forward Traversal Rules
			idx = curIdx;
			boolean blocked = false;

			while (idx < nodes.size()) {
				tempNode = nodes.get(idx);
				if (tempNode.getParentLocation() == par) {
					if (!blocked) {
						ISeqRuleset stopTrav = tempNode.getActivity().getPreSeqRules();

						String result = null;
						if (stopTrav != null) {
							result = stopTrav.evaluate(SeqRuleset.RULE_TYPE_FORWARDBLOCK, tempNode.getActivity(), false);
						}

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

			int curParent = (nodes.get(curIdx)).getParentLocation();

			int idx = nodes.size() - 1;
			tempNode = nodes.get(idx);

			// Walk backward from last available activity,
			// checking each until we get to a sibling of the current activity
			while (tempNode.getParentLocation() != -1 && tempNode.getParentLocation() != curParent) {
				tempNode = nodes.get(tempNode.getParentLocation());
				ISeqRuleset stopTrav = tempNode.getActivity().getPreSeqRules();

				String result = null;
				if (stopTrav != null) {
					result = stopTrav.evaluate(SeqRuleset.RULE_TYPE_FORWARDBLOCK, tempNode.getActivity(), false);
				}

				// If the rule evaluation did not return null,
				// then all of its descendents are blocked
				if (result != null) {
					if (log.isDebugEnabled()) {
						log.debug("  ::--> BLOCKED SOURCE --> {} [{}]", tempNode.getActivity().getID(), tempNode.getDepth());
					}

					// The depth of the blocked activity
					int blocked = tempNode.getDepth();

					for (int i = idx; i < nodes.size(); i++) {
						ActivityNode tempAN = nodes.get(i);

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
				tempNode = nodes.get(idx);
			}
		}

		log.debug("  ::--> Completed sixth pass");

		// Boundary condition -- if there is a TOC make sure all "selectable"
		// clusters actually flow into content
		for (int i = 0; i < nodes.size(); i++) {
			tempNode = nodes.get(i);

			if (!tempNode.isLeaf()) {
				if (log.isDebugEnabled()) {
					log.debug("  ::--> Process 'Continue' request from {}", tempNode.getActivity().getID());
				}

				SeqActivity from = tempNode.getActivity();

				// Confirm 'flow' is enabled from this cluster
				if (from.getControlModeFlow()) {
					// Begin traversing the activity tree from the root
					Walk treeWalk = new Walk();
					treeWalk.at = from;

					boolean success = processFlow(ADLSequencer.FLOW_FORWARD, true, treeWalk, false);

					if (!success) {
						tempNode.setSelectable(false);

						if (log.isDebugEnabled()) {
							log.debug("  :+: CONTINUE FAILED :+:  --> {}", treeWalk.at.getID());
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
			tempNode = nodes.get(i);

			if (tempNode.isCurrent() && tempNode.isInChoice()) {
				if (tempNode.getDepth() < 0) {
					tempNode.setDepth(-tempNode.getDepth());
				}
			}

			if (tempNode.getDepth() >= 0) {
				while (tempNode.getParentLocation() != -1) {
					tempNode = nodes.get(tempNode.getParentLocation());

					if (tempNode.getDepth() < 0) {
						tempNode.setDepth(-tempNode.getDepth());
					}
				}
			} else if (!tempNode.isHidden()) {
				tempNode.setDepth(-1);
			}
		}

		for (int i = 0; i < nodes.size(); i++) {
			tempNode = nodes.get(i);

			if (tempNode.isHidden()) {
				tempNode.setDepth(-1);
				List<Integer> parents = new ArrayList<>();

				for (int j = i + 1; j < nodes.size(); j++) {
					tempNode = nodes.get(j);

					if (tempNode.getParentLocation() == i && tempNode.getDepth() > 0) {
						tempNode.setDepth(tempNode.getDepth() - 1);
						parents.add(j);
					} else {
						if (tempNode.getDepth() != -1) {
							int idx = parents.indexOf(tempNode.getParentLocation());

							if (idx != -1) {
								tempNode.setDepth(tempNode.getDepth() - 1);
								parents.add(j);
							}
						}
					}
				}
			}
		}

        log.debug("""
                    ::--> Completed TOC walk up
                    :: ADLSequencer --> END   - getTOC
                  """);

		ActivityNode rootNode = null;
		for (int i = 0; i < nodes.size(); i++) {
			ActivityNode node = nodes.get(i);

			if (node.getParentLocation() == -1) {
				rootNode = node;
			} else if (node.getDepth() >= 0) {
				ActivityNode parentNode = nodes.get(node.getParentLocation());
				parentNode.add(node);
			}
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		return treeModel;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Sequencing Behavior

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Retrieves the current set of valid navigation requests.
	 * 
	 * @param oValid
	 *            Upon return, contains the set of valid navigation reqeusts.
	 */
	@Override
	public void getValidRequests(IValidRequests argValid) {
        log.debug("  :: ADLSequencer --> BEGIN - getValidRequests");
		
		ADLValidRequests valid = null;
        ADLValidRequests oValid = (ADLValidRequests) argValid;

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
				//oValid.mTOC = (List) (((List) (valid.mTOC)).clone());
				oValid.mTreeModel = valid.mTreeModel; //convertTOC((List)oValid.mTOC);
			}

			if (valid.mChoice != null) {
				oValid.mChoice = new HashMap<>(valid.mChoice);
			}
		} else {
            log.debug("  ::--> ERROR : Unable to validate requests");

            // Make sure nothing is valid
            oValid.mContinue = false;
            oValid.mContinueExit = false;
            oValid.mPrevious = false;
            oValid.mChoice = null;
            // TODO: Remove this
            //oValid.mTOC = null;
            oValid.mTreeModel = null;
        }

        log.debug("  ::--> [{}]", oValid.mChoice == null ? "NULL" : oValid.mChoice.size());
        log.debug("  :: ADLSequencer --> END   - getValidRequests");
	}

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
	private void invokeRollup(SeqActivity ioTarget, List<String> iWriteObjIDs) {
        log.debug("  :: ADLSequencer --> BEGIN - invokeRollup");
		log.debug("  ::-->  Start: {}", ioTarget.getID());

		Hashtable<String, Integer> rollupSet = new Hashtable<>();

		// Case #1 -- Rollup applies along the active path
		if (ioTarget == mSeqTree.getCurrentActivity()) {
            log.debug("  ::--> CASE #1 Rollup");
			SeqActivity walk = ioTarget;

			// Walk from the target to the root, apply rollup rules at each step
			while (walk != null) {
                log.debug("  ::--> Adding :: {}", walk.getID());

				rollupSet.put(walk.getID(), walk.getDepth());

				List<String> writeObjIDs = walk.getObjIDs(null, false);

				if (writeObjIDs != null) {
					for (int i = 0; i < writeObjIDs.size(); i++) {
						String objID = writeObjIDs.get(i);
                        log.debug("  ::--> Rolling up Obj -- {}", objID);

						// Need to identify all activity's that 'read' this
						// objective
						// into their primary objective -- those activities need
						// to be
						// included in the rollup set
						List<String> acts = mSeqTree.getObjMap(objID);
                        log.debug("  ACTS == {}", acts);

						if (acts != null) {
							for (int j = 0; j < acts.size(); j++) {
								SeqActivity act = getActivity(acts.get(j));
                                log.debug("  *+> {} <+*  :: {}", j, act.getID());

								// Only rollup at the parent of the affected
								// activity
								act = act.getParent();

								if (act != null) {
									// Only add if the activity is selected
									if (act.getIsSelected()) {
                                        log.debug("  ::--> Adding :: {}", act.getID());
										rollupSet.put(act.getID(), act.getDepth());
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
            log.debug("  ::--> CASE #2 Rollup");

			for (int i = 0; i < iWriteObjIDs.size(); i++) {
				String objID = iWriteObjIDs.get(i);
                log.debug("  ::--> Rolling up Obj -- {}", objID);

				// Need to identify all activity's that 'read' this objective
				// into their primary objective -- those activities need to be
				// included in the rollup set
				List<String> acts = mSeqTree.getObjMap(objID);
                log.debug("  ACTS == {}", acts);

				if (acts != null) {
					for (int j = 0; j < acts.size(); j++) {
						SeqActivity act = getActivity(acts.get(j));
                        log.debug("  *+> {} <+*  :: {}", j, act.getID());

						// Only rollup at the parent of the affected activity
						act = act.getParent();

						if (act != null) {
							// Only add if the activity is selected
							if (act.getIsSelected()) {
                                log.debug("  ::--> Adding :: {}", act.getID());
								rollupSet.put(act.getID(), act.getDepth());
							}
						}
					}
				}
			}
		}

		// Perform the deterministic rollup extension
		while (!rollupSet.isEmpty()) {
            log.debug("  ::--> Rollup Set Size == {}", rollupSet.size());

			for (Entry<String, Integer> entry : rollupSet.entrySet()) {
				log.debug("  ::-->  {}  //  {}", entry.getKey(), entry.getValue());
			}

			// Find the deepest activity
			SeqActivity deepest = null;
			int depth = -1;

			for (Entry<String, Integer> entry : rollupSet.entrySet()) {
				String key = entry.getKey();
				int thisDepth = entry.getValue();

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
					@SuppressWarnings("unused")
					String completed = "unknown";
					if (deepest.getObjStatus(false)) {
						completed = deepest.getObjSatisfied(false) ? "satisfied" : "notSatisfied";
					}

					if (deepest.getObjMeasureStatus(false)) {
						completed = Double.valueOf(deepest.getObjMeasure(false)).toString();
					}

					if (deepest.getProgressStatus(false)) {
						completed = deepest.getAttemptCompleted(false) ? "completed" : "incomplete";
					}

					//ADLSeqUtilities.setCourseStatus(mSeqTree.getCourseID(),
					//		mSeqTree.getLearnerID(), satisfied, measure,
					//		completed);
				}

			} else {
                log.debug("  :: ERROR :: No activity found");
			}
		}
        log.debug("  :: ADLSequencer --> END   - invokeRollup");
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
        log.debug("  :: ADLSequencer --> BEGIN - isDescendent");
        log.debug("  ::-->  Root --> {}", iRoot == null ? "NULL" : iRoot.getID());
        log.debug("  ::-->  Target  --> {}", iTarget == null ? "NULL" : iTarget.getID());

		boolean found = false;

		if (iRoot == null) {
            log.debug("  ::-->  ERROR : NULL Root");
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
        log.debug("  ::-->  {}", found);
		log.debug("  :: ADLSequencer --> END   - isDescendent");

		return found;
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
	@Override
	public ADLLaunch navigate(int iRequest) {

		// This method implements all cases, except case #7 of the Navigation
		// Request Process (NB.2.1).
		//
		// It also applies the Overall Sequencing Process (OP) to the
		// indicated navigation request.
        log.debug("  :: ADLSequencer --> BEGIN - navigate");
        log.debug("  ::--> {}", iRequest);

        // This function attempts to translate the navigation request into the
		// corresponding termination and sequencing requests, and invoke the
		// overall sequencing process.

		ADLLaunch launch = new ADLLaunch();

		// Make sure an activity tree has been associated with this sequencer
		if (mSeqTree == null) {
            log.debug("  ::--> ERROR : No activity tree defined.");
			log.debug("  :: ADLSequencer --> END   - navigate");

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
            log.debug("  ::--> No current Activity -- New Session");

			prepareClusters();
			newSession = true;

			validateRequests();
		}

		boolean process = true;

		ADLValidRequests valid = null;

		if (newSession && iRequest == SeqNavRequests.NAV_NONE) {
            log.debug("  ::--> Processing a TOC request");
		} else if (newSession && (iRequest == SeqNavRequests.NAV_EXITALL || iRequest == SeqNavRequests.NAV_ABANDONALL)) {
            log.debug("  ::--> Exiting a session that hasn't started");

			launch.mSeqNonContent = ADLLaunch.LAUNCH_EXITSESSION;
			launch.mEndSession = true;

			process = false;
		} else if (iRequest == SeqNavRequests.NAV_CONTINUE || iRequest == SeqNavRequests.NAV_PREVIOUS) {
			validateRequests();
			valid = mSeqTree.getValidRequests();

			// Can't validate requests -- Error
			if (valid == null) {
                log.debug("  ::--> ERROR : " + "Cannot validate request");

				launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
				launch.mEndSession = true;

				// Invalid request -- do not process
				process = false;
			} else {
				if (iRequest == SeqNavRequests.NAV_CONTINUE) {
					if (!valid.mContinue) {
                        log.debug("  ::--> Continue not valid");

						process = false;
						launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;
					}
				} else {
					if (!valid.mPrevious) {
                        log.debug("  ::--> Previous not valid");

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
                    log.debug("  ::--> Invalid Sequencing");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_RESUMEALL:

				delReq = doSequencingRequest(ADLSequencer.SEQ_RESUMEALL);

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
                    log.debug("  ::--> Invalid Sequencing");
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
                    log.debug("  ::--> Invalid Termination");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
                    log.debug("  ::--> Invalid Sequencing");
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
                    log.debug("  ::--> Invalid Termination");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
                    log.debug("  ::--> Invalid Sequencing");
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
                        log.debug("  ::--> ERROR : " + "Postconditions Processed");
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);

					// If the session hasn't ended, re-validate nav requests
					if (!mEndSession && !mExitCourse) {
                        log.debug("  ::--> REVALIDATE");
						validateRequests();
					}

				} else {
                    log.debug("  ::--> Invalid Termination");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
                    log.debug("  ::--> Invalid Sequencing");
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
                        log.debug("  ::--> ERROR : " + "Postconditions Processed");
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);
				} else {
                    log.debug("  ::--> Invalid Termination");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
                    log.debug("  ::--> Invalid Sequencing");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}

				break;

			case SeqNavRequests.NAV_SUSPENDALL:

				// Issue a termination request of 'suspendAll'
				seqReq = doTerminationRequest(ADLSequencer.TER_SUSPENDALL, false);

				// The termination process cannot return a sequencing request
				// because post condition rules are not evaluated.
				if (mValidTermination) {
					if (seqReq != null) {
                        log.debug("  ::--> ERROR : " + "Postconditions Processed");
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);

				} else {
                    log.debug("  ::--> Invalid Termination");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
                    log.debug("  ::--> Invalid Sequencing");
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
                        log.debug("  ::--> REVALIDATE");
						validateRequests();
					}
				} else {
                    log.debug("  ::--> Invalid Termination");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
                    log.debug("  ::--> Invalid Sequencing");
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
                        log.debug("  ::--> ERROR : " + "Postconditions Processed");
					}

					delReq = doSequencingRequest(ADLSequencer.SEQ_EXIT);
				} else {
                    log.debug("  ::--> Invalid Termination");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
                    log.debug("  ::--> Invalid Sequencing");
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
                log.debug("  ::-->  ERROR : " + "Invalid navigation request: {}", iRequest);
				launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
			}
		} else {
            log.debug("  ::--> INVALID NAV REQUEST");
			launch.mNavState = mSeqTree.getValidRequests();

			// If navigation requests haven't been validated, try to validate
			// now.
			if (launch.mNavState == null) {
                log.debug("  ::--> Not Validated Yet -- DO IT NOW");
				validateRequests();
				launch.mNavState = mSeqTree.getValidRequests();
			}
		}
        log.debug("  :: ADLSequencer --> END   - navigate");
		return launch;
	}

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
	@Override
	public ADLLaunch navigate(String iTarget) {

		// This method implements case 7 of the Navigation Request Process
		// (NB.2.1).
		//
		// It also applies the Overall Sequencing Process (OP) to the
		// indicated navigation request.
        log.debug("  :: ADLSequencer --> BEGIN - navigate[choice]");
		log.debug("  :: [{}]", iTarget);

		ADLLaunch launch = new ADLLaunch();

		// Make sure an activity tree has been associated with this sequencer
		if (mSeqTree == null) {
            log.debug("  ::--> ERROR : No activity tree defined.");
			log.debug("  :: ADLSequencer --> END   - navigate[choice]");

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
						ActivityNode testNode = valid.mChoice.get(iTarget);
						//ADLTOC test = (ADLTOC) valid.mChoice.get(iTarget);

						if (testNode == null) {
                            log.debug("  ::--> Target not available");
							launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;
							process = false;
						} else if (!testNode.isSelectable()) {
                            log.debug("  ::--> Target not selectable");
							launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;
							process = false;
						}
					} else {
                        log.debug("  ::--> No 'choice' enabled");
						launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR_INVALIDNAVREQ;
						process = false;
					}
				} else {
                    log.debug("  ::--> ERROR : Cannot validate request");
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
						seqReq = doTerminationRequest(ADLSequencer.TER_EXIT, false);

						if (seqReq == null) {
							seqReq = iTarget;
						}
					}
				}

				if (mValidTermination) {
					// Issue the pending sequencing request
					delReq = doSequencingRequest(seqReq);
				} else {
                    log.debug("  ::--> Invalid Termination");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_NOTHING;
				}

				if (mValidSequencing) {
					doDeliveryRequest(delReq, false, launch);
				} else {
                    log.debug("  ::--> Invalid Sequencing");
					launch.mSeqNonContent = ADLLaunch.LAUNCH_SEQ_BLOCKED;
				}
			} else {
                log.debug("  ::--> Bad Request");
				launch.mNavState = mSeqTree.getValidRequests();
			}
		} else {
            log.debug("  ::--> ERROR : The target activity is not in the tree");
			launch.mSeqNonContent = ADLLaunch.LAUNCH_ERROR;
			launch.mEndSession = true;
		}
        log.debug("  :: ADLSequencer --> END   - navigate[choice]");
		return launch;
	}

	/**
	 * Walk the activity tree for the first time preparing clusters by applying
	 * selection and randomization processes where appropriate.
	 */
	private void prepareClusters() {
        log.debug("  :: ADLSequencer --> BEGIN - prepareClusters");

		SeqActivity walk = mSeqTree.getRoot();
		List<SeqActivity> lookAt = new ArrayList<>();

		if (walk != null) {
			while (walk != null) {
				// Only prepare clusters
				if (walk.hasChildren(true)) {
					if (!walk.getSelectionTiming().equals(SeqActivity.TIMING_NEVER)) {
						if (!walk.getSelection()) {
							doSelection(walk);
							walk.setSelection(true);
						}
					}

					if (!walk.getRandomTiming().equals(SeqActivity.TIMING_NEVER)) {
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
					if (!lookAt.isEmpty()) {
						walk = lookAt.get(0);
						walk = walk.getChildren(false).get(0);

						lookAt.remove(0);
					}
				}
			}
		} else {
            log.debug("  ERROR :: NULL Activity Tree");
		}
        log.debug("  :: ADLSequencer --> END   - prepareClusters");
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
	private boolean processFlow(int iDirection, boolean iEnter, Walk ioFrom, boolean iConChoice) {

		// This method implements Flow Subprocess SB.2.3
        log.debug("""
                  :: ADLSequencer --> BEGIN - processFlow
                  ::-->  {}
                  ::-->  {}
                  ::--> Con Choice?  {}
                """,
                iDirection, iConChoice, iConChoice ? "Yes" : "No");

        if (ioFrom.at != null) {
            log.debug("  ::-->  {}", ioFrom.at.getID());
        } else {
            log.debug("  ::-->  ERROR : NULL starting point");
		}

		boolean success = true;

		SeqActivity candidate = ioFrom.at;

		// Make sure we have somewhere to start from
		if (candidate != null) {

			Walk walk = walkTree(iDirection, ADLSequencer.FLOW_NONE, iEnter, candidate, !iConChoice);

			if (!iConChoice && walk.at != null) {
				ioFrom.at = walk.at;
				success = walkActivity(iDirection, ADLSequencer.FLOW_NONE, ioFrom);
			} else {
				if (iConChoice) {
					ioFrom.at = walk.at;
                    log.debug("  ::--> Constrained Choice test");
				} else {
                    log.debug("  ::--> No 'next' activity");
				}

				success = false;
			}

			// Check to see if the sequencing session is ending due to
			// walking off the activity tree
			if (walk.at == null && walk.endSession) {
                log.debug("  ::--> ENDING SESSION");

				// End the attempt on the root of the activity tree
				terminateDescendentAttempts(mSeqTree.getRoot());

				// The sequencing session is over -- set global state
				mEndSession = true;

				success = false;
			}
		} else {
			success = false;
		}

        log.debug("""
                    ::-->  {}
                    ::-->  {}
                    :: ADLSequencer --> END   - processFlow
                  """,
                success,
                ioFrom.at != null ? ioFrom.at.getID() : "NULL");

        return success;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Delivery Behavior

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
	@Override
	public void reportSuspension(String iID, boolean iSuspended) {
        log.debug("""
                    :: ADLSequencer --> BEGIN - reportSuspension
                    ::--> Target activity: {}
                    ::-->  {}
                  """, iID, iSuspended);

        SeqActivity target = getActivity(iID);

		// Make sure the target activity is valid
		if (target != null) {
			// Confirm the activity is still active
			if (target.getIsActive()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false) && mSeqTree.getCurrentActivity() == target) {
					// Set the activity's suspended state
					target.setIsSuspended(iSuspended);
				} else {
                    log.debug("  ::--> ERROR : Invalid target");
				}
			} else {
                log.debug("  ::--> ERROR : Target not active");
			}
		} else {
            log.debug("  ::--> ERROR : Activity does not exist");
		}
        log.debug("  :: ADLSequencer --> END   - reportSuspension");
	}

	/**
	 * Sets the active activity tree for this sequencer to act on.
	 * 
	 * @param iTree
	 *            The activty tree for this sequencer to act on.
	 */
	@Override
	public void setActivityTree(ISeqActivityTree iTree) {
        log.debug("  :: ADLSequencer --> BEGIN - setActivityTree");

		// Make sure the activity tree exists.
		if (iTree != null) {
			// Set the activity tree to be acted upon
			mSeqTree = (SeqActivityTree) iTree;
            log.debug("  ::--> Activity tree set.");
		} else {
            log.debug("  ::--> NULL activity tree.");
		}
        log.debug("  :: ADLSequencer --> END   - setActivityTree");
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
	@Override
	public void setAttemptDuration(String iID, IDuration argDur) {
		ADLDuration iDur = (ADLDuration) argDur;

        log.debug("""
                    :: ADLSequencer --> BEGIN - setAttemptDuration
                    ::--> {}  
                    ::--> {}
                  """, iID, iDur != null ? iDur.format(IDuration.FORMAT_SCHEMA) : "NULL");

        SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {
			// Make sure the activity is a valid target for status changes
			// -- the tracked active leaf current activity
			if (target.getIsActive() && target.getIsTracked()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false) && mSeqTree.getCurrentActivity() == target) {
					target.setCurAttemptExDur(iDur);

					// Revalidate the navigation requests
					validateRequests();
				} else {
                    log.debug("  ::--> ERROR : Invalid target");
				}
			} else {
                log.debug("  ::--> ERROR : Target not active");
			}
		} else {
            log.debug("  ::--> ERROR : Activity does not exist");
		}
        log.debug("  :: ADLSequencer --> END   - setAttemptDuration");
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Utility Processes

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

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
	@Override
	public void setAttemptObjMeasure(String iID, String iObjID, double iMeasure) {
        log.debug("""
                    :: ADLSequencer --> BEGIN - setAttemptObjMeasure
                    ::--> Target activity: {}
                    ::--> Objective:       {}
                    ::--> Measure:         {}
                  """, iID, iObjID, iMeasure);

        // Find the target activty
		SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {

			// Make sure the activity is a valid target for status changes
			// -- the tracked active leaf current activity
			if (target.getIsActive() && target.getIsTracked()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false) && mSeqTree.getCurrentActivity() == target) {
					/* boolean statusChange = */
					target.setObjMeasure(iObjID, iMeasure);

					if (true /* statusChange */) {
						// If the activity's status changed, it may affect other
						// activities -- invoke rollup

						target.getObjIDs(iObjID, false);

						// Revalidate the navigation requests
						validateRequests();
					}
				} else {
                    log.debug("  ::--> ERROR : Invalid target");
				}
			} else {
                log.debug("  ::--> ERROR : Target not active");
			}
		} else {
            log.debug("  ::--> ERROR : Activity does not exist");
		}
        log.debug("  :: ADLSequencer --> END   - setAttemptObjMeasure");
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
	@Override
	public void setAttemptObjSatisfied(String iID, String iObjID, String iStatus) {
        log.debug("""
                    :: ADLSequencer --> BEGIN - setAttemptObjSatisfied
                    ::--> Target activity: {}
                    ::--> Objective:       {}
                    ::--> Status:          {}
                  """, iID, iObjID, iStatus);
		// Find the activty whose status is being set
		SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {

			// Make sure the activity is a valid target for status changes
			// -- the tracked active leaf current activity
			if (target.getIsActive() && target.getIsTracked()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false) && mSeqTree.getCurrentActivity() == target) {
					boolean statusChange = target.setObjSatisfied(iObjID, iStatus);

					if (statusChange) {
						target.getObjIDs(iObjID, false);

						// Revalidate the navigation requests
						validateRequests();
					}
				} else {
                    log.debug("  ::--> ERROR : Invalid target");
				}
			} else {
                log.debug("  ::--> ERROR : Target not active");
			}
		} else {
            log.debug("  ::--> ERROR : Activity does not exist");
		}
        log.debug("  :: ADLSequencer --> END   - setAttemptObjSatisfied");
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
	@Override
	public void setAttemptProgressStatus(String iID, String iProgress) {
        log.debug("""
                    :: ADLSequencer --> BEGIN - setAttemptProgressStatus
                    ::--> Target activity:    {}
                    ::--> Completion status:  {}
                  """, iID, iProgress);

        SeqActivity target = getActivity(iID);

		// Make sure the activity exists
		if (target != null) {
			// Make sure the activity is a valid target for status changes
			// -- the tracked active leaf current activity
			if (target.getIsActive() && target.getIsTracked()) {
				// If the activity is a leaf and is the current activity
				if (!target.hasChildren(false) && mSeqTree.getCurrentActivity() == target) {
					boolean statusChange = target.setProgress(iProgress);

					if (statusChange) {

						// If the activity's status changed, it may affect other
						// activities -- invoke rollup
						// invokeRollup(target, null);

						// Revalidate the navigation requests
						validateRequests();
					}
				} else {
                    log.debug("  ::--> ERROR : Invalid target");
				}
			} else {
                log.debug("  ::--> ERROR : Target not active");
			}
		} else {
            log.debug("  ::--> ERROR : Activity does not exist");
		}
        log.debug("  :: ADLSequencer --> END   - setAttemptProgressStatus");
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
        log.debug("  ::--> RETRY == {}", mRetry);
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
        log.debug("  :: ADLSequencer --> BEGIN - terminateDescendentAttempts");

		if (iTarget != null) {
			log.debug("  ::-->  Target: {}", iTarget.getID());
        } else {
            log.debug("  ::--> ERROR : NULL Activity");
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
            log.debug("  ::--> No current activity");
		}
        log.debug("  :: ADLSequencer --> END   -  terminateDescendentAttempts");
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	 Methods to facilitate navigation and UI

	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

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
        log.debug("  :: ADLSequencer --> BEGIN - validateRequests");

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

			String delReq = null;

			// If there is a current activity, 'suspendAll' is valid
			valid.mSuspend = true;

			// If the current activity does not prevent choiceExit,
			// Test all 'Choice' requests
			if (cur.getControlModeChoiceExit() || !cur.getIsActive()) {
				// TODO: Remove this
				//valid.mTOC = getTOC(mSeqTree.getRoot());
				valid.mTreeModel = getTreeModel(mSeqTree.getRoot()); //convertTOC(valid.mTOC);
			}

			if (valid.mTreeModel != null) {
				valid.mChoice = getChoiceSet(valid.mTreeModel);
			}

			if (valid.mTreeModel != null && valid.mTreeModel.getRoot() != null && ((ActivityNode) valid.mTreeModel.getRoot()).getChildCount() == 0) {
				valid.mTreeModel = null;
			}

			// TODO: Remove this.
			/*if (valid.mTOC != null) {
				List newTOC = new ArrayList();

				valid.mChoice = getChoiceSet((List) valid.mTOC, newTOC);

				if (newTOC.size() > 0) {
					valid.mTOC = newTOC;
					//valid.mTreeModel = convertTOC(newTOC);
				} else {
					valid.mTOC = null;
					valid.mTreeModel = null;
				}
			}*/

			if (cur.getParent() != null) {
                log.debug("  ::--> Validate 'Continue'");

				// Always provide a Continue Button if the current activity
				// is in a 'Flow' cluster
				if (cur.getParent().getControlModeFlow()) {
					valid.mContinue = true;
				}				
                log.debug("  ::--> Validate 'Previous'");

				test = doIMSNavValidation(SeqNavRequests.NAV_PREVIOUS);

				if (test) {
					// Test the 'Previous' request
					mValidSequencing = true;

					delReq = doSequencingRequest(ADLSequencer.SEQ_PREVIOUS);
					if (mValidSequencing) {
						valid.mPrevious = doDeliveryRequest(delReq, true, tempLaunch);
					}
				}
			}
		} else {
            log.debug("  ::--> No current activity");

			valid = new ADLValidRequests();
            log.debug("  ::--> Validate 'Start' and 'Resume'");

			// Check to see if a resume All should be processed instead of a
			// start
			if (mSeqTree.getSuspendAll() != null) {
				valid.mResume = true;
			} else {
				// Test Start Navigation Request
				Walk walk = new Walk();
				walk.at = mSeqTree.getRoot();

				valid.mStart = processFlow(ADLSequencer.FLOW_FORWARD, true, walk, false);

				// Validate availablity of the identfied activity if one was
				// identified
				if (valid.mStart) {
					boolean ok = true;

					while (walk.at != null && ok) {
                        log.debug("  ::-->  Checking --> {}", walk.at.getID());

						ok = !checkActivity(walk.at);

						if (ok) {
							walk.at = walk.at.getParent();
						} else {
							valid.mStart = false;
						}
					}
				}
			}
            log.debug("""
                        ::--> 'Start' Request is {}
                        ::--> 'Resume All' Request is {}
                        ::--> Validate 'Choice' requests
                      """, valid.mStart ? "VALID" : "INVALID", valid.mResume ? "VALID" : "INVALID");

			// Test all 'Choice' requests
			// TODO: Remove this
			//valid.mTOC = getTOC(mSeqTree.getRoot());
			valid.mTreeModel = getTreeModel(mSeqTree.getRoot()); //convertTOC(valid.mTOC);

			if (valid.mTreeModel != null) {
				valid.mChoice = getChoiceSet(valid.mTreeModel);
			}

			if (valid.mTreeModel != null && valid.mTreeModel.getRoot() != null && ((ActivityNode) valid.mTreeModel.getRoot()).getChildCount() == 0) {
				valid.mTreeModel = null;
			}

			// TODO: Remove this
			/*if (valid.mTOC != null) {
				List newTOC = new ArrayList();

				valid.mChoice = getChoiceSet((List) valid.mTOC, newTOC);

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
        log.debug("  :: ADLSequencer --> END   - validateRequests");
	}

	/*private Hashtable getChoiceSet(List iOldTOC, List oNewTOC) {
		log.debug("  :: ADLSequencer  --> BEGIN - getChoiceSet");
		if (iOldTOC != null) {
			log.debug("  ::--> {}", iOldTOC.size());
		} else {
			log.debug("  ::--> NULL");
		}

		Hashtable set = null;
		String lastLeaf = null;

		if (iOldTOC != null) {
			ADLTOC temp = null;
			set = new Hashtable();

			// Walk backward along the List looking for the last available
			// leaf
			for (int i = iOldTOC.size() - 1; i >= 0; i--) {
				temp = (ADLTOC) iOldTOC.get(i);

				if (temp.mDepth == -1) {
				    log.debug("  ::--> Remove :: " + temp.mID);

					if (temp.mIsSelectable) {
						log.debug("  ::--> Invisible :: {}",  temp.mID);

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
			log.debug("  ::--> Setting last leaf --> {}", lastLeaf);
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
			ADLTOC temp = (ADLTOC) oNewTOC.get(0);

			if (!temp.mIsEnabled) {
				log.debug("  ::--> Clearing single non-enabled activity");
				oNewTOC.remove(0);
			} else if (!temp.mLeaf) {
				log.debug("  ::--> Clearing root activity");
				oNewTOC.remove(0);
			}
		}
		if (set != null) {
			log.debug("  ::--> {}  //  {}", set.size(), oNewTOC.size());

			for (int i = 0; i < oNewTOC.size(); i++) {
				ADLTOC temp = (ADLTOC) oNewTOC.get(i);

				temp.dumpState();
			} else {
				log.debug("  ::--> NULL");
			}

			log.debug("  :: ADLSequencer  --> END   - getChoiceSet");
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
	
	
	private int addChildren(DefaultMutableTreeNode node, List copy) {
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
		
		List copy = new ArrayList(tocList);
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
		
		List lookAt = new ArrayList();
		boolean done = false;

		// Make sure we have an activity tree
		if (mSeqTree == null) {
			log.debug("  ::-->  No Activity Tree");
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
        log.debug("  :: ADLSequencer --> BEGIN - walkActivity");
		log.debug("  Dir  ::-->  {}", (iDirection == ADLSequencer.FLOW_BACKWARD ? "Backward" : "Forward"));
		if (iPrevDirection == ADLSequencer.FLOW_NONE) {
            log.debug("  Prev ::-->  None");
        } else {
            log.debug("  Prev ::-->  {}", (iPrevDirection == ADLSequencer.FLOW_BACKWARD ? "Backward" : "Forward"));
        }

        if (ioFrom.at != null) {
            log.debug("  ::-->  {}", ioFrom.at.getID());
        } else {
            log.debug("  ::--> ERROR : NULL starting point");
        }

        boolean deliver = true;

		SeqActivity parent = ioFrom.at.getParent();

		if (parent != null) {
			// Confirm that 'flow' is enabled for the cluster
			if (!parent.getControlModeFlow()) {
                log.debug("  ::--> Control Mode violated");
				deliver = false;
			}
		} else {
            log.debug("  ::--> ERROR : Cannot have null parent");
			deliver = false;
		}

		if (deliver) {
			// Check if the activity should be 'skipped'.
			String result = null;

			ISeqRuleset skippedRules = ioFrom.at.getPreSeqRules();

			if (skippedRules != null) {
				result = skippedRules.evaluate(SeqRuleset.RULE_TYPE_SKIPPED, ioFrom.at, mRetry);
			}

			// If the rule evaluation did not return null, the activity is
			// skipped
			if (result != null) {
				Walk walk = walkTree(iDirection, iPrevDirection, false, ioFrom.at, true);

				if (walk.at == null) {
					deliver = false;
				} else {
                    log.debug("  ::-->  RECURSION  <--::");

					ioFrom.at = walk.at;

					// Test if we've switched directions...
					if (iPrevDirection == ADLSequencer.FLOW_BACKWARD && walk.direction == ADLSequencer.FLOW_BACKWARD){
						return walkActivity(ADLSequencer.FLOW_BACKWARD, ADLSequencer.FLOW_NONE, ioFrom);
					} else {
						return walkActivity(iDirection, iPrevDirection, ioFrom);
					}
				}
			} else {
				// The activity was not skipped, make sure it is enabled
				if (!checkActivity(ioFrom.at)) {
					// Make sure the activity being considered is a leaf
					if (ioFrom.at.hasChildren(false)) {
						Walk walk = walkTree(iDirection, ADLSequencer.FLOW_NONE, true, ioFrom.at, true);

						if (walk.at != null) {

							ioFrom.at = walk.at;

							if (iDirection == ADLSequencer.FLOW_BACKWARD && walk.direction == ADLSequencer.FLOW_FORWARD) {
                                log.debug("  ::--> REVERSING");
								deliver = walkActivity(ADLSequencer.FLOW_FORWARD, ADLSequencer.FLOW_BACKWARD, ioFrom);
							} else {
								deliver = walkActivity(iDirection, ADLSequencer.FLOW_NONE, ioFrom);
							}
						} else {
							deliver = false;
						}
					} else {
                        log.debug("  ::--> Found a leaf");
					}
				} else {
					deliver = false;
				}
			}
		}
        log.debug("""
                    ::-->  {}
                    ::-->  {}
                    :: ADLSequencer --> END   - walkActivity
                  """, deliver, ioFrom.at == null ? "NULL" : ioFrom.at.getID());
        return deliver;
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
	private Walk walkTree(int iDirection, int iPrevDirection, boolean iEnter, SeqActivity iFrom, boolean iControl) {

		// This method implements Flow Subprocess SB.2.1
        log.debug("""
                    :: ADLSequencer --> BEGIN - walkTree
                    Dir  ::-->  {}
                    Prev ::-->  {}
                    ::-->  {}
                    ::--> Control?  {}
                    ::-->  {}
                  """,
                iDirection == ADLSequencer.FLOW_BACKWARD ? "Backward" : "Forward",
                iPrevDirection == ADLSequencer.FLOW_NONE ? "None" : iPrevDirection == ADLSequencer.FLOW_BACKWARD ? "Backward" : "Forward",
                iEnter ? "Enter" : "Don't Enter",
                iControl ? "Yes" : "No",
                iFrom == null ? "NULL" : iFrom.getID());

		SeqActivity next = null;
		SeqActivity parent = null;

		int direction = iDirection;
		boolean reversed = false;

		boolean done = false;
		boolean endSession = false;

		if (iFrom == null) {
            log.debug("  ::--> Walked off the Activity Tree");
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
					iFrom = (parent.getChildren(false).get(0));

					reversed = true;
                    log.debug("  ::  REVERSING DIRECTION ::   {}", iFrom.getID());
				}
			}
		}

		if (!done && direction == ADLSequencer.FLOW_FORWARD) {
			if (iFrom.getID().equals(mSeqTree.getLastLeaf())) {

				// We are at the last leaf of the tree, the sequencing
				// session is over
				done = true;
				endSession = true;
                log.debug("  ::-->  Last Leaf");
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
					// log.debug(" ::--> TESTING FORWARD BLOCK");
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
                        log.debug("  ::-->  FORWARD RECURSION  <--::");

						Walk walk = walkTree(direction, ADLSequencer.FLOW_NONE, false, parent, iControl);

						next = walk.at;
						endSession = walk.endSession;
					}
				}
				// else
				// {
				// log.debug(" ::--> BLOCKED <--::");
				//
				// done = true;
				// }
				// }

				// Enter the Cluster
				else {
					// Return the first child activity
					next = (iFrom.getChildren(false).get(0));
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
                            log.debug("  ::--> Forward Only Control Mode violation at -->  {}", iFrom.getID());
							log.debug("  :: ADLSequencer --> END   - walkTree");
							done = true;
						}
					}

					if (!done) {
						next = iFrom.getPrevSibling(false);

						if (next == null) {
                            log.debug("  ::-->  BACKWARD RECURSION  <--::");

							Walk walk = walkTree(direction, ADLSequencer.FLOW_NONE, false, parent, iControl);

							next = walk.at;
							endSession = walk.endSession;
						}
					}
				}

				// Enter the cluster backward
				else {
					if (iFrom.getControlForwardOnly()) {
						// Return the first child activity
						next = (iFrom.getChildren(false).get(0));

						// And switch direction
						direction = ADLSequencer.FLOW_FORWARD;
					} else {
						int size = iFrom.getChildren(false).size();

						// Return the last child activity
						next = (iFrom.getChildren(false).get(size - 1));
					}
				}
			} else {
                log.debug("  ::--> ERROR : Walked off the root");
			}
		} else if (!done) {
            log.debug("  ::--> ERROR : Invalid direction");
		}

        log.debug("""
                    ::-->  {}
                    ::-->  End Session?  {}
                    ::--> MOVING ---> {}
                    :: ADLSequencer --> END   - walkTree
                  """,
                next != null ? next.getID() : "NULL",
                endSession ? "YES" : "NO",
                direction == ADLSequencer.FLOW_FORWARD ? "Forward" : "Backward");

        Walk walk = new Walk();
		walk.at = next;
		walk.direction = direction;
		walk.endSession = endSession;
		return walk;
	}

}
