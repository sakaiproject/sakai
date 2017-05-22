/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.event.cover;

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * NotificationService is a static Cover for the {@link org.sakaiproject.event.api.NotificationService NotificationService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class NotificationService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.event.api.NotificationService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.event.api.NotificationService) ComponentManager
						.get(org.sakaiproject.event.api.NotificationService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.event.api.NotificationService) ComponentManager
					.get(org.sakaiproject.event.api.NotificationService.class);
		}
	}

	private static org.sakaiproject.event.api.NotificationService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.event.api.NotificationService.SERVICE_NAME;

	public static java.lang.String REFERENCE_ROOT = org.sakaiproject.event.api.NotificationService.REFERENCE_ROOT;

	public static java.lang.String SECURE_ADD_NOTIFICATION = org.sakaiproject.event.api.NotificationService.SECURE_ADD_NOTIFICATION;

	public static java.lang.String SECURE_UPDATE_NOTIFICATION = org.sakaiproject.event.api.NotificationService.SECURE_UPDATE_NOTIFICATION;

	public static java.lang.String SECURE_REMOVE_NOTIFICATION = org.sakaiproject.event.api.NotificationService.SECURE_REMOVE_NOTIFICATION;

	public static final int NOTI_IGNORE = org.sakaiproject.event.api.NotificationService.NOTI_IGNORE;

	public static int NOTI_NONE = org.sakaiproject.event.api.NotificationService.NOTI_NONE;

	public static int NOTI_REQUIRED = org.sakaiproject.event.api.NotificationService.NOTI_REQUIRED;

	public static int NOTI_OPTIONAL = org.sakaiproject.event.api.NotificationService.NOTI_OPTIONAL;

	public static int PREF_NONE = org.sakaiproject.event.api.NotificationService.PREF_NONE;

	public static int PREF_IGNORE = org.sakaiproject.event.api.NotificationService.PREF_IGNORE;

	public static int PREF_DIGEST = org.sakaiproject.event.api.NotificationService.PREF_DIGEST;

	public static int PREF_IMMEDIATE = org.sakaiproject.event.api.NotificationService.PREF_IMMEDIATE;

	public static java.lang.String PREFS_DEFAULT = org.sakaiproject.event.api.NotificationService.PREFS_DEFAULT;

	public static java.lang.String PREFS_TYPE = org.sakaiproject.event.api.NotificationService.PREFS_TYPE;

	public static java.lang.String PREFS_SITE = org.sakaiproject.event.api.NotificationService.PREFS_SITE;

	public static java.lang.String PREFS_NOTI = org.sakaiproject.event.api.NotificationService.PREFS_NOTI;

	public static java.lang.String NOTI_OVERRIDE_EXTENSION = org.sakaiproject.event.api.NotificationService.NOTI_OVERRIDE_EXTENSION;
	
	public static void commitEdit(org.sakaiproject.event.api.NotificationEdit param0)
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return;

		service.commitEdit(param0);
	}

	public static void cancelEdit(org.sakaiproject.event.api.NotificationEdit param0)
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return;

		service.cancelEdit(param0);
	}

	public static org.sakaiproject.event.api.NotificationEdit addNotification()
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return null;

		return service.addNotification();
	}

	public static org.sakaiproject.event.api.NotificationEdit addTransientNotification()
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return null;

		return service.addTransientNotification();
	}

	public static org.sakaiproject.event.api.Notification getNotification(java.lang.String param0)
			throws org.sakaiproject.event.api.NotificationNotDefinedException
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return null;

		return service.getNotification(param0);
	}

	public static org.sakaiproject.event.api.NotificationEdit editNotification(java.lang.String param0)
			throws org.sakaiproject.event.api.NotificationNotDefinedException,
			org.sakaiproject.event.api.NotificationLockedException
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return null;

		return service.editNotification(param0);
	}

	public static void removeNotification(org.sakaiproject.event.api.NotificationEdit param0)
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return;

		service.removeNotification(param0);
	}

	public static java.lang.String notificationReference(java.lang.String param0)
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return null;

		return service.notificationReference(param0);
	}

	public static org.sakaiproject.event.api.Notification findNotification(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return null;

		return service.findNotification(param0, param1);
	}
	
	public static List<org.sakaiproject.event.api.Notification> findNotifications(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return null;
		
		return service.findNotifications(param0, param1);
	}

	public static boolean isNotificationToReplyable()
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return false;

		return service.isNotificationToReplyable();
	}

	public static boolean isNotificationFromReplyable()
	{
		org.sakaiproject.event.api.NotificationService service = getInstance();
		if (service == null) return false;

		return service.isNotificationFromReplyable();
	}
}
