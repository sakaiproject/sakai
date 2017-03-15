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

package org.sakaiproject.rubrics.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true) // See https://jira.spring.io/browse/DATAREST-884
@Entity
@Table(name = "rbc_evaluation",
    uniqueConstraints = @UniqueConstraint(columnNames = { "association_id", "evaluated_item_id", "evaluator_id" })
)
@JsonPropertyOrder({"id", "evaluator_id", "evaluated_item_id", "evaluated_item_owner_id", "overallComment",
        "criterionOutcomes", "metadata"})
public class Evaluation extends BaseResource<Evaluation.Metadata> implements Serializable {

    @Id
    @SequenceGenerator(name="rbc_eval_seq", sequenceName = "rbc_eval_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_eval_seq")
    private Long id;

    @Column(name = "evaluator_id")
    @NotNull
    private String evaluatorId;

    @Column(name = "evaluated_item_id")
    @NotNull
    private String evaluatedItemId;

    @Column(name = "evaluated_item_owner_id")
    @NotNull
    private String evaluatedItemOwnerId;

    private String overallComment;

    @ManyToOne
    @JoinColumn(name = "association_id", referencedColumnName = "id", nullable = false)
    private ToolItemRubricAssociation toolItemRubricAssociation;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "rbc_eval_criterion_outcomes")
    private List<CriterionOutcome> criterionOutcomes;

    public Metadata getMetadata() {
        if (this.metadata == null) {
            this.metadata = new Metadata(); //initialize only if not set by reflection based utility like JPA or Jackson
        }
        return this.metadata;
    }

    @Embeddable
    public static class Metadata extends BaseMetadata { }

    @Data
    @Entity
    @Table(name = "rbc_criterion_outcome")
    public static class CriterionOutcome {

        @Id
        @SequenceGenerator(name="rbc_crit_out_seq", sequenceName = "rbc_crit_out_seq")
        @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_crit_out_seq")
        private Long id;

        @Column(name = "criterion_id")
        private Long criterionId;

        @ManyToOne
        @JoinColumn(name = "criterion_id", referencedColumnName = "id", insertable = false, updatable = false)
        private Criterion criterion;

        @Column(name = "selected_rating_id")
        private Long selectedRatingId;

        private boolean pointsAdjusted;

        @NotNull
        private Integer points;

        private String comments;
    }
}