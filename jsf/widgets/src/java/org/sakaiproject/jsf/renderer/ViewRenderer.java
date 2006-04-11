/**********************************************************************************
*
* Header:
*
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
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.jsf.util.RendererUtil;

public class ViewRenderer extends Renderer
{
	public boolean supportsComponentType(UIComponent component)
	{
		// this should be just UIViewRoot, but since that's not working now...
		return (component instanceof UIViewRoot) || (component instanceof UIOutput);
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException
	{
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();

		ResponseWriter writer = context.getResponseWriter();

		if (!renderAsFragment(context))
		{
			// The stylesheets and javascripts to include are really the portal's responsibility
			// so get them from the portal through the request attributes

			String headInclude = (String) req.getAttribute("sakai.html.head");
			String bodyonload = (String) req.getAttribute("sakai.html.body.onload");

			if (headInclude == null || headInclude.length() == 0)
			{
				// include default stylesheet
				headInclude = "<link href=\"/sakai-jsf-resource/css/sakai.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
			}

			writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
			writer.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n");
			writer.write("<head>\n");
			String title = (String) RendererUtil.getAttribute(context, component, "title");
			if (title != null)
			{
				writer.write("<title>");
				writer.write(title);
				writer.write("</title>\n");
			}

			writer.write(headInclude);

			writer.write("</head>\n");

			writer.write("<body");

			if (bodyonload != null && bodyonload.length() > 0)
			{
				writer.write(" onload=\"");
				writer.write(bodyonload);
				writer.write("\"");
			}
			writer.write(">\n");

		}

		writer.write("<div class=\"portletBody\">\n");
	}

	/**
	 * @param context FacesContext for the request we are processing
	 * @param component UIComponent to be rendered
	 * @exception IOException if an input/output error occurs while rendering
	 * @exception NullPointerException if <code>context</code> or <code>component</code> is null
	 */
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException
	{
		ResponseWriter writer = context.getResponseWriter();

		writer.write("</div>");
		if (!renderAsFragment(context))
		{
			writer.write("</body></html>");
		}
	}

	/** Looks at Sakai-specific attributes to determine if the view should
	 * render HTML, HEAD, BODY; if the request attribute "sakai.fragment"="true",
	 * then don't render HTML, HEAD, BODY, etc.
	 * @param context
	 * @return
	 */
	protected static boolean renderAsFragment(FacesContext context)
	{
		String fragStr = (String) ((HttpServletRequest) context.getExternalContext().getRequest()).getAttribute("sakai.fragment");
		return (fragStr != null && "true".equals(fragStr));
	}

}



