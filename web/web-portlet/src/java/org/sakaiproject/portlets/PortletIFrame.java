/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005-2013 The Sakai Foundation.
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

package org.sakaiproject.portlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.portlet.util.JSPHelper;
import org.sakaiproject.portlet.util.VelocityHelper;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

// Velocity

/**
 * a simple PortletIFrame Portlet
 */
@Slf4j
public class PortletIFrame extends GenericPortlet {

	/** Event for accessing the web-content tool */
	protected final static String EVENT_ACCESS_WEB_CONTENT = "webcontent.read";
	
	/** Event for modifying the web-content tool configuration */
	protected final static String EVENT_REVISE_WEB_CONTENT = "webcontent.revise";
	
	// This is old-style internationalization (i.e. not dynamic based
	// on user preference) to do that would make this depend on
	// Sakai Unique APIs. :(
	// private static ResourceBundle rb =  ResourceBundle.getBundle("iframe");
	protected static ResourceLoader rb = new ResourceLoader("iframe");

	protected final FormattedText formattedText = ComponentManager.get(FormattedText.class);

	private final VelocityHelper vHelper = new VelocityHelper();

	VelocityEngine vengine = null;

	private PortletContext pContext;

    private AuthzGroupService authzGroupService;

	// TODO: Perhaps these constancts should come from portlet.xml

	/** The source URL, in config and context. */
	protected final static String SOURCE = "source";

	/** The value in context for the source URL to actually used, as computed from special and URL. */
	protected final static String URL = "url";

	/** The height, in config and context. */
	protected final static String HEIGHT = "height";

	/** The custom height from user input * */
	protected final static String CUSTOM_HEIGHT = "customNumberField";

	protected final String POPUP = "popup";
	protected final String MAXIMIZE = "maximize";

	protected final static String TITLE = "title";

	private static final String FORM_PAGE_TITLE = "title-of-page";

	private static final int MAX_TITLE_LENGTH = 99;

    private static final int MAX_SITE_INFO_URL_LENGTH = 255;

	private static String ALERT_MESSAGE = "sakai:alert-message";

    /** The Annotated URL Tool's url attribute, in config and context. */
    protected final static String TARGETPAGE_URL = "TargetPageUrl";

    /** The Annotated URL Tool's name attribute, in config and context. */
    protected final static String TARGETPAGE_NAME = "TargetPageName";

    /** The Annotated URL Tool's text attribute, in config and context. */
    protected final static String ANNOTATED_TEXT = "desp";

    /** The special attribute in config and context. */
    protected final static String SPECIAL = "special";

    /** Special value for site. */
    protected final static String SPECIAL_SITE = "site";

   /** Special value for Annotated URL Tool. */
    protected final static String SPECIAL_ANNOTATEDURL = "annotatedurl";

    /** Special value for myworkspace. */
    protected final static String SPECIAL_WORKSPACE = "workspace";

    /** Special value for worksite. */
    protected final static String SPECIAL_WORKSITE = "worksite";

    /** Support an external url defined in sakai.properties, in config and context. */
    protected final static String SAKAI_PROPERTIES_URL_KEY = "sakai.properties.url.key";

    /** If set, always hide the OPTIONS button */
    protected final static String HIDE_OPTIONS = "hide.options";

    private final static String PASS_PID = "passthroughPID";

    /**
     * Expand macros to insert session information into the URL?
     */
    private final static String MACRO_EXPANSION       = "expandMacros";

    /** Macro name: Site id (GUID) */
    protected static final String MACRO_SITE_ID             = "$SITE_ID";
    /** Macro name: User id */
    protected static final String MACRO_USER_ID             = "$USER_ID";
    /** Macro name: User enterprise id */
    protected static final String MACRO_USER_EID            = "$USER_EID";
    /** Macro name: First name */
    protected static final String MACRO_USER_FIRST_NAME     = "$USER_FIRST_NAME";
    /** Macro name: Last name */
    protected static final String MACRO_USER_LAST_NAME      = "$USER_LAST_NAME";
    /** Macro name: Role */
    protected static final String MACRO_USER_ROLE           = "$USER_ROLE";

    private static final String MACRO_CLASS_SITE_PROP = "SITE_PROP:";
   
    private static final String IFRAME_ALLOWED_MACROS_PROPERTY = "iframe.allowed.macros";

    private static final String MACRO_DEFAULT_ALLOWED = "$USER_ID,$USER_EID,$USER_FIRST_NAME,$USER_LAST_NAME,$SITE_ID,$USER_ROLE";

	// Default is six hours
    private static final String IFRAME_XFRAME_CACHETIME = "iframe.xframe.cachetime";
    private static final int IFRAME_XFRAME_CACHETIME_DEFAULT = 3600*1000*6;

