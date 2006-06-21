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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.fileupload.FileItem;
import org.sakaiproject.api.app.podcasts.PodcastService;

public class addPodcastBean {
	private String filename;
	private Date date;
	private String title;
	private String description;
	private String email;
	private long fileSize;
    BufferedInputStream fileAsStream;

	private PodcastService podcastService;

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
	        // TODO: 1. save this as a property?
	        //       2. also save the type of file?
	        fileSize = item.getSize();
	        System.out.println("processFileUpload(): item: " + item + " fieldname: " + fieldName + " filename: " + filename + " length: " + fileSize);

	        // Read the file as a stream (may be more memory-efficient)
	        fileAsStream = new BufferedInputStream(item.getInputStream());

	        // Read the contents as a byte array
	        // Just need to upload in preparation for depositing into Resources
	        //fileContents = item.get();

        }
        catch (Exception ex)
        {
            // handle exception
            System.out.println("Houston, we have a problem.");
            ex.printStackTrace();
        }
    }
	
	/**
	 * This attempts to add a podcast
	 */
	public String processAdd() {
		byte[] fileContents = new byte[(int) fileSize];
		
		try {
			fileAsStream.read(fileContents);
		}
		catch (IOException ioe) {
			System.out.println("What happened to the fileStream?");
		}
		
		podcastService.addPodcast(title, date, description, fileContents);

		date = null;
		title="";
		description="";
		return "cancel";
	}
	
	public String processCancelAdd() {
		date = null;
		title="";
		description="";
		fileAsStream = null;
		return "cancel";
	}
	
	public String processRevisePodcast() {
		return "cancel";
	}
	
	public String processCancelRevise() {
		return "cancel";
	}

	public void setPodcastService(PodcastService podcastService) {
		this.podcastService = podcastService;
	}
    
}
