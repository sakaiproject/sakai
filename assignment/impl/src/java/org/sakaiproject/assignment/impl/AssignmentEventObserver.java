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
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tasks.api.UserTaskAdapterBean;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentEventObserver implements Observer {

    @Setter private AssignmentService assignmentService;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private GradingService gradingService;
    @Setter private TaskService taskService;
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
                            final String gradebookUid = parts[1];
                            final String itemId = parts[2];
                            final String studentId = parts[3];
                            final String score = parts[4];
                            log.debug("Updating score for user {} for item {} with score {} in gradebook {} by {}", studentId, itemId, score, gradebookUid, source);

                            if ("gradebookng".equals(source)) {
                                List<Assignment> assignments = null;
                                Optional<AssignmentSubmission> submission = Optional.empty();
                                // Assignments stores the gradebook item name and not the id :(, so we need to look it up
                                try {
                                    org.sakaiproject.grading.api.Assignment gradebookAssignment = gradingService.getAssignmentByNameOrId(gradebookUid, event.getContext(), itemId);
                                    assignments = assignmentService.getAssignmentsForGradebookLink(event.getContext(), gradebookAssignment.getName());
                                    if (assignments != null) {
                                        for (Assignment a : assignments) {
                                            final User user = userDirectoryService.getUser(studentId);
                                            submission = Optional.ofNullable(assignmentService.getSubmission(a.getId(), user.getId()));
                                            submission = Optional.ofNullable(submission.orElseGet(() -> {
                                                try {
                                                    return assignmentService.addSubmission(a.getId(), assignmentService.getSubmitterIdForAssignment(a, user.getId()));
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
                                        }
                                    } else {
                                        log.debug("No matching assignment found with gradebook item id, {}", itemId);
                                    }
                                } catch (IdUnusedException | PermissionException e) {
                                    if (!submission.isPresent()) {
                                        log.warn("Can't retrieve submission for user {} for assignment {}, {}", studentId, itemId, e.getMessage());
                                    } else {
                                        log.warn("Can't update submission for user {}, {}", studentId, e.getMessage());
                                    }
                                } catch (AssessmentNotFoundException anfe) {
                                    log.debug("Can't retrieve gradebook assignment for gradebook {} and item {}, {}", gradebookUid, itemId, anfe.getMessage());
                                } catch (UserNotDefinedException e) {
                                    log.warn("Can't retrieve user {}, {}", studentId, e.getMessage());
                                }
                            } else {
                                log.debug("Score update not supported for event with source of {}", source);
                            }
                        }
                        break;
                    case SiteService.EVENT_USER_SITE_MEMBERSHIP_ADD:

                        // A user has been added to a site. Lets make sure any current assignments
                        // for that site have a corresponding task for the new user.
                        String userId = userDirectoryService.idFromReference(event.getResource());
                        String siteId = event.getContext();
                        assignmentService.getAssignmentsForContext(siteId).forEach(ass -> {


                            String ref = AssignmentReferenceReckoner.reckoner().assignment(ass).reckon().getReference();
                            taskService.getTask(ref).ifPresent(task -> {

                                UserTaskAdapterBean userTaskBean = new UserTaskAdapterBean();
                                userTaskBean.setTaskId(task.getId());
                                userTaskBean.setUserId(userId);
                                userTaskBean.setPriority(Priorities.HIGH);

                                taskService.createUserTask(task, userTaskBean);
                            });
                        });
                    default:
                        log.debug("This observer is not interested in event [{}]", event);
                        break;
                }
            }
        }
    }
}
