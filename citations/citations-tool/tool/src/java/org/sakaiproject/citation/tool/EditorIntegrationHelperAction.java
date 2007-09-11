/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/citations/trunk/citations-tool/tool/src/java/org/sakaiproject/citation/tool/CitationHelperAction.java $
 * $Id: CitationHelperAction.java 34481 2007-08-27 21:36:06Z dsobiera@indiana.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.tool;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.citation.api.ActiveSearch;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationHelper;
import org.sakaiproject.citation.api.CitationIterator;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.api.SearchCategory;
import org.sakaiproject.citation.api.SearchDatabaseHierarchy;
import org.sakaiproject.citation.api.Schema.Field;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.citation.cover.CitationService;
import org.sakaiproject.citation.cover.ConfigurationService;
import org.sakaiproject.citation.cover.SearchManager;
import org.sakaiproject.citation.util.api.SearchException;
import org.sakaiproject.citation.util.api.SearchQuery;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * Provide support for the Citations editor integration functionality
 */
public class EditorIntegrationHelperAction extends CitationHelperAction
{
	protected final static Log m_log = LogFactory.getLog(EditorIntegrationHelperAction.class);

  /**
   * Editor Integration Library Resources Search
   */
  public void doIntegrationSearch(RunData data)
  {
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid());

    m_log.debug("doIntegrationSearch()");

    setCaller(state, Caller.EDITOR_INTEGRATION);
		doSearchCommon(state, Mode.ERROR);
  }

	/**
	 * Access the SessionState for the current request.  This is a private
	 * session state for this servlet/helper, scoped for the current request.
	 *
	 * @see org.sakaiproject.cheftool.ToolServlet#getState(RunData data)
	 *
	 * @param req The current portlet request.
	 * @return The SessionState objet for the current request.
	 */
	protected SessionState getState(HttpServletRequest req)
	{
		/*
		 * key the state based on the pid, if present. If not we will use the servlet's class name
		 */
		String key = getPid(req);

		if (key == null)
		{
			key = this.toString() + ".";
			m_log.warn("getState(): using servlet key: " + key);
		}
		/*
		 * Append our full name to form a unique session state key for this helper
		 */
    key += ".org.sakaiproject.citation.tool.EditorIntegrationHelperAction";

		SessionState rv = org.sakaiproject.event.cover.UsageSessionService.getSessionState(key);

		if (rv == null)
		{
		  m_log.warn("getState(): no state found for key: "
			        +   key
			        +   " "
			        +   req.getPathInfo()
			        +   " "
			        +   req.getQueryString()
			        +   " "
				    	+   req.getRequestURI());
		}
		return rv;
	}
}