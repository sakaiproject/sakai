/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
// FIXME: Tool
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
