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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.podcasts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.api.app.podcasts.PodcastPermissionsService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
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
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

public class PodcastServiceImpl implements PodcastService {
	/** Used to retrieve global podcast title and description from podcast folder collection **/
	private final String PODFEED_TITLE = "podfeedTitle";
	private final String PODFEED_DESCRIPTION = "podfeedDescription";
	
	/** Used to grab the default feed title prefix */
	private final String FEED_TITLE_STRING = "feed_title";

	/** Used to get the default feed description pieces from the message bundle */
	private final String FEED_DESC1_STRING = "feed_desc1";
	private final String FEED_DESC2_STRING = "feed_desc2";
	
	/** Used to pull message bundle */
	private final String PODFEED_MESSAGE_BUNDLE = "org.sakaiproject.api.podcasts.bundle.Messages";
//	private ResourceBundle resbud = ResourceBundle.getBundle(PODFEED_MESSAGE_BUNDLE);
	private ResourceLoader resbud = new ResourceLoader(PODFEED_MESSAGE_BUNDLE);

	/** Used for event tracking of podcasts - adding a podcast **/
	private final String EVENT_ADD_PODCAST = "podcast.add";
	
	/** Used for event tracking of podcasts - revisiong a podcast **/
	private final String EVENT_REVISE_PODCAST = "podcast.revise";
	
	/** Used for event tracking of podcasts - deleting a podcast **/
	private final String EVENT_DELETE_PODCAST = "podcast.delete";

	/** Options. 0 = Display to non-members, 1 = Display to Site * */
	private final int PUBLIC = 0;
	private final int SITE = 1;

	private Log LOG = LogFactory.getLog(PodcastServiceImpl.class);
	
	private Reference siteRef;

	// injected beans
	private ContentHostingService contentHostingService;
	private ToolManager toolManager;
	private SessionManager sessionManager;
	private PodcastPermissionsService podcastPermissionsService;

	// FUTURE; TO BE IMPLEMENTED 
//	private NotificationService notificationService;

	/** Needed for when Notification implemented * */
	protected String m_relativeAccessPoint = null;

	PodcastServiceImpl() {
	}

	/** Injects ContentHostingService into this service **/
	public void setContentHostingService(ContentHostingService chs) {
		this.contentHostingService = chs;
	}

	/** Injects ToolManager into this service **/
	public void setToolManager(ToolManager tm) {
		toolManager = tm;
	}

	/** Injects PodcastAuthzService into this service **/
	public void setPodcastPermissionsService(PodcastPermissionsService podcastPermissionsService) {
		this.podcastPermissionsService = podcastPermissionsService;
	}

	/**
	 * Retrieve the site id
	 */
	public String getSiteId() {
		return toolManager.getCurrentPlacement().getContext();
	}

	/**
	 * Retrieve the current user id
	 */
	public String getUserId() {
		return SessionManager.getCurrentSessionUserId();
	}

	/**
	 * Retrieve the current user display name
	 */
	public String getUserName() {
		return UserDirectoryService.getCurrentUser().getDisplayName();
	}

	/**
	 * Returns the site URL as a string
	 * 
	 * @return 
	 * 		String containing the sites URL
	 */
	public String getSiteURL() {
		return contentHostingService.getEntityUrl(siteRef);
	}

	/**
	 * Returns only those podcasts whose DISPLAY_DATE property is today or earlier
	 * 
	 * @param resourcesList
	 *            List of podcasts
	 * 
	 * @return List 
	 * 			List of podcasts whose DISPLAY_DATE is today or before
	 */
	public List filterPodcasts(List resourcesList) {
		return filterPodcasts(resourcesList, getSiteId());
	}

	/**
	 * Returns only those podcasts whose DISPLAY_DATE property is today or earlier
	 * This version for feed so can pass siteId
	 * 
	 * @param resourcesList
	 *            List of podcasts
	 * 
	 * @return List 
	 * 			List of podcasts whose DISPLAY_DATE is today or before
	 */	
	public List filterPodcasts(List resourcesList, String siteId) {
		List filteredPodcasts = new ArrayList();

		final Time now = TimeService.newTime();

		// loop to check if DISPLAY_DATE has been set. If not, set it
		final Iterator podcastIter = resourcesList.iterator();
		ContentResource aResource = null;
		ResourceProperties itsProperties = null;

		// for each bean
		while (podcastIter.hasNext()) {
			// get its properties from ContentHosting
			aResource = (ContentResource) podcastIter.next();
			itsProperties = aResource.getProperties();

			try {
				Time podcastTime = aResource.getReleaseDate();
				
				if (podcastTime == null) {
					podcastTime = itsProperties.getTimeProperty(DISPLAY_DATE);
				}

				// has it been published or does user have hidden permission
				if (podcastTime.getTime() <= now.getTime() || 
					podcastPermissionsService.hasPerm(PodcastPermissionsService.HIDDEN_PERMISSIONS, 
													  retrievePodcastFolderId(siteId), siteId)) {
					
					// check if there is a retract date and if so, we have not
					// passed it
					final Time retractDate = aResource.getRetractDate();
					if (retractDate == null || retractDate.getTime() >= now.getTime()) {
						filteredPodcasts.add(aResource);
					}
				}
			} 
			catch (Exception e) {
				// catches EntityPropertyNotDefinedException, EntityPropertyTypeException
				// any problems, skip this one
				LOG.warn(e.getMessage() + " for podcast item: " + aResource.getId() + ". SKIPPING...", e);
			} 
		}

		return filteredPodcasts;
	}

