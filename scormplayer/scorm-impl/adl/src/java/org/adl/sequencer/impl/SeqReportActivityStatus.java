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

import org.adl.sequencer.IDuration;

/**
 * Provides the mechanism to allow the RTE to communicate runtime activity
 * state and status information to the sequencer.<br><br>
 * 
 * <strong>Filename:</strong> SeqReportActivityStatus.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * An activity may report status and tracking information to the RTE, while it
 * is active.  It is the reponsibility of the RTE to communicate any state
 * and status information it recieves to the sequencer.  This ensures that the
 * state of the activity tree remains valid throughout a learner's session.
 * <br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE.<br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * These methods only apply to an active leaf activity (the current activity).
 * Invoking any of these methods commits the data and will affect validation of
 * potential sequencing requests.<br><br>
 *  
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS Specification
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 * 
 * @author ADL Technical Team
 */
public interface SeqReportActivityStatus {

	/**
	 * This method is used to inform the sequencer to clear one of the
	 * activity's objective's measures -- set it to 'unknown'.
	 * 
	 * @param iID    ID of the activity whose measure has changed.
	 * 
	 * @param iObjID ID of the objective whose measure has changed.
	 */
	void clearAttemptObjMeasure(String iID, String iObjID);

	/**
	 * This method is used to inform the sequencer of the suspended state for the
	 * current activity.  This state will take affect when the activity
	 * terminates.
	 * 
	 * @param iID        ID of the activity whose suspended state is being set.
	 * 
	 * @param iSuspended Indicates if the activity is suspended (<code>true
	 *                   </code>) or not (<code>false</code>).
	 */
	void reportSuspension(String iID, boolean iSuspended);

	/**
	 * This method is used to inform the sequencer of a change to an activity's
	 * current attempt experienced duration.
	 * 
	 * @param iID    ID of the activity being affected.
	 * 
	 * @param iDur   Indicates the experienced duration of the current attempt.
	 */
	void setAttemptDuration(String iID, IDuration iDur);

	/**
	 * This method is used to inform the sequencer of a change to one of the
	 * activity's objective's measures.
	 * 
	 * @param iID      ID of the activity whose measure has changed.
	 * 
	 * @param iObjID   ID of the objective whose measure has changed.
	 * 
	 * @param iMeasure New value for the objective's measure.
	 */
	void setAttemptObjMeasure(String iID, String iObjID, double iMeasure);

	/**
	 * This method is used to inform the sequencer of a change to one of the
	 * activity's objective's satisfaction statuses.
	 * 
	 * @param iID     ID of the activity whose status has changed.
	 * 
	 * @param iObjID  ID of the objective whose satisfaction has changed.
	 * 
	 * @param iStatus New value for the objective's satisfaction status.
	 *                Valid values are 'unknown', 'satisfied, 'notsatisfied'.
	 */
	void setAttemptObjSatisfied(String iID, String iObjID, String iStatus);

	/**
	 * This method is used to inform the sequencer of a change to the activity's
	 * progress status.
	 * 
	 * @param iID       ID of the activity whose progress status has changed.
	 * 
	 * @param iProgress New value for the activity's progress status.
	 *                  Valid values are: 'unknown', 'completed', 'incomplete'.
	 */
	void setAttemptProgressStatus(String iID, String iProgress);

} // end SeqReportActivityStatus
