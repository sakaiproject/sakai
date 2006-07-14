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

package org.sakaiproject.api.app.podcasts;

import java.util.Date;
import java.util.List;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.ToolManager;

// import org.sakaiproject.entity.api.EntityProducer;

public interface PodcastService // extends EntityProducer
{
  /** This string can be used to find the service in the service manager. */
	public static final String COLLECTION_PODCASTS = "podcasts";
	
	/** This string used as part of URL for podcast feed **/
	public static final String COLLECTION_PODCASTS_FEED = "podcastsFeed";
	
	/** This string used for Title of the podcast collection **/
	public static final String COLLECTION_PODCASTS_TITLE = "Podcasts";
	
	/** This string gives description for podcasts folder **/
	public static final String COLLECTION_PODCASTS_DESCRIPTION = "Common Folder for All Site Podcasts";

	/** This string gives the Service name (class w/ package prefix) for podcast service **/
	public static final String PODCASTS_SERVICE_NAME = "org.sakaiproject.api.app.syllabus.PodcastService";

	/** This string is the name of the property used when displaying and sorting the podcasts **/
	public static final String DISPLAY_DATE = "displayDate";
	
	/** This string gives the update function (permission) string for checking permissions **/
	public static final String UPDATE_PERMISSIONS = "/site.upd";

	/**
	 * Determines if podcast folder is part of Resources of site.
	 * If not, creates it.
	 * 
	 * @return true if folder exists/created, false otherwise.
	 */
	public boolean checkPodcastFolder ();

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
	public boolean checkForActualPodcasts();

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
	public List getPodcasts();
	
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
	 * Returns the full URL of the file from ContentHostingService
	 * 
	 * @param resourceId The ID for the file whose URL is wanted
	 * 
	 * @return String The full URL for the file
	 */
	public String getPodcastFileURL(String resourceId);

	/**
	 * Returns an editable resource if ID exists.
	 * 
	 * @return ContentResourceEdit object if ID valid, null otherwise
	 */
	public ContentResourceEdit getAResource(String resourceId);

	/**
	 * Sets the DISPLAY_DATE property of a podcast to CREATION_DATE
	 * 
	 * @param rp The ResourceProperties of the podcast to set 
	 */
	public void setDISPLAY_DATE(ResourceProperties rp);

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
			               String filename);
	
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
            String filename);
	
	/**
	 * Determines if user can modify the site.
	 * 
	 * @return boolean true if user can modify, false otherwise
	 */
	public boolean canUpdateSite();
}