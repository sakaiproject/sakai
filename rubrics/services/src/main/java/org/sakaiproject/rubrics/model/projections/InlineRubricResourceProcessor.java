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
package org.sakaiproject.rubrics.model.projections;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

/**
 * This redundancy with {@link org.sakaiproject.rubrics.repository.RubricResourceProcessor} is an unfortunate necessity
 * for what is considered an edge case by the Spring Data REST team to avoid other complications if they allowed the
 * primary entity ResourceProcessor to be used to serialize a projection.
 * See https://jira.spring.io/browse/DATAREST-713
 */
public class InlineRubricResourceProcessor implements ResourceProcessor<Resource<InlineRubric>> {

    @Override
    public Resource<InlineRubric> process(Resource<InlineRubric> rubricResource) {
        InlineRubric rubric = rubricResource.getContent();
        if (rubric.getToolItemAssociations() != null && rubric.getToolItemAssociations().size() > 0) {
            rubric.getMetadata().setLocked(true);
        }
        return rubricResource;
    }
}