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

package org.sakaiproject.lti.impl;

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
import org.apache.commons.lang3.math.NumberUtils;
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
import org.sakaiproject.util.IframeUrlUtil;
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

import org.sakaiproject.lti.LocalEventTrackingService;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.lti13.util.SakaiLineItem;
import org.sakaiproject.lti13.LineItemUtil;

import org.tsugi.lti.LTIUtil;

import org.apache.commons.codec.binary.Base64;
import org.tsugi.util.Base64DoubleUrlEncodeSafe;

@SuppressWarnings("deprecation")
@Slf4j
public class LTISecurityServiceImpl implements EntityProducer {
	public static final String SERVICE_NAME = LTISecurityServiceImpl.class.getName();

	private static ResourceLoader rb = new ResourceLoader("basicltisvc");
	
	@Setter private FormattedText formattedText;
	@Setter private SiteService siteService;
	@Setter private ToolManager toolManager;
	@Setter private SessionManager sessionManager;
	@Setter private EntityManager entityManager;
	@Setter private SecurityService securityService;

	public static final String REFERENCE_ROOT="/lti";
	public static final String REFERENCE_ROOT_LEGACY="/basiclti";
	public static final String APPLICATION_ID = "sakai:basiclti";
	public static final String TOOL_REGISTRATION = "sakai.basiclti";
	public static final String EVENT_LTI_LAUNCH = "basiclti.launch";

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

