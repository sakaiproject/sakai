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