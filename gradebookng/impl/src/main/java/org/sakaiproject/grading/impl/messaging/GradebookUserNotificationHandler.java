/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.grading.impl.messaging;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.GradingPersistenceManager;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.model.GradebookAssignment;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Notifies students when a score is posted for a released gradebook item. This covers
 * manually created items as well as external items (LTI, SCORM, Tests &amp; Quizzes, ...)
 * whose scores are pushed into the gradebook. Items maintained by Assignments are skipped
 * because Assignments already notifies when a submission grade is released.
 */
@Slf4j
public class GradebookUserNotificationHandler extends AbstractUserNotificationHandler {

    public static final String EVENT_UPDATE_ITEM_SCORE = "gradebook.updateItemScore";
    public static final String GRADEBOOK_TOOL_ID = "sakai.gradebookng";
    // Assignments sends its own notification on grade release (asn.grade.submission)
    public static final String ASSIGNMENTS_APP_NAME = "sakai.assignment.grades";

    // Score events from the grading service: /gradebook/{gradebookUid}/{itemName}/{studentId}/{points}/student
    private static final String SERVICE_REF_PREFIX = "/gradebook/";

    @Autowired private GradingPersistenceManager gradingPersistenceManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SiteService siteService;
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    @Autowired private SessionFactory sessionFactory;
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    @Autowired private PlatformTransactionManager transactionManager;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(EVENT_UPDATE_ITEM_SCORE);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        String ref = e.getResource();

        // The gradebook tool also posts gradebook.updateItemScore with a /gradebookng/ reference for
        // the same save, so only the grading service events are handled to avoid duplicates.
        if (e.getLrsStatement() != null || ref == null || !ref.startsWith(SERVICE_REF_PREFIX)) {
            return Optional.empty();
        }

        // ["", "gradebook", uid, itemName..., studentId, points, "student"]. The item name may contain slashes.
        String[] parts = ref.split("/", -1);
        if (parts.length < 7) {
            log.debug("Unexpected gradebook score event reference: {}", ref);
            return Optional.empty();
        }

        String gradebookUid = parts[2];
        String itemName = String.join("/", Arrays.copyOfRange(parts, 3, parts.length - 3));
        String studentId = parts[parts.length - 3];
        String points = parts[parts.length - 2];
        String from = e.getUserId();

        // A cleared score is not a new grade. Scores posted in the student's own session (SCORM, auto-scored
        // quizzes) still notify: the student did not enter the grade, the tool did.
        if (StringUtils.isAnyBlank(gradebookUid, itemName, studentId) || StringUtils.equalsAny(points, "", "null")) {
            return Optional.empty();
        }

        try {
            Optional<Gradebook> optGradebook = gradingPersistenceManager.getGradebook(gradebookUid);
            if (optGradebook.isEmpty() || !BooleanUtils.isTrue(optGradebook.get().getAssignmentsDisplayed())) {
                return Optional.empty();
            }

            Optional<GradebookAssignment> optItem = gradingPersistenceManager.getAssignmentByNameAndGradebook(itemName, gradebookUid);
            if (optItem.isEmpty()) {
                return Optional.empty();
            }

            GradebookAssignment item = optItem.get();
            if (!BooleanUtils.isTrue(item.getReleased())
                    || (BooleanUtils.isTrue(item.getExternallyMaintained()) && ASSIGNMENTS_APP_NAME.equals(item.getExternalAppName()))) {
                return Optional.empty();
            }

            Site site = getSite(gradebookUid);
            if (site == null) {
                return Optional.empty();
            }

            ToolConfiguration tool = site.getToolForCommonId(GRADEBOOK_TOOL_ID);
            if (tool == null) {
                return Optional.empty();
            }

            if (hasUnviewedNotification(studentId, site.getId(), SERVICE_REF_PREFIX + gradebookUid + "/" + itemName + "/" + studentId + "/")) {
                return Optional.empty();
            }

            String url = serverConfigurationService.getPortalUrl() + "/site/" + site.getId() + "/tool/" + tool.getId();
            return Optional.of(Collections.singletonList(
                    new UserNotificationData(from, studentId, site.getId(), item.getName(), url, GRADEBOOK_TOOL_ID, false, null)));
        } catch (Exception ex) {
            log.error("Failed to create a grade notification for event reference {}", ref, ex);
        }

        return Optional.empty();
    }

    /**
     * The gradebook uid is the site id, or a group id when the site uses a gradebook per group.
     */
    private Site getSite(String gradebookUid) {

        try {
            return siteService.getSite(gradebookUid);
        } catch (IdUnusedException iue) {
            Group group = siteService.findGroup(gradebookUid);
            if (group != null) {
                return group.getContainingSite();
            }
            log.debug("No site or group for gradebook uid {}", gradebookUid);
            return null;
        }
    }

    /**
     * Avoid piling up notifications when an item is re-scored before the student has seen the
     * previous notification for it. Once viewed or cleared, a new score notifies again.
     */
    boolean hasUnviewedNotification(String studentId, String siteId, String itemRefPrefix) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        List<String> refs = transactionTemplate.execute(status -> {
            CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<UserNotification> root = cq.from(UserNotification.class);
            cq.select(root.get("ref"))
                .where(
                    cb.equal(root.get("event"), EVENT_UPDATE_ITEM_SCORE),
                    cb.equal(root.get("toUser"), studentId),
                    cb.equal(root.get("siteId"), siteId),
                    cb.isFalse(root.get("viewed"))
                );
            return sessionFactory.getCurrentSession().createQuery(cq).getResultList();
        });

        return refs != null && refs.stream().anyMatch(r -> r != null && r.startsWith(itemRefPrefix));
    }
}
