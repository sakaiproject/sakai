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

/**
 * Describes the current state of a particular bucket.  It is passed back to the
 * SCO in response to queries.
 * <br><br>
 *
 * <strong>Filename:</strong> BucketState.java<br><br>
 *
 * <strong>Description:</strong><br>
 * 
 * Describes the current state of a particular bucket.  It is passed back to the
 * SCO in response to queries.
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
public class BucketState implements Serializable
{
   /**
    *
    * Reference to a type definition for the bucket's data.
    *
    */
   public String mBucketType = null;

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
   public Integer mTotalSpace = null;

   /**
    *
    * Describes the amount of space currently used in the bucket.
    *
    */
   public Integer mUsed = null;

   /**
    *
    * Default Constructor.
    *
    */
   public BucketState()
   {
   }
}
