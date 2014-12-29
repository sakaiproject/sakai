/**********************************************************************************
 * $URL$
 * $Id$
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
package org.sakaiproject.api.app.syllabus.cover;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;

public class SyllabusService
{
	public static java.lang.String APPLICATION_ID = org.sakaiproject.api.app.syllabus.SyllabusService.APPLICATION_ID;

	private static org.sakaiproject.api.app.syllabus.SyllabusService m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.api.app.syllabus.SyllabusService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.api.app.syllabus.SyllabusService) ComponentManager
						.get(org.sakaiproject.api.app.syllabus.SyllabusService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.api.app.syllabus.SyllabusService) ComponentManager
					.get(org.sakaiproject.api.app.syllabus.SyllabusService.class);
		}
	}

	public static java.lang.String merge(java.lang.String param0, org.w3c.dom.Element param1, java.lang.String param2,
			java.lang.String param3, java.util.Map param4, java.util.HashMap param5, java.util.Set param6)
	{
		org.sakaiproject.api.app.syllabus.SyllabusService service = getInstance();
		if (service == null) return null;

		return service.merge(param0, param1, param2, param3, param4, param5, param6);
	}

	public static java.lang.String getLabel()
	{
		org.sakaiproject.api.app.syllabus.SyllabusService service = getInstance();
		if (service == null) return null;

		return service.getLabel();
	}

	public static java.lang.String archive(java.lang.String param0, org.w3c.dom.Document param1, java.util.Stack param2,
			java.lang.String param3, java.util.List param4)
	{
		org.sakaiproject.api.app.syllabus.SyllabusService service = getInstance();
		if (service == null) return null;

		return service.archive(param0, param1, param2, param3, param4);

	}

	public static void importResources(java.lang.String param0, java.lang.String param1, java.util.List param2)
	{
		org.sakaiproject.api.app.syllabus.SyllabusService service = getInstance();
		if (service == null) return;

		service.importEntities(param0, param1, param2);
	}

	public static List getMessages(String id)
	{
		org.sakaiproject.api.app.syllabus.SyllabusService service = getInstance();
		if (service == null) return null;

		return service.getMessages(id);
	}
}
