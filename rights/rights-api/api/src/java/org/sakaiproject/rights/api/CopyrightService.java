package org.sakaiproject.rights.api;

public interface CopyrightService 
{
	/**
	 * @param entityRef
	 * @param rights
	 */
	public void setRightsAssignment(String entityRef, RightsAssignment rights);
	
	/**
	 * @param entityRef
	 * @return
	 */
	public RightsAssignment getRightsAssignment(String entityRef);
	
	/**
	 * @param entityRef
	 * @return
	 */
	public RightsAssignment addRightsAssignment(String entityRef);
	
}	// interface CopyrightService
