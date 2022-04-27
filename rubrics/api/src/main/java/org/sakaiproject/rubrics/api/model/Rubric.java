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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@NoArgsConstructor
@Table(name = "rbc_rubric")
@ToString(exclude = {"criterions", "toolItemAssociations"})
public class Rubric implements PersistableEntity<Long>, Serializable, Cloneable {

    @Id
    @SequenceGenerator(name="rbc_seq",sequenceName = "rbc_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator ="rbc_seq" )
    private Long id;

    private String title;

    private Boolean weighted = Boolean.FALSE;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "rubric_id")
    @OrderColumn(name = "order_index")
    private List<Criterion> criteria = new ArrayList<>();

    @OneToMany(mappedBy = "rubric")
    private List<ToolItemRubricAssociation> associations;

    private Instant created;

    private Instant modified;

    @Column(length = 99)
    private String ownerId;

    @Column(length = 99)
    private String creatorId;

    private Boolean shared = Boolean.FALSE;

    @Transient
    private Boolean locked = Boolean.FALSE;

    @PostLoad
    @PostUpdate
    public void determineLockStatus() {

        if (getAssociations() != null && getAssociations().size() > 0) {
            for (ToolItemRubricAssociation tira : getAssociations()) {
                if (tira.getParameters() == null) {
                    locked = true;
                } else if (!tira.getParameters().containsKey(RubricsConstants.RBCS_SOFT_DELETED) || !tira.getParameters().get(RubricsConstants.RBCS_SOFT_DELETED)) {
                    locked = true;
                    break;
                }
            }
        }
    }

    public Rubric clone(String siteId) {

        Rubric clonedRubric = new Rubric();
        clonedRubric.setId(null);
        clonedRubric.setOwnerId(siteId);
        clonedRubric.setTitle(this.title);
        clonedRubric.setWeighted(this.weighted);
        clonedRubric.setLocked(false);
        clonedRubric.setShared(false);
        clonedRubric.setOwnerId(this.ownerId);
        clonedRubric.setCriteria(this.getCriteria().stream().map(c -> c.clone())
            .collect(Collectors.toList()));
        return clonedRubric;
    }
}
