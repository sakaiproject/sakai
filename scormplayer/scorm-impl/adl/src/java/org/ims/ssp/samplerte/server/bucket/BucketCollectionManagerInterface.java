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
/******************************************************************************
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
package org.ims.ssp.samplerte.server.bucket;

/**
 * Provides a SCO access to the set of buckets explicitly requested by the SCO.
 * <br><br>
 *
 * <strong>Filename:</strong> BucketCollectionManagerInterface.java<br><br>
 *
 * <strong>Description:</strong><br>
 * 
 * Provides a SCO access to the set of buckets explicitly requested by the SCO.
 * <br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd Edtion 
 * Sample RTE. <br>
 * <br>
 *
 * <strong>Implementation Issues:</strong><br><br>
 *
 * <strong>Known Problems:</strong><br><br>
 *
 * <strong>Side Effects:</strong><br><br>
 *
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SSP Specification
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 *
 * @author ADL Technical Team
 */
public interface BucketCollectionManagerInterface {
	/**
	 *
	 * Creates a bucket.
	 *
	 * @param iDescription - Requirements for the bucket to be created.
	 *
	 * @return - Status or result information about the outcome of this call.
	 */
	StatusInfo allocate(BucketAllocation iDescription);

	/**
	 *
	 * Retrieves the success status of the specified bucket.
	 *
	 * @param iBucketID - The identifier of the bucket.
	 * @param oSuccessStatus - The information requested.
	 *
	 * @return - Status or result information about the outcome of this call.
	 */
	StatusInfo getAllocationSuccess(String iBucketID, Integer oSuccessStatus);

	/**
	 *
	 * Retrieves an array of bucketIDs.
	 *
	 * @param oBucketIDs - The array ov bucketIDs requested.
	 *
	 * @return - Status or result information about the outcome of this call.
	 */
	StatusInfo getBucketIDs(String[] oBucketIDs);
}
