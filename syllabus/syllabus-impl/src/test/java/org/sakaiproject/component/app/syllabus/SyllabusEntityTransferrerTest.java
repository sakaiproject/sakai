/*
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
package org.sakaiproject.component.app.syllabus;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SyllabusServiceTestConfiguration.class)
public class SyllabusEntityTransferrerTest {

    @Autowired private SyllabusManager syllabusManager;
    @Autowired private EntityManager entityManager;
    @Autowired private LTIService ltiService;
    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;

    @Test
    public void registeredCopyCallbackUsesServiceTransactionAndPreservesSource() throws Exception {
        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        Site destination = mock(Site.class);
        when(siteService.getSite(toContext)).thenReturn(destination);
        when(destination.getTitle()).thenReturn("Destination");
        User user = mock(User.class);
        when(user.getId()).thenReturn("instructor");
        when(userDirectoryService.getCurrentUser()).thenReturn(user);
        SyllabusItem sourceItem = syllabusManager.createSyllabusItem("instructor", fromContext, null);
        SyllabusData source = syllabusManager.createSyllabusDataObject("Syllabus to copy", 0,
            "Original content", "no", SyllabusData.ITEM_DRAFT, "none", null, null, false,
            null, null, sourceItem);
        syllabusManager.addSyllabusToSyllabusItem(sourceItem, source, false);
        List<Boolean> transactions = new ArrayList<>();
        when(ltiService.fixLtiLaunchUrls(eq("Original content"), eq(fromContext), eq(toContext), any(Map.class)))
            .thenAnswer(invocation -> {
                transactions.add(TransactionSynchronizationManager.isActualTransactionActive());
                return "Destination content";
            });
        ArgumentCaptor<EntityProducer> producer = ArgumentCaptor.forClass(EntityProducer.class);
        verify(entityManager).registerEntityProducer(producer.capture(), eq(SyllabusService.REFERENCE_ROOT));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        ((EntityTransferrer) producer.getValue()).transferCopyEntities(fromContext, toContext,
            List.of(source.getSyllabusId().toString()), List.of());

        SyllabusItem destinationItem = syllabusManager.getSyllabusItemByContextId(toContext);
        assertNotNull(destinationItem);
        Set<SyllabusData> copies = syllabusManager.getSyllabiForSyllabusItem(destinationItem);
        assertEquals(1, copies.size());
        assertEquals("Destination content", copies.iterator().next().getAsset());
        assertEquals("Original content", syllabusManager.getSyllabusData(source.getSyllabusId().toString()).getAsset());
        assertEquals("The registered copy callback must retain the service transaction between manager calls",
            List.of(true), transactions);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }
}
