/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.site.util.SiteTypeUtil;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SiteAddParticipantHandler {

    private static final String STATE_ATTRIBUTE = SiteAddParticipantHandler.class.getName() + ".STATE";

    private final AuthzGroupService authzGroupService;
    private final CourseManagementService courseManagementService;
    private final ServerConfigurationService serverConfigurationService;
    private final SessionManager sessionManager;
    private final SiteService siteService;
    private final ToolManager toolManager;
    @Getter private final List<ParticipantMessage> messages = new ArrayList<>();
    private final ParticipantRealmUpdater participantRealmUpdater;
    private final ParticipantAccountParser participantAccountParser;

    private String csrfToken;
    private AuthzGroup realm;
    private List<Role> roles = new ArrayList<>();
    private Site site;
    private String siteId;
    private SiteTypeUtil siteTypeUtil;
    /** The tool session owns this serializable operation state for the current request. */
    private ParticipantWizardState wizardState = new ParticipantWizardState();

    public SiteAddParticipantHandler(AuthzGroupService authzGroupService,
            CourseManagementService courseManagementService, ServerConfigurationService serverConfigurationService,
            SessionManager sessionManager, SiteService siteService, ToolManager toolManager,
            ParticipantRealmUpdater participantRealmUpdater, ParticipantAccountParser participantAccountParser) {
        this.authzGroupService = authzGroupService;
        this.courseManagementService = courseManagementService;
        this.serverConfigurationService = serverConfigurationService;
        this.sessionManager = sessionManager;
        this.siteService = siteService;
        this.toolManager = toolManager;
        this.participantRealmUpdater = participantRealmUpdater;
        this.participantAccountParser = participantAccountParser;
    }

    public boolean canAddParticipant() {
		if (site == null) init();
		if (siteService.allowUpdateSiteMembership(site.getId())) return true;

		messages.add(new ParticipantMessage(
				"java.permeditsite",
				new Object[] {site.getTitle()},
				ParticipantMessage.Severity.ERROR));
		log.warn("User doesn't have permission to update members in site {}", site.getId());
		return false;
	}

	public String getCsrfToken() {
		return Optional.ofNullable(sessionManager.getCurrentSession().getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE))
				.map(Object::toString)
				.orElse("");
	}

	public List<Role> getRoles() {
		if (roles.isEmpty()) {
            init();
        }
		List<Role> sortedRoles = new ArrayList<>(roles);
		Collections.sort(sortedRoles);
        return sortedRoles;
	}

	/**
     * Initialize helper by getting the current site
     */
    public void init() {
        siteTypeUtil = new SiteTypeUtil(siteService, serverConfigurationService);

        if (site == null) {
			siteId = Optional.ofNullable(sessionManager.getCurrentToolSession().getAttribute(ParticipantConstants.HELPER_SITE_ID_ATTRIBUTE))
					.map(Object::toString)
					.orElseGet(() -> toolManager.getCurrentPlacement().getContext());
            try {
                site = siteService.getSite(siteId);
                realm = authzGroupService.getAuthzGroup(siteService.siteReference(siteId));
                roles = new ArrayList<>(SiteParticipantHelper.getAllowedRoles(site.getType(), realm.getRoles()));
            } catch (Exception e) {
				log.warn("could not find site [{}], {}", siteId, e);
            }
        }
	}

	public String getSiteTitle() {
		if (site == null) init();
		if (site != null) return site.getTitle();
		return "";
	}

    public boolean isCourseSite() {
    	return site != null && siteTypeUtil.isCourseSite(site.getType());
    }

	public String getServerConfigurationString(String param) {
		return getServerConfigurationString(param, null);
	}

	public String getServerConfigurationString(String param, String defaultValue) {
		return serverConfigurationService.getString(param, defaultValue);
	}

    /** Restores the current tool-session operation into this request-scoped handler. */
    public void beginStep() {
        wizardState = readState();
        init();
    }

    /** Starts a new operation when Site Info launches this helper at its root URL. */
    public void startNewOperation() {
        resetMessages();
        reset();
        clearState();
    }

    public boolean submitAdd(String csrfToken, String officialAccounts, String nonOfficialAccounts,
            ParticipantStatus selectedStatus) {
        csrfToken(csrfToken);
        wizardState.setOfficialAccountParticipant(officialAccounts);
        wizardState.setNonOfficialAccountParticipant(nonOfficialAccounts);
        if (selectedStatus == null) {
            resetMessages();
            messages.add(new ParticipantMessage("java.pleasechoose", null, ParticipantMessage.Severity.ERROR));
            saveState();
            return false;
        }
        wizardState.setStatus(selectedStatus);
        if (invalidCsrfToken()) {
            saveState();
            return false;
        }

        resetMessages();
        ParticipantAccountParser.Result result = participantAccountParser.parse(site,
                wizardState.getOfficialAccountParticipant(), wizardState.getOfficialAccountEidOnly(),
                wizardState.getNonOfficialAccountParticipant());
        wizardState.setOfficialAccountParticipant(result.officialAccounts());
        wizardState.setOfficialAccountEidOnly(result.officialAccountEidOnly());
        wizardState.setNonOfficialAccountParticipant(result.nonOfficialAccounts());
        wizardState.setUserRoleEntries(result.entries());
        messages.addAll(result.messages());
        saveState();
        return messages.isEmpty();
    }

    public boolean submitRoles(String csrfToken, ParticipantRoleMode selectedRoleMode, String selectedSameRole,
            List<String> individualRoles) {
        csrfToken(csrfToken);
        if (selectedRoleMode == null) {
            resetMessages();
            messages.add(new ParticipantMessage("java.roletype", null, ParticipantMessage.Severity.ERROR));
            saveState();
            return false;
        }

        wizardState.setRoleMode(selectedRoleMode);
        wizardState.setSameRoleChoice(selectedSameRole);
        boolean valid = ParticipantRoleMode.DIFFERENT_ROLE.equals(wizardState.getRoleMode())
                ? applyIndividualRoles(individualRoles)
                : applySameRole();
        saveState();
        return valid;
    }

    public void clearStepMessages() {
        resetMessages();
        saveState();
    }

    /** Cancels the operation and returns the helper's caller-provided exit URL. */
    public String cancel() {
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ParticipantConstants.ATTR_TOP_REFRESH, Boolean.TRUE);
        setNextPage(SiteConstants.SITE_INFO_TEMPLATE_INDEX);
        String doneUrl = getDoneUrl();
        resetMessages();
        reset();
        clearState();
        return doneUrl;
    }

	/** Utility method; sets the template index of the desired helper page. */
	private void setNextPage(String nextPageTemplateIndex) {
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(SiteConstants.STATE_TEMPLATE_INDEX, nextPageTemplateIndex);
    }

    private boolean invalidCsrfToken() {
		boolean invalid = !StringUtils.equals(csrfToken, getCsrfToken());
		if (invalid) {
		messages.add(new ParticipantMessage(
				"java.badcsrftoken",
				null,
				ParticipantMessage.Severity.ERROR));
		}
		return invalid;
    }

    private void resetMessages() {
		messages.clear();
    }
    
    private boolean applySameRole() {
        if (invalidCsrfToken()) return false;

        resetMessages();
        String sameRoleChoice = wizardState.getSameRoleChoice();
        if (!validateRoles(Collections.singleton(sameRoleChoice))) return false;

        List<UserRoleEntry> assignedEntries = wizardState.getUserRoleEntries().stream()
                .map(entry -> entry.withRole(sameRoleChoice))
                .collect(Collectors.toList());
        wizardState.setUserRoleEntries(assignedEntries);
        return true;
    }

    private boolean applyIndividualRoles(List<String> individualRoles) {
        if (invalidCsrfToken()) return false;

        resetMessages();
        if (individualRoles == null || individualRoles.size() != wizardState.getUserRoleEntries().size()) {
            messages.add(new ParticipantMessage("java.pleasechoose", null, ParticipantMessage.Severity.ERROR));
            return false;
        }
        if (!validateRoles(individualRoles)) return false;

        List<UserRoleEntry> assignedEntries = new ArrayList<>(wizardState.getUserRoleEntries());
        for (int i = 0; i < individualRoles.size(); i++) {
            assignedEntries.set(i, assignedEntries.get(i).withRole(individualRoles.get(i)));
        }
        wizardState.setUserRoleEntries(assignedEntries);

        return true;
    }

    private boolean validateRoles(Collection<String> roleNames) {
        if (realm == null) init();
        Set<String> allowedRoleNames = getRoles().stream().map(Role::getId).collect(Collectors.toSet());
        boolean mayUpdateRealm = authzGroupService.allowUpdate(siteService.siteReference(siteId));
        for (String roleName : roleNames) {
            ParticipantRolePolicy.Outcome outcome = ParticipantRolePolicy.evaluate(roleName, realm, allowedRoleNames,
                    mayUpdateRealm);
            if (outcome == ParticipantRolePolicy.Outcome.ALLOWED) {
                continue;
            }
            ParticipantMessage message = ParticipantRolePolicy.messageFor(outcome, roleName, true);
            if (message != null) {
                messages.add(message);
            }
            return false;
        }
        return true;
    }
    
    /** Adds the selected participants and preserves only rejected entries for correction. */
    public boolean finish(String csrfToken, ParticipantNotificationOption selectedNotificationOption) {
        csrfToken(csrfToken);
        if (selectedNotificationOption == null) {
            resetMessages();
            messages.add(new ParticipantMessage("java.pleasechoose", null, ParticipantMessage.Severity.ERROR));
            saveState();
            return false;
        }
        if (invalidCsrfToken()) {
            saveState();
            return false;
        }
        wizardState.setNotificationOption(selectedNotificationOption);

        resetMessages();
        if (site == null) init();
        ParticipantRealmUpdater.Result updateResult = participantRealmUpdater.addParticipants(site, roles,
                wizardState.getUserRoleEntries(), wizardState.getStatus(), wizardState.getNotificationOption());
        messages.addAll(updateResult.messages());

        if (updateResult.committed() && updateResult.rejectedEntries().isEmpty()) {
            reset();
            setNextPage(SiteConstants.MANAGE_PARTICIPANTS_TEMPLATE_INDEX);
            clearState();
            return true;
        }

        if (updateResult.committed()) {
            wizardState.setUserRoleEntries(new ArrayList<>(updateResult.rejectedEntries()));
            messages.add(new ParticipantMessage("java.allusers", null, ParticipantMessage.Severity.INFO));
        }
        saveState();
        return false;
    }

    /**
     * Gets the current tool
     * @return Tool
     */
    public Tool getCurrentTool() {
        return toolManager.getCurrentTool();
    }

    /** Return the caller-provided URL for leaving this Site Info helper. */
	public String getDoneUrl() {
        ToolSession session = sessionManager.getCurrentToolSession();
        if (session == null) {
            return "/";
        }

        String doneUrl = (String) session.getAttribute(ParticipantConstants.HELPER_TOOL_ID + Tool.HELPER_DONE_URL);
        if (StringUtils.isNotBlank(doneUrl)) {
            return doneUrl;
        }

        Tool tool = getCurrentTool();
        doneUrl = tool == null ? null : (String) session.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
        return StringUtils.defaultIfBlank(doneUrl, "/");
	}

    /** Saves the selected notification option when returning from the final review step. */
    public void saveNotificationChoice(String csrfToken, ParticipantNotificationOption selectedNotificationOption) {
        if (!StringUtils.equals(csrfToken, getCsrfToken()) || selectedNotificationOption == null) {
            return;
        }
        wizardState.setNotificationOption(selectedNotificationOption);
        saveState();
    }

    public boolean hasParticipants() {
        return !wizardState.getUserRoleEntries().isEmpty();
    }

    public ParticipantWizardSnapshot snapshot() {
        return ParticipantWizardSnapshot.from(wizardState);
    }

    public boolean allowsNonOfficialAccounts() {
        return "true".equalsIgnoreCase(getAllowNonOfficialAccount());
    }

    public boolean showsStatusChoice() {
        return "true".equalsIgnoreCase(getServerConfigurationString("activeInactiveUser", Boolean.FALSE.toString()));
    }

    public boolean showsCourseInstructions() {
        return isCourseSite() && !courseManagementService.getCurrentAcademicSessions().isEmpty();
    }

	private void reset() {
		site = null;
		siteId = null;
		realm = null;
		roles = new ArrayList<>();
		wizardState = new ParticipantWizardState();
	}

    private void csrfToken(String value) {
        csrfToken = value;
    }

    private ParticipantWizardState readState() {
        ToolSession session = sessionManager.getCurrentToolSession();
        Object state = session == null ? null : session.getAttribute(STATE_ATTRIBUTE);
        return state instanceof ParticipantWizardState ? ((ParticipantWizardState) state).copy()
                : new ParticipantWizardState();
    }

    private void saveState() {
        ToolSession session = sessionManager.getCurrentToolSession();
        if (session == null) return;

        session.setAttribute(STATE_ATTRIBUTE, wizardState.copy());
    }

    private void clearState() {
        ToolSession session = sessionManager.getCurrentToolSession();
        if (session != null) {
            session.removeAttribute(STATE_ATTRIBUTE);
        }
    }

	/**
	 * get the settings whether non official account users are allowed or not
	 * site-wide settings can override the system-wide settings
	 */
	public String getAllowNonOfficialAccount() {
		// get system setting first
    	String rv = getServerConfigurationString("nonOfficialAccount", "true");
    	
    	// get site property, if different, it overrides sakai.properties setting
    	if (site == null) {
			log.error("Could not get site and thus, site properties.");
    	} else {
    	    String allowThisSiteAddNonOfficialParticipant = site.getProperties().getProperty("nonOfficialAccount");
    	    log.debug("Site non-official allowed? nonOfficialAccount={}", allowThisSiteAddNonOfficialParticipant);
    	    if (allowThisSiteAddNonOfficialParticipant != null && !allowThisSiteAddNonOfficialParticipant.equalsIgnoreCase(rv)) {
    	        rv = allowThisSiteAddNonOfficialParticipant;
    	    }
    	}
    	
    	return rv;
	}
}
