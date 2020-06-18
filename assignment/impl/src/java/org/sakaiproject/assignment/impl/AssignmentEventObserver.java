/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentEventObserver implements Observer {

    @Setter private AssignmentService assignmentService;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private GradebookService gradebookService;
    @Setter private UserDirectoryService userDirectoryService;

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
                                        final Assignment a = assignment.get();
                                        final User user = userDirectoryService.getUser(studentId);
                                        submission = Optional.ofNullable(assignmentService.getSubmission(a.getId(), user.getId()));
                                        submission = Optional.ofNullable(submission.orElseGet(() -> {
                                            try {
                                                return assignmentService.addSubmission(a.getId(), assignmentService.getSubmitterIdForAssignment(a, user));
                                            } catch (PermissionException e) {
                                                log.warn("Can't access submission for assignment {} and user {}, {}", a.getId(), user.getId(), e.getMessage());
                                            }
                                            return null;
                                        }));

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
                                            log.warn("Submission not found for assignment {} and student {}, ", itemId, studentId);
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
                                        log.warn("Can't update submission for user {}, {}", studentId, e.getMessage());
                                    }
                                } catch (AssessmentNotFoundException anfe) {
                                    log.warn("Can't retrieve gradebook assignment for gradebook {} and item {}, {}", gradebookId, itemId, anfe.getMessage());
                                } catch (UserNotDefinedException e) {
                                    log.warn("Can't retrieve user {}, {}", studentId, e.getMessage());
                                }
                            } else {
                                log.debug("Score update not supported for event with source of {}", source);
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
