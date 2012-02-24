/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.service;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.portal.api.StoredState;
import org.sakaiproject.tool.api.Placement;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */

public class StoredStateImpl implements StoredState
{
	private SessionRequestHolder requestHolder = null;

	private Placement placement = null;

	private String toolContextPath = null;

	private String toolPathInfo = null;

	private String marker;

	private String replacement;

	private String skin;

	public StoredStateImpl(String marker, String replacement)
	{
		this.marker = marker;
		this.replacement = replacement;
	}

	public Placement getPlacement()
	{
		return placement;
	}

	public void setPlacement(Placement placement)
	{
		this.placement = placement;
	}

	public HttpServletRequest getRequest(HttpServletRequest currentRequest)
	{
		return new RecoveredServletRequest(currentRequest, requestHolder);
	}

	public void setRequest(HttpServletRequest request)
	{
		this.requestHolder = new SessionRequestHolder(request, marker, replacement);
	}

	public String getToolContextPath()
	{
		return toolContextPath;
	}

	public void setToolContextPath(String toolContextPath)
	{
		if (toolContextPath != null)
		{
			this.toolContextPath = PortalStringUtil.replaceFirst(toolContextPath, marker,
					replacement);
		}
		else
		{
			this.toolContextPath = null;
		}
	}

	public String getToolPathInfo()
	{
		return toolPathInfo;
	}

	public void setToolPathInfo(String toolPathInfo)
	{
		if (toolPathInfo != null)
		{
			this.toolPathInfo = PortalStringUtil.replaceFirst(toolPathInfo, marker,
					replacement);
		}
		else
		{
			this.toolPathInfo = null;
		}
	}

	public void setSkin(String skin)
	{
		this.skin = skin;

	}

	public String getSkin()
	{
		return skin;
	}

}