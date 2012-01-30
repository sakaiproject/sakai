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
package org.ims.ssp.samplerte.server;

/**
 * This class is an enumeration of possible SSP operation types that describe 
 * the operation to be done to an SSP bucket.<br><br>
 * 
 * <strong>Filename:</strong> SSP_Operation.java<br><br>
 * 
 * <strong>Description:</strong> <br><br>
 * This class is an enumeration of possible SSP operation types that describe 
 * the operation to be done to an SSP bucket.  It consists of the NONE, 
 * ALLOCATE, GET_ALLOCATION_SUCCESS, GET_BUCKET_IDS, APPEND_DATA, GET_DATA,
 * GET_DATA, GET_STATE, SET_DATA.
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
public class SSP_Operation {
	/**
	 * Indicates that no SSP operation was defined by the LMS.
	 *
	 * <br>NONE
	 *
	 * <br><b>0</b>
	 *
	 * <br>[SCORM SSP SUBSYSTEM CONSTANT]
	 */
	public static final int NONE = -1;

	/**
	 * Indicates that the SCO requests that a bucket be allocated by the LMS.
	 *
	 * <br>Allocate
	 *
	 * <br><b>0</b>
	 *
	 * <br>[SCORM SSP SUBSYSTEM CONSTANT]
	 */
	public static final int ALLOCATE = 0;

	/**
	 * Indicates that the SCO is requesting whether the LMS's allocation of a
	 * bucket was successful.
	 *
	 * <br>Get Allocation
	 *
	 * <br><b>1</b>
	 *
	 * <br>[SCORM SSP SUBSYSTEM CONSTANT]
	 */
	public static final int GET_ALLOCATION_SUCCESS = 1;

	/**
	 * Indicates that the SCO is requesting the IDs of the buckets that are
	 * accessible to it.
	 *
	 * <br>Get Bucket IDs
	 *
	 * <br><b>2</b>
	 *
	 * <br>[SCORM SSP SUBSYSTEM CONSTANT]
	 */
	public static final int GET_BUCKET_IDS = 2;

	/**
	 * Indicates that the SCO requests that data be appended to a bucket.
	 *
	 * <br>Append Data
	 *
	 * <br><b>3</b>
	 *
	 * <br>[SCORM SSP SUBSYSTEM CONSTANT]
	 */
	public static final int APPEND_DATA = 3;

	/**
	 * Indicates that the SCO requests the data from a bucket.
	 *
	 * <br>Get Data
	 *
	 * <br><b>4</b>
	 *
	 * <br>[SCORM SSP SUBSYSTEM CONSTANT]
	 */
	public static final int GET_DATA = 4;

	/**
	 * Indicates that the SCO requests the state of the bucket, including the
	 * total space available, amount used, type definition for the bucket's data.
	 *
	 * <br>Get State
	 *
	 * <br><b>5</b>
	 *
	 * <br>[SCORM SSP SUBSYSTEM CONSTANT]
	 */
	public static final int GET_STATE = 5;

	/**
	 * Indicates that the SCO requests that data be set to the bucket.
	 *
	 * <br>Set Data
	 *
	 * <br><b>6</b>
	 *
	 * <br>[SCORM SSP SUBSYSTEM CONSTANT]
	 */
	public static final int SET_DATA = 6;
}
