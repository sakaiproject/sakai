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
package org.sakaiproject.microsoft.controller.auxiliar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SiteSynchronizationRequest {
	private List<String> selectedSiteIds = new ArrayList<String>();
	private List<String> selectedTeamIds = new ArrayList<String>();
	private boolean forced = false;
	private String newTeamName;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate syncDateFrom;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate syncDateTo;
}
