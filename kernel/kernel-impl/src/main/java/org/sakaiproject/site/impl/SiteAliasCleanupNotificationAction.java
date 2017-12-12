/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
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

package org.sakaiproject.site.impl;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Element;

import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;

/**
 * Removes orphaned aliases when a site is deleted. Only deals with
 * site aliases and not, for example, mail archive channel aliases.
 * 
 * @author dmccallum
 *
 */
@Slf4j
public class SiteAliasCleanupNotificationAction implements NotificationAction {

	private AliasService aliasService;
	private NotificationService notificationService;
	private boolean enabled = true;
	private boolean propagateExceptions = false;
	
	public NotificationAction getClone() {
		SiteAliasCleanupNotificationAction clone = 
			new SiteAliasCleanupNotificationAction();
		clone.setAliasService(aliasService);
		clone.setNotificationService(notificationService);
		clone.setEnabled(enabled);
		clone.setPropagateExceptions(propagateExceptions);
		return clone;
	}
	
	/**
	 * Registered this {@link NotificationAction} with the 
	 * {@link NotificationService}. Typically invoked by DI framework.
	 */
	public void init() {
		NotificationEdit notification = notificationService.addTransientNotification();
		// we're only interested in being notified of site deletions
		notification.setFunction(SiteService.SECURE_REMOVE_SITE);
		// reiterate that we're only interested in site deletions (likely overkill)
		notification.setResourceFilter(SiteService.REFERENCE_ROOT);
		// assign this object as the actual action to be fired on site deletions
		notification.setAction(this);
	}

	/**
	 * Removes all aliases targeting the resource associated with the given
	 * {@link Event}. Use {@link #setPropagateExceptions(boolean)} to
	 * control whether or not failures are just logged or are wrapped
	 * and raised as unchecked exceptions.
	 * 
	 * <p>Please note that the implementation assumes the configuration
	 * is correct such that this method only receives site deletion
	 * events. No guards are implemented in that respect.</p>
	 */
	public void notify(Notification notif, Event event) {
		if ( !(enabled) ) {
			return;
		}
		
		String deletedResource = event.getResource();
		try {
			aliasService.removeTargetAliases(deletedResource);
		} catch ( RuntimeException e ) {
			if ( propagateExceptions ) {
				throw e;
			} else {
				log.warn("Unable to remove aliases for [" + deletedResource + "]", e);
			}
		} catch ( PermissionException e ) {
			if ( propagateExceptions ) {
				throw new RuntimeException("Unable to remove aliases for [" + deletedResource  + "] because of insufficient permissions", e);
			} else {
				log.warn("Unable to remove aliases for [" + deletedResource + "] because of insufficient permissions", e);
			}
		}
	}

	public void set(Element arg0) {
		// nothing to do
	}

	public void set(NotificationAction arg0) {
		// nothing to do
	}

	public void toXml(Element arg0) {
		// nothing to do
	}

	/**
	 * Is site alias cleanup enabled. If not, simply ignores all
	 * events. Otherwise, will attempt to remove all aliases scoped
	 * to the deleted site. Aliases scoped to entities/resources
	 * associated with the site, e.g. a mail channel, need to
	 * be cleaned up elsewhere.
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Toggle alias cleanup behavior on and off.
	 * 
	 * @see #isEnabled()
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setAliasService(AliasService aliasService) {
		this.aliasService = aliasService;
	}
	
	public AliasService getAliasService() {
		return aliasService;
	}

	/**
	 * If <code>true</code>, {@link #notify(Notification, Event)} will
	 * raise unchecked exceptions as is and wrap and rethrow checked 
	 * exceptions. Otherwise such conditions are just logged.
	 * 
	 */
	public boolean isPropagateExceptions() {
		return propagateExceptions;
	}

	/**
	 * Control failure handling in {@link #notify(Notification, Event)}.
	 * 
	 * @see #isPropagateExceptions()
	 * @param propagateExceptions
	 */
	public void setPropagateExceptions(boolean propagateExceptions) {
		this.propagateExceptions = propagateExceptions;
	}

	/**
	 * Assign the {@link NotificationService} with which to register
	 * this {@link NotificationAction} on a call to {@link #init()}
	 * 
	 * @param notificationService
	 */
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}

}
