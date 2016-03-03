/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2009 The Sakai Foundation
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

package org.sakaiproject.blti;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.*;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.tsugi.basiclti.BasicLTIConstants;
import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.basiclti.BasicLTIProviderUtil;

import org.tsugi.casa.objects.Application;

import org.tsugi.contentitem.objects.ContentItemResponse;

import org.tsugi.jackson.JacksonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.lti.api.BLTIProcessor;
import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.SiteEmailPreferenceSetter;
import org.sakaiproject.lti.api.UserFinderOrCreator;
import org.sakaiproject.lti.api.UserLocaleSetter;
import org.sakaiproject.lti.api.UserPictureSetter;
import org.sakaiproject.lti.api.SiteMembershipUpdater;
import org.sakaiproject.lti.api.SiteMembershipsSynchroniser;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.sakaiproject.basiclti.util.SakaiCASAUtil;
import org.sakaiproject.basiclti.util.SakaiContentItemUtil;
import org.sakaiproject.basiclti.util.SakaiLTIProviderUtil;
import org.sakaiproject.basiclti.util.LegacyShaUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Notes:
 * 
 * This program is directly exposed as a URL to receive IMS Basic LTI launches
 * so it must be carefully reviewed and any changes must be looked at carefully.
 * Here are some issues:
 * 
 * - This will only function when it is enabled via sakai.properties
 * 
 * - This servlet makes use of security advisors - once an advisor has been
 * added, it must be removed - often in a finally. Also the code below only adds
 * the advisor for very short segments of code to allow for easier review.
 * 
 * Implemented using a SHA-1 hash of the effective context_id and then stores
 * the original context_id in a site.property "lti_context_id" which will be
 * useful for later reference. Since SHA-1 hashes to 40 chars, that would leave
 * us 59 chars (i.e. 58 + ":") to use for LTI key. This also means that the new
 * maximum supported size of an effective context_id is the maximum message size
 * of SHA-1: maximum length of (264 ? 1) bits.
 */

