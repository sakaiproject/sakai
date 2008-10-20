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
 * Encapsulation of a set of sequencing rules associated with an activity.
 * <br><br>
 * 
 * <strong>Filename:</strong> SeqRuleset.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This is an implementation of sequencing rule behaviors described in the
 * IMS SS Specification.  This class encapsulates all sequencing rules,
 * performs evaluation, and takes appropriate action(s) in response to rules.
 * <br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE. <br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * As with other classes that encapsulate sequencing behaviors, this class
 * is not optimized.  It is intended to demonstrate the intension of the
 * specification and not a 'full-featured' implementation.<br><br>
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
public class SeqRuleset implements Serializable, ISeqRuleset
{
	static final long serialVersionUID = 1L; //-1546682264123089317L;

	private long id;

   /**
    * Enumeration of the possible types of rules that may be evaluated.
    * <br>All
    * <br><b>1</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
	public static final int RULE_TYPE_ANY               =  1;

   /**
    * Enumeration of the possible types of rules that may be evaluated.
    * <br>Exit Rules
    * <br><b>2</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
	public static final int RULE_TYPE_EXIT              =  2;

   /**
    * Enumeration of the possible types of rules that may be evaluated.
    * <br>Post Condition Rules
    * <br><b>3</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
	public static final int RULE_TYPE_POST              =  3;

   /**
    * Enumeration of the possible types of rules that may be evaluated.
    * <br>Skipped Rules
    * <br><b>5</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
	public static final int RULE_TYPE_SKIPPED           =  4;

   /**
    * Enumeration of the possible types of rules that may be evaluated.
    * <br>Disabled Rules
    * <br><b>6</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
	public static final int RULE_TYPE_DISABLED          =  5;

   /**
    * Enumeration of the possible types of rules that may be evaluated.
    * <br>Hide from Choice Rules
    * <br><b>7</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
	public static final int RULE_TYPE_HIDDEN            =  6;

   /**
    * Enumeration of the possible types of rules that may be evaluated.
    * <br>Stop Forward Progress Rules
    * <br><b>8</b>
    * <br>[SEQUENCING SUBSYSTEM CONSTANT]
    */
	public static final int RULE_TYPE_FORWARDBLOCK      =  7;
   
   /**
    * This controls display of log messages to the java console
    */
   private static boolean _Debug = DebugIndicator.ON;

   /**
    * This is the set of sequencing rules defined for an activity
    */
   private List mRules = null;

   
   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
   Constructors 
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
   // Default constructor, does nothing explicitly
   public SeqRuleset() {}
   
   /**
    * Initializes a set of sequencing rules.
    * 
    * @param iRules      Set of preconstructed sequencing rules (<code>SeqRule
    *                    </code>).
    */
   public SeqRuleset(List iRules)
   {

      if ( _Debug )
      {
         System.out.println("  :: SeqRuleset  --> BEGIN - constructor");

         for ( int i = 0; i < iRules.size(); i++ )
         {
            ISeqRule temp = (ISeqRule)iRules.get(i);

            temp.dumpState();
         }
      }

      mRules = iRules;

      if ( _Debug )
      {
         System.out.println("  :: SeqRuleset  --> END   - constructor");
      }
   }

   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Public Methods 
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
   /**
    * Evaluates this set of sequencing rules for the target activity and the
    * desired time of evaluation.
    * 
    * @param iType          Indicates the type of sequencing rules to be evaluat
    * 
    * @param iThisActivity The target activity of the rule evaluation.
    * 
    * @param iRetry         Indicates that this rule is being evaluated during
    *                       a Retry sequencing request process.
    * 
    * @return A sequencing request (<code>String</code>) or <code>null</code>.
    * @see org.adl.sequencer.SeqRuleset
    */
   public String evaluate(int iType, ISeqActivity iThisActivity, boolean iRetry)
   {
      if ( _Debug )
      {
         System.out.println("  :: SeqRuleset   --> BEGIN - evaluate");
         System.out.println("  ::-->  " + iType);
      }

      String action = null;

      // Evaluate all sequencing rules of type 'iType'.
      // Evaluation stops at the first rule that evaluates to true 
      if ( mRules != null )
      {
         boolean cont = true;

         for ( int i = 0; i < mRules.size() && cont; i++ )
         {

            SeqRule rule = (SeqRule)mRules.get(i);
            String result = rule.evaluate(iType, iThisActivity, iRetry);

            if ( !result.equals(SeqRule.SEQ_ACTION_NOACTION) )
            {
               cont = false;
               action = result;
            }
         }
      }

      if ( _Debug )
      {
         System.out.println("  ::--> " + action);
         System.out.println("  :: SeqRuleset   --> END   - evaluate");
      }

      return action;
   }


   /**
    * Describes the number of rollup rules in this set
    * 
    * @return The count of rollup rules in this set.
    */
   public int size()
   {
      if ( mRules != null )
      {
         return mRules.size();
      }

      return 0;
   }

}  // end SeqRuleset
