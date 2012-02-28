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
 * Describes an interface for managing buckets.
 * 
 * @author ADL Technical Team
 */
public interface BucketManagerInterface {
	/**
	 *
	 * Updates the bucket to put a block of data in the bucket, starting at the
	 * end of the current data.
	 *
	 * @param ibucketID - The identifier of the bucket.
	 * @param iData - The data to be stored in this bucket.
	 * @param iBucketAllocation - The bucket allocation information.
	 *
	 * @return - Status or result information about the outcome of this call.
	 */
	StatusInfo appendData(String ibucketID, byte[] iData, BucketAllocation iBucketAllocation);

	/**
	 *
	 * Retrieves all data currently stored in the bucket
	 *
	 * @param iBucketID - The identifier of the bucket.
	 * @param oData - The data stored in this bucket.
	 * @param iBucketAllocation - The bucket allocation information.
	 *
	 * @return - Status or result information about the outcome of this call.
	 */
	StatusInfo getData(String iBucketID, byte[] oData, BucketAllocation iBucketAllocation);

	/**
	 *
	 * Retrieves the specified amount of data starting at the specified offset
	 * position.
	 *
	 * @param iBucketID - The identifier of the bucket.
	 * @param iOffset - The position in the bucket to start reading.
	 * @param iSize - The amount of data requested.
	 * @param oData - The specified data stored in this bucket starting at the
	 *                specified offset position.
	 * @param iBucketAllocation - The bucket allocation information.
	 *
	 * @return - Status or result information about the outcome of this call.
	 */
	StatusInfo getData(String iBucketID, int iOffset, int iSize, byte[] oData, BucketAllocation iBucketAllocation);

	/**
	 *
	 * Retrieves the current state of the bucket.
	 *
	 * @param iBucketID - The identifier of the bucket.
	 * @param oState - The current state of the bucket.
	 * @param iBucketAllocation - The bucket allocation information.
	 *
	 * @return - Status or result information about the outcome of this call.
	 */
	StatusInfo getState(String iBucketID, BucketState oState, BucketAllocation iBucketAllocation);

	/**
	 *
	 * Updates the bucket to put a block of data in the bucket, replacing all
	 * data currently stored in the bucket
	 *
	 * @param iBucketID - The identifier of the bucket.
	 * @param iData - The data to be stored in this bucket.
	 * @param iBucketAllocation - The bucket allocation information.
	 *
	 * @return - Status or result information about the outcome of this call.
	 */
	StatusInfo setData(String iBucketID, byte[] iData, BucketAllocation iBucketAllocation);

	/**
	 *
	 * Updates the bucket to put a block of data in the bucket, replacing any
	 * data currently stored in the bucket starting at the specified offset
	 * position.
	 *
	 * @param iBucketID - The identifier of the bucket.
	 * @param iOffset - The position in the bucket to start reading.
	 * @param iData - The data to be stored in this bucket.
	 * @param iBucketAllocation - The bucket allocation information.
	 *
	 * @return - Status or result information about the outcome of this call.
	 */
	StatusInfo setData(String iBucketID, int iOffset, byte[] iData, BucketAllocation iBucketAllocation);
}
