/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.event.api;

import java.util.List;

/**
 * <p>
 * NotificationService is ...
 * </p>
 */
public interface NotificationService
{
	/** This string can be used to find the service in the service manager. */
	static final String SERVICE_NAME = NotificationService.class.getName();

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/notification";

	/** ability / event for notification add. */
	static final String SECURE_ADD_NOTIFICATION = "notification.add";

	/** ability / event for notification update. */
	static final String SECURE_UPDATE_NOTIFICATION = "notification.update";

	/** ability / event for notification removal. */
	static final String SECURE_REMOVE_NOTIFICATION = "notification.remove";
	
	/** Notification option value to tell API to skip notification logic. */
	static final int NOTI_IGNORE = -1;

	/** Notification option value for undefined or no notification. */
	static final int NOTI_NONE = 0;

	/** Notification option value for required notification. */
	static final int NOTI_REQUIRED = 1;

	/** Notification option value for optional notification. */
	static final int NOTI_OPTIONAL = 2;

	/** Notification option value for undefined notification. */
	static final int PREF_NONE = 0;

	/** Notification preference value for blocking notification. */
	static final int PREF_IGNORE = 1;

	/** Notification preference value for digest notification. */
	static final int PREF_DIGEST = 2;

	/** Notification preference value for immediate notification. */
	static final int PREF_IMMEDIATE = 3;

	/** Preferences key for default notification prefs. */
	static final String PREFS_DEFAULT = "noti:default";

	/** Preferences key for default for a resource type notification prefs - append the resource type. */
	static final String PREFS_TYPE = "noti:types:";

	/** Preferences key for default for a site notification prefs - append the site id. */
	static final String PREFS_SITE = "noti:sites:";

	/** Preferences key for a specific notification - append the notification id. */
	static final String PREFS_NOTI = "noti:notis:";
	
	/** Preferences extension used for site specific preferences */
	static final String NOTI_OVERRIDE_EXTENSION = "_override";

	/**
	 * Establish a new notification, locked for edit. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @return a new Notification, locked for edit.
	 */
	NotificationEdit addNotification()
	/* throws PermissionException */;

	/**
	 * Establish a new transient notification. Transient notifications are processed by the service but not stored in storage. Modification to the notification can be done at any time, do not use edit(), commit() or remove() on it.
	 * 
	 * @return a new transient Notification.
	 */
	NotificationEdit addTransientNotification();

	/**
	 * Access a notification object.
	 * 
	 * @param id
	 *        The notification id string.
	 * @return A notification object containing the notification information.
	 * @exception NotificationNotDefinedException
	 *            if not found.
	 */
	Notification getNotification(String id) throws NotificationNotDefinedException;

	/**
	 * Get a locked notification object for editing. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param id
	 *        The notification id string.
	 * @return A NotificationEdit object for editing.
	 * @exception NotificationNotDefinedException
	 *            if not found.
	 * @exception NotificationLockedException
	 *            if the current notification is elsewhere locked for edit.
	 */
	NotificationEdit editNotification(String id) throws NotificationNotDefinedException, NotificationLockedException;

	/**
	 * Commit the changes made to a NotificationEdit object, and release the lock. The NotificationEdit is disabled, and not to be used after this call.
	 * 
	 * @param notification
	 *        The NotificationEdit object to commit.
	 */
	void commitEdit(NotificationEdit notification);

	/**
	 * Cancel the changes made to a NotificationEdit object, and release the lock. The NotificationEdit is disabled, and not to be used after this call.
	 * 
	 * @param notification
	 *        The NotificationEdit object to commit.
	 */
	void cancelEdit(NotificationEdit notification);

	/**
	 * Remove this notification - it must be a notification with a lock from editNotification(). The NotificationEdit is disabled, and not to be used after this call.
	 * 
	 * @param notification
	 *        The notification
	 * @exception PermissionException
	 *            if the current notification does not have permission to remove this notification.
	 */
	void removeNotification(NotificationEdit notification)
	/* throws PermissionException */;

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 * 
	 * @param id
	 *        The notification id.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	String notificationReference(String id);

	/**
	 * Find a notification object.
	 * 
	 * @param function
	 *        The function setting of the notification object.
	 * @param filter
	 *        The resourceFilter setting of the notification object.
	 * @return A notification object matching the criteria, or null if none found.
	 */
	Notification findNotification(String function, String filter);

	/**
	 * Find a notification object.
	 * 
	 * @param function
	 *        The function setting of the notification object.
	 * @param filter
	 *        The resourceFilter setting of the notification object.
	 * @return A notification object matching the criteria, or null if none found.
	 */
	List<Notification> findNotifications(String function, String filter);

	/**
	 * Check if an email notification should be reply-able in the To: field
	 * 
	 * @return true if email notifications should be reply-able in the To: field, false if not.
	 */
	boolean isNotificationToReplyable();

	/**
	 * Check if an email notification should be reply-able in the From: field
	 * 
	 * @return true if email notifications should be reply-able in the From: field, false if not.
	 */
	boolean isNotificationFromReplyable();
	
}
