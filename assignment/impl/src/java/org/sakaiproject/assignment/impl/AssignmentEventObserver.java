package org.sakaiproject.assignment.impl;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentEventObserver implements Observer {

    @Setter private AssignmentService assignmentService;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private GradebookService gradebookService;

    public void init() {
        eventTrackingService.addLocalObserver(this);
    }

    public void destroy() {
        eventTrackingService.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Event) {
            Event event = (Event) arg;
            if (event.getModify() && StringUtils.isNoneBlank(event.getEvent(), event.getContext())) {
                switch (event.getEvent()) {
                    case "gradebook.updateItemScore": // grade updated in gradebook lets attempt to update the submission
                        String[] parts = StringUtils.split(event.getResource(), '/');
                        if (parts.length >= 5) {
                            final String source = parts[0];
                            final String gradebookId = parts[1];
                            final String itemId = parts[2];
                            final String studentId = parts[3];
                            final String score = parts[4];
                            log.debug("Updating score for user {} for item {} with score {} in gradebook {} by {}", studentId, itemId, score, gradebookId, source);
                            if ("gradebookng".equals(source)) {
                                Optional<Assignment> assignment = Optional.empty();
                                Optional<AssignmentSubmission> submission = Optional.empty();
                                // Assignments stores the gradebook item name and not the id :(, so we need to look it up
                                try {
                                    org.sakaiproject.service.gradebook.shared.Assignment gradebookAssignment = gradebookService.getAssignmentByNameOrId(event.getContext(), itemId);
                                    assignment = Optional.ofNullable(assignmentService.getAssignmentForGradebookLink(event.getContext(), gradebookAssignment.getName()));
                                    if (assignment.isPresent()) {
                                        Assignment a = assignment.get();
                                        submission = Optional.ofNullable(assignmentService.getSubmission(a.getId(), studentId));
                                        if (submission.isPresent()) {
                                            AssignmentSubmission s = submission.get();
                                            final String grade;
                                            if (Assignment.GradeType.SCORE_GRADE_TYPE.equals(a.getTypeOfGrade())) {
                                                int dec = (int) Math.log10(a.getScaleFactor());
                                                StringBuilder scaledScore = new StringBuilder(score);
                                                IntStream.range(0, dec).forEach(i -> scaledScore.append("0"));
                                                grade = scaledScore.toString();
                                            } else {
                                                grade = score;
                                            }
                                            if (a.getIsGroup()) {
                                                // grades will show up as overrides for group assignments
                                                Set<AssignmentSubmissionSubmitter> submitters = s.getSubmitters();
                                                submitters.stream().filter(u -> studentId.equals(u.getSubmitter())).findAny().ifPresent(u -> u.setGrade(grade));
                                            } else {
                                                s.setGrade(grade);
                                            }
                                            s.setGraded(true);
                                            s.setGradedBy(event.getUserId());
                                            assignmentService.updateSubmission(s);
                                            log.debug("Updated score for user {} for submission {} with score {}", studentId, s.getId(), score);
                                        } else {
                                            log.debug("Submission not found for assignment {} and student {}, ", itemId, studentId);
                                        }
                                    } else {
                                        log.warn("No matching assignment found with gradebook item id, {}", itemId);
                                    }
                                } catch (IdUnusedException | PermissionException e) {
                                    if (!assignment.isPresent()) {
                                        log.warn("Can't retrieve assignment for gradebook item id {}, {}", itemId, e.getMessage());
                                    } else if (!submission.isPresent()) {
                                        log.warn("Can't retrieve submission for user {} for assignment {}, {}", studentId, assignment.get().getId(), e.getMessage());
                                    } else {
                                        // permission exception while updating submission
                                        log.warn("Can't update submission for user {}, {}", studentId, e.getMessage());
                                    }
                                } catch (AssessmentNotFoundException anfe) {
                                    log.warn("Can't retrieve gradebook assignment for gradebook {} and item {}", gradebookId, itemId);
                                }
                            } else {
                                log.warn("Score update not supported for source {}", source);
                            }
                        }
                        break;
                    default:
                        log.debug("This observer is not interested in event [{}]", event);
                        break;
                }
            }
        }
    }
}
