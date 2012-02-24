/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.email.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * EmailService is a static Cover for the {@link org.sakaiproject.email.api.EmailService EmailService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class EmailService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.email.api.EmailService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.email.api.EmailService) ComponentManager
						.get(org.sakaiproject.email.api.EmailService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.email.api.EmailService) ComponentManager.get(org.sakaiproject.email.api.EmailService.class);
		}
	}

	private static org.sakaiproject.email.api.EmailService m_instance = null;

	public static void sendMail(javax.mail.internet.InternetAddress param0, javax.mail.internet.InternetAddress[] param1,
			java.lang.String param2, java.lang.String param3, javax.mail.internet.InternetAddress[] param4,
			javax.mail.internet.InternetAddress[] param5, java.util.List param6)
	{
		org.sakaiproject.email.api.EmailService service = getInstance();
		if (service == null) return;

		service.sendMail(param0, param1, param2, param3, param4, param5, param6);
	}

	public static void send(java.lang.String param0, java.lang.String param1, java.lang.String param2, java.lang.String param3,
			java.lang.String param4, java.lang.String param5, java.util.List param6)
	{
		org.sakaiproject.email.api.EmailService service = getInstance();
		if (service == null) return;

		service.send(param0, param1, param2, param3, param4, param5, param6);
	}

	public static void sendToUsers(java.util.Collection users, java.util.Collection headers, java.lang.String message)
	{
		org.sakaiproject.email.api.EmailService service = getInstance();
		if (service == null) return;

		service.sendToUsers(users, headers, message);
	}
}
