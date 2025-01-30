/**
 * Copyright (c) ${license.git.copyrightYears} ${holder}
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

import java.util.Optional;

import org.sakaiproject.tool.assessment.data.dao.assessment.SebValidationData;

public interface SebValidationFacadeQueriesAPI {

  public String QUERY_GET_ENTRY_FOR_ASSESSMENT_AND_AGENT = "getSebValidationsForAssessmentAndAgent";
  public String QUERY_EXPIRE_CONFIGS_FOR_ASSESSMENT_AND_AGENT = "expireSebValidationsForAssessmentAndAgent";

  /**
   * Retrieve SEB assessment configuration
   * @param assessmentId Long assessmentId of the published assessment
   * @return
  */
  public Optional<SebValidationData> getLastSebValidation(final Long assessmentId, final String agentId);

  public void saveSebValidation(Long assessmentId, String agentId, String url, String configKeyHash, String examKeyHash);

  public void expireSebValidations(Long assessmentId, String agentId);
}
