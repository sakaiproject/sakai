/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.meetings.api;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.meetings.api.model.Meeting;

public interface MeetingService {

	public Iterable<Meeting> getAllMeetings();
	public List<Meeting> getAllMeetingsFromSite(String siteId);
	public List<Meeting> getUserMeetings(String userId, String siteId, List <String> groupIds);
	public Meeting createMeeting(Meeting meetingData);
	public void updateMeeting(Meeting meetingData);
	public void deleteMeetingById(String id);
	public Optional<Meeting> getMeetingById(String id);
	public Meeting getMeeting(String id);
	public void removeSiteAndGroupAttendeesByMeetingId(String id);
	public void setMeetingProperty(Meeting meeting, String property, String value);
	public String getMeetingProperty(Meeting meeting, String property);
	public void removeMeetingProperty(Meeting meeting, String property);
	
}
