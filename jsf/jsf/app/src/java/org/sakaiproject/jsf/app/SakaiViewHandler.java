/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.jsf.app;

import java.io.IOException;
import java.util.Locale;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.kernel.tool.Tool;
import org.sakaiproject.util.web.Web;

/**
 * <p>
 * SakaiViewHandler extends the basic ViewHandler functionality for getActionURL(). getActionURL() is extended ...
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
public class SakaiViewHandler extends ViewHandler
{
	// TODO: Note, these two values must match those in jsf-tool's JsfTool

	/** Request attribute we set to help the return URL know what path we add (does not need to be in the URL. */
	public static final String URL_PATH = "sakai.jsf.tool.URL.path";

	/** Request attribute we set to help the return URL know what extension we (or jsf) add (does not need to be in the URL. */
	public static final String URL_EXT = "sakai.jsf.tool.URL.ext";

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SakaiViewHandler.class);

	/** The wrapped ViewHandler. */
	private ViewHandler m_wrapped = null;

	private SakaiViewHandler()
	{
	}

	public SakaiViewHandler(ViewHandler wrapped)
	{
		m_wrapped = wrapped;
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

		String ext = (String) req.getAttribute(URL_EXT);
		if ((ext != null) && path.endsWith(ext)) path = path.substring(0, path.length() - ext.length());

		// make sure the URL processing uses the Sakai, not Native the request object so we can get at the URL information setup by the invoker
		req.removeAttribute(Tool.NATIVE_URL);

		// form our return URL
		String rv = Web.returnUrl(req, path);

		// restore (if needed)
		req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		M_log.debug("action url for view: " + viewId + " = " + rv);

		return rv;
	}

	/** Methods delegated to default ViewHandler */

	public Locale calculateLocale(FacesContext arg0)
	{
		return m_wrapped.calculateLocale(arg0);
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

	public void renderView(FacesContext arg0, UIViewRoot arg1) throws IOException, FacesException
	{
		m_wrapped.renderView(arg0, arg1);
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



