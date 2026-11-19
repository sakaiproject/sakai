/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
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

package org.sakaiproject.tags.api;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.sakaiproject.springframework.data.PersistableEntity;

/**
 * The interface for the tag service.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "TagServiceCollection")
@Table(name = "tagservice_collection",
    indexes = @Index(name = "tagservice_collection_siteid", columnList = "siteid"),
    uniqueConstraints = @UniqueConstraint(name = "tagservice_site_name", columnNames = { "siteid", "name" }))
public class TagCollection implements PersistableEntity<String> {


    @Id
    @Column(name = "tagcollectionid", length = 99)
    private String tagCollectionId;
    /** Null for a global collection; otherwise its site or pool owner workspace (~userId). */
    // Pool-owner workspace scopes add ~ to a user ID of up to 99 characters.
    @Column(name = "siteid", length = 100)
    private String siteId;
    @Column(name = "name", length = 255)
    private String name;
    @Lob
    @Column(name = "description", length = Length.LONG32)
    private String description;
    @Column(name = "createdby", length = 99)
    private String createdBy;
    @Column(name = "creationdate")
    private Long creationDate;
    @Column(name = "externalsourcename", length = 255, unique = true)
    private String externalSourceName;
    @Lob
    @Column(name = "externalsourcedescription", length = Length.LONG32)
    private String externalSourceDescription;
    @Column(name = "lastmodifiedby", length = 99)
    private String lastModifiedBy;
    @Column(name = "lastmodificationdate")
    private Long lastModificationDate;
    @Column(name = "externalupdate")
    private Boolean externalUpdate;
    @Column(name = "externalcreation")
    private Boolean externalCreation;
    @Column(name = "lastsynchronizationdate")
    private Long lastSynchronizationDate;
    @Column(name = "lastupdatedateinexternalsystem")
    private Long lastUpdateDateInExternalSystem;


    @Override
    @JsonIgnore
    public String getId() {
        return tagCollectionId;
    }
}
    
