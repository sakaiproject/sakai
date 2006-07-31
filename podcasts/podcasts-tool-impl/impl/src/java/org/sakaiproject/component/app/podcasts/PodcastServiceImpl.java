/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/syllabus/tags/sakai_2-2-001/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/SyllabusService.java $
 * $Id: SyllabusService.java 8802 2006-05-03 15:06:26Z josrodri@iupui.edu $
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
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.authz.api.SecurityService;
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
import org.sakaiproject.event.api.NotificationService;
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
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Validator;

public class PodcastServiceImpl implements PodcastService
{
	private ContentHostingService contentHostingService;
	private ToolManager toolManager;
	private Log LOG = LogFactory.getLog(PodcastServiceImpl.class);
	private Reference siteRef;
	private PodcastComparator podcastComparator;
	private SecurityService securityService;
	private UserDirectoryService userDirectoryService;
	
	PodcastServiceImpl() {
	}

	public void setContentHostingService( ContentHostingService chs) {
		this.contentHostingService = chs;
	}
	
	public void setToolManager( ToolManager tm) {
		toolManager = tm;
	}

	/**
	 * @param securityService The securityService to set.
	 */
	public void setSecurityService(SecurityService ss) {
		securityService = ss;
	}

	/**
	 * @param userDirectoryService The userDirectoryService to set.
	 */
	public void setUserDirectoryService(UserDirectoryService uds) {
		userDirectoryService = uds;
	}

	/**
	 * Retrieve the site id
	 */
	public String getSiteId() {
		return toolManager.getCurrentPlacement().getContext();
	}
	
	public String getUserId() {
		return SessionManager.getCurrentSessionUserId();
	}

	/**
	 * Returns the site URL as a string
	 * 
	 * @return String containing the sites URL
	 */
	public String getSiteURL() {
		return contentHostingService.getEntityUrl(siteRef);
	}

