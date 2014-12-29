/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.tool.api;

import java.util.Properties;
import java.util.Set;

/**
 * <p>
 * Tool models a Sakai user interface producing tool.  Tool and its attributes are immutable.
 * </p>
 */
public interface Tool
{
	/** The request attribute name whose value if "true" requests producing a document fragment rather than a full document. */
	public static final String FRAGMENT = "sakai.fragment";

	/** The request attribute name whose value if "true" requests producing a document suitable for aggregation in a portal. */
	public static final String PORTLET = "sakai.portlet";

	/** The request attribute name containing the Tool definition for the current request. */
	public static final String TOOL = "sakai.tool";

	/** The request attribute name containing the ToolSession for the current request. */
	public static final String TOOL_SESSION = "sakai.tool.session";

	/** The request attribute name if present causes our wrapped requests to report the native URL rather than the Sakai set up URL information. */
	public static final String NATIVE_URL = "sakai.request.native.url";

	/** The request attribute name containing the Tool placement for the current request. */
	public static final String PLACEMENT = "sakai.tool.placement";

	/** The request attribute / URL parameter name containing the Tool placement id for the current request. */
	public static final String PLACEMENT_ID = "sakai.tool.placement.id";

	/** Standard session attribute shared between client and helper: URL to redirect to when helper is done. */
	public static final String HELPER_DONE_URL = "sakai.tool.helper.done.url";

	/** Standard session attribute shared between client and helper: end user message. */
	public static final String HELPER_MESSAGE = "sakai.tool.helper.message";

	/**
	 * Access the well known id of the tool.
	 * 
	 * @return The well known id of the tool.
	 */
	String getId();

	/**
	 * Access the tool's home destination, if one is registered
	 * @return The tool's registered home destination, or null if not registered.
	 */
	String getHome();

	/**
	 * Access the tool title.
	 * 
	 * @return The tool title.
	 */
	String getTitle();

	/**
	 * Access the tool description.
	 * 
	 * @return The tool description.
	 */
	String getDescription();

	/**
	 * Access the configuration properties from registration for the tool.  Access is read only.
	 * 
	 * @return The read-only configuration properties from registration for the tool.
	 */
	Properties getRegisteredConfig();

	/**
	 * Access the configuration properties that may be configured (not the final ones) from registration for the tool.  Access is read only.
	 * 
	 * @return The read-only configuration properties from registration for the tool.
	 */
	Properties getMutableConfig();

	/**
	 * Access the configuration properties that are final ones, may not be configured, from registration for the tool.  Access is read only.
	 * 
	 * @return The read-only configuration properties from registration for the tool.
	 */
	Properties getFinalConfig();

	/**
	 * Access the keywords registered for this tool.   Access is read only.
	 * 
	 * @return The Set (String) of keywords registered for this tool.
	 */
	Set<String> getKeywords();

	/**
	 * Access the catagories registered for this tool.   Access is read only.
	 * 
	 * @return The Set (String) of categories registered for this tool.
	 */
	Set<String> getCategories();
	
	/**
	 * typesafe enumeration for access security.
	 */
	public class AccessSecurity
	{
		private static final int TOOL_SECURITY = 0;
		private static final int PORTAL_SECURITY = 1;
		private int m_type = -1;
		private AccessSecurity(int type)
		{
			m_type = type;
		}
		public static final AccessSecurity TOOL = new AccessSecurity(TOOL_SECURITY);
		public static final AccessSecurity PORTAL = new AccessSecurity(PORTAL_SECURITY);
	}

	/**
	 * Access the setting for this tool for the access security.  Access is read only.
	 * @return ACCESS_SECURITY_PORTAL if the tool is configured to let the portal handle access security, or ACCESS_SECURITY_TOOL if it handles it internally.
	 */
	AccessSecurity getAccessSecurity();
}
