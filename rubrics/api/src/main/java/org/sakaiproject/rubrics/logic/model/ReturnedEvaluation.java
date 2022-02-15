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
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ForeignKey;
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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@NoArgsConstructor
@Table(name = "rbc_returned_evaluation",
    indexes = { @Index(name = "rbc_ret_orig_id",  columnList = "original_evaluation_id")}
)
@ToString(exclude = {"criterionOutcomes"})
public class ReturnedEvaluation implements Serializable {

    @Id
    @SequenceGenerator(name="rbc_ret_eval_seq", sequenceName = "rbc_ret_eval_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_ret_eval_seq")
    private Long id;

    @Column(name = "original_evaluation_id", nullable = false)
    private Long originalEvaluationId;

    private String overallComment;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "rbc_returned_criterion_outs",
        joinColumns = @JoinColumn(name = "rbc_returned_evaluation_id", referencedColumnName = "id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "rbc_returned_criterion_out_id", referencedColumnName = "id", nullable = false),
        foreignKey = @ForeignKey(name = "returned_evalution_id_fk"),
        inverseForeignKey = @ForeignKey(name = "returned_criterion_out_id_fk"),
        uniqueConstraints = { @UniqueConstraint(name = "returned_criterion_out_id_key", columnNames = {"rbc_returned_criterion_out_id"}) },
        indexes = { @Index(name = "returned_evaluation_id_key", columnList = "rbc_returned_evaluation_id") }
    )
    private List<ReturnedCriterionOutcome> criterionOutcomes;
}
