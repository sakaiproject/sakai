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

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;

import java.util.List;

import java.sql.Time;

public interface PodcastService extends EntityProducer
{
  /** This string can be used to find the service in the service manager. */
	public static final String COLLECTION_PODCASTS = "podcasts";
	
	public static final String COLLECTION_PODCASTS_TITLE = "Podcasts";
	
	public static final String COLLECTION_PODCASTS_DESCRIPTION = "Common Folder for All Site Podcasts";
		
	public static final String PODCASTS_SERVICE_NAME = "org.sakaiproject.api.app.syllabus.PodcastService";

	public void addPodcast(String title, Time displayDate, String description, byte[] body);
	
	public List getPodcasts();
	
	public void removePodcast(String resourceId);
	
	public String getSiteId();
	
	public boolean getPodcastCollection ();

}