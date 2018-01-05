/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.services.gradebook;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;

/**
 * The GradingService calls the back end to get grading information from
 * the database.
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class GradebookServiceHelper
{

    public static boolean addToGradebook(PublishedAssessmentData publishedAssessment) throws Exception {
      return false;
    }

    public static boolean isAssignmentDefined(String assessmentTitle) throws Exception {
      return false;
    }

    public static void removeExternalAssessment(String siteId,String publishedAssessmentId) throws Exception {
    }

    public static void updateExternalAssessment(AssessmentGradingData ag, String agentIdString) {
    }

    public static boolean gradebookExists(String siteId) {
        return false;
    }

    public static void updateExternalAssessmentScore(AssessmentGradingData ag) throws Exception {
    }
}
