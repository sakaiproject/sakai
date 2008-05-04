/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.event.api;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.exception.NotificationNotDefinedException;
import org.sakaiproject.exception.PermissionException;

/**
 * <p>
 * NotificationService is ...
 * </p>
 */
public interface NotificationService {
	/** This string can be used to find the service in the service manager. */
	static final String SERVICE_NAME = NotificationService.class.getName();

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/notification";

	public static enum Permission implements Entity.Permission {

		/** ability / event for notification add. */
		SECURE_ADD_NOTIFICATION("notification.add"),

		/** ability / event for notification update. */
		SECURE_UPDATE_NOTIFICATION("notification.update"),

		/** ability / event for notification removal. */
		SECURE_REMOVE_NOTIFICATION("notification.remove");

		private final String permission;

		private Permission(String permission) {
			this.permission = permission;
		}

		@Override
		public String toString() {
			return permission;
		}

	}

	public static enum NotificationOption {

		/** Notification option value for undefined or no notification. */
		NOTI_NONE(0),

		/** Notification option value for required notification. */
		NOTI_REQUIRED(1),

		/** Notification option value for optional notification. */
		NOTI_OPTIONAL(2);

		private final int option;

		private NotificationOption(int option) {
			this.option = option;
		}

		@Override
		public String toString() {
			return String.valueOf(option);
		}

		public int toInteger() {
			return option;
		}

	}

	public static enum NotificationPreference {

		/** Notification option value for undefined notification. */
		PREF_NONE(0),

		/** Notification preference value for blocking notification. */
		PREF_IGNORE(1),

		/** Notification preference value for digest notification. */
		PREF_DIGEST(2),

		/** Notification preference value for immediate notification. */
		PREF_IMMEDIATE(3);

		private final int option;

		private NotificationPreference(int option) {
			this.option = option;
		}

		@Override
		public String toString() {
			return String.valueOf(option);
		}

		public int toInteger() {
			return option;
		}

	}

	public static enum NotificationPreferenceType {

		/** Preferences key for default notification prefs. */
		PREFS_DEFAULT("noti:default"),

		/**
		 * Preferences key for default for a resource type notification prefs -
		 * append the resource type.
		 */
		PREFS_TYPE("noti:types:"),

		/**
		 * Preferences key for default for a site notification prefs - append
		 * the site id.
		 */
		PREFS_SITE("noti:sites:"),

		/**
		 * Preferences key for a specific notification - append the notification
		 * id.
		 */
		PREFS_NOTI("noti:notis:");

		private final String notificationType;

		private NotificationPreferenceType(String notificationType) {
			this.notificationType = notificationType;
		}

		@Override
		public String toString() {
			return notificationType;
		}

	}

	/**
	 * Establish a new notification, locked for edit. Must commitEdit() to make
	 * official, or cancelEdit() when done!
	 * 
	 * @return a new Notification, locked for edit.
	 */
	Notification addNotification()
	/* throws PermissionException */;

	/**
	 * Establish a new transient notification. Transient notifications are
	 * processed by the service but not stored in storage. Modification to the
	 * notification can be done at any time, do not use edit(), commit() or
	 * remove() on it.
	 * 
	 * @return a new transient Notification.
	 */
	Notification addTransientNotification();

	/**
	 * Access a notification object.
	 * 
	 * @param id
	 *            The notification id string.
	 * @return A notification object containing the notification information.
	 * @exception NotificationNotDefinedException
	 *                if not found.
	 */
	Notification getNotification(String id)
			throws NotificationNotDefinedException;

	/**
	 * Remove this notification - it must be a notification with a lock from
	 * editNotification(). The NotificationEdit is disabled, and not to be used
	 * after this call.
	 * 
	 * @param id
	 *            The notification id.
	 * @exception PermissionException
	 *                if the current notification does not have permission to
	 *                remove this notification.
	 */
	void removeNotification(Notification notification)
	/* throws PermissionException */;

	/**
	 * Access the internal reference which can be used to access the resource
	 * from within the system.
	 * 
	 * @param id
	 *            The notification id.
	 * @return The the internal reference which can be used to access the
	 *         resource from within the system.
	 */
	String notificationReference(String id);

	/**
	 * Find a notification object.
	 * 
	 * @param function
	 *            The function setting of the notification object.
	 * @param filter
	 *            The resourceFilter setting of the notification object.
	 * @return A notification object matching the criteria, or null if none
	 *         found.
	 */
	Notification findNotification(Entity.Permission function, String filter);

	/**
	 * Check if an email notification should be reply-able in the To: field
	 * 
	 * @return true if email notifications should be reply-able in the To:
	 *         field, false if not.
	 */
	boolean isNotificationToReplyable();

	/**
	 * Check if an email notification should be reply-able in the From: field
	 * 
	 * @return true if email notifications should be reply-able in the From:
	 *         field, false if not.
	 */
	boolean isNotificationFromReplyable();
}
