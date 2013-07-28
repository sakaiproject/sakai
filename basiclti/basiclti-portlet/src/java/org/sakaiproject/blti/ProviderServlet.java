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

import java.lang.reflect.Method;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.*;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imsglobal.basiclti.BasicLTIConstants;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.lti.api.BLTIProcessor;
import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.basiclti.util.ShaUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.cover.IdManager;
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
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.PreferencesService;
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
    
    private Object profileImageLogicObject = null;
    private Method saveOfficialImageUrlMethod = null;
    private Object profilePreferencesLogicObject = null;
    private Method setUseOfficialImageMethod = null;
    private Method getPreferencesRecordForUserMethod = null;
    private Method savePreferencesRecordMethod = null;

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
        
        setupProfile2Methods();
	}
	
    /**
     * BLTI-155. Use reflection to lookup the Profile2 methods we need for setting profile
     * pictures provided by consumers
     */
	private void setupProfile2Methods() {
        // BLTI-155 START
        // Test whether Profile2 is available and setup the reflective methods if so
        profileImageLogicObject = ComponentManager.getInstance().get("org.sakaiproject.profile2.logic.ProfileImageLogic");
        profilePreferencesLogicObject = ComponentManager.getInstance().get("org.sakaiproject.profile2.logic.ProfilePreferencesLogic");
                
        if(profileImageLogicObject != null && profilePreferencesLogicObject != null) {
        	M_log.debug("Profile2 is installed.");
        	// It is. Cache the methods for later use.
            try {
            	saveOfficialImageUrlMethod = profileImageLogicObject.getClass().getMethod("saveOfficialImageUrl", new Class[] { String.class,String.class});
                getPreferencesRecordForUserMethod = profilePreferencesLogicObject.getClass().getMethod("getPreferencesRecordForUser", new Class[] { String.class });
                Class preferencesClazz = Class.forName("org.sakaiproject.profile2.model.ProfilePreferences");
                setUseOfficialImageMethod = preferencesClazz.getMethod("setUseOfficialImage", new Class[] { boolean.class});
                savePreferencesRecordMethod = profilePreferencesLogicObject.getClass().getMethod("savePreferencesRecord", new Class[] { preferencesClazz });
                M_log.debug("Methods cached.");
            } catch(Exception e) {
            	M_log.warn("Tried to locate the profile2 api but failed. Consumer user_image launch parameters WILL NOT be shown");
            }
        }
        // BLTI-155 END
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

        payload.put("oauth_message", OAuthServlet.getMessage(request, null));
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

		if (M_log.isDebugEnabled()) {
			Map<String, String[]> params = (Map<String, String[]>) request
					.getParameterMap();
			for (Map.Entry<String, String[]> param : params.entrySet()) {
				M_log.debug(param.getKey() + ":" + param.getValue()[0]);
			}
		}

		Map payload = getPayloadAsMap(request);


		// Get the list of highly trusted consumers from sakai.properties.
		// If the incoming consumer is highly trusted, we use the context_id and site_id as is,
		// ie without prefixing them with the oauth_consumer_key first.
		// We also don't both checking their roles in the site.
        boolean isTrustedConsumer = isTrustedConsumer(payload);

        try {
            invokeProcessors(payload, isTrustedConsumer, ProcessingState.beforeValidation);

            validate(payload, isTrustedConsumer);

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterValidation);

            User user = findOrCreateUser(payload, isTrustedConsumer);
            
            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterUserCreation, user);

            loginUser(ipAddress, user);
            
            setupUserPicture(payload, user, isTrustedConsumer);
            
            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterLogin, user);

            Site site = findOrCreateSite(payload, isTrustedConsumer);

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterSiteCreation, user, site);

            site = addOrUpdateSiteMembership(payload, isTrustedConsumer, user, site);

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.afterSiteMembership, user, site);

            String toolPlacementId = addOrCreateTool(payload, isTrustedConsumer, user, site);

            invokeProcessors(payload, isTrustedConsumer, ProcessingState.beforeLaunch, user, site);


            // Construct a URL to this tool
            StringBuilder url = new StringBuilder();
                url.append(ServerConfigurationService.getServerUrl());
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

    protected void validate(Map payload, boolean isTrustedConsumer) throws LTIException {


          //check parameters
          String lti_message_type = (String) payload.get(BasicLTIConstants.LTI_MESSAGE_TYPE);
          String lti_version = (String) payload.get(BasicLTIConstants.LTI_VERSION);
          String oauth_consumer_key = (String) payload.get("oauth_consumer_key");
          String resource_link_id = (String) payload.get(BasicLTIConstants.RESOURCE_LINK_ID);
          String user_id = (String) payload.get(BasicLTIConstants.USER_ID);
          String context_id = (String) payload.get(BasicLTIConstants.CONTEXT_ID);


          if(!BasicLTIUtil.equals(lti_message_type, "basic-lti-launch-request")) {
              throw new LTIException("launch.invalid", "lti_message_type="+lti_message_type, null);
          }

          if(!BasicLTIUtil.equals(lti_version, "LTI-1p0")) {
              throw new LTIException( "launch.invalid", "lti_version="+lti_version, null);

          }

          if(BasicLTIUtil.isBlank(oauth_consumer_key)) {
              throw new LTIException( "launch.missing", "oauth_consumer_key", null);

          }

          if(BasicLTIUtil.isBlank(resource_link_id)) {
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

          if (allowedTools != null && !allowedToolsList.contains(tool_id)) {
              throw new LTIException( "launch.tool.notallowed", tool_id, null);
          }
          final Tool toolCheck = ToolManager.getTool(tool_id);
          if (toolCheck == null) {
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
            new LTIException( "launch.tool.search", "tool_id="+tool_id, e);

        }

        if (M_log.isDebugEnabled()) {
            M_log.debug("toolPlacementId=" + toolPlacementId);
        }

        // If tool not in site, and we are a trusted consumer, error
        // Otherwise, add tool to the site
        ToolConfiguration toolConfig = null;
        if(BasicLTIUtil.isBlank(toolPlacementId)) {
            if(trustedConsumer) {
                new LTIException("launch.site.tool.missing", "tool_id="+tool_id + ", siteId="+site.getId(), null);

            } else {
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
                        new LTIException( "launch.site.save", "tool_id="+tool_id + ", siteId="+site.getId(), e);

                    } finally {
                        popAdvisor();
                    }
                    toolPlacementId = toolConfig.getId();

                } catch (Exception e) {
                    new LTIException( "launch.tool.add", "tool_id="+tool_id + ", siteId="+site.getId(), e);

                }
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

    private Site addOrUpdateSiteMembership(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException {
        String userrole = getUserRole(payload, trustedConsumer);

        // Check if the user is a member of the site already
        boolean userExistsInSite = false;
        try {
            Member member = site.getMember(user.getId());
            if(member != null && BasicLTIUtil.equals(member.getUserEid(), user.getEid())) {
                userExistsInSite = true;
            }
        } catch (Exception e) {
            M_log.warn(e.getLocalizedMessage(), e);
            throw new LTIException( "launch.site.invalid", "siteId="+site.getId(), e);

        }

        if (M_log.isDebugEnabled()) {
            M_log.debug("userExistsInSite=" + userExistsInSite);
        }

        // If not a member of the site, and we are a trusted consumer, error
        // Otherwise, add them to the site
        if(!userExistsInSite) {
            if(trustedConsumer) {
                throw new LTIException( "launch.site.user.missing", "user_id="+user.getId()+ ", siteId="+site.getId(), null);
            } else {
                try {
                    site = SiteService.getSite(site.getId());
                    Set<Role> roles = site.getRoles();

                    //BLTI-151 see if we can directly map the incoming role to the list of site roles
                    String newRole = null;
                    if (M_log.isDebugEnabled()) {
                        M_log.debug("Incoming userrole:" + userrole);
                    }
                    for (Role r : roles) {
                        String roleId = r.getId();

                        if (BasicLTIUtil.equalsIgnoreCase(roleId, userrole)) {
                            newRole = roleId;
                            if (M_log.isDebugEnabled()) {
                                M_log.debug("Matched incoming role to role in site:" + roleId);
                            }
                            break;
                        }
                    }

                    //if we haven't mapped a role, check against the standard roles and fallback
                    if (BasicLTIUtil.isBlank(newRole)) {

                        if (M_log.isDebugEnabled()) {
                            M_log.debug("No match, falling back to determine role");
                        }

                        String maintainRole = site.getMaintainRole();
                        String joinerRole = site.getJoinerRole();

                        for (Role r : roles) {
                            String roleId = r.getId();
                            if (maintainRole == null && (roleId.equalsIgnoreCase("maintain") || roleId.equalsIgnoreCase("instructor"))) {
                                maintainRole = roleId;
                            }

                            if (joinerRole == null && (roleId.equalsIgnoreCase("access") || roleId.equalsIgnoreCase("student"))) {
                                joinerRole = roleId;
                            }
                        }

                        boolean isInstructor = userrole.indexOf("instructor") >= 0;
                        newRole = joinerRole;
                        if (isInstructor && maintainRole != null) {
                            newRole = maintainRole;
                        }

                        if (M_log.isDebugEnabled()) {
                            M_log.debug("Determined newRole as: " + newRole);
                        }
                    }
                    if (newRole == null) {
                        M_log.warn("Could not find Sakai role, role=" + userrole+ " user=" + user.getId() + " site=" + site.getId());
                        throw new LTIException( "launch.role.missing", "siteId="+site.getId(), null);

                    }


                    Role currentRoleObject = site.getUserRole(user.getId());
                    String currentRole = null;
                    if (currentRoleObject != null) {
                        currentRole = currentRoleObject.getId();
                    }

                    if (!newRole.equals(currentRole)) {
                        site.addMember(user.getId(), newRole, true, false);
                        if (currentRole == null) {
                            M_log.info("Added role=" + newRole + " user=" + user.getId() + " site=" + site.getId() + " LMS Role=" + userrole);
                        } else {
                            M_log.info("Old role=" + currentRole + " New role=" + newRole + " user=" + user.getId() + " site=" + site.getId()+ " LMS Role=" + userrole);
                        }


                        pushAdvisor();
                        String tool_id = (String) payload.get("tool_id");
                        try {
                            SiteService.save(site);
                            M_log.info("Site saved role=" + newRole + " user="+ user.getId() + " site=" + site.getId());

                        } catch (Exception e) {
                            throw new LTIException("launch.site.save", "siteId="+ site.getId() + " tool_id=" + tool_id, e);
                        } finally {
                            popAdvisor();
                        }

                    }
                } catch (Exception e) {
                    M_log.warn("Could not add user to site role=" + userrole + " user="+ user.getId() + " site=" + site.getId());
                    M_log.warn(e.getLocalizedMessage(), e);
                    throw new LTIException( "launch.join.site", "siteId="+site.getId(), e);

                }
            }
        }
        return site;
    }

    protected Site findOrCreateSite(Map payload, boolean trustedConsumer) throws LTIException {

        String context_id = (String) payload.get(BasicLTIConstants.CONTEXT_ID);
        String oauth_consumer_key = (String) payload.get("oauth_consumer_key");
        String siteId = null;

        if (trustedConsumer) {
            siteId = context_id;
        } else {
            siteId = ShaUtil.sha1Hash(oauth_consumer_key + ":" + context_id);
        }
        if (M_log.isDebugEnabled()) {
            M_log.debug("siteId=" + siteId);
        }

        Site site = null;

        // Get the site if it exists
        if (ServerConfigurationService.getBoolean("basiclti.provider.lookupSitesByLTIContextIdProperty", false))  {
            try {
                site = findSiteByLTIContextId(context_id);
                if (site != null) {
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

            final String context_title = (String) payload.get(BasicLTIConstants.CONTEXT_TITLE);
            final String context_label = (String) payload.get(BasicLTIConstants.CONTEXT_LABEL);
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

    protected String getEid(Map payload, boolean trustedConsumer, String user_id) throws LTIException {
        String eid;
        String oauth_consumer_key = (String) payload.get("oauth_consumer_key");
        String ext_sakai_provider_eid = (String) payload.get(BasicLTIConstants.EXT_SAKAI_PROVIDER_EID);


        if(BasicLTIUtil.isNotBlank(ext_sakai_provider_eid)){
			eid = (String) payload.get(BasicLTIConstants.EXT_SAKAI_PROVIDER_EID);
		} else {

			if(trustedConsumer) {
				try {
					eid = UserDirectoryService.getUserEid(user_id);
				} catch (Exception e) {
					M_log.error(e.getLocalizedMessage(), e);
					throw new LTIException( "launch.user.invalid", "user_id="+user_id, e);
				}
			} else {
				eid = oauth_consumer_key + ":" + user_id;
			}
			if (M_log.isDebugEnabled()) {
				M_log.debug("eid=" + eid);
			}
		}
        return eid;
    }

    private String getUserRole(Map payload, boolean trustedConsumer) {
        // Setup role in the site. If trusted, we don't need this as the user already has a role in the site
        String userrole = null;

        if(!trustedConsumer) {
            userrole = (String) payload.get(BasicLTIConstants.ROLES);
            if (userrole == null) {
                userrole = "";
            } else {
                userrole = userrole.toLowerCase();
            }
        }
        return userrole;
    }

    protected User findOrCreateUser(Map payload, boolean trustedConsumer) throws LTIException {
        User user;
        String user_id = (String) payload.get(BasicLTIConstants.USER_ID);

        // Get the eid, either from the value provided or if trusted get it from the user_id,otherwise construct it.
        String eid = getEid(payload, trustedConsumer, user_id);


        // If we did not get first and last name, split lis_person_name_full
        final String fullname = (String) payload.get(BasicLTIConstants.LIS_PERSON_NAME_FULL);
        String fname = (String) payload.get(BasicLTIConstants.LIS_PERSON_NAME_GIVEN);
        String lname = (String) payload.get(BasicLTIConstants.LIS_PERSON_NAME_FAMILY);
        String email = (String) payload.get(BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY);

        if (fname == null && lname == null && fullname != null) {
            int ipos = fullname.trim().lastIndexOf(' ');
            if (ipos == -1) {
                fname = fullname;
            } else {
                fname = fullname.substring(0, ipos);
                lname = fullname.substring(ipos + 1);
            }
        }
        
        // If trusted consumer, login, otherwise check for existing user and create one if required
        // Note that if trusted, then the user must have already logged into Sakai in order to have an account stub created for them
        // otherwise this will fail since they don't exist. Perhaps this should be addressed?
        if (trustedConsumer) {
            try {
                user = UserDirectoryService.getUser(user_id);
            } catch (UserNotDefinedException e) {
                throw new LTIException("launch.user.invalid", "user_id=" + user_id, e);
            }

        } else {

            try {
                user = UserDirectoryService.getUserByEid(eid);
            } catch (Exception e) {
                if (M_log.isDebugEnabled()) {
                    M_log.debug(e.getLocalizedMessage(), e);
                }
                user = null;
            }

            if (user == null) {
                try {
                    String hiddenPW = IdManager.createUuid();
                    UserDirectoryService.addUser(null, eid, fname, lname, email, hiddenPW, "registered", null);
                    M_log.info("Created user=" + eid);
                    user = UserDirectoryService.getUserByEid(eid);
                } catch (Exception e) {
                    throw new LTIException("launch.create.user", "user_id=" + user_id, e);
                }
            }
            
            // BLTI-153. Set up user's language.
            String locale = (String) payload.get(BasicLTIConstants.LAUNCH_PRESENTATION_LOCALE);
            if(locale != null && locale.length() > 0) {
                try {
                    PreferencesEdit pe = null;
                    try {
                    	pe = PreferencesService.edit(user.getId());
                    } catch(IdUnusedException idue) {
                        pe = PreferencesService.add(user.getId());
                    }
                    
                    ResourcePropertiesEdit propsEdit = pe.getPropertiesEdit("sakai:resourceloader");
                    propsEdit.removeProperty(Preferences.FIELD_LOCALE);
                    propsEdit.addProperty(Preferences.FIELD_LOCALE,locale);
                    PreferencesService.commit(pe);
                } catch(Exception e) {
                    M_log.error("Failed to setup launcher's locale",e);
                }
            }

            // post the login event
            // eventTrackingService().post(eventTrackingService().newEvent(EVENT_LOGIN,
            // null, true));
        }
        return user;
    }

    private void loginUser(String ipAddress, User user) {
        Session sess = SessionManager.getCurrentSession();
        UsageSessionService.login(user.getId(), user.getEid(), ipAddress, null, UsageSessionService.EVENT_LOGIN_WS);
        sess.setUserId(user.getId());
        sess.setUserEid(user.getEid());
    }
    
    /**
     * BLTI-155. If Profile2 is installed, set the profile picture to the user_image url, if supplied.
     * 
     * @param payload The LTI launch parameters in a Map
     * @param user The provisioned user who MUST be already logged in.
     * @param isTrustedConsumer If this is true, do nothing as we assume that a local
     * 							user corresponding to the consumer user already exists
     */
    private void setupUserPicture(Map payload, User user, boolean isTrustedConsumer) {
    	
    	if(isTrustedConsumer) return;
    	
    	String imageUrl = (String) payload.get(BasicLTIConstants.USER_IMAGE);
    	        
    	if(imageUrl != null && imageUrl.length() > 0) {
    		M_log.debug("User image supplied by consumer: " + imageUrl);
    	        
    		if(saveOfficialImageUrlMethod != null && getPreferencesRecordForUserMethod != null && setUseOfficialImageMethod != null && savePreferencesRecordMethod != null) {
    			try {
    				saveOfficialImageUrlMethod.invoke(profileImageLogicObject, new Object[] {user.getId(), imageUrl});
    				Object prefs = getPreferencesRecordForUserMethod.invoke(profilePreferencesLogicObject, new String [] { user.getId() });
    				setUseOfficialImageMethod.invoke(prefs,new Object[] { true });
    				savePreferencesRecordMethod.invoke(profilePreferencesLogicObject,new Object[] { prefs });
    			} catch(Exception e) {
    				M_log.error("Failed to setup launcher's Profile2 picture.",e);
    			}
    		}
    	}
    }

    protected boolean isTrustedConsumer(Map payload) {
        boolean isTrustedConsumer = false;
        String oauth_consumer_key = (String) payload.get("oauth_consumer_key");

        final String trustedConsumersConfig = ServerConfigurationService
                .getString("basiclti.provider.highly.trusted.consumers", null);
        if(BasicLTIUtil.isNotBlank(trustedConsumersConfig)) {
            String[] trustedConsumers = trustedConsumersConfig.split(":");
            List<String> trustedConsumersList = Arrays.asList(trustedConsumers);

            if (trustedConsumersList.contains(oauth_consumer_key)) {
                isTrustedConsumer = true;
            }
        }

        if (M_log.isDebugEnabled()) {
            M_log.debug("Consumer=" + oauth_consumer_key);
            M_log.debug("Trusted=" + isTrustedConsumer);
        }
        return isTrustedConsumer;
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
}
