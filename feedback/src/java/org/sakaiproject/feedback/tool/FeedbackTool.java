/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.feedback.tool;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.feedback.util.Constants;
import org.sakaiproject.feedback.util.SakaiProxy;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
public class FeedbackTool extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    private static final Log logger = LogFactory.getLog(FeedbackTool.class);

    private SakaiProxy sakaiProxy = null;

    private SecurityService securityService = null;

    private SiteService siteService = null;

    private final String[] DYNAMIC_PROPERTIES = { "help_tooltip",  "overview", "technical_setup_instruction", "report_technical_tooltip", "short_technical_description",
            "suggest_feature_tooltip", "feature_description", "technical_instruction",  "error", "help_home"};

    // In entitybroker you can't have slashes in IDs so we need to escape them.
    public static final String FORWARD_SLASH = "FORWARD_SLASH";

    private static ResourceLoader rb = new ResourceLoader("org.sakaiproject.feedback");

    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        try {
            ApplicationContext context
                = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            sakaiProxy = (SakaiProxy) context.getBean("org.sakaiproject.feedback.util.SakaiProxy");
            securityService = (SecurityService) context.getBean("org.sakaiproject.authz.api.SecurityService");
            siteService = (SiteService) context.getBean("org.sakaiproject.site.api.SiteService");

        } catch (Throwable t) {
            throw new ServletException("Failed to initialise FeedbackTool servlet.", t);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String userId = sakaiProxy.getCurrentUserId();

        String siteId = sakaiProxy.getCurrentSiteId();

        // Change the site ID if overridden.
        siteId = overrideSiteId(request, siteId);

        Site site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            // This is expected in some cases.
        }
        boolean siteExists = site != null;

        Map<String, String> siteUpdaters = new HashMap<String, String>();
        Map<String, String> emailRecipients = new LinkedHashMap<String, String>();

        String serviceName = sakaiProxy.getConfigString("ui.service", "Sakai");
        boolean hasViewPermission = false;
        if (siteExists){
            hasViewPermission = securityService.unlock("roster.viewallmembers", site.getReference());
            if(hasViewPermission) {
                siteUpdaters = sakaiProxy.getSiteUpdaters(siteId);
            }
            addRecipients(site, emailRecipients, siteUpdaters, serviceName);
        }
        else {
            String serviceContactName = rb.getFormattedMessage("technical_team_name", new String[]{serviceName});
            String serviceContactEmail = sakaiProxy.getConfigString(Constants.PROP_TECHNICAL_ADDRESS, null);
            emailRecipients.put(serviceContactEmail, serviceContactName);
        }

        if (userId != null) {
            setMapAttribute(request, "siteUpdaters", emailRecipients);
        } else {
            if (sakaiProxy.getConfigBoolean("user.recaptcha.enabled", false)) {
                String publicKey = sakaiProxy.getConfigString("user.recaptcha.public-key", "");
                setStringAttribute(request, "recaptchaPublicKey", publicKey);
            }
        }

        setMapAttribute(request, "i18n", getBundle(serviceName));
        setStringAttribute(request, "language", rb.getLocale().getLanguage());
        request.setAttribute("enableTechnical",
                (sakaiProxy.getConfigString(Constants.PROP_TECHNICAL_ADDRESS, null) == null)
                        ? false : true);

        request.setAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
        setStringAttribute(request, "userId", (userId == null) ? "" : userId);
        setStringAttribute(request, "siteId", (siteId != null)?siteId.replaceAll("/", FORWARD_SLASH):null);
        request.setAttribute("siteExists", siteExists);
        setStringAttribute(request, "featureSuggestionUrl", sakaiProxy.getConfigString("feedback.featureSuggestionUrl", ""));
        setStringAttribute(request, "helpPagesUrl", sakaiProxy.getConfigString("feedback.helpPagesUrl", "/portal/help/main"));
        setStringAttribute(request, "helpPagesTarget", sakaiProxy.getConfigString("feedback.helpPagesTarget", "_blank"));
        setStringAttribute(request, "supplementaryInfo", sakaiProxy.getConfigString("feedback.supplementaryInfo", ""));
        request.setAttribute("maxAttachmentsMB", sakaiProxy.getAttachmentLimit());
        setStringAttribute(request, "technicalToAddress", sakaiProxy.getConfigString(Constants.PROP_TECHNICAL_ADDRESS, null));
        request.setAttribute("showContentPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_CONTENT_PANEL, true));
        request.setAttribute("showHelpPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_HELP_PANEL, true));
        request.setAttribute("showTechnicalPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_TECHNICAL_PANEL, true));
        request.setAttribute("showSuggestionsPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_SUGGESTIONS_PANEL, true));

        String contactName = null;
        String siteEmail = null;
        if (siteExists){
            siteEmail = site.getProperties().getProperty(Site.PROP_SITE_CONTACT_EMAIL);
        }

        if (siteEmail!=null && !siteEmail.isEmpty() && siteExists){
            contactName = site.getProperties().getProperty(Site.PROP_SITE_CONTACT_NAME);
        }
        else if (!hasViewPermission){
            String serviceContactName = rb.getFormattedMessage("technical_team_name", new String[]{serviceName});
            String serviceContactEmail = sakaiProxy.getConfigString(Constants.PROP_TECHNICAL_ADDRESS, null);
            contactName = String.format("%s <%s>" ,serviceContactName, serviceContactEmail);
        }
        setStringAttribute(request, "contactName", contactName);

        response.setContentType("text/html");
        request.getRequestDispatcher("/WEB-INF/bootstrap.jsp").include(request, response);
    }

    /**
     * This allows the site ID be overridden from the session or request.
     * @param request The HttpServletRequest
     * @param siteId The current Site ID.
     * @return The site ID.
     */
    private String overrideSiteId(HttpServletRequest request, String siteId) {
        // This is set by the portal and is the site ID of the original site that was being accessed
        // when using a URL like /portal/site/{siteId}/page/contact_us this will contain the site ID.
        String contactUsSiteId = (String) request.getSession().getAttribute("contact.us.origin.site");
        if (contactUsSiteId!=null) {
            siteId = contactUsSiteId;
        }
        // When inside the !error site the original URL is put as a request parameter and then passed through.
        if (siteId.equals("!error")) { // if site is unavailable then retrieve siteId
            siteId = request.getParameter("siteId");
        }
        return siteId;
    }

    private void setStringAttribute(HttpServletRequest request, String key, String value){
		request.setAttribute(key, StringEscapeUtils.escapeJavaScript(value));
	}

	private void setMapAttribute(HttpServletRequest request, String key, Map<String, String> map){
		for (String s : map.keySet()) {
			map.put(s, StringEscapeUtils.escapeJavaScript( map.get(s)));
		}
		request.setAttribute(key, map);
	}

    private void addRecipients(Site site, Map<String, String> emailRecipients, Map<String, String> siteUpdaters, String serviceName) {
        String siteContact = site.getProperties().getProperty(Site.PROP_SITE_CONTACT_NAME);
        String siteEmail = site.getProperties().getProperty(Site.PROP_SITE_CONTACT_EMAIL);
        if (siteEmail!=null && !siteEmail.isEmpty()){
            emailRecipients.put(siteEmail, siteContact + " (site contact)");
        }
        else if (siteUpdaters.isEmpty()){
            String serviceContactName = rb.getFormattedMessage("technical_team_name", new String[]{serviceName});
            String serviceContactEmail = sakaiProxy.getConfigString(Constants.PROP_TECHNICAL_ADDRESS, null);
            emailRecipients.put(serviceContactEmail, serviceContactName);
        }
        emailRecipients.putAll(siteUpdaters);
    }

    private Map<String, String> getBundle(String serviceName) {
        Map<String, String> bundleMap = new HashMap<String, String>();
        for (Object key : rb.keySet()) {
            bundleMap.put((String) key, rb.getString((String) key));
        }
        formatProperties(rb, bundleMap, serviceName);
        return bundleMap;
    }

    private void formatProperties(ResourceLoader rb, Map<String, String> bundleMap, String serviceName) {

        for (String property : DYNAMIC_PROPERTIES) {
            bundleMap.put(property, MessageFormat.format(rb.getString(property), new String[]{serviceName}));
        }

        if (serviceName!=null && !serviceName.isEmpty()){
            bundleMap.put("technical_link", MessageFormat.format(rb.getString("technical_link"), new String[]{serviceName}));
        }
        else {
            bundleMap.put("technical_link", rb.getString("ask_link"));
        }
    }
}