	/**
	 * Get ContentCollection object for podcasts
	 * 
	 * @param String
	 *            The siteId to grab the correct podcasts
	 * 
	 * @return ContentCollection The ContentCollection object containing the
	 *         podcasts
	 */
	public ContentCollection getContentCollection(String siteId)
			throws IdUnusedException, PermissionException {

		ContentCollection collection = null;

		try {
			final String podcastsCollection = retrievePodcastFolderId(siteId);

			collection = contentHostingService.getCollection(podcastsCollection);

		} 
		catch (TypeException e) {
			LOG.error("TypeException when trying to get podcast collection for site: "
							+ siteId + ": " + e.getMessage(), e);
			throw new Error(e);

		} 
		catch (IdUnusedException e) {
			LOG.warn("IdUnusedException while attempting to get podcast collection. "
							+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw new Error(e);

		}
		catch (PermissionException e) {
			// catches PermissionException
			LOG.warn("PermissionException when trying to get podcast collection for site: "
							+ siteId + ": " + e.getMessage(), e);
			
			// now group aware, so folder may be restricted to a group
			// so rethrow exception so UI can capture and deal with
			throw e;
		}

		return collection;
	}

	/**
	 * Get ContentCollection object for podcasts
	 * 
	 * @param String
	 *            The siteId to grab the correct podcasts
	 *            
	 * @return ContentCollection 
	 * 			The ContentCollection object containing the podcasts
	 */
	public ContentCollectionEdit getContentCollectionEditable(String siteId)
			throws IdUnusedException, PermissionException, InUseException {

		ContentCollectionEdit collection = null;
		String podcastsCollection = "";

		try {
			podcastsCollection = retrievePodcastFolderId(siteId);

			collection = contentHostingService.editCollection(podcastsCollection);
		} 
		catch (TypeException e) {
			LOG.error("TypeException when trying to get podcast collection for site: "
							+ siteId + ": " + e.getMessage(), e);
			throw new Error(e);

		} 
		catch (IdUnusedException e) {
			LOG.error("IdUnusedException when trying to get podcast collection for edit in site: "
							+ siteId + " " + e.getMessage(), e);
			throw new Error(e);

		} 
		catch (PermissionException e) {
			LOG.error("PermissionException when trying to get podcast collection for edit in site: "
							+ siteId + " " + e.getMessage(), e);
			throw new Error(e);

		} 
		catch (InUseException e) {
			LOG.warn("InUseException attempting to retrieve podcast folder " + podcastsCollection  
							+ " for site: " + siteId + ". " + e.getMessage(), e);
			throw new Error(e);
			
		}

		return collection;
	}

	/**
	 * Returns a reference to Podcasts folder in Resources to pass to
	 * permissions page.
	 */
	public String getPodcastsFolderRef() {
		try {
			ContentCollection podcastFolder = getContentCollection(getSiteId());
		
			return podcastFolder.getReference();
		}
		catch (Exception e) {
			LOG.error("Exception thrown while attempting to retrieve podcast folder reference.", e);
		}
		
		return null;
	}

	/**
	 * Remove non-file resources from list of potential podcasts and
	 * files restricted to groups user not part of
	 * 
	 * @param resourcesList
	 *           The list of potential podcasts
	 * 
	 * @return List 
	 * 			List of files to make up the podcasts
	 */
	public List filterResources(List resourcesList) {
		return filterResources(resourcesList, getSiteId());
	}
	
	public List filterResources(List resourcesList, String siteId) {
		List filteredResources = new ArrayList();
		ContentResource aResource = null;

		// is this user an instructor?
		// need to clear advisors so doesn't blindly return true
		boolean hadAdvisor = false;
		if (SecurityService.hasAdvisors())  {
			SecurityService.clearAdvisors();
			hadAdvisor = true;
		}
		final boolean canUpdateSite = podcastPermissionsService.canUpdateSite(siteId);
		if (hadAdvisor)  enablePodcastSecurityAdvisor();
		
		// loop to check if objects are collections (folders) or resources
		// (files)
		final Iterator podcastIter = resourcesList.iterator();

		// for each bean
		while (podcastIter.hasNext()) {
			// get its properties from ContentHosting
			try {

				 aResource = (ContentResource) podcastIter.next();
				
				if (aResource.isResource()) {
					final boolean isGrouped = podcastPermissionsService.isGrouped(aResource);
					
					if ((! canUpdateSite) && isGrouped) {
							if (podcastPermissionsService.canAccessViaGroups(aResource.getGroups(), siteId)) {
								filteredResources.add(aResource);					
							}
						}
					else {
						filteredResources.add(aResource);
					}
				}
			}
			catch (ClassCastException e) {
				LOG.info("Non-file resource in podcasts folder at site " + siteId + ", so ignoring. ");				
			}
		}

		return filteredResources;
	}

	/**
	 * Returns TRUE if the possible id is the correct one. If they
	 * cannot access an Exception will be thrown.
	 * Created for SAK-13740
	 * @param podcastCollection
	 * @param siteId
	 * @param isStudent
	 * @return
	 */
	private boolean isPodcastsFolderId(String podcastsCollection, String siteId, boolean isStudent) 
						throws TypeException, IdUnusedException, PermissionException {
		// SAK-13740: need to access folder to determine if folder is hidden BUT if user is student
		// and the folder is hidden, PermissionException thrown so SecurityAdvisor enabled. 
		// Need to determine so a WARNing can be logged and not a PermissionException.
		if (isStudent) {
			enablePodcastSecurityAdvisor();
		}
		
		ContentCollection podcastFolder = contentHostingService.getCollection(podcastsCollection);
		
		if (isStudent) {
			Date tempDate = null;
			if (podcastFolder.getRetractDate() != null) {
				tempDate = new Date(podcastFolder.getRetractDate().getTime());
			}

			boolean result = podcastPermissionsService.isResourceHidden(podcastFolder, tempDate);
			SecurityService.clearAdvisors();
		
			if (result) {
			// a student/access user is attempting to access and the folder is 'hidden' so just log the
			// situation and return the String - what should happen sp don't print out stack trace
			LOG.warn("Podcasts folder " + podcastsCollection + " is HIDDEN, before RELEASE DATE, or " +
							"after RETRACT DATE so cannot access.");
			}
			else {
				// not hidden so check without SecurityAdvisor to see if we can access
				podcastFolder = contentHostingService.getCollection(podcastsCollection);				
			}
		}
			
		return true;
	}

	/**
	 * Returns podcast folder id using either 'podcasts' or 'Podcasts'. If it
	 * does not exist in either form, will create it.
	 * 
	 * @param siteId
	 *            	The site to search
	 *            
	 * @return String 
	 * 				Contains the complete id for the podcast folder
	 * 
	 * @throws PermissionException
	 *             Access denied or Not found so not available
	 * @throws IdInvalidException
	 *             Constructed Id not valid
	 * @throws IdUsedException
	 *             When attempting to create Podcast folder, id is a duplicate
	 */
	public String retrievePodcastFolderId(String siteId)
			throws PermissionException {

		final String siteCollection = contentHostingService.getSiteCollection(siteId);
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;

		boolean isStudent = ! podcastPermissionsService.canUpdateSite(siteId);
		// Also refactored to streamline code.
		try {
			if (isPodcastsFolderId(podcastsCollection, siteId, isStudent)) {
				return podcastsCollection;
			}
		}
		catch (TypeException e1) {
			LOG.error("TypeException while trying to determine correct podcast folder Id String "
							+ " for site: " + siteId + ". " + e1.getMessage(), e1);
			throw new Error(e1);
		} 
		catch (IdUnusedException e2) {
			// Podcasts is truly not the name of the folder, so drop through and try another
			podcastsCollection = siteCollection + COLLECTION_PODCASTS_ALT + Entity.SEPARATOR;

			// once again, since we are dealing with a student/access user, if folder is 'hidden'
			// this user can't access, so enable an advisor to determine if it truly does exist
			try {
				if (isPodcastsFolderId(podcastsCollection, siteId, isStudent)) {
					return podcastsCollection;
				}
			} 
			catch (IdUnusedException e) {
				LOG.warn("IdUnusedException while trying to determine correct podcast folder id "
						+ " for site: " + siteId + ". " + e.getMessage());
				
				// if we get here it does not exist so create
				if (isStudent) {
					enablePodcastSecurityAdvisor();
				}
				createPodcastsFolder(siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR, siteId);
			} 
			catch (TypeException e) {
				LOG.error("TypeException while trying to determine correct podcast folder Id String "
						+ " for site: " + siteId + ". " + e.getMessage(), e);
				throw new Error(e);
			}
		}
		finally {
			SecurityService.clearAdvisors();
		}
		
		return null;
	}

	/**
	 * Retrieve Podcasts for site and if podcast folder does not exist, create
	 * it. Used within tool since siteId known
	 * 
	 * @return List
	 * 				A List of podcast resources
	 */
	public List getPodcasts() throws PermissionException, InUseException,
			IdInvalidException, InconsistentException, IdUsedException {
		return getPodcasts(getSiteId());
	
	}

	/**
	 * Retrieve Podcasts for site and if podcast folder does not exist, create
	 * it. Used by feed since no context to pull siteId from
	 * 
	 * @param String
	 *            	The siteId the feed needs the podcasts from
	 * 
	 * @return List
	 * 				A List of podcast resources
	 */
	public List getPodcasts(String siteId) throws PermissionException,
			InUseException, IdInvalidException, InconsistentException,IdUsedException {
		List resourcesList = new ArrayList();
		final String podcastsCollection = retrievePodcastFolderId(siteId);

		try {			
			checkForFeedInfo(podcastsCollection, siteId);

			// Get podcasts folder collection from Resource for this site
			final ContentCollection collectionEdit = getContentCollection(siteId);

			// If not instructor, check if folder is restricted to group access
			//    and if so, if this user does not have access, return empty List
			if (! podcastPermissionsService.canUpdateSite(siteId)
					&& podcastPermissionsService.isGrouped(collectionEdit)
					&& ! podcastPermissionsService.canAccessViaGroups(collectionEdit.getGroups(), siteId)) {
						return new ArrayList();
			}

			resourcesList = collectionEdit.getMemberResources();

			// remove non-file resources from collection as well as
			// those restricted to groups (if user non-instructor)
			resourcesList = filterResources(resourcesList, siteId);

			// if added from Resources will not have this property.
			// if not, this will call a method to set it.
			// returns the revised list of podcasts, suitable for framing (sorting)
			resourcesList = checkDISPLAY_DATE(resourcesList, siteId);

			// sort based on display (publish) date, most recent first
			PodcastComparator podcastComparator = new PodcastComparator(
					DISPLAY_DATE, false);
			Collections.sort(resourcesList, podcastComparator);

		} 
		catch (IdUnusedException ex) {
				// Does not exist, attempt to create it
			if (podcastPermissionsService.canUpdateSite()) {
				createPodcastsFolder(podcastsCollection, siteId);
			}
			else {
				return new ArrayList();
			}
		}

		return resourcesList;
	}

	/**
	 * Pulls a ContentResource from ContentHostingService.
	 * 
	 * @param String
	 *            	The resourceId of the resource to get
	 *            
	 * @return ContentResource
	 * 				If found, null otherwise
	 */
	private ContentResource getAResource(String resourceId)
			throws PermissionException, IdUnusedException {
		ContentResource crEdit = null;

		try {
			crEdit = contentHostingService.getResource(resourceId);

		} 
		catch (TypeException e) {
			LOG.error("TypeException while attempting to pull resource: "
					+ resourceId + " for site: " + getSiteId() + ". " + e.getMessage(), e);
			throw new Error(e);
		}
		
		return crEdit;
	}

	/**
	 * Pulls a ContentResourceEdit from ContentHostingService.
	 * 
	 * @param String
	 *            	The resourceId of the resource to get
	 *            
	 * @return ContentResourceEdit 
	 * 				If found, null otherwise
	 */
	private ContentResourceEdit getAResourceEdit(String resourceId)
			throws PermissionException, IdUnusedException {
		ContentResourceEdit crEdit = null;

		try {
			crEdit = contentHostingService.editResource(resourceId);

		} 
		catch (TypeException e) {
			LOG.error("TypeException while attempting to pull resource: "
					+ resourceId + " for site: " + getSiteId() + ". " + e.getMessage(), e);
			throw new Error(e);
		}
		catch (InUseException e) {
			// Weirdness, should not be in use
			return null;
		}

		return crEdit;
	}

	/**
	 * Add a podcast to the site's resources
	 * 
	 * @param title
	 *            The title of this podcast resource
	 * @param displayDate
	 *            The display date for this podcast resource
	 * @param description
	 *            The description of this podcast resource
	 * @param body
	 *            The bytes of this podcast
	 *            
	 * @throws OverQuotaException
	 * 			 To display Over Quota Alert to user
	 * @throws ServerOverloadException 
	 * 			 To display Internal Server Error Alert to user
	 * @throws InconsistentException
	 * 			 To display Internal Server Error Alert to user
	 * @throws IdInvalidException
	 * 			 To display Invalid Id Alert to user
	 * @throws IdLengthException
	 * 			 To display File path too long Alert to user
	 * @throws PermissionException
	 * 			 To display Permission denied Alert to user
	 * @throws IdUniquenessException
	 * 			 To display Duplicate id used Alert to user
	 */
	public void addPodcast(String title, Date displayDate, String description,
			byte[] body, String filename, String contentType)
			throws OverQuotaException, ServerOverloadException,
			InconsistentException, IdInvalidException, IdLengthException,
			PermissionException, IdUniquenessException {

		final int idVariationLimit = 100;	// actually is checked against
											// if they need more than 100 copies, too bad
		final String resourceCollection = retrievePodcastFolderId(getSiteId());
		
		String basename, extension = "";
		int dot = filename.lastIndexOf('.');
		if (dot != -1) {
			basename = filename.substring(0, dot);
			extension = filename.substring(dot);
		}
		else {
			basename = filename;
		}
		
		// Method: create a resource, fill in its properties,
		//			commit to officially save it
		ContentResourceEdit cr = null;
		try {
			// create the initial object
			cr = contentHostingService.addResource(resourceCollection, basename, extension, idVariationLimit);
		} 
		catch (IdUnusedException e) {
			LOG.error("IdUnusedException trying to add a podcast to Podcasts folder in Resources", e);
			throw new InconsistentException("Could not find the collection " + resourceCollection + " while attempting to " +
					"add the podcast " + filename);
		}
		
		// Add the actual contents of the file and content type
		cr.setContent(body);
		cr.setContentType(contentType);

		// fill up its properties
		final ResourcePropertiesEdit resourceProperties = cr.getPropertiesEdit();

		resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION,
				Boolean.FALSE.toString());

		resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME,
				title);

		resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION,
				description);

		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		formatter.setTimeZone(TimeService.getLocalTimeZone());
		
		resourceProperties.addProperty(DISPLAY_DATE, formatter
				.format(displayDate));

		resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_LENGTH,
				new Integer(body.length).toString());

		// now to commit the changes
		contentHostingService.commitResource(cr, NotificationService.NOTI_NONE);
		
		// add entry for event tracking
		final Event event = EventTrackingService.newEvent(EVENT_ADD_PODCAST,
				getEventMessage(cr.getReference()), true, NotificationService.NOTI_NONE);
		EventTrackingService.post(event);

	}

	/**
	 * Removes a podcast
	 * 
	 * @param id
	 *            The podcast to be removed from ContentHosting
	 */
	public void removePodcast(String resourceId) throws IdUnusedException,
			InUseException, TypeException, PermissionException {

		ContentResourceEdit edit = null;

		edit = contentHostingService.editResource(resourceId);

		contentHostingService.removeResource(edit);

		// add entry for event tracking
		final Event event = EventTrackingService.newEvent(EVENT_DELETE_PODCAST,
				edit.getReference(), true, NotificationService.NOTI_NONE);
		EventTrackingService.post(event);

	}

	/**
	 * Tests whether the podcasts folder exists and create it if it does not
	 * 
	 * @return True - if exists, false - otherwise
	 */
	public boolean checkPodcastFolder() throws PermissionException,
			InUseException {
		return (retrievePodcastFolderId(getSiteId()) != null);

	}

	private boolean anyPodcastsVisible(List podcasts) {
		final List filteredPodcasts = filterResources(podcasts);
		
		return filteredPodcasts != null && ! filteredPodcasts.isEmpty();
	}
	
	/**
	 * Determines if folder contains actual files
	 * 
	 * @return boolean true if files are stored there, false otherwise
	 */
	public boolean checkForActualPodcasts() {		
		try {
			// if student/access user and folder exists but is hidden
			if (! podcastPermissionsService.canUpdateSite() && isPodcastFolderHidden(getSiteId()) ) {
				return false;
			}

			final String podcastsCollection = retrievePodcastFolderId(getSiteId());

			if (podcastsCollection != null) {

				final ContentCollection collection = contentHostingService
						.getCollection(podcastsCollection);
				
				if (collection == null) {
					return false;
				}
				else {
					final List resourcesList = collection.getMemberResources();

					if (resourcesList != null) {
						if (resourcesList.isEmpty())
							return false;
						else if (podcastPermissionsService.canUpdateSite())
							return true;
						else 
							return anyPodcastsVisible(resourcesList);
					} 
					else
						return false;
				}
			} 
		}
		catch (Exception e) {
			// catches IdUnusedException, TypeException, PermissionException
			LOG.warn(e.getMessage() + " while checking for files in podcast folder: "
						+ " for site: " + getSiteId() + ". " + e.getMessage(), e);
				
		}
		
		return false;
	}

	/**
	 * Will apply changes made (if any) to podcast
	 * 
	 * @param String
	 *            The resourceId
	 * @param String
	 *            The title
	 * @param Date
	 *            The display/publish date
	 * @param String
	 *            The description
	 * @param byte[]
	 *            The actual file contents
	 * @param String
	 *            The filename
	 */
	public void revisePodcast(String resourceId, String title, Date date,
			String description, byte[] body, String filename)
			throws PermissionException, InUseException, OverQuotaException,
			ServerOverloadException {

		try {
			// get Resource to modify
			ContentResourceEdit podcastEditable = null;

			podcastEditable = contentHostingService.editResource(resourceId);

			final ResourcePropertiesEdit podcastResourceEditable = podcastEditable
					.getPropertiesEdit();

			if (!title.equals(podcastResourceEditable
								.getProperty(ResourceProperties.PROP_DISPLAY_NAME))) {

				podcastResourceEditable
						.removeProperty(ResourceProperties.PROP_DISPLAY_NAME);

				podcastResourceEditable.addProperty(
						ResourceProperties.PROP_DISPLAY_NAME, title);

			}

			if (!description.equals(podcastResourceEditable
									  .getProperty(ResourceProperties.PROP_DESCRIPTION))) {

				podcastResourceEditable
						.removeProperty(ResourceProperties.PROP_DESCRIPTION);

				podcastResourceEditable.addProperty(
						ResourceProperties.PROP_DESCRIPTION, description);

			}

			if (date != null) {
				podcastResourceEditable.removeProperty(DISPLAY_DATE);

				final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				formatter.setTimeZone(TimeService.getLocalTimeZone());

				podcastResourceEditable.addProperty(DISPLAY_DATE, formatter.format(date));

				podcastEditable.setReleaseDate(TimeService.newTime(date.getTime()));
			}

			// REMOVED SINCE IF FILENAME CHANGED, ENTIRELY NEW RESOURCE CREATED SO THIS CODE SHOULD NEVER BE EXECUTED
/* 			if (!filename.equals(podcastResourceEditable.getProperty(ResourceProperties.PROP_ORIGINAL_FILENAME)))) {
				String oldFilename = podcastResourceEditable.getProperty(ResourceProperties.PROP_ORIGINAL_FILENAME);
				
				podcastResourceEditable.removeProperty(ResourceProperties.PROP_ORIGINAL_FILENAME);

				podcastResourceEditable.addProperty(
						ResourceProperties.PROP_ORIGINAL_FILENAME, Validator
								.escapeResourceName(filename));

				podcastResourceEditable.removeProperty(ResourceProperties.PROP_CONTENT_LENGTH);

				podcastResourceEditable.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, 
														new Integer(body.length).toString());

				// If title = filename, since filename changed, change title to match 
				if (oldFilename == podcastResourceEditable.getProperty(ResourceProperties.PROP_DISPLAY_NAME)) {
					podcastResourceEditable.removeProperty(ResourceProperties.PROP_DISPLAY_NAME);

					podcastResourceEditable.addProperty(ResourceProperties.PROP_DISPLAY_NAME, 
													  Validator.escapeResourceName(filename));
				}
			}
*/
			// Set for no notification. TODO: when notification implemented,
			// need to revisit 2nd parameter.
			contentHostingService.commitResource(podcastEditable,
					NotificationService.NOTI_NONE);

			// add entry for event tracking
			Event event = EventTrackingService.newEvent(EVENT_REVISE_PODCAST,
					podcastEditable.getReference(), true);
			EventTrackingService.post(event);

		}
		catch (IdUnusedException e) {
			LOG.error(e.getMessage() + " while revising podcasts for site: "
					+ getSiteId() + ". ", e);
			throw new Error(e);

		}
		catch (TypeException e) {
			LOG.error(e.getMessage() + " while revising podcasts for site: " 
							+ getSiteId() + ". ", e);
			throw new Error(e);

		}
	}

	/**
	 * Checks if podcast resources have a DISPLAY_DATE set. Occurs when files
	 * uploaded to podcast folder from Resources.
	 * 
	 * @param List
	 *            The list of podcast resources
	 * 
	 * @return List The list of podcast resource all with DISPLAY_DATE property
	 */
	public List checkDISPLAY_DATE(List resourcesList) {
		return checkDISPLAY_DATE(resourcesList, getSiteId());
	}
	
	public List checkDISPLAY_DATE(List resourcesList, String siteId) {
		// recreate the list in case needed to
		// add DISPLAY_DATE to podcast(s)
		List revisedList = new ArrayList();
		
		final Iterator podcastIter = resourcesList.iterator();
		ContentResource aResource = null;
		ResourceProperties itsProperties= null;

		// for each bean
		// loop to check if DISPLAY_DATE has been set. If not, set it
		while (podcastIter.hasNext()) {
			aResource = (ContentResource) podcastIter.next();

			final String id = aResource.getId();  		// save id just in case modified and committed, need to get it back
			itsProperties = aResource.getProperties();  // get its properties from ContentHosting

			try {
				// Release/Retract dates implemented after Podcasts
				// so if null, need to check if old Podcast, ie uses
				// DISPLAY_DATE property.
				// Also, if hidden property set, release date becomes null.
				if (aResource.getReleaseDate() == null) {
					if (itsProperties.getProperty(DISPLAY_DATE) == null) {
						aResource = setDISPLAY_DATE(siteId, aResource.getId(), null);
						itsProperties = aResource.getProperties();
					}
					
					if (! aResource.isHidden()) {
						setReleaseDate(siteId, aResource, itsProperties.getTimeProperty(DISPLAY_DATE));
						
						try {
							aResource = getAResource(id);
						}
						catch (Exception e) {
							LOG.error(e);
						}
					}
				}
				else {
					if (itsProperties.getProperty(DISPLAY_DATE) == null) {
						aResource = setDISPLAY_DATE(siteId, aResource.getId(), null);
						itsProperties = aResource.getProperties();
					}
				}
			} 
			catch (EntityPropertyNotDefinedException e) {
				// DISPLAY_DATE does not exist, add it
				LOG.info("DISPLAY_DATE does not exist for " + aResource.getId() + " attempting to add.");

				try {
					aResource = setDISPLAY_DATE(siteId, aResource.getId(), null);
				
					if (aResource.getReleaseDate() == null && ! aResource.isHidden()) {
						if (! aResource.isHidden()) {
							setReleaseDate(siteId, aResource, itsProperties.getTimeProperty(DISPLAY_DATE));
							aResource = getAResource(id);
						}
					}
				}
				catch (EntityPropertyTypeException e1) {
					// Weirdness, should have just set it
					LOG.debug("EntityPropertyTypeException while trying to set Release Date after" +
									" freshly setting DISPLAY_DATE", e1);
				}
				catch (EntityPropertyNotDefinedException e1) {
					// Weirdness, should have just set it
					LOG.debug("EntityPropertyNotDefinedException while trying to set Release Date after" +
									" freshly setting DISPLAY_DATE", e1);						
				}
				catch (Exception e1) {
					// PermissionException, IdUnusedException from getAResource
					LOG.error(e1);
				}
			} 
			catch (EntityPropertyTypeException e) {
				// not a file, skip over it
				LOG.debug("EntityPropertyTypeException while checking for DISPLAY_DATE. "
							+ " Possible non-resource (aka a folder) exists in podcasts folder. " 
							+ "SKIPPING..." + e.getMessage(), e);

			}
			finally {
				SecurityService.clearAdvisors();
			}

			// aResource values properly set, so add to list
			revisedList.add(aResource);
			
			aResource = null;
		}
		
		return revisedList;
	}

	/**
	 * Sets Release Date property of the podcast resource
	 * 
	 * @param aResource
	 * 				The ContentResource object of the podcast
	 * 
	 * @param displayDate
	 * 				The Time object the Release Date is set to
	 */
	private ContentResource setReleaseDate(ContentResource aResource, Time displayDate) {
		return setReleaseDate(getSiteId(), aResource, displayDate);
	}
	
	private ContentResource setReleaseDate(String siteId, ContentResource aResource, Time displayDate) {
		ContentResource refreshedResource = null;
		ContentResourceEdit aResourceEdit = null;
		
		try {
			aResourceEdit = getAResourceEdit(aResource.getId());

			if (aResourceEdit.getReleaseDate() == null) {
				Time releaseDate = getDISPLAY_DATE(aResourceEdit.getPropertiesEdit());
				
				aResourceEdit.setReleaseDate(releaseDate);

				contentHostingService.commitResource(aResourceEdit, NotificationService.NOTI_NONE);
			
				// add entry for event tracking
				final Event event = EventTrackingService.newEvent(EVENT_REVISE_PODCAST,
						getEventMessage(aResourceEdit.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME), siteId),
											true, NotificationService.NOTI_NONE);
				EventTrackingService.post(event);
			}
		}
		catch (Exception e1) {
			// catches  PermissionException	IdUnusedException
			//			TypeException		InUseException
			LOG.error("Problem getting resource for editing while trying to set DISPLAY_DATE for site " + siteId + ". ", e1);
			
			if (aResourceEdit != null) {
				contentHostingService.cancelResource(aResourceEdit);						
			}			
		} 
		
		try {
			refreshedResource = getAResource(aResource.getId());
		}
		catch (Exception e) {
			// Weirdness since we just used this to update its release date
			LOG.error("Problem retrieving updated podcast resource after adding release date.", e);
		}
		
		return refreshedResource;
	}
	
	/**
	 * Sets the DISPLAY_DATE property to the creation date of the podcast.
	 * Needed if file added using Resources. Time stored is GMT so when pulled
	 * need to convert to local.
	 * 
	 * @param ResourceProperties
	 *            The ResourceProperties that need DISPLAY_DATE added
	 */
	public ContentResource setDISPLAY_DATE(String resourceId, Time releaseDate) {
		return setDISPLAY_DATE(getSiteId(), resourceId, releaseDate);
	}
	
	public ContentResource setDISPLAY_DATE(String siteId, String resourceId, Time releaseDate) {
		ContentResource refreshedResource = null;
		
		final SimpleDateFormat formatterProp = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		formatterProp.setTimeZone(TimeService.getLocalTimeZone());
		Date tempDate = null;

		try {
			ContentResourceEdit aResource = getAResourceEdit(resourceId);
			ResourceProperties rp = aResource.getProperties();

			// Convert GMT time stored by Resources into local time
			if (releaseDate == null) {
				tempDate = formatterProp.parse(rp.getTimeProperty(
							ResourceProperties.PROP_MODIFIED_DATE).toStringLocal());
			}
			else {
				tempDate = new Date(releaseDate.getTime());
			}

			rp.addProperty(DISPLAY_DATE, formatterProp.format(tempDate));

			contentHostingService.commitResource(aResource, NotificationService.NOTI_NONE);
			
			// add entry for event tracking
			final Event event = EventTrackingService.newEvent(EVENT_REVISE_PODCAST,
									getEventMessage(aResource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME), siteId),
										true, NotificationService.NOTI_NONE);
			EventTrackingService.post(event);
		} 
		catch (Exception e) {
			// catches EntityPropertyNotDefinedException
			//         EntityPropertyTypeException, ParseException
			LOG.error(e.getMessage() + " while setting DISPLAY_DATE for "
							+ "file in site " + siteId + ". " + e.getMessage(), e);
			throw new Error(e);
		} 

		try {
			refreshedResource = getAResource(resourceId);
		}
		catch (Exception e) {
			// Weirdness since we just used this to update its release date
			LOG.error("Problem retrieving updated podcast resource after adding release date.", e);
		}
		
		return refreshedResource;
	}

	/**
	 * If Release Date property not set, check if DISPLAY_DATE exists
	 * and if it does, return it so it can be set as the Release Date.
	 * If DISPLAY_DATE does not exist, default to last modified date.
	 * 
	 * @param ResourceProperties
	 *            The ResourceProperties to get DISPLAY_DATE from
	 */
	public Time getDISPLAY_DATE(ResourceProperties rp) {
		final SimpleDateFormat formatterProp = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		formatterProp.setTimeZone(TimeService.getLocalTimeZone());

		Date tempDate = null;

		try {
			// Convert GMT time stored by Resources into local time
			tempDate = formatterProp.parse(rp.getTimeProperty(DISPLAY_DATE).toStringLocal());
			
			return TimeService.newTime(tempDate.getTime());
		}
		catch (Exception e) {
			try {
				tempDate = formatterProp.parse(rp.getTimeProperty(
						ResourceProperties.PROP_MODIFIED_DATE).toStringLocal());

				return TimeService.newTime(tempDate.getTime());
			} 
			catch (Exception e1) {
				// catches EntityPropertyNotDefinedException
				//         EntityPropertyTypeException, ParseException
				LOG.info(e1.getMessage() + " while getting DISPLAY_DATE for "
						+ "file in site " + getSiteId() + ". ", e);
			}
		}

		return null;
	}

	/**
	 * Checks if podcast feed title and description exists and if not, add them
	 * 
	 * @param podcastsCollection
	 * 			The id for the podcasts collection
	 * 
	 * @param siteId
	 * 			The site id
	 */
	private void checkForFeedInfo(String podcastsCollection, String siteId) {
		try {
			final ContentCollection podcasts = contentHostingService.getCollection(podcastsCollection);
		
			final ResourceProperties rp = podcasts.getProperties();
		
			final String podfeedTitle = rp.getProperty(PODFEED_TITLE);

			if (podfeedTitle == null) {
				// Podfeed Title does not exist, so add it
				final ContentCollectionEdit podcastsEdit = contentHostingService.editCollection(podcastsCollection);

				final ResourcePropertiesEdit resourceProperties = podcastsEdit.getPropertiesEdit();
			
				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, COLLECTION_PODCASTS_TITLE);

				resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, COLLECTION_PODCASTS_DESCRIPTION);

				try {
					// Set default feed title and description
					resourceProperties.addProperty(PODFEED_TITLE, SiteService.getSite(siteId).getTitle() + "'s Official Podcasts");

					final String feedDescription = "This is the official podcast for course "
							+ SiteService.getSite(siteId).getTitle() + ". Please check back throughout the semester for updates.";
					resourceProperties.addProperty(PODFEED_DESCRIPTION, feedDescription);
			
					commitContentCollection(podcastsEdit);
				}
				catch (IdUnusedException e) {
					LOG.error("IdUnusedException attempting to get site info to set feed title and description for site " + siteId, e);
				}
			}
		}
		catch (IdUnusedException e) {
			LOG.error("IdUnusedException attempting to retrive podcast folder collection to check "
					+ "if feed info exists for site " + siteId, e);
		}
		catch (TypeException e) {
			LOG.error("TypeException attempting to retrive podcast folder collection to check "
					+ "if feed info exists for site " + siteId, e);
		}
		catch (PermissionException e) {
			LOG.error("PermissionException attempting to retrive podcast folder collection to check "
					+ "if feed info exists for site " + siteId, e);
		}
		catch (InUseException e) {
			LOG.info("InUsedException attempting to retrive podcast folder collection to check "
					+ "if feed info exists for site " + siteId, e);
		}
	}
	
	/**
	 * Returns the file's URL
	 * 
	 * @param String
	 *            The resource Id
	 * 
	 * @return String The URL for the resource
	 */
	public String getPodcastFileURL(String resourceId)
			throws PermissionException, IdUnusedException {

		String Url = null;
		try {
			Url = contentHostingService.getResource(resourceId).getUrl();

		} 
		catch (TypeException e) {
			LOG.error("TypeException while getting the resource " + resourceId
					+ "'s URL. Resource from site " + getSiteId() + ". " + e.getMessage(), e);
			throw new Error(e);
		}

		return Url;
	}

	/**
	 * 	FUTURE: needed to implement Notification services
	 *
	 */
	public void init() {
/*		EntityManager.registerEntityProducer(this, REFERENCE_ROOT);

		m_relativeAccessPoint = REFERENCE_ROOT;

		NotificationEdit edit = notificationService.addTransientNotification();

		edit.setFunction(EVENT_PODCAST_ADD);
		edit.addFunction(EVENT_PODCAST_REVISE);

		edit.setResourceFilter(getAccessPoint(true));

		edit.setAction(new SiteEmailNotificationPodcasts());
*/
	}

	public void destroy() {
	}

	/**
	 * Changes the podcast folder view status (either PUBLIC or SITE)
	 * 
	 * @param boolean
	 *            True means PUBLIC view, FALSE means private
	 */
	public void reviseOptions(boolean option) {

		String podcastsCollection = null;

		try {
			podcastsCollection = retrievePodcastFolderId(getSiteId());

			contentHostingService.setPubView(podcastsCollection, option);

		} 
		catch (PermissionException e) {
			LOG.warn("PermissionException attempting to retrieve podcast folder id "
							+ " for site " + getSiteId() + ". " + e.getMessage(), e);
			throw new Error(e);

		}
	}

	/**
	 * Returns (from content hosting) whether the podcast folder is PUBLIC or
	 * SITE
	 * 
	 * @return int 
	 * 				0 = Display to non-members, 1 = Display to Site
	 */
	public int getOptions() {

		String podcastsCollection = null;
		try {
			podcastsCollection = retrievePodcastFolderId(getSiteId());

			if (isPublic(podcastsCollection)) {
				return PUBLIC;
			} 
			else {
				return SITE;

			}
		} 
		catch (PermissionException e) {
			LOG.warn("PermissionException attempting to retrieve podcast folder id "
							+ " for site " + getSiteId() + ". " + e.getMessage(), e);
			throw new Error(e);
			
		}

	}

	/**
	 * Commits changes to ContentHosting (releases the lock)
	 * 
	 * @param ContentCollectionEdit
	 *            The ContentCollection to be saved
	 */
	public void commitContentCollection(ContentCollectionEdit cce) {
		contentHostingService.commitCollection(cce);

	}

	/**
	 * Cancels attempt at changing this collection (releases the lock)
	 * 
	 * @param cce
	 *            The ContentCollectionEdit that is not to be changed
	 */
	public void cancelContentCollection(ContentCollectionEdit cce) {
		contentHostingService.cancelCollection(cce);
	}

	/**
	 * Returns boolean TRUE = Display to non-members FALSE - Display to Site
	 * 
	 * @param podcastFolderId
	 * 				The podcast folder id to check
	 * 
	 * @return boolean 
	 * 				TRUE - Display to non-members, FALSE - Display to Site
	 */
	public boolean isPublic(String podcastFolderId) {
		return contentHostingService.isPubView(podcastFolderId);
	}

	/**
	 * Returns TRUE if Podcasts folder has HIDDEN property set
	 * OR release date is in the future
	 * OR retract date is in the past
	 * 
	 * 05/08 - enable security advisor since if folder is truly
	 * hidden and a student/access user attempts to access it will
	 * generate a Permissions error
	 */
	public boolean isPodcastFolderHidden(String siteId) 
						throws IdUnusedException, PermissionException {
		enablePodcastSecurityAdvisor();
		ContentCollection podcastFolder = getContentCollection(siteId);
		
		Date tempDate = null;
		if (podcastFolder.getReleaseDate() != null) {
			tempDate = new Date(podcastFolder.getReleaseDate().getTime());
		}

		boolean result = podcastPermissionsService.isResourceHidden(podcastFolder, tempDate);
		SecurityService.clearAdvisors();
		return result;
	}
	
	/**
	 * Creates the podcasts folder in Resources
	 * 
	 * @param podcastsCollection
	 * 				The id to be used for the podcasts folder
	 * 
	 * @param siteId
	 * 				The site id for whom the folder is to be created
	 */
	private void createPodcastsFolder(String podcastsCollection, String siteId) {
		try {
			LOG.info("Could not find podcast folder, attempting to create.");

			ContentCollectionEdit collection = 
						contentHostingService.addCollection(podcastsCollection);
			
			final ResourcePropertiesEdit resourceProperties = collection.getPropertiesEdit();
			
			resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME,
											COLLECTION_PODCASTS_TITLE);

			resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION,
											COLLECTION_PODCASTS_DESCRIPTION);

			// Set default feed title and description
			resourceProperties.addProperty(PODFEED_TITLE,
					SiteService.getSite(siteId).getTitle() + getMessageBundleString(FEED_TITLE_STRING));

			final String feedDescription =  SiteService.getSite(siteId).getTitle()
												+ getMessageBundleString(FEED_DESC1_STRING)
												+ getMessageBundleString(FEED_DESC2_STRING);

			resourceProperties.addProperty(PODFEED_DESCRIPTION,
					feedDescription);

			contentHostingService.commitCollection(collection);

			contentHostingService.setPubView(collection.getId(), true);
		} 
		catch (Exception e) {
			// catches	IdUnusedException, 		TypeException
			//			InconsistentException,	IdUsedException
			//			IdInvalidException		PermissionException
			//			InUseException
			LOG.error(e.getMessage() + " while attempting to create Podcasts folder: "
							+ " for site: " + siteId + ". NOT CREATED... " + e.getMessage(), e);
			throw new Error(e);
		}
	}

	/**
	 * Returns the date set in GMT time
	 * 
	 * @param date
	 * 			The date represented as a long value
	 * 
	 * @return Date
	 * 			The Date object set in GMT time
	 */
	public Date getGMTdate(long date) {
		final Calendar cal = Calendar.getInstance(TimeService.getLocalTimeZone());
		cal.setTimeInMillis(date);
		
		int gmtoffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
		
		return new Date(date - gmtoffset);
	}

	/**
	 * Generates a message for EventTracking
	 * 
	 * @param object
	 * 			The object that is part of the event
	 * 
	 * @return
	 * 			The String to be used to post the event
	 */
	private String getEventMessage(Object object) {
		return getEventMessage(object, getSiteId());
	}
	
	private String getEventMessage(Object object, String siteId) {
		return "/content/group/" + siteId + "/Podcasts/" + getCurrentUser() + Entity.SEPARATOR + object.toString();
	}
	
	private String getCurrentUser() {        
		return sessionManager.getCurrentSessionUserId();
	}    

	/**
	 * Determines if authenticated user has 'read' access to podcast collection folder
	 * 
	 * @param id
	 * 			The id for the podcast collection folder
	 * 
	 * @return
	 * 		TRUE - has read access, FALSE - does not
	 */
	public boolean allowAccess(String id) {
		return podcastPermissionsService.allowAccess(id);
	}

	/**
	 * Sets the Faces error message by pulling the message from the
	 * MessageBundle using the name passed in
	 * 
	 * @param key
	 *           The name in the MessageBundle for the message wanted
	 *            
	 * @return String
	 * 			The string that is the value of the message
	 */
	private String getMessageBundleString(String key) {
		return resbud.getString(key);
	}
	
	/**
	 * Establish a security advisor to allow the "embedded" azg work to occur
	 * with no need for additional security permissions.
	 * Kept here since don't want to make public.
	 */
	protected void enablePodcastSecurityAdvisor() {
		// put in a security advisor so we can do our podcast work without need
		// of further permissions
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}

}