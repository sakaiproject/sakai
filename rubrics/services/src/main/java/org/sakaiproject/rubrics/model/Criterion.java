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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rbc_criterion")
@JsonPropertyOrder({"id", "title", "description", "metadata"})
public class Criterion extends BaseResource<Criterion.Metadata> implements Serializable, Cloneable {

    @Id
    @SequenceGenerator(name="rbc_crit_seq", sequenceName="rbc_crit_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_crit_seq")
    private Long id;

    private String title;
    private String description;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "rbc_criterion_ratings")
    @OrderColumn(name = "order_index")
    private List<Rating> ratings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Rubric rubric;

    public Metadata getMetadata() {
        if (this.metadata == null) {
            this.metadata = new Metadata(); //initialize only if not set by reflection based utility like JPA or Jackson
        }
        return this.metadata;
    }

    @Embeddable
    public static class Metadata extends BaseMetadata {  }

    @PostLoad
    @PostUpdate
    public void determineSharedParentStatus() {
        Rubric rubric = getRubric();
        if (rubric != null && rubric.getMetadata().isShared()) {
            getMetadata().setShared(true);
        }
    }

    @Override
    public Criterion clone() throws CloneNotSupportedException {
        Criterion clonedCriterion = new Criterion();
        clonedCriterion.setId(null);
        clonedCriterion.setTitle(this.title);
        clonedCriterion.setDescription(this.description);
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
}
