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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true) // ConstructorProperties negatively affect JsonIgnore - see https://github.com/FasterXML/jackson-databind/issues/1317#issuecomment-244495737
@Entity
@Table(name = "rbc_rubric")
@JsonPropertyOrder({"id", "title", "description", "metadata"})
public class Rubric extends BaseResource<Rubric.Metadata> implements Serializable, Cloneable {

    @Id
    @SequenceGenerator(name="rbc_seq",sequenceName = "rbc_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator ="rbc_seq" )
    private Long id;

    private String title;
    private String description;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "rbc_rubric_criterions")
    @OrderColumn(name = "order_index")
    private List<Criterion> criterions;

    @OneToMany(mappedBy = "rubric")
    @RestResource(exported = false)
    @JsonIgnore
    private List<ToolItemRubricAssociation> toolItemAssociations;

    public Metadata getMetadata() {
        if (this.metadata == null) {
            this.metadata = new Metadata(); //initialize only if not set by reflection based utility like JPA or Jackson
        }
        return this.metadata;
    }

    @Data
    @Embeddable
    public static class Metadata extends BaseMetadata {

        /**
         * This shared field masks the @Transient version in the BaseMetadata and makes this one persistable.
         */
        @JsonProperty("public")
        @Column(name = "shared", insertable = false, updatable = false)
        private boolean shared;

        @Transient
        private boolean locked;
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        this.getMetadata().setShared(false);
        this.getMetadata().setLocked(false);
    }

    @PostLoad
    @PostUpdate
    public void determineLockStatus() {
        if (getToolItemAssociations() != null && getToolItemAssociations().size() > 0) {
            getMetadata().setLocked(true);
        }
    }

    @Override
    public Rubric clone() throws CloneNotSupportedException {
        Rubric clonedRubric = new Rubric();
        clonedRubric.setId(null);
        clonedRubric.setTitle(this.title);
        clonedRubric.setDescription(this.description);
        Metadata metadata = new Metadata();
        metadata.setLocked(false);
        metadata.setShared(false);
        clonedRubric.setMetadata(metadata);
        clonedRubric.setCriterions(this.getCriterions().stream().map(criterion -> {
            Criterion clonedCriterion = null;
            try {
                clonedCriterion = criterion.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            return clonedCriterion;
        }).collect(Collectors.toList()));
        return clonedRubric;
    }
}
