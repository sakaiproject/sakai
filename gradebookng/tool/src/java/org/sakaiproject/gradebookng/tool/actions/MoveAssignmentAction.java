package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

abstract public class MoveAssignmentAction extends ActionImpl implements Serializable {

    private static final long serialVersionUID = 1L;

    @SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    protected GradebookNgBusinessService businessService;

    public MoveAssignmentAction() {
    }
    /**
     * Get the assignment's current sort index within its category. If this value is null in the database, best calculate this index from
     * the assignments.
     *
     * @param assignmentId the id of the assignment
     * @return the current sort index of the assignment within their category
     */
    protected Integer calculateCurrentCategorizedSortOrder(final Long assignmentId) {
        final Assignment assignment = MoveAssignmentAction.this.businessService.getAssignment(assignmentId.longValue());
        Integer order = assignment.getCategorizedSortOrder();

        if (order == null) {
            // if no categorized order for assignment, calculate one based on the default sort order
            final List<Assignment> assignments = MoveAssignmentAction.this.businessService.getGradebookAssignments();
            final List<Long> assignmentIdsInCategory = assignments.stream()
                .filter(a -> (a.getCategoryId() != null) && a.getCategoryId().equals(assignment.getCategoryId()))
                .map(Assignment::getId)
                .collect(Collectors.toList());

            order = assignmentIdsInCategory.indexOf(assignmentId);
        }

        return order;
    }

}
