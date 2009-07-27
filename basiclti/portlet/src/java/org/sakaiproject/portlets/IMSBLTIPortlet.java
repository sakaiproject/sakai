package org.sakaiproject.portlets;

import org.imsglobal.basiclti.BasicLTIUtil;

import java.lang.Integer;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.PrintStream;

import java.net.URL;
import java.net.URLEncoder;

import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletConfig;
import javax.portlet.WindowState;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.ReadOnlyException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.portlet.util.SakaiPortletUtil;
import org.sakaiproject.portlet.util.PortletHelper;

// Sakai APIs
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;


// For Rutgers Security
import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * a simple IMSBLTIPortlet Portlet
 */
public class IMSBLTIPortlet extends GenericPortlet {

    private static ResourceLoader rb = new ResourceLoader("basiclti");

    private PortletContext pContext;

    private ArrayList<String> fieldList = new ArrayList<String>();

    /** Our log (commons). */
    private static Log M_log = LogFactory.getLog(IMSBLTIPortlet.class);

    public static String EVENT_BASICLTI_CONFIG = "basiclti.config";

    /** To turn on really verbose debugging */
    private static boolean verbosePrint = false;

    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        pContext = config.getPortletContext();

        // Populate the list of fields
        fieldList.add("launch");
        fieldList.add("secret");
        fieldList.add("key");
        fieldList.add("xml");
        fieldList.add("frameheight");
        fieldList.add("debug");
        fieldList.add("pagetitle");
        fieldList.add("tooltitle");
    }

    // Simple Debug Print Mechanism
    public void dPrint(String str)
    {
	if ( verbosePrint ) System.out.println(str);
	M_log.trace(str);
    }

    // If the property is final, the property wins.  If it is not final,
    // the portlet preferences take precedence.
    public String getTitleString(RenderRequest request)
    {
	return getCorrectProperty(request, "tooltitle", null);
    }

    // Render the portlet - this is not supposed to change the state of the portlet
    // Render may be called many times so if it changes the state - that is tacky
    // Render will be called when someone presses "refresh" or when another portlet
    // on the same page is handed an Action.
    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

	dPrint("==== doView called ====");

        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        PortletSession pSession = request.getPortletSession(true);
        PortletPreferences prefs = request.getPreferences();

	String title = getTitleString(request);
	if ( title != null ) response.setTitle(title);

	String context = getContext();
	Placement placement = ToolManager.getCurrentPlacement();

	// Get the optional values from session
	String frameHeight = (String) pSession.getAttribute("sakai.frameheight");
	dPrint("fh="+frameHeight);
        String iframeUrl = "/access/basiclti/site/"+context+"/"+placement.getId();

	if ( iframeUrl != null ) {
            StringBuffer text = new StringBuffer();

	    Session session = SessionManager.getCurrentSession();
	    session.setAttribute("sakai:maximized-url",iframeUrl);
            dPrint("Setting sakai:maximized-url="+iframeUrl);

	    text.append("<iframe ");
	    text.append("title=\"Site Info\" ");
	    if ( frameHeight == null ) frameHeight = "1200";
   	    text.append("height=\""+frameHeight+"\" \n");
	    text.append("width=\"100%\" frameborder=\"0\" marginwidth=\"0\"\n");
	    text.append("marginheight=\"0\" scrolling=\"auto\"\n");
	    text.append("src=\""+iframeUrl+"\">\n");
	    text.append(rb.getString("noiframes"));
	    text.append("<br>");
	    text.append("<a href=\""+iframeUrl+"\">Press here for content</a>\n");
	    text.append("</iframe>");
            out.println(text);
	    dPrint("==== doView complete ====");
	    return;
	} else {
	    out.println(rb.getString("not.configured"));
	}

	dPrint("==== doView complete ====");
    }

    // Prepare the edit screen with data
    public void prepareEdit(RenderRequest request)
    {
        // Hand up the tool properties
        Placement placement = ToolManager.getCurrentPlacement();
        Properties config = placement.getConfig();
        dPrint("placement="+ placement.getId());
        dPrint("placement.toolId="+ placement.getToolId());
        dPrint("properties="+ config);
	for (String element : fieldList) {
		String propertyName = placement.getToolId() + "." + element;
		String propValue = ServerConfigurationService.getString(propertyName,null);
		if ( propValue != null && propValue.trim().length() > 0 ) {
			dPrint("Forcing Final = "+propertyName);
			config.setProperty("final."+element,"true");
		}
	}
	request.setAttribute("imsti.properties", config);

        // Hand up the old values
       	Properties oldValues = new Properties();
	addProperty(oldValues, request, "launch", "http://simplelti.appspot.com/launch");
	for (String element : fieldList) {
		if ( "launch".equals(element) ) continue;
		addProperty(oldValues, request, element, null);
	}

	request.setAttribute("imsti.oldvalues", oldValues);
    }

    public void addProperty(Properties values, RenderRequest request,
		String propName, String defaultValue)
    {
	String propValue = getCorrectProperty(request, propName, defaultValue);
	if ( propValue != null ) {
		values.setProperty("imsti."+propName,propValue);
	}
    }

    // Get Property - Precedence is frozen server configuration, sakai tool properties, 
    //     portlet preferences, sakai tool properties, and then default
    public String getCorrectProperty(PortletRequest request, String propName, String defaultValue)
    {
        Placement placement = ToolManager.getCurrentPlacement();
	String propertyName = placement.getToolId() + "." + propName;
	String propValue = ServerConfigurationService.getString(propertyName,null);
	if ( propValue != null && propValue.trim().length() > 0 ) {
		// System.out.println("Sakai.home "+propName+"="+propValue);
		return propValue;
	}

        Properties config = placement.getConfig();
	propValue = getSakaiProperty(config, "imsti."+propName);
        if ( propValue != null && "true".equals(config.getProperty("final."+propName)) )
        {
                // System.out.println("Frozen "+propName+" ="+propValue);
		return propValue;
        }

        PortletPreferences prefs = request.getPreferences();
        propValue = prefs.getValue("imsti."+propName, null);
        if ( propValue != null ) {
                // System.out.println("Portlet "+propName+" ="+propValue);
		return propValue;
        }

        propValue = getSakaiProperty(config, "imsti."+propName);
        if ( propValue != null ) {
                // System.out.println("Tool "+propName+" ="+propValue);
		return propValue;
        }

        if ( defaultValue != null ) {
                // System.out.println("Default "+propName+" ="+defaultValue);
		return propValue;
        }
        // System.out.println("Fell through "+propName);
	return null;
    }

    // isPropertyFinal() - if it comes from the Server configuration or
    //     the final.propName is set to true
    public boolean isPropertyFinal(String propName)
    {
        Placement placement = ToolManager.getCurrentPlacement();
	String propertyName = placement.getToolId() + "." + propName;
	String propValue = ServerConfigurationService.getString(propertyName,null);
	if ( propValue != null && propValue.trim().length() > 0 ) {
		return true;
	}

        Properties config = placement.getConfig();
	propValue = getSakaiProperty(config, "imsti."+propName);
        if ( propValue != null && "true".equals(config.getProperty("final."+propName)) )
        {
		return true;
        }
	return false;
    }

    public void doEdit(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        response.setContentType("text/html");
        dPrint("==== doEdit called ====");

        PortletSession pSession = request.getPortletSession(true);
        PortletPreferences prefs = request.getPreferences();

	String title = getTitleString(request);
	if ( title != null ) response.setTitle(title);

	// Debug
	String inputData = (String) pSession.getAttribute("sakai.descriptor");
	if ( inputData != null ) dPrint("descriptor.length()="+inputData.length());
	String url = (String) pSession.getAttribute("sakai.url");
	dPrint("sakai.url="+url);


	String view = (String) pSession.getAttribute("sakai.view");
	dPrint("sakai.view="+view);
	if ( "edit.reset".equals(view) ) {
        	sendToJSP(request, response, "/editreset.jsp");
	} else {
    		prepareEdit(request);
        	sendToJSP(request, response, "/edit.jsp");
	}

	dPrint("==== doEdit called ====");
    }

    public void doHelp(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        dPrint("==== doHelp called ====");
        PortletPreferences prefs = request.getPreferences();

	String title = getTitleString(request);
	if ( title != null ) response.setTitle(title);
        sendToJSP(request, response, "/help.jsp");
        dPrint("==== doHelp done  ====");
    }

    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

	dPrint("==== processAction called ====");

	String action = request.getParameter("sakai.action");
	dPrint("sakai.action = "+action);

        PortletSession pSession = request.getPortletSession(true);

	clearErrorMessage(request);

        String view = (String) pSession.getAttribute("sakai.view");
        dPrint("sakai.view="+view);

	if ( action == null ) {
		// Do nothing
       	} else if ( action.equals("main") ) {
		response.setPortletMode(PortletMode.VIEW);
       	} else if ( action.equals("edit") ) {
		pSession.setAttribute("sakai.view", "edit");
	} else if ( action.equals("edit.reset") ) {
                pSession.setAttribute("sakai.view","edit.reset");
	}else if (action.equals("edit.setup")){
		pSession.setAttribute("sakai.view","edit.setup");
	} else if ( action.equals("edit.clear") ) {
		clearSession(request);
		response.setPortletMode(PortletMode.VIEW);
		pSession.setAttribute("sakai.view", "main");
	} else if ( action.equals("edit.do.reset") ) {
                processActionReset(action,request, response);
	} else if ( action.equals("edit.save") ) {
                processActionSave(action,request, response);
	}
	dPrint("==== End of ProcessAction ====");
    }

    private void clearSession(PortletRequest request)
    {
	PortletSession pSession = request.getPortletSession(true);

	pSession.removeAttribute("sakai.url");
	pSession.removeAttribute("sakai.widget");
	pSession.removeAttribute("sakai.descriptor");
	pSession.removeAttribute("sakai.attemptdescriptor");

	for (String element : fieldList) {
		pSession.removeAttribute("sakai."+element);
	}
    }

    public void processActionReset(String action,ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

	// TODO: Check Role
	dPrint("Removing preferences....");
	clearSession(request);
        PortletSession pSession = request.getPortletSession(true);
        PortletPreferences prefs = request.getPreferences();
        try {
		prefs.reset("sakai.descriptor");
                for (String element : fieldList) {
                        prefs.reset("imsti."+element);
                        prefs.reset("sakai:imsti."+element);
                }
		dPrint("Preference removed");
        } catch (ReadOnlyException e) {
		setErrorMessage(request, rb.getString("error.modify.prefs")) ;
        }
        prefs.store();

	// Go back to the main edit page
	pSession.setAttribute("sakai.view", "edit");
    }

    public void processActionEdit(String action,ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

    }

    public Properties getSakaiProperties()
    {
	Placement placement = ToolManager.getCurrentPlacement();
	return placement.getConfig();
    }

    // EMpty or all whitespace properties are null
    public String getSakaiProperty(Properties config, String key)
    {
        String propValue = config.getProperty(key);
        if ( propValue != null && propValue.trim().length() < 1 ) propValue = null;
        return propValue;
    }

    // Insure that if we have frozen properties - we never accept form data
    public String getFormParameter(ActionRequest request, Properties sakaiProperties, String propName)
    {
	String propValue = getCorrectProperty(request, propName, null);
	if ( propValue == null || ! isPropertyFinal(propName) ) 
	{
		propValue = request.getParameter("imsti."+propName);
	}
	dPrint("Form/Final imsti."+propName+"="+propValue);
	if (propValue != null ) propValue = propValue.trim();
	return propValue;
    }

    public void processActionSave(String action,ActionRequest request, ActionResponse response)
            throws PortletException, IOException {

        PortletSession pSession = request.getPortletSession(true);
	Properties sakaiProperties = getSakaiProperties();

	String imsType  = getFormParameter(request,sakaiProperties,"type");

	String imsTIUrl  = getFormParameter(request,sakaiProperties,"launch");
        if ( imsTIUrl != null && imsTIUrl.trim().length() < 1 ) imsTIUrl = null;
	String imsTIXml  = getFormParameter(request,sakaiProperties,"xml");
        if ( imsTIXml != null && imsTIXml.trim().length() < 1 ) imsTIXml = null;

        // imsType will be null if launch or xml is coming from final properties
        if ( imsType != null ) {
            if ( imsType.equalsIgnoreCase("XML") ) {
                if ( imsTIXml != null ) imsTIUrl = null;
            } else {
                if ( imsTIUrl != null ) imsTIXml = null;
            }
	}

        String launch_url = imsTIUrl;
        if ( imsTIXml != null ) {
		launch_url = BasicLTIUtil.validateDescriptor(imsTIXml);
		if ( launch_url == null ) {
			setErrorMessage(request, rb.getString("error.xml.input"));
			return;
		}
	} else if ( imsTIUrl == null ) {
		setErrorMessage(request, rb.getString("error.no.input") );
		return;
        }

	// Passed the sanity checks - time to save it all!

	String context = getContext();
	Placement placement = ToolManager.getCurrentPlacement();

	// Update the Page Title (button text)
	String imsTIPageTitle  = getFormParameter(request,sakaiProperties,"pagetitle");
	if ( imsTIPageTitle != null && imsTIPageTitle.trim().length() > 0 ) {
       		try {
       			ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
       			Site site = SiteService.getSite(context);
       			SitePage page = site.getPage(toolConfig.getPageId());
			page.setTitle(imsTIPageTitle);
			SiteService.save(site);
        	} catch (Exception e) {
               		setErrorMessage(request, rb.getString("error.page.title"));
		}
	}

        // Store preferences
        PortletPreferences prefs = request.getPreferences();
        boolean changed = false;
        for (String element : fieldList) {
                String formParm  = getFormParameter(request,sakaiProperties,element);
                try {
                        prefs.setValue("sakai:imsti."+element, formParm);
                        changed = true;
                } catch (ReadOnlyException e) {
                        setErrorMessage(request, rb.getString("error.modify.prefs") );
                }
        }

        // Clear out the other setting
        if ( imsType != null ) {
            if ( imsType.equalsIgnoreCase("XML") ) {
               if ( imsTIXml != null ) {
                   prefs.reset("sakai:imsti.launch");
                   changed = true;
               }
            } else {
               if ( imsTIUrl != null ) {
                   prefs.reset("sakai:imsti.xml");
                   changed = true;
               }
            }
        }

        // track event and store
        if ( changed ) {
            // 2.6 Event Tracking
            Event event = EventTrackingService.newEvent(EVENT_BASICLTI_CONFIG, launch_url, context, true, NotificationService.NOTI_OPTIONAL);
            // 2.5 Event Tracking
            // Event event = EventTrackingService.newEvent(EVENT_BASICLTI_CONFIG, launch_url, true);
            EventTrackingService.post(event);
            prefs.store();
	}

	pSession.setAttribute("sakai.view", "main");
	response.setPortletMode(PortletMode.VIEW);
    }

	private void propCopy(Properties newProp,Map<String,String> newMap, String key)
	{
		if ( key == null ) return;
		String value = newProp.getProperty(key);
		if ( value == null ) return;
		newMap.put(key, value);
	}

        /**
         * Get the current site page our current tool is placed on.
         * 
         * @return The site page id on which our tool is placed.
         */
        protected String getCurrentSitePageId()
        {
                ToolSession ts = SessionManager.getCurrentToolSession();

		Placement placement = ToolManager.getCurrentPlacement();
		ToolConfiguration tool = SiteService.findTool(placement.getId());
		if (tool != null)
		{
			return tool.getPageId();
		}
                return null;
        }

    // TODO: Local cleverness ???
    private void sendToJSP(RenderRequest request, RenderResponse response,
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

    // Error Message
    public void clearErrorMessage(PortletRequest request)
    {
	PortletHelper.clearErrorMessage(request);
    }

    public void setErrorMessage(PortletRequest request, String errorMsg)
    {
	PortletHelper.setErrorMessage(request,errorMsg);
    }

    public void setErrorMessage(PortletRequest request, String errorMsg, Throwable t)
    {
	PortletHelper.setErrorMessage(request,errorMsg,t);
    }

    private String getContext()
    {
            String retval = ToolManager.getCurrentPlacement().getContext();
            return retval;
    }

}
