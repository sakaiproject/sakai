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
 * Encapsulation of a set of conditions used to evaluate sequencing and rollup
 * rules.<br><br>
 * 
 * <strong>Filename:</strong> SeqConditionSet.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This is an implementation of rule condition evaluation used for sequencing
 * and rollup rules.<br><br>
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
 *     <li>IMS SS Specification
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class SeqConditionSet implements Serializable {

	private long id;

	static final long serialVersionUID = 1L; // -8248834341813485224L;

	/**
	 * Enumeration of possible evaluation results.
	 * <br>unknown
	 * <br><b>0</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int EVALUATE_UNKNOWN = 0;

	/**
	 * Enumeration of possible evaluation results.
	 * <br>true
	 * <br><b>1</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int EVALUATE_TRUE = 1;

	/**
	 * Enumeration of possible evaluation results.
	 * <br>false
	 * <br><b>-1</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static final int EVALUATE_FALSE = -1;

	/**
	 * Enumeration of possible evaluation criteria -- described in Sequencing
	 * Rule Description (element 2.1) and Rollup Rule Description (element 5.2)
	 * of the IMS SS Specification.
	 * <br>All
	 * <br><b>"all"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String COMBINATION_ALL = "all";

	/**
	 * Enumeration of possible evaluation criteria -- described in Sequencing
	 * Rule Description (element 2.1) and Rollup Rule Description (element 5.2)
	 * of the IMS SS Specification.
	 * <br>Any
	 * <br><b>"any"</b>
	 * <br>[SEQUENCING SUBSYSTEM CONSTANT]
	 */
	public static String COMBINATION_ANY = "any";

	/**
	 * This controls display of log messages to the java console
	 */
	private static boolean _Debug = DebugIndicator.ON;

	/**
	 * Describes the evaluation criteria for this set of conditions.
	 */
	public String mCombination = null;

	/**
	 * Describes the set of conditions required for this rule's action to take
	 * effect.
	 */
	public List<SeqCondition> mConditions = null;

	/**
	 * Describes if the current condition evaluation is part of a 'retry' 
	 * sequencing request.
	 */
	private boolean mRetry = false;

	/**
	 * Describes if these conditions are being evaluated in a Rollup Rule
	 */
	private boolean mRollup = false;

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	Constructors 
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
	/**
	 * Default constructor
	 */
	public SeqConditionSet() {
	}

	public SeqConditionSet(boolean iRollup) {
		mRollup = iRollup;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Public Methods 
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * This method provides the state this <code>SeqConditionSet</code> object
	 * for diagnostic purposes.
	 */
	public void dumpState() {
		if (_Debug) {
			System.out.println("  :: SeqConditionSet  --> BEGIN - dumpState");

			System.out.println("  ::--> Set : " + mCombination);

			if (mConditions != null) {
				System.out.println("  ::-->   [" + mConditions.size() + "]");
				System.out.println("  ::----------------::");

				for (int i = 0; i < mConditions.size(); i++) {
					SeqCondition cond = mConditions.get(i);

					cond.dumpState();
				}

			} else {
				System.out.println("         NULL");
				System.out.println("  ::----------------::");
			}

			System.out.println("  :: SeqConditionSet --> END   - dumpState");
		}
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Package Methods 
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

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
		SeqConditionSet other = (SeqConditionSet) obj;
		if (id != other.id){
			return false;
		}
		return true;
	}

	/**
	 * Evaluates this condition set
	 * 
	 * @param iThisActivity The activity being evaluated
	 * 
	 * @return The result of the condition set evaluation
	 */
	int evaluate(ISeqActivity iThisActivity) {

		if (_Debug) {
			System.out.println("  :: SeqConditionSet --> BEGIN - evaluate");
			System.out.println("  :: --> RETRY == " + mRetry);
		}

		int result = EVALUATE_UNKNOWN;

		// Make sure we have a valid target activity  
		if (iThisActivity != null) {

			if (_Debug) {
				System.out.println("  ::-->  Set - " + mCombination);

				if (mConditions != null) {
					System.out.println("  ::-->  [" + mConditions.size() + "]");
				} else {
					System.out.println("  ::-->  NULL");
				}
			}

			if (mConditions != null) {
				// Evaluate this rule's conditions
				if (mCombination.equals(COMBINATION_ALL)) {
					result = EVALUATE_TRUE;

					for (int i = 0; i < mConditions.size(); i++) {
						int thisEval = evaluateCondition(i, (SeqActivity) iThisActivity);

						if (thisEval != EVALUATE_TRUE) {
							result = thisEval;

							// done with this evaluation
							break;
						}
					}
				} else if (mCombination.equals(COMBINATION_ANY)) {
					// Assume we have enought information to evaluate
					result = EVALUATE_FALSE;

					for (int i = 0; i < mConditions.size(); i++) {
						int thisEval = evaluateCondition(i, (SeqActivity) iThisActivity);

						if (thisEval == EVALUATE_TRUE) {
							result = EVALUATE_TRUE;

							// done with this evaluation
							break;
						} else if (thisEval == EVALUATE_UNKNOWN) {
							// Something is missing...
							result = EVALUATE_UNKNOWN;
						}

					}
				}
			}
		}

		// Reset the 'retry' flag
		mRetry = false;

		if (_Debug) {
			System.out.println("  ::-->  " + result);
			System.out.println("  :: SeqConditionSet --> END   - evaluate");
		}

		return result;

	}

	/**
	 * Evaluates this condition set
	 * 
	 * @param iThisActivity The activity being evaluated
	 * 
	 * @param iIsRetry      Indicates if this evaluation is occuring during the
	 *                      processing of a 'retry' sequencing request.
	 * 
	 * @return The result of the condition set evaluation
	 */
	int evaluate(ISeqActivity iThisActivity, boolean iIsRetry) {
		mRetry = iIsRetry;

		return evaluate(iThisActivity);
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	   
	    Private Methods 
	   
	   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
	/**
	 * Evaluates one condition of this condition set.
	 * 
	 * @param iIndex  Index of the the condition to be evaluated.
	 * 
	 * @param iTarget The activity this condition is associated with.
	 * 
	 * @return <code>true</code> if the condition evaluates to 'true, othewise
	 *         <code>false</code>.
	 */
	private int evaluateCondition(int iIndex, SeqActivity iTarget) {

		if (_Debug) {
			System.out.println("  :: SeqConditionSet --> BEGIN - " + "evaluateCondition");
			System.out.println("  ::-->  " + iIndex);
		}

		int result = EVALUATE_UNKNOWN;

		// Make sure this condition exists
		if (iIndex < mConditions.size()) {

			SeqCondition cond = mConditions.get(iIndex);

			if (_Debug) {
				System.out.println("  ::--> Evaluate :: " + cond.mCondition);
			}

			// evaluate the current condtion
			if (cond.mCondition.equals(SeqCondition.ALWAYS)) {
				result = EVALUATE_TRUE;
			} else if (cond.mCondition.equals(SeqCondition.NEVER)) {
				result = EVALUATE_FALSE;
			} else if (cond.mCondition.equals(SeqCondition.SATISFIED)) {
				if (iTarget.getObjStatus(cond.mObjID, mRollup)) {
					result = (iTarget.getObjSatisfied(cond.mObjID, mRollup)) ? EVALUATE_TRUE : EVALUATE_FALSE;
				} else {
					result = EVALUATE_UNKNOWN;
				}
			} else if (cond.mCondition.equals(SeqCondition.OBJSTATUSKNOWN)) {
				result = iTarget.getObjStatus(cond.mObjID, mRollup) ? EVALUATE_TRUE : EVALUATE_FALSE;
			} else if (cond.mCondition.equals(SeqCondition.OBJMEASUREKNOWN)) {
				result = iTarget.getObjMeasureStatus(cond.mObjID, mRollup) ? EVALUATE_TRUE : EVALUATE_FALSE;
			} else if (cond.mCondition.equals(SeqCondition.OBJMEASUREGRTHAN)) {
				if (iTarget.getObjMeasureStatus(cond.mObjID, mRollup)) {
					result = (iTarget.getObjMeasure(cond.mObjID, mRollup) > cond.mThreshold) ? EVALUATE_TRUE : EVALUATE_FALSE;
				} else {
					result = EVALUATE_UNKNOWN;
				}
			} else if (cond.mCondition.equals(SeqCondition.OBJMEASURELSTHAN)) {
				if (iTarget.getObjMeasureStatus(cond.mObjID, mRollup)) {

					result = (iTarget.getObjMeasure(cond.mObjID, mRollup) < cond.mThreshold) ? EVALUATE_TRUE : EVALUATE_FALSE;
				} else {
					result = EVALUATE_UNKNOWN;
				}
			} else if (cond.mCondition.equals(SeqCondition.COMPLETED)) {
				if (iTarget.getProgressStatus(mRollup)) {
					result = iTarget.getAttemptCompleted(mRollup) ? EVALUATE_TRUE : EVALUATE_FALSE;
				} else {
					result = EVALUATE_UNKNOWN;
				}
			} else if (cond.mCondition.equals(SeqCondition.PROGRESSKNOWN)) {
				result = iTarget.getProgressStatus(mRollup) ? EVALUATE_TRUE : EVALUATE_FALSE;
			} else if (cond.mCondition.equals(SeqCondition.ATTEMPTED)) {
				result = iTarget.getActivityAttempted() ? EVALUATE_TRUE : EVALUATE_FALSE;
			} else if (cond.mCondition.equals(SeqCondition.ATTEMPTSEXCEEDED)) {
				if (iTarget.getAttemptLimitControl()) {
					long maxAttempt = iTarget.getAttemptLimit();

					// Check if this limit condition exists
					if (maxAttempt >= 0) {
						result = (iTarget.getNumAttempt() >= maxAttempt) ? EVALUATE_TRUE : EVALUATE_FALSE;
					}
				}
			} else if (cond.mCondition.equals(SeqCondition.TIMELIMITEXCEEDED)) {
				// add later with other time tracking implementation...
				// -+- TODO -+-
			} else if (cond.mCondition.equals(SeqCondition.OUTSIDETIME)) {

				// add later with other time tracking implementation...
				// -+- TODO -+-
			}

			// Account for condition operator
			if (cond.mNot && result != EVALUATE_UNKNOWN) {
				if (_Debug) {
					System.out.println("  ::--> Negate Result");
				}

				result = (result == EVALUATE_FALSE) ? EVALUATE_TRUE : EVALUATE_FALSE;
			}
		}

		if (_Debug) {
			System.out.println("  ::-->  " + ((result == 1) ? "True" : "False"));
			System.out.println("  :: SeqConditionSet --> END   - " + "evaluateCondition");
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

} // end SeqConditionSet
