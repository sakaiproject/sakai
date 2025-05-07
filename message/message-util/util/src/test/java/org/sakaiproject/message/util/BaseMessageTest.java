/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2025 The Apereo Foundation
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

package org.sakaiproject.message.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.message.api.MessageService;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Element;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BaseMessageTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Message message;

    @Mock
    private Reference reference;

    @Mock
    private MessageHeader messageHeader;

    @Mock
    private ResourceProperties messageProperties;

    private BaseMessageTestImpl baseMessage;

    @Before
    public void setUp() {
        baseMessage = new BaseMessageTestImpl();
        baseMessage.setEntityManager(entityManager);

        // Set up common mocks
        when(message.getReference()).thenReturn("message-reference");
        when(entityManager.newReference("message-reference")).thenReturn(reference);
        when(reference.getContext()).thenReturn("site-id");
        when(message.getHeader()).thenReturn(messageHeader);
        when(message.getProperties()).thenReturn(messageProperties);
    }

    @Test
    public void testIsMessageViewableDraft() {
        // Setup a draft message
        when(messageHeader.getDraft()).thenReturn(true);

        // A draft message should not be viewable
        assertFalse("Draft messages should not be viewable", baseMessage.isMessageViewable(message));
    }

    @Test
    public void testIsMessageViewableNoDates() throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
        // Setup a non-draft message with no release or retract dates
        when(messageHeader.getDraft()).thenReturn(false);
        when(messageProperties.getInstantProperty(MessageService.RELEASE_DATE))
            .thenThrow(EntityPropertyNotDefinedException.class);
        when(messageProperties.getInstantProperty(MessageService.RETRACT_DATE))
            .thenThrow(EntityPropertyNotDefinedException.class);

        // A message with no dates should be viewable
        assertTrue("Message with no dates should be viewable", baseMessage.isMessageViewable(message));
    }

    @Test
    public void testIsMessageViewableFutureReleaseDate() throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
        // Setup a message with a future release date
        when(messageHeader.getDraft()).thenReturn(false);
        Instant futureDate = Instant.now().plus(1, ChronoUnit.DAYS);
        when(messageProperties.getInstantProperty(MessageService.RELEASE_DATE)).thenReturn(futureDate);
        when(messageProperties.getInstantProperty(MessageService.RETRACT_DATE))
            .thenThrow(EntityPropertyNotDefinedException.class);

        // A message with a future release date should not be viewable
        assertFalse("Message with future release date should not be viewable", baseMessage.isMessageViewable(message));
    }

    @Test
    public void testIsMessageViewablePastReleaseDate() throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
        // Setup a message with a past release date
        when(messageHeader.getDraft()).thenReturn(false);
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
        when(messageProperties.getInstantProperty(MessageService.RELEASE_DATE)).thenReturn(pastDate);
        when(messageProperties.getInstantProperty(MessageService.RETRACT_DATE))
            .thenThrow(EntityPropertyNotDefinedException.class);

        // A message with a past release date should be viewable
        assertTrue("Message with past release date should be viewable", baseMessage.isMessageViewable(message));
    }

    @Test
    public void testIsMessageViewablePastRetractDate() throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
        // Setup a message with a past retract date
        when(messageHeader.getDraft()).thenReturn(false);
        when(messageProperties.getInstantProperty(MessageService.RELEASE_DATE))
            .thenThrow(EntityPropertyNotDefinedException.class);
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
        when(messageProperties.getInstantProperty(MessageService.RETRACT_DATE)).thenReturn(pastDate);

        // A message with a past retract date should not be viewable
        assertFalse("Message with past retract date should not be viewable", baseMessage.isMessageViewable(message));
    }

    @Test
    public void testIsMessageViewableFutureRetractDate() throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
        // Setup a message with a future retract date
        when(messageHeader.getDraft()).thenReturn(false);
        when(messageProperties.getInstantProperty(MessageService.RELEASE_DATE))
            .thenThrow(EntityPropertyNotDefinedException.class);
        Instant futureDate = Instant.now().plus(1, ChronoUnit.DAYS);
        when(messageProperties.getInstantProperty(MessageService.RETRACT_DATE)).thenReturn(futureDate);

        // A message with a future retract date should be viewable
        assertTrue("Message with future retract date should be viewable", baseMessage.isMessageViewable(message));
    }

    @Test
    public void testIsMessageViewableBetweenReleaseDateAndRetractDate() throws EntityPropertyNotDefinedException, EntityPropertyTypeException {
        // Setup a message with a past release date and future retract date
        when(messageHeader.getDraft()).thenReturn(false);
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant futureDate = Instant.now().plus(1, ChronoUnit.DAYS);
        when(messageProperties.getInstantProperty(MessageService.RELEASE_DATE)).thenReturn(pastDate);
        when(messageProperties.getInstantProperty(MessageService.RETRACT_DATE)).thenReturn(futureDate);

        // A message between release and retract dates should be viewable
        assertTrue("Message between release and retract dates should be viewable", baseMessage.isMessageViewable(message));
    }

    /**
     * A concrete implementation of BaseMessage for testing
     */
    private static class BaseMessageTestImpl extends BaseMessage {
        @Override
        public void setEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        // Need to implement this abstract method to instantiate BaseMessage
        @Override
        public String serviceName() {
            return "MessageTestService";
        }

        @Override
        protected Storage newStorage() {
            return null;
        }

        @Override
        protected MessageHeaderEdit newMessageHeader(Message msg, String id) {
            return null;
        }

        @Override
        protected MessageHeaderEdit newMessageHeader(Message msg, Element el) {
            return null;
        }

        @Override
        protected MessageHeaderEdit newMessageHeader(Message msg, MessageHeader other) {
            return null;
        }

        @Override
        protected String eventId(String secure) {
            return "";
        }

        @Override
        protected String getReferenceRoot() {
            return "/test-reference-root";
        }

        @Override
        public String[] summarizableToolIds() {
            return new String[0];
        }

        @Override
        public Entity newContainer(String ref) {
            return null;
        }

        @Override
        public Entity newContainer(Element element) {
            return null;
        }

        @Override
        public Entity newContainer(Entity other) {
            return null;
        }

        @Override
        public Edit newContainerEdit(String ref) {
            return null;
        }

        @Override
        public Edit newContainerEdit(Element element) {
            return null;
        }

        @Override
        public Edit newContainerEdit(Entity other) {
            return null;
        }

        @Override
        public String getOwnerId(Entity r) {
            return "";
        }

        @Override
        public boolean isDraft(Entity r) {
            return false;
        }

        @Override
        public Time getDate(Entity r) {
            return null;
        }

        @Override
        public Entity newResource(Entity container, String id, Object[] others) {
            return null;
        }

        @Override
        public Entity newResource(Entity container, Element element) {
            return null;
        }

        @Override
        public Entity newResource(Entity container, Entity other) {
            return null;
        }

        @Override
        public Edit newResourceEdit(Entity container, String id, Object[] others) {
            return null;
        }

        @Override
        public Edit newResourceEdit(Entity container, Element element) {
            return null;
        }

        @Override
        public Edit newResourceEdit(Entity container, Entity other) {
            return null;
        }

        @Override
        public Object[] storageFields(Entity r) {
            return new Object[0];
        }
    }
}
