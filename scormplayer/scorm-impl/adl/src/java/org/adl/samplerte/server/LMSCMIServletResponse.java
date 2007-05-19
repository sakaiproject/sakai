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

import java.io.Serializable;
import java.util.Vector;

import org.adl.datamodels.SCODataManager;
import org.adl.sequencer.ADLValidRequests;

/**
 * <strong>Filename:</strong> LMSCMIServletResponse<br><br>
 *
 * <strong>Description:</strong><br>
 * This class contains the data that the <code>LMSCMIServlet</code> sends  
 * across the socket to the <code>APIAdaptor</code>.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM Sample RTE 1.3. <br>
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
 *     <li>IMS SS Specification</li>
 *     <li>SCORM 2004</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class LMSCMIServletResponse implements Serializable
{
   /**
    * The run-time data associated with the activity.
    */
   public SCODataManager mActivityData = null;

   /**
    * The state of the UI in relation to the current activity.
    */
   public ADLValidRequests mValidRequests = null;

   /**
    * Indicates if an activity is avaliable for immediate delivery.
    */
   public boolean mAvailableActivity = false;

   /**
    * Provides time out tracking data for the LMS Client.
    */
   public Vector mTimeoutTracking = null;

   /**
    * Indicates if the user 'logged out'.
    */
   public boolean mLogout = false;

   /**
    * Indicates any error that occured while processing the request.
    */
   public String mError = null;

   /**
    * Indicates if control mode is flow.
    */
   public boolean mFlow = true;

   /**
    * Indicates if control mode is choice.
    */
   public boolean mChoice = false;

   /**
    * Indicates if control mode is autoadvance.
    */
   public boolean mAuto = false;

   /**
    * Default constructor
    */
   public LMSCMIServletResponse()
   {
   }
} // LMSCMIServletResponse

