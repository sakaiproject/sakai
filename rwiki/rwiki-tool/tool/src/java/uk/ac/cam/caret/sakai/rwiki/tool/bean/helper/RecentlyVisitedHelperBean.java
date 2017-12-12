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
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import uk.ac.cam.caret.sakai.rwiki.tool.bean.RecentlyVisitedBean;

/**
 * Helper bean to get a RecentlyVistedBean from a request.
 * 
 * @author andrew
 */
@Slf4j
public class RecentlyVisitedHelperBean
{

	/**
	 * Session attribute to save the recentlyVisitedBean
	 */
	public static final String RECENT_VISIT_ATTR = "recentlyVisitedBean";

	private ServletRequest request;

	private RecentlyVisitedBean recentBean;

	private String defaultSpace;

	/**
	 * Sets the recently visited bean using the set request and logger and
	 * default space.
	 */
	public void init()
	{
		recentBean = RecentlyVisitedHelperBean.getRecentlyVisitedBean(
				(HttpServletRequest) request, defaultSpace);
	}

	/**
	 * Set the current request
	 * 
	 * @param servletRequest
	 */
	public void setServletRequest(ServletRequest servletRequest)
	{
		this.request = servletRequest;
	}

	/**
	 * Set the default space
	 * 
	 * @param defaultSpace
	 */
	public void setDefaultSpace(String defaultSpace)
	{
		this.defaultSpace = defaultSpace;
	}

	/**
	 * Retrieve the current <code>RecentlyVisitedBean</code> from the passed
	 * in <code>ServletRequest</code> or create one in the default space.
	 * 
	 * @param request
	 *        current servlet request
	 * @param log
	 *        current logger
	 * @param defaultSpace
	 *        defaultSpace to for the RecentlyVisitedBean
	 * @return RecentlyVisitedBean
	 */
	public static RecentlyVisitedBean getRecentlyVisitedBean(
			HttpServletRequest request, String defaultSpace)
	{
		HttpSession session = request.getSession();
		RecentlyVisitedBean bean = null;
		try
		{
			bean = (RecentlyVisitedBean) session
					.getAttribute(RECENT_VISIT_ATTR);
		}
		catch (ClassCastException e)
		{
			log.warn("Session contains object at " + RECENT_VISIT_ATTR
					+ " which is not a valid breadcrumb bean\n" + "Object is: "
					+ session.getAttribute(RECENT_VISIT_ATTR).toString());
		}

		if (bean == null)
		{
			bean = new RecentlyVisitedBean(defaultSpace);
			session.setAttribute(RECENT_VISIT_ATTR, bean);
		}

		return bean;
	}

	/**
	 * Get the retrieved recently visited bean
	 * 
	 * @return recentlyVisitedBean
	 */
	public RecentlyVisitedBean getRecentlyVisitedBean()
	{
		return recentBean;
	}

}
