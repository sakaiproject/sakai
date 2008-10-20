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

import org.adl.util.debug.DebugIndicator;

import java.io.Serializable;

/**
 * <strong>Filename:</strong> SeqCondition.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This class wraps one condition evaluated during rollup and sequencing rules
 * evaluation<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE. <br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * All fields are purposefully public to allow immediate access to known data
 * elements.<br>
 * 
 * All possible conditions are enumerated by this class although some 
 * conditions do not apply in all instances.<br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS 1.0</li>
 *     <li>SCORM 2004 3rd Edition</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class SeqCondition implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long id;
	
   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Satisfied
    * <br><b>"satisfied"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String SATISFIED         = "satisfied";


   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Objective Status Known
    * <br><b>"objectiveStatusKnown"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String OBJSTATUSKNOWN    = "objectiveStatusKnown";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Objective Measure Known
    * <br><b>"objectiveMeasureKnown"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String OBJMEASUREKNOWN   = "objectiveMeasureKnown";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Objective Measure Greater Than
    * <br><b>"objectiveMeasureGreaterThan"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String OBJMEASUREGRTHAN  = "objectiveMeasureGreaterThan";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Objective Measure Less Than
    * <br><b>"objectiveMeasureLessThan"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String OBJMEASURELSTHAN  = "objectiveMeasureLessThan";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Completed
    * <br><b>"completed"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String COMPLETED         = "completed";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Progress Known
    * <br><b>"progressKnown"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String PROGRESSKNOWN     = "activityProgressKnown";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Attempted
    * <br><b>"attempted"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String ATTEMPTED         = "attempted";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Attempt Limit Exceeded
    * <br><b>"attemptLimitExeeded"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String ATTEMPTSEXCEEDED  = "attemptLimitExceeded";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Time Limit Exceeded
    * <br><b>"timeLimitExceeded"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String TIMELIMITEXCEEDED = "timeLimitExceeded";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Outside Avaliable Time Range
    * <br><b>"outsideAvailableTimeRange"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String OUTSIDETIME       = "outsideAvailableTimeRange";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Always
    * <br><b>"always"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String ALWAYS       = "always";

   /**
    * Enumeration of possible evaluation criteria -- described in Sequencing
    * Rule Description (element 2.2.1) and Rollup Rule Description 
    * (element 3.2.1) of the IMS SS Specification.
    * <br>Never
    * <br><b>"never"</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
   public static String NEVER       = "never";

   /**
    * This controls display of log messages to the java console
    */
   private static boolean _Debug = DebugIndicator.ON;
   
   /**
    * The condition to be evaluated
    */
   public String mCondition = null;

   /**
    * Indicates if the condition evaluation should be negated
    */
   public boolean mNot = false;

   /** 
    * Indicates the objective being tested
    */
   public String mObjID = null;

   /**
    * Indicates the measure threshold being tested
    */
   public double mThreshold = 0.0;

   /**
    *  Default constructor 
    */
   public SeqCondition()
   {
      // Default constructor does nothing explicitly 
   }

   /**
    * This method provides the state this <code>SeqCondition</code> object for
    * diagnostic purposes.
    */
   public void dumpState()
   {
      if ( _Debug )
      {
         System.out.println("  :: SeqCondition  --> BEGIN - dumpState");

         System.out.println("  ::--> Condition :  " + mCondition);
         System.out.println("  ::--> Not?      :  " + mNot);
         System.out.println("  ::--> Obj ID    :  " + mObjID);
         System.out.println("  ::--> Threshold :  " + mThreshold);

         System.out.println("  :: SeqCondition  --> END   - dumpState");
      }
   }

}  // end SeqCondition
