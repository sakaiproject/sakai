/*
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *			 http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.plus.tool;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.plus.tool.exception.MissingSessionException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.exception.IdUnusedException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.context.MessageSource;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.plus.api.PlusService;
import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.model.ContextLog;
import org.sakaiproject.plus.api.model.Membership;
import org.sakaiproject.plus.api.repository.TenantRepository;
import org.sakaiproject.plus.api.repository.ContextRepository;
import org.sakaiproject.plus.api.repository.ContextLogRepository;
import org.sakaiproject.plus.api.repository.MembershipRepository;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MainController {

	// The number of days to retain log entries
	static int CONTEXT_LOG_EXPIRE = 14;

	protected static ResourceLoader rb = new ResourceLoader("Messages");

	@Resource
	private SessionManager sessionManager;

	@Resource
	private ToolManager toolManager;

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private ContextRepository contextRepository;

	@Autowired
	private ContextLogRepository contextLogRepository;

	@Autowired
	private MembershipRepository membershipRepository;

	@Autowired
	private LTIService ltiService;

	@Autowired
	private SiteService siteService;

	@Autowired
	private PlusService plusService;

	@Autowired
	private MessageSource messageSource;

	@GetMapping(value = {"/", "/index"})
	public String pageIndex(Model model, HttpServletRequest request) {

		contextLogRepository.deleteOlderThanDays(CONTEXT_LOG_EXPIRE);

		if ( isAdmin() ) return adminTenants(model, request);

		Placement placement = toolManager.getCurrentPlacement();
		String contextId = placement.getContext();
		if ( isInstructor(contextId) ) return contextDetail(model, contextId, request);

		return "notallow";
	}

	public String adminTenants(Model model, HttpServletRequest request) {

		if ( ! isAdmin() ) return "notallow";

		loadModel(model, request);
		Iterable<Tenant> tenants = tenantRepository.findAll();
		model.addAttribute("tenants", tenants);
		model.addAttribute("enabled",  plusService.enabled());
		return "index";
	}

	@GetMapping(value = {"/create"})
	public String createTenant(Model model, HttpServletRequest request) {

		if ( ! isAdmin() ) return "notallow";
		loadModel(model, request);

		if (!model.containsAttribute("tenant")) {
			Tenant tenant = new Tenant();
			model.addAttribute("tenant", tenant);
		}

		model.addAttribute("doUpdate", Boolean.FALSE);
		return "form";
	}

	@PostMapping("/tenant")
	public String submitForm(@ModelAttribute("tenant") Tenant tenant, RedirectAttributes redirectAttrs) {

		String oldTenantId = tenant.getId();
		if ( oldTenantId != null ) {
			Optional<Tenant> optTenant = tenantRepository.findById(oldTenantId);
			if ( ! optTenant.isPresent() ) return "notfound";

			Tenant editTenant = optTenant.get();
			editTenant.setTitle(tenant.getTitle());
			editTenant.setDescription(tenant.getDescription());
			editTenant.setIssuer(tenant.getIssuer());
			editTenant.setClientId(tenant.getClientId());
			editTenant.setDeploymentId(tenant.getDeploymentId());
			editTenant.setTrustEmail(tenant.getTrustEmail());
			editTenant.setSiteTemplate(tenant.getSiteTemplate());
			editTenant.setRealmTemplate(tenant.getRealmTemplate());
			editTenant.setInboundRoleMap(tenant.getInboundRoleMap());
			editTenant.setTimeZone(tenant.getTimeZone());
			editTenant.setAllowedTools(tenant.getAllowedTools());
			editTenant.setNewWindowTools(tenant.getNewWindowTools());
			editTenant.setVerbose(tenant.getVerbose());
			editTenant.setOidcAuth(tenant.getOidcAuth());
			editTenant.setOidcKeySet(tenant.getOidcKeySet());
			editTenant.setOidcToken(tenant.getOidcToken());
			editTenant.setOidcAudience(tenant.getOidcAudience());
			editTenant.setOidcRegistrationLock(tenant.getOidcRegistrationLock());
			try {
				tenantRepository.save(editTenant);
				log.info("Updating Plus Tenant id={}", oldTenantId);
			} catch(Exception e) {
				redirectAttrs.addFlashAttribute("flashError", rb.getString("plus.tool.error.save")+" "+e.getMessage());
				log.info("Error Updating Plus Tenant id={} {}", oldTenantId, e.getMessage());
				return "redirect:/";
			}
		} else {
			// Because of uniqueness constraint, put in a dummy ClientID
			if ( StringUtils.isBlank(tenant.getClientId()) ) {
				tenant.setClientId("TmpCID-"+Instant.now().toString());
			}
			try {
				tenantRepository.save(tenant);
				log.info("Created Plus Tenant id={}", tenant.getId());
			} catch(Exception e) {
				redirectAttrs.addFlashAttribute("flashError", rb.getString("plus.tool.error.save")+" "+e.getMessage());
				log.info("Error Creating Plus Tenant {}", e.getMessage());
				redirectAttrs.addFlashAttribute("tenant", tenant);
				return "redirect:/create";
			}
		}

		redirectAttrs.addFlashAttribute("flashSuccess", rb.getString("plus.tool.success.saved"));
		return "redirect:/";
	}

	@GetMapping(value = "/tenant/{tenantId}")
	public String tenantDetail(Model model, @PathVariable String tenantId, HttpServletRequest request) {

		if ( ! isAdmin() ) return "notallow";

		Optional<Tenant> optTenant = tenantRepository.findById(tenantId);
		if ( ! optTenant.isPresent() ) return "notfound";
		Tenant tenant = optTenant.get();

		loadModel(model, request);
		model.addAttribute("tenant", tenant);

		model.addAttribute("oidcKeySet", plusService.getOidcKeySet());
		model.addAttribute("oidcLogin", plusService.getOidcLogin(tenant));
		model.addAttribute("oidcLaunch", plusService.getOidcLaunch());

		// http://localhost:8080/plus/sakai/dynamic/123?unlock_token=42
		model.addAttribute("imsURL", plusService.getLTIDynamicRegistration(tenant));

		// https://dev1.sakaicloud.com/plus/sakai/canvas-config.json?guid=123456
		model.addAttribute("canvasURL", plusService.getCanvasConfig(tenant));
		return "tenant";
	}

	@GetMapping(value = "/edit/{tenantId}")
	public String tenantEdit(Model model, @PathVariable String tenantId, HttpServletRequest request) {

		if ( ! isAdmin() ) return "notallow";

		Optional<Tenant> optTenant = tenantRepository.findById(tenantId);
		if ( ! optTenant.isPresent() ) return "notfound";
		Tenant tenant = optTenant.get();

		loadModel(model, request);
		model.addAttribute("tenant", tenant);
		model.addAttribute("doUpdate", Boolean.TRUE);
		return "form";
	}

	@GetMapping(value = "/delete/{tenantId}")
	public String tenantDelete(Model model, @PathVariable String tenantId, HttpServletRequest request) {

		if ( ! isAdmin() ) return "notallow";

		Optional<Tenant> optTenant = tenantRepository.findById(tenantId);
		if ( ! optTenant.isPresent() ) return "notfound";
		Tenant tenant = optTenant.get();

		loadModel(model, request);
		model.addAttribute("tenant", tenant);
		return "delete";
	}

	@PostMapping(value = "/delete/{tenantId}")
	public String tenantDeletePost(Model model, @PathVariable String tenantId, RedirectAttributes redirectAttrs) {
		if ( ! isAdmin() ) return "notallow";

		Optional<Tenant> optTenant = tenantRepository.findById(tenantId);
		if ( ! optTenant.isPresent() ) return "notfound";

		log.info("Deleteing Plus Tenant id={}", tenantId);

		tenantRepository.deleteById(tenantId);
		redirectAttrs.addFlashAttribute("flashSuccess", rb.getString("plus.tool.success.deleted"));
		return "redirect:/";
	}

	@GetMapping(value = "/contexts/{tenantId}")
	public String contexts(Model model, @PathVariable String tenantId, HttpServletRequest request) {

		if ( ! isAdmin() ) return "notallow";
		loadModel(model, request);

		Optional<Tenant> optTenant = tenantRepository.findById(tenantId);
		if ( ! optTenant.isPresent() ) return "notfound";
		Tenant tenant = optTenant.get();

		model.addAttribute("tenant", tenant);

		List<Context> contexts = null;
		if ( tenant != null ) {
			contexts = contextRepository.findByTenant(tenant);
			model.addAttribute("contexts", contexts);
		}
		return "contexts";
	}

	@GetMapping(value = "/context/{contextId}")
	public String contextDetailAdmin(Model model, @PathVariable String contextId, HttpServletRequest request) {
		if ( ! isAdmin() ) return "notallow";
		return contextDetail(model, contextId, request);
	}

	@GetMapping(value = "/membership/{contextId}")
	public String membershipsDetail(Model model, @PathVariable String contextId, HttpServletRequest request) {

		Optional<Context> optContext = contextRepository.findById(contextId);
		if ( ! optContext.isPresent() ) return "notfound";
		Context context = optContext.get();

		int minutes = plusService.getInactiveExpireMinutes(context);
		List<Membership> current_memberships = plusService.getSiteUsersMinutesOld(context, minutes);

		loadModel(model, request);
		model.addAttribute("tenantId", context.getTenant().getId());
		model.addAttribute("context", context);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("memberships", current_memberships);

		return "membership";
	}

	@PostMapping(value = "/expire/{contextId}")
	public String membershipsDetail(Model model, @PathVariable String contextId, HttpServletRequest request, RedirectAttributes redirectAttrs) {

		Optional<Context> optContext = contextRepository.findById(contextId);
		if ( ! optContext.isPresent() ) return "notfound";

		Context context = optContext.get();
		int minutes = plusService.getInactiveExpireMinutes(context);
		List<Membership> deleted_memberships = plusService.removeSiteUsersMinutesOld(context, minutes);

		redirectAttrs.addFlashAttribute("flashSuccess", rb.getString("plus.tool.memberships.removed")+" "+deleted_memberships.size());
		return "redirect:/membership/" + contextId;
	}

	public String contextDetail(Model model, String contextId, HttpServletRequest request) {

		Optional<Context> optContext = contextRepository.findById(contextId);
		if ( ! optContext.isPresent() ) return "notfound";
		Context context = optContext.get();

		loadModel(model, request);
		model.addAttribute("tenantId", context.getTenant().getId());
		model.addAttribute("context", context);
		model.addAttribute("admin", isAdmin());

		List<ContextLog> failures = contextLogRepository.getLogEntries(context, Boolean.FALSE, 20);
		model.addAttribute("failures", failures);
		List<ContextLog> successes = contextLogRepository.getLogEntries(context, Boolean.TRUE, 20);
		model.addAttribute("successes", successes);

		return "context";
	}

	private void loadModel(Model model, HttpServletRequest request) {

		model.addAttribute("cdnQuery", PortalUtils.getCDNQuery());

		Placement placement = toolManager.getCurrentPlacement();
		model.addAttribute("siteId", placement.getContext());
		String baseUrl = "/portal/site/" + placement.getContext() + "/tool/" + toolManager.getCurrentPlacement().getId();
		model.addAttribute("baseUrl", baseUrl);
		String serverUrl = SakaiBLTIUtil.getOurServerUrl();
		model.addAttribute("serverUrl", serverUrl);
		model.addAttribute("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));
	}

	/**
	 * Check for a valid session
	 * if not valid a 403 Forbidden will be returned
	 */
	private Session getSakaiSession() {

		try {
			Session session = sessionManager.getCurrentSession();
			if (StringUtils.isBlank(session.getUserId())) {
				log.error("Sakai user session is invalid");
				throw new MissingSessionException();
			}
			return session;
		} catch (IllegalStateException e) {
			log.error("Could not retrieve the sakai session");
			throw new MissingSessionException(e.getCause());
		}
	}

	/**
	 * Check if this is an admin placement
	 */
	private boolean isAdmin() {
		getSakaiSession();
		Placement placement = toolManager.getCurrentPlacement();
		return ltiService.isAdmin(placement.getContext());
	}

	/**
	 * Check if this is an instructor in the site placement
	 */
	private boolean isInstructor(String contextId) {

		// Just to make sure.
		getSakaiSession();

		return siteService.allowUpdateSite(contextId);
	}

}
