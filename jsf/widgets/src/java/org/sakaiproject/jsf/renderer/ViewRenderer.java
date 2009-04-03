/**********************************************************************************
*
* Header:
*
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
			// so get them from the portal through the request attributes.
			// Any tool-specific stylesheets need to be placed after Sakai's base CSS (so that
			// tool-specific overrides can take place), but before the installation's skin CSS
			// (so that the tool can be skinned).
			String headBaseCss = (String)req.getAttribute("sakai.html.head.css.base");
			if (headBaseCss == null || headBaseCss.length() == 0) {
				// include default stylesheet
				headBaseCss = "<link href=\"/sakai-jsf-resource/css/sakai.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
			}
			String toolCssHref = (String) RendererUtil.getAttribute(context, component, "toolCssHref");
			if (toolCssHref != null) {
				toolCssHref = "<link href=\"" + toolCssHref + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
			}			
			String headSkinCss = (String)req.getAttribute("sakai.html.head.css.skin");
			String headJs = (String)req.getAttribute("sakai.html.head.js");
			String bodyonload = (String) req.getAttribute("sakai.html.body.onload");

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

			writer.write(headBaseCss);
			if (toolCssHref != null) writer.write(toolCssHref);
			if (headSkinCss != null) writer.write(headSkinCss);
			if (headJs != null) writer.write(headJs);

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



