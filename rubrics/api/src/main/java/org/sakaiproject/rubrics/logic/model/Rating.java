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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.ToString;
import org.sakaiproject.rubrics.logic.listener.MetadataListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Entity
@EntityListeners(MetadataListener.class)
@JsonPropertyOrder({"id", "title", "description", "points", "metadata"})
@NoArgsConstructor
@Table(name = "rbc_rating")
@ToString(exclude = {"criterion"})
public class Rating implements Modifiable, Serializable, Cloneable {

    @Id
    @SequenceGenerator(name="rbc_rat_seq", sequenceName ="rbc_rat_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_rat_seq")
    private Long id;

    private String title;

    @Lob
    private String description;
    private Double points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Criterion criterion;

    @Embedded
    private Metadata metadata;

    @PostLoad
    @PostUpdate
    public void determineSharedParentStatus() {
        Criterion criterion = getCriterion();
        if (criterion != null) {
            Rubric rubric = criterion.getRubric();
            if (rubric != null && rubric.getMetadata().isShared()) {
                getMetadata().setShared(true);
            }
        }
    }

    @Override
    public Rating clone() throws CloneNotSupportedException {
        Rating clonedRating = new Rating();
        clonedRating.setId(null);
        clonedRating.setTitle(this.title);
        clonedRating.setDescription(this.description);
        clonedRating.setPoints(this.points);
        return clonedRating;
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
