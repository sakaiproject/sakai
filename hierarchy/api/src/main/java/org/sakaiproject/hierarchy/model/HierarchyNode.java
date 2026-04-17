/**
 * Copyright (c) 2007-2024 The Apereo Foundation
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
package org.sakaiproject.hierarchy.model;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@NoArgsConstructor
@Table(name = "HIERARCHY_NODE")
public class HierarchyNode implements PersistableEntity<Long> {

    @Id
    @Column(name = "ID")
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "hierarchy_node_seq")
    @SequenceGenerator(name = "hierarchy_node_seq", sequenceName = "HIERARCHY_NODE_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "HIERARCHYID", length = 255)
    private String hierarchyId;

    @Column(name = "ISROOTNODE", nullable = false)
    private Boolean isRootNode = false;

    @Column(name = "OWNERID", length = 99)
    private String ownerId;

    @Column(name = "TITLE", length = 255)
    private String title;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PERMTOKEN", length = 255)
    private String permToken;

    @Column(name = "ISDISABLED", nullable = false)
    private Boolean isDisabled = false;

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "HIERARCHY_NODE_PARENTS",
        joinColumns = @JoinColumn(name = "NODE_ID"),
        inverseJoinColumns = @JoinColumn(name = "PARENT_NODE_ID"))
    @ToString.Exclude
    private Set<HierarchyNode> parents = new HashSet<>();

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ManyToMany(mappedBy = "parents", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<HierarchyNode> children = new HashSet<>();

    public Set<String> getDirectParentNodeIds() {
        return parents.stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
    }

    public Set<String> getDirectChildNodeIds() {
        return children.stream().map(n -> n.getId().toString()).collect(Collectors.toSet());
    }
}