    private static final String XFRAME_LAST_TIME = "xframe-last-time";
    private static final String XFRAME_LAST_STATUS = "xframe-last-status";

    private static final String IFRAME_XFRAME_LOADTIME = "iframe.xframe.loadtime";
    private static final int IFRAME_XFRAME_LOADTIME_DEFAULT = 8000;

    private static long xframeCache = IFRAME_XFRAME_CACHETIME_DEFAULT;
    private static long xframeLoad = IFRAME_XFRAME_LOADTIME_DEFAULT;

	// Regular expressions
    private static final String IFRAME_XFRAME_POPUP = "iframe.xframe.popup";
    private static final String IFRAME_XFRAME_INLINE = "iframe.xframe.inline";

    public final static String CURRENT_HTTP_REQUEST = "org.sakaiproject.util.RequestFilter.http_request";

    private static ArrayList allowedMacrosList;
    static
    {
        xframeCache = IFRAME_XFRAME_CACHETIME_DEFAULT;
        String xframeCacheS = 
            ServerConfigurationService.getString(IFRAME_XFRAME_CACHETIME, null);
        try { 
            if ( xframeCacheS != null ) xframeCache = Long.parseLong(xframeCacheS);
        } catch (NumberFormatException nfe) {
            xframeCache = IFRAME_XFRAME_CACHETIME_DEFAULT;
        }

        xframeLoad = IFRAME_XFRAME_LOADTIME_DEFAULT;
        String xframeLoadS = 
            ServerConfigurationService.getString(IFRAME_XFRAME_LOADTIME, null);
        try { 
            if ( xframeLoadS != null ) xframeLoad = Long.parseLong(xframeLoadS);
        } catch (NumberFormatException nfe) {
            xframeLoad = IFRAME_XFRAME_LOADTIME_DEFAULT;
        }

        allowedMacrosList = new ArrayList();

        
        final String allowedMacros =
            ServerConfigurationService.getString(IFRAME_ALLOWED_MACROS_PROPERTY, MACRO_DEFAULT_ALLOWED)
            // Remove braces from allowedMacros as those were previously allowed so this is for compatibility 
            .replaceAll("\\{|\\}", "");
        String parts[] = allowedMacros.split(",");

        if(parts != null) {

            for(int i = 0; i < parts.length; i++) {

                allowedMacrosList.add(parts[i]);

            }

        }
    }

 /** Choices of pixels displayed in the customization page */
    public String[] ourPixels = { "300px", "450px", "600px", "750px", "900px", "1200px", "1800px", "2400px" };


	// If the property is final, the property wins.  If it is not final,
	// the portlet preferences take precedence.
	public String getTitleString(RenderRequest request)
	{
		Placement placement = ToolManager.getCurrentPlacement();
		return placement.getTitle();
	}

	public void init(PortletConfig config) throws PortletException {
		super.init(config);
		authzGroupService = ComponentManager.get(AuthzGroupService.class);

		pContext = config.getPortletContext();
		try {
			vengine = vHelper.makeEngine(pContext);
		}
		catch(Exception e)
		{
			throw new PortletException("Cannot initialize Velocity ", e);
		}
		log.info("iFrame Portlet vengine="+vengine+" rb="+rb);
	}

	private void addAlert(ActionRequest request,String message) {
		PortletSession pSession = request.getPortletSession(true);
		pSession.setAttribute(ALERT_MESSAGE, message);
	}

	private void sendAlert(RenderRequest request, Context context) {
		PortletSession pSession = request.getPortletSession(true);
		String str = (String) pSession.getAttribute(ALERT_MESSAGE);
		pSession.removeAttribute(ALERT_MESSAGE);
		if ( str != null && str.length() > 0 ) context.put("alertMessage", formattedText.escapeHtml(str, false));
	}

