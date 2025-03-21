/*
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
package org.sakaiproject.microsoft.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceRecord {

		private String email;
		public String id;
		public String displayName;
		private String role;
		private int totalAttendanceInSeconds;
		private List<AttendanceInterval> attendanceIntervals;

		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }

		public String getDisplayName() { return displayName; }
		public void setDisplayName(String displayName) { this.displayName = displayName; }

		public String getRole() { return role; }
		public void setRole(String role) { this.role = role; }

		public int getTotalAttendanceInSeconds() { return totalAttendanceInSeconds; }
		public void setTotalAttendanceInSeconds(int totalAttendanceInSeconds) { this.totalAttendanceInSeconds = totalAttendanceInSeconds; }

		public List<AttendanceInterval> getAttendanceIntervals() { return attendanceIntervals; }
		public void setAttendanceIntervals(List<AttendanceInterval> attendanceIntervals) { this.attendanceIntervals = attendanceIntervals; }
}

