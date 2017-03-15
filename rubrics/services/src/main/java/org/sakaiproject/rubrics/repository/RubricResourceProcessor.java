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