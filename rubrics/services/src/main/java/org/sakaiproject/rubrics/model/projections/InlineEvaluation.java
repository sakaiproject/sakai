/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.model.projections;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.sakaiproject.rubrics.model.Evaluation;
import org.sakaiproject.rubrics.model.ToolItemRubricAssociation;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "inlineEvaluation", types = { Evaluation.class })
@JsonPropertyOrder({"id", "evaluatorId", "evaluatedItemId", "evaluatedItemOwnerId", "overallComment", "metadata",
        "toolItemRubricAssociation", "criterionOutcomes"})
public interface InlineEvaluation {

    Long getId();

    String getEvaluatorId();

    String getEvaluatedItemId();

    String getEvaluatedItemOwnerId();

    String getOverallComment();

    ToolItemRubricAssociation getToolItemRubricAssociation();

    List<Evaluation.CriterionOutcome> getCriterionOutcomes();

    Evaluation.Metadata getMetadata();
}
