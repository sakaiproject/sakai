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
package org.sakaiproject.assignment.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.messaging.api.model.UserNotification;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GradeAssignmentUserNotificationHandler extends AbstractUserNotificationHandler {

    @Resource
    private AssignmentService assignmentService;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        // Sometimes events are literally fired for LRS purposes. We don't want alerts for those.
        if (e.getLrsStatement() != null) {
            return Optional.empty();
        }

        String from = e.getUserId();

        String ref = e.getResource();
        String[] pathParts = ref.split("/");

        String siteId = pathParts[3];
        String submissionId = pathParts[pathParts.length - 1];
        try {
            AssignmentSubmission submission = assignmentService.getSubmission(submissionId);
            if (submission.getGradeReleased()) {
                Assignment assignment = submission.getAssignment();
                String title = assignment.getTitle();
                List<UserNotificationData> bhEvents = new ArrayList<>();
                submission.getSubmitters().forEach(to -> {

                    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                    long currentCount = transactionTemplate.execute(status -> {

                            return (Long) sessionFactory.getCurrentSession().createCriteria(UserNotification.class)
                                .add(Restrictions.eq("event", AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION))
                                .add(Restrictions.eq("ref", ref))
                                .add(Restrictions.eq("toUser", to.getSubmitter())).setProjection(Projections.rowCount()).uniqueResult();
                        });

                    if (currentCount == 0) {
                        try {
                            String url = assignmentService.getDeepLink(siteId, assignment.getId(), to.getSubmitter());
                            if (StringUtils.isNotBlank(url)) { 
                                bhEvents.add(new UserNotificationData(from, to.getSubmitter(), siteId, title, url, AssignmentConstants.TOOL_ID, false, null));
                            }
                        } catch(Exception exc) {
                            log.error("Error retrieving deep link for assignment {} and user {} on site {}", assignment.getId(), to.getSubmitter(), siteId, exc);
                        }
                    }
                });

                return Optional.of(bhEvents);
            }
        } catch (Exception ex) {
            log.error("Failed to find either the submission or the site", ex);
        }

        return Optional.empty();
    }
}
