/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
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

package org.sakaiproject.user.jsf;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;


/**
 * @author Chen Wen
 * @version $Id$
 *
 */
public class HideDivisionRenderer extends Renderer
{
	private static final String RESOURCE_PATH;
	private static final String FOLD_IMG_HIDE;
	private static final String FOLD_IMG_SHOW;
	private static final String CURSOR;

	static {
		RESOURCE_PATH = "/library";
		FOLD_IMG_HIDE = RESOURCE_PATH + "/image/sakai/expand.gif";
		FOLD_IMG_SHOW = RESOURCE_PATH + "/image/sakai/collapse.gif";
		CURSOR = "cursor:pointer";
	}

	public boolean supportsComponentType(UIComponent component)
	{
		return (component instanceof org.sakaiproject.user.jsf.HideDivisionComponent);
	}

	public void decode(FacesContext context, UIComponent component)
	{
	}
	
	public void encodeChildren(FacesContext context, UIComponent component)
			throws IOException {
		super.encodeChildren(context, component);
	}

	public void encodeBegin(FacesContext context, UIComponent component)
	throws IOException {

		if (!component.isRendered()) {
			return;
		}

		ResponseWriter writer = context.getResponseWriter();
		String jsfId = (String) RendererUtil.getAttribute(context, component, "id");
		String id = jsfId;

		if (component.getId() != null &&
				!component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
		{
			id = component.getClientId(context);
		}

		String title = (String) RendererUtil.getAttribute(context, component, "title");
		Object tmpFoldStr = RendererUtil.getAttribute(context, component, "hideByDefault");
		String key = (String)RendererUtil.getAttribute(context, component, "key");
		
		writer.write("<fieldset>");
		writer.write("<legend>");
		writer.write("<a role='button' data-toggle='collapse' aria-expanded='true' aria-target='" + id + "' href='#" + id + "' data-target=\"[id='" + id + "']\">" + title + "</a>");
		writer.write("</legend>");

		writer.write("<div class='collapse in' " +
			" id=\"" + id + "\">");
	}


	public void encodeEnd(FacesContext context, UIComponent component) throws
	IOException {
		if (!component.isRendered()) {
			return;
		}

		ResponseWriter writer = context.getResponseWriter();
		writer.write("</div>");
		writer.write("</fieldset>");

	}

}
