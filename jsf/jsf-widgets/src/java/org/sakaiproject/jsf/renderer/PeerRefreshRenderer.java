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


public class PeerRefreshRenderer extends Renderer
{
	public boolean supportsComponentType(UIComponent component)
	{
		return (component instanceof UIOutput);
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException
	{
		ResponseWriter writer = context.getResponseWriter();
		writer.write("<div class =\"instruction\">");
	}

	public void encodeEnd(FacesContext context, UIComponent component) throws IOException
	{
		ResponseWriter writer = context.getResponseWriter();
		String txt = (String) RendererUtil.getAttribute(context, component, "value");
		if ((txt != null) && (txt.length() > 0))
		{
			writer.write("<script type=\"text/javascript\" language=\"JavaScript\">\n");
			writer.write("try\n");
			writer.write("{\n");
			writer.write("	if (parent." + txt + ".location.toString().length > 1)\n");
			writer.write("	{\n");
			writer.write("		parent." + txt + ".location.replace(parent." + txt + ".location);\n");
			writer.write("	}\n");
			writer.write("}\n");
			writer.write("catch (e1)\n");
			writer.write("{\n");
			writer.write("	try\n");
			writer.write("	{\n");
			writer.write("		if (parent.parent." + txt + ".location.toString().length > 1)\n");
			writer.write("		{\n");
			writer.write("			parent.parent." + txt + ".location.replace(parent.parent." + txt + ".location);\n");
			writer.write("		}\n");
			writer.write("	}\n");
			writer.write("	catch (e2)\n");
			writer.write("	{\n");
			writer.write("	}\n");
			writer.write("}\n");
			writer.write("</script>\n");
		}
	}
}