	// Render the portlet - this is not supposed to change the state of the portlet
	// Render may be called many times so if it changes the state - that is tacky
	// Render will be called when someone presses "refresh" or when another portlet
	// onthe same page is handed an Action.
	public void doView(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {
			response.setContentType("text/html");

			// log.info("==== doView called ====");

			// Grab that underlying request to get a GET parameter
			ServletRequest req = (ServletRequest) ThreadLocalManager.get(CURRENT_HTTP_REQUEST);
			String popupDone = req.getParameter("sakai.popup");

			PrintWriter out = response.getWriter();
			Context context = new VelocityContext();
			Placement placement = ToolManager.getCurrentPlacement();

			if (placement == null) {
				out.println(rb.getString("error.placement.isNull"));
				return;
			}
            Properties config = getAllProperties(placement);

			response.setTitle(placement.getTitle());
			String source = config.getProperty(SOURCE);
			if ( source == null ) source = "";
			String height = config.getProperty(HEIGHT);
			if ( height == null ) height = "1200px";
            String sakaiPropertiesUrlKey = config.getProperty(SAKAI_PROPERTIES_URL_KEY);
            String hideOptions = config.getProperty(HIDE_OPTIONS);

            String special = getSpecial(config);

			// Handle the situation where we are displaying the worksite information
			if ( SPECIAL_WORKSITE.equals(special) ) {
				try
				{
					// If the site does not have an info url, we show description or title
					Site s = SiteService.getSite(placement.getContext());
					String rv = StringUtils.trimToNull(s.getInfoUrlFull());
					if (rv == null)
					{
						String siteInfo = StringUtils.trimToNull(s.getDescription());
						if ( siteInfo == null ) {
							siteInfo = StringUtils.trimToNull(s.getTitle());
						}
						StringBuilder alertMsg = new StringBuilder();
						if ( siteInfo != null ) siteInfo = formattedText.processFormattedText(siteInfo, alertMsg);
						context.put("cdnVersion", PortalUtils.getCDNQuery());
						context.put("siteInfo", siteInfo);
						context.put("height",height);
						vHelper.doTemplate(vengine, "/vm/info.vm", context, out);
						return;
					}
				}
				catch (Exception any)
				{
					log.error(any.getMessage(), any);
				}
			}

			boolean popup = "true".equals(placement.getPlacementConfig().getProperty(POPUP));
			boolean maximize = "true".equals(placement.getPlacementConfig().getProperty(MAXIMIZE));

            // set the pass_pid parameter
            String passPidStr = config.getProperty(PASS_PID, "false");
            boolean passPid = "true".equalsIgnoreCase(passPidStr);

            // Set the macro expansion
            String macroExpansionStr = config.getProperty(MACRO_EXPANSION, "true");
            boolean macroExpansion = ! ( "false".equalsIgnoreCase(macroExpansionStr));

            // Compute the URL
            String url = sourceUrl(special, source, placement.getContext(), macroExpansion, passPid, placement.getId(), sakaiPropertiesUrlKey);

            //log.info("special="+special+" source="+source+" pgc="+placement.getContext()+" macroExpansion="+macroExpansion+" passPid="+passPid+" PGID="+placement.getId()+" sakaiPropertiesUrlKey="+sakaiPropertiesUrlKey+" url="+url);

			if ( url != null && url.trim().length() > 0 ) {
				url = sanitizeHrefURL(url);
				if ( url == null || ! validateURL(url) ) {
					log.warn("invalid URL suppressed placement="+placement.getId()+" site="+placement.getContext()+" url="+url);
					url = "about:blank";
				}

				// Check if the site sets X-Frame options
				popup = popup || popupXFrame(request, placement, url);

                Session session = SessionManager.getCurrentSession();
                String csrfToken = (String) session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
                if ( csrfToken != null ) context.put("sakai_csrf_token", csrfToken);
				context.put("tlang", rb);
				context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("PortletIFrame"));
				context.put("validator", formattedText);
				context.put("source",url);
				context.put("height",height);
				context.put("browser-feature-allow", ServerConfigurationService.getBrowserFeatureAllowString());
				sendAlert(request,context);
				context.put("popup", Boolean.valueOf(popup));
				context.put("popupdone", Boolean.valueOf(popupDone != null));
				context.put("maximize", Boolean.valueOf(maximize));
				context.put("placement", placement.getId().replaceAll("[^a-zA-Z0-9]","_"));
				context.put("loadTime", new Long(xframeLoad));

				// SAK-23566 capture the view calendar events
				if (placement != null && placement.getContext() != null && placement.getId() != null) {
				    EventTrackingService ets = (EventTrackingService) ComponentManager.get(EventTrackingService.class);
				    if (ets != null) {
				        String eventRef = "/web/"+placement.getContext()+"/id/"+placement.getId()+"/url/"+URLEncoder.encode(url, "UTF-8");
				        eventRef = StringUtils.abbreviate(eventRef, 240); // ensure the ref won't pass 255 chars
				        String etsProperty = (StringUtils.trimToNull(config.getProperty(EVENT_ACCESS_WEB_CONTENT)) != null) ? config.getProperty(EVENT_ACCESS_WEB_CONTENT) : EVENT_ACCESS_WEB_CONTENT;
				        ets.post(ets.newEvent(etsProperty, eventRef, false));
				    }
				}

                // TODO: state.setAttribute(TARGETPAGE_URL,config.getProperty(TARGETPAGE_URL));
                // TODO: state.setAttribute(TARGETPAGE_NAME,config.getProperty(TARGETPAGE_NAME));

				vHelper.doTemplate(vengine, "/vm/main.vm", context, out);
			} else {
				out.println("Not yet configured");
			}

            // TODO: state.setAttribute(EVENT_ACCESS_WEB_CONTENT, config.getProperty(EVENT_ACCESS_WEB_CONTENT));
            // TODO: state.setAttribute(EVENT_REVISE_WEB_CONTENT, config.getProperty(EVENT_REVISE_WEB_CONTENT));

			// log.info("==== doView complete ====");
		}

