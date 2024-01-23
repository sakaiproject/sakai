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
package org.sakaiproject.microsoft.api.data;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicrosoftUser {
	public static final String OWNER = "owner";
	public static final String GUEST = "guest";

	private String id;
	private String name;
	private String email;
	/**
	 * Member Id in a Team or Channel. Only set by getTeamMembers, getChannelMembers, checkUserInTeam or checkUserInChannel
	 */
	private String memberId;
	
	@Builder.Default
	private boolean owner = false;
	
	@Builder.Default
	private boolean guest = false;
}
