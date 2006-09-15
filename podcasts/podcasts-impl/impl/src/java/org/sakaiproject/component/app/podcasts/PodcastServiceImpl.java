/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
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
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Validator;

public class PodcastServiceImpl implements PodcastService {
	/** Used to retrieve global podcast title and description from podcast folder collection **/
	private final String PODFEED_TITLE = "podfeedTitle";
	private final String PODFEED_DESCRIPTION = "podfeedDescription";
	
	/** Used for event tracking of podcasts - adding a podcast **/
	private final String EVENT_ADD_PODCAST = "podcast.add";
	
	/** Used for event tracking of podcasts - revisiong a podcast **/
	private final String EVENT_REVISE_PODCAST = "podcast.revise";
	
	/** Used for event tracking of podcasts - deleting a podcast **/
	private final String 	EVENT_DELETE_PODCAST = "podcast.delete";

	/** Options. 0 = Display to non-members, 1 = Display to Site * */
	private final int PUBLIC = 0;
	private final int SITE = 1;

	private Log LOG = LogFactory.getLog(PodcastServiceImpl.class);

	private Reference siteRef;

	// injected beans
	private ContentHostingService contentHostingService;
	private ToolManager toolManager;
	private SessionManager sessionManager;

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
				final Time podcastTime = itsProperties.getTimeProperty(DISPLAY_DATE);

