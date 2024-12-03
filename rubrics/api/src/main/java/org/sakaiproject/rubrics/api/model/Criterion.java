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
import javax.persistence.Entity;
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Entity
@NoArgsConstructor
@Table(name = "rbc_criterion")
@ToString(exclude = {"ratings"})
public class Criterion implements PersistableEntity<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_crit_seq")
    @SequenceGenerator(name="rbc_crit_seq", sequenceName="rbc_crit_seq")
    @JsonIgnore
    private Long id;

    @JacksonXmlProperty(isAttribute = true)
    private String title;

    @Lob
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JacksonXmlCData
    private String description;

    @JacksonXmlProperty(isAttribute = true)
    private Float weight = 0F;

    @EqualsAndHashCode.Exclude
    @OrderColumn(name = "order_index")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "criterion")
    @JacksonXmlElementWrapper(localName = "ratings")
    @JacksonXmlProperty(localName = "rating")
    private List<Rating> ratings = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "rubric_id")
    @JsonIgnore
    private Rubric rubric;

    @Override
    public Criterion clone() {
        Criterion clonedCriterion = new Criterion();
        clonedCriterion.setTitle(this.title);
        clonedCriterion.setDescription(this.description);
        clonedCriterion.setWeight(this.weight);
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
