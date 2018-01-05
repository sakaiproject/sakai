/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
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

package org.sakaiproject.jsf.app;

import java.io.IOException;
import java.util.Locale;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

/**
 * <p>
 * SakaiViewHandler extends the basic ViewHandler functionality for getActionURL(). getActionURL() is extended ...
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
@Slf4j
public class SakaiViewHandler extends ViewHandler
{
	// TODO: Note, these two values must match those in jsf-tool's JsfTool

	/** Request attribute we set to help the return URL know what path we add (does not need to be in the URL. */
	public static final String URL_PATH = "sakai.jsf.tool.URL.path";

	/** Request attribute we set to help the return URL know what extension we (or jsf) add (does not need to be in the URL. */
	public static final String URL_EXT = "sakai.jsf.tool.URL.ext";

	/** The wrapped ViewHandler. */
	private ViewHandler m_wrapped = null;

	/** Resource bundle using current language locale. */
	private ResourceLoader rb;

	private SakaiViewHandler()
	{
	}

	public SakaiViewHandler(ViewHandler wrapped)
	{
		m_wrapped = wrapped;
		rb = new ResourceLoader();
	}

	public String getActionURL(FacesContext context, String viewId)
	{
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();

		if (req.getAttribute(URL_EXT) == null)
		{
			// If the request didn't go through JsfTool (the JSF is accessed directly from its webapp, 
			// not as a Sakai tool), then don't do Sakai's special action URL handling.
			return m_wrapped.getActionURL(context, viewId);
		}
		
		// get the path that got us here (from the tool's point of view)
		String path = viewId;

		// modify the path to remove things that were added by Sakai navigation to get here (prefix path, suffix extension)
		String prefix = (String) req.getAttribute(URL_PATH);
		if ((prefix != null) && path.startsWith(prefix)) path = path.substring(prefix.length());

		Object extensions = req.getAttribute(URL_EXT);
		String [] exts = extensions instanceof String?new String[]{(String)extensions}:(String[])extensions; 
		for (String ext:exts) {
			if ((ext != null) && path.endsWith(ext)) path = path.substring(0, path.length() - ext.length());
		}

		// make sure the URL processing uses the Sakai, not Native the request object so we can get at the URL information setup by the invoker
		req.removeAttribute(Tool.NATIVE_URL);

		// form our return URL
		String rv = Web.returnUrl(req, path);

		// restore (if needed)
		req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		log.debug("action url for view: " + viewId + " = " + rv);

		return rv;
	}

	/** Methods delegated to default ViewHandler */

	public Locale calculateLocale(FacesContext arg0)
	{
		return rb.getLocale();
	}

	public String calculateRenderKitId(FacesContext arg0)
	{
		return m_wrapped.calculateRenderKitId(arg0);
	}

	public UIViewRoot createView(FacesContext arg0, String arg1)
	{
		UIViewRoot root = m_wrapped.createView(arg0, arg1);

		if (root != null)
		{
			// restore messages
			MessageSaver.restoreMessages(arg0);
		}

		return root;
	}

	public String getResourceURL(FacesContext arg0, String arg1)
	{
		return m_wrapped.getResourceURL(arg0, arg1);
	}

	public void renderView(FacesContext context, UIViewRoot root) throws IOException, FacesException {
		// SAK-20286 start
		// Get the request
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
		String requestURI = req.getRequestURI();
		// Make the attribute name unique to the request 
		String attrName = "sakai.jsf.tool.URL.loopDetect.viewId-" + requestURI;
		// Try to fetch the attribute
		Object attribute = req.getAttribute(attrName);
		// If the attribute is null, this is the first request for this view
		if (attribute == null) {
			req.setAttribute(attrName, "true");
		} else if ("true".equals(attribute)) { // A looping request is detected.
			HttpServletResponse res = (HttpServletResponse) context.getExternalContext().getResponse();
			// Send a 404
			res.sendError(404, "File not found: " + requestURI);
		}
		// SAK-20286 end
		m_wrapped.renderView(context, root);
	}

	public UIViewRoot restoreView(FacesContext arg0, String arg1)
	{
		UIViewRoot root = m_wrapped.restoreView(arg0, arg1);

		if (root != null)
		{
			// restore messages
			MessageSaver.restoreMessages(arg0);
		}

		return root;
	}

	public void writeState(FacesContext arg0) throws IOException
	{
		m_wrapped.writeState(arg0);
	}
}



