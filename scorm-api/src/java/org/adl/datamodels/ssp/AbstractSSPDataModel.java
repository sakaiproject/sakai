package org.adl.datamodels.ssp;

import java.net.URL;
import java.util.List;

import org.adl.datamodels.DMElement;
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
	protected List<DMElement> mManaged = null;

	public List<DMElement> getManagedElements() {
		return mManaged;
	}

}
