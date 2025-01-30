package org.adl.sequencer;

public interface ISeqRollupRuleset {

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	 
	 Public Methods 
	 
	 -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
	/**
	 * Evaluates this set of rollup rules for the target activity.  
	 *
	 * @param ioThisActivity The target activity of the rollup evaluation.
	 */
	public void evaluate(ISeqActivity ioThisActivity);

	/**
	 * Describes the number of rollup rules in this set
	 * 
	 * @return The count of rollup rules in this set.
	 */
	public int size();

}