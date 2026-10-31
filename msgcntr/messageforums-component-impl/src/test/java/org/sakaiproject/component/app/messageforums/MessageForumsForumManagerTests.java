/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.messageforums;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.common.type.TypeManager;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MsgcntrTestConfiguration.class})
public class MessageForumsForumManagerTests extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private MessageForumsForumManager forums;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private TypeManager types;
    @Autowired @Qualifier("org.sakaiproject.tool.api.ToolManager") private ToolManager toolManager;

    @Test
    public void findForumsByTypeAndContextIncludesTopicsAndExcludesOtherSitesAndTypes() {
        Type type = mock(Type.class);
        when(type.getUuid()).thenReturn("discussion");
        when(types.createType(any(), any(), any(), any(), any())).thenReturn(type);
        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn("test-context");
        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        DiscussionForum expected = persistForum("test-context", "discussion");
        persistForum("other-site", "discussion");
        persistForum("test-context", "other-type");
        DiscussionTopic topic = forums.createDiscussionForumTopic(expected);
        topic.setUuid(UUID.randomUUID().toString());
        topic.setTypeUuid("discussion");
        topic.setTitle("Imported topic");
        topic.setCreatedBy("importer");
        topic.setModifiedBy("importer");
        topic.setModified(Date.from(Instant.now()));
        topic.setSortIndex(1);
        topic.setMutable(true);
        expected.addTopic(topic);
        Session session = sessionFactory.getCurrentSession();
        session.persist(topic);
        session.flush();
        session.clear();

        assertImportedForum(forums.getForumByTypeAndContext("discussion", "test-context"), expected.getId());
        session.clear();
        assertImportedForum(forums.getForumByTypeAndContext("discussion"), expected.getId());
    }

    private void assertImportedForum(List<?> results, Long expectedId) {
        assertEquals(1, results.size());
        DiscussionForum actual = (DiscussionForum) results.get(0);
        assertEquals(expectedId, actual.getId());
        assertTrue(Hibernate.isInitialized(actual.getTopicsSet()));
        assertEquals(1, actual.getTopics().size());
        assertEquals("Imported topic", ((DiscussionTopic) actual.getTopics().get(0)).getTitle());
    }

    private DiscussionForum persistForum(String context, String type) {
        Date now = Date.from(Instant.now());
        AreaImpl area = new AreaImpl();
        area.setUuid(UUID.randomUUID().toString());
        area.setContextId(context);
        area.setName("Imported area");
        area.setTypeUuid(type);
        area.setCreated(now);
        area.setModified(now);
        area.setCreatedBy("importer");
        area.setModifiedBy("importer");
        area.setAutoMarkThreadsRead(false);
        area.setEnabled(true);
        area.setHidden(false);
        area.setLocked(false);
        area.setModerated(false);
        area.setPostFirst(false);
        sessionFactory.getCurrentSession().persist(area);

        DiscussionForum forum = forums.createDiscussionForum();
        forum.setUuid(UUID.randomUUID().toString());
        forum.setTypeUuid(type);
        forum.setTitle("Imported forum");
        forum.setArea(area);
        forum.setCreatedBy("importer");
        forum.setModifiedBy("importer");
        forum.setModified(now);
        forum.setSortIndex(1);
        sessionFactory.getCurrentSession().persist(forum);
        return forum;
    }
}
