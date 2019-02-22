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

import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import org.adl.sequencer.ADLAuxiliaryResource;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.ILaunch;
import org.adl.sequencer.IValidRequests;
import org.adl.util.debug.DebugIndicator;

/**
 * Encapsulation of information required for launch.<br><br>
 * 
 * <strong>Filename:</strong> ADLLaunch.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * The <code>ADLLaunch</code> encapsulates the information returned from the 
 * <code>ADLSequencer</code> to inform the LMS what content to launch.  This 
 * information is the result of invoking the sequencing process.  The SCORM
 * 2004 3rd Edition Sample RTE uses the information to determine the result
 * of the signaled navigation event and to launch any content object selected
 * by the <code>ADLSequencer</code>.<br><br> It also provides information to
 * the RTE concerning, errors, set of available auxilary resources, and the
 * currently valid navigation requests.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE.<br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * All fields are purposefully public to allow immediate access to known data
 * elements.<br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS Simple Sequencing 1.0
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class ADLLaunch implements ILaunch {

	/**
	 * Enumeration of possible results of the sequencing process that do not
	 * identify the 'next' activity to launch after processing a navigation
	 * request.<br><br>
	 * TOC -- A request to view the table of contents. 
	 * the activity tree.<br><br>
	 * <br><b>"_TOC_"</b>
	 */
	public static String LAUNCH_TOC = "_TOC_";

	/**
	 * Enumeration of possible results of the sequencing process that do not
	 * identify the 'next' activity to launch after processing a navigation
	 * request.<br><br>
	 * Course Complete -- An Exit action rule has succeeded on the the root of 
	 * the activity tree.<br><br>
	 * <br><b>"_COURSECOMPLETE_"</b>
	 */
	public static String LAUNCH_COURSECOMPLETE = "_COURSECOMPLETE_";

	/**
	 * Enumeration of possible results of the sequencing process that do not
	 * identify the 'next' activity to launch after processing a navigation
	 * request.<br><br>
	 * End Session -- A Termination request (ExitAll, AbandonAll, SuspendAll), 
	 * has resulted in the current sequencing session ending.<br><br>
	 */
	public static String LAUNCH_EXITSESSION = "_ENDSESSION_";

	/**
	 * Enumeration of possible results of the sequencing process that do not
	 * identify the 'next' activity to launch after processing a navigation
	 * request.<br><br>
	 * Sequencing Request Blocked -- The traversal of the activity tree was 
	 * blocked while processing the sequencing request.  This may be caused by
	 * invalid control modes, disabled activities, violated limit conditions, or 
	 * sequencing rules. 
	 * <br><b>"_SEQBLOCKED_"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String LAUNCH_SEQ_BLOCKED = "_SEQBLOCKED_";

	/**
	 * Enumeration of possible results of the sequencing process that do not
	 * identify the 'next' activity to launch after processing a navigation
	 * request.<br><br>
	 * Nothing -- Either the termination process or delivery process has failed. 
	 * <br><br>No activity was identified for delivery; the system must wait for
	 * another navigation request.<br><br>
	 * <br><b>"_NOTHING_"</b>
	 */
	public static String LAUNCH_NOTHING = "_NOTHING_";

	/**
	 * Enumeration of possible results of the sequencing process that do not
	 * identify the 'next' activity to launch after processing a navigation
	 * request.<br><br>
	 * General Error -- An error occured during some process resulting in an
	 * overall process failure.<br><br>
	 * <b>NOTE: This result is unrecoverable.  The sequencing session must end.
	 * </b>
	 * <br><b>"_ERROR_"</b>
	 */
	public static String LAUNCH_ERROR = "_ERROR_";

	/**
	 * Enumeration of possible results of the sequencing process that do not
	 * identify the 'next' activity to launch after processing a navigation
	 * request.<br><br>
	 * <br>Sequencer Deadlock -- The current navigation request did not result
	 * in an activity to deliver, and no further navigation requests will
	 * succeed; the activity tree state prohibits further processing.<br><br>
	 * <b>NOTE: This result is unrecoverable.  The sequencing session must end.
	 * </b>
	 * <br><b>"_FAILEDATROOT_"</b>
	 */
	public static String LAUNCH_ERROR_DEADLOCK = "_DEADLOCK_";

	/**
	 * Enumeration of possible results of the sequencing process that do not
	 * identify the 'next' activity to launch after processing a navigation
	 * request.<br><br>
	 * Invalid Navigation Request -- The navigation request could not be
	 * processed; the overall sequencing process was not invoked.<br><br>
	 * <br><b>"_INVALIDNAVREQ_"</b>
	 */
	public static String LAUNCH_ERROR_INVALIDNAVREQ = "_INVALIDNAVREQ_";

	/**
	 * This controls display of log messages to the java console.
	 */
	private static boolean _Debug = DebugIndicator.ON;

	/**
	 * Informs the Client what has occured in within the sequencer if no content has
	 * been identifed for delivery.
	 */
	public String mSeqNonContent = null;

	/**
	 * This flag notifies the Client that a sequencing process has resulted in the
	 * conclusion of the current user session on the current activity tree.
	 */
	public boolean mEndSession = false;

	/**
	 * The activity ID for the activity for delivery.
	 */
	public String mActivityID = null;

	/**
	 * The ID of the resource associated with the activity for delivery.
	 */
	public String mResourceID = null;

	/**
	 * The ID of the run-time state information associated with the activity to
	 * be delivery.
	 */
	public String mStateID = null;

	/**
	 * The attempt count on the activity identified for delivery.
	 */
	public long mNumAttempt = 0;

	/**
	 * This describes the intended launch mode for the activity.
	 */
	public String mDeliveryMode = "normal";

	/**
	 * This describes the intended launch mode for the activity.
	 */
	public String mMaxTime = null;

	/**
	 * A set of auxillary resources (<code>ADLAuxillaryResource</code>)
	 * available to the activity identified for delivery.
	 */
	public Hashtable<String, ADLAuxiliaryResource> mServices = null;

	/**
	 * Provides state information for the UI controls to be enabled for the
	 * activity identified for delivery.
	 */
	public ADLValidRequests mNavState = null;

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Public Methods
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * This method provides the state this <code>ADLLaunch</code> object
	 * for diagnostic purposes.<br>
	 */
	@Override
	public void dumpState() {
		if (_Debug) {
			System.out.println("  :: ADLLaunch  --> BEGIN - dumpState");

			System.out.println("  ::--> Non Content:   " + mSeqNonContent);
			System.out.println("  ::--> End Session:   " + mEndSession);
			System.out.println("  ::--> Activity ID:   " + mActivityID);
			System.out.println("  ::--> Resource ID:   " + mResourceID);
			System.out.println("  ::--> State ID:      " + mStateID);
			System.out.println("  ::--> Attempt #:     " + mNumAttempt);
			System.out.println("  ::--> Delivery Mode: " + mDeliveryMode);
			System.out.println("  ::--> Max Time:      " + mMaxTime);

			if (mServices != null) {
				Set<Entry<String, ADLAuxiliaryResource>> entrySet = mServices.entrySet();
				for (Entry<String, ADLAuxiliaryResource> entry : entrySet) {
					entry.getValue().dumpState();
				}
			}
			/*
			         if ( mNavState != null )
			         {
			            mNavState.dumpState();
			         }
			         else
			         {
			            System.out.println("  ::--> Nav State:       NULL");
			         }
			*/
			System.out.println("  :: ADLLaunch  --> END   - dumpState");
		}
	}

	@Override
	public String getActivityId() {
		return mActivityID;
	}

	@Override
	public String getLaunchStatusNoContent() {
		return mSeqNonContent;
	}

	@Override
	public IValidRequests getNavState() {
		return mNavState;
	}

	@Override
	public String getSco() {
		return mStateID;
	}

} // end ADLLaunch
