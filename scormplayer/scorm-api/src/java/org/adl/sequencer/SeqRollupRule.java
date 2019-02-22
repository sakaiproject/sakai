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

import org.adl.util.debug.DebugIndicator;

/**
 * Encapsulates one rollup rule.<br><br>
 * 
 * <strong>Filename:</strong> SeqRollupRule.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This class encapsulates the Sequencing Definition Model element SM.5.  It
 * describes one rollup rule.<br><br>
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
public class SeqRollupRule implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;

	/**
	 * Enumeration of possible rollup actions -- described in element 5.4 of the
	 * IMS SS Specification.
	 * <br>No Status Change
	 * <br><b>0</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int ROLLUP_ACTION_NOCHANGE = 0;

	/**
	 * Enumeration of possible rollup actions -- described in element 5.4 of the
	 * IMS SS Specification.
	 * <br>Satisfied
	 * <br><b>1</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int ROLLUP_ACTION_SATISFIED = 1;

	/**
	 * Enumeration of possible rollup actions -- described in element 5.4 of the
	 * IMS SS Specification.
	 * <br>Not Satisfied
	 * <br><b>2</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int ROLLUP_ACTION_NOTSATISFIED = 2;

	/**
	 * Enumeration of possible rollup actions -- described in element 5.4 of the
	 * IMS SS Specification.
	 * <br>Completed
	 * <br><b>3</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int ROLLUP_ACTION_COMPLETED = 3;

	/**
	 * Enumeration of possible rollup actions -- described in element 5.4 of the
	 * IMS SS Specification.
	 * <br>Incomplete
	 * <br><b>4</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int ROLLUP_ACTION_INCOMPLETE = 4;

	/**
	 * Enumeration of rollup consideration controls
	 * These are SCORM extensions to IMS SS defined in the SCORM Sequencing and
	 * Navigation book
	 * <br>Always
	 * <br><b>"always"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String ROLLUP_CONSIDER_ALWAYS = "always";

	/**
	 * Enumeration of rollup consideration controls
	 * These are SCORM extensions to IMS SS defined in the SCORM Sequencing and
	 * Navigation book
	 * <br>If Attempted
	 * <br><b>"ifAttempted"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String ROLLUP_CONSIDER_ATTEMPTED = "ifAttempted";

	/**
	 * Enumeration of rollup consideration controls
	 * These are SCORM extensions to IMS SS defined in the SCORM Sequencing and
	 * Navigation book
	 * <br>If Not Skipped
	 * <br><b>"ifNotSkipped"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String ROLLUP_CONSIDER_NOTSKIPPED = "ifNotSkipped";

	/**
	 * Enumeration of rollup consideration controls
	 * These are SCORM extensions to IMS SS defined in the SCORM Sequencing and
	 * Navigation book
	 * <br>If Not Suspended
	 * <br><b>"ifNotSuspended"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String ROLLUP_CONSIDER_NOTSUSPENDED = "ifNotSuspended";

	/**
	 * Enumeration of rollup rule set qualifiers -- described in element 5.1 of
	 * the IMS SS Specification.
	 * <br>All
	 * <br><b>"All"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String ROLLUP_SET_ALL = "all";

	/**
	 * Enumeration of rollup rule set qualifiers -- described in element 5.1 of
	 * the IMS SS Specification.
	 * <br>Any
	 * <br><b>"Any"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String ROLLUP_SET_ANY = "any";

	/**
	 * Enumeration of rollup rule set qualifiers -- described in element 5.1 of
	 * the IMS SS Specification.
	 * <br>None
	 * <br><b>"None"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String ROLLUP_SET_NONE = "none";

	/**
	 * Enumeration of rollup rule set qualifiers -- described in element 5.1 of
	 * the IMS SS Specification.
	 * <br>At Least Count
	 * <br><b>"MinimumSet"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String ROLLUP_SET_ATLEASTCOUNT = "atLeastCount";

	/**
	 * Enumeration of rollup rule set qualifiers -- described in element 5.1 of
	 * the IMS SS Specification.
	 * <br>At Least Percent
	 * <br><b>"MinimumPercent"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String ROLLUP_SET_ATLEASTPERCENT = "atLeastPercent";

	/**
	 * This controls display of log messages to the java console
	 */
	private static boolean _Debug = DebugIndicator.ON;

	/**
	 * This describes the rollup rule action (element 5.4)
	 */
	public int mAction = SeqRollupRule.ROLLUP_ACTION_SATISFIED;

	/**
	 * This describes the rollup rule Child Activity Set (element 5.1)
	 */
	public String mChildActivitySet = SeqRollupRule.ROLLUP_SET_ALL;

	/**
	 * This describes the rollup rule Minimum Count (element 5.1.1)
	 */
	public long mMinCount = 0;

	/**
	 * This describes the rollup rule Minimum Percent (element 5.1.2)
	 */
	public double mMinPercent = 0.0;

	/**
	 * This describes the rollup rule conditions (element 5.2)
	 */
	public SeqConditionSet mConditions = null;

	/**
	 * Default constructor
	 */
	public SeqRollupRule() {
		// Default constructor
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Public Methods 
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * This method provides the state this <code>SeqRollupRule</code> object for
	 * diagnostic purposes.
	 */
	public void dumpState() {
		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> BEGIN - dumpState");

			System.out.println("  ::--> Action      : " + mAction);
			System.out.println("  ::--> Set         : " + mChildActivitySet);
			System.out.println("  ::--> minCount    : " + mMinCount);
			System.out.println("  ::--> minPercent  : " + mMinPercent);
			System.out.println("  ------------------- ");

			if (mConditions != null) {
				mConditions.dumpState();
			} else {
				System.out.println("  ::--> NULL conditions");
			}

			System.out.println("  :: SeqRollupRule  --> END   - dumpState");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		SeqRollupRule other = (SeqRollupRule) obj;
		if (id != other.id){
			return false;
		}
		return true;
	}

	/**
	 * Evaluates this rollup rule using its declared parameters.
	 * 
	 * @param iChildren Set of child activities (<code>SeqActivity</code>) used
	 *                  during the evaluation of this rollup rule.
	 * 
	 * @return The resulting status change caused by the evaluation of the rule.
	 *         This value is a member of the <code>ROLLUP_ACTION_XXX</code>
	 *         enumeration.
	 */
	public int evaluate(List<SeqActivity> iChildren) {

		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> BEGIN - evaluate");
		}

		// Evaluate 'this' rollup rule, using the activity's children
		boolean result = false;

		if (mChildActivitySet.equals(SeqRollupRule.ROLLUP_SET_ALL)) {
			result = evaluateAll(iChildren);
		} else if (mChildActivitySet.equals(SeqRollupRule.ROLLUP_SET_ANY)) {
			result = evaluateAny(iChildren);
		} else if (mChildActivitySet.equals(SeqRollupRule.ROLLUP_SET_NONE)) {
			result = evaluateNone(iChildren);
		} else if (mChildActivitySet.equals(SeqRollupRule.ROLLUP_SET_ATLEASTCOUNT)) {
			result = evaluateMinCount(iChildren);
		} else if (mChildActivitySet.equals(SeqRollupRule.ROLLUP_SET_ATLEASTPERCENT)) {
			result = evaluateMinPercent(iChildren);
		}

		int action = SeqRollupRule.ROLLUP_ACTION_NOCHANGE;

		if (result) {
			action = mAction;
		}

		if (_Debug) {
			System.out.println("  ::-->  " + action);
			System.out.println("  :: SeqRollupRule  --> END - evaluate");
		}

		return action;
	}

	/**
	 * Evaluates this rollup rule requiring ALL children to have their status
	 * values equal to the status qualifier.
	 * 
	 * @param iChildren Set of child activities (<code>SeqActivity</code>) used
	 *                  during the evaluation of this rollup rule.
	 * 
	 * @return <code>true</code> if the rollup rule evaluates to 'True',
	 *         otherwise <code>false</code>.
	 */
	private boolean evaluateAll(List<SeqActivity> iChildren) {
		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> BEGIN - evaluateAll");
		}

		boolean result = true;
		SeqActivity tempActivity = null;

		int i = 0;
		while (result && i < iChildren.size()) {
			// Look at the next child for evaluation 
			tempActivity = iChildren.get(i);

			// Make sure the child is included in rollup 
			if (isIncluded(tempActivity)) {
				result = result && (mConditions.evaluate(tempActivity) == SeqConditionSet.EVALUATE_TRUE);
			}

			i++;
		}

		if (_Debug) {
			System.out.println("  ::-->  " + result);
			System.out.println("  :: SeqRollupRule  --> END   - evaluateAll");
		}

		return result;
	}

	/**
	 * Evaluates this rollup rule requiring ANY ONE of its children to have a
	 * status value equal to the status qualifier.
	 * 
	 * @param iChildren Set of child activities (<code>SeqActivity</code>) used
	 *                  during the evaluation of this rollup rule.
	 * 
	 * @return <code>true</code> if the rollup rule evaluates to 'True',
	 *         otherwise <code>false</code>.
	 */
	private boolean evaluateAny(List<SeqActivity> iChildren) {
		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> BEGIN - evaluateAny");
		}

		boolean result = false;
		SeqActivity tempActivity = null;

		int i = 0;
		while ((!result) && i < iChildren.size()) {
			// Look at the next child for evaluation 
			tempActivity = iChildren.get(i);

			// Make sure the child is included in rollup 
			if (isIncluded(tempActivity)) {
				result = result || (mConditions.evaluate(tempActivity) == SeqConditionSet.EVALUATE_TRUE);
			}

			i++;
		}

		if (_Debug) {
			System.out.println("  ::-->  " + result);
			System.out.println("  :: SeqRollupRule  --> END   - evaluateAny");
		}

		return result;
	}

	/**
	 * Evaluates this rollup rule requiring the children having their status
	 * values equal to the status qualifier be a number equal to or greater than
	 * the Minimum Child Set value.
	 * 
	 * @param iChildren Set of child activities (<code>SeqActivity</code>) used
	 *                  during the evaluation of this rollup rule.
	 * 
	 * @return <code>true</code> if the rollup rule evaluates to 'True',
	 *         otherwise <code>false</code>.
	 */
	private boolean evaluateMinCount(List<SeqActivity> iChildren) {

		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> BEGIN - evaluateMinSize");
		}

		long count = 0;
		SeqActivity tempActivity = null;

		int i = 0;
		while ((count < mMinCount) && i < iChildren.size()) {
			// Look at the next child for evaluation 
			tempActivity = iChildren.get(i);

			// Make sure the child is included in rollup 
			if (isIncluded(tempActivity)) {
				if (mConditions.evaluate(tempActivity) == SeqConditionSet.EVALUATE_TRUE) {
					count++;
				}
			}

			i++;
		}

		boolean result = (count >= mMinCount);

		if (_Debug) {
			System.out.println("  ::-->  " + result);
			System.out.println("  :: SeqRollupRule  --> END   - evaluateMinSize");
		}

		return result;
	}

	/**
	 * Evaluates this rollup rule requiring number of children having their statu
	 * values equal to the status qualifier be a percentage equal to or greater
	 * than MinimumSize.
	 * 
	 * @param iChildren Set of child activities (<code>SeqActivity</code>) used
	 *                  during the evaluation of this rollup rule.
	 * 
	 * @return <code>true</code> if the rollup rule evaluates to 'True',
	 *         otherwise <code>false</code>.
	 */
	private boolean evaluateMinPercent(List<SeqActivity> iChildren) {
		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> BEGIN - " + "evaluateMinPercent");
		}

		long countAll = 0;
		long count = 0;
		SeqActivity tempActivity = null;

		int i = 0;
		while (i < iChildren.size()) {
			// Look at the next child for evaluation 
			tempActivity = iChildren.get(i);

			// Make sure the child is included in rollup 
			if (isIncluded(tempActivity)) {
				countAll++;

				if (mConditions.evaluate(tempActivity) == SeqConditionSet.EVALUATE_TRUE) {
					count++;
				}
			}

			i++;
		}

		boolean result = (count >= (long) ((mMinPercent * countAll) + 0.5));

		if (_Debug) {
			System.out.println("  ::-->  " + result);
			System.out.println("  :: SeqRollupRule  --> END   - " + "evaluateMinPercent");
		}
		return result;
	}

	/**
	 * Evaluates this rollup rule requiring NONE children to have their status
	 * values equal to the status qualifier.
	 * 
	 * @param iChildren Set of child activities (<code>SeqActivity</code>) used
	 *                  during the evaluation of this rollup rule.
	 * 
	 * @return <code>true</code> if the rollup rule evaluates to 'True',
	 *         otherwise <code>false</code>.
	 */
	private boolean evaluateNone(List<SeqActivity> iChildren) {
		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> BEGIN - evaluateNone");
		}

		boolean result = true;
		SeqActivity tempActivity = null;

		int i = 0;
		while (result && i < iChildren.size()) {
			// Look at the next child for evaluation 
			tempActivity = iChildren.get(i);

			// Make sure the child is included in rollup 
			if (isIncluded(tempActivity)) {
				int eval = mConditions.evaluate(tempActivity);

				result = result && !(eval == SeqConditionSet.EVALUATE_TRUE || eval == SeqConditionSet.EVALUATE_UNKNOWN);
			}

			i++;
		}

		if (_Debug) {
			System.out.println("  ::-->  " + result);
			System.out.println("  :: SeqRollupRule  --> END   - evaluateNone");
		}

		return result;
	}

	public long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	   
	    Private Methods 
	   
	   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
	/**
	 * Determines if the target activity should be included during the evaluation
	 * of its parent's rollup rules.
	 * 
	 * @param iActivity The activity being considered
	 * 
	 * @return <code>true</code> if the activity should be included in the
	 *         evaluation of its parent's rollup, otherwise <code>false</code>.
	 */
	private boolean isIncluded(SeqActivity iActivity) {

		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> BEGIN - isIncluded");
			System.out.println("  ::-->  " + iActivity.getID());
		}

		// Assume all children are included in rollup
		boolean include = true;

		// Make sure the activity is tracked
		if (iActivity.getIsTracked()) {

			// Make sure the delivery mode is 'Normal'
			if (iActivity.getDeliveryMode().equals("normal")) {

				if (mAction == SeqRollupRule.ROLLUP_ACTION_SATISFIED || mAction == SeqRollupRule.ROLLUP_ACTION_NOTSATISFIED) {
					include = iActivity.getIsObjRolledUp();
				} else if (mAction == SeqRollupRule.ROLLUP_ACTION_COMPLETED || mAction == SeqRollupRule.ROLLUP_ACTION_INCOMPLETE) {
					include = iActivity.getIsProgressRolledUp();
				}
			} else {
				include = false;
			}
		} else {
			include = false;
		}

		// Check 'Is Required For' SCORM Sequencing extensions
		if (include) {
			String consider = null;

			switch (mAction) {
			case SeqRollupRule.ROLLUP_ACTION_SATISFIED:

				consider = iActivity.getRequiredForSatisfied();
				break;

			case SeqRollupRule.ROLLUP_ACTION_NOTSATISFIED:

				consider = iActivity.getRequiredForNotSatisfied();
				break;

			case SeqRollupRule.ROLLUP_ACTION_COMPLETED:

				consider = iActivity.getRequiredForCompleted();
				break;

			case SeqRollupRule.ROLLUP_ACTION_INCOMPLETE:

				consider = iActivity.getRequiredForIncomplete();
				break;

			default:

				include = false;

				if (_Debug) {
					System.out.println("  ::--> ERROR :: Invalid rollup " + "action");
				}
			}

			if (consider != null) {
				if (consider.equals(SeqRollupRule.ROLLUP_CONSIDER_NOTSUSPENDED)) {
					if (_Debug) {
						System.out.println("  ::--> Looking At Not Suspended");
					}

					if (iActivity.getActivityAttempted() && iActivity.getIsSuspended()) {
						include = false;
					}

				} else if (consider.equals(SeqRollupRule.ROLLUP_CONSIDER_ATTEMPTED)) {
					if (_Debug) {
						System.out.println("  ::--> Looking At Attempted");
					}

					include = iActivity.getActivityAttempted();

				} else if (consider.equals(SeqRollupRule.ROLLUP_CONSIDER_NOTSKIPPED)) {
					if (_Debug) {
						System.out.println("  ::--> Looking At Not Skipped");
					}

					// Check if the activity should be 'skipped'.

					// Attempt to get rule information from the activity node
					ISeqRuleset skippedRules = iActivity.getPreSeqRules();
					String result = null;

					if (skippedRules != null) {
						result = skippedRules.evaluate(SeqRuleset.RULE_TYPE_SKIPPED, iActivity, false);
					}

					// If the rule evaluation did not return null,
					// the activity is skipped, don't include it in rollup
					if (result != null) {
						include = false;
					}
				} else {
					if (_Debug) {
						System.out.println("  ::--> Looking At Always");
					}

					include = true;
				}

			}
		}

		if (_Debug) {
			System.out.println("  ::-->  " + include);
			System.out.println("  :: SeqRollupRule  --> END   - isIncluded");
		}

		return include;
	}

	/**
	    * Translates a rule action represented as a <code>String</code> into the
	    * coresponding element of the <code>ROLLUP_ACTION_XXX_</code> enumeration.
	    * 
	    * @param iAction The <code>String</code> representation of this rule's
	    *                action.
	    */
	public void setRollupAction(String iAction) {

		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> BEGIN - " + "setRollupAction");
			System.out.println("  :: " + iAction);
		}

		if (iAction.equals("satisfied")) {
			mAction = SeqRollupRule.ROLLUP_ACTION_SATISFIED;
		} else if (iAction.equals("notSatisfied")) {
			mAction = SeqRollupRule.ROLLUP_ACTION_NOTSATISFIED;
		} else if (iAction.equals("completed")) {
			mAction = SeqRollupRule.ROLLUP_ACTION_COMPLETED;
		} else if (iAction.equals("incomplete")) {
			mAction = SeqRollupRule.ROLLUP_ACTION_INCOMPLETE;
		} else {
			if (_Debug) {
				System.out.println("  ::--> ERROR : Invalid Action");
			}
		}

		if (_Debug) {
			System.out.println("  :: SeqRollupRule  --> END   - " + "setRollupAction");
		}
	}
} // end SeqRollupRule