				if (podcastTime.getTime() <= now.getTime()) {
					filteredPodcasts.add(aResource);

				}
			} 
			catch (Exception e) {
				// catches EntityPropertyNotDefinedException, EntityPropertyTypeException
				// any problems, skip this one
				LOG.warn("EntityPropertyNotDefinedException for podcast item: "
						+ aResource + ". SKIPPING...", e);

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

			collection = contentHostingService
					.getCollection(podcastsCollection);

		} 
		catch (TypeException e) {
			LOG.error("TypeException when trying to get podcast collection for site: "
							+ siteId + ": " + e.getMessage(), e);
			throw new Error(e);

		} 
		catch (IdUnusedException e) {
			LOG.warn("IdUnusedException while attempting to get podcast collection. "
							+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw e;

		}
		catch (PermissionException e) {
			// catches PermissionException, IdUnusedException 
			LOG.warn("PermissionException when trying to get podcast collection for site: "
							+ siteId + ": " + e.getMessage(), e);
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

			collection = contentHostingService
					.editCollection(podcastsCollection);

		} 
		catch (TypeException e) {
			LOG.error("TypeException when trying to get podcast collection for site: "
							+ siteId + ": " + e.getMessage(), e);
			throw new Error(e);

		} 
		catch (IdUnusedException e) {
			LOG.error("IdUnusedException when trying to get podcast collection for edit in site: "
							+ siteId + " " + e.getMessage(), e);
			throw e;

		} 
		catch (PermissionException e) {
			LOG.error("PermissionException when trying to get podcast collection for edit in site: "
							+ siteId + " " + e.getMessage(), e);
			throw e;

		} 
		catch (InUseException e) {
			LOG.warn("InUseException attempting to retrieve podcast folder " + podcastsCollection  
							+ " for site: " + siteId + ". " + e.getMessage(), e);
			throw e;
			
		}

		return collection;
	}

	/**
	 * Remove on file resources from list of potential podcasts
	 * 
	 * @param resourcesList
	 *           The list of potential podcasts
	 * 
	 * @return List 
	 * 			List of files to make up the podcasts
	 */
	public List filterResources(List resourcesList) {
		List filteredResources = new ArrayList();
		ContentResource aResource = null;

		// loop to check if objects are collections (folders) or resources
		// (files)
		final Iterator podcastIter = resourcesList.iterator();

		// for each bean
		while (podcastIter.hasNext()) {
			// get its properties from ContentHosting
			try {

				 aResource = (ContentResource) podcastIter.next();
				
				if (aResource.isResource()) {
					filteredResources.add(aResource);
				
				}
			} 
			catch (ClassCastException e) {
				LOG.info("Non-file resource in podcasts folder, so ignoring. ");
				
			}

		}

		return filteredResources;
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

		final String siteCollection = contentHostingService
				.getSiteCollection(siteId);
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS
				+ Entity.SEPARATOR;

		try {
			contentHostingService.checkCollection(podcastsCollection);

			return podcastsCollection;

		} 
		catch (PermissionException e) {
			// Sometimes it converts an IdUnusedException into a permission
			// exception so try this. Have tried 'Podcasts', now try 'podcasts'
			// If PermissionException thrown again, pass it along
			podcastsCollection = siteCollection + COLLECTION_PODCASTS_ALT
					+ Entity.SEPARATOR;

			try {
				contentHostingService.checkCollection(podcastsCollection);

				return podcastsCollection;

			} 
			catch (TypeException e1) {
				LOG.error("TypeException while trying to determine correct podcast folder Id String "
								+ " for site: " + siteId + ". " + e1.getMessage(), e1);
				throw new Error(e);

			} 
			catch (IdUnusedException e1) {
				LOG.warn("IdUnusedException while trying to determine correct podcast folder id "
								+ " for site: " + siteId + ". " + e1.getMessage(), e1);

			} 
			catch (PermissionException e1) {
				// If thrown here, it truly is a PermissionException
				LOG.warn("PermissionException while trying to determine correct podcast folder Id String "
								+ " for site: " + siteId + ". " + e1.getMessage(), e1);
				throw e1;

			}
		} 
		catch (IdUnusedException e) {
			// 'Podcasts' - no luck, try 'podcasts'
			podcastsCollection = siteCollection + COLLECTION_PODCASTS_ALT
					+ Entity.SEPARATOR;

			try {
				contentHostingService.checkCollection(podcastsCollection);

				return podcastsCollection;

			} 
			catch (IdUnusedException e1) {
				// Does not exist, so try to create it
				podcastsCollection = siteCollection + COLLECTION_PODCASTS
				+ Entity.SEPARATOR;
				
				createPodcastsFolder(podcastsCollection, siteId);
				
				return podcastsCollection;

			}
			catch (PermissionException e1) {
				// so try this. If PermissionException thrown again, there is
				// truly a problem.
				LOG.warn("PermissionException thrown on second attempt at retrieving podcasts folder. "
							+ " for site: " + siteId + ". " + e1.getMessage(), e1);
				throw e1;

			} 
			catch (TypeException e1) {
				LOG.error("TypeException while getting podcasts folder using 'Podcasts' string: "
								+ e1.getMessage(), e1);
				throw new Error(e);

			}
		} 
		catch (TypeException e) {
			LOG.error("TypeException while getting podcasts folder using 'podcasts' string: "
							+ e.getMessage(), e);
			throw new Error(e);

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
			InUseException, IdInvalidException, InconsistentException,
			IdUsedException {

		List resourcesList = null;
		final String podcastsCollection = retrievePodcastFolderId(siteId);

		try {

			// just in case anything added from Resources so it does
			// not have DISPLAY_DATE property
			final ContentCollection collectionEdit = getContentCollection(siteId);
			
			resourcesList = collectionEdit.getMemberResources();

			// remove non-file resources from collection
			resourcesList = filterResources(resourcesList);

			// if added from Resources will not have this property.
			// if not, this will call a method to set it.
			resourcesList = checkDISPLAY_DATE(resourcesList);

			// sort based on display (publish) date, most recent first
			PodcastComparator podcastComparator = new PodcastComparator(
					DISPLAY_DATE, false);
			Collections.sort(resourcesList, podcastComparator);

		} 
		catch (IdUnusedException ex) {
				// Does not exist, attempt to create it
				createPodcastsFolder(podcastsCollection, siteId);
		}

		return resourcesList;
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
	public ContentResourceEdit getAResource(String resourceId)
			throws PermissionException, IdUnusedException {
		ContentResourceEdit crEdit = null;

		try {
			crEdit = (ContentResourceEdit) contentHostingService
					.getResource(resourceId);

		} 
		catch (TypeException e) {
			LOG.error("TypeException while attempting to pull resource: "
					+ resourceId + " for site: " + getSiteId() + ". " + e.getMessage());
			throw new Error(e);

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

		final int idVariationLimit = 0;
		
		final ResourcePropertiesEdit resourceProperties = contentHostingService
				.newResourceProperties();

		final String resourceCollection = retrievePodcastFolderId(getSiteId());

		resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION,
				Boolean.FALSE.toString());

		resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME,
				title);

		resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION,
				description);

		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		resourceProperties.addProperty(DISPLAY_DATE, formatter
				.format(displayDate));

		resourceProperties.addProperty(
				ResourceProperties.PROP_ORIGINAL_FILENAME, filename);

		resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_LENGTH,
				new Integer(body.length).toString());

		// TODO: change NotificationService based on user input
		contentHostingService.addResource(Validator
				.escapeResourceName(filename), resourceCollection, idVariationLimit,
				contentType, body, resourceProperties,
				NotificationService.NOTI_NONE);
		
		// add entry for event tracking
		final Event event = EventTrackingService.newEvent(EVENT_ADD_PODCAST,
				getEventMessage(filename), true, NotificationService.NOTI_NONE);
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

	/**
	 * Determines if folder contains actual files
	 * 
	 * @return boolean true if files are stored there, false otherwise
	 */
	public boolean checkForActualPodcasts() {

		try {
			final String podcastsCollection = retrievePodcastFolderId(getSiteId());

			if (podcastsCollection != null) {

				final ContentCollection collection = contentHostingService
						.getCollection(podcastsCollection);

				final List resourcesList = collection.getMemberResources();

				if (resourcesList != null) {
					if (resourcesList.isEmpty())
						return false;
					else
						return true;
				} else
					return false;

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

				final SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");

				podcastResourceEditable.addProperty(DISPLAY_DATE, formatter
						.format(date));

			}

			if (!filename.equals(podcastResourceEditable
					.getProperty(ResourceProperties.PROP_ORIGINAL_FILENAME))) {

				podcastResourceEditable
						.removeProperty(ResourceProperties.PROP_ORIGINAL_FILENAME);

				podcastResourceEditable.addProperty(
						ResourceProperties.PROP_ORIGINAL_FILENAME, Validator
								.escapeResourceName(filename));

				podcastResourceEditable
						.removeProperty(ResourceProperties.PROP_CONTENT_LENGTH);

				podcastResourceEditable.addProperty(
						ResourceProperties.PROP_CONTENT_LENGTH, new Integer(
								body.length).toString());

				podcastResourceEditable
						.removeProperty(ResourceProperties.PROP_DISPLAY_NAME);

				podcastResourceEditable.addProperty(
						ResourceProperties.PROP_DISPLAY_NAME, Validator
								.escapeResourceName(filename));
			}

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
		// recreate the list in case needed to
		// add DISPLAY_DATE to podcast(s)
		List revisedList = new ArrayList();
		
		final Iterator podcastIter = resourcesList.iterator();
		ContentResource aResource = null;
		ResourceProperties itsProperties= null;

		// for each bean
		// loop to check if DISPLAY_DATE has been set. If not, set it
		while (podcastIter.hasNext()) {
			// get its properties from ContentHosting
			aResource = (ContentResource) podcastIter.next();
			
			itsProperties = aResource.getProperties();

			try {
				itsProperties.getTimeProperty(DISPLAY_DATE);
				
				revisedList.add(aResource);

			} 
			catch (EntityPropertyNotDefinedException e) {
				// DISPLAY_DATE does not exist, add it
				LOG.info("DISPLAY_DATE does not exist for " + aResource.getId() + " attempting to add.");

				ContentResourceEdit aResourceEdit = null;
				try {
					aResourceEdit = contentHostingService.editResource(aResource.getId()); 
					
					setDISPLAY_DATE(aResourceEdit.getPropertiesEdit());
					
					contentHostingService.commitResource(aResourceEdit, NotificationService.NOTI_NONE);
					
					revisedList.add(aResourceEdit);
					
				} catch (Exception e1) {
					// catches   PermissionException	IdUnusedException
					//			TypeException		InUseException
					LOG.error("Problem getting resource for editing while trying to set DISPLAY_DATE for site " + getSiteId());
					
					if (aResourceEdit != null) {
						contentHostingService.cancelResource(aResourceEdit);						
					}
					
					throw new Error(e);
				} 
				
			} 
			catch (EntityPropertyTypeException e) {
				// not a file, skip over it
				LOG.debug("EntityPropertyTypeException while checking for DISPLAY_DATE. "
							+ " Possible non-resource (aka a folder) exists in podcasts folder. " 
							+ "SKIPPING..." + e.getMessage());

			}
		}

		return revisedList;
	}

	/**
	 * Sets the DISPLAY_DATE property to the creation date of the podcast.
	 * Needed if file added using Resources. Time stored is GMT so when pulled
	 * need to convert to local.
	 * 
	 * @param ResourceProperties
	 *            The ResourceProperties that need DISPLAY_DATE added
	 */
	public void setDISPLAY_DATE(ResourceProperties rp) {
		final SimpleDateFormat formatterProp = new SimpleDateFormat(
				"yyyyMMddHHmmssSSS");

		Date tempDate = null;

		try {
			// Convert GMT time stored by Resources into local time
			tempDate = formatterProp.parse(rp.getTimeProperty(
					ResourceProperties.PROP_MODIFIED_DATE).toStringLocal());

			rp.addProperty(DISPLAY_DATE, formatterProp.format(tempDate));

		} 
		catch (Exception e) {
			// catches EntityPropertyNotDefinedException
			//         EntityPropertyTypeException, ParseException
			LOG.error(e.getMessage() + " while setting DISPLAY_DATE for "
							+ "for file in site " + getSiteId() + ". " + e.getMessage(), e);
			throw new Error(e);

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
					+ "'s URL. Resource from site " + getSiteId() + ". " + e.getMessage());
			throw new Error(e);

		}

		return Url;
	}

	/**
	 * Determine whether user and update the site
	 * 
	 * @param siteId
	 *            	The siteId for the site to test
	 * 
	 * @return True 
	 * 				True if can update, False otherwise
	 */
	public boolean canUpdateSite(String siteId) {
		return SecurityService.unlock(UPDATE_PERMISSIONS, "/site/"+ siteId);

	}

	/**
	 * Determine whether user and update the site
	 * 
	 * @param siteId
	 *           The siteId for the site to test
	 * 
	 * @return 
	 * 			True if can update, False otherwise
	 */
	public boolean canUpdateSite() {
		return canUpdateSite(getSiteId());

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
							+ " for site " + getSiteId() + ". " + e.getMessage());
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

			} else {
				return SITE;

			}
		} 
		catch (PermissionException e) {
			LOG.warn("PermissionException attempting to retrieve podcast folder id "
							+ " for site " + getSiteId() + ". " + e.getMessage());
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

			final ResourcePropertiesEdit resourceProperties = contentHostingService
					.newResourceProperties();

			resourceProperties.addProperty(
					ResourceProperties.PROP_DISPLAY_NAME,
					COLLECTION_PODCASTS_TITLE);

			resourceProperties.addProperty(
					ResourceProperties.PROP_DESCRIPTION,
					COLLECTION_PODCASTS_DESCRIPTION);

			// Set default feed title and description
			resourceProperties.addProperty(PODFEED_TITLE,
					"Podcasts for " + SiteService.getSite(siteId).getTitle());

			final String feedDescription = "This is the official podcast for course "
					+ SiteService.getSite(siteId).getTitle()
					+ ". Please check back throughout the semester for updates.";

			resourceProperties.addProperty(PODFEED_DESCRIPTION,
					feedDescription);

			ContentCollection collection = contentHostingService
					.addCollection(podcastsCollection,
							resourceProperties);

			contentHostingService.setPubView(collection.getId(), true);

			ContentCollectionEdit edit = contentHostingService
					.editCollection(collection.getId());

			commitContentCollection(edit);

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
		final Calendar here = Calendar.getInstance();
		final int gmtoffset = here.get(Calendar.DST_OFFSET) + here.get(Calendar.ZONE_OFFSET);

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
		return "/content/group/podcast/" + getCurrentUser() + Entity.SEPARATOR + object.toString();
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
		return contentHostingService.allowGetCollection(id);
	}
}