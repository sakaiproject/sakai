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
 *			 http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.basiclti.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Properties;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.json.simple.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.lti.api.LTIExportService;
import org.sakaiproject.lti.api.LTIExportService.ExportType;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;

import org.sakaiproject.basiclti.LocalEventTrackingService;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;

import org.apache.commons.codec.binary.Base64;

@SuppressWarnings("deprecation")
@Slf4j
public class BasicLTISecurityServiceImpl implements EntityProducer {
	public static final String SERVICE_NAME = BasicLTISecurityServiceImpl.class.getName();

	private static ResourceLoader rb = new ResourceLoader("basicltisvc");
	
	@Setter private FormattedText formattedText;
	@Setter private SiteService siteService;
	@Setter private ToolManager toolManager;
	@Setter private SessionManager sessionManager;
	@Setter private EntityManager entityManager;
	@Setter private SecurityService securityService;

	public static final String MIME_TYPE_BLTI="ims/basiclti";
	public static final String REFERENCE_ROOT="/basiclti";
	public static final String APPLICATION_ID = "sakai:basiclti";
	public static final String TOOL_REGISTRATION = "sakai.basiclti";
	public static final String EVENT_BASICLTI_LAUNCH = "basiclti.launch";

	// Note: security needs a proper Resource reference

	/*******************************************************************************
	 * Dependencies and their setter methods
	 *******************************************************************************/

	/**
	 * Check security for this entity.
	 *
	 * @param ref
	 *		The Reference to the entity.
	 * @return true if allowed, false if not.
	 */
	protected boolean checkSecurity(Reference ref)
	{
		String contextId = ref.getContext();
		try
		{
			Site site = siteService.getSiteVisit(contextId);
			if ( site != null ) return true;
		}
		catch(IdUnusedException ex)
		{
			return false;
		}
		catch(PermissionException ex)
		{
			return false;
		}
		log.debug("ID={}", ref.getId());
		log.debug("Type={}", ref.getType());
		log.debug("SubType={}", ref.getSubType());

		return false;
	}
	/*******************************************************************************
	 * Init and Destroy
	 *******************************************************************************/
	/** A service */
	protected static LTIService ltiService = null;

	protected static LTIExportService ltiExportService;

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{


		log.info("{}.init()", this);

		if (ServerConfigurationService.getString(SakaiBLTIUtil.BASICLTI_ENCRYPTION_KEY, null) == null) {
			log.error("BasicLTI secrets in database unencrypted, please set {}", SakaiBLTIUtil.BASICLTI_ENCRYPTION_KEY);
		}
		try
		{
			// register as an entity producer
			entityManager.registerEntityProducer(this,REFERENCE_ROOT);
		}
		catch (Throwable t)
		{
			log.warn("init(): {}", t.getMessage());
		}
		if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		if ( ltiExportService == null ) ltiExportService = (LTIExportService)ComponentManager.get("org.sakaiproject.lti.api.LTIExportService");
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		log.info("{}.destroy()", this);
	}

	/**
	 *
	 */
	public BasicLTISecurityServiceImpl() {
		super();

	}


	public boolean isSuperUser(String userId)
	{
		return securityService.isSuperUser(userId);
	}


