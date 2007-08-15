package org.adl.sequencer;

public interface ISeqRuleset {

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
	public String evaluate(int iType, ISeqActivity iThisActivity, boolean iRetry);

	/**
	 * Describes the number of rollup rules in this set
	 * 
	 * @return The count of rollup rules in this set.
	 */
	public int size();

}