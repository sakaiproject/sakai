/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.rubrics.api.beans;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sakaiproject.rubrics.api.model.EvaluatedItemOwnerType;
import org.sakaiproject.rubrics.api.model.Evaluation;
import org.sakaiproject.rubrics.api.model.EvaluationStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EvaluationTransferBean {

    private Long id;
    private Long associationId;
    private Instant created;
    private String creatorId;
    private List<CriterionOutcomeTransferBean> criterionOutcomes = new ArrayList<>();
    private String evaluatedItemId;
    private String evaluatedItemOwnerId;
    private EvaluatedItemOwnerType evaluatedItemOwnerType;
    private String evaluatorId;
    private boolean isNew;
    private Instant modified;
    private String overallComment;
    private String ownerId;
    private EvaluationStatus status;

    public EvaluationTransferBean(Evaluation evaluation) {
        id = evaluation.getId();
        associationId = evaluation.getAssociationId();
        created = evaluation.getCreated();
        creatorId = evaluation.getCreatorId();
        criterionOutcomes = evaluation.getCriterionOutcomes().stream().map(CriterionOutcomeTransferBean::new).collect(Collectors.toList());
        evaluatedItemId = evaluation.getEvaluatedItemId();
        evaluatedItemOwnerId = evaluation.getEvaluatedItemOwnerId();
        evaluatedItemOwnerType = evaluation.getEvaluatedItemOwnerType();
        evaluatorId = evaluation.getEvaluatorId();
        modified = evaluation.getModified();
        overallComment = evaluation.getOverallComment();
        ownerId = evaluation.getOwnerId();
        status = evaluation.getStatus();
    }
}