    // Determine if we should pop up due to an X-Frame-Options : [SAMEORIGIN]
    public boolean popupXFrame(RenderRequest request, Placement placement, String url) 
    {
        if ( xframeCache < 1 ) return false;

        // Only check http:// and https:// urls
        if ( ! (url.startsWith("http://") || url.startsWith("https://")) ) return false;

        // Check the "Always POPUP" and "Always INLINE" regular expressions
        String pattern = null;
        Pattern p = null;
        Matcher m = null;
        pattern = ServerConfigurationService.getString(IFRAME_XFRAME_POPUP, null);
        if ( pattern != null && pattern.length() > 1 ) {
            p = Pattern.compile(pattern);
            m = p.matcher(url.toLowerCase());
            if ( m.find() ) {
                return true;
            }
        }
        pattern = ServerConfigurationService.getString(IFRAME_XFRAME_INLINE, null);
        if ( pattern != null && pattern.length() > 1 ) {
            p = Pattern.compile(pattern);
            m = p.matcher(url.toLowerCase());
            if ( m.find() ) {
                return false;
            }
        }

        // Don't check Local URLs
        String serverUrl = ServerConfigurationService.getServerUrl();
        if ( url.startsWith(serverUrl) ) return false;
        if ( url.startsWith(ServerConfigurationService.getAccessUrl()) ) return false;

        // Force http:// to pop-up if we are https://
        if ( request.isSecure() || ( serverUrl != null && serverUrl.startsWith("https://") ) ) {
            if ( url.startsWith("http://") ) return true;
        }

        // Check to see if time has expired...
        Date date = new Date();
        long nowTime = date.getTime();
        
        String lastTimeS = placement.getPlacementConfig().getProperty(XFRAME_LAST_TIME);
        long lastTime = -1;
        try {
            lastTime = Long.parseLong(lastTimeS);
        } catch (NumberFormatException nfe) {
            lastTime = -1;
        }

        log.debug("lastTime="+lastTime+" nowTime="+nowTime);

        if ( lastTime > 0 && nowTime < lastTime + xframeCache ) {
            String lastXF = placement.getPlacementConfig().getProperty(XFRAME_LAST_STATUS);
            log.debug("Status from placement="+lastXF);
            return "true".equals(lastXF);
        }

        placement.getPlacementConfig().setProperty(XFRAME_LAST_TIME, String.valueOf(nowTime));
        boolean retval = false;
        try {
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection con =
                (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");

            String sakaiVersion = ServerConfigurationService.getString("version.sakai", "?");
            con.setRequestProperty("User-Agent","Java Sakai/"+sakaiVersion);

            Map headerfields = con.getHeaderFields();
            Set headers = headerfields.entrySet(); 
            for(Iterator i = headers.iterator(); i.hasNext();) { 
                Map.Entry map = (Map.Entry)i.next();
                String key = (String) map.getKey();
                if ( key == null ) continue;
                key = key.toLowerCase();
                if ( ! "x-frame-options".equals(key) ) continue;

                // Since the valid entries are SAMEORIGIN, DENY, or ALLOW-URI
                // we can pretty much assume the answer is "not us" if the header
                // is present
                retval = true;
                break;
            }

        }
        catch (Exception e) {
            // Fail pretty silently because this could be pretty chatty with bad urls and all
            log.debug(e.getMessage());
            retval = false;
        }
        placement.getPlacementConfig().setProperty(XFRAME_LAST_STATUS, String.valueOf(retval));
        // Permanently set popup to true as we don't expect that a site will go back
        if ( retval == true ) placement.getPlacementConfig().setProperty(POPUP, "true");
        placement.save();
        log.debug("Retrieved="+url+" XFrame="+retval);
        return retval;
    }

	public void doEdit(RenderRequest request, RenderResponse response)
		throws PortletException, IOException 
    {
			// log.info("==== doEdit called ====");
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			String title = getTitleString(request);
			if ( title != null ) response.setTitle(title);

			Context context = new VelocityContext();
            Session session = SessionManager.getCurrentSession();
            String csrfToken = (String) session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
            if ( csrfToken != null ) context.put("sakai_csrf_token", csrfToken);
			context.put("tlang", rb);
			context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("PortletIFrame"));
			context.put("validator", formattedText);
			sendAlert(request,context);

			PortletURL url = response.createActionURL();
			context.put("actionUrl", url.toString());
			context.put("doCancel", "sakai.cancel");
			context.put("doUpdate", "sakai.update");

			Placement placement = ToolManager.getCurrentPlacement();
			if (placement == null) {
				out.println(rb.getString("error.placement.isNull"));
				return;
			}
            Properties config = getAllProperties(placement);
            String special = getSpecial(config);
			context.put("title", formattedText.escapeHtml(placement.getTitle(), false));
			String fa_icon = placement.getPlacementConfig().getProperty("imsti.fa_icon");
			if ( fa_icon != null ) context.put("fa_icon", fa_icon );
			String source = placement.getPlacementConfig().getProperty(SOURCE);
			if ( source == null ) source = "";
			if ( special == null ) context.put("source",source);
			String height = placement.getPlacementConfig().getProperty(HEIGHT);
			if ( height == null ) height = "1200px";
			context.put("height",height);

			ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
			if ( toolConfig != null )
			{
				try
				{
					Site site = SiteService.getSite(toolConfig.getSiteId());
					String siteId = site.getId();
					SitePage page = site.getPage(toolConfig.getPageId());
					context.put("siteId", siteId);
					// if this is the only tool on that page, update the page's title also
					if ((page.getTools() != null) && (page.getTools().size() == 1))
					{
						context.put("showPopup", Boolean.TRUE);
						boolean popup = "true".equals(placement.getPlacementConfig().getProperty(POPUP));
						context.put("popup", Boolean.valueOf(popup));

						boolean maximize = "true".equals(placement.getPlacementConfig().getProperty(MAXIMIZE));
						context.put("maximize", Boolean.valueOf(maximize));

						context.put("pageTitleEditable", Boolean.TRUE);
						context.put("page_title",  formattedText.escapeHtml(page.getTitle(), false));
					}
				}
				catch (Throwable e)
				{
				}
			}

		    if (special == null)
		    {
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
					    String siteId = s.getId();

						String infoUrl = StringUtils.trimToNull(s.getInfoUrl());
						if (infoUrl != null)
						{
							//Check if infoUrl is relative? and prepend the server url
							if(infoUrl.startsWith("/") && !infoUrl.contains("://")){
								infoUrl = ServerConfigurationService.getServerUrl() + infoUrl;
							}
							//Check if infoUrl is relative? and prepend the server url
							String serverUrl = ServerConfigurationService.getServerUrl();
							if(infoUrl.startsWith("/") && infoUrl.indexOf("://") == -1){
								infoUrl = serverUrl + infoUrl;
							}
							context.put("info_url", formattedText.escapeHtmlFormattedTextarea(infoUrl));
						}

					    String description = StringUtils.trimToNull(s.getDescription());
					    if (description != null)
					    {
	                        description = formattedText.escapeHtmlFormattedTextarea(description);
						    context.put("description", description);
					    }
				    }
				    catch (Throwable e)
				    {
				    }
			    }
			    else if (SPECIAL_ANNOTATEDURL.equals(special))
			    {
				
				    context.put("heading", rb.getString("gen.custom.annotatedurl"));

				    // for Annotated URL Tool page, also include the description
				    try
				    {		
					    String desp = config.getProperty(ANNOTATED_TEXT);
					    context.put("description", desp);
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
		    context.put("height", height);

		    // output the max limit 
		    context.put("max_length_title", MAX_TITLE_LENGTH);
		    context.put("max_length_info_url", MAX_SITE_INFO_URL_LENGTH);

            String template = "/vm/edit.vm";
            if (SPECIAL_SITE.equals(special)) template = "/vm/edit-site.vm";
            if (SPECIAL_WORKSITE.equals(special)) template = "/vm/edit-site.vm";
            if (SPECIAL_ANNOTATEDURL.equals(special)) template = "/vm/edit-annotatedurl.vm";
            // log.info("EDIT TEMP="+template+" special="+special);

			// capture the revise events
			if (placement != null && placement.getContext() != null && placement.getId() != null) {
			    EventTrackingService ets = (EventTrackingService) ComponentManager.get(EventTrackingService.class);
			    if (ets != null) {
			        String eventRef = "/web/"+placement.getContext()+"/id/"+placement.getId()+"/url/"+URLEncoder.encode(source, "UTF-8");
			        eventRef = StringUtils.abbreviate(eventRef, 240); // ensure the ref won't pass 255 chars
			        String etsProperty = (StringUtils.trimToNull(config.getProperty(EVENT_REVISE_WEB_CONTENT)) != null) ? config.getProperty(EVENT_REVISE_WEB_CONTENT) : EVENT_REVISE_WEB_CONTENT;
					ets.post(ets.newEvent(etsProperty, eventRef, false));
			    }
			}

			vHelper.doTemplate(vengine, template, context, out);

			// log.info("==== doEdit done ====");
		}

	public void doHelp(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {
			// log.info("==== doHelp called ====");
			// sendToJSP(request, response, "/help.jsp");
			JSPHelper.sendToJSP(pContext, request, response, "/help.jsp");
			// log.info("==== doHelp done ====");
		}

	// Process action is called for action URLs / form posts, etc
	// Process action is called once for each click - doView may be called many times
	// Hence an obsession in process action with putting things in session to 
	// Send to the render process.
	public void processAction(ActionRequest request, ActionResponse response)
		throws PortletException, IOException {

			// log.info("==== processAction called ====");

			PortletSession pSession = request.getPortletSession(true);

			// Our first challenge is to figure out which action we want to take
			// The view selects the "next action" either as a URL parameter
			// or as a hidden field in the POST data - we check both

			String doCancel = request.getParameter("sakai.cancel");
			String doUpdate = request.getParameter("sakai.update");

			// Our next challenge is to pick which action the previous view
			// has told us to do.  Note that the view may place several actions
			// on the screen and the user may have an option to pick between
			// them.  Make sure we handle the "no action" fall-through.

			pSession.removeAttribute("error.message");

			if ( doCancel != null ) {
				response.setPortletMode(PortletMode.VIEW);
			} else if ( doUpdate != null ) {
				processActionEdit(request, response);
			} else {
				// log.info("Unknown action");
				response.setPortletMode(PortletMode.VIEW);
			}

			// log.info("==== End of ProcessAction  ====");
		}

	public void processActionEdit(ActionRequest request, ActionResponse response)
		throws PortletException, IOException 
		{
			// TODO: Check Role

			// Stay in EDIT mode unless we are successful
			response.setPortletMode(PortletMode.EDIT);

			// get the site toolConfiguration, if this is part of a site.
			Placement placement = ToolManager.getCurrentPlacement();
			if (placement == null) {
				log.error(rb.getString("error.placement.isNull"));
				return;
			}
			ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
            Properties config = getAllProperties(placement);
            String special = getSpecial(config);

            // Get and verify the source
			String source = StringUtils.trimToEmpty(request.getParameter("source"));

            // If this is a normal placement we do not allow blank (i.e. not special)
            if ( special == null ) {
                if (StringUtils.isBlank(source))
                {
                    addAlert(request, rb.getString("gen.url.empty"));
                    return;
                }
            }

            // If we have a URL from the user, lets validate it
            if ((StringUtils.isNotBlank(source)) && (!validateURL(source)) ) {
                addAlert(request, rb.getString("gen.url.invalid"));
                return;
            }

            // update state
			if ( source == null ) source = "";
            placement.getPlacementConfig().setProperty(SOURCE, source);

            // site info url 
            String infoUrl = StringUtils.trimToNull(request.getParameter("infourl"));
            if (infoUrl != null && infoUrl.length() > MAX_SITE_INFO_URL_LENGTH)
            {
                addAlert(request, rb.getString("gen.info.url.toolong"));
                return;
            }

            // If we have an infourl from the user, lets validate it
            if ((StringUtils.isNotBlank(infoUrl)) && (!validateURL(infoUrl)) ) {
                addAlert(request, rb.getString("gen.url.invalid"));
                return;
            }

			String height = request.getParameter(HEIGHT);
			if (height.equals(rb.getString("gen.heisomelse")))
			{
				String customHeight = request.getParameter(CUSTOM_HEIGHT);
				if ((customHeight != null) && (!customHeight.equals("")))
				{
					if (!checkDigits(customHeight))
					{
						addAlert(request,rb.getString("java.alert.pleentval"));
						return;
					}
					height = customHeight + "px";
					placement.getPlacementConfig().setProperty(HEIGHT, height);
				}
				else
				{
					addAlert(request,rb.getString("java.alert.pleentval"));
					return;
				}
			}
			else
			{
				placement.getPlacementConfig().setProperty(HEIGHT, height);
			}

			// This will be null if editing a Web Content tool
			// An empty string can be valid from the instructor to clear out previous value
			String description = request.getParameter("description");

			// update the site info
			if (description != null || infoUrl != null)
			{
				try
				{
					// Need to save this processed/escaped
					String processedDescription = formattedText.processFormattedText(description, new StringBuilder());
					SiteService.saveSiteInfo(placement.getContext(), processedDescription, infoUrl);
				}
				catch (Throwable e)
				{
					log.warn("doConfigure_update attempting to saveSiteInfo", e);
				}
			}

			// title
			String title = request.getParameter(TITLE);
			if (StringUtils.isBlank(title))
			{
				addAlert(request,rb.getString("gen.tootit.empty"));
				return;			
				// SAK-19515 check for LENGTH of tool title
			} 
			else if (title.length() > MAX_TITLE_LENGTH)
			{
				addAlert(request,rb.getString("gen.tootit.toolong"));
				return;			
			}
			placement.setTitle(title);

			// icon
			String fa_icon = request.getParameter("fa_icon");
			if ( fa_icon != null && fa_icon.length() > 0 ) {
				placement.getPlacementConfig().setProperty("imsti.fa_icon",fa_icon);
			}

			try
			{
				Site site = SiteService.getSite(toolConfig.getSiteId());
				SitePage page = site.getPage(toolConfig.getPageId());
				if (page.isHomePage()) page.setHomeToolsTitleCustom(placement.getId());
				else page.setTitleCustom(true);

				// for web content tool, if it is a site page tool, and the only tool on the page, update the page title / popup.
				if (toolConfig != null && ! SPECIAL_WORKSITE.equals(special) && ! SPECIAL_WORKSPACE.equals(special) )
				{
					// if this is the only tool on that page, update the page's title also
					if ((page.getTools() != null) && (page.getTools().size() == 1))
					{
						String newPageTitle = request.getParameter(FORM_PAGE_TITLE);

						if (StringUtils.isBlank(newPageTitle))
						{
							addAlert(request,rb.getString("gen.pagtit.empty"));
							return;		
						}
						else if (newPageTitle.length() > MAX_TITLE_LENGTH)
						{
							addAlert(request,rb.getString("gen.pagtit.toolong"));
							return;			
						}
						page.setTitle(newPageTitle);				
					}
				}

				SiteService.save(site);
			}
			catch (Exception ignore)
			{
				log.warn("doConfigure_update: " + ignore);
			}

			// popup and maximize
			String spop = request.getParameter("popup");
			if ( ! "true".equals(spop) ) spop = "false";
			placement.getPlacementConfig().setProperty(POPUP, spop);
			String smax = request.getParameter("maximize");
			if ( ! "true".equals(smax) ) smax = "false";
			placement.getPlacementConfig().setProperty(MAXIMIZE, smax);

            // Make sure we re-check X-Frame-Options
            placement.getPlacementConfig().setProperty(XFRAME_LAST_STATUS, "");
            placement.getPlacementConfig().setProperty(XFRAME_LAST_TIME, "-1");
			placement.save();

            // Handle the infoUrl
            if (SPECIAL_WORKSITE.equals(special))
            {
                //Check info-url for null and empty
                if(StringUtils.isNotBlank(infoUrl)) {
                    // If the site info url has server url then make it a relative link.
                    Collection<String> serverNames = new ArrayList<String>();
                    //get the server name
                    serverNames.add(new URL(ServerConfigurationService.getServerUrl()).getHost());
                    serverNames.addAll(ServerConfigurationService.getInstance().getServerNameAliases());
                    for (String serverName : serverNames) {
                        // if the supplied url starts with protocol//serverName:port/
                        Pattern serverUrlPattern = Pattern.compile(String.format("^(https?:)?//%s:?\\d*/", serverName));
                        infoUrl = serverUrlPattern.matcher(infoUrl).replaceFirst("/");
                    }
                }
            }

			response.setPortletMode(PortletMode.VIEW);
		}

	/** Valid digits for custom height from user input **/
	protected static final String VALID_DIGITS = "0123456789";

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
	 * Get the special type of this placement, compensating for legacy patterns
	 */
	protected String getSpecial(Properties config)
    {
        String special = config.getProperty(SPECIAL);
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
            else if ("true".equals(config.getProperty("annotatedurl")))
            {
                special = SPECIAL_ANNOTATEDURL;
            }
        }
        return special;
    }

	/**
	 * Compute the actual URL we will used, based on the configuration special and source URLs
	 */
	protected String sourceUrl(String special, String source, String context, boolean macroExpansion, boolean passPid, String pid, String sakaiPropertiesUrlKey)
	{
		String rv = StringUtils.trimToNull(source);

		// if marked for "site", use the site intro from the properties
		if (SPECIAL_SITE.equals(special))
		{
			rv = StringUtils.trimToNull(getLocalizedURL("server.info.url"));
		}

		// if marked for "workspace", use the "user" site info from the properties
		else if (SPECIAL_WORKSPACE.equals(special))
		{
			rv = StringUtils.trimToNull(getLocalizedURL("myworkspace.info.url"));
		}

		// if marked for "worksite", use the setting from the site's definition
		else if (SPECIAL_WORKSITE.equals(special))
		{
			// set the url to the site of this request's config'ed url
			try
			{
				// get the site's info URL, if defined
				Site s = SiteService.getSite(context);
				rv = StringUtils.trimToNull(s.getInfoUrlFull());

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
			rv = StringUtils.trimToNull(ServerConfigurationService.getString(sakaiPropertiesUrlKey));
		}
		

		// if it's not special, and we have no value yet, set it to the webcontent instruction page, as configured
		if (rv == null || rv.equals("http://") || rv.equals("https://"))
		{
			rv = StringUtils.trimToNull(getLocalizedURL("webcontent.instructions.url"));
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

    /** Construct and return localized filepath, if it exists
     **/
    private String getLocalizedURL(String property) {
        String filename = ServerConfigurationService.getString(property);
        if ( filename == null || filename.trim().length()==0 )
            return filename;
        else
            filename = filename.trim();

        int extIndex = filename.lastIndexOf(".") >= 0 ? filename.lastIndexOf(".") : filename.length()-1;
        String ext = filename.substring(extIndex);
        String doc = filename.substring(0,extIndex);

        Locale locale = new ResourceLoader().getLocale();

        // You can only access inside the current context in Tomcat 8.
        // Tomcat 8 advises against unpacking the WARs so this isn't a good long term solution.
        String rootPath = getPortletConfig().getPortletContext().getRealPath("/");
        if (locale != null){
            // check if localized file exists for current language/locale/variant
            String localizedFile = doc + "_" + locale.toString() + ext;
            String filePath = rootPath+ ".."+localizedFile;
            if ( (new File(filePath)).exists() )
                return localizedFile;

            // otherwise, check if localized file exists for current language
            localizedFile = doc + "_" + locale.getLanguage() + ext;
            filePath = rootPath+ ".."+localizedFile;
            if ( (new File(filePath)).exists() )
                return localizedFile;
        }
        return filename;
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

		group = authzGroupService.getAuthzGroup("/site/" + getSiteId());
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

			if (macroName.startsWith("$"+MACRO_CLASS_SITE_PROP)) 
			{
				macroName = macroName.substring(1); // Remove leading "$"
				
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
		if (originalText.indexOf("$") == -1)
		{
			return originalText;
		}
		/*
		 * Expand each macro
		 */
		sb = new StringBuilder(originalText);
        // Remove braces from allowedMacros as those were previously allowed so this is for compatibility 
		originalText = originalText.replaceAll("\\{|\\}", "");

		Iterator i = allowedMacrosList.iterator();
		
		while(i.hasNext()) {
			
			String macro = (String) i.next();
		
			expand(sb, macro);
			
		}

		return sb.toString();
	}

    // Work around lack of final config values in placementConfig();
    private Properties getAllProperties(Placement placement)
    {
        Properties config = placement.getTool().getRegisteredConfig();
        Properties mconfig = placement.getPlacementConfig();
        for ( Object okey : mconfig.keySet() ) {
            String key = (String) okey;
            config.setProperty(key,mconfig.getProperty(key));
        }
        return config;
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

	// TODO: When FormattedText KNL-1105 is updated take those methods

    /* (non-Javadoc)
     * @see org.sakaiproject.util.api.FormattedText#validateURL(java.lang.String)
     */

    private static final String PROTOCOL_PREFIX = "http:";
    private static final String HOST_PREFIX = "http://127.0.0.1";
    private static final String ABOUT_BLANK = "about:blank";

    public boolean validateURL(String urlToValidate) {
		// return FormattedText.validateURL(urlToValidate); // KNL-1105
        if (StringUtils.isBlank(urlToValidate)) return false;

		if ( ABOUT_BLANK.equals(urlToValidate) ) return true;

        // Check if the url is "Escapable" - run through the URL-URI-URL gauntlet
        String escapedURL = sanitizeHrefURL(urlToValidate);
        if ( escapedURL == null ) return false;

        // For a protocol-relative URL, we validate with protocol attached 
        // RFC 1808 Section 4
        if ((urlToValidate.startsWith("//")) && (urlToValidate.indexOf("://") == -1))
        {
            urlToValidate = PROTOCOL_PREFIX + urlToValidate;
        }

        // For a site-relative URL, we validate with host name and protocol attached 
        // SAK-13787 SAK-23752
        if ((urlToValidate.startsWith("/")) && (urlToValidate.indexOf("://") == -1))
        {
            urlToValidate = HOST_PREFIX + urlToValidate;
        }

        // Validate the url
        return formattedText.validateURL(urlToValidate);
    }

    public String sanitizeHrefURL(String urlToEscape) {
         return formattedText.sanitizeHrefURL(urlToEscape);
    }
}
