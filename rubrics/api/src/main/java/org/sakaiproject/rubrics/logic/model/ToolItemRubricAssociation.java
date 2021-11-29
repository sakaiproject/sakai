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
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.sakaiproject.rubrics.logic.listener.MetadataListener;
import org.sakaiproject.rubrics.logic.RubricsConstants;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Entity
@EntityListeners(MetadataListener.class)
@JsonPropertyOrder({"id", "toolId", "itemId", "rubricId", "parameters", "metadata"})
@NoArgsConstructor
@Table(name = "rbc_tool_item_rbc_assoc",
    indexes = {@Index(name = "rbc_tool_item_owner", columnList = "toolId, itemId, ownerId")}
)
@ToString(exclude = {"rubric", "parameters"})
public class ToolItemRubricAssociation implements Modifiable, Serializable, Cloneable  {

    @Id
    @SequenceGenerator(name="rbc_tool_item_rbc_seq", sequenceName = "rbc_tool_item_rbc_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_tool_item_rbc_seq")
    private Long id;

    @Column(length=99)
    private String toolId;
    private String itemId;

    @Column(name = "rubric_id")
    private Long rubricId;

    @ManyToOne
    @JoinColumn(name = "rubric_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Rubric rubric;

    @Embedded
    private Metadata metadata;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;

    @ElementCollection
    @CollectionTable(name = "rbc_tool_item_rbc_assoc_conf", joinColumns = @JoinColumn(name = "association_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "parameter_label")
    @Column(name="parameters")
    @Fetch(FetchMode.SUBSELECT)
    private Map<String, Boolean> parameters;

    public Map<String, String> getFormattedAssociation() {
        Map<String, String> formattedParams = new HashMap<>();
        formattedParams.put(RubricsConstants.RBCS_ASSOCIATE,"1");
        formattedParams.put(RubricsConstants.RBCS_LIST, String.valueOf(rubricId));
        parameters.forEach((k,v) -> formattedParams.put(RubricsConstants.RBCS_CONFIG +k, String.valueOf(v ? 1 : 0)));
        return formattedParams;
    }

    public Boolean getParameter(String param) {
        return parameters.get(param);
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
