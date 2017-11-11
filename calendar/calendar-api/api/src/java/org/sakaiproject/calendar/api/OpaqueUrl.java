/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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
package org.sakaiproject.calendar.api;

/**
 * OpaqueURL stores the unique IDs used in calendar feeds. These unique URLs allow calendar clients to access
 * the calendar without supplying credentials.
 */
public interface OpaqueUrl {

	/**
	 * The user's ID to who this OpaqueUrl belongs.
	 * @return A User ID.
	 */
	public String getUserUUID();

	/**
	 * The calendar reference which this OpaqueUrl points to, it doesn't mean the user still has permission
	 * to view it.
	 * @return A Calendar reference.
	 */
	public String getCalendarRef();

	/**
	 * The ID which should be in the URL.
	 * @return An unguessable ID which should be part of the URL clients use to connect.
	 */
	public String getOpaqueUUID();

}
