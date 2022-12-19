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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Entity
@NoArgsConstructor
@Table(name = "rbc_criterion")
@ToString(exclude = {"ratings", "rubric"})
public class Criterion implements PersistableEntity<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_crit_seq")
    @SequenceGenerator(name="rbc_crit_seq", sequenceName="rbc_crit_seq")
    private Long id;

    private String title;

    @Lob
    private String description;

    private Float weight = 0F;

    @EqualsAndHashCode.Exclude
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "criterion")
    private List<Rating> ratings = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "rubric_id")
    private Rubric rubric;

    @Override
    public Criterion clone() {
        Criterion clonedCriterion = new Criterion();
        clonedCriterion.setTitle(this.title);
        clonedCriterion.setDescription(this.description);
        clonedCriterion.getRatings()
                .addAll(this.getRatings().stream()
                        .map(r -> {
                            Rating clonedRating = r.clone();
                            clonedRating.setCriterion(clonedCriterion);
                            return clonedRating;
                        })
                        .collect(Collectors.toList()));
        return clonedCriterion;
    }
}
