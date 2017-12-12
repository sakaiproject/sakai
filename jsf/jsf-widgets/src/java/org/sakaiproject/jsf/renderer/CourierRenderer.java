/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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


package org.sakaiproject.jsf.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.jsf.util.RendererUtil;
import org.sakaiproject.tool.api.SessionManager;

@Slf4j
public class CourierRenderer extends Renderer
{
	
	private SessionManager sessionManager = (SessionManager)
			ComponentManager.get(SessionManager.class);
	
	public boolean supportsComponentType(UIComponent component)
	{
		return (component instanceof UIOutput);
	}

	public void decode(FacesContext context, UIComponent component)
	{
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException
	{
	}

	public void encodeChildren(FacesContext context, UIComponent component) throws IOException
	{
	}


	public void encodeEnd(FacesContext context, UIComponent component) throws IOException
	{
		ResponseWriter writer = context.getResponseWriter();
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();

		// update time, in seconds
		String updateTime = (String) RendererUtil.getAttribute(context, component, "refresh");
		if (updateTime == null || updateTime.length() == 0)
		{
			updateTime = "10";
		}
		
		// the current tool's placement ID
		String placementId = (String) req.getAttribute("sakai.tool.placement.id");
		if (placementId == null)
		{
			try {
				throw new Exception("Placement ID is null in request: sakai.tool.placement.id");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		// the current user's ID
		String userId = sessionManager.getCurrentSessionUserId();
		if (userId == null)
		{
			try {
				throw new Exception("User ID is null in session");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		writer.write("<script type=\"text/javascript\" language=\"JavaScript\">\n");
		writer.write("updateTime = " + updateTime + "000;\n");
		writer.write("updateUrl = \"" + serverUrl(req) + "/courier/" + placementId);
		writer.write("?userId="+userId+"\";\n");
		writer.write("scheduleUpdate();\n");
		writer.write("</script>\n");
	}
	
	/** 
	 * This method is a duplicate of org.sakaiproject.util.web.Web.serverUrl()
	 * Duplicated here from org.sakaiproject.util.web.Web.java so that 
	 * the JSF tag library doesn't have a direct jar dependency on more of Sakai.
	 */
	private static String serverUrl(HttpServletRequest req)
	{
		StringBuilder url = new StringBuilder();
		url.append(req.getScheme());
		url.append("://");
		url.append(req.getServerName());
		if (((req.getServerPort() != 80) && (!req.isSecure())) || ((req.getServerPort() != 443) && (req.isSecure())))
		{
			url.append(":");
			url.append(req.getServerPort());
		}

		return url.toString();
	}
}



