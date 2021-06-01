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
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.sakaiproject.rubrics.logic.listener.MetadataListener;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@Data
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@EntityListeners(MetadataListener.class)
@JsonPropertyOrder({"id", "evaluator_id", "evaluated_item_id", "evaluated_item_owner_id", "overallComment",
        "criterionOutcomes", "metadata"})
@NoArgsConstructor
@Table(name = "rbc_evaluation",
    indexes = { @Index(name = "rbc_eval_owner",  columnList="ownerId") },
    uniqueConstraints = @UniqueConstraint(columnNames = { "association_id", "evaluated_item_id", "evaluator_id" })
)
@ToString(exclude = {"toolItemRubricAssociation", "criterionOutcomes"})
public class Evaluation implements Modifiable, Serializable {

    @Id
    @SequenceGenerator(name="rbc_eval_seq", sequenceName = "rbc_eval_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_eval_seq")
    private Long id;

    @Column(name = "evaluator_id", length=99)
    @NonNull
    private String evaluatorId;

    @Column(name = "evaluated_item_id")
    @NonNull
    private String evaluatedItemId;

    @Column(name = "evaluated_item_owner_id", length=99)
    @NonNull
    private String evaluatedItemOwnerId;

    private String overallComment;

    @ManyToOne
    @JoinColumn(name = "association_id", referencedColumnName = "id", nullable = false)
    private ToolItemRubricAssociation toolItemRubricAssociation;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "rbc_eval_criterion_outcomes",
            joinColumns = @JoinColumn(name = "rbc_evaluation_id", referencedColumnName = "id", nullable = false))
    private List<CriterionOutcome> criterionOutcomes;

    @Enumerated
    @Column(nullable = false)
    private EvaluationStatus status = EvaluationStatus.DRAFT;

    @Enumerated
    @Column(name = "evaluated_item_owner_type")
    private EvaluatedItemOwnerType evaluatedItemOwnerType = EvaluatedItemOwnerType.USER;

    @Embedded
    private Metadata metadata;

    @Override
    public Metadata getModified() {
        return metadata;
    }

    @Override
    public void setModified(Metadata metadata) {
        this.metadata = metadata;
    }
}
