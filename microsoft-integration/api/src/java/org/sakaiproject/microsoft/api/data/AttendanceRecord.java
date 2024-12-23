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

