package org.adl.sequencer;

public interface ILaunch {

	public IValidRequests getNavState();
	
	/**
	 * This method provides the state this <code>ADLLaunch</code> object
	 * for diagnostic purposes.<br>
	 */
	public void dumpState();

}