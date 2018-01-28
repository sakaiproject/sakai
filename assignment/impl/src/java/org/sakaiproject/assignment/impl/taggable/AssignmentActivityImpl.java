/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.impl.taggable;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.taggable.api.TaggableActivity;
import org.sakaiproject.taggable.api.TaggableActivityProducer;

public class AssignmentActivityImpl implements TaggableActivity {

    private Assignment assignment;
    private Entity entity;
    private AssignmentActivityProducerImpl producer;

    public AssignmentActivityImpl(Assignment assignment, Entity entity, AssignmentActivityProducerImpl producer) {
        this.assignment = assignment;
        this.entity = entity;
        this.producer = producer;
    }

    public boolean equals(Object object) {
        if (object instanceof TaggableActivity) {
            TaggableActivity activity = (TaggableActivity) object;
            return activity.getReference().equals(this.getReference());
        }
        return false;
    }

    public String getContext() {
        return assignment.getContext();
    }

    public String getDescription() {
        return assignment.getInstructions();
    }

    public Object getObject() {
        return assignment;
    }

    public TaggableActivityProducer getProducer() {
        return producer;
    }

    public String getReference() {
        return entity.getReference();
    }

    public String getTitle() {
        return assignment.getTitle();
    }

    public String getActivityDetailUrl() {
        //String url = assignment.getUrl();
        String url = producer.serverConfigurationService.getServerUrl() +
                "/direct/assignment/" + assignment.getId() + "/doView_assignment";
        return url;
    }

    public String getTypeName() {
        return producer.getName();
    }

    public boolean getUseDecoration() {
        return true;
    }

    public String getActivityDetailUrlParams() {
        return "?TB_iframe=true";
    }

}
