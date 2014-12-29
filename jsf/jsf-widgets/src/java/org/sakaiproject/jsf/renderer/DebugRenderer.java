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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;


public class DebugRenderer extends Renderer
{
	public boolean supportsComponentType(UIComponent component)
	{
		return (component instanceof UIOutput);
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException
	{
		if (!component.isRendered()) return;

		ResponseWriter writer = context.getResponseWriter();
		writer.write("<xmp>");
		writer.write("***** DEBUG TAG RENDER OUTPUT *****\n\n");

		dumpJSFVariable("applicationScope", context);
		dumpJSFVariable("sessionScope", context);
		dumpJSFVariable("requestScope", context);
		dumpJSFVariable("toolScope", context);
		dumpJSFVariable("toolConfig", context);
		dumpJSFVariable("param", context);
		writer.write("</xmp>");
	}

	private void dumpJSFVariable(String varName, FacesContext context)
		throws IOException
	{
		Object varValue = context.getApplication().getVariableResolver().resolveVariable(context, varName);
		ResponseWriter writer = context.getResponseWriter();

		if (varValue instanceof Map)
		{
		    dumpMap((Map) varValue, varName, writer);
		}
		else
		{
		    writer.write(varName);
		    writer.write(": ");
		    writer.write(String.valueOf(varValue));
		    writer.write("\n\n");
		}

	}

	private void dumpMap(Map map, String mapName, ResponseWriter writer)
		throws IOException
	{
	    writer.write(mapName);
	    if (map == null)
	    {
	        writer.write(" is null\n\n");
	        return;
	    }

	    writer.write(" " + map + " contains: \n");
	    Iterator i = map.entrySet().iterator();
	    while (i.hasNext())
	    {
	    	Entry entry = (Entry) i.next();
	        Object name = entry.getKey();
	        Object value = entry.getValue();
	        writer.write(String.valueOf(name));
	        writer.write(" -> ");
	        writer.write(String.valueOf(value));
	        writer.write('\n');
	    }

	    writer.write("\n\n");
	}
}



