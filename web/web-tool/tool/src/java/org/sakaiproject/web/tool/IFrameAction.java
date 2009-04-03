/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.web.tool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Collections;
import java.net.URLEncoder;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * <p>
 * IFrameAction is the Sakai tool to place any web content in an IFrame on the page.
 * </p>
 * <p>
 * Three special modes are supported - these pick the URL content from special places:
 * </p>
 * <ul>
 * <li>"site" - to show the services "server.info.url" configuration URL setting</li>
 * <li>"workspace" - to show the configured "myworkspace.info.url" URL, introducing a my workspace to users</li>
 * <li>"worksite" - to show the current site's "getInfoUrlFull()" setting</li>
 * </ul>
 */
public class IFrameAction extends VelocityPortletPaneledAction
{


	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("iframe");

	/** The source URL, in state, config and context. */
	protected final static String SOURCE = "source";

	/** The value in state and context for the source URL to actually used, as computed from special and URL. */
	protected final static String URL = "url";

	/** The height, in state, config and context. */
	protected final static String HEIGHT = "height";

	/** The custom height from user input * */
	protected final static String CUSTOM_HEIGHT = "customNumberField";

	/** The special attribute, in state, config and context. */
	protected final static String SPECIAL = "special";

	/** Support an external url defined in sakai.properties, in state, config and context. */
	protected final static String SAKAI_PROPERTIES_URL_KEY = "sakai.properties.url.key";
	
	/** If set, always hide the OPTIONS button */
	protected final static String HIDE_OPTIONS = "hide.options";
	
	/** Special value for site. */
	protected final static String SPECIAL_SITE = "site";

	/** Special value for myworkspace. */
	protected final static String SPECIAL_WORKSPACE = "workspace";

	/** Special value for worksite. */
	protected final static String SPECIAL_WORKSITE = "worksite";
	

	/** The title, in state and context. */
	protected final static String TITLE = "title";

	/**
	 * Whether to pass through the PID to the URL displayed in the IFRAME. This enables integration in that the application in the IFRAME will know what site and tool it is part of.
	 */
	private final static String PASS_PID = "passthroughPID";

	
	
	/** Valid digits for custom height from user input **/
	protected static final String VALID_DIGITS = "0123456789";

	/** Choices of pixels displayed in the customization page */
	public String[] ourPixels = { "300px", "450px", "600px", "750px", "900px", "1200px", "1800px", "2400px" };
	
	/** Attributes for web content tool page title **/
	private static final String STATE_PAGE_TITLE = "pageTitle";
	
	private static final String FORM_PAGE_TITLE = "title-of-page";
	
	private static final String FORM_TOOL_TITLE = "title-of-tool";

	

	/**
	 * Expand macros to insert session information into the URL?
	 */
	private final static String MACRO_EXPANSION       = "expandMacros";

	/** Macro name: Site id (GUID) */
	protected static final String MACRO_SITE_ID             = "${SITE_ID}";
	/** Macro name: User id */
	protected static final String MACRO_USER_ID             = "${USER_ID}";
	/** Macro name: User enterprise id */
	protected static final String MACRO_USER_EID            = "${USER_EID}";
	/** Macro name: First name */
	protected static final String MACRO_USER_FIRST_NAME     = "${USER_FIRST_NAME}";
	/** Macro name: Last name */
	protected static final String MACRO_USER_LAST_NAME      = "${USER_LAST_NAME}";
	/** Macro name: Role */
	protected static final String MACRO_USER_ROLE           = "${USER_ROLE}";
	/** Macro name: Session */
	protected static final String MACRO_SESSION_ID          = "${SESSION_ID}";

	private static final String MACRO_CLASS_SITE_PROP = "SITE_PROP:";
	
	private static final String IFRAME_ALLOWED_MACROS_PROPERTY = "iframe.allowed.macros";
	
	private static final String MACRO_DEFAULT_ALLOWED = "${USER_ID},${USER_EID},${USER_FIRST_NAME},${USER_LAST_NAME},${SITE_ID},${USER_ROLE}";
	
	private static ArrayList allowedMacrosList;
	// initialize list of approved macros for replacement within URL
	static
	{
		allowedMacrosList = new ArrayList();
		
		final String allowedMacros = 
			ServerConfigurationService.getString(IFRAME_ALLOWED_MACROS_PROPERTY, MACRO_DEFAULT_ALLOWED);
			
		String parts[] = allowedMacros.split(",");
		
		if(parts != null) {
		
			for(int i = 0; i < parts.length; i++) {
			
				allowedMacrosList.add(parts[i]);
			
			}
		
		}
	}
	
