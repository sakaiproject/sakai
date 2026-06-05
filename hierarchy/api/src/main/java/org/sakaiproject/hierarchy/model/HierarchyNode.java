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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
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

/**
 * A single node in a named hierarchy. Nodes are linked via a self-referential many-to-many
 * relationship: a node may have multiple direct parents and multiple direct children, allowing
 * DAG-shaped hierarchies in addition to simple trees.
 *
 * <p>Equality and hashing are based solely on {@link #id}.</p>
 *
 * <p>The {@link #parents} and {@link #children} collections are lazily loaded and must only be
 * accessed within an active transaction.</p>
 */
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@NoArgsConstructor
@Table(name = "HIERARCHY_NODE", indexes = {
    @Index(name = "IDX_HN_HIERARCHYID", columnList = "HIERARCHYID")
})
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

    /** Token used to group nodes that share a permission boundary. */
    @Column(name = "PERMTOKEN", length = 255)
    private String permToken;

    @Column(name = "ISDISABLED", nullable = false)
    private Boolean isDisabled = false;

    /**
     * The set of parent nodes in the hierarchy that this node is a child of.
     * This represents the owning side of the many-to-many relationship between child and parent nodes.
     * A node can have multiple parents, allowing for complex hierarchical structures beyond simple trees.
     * The collection is lazily loaded and uses non-strict read-write caching for performance optimization.
     * The relationship is persisted in the HIERARCHY_NODE_PARENTS join table. This field is excluded from
     * toString() to prevent circular references.
     *
     * <p>Since these are fetched lazily, they should only be iterated on while the entity is not detached.</p>
     * 
     * @see #children
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "HIERARCHY_NODE_PARENTS",
        joinColumns = @JoinColumn(name = "NODE_ID"),
        inverseJoinColumns = @JoinColumn(name = "PARENT_NODE_ID"),
        indexes = @Index(name = "IDX_HNP_PARENT_NODE_ID", columnList = "PARENT_NODE_ID"))
    @ToString.Exclude
    private Set<HierarchyNode> parents = new HashSet<>();

    /**
     * The set of child nodes in the hierarchy that have this node as their parent.
     * This represents the inverse side of the many-to-many relationship between parent and child nodes,
     * mapped by the parents collection. The collection is lazily loaded and uses non-strict read-write
     * caching for performance optimization. This field is excluded from toString() to prevent circular
     * references.
     *
     * <p>Since these are fetched lazily, they should only be iterated on while the entity is not detached.</p>
     *
     * @see #parents
     */
    @ManyToMany(mappedBy = "parents", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<HierarchyNode> children = new HashSet<>();

}
