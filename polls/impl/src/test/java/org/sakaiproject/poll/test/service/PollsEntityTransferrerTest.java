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
package org.sakaiproject.poll.test.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.poll.api.PollConstants;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.repository.PollRepository;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PollsServiceTestConfiguration.class)
public class PollsEntityTransferrerTest {

    @Autowired private PollsService pollsService;
    @Autowired private PollRepository pollRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private LTIService ltiService;
    @Autowired private SiteService siteService;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;

    @Test
    public void registeredCopyCallbackUsesServiceTransaction() {
        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor");
        when(siteService.siteReference(toContext)).thenReturn("/site/" + toContext);
        when(securityService.unlock("instructor", PollConstants.PERMISSION_ADD, "/site/" + toContext)).thenReturn(true);
        Poll source = new Poll();
        source.setSiteId(fromContext);
        source.setOwner("instructor");
        source.setText("Poll to copy");
        source.setDescription("Content to copy");
        source.setCreationDate(Instant.now());
        source.setVoteOpen(Instant.now());
        source.setVoteClose(Instant.now().plusSeconds(3600));
        source = pollRepository.save(source);
        List<Boolean> transactions = new ArrayList<>();
        when(ltiService.fixLtiLaunchUrls(anyString(), eq(fromContext), eq(toContext), any(Map.class)))
            .thenAnswer(invocation -> {
                transactions.add(TransactionSynchronizationManager.isActualTransactionActive());
                return invocation.getArgument(0);
            });
        ArgumentCaptor<EntityProducer> producer = ArgumentCaptor.forClass(EntityProducer.class);
        verify(entityManager).registerEntityProducer(producer.capture(), eq(PollConstants.REFERENCE_ROOT));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        ((EntityTransferrer) producer.getValue()).transferCopyEntities(fromContext, toContext,
            List.of(source.getId().toString()), List.of());

        List<Poll> copies = pollsService.findAllPolls(toContext);
        assertEquals(1, copies.size());
        assertEquals("Content to copy", copies.get(0).getDescription());
        assertEquals("The registered copy callback must retain the service transaction between repository calls",
            List.of(true), transactions);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }
}
