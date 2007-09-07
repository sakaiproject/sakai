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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.adl.util.debug.DebugIndicator;

/**
 * Encapsulation of information tracked for each attempt at an activity.<br><br>
 * 
 * <strong>Filename:</strong> ADLTracking.java<br>
 *
 * <strong>Description:</strong><br>
 * An <code>ADLTracking</code> encapsulates the information required by the
 * sequencer to track status for each new attempt on an activity.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE.<br>
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
public class ADLTracking implements Serializable
{
	private static final long serialVersionUID = 1L;

	private long id;
	
/**
    * Enumeration of possible values for tracking elements  -- described in 
    * Tracking Model elements 2.1 and 2.2 of the IMS SS Specification.
    * <br>unknown
    * <br><b>"unknown"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String TRACK_UNKNOWN            = "unknown";

   /**
    * Enumeration of possible values for tracking elements  -- described in 
    * Tracking Model elements 2.1 and 2.2 of the IMS SS Specification.
    * <br>satisfied
    * <br><b>"satisfied"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]           
    */
   public static String TRACK_SATISFIED          = "satisfied";

   /**
    * Enumeration of possible values for tracking elements  -- described in 
    * Tracking Model elements 2.1 and 2.2 of the IMS SS Specification.
    * <br>notSatisfied
    * <br><b>"notSatisfied"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String TRACK_NOTSATISFIED       = "notSatisfied";

   /**
    * Enumeration of possible values for tracking elements  -- described in 
    * Tracking Model elements 2.1 and 2.2 of the IMS SS Specification.
    * <br>completed
    * <br><b>"completed"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String TRACK_COMPLETED          = "completed";

   /**
    * Enumeration of possible values for tracking elements  -- described in 
    * Tracking Model elements 2.1 and 2.2 of the IMS SS Specification.
    * <br>incomplete
    * <br><b>"incomplete"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String TRACK_INCOMPLETE         = "incomplete";


   /**
    * This controls display of log messages to the java console
    */
   private static boolean _Debug = DebugIndicator.ON;

   /**
    * Indicates if the recorded Progress status is invalid
    */
   public boolean mDirtyPro = false;


   /**
    * The objectives associated with this activity
    */
   public Map mObjectives = null;

   /**
    * Describes the ID for the objective that contributes to rollup.
    */
   public String mPrimaryObj = "_primary_";

   /**
    * The progress tracking status.
    */
   public String mProgress = ADLTracking.TRACK_UNKNOWN;


   /** 
    * This describes the activity's absolute duration.<br>
    * Tracking element 1.2.2 Element 4
    */
   public ADLDuration mAttemptAbDur = null;

   /** 
    * This describes the activity's experienced duration.<br>
    * Tracking element 1.2.2 Element 5
    */
   public ADLDuration mAttemptExDur = null;


   /**
    * Represents the attempt number.
    */
   public long mAttempt = 0;


   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Public Methods
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
   public ADLTracking() {
	   
	   if ( mObjectives == null )
       {
          mObjectives = new Hashtable();
       }
	   
   }
   
   
   /**
    * Initializes tracking status information for this attempt on the
    * associated activity.
    * 
    * @param iObjs      A list of local Objectives (<code>SeqObjective</code>).
    * 
    * @param iLearnerID Identifies the learner this tracking information is
    *                   related to.
    * 
    * @param iScopeID   Identifies the scope this tracking information applies
    */
   public ADLTracking(List iObjs, String iLearnerID, String iScopeID) 
   {

      if ( iObjs != null )
      {

         for ( int i = 0; i < iObjs.size(); i++ )
         {
            SeqObjective obj = (SeqObjective)iObjs.get(i);

            if ( _Debug )
            {
               System.out.println("  ::--> Building Objective  :: " 
                                  + obj.mObjID);
            }

            // Construct an objective for each local objective
            SeqObjectiveTracking objTrack = 
            new SeqObjectiveTracking(obj, iLearnerID, iScopeID);


            // If the objective is defined, add it to the set of objectives
            // associated with this activity
            if ( mObjectives == null )
            {
               mObjectives = new Hashtable();
            }

            mObjectives.put(obj.mObjID, objTrack);

            // Remember if this objective contributes to rollup
            if ( obj.mContributesToRollup )
            {
               mPrimaryObj = obj.mObjID;
            }
         }
      }
      else
      {
         if ( _Debug )
         {
            System.out.println("  ::--> Making default Obj");
         }

         // All activities must have at least one objective and that objective
         // is the primary objective

         SeqObjective def = new SeqObjective();
         def.mContributesToRollup = true;

         SeqObjectiveTracking objTrack =
         new SeqObjectiveTracking(def, iLearnerID, iScopeID);

         if ( mObjectives == null )
         {
            mObjectives = new Hashtable();
         }

         mObjectives.put(def.mObjID, objTrack);

         mPrimaryObj = def.mObjID;
      }
   }


   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Public Methods
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
    * This method provides the state this <code>ADLTracking</code> object for
    * diagnostic purposes.<br>
    */
   public void dumpState()
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLTracking   --> BEGIN - dumpState");

         System.out.println("\t  ::--> Attempt #:   " + mAttempt);
         System.out.println("\t  ::--> Dirty Pro:   " + mDirtyPro);

         if ( mObjectives == null )
         {
            System.out.println("\t  ::--> Objectives :       NULL");
         }
         else
         {

            System.out.println("\t  ::--> Objectives :       [" + 
                               mObjectives.size() + "]");

            Iterator it = mObjectives.keySet().iterator();

            while ( it.hasNext() )
            {
               String key = (String)it.next();

               System.out.println("\t\t  :: " + key + " ::");

               SeqObjectiveTracking obj = 
               (SeqObjectiveTracking)mObjectives.get(key);

               System.out.println("\t\t  ::--> " + 
                                  obj.getObjStatus(false));
               System.out.println("\t\t  ::--> " + 
                                  obj.getObjMeasure(false));
            }

         }

         System.out.println("\t  ::--> Primary:       " + mPrimaryObj);  
         System.out.println("\t  ::--> Progress:      " + mProgress);

         System.out.println("  :: ADLTracking   --> END   - dumpState");
      }
   }

   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Package Methods
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
     * Indicates that the current Objective state is invalid due to a new
     * attempt on the activity's parent.
     */
   void setDirtyObj()
   {
      if ( _Debug )
      {
         System.out.println("  :: ADLTracking     --> BEGIN - " +
                            "setDirtyObj");
      }

      if ( mObjectives != null )
      {

         Iterator it = mObjectives.keySet().iterator();

         while ( it.hasNext() )
         {
            String key = (String)it.next();

            SeqObjectiveTracking obj =
            (SeqObjectiveTracking)mObjectives.get(key);

            obj.setDirtyObj();

         }
      }

      if ( _Debug )
      {
         System.out.println("  :: ADLTracking     --> END   - " +
                            "setDirtyObj");
      }
   }

}  // end ADLTracking
