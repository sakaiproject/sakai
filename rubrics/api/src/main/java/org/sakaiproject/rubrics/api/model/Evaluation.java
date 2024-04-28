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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@Data
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@NoArgsConstructor
@Table(name = "rbc_evaluation",
    indexes = { @Index(name = "rbc_eval_owner",  columnList="ownerId"),
                @Index(name = "rbc_eval_association_idx",  columnList="association_id") },
    uniqueConstraints = @UniqueConstraint(columnNames = { "association_id", "evaluated_item_id", "evaluated_item_owner_id" })
)
@ToString(exclude = {"criterionOutcomes"})
public class Evaluation implements PersistableEntity<Long>, Serializable {

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

    @Column(name = "association_id", nullable = false)
    private Long associationId;

    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "rbc_eval_criterion_outcomes",
            joinColumns = @JoinColumn(name = "rbc_evaluation_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "criterionOutcomes_id", referencedColumnName = "id", nullable = false))
    private List<CriterionOutcome> criterionOutcomes = new ArrayList<>();

    @Enumerated
    private EvaluationStatus status = EvaluationStatus.RETURNED;

    @Enumerated
    @Column(name = "evaluated_item_owner_type")
    private EvaluatedItemOwnerType evaluatedItemOwnerType = EvaluatedItemOwnerType.USER;

    private Instant created;

    private Instant modified;

    @Column(length = 99)
    private String creatorId;

    @Column(length = 99)
    private String ownerId;
}
