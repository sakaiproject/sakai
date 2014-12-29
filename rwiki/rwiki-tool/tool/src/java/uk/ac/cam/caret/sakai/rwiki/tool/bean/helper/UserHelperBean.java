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

package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Gets the current user from the request
 * 
 * @author andrew
 */
public class UserHelperBean
{

	private ServletRequest request;

	private String user;

	/**
	 * initialises the bean and sets the current user
	 */
	public void init()
	{
		user = ((HttpServletRequest) request).getRemoteUser();
	}

	/**
	 * Get the current user as string
	 * 
	 * @return user as string
	 */
	public String getUser()
	{
		return user;
	}

	/**
	 * Set the current servletRequest
	 * 
	 * @param servletRequest
	 */
	public void setServletRequest(ServletRequest servletRequest)
	{
		this.request = servletRequest;
	}

	/**
	 * Get the current servletRequest
	 * 
	 * @return current servletRequest
	 */
	public ServletRequest getServletRequest()
	{
		return request;
	}
}
