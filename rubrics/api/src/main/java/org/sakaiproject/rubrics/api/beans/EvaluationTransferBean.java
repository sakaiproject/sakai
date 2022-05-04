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

import org.sakaiproject.rubrics.api.model.Evaluation;

import org.sakaiproject.rubrics.api.model.EvaluatedItemOwnerType;
import org.sakaiproject.rubrics.api.model.EvaluationStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluationTransferBean {

    public Long id;
    public String evaluatorId;
    public Long associationId;
    public String evaluatedItemId;
    public String evaluatedItemOwnerId;
    public String overallComment;
    public List<CriterionOutcomeTransferBean> criterionOutcomes;
    public EvaluationStatus status;
    public EvaluatedItemOwnerType evaluatedItemOwnerType;
    public Instant created;
    public Instant modified;
    public String creatorId;
    public String ownerId;

    public boolean isNew;

    public static EvaluationTransferBean of(Evaluation evaluation) {

        EvaluationTransferBean bean = new EvaluationTransferBean();
        bean.id = evaluation.getId();
        bean.associationId = evaluation.getAssociationId();
        bean.evaluatorId = evaluation.getEvaluatorId();
        bean.evaluatedItemId = evaluation.getEvaluatedItemId();
        bean.evaluatedItemOwnerId = evaluation.getEvaluatedItemOwnerId();
        bean.overallComment = evaluation.getOverallComment();
        bean.criterionOutcomes = evaluation.getCriterionOutcomes().stream().map(CriterionOutcomeTransferBean::of).collect(Collectors.toList());
        bean.status = evaluation.getStatus();
        bean.evaluatedItemOwnerType = evaluation.getEvaluatedItemOwnerType();
        bean.created = evaluation.getCreated();
        bean.modified = evaluation.getModified();
        bean.creatorId = evaluation.getCreatorId();
        bean.ownerId = evaluation.getOwnerId();
        return bean;
    }

    public Evaluation toEvaluation() {

        Evaluation evaluation = new Evaluation();
        evaluation.setId(id);
        evaluation.setAssociationId(associationId);
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setEvaluatedItemId(evaluatedItemId);
        evaluation.setEvaluatedItemOwnerId(evaluatedItemOwnerId);
        evaluation.setOverallComment(overallComment);
        evaluation.setCriterionOutcomes(criterionOutcomes.stream().map(co -> co.toCriterionOutcome()).collect(Collectors.toList()));
        evaluation.setStatus(status);
        evaluation.setEvaluatedItemOwnerType(evaluatedItemOwnerType);
        evaluation.setCreated(created);
        evaluation.setModified(modified);
        evaluation.setCreatorId(creatorId);
        evaluation.setOwnerId(ownerId);
        return evaluation;
    }
}