	/**
	 * Retrieve Podcasts for site and if podcast folder does not exist,
	 * create it.
	 * 
	 * @return A List of podcast resources
	 */
	public List getPodcasts() throws PermissionException, InUseException, IdInvalidException, 
					InconsistentException, IdUsedException {
		List resourcesList = null;
		
		String siteCollection = contentHostingService.getSiteCollection( getSiteId() );
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;

		try {
			ContentCollection collection = contentHostingService.getCollection(podcastsCollection);

			resourcesList = collection.getMemberResources();
			
			checkDISPLAY_DATE(resourcesList);
			
			PodcastComparator podcastComparator = new PodcastComparator(DISPLAY_DATE, false);
			
			Collections.sort(resourcesList, podcastComparator);

			// call a sort using Collections.sort(comparator, resourcesList);
		}
		catch (IdUnusedException ex) {
			try {
				// Podcasts folder does not exist, create it and return a null list
				System.out.println("Attempting to create podcasts folder.");
				ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();
				
				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, COLLECTION_PODCASTS_TITLE);
				resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, COLLECTION_PODCASTS_DESCRIPTION);
				
				ContentCollection collection = contentHostingService.addCollection(podcastsCollection, resourceProperties);
				
				contentHostingService.setPubView(collection.getId(), true);
				
				ContentCollectionEdit edit = contentHostingService.editCollection(collection.getId());

				contentHostingService.commitCollection(edit);

				// TODO: create an empty resourceList instead of null?
			}
			catch (IdUnusedException e) {
				// TODO: handle if necessary
				LOG.warn("IdUnusedException: " + e.getMessage(), e);
			}
			catch (TypeException e) {
				LOG.warn("TypeException: " + e.getMessage(), e);
			}
			
		}
		catch (TypeException e) {
			LOG.warn("TypeException: " + e.getMessage(), e);
		}
		
		return resourcesList;
	}
	
	/**
	 * Retrieve Podcasts for site and if podcast folder does not exist,
	 * create it. Used by feed since no context to pull siteID from
	 * 
	 * @param String The siteID the feed needs the podcasts from
	 * 
	 * @return A List of podcast resources
	 */
	public List getPodcasts(String siteID) throws PermissionException, InUseException, IdInvalidException, 
					InconsistentException, IdUsedException {
		List resourcesList = null;
		
		String siteCollection = contentHostingService.getSiteCollection( siteID );
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;

		try {
			ContentCollection collection = contentHostingService.getCollection(podcastsCollection);

			resourcesList = collection.getMemberResources();
			
			checkDISPLAY_DATE(resourcesList);
			
			PodcastComparator podcastComparator = new PodcastComparator(DISPLAY_DATE, false);
			
			// call a sort using Collections.sort(comparator, resourcesList);
			Collections.sort(resourcesList, podcastComparator);

		}
		catch (IdUnusedException ex) {
			try {
				// Podcasts folder does not exist, create it and return a null list
				System.out.println("Attempting to create podcasts folder.");
				ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();
				
				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, COLLECTION_PODCASTS_TITLE);
				resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, COLLECTION_PODCASTS_DESCRIPTION);
				
				ContentCollection collection = contentHostingService.addCollection(podcastsCollection, resourceProperties);
				
				contentHostingService.setPubView(collection.getId(), true);
				
				ContentCollectionEdit edit = contentHostingService.editCollection(collection.getId());

				contentHostingService.commitCollection(edit);

				// TODO: create an empty resourceList instead of null?
			}
			catch (IdUnusedException e) {
				// TODO: handle if necessary
				LOG.warn("IdUnusedException: " + e.getMessage(), e);
			}
			catch (TypeException e) {
				LOG.warn("TypeException: " + e.getMessage(), e);
			}
			
		}
		catch (TypeException e) {
			LOG.warn("TypeException: " + e.getMessage(), e);
		}
		
		return resourcesList;
	}

	public ContentResourceEdit getAResource(String resourceId) throws PermissionException, IdUnusedException {

		try {
			return (ContentResourceEdit) contentHostingService.getResource(resourceId);				
		}
		catch (TypeException e) {
			LOG.warn("TypeException: " + e.getMessage(), e);
		}

		return null;
	} 
	
	
	/**
	 *  Add a podcast to the site's resources
	 *  
	 *  @param title
	 *  			the title of this podcast resource
	 *  @param displayDate
	 *  			the display date for this podcast resource
	 *  @param description
	 *  			the description of this podcast resource
	 *  @param body
	 *  			the bytes of this podcast
	 */
	public void addPodcast(String title, Date displayDate, String description, byte[] body, 
			               String filename, String contentType) throws OverQuotaException, ServerOverloadException, InconsistentException, 
			               IdInvalidException, IdLengthException, PermissionException, IdUniquenessException {
		String siteCollection = contentHostingService.getSiteCollection( getSiteId() );

		ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();
		String resourceCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
			
		//TODO: may need additional properties here
		resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString() );
		resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, title);
		resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		resourceProperties.addProperty(DISPLAY_DATE, formatter.format(displayDate));
		resourceProperties.addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, filename);
		resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, new Integer(body.length).toString());
			
		contentHostingService.addResource(Validator.escapeResourceName(title), resourceCollection, 0,
			contentType, body, resourceProperties, NotificationService.NOTI_NONE);
	}
	
	/**
	 * Removes a podcast
	 * 
	 * @param id
	 */
	public void removePodcast(String resourceId) 
		throws IdUnusedException, InUseException, TypeException, PermissionException {

		ContentResourceEdit edit = null;
		
		edit = contentHostingService.editResource(resourceId);
		contentHostingService.removeResource(edit);
	}
	
	/**
	 *  Tests whether the podcasts folder exists
	 *  and create it if it does not
	 *  
	 * @return true if exists, false otherwise
	 */
	public boolean checkPodcastFolder () throws  PermissionException, InUseException {
	
		boolean podcastCollection = false;
		String siteCollection = contentHostingService.getSiteCollection( getSiteId() );
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
		
		try {
			contentHostingService.checkCollection(podcastsCollection);		
			podcastCollection =  true;
		}
		catch (IdUnusedException ex) {
			try {
				// TODO: Does not exist. Create podcasts folder
				ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();
				
				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, COLLECTION_PODCASTS_TITLE);
				resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, COLLECTION_PODCASTS_DESCRIPTION);
				
				ContentCollection collection = contentHostingService.addCollection(podcastsCollection, resourceProperties);
				
				contentHostingService.setPubView(collection.getId(), true);
				
				ContentCollectionEdit edit = contentHostingService.editCollection(collection.getId());

				contentHostingService.commitCollection(edit);
				
				podcastCollection = true;
			}
			catch (TypeException e) {
				LOG.warn("TypeException: " + e.getMessage(), e);
			}
			catch (IdInvalidException e) {
				LOG.error("IdInvalidException: " + e.getMessage(), e);
			}
			catch (InconsistentException e) {
				LOG.error("InconsistentError: " + e.getMessage(), e);
			} catch (IdUnusedException e) {
				LOG.warn("IdUnusedException: " + e.getMessage(), e);
			} catch (IdUsedException e) {
				// TODO Auto-generated catch block
				LOG.warn("IdUsedException: " + e.getMessage(), e);
			}
		}
		catch (TypeException e) {
			LOG.warn("TypeException: " + e.getMessage(), e);
		}

		return podcastCollection;
	}
	
	public boolean checkForActualPodcasts() throws PermissionException {
		String siteCollection = contentHostingService.getSiteCollection( getSiteId() );
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
		List resourcesList = null;

		try {

			ContentCollection collection = contentHostingService.getCollection(podcastsCollection);
			
			resourcesList = collection.getMemberResources();
			
			if (resourcesList != null) {
				if (resourcesList.isEmpty())
					return false;
				else
					return true;					
			}
			else 
				return false;
			
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			LOG.warn("IdUnusedException: " + e.getMessage(), e);
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			LOG.warn("TypeException: " + e.getMessage(), e);
		}

		return false;
	}

	public PodcastComparator getPodcastComparator() {
		return podcastComparator;
	}

	public void setPodcastComparator(PodcastComparator podcastComparator) {
		this.podcastComparator = podcastComparator;
	}
	
	public void revisePodcast(String resourceId, String title, Date date, String description, byte[] body, 
            String filename) throws PermissionException, InUseException, OverQuotaException, ServerOverloadException {
		
		try {
			// get Resource to modify
			ContentResourceEdit podcastEditable = null;
		
			podcastEditable = contentHostingService.editResource(resourceId);
			ResourcePropertiesEdit podcastResourceEditable = podcastEditable.getPropertiesEdit();

			if (! title.equals(podcastResourceEditable.getProperty(ResourceProperties.PROP_DISPLAY_NAME))) {
				podcastResourceEditable.removeProperty(ResourceProperties.PROP_DISPLAY_NAME);
				podcastResourceEditable.addProperty(ResourceProperties.PROP_DISPLAY_NAME, title);
			
			}

			if (! description.equals(podcastResourceEditable.getProperty(ResourceProperties.PROP_DESCRIPTION))) {
				podcastResourceEditable.removeProperty(ResourceProperties.PROP_DESCRIPTION);
				podcastResourceEditable.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
		
			}

			if (date != null) {
				podcastResourceEditable.removeProperty(DISPLAY_DATE);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				podcastResourceEditable.addProperty(DISPLAY_DATE, formatter.format(date));
		
			}
			
			if (! filename.equals(podcastResourceEditable.getProperty(ResourceProperties.PROP_ORIGINAL_FILENAME))) {
				podcastResourceEditable.removeProperty(ResourceProperties.PROP_ORIGINAL_FILENAME);
				podcastResourceEditable.addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, Validator.escapeResourceName(filename) );

				podcastResourceEditable.removeProperty(ResourceProperties.PROP_CONTENT_LENGTH);
				podcastResourceEditable.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, new Integer(body.length).toString());
			}

			contentHostingService.commitResource(podcastEditable);

		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			LOG.trace("Expected PermissionException: " + e.getMessage(), e);
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			LOG.warn("TypeException: " + e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public List checkDISPLAY_DATE(List resourcesList) {
		
		//loop to check if DISPLAY_DATE has been set. If not, set it
		Iterator podcastIter = resourcesList.iterator();
		
		// for each bean
		while (podcastIter.hasNext() ) {
			// get its properties from ContentHosting
			ContentResource aResource = (ContentResource) podcastIter.next();
			ResourceProperties itsProperties = aResource.getProperties();
			
			try {
				itsProperties.getTimeProperty(DISPLAY_DATE);
			}
			catch (Exception e) {
				// does not exit, set CREATION_DATE to DISPLAY_DATE
				setDISPLAY_DATE(itsProperties);
			}
		}

		return resourcesList;
	}
	
	public void setDISPLAY_DATE(ResourceProperties rp) {
		SimpleDateFormat formatterProp = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date tempDate = null;
		try {
			tempDate = new Date(rp.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime());
		} catch (EntityPropertyNotDefinedException e) {
			// TODO Auto-generated catch block
			LOG.warn("EntityPropertyNotDefinedException: " + e.getMessage(), e);
		} catch (EntityPropertyTypeException e) {
			// TODO Auto-generated catch block
			LOG.warn("EntityPropertyTypeException: " + e.getMessage(), e);
		}
		
		rp.addProperty(DISPLAY_DATE, formatterProp.format(tempDate));

	}
	
	public String getPodcastFileURL(String resourceId) throws PermissionException, IdUnusedException {
		try {
				return (contentHostingService.getResource(resourceId)).getUrl();

		} catch (TypeException e) {
			// TODO Auto-generated catch block
			LOG.warn("TypeException: " + e.getMessage(), e);
		}
		
		return null;
	}
	
	public boolean canUpdateSite() {
		return securityService.unlock(userDirectoryService.getCurrentUser(), UPDATE_PERMISSIONS, getSiteId());
	}
}

