/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

import org.sakaiproject.jsf.util.RendererUtil;


public class ViewTitleRenderer extends Renderer
{
	public boolean supportsComponentType(UIComponent component)
	{
		return (component instanceof UIOutput);
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException
	{
		if (!component.isRendered()) return;

		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("h3", null);

		// TODO: Should we really write out the ID?  Is there any need for this in this tag?
//		String id = (String) RendererUtil.getAttribute(context, component, "id");
//		if (id != null)
//		{
//			writer.writeAttribute("id", id, null);
//		}

		String cssClass = "insColor insBak insBorder";
		Integer indent = (Integer) RendererUtil.getAttribute(context, component, "indent");
		if (indent != null)
		{
			cssClass = cssClass + " indnt"+indent;
		}
		writer.writeAttribute("class", cssClass, null);
		writer.writeText((String) RendererUtil.getAttribute(context, component, "value"), null);
	}

	/**
	 * @param context FacesContext for the request we are processing
	 * @param component UIComponent to be rendered
	 * @exception IOException if an input/output error occurs while rendering
	 * @exception NullPointerException if <code>context</code> or <code>component</code> is null
	 */
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException
	{
		if (!component.isRendered()) return;

		ResponseWriter writer = context.getResponseWriter();
		writer.endElement("h3");
	}
}



