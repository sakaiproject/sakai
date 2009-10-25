/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * @author rshastri
 */
public class ProfileManagerImpl implements ProfileManager
{
	private static final Log LOG = LogFactory.getLog(ProfileManagerImpl.class);

	/** Dependency: SakaiPersonManager */
	private SakaiPersonManager sakaiPersonManager;

	/** Dependency: userDirectoryService */
	private UserDirectoryService userDirectoryService;
	
	private static final String ANONYMOUS = "Anonymous";

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService scs) {
		serverConfigurationService = scs;
	}
	
	public void init()
	{
		
		LOG.info("init()");
	}

	public void destroy()
	{
		LOG.debug("destroy()"); // do nothing (for now)
	}

	/**
	 * @see org.sakaiproject.api.app.profile.ProfileManager#getProfile()
	 */
	public Profile getProfile()
	{
		LOG.debug("getProfile()");

		return getProfileById(getCurrentUserId());
	}

	public Map<String, Profile> getProfiles(Set<String> userIds)
	{
		LOG.debug("getProfiles()");
		return findProfiles(userIds);
	}

	/**
	 * @see org.sakaiproject.api.app.profile.ProfileManager#findProfiles(java.lang.String) Returns userMutable profiles only
	 */
	public List findProfiles(String searchString)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("findProfiles(" + searchString + ")");
		}
		if (searchString == null || searchString.length() < 1)
			throw new IllegalArgumentException("Illegal searchString argument passed!");

		List profiles = sakaiPersonManager.findSakaiPerson(searchString);
		List searchResults = new ArrayList();
		Profile profile;

		if ((profiles != null) && (profiles.size() > 0))
		{
			Iterator profileIterator = profiles.iterator();

			while (profileIterator.hasNext())
			{
				profile = new ProfileImpl((SakaiPerson) profileIterator.next());

				// Select the user mutable profile for display on if the public information is viewable.
				if ((profile != null)
						&& profile.getSakaiPerson().getTypeUuid().equals(sakaiPersonManager.getUserMutableType().getUuid()))
				{
					if ((getCurrentUserId().equals(profile.getUserId()) || SecurityService.isSuperUser()))
					{
						// allow user to search and view own profile and superuser to view all profiles
						searchResults.add(profile);
					}
					else if ((profile.getHidePublicInfo() != null) && (profile.getHidePublicInfo().booleanValue() != true))
					{
						if (profile.getHidePrivateInfo() != null && profile.getHidePrivateInfo().booleanValue() != true)
						{
							searchResults.add(profile);
						}
						else
						{
							searchResults.add(getOnlyPublicProfile(profile));
						}

					}
				}

			}
		}

		return searchResults;
	}

	/**
	 * @see org.sakaiproject.api.app.profile.ProfileManager#save(org.sakaiproject.api.app.profile.Profile)
	 */
	public void save(Profile profile)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("save(" + profile + ")");
		}
		if (profile == null) throw new IllegalArgumentException("Illegal profile argument passed!");

		sakaiPersonManager.save(profile.getSakaiPerson());
	}

	/**
	 * @param sakaiPersonManager
	 */
	public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSakaiPersonManager(SakaiPersonManager " + sakaiPersonManager + ")");
		}

		this.sakaiPersonManager = sakaiPersonManager;
	}

	/**
	 * @param userDirectoryService
	 *        The userDirectoryService to set.
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setUserDirectoryService(userDirectoryService " + userDirectoryService + ")");
		}

		this.userDirectoryService = userDirectoryService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.ProfileManager#getInstitutionalPhotoByUserId(java.lang.String)
	 */
	public byte[] getInstitutionalPhotoByUserId(String uid)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getInstitutionalPhotoByUserId(String " + uid + ")");
		}
		return getInstitutionalPhoto(uid, false);

	}

	public byte[] getInstitutionalPhotoByUserId(String uid, boolean viewerHasPermission)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getInstitutionalPhotoByUserId(String" + uid + ", boolean " + viewerHasPermission + ")");
		}
		return getInstitutionalPhoto(uid, viewerHasPermission);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.ProfileManager#getUserProfileById(java.lang.String)
	 */
	public Profile getUserProfileById(String id)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getUserProfileById(String" + id + ")");
		}
		SakaiPerson sakaiPerson = sakaiPersonManager.getSakaiPerson(id, sakaiPersonManager
				.getUserMutableType());
		if (sakaiPerson == null)
		{
			return null;
		}
		return new ProfileImpl(sakaiPerson);
	}

	public boolean displayCompleteProfile(Profile profile)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("displayCompleteProfile(Profile" + profile + ")");
		}
		// complete profile visble to Owner and superUser
		if (profile == null)
		{
			return false;
		}
		if ((isCurrentUserProfile(profile) || SecurityService.isSuperUser()))
		{
			return true;
		}
		else if (profile.getHidePrivateInfo() == null)
		{
			return false;
		}
		if (profile.getHidePublicInfo() == null)
		{
			return false;
		}
		if (profile.getHidePrivateInfo().booleanValue() != true && profile.getHidePublicInfo().booleanValue() != true)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.ProfileManager#isCurrentUserProfile(org.sakaiproject.api.app.profile.Profile)
	 */
	public boolean isCurrentUserProfile(Profile profile)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("isCurrentUserProfile(Profile" + profile + ")");
		}
		return ((profile != null) && profile.getUserId().equals(getCurrentUserId()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.ProfileManager#isDisplayPictureURL(org.sakaiproject.api.app.profile.Profile)
	 */
	public boolean isDisplayPictureURL(Profile profile)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("isDisplayPictureURL(Profile" + profile + ")");
		}
		return (profile != null
				&& displayCompleteProfile(profile)
				&& (profile.isInstitutionalPictureIdPreferred() == null || profile.isInstitutionalPictureIdPreferred()
						.booleanValue() != true) && profile.getPictureUrl() != null && profile.getPictureUrl().trim().length() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.ProfileManager#isDisplayUniversityPhoto(org.sakaiproject.api.app.profile.Profile)
	 */
	public boolean isDisplayUniversityPhoto(Profile profile)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("isDisplayUniversityPhoto(Profile" + profile + ")");
		}
		return (profile != null && displayCompleteProfile(profile) && profile.isInstitutionalPictureIdPreferred() != null
				&& profile.isInstitutionalPictureIdPreferred().booleanValue() == true
				&& getInstitutionalPhotoByUserId(profile.getUserId()) != null && getInstitutionalPhotoByUserId(profile.getUserId()).length > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.ProfileManager#isDisplayUniversityPhotoUnavailable(org.sakaiproject.api.app.profile.Profile)
	 */
	public boolean isDisplayUniversityPhotoUnavailable(Profile profile)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("isDisplayUniversityPhotoUnavailable(Profile" + profile + ")");
		}
		return (profile != null && displayCompleteProfile(profile) && profile.isInstitutionalPictureIdPreferred() != null
				&& profile.isInstitutionalPictureIdPreferred().booleanValue() == true
				&& getInstitutionalPhotoByUserId(profile.getUserId()) == null && (getInstitutionalPhotoByUserId(profile.getUserId()) == null || getInstitutionalPhotoByUserId(profile
				.getUserId()).length < 1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.ProfileManager#isDisplayNoPhoto(org.sakaiproject.api.app.profile.Profile)
	 */
	public boolean isDisplayNoPhoto(Profile profile)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("isDisplayNoPhoto(Profile" + profile + ")");
		}
		return (profile == null || !displayCompleteProfile(profile) || (profile.isInstitutionalPictureIdPreferred() == null || (profile
				.isInstitutionalPictureIdPreferred().booleanValue() != true && (profile.getPictureUrl() == null || profile
				.getPictureUrl().trim().length() < 1))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.ProfileManager#isShowProfileTool(org.sakaiproject.api.app.profile.Profile)
	 */
	public boolean isShowTool()
	{
		LOG.debug("isShowTool()");  
	    Profile profile = getProfile();
	    
	    return (profile.getUserId() != ANONYMOUS && profile.getUserId().equalsIgnoreCase(getCurrentUserId()));	
	}
  

   public boolean isShowSearch()
   {
      LOG.debug("isShowSearch()");
      Profile profile = getProfile();
      if(!"false".equalsIgnoreCase(serverConfigurationService.getString
              ("profile.showSearch", "true")) 
              || userDirectoryService.getCurrentUser().getId().equals(UserDirectoryService.ADMIN_ID))
      {
	      // implement isAnonymous later on.
	      if(!"false".equalsIgnoreCase(serverConfigurationService.getString
	            ("separateIdEid@org.sakaiproject.user.api.UserDirectoryService")))
	      {
	         return (profile.getUserId() != ANONYMOUS && isSiteMember(profile.getSakaiPerson().getAgentUuid()));
	      }
	      return (profile.getUserId() != ANONYMOUS && isSiteMember(profile.getUserId()));
      }
      return false;
   }

	private Profile getOnlyPublicProfile(Profile profile)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getOnlyPublicProfile(Profile" + profile + ")");
		}
		profile.getSakaiPerson().setJpegPhoto(null);
		profile.setPictureUrl(null);
		profile.setEmail(null);
		profile.setHomepage(null);
		profile.setHomePhone(null);
		profile.setOtherInformation(null);
		return profile;
	}

	/**
	 * Get the id photo if the profile member is site member and the requestor is either site maintainter or user or superuser.
	 * 
	 * @param userId
	 * @param siteMaintainer
	 * @return
	 */
	private byte[] getInstitutionalPhoto(String userId, boolean viewerHasPermission)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getInstitutionalPhoto(" + userId + ")");
		}
		if (userId == null || userId.length() < 1) throw new IllegalArgumentException("Illegal userId argument passed!");

		SakaiPerson sakaiSystemPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getSystemMutableType());
		SakaiPerson sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getUserMutableType());
		Profile profile = null;

		if ((sakaiSystemPerson == null))
		{
			try
			{
				userDirectoryService.getUser(userId);
			}
			catch (UserNotDefinedException unde)
			{
				LOG.warn("User " + userId + " does not exist. ", unde);
				return null;
			}
			sakaiSystemPerson = sakaiPersonManager.create(userId, sakaiPersonManager.getSystemMutableType());
		}
		Profile systemProfile = new ProfileImpl(sakaiSystemPerson);
		
		// Fetch current users institutional photo for either the user or super user
		if (getCurrentUserId().equals(userId) || SecurityService.isSuperUser() || viewerHasPermission)
		{
			if(LOG.isDebugEnabled()) LOG.debug("Official Photo fetched for userId " + userId);
			return systemProfile.getInstitutionalPicture();	
		}

		// if the public information && private information is viewable and user uses to display institutional picture id.
		if (sakaiPerson != null)
		{
			profile = new ProfileImpl(sakaiPerson);
			if (sakaiPerson != null && (profile.getHidePublicInfo() != null)
					&& (profile.getHidePublicInfo().booleanValue() == false) && profile.getHidePrivateInfo() != null
					&& profile.getHidePrivateInfo().booleanValue() == false
					&& profile.isInstitutionalPictureIdPreferred() != null
					&& profile.isInstitutionalPictureIdPreferred().booleanValue() == true)
			{
				if(LOG.isDebugEnabled()) LOG.debug("Official Photo fetched for userId " + userId);			
				return systemProfile.getInstitutionalPicture();				
			}

		}
		return null;
	}

	/**
	 * @param uid
	 * @return
	 */
	private boolean isSiteMember(String uid)
	{
		
		if (LOG.isDebugEnabled())
		{
			LOG.debug("isSiteMember(String" + uid + ")");
		}
		try
		{
			return SecurityService.unlock(uid, SiteService.SITE_VISIT, SiteService.siteReference(getCurrentSiteId()));
		}
		catch (Exception e)
		{
			LOG.error("Exception:", e);
		}
		return false;
	}

	/**
	 * @return
	 */
	private String getCurrentSiteId()
	{
		LOG.debug("getCurrentSiteId()");
		Placement placement = ToolManager.getCurrentPlacement();
		return placement.getContext();
	}

	/**
	 * @param id
	 * @return
	 */
	private Profile getProfileById(String id)
	{
		Set<String> userIds = new HashSet<String>();
		userIds.add(id);
		Map<String, Profile> profiles = findProfiles(userIds);
		return profiles.get(id);
	}

	private Map<String, Profile> findProfiles(Set<String> userIds) {
		Map<String, Profile> profiles = new HashMap<String, Profile>();
		if(userIds == null || userIds.isEmpty()) {
			return profiles;
		}

		Map<String, SakaiPerson> sakaiPeople = sakaiPersonManager.getSakaiPersons(userIds, sakaiPersonManager.getUserMutableType());
		
		for(Iterator<String>iter = userIds.iterator(); iter.hasNext();)
		{
			String userId = iter.next();
			if (userId == null || userId.length() < 1)
			{
				LOG.info("Illegal uid argument passed: userId=" + userId);
				continue;
			}
			SakaiPerson sakaiPerson = sakaiPeople.get(userId);
	
			if ((userId != null) && (userId.trim().length() > 0))
			{
				try
				{
					User user = userDirectoryService.getUser(userId);
	
					if (sakaiPerson == null)
					{
						LOG.info("Could not find a sakaiPerson for id=" + user.getId() + ", eid=" + user.getEid());
						sakaiPerson = sakaiPersonManager.create(user.getId(), sakaiPersonManager.getUserMutableType());
						sakaiPeople.put(user.getId(), sakaiPerson);
					}
				}
				catch (UserNotDefinedException e)
				{

					if(LOG.isDebugEnabled()) LOG.debug("Profile requested for nonexistent userid: " + userId);
					// TODO: how to handle this use case with UserDirectoryService? name? email? password? Why even do it? -ggolden
					// User user = userDirectoryService.addUser(sessionManagerUserId, "", sessionManagerUserId, "", "", "", null);
					// sakaiPerson = sakaiPersonManager.create( userId, sakaiPersonManager.getUserMutableType());

				}
			}
			profiles.put(userId, new ProfileImpl(sakaiPerson));
		}
		
		if(LOG.isDebugEnabled()) LOG.debug("Returning profiles for " + profiles.keySet().size() + " users");
		return profiles;
	}
	/**
	 * @return
	 */
	private String getCurrentUserId()
	{
		LOG.debug("getCurrentUser()");
		return SessionManager.getCurrentSession().getUserId();
	}
	
	

}
