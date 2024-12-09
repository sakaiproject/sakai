/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2005-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class JSPHelper {

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
