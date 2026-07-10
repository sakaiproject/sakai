/**
 * Copyright (c) 2003-2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.announcement.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.time.api.Time;

/**
 * the Email Notification level is persisted and pre-selected on edit, so this guard keeps
 * Sakai's existing "when to notify" behaviour for that level: notify only for a new post, a draft being
 * released, or a scheduled future release; a plain re-save of a live announcement must NOT re-email.
 * (A deliberate one-off send is handled separately by the transient "Send an email notification now" box.)
 * args: isNew, wasDraft, nowHidden, releaseDate, now
 */
public class AnnouncementNotificationGuardTest {

	@Test
	public void newImmediatePostNotifies() {
		assertTrue(AnnouncementAction.shouldSendNotification(true, false, false, null, mock(Time.class)));
	}

	@Test
	public void newDraftDoesNotNotify() {
		assertFalse(AnnouncementAction.shouldSendNotification(true, false, true, null, mock(Time.class)));
	}

	@Test
	public void editingLiveAnnouncementDoesNotResend() {
		// The core fix: re-saving an already-released announcement must not re-notify.
		assertFalse(AnnouncementAction.shouldSendNotification(false, false, false, null, mock(Time.class)));
	}

	@Test
	public void draftBeingReleasedNotifies() {
		assertTrue(AnnouncementAction.shouldSendNotification(false, true, false, null, mock(Time.class)));
	}

	@Test
	public void draftStayingHiddenDoesNotNotify() {
		assertFalse(AnnouncementAction.shouldSendNotification(false, true, true, null, mock(Time.class)));
	}

	@Test
	public void scheduledFutureReleaseNotifies() {
		Time now = mock(Time.class), release = mock(Time.class);
		when(now.before(release)).thenReturn(true);
		assertTrue(AnnouncementAction.shouldSendNotification(false, false, false, release, now));
	}

	@Test
	public void editingAfterReleaseDoesNotResend() {
		Time now = mock(Time.class), release = mock(Time.class);
		when(now.before(release)).thenReturn(false);
		assertFalse(AnnouncementAction.shouldSendNotification(false, false, false, release, now));
	}

	// resolveCommitLevel(sendNow, oneOffLevel, firstNotification, persistentLevel)
	// NOTI_REQUIRED = High, NOTI_OPTIONAL = Low, NOTI_NONE = no email.

	@Test
	public void oneOffCheckboxSendsAtItsOwnLevel() {
		// a ticked "send now" wins even when the persistent path would not send (a plain live edit)
		assertEquals(NotificationService.NOTI_OPTIONAL,
			AnnouncementAction.resolveCommitLevel(true, NotificationService.NOTI_OPTIONAL, false, NotificationService.NOTI_NONE));
	}

	@Test
	public void firstNotificationSendsAtPersistentLevel() {
		// no one-off: a genuine first notification sends at the persisted dropdown level
		assertEquals(NotificationService.NOTI_REQUIRED,
			AnnouncementAction.resolveCommitLevel(false, NotificationService.NOTI_OPTIONAL, true, NotificationService.NOTI_REQUIRED));
	}

	@Test
	public void plainLiveEditSendsNothing() {
		// no one-off and not a first notification (a plain re-save of a live item) => no email
		assertEquals(NotificationService.NOTI_NONE,
			AnnouncementAction.resolveCommitLevel(false, NotificationService.NOTI_REQUIRED, false, NotificationService.NOTI_REQUIRED));
	}

	// canSendNow(notifyNowChecked, nowHidden, releaseDate, retractDate, now) -- the one-off "Send now"
	// gate, enforced server-side so a bypassed/disabled client control cannot email a hidden, unreleased,
	// or retracted item.

	@Test
	public void sendNowRequiresTheBoxTicked() {
		assertFalse(AnnouncementAction.canSendNow(false, false, null, null, mock(Time.class)));
	}

	@Test
	public void sendNowBlockedWhileHidden() {
		assertFalse(AnnouncementAction.canSendNow(true, true, null, null, mock(Time.class)));
	}

	@Test
	public void sendNowAllowedForAVisibleItem() {
		assertTrue(AnnouncementAction.canSendNow(true, false, null, null, mock(Time.class)));
	}

	@Test
	public void sendNowBlockedForAFutureRelease() {
		Time now = mock(Time.class), release = mock(Time.class);
		when(now.before(release)).thenReturn(true);
		assertFalse(AnnouncementAction.canSendNow(true, false, release, null, now));
	}

	@Test
	public void sendNowAllowedOnceReleaseHasPassed() {
		Time now = mock(Time.class), release = mock(Time.class);
		when(now.before(release)).thenReturn(false);
		assertTrue(AnnouncementAction.canSendNow(true, false, release, null, now));
	}

	@Test
	public void sendNowBlockedAfterRetract() {
		// once the retract date has passed the item is no longer visible, so "Send now" must not fire
		Time now = mock(Time.class), retract = mock(Time.class);
		when(now.before(retract)).thenReturn(false);
		assertFalse(AnnouncementAction.canSendNow(true, false, null, retract, now));
	}

	@Test
	public void sendNowAllowedBeforeRetract() {
		Time now = mock(Time.class), retract = mock(Time.class);
		when(now.before(retract)).thenReturn(true);
		assertTrue(AnnouncementAction.canSendNow(true, false, null, retract, now));
	}

	// resolveNotificationLevel(submitted, stored, locked) -- the persisted level is server-authoritative
	// once released, and falls back to the stored value when the disabled control is omitted.

	@Test
	public void lockedLevelKeepsTheStoredValue() {
		// a released (locked) item ignores a crafted/changed submitted level
		assertEquals("r", AnnouncementAction.resolveNotificationLevel("o", "r", true));
	}

	@Test
	public void editableLevelTrustsTheSubmittedValue() {
		assertEquals("o", AnnouncementAction.resolveNotificationLevel("o", "r", false));
	}

	@Test
	public void absentLevelFallsBackToStored() {
		// a disabled control isn't submitted -> don't default to Optional, keep the stored level
		assertEquals("r", AnnouncementAction.resolveNotificationLevel(null, "r", false));
		assertEquals("r", AnnouncementAction.resolveNotificationLevel("", "r", false));
	}

	// notiLevelFor maps a stored "r"/"n"/other value to a NotificationService level.

	@Test
	public void notiLevelForMapsStoredValues() {
		assertEquals(NotificationService.NOTI_REQUIRED, AnnouncementAction.notiLevelFor("r"));
		assertEquals(NotificationService.NOTI_NONE, AnnouncementAction.notiLevelFor("n"));
		assertEquals(NotificationService.NOTI_OPTIONAL, AnnouncementAction.notiLevelFor("o"));
		assertEquals(NotificationService.NOTI_OPTIONAL, AnnouncementAction.notiLevelFor(null));
	}
}
