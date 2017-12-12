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

import java.lang.reflect.Field;

import lombok.extern.slf4j.Slf4j;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.impl.SiteAliasCleanupNotificationAction;

/**
 * Verifies event-driven cleanup of site aliases.
 * 
 * @author dmccallum
 *
 */
@Slf4j
public class SiteAliasCleanupNotificationActionTest extends MockObjectTestCase {
	
	private SiteAliasCleanupNotificationAction cleanupAction;
	private NotificationEdit cleanupActionWrappingNotificationEdit;
	private Notification inboundNotification;
	private Event inboundEvent;
	private AliasService aliasService;
	private NotificationService notificationService;

	protected void setUp() throws Exception {
		aliasService = mock(AliasService.class);
		notificationService = mock(NotificationService.class);
		cleanupActionWrappingNotificationEdit = mock(NotificationEdit.class);
		inboundNotification = mock(Notification.class);
		inboundEvent = mock(Event.class);
		cleanupAction = new SiteAliasCleanupNotificationAction();
		cleanupAction.setAliasService(aliasService);
		cleanupAction.setNotificationService(notificationService);
		super.setUp();
	}

	public void testDeletesAliasesTargetingEventResourceIfEnabled() throws PermissionException {
		expectInit();
		
		checking(new Expectations() {{
			final String resourceId = "/entity/id";
			// allowing() makes test more resilient to changes in logging code,
			// and it is unlikely the object under test is hard-coded to
			// remove aliases for "/entity/id"
			allowing(inboundEvent).getResource(); will(returnValue(resourceId));
			one(aliasService).removeTargetAliases(resourceId);
		}});
		
		// implicitly tests that alias deletion is enabled by default
		cleanupAction.init();
		cleanupAction.notify(inboundNotification, inboundEvent);
	}

	public void testIgnoresNotificationsIfDisabled() {
		cleanupAction.setEnabled(false);
		expectInit();
		cleanupAction.init();
		cleanupAction.notify(inboundNotification, inboundEvent);
	}
	
	public void testLogsAndSwallowsUncheckedExceptionsByDefault() throws PermissionException {
		expectInit();
		checking(new Expectations() {{
			final String resourceId = "/entity/id";
			RuntimeException failure = new RuntimeException("this is a simulated failure");
			allowing(inboundEvent).getResource(); will(returnValue(resourceId));
			one(aliasService).removeTargetAliases(resourceId); will(throwException(failure));
			log.warn(with(any(String.class)), with(same(failure)));
		}});
		
		cleanupAction.init();
		cleanupAction.notify(inboundNotification, inboundEvent);
	}

	public void testLogsAndSwallowsCheckedExceptionsByDefault() throws PermissionException {
		expectInit();
		checking(new Expectations() {{
			final String resourceId = "/entity/id";
			PermissionException failure = 
				new PermissionException("user-123", SiteService.SECURE_REMOVE_SITE, resourceId);
			allowing(inboundEvent).getResource(); will(returnValue(resourceId));
			one(aliasService).removeTargetAliases(resourceId); will(throwException(failure));
			log.warn(with(any(String.class)), with(same(failure)));
		}});
		
		cleanupAction.init();
		cleanupAction.notify(inboundNotification, inboundEvent);
	}
	
	public void testRaisesUncheckedExceptionsIfSoConfigured() throws PermissionException {
		cleanupAction.setPropagateExceptions(true);
		expectInit();
		final RuntimeException failure = new RuntimeException("this is a simulated failure");
		checking(new Expectations() {{
			final String resourceId = "/entity/id";
			allowing(inboundEvent).getResource(); will(returnValue(resourceId));
			one(aliasService).removeTargetAliases(resourceId); will(throwException(failure));
		}});
		
		cleanupAction.init();
		try {
			cleanupAction.notify(inboundNotification, inboundEvent);
			fail("Should have raised the unchecked exception representing the failure to delete aliases");
		} catch ( RuntimeException e ) {
			assertSame("Threw the wrong exception", failure, e);
		}
	}
	
	public void testRaisesCheckedExceptionsInUncheckedExceptionsIfSoConfigured() throws PermissionException {
		cleanupAction.setPropagateExceptions(true);
		expectInit();
		final String resourceId = "/entity/id";
		final PermissionException failure = 
			new PermissionException("user-123", SiteService.SECURE_REMOVE_SITE, resourceId);
		checking(new Expectations() {{
			allowing(inboundEvent).getResource(); will(returnValue(resourceId));
			one(aliasService).removeTargetAliases(resourceId); will(throwException(failure));
		}});
		
		cleanupAction.init();
		try {
			cleanupAction.notify(inboundNotification, inboundEvent);
			fail("Should have raised an unchecked exception wrapping the checked exception indicating failure to delete aliases");
		} catch ( RuntimeException e ) {
			assertSame("Didn't wrap exception representing failure to delete aliases", failure, e.getCause());
		}
	}
	
	public void testCloneCopiesInstanceMembers() {
		// ensure we're not getting false positives b/c of defaulting
		cleanupAction.setEnabled(!(cleanupAction.isEnabled()));
		cleanupAction.setPropagateExceptions(!(cleanupAction.isPropagateExceptions()));
		
		SiteAliasCleanupNotificationAction clone = 
			(SiteAliasCleanupNotificationAction) cleanupAction.getClone();
		assertEquals("Failed to copy the \"enabled\" property", 
				cleanupAction.isEnabled(), clone.isEnabled());
		assertEquals("Failed to copy the \"propagateExceptions\" property", 
				cleanupAction.isPropagateExceptions(), clone.isPropagateExceptions());
		assertSame("Failed to copy the \"aliasService\" property",
				cleanupAction.getAliasService(), clone.getAliasService());
		assertSame("Failed to copy the \"notificationService\" property",
				cleanupAction.getNotificationService(), clone.getNotificationService());
	}
	
	private void expectInit() {
		checking(new Expectations() {{
			one(notificationService).addTransientNotification();
			will(returnValue(cleanupActionWrappingNotificationEdit));
			
			one(cleanupActionWrappingNotificationEdit).setFunction(SiteService.SECURE_REMOVE_SITE);
			one(cleanupActionWrappingNotificationEdit).setResourceFilter(SiteService.REFERENCE_ROOT);
			one(cleanupActionWrappingNotificationEdit).setAction(cleanupAction);
		}});
	}
	
	private void setField(String fieldName,
			Class<?> clazz, Object onInstance, Object fieldValue) 
	throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(onInstance, fieldValue);
	}
}
