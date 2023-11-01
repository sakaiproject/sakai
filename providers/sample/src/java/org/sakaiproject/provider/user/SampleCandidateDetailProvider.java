/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.provider.user;

import java.lang.Math;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.detail.ValueEncryptionUtilities;

/**
 * <p>
 * SampleCandidateDetailProvider is a sample CandidateDetailProvider.
 * </p>
 */
@Slf4j
public class SampleCandidateDetailProvider implements CandidateDetailProvider
{
	private static final String USER_PROP_CANDIDATE_ID = "candidateID";
	private static final String USER_PROP_ADDITIONAL_INFO = "additionalInfo";
	private static final String USER_PROP_SPECIAL_NEEDS = "specialNeeds";
	private static final String USER_PROP_STUDENT_NUMBER = "studentNumber";

	private final static String SITE_PROP_USE_INSTITUTIONAL_ANONYMOUS_ID = "useInstitutionalAnonymousID";
	private final static String SITE_PROP_DISPLAY_ADDITIONAL_INFORMATION = "displayAdditionalInformation";
	private final static String SITE_PROP_DISPLAY_SPECIAL_NEEDS = "displaySpecialNeeds";
	private final static String SYSTEM_PROP_ENCRYPT_NUMERIC_ID = "encryptInstitutionalNumericID";
	private final static String SITE_PROP_USE_INSTITUTIONAL_NUMERIC_ID = "useInstitutionalNumericID";
	
	private final static String SYSTEM_PROP_USE_INSTITUTIONAL_ANONYMOUS_ID = "useInstitutionalAnonymousID";
	private final static String SYSTEM_PROP_DISPLAY_ADDITIONAL_INFORMATION = "displayAdditionalInformation";
	private final static String SYSTEM_PROP_DISPLAY_SPECIAL_NEEDS = "displaySpecialNeeds";
	private final static String SYSTEM_PROP_USE_INSTITUTIONAL_NUMERIC_ID = "useInstitutionalNumericID";
	private final static String SYSTEM_PROP_ENCRYPT_CANDIDATE_DETAILS = "encryptCandidateDetails";
	
	private static final String[] SAMPLE_SPECIAL_NEEDS = {
		"Anticipate the material to work on, for example upload the teaching material ahead of time.",
		"Don't overload information pages.",
		"At the beginning of the class, the teacher should indicate how he will organize the session and summarize the most important ideas at the end of the session.	",
		"Respect if student does not want to read out loud in front of classmates.",
		"Less demanding spelling.",
		"More time to take the exams.",
		"Brief problem statements divided into parts."
	};
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	private SecureRandom random = new SecureRandom();
	private PreferencesService preferencesService;
	private ServerConfigurationService serverConfigurationService;
	private SessionManager sessionManager;
	private SiteService siteService;
	private ToolManager toolManager;
	private ValueEncryptionUtilities encryptionUtilities;

	public void setPreferencesService(PreferencesService preferencesService) {
		this.preferencesService = preferencesService;
	}
	
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void setValueEncryptionUtilities(ValueEncryptionUtilities encryptionUtilities) {
		this.encryptionUtilities = encryptionUtilities;
	}

	/***************************************************************************
	 * Init and Destroy
	 **************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		Objects.requireNonNull(preferencesService, "ServerConfigurationService must be set");
		Objects.requireNonNull(serverConfigurationService, "ServerConfigurationService must be set");
		Objects.requireNonNull(sessionManager, "SessionManager must be set");
		Objects.requireNonNull(siteService, "SiteService must be set");
		Objects.requireNonNull(toolManager, "ToolManager must be set");

		log.info("init()");
	}
	
	/**
	 * Returns to uninitialized state. You can use this method to release resources thet your Service allocated when Turbine shuts down.
	 */
	public void destroy()
	{

		log.info("destroy()");

	} // destroy

	/**********************************************************************************************************************************************************************************************************************************************************
	 * CandidateDetailProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct.
	 */
	public SampleCandidateDetailProvider()
	{
	}	
	
