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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.sakaiproject.rubrics.logic.listener.MetadataListener;
import org.sakaiproject.rubrics.logic.RubricsConstants;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@EntityListeners(MetadataListener.class)
@JsonPropertyOrder({"id", "title", "description", "metadata"})
@NoArgsConstructor
@Table(name = "rbc_rubric")
@ToString(exclude = {"criterions", "toolItemAssociations"})
public class Rubric implements Modifiable, Serializable, Cloneable {

    @Id
    @SequenceGenerator(name="rbc_seq",sequenceName = "rbc_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator ="rbc_seq" )
    private Long id;

    private String title;
    private String description;

    @Column(nullable = false)
    private Boolean weighted = Boolean.FALSE;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "rbc_rubric_criterions",
            joinColumns = @JoinColumn(name = "rbc_rubric_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "criterions_id", referencedColumnName = "id", nullable = false))
    @OrderColumn(name = "order_index")
    private List<Criterion> criterions;

    @OneToMany(mappedBy = "rubric")
    @RestResource(exported = false)
    @JsonIgnore
    private List<ToolItemRubricAssociation> toolItemAssociations;

    @Embedded
    private Metadata metadata;

    @PostLoad
    @PostUpdate
    public void determineLockStatus() {
        if (getToolItemAssociations() != null && getToolItemAssociations().size() > 0) {
            for(ToolItemRubricAssociation tira : getToolItemAssociations()) {
                if(tira.getParameters() == null) {
                    getMetadata().setLocked(true);
                } else if(!tira.getParameters().containsKey(RubricsConstants.RBCS_SOFT_DELETED) || !tira.getParameters().get(RubricsConstants.RBCS_SOFT_DELETED)) {
                    getMetadata().setLocked(true);
                    break;
                }
            }
        }
    }

    @Override
    public Rubric clone() throws CloneNotSupportedException {
        Rubric clonedRubric = new Rubric();
        clonedRubric.setId(null);
        clonedRubric.setTitle(this.title);
        clonedRubric.setWeighted(this.weighted);
        clonedRubric.setDescription(this.description);
        Metadata metadata = new Metadata();
        metadata.setLocked(false);
        metadata.setShared(false);
        clonedRubric.setMetadata(metadata);
        clonedRubric.setCriterions(this.getCriterions().stream().map(criterion -> {
            Criterion clonedCriterion = null;
            try {
                clonedCriterion = criterion.clone(true);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            return clonedCriterion;
        }).collect(Collectors.toList()));
        return clonedRubric;
    }

    public Rubric clone(String siteId) throws CloneNotSupportedException {
        Rubric clonedRubric = new Rubric();
        clonedRubric.setId(null);
        clonedRubric.setTitle(this.title);
        clonedRubric.setWeighted(this.weighted);
        clonedRubric.setDescription(this.description);
        Metadata metadata = new Metadata();
        metadata.setLocked(false);
        metadata.setShared(false);
        metadata.setOwnerId(siteId);
        clonedRubric.setMetadata(metadata);
        clonedRubric.setCriterions(this.getCriterions().stream().map(criterion -> {
            Criterion clonedCriterion = null;
            try {
				clonedCriterion = criterion.clone(true);
				Metadata cmetadata = new Metadata();
				cmetadata.setLocked(false);
				cmetadata.setShared(false);
				cmetadata.setOwnerId(siteId);
				clonedCriterion.setMetadata(cmetadata);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            return clonedCriterion;
        }).collect(Collectors.toList()));
        return clonedRubric;
    }

    @Override
    public Metadata getModified() {
        return metadata;
    }

    @Override
    public void setModified(Metadata metadata) {
        this.metadata = metadata;
    }

    @PrePersist
    public void prePersist() {
        if (this.weighted == null) {
            this.weighted = Boolean.FALSE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.weighted == null) {
            this.weighted = Boolean.FALSE;
        }
    }
}
