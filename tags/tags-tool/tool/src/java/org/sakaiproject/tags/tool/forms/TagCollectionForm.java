/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 *
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

package org.sakaiproject.tags.tool.forms;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tags.api.TagCollection;

/**
 * Maps to and from the collection HTML form and a collection data object.
 */
@Data
@Slf4j
public class TagCollectionForm extends BaseForm {

    private final String name;
    private final String description;
    private final String createdBy;
    private final Long creationDate;
    private final String externalSourceName;
    private final String externalSourceDescription;
    private final String lastModifiedBy;
    private final Long lastModificationDate;
    private final Boolean externalUpdate;
    private final Boolean externalCreation;
    private final Long lastSynchronizationDate;
    private final Long lastUpdateDateInExternalSystem  ;

    private TagCollectionForm(String uuid, String name,
                         String description, String createdBy, Long creationDate,
                         String externalSourceName, String externalSourceDescription,
                         String lastModifiedBy, Long lastModificationDate, Boolean externalUpdate, Boolean externalCreation,
                         Long lastSynchronizationDate, Long lastUpdateDateInExternalSystem) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.externalSourceName = externalSourceName;
        this.externalSourceDescription = externalSourceDescription;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModificationDate = lastModificationDate;
        this.externalUpdate= externalUpdate;
        this.externalCreation= externalCreation;
        this.lastSynchronizationDate = lastSynchronizationDate;
        this.lastUpdateDateInExternalSystem = lastUpdateDateInExternalSystem;
    }



    public static TagCollectionForm fromTagCollection(TagCollection existingTagCollection) {
        try {
            String uuid = existingTagCollection.getTagCollectionId();

            return new TagCollectionForm(uuid,
                    existingTagCollection.getName(),
                    existingTagCollection.getDescription(),
                    existingTagCollection.getCreatedBy(),
                    existingTagCollection.getCreationDate(),
                    existingTagCollection.getExternalSourceName(),
                    existingTagCollection.getExternalSourceDescription(),
                    existingTagCollection.getLastModifiedBy(),
                    existingTagCollection.getLastModificationDate(),
                    existingTagCollection.getExternalUpdate(),
                    existingTagCollection.getExternalCreation(),
                    existingTagCollection.getLastSynchronizationDate(),
                    existingTagCollection.getLastUpdateDateInExternalSystem());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    public static TagCollectionForm fromRequest(String uuid, HttpServletRequest request) {

        String name= request.getParameter("name");
        String description= request.getParameter("description");
        String createdBy= request.getParameter("createdBy");
        Long creationDate = parseNullableLong(request.getParameter("creationDate"));
        String externalSourceName= request.getParameter("externalSourceName");
        String externalSourceDescription= request.getParameter("externalSourceDescription");
        String lastModifiedBy= request.getParameter("lastModifiedBy");
        Long lastModificationDate = parseNullableLong(request.getParameter("lastModificationDate"));
        Boolean externalUpdate= BooleanUtils.toBooleanObject(request.getParameter("externalUpdate"));
        Boolean externalCreation= BooleanUtils.toBooleanObject(request.getParameter("externalCreation"));
        Long lastSynchronizationDate = parseNullableLong(request.getParameter("lastSynchronizationDate"));
        Long lastUpdateDateInExternalSystem = parseNullableLong(request.getParameter("lastUpdateDateInExternalSystem"));



        return new TagCollectionForm( uuid, name,
                 description, createdBy, creationDate,
                 externalSourceName, externalSourceDescription,
                 lastModifiedBy, lastModificationDate, externalUpdate, externalCreation,
                 lastSynchronizationDate, lastUpdateDateInExternalSystem);
    }

    public TagCollection toTagCollection() {
        return new TagCollection(uuid, name,
                description, createdBy, creationDate,
                externalSourceName, externalSourceDescription,
                lastModifiedBy, lastModificationDate, externalUpdate, externalCreation,
                lastSynchronizationDate, lastUpdateDateInExternalSystem);
    }

}

