/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

import org.sakaiproject.exception.PermissionException;



public interface PodfeedService {

	public static final String PODFEED_CATEGORY = "Podcast";
	
	/**
	 * Method to generate the XML file for the feed.
	 */
	public String generatePodcastRSS();

	/**
	 * Method to generate the XML file for the specific category of feed with the name, siteId,
	 * and feedType passed it. So if different formats are supported, we are ready.
	 * 
	 * @param siteID 
	 * 			The siteId for the site wanted
	 * @param feedType 
	 * 			The feed type (Currently RSS 2.0)
	 * 
	 * @return A string that is the XML file
	 */
	public String generatePodcastRSS(String siteID, String feedType);
	
	/**
	 * Gets the global feed title for the podcast.
	 * 
	 * @return String containing the global podcast feed title.
	 */
	public String getPodfeedTitle();
	public String getPodfeedTitle(String siteId);
	
	/**
	 * Sets the global feed title to the string passed in.
	 * 
	 * @param feedTitle The new title for the podcast feed.
	 */
	public void setPodfeedTitle(String feedTitle);
	
	/**
	 * Returns the global description for the podcast feed.
	 * 
	 * @return String containing the global podcast feed description.
	 */
	public String getPodfeedDescription();
	public String getPodfeedDescription(String siteId);
	
	/**
	 * Sets the global feed description to the string passed in.
	 * 
	 * @param feedDescription The new description for the podcast feed.
	 */
	public void setPodfeedDescription(String feedDescription);

	/**
	 * Returns the global description for the podcast feed.
	 * 
	 * @return String containing the global podcast feed description.
	 */
	public String getPodfeedGenerator();
	public String getPodfeedGenerator(String siteId);
	
	/**
	 * Sets the global feed description to the string passed in.
	 * 
	 * @param feedDescription The new description for the podcast feed.
	 */
	public void setPodfeedGenerator(String feedGenerator);

	/**
	 * Returns the global description for the podcast feed.
	 * 
	 * @return String containing the global podcast feed description.
	 */
	public String getPodfeedCopyright();
	public String getPodfeedCopyright(String siteId);
	
	/**
	 * Sets the global feed description to the string passed in.
	 * 
	 * @param feedDescription The new description for the podcast feed.
	 */
	public void setPodfeedCopyright(String feedCopyright);

	/**
	 * Returns podcast folder id using either 'podcasts' or 'Podcasts'.
	 * If it does not exist in either form, will create it.
	 * 
	 * @param siteId The site to search
	 * @return String containing the complete id for the podcast folder
	 * 
	 * @throws PermissionException
	 */
	public String retrievePodcastFolderId(String siteId); 

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

}
