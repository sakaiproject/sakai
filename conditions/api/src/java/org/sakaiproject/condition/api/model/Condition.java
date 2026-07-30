/**
 * Copyright (c) 2023 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.condition.api.model;

import java.util.Arrays;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Entity
@Table(name = "COND_CONDITION",
        indexes = { @Index(name = "IDX_CONDITION_SITE_ID", columnList = "SITE_ID") })
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "subConditions", "parentConditions" })
public class Condition {


    @Id
    @Column(name = "ID", nullable = false, length = 36)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @NonNull
    @Column(name = "COND_TYPE", length = 99, nullable = false)
    @Enumerated(EnumType.STRING)
    private ConditionType type;

    @Column(name = "OPERATOR", length = 99)
    @Enumerated(EnumType.STRING)
    private ConditionOperator operator;

    @Column(name = "ARGUMENT", length = 999)
    private String argument;

    @Column(name = "SITE_ID", length = 36, nullable = false)
    private String siteId;

    @Column(name = "TOOL_ID", length = 99, nullable = false)
    private String toolId;

    @Column(name = "ITEM_ID", length = 99)
    private String itemId;

    @JsonIgnore
    @ManyToMany(mappedBy = "subConditions", cascade = CascadeType.ALL)
    private Set<Condition> parentConditions;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "COND_PARENT_CHILD",
            joinColumns = { @JoinColumn(name = "PARENT_ID", referencedColumnName = "ID") },
            inverseJoinColumns = { @JoinColumn(name="CHILD_ID", referencedColumnName = "ID") })
    private Set<Condition> subConditions;

    public Boolean getHasParent() {
        return this.parentConditions != null
                ? this.parentConditions.size() > 0
                : null;
    }

    // Only show ids of parent-conditions in string representation of condition
    @ToString.Include
    private String parentConditions() {
        return parentConditions != null
                ? Arrays.deepToString(parentConditions.stream()
                                .map(Condition::getId)
                                .toArray(String[]::new))
                : "null";
    }

    // Only show ids of sub-conditions in string representation of condition
    @ToString.Include
    private String subConditions() {
        return subConditions != null
                ? Arrays.deepToString(subConditions.stream()
                                .map(Condition::getId)
                                .toArray(String[]::new))
                : "null";
    }
}
