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

	/** Service name (class w/ package prefix) for podcast service **/
	public static final String PODCASTS_SERVICE_NAME = "org.sakaiproject.api.app.syllabus.PodcastService";

	/** This string is the name of the property used when displaying and sorting the podcasts **/
	public static final String DISPLAY_DATE = "displayDate";
	
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
	 * Returns a list of the podcasts stored in site/podcasts folder
	 * 
	 * @return List of podcasts
	 */
	public List getPodcasts();

	/**
	 * Returns the requested podcast
	 * 
	 * @return ContentResource of the podcast wanted
	 */
//	public ContentResource getAPodcast(String Id);

	/**
	 * Removes a podcast from site/podcasts folder
	 * 
	 * @param resourceId resourceId of the podcast to be deleted
	 */
	public void removePodcast(String resourceId)
		throws IdUnusedException, InUseException, TypeException, PermissionException; 
	
	/**
	 * Returns SiteId for the site this tool is a part of
	 * 
	 * @return String of the site id
	 */
	public String getSiteId();
	
	/**
	 * Determines if podcast folder is part of Resources of site.
	 * If not, creates it.
	 * 
	 * @return true if folder exists/created, false otherwise.
	 */
	public boolean checkPodcastFolder ();

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
	 * Determines if there are actual podcasts in the folder
	 * 
	 * @return true if there are actual podcasts, false otherwise
	 */
	public boolean checkForActualPodcasts();

	/**
	 * Returns an editable resource if ID exists.
	 * 
	 * @return ContentResourceEdit object if ID valid, null otherwise
	 */
	public ContentResourceEdit getAResource(String resourceId);
	
	/**
	 * Returns an editable resource if ID exists.
	 * 
	 * @return ContentResourceEdit object if ID valid, null otherwise
	 */
	public void revisePodcast(String resourceId, String title, String displayDate, String description, byte[] body, 
            String filename);

}