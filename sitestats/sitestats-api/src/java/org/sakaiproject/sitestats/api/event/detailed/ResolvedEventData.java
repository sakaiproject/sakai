/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.sitestats.api.event.detailed;

/**
 * Common empty interface to tag the various data objects returned by ref resolvers.
 *
 * @author plukasew, bjones86
 */
public interface ResolvedEventData
{
	public static final class Error implements ResolvedEventData {} // ref does not contain expected data or other general error state
	public static final class PermissionError implements ResolvedEventData {} // user does not have permission to retrieve data for this event
	public static final class NoDetails implements ResolvedEventData {} // particular ref cannot provide further details

	public static final Error ERROR = new Error();
	public static final PermissionError PERM_ERROR = new PermissionError();
	public static final NoDetails NO_DETAILS = new NoDetails();
}
