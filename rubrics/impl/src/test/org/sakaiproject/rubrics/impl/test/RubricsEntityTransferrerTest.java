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
package org.sakaiproject.rubrics.impl.test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.Criterion;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RubricsTestConfiguration.class)
public class RubricsEntityTransferrerTest {

    @Autowired private RubricRepository rubricRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private FormattedText formattedText;

    @Test
    public void registeredCopyCallbackUsesServiceTransaction() {
        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        Rubric source = new Rubric();
        source.setOwnerId(fromContext);
        source.setCreatorId("instructor");
        source.setTitle("Rubric to copy");
        Criterion criterion = new Criterion();
        criterion.setRubric(source);
        criterion.setTitle("Criterion to copy");
        criterion.setDescription("Content to copy");
        source.getCriteria().add(criterion);
        source = rubricRepository.save(source);
        List<Boolean> transactions = new ArrayList<>();
        when(formattedText.processFormattedText(eq("Content to copy"), isNull(), eq(FormattedText.Level.HIGH)))
            .thenAnswer(invocation -> {
                transactions.add(TransactionSynchronizationManager.isActualTransactionActive());
                return invocation.getArgument(0);
            });
        ArgumentCaptor<EntityProducer> producer = ArgumentCaptor.forClass(EntityProducer.class);
        verify(entityManager).registerEntityProducer(producer.capture(), eq("/rubrics"));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        Map<String, String> copied = ((EntityTransferrer) producer.getValue()).transferCopyEntities(
            fromContext, toContext, List.of(source.getId().toString()), List.of());

        assertEquals(1, copied.size());
        List<Rubric> copies = rubricRepository.findByOwnerId(toContext);
        assertEquals(1, copies.size());
        assertEquals("Criterion to copy", copies.get(0).getCriteria().get(0).getTitle());
        assertEquals("The registered copy callback must retain the service transaction between repository calls",
            List.of(true), transactions);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }
}
