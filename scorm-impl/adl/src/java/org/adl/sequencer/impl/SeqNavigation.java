/*
 * #%L
 * SCORM ADL Impl
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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

/**
 * Provides an interface for the RTE to communicate navigation requests to the
 * sequencer<br><br>.
 * 
 * <strong>Filename:</strong> SeqNavigation.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This interface represents the entry point to the Overall Sequencing Process
 * described in IMS ss.  The two <code>navigate</code> methods provide a way
 * for the RTE to signal a navigation request. Each <code>navigate</code>
 * method provides an <code>SeqLaunch</code> object, which contains the 
 * information required by the RTE to launch the resource(s) associated with the
 * idenified activity, or an error code if any sequencing process fails.<br><br>
 * 
 * When an navigation request does not result in an activity to be delivered,
 * an <code>ADLLaunch</code> object will still be returned by, however the value
 * its <code>mSeqNonContent</code> field will contain a special value from the
 * <code>ADLLaunch.LAUNCH_[XXX]</code> enumeration.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE. <br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * It is the responsibility of the implementation of this interface to
 * perform any and all prelauch actions to prepare the identifed activity (and
 * its resource(s)) for launch, prior to returning an <code>ADLLaunch</code> 
 * object.<br><br>
 * 
 * If the navigation event does not result in a deliverable activity, it is the
 * responsibily of the RTE to gracefully handle other results.<br><br>
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
public interface SeqNavigation {
	/**
	 * This method is used to inform the sequencer that a navigation request,
	 * other than 'Choice' has occured.
	 * 
	 * @param iRequest Indicates which navigation request should be processed.
	 * 
	 * @return Information about the 'Next' activity to delivery or a processing
	 *         error.
	 * @see org.adl.sequencer.SeqNavRequests
	 * @see org.adl.sequencer.impl.ADLLaunch
	 */
	ADLLaunch navigate(int iRequest);

	/**
	 * This method is used to inform the sequencer that a 'Choice' navigation
	 * request has occured.
	 * 
	 * @param iTarget ID (<code>String</code>) of the target activity.
	 * 
	 * @return Information about the 'Next' activity to delivery or a processing
	 *         error.
	 * @see org.adl.sequencer.impl.ADLLaunch
	 */
	ADLLaunch navigate(String iTarget);

} // end SeqNavigation
