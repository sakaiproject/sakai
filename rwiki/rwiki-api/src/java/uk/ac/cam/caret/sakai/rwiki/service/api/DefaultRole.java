package uk.ac.cam.caret.sakai.rwiki.service.api;

import java.util.Map;

//FIXME: Service, remove

public interface DefaultRole {

	/**
	 * @return Returns the enabledFunctions.
	 */
	public abstract Map getEnabledFunctions();

	/**
	 * @param enabledFunctions The enabledFunctions to set.
	 */
	public abstract void setEnabledFunctions(Map enabledFunctions);

	/**
	 * @return Returns the roleId.
	 */
	public abstract String getRoleId();

	/**
	 * @param roleId The roleId to set.
	 */
	public abstract void setRoleId(String roleId);

}