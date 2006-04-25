package org.sakaiproject.service.gradebook.shared;
/**
 * indicates that there was an attempt to enter an assignments grade with a zero
 * point grade allowed but with a grade greater than "0" assigned to it which could results
 * in a divide by zero calculation.
 *
 * @author <a href="mailto:louis@media.berkeley.edu">Louis Majanja</a>
 */
public class AssignmentHasIllegalPointsException extends GradebookException {
    public AssignmentHasIllegalPointsException(String message) {
        super(message);
    }
}
