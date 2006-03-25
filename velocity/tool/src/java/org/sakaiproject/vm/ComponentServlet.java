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

package org.sakaiproject.vm;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.util.ParameterParser;

/**
 * <p>
 * ComponentServlet does some setup and provides some support for (mostly legacy) Sakai servlets and tools.
 * <p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
public abstract class ComponentServlet extends HttpServlet
{
	/** This request's parsed parameters */
	protected final static String ATTR_PARAMS = "sakai.wrapper.params";

	/**
	 * Override service, adding the setup for legacy.
	 */
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException
	{
		// parse the parameters of the request, considering Unicode issues, into a ParameterParser
		ParameterParser parser = new ParameterParser(req);

		// make this available from the req as an attribute
		req.setAttribute(ATTR_PARAMS, parser);

		// Setup.setup(req, resp);
		super.service(req, resp);
	}

	/**
	 * Send a redirect so our parent ends up at the url, via javascript.
	 * 
	 * @param url
	 *        The redirect url
	 */
	protected void sendParentRedirect(HttpServletResponse resp, String url)
	{
		try
		{
			resp.setContentType("text/html; charset=UTF-8");
			PrintWriter out = resp.getWriter();
			out
					.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			out.println("<html><head></head><body>");
			out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
			out.println("if (parent)\n" + "{\n\tparent.location.replace('" + url + "');\n}\n");
			out.println("</script>");
			out.println("</body></html>");
			resp.flushBuffer();
		}
		catch (IOException e)
		{
		}
	}

	// set standard no-cache headers
	protected void setNoCacheHeaders(HttpServletResponse resp)
	{
		resp.setContentType("text/html; charset=UTF-8");
		// some old date
		resp.addHeader("Expires", "Mon, 01 Jan 2001 00:00:00 GMT");
		// TODO: do we need this? adding a date header is expensive contention for the date formatter, ours or Tomcats.
		// resp.addDateHeader("Last-Modified", System.currentTimeMillis());
		resp.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		resp.addHeader("Pragma", "no-cache");
	}
}



