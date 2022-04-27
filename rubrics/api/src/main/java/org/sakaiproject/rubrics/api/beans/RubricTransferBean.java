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

import org.sakaiproject.rubrics.api.model.Rubric;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RubricTransferBean {

    public Long id = null;
    public String title;
    public Boolean weighted;
    public List<CriterionTransferBean> criteria = new ArrayList<>();
    public Instant created;
    public String formattedCreatedDate;
    public Instant modified;
    public String formattedModifiedDate;
    public String ownerId;
    public String siteTitle;
    public String creatorId;
    public String creatorDisplayName;
    public Boolean shared;
    public Boolean locked;

    public static RubricTransferBean of(Rubric rubric) {

        RubricTransferBean bean = new RubricTransferBean();
        bean.id = rubric.getId();
        bean.title = rubric.getTitle();
        bean.weighted = rubric.getWeighted();
        bean.criteria = rubric.getCriteria().stream().map(CriterionTransferBean::of).collect(Collectors.toList());
        bean.created = rubric.getCreated();
        bean.modified = rubric.getModified();
        bean.ownerId = rubric.getOwnerId();
        bean.creatorId = rubric.getCreatorId();
        bean.shared = rubric.getShared();
        bean.locked = rubric.getLocked();
        return bean;
    }

    public Rubric toRubric() {

        Rubric rubric = new Rubric();
        rubric.setId(id);
        rubric.setTitle(title);
        rubric.setWeighted(weighted);
        rubric.setCriteria(criteria.stream().map(c -> c.toCriterion()).collect(Collectors.toList()));
        rubric.setCreated(created);
        rubric.setModified(modified);
        rubric.setOwnerId(ownerId);
        rubric.setCreatorId(creatorId);
        rubric.setShared(shared);
        return rubric;
    }
}
