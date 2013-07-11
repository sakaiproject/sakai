/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
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
 *
 **********************************************************************************/
package org.sakaiproject.service.gradebook.shared;

import java.util.List;

/**
 * Provider extension to integrate external assessment sources with the gradebook.
 * This is an extension of ExternalAssignmentProvider, only here for backward
 * compatibility of the 2.9 API. The reason it is added is to support tools
 * that participate in the gradebook but have not implemented the provider.
 * Before this was implemented, external items for tools without a provider
 * could never be visible in the student view of the gradebook.
 *
 * This interface should be merged into ExternalAssignmentProvider for the next
 * major release.
 *
 * See also: https://jira.sakaiproject.org/browse/SAK-23733
 * See also: https://jira.sakaiproject.org/browse/SAK-19668
 * @since 2.9.3
 */
public interface ExternalAssignmentProviderCompat {

	/**
	 * Retrieve all assignments for a gradebook that are marked as externally
	 * maintained.
	 *
	 * @param gradebookUid The gradebook's unique identifier
	 * @return A list of external IDs of assignments managed by this provider
	 */
	List<String> getAllExternalAssignments(String gradebookUid);
}

