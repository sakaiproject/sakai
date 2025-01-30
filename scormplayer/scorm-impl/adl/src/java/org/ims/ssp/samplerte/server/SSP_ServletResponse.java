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
package org.ims.ssp.samplerte.server;

import java.io.Serializable;

import org.ims.ssp.samplerte.server.bucket.BucketState;
import org.ims.ssp.samplerte.server.bucket.ManagedBucket;
import org.ims.ssp.samplerte.server.bucket.StatusInfo;

/**
 * A serializable object that returns information from the server to the client.
 * <br><br>
 * 
 * <strong>Filename:</strong> SSP_ServletResponse.java<br><br>
 * 
 * <strong>Description:</strong> <br><br>
 * A serializable object that returns information from the server to the client,
 * including information on the bucket ID, student ID, course ID, SCO ID, 
 * attempt ID, bucket allocation success, and bucket state.
 * <br><br>
 * 
 * <strong>Design Issues:</strong><br><br>
 * 
 * <strong>Implementation Issues:</strong><br><br>
 * 
 * <strong>Known Problems:</strong> <br><br>
 * 
 * <strong>Side Effects:</strong> <br><br>
 * 
 * <strong>References:</strong> SCORM <br><br>
 * 
 * @author ADL Technical Team
 */
public class SSP_ServletResponse implements Serializable {
	/**
	  * 
	  */
	private static final long serialVersionUID = 1L;

	/**
	    *
	    * The ID of the bucket associated with this request.
	    *
	    */
	public String mBucketID = null;

	/**
	 *
	 * The ID of the student associated with this request.
	 *
	 */
	public String mStudentID = null;

	/**
	 *
	 * The ID of the course associated with this request.
	 *
	 */
	public String mCourseID = null;

	/**
	 *
	 * The ID of the sco associated with this request.
	 *
	 */
	public String mSCOID = null;

	/**
	 *
	 * Indicates number of the current attempt.
	 *
	 */
	public String mAttemptID = null;

	/**
	 *
	 * The information requested.
	 *
	 */
	public String mReturnValue = null;

	/**
	 *
	 * The allocation success information for this bucket.
	 *
	 */
	public ManagedBucket mManagedBucketInfo = null;

	/**
	 *
	 * Error Information about the last request.
	 *
	 */
	public StatusInfo mStatusInfo = null;

	/**
	 *
	 * Describes the current state of the given bucket.  The object if passed
	 * in the event of a call to GetState() - ssp.n.bucket_state or
	 * ssp.bucket_state
	 *
	 */
	public BucketState mBucketState = null;

	/**
	 *
	 * Default constructor.
	 *
	 */
	public SSP_ServletResponse() {
	}
}
