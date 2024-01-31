/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.velocity.runtime.resource.ContentResource;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.condition.api.ConditionService;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.webapi.beans.LessonItemRestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class LessonsController extends AbstractSakaiApiController {


    private static final String SITE_SEGMENT = "/site/";

    private static final SecurityAdvisor ADVISOR_ALLOW_LESSONBUILDER_UPDATE =
            (String userId, String function, String reference) ->
                    SimplePage.PERMISSION_LESSONBUILDER_UPDATE.equals(function)
                            ? SecurityAdvice.ALLOWED
                            : SecurityAdvice.PASS;

    @Autowired
    private ConditionService conditionService;

    @Autowired
    private ContentHostingService contentHostingService;


    @Autowired
    private GradingService gradingService;

    @Autowired
    @Qualifier("org.sakaiproject.lessonbuildertool.model.SimplePageToolDao")
    private SimplePageToolDao lessonService;

    @Autowired
    private SecurityService securityService;


    // lessonId = itemId of lesson's root page
    @DeleteMapping(value = "/sites/{siteId}/lessons/{lessonId}/items")
    public ResponseEntity<String> deleteLessonItems(@PathVariable String siteId, @PathVariable Long lessonId) {
        String userId = checkSakaiSession().getUserId();
        checkSite(siteId);

        SimplePage lesson = pageIdFromLessonId(lessonId)
                .flatMap(pageId -> lessonService.getSitePages(siteId).stream()
                        .filter(sitePage -> pageId.equals(sitePage.getPageId()))
                        .findAny())
                .orElse(null);

        if (lesson == null) {
            return ResponseEntity.badRequest().body("Lesson not found");
        }

        if (!canUpdateLessonItems(userId, lesson)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<SimplePageItem> pageItems = lessonService.findPageItemsByPageId(lesson.getPageId());

        int toDeleteCount = pageItems.size();

        if (toDeleteCount == 0) {
            return ResponseEntity.ok("Lesson is already empty");
        }

        int deletedCount = pageItems.stream()
                .map(item -> {
                    if (SimplePageItem.CHECKLIST == item.getType()) {
                        lessonService.deleteAllSavedStatusesForChecklist(item);
                    }

                    // Remove external assessment entries for id's that are set on the item
                    List.of(Optional.ofNullable(item.getGradebookId()), Optional.ofNullable(item.getAltGradebook())).stream()
                            .flatMap(Optional::stream)
                            .forEach(gradebookExternalId -> {
                                gradingService.removeExternalAssignment(null, gradebookExternalId, LessonBuilderConstants.TOOL_ID);
                            });

                    boolean deleted = false;
                    // Pushing this advisor because the internal permission check will create a bad context
                    securityService.pushAdvisor(ADVISOR_ALLOW_LESSONBUILDER_UPDATE);
                    try {
                        deleted = lessonService.deleteItem(item);
                    } finally {
                        securityService.popAdvisor(ADVISOR_ALLOW_LESSONBUILDER_UPDATE);
                    }
                    return deleted;
                })
                .mapToInt(itemDeleted -> itemDeleted ? 1 : 0)
                .sum();

        String message = deletedCount + " of " + toDeleteCount + " of items deleted";
        HttpStatus status = deletedCount == toDeleteCount ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(message);
    }

    @DeleteMapping("/sites/{siteId}/lessons/{lessonId}/conditions")
    public ResponseEntity<String> deleteLessonConditions(@PathVariable String siteId, @PathVariable Long lessonId) {
        String userId = checkSakaiSession().getUserId();
        Site site = checkSite(siteId);

        SimplePage lesson = pageIdFromLessonId(lessonId)
                .flatMap(pageId -> lessonService.getSitePages(siteId).stream()
                        .filter(sitePage -> pageId.equals(sitePage.getPageId()))
                        .findAny())
                .orElse(null);

        if (lesson == null) {
            return ResponseEntity.badRequest().body("Lesson not found");
        }

        if (!canUpdateConditions(userId, site.getReference())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<SimplePageItem> pageItems = lessonService.findPageItemsByPageId(lesson.getPageId());
        try {
            pageItems.stream()
                    .map(pageItem -> conditionService.getRootConditionForItem(siteId,
                            LessonBuilderConstants.TOOL_ID, Long.valueOf(pageItem.getId()).toString()))
                    .flatMap(Optional::stream)
                    .map(Condition::getId)
                    .forEach(conditionService::deleteCondition);
        } catch (Exception e) {
            log.error("Condition could not be deleted due to {} {}", e.toString(), ExceptionUtils.getStackTrace(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Conditions could not be deleted");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/sites/{siteId}/lessons/{lessonId}/items/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LessonItemRestBean>> createLessonItems(@PathVariable String siteId, @PathVariable Long lessonId,
            @RequestBody List<LessonItemRestBean> lessonItems) {
        String userId = checkSakaiSession().getUserId();
        checkSite(siteId);

        SimplePage lesson = pageIdFromLessonId(lessonId)
                .flatMap(pageId -> lessonService.getSitePages(siteId).stream()
                        .filter(sitePage -> pageId.equals(sitePage.getPageId()))
                        .findAny())
                .orElse(null);

        if (lesson == null) {
            log.debug("lesson == null");
            return ResponseEntity.badRequest().build();
        }

        if (!canUpdateLessonItems(userId, lesson)) {
            log.debug("no update permission for user {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<SimplePageItem> pageItems = lessonService.findPageItemsByPageId(lesson.getPageId());

        if (!pageItems.isEmpty()) {
            log.debug("page items not empty; pageItems: {}", pageItems.stream().map(SimplePageItem::getId).collect(Collectors.toSet()));
            return ResponseEntity.badRequest().build();
        }

        boolean allItemsCreateable = lessonItems.size() == lessonItems.stream()
                .filter(LessonItemRestBean::isCreatable)
                .collect(Collectors.counting()).intValue();

        if (!allItemsCreateable) {
            log.debug("no all items are creatable");
            return ResponseEntity.badRequest().build();
        }

        // Create Lesson items and collect results
        List<Boolean> createdStatuses = new ArrayList<>(); 

        // Get resource properties for resource items
        Map<String, ResourceProperties> resourcePropertiesMap = new HashMap<>();
        for(LessonItemRestBean lessonItem : lessonItems) {
            if (SimplePageItem.RESOURCE != lessonItem.getType()) {
                continue;
            }

            String resourceId = lessonItem.getContentRef();
            try {
                ResourceProperties resourceProperties = contentHostingService.getProperties(resourceId);
                resourcePropertiesMap.put(lessonItem.getContentRef(), resourceProperties);
            } catch (IdUnusedException e) {
                log.debug("can not find resource with id {}", resourceId);
                return ResponseEntity.badRequest().build();
            } catch (PermissionException e) {
                log.debug("no permission to get resource {}", resourceId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        int sequence = 1;
        for(LessonItemRestBean lessonItem : lessonItems) {
            switch(lessonItem.getType()) {
                case SimplePageItem.RESOURCE:
                    ResourceProperties resourceProperties = resourcePropertiesMap.get(lessonItem.getContentRef());
                    lessonItem.setHtml(resourceProperties.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
                    break;
                default:
                    break;
            }
            lessonItem.setSequence(sequence);
            boolean lessonItemCreated = createLessonItem(lessonItem);
            createdStatuses.add(lessonItemCreated);
            sequence++;
        }

        int requestedCount = lessonItems.size();
        int failedCount = createdStatuses.stream().filter(status -> !status).collect(Collectors.counting()).intValue();

        if (failedCount > 0) {
            log.error("Could not create all lesson items. Created {} of requested {}",
                    requestedCount - failedCount, requestedCount);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        List<SimplePageItem> updatedPageItems = lessonService.findPageItemsByPageId(lesson.getPageId());

        return ResponseEntity.ok(updatedPageItems.stream()
                .map(LessonItemRestBean::of)
                .collect(Collectors.toList()));
    }

    private boolean createLessonItem(LessonItemRestBean lessonItem) {
        SimplePageItem  itemToSave = lessonService.makeItem(lessonItem.getPageId(), lessonItem.getSequence(),
                lessonItem.getType(), lessonItem.getContentRef(), lessonItem.getTitle());

        Optional.ofNullable(lessonItem.getFormat()).ifPresent(format -> itemToSave.setFormat(format));
        Optional.ofNullable(lessonItem.getHtml()).ifPresent(html -> itemToSave.setHtml(html));

        return lessonService.quickSaveItem(itemToSave);
    }

    private boolean canUpdateConditions(String userId, String siteRef) {
        return securityService.unlock(userId, ConditionService.PERMISSION_UPDATE_CONDITION, siteRef);
    }

    private boolean canUpdateLessonItems(String userId, SimplePage page) {
        String siteRef = SITE_SEGMENT + page.getSiteId();

        return securityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_UPDATE, siteRef);
    }

    private Optional<Long> pageIdFromLessonId(Long lessonId) {
        if (lessonId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(lessonService.findItem(lessonId))
                .map(pageItem -> pageItem.getSakaiId())
                .map(pageId -> Long.valueOf(pageId));
    }
}
