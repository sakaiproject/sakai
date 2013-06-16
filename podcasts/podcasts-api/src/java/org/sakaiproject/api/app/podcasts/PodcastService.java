/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.api.app.podcasts;

import java.util.Date;
import java.util.List;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.ToolManager;

// import org.sakaiproject.entity.api.EntityProducer;

public interface PodcastService // extends EntityProducer
{
	/** This string can be used to find the service in the service manager. */
	public static final String COLLECTION_PODCASTS = "Podcasts";
	
	/** This string can be used as an alternate method to finding the podcasts folder */
	public static final String COLLECTION_PODCASTS_ALT = "podcasts";
	
	/** This string used as part of URL for podcast feed **/
	public static final String COLLECTION_PODCASTS_FEED = "podcastsFeed";
	
	/** This string used for Title of the podcast collection **/
	public static final String COLLECTION_PODCASTS_TITLE = "Podcasts";
	
	/** This string gives description for podcasts folder **/
	public static final String COLLECTION_PODCASTS_DESCRIPTION = "Common Folder for All Site Podcasts";

	/** This string gives the Service name (class w/ package prefix) for podcast service **/
	public static final String PODCASTS_SERVICE_NAME = "org.sakaiproject.api.app.podcasts.PodcastService";

	/** This string is the name of the property used when displaying and sorting the podcasts **/
	public static final String DISPLAY_DATE = "displayDate";
	
	/** This string is the name of the property for the title of a podcast in the feed **/
	public static final String DISPLAY_TITLE = "displayTitle";
	
	/** This string gives the update function (permission) string for checking permissions
	public static final String UPDATE_PERMISSIONS = "site.upd";
	public static final String NEW_PERMISSIONS = "content.new";
	public static final String READ_PERMISSIONS = "content.read";
	public static final String REVISE_ANY_PERMISSIONS = "content.revise.any";
	public static final String REVISE_OWN_PERMISSIONS = "content.revise.own";
	public static final String DELETE_ANY_PERMISSIONS = "content.delete.any";
	public static final String DELETE_OWN_PERMISSIONS = "content.delete.own";
	public static final String ALL_GROUPS_PERMISSIONS = "content.all.groups";
	public static final String HIDDEN_PERMISSIONS = "content.hidden";
 **/
	/**
	 * Determines if podcast folder is part of Resources of site.
	 * If not, creates it.
	 * 
	 * @return true if folder exists/created, false otherwise.
	 */
	public boolean checkPodcastFolder () throws InUseException, PermissionException;

	/**
	 * Will check if any podcasts were added in Resources and do not have their DISPLAY_DATE property set.
	 * 
	 * @param resourcesList The List of podcasts to check
	 * 
	 * @return List of updated podcasts
	 */
	public List checkDISPLAY_DATE(List resourcesList);

	/**
	 * Determines if there are actual podcasts in the folder
	 * 
	 * @return true if there are actual podcasts, false otherwise
	 */
	public boolean checkForActualPodcasts() throws PermissionException;

	/**
	 * Used to inject the ContentHostingService
	 * 
	 * @param chs The application's ContentHostingService
	 */
	public void setContentHostingService(ContentHostingService chs);
	
	/**
	 * Used to inject the ToolManager
	 * 
	 * @param tm The application's ToolManager
	 */
	public void setToolManager(ToolManager tm);
	
	/**
	 * Returns a list of the podcasts stored in site/podcasts folder
	 * 
	 * @return List of podcasts
	 */
	public List getPodcasts()throws PermissionException, InUseException, IdInvalidException, 
					InconsistentException, IdUsedException;
	
	/**
	 * Returns a list of the podcasts stored in site/podcasts folder
	 * 
	 * @param siteID Passed in by the podfeed server
	 * 
	 * @return List of podcasts
	 * 
	 * @throws PermissionException
	 * @throws InUseException
	 * @throws IdInvalidException
	 * @throws InconsistentException
	 * @throws IdUsedException
	 */
	public List getPodcasts(String siteID)throws PermissionException, InUseException, IdInvalidException, 
					InconsistentException, IdUsedException;
	
	/**
	 * Returns SiteId for the site this tool is a part of
	 * 
	 * @return String of the site id
	 */
	public String getSiteId();
	
	/**
	 * Returns UserId for the current user
	 * 
	 * @return String of the user ID
	 */
	public String getUserId();
	
	/**
	 * Returns user display name for current user
	 * 
	 * @return String of the user display name
	 */
	public String getUserName();
	
	/**
	 * Returns the full URL of the file from ContentHostingService
	 * 
	 * @param resourceId The ID for the file whose URL is wanted
	 * 
	 * @return String The full URL for the file
	 */
	public String getPodcastFileURL(String resourceId) throws PermissionException, IdUnusedException;

