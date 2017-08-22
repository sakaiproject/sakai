/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.rubrics.repository;

import org.sakaiproject.rubrics.model.Rubric;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

/**
 * <em>WARNING: Must keep {@link org.sakaiproject.rubrics.model.projections.InlineRubricResourceProcessor} in sync with
 * any changes to this class.
 */
public class RubricResourceProcessor implements ResourceProcessor<Resource<Rubric>> {

    @Override
    public Resource<Rubric> process(Resource<Rubric> rubricResource) {
        Rubric rubric = rubricResource.getContent();
        if (rubric.getToolItemAssociations() != null && rubric.getToolItemAssociations().size() > 0) {
            rubric.getMetadata().setLocked(true);
        }
        return rubricResource;
    }
}