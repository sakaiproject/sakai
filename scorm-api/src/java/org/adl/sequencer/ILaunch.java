package org.adl.sequencer;

public interface ILaunch {

	/**
	 * This method provides the state this <code>ADLLaunch</code> object
	 * for diagnostic purposes.<br>
	 */
	public void dumpState();

	public String getActivityId();

	public String getLaunchStatusNoContent();

	public IValidRequests getNavState();

	public String getSco();

}