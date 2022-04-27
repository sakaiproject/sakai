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

import org.sakaiproject.rubrics.api.model.CriterionOutcome;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CriterionOutcomeTransferBean {

    public Long id;
    public Long criterionId;
    public Long selectedRatingId;
    public Boolean pointsAdjusted;
    public Double points;
    public String comments;

    public static CriterionOutcomeTransferBean of(CriterionOutcome outcome) {

        CriterionOutcomeTransferBean bean = new CriterionOutcomeTransferBean();
        bean.id = outcome.getId();
        bean.criterionId = outcome.getCriterionId();
        bean.selectedRatingId = outcome.getSelectedRatingId();
        bean.pointsAdjusted = outcome.getPointsAdjusted();
        bean.points = outcome.getPoints();
        bean.comments = outcome.getComments();
        return bean;
    }

    public CriterionOutcome toCriterionOutcome() {

        CriterionOutcome outcome = new CriterionOutcome();
        outcome.setId(id);
        outcome.setCriterionId(criterionId);
        outcome.setSelectedRatingId(selectedRatingId);
        outcome.setPointsAdjusted(pointsAdjusted);
        outcome.setPoints(points);
        outcome.setComments(comments);
        return outcome;
    }
}
