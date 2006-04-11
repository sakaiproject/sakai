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



