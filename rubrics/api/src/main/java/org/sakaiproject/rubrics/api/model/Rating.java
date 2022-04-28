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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.sakaiproject.springframework.data.PersistableEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Entity
@NoArgsConstructor
@Table(name = "rbc_rating")
@ToString(exclude = {"criterion"})
public class Rating implements PersistableEntity<Long>, Serializable {

    @Id
    @SequenceGenerator(name="rbc_rat_seq", sequenceName ="rbc_rat_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_rat_seq")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private Double points;

    //@ManyToOne(fetch = FetchType.LAZY)
    //private Criterion criterion;

    @Override
    public Rating clone() {

        Rating clonedRating = new Rating();
        clonedRating.setId(null);
        clonedRating.setTitle(this.title);
        clonedRating.setDescription(this.description);
        clonedRating.setPoints(this.points);
        return clonedRating;
    }
}