@SuppressWarnings("deprecation")
public class ProviderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Log M_log = LogFactory.getLog(ProviderServlet.class);
	private static ResourceLoader rb = new ResourceLoader("basiclti");
	private static final String BASICLTI_RESOURCE_LINK = "blti:resource_link_id";
    private static final String LTI_CONTEXT_ID = "lti_context_id";

    // All loaded from the component manager
    private SiteMembershipUpdater siteMembershipUpdater = null;
    private SiteMembershipsSynchroniser siteMembershipsSynchroniser  = null;
    private SiteEmailPreferenceSetter siteEmailPreferenceSetter = null;
    private UserFinderOrCreator userFinderOrCreator = null;
    private UserLocaleSetter userLocaleSetter = null;
    private UserPictureSetter userPictureSetter = null;
    private LTIService ltiService = null;

    private List<BLTIProcessor> bltiProcessors = new ArrayList();

    private enum ProcessingState {
        beforeValidation, afterValidation, afterUserCreation, afterLogin, afterSiteCreation,
        afterSiteMembership, beforeLaunch
    }


	/**
	 * Setup a security advisor.
	 */
	public void pushAdvisor() {
		// setup a security advisor
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Remove our security advisor.
	 */
	public void popAdvisor() {
		SecurityService.popAdvisor();
	}

	public void doError(HttpServletRequest request,HttpServletResponse response, String s, String message, Throwable e) throws java.io.IOException {
		if (e != null) {
			M_log.error(e.getLocalizedMessage(), e);
		}
		M_log.info(rb.getString(s) + ": " + message);
		String return_url = request.getParameter(BasicLTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
		if (return_url != null && return_url.length() > 1) {
			if (return_url.indexOf('?') > 1) {
				return_url += "&lti_msg=" + URLEncoder.encode(rb.getString(s), "UTF-8");
			} else {
				return_url += "?lti_msg=" + URLEncoder.encode(rb.getString(s), "UTF-8");
			}
			// Avoid Response Splitting
			return_url = return_url.replaceAll("[\r\n]","");
			response.sendRedirect(return_url);
			return;
		}
		PrintWriter out = response.getWriter();
		out.println(rb.getString(s));
	}

	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

        siteEmailPreferenceSetter = (SiteEmailPreferenceSetter) ComponentManager.getInstance().get("org.sakaiproject.lti.api.SiteEmailPreferenceSetter");
        if (siteEmailPreferenceSetter  == null) {
            throw new ServletException("Failed to set siteEmailPreferenceSetter.");
        }

        siteMembershipUpdater = (SiteMembershipUpdater) ComponentManager.getInstance().get("org.sakaiproject.lti.api.SiteMembershipUpdater");
        if (siteMembershipUpdater == null) {
            throw new ServletException("Failed to set siteMembershipUpdater.");
        }

        siteMembershipsSynchroniser = (SiteMembershipsSynchroniser) ComponentManager.getInstance().get("org.sakaiproject.lti.api.SiteMembershipsSynchroniser");
        if (siteMembershipsSynchroniser == null) {
            throw new ServletException("Failed to set siteMembershipsSynchroniser.");
        }

        userFinderOrCreator = (UserFinderOrCreator) ComponentManager.getInstance().get("org.sakaiproject.lti.api.UserFinderOrCreator");
        if (userFinderOrCreator  == null) {
            throw new ServletException("Failed to set userFinderOrCreator.");
        }

        userPictureSetter = (UserPictureSetter) ComponentManager.getInstance().get("org.sakaiproject.lti.api.UserPictureSetter");
        if (userPictureSetter == null) {
            throw new ServletException("Failed to set userPictureSettter.");
        }

        userLocaleSetter = (UserLocaleSetter) ComponentManager.getInstance().get("org.sakaiproject.lti.api.UserLocaleSetter");
        if (userLocaleSetter == null) {
            throw new ServletException("Failed to set userLocaleSettter.");
        }

        ltiService = (LTIService) ComponentManager.getInstance().get("org.sakaiproject.lti.api.LTIService");
        if (ltiService  == null) {
            throw new ServletException("Failed to set ltiService.");
        }

        ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());

        // load all instance of BLTIProcessor in component mgr by type detection
        Collection processors = ac.getParent().getBeansOfType(BLTIProcessor.class).values();
        bltiProcessors = new ArrayList(processors);
        // sort in using getOrder() method

        // sort them so the execution order is determined consistenly - by getOrder()
        Collections.sort(bltiProcessors, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((BLTIProcessor) (o1)).getOrder())
                        .compareTo(((BLTIProcessor) (o2)).getOrder());
            }
        });
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		doPost(request, response);
	}

	protected Map getPayloadAsMap(HttpServletRequest request) {
		Map payload = new HashMap();
		for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			payload.put(key, request.getParameter(key));
		}

		String requestURL = SakaiBLTIUtil.getOurServletPath(request);
		payload.put("oauth_message", OAuthServlet.getMessage(request, requestURL));
		payload.put("tool_id", 	request.getPathInfo());
		return payload;
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ipAddress = request.getRemoteAddr();

		if (M_log.isDebugEnabled()) {
			M_log.debug("Basic LTI Provider request from IP=" + ipAddress);
		}

		String enabled = ServerConfigurationService.getString(
				"basiclti.provider.enabled", null);
		if (enabled == null || !("true".equals(enabled))) {
			M_log.warn("Basic LTI Provider is Disabled IP=" + ipAddress);
			response.sendError(HttpServletResponse.SC_FORBIDDEN,
					"Basic LTI Provider is Disabled");
			return;
		}

		if ( "/casa.json".equals(request.getPathInfo()) ) {
			if ( ServerConfigurationService.getBoolean("casa.provider", true))  {
				handleCASAList(request, response);
				return;
			} else {
				M_log.warn("CASA Provider is Disabled IP=" + ipAddress);
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"CASA Provider is Disabled");
				return;
			}
		}

		if ( "/canvas-config.xml".equals(request.getPathInfo()) ) {
			if ( ServerConfigurationService.getBoolean("canvas.config.enabled", true))  {
				handleCanvasConfig(request, response);
				return;
			} else {
				M_log.warn("Canvas config is Disabled IP=" + ipAddress);
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"Canvas config is Disabled");
				return;
			}
		}

		// If this is a LTI request of any kind, make sure we don't have any
		// prior payload in the session.
		if ( BasicLTIUtil.isRequest(request) ) {
			Session sess = SessionManager.getCurrentSession();
			sess.removeAttribute("payload");
		}

		// Check if we support ContentItem.
		// If we are doing ContentItem and have a payload and are not a launch
		// short-circuit to ContentItem
		if ( "/content.item".equals(request.getPathInfo()) ) {
			if ( ! ServerConfigurationService.getBoolean("contentitem.provider", true))  {
				M_log.warn("ContentItem is Disabled IP=" + ipAddress);
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"ContentItem is Disabled");
				return;
			} else {
				Session sess = SessionManager.getCurrentSession();
				Map session_payload = (Map) sess.getAttribute("payload");
				if ( session_payload != null ) {
					// Post-Login requests to content.item
					M_log.debug("ContentItem already logged in "+sess.getUserId());
					handleContentItem(request, response, session_payload);
					return;
				}
			}
		}

		if (M_log.isDebugEnabled()) {
			Map<String, String[]> params = (Map<String, String[]>) request
					.getParameterMap();
			for (Map.Entry<String, String[]> param : params.entrySet()) {
				M_log.debug(param.getKey() + ":" + param.getValue()[0]);
			}
		}

		Map payload = getPayloadAsMap(request);

		// Get the list of highly trusted consumers from sakai.properties.
		// If the incoming consumer is highly trusted, we use the context_id and
		// site_id as is,
		// ie without prefixing them with the oauth_consumer_key first.
		// We also don't both checking their roles in the site.
		boolean isTrustedConsumer = BasicLTIProviderUtil.isHighlyTrustedConsumer(payload);

		/*
		 * Get the list of email trusted consumers from sakai.properties. If the
		 * incoming consumer is email trusted, we use the email address provided
		 * by the consumer and look up the "user" info from sakai instead of
		 * consumer's. This use case is especially valuable if 2 different LMS's
		 * acting as TP and TC referring to same user and can be uniquely
		 * identified by email address. more details SAK-29372
		 */
		boolean isEmailTrustedConsumer = BasicLTIProviderUtil.isEmailTrustedConsumer(payload);

		/*
		 * Checking if the email trusted consumer property and trusted consumer
		 * and not both enabled. the case would be an error condition
		 */
		if (isTrustedConsumer && isEmailTrustedConsumer) {
			M_log.warn("Both Email Trusted and Trusted Consumer property is enabled, this is invalid  IP=" + ipAddress);
			response.sendError(HttpServletResponse.SC_FORBIDDEN,
					"Both Email Trusted and Trusted Consumer property is enabled, this is invalid ");
			return;

		}

        try {
            invokeProcessors(payload, isTrustedConsumer, ProcessingState.beforeValidation);

            validate(payload, isTrustedConsumer);

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterValidation);

            User user = userFinderOrCreator.findOrCreateUser(payload, isTrustedConsumer, isEmailTrustedConsumer);

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterUserCreation, user);

            // Check if we are loop-backing on the same server, and already logged in as same user
            Session sess = SessionManager.getCurrentSession();
            String serverUrl = SakaiBLTIUtil.getOurServerUrl();
            String ext_sakai_server = (String) payload.get("ext_sakai_server");

            if ( "/content.item".equals(request.getPathInfo()) && isTrustedConsumer &&
                ext_sakai_server != null && ext_sakai_server.equals(serverUrl) &&
                user.getId().equals(sess.getUserId()) ) {

                M_log.debug("ContentItem looping back as "+sess.getUserId());
                sess.setAttribute("payload", payload);
                handleContentItem(request, response, payload);
                return;
            }

            loginUser(ipAddress, user);

            // Re-grab the session
            sess = SessionManager.getCurrentSession();

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterLogin, user);

            // This needs to happen after login, when we have a session for the user.
            userLocaleSetter.setupUserLocale(payload, user, isTrustedConsumer,isEmailTrustedConsumer);

            userPictureSetter.setupUserPicture(payload, user, isTrustedConsumer, isEmailTrustedConsumer);

            // The first launch of content.item - no site needed
            if ( "/content.item".equals(request.getPathInfo()) ) {
                    M_log.debug("ContentItem inital external login "+sess.getUserId());
                    sess.setAttribute("payload", payload);
					handleContentItem(request, response, payload);
                    return;
            }

            Site site = findOrCreateSite(payload, isTrustedConsumer);

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterSiteCreation, user, site);

            siteEmailPreferenceSetter.setupUserEmailPreferenceForSite(payload, user, site, isTrustedConsumer);

            site = siteMembershipUpdater.addOrUpdateSiteMembership(payload, isTrustedConsumer, user, site);

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterSiteMembership, user, site);

            String toolPlacementId = addOrCreateTool(payload, isTrustedConsumer, user, site);

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.beforeLaunch, user, site);

            syncSiteMembershipsOnceThenSchedule(payload, site, isTrustedConsumer, isEmailTrustedConsumer);

            // Construct a URL to this tool
            StringBuilder url = new StringBuilder();
                url.append(SakaiBLTIUtil.getOurServerUrl());
                url.append(ServerConfigurationService.getString("portalPath", "/portal"));
                url.append("/tool-reset/");
                url.append(toolPlacementId);
                url.append("?panel=Main");

            if (M_log.isDebugEnabled()) {
                M_log.debug("url=" + url.toString());
            }
            //String toolLink = ServerConfigurationService.getPortalUrl()+ "/tool-reset/" + placement_id + "?panel=Main";
            // Compensate for bug in getPortalUrl()
            //toolLink = toolLink.replace("IMS BLTI Portlet", "portal");
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect(url.toString());

        } catch (LTIException ltiException) {
            doError(request, response, ltiException.getErrorKey(), ltiException.getMessage(), ltiException.getCause());
        }


		/*
		
		PrintWriter out = response.getWriter();
		out.println("<body><div style=\"text-align: center\">");
		out.println("&nbsp;<br/>&nbsp;<br/>&nbsp;<br/>&nbsp;<br/>");
		out.println("&nbsp;<br/>&nbsp;<br/>&nbsp;<br/>&nbsp;<br/>");
		out.println("<a href=\"" + url.toString() + "\">");
		out.println("<span id=\"hideme\">" + rb.getString("launch.continue")
				+ "</span>");
		out.println("</a>");
		out.println(" <script language=\"javascript\"> \n"
						+ "    document.getElementById(\"hideme\").style.display = \"none\";\n"
						+ "    location.href=\"" + url.toString() + "\";\n"
						+ " </script> \n");
		out.println("</div>");
		out.println("</body>");
		
		out.close();
		*/
		

	}

    protected void invokeProcessors(Map payload, boolean trustedConsumer, ProcessingState processingState, User user) throws LTIException {
        invokeProcessors(payload, trustedConsumer, processingState, user, null, null);
    }

    protected void invokeProcessors(Map payload, boolean trustedConsumer,
                                    ProcessingState processingState) throws LTIException{
        invokeProcessors(payload, trustedConsumer, processingState, null, null, null);
    }

    protected void invokeProcessors(Map payload, boolean trustedConsumer,
                                    ProcessingState processingState, User user,
                                    Site site) throws LTIException{
        invokeProcessors(payload, trustedConsumer, processingState, user, site, null);
    }


    protected void invokeProcessors(Map payload, boolean trustedConsumer,
                                    ProcessingState processingState, User user,
                                    Site site, String toolPlacementId) throws LTIException{
        if (!bltiProcessors.isEmpty()) {
            for (BLTIProcessor processor : bltiProcessors) {
                switch (processingState) {

                    case beforeValidation:
                        processor.beforeValidation(payload, trustedConsumer);
                        break;
                    case afterValidation:
                        processor.afterValidation(payload, trustedConsumer);
                        break;
                    case afterUserCreation:
                        processor.afterUserCreation(payload, user);
                        break;
                    case afterLogin:
                        processor.afterLogin(payload, trustedConsumer, user);
                        break;
                    case afterSiteCreation:
                        processor.afterSiteCreation(payload, trustedConsumer, user, site);
                        break;
                    case afterSiteMembership:
                        processor.afterSiteMembership(payload, trustedConsumer, user, site);
                        break;
                    case beforeLaunch:
                        processor.beforeLaunch(payload, trustedConsumer, user, site, toolPlacementId);
                        break;
                    default:
                        M_log.error("unknown processing state of " + processingState);
                }
            }
        }
    }

    protected void validate(Map payload, boolean isTrustedConsumer) throws LTIException
    {
          //check parameters
          String lti_message_type = (String) payload.get(BasicLTIConstants.LTI_MESSAGE_TYPE);
          String lti_version = (String) payload.get(BasicLTIConstants.LTI_VERSION);
          String oauth_consumer_key = (String) payload.get("oauth_consumer_key");
          String resource_link_id = (String) payload.get(BasicLTIConstants.RESOURCE_LINK_ID);
          String user_id = (String) payload.get(BasicLTIConstants.USER_ID);
          String context_id = (String) payload.get(BasicLTIConstants.CONTEXT_ID);


          boolean launch = true;
          if( BasicLTIUtil.equals(lti_message_type, "basic-lti-launch-request") ) {
              launch = true;
          } else if ( BasicLTIUtil.equals(lti_message_type, "ContentItemSelectionRequest") ) {
              launch = false;
          } else {
              throw new LTIException("launch.invalid", "lti_message_type="+lti_message_type, null);
          }

          if(!BasicLTIUtil.equals(lti_version, "LTI-1p0")) {
              throw new LTIException( "launch.invalid", "lti_version="+lti_version, null);

          }

          if(BasicLTIUtil.isBlank(oauth_consumer_key)) {
              throw new LTIException( "launch.missing", "oauth_consumer_key", null);

          }

          if(launch && BasicLTIUtil.isBlank(resource_link_id)) {
              throw new LTIException( "launch.missing", "resource_link_id", null);

          }

          if(BasicLTIUtil.isBlank(user_id)) {
              throw new LTIException( "launch.missing", "user_id", null);

          }
          if (M_log.isDebugEnabled()) {
              M_log.debug("user_id=" + user_id);
          }

          //check tool_id
          String tool_id = (String) payload.get("tool_id");
          if (tool_id == null) {
              throw new LTIException("launch.tool_id.required", null, null);
          }

          // Trim off the leading slash and any trailing space
          tool_id = tool_id.substring(1).trim();
          if (M_log.isDebugEnabled()) {
              M_log.debug("tool_id=" + tool_id);
          }
          // store modified tool_id back in payload
          payload.put("tool_id", tool_id);
          final String allowedToolsConfig = ServerConfigurationService.getString("basiclti.provider.allowedtools", "");

          final String[] allowedTools = allowedToolsConfig.split(":");
          final List<String> allowedToolsList = Arrays.asList(allowedTools);

          if (launch && allowedTools != null && !allowedToolsList.contains(tool_id)) {
              throw new LTIException( "launch.tool.notallowed", tool_id, null);
          }
          final Tool toolCheck = ToolManager.getTool(tool_id);
          if (launch && toolCheck == null) {
              throw new LTIException("launch.tool.notfound", tool_id, null);
          }




          // Check for the ext_sakai_provider_eid param. If set, this will contain the eid that we are to use
          // in place of using the user_id parameter
          // WE still need that parameter though, so translate it from the given eid.
          boolean useProvidedEid = false;
          String ext_sakai_provider_eid = (String) payload.get(BasicLTIConstants.EXT_SAKAI_PROVIDER_EID);
          if(BasicLTIUtil.isNotBlank(ext_sakai_provider_eid)){
              useProvidedEid = true;
              try {
                  user_id = UserDirectoryService.getUserId(ext_sakai_provider_eid);
              } catch (Exception e) {
                  M_log.error(e.getLocalizedMessage(), e);
                  throw new LTIException("launch.provided.eid.invalid", "ext_sakai_provider_eid="+ext_sakai_provider_eid, e);
              }
          }

          if (M_log.isDebugEnabled()) {
              M_log.debug("ext_sakai_provider_eid=" + ext_sakai_provider_eid);
          }


          // Contextualize the context_id with the OAuth consumer key
          // Also use the resource_link_id for the context_id if we did not get a context_id
          // BLTI-31: if trusted, context_id is required and use the param without modification
          if(BasicLTIUtil.isBlank(context_id)) {
              if(isTrustedConsumer) {
                  throw new LTIException( "launch.missing",context_id, null);
              } else {
                  context_id = "res:" + resource_link_id;
                  payload.put(BasicLTIConstants.CONTEXT_ID, context_id);
              }
          }

          // Check if context_id is simply a ~. If so, get the id of that user's My Workspace site
          // and use that to construct the full context_id
          if(BasicLTIUtil.equals(context_id, "~")){
              if(useProvidedEid) {
                  String userSiteId = null;
                  try {
                      userSiteId = SiteService.getUserSiteId(user_id);
                  } catch (Exception e) {
                      M_log.warn("Failed to get My Workspace site for user_id:" + user_id);
                      M_log.error(e.getLocalizedMessage(), e);
                      throw new LTIException( "launch.user.site.unknown", "user_id="+user_id, e);
                  }
                  context_id = userSiteId;
                  payload.put(BasicLTIConstants.CONTEXT_ID, context_id);
              }
          }

          if (M_log.isDebugEnabled()) {
              M_log.debug("context_id=" + context_id);
          }


          // Lookup the secret
          final String configPrefix = "basiclti.provider." + oauth_consumer_key + ".";
          final String oauth_secret = ServerConfigurationService.getString(configPrefix+ "secret", null);
          if (oauth_secret == null) {
              throw new LTIException( "launch.key.notfound",oauth_consumer_key, null);
          }
          final OAuthMessage oam = (OAuthMessage) payload.get("oauth_message");

          final String forcedURIScheme = ServerConfigurationService.getString("basiclti.provider.forcedurischeme", null);

          if(forcedURIScheme != null) {
        	  try {
        		  URI testURI = new URI(oam.URL);
        		  URI newURI = new URI(forcedURIScheme,testURI.getSchemeSpecificPart(),null);
        		  oam.URL = newURI.toString();
        	  } catch (URISyntaxException use) {
        	  }
          }
          final OAuthValidator oav = new SimpleOAuthValidator();
          final OAuthConsumer cons = new OAuthConsumer("about:blank#OAuth+CallBack+NotUsed", oauth_consumer_key,oauth_secret, null);

          final OAuthAccessor acc = new OAuthAccessor(cons);

          String base_string = null;
          try {
              base_string = OAuthSignatureMethod.getBaseString(oam);
          } catch (Exception e) {
              M_log.error(e.getLocalizedMessage(), e);
              base_string = null;
          }

          try {
              oav.validateMessage(oam, acc);
          } catch (Exception e) {
              M_log.warn("Provider failed to validate message");
              M_log.warn(e.getLocalizedMessage(), e);
              if (base_string != null) {
                  M_log.warn(base_string);
              }
              throw new LTIException( "launch.no.validate", context_id, e);
          }

          final Session sess = SessionManager.getCurrentSession();

          if (sess == null) {
              throw new LTIException( "launch.no.session", context_id, null);
          }


      }


    private String addOrCreateTool(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException {
        // Check if the site already has the tool
        String toolPlacementId = null;
        String tool_id = (String) payload.get("tool_id");
        try {
            site = SiteService.getSite(site.getId());
            ToolConfiguration toolConfig = site.getToolForCommonId(tool_id);
            if(toolConfig != null) {
                toolPlacementId = toolConfig.getId();
            }
        } catch (Exception e) {
            M_log.warn(e.getLocalizedMessage(), e);
            throw new LTIException( "launch.tool.search", "tool_id="+tool_id, e);
        }

        if (M_log.isDebugEnabled()) {
            M_log.debug("toolPlacementId=" + toolPlacementId);
        }

        // If tool not in site, and we are a trusted consumer, error
        // Otherwise, add tool to the site
        ToolConfiguration toolConfig = null;
        if(BasicLTIUtil.isBlank(toolPlacementId)) {
            try {
                SitePage sitePageEdit = null;
                sitePageEdit = site.addPage();
                sitePageEdit.setTitle(tool_id);

                toolConfig = sitePageEdit.addTool();
                toolConfig.setTool(tool_id, ToolManager.getTool(tool_id));
                toolConfig.setTitle(tool_id);

                Properties propsedit = toolConfig.getPlacementConfig();
                propsedit.setProperty(BASICLTI_RESOURCE_LINK,  (String) payload.get(BasicLTIConstants.RESOURCE_LINK_ID));
                pushAdvisor();
                try {
                    SiteService.save(site);
                    M_log.info("Tool added, tool_id="+tool_id + ", siteId="+site.getId());
                } catch (Exception e) {
                    throw new LTIException( "launch.site.save", "tool_id="+tool_id + ", siteId="+site.getId(), e);
                } finally {
                    popAdvisor();
                }
                toolPlacementId = toolConfig.getId();

            } catch (Exception e) {
                throw new LTIException( "launch.tool.add", "tool_id="+tool_id + ", siteId="+site.getId(), e);
            }
        }

        // Get ToolConfiguration for tool if not already setup
        if(toolConfig == null){
            toolConfig =  site.getToolForCommonId(tool_id);
        }

        // Check user has access to this tool in this site
        if(!ToolManager.isVisible(site, toolConfig)) {
            M_log.warn("Not allowed to access tool user_id=" + user.getId() + " site="+ site.getId() + " tool=" + tool_id);
            throw new LTIException( "launch.site.tool.denied", "user_id=" + user.getId() + " site="+ site.getId() + " tool=" + tool_id, null);

        }
        return toolPlacementId;
    }

    protected Site findOrCreateSite(Map payload, boolean trustedConsumer) throws LTIException {

        String context_id = (String) payload.get(BasicLTIConstants.CONTEXT_ID);
        String oauth_consumer_key = (String) payload.get("oauth_consumer_key");
        String siteId = null;

        if (trustedConsumer) {
            siteId = context_id;
        } else {
            siteId = LegacyShaUtil.sha1Hash(oauth_consumer_key + ":" + context_id);
        }
        if (M_log.isDebugEnabled()) {
            M_log.debug("siteId=" + siteId);
        }

        final String context_title = (String) payload.get(BasicLTIConstants.CONTEXT_TITLE);
        final String context_label = (String) payload.get(BasicLTIConstants.CONTEXT_LABEL);

        Site site = null;

        // Get the site if it exists
        if (ServerConfigurationService.getBoolean("basiclti.provider.lookupSitesByLTIContextIdProperty", false))  {
            try {
                site = findSiteByLTIContextId(context_id);
                if (site != null) {
                    updateSiteDetailsIfChanged(site, context_title, context_label);
                    return site;
                }
            } catch (Exception e) {
                if (M_log.isDebugEnabled()) {
                    M_log.debug(e.getLocalizedMessage(), e);
                }
            }
        } else {
            try {
                site = SiteService.getSite(siteId);
                updateSiteDetailsIfChanged(site, context_title, context_label);
                return site;
            } catch (Exception e) {
                if (M_log.isDebugEnabled()) {
                    M_log.debug(e.getLocalizedMessage(), e);
                }
            }
        }

    // If trusted and site does not exist, error, otherwise, create the site
        if (trustedConsumer) {
            throw new LTIException("launch.site.invalid", "siteId=" + siteId, null);
        } else {

            pushAdvisor();
            try {
                String sakai_type = "project";

                // BLTI-154. If an autocreation site template has been specced in sakai.properties, use it.
                String autoSiteTemplateId = ServerConfigurationService.getString("basiclti.provider.autositetemplate", null);

                boolean templateSiteExists = SiteService.siteExists(autoSiteTemplateId);

                if(!templateSiteExists) {
                    M_log.warn("A template site id was specced (" + autoSiteTemplateId + ") but no site with this id exists. A default lti site will be created instead.");
                }

                if(autoSiteTemplateId == null || !templateSiteExists) {
                    //BLTI-151 If the new site type has been specified in sakai.properties, use it.
                    sakai_type = ServerConfigurationService.getString("basiclti.provider.newsitetype", null);
                    if(BasicLTIUtil.isBlank(sakai_type)) {
                        // It wasn't specced in the props. Test for the ims course context type.
                        final String context_type = (String) payload.get(BasicLTIConstants.CONTEXT_TYPE);
                        if (BasicLTIUtil.equalsIgnoreCase(context_type, "course")) {
                            sakai_type = "course";
                        } else {
                            sakai_type = BasicLTIConstants.NEW_SITE_TYPE;
                        }
                    }
                    site = SiteService.addSite(siteId, sakai_type);
                    site.setType(sakai_type);
                } else {
               		Site autoSiteTemplate = SiteService.getSite(autoSiteTemplateId);
               		site = SiteService.addSite(siteId, autoSiteTemplate);
                }

                if (BasicLTIUtil.isNotBlank(context_title)) {
                    site.setTitle(context_title);
                }
                if (BasicLTIUtil.isNotBlank(context_label)) {
                    site.setShortDescription(context_label);
                }
                site.setJoinable(false);
                site.setPublished(true);
                site.setPubView(false);
                // record the original context_id to a site property
                site.getPropertiesEdit().addProperty(LTI_CONTEXT_ID, context_id);

                try {
                    SiteService.save(site);
                    M_log.info("Created  site=" + siteId + " label=" + context_label + " type=" + sakai_type + " title=" + context_title);

                } catch (Exception e) {
                    throw new LTIException("launch.site.save", "siteId=" + siteId, e);
                }

            } catch (Exception e) {
                throw new LTIException("launch.create.site", "siteId=" + siteId, e);
            } finally {
                popAdvisor();
            }
        }

        try {
            return SiteService.getSite(site.getId());
        } catch (IdUnusedException e) {
			throw new LTIException( "launch.site.invalid", "siteId="+siteId, e);

		}
    }

    private final void updateSiteDetailsIfChanged(Site site, String context_title, String context_label) {

        boolean changed = false;

        if (BasicLTIUtil.isNotBlank(context_title) && !context_title.equals(site.getTitle())) {
            site.setTitle(context_title);
            changed = true;
        }

        if (BasicLTIUtil.isNotBlank(context_label) && !context_label.equals(site.getShortDescription())) {
            site.setShortDescription(context_label);
            changed = true;
        }

        if(changed) {
            try {
                SiteService.save(site);
                M_log.info("Updated  site=" + site.getId() + " title=" + context_title + " label=" + context_label);
            } catch (Exception e) {
                M_log.warn("Failed to update site title and/or label");
            }
        }
    }

    private void loginUser(String ipAddress, User user) {
        Session sess = SessionManager.getCurrentSession();
        UsageSessionService.login(user.getId(), user.getEid(), ipAddress, null, UsageSessionService.EVENT_LOGIN_WS);
        sess.setUserId(user.getId());
        sess.setUserEid(user.getEid());
    }


	public void destroy() {

	}


    public Site findSiteByLTIContextId(String externalOaeId) throws Exception {
        Map propertyCriteria = new HashMap();

		// Replace search property
		propertyCriteria.put(LTI_CONTEXT_ID, externalOaeId);

		List list = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ANY, null, null,
				propertyCriteria, org.sakaiproject.site.api.SiteService.SortType.NONE, null);

		if (list != null && list.size() > 0) {
            for (Iterator i=list.iterator(); i.hasNext();) {
                Site site = (Site) i.next();
                if (site.getProperties() != null) {
                    String loadedExternalSiteId = (String) site.getProperties().get(LTI_CONTEXT_ID);
                    if (loadedExternalSiteId != null && loadedExternalSiteId.equals(externalOaeId)) {
                        // deeply load site, otherwise groups won't be loaded
                        M_log.debug("found site: " + site.getId() + " with lti_context_id:" + externalOaeId);
                        return SiteService.getSite(site.getId());
                    }
                }
            }
        }

		return null;
	}

    private void syncSiteMembershipsOnceThenSchedule(Map payload, Site site, boolean isTrustedConsumer, boolean isEmailTrustedConsumer) throws LTIException {

        if (isTrustedConsumer) return;

        M_log.debug("synchSiteMembershipsOnceThenSchedule");

        if (!ServerConfigurationService.getBoolean(SakaiBLTIUtil.INCOMING_ROSTER_ENABLED, false)) {
            M_log.info("LTI Memberships synchronization disabled.");
            return;
        }

        final String membershipsUrl = (String) payload.get("ext_ims_lis_memberships_url");

        if (!BasicLTIUtil.isNotBlank(membershipsUrl)) {
            M_log.info("LTI Memberships extension is not supported.");
            return;
        }

        if(M_log.isDebugEnabled()) M_log.debug("Memberships URL: " + membershipsUrl);

        final String membershipsId = (String) payload.get("ext_ims_lis_memberships_id");

        if (!BasicLTIUtil.isNotBlank(membershipsId)) {
            M_log.info("No memberships id supplied. Memberships will NOT be synchronized.");
            return;
        }

        final String siteId = site.getId();

        // If this site has already been scheduled, then we do nothing.
        if (ltiService.getMembershipsJob(siteId) != null) {
            if (M_log.isDebugEnabled()) {
                M_log.debug("Site '" + siteId + "' already scheduled for memberships sync. Doing nothing ...");
            }
            return;
        }

        final String oauth_consumer_key = (String) payload.get(OAuth.OAUTH_CONSUMER_KEY);

        // This is non standard. Moodle's core LTI plugin does not currently do memberships and
        // a fix for this has been proposed at https://tracker.moodle.org/browse/MDL-41724. I don't
        // think this will ever become core and the first time memberships will appear in core lti
        // is with LTI2. At that point this code will be replaced with standard LTI2 JSON type stuff.

        String lms = (String) payload.get("ext_lms");
        final String callbackType
            = (BasicLTIUtil.isNotBlank(lms) && lms.equals("moodle-2"))
                ? "ext-moodle-2" : (String) payload.get(BasicLTIConstants.LTI_VERSION);

        (new Thread(new Runnable() {

                public void run() {

                    long then = 0L;

                    if (M_log.isDebugEnabled()) {
                        M_log.debug("Starting memberships sync.");
                        then = (new Date()).getTime();
                    }

                    siteMembershipsSynchroniser.synchroniseSiteMemberships(siteId, membershipsId, membershipsUrl, oauth_consumer_key, isEmailTrustedConsumer,callbackType);

                    if (M_log.isDebugEnabled()) {
                        long now = (new Date()).getTime();
                        M_log.debug("Memberships sync finished. It took " + ((now - then)/1000) + " seconds.");
                    }
                }
            }, "org.sakaiproject.blti.ProviderServlet.MembershipsSync")).start();

        ltiService.insertMembershipsJob(siteId, membershipsId, membershipsUrl, oauth_consumer_key, callbackType);
    }

	private void handleCASAList(HttpServletRequest request, HttpServletResponse response)
	{
                ArrayList<Application> apps = new ArrayList<Application>();

		String allowedToolsConfig = ServerConfigurationService.getString("basiclti.provider.allowedtools", "");
		String[] allowedTools = allowedToolsConfig.split(":");
		List<String> allowedToolsList = Arrays.asList(allowedTools);

		for (String toolId : allowedToolsList) {
			Application app = SakaiCASAUtil.getCASAEntry(toolId);
			if ( app == null ) {
				M_log.warn("Could not produce CASA entry for "+toolId);
				continue;
			}
			apps.add(app);
		}

                try {
		        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json");
                        PrintWriter out = response.getWriter();
                        out.write(JacksonUtil.prettyPrint(apps));
                }
                catch (Exception e) {
                        e.printStackTrace();
                }
	}

	private void handleContentItem(HttpServletRequest request, HttpServletResponse response, Map payload)
		throws ServletException, IOException
	{

		String allowedToolsConfig = ServerConfigurationService.getString("basiclti.provider.allowedtools", "");
		String[] allowedTools = allowedToolsConfig.split(":");
		List<String> allowedToolsList = Arrays.asList(allowedTools);

		String tool_id = (String) request.getParameter("install");
		if ( tool_id == null ) {
			ArrayList<Tool> tools = new ArrayList<Tool>();
			for (String toolId : allowedToolsList) {
				Tool theTool = ToolManager.getTool(toolId);
				if ( theTool == null ) continue;
				tools.add(theTool);
			}
			request.setAttribute("tools",tools);
		} else {
			if ( !allowedToolsList.contains(tool_id)) {
				doError(request, response, "launch.tool.notallowed", tool_id, null);
				return;
			}
			final Tool toolCheck = ToolManager.getTool(tool_id);
			if ( toolCheck == null) {
				doError(request, response, "launch.tool.notfound", tool_id, null);
				return;
			}

			String content_item_return_url = (String) payload.get("content_item_return_url");
			if ( content_item_return_url == null) {
				doError(request, response, "content_item.return_url.notfound", tool_id, null);
				return;
			}

			ContentItemResponse resp = SakaiContentItemUtil.getContentItemResponse(tool_id);
			if ( resp == null) {
				doError(request, response, "launch.tool.notfound", tool_id, null);
				return;
			}
			String content_items = resp.prettyPrintLog();

			// Set up the return
			Map<String, String> ltiMap = new HashMap<String, String> ();
			Map<String, String> extra = new HashMap<String, String> ();
			ltiMap.put(BasicLTIConstants.LTI_MESSAGE_TYPE, BasicLTIConstants.LTI_MESSAGE_TYPE_CONTENTITEMSELECTION);
			ltiMap.put(BasicLTIConstants.LTI_VERSION, BasicLTIConstants.LTI_VERSION_1);
			ltiMap.put("content_items", content_items);
			String data = (String) payload.get("data");
			if ( data != null ) ltiMap.put("data", data);
			M_log.debug("ltiMap="+ltiMap);

			boolean dodebug = M_log.isDebugEnabled();
			boolean autosubmit = false;
			String launchtext = rb.getString("content_item.install.button");
			String back_to_store = rb.getString("content_item.back.to.store");
			extra.put("button_html","<input type=\"submit\" value=\""+back_to_store+"\"onclick=\"location.href='content.item'; return false;\">");
			String launch_html = BasicLTIUtil.postLaunchHTML(ltiMap, content_item_return_url, launchtext, autosubmit, dodebug, extra);

			request.setAttribute("back_to_store", rb.getString("content_item.back.to.store"));
			request.setAttribute("install",tool_id);
			request.setAttribute("launch_html",launch_html);
			request.setAttribute("tool",toolCheck);
		}

		// Forward to the JSP
		ServletContext sc = this.getServletContext();
		RequestDispatcher rd = sc.getRequestDispatcher("/contentitem.jsp");
		try {
			rd.forward(request, response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleCanvasConfig(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		String title = ServerConfigurationService.getString("canvas.config.title",
			rb.getString("canvas.config.title"));
		String description = ServerConfigurationService.getString("canvas.config.description",
			rb.getString("canvas.config.description"));
		String launch = ServerConfigurationService.getString("canvas.config.launch",
			SakaiLTIProviderUtil.getProviderLaunchUrl("content.item"));
		String icon = ServerConfigurationService.getString("canvas.config.domain",
			"https://www.apereo.org/sites/all/themes/apereo/images/apereo-logo-white-bg.png");
		request.setAttribute("title", title);
		request.setAttribute("description", description);
		request.setAttribute("launch", launch);
		request.setAttribute("icon",icon);

		try {
			String serverUrl = SakaiBLTIUtil.getOurServerUrl();
			URL netUrl = new URL(serverUrl);
			String host = netUrl.getHost();
			String domain = ServerConfigurationService.getString("canvas.config.domain", host);
			request.setAttribute("domain", domain);
		} catch (MalformedURLException e) {
			doError(request, response, "canvas.error.missing.domain", e.getMessage(), e.getCause());
		}

		// Forward to the JSP
		ServletContext sc = this.getServletContext();
		RequestDispatcher rd = sc.getRequestDispatcher("/canvas-config.jsp");
		try {
			rd.forward(request, response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
