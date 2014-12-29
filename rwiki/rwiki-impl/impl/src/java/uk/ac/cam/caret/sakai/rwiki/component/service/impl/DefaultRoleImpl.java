/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

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
