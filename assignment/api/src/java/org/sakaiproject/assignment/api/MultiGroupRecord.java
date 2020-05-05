/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.assignment.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import lombok.Value;

/**
 * Immutable object pairing a user with groups they belong to
 * @author plukasew
 */
public final class MultiGroupRecord implements Serializable
{
	public final AsnUser user;
	public final List<AsnGroup> groups;

	public MultiGroupRecord(AsnUser user, List<AsnGroup> groups)
	{
		this.user = user;
		this.groups = Collections.unmodifiableList(groups);
	}

	@Value
	public static final class AsnUser implements Serializable
	{
		String id, displayId, displayName;
	}

	@Value
	public static final class AsnGroup implements Serializable
	{
		String id, title;
	}
}
