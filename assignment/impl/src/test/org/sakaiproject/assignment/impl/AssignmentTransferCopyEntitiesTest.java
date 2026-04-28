/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AssignmentTestConfiguration.class})
public class AssignmentTransferCopyEntitiesTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private AssignmentService assignmentService;
    @Resource(name = "org.sakaiproject.announcement.api.AnnouncementService")
    private AnnouncementService announcementService;
    @Autowired private EntityManager entityManager;
    @Autowired private FormattedText formattedText;
    @Autowired private SecurityService securityService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Resource(name = "org.sakaiproject.time.api.UserTimeService")
    private UserTimeService userTimeService;
    @Autowired private UserDirectoryService userDirectoryService;
    private ResourceLoader resourceLoader;

    @Before
    public void setUp() {
        when(serverConfigurationService.getAccessUrl()).thenReturn("http://localhost:8080/access");
        resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        when(resourceLoader.getFormattedMessage(eq("assig6"), any())).thenReturn("Open Assignment");
        when(resourceLoader.getFormattedMessage(eq("opedat"), any(), any())).thenReturn("Open Date Body");
        when(formattedText.convertPlaintextToFormattedText(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityManager.newReferenceList()).thenReturn(Collections.emptyList());
        when(serverConfigurationService.getBoolean(eq("import.importAsDraft"), anyBoolean())).thenReturn(true);
        when(serverConfigurationService.getBoolean(eq("assignment.import.importAsDraft"), anyBoolean())).thenReturn(true);
        when(userTimeService.getLocalTimeZone()).thenReturn(TimeZone.getTimeZone("UTC"));
        when(securityService.unlockUsers(anyString(), anyString())).thenReturn(Collections.emptyList());

        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn("test-user-id");
        when(userDirectoryService.getCurrentUser()).thenReturn(currentUser);
        when(sessionManager.getCurrentSessionUserId()).thenReturn("test-user-id");

        getAssignmentServiceImpl().setResourceLoader(resourceLoader);
    }

    @Test
    public void transferCopyEntitiesRecreatesOpenDateAnnouncementFromExistingSourceLink() throws Exception {

        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        Assignment sourceAssignment = createPublishedAssignment(fromContext);
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());
        sourceAssignment.getProperties().put(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED, Boolean.TRUE.toString());
        sourceAssignment.getProperties().put(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID, "source-announcement-id");
        updateAssignment(sourceAssignment);

        String toChannelId = "/announcement/" + toContext + "/main";
        AnnouncementChannel announcementChannel = mock(AnnouncementChannel.class);
        AnnouncementMessageEdit message = mock(AnnouncementMessageEdit.class);
        AnnouncementMessageHeaderEdit header = mock(AnnouncementMessageHeaderEdit.class);
        ResourcePropertiesEdit messageProperties = mock(ResourcePropertiesEdit.class);

        when(announcementService.channelReference(toContext, SiteService.MAIN_CONTAINER)).thenReturn(toChannelId);
        when(announcementService.getAnnouncementChannel(toChannelId)).thenReturn(announcementChannel);
        when(announcementChannel.addAnnouncementMessage()).thenReturn(message);
        when(message.getAnnouncementHeaderEdit()).thenReturn(header);
        when(message.getPropertiesEdit()).thenReturn(messageProperties);
        when(message.getId()).thenReturn("imported-announcement-id");

        stubContextPermissions(toContext);

        Map<String, String> copied = getAssignmentServiceImpl().transferCopyEntities(fromContext, toContext, null,
            List.of(EntityTransferrer.PUBLISH_OPTION));

        assertTrue(copied.containsKey("assignment/" + sourceAssignment.getId()));

        Collection<Assignment> importedAssignments = assignmentService.getAssignmentsForContext(toContext);
        assertEquals(1, importedAssignments.size());
        Assignment importedAssignment = importedAssignments.iterator().next();
        assertEquals("imported-announcement-id",
            importedAssignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
        assertEquals(Boolean.TRUE.toString(),
            importedAssignment.getProperties().get(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
        assertEquals(Boolean.TRUE.toString(),
            importedAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
        verify(messageProperties, never()).addProperty(eq(AnnouncementService.RELEASE_DATE), anyString());
        verify(announcementChannel).commitMessage(message, 0, "org.sakaiproject.announcement.impl.SiteEmailNotificationAnnc");
    }

    @Test
    public void transferCopyEntitiesRecreatesDraftOpenDateAnnouncementWhenImportDefaultsToDraft() throws Exception {

        String fromContext = UUID.randomUUID().toString();
        String toContext = UUID.randomUUID().toString();
        Assignment sourceAssignment = createPublishedAssignment(fromContext);
        sourceAssignment.getProperties().put(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE, Boolean.FALSE.toString());
        sourceAssignment.getProperties().put(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED, Boolean.TRUE.toString());
        sourceAssignment.getProperties().put(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID, "source-announcement-id");
        updateAssignment(sourceAssignment);

        String toChannelId = "/announcement/" + toContext + "/main";
        AnnouncementChannel announcementChannel = mock(AnnouncementChannel.class);
        AnnouncementMessageEdit message = mock(AnnouncementMessageEdit.class);
        AnnouncementMessageHeaderEdit header = mock(AnnouncementMessageHeaderEdit.class);
        ResourcePropertiesEdit messageProperties = mock(ResourcePropertiesEdit.class);

        when(announcementService.channelReference(toContext, SiteService.MAIN_CONTAINER)).thenReturn(toChannelId);
        when(announcementService.getAnnouncementChannel(toChannelId)).thenReturn(announcementChannel);
        when(announcementChannel.addAnnouncementMessage()).thenReturn(message);
        when(message.getAnnouncementHeaderEdit()).thenReturn(header);
        when(message.getPropertiesEdit()).thenReturn(messageProperties);
        when(message.getId()).thenReturn("draft-imported-announcement-id");

        stubContextPermissions(toContext);

        Map<String, String> copied = getAssignmentServiceImpl().transferCopyEntities(fromContext, toContext, null, null);

        assertTrue(copied.containsKey("assignment/" + sourceAssignment.getId()));

        Collection<Assignment> importedAssignments = assignmentService.getAssignmentsForContext(toContext);
        assertEquals(1, importedAssignments.size());
        Assignment importedAssignment = importedAssignments.iterator().next();
        assertTrue(importedAssignment.getDraft());
        assertEquals("draft-imported-announcement-id",
            importedAssignment.getProperties().get(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID));
        assertEquals(Boolean.TRUE.toString(),
            importedAssignment.getProperties().get(ResourceProperties.NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE));
        Assert.assertNull(importedAssignment.getProperties().get(AssignmentConstants.NEW_ASSIGNMENT_OPEN_DATE_ANNOUNCED));
        verify(messageProperties, never()).addProperty(eq(AnnouncementService.RELEASE_DATE), anyString());
        verify(header).setDraft(true);
        verify(announcementChannel).commitMessage(message, 0, "org.sakaiproject.announcement.impl.SiteEmailNotificationAnnc");
    }

    private Assignment createPublishedAssignment(String context) {

        stubContextPermissions(context);

        try {
            Assignment assignment = assignmentService.addAssignment(context);
            assignment.setTitle("Assignment " + context);
            assignment.setDraft(false);
            assignment.setOpenDate(Instant.now().minus(2, ChronoUnit.DAYS));
            assignment.setDueDate(Instant.now().plus(2, ChronoUnit.DAYS));
            assignment.setCloseDate(Instant.now().plus(3, ChronoUnit.DAYS));
            assignment.setHideDueDate(false);
            assignment.setTypeOfAccess(Assignment.Access.SITE);
            assignment.getProperties().clear();
            assignment.getProperties().put("newAssignment", Boolean.FALSE.toString());
            assignment.getProperties().put(AssignmentConstants.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK,
                AssignmentConstants.GRADEBOOK_INTEGRATION_NO);
            assignmentService.updateAssignment(assignment);
            return assignment;
        } catch (Exception e) {
            Assert.fail(e.toString());
            return null;
        }
    }

    private void updateAssignment(Assignment assignment) {

        assertNotNull(assignment);
        String contextReference = AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, contextReference)).thenReturn(true);

        try {
            assignmentService.updateAssignment(assignment);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    private void stubContextPermissions(String context) {

        String contextReference = AssignmentReferenceReckoner.reckoner().context(context).reckon().getReference();
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT, contextReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, contextReference)).thenReturn(true);
        when(securityService.unlock(AssignmentServiceConstants.SECURE_UPDATE_ASSIGNMENT, contextReference)).thenReturn(true);
    }

    private AssignmentServiceImpl getAssignmentServiceImpl() {

        return (AssignmentServiceImpl) AopTestUtils.getTargetObject(assignmentService);
    }
}
