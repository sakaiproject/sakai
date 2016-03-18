/*
 * #%L
 * SCORM API
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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