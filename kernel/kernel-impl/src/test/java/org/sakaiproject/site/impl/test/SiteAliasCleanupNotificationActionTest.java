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
package org.sakaiproject.site.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.impl.DbNotificationService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.impl.SiteAliasCleanupNotificationAction;

/**
 * Verifies event-driven cleanup of site aliases.
 *
 * @author dmccallum
 */
public class SiteAliasCleanupNotificationActionTest {

	private static final String RESOURCE = "/site/test-site";

	private SiteAliasCleanupNotificationAction cleanupAction;
	private Event inboundEvent;
	private AliasService aliasService;
	private NotificationService notificationService;

	@Before
	public void setUp() {
		aliasService = mock(AliasService.class);
		notificationService = mock(NotificationService.class);
		inboundEvent = mock(Event.class);
		when(inboundEvent.getResource()).thenReturn(RESOURCE);

		cleanupAction = new SiteAliasCleanupNotificationAction();
		cleanupAction.setAliasService(aliasService);
		cleanupAction.setNotificationService(notificationService);
	}

	@Test
	public void testRegistersForSiteDeletions() {
		NotificationEdit notification = new DbNotificationService().new BaseNotificationEdit("transient");
		when(notificationService.addTransientNotification()).thenReturn(notification);

		cleanupAction.init();

		verify(notificationService).addTransientNotification();
		assertEquals(SiteService.SECURE_REMOVE_SITE, notification.getFunction());
		assertEquals(SiteService.REFERENCE_ROOT, notification.getResourceFilter());
		assertSame(cleanupAction, notification.getAction());
	}

	@Test
	public void testDeletesAliasesTargetingEventResourceIfEnabled() throws PermissionException {
		cleanupAction.notify(null, inboundEvent);

		verify(aliasService).removeTargetAliases(RESOURCE);
	}

	@Test
	public void testIgnoresNotificationsIfDisabled() {
		cleanupAction.setEnabled(false);

		cleanupAction.notify(null, inboundEvent);

		verifyNoInteractions(aliasService);
	}

	@Test
	public void testSwallowsUncheckedExceptionsByDefault() throws PermissionException {
		RuntimeException failure = new RuntimeException("This is a simulated failure");
		doThrow(failure).when(aliasService).removeTargetAliases(RESOURCE);

		cleanupAction.notify(null, inboundEvent);

		verify(aliasService).removeTargetAliases(RESOURCE);
	}

	@Test
	public void testSwallowsCheckedExceptionsByDefault() throws PermissionException {
		PermissionException failure = new PermissionException("user-123", SiteService.SECURE_REMOVE_SITE, RESOURCE);
		doThrow(failure).when(aliasService).removeTargetAliases(RESOURCE);

		cleanupAction.notify(null, inboundEvent);

		verify(aliasService).removeTargetAliases(RESOURCE);
	}

	@Test
	public void testRaisesUncheckedExceptionsIfConfigured() throws PermissionException {
		cleanupAction.setPropagateExceptions(true);
		RuntimeException failure = new RuntimeException("This is a simulated failure");
		doThrow(failure).when(aliasService).removeTargetAliases(RESOURCE);

		RuntimeException thrown = assertThrows(RuntimeException.class, () -> cleanupAction.notify(null, inboundEvent));

		assertSame(failure, thrown);
	}

	@Test
	public void testWrapsCheckedExceptionsIfConfigured() throws PermissionException {
		cleanupAction.setPropagateExceptions(true);
		PermissionException failure = new PermissionException("user-123", SiteService.SECURE_REMOVE_SITE, RESOURCE);
		doThrow(failure).when(aliasService).removeTargetAliases(RESOURCE);

		RuntimeException thrown = assertThrows(RuntimeException.class, () -> cleanupAction.notify(null, inboundEvent));

		assertSame(failure, thrown.getCause());
	}

	@Test
	public void testCloneCopiesInstanceMembers() {
		// Set both flags to their non-default values.
		cleanupAction.setEnabled(false);
		cleanupAction.setPropagateExceptions(true);

		SiteAliasCleanupNotificationAction clone = (SiteAliasCleanupNotificationAction) cleanupAction.getClone();
		assertEquals(cleanupAction.isEnabled(), clone.isEnabled());
		assertEquals(cleanupAction.isPropagateExceptions(), clone.isPropagateExceptions());
		assertSame(cleanupAction.getAliasService(), clone.getAliasService());
		assertSame(cleanupAction.getNotificationService(), clone.getNotificationService());
	}
}
