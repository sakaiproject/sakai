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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.springframework.data.PersistableEntity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Entity
@NoArgsConstructor
@Table(name = "rbc_tool_item_rbc_assoc",
    indexes = { @Index(name = "rbc_tool_item_owner", columnList = "toolId, itemId, siteId"),
                @Index(name = "rbc_item_rubric", columnList = "itemId, rubric_id", unique = true) }
)
@ToString(exclude = {"rubric", "parameters"})
public class ToolItemRubricAssociation implements PersistableEntity<Long>, Serializable, Cloneable  {

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

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime created;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime modified;

    @Column(length = 99)
    private String creatorId;

    @Column(length = 99)
    private String siteId;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;

    @ElementCollection
    @CollectionTable(name = "rbc_tool_item_rbc_assoc_conf", joinColumns = @JoinColumn(name = "association_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "parameter_label")
    @Fetch(FetchMode.SUBSELECT)
    private Map<String, Boolean> parameters;

    public Map<String, String> getFormattedAssociation() {

        Map<String, String> formattedParams = new HashMap<>();
        formattedParams.put(RubricsConstants.RBCS_ASSOCIATE,"1");
        formattedParams.put(RubricsConstants.RBCS_LIST, String.valueOf(rubricId));
        parameters.forEach((k,v) -> formattedParams.put(RubricsConstants.RBCS_CONFIG +k, String.valueOf(v ? 1 : 0)));
        return formattedParams;
    }
}
