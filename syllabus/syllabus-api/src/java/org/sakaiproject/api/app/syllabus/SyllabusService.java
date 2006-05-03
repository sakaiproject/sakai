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

package org.sakaiproject.api.app.syllabus;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;

import java.util.List;

public interface SyllabusService extends EntityProducer
{
  /** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = SyllabusService.class.getName();
	
	public static final String EVENT_SYLLABUS_POST_NEW = "syllabus.post.new";
	
	public static final String EVENT_SYLLABUS_POST_CHANGE = "syllabus.post.change";
	
	public static final String EVENT_SYLLABUS_DELETE_POST = "syllabus.delete.posted";
	
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "syllabus";
	
	public static final String SYLLABUS_SERVICE_NAME = "org.sakaiproject.api.app.syllabus.SyllabusService";
	
	public void postNewSyllabus(SyllabusData data);
	
	public void postChangeSyllabus(SyllabusData data);
	
	public void deletePostedSyllabus(SyllabusData data);
	
	public List getMessages(String id);
	
	public void importEntities(String fromSiteId, String toSiteId, List resourceIds);
}
