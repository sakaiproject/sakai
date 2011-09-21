/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

import java.lang.Integer;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletContext;
import javax.portlet.PortletConfig;
import javax.portlet.WindowState;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.ReadOnlyException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.portlet.util.VelocityHelper;
import org.sakaiproject.portlet.util.JSPHelper;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.ToolSession;

// Velocity
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.app.VelocityEngine;

/**
 * a simple SakaiIFrame Portlet
 */
public class SakaiIFrame extends GenericPortlet {

    private static final Log M_log = LogFactory.getLog(SakaiIFrame.class);

    // This is old-style internationalization (i.e. not dynamic based
    // on user preference) to do that would make this depend on
    // Sakai Unique APIs. :(
    // private static ResourceBundle rb =  ResourceBundle.getBundle("iframe");
    protected static ResourceLoader rb = new ResourceLoader("iframe");

    protected final FormattedText validator = new FormattedText();

    private final VelocityHelper vHelper = new VelocityHelper();

    VelocityEngine vengine = null;

    private PortletContext pContext;

    // TODO: Perhaps these constancts should come from portlet.xml

	/** The source URL, in state, config and context. */
	protected final static String SOURCE = "source";

	/** The value in state and context for the source URL to actually used, as computed from special and URL. */
	protected final static String URL = "url";

	/** The height, in state, config and context. */
	protected final static String HEIGHT = "height";

	/** The custom height from user input * */
	protected final static String CUSTOM_HEIGHT = "customNumberField";

	protected final String POPUP = "sakai:popup";
	protected final String MAXIMIZE = "sakai:maximize";

	protected final static String TITLE = "title";

	private static final String FORM_PAGE_TITLE = "title-of-page";
	
	private static final String FORM_TOOL_TITLE = "title-of-tool";

	private static final int MAX_TITLE_LENGTH = 99;

    private static String ALERT_MESSAGE = "sakai:alert-message";