	/**
	 * Populate the state with configuration settings
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		// TODO: we might want to keep this from running for each request - but by letting it we get fresh info each time... -ggolden
		super.initState(state, portlet, rundata);

		Placement placement = ToolManager.getCurrentPlacement();
		Properties config = placement.getConfig();

		// set the pass_pid parameter
		boolean passPid = false;
		String passPidStr = config.getProperty(PASS_PID, "false");
		state.removeAttribute(PASS_PID);
		if ("true".equalsIgnoreCase(passPidStr))
		{
			state.setAttribute(PASS_PID, Boolean.TRUE);
			passPid = true;
		}

		// Assume macro expansion (disable on request)
		boolean macroExpansion = true;
		String macroExpansionStr = config.getProperty(MACRO_EXPANSION, "true");

		state.removeAttribute(MACRO_EXPANSION);
		if ("false".equalsIgnoreCase(macroExpansionStr))
		{
			state.setAttribute(MACRO_EXPANSION, Boolean.FALSE);
			macroExpansion = false;
		}

		// set the special setting
		String special = config.getProperty(SPECIAL);
		
		final String sakaiPropertiesUrlKey = config.getProperty(SAKAI_PROPERTIES_URL_KEY);
		
		final String hideOptions = config.getProperty(HIDE_OPTIONS);

		// check for an older way the ChefWebPagePortlet took parameters, converting to our "special" values
		if (special == null)
		{
			if ("true".equals(config.getProperty("site")))
			{
				special = SPECIAL_SITE;
			}
			else if ("true".equals(config.getProperty("workspace")))
			{
				special = SPECIAL_WORKSPACE;
			}
			else if ("true".equals(config.getProperty("worksite")))
			{
				special = SPECIAL_WORKSITE;
			}
		}

		state.removeAttribute(SPECIAL);
		if ((special != null) && (special.trim().length() > 0))
		{
			state.setAttribute(SPECIAL, special);
		}
		
		
		state.removeAttribute(HIDE_OPTIONS);
		if ((hideOptions != null) && (hideOptions.trim().length() > 0))
		{
			state.setAttribute(HIDE_OPTIONS, hideOptions);
		}

		// set the source url setting
		String source = StringUtil.trimToNull(config.getProperty(SOURCE));

		// check for an older way the ChefWebPagePortlet took parameters, converting to our "source" value
		if (source == null)
		{
			source = StringUtil.trimToNull(config.getProperty("url"));
		}

		// store the raw as-configured source url
		state.removeAttribute(SOURCE);
		if (source != null)
		{
			state.setAttribute(SOURCE, source);
		}

		// compute working URL, modified from the configuration URL if special
		String url = sourceUrl(special, source, placement.getContext(), macroExpansion, passPid, placement.getId(), sakaiPropertiesUrlKey);
		state.setAttribute(URL, url);

		// set the height
		state.setAttribute(HEIGHT, config.getProperty(HEIGHT, "600px"));

		// set the title
		state.setAttribute(TITLE, placement.getTitle());
		
		if (state.getAttribute(STATE_PAGE_TITLE) == null)
		{
			SitePage p = SiteService.findPage(getCurrentSitePageId());
			state.setAttribute(STATE_PAGE_TITLE, p.getTitle());
		}
		
	}
	
	/**
	 * Get the current site page our current tool is placed on.
	 * 
	 * @return The site page id on which our tool is placed.
	 */
	protected String getCurrentSitePageId()
	{
		ToolSession ts = SessionManager.getCurrentToolSession();
		if (ts != null)
		{
			ToolConfiguration tool = SiteService.findTool(ts.getPlacementId());
			if (tool != null)
			{
				return tool.getPageId();
			}
		}
		
		return null;
	}

