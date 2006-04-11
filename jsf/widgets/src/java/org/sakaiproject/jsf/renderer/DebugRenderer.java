/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

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
	    Iterator i = map.keySet().iterator();
	    while (i.hasNext())
	    {
	        Object name = i.next();
	        Object value = map.get(name);
	        writer.write(String.valueOf(name));
	        writer.write(" -> ");
	        writer.write(String.valueOf(value));
	        writer.write('\n');
	    }

	    writer.write("\n\n");
	}
}



