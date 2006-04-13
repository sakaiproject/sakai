package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.util.Map;

import uk.ac.cam.caret.sakai.rwiki.service.api.DefaultRole;

// FIXME: Remove
public class DefaultRoleImpl implements DefaultRole
{
	private String roleId = null;

	private Map enabledFunctions = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.component.service.impl.DefaultRole#getEnabledFunctions()
	 */
	public Map getEnabledFunctions()
	{
		return enabledFunctions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.component.service.impl.DefaultRole#setEnabledFunctions(java.util.Map)
	 */
	public void setEnabledFunctions(Map enabledFunctions)
	{
		this.enabledFunctions = enabledFunctions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.component.service.impl.DefaultRole#getRoleId()
	 */
	public String getRoleId()
	{
		return roleId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.component.service.impl.DefaultRole#setRoleId(java.lang.String)
	 */
	public void setRoleId(String roleId)
	{
		this.roleId = roleId;
	}

}