	/**
	 * Compute the actual URL we will used, based on the configuration special and source URLs
	 */
	protected String sourceUrl(String special, String source, String context, boolean macroExpansion, boolean passPid, String pid, String sakaiPropertiesUrlKey)
	{
		String rv = StringUtil.trimToNull(source);

		// if marked for "site", use the site intro from the properties
		if (SPECIAL_SITE.equals(special))
		{
			// set the url to the site config'ed url
			rv = StringUtil.trimToNull(ServerConfigurationService.getString("server.info.url"));
		}

		// if marked for "workspace", use the "user" site info from the properties
		else if (SPECIAL_WORKSPACE.equals(special))
		{
			// set the url to the site config'ed url
			rv = StringUtil.trimToNull(ServerConfigurationService.getString("myworkspace.info.url"));
		}

		// if marked for "worksite", use the setting from the site's definition
		else if (SPECIAL_WORKSITE.equals(special))
		{
			// set the url to the site of this request's config'ed url
			try
			{
				// get the site's info URL, if defined
				Site s = SiteService.getSite(context);
				rv = StringUtil.trimToNull(s.getInfoUrlFull());

				// compute the info url for the site if it has no specific InfoUrl
				if (rv == null)
				{
					// access will show the site description or title...
					rv = ServerConfigurationService.getAccessUrl() + s.getReference();
				}
			}
			catch (Exception any)
			{
			}
		} 
		
		else if (sakaiPropertiesUrlKey != null && sakaiPropertiesUrlKey.length() > 1)
		{
			// set the url to a string defined in sakai.properties
			rv = StringUtil.trimToNull(ServerConfigurationService.getString(sakaiPropertiesUrlKey));
		}
		

		// if it's not special, and we have no value yet, set it to the webcontent instruction page, as configured
		if (rv == null)
		{
			rv = StringUtil.trimToNull(ServerConfigurationService.getString("webcontent.instructions.url"));
		}

		if (rv != null)
		{
			// accept a partial reference url (i.e. "/content/group/sakai/test.gif"), convert to full url
			rv = convertReferenceUrl(rv);

			// pass the PID through on the URL, IF configured to do so
			if (passPid)
			{
				if (rv.indexOf("?") < 0)
				{
					rv = rv + "?";
				}
				else
				{
					rv = rv + "&";
				}

				rv = rv + "pid=" + pid;
			}

			if (macroExpansion)
			{
				rv = doMacroExpansion(rv);
			}
		}

		return rv;
	}

	/**
	 * If the url is a valid reference, convert it to a URL, else return it unchanged.
	 */
	protected String convertReferenceUrl(String url)
	{
		// make a reference
		Reference ref = EntityManager.newReference(url);

		// if it didn't recognize this, return it unchanged
		if (ref.isKnownType())
		{
			// return the reference's url
			String refUrl = ref.getUrl();
			if (refUrl != null)
			{
				return refUrl;
			}
		}

		return url;
	}

	/**
	 * Get the current user id
	 * @throws SessionDataException
	 * @return User id
	 */
	private String getUserId() throws SessionDataException
	{
		Session session = SessionManager.getCurrentSession();

		if (session == null)
		{
			throw new SessionDataException("No current user session");
		}
		return session.getUserId();
	}
	
	/**
	 * Get the current session id
	 * @throws SessionDataException
	 * @return Session id
	 */
	private String getSessionId() throws SessionDataException
	{
		Session session = SessionManager.getCurrentSession();

		if (session == null)
		{
			throw new SessionDataException("No current user session");
		}
		return session.getId();
	}
	

	/**
	 * Get the current user eid
	 * @throws SessionDataException
	 * @return User eid
	 */
	private String getUserEid() throws SessionDataException
	{
		Session session = SessionManager.getCurrentSession();

		if (session == null)
		{
			throw new SessionDataException("No current user session");
		}
		return session.getUserEid();
	}
	

	/**
	 * Get current User information
	 * @throws IdUnusedException, SessionDataException
	 * @return {@link User} data
	 * @throws UserNotDefinedException 
	 */
	private User getUser() throws IdUnusedException, SessionDataException, UserNotDefinedException
	{
		
		return UserDirectoryService.getUser(this.getUserId());
	}

	/**
	 * Get the current site id
	 * @throws SessionDataException
	 * @return Site id (GUID)
	 */
	private String getSiteId() throws SessionDataException
	{
		Placement placement = ToolManager.getCurrentPlacement();

		if (placement == null)
		{
			throw new SessionDataException("No current tool placement");
		}
		return placement.getContext();
	}

	/**
	 * Fetch the user role in the current site
	 * @throws IdUnusedException, SessionDataException
	 * @return Role
	 * @throws GroupNotDefinedException 
	 */
	private String getUserRole() throws IdUnusedException, SessionDataException, GroupNotDefinedException
	{
		AuthzGroup 	group;
		Role 				role;

		group = AuthzGroupService.getAuthzGroup("/site/" + getSiteId());
		if (group == null)
		{
			throw new SessionDataException("No current group");
		}

		role = group.getUserRole(this.getUserId());
		if (role == null)
		{
			throw new SessionDataException("No current role");
		}
		return role.getId();
	}

