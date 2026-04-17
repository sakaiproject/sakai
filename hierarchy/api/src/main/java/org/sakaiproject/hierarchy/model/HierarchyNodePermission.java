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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "HIERARCHY_PERMS")
@ToString(onlyExplicitlyIncluded = true)
public class HierarchyNodePermission implements PersistableEntity<Long>, Serializable {

    @Id
    @Column(name = "ID")
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "hierarchy_perm_seq")
    @SequenceGenerator(name = "hierarchy_perm_seq", sequenceName = "HIERARCHY_PERM_ID_SEQ", allocationSize = 1)
    @ToString.Include
    private Long id;

    @Column(name = "CREATEDON", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "LASTMODIFIED", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;

    @Column(name = "USERID", length = 99, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String userId;

    @Column(name = "NODEID", length = 255, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String nodeId;

    @Column(name = "PERMISSION", length = 255, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String permission;

    public HierarchyNodePermission(String userId, String nodeId, String permission) {
        if (StringUtils.isAnyBlank(userId, nodeId, permission)) {
            throw new IllegalArgumentException("None of the inputs can be null or blank: type=" + userId + ":id=" + nodeId + ":eid=" + permission);
        }
        this.userId = userId;
        this.nodeId = nodeId;
        this.permission = permission;
        this.createdOn = new Date();
        this.lastModified = this.createdOn;
    }
}