	/*******************************************************************************************************************************
	 * EntityProducer
	 ******************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 /access/basiclti/site/12-siteid-456/98-placement-id
	 /access/basiclti/content/ --- content path ---- (Future)
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT))
		{
			// we will get null, simplelti, site, <context>, <placement>
			// we will store the context, and the ContentHosting reference in our id field.
			String id = null;
			String context = null;
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			if ( parts.length == 5 && parts[2].equals("site") )
			{
				context = parts[3];
				id = parts[4];
				//Should the slashes below be entityseparator
				// id = "/" + StringUtil.unsplit(parts, 2, parts.length - 2, "/");
			}

			ref.set(APPLICATION_ID, "site", id, null, context);

			return true;
		}

		return false;
	}

	private void sendHTMLPage(HttpServletResponse res, String body)
	{
		try
		{
			res.setContentType("text/html; charset=UTF-8");
			res.setCharacterEncoding("utf-8");
			res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
			res.addDateHeader("Last-Modified", System.currentTimeMillis());
			res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
			res.addHeader("Pragma", "no-cache");
			java.io.PrintWriter out = res.getWriter();

			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
			out.println("<html>\n<head>");
			out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
			out.println("</head>\n<body>\n");
			out.println(body);
			out.println("\n</body>\n</html>");
		}
		catch (Exception e)
		{
			log.warn("Failed to send HTML page.", e);
		}

	}

	private void doSplash(HttpServletRequest req, HttpServletResponse res, String splash, ResourceLoader rb)
	{
		// req.getRequestURL()=http://localhost:8080/access/basiclti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
		// req.getRequestURI()=/access/basiclti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
		String acceptPath = req.getRequestURI().toString() + "?splash=bypass";
				String body = "<div align=\"center\" style=\"text-align:left;width:80%;margin-top:5px;margin-left:auto;margin-right:auto;border-width:1px 1px 1px 1px;border-style:solid;border-color: gray;padding:.5em;font-family:Verdana,Arial,Helvetica,sans-serif;font-size:.8em\">";
		body += splash+"</div><p>";
		String txt = rb.getString("launch.button", "Press to continue to external tool.");
		body += "<form><input type=\"submit\" onclick=\"window.location='"+acceptPath+"';return false;\" value=\"";
		body += rb.getString("launch.button", "Press to continue to proceed to external tool.");
		body += "\"></form></p>\n";
		sendHTMLPage(res, body);
	}

	/*
		iss required, the issuer identifier identifying the learning platform

		login_hint	required, Hint to the Authorization Server about the login
					identifier the End-User might use to log in (if necessary).
	*/
	private void redirectOIDC(HttpServletRequest req, HttpServletResponse res,
		Map<String, Object> content, Map<String, Object> tool, String oidc_endpoint, ResourceLoader rb)
	{
		// req.getRequestURL()=http://localhost:8080/access/basiclti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
		// req.getRequestURI()=/access/basiclti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
		String login_hint = req.getRequestURI();
		String query_string = req.getQueryString();

		if ( StringUtils.isNotEmpty(query_string)) {
			login_hint = login_hint + "?" + query_string;
		}
		String launch_url = StringUtils.trimToNull((String) tool.get(LTIService.LTI_LAUNCH));
		if ( content != null ) {
			String content_launch_url = StringUtils.trimToNull((String) content.get(LTIService.LTI_LAUNCH));
			if ( content_launch_url != null ) launch_url = content_launch_url;
		}

		byte[] bytesEncoded = Base64.encodeBase64(login_hint.getBytes());
		String encoded_login_hint = new String(bytesEncoded);
		try {
			URIBuilder redirect = new URIBuilder(oidc_endpoint.trim());
			redirect.addParameter("iss", SakaiBLTIUtil.getOurServerUrl());
			redirect.addParameter("login_hint", encoded_login_hint);
			if (StringUtils.isNotBlank(launch_url)) {
				redirect.addParameter("target_link_uri", launch_url);
			}
			res.sendRedirect(redirect.build().toString());
		} catch (IOException unlikely) {
			log.error("failed redirect {}", unlikely.getMessage());
		} catch (URISyntaxException e) {
			log.error("Syntax exception building the URL with the params: {}.", e.getMessage());
		}

	}

