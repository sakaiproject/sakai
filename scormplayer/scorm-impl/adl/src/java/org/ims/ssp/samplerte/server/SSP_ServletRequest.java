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

/**
 * Provides a method to send client side SSP requests to the server.
 * <br><br>
 *
 * <strong>Filename:</strong> SSP_ServletRequest.java<br><br>
 *
 * <strong>Description:</strong><br>
 * 
 * Provides a method to send client side SSP requests to the server.
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
public class SSP_ServletRequest implements Serializable
{
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
    * Indicates the type of SSP operation.  This value may be one of the
    * enumerated values described in the <code>SSP_Operation</code> class.
    *
    */
   public int mOperationType = 0;

   /**
    *
    * Identifies the minimum acceptable size of the data bucket.  This should
    * only be set in the case of an allocate operation.
    *
    */
   public String mMinimumSize = null;

   /**
    *
    * Identifies the desired size of the data bucket.  This should
    * only be set in the case of an allocate operation.
    *
    */
   public String mRequestedSize = null;

   /**
    *
    * Indicates if the size of the data bucket is "reducible" to the value
    * identified in the mMinimumSize attribute.  This should only be set in the
    * case of an allocate operation.
    *
    */
   public String mReducible = null;

   /**
    *
    * Identifies the type (format) of the data bucket's content.  This attribute
    * shall not be set to empty string ("").  This should only be set in the
    * case of an allocate operation.
    *
    */
   public String mBucketType = null;

   /**
    *
    * Indicates the expected persistence of the data bucket.  This value may be
    * one of the enumerated values described in the <code>Persistence</code>
    * class.  This should only be set in the case of an allocate operation.
    *
    */
   public int mPersistence = -1;

   /**
    *
    * Identifies the starting (zero-based) octet for the bucket access.  This
    * should only be set in the case of a set or get operation.
    *
    */
   public String mOffset = null;

   /**
    *
    * Identifies the number of octets requested.  This should only be set in the
    * case of a set or get operation.
    *
    */
   public String mSize = null;

   /**
    *
    * The intended value of the SSP datamodel element.  If this is a get
    * operation, then this value shall be null.  This value shall not contain
    * delimiters.
    *
    */
   public String mValue = null;

   /**
    *
    * The array position in the managed bucket array.  This value shall be
    * set to -1 if it is not a manged bucket.
    *
    */
   public int mManagedBucketIndex = -1;

   /**
    *
    * Default constructor
    *
    */
   public SSP_ServletRequest()
   {
      // no defined implementation
   }
}
