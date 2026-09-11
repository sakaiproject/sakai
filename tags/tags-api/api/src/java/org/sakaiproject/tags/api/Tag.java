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
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.sakaiproject.springframework.data.PersistableEntity;

/**
 * A data object representing a tag.
 */
@Data
@NoArgsConstructor
@Entity(name = "TagServiceTag")
@Table(name = "tagservice_tag", indexes = {
    @Index(name = "tagservice_tag_taglabel", columnList = "taglabel"),
    @Index(name = "tagservice_tag_tagcollectionid", columnList = "tagcollectionid"),
    @Index(name = "tagservice_tag_externalid", columnList = "externalid")
})
public class Tag implements PersistableEntity<String> {

    @Id
    @Column(name = "tagid", length = 36)
    private String tagId;
    @Column(name = "tagcollectionid", length = 36, nullable = false)
    private String tagCollectionId;
    @Column(name = "taglabel", length = 255)
    private String tagLabel;
    @Lob
    @Column(name = "description", length = 65535)
    private String description;
    @Column(name = "createdby", length = 255)
    private String createdBy;
    @Column(name = "creationdate")
    private Long creationDate;
    @Column(name = "lastmodifiedby", length = 255)
    private String lastModifiedBy;
    @Column(name = "lastmodificationdate")
    private Long lastModificationDate;
    @Column(name = "externalid", length = 255)
    private String externalId;
    @Lob
    @Column(name = "alternativelabels", length = 65535)
    private String alternativeLabels;
    @Column(name = "externalcreation")
    private Boolean externalCreation;
    @Column(name = "externalcreationdate")
    private Long externalCreationDate;
    @Column(name = "externalupdate")
    private Boolean externalUpdate;
    @Column(name = "lastupdatedateinexternalsystem")
    private Long lastUpdateDateInExternalSystem;
    @Column(name = "parentid", length = 255)
    private String parentId;
    @Lob
    @Column(name = "externalhierarchycode", length = 65535)
    private String externalHierarchyCode;
    @Column(name = "externaltype", length = 255)
    private String externalType;
    @Lob
    @Column(name = "data", length = 65535)
    private String data;
    @Transient
    private String collectionName;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagcollectionid", insertable = false, updatable = false,
        foreignKey = @ForeignKey(name = "tagservice_tag_fk"))
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TagCollection collection;

    public Tag(String tagId, String tagCollectionId, String tagLabel, String description, String createdBy, long creationDate, String lastModifiedBy, long lastModificationDate, String externalId, String alternativeLabels, Boolean externalCreation, long externalCreationDate, Boolean externalUpdate, long lastUpdateDateInExternalSystem, String parentId, String externalHierarchyCode, String externalType, String data, String collectionName) {
        this.tagId = tagId;
        this.tagCollectionId = tagCollectionId;
        this.tagLabel = tagLabel;
        this.description = description;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModificationDate = lastModificationDate;
        this.externalId = externalId;
        this.alternativeLabels = alternativeLabels;
        this.externalCreation = externalCreation;
        this.externalCreationDate = externalCreationDate;
        this.externalUpdate = externalUpdate;
        this.lastUpdateDateInExternalSystem = lastUpdateDateInExternalSystem;
        this.parentId = parentId;
        this.externalHierarchyCode = externalHierarchyCode;
        this.externalType = externalType;
        this.data = data;
        this.collectionName = collectionName;
    }

    public long getCreationDate() {
        return creationDate == null ? 0L : creationDate;
    }

    public long getLastModificationDate() {
        return lastModificationDate == null ? 0L : lastModificationDate;
    }

    public Boolean getExternalCreation() {
        return Boolean.TRUE.equals(externalCreation);
    }

    public long getExternalCreationDate() {
        return externalCreationDate == null ? 0L : externalCreationDate;
    }

    public Boolean getExternalUpdate() {
        return Boolean.TRUE.equals(externalUpdate);
    }

    public long getLastUpdateDateInExternalSystem() {
        return lastUpdateDateInExternalSystem == null ? 0L : lastUpdateDateInExternalSystem;
    }

    @Override
    @JsonIgnore
    public String getId() {
        return tagId;
    }

    public Errors validate() {
        Errors errors = new Errors();
        //At this moment there is not extra validation. This can be the place to do this in the future
        return errors;
    }


}
    
