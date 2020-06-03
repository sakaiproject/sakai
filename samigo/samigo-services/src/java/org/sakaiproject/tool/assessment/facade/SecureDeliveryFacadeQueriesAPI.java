/**
 * Copyright (c) 2020 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.facade;

import java.util.List;

import org.sakaiproject.tool.assessment.data.dao.grading.SecureDeliveryData;

public interface SecureDeliveryFacadeQueriesAPI {

	String QUERY_GET_URLS_FOR_ASSESSMENT = "getEntriesForAssessment";
	String QUERY_GET_URLS_FOR_ASSESSMENT_AND_USER = "getEntriesForAssessmentAndUser";

  /**
   * Retrieve all special URLs for student delivery and instructor review of the proctoring solution
   * @param assessmentId Long assessmentId of the published assessment
   * @return
  */
  public List<SecureDeliveryData> getUrlsForAssessment(final Long assessmentId);

  /**
   * Retrieve all special URLs for student delivery and instructor review of the proctoring solution for one user only
   * @param assessmentId Long assessmentId of the published assessment
   * @return
  */
  public List<SecureDeliveryData> getUrlsForAssessmentAndUser(final Long assessmentId, final String agentId);

  public void saveUrlsForAssessmentAndUser(Long assessmentId, String agentId, String instructorUrl, String studentUrl);

}
