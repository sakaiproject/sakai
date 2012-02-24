/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/web/trunk/web-api/api/src/java/org/sakaiproject/web/api/WebService.java$
 * $Id: WebService.java 9227 2006-06-22 02:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.web.api;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;

public interface WebService extends EntityProducer
{
	public static final String SERVICE_NAME = WebService.class.getName();
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "web";
}
