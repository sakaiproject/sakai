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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.mockito.ArgumentMatchers.anyString;

import java.time.Instant;
import java.util.UUID;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.sakaiproject.attendance.model.*;
import org.sakaiproject.attendance.test.AttendanceTestConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AttendanceTestConfiguration.class)
@Transactional(transactionManager = "org.sakaiproject.springframework.orm.jpa.GlobalJpaTransactionManager")
public class AttendanceLogicTest {
    @Autowired private AttendanceLogic logic;
    @Autowired private SakaiProxy sakaiProxy;
    @Autowired private org.sakaiproject.attendance.api.AttendanceGradebookProvider gradebook;

    @Autowired private SessionManager sessionManager;
    @Autowired private org.sakaiproject.event.api.EventTrackingService eventTrackingService;
    @Autowired private org.sakaiproject.authz.api.SecurityService securityService;
    @Autowired private org.sakaiproject.site.api.SiteService siteService;
    @Autowired private org.sakaiproject.tool.api.ToolManager toolManager;
    @Autowired @org.springframework.beans.factory.annotation.Qualifier("attendanceEntityManager")
    private jakarta.persistence.EntityManager persistence;

    @Before
    public void authenticateInstructor() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor");
        when(securityService.isSuperUser()).thenReturn(true);
    }

    @After
    public void resetBoundaryServices() {
        reset(sessionManager, securityService, siteService, toolManager, eventTrackingService);
    }


    @Test
    public void updateAttendanceEventAuditsNewRecordsBeforeSaving() {
        when(sessionManager.getCurrentSessionUserId()).thenReturn("instructor");
        AttendanceSite site = logic.getAttendanceSite(UUID.randomUUID().toString());
        assertEquals(6, logic.getAllStatusesForSite(site).size());
        AttendanceEvent event = new AttendanceEvent();
        event.setName("Meeting");
        event.setAttendanceSite(site);
        AttendanceRecord newRecord = new AttendanceRecord(event, "student", Status.PRESENT);
        event.getRecords().add(newRecord);
        assertTrue(logic.updateAttendanceEvent(event));
        assertNotNull(event.getId());
        assertEquals("instructor", newRecord.getLastModifiedBy());
        assertNotNull(newRecord.getLastModifiedDate());
        Instant previousModification = newRecord.getLastModifiedDate();
        when(sessionManager.getCurrentSessionUserId()).thenReturn("another-instructor");
        event.setName("Updated meeting");
        assertTrue(logic.updateAttendanceEvent(event));
        assertEquals("instructor", newRecord.getLastModifiedBy());
        assertEquals(previousModification, newRecord.getLastModifiedDate());
        assertEquals("another-instructor", event.getLastModifiedBy());
        assertEquals("Updated meeting", logic.getAttendanceEvent(event.getId()).getName());
    }

    @Test
    public void getStatsForStatusReturnsCorrectField() {
        AttendanceStats stats = new AttendanceStats();
        stats.setPresent(7);
        stats.setUnexcused(3);
        stats.setExcused(2);
        stats.setLate(5);
        stats.setLeftEarly(1);
        assertEquals(7, logic.getStatsForStatus(stats, Status.PRESENT));
        assertEquals(3, logic.getStatsForStatus(stats, Status.UNEXCUSED_ABSENCE));
        assertEquals(2, logic.getStatsForStatus(stats, Status.EXCUSED_ABSENCE));
        assertEquals(5, logic.getStatsForStatus(stats, Status.LATE));
        assertEquals(1, logic.getStatsForStatus(stats, Status.LEFT_EARLY));
    }
    @Test
    public void manualGradesSurviveAttendanceChangesWhenAutoGradingIsDisabled() {
        String siteId = UUID.randomUUID().toString();
        org.sakaiproject.tool.api.Placement placement = org.mockito.Mockito.mock(org.sakaiproject.tool.api.Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn(siteId);
        AttendanceSite site = logic.getAttendanceSite(siteId);
        site.setMaximumGrade(100.0);
        site.setGradingMethod(0);
        logic.updateAttendanceSite(site);
        AttendanceGrade grade = new AttendanceGrade(site, "student");
        grade.setGrade(87.0);
        logic.updateAttendanceGrade(grade);
        AttendanceEvent event = new AttendanceEvent();
        event.setAttendanceSite(site);
        event.setName("Class");
        logic.updateAttendanceEvent(event);
        AttendanceRecord record = new AttendanceRecord(event, "student", Status.PRESENT);
        logic.updateAttendanceRecord(record, Status.UNKNOWN);
        persistence.flush();
        persistence.clear();
        assertEquals(Double.valueOf(87), logic.getAttendanceGrade("student").getGrade());
    }

    @Test
    public void rejectsStudentWritesAndForgedEventSites() {
        AttendanceSite site = logic.getAttendanceSite(UUID.randomUUID().toString());
        AttendanceEvent event = new AttendanceEvent();
        event.setAttendanceSite(site);
        event.setName("Private event");
        logic.updateAttendanceEvent(event);
        persistence.flush();
        persistence.clear();
        when(securityService.isSuperUser()).thenReturn(false);
        assertThrows(SecurityException.class, () -> logic.deleteAttendanceEvent(event));
        assertThrows(SecurityException.class, () -> logic.updateAttendanceEvent(event));
        assertThrows(SecurityException.class, () -> logic.updateAttendanceSite(site));
        assertThrows(SecurityException.class, () -> gradebook.create(site, null));
        assertThrows(SecurityException.class, () -> gradebook.sendToGradebook(new AttendanceGrade(site, "student")));

        // A caller may manage one site, but cannot submit an event ID from another site.
        when(securityService.isSuperUser()).thenReturn(true);
        AttendanceSite other = logic.getAttendanceSite(UUID.randomUUID().toString());
        persistence.flush();
        persistence.clear();
        AttendanceEvent forged = new AttendanceEvent();
        forged.setId(event.getId());
        forged.setAttendanceSite(other);
        forged.setName("Forged");
        assertThrows(SecurityException.class, () -> logic.updateAttendanceEvent(forged));
    }

    @Test
    @Transactional(transactionManager = "org.sakaiproject.springframework.orm.jpa.GlobalJpaTransactionManager", propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public void initializesStatusesWhenFirstEntryPointIsAnEventsRead() {
        String siteId = UUID.randomUUID().toString();
        org.sakaiproject.tool.api.Placement placement = org.mockito.Mockito.mock(org.sakaiproject.tool.api.Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn(siteId);
        assertTrue(logic.getAttendanceEventsForCurrentSite().isEmpty());
        assertEquals(6, logic.getActiveStatusesForCurrentSite().size());
        // The first transaction has completed: verify through a fresh service transaction.
        assertNotNull(logic.getAttendanceSite(siteId).getId());
    }
    @Test
    public void grantsManagementOnlyThroughSakaiPermissionsInTheTargetSite() {
        when(securityService.isSuperUser()).thenReturn(false);
        when(siteService.siteReference(anyString())).thenAnswer(invocation -> "/site/" + invocation.getArgument(0));
        assertFalse(sakaiProxy.canManageAttendance("observer-site"));
        when(securityService.unlock("section.role.instructor", "/site/instructor-site")).thenReturn(true);
        when(securityService.unlock("section.role.ta", "/site/ta-site")).thenReturn(true);
        when(securityService.unlock("site.upd", "/site/project-site")).thenReturn(true);
        assertTrue(sakaiProxy.canManageAttendance("instructor-site"));
        assertTrue(sakaiProxy.canManageAttendance("ta-site"));
        assertTrue(sakaiProxy.canManageAttendance("project-site"));
        assertFalse(sakaiProxy.canManageAttendance("other-site"));
        assertTrue(sakaiProxy.canAccessAttendance("instructor-site"));
        when(siteService.allowAccessSite("student-site")).thenReturn(true);
        assertTrue(sakaiProxy.canAccessAttendance("student-site"));
        assertFalse(sakaiProxy.canManageAttendance("student-site"));
        assertFalse(sakaiProxy.canAccessAttendance("other-site"));
    }

    @Test
    public void regradesTheExplicitSiteInsteadOfTheCurrentPlacement() {
        AttendanceSite target = logic.getAttendanceSite(UUID.randomUUID().toString());
        target.setMaximumGrade(100.0);
        target.setGradingMethod(1);
        logic.updateAttendanceSite(target);
        AttendanceSite current = logic.getAttendanceSite(UUID.randomUUID().toString());
        AttendanceGrade targetGrade = new AttendanceGrade(target, "student");
        targetGrade.setGrade(25.0);
        logic.updateAttendanceGrade(targetGrade);
        AttendanceGrade currentGrade = new AttendanceGrade(current, "student");
        currentGrade.setGrade(42.0);
        logic.updateAttendanceGrade(currentGrade);
        org.sakaiproject.tool.api.Placement placement = org.mockito.Mockito.mock(org.sakaiproject.tool.api.Placement.class);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn(current.getSiteID());
        org.sakaiproject.user.api.User student = org.mockito.Mockito.mock(org.sakaiproject.user.api.User.class);
        when(student.getId()).thenReturn("student");
        when(securityService.unlockUsers("section.role.student", "/site/" + target.getSiteID())).thenReturn(java.util.List.of(student));
        logic.regradeAll(target);
        persistence.flush();
        persistence.clear();
        assertEquals(Double.valueOf(100), logic.getAttendanceGrade(targetGrade.getId()).getGrade());
        assertEquals(Double.valueOf(42), logic.getAttendanceGrade(currentGrade.getId()).getGrade());
    }
    @Test
    public void deletesEventsAndRulesWithResolvableSiteScopedReferences() {
        AttendanceSite site = logic.getAttendanceSite(UUID.randomUUID().toString());
        AttendanceEvent event = new AttendanceEvent();
        event.setAttendanceSite(site);
        event.setName("Meeting to delete");
        logic.updateAttendanceEvent(event);
        GradingRule rule = new GradingRule(site);
        rule.setStatus(Status.PRESENT);
        rule.setStartRange(1);
        rule.setPoints(1.0);
        logic.addGradingRule(rule);
        persistence.flush();
        Long eventId = event.getId();
        Long ruleId = rule.getId();

        assertTrue(logic.deleteAttendanceEvent(event));
        assertTrue(logic.deleteGradingRule(rule));
        persistence.flush();
        persistence.clear();
        assertNull(logic.getAttendanceEvent(eventId));
        assertTrue(logic.getGradingRulesForSite(site).isEmpty());

        org.mockito.ArgumentCaptor<String> references = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(eventTrackingService, org.mockito.Mockito.times(2)).newEvent(
                org.mockito.ArgumentMatchers.anyString(), references.capture(), org.mockito.ArgumentMatchers.eq(true));
        assertEquals(java.util.List.of("/attendance/" + site.getSiteID() + "/event/" + eventId,
                "/attendance/" + site.getSiteID() + "/rule/" + ruleId), references.getAllValues());
        org.sakaiproject.entity.api.Reference parsed = org.mockito.Mockito.mock(org.sakaiproject.entity.api.Reference.class);
        for (String reference : references.getAllValues()) {
            assertTrue(logic.parseEntityReference(reference, parsed));
        }
        org.mockito.Mockito.verify(parsed).set("sakai.attendance", "event", eventId.toString(), null, site.getSiteID());
        org.mockito.Mockito.verify(parsed).set("sakai.attendance", "rule", ruleId.toString(), null, site.getSiteID());
    }

    @Test
    public void rejectsUnrelatedAndMalformedEntityReferences() {
        org.sakaiproject.entity.api.Reference parsed = org.mockito.Mockito.mock(org.sakaiproject.entity.api.Reference.class);
        assertFalse(logic.parseEntityReference(null, parsed));
        for (String reference : java.util.List.of("42", "/other/site/event/42", "/attendance/site",
                "/attendance//event/42", "/attendance/site/event/", "/attendance/site/unknown/42",
                "/attendance/site/event/42/extra")) {
            assertFalse(reference, logic.parseEntityReference(reference, parsed));
        }
        org.mockito.Mockito.verifyNoInteractions(parsed);
    }

}