	public Optional<String> getCandidateID(User user, Site site){
		if(site == null) {
			log.error("getCandidateID: Null site.");
			return Optional.empty();
		}
		try {
			if(user != null) {
				//check if additional notes is enabled (system-wide or site-based)
				if(isAdditionalNotesEnabled(site)) {
					if(user.getProperties() != null && StringUtils.isNotBlank(user.getProperties().getProperty(USER_PROP_CANDIDATE_ID)) && StringUtils.isNotBlank(encryptionUtilities.decrypt(user.getProperties().getProperty(USER_PROP_CANDIDATE_ID)))) {
						log.debug("Using user candidateID property for user {}", user.getId());
						//this property is encrypted, so we need to decrypt it
						return Optional.ofNullable(encryptionUtilities.decrypt(user.getProperties().getProperty(USER_PROP_CANDIDATE_ID)));
					} else {
						int hashInt = user.getId().hashCode();
						if(hashInt % 10 == 4){
							log.debug("Not generating random sample candidate id for user " + user.getId());
							return Optional.empty();
						} else {
							log.debug("Generating sample candidate id for user {}", user.getId());
							return Optional.ofNullable(String.valueOf(Math.abs(hashInt % 100000)));
						}
					}
				}
			}
		} catch(Exception e) {
			log.error("Error getting sample candidateID for "+((user != null) ? user.getId() : "-null-"), e);
		}
		return Optional.empty();
	}

