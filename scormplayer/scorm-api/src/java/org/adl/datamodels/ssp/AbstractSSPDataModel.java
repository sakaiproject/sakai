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
package org.adl.datamodels.ssp;

import java.net.URL;
import java.util.List;

import org.adl.datamodels.DataModel;

public abstract class AbstractSSPDataModel extends DataModel {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	/**
	    * Describes the learner this data model is associated with.
	    */
	protected String mLearnerID = null;

	/**
	 * Describes the sco this data model is associated with.
	 */
	protected String mSCOID = null;

	/**
	 * Describes the learner this data model is associated with.
	 */
	protected URL mURL = null;

	/**
	 * Describes the course this data model is associated with.
	 */
	protected String mCourseID = null;

	/**
	 * Describes the learner attempt # this data model is associated with.
	 */
	protected String mAttemptNum = null;

	/**
	 * Describes the dot-notation binding string for this data model.
	 */
	protected String mBinding = "ssp";

	/**
	 * Describes the data model elements managed by this data model.
	 */
	protected List<?> mManaged = null;

	public List<?> getManagedElements() {
		return mManaged;
	}

}
