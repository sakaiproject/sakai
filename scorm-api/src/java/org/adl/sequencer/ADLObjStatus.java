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

package org.adl.sequencer;

import java.io.Serializable;


/**
 * Encapsulation of objective status information associated with an activity.
 * <br><br>
 * 
 * <strong>Filename:</strong> ADLObjStatus.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * <br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE. <br>
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
public class ADLObjStatus implements Serializable
{
	private static final long serialVersionUID = 1L;

/**
    * Identifier of this objective.
    */
   public String mObjID = null;

   /** 
    * Indicates if this objective has a known measure.
    */
   public boolean mHasMeasure = false;

   /**
    * Indicates the measure known for this objective.<br><br>
    * Valid range: <code>[-1.0, 1.0]</code>
    */
   public double mMeasure = 1.0;

   /** 
    * Indicates if the objective contributes to rollup
    */
   public String mStatus = ADLTracking.TRACK_UNKNOWN;

   
   public ADLObjStatus() {}
   

}  // end ADLObjStatus
