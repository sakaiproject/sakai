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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.feedback.util.Constants;
import org.sakaiproject.feedback.util.SakaiProxy;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
public class FeedbackTool extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    private SakaiProxy sakaiProxy = null;

    private SecurityService securityService = null;

    private SiteService siteService = null;

    private final String[] DYNAMIC_PROPERTIES = { "help_tooltip",  "overview", "technical_setup_instruction", "report_technical_tooltip", "short_technical_description",
            "suggest_feature_tooltip", "feature_description", "technical_instruction", "ask_instruction",  "error", "help_home", "ask_setup_instruction", "feature_suggestion_setup_instruction"};

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

        String technicalToAddress = sakaiProxy.getConfigString(Constants.PROP_TECHNICAL_ADDRESS, null);
        setStringAttribute(request, "technicalToAddress", technicalToAddress);
        Boolean technicalPanelAsLink = sakaiProxy.getConfigBoolean(Constants.TECHNICAL_PANEL_AS_LINK, false);
        request.setAttribute("technicalPanelAsLink", technicalPanelAsLink);

        String helpToAddress = sakaiProxy.getConfigString(Constants.PROP_HELP_ADDRESS, null);
        setStringAttribute(request, "helpToAddress", helpToAddress);
        Boolean helpPanelAsLink = sakaiProxy.getConfigBoolean(Constants.HELP_PANEL_AS_LINK, false);
        request.setAttribute("helpPanelAsLink", helpPanelAsLink);

        String suggestionsToAddress = sakaiProxy.getConfigString(Constants.PROP_SUGGESTIONS_ADDRESS, null);
        setStringAttribute(request, "suggestionsToAddress", suggestionsToAddress);
        Boolean suggestionsPanelAsLink = sakaiProxy.getConfigBoolean(Constants.SUGGESTIONS_PANEL_AS_LINK, true);
        request.setAttribute("suggestionsPanelAsLink", suggestionsPanelAsLink);

        String supplementalAToAddress = sakaiProxy.getConfigString(Constants.PROP_SUPPLEMENTAL_A_ADDRESS, null);
        setStringAttribute(request, "supplementalAToAddress", supplementalAToAddress);
        Boolean supplementalAPanelAsLink = sakaiProxy.getConfigBoolean(Constants.SUPPLEMENTAL_A_PANEL_AS_LINK, true);
        request.setAttribute("supplementalAPanelAsLink", supplementalAPanelAsLink);

        String supplementalBToAddress = sakaiProxy.getConfigString(Constants.PROP_SUPPLEMENTAL_B_ADDRESS, null);
        setStringAttribute(request, "supplementalBToAddress", supplementalBToAddress);
        Boolean supplementalBPanelAsLink = sakaiProxy.getConfigBoolean(Constants.SUPPLEMENTAL_B_PANEL_AS_LINK, true);
        request.setAttribute("supplementalBPanelAsLink", supplementalBPanelAsLink);
        
        request.setAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
        setStringAttribute(request, "userId", (userId == null) ? "" : userId);
        setStringAttribute(request, "siteId", (siteId != null)?siteId.replaceAll("/", FORWARD_SLASH):null);
        request.setAttribute("siteExists", siteExists);
        setStringAttribute(request, "helpPagesUrl", sakaiProxy.getConfigString(Constants.PROP_HELPPAGES_URL, "/portal/help/main"));

        String featureSuggestionUrl = sakaiProxy.getConfigString(Constants.PROP_SUGGESTIONS_URL, "");
        setStringAttribute(request, "featureSuggestionUrl", featureSuggestionUrl);
        String helpdeskUrl = sakaiProxy.getConfigString(Constants.PROP_HELPDESK_URL, "");
        setStringAttribute(request, "helpdeskUrl", helpdeskUrl);
        String technicalUrl = sakaiProxy.getConfigString(Constants.PROP_TECHNICAL_URL, "");
        setStringAttribute(request, "technicalUrl", technicalUrl);
        String supplementalAUrl = sakaiProxy.getConfigString(Constants.PROP_SUPPLEMENTAL_A_URL, "");
        setStringAttribute(request, "supplementalAUrl", supplementalAUrl);
        String supplementalBUrl = sakaiProxy.getConfigString(Constants.PROP_SUPPLEMENTAL_B_URL, "");
        setStringAttribute(request, "supplementalBUrl", supplementalBUrl);

        Boolean enableHelp = true;
        if (helpPanelAsLink) {
            if (helpdeskUrl.isEmpty()) {
                enableHelp = false;
            }
        } else {
            enableHelp = helpToAddress != null;
        }
        request.setAttribute("enableHelp", enableHelp);

        Boolean enableTechnical = true;
        if (technicalPanelAsLink) {
            if (technicalUrl.isEmpty()) {
                enableTechnical = false;
            }
        } else {
            enableTechnical = technicalToAddress != null;
        }
        request.setAttribute("enableTechnical", enableTechnical);

        Boolean enableSuggestions = true;
        if (suggestionsPanelAsLink) {
            if (featureSuggestionUrl.isEmpty()) {
                enableSuggestions = false;
            }
        } else {
            enableSuggestions = suggestionsToAddress != null;
        }
        request.setAttribute("enableSuggestions", enableSuggestions);

        Boolean enableSupplementalA = true;
        if (supplementalAPanelAsLink) {
            if (supplementalAUrl.isEmpty()) {
                enableSupplementalA = false;
            }
        } else {
            enableSupplementalA = supplementalAToAddress != null;
        }
        request.setAttribute("enableSupplementalA", enableSupplementalA);

        Boolean enableSupplementalB = true;
        if (supplementalBPanelAsLink) {
            if (supplementalBUrl.isEmpty()) {
                enableSupplementalB = false;
            }
        } else {
            enableSupplementalB = supplementalBToAddress != null;
        }
        request.setAttribute("enableSupplementalB", enableSupplementalB);

        setStringAttribute(request, "helpPagesTarget", sakaiProxy.getConfigString(Constants.PROP_HELPPAGES_TARGET, "_blank"));
        setStringAttribute(request, "supplementaryInfo", sakaiProxy.getConfigString(Constants.PROP_SUPPLEMENTARY_INFO, ""));
        request.setAttribute("maxAttachmentsMB", sakaiProxy.getAttachmentLimit());
        request.setAttribute("showContentPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_CONTENT_PANEL, true));
        request.setAttribute("showHelpPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_HELP_PANEL, true));
        request.setAttribute("showTechnicalPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_TECHNICAL_PANEL, true));
        request.setAttribute("showSuggestionsPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_SUGGESTIONS_PANEL, true));
        request.setAttribute("showSupplementalAPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_SUPPLEMENTAL_A_PANEL, false));
        request.setAttribute("showSupplementalBPanel", sakaiProxy.getConfigBoolean(Constants.SHOW_SUPPLEMENTAL_B_PANEL, false));

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
        if (siteId.equals("!error")) {
            // When inside the !error site we get the URL of the original site being accessed
            return request.getContextPath();
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
            if(property.equals("ask_instruction")) {
                String name = sakaiProxy.getConfigString("feedback.helpdeskName", serviceName);
                bundleMap.put(property, MessageFormat.format(rb.getString(property), new String[]{name}));
                continue;
            }
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
