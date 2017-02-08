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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true) // See https://jira.spring.io/browse/DATAREST-884
@Entity
@Table(name = "rbc_tool_item_rbc_assoc")
@JsonPropertyOrder({"id", "toolId", "itemId", "rubricId", "parameters", "metadata"})
public class ToolItemRubricAssociation extends BaseResource<ToolItemRubricAssociation.Metadata>
        implements Serializable, Cloneable  {

    @Id
    @SequenceGenerator(name="rbc_tool_item_rbc_seq", sequenceName = "rbc_tool_item_rbc_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rbc_tool_item_rbc_seq")
    private Long id;

    private String toolId;
    private String itemId;

    @Column(name = "rubric_id")
    private Long rubricId;

    @ManyToOne
    @JoinColumn(name = "rubric_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Rubric rubric;

    public Metadata getMetadata() {
        if (this.metadata == null) {
            this.metadata = new Metadata(); //initialize only if not set by reflection based utility like JPA or Jackson
        }
        return this.metadata;
    }

    @Embeddable
    public static class Metadata extends BaseMetadata { }

    @ElementCollection
    @CollectionTable(name = "rbc_tool_item_rbc_assoc_conf", joinColumns = @JoinColumn(name = "association_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "parameter_label")
    @Column(name="parameters")
    private Map<String, Boolean> parameters;

    public Map<String, Boolean> getParameters() {
        return parameters;
    }

    public Boolean getParameter(String param) {
        return parameters.get(param);
    }
}
