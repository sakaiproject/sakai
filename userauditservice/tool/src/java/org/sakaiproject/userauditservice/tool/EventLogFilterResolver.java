/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.userauditservice.tool;

import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.TimeZone;

import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

final class EventLogFilterResolver {

	private EventLogFilterResolver() {
	}

	static Result resolve(String userIdFilter, String fromDateFilter, String toDateFilter,
			UserDirectoryService userDirectoryService, UserTimeService userTimeService) {

		String trimmedUserIdFilter = trimToNull(userIdFilter);
		String trimmedFromDateFilter = trimToNull(fromDateFilter);
		String trimmedToDateFilter = trimToNull(toDateFilter);

		String userId = null;
		if (trimmedUserIdFilter != null) {
			try {
				userId = userDirectoryService.getUserByEid(trimmedUserIdFilter).getId();
			}
			catch (UserNotDefinedException e) {
				return Result.empty(trimmedUserIdFilter, trimmedFromDateFilter, trimmedToDateFilter,
						"event_log_filter_user_not_found");
			}
		}

		TimeZone timeZone = userTimeService.getLocalTimeZone();
		try {
			EventLogFilter filter = EventLogFilter.of(userId, trimmedFromDateFilter, trimmedToDateFilter, timeZone);
			return Result.filtered(trimmedUserIdFilter, trimmedFromDateFilter, trimmedToDateFilter, filter);
		}
		catch (DateTimeParseException e) {
			return Result.empty(trimmedUserIdFilter, trimmedFromDateFilter, trimmedToDateFilter,
					"event_log_filter_invalid_date");
		}
		catch (IllegalArgumentException e) {
			return Result.empty(trimmedUserIdFilter, trimmedFromDateFilter, trimmedToDateFilter,
					"event_log_filter_to_before_from");
		}
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	static final class Result {
		final String userIdFilter;
		final String fromDateFilter;
		final String toDateFilter;
		final Optional<EventLogFilter> filter;
		final Optional<String> messageKey;

		private Result(String userIdFilter, String fromDateFilter, String toDateFilter,
				Optional<EventLogFilter> filter, Optional<String> messageKey) {
			this.userIdFilter = userIdFilter;
			this.fromDateFilter = fromDateFilter;
			this.toDateFilter = toDateFilter;
			this.filter = filter;
			this.messageKey = messageKey;
		}

		static Result filtered(String userIdFilter, String fromDateFilter, String toDateFilter, EventLogFilter filter) {
			return new Result(userIdFilter, fromDateFilter, toDateFilter, Optional.of(filter), Optional.empty());
		}

		static Result empty(String userIdFilter, String fromDateFilter, String toDateFilter, String messageKey) {
			return new Result(userIdFilter, fromDateFilter, toDateFilter, Optional.empty(), Optional.of(messageKey));
		}
	}
}
