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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
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
import org.sakaiproject.util.Validator;


public class PodcastServiceImpl implements PodcastService
{
	private ContentHostingService contentHostingService;
	private ToolManager toolManager;
	private Log LOG = LogFactory.getLog(PodcastServiceImpl.class);
	private Reference siteRef;
	private boolean podcastCollection;
	private PodcastComparator podcastComparator;
	
	PodcastServiceImpl() {
	}

	public void setContentHostingService( ContentHostingService chs) {
		this.contentHostingService = chs;
	}
	
	public void setToolManager( ToolManager tm) {
		toolManager = tm;
	}

	/**
	 * Retrieve the site id
	 */
	public String getSiteId() {
		return toolManager.getCurrentPlacement().getContext();
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
	public List getPodcasts() {
		List resourcesList = null;
		
		String siteCollection = contentHostingService.getSiteCollection( getSiteId() );
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;

		try {
			ContentCollection collection = contentHostingService.getCollection(podcastsCollection);

			resourcesList = collection.getMemberResources();
			
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
			}
			catch (TypeException e) {
			}
			catch (InUseException e) {
			}
			catch (PermissionException e) {
			}
			catch (IdInvalidException e) {
			}
			catch (InconsistentException e) {
			}
			catch (IdUsedException e) {
			}
			
		}
		catch (TypeException e) {
			
		}
		catch (PermissionException e) {
			
		}
		
		return resourcesList;
	}
	
	public ContentResourceEdit getAResource(String resourceId) {

		try {
			return (ContentResourceEdit) contentHostingService.getResource(resourceId);				
		}
		catch (PermissionException pe) {
			
		}
		catch (TypeException te) {
			
		}
		catch (IdUnusedException iue) {
			
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
			               String filename) {
		String siteCollection = contentHostingService.getSiteCollection( getSiteId() );

		try {
			ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();
			String resourceCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
			
			//TODO: may need additional properties here
			resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString() );
			resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, title);
			resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
			resourceProperties.addProperty(DISPLAY_DATE, displayDate.toString());
			resourceProperties.addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, filename);
			resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, new Integer(body.length).toString());
			
			contentHostingService.addResource(Validator.escapeResourceName(title), resourceCollection, 0,
					ResourceProperties.FILE_TYPE, body, resourceProperties, NotificationService.NOTI_NONE);
		}
		catch (OverQuotaException e) {
			
		}
		catch (IdInvalidException e) {
			
		}
		catch (ServerOverloadException e) {
			
		}
		catch (InconsistentException e) {
			
		}
		catch (IdLengthException e) {
			
		}
		catch (PermissionException e) {
			
		}
		catch (IdUniquenessException e) {
			
		}
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
	public boolean checkPodcastFolder () {
		String siteCollection = contentHostingService.getSiteCollection( getSiteId() );
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
		
		try {
			contentHostingService.checkCollection(podcastsCollection);		
			podcastCollection =  true;
		}
		catch(Exception ex){
		//catch (IdUnusedException ex) {
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
			catch (IdUnusedException e) {
				// TODO: user must have renamed/deleted podcast folder, recreate
				System.out.println("Problem creating podcasts folder");
			}
			catch (TypeException e) {
			}
			catch (InUseException e) {
			}
			catch (PermissionException e) {
			}
			catch (IdInvalidException e) {
			}
			catch (InconsistentException e) {
			}
			catch (IdUsedException e) {
			}
		}

		return podcastCollection;
	}
	
	public void setPodcastCollection (boolean podcastCollection) {
		this.podcastCollection = podcastCollection;
	}
	
	public boolean checkForActualPodcasts() {
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
		}
		catch (Exception e) {
			
		}

		return false;
	}

	public PodcastComparator getPodcastComparator() {
		return podcastComparator;
	}

	public void setPodcastComparator(PodcastComparator podcastComparator) {
		this.podcastComparator = podcastComparator;
	}
	
	public void revisePodcast(String resourceId, String title, String displayDate, String description, byte[] body, 
            String filename) {
		
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
			
			if (displayDate != null) {
				podcastResourceEditable.removeProperty(DISPLAY_DATE);
				podcastResourceEditable.addProperty(DISPLAY_DATE, displayDate);
		
			}
			
			if (! filename.equals(podcastResourceEditable.getProperty(ResourceProperties.PROP_ORIGINAL_FILENAME))) {
				podcastResourceEditable.removeProperty(ResourceProperties.PROP_ORIGINAL_FILENAME);
				podcastResourceEditable.addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, filename);

				podcastResourceEditable.removeProperty(ResourceProperties.PROP_CONTENT_LENGTH);
				podcastResourceEditable.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, new Integer(body.length).toString());
			}

			contentHostingService.commitResource(podcastEditable);

		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OverQuotaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerOverloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
