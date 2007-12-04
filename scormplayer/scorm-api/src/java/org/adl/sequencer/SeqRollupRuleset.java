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
import java.util.List;
import java.util.Vector;

import org.adl.util.debug.DebugIndicator;

/**
 * Encapsulation of a set of rollup rules associated with an activity.<br><br>
 * 
 * <strong>Filename:</strong> SeqRollupRuleset.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This is an implementation of rollup behavior defined in section RB of the
 * IMS SS Specification.  This class performs evaluation and makes appropriate
 * status change(s) to the affected activity.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE. <br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * As with other classes that encapsulate sequencing behaviors, this class is
 * not optimized.  It is intended to demonstrate the intension of the
 * specification and not provide a 'full-featured' implementation.<br><br>
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
public class SeqRollupRuleset implements Serializable, ISeqRollupRuleset
{
	private static final long serialVersionUID = 1L;
	
	private long id;
	
   /**
    * This controls display of log messages to the java console
    */
   private static boolean _Debug = DebugIndicator.ON;

   /**
    * This is the set of rollup rules applied to the activity.
    */
   private List mRollupRules = null;

   /**
    * This is the result of evaluating the 'Satisfied' rollup rules
    */
   private boolean mIsSatisfied = false;

   /**
    * This is the result of evaluating the 'Not Satisfied' rollup rules
    */
   private boolean mIsNotSatisfied = false;

   /**
    * This is the result of evaluating the 'Completed' rollup rules
    */
   private boolean mIsCompleted = false;

   /**
    * This is the result of evaluating the 'Incomplete' rollup rules
    */
   private boolean mIsIncomplete = false;


   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
   Constructors 
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
    * Default Constructor
    */
   public SeqRollupRuleset()
   {
      // Default constructor
   }

   /**
    * Initializes the current set of rollup rules.
    * 
    * @param iRules      Set of preconstructed rollup rules (<code>SeqRollupRule
    *                    </code>).
    */
   public SeqRollupRuleset(Vector iRules)
   {

      if ( _Debug )
      {
         System.out.println("  :: SeqRollupRuleset  --> BEGIN - constructor");

         if ( iRules == null )
         {
            System.out.println("  ::--> Default Rules");
         }
         else
         {
            for ( int i = 0; i < iRules.size(); i++ )
            {
               SeqRollupRule temp = (SeqRollupRule)iRules.get(i);

               temp.dumpState();
            }
         }
      }

      mRollupRules = iRules;

      if ( _Debug )
      {
         System.out.println("  :: SeqRollupRuleset  --> END   - constructor");
      }
   }

   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Public Methods 
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
   /**
    * Evaluates this set of rollup rules for the target activity.  
    *
    * @param ioThisActivity The target activity of the rollup evaluation.
    */
   public void evaluate(ISeqActivity thisActivity)
   {
	  SeqActivity ioThisActivity = (SeqActivity)thisActivity;
      // Clear previous evaluation state -- nothing should change due to rollup.
      mIsCompleted = false;
      mIsIncomplete = false;
      mIsSatisfied = false;
      mIsNotSatisfied = false;

      // This method implements part of RB.1.5

      if ( _Debug )
      {
         System.out.println("  :: SeqRollupRuleset  --> BEGIN - evaluate");
         if ( mRollupRules != null )
         {
            System.out.println("  ::-->  " + mRollupRules.size());
         }
         else
         {
            System.out.println("  ::-->  NULL");
         }
      }

      // Evaluate all defined rollup rules for this activity.
      // Make sure there is a legal target and a set of children. 
      if ( ioThisActivity != null )
      {

         if ( ioThisActivity.getChildren(false) != null )
         {

            // Step 3.1 -- apply the Measure Rollup Process
            applyMeasureRollup(ioThisActivity);

            boolean satisfiedRule = false;
            boolean completedRule = false;

            if ( mRollupRules != null )
            {
               // Confirm at least one rule is defined for both sets --
               //  Complete/Incomplete and Satisfied/Not Satisfied
               for ( int i = 0; i < mRollupRules.size(); i++ )
               {
                  SeqRollupRule rule = (SeqRollupRule)mRollupRules.
                                       get(i);

                  if ( rule.mAction == SeqRollupRule.
                       ROLLUP_ACTION_SATISFIED ||
                       rule.mAction == SeqRollupRule.
                       ROLLUP_ACTION_NOTSATISFIED )
                  {
                     satisfiedRule = true;
                  }

                  if ( rule.mAction == SeqRollupRule.
                       ROLLUP_ACTION_COMPLETED ||
                       rule.mAction == SeqRollupRule.
                       ROLLUP_ACTION_INCOMPLETE )
                  {
                     completedRule = true;
                  }
               }
            }

            // If no satisfied rule is defined, use default objective rollup
            if ( !satisfiedRule )
            {
               if ( _Debug )
               {
                  System.out.println("  ::--> Creating default " + 
                                     "satisfied rules");
               }

               if ( mRollupRules == null )
               {
                  mRollupRules = new Vector();
               }

               // Create default Not Satisfied rule
               SeqConditionSet set = new SeqConditionSet(true);
               SeqCondition cond = new SeqCondition();
               SeqRollupRule rule = new SeqRollupRule();

               set.mCombination = SeqConditionSet.COMBINATION_ANY;
               set.mConditions = new Vector();

               cond.mCondition = SeqCondition.ATTEMPTED;
               set.mConditions.add(cond);

               cond = new SeqCondition();
               cond.mCondition = SeqCondition.SATISFIED;
               cond.mNot = true;
               set.mConditions.add(cond);

               rule.mAction = SeqRollupRule.ROLLUP_ACTION_NOTSATISFIED;
               rule.mConditions = set;

               // Add the default Not Satisfied rule to the set
               mRollupRules.add(rule);

               // Create default Satisfied rule
               rule = new SeqRollupRule();
               set = new SeqConditionSet(true);
               cond = new SeqCondition();

               set.mCombination = SeqConditionSet.COMBINATION_ALL;
               cond.mCondition = SeqCondition.SATISFIED;
               set.mConditions = new Vector();
               set.mConditions.add(cond);

               rule.mAction = SeqRollupRule.ROLLUP_ACTION_SATISFIED;
               rule.mConditions = set;

               // Add the default Satisfied rule to the set
               mRollupRules.add(rule);
            }

            // If no completion rule is defined, use default completion rollup
            if ( !completedRule )
            {

               if ( _Debug )
               {
                  System.out.println("  ::--> Creating default " + 
                                     "completion rules");
               }

               if ( mRollupRules == null )
               {
                  mRollupRules = new Vector();
               }

               // Create default Incomplete rule
               SeqConditionSet set = new SeqConditionSet(true);
               SeqCondition cond = new SeqCondition();
               SeqRollupRule rule = new SeqRollupRule();

               set.mCombination = SeqConditionSet.COMBINATION_ANY;
               set.mConditions = new Vector();

               cond.mCondition = SeqCondition.ATTEMPTED;
               set.mConditions.add(cond);

               cond = new SeqCondition();
               cond.mCondition = SeqCondition.COMPLETED;
               cond.mNot = true;
               set.mConditions.add(cond);

               rule.mAction = SeqRollupRule.ROLLUP_ACTION_INCOMPLETE;
               rule.mConditions = set;

               // Add the default Incomplete rule to the set
               mRollupRules.add(rule);

               // Create default Completion rule
               rule = new SeqRollupRule();
               set = new SeqConditionSet(true);
               cond = new SeqCondition();

               set.mCombination = SeqConditionSet.COMBINATION_ALL;
               cond.mCondition = SeqCondition.COMPLETED;
               set.mConditions = new Vector();
               set.mConditions.add(cond);

               rule = new SeqRollupRule();
               rule.mAction = SeqRollupRule.ROLLUP_ACTION_COMPLETED;
               rule.mConditions = set;

               // Add the default Completion rule to the set
               mRollupRules.add(rule);
            }

            if ( _Debug )
            {
               System.out.println("  ::--> Size == " + mRollupRules.size());
            }

            // Evaluate all rollup rules.
            for ( int i = 0; i < mRollupRules.size(); i++ )
            {
               SeqRollupRule rule = (SeqRollupRule)mRollupRules.get(i);

               if ( _Debug )
               {
                  System.out.print("  :: EVALUATE ::-->  ");

                  switch(rule.mAction)
                  {
                     case SeqRollupRule.ROLLUP_ACTION_SATISFIED:

                        System.out.println("satisified");
                        break;

                     case SeqRollupRule.ROLLUP_ACTION_NOTSATISFIED:

                        System.out.println("notSatisified");
                        break;

                     case SeqRollupRule.ROLLUP_ACTION_COMPLETED:

                        System.out.println("completed");
                        break;

                     case SeqRollupRule.ROLLUP_ACTION_INCOMPLETE:

                        System.out.println("incomplete");
                        break;

                     default:
                        System.out.println("ERROR");

                  }
               }

               int result = rule.evaluate(ioThisActivity.getChildren(false));

               // Track state changes
               switch ( result )
               {
                  case SeqRollupRule.ROLLUP_ACTION_NOCHANGE:

                     // No status change indicated
                     if ( _Debug )
                     {
                        System.out.println("  :+ NO STATUS CHANGE +: CHANGE");
                     }

                     break;

                  case SeqRollupRule.ROLLUP_ACTION_SATISFIED:

                     if ( _Debug )
                     {
                        System.out.println("  :+ SATISFIED +: CHANGE");
                     }

                     mIsSatisfied = true;
                     break;

                  case SeqRollupRule.ROLLUP_ACTION_NOTSATISFIED:

                     if ( _Debug )
                     {
                        System.out.println("  :+ NOT SATISFIED +: CHANGE");
                     }

                     mIsNotSatisfied = true;
                     break;

                  case SeqRollupRule.ROLLUP_ACTION_COMPLETED:

                     if ( _Debug )
                     {
                        System.out.println("  :+ COMPLETED +: CHANGE");
                     }

                     mIsCompleted = true;
                     break;

                  case SeqRollupRule.ROLLUP_ACTION_INCOMPLETE:

                     if ( _Debug )
                     {
                        System.out.println("  :+ INCOMPLETE +: CHANGE");
                     }

                     mIsIncomplete = true;
                     break;
                     
                  default:
                     break;
               }
            }

            // If a measure threshold exists, it was already used to determine
            // the activity's status.  Otherwise, use the results of the rollup
            if ( !ioThisActivity.getObjSatisfiedByMeasure() )
            {
               if ( _Debug )
               {
                  System.out.println("  ::--> Objective rollup using rules");
               }

               if ( mIsSatisfied )
               {
                  ioThisActivity.setObjSatisfied(ADLTracking.TRACK_SATISFIED);
               }
               else if ( mIsNotSatisfied )
               {
                  ioThisActivity.
                  setObjSatisfied(ADLTracking.TRACK_NOTSATISFIED);
               }
            }

            if ( _Debug )
            {
               System.out.println("  ::--> Completion rollup using rules");
            }

            if ( mIsCompleted )
            {
               ioThisActivity.setProgress(ADLTracking.TRACK_COMPLETED);
            }
            else if ( mIsIncomplete )
            {
               ioThisActivity.setProgress(ADLTracking.TRACK_INCOMPLETE);
            }
         }
         else
         {
            if ( _Debug )
            {
               System.out.println("  ::--> ERROR : No Children");
            }
         }
      }
      else
      {
         if ( _Debug )
         {
            System.out.println("  ::--> ERROR : Invalid rollup rules");
         }
      }

      if ( _Debug )
      {
         System.out.println("  :: SeqRollupRuleset  --> END - evaluate");
      }
   }

   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Private Methods 
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
   /**
    * Applies the Measure Rollup Process to the activity (RB.1.1).
    *
    * @param ioThisActivity The target activity of the rollup evaluation.
    */
   private void applyMeasureRollup(SeqActivity ioThisActivity)
   {
      if ( _Debug )
      {
         System.out.println("  :: SeqRollupRuleset  --> BEGIN - " + 
                            "applyMeasureRollup");
      }

      double total = 0.0;
      double countedMeasure = 0.0;

      List children = ioThisActivity.getChildren(false);

      // Measure Rollup Behavior 
      for ( int i = 0; i < children.size(); i++ )
      {
         SeqActivity child = (SeqActivity)children.get(i);

         if ( _Debug )
         {
            System.out.println("  ::--> Look At :: " + child.getID());
         }

         if ( child.getIsTracked() )
         {
            // Make sure a non-zero weight is defined
            if ( child.getObjMeasureWeight() > 0.0 )
            {
               countedMeasure += child.getObjMeasureWeight();

               // If a measure is defined for the child
               if ( child.getObjMeasureStatus(false) )
               {
                  total += child.getObjMeasureWeight() * 
                           child.getObjMeasure(false);
               }
            }
         }
      }

      if ( countedMeasure > 0.0 )
      {

         if ( _Debug )
         {
            System.out.println("  ::--> Counted         --> " +
                               countedMeasure);
            System.out.println("  ::--> Setting Measure --> " + 
                               (total / countedMeasure));
         }

         ioThisActivity.setObjMeasure(total / countedMeasure);
      }
      else
      {

         if ( _Debug )
         {
            System.out.println("  ::--> Setting Measure --> UNKNOWN");
         }

         // Measure could not be determined through rollup, clear measure
         ioThisActivity.clearObjMeasure();
      }

      if ( _Debug )
      {
         System.out.println("  :: SeqRollupRuleset  --> END   - " + 
                            "applyMeasureRollup");
      }
   }


   /**
    * Describes the number of rollup rules in this set
    * 
    * @return The count of rollup rules in this set.
    */
   public int size()
   {
      if ( mRollupRules != null )
      {
         return mRollupRules.size();
      }

      return 0;
   }

}  // end SeqRollupRuleset
