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

package org.sakaiproject.assignment.api.taggable;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.taggable.api.TaggableActivity;
import org.sakaiproject.taggable.api.TaggableActivityProducer;
import org.sakaiproject.taggable.api.TaggableItem;

/**
 * A producer of assignments as taggable activities.
 *
 * @author The Sakai Foundation.
 */
public interface AssignmentActivityProducer extends TaggableActivityProducer {

    /**
     * The type name of this producer.
     */
    public static final String PRODUCER_ID = AssignmentActivityProducer.class.getName();

    /**
     * Method to wrap the given assignment as a taggable activity.
     *
     * @param assignment The assignment.
     * @return The assignment represented as a taggable activity.
     */
    public TaggableActivity getActivity(Assignment assignment);

    /**
     * Method to wrap the given assignment submission as a taggable item.
     *
     * @param assignmentSubmission The assignment submission.
     * @param userId               The identifier of the user that this item belongs to.
     * @return The assignment submission represented as a taggable item.
     */
    public TaggableItem getItem(AssignmentSubmission assignmentSubmission, String userId);
}