	/**
	 * Handle the LTI 1.1.2 round trip logic
	*/
	private void handleLTI112(HttpServletRequest req, HttpServletResponse res, Map<String, Object> tool)
	{
		String default_launch_type = ServerConfigurationService.getString(SakaiBLTIUtil.BASICLTI_LTI11_LAUNCH_TYPE,
				SakaiBLTIUtil.BASICLTI_LTI11_LAUNCH_TYPE_DEFAULT);
		Long lti11_launch_type = SakaiBLTIUtil.getLongKey(tool.get(LTIService.LTI11_LAUNCH_TYPE));

		if ( SakaiBLTIUtil.isLTI13(tool, null) ) return;

		if ( lti11_launch_type.equals(LTIService.LTI11_LAUNCH_TYPE_LEGACY) ) return;

		if ( lti11_launch_type.equals(LTIService.LTI11_LAUNCH_TYPE_LTI112) ||
					SakaiBLTIUtil.BASICLTI_LTI11_LAUNCH_TYPE_LTI112.equals(default_launch_type) ) {

			String tool_state = req.getParameter("tool_state");
			if ( StringUtils.isEmpty(tool_state) ) {
				// req.getRequestURL()=http://localhost:8080/access/basiclti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
				// req.getRequestURI()=/access/basiclti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
				String platform_state = req.getRequestURI();
				String query_string = req.getQueryString();
				if ( StringUtils.isNotEmpty(query_string) ) {
					platform_state = platform_state + "?" + query_string;
				}
				tool.put("platform_state", platform_state);
				String relaunch_url = SakaiBLTIUtil.getOurServerUrl() + "/imsoidc/lti13/lti112";
				tool.put("relaunch_url", relaunch_url);
			} else {
				tool.put("tool_state", tool_state);
			}
		}
	}

