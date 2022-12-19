/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.rubrics.api.beans;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sakaiproject.rubrics.api.model.Rubric;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RubricTransferBean {

    private Long id;
    private Instant created;
    private String creatorDisplayName;
    private String creatorId;
    private List<CriterionTransferBean> criteria = new ArrayList<>();
    private String formattedCreatedDate;
    private String formattedModifiedDate;
    private Boolean locked;
    private Instant modified;
    private String ownerId;
    private Boolean shared;
    private String siteTitle;
    private String title;
    private Boolean weighted;

    public RubricTransferBean(Rubric rubric) {
        id = rubric.getId();
        created = rubric.getCreated();
        creatorId = rubric.getCreatorId();
        criteria = rubric.getCriteria().stream().map(CriterionTransferBean::new).collect(Collectors.toList());
        locked = rubric.getLocked();
        modified = rubric.getModified();
        ownerId = rubric.getOwnerId();
        shared = rubric.getShared();
        title = rubric.getTitle();
        weighted = rubric.getWeighted();
    }
}
