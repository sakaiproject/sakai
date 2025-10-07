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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tags.api.Errors;
import org.sakaiproject.tags.api.MissingUuidException;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.tool.handlers.CrudHandler;
import org.sakaiproject.util.api.FormattedText;

import java.util.Optional;

/**
 * Maps to and from the tag HTML form and a tag data object.
 */
@Data
@Slf4j
public class TagForm extends BaseForm {

    private final String tagCollectionId;
    private final String tagLabel;
    private final String description;
    private final String createdBy;
    private final long creationDate;
    private final String lastModifiedBy;
    private final long lastModificationDate;
    private final String externalId;
    private final String alternativeLabels;
    private final Boolean externalCreation;
    private final long externalCreationDate;
    private final Boolean externalUpdate;
    private final long lastUpdateDateInExternalSystem;
    private final String parentId;
    private final String externalHierarchyCode;
    private final String externalType;
    private final String data;
    private final String collectionName;


    public TagForm(String uuid, String tagCollectionId, String tagLabel, String description, String createdBy,
               long creationDate, String lastModifiedBy, long lastModificationDate, String externalId,
               String alternativeLabels, Boolean externalCreation, long externalCreationDate,
               Boolean externalUpdate, long lastUpdateDateInExternalSystem, String parentId,
               String externalHierarchyCode, String externalType, String data, String collectionName) {
        this.uuid = uuid;
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
        this.externalUpdate= externalUpdate;
        this.lastUpdateDateInExternalSystem = lastUpdateDateInExternalSystem;
        this.parentId = parentId;
        this.externalHierarchyCode = externalHierarchyCode;
        this.externalType = externalType;
        this.data = data;
        this.collectionName = collectionName;
    }



    public static TagForm fromTag(Tag existingTag) {

            String uuid = existingTag.getTagId();
            if (uuid==null){
                throw new RuntimeException("No tagId has been set for this tag");
            }
            return new TagForm(uuid,
                    existingTag.getTagCollectionId(),
                    existingTag.getTagLabel(),
                    existingTag.getDescription(),
                    existingTag.getCreatedBy(),
                    existingTag.getCreationDate(),
                    existingTag.getLastModifiedBy(),
                    existingTag.getLastModificationDate(),
                    existingTag.getExternalId(),
                    existingTag.getAlternativeLabels(),
                    existingTag.getExternalCreation(),
                    existingTag.getExternalCreationDate(),
                    existingTag.getExternalUpdate(),
                    existingTag.getLastUpdateDateInExternalSystem(),
                    existingTag.getParentId(),
                    existingTag.getExternalHierarchyCode(),
                    existingTag.getExternalType(),
                    existingTag.getData(),
                    existingTag.getCollectionName());
    }

    public static TagForm fromRequest(String uuid, HttpServletRequest request) {

        String tagCollectionId = request.getParameter("tagCollectionId");
        String tagLabel = request.getParameter("tagLabel");
        String description = request.getParameter("description");
        String createdBy = request.getParameter("createdBy");
        long creationDate;
        try {
            creationDate= Long.parseLong(request.getParameter("creationDate"));
        }catch (Exception e){
            creationDate=0L;
        }
        String lastModifiedBy = request.getParameter("lastModifiedBy");
        long lastModificationDate;
        try {
            lastModificationDate= Long.parseLong(request.getParameter("lastModificationDate"));
        }catch (Exception e){
            lastModificationDate=0L;
        }
        String externalId = request.getParameter("externalId");
        String alternativeLabels = request.getParameter("alternativeLabels");
        Boolean externalCreation = "true".equals(request.getParameter("externalCreation"));
        long externalCreationDate;
        try {
            externalCreationDate= Long.parseLong(request.getParameter("externalCreationDate"));
        }catch (Exception e){
            externalCreationDate=0L;
        }
        Boolean externalUpdate= "true".equals(request.getParameter("externalUpdate"));
        long lastUpdateDateInExternalSystem;
        try {
            lastUpdateDateInExternalSystem= Long.parseLong(request.getParameter("lastUpdateDateInExternalSystem"));
        }catch (Exception e){
            lastUpdateDateInExternalSystem=0L;
        }
        String parentId = request.getParameter("parentId");
        String externalHierarchyCode = request.getParameter("externalHierarchyCode");
        String externalType = request.getParameter("externalType");
        String data = request.getParameter("data");
        String collectionName = request.getParameter("collectionName");


        return new TagForm(uuid, tagCollectionId, tagLabel, description, createdBy,
                creationDate, lastModifiedBy, lastModificationDate, externalId,
                alternativeLabels, externalCreation, externalCreationDate,
                externalUpdate, lastUpdateDateInExternalSystem , parentId,
                externalHierarchyCode, externalType, data, collectionName);
    }

    public Errors validate(FormattedText formattedText, CrudHandler.CrudMode mode) {
        Errors errors = new Errors();

        // Validate required fields
        if (tagLabel == null || tagLabel.trim().isEmpty()) {
            errors.addError("tagLabel", "tag_label_required");
        }

        // Validate field lengths
        if (tagLabel != null && tagLabel.length() > 255) {
            errors.addError("tagLabel", "tag_label_too_long");
        }

        if (description != null && description.length() > 1000) {
            errors.addError("description", "description_too_long");
        }

        if (externalId != null && externalId.length() > 255) {
            errors.addError("externalId", "external_id_too_long");
        }

        // XSS validation checks
        StringBuilder tagMessages = new StringBuilder();
        formattedText.processFormattedText(tagLabel, tagMessages);
        if (!tagMessages.isEmpty()) {
            errors.addError("tagLabel", "contains_xss");
        }

        StringBuilder descriptionMessages = new StringBuilder();
        formattedText.processFormattedText(description, descriptionMessages);
        if (!descriptionMessages.isEmpty()) {
            errors.addError("description", "contains_xss");
        }

        // Merge with model-level validation errors
        Errors modelErrors = toTag().validate();

        return errors.merge(modelErrors);
    }

    public Tag toTag() {
        return new Tag(uuid, tagCollectionId, tagLabel, description, createdBy,
                creationDate, lastModifiedBy, lastModificationDate, externalId,
                alternativeLabels, externalCreation, externalCreationDate,
                externalUpdate, lastUpdateDateInExternalSystem , parentId,
                externalHierarchyCode, externalType, data, collectionName);
    }
}
