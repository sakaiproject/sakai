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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.CandidateDetailProvider;
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
	private static final String USER_PROP_STUDENT_NUMBER = "studentNumber";
	
	private final static String SITE_PROP_USE_INSTITUTIONAL_ANONYMOUS_ID = "useInstitutionalAnonymousID";
	private final static String SITE_PROP_DISPLAY_ADDITIONAL_INFORMATION = "displayAdditionalInformation";
	private final static String SITE_PROP_USE_INSTITUTIONAL_NUMERIC_ID = "useInstitutionalNumericID";
	
	private final static String SYSTEM_PROP_USE_INSTITUTIONAL_ANONYMOUS_ID = "useInstitutionalAnonymousID";
	private final static String SYSTEM_PROP_DISPLAY_ADDITIONAL_INFORMATION = "displayAdditionalInformation";
	private final static String SYSTEM_PROP_USE_INSTITUTIONAL_NUMERIC_ID = "useInstitutionalNumericID";
	private final static String SYSTEM_PROP_ENCRYPT_NUMERIC_ID = "encryptInstitutionalNumericID";

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	private SecureRandom random = new SecureRandom();
	private ServerConfigurationService serverConfigurationService;
	private SiteService siteService;
	private ToolManager toolManager;
	private ValueEncryptionUtilities encryptionUtilities;
	 
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
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
		Objects.requireNonNull(siteService, "SiteService must be set");
		Objects.requireNonNull(toolManager, "ToolManager must be set");
		Objects.requireNonNull(serverConfigurationService, "ServerConfigurationService must be set");

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
				if(isAdditionalNotesEnabled(site)) {
					if(user.getProperties() != null && user.getProperties().getPropertyList(USER_PROP_ADDITIONAL_INFO) != null) {
						log.debug("Showing additional notes for user {}", user.getId());
						List<String> ret = new ArrayList<String>();
						for(String s : user.getProperties().getPropertyList(USER_PROP_ADDITIONAL_INFO)) {
							//this property is encrypted, so we need to decrypt it
							if(StringUtils.isNotBlank(s) && StringUtils.isNotBlank(encryptionUtilities.decrypt(s))){
								ret.add(encryptionUtilities.decrypt(s));
							}
						}
						return Optional.ofNullable(ret);
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
	
	@Override
	public Optional<String> getInstitutionalNumericId(User user, Site site){
		if(site == null) {
			log.error("getInstitutionalNumericId: Null site.");
			return Optional.empty();
		}
		try {
			if(user != null) {
				//check if student number is enabled (system-wide or site-based)
				if(isInstitutionalNumericIdEnabled(site)) {
					if(user.getProperties() != null && StringUtils.isNotBlank(user.getProperties().getProperty(USER_PROP_STUDENT_NUMBER))) {
						log.debug("Using user candidateID property for user {}", user.getId());
						String studentNumber = user.getProperties().getProperty(USER_PROP_STUDENT_NUMBER);
						if (serverConfigurationService.getBoolean(SYSTEM_PROP_ENCRYPT_NUMERIC_ID, true))
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
	
	public boolean isAdditionalNotesEnabled(Site site) {
		try {
			return (serverConfigurationService.getBoolean(SYSTEM_PROP_DISPLAY_ADDITIONAL_INFORMATION, false) || (site != null && Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROP_DISPLAY_ADDITIONAL_INFORMATION))));
		} catch(Exception e) {
			log.warn("Error on isAdditionalNotesEnabled (sample) ", e);
		}
		return false;
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
			return (serverConfigurationService.getBoolean(SYSTEM_PROP_USE_INSTITUTIONAL_NUMERIC_ID, false)
					|| (site != null && Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROP_USE_INSTITUTIONAL_NUMERIC_ID))));
		} catch(Exception e) {
			log.warn("Error on isInstitutionalNumericIdEnabled (sample) ", e);
		}
		return false;
	}

}