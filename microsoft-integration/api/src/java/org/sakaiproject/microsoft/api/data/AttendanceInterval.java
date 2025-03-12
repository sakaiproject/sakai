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
import lombok.Data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceInterval {
	public String joinDateTime;
	public String leaveDateTime;
	public int durationInSeconds;

	public String getJoinDateTime() { return joinDateTime; }
	public void setJoinDateTime(String joinDateTime) { this.joinDateTime = joinDateTime; }

	public String getLeaveDateTime() { return leaveDateTime; }
	public void setLeaveDateTime(String leaveDateTime) { this.leaveDateTime = leaveDateTime; }

	public int getDurationInSeconds() { return durationInSeconds; }
	public void setDurationInSeconds(int durationInSeconds) { this.durationInSeconds = durationInSeconds; }

	public static String formatDateTime(String dateTime) {

		ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy - HH:mm:ss");
		return zonedDateTime.format(formatter);
	}
}


