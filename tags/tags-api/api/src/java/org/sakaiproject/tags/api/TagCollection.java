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


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.sakaiproject.springframework.data.PersistableEntity;

/**
 * The interface for the tag service.
 */
@Data
@NoArgsConstructor
@Entity(name = "TagServiceCollection")
@Table(name = "tagservice_collection")
public class TagCollection implements PersistableEntity<String> {


    @Id
    @Column(name = "tagcollectionid", length = 99)
    private String tagCollectionId;
    @Column(name = "name", length = 255, unique = true)
    private String name;
    @Lob
    @Column(name = "description", length = 65535)
    private String description;
    @Column(name = "createdby", length = 99)
    private String createdBy;
    @Column(name = "creationdate")
    private Long creationDate;
    @Column(name = "externalsourcename", length = 255, unique = true)
    private String externalSourceName;
    @Lob
    @Column(name = "externalsourcedescription", length = 65535)
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


    public TagCollection(String tagCollectionId, String name, String description, String createdBy, long creationDate, String externalSourceName, String externalSourceDescription, String lastModifiedBy, long lastModificationDate, Boolean externalUpdate, Boolean externalCreation, long lastSynchronizationDate, long lastUpdateDateInExternalSystem) {
        this.tagCollectionId = tagCollectionId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.externalSourceName = externalSourceName;
        this.externalSourceDescription = externalSourceDescription;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModificationDate = lastModificationDate;
        this.externalUpdate = externalUpdate;
        this.externalCreation = externalCreation;
        this.lastSynchronizationDate = lastSynchronizationDate;
        this.lastUpdateDateInExternalSystem = lastUpdateDateInExternalSystem;
    }

    public long getCreationDate() {
        return creationDate == null ? 0L : creationDate;
    }

    public long getLastModificationDate() {
        return lastModificationDate == null ? 0L : lastModificationDate;
    }

    public Boolean getExternalUpdate() {
        return Boolean.TRUE.equals(externalUpdate);
    }

    public Boolean getExternalCreation() {
        return Boolean.TRUE.equals(externalCreation);
    }

    public long getLastSynchronizationDate() {
        return lastSynchronizationDate == null ? 0L : lastSynchronizationDate;
    }

    public long getLastUpdateDateInExternalSystem() {
        return lastUpdateDateInExternalSystem == null ? 0L : lastUpdateDateInExternalSystem;
    }

    @Override
    @JsonIgnore
    public String getId() {
        return tagCollectionId;
    }

    public Errors validate() {
        Errors errors = new Errors();
        //At this moment there is not extra validation. This can be the place to do this in the future
        return errors;
    }
}
    
