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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AssignmentTestConfiguration.class)
public class AssignmentSupplementItemServiceTest {

    @Autowired
    private AssignmentSupplementItemService service;

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