	/**
	 * Get a site property by name
	 *
	 * @param name Property name
	 * @throws IdUnusedException, SessionDataException
	 * @return The property value (null if none)
	 */
	private String getSiteProperty(String name) throws IdUnusedException, SessionDataException
	{
		Site site;

		site = SiteService.getSite(getSiteId());
		return site.getProperties().getProperty(name);
	}

	/**
	 * Lookup value for requested macro name
	 */
	private String getMacroValue(String macroName)
	{
		try
		{
			if (macroName.equals(MACRO_USER_ID))
			{
				return this.getUserId();
			}
			if (macroName.equals(MACRO_USER_EID))
			{
				return this.getUserEid();
			}
			if (macroName.equals(MACRO_USER_FIRST_NAME))
			{
				return this.getUser().getFirstName();
			}
			if (macroName.equals(MACRO_USER_LAST_NAME))
			{
				return this.getUser().getLastName();
			}

			if (macroName.equals(MACRO_SITE_ID))
			{
				return getSiteId();
			}
			if (macroName.equals(MACRO_USER_ROLE))
			{
				return this.getUserRole();
			}
			if (macroName.equals(MACRO_SESSION_ID))
			{
				return this.getSessionId();
			}

			if (macroName.startsWith("${"+MACRO_CLASS_SITE_PROP)) 
			{
				macroName = macroName.substring(2); // Remove leading "${"
				macroName = macroName.substring(0, macroName.length()-1); // Remove trailing "}" 
				
				// at this point we have "SITE_PROP:some-property-name"
				// separate the property name from the prefix then return the property value
				String[] sitePropertyKey = macroName.split(":");
				
				if (sitePropertyKey != null && sitePropertyKey.length > 1) {	
				
					String sitePropertyValue = getSiteProperty(sitePropertyKey[1]);
	
					return (sitePropertyValue == null) ? "" : sitePropertyValue;
				
				}
			}
		}
		catch (Throwable throwable)
		{
			return "";
		}
		/*
		 * An unsupported macro: use the original text "as is"
		 */
		return macroName;
	}

	/**
	 * Expand one macro reference
	 * @param text Expand macros found in this text
	 * @param macroName Macro name
	 */
	private void expand(StringBuilder sb, String macroName)
	{
		int index;

		/*
		 * Replace every occurance of the macro in the parameter list
		 */
		index = sb.indexOf(macroName);
		while (index != -1)
		{
			String  macroValue = URLEncoder.encode(getMacroValue(macroName));

			sb.replace(index, (index + macroName.length()), macroValue);
			index = sb.indexOf(macroName, (index + macroValue.length()));
		}
	}

	/**
	 * Expand macros, inserting session and site information
	 * @param originalText Expand macros found in this text
	 * @return [possibly] Updated text
	 */
	private String doMacroExpansion(String originalText)
	{
		StringBuilder  sb;

		/*
		 * Quit now if no macros are embedded in the text
		 */
		if (originalText.indexOf("${") == -1)
		{
			return originalText;
		}
		/*
		 * Expand each macro
		 */
		sb = new StringBuilder(originalText);

		Iterator i = allowedMacrosList.iterator();
		
		while(i.hasNext()) {
			
			String macro = (String) i.next();
		
			expand(sb, macro);
			
		}

		return sb.toString();
	}

	/**
	 * Setup the velocity context and choose the template for the response.
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// do options if we are in options mode
		if (MODE_OPTIONS.equals(state.getAttribute(STATE_MODE)))
		{
			return buildOptionsPanelContext(portlet, context, rundata, state);
		}

		// if we rely on state (like all the other tools), we won't pick up any changes others make to the configuration till we are refreshed... -ggolden

		// set our configuration into the context for the vm
		context.put(URL, (String) state.getAttribute(URL));
		context.put(HEIGHT, state.getAttribute(HEIGHT));

		// set the resource bundle with our strings
		context.put("tlang", rb);

		// setup for the options menu if needed
		
		String hideOptions = (String) state.getAttribute(HIDE_OPTIONS);
		
		
		if (hideOptions != null && "true".equalsIgnoreCase(hideOptions)) 
		{
			// always hide Options menu if hide.options is specified
		} else if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
				
		{
			context.put("options_title", ToolManager.getCurrentPlacement().getTitle() + " " + rb.getString("gen.options"));
		}
	

		return (String) getContext(rundata).get("template");
	}

	/**
	 * Setup the velocity context and choose the template for options.
	 */
	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		// provide the source, and let the user edit, if not special
		String special = (String) state.getAttribute(SPECIAL);
		if (special == null)
		{
			String source = (String) state.getAttribute(SOURCE);
			if (source == null) source = "";
			context.put(SOURCE, source);
			context.put("heading", rb.getString("gen.custom"));
		}