	/**
	 * Check if the current user can update the site associated with this entity
	 *
	 * @param ref
	 *		The Reference to the entity.
	 * @return true if allowed, false if not.
	 */
	protected boolean checkSiteUpdate(Reference ref)
	{
		String contextId = ref.getContext();
		return siteService.allowUpdateSite(contextId);
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

		if (ServerConfigurationService.getString(SakaiLTIUtil.LTI_ENCRYPTION_KEY, null) == null) {
			log.error("LTI secrets in database unencrypted, please set {}", SakaiLTIUtil.LTI_ENCRYPTION_KEY);
		}
		try
		{
			// register as an entity producer
			entityManager.registerEntityProducer(this,REFERENCE_ROOT);
			entityManager.registerEntityProducer(this,REFERENCE_ROOT_LEGACY);
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
	public LTISecurityServiceImpl() {
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
	 /access/lti/site/12-siteid-456/98-placement-id
	 /access/lti/content/ --- content path ---- (Future)
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT) || reference.startsWith(REFERENCE_ROOT_LEGACY) )
		{
			// we will get null, lti, site, <context>, <placement>
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

	private void doSplash(HttpServletRequest req, HttpServletResponse res, String splash, ResourceLoader rb)
	{
		// req.getRequestURL()=http://localhost:8080/access/lti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
		// req.getRequestURI()=/access/lti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
		String acceptPath = req.getRequestURI().toString() + "?splash=bypass";
		String body = "<div align=\"center\" style=\"text-align:left;width:80%;margin-top:5px;margin-left:auto;margin-right:auto;border-width:1px 1px 1px 1px;border-style:solid;border-color: gray;padding:.5em;font-family:Verdana,Arial,Helvetica,sans-serif;font-size:.8em\">";
		String txt = rb.getString("launch.button", "Press to continue to external tool.");
		body += "<form><input type=\"submit\" onclick=\"window.location='"+acceptPath+"';return false;\" value=\"";
		body += rb.getString("launch.button", "Press to continue to proceed to external tool.");
		body += "\"></form></p>\n";
		body += splash+"</div><p>";
		org.tsugi.lti.LTIUtil.sendHTMLPage(res, body);
	}

	/**
	 * Returns JavaScript to add or remove sakai-iframe-force-light based on destination.
	 */
	private String forceLightJsSnippet(boolean addClass) {
		String op = addClass ? "add" : "remove";
		return "try { if (window.frameElement) window.frameElement.classList." + op + "('sakai-iframe-force-light'); } catch(e) { }";
	}

	// Do a redirect in HTML + JavaScript instead of with a 302 so we have some recovery options inside an iframe
	private void doRedirect(HttpServletRequest req, HttpServletResponse res, String redirectUrl, ResourceLoader rb)
	{
		Integer hash = redirectUrl.hashCode();

		StringBuilder body = new StringBuilder();
		body.append("<div style=\"padding:.5em;font-family:Verdana,Arial,Helvetica,sans-serif;\">\n");
		body.append("<p id=\"lti-message-"+hash+"\" style=\"display: none;\">");
		body.append(rb.getString("error.redirect.timeout", "Having problems reaching remote tool"));
		body.append("</p>\n");

		// Add or remove force-light based on redirect destination
		boolean forceLight = !IframeUrlUtil.isLocalToSakai(redirectUrl, ServerConfigurationService.getServerUrl());
		String forceLightJs = forceLightJsSnippet(forceLight);

		// We give this three chances - try to submit right away - submit 1/2 second from now and show the link 5 seconds from now
		body.append("<script>\n");
		body.append(forceLightJs);
		body.append("parent.postMessage('{ \"subject\": \"org.sakailms.lti.prelaunch\" }', '*');console.log('access.doRedirect prelaunch request');");
		body.append("setTimeout(function() {document.getElementById('lti-message-"+hash+"').style.display='block';}, 5000);\n");
		body.append("setTimeout(function() {window.location='"+redirectUrl+"';}, 500);\n");
		body.append("window.location='"+redirectUrl+"';\n");
		body.append("</script>\n");
		body.append("</div>");
		org.tsugi.lti.LTIUtil.sendHTMLPage(res, body.toString());
	}

	/*
		iss required, the issuer identifier identifying the learning platform

		login_hint	required, Hint to the Authorization Server about the login
					identifier the End-User might use to log in (if necessary).
	*/
	private void redirectOIDC(HttpServletRequest req, HttpServletResponse res,
		Map<String, Object> content, Map<String, Object> tool, String oidc_endpoint, ResourceLoader rb)
	{
		// req.getRequestURL()=http://localhost:8080/access/lti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
		// req.getRequestURI()=/access/lti/site/85fd092b-1755-4aa9-8abc-e6549527dce0/content:0
		String login_hint = req.getRequestURI();
		String query_string = req.getQueryString();
		String messageTypeParm = req.getParameter(SakaiLTIUtil.MESSAGE_TYPE_PARAMETER);

		if ( StringUtils.isNotEmpty(query_string)) {
			login_hint = login_hint + "?" + query_string;
		}


		String launch_url = StringUtils.trimToNull((String) tool.get(LTIService.LTI_LAUNCH));
		if ( content != null ) {
			String content_launch_url = StringUtils.trimToNull((String) content.get(LTIService.LTI_LAUNCH));
			if ( content_launch_url != null ) launch_url = content_launch_url;

			// See if we have a lineItem associated with this launch in case we need it later
			String lineItemStr = (String) content.get(LTIService.LTI_LINEITEM);
			SakaiLineItem sakaiLineItem = LineItemUtil.parseLineItem(lineItemStr);

			if ( SakaiLTIUtil.MESSAGE_TYPE_PARAMETER_CONTENT_REVIEW.equals(messageTypeParm)) {
				if ( sakaiLineItem != null && sakaiLineItem.submissionReview != null && StringUtils.isNotEmpty(sakaiLineItem.submissionReview.url) ) launch_url = sakaiLineItem.submissionReview.url;
			}
		}

		String client_id = StringUtils.trimToNull((String) tool.get(LTIService.LTI13_CLIENT_ID));
		String deployment_id = ServerConfigurationService.getString(SakaiLTIUtil.LTI13_DEPLOYMENT_ID, SakaiLTIUtil.LTI13_DEPLOYMENT_ID_DEFAULT);

		// Use Base64DoubleUrlEncodeSafe to ensure proper URL-safe encoding
		String encoded_login_hint = Base64DoubleUrlEncodeSafe.encode(login_hint);
		
		try {
			URIBuilder redirect = new URIBuilder(oidc_endpoint.trim());
			redirect.addParameter("iss", SakaiLTIUtil.getOurServerUrl());
			redirect.addParameter("login_hint", encoded_login_hint);
			redirect.addParameter("lti_storage_target", "_parent");
			if ( client_id != null ) redirect.addParameter("client_id", client_id);
			if ( deployment_id != null ) redirect.addParameter("lti_deployment_id", deployment_id);
			SakaiLTIUtil.addSakaiBaseCapabilities(redirect);
			if (StringUtils.isNotBlank(launch_url)) {
				redirect.addParameter("target_link_uri", launch_url);
			}
			doRedirect(req, res, redirect.build().toString(), rb);
		} catch (URISyntaxException e) {
			log.error("Syntax exception building the URL with the params: {}.", e.getMessage());
		}

	}

	/**
	 * Do some sanity checking on the aunch data to make sure we have enough to accomplish the launch
	 */
	private boolean sanityCheck(HttpServletRequest req, HttpServletResponse res,
		Map<String, Object> content, Map<String, Object> tool, ResourceLoader rb)
	{

		String oidc_endpoint = (String) tool.get(LTIService.LTI13_TOOL_ENDPOINT);
		if (SakaiLTIUtil.isLTI13(tool) && StringUtils.isBlank(oidc_endpoint) ) {
			String errorMessage = "<p>" + SakaiLTIUtil.getRB(rb, "error.no.oidc_endpoint", "Missing oidc_endpoint value for LTI 1.3 launch") + "</p>";
			org.tsugi.lti.LTIUtil.sendHTMLPage(res, errorMessage);
			return false;
		}

		return true;
	}

	@Override
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
					Long toolKey = LTIUtil.toLongKey(toolStr);
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

					String oidc_endpoint = (String) tool.get(LTIService.LTI13_TOOL_ENDPOINT);
					log.debug("State={} nonce={} oidc_endpoint={}",state, nonce, oidc_endpoint);

					// Sanity check for missing config data
					if ( ! sanityCheck(req, res, null, tool, rb) ) return;

					if (SakaiLTIUtil.isLTI13(tool) && StringUtils.isNotBlank(oidc_endpoint) &&
							( StringUtils.isEmpty(state) || StringUtils.isEmpty(state) ) ) {
						redirectOIDC(req, res, null, tool, oidc_endpoint, rb);
						return;
					}

					retval = SakaiLTIUtil.postContentItemSelectionRequest(toolKey, tool, state, nonce, rb, contentReturn, propData);

				}
				else if ( refId.startsWith("content:") && refId.length() > 8 )
				{
					Map<String,Object> content;
					Map<String,Object> tool = null;

					String contentStr = refId.substring(8);
					Long contentKey = LTIUtil.toLongKey(contentStr);
					if (contentKey < 1 ) {
						throw new EntityNotDefinedException("Could not load content item");
					}

					content = ltiService.getContentDao(contentKey,ref.getContext());
					if (content == null ) {
						throw new EntityNotDefinedException("Could not load content item");
					}

					// Check to see if we need launch protection
					int protect = LTIUtil.toInt(content.get(LTIService.LTI_PROTECT));
					String launch_code_key = SakaiLTIUtil.getLaunchCodeKey(content);
					Session session = sessionManager.getCurrentSession();

					// SAK-43709 - Prior to Sakai-21 there is no protect field in Content
					// If there is no protect value, we fall back to the pre-21 description in JSON
					if ( protect < 0 ) {
						String content_settings = (String) content.get(LTIService.LTI_SETTINGS);
						JSONObject content_json = org.tsugi.lti.LTIUtil.parseJSONObject(content_settings);
						protect = LTIUtil.toInt(content_json.get(LTIService.LTI_PROTECT));
					}

					if ( protect > 0 && ! checkSiteUpdate(ref) ) {
						String launch_code = (String) session.getAttribute(launch_code_key);

						// We don't remove the token until later because LTI 1.3 pass through this twice
						if ( launch_code == null || ! SakaiLTIUtil.checkLaunchCode(content, launch_code) ) {
							throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "basiclti", ref.getReference());
						}
					}

					String siteId = (String) content.get(LTIService.LTI_SITE_ID);
					if ( siteId == null || ! siteId.equals(ref.getContext()) )
					{
						throw new EntityNotDefinedException("Incorrect site");
					}

					Long toolKey = LTIUtil.toLongKey(content.get(LTIService.LTI_TOOL_ID));
					if ( toolKey >= 0 ) tool = ltiService.getTool(toolKey, ref.getContext());

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
						String oidc_endpoint = (String) tool.get(LTIService.LTI13_TOOL_ENDPOINT);
						log.debug("State={} nonce={} oidc_endpoint={}",state, nonce, oidc_endpoint);

						// Sanity check for missing config data
						if ( ! sanityCheck(req, res, content, tool, rb) ) return;

						if (SakaiLTIUtil.isLTI13(tool) && StringUtils.isNotBlank(oidc_endpoint) &&
								(StringUtils.isEmpty(state) || StringUtils.isEmpty(nonce) ) ) {
							redirectOIDC(req, res, content, tool, oidc_endpoint, rb);
							return;
						}
					}

