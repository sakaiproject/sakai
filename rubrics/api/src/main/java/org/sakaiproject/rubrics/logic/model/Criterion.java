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
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.sakaiproject.rubrics.logic.listener.MetadataListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Entity
@EntityListeners(MetadataListener.class)
@JsonPropertyOrder({"id", "title", "description", "metadata"})
@NoArgsConstructor
@Table(name = "rbc_criterion")
@ToString(exclude = {"ratings", "rubric"})
public class Criterion implements Modifiable, Serializable, Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_crit_seq")
    @SequenceGenerator(name="rbc_crit_seq", sequenceName="rbc_crit_seq")
    private Long id;

    private String title;

    @Lob
    private String description;

    @Column(columnDefinition="float default 0")
    private Float weight = 0F;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "rbc_criterion_ratings",
            joinColumns = @JoinColumn(name = "rbc_criterion_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "ratings_id", referencedColumnName = "id", nullable = false))
    @OrderColumn(name = "order_index")
    private List<Rating> ratings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Rubric rubric;

    @Embedded
    public Metadata metadata;

    @PostLoad
    @PostUpdate
    public void determineSharedParentStatus() {
        Rubric rubric = getRubric();
        if (rubric != null && rubric.getMetadata().isShared()) {
            getMetadata().setShared(true);
        }
    }

    public Criterion clone(boolean fromRubric) throws CloneNotSupportedException {
        Criterion clonedCriterion = new Criterion();
        clonedCriterion.setId(null);
        clonedCriterion.setTitle(this.title);
        clonedCriterion.setDescription(this.description);
        if (fromRubric) {
            clonedCriterion.setWeight(this.weight);
        }
        clonedCriterion.setRatings(this.getRatings().stream().map(rating -> {
            Rating clonedRating = null;
            try {
                clonedRating = rating.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            return clonedRating;
        }).collect(Collectors.toList()));
        return clonedCriterion;
    }

    @Override
    public Metadata getModified() {
        return metadata;
    }

    @Override
    public void setModified(Metadata metadata) {
        this.metadata = metadata;
    }
}
