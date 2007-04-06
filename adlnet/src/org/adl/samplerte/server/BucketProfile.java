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

package org.adl.samplerte.server;

/**
 * Encapsulation of information required for launch.<br><br>
 * 
 * <strong>Filename:</strong> BucketProfile.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * The <code>BucketProfile</code> encapsulates the information about a specific
 * bucket<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM Sample RTE<br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * All fields are purposefully public to allow immediate access to known data
 * elements.<br><br>
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
public class BucketProfile
{
   /**
    * The user identifier used by the Sample RTE.
    */
   public String mUserID = null;
   
   /**
    * The bucket identifier used bye the Sample RTE.
    */
   public String mBucketID = null;
   
   /**
    * The attempt identifier used by the Sample RTE.
    */
   public String mAttemptID = null;
   
   /**
    * The unique identifier used by the Sample RTE for the course which this 
    * bucket is associated with.
    */
   public String mCourseID = null;
   
   /**
    * The unique identifier used by the Sample RTE for the SCO which this bucket 
    * is associated with.
    */
   public String mSCOID = null;
   
   /**
    * The unique identifier used by the Sample RTE for the activity which this 
    * bucket is associated with.
    */
   public String mActivityID = null;
   
   /**
    * The size requested for this bucket.
    */
   public String mRequested = null;
   
   /**
    * The minimum size of the bucket if the LMS cannot make a bucket of the 
    * requested size.
    */
   public String mMinimum = null;
   
   /**
    * Whether or not the bucket can be created at the minimum size if the LMS 
    * cannot create a bucket of the requested size.
    */
   public String mReducible = null;
   
   /**
    * The scope of the bucket, should be one of either learner, course, or 
    * session.
    */
   public int mPersistence = -1;
   
   /**
    * This is defined by the SCO.
    */
   public String mType = null;
   
   
   /**
    * If an attempt is made to reallocate the bucket, this represents whether 
    * that attempt was a failure.
    */
   public boolean mReallocationFailure = false;
}