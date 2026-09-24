/*
 * Copyright (c) 2026 The Apereo Foundation
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItem;
import org.sakaiproject.assignment.api.model.AssignmentAllPurposeItemAccess;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemService;
import org.sakaiproject.assignment.api.model.AssignmentSupplementItemUpdate;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AssignmentTestConfiguration.class)
public class AssignmentSupplementItemServiceTest {

    @Autowired
    private AssignmentSupplementItemService service;

    @Autowired
    private AuthzGroupService authzGroupService;

    @Autowired
    private SiteService siteService;

    @Test
    public void accessLookupFailureSavesItemAndPreservesAccess() throws Exception {
        String assignmentId = UUID.randomUUID().toString();
        AssignmentAllPurposeItem item = service.newAllPurposeItem();
        item.setAssignmentId(assignmentId);
        item.setTitle("Before edit");
        item.setHide(false);
        item.setAttachmentSet(new HashSet<>());
        service.saveAllPurposeItem(item);
        service.saveAllPurposeItemWithAccess(service.getAllPurposeItem(assignmentId), Set.of("student"));
        Map<String, Long> originalAccess = accessIds(service.getAllPurposeItem(assignmentId));

        String siteId = UUID.randomUUID().toString();
        String siteReference = "/site/" + siteId;
        when(siteService.siteReference(siteId)).thenReturn(siteReference);
        when(authzGroupService.getAuthzGroup(siteReference)).thenThrow(new RuntimeException("lookup failed"));

        AssignmentSupplementItemUpdate update = new AssignmentSupplementItemUpdate(null, null,
                AssignmentSupplementItemUpdate.Change.save(new AssignmentSupplementItemUpdate.AllPurpose(
                        "After edit", "Text", false, null, null, Set.of(), Set.of("instructor"))));
        assertEquals(AssignmentSupplementItemService.SaveResult.ACCESS_LOOKUP_FAILED,
                service.saveSupplementItems(assignmentId, siteId, null, update));

        AssignmentAllPurposeItem saved = service.getAllPurposeItem(assignmentId);
        assertEquals("After edit", saved.getTitle());
        assertEquals(originalAccess, accessIds(saved));
    }

    @Test
    public void savesModelAnswerAndNoteThroughService() {
        String assignmentId = UUID.randomUUID().toString();
        AssignmentSupplementItemUpdate update = new AssignmentSupplementItemUpdate(
                AssignmentSupplementItemUpdate.Change.save(
                        new AssignmentSupplementItemUpdate.ModelAnswer("Answer", 2, Set.of())),
                AssignmentSupplementItemUpdate.Change.save(
                        new AssignmentSupplementItemUpdate.Note("Private note", 1)), null);

        assertEquals(AssignmentSupplementItemService.SaveResult.SAVED,
                service.saveSupplementItems(assignmentId, "site", "instructor", update));
        assertEquals("Answer", service.getModelAnswer(assignmentId).getText());
        assertEquals("Private note", service.getNoteItem(assignmentId).getNote());
        assertEquals("instructor", service.getNoteItem(assignmentId).getCreatorId());
        assertTrue(service.getModelAnswer(assignmentId).getAttachmentSet().isEmpty());
    }

    @Test
    public void editingModelAnswerKeepsSelectedAttachment() {
        String assignmentId = UUID.randomUUID().toString();
        String attachmentId = "/attachment/" + UUID.randomUUID();
        AssignmentSupplementItemUpdate first = new AssignmentSupplementItemUpdate(
                AssignmentSupplementItemUpdate.Change.save(
                        new AssignmentSupplementItemUpdate.ModelAnswer("Before", 1, Set.of(attachmentId))),
                null, null);
        service.saveSupplementItems(assignmentId, "site", null, first);

        AssignmentSupplementItemUpdate second = new AssignmentSupplementItemUpdate(
                AssignmentSupplementItemUpdate.Change.save(
                        new AssignmentSupplementItemUpdate.ModelAnswer("After", 2, Set.of(attachmentId))),
                null, null);
        service.saveSupplementItems(assignmentId, "site", null, second);

        assertEquals(Set.of(attachmentId),
                Set.copyOf(service.getAttachmentListForSupplementItem(service.getModelAnswer(assignmentId))));
    }

    @Test
    public void removesSupplementItemsWithTheirChildren() {
        String assignmentId = UUID.randomUUID().toString();
        service.saveSupplementItems(assignmentId, "site", null, new AssignmentSupplementItemUpdate(
                AssignmentSupplementItemUpdate.Change.save(
                        new AssignmentSupplementItemUpdate.ModelAnswer("Answer", 1, Set.of("/attachment/model"))),
                null,
                AssignmentSupplementItemUpdate.Change.save(
                        new AssignmentSupplementItemUpdate.AllPurpose("Resource", "Text", false,
                                null, null, Set.of("/attachment/resource"), null))));
        service.saveAllPurposeItemWithAccess(service.getAllPurposeItem(assignmentId), Set.of("student"));

        service.saveSupplementItems(assignmentId, "site", null, new AssignmentSupplementItemUpdate(
                AssignmentSupplementItemUpdate.Change.remove(), null,
                AssignmentSupplementItemUpdate.Change.remove()));

        assertNull(service.getModelAnswer(assignmentId));
        assertNull(service.getAllPurposeItem(assignmentId));
    }

    @Test
    public void editingAllPurposeItemRetainsExistingAccessRows() {
        String assignmentId = UUID.randomUUID().toString();
        AssignmentAllPurposeItem item = service.newAllPurposeItem();
        item.setAssignmentId(assignmentId);
        item.setTitle("Before edit");
        item.setHide(false);
        item.setAttachmentSet(new HashSet<>());
        service.saveAllPurposeItem(item);

        item = service.getAllPurposeItem(assignmentId);
        service.saveAllPurposeItemWithAccess(item, Set.of("admin", "student"));
        AssignmentAllPurposeItem saved = service.getAllPurposeItem(assignmentId);
        Map<String, Long> originalIds = accessIds(saved);
        assertEquals(Set.of("admin", "student"), originalIds.keySet());

        saved.setTitle("After edit");
        saved.setAttachmentSet(new HashSet<>());
        service.saveAllPurposeItemWithAccess(saved, Set.of("admin", "instructor"));
        AssignmentAllPurposeItem edited = service.getAllPurposeItem(assignmentId);
        Map<String, Long> editedIds = accessIds(edited);

        assertEquals("After edit", edited.getTitle());
        assertEquals(Set.of("admin", "instructor"), editedIds.keySet());
        assertEquals(originalIds.get("admin"), editedIds.get("admin"));
        assertNotNull(editedIds.get("instructor"));

        service.saveAllPurposeItemWithAccess(edited, Set.of("admin", "instructor"));
        assertEquals(editedIds, accessIds(service.getAllPurposeItem(assignmentId)));
    }

    private Map<String, Long> accessIds(AssignmentAllPurposeItem item) {
        return item.getAccessSet().stream().collect(Collectors.toMap(
                AssignmentAllPurposeItemAccess::getAccess, AssignmentAllPurposeItemAccess::getId));
    }
}