	public Optional<List<String>> getAdditionalNotes(User user, Site site){
		if(site == null) {
			log.error("getAdditionalNotes: Null site.");
			return Optional.empty();
		}
		try {
			if(user != null) {
				//check if additional notes is enabled (system-wide or site-based)
				if (isAdditionalNotesEnabled(site) && user.getProperties() != null) {
					List<String> additionalNotesList = getI18nPropertyList(USER_PROP_ADDITIONAL_INFO, user, site);
					if (additionalNotesList != null) {
						log.debug("Showing additional notes for user {}", user.getId());
						if (serverConfigurationService.getBoolean(SYSTEM_PROP_ENCRYPT_CANDIDATE_DETAILS, true)) {
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
					} else {
						List<String> ret = new ArrayList<String>();
						int hashInt = user.getId().hashCode();
						if(hashInt % 10 == 2){
							log.debug("Not generating random additional notes for user {}", user.getId());
							return Optional.empty();
						} else {
							log.debug("Generating random additional notes for user {}", user.getId());
							String notes = new BigInteger(130, random).toString(32);
							ret.add(notes);
							if(hashInt % 10 == 7){
								log.debug("Generating more random additional notes for user {}", user.getId());
								notes = new BigInteger(130, random).toString(32);
								ret.add(notes);
							}
							return Optional.ofNullable(ret);
						}
					}
				}
			}
		} catch(Exception e) {
			log.error("Error getting sample additional info for {}", ((user != null) ? user.getId() : "-null-"), e);
		}
		return Optional.empty();
	}

	public boolean isAdditionalNotesEnabled(Site site) {
		try {
			return (serverConfigurationService.getBoolean(SYSTEM_PROP_DISPLAY_ADDITIONAL_INFORMATION, false) || (site != null && Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROP_DISPLAY_ADDITIONAL_INFORMATION))));
		} catch(Exception e) {
			log.warn("Could not determine if Additional Notes is enabled: {} ", e.toString());
		}
		return false;
	}

	public Optional<List<String>> getSpecialNeeds(User user, Site site){
		if(site == null) {
			log.error("A null site was detected, returning empty");
			return Optional.empty();
		}

		try {
			//check if special needs info is enabled (system-wide or site-based)
			if (user != null && isSpecialNeedsEnabled(site) && user.getProperties() != null ) {
				List<String> specialNeedsList = getI18nPropertyList(USER_PROP_SPECIAL_NEEDS, user, site);
				if (specialNeedsList != null) {
					log.debug("Showing special needs info for user {}", user.getId());
					if (serverConfigurationService.getBoolean(SYSTEM_PROP_ENCRYPT_CANDIDATE_DETAILS, true)) {
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
				} else {
					List<String> sampleSpecialNeeds = new ArrayList<String>();
					int hashInt = user.getId().hashCode();
					if (hashInt % 10 == 2) {
						log.debug("Not generating random special needs infos for user {}", user.getId());
						return Optional.empty();
					} else {
						log.debug("Generating random special needs infos for user {}", user.getId());
						sampleSpecialNeeds.add(SAMPLE_SPECIAL_NEEDS[random.nextInt(SAMPLE_SPECIAL_NEEDS.length)]);
						sampleSpecialNeeds.add(SAMPLE_SPECIAL_NEEDS[random.nextInt(SAMPLE_SPECIAL_NEEDS.length)]);
						if (hashInt % 10 == 7) {
							log.debug("Generating more random special needs infos for user {}", user.getId());
							sampleSpecialNeeds.add(SAMPLE_SPECIAL_NEEDS[random.nextInt(SAMPLE_SPECIAL_NEEDS.length)]);
						}
						return Optional.ofNullable(sampleSpecialNeeds);
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not determine if special needs is enabled: {} ", e.toString());
		}
		return Optional.empty();
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

	public boolean isSpecialNeedsEnabled(Site site) {
		try {
			return (serverConfigurationService.getBoolean(SYSTEM_PROP_DISPLAY_SPECIAL_NEEDS, true) || (site != null && Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROP_DISPLAY_SPECIAL_NEEDS))));
		} catch(Exception e) {
			log.warn("Could not determine if special needs is enabled: {} ", e.toString());
		}
		return false;
	}

	@Override
	public Optional<String> getInstitutionalNumericId(User user, Site site){

		try {
			if(user != null) {
				//check if student number is enabled (system-wide or site-based)
				if(isInstitutionalNumericIdEnabled(site)) {
					if(user.getProperties() != null && StringUtils.isNotBlank(user.getProperties().getProperty(USER_PROP_STUDENT_NUMBER))) {
						log.debug("Using user candidateID property for user {}", user.getId());
						String studentNumber = user.getProperties().getProperty(USER_PROP_STUDENT_NUMBER);
						if (serverConfigurationService.getBoolean(SYSTEM_PROP_ENCRYPT_NUMERIC_ID,
							serverConfigurationService.getBoolean(SYSTEM_PROP_ENCRYPT_CANDIDATE_DETAILS, true)))
						{
							studentNumber = encryptionUtilities.decrypt(studentNumber);
						}

						return Optional.ofNullable(studentNumber);
					} else {
						int hashInt = user.getId().hashCode();
						if(hashInt % 10 == 4){
							log.debug("Not generating random sample student number for user " + user.getId());
							return Optional.empty();
						} else {
							log.debug("Generating sample candidate id for user {}", user.getId());
							return Optional.ofNullable(String.valueOf(Math.abs(hashInt % 100000)));
						}
					}
				}
			}
		} catch(Exception e) {
			log.error("Error getting sample studentNumber for "+((user != null) ? user.getId() : "-null-"), e);
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<String> getInstitutionalNumericIdIgnoringCandidatePermissions(User candidate, Site site)
	{
		return getInstitutionalNumericId(candidate, site);
	}

	public boolean useInstitutionalAnonymousId(Site site) {
		try {
			return (serverConfigurationService.getBoolean(SYSTEM_PROP_USE_INSTITUTIONAL_ANONYMOUS_ID, false) || (site != null && Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROP_USE_INSTITUTIONAL_ANONYMOUS_ID))));
		} catch(Exception e) {
			log.warn("Error on useInstitutionalAnonymousId (sample) ", e);
		}
		return false;
	}
	
	@Override
	public boolean isInstitutionalNumericIdEnabled(Site site) {
		try {
			return (serverConfigurationService.getBoolean(SYSTEM_PROP_USE_INSTITUTIONAL_NUMERIC_ID, true)
					|| (site != null && Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROP_USE_INSTITUTIONAL_NUMERIC_ID))));
		} catch(Exception e) {
			log.warn("Error on isInstitutionalNumericIdEnabled (sample) ", e);
		}
		return false;
	}

}
