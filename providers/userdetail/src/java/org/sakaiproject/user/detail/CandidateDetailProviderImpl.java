/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.user.detail;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;

/**
 * This candidate details provider looks up information on the user object's properties, this assumes that the
 * user provider has loaded the additional data already. It also assumes that the data is encrytped on the
 * user object.
 */
@Slf4j
@Setter 
public class CandidateDetailProviderImpl implements CandidateDetailProvider {
	
	private static final String USER_PROP_CANDIDATE_ID = "candidateID";
	private static final String USER_PROP_ADDITIONAL_INFO = "additionalInfo";
	private static final String USER_PROP_SPECIAL_NEEDS = "specialNeeds";
	private static final String USER_PROP_STUDENT_NUMBER = "studentNumber";
	private static final String USER_PERM_STUDENT_NUMBER_VISIBLE = "user.studentnumber.visible";
	
	private final static String PROP_USE_INSTITUTIONAL_ANONYMOUS_ID = "useInstitutionalAnonymousID";
	private final static String PROP_DISPLAY_ADDITIONAL_INFORMATION = "displayAdditionalInformation";
	private final static String PROP_DISPLAY_SPECIAL_NEEDS = "displaySpecialNeeds";
	private final static String PROP_USE_INSTITUTIONAL_NUMERIC_ID = "useInstitutionalNumericID";
	private final static String PROP_ENCRYPT_NUMERIC_ID = "encryptInstitutionalNumericID";
	private final static String PROP_ENCRYPT_CANDIDATE_DETAILS = "encryptCandidateDetails";

	private PreferencesService preferencesService;
	private ServerConfigurationService serverConfigurationService;
	private SessionManager sessionManager;
	private SiteService siteService;
	private SecurityService securityService;
	private ToolManager toolManager;
	private ValueEncryptionUtilities encryptionUtilities;
	
	public void init() {
		Objects.requireNonNull(preferencesService, "ServerConfigurationService must be set");
		Objects.requireNonNull(siteService, "SiteService must be set");
		Objects.requireNonNull(toolManager, "ToolManager must be set");
		Objects.requireNonNull(serverConfigurationService, "ServerConfigurationService must be set");
		Objects.requireNonNull(sessionManager, "SessionManager must be set");
		Objects.requireNonNull(encryptionUtilities, "ValueEncryptionUtilities must be set");
		Objects.requireNonNull(securityService, "SecurityService must be set");
	}
	
	public Optional<String> getCandidateID(User user, Site site) {
		try {
			if(user != null) {
				//check if we should use the institutional anonymous id (system-wide or site-based)
				if(useInstitutionalAnonymousId(site)) {
					if(StringUtils.isNotBlank(user.getProperties().getProperty(USER_PROP_CANDIDATE_ID))) {
						String decrypt = encryptionUtilities.decrypt(user.getProperties().getProperty(USER_PROP_CANDIDATE_ID));
						//this property is encrypted, so we need to decrypt it
						if (StringUtils.isNotBlank(decrypt)) {
							return Optional.ofNullable(decrypt);
						}
					}
				}
			}
		} catch(Exception e) {
			log.warn("Error getting candidateID for {}", ((user != null) ? user.getId() : "-null-"), e);
		}
		return Optional.empty();
	}

	public boolean useInstitutionalAnonymousId(Site site) {
		try {
			return (serverConfigurationService.getBoolean(PROP_USE_INSTITUTIONAL_ANONYMOUS_ID, false) ||
			(site != null && site.getProperties().getBooleanProperty(PROP_USE_INSTITUTIONAL_ANONYMOUS_ID)));
		} catch(Exception ignore) {}
		return false;
	}

	private List<String> getI18nPropertyList(String propName, User user, Site site) {

		String siteLanguage = site.getProperties().getProperty(Site.PROP_SITE_LANGUAGE);

		if (StringUtils.isNotEmpty(siteLanguage)) {
			siteLanguage = "_" + StringUtils.substring(siteLanguage, 0, 2);
			List<String> propList = user.getProperties().getPropertyList(propName + siteLanguage);
			if (propList != null) {
				return propList; 
			}
		}

		String userLanguage = preferencesService.getLocale(sessionManager.getCurrentSession().getUserId()).getLanguage();

		if (StringUtils.isNotEmpty(userLanguage)) {
			userLanguage = "_" + userLanguage;
			List<String> propList = user.getProperties().getPropertyList(propName + userLanguage);
			if (propList != null) {
				return propList; 
			}
		}

		return user.getProperties().getPropertyList(propName);
	}

