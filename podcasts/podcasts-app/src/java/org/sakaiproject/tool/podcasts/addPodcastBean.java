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

package org.sakaiproject.tool.podcasts;

import java.io.InputStream;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.fileupload.FileItem;

public class addPodcastBean {
	private String filename;
	private Date date;
	private String title;
	private String description;
	private String email;

	private SelectItem [] emailItems = {
		new SelectItem("none", "None - No notification"),
		new SelectItem("low", "Low - Only participants who have opted in"),
		new SelectItem("high", "High - All participants")
	};
	
	public addPodcastBean () {
		filename = "";
		title="";
		description="";
		email="";
}
	
	public addPodcastBean (String filename, Date date, String title, String description, String email) {
		this.filename = filename;
		this.date = date;
		this.title = title;
		this.description = description;
		this.email = email;

	}

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
	    this.filename = filename;
	}

	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
	    this.date = date;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public SelectItem [] getEmailItems() {
		return emailItems;
	}
	
	public String getemail() {
		return email;
	}
	
	public void setemail(String email) {
		this.email = email;
	}
	
	public void processFileUpload (ValueChangeEvent event)
            throws AbortProcessingException
    {
	   UIComponent component = event.getComponent();
        Object newValue = event.getNewValue();
        Object oldValue = event.getOldValue();
        PhaseId phaseId = event.getPhaseId();
        Object source = event.getSource();
        System.out.println("processFileUpload() event: " + event + " component: "
                + component + " newValue: " + newValue + " oldValue: " + oldValue
                + " phaseId: " + phaseId + " source: " + source);

        if (newValue instanceof String) return;
        if (newValue == null) return;

        // must be a FileItem
        try
        {
            FileItem item = (FileItem) event.getNewValue();
	        String fieldName = item.getFieldName();
	        filename = item.getName();
	        long fileSize = item.getSize();
	        System.out.println("processFileUpload(): item: " + item + " fieldname: " + fieldName + " filename: " + filename + " length: " + fileSize);

	        // Read the file as a stream (may be more memory-efficient)
	        InputStream fileAsStream = item.getInputStream();

	        // Read the contents as a byte array
	        byte[] fileContents = item.get();

	        // now process the file.  Do application-specific processing
	        // such as parsing the file, storing it in the database,
	        // or whatever needs to happen with the uploaded file.

        }
        catch (Exception ex)
        {
            // handle exception
            System.out.println("Houston, we have a problem.");
        }
    }
	
	/**
	 * This attempts to add a podcast
	 */
	public String processAdd() {
		return "main";
	}
	
	public String processCancelAdd() {
		return "cancel";
	}
	
	public String processRevisePodcast() {
		return "cancel";
	}
	
	public String processCancelRevise() {
		return "cancel";
	}

	/**
	public void addPodcast(String title, Time displayDate, String description, byte[] body) {
		//get the site's collection
		String siteCollection = ContentHostingService.getSiteCollection(getSiteId());
		String podcastCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
		ContentCollectionEdit collectionEdit = null;
		
		try {
			collectionEdit = ContentHostingService.editCollection(podcastsCollection);
		}
		catch (IdUnusedException un) {
			// make it
			try {
				ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties();
				
				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, COLLECTION_PODCASTS_TITLE);
				resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, COLLECTION_PODCASTS_DESCRIPTION);
				
				ContentCollection collection = ContentHostingService.addCollection(podcastsCollection, resourceProperties);
				ContentHostingService.setPubView(collection.getId(), true);
				
				ContentCollectionEdit edit = ContentHostingService.editCollection(collection.getId());
				ContentHostingService.commitCollection(edit);
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
			LOG.warn("enableResources: " + e);
		}
		catch (PermissionException e) {
			LOG.warn("enableResources: " + e);
		}
		catch (InUseException e) {
			
		}
		
		try {
			ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties();
			String resourceCollection = siteCollection + COLLECTION_PODCASTS + Entity.SEPARATOR;
			
			//TODO: may need additional properties here
			//  possibly add notification level?
			resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());
			resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, title);
			resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
			resourceProperties.addProperty(ResourceProperties.PROP_CREATION_DATE, displayDate.toString());
			
			ContentHostingService.addResource(Validator.escapeResourceName(title), resourceCollection, 0,
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
	} 	**/
}
