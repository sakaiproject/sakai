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
package org.sakaiproject.announcement.entityprovider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;

/**
 * Covers the per-student "email notifications enabled" resolution that drives the Yes/No
 * column (and the light-red highlighting) in the read-receipts overlay: only an explicit
 * "None" announcement notification preference counts as disabled; every other value — and a
 * missing preference — is treated as enabled.
 */
public class AnnouncementEntityProviderReadReceiptsTest {

	private static final String USER = "user-1";

	@Mock private PreferencesService preferencesService;
	@Mock private Preferences preferences;
	@Mock private ResourceProperties properties;

	private AnnouncementEntityProviderImpl provider;
	private AutoCloseable mocks;

	@Before
	public void setUp() throws Exception {
		mocks = MockitoAnnotations.openMocks(this);
		provider = new AnnouncementEntityProviderImpl();
		provider.setPreferencesService(preferencesService);
		when(preferencesService.getPreferences(USER)).thenReturn(preferences);
		when(preferences.getProperties(anyString())).thenReturn(properties);
	}

	@After
	public void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	public void emailDisabledWhenPreferenceIsNone() throws Exception {
		when(properties.getLongProperty(anyString())).thenReturn(0L); // NOTI_NONE
		assertFalse(provider.isAnnouncementEmailEnabled(USER));
	}

	@Test
	public void emailEnabledWhenPreferenceIsEachEmail() throws Exception {
		when(properties.getLongProperty(anyString())).thenReturn(1L); // NOTI_REQUIRED
		assertTrue(provider.isAnnouncementEmailEnabled(USER));
	}

	@Test
	public void emailEnabledWhenPreferenceIsDigest() throws Exception {
		when(properties.getLongProperty(anyString())).thenReturn(2L); // NOTI_OPTIONAL
		assertTrue(provider.isAnnouncementEmailEnabled(USER));
	}

	@Test
	public void emailEnabledByDefaultWhenPreferenceMissing() throws Exception {
		// A missing/unparseable preference throws; the resolver defaults to enabled.
		when(properties.getLongProperty(anyString())).thenThrow(new RuntimeException("no such property"));
		assertTrue(provider.isAnnouncementEmailEnabled(USER));
	}
}
