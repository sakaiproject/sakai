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

/**
 * Enumeration of acceptable navigation requests.<br><br>
 * 
 * <strong>Filename:</strong> SeqNavRequests.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * The <code>ADLSequencer</code> will accept the navigation requests defined
 * in by this class.  Requests correspond to the IMS SS Specification section
 * Navagation Behavior (NB) section.<br><br>
 * 
 * Navigation requests are signaled to the sequencer through the <code>
 * ADLNavigation</code> interface.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE.<br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * This enumeration does not include the 'choice' navigation request.  Because 
 * the 'choice' navigation request requires a parameter indicating the target
 * activity, 'choice' navigation requests are triggered through an independent
 * method on the <code>ADLNavigation</code> interface.
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS 1.0
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class SeqNavRequests {

	/**
	 * Enumeration of possible navigation requests -- 
	 * In this case, No navigation request is also valid.
	 * <br>None
	 * <br><b>0</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_NONE = 0;

	/**
	 * Enumeration of possible navigation requests -- described in Navigation
	 * Behavior (Section NB of the IMS SS Specification).
	 * <p>
	 * This event indicates a desire to identify the first or “starting” activity available in the activity tree. This event is typically generated automatically by the LMS when the learner begins a new attempt on the root activity of the activity tree.
	 * </p>
	 * <br>Start
	 * <br><b>1</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_START = 1;

	/**
	 * Enumeration of possible navigation requests -- described in Navigation
	 * <p>
	 * ￼￼￼This event indicates a desire to resume a previously suspended attempt on the root activity of the activity tree. This event is typically generated automatically by the LMS when the learner resumes a previously suspended attempt on an activity tree.
	 * </p>
	 * Behavior (Section NB of the IMS SS Specification).
	 * <br>Resume All
	 * <br><b>2</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_RESUMEALL = 2;

	/**
	 * Enumeration of possible navigation requests -- described in Navigation
	 * <p>
	 * This event indicates a desire to identify the “next” (in relation to the Current Activity) logical learning activity available in the activity tree.
	 * </p>
	 * Behavior (Section NB of the IMS SS Specification).
	 * <br>Continue
	 * <br><b>3</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_CONTINUE = 3;

	/**
	 * Enumeration of possible navigation requests -- described in Navigation
	 * <p>
	 * This event indicates a desire to identify the “previous” (in relation to the current activity) logical learning activity available in the activity tree.
	 * </p>
	 * Behavior (Section NB of the IMS SS Specification).
	 * <br>Previous
	 * <br><b>4</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_PREVIOUS = 4;

	/**
	 * Enumeration of possible navigation requests -- described in Navigation
	 * Behavior (Section NB of the IMS SS Specification).
	 * ￼<p>
	 * ￼The current attempt on the current activity is terminated abnormally and the activity is not complete. The attempt may not be resumed. There is no rollback of any tracking data.
	 * </p>
	 * <br>Abandon
	 * <br><b>5</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_ABANDON = 5;

	/**
	 * Enumeration of possible navigation requests -- described in Navigation
	 * Behavior (Section NB of the IMS SS Specification).
	 * 
	 * ￼<p>
	 * The current attempts on the active activities (from the root to the current activity, inclusive) are terminated abnormally and the activities are not complete. Attempts on any abandoned activity may not be resumed. There is no rollback of any tracking data.
	 * </p>
	 * 
	 * <br>AbandonAll
	 * <br><b>6</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_ABANDONALL = 6;

	/**
	 * Enumeration of possible navigation requests -- described in Navigation
	 * Behavior (Section NB of the IMS SS Specification).
	 * 
	 * ￼<p>
	 * The current attempts on the active activities (from the root to the current activity, inclusive) are terminated normally; the attempts are over.
	 * 
	 * <br>SuspendAll
	 * <br><b>7</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_SUSPENDALL = 7;

	/**
	 * The current attempt on the current activity is terminated normally; the attempt is over.
	 * 
	 * ￼<p>
	 * The current attempts on the active activities (from the root to the current activity, inclusive) are suspended. The attempt on the current activity may be resumed.
	 * </p>
	 * 
	 * Enumeration of possible navigation requests -- described in Navigation
	 * Behavior (Section NB of the IMS SS Specification).
	 * <br>Exit
	 * <br><b>8</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_EXIT = 8;

	/**
	 * Enumeration of possible navigation requests -- described in Navigation
	 * <p>This event indicates that the current attempt on the root activity of the activity tree has finished normally.</p>
	 * <p>This event ends the current attempt on the activity tree’s root activity and all active learning activities.</p>
	 * Behavior (Section NB of the IMS SS Specification).
	 * <br>ExitAll
	 * <br><b>9</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int NAV_EXITALL = 9;

} // end SeqNavRequests
