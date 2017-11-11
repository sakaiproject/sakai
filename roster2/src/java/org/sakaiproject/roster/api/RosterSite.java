/**
 * Copyright (c) 2010-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.api;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Container for a site.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@RequiredArgsConstructor
public class RosterSite {

	@Getter
	private final String id;
	@Getter @Setter
	private String title;
	@Getter @Setter
	private List<String> userRoles;
	@Getter @Setter
	private List<RosterGroup> siteGroups;
	@Getter @Setter
	private List<RosterEnrollment> siteEnrollmentSets;
	@Getter @Setter
	private Map<String, String> enrollmentStatusCodes;
	@Getter @Setter
	private int membersTotal;
	@Getter @Setter
	private Map<String, Integer> roleCounts;
}
