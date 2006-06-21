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
	 * Retrieve Podcasts for site
	 * 
	 * @return A List of podcast resources
	 */
	public List getPodcasts() {
		String siteCollection = contentHostingService.getSiteCollection( getSiteId() );
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
		List resourcesList = null;
		
		try {
			ContentCollection collection = contentHostingService.getCollection(podcastsCollection);
			
			resourcesList = collection.getMemberResources();
		}
		catch (IdUnusedException ex) {
			try {
				System.out.println("Attempting to create podcasts folder.");
				ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();
				
				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, COLLECTION_PODCASTS_TITLE);
				resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, COLLECTION_PODCASTS_DESCRIPTION);
				
				ContentCollection collection = contentHostingService.addCollection(podcastsCollection, resourceProperties);
				
				contentHostingService.setPubView(collection.getId(), true);
				
				ContentCollectionEdit edit = contentHostingService.editCollection(collection.getId());

				contentHostingService.commitCollection(edit);
			}
			catch (IdUnusedException e) {
				// TODO: handle if necessary
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
		catch (TypeException e) {
			
		}
		catch (PermissionException e) {
			
		}
		
		return resourcesList;
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
	public void addPodcast(String title, Date displayDate, String description, byte[] body) {
		String siteCollection = contentHostingService.getSiteCollection( getSiteId() );

		try {
			ResourcePropertiesEdit resourceProperties = contentHostingService.newResourceProperties();
			String resourceCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
			
			//TODO: may need additional properties here
			resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString() );
			resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, title);
			resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
			resourceProperties.addProperty(ResourceProperties.PROP_CREATION_DATE, displayDate.toString());
			
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
	public void removePodcast(String resourceId) {
		ContentResourceEdit edit = null;
		
		try {
			edit = contentHostingService.editResource(resourceId);
			
			contentHostingService.removeResource(edit);
		}
		catch (IdUnusedException e) {
			
		}
		catch (InUseException e) {
			
		}
		catch (TypeException e) {
			
		}
		catch (PermissionException e) {
			
		}
	}
	
	/**
	 *  Tests whether the podcasts folder exists
	 *  and create it if it does not
	 *  
	 * @return true if exists, false otherwise
	 */
	public boolean checkPodcastFolder () {
		String siteCollection = contentHostingService.getSiteCollection(toolManager.getCurrentPlacement().getContext());
		String podcastsCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
		ContentCollectionEdit collectionEdit = null;
		
		try {
			//collectionEdit = contentHostingService.editCollection(podcastsCollection);
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
//		catch (TypeException e) {
//		}
//		catch (PermissionException e) {
//		}
//		catch (InUseException e) {
//		}

		return podcastCollection;
	}
	
	public void setPodcastCollection (boolean podcastCollection) {
		this.podcastCollection = podcastCollection;
	}
	
}