	/**
	 * Returns a reference to the Podcasts folder in Resources. 
	 * Used to display permissions page.
	 */
	public String getPodcastsFolderRef();
	
	/**
	 * Does the actual adding of podcast to Resources
	 * 
	 * @param title User specified title for the podcast
	 * @param displayDate Date when podcast will be available for viewing
	 * @param description User specified description for the podcast
	 * @param body The actual contents of the podcast
	 * @param filename The filename of the podcast being saved
	 */
	public void addPodcast(String title, Date displayDate, String description, byte[] body, 
			               String filename, String contentType) throws OverQuotaException, ServerOverloadException, InconsistentException,
			               IdInvalidException, IdLengthException, PermissionException, IdUniquenessException;
	
	/**
	 * Removes a podcast from site/podcasts folder
	 * 
	 * @param resourceId resourceId of the podcast to be deleted
	 */
	public void removePodcast(String resourceId)
		throws IdUnusedException, InUseException, TypeException, PermissionException; 
	
	/**
	 * Returns an editable resource if ID exists.
	 * 
	 * @return ContentResourceEdit object if ID valid, null otherwise
	 */
	public void revisePodcast(String resourceId, String title, Date date, String description, byte[] body, 
            String filename) throws PermissionException, InUseException, OverQuotaException, ServerOverloadException;
	
	/**
	 * Returns the ContentCollection that contains the podcasts.
	 * 
	 * @param siteId The site id to pull the correct collection
	 * 
	 * @return ContentCollection
	 */
	public ContentCollection getContentCollection(String siteId) throws IdUnusedException, PermissionException;
	
	/**
	 * Returns the ContentCollection that contains the podcasts.
	 * 
	 * @param siteId The site id to pull the correct collection
	 * 
	 * @return ContentCollection
	 */
	public ContentCollectionEdit getContentCollectionEditable(String siteId) throws IdUnusedException, PermissionException, InUseException;
	
	/**
	 * Changes whether the podcast folder is public/private
	 * 
	 * @param option int that sets the public/private option
	 */
	public void reviseOptions(boolean option);

	/**
	 * Only add podcast resources whose DISPLAY_DATE is today or earlier
	 * 2 parameter for filtering podcasts for feed. First method is
	 * when activated from within Podcasts tool, second when coming from
	 * servlet so need to pass in site id wanted.
	 * 
	 * @param resourcesList List of podcasts
	 * 
	 * @return List of podcasts whose DISPLAY_DATE is today or before
	 */
	public List filterPodcasts(List resourcesList);
	public List filterPodcasts(List resourcesList, String siteId);

	/**
	 * Gets whether the podcast folder is Publicly viewable or not.
	 * 
	 * @return int 0 = Public 1 = Site 
	 */
	public int getOptions();
	
	/**
	 * Commit the changes made to the ContentCollection.
	 * 
	 * @param contentCollectionEdit The ContentCollection object that needs to be commited.
	 */
	public void commitContentCollection(ContentCollectionEdit contentCollectionEdit);
	
	/**
	 * Cancels attempt at changing this collection (releases the lock)
	 * 
	 * @param cce
	 *            The ContentCollectionEdit that is not to be changed
	 */
	public void cancelContentCollection(ContentCollectionEdit cce);

	/**
	 * Returns podcast folder id using either 'podcasts' or 'Podcasts'. If it
	 * does not exist in either form, will create it.
	 * 
	 * @param siteId
	 *            The site to search
	 * @return String containing the complete id for the podcast folder
	 * 
	 * @throws PermissionException
	 */
	public String retrievePodcastFolderId(String siteId)
			throws PermissionException; 

	/**
	 * Returns whether site is Public (true) or Site (false)
	 * @param siteId
	 * @return
	 */
	public boolean isPublic(String siteId);
	
	/**
	 * Takes the date String passed in and converts it to a Date object
	 * using GMT time
	 * 
	 * @param date
	 * 			A long value representing the date to be converted`
	 * 
	 * @return
	 * 			Date object created
	 */
	public Date getGMTdate(long date);

	/**
	 * Determines if authenticated user has 'read' access to podcast collection folder
	 * 
	 * @param id
	 * 			The id for the podcast collection folder
	 * 
	 * @return
	 * 		TRUE - has read access, FALSE - does not
	 */
	public boolean allowAccess(String id);

	/**
	 * Returns TRUE if Podcasts folder has been set to HIDDEN.
	 * 
	 * @param siteId
	 * 			The site id to check
	 */
	public boolean isPodcastFolderHidden(String siteId)
			throws IdUnusedException, PermissionException;
	
	/**
	 * Check if an option should be presented to the user.
	 * @see #reviseOptions(boolean)
	 * @see #getOptions()
	 */
	public boolean allowOptions(int option);
}