		// set the heading based on special
		else
		{
			if (SPECIAL_SITE.equals(special))
			{
				context.put("heading", rb.getString("gen.custom.site"));
			}

			else if (SPECIAL_WORKSPACE.equals(special))
			{
				context.put("heading", rb.getString("gen.custom.workspace"));
			}

			else if (SPECIAL_WORKSITE.equals(special))
			{
				context.put("heading", rb.getString("gen.custom.worksite"));

				// for worksite, also include the Site's infourl and description
				try
				{
					Site s = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());

					String infoUrl = StringUtil.trimToNull(s.getInfoUrl());
					if (infoUrl != null)
					{
						context.put("info_url", infoUrl);
					}

					String description = StringUtil.trimToNull(s.getDescription());
					if (description != null)
					{
						context.put("description", description);
					}
				}
				catch (Throwable e)
				{
				}
			}

			else
			{
				context.put("heading", rb.getString("gen.custom"));
			}
		}

		boolean selected = false;
		String height = state.getAttribute(HEIGHT).toString();
		for (int i = 0; i < ourPixels.length; i++)
		{
			if (height.equals(ourPixels[i]))
			{
				selected = true;
				continue;
			}
		}
		if (!selected)
		{
			String[] strings = height.trim().split("px");
			context.put("custom_height", strings[0]);
			height = rb.getString("gen.heisomelse");
		}
		context.put(HEIGHT, height);

		context.put(TITLE, state.getAttribute(TITLE));
		context.put("tlang", rb);

		context.put("doUpdate", BUTTON + "doConfigure_update");
		context.put("doCancel", BUTTON + "doCancel");
		
		context.put("form_tool_title", FORM_TOOL_TITLE);
		context.put("form_page_title", FORM_PAGE_TITLE);

		// if we are part of a site, and the only tool on the page, offer the popup to edit
		Placement placement = ToolManager.getCurrentPlacement();
		ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
		if ((state.getAttribute(SPECIAL) == null) && (toolConfig != null))
		{
			try
			{
				Site site = SiteService.getSite(toolConfig.getSiteId());
				SitePage page = site.getPage(toolConfig.getPageId());

				// if this is the only tool on that page, update the page's title also
				if ((page.getTools() != null) && (page.getTools().size() == 1))
				{
					context.put("showPopup", Boolean.TRUE);
					context.put("popup", Boolean.valueOf(page.isPopUp()));
					
					context.put("pageTitleEditable", Boolean.TRUE);
					context.put("page_title", (String) state.getAttribute(STATE_PAGE_TITLE));
				}
			}
			catch (Throwable e)
			{
			}
		}

		// pick the "-customize" template based on the standard template name
		String template = (String) getContext(data).get("template");

		// pick the site customize template if we are in that mode
		if (SPECIAL_WORKSITE.equals(special))
		{
			template = template + "-site-customize";
		}
		else
		{
			template = template + "-customize";
		}

		return template;
	}

	/**
	 * Handle the configure context's update button
	 */
	public void doConfigure_update(RunData data, Context context)
	{
		// TODO: if we do limit the initState() calls, we need to make sure we get a new one after this call -ggolden

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		Placement placement = ToolManager.getCurrentPlacement();

		// get the site toolConfiguration, if this is part of a site.
		ToolConfiguration toolConfig = SiteService.findTool(placement.getId());

		// read source if we are not special
		if (state.getAttribute(SPECIAL) == null)
		{
			String source = StringUtil.trimToZero(data.getParameters().getString(SOURCE));
			if ((source != null) && (source.length() > 0) && (!source.startsWith("/")) && (source.indexOf("://") == -1))
			{
				source = "http://" + source;
			}

			// update state
			// state.setAttribute(SOURCE, source);
			placement.getPlacementConfig().setProperty(SOURCE, source);
		}

		else if (SPECIAL_WORKSITE.equals(state.getAttribute(SPECIAL)))
		{
			String infoUrl = StringUtil.trimToNull(data.getParameters().getString("infourl"));
			if ((infoUrl != null) && (infoUrl.length() > 0) && (!infoUrl.startsWith("/")) && (infoUrl.indexOf("://") == -1))
			{
				infoUrl = "http://" + infoUrl;
			}
			String description = StringUtil.trimToNull(data.getParameters().getString("description"));

			// update the site info
			try
			{
				SiteService.saveSiteInfo(ToolManager.getCurrentPlacement().getContext(), description, infoUrl);
			}
			catch (Throwable e)
			{
			}
		}

		// height
		String height = data.getParameters().getString(HEIGHT);
		if (height.equals(rb.getString("gen.heisomelse")))
		{
			String customHeight = data.getParameters().getString(CUSTOM_HEIGHT);
			if ((customHeight != null) && (!customHeight.equals("")))
			{
				if (!checkDigits(customHeight))
				{
					addAlert(state, rb.getString("java.alert.pleentval"));
					return;
				}
				state.setAttribute(HEIGHT, customHeight);
				height = customHeight + "px";
				state.setAttribute(HEIGHT, height);
				placement.getPlacementConfig().setProperty(HEIGHT, height);
			}
			else
			{
				addAlert(state, rb.getString("java.alert.pleentval"));
				return;
			}
		}
		else
		{
			state.setAttribute(HEIGHT, height);
			placement.getPlacementConfig().setProperty(HEIGHT, height);
		}

		// title
		String title = data.getParameters().getString(TITLE);
		// state.setAttribute(TITLE, title);
		if (StringUtil.trimToNull(title) == null)
		{
			addAlert(state, rb.getString("gen.tootit"));
			return;			
		}
		placement.setTitle(title);
		
		// for web content tool, if it is a site page tool, and the only tool on the page, update the page title / popup.
		if ((state.getAttribute(SPECIAL) == null) && (toolConfig != null))
		{
			try
			{
				Site site = SiteService.getSite(toolConfig.getSiteId());
				SitePage page = site.getPage(toolConfig.getPageId());

				// if this is the only tool on that page, update the page's title also
				if ((page.getTools() != null) && (page.getTools().size() == 1))
				{
					// TODO: save site page title? -ggolden
					String newPageTitle = data.getParameters().getString(FORM_PAGE_TITLE);
					String currentPageTitle = (String) state.getAttribute(STATE_PAGE_TITLE);
					
					if (StringUtil.trimToNull(newPageTitle) == null)
					{
						addAlert(state, rb.getString("gen.pagtit"));
						return;		
					}
					else if (!newPageTitle.equals(currentPageTitle))
					{
						page.setTitle(newPageTitle);
						state.setAttribute(STATE_PAGE_TITLE, newPageTitle);
					}

					// popup
					boolean popup = data.getParameters().getBoolean("popup");
					page.setPopup(popup);

					SiteService.save(site);
				}
			}
			catch (Exception ignore)
			{
			}
		}

		// save
		// TODO: we might have just saved the entire site, so this would not be needed -ggolden
		placement.save();

		// we are done with customization... back to the main mode
		state.removeAttribute(STATE_MODE);

		// deliver an update to the title panel (to show the new title)
		// String titleId = titlePanelUpdateId(peid);
		// schedulePeerFrameRefresh(titleId);

		// refresh the whole page, since popup and title may have changed
		scheduleTopRefresh();
	}

	/**
	 * doCancel called for form input tags type="submit" named="eventSubmit_doCancel" cancel the options process
	 */
	public void doCancel(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// we are done with customization... back to the main mode
		state.removeAttribute(STATE_MODE);
		state.removeAttribute(STATE_MESSAGE);
	}

	/**
	 * Check if the string from user input contains any characters other than digits
	 * 
	 * @param height
	 *        String from user input
	 * @return True if all are digits. Or False if any is not digit.
	 */
	private boolean checkDigits(String height)
	{
		for (int i = 0; i < height.length(); i++)
		{
			if (VALID_DIGITS.indexOf(height.charAt(i)) == -1) return false;
		}
		return true;
	}

	/**
	 * Note a "local" problem (we failed to get session or site data)
	 */
	private static class SessionDataException extends Exception
	{
		public SessionDataException(String text)
		{
			super(text);
		}
	}
}
