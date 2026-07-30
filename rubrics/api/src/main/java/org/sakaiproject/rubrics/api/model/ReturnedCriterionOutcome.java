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

package org.sakaiproject.rubrics.api.model;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@Entity
@Data
@NoArgsConstructor
@Table(name = "rbc_returned_criterion_out")
@ToString()
public class ReturnedCriterionOutcome implements PersistableEntity<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_ret_crit_out_seq")
    @SequenceGenerator(name="rbc_crit_out_seq", sequenceName = "rbc_ret_crit_out_seq")
    private Long id;

    @Column(name = "criterion_id")
    private Long criterionId;

    @Column(name = "selected_rating_id")
    private Long selectedRatingId;

    @Column
    private Boolean pointsAdjusted = Boolean.FALSE;

    @NonNull
    private Double points;

    @Lob
    @Column(length = 65535)
    private String comments;

    public ReturnedCriterionOutcome(CriterionOutcome outcome) {
        criterionId = outcome.getCriterionId();
        selectedRatingId = outcome.getSelectedRatingId();
        pointsAdjusted = outcome.getPointsAdjusted();
        points = outcome.getPoints();
        comments = outcome.getComments();
    }
}
