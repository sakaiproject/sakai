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

package org.sakaiproject.rubrics.logic.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@Entity
@Data
@NoArgsConstructor
@Table(name = "rbc_criterion_outcome")
@ToString(exclude = {"criterion"})
public class CriterionOutcome implements Serializable {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_crit_out_seq")
        @SequenceGenerator(name="rbc_crit_out_seq", sequenceName = "rbc_crit_out_seq")
        private Long id;

        @Column(name = "criterion_id")
        private Long criterionId;

        @ManyToOne
        @JoinColumn(name = "criterion_id", referencedColumnName = "id", insertable = false, updatable = false)
        private Criterion criterion;

        @Column(name = "selected_rating_id")
        private Long selectedRatingId;

        @Column(nullable = false)
        private boolean pointsAdjusted;

        @NonNull
        private Double points;

        @Lob
        @Column(length = 65535)
        private String comments;

}