	/**
	 * Do some sanity checking on the aunch data to make sure we have enough to accomplish the launch
	 */
	private boolean sanityCheck(HttpServletRequest req, HttpServletResponse res,
		Map<String, Object> content, Map<String, Object> tool, ResourceLoader rb)
	{

		String oidc_endpoint = (String) tool.get(LTIService.LTI13_OIDC_ENDPOINT);
		if (SakaiBLTIUtil.isLTI13(tool, content) && StringUtils.isBlank(oidc_endpoint) ) {
			String errorMessage = "<p>" + SakaiBLTIUtil.getRB(rb, "error.no.oidc_endpoint", "Missing oidc_endpoint value for LTI 1.3 launch") + "</p>";
			sendHTMLPage(res, errorMessage);
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			@SuppressWarnings("unchecked")
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
					EntityAccessOverloadException, EntityCopyrightException
			{
				// decide on security
				if (!checkSecurity(ref))
				{
					throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "basiclti", ref.getReference());
				}

				String refId = ref.getId();
				String [] retval = null;
				if ( refId.startsWith("tool:") && refId.length() > 5 )
				{
					Map<String,Object> tool;

					String toolStr = refId.substring(5);
					String contentReturn = req.getParameter("contentReturn");
					Enumeration attrs =  req.getParameterNames();
					Properties propData = new Properties();
					while(attrs.hasMoreElements()) {
						String key = (String) attrs.nextElement();
						if ( "contentReturn".equals(key) ) continue;
						if ( key == null ) continue;
						String value = req.getParameter(key);
						if ( value == null ) continue;
						propData.setProperty(key,value);
					}
					Long toolKey = SakaiBLTIUtil.getLongKey(toolStr);
					if (toolKey < 1 ) {
						throw new EntityNotDefinedException("Could not load tool");
					}

					tool = ltiService.getToolDao(toolKey, ref.getContext());
					if (tool == null ) {
						throw new EntityNotDefinedException("Could not load tool");
					}

					// Save for LTI 13 Issuer
					String orig_site_id = StringUtils.trimToNull((String) tool.get(LTIService.LTI_SITE_ID));
					if ( orig_site_id == null ) {
						tool.put("orig_site_id_null", "true");
					}
					tool.put(LTIService.LTI_SITE_ID, ref.getContext());

					String state = req.getParameter("state");
					String nonce = req.getParameter("nonce");

					String oidc_endpoint = (String) tool.get(LTIService.LTI13_OIDC_ENDPOINT);
					log.debug("State={} nonce={} oidc_endpoint={}",state, nonce, oidc_endpoint);

					// Sanity check for missing config data
					if ( ! sanityCheck(req, res, null, tool, rb) ) return;

					if (SakaiBLTIUtil.isLTI13(tool, null) && StringUtils.isNotBlank(oidc_endpoint) &&
							( StringUtils.isEmpty(state) || StringUtils.isEmpty(state) ) ) {
						redirectOIDC(req, res, null, tool, oidc_endpoint, rb);
						return;
					}

					handleLTI112(req, res, tool);

					retval = SakaiBLTIUtil.postContentItemSelectionRequest(toolKey, tool, state, nonce, rb, contentReturn, propData);

				}
				else if ( refId.startsWith("content:") && refId.length() > 8 )
				{
					Map<String,Object> content;
					Map<String,Object> tool = null;

					String contentStr = refId.substring(8);
					Long contentKey = SakaiBLTIUtil.getLongKey(contentStr);
					if (contentKey < 1 ) {
						throw new EntityNotDefinedException("Could not load content item");
					}

					content = ltiService.getContentDao(contentKey,ref.getContext());
					if (content == null ) {
						throw new EntityNotDefinedException("Could not load content item");
					}

					// Check to see if we need launch protection
					int protect = SakaiBLTIUtil.getInt(content.get(LTIService.LTI_PROTECT));
					String launch_code_key = SakaiBLTIUtil.getLaunchCodeKey(content);
					Session session = sessionManager.getCurrentSession();

					// SAK-43709 - Prior to Sakai-21 there is no protect field in Content
					// If there is no protect value, we fall back to the pre-21 description in JSON
					if ( protect < 0 ) {
						String content_settings = (String) content.get(LTIService.LTI_SETTINGS);
						JSONObject content_json = org.tsugi.basiclti.BasicLTIUtil.parseJSONObject(content_settings);
						protect = SakaiBLTIUtil.getInt(content_json.get(LTIService.LTI_PROTECT));
					}

					if ( protect > 0 ) {
						String launch_code = (String) session.getAttribute(launch_code_key);

						// We don't remove the token until later because LTI 1.3 pass through this twice
						if ( launch_code == null || ! SakaiBLTIUtil.checkLaunchCode(content, launch_code) ) {
							throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "basiclti", ref.getReference());
						}
					}

					String siteId = (String) content.get(LTIService.LTI_SITE_ID);
					if ( siteId == null || ! siteId.equals(ref.getContext()) )
					{
						throw new EntityNotDefinedException("Incorrect site");
					}

					Long toolKey = SakaiBLTIUtil.getLongKey(content.get(LTIService.LTI_TOOL_ID));
					if ( toolKey >= 0 ) tool = ltiService.getToolDao(toolKey, ref.getContext());
					if ( tool != null )
					{
						// SITE_ID can be null for the tool
						siteId = (String) tool.get(LTIService.LTI_SITE_ID);
						if ( siteId != null && ! siteId.equals(ref.getContext()) )
						{
							tool = null;
						}
					}

					ltiService.filterContent(content, tool);

					String splash = null;
					if ( tool != null ) splash = (String) tool.get("splash");
					String splashParm = req.getParameter("splash");
					siteId = null;
					if ( tool != null ) siteId = (String) tool.get(LTIService.LTI_SITE_ID);
					if ( splashParm == null && splash != null && splash.trim().length() > 1 )
					{
							// XSS Note: Administrator-created tools can put HTML in the splash.
							if ( siteId != null ) splash = formattedText.escapeHtml(splash,false);
							doSplash(req, res, splash, rb);
							return;
					}
					String state = req.getParameter("state");
					String nonce = req.getParameter("nonce");

					if ( tool != null ) {
						String oidc_endpoint = (String) tool.get(LTIService.LTI13_OIDC_ENDPOINT);
						log.debug("State={} nonce={} oidc_endpoint={}",state, nonce, oidc_endpoint);

						// Sanity check for missing config data
						if ( ! sanityCheck(req, res, content, tool, rb) ) return;

						if (SakaiBLTIUtil.isLTI13(tool, content) && StringUtils.isNotBlank(oidc_endpoint) &&
								(StringUtils.isEmpty(state) || StringUtils.isEmpty(nonce) ) ) {
							redirectOIDC(req, res, content, tool, oidc_endpoint, rb);
							return;
						}
					}

					if ( tool != null ) handleLTI112(req, res, tool);

					retval = SakaiBLTIUtil.postLaunchHTML(content, tool, state, nonce, ltiService, rb);

					// Once we are ready to do the actual launch, remove the assignments protection key
					session.removeAttribute(launch_code_key);  // You get one try
				}
				else if (refId.startsWith("export:") && refId.length() > 7)
				{
					final String[] tokens = refId.split(":");
					try
					{
						ExportType exportType = ExportType.valueOf(tokens[1]);

						String filterId = null;
						if (tokens.length == 3)
						{
							filterId = tokens[2];
						}
						if (exportType == ExportType.CSV)
						{
							res.setContentType("text/csv");
							res.setHeader("Content-Disposition", "attachment; filename = export_tool_links.csv");
						}
						if (exportType == ExportType.EXCEL)
						{
							res.setContentType("application/vnd.ms-excel");
							res.setHeader("Content-Disposition", "attachment; filename = export_tool_links.xls");
						}
						OutputStream out = null;
						try
						{
							out = (OutputStream)res.getOutputStream();
							ltiExportService.export(out, ref.getContext(), exportType, filterId);
						}
						catch(Exception ignore)
						{
							log.warn(": lti export {}", ignore.getMessage());
						}
						finally
						{
							if (out != null)
							{
								try
								{
									out.flush();
									out.close();
								}
								catch (Throwable ignore)
								{
									log.warn(": lti export {}", ignore.getMessage());
								}
							}
						}
					}
					catch (java.lang.IllegalArgumentException ex)
					{
						log.warn(": lti export invalid export type", ex);
					}
				}
				else
				{
					String splashParm = req.getParameter("splash");
					if ( splashParm == null )
					{
						ToolConfiguration placement = siteService.findTool(refId);
						Properties config = placement == null ? null : placement.getConfig();

						if ( placement != null )
						{
							// XSS Note: Only the Administrator can set overridesplash - so we allow HTML
							String splash = SakaiBLTIUtil.toNull(SakaiBLTIUtil.getCorrectProperty(config,"overridesplash", placement));
							String send_session = SakaiBLTIUtil.toNull(SakaiBLTIUtil.getCorrectProperty(config,"ext_sakai_encrypted_session", placement));
							if ( splash == null && send_session != null && send_session.equals("true") && ! securityService.isSuperUser() )
							{
								splash = rb.getString("session.warning", "<p><span style=\"color:red\">Warning:</span> This tool makes use of your logged in session.  This means that the tool can access your data in this system.  Only continue to this tool if you are willing to share your data with this tool.</p>");
							}
							if ( splash == null )
							{
								// This may be user-set so no HTML
								splash = SakaiBLTIUtil.toNull(SakaiBLTIUtil.getCorrectProperty(config,"splash", placement));
								if ( splash != null ) splash = formattedText.escapeHtml(splash,false);
							}

							// XSS Note: Only the Administrator can set defaultsplash - so we allow HTML
							if ( splash == null )
							{
								splash = SakaiBLTIUtil.toNull(SakaiBLTIUtil.getCorrectProperty(config,"defaultsplash", placement));
							}

							if ( splash != null && splash.trim().length() > 1 )
							{
								doSplash(req, res, splash, rb);
								return;
							}
						}
					}

					// Get the post data for the placement
					retval = SakaiBLTIUtil.postLaunchHTML(refId, rb);
				}

				try
				{
					if (retval != null) {
						sendHTMLPage(res, retval[0]);
					}
					String refstring = ref.getReference();
					if ( retval != null && retval.length > 1 ) refstring = retval[1];
					Event event = LocalEventTrackingService.newEvent(EVENT_BASICLTI_LAUNCH, refstring, ref.getContext(),  false, NotificationService.NOTI_OPTIONAL);
					// SAK-24069 - Extend Sakai session lifetime on LTI tool launch
					Session session = sessionManager.getCurrentSession();
					if (session !=null) {
						int seconds = ServerConfigurationService.getInt(SakaiBLTIUtil.BASICLTI_LAUNCH_SESSION_TIMEOUT, 10800);
						if ( seconds != 0 ) session.setMaxInactiveInterval(seconds);
					}

					LocalEventTrackingService.post(event);
				}
				catch (Exception e)
				{
					log.warn("Failed to track event.", e);
				}

			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<String> getEntityAuthzGroups(Reference ref, String userId)
	{
		// Since we handle security ourself, we won't support anyone else asking
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return ServerConfigurationService.getAccessUrl() + ref.getReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "basiclti";
	}

	public boolean willArchiveMerge()
	{
		return true;
	}

	@SuppressWarnings("unchecked")
		public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
				Set userListAllowImport)
		{
			StringBuilder results = new StringBuilder("Merging BasicLTI ");
			org.w3c.dom.NodeList nodeList = root.getElementsByTagName("basicLTI");

			try {
				Site site = siteService.getSite(siteId);

				for(int i=0; i < nodeList.getLength(); i++)
				{
					BasicLTIArchiveBean basicLTI = new BasicLTIArchiveBean(nodeList.item(i));
					log.info("BASIC LTI: {}", basicLTI);
					results.append(", merging basicLTI tool " + basicLTI.getPageTitle());

					SitePage sitePage = site.addPage();
					sitePage.setTitle(basicLTI.getPageTitle());
					// This property affects both the Tool and SitePage.
					sitePage.setTitleCustom(true);

					ToolConfiguration toolConfiguration = sitePage.addTool();
					toolConfiguration.setTool(TOOL_REGISTRATION, toolManager.getTool(TOOL_REGISTRATION));
					toolConfiguration.setTitle(basicLTI.getToolTitle());

					for(Object key: basicLTI.getSiteToolProperties().keySet())
					{
						toolConfiguration.getPlacementConfig().setProperty((String)key, (String)basicLTI.getSiteToolProperties().get(key));
					}

					siteService.save(site);
				}
			} catch (Exception e) {
				log.warn("Failed to merge site: {}, error: {}", siteId, e);
			}

			results.append(".");
			return results.toString();
		}

