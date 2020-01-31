/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;

public class AnnouncementWrapperTest {

	@Test
	public void computesTwelveHourOldMessageWithinOneDay() {
		AnnouncementMessage message = mock(AnnouncementMessage.class);
		AnnouncementMessageHeader header = mock(AnnouncementMessageHeader.class);

		when(message.getHeader()).thenReturn(header);
		when(header.getInstant()).thenReturn(twelveHoursAgo());

		assertThat(AnnouncementWrapper.isMessageWithinLastNDays(message, 1), is(true));
	}

	@Test
	public void computesTwoWeekOldMessageNotWithinSevenDays() {
		AnnouncementMessage message = mock(AnnouncementMessage.class);
		AnnouncementMessageHeader header = mock(AnnouncementMessageHeader.class);

		when(message.getHeader()).thenReturn(header);
		when(header.getInstant()).thenReturn(twoWeeksAgo());

		assertThat(AnnouncementWrapper.isMessageWithinLastNDays(message, 7), is(false));
	}

	private Instant twelveHoursAgo() {
		return Instant.now().minus(Duration.ofHours(12));
	}

	private Instant twoWeeksAgo() {
		return Instant.now().minus(Duration.ofDays(14));
	}
}
