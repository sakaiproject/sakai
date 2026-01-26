/**********************************************************************************
 * Copyright (c) 2025 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.services.question;

import java.util.List;
import java.util.Map;

public interface QuestionSearchService {

    List<QuestionSearchResult> searchByTags(List<String> tagLabels, boolean andLogic);

    List<QuestionSearchResult> searchByText(String text, boolean andLogic);

    boolean userOwnsQuestion(String questionId);

    List<String> getQuestionOrigins(String hash, Map<String, String> titleCache);
}