					retval = SakaiLTIUtil.postLaunchHTML(content, tool, state, nonce, ltiService, rb);

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
							String splash = StringUtils.trimToNull(SakaiLTIUtil.getCorrectProperty(config,"overridesplash", placement));
							if ( splash == null )
							{
								// This may be user-set so no HTML
								splash = StringUtils.trimToNull(SakaiLTIUtil.getCorrectProperty(config,"splash", placement));
								if ( splash != null ) splash = formattedText.escapeHtml(splash,false);
							}

							// XSS Note: Only the Administrator can set defaultsplash - so we allow HTML
							if ( splash == null )
							{
								splash = StringUtils.trimToNull(SakaiLTIUtil.getCorrectProperty(config,"defaultsplash", placement));
							}

							if ( splash != null && splash.trim().length() > 1 )
							{
								doSplash(req, res, splash, rb);
								return;
							}
						}
					}

					// Get the post data for the placement
					retval = SakaiLTIUtil.postLaunchHTML(refId, rb);
				}

				try
				{
					if (retval != null) {
						String launchUrl = (retval.length > 1) ? (String) retval[1] : null;
						boolean forceLight = launchUrl != null && !IframeUrlUtil.isLocalToSakai(launchUrl, ServerConfigurationService.getServerUrl());
						String forceLightScript = "<script>" + forceLightJsSnippet(forceLight) + "</script>";
						org.tsugi.lti.LTIUtil.sendHTMLPage(res, forceLightScript + retval[0]);
					}
					String refstring = ref.getReference();
					if ( retval != null && retval.length > 1 ) refstring = retval[1];
					Event event = LocalEventTrackingService.newEvent(EVENT_LTI_LAUNCH, refstring, ref.getContext(),  false, NotificationService.NOTI_OPTIONAL);
					// SAK-24069 - Extend Sakai session lifetime on LTI tool launch
					Session session = sessionManager.getCurrentSession();
					if (session !=null) {
						int seconds = ServerConfigurationService.getInt(SakaiLTIUtil.LTI_LAUNCH_SESSION_TIMEOUT, 10800);
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

	@Override
	public String getEntityUrl(Reference ref)
	{
		return ServerConfigurationService.getAccessUrl() + ref.getReference();
	}

	@Override
	public String getLabel()
	{
		return "basiclti";
	}

	@Override
	public boolean willArchiveMerge()
	{
		return true;
	}

	@SuppressWarnings("unchecked")
		public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
				Set userListAllowImport)
		{
			StringBuilder results = new StringBuilder("Merging LTI ");
			org.w3c.dom.NodeList nodeList = root.getElementsByTagName("basicLTI");

			try {
				Site site = siteService.getSite(siteId);

				for(int i=0; i < nodeList.getLength(); i++)
				{
					LTIArchiveBean basicLTI = new LTIArchiveBean(nodeList.item(i));
					log.debug("LTI: {}", basicLTI);
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

		/**
		 * This archive includes several versions of the LTI content items and tools
		 */
		@SuppressWarnings("unchecked")
		public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
		{
			log.debug("-------basic-lti-------- archive('{}, {}, {}, {}, {}')", siteId, doc, stack, archivePath, attachments);

			StringBuilder results = new StringBuilder("archiving basiclti "+siteId+"\n");

			int count = 0;
			int contentCount = 0;
			try {
				Site site = siteService.getSite(siteId);
				log.debug("SITE: {} : {}", site.getId(), site.getTitle());
				Element basicLtiList = doc.createElement("org.sakaiproject.lti.service.LTISecurityService");

				// Export the LTI tools (legacy)
				for (SitePage sitePage : site.getPages()) {
					for (ToolConfiguration toolConfiguration : sitePage.getTools()) {
						if ( toolConfiguration.getTool() == null ) continue;
						if (toolConfiguration.getTool().getId().equals(
							TOOL_REGISTRATION)) {
							// results.append(" tool=" + toolConfiguration.getId() + "\n");
							count++;

							LTIArchiveBean basicLTIArchiveBean = new LTIArchiveBean();
							basicLTIArchiveBean.setPageTitle(sitePage.getTitle());
							basicLTIArchiveBean.setToolTitle(toolConfiguration.getTitle());
							basicLTIArchiveBean.setSiteToolProperties(toolConfiguration.getConfig());

							Node newNode = basicLTIArchiveBean.toNode(doc);
							basicLtiList.appendChild(newNode);
						}
					}
				}

				// Export the LTI Content Items
				List<Map<String,Object>> contents = ltiService.getContentsDao(null, null, 0, 0, siteId, false);
				for (Map<String,Object> contentItem : contents) {
					// Legacy circa 2022
					LTIContentArchiveBean ltiContentArchiveBean = new LTIContentArchiveBean(contentItem);
					Node newNode = ltiContentArchiveBean.toNode(doc);
					basicLtiList.appendChild(newNode);

					// Modern (Sakai-25 style)
					Long contentKey = ltiService.getId(contentItem);
					if ( contentKey > 0 ) {
						Element contentElement = ltiService.archiveContentByKey(doc, contentKey, siteId);
						if ( contentElement != null ) basicLtiList.appendChild(contentElement);
					}

					contentCount++;
				}

				// Finish
				((Element) stack.peek()).appendChild(basicLtiList);
				stack.push(basicLtiList);
				stack.pop();
			}
			catch (IdUnusedException iue) {
				log.info("SITE ID {} DOES NOT EXIST.", siteId);
				results.append("LTI Site does not exist\n");
			}
			// Something we did not expect
			catch (Exception e) {
				log.warn("Failed to archive: {}, error: {}", siteId, e);
				results.append("basiclti exception:"+e.getClass().getName()+"\n");
			}
			results.append("archiving basiclti: "+count+" tools and " + contentCount + " content items archived\n");

			return results.toString();
		}
}
