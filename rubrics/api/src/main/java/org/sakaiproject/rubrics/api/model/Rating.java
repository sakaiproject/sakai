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
import javax.persistence.*;

import lombok.EqualsAndHashCode;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Entity
@NoArgsConstructor
@Table(name = "rbc_rating")
public class Rating implements PersistableEntity<Long>, Serializable {

    @Id
    @SequenceGenerator(name="rbc_rat_seq", sequenceName ="rbc_rat_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_rat_seq")
    @JsonIgnore
    private Long id;

    @Column(nullable = false)
    @JacksonXmlProperty(isAttribute = true)
    private String title;

    @Lob
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JacksonXmlCData
    private String description;

    @Column(nullable = false)
    @JacksonXmlProperty(isAttribute = true)
    private Double points;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    @JsonIgnore
    private Criterion criterion;

    @Override
    public Rating clone() {
        Rating clonedRating = new Rating();
        clonedRating.setTitle(this.title);
        clonedRating.setDescription(this.description);
        clonedRating.setPoints(this.points);
        return clonedRating;
    }
}
