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
 ******************************************************************************/

package org.adl.datamodels.ssp;

import java.io.Serializable;

/**
 * Enumeration of all SSP data model errors.<br>
 * <br>
 * 
 * <strong>Filename:</strong> SSP_DMErrorCodes.java<br>
 * <br>
 * 
 * <strong>Description:</strong> Enumeration of SSP error codes<br>
 * <br>
 * 
 * @author ADL Technical Team
 */
public class SSP_DMErrorCodes implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8648996862177072808L;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int NO_ERROR = 0;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int INVALID_SET_PARMS = 10000;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int INVALID_GET_PARMS = 10001;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int ALLOCATE_MIN_GREATER_MAX = 10002;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int DATABASE_UPDATE_FAILURE = 10003;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int TYPE_MISMATCH = 10004;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int ALLOCATE_SPACE_EXCEEDED = 10005;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int BUCKET_NOT_ALLOCATED_SET = 10006;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int BUCKET_NOT_ALLOCATED_GET = 10007;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int REQUESTED_SIZE_EXCEEDED_AVAIL = 10008;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int BUCKET_SIZE_EXCEEDED_SET = 10009;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int CREATE_BUCKET_PERSIST = 10010;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int DATABASE_CREATE_FAILURE = 10011;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int BUCKET_IMPROPERLY_DECLARED_SET = 10012;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int BUCKET_IMPROPERLY_DECLARED_GET = 10013;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int OFFSET_EXCEEDS_BUCKET_SIZE_SET = 10014;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int OFFSET_EXCEEDS_BUCKET_SIZE_GET = 10015;

	/**
	 * Enumeration of possible implementation specific data model exceptions.
	 */
	public static final int BUCKET_NOT_PACKED = 10016;

} // end SSP_DMErrorCodes
