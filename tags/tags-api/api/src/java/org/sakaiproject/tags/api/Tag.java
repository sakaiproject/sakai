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


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A data object representing a tag.
 */
@Data
@AllArgsConstructor
public class Tag {

    private String tagId;
    private String tagCollectionId;
    private String tagLabel;
    private String description;
    private String createdBy;
    private long  creationDate;
    private String lastModifiedBy;
    private long  lastModificationDate;
    private String externalId;
    private String alternativeLabels;
    private Boolean externalCreation;
    private long  externalCreationDate;
    private Boolean externalUpdate;
    private long  lastUpdateDateInExternalSystem ;
    private String parentId;
    private String externalHierarchyCode;
    private String externalType;
    private String data;
    private String collectionName;



    /**
     * Check that the values we've been given make sense.
     */
    public Errors validate() {
        Errors errors = new Errors();
        //At this moment there is not extra validation. This can be the place to do this in the future
        return errors;
    }


}
    
