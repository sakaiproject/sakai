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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfigRequest {
	List<String> synch_config_items = new ArrayList<>();
	boolean onedriveEnabled;
	long maxUploadSize;
	SakaiUserIdentifier mapped_sakai_user_id;
	MicrosoftUserIdentifier mapped_microsoft_user_id;
	SakaiSiteFilter siteFilter;
	long syncDuration;
	SakaiSiteFilter jobSiteFilter;
}
