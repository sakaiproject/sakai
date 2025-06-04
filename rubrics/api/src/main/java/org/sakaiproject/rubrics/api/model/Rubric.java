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
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.KebabCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@AllArgsConstructor
@Data
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@NoArgsConstructor
@Table(name = "rbc_rubric")
@ToString(exclude = {"criteria", "associations"})
@JsonRootName(value = "rubric")
@JsonNaming(KebabCaseStrategy.class)
public class Rubric implements PersistableEntity<Long>, Serializable, Cloneable {

    @Id
    @SequenceGenerator(name="rbc_seq",sequenceName = "rbc_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator ="rbc_seq" )
    @JsonIgnore
    private Long id;

    @Column(nullable = false)
    @JacksonXmlProperty(isAttribute = true)
    private String title;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean weighted = Boolean.FALSE;

    @Column(nullable = false)
    @JacksonXmlProperty(isAttribute = true)
    private Boolean draft = Boolean.FALSE;

    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "rubric")
    @OrderColumn(name = "order_index")
    @JacksonXmlElementWrapper(localName = "criteria")
    @JacksonXmlProperty(localName = "criterion")
    private List<Criterion> criteria = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "rubric")
    @JsonIgnore
    private List<ToolItemRubricAssociation> associations = new ArrayList<>();

    @Column(nullable = false)
    @JsonIgnore
    private Instant created = Instant.now();

    @JsonIgnore
    private Instant modified;

    @Column(length = 99, nullable = false)
    @JacksonXmlProperty(isAttribute = true)
    private String ownerId;

    @Column(length = 99, nullable = false)
    @JacksonXmlProperty(isAttribute = true)
    private String creatorId;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean shared = Boolean.FALSE;

    @Column(name = "max_points")
    @JacksonXmlProperty(isAttribute = true)
    private Double maxPoints = 0D;

    @JacksonXmlProperty(isAttribute = true)
    @Column
    private Boolean adhoc = Boolean.FALSE;

    @Transient
    @JsonIgnore
    private Boolean locked = Boolean.FALSE;

    public Rubric clone(String siteId) {

        Rubric clonedRubric = new Rubric();
        clonedRubric.setCreatorId(creatorId);
        clonedRubric.setOwnerId(siteId);
        clonedRubric.setTitle(title);
        clonedRubric.setWeighted(weighted);
        clonedRubric.setLocked(false);
        clonedRubric.setShared(false);
        clonedRubric.setDraft(draft);
        clonedRubric.getCriteria()
                .addAll(this.getCriteria().stream()
                        .map(c -> {
                            Criterion clonedCriterion = c.clone();
                            clonedCriterion.setRubric(clonedRubric);
                            return clonedCriterion;
                        })
                        .collect(Collectors.toList()));
        clonedRubric.setMaxPoints(maxPoints);
        return clonedRubric;
    }
}