	@SuppressWarnings("unchecked")
		public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
		{
			log.info("-------basic-lti-------- archive('{}')", StringUtils.join(new Object[] { siteId, doc, stack, archivePath, attachments }, "','"));

			StringBuilder results = new StringBuilder("archiving basiclti "+siteId+"\n");

			int count = 0;
			try {
				Site site = siteService.getSite(siteId);
				log.info("SITE: {} : {}", site.getId(), site.getTitle());
				Element basicLtiList = doc.createElement("org.sakaiproject.basiclti.service.BasicLTISecurityService");

				for (SitePage sitePage : site.getPages()) {
					for (ToolConfiguration toolConfiguration : sitePage.getTools()) {
						if ( toolConfiguration.getTool() == null ) continue;
						if (toolConfiguration.getTool().getId().equals(
							TOOL_REGISTRATION)) {
							// results.append(" tool=" + toolConfiguration.getId() + "\n");
							count++;

							BasicLTIArchiveBean basicLTIArchiveBean = new BasicLTIArchiveBean();
							basicLTIArchiveBean.setPageTitle(sitePage.getTitle());
							basicLTIArchiveBean.setToolTitle(toolConfiguration.getTitle());
							basicLTIArchiveBean.setSiteToolProperties(toolConfiguration.getConfig());

							Node newNode = basicLTIArchiveBean.toNode(doc);
							basicLtiList.appendChild(newNode);
						}
					}
				}

				((Element) stack.peek()).appendChild(basicLtiList);
				stack.push(basicLtiList);
				stack.pop();
			}
			catch (IdUnusedException iue) {
				log.info("SITE ID {} DOES NOT EXIST.", siteId);
				results.append("Basic LTI Site does not exist\n");
			}
			// Something we did not expect
			catch (Exception e) {
				log.warn("Failed to archive: {}, error: {}", siteId, e);
				results.append("basiclti exception:"+e.getClass().getName()+"\n");
			}
			results.append("archiving basiclti ("+count+") tools archived\n");

			return results.toString();
		}
}
