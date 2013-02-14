package org.sakaiproject.portlet.util;

import java.io.IOException;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequestDispatcher;

/**
 * JSP Helper Class
 */
public class JSPHelper 
{

	public static void sendToJSP(PortletContext pContext, 
			RenderRequest request, RenderResponse response,
			String jspPage) throws PortletException {
		response.setContentType(request.getResponseContentType());
		if (jspPage != null && jspPage.length() != 0) {
			try {
				PortletRequestDispatcher dispatcher = pContext
					.getRequestDispatcher(jspPage);
				dispatcher.include(request, response);
			} catch (IOException e) {
				throw new PortletException("Sakai Dispatch unabble to use "
						+ jspPage, e);
			}
		}
	}
}
