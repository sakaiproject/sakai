package org.sakaiproject.scorm.model.api;

import org.adl.sequencer.SeqActivity;

public class SeqActivityTreeSnapshot {

	private Long id;

	/**
	 * This is the course identifier for the current activity tree
	 */
	protected String mCourseID;

	/**
	 * This is last activity experienced by the student prior to a SuspendAll
	 * sequencing request.<br><br>
	 * Defined in IMS SS AM
	 */
	protected SeqActivity mSuspendAll;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SeqActivityTreeSnapshot other = (SeqActivityTreeSnapshot) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getCourseId() {
		return mCourseID;
	}

	public String getCourseID() {
		return mCourseID;
	}

	public String getmCourseID() {
		return mCourseID;
	}

	/**
	 * Retrieves the activity (<code>SeqActivity</code>) associated with the
	 * last attempted activity before a 'SuspendAll' sequencing request.
	 * 
	 * @return The activity (<code>SeqActivity</code>) associated with the last
	 *         attempted activity or <code>null</code> if none exists.
	 */
	public SeqActivity getSuspendAll() {
		return mSuspendAll;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public void setCourseID(String courseID) {
		mCourseID = courseID;
	}

	public void setmCourseID(String mCourseID) {
		this.mCourseID = mCourseID;
	}

	public void setSuspendAll(SeqActivity suspendAll) {
		mSuspendAll = suspendAll;
	}

}
