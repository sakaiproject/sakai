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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.accountvalidator.api.service.AccountValidationService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.site.util.SiteTypeUtil;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.api.PasswordFactory;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SiteAddParticipantHandler {

    public static final String EMAIL_CHAR = "@";
    public static final String HELPER_ID = "sakai.tool.helper.id";
    private static final String HELPER_TOOL_ID = "sakai-site-manage-participant-helper";
    public static final String SAK_PROP_INVALID_EMAIL_DOMAINS = "invalidEmailInIdAccountString";
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh";
    private static final String STATE_ATTRIBUTE = SiteAddParticipantHandler.class.getName() + ".STATE";

    @Setter private AuthzGroupService authzGroupService;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private PasswordFactory passwordFactory;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;
    @Setter private ToolManager toolManager;
    @Getter private final List<ParticipantMessage> messages = new ArrayList<>();
    @Setter private UserAuditRegistration userAuditRegistration;
    @Setter private UserAuditService userAuditService;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private CourseManagementService courseManagementService;
    @Setter private ParticipantAccountParser participantAccountParser;
    @Setter private ParticipantRealmUpdater participantRealmUpdater;

    private String csrfToken;
    private ParticipantNotificationOption notificationOption = ParticipantNotificationOption.DO_NOT_SEND;
    @Getter @Setter public String nonOfficialAccountParticipant = null;
    @Setter private UserNotificationProvider notiProvider;
    @Getter @Setter public String officialAccountParticipant = null;
    @Getter @Setter public List<String> officialAccountEidOnly = new ArrayList<>();
    // realm for the site
    public AuthzGroup realm = null;
    // the role set for the site
    @Setter public List<Role> roles = new ArrayList<>();
    private ParticipantRoleMode roleMode = ParticipantRoleMode.SAME_ROLE;
    // whether the same role used for all users
    @Setter @Getter public String sameRoleChoice = null;
    public Site site = null;
    public String siteId = null;
    private ParticipantStatus status = ParticipantStatus.ACTIVE;
    // the user selected
    @Getter @Setter private List<UserRoleEntry> userRoleEntries = new ArrayList<>();
    @Setter public AccountValidationService accountValidationService;
    private SiteTypeUtil siteTypeUtil;
    /** The tool session owns this serializable operation state for the current request. */
    private ParticipantWizardState wizardState;

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
		Collections.sort(roles);
        return roles;
	}

	public String getUserRole(String userId) {
		return userRoleEntries.stream()
				.filter(ure -> ure.getEid().equals(userId))
				.findAny()
				.map(UserRoleEntry::getRole)
				.orElse("");
	}

	public List<String> getUsers() {
		return userRoleEntries.stream()
				.map(UserRoleEntry::getEid)
				.collect(Collectors.toList());
	}

	/**
     * Initialize helper by getting the current site
     */
    public void init() {
        siteTypeUtil = new SiteTypeUtil(siteService, serverConfigurationService);

        if (site == null) {
			siteId = Optional.ofNullable(sessionManager.getCurrentToolSession().getAttribute(HELPER_ID + ".siteId"))
					.map(Object::toString)
					.orElseGet(() -> toolManager.getCurrentPlacement().getContext());
            try {
                site = siteService.getSite(siteId);
                realm = authzGroupService.getAuthzGroup(siteService.siteReference(siteId));
                roles = SiteParticipantHelper.getAllowedRoles( site.getType(), realm.getRoles() );
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
        officialAccountParticipant = wizardState.getOfficialAccountParticipant();
        officialAccountEidOnly = wizardState.getOfficialAccountEidOnly();
        nonOfficialAccountParticipant = wizardState.getNonOfficialAccountParticipant();
        roleMode = Optional.ofNullable(wizardState.getRoleMode()).orElse(ParticipantRoleMode.SAME_ROLE);
        sameRoleChoice = wizardState.getSameRoleChoice();
        status = Optional.ofNullable(wizardState.getStatus()).orElse(ParticipantStatus.ACTIVE);
        notificationOption = Optional.ofNullable(wizardState.getNotificationOption())
                .orElse(ParticipantNotificationOption.DO_NOT_SEND);
        userRoleEntries = wizardState.getUserRoleEntries();
        init();
    }

    /** Starts a new operation when Site Info launches this helper at its root URL. */
    public void startNewOperation() {
        resetMessages();
        reset();
        wizardState = null;
        clearState();
    }

    public boolean submitAdd(String csrfToken, String officialAccounts, String nonOfficialAccounts,
            ParticipantStatus selectedStatus) {
        csrfToken(csrfToken);
        officialAccountParticipant = officialAccounts;
        nonOfficialAccountParticipant = nonOfficialAccounts;
        if (selectedStatus == null) {
            resetMessages();
            messages.add(new ParticipantMessage("java.pleasechoose", null, ParticipantMessage.Severity.ERROR));
            saveState();
            return false;
        }
        status = selectedStatus;
        if (invalidCsrfToken()) {
            saveState();
            return false;
        }

        resetMessages();
        ParticipantAccountParser.Result result = participantAccountParser.parse(site, officialAccountParticipant,
                officialAccountEidOnly, nonOfficialAccountParticipant);
        officialAccountParticipant = result.officialAccounts();
        officialAccountEidOnly = result.officialAccountEidOnly();
        nonOfficialAccountParticipant = result.nonOfficialAccounts();
        userRoleEntries = result.entries();
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

        roleMode = selectedRoleMode;
        sameRoleChoice = selectedSameRole;
        boolean valid = ParticipantRoleMode.DIFFERENT_ROLE.equals(roleMode)
                ? applyIndividualRoles(individualRoles)
                : applySameRole();
        saveState();
        return valid;
    }

    public void backToAdd() {
        resetMessages();
        saveState();
    }

    public void backToRoles() {
        resetMessages();
        saveState();
    }

    /** Cancels the operation and returns the helper's caller-provided exit URL. */
    public String cancel() {
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
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
        if (StringUtils.isBlank(sameRoleChoice)) {
            messages.add(new ParticipantMessage("java.pleasechoose", null, ParticipantMessage.Severity.ERROR));
            return false;
        }
        if (!authzGroupService.allowUpdate("/site/" + siteId)) {
            if (realm == null) init();
            Role role = realm.getRole(sameRoleChoice);
            if (role != null && role.isAllowed("site.upd")) {
                messages.add(new ParticipantMessage("java.roleperm", new Object[] {sameRoleChoice},
                        ParticipantMessage.Severity.ERROR));
                return false;
            }
        }

        for (UserRoleEntry entry : userRoleEntries) {
            entry.setRole(sameRoleChoice);
        }
        return true;
    }

    private boolean applyIndividualRoles(List<String> individualRoles) {
        if (invalidCsrfToken()) return false;

        resetMessages();
        if (individualRoles == null || individualRoles.size() != userRoleEntries.size()) {
            messages.add(new ParticipantMessage("java.pleasechoose", null, ParticipantMessage.Severity.ERROR));
            return false;
        }
        for (int i = 0; i < individualRoles.size(); i++) {
            userRoleEntries.get(i).setRole(individualRoles.get(i));
        }

        if (!authzGroupService.allowUpdate("/site/" + siteId)) {
            for (String roleName : new HashSet<>(individualRoles)) {
                Role role = realm.getRole(roleName);
                if (role != null && role.isAllowed("site.upd")) {
                    messages.add(new ParticipantMessage("java.roleperm", new Object[] {roleName},
                            ParticipantMessage.Severity.ERROR));
                    return false;
                }
            }
        }
        return true;
    }
    
	/**
	 * whether the eId is considered of official account
	 * @param id the id to check
	 * @return true if id does not contain the char '@'
	 */
	private boolean isOfficialAccount(String id) {
		return !id.contains(EMAIL_CHAR);
	}
	
    /** Creates required guest accounts and adds all selected participants. */
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
        notificationOption = selectedNotificationOption;

    	List<String> validationUsers = new ArrayList<>();
		resetMessages();
    	if (site == null) init();
    	for (UserRoleEntry entry:userRoleEntries) {
			String eId = entry.getEid();

			if (!isOfficialAccount(eId)) { // if this is a nonOfficialAccount
				try {
					userDirectoryService.getUserByEid(eId);
				} catch (UserNotDefinedException e) {
					// if there is no such user yet, add the user
					try {
						UserEdit uEdit = userDirectoryService.addUser(null, eId);

						// set email address
						uEdit.setEmail(eId);

						// set the guest user type
						uEdit.setType("guest");

						// set the guest first name
						String firstName = entry.getFirstName();
						if (firstName != null  && !firstName.isEmpty()) uEdit.setFirstName(entry.getFirstName());

						// set the guest last name
						String lastName = entry.getLastName();
						if (lastName != null  && !lastName.isEmpty()) uEdit.setLastName(entry.getLastName());

						String pw = passwordFactory.generatePassword();
						uEdit.setPassword(pw);

						// and save
						userDirectoryService.commitEdit(uEdit);

						boolean notifyNewUserEmail = serverConfigurationService.getBoolean("notifyNewUserEmail", true);
						boolean validateUsers = serverConfigurationService.getBoolean("siteManage.validateNewUsers", true);
						if (notifyNewUserEmail && !validateUsers) {
								notiProvider.notifyNewUserEmail(uEdit, pw, site);
						} else if (notifyNewUserEmail) {
							validationUsers.add(uEdit.getId());
						}
					} catch (UserIdInvalidException ee) {
						messages.add(new ParticipantMessage(
								"java.isinval",
								new Object[] {eId},
								ParticipantMessage.Severity.INFO));
						log.warn("id [{}] is invalid", eId, ee);
					} catch (UserAlreadyDefinedException ee) {
						messages.add(new ParticipantMessage(
								"java.beenused",
								new Object[] {eId},
								ParticipantMessage.Severity.INFO));
						log.warn("id [{}] has been used", eId, ee);
					} catch (UserPermissionException ee) {
						messages.add(new ParticipantMessage(
								"java.haveadd",
								new Object[] {eId},
								ParticipantMessage.Severity.INFO));
						log.warn("You don't have permission to add [{}]", eId, ee);
					}
				}
			}
		}

		// batch add and updates the successful added list
		List<String> addedParticipantEIds = participantRealmUpdater.addParticipants(site, realm, userRoleEntries,
                status, notificationOption, messages);

		// update the not added user list
		String notAddedOfficialAccounts = "";
		String notAddedNonOfficialAccounts = "";
		for (UserRoleEntry entry:userRoleEntries) {
			String iEId = entry.getEid();
			if (!addedParticipantEIds.contains(iEId)) {
				if (isOfficialAccount(iEId)) {
					// no email in eid
					notAddedOfficialAccounts = notAddedOfficialAccounts.concat(iEId + "\n");
				} else {
					// email in eid
					notAddedNonOfficialAccounts = notAddedNonOfficialAccounts.concat(iEId + "\n");
				}
			}
		}

		// finally send any account validations
		for( String userId : validationUsers ) {
			accountValidationService.createValidationAccount(userId, true);
		}
		
		
		if (!addedParticipantEIds.isEmpty()
				&& (!notAddedOfficialAccounts.isEmpty() || !notAddedNonOfficialAccounts.isEmpty())) {
			// at lease one officialAccount account or an nonOfficialAccount
			// account added, and there are also failures
			messages.add(new ParticipantMessage(
					"java.allusers",
					null,
					ParticipantMessage.Severity.INFO));
		}
    		
		if (messages.isEmpty()) {
			// time to reset user inputs
			reset();

			// After succesfully adding participants, return to the 'Manage Participants' UI rather than whatever the previously selected tab was
			setNextPage(SiteConstants.MANAGE_PARTICIPANTS_TEMPLATE_INDEX);

			clearState();
			return true;
		} else {
			saveState();
			return false;
		}
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

        String doneUrl = (String) session.getAttribute(HELPER_TOOL_ID + Tool.HELPER_DONE_URL);
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
        notificationOption = selectedNotificationOption;
        saveState();
    }

    public boolean hasParticipants() {
        return !userRoleEntries.isEmpty();
    }

    public List<UserRoleEntry> getParticipants() {
        return Collections.unmodifiableList(userRoleEntries);
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

    public String getRoleChoice() {
        return roleMode.getFormValue();
    }

    public String getStatusChoice() {
        return status.getFormValue();
    }

    public boolean isActive() {
        return status.isActive();
    }

    public String getEmailNotiChoice() {
        return notificationOption.getFormValue();
    }

	private void reset() {
		site = null;
		siteId = null;
		realm = null;
		roles.clear();
		officialAccountParticipant = null;
		officialAccountEidOnly = new ArrayList<>();
		nonOfficialAccountParticipant = null;
		roleMode = ParticipantRoleMode.SAME_ROLE;
		status = ParticipantStatus.ACTIVE;
		sameRoleChoice = null;
		notificationOption = ParticipantNotificationOption.DO_NOT_SEND;
		userRoleEntries = new ArrayList<>();
	}

    private void csrfToken(String value) {
        csrfToken = value;
    }

    private ParticipantWizardState readState() {
        ToolSession session = sessionManager.getCurrentToolSession();
        Object state = session == null ? null : session.getAttribute(STATE_ATTRIBUTE);
        return state instanceof ParticipantWizardState ? (ParticipantWizardState) state : new ParticipantWizardState();
    }

    private void saveState() {
        ToolSession session = sessionManager.getCurrentToolSession();
        if (session == null) return;

        if (wizardState == null) wizardState = new ParticipantWizardState();
        wizardState.setOfficialAccountParticipant(officialAccountParticipant);
        wizardState.setOfficialAccountEidOnly(officialAccountEidOnly);
        wizardState.setNonOfficialAccountParticipant(nonOfficialAccountParticipant);
        wizardState.setRoleMode(roleMode);
        wizardState.setSameRoleChoice(sameRoleChoice);
        wizardState.setStatus(status);
        wizardState.setNotificationOption(notificationOption);
        wizardState.setUserRoleEntries(userRoleEntries);
        session.setAttribute(STATE_ATTRIBUTE, wizardState);
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