    // If the property is final, the property wins.  If it is not final,
    // the portlet preferences take precedence.
    public String getTitleString(RenderRequest request)
    {
	Placement placement = ToolManager.getCurrentPlacement();
	return placement.getTitle();
    }

    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        pContext = config.getPortletContext();
	try {
		vengine = vHelper.makeEngine(pContext);
	}
	catch(Exception e)
	{
		throw new PortletException("Cannot initialize Velocity ", e);
	}
	M_log.info("iFrame Portlet vengine="+vengine+" rb="+rb);
	
    }

    private void addAlert(ActionRequest request,String message) {
        PortletSession pSession = request.getPortletSession(true);
	pSession.setAttribute(ALERT_MESSAGE, message);
    }

    private void sendAlert(RenderRequest request, Context context) {
        PortletSession pSession = request.getPortletSession(true);
	String str = (String) pSession.getAttribute(ALERT_MESSAGE);
        pSession.removeAttribute(ALERT_MESSAGE);
        if ( str != null && str.length() > 0 ) context.put("alertMessage", validator.escapeHtml(str, false));
    }

    // Render the portlet - this is not supposed to change the state of the portlet
    // Render may be called many times so if it changes the state - that is tacky
    // Render will be called when someone presses "refresh" or when another portlet
    // onthe same page is handed an Action.
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setContentType("text/html");

	// System.out.println("==== doView called ====");

        PrintWriter out = response.getWriter();
	Placement placement = ToolManager.getCurrentPlacement();
	response.setTitle(placement.getTitle());
        String source = placement.getPlacementConfig().getProperty(SOURCE);
	if ( source == null ) source = "";
        String height = placement.getPlacementConfig().getProperty(HEIGHT);
	if ( height == null ) height = "1200px";
	boolean popup = "true".equals(placement.getPlacementConfig().getProperty(POPUP));
	boolean maximize = "true".equals(placement.getPlacementConfig().getProperty(MAXIMIZE));

        if ( source != null && source.trim().length() > 0 ) {
            Context context = new VelocityContext();
            context.put("tlang", rb);
            context.put("validator", validator);
	    context.put("source",source);
	    context.put("height",height);
            sendAlert(request,context);
            context.put("popup", Boolean.valueOf(popup));
            context.put("maximize", Boolean.valueOf(maximize));

            vHelper.doTemplate(vengine, "/vm/main.vm", context, out);
        } else {
            out.println("Not yet configured");
        }

	// System.out.println("==== doView complete ====");
    }

    public void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        // System.out.println("==== doEdit called ====");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
	String title = getTitleString(request);
	if ( title != null ) response.setTitle(title);

        Context context = new VelocityContext();
        context.put("tlang", rb);
        context.put("validator", validator);
        sendAlert(request,context);

        PortletURL url = response.createActionURL();
	context.put("actionUrl", url.toString());
        context.put("doCancel", "sakai.cancel");
        context.put("doUpdate", "sakai.update");

	Placement placement = ToolManager.getCurrentPlacement();
	context.put("title", validator.escapeHtml(placement.getTitle(), false));
        String source = placement.getPlacementConfig().getProperty(SOURCE);
	if ( source == null ) source = "";
	context.put("source",source);
        String height = placement.getPlacementConfig().getProperty(HEIGHT);
	context.put("height",height);

	ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
	if ( toolConfig != null )
	{
		try
		{
			Site site = SiteService.getSite(toolConfig.getSiteId());
			String siteId = site.getId();
			SitePage page = site.getPage(toolConfig.getPageId());

			// if this is the only tool on that page, update the page's title also
			if ((page.getTools() != null) && (page.getTools().size() == 1))
			{
				context.put("showPopup", Boolean.TRUE);
				boolean popup = "true".equals(placement.getPlacementConfig().getProperty(POPUP));
				context.put("popup", Boolean.valueOf(popup));

				boolean maximize = "true".equals(placement.getPlacementConfig().getProperty(MAXIMIZE));
				context.put("maximize", Boolean.valueOf(maximize));

				context.put("pageTitleEditable", Boolean.TRUE);
				context.put("page_title",  validator.escapeHtml(page.getTitle(), false));
			}
		}
		catch (Throwable e)
		{
		}
	}

        vHelper.doTemplate(vengine, "/vm/edit.vm", context, out);

        // System.out.println("==== doEdit done ====");
    }

    public void doHelp(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        // System.out.println("==== doHelp called ====");
        // sendToJSP(request, response, "/help.jsp");
        JSPHelper.sendToJSP(pContext, request, response, "/help.jsp");
        // System.out.println("==== doHelp done ====");
    }

    // Process action is called for action URLs / form posts, etc
    // Process action is called once for each click - doView may be called many times
    // Hence an obsession in process action with putting things in session to 
    // Send to the render process.
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

	// System.out.println("==== processAction called ====");

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
		// System.out.println("Unknown action");
		response.setPortletMode(PortletMode.VIEW);
	}

	// System.out.println("==== End of ProcessAction  ====");
    }

    public void processActionEdit(ActionRequest request, ActionResponse response)
            throws PortletException, IOException 
    {
        // TODO: Check Role
  
	// Stay in EDIT mode unless we are successful
	response.setPortletMode(PortletMode.EDIT);

	Placement placement = ToolManager.getCurrentPlacement();
	// get the site toolConfiguration, if this is part of a site.
	ToolConfiguration toolConfig = SiteService.findTool(placement.getId());

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
	
	try
	{
		Site site = SiteService.getSite(toolConfig.getSiteId());
		SitePage page = site.getPage(toolConfig.getPageId());
		page.setTitleCustom(true);
		
		// for web content tool, if it is a site page tool, and the only tool on the page, update the page title / popup.
		if (toolConfig != null)
		{
			// if this is the only tool on that page, update the page's title also
			if ((page.getTools() != null) && (page.getTools().size() == 1))
			{
				// String newPageTitle = data.getParameters().getString(FORM_PAGE_TITLE);
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
		M_log.warn("doConfigure_update: " + ignore);
	}

	// popup and maximize
	String spop = request.getParameter("popup");
	if ( ! "true".equals(spop) ) spop = "false";
	placement.getPlacementConfig().setProperty(POPUP, spop);
	String smax = request.getParameter("maximize");
	if ( ! "true".equals(smax) ) smax = "false";
	placement.getPlacementConfig().setProperty(MAXIMIZE, smax);
	String source = request.getParameter("source");
	if ( source == null ) source = "";
	placement.getPlacementConfig().setProperty(SOURCE, source);

	placement.save();

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
}
