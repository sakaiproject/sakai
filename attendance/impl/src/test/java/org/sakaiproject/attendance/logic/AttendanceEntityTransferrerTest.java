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
package org.sakaiproject.attendance.logic;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.test.AttendanceTestConfiguration;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AttendanceTestConfiguration.class)
public class AttendanceEntityTransferrerTest {
    @Autowired private AttendanceLogic logic;
    @Autowired private AttendanceDao dao;
    @Autowired private EntityManager entityManager;
    @Autowired private SessionManager sessionManager;
    @Autowired private SecurityService securityService;
    @Autowired @Qualifier("org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private HibernateTransactionManager transactionManager;

    private EntityTransferrer transferrer;
    private TransactionTemplate transaction;

    @Before
    public void setUp() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor");
        when(securityService.isSuperUser()).thenReturn(true);
        ArgumentCaptor<EntityProducer> producer = ArgumentCaptor.forClass(EntityProducer.class);
        verify(entityManager).registerEntityProducer(producer.capture(), eq("/attendance"));
        transferrer = (EntityTransferrer) producer.getValue();
        transaction = new TransactionTemplate(transactionManager);
    }

    @After
    public void resetBoundaryServices() {
        reset(sessionManager, securityService);
    }

    @Test
    public void importsIntoSiteWithoutAttendance() {
        assertImportCommitted(false);
    }

    @Test
    public void importsIntoSiteWithAttendance() {
        assertImportCommitted(true);
    }

    private void assertImportCommitted(boolean existingDestination) {
        String source = UUID.randomUUID().toString();
        String target = UUID.randomUUID().toString();
        AttendanceEvent original = createSourceEvent(source);
        if (existingDestination) {
            logic.getAttendanceSite(target);
        }
        // SiteManageService invokes the registered producer inside this transaction type.
        Map<String, String> references = transaction.execute(status ->
                transferrer.transferCopyEntities(source, target, List.of(), List.of(), false));

        transaction.executeWithoutResult(status -> {
            AttendanceSite destination = dao.getAttendanceSite(target);
            assertNotNull(destination);
            assertEquals(6, dao.getAllStatusesForSite(destination).size());
            List<AttendanceEvent> copied = dao.getAttendanceEventsForSite(destination);
            assertEquals(1, copied.size());
            AttendanceEvent event = copied.get(0);
            assertNotEquals(original.getId(), event.getId());
            assertEquals(original.getName(), event.getName());
            assertEquals(original.getStartDateTime(), event.getStartDateTime());
            assertEquals(original.getEndDateTime(), event.getEndDateTime());
            assertEquals(original.getIsRequired(), event.getIsRequired());
            assertEquals(original.getIsReoccurring(), event.getIsReoccurring());
            assertEquals("instructor", event.getLastModifiedBy());
            assertEquals("attendance/" + event.getId(), references.get("attendance/" + original.getId()));
        });
    }

    @Test
    public void rollsBackAttendanceWhenImportTransactionFails() {
        String source = UUID.randomUUID().toString();
        String target = UUID.randomUUID().toString();
        createSourceEvent(source);
        RuntimeException failure = new RuntimeException("Import failed");
        assertSame(failure, assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(status -> {
            transferrer.transferCopyEntities(source, target, List.of(), List.of(), false);
            // Force SQL before rollback so this also covers flushed writes.
            assertEquals(1, dao.getAttendanceEventsForSite(dao.getAttendanceSite(target)).size());
            throw failure;
        })));
        transaction.executeWithoutResult(status -> {
            assertNull(dao.getAttendanceSite(target));
            assertEquals(1, dao.getAttendanceEventsForSite(dao.getAttendanceSite(source)).size());
        });
    }

    private AttendanceEvent createSourceEvent(String source) {
        AttendanceSite site = logic.getAttendanceSite(source);
        AttendanceEvent event = new AttendanceEvent();
        event.setAttendanceSite(site);
        event.setName("Class meeting");
        event.setStartDateTime(Instant.parse("2026-11-02T14:00:00Z"));
        event.setEndDateTime(Instant.parse("2026-11-02T15:00:00Z"));
        event.setIsRequired(true);
        event.setIsReoccurring(false);
        logic.addAttendanceEventNow(event);
        return event;
    }
}
