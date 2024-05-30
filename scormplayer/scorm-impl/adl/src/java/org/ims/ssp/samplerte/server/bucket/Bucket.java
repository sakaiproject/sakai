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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Logical implemenation of a conceptual description of an IMS SSP bucket.
 * <br><br>
 *
 * <strong>Filename:</strong> Bucket.java<br><br>
 *
 * <strong>Description:</strong><br>
 * Logical implemenation of a conceptual description of an IMS SSP bucket.
 *
 * <br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd Edition
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
public class Bucket implements Serializable {
	/**
	  * 
	  */
	private static final long serialVersionUID = 1L;

	/**
	    * The UTF-16 character set.
	    */
	public static final String CHARSET = "UTF-16";

	/**
	 *
	 * The identifier of the bucket.
	 *
	 */
	private String mBucketID = null;

	/**
	 *
	 * Reference to a type definition for the bucket's data.
	 *
	 */
	private String mBucketType = null;

	/**
	 *
	 * The data stored in this bucket.
	 *
	 */
	private byte[] mData = null;

	/**
	 *
	 * Describes the minimum amount of space requested.
	 *
	 */
	private int mMinimum = 0;

	/**
	 *
	 * Describes how long the runtime system should persist the data in the
	 * bucket.  This value may be one of the enumerated values described in the
	 * <code>Persistence</code> class.  If not provided, the default value is
	 * <code>Persistence.LEARNER</code>
	 *
	 */
	private int mPersistence = Persistence.LEARNER;

	/**
	 *
	 * Indicates if the amount of space requested was allowed to be reduced.
	 *
	 */
	private boolean mReducible = false;

	/**
	 *
	 * Describes the amount of space requested.
	 *
	 */
	private int mRequested = 0;

	/**
	 *
	 * The total amount of space available for this bucket.  This number is
	 * determined once, upon bucket allocation, based on the bucket's allocation
	 * requirements. <b>Note:</b> This element does not necessarily represent the
	 * "allocated" space for the bucket.  Instead, it is the amount of space the
	 * runtime system has committed to the bucket, based on the bucket's
	 * allocation requirements.
	 *
	 */
	private int mTotalSpace = 0;

	/**
	 *
	 * Describes the amount of space currently used in the bucket.
	 *
	 */
	private int mUsed = 0;

	/**
	 *
	 * Constructor method.
	 *
	 * Side Effect: This constructor instantiates the amount of space currently
	 * used in the bucket (<code>mUsed</code>) based on the length of the
	 * <code>iData</code> parameter.
	 *
	 * @param iBucketID - The identifier of the bucket.
	 * @param iBucketType - Reference to a type definition for the bucket's data.
	 * @param iMinimum - Describes the minimum amount of space requested.
	 * @param iPersistence - Describes how long the runtime system should persist
	 *                       the data in the bucket.  This value shall be one of
	 *                       the enumerated values described in the
	 *                       <code>Persistence</code> class.
	 * @param iReducible - Indicates if the amount of space requested was allowed
	 *                     to be reduced.
	 * @param iRequested - Describes the amount of space requested.
	 * @param iTotalSpace - The total amount of space available for this bucket.
	 *
	 */
	public Bucket(String iBucketID, String iBucketType, int iMinimum, int iPersistence, boolean iReducible, int iRequested, int iTotalSpace) {
		mBucketID = iBucketID;
		mBucketType = iBucketType;
		mMinimum = iMinimum;
		mPersistence = iPersistence;
		mReducible = iReducible;
		mRequested = iRequested;
		mTotalSpace = iTotalSpace;
		mUsed = 0;
		String empty = "";

		try {
			mData = empty.getBytes(CHARSET);
		} catch (UnsupportedEncodingException uee) {
			mData = empty.getBytes();

			System.out.println("UnsupportedEncodingException: " + CHARSET + " is not a supported encoding.  The default " + "encoding is being used");
		}
	}

	/**
	 * Accessor method to retrieve the ID of the bucket.
	 *
	 * @return - The identifier of the bucket.
	 */
	public String getBucketID() {
		return mBucketID;
	}

	/**
	 *
	 * Accessor method to retrieve the Type of the bucket.
	 *
	 * @return - Reference to a type definition for the bucket's data.
	 *
	 */
	public String getBucketType() {
		return mBucketType;
	}

	/**
	 *
	 * Accessor method to retrieve the data contained in the bucket.
	 *
	 * @return - The data stored in this bucket.
	 *
	 */
	public byte[] getData() {
		return mData;
	}

	/**
	 *
	 * Accessor method to retrieve the minimum size requirement of the bucket.
	 *
	 * @return - The minimum amount of space requested.
	 *
	 */
	public int getMinimum() {
		return mMinimum;
	}

	/**
	 *
	 * Accessor method to retrieve the persistence scope of the bucket.
	 *
	 * @return - How long the runtime system should persist the data in
	 *           the bucket.  This value may be one of the enumerated values
	 *           described in the <code>Persistence</code> class.
	 *
	 */
	public int getPersistence() {
		return mPersistence;
	}

	/**
	 *
	 * Accessor method to retrieve the value that indicates if the amount of
	 * space requested was allowed to be reduced.
	 *
	 * @return - An Indicator if the amount of space requested was allowed to be
	 *           reduced.
	 *
	 */
	public boolean getReducible() {
		return mReducible;
	}

	/**
	 *
	 * Accessor method to retrieve the requested size of the bucket.
	 *
	 * @return - The amount of space requested.
	 *
	 */
	public int getRequested() {
		return mRequested;
	}

	/**
	 *
	 * Accessor method to retrieve the total size of the bucket.
	 *
	 * @return - The total amount of space available for this bucket.
	 *
	 */
	public int getTotalSpace() {
		return mTotalSpace;
	}

	/**
	 *
	 * Accessor method to retrieve the amount of space used of the bucket.
	 *
	 * @return - The amount of space currently used in the bucket.
	 *
	 */
	public int getUsed() {
		return mUsed;
	}

	/**
	 * Accessor method to store the data to be contained in the bucket.
	 *
	 * Side Effect: This method updates the amount of space currently
	 * used in the bucket (<code>mUsed</code>) based on the length of the
	 * <code>iData</code> parameter.
	 *
	 * @param iData - The data stored in this bucket.
	 */
	public void setData(byte[] iData) {
		mData = iData;

		String data;

		try {
			data = new String(mData, CHARSET);
		} catch (UnsupportedEncodingException uee) {
			data = new String(mData);
		}

		mUsed = data.length() * 2;

	}
}
