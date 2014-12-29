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

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.DiffBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.HistoryBean;

/**
 * Relatively short lived bean to help create a DiffBean. This is currently used
 * by the <code>RequestScopeSuperBean</code>, and was used by the
 * RequestScopeApplicationContext before it became too slow.
 * 
 * @author andrew
 */
public class DiffHelperBean
{

	/**
	 * current servletRequest
	 */
	private ServletRequest request;

	/**
	 * current rwikiObject
	 */
	private RWikiObject rwikiObject;

	private RWikiObjectService rwikiObjectService;

	/**
	 * DiffBean that was created
	 */
	private DiffBean diffBean;

	/**
	 * initialise the DiffHelperBean, effectively creating a new diffBean for
	 * the rwikiObject
	 */
	public void init()
	{
		String left = request.getParameter(HistoryBean.LEFT_PARAM);
		String right = request.getParameter(HistoryBean.RIGHT_PARAM);
		diffBean = new DiffBean(rwikiObject, rwikiObjectService, left, right);
	}

	/**
	 * Set the current servletRequest
	 * 
	 * @param servletRequest
	 *        current request to respond to
	 */
	public void setServletRequest(ServletRequest servletRequest)
	{
		this.request = servletRequest;
	}

	/**
	 * Get the currently selected rwikiObject
	 * 
	 * @return rwikiObject
	 */
	public RWikiObject getRwikiObject()
	{
		return rwikiObject;
	}

	/**
	 * Set the RWikiObject for the diffBean
	 * 
	 * @param rwikiObject
	 *        RWikiObject for the diffs
	 */
	public void setRwikiObject(RWikiObject rwikiObject)
	{
		this.rwikiObject = rwikiObject;
	}

	/**
	 * Get the DiffBean for the current RWikiObject and the request
	 * 
	 * @return
	 */
	public DiffBean getDiffBean()
	{
		return diffBean;
	}

	/**
	 * Set the diffBean for this DiffHelperBean
	 * 
	 * @param diffBean
	 */
	public void setDiffBean(DiffBean diffBean)
	{
		this.diffBean = diffBean;
	}

	public RWikiObjectService getRwikiObjectService()
	{
		return rwikiObjectService;
	}

	public void setRwikiObjectService(RWikiObjectService rwikiObjectService)
	{
		this.rwikiObjectService = rwikiObjectService;
	}

}