	public Optional<List<String>> getAdditionalNotes(User user, Site site) {
		try {
			//check if additional notes are enabled (system-wide or site-based)
			if(user != null && isAdditionalNotesEnabled(site) && user.getProperties() != null) {
				List<String> additionalNotesList = getI18nPropertyList(USER_PROP_ADDITIONAL_INFO, user, site);
				if (additionalNotesList != null) {
					if (serverConfigurationService.getBoolean(PROP_ENCRYPT_CANDIDATE_DETAILS, true)) {
						 return Optional.of(additionalNotesList.stream()
								.filter(StringUtils::isNotBlank)
								.map(encryptionUtilities::decrypt)
								.filter(StringUtils::isNotBlank)
							.collect(Collectors.toList()));
					} else {
						 return Optional.of(additionalNotesList.stream()
								.filter(StringUtils::isNotBlank)
							.collect(Collectors.toList()));
					}
				}
			}
		} catch(Exception e) {
			log.warn("Error getting additional info for {}", ((user != null) ? user.getId() : "-null-"), e);
		}
		return Optional.empty();
	}

	public boolean isAdditionalNotesEnabled(Site site) {
		try {
			return (serverConfigurationService.getBoolean(PROP_DISPLAY_ADDITIONAL_INFORMATION, false) ||
			(site != null && site.getProperties().getBooleanProperty(PROP_DISPLAY_ADDITIONAL_INFORMATION)));
		} catch(Exception ignore) {}
		return false;
	}

	public Optional<List<String>> getSpecialNeeds(User user, Site site) {
		try {
			//check if special needs info is enabled (system-wide or site-based)
			if (user != null && isSpecialNeedsEnabled(site) && user.getProperties() != null ) {
				List<String> specialNeedsList = getI18nPropertyList(USER_PROP_SPECIAL_NEEDS, user, site);
				if (specialNeedsList != null) {
					if (serverConfigurationService.getBoolean(PROP_ENCRYPT_CANDIDATE_DETAILS, true)) {
						 return Optional.of(specialNeedsList.stream()
								.filter(StringUtils::isNotBlank)
								.map(encryptionUtilities::decrypt)
								.filter(StringUtils::isNotBlank)
							.collect(Collectors.toList()));
					} else {
						 return Optional.of(specialNeedsList.stream()
								.filter(StringUtils::isNotBlank)
							.collect(Collectors.toList()));
					}
				}
			}
		} catch (Exception e) {
			log.warn("Error special needs info for {}", ((user != null) ? user.getId() : "-null-"), e);
		}
		return Optional.empty();
	}

	public boolean isSpecialNeedsEnabled(Site site) {
		try {
			return (serverConfigurationService.getBoolean(PROP_DISPLAY_SPECIAL_NEEDS, true) || (site != null && site.getProperties().getBooleanProperty(PROP_DISPLAY_SPECIAL_NEEDS)));
		} catch(Exception ignore) {}
		return false;
	}

	@Override
	public Optional<String> getInstitutionalNumericId(User user, Site site)
	{
		return getNumericId(user, site, true);
	}
	
	@Override
	public Optional<String> getInstitutionalNumericIdIgnoringCandidatePermissions(User candidate, Site site)
	{
		if (site == null) {
			return getNumericId(candidate);
		}

		return getNumericId(candidate, site, false);
	}

	private Optional<String> getNumericId(User user) {

		try {
			String studentNumber = user.getProperties().getProperty(USER_PROP_STUDENT_NUMBER);
			if (serverConfigurationService.getBoolean(PROP_ENCRYPT_NUMERIC_ID,
				serverConfigurationService.getBoolean(PROP_ENCRYPT_CANDIDATE_DETAILS, true)))
			{
				studentNumber = encryptionUtilities.decrypt(studentNumber);
			}
				
			if (StringUtils.isNotBlank(studentNumber)) {
				return Optional.of(studentNumber);
			}
		} catch (Exception e) {
			log.warn("Error getting studentNumber for {}", user.getId(), e);
		}
		
		return Optional.empty();
	}

	private Optional<String> getNumericId(User user, Site site, boolean checkVisibilityPermission)
	{
		if (user == null || site == null || !isInstitutionalNumericIdEnabled(site)
				|| (checkVisibilityPermission && !securityService.unlock(user, USER_PERM_STUDENT_NUMBER_VISIBLE, site.getReference())))
		{
			return Optional.empty();
		}

		return getNumericId(user);
	}
	
	@Override
	public boolean isInstitutionalNumericIdEnabled(Site site)
	{
		try
		{
			return (serverConfigurationService.getBoolean(PROP_USE_INSTITUTIONAL_NUMERIC_ID, true) ||
			(site != null && site.getProperties().getBooleanProperty(PROP_USE_INSTITUTIONAL_NUMERIC_ID)));
		}
		catch (Exception ignore)
		{
			// ignore
		}
		
		return false;
	}
	

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	public void setSecurityService(SecurityService value)
	{
		securityService = value;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void setValueEncryptionUtilities(ValueEncryptionUtilities encryptionUtilities) {
		this.encryptionUtilities = encryptionUtilities;
	}
}